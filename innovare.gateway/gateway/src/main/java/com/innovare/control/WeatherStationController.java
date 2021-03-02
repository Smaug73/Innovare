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

import io.netty.handler.codec.http2.Http2FrameLogger.Direction;
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
	private long timestamp;//Istante ultimo campionamento
	
	private long tempoCampionamento;
	
	//DEBUG
	public WeatherStationController() {
		channels= new HashMap<String,Float>();
		channelsSample= new HashMap<String,Sample>();
		channelsNames= new HashSet<String>();
		//Creo l'hash set dei nomi dei vari canali
				for(int i=0; i< Utilities.channelsNames.length; i++) {
					channelsNames.add(Utilities.channelsNames[i]);
				}
	}
	
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
		//System.out.println("\nDEBUG HASHSET");
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
			//this.campionamentoFromFile();//TEST/////////////////
			try {
				this.campionamentoFromProcess();
				
				
				//Dopo il campionamento inviamo tramite mqtt i dati
				this.mqttClient.connect(1883, ConfigurationController.ipMiddleLayer, s -> {	
						
						ArrayList<Sample> newSamples= new ArrayList<Sample>();
					
						for(int i=0; i<Utilities.channelsNames.length; i++) {
							try {
								
								newSamples.add(this.getDato(Utilities.channelsNames[i]));
								
								
							} catch (Exception e) {
								System.err.println(e.getMessage());
								//e.printStackTrace();
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
						System.out.println("LOG-WEATHERSTATION-CHANNEL: invio");
						
						try {
							System.out.println(new ObjectMapper().writeValueAsString(newSamples));
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
				
				
			} catch (IOException e1) {
				
				System.err.println("ERRORE AVVIO PROCESSO LETTURA WEATHERSTATION..");
				e1.printStackTrace();
			}
			//this.campionamentoFromProcess(); /////VERO
			
			
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
			this.timestamp= System.currentTimeMillis();
			//Leggiamo per ogni canale il valore corrispondente
			//Avanziamo di due posizioni alla volta ed ogni volta che leggiamo un channel che e' presente lo aggiungo
			for(int j=0; j<token.length;j=j+1) {
				System.out.println("Token: "+token[j]);
				if(this.channelsNames.contains(token[j])) {
					try {
						System.out.println("SI!");
						//this.channels.put(token[j], Float.valueOf(token[j+1]));
						this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(token[j+1])));
					}
					catch(NumberFormatException n) {
						System.err.println("Errore conversione numero, sara' aggiunto null");
						//this.channels.put(token[j], null);
						//Inseriamo un Sample con canale corretto e valore 0
						if(token[j].equalsIgnoreCase("rtWindDirRose")) {
							switch(token[j+1]) {
								case "N": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.N.getNum())));
									break;
								case "NNW": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.NNW.getNum())));
								  	break;
								case "NNE": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.NNE.getNum())));
								  	break;
								case "NW": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.NW.getNum())));
								  	break;
								case "NE": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.NE.getNum())));
								  	break;
								case "WNW": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.WNW.getNum())));
								  	break;
								case "WSW": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.WSW.getNum())));
								  	break;
								case "E": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.E.getNum())));
								  	break;
								case "ENE": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.ENE.getNum())));
								  	break;
								case "ESE": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.ESE.getNum())));
								  	break;
								case "S": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.S.getNum())));
								  	break;
								case "SSE": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.SSE.getNum())));
								  	break;
								case "SSW": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.SSW.getNum())));
								  	break;
								case "SW": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.SW.getNum())));
								  	break;
								case "SE": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.SE.getNum())));
								  	break;	
								default: 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],0));
								  	break;
							}	
						}
						
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
	/*
	public synchronized void campionamentoFromFileFromProcess() {
		try {
			//Lanciamo il processo e salviamo nel file sample.txt
			Process processSt= Runtime.getRuntime().exec("./vproweather -x /dev/ttyUSB0 >> sample.txt",null,new File(Utilities.scriptWeatherPath));
			//attendiamo file del processo
			int processSegOutput=processSt.waitFor();
			//leggiamo il file prodotto
			File f= new File(Utilities.scriptWeatherPath+"sample.txt");
			//Leggiamo tutto il file
			String fileString="";
			Scanner sc = new Scanner(f);
			while(sc.hasNext()) {
				fileString=fileString+sc.nextLine()+"\n";
			}
			sc.close();
			
			//Letto tutto il file facciamo uno split
			System.out.println("\nFILE:");//debug
			System.out.println(fileString);//debug
			String reg="[ = \n]+";
			String[] token=fileString.split(reg);
			
			//debug
			System.out.println("\nDEBUG SPLIT STRING");//debug
			for(int i=0;i<token.length;i++)//debug
				System.out.println(token[i]);//debug
			////////////
			
			//Generazione time stamp per il sample appena catturati
			this.timestamp= System.currentTimeMillis();
			//Leggiamo per ogni canale il valore corrispondente
			//Avanziamo di due posizioni alla volta ed ogni volta che leggiamo un channel che e' presente lo aggiungo
			for(int j=0; j<token.length;j=j+1) {
				System.out.println("Token: "+token[j]);
				if(this.channelsNames.contains(token[j])) {
					try {
						System.out.println("SI!");
						//this.channels.put(token[j], Float.valueOf(token[j+1]));
						this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(token[j+1])));
					}
					catch(NumberFormatException n) {
						System.err.println("Errore conversione numero, sara' aggiunto null");
						//this.channels.put(token[j], null);
						//Inseriamo un Sample con canale corretto e valore 0
						if(token[j].equalsIgnoreCase("rtWindDirRose")) {
							switch(token[j+1]) {
								case "N": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.N.getNum())));
									break;
								case "NNW": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.NNW.getNum())));
								  	break;
								case "NNE": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.NNE.getNum())));
								  	break;
								case "NW": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.NW.getNum())));
								  	break;
								case "NE": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.NE.getNum())));
								  	break;
								case "WNW": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.WNW.getNum())));
								  	break;
								case "WSW": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.WSW.getNum())));
								  	break;
								case "E": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.E.getNum())));
								  	break;
								case "ENE": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.ENE.getNum())));
								  	break;
								case "ESE": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.ESE.getNum())));
								  	break;
								case "S": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.S.getNum())));
								  	break;
								case "SSE": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.SSE.getNum())));
								  	break;
								case "SSW": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.SSW.getNum())));
								  	break;
								case "SW": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.SW.getNum())));
								  	break;
								case "SE": 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],Float.valueOf(com.innovare.model.Direction.SE.getNum())));
								  	break;	
								default: 
									this.channelsSample.put(token[j], new Sample(this.timestamp,token[j],0));
								  	break;
							}	
						}
						
					}	
				}
			}
		
			//Eliminiamo il file prodotto
			sc.close();
			this.deleteAllFiles(f);
		
		
		}catch (FileNotFoundException e) {
				System.out.println("File non trovato");
				e.printStackTrace();
			
		} catch (IOException e) {
			//Eliminiamo il file prodotto
			this.deleteAllFiles(new File(Utilities.scriptWeatherPath+"sample.txt"));
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	*/
	
	
	
	
	//Aggiorna i dati della weatherstation da un processo, l'aggiornamento dei dati deve avvenire in mutua esclusione
	public synchronized void campionamentoFromProcess() throws IOException {
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
		
		//Generazione time stamp per i sample appena catturati
		long timestamp= System.currentTimeMillis();
		
		while ((line = br.readLine()) != null) {
			line=line+"\n";
			//System.out.println("debug--line: "+line);
			//L'array generato dove essere di dimensione 2, canale[0] e valore[1]
			token=line.split(reg);
			//System.out.println("debug--token: ");
			//for(String s: token) {
			//	System.out.println(s);			
			//}
			
			//Se il canale letto fa parte di quelli di interesse allora lo inserisco nella hashMap
			if(this.channelsNames.contains(token[0])) {
				try {
					//System.out.println("SI!"+"\n");
					//DEBUG
					System.out.println("token0<"+token[0]+"> token1<"+token[1]+">\n");
					/*
					//Controllo se il canale letto e' rtInsideTemp o rtOutsideTemp, va convertita la temperatura
					if(token[0].equalsIgnoreCase("rtOutsideTemp") || token[0].equalsIgnoreCase("rtInsideTemp"))
						//Controlliamo che sia stato letto corretamente dal programma 
						//Come documentato a pag 31  VantageSeriaProtocolDocs_v261 https://drive.google.com/drive/u/0/folders/18HTT0s0AMP_H6wIpoATokqDbR2JfiNNL
						if(!token[1].equalsIgnoreCase("3276.7") && !token[1].equalsIgnoreCase("32767") )
							//Convertiamo il valore della temperatura in Celsius.
							this.channelsSample.put(token[0], new Sample(timestamp,token[0], (Float.valueOf(token[1])-32)*(5/9) ));
						else
							this.channelsSample.put(token[0], new Sample(timestamp,token[0]));
					else {
						System.out.println("Aggiunta normale");
						//Lo aggiungiamo con valore 
						this.channelsSample.put(token[0], new Sample(timestamp,token[0],Float.valueOf(token[1])));
					}
					*/
					
					switch(token[0]) {
					
										
					case "rtInsideTemp":
						if(!token[1].equalsIgnoreCase("3276.7") && !token[1].equalsIgnoreCase("32767") )
							//Convertiamo il valore della temperatura in Celsius.
							this.channelsSample.put(token[0], new Sample(timestamp,token[0], (Float.valueOf(token[1])-32)*(5/9) ));
						else
							//Caso errore lettura
							this.channelsSample.put(token[0], new Sample(timestamp,token[0], Float.valueOf("-273.15")));
						break;
						
					case "rtInsideHum":
						if(!token[1].equalsIgnoreCase("255") )
							//Convertiamo il valore della temperatura in Celsius.
							this.channelsSample.put(token[0], new Sample(timestamp,token[0], Float.valueOf(token[1])  ));
						else
							//Caso errore lettura
							this.channelsSample.put(token[0], new Sample(timestamp,token[0]));
						break;
						
					case "rtOutsideTemp":
						if(!token[1].equalsIgnoreCase("3276.7") && !token[1].equalsIgnoreCase("32767") )
							//Convertiamo il valore della temperatura in Celsius.
							this.channelsSample.put(token[0], new Sample(timestamp,token[0], (Float.valueOf(token[1])-32)*(5/9) ));
						else
							//Caso errore lettura
							this.channelsSample.put(token[0], new Sample(timestamp,token[0], Float.valueOf("-273.15")));
						break;
						
						
					case "rtWindSpeed":
						if(!token[1].equalsIgnoreCase("255") )
							//Convertiamo il valore della temperatura in Celsius.
							this.channelsSample.put(token[0], new Sample(timestamp,token[0], Float.valueOf(token[1])  ));
						else
							//Caso errore lettura
							this.channelsSample.put(token[0], new Sample(timestamp,token[0]));
						break;
						
					case "rtWindAvgSpeed":
						if(!token[1].equalsIgnoreCase("255") )
							//Convertiamo il valore della temperatura in Celsius.
							this.channelsSample.put(token[0], new Sample(timestamp,token[0], Float.valueOf(token[1])  ));
						else
							//Caso errore lettura
							this.channelsSample.put(token[0], new Sample(timestamp,token[0]));
						break;
						
					case "rtWindDir":
						if(!token[1].equalsIgnoreCase("32767") )
							//Convertiamo il valore della temperatura in Celsius.
							this.channelsSample.put(token[0], new Sample(timestamp,token[0], Float.valueOf(token[1])  ));
						else
							//Caso errore lettura
							this.channelsSample.put(token[0], new Sample(timestamp,token[0]));
						break;
						
					case "rtOutsideHum":
						if(!token[1].equalsIgnoreCase("255") )
							//Convertiamo il valore della temperatura in Celsius.
							this.channelsSample.put(token[0], new Sample(timestamp,token[0], Float.valueOf(token[1])  ));
						else
							//Caso errore lettura
							this.channelsSample.put(token[0], new Sample(timestamp,token[0]));
						break;
						
					case "rtRainRate":
						if(!token[1].equalsIgnoreCase("655.35") )
							//Convertiamo il valore della temperatura in Celsius.
							this.channelsSample.put(token[0], new Sample(timestamp,token[0], Float.valueOf(token[1])  ));
						else
							//Caso errore lettura
							this.channelsSample.put(token[0], new Sample(timestamp,token[0]));
						break;
					
					default:
						this.channelsSample.put(token[0], new Sample(timestamp,token[0], Float.valueOf(token[1])  ));
						
					}
					
		
				}
				catch(NumberFormatException n) {
					//System.err.println("Errore conversione numero, sara' aggiunto null"+"\n");
					//this.channels.put(token[0], null);
					//CONTROLLIAMO CASO DELLA MISURA DI WIND-DIR-ROSE
					if(!token[0].equalsIgnoreCase("rtWindDirRose"))
						System.out.println("WEATHER STATION ERROR--- Errore lettura :"+token[0]);
						//this.channelsSample.put(token[0], new Sample(timestamp,token[0]));
					else {
						switch(token[1]) {
						case "N": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.N.getNum())));
							break;
						case "NNW": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.NNW.getNum())));
						  	break;
						case "NNE": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.NNE.getNum())));
						  	break;
						case "NW": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.NW.getNum())));
						  	break;
						case "NE": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.NE.getNum())));
						  	break;
						case "WNW": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.WNW.getNum())));
						  	break;
						case "WSW": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.WSW.getNum())));
						  	break;
						case "E": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.E.getNum())));
						  	break;
						case "ENE": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.ENE.getNum())));
						  	break;
						case "ESE": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.ESE.getNum())));
						  	break;
						case "S": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.S.getNum())));
						  	break;
						case "SSE": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.SSE.getNum())));
						  	break;
						case "SSW": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.SSW.getNum())));
						  	break;
						case "SW": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.SW.getNum())));
						  	break;
						case "SE": 
							this.channelsSample.put(token[0], new Sample(this.timestamp,token[0],Float.valueOf(com.innovare.model.Direction.SE.getNum())));
						  	break;	
						default: 
							this.channelsSample.put(token[0], new Sample(timestamp,token[0]));
						  	break;
					}
				}	
			}
		}else {
			//System.out.println("NO"+"\n");
		}
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
			throw new Exception("Il canale "+channelName+" richiesto non esite");
	}
	
	public HashMap<String,Float> getMapValue(){
		return this.channels;
	}
	
	private void deleteAllFiles(File f) {
		//Rimozione ricorsiva
		if(f.isDirectory()) {
			File[] childF= f.listFiles();
			for(File child: childF)
				deleteAllFiles(child);
		}
		else
			f.delete();
	}

	
	
	public HashMap<String, Float> getChannels() {
		return channels;
	}

	public HashMap<String, Sample> getChannelsSample() {
		return channelsSample;
	}

	public HashSet<String> getChannelsNames() {
		return channelsNames;
	}

	public String getCampionamento() {
		return campionamento;
	}

	public MqttClient getMqttClient() {
		return mqttClient;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getTempoCampionamento() {
		return tempoCampionamento;
	}
	
	
	
}
