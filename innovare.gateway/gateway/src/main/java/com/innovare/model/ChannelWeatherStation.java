package com.innovare.model;

import java.util.PriorityQueue;

public class ChannelWeatherStation extends Thread{

	private String ID; //Scritto cosi' altrimenti si sovrappone all'id del thread
	private long periodo;	//Periodo di campionamento
	private PriorityQueue<Sample> samplesQueue=null;
	
	
	
}
