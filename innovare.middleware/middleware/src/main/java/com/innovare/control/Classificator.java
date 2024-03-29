package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.nio.file.Path;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.innovare.model.ClassificationSint;
import com.innovare.model.PlantClassification;
import com.innovare.utils.ClassificationException;
import com.innovare.utils.Utilities;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/*
 * Si occupa della creazione della classificazione delle immagini interagendo con lo script python.
 * Il classificatore generera' un file json all'interno della stessa cartella delle immagini generate
 * con il nome di "classifications.json"
 * 
 * 
 * ATTENZIONE: La struttura della directory con dentro le foto deve essere del tipo:  nomeDirectory/Thermal_Optical/ * /* / *.jpg 
 * Immagini con estenzione rigorosamente .jpg 
 * 
 */

// python3 segmenter.py --create_hash_symlinks --image_processing_limit 5 -v --srcdir /home/stefano/InnovareZip/testDataset/ --dstdir /home/stefano/InnovareImages/ 
//Aggiungere normalizazione nel comando, come src dir va passata la cartella che contiene tutti i dataset unzippati(nel nostro caso deve essere solo uno) 
public class Classificator {
	
	private static final String LOG="DEBUG-CLASSIFICATOR:";
	
	private String imagesDirName;
	private Vertx vertx;
	//private String scriptPath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareScript"+System.getProperty("file.separator");
	//private String destination=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareImages"+System.getProperty("file.separator");
	//private String zipSourcePath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareZip"+System.getProperty("file.separator");
	//private String modelPath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareModels"+System.getProperty("file.separator");
	
	public Classificator(String imagesDirName,Vertx v) {
		super();
		this.vertx=v;
		if(imagesDirName.substring(imagesDirName.length()-4, imagesDirName.length()).equalsIgnoreCase(".zip"))
			this.imagesDirName = imagesDirName.substring(0, imagesDirName.length()-4);
		else 
			this.imagesDirName= imagesDirName;
		Logger.getLogger().print("Test dirname: "+this.imagesDirName);
		//Logger.getLogger().print("Test -4 -1: "+imagesDirName.substring(imagesDirName.length()-4, imagesDirName.length()-1));
		//Logger.getLogger().print("Test 0 -4: "+imagesDirName.substring(0, imagesDirName.length()-4));
		//Creazione del relative Path e del Path di destinazione
	}

	/*
	 * Metodo per effettuare l'unzip del file contenente le foto
	 * Le foto del volo vengono estratte e salvate nella cartella InnovareImages
	 */
	public void unZipFile() throws FileNotFoundException {
			
		try {
	         ZipFile zipFile = new ZipFile(Utilities.zipSourcePath+this.imagesDirName+".zip");
	         if (zipFile.isEncrypted()) {
	        	 Logger.getLogger().print("FILE-ERROR:Il file .zip appena letto e' criptato.");
	            throw new FileNotFoundException();
	         }
	         //Nel caso non esista la cartella delle immagini da classificare viene creata
	         if(createDirOrFile(Utilities.datasetPath)) {
	        	 //Estraiamo le foto del volo nella cartella InnovareImages nella sottocartella con nome uguale a quello dello zip
	        	 //this.createDirOrFile(Utilities.datasetPath+System.getProperty("file.separator")+this.imagesDirName);
	        	 zipFile.extractAll(Utilities.datasetPath+System.getProperty("file.separator"));
	         }
	         else
	        	 throw new FileNotFoundException("FILE-ERROR:Fallimento creazione cartella di destinazione file Zip.");
	    } catch (ZipException e) {
	        e.printStackTrace();
	    }
		
	}
	
	
	
