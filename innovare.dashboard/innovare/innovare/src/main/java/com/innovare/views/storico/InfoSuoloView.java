package com.innovare.views.storico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.innovare.model.Sample;
import com.innovare.ui.utils.FlexBoxLayout;
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
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "tempsuolo", layout = ContentView.class)
@PageTitle("Temperatura Del Suolo")
@CssImport("./styles/views/main/main-view.css")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class InfoSuoloView extends StoricoView{
	
	private HashMap<Integer, ArrayList<Sample>> sensors;

	public InfoSuoloView() {
		super();
		setId("temperatura-suolo-view");
	}

	@Override
	protected void getData() {
		///
		sensors = new HashMap<Integer, ArrayList<Sample>>();
		ArrayList<Integer> channels = HttpHandler.getActiveChannels();
		for(Integer channel : channels) {
			ArrayList<Sample> samples = HttpHandler.getAllSamples(channel);
			//FILTRO ELIMINO I SAMPLE NON CORRETTI
			for (Iterator<Sample> iterator = samples.iterator(); iterator.hasNext(); ) {
				Sample value = iterator.next();
			    if (value.getMisure() == -273.15) {
			        iterator.remove();
			    }
			}
			sensors.put(channel, samples);
		}
		////
		//sensors = new HashMap<Integer, ArrayList<Sample>>();
		//ArrayList<Integer> channels = new ArrayList<Integer>();
/*		
		channels.add(16);
		channels.add(17);
		channels.add(20);
		for(Integer channel : channels) {
			ArrayList<Sample> samples = new ArrayList<Sample>();
			Sample sample1 = new Sample("id1", System.currentTimeMillis() - 36000, "" + channel, (float)channel);
			Sample sample2 = new Sample("id2", System.currentTimeMillis(), "" + channel, (float)channel - 2);
			samples.add(sample1);
			samples.add(sample2);
			sensors.put(channel, samples);
		}
*/
	}


	@Override
	protected Component createChart() {
		FlexBoxLayout content = new FlexBoxLayout();
		for(Integer channel : sensors.keySet()) {
			content.add(createChart(sensors.get(channel), "Sensore Canale " + channel));
		}
		
		content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
        return content;
	}
	
	private Component createChart(ArrayList<Sample> samples, String nameSensor) {
		
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
        configuration.setTitle(nameSensor);
        
        chart.setTimeline(true);
        
        PlotOptionsAreasplinerange opt = new PlotOptionsAreasplinerange();
        configuration.setPlotOptions(opt);
        
        return super.createCard(chart);
	}
}
