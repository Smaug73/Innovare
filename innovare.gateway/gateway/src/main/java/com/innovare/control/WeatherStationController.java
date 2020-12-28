package com.innovare.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.Sample;
import com.innovare.utils.Utilities;


import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;


/*
 * Periodicamente genera nuovi sample dalla weatherstation
 * Deve essere accessibile in mutua escluzione dai thread Channel, anche se eseguono solo una operazione di lettura
 * potrebbero accedere alla risorsa mentre sta aggiornando i dati al suon interno
 */
public class WeatherStationController extends Thread{
	private HashMap<String,Float> channels;
	private HashMap<String,Sample> channelsSample;
	private HashSet<String> channelsNames;
	private String campionamento;
	private MqttClient mqttClient;
	
	private long tempoCampionamento;
	
	public WeatherStationController(long tempoCampionamento, Vertx vertx) {
		
		channels= new HashMap<String,Float>();
		channelsSample= new HashMap<String,Sample>();
		channelsNames= new HashSet<String>();
		this.tempoCampionamento=tempoCampionamento;
		
		//Creo il client mqtt
		this.mqttClient= MqttClient.create(vertx);
		
		//Creo l'hash set dei nomi dei vari canali
		for(int i=0; i< Utilities.channelsNames.length; i++) {
			channelsNames.add(Utilities.channelsNames[i]);
		}
		
		//debug
		System.out.println("\nDEBUG HASHSET");
		for(int i=0;i<channelsNames.size();i++)
			System.out.println(channelsNames.toArray()[i]);
		////////////
	}
	
