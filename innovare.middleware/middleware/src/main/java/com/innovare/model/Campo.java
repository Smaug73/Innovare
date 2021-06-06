package com.innovare.model;

public enum Campo {
	MANUAL("tradizionale"), 
	AUTO("automatico");
	
	private String nome;
	
	Campo(String nome){
		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}
	
	
}
