package com.innovare.views.home;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AxisTitle;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Crosshair;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.DateTimeLabelFormats;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsBar;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsSpline;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.awt.Toolkit;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.ui.utils.Left;
import com.innovare.ui.utils.ListItem;
import com.innovare.ui.utils.BorderRadius;
import com.innovare.ui.utils.Bottom;
import com.innovare.ui.utils.BoxSizing;
import com.innovare.ui.utils.Display;
import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.ui.utils.FontSize;
import com.innovare.ui.utils.Horizontal;
import com.innovare.ui.utils.IconSize;
import com.innovare.ui.utils.LumoStyles;
import com.innovare.ui.utils.Right;
import com.innovare.ui.utils.Shadow;
import com.innovare.ui.utils.TextColor;
import com.innovare.ui.utils.Top;
import com.innovare.ui.utils.UIUtils;
import com.innovare.ui.utils.Uniform;
import com.innovare.utils.Sample;
import com.innovare.views.main.ContentView;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.Command;
import com.vaadin.ui.UI;


@CssImport("./styles/views/main/main-view.css")
@Route(value = "home", layout = ContentView.class)
@PageTitle("Home")
@RouteAlias(value = "", layout = ContentView.class)
public class HomeView extends Div {
	
	public static final String MAX_WIDTH = (Toolkit.getDefaultToolkit().getScreenSize().width - 100) + "px";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

	private ArrayList<Sample> samples;
	private HttpClient client;
	private URIBuilder builder;
	
