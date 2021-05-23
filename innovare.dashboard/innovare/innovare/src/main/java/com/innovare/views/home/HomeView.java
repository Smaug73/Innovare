package com.innovare.views.home;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AxisTitle;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Crosshair;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.PointPlacement;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.innovare.ui.utils.Left;
import com.innovare.model.Classification;
import com.innovare.model.IrrigationState;
import com.innovare.model.Irrigazione;
import com.innovare.model.Sample;
import com.innovare.model.Sensor;
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
import com.innovare.utils.Channel;
import com.innovare.utils.Constants;
import com.innovare.utils.Direction;
import com.innovare.utils.HttpHandler;
import com.innovare.views.main.ContentView;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.Command;


@CssImport("./styles/views/main/main-view.css")
@Route(value = "home", layout = ContentView.class)
@PageTitle("Home")
@RouteAlias(value = "", layout = ContentView.class)
public class HomeView extends Div {
	
	private String isIrrigationOn;
	private Irrigazione lastIrrigation;
	private ArrayList<Classification> classifications;
	private ArrayList<Integer> channels;

	private long lastMeasureDate;
	private float lastMeasureTemp;
	private float lastMeasureUV;
	private float lastMeasureRain;
	private float lastMeasureHum;
	private float lastMeasureHeat;
	private float lastMeasureWindSpeed;
	private float lastMeasureWindDirection;
	private HashMap<Integer, Sensor> sensors;
	
	private float quantitaAttuale;
	private FlexBoxLayout quantitaL;
	private FlexBoxLayout cardLastIrrigation;
	private FlexBoxLayout cardIrrigationState;
	
    public HomeView() {
        setId("home-view");
        getData();
        
        add(createContent());
        
    }
    
    private void getData() {
/*
    	lastIrrigation = new Irrigazione(System.currentTimeMillis() - 54657, System.currentTimeMillis() + 40000, 58);
    	classifications = new ArrayList();
    	isIrrigationOn = "ON";
    	
		lastMeasureDate = System.currentTimeMillis();
		lastMeasureTemp = 13;
		lastMeasureUV = 15;
		lastMeasureRain = 9;
		lastMeasureHum = 32;
		lastMeasureHeat = 550;
		lastMeasureWindSpeed = (float) Channel.WIND_SPEED.getInvalidValue();
		lastMeasureWindDirection = (float) 1.0;
		
		sensors = new HashMap<Integer, Sensor>();
		ArrayList<Integer> channels = new ArrayList<Integer>();
		channels.add(16);
		channels.add(17);
		channels.add(20);
		for(Integer channel : channels) {
			Sample sample1 = new Sample("id1", System.currentTimeMillis() - 36000, "" + channel, (float)channel);
			Sensor sensor1 = new Sensor("Sensore Canale " + channel, sample1);
			sensors.put(channel, sensor1);
		}
*/	
   	
		isIrrigationOn = HttpHandler.getCurrentIrrigationState();
		if(isIrrigationOn == null) {
			isIrrigationOn = "UNKNOWN";
		}
    	lastIrrigation = HttpHandler.getLastIrrigation();
    	
		classifications = HttpHandler.getLastClassifications();
		
		lastMeasureDate = HttpHandler.getLastSample(Channel.OUTSIDE_TEMP).getTimestamp();
		lastMeasureTemp = HttpHandler.getLastSample(Channel.OUTSIDE_TEMP).getMisure();
		lastMeasureUV = HttpHandler.getLastSample(Channel.UV_LEVEL).getMisure();
		lastMeasureRain = HttpHandler.getLastSample(Channel.DAY_RAIN).getMisure();
		lastMeasureHum = HttpHandler.getLastSample(Channel.OUTSIDE_HUM).getMisure();
		lastMeasureHeat = HttpHandler.getLastSample(Channel.SOLAR_RAD).getMisure();
		lastMeasureWindSpeed = HttpHandler.getLastSample(Channel.WIND_SPEED).getMisure();
		lastMeasureWindDirection = HttpHandler.getLastSample(Channel.WIND_DIR_ROSE).getMisure();

		sensors = new HashMap<Integer, Sensor>();
		channels = HttpHandler.getActiveChannels();
		if(channels == null) {
			channels = new ArrayList<Integer>();
		}
		
		// Si filtrano i canali in modo da prendere solo quelli relativi al suolo
		Channel[] other_channels = Channel.values();
		for (Iterator<Integer> iterator = channels.iterator(); iterator.hasNext(); ) {
			Integer value = iterator.next();
						
			for(Channel ch : other_channels) {
				if (value == ch.getValue()) {
					iterator.remove();
				}
			}
		}
		
			
		
		for(Integer channel : channels) {
			Sample sample = HttpHandler.getLastSample(channel);
			Sensor sensor = new Sensor("Sensore Canale " + channel, sample);
			sensors.put(channel, sensor);
		}

	}

