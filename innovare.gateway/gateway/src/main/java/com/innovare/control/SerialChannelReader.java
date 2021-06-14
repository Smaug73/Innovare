package com.innovare.control;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import com.fazecast.jSerialComm.SerialPort;
import com.innovare.model.Sample;
import com.innovare.utils.Utilities;

public class SerialChannelReader {

	
	private ArrayList<Integer> channelsId;
	private String channelName= "Seriale-"; // INSERIRE NUMERO SENSORE SERIALE NEL FARLO
	private String serialPort;
	
	private String identification = "I!";
	private String startMeasure = "M!";
	private String getMeasure = "D0!";
	
	public SerialChannelReader(){
		//recupera gli id dei canali da leggere
		this.channelsId=ConfigurationController.idSerialChannel;
		this.serialPort=ConfigurationController.serialPort;
	}
	
	
	//Da definire, per testing creiamo dato simulato
	public ArrayList<Sample> getMeasursFromSerial() throws Exception {
		
		ArrayList<Sample> meas= new ArrayList<Sample>();
		
		try {
			//Prendiamo il numero di sensori da dover interrogare
			int numSen= ConfigurationController.numSens;
			
			for(int i=0; i<numSen; i++) {
				System.out.println("Lettura sensore da terra con codice : "+i);
				Process processSeg= Runtime.getRuntime().exec("python3 "+Utilities.scriptUtils+"serialSensor.py "+i);
				//OutputStream outPSeg= processSeg.getOutputStream();
				InputStreamReader isr = new InputStreamReader(processSeg.getInputStream());
				BufferedReader rdr = new BufferedReader(isr);
				String line;
				Sample temp, umi;
				while((line = rdr.readLine()) != null) {
					System.out.println(line);
					System.out.println("Genero la misura di temperatura..");
					float temperatura= Float.parseFloat(line);
					temp= new Sample(System.currentTimeMillis(),"Serial-Temp-"+i,temperatura);
					meas.add(temp);
					
					line = rdr.readLine();
					float um= Float.parseFloat(line);
					umi = new Sample(System.currentTimeMillis(),"Serial-Umi-"+i,um);
					System.out.println(line);
					System.out.println("Genero la misura di umidita'..");
					meas.add(umi);
				}	
				
				//Attendiamo la fine della segmentazione
				int processSegOutput=processSeg.waitFor();
				if(processSegOutput == 1)
					throw new Exception("Errore lettura da sensore con id: "+i);
				else {
					
					System.out.println("DONE!");
				}
			}

		}catch(Exception e) {
			System.out.println("Errore lettura dati dal sensore seriale!");
		}
			
		
		//meas.add();
		return meas;	
			
	}


	


	public ArrayList<Integer> getChannelsId() {
		return channelsId;
	}


	public void setChannelsId(ArrayList<Integer> channelsId) {
		this.channelsId = channelsId;
	}


	
	
	
	
	
}
