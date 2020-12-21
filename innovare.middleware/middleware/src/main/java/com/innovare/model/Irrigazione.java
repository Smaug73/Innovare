package com.innovare.model;

import java.sql.Timestamp;

public class Irrigazione {

	private Timestamp inizioIrrig;
	private Timestamp fineIrrig;
	private double quantita;
	
	public Irrigazione(Timestamp inizioIrrig, Timestamp fineIrrig, double quantita) {
		this.inizioIrrig = inizioIrrig;
		this.fineIrrig = fineIrrig;
		this.quantita = quantita;
	}
	
	public Irrigazione() {}
	

	public Timestamp getInizioIrrig() {
		return inizioIrrig;
	}

	public void setInizioIrrig(Timestamp inizioIrrig) {
		this.inizioIrrig = inizioIrrig;
	}

	public Timestamp getFineIrrig() {
		return fineIrrig;
	}

	public void setFineIrrig(Timestamp fineIrrig) {
		this.fineIrrig = fineIrrig;
	}

	public double getQuantita() {
		return quantita;
	}

	public void setQuantita(double quantita) {
		this.quantita = quantita;
	}
	
	

	

}
