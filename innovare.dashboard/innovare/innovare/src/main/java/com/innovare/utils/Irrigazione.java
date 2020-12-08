package com.innovare.utils;

import java.util.Date;

public class Irrigazione {
	private Date inizioIrrig;
	private Date fineIrrig;
	private double quantita;
	
	public Irrigazione(Date inizioIrrig, Date fineIrrig, double quantita) {
		super();
		this.inizioIrrig = inizioIrrig;
		this.fineIrrig = fineIrrig;
		this.quantita = quantita;
	}

	public Date getInizioIrrig() {
		return inizioIrrig;
	}

	public void setInizioIrrig(Date inizioIrrig) {
		this.inizioIrrig = inizioIrrig;
	}

	public Date getFineIrrig() {
		return fineIrrig;
	}

	public void setFineIrrig(Date fineIrrig) {
		this.fineIrrig = fineIrrig;
	}

	public double getQuantita() {
		return quantita;
	}

	public void setQuantita(double quantita) {
		this.quantita = quantita;
	}
	
	

}
