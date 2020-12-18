package com.innovare.model;

import java.sql.Timestamp;

public class Sensor {
	private String name;
	private double temperature;
	private double humidity;
	private Timestamp date;
	
	public enum Char {
		TEMPERATURE("Temperatura"),
		HUMIDITY("Umidità");

		private String name;

		Char(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	
	public Sensor(String name, double temperature, double humidity, Timestamp date) {
		super();
		this.name = name;
		this.temperature = temperature;
		this.humidity = humidity;
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public double getHumidity() {
		return humidity;
	}

	public void setHumidity(double humidity) {
		this.humidity = humidity;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}
	
	

}
