package com.innovare.gateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.ConfigurationItem;
import com.innovare.model.MisuraTest;
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
	

	
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
	  
	 //Metodi di verifica del corretto caricamento del gateway. Da aggiungere..
	  
	  
	//Creazione client MQTT
	    MqttClient client = MqttClient.create(vertx);
	    client.connect(1883, "localhost", s -> {
	    	
	    	//Appena il gateway si collega per avvisare del suo corretto collegamento.
	    	 client.publish("gatewayLog",
	    			 //Configurazione di test salvata come oggetto json
		    		  Buffer.buffer("Gateway collegato."),
		    		  MqttQoS.AT_LEAST_ONCE,
		    		  false,
		    		  false);
	    	
	    });
	    
	   
    
  }
  
  
  public static void main(String[] args) {
	  
	  	//Si deve instanziare l'oggetto ConfigurationItem di questo Gateway
	  	/*
	  	 * Il configuration Item è già presente nel db
	  	 * 
	  	 * Property p1= new Property("indirizzo","192.168.55.5");//indirizzo di esempio, sarà settato statico
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
	  	*/
	  	
	  /*
	   * DEFINIRE LA PARTE DI RICONOSCIMENTO SENSORI
	   * 
	   * 
	   * 
	   */
	  	
	    Vertx vertx = Vertx.vertx();
	    vertx.deployVerticle(new GatewayVerticle());
	    MqttClient client = MqttClient.create(vertx);
	    
	    //Creazione evento schedulato di campionamente dei dati dal sensore, nel nostro caso ogni 10 secondi
	    long timerId =	vertx.setPeriodic(10000, id ->{
	    	
	    	//metodo per la cattura dei dati-- DA DEFINIRE
		    MisuraTest misure= new MisuraTest();
		    System.out.println(misure.toString());
	    	
	    	//invio dei dati tramite mqtt
	    	
	    	//Usiamo un metodo di test per ora
	    	client.connect(1883, "localhost", s -> {	
		    	//Appena il gateway si collega invia le proprie configurazioni.
					try {
						client.publish("misure",
								 //Configurazione di test salvata come oggetto json
								  Buffer.buffer(new ObjectMapper().writeValueAsString(misure)),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);
					} catch (JsonProcessingException e) {
						System.err.println("La misurazione non è stata convertita correttamente in json!");
						e.printStackTrace();
					}
					//Il client si disconnette dopo aver inviato il messaggio.-NECESSARIO PER EVITARE BUG SULLA RICONNESSIONE-
					client.disconnect();
		    });
	    	
	    	
	    });
	   
	    
  }
  
  
  
 
  public JsonObject getConfigurazione(){
	  return null;
  }
  

  
  
  
}
