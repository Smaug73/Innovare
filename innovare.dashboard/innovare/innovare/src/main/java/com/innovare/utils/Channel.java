package com.innovare.utils;

public enum Channel {

	TEMPERATURE_AMB(1),
	HUMIDITY_AMB(2),
	TEMPERATURE_SUOLO(3),
	HUMIDITY_SUOLO(4),
	WIND(5),
	RAIN(6);

	private int value;

	Channel(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}