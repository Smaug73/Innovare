package com.innovare.model;

public enum IrrigationState {
	ACCESO("Acceso", "var(--lumo-success-text-color)", "ON"), 
	SPENTO("Spento", "var(--lumo-error-text-color)", "OFF");

	private String name;
	private String color;
	private String shortName;

	IrrigationState(String name, String color, String shortName) {
		this.name = name;
		this.color = color;
		this.shortName = shortName;
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}

	public String getShortName() {
		return shortName;
	}

}
