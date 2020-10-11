package com.innovare.views.configurazione;



import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.http.Header;
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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
@Route(value = "config", layout = ContentView.class)
@PageTitle("Config")
@CssImport("./styles/views/innovare/innovare-view.css")
//@Secured(Role.ADMIN)
public class ConfigurazioneView extends Div {

    public ConfigurazioneView() throws IOException, InterruptedException, URISyntaxException, ParseException {
        setId("config-view");
        add(new Label("Content placeholder"));
        
        URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("localhost:8888").setPath("/configuration");
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(builder.build())
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
       
        //Creo un oggetto json dalla scritta json
        //Si può creare un mapping diretto tra json e classi utilizzando la libreria gson come fatto in android
       
        
        
        /*
        URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("localhost:8888").setPath("/configuration");
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
				System.out.println("Configuration call done.");
				System.out.println(response.getAllHeaders().toString());
				for(Header h: response.getAllHeaders()) {
					System.out.println(h.getValue().toString());
				}
				System.out.println(response.getEntity().toString());
				response.
				
				
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

		*/
		
    }

}
