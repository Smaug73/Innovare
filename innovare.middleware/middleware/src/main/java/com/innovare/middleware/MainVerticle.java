package com.innovare.middleware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.User;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
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
	
	
	final List<JsonObject> configuration = new ArrayList<>(
			//Arrays.asList(
		 //   new JsonObject().put("Gateway", "prova").put("Model", "prova")	    
		  //)
			);
	
	//private MongoClient mongo=null;
	
	private User userLog;
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
	
	
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
	  
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
		 //Con questo test cercheremmo tutti gli utenti, matchara tutti perchè abbiamo specificato dettagli in particolare
		 //this.findUser("admin");
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
	   * MQTT CLIENT 
	   * 
	   */
	  
	  //Creazione client MQTT per cattura log dei gateway
	  MqttClient clientLog= MqttClient.create(vertx);
	  clientLog.connect(1883, "localhost", s -> {
			 
		  clientLog.publishHandler(c -> {
	    		//Ogni qual volta viene pubblicata una misura la stampiamo e la salviamo.
				  System.out.println("There are new message in topic: " + c.topicName());
	    		  System.out.println("Content(as string) of the message: " + c.payload().toString());
	    		  System.out.println("QoS: " + c.qosLevel());	
	    		  System.out.println("LOG-GATEWAY: "+c.payload().toString());
	    		  
	    		  if(c.payload().toString().startsWith("channelNumber:")) {
	    			  String num=c.payload().toString().substring(14);
	    			  System.out.println("Numero di channel: "+num);
	    			  this.numberOfChannel= Integer.parseInt(num);
	    			  //Avvio creazione client per la ricezione dei valori dei canali
	    			  this.mqttClientCreation();
	    		  }
	    		  
	    		  //JsonObject confJson= new JsonObject( c.payload().toString());
	    		})
	    		  .subscribe("gatewayLog", 2);	    
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
    	    routerFactory.addHandlerByOperationId("actualConfiguration", routingContext ->{
    	    	
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
    	    /*
    	    	routingContext
    	    	 .response()
    	    	 .setStatusCode(200)
    	    	 .putHeader(HttpHeaders.CONTENT_TYPE, "applivation/json")
    	    	 .end(new JsonArray(getAllConfiguration()).encode())
    	    		);
    	    */
    	    //Login     
    	    routerFactory.addHandlerByOperationId("login", routingContext ->{
    	    	String username= routingContext.request().getParam("username").toString();
    	    	String password= routingContext.request().getParam("password").toString();
    	    	System.out.print(username+" "+password);	//Stampa di TEST
    	    	/*RequestParameters params = routingContext.get("parsedParameters"); // (1)
    	    	String username = params.pathParameter("username").toString();
    	    	String password = params.pathParameter("password").toString();*/
    	    	
    	    	//Verifichiamo che l'utente loggato sia corretto andando ad effettuare una chiamata al database MongoDb.
    	    	 JsonObject q = new JsonObject()
    	    			 .put("username", username)
    	    			 .put("password", password);
    	    	 
    	    	 mongoClient.findOne("Utenti", q, null , res -> {
    	    		 if (res.succeeded()) {
    	    			 System.out.println("Conferma login effettuato per utente: user: "+username);
    	    			 routingContext
    	    	    	 	.response()
    	    	    	 	.setStatusCode(200)
    	    	    	 	.end("Conferma login effettuato per utente!");
    	    			 
    	    		 } else {
		    	   	      res.cause().printStackTrace();
		    	   	      
		    	   	      routingContext
		    	   	      .response()
			              .setStatusCode(404)
			              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
			              .end();
		    	   	    }
		    	   	  });
    	    });
    	    	
    	    	//misures    
    	    /*
    	     * La misura ora è cambiata deve contenere i sample del canale ecc.
    	     */
    	    	    routerFactory.addHandlerByOperationId("misures", routingContext ->{
    	    	    	
    	    	    	JsonObject q = new JsonObject();
    	    	    	 mongoClient.find("Utenti", q, res -> {
    	    	    		 if (res.succeeded()) {
    	    	    			 
    	    	    			 routingContext
    	    	    	    	 	.response()
    	    	    	    	 	.setStatusCode(200)
    	    	    	    	 	.end("Conferma login effettuato per utente!");
    	    	    			 
    	    	    		 } else {
    			    	   	      res.cause().printStackTrace();
    			    	   	      
    			    	   	      routingContext
    			    	   	      .response()
    				              .setStatusCode(404)
    				              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    				              .end();
    			    	   	    }
    			    	   	  });
    	    	    });  	
    	    		
    	    	/*
    	    	 * Dalla dashboard dobbiamo capire se si sta cercando di entrare come admin o user. 
    	    	 * Da definire i dati comunicati dalla dashboard, questo è solo un test.
    	    	 * 
    	    	 *
    	    	 * 
    	    	 * 
    	    	try {
					userLog= User.convertJsonToUser(userJson.toString());	//variabile dove si salva l'utente loggato correttamente
					routingContext
	    	    	 .response()
	    	    	 .setStatusCode(200)
	    	    	 .end("Conferma login effettu");
				} catch (JsonMappingException e1) {
					// TODO Auto-generated catch block
					System.out.println("Errore");
					e1.printStackTrace();
				} catch (JsonProcessingException e1) {
					// TODO Auto-generated catch block
					System.out.println("Errore");
					e1.printStackTrace();
				}
    	    	/*
    	    	 * Aggiungere la verifica sul database.
    	    	 */  	    	
    	   
    	  
    	 
    	    
    	    
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
	    Vertx vertx = Vertx.vertx();
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
  
  
  public JsonObject getConfigurazione(){
	  return null;
  }
  

  private List<JsonObject> getAllConfiguration() {
	  if(this.configuration.isEmpty())
		  System.out.println("La lista dei gateway è vuota");
    return this.configuration;
  }
  
  
}
