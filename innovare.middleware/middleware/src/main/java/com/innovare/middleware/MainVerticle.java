package com.innovare.middleware;

import java.io.FileNotFoundException;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.control.Classificator;
import com.innovare.control.ConfigurationController;
import com.innovare.control.IrrigationController;
import com.innovare.control.Logger;
import com.innovare.control.LoggingController;
import com.innovare.control.ModelController;
import com.innovare.control.SampleCSVController;
import com.innovare.control.SensorDataController;
import com.innovare.model.Campo;
import com.innovare.model.ClassificationSint;
import com.innovare.model.IrrigationState;
import com.innovare.model.Irrigazione;
import com.innovare.model.Model;
import com.innovare.model.PlantClassification;
import com.innovare.model.Sample;
import com.innovare.model.User;
import com.innovare.utils.NoUserLogException;
import com.innovare.utils.Utilities;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.messages.MqttPublishMessage;
import net.lingala.zip4j.exception.ZipException;

public class MainVerticle extends AbstractVerticle {

	
	/*
	 * IMPORTANTE:Il tipo password utilizzato per i json per rendere sicure le password non è supportato e se utilizzato 
	 * 			  manda in crash il programma.
	 * 			
	 * 			Le get del login in fase di test hanno generato una risposta strana da parte del server che rispondeva
	 * 			correttamente con codice di ritorno 200 ma generava anche un successivo codice 500 internal server error.
	 * 		
	 * 
	 * 
	 * 
	 * 
	 * 	
	 */
	
	private final String LOG="MAIN-VERTICLE-LOG: ";
	private JsonArray configuration ;
	
	//private MongoClient mongo=null;
	
	//private User userLog;
	/*
	 * Usare jackson per fare mapping java / json
	 * 
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	private HttpServer server;
	
	private int numberOfChannel=0;
	private HashMap<Integer,MqttClient> mqttClients;
	private HashMap<String,JsonObject> sampleChannelQueue;
	private MongoClient mongoClient;
	//private Model selectedModel=null;
	private ModelController modelController;
	private LoggingController loggingController;
	private MqttClient irrigationCommandClient;
	private MqttClient irrigationLog;
	private MqttClient clientWS;
	
	private SampleCSVController csvController;
	
	private String irrigationState=null;
	
	private IrrigationController irrigationController=null;
	
	public static ConfigurationController configurationController;
	
	private JobDetail job;
	private Trigger trigger;
	private Scheduler sch;
	private Irrigazione irr=null;
	
	/*
	 * AGGIUNGERE PRIORITY QUEUE DELLE CLASSIFICAZIONI, DEVE CONTENERE LE ULTIME 4 CLASSIFICAZIONI EFFETTUATE
	 */
	
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
	  //INSTANZIAZIONI INIZIALI
	  configuration = new JsonArray();
	  
	  
	  //Connessione a MongoDB    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      Logger.getLogger().print("Starting MongoConnection..");
      
	  JsonObject config = Vertx.currentContext().config();

	    String uri = config.getString("mongo_uri");
	    if (uri == null) {
	      uri = "mongodb://localhost:27017";
	    }
	    Logger.getLogger().print(uri);

	    String db = config.getString("mongo_db");
	    if (db == null) {
	      db = "innovare";
	    }
	    Logger.getLogger().print(db);
	   
	    JsonObject mongoconfig = new JsonObject()
	        .put("connection_string", uri)
	        .put("db_name", db);
	    	
	    mongoClient = MongoClient.createShared(vertx, mongoconfig);

	  
	  //TEST MONGODB
	  JsonObject query = new JsonObject();
	  mongoClient.find("Utenti", query, res -> {
	    if (res.succeeded()) {
	      for (JsonObject json : res.result()) {
	        Logger.getLogger().print(json.encodePrettily());
	        Logger.getLogger().print("Connessione effettuata con successo al db!");
	      }
	    } else {
	      res.cause().printStackTrace();
	    
	    }
	  });
	  
	  
	  
	  //TEST====================================================================================================
	  /*
	   * CONFIGURATION CONTROLLER===============================================================================
	   */
	  configurationController= new ConfigurationController();
	  SensorDataController sc= new SensorDataController(mongoClient, configurationController.getChannelForClassification());
	  try {
			Future<HashMap<Integer, Sample>> result =sc.getLastSamples();
			result.onComplete(res->{
				HashMap<Integer, Sample> rr=res.result();
				
				for(Integer i:  rr.keySet())
					Logger.getLogger().print("Channel: "+i+" value: "+rr.get(i).toString() );
			});
			
			
	  } catch (Exception e) {
			System.err.println("FAIL");
			e.printStackTrace();
	  }
	  //=====================================================================================================
	  
	  
	  
	  /*
	   * Creazione del MODEL CONTROLLER   =====================================================================================================
	   */
	  this.modelController= new ModelController();
	  Logger.getLogger().print(this.LOG+": modelController creato: "+this.modelController.toString());
	  /*
	   *Recuperiamo il modello selezionato in precedenza 
	   */
	  JsonObject querySelectedModel = new JsonObject();
	  Logger.getLogger().print(this.LOG+" Avvio ricerca modello selezionato.");
	  mongoClient.find("selectedModel", querySelectedModel, res -> {
	    if (res.succeeded()) {
		    	
		    if(!res.result().isEmpty()){
		      /*
		       * Esiste un unico documento in questa collezione
		       * Ricerchiamo il modello selezionato in precedenza nel db e lo impostiamo come modello selezionato
		       */
		    	JsonObject jsonSelectedModel=res.result().get(0);
		    	Logger.getLogger().print(this.LOG+"Modello selezionato predentemente: "+jsonSelectedModel.encodePrettily());
		    	Logger.getLogger().print(this.LOG+"Modello selezionato predentemente: "+jsonSelectedModel.encodePrettily());
		    	Model selectedModel;
				try {
					selectedModel = new ObjectMapper().readValue(jsonSelectedModel.toString(), Model.class);
					this.modelController.setModelSelected(selectedModel.getName());
				} catch (JsonProcessingException e) {
					System.err.println("Errore parsing json del modello selezionato");
					System.err.println("Verra impostato a null il modello selezionato.");
					Logger.getLogger().print("Errore parsing json del modello selezionato, verra' impostato a null il modello selezionato.");
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					System.err.println("Il modello che è stato selezionato in precedenza non è più disponibile nel fileSystem!");
					System.err.println("Verra impostato a null il modello selezionato.");
					Logger.getLogger().print("Il modello che è stato selezionato in precedenza non è più disponibile nel fileSystem!Verra' impostato a null il modello selezionato.");
					e.printStackTrace();
				}
	    	}
		    //Logger.getLogger().print(this.LOG+"Nessun modello selezionato.");mongoClient
		    
	    } else {
	      res.cause().printStackTrace();
	    
	    }
	  });
	  
	  /*
	   * LOGGIN CONTROLLER   =====================================================================================================
	   */
	  this.loggingController= new LoggingController();
	  Logger.getLogger().print(this.LOG+": loggingController creato: "+this.loggingController.toString());
	  /*
	   * MQTT CLIENT        =====================================================================================================
	   * 
	   */	  
	  /*
	   * Colleghiamo ed iscriviamo il client al canale del suo corrispondente channel
	   */
	  Logger.getLogger().print("Creazione struttura dati ultimi sample");
	  this.sampleChannelQueue= new HashMap<String,JsonObject>();
	  
	  //Creazione client MQTT per cattura log dei gateway
	  MqttClient clientLog= MqttClient.create(vertx);
	  clientLog.connect(1883, "localhost", s -> {
		  
		  clientLog.publishHandler(c -> {
	    		//Ogni qual volta viene pubblicata una misura la stampiamo e la salviamo.
				  //Logger.getLogger().print("There are new message in topic: " + c.topicName());
	    		  //Logger.getLogger().print("Content(as string) of the message: " + c.payload().toString());
	    		  //Logger.getLogger().print("QoS: " + c.qosLevel());	
	    		  Logger.getLogger().print("LOG-GATEWAY: "+c.payload().toString());
	    		  
	    		  if(c.payload().toString().startsWith("channelNumber:")) {
	    			  String num=c.payload().toString().substring(14);
	    			  Logger.getLogger().print("Numero di channel: "+num);
	    			  Logger.getLogger().print("Numero di channel: "+num);
	    			  
	    			  this.numberOfChannel= Integer.parseInt(num);
	    			  //Avvio creazione client per la ricezione dei valori dei canali
	    			  //this.mqttClientCreation(); //DEBUGGGGG TEST NUOVO CLIENT
	    			  this.setClientWeatherStation();
	    		  }
	    		  
	    		  //Salviamo i configurationItem
	    		  if(c.payload().toString().contains("id")) {
	    			 this.configuration.add(new JsonObject(c.payload().toString()));
	    			 Logger.getLogger().print("LOG-GATEWAY: Aggiunto "+c.payload().toString()+" alle configurazioni.");
	    		  }
	    		  //JsonObject confJson= new JsonObject( c.payload().toString());
	    		})
	    		  .subscribe("gatewayLog", 2);	    
	    });
	  
	  /*
	   * MQTT CLIENT LOG IRRIGAZIONE    =====================================================================================================
	   * 
	   * Client per l'invio di comandi 
	   */
	  Logger.getLogger().print("Creazione client mqtt per i comandi di irrigazione.");
	  this.irrigationCommandClient= MqttClient.create(vertx);
	  
	  /*
	   * Client per la ricezione del log dell'irrigazione. 
	   */
	  Logger.getLogger().print("Creazione client mqtt per il log dell'irrigazione.");
	  this.irrigationLog= MqttClient.create(vertx);
	  this.irrigationLog.connect(1883, Utilities.ipMqtt, s ->{
		  irrigationLog.publishHandler(c -> {
	    		  
	    		  Logger.getLogger().print(Utilities.irrigationLogMqttChannel+":"+c.payload().toString());
	    		  //Modifichiamo lo stato dell'irrigazione corrente
	    		  if(c.payload().toString().contains(Utilities.stateOn)) {
	    			  this.irrigationState=Utilities.stateOn;
	    			  Logger.getLogger().print("---DEBUG IRRIGAZIONE---- stato irrigazione modificato ON");
	    		  }
	    		  else
	    			  if(c.payload().toString().contains(Utilities.stateOff)) {
	    				  this.irrigationState=Utilities.stateOff;
	    				  Logger.getLogger().print("---DEBUG IRRIGAZIONE---- stato irrigazione modificato OFF");
	    			  }
	    		  
	    		  //JsonObject confJson= new JsonObject( c.payload().toString());
	    		})
	    		  .subscribe(Utilities.irrigationLogMqttChannel, 2);	  
	  });
	  
	  
	  /*
	   *CSV CONTROLLER ed avvio del thread
	   */
	  
