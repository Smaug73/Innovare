package com.innovare.views.innovare;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.innovare.views.main.MainView;

@Route(value = "about", layout = MainView.class)
@PageTitle("Innovare")
@CssImport("./styles/views/innovare/innovare-view.css")
public class InnovareView extends Div {

    public InnovareView() {
        setId("innovare-view");
        add(new Label("Content placeholder"));
    }

}
