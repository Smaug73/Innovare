package com.innovare.model;

public enum Status {
	CARENZA("CARENZA",
			"Piante con carenza di acqua"), 
	NORMALE("NORMALE", "Piante sane"), 
	ECCESSO("ECCESSO", "Piante con eccesso di acqua"),
	SCARTATE("SCARTATE","Immagini scartate"),
	INFESTANTI("INFESTANTI","Piante infestanti");
	public String name;
	public String desc;

	Status(String name, String desc) {
		this.name = name;
		this.desc = desc;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}
}
