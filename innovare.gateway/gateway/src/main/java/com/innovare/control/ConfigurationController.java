package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;

import com.innovare.utils.Utilities;

public class ConfigurationController {

	public static String ipMiddleLayer=Utilities.ipMiddleLayer;
	public static long tempoCampionamentoWS= Utilities.tempoCampionamentoWeatherStationTest;
	public static boolean testingIrrigazione= false;
	
	//Il tempo di irrigazione massimo viene espresso in long  
	//ma viene prelevato dal file di configurazione in minuti
	public static long irrigationMaxTime=120000;
	
	public static LocalTime waetherStationTime;
	public static ArrayList<LocalTime> waetherStationTimes= new ArrayList<LocalTime>();
	
	//id canali letti da seriale
	public static ArrayList<Integer> idSerialChannel= new ArrayList<Integer>();
	
	//porta seriala utilizzata per la lettura dei sensori da seriale
	public static String serialPort = "";
	
	//id rele irrigazione automatica
	public static int releAut= 1;
	
	//id rele irrigazione manuale
	public static int releMan=2;
	
	//numero sensori
	public static int numSens=1;
	
	
	public ConfigurationController(){
		//verifichiamo l'esistenza del file di configurazione e lo leggiamo
		//altrimenti lasciamo i valori nel loro stato predefinito in Utilities
		File confF= new File(Utilities.configurationPath);
		
		//leggiamo l'intero file
		try {
			Scanner sc= new Scanner(confF);
			while(sc.hasNextLine()) {
				String token=sc.next();
				if(!token.contains("#"))
					switch(token) {
						case "ipMiddlelayer" :
							this.ipMiddleLayer=sc.next();
							if(sc.hasNext())
								sc.nextLine();
							break;
						case "numSensor" :
							if(sc.hasNextInt())
								this.numSens=sc.nextInt();
							if(sc.hasNext())
								sc.nextLine();
							break;
						case "campionamentoWS" :
							this.tempoCampionamentoWS= Long.parseLong(sc.next());
							if(sc.hasNext())
								sc.nextLine();
							break;					
						case "testing-irrigazioneON" :
							this.testingIrrigazione= true;
							if(sc.hasNext())
								sc.nextLine();
							break;
							
						case "irrigationMaxTime":
							//Leggiamo il tempo in minuti e lo convertiamo in long
							try {
								this.setMaxTimeIrrigationFromInt(sc.nextInt());
							}catch(Exception e) {
								System.err.println("ERRORE CONFIGURATION-FILE middlelayer: errore lettura irrigationMaxTime , configurazione valori di base: 60 minuti");
								this.setMaxTimeIrrigationFromInt(60);
							}
							System.out.println("CONFIGURATION-FILE middlelayer: irrigationMaxTime : "+this.irrigationMaxTime);
							sc.nextLine();
							break;
						case "weatherStationStart":
							try {
								//Leggiamo ora e minuti di inizio dell'irrigazione
								/*
								int hour=sc.nextInt();//ora;
								int minute= sc.nextInt();//minuti
								if(hour>IrrigationController.maxHour || minute>IrrigationController.maxMinute) {
									System.err.println("ERRORE CONFIRATION-FILE middlelayer: configurazione con valori di base: 14:00");
									this.timeIrrigation[0]= IrrigationController.defaultHour;//ora
									this.timeIrrigation[1]= IrrigationController.defaultMinute;//minuti
								}else {
									this.timeIrrigation[0]= hour;//ora
									this.timeIrrigation[1]= minute;//minuti
								}*/
								String linewithdata= sc.nextLine();
								Scanner scW= new Scanner(linewithdata);
								String localTimeString;
								while(scW.hasNext() ) {
									try {	
										localTimeString = scW.next();
										System.out.println(localTimeString);
										LocalTime wst= LocalTime.parse(localTimeString);
										//valori uguali non vengono aggiunti
										if(!this.findWeatherTime(wst))
											this.waetherStationTimes.add(wst);
									}catch(DateTimeException ed) {
										System.err.println("ERRORE CONFIRATION-FILE middlelayer: "+ed.getMessage());
										System.err.println("ERRORE CONFIRATION-FILE middlelayer: la data di campionamento non verra' aggiunta");
									}
								}
								if(this.waetherStationTimes.size()==0)
									throw new Exception("Nessun orario definito nel file di configurazione.");
								
							}catch(Exception e) {
								System.err.println("ERRORE CONFIRATION-FILE middlelayer: "+e.getMessage());
								System.err.println("ERRORE CONFIRATION-FILE middlelayer: configurazione con valori di base: 14:00:00");
								this.waetherStationTime= LocalTime.of(WeatherStationController.defaultHour,WeatherStationController.defaultMinute,WeatherStationController.defaultSecond);
								this.waetherStationTimes.add(waetherStationTime);
								//this.timeIrrigation[0]= IrrigationController.defaultHour;//ora
								//this.timeIrrigation[1]= IrrigationController.defaultMinute;//minuti
							}
							System.out.println("CONFIGURATION-FILE middlelayer: irrigationTimes : "+this.waetherStationTimes.toString());
							//sc.nextLine();
							break;
							
							case "serialChannel" :
								//Leggiamo i canali da considerare per la classificazione
								while(sc.hasNextInt()) {
									try {
										this.idSerialChannel.add(sc.nextInt());
									}catch(Exception e) {
										System.err.println("ERRORE CONFIGURATION-FILE middlelayer: errore di lettura di uno dei canali di serialChannel");
									}
								}
							System.out.println("CONFIGURATION-FILE middlelayer: serialChannel : "+this.idSerialChannel.toString());
							break;
							
							case "releAut" :
								//Leggiamo i canali da considerare per la classificazione
								while(sc.hasNextInt()) {
									try {
										this.releAut= sc.nextInt();
									}catch(Exception e) {
										System.err.println("ERRORE CONFIGURATION-FILE middlelayer: errore di lettura releAut.");
										System.err.println("ERRORE CONFIGURATION-FILE middlelayer: sara' lasciato id default.");
									}
								}
							System.out.println("CONFIGURATION-FILE middlelayer: releAut : "+this.releAut);
							break;
							
							case "releMan" :
								//Leggiamo i canali da considerare per la classificazione
								while(sc.hasNextInt()) {
									try {
										this.releMan= sc.nextInt();
									}catch(Exception e) {
										System.err.println("ERRORE CONFIGURATION-FILE middlelayer: errore di lettura releMan.");
										System.err.println("ERRORE CONFIGURATION-FILE middlelayer: sara' lasciato id default.");
									}
								}
							System.out.println("CONFIGURATION-FILE middlelayer: releMan : "+this.releMan);
							break;
							
							case "serialPort" :
								if(sc.hasNext())
									try {
										this.serialPort=sc.next();
									}catch(Exception e) {
										System.err.println("ERRORE CONFIGURATION-FILE middlelayer: errore di lettura serialPort.");
										System.err.println("ERRORE CONFIGURATION-FILE middlelayer: sara' lasciato id default.");
										this.serialPort="/dev/ttyUSB0";
									}
								System.out.println("CONFIGURATION-FILE middlelayer: serialPort : "+this.serialPort);
							break;
							
						default:
							sc.nextLine();
							
					}	
				else
					if(sc.hasNext())
						sc.nextLine();
			}
			sc.close();

		} catch (FileNotFoundException e) {
			System.err.println("ERRORE LETTURA FILE CONFIGURAZIONE: file non presente.");
			
		}catch(Exception e) {
			System.err.println("ERRORE lettura generico..");
		}
		
	}


	public String getIpMiddleLayer() {
		return ipMiddleLayer;
	}


	public long getTempoCampionamentoWS() {
		return tempoCampionamentoWS;
	}
	
	private void setMaxTimeIrrigationFromInt(int timeI) {
		//Conversione dei minuti in millisecondi
		this.irrigationMaxTime= Long.parseLong(Integer.toString(timeI*1000*60));
	}
	
	public boolean findWeatherTime(LocalTime lc) {
		for(LocalTime l: this.waetherStationTimes) {
			if(l.compareTo(lc)==0)
				return true;
		}
		return false;
	}
	
}
