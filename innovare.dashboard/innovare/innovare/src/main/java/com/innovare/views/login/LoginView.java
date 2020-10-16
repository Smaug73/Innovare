package com.innovare.views.login;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import static com.vaadin.flow.server.VaadinSession.getCurrent;
import com.innovare.views.main.MainView;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

@Route(value = "login", layout = MainView.class)
@PageTitle("Login")
@CssImport("./styles/views/login/login-view.css")
public class LoginView extends Composite<Div> implements BeforeEnterObserver{

	public static final String ATTRIBUTE_USERNAME = "username";
	public static final String ATTRIBUTE_IS_AUTH = "auth";

	private final LoginForm loginForm = new LoginForm();
	private String parameter = "";


	public LoginView() {
		loginForm.addLoginListener(event -> {
			VaadinSession vaadinSession = getCurrent();
	
			String username = event.getUsername();
			String password = event.getPassword();
    
			URIBuilder builder = new URIBuilder();
			builder.setScheme("http").setHost("localhost:8888").setPath("/login")
			.setParameter("username", username)
			.setParameter("password", password);
	
			/*URI uri = null;
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
					vaadinSession.setAttribute(ATTRIBUTE_USERNAME , username);
					vaadinSession.setAttribute(ATTRIBUTE_IS_AUTH , Boolean.TRUE);
					UI.getCurrent().navigate(parameter);
			
				/*}else
				{
					System.out.println("Errore");
					loginForm.setError( true );
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Errore");
			}*/
  
		});
		getContent().add(loginForm);
	}


	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		VaadinSession vs = getCurrent();
		if(vs != null) {
			vs.setAttribute(ATTRIBUTE_USERNAME, null);
			vs.setAttribute(ATTRIBUTE_IS_AUTH , Boolean.FALSE);
		}
	}
}
