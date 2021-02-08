package com.innovare.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Viene instanziata direttamente caricando il file configuration.json che si deve trovare nella directory InnovareData
 */
public class UtilitiesLoad {

	public String ipMqtt;
	public String ipMiddlelayer;
	public String ipDashboard;
	public double sogliaClassificazione;
	public static final String configurationJsonPath=System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareData"+System.getProperty("file.separator")+"configuration.json";
	
	public UtilitiesLoad() {
		
	}
	
	
	public static UtilitiesLoad UtilitiesLoadFromJson() {
		File confJsonFile= new File(configurationJsonPath);
		
		//Dopo aver letto il json lo convertiamo in una classe
		try {
			
			Scanner reader= new Scanner(confJsonFile);
			String jsonStrings="";
			//Leggiamo tutto il json
			while(reader.hasNextLine()) {
				jsonStrings= jsonStrings+reader.nextLine().toString();
			}
			reader.close();
			return new ObjectMapper().readValue(jsonStrings, UtilitiesLoad.class);
			
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			System.err.println("ERRORE: parsing json non riuscito.");
			e.printStackTrace();
			System.err.println("Impostazione configurazione base...");
			return UtilitiesLoadFromBaseConfiguration();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			System.err.println("ERRORE: parsing json non riuscito.");
			e.printStackTrace();
			System.err.println("Impostazione configurazione base...");
			return UtilitiesLoadFromBaseConfiguration();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("ERRORE: file configuration.json non trovato!");
			e.printStackTrace();
			System.err.println("Impostazione configurazione base...");
			return UtilitiesLoadFromBaseConfiguration();
		}
			
	}

	public static UtilitiesLoad UtilitiesLoadFromBaseConfiguration() {
		UtilitiesLoad ul = new UtilitiesLoad();
		ul.setIpDashboard(Utilities.ipDashBoad);
		ul.setIpMiddlelayer(Utilities.ipMiddleLayer);
		ul.setIpMqtt(Utilities.ipMqtt);
		ul.setSogliaClassificazione(Utilities.sogliaClassificazione);
		return ul;
	}
	
	
	public String getIpMqtt() {
		return ipMqtt;
	}


	public void setIpMqtt(String ipMqtt) {
		this.ipMqtt = ipMqtt;
	}


	public String getIpMiddlelayer() {
		return ipMiddlelayer;
	}


	public void setIpMiddlelayer(String ipMiddlelayer) {
		this.ipMiddlelayer = ipMiddlelayer;
	}


	public String getIpDashboard() {
		return ipDashboard;
	}


	public void setIpDashboard(String ipDashboard) {
		this.ipDashboard = ipDashboard;
	}


	public double getSogliaClassificazione() {
		return sogliaClassificazione;
	}


	public void setSogliaClassificazione(double sogliaClassificazione) {
		this.sogliaClassificazione = sogliaClassificazione;
	}


	@Override
	public String toString() {
		return "UtilitiesLoad [ipMqtt=" + ipMqtt + ", ipMiddlelayer=" + ipMiddlelayer + ", ipDashboard=" + ipDashboard
				+ ", sogliaClassificazione=" + sogliaClassificazione + "]";
	}
	
	
	
	
}
