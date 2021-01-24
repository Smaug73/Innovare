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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import com.innovare.ui.utils.Left;
import com.innovare.ui.utils.ListItem;
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
import com.innovare.utils.Constants;
import com.innovare.utils.HttpHandler;
import com.innovare.views.main.ContentView;
import com.vaadin.flow.router.RouteAlias;


@CssImport("./styles/views/main/main-view.css")
@Route(value = "home", layout = ContentView.class)
@PageTitle("Home")
@RouteAlias(value = "", layout = ContentView.class)
public class HomeView extends Div {
	
	private String isIrrigationOn;
	private Irrigazione lastIrrigation;
	private ArrayList<Classification> classifications;
	private ArrayList<Sensor> sensors;
	private ArrayList<Sample> samples;

	private Date lastMeasureDate;
	private int lastMeasureTemp;
	private int lastMeasureUV;
	private int lastMeasureRain;
	private int lastMeasureHum;
	private int lastMeasureHeat;
	
    public HomeView() {
        setId("home-view");
        getData();
        
        add(createContent());
        
    }
    
    private void getData() {
    	isIrrigationOn = HttpHandler.getCurrentIrrigationState();
	//lastIrrigation = HttpHandler.getLastIrrigation();
    	lastIrrigation = new Irrigazione(new Timestamp(System.currentTimeMillis() - 64872389),
				new Timestamp(System.currentTimeMillis()), 58.34);

    	classifications = HttpHandler.getLastClassifications();
    	//classifications = new ArrayList();
    	//isIrrigationOn = "OFF";
    	
		lastMeasureDate = new Date();
		lastMeasureTemp = 13;
		lastMeasureUV = 15;
		lastMeasureRain = 9;
		lastMeasureHum = 32;
		lastMeasureHeat = 550;

    	sensors = new ArrayList<Sensor>();
    	Sensor sens1 = new Sensor("Sensor1", 20.56, 33, new Timestamp(System.currentTimeMillis()));
    	Sensor sens2 = new Sensor("Sensor2", 21.08, 37, new Timestamp(System.currentTimeMillis()));
    	Sensor sens3 = new Sensor("Sensor3", 19.88, 32, new Timestamp(System.currentTimeMillis()));
    	sensors.add(sens1);
    	sensors.add(sens2);
    	sensors.add(sens3);
	}

	private Component createContent() {
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
		
		FlexBoxLayout fromLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Dalle:"));
		fromLabel.setWidth("200px");
		FlexBoxLayout fromDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, 
				Constants.TIME_FORMAT.format(lastIrrigation.getInizioIrrig())
				+ " " + Constants.DATE_FORMAT.format(lastIrrigation.getInizioIrrig())));


		FlexBoxLayout from = new FlexBoxLayout(fromLabel, fromDate);
		from.setFlexDirection(FlexLayout.FlexDirection.ROW);
		from.setFlexGrow(2, fromLabel, fromDate);

		FlexBoxLayout toLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Alle:"));
		toLabel.setWidth("200px");

		FlexBoxLayout toDate;
		if(lastIrrigation.getFineIrrig() != null) {
			toDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, 
					Constants.TIME_FORMAT.format(lastIrrigation.getFineIrrig())
					+ " " + Constants.DATE_FORMAT.format(lastIrrigation.getInizioIrrig())));
		}
		else {
			toDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "In corso"));
		}

		FlexBoxLayout to = new FlexBoxLayout(toLabel, toDate);
		to.setFlexDirection(FlexLayout.FlexDirection.ROW);
		to.setFlexGrow(2, toLabel, toDate);

		FlexBoxLayout quantitaLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Quantità:"));
		quantitaLabel.setWidth("200px");
		FlexBoxLayout quantitaL = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "" + lastIrrigation.getQuantita()));


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
		else {
			state = IrrigationState.SPENTO.getName();
			colorState = IrrigationState.SPENTO.getColor();
		}


		FlexBoxLayout descLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Stato dell'impianto irriguo:"));
		descLabel.setWidth("200px");
		descLabel.setAlignItems(Alignment.CENTER);

		Icon icon = UIUtils.createIcon(IconSize.S, colorState, VaadinIcon.CIRCLE);
		Label label = UIUtils.createLabel(FontSize.L, state);
		FlexBoxLayout status = new FlexBoxLayout(icon, label);
		status.setSpacing(Right.S);
		status.setAlignItems(Alignment.CENTER);

		FlexBoxLayout card = new FlexBoxLayout(descLabel, status);
		card.setFlexDirection(FlexLayout.FlexDirection.ROW);
		card.setFlexGrow(1, descLabel, status);
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



		FlexBoxLayout dateLabel = new FlexBoxLayout(
				UIUtils.createLabel(FontSize.XS, Constants.DATE_FORMAT.format(lastMeasureDate) 
						+ " " + Constants.TIME_FORMAT.format(lastMeasureDate)));
		dateLabel.setMargin(Bottom.M);

		FlexBoxLayout tempLabel = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, TextColor.TERTIARY.getValue(), VaadinIcon.CLOUD_O),
				UIUtils.createLabel(FontSize.S, "Temperatura: " + lastMeasureTemp + "°C"));
		tempLabel.setAlignItems(Alignment.CENTER);
		tempLabel.setSpacing(Right.S, Left.S);
		tempLabel.setMargin(Bottom.S);
		
		FlexBoxLayout uvLabel = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, TextColor.TERTIARY.getValue(), VaadinIcon.SUN_O),
				UIUtils.createLabel(FontSize.S, "Indice UV: " + lastMeasureUV));
		uvLabel.setAlignItems(Alignment.CENTER);
		uvLabel.setSpacing(Right.S, Left.S);
		uvLabel.setMargin(Bottom.S);
		
		FlexBoxLayout radiationLabel = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, TextColor.TERTIARY.getValue(), VaadinIcon.SUN_O),
				UIUtils.createLabel(FontSize.S, "Irraggiamento: " + lastMeasureHeat + " W/m^2"));
		radiationLabel.setAlignItems(Alignment.CENTER);
		radiationLabel.setSpacing(Right.S, Left.S);
		radiationLabel.setMargin(Bottom.S);
		
		FlexBoxLayout rainLabel = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, TextColor.TERTIARY.getValue(), VaadinIcon.DROP),
				UIUtils.createLabel(FontSize.S, "Pioggia: " + lastMeasureRain + " mm"));
		rainLabel.setAlignItems(Alignment.CENTER);
		rainLabel.setSpacing(Right.S, Left.S);
		rainLabel.setMargin(Bottom.S);
		
		
		FlexBoxLayout humLabel = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, TextColor.TERTIARY.getValue() , VaadinIcon.DROP),
				UIUtils.createLabel(FontSize.S, "Umidità: " + lastMeasureHum + "%"));
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

		
		ListDataProvider<Sensor> dataProvider = DataProvider.ofCollection(sensors);
		
		grid.setDataProvider(dataProvider);
		grid.setSizeFull();
		grid.setHeightByRows(true);

		grid.addColumn(Sensor::getName)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setFrozen(true);
		grid.addColumn(new ComponentRenderer<>(this::createDateLabel))
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setFrozen(true)
				.setHeader("Data")
				.setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn(Sensor::getTemperature)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Temperatura [°C]")
				.setTextAlign(ColumnTextAlign.CENTER)
				.setSortable(true);
		grid.addColumn(Sensor::getHumidity)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Umidità [%]")
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
			String dateString = Constants.DATE_FORMAT.format(sensor.getDate()) + " " + Constants.TIME_FORMAT.format(sensor.getDate());
			return UIUtils.createLabel(FontSize.S, dateString);
		}

}