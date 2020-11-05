package com.innovare.utils;

import static com.vaadin.flow.server.VaadinSession.getCurrent;

import java.util.Optional;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

public class Authenticator {
	public static final String ATTRIBUTE_USERNAME = "username";
	public static final String ATTRIBUTE_IS_AUTH = "auth";
	public static final String ATTRIBUTE_ROLE= "role";
	
	public static boolean authenticate ( String username , String password )
    {
		VaadinSession vaadinSession = getCurrent();
		
		/*URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("localhost:8888").setPath("/login")
		.setParameter("username", username)
		.setParameter("password", password);

		URI uri = null;
		try {
			uri = builder.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		HttpGet get = new HttpGet(uri);
		CloseableHttpClient client = HttpClients.createDefault();

		try(CloseableHttpResponse response = client.execute(get)){
			String status = response.getStatusLine().toString();
			System.out.println(status);
			if(status.contains("200")) {
				System.out.println("OK!");*/
				if(username.contains("admin"))  vaadinSession.setAttribute(ATTRIBUTE_ROLE , Role.ADMIN);
				else vaadinSession.setAttribute(ATTRIBUTE_ROLE , Role.USER);
				vaadinSession.setAttribute(ATTRIBUTE_USERNAME , username);
				vaadinSession.setAttribute(ATTRIBUTE_IS_AUTH , true);
				return true;
		
			/*}else
			{
				System.out.println("Errore");
				return false;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Errore")
		}*/
    }

}
