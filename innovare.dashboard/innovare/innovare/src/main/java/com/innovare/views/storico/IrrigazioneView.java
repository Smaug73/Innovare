package com.innovare.views.storico;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.innovare.ui.utils.FontSize;
import com.innovare.ui.utils.UIUtils;
import com.innovare.utils.Irrigazione;
import com.innovare.views.home.HomeView;
import com.innovare.views.main.ContentView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "irrig", layout = ContentView.class)
@PageTitle("Irrigazioni")
@CssImport("./styles/views/main/main-view.css")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class IrrigazioneView extends StoricoView {
	
private ArrayList<Irrigazione> IRRIGAZIONI;
	
	public IrrigazioneView() {
		super();
		setId("irrigazione-view");
	}

	@Override
	protected void getData() {
		Timestamp time = new Timestamp(System.currentTimeMillis());
		IRRIGAZIONI = new ArrayList<>();
		IRRIGAZIONI.add(new Irrigazione(time, time, 45.87));
		IRRIGAZIONI.add(new Irrigazione(time, time, 60.22));
		IRRIGAZIONI.add(new Irrigazione(time, time, 76.03));
		IRRIGAZIONI.add(new Irrigazione(time, time, 43.99));
	}

	@Override
	protected Component createChart() {
		Grid<Irrigazione> grid = new Grid<>();
		grid.setSelectionMode(SelectionMode.SINGLE);

		
		ListDataProvider<Irrigazione> dataProvider = DataProvider.ofCollection(IRRIGAZIONI);
		
		grid.setDataProvider(dataProvider);
		grid.setSizeFull();
		grid.setHeightByRows(true);

		grid.addColumn(new ComponentRenderer<>(this::createDateStartLabel))
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Inizio")
				.setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn(new ComponentRenderer<>(this::createDateFinishLabel))
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Fine")
				.setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn(Irrigazione::getQuantita)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Quantità di Acqua Usata [L]")
				.setTextAlign(ColumnTextAlign.CENTER)
				.setSortable(true);
		
		
		
		grid.setVerticalScrollingEnabled(false);
		
		return super.createCard(grid);
	}
	
	// Crea gli item della colonna Inizio
	private Component createDateStartLabel(Irrigazione irrig) {
		String dateString = HomeView.DATE_FORMAT.format(irrig.getInizioIrrig()) 
				+ " " + HomeView.TIME_FORMAT.format(irrig.getInizioIrrig());
		return UIUtils.createLabel(FontSize.S, dateString);
	}
		
	// Crea gli item della colonna Fine
	private Component createDateFinishLabel(Irrigazione irrig) {
		String dateString = HomeView.DATE_FORMAT.format(irrig.getFineIrrig()) 
				+ " " + HomeView.TIME_FORMAT.format(irrig.getFineIrrig());
		return UIUtils.createLabel(FontSize.S, dateString);
	}
		
	

}
