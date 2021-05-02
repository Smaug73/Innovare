package com.innovare.middleware;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import com.innovare.control.IrrigationController;

import io.vertx.ext.mongo.MongoClient;
import io.vertx.mqtt.MqttClient;

public class TestIrrigationController {

	public MqttClient irrigationComClient;
	public MongoClient mongC;
	
	
	
	@Test
	public void testDelayFromNewIrrigation() {
		String newIrrigationTimeString= "22:00";
		
		
		
		LocalTime nuovo = LocalTime.parse(newIrrigationTimeString);
		//System.out.println("test: "+nuovo.until(LocalTime.MAX,ChronoUnit.MILLIS));
		System.out.println("Tempo convertito : "+nuovo.toString());
		System.out.println("Tempo convertito : "+nuovo.toSecondOfDay()*1000);
		
		/*
		IrrigationController irr=new IrrigationController(mongC,irrigationComClient);
		System.out.println("Calcolo distanza temporale: "+irr.delayFromNewIrrigation(nuovo));
		
		System.out.println("Calcolo distanza temporale: in ore: "+(irr.delayFromNewIrrigation(nuovo)/1000)/3600);
		*/
	}
	
	
	
}
