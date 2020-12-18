package com.innovare.model;

import java.util.Random;

public class Model {
	
	private String name;
	private String _id;
	
	public Model() {}

	public Model(String name, String uid) {
		super();
		this.name = name;
		this._id = uid;	
	}
	
	/*
	 * L'uid viene generato casualmente come un intero
	 */
	public Model(String name) {
		super();
		this.name = name;
		this._id = Integer.toString(new Random().nextInt());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}	
	
	

}
