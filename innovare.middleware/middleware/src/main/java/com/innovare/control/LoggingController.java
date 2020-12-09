package com.innovare.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.innovare.model.User;
import com.innovare.utils.NoUserLogException;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class LoggingController {
	
	private User loggedUser=null;

	
	public LoggingController() {}
	
	
	public boolean isUserLogged() {
		if (this.loggedUser!=null)
			return true;
		else 
			return false;
	}
	
	public boolean isUserAdmin() {
		if (this.loggedUser!=null && this.loggedUser.isAdministrator())
			return true;
		else 
			return false;
	}
	
	/*
	 * Controlliamo se l'utente e' già loggato e se non lo è lo loggiamo
	 */
	public boolean logIn(String userJsonString) {
		
		if(!isUserLogged()) {
			try {
				this.loggedUser= User.convertJsonToUser(userJsonString);
				return true;
			} catch (JsonMappingException e) {
				e.printStackTrace();
				return false;
			} catch (JsonProcessingException e) {		
				e.printStackTrace();
				return false;
			}
		}else {
			return false;
		}
		
		
	}
	
	/*
	 * Effettua il logout dal sistema
	 */
	public void logout() {
		this.loggedUser=null;
		System.out.println("Logout eseguito.");
	}
	
	public User getUserLogged() throws NoUserLogException {
		if(this.isUserLogged())
			return this.loggedUser;
		else 
			throw new NoUserLogException("Nessun Utente loggato nel sistema");
	}

	
	
	@Override
	public String toString() {
		return "LoggingController [loggedUser=" + loggedUser + "]";
	}
	
	
	
}
