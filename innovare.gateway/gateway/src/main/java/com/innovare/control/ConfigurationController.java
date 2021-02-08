package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.innovare.utils.Utilities;

public class ConfigurationController {

	public static String ipMiddleLayer=Utilities.ipMiddleLayer;
	public static long tempoCampionamentoWS= Utilities.tempoCampionamentoWeatherStationTest;
	
	
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
			
		}		
		
	}


	public String getIpMiddleLayer() {
		return ipMiddleLayer;
	}


	public long getTempoCampionamentoWS() {
		return tempoCampionamentoWS;
	}
	
	
	
	
}
