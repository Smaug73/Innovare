package com.innovare.utils;

public enum Channel {
	/*
	public static final String[] channelsNames= {"rtBaroCurr","rtInsideTemp","rtInsideHum","rtOutsideTemp",
			"rtWindSpeed","rtWindAvgSpeed","rtWindDir","rtOutsideHum","rtRainRate","rtUVLevel","rtSolarRad", 
			"rtDayRain","rtMonthRain","rtYearRain"};
	*/
	
	BARO_CURR(0, "rtBaroCurr"),
	INSIDE_TEMP(1, "rtInsideTemp"),
	INSIDE_HUM(2, "rtInsideHum"),
	OUTSIDE_TEMP(3, "rtOutsideTemp"),
	WIND_SPEED(4, "rtWindSpeed"),
	AVG_WIND_SPEED(5, "rtWindAvgSpeed"),
	WIND_DIR(6, "rtWindDir"),
	OUTSIDE_HUM(7, "rtOutsideHum"),
	RAIN_RATE(8, "rtRainRate"),
	UV_LEVEL(9, "rtUVLevel"),
	SOLAR_RAD(10, "rtSolarRad"),
	DAY_RAIN(11, "rtDayRain"),
	MONTH_RAIN(12, "rtMonthRain"),
	YEAR_RAIN(13, "rtYearRain");

	private int value;
	private String name;

	Channel(int value, String name) {
		this.value = value;
		this.name = name;
	}

	public int getValue() {
		return value;
	}
	
	public String getName() {
		return name;
	}
}