package com.innovare.model;

import java.sql.Timestamp;
import java.util.Comparator;

public class Sample implements Comparable<Sample>{
	
	
	
	private long timestamp;
	private float misure;	//per ora solo questo
	private String Channel;
	private String _id;
	
	public Sample() {
		this.timestamp= System.currentTimeMillis();
		/*
		 * Durante l'instanziamento del Sample, deve essere avviata la cattura delle informazioni 
		 * dai sensori, in modo da riempirne i della misura. Questo può ritardare durante l'instanziamento.
		 */
		this.misure=30;//solo per il testing
	}
	
	

	public Sample(long timestamp, float misure,String Channel) {
		super();
		this.timestamp = timestamp;
		this.misure = misure;
		this.Channel= Channel;
		//this._id= "generic";
	}

	

	public Sample(long timestamp, float misure, String channel, String _id) {
		super();
		this.timestamp = timestamp;
		this.misure = misure;
		Channel = channel;
		this._id = _id;
	}



	public String get_id() {
		return _id;
	}



	public void set_id(String _id) {
		this._id = _id;
	}



	public void setMisure(float misure) {
		this.misure = misure;
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

	
	

	public String getChannel() {
		return Channel;
	}



	public void setChannel(String channel) {
		Channel = channel;
	}



	@Override
	public String toString() {
		return "Sample [timestamp=" + timestamp + ", misure=" + misure + "]";
	}



	@Override
	public int compareTo(Sample o) {
		if(this.timestamp>o.getTimestamp())
			return 1;
		else 
			if(this.timestamp < o.getTimestamp())
				return -1;
			else
				return 0;
	}


	/*@Override
	public int compare(Sample o1, Sample o2) {
		//Confronto sui timestamp per ordinarli
		return o1.getTimestamp().compareTo(o2.getTimestamp());
		
	}*/


	
	
	
	
}