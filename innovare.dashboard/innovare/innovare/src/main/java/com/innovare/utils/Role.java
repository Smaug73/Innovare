package com.innovare.utils;

public class Role {

	public static final String USER = "user";
	public static final String ADMIN = "admin";

	private Role() {
	}

	public static String[] getAllRoles() {
		return new String[] { USER, ADMIN };
	}
}
