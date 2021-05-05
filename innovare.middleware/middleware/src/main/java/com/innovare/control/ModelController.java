package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.innovare.model.Model;
import com.innovare.utils.Utilities;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/*
 * Gestore dei modelli di classificazione
 */
public class ModelController {
	
	private Model modelSelected=null;
	
	public ModelController() {}
	
	
	//Cerco un modello con un determinato nome
	public boolean foundModel(String nameSearch) {
		File f = new File(Utilities.modelPath);	
		if(f.exists()) {
			//Controllo tutti i file contenuti nella cartella
			File[] childs= f.listFiles();
			for(File c: childs) {
				if(c.getName().equals(nameSearch))
					return true;				
			}
			return false;
		}else	
			return false;
	}
	
	public ArrayList<Model> getAllModel(){
		File f = new File(Utilities.modelPath);
		ArrayList<Model> models= new ArrayList<Model>();
		if(f.exists()) {
			//Controllo tutti i file contenuti nella cartella e passo i loro nomi tramite un json
			File[] childs= f.listFiles();
			Model m;
			for(File c: childs) {
				m= new Model(c.getName());
				models.add(m);			
			}
			return models;
		}else
			return models;//Ritorna una lista vuota di modelli				
	}
	
	public void setModelSelected(String modelName) throws FileNotFoundException {
		if(foundModel(modelName)) {
			this.modelSelected=new Model(modelName);
			System.out.println("Modello selezionato: "+modelName);
		}
		else 
			throw new FileNotFoundException("Il modello: "+modelName+" non esiste.");
	}
	
	
	/*
	 * Utilizziamo Future poiche' il processo di UnZip puo' richiedere molto tempo se il file e' grande
	 */
	public Future<Boolean> unZipModel(String zipName) throws FileNotFoundException, ZipException {
		
		Promise<Boolean> prom= Promise.promise();
		
		if(this.foundModel(zipName)) {
			
				//Apriamo il file zip del modello
		         ZipFile zipFile = new ZipFile(Utilities.modelPath+zipName);
		         File fz= new File(Utilities.modelPath+zipName);
		         
		                 
		         if(!zipFile.isValidZipFile() || !fz.canRead()) {
		        	 System.err.println("FILE-ERROR:file zip non valido");
			         //throw new FileNotFoundException();
		        	 prom.fail("DEBUG UnZipModel: file zip non valido!");
		         }
		         if (zipFile.isEncrypted()) {
		        	 System.err.println("FILE-ERROR:Il file .zip appena letto e' criptato.");
		            //throw new FileNotFoundException();
		        	 prom.fail("DEBUG UnZipModel: Il file .zip appena letto e' criptato.!");
		         }
		        
		         //Unzippiamo tutto nella stessa cartella
		         zipFile.extractAll(Utilities.modelPath);
		         
		         //Eliminiamo il file zip se tutto è andato a buon fine
		         fz= new File(Utilities.modelPath+zipName);
		         
		         if (fz.delete()) { 
		             System.out.println("Deleted the folder: " + fz.getName());
		           } else {
		             System.out.println("Failed to delete the folder.");
		             throw new FileNotFoundException();
		           }
		         
		        //Unzip completato correttamente
		        prom.complete(true);
		}else
			prom.fail("DEBUG UnZipModel: File not Found!");
		
		return prom.future();
	}
	
	
	/*
	 * Verificare se il modello e' ancora in memoria
	 */
	public Model getSelectedModel() {
		return this.modelSelected;
	}
	
}
