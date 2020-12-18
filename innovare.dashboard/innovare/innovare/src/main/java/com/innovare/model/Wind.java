package com.innovare.model;

import java.time.LocalDate;

public class Wind {

	private Direction direction;
	private LocalDate date;
	private double speed;
	
	
	public enum Direction {
		N("Nord"), 
		NNE("Nord-Nord-Est"), 
		NNW("Nord-Nord-Ovest"),
		NW("Nord-Ovest"),
		NE("Nord-Est"),
		W("Ovest"),
		WNW("Ovest-Nord-Ovest"),
		WSW("Ovest-Sud-Ovest"),
		E("Est"),
		ENE("Est-Nord-Est"),
		ESE("Est-Sud-Est"),
		S("Sud"), 
		SSE("Sud-Sud-Est"), 
		SSW("Sud-Sud-Ovest"),
		SW("Sud-Ovest"),
		SE("Sud-Est");
		

		private String name;

		Direction(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}


	public Wind(Direction direction, LocalDate date, double d) {
		super();
		this.direction = direction;
		this.date = date;
		this.speed = d;
	}


	public Direction getDirection() {
		return direction;
	}


	public LocalDate getDate() {
		return date;
	}


	public double getSpeed() {
		return speed;
	}
	
	
}
