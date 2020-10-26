package com.innovare.gateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.Channel;
import com.innovare.model.ConfigurationItem;
import com.innovare.model.MisuraTest;
import com.innovare.model.Property;
import com.innovare.model.Role;
import com.innovare.model.Sample;
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
	 * 
	 * 
	 * INFORMAZIONI SU COMUNICAZIONE MQTT DEI SAMPLES CATTURATI DAL GATEWAY:
	 * 	Ogni channel possiede una coda che viene riempita da un Thread di campionamento.
	 * 	La coda può avere 0,1, >1 elementi al suo interno.
	 * 	Se numero di elementi = 0, non viene inviato niente.
	 * 	Se il numero di elementi >=1, viene inviato un array di Sample.
	 * 	E' da tenere presente che i Samples una volta inviati vengono eliminati dalla coda e memorizzati dal MiddleWare nel suo DataBase. 
	 * 	Quindi tutti i Samples presenti nelle code, sono Samples ancora non memorizzati
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
	    
	    /*
	     * Creazione dei Channels disposti al campionamento.(In questo caso di test uno solo)
	     */
	    Channel chan= new Channel("1",10000);	//campionamento ogni 10 secondi
	    chan.start();	//Avvio del Thread di campionamento
	    
	    
	    /*
	     * Creazione dell'evento di prelevamento di uno o più sample in un dato Channel
	     */
	    long timerId =	vertx.setPeriodic(15000, id ->{
	    	
	    	
	    	
	    	//metodo per la cattura dei dati-- DA DEFINIRE
	    	try {
	    		ArrayList<Sample> misure= chan.getNewSample();
	    		for(Sample s: misure) {
	    			System.out.println(s.toString());
	    		}
	    		
	    		
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
	    		
	    		
	    	}
	    	catch(NoSuchElementException e){
	    		System.err.println(e.getMessage());
	    	}
	    	
	    	
		    
	    	
	    	//invio dei dati tramite mqtt
	    	
	    	
	    	
	    	
	    });
	   
	    
  }
  
  
  
  

  
  
  
}