	private Component createContent() {

		cardLastIrrigation = new FlexBoxLayout();
		cardIrrigationState = new FlexBoxLayout();
		
		Component data = createData();
		Component irrigation = createIrrigationState();
		Component lastIrrigation = createLastIrrigation();
		Component sensors = createSensorData();

		FlexBoxLayout content = new FlexBoxLayout(irrigation, lastIrrigation, data, sensors);
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
    	lastIrr.setMaxWidth(Constants.MAX_WIDTH);
    	lastIrr.setPadding(Horizontal.RESPONSIVE_L);
    	lastIrr.setWidthFull();
  		return lastIrr;
	}

	private Component createLastIrrigationCard() {
		String fromString;
		String toString;
		
		/* Se la richiesta HTTP ha ricevuto una risposta valida, si va a recuperare e mostrare
		 * la data e l'ora di inizio e fine dell'ultima irrigazione effettuata.
		 * Se la richiesta HTTP non riceve una risposta valida, si notifica all'utente un errore di connessione
		 */
		if(lastIrrigation != null) {
			fromString = Constants.TIME_FORMAT.format(lastIrrigation.getInizioIrrig())
					+ " " + Constants.DATE_FORMAT.format(lastIrrigation.getInizioIrrig());
			toString = Constants.TIME_FORMAT.format(lastIrrigation.getFineIrrig())
					+ " " + Constants.DATE_FORMAT.format(lastIrrigation.getFineIrrig());
		}
		else {
			fromString = Constants.erroreConnessione;
			toString = Constants.erroreConnessione;
		}
		
		// Si crea la riga relativa all'inizio dell'ultima irrigazione
		FlexBoxLayout fromLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Dalle:"));
		fromLabel.setWidth("200px");
		
		FlexBoxLayout fromDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, fromString));


		FlexBoxLayout from = new FlexBoxLayout(fromLabel, fromDate);
		from.setFlexDirection(FlexLayout.FlexDirection.ROW);
		from.setFlexGrow(2, fromLabel, fromDate);

		// Si crea la riga relativa alla fine dell'ultima irrigazione
		FlexBoxLayout toLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Alle:"));
		toLabel.setWidth("200px");
		
		FlexBoxLayout toDate;
		
		/* Se l'irrigazione è spenta, viene mostrata la data e l'ora in cui si è conclusa l'ultima irrigazione.
		 * Se ci sono problemi di connessione si mostra un messaggio di errore di connessione.
		 * Se l'irrigazione è accesa, viene mostrato un messaggio che indica che l'irrigazione è ancora in corso
		 * e quali sono la data e l'ora previste di fine di tale irrigazione
		 */
		if(!isIrrigationOn.equalsIgnoreCase("ON")) {
			toDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, toString));
		}
		else {
			toDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "In corso - Fine prevista: " + toString));
		}

		FlexBoxLayout to = new FlexBoxLayout(toLabel, toDate);
		to.setFlexDirection(FlexLayout.FlexDirection.ROW);
		to.setFlexGrow(2, toLabel, toDate);

		// Si crea la riga relativa alla quantità di acqua erogata durante l'ultima irrigazione
		FlexBoxLayout quantitaLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Quantità (L):"));
		quantitaLabel.setWidth("200px");
		
		/* Se non c'è nessuna irrigazione in corso, allora bisogna mostrare i dati dell'ultima irrigazione:
		 * Data Inizio, Data Fine e Quantità di acqua erogata.
		 * Se non si riescono a recuperare i dati dell'ultima irrigazione, è necessario avvisare che c'è stato un errore di connessione.
		 * Se c'è un'irrigazione in corso, bisogna mostrare quando è iniziata, quando è previsto che finisca
		 * e la quantità correntemente erogata rispetto al totale da erogare
		 */
		if(!isIrrigationOn.equalsIgnoreCase("ON")) {
			if(lastIrrigation != null) {
				quantitaL = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "" + lastIrrigation.getQuantita()));
			}
			else {
				quantitaL = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, Constants.erroreConnessione));
			}
		}
		else {
			/* 
			 * 
			 */
			if(lastIrrigation != null) {
				long durata = (lastIrrigation.getFineIrrig()- lastIrrigation.getInizioIrrig());
				int intervallo = (int) (durata/10);
				double portata = lastIrrigation.getQuantita()/durata;
				double aggiornamentoQuantita = Math.round(portata * intervallo * 100.0) / 100.0;
				
				//System.out.println("Durata: " + durata/1000 + "s\nIntervallo: " + intervallo/1000 + "s\nPortata: " + portata*1000 + " L/s\nAggiornamento: " + aggiornamentoQuantita);
				
				quantitaL = new FlexBoxLayout();
				quantitaAttuale = (float) ((System.currentTimeMillis() - lastIrrigation.getInizioIrrig()) * portata);
				quantitaAttuale = (float) (Math.round(quantitaAttuale * 100.0) / 100.0);
				quantitaL.add(UIUtils.createLabel(FontSize.L, quantitaAttuale + "/" + lastIrrigation.getQuantita()));
				
				runWhileAttached(quantitaL, () -> {
	            	quantitaAttuale += aggiornamentoQuantita;
	            	quantitaAttuale = (float) (Math.round(quantitaAttuale * 100.0) / 100.0);
					if(quantitaAttuale < lastIrrigation.getQuantita()) {
	                	quantitaL.removeAll();
	                	
	                	// Se la quantità attualmente erogata è uguale a quella da erogare a meno dell'1%, le due quantità si considerano uguali.
	                	// In pratica si riconduce quella differenza a un errore dovuto all'approssimazione che viene fatta.
						if((lastIrrigation.getQuantita() - quantitaAttuale) < (lastIrrigation.getQuantita() / 100)) {
							quantitaL.add(UIUtils.createLabel(FontSize.L, lastIrrigation.getQuantita() + "/" + lastIrrigation.getQuantita()));
							do {
								//isIrrigationOn = "OFF";
								isIrrigationOn = HttpHandler.getCurrentIrrigationState();
								if(isIrrigationOn == null) {
									isIrrigationOn = "UNKNOWN";
								}
							} while(isIrrigationOn.equalsIgnoreCase("ON"));
							cardLastIrrigation.removeAll();
							cardLastIrrigation = (FlexBoxLayout) createLastIrrigationCard();
							cardIrrigationState.removeAll();
							cardIrrigationState = (FlexBoxLayout) createIrrigationStateCard();
						}
						else {
							quantitaL.add(UIUtils.createLabel(FontSize.L, quantitaAttuale + "/" + lastIrrigation.getQuantita()));
						}
					}
					else {
						
						quantitaL.add(UIUtils.createLabel(FontSize.L, lastIrrigation.getQuantita() + "/" + lastIrrigation.getQuantita()));
						do {
							//isIrrigationOn = "OFF";
							isIrrigationOn = HttpHandler.getCurrentIrrigationState();
							if(isIrrigationOn == null) {
								isIrrigationOn = "UNKNOWN";
							}
						} while(isIrrigationOn.equalsIgnoreCase("ON"));
						cardLastIrrigation.removeAll();
						cardLastIrrigation = (FlexBoxLayout) createLastIrrigationCard();
						cardIrrigationState.removeAll();
						cardIrrigationState = (FlexBoxLayout) createIrrigationStateCard();
					}
	                
				}, intervallo, intervallo);
			}
			else {
				quantitaL = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, Constants.erroreConnessione));
			}
		}

		FlexBoxLayout quantita = new FlexBoxLayout(quantitaLabel, quantitaL);
		to.setFlexDirection(FlexLayout.FlexDirection.ROW);
		to.setFlexGrow(2, quantitaLabel, quantitaL);

		cardLastIrrigation.add(from, to, quantita);
		cardLastIrrigation.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		cardLastIrrigation.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		cardLastIrrigation.setBorderRadius(BorderRadius.S);
		cardLastIrrigation.setBoxSizing(BoxSizing.BORDER_BOX);
		cardLastIrrigation.setPadding(Uniform.M);
		cardLastIrrigation.setShadow(Shadow.XS);
		cardLastIrrigation.setHeightFull();
		cardLastIrrigation.setMargin(Bottom.L);
		return cardLastIrrigation;
	}
	
	

	private Component createIrrigationState() {
    	FlexBoxLayout irrState = new FlexBoxLayout(
  				createHeader(VaadinIcon.SLIDER, "Stato Irrigazione"),
  				createIrrigationStateCard());
    	irrState.setBoxSizing(BoxSizing.BORDER_BOX);
  		irrState.setDisplay(Display.BLOCK);
  		irrState.setMargin(Top.L);
  		irrState.setMaxWidth(Constants.MAX_WIDTH);
  		irrState.setPadding(Horizontal.RESPONSIVE_L);
  		irrState.setWidthFull();
  		return irrState;
	}

	private Component createIrrigationStateCard() {
		String state;
		String colorState;

		if(isIrrigationOn.equals("ON")) {
			state = IrrigationState.ACCESO.getName();
			colorState = IrrigationState.ACCESO.getColor();
		}
		else if(isIrrigationOn.equals("OFF")) {
			state = IrrigationState.SPENTO.getName();
			colorState = IrrigationState.SPENTO.getColor();
		}
		else {
			state = Constants.erroreConnessione;
			colorState = TextColor.DISABLED.getValue();
		}


		FlexBoxLayout descLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Stato dell'impianto irriguo:"));
		descLabel.setWidth("200px");
		descLabel.setAlignItems(Alignment.CENTER);

		Icon icon = UIUtils.createIcon(IconSize.S, colorState, VaadinIcon.CIRCLE);
		Label label = UIUtils.createLabel(FontSize.L, state);
		FlexBoxLayout status = new FlexBoxLayout(icon, label);
		status.setSpacing(Right.S);
		status.setAlignItems(Alignment.CENTER);

		cardIrrigationState.add(descLabel, status);
		cardIrrigationState.setFlexDirection(FlexLayout.FlexDirection.ROW);
		cardIrrigationState.setFlexGrow(1, descLabel, status);
		cardIrrigationState.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		cardIrrigationState.setBorderRadius(BorderRadius.S);
		cardIrrigationState.setBoxSizing(BoxSizing.BORDER_BOX);
		cardIrrigationState.setPadding(Uniform.M);
		cardIrrigationState.setShadow(Shadow.XS);
		cardIrrigationState.setHeightFull();
		cardIrrigationState.setMargin(Bottom.L);
		return cardIrrigationState;
	}

	private Component createData() {
		Component ambientData = createAmbientData();
		Component classificationData = createClassifications();

		Row docs = new Row(ambientData, classificationData);
		docs.addClassName(LumoStyles.Margin.Top.XL);
		docs.setComponentSpan(classificationData, 3);
		UIUtils.setMaxWidth(Constants.MAX_WIDTH, docs);
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

		XAxis x = new XAxis();

		ListSeries carenza = new ListSeries(Classification.Status.CARENZA.getName());
		ListSeries sane = new ListSeries(Classification.Status.NORMALE.getName());
		ListSeries eccesso = new ListSeries(Classification.Status.ECCESSO.getName());
		ListSeries scartate = new ListSeries(Classification.Status.SCARTATE.getName());
		ListSeries infestanti = new ListSeries(Classification.Status.INFESTANTI.getName());
		
		if(classifications == null) {
			classifications = new ArrayList<Classification>();
			configuration.setSubTitle(Constants.erroreConnessione);
		}
		
		for(Classification classif : classifications) {
			carenza.addData(classif.getPercCarenza());
			sane.addData(classif.getPercSane());
			eccesso.addData(classif.getPercEccesso());
			scartate.addData(classif.getPercScartate());
			infestanti.addData(classif.getPercInfestanti());
			x.addCategory(Constants.DATE_FORMAT.format(classif.getDate()));
		}

		PlotOptionsSeries posCarenza = new PlotOptionsSeries();
		posCarenza.setColorIndex(8);
		carenza.setPlotOptions(posCarenza);
		configuration.addSeries(carenza);

		PlotOptionsSeries posSane = new PlotOptionsSeries();
		posSane.setColorIndex(2);
		sane.setPlotOptions(posSane);
		configuration.addSeries(sane);

		PlotOptionsSeries posEccesso = new PlotOptionsSeries();
		posEccesso.setColorIndex(4);
		eccesso.setPlotOptions(posEccesso);
		configuration.addSeries(eccesso);

		PlotOptionsSeries posScartate = new PlotOptionsSeries();
		posScartate.setColorIndex(1);
		scartate.setPlotOptions(posScartate);
		configuration.addSeries(scartate);

		PlotOptionsSeries posInfestanti = new PlotOptionsSeries();
		posInfestanti.setColorIndex(3);
		infestanti.setPlotOptions(posInfestanti);
		configuration.addSeries(infestanti);


		x.setCrosshair(new Crosshair());
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

		String dateString;
		if(lastMeasureDate == 0) {
			dateString = Constants.erroreDato;
		}
		else {
			dateString = Constants.DATE_FORMAT.format(lastMeasureDate) + " " + Constants.TIME_FORMAT.format(lastMeasureDate);
		}
		
		String tempString;
		if(lastMeasureTemp == Channel.OUTSIDE_TEMP.getInvalidValue()) {
			tempString = Constants.erroreDato;
		}
		else {
			tempString = "Temperatura: " + lastMeasureTemp + "°C";
		}
		
		String uvString;
		if(lastMeasureUV == Channel.UV_LEVEL.getInvalidValue()) {
			uvString = Constants.erroreDato;
		}
		else {
			uvString = "Indice UV: " + lastMeasureUV;
		}
		
		String radiationString;
		if(lastMeasureHeat == Channel.SOLAR_RAD.getInvalidValue()) {
			radiationString = Constants.erroreDato;
		}
		else {
			radiationString = "Irraggiamento: " + lastMeasureHeat + " W/m^2";
		}
		
		String rainString;
		if(lastMeasureRain == Channel.DAY_RAIN.getInvalidValue()) {
			rainString = Constants.erroreDato;
		}
		else {
			rainString = "Pioggia: " + lastMeasureRain + " mm";
		}
		
		String humString;
		if(lastMeasureHum == Channel.OUTSIDE_HUM.getInvalidValue()) {
			humString = Constants.erroreDato;
		}
		else {
			humString = "Umidità: " + lastMeasureHum + "%";
		}

		FlexBoxLayout dateLabel = new FlexBoxLayout(
				UIUtils.createLabel(FontSize.XS, dateString));
		dateLabel.setMargin(Bottom.M);

		FlexBoxLayout tempLabel = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, TextColor.TERTIARY.getValue(), VaadinIcon.CLOUD_O),
				UIUtils.createLabel(FontSize.S, tempString));
		tempLabel.setAlignItems(Alignment.CENTER);
		tempLabel.setSpacing(Right.S, Left.S);
		tempLabel.setMargin(Bottom.S);
		
		FlexBoxLayout uvLabel = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, TextColor.TERTIARY.getValue(), VaadinIcon.SUN_O),
				UIUtils.createLabel(FontSize.S, uvString));
		uvLabel.setAlignItems(Alignment.CENTER);
		uvLabel.setSpacing(Right.S, Left.S);
		uvLabel.setMargin(Bottom.S);
		
		FlexBoxLayout radiationLabel = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, TextColor.TERTIARY.getValue(), VaadinIcon.SUN_O),
				UIUtils.createLabel(FontSize.S, radiationString));
		radiationLabel.setAlignItems(Alignment.CENTER);
		radiationLabel.setSpacing(Right.S, Left.S);
		radiationLabel.setMargin(Bottom.S);
		
		FlexBoxLayout rainLabel = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, TextColor.TERTIARY.getValue(), VaadinIcon.DROP),
				UIUtils.createLabel(FontSize.S, rainString));
		rainLabel.setAlignItems(Alignment.CENTER);
		rainLabel.setSpacing(Right.S, Left.S);
		rainLabel.setMargin(Bottom.S);
		
		
		FlexBoxLayout humLabel = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, TextColor.TERTIARY.getValue() , VaadinIcon.DROP),
				UIUtils.createLabel(FontSize.S, humString));
		humLabel.setSpacing(Right.S, Left.S);
		humLabel.setMargin(Bottom.S);
		
		FlexBoxLayout ambData = new FlexBoxLayout(tempLabel, humLabel, uvLabel, radiationLabel, rainLabel);
		ambData.setAlignItems(FlexComponent.Alignment.START);
		ambData.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		
		FlexBoxLayout tempHumCard = new FlexBoxLayout(dateLabel, ambData);
		tempHumCard.setAlignItems(FlexComponent.Alignment.CENTER);
		tempHumCard.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		tempHumCard.setMinHeight("200px");
		tempHumCard.setMargin(Bottom.XS);
		//tempHumCard.setMaxWidth("250px");
		UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, tempHumCard);
		UIUtils.setBorderRadius(BorderRadius.S, tempHumCard);
		UIUtils.setShadow(Shadow.XS, tempHumCard);
		
		FlexBoxLayout windCard = new FlexBoxLayout(createWindChart());
		windCard.setAlignItems(FlexComponent.Alignment.CENTER);
		windCard.setHeight("200px");
		//windCard.setMaxWidth("250px");
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

        if((lastMeasureWindDirection != Channel.WIND_DIR.getInvalidValue()) && (lastMeasureWindSpeed != Channel.WIND_SPEED.getInvalidValue())
        		&& (Direction.getDirection((int)lastMeasureWindDirection) != null)) {
        	int direction = (int)lastMeasureWindDirection;
        
        	Float[] arrayDirectionsSpeed = new Float[16];
        	for(int i = 0; i < 16; i++) {
        		if(i == direction) {
        			arrayDirectionsSpeed[i] = lastMeasureWindSpeed;
        		}
       			else {
       				arrayDirectionsSpeed[i] = (float) 0.0;
       			}	
        	}
        
        	ListSeries series = new ListSeries(Direction.getDirection(direction).getName(), arrayDirectionsSpeed);
        	series.setName("Velocità del Vento");
        	conf.setSeries(series);
        }

        FlexBoxLayout card = new FlexBoxLayout(chart);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setSizeFull();
		return card;
	}
	
	private Component createSensorData() {
    	FlexBoxLayout sensorData = new FlexBoxLayout(
  				createHeader(VaadinIcon.ROAD, "Sensori a Terra"),
  				createSensorDataGrid());
    	sensorData .setBoxSizing(BoxSizing.BORDER_BOX);
    	sensorData .setDisplay(Display.BLOCK);
    	sensorData .setMargin(Top.L);
    	sensorData .setMaxWidth(Constants.MAX_WIDTH);
    	sensorData .setPadding(Horizontal.RESPONSIVE_L);
    	sensorData .setWidthFull();
  		return sensorData ;
	}

	private Component createSensorDataGrid() {
		Grid<Sensor> grid = new Grid<>();
		grid.setSelectionMode(SelectionMode.SINGLE);

		
		ListDataProvider<Sensor> dataProvider = DataProvider.ofCollection(sensors.values());
		
		grid.setDataProvider(dataProvider);
		grid.setSizeFull();
		grid.setHeightByRows(true);

		grid.addColumn(Sensor::getName)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setFrozen(true)
				.setHeader("Sensore");
		grid.addColumn(new ComponentRenderer<>(this::createDateLabel))
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setFrozen(true)
				.setHeader("Data")
				.setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn(new ComponentRenderer<>(this::createValueLabel))
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Valore")
				.setTextAlign(ColumnTextAlign.CENTER)
				.setSortable(true);
		
		
		
		grid.setVerticalScrollingEnabled(false);
		
		FlexBoxLayout card = new FlexBoxLayout(grid);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setBorderRadius(BorderRadius.S);
		card.setBoxSizing(BoxSizing.BORDER_BOX);
		card.setPadding(Uniform.M);
		card.setShadow(Shadow.XS);
		card.setHeightFull();
		card.setMargin(Bottom.XL);
		return card;
	}

	// Crea gli item della colonna Data 
	private Component createDateLabel(Sensor sensor) {
		String dateString = Constants.DATE_FORMAT.format(sensor.getSample().getTimestamp()) + " " + Constants.TIME_FORMAT.format(sensor.getSample().getTimestamp());
		return UIUtils.createLabel(FontSize.S, dateString);
	}

	// Crea gli item della colonna Valore 
	private Component createValueLabel(Sensor sensor) {
		return UIUtils.createLabel(FontSize.S, "" + sensor.getSample().getMisure());
	}
	
	public static void runWhileAttached(Component component, Command task,
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
	}

}