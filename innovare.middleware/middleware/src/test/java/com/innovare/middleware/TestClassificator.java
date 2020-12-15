package com.innovare.middleware;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.control.Classificator;
import com.innovare.model.ClassificationSint;
import com.innovare.model.PlantClassification;
import com.innovare.utils.Utilities;


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
	
	/*@Test
	public void testDecompressioneAndClassificazione() {
		Classificator c= new Classificator("testDataset.zip");
		ArrayList<PlantClassification> listOfClassification;
		try {
			listOfClassification=c.unZipAndClassification(this.modelName);
			System.out.println("Test: "+listOfClassification);
			//assertTrue(listOfClassification.size() == 2);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	/*@Test
	public void testDecompressioneAndClassificazione() {
		DateTimeFormatter formatter= DateTimeFormatter.ofPattern("yy-MM-dd");
		LocalDate date= LocalDate.parse("20-08-31",formatter);
		System.out.println(date.toString());
	}*/
	
	@Test
	public void testClassificationSint() throws JsonMappingException, JsonProcessingException {
		String plantClassificationArray="[{\"originalImage\": \"/home/stefano/InnovareData/InnovareSegmentDS/testDataset/020_08_31_Thermal_Optical-Fila_3_4-Session1-DJI_1234-erased_background_with_boxes.jpg\", \"hash\": \"e1a80286757bde4e942d4003cd4a2904\", \"classification\": {\"classifications\": [{\"classe\": \"Pianta Normale\", \"score\": 8.533674478530884}, {\"classe\": \"Carenza di acqua\", \"score\": 8.533674478530884}, {\"classe\": \"Eccesso di acqua\", \"score\": 8.533674478530884}, {\"classe\": \"Infestanti\", \"score\": 8.533674478530884}, {\"classe\": \"Ambigua\", \"score\": 8.533674478530884}], \"hash\": \"'e1a80286757bde4e942d4003cd4a2904'\"}, \"date\": \"20-08-31\", \"path\": \"/home/stefano/InnovareData/InnovareSegmentDS/testDataset/020_08_31_Thermal_Optical-Fila_3_4-Session1-DJI_1234-plant_0.jpg\", \"model\": \"stub.h5\"}, {\"originalImage\": \"/home/stefano/InnovareData/InnovareSegmentDS/testDataset/020_08_31_Thermal_Optical-Fila_3_4-Session1-DJI_1234-erased_background_with_boxes.jpg\", \"hash\": \"e047efb93b7b4508c364c56477f44990\", \"classification\": {\"classifications\": [{\"classe\": \"Pianta Normale\", \"score\": 8.533674478530884}, {\"classe\": \"Carenza di acqua\", \"score\": 8.533674478530884}, {\"classe\": \"Eccesso di acqua\", \"score\": 8.533674478530884}, {\"classe\": \"Infestanti\", \"score\": 8.533674478530884}, {\"classe\": \"Ambigua\", \"score\": 8.533674478530884}], \"hash\": \"'e047efb93b7b4508c364c56477f44990'\"}, \"date\": \"20-08-31\", \"path\": \"/home/stefano/InnovareData/InnovareSegmentDS/testDataset/020_08_31_Thermal_Optical-Fila_3_4-Session1-DJI_1234-plant_1.jpg\", \"model\": \"stub.h5\"}]";
		
		ArrayList<PlantClassification> pc=  new ObjectMapper().readValue(plantClassificationArray, new TypeReference<ArrayList<PlantClassification>>(){});
		System.out.println("Piante da classificare: "+pc.toString());
		ClassificationSint cs= new ClassificationSint(pc);
		System.out.println("Classificazione sintetica: "+cs.toString());
	}
	
	/*
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
		
	}*/
	
		
}
