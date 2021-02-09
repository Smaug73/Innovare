package com.innovare.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.control.ConfigurationController;
import com.innovare.gateway.GatewayVerticle;
import com.innovare.utils.Utilities;

import afu.org.checkerframework.checker.units.qual.Time;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;

import java.io.IOException;
import java.sql.Timestamp;

public class IrrigationController extends Thread{

	
	public static final String stateOff="OFF";
	public static final String stateOn="ON";
	
	//Stato inizialmente impostato ad off
	private String stato=this.stateOff;
	private MqttClient logClient=null;
	private MqttClient commandClient=null;
	
	public static final String scriptPathIrrigation="python3 "+System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareData"+System.getProperty("file.separator")+"InnovareScript"+System.getProperty("file.separator")+" python3 irrigationCommand.py";

	
	private long irrigationTime; 
	
	/*
	 * Si potrebbe controllare il reale stato dell'irrigazione nel momento della
	 * instanziazione per settare lo stato di conseguenza, in modo di non avere 
	 * eventuali incongruenze sul campo.
	 */
	public IrrigationController() {
	}
	
	
	/*
	 * Metodo per l'avvio dell'irrigazione
	 * Una volta avviata l'irrigazione passiamo allo script il tempo di irrigazione
	 * Si fer
	 */
	public void startIrrigation(long time) {
		/*
		 * Manca la parte dell'avvio dell'irrigazione
		 */	
		this.irrigationTime=time;
		////
		this.stato=this.stateOn;
		Timestamp tm= new Timestamp(System.currentTimeMillis());
		if(this.logClient!=null) {
			//Connesione al server mqtt
			this.logClient.connect(1883, Utilities.ipMqtt, s -> {	
				
				System.out.println(tm+": Avvio irrigazione.");
				this.logClient.publish("Irrigation-LOG", Buffer.buffer(tm+": "+this.stateOn),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);		
				this.logClient.disconnect();//Per evitare problemi relativi alla connessione
		    });		
		}else {
			System.out.println("Client mqtt per il log non instanziato");
		}
		
		//Avvio dello script per l'irrigazione
		this.run();
		
	}
	
