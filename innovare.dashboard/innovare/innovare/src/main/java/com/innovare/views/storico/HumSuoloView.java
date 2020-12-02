package com.innovare.views.storico;

import java.util.ArrayList;

import com.innovare.utils.Sample;
import com.innovare.views.main.ContentView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.PlotOptionsAreasplinerange;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "humsuolo", layout = ContentView.class)
@PageTitle("Umidità Del Suolo")
@CssImport("./styles/views/main/main-view.css")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class HumSuoloView extends StoricoView {
	
	private ArrayList<Sample> samples;

	public HumSuoloView() {
		super();
		setId("umidità-suolo-view");
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
		
	}


	@Override
	protected Component createChart() {
		final Chart chart = new Chart(ChartType.AREASPLINERANGE);

        Configuration configuration = chart.getConfiguration();
        
        Tooltip tooltip = configuration.getTooltip();
        tooltip.setValueSuffix("%");
        
        DataSeries dataSeries = new DataSeries("Variazione Umidità");
        if(samples == null) samples = new ArrayList<Sample>();
        for (Sample sample : samples) {
            dataSeries.add(new DataSeriesItem(sample.getTimestamp(), sample.getMisure(), sample.getMisure()));
        }
        configuration.setSeries(dataSeries);
        
        chart.setTimeline(true);
        
        PlotOptionsAreasplinerange opt = new PlotOptionsAreasplinerange();
        configuration.setPlotOptions(opt);
        
        return super.createCard(chart);
	}

}
