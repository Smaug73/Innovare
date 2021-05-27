package com.innovare.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;

/*
 * Classe per la gestione dei dati di log
 */
public class Logger {

	//I dati di log sono scritti nel file timestamp-middlelayerLog.log nella directory InnovareData
	//Viene generato un nuovo file di log all'avvio
	private static String pathLogFile=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareData"+System.getProperty("file.separator")+"InnovareLog"+System.getProperty("file.separator");
	private static Logger l=null;
	private PrintStream ps;
	private File fileLog;
	
	private Logger() throws FileNotFoundException {
		//In fase di creazione generiamo il nuovo file di log
		Timestamp tm= new Timestamp(System.currentTimeMillis());
		this.fileLog= new File(this.pathLogFile+tm.toString()+"-middlelayerLog.log");
		try {
			this.fileLog.createNewFile();
		} catch (IOException e) {
			//Il file gia' esiste
			System.out.println("File di log gia' esistente");
			e.printStackTrace();
		}	
		this.ps= new PrintStream(this.fileLog);
		
	}
	
	public static Logger getLogger() {
		if(l==null) {
			try {
				l= new Logger();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}	
		}
		return l;
	}
	
	//Scriviamo una stringa all'interno del file di log
	public synchronized void print(String s) {
		Timestamp tm= new Timestamp(System.currentTimeMillis());
		this.ps.append(tm.toString()+" "+s+"\n");
		System.out.println(tm.toString()+" "+s+"\n");
	}
	
}
