package com.innovare.gateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.ConfigurationItem;
import com.innovare.model.Property;
import com.innovare.model.Role;
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
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.mqtt.MqttClient;

public class GatewayVerticle extends AbstractVerticle {

	
	/*
	 * IMPORTANTE:Il tipo password utilizzato per i json per rendere sicure le password non è supportato e se utilizzato 
	 * 			  manda in crash il programma.
	 * 			
	 * 			Le get del login in fase di test hanno generato una risposta strana da parte del server che rispondeva
	 * 			correttamente con codice di ritorno 200 ma generava anche un successivo codice 500 internal server error.
	 * 			
	 */
	
	
	final List<JsonObject> configuration = new ArrayList<>(Arrays.asList(
		    new JsonObject().put("Gateway", "prova").put("Model", "prova")	    
		  ));
	
	
	
	
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
	  
	 //Metodi di verifica del corretto caricamento del gateway. Da aggiungere..
	  
	  
	//Creazione client MQTT
	    MqttClient client = MqttClient.create(vertx);
	    client.connect(1883, "localhost", s -> {
	    	
	    	//Appena il gateway si collega invia le proprie configurazioni.
	    	 client.publish("Configuration",
	    			 //Configurazione di test salvata come oggetto json
		    		  Buffer.buffer(configuration.get(0).encode()),
		    		  MqttQoS.AT_LEAST_ONCE,
		    		  false,
		    		  false);
	    	
	    });
	    
	   
    
  }
  
  
  public static void main(String[] args) {
	  
	  	//Si deve instanziare l'oggetto ConfigurationItem di questo Gateway
	  	Property p1= new Property("indirizzo","192.168.55.5");//indirizzo di esempio, sarà settato statico
	  	Property p2= new Property("porta","8888");			 //dati di esempio
	  	Property[] ps= new Property[10];					 //Grandezza array di esempio
	  	
	  	ConfigurationItem configuration= new ConfigurationItem("SensorGateway",ps);
	  	
	  	//test
	  	ObjectMapper mp = new  ObjectMapper();
	  	try {
	  		String provajson= mp.writeValueAsString(configuration);
	  		System.out.println("TestJson= "+provajson);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  	
	  	
	  	
	    Vertx vertx = Vertx.vertx();
	    vertx.deployVerticle(new GatewayVerticle());
	    
	    
	    //Creazione evento schedulato di campionamente dei dati dal sensore
	    long timerId =	vertx.setPeriodic(5000, id ->{
	    	
	    	//metodo per la cattura dei dati
	    	//invio dei dati tramite mqtt
	    	MqttClient client = MqttClient.create(vertx);
	    	//Usiamo un metodo di test per ora
	    	client.connect(1883, "localhost", s -> {	
		    	//Appena il gateway si collega invia le proprie configurazioni.
		    	 client.publish("Dati",
		    			 //Configurazione di test salvata come oggetto json
			    		  Buffer.buffer("test dati"),
			    		  MqttQoS.AT_LEAST_ONCE,
			    		  false,
			    		  false);
		    });
	    	
	    	
	    });
	   
	    
  }
  
  
  
 
  public JsonObject getConfigurazione(){
	  return null;
  }
  

  
  
  
}
