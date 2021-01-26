package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.Sample;
import com.innovare.utils.Utilities;
import com.opencsv.CSVReader;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class SampleCSVController extends Thread{

	MongoClient mongoClient=null;
	
	private ArrayList<Integer> channelNumberCSV= new ArrayList<Integer>();
	public int startingChannel=16;
	
	public SampleCSVController() {}
	
	public SampleCSVController(MongoClient mongoClient) {
		this.mongoClient= mongoClient;
	}
	
	public void run() {
		
		if(this.mongoClient==null) {
			System.out.println("CSV CONTROLLER DEBUG: nessun client mongo e' stato assegnato.");
			return;
		}else
			while(true) {
				
				//Mettiamo in sleep il thread
				try {
					this.sleep(10000);
				} catch (InterruptedException e) {
					System.err.println("Errore nella sleep del thread WeatherStationController");
					e.printStackTrace();
				}
				
				System.out.println("CSV CONTROLLER DEBUG: Leggo file csv e salvo le nuove misure..");
				
				try {
					HashMap<String, ArrayList<Sample> > newSamples= this.readSampleFromCSV();
					//debug
					System.out.println("Sensori attivi:"+newSamples.keySet());
					JsonObject jo;
					//VERIFICARE CONCORRENZA SUL CLIENT MONGO
					//Ogni nuovo sample viene aggiunto al database in base al canale
					for(int i=0;i<10;i++) {
						if(newSamples.containsKey("#ch"+i)) {
							System.out.println("Salvataggio misure canale ch"+(i+this.startingChannel-1)+"..");
							ArrayList<Sample> samples= newSamples.get("#ch"+i);
							for(Sample s: samples) {
								jo= new JsonObject(new ObjectMapper().writeValueAsString(s));
								this.mongoClient.insert("channel-"+(i+this.startingChannel-1), jo, res ->{
									if(res.succeeded())
				    				  System.out.println("Misura salvata correttamente nel DB.");
									else
				    				  System.err.println("ERRORE salvataggio misura");  
								});
							}
						}
						
						
					}
					
				} catch (IOException e1) {
					System.err.println(e1.getMessage());
					e1.printStackTrace();
				}
				
				//Mettiamo in sleep il thread
				try {
					this.sleep(600000);
				} catch (InterruptedException e) {
					System.err.println("Errore nella sleep del thread WeatherStationController");
					e.printStackTrace();
				}
			}	
	}
	
	/*
	 * Lettura dei Sample dal file csv, che andra' cancellato
	 */
	public HashMap<String, ArrayList<Sample> > readSampleFromCSV() throws IOException {
		//La lista dei sensori attivi deve essere resettata
		this.channelNumberCSV= new ArrayList<Integer>();
		
		//Cerchiamo il file csv all'interno del path
		File dirCSV= new File(Utilities.sensorDocumentPath);
		File csvFile;
		
		//Al suo interno ci deve essere un solo file che dovra' essere letto
		if(dirCSV.isDirectory()) {
			System.out.println("DEBUG dir:"+dirCSV.getName());
			File[] child= dirCSV.listFiles();
			//Caso nel quale non c'e' nessun nuovo file da leggere
			if(child.length==0) {
				System.out.println("DEBUG CSV READER: Nessun nuovo fil trovato.");
				throw new FileNotFoundException(); 
			}
			
			if(child.length>1) {
				System.err.println("ERRORE: presenza di un file");
				csvFile= findNewer(child);
			}
			else
				csvFile= child[0];
			
			System.out.println("DEBUG file:"+csvFile.getName());
			//Trovato il file lo leggiamo e lo deparsiamo.
			
				FileReader fr= new FileReader(csvFile.getAbsolutePath());
				CSVReader csvReader = new CSVReader(fr); 
				
				List<String[]> allData = csvReader.readAll();
				
				//Creo una raccolta dei sample per ogni sensore
				HashMap<String, ArrayList<Sample> > samplesMaps= new HashMap<String,ArrayList<Sample>>();
				
				
				
				int sensorNumber=0;	
				int channelIdInt=0;
				int posizioneValor=-1;
				int posizioneChannel=-1;
				int dist=-1;
				//Inizio prendendo i valori di mio interessa
				for(int j=0; j<allData.get(0).length; j++) {
					//Vedo quanti sono i sensori attivi
					if(allData.get(0)[j].contains("ch")) {
						if(posizioneChannel==-1)
							posizioneChannel=j;
						//controllo se ha un valore 0 oppure 1 per vedere se e' attivo e aggiungiamo il sensore
						if(allData.get(1)[j].contains("1")) {
							samplesMaps.put(allData.get(0)[j], new ArrayList<Sample>());
							//Aggiungiamo alla lista dei sensori
							this.channelNumberCSV.add(this.startingChannel+channelIdInt);
							sensorNumber++;
						}	
						channelIdInt++;//Incrementiamo l'id int del canale che stiamo leggendo
					}
					
					if(allData.get(0)[j].contains("m") && allData.get(0)[j].startsWith("m") && (posizioneValor==-1)) {
						//troviamo la prima posizione dalla quale prendere i valori
						posizioneValor=j;	
						dist=posizioneValor-posizioneChannel;
					}
				}
				
				/*
				 * DEBUG
				 *
				for (String[] record : allData) {
				    System.out.println("1 : " + record[0]);
				    System.out.println("2 : " + record[1]);
				    System.out.println("3 : " + record[2]);
				    System.out.println("4 : " + record[3]);
				    System.out.println("---------------------------");
				}
				System.out.println("PosizioneVal: " + posizioneValor);
				System.out.println("sensorNumber: " + sensorNumber);
				System.out.println("sampleMap: " + samplesMaps.keySet().toArray()[0]);
				
				/*
				 * 
				 */
				
				//Leggiamo riga per riga scartando la prima
				for (int j=1; j<allData.size(); j++) {
					for(int i=0; i<samplesMaps.keySet().size(); i++) {
						//prendo l'arraylist di un certo sensore e aggiungo il nuovo sample prendendo i dati dalle posizioni del csv
						samplesMaps.get(samplesMaps.keySet().toArray()[i]).add(new Sample(allData.get(j)[0],Float.valueOf(allData.get(j)[i+posizioneValor])));		
					}		
		        }
				
				//AGGIUNGERE RIMOZIONE DEL FILE
				
				return samplesMaps;
				
			 
		}else
			throw new FileNotFoundException(); 
	}
	
	
	private File findNewer(File[] files) {
		int max=0;
		for(int i=1; i< (files.length-1); i++) {
			if(files[max].lastModified() < files[i].lastModified()) {
				max=i;
			}
		}
		return files[max];
	}
	
	
	private void deleteAllFiles(File[] files) {
		for(File f: files) {
			//Rimozione ricorsiva
			if(f.isDirectory()) {
				File[] childF= f.listFiles();
				deleteAllFiles(childF);
			}
			else
				f.delete();
		}
	}

	public ArrayList<Integer> getChannelNumberCSV() {
		return channelNumberCSV;
	}


	
	
}
