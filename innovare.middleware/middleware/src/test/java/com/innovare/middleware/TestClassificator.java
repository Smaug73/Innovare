package com.innovare.middleware;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.control.Classificator;
import com.innovare.control.ConfigurationController;
import com.innovare.control.IrrigationController;
import com.innovare.control.Logger;
import com.innovare.control.ModelController;
import com.innovare.control.SampleCSVController;
import com.innovare.control.SensorDataController;
import com.innovare.control.SensorDataControllerSync;
import com.innovare.model.ClassificationSint;
import com.innovare.model.Evapotranspiration;
import com.innovare.model.Irrigazione;
import com.innovare.model.PlantClassification;
import com.innovare.model.Sample;
import com.innovare.utils.Utilities;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.mqtt.MqttClient;
import net.lingala.zip4j.exception.ZipException;


public class TestClassificator {
	
	private String modelName="stub.h5";
	public static final String dirName="test";
	//private String dirImages="test.zip";
	private ArrayList<PlantClassification> classifications;
	private MqttClient irrigationCommandClient;
	private MongoClient mongoClient;
	
	
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
			Logger.getLogger().print(classifications.toString());
		} catch (JsonProcessingException e) {
			Logger.getLogger().print("Errore creazione oggetto nel Before");
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
			Logger.getLogger().print("Test: "+listOfClassification);
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
			Logger.getLogger().print("Test: "+listOfClassification);
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
		Logger.getLogger().print(date.toString());
	}*/
	
	/*@Test
	public void testClassificationSint() throws JsonMappingException, JsonProcessingException {
		String plantClassificationArray="[{\"originalImage\": \"/home/stefano/InnovareData/InnovareSegmentDS/testDataset/2020_08_31_Thermal_Optical-Fila_3_4-Session1-DJI_1234-erased_background_with_boxes.jpg\", \"hash\": \"e1a80286757bde4e942d4003cd4a2904\", \"classification\": {\"classifications\": [{\"classe\": \"Pianta Normale\", \"score\": 8.533674478530884}, {\"classe\": \"Carenza di acqua\", \"score\": 8.533674478530884}, {\"classe\": \"Eccesso di acqua\", \"score\": 8.533674478530884}, {\"classe\": \"Infestanti\", \"score\": 8.533674478530884}, {\"classe\": \"Ambigua\", \"score\": 8.533674478530884}], \"hash\": \"'e1a80286757bde4e942d4003cd4a2904'\"}, \"date\": \"20-08-31\", \"path\": \"/home/stefano/InnovareData/InnovareSegmentDS/testDataset/2020_08_31_Thermal_Optical-Fila_3_4-Session1-DJI_1234-plant_0.jpg\", \"model\": \"stub.h5\"}, {\"originalImage\": \"/home/stefano/InnovareData/InnovareSegmentDS/testDataset/2020_08_31_Thermal_Optical-Fila_3_4-Session1-DJI_1234-erased_background_with_boxes.jpg\", \"hash\": \"e047efb93b7b4508c364c56477f44990\", \"classification\": {\"classifications\": [{\"classe\": \"Pianta Normale\", \"score\": 8.533674478530884}, {\"classe\": \"Carenza di acqua\", \"score\": 8.533674478530884}, {\"classe\": \"Eccesso di acqua\", \"score\": 8.533674478530884}, {\"classe\": \"Infestanti\", \"score\": 8.533674478530884}, {\"classe\": \"Ambigua\", \"score\": 8.533674478530884}], \"hash\": \"'e047efb93b7b4508c364c56477f44990'\"}, \"date\": \"20-08-31\", \"path\": \"/home/stefano/InnovareData/InnovareSegmentDS/testDataset/020_08_31_Thermal_Optical-Fila_3_4-Session1-DJI_1234-plant_1.jpg\", \"model\": \"stub.h5\"}]";
		
		ArrayList<PlantClassification> pc=  new ObjectMapper().readValue(plantClassificationArray, new TypeReference<ArrayList<PlantClassification>>(){});
		Logger.getLogger().print("Piante da classificare: "+pc.toString());
		ClassificationSint cs= new ClassificationSint(pc);
		Logger.getLogger().print("Classificazione sintetica: "+cs.toString());
		String jsonCs= new ObjectMapper().writeValueAsString(cs);
		Logger.getLogger().print(jsonCs);

	}
	
	@Test
	public void foundModel() throws FileNotFoundException {
		ModelController mc= new ModelController();
		if(mc.foundModel("stub.h5"))
			Logger.getLogger().print("Trovato!");
		else
			Logger.getLogger().print("no :(");
		mc.setModelSelected("stub.h5");
		Logger.getLogger().print("Modello selezionato: "+mc.getSelectedModel().getName());
	}
	*/
	/*@Test
	public void unzipModel() throws FileNotFoundException, ZipException {
		ModelController mc= new ModelController();
		mc.unZipModel("stubdsds.zip");
	}
	*/
	/*
	@Test
	public void testCSVReader() throws IOException {
		SampleCSVController sc = new SampleCSVController();
		HashMap<String,ArrayList<Sample>> hsc=sc.readSampleFromCSV();
	}
	*/
	
	/*
	 *Testing SensorDataController
	 
	@Test
	public void testSensorDataController() {
		ConfigurationController cc= new ConfigurationController();
		Logger.getLogger().print(cc.getChannelForClassification());
		
		Vertx vertx = Vertx.vertx();
		String uri = "mongodb://localhost:27017";
		String db = "innovare";
		JsonObject mongoconfig = new JsonObject()
		        .put("connection_string", uri)
		        .put("db_name", db);
		
		Logger.getLogger().print("ok");
		
		MongoClient mongoClient= MongoClient.create(vertx, mongoconfig);
		SensorDataController sc= new SensorDataController(mongoClient, cc.getChannelForClassification());
		
		try {
			Future<HashMap<Integer, Sample>> result =sc.getLastSamples();
			result.onComplete(res->{
				HashMap<Integer, Sample> rr=res.result();
				Logger.getLogger().print("OK");
				for(Integer i:  rr.keySet())
					Logger.getLogger().print("Channel: "+i+" value: "+rr.get(i).toString() );
			});
			
			
		} catch (Exception e) {
			Logger.getLogger().print("FAIL");
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testLogger() throws FileNotFoundException {
		Logger.getLogger().print("TestString1");
		Logger.getLogger().print("TestString2");
		Logger.getLogger().print("TestString3");
		Logger.getLogger().print("TestString4");
	}
	
	
	@Test
	public void testSensorDataControllerSync() throws JsonMappingException, JsonProcessingException {
		
		SensorDataControllerSync sd= new SensorDataControllerSync();
		
		Sample ls=sd.getLastSamplesFromMongoSynch(1);
		Logger.getLogger().print("Last sample: "+ls.toString());
		
		//Attenzione utilizzare cast long altrimenti va in overflow e errore sul calcolo
		Timestamp tm = new Timestamp(System.currentTimeMillis()-(long)(1000*60*60*24)*7);
		Logger.getLogger().print("Sample from "+tm.toString()+" : ");
		ArrayList<Sample> samples=sd.getAllSamplesFromTimeFrame((long) (1000*60*60*24*120), 1);
		for(Sample s: samples)
			Logger.getLogger().print(s.toString());
		
	}
	
	
	@Test
	public void testKCRead() {
		ConfigurationController cc= new ConfigurationController();
		cc.kcFileRead();
	}
	
	
	@Test
	public void testEvapotrasp() {
		ArrayList<Sample> temps=new ArrayList<Sample>();
		temps.add(new Sample());
		double res= Evapotranspiration.calculate(30.0, 30.0, temps, 20.0*3.386, 0.0);
		
		Logger.getLogger().print("Risultato: "+(float)res);
	}
	*/
	
	@Test
	public void testIrr() {
		Irrigazione irr= new Irrigazione(System.currentTimeMillis());
		System.out.println(irr.toString());
	}
	
	
	/*
	@Test
	public void testWeatherStation() throws IOException, InterruptedException {
		Logger.getLogger().print("cd && cd "+Utilities.scriptPath+"vproweather-1.1"+System.getProperty("file.separator")+" && sudo ./vproweather -x /dev/ttyUSB0\n");
		Process processSt= Runtime.getRuntime().exec("./vproweather -x /dev/ttyUSB0\n >> sample.txt",null,new File(Utilities.scriptPath+"vproweather-1.1"+System.getProperty("file.separator")));
		//Attendiamo la fine della segmentazione
		InputStream is = processSt.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    String line;
		    while ((line = br.readLine()) != null) {
		      Logger.getLogger().print(line);
		    }
		//OutputStream outPSeg= processSt.getOutputStream();
		//Logger.getLogger().print(outPSeg.toString());
	}
	*
	
	@Test
	public void testIrrigationController() {
		
		
		IrrigationController irr= new IrrigationController();
		
	}
	
	/*
	@Test
	public void testData() throws ParseException, JsonMappingException, JsonProcessingException {
		String data= "2020-08-31";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date parsedDate = dateFormat.parse(data);
	    Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
		Logger.getLogger().print(timestamp.toString());
		Logger.getLogger().print(parsedDate.toString());
		String jsonTimestamp= "1606334624959";
		timestamp= new ObjectMapper().readValue(jsonTimestamp, Timestamp.class);
		//String jsonTimeStamp= new ObjectMapper().writeValueAsString(timestamp);
		Logger.getLogger().print("Prova con long:"+timestamp);
	}
	*/
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
