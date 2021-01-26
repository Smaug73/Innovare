package com.innovare.control;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class IrrigationController implements Job{

	private static final String LOGIRR="LOG-IRRIGATION-CONTROLLER: ";
	//Orario di start dell'irrigazione giornaliera
	private LocalTime startIrrigationTime= LocalTime.of(Utilities.hourStartIrrigation, Utilities.minuteStartIrrigation);
	private MongoClient mongoClient;
	private MqttClient irrigationCommandClient;
	private JobDetail job;
	private Trigger trigger;
	private Scheduler sch;
	
	
	private long timeWaitIrrigation=0;
	
	public IrrigationController(MongoClient mongC,MqttClient irrigationComClient) {
		this.mongoClient=mongC;
		this.irrigationCommandClient= irrigationComClient;
	}
	
	public void startSchedulingIrrigation() {
		
		try {
			System.out.println(LOGIRR+System.currentTimeMillis()+"  Avvio SCHEDULING job di Irrigazione...");
			this.job= JobBuilder.newJob(IrrigationController.class)
					.withIdentity("IrrigationJob")
					.build();
			this.trigger= TriggerBuilder.newTrigger()
										.withSchedule(SimpleScheduleBuilder.simpleSchedule()
																			.withIntervalInSeconds(20)
																			.repeatForever())
										.build();
			SchedulerFactory schFactory = new StdSchedulerFactory();
			this.sch= schFactory.getScheduler();
			this.sch.start();
			this.sch.scheduleJob(this.job,this.trigger);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		System.out.println(LOGIRR+System.currentTimeMillis()+"  Avvio job di Irrigazione...");
		JsonObject q= new JsonObject();
		//Controlliamo l'ultima classificazione effettuata
		this.mongoClient.find("ClassificazioniSintetiche",q, res-> {
    		/*
    		 * Successo nel trovare i sample nel db
    		 */
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
    		startIrrigation();
    		
    		
    		//Attesa irrigazione
    		//Mettiamo in sleep il thread
    		timeWaitIrrigation=newIrr.getFineIrrig()-newIrr.getInizioIrrig();
			try {
				Thread.sleep(newIrr.getFineIrrig()-newIrr.getInizioIrrig());
			} catch (InterruptedException e) {
				System.err.println("Errore nella sleep del thread IrrigationController");
				e.printStackTrace();
			}
    		
    		/*
    		 * Stop-Irrigazione
    		 */
    		stopIrrigation();
    		
    		//Attendiamo il tempo per riattivare il thread per l'irrigazione
    	});		
		
	}
	
	
	/*
	 * Thread per la gestione dell'irrigazione giornaliera
	 *
	public void run(){
		/*
		 * Il thread deve verificare l'ultima classificazione generata e deve basarsi su quella
		 * generare la nuova irrigazione
		 *
		while(true) {
				
			JsonObject q= new JsonObject();
			//Controlliamo l'ultima classificazione effettuata
			this.mongoClient.find("ClassificazioniSintetiche",q, res-> {
	    		/*
	    		 * Successo nel trovare i sample nel db
	    		 *
				Irrigazione newIrr;
	    		if(res.succeeded()) {
	    			/*
	    			 * Prendiamo solo l'ultima
	    			 *
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
	    				 *
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
	    		 *
	    		else {
	    			System.out.println(LOGIRR+"Nessun db mongo di classificazioni trovato...");
	    			System.out.println(LOGIRR+"Creazione irrigazione NORMALE.");
	    			newIrr= new Irrigazione(Status.NORMALE);
	    		}
	    		
	    		/*
	    		 * Avvio irrigazione
	    		 *
	    		startIrrigation();
	    		
	    		
	    		//Attesa irrigazione
	    		//Mettiamo in sleep il thread
	    		timeWaitIrrigation=newIrr.getFineIrrig()-newIrr.getInizioIrrig();
				try {
					this.sleep(newIrr.getFineIrrig()-newIrr.getInizioIrrig());
				} catch (InterruptedException e) {
					System.err.println("Errore nella sleep del thread IrrigationController");
					e.printStackTrace();
				}
	    		
	    		/*
	    		 * Stop-Irrigazione
	    		 *
	    		stopIrrigation();
	    		
	    		//Attendiamo il tempo per riattivare il thread per l'irrigazione
	    	});		
			//Attesa irrigazione
    		//Mettiamo in sleep il thread fino alla prossima irrigazione
			try {
				this.sleep(60*60*24*7*1000-timeWaitIrrigation); //una settimana dopo meno il tempo perso ad irrigare
			} catch (InterruptedException e) {
				System.err.println("Errore nella sleep del thread IrrigationController");
				e.printStackTrace();
			}
			
		}	
	}
	*/
	
	
	
	/*
	 * Genera un nuovo piano di irrigazione basandosi sulla nuova classificazione e la precedente
	 * Per farlo decide l'orario di fine dell'irrigazione basandosi sulla classificazione acquisita
	 
	public Irrigazione createIrrigation(ClassificationSint lastClassification) {
		
		return new Irrigazione(lastClassification.getMaxPercForIrrigation());
		
	}
	*/
	
	private void startIrrigation() {
		this.irrigationCommandClient.connect(1883, Utilities.ipMqtt, t ->{
			this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
    	    		  Buffer.buffer(Utilities.stateOn),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);
			this.irrigationCommandClient.disconnect();
	    	System.out.println("Invio comando start effettuato.");
		  });
	}
	
	private void stopIrrigation() {
		this.irrigationCommandClient.connect(1883, Utilities.ipMqtt, v ->{
			
			this.irrigationCommandClient.publish(Utilities.irrigationCommandMqttChannel,
    	    		  Buffer.buffer(Utilities.stateOff),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);
			this.irrigationCommandClient.disconnect();
	    	
	    	System.out.println("Invio comando stop effettuato.");
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



	
	
	
	
	
}
