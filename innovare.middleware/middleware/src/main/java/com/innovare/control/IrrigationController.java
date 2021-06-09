package com.innovare.control;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.middleware.MainVerticle;
import com.innovare.model.Campo;
import com.innovare.model.ClassificationSint;
import com.innovare.model.Evapotranspiration;
import com.innovare.model.Irrigazione;
import com.innovare.model.Sample;
import com.innovare.utils.Utilities;
import com.innovare.model.Status;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.mqtt.MqttClient;


import java.time.temporal.ChronoUnit;

/*
 * Si occupa del controllo dell'innaffiamento
 */
public class IrrigationController extends TimerTask {

	public static final int maxHour=23;
	public static final int maxMinute=60;
	
	public static final int defaultHour=14;
	public static final int defaultMinute=0;
	public static final int defaultSecond=0;
	
	public static final float defaultPortata=0;
	
	public static final long delayDay= 24*60*60*1000l;
	public static final long delayOneMinutetTest= 60*1000l;//Un minuto di delay
	public static final long delayStartDEFAULT=1000*20l; // 20 Secondi
	private static final String LOGIRR="LOG-IRRIGATION-CONTROLLER: ";
	
	private String state=Utilities.stateOff;
	//stato irrigazione campo manuale
	private String statoRelMan=Utilities.stateOff;
	private Irrigazione irr=null;
	
	//Comandi irrigazione secondo campo manuale
	public static final String stateC2On="c2ON";
	public static final String stateC2Off="c2OFF";
	
	//private LocalTime StartingTimeIrrigation= LocalTime.of(14, 0);
	private LocalTime StartingTimeIrrigation;
	private MongoClient mongoClient;
	private MqttClient irrigationCommandClient;
	private MqttClient irrigationTradCommandCliet;
	private long delayFromIrrigation;
	
	//Timer per l'irrigazione 
	private Timer timer=null;
	
	
	//Orario di start dell'irrigazione giornaliera
	//private LocalTime startIrrigationTime= LocalTime.of(Utilities.hourStartIrrigation, Utilities.minuteStartIrrigation);
	private JobDetail job;
	private Trigger trigger;
	private Scheduler sch;
	
	
	
	//private long timeWaitIrrigation=0;
	
	
	
	public IrrigationController(MongoClient mongC,MqttClient irrigationComClient,MqttClient irrigationTraClient,Timer t,LocalTime StartingTimeIrrigation) {
		this.mongoClient=mongC;
		this.irrigationCommandClient= irrigationComClient;
		this.irrigationTradCommandCliet=irrigationTraClient;
		this.timer=t;
		this.StartingTimeIrrigation= StartingTimeIrrigation;
		this.delayFromIrrigation= this.delayFromNewIrrigation(StartingTimeIrrigation);
		
		
		Logger.getLogger().print("DelayFromLocation: "+this.delayFromIrrigation);
		Logger.getLogger().print("StartingTimeIrrigation: "+this.StartingTimeIrrigation);
		
		
		//Connettiamo il client mqtt per i comandi verso l'irrigazione
		this.irrigationCommandClient.connect(1883, Utilities.ipMqtt, x ->{
			Logger.getLogger().print(this.LOGIRR+" Client per i comandi IRRIGAZIONE AUTOMATICA connesso correttamente.");
		});
		
		//Connettiamo il client mqtt per irrigazione tradizionale
		this.irrigationTradCommandCliet.connect(1883, Utilities.ipMqtt, x ->{
			Logger.getLogger().print(this.LOGIRR+" Client per i comandi IRRIGAZIONE TRADIZIONALE connesso correttamente.");
		});
		
	}
	
	/*
	 * Scheduliamo attraverso il timer l'irrigazione
	 */
	public void start() {
		this.timer.schedule(
	    		this,
	    		this.delayFromNewIrrigation(this.getStartingTimeIrrigation()),
	    		//this.irrigationController.delayDay
	    		//this.irrigationController.delayOneMinutetTest  //TEST
	    		//this.delayFromIrrigation
	    		this.delayDay
	    );
		Logger.getLogger().print("DEBUG IRRIGATION-CONTROLLER: irrigazione schedulata");
	}
	
	
	/*
	 * Metodo per reimpostare nuovo orario di irrigazione
	 *
	public Future<Boolean> setNewDelayAndTimer(LocalTime lc) {
		
		Promise<Boolean> newSetDone= Promise.promise();
		
		//Imposto il nuovo orario
		this.StartingTimeIrrigation= lc;
		
		//Dopo aver impostato l'irrigazione resettare il timer e il timer task per crearne uno nuovo
		this.timer.cancel();
		
		//Ricreiamo il timer
		this.timer=new Timer();
		
		//Schedulo il nuovo timer
		this.start();
		
		newSetDone.complete(true);
		
		return newSetDone.future();
	}
	*/
	
	public void canceleIrrigationTask() {
		//Cancelliamo il task di irrigazione
		this.timer.cancel();
	}
	
	/*
	public void startSchedulingIrrigation() {
		
		try {
			Logger.getLogger().print(LOGIRR+System.currentTimeMillis()+"  Avvio SCHEDULING job di Irrigazione...");
			this.job= JobBuilder.newJob(IrrigationController.class)
					.withIdentity("IrrigationJob","group1")
					.build();
			this.trigger= TriggerBuilder.newTrigger()
					.withIdentity("IrrigationJob","group1")
					.forJob(this.job)
					.withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
					.build();
			
			this.sch= new StdSchedulerFactory().getScheduler();
			this.sch.scheduleJob(this.job,this.trigger);
			this.sch.start();
			
			Logger.getLogger().print(LOGIRR+System.currentTimeMillis()+"  Avvio SCHEDULING start...");
			
		} catch (SchedulerException e) {
			Logger.getLogger().print(LOGIRR+System.currentTimeMillis()+"ERRORE SCHEDULING..");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	*/
	
	
	
