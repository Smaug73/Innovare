package com.innovare.utils;

public enum IrrigationState {
	ACCESO("Acceso", "var(--lumo-success-text-color)"), 
	SPENTO("Spento", "var(--lumo-error-text-color)");

	private String name;
	private String color;

	IrrigationState(String name, String color) {
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}

}
