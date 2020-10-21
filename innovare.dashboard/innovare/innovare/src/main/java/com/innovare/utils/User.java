package com.innovare.utils;

public class User {
	
	private String email;
	private String role;


	public User() {
		// An empty constructor is needed for all beans
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
