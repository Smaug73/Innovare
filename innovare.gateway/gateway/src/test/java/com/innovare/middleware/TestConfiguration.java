package com.innovare.middleware;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

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
		
	}*/
	
	@Test
	public void provaLetturaStringa() {
		String pro= "prova = 1\nprova = 2\nprova = 3\nprova = 4\n";
		System.out.print(pro);
		String reg="[ =]+";
		String[] token=pro.split(reg);
		for(int i=0;i<token.length;i++)
			System.out.println(token[i]);
	}
	
	
}
