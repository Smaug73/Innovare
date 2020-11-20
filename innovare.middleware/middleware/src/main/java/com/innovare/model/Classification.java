package com.innovare.model;

public class Classification {
	
	private String classe;
	private float value;
	
	public Classification(String classe, float value) {
		super();
		this.classe = classe;
		this.value = value;
	}
	
	public String getClasse() {
		return classe;
	}
	public void setClasse(String classe) {
		this.classe = classe;
	}
	public float getValue() {
		return value;
	}
	public void setValue(float value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Classification [classe=" + classe + ", value=" + value + "]";
	}
	
	

}
