package com.innovare.middleware;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.innovare.control.ConfigurationController;
import com.innovare.control.WeatherStationController;
import com.innovare.gateway.GatewayVerticle;
import com.innovare.model.Sample;
import com.innovare.utils.Utilities;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {
	/*
  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new GatewayVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
    testContext.completeNow();
  }
  
  
	
  @Test
  public void testWeatherStation() {
	  WeatherStationController wsc= new WeatherStationController();
	  wsc.campionamentoFromFileFromProcess();
	  
	  for(String key :wsc.getChannelsSample().keySet()) {
		  System.out.println("chiave: "+key+" sample"+wsc.getChannelsSample().get(key).toString());
	  }
	  
  }
  
  @Test 
  public void weatherstationREadRoutin() throws IOException, InterruptedException {
	//Lanciamo il processo e salviamo nel file sample.txt
		Process processSt= Runtime.getRuntime().exec("./vproweather -x /dev/ttyUSB0 >> sample.txt",null,new File(Utilities.scriptWeatherPath));
		//attendiamo file del processo
		int processSegOutput=processSt.waitFor();
		//leggiamo il file prodotto
		File f= new File(Utilities.scriptWeatherPath+"sample.txt");
		//Leggiamo tutto il file
		String fileString="";
		Scanner sc = new Scanner(f);
		while(sc.hasNext()) {
			fileString=fileString+sc.nextLine()+"\n";
		}
		sc.close();
		
		//Letto tutto il file facciamo uno split
		System.out.println("\nFILE:");//debug
		System.out.println(fileString);//debug
  }
  
	
  @Test
  public void weatherCampFromFile() throws IOException {
	  System.out.println("TEST");
	  WeatherStationController wsc= new WeatherStationController();
	  wsc.campionamentoFromProcess();
	  
	  for(String key :wsc.getChannelsSample().keySet()) {
		  System.out.println("chiave: "+key+" sample"+wsc.getChannelsSample().get(key).toString());
	  }
	
  }
  
  */
  
  @Test
  public void configurationTest() {
	ConfigurationController cc= new ConfigurationController();
	System.out.println(cc.getIpMiddleLayer());
	System.out.println(cc.getTempoCampionamentoWS());

  }
  
  
  
  
  
  
}
