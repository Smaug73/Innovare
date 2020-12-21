package com.innovare.middleware;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.innovare.utils.Utilities;
import com.innovare.utils.UtilitiesLoad;

public class TestConfiguration {

	@Test
	public void configurationFromJsonFile() {
		UtilitiesLoad cl= UtilitiesLoad.UtilitiesLoadFromJson();
		System.out.println(cl.toString());
	}
	
	@Test
	public void avvioProcessoWeatherStation() throws IOException {
		System.out.println(Utilities.scriptPath+" ./vproweather -m /dev/ttyUSB0");
		Process processSeg= Runtime.getRuntime().exec(Utilities.scriptPath+" ./vproweather -m /dev/ttyUSB0");
		
	}
	
	
	
}
