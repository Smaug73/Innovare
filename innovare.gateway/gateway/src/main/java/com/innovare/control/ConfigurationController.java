package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.DateTimeException;
import java.time.LocalTime;
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
								String localTimeString;
								if(sc.hasNext())
									localTimeString = sc.next();
								else throw new Exception("nessun orario definito nel file di configurazione.");
								this.waetherStationTime= LocalTime.parse(localTimeString);
								
							}catch(DateTimeException ed) {
								System.err.println("ERRORE CONFIRATION-FILE middlelayer: "+ed.getMessage());
								System.err.println("ERRORE CONFIRATION-FILE middlelayer: configurazione con valori di base: 13:55:00");
								this.waetherStationTime= LocalTime.of(WeatherStationController.defaultHour,WeatherStationController.defaultMinute,WeatherStationController.defaultSecond);
								
							}
							catch(Exception e) {
								System.err.println("ERRORE CONFIRATION-FILE middlelayer: "+e.getMessage());
								System.err.println("ERRORE CONFIRATION-FILE middlelayer: configurazione con valori di base: 14:00:00");
								this.waetherStationTime= LocalTime.of(WeatherStationController.defaultHour,WeatherStationController.defaultMinute,WeatherStationController.defaultSecond);
								//this.timeIrrigation[0]= IrrigationController.defaultHour;//ora
								//this.timeIrrigation[1]= IrrigationController.defaultMinute;//minuti
							}
							System.out.println("CONFIGURATION-FILE middlelayer: irrigationTime : "+this.waetherStationTime);
							sc.nextLine();
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
	
	
}
