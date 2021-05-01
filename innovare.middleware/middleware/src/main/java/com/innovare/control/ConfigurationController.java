package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.innovare.utils.Utilities;

public class ConfigurationController {

	public static boolean testingIrrigazione= false;
	public ArrayList<Integer> channelForClassification= new ArrayList<Integer>();
	
	//Il tempo di irrigazione massimo viene espresso in long  
	//ma viene prelevato dal file di configurazione in minuti
	public long irrigationMaxTime;
	
	public ConfigurationController(){
		//verifichiamo l'esistenza del file di configurazione e lo leggiamo
		//altrimenti lasciamo i valori nel loro stato predefinito in Utilities
		File confF= new File(Utilities.configurationMiddleLayerPath);
		
		
		
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
							while(sc.hasNext()) {
								this.channelForClassification.add(sc.nextInt());
							}
							break;
						case "irrigationMaxTime":
							//Leggiamo il tempo in minuti e lo convertiamo in long
							this.setMaxTimeIrrigationFromInt(sc.nextInt());
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
			System.err.println("Caricamento impostazioni predefinite..");
			
		}catch(Exception e) {
			System.err.println("ERRORE lettura generico..");
		}
		
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
	
	
	
	
}
