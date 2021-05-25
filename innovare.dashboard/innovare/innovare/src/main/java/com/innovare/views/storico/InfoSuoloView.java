package com.innovare.views.storico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.innovare.model.ChannelMeasure;
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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "infosuolo", layout = ContentView.class)
@PageTitle("Dati Suolo")
@CssImport("./styles/views/main/main-view.css")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class InfoSuoloView extends StoricoView{
	
	private HashMap<Integer, ArrayList<Sample>> sensors;
	private HashMap<Integer, String> channelMeasures;

	public InfoSuoloView() {
		super();
		setId("info-suolo-view");
	}

	@Override
	protected void getData() {
/*		sensors = new HashMap<Integer, ArrayList<Sample>>();
		ArrayList<Integer> channels = new ArrayList<Integer>();
		
		channels.add(10);
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
		sensors = new HashMap<Integer, ArrayList<Sample>>();
		channelMeasures = new HashMap<Integer, String>();
		
		
		try {
			ArrayList<Integer> channels = HttpHandler.getActiveChannels();
			
			// Si filtrano i canali in modo da prendere solo quelli relativi al suolo
			for (Iterator<Integer> iterator = channels.iterator(); iterator.hasNext(); ) {
				Integer value = iterator.next();
				Channel[] other_channels = Channel.values();
				for(Channel ch : other_channels) {
					if (value == ch.getValue()) {
						iterator.remove();
					}
				}
			}
		
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
			
			ArrayList<ChannelMeasure> measures = HttpHandler.getChannelMeasures();
			
			for(ChannelMeasure cm : measures) {
				channelMeasures.put(cm.getChannelnum(), cm.getMeasure());
			}
		}
		catch(NullPointerException e) {
			Notification notif = Notification.show("Controlla la tua connessione");
			notif.setPosition(Position.BOTTOM_END);
			notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
		}
		
		

	}


	@Override
	protected Component createChart() {
		FlexBoxLayout content = new FlexBoxLayout();
		for(Integer channel : sensors.keySet()) {
			content.add(createChart(sensors.get(channel), channel));
		}
		
		content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
        return content;
	}
	
	private Component createChart(ArrayList<Sample> samples, Integer sensorChannel) {
		
		final Chart chart = new Chart(ChartType.AREASPLINE);

        Configuration configuration = chart.getConfiguration();
        
        Tooltip tooltip = configuration.getTooltip();
        tooltip.setValueSuffix(channelMeasures.get(sensorChannel));
        
        DataSeries dataSeries = new DataSeries("Info");
        if(samples == null) samples = new ArrayList<Sample>();
        for (Sample sample : samples) {
            dataSeries.add(new DataSeriesItem(sample.getTimestamp(), sample.getMisure(), sample.getMisure()));
        }
        configuration.setSeries(dataSeries);
        configuration.setTitle("Sensore Canale " + sensorChannel);
        
        chart.setTimeline(true);
        
        PlotOptionsAreasplinerange opt = new PlotOptionsAreasplinerange();
        configuration.setPlotOptions(opt);
        
        return super.createCard(chart);
	}
}
