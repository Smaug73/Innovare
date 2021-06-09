package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.ChannelMeasure;
import com.innovare.model.Sample;
import com.innovare.utils.Utilities;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class ConfigurationController {

	public static boolean testingIrrigazione= false;
	public ArrayList<Integer> channelForClassification= new ArrayList<Integer>();
	
	//Il tempo di irrigazione massimo viene espresso in long  
	//ma viene prelevato dal file di configurazione in minuti
	public static long irrigationMaxTime=120000;
	
	//Array contenente ora e minuti si inizio dell'irrigazione in una giornata
	public LocalTime timeIrrigation;
	
	//Portata irrigazione
	public static float portataIrrigation=10;
	
	//Delay dalla prossima irrigazione
	public long delayIrrigation;
	
	//IP gateway, di default settato su localhost
	public static String gatewayIP="localhost";
	
	public static ArrayList<ChannelMeasure> channelMeasureArray;
	
	//Quantita' acqua standard per l'irrigazione
	public static float quantitastand;
	
	//Mappa contenente i valori di kc
	public static HashMap<Date,Double> kcMap=null;
	
	//Soglia percentuale classificazione
	public static float sogliaclassificazione=2;
	
	//id canali letti da seriale
	public static ArrayList<Integer> idSerialChannel=new ArrayList<Integer>();
	
	public ConfigurationController(){
		//verifichiamo l'esistenza del file di configurazione e lo leggiamo
		//altrimenti lasciamo i valori nel loro stato predefinito in Utilities
		File confF= new File(Utilities.configurationMiddleLayerPath);
		
		
		Logger.getLogger().print("CONFIGURATION-FILE: Lettura file di configurazione...");
		//leggiamo l'intero file
		try {
			Scanner sc= new Scanner(confF);
			while(sc.hasNextLine()) {
				String token=sc.next();
				if(!token.contains("#"))
					switch(token) {				
						case "testing-irrigazioneON" :
							this.testingIrrigazione= true;
							if(sc.hasNext())
								sc.nextLine();
							break;
							
						case "channelForClassification" :
							//Leggiamo i canali da considerare per la classificazione
							while(sc.hasNextInt()) {
								try {
									this.channelForClassification.add(sc.nextInt());
								}catch(Exception e) {
									Logger.getLogger().print("ERRORE CONFIGURATION-FILE middlelayer: errore di lettura di uno dei canali di channelForClassification");
								}
							}
							Logger.getLogger().print("CONFIGURATION-FILE middlelayer: channelForClassification : "+this.channelForClassification.toString());
							break;
							
						case "irrigationMaxTime":
							//Leggiamo il tempo in minuti e lo convertiamo in long
							try {
								this.setMaxTimeIrrigationFromInt(sc.nextInt()*60);
							}catch(Exception e) {
								Logger.getLogger().print("ERRORE CONFIGURATION-FILE middlelayer: errore lettura irrigationMaxTime , configurazione valori di base: 60 minuti");
								this.setMaxTimeIrrigationFromInt(60);
							}
							Logger.getLogger().print("CONFIGURATION-FILE middlelayer: irrigationMaxTime : "+this.irrigationMaxTime);
							sc.nextLine();
							break;
							
						case "irrigationTime":
							try {
								//Leggiamo ora e minuti di inizio dell'irrigazione
								/*
								int hour=sc.nextInt();//ora;
								int minute= sc.nextInt();//minuti
								if(hour>IrrigationController.maxHour || minute>IrrigationController.maxMinute) {
									Logger.getLogger().print("ERRORE CONFIRATION-FILE middlelayer: configurazione con valori di base: 14:00");
									this.timeIrrigation[0]= IrrigationController.defaultHour;//ora
									this.timeIrrigation[1]= IrrigationController.defaultMinute;//minuti
								}else {
									this.timeIrrigation[0]= hour;//ora
									this.timeIrrigation[1]= minute;//minuti
								}*/
								String localTimeString;
								if(sc.hasNext())
									localTimeString = sc.next();
								else throw new Exception("nessun orario definito nel file di configurazione.");
								this.timeIrrigation= LocalTime.parse(localTimeString);
								
							}catch(DateTimeException ed) {
								Logger.getLogger().print("ERRORE CONFIRATION-FILE middlelayer: "+ed.getMessage());
								Logger.getLogger().print("ERRORE CONFIRATION-FILE middlelayer: configurazione con valori di base: 14:00:00");
								this.timeIrrigation= LocalTime.of(IrrigationController.defaultHour,IrrigationController.defaultMinute,IrrigationController.defaultSecond);
								
							}
							catch(Exception e) {
								Logger.getLogger().print("ERRORE CONFIRATION-FILE middlelayer: "+e.getMessage());
								Logger.getLogger().print("ERRORE CONFIRATION-FILE middlelayer: configurazione con valori di base: 14:00:00");
								this.timeIrrigation= LocalTime.of(IrrigationController.defaultHour,IrrigationController.defaultMinute,IrrigationController.defaultSecond);
								//this.timeIrrigation[0]= IrrigationController.defaultHour;//ora
								//this.timeIrrigation[1]= IrrigationController.defaultMinute;//minuti
							}
							Logger.getLogger().print("CONFIGURATION-FILE middlelayer: irrigationTime : "+this.timeIrrigation);
							sc.nextLine();
							break;	
						
						case "irrigationPortata":
							//Leggiamo il tempo in minuti e lo convertiamo in long
							try {
								float port= sc.nextFloat();
								if(port<0) 
									throw new Exception("valore della portata non accettabile, impostazione portata di default: "+IrrigationController.defaultPortata);
								this.portataIrrigation=port;
								
							}
							catch(Exception e) {
								Logger.getLogger().print("ERRORE CONFIRATION-FILE middlelayer: lettura portata "+e.getMessage());
								this.portataIrrigation= IrrigationController.defaultPortata;
							}
							Logger.getLogger().print("CONFIGURATION-FILE middlelayer: irrigationPortata : "+this.portataIrrigation);
							sc.nextLine();
							break;
						
						case "irrigationDelay":
							//Leggiamo il tempo in minuti e lo convertiamo in long
							try {
								int delaySet= sc.nextInt();
								
								// test case
								if(delaySet==1) {
									this.delayIrrigation= IrrigationController.delayDay;
									Logger.getLogger().print("CONFIGURATION-FILE middlelayer: delayIrrigation : "+this.delayIrrigation);
									break;
								}else
									this.setDelayFromNextIrrigationFromInt(sc.nextInt());
								
							}catch(Exception e) {
								Logger.getLogger().print("ERRORE CONFIGURATION-FILE middlelayer: errore lettura irrigationDelay , configurazione valori di base: "+IrrigationController.delayOneMinutetTest);
								this.delayIrrigation= IrrigationController.delayOneMinutetTest;
							}
							Logger.getLogger().print("CONFIGURATION-FILE middlelayer: delayIrrigation : "+this.delayIrrigation);
							sc.nextLine();
							break;
							
						case "gatewayIP":
							//Leggiamo il tempo in minuti e lo convertiamo in long
							try {
								if(sc.hasNext())
									this.gatewayIP = sc.next();
								else throw new Exception("Nessun gateway Ip presente nel file di configurazione");
							}catch(Exception e) {
								Logger.getLogger().print("ERRORE CONFIGURATION-FILE middlelayer: errore lettura gateway Ip , configurazione valori di base: "+this.gatewayIP);
								
							}
							Logger.getLogger().print("CONFIGURATION-FILE middlelayer: gateway Ip : "+this.gatewayIP);
							if(sc.hasNextLine())
								sc.nextLine();
							break;
							
							
						case "ChannelMeasure":
							this.channelMeasureArray= new ArrayList<ChannelMeasure>();
							String measureChannel15= "none";
							String measureChannel16= "none";
							try{
								if(sc.hasNext())
									measureChannel15= sc.next();
								this.channelMeasureArray.add(new ChannelMeasure(15,measureChannel15));
								if(sc.hasNext())
									measureChannel16= sc.next();
								this.channelMeasureArray.add(new ChannelMeasure(16,measureChannel16));
								
							}catch(Exception e){
								Logger.getLogger().print("ERRORE CONFIGURATION-FILE middlelayer: errore lettura channelMeasure , configurazione valori di base per i channel: null");
								this.channelMeasureArray= new ArrayList<ChannelMeasure>();
								this.channelMeasureArray.add(new ChannelMeasure(15,"none"));
								this.channelMeasureArray.add(new ChannelMeasure(16,"none"));
							}
							Logger.getLogger().print("CONFIGURATION-FILE middlelayer: ChannelMeasure : "+this.channelMeasureArray.toString());
							
							break;
							
						case "quantStandard":
							try {
								this.quantitastand= sc.nextFloat();
								
							}catch(Exception e) {
								Logger.getLogger().print("ERRORE CONFIGURATION-FILE middlelayer: errore lettura quantStandard , configurazione valori di base: 0");
								this.quantitastand=0;
							}
							Logger.getLogger().print("CONFIGURATION-FILE middlelayer: quantStandard : "+this.quantitastand);
							if(sc.hasNextLine())
								sc.nextLine();
						break;
						
						case "sogliaClassificazione":
							try {
								this.sogliaclassificazione= sc.nextFloat();
								
							}catch(Exception e) {
								Logger.getLogger().print("ERRORE CONFIGURATION-FILE middlelayer: errore lettura sogliaClassificazione , configurazione valori di base: 2");
								this.sogliaclassificazione=0;
							}
							Logger.getLogger().print("CONFIGURATION-FILE middlelayer: sogliaClassificazione : "+this.sogliaclassificazione);
							if(sc.hasNextLine())
								sc.nextLine();
						break;
						
						
						case "serialChannel" :
							//Leggiamo i canali da considerare per la classificazione
							while(sc.hasNextInt()) {
								try {
									this.idSerialChannel.add(sc.nextInt());
								}catch(Exception e) {
									Logger.getLogger().print("ERRORE CONFIGURATION-FILE middlelayer: errore di lettura di uno dei canali di serialChannel");
								}
							}
						Logger.getLogger().print("CONFIGURATION-FILE middlelayer: serialChannel : "+this.idSerialChannel.toString());
						break;
						
						
						
						
							
						default:
							if(sc.hasNextLine())
								sc.nextLine();
					}	
				else
					if(sc.hasNext())
						sc.nextLine();
			}
			sc.close();

		} catch (FileNotFoundException e) {
			Logger.getLogger().print("ERRORE LETTURA FILE CONFIGURAZIONE: file non presente.");
			Logger.getLogger().print("Caricamento impostazioni predefinite..");
			
		}catch(Exception e) {
			Logger.getLogger().print("ERRORE lettura generico.."+e.getMessage());
			//e.printStackTrace();
		}
		
	}
	
	
	public void kcFileRead() {
		Logger.getLogger().print("Lettura valori KC dal file kc.csv ..");
		//Apriamo il file csv contenente i dati sul kc per ogni data
		try {
			//leggiamo il file csv
			CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
			CSVReader reader = new CSVReaderBuilder(new FileReader(Utilities.kcFilePath))
					.withCSVParser(csvParser)
					.withSkipLines(1)
					.build();	
			List<String[]> r = reader.readAll();
			
			this.kcMap= new HashMap<Date,Double>();
			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy");
			
			r.forEach( x -> {
				Logger.getLogger().print(Arrays.toString(x));
				try {
					//Logger.getLogger().print("DEBUG :"+x[0]);
					//Logger.getLogger().print("DEBUG Sdf:"+df.toPattern());
					Date d= df.parse(x[0]);
					Double kc = Double.parseDouble(x[1]);
					//Logger.getLogger().print("DEBUG  data: "+d.toString()+" kc:"+kc);
					//Aggiungiamola alla mappa
					kcMap.put(d, kc);
					
				} catch (ParseException e) {
					Logger.getLogger().print("Errore lettura Data");
					e.printStackTrace();
				}
				
			});
		}catch(Exception e) {
			Logger.getLogger().print("Errore durante lettura file KC");
			e.printStackTrace();
		}
	}
	

	
	
	public long getIrrigationMaxTime() {
		return irrigationMaxTime;
	}




	public void setIrrigationMaxTime(long irrigationMaxTime) {
		this.irrigationMaxTime = irrigationMaxTime;
	}


	public LocalTime getTimeIrrigation() {
		return timeIrrigation;
	}



	public void setTimeIrrigation(LocalTime timeIrrigation) {
		this.timeIrrigation = timeIrrigation;
	}



	public static boolean isTestingIrrigazione() {
		return testingIrrigazione;
	}

	public ArrayList<Integer> getChannelForClassification() {
		return channelForClassification;
	}


	private void setMaxTimeIrrigationFromInt(int timeI) {
		this.irrigationMaxTime= Long.parseLong(Integer.toString(timeI*1000));
	}
	
	private void setDelayFromNextIrrigationFromInt(int timeI) {
		this.delayIrrigation= Long.parseLong(Integer.toString(timeI*1000));
	}



	public float getPortataIrrigation() {
		return portataIrrigation;
	}



	public void setPortataIrrigation(float portataIrrigation) {
		this.portataIrrigation = portataIrrigation;
	}



	public long getDelayIrrigation() {
		return delayIrrigation;
	}



	public void setDelayIrrigation(long delayIrrigation) {
		this.delayIrrigation = delayIrrigation;
	}
	
	
	
}
