package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
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
	private void campionamentoFromFile() {
		File f= new File(Utilities.scriptWeatherPath+"test.txt");
		//Leggiamo riga per riga il file e aggiungiamo solo quando troviamo un sensore di quelli di interesse
		try {
			//Leggiamo tutto il file
			String fileString="";
			Scanner sc = new Scanner(f);
			while(sc.hasNext()) {
				fileString=fileString+sc.nextLine();
			}
			
			//Letto tutto il file facciamo uno split
			System.out.print(fileString);
			String reg="[ =]+";
			String[] token=fileString.split(reg);
			//debug
			for(int i=0;i<token.length;i++)
				System.out.println(token[i]);
			//Leggiamo per ogni canale il valore corrispondente
			//Avanziamo di due posizioni alla volta ed ogni volta che leggiamo un channel che e' presente lo aggiungo
			for(int j=0; j<token.length;j=j+2) {
				if(this.channelsNames.contains(token[j]))
					try {
						this.channels.put(token[j], Float.valueOf(token[j+1]));
					}
					catch(NumberFormatException n) {
						System.out.println("Errore conversione numero, sara' aggiunto null");
						this.channels.put(token[j], null);
					}
					
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("File non trovato");
			e.printStackTrace();
		}
	}
	
	//Aggiorna i dati della weatherstation da un processo
	private void campionamentoFromProcess() {}
	
	
	public synchronized Float getDato(String channelName) throws Exception {
		if(channels.containsKey(channelName))
			return channels.get(channelName);
		else 
			throw new Exception("Il canale richiesto non esite");
	}
	
}
