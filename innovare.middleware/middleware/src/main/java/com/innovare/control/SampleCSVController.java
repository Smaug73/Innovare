package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.innovare.model.Sample;
import com.innovare.utils.Utilities;
import com.opencsv.CSVReader;

public class SampleCSVController {

	
	
	public SampleCSVController() {}
	
	
	/*
	 * Lettura dei Sample dal file csv, che andra' cancellato
	 */
	public HashMap<String, ArrayList<Sample> > readSampleFromCSV() throws IOException {
		//Cerchiamo il file csv all'interno del path
		File dirCSV= new File(Utilities.sensorDocumentPath);
		File csvFile;
		
		//Al suo interno ci deve essere un solo file che dovra' essere letto
		if(dirCSV.isDirectory()) {
			File[] child= dirCSV.listFiles();
			//Caso nel quale non c'e' nessun nuovo file da leggere
			if(child.length==0) {
				throw new FileNotFoundException(); 
			}
			
			if(child.length>1) {
				System.err.println("ERRORE: presenza di un file");
				csvFile= findNewer(child);
			}
			else
				csvFile= child[0];
			
			//Trovato il file lo leggiamo e lo deparsiamo.
			
				FileReader fr= new FileReader(csvFile.getAbsolutePath());
				CSVReader csvReader = new CSVReader(fr); 
				
				List<String[]> allData = csvReader.readAll();
				
				//Creo una raccolta dei sample per ogni sensore
				HashMap<String, ArrayList<Sample> > samplesMaps= new HashMap<String,ArrayList<Sample>>();
				
				
				
				int sensorNumber=0;	
				int posizioneValor=-1;		
				//Inizio prendendo i valori di mio interessa
				for(int j=0; j<allData.get(0).length; j++) {
					//Vedo quanti sono i sensori attivi
					if(allData.get(0)[j].contains("ch")) {
						//controllo se ha un valore 0 oppure 1 per vedere se e' attivo e aggiungiamo il sensore
						if(allData.get(1)[j].contains("1")) {
							samplesMaps.put(allData.get(0)[j], new ArrayList<Sample>());
							sensorNumber++;
						}	
					}
					
					if(allData.get(0)[j].contains("m") && (posizioneValor==-1)) {
						//troviamo la prima posizione dalla quale prendere i valori
						posizioneValor=j;	
					}
				}
				
				
				//Leggiamo riga per riga scartando la prima
				for (int j=1; j<allData.size(); j++) {
					for(int i=0; i<sensorNumber; i++) {
						//prendo l'arraylist di un certo sensore e aggiungo il nuovo sample prendendo i dati dalle posizioni del csv
						samplesMaps.get(allData.get(0)[i+posizioneValor]).add(new Sample(allData.get(j)[0],Integer.valueOf(allData.get(j)[i+posizioneValor])));		
					}		
		        }
				
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
	
	
}