	  this.csvController= new SampleCSVController(MongoClient.createShared(vertx, mongoconfig));
	  csvController.start();
	  
	  
	  /*
	   * IRRIGATION-CONTROLLER avvio	=====================================================================================================
	   */
	  //Avvio irrigation Controller
	  irrigationControllerCreation(MongoClient.createShared(vertx, mongoconfig),this.configurationController.getTimeIrrigation());
	  //this.irrigationController =new IrrigationController(MongoClient.createShared(vertx, mongoconfig),this.irrigationCommandClient);
	  /*
	    this.timer = new Timer();
	    Calendar dateC = Calendar.getInstance();
	    dateC.set(
	      Calendar.DAY_OF_WEEK,
	      Calendar.SUNDAY
	    );
	    dateC.set(Calendar.HOUR, 0);
	    dateC.set(Calendar.MINUTE, 0);
	    dateC.set(Calendar.SECOND, 0);
	    dateC.set(Calendar.MILLISECOND, 0);
	    this.irrigationController= new IrrigationController(MongoClient.createShared(vertx, mongoconfig),this.irrigationCommandClient);
	    Logger.getLogger().print("DEBUG: irrigationController  started");
	    // Schedule to run 
	    /*timer.schedule(
	      this.irrigationController,
	      120000,
	      120000*5
	     );
	  	*/
	    /*
	     * Andiamo a definire tra quanto tempo partira' la prima irrigazione e la frequanza dell'irrigazione
	     *
	    this.timer.schedule(
	    		this.irrigationController,
	    		this.irrigationController.delayFromNewIrrigation(this.irrigationController.getStartingTimeIrrigation()),
	    		//this.irrigationController.delayDay
	    		this.irrigationController.delayOneMinutetTest  //TEST
	    );
	    */
	    
	    
	    
	    
	    ///=====================================================================================================    
	    /*
	     * DEFINIZIONE CHIAMATE REST
	     * 
	     */
	    
	  //Impostazioni per l'api rest per la comunicazione con la DASHBOARD
	  OpenAPI3RouterFactory.create(this.vertx, "resources/InnovareMiddleware.yaml", ar -> {
    	  if (ar.succeeded()) {
    		  
    		//Router per la richiesta della configuratone attuale  
    	    OpenAPI3RouterFactory routerFactory = ar.result(); // (1)
    	   /* routerFactory.addHandlerByOperationId("actualConfiguration", routingContext ->{
    	    	
    	    	mongoClient.find("ConfigurationItem", query, res -> {
    	    		  if (res.succeeded()) {
    	    			  for (JsonObject json : res.result()) {
    	    				  Logger.getLogger().print(json.encodePrettily());
    	    		      List<JsonObject> configurations= res.result();
    	    		      
    	    		      routingContext
		    	   	      .response()
			              .setStatusCode(200)
			              .putHeader(HttpHeaders.CONTENT_TYPE, "applivation/json")
			              .end(new JsonArray(configurations).encode());	//Ritorniamo la lista dei json dei ConfigurationItem che ci sono fino ad ora.
    	    		      //Nel json prodotto è presente anche _id di mongodb, da eliminare
    	    		    }
    	    		  } else {
    	    		    res.cause().printStackTrace();
    	    		  }
    	    		});
    	    });
    	   */
    	    //Login     
    	    routerFactory.addHandlerByOperationId("login", routingContext ->{
    	    	//Controlliamo che non si già loggato
    	    	if(!this.loggingController.isUserLogged()) {
    	    		//String username= routingContext.request().getParam("username").toString();
    	    		String username=routingContext.request().params().get("username");
        	    	//String password= routingContext.request().getParam("password").toString();
    	    		String password=routingContext.request().params().get("password");
        	    	Logger.getLogger().print("Utente che cerca di loggarsi nel sistema: "+username+" "+password);	//Stampa di TEST
        	    	/*RequestParameters params = routingContext.get("parsedParameters"); // (1)
        	    	String username = params.pathParameter("username").toString();
        	    	String password = params.pathParameter("password").toString();*/
        	    	
        	    	//Verifichiamo che l'utente loggato sia corretto andando ad effettuare una chiamata al database MongoDb.
        	    	 JsonObject q = new JsonObject()
        	    			 .put("username", username)
        	    			 .put("password", password);
        	    	 
        	    	 mongoClient.findOne("Utenti", q, null , res -> {
        	    		 if (res.succeeded() && (res.result()!=null)) {
							Logger.getLogger().print("test login: "+res.result());
        	    			 if(this.loggingController.logIn(res.result().toString())) {
        	    				 try {
    								Logger.getLogger().print("Conferma login effettuato per utente: user: "+this.loggingController.getUserLogged().toString());
    								Logger.getLogger().print("Conferma login effettuato per utente: user: "+this.loggingController.getUserLogged().toString());
    								routingContext
								 	.response()
								 	.setStatusCode(200)
								 	.end( this.loggingController.getUserLogged().getRole().toString()  );
    							 }catch(NoUserLogException e) {
    								// TODO Auto-generated catch block
    								Logger.getLogger().print("Errore toString user loggato");
    								Logger.getLogger().print("Errore toString user loggato");
    								 routingContext
	       	   		    	   	      .response()
	       	   			              .setStatusCode(404)
	       	   			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	       	   			              .end("ERRORE: caricamento utente");
    								e.printStackTrace();
    							}			 
            	    			
        	    			}else {
        	    				 routingContext
    	   		    	   	      .response()
    	   			              .setStatusCode(404)
    	   			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    	   			              .end("ERRORE: caricamento utente");
        	    			 }
        	    			
        	    		}else{
        	    			  Logger.getLogger().print("Utente "+username+" "+password+" non esistente.");
        	    			  Logger.getLogger().print("Utente "+username+" "+password+" non esistente.");
    		    	   	      //res.cause().printStackTrace();
    		    	   	      /*
    		    	   	       * Caso nel quale non esiste lo User
    		    	   	       */
    		    	   	   routingContext
    	    	    		.response()
    	    	    		.setStatusCode(404)
    	    	    		.end("Dati di login errati");
    		    	   	    }
    		    	   	  });
    	    	}
    	    	else {
    	    		routingContext
	    	   	      .response()
		              .setStatusCode(401)
		              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
		              .end("Non autorizzato: già loggato.");
    	    	}
    	    	
    	    	
    	    });
    	    	
    	    
    	    /*
    	     * LOGOUT
    	     */
    	    routerFactory.addHandlerByOperationId("logout", routingContext ->{
    	    	
    	    	if(this.loggingController.isUserLogged()) {
    	    		this.loggingController.logout();
    	    		routingContext
    	    		.response()
    	    		.setStatusCode(200)
    	    		.end("Logout effettuato con successo.");
    	    	}
    	    	else {
    	    		routingContext
	    	   	      .response()
		              .setStatusCode(401)
		              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
		              .end("Non autorizzato: non sei loggato.");
    	    	}
    	    });  	
    	    
    	      
    	    /*
    	     * Ultima misura registrata in un determinato canale  	LASTSAMPLE
    	     */
    	    	    routerFactory.addHandlerByOperationId("lastsample", routingContext ->{
    	    	    	
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		String channel= routingContext.request().getParam("idcanale");
        	    	    	/*
        	    	    	 * Fallimento se il parametro non è stato inserito o non esiste il canale selezionato
        	    	    	 */
        	    	    	if(channel==null || !this.sampleChannelQueue.containsKey(channel)){
        	    	    		/*
        	    	    		 * Cerchiamo nel db l'ultimo sample se non fa parte degli ultimi sample che sono stati 
        	    	    		 * raccolti a runtime.
        	    	    		 * 
        	    	    		 */
        	    	    	
        	    	    		JsonObject q= new JsonObject();
        	    	    		Logger.getLogger().print("DEBUG LASTSAMPLE CHANNEL: "+channel);
        	    	    		Logger.getLogger().print("DEBUG LASTSAMPLE CHANNEL: "+channel);
        	    	    		this.mongoClient.find("channel-"+channel,q, res-> {
	        	    	      		/*
	        	    	      		 * Successo nel trovare i sample nel db
	        	    	      		 */
	        	    	      		if(res.succeeded()) {
	        	    	      			/*
	        	    	      			 * Prendiamo solo l'ultimo
	        	    	      			 */
	    	    	    		    	  long max=0;
	    	    	    		    	  JsonObject lastSample=new JsonObject();
	    	    	    			      for (JsonObject json : res.result()) {
	    	    	    			    	if(json.containsKey("timestamp") && (json.getLong("timestamp")>max)) {	
	    	    	    			    			lastSample=json;
	    	    	    			    	}   
	    	    	    			      }
	    	    	    			      Logger.getLogger().print("Canale: "+channel+" --- Last sample  "+lastSample.encodePrettily());
	    	    	    			      Logger.getLogger().print("Canale: "+channel+" --- Last sample  "+lastSample.encodePrettily());
	    	    	    			      /*
	    	    	    			       * Invio dell'ultimo sample registrato
	    	    	    			       * ATTENZIONE: eventualita' che il sample inviato sia un oggetto json 
	    	    	    			       * 			 vuoto, poiche' potrebbe non esserci nessun sample nel 
	    	    	    			       * 			 db per un dato canale
	    	    	    			       */
	    	    	    			      routingContext
		            	    	    	 	.response()
		            	    	    	 	.setStatusCode(200)
		            	    	    	 	.end(lastSample.toString());
	        	    	    		}else
		        	    	    		routingContext
		    			    	   	      .response()
		    				              .setStatusCode(400)
		    				              .end();
        	    	    		});
        	    	    		
        	    	    	}else {
	        	    	    		/*
	        	    	    		 * Restituiamo l'ultimo Sample registrato ma non lo eliminiamo
	        	    	    		 */
	        	    	    		JsonObject sample= this.sampleChannelQueue.get(channel);
	        	    	    		if(!sample.isEmpty()) {
	                	    	    	routingContext
	            	    	    	 	.response()
	            	    	    	 	.setStatusCode(200)
	            	    	    	 	.end(sample.toString());
	        	    	    		}else
	        	    	    			/*
	        	    	    			 * Caso nel quale non ci sono nuovi Sample
	        	    	    			 */
	    	        	    	    	routingContext
	    	    	    	    	 	.response()
	    	    	    	    	 	.setStatusCode(200)
	    	    	    	    	 	.end();
	        	    	    	}
        	    	    	
	    	    	    }else{
	    	    	    	routingContext
	    		    	   	   .response()
	    			           .setStatusCode(401)
	    			           .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	    			           .end("Non autorizzato: non sei loggato.");
	    	    	    }
    	   
    	    	    });  	
    	    		
    	    	/*
    	    	 * Restituiamo le ultime misure non ancora mostrate dalla DashBoard e le eliminiamo dall'array   LASTSAMPLES
    	    	 */   	    	    
    	    	    routerFactory.addHandlerByOperationId("lastsamples", routingContext ->{
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		String channel= routingContext.request().getParam("idcanale");
        	    	    	/*
        	    	    	 * Fallimento se non il parametro non è stato inserito o non esiste il canale selezionato
        	    	    	 */
    	    	    		Logger.getLogger().print("Chiamata REST lastsamples");
    	    	    		Logger.getLogger().print("DEBUG----LASTSAMPLE");
        	    	    	if(channel==null)
        	    	    		routingContext
    			    	   	      .response()
    				              .setStatusCode(400)
    				              .end("ERRORE CANALE NON SCRITTO CORRETTAMENTE");
        	    	    	else {
        	    	    		/*
        	    	    		 * Restituiamo gli ultimi Sample e li eliminiamo
        	    	    		 *
            	    	    	ArrayList<JsonObject> samples= this.sampleChannelQueue.get(channel);
            	    	    	
            	    	    	
            	    	    	if(!samples.isEmpty()) {
            	    	    		Logger.getLogger().print("Samples inviati: "+samples.toString());
            	    	    		routingContext
        								.response()
        								.setStatusCode(200)
        								.end(samples.toString());
        								this.sampleChannelQueue.get(channel).clear();
        	            	    */	 
        								Logger.getLogger().print("NOT IN USE");
                	    	    		routingContext
        	    	    	    	 	.response()
        	    	    	    	 	.setStatusCode(200)
        	    	    	    	 	.end();
        						/*	
            	    	    	}else {
            	    	    		Logger.getLogger().print("No new Value");
            	    	    		routingContext
    	    	    	    	 	.response()
    	    	    	    	 	.setStatusCode(200)
    	    	    	    	 	.end();
            	    	    	}*/
            	    	    	
        	    	    	}
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}
    	    	    	
    	    	    	
    	    	    }); 
    	    	 
    	    	    routerFactory.addHandlerByOperationId("daysamples", routingContext ->{
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		String channel= routingContext.request().getParam("idcanale");
        	    	    	if(channel==null || !this.sampleChannelQueue.containsKey(channel))
        	    	    		routingContext
    			    	   	      .response()
    				              .setStatusCode(400)
    				              .end();
        	    	    	else {
        	    	    		/*
        	    	    		 * Restituiamo i Samples presenti nella queue ma non li eliminiamo.
        	    	    		 
            	    	    	ArrayList<JsonObject> samples= this.sampleChannelQueue.get(channel);
            	    	    	
            	    	    	
            	    	    	if(!samples.isEmpty()) {
            	    	    		Logger.getLogger().print("Samples inviati: "+samples.toString());
            	    	    		routingContext
        								.response()
        								.setStatusCode(200)
        								.end(samples.toString());
        							
            	    	    	}else {
            	    	    		Logger.getLogger().print("No new Value");
            	    	    		routingContext
    	    	    	    	 	.response()
    	    	    	    	 	.setStatusCode(200)
    	    	    	    	 	.end();
            	    	    	}
            	    	    	*/
            	    	    	Logger.getLogger().print("NOT IN USE");
        	    	    		routingContext
	    	    	    	 	.response()
	    	    	    	 	.setStatusCode(200)
	    	    	    	 	.end();
        	    	    	}
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}	    	    	
    	    	    }); 
    	    	    
