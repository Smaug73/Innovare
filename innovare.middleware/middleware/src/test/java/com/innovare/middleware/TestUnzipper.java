package com.innovare.middleware;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;


import com.innovare.control.Classificator;
import com.innovare.model.PlantClassification;


public class TestUnzipper {
	
	
	private String modelName="stub.h5";
	public static final String dirName="test";
	private ArrayList<PlantClassification> classifications;
	
	@Test
	public void testDecompressione() {
		//Creo un nuovo Classificator
		boolean succ=false;
		
		Classificator c= new Classificator(this.dirName);
		try {
			c.unZipFile();
			succ=true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(succ);	
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
