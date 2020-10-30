package com.innovare.model;

import java.sql.Timestamp;
import java.util.Comparator;

public class Sample implements Comparable<Sample>{
	
	/*
	 * SuperClasse Sample per la creazione di specifici sample
	 * Utilizzata per testare, in seguitò diventerà abstract e saranno create classi
	 * specifiche per uno specifico sample.
	 */
	
	private String timestamp;
	private String channel="defaultValue";
	private int misure;	//per ora solo questo
	
	public Sample() {
		this.timestamp= (new Timestamp(System.currentTimeMillis())).toString();
		/*
		 * Durante l'instanziamento del Sample, deve essere avviata la cattura delle informazioni 
		 * dai sensori, in modo da riempirne i della misura. Questo può ritardare durante l'instanziamento.
		 */
		this.misure=30;//solo per il testing
	}
	
	public Sample(String channel) {
		this.timestamp= (new Timestamp(System.currentTimeMillis())).toString();
		this.channel=channel;
		/*
		 * Durante l'instanziamento del Sample, deve essere avviata la cattura delle informazioni 
		 * dai sensori, in modo da riempirne i della misura. Questo può ritardare durante l'instanziamento.
		 */
		this.misure=30;//solo per il testing
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public int getMisure() {
		return misure;
	}

	public void setMisure(int misure) {
		this.misure = misure;
	}
	
	
	public String getChannel() {
		return channel;
	}


	public void setChannel(String channel) {
		this.channel = channel;
	}


	@Override
	public String toString() {
		return "Sample [timestamp=" + timestamp + ", channel=" + channel + ", misure=" + misure + "]";
	}


	@Override
	public int compareTo(Sample o) {
		//Confronto sui timestamp per ordinarli
			return this.getTimestamp().compareTo(o.getTimestamp());
	}
	
	
	
}
