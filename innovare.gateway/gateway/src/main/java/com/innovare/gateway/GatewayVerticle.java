package com.innovare.gateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.Channel;
import com.innovare.model.ConfigurationItem;
import com.innovare.model.IrrigationController;
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
	 * IRRIGAZIONE:
	 * 	Per quanto riguarda l'irrigatione è presente un client per l'invio di dati di Log e per la ricezione dei comandi per il pilotaggio.
	 * 	Inoltre è presente lo stato dell'irrigazione e la quantità di acqua in litri, all'interno dell'oggetto IrrigationController.
	 * 
	 */
	
  public static final String serverIP="192.168.0.185";	
	
  private int numberOfChannel= 2; //per ora test
  private HashMap<Channel,MqttClient> mapClient;
  private IrrigationController irrigation;
  private ConfigurationItem cfi;

  
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
	  
	 //Metodi di verifica del corretto caricamento del gateway. Da aggiungere..
	 this.cfi=this.generateConfigurationItem();
	
	  
	//Creazione client MQTT
	    MqttClient client = MqttClient.create(vertx);
	    client.connect(1883, "localhost", t -> {
	    	
	    	//Appena il gateway si collega per avvisare del suo corretto collegamento.
	    	 client.publish("gatewayLog",
	    			 //Configurazione di test salvata come oggetto json
		    		  Buffer.buffer("Gateway collegato.Pubblicazione numero canali.."),
		    		  MqttQoS.AT_LEAST_ONCE,
		    		  false,
		    		  false);
	    	 //Comunicazione numero di canali
	    	 client.publish("gatewayLog",
	    			 //Configurazione di test salvata come oggetto json
		    		  Buffer.buffer("channelNumber:"+this.numberOfChannel),
		    		  MqttQoS.AT_LEAST_ONCE,
		    		  false,
		    		  false);
	    	 //Comunicazione numero di canali
	    	 try {
				client.publish("gatewayLog",
						 //Configurazione di test salvata come oggetto json
						  Buffer.buffer(new ObjectMapper().writeValueAsString(this.cfi)),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);
				//TEST PER AGGIUNGERE UN CONFIGURATION ITEM IN PIÙ
				client.publish("gatewayLog",
						 //Configurazione di test salvata come oggetto json
						  Buffer.buffer(new ObjectMapper().writeValueAsString(this.cfi)),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);
			} catch (JsonProcessingException e) {
				System.err.println("ERRORE: conversione json del configuration item");
				e.printStackTrace();
			}
	    	 client.disconnect();
	    	/*
	    	 * Richiamo operazione per l'instanziazione dei canali dopo aver comunicato il loro numero
	    	 */
	    	 //RIATTIVARE LA CREAZIONE DEI THREAD , COMMENTATO PER TEST
	    //	 this.channelCreation();   ///////////////////////////////////////////////////
	    	 
	    	 
	    	 //Si può anche disconnettere questo client dopo l'instanziazione dei client dei singoli sensori...
	    });
	    
	    
	    /*
	     * IRRIGAZIONE
	     */
    	System.out.println("Creazione Irrigazione Controller..");
	    this.irrigation= new IrrigationController();
	    /*
	     * Il controller per il comando deve ricevere, quindi si iscriverà al relativo topic
	     */
	    this.irrigation.setCommandClient( MqttClient.create(vertx));
	    this.irrigation.getCommandClient().connect(1883, "localhost", p -> {
	    	System.out.println("Client mqtt per i comandi dell'irrigazione connesso..");
	    	/*
		     *Il client mqtt per la ricezione di comandi si iscriverà al relativo topic 
		     */
		    this.irrigation.getCommandClient().publishHandler(c ->{
		    	System.out.println("Comando ricevuto: "+c.payload().toString());
		    	String comando=c.payload().toString();
		    	//long time=Long.parseLong(comando.substring(18));
		    	
		    	//System.out.println("Comando ricevuto: "+c.payload().toString());
		    	//System.out.println("Tempo di irrigazione: "+time);
		    	
		    	//Impostiamo in base al comando
		    	if(comando.equalsIgnoreCase(IrrigationController.stateOn))
		    		irrigation.startIrrigation();
		    	else
		    		if(comando.equalsIgnoreCase(IrrigationController.stateOff))
		    			irrigation.stopIrrigation();
		    })
		    .subscribe("Irrigation-COMMAND", 2);	    
		});
	    
	    this.irrigation.setLogClient(MqttClient.create(vertx));
	    System.out.println("Client mqtt per il log dell'irrigazione creato..");
	    
  
  }
  

  
  
  /*
   * Metodo per la creazione dei canali
   */
  private void channelCreation() {
	  /*
	   * Creo un client per ogni canale e avvio i thread di raccolta dati
	   */
	  this.mapClient= new HashMap<Channel,MqttClient>();
	  	  
	  /*
	   * Per ogni canale creo un client mqtt per la pubblicazione dei Samples
	   */
	  Channel chan;
	  for(int i=0; i<this.numberOfChannel; i++) {
		  System.out.println("Creazione canale numero: "+(i+1));
		  chan=new Channel(""+i,9000L);
		  this.mapClient.put(chan,MqttClient.create(vertx));
		  startingClient(chan);
	  }
		  
  }
  
  
  
  
  private void startingClient(Channel chan) {
	  /*
	   * Avvio del Thread per il campionamento
	   */
	  chan.start();

	  /*
	   * Creazione dell'evento di prelevamento di uno o più sample in un dato Channel
	   */
	  long timerId = vertx.setPeriodic(10000, id ->{	
    	//metodo per la cattura dei dati-- DA DEFINIRE
    	try {
    		ArrayList<Sample> misure= chan.getNewSample();
    		for(Sample s: misure) {
    			System.out.println(s.toString());
    		}	
    		System.out.println(chan.toString());
	    	this.mapClient.get(chan).connect(1883, "localhost", s -> {	
		    	//
					try {
						this.mapClient.get(chan).publish(chan.getID(),
								 //Invio dell'array contenente le misure
								  Buffer.buffer(new ObjectMapper().writeValueAsString(misure)),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);
					} catch (JsonProcessingException e) {
						System.err.println("La misurazione non è stata convertita correttamente in json!");
						e.printStackTrace();
					}
					//Il client si disconnette dopo aver inviato il messaggio.-NECESSARIO PER EVITARE BUG SULLA RICONNESSIONE-
					this.mapClient.get(chan).disconnect();
		    });
    		
    	}
    	catch(NoSuchElementException e){
    		System.err.println(e.getMessage());
    	}
    	//invio dei dati tramite mqtt   		
    });
	  
  }
  
  
  private ConfigurationItem generateConfigurationItem() {
	  Property p1= new Property("indirizzo","localhost");
	  Property p2= new Property("porta","8888");
	  Property[] pa = {p1,p2};
	  ConfigurationItem cfiN= new ConfigurationItem("sensor-gateway",pa);
	  return cfiN;
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
	    
  }
  
  
  
  

  
  
  
}
