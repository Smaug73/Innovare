package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.nio.file.Path;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.innovare.model.PlantClassification;


import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/*
 * Si occupa della creazione della classificazione delle immagini interagendo con lo script python.
 * Il classificatore generera' un file json all'interno della stessa cartella delle immagini generate
 * con il nome di "classifications.json"
 */
public class Classificator {
	
	private String imagesDirName;
	private String scriptPath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareScript"+System.getProperty("file.separator");
	private String destination=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareImages"+System.getProperty("file.separator");
	private String zipSourcePath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareZip"+System.getProperty("file.separator");
	private String modelPath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareModels"+System.getProperty("file.separator");
	
	public Classificator(String imagesDirName) {
		super();
		if(imagesDirName.substring(imagesDirName.length()-4, imagesDirName.length()-1).equalsIgnoreCase(".zip"))
			this.imagesDirName = imagesDirName.substring(0, imagesDirName.length()-1);
		else 
			this.imagesDirName= imagesDirName;
		System.out.println("Test dirname: "+this.imagesDirName);
		//Creazione del relative Path e del Path di destinazione
	}

	/*
	 * Metodo per effettuare l'unzip del file contenente le foto
	 */
	public void unZipFile() throws FileNotFoundException {
			
		try {
	         ZipFile zipFile = new ZipFile(this.zipSourcePath+this.imagesDirName+".zip");
	         if (zipFile.isEncrypted()) {
	        	 System.err.println("FILE-ERROR:Il file .zip appena letto e' criptato.");
	            throw new FileNotFoundException();
	         }
	         //Nel caso non esista la cartella delle immagini da classificare viene creata
	         if(createDirOrFile(destination))
	        	 zipFile.extractAll(destination);
	         else
	        	 throw new FileNotFoundException("FILE-ERROR:Fallimento creazione cartella di destinazione file Zip.");
	    } catch (ZipException e) {
	        e.printStackTrace();
	    }
		
	}
	
	
	
	
	public ArrayList<PlantClassification> newClassification(String modelName) throws FileNotFoundException{	
		//Verifichiamo che il modello selezionato esista
		if(!this.existDirOrFile(this.modelPath+modelName))
			throw new FileNotFoundException("Il modello selezionato non esiste.");
		
		//Avvio il processo di classificazione 
		ArrayList<PlantClassification> classifications = new ArrayList<PlantClassification>();
		try {
			System.out.println("python "+this.scriptPath+"ClassificationScript.py "+this.modelPath+modelName+" "+this.destination+this.imagesDirName+"/");
			Process process= Runtime.getRuntime().exec("python "+this.scriptPath+"ClassificationScript.py "+this.modelPath+modelName+" "+this.destination+this.imagesDirName+"/");
			
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
				File jsonFile= new File(this.destination+this.imagesDirName+"/classification.json");
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
			System.err.println("Errore processo python con path:"+this.scriptPath+"ClassificationScript.py e nome modello:"+modelName);
			e.printStackTrace();
			return classifications;
		} 
		
		
	}
	
	/* Il metodo copy da un errore, da controllare.
	 * Nel fare il load bisogna controllare che il file sia .h5 
	public void saveNewModel(String newModelPath) {
		Path source= Path.of(newModelPath);
		Path dest= Path.of(this.modelPath);
		Files.copy(source , dest.resolve(source.getFileName()), "REPLACE_EXISTING");
	}
	*/
	
	/*
	 * Serve a controllare se la directory o il file e' gia' esistente e nel caso crearlo
	 * Ritorna false se la cartella o il file non esiste
	 */
	private boolean createDirOrFile(String path){
		File f = new File(path);
		if(f.exists())
			return f.exists();
		else {
			f.mkdirs();
			return f.exists();
		}
	}
	
	/*
	 * Controllo se un file o una Directory esiste
	 */
	private boolean existDirOrFile(String path) {
		File f = new File(path);
		if(f.exists())
			return f.exists();
		else {
			return false;
		}
	}
	
	
	/*
	 * Ritorna il json della classificazione in formato Stringa
	 */
	public String getJsonStringLastClassification() throws FileNotFoundException {
		File jsonFile= new File(this.imagesDirName+"classification.json");
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
