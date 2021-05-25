package com.innovare.model;

public class ChannelMeasure {
	private int channelnum;
	private String measure;
	
	
	public ChannelMeasure() {}
	
	public ChannelMeasure(int channelnum, String measure) {
		super();
		this.channelnum = channelnum;
		this.measure = measure;
	}
	
	
	public int getChannelnum() {
		return channelnum;
	}
	public void setChannelnum(int channelnum) {
		this.channelnum = channelnum;
	}
	public String getMeasure() {
		return measure;
	}
	public void setMeasure(String measure) {
		this.measure = measure;
	}


	@Override
	public String toString() {
		return "ChannelMeasure [channelnum=" + channelnum + ", measure=" + measure + "]";
	}
	

}
