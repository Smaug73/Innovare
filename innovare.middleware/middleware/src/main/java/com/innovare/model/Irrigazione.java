package com.innovare.model;

import java.sql.Timestamp;

import com.innovare.control.ConfigurationController;

public class Irrigazione {

	private long inizioIrrig;
	private long fineIrrig;
	private float quantita;
	
	
	
	public Irrigazione() {}
	
	public Irrigazione(long timeStart) {
		this.inizioIrrig=timeStart;
		this.fineIrrig=ConfigurationController.irrigationMaxTime+this.inizioIrrig;
		this.quantita= ((this.fineIrrig-this.inizioIrrig)/1000)*ConfigurationController.portataIrrigation;
		
		System.out.println("DEBUG : "+ConfigurationController.irrigationMaxTime);
		System.out.println("DEBUG IRR: durata "+(this.fineIrrig-this.inizioIrrig)/1000l+" secondi ");
	}
	
	/*
	 * Costruttore dell'irrigazione basandosi su quantita' e portata dell'impianto irriguo
	 */
	
	public Irrigazione(float quant) {
		
		//Settiamo quantita'
		this.quantita=quant;
		
		//Settiamo inizio e fine dell'irrigazione
		Timestamp now= new Timestamp(System.currentTimeMillis());
		this.inizioIrrig=now.getTime();
		long durataTot=(long) (this.quantita/ConfigurationController.portataIrrigation)*1000;
		this.fineIrrig=this.inizioIrrig+durataTot;
	}
	
	
	public Irrigazione(Status s) {
		/*switch(s) {
		case CARENZA:
			
			break;
		case ECCESSO:
			break;
		case INFESTANTI:
			break;
		case NORMALE:
			break;
		case SCARTATE:
			break;
		default:
			break;
		
		}*/
		Timestamp now= new Timestamp(System.currentTimeMillis());
		this.inizioIrrig=now.getTime();
		//fine fra 10 secondi per test
		//this.fineIrrig=this.inizioIrrig+(10*1000);
		//this.quantita=100;
		long durataTot=(long) (this.quantita/ConfigurationController.portataIrrigation)*1000;
		this.fineIrrig=this.inizioIrrig+durataTot;
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
