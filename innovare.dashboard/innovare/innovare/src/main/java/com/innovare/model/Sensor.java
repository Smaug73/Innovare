package com.innovare.model;
public class Sensor {
	private String name;
	private Sample sample;
	
	public Sensor(String name, Sample sample) {
		super();
		this.name = name;
		this.sample = sample;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Sample getSample() {
		return sample;
	}

}
