package com.innovare.utils;

public final class Utilities {

	public static final String scriptPath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareData"+System.getProperty("file.separator")+"InnovareScript"+System.getProperty("file.separator");
	public static final String datasetPath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareData"+System.getProperty("file.separator")+"InnovareImages"+System.getProperty("file.separator");
	public static final String zipSourcePath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareData"+System.getProperty("file.separator")+"InnovareZip"+System.getProperty("file.separator");
	public static final String modelPath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareData"+System.getProperty("file.separator")+"InnovareModels"+System.getProperty("file.separator");
	public static final String segmentDataSetPath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareData"+System.getProperty("file.separator")+"InnovareSegmentDS"+System.getProperty("file.separator");
	public static final String sensorDocumentPath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareData"+System.getProperty("file.separator")+"InnovareSensorFTP"+System.getProperty("file.separator");
	public static final String ipMqtt="localhost";
	public static final String irrigationLogMqttChannel="Irrigation-LOG";
	public static final String irrigationCommandMqttChannel="Irrigation-COMMAND";
	public static final String stateOff="OFF";
	public static final String stateOn="ON";
	public static final double sogliaClassificazione=70.0;
	public static final String classeSane="Pianta Normale";
	public static final String classeAmbigue="Ambigua";
	public static final String classeEccesso="Eccesso di acqua";
	public static final String classeInfestanti="Infestanti";
	public static final String classeCarenza="Carenza di acqua";
	public static final String[] channelsNames= {"rtBaroCurr","rtInsideTemp","rtInsideHum","rtOutsideTemp","rtWindSpeed","rtWindAvgSpeed","rtWindDir","rtOutsideHum","rtRainRate","rtUVLevel","rtSolarRad","rtDayRain","rtMonthRain","rtYearRain"};
	public static final int hourStartIrrigation=9;
	public static final int minuteStartIrrigation=30;
	public static final float portata=1;//1 litro al minuto
	public static final String channelCSV="Umidita' Terreno";
}
