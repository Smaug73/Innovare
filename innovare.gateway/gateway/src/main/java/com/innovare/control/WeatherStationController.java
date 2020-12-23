package com.innovare.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import com.innovare.utils.Utilities;


/*
 * Periodicamente genera nuovi sample dalla weatherstation
 * Deve essere accessibile in mutua escluzione dai thread Channel, anche se eseguono solo una operazione di lettura
 * potrebbero accedere alla risorsa mentre sta aggiornando i dati al suon interno
 */
public class WeatherStationController extends Thread{
	private HashMap<String,Float> channels;
	private HashSet<String> channelsNames;
	private String campionamento;
	
	private long tempoCampionamento;
	
	public WeatherStationController(long tempoCampionamento) {
		
		channels= new HashMap<String,Float>();
		channelsNames= new HashSet<String>();
		this.tempoCampionamento=tempoCampionamento;
		
		//Creo l'hash set dei nomi dei vari canali
		for(int i=0; i< Utilities.channelsNames.length; i++) {
			channelsNames.add(Utilities.channelsNames[i]);
		}
		
		//debug
		System.out.println("\nDEBUG HASHSET");
		for(int i=0;i<channelsNames.size();i++)
			System.out.println(channelsNames.toArray()[i]);
		////////////
	}
	
	/*
	 * All'interno del metodo run andiamo ad aggiornare i valori e creare nuovi sample
	 */
	public void run() {
		while(true) {
			//Avvio l'aggiornamento dei valori tramite uno dei due metodi
			this.campionamentoFromFile();
			//Attendiamo il tempo per un nuovo campionamento
			try {
				this.sleep(tempoCampionamento);
			} catch (InterruptedException e) {
				System.err.println("Errore nella sleep del thread WeatherStationController");
				e.printStackTrace();
			}
		}
		
		
		
	}
	
	
	
	//Aggiorna i dati della weatherstation da un file 
	public void campionamentoFromFile() {
		File f= new File(Utilities.scriptWeatherPath+"test.txt");
		//Leggiamo riga per riga il file e aggiungiamo solo quando troviamo un sensore di quelli di interesse
		try {
			//Leggiamo tutto il file
			String fileString="";
			Scanner sc = new Scanner(f);
			while(sc.hasNext()) {
				fileString=fileString+sc.nextLine()+"\n";
			}
			
			//Letto tutto il file facciamo uno split
			System.out.println("\nFILE:");
			System.out.println(fileString);
			String reg="[ = \n]+";
			String[] token=fileString.split(reg);
			
			//debug
			System.out.println("\nDEBUG SPLIT STRING");
			for(int i=0;i<token.length;i++)
				System.out.println(token[i]);
			////////////
			
			//Leggiamo per ogni canale il valore corrispondente
			//Avanziamo di due posizioni alla volta ed ogni volta che leggiamo un channel che e' presente lo aggiungo
			for(int j=0; j<token.length;j=j+1) {
				System.out.println("Token: "+token[j]);
				if(this.channelsNames.contains(token[j])) {
					try {
						System.out.println("SI!");
						this.channels.put(token[j], Float.valueOf(token[j+1]));
					}
					catch(NumberFormatException n) {
						System.err.println("Errore conversione numero, sara' aggiunto null");
						this.channels.put(token[j], null);
					}	
				}else
					System.out.println("NO");
			}
			
			//DEBUG
			System.out.println("DEBUG:");
			for(String s: this.channels.keySet())
				System.out.println(this.channels.get(s));
			////////
			
		} catch (FileNotFoundException e) {
			System.out.println("File non trovato");
			e.printStackTrace();
		}
	}
	
	//Aggiorna i dati della weatherstation da un processo
	private void campionamentoFromProcess() throws IOException {
		//Lanciamo il processo e leggiamo l'outputstream
		Process processSt= Runtime.getRuntime().exec("./vproweather -x /dev/ttyUSB0\n >> sample.txt",null,new File(Utilities.scriptPath+"vproweather-1.1"+System.getProperty("file.separator")));
		//Leggiamo l'output del processo e lo salviamo in una stringaAttendiamo la fine della segmentazione
		InputStream is = processSt.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String output="";
		String line;
		String[] token;
		String reg="[ = \n]+";
		while ((line = br.readLine()) != null) {
			//L'array generato dove essere di dimensione 2, canale[0] e valore[1]
			token=line.split(reg);
			//Se il canale letto fa parte di quelli di interesse allora lo inserisco nella hashMap
			if(this.channelsNames.contains(token[0])) {
				try {
					System.out.println("SI!");
					this.channels.put(token[0], Float.valueOf(token[1]));
				}
				catch(NumberFormatException n) {
					System.err.println("Errore conversione numero, sara' aggiunto null");
					this.channels.put(token[0], null);
				}	
			}else
				System.out.println("NO");
		}
	
	}
	
	
	public synchronized Float getDato(String channelName) throws Exception {
		if(channels.containsKey(channelName))
			return channels.get(channelName);
		else 
			throw new Exception("Il canale richiesto non esite");
	}
	
	public HashMap<String,Float> getMapValue(){
		return this.channels;
	}
}
