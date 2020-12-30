package com.innovare.model;

import java.sql.Timestamp;

import com.innovare.utils.Direction;

public class Wind {
	private Direction direction;
	private Timestamp date;
	private float speed;
	
	public Wind(Direction direction, Timestamp date, float speed) {
		this.direction = direction;
		this.date = date;
		this.speed = speed;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	

}
