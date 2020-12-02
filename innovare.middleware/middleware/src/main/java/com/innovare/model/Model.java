package com.innovare.model;

import java.util.Random;

public class Model {
	
	private String name;
	private String uid;
	
	public Model() {}

	public Model(String name, String uid) {
		super();
		this.name = name;
		this.uid = uid;	
	}
	
	/*
	 * L'uid viene generato casualmente come un intero
	 */
	public Model(String name) {
		super();
		this.name = name;
		this.uid = Integer.toString(new Random().nextInt());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}	
	
	

}
