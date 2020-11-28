package com.innovare.middleware;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.control.Classificator;
import com.innovare.model.PlantClassification;


public class TestClassificator {
	
	private String modelName="stub.h5";
	public static final String dirName="test";
	//private String dirImages="test.zip";
	private ArrayList<PlantClassification> classifications;
	//private String jsonString="[{\"date\": \"2020/09/11\", \"path\": \"/home/stefano/Scrivania/Lavoro/IMMAGINI-TestPythonScript/test/2020_09_11_Dataset_FondatanaDeiFieri-Thermal_Optical-Fila_1_2-Session1-DJI_0455-plant_4.jpg\", \"hash\": \"a91f820cc32c8266445bc97974b0aee9\", \"classification\": {\"classifications\": [{\"classe\": \"0\", \"score\": 23.196931183338165}, {\"classe\": \"1\", \"score\": 23.196931183338165}, {\"classe\": \"2\", \"score\": 23.196931183338165}, {\"classe\": \"3\", \"score\": 23.196931183338165}, {\"classe\": \"4\", \"score\": 23.196931183338165}, {\"classe\": \"5\", \"score\": 23.196931183338165}, {\"classe\": \"6\", \"score\": 23.196931183338165}, {\"classe\": \"7\", \"score\": 23.196931183338165}, {\"classe\": \"8\", \"score\": 23.196931183338165}, {\"classe\": \"9\", \"score\": 23.196931183338165}], \"hash\": \"'a91f820cc32c8266445bc97974b0aee9'\"}, \"originalImage\": \"/home/stefano/Scrivania/Lavoro/IMMAGINI-TestPythonScript/test/2020_09_11_Dataset_FondatanaDeiFieri-Thermal_Optical-Fila_1_2-Session1-DJI_0455-erased_background_with_boxes.jpg\"}]";
	
	/*
	 * Fare molta attenzione con il matching tra nome variabili nel json e nelle classi java
	 * Un incongruenza fa fallire il mapping.
	 */
	
	/*@BeforeEach
	public void createListJson() {
		
		classifications = new ArrayList<PlantClassification>();
		try {
			
			classifications= new ObjectMapper().readValue(jsonString, new TypeReference<ArrayList<PlantClassification>>(){});
			System.out.println(classifications.toString());
		} catch (JsonProcessingException e) {
			System.err.println("Errore creazione oggetto nel Before");
			e.printStackTrace();
		}
	}*/

	/*@Test
	public void testOne() {
		//Creo un nuovo Classificator
		Classificator c= new Classificator(this.dirZip);
		ArrayList<PlantClassification> listOfClassification;
		try {
			listOfClassification = c.newClassification(this.modelName);
			System.out.println("Test: "+listOfClassification);
			assertTrue(listOfClassification.size() == 2);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	@Test
	public void testDecompressioneAndClassificazione() {
		Classificator c= new Classificator(this.dirName);
		ArrayList<PlantClassification> listOfClassification;
		try {
			c.unZipFile();
			listOfClassification = c.newClassification(this.modelName);
			System.out.println("Test: "+listOfClassification);
			assertTrue(listOfClassification.size() == 2);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	@AfterAll
	public static void eliminateFiles() {
		File f= new File(System.getProperty("user.home")+"/InnovareImages/"+TestClassificator.dirName);
		if(f.exists())
			eliminateRecursive(f);
	}
	
	public static void eliminateRecursive(File file) {
		if(file.isDirectory()) {
			File[] subFiles= file.listFiles();
			for(File subF:subFiles) {
				eliminateRecursive(subF);
			}
			file.delete();
		}
		else {
			file.delete();
			return;
		}
		
	}
	
		
}
