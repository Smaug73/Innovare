package com.innovare.model;

public enum Direction {
	N("Nord", 0), 
	NNE("Nord-Nord-Est", 1), 
	NNW("Nord-Nord-Ovest", 2),
	NW("Nord-Ovest", 3),
	NE("Nord-Est", 4),
	W("Ovest", 5),
	WNW("Ovest-Nord-Ovest", 6),
	WSW("Ovest-Sud-Ovest", 7),
	E("Est", 8),
	ENE("Est-Nord-Est", 9),
	ESE("Est-Sud-Est", 10),
	S("Sud", 11), 
	SSE("Sud-Sud-Est", 12), 
	SSW("Sud-Sud-Ovest", 13),
	SW("Sud-Ovest", 14),
	SE("Sud-Est",15);
	

	private String name;
	private int num;

	Direction(String name, int num) {
		this.name = name;
		this.num = num;
	}

	public String getName() {
		return name;
	}
	
	public int getNum() {
		return num;
	}
}
