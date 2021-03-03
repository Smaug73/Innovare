package com.innovare.views.storico;

import java.util.ArrayList;

import com.innovare.model.Sample;
import com.innovare.utils.Channel;
import com.innovare.utils.HttpHandler;
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

@Route(value = "tempamb", layout = ContentView.class)
@PageTitle("Temperatura Ambientale")
@CssImport("./styles/views/main/main-view.css")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class TempAmbView extends StoricoView{
	
	private ArrayList<Sample> samples;

	public TempAmbView() {
		super();
		setId("temperatura-ambientale-view");
	}

	@Override
	protected void getData() {
		samples = HttpHandler.getAllSamples(Channel.OUTSIDE_TEMP.getValue());
		
		//FILTRO ELIMINO I SAMPLE NON CORRETTI
		for(Sample s: samples) {
			if(s.getMisure() == Channel.OUTSIDE_TEMP.getInvalidValue())
					samples.remove(s);
		}
	}


	@Override
	protected Component createChart() {
		final Chart chart = new Chart(ChartType.AREASPLINE);
		
        Configuration configuration = chart.getConfiguration();
        
        Tooltip tooltip = configuration.getTooltip();
        tooltip.setValueSuffix("°C");
        
        DataSeries dataSeries = new DataSeries("Temperatura");
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