	/*
	 * All'interno del metodo run andiamo ad aggiornare i valori e creare nuovi sample
	 */
	public void run() {
		while(true) {
			//Avvio l'aggiornamento dei valori tramite uno dei due metodi
			this.campionamentoFromFile();//TEST/////////////////
			//this.campionamentoFromProcess(); /////VERO
			
			//Dopo il campionamento inviamo tramite mqtt i dati
			this.mqttClient.connect(1883, Utilities.ipMiddleLayer, s -> {	
					
					ArrayList<Sample> newSamples= new ArrayList<Sample>();
				
					for(int i=0; i<Utilities.channelsNames.length; i++) {
						try {
							if(this.getDato(Utilities.channelsNames[i])!=null)
								newSamples.add(this.getDato(Utilities.channelsNames[i]));
							else {
								System.out.println("VOLERE DA INVIARE NULL, PROBLEMI AL CANALE:"+i);
								newSamples.add(new Sample());
							}
							
						} catch (Exception e) {
							System.err.println(e.getMessage());
							e.printStackTrace();
						}
						/*
						//DEBUG
						try {
							System.out.println("Nuovo Sample: "+new ObjectMapper().writeValueAsString(this.getDato(Utilities.channelsNames[i])));
							if(this.getDato(Utilities.channelsNames[i])!=null)
								this.mqttClient.publish(""+i,
										 //Invio dell'array contenente le misure
										  Buffer.buffer(new ObjectMapper().writeValueAsString(this.getDato(Utilities.channelsNames[i]))),
										  MqttQoS.AT_LEAST_ONCE,
										  false,
										  false);	
							else
								System.out.println("VOLERE DA INVIARE NULL, PROBLEMI AL CANALE:"+i);
						} catch (Exception e) {
							//errore non esiste il canale
							System.err.println(e.getMessage());
							e.printStackTrace();
						}*/						
					} 
					
					try {
						this.mqttClient.publish("weatherStation",
								 //Invio dell'array contenente le misure
								  Buffer.buffer(new ObjectMapper().writeValueAsString(newSamples)),
								  MqttQoS.AT_LEAST_ONCE,
								  false,
								  false);
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
					
					//Il client si disconnette dopo aver inviato il messaggio.-NECESSARIO PER EVITARE BUG SULLA RICONNESSIONE-
					this.mqttClient.disconnect();
					
		    });
			try {
				this.sleep(tempoCampionamento);
			} catch (InterruptedException e) {
				System.err.println("Errore nella sleep del thread WeatherStationController");
				e.printStackTrace();
			}
		}
		
		
		
	}
	
	
	
	//Aggiorna i dati della weatherstation da un file METODO TESTTTTT
	public synchronized void campionamentoFromFile() {
		File f= new File(Utilities.scriptWeatherPath+"test.txt");
		//Leggiamo riga per riga il file e aggiungiamo solo quando troviamo un sensore di quelli di interesse
		try {
			//Leggiamo tutto il file
			String fileString="";
			Scanner sc = new Scanner(f);
			while(sc.hasNext()) {
				fileString=fileString+sc.nextLine()+"\n";
			}
			sc.close();
			
			//Letto tutto il file facciamo uno split
			//System.out.println("\nFILE:");
			//System.out.println(fileString);
			String reg="[ = \n]+";
			String[] token=fileString.split(reg);
			
			//debug
			//System.out.println("\nDEBUG SPLIT STRING");
			//for(int i=0;i<token.length;i++)
			//	System.out.println(token[i]);
			////////////
			
			//Generazione time stamp per il sample appena catturati
			long timestamp= System.currentTimeMillis();
			//Leggiamo per ogni canale il valore corrispondente
			//Avanziamo di due posizioni alla volta ed ogni volta che leggiamo un channel che e' presente lo aggiungo
			for(int j=0; j<token.length;j=j+1) {
				//System.out.println("Token: "+token[j]);
				if(this.channelsNames.contains(token[j])) {
					try {
						//System.out.println("SI!");
						//this.channels.put(token[j], Float.valueOf(token[j+1]));
						this.channelsSample.put(token[j], new Sample(timestamp,token[j],Float.valueOf(token[j+1])));
					}
					catch(NumberFormatException n) {
						System.err.println("Errore conversione numero, sara' aggiunto null");
						//this.channels.put(token[j], null);
						this.channelsSample.put(token[j], null);
					}	
				}
				//else
					//System.out.println("NO");
			}
			
			//DEBUG
			//System.out.println("DEBUG:");
			//for(String s: this.channels.keySet())
				//System.out.println(this.channels.get(s));
			////////
			
		} catch (FileNotFoundException e) {
			System.out.println("File non trovato");
			e.printStackTrace();
		}
	}
	
	//Aggiorna i dati della weatherstation da un processo, l'aggiornamento dei dati deve avvenire in mutua esclusione
	private synchronized void campionamentoFromProcess() throws IOException {
		//Lanciamo il processo e leggiamo l'outputstream
		Process processSt= Runtime.getRuntime().exec("./vproweather -x /dev/ttyUSB0\n >> sample.txt",null,new File(Utilities.scriptPath+"vproweather-1.1"+System.getProperty("file.separator")));
		//Leggiamo l'output del processo e lo salviamo in una stringa
		InputStream is = processSt.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String output="";
		String line;
		String[] token;
		String reg="[ = \n]+";
		
		//Generazione time stamp per il sample appena catturati
		long timestamp= System.currentTimeMillis();
		
		while ((line = br.readLine()) != null) {
			//L'array generato dove essere di dimensione 2, canale[0] e valore[1]
			token=line.split(reg);
			//Se il canale letto fa parte di quelli di interesse allora lo inserisco nella hashMap
			if(this.channelsNames.contains(token[0])) {
				try {
					System.out.println("SI!");
					//this.channels.put(token[0], Float.valueOf(token[1]));
					this.channelsSample.put(token[0], new Sample(timestamp,token[0],Float.valueOf(token[1])));
				}
				catch(NumberFormatException n) {
					System.err.println("Errore conversione numero, sara' aggiunto null");
					//this.channels.put(token[0], null);
					this.channelsSample.put(token[0], null);
				}	
			}else
				System.out.println("NO");
		}
		is.close();
		isr.close();
		br.close();
		
	}
	
	//invio dati dei canali tramite mqtt
	private void mqttComunication() {
		
	}
	
	
	public synchronized Sample getDato(String channelName) throws Exception {
		if(channelsSample.containsKey(channelName))
			return channelsSample.get(channelName);
		else 
			throw new Exception("Il canale richiesto non esite");
	}
	
	public HashMap<String,Float> getMapValue(){
		return this.channels;
	}
	
	
}
