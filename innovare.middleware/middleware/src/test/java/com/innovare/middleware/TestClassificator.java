package com.innovare.middleware;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.control.Classificator;
import com.innovare.model.Classification;
import com.innovare.model.PlantClassification;
import com.innovare.model.Result;


public class TestClassificator {
	
	private String modelName="stub.h5";
	private String pathImages="/home/stefano/Scrivania/Lavoro/IMMAGINI-TestPythonScript/test/";
	private ArrayList<PlantClassification> classifications;
	private String jsonString="[{\"date\": \"2020/09/11\", \"path\": \"/home/stefano/Scrivania/Lavoro/IMMAGINI-TestPythonScript/test/2020_09_11_Dataset_FondatanaDeiFieri-Thermal_Optical-Fila_1_2-Session1-DJI_0455-plant_4.jpg\", \"hash\": \"a91f820cc32c8266445bc97974b0aee9\", \"classification\": {\"classifications\": [{\"classe\": \"0\", \"score\": 23.196931183338165}, {\"classe\": \"1\", \"score\": 23.196931183338165}, {\"classe\": \"2\", \"score\": 23.196931183338165}, {\"classe\": \"3\", \"score\": 23.196931183338165}, {\"classe\": \"4\", \"score\": 23.196931183338165}, {\"classe\": \"5\", \"score\": 23.196931183338165}, {\"classe\": \"6\", \"score\": 23.196931183338165}, {\"classe\": \"7\", \"score\": 23.196931183338165}, {\"classe\": \"8\", \"score\": 23.196931183338165}, {\"classe\": \"9\", \"score\": 23.196931183338165}], \"hash\": \"'a91f820cc32c8266445bc97974b0aee9'\"}, \"originalImage\": \"/home/stefano/Scrivania/Lavoro/IMMAGINI-TestPythonScript/test/2020_09_11_Dataset_FondatanaDeiFieri-Thermal_Optical-Fila_1_2-Session1-DJI_0455-erased_background_with_boxes.jpg\"}]";
	
	/*
	 * Fare molta attenzione con il matching tra nome variabili nel json e nelle classi java
	 * Un incongruenza fa fallire il mapping.
	 */
	
	@BeforeEach
	public void createListJson() {
		
		classifications = new ArrayList<PlantClassification>();
		try {
			
			classifications= new ObjectMapper().readValue(jsonString, new TypeReference<ArrayList<PlantClassification>>(){});
			System.out.println(classifications.toString());
		} catch (JsonProcessingException e) {
			System.err.println("Errore creazione oggetto nel Before");
			e.printStackTrace();
		}
	}

	@Test
	public void testOne() {
		//Creo un nuovo Classificator
		Classificator c= new Classificator(this.pathImages, this.modelName);
		ArrayList<PlantClassification> listOfClassification= c.newClassification();
		System.out.println("Test: "+listOfClassification);
		assertTrue(listOfClassification.size() == classifications.size());
		
	}
	
	
}
