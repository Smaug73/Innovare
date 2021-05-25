package com.innovare.model;
public class Sensor {
	private int name;
	private Sample sample;
	
	public Sensor(int name, Sample sample) {
		super();
		this.name = name;
		this.sample = sample;
	}

	public int getName() {
		return name;
	}

	public void setName(int name) {
		this.name = name;
	}

	public Sample getSample() {
		return sample;
	}

}
