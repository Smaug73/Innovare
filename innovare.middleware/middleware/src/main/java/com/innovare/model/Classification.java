package com.innovare.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class Classification {
	
	private String hash;
	private ArrayList<Result> classifications;
	
	
	public Classification() {
		super();
	}


	public Classification(String hash, ArrayList<Result> classifications) {
		super();
		this.hash = hash;
		this.classifications = classifications;
	}


	public String getHash() {
		return hash;
	}


	public void setHash(String hash) {
		this.hash = hash;
	}


	public ArrayList<Result> getClassifications() {
		return classifications;
	}


	public void setClassifications(ArrayList<Result> classifications) {
		this.classifications = classifications;
	}


	@Override
	public String toString() {
		return "Classification [hash=" + hash + ", classifications=" + classifications + "]";
	}
	
	
	

}
