package com.innovare.views.home;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AxisTitle;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Crosshair;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.PointPlacement;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
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
import com.innovare.utils.IrrigationState;
import com.innovare.utils.Property;
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
		Component irrigation = createIrrigationState();
		Component lastIrrigation = createLastIrrigation();

		FlexBoxLayout content = new FlexBoxLayout(irrigation, lastIrrigation, data);
		content.setAlignItems(FlexComponent.Alignment.CENTER);
		content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		return content;
	}
    
    private Component createLastIrrigation() {
    	FlexBoxLayout lastIrr = new FlexBoxLayout(
  				createHeader(VaadinIcon.DROP, "Ultima Irrigazione"),
  				createLastIrrigationCard());
    	lastIrr.setBoxSizing(BoxSizing.BORDER_BOX);
    	lastIrr.setDisplay(Display.BLOCK);
    	lastIrr.setMargin(Top.L);
    	lastIrr.setMaxWidth(MAX_WIDTH);
    	lastIrr.setPadding(Horizontal.RESPONSIVE_L);
    	lastIrr.setWidthFull();
  		return lastIrr;
	}

	private Component createLastIrrigationCard() {
		
		FlexBoxLayout fromLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Dalle:"));
		fromLabel.setWidth("200px");
		FlexBoxLayout fromDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, DATE_FORMAT.format(new Date()) 
				+ " " + TIME_FORMAT.format(new Date())));
		
		
		FlexBoxLayout from = new FlexBoxLayout(fromLabel, fromDate);
		from.setFlexDirection(FlexLayout.FlexDirection.ROW);
		from.setFlexGrow(2, fromLabel, fromDate);
		
		FlexBoxLayout toLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Alle:"));
		toLabel.setWidth("200px");
		/*FlexBoxLayout toDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, DATE_FORMAT.format(new Date()) 
				+ " " + TIME_FORMAT.format(new Date())));*/
		FlexBoxLayout toDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "In corso"));
		
		FlexBoxLayout to = new FlexBoxLayout(toLabel, toDate);
		to.setFlexDirection(FlexLayout.FlexDirection.ROW);
		to.setFlexGrow(2, toLabel, toDate);
		
		FlexBoxLayout quantitaLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Quantità:"));
		quantitaLabel.setWidth("200px");
		FlexBoxLayout quantitaL = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "230.92 L"));
		
		
		FlexBoxLayout quantita = new FlexBoxLayout(quantitaLabel, quantitaL);
		to.setFlexDirection(FlexLayout.FlexDirection.ROW);
		to.setFlexGrow(2, quantitaLabel, quantitaL);
		
		FlexBoxLayout card = new FlexBoxLayout(from, to, quantita);
		card.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setBorderRadius(BorderRadius.S);
		card.setBoxSizing(BoxSizing.BORDER_BOX);
		card.setPadding(Uniform.M);
		card.setShadow(Shadow.XS);
		card.setHeightFull();
		card.setMargin(Bottom.L);
		return card;
	}

	private Component createIrrigationState() {
    	FlexBoxLayout irrState = new FlexBoxLayout(
  				createHeader(VaadinIcon.SLIDER, "Stato Irrigazione"),
  				createIrrigationStateCard());
    	irrState.setBoxSizing(BoxSizing.BORDER_BOX);
  		irrState.setDisplay(Display.BLOCK);
  		irrState.setMargin(Top.L);
  		irrState.setMaxWidth(MAX_WIDTH);
  		irrState.setPadding(Horizontal.RESPONSIVE_L);
  		irrState.setWidthFull();
  		return irrState;
	}

	private Component createIrrigationStateCard() {
		
		/* 
		 * Recupero stato irrigazione da middleware per iniziallizare
		 * la variabile booleana "acceso"
		 */
		boolean acceso = true;
		String state;
		String colorState;
		if(acceso) {
			state = IrrigationState.ACCESO.getName();
			colorState = IrrigationState.ACCESO.getColor();
		}
		else {
			state = IrrigationState.SPENTO.getName();
			colorState = IrrigationState.SPENTO.getColor();
		}
		
		FlexBoxLayout descLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Stato dell'impianto irriguo:"));
		descLabel.setWidth("200px");
		FlexBoxLayout status = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, colorState, VaadinIcon.CIRCLE),
				UIUtils.createLabel(FontSize.L, state));
		status.setSpacing(Right.S);
		status.setAlignItems(Alignment.CENTER);
		
		
		FlexBoxLayout card = new FlexBoxLayout(descLabel, status);
		card.setFlexDirection(FlexLayout.FlexDirection.ROW);
		card.setFlexGrow(2, descLabel, status);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setBorderRadius(BorderRadius.S);
		card.setBoxSizing(BoxSizing.BORDER_BOX);
		card.setPadding(Uniform.M);
		card.setShadow(Shadow.XS);
		card.setHeightFull();
		card.setMargin(Bottom.L);
		return card;
	}

	private Component createData() {
		Component ambientData = createAmbientData();
		Component classificationData = createClassifications();

		Row docs = new Row(ambientData, classificationData);
		docs.addClassName(LumoStyles.Margin.Top.XL);
		docs.setComponentSpan(classificationData, 3);
		UIUtils.setMaxWidth(MAX_WIDTH, docs);
		docs.setWidthFull();

		return docs;
	}
    
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
 	private Component createClassifications() {
 		FlexBoxLayout header = createHeader(VaadinIcon.TAGS, "Ultime Classificazioni");
 		
 		Chart chart = new Chart();
 		chart.setSizeFull();

         Configuration configuration = chart.getConfiguration();
         chart.getConfiguration().getChart().setType(ChartType.BAR);
         

         ListSeries carenza = new ListSeries("Carenza di acqua", 15, 9, 5, 10);
         PlotOptionsSeries posCarenza = new PlotOptionsSeries();
         posCarenza.setColorIndex(8);
         carenza.setPlotOptions(posCarenza);
         configuration.addSeries(carenza);
         
         ListSeries sane = new ListSeries("Sane", 75, 81, 95, 70);
         PlotOptionsSeries posSane = new PlotOptionsSeries();
         posSane.setColorIndex(2);
         sane.setPlotOptions(posSane);
         configuration.addSeries(sane);
         
         ListSeries eccesso = new ListSeries("Eccesso di acqua", 5, 6, 0, 10);
         PlotOptionsSeries posEccesso = new PlotOptionsSeries();
         posEccesso.setColorIndex(4);
         eccesso.setPlotOptions(posEccesso);
         configuration.addSeries(eccesso);
         
         ListSeries scartate = new ListSeries("Immagini scartate", 3, 2, 0, 5);
         PlotOptionsSeries posScartate = new PlotOptionsSeries();
         posScartate.setColorIndex(1);
         scartate.setPlotOptions(posScartate);
         configuration.addSeries(scartate);
         
         ListSeries infestanti = new ListSeries("Infestanti", 2, 2, 0, 5);
         PlotOptionsSeries posInfestanti = new PlotOptionsSeries();
         posInfestanti.setColorIndex(3);
         infestanti.setPlotOptions(posInfestanti);
         configuration.addSeries(infestanti);
         
         XAxis x = new XAxis();
         x.setCrosshair(new Crosshair());
         x.setCategories("08/09/2020", "28/08/2020", "18/08/2020", "08/08/2020");
         configuration.addxAxis(x);
     

         YAxis y = new YAxis();
         y.setMin(0);
         AxisTitle yTitle = new AxisTitle();
         yTitle.setText("Percentuale [%]");
         yTitle.setAlign(VerticalAlign.HIGH);
         y.setTitle(yTitle);
         y.setMax(100);
         
         configuration.addyAxis(y);

         Tooltip tooltip = new Tooltip();
         tooltip.setShared(true);
         tooltip.setValueSuffix("%");
         configuration.setTooltip(tooltip);
         
         /*PlotOptionsSeries plot = new PlotOptionsSeries();
         plot.setStacking(Stacking.NORMAL);
         configuration.setPlotOptions(plot);*/
         
         chart.setSizeFull();
         FlexBoxLayout card = new FlexBoxLayout(chart);
         
 		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
 		card.setBorderRadius(BorderRadius.S);
 		card.setBoxSizing(BoxSizing.BORDER_BOX);
 		card.setHeight("404px");
 		card.setPadding(Uniform.M);
 		card.setShadow(Shadow.XS);
 		card.setMargin(Bottom.XL);

		FlexBoxLayout reports = new FlexBoxLayout(header, card);
		reports.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		reports.setPadding(Bottom.XL, Right.RESPONSIVE_L, Left.RESPONSIVE_L);
		return reports;
 	}
   
 	
 	

	private Component createAmbientData() {
		FlexBoxLayout header = createHeader(VaadinIcon.SUN_O, "Ambiente");
		
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
		
		FlexBoxLayout tempHumCard = new FlexBoxLayout(dateLabel, tempLabel, maxTempLabel, minTempLabel, humLabel);
		tempHumCard.setAlignItems(FlexComponent.Alignment.CENTER);
		tempHumCard.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		tempHumCard.setMinHeight("200px");
		tempHumCard.setMargin(Bottom.XS);
		tempHumCard.setMaxWidth("215px");
		UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, tempHumCard);
		UIUtils.setBorderRadius(BorderRadius.S, tempHumCard);
		UIUtils.setShadow(Shadow.XS, tempHumCard);
		
		FlexBoxLayout windCard = new FlexBoxLayout(createWindChart());
		windCard.setAlignItems(FlexComponent.Alignment.CENTER);
		windCard.setHeight("200px");
		windCard.setMaxWidth("215px");
		UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, windCard);
		UIUtils.setBorderRadius(BorderRadius.S, windCard);
		UIUtils.setShadow(Shadow.XS, windCard);

		FlexBoxLayout reports = new FlexBoxLayout(header, tempHumCard, windCard);
		reports.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		reports.setPadding(Bottom.XL, Right.RESPONSIVE_L, Left.RESPONSIVE_L);
		return reports;
	}

	private Component createWindChart() {
		Chart chart = new Chart(ChartType.COLUMN);

        Configuration conf = chart.getConfiguration();

        conf.getChart().setPolar(true);

        XAxis xAxis = conf.getxAxis();
        xAxis.setCategories("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW");

        YAxis yAxis = conf.getyAxis();
        yAxis.setEndOnTick(false);
        yAxis.setShowLastLabel(false);
        yAxis.setShowFirstLabel(false);

        conf.getPane().setSize("60%");
        conf.getLegend().setEnabled(false);

        PlotOptionsColumn plotOptionsColumn = new PlotOptionsColumn();
        plotOptionsColumn.setPointPlacement(PointPlacement.ON);
        conf.setPlotOptions(plotOptionsColumn);
        
        Tooltip tooltip = conf.getTooltip();
        tooltip.setValueSuffix("m/s");


        ListSeries series = new ListSeries("NNE", 0.0, 6.7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        series.setName("Velocità del Vento");
        ListSeries seriesS = new ListSeries("S", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 7.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        seriesS.setName("Velocità del Vento");
        conf.setSeries(series, seriesS);
        

        FlexBoxLayout card = new FlexBoxLayout(chart);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setSizeFull();
		return card;
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
    

}