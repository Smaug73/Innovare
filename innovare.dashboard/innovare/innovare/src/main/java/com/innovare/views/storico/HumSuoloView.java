package com.innovare.views.storico;

import java.util.ArrayList;

import com.innovare.model.Sample;
import com.innovare.ui.utils.Bottom;
import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.ui.utils.Horizontal;
import com.innovare.ui.utils.IconSize;
import com.innovare.ui.utils.Right;
import com.innovare.ui.utils.TextColor;
import com.innovare.ui.utils.UIUtils;
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
	
	private ArrayList<Sample> samples1;
	private ArrayList<Sample> samples2;
	private ArrayList<Sample> samples3;
	private ArrayList<Sample> samples4;
	private ArrayList<Sample> samples5;

	public HumSuoloView() {
		super();
		setId("umidità-suolo-view");
	}

	@Override
	protected void getData() {
	}


	@Override
	protected Component createChart() {
		Component chart1 = createChart(samples1, "Sensore1");
		Component chart2 = createChart(samples2, "Sensore2");
		Component chart3 = createChart(samples3, "Sensore3");
		Component chart4 = createChart(samples4, "Sensore4");
		Component chart5 = createChart(samples5, "Sensore5");
		
		FlexBoxLayout content = new FlexBoxLayout(chart1, chart2, chart3, chart4, chart5);
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
