package com.innovare.middleware;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.control.WeatherStationController;
import com.innovare.model.Sample;
import com.innovare.utils.Utilities;
import com.innovare.utils.UtilitiesLoad;

public class TestConfiguration {

	/*@Test
	public void configurationFromJsonFile() {
		UtilitiesLoad cl= UtilitiesLoad.UtilitiesLoadFromJson();
		System.out.println(cl.toString());
	}
	
	@Test
	public void avvioProcessoWeatherStation() throws IOException {
		System.out.println(Utilities.scriptPath+" ./vproweather -m /dev/ttyUSB0");
		Process processSeg= Runtime.getRuntime().exec(Utilities.scriptPath+" ./vproweather -m /dev/ttyUSB0");
		
	}
	
	//@Test
	public void provaLetturaStringa() {
		String pro= "prova = 1\nprova = 2\nprova = 3\nprova = 4\n";
		System.out.print(pro);
		String reg="[ =]+";
		String[] token=pro.split(reg);
		for(int i=0;i<token.length;i++)
			System.out.println(token[i]);
	}
	
	
	@Test
	public void testWeatherstation() throws JsonProcessingException {
		ArrayList<Sample> newSamples= new ArrayList<Sample>();
		newSamples.add(new Sample());
		newSamples.add(new Sample());
		newSamples.add(new Sample());
		newSamples.add(new Sample());
		System.out.println(new ObjectMapper().writeValueAsString(newSamples));
		
	}
	*/
}
