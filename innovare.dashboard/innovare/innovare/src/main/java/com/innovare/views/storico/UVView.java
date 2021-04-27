package com.innovare.views.storico;

import java.util.ArrayList;
import java.util.Iterator;

import com.innovare.model.Sample;
import com.innovare.ui.utils.FontSize;
import com.innovare.ui.utils.UIUtils;
import com.innovare.utils.Channel;
import com.innovare.utils.Constants;
import com.innovare.utils.HttpHandler;
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

@Route(value = "uv", layout = ContentView.class)
@PageTitle("UV Levels")
@CssImport("./styles/views/main/main-view.css")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class UVView extends StoricoView{

	private ArrayList<Sample> uvLevels;
	
	public UVView() {
		super();
		setId("uv-view");
	}

	@Override
	protected void getData() {
		uvLevels = HttpHandler.getAllSamples(Channel.UV_LEVEL.getValue());
		
		//FILTRO ELIMINO I SAMPLE NON CORRETTI
		for (Iterator<Sample> iterator = uvLevels.iterator(); iterator.hasNext(); ) {
			Sample value = iterator.next();
		    if (value.getMisure() == Channel.UV_LEVEL.getInvalidValue()) {
		        iterator.remove();
		    }
		}
	}

	@Override
	protected Component createChart() {
		Grid<Sample> grid = new Grid<>();
		grid.setSelectionMode(SelectionMode.SINGLE);

		
		ListDataProvider<Sample> dataProvider = DataProvider.ofCollection(uvLevels);
		
		grid.setDataProvider(dataProvider);
		grid.setSizeFull();
		grid.setHeightByRows(true);

		grid.addColumn(new ComponentRenderer<>(this::createDateLabel))
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setFrozen(true)
				.setHeader("Data")
				.setSortable(true);
		grid.addColumn(Sample::getMisure)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Valore")
				.setTextAlign(ColumnTextAlign.CENTER);
		
		
		grid.setVerticalScrollingEnabled(true);
		
		return super.createCard(grid);
	}
	
	// Crea gli item della colonna Data 
	private Component createDateLabel(Sample sample) {
		String dateString = Constants.DATE_FORMAT.format(sample.getTimestamp());
		return UIUtils.createLabel(FontSize.S, dateString);
	}

}
