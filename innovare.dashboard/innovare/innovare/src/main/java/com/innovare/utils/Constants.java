package com.innovare.utils;

import java.awt.Toolkit;
import java.text.SimpleDateFormat;

public class Constants {
	
	public static final String zipSourcePath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareData"+System.getProperty("file.separator")+"InnovareZip"+System.getProperty("file.separator");
	public static final String modelPath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareData"+System.getProperty("file.separator")+"InnovareModels"+System.getProperty("file.separator");
	

	public static final String MAX_WIDTH = (Toolkit.getDefaultToolkit().getScreenSize().width - 100) + "px";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
	
	public static final String erroreConnessione = "Impossibile recuperare i dati. Controlla la tua connessione";
	public static final String erroreDato = "ERRORE: il dato non è stato letto correttamente";
}