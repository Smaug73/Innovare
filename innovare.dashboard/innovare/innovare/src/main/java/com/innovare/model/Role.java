package com.innovare.model;

public class Role {

	public static final String USER = "USER";
	public static final String ADMIN = "ADMIN";

	private Role() {
	}

	public static String[] getAllRoles() {
		return new String[] { USER, ADMIN };
	}
}
