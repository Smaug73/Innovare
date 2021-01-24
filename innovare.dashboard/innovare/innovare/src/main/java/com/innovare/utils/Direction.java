package com.innovare.utils;

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
	
	public static Direction getDirection(int num) {
		switch(num) {
			case 0: return N;
			case 1: return NNE;
			case 2: return NNW;
			case 3: return NW;
			case 4: return NE;
			case 5: return W;
			case 6: return WNW;
			case 7: return WSW;
			case 8: return E;
			case 9: return ENE;
			case 10: return ESE;
			case 11: return S;
			case 12: return SSE;
			case 13: return SSW;
			case 14: return SW;
			case 15: return SE;
			default: return null;
		}
	}
}
