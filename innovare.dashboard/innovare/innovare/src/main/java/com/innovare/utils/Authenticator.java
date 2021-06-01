package com.innovare.utils;

import static com.vaadin.flow.server.VaadinSession.getCurrent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;

import com.innovare.model.Role;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.VaadinSession;

public class Authenticator {
	public static final String ATTRIBUTE_USERNAME = "username";
	public static final String ATTRIBUTE_IS_AUTH = "auth";
	public static final String ATTRIBUTE_ROLE= "role";
	
	public static boolean authenticate ( String username , String password )
    {
		VaadinSession vaadinSession = getCurrent();
		
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("localhost:8888").setPath("/login") 
		.setParameter("username", username)
		.setParameter("password", password);
	        
	    HttpClient client = HttpClient.newHttpClient();
	        HttpRequest request;
			try {
				request = HttpRequest.newBuilder()
				        .uri(builder.build())
				        .build();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				
				String body = response.body();
				if(body.trim().equals(Role.ADMIN))  vaadinSession.setAttribute(ATTRIBUTE_ROLE , Role.ADMIN);
				else if (body.trim().equals(Role.USER)) vaadinSession.setAttribute(ATTRIBUTE_ROLE , Role.USER);
				else return false; 	
				vaadinSession.setAttribute(ATTRIBUTE_USERNAME , username);
				vaadinSession.setAttribute(ATTRIBUTE_IS_AUTH , true);
			return true;
					} catch (IOException e) {
				Notification notif = Notification.show("Controlla la tua connessione");
				notif.setPosition(Position.BOTTOM_END);
				return false;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return false;
			}
    }

	public static boolean logout() {
		VaadinSession vaadinSession = getCurrent();
		
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("localhost:8888").setPath("/logout");
	        
	    HttpClient client = HttpClient.newHttpClient();
	        HttpRequest request;
			try {
				request = HttpRequest.newBuilder()
				        .uri(builder.build())
				        .build();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				
				if(response.statusCode() == 200) {
					vaadinSession.setAttribute(ATTRIBUTE_USERNAME, null);
					vaadinSession.setAttribute(ATTRIBUTE_IS_AUTH, false);
					vaadinSession.setAttribute(ATTRIBUTE_ROLE, null);
				}
				return true;
			} catch (IOException e) {
				//e.printStackTrace();
				System.out.println(e);
				Notification notif = Notification.show("Controlla la tua connessione");
				notif.setPosition(Position.BOTTOM_END);
				notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
				return false;
			} catch (InterruptedException e) {
				e.printStackTrace();
				Notification notif = Notification.show("Si è verificato un errore");
				notif.setPosition(Position.BOTTOM_END);
				notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
				return false;
			} catch (URISyntaxException e) {
				e.printStackTrace();
				Notification notif = Notification.show("Si è verificato un errore");
				notif.setPosition(Position.BOTTOM_END);
				notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
				return false;
			} 
	}
}