    public HomeView() {
        setId("home-view");
        /*
         * Richiesta dati
         */
        /*builder = new URIBuilder();
		builder.setScheme("http").setHost("localhost:8888").setPath("/daysamples/1");
   	    client = HttpClient.newHttpClient();
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
        
        add(createContent());
        
    }
    
    private Component createContent() {
		Component data = createData();
		Component classifications = createClassifications();

		FlexBoxLayout content = new FlexBoxLayout(classifications, data);
		content.setAlignItems(FlexComponent.Alignment.CENTER);
		content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		return content;
	}
    
    // Crea il layout in cui aggiungere la chart delle ultime classificazioni
  	private Component createClassifications() {
  		FlexBoxLayout classifications = new FlexBoxLayout(
  				createHeader(VaadinIcon.FORWARD, "Ultime Classificazioni"),
  				createClassificationsChart());
  		classifications.setBoxSizing(BoxSizing.BORDER_BOX);
  		classifications.setDisplay(Display.BLOCK);
  		classifications.setMargin(Top.L);
  		classifications.setMaxWidth(MAX_WIDTH);
  		classifications.setPadding(Horizontal.RESPONSIVE_L);
  		classifications.setWidthFull();
  		return classifications;
  	}
    
  /*  private Component createAmbiente() {
		FlexBoxLayout temperatures = new FlexBoxLayout(
				createHeader(VaadinIcon.SUN_O, "Dati Ambientali"),
				createAreaChart());
		temperatures.setBoxSizing(BoxSizing.BORDER_BOX);
		temperatures.setDisplay(Display.BLOCK);
		temperatures.setMargin(Top.XL);
		temperatures.setMaxWidth(MAX_WIDTH);
		temperatures.setPadding(Horizontal.RESPONSIVE_L);
		temperatures.setWidthFull();
		return temperatures;
	}*/
    
    private FlexBoxLayout createHeader(VaadinIcon icon, String title) {
		FlexBoxLayout header = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.M, TextColor.TERTIARY.getValue(), icon),
				UIUtils.createH3Label(title));
		header.setAlignItems(FlexComponent.Alignment.CENTER);
		header.setMargin(Bottom.L, Horizontal.RESPONSIVE_L);
		header.setSpacing(Right.L);
		return header;
	}
    
    
    
 	// Crea la chart delle ultime 4 classificazioni
 	private Component createClassificationsChart() {
 		Chart chart = new Chart();
 		chart.setSizeFull();

         Configuration configuration = chart.getConfiguration();
         chart.getConfiguration().getChart().setType(ChartType.BAR);
         

         ListSeries carenza = new ListSeries("Carenza di acqua", 17, 9, 5, 10);
         PlotOptionsSeries posCarenza = new PlotOptionsSeries();
         posCarenza.setColorIndex(8);
         carenza.setPlotOptions(posCarenza);
         configuration.addSeries(carenza);
         
         ListSeries sane = new ListSeries("Sane", 75, 81, 95, 75);
         PlotOptionsSeries posSane = new PlotOptionsSeries();
         posSane.setColorIndex(2);
         sane.setPlotOptions(posSane);
         configuration.addSeries(sane);
         
         ListSeries eccesso = new ListSeries("Eccesso di acqua", 5, 8, 0, 10);
         PlotOptionsSeries posEccesso = new PlotOptionsSeries();
         posEccesso.setColorIndex(4);
         eccesso.setPlotOptions(posEccesso);
         configuration.addSeries(eccesso);
         
         ListSeries scartate = new ListSeries("Immagini scartate", 3, 2, 0, 5);
         PlotOptionsSeries posScartate = new PlotOptionsSeries();
         posScartate.setColorIndex(1);
         scartate.setPlotOptions(posScartate);
         configuration.addSeries(scartate);
         
         ListSeries infestanti = new ListSeries("Infestanti", 3, 2, 0, 5);
         PlotOptionsSeries posInfestanti = new PlotOptionsSeries();
         posInfestanti.setColorIndex(3);
         infestanti.setPlotOptions(posInfestanti);
         configuration.addSeries(infestanti);
         
         XAxis x = new XAxis();
         x.setCrosshair(new Crosshair());
         x.setCategories("08/09/2020", "28/08/2020", "18/08/2020", "08/08/2020", "28/07/2020");
         configuration.addxAxis(x);
     

         YAxis y = new YAxis();
         y.setMin(0);
         AxisTitle yTitle = new AxisTitle();
         yTitle.setText("Percentuale [%]");
         yTitle.setAlign(VerticalAlign.HIGH);
         y.setTitle(yTitle);
         
         configuration.addyAxis(y);

         Tooltip tooltip = new Tooltip();
         tooltip.setShared(true);
         tooltip.setValueSuffix("%");
         configuration.setTooltip(tooltip);
         
         chart.setSizeFull();
         FlexBoxLayout card = new FlexBoxLayout(chart);
         
 		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
 		card.setBorderRadius(BorderRadius.S);
 		card.setBoxSizing(BoxSizing.BORDER_BOX);
 		card.setHeight("400px");
 		card.setPadding(Uniform.M);
 		card.setShadow(Shadow.XS);
 		card.setMargin(Bottom.XL);
 		return card;
 	}
   
 	
 	private Component createData() {
		Component ambientData = createAmbientData();
		Component sensorData = createClassifications();

		Row docs = new Row(ambientData, sensorData);
		docs.addClassName(LumoStyles.Margin.Top.XL);
		docs.setComponentSpan(sensorData, 3);
		UIUtils.setMaxWidth(MAX_WIDTH, docs);
		docs.setWidthFull();

		return docs;
	}

	private Component createAmbientData() {
		FlexBoxLayout header = createHeader(VaadinIcon.SUN_O, "Dati Ambientali");
		
		Date lastMeasureDate = new Date();
		int lastMeasureTemp = 13;
		int maxTempToday = 15;
		int minTempToday = 9;
		int lastMeasureHum = 32;
		int lastMeasureHeat = 50;
		
		VaadinIcon weathIcon;
		String color;
		if(lastMeasureHeat < 50) {
			weathIcon = VaadinIcon.CLOUD_O;
			color = "#308DDF";
		}
		else {
			weathIcon = VaadinIcon.SUN_O;
			color = " #F5F219 ";
		}
	
			
		FlexBoxLayout dateLabel = new FlexBoxLayout(
				UIUtils.createLabel(FontSize.XS, DATE_FORMAT.format(lastMeasureDate) 
				+ " " + TIME_FORMAT.format(lastMeasureDate)));
		dateLabel.setMargin(Bottom.M);
		
		
		FlexBoxLayout tempLabel = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.L, color, weathIcon),
				UIUtils.createH1Label(lastMeasureTemp + "°C"));
		tempLabel.setAlignItems(Alignment.CENTER);
		tempLabel.setSpacing(Right.M);
		tempLabel.setMargin(Bottom.M);
		
		FlexBoxLayout maxTempLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.XS, "Max: " + maxTempToday + "°C"));
		FlexBoxLayout minTempLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.XS, "Min: " + minTempToday + "°C"));
		minTempLabel.setMargin(Bottom.M);
		
		FlexBoxLayout humLabel = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, TextColor.TERTIARY.getValue() , VaadinIcon.DROP),
				UIUtils.createLabel(FontSize.S, "Umidità: " + lastMeasureHum + "%"));
		humLabel.setSpacing(Right.XS);
		humLabel.setMargin(Bottom.M);

		/*Tabs tabs = new Tabs();
		for (String label : new String[]{"All", "Archive", "Workflows",
				"Support"}) {
			tabs.add(new Tab(label));
		}

		Div items = new Div(
				new ListItem(
						UIUtils.createIcon(IconSize.M, TextColor.TERTIARY,
								VaadinIcon.CHART),
						"Weekly Report", "Generated Oct 5, 2018",
						createInfoButton()),
				new ListItem(
						UIUtils.createIcon(IconSize.M, TextColor.TERTIARY,
								VaadinIcon.SITEMAP),
						"Payment Workflows", "Last modified Oct 24, 2018",
						createInfoButton()));
		items.addClassNames(LumoStyles.Padding.Vertical.S);*/
		
		FlexBoxLayout card = new FlexBoxLayout(dateLabel, tempLabel, maxTempLabel, minTempLabel, humLabel);
		card.setAlignItems(FlexComponent.Alignment.CENTER);
		card.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		card.setMinHeight("200px");
		UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, card);
		UIUtils.setBorderRadius(BorderRadius.S, card);
		UIUtils.setShadow(Shadow.XS, card);

		FlexBoxLayout reports = new FlexBoxLayout(header, card);
		reports.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		reports.setPadding(Bottom.XL, Right.RESPONSIVE_L, Left.RESPONSIVE_L);
		return reports;
	}

	private Component createSensorData() {
		FlexBoxLayout header = createHeader(VaadinIcon.ROAD, "Dati Suolo");
		
		Date lastMeasureDate = new Date();
		int lastMeasureTemp = 10;
		int lastMeasureHum = 68;

		Tabs tabs = new Tabs();
		for (String label : new String[]{"Sensore 1", "Sensore 2", "Sensore 3",
				"Sensore 4"}) {
			tabs.add(new Tab(label));
		}

		Div items = new Div(
				new ListItem(
						UIUtils.createIcon(IconSize.M, TextColor.TERTIARY.getValue(),
								VaadinIcon.SUN_O),
						"Temperatura: " + lastMeasureTemp + "°C", "Data: " + DATE_FORMAT.format(lastMeasureDate) 
						+ " " + TIME_FORMAT.format(lastMeasureDate)),
				new ListItem(
						UIUtils.createIcon(IconSize.M, TextColor.TERTIARY.getValue(),
								VaadinIcon.DROP),
						"Umidità: " + lastMeasureHum + "%", "Data: " + DATE_FORMAT.format(lastMeasureDate) 
						+ " " + TIME_FORMAT.format(lastMeasureDate)));
		items.addClassNames(LumoStyles.Padding.Vertical.XS);

		Div card = new Div(tabs, items);
		card.setMinHeight("200px");
		UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, card);
		UIUtils.setBorderRadius(BorderRadius.S, card);
		UIUtils.setShadow(Shadow.XS, card);

		FlexBoxLayout logs = new FlexBoxLayout(header, card);
		//logs.addClassName(CLASS_NAME + "__logs");
		logs.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		logs.setPadding(Bottom.XL, Right.RESPONSIVE_L, Left.RESPONSIVE_L);
		return logs;
	}
    
    /*private Component createAreaChart() {
    	final Random random = new Random();

        final Chart chart = new Chart();

        final Configuration configuration = chart.getConfiguration();
        configuration.getChart().setType(ChartType.SPLINE);
        configuration.getTitle().setText(DATE_FORMAT.format(new Date()));
        
        

        XAxis xAxis = configuration.getxAxis();
        xAxis.setType(AxisType.DATETIME);
        xAxis.setTickPixelInterval(75);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle(new AxisTitle("Temperatura [°C]"));
        yAxis.setMin(0);

        configuration.getTooltip().setEnabled(false);
        configuration.getLegend().setEnabled(false);

        final DataSeries series = new DataSeries();
        series.setPlotOptions(new PlotOptionsSpline());
        series.setName("Temperatura");
        
        if(samples == null) samples = new ArrayList<Sample>();
        for (Sample sample : samples) {
            series.add(new DataSeriesItem(sample.getTimestamp(),  sample.getMisure()));
        }

        configuration.setSeries(series);
        
        configuration.getTooltip().setEnabled(true);
        configuration.getTooltip().setValueSuffix("°C");
        configuration.getTooltip().setXDateFormat("%d/%m/%Y %H:%M");;
       

        runWhileAttached(chart, () -> {
        	/*builder.setScheme("http").setHost("localhost:8888").setPath("/lastsample/1");   
        	HttpRequest request;
   			try {
   				request = HttpRequest.newBuilder()
   				        .uri(builder.build())
   				        .build();
   				HttpResponse<String> response = client.send(request,
   				         HttpResponse.BodyHandlers.ofString());
   				//Controllo sul array vuoti
   				System.out.println("BODY: "+response.body());
   				//if(!response.body().isEmpty() && !response.body().equalsIgnoreCase("NO-NEW-SAMPLE")) {
	   				/*ArrayList<Sample> newSamples = new ObjectMapper().readValue(response.body(), new TypeReference<ArrayList<Sample>>(){});
	   				for(Sample s: newSamples) {
	   					this.samples.add(s);
	   					series.add(new DataSeriesItem(s.getTimestamp(), s.getMisure()), true, true);
	   				//}*/
   				//}
   					/*Sample newSam= new ObjectMapper().readValue( response.body(),Sample.class);
   					this.samples.add(newSam);
   					series.add(new DataSeriesItem(newSam.getTimestamp(),  newSam.getMisure()));
   				
   			} catch (IOException e) {
   				e.printStackTrace();
   			} catch (InterruptedException e) {
   				e.printStackTrace();
   			} catch (URISyntaxException e) {
   				e.printStackTrace();
   			}*/
        	     
   /*     }, 15*1000, 10000)
		
		FlexBoxLayout card = new FlexBoxLayout(chart);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setBorderRadius(BorderRadius.S);
		card.setBoxSizing(BoxSizing.BORDER_BOX);
		card.setHeight("400px");
		card.setPadding(Uniform.M);
		card.setShadow(Shadow.XS);
		card.setMargin(Bottom.XL);
		return card;
	}
    
  /*  public static void runWhileAttached(Component component, Command task,
            final int interval, final int initialPause) {
        component.addAttachListener(event -> {
            ScheduledExecutorService executor = Executors
                    .newScheduledThreadPool(1);

            component.getUI().ifPresent(ui -> ui.setPollInterval(interval));

            final ScheduledFuture<?> scheduledFuture = executor
                    .scheduleAtFixedRate(() -> {
                        component.getUI().ifPresent(ui -> ui.access(task));
                    }, initialPause, interval, TimeUnit.MILLISECONDS);

            component.addDetachListener(detach -> {
                scheduledFuture.cancel(true);
                detach.getUI().setPollInterval(-1);
            });
        });
    }*/

}






/*
 * 	public class SplineUpdatingEachSecond extends AbstractChartExample {

    @Override public void initDemo() {
        final Random random = new Random();

        final Chart chart = new Chart();

        final Configuration configuration = chart.getConfiguration();
        configuration.getChart().setType(ChartType.SPLINE);
        configuration.getTitle().setText("Live random data");

        XAxis xAxis = configuration.getxAxis();
        xAxis.setType(AxisType.DATETIME);
        xAxis.setTickPixelInterval(150);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle(new AxisTitle("Value"));

        configuration.getTooltip().setEnabled(false);
        configuration.getLegend().setEnabled(false);

        final DataSeries series = new DataSeries();
        series.setPlotOptions(new PlotOptionsSpline());
        series.setName("Random data");
        for (int i = -19; i <= 0; i++) {
            series.add(new DataSeriesItem(System.currentTimeMillis() + i * 1000, random.nextDouble()));
        }

        configuration.setSeries(series);

        runWhileAttached(chart, () -> {
                final long x = System.currentTimeMillis();
                final double y = random.nextDouble();
                series.add(new DataSeriesItem(x, y), true, true);
        }, 1000, 1000);

        add(chart);
    }
}*/
