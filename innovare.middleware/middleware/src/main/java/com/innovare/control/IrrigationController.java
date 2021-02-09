package com.innovare.control;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.ClassificationSint;
import com.innovare.model.Irrigazione;
import com.innovare.utils.Utilities;
import com.innovare.model.Status;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.mqtt.MqttClient;

/*
 * Si occupa del controllo dell'innaffiamento
 */
public class IrrigationController extends TimerTask implements Job{

	private static final String LOGIRR="LOG-IRRIGATION-CONTROLLER: ";
	//Orario di start dell'irrigazione giornaliera
	private LocalTime startIrrigationTime= LocalTime.of(Utilities.hourStartIrrigation, Utilities.minuteStartIrrigation);
	private MongoClient mongoClient;
	private MqttClient irrigationCommandClient;
	private JobDetail job;
	private Trigger trigger;
	private Scheduler sch;
	
	private Irrigazione irr=null;
	
	private String state=Utilities.stateOff;
	
	private long timeWaitIrrigation=0;
	
	public IrrigationController(MongoClient mongC,MqttClient irrigationComClient) {
		this.mongoClient=mongC;
		this.irrigationCommandClient= irrigationComClient;
	}
	
	public void startSchedulingIrrigation() {
		
		try {
			System.out.println(LOGIRR+System.currentTimeMillis()+"  Avvio SCHEDULING job di Irrigazione...");
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
			
			System.out.println(LOGIRR+System.currentTimeMillis()+"  Avvio SCHEDULING start...");
			
		} catch (SchedulerException e) {
			System.err.println(LOGIRR+System.currentTimeMillis()+"ERRORE SCHEDULING..");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		System.out.println(LOGIRR+System.currentTimeMillis()+" IRRIGATION FLAG");
		
		//Controlliamo se non e' in eseguzione un'altra irrigazione
		if(this.state==Utilities.stateOn) {
			System.out.println(LOGIRR+System.currentTimeMillis()+" Irrigazione gia' in eseguzione!");
			return ;
		}else
			System.out.println(LOGIRR+System.currentTimeMillis()+" Irrigazione non in eseguzione...");
		
		
		System.out.println(LOGIRR+System.currentTimeMillis()+"  Avvio job di Irrigazione...");
		JsonObject q= new JsonObject();
		//Controlliamo l'ultima classificazione effettuata
		this.mongoClient.find("ClassificazioniSintetiche",q, res-> {
    		/*
    		 * Successo nel trovare i sample nel db
    		 */
			System.out.println(LOGIRR+System.currentTimeMillis()+" Ricerca ultima classificazione effettuata...");
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
						System.err.println(LOGIRR+"Errore conversione json.");
						e.printStackTrace();
					}		
    			}
    			
    			Collections.sort((List<ClassificationSint>) csa);
    			//ArrayList<ClassificationSint> arrayResp= new ArrayList<ClassificationSint>();
    			
    			System.out.println(LOGIRR+csa.toString());
    			if(csa.size()>=1) {
    				lastClassification=csa.get(0);
    				System.out.println(LOGIRR+"Ultima classificazione effettuata: "+lastClassification);
    				//Generiamo una nuova Irrigazione
    				newIrr= new Irrigazione(lastClassification.getMaxPercForIrrigation());
    				
    				System.out.println(LOGIRR+"Nuova Irrigazione: "+newIrr);
    				//Dopo averla creata l'irrigazione va memorizzata nel dataBase Mongo
    				try {
						memorizationIrrigation(newIrr);
					} catch (JsonProcessingException e) {
						
						e.printStackTrace();
					}
    			}
    			else {
    				/*
    				 * Nessuna classificazione acquisita precedentemente, irrigazione normale
    				 */
    				System.out.println(LOGIRR+"Nessuna classificazione precedente.");
    				newIrr= new Irrigazione(Status.NORMALE);
    				
    				System.out.println(LOGIRR+"Nuova Irrigazione: "+newIrr);
    				//Dopo averla creata l'irrigazione va memorizzata nel dataBase Mongo
    				try {
						memorizationIrrigation(newIrr);
					} catch (JsonProcessingException e) {					
						e.printStackTrace();
					}	
    			}  			
    		}
    		/*
    		 * Caso di fallimento
    		 */
    		else {
    			System.out.println(LOGIRR+"Nessun db mongo di classificazioni trovato...");
    			System.out.println(LOGIRR+"Creazione irrigazione NORMALE.");
    			newIrr= new Irrigazione(Status.NORMALE);
    		}
    		
    		
    		
    		
    		//Attesa irrigazione
    		//Mettiamo in sleep il thread
    		timeWaitIrrigation=newIrr.getFineIrrig()-newIrr.getInizioIrrig();
    		/*
    		 * Avvio irrigazione, controllo prima se non e' in corso un'altra irrigazione
    		 */
    		if(this.state== Utilities.stateOff)
    			startIrrigation(timeWaitIrrigation);
    		else
    			System.out.println(LOGIRR+"Irrigazione gia' in corso, non verra' iniziata altra irrigazione.");
    		
    		/*try {
				Thread.sleep(newIrr.getFineIrrig()-newIrr.getInizioIrrig());
			} catch (InterruptedException e) {
				System.err.println("Errore nella sleep del thread IrrigationController");
				e.printStackTrace();
			}
    		
    		/*
    		 * Stop-Irrigazione
    		 */
    		//stopIrrigation();
    		
    		//Attendiamo il tempo per riattivare il thread per l'irrigazione
    	});		
		
	}
	
	
	/*
	 * Thread per la gestione dell'irrigazione giornaliera
	 */
	public void run(){
		/*
		 * Il thread deve verificare l'ultima classificazione generata e deve basarsi su quella
		 * per generare la nuova irrigazione
		 */
		
			//Controlliamo se non e' in eseguzione un'altra irrigazione
			if(this.state==Utilities.stateOn) {
				//se e' in eseguzione usciamo
				System.out.println(LOGIRR+System.currentTimeMillis()+" Irrigazione gia' in eseguzione!");
				return ;
			}else
				System.out.println(LOGIRR+System.currentTimeMillis()+" Irrigazione non in eseguzione...");
			
			System.out.println(LOGIRR+System.currentTimeMillis()+" flag  Avvio job di Irrigazione...");	
			JsonObject q= new JsonObject();
			//Controlliamo l'ultima classificazione effettuata
			this.mongoClient.find("ClassificazioniSintetiche",q, res-> {
	    		/*
	    		 * Successo nel trovare i sample nel db
	    		 */
				System.out.println(LOGIRR+System.currentTimeMillis()+" Ricerca ultima classificazione effettuata...");
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
							System.err.println(LOGIRR+"Errore conversione json.");
							e.printStackTrace();
						}		
	    			}
	    			
	    			Collections.sort((List<ClassificationSint>) csa);
	    			//ArrayList<ClassificationSint> arrayResp= new ArrayList<ClassificationSint>();
	    			
	    			System.out.println(csa.toString());
	    			if(csa.size()>=1) {
	    				lastClassification=csa.get(0);
	    				System.out.println(LOGIRR+"Ultima classificazione effettuata: "+lastClassification);
	    				//Generiamo una nuova Irrigazione
	    				newIrr= new Irrigazione(lastClassification.getMaxPercForIrrigation());
	    				
	    				System.out.println(LOGIRR+"Nuova Irrigazione: "+newIrr);
	    				//Dopo averla creata l'irrigazione va memorizzata nel dataBase Mongo
	    				try {
							memorizationIrrigation(newIrr);
						} catch (JsonProcessingException e) {
							
							e.printStackTrace();
						}
	    			}
	    			else {
	    				/*
	    				 * Nessuna classificazione acquisita precedentemente, irrigazione normale
	    				 */
	    				System.out.println(LOGIRR+"Nessuna classificazione precedente.");
	    				newIrr= new Irrigazione(Status.NORMALE);
	    				
	    				System.out.println(LOGIRR+"Nuova Irrigazione: "+newIrr);
	    				//Dopo averla creata l'irrigazione va memorizzata nel dataBase Mongo
	    				try {
							memorizationIrrigation(newIrr);
						} catch (JsonProcessingException e) {					
							e.printStackTrace();
						}	
	    			}  			
	    		}
	    		/*
	    		 * Caso di fallimento
	    		 */
	    		else {
	    			System.out.println(LOGIRR+"Nessun db mongo di classificazioni trovato...");
	    			System.out.println(LOGIRR+"Creazione irrigazione NORMALE.");
	    			newIrr= new Irrigazione(Status.NORMALE);
	    		}
	    		
	    		/*
	    		 * Avvio irrigazione
	    		 */
	    		timeWaitIrrigation=newIrr.getFineIrrig()-newIrr.getInizioIrrig();
	    		startIrrigation(timeWaitIrrigation);
	    		
	    		
	    		
	    	});		
				
	}
	
	
	
	
	/*
	 * Genera un nuovo piano di irrigazione basandosi sulla nuova classificazione e la precedente
	 * Per farlo decide l'orario di fine dell'irrigazione basandosi sulla classificazione acquisita
	 
	public Irrigazione createIrrigation(ClassificationSint lastClassification) {
		
		return new Irrigazione(lastClassification.getMaxPercForIrrigation());
		
	}
	*/
	
	public void startIrrigation(long time) {
		
		this.irrigationCommandClient.connect(1883, Utilities.ipMqtt, t ->{
			System.out.println("DEBUG IRRIGAZIONE AUTOMATICA--- INVIO STATE-ON AL GATEWAY");
			
			this.irrigationCommandClient.publishHandler(r-> {
				//Attendiamo risposta dal gateway
				if(r.payload().toString().contains(Utilities.stateOn)) {
					
					this.irrigationCommandClient.disconnect();
					//IMPOSTO LO STATO DELL'IRRIGAZIONE A ON
					this.state=Utilities.stateOn;
					System.out.println("DEBUG IRRIGAZIONE AUTOMATICA--- Invio comando start effettuato con successo.");
					
					//Attendiamo fino alla fine dell'irrigazione
			    	try {
						Thread.sleep(time);
					} catch (InterruptedException e) {
						System.err.println("DEBUG IRRIGAZIONE AUTOMATICA--- Errore nella sleep del thread IrrigationController");
						e.printStackTrace();
					}
		    		
		    	
		    		 //Avviamo Stop-Irrigazione
		    		stopIrrigation();
	    			  
	    		}
				else if(r.payload().toString().contains("ERROR")) {
					//Caso errore
					System.out.println("DEBUG IRRIGAZIONE AUTOMATICA--- ERROR: irrigazione non avviata");
					this.irrigationCommandClient.disconnect();
	    	    	System.out.println("DEBUG IRRIGAZIONE AUTOMATICA--- Stato attuale: errore attivazione.");
	    		}
	
			}).subscribe("Irrigation-LOG", 2);
			
			//Invio comando di azionamento dell'irrigazione
			this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
  	    		  Buffer.buffer(Utilities.stateOn),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);	
			
		});
			  
	}
	
	
	
	
	public void stopIrrigation() {
		//this.state=Utilities.stateOff;
		this.irrigationCommandClient.connect(1883, Utilities.ipMqtt, v ->{
			
			this.irrigationCommandClient.publishHandler(r->{
				
				if(r.payload().toString().contains(Utilities.stateOff)) {
	    			  this.setState(Utilities.stateOff);
	    			  System.out.println("DEBUG IRRIGAZIONE AUTOMATICA--- stato irrigazione modificato OFF");
	    			  this.irrigationCommandClient.disconnect(); 
	    			  
	    		}
				else if(r.payload().toString().contains("ERROR")) {
					//Caso errore
					System.out.println("---DEBUG IRRIGAZIONE-LOG---- ERROR: irrigazione non disattivata");
					//ERRORE NEL DISATTIVARE IMPIANTO DI IRRIGAZIONE
	    		}
	
			}).subscribe("Irrigation-LOG", 2);
					
			System.out.println("DEBUG IRRIGAZIONE--- INVIO STATE-OFF AL GATEWAY");
			this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
    	    		  Buffer.buffer(Utilities.stateOff),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);
			
		  });
	}
	
	
	
	//Questo metodo viene chiamato quando non si sa subito il tempo di durata dell'irrigazione
	//Viene eseguito dalla chiamata rest di start dell'irrigazione
	public void startIrrigationDirect() {
		//cambiamo stato di irrigazione
		this.state=Utilities.stateOn;
		
		//Creiamo nuova irrigazione
		this.irr= new Irrigazione(System.currentTimeMillis());
		
		//Invio il comando di irrigazione al gateway
		this.irrigationCommandClient.connect(1883, Utilities.ipMqtt, t ->{
			System.out.println("DEBUG IRRIGAZIONE--- INVIO STATE-ON AL GATEWAY");
			this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
    	    		  Buffer.buffer(Utilities.stateOn),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);
			//attendiamo l'effettivo invio del comando
			this.irrigationCommandClient.publishCompletionHandler(id ->{
				this.irrigationCommandClient.disconnect();
				
				System.out.println("Invio comando start effettuato.");	    	
			});
	
		  });
	}
	
	public void stopIrrigationDirect() {
		this.state=Utilities.stateOff;
		this.irrigationCommandClient.connect(1883, Utilities.ipMqtt, v ->{
			System.out.println("DEBUG IRRIGAZIONE--- INVIO STATE-OFF AL GATEWAY");
			this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
    	    		  Buffer.buffer(Utilities.stateOff),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);
			//attendiamo l'effettivo invio del comando
			this.irrigationCommandClient.publishCompletionHandler(id ->{
				this.irrigationCommandClient.disconnect();
				
				System.out.println("Invio comando stop effettuato.");	
				
				//Salviamo l'irrigazione
				if(this.irr!=null) {
					this.irr.setFineIrrig(System.currentTimeMillis());
					float qualt= (this.irr.getFineIrrig()-this.irr.getInizioIrrig())*Utilities.capacita;
					this.irr.setQuantita(qualt);
					try {
						this.memorizationIrrigation(irr);
						System.err.println("Irrigazione memorizzata");
					} catch (JsonProcessingException e) {
						System.err.println("Impossibile memorizzare irrigazione: JsonProcessingException");
						System.err.println(e.getMessage());
					}
				}
			});		
		  });
	}
	
	private void memorizationIrrigation(Irrigazione irr) throws JsonProcessingException {
		
		//Genero il json
		String jsonIrr=new ObjectMapper().writeValueAsString(irr);
		JsonObject jsonObjIrr= new JsonObject(jsonIrr);
		
		mongoClient.insert("Irrigazioni", jsonObjIrr , res ->{
			  if(res.succeeded())
				  System.out.println(LOGIRR+"Irrigazione salvata correttamente nel DB.");
			  else
				  System.err.println(LOGIRR+"ERRORE salvataggio Irrigazione");  
			});		
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	
}
