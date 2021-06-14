package com.innovare.control;

import java.io.InputStream;
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
		/*ArrayList<Sample> meas= new ArrayList<Sample>();
		meas.add(new Sample(System.currentTimeMillis(),this.channelName,10));
		if(meas==null)
			throw new Exception("Errore lettura seriale!");
		return meas;
		*/
		SerialPort sp= SerialPort.getCommPort(this.serialPort);
		if(sp.openPort()) {
			sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
			//scriviamo comando di rilascio dati
			OutputStream out= sp.getOutputStream();
			InputStream in= sp.getInputStream();
			
			String prepMis="aM!";
			String getdata="aD0!";
			//richiedo preparazione invio dati
			out.write(getdata.getBytes());
			byte[] dataResp=in.readAllBytes();
			String dataStr= dataResp.toString();
			
			//Recuperiamo dati sensore dalla stringa
			
			
			
			ArrayList<Sample> meas= new ArrayList<Sample>();
			//meas.add();
			return meas;
			
		}else throw new Exception("Errore lettura seriale!");
	
	}


	public ArrayList<Integer> getChannelsId() {
		return channelsId;
	}


	public void setChannelsId(ArrayList<Integer> channelsId) {
		this.channelsId = channelsId;
	}


	
	
	
	
	
}