	/*
	 * Thread per la gestione dell'irrigazione giornaliera
	 */
	public void run(){
		/*
		 * Il thread deve verificare l'ultima classificazione generata e deve basarsi su quella
		 * per generare la nuova irrigazione
		 */
		
			//Controlliamo se non e' in eseguzione un'altra irrigazione o se e' gia' stata inviata una richiesta di irrigazione
			if(this.state==Utilities.stateOn || this.state==Utilities.stateLock) {
				//se e' in eseguzione usciamo
				Logger.getLogger().print(LOGIRR+System.currentTimeMillis()+" Irrigazione gia' in eseguzione!");
				return ;
			}
			//Controlliamo se il client MQTT e' connesso correttamente al gateway
			//Se non connesso ci fermiamo, altrimenti possiamo proseguire con l'esecuzione
			else if(!this.irrigationCommandClient.isConnected()) {
				Logger.getLogger().print(LOGIRR+" ERRORE: irrigazioneClient non connesso al gateway, controllare connessione tra le componenti...");
				return;
			}
			else
				Logger.getLogger().print(LOGIRR+System.currentTimeMillis()+" Irrigazione non in eseguzione...");
			
			Logger.getLogger().print(LOGIRR+System.currentTimeMillis()+" Avvio job di Irrigazione...");	
			JsonObject q= new JsonObject();
			//Controlliamo l'ultima classificazione effettuata
			this.mongoClient.find("ClassificazioniSintetiche",q, res-> {
	    		/*
	    		 * Successo nel trovare i sample nel db
	    		 */
				Logger.getLogger().print(LOGIRR+System.currentTimeMillis()+" Ricerca ultima classificazione effettuata...");
				Irrigazione newIrr;
	    		if(res.succeeded()) {
	    			/*
	    			 * Prendiamo solo l'ultima
	    			 */
	    			ClassificationSint lastClassification;
	    			ArrayList<ClassificationSint> csa= new ArrayList<ClassificationSint>();
	    			ClassificationSint csObj;
	    			for(JsonObject jo: res.result()) {
	    				try {
							csObj= new ObjectMapper().readValue(jo.toString(), ClassificationSint.class);
							csa.add(csObj);
							
						} catch (JsonProcessingException e) {
							Logger.getLogger().print(LOGIRR+"Errore conversione json.");
							e.printStackTrace();
						}		
	    			}
	    			
	    			Collections.sort((List<ClassificationSint>) csa);
	    			//ArrayList<ClassificationSint> arrayResp= new ArrayList<ClassificationSint>();
	    			
	    			Logger.getLogger().print(LOGIRR+" Classificazioni trovate: "+csa.toString());
	    			if(csa.size()>=1) {
	    				lastClassification=csa.get(0);
	    				Logger.getLogger().print(LOGIRR+"Ultima classificazione effettuata: "+lastClassification);
	    				//Generiamo una nuova Irrigazione
	    				
	    				/*
	    				 * AGGIUNTA STRATEGIA DI IRRIGAZIONE PER DECIDERE QUANTO IRRIGARE======================================================================
	    				 */
	    				IrrigationStrategy strategy= new IrrigationStrategy();
	    				try {
							this.irr = strategy.createIrrigation(lastClassification);
						} catch (JsonMappingException e) {
							Logger.getLogger().print(" Irrigazione automatica ERRORE: errore creazione irrigazione automatica!");
							e.printStackTrace();
							this.irr=null;
							return;
						} catch (JsonProcessingException e) {
							Logger.getLogger().print(" Irrigazione automatica ERRORE: errore creazione irrigazione automatica!");
							e.printStackTrace();
							this.irr=null;
							return;
						}
	    				
	    				//newIrr= new Irrigazione(lastClassification.getMaxPercForIrrigation());
	    				
	    				Logger.getLogger().print(LOGIRR+"Nuova Irrigazione: "+this.irr);
	    				
	    				//L'irrigazione verra' memorizzata solo quando terminera' effettivamente
	    			}
	    			else {
	    				/*
	    				 * Nessuna classificazione acquisita precedentemente, irrigazione normale
	    				 */
	    				Logger.getLogger().print(LOGIRR+" Nessuna classificazione precedente.");
	    				this.irr= new Irrigazione(Status.NORMALE);
	    				
	    				Logger.getLogger().print(LOGIRR+"Nuova Irrigazione: "+this.irr);
	    				
	    			}  			
	    		}
	    		/*
	    		 * Caso di fallimento
	    		 */
	    		else {
	    			Logger.getLogger().print(LOGIRR+" Fallimento ricerca classificazioni in mongoDB ...");
	    			Logger.getLogger().print(LOGIRR+" Creazione irrigazione NORMALE.");
	    			this.irr= new Irrigazione(Status.NORMALE);
	    		}
	    		
	    		/*
	    		 * Avvio irrigazione
	    		 */
	    		//timeWaitIrrigation=newIrr.getFineIrrig()-newIrr.getInizioIrrig();
	    		if(this.irr.getQuantita()>0)
	    			startIrrigation(this.irr);
	    		else {
	    			Logger.getLogger().print("IRRIGATION-CONTROLLER: ATTENZIONE non ci sono condizioni per irrigare il campo!");
	    		}
	    		
	    		
	    		
	    	});		
				
	}
	
	
	
	
	/*
	 * Genera un nuovo piano di irrigazione basandosi sulla nuova classificazione e la precedente
	 * Per farlo decide l'orario di fine dell'irrigazione basandosi sulla classificazione acquisita
	 
	public Irrigazione createIrrigation(ClassificationSint lastClassification) {
		
		return new Irrigazione(lastClassification.getMaxPercForIrrigation());
		
	}
	*/
	