	public void startIrrigation() {
		/*
		 * Manca la parte dell'avvio dell'irrigazione
		 */
		
		//this.irrigationTime=time;
		////
		this.stato=this.stateOn;
		Timestamp tm= new Timestamp(System.currentTimeMillis());
		if(this.logClient!=null) {
			//Connesione al server mqtt
			this.logClient.connect(1883, Utilities.ipMqtt, s -> {	
				
				System.out.println(tm+": Avvio irrigazione.");
				this.logClient.publish("Irrigation-LOG", Buffer.buffer(tm+"-IrrigazioneLogGateway: "+this.stateOn),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);		
				this.logClient.disconnect();//Per evitare problemi relativi alla connessione
		    });		
		}else {
			System.out.println("Client mqtt per il log non instanziato");
		}
		
		//AGGIUNGERLO DOPO AVER CREATO LO SCRIPT DI AVVIO
		//Avvio dello script per l'irrigazione
		//this.run();
		//Aggiungere lo script di irrigazione al quale va aggiunto 
		Process process;
		try {
			process = Runtime.getRuntime().exec(this.scriptPathIrrigation+" on");
			int processOutput=process.waitFor();
			
			//TESTING 
			if(ConfigurationController.testingIrrigazione)
				processOutput=0;
			
			//Controlliamo se il processo e' stato eseguito correttamente
			if(processOutput==0) {
				System.out.println("IrrigationControllerLOG: processo eseguito con successo!");
				//Se il processo e' stato eseguito con successo bisogna comunicarlo al middlelayer
				if(this.logClient!=null) {
					//Connesione al server mqtt
					this.logClient.connect(1883, Utilities.ipMqtt, s -> {	
						
						System.out.println("Comunicazione esito positivo del processo...");
						this.logClient.publish("Irrigation-LOG", Buffer.buffer(tm+"-IrrigazioneLogGateway: "+this.stateOn),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);		
						this.logClient.disconnect();//Per evitare problemi relativi alla connessione
				    });		
				}else {
					System.out.println("Client mqtt per il log non instanziato");
				}
			}else {
				
				System.out.println("IrrigationControllerLOG: processo NON ESEUGITO CON SUCCESSO");
				//Se il processo e' stato eseguito con successo bisogna comunicarlo al middlelayer
				if(this.logClient!=null) {
					//Connesione al server mqtt
					this.logClient.connect(1883, Utilities.ipMqtt, s -> {	
						
						System.out.println("Comunicazione esito NEGATIVO del processo...");
						this.logClient.publish("Irrigation-LOG", Buffer.buffer(tm+"-IrrigazioneLogGateway: ERROR."),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);		
						this.logClient.disconnect();//Per evitare problemi relativi alla connessione
				    });		
				}else {
					System.out.println("Client mqtt per il log non instanziato");
				}
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
		
	}
	
	public void run() {
		/*
		 * Attendo la fine dell'irrigazione e la blocco riportando lo stato ad off
		 */
		try {
			/*
			 *Avvio lo script per l'irrigazione che una volta finito restituirà i 
			 */
			
			//Aggiungere lo script di irrigazione al quale va aggiunto 
			Process process= Runtime.getRuntime().exec(this.scriptPathIrrigation+" on");
			int processOutput=process.waitFor();
			
			/*
			 * Se tutto è andato bene
			 */
			this.stopIrrigation();
			
			
		} catch (InterruptedException e) {
			System.out.print("ERRORE RUN IRRIGATION CONTROLLER:");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.print("ERRORE RUN IRRIGATION: errore avvio script di irrigazione.");
			e.printStackTrace();
		}
	}
	
	
	
	public void stopIrrigation() {
		
		////
		this.stato=this.stateOff;
		Timestamp tm= new Timestamp(System.currentTimeMillis());
		if(this.logClient!=null) {
			//Connesione al server mqtt
			this.logClient.connect(1883, Utilities.ipMqtt, s -> {	
				
				System.out.println(tm+": Stop irrigazione.");
				this.logClient.publish("Irrigation-LOG", Buffer.buffer(tm+"-IrrigazioneLogGateway: "+this.stateOff),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);		
				this.logClient.disconnect();//Per evitare problemi relativi alla connessione
		    });
		
		}else {
			System.out.println("Client mqtt per il log non instanziato");
		}
		
		//FERMARE IRRIGAZIONE CON SCRIPT
		Process process;
		try {
			process = Runtime.getRuntime().exec(this.scriptPathIrrigation+" off");
			int processOutput=process.waitFor();
			
			//TESTING 
			if(ConfigurationController.testingIrrigazione)
				processOutput=0;
			
			//Controlliamo se il processo e' stato eseguito correttamente
			if(processOutput==0) {
				System.out.println("IrrigationControllerLOG: processo eseguito con successo!");
				//Se il processo e' stato eseguito con successo bisogna comunicarlo al middlelayer
				if(this.logClient!=null) {
					//Connesione al server mqtt
					this.logClient.connect(1883, Utilities.ipMqtt, s -> {	
						
						System.out.println("Comunicazione esito positivo del processo...");
						this.logClient.publish("Irrigation-LOG", Buffer.buffer(tm+"-IrrigazioneLogGateway: "+this.stateOff),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);		
						this.logClient.disconnect();//Per evitare problemi relativi alla connessione
				    });		
				}else {
					System.out.println("Client mqtt per il log non instanziato");
				}
			}else {
				
				System.out.println("IrrigationControllerLOG: processo NON ESEUGITO CON SUCCESSO");
				//Se il processo e' stato eseguito con successo bisogna comunicarlo al middlelayer
				if(this.logClient!=null) {
					//Connesione al server mqtt
					this.logClient.connect(1883, Utilities.ipMqtt, s -> {	
						
						System.out.println("Comunicazione esito NEGATIVO del processo...");
						this.logClient.publish("Irrigation-LOG", Buffer.buffer(tm+"-IrrigazioneLogGateway: ERROR."),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);		
						this.logClient.disconnect();//Per evitare problemi relativi alla connessione
				    });		
				}else {
					System.out.println("Client mqtt per il log non instanziato");
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public String getStato() {
		return this.stato;
	}


	public MqttClient getLogClient() {
		return logClient;
	}


	public void setLogClient(MqttClient logClient) {
		this.logClient = logClient;
	}


	public MqttClient getCommandClient() {
		return commandClient;
	}


	public void setCommandClient(MqttClient commandClient) {
		this.commandClient = commandClient;
	}
	

}
