package com.innovare.model;

import java.sql.Timestamp;
import java.util.Random;

public class Sample implements Comparable<Sample>{
	
	/*
	 * SuperClasse Sample per la creazione di specifici sample
	 * Utilizzata per testare, in seguitò diventerà abstract e saranno create classi
	 * specifiche per uno specifico sample.
	 */
	
	private long timestamp;
	private String channel="defaultValue";
	private float misure;	//per ora solo questo
	
	public Sample() {
		this.timestamp= System.currentTimeMillis();
		/*
		 * Durante l'instanziamento del Sample, deve essere avviata la cattura delle informazioni 
		 * dai sensori, in modo da riempirne i della misura. Questo può ritardare durante l'instanziamento.
		 */
		Random random = new Random();
		this.misure=random.nextFloat()*45;//solo per il testing
	}
	
	public Sample(String channel) {
		this.timestamp= System.currentTimeMillis();
		this.channel=channel;
		/*
		 * Durante l'instanziamento del Sample, deve essere avviata la cattura delle informazioni 
		 * dai sensori, in modo da riempirne i della misura. Questo può ritardare durante l'instanziamento.
		 */
		Random random = new Random();
		this.misure=random.nextFloat()*45;//solo per il testing
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public float getMisure() {
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
		if(this.getTimestamp()> o.getTimestamp())
			return 1;
		else if(this.getTimestamp()== o.getTimestamp())
			return 0;
		else return -1;
	}
	
	
	
}
