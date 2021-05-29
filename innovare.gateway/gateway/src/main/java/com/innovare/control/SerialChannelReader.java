package com.innovare.control;

import java.util.ArrayList;

import com.innovare.model.Sample;
import com.innovare.utils.Utilities;

public class SerialChannelReader {

	
	private ArrayList<Integer> channelsId;
	private String channelName= "Seriale-"; // INSERIRE NUMERO SENSORE SERIALE NEL FARLO
	
	
	public SerialChannelReader(){
		//recupera gli id dei canali da leggere
		this.channelsId=ConfigurationController.idSerialChannel;
		
		
	}
	
	
	//Da definire, per testing creiamo dato simulato
	public ArrayList<Sample> getMeasursFromSerial() throws Exception {
		ArrayList<Sample> meas= new ArrayList<Sample>();
		meas.add(new Sample(System.currentTimeMillis(),this.channelName,10));
		if(meas==null)
			throw new Exception("Errore lettura seriale!");
		return meas;
	}


	public ArrayList<Integer> getChannelsId() {
		return channelsId;
	}


	public void setChannelsId(ArrayList<Integer> channelsId) {
		this.channelsId = channelsId;
	}


	
	
	
	
	
}
