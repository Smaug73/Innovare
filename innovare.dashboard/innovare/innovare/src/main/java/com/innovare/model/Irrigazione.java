package com.innovare.model;

import java.sql.Timestamp;

public class Irrigazione {
	private long inizioIrrig;
	private long fineIrrig;
	private float quantita;
	private String _id;
	
	public Irrigazione() {}
	
	public Irrigazione(long inizioIrrig, long fineIrrig, float quantita) {
		super();
		this.inizioIrrig = inizioIrrig;
		this.fineIrrig = fineIrrig;
		this.quantita = quantita;
	}
	
	public long getInizioIrrig() {
		return inizioIrrig;
	}
	public void setInizioIrrig(long inizioIrrig) {
		this.inizioIrrig = inizioIrrig;
	}
	public long getFineIrrig() {
		return fineIrrig;
	}
	public void setFineIrrig(long fineIrrig) {
		this.fineIrrig = fineIrrig;
	}
	public float getQuantita() {
		return quantita;
	}
	public void setQuantita(float quantita) {
		this.quantita = quantita;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	@Override
	public String toString() {
		return "Irrigazione [inizioIrrig=" + inizioIrrig + ", fineIrrig=" + fineIrrig + ", quantita=" + quantita
				+ ", _id=" + _id + "]";
	}
	
	
	
	
}
