package com.innovare.model;

import java.util.Stack;

public class Channel {
	
	/*
	 * Deve contenere uno stack dei sample raccolti.
	 * Il channel identifica un tipo di misurazione effettuata
	 * 
	 */	
	private String id;
	
	private Stack<Sample> samples;
	
	
	
	public Channel() {}
	
	public Channel(String id, Stack<Sample> samples) {
		super();
		this.id = id;
		this.samples = samples;
	}
	
	//Metodo per avviare il thread per il campionamento del determinato segnale
	//Il thread deve andare a riempire lo stack, quindi si deve implementare una logica bloccante sullo stack 
	//quando ci vuole accedere il thread
	public void startListening() {
		
	}

	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public Stack<Sample> getSamples() {
		return samples;
	}


	public void setSamples(Stack<Sample> samples) {
		this.samples = samples;
	}


	@Override
	public String toString() {
		return "Channel [id=" + id + ", samples=" + samples + "]";
	}
	
	
	

}
