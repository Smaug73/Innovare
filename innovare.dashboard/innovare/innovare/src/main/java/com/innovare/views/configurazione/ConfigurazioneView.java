package com.innovare.views.configurazione;



import com.innovare.utils.Role;
import com.innovare.views.main.ContentView;
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
    }

}
