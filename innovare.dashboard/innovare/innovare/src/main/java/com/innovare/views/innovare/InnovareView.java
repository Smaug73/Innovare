package com.innovare.views.innovare;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import com.innovare.ui.utils.Horizontal;
import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.ui.utils.Uniform;
import com.innovare.views.main.ContentView;

@Route(value = "about", layout = ContentView.class)
@PageTitle("Innovare")
public class InnovareView extends Div {

    public InnovareView() {
        setId("innovare-view");
        add(createContent());
    }
    
    private Component createContent() {
		Html titolo = new Html("<p>INNOVARE - gestIone iNtelligente e"
				+ "sosteNibile del fabbisogno idricO delle coltiVazioni mediAnte sensoRi aerei e di tErra</p>");

		Html descrizione = new Html("<p>INNOVARE è un sistema integrato ad elevato contenuto tecnologico per"
				+ " la gestIone iNtelligente e sosteNibile del fabbisogno idricO delle coltiVazioni mediAnte sensoRi aerei e di tErra."
				+ " La gestione è eseguita da un avanzato sistema software di Intelligenza Artificiale (IA), in grado di elaborare in tempo reale"
				+ " la più efficace strategia irrigua, sulla base delle informazioni acquisite dal monitoraggio dello stato idrico del suolo, dello"
				+ " stato di crescita delle piante, delle condizioni atmosferiche e, pilotarne l’attuazione per mezzo di un impianto irriguo"
				+ " automatizzato.\r\n"
				+ "Il monitoraggio è eseguito da un innovativo sottosistema hardware che comprende sensori in fibra ottica (FOS) per le"
				+ " rilevazioni in suolo, droni - Unmanned Aerial System - (UAV) equipaggiati con camere ad alta risoluzione per il"
				+ " telerilevamento aereo delle immagini della coltivazione ed una stazione metereologica per le rilevazioni atmosferiche</p>");



		FlexBoxLayout content = new FlexBoxLayout(titolo, descrizione);
		content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		content.setMargin(Horizontal.AUTO);
		content.setMaxWidth("900px");
		content.setPadding(Uniform.RESPONSIVE_L);
		return content;
	}


}
