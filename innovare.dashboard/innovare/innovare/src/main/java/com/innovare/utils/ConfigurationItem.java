package com.innovare.utils;

import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationItem {

	//Classe di prova.
	
	private String id;
	private Property[] properties;
	
	public ConfigurationItem() {
		
	}
	
	
	public ConfigurationItem(String string, Property[] ps) {
		// TODO Auto-generated constructor stub
	}


	public static ConfigurationItem convertJsonConfigurationItem(String json) throws JsonMappingException, JsonProcessingException {
		
		return new ObjectMapper().readValue(json, ConfigurationItem.class);
	}
	

	/*
	public static void main(String argv[]) throws JsonMappingException, JsonProcessingException {
		{
		String jsonEx="{\r\n"
				+ "    \"id\": \"sensor-gateway\",\r\n"
				+ "    \"properties\": [\r\n"
				+ "        {\r\n"
				+ "            \"id\": \"indirizzo\",\r\n"
				+ "            \"value\": \"192.168.55.5\"\r\n"
				+ "        },\r\n"
				+ "        {\r\n"
				+ "            \"id\": \"porta\",\r\n"
				+ "            \"value\": \"8888\"\r\n"
				+ "        }\r\n"
				+ "    ]\r\n"
				+ "}";
		
		ConfigurationItem it= new ObjectMapper().readValue(jsonEx, ConfigurationItem.class);//mapping json->obj
		String jsonConv = new ObjectMapper().writeValueAsString(it);
	
		
		
		System.out.println(it.toString());
		System.out.println(jsonConv);
		
		}
		{
			String jsonEx="{\r\n"
					+ "    \"password\":\"iUPse124#\",\r\n"
					+ "    \"role\" : \"ADMIN\",\r\n"
					+ "    \"username\" : \"administrator\"\r\n"
					+ "    \r\n"
					+ "}";
			
			User it= new ObjectMapper().readValue(jsonEx, User.class);//mapping json->obj
			String jsonConv = new ObjectMapper().writeValueAsString(it);
		
			
			
			System.out.println(it.toString());
			System.out.println(jsonConv);
			
			}
		
	}*/
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Property[] getProperties() {
		return properties;
	}
	public void setProperties(Property[] properties) {
		this.properties = properties;
	}
	
	@Override
	public String toString() {
		return "ConfigurationItem [id=" + id + ", properties=" + Arrays.toString(properties) + "]";
	}
	
	
}