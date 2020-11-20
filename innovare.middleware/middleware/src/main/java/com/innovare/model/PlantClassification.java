package com.innovare.model;

import java.util.ArrayList;
/*
 * Modello rappresentante la classificazione di una singola pianta
 */
public class PlantClassification {

	private String uid; //uid della singola pianta alla quale appartiene la classificazione
	private ArrayList<Classification> classifications;
	
	
	public PlantClassification(String uid, ArrayList<Classification> classifications) {
		super();
		this.uid = uid;
		this.classifications = classifications;
	}


	public String getUid() {
		return uid;
	}


	public void setUid(String uid) {
		this.uid = uid;
	}


	public ArrayList<Classification> getClassifications() {
		return classifications;
	}


	public void setClassifications(ArrayList<Classification> classifications) {
		this.classifications = classifications;
	}


	@Override
	public String toString() {
		return "PlantClassification [uid=" + uid + ", classifications=" + classifications + "]";
	}
	
	
	
	
}
