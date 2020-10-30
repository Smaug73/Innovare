package com.innovare.model;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

public class Channel extends Thread{
	
	/*
	 * Deve contenere una queue dei sample raccolti.
	 * Il channel identifica un tipo di misurazione effettuata
	 * 
	 * Potrebbe essere aggiunta una condizione per fermare il thread di campionamento.
	 * Nel caso reale ci deve essere una sola misura all'interno della queue, dato che il campionamento risulta
	 * molto lento, quindi la queue si riempirà solo nel momento nel quale il channel non sarà costantemente liberato.
	 */	
	private String ID; //Scritto cosi' altrimenti si sovrappone all'id del thread
	private long periodo;	//Periodo di campionamento
	private PriorityQueue<Sample> samplesQueue=null;
	
	
	
	public Channel(String id, long periodo) {
		super();
		this.periodo=periodo;//calcolo dei secondi di attesa tra un campionamento e l'altro
		this.ID = id;
		this.samplesQueue = new PriorityQueue<Sample>();
	}
	
	public Channel(String id, long periodo, PriorityQueue<Sample> samples) {
		super();
		this.periodo= periodo;
		this.ID = id;
		this.samplesQueue = samples;
	}
	
	
	/*
	 * Una volta che il Thread viene avviato dovrà generare in modo periodico dei sample ed aggiungerli alla coda
	 * Ogni qualvolta un elemento viene inviato, deve essere tolto dalla coda.
	 * Il campionamento consiste in un ciclo while ed una wait.
	 */
	public void run() {
		
		while(true) {
			if(this.samplesQueue == null)
				this.samplesQueue = new PriorityQueue<Sample>();
			
			try {
				this.addNewSample();
				this.sleep(periodo);
				//this.wait(this.periodo);
			} catch (Exception e) {		
				e.printStackTrace();
			}
			
			try {
				this.sleep(periodo);
				//this.wait(this.periodo);
			} catch (Exception e) {		
				e.printStackTrace();
			}

		}
				
	}
	
	

	/*
	 * Ci deve essere mutua escluzione nell'andare ad aggiunere nuovi Sample(in scrittura)
	 */
	private synchronized void addNewSample() throws Exception {
		if(this.samplesQueue!=null)
			this.samplesQueue.add(new Sample(this.ID));
		else
			throw new Exception("SampleQueue non instanziata");
	}
	
	
	/*
	 * Ritorniamo il primo Sample della Queue e lo eliminiamo
	 */
	public synchronized ArrayList<Sample> getNewSample() throws NoSuchElementException{
		if(this.samplesQueue!=null)
			if(!this.samplesQueue.isEmpty()) {	//caso nel quale la queue non è vuota
				ArrayList<Sample> newMisures= new ArrayList<Sample>();
				while(!this.samplesQueue.isEmpty()) {
					newMisures.add(this.samplesQueue.remove());
				}
				return newMisures;	//converto in array per semplificarne la conversione in json
			}
			else throw  new NoSuchElementException("Queue vuota");	//caso coda vuota	
		else
			throw new NoSuchElementException("SampleQueue non instanziata");
	}
	
	

	@Override
	public String toString() {
		return "Channel [ID=" + ID + ", periodo=" + periodo + "]";
	}

	public String getID() {
		return this.ID;
	}
	
	public long getPeriodo() {
		return this.periodo;
	}

}
