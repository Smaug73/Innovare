package com.innovare.views.storico;

import java.util.ArrayList;
import java.util.Iterator;

import com.innovare.model.Sample;
import com.innovare.utils.Channel;
import com.innovare.utils.HttpHandler;
import com.innovare.views.main.ContentView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Cursor;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.PlotOptionsAreasplinerange;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "rain", layout = ContentView.class)
@PageTitle("Piogge")
@CssImport("./styles/views/main/main-view.css")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class RainView extends StoricoView{
	private ArrayList<Sample> samples;
	
	public RainView() {
		super();
		setId("rain-view");
	}
	
	@Override
	protected void getData() {
		samples = HttpHandler.getAllSamples(Channel.DAY_RAIN.getValue());
		//FILTRO ELIMINO I SAMPLE NON CORRETTI
		for (Iterator<Sample> iterator = samples.iterator(); iterator.hasNext(); ) {
			Sample value = iterator.next();
			if (value.getMisure() == Channel.UV_LEVEL.getInvalidValue()) {
				iterator.remove();
			}
		}
	}

	@Override
	protected Component createChart() {
		
		final Chart chart = new Chart(ChartType.AREASPLINE);

        Configuration configuration = chart.getConfiguration();
        
        Tooltip tooltip = configuration.getTooltip();
        tooltip.setValueSuffix("mm");
        
        DataSeries dataSeries = new DataSeries("Piogge");
        if(samples == null) samples = new ArrayList<Sample>();
        for (Sample sample : samples) {
            dataSeries.add(new DataSeriesItem(sample.getTimestamp(), sample.getMisure(), sample.getMisure()));
        }
        configuration.setSeries(dataSeries);
        
        chart.setTimeline(true);
        
        PlotOptionsAreasplinerange opt = new PlotOptionsAreasplinerange();
        configuration.setPlotOptions(opt);
        
        return super.createCard(chart);
		
		
/*
		final Chart chart = new Chart(ChartType.COLUMN);
        chart.setId("rain-chart");

        final Configuration conf = chart.getConfiguration();

        conf.setTitle("Piogge");
        conf.getLegend().setEnabled(false);

        XAxis x = new XAxis();
        x.setType(AxisType.CATEGORY);
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setTitle("Quantità di pioggia [mm]");
        conf.addyAxis(y);

        PlotOptionsColumn column = new PlotOptionsColumn();
        column.setCursor(Cursor.POINTER);
        column.setDataLabels(new DataLabels(true));

        conf.setPlotOptions(column);

        DataSeries yearsSeries = new DataSeries();
        yearsSeries.setName("Anni");

        DataSeriesItem yearItem = new DataSeriesItem(
                "2019", 624);
        DataSeries monthsSeries = new DataSeries("Mesi");
        monthsSeries.setId("2019");

        DataSeriesItem monthItem = new DataSeriesItem("Gen", 12);
        DataSeries detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Gen 2019");
        String[] categories31 = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9",
        		"10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22",
        		"23", "24", "25", "26", "27", "28", "29", "30", "31"};
        Number[] mm = new Number[] { 79.3, 7.3, 2.5, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4,5,4,4 };
        detailsSeries.setData(categories31, mm);
        monthsSeries.addItemWithDrilldown(monthItem, detailsSeries);

        monthItem = new DataSeriesItem("Feb", 59.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Feb 2019");
        String[] categories28 = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9",
        		"10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22",
        		"23", "24", "25", "26", "27", "28"};
        mm = new Number[] { 73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4};
        detailsSeries.setData(categories28, mm);
        monthsSeries.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Mar", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Mar 2019");
        mm = new Number[] { 73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4,9,0.9,54};
        detailsSeries.setData(categories31, mm);
        monthsSeries.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Apr", 400);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Apr 2019");
        String[] categories30 = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9",
        		"10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22",
        		"23", "24", "25", "26", "27", "28", "29", "30"};
        mm = new Number[] { 73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4, 8.9, 109.8};
        detailsSeries.setData(categories30, mm);
        monthsSeries.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Mag", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Mag 2019");
        mm = new Number[] { 73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4,9,0.9,54};
        detailsSeries.setData(categories31, mm);
        monthsSeries.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Giu", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Giu 2019");
        mm = new Number[] { 73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4,4,4};
        detailsSeries.setData(categories30, mm);
        monthsSeries.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Lug", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Lug 2019");
        mm = new Number[] {73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4,9,0.9,54};
        detailsSeries.setData(categories31, mm);
        monthsSeries.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Ago", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Ago 2019");
        mm = new Number[] { 73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4,9,0.9,54};
        detailsSeries.setData(categories31, mm);
        monthsSeries.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Set", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Set 2019");
        mm = new Number[] { 73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4,4,4};
        detailsSeries.setData(categories30, mm);
        monthsSeries.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Ott", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Ott 2019");
        mm = new Number[] {73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4,9,0.9,54};
        detailsSeries.setData(categories31, mm);
        monthsSeries.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Nov", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Nov 2019");
        mm = new Number[] {  73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4,4,4};
        detailsSeries.setData(categories30, mm);
        monthsSeries.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Dic", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Dic 2019");
        mm = new Number[] {73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4,9,0.9,54};
        detailsSeries.setData(categories31, mm);
        monthsSeries.addItemWithDrilldown(monthItem, detailsSeries);

        yearsSeries.addItemWithDrilldown(yearItem, monthsSeries);
        
        yearItem = new DataSeriesItem("2020", 562.9);
        DataSeries monthsSeries2020 = new DataSeries("Mesi");
        monthsSeries2020.setId("Anno 2020");
        
        monthItem = new DataSeriesItem("Gen", 12);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Gen 2020");
        mm = new Number[] { 79.3, 7.3, 2.5, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4,5,4,4 };
        detailsSeries.setData(categories31, mm);
        monthsSeries2020.addItemWithDrilldown(monthItem, detailsSeries);

        monthItem = new DataSeriesItem("Feb", 59.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Feb 2020");
        mm = new Number[] { 73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4};
        detailsSeries.setData(categories28, mm);
        monthsSeries2020.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Mar", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Mar 2020");
        mm = new Number[] { 73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4, 5.78, 87.3, 42.9};
        detailsSeries.setData(categories31, mm);
        monthsSeries2020.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Apr", 400);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Apr 2020");
        mm = new Number[] { 73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4, 8.9, 109.8};
        detailsSeries.setData(categories30, mm);
        monthsSeries2020.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Mag", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Mag 2020");
        mm = new Number[] {  73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4, 5.78, 87.3, 42.9};
        detailsSeries.setData(categories31, mm);
        monthsSeries2020.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Giu", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Giu 2020");
        mm = new Number[] { 73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4, 8.9, 109.8};
        detailsSeries.setData(categories30, mm);
        monthsSeries2020.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Lug", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Lug 2020");
        mm = new Number[] {  73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4, 5.78, 87.3, 42.9};
        detailsSeries.setData(categories31, mm);
        monthsSeries2020.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Ago", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Ago 2020");
        mm = new Number[] {  73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4, 5.78, 87.3, 42.9};
        detailsSeries.setData(categories31, mm);
        monthsSeries2020.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Set", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Set 2020");
        mm = new Number[] {73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4, 8.9, 109.8};
        detailsSeries.setData(categories30, mm);
        monthsSeries2020.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Ott", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Ott 2020");
        mm = new Number[] {  73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13, 14, 52, 63, 22, 98, 56, 0.9, 6,3,8,4,2,9,6,4, 5.78, 87.3, 42.9};
        detailsSeries.setData(categories31, mm);
        monthsSeries2020.addItemWithDrilldown(monthItem, detailsSeries);
        
        monthItem = new DataSeriesItem("Nov", 309.8);
        detailsSeries = new DataSeries("Giorni");
        detailsSeries.setId("Nov 2020");
        String[] categories13 = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"};
        mm = new Number[] {73.7, 6.4, 1.8, 4, 5, 3, 9, 0.2, 4, 5, 2.7, 12, 13};
        detailsSeries.setData(categories13, mm);
        monthsSeries2020.addItemWithDrilldown(monthItem, detailsSeries);
        

        yearsSeries.addItemWithDrilldown(yearItem, monthsSeries2020);

        conf.addSeries(yearsSeries);
        
        

		return super.createCard(chart);
*/
	}


}
