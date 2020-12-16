package com.innovare.views.storico;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.backend.stub.DummyData;
import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.ui.utils.FontSize;
import com.innovare.ui.utils.IconSize;
import com.innovare.ui.utils.Right;
import com.innovare.ui.utils.TextColor;
import com.innovare.ui.utils.UIUtils;
import com.innovare.utils.Classification;
import com.innovare.utils.Classification.Status;
import com.innovare.views.home.HomeView;
import com.innovare.views.main.ContentView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


@Route(value = "classif", layout = ContentView.class)
@PageTitle("Classificazioni")
@CssImport("./styles/views/main/main-view.css")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class ClassificazioniView extends StoricoView{
	
	private ArrayList<Classification> CLASSIFICATIONS;
	
	public ClassificazioniView() {
		super();
		setId("classificazioni-view");
	}
	
	@Override
	protected void getData() {
		/* Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		CLASSIFICATIONS = new ArrayList<>();
		CLASSIFICATIONS.add(new Classification(Classification.Status.CARENZA, timestamp, 73, 10, 8, 9, 4, "modello1"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.INFESTANTI, timestamp, 6, 20, 73, 1, 79, "modello2"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 3, 75, 20, 2, 4, "modello1"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.CARENZA, timestamp, 58, 29, 13, 7, 4, "modello1"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.ECCESSO, timestamp, 2, 88, 10, 7, 4, "modello1"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 8, 90, 2, 7, 4, "modello1"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 1, 93, 6, 7, 4, "modello1"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 4, 84, 12, 7, 4, "modello1"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 0, 100, 0, 7, 4, "modello1"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 0, 100, 0, 7, 4, "modello1"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 0, 100, 0, 7, 4, "modello2"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 0, 100, 0, 7, 4, "modello2"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 0, 100, 0, 7, 4, "modello2"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 0, 100, 0, 7, 4, "modello2"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 0, 100, 0, 7, 4, "modello2"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 0, 100, 0, 7, 4, "modello2"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 0, 100, 0, 7, 4, "modello2"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.SCARTATE, timestamp, 0, 0, 0, 100, 4, "modello2"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 0, 100, 0, 6, 4, "modello2"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 0, 100, 0, 3, 4, "modello2"));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, timestamp, 0, 100, 0, 9, 4, "modello2"));
		*/
		
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("localhost:8888").setPath("/getClassifications");
	        
	    HttpClient client = HttpClient.newHttpClient();
	        HttpRequest request;
			try {
				request = HttpRequest.newBuilder()
				        .uri(builder.build())
				        .build();
				HttpResponse<String> response = client.send(request,
				         HttpResponse.BodyHandlers.ofString());
				
				CLASSIFICATIONS = new ObjectMapper().readValue(response.body(), new TypeReference<ArrayList<Classification>>(){});
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		
	}

	
	@Override
	protected Component createChart() {
		Grid<Classification> grid = new Grid<>();
		grid.setSelectionMode(SelectionMode.SINGLE);

		
		ListDataProvider<Classification> dataProvider = DataProvider.ofCollection(CLASSIFICATIONS);
		
		grid.setDataProvider(dataProvider);
		grid.setSizeFull();
		grid.setHeightByRows(true);

		grid.addColumn(new ComponentRenderer<>(this::createDateLabel))
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setFrozen(true)
				.setHeader("Data Volo")
				.setSortable(true);
		grid.addColumn(new ComponentRenderer<>(this::createStatusLabel))
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setFrozen(true)
				.setHeader("Stato")
				.setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn(Classification::getModel)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Classificatore")
				.setFrozen(true)
				.setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn(Classification::getPercCarenza)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Carenza di acqua [%]")
				.setTextAlign(ColumnTextAlign.CENTER)
				.setSortable(true);
		grid.addColumn(Classification::getPercSane)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Sane [%]")
				.setTextAlign(ColumnTextAlign.CENTER)
				.setSortable(true);
		grid.addColumn(Classification::getPercEccesso)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Eccesso di acqua [%]")
				.setTextAlign(ColumnTextAlign.CENTER)
				.setSortable(true);
		grid.addColumn(Classification::getPercScartate)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Immagini scartate [%]")
				.setTextAlign(ColumnTextAlign.CENTER)
				.setSortable(true);
		grid.addColumn(Classification::getPercInfestanti)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Infestanti [%]")
				.setTextAlign(ColumnTextAlign.CENTER)
				.setSortable(true);
		
		
		
		grid.setVerticalScrollingEnabled(false);
		
		return super.createCard(grid);
	}
	
	
	// Crea gli item della colonna Status
	private Component createStatusLabel(Classification classif) {
		String textColor;
		if(classif.getStatus().equals(Status.CARENZA.getName())) textColor = TextColor.ERROR.getValue();
		else if(classif.getStatus().equals(Status.ECCESSO.getName())) textColor = TextColor.PRIMARY.getValue();
		else if(classif.getStatus().equals(Status.NORMALE.getName())) textColor = TextColor.SUCCESS.getValue();
		else if(classif.getStatus().equals(Status.INFESTANTI.getName())) textColor = TextColor.ORANGE.getValue();
		else textColor = TextColor.GREY.getValue();
			
		FlexBoxLayout status = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, textColor, VaadinIcon.CIRCLE),
				UIUtils.createLabel(FontSize.S, classif.getStatus()));
		status.setSpacing(Right.L);
		return status;
	}
	
	
	// Crea gli item della colonna Data Volo
	private Component createDateLabel(Classification classif) {
		String dateString = classif.getDate().toString();
		return UIUtils.createLabel(FontSize.S, dateString);
	}
}

