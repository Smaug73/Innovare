package com.innovare.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class User {

	private Role role;
	private String username;
	private String password;
	
	
	public User() {
		super();
	}
	
	
	public static User convertJsonToUser(String jsonUser) throws JsonMappingException, JsonProcessingException {
		return  new ObjectMapper().readValue(jsonUser, User.class);
	}
	
	@Override
	public String toString() {
		return "User [role=" + role + ", username=" + username + "]";
	}
	
	
	
	public boolean isAdministrator() {
		if(role.compareTo(Role.ADMIN)==0)
			return true;
		else
			return false;
	}
	
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
}
