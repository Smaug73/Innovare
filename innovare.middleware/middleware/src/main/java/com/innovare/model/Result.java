package com.innovare.model;

public class Result {

	private String classe;
	private double score;
	
	
	
	public Result() {
		super();
	}


	public Result(String classe, double score) {
		super();
		this.classe = classe;
		this.score = score;
	}
	
	
	public String getClasse() {
		return classe;
	}
	public void setClasse(String classe) {
		this.classe = classe;
	}
	public double getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}


	@Override
	public String toString() {
		return "Result [classe=" + classe + ", score=" + score + "]";
	}
	
	
	
}
