package com.innovare.model;


import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
/*
 * Modello rappresentante la classificazione di una singola pianta
 */
//@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantClassification {

	private String hash; //uid della singola pianta alla quale appartiene la classificazione
	private Classification classification;
	private String date;
	private String originalImage;
	private String path;
	private String modelName;
	
	
	public PlantClassification() {}

	
	public PlantClassification(String hash, Classification classification, String date,
			String originalImage, String path,String modelname) {
		super();
		this.hash = hash;
		this.classification = classification;
		this.date = date;
		this.originalImage = originalImage;
		this.path = path;
		this.modelName=modelname;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Classification getClassification() {
		return classification;
	}

	public void setClassifications(Classification classification) {
		this.classification = classification;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getOriginalImage() {
		return originalImage;
	}

	public void setOriginalImage(String originalImage) {
		this.originalImage = originalImage;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getModelName() {
		return modelName;
	}


	public void setModelName(String modelName) {
		this.modelName = modelName;
	}


	@Override
	public String toString() {
		return "PlantClassification [hash=" + hash + ", classification=" + classification + ", date=" + date
				+ ", originalImage=" + originalImage + ", path=" + path + ", modelName=" + modelName + "]";
	}


	
	
}
