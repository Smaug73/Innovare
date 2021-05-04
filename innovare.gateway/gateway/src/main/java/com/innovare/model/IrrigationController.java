package com.innovare.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.control.ConfigurationController;
import com.innovare.gateway.GatewayVerticle;
import com.innovare.utils.Utilities;

import afu.org.checkerframework.checker.units.qual.Time;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
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
	
	private Irrigazione irrigazione;
	
	//Ci serve per inserire il timer nell'operazione di irrigazione
	private Vertx vertx;
	
	
	private ConfigurationController confContr;
	
	public static final String scriptPathIrrigation="python3 "+System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareData"+System.getProperty("file.separator")+"InnovareScript"+System.getProperty("file.separator")+"irrigationCommand.py";

	
	private long irrigationTime; 
	
	/*
	 * Si potrebbe controllare il reale stato dell'irrigazione nel momento della
	 * instanziazione per settare lo stato di conseguenza, in modo di non avere 
	 * eventuali incongruenze sul campo.
	 */
	public IrrigationController(ConfigurationController confContr, Vertx v) {
		this.confContr= confContr;
		this.vertx= v;
		this.settingMqttClient();
	}
	
	private void settingMqttClient() {
		
		this.commandClient= MqttClient.create(vertx);
		this.commandClient.connect(1883, ConfigurationController.ipMiddleLayer, p -> {
	    	System.out.println("IRRIGATION-MQTT: Client mqtt per i comandi dell'irrigazione connesso..");
	    	/*
		     *Il client mqtt per la ricezione di comandi si iscriverà al relativo topic 
		     */
		    this.commandClient.publishHandler(c ->{
		    	System.out.println("IRRIGATION-MQTT: Comando ricevuto: "+c.payload().toString());
		    	String comando=c.payload().toString();
		    	//long time=Long.parseLong(comando.substring(18));
		    	
		    	//System.out.println("Comando ricevuto: "+c.payload().toString());
		    	//System.out.println("Tempo di irrigazione: "+time);
		    	
		    	//Impostiamo in base al comando
		    	if(comando.equalsIgnoreCase(IrrigationController.stateOn)) {
		    		Future<Boolean> resultS=startIrrigation();
		    		resultS.onComplete(h->{
		    			if(h.succeeded())
		    				this.startResponseMqtt(true);
		    			else
		    				this.startResponseMqtt(false);
		    		});
		    	}
		    	else
		    		if(comando.equalsIgnoreCase(IrrigationController.stateOff)) {
		    			Future<Boolean> resultF=stopIrrigation();
		    			resultF.onComplete(t->{
			    			if(t.succeeded())
			    				this.startResponseMqtt(true);
			    			else
			    				this.startResponseMqtt(false);
		    			});
		    		}
		    	else
		    		//Caso nel quale e' una irrigazione programmata
		    		if(comando.contains("inizioIrrig")) {
		    			
		    			try {
		    				//Deparsiamo dal Json l'irrigazione
							this.irrigazione = new ObjectMapper().readValue(comando, Irrigazione.class);
							
							//avviamo l'irrigazione
							this.startIrrigation(this.irrigazione.getFineIrrig()-this.irrigazione.getInizioIrrig());
							
						} catch (JsonMappingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JsonProcessingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    			
		    		}
		    })
		    .subscribe("Irrigation-COMMAND", 2);	    
		});
	    
	    this.logClient=MqttClient.create(vertx);
	    System.out.println("Client mqtt per il log dell'irrigazione creato..");
		
		
	}
	
	
	/*
	 * Metodo per l'avvio dell'irrigazione
	 * Una volta avviata l'irrigazione passiamo allo script il tempo di irrigazione
	 * Si fer
	 */
	public void startIrrigation(long time) {
		
		this.irrigationTime=time;
		////
		Timestamp tm= new Timestamp(System.currentTimeMillis());
		Future<Boolean> startIrrigationFuture= startIrrigation();
		//Se si e' avviata con successo l'irrigazione
		startIrrigationFuture.onSuccess(r->{
			
			//Se il processo e' stato eseguito con successo bisogna comunicarlo al middlelayer
			if(this.logClient!=null) {
				//Connesione al server mqtt
				this.logClient.connect(1883, this.confContr.getIpMiddleLayer(), s -> {	
					
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
			
			//Comunichiamo l'esito anche sul canale COMMAND
			if(this.commandClient.isConnected())
				this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("DONE"),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);
			else
				this.commandClient.connect(1883, this.confContr.getIpMiddleLayer(), c->{
					this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("DONE"),
							  MqttQoS.AT_LEAST_ONCE,
							  false,
							  false);
				}	
			);
			
			
			//Attendiamo fino a fermare l'irrigazione
			vertx.setTimer(time, h->{
				
				
				
				//Dopodiche' invochiamo lo stop dell'irrigazione
				Future<Boolean> stopIrr=stopIrrigation();
				stopIrr.onSuccess(stS->{
					System.out.println(new Timestamp(System.currentTimeMillis()).toString()+"  IrrigationController: irrigazione correttamente terminata.");
					
					//Avvertiamo il canale COMMAND della corretta fine del processo di irrigazione==========================================================
					if(this.commandClient.isConnected())
						this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer(this.stateOff),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);
					else
						this.commandClient.connect(1883, this.confContr.getIpMiddleLayer(), s -> {	
							System.out.println("Comunicazione esito positivo del processo di irrigazione...");
							this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer(this.stateOff),
									  MqttQoS.AT_LEAST_ONCE,
									  false,
									  false);		
							//this.commandClient.disconnect();//Per evitare problemi relativi alla connessione
				    });
					
				});
				
				stopIrr.onFailure(stF->{
					System.err.println(new Timestamp(System.currentTimeMillis()).toString()+"  IrrigationController: ERRORE TERMINAZIONE IRRIGAZIONE");
					System.err.println(new Timestamp(System.currentTimeMillis()).toString()+" "+stF.getMessage());
				});
				
				
			});
		});
		
		startIrrigationFuture.onFailure(starF->{
			System.err.println(new Timestamp(System.currentTimeMillis()).toString()+"  IrrigationController: ERRORE AVVIO IRRIGAZIONE");
			
			//Se il processo e' stato eseguito con successo bisogna comunicarlo al middlelayer
			if(this.logClient!=null) {
				//Connesione al server mqtt
				this.logClient.connect(1883, this.confContr.getIpMiddleLayer(), s -> {	
					
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
			
			//Comunichiamo l'esito anche sul canale COMMAND
			if(this.commandClient.isConnected())
				this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("FAIL"),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);
			else
				this.commandClient.connect(1883, this.confContr.getIpMiddleLayer(), c->{
					this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("FAIL"),
							  MqttQoS.AT_LEAST_ONCE,
							  false,
							  false);
				});
			
		});
		
	}
	
	
	
	
	public Future<Boolean> startIrrigation() {
		
		Promise<Boolean> resultStart= Promise.promise();
		
		if(this.stato==Utilities.stateOn) {
			resultStart.fail("Already started!");
			return resultStart.future();
		}
		
		
		Timestamp tm= new Timestamp(System.currentTimeMillis());
		if(this.logClient!=null) {
			//Connesione al server mqtt
			this.logClient.connect(1883, this.confContr.getIpMiddleLayer(), s -> {	
				
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
				//Modifico stato irrigazione
				this.stato=this.stateOn;
				
				System.out.println("IrrigationControllerLOG: processo eseguito con successo!");
				/*
				//Se il processo e' stato eseguito con successo bisogna comunicarlo al middlelayer
				if(this.logClient!=null) {
					//Connesione al server mqtt
					this.logClient.connect(1883, this.confContr.getIpMiddleLayer(), s -> {	
						
						System.out.println("Comunicazione esito positivo del processo...");
						this.logClient.publish("Irrigation-LOG", Buffer.buffer(tm+"-IrrigazioneLogGateway: "+this.stateOn),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);		
						this.logClient.disconnect();//Per evitare problemi relativi alla connessione
				    });	
					
					//Comunichiamo l'esito anche sul canale COMMAND
					if(this.commandClient.isConnected())
						this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("DONE"),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);
					else
						this.commandClient.connect(1883, this.confContr.getIpMiddleLayer(), c->{
							this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("DONE"),
									  MqttQoS.AT_LEAST_ONCE,
									  false,
									  false);
						}
						
					);
				}else {
					System.out.println("Client mqtt per il log non instanziato");
				}
				*/
				resultStart.complete(true);
				
			}else {
				
				System.out.println("IrrigationControllerLOG: processo NON ESEGUITO CON SUCCESSO");
				/*
				//Se il processo e' stato eseguito con successo bisogna comunicarlo al middlelayer
				if(this.logClient!=null) {
					//Connesione al server mqtt
					this.logClient.connect(1883, this.confContr.getIpMiddleLayer(), s -> {	
						
						System.out.println("Comunicazione esito NEGATIVO del processo...");
						this.logClient.publish("Irrigation-LOG", Buffer.buffer(tm+"-IrrigazioneLogGateway: ERROR."),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);		
						this.logClient.disconnect();//Per evitare problemi relativi alla connessione
				    });	
					
					//Comunichiamo l'esito anche sul canale COMMAND
					if(this.commandClient.isConnected())
						this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("FAIL"),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);
					else
						this.commandClient.connect(1883, this.confContr.getIpMiddleLayer(), c->{
							this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("FAIL"),
									  MqttQoS.AT_LEAST_ONCE,
									  false,
									  false);
						});
					
				}else {
					System.out.println("Client mqtt per il log non instanziato");
				}
				*/
				resultStart.fail("Errore avvio processo per l'irrigazione"); 
			}
			
		} catch (IOException e) {
			resultStart.fail("Errore avvio processo per l'irrigazione");
			e.printStackTrace();
		} catch (InterruptedException e) {
			resultStart.fail("Errore avvio processo per l'irrigazione");
			e.printStackTrace();
		}
		
		return resultStart.future();
	}
	
	/*
	public void run() {
		/*
		 * Attendo la fine dell'irrigazione e la blocco riportando lo stato ad off
		 *
		try {
			/*
			 *Avvio lo script per l'irrigazione che una volta finito restituirà i 
			 *
			
			//Aggiungere lo script di irrigazione al quale va aggiunto 
			Process process= Runtime.getRuntime().exec(this.scriptPathIrrigation+" on");
			int processOutput=process.waitFor();
			
			/*
			 * Se tutto è andato bene
			 *
			this.stopIrrigation();
			
			
		} catch (InterruptedException e) {
			System.out.print("ERRORE START IRRIGAZION");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.print("ERRORE START IRRIGATION");
			e.printStackTrace();
		}
	}
	*/
	
	
	public Future<Boolean> stopIrrigation() {
		
		Promise<Boolean> resultStop= Promise.promise();
		
		if(this.stato==Utilities.stateOff) {
			resultStop.fail("Already stopped!");
			return resultStop.future();
		}
		
		Timestamp tm= new Timestamp(System.currentTimeMillis());
		if(this.logClient!=null) {
			//Connesione al server mqtt
			this.logClient.connect(1883, this.confContr.getIpMiddleLayer(), s -> {	
				
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
				
				//Modifichiamo stato
				this.stato=this.stateOff;
				
				//Se il processo e' stato eseguito con successo bisogna comunicarlo al middlelayer
				if(this.logClient!=null) {
					//Connessione al server mqtt canale 
					this.logClient.connect(1883, this.confContr.getIpMiddleLayer(), s -> {	
						
						System.out.println("Comunicazione esito positivo del processo...");
						this.logClient.publish("Irrigation-LOG", Buffer.buffer(tm+"-IrrigazioneLogGateway: "+this.stateOff),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);		
						this.logClient.disconnect();//Per evitare problemi relativi alla connessione
						
				    });
			
					
					//Comunichiamo l'esito anche sul canale COMMAND
					if(this.commandClient.isConnected())
						this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("DONE"),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);
					else
						this.commandClient.connect(1883, this.confContr.getIpMiddleLayer(), c->{
							this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("DONE"),
									  MqttQoS.AT_LEAST_ONCE,
									  false,
									  false);
						});
					
				}else {
					System.out.println("Client mqtt per il log non instanziato");
				}
				
				resultStop.complete(true);
				
			}else {
				
				System.out.println("IrrigationControllerLOG: processo NON ESEUGITO CON SUCCESSO");
				//Se il processo e' stato eseguito con successo bisogna comunicarlo al middlelayer
				if(this.logClient!=null) {
					//Connessione al server mqtt
					this.logClient.connect(1883, this.confContr.getIpMiddleLayer(), s -> {	
						
						System.out.println("Comunicazione esito NEGATIVO del processo...");
						this.logClient.publish("Irrigation-LOG", Buffer.buffer(tm+"-IrrigazioneLogGateway: ERROR."),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);		
						this.logClient.disconnect();//Per evitare problemi relativi alla connessione
				    });	
					
					//Comunichiamo l'esito anche sul canale COMMAND
					if(this.commandClient.isConnected())
						this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("FAIL"),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);
					else
						this.commandClient.connect(1883, this.confContr.getIpMiddleLayer(), c->{
							this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("FAIL"),
									  MqttQoS.AT_LEAST_ONCE,
									  false,
									  false);
						});
					
				}else {
					System.out.println("Client mqtt per il log non instanziato");
				}
				resultStop.fail("ERRORE TERMINAZIONE IRRIGAZIONE");
			}
		} catch (IOException e) {
			System.out.print("ERRORE STOP IRRIGATION");
			resultStop.fail("ERRORE TERMINAZIONE IRRIGAZIONE");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.print("ERRORE STOP IRRIGATION");
			resultStop.fail("ERRORE TERMINAZIONE IRRIGAZIONE");
			e.printStackTrace();
		}
		
		return resultStop.future();
		
	}
	
	
	
	public void startResponseMqtt(boolean result) {
		
		if(result) {
			//Connesione al server mqtt
			if(this.logClient!=null && !this.logClient.isConnected())
				this.logClient.connect(1883, this.confContr.getIpMiddleLayer(), s -> {	
					
					System.out.println("Comunicazione esito POSITIVO del processo...");
					this.logClient.publish("Irrigation-LOG", Buffer.buffer(new Timestamp(System.currentTimeMillis())+"-IrrigazioneLogGateway: "+this.stato),
							  MqttQoS.AT_LEAST_ONCE,
							  false,
							  false);		
					this.logClient.disconnect();//Per evitare problemi relativi alla connessione
			    });	
			
			//Comunichiamo l'esito anche sul canale COMMAND
			if(this.commandClient.isConnected())
				this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("DONE"),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);
			else
				this.commandClient.connect(1883, this.confContr.getIpMiddleLayer(), c->{
					this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("DONE"),
							  MqttQoS.AT_LEAST_ONCE,
							  false,
							  false);
				}
			);
			
		}else {
			//Se il processo NON e' stato eseguito con successo bisogna comunicarlo al middlelayer
			if(logClient!=null && !this.logClient.isConnected()) {
				//Connessione al server mqtt
				this.logClient.connect(1883, this.confContr.getIpMiddleLayer(), s -> {	
					
					System.out.println("Comunicazione esito NEGATIVO del processo...");
					this.logClient.publish("Irrigation-LOG", Buffer.buffer(new Timestamp(System.currentTimeMillis())+"-IrrigazioneLogGateway: ERROR."),
							  MqttQoS.AT_LEAST_ONCE,
							  false,
							  false);		
					this.logClient.disconnect();//Per evitare problemi relativi alla connessione
			    });			
			}
			
			//Comunichiamo l'esito anche sul canale COMMAND
			if(this.commandClient.isConnected())
				this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("FAIL"),
						  MqttQoS.AT_LEAST_ONCE,
						  false,
						  false);
			else
				this.commandClient.connect(1883, this.confContr.getIpMiddleLayer(), c->{
					this.commandClient.publish("Irrigation-RESPONSE", Buffer.buffer("FAIL"),
							  MqttQoS.AT_LEAST_ONCE,
							  false,
							  false);
				});
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
	
	public void setVertx(Vertx v) {
		this.vertx=v;
	}
	

}
