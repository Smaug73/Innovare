package com.innovare.model;

import java.sql.Timestamp;

public class Irrigazione {

	private long inizioIrrig;
	private long fineIrrig;
	private float quantita;
	
	
	
	public Irrigazione() {}
	
	public Irrigazione(long timeStart) {
		this.inizioIrrig=timeStart;
	}
	
	
	public Irrigazione(long inizioIrrig, long fineIrrig, float quantita) {
		super();
		this.inizioIrrig = inizioIrrig;
		this.fineIrrig = fineIrrig;
		this.quantita = quantita;
	}

	
	public long getInizioIrrig() {
		return inizioIrrig;
	}

	public long getFineIrrig() {
		return fineIrrig;
	}

	public float getQuantita() {
		return quantita;
	}

	public void setInizioIrrig(long inizioIrrig) {
		this.inizioIrrig = inizioIrrig;
	}

	public void setFineIrrig(long fineIrrig) {
		this.fineIrrig = fineIrrig;
	}


	public void setQuantita(float quantita) {
		this.quantita = quantita;
	}
	 

	@Override
	public String toString() {
		return "Irrigazione [inizioIrrig=" + inizioIrrig + ", fineIrrig=" + fineIrrig + ", quantita=" + quantita + "]";
	}
	
	
	

	

}
