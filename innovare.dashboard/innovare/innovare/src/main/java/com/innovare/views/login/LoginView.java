package com.innovare.views.login;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.utils.Authenticator;
import com.innovare.views.main.MainView;


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
		Authenticator.logout();
	}
}
