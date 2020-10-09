package com.innovare.gateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
	  
	 //Metodi di verifica del corretto caricamento del gateway.
	  
	  
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
	    	
	    	/* client.publish("test",
	    			 //Configurazione di test salvata come oggetto json
		    		  Buffer.buffer("test"),
		    		  MqttQoS.AT_LEAST_ONCE,
		    		  false,
		    		  false);
	    	*/
	    
	    	
	    });
	    
	   
    
  }
  
  
  public static void main(String[] args) {
	    Vertx vertx = Vertx.vertx();
	    vertx.deployVerticle(new GatewayVerticle());
  }
  
  
  
 
  public JsonObject getConfigurazione(){
	  return null;
  }
  

  
  
  
}
