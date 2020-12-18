package com.innovare.middleware;

import java.io.FileNotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.control.Classificator;
import com.innovare.control.LoggingController;
import com.innovare.control.ModelController;
import com.innovare.model.ClassificationSint;
import com.innovare.model.IrrigationState;
import com.innovare.model.Model;
import com.innovare.model.PlantClassification;
import com.innovare.model.User;
import com.innovare.utils.NoUserLogException;
import com.innovare.utils.Utilities;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
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
	
	private int numberOfChannel;
	private HashMap<Integer,MqttClient> mqttClients;
	private HashMap<String,ArrayList<JsonObject>> sampleChannelQueue;
	private MongoClient mongoClient;
	//private Model selectedModel=null;
	private ModelController modelController;
	private LoggingController loggingController;
	private MqttClient irrigationCommandClient;
	private MqttClient irrigationLog;
	
	private String irrigationState=null;
	
	/*
	 * AGGIUNGERE PRIORITY QUEUE DELLE CLASSIFICAZIONI, DEVE CONTENERE LE ULTIME 4 CLASSIFICAZIONI EFFETTUATE
	 */
	
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
	  //INSTANZIAZIONI INIZIALI
	  configuration = new JsonArray();
	  
	  
	  //Connessione a MongoDB
      System.out.println("Starting MongoConnection..");

	  JsonObject config = Vertx.currentContext().config();

	    String uri = config.getString("mongo_uri");
	    if (uri == null) {
	      uri = "mongodb://localhost:27017";
	    }
	    System.out.println(uri);

	    String db = config.getString("mongo_db");
	    if (db == null) {
	      db = "innovare";
	    }
	    System.out.println(db);
	    JsonObject mongoconfig = new JsonObject()
	        .put("connection_string", uri)
	        .put("db_name", db);
	    	
	    mongoClient = MongoClient.createShared(vertx, mongoconfig);

	  
	  //TEST MONGODB
	  JsonObject query = new JsonObject();
	  mongoClient.find("Utenti", query, res -> {
	    if (res.succeeded()) {
	      for (JsonObject json : res.result()) {
	        System.out.println(json.encodePrettily());
	        System.out.println("Connessione effettuata con successo al db!");
	      }
	    } else {
	      res.cause().printStackTrace();
	    
	    }
	  });
	  
	  /*
	   * Creazione del modelController
	   */
	  this.modelController= new ModelController();
	  System.out.println(this.LOG+": modelController creato: "+this.modelController.toString());
	  
	  /*
	   *Recuperiamo il modello selezionato in precedenza 
	   */
	  JsonObject querySelectedModel = new JsonObject();
	  System.out.println(this.LOG+" Avvio ricerca modello selezionato.");
	  mongoClient.find("selectedModel", querySelectedModel, res -> {
	    if (res.succeeded()) {
		    	
		    if(!res.result().isEmpty()){
		      /*
		       * Esiste un unico documento in questa collezione
		       * Ricerchiamo il modello selezionato in precedenza nel db e lo impostiamo come modello selezionato
		       */
		    	JsonObject jsonSelectedModel=res.result().get(0);
		    	System.out.println(this.LOG+"Modello selezionato predentemente: "+jsonSelectedModel.encodePrettily());
		    	Model selectedModel;
				try {
					selectedModel = new ObjectMapper().readValue(jsonSelectedModel.toString(), Model.class);
					this.modelController.setModelSelected(selectedModel.getName());
				} catch (JsonProcessingException e) {
					System.err.println("Errore parsing json del modello selezionato");
					System.err.println("Verra impostato a null il modello selezionato.");
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					System.err.println("Il modello che è stato selezionato in precedenza non è più disponibile nel fileSystem!");
					System.err.println("Verra impostato a null il modello selezionato.");
					e.printStackTrace();
				}
	    	}
		    //System.out.println(this.LOG+"Nessun modello selezionato.");
		    
	    } else {
	      res.cause().printStackTrace();
	    
	    }
	  });
	  
	  /*
	   * LOGGIN CONTROLLER
	   */
	  this.loggingController= new LoggingController();
	  System.out.println(this.LOG+": loggingController creato: "+this.loggingController.toString());
	  
	  /*
	   * MQTT CLIENT 
	   * 
	   */	  
	  //Creazione client MQTT per cattura log dei gateway
	  MqttClient clientLog= MqttClient.create(vertx);
	  clientLog.connect(1883, "localhost", s -> {
		  
		  clientLog.publishHandler(c -> {
	    		//Ogni qual volta viene pubblicata una misura la stampiamo e la salviamo.
				  //System.out.println("There are new message in topic: " + c.topicName());
	    		  //System.out.println("Content(as string) of the message: " + c.payload().toString());
	    		  //System.out.println("QoS: " + c.qosLevel());	
	    		  System.out.println("LOG-GATEWAY: "+c.payload().toString());
	    		  
	    		  if(c.payload().toString().startsWith("channelNumber:")) {
	    			  String num=c.payload().toString().substring(14);
	    			  System.out.println("Numero di channel: "+num);
	    			  this.numberOfChannel= Integer.parseInt(num);
	    			  //Avvio creazione client per la ricezione dei valori dei canali
	    			  this.mqttClientCreation();
	    		  }
	    		  
	    		  //Salviamo i configurationItem
	    		  if(c.payload().toString().contains("id")) {
	    			 this.configuration.add(new JsonObject(c.payload().toString()));
	    			 System.out.println("LOG-GATEWAY: Aggiunto "+c.payload().toString()+" alle configurazioni.");
	    		  }
	    		  //JsonObject confJson= new JsonObject( c.payload().toString());
	    		})
	    		  .subscribe("gatewayLog", 2);	    
		  
	    });
	  
	  /*
	   * MQTT CLIENT IRRIGAZIONE
	   * 
	   * Client per l'invio di comandi 
	   */
	  System.out.println("Creazione client mqtt per i comandi di irrigazione.");
	  this.irrigationCommandClient= MqttClient.create(vertx);
	  
	  /*
	   * Client per la ricezione del log dell'irrigazione.
	   */
	  System.out.println("Creazione client mqtt per il log dell'irrigazione.");
	  this.irrigationLog= MqttClient.create(vertx);
	  this.irrigationLog.connect(1883, Utilities.ipMqtt, s ->{
		  irrigationLog.publishHandler(c -> {
	    		  
	    		  System.out.println(Utilities.irrigationLogMqttChannel+":"+c.payload().toString());	
	    		  //Modifichiamo lo stato dell'irrigazione corrente
	    		  if(c.payload().toString().contains(Utilities.stateOn))
	    			  this.irrigationState=Utilities.stateOn;
	    		  else
	    			  if(c.payload().toString().contains(Utilities.stateOff))
	    				  this.irrigationState=Utilities.stateOff;
	    		  
	    		  //JsonObject confJson= new JsonObject( c.payload().toString());
	    		})
	    		  .subscribe(Utilities.irrigationLogMqttChannel, 2);	  
	  });
	  
	  
	  
	    //////////////////////////////////////////////
	    
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
    	    				  System.out.println(json.encodePrettily());
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
        	    	System.out.println("Utente che cerca di loggarsi nel sistema: "+username+" "+password);	//Stampa di TEST
        	    	/*RequestParameters params = routingContext.get("parsedParameters"); // (1)
        	    	String username = params.pathParameter("username").toString();
        	    	String password = params.pathParameter("password").toString();*/
        	    	
        	    	//Verifichiamo che l'utente loggato sia corretto andando ad effettuare una chiamata al database MongoDb.
        	    	 JsonObject q = new JsonObject()
        	    			 .put("username", username)
        	    			 .put("password", password);
        	    	 
        	    	 mongoClient.findOne("Utenti", q, null , res -> {
        	    		 if (res.succeeded()) {
        	    			 if(this.loggingController.logIn(res.result().toString())) {
        	    				 try {
    								System.out.println("Conferma login effettuato per utente: user: "+this.loggingController.getUserLogged().toString());
    								routingContext
								 	.response()
								 	.setStatusCode(200)
								 	.end( this.loggingController.getUserLogged().getRole().toString()  );
    							 }catch(NoUserLogException e) {
    								// TODO Auto-generated catch block
    								System.out.println("Errore toString user loggato");
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
    		    	   	      res.cause().printStackTrace();
    		    	   	      /*
    		    	   	       * Caso nel quale non esiste lo User
    		    	   	       */
    		    	   	      routingContext
    		    	   	      .response()
    			              .setStatusCode(404)
    			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    			              .end("Errore username o password");
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
        	    	    	 * Fallimento se non il parametro non è stato inserito o non esiste il canale selezionato
        	    	    	 */
        	    	    	if(channel==null || !this.sampleChannelQueue.containsKey(channel))
        	    	    		routingContext
    			    	   	      .response()
    				              .setStatusCode(400)
    				              .end();
        	    	    	else {
        	    	    		/*
        	    	    		 * Restituiamo l'ultimo Sample registrato ma non lo eliminiamo
        	    	    		 */
        	    	    		ArrayList<JsonObject> samples= this.sampleChannelQueue.get(channel);
        	    	    		if(!samples.isEmpty()) {
                	    	    	routingContext
            	    	    	 	.response()
            	    	    	 	.setStatusCode(200)
            	    	    	 	.end(samples.get(samples.size()-1).toString());
        	    	    		}else
        	    	    			/*
        	    	    			 * Caso nel quale non ci sono nuovi Sample
        	    	    			 */
    	        	    	    	routingContext
    	    	    	    	 	.response()
    	    	    	    	 	.setStatusCode(200)
    	    	    	    	 	.end("NO-NEW-SAMPLE");
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
    	    	 * Restituiamo le ultime misure non ancora mostrate dalla DashBoard e le eliminiamo dall'array   LASTSAMPLES
    	    	 */   	    	    
    	    	    routerFactory.addHandlerByOperationId("lastsamples", routingContext ->{
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		String channel= routingContext.request().getParam("idcanale");
        	    	    	/*
        	    	    	 * Fallimento se non il parametro non è stato inserito o non esiste il canale selezionato
        	    	    	 */
        	    	    	if(channel==null || !this.sampleChannelQueue.containsKey(channel))
        	    	    		routingContext
    			    	   	      .response()
    				              .setStatusCode(400)
    				              .end();
        	    	    	else {
        	    	    		/*
        	    	    		 * Restituiamo gli ultimi Sample e li eliminiamo
        	    	    		 */
            	    	    	ArrayList<JsonObject> samples= this.sampleChannelQueue.get(channel);
            	    	    	
            	    	    	
            	    	    	if(!samples.isEmpty()) {
            	    	    		System.out.println("Samples inviati: "+samples.toString());
            	    	    		routingContext
        								.response()
        								.setStatusCode(200)
        								.end(samples.toString());
        								this.sampleChannelQueue.get(channel).clear();
        	            	    	 
        							
            	    	    	}else {
            	    	    		System.out.println("No new Value");
            	    	    		routingContext
    	    	    	    	 	.response()
    	    	    	    	 	.setStatusCode(200)
    	    	    	    	 	.end();
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
        	    	    		 */
            	    	    	ArrayList<JsonObject> samples= this.sampleChannelQueue.get(channel);
            	    	    	
            	    	    	
            	    	    	if(!samples.isEmpty()) {
            	    	    		System.out.println("Samples inviati: "+samples.toString());
            	    	    		routingContext
        								.response()
        								.setStatusCode(200)
        								.end(samples.toString());
        							
            	    	    	}else {
            	    	    		System.out.println("No new Value");
            	    	    		routingContext
    	    	    	    	 	.response()
    	    	    	    	 	.setStatusCode(200)
    	    	    	    	 	.end();
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
    	    	     * Restituiamo tutti i sample di un determinato canale  ALL SAMPLE
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("allsamples", routingContext ->{
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		String channel= routingContext.request().getParam("idcanale");
        	    	    	/*
        	    	    	 * Fallimento se il parametro non è stato inserito o non esiste il canale selezionato
        	    	    	 */
        	    	    	if(channel==null || !this.sampleChannelQueue.containsKey(channel))
        	    	    		routingContext
    			    	   	      .response()
    				              .setStatusCode(400)
    				              .end();
        	    	    	else {
        	    	    		/*
        	    	    		 * Restituiamo tutti i Sample di un determinato canale
        	    	    		 */
        	    	    		JsonObject q= new JsonObject().put("channel", channel);
            	    	    	this.mongoClient.find("channel-"+channel,q, res-> {
            	    	    		/*
            	    	    		 * Successo nel trovare i sample nel db
            	    	    		 */
            	    	    		if(res.succeeded()) {
            	    	    			routingContext
        								.response()
        								.setStatusCode(200)
        								.end(res.result().toString());
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
        	    	    		Classificator c= new Classificator(dataSet);
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
        						    			  if(res.succeeded())
        						    				  System.out.println("Classificazione salvata correttamente nel DB.");
        						    			  else
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
    	    	    	if(this.loggingController.isUserLogged()) {
        	    	    	String dataSet= routingContext.request().getParam("datasetName");
        	    	    	System.out.println("Dataset: "+dataSet);
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
        	    	    		 * Avviamo una nuova classificazione
        	    	    		 */
        	    	    		Classificator c= new Classificator(dataSet);
        	    	    		try {
        	    	    			//Genero una nuova classificatione sintetica
            	    	    		ClassificationSint cs=c.unZipAndClassification(this.modelController.getSelectedModel().getName());
            	    	    		
            	    	    		//Inviamo il json al front-end
            	    	    		try { 	
            	    	    			//Genero il json della classificazione
            	    	    			String jsonClassifications=new ObjectMapper().writeValueAsString(cs);
        								//Invio il json al front-end
            	    	    			routingContext
        								.response()
        								.setStatusCode(200)
        								.end(jsonClassifications);
            	    	    			
        								//Memorizzio nel database tutte le classificazioni
            	    	    			//JsonArray newClassificationsJson= new JsonArray(jsonClassifications);
            	    	    			JsonObject singleClassification= new JsonObject(jsonClassifications);
            	    	    			mongoClient.insert("ClassificazioniSintetiche", singleClassification , res ->{
  						    			  if(res.succeeded())
  						    				  System.out.println("Classificazione sintetica salvata correttamente nel DB.");
  						    			  else
  						    				  System.err.println("ERRORE salvataggio misura");  
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
        						    				  System.out.println("Singola classificazione salvata correttamente nel DB.");
        						    			  else
        						    				  System.err.println("ERRORE salvataggio misura");  
        						    		});
        								}	
            	    	    					
        							} catch (JsonProcessingException e) {
        								System.err.println("Errore conversione json");
        								e.printStackTrace();

        								routingContext
        				    	   	      .response()
        					              .setStatusCode(400)
        					              .end("Errore json convertito");
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
    	    	     * Richiesta le classificazioni di una certa data   GET-CLASSIFICATION-BY-DATE
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("getClassificationsByDate", routingContext ->{
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
        	    	    		//System.out.println(date);
        	    	    		JsonObject q= new JsonObject().put("date",date );
            	    	    	this.mongoClient.find("ClassificazioniSintetiche",q, res-> {
            	    	    		/*
            	    	    		 * Successo nel trovare i sample nel db
            	    	    		 */
            	    	    		if(res.succeeded()) {
            	    	    			routingContext
        								.response()
        								.setStatusCode(200)
        								.end(res.result().toString());
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
            	    	    			routingContext
        								.response()
        								.setStatusCode(200)
        								.end(res.result().toString());
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
    	    	     * Richiede le ultime 4 classificazioni   GET-LAST-CLASSIFICATIONS
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("lastclassificazion", routingContext ->{
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
            	    	    			System.out.println(csa.toString());
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
    								routingContext
    								.response()
    								.setStatusCode(200)
    								.end(new ObjectMapper().writeValueAsString(selectedModel));
    							} catch (JsonProcessingException e) {
    								// TODO Auto-generated catch block
    								e.printStackTrace();
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
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		/*
    	    	    		 * Seleziono il modello da utilizzare come classificatore
    	    	    		 */
        	    	    	String modelName= routingContext.request().getParam("modelName");
        	    	    	try {
        	    	    		System.out.println("Modello selezionato: "+modelName+"... ");			
    							/*
    							 * Salviamo nel db come modello selezionato
    							 */
        	    	    		if(this.modelController.getSelectedModel() != null) {
        	    	    			/*
        	    	    			 * Caso nel quale esiste un modello già selezionato
        	    	    			 */
        	    	    			//Verifico che il modello sia nel filesystem e aggiorno il controller
        							this.modelController.setModelSelected(modelName);

        	    	    			JsonObject oldmodel= new JsonObject().put("name", this.modelController.getSelectedModel().getName());
        	    	    			JsonObject update = new JsonObject().put("$set", new JsonObject()
        	    	    					  .put("name", modelName));
        	    	    			mongoClient.updateCollection("selectedModel", query, update, res -> {
        	    	    				  if (res.succeeded()) {
        	    	    				    System.out.println("Modello selezionato salvato nel db");
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
        	    	    			
        	    	    			
        	    	    		}else {
        	    	    			System.out.println("Nessun modello è presente nel db, aggiunta nuovo modello...");
        	    	    			//Verifico che il modello sia nel filesystem e aggiorno il controller
        							this.modelController.setModelSelected(modelName);

        	    	    			Model newModel= new Model(modelName);
        	    	    			String jsonStringModel= new ObjectMapper().writeValueAsString(newModel);
        	    	    			JsonObject newModelJson = new JsonObject();		  
        	    	    			mongoClient.save("selectedModel", newModelJson, res -> {
        	    	    					  if (res.succeeded()) {
        	    	    					    System.out.println("Modello selezionato salvato nel db");
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
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		/*
    	    	    		 * Aggiungo un nuovo modello di classificazione
    	    	    		 */
        	    	    	String modelNameZip= routingContext.request().getParam("fileName");
        	    	    	System.out.println("File Zip da estrarre: "+modelNameZip+"... ");
							ModelController mc= new ModelController();
							try {
							
								mc.unZipModel(modelNameZip);
								routingContext
								.response()
								.setStatusCode(200)
								.end();
							} catch (FileNotFoundException | ZipException e) {
								// TODO Auto-generated catch block
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
    	    	     * START IRRIGATION
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("startIrrigation", routingContext ->{	
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		/*
    	    	    		 * Invio il comando tramite il client mqtt per il comando
    	    	    		 */
    	    	    		System.out.println("Invio comando di start dell'irrigazione...");
    	    	    		/*
    	    	    		 * Utiliziamo un handler per capire quando la pubblicazione è stata completata(?)
    	    	    		 * Quando è andata a buon fine restituiamo un 200
    	    	    		 */
    	    	    		/*this.irrigationCommandClient.publishCompletionHandler(c ->{
    	    	    			System.out.println("Invio effettuato con successo");
    	    	    			routingContext
    							.response()
    							.setStatusCode(200)
    							.end();
    	    	    			//Disconnesione per evitare problemi
            	    	    	this.irrigationCommandClient.disconnect();
    	    	    		});*/
    	    	    		this.irrigationCommandClient.connect(1883, Utilities.ipMqtt, t ->{
    	    	    			this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
    	        	    	    		  Buffer.buffer(Utilities.stateOn),
    	  								  MqttQoS.AT_LEAST_ONCE,
    	  								  false,
    	  								  false);
    	    	    			this.irrigationCommandClient.disconnect();
            	    	    	
            	    	    	routingContext
    							.response()
    							.setStatusCode(200)
    							.end("Invio comando effettuato");
            	    	    	System.out.println("Invio comando start effettuato.");
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
    	    	     * STOP IRRIGATION
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("stopIrrigation", routingContext ->{	
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		/*
    	    	    		 * Invio il comando tramite il client mqtt per il comando
    	    	    		 */
    	    	    		System.out.println("Invio comando di stop dell'irrigazione...");
    	    	    		/*
    	    	    		 * Utiliziamo un handler per capire quando la pubblicazione è stata completata(?)
    	    	    		 * Quando è andata a buon fine restituiamo un 200
    	    	    		 */
    	    	    		this.irrigationCommandClient.connect(1883, Utilities.ipMqtt, v ->{
    	    	    			
    	    	    			this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
    	        	    	    		  Buffer.buffer(Utilities.stateOff),
    	  								  MqttQoS.AT_LEAST_ONCE,
    	  								  false,
    	  								  false);
    	    	    			this.irrigationCommandClient.disconnect();
            	    	    	
            	    	    	routingContext
    							.response()
    							.setStatusCode(200)
    							.end("Invio comando effettuato");
            	    	    	
            	    	    	System.out.println("Invio comando stop effettuato.");
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
    	    	     * STATO IRRIGAZIONE
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("getStatoIrrigazione", routingContext ->{	
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		//Caso nel quale non è stata creata nessuna irrigazione
    	    	    		if(this.irrigationState == null) {
    	    	    			routingContext
	      		    	   	      .response()
	      			              .setStatusCode(200)
	      			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	      			              .end(Utilities.stateOff);
    	    	    		}else {
    	    	    			routingContext
	      		    	   	      .response()
	      			              .setStatusCode(200)
	      			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	      			              .end(this.irrigationState);
    	    	    			 	    			
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
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		//Caso nel quale non è stata creata nessuna irrigazione	    
    	    	    		System.out.println("Invio irrigazioni...");
    	    	    		JsonObject irrigazioniQuery= new JsonObject();
    	    	    		this.mongoClient.find("Irrigazioni",irrigazioniQuery , res -> {
    	    	    		    if (res.succeeded()) {
    	    	    			      for (JsonObject json : res.result()) {
    	    	    			        System.out.println(json.encodePrettily());
    	    	    			        System.out.println("Connessione effettuata con successo al db!");
    	    	    			      }
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
    	    	     * GET CONFIGURATIONS
    	    	     */
    	    	    routerFactory.addHandlerByOperationId("getConfigurations", routingContext ->{	
    	    	    	if(this.loggingController.isUserLogged()) {
    	    	    		//Caso nel quale non è stata creata nessuna irrigazione	    
    	    	    		System.out.println("Invio configuazioni: "+this.configuration.toString());
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
    		 System.out.println("Errore creazione verticle");
    		 startPromise.fail(ar.cause());
    	  }
    	});
    
  
  }
  
  
  public static void main(String[] args) {
	  /*
	   * Per evitare problemi riguardanti la latenza del servizio di classificazione
	   * aumentiamo il tempo di attesa per un event loop thread handler	
	   */
	  VertxOptions options = new VertxOptions();
	  options.setMaxEventLoopExecuteTime(60);
	  options.setMaxEventLoopExecuteTimeUnit(TimeUnit.SECONDS);
	  
	  Vertx vertx = Vertx.vertx(options);
	  vertx.deployVerticle(new MainVerticle());
	    
  }
  

  
  private void mqttClientCreation() {
	  /*
	   * Creo l'hashmap contenente i client e quello delle priorityqueue
	   */
	  this.mqttClients= new HashMap<Integer,MqttClient>();
	  this.sampleChannelQueue= new HashMap<String,ArrayList<JsonObject>>();
	  
	  for(int i=0; i<this.numberOfChannel; i++) {
		  
		this.mqttClients.put(i, MqttClient.create(vertx));
		this.sampleChannelQueue.put(""+i, new ArrayList<JsonObject>());
		this.defineClient(i);
	
	  }  
  }
 
  
  private void defineClient(int i) {
	  
	  MqttClient client= this.mqttClients.get(i);
	  /*
	   * Colleghiamo ed iscriviamo il client al canale del suo corrispondente channel
	   */
	  client.connect(1883, "localhost", s -> {		 
		this.mqttClients.get(i).publishHandler(c -> {
	    		//Ogni qual volta viene pubblicata una misura la stampiamo e la salviamo.
				  System.out.println("There are new message in topic: " + c.topicName());
	    		  System.out.println("Content(as string) of the message: " + c.payload().toString());
	    		  System.out.println("QoS: " + c.qosLevel());	
	    		  System.out.println("LOG-GATEWAY: "+c.payload().toString());
	    		  
	    		 /* 
	    		  * Il contenuto deve essere salvato nel database e nella PriorityQueue
	    		  */
	    		  JsonArray newMisures= c.payload().toJsonArray();	//Le nuove misure sono fornite tramite un array di json	    		  
	    		  /*
	    		   * La misura che è arrivata è un array contenente le nuove misurazioni.
	    		   */
	    		  JsonObject singleMisure;
	    		  //Salviamo le nuove misure.
	    		  for(int j=0; j<newMisures.size(); j++ ) {
	    			  singleMisure= newMisures.getJsonObject(j);
	    			  //Salviamo la misura nella priorityQueue
	    			  this.sampleChannelQueue.get(""+i).add(singleMisure);
	    			  
	    			  //Salviamo la misura nel DB
	    			  mongoClient.insert("channel-"+i, singleMisure , res ->{
		    			  if(res.succeeded())
		    				  System.out.println("Misura salvata correttamente nel DB.");
		    			  else
		    				  System.err.println("ERRORE salvataggio misura");  
		    		  });
	    		 
	    		  }
				})
	    		  .subscribe(""+i, 2);	    
	    });
  }
  
  

}
