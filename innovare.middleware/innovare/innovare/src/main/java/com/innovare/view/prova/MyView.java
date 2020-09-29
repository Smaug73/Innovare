package com.innovare.view.prova;

import com.vaadin.flow.templatemodel.TemplateModel;
import com.innovare.views.main.MainView;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


@Route(value="prova",layout = MainView.class)
@PageTitle("Prova")
public class MyView extends VerticalLayout {

    /**
     * Creates a new MyView.
     */
    public MyView() {
    	
    	setId("prova-view");
        // You can initialise any data required for the connected UI components here.
    	Button button = new Button("Click me!");
    	button.addClickListener(clickEvent -> 
    			add(new Text("Fai piano!!")));
    	
    	
    	HorizontalLayout layout = new HorizontalLayout(button, new DatePicker("Pick a date"));
    	layout.setDefaultVerticalComponentAlignment(Alignment.END);
    	add(layout);
    	
    }


}
