package com.innovare.views.configurazione;



import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.innovare.utils.Role;
import com.innovare.views.main.ContentView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "config", layout = ContentView.class)
@PageTitle("Config")
@CssImport("./styles/views/innovare/innovare-view.css")
//@Secured(Role.ADMIN)
public class ConfigurazioneView extends Div {

    public ConfigurazioneView() {
        setId("config-view");
        add(new Label("Content placeholder"));
        
        
        URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("localhost:8888").setPath("/actualConfiguration");
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
				System.out.println("OK!");
				System.out.println(response.getAllHeaders().toString());
				//add(new Label("Content placeholder"));
				//vaadinSession.setAttribute(ATTRIBUTE_USERNAME , username);
				//vaadinSession.setAttribute(ATTRIBUTE_IS_AUTH , Boolean.TRUE);
				//UI.getCurrent().navigate(parameter);
		
			}else
			{
				System.out.println("Errore");
				//loginForm.setError( true );
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Errore");
		}

	
		
    }

}