	public void startIrrigation(Irrigazione irr) {
		
		//Tempo di irrigazione
		long time=irr.getFineIrrig()-irr.getInizioIrrig();
		
		
		/*
		 * Ci colleghiamo con il server MQTT
		 */
		if(!irrigationCommandClient.isConnected())
			this.irrigationCommandClient.connect(1883, MainVerticle.configurationController.gatewayIP , t ->{
				Logger.getLogger().print("DEBUG IRRIGAZIONE AUTOMATICA --- INVIO STATE-ON AL GATEWAY, in attesa risposta...");
				
				//Cambio lo stato da subito poiche' potrebbe arrivare una richiesta di irrigazione diretta
				//Lo facciamo dopo la corretta connessione 
				this.state= Utilities.stateLock;
				
				//Handler di attesa della risposta positiva da parte del gateway
				this.irrigationCommandClient.publishHandler(r-> {
					
					//Attendiamo risposta positiva dal gateway
					if(r.payload().toString().contains(Utilities.stateOn)) {
						
						//this.irrigationCommandClient.disconnect();
						
						//IMPOSTO LO STATO DELL'IRRIGAZIONE A ON
						this.state=Utilities.stateOn;
						Logger.getLogger().print("DEBUG IRRIGAZIONE AUTOMATICA--- start effettuato con successo.");
						
						//Attendiamo fino alla fine dell'irrigazione
				    	//try {
						//	Thread.sleep(time);
						//} catch (InterruptedException e) {
						//	Logger.getLogger().print("DEBUG IRRIGAZIONE AUTOMATICA--- Errore nella sleep del thread IrrigationController");
						//	e.printStackTrace();
						//}
			    		
			    	
			    		 //Avviamo Stop-Irrigazione
			    		//stopIrrigation();
		    			  
						
		    		}else
		    			//Risposta irrigazione		===============================
		    			if(r.payload().toString().contains(Utilities.stateOff)) {
		    				//Cambiamo Stato 
		    				this.state= Utilities.stateOff;
		    				
			    			//Caso terminazione irrigazione
			    			//this.irrigationCommandClient.disconnect();
			    			Logger.getLogger().print("DEBUG IRRIGAZIONE AUTOMATICA--- Fine irrigazione");
			    			
			    			
			    			//Al termine del processo di irrigazione, memorizziamo l'irrigazione, con l'effettiva durata
			    			this.irr.setFineIrrig(System.currentTimeMillis());
							float qualt= (this.irr.getFineIrrig()-this.irr.getInizioIrrig())*Utilities.capacita;
							this.irr.setQuantita(qualt);
							try {
								this.memorizationIrrigation(irr);
								Logger.getLogger().print(this.LOGIRR+"Irrigazione memorizzata");
							} catch (JsonProcessingException e) {
								Logger.getLogger().print("Impossibile memorizzare irrigazione: JsonProcessingException");
								Logger.getLogger().print(e.getMessage());
							}
							
							//Rendiamo null la variabile irrigazione
							this.irr=null;
			    			
		    			
			    		}else if(r.payload().toString().contains("ERROR")) {
							//Cambiamo Stato 
		    				this.state= Utilities.stateOff;
		    				
		    				//Rendiamo null l'irrigazione
							this.irr=null;
		    				
							//Caso errore
							Logger.getLogger().print("DEBUG IRRIGAZIONE AUTOMATICA--- ERROR: irrigazione non avviata");
							//this.irrigationCommandClient.disconnect();
			    	    	Logger.getLogger().print("DEBUG IRRIGAZIONE AUTOMATICA--- Stato attuale: errore attivazione.");
			    		}
		
					//Utilizziamo un canale differente
				}).subscribe("Irrigation-RESPONSE", 2);
				
				//Invio comando di azionamento dell'irrigazione passando l'irrigazione in formato json al Gateway
				this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
	  	    		  Buffer.buffer(JsonObject.mapFrom(irr).toString()),
							  MqttQoS.AT_LEAST_ONCE,
							  false,
							  false);	
			});
		else {
			
			//Cambio lo stato da subito poiche' potrebbe arrivare una richiesta di irrigazione diretta
			//Lo facciamo dopo la corretta connessione 
			this.state= Utilities.stateLock;
			
			//Handler di attesa della risposta positiva da parte del gateway
			this.irrigationCommandClient.publishHandler(r-> {
				
				//Attendiamo risposta positiva dal gateway
				if(r.payload().toString().contains(Utilities.stateOn)) {
					
					//this.irrigationCommandClient.disconnect();
					
					//IMPOSTO LO STATO DELL'IRRIGAZIONE A ON
					this.state=Utilities.stateOn;
					Logger.getLogger().print("DEBUG IRRIGAZIONE AUTOMATICA--- start effettuato con successo.");

	    			  
					
	    		}else
	    			//Risposta irrigazione		===============================
	    			if(r.payload().toString().contains(Utilities.stateOff)) {
	    				//Cambiamo Stato 
	    				this.state= Utilities.stateOff;
	    				
		    			//Caso terminazione irrigazione
		    			//this.irrigationCommandClient.disconnect();
		    			Logger.getLogger().print("DEBUG IRRIGAZIONE AUTOMATICA--- Fine irrigazione");
		    			
		    			//Al termine del processo di irrigazione, memorizziamo l'irrigazione, con l'effettiva durata
		    			this.irr.setFineIrrig(System.currentTimeMillis());
						float qualt= (this.irr.getFineIrrig()-this.irr.getInizioIrrig())*ConfigurationController.portataIrrigation/1000;
						this.irr.setQuantita(qualt);
						try {
							this.memorizationIrrigation(irr);
							Logger.getLogger().print(this.LOGIRR+"Irrigazione memorizzata");
						} catch (JsonProcessingException e) {
							Logger.getLogger().print("Impossibile memorizzare irrigazione: JsonProcessingException");
							Logger.getLogger().print(e.getMessage());
						}
						
						//Rendiamo null la variabile irrigazione
						this.irr=null;
		    			
		    			
	    			
		    		}
					else if(r.payload().toString().contains("ERROR")) {
						//Cambiamo Stato 
	    				this.state= Utilities.stateOff;
	    				
	    				//Rendiamo null la variabile irrigazione
						this.irr=null;
	    				
						//Caso errore
						Logger.getLogger().print("DEBUG IRRIGAZIONE AUTOMATICA--- ERROR: irrigazione non avviata");
						//this.irrigationCommandClient.disconnect();
		    	    	Logger.getLogger().print("DEBUG IRRIGAZIONE AUTOMATICA--- Stato attuale: errore attivazione.");
		    		}
	
				//Utilizziamo un canale differente
			}).subscribe("Irrigation-RESPONSE", 2);
			
			//Invio comando di azionamento dell'irrigazione passando l'irrigazione in formato json al Gateway
			this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
  	    		  Buffer.buffer(JsonObject.mapFrom(irr).toString()),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);
			
		}
			  
	}
	
	
	
	
	/*
	 * Metodo di avvio diretto dell'irrigazione, senza durata
	 */
	public Future<Irrigazione> startIrrigationDirect(Campo c) {
		
		Promise<Irrigazione> resultStart= Promise.promise();
		
		//Controlliamo che non sia gia' in esecuzione l'irrigazione
		if(c.getNome().equalsIgnoreCase(Campo.AUTO.getNome()) &&  this.state!=Utilities.stateOn && this.state!=Utilities.stateLock) {		
			
			if(!this.irrigationCommandClient.isConnected())
				//Invio il comando di irrigazione al gateway
				this.irrigationCommandClient.connect(1883, Utilities.ipMqtt, t ->{
					
					this.irrigationCommandClient.publishHandler(resp->{
						if(resp.payload().toString().contains("DONE")) {
							this.state=Utilities.stateOn;
							Logger.getLogger().print("Irrigazione avviata!");
							//Creiamo nuova irrigazione
							this.irr= new Irrigazione(System.currentTimeMillis());
							resultStart.complete(this.irr);
						}
						else if(resp.payload().toString().contains("FAIL")) {
							Logger.getLogger().print("Errore avvio irrigazione");
							resultStart.fail("FAIL");
						}
						this.irrigationCommandClient.disconnect();
					}).subscribe("Irrigation-RESPONSE", 2);
					
					Logger.getLogger().print("DEBUG IRRIGAZIONE--- INVIO STATE-ON AL GATEWAY");
					this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
		    	    		  Buffer.buffer(Utilities.stateOn),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);
					
					//attendiamo l'effettivo invio del comando
					//this.irrigationCommandClient.publishCompletionHandler(id ->{
					//this.irrigationCommandClient.disconnect();
						
					Logger.getLogger().print("Invio comando start effettuato.");	    	
				});
			//CASO GIA' CONNESSO============================================================================
			else {
				this.irrigationCommandClient.publishHandler(resp->{
					if(resp.payload().toString().contains("DONE")) {
						this.state=Utilities.stateOn;
						Logger.getLogger().print("Irrigazione avviata!");
						//Creiamo nuova irrigazione
						this.irr= new Irrigazione(System.currentTimeMillis());
						
						//Settiamo il command client in ascolto per eventuale spegnimento 
						//dovuto a tempo massimo di irrigazione
						Future<Boolean> forcedCloseList=this.listenForIrrigationForceClose();
						forcedCloseList.onComplete(h->{
							resultStart.complete(this.irr);
						});
						//resultStart.complete(this.irr);
					}
					else if(resp.payload().toString().contains("FAIL")) {
						Logger.getLogger().print("Errore avvio irrigazione");
						resultStart.fail("FAIL");
					}
					//this.irrigationCommandClient.disconnect();
				}).subscribe("Irrigation-RESPONSE", 2);
				
				Logger.getLogger().print("DEBUG IRRIGAZIONE--- INVIO STATE-ON AL GATEWAY");
				this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
	    	    		  Buffer.buffer(Utilities.stateOn),
							  MqttQoS.AT_LEAST_ONCE,
							  false,
							  false);
				
			}
			
			//AVVIO IRRIGAZIONE SECONDO CAMPO ==========================================================================================
		}else if(this.statoRelMan==Utilities.stateOff && c.getNome().equalsIgnoreCase(Campo.MANUAL.getNome()) ) {
			//invio comando avvio campo irrigazione classica
			if(!this.irrigationTradCommandCliet.isConnected())
				//Invio il comando di irrigazione al gateway
				this.irrigationTradCommandCliet.connect(1883, Utilities.ipMqtt, t ->{
					
					this.irrigationTradCommandCliet.publishHandler(resp->{
						if(resp.payload().toString().contains("DONE")) {
							Logger.getLogger().print("Irrigazione secondo campo avviata!");
							
							this.statoRelMan=Utilities.stateOn;
							
							//Creiamo nuova irrigazione
							this.irr= new Irrigazione(System.currentTimeMillis());
							
							System.out.println(this.irr.toString());
							resultStart.complete(this.irr);
						}
						else if(resp.payload().toString().contains("FAIL")) {
							Logger.getLogger().print("Errore avvio irrigazione");
							resultStart.fail("FAIL");
						}
						this.irrigationTradCommandCliet.disconnect();
					}).subscribe("Irrigation-RESPONSE", 2);
					
					Logger.getLogger().print("DEBUG IRRIGAZIONE--- INVIO STATE-ON AL GATEWAY PER IRRIGAZIONE SECONDO CAMPO");
					this.irrigationTradCommandCliet.publish(Utilities.irrigationCommandMqttChannel,
		    	    		  Buffer.buffer(this.stateC2On),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);
					
					//Per evitare problemi con handler della irrigazione automatica
					
					//attendiamo l'effettivo invio del comando
					//this.irrigationCommandClient.publishCompletionHandler(id ->{
					//this.irrigationCommandClient.disconnect();
						
					Logger.getLogger().print("Invio comando start effettuato.");	    	
				});
			//CASO GIA' CONNESSO============================================================================
			else {
				this.irrigationTradCommandCliet.publishHandler(resp->{
					if(resp.payload().toString().contains("DONE")) {
						
						Logger.getLogger().print("Irrigazione campo tradizionale avviata!");
						//Creiamo nuova irrigazione
						this.irr= new Irrigazione(System.currentTimeMillis());
						
						this.statoRelMan=Utilities.stateOn;
						
						resultStart.complete(this.irr);
						
					}
					else if(resp.payload().toString().contains("FAIL")) {
						Logger.getLogger().print("Errore avvio irrigazione campo tradizionale");
						resultStart.fail("FAIL");
					}
					//this.irrigationCommandClient.disconnect();
				}).subscribe("Irrigation-RESPONSE", 2);
				
				Logger.getLogger().print("DEBUG IRRIGAZIONE--- INVIO STATE-ON AL GATEWAY");
				this.irrigationTradCommandCliet.publish(Utilities.irrigationCommandMqttChannel,
	    	    		  Buffer.buffer(this.stateC2On),
							  MqttQoS.AT_LEAST_ONCE,
							  false,
							  false);	
			}	
		}else {
			Logger.getLogger().print("Irrigazione gia' avviata!");
			resultStart.fail("FAIL");
		}
		
		return resultStart.future();
		
	}
	
	/*
	 * Setting per ascoltare eventuale chiusura forzata dell'irrigazione
	 */
	private Future<Boolean> listenForIrrigationForceClose(){
		
		Logger.getLogger().print(this.LOGIRR+" irrigation forced close setting!");
		
		Promise<Boolean> resultStop= Promise.promise();
		
		this.irrigationCommandClient.publishHandler(resp->{
			if(resp.payload().toString().contains("DONE")) {
				Logger.getLogger().print(this.LOGIRR+"IRRIGATION FORCED CLOSE!");
				this.state=Utilities.stateOff;
				Logger.getLogger().print("Irrigazione fermata!");
				//Salviamo l'irrigazione
				if(this.irr!=null) {
					this.irr.setFineIrrig(System.currentTimeMillis());
					float qualt= ((this.irr.getFineIrrig()-this.irr.getInizioIrrig())*ConfigurationController.portataIrrigation)/1000;
					this.irr.setQuantita(qualt);
					try {
						this.memorizationIrrigation(irr);
						Logger.getLogger().print(this.LOGIRR+"Irrigazione memorizzata");
					} catch (JsonProcessingException e) {
						Logger.getLogger().print("Impossibile memorizzare irrigazione: JsonProcessingException");
						Logger.getLogger().print(e.getMessage());
					}
				}
				//resultStop.complete(true);
			}else if(resp.payload().toString().contains("FAIL")) {
				Logger.getLogger().print("Errore stop irrigazione!");
				//resultStop.fail("FAIL");
			}
			
			//Riportiamo la variabile irr a null
			this.irr=null;
			
			//Disconnessione
			this.irrigationCommandClient.disconnect();
			
		}).subscribe("Irrigation-RESPONSE", 2);
		
		resultStop.complete(true);
		return resultStop.future();
	}
	
	
	
	/*
	 * Metodo stop irrigazione diretto. Utilizzabile nel caso di assenza tempo di irrigazione
	 */
	public Future<Irrigazione> stopIrrigationDirect(Campo c) {
		
		Promise<Irrigazione> resultStop= Promise.promise();
		
		//Forziamo disconnessione canale di Command rimasto in ascolto per eventuale chiusura forzata dal tempo massimo di irrigazione
		//this.irrigationCommandClient.disconnect();
		
		if(c.getNome().equalsIgnoreCase(Campo.AUTO.getNome()) && this.state!=Utilities.stateOff && this.state!=Utilities.stateLock)
			
				if(!this.irrigationCommandClient.isConnected())
					this.irrigationCommandClient.connect(1883, Utilities.ipMqtt, v ->{
						
						this.irrigationCommandClient.publishHandler(resp->{
							if(resp.payload().toString().contains("DONE")) {
								this.state=Utilities.stateOff;
								Logger.getLogger().print("Irrigazione fermata!");
								//Salviamo l'irrigazione
								if(this.irr!=null) {
									this.irr.setFineIrrig(System.currentTimeMillis());
									float qualt= ((this.irr.getFineIrrig()-this.irr.getInizioIrrig())*ConfigurationController.portataIrrigation)/1000;
									this.irr.setQuantita(qualt);
									try {
										this.memorizationIrrigation(irr);
										Logger.getLogger().print(this.LOGIRR+"Irrigazione memorizzata");
									} catch (JsonProcessingException e) {
										Logger.getLogger().print("Impossibile memorizzare irrigazione: JsonProcessingException");
										Logger.getLogger().print(e.getMessage());
									}
								}
								
								
								
								resultStop.complete(this.irr);
								
							}else if(resp.payload().toString().contains("FAIL")) {
								Logger.getLogger().print("Errore stop irrigazione!");
								resultStop.fail("FAIL");
								
							}
							
							//Riportiamo la variabile irr a null
							this.irr=null;
							
							//Disconnessione
							this.irrigationCommandClient.disconnect();
							
						}).subscribe("Irrigation-RESPONSE", 2);
						
						Logger.getLogger().print("stopIrrigazione diretta-DEBUG: INVIO STATE-OFF AL GATEWAY");
						
						this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
			    	    		  Buffer.buffer(Utilities.stateOff),
									  MqttQoS.AT_LEAST_ONCE,
									  false,
									  false);
						Logger.getLogger().print("stopIrrigazione diretta-DEBUG: Invio comando stop effettuato.");		
						});		
				else {
					//CASO GIA' CONNESSO============================================================================
					this.irrigationCommandClient.publishHandler(resp->{
						if(resp.payload().toString().contains("DONE")) {
							this.state=Utilities.stateOff;
							Logger.getLogger().print("Irrigazione fermata!");
							//Salviamo l'irrigazione
							if(this.irr!=null) {
								this.irr.setFineIrrig(System.currentTimeMillis());
								float qualt= ((this.irr.getFineIrrig()-this.irr.getInizioIrrig())*ConfigurationController.portataIrrigation)/1000;
								this.irr.setQuantita(qualt);
								try {
									this.memorizationIrrigation(irr);
									Logger.getLogger().print(this.LOGIRR+"Irrigazione memorizzata");
								} catch (JsonProcessingException e) {
									Logger.getLogger().print("Impossibile memorizzare irrigazione: JsonProcessingException");
									Logger.getLogger().print(e.getMessage());
								}
							}
							
							resultStop.complete(this.irr);
							
						}else if(resp.payload().toString().contains("FAIL")) {
							Logger.getLogger().print("Errore stop irrigazione!");
							resultStop.fail("FAIL");
						}
						
						//Riportiamo la variabile irr a null
						this.irr=null;
						
						//Disconnessione
						this.irrigationCommandClient.disconnect();
						
					}).subscribe("Irrigation-RESPONSE", 2);
					
					Logger.getLogger().print("stopIrrigazione diretta-DEBUG: INVIO STATE-OFF AL GATEWAY");
					
					this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
		    	    		  Buffer.buffer(Utilities.stateOff),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);
					Logger.getLogger().print("stopIrrigazione diretta-DEBUG: Invio comando stop effettuato.");
				}
		
		
		//STOP CAMPO MANUALE ----------------------------------------------------------------------------------------------
		else if(this.statoRelMan==Utilities.stateOn &&  c.getNome().equalsIgnoreCase(Campo.MANUAL.getNome())) {
			
			if(!this.irrigationTradCommandCliet.isConnected())
				this.irrigationTradCommandCliet.connect(1883, Utilities.ipMqtt, v ->{
					
					this.irrigationTradCommandCliet.publishHandler(resp->{
						if(resp.payload().toString().contains("DONE")) {
							Logger.getLogger().print("Irrigazione campo tradizionale fermata!");
							
							this.statoRelMan=Utilities.stateOff;
							
							resultStop.complete(this.irr);
							
						}else if(resp.payload().toString().contains("FAIL")) {
							Logger.getLogger().print("Errore stop irrigazione!");
							resultStop.fail("FAIL");
							
						}
						//Disconnessione
						this.irrigationTradCommandCliet.disconnect();
						
					}).subscribe("Irrigation-RESPONSE", 2);
					
					Logger.getLogger().print("stopIrrigazione diretta-DEBUG: INVIO STATE-OFF AL GATEWAY");
					
					this.irrigationTradCommandCliet.publish(Utilities.irrigationCommandMqttChannel,
		    	    		  Buffer.buffer(this.stateC2Off),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);
					Logger.getLogger().print("stopIrrigazione diretta-DEBUG: Invio comando stop al campo tradizionale effettuato.");		
					});		
			else {
				//CASO GIA' CONNESSO============================================================================
				this.irrigationTradCommandCliet.publishHandler(resp->{
					if(resp.payload().toString().contains("DONE")) {
						Logger.getLogger().print("Irrigazione campo tradizionale fermata!");
						
						this.statoRelMan=Utilities.stateOff;
						
						resultStop.complete(this.irr);		
					}else if(resp.payload().toString().contains("FAIL")) {
						Logger.getLogger().print("Errore stop irrigazione!");
						resultStop.fail("FAIL");
					}
					//Disconnessione
					this.irrigationTradCommandCliet.disconnect();
					
				}).subscribe("Irrigation-RESPONSE", 2);
				
				Logger.getLogger().print("stopIrrigazione diretta-DEBUG: INVIO STATE-OFF AL GATEWAY");
				
				this.irrigationTradCommandCliet.publish(Utilities.irrigationCommandMqttChannel,
	    	    		  Buffer.buffer(this.stateC2Off),
							  MqttQoS.AT_LEAST_ONCE,
							  false,
							  false);
				Logger.getLogger().print("stopIrrigazione diretta-DEBUG: Invio comando stop al campo tradizionale effettuato.");				
			}
		}
		else {
			Logger.getLogger().print("Irrigazione gia' fermata o in corso irrigazione automatica.");
			resultStop.fail("FAIL");
		}
		
		return resultStop.future();
		
	}
	
	
	
	
	
	
	
	private void memorizationIrrigation(Irrigazione irr) throws JsonProcessingException {
		
		//Genero il json
		String jsonIrr=new ObjectMapper().writeValueAsString(irr);
		JsonObject jsonObjIrr= new JsonObject(jsonIrr);
		
		mongoClient.insert("Irrigazioni", jsonObjIrr , res ->{
			  if(res.succeeded())
				  Logger.getLogger().print(LOGIRR+"Irrigazione salvata correttamente nel DB.");
			  else
				  Logger.getLogger().print(LOGIRR+"ERRORE salvataggio Irrigazione");  
			});		
	}
	
	
	
	/*
	 * Metodo per il calcolo della distanza temporale tra il tempo ld e il tempo attuale
	 * 
	 */
	public long delayFromNewIrrigation(LocalTime ld) {
		
		//Calcoliamo il delay come la distanza da questo istante all'orario di irrigazione
		LocalTime now= LocalTime.now();
		
		
		if(now.compareTo(ld)==1) { 
			//Now e' maggiore
			//Dobbiamo calcolare il tempo sommando la differenza tra il LocalTime massimo e now e il LocalTimeMinimo e ld
			return now.until(LocalTime.MAX, ChronoUnit.MILLIS)+ ld.until(LocalTime.MIN, ChronoUnit.MILLIS)*-1;
		}
		else
			return now.until(ld, ChronoUnit.MILLIS);
	
	}
	
	
	public String getState() {
		return state;
	}
	
	public String getStateTrad() {
		return this.statoRelMan;
	}

	public void setState(String state) {
		this.state = state;
	}

	public LocalTime getStartingTimeIrrigation() {
		return StartingTimeIrrigation;
	}

	public void setStartingTimeIrrigation(LocalTime defaultStartingTimeIrrigation) {
		this.StartingTimeIrrigation = defaultStartingTimeIrrigation;
	}
	
	
	
	
	
	public Irrigazione getIrr() {
		return irr;
	}





	/*
	 * Da definire
	 */
	class IrrigationStrategy extends Strategy{
		
		
		public Irrigazione createIrrigation(ClassificationSint cs) throws JsonMappingException, JsonProcessingException  {
			
			Logger.getLogger().print("IRRIGATION-STRATEGY: Classificazione prelevata dal db: "+cs.toString());
			//Caso ECCESSO ACQUA -> no irrigazione
			Status statusclass= cs.getStatus();
			
			if(statusclass==Status.ECCESSO) {
				Logger.getLogger().print("IRRIGATION-STRATEGY: risultato classificazione: ECCESSO di acqua");
				return new Irrigazione(0,0,0);
			}
			
			//caso CRESCITA REGOLARE -> irrigazione standard
			if(statusclass==Status.NORMALE) {
				Logger.getLogger().print("IRRIGATION-STRATEGY: risultato classificazione: stato piante NORMALE");
				
				SensorDataControllerSync sd= new SensorDataControllerSync();
				
				
				
				// Calcolo quantita' acqua ------------------------
				
				
				//prelevare piogge giornaliere canale 11
				float dayrain = sd.getLastSamplesFromMongoSynch(11).getMisure();
				float temperatura= sd.getLastSamplesFromMongoSynch(3).getMisure();
				//Controlliamo dati vento, canale 4
				float windspeed = sd.getLastSamplesFromMongoSynch(4).getMisure();
				//convertiamo il vento da MPH(miglia per ore) a Km/h (chilometri per ora) 1 MPH = 1.609Km/h
				windspeed = (float) (windspeed*1.609);
				
				////////////	UMIDITA'	////////////////
				//Canali umidita' seriale
				ArrayList<Integer> serialC= ConfigurationController.idSerialChannel;
				//Canali contenente dati umidita' da CSV
				ArrayList<Integer> csv= SampleCSVController.channelNumberCSV;
				
				serialC.addAll(csv);
				/////////////////////////////////////////////////////////////////////////////////UMIDITA' 
				float umid= sd.meanMeasureOfChannels(serialC);
				
				ArrayList<Sample> temperaturs= sd.getAllSamplesFromTimeFrame(60*60*24*1000l, 3);
				float pressione = sd.getLastSamplesFromMongoSynch(0).getMisure();
				double quant= Evapotranspiration.calculate(temperatura, umid, temperaturs, pressione, windspeed);
				//-------------------------------------------------
					
			}
			
			//caso CARENZA -> controllo soglie
			if(statusclass==Status.CARENZA) {
				Logger.getLogger().print("IRRIGATION-STRATEGY: risultato classificazione: CARENZA ");
				
				SensorDataControllerSync sd= new SensorDataControllerSync();
				
				//Controllo temperature : canale 3 : OutSideTemp
				float temp= sd.getLastSamplesFromMongoSynch(3).getMisure();
				
				//Controlliamo che al temperatura sia stata corretamente misurata
				if(temp!=-273.15) {

					//Caso temperatura ottimale (inferiore ai 32 gradi): TEMPERATURA OTTIMALE
					if(temp<32) {
						//Controlliamo dati vento, canale 4
						float windspeed = sd.getLastSamplesFromMongoSynch(4).getMisure();
						//convertiamo il vento da MPH(miglia per ore) a Km/h (chilometri per ora) 1 MPH = 1.609Km/h
						windspeed = (float) (windspeed*1.609);
						
						//Controlliamo se il vento supera i 15 kmh : VENTO NON OTTIMALE
						if(windspeed>15) {
							Logger.getLogger().print("IRRIGATION-STRATEGY: temperatura ottimale, vento non ottimale : irrigazione sospesa!");
							return new Irrigazione(0,0,0);
						}
						
						//VENTO OTTIMALE ------------
						//Controllo precipitazione in corso: rtRainRate canale 8
						float rainrate=sd.getLastSamplesFromMongoSynch(8).getMisure(); 
						
						if(rainrate==-1) {
							//controllo se sta piovendo : PRECIPITAZIONE IN CORSO
							Logger.getLogger().print("IRRIGATION-STRATEGY: temperatura ottimale, vento ottimale,precipitazione in corso  : irrigazione sospesa!");
							return new Irrigazione(0,0,0);
						}else {
							//controllo se sta piovendo : PRECIPITAZIONE NON IN CORSO
								
							//controllo umidita' terreno: CONTROLLARE CANALI DA UTILIZZARE PER UMIDITA'
							//Canali umidita' seriale
							ArrayList<Integer> serialC= ConfigurationController.idSerialChannel;
							//Canali contenente dati umidita' da CSV
							ArrayList<Integer> csv= SampleCSVController.channelNumberCSV;
							
							serialC.addAll(csv);
							/////////////////////////////////////////////////////////////////////////////////UMIDITA' 
							float umid= sd.meanMeasureOfChannels(serialC);
							if(umid>80) {
								//controllo umidita' terreno: UMIDITA' OTTIMALE
								Logger.getLogger().print("IRRIGATION-STRATEGY: temperatura ottimale, vento ottimale,nessuna precipitazione in corso, umidita' relativa ottimale  : turno di irrigazione soppresso!");
								return new Irrigazione(0,0,0);
							}
							//controllo umidita' terreno: UMIDITA' NON OTTIMALE
							Logger.getLogger().print("IRRIGATION-STRATEGY: temperatura ottimale, vento ottimale,nessuna precipitazione in corso,umidita' relativa non ottimale  : irrigazione standard ridotta 1/2");
							//Quantita' da usare = quant Standard/2
							ArrayList<Sample> temperaturs= sd.getAllSamplesFromTimeFrame(60*60*24*1000l, 3);
							float pressione = sd.getLastSamplesFromMongoSynch(0).getMisure();
							double quant= Evapotranspiration.calculate(temp, umid, temperaturs, pressione, windspeed)/2;
							return new Irrigazione((float)quant);
						}
					} 
						
					//Caso temperatura ottimale (inferiore ai 32 gradi): TEMPERATURA NON OTTIMALE - TEMPERATURA > 32 
					if(temp>=32 ) {
						//Controlliamo dati vento, canale 4
						float windspeed = sd.getLastSamplesFromMongoSynch(4).getMisure();
						//convertiamo il vento da MPH(miglia per ore) a Km/h (chilometri per ora) 1 MPH = 1.609Km/h
						windspeed = (float) (windspeed*1.609);
						
						//Controlliamo se il vento supera i 15 kmh : VENTO NON OTTIMALE
						if(windspeed>15) {
							
							//Controllo precipitazione in corso: rtRainRate canale 8
							float rainrate=sd.getLastSamplesFromMongoSynch(8).getMisure(); 
							
							if(rainrate==-1) {
								//controllo se sta piovendo : PRECIPITAZIONE IN CORSO
								Logger.getLogger().print("IRRIGATION-STRATEGY: temperatura non ottimale, vento non ottimale,precipitazione in corso  : irrigazione sospesa!");
								return new Irrigazione(0,0,0);
							}
							//-----NESSUNA PRECIPITAZIONE IN CORSO
							//controllo umidita' terreno: CONTROLLARE CANALI DA UTILIZZARE PER UMIDITA'  
							//Canali umidita' seriale
							ArrayList<Integer> serialC= ConfigurationController.idSerialChannel;
							//Canali contenente dati umidita' da CSV
							ArrayList<Integer> csv= SampleCSVController.channelNumberCSV;
							
							serialC.addAll(csv);
							float umid= sd.meanMeasureOfChannels(serialC);
							
							if(umid>80) {
								//controllo umidita' terreno: UMIDITA' OTTIMALE
								Logger.getLogger().print("IRRIGATION-STRATEGY: temperatura non ottimale, vento non ottimale,nessuna precipitazione in corso, umidita' relativa ottimale  : turno di irrigazione soppresso!");
								return new Irrigazione(0,0,0);
							}
							//UMIDITA' NON OTTIMALE
							Logger.getLogger().print("IRRIGATION-STRATEGY: temperatura non ottimale, vento non ottimale,nessuna precipitazione in corso, umidita' relativa non ottimale : irrigazione standard ridotta 1/4");
							
							ArrayList<Sample> temperaturs= sd.getAllSamplesFromTimeFrame(60*60*24*1000l, 3);
							float pressione = sd.getLastSamplesFromMongoSynch(0).getMisure();
							double quant= Evapotranspiration.calculate(temp, umid, temperaturs, pressione, windspeed)/4;
							return new Irrigazione((float)quant);
						
						}else {
						//------------VENTO OTTIMALE
							//Controllo precipitazione in corso: rtRainRate canale 8
							float rainrate=sd.getLastSamplesFromMongoSynch(8).getMisure(); 
							
							if(rainrate==-1) {
								//controllo se sta piovendo : PRECIPITAZIONE IN CORSO
								Logger.getLogger().print("IRRIGATION-STRATEGY: temperatura non ottimale, vento ottimale,precipitazione in corso  : irrigazione sospesa!");
								return new Irrigazione(0,0,0);
							}
							
							//-----NESSUNA PRECIPITAZIONE IN CORSO
							//controllo umidita' terreno: CONTROLLARE CANALI DA UTILIZZARE PER UMIDITA'  
							//Canali umidita' seriale
							ArrayList<Integer> serialC= ConfigurationController.idSerialChannel;
							//Canali contenente dati umidita' da CSV
							ArrayList<Integer> csv= SampleCSVController.channelNumberCSV;
							
							serialC.addAll(csv);
							float umid= sd.meanMeasureOfChannels(serialC);
							
							if(umid>80) {
								//controllo umidita' terreno: UMIDITA' OTTIMALE
								Logger.getLogger().print("IRRIGATION-STRATEGY: temperatura non ottimale, vento ottimale,nessuna precipitazione in corso, umidita' relativa ottimale  : turno di irrigazione soppresso!");
								return new Irrigazione(0,0,0);
							}
							//UMIDITA' NON OTTIMALE
							Logger.getLogger().print("IRRIGATION-STRATEGY: temperatura non ottimale, vento ottimale,nessuna precipitazione in corso, umidita' relativa non ottimale : irrigazione standard ridotta 1/4");
							
							ArrayList<Sample> temperaturs= sd.getAllSamplesFromTimeFrame(60*60*24*1000l, 3);
							float pressione = sd.getLastSamplesFromMongoSynch(0).getMisure();
							double quant= Evapotranspiration.calculate(temp, umid, temperaturs, pressione, windspeed)/2;
							return new Irrigazione((float)quant);
						}
					}
					
					
					
				}else {
					//CASO TEMPERATURA NON CORRETTAMENTE MISURATA, IRRIGAZIONE SOSPESA! Eventuale avviso
					Logger.getLogger().print("IRRIGATION-STRATEGY: temperatura non correttamente misurata! Controllare sensore temperatura WeatherStation ! ");
					Logger.getLogger().print("IRRIGATION-STRATEGY: temperatura non correttamente misurata! irrigazione sospesa!");
					return new Irrigazione(0,0,0);
				}
				
			}
			
			//CASO NESSUNO DEGLI IF SIA ANDATO A BUON FINE
			Logger.getLogger().print("Dati non compatibili per la creazione di una irrigazione!");
			return new Irrigazione(0,0,0);
			
			
			//GETIONE ALTRI STATUS
			//STATUS CLASSIFICAZIONE NON RIUSCITA
			//STATUS CLASSIFICAZIONE PIANTE MALATE
			
		}

		private float calcolo(double temperatura,double umidRel,ArrayList<Sample> temperature,double pressione,double vento) {
			//double etc=Evapotranspiration.calculate(temperatura, umidRel, temperature, pressione, vento);
			//return  etc-piogge?-risalita? ;
			return 0;
		}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
