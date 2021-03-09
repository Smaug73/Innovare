package com.innovare.views.storico;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.innovare.model.Sample;
import com.innovare.model.Wind;
import com.innovare.ui.utils.FontSize;
import com.innovare.ui.utils.UIUtils;
import com.innovare.utils.Channel;
import com.innovare.utils.Constants;
import com.innovare.utils.Direction;
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

@Route(value = "wind", layout = ContentView.class)
@PageTitle("Venti")
@CssImport("./styles/views/main/main-view.css")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class WindView extends StoricoView{
	
	//private ArrayList<Sample> WIND_DIRECTIONS;
	//private ArrayList<Sample> WIND_SPEEDS;
	private ArrayList<Wind> WIND;
	
	public WindView() {
		super();
		setId("wind-view");
	}

	@Override
	protected void getData() {
/*		ArrayList<Sample> WIND_DIRECTIONS = new ArrayList<>();
		WIND_DIRECTIONS.add(new Sample("0", System.currentTimeMillis() - 64587456, Channel.WIND_DIR.getName(), Direction.N.getNum()));
		WIND_DIRECTIONS.add(new Sample("1", System.currentTimeMillis() - 67364731, Channel.WIND_DIR.getName(), Direction.N.getNum()));
		WIND_DIRECTIONS.add(new Sample("2", System.currentTimeMillis() - 64252889, Channel.WIND_DIR.getName(), Direction.N.getNum()));
		
		ArrayList<Sample> WIND_SPEEDS = new ArrayList<>();
		WIND_SPEEDS.add(new Sample("15", System.currentTimeMillis() - 64587456, Channel.WIND_SPEED.getName(), (float) 8.6));
		WIND_SPEEDS.add(new Sample("16", System.currentTimeMillis() - 67364731, Channel.WIND_SPEED.getName(), (float) 8.6));
		WIND_SPEEDS.add(new Sample("17", System.currentTimeMillis() - 64252889, Channel.WIND_SPEED.getName(), (float) 8.6));
*/
		ArrayList<Sample> WIND_DIRECTIONS = HttpHandler.getAllSamples(Channel.WIND_DIR.getValue());
		ArrayList<Sample> WIND_SPEEDS = HttpHandler.getAllSamples(Channel.WIND_SPEED.getValue());
		
		
		
		WIND = new ArrayList<>();
		for(Sample dir : WIND_DIRECTIONS) {
			for(Sample speed : WIND_SPEEDS) {
				if(dir.getTimestamp() == speed.getTimestamp()) {
					//Fix per evitare che mostri sample incorretamente registrati dalla weatherstation
					if (speed.getMisure() != Channel.WIND_SPEED.getInvalidValue()) {
						Wind wind = new Wind(Direction.getDirection((int) dir.getMisure()), new Timestamp(dir.getTimestamp()), speed.getMisure());
						WIND.add(wind);
					}
				}
			}
		}
		
	}
	

	@Override
	protected Component createChart() {
		Grid<Wind> grid = new Grid<>();
		grid.setSelectionMode(SelectionMode.SINGLE);

		
		ListDataProvider<Wind> dataProvider = DataProvider.ofCollection(WIND);
		
		grid.setDataProvider(dataProvider);
		grid.setSizeFull();
		grid.setHeightByRows(true);

		grid.addColumn(new ComponentRenderer<>(this::createDateLabel))
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setFrozen(true)
				.setHeader("Data")
				.setSortable(true);
		grid.addColumn(Wind::getDirection)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Direzione")
				.setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn(Wind::getSpeed)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Velocità [m/s]")
				.setTextAlign(ColumnTextAlign.CENTER)
				.setSortable(true);
		
		
		grid.setVerticalScrollingEnabled(true);
		
		return super.createCard(grid);
	}
	
	// Crea gli item della colonna Data 
	private Component createDateLabel(Wind wind) {
		String dateString = Constants.DATE_FORMAT.format(wind.getDate());
		return UIUtils.createLabel(FontSize.S, dateString);
	}

}
