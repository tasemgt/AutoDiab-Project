package com.example.bluetransport;

public class Meter{
	
	private String type, sn;
	
	public Meter(String data){
		String s = " ";		
		this.type = "CNU";
		this.sn = data.substring(data.lastIndexOf(s));
	}
	
	public String getSN(){
		return this.sn;
	}
	
	public String getMeter(){
		return this.type;
	}
}
