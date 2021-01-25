package com.innovare.views.storico;

import java.util.ArrayList;
import java.util.HashMap;

import com.innovare.model.Sample;
import com.innovare.model.Sensor;
import com.innovare.ui.utils.Bottom;
import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.ui.utils.Horizontal;
import com.innovare.ui.utils.IconSize;
import com.innovare.ui.utils.Right;
import com.innovare.ui.utils.TextColor;
import com.innovare.ui.utils.UIUtils;
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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "humsuolo", layout = ContentView.class)
@PageTitle("Umidità Del Suolo")
@CssImport("./styles/views/main/main-view.css")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class HumSuoloView extends StoricoView {
	
	private HashMap<Integer, ArrayList<Sample>> sensors;

	public HumSuoloView() {
		super();
		setId("umidità-suolo-view");
	}

	@Override
	protected void getData() {
		sensors = new HashMap<Integer, ArrayList<Sample>>();
		ArrayList<Integer> channels = HttpHandler.getActiveChannels();
		for(Integer channel : channels) {
			ArrayList<Sample> samples = HttpHandler.getAllSamples(channel);
			sensors.put(channel, samples);
		}
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
        configuration.setTitle(nameSensor);
        
        chart.setTimeline(true);
        
        PlotOptionsAreasplinerange opt = new PlotOptionsAreasplinerange();
        configuration.setPlotOptions(opt);
        
        return super.createCard(chart);
	}

}
