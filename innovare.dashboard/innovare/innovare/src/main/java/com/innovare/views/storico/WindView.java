package com.innovare.views.storico;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.innovare.ui.utils.FontSize;
import com.innovare.ui.utils.UIUtils;
import com.innovare.utils.Wind;
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
	
	private ArrayList<Wind> WIND;
	
	public WindView() {
		super();
		setId("wind-view");
	}

	@Override
	protected void getData() {
		/*URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("localhost:8888").setPath("/allsample/1");
	        
	    HttpClient client = HttpClient.newHttpClient();
	        HttpRequest request;
			try {
				request = HttpRequest.newBuilder()
				        .uri(builder.build())
				        .build();
				HttpResponse<String> response = client.send(request,
				         HttpResponse.BodyHandlers.ofString());
				
				samples = new ObjectMapper().readValue(response.body(), new TypeReference<ArrayList<Sample>>(){});
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}*/

		WIND = new ArrayList<>();
		WIND.add(new Wind(Wind.Direction.N, LocalDate.now(), 5));
		WIND.add(new Wind(Wind.Direction.NNE, LocalDate.now(), 2));
		WIND.add(new Wind(Wind.Direction.S, LocalDate.now(), 1));
		WIND.add(new Wind(Wind.Direction.W, LocalDate.now(), 1.7));
		WIND.add(new Wind(Wind.Direction.WNW, LocalDate.now(), 2.3));
		WIND.add(new Wind(Wind.Direction.E, LocalDate.now(), 8.4));
		WIND.add(new Wind(Wind.Direction.E, LocalDate.now(), 8.4));
		WIND.add(new Wind(Wind.Direction.E, LocalDate.now(), 8.4));
		WIND.add(new Wind(Wind.Direction.E, LocalDate.now(), 8.4));
		WIND.add(new Wind(Wind.Direction.E, LocalDate.now(), 8.4));
		WIND.add(new Wind(Wind.Direction.E, LocalDate.now(), 8.4));
		WIND.add(new Wind(Wind.Direction.E, LocalDate.now(), 8.4));
		WIND.add(new Wind(Wind.Direction.E, LocalDate.now(), 8.4));
		WIND.add(new Wind(Wind.Direction.E, LocalDate.now(), 8.4));
		WIND.add(new Wind(Wind.Direction.E, LocalDate.now(), 8.4));
		
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
			String dateString = wind.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			return UIUtils.createLabel(FontSize.S, dateString);
		}

}
