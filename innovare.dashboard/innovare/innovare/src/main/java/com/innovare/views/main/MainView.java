package com.innovare.views.main;


import static com.vaadin.flow.server.VaadinSession.getCurrent;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Collection;

import com.vaadin.flow.server.VaadinSession;

import com.vaadin.addon.charts.ChartOptions;
import com.vaadin.addon.charts.ChartSelectionEvent;
import com.vaadin.addon.charts.model.Lang;
import com.vaadin.addon.charts.shared.ChartOptionsState;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.PWA;
import com.innovare.views.main.MainView;
import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.utils.Authenticator;
import com.innovare.views.login.LoginView;
import com.vaadin.flow.component.UI;

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
    	VaadinSession vs = getCurrent();
    	Collection<UI> uis = vs.getUIs();
    	int i = 0;
    	for(UI ui : uis) {
    		System.out.println("UI " + ui.getUIId() + " in main diverso da null");
    		i++;
    	}
    	Lang lang = new Lang();
    	lang.setDrillUpText("< PROVA");
    	//ChartOptions.get().setLang(lang);
    	if(UI.getCurrent() != null) System.out.println("UI in main diverso da null");
        else System.out.println("UI in main uguale a null");
    	
    	
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