    	    	    /*
    	    	     * Restituiamo tutti i sample di un determinato canale  ALL SAMPLE
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("allsamples", routingContext ->{
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		String channel= routingContext.request().getParam("idcanale");
        	    	    	/*
        	    	    	 * Fallimento se il parametro non è stato inserito o non esiste il canale selezionato
        	    	    	 */
        	    	    	if(channel==null)
        	    	    		routingContext
    			    	   	      .response()
    				              .setStatusCode(400)
    				              .end();
        	    	    	else {
        	    	    		/*
        	    	    		 * Restituiamo tutti i Sample di un determinato canale
        	    	    		 */
        	    	    		JsonObject q= new JsonObject();
        	    	    		Logger.getLogger().print("DEBUG ALL SAMPLE CHANNEL: "+channel);
        	    	    		Logger.getLogger().print("Chiamata REST allsamples");
            	    	    	this.mongoClient.find("channel-"+channel,q, res-> {
            	    	    		/*
            	    	    		 * Successo nel trovare i sample nel db
            	    	    		 */
            	    	    		if(res.succeeded()) {
            	    	    			routingContext
        								.response()
        								.setStatusCode(200)
        								.end(res.result().toString());
            	    	    			Logger.getLogger().print("DEBUG ALL SAMPLE: "+res.result().toString());
            	    	    			Logger.getLogger().print("DEBUG ALL SAMPLE: "+res.result().toString());
            	    	    		}
            	    	    		/*
            	    	    		 * Caso di fallimento
            	    	    		 */
            	    	    		else {
            	    	    			routingContext
          			    	   	      .response()
          				              .setStatusCode(400)
          				              .end("GET-ALL-SAMPLE FAIL: No-sample-find");
            	    	    		}
            	    	    	});
            	    	    	
        	    	    	}
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}
    	    	    	
    	    	    	
    	    	    }); 
    	    	    
    	    	    /*
    	    	     * Richiesta di una nuova classificazione utilizzando un certo dataset  NEWCLASSIFICAZION OLD
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("newclassification", routingContext ->{
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		String modelName= routingContext.request().getParam("modelName");
        	    	    	String dataSet= routingContext.request().getParam("dataset");
        	    	    	Logger.getLogger().print("Chiamata REST newclassification");
        	    	    	/*
        	    	    	 * Fallimento se non il parametro non è stato inserito o non esiste il canale selezionato
        	    	    	 */
        	    	    	if(modelName==null || dataSet==null)
        	    	    		routingContext
    			    	   	      .response()
    				              .setStatusCode(400)
    				              .end("No model with this name.");
        	    	    	else {
        	    	    		/*
        	    	    		 * Avviamo una nuova classificazione
        	    	    		 */
        	    	    		Classificator c= new Classificator(dataSet,this.vertx);
        	    	    		try {
        	    	    			//Genero una nuova classificatione
            	    	    		c.newClassification(modelName);
            	    	    		
            	    	    		//Inviamo il json al front-end
            	    	    		try { 	
            	    	    			//Genero il json della classificazione
            	    	    			String jsonClassifications=c.getJsonStringLastClassification();
        								//Invio il json al front-end
            	    	    			routingContext
        								.response()
        								.setStatusCode(200)
        								.end(jsonClassifications);
            	    	    			
        								//Memorizzio nel database tutte le classificazioni
            	    	    			JsonArray newClassificationsJson= new JsonArray(jsonClassifications);
            	    	    			JsonObject singleClassification;
        								for(int i=0;i< newClassificationsJson.size() ; i++) {
        									singleClassification = newClassificationsJson.getJsonObject(i);
        					    			mongoClient.insert("classifications", singleClassification , res ->{
        						    			  if(res.succeeded()) {
        						    				  Logger.getLogger().print("Classificazione salvata correttamente nel DB.");
        						    				  Logger.getLogger().print("Classificazione salvata correttamente nel DB.");
        						    			  }else
        						    				  System.err.println("ERRORE salvataggio misura");  
        						    		});
        								}			
        							} catch (FileNotFoundException e) {
        								System.err.println("File non aperto correttamente");
        								e.printStackTrace();

        								routingContext
        				    	   	      .response()
        					              .setStatusCode(400)
        					              .end("Errore lettura file.");
        							}  
        	    	    			
        	    	    		}catch(FileNotFoundException e) {
        								System.err.println("Modello selezionato non esistente");
        								e.printStackTrace();

        								routingContext
        				    	   	      .response()
        					              .setStatusCode(400)
        					              .end("Modello selezionato non esistente");
        						}     	    	    
        	    	    		   	    	    	
        	    	    	}
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}
    	    	    	
    	    	    	
    	    	    });
    	    	    
    	    	    
    	    	    /*
    	    	     * Richiesta di una nuova CLASSIFICATION SINT
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("newclassificationSint", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST newclassificationSint");
    	    	    	if(this.loggingController.isUserLogged()) {
        	    	    	String dataSet= routingContext.request().getParam("datasetName");
        	    	    	Logger.getLogger().print("Dataset: "+dataSet);
        	    	    	Logger.getLogger().print("Dataset: "+dataSet);
        	    	    	/*
        	    	    	 * Fallimento se il parametro non è stato inserito o non esiste il canale selezionato
        	    	    	 */
        	    	    	
        	    	    	if(this.modelController.getSelectedModel()==null || dataSet==null) {
        	    	    		if(dataSet==null)
        	    	    			routingContext
	      			    	   	      .response()
	      				              .setStatusCode(400)
	      				              .end("No dataset selected.");
        	    	    		else
	        	    	    		routingContext
	    			    	   	      .response()
	    				              .setStatusCode(400)
	    				              .end("No model selected.");
        	    	    	}
        	    	    	else {
        	    	    		/*
        	    	    		 * Avviamo una nuova classificazione============================================================================= OLD =========================================
        	    	    		 *
        	    	    		Classificator c= new Classificator(dataSet,this.vertx);
        	    	    		try {
        	    	    			//Genero una nuova classificatione sintetica
            	    	    		ClassificationSint cs=c.unZipAndClassification(this.modelController.getSelectedModel().getName());
            	    	    		
            	    	    		//Inviamo il json al front-end
            	    	    		 	
            	    	    			//Genero il json della classificazione
            	    	    			String jsonClassifications=new ObjectMapper().writeValueAsString(cs);
        								//Invio il json al front-end
            	    	    			routingContext
        								.response()
        								.setStatusCode(200)
        								.end(jsonClassifications);
            	    	    			
            	    	    			/*
            	    	    			 * INSERIRE PARTE PER LA GENERAZIONE DELLA NUOVA IRRIGAZIONE
            	    	    			 * IrrigationController ic= new IrrigationController();
            	    	    			 * ic.createIrrigation(....
            	    	    			 * 
            	    	    			 * L'IRRIGAZIONE VA DI CONSEGUENZA SALVATA NEL DATABASE MONGO
            	    	    			 *
            	    	    			
        								//Memorizzio nel database tutte le classificazioni
            	    	    			//JsonArray newClassificationsJson= new JsonArray(jsonClassifications);
            	    	    			JsonObject singleClassification= new JsonObject(jsonClassifications);
            	    	    			Logger.getLogger().print("--DEBUG-- "+singleClassification.toString());
            	    	    			mongoClient.insert("ClassificazioniSintetiche", singleClassification , res ->{
  						    			  if(res.succeeded())
  						    				  Logger.getLogger().print("Classificazione sintetica salvata correttamente nel DB.");
  						    			  else
  						    				  System.err.println("ERRORE salvataggio ClassificazioniSintetiche");  
            	    	    			});
            	    	    			
            	    	    			//Salviamo nel db le classificazioni delle singole immagini
            	    	    			String imagesClassifications= c.getJsonStringLastClassification();
            	    	    			//Memorizzio nel database tutte le classificazioni
            	    	    			JsonArray newClassificationsJson= new JsonArray(imagesClassifications);
            	    	    			JsonObject singleImageClassification;
        								for(int i=0;i< newClassificationsJson.size() ; i++) {
        									singleImageClassification = newClassificationsJson.getJsonObject(i);
        					    			mongoClient.insert("classifications", singleImageClassification , res ->{
        						    			  if(res.succeeded())
        						    				  Logger.getLogger().print("Singola classificazione salvata correttamente nel DB.");
        						    			  else
        						    				  System.err.println("ERRORE salvataggio classifications");  
        						    		});
        								}	
            	    	    					
        						}catch (JsonProcessingException e) {
        								System.err.println("Errore conversione json");
        								e.printStackTrace();

        								routingContext
        				    	   	      .response()
        					              .setStatusCode(400)
        					              .end("Errore json convertito");
								}catch(FileNotFoundException e) {
        								System.err.println("Modello selezionato non esistente");
        								e.printStackTrace();

        								routingContext
        				    	   	      .response()
        					              .setStatusCode(400)
        					              .end("Modello selezionato non esistente");
        						}     	    	    
        	    	    		 */
        	    	    		//================================================================================================================= NEW ===================================
        	    	    		
        	    	    		Classificator c= new Classificator(dataSet,this.vertx);
        	    	    		Future<ClassificationSint> resultClassification= c.asincronousClassification(this.modelController.getSelectedModel().getName());
        	    	    		resultClassification.onComplete(succ->{
        	    	    			
        	    	    			//Caso di classificazione eseguita correttamente
        	    	    			if(succ.succeeded()) {
        	    	    				ClassificationSint classification= succ.result();
            	    	    			//Genero il json della classificazione
            	    	    			String jsonClassifications;
										try {
											//Converto la classificazione in Json
											jsonClassifications = new ObjectMapper().writeValueAsString(classification);
											//Invio il json al front-end
	            	    	    			routingContext
	        								.response()
	        								.setStatusCode(200)
	        								.end(jsonClassifications);
	            	    	    			
	            	    	    			//Memorizzio nel database tutte le classificazioni
	            	    	    			//JsonArray newClassificationsJson= new JsonArray(jsonClassifications);
	            	    	    			JsonObject singleClassification= new JsonObject(jsonClassifications);
	            	    	    			Logger.getLogger().print("--DEBUG-- "+singleClassification.toString());
	            	    	    			Logger.getLogger().print("--DEBUG-- "+singleClassification.toString());
	            	    	    			
	            	    	    			mongoClient.insert("ClassificazioniSintetiche", singleClassification , res ->{
	  						    			  if(res.succeeded()) {
	  						    				  Logger.getLogger().print("Classificazione sintetica salvata correttamente nel DB.");
	  						    				  Logger.getLogger().print("Classificazione sintetica salvata correttamente nel DB.");
	  						    			  }else {
	  						    				  System.err.println("ERRORE salvataggio ClassificazioniSintetiche");
	  						    				  Logger.getLogger().print("ERRORE salvataggio ClassificazioniSintetiche");
	  						    			  }
	  						    			  });
											
	            	    	    			//Salviamo nel db le classificazioni delle singole immagini
	            	    	    			String imagesClassifications= c.getJsonStringLastClassification();
	            	    	    			//Memorizzio nel database tutte le classificazioni
	            	    	    			JsonArray newClassificationsJson= new JsonArray(imagesClassifications);
	            	    	    			JsonObject singleImageClassification;
	        								for(int i=0;i< newClassificationsJson.size() ; i++) {
	        									singleImageClassification = newClassificationsJson.getJsonObject(i);
	        					    			mongoClient.insert("classifications", singleImageClassification , res ->{
	        						    			  if(res.succeeded()) {
	        						    				  Logger.getLogger().print("Singola classificazione salvata correttamente nel DB.");
	        						    				  Logger.getLogger().print("Singola classificazione salvata correttamente nel DB.");
	        						    			  }else {
	        						    				  System.err.println("ERRORE salvataggio classifications");
	        						    				  Logger.getLogger().print("ERRORE salvataggio classifications");
	        						    			  }
	        						    		});
	        								}
											
										} catch (JsonProcessingException e) {
											System.err.println("ERRORE CHIAMATA CLASSIFICATION-SINT: errore conversione classificazione in json.");
											Logger.getLogger().print("ERRORE CHIAMATA CLASSIFICATION-SINT: errore conversione classificazione in json.");
											e.printStackTrace();
											routingContext
	        				    	   	      .response()
	        					              .setStatusCode(400)
	        					              .end("Errore json convertito");
										} catch (FileNotFoundException e) {
											System.err.println("ERRORE CHIAMATA CLASSIFICATION-SINT: errore salvataggio classificazione singole immagini");
											Logger.getLogger().print("ERRORE CHIAMATA CLASSIFICATION-SINT: errore salvataggio classificazione singole immagini");
											e.printStackTrace();
										}
										
										
        								
        	    	    			}else {
        	    	    				//Caso esecuzione non eseguita correttamente
        	    	    				System.err.println("ERRORE CHIAMATA CLASSIFICATION-SINT: errore esecuzione classificazione.");
        	    	    				Logger.getLogger().print("ERRORE CHIAMATA CLASSIFICATION-SINT: errore esecuzione classificazione.");
        	    	    				routingContext
	      				    	   	      .response()
	      					              .setStatusCode(400)
	      					              .end("Errore esecuzione classificazione");
        	    	    			}
        	    	    			
        	    	    			
        	    	    		});
        	    	    		
        	    	    		
        	    	    		
        	    	    		
        	    	    	}
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}
    	    	    	
    	    	    	
    	    	    });
    	    	    
    	    	    
    	    	    /*
    	    	     * Richiesta le classificazioni di una certa data   GET-CLASSIFICATION-BY-DATE
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("getClassificationsByDate", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST getClassificationsByDate");
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		String dateString= routingContext.request().getParam("date");
    	    	    		
        	    	    	/*
        	    	    	 * Fallimento se il parametro non è stato inserito correttamente
        	    	    	 */
        	    	    	if(dateString==null)
        	    	    		routingContext
    			    	   	      .response()
    				              .setStatusCode(400)
    				              .end("No model with this name.");
        	    	    	else {
        	    	    		/*
        	    	    		 * Avviamo una nuova classificazione
        	    	    		 */
        	    	    		long date = Long.parseLong(dateString);
        	    	    		//Logger.getLogger().print(date);
        	    	    		JsonObject q= new JsonObject().put("date",date );
            	    	    	this.mongoClient.find("ClassificazioniSintetiche",q, res-> {
            	    	    		/*
            	    	    		 * Successo nel trovare i sample nel db
            	    	    		 */
            	    	    		if(res.succeeded()) {
            	    	    			Logger.getLogger().print(res.result().toString());
            	    	    			routingContext
        								.response()
        								.setStatusCode(200)
        								.end(res.result().toString());
            	    	    		}
            	    	    		/*
            	    	    		 * Caso di fallimento
            	    	    		 */
            	    	    		else {
            	    	    			Logger.getLogger().print("No-sample-find");
            	    	    			routingContext
          			    	   	      .response()
          				              .setStatusCode(400)
          				              .end("No-sample-find");
            	    	    		}
            	    	    	});
        	    	    		    	    	
        	    	    	}
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}
    	    	    	
    	    	    	
    	    	    }); 
    	    	    
    	    	    /*
    	    	     * Richiesta le classificazioni di una certa data   GET-ALL-CLASSIFICATIONS
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("getClassifications", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST getClassifications");
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		/*
    	    	    		 * Restituiamo tutte le classificazioni fatte.
    	    	    		 */
     
        	    	    		JsonObject q= new JsonObject();
            	    	    	this.mongoClient.find("ClassificazioniSintetiche",q, res-> {
            	    	    		/*
            	    	    		 * Successo nel trovare i sample nel db
            	    	    		 */
            	    	    		if(res.succeeded()) {
            	    	    			Logger.getLogger().print(res.result().toString());
            	    	    			routingContext
        								.response()
        								.setStatusCode(200)
        								.end(res.result().toString());
            	    	    		}
            	    	    		/*
            	    	    		 * Caso di fallimento
            	    	    		 */
            	    	    		else {
            	    	    			Logger.getLogger().print("No-sample-find");
            	    	    			routingContext
          			    	   	      .response()
          				              .setStatusCode(400)
          				              .end("No-sample-find");
            	    	    		}
            	    	    	});
        	    	    		    	    	
        	    	    	}
	    	    	    	else {
	    	    	    		routingContext
	    		    	   	      .response()
	    			              .setStatusCode(401)
	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	    			              .end("Non autorizzato: non sei loggato.");
	    	    	    	}
    	    	    	
    	    	    	
    	    	    }); 
    	    	    
    	    	    /*
    	    	     * Richiede le ultime 4 classificazioni   GET-LAST-CLASSIFICATIONS
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("lastclassificazion", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST lastclassificazion");
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		/*
    	    	    		 * Restituiamo tutte le classificazioni fatte.
    	    	    		 */
        	    	    		JsonObject q= new JsonObject();
            	    	    	this.mongoClient.find("ClassificazioniSintetiche",q, res-> {
            	    	    		/*
            	    	    		 * Successo nel trovare i sample nel db
            	    	    		 */
            	    	    		if(res.succeeded()) {
            	    	    			/*
            	    	    			 * Prendiamo solo le ultime 4 massimo
            	    	    			 */
            	    	    			ArrayList<ClassificationSint> csa= new ArrayList<ClassificationSint>();
            	    	    			ClassificationSint csObj;
            	    	    			for(JsonObject jo: res.result()) {
            	    	    				try {
												csObj= new ObjectMapper().readValue(jo.toString(), ClassificationSint.class);
												csa.add(csObj);
												
											} catch (JsonProcessingException e) {
												System.err.println("Errore conversione json.");
												e.printStackTrace();
											}		
            	    	    			}
            	    	    			
            	    	    			Collections.sort((List<ClassificationSint>) csa);
            	    	    			ArrayList<ClassificationSint> arrayResp= new ArrayList<ClassificationSint>();
            	    	    			Logger.getLogger().print("DEBUG GET-LAST-CLASSIFICATIONS: "+csa.toString());
            	    	    			Logger.getLogger().print("DEBUG GET-LAST-CLASSIFICATIONS: "+csa.toString());
            	    	    			
            	    	    			for(int i=0; i<4; i++) {
            	    	    				if((csa.size()-i)!=0)
            	    	    					arrayResp.add(csa.get(i));
            	    	    				else
            	    	    					i=4;
            	    	    			}
            	    	    			
            	    	    			try {
            	    	    				
											routingContext
											.response()
											.setStatusCode(200)
											.end(new ObjectMapper().writeValueAsString(arrayResp));
										} catch (JsonProcessingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
											routingContext
		          			    	   	      .response()
		          				              .setStatusCode(400)
		          				              .end("ERRORE CONVERSIONE JSON.");
										}
            	    	    		}
            	    	    		/*
            	    	    		 * Caso di fallimento
            	    	    		 */
            	    	    		else {
            	    	    			routingContext
          			    	   	      .response()
          				              .setStatusCode(400)
          				              .end("No-sample-find");
            	    	    		}
            	    	    	});
        	    	    		    	    	
        	    	    	}
	    	    	    	else {
	    	    	    		routingContext
	    		    	   	      .response()
	    			              .setStatusCode(401)
	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	    			              .end("Non autorizzato: non sei loggato.");
	    	    	    	}
    	    	    	
    	    	    	
    	    	    }); 
    	    	    
    	    	    		    	    	
    	    	    /*
    	    	     * Richiede tutti i modelli presenti nel sistema GET-MODELS
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("getModels", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST getModels");
    	    	    	if(this.loggingController.isUserLogged()) {
	    	    	    		/*
	    	    	    		 * Cerchiamo i modelli all'interno della directory 
	    	    	    		 */
	    	    	    	ArrayList<Model> models;
	    	    	    	models= this.modelController.getAllModel();
	    	    	    	JsonArray ja= new JsonArray();
	    	    			JsonObject jo;
	    	    	    	for(Model m: models) {
	    	    	    		try {
									jo= new JsonObject(new ObjectMapper().writeValueAsString(m));
									ja.add(jo);
								} catch (JsonProcessingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
	    	    	    	}
	    	    	    	Logger.getLogger().print(ja.toString());
	    	    	    	routingContext
							.response()
							.setStatusCode(200)
							.end(ja.toString());
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}
    	    	    	
    	    	    	
    	    	    			
        	    	});
    	    	 
    	    	    
    	    	    /*
    	    	     * Ritorna il modello delezionato:  SELECTED-MODEL
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("selectedModel", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST selectedModel");
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		/*
    	    	    		 * Restituisco il modello selezionato
    	    	    		 */
        	    	    	Model selectedModel=this.modelController.getSelectedModel();
        	    	    	if(selectedModel==null)
        	    	    		routingContext
    							.response()
    							.setStatusCode(400)
    							.end("NO MODEL SELECTED");
        	    	    	else {
        	    	    		try {
        	    	    			Logger.getLogger().print(new ObjectMapper().writeValueAsString(selectedModel));
    								routingContext
    								.response()
    								.setStatusCode(200)
    								.end(new ObjectMapper().writeValueAsString(selectedModel));
    							} catch (JsonProcessingException e) {
    								// TODO Auto-generated catch block
    								e.printStackTrace();
    								Logger.getLogger().print("ERROR:JSON SELECTED MODEL");
    								routingContext
    								.response()
    								.setStatusCode(400)
    								.end("ERROR:JSON SELECTED MODEL");
    							}
        	    	    	}
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}   	    	
    	    	    }); 
    	    	    
    	    	    
    	    	    /*
    	    	     * Scelta modello da utilizzare: SET-MODEL
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("setModel", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST setModel");
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		/*
    	    	    		 * Seleziono il modello da utilizzare come classificatore
    	    	    		 */
        	    	    	String modelName= routingContext.request().getParam("modelName");
        	    	    	try {
        	    	    		Logger.getLogger().print("Modello selezionato: "+modelName+"... ");
        	    	    		Logger.getLogger().print("Modello selezionato: "+modelName+"... ");
    							/*
    							 * Salviamo nel db come modello selezionato
    							 */
        	    	    		if(this.modelController.getSelectedModel() != null) {
        	    	    			/*
        	    	    			 * Caso nel quale esiste un modello già selezionato
        	    	    			 */
        	    	    			//Verifico che il modello sia nel filesystem e aggiorno il controller
        	    	    			this.modelController.foundModel(modelName);
        							this.modelController.setModelSelected(modelName);

        	    	    			JsonObject oldmodel= new JsonObject().put("name", this.modelController.getSelectedModel().getName());
        	    	    			JsonObject update = new JsonObject().put("$set", new JsonObject()
        	    	    					  .put("name", modelName));
        	    	    			mongoClient.updateCollection("selectedModel", query, update, res -> {
        	    	    				  if (res.succeeded()) {
        	    	    				    Logger.getLogger().print("Modello selezionato salvato nel db");
        	    	    				    Logger.getLogger().print("Modello selezionato salvato nel db");
        	    	    				    routingContext
    	        							.response()
    	        							.setStatusCode(200)
    	        							.end();
        	    	    				  } else {
        	    	    				    res.cause().printStackTrace();
        	    	    				    Logger.getLogger().print("ERROR: modello non salvato correttamente");
        	    	    				    routingContext
    	        							.response()
    	        							.setStatusCode(400)
    	        							.end("ERROR: modello non salvato correttamente");
        	    	    				  }
        	    	    				});
        	    	    			
        	    	    			
        	    	    		}else {
        	    	    			Logger.getLogger().print("Nessun modello è presente nel db, aggiunta nuovo modello...");
        	    	    			//Verifico che il modello sia nel filesystem e aggiorno il controller
        							this.modelController.setModelSelected(modelName);

        	    	    			Model newModel= new Model(modelName);
        	    	    			String jsonStringModel= new ObjectMapper().writeValueAsString(newModel);
        	    	    			JsonObject newModelJson = new JsonObject();		  
        	    	    			mongoClient.save("selectedModel", newModelJson, res -> {
        	    	    					  if (res.succeeded()) {
        	    	    					    Logger.getLogger().print("Modello selezionato salvato nel db");
        	    	    					    Logger.getLogger().print("Modello selezionato salvato nel db");
        	    	    					    routingContext
        	        							.response()
        	        							.setStatusCode(200)
        	        							.end();
        	    	    					  } else {
        	    	    					    res.cause().printStackTrace();
        	    	    					    routingContext
        	        							.response()
        	        							.setStatusCode(400)
        	        							.end("ERROR: modello non salvato correttamente");
        	    	    					  }
        	    	    			});    			    			
        	    	    		}  	        	    	      							
    							
    						} catch (FileNotFoundException e) {					
    							e.printStackTrace();
    							routingContext
    							.response()
    							.setStatusCode(400)
    							.end("ERROR: MODEL SELECTED NOT FOUND");
    						} catch (JsonProcessingException e) {
								System.err.println("Errore aggiunta nuovo modello nel db.Il modello non verrà selezionato.");
								Logger.getLogger().print("Errore aggiunta nuovo modello nel db.Il modello non verrà selezionato.");
								e.printStackTrace();
								routingContext
    							.response()
    							.setStatusCode(400)
    							.end("ERROR: MODELLO NON AGGIUNTO.");
							}  
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}
	    	    	 	    	
    	    	    }); 
    	    	    
    	    	    
    	    	    /*
    	    	     * Scelta modello da utilizzare: NEW-MODEL
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("newModel", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST newModel");
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		/*
    	    	    		 * Aggiungo un nuovo modello di classificazione
    	    	    		 */
        	    	    	String modelNameZip= routingContext.request().getParam("fileName");
        	    	    	Logger.getLogger().print("File Zip da estrarre: "+modelNameZip+"... ");
        	    	    	Logger.getLogger().print("File Zip da estrarre: "+modelNameZip+"... ");
							ModelController mc= new ModelController();
							try {
							
								Future<Boolean> unzipRes=mc.unZipModel(modelNameZip);
								unzipRes.onSuccess(h->{
									routingContext
									.response()
									.setStatusCode(200)
									.end();
								});
								unzipRes.onFailure(zf->{
									routingContext
	    							.response()
	    							.setStatusCode(400)
	    							.end();
								});
								
							} catch (FileNotFoundException | ZipException e) {
								Logger.getLogger().print("ERROR: FILE ZIP NON VALIDO");
								e.printStackTrace();
    							routingContext
    							.response()
    							.setStatusCode(400)
    							.end("ERROR: FILE ZIP NON VALIDO");
								e.printStackTrace();
							}
							
							/*
							 * Salviamo nel db come modello selezionato
							 */  
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}
	    	    	 	    	
    	    	    }); 
    	    	    
    	    	    
    	    	    /*
    	    	     * START IRRIGATION DIRECT
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("startIrrigation", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST startIrrigation");
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		Logger.getLogger().print("Invio comando di start dell'irrigazione...");
    	    	    		
    	    	    		//Controlliamo che non abbia query param per indicare il campo tradizionale
    	    	    		if(routingContext.request().query().contains("campo") && routingContext.request().getParam("campo").equalsIgnoreCase(Campo.MANUAL.getNome())) {
    	    	    			
    	    	    			Logger.getLogger().print("Richiesta avvio irrigazione campo tradizionale");
    	    	    			
    	    	    			Future<Irrigazione> result=this.irrigationController.startIrrigationDirect(Campo.MANUAL);
    	    	    			result.onSuccess( c->{
    	    	    				//Ritorniamo lo stato 200 per indicare il corretto avvio dell'irrigazione
    	    	    				routingContext
		  	    		    			  .response()
		  	    		    			  .setStatusCode(200)
		  	    		    			  .end();
	  	    		    			Logger.getLogger().print("Irrigazione campo tradizionale avvenuta con successo!");
	  	    		    			
    	    	    			});
    	    	    			result.onFailure(f->{
    	    	    				Logger.getLogger().print("---DEBUG IRRIGAZIONE-LOG---- ERROR: irrigazione non avviata");
    	    	    				routingContext
	        						.response()
	        						.setStatusCode(400)
	        						.end("Stato attuale: errore attivazione.");
                	    	    	Logger.getLogger().print("Stato attuale: errore attivazione.");
    	    	    			});
    	    	    		}
    	    	    		else //if(this.irrigationController.getState() != Utilities.stateOn) {
    	    	    		//if(!routingContext.request().query().contains("campo") &&(this.irrigationController.getState() != Utilities.stateOn)){
    	    	    		if(routingContext.request().getParam("campo").equalsIgnoreCase(Campo.AUTO.getNome()) && (this.irrigationController.getState() != Utilities.stateOn)){
    	    	    			Future<Irrigazione> result=this.irrigationController.startIrrigationDirect(Campo.AUTO);
    	    	    			
    	    	    			result.onSuccess( c->{
    	    	    				//Convertiamo l'irrigazione
    	    	    				try {
										String irrigationJson= new ObjectMapper().writeValueAsString(c);
										routingContext
		  	    		    			  .response()
		  	    		    			  .setStatusCode(200)
		  	    		    			  .end(irrigationJson);
										Logger.getLogger().print("Irrigazione ON : "+irrigationJson);
										
									} catch (JsonProcessingException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										routingContext
		        						.response()
		        						.setStatusCode(400)
		        						.end("Errore parsing Irrigazione");
									}
	  	    		    			Logger.getLogger().print("Stato attuale: irrigazione attivata.");
	  	    		    			
    	    	    			});
    	    	    			result.onFailure(f->{
    	    	    				Logger.getLogger().print("---DEBUG IRRIGAZIONE-LOG---- ERROR: irrigazione non avviata");
    	    	    				routingContext
	        						.response()
	        						.setStatusCode(400)
	        						.end("Stato attuale: errore attivazione.");
                	    	    	Logger.getLogger().print("Stato attuale: errore attivazione.");
    	    	    			});
    	    	    				
	    	    	    	}else
	    	    	    		//if(!routingContext.request().query().contains("campo") &&(this.irrigationController.getState() == Utilities.stateOn))
   
  	    	    			 {
            	    	    	routingContext
    							.response()
    							.setStatusCode(400)
    							.end("Stato attuale: irrigazione gia' in eseguzione");
            	    	    	Logger.getLogger().print("Stato attuale: irrigazione gia' in eseguzione");
    	    	    		};
        	    	    	
        	    	    	 	
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}	    	    	 	    	
    	    	    }); 
    	    	    
    	    	    
    	    	    
    	    	    /*
    	    	     * STOP IRRIGATION DIRECT
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("stopIrrigation", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST stopIrrigation");
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		
    	    	    		//Controlliamo che non abbia query param per indicare il campo tradizionale
    	    	    		if(routingContext.request().getParam("campo").equalsIgnoreCase(Campo.MANUAL.getNome())) {
    	    	    			
    	    	    			Logger.getLogger().print("Richiesta avvio irrigazione campo tradizionale");
    	    	    			
    	    	    			Future<Irrigazione> result=this.irrigationController.stopIrrigationDirect(Campo.MANUAL);
    	    	    			result.onSuccess( c->{
    	    	    				//Ritorniamo lo stato 200 per indicare il corretto avvio dell'irrigazione
    	    	    				routingContext
		  	    		    			  .response()
		  	    		    			  .setStatusCode(200)
		  	    		    			  .end();
	  	    		    			Logger.getLogger().print("STOP-Irrigazione campo tradizionale avvenuta con successo!");
	  	    		    			
    	    	    			});
    	    	    			result.onFailure(f->{
    	    	    				Logger.getLogger().print("---DEBUG IRRIGAZIONE-LOG---- ERROR: irrigazione CAMPO TRADIZIONALE NON FERMATA CORRETTAMENTE");
    	    	    				routingContext
	        						.response()
	        						.setStatusCode(400)
	        						.end("Stato attuale: errore attivazione.");
                	    	    	Logger.getLogger().print("Stato attuale: errore attivazione.");
    	    	    			});
    	    	    		}else
    	    	    		//STOP IRRIGAZIONE CAMPO AUTOMATICO			-------------------------------------------------	
    	    	    		if(routingContext.request().getParam("campo").equalsIgnoreCase(Campo.AUTO.getNome()) && (this.irrigationController.getState() != Utilities.stateOff)) {
    	    	    			
    	    	    			Logger.getLogger().print("Invio comando di stop dell'irrigazione...");
    	    	    			
    	    	    			Future<Irrigazione> result=this.irrigationController.stopIrrigationDirect(Campo.AUTO);
    	    	    			result.onSuccess( c->{
    	    	    				//Convertiamo l'irrigazione
    	    	    				try {
										String irrigationJson= new ObjectMapper().writeValueAsString(c);
										routingContext
		  	    		    			  .response()
		  	    		    			  .setStatusCode(200)
		  	    		    			  .end(irrigationJson);
										Logger.getLogger().print("Irrigazione OFF : "+irrigationJson);
									} catch (JsonProcessingException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										routingContext
		        						.response()
		        						.setStatusCode(400)
		        						.end("Errore parsing Irrigazione");
									}
	  	    		    			Logger.getLogger().print("Stato attuale: irrigazione OFF.");
    	    	    			});
    	    	    			result.onFailure(f->{
    	    	    				Logger.getLogger().print("---DEBUG IRRIGAZIONE-LOG---- ERROR: irrigazione non avviata");
    	    						routingContext
	        						.response()
	        						.setStatusCode(400)
	        						.end("Stato attuale: errore attivazione.");
                	    	    	Logger.getLogger().print("Stato attuale: errore attivazione.");
    	    	    			});
    	    	    				
	    	    	    	}else {
	    	    	    		//Caso nel quale l'irrigazione e' gia' disattivata
            	    	    	routingContext
    							.response()
    							.setStatusCode(400)
    							.end("Stato attuale: irrigazione gia' in eseguzione");
            	    	    	Logger.getLogger().print("Stato attuale: irrigazione gia' spenta");
    	    	    		};
        	    	    	
        	    	    	
        	    	    	
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}	    	    	 	    	
    	    	    }); 
    	    	    
    	    	    
    	    	    /*
    	    	     * STATO IRRIGAZIONE
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("getStatoIrrigazione", routingContext ->{
    	    	    	//Logger.getLogger().print("Chiamata REST getStatoIrrigazione");
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		//Caso nel quale non è stata creata nessuna irrigazione
    	    	    		if(this.irrigationController != null) {
    	    	    			
    	    	    			//System.out.println("DEBUG ======================== "+routingContext.request().getParam("campo"));
    	    	    			
    	    	    			if(routingContext.request().getParam("campo").equalsIgnoreCase(Campo.AUTO.getNome()))
    	    	    				routingContext
	  	      		    	   	      .response()
	  	      			              .setStatusCode(200)
	  	      			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	  	      			              .end(this.irrigationController.getState());
    	    	    			else if(routingContext.request().getParam("campo").equalsIgnoreCase(Campo.MANUAL.getNome()))
    	    	    				routingContext
	  	      		    	   	      .response()
	  	      			              .setStatusCode(200)
	  	      			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	  	      			              .end(this.irrigationController.getStateTrad());
    	    	    			
    	    	    		}else {
    	    	    			routingContext
	      		    	   	      .response()
	      			              .setStatusCode(400)
	      			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	      			              .end("Errore: stato non determinabile");
    	    	    			 	    			
    	    	    		}    	    		
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}	    	    	 	    	
    	    	    }); 
    	    	    
    	    	    /*
    	    	     * GET ALL IRRIGAZIONI
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("irrigationStorico", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST irrigationStorico");
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		 
    	    	    		Logger.getLogger().print("Invio irrigazioni...");
    	    	    		JsonObject irrigazioniQuery= new JsonObject();
    	    	    		this.mongoClient.find("Irrigazioni",irrigazioniQuery , res -> {
    	    	    		    if (res.succeeded()) {
    	    	    			      //for (JsonObject json : res.result()) {
    	    	    			        //Logger.getLogger().print(json.encodePrettily());
    	    	    			        //Logger.getLogger().print("Connessione effettuata con successo al db!");
    	    	    			      //}
    	    	    			      routingContext
    	    		    	   	      .response()
    	    			              .setStatusCode(200)
    	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    	    			              .end(res.result().toString());
    	    	    			    } else {
    	    	    			      res.cause().printStackTrace();
    	    	    			      routingContext
    	    		    	   	      .response()
    	    			              .setStatusCode(200)
    	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    	    			              .end();
    	    	    			    }
    	    	    			  });
    	    	    			    		
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}	    	    	 	    	
    	    	    }); 
    	    	    
    	    	    /*
    	    	     * LAST-IRRIGATION
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("lastIrrigation", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST lastIrrigation");
    	    	    	if(this.loggingController.isUserLogged()) {	    	    		
    	    	    
    	    	    		Logger.getLogger().print("Invio ultima irrigazione...");
    	    	    		
    	    	    		if(this.irrigationController.getState()==Utilities.stateOn) {
    	    	    			if(this.irrigationController!=null) {
    	    	    				try {
										String irrigationJson= new ObjectMapper().writeValueAsString(this.irrigationController.getIrr());
										routingContext
	      	    		    	   	      .response()
	      	    			              .setStatusCode(200)
	      	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	      	    			              .end(irrigationJson);
									} catch (JsonProcessingException e) {
										
										e.printStackTrace();
										routingContext
		  	    		    	   	      .response()
		  	    			              .setStatusCode(400)
		  	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
		  	    			              .end("");
									} 
    	    	    			}else {
    	    	    				
  	    	    			      routingContext
  	    		    	   	      .response()
  	    			              .setStatusCode(400)
  	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
  	    			              .end("");
    	    	    			}
    	    	    		}
    	    	    		else {
    	    	    			JsonObject irrigazioniQuery= new JsonObject();
        	    	    		this.mongoClient.find("Irrigazioni",irrigazioniQuery , res -> {
        	    	    		    if (res.succeeded()) {
        	    	    		    	
        	    	    		    	//cerchiamo l'ultima irrigazione effettuata
        	    	    		    	  long max=0;
        	    	    		    	  JsonObject lastIrrigation=new JsonObject();
        	    	    			      for (JsonObject json : res.result()) {
        	    	    			    	if(json.containsKey("inizioIrrig") && (json.getLong("inizioIrrig")>max)) {
        	    	    			    		lastIrrigation=json;
        	    	    			    	}
        	    	    			        //Logger.getLogger().print("Last irrigation: "+lastIrrigation.encodePrettily());
        	    	    			        //Logger.getLogger().print("Connessione effettuata con successo al db!");
        	    	    			      }
        	    	    			      Logger.getLogger().print("Last irrigation: "+lastIrrigation.encodePrettily());
        	    	    			      routingContext
        	    		    	   	      .response()
        	    			              .setStatusCode(200)
        	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        	    			              .end(lastIrrigation.toString());
        	    	    			    } else {
        	    	    			      res.cause().printStackTrace();
        	    	    			      routingContext
        	    		    	   	      .response()
        	    			              .setStatusCode(400)
        	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        	    			              .end("No last irrigation.");
        	    	    			    }
        	    	    		});
        	    	    			
    	    	    		}
    	    	    		
    	    	    		    		
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}	    	    	 	    	
    	    	    }); 
    	    	    
    	    	    /*
    	    	     * IRRIGATION TIME
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("irrigationTime", routingContext ->{	
    	    	    	Logger.getLogger().print("Chiamata REST irrigationTime");
    	    	    	if(this.loggingController.isUserLogged()) {	    	    		
    	    	    
    	    	    		Logger.getLogger().print("GET-IRRIGATION-TIME...:"+this.irrigationController.getStartingTimeIrrigation().toString());
    	    	    		if(this.irrigationController != null)
	    	    	    		routingContext
	  		    	   	       .response()
	  			               .setStatusCode(200)
	  			               .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	  			               .end(this.irrigationController.getStartingTimeIrrigation().toString());
    	    	    		else	
    	    	    			routingContext
	    		    	   	      .response()
	    			              .setStatusCode(400)
	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	    			              .end("Request error");
    	    	    			    		
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}	    	    	 	    	
    	    	    }); 
    	    	    
    	    	    
    	    	    
    	    	    /*
    	    	     * SET IRRIGATION TIME
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("setIrrigationTime", routingContext ->{	
    	    	    	Logger.getLogger().print("Chiamata REST setIrrigationTime");
    	    	    	if(this.loggingController.isUserLogged()) {	    	    		
    	    	    
    	    	    		Logger.getLogger().print("SET-IRRIGATION-TIME ...");
    	    	    		if(this.irrigationController != null) {
    	    	    			try {
    	    	    				
    	    	    				//Converto il tempo acquisito come parametro in LocalTime dell'irrigationController
    	    	    				Logger.getLogger().print(routingContext.request().getParam("irrigationTime"));
        	    	    			LocalTime newIrrigationTime =LocalTime.parse(routingContext.request().getParam("irrigationTime"));
        	    	    			//Per evitare bug mancanza secondi
        	    	    			newIrrigationTime= LocalTime.of(newIrrigationTime.getHour(), newIrrigationTime.getMinute(), 0);
        	    	    			Logger.getLogger().print("New Irrigation time: "+newIrrigationTime.getHour()+" "+newIrrigationTime.getMinute()+" "+newIrrigationTime.getSecond());
        	    	    			//Dopo aver impostato l'irrigazione resettare il timer e il timer task e crearne uno nuovo
        	    	    			//this.timer.cancel();
        	    	    			
        	    	    			//Un timer task non e' riutilizzabile quindi una volta cancellato va ricostruito
        	    	    			//this.irrigationController= new IrrigationController(MongoClient.createShared(vertx, mongoconfig),this.irrigationCommandClient);
        	    	    			//Impostiamo il nuovo tempo di irrigazione
        	    	    			//this.irrigationController.setStartingTimeIrrigation(newIrrigationTime);
        	    	    			/*
        	    	    			 * Attendiamo fino al corretto completamento dello scheduling del nuovo orario
        	    	    			 */
        	    	    			Logger.getLogger().print("SET-IRRIGATION CALL...");
        	    	    			/*
        	    	    			 * Bisogna ricreare il controller poiche' il timerTask non e' riutilizzabile
        	    	    			 */
        	    	    			this.irrigationController.canceleIrrigationTask();
        	    	    			//Avvio irrigation Controller
        	    	    			irrigationControllerCreation(MongoClient.createShared(vertx, mongoconfig),newIrrigationTime);

        	    	    			routingContext
	     	  		    	   	       .response()
	     	  			               .setStatusCode(200)
	     	  			               .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	     	  			               .end(this.irrigationController.getStartingTimeIrrigation().toString());
        	    	    			
        	    	    			//Ricreiamo il timer
        	    	    			//this.timer=new Timer();
        	    	    			      	    	    			
        	    	    			//Il calcolo dell'inizio della nuova irrigazione viene effettuato dall'irrigationController
        	    	    			/*
        	    	    			this.timer.schedule(this.irrigationController, 
        	    	    					this.irrigationController.delayFromNewIrrigation(newIrrigationTime),
        	    	    					this.irrigationController.delayDay);
        	    	    			
        	    	    			
        	    	    			//Logger.getLogger().print("--debug tempo da attendere:  "+this.irrigationController.delayFromNewIrrigation(newIrrigationTime)/(60*60*1000));
        	    	    			
        	    	    			//Restituiamo risposta
        	    	    			routingContext
     	  		    	   	       .response()
     	  			               .setStatusCode(200)
     	  			               .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
     	  			               .end(this.irrigationController.getStartingTimeIrrigation().toString());
        	    	    			 */
        	    	    			
    	    	    			}
    	    	    			catch(DateTimeException err) {
    	    	    				System.err.println("Errore SET IRRIGATION TIME- formato date sbagliato");
    	    	    				System.err.println(err.getStackTrace());
    	    	    				routingContext
	  	    		    	   	      .response()
	  	    			              .setStatusCode(500)
	  	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	  	    			              .end("Internal server error!");
    	    	    				
    	    	    			}
    	    	    			catch(Exception e) {
    	    	    				//In caso di generazione di eccezione
    	    	    				System.err.println("Errore SET IRRIGATION TIME");
    	    	    				System.err.println(e.getStackTrace());
    	    	    				e.printStackTrace();
    	    	    				routingContext
	  	    		    	   	      .response()
	  	    			              .setStatusCode(500)
	  	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	  	    			              .end("Internal server error!");
    	    	    			}
    	    	    			
    	    	    			
    	    	    		}
	    	    	    		
    	    	    		else	
    	    	    			routingContext
	    		    	   	      .response()
	    			              .setStatusCode(400)
	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	    			              .end("No error irrigation controller...");
    	    	    			    		
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}	    	    	 	    	
    	    	    });
    	    	    
    	    	    
    	    	    
    	    	    /*
    	    	     * CHANNELS-NUMBER
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("channelsNum", routingContext ->{	
    	    	    	Logger.getLogger().print("Chiamata REST channelsNum");
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		//Caso nel quale non è stata creata nessuna irrigazione	    
    	    	    		Logger.getLogger().print("Invio numero canali... ");
    	    	    		//Calcolo numero canali presenti
    	    	    		ArrayList<Integer> listIdChannel= new ArrayList<Integer>();
    	    	    		for(int i=0;i<this.numberOfChannel;i++) {
    	    	    			listIdChannel.add(i);
    	    	    		}
    	    	    		//DEBUG
    	    	    		Logger.getLogger().print("--DEBUG-------Numero canali gateway: "+listIdChannel.size()+" Canali gateway: "+listIdChannel.toString());
    	    	    		
    	    	    		listIdChannel.addAll(this.csvController.getChannelNumberCSV());
    	    	    		Logger.getLogger().print("--DEBUG-------Numero canali totale: "+listIdChannel.size()+" Canali : "+listIdChannel.toString());
    	    	    		
							try {
								String jsonArray = new ObjectMapper().writeValueAsString(listIdChannel);
								Logger.getLogger().print("--DEBUG-------Json: "+listIdChannel.size());
								routingContext
	    		    	   	      .response()
	    			              .setStatusCode(200)
	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	    			              .end(jsonArray);
							
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								routingContext
	    		    	   	      .response()
	    			              .setStatusCode(500)
	    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	    			              .end();
								e.printStackTrace();
							}
    	    	    		
    	    	    		

    	    	    			    		
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}	    	    	 	    	
    	    	    }); 
    	    	    
    	    	    /*
    	    	     * GET CONFIGURATIONS
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("getConfigurations", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST getConfigurations");
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		//Caso nel quale non è stata creata nessuna irrigazione	    
    	    	    		Logger.getLogger().print("Invio configuazioni: "+this.configuration.toString());
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(200)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end(this.configuration.toString());	    		
    	    	    	}
    	    	    	else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}	    	    	 	    	
    	    	    }); 
    	    	    
    	    	    /*
    	    	     * ----- GET CHANNEL MEASURE --------
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("channelMeasure", routingContext ->{
    	    	    	Logger.getLogger().print("Chiamata REST channelMeasure");
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		//Caso nel quale non è stata creata nessuna irrigazione	    
    	    	    		Logger.getLogger().print("Invio channelMeasure: "+this.configurationController.channelMeasureArray.toString());
    	    	    		try {
    	    	    		  String jsonresp= new ObjectMapper().writeValueAsString(this.configurationController.channelMeasureArray);
    	    	    		  Logger.getLogger().print("TEST: "+this.configurationController.channelMeasureArray.toString());
    	    	    		  routingContext
      		    	   	      .response()
      			              .setStatusCode(200)
      			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
      			              .end(jsonresp);	    		
    	    	    		}catch(Exception e) {
    	    	    			routingContext
      		    	   	      .response()
      			              .setStatusCode(400)
      			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
      			              .end("Errore!");
    	    	    		}
    	    	    		
    	    	    	}else {
    	    	    		routingContext
    		    	   	      .response()
    			              .setStatusCode(401)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Non autorizzato: non sei loggato.");
    	    	    	}	    	    	 	    	
    	    	    	
    	    	    }); 
    	    	    
    	    	    
    	    Router router = routerFactory.getRouter(); // <1>
            router.errorHandler(404, routingContext -> { // <2>
              JsonObject errorObject = new JsonObject() // <3>
                .put("code", 404)
                .put("message",
                  (routingContext.failure() != null) ?
                    routingContext.failure().getMessage() :
                    "Not Found"
                );
              routingContext
                .response()
                .setStatusCode(404)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(errorObject.encode()); // <4>
            });
            router.errorHandler(400, routingContext -> {
              JsonObject errorObject = new JsonObject()
                .put("code", 400)
                .put("message",
                  (routingContext.failure() != null) ?
                    routingContext.failure().getMessage() :
                    "Validation Exception"
                );
              routingContext
                .response()
                .setStatusCode(400)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(errorObject.encode());
            });
    	    
            server = vertx.createHttpServer(new HttpServerOptions().setPort(8888).setHost("localhost")); // <5>
            server.requestHandler(router).listen(); // <6>
            
            // end::routerGen[]
            startPromise.complete(); // Complete the verticle start
    	    
    	  } else {
			// Something went wrong during router factory initialization
    	    //future.fail(ar.cause()); // (2)
    		 Logger.getLogger().print("Errore creazione verticle");
    		 startPromise.fail(ar.cause());
    	  }
    	});
    
  
  }
  
  

  
  public static void main(String[] args) {
	  /*
	   * Per evitare problemi riguardanti la latenza del servizio di classificazione
	   * aumentiamo il tempo di attesa per un event loop thread handler	
	   */
	  //configurationController= new ConfigurationController();
	  VertxOptions options = new VertxOptions();
	  options.setMaxEventLoopExecuteTime(60);
	  options.setMaxEventLoopExecuteTimeUnit(TimeUnit.SECONDS);
	  
	  //Impostiamo il numero di worker massimi di questa verticle
	  options.setWorkerPoolSize(2);
	  
	  
	  
	  Vertx vertx = Vertx.vertx(options);
	  vertx.deployVerticle(new MainVerticle());
	  
  }
  

  /*
  private void mqttClientCreation() {
	  /*
	   * Creo l'hashmap contenente i client e quello delle priorityqueue
	   *
	  this.mqttClients= new HashMap<Integer,MqttClient>();
	  this.sampleChannelQueue= new HashMap<String,ArrayList<JsonObject>>();
	  
	  for(int i=0; i<this.numberOfChannel; i++) {
		  
		this.mqttClients.put(i, MqttClient.create(vertx));
		this.sampleChannelQueue.put(""+i, new ArrayList<JsonObject>());
		//this.defineClient(i);
		defineClientWeatherStation(i);
	  }  
  }
 
  
  private void defineClient(int i) {
	  
	  MqttClient client= this.mqttClients.get(i);
	  /*
	   * Colleghiamo ed iscriviamo il client al canale del suo corrispondente channel
	   *
	  client.connect(1883, "localhost", s -> {		 
		this.mqttClients.get(i).publishHandler(c -> {
	    		//Ogni qual volta viene pubblicata una misura la stampiamo e la salviamo.
				  Logger.getLogger().print("There are new message in topic: " + c.topicName());
	    		  Logger.getLogger().print("Content(as string) of the message: " + c.payload().toString());
	    		  Logger.getLogger().print("QoS: " + c.qosLevel());	
	    		  Logger.getLogger().print("LOG-GATEWAY: "+c.payload().toString());
	    		  
	    		 /* 
	    		  * Il contenuto deve essere salvato nel database e nella PriorityQueue
	    		  *
	    		  JsonArray newMisures= c.payload().toJsonArray();	//Le nuove misure sono fornite tramite un array di json	    		  
	    		  /*
	    		   * La misura che è arrivata è un array contenente le nuove misurazioni.
	    		   *
	    		  JsonObject singleMisure;
	    		  //Salviamo le nuove misure.
	    		  for(int j=0; j<newMisures.size(); j++ ) {
	    			  singleMisure= newMisures.getJsonObject(j);
	    			  //Salviamo la misura nella priorityQueue
	    			  this.sampleChannelQueue.get(""+i).add(singleMisure);
	    			  
	    			  //Salviamo la misura nel DB
	    			  mongoClient.insert("channel-"+i, singleMisure , res ->{
		    			  if(res.succeeded())
		    				  Logger.getLogger().print("Misura salvata correttamente nel DB.");
		    			  else
		    				  System.err.println("ERRORE salvataggio misura");  
		    		  });
	    		 
	    		  }
				})
	    		  .subscribe(""+i, 2);	    
	    });
  }
  
  private void defineClientWeatherStation(int i) {
	  
	  MqttClient client= this.mqttClients.get(i);
	  /*
	   * Colleghiamo ed iscriviamo il client al canale del suo corrispondente channel
	   *
	  client.connect(1883, "localhost", s -> {		 
		this.mqttClients.get(i).publishHandler(c -> {
	    		//Ogni qual volta viene pubblicata una misura la stampiamo e la salviamo.
				  Logger.getLogger().print("There are new message in topic: " + c.topicName());
	    		  Logger.getLogger().print("Content(as string) of the message: " + c.payload().toString());
	    		  Logger.getLogger().print("QoS: " + c.qosLevel());	
	    		  Logger.getLogger().print("LOG-GATEWAY: "+c.payload().toString());
	    		  	  
	    		  /*
	    		   * La nuova misura viene convertita in jsonObject
	    		   *
	    		  JsonObject newMisure=c.payload().toJsonObject();
	    		  //Salviamo la misura nella priorityQueue
	    		  this.sampleChannelQueue.get(""+i).add(newMisure);
	    		  
	    		  //Salviamo la misura nel DB
    			  mongoClient.insert("channel-"+i, newMisure , res ->{
	    			  if(res.succeeded())
	    				  Logger.getLogger().print("Misura salvata correttamente nel DB.");
	    			  else
	    				  System.err.println("ERRORE salvataggio misura");  
	    		  });
	    		  
				})
	    		  .subscribe(""+i, 2);	    
	    });
  }
  */
  
  
  
  
  /*
   * Creazione dell'irrigationController per il controllo dell'irrigazione
   */
  private void irrigationControllerCreation(MongoClient mongoClient,LocalTime irrigationStartTime) {
	  
	 	Timer timer = new Timer();
	    Calendar dateC = Calendar.getInstance();
	    dateC.set(
	      Calendar.DAY_OF_WEEK,
	      Calendar.SUNDAY
	    );
	    dateC.set(Calendar.HOUR, 0);
	    dateC.set(Calendar.MINUTE, 0);
	    dateC.set(Calendar.SECOND, 0);
	    dateC.set(Calendar.MILLISECOND, 0);
	    
	    
	    //this.irrigationController= new IrrigationController(MongoClient.createShared(vertx, mongoconfig),this.irrigationCommandClient);
	    this.irrigationController= new IrrigationController(mongoClient,MqttClient.create(vertx),MqttClient.create(vertx),timer,irrigationStartTime);
	    //Iniziamo lo scheduling
	    //this.irrigationController.start();
	    
	    
  }
  
  
  
  
  
  
  
  private void setClientWeatherStation() {
	  
	  //DEBUG
	  Logger.getLogger().print("MQTT WeatherStation Client creation...");
	  this.clientWS= MqttClient.create(vertx);
	  
	  
	  clientWS.connect(1883, "localhost", s -> {		 
		clientWS.publishHandler(c -> {
	    		//Ogni qual volta viene pubblicata una misura la stampiamo e la salviamo.
				  Logger.getLogger().print("There are new message in topic: " + c.topicName());
	    		  Logger.getLogger().print("Content(as string) of the message: " + c.payload().toString());
	    		  Logger.getLogger().print("QoS: " + c.qosLevel());	
	    		  Logger.getLogger().print("LOG-GATEWAY: "+c.payload().toString());
	    		  	  
	    		  /* 
		    		  * Il contenuto deve essere salvato nel database e nella PriorityQueue
		    		  */
		    		  JsonArray newMisures= c.payload().toJsonArray();	//Le nuove misure sono fornite tramite un array di json	    		  
		    		  /*
		    		   * La misura che è arrivata è un array contenente le nuove misurazioni.
		    		   */
		    		  if(newMisures.size()<(Utilities.channelsNames.length+configurationController.idSerialChannel.size()))
		    			  Logger.getLogger().print("--DEBUG-----Sono arrivate meno misure di quelle previste------");
		    		  else if(newMisures.size()==Utilities.channelsNames.length+configurationController.idSerialChannel.size())
		    			  Logger.getLogger().print("--DEBUG-----Misure uguali in numero------");
		    		  
		    		  
		    		  JsonObject singleMisure;
		    		  //Salviamo le nuove misure.
		    		  for(int i=0; i<newMisures.size(); i++ ) {
		    			  singleMisure= newMisures.getJsonObject(i);
		    			  String channelName;
		    			  int channelId=-1;
		    			  if(singleMisure.containsKey("channel")){
		    				  //Logger.getLogger().print("--DEBUG-----Canale trovato-----");
		    				  channelName=singleMisure.getString("channel");
		    				  if(channelName.contains("Serial")) {
		    					  String[] strSpl = channelName.split("-");
		    					  if(channelName.contains("Temp"))
		    						  channelId=Utilities.channelsNames.length+(Integer.parseInt(strSpl[2])*2);  //primo canale dopo le misure della weatherstation
		    					  else
		    						  channelId=Utilities.channelsNames.length+(Integer.parseInt(strSpl[2])*2)+1;
		    				  }else
			    				  for(int k=0;k<Utilities.channelsNames.length;k++) {
			    					  if(channelName.equalsIgnoreCase(Utilities.channelsNames[k]))
			    						  channelId=k;				  
			    				  }
		    				 
		    				  if(channelId!=-1) {   				  
			    				//Salviamo la misura nella priorityQueue
				    			  this.sampleChannelQueue.put(""+channelId, singleMisure);
				    			  
				    			//Salviamo la misura nel DB
				    			  mongoClient.insert("channel-"+channelId, singleMisure , res ->{
					    			  //if(res.succeeded())
					    				  //Logger.getLogger().print("Misura salvata correttamente nel DB.");
					    			  //else
					    				  //System.err.println("ERRORE salvataggio misura");  
					    		  });
		    				  }	  
		    				
			    			  
			    			  channelId=-1; 
		    			  }
		    			  
		    			  
		    			  
		    		 
		    		  }	
	    		  
				})
	    		  .subscribe("weatherStation", 2);	    
	    });
  }
  
  

}
