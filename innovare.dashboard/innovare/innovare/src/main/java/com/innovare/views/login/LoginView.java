package com.innovare.views.login;

import com.vaadin.addon.charts.ChartOptions;
import com.vaadin.addon.charts.model.Lang;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.ContentAlignment;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import static com.vaadin.flow.server.VaadinSession.getCurrent;

import com.innovare.ui.utils.BoxSizing;
import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.ui.utils.LumoStyles;
import com.innovare.utils.Authenticator;
import com.innovare.utils.Role;
import com.innovare.utils.User;
import com.innovare.views.main.MainView;

import java.awt.Color;
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

	

	private final LoginForm loginForm = new LoginForm();
	private String parameter = "";


	public LoginView() {
		FlexBoxLayout content = new FlexBoxLayout(loginForm);
		content.setAlignItems(FlexComponent.Alignment.CENTER);
		content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		loginForm.setForgotPasswordButtonVisible(false);
		loginForm.addLoginListener(event -> {
	
			String username = event.getUsername();
			String password = event.getPassword();
    
			if(Authenticator.authenticate(username, password)) { 
				UI.getCurrent().navigate(parameter);
			}
			else loginForm.setError( true );
  
		});
		getContent().add(content);
	}
	

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		VaadinSession vs = getCurrent();
		if(vs != null) {
			vs.setAttribute("username", null);
			vs.setAttribute("auth", false);
			vs.setAttribute("role", null);
		}
	}
}
