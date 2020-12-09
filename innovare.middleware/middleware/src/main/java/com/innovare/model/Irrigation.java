package com.innovare.model;

public class Irrigation {

	private long startTime;
	private long finishTime;
	
	public Irrigation() {
		
	}

	
	public Irrigation(long startTime, long finishTime) {
		super();
		this.startTime = startTime;
		this.finishTime = finishTime;
	}


	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}


	@Override
	public String toString() {
		return "Irrigation [startTime=" + startTime + ", finishTime=" + finishTime + "]";
	}
	
	

}
