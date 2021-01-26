package com.innovare.utils;

public enum Channel {
	/*
	public static final String[] channelsNames= {"rtBaroCurr","rtInsideTemp","rtInsideHum","rtOutsideTemp",
			"rtWindSpeed","rtWindAvgSpeed","rtWindDir","rtOutsideHum","rtRainRate","rtUVLevel","rtSolarRad", 
			"rtDayRain","rtMonthRain","rtYearRain"};
	*/
	
	BARO_CURR(0, "rtBaroCurr", -1),
	INSIDE_TEMP(1, "rtInsideTemp", (float) -273.15),
	INSIDE_HUM(2, "rtInsideHum", -1),
	OUTSIDE_TEMP(3, "rtOutsideTemp", (float) -273.15),
	WIND_SPEED(4, "rtWindSpeed", -1),
	AVG_WIND_SPEED(5, "rtWindAvgSpeed", -1),
	WIND_DIR(6, "rtWindDir", -1),
	OUTSIDE_HUM(7, "rtOutsideHum", -1),
	RAIN_RATE(8, "rtRainRate", -1),
	UV_LEVEL(9, "rtUVLevel", -1),
	SOLAR_RAD(10, "rtSolarRad", -1),
	DAY_RAIN(11, "rtDayRain", -1),
	MONTH_RAIN(12, "rtMonthRain", -1),
	YEAR_RAIN(13, "rtYearRain", -1);

	private int value;
	private String name;
	private float invalidValue;

	Channel(int value, String name, float invalidValue) {
		this.value = value;
		this.name = name;
		this.invalidValue = invalidValue;
	}

	public int getValue() {
		return value;
	}
	
	public String getName() {
		return name;
	}

	public float getInvalidValue() {
		return invalidValue;
	}
	
}