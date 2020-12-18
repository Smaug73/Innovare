package com.innovare.views.main;


import static com.vaadin.flow.server.VaadinSession.getCurrent;
import static java.util.Optional.ofNullable;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.PWA;
import com.innovare.views.main.MainView;
import com.innovare.utils.Authenticator;
import com.innovare.views.login.LoginView;

@CssImport("./styles/lumo/border-radius.css")
@CssImport("./styles/lumo/icon-size.css")
@CssImport("./styles/lumo/margin.css")
@CssImport("./styles/lumo/padding.css")
@CssImport("./styles/lumo/shadow.css")
@CssImport("./styles/lumo/spacing.css")
@CssImport("./styles/lumo/typography.css")
@CssImport("./styles/misc/box-shadow-borders.css")
@CssImport("./styles/views/main/main-view.css")
@CssImport(value = "./styles/styles.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge")
@PWA(name = "Innovare", shortName = "Innovare", iconPath = "images/icona_innovare.png", backgroundColor = "#233348", themeColor = "#233348")
public class MainView extends AppLayout implements RouterLayout, BeforeEnterObserver{

    

    public MainView() {
    }

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		
		String path = event.getLocation().getPath();

		Boolean isAuthenticated = ofNullable((Boolean)getCurrent().getAttribute(Authenticator.ATTRIBUTE_IS_AUTH))
                .orElse(Boolean.FALSE);

		 if (!isAuthenticated && !path.contains("login")) {
			      event.forwardTo(LoginView.class);
		 }
		
	}
    
    
}
