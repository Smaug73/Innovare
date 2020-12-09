package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.Model;
import com.innovare.utils.Utilities;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

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
		File f = new File(System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareModels"+System.getProperty("file.separator"));
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
		if(foundModel(modelName))
			this.modelSelected=new Model(modelName);
		else 
			throw new FileNotFoundException("Il modello: "+modelName+" non esiste.");
	}
	
	public Model getSelectedModel() {
		return this.modelSelected;
	}
	
}
