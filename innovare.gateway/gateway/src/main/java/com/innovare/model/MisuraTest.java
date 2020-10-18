package com.innovare.model;


import java.sql.Timestamp;

public class MisuraTest {
	
	//private Timestamp timestamp;
	private String timestamp;
	private int temperature;
	private int wind;
	private int humidity;
	private double rain;
	
	
	//public MisuraTest() {}
	
	public MisuraTest() {
		this.timestamp= (new Timestamp(System.currentTimeMillis())).toString();
		this.temperature=30;
		this.wind=50;
		this.humidity=50;
		this.rain=327.68;		
	}
	
	
	
	
	/* 
	 //TEST JSON
	public static void main(String[] argv) {
		MisuraTest m= new MisuraTest();
		
		try {
			//Per trasformare l'oggetto in unsa stringa json
			String misure= new ObjectMapper().writeValueAsString(m);
			System.out.println(misure);
			
			//Per trasformare la stringa json in un oggetto
			MisuraTest misDecodeFromJson= new ObjectMapper().readValue(misure, MisuraTest.class);
			System.out.println(misDecodeFromJson.toString());
		} catch (JsonProcessingException e) {
			System.err.println("Misura non correttamente convertita in json!");
			e.printStackTrace();
		}
	}
	*/



	public String getTimestamp() {
		return timestamp;
	}




	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}




	public int getTemperature() {
		return temperature;
	}




	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}




	public int getWind() {
		return wind;
	}




	public void setWind(int wind) {
		this.wind = wind;
	}




	public int getHumidity() {
		return humidity;
	}




	public void setHumidity(int humidity) {
		this.humidity = humidity;
	}




	public double getRain() {
		return rain;
	}




	public void setRain(double rain) {
		this.rain = rain;
	}





	@Override
	public String toString() {
		return "MisuraTest [timestamp=" + timestamp + ", temperature=" + temperature + ", wind=" + wind + ", humidity="
				+ humidity + ", rain=" + rain + "]";
	}
	
	

}
