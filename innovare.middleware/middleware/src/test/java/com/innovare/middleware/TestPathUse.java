package com.innovare.middleware;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.control.ModelController;
import com.innovare.model.ClassificationSint;
import com.innovare.model.ClassificationSint.Status;
import com.innovare.model.Model;

public class TestPathUse {
	
	
	
	public void testPathUsed() {
		File f= new File(System.getProperty("user.home")+System.getProperty("file.separator")+"InnovareScript"+System.getProperty("file.separator"));
		System.out.println(f.getAbsolutePath());
		//f.mkdir();
	}
	
	public void testClassSintJson() throws JsonProcessingException {
		ClassificationSint cs= new ClassificationSint(Status.NORMALE, LocalDate.now(), 8, 40, 52);
		System.out.println(cs.toString());
		String jsonConv = new ObjectMapper().writeValueAsString(cs);
		System.out.println(jsonConv);
		Model m = new Model("stub.h5");
		String jsonM = new ObjectMapper().writeValueAsString(m);
		System.out.println(jsonM);
	}

	@Test
	public void testModelController() {
		ModelController mc= new ModelController();
		for(Model m: mc.getAllModel())	
			System.out.println(m.getName());
		assertTrue(mc.foundModel("stub.h5"));
	}
	
}
