package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.PlantClassification;

import io.vertx.core.json.JsonObject;

/*
 * Si occupa della creazione della classificazione delle immagini interagendo con lo script python.
 * Il classificatore generera' un file json all'interno della stessa cartella delle immagini generate
 * con il nome di "classifications.json"
 */
public class Classificator {
	
	private String imagesPath;
	private String modelName;
	private String scriptPath="/home/stefano/Innovare/innovare.utils/pythonScript/";
	
	
	public Classificator(String imagesPath, String modelName) {
		super();
		this.imagesPath = imagesPath;
		this.modelName = modelName;
	}


	public ArrayList<PlantClassification> newClassification(){
		//Avvio il processo di classificazione 
		ArrayList<PlantClassification> classifications = new ArrayList<PlantClassification>();
		try {
			System.out.println("python "+this.scriptPath+"Esempio1.py "+this.modelName+" "+this.imagesPath);
			Process process= Runtime.getRuntime().exec("python "+this.scriptPath+"Esempio1.py "+this.modelName+" "+this.imagesPath);
			
			int processOutput=process.waitFor();
			OutputStream outP= process.getOutputStream();
			System.out.println(outP.toString());
			
			if(processOutput == 0) {
				/* 
				 * Terminato il processo leggiamo il file di classificazione generato e lo 
				 * deparsiamo in una classe i cui elementi dovranno essere salvati all'interno del database
				 * Se il processo non e' andato a buon fine lanciamo una eccezione, lo stesso vale se il 
				 * file che doveva essere generato non e' stato generato.
				 */	
				File jsonFile= new File(this.imagesPath+"classification.json");
				Scanner reader= new Scanner(jsonFile);
				String jsonStrings="";
				//Leggiamo tutto il json
				while(reader.hasNextLine()) {
					jsonStrings= jsonStrings+reader.nextLine().toString();
				}
				reader.close();
				//Dopo aver letto il json lo convertiamo in una classe
				return classifications= new ObjectMapper().readValue(jsonStrings, new TypeReference<ArrayList<PlantClassification>>(){});
				
			}
			else throw new InterruptedException();
			
		} catch (IOException | InterruptedException e) {
			System.err.println("Errore processo python con path:"+this.scriptPath+" e nome modello:"+this.modelName);
			e.printStackTrace();
			return classifications;
		} 
		
		
	}
	
	public String getJsonStringLastClassification() throws FileNotFoundException {
		File jsonFile= new File(this.imagesPath+"classification.json");
		Scanner reader= new Scanner(jsonFile);
		String jsonStrings="";
		//Leggiamo tutto il json
		while(reader.hasNextLine()) {
			jsonStrings= jsonStrings+reader.nextLine().toString();
		}
		reader.close();
		return jsonStrings;
	}
	
	
	
	
	
}
