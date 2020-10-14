package com.innovare.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Property {

	private String id;
	private String value;
	
	
	
	public Property() {
		
	}
	
	public Property(String id, String value) {
		this.id= id;
		this.value= value;
	}
	
	public static Property convertJsonProperty(String json) throws JsonMappingException, JsonProcessingException {
		return new ObjectMapper().readValue(json, Property.class);
	}
	
	@Override
	public String toString() {
		return "Property [id=" + id + ", value=" + value + "]";
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
