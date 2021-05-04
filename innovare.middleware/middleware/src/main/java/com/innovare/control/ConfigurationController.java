package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;

import com.innovare.utils.Utilities;

public class ConfigurationController {

	public static boolean testingIrrigazione= false;
	public ArrayList<Integer> channelForClassification= new ArrayList<Integer>();
	
	//Il tempo di irrigazione massimo viene espresso in long  
	//ma viene prelevato dal file di configurazione in minuti
	public long irrigationMaxTime;
	
	//Array contenente ora e minuti si inizio dell'irrigazione in una giornata
	public LocalTime timeIrrigation;
	
	//Portata irrigazione
	public float portataIrrigation;
	
	//Delay dalla prossima irrigazione
	public long delayIrrigation;
	
	//IP gateway, di default settato su localhost
	public static String gatewayIP="localhost";
	
	public ConfigurationController(){
		//verifichiamo l'esistenza del file di configurazione e lo leggiamo
		//altrimenti lasciamo i valori nel loro stato predefinito in Utilities
		File confF= new File(Utilities.configurationMiddleLayerPath);
		
		
		System.out.println("CONFIGURATION-FILE: Lettura file di configurazione...");
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
									System.err.println("ERRORE CONFIGURATION-FILE middlelayer: errore di lettura di uno dei canali di channelForClassification");
								}
							}
							System.out.println("CONFIGURATION-FILE middlelayer: channelForClassification : "+this.channelForClassification.toString());
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
							
						case "irrigationTime":
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
								this.timeIrrigation= LocalTime.parse(localTimeString);
								
							}catch(DateTimeException ed) {
								System.err.println("ERRORE CONFIRATION-FILE middlelayer: "+ed.getMessage());
								System.err.println("ERRORE CONFIRATION-FILE middlelayer: configurazione con valori di base: 14:00:00");
								this.timeIrrigation= LocalTime.of(IrrigationController.defaultHour,IrrigationController.defaultMinute,IrrigationController.defaultSecond);
								
							}
							catch(Exception e) {
								System.err.println("ERRORE CONFIRATION-FILE middlelayer: "+e.getMessage());
								System.err.println("ERRORE CONFIRATION-FILE middlelayer: configurazione con valori di base: 14:00:00");
								this.timeIrrigation= LocalTime.of(IrrigationController.defaultHour,IrrigationController.defaultMinute,IrrigationController.defaultSecond);
								//this.timeIrrigation[0]= IrrigationController.defaultHour;//ora
								//this.timeIrrigation[1]= IrrigationController.defaultMinute;//minuti
							}
							System.out.println("CONFIGURATION-FILE middlelayer: irrigationTime : "+this.timeIrrigation);
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
								System.err.println("ERRORE CONFIRATION-FILE middlelayer: lettura portata "+e.getMessage());
								this.portataIrrigation= IrrigationController.defaultPortata;
							}
							System.out.println("CONFIGURATION-FILE middlelayer: irrigationPortata : "+this.portataIrrigation);
							sc.nextLine();
							break;
						
						case "irrigationDelay":
							//Leggiamo il tempo in minuti e lo convertiamo in long
							try {
								this.setDelayFromNextIrrigationFromInt(sc.nextInt());
							}catch(Exception e) {
								System.err.println("ERRORE CONFIGURATION-FILE middlelayer: errore lettura irrigationDelay , configurazione valori di base: "+IrrigationController.delayOneMinutetTest);
								this.delayIrrigation= IrrigationController.delayOneMinutetTest;
							}
							System.out.println("CONFIGURATION-FILE middlelayer: delayIrrigation : "+this.delayIrrigation);
							sc.nextLine();
							break;
							
						case "gatewayIP":
							//Leggiamo il tempo in minuti e lo convertiamo in long
							try {
								if(sc.hasNext())
									this.gatewayIP = sc.next();
								else throw new Exception("Nessun gateway Ip presente nel file di configurazione");
							}catch(Exception e) {
								System.err.println("ERRORE CONFIGURATION-FILE middlelayer: errore lettura gateway Ip , configurazione valori di base: "+this.gatewayIP);
								
							}
							System.out.println("CONFIGURATION-FILE middlelayer: gateway Ip : "+this.gatewayIP);
							if(sc.hasNextLine())
								sc.nextLine();
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
			System.err.println("ERRORE LETTURA FILE CONFIGURAZIONE: file non presente.");
			System.err.println("Caricamento impostazioni predefinite..");
			
		}catch(Exception e) {
			System.err.println("ERRORE lettura generico.."+e.getMessage());
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
	
	
}
