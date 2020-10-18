package com.innovare.model;

import java.sql.Timestamp;

public class Sample {
	
	
	private String timestamp;
	private int misure;	//per ora solo questo
	
	public Sample() {
		this.timestamp= (new Timestamp(System.currentTimeMillis())).toString();
		this.misure=30;
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

	@Override
	public String toString() {
		return "Sample [timestamp=" + timestamp + ", misure=" + misure + "]";
	}
	
	
	
}