	public ClassificationSint unZipAndClassification(String modelName) throws FileNotFoundException, ClassificationException{
		//Verifichiamo che il modello selezionato esista
		if(!this.existDirOrFile(Utilities.modelPath+modelName))
			throw new FileNotFoundException("Il modello selezionato non esiste.");
		
		//Avvio il processo di classificazione 
		ArrayList<PlantClassification> classifications = new ArrayList<PlantClassification>();
		
		try {
			//Unzippo il file zip nella directory dalla quale segmentero' le immagini
			this.unZipFile();
			
			//Prima di avviare il segmenter viene creata la cartella interna a InnovareSegmentDS che conterrà le immagini segmentate del volo
			this.createDirOrFile(Utilities.segmentDataSetPath+System.getProperty("file.separator")+this.imagesDirName);
			//Avvio il segmenter
			Logger.getLogger().print("python3 "+Utilities.scriptPath+"segmenter.py --create_hash_symlinks --image_processing_limit 5 -v --srcdir "+Utilities.datasetPath+this.imagesDirName+System.getProperty("file.separator")+" --dstdir "+Utilities.segmentDataSetPath+this.imagesDirName+System.getProperty("file.separator"));
			Process processSeg= Runtime.getRuntime().exec("python3 "+Utilities.scriptPath+"segmenter.py --create_hash_symlinks --image_processing_limit 5 -v --srcdir "+Utilities.datasetPath+this.imagesDirName+" --dstdir "+Utilities.segmentDataSetPath+this.imagesDirName);
			//Attendiamo la fine della segmentazione
			int processSegOutput=processSeg.waitFor();
			OutputStream outPSeg= processSeg.getOutputStream();
			Logger.getLogger().print(outPSeg.toString());
			
			//Finita la segmentazione avviamo la classificazione
			Logger.getLogger().print("python3 "+Utilities.scriptPath+"ClassificationScript.py "+Utilities.modelPath+modelName+" "+Utilities.segmentDataSetPath+this.imagesDirName+System.getProperty("file.separator"));
			Process process= Runtime.getRuntime().exec("python3 "+Utilities.scriptPath+"ClassificationScript.py "+Utilities.modelPath+modelName+" "+Utilities.segmentDataSetPath+this.imagesDirName+System.getProperty("file.separator"));
			
			//Attendiamo la fine del processo di classificazione
			int processOutput=process.waitFor();
			OutputStream outP= process.getOutputStream();
			Logger.getLogger().print(outP.toString());
			
			if(processOutput == 0) {
				/* 
				 * Terminato il processo leggiamo il file di classificazione generato e lo 
				 * deparsiamo in una classe i cui elementi dovranno essere salvati all'interno del database
				 * Se il processo non e' andato a buon fine lanciamo una eccezione, lo stesso vale se il 
				 * file che doveva essere generato non e' stato generato.
				 */	
				File jsonFile= new File(Utilities.segmentDataSetPath+this.imagesDirName+"/classification.json");
				Scanner reader= new Scanner(jsonFile);
				String jsonStrings="";
				//Leggiamo tutto il json
				while(reader.hasNextLine()) {
					jsonStrings= jsonStrings+reader.nextLine().toString();
				}
				reader.close();
				//Dopo aver letto il json lo convertiamo in una classe
				classifications= new ObjectMapper().readValue(jsonStrings, new TypeReference<ArrayList<PlantClassification>>(){});
				return new ClassificationSint(classifications);
			}
			else throw new InterruptedException();
			
			
			
		}
		catch (IOException e) {
			Logger.getLogger().print("Errore processo python con path:"+Utilities.scriptPath+"ClassificationScript.py e nome modello:"+modelName);
			e.printStackTrace();
			return new ClassificationSint();
		} catch (InterruptedException e) {
			Logger.getLogger().print("Errore processo python con path:"+Utilities.scriptPath+"ClassificationScript.py e nome modello:"+modelName);
			e.printStackTrace();
			return new ClassificationSint();
		}
				
	}
	
	
	/*
	 * Esecuzione asincrona del processo di classificazione
	 */
	public Future<ClassificationSint> asincronousClassification(String modelName){
		
		Promise<ClassificationSint> classificationP= Promise.promise();
		/*
		 * Preleviamo un worker per l'esecuzione del processo esterno
		 * Questo per evitare di bloccare l'eventLoop
		 * Lo configuriamo al momento, in modo da prelevare i parametri di configurazione
		 */
		int poolSize = 10;

		// 2 minutes
		long maxExecuteTime = 2;
		TimeUnit maxExecuteTimeUnit = TimeUnit.MINUTES;

		WorkerExecutor executor = vertx.createSharedWorkerExecutor("my-worker-pool", poolSize, maxExecuteTime, maxExecuteTimeUnit);
		
		executor.executeBlocking(promise -> {
			try {
				//Avviamo la parte di codice bloccante da dover eseguire
				ClassificationSint classifResult=this.unZipAndClassificationAsincronous(modelName);
				
				//Completata la classificazione verifichiamo il risultato
				if(classifResult!=null)
					promise.complete(classifResult);
				else
					promise.fail(this.LOG+" Errore esecuzione classificazione");
				
			}catch(FileNotFoundException | ClassificationException e) {
				Logger.getLogger().print("Errore generazione classificazione: "+e.getMessage());
				promise.fail(this.LOG+" Errore esecuzione classificazione");
			}
			
			
			
		}, res -> {
			if(res.succeeded()) {
				Logger.getLogger().print(this.LOG+" Classificazione eseguita con successo!");
				classificationP.complete((ClassificationSint) res.result());
			}else {
				classificationP.fail(this.LOG+" Errore esecuzione classificazione");
			}
		  
		});
		
		
		return classificationP.future();
	}
	
	
	
	/*
	 * Classificazione utilizzata dal Thread worker in modo asincrono
	 */
	public ClassificationSint unZipAndClassificationAsincronous(String modelName) throws FileNotFoundException, ClassificationException{
		//Verifichiamo che il modello selezionato esista
		if(!this.existDirOrFile(Utilities.modelPath+modelName))
			throw new FileNotFoundException("Il modello selezionato non esiste.");
		
		//Avvio il processo di classificazione 
		ArrayList<PlantClassification> classifications = new ArrayList<PlantClassification>();
		
		try {
			//Unzippo il file zip nella directory dalla quale segmentero' le immagini
			this.unZipFile();
			
			//Prima di avviare il segmenter viene creata la cartella interna a InnovareSegmentDS che conterrà le immagini segmentate del volo
			this.createDirOrFile(Utilities.segmentDataSetPath+System.getProperty("file.separator")+this.imagesDirName);
			//Avvio il segmenter
			Logger.getLogger().print("python3 "+Utilities.scriptPath+"segmenter.py --create_hash_symlinks --image_processing_limit 5 -v --srcdir "+Utilities.datasetPath+this.imagesDirName+System.getProperty("file.separator")+" --dstdir "+Utilities.segmentDataSetPath+this.imagesDirName+System.getProperty("file.separator"));
			Process processSeg= Runtime.getRuntime().exec("python3 "+Utilities.scriptPath+"segmenter.py --create_hash_symlinks --image_processing_limit 5 -v --srcdir "+Utilities.datasetPath+this.imagesDirName+" --dstdir "+Utilities.segmentDataSetPath+this.imagesDirName);
			//Attendiamo la fine della segmentazione
			int processSegOutput=processSeg.waitFor();
			OutputStream outPSeg= processSeg.getOutputStream();
			Logger.getLogger().print(outPSeg.toString());
			
			//Finita la segmentazione avviamo la classificazione
			Logger.getLogger().print("python3 "+Utilities.scriptPath+"ClassificationScript.py "+Utilities.modelPath+modelName+" "+Utilities.segmentDataSetPath+this.imagesDirName+System.getProperty("file.separator"));
			Process process= Runtime.getRuntime().exec("python3 "+Utilities.scriptPath+"ClassificationScript.py "+Utilities.modelPath+modelName+" "+Utilities.segmentDataSetPath+this.imagesDirName+System.getProperty("file.separator"));
			
			//Attendiamo la fine del processo di classificazione
			int processOutput=process.waitFor();
			OutputStream outP= process.getOutputStream();
			Logger.getLogger().print(outP.toString());
			
			if(processOutput == 0) {
				/* 
				 * Terminato il processo leggiamo il file di classificazione generato e lo 
				 * deparsiamo in una classe i cui elementi dovranno essere salvati all'interno del database
				 * Se il processo non e' andato a buon fine lanciamo una eccezione, lo stesso vale se il 
				 * file che doveva essere generato non e' stato generato.
				 */	
				File jsonFile= new File(Utilities.segmentDataSetPath+this.imagesDirName+"/classification.json");
				Scanner reader= new Scanner(jsonFile);
				String jsonStrings="";
				//Leggiamo tutto il json
				while(reader.hasNextLine()) {
					jsonStrings= jsonStrings+reader.nextLine().toString();
				}
				reader.close();
				//Dopo aver letto il json lo convertiamo in una classe
				classifications= new ObjectMapper().readValue(jsonStrings, new TypeReference<ArrayList<PlantClassification>>(){});
				return new ClassificationSint(classifications);
			}
			else throw new InterruptedException();
			
			
			
		}
		catch (IOException e) {
			Logger.getLogger().print("Errore processo python con path:"+Utilities.scriptPath+"ClassificationScript.py e nome modello:"+modelName);
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			Logger.getLogger().print("Errore processo python con path:"+Utilities.scriptPath+"ClassificationScript.py e nome modello:"+modelName);
			e.printStackTrace();
			return null;
		}
				
	}
	
	
	
	public ArrayList<PlantClassification> newClassification(String modelName) throws FileNotFoundException{	
		//Verifichiamo che il modello selezionato esista
		if(!this.existDirOrFile(Utilities.modelPath+modelName))
			throw new FileNotFoundException("Il modello selezionato non esiste.");
		
		//Avvio il processo di classificazione 
		ArrayList<PlantClassification> classifications = new ArrayList<PlantClassification>();
		try {
			Logger.getLogger().print("python "+Utilities.scriptPath+"ClassificationScript.py "+Utilities.modelPath+modelName+" "+Utilities.datasetPath+this.imagesDirName+"/");
			Process process= Runtime.getRuntime().exec("python "+Utilities.scriptPath+"ClassificationScript.py "+Utilities.modelPath+modelName+" "+Utilities.datasetPath+this.imagesDirName+"/");
			
			int processOutput=process.waitFor();
			OutputStream outP= process.getOutputStream();
			Logger.getLogger().print(outP.toString());
			
			if(processOutput == 0) {
				/* 
				 * Terminato il processo leggiamo il file di classificazione generato e lo 
				 * deparsiamo in una classe i cui elementi dovranno essere salvati all'interno del database
				 * Se il processo non e' andato a buon fine lanciamo una eccezione, lo stesso vale se il 
				 * file che doveva essere generato non e' stato generato.
				 */	
				File jsonFile= new File(Utilities.datasetPath+this.imagesDirName+"/classification.json");
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
			Logger.getLogger().print("Errore processo python con path:"+Utilities.scriptPath+"ClassificationScript.py e nome modello:"+modelName);
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
		File jsonFile= new File(Utilities.segmentDataSetPath+this.imagesDirName+System.getProperty("file.separator")+"classification.json");
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
