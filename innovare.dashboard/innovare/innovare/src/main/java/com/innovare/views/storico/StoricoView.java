package com.innovare.views.storico;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AxisTitle;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Crosshair;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsAreasplinerange;
import com.vaadin.flow.component.charts.model.PlotOptionsBar;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.VerticalAlign;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.ui.utils.FontSize;
import com.innovare.ui.utils.Uniform;
import com.innovare.utils.Sample;
import com.innovare.backend.stub.DummyData;
import com.innovare.backend.stub.StockPrices;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.backend.stub.Classification;
import com.innovare.backend.stub.Classification.Status;
import com.innovare.ui.utils.BorderRadius;
import com.innovare.ui.utils.Shadow;
import com.innovare.ui.utils.Bottom;
import com.innovare.ui.utils.Horizontal;
import com.innovare.ui.utils.Right;
import com.innovare.ui.utils.Top;
import com.innovare.ui.utils.IconSize;
import com.innovare.ui.utils.LumoStyles;
import com.innovare.ui.utils.TextColor;
import com.innovare.ui.utils.UIUtils;
import com.innovare.ui.utils.BoxSizing;
import com.innovare.ui.utils.Display;
import com.innovare.views.main.ContentView;

@Route(value = "storico", layout = ContentView.class)
@PageTitle("Storico")
@CssImport("./styles/views/main/main-view.css")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class StoricoView extends Div{

	private static final String CLASS_NAME = "storico";
	public static final String MAX_WIDTH = "1024px";
	private Grid<Classification> grid;
	private ListDataProvider<Classification> dataProvider;
	private ArrayList<Classification> CLASSIFICATIONS = new ArrayList<>();
	private ArrayList<Sample> samples;

	public StoricoView() {
		setId("storico-view");
		
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("localhost:8888").setPath("/allsample/1");
	        
	    HttpClient client = HttpClient.newHttpClient();
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
			}

			
		
		
		CLASSIFICATIONS.add(new Classification(Classification.Status.CARENZA, DummyData.getDate(), 73, 19, 8));
		CLASSIFICATIONS.add(new Classification(Classification.Status.ECCESSO, DummyData.getDate(), 6, 21, 73));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, DummyData.getDate(), 5, 75, 20));
		CLASSIFICATIONS.add(new Classification(Classification.Status.CARENZA, DummyData.getDate(), 58, 29, 13));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, DummyData.getDate(), 2, 88, 10));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, DummyData.getDate(), 8, 90, 2));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, DummyData.getDate(), 1, 93, 6));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, DummyData.getDate(), 4, 84, 12));
		CLASSIFICATIONS.add(new Classification(Classification.Status.NORMALE, DummyData.getDate(), 0, 100, 0));
		
		add(createContent());
	}

	// Aggiunge tutte le chart alla view
	private Component createContent() {
		Component classifications = createClassifications();
		Component temperatures = createTemperatures();
		Component gridClassifications = createGridClassifications();

		FlexBoxLayout content = new FlexBoxLayout(classifications,gridClassifications, temperatures);
		content.setAlignItems(FlexComponent.Alignment.CENTER);
		content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		content.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		return content;
	}
	
	// Crea l'header, composto da icona e titolo, di ogni chart
	private FlexBoxLayout createHeader(VaadinIcon icon, String title) {
		FlexBoxLayout header = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, icon),
				UIUtils.createH3Label(title));
		header.setAlignItems(FlexComponent.Alignment.CENTER);
		header.setMargin(Bottom.L, Horizontal.RESPONSIVE_L);
		header.setSpacing(Right.L);
		return header;
	}

	// Crea il layout in cui aggiungere la chart della temperatura
	private Component createTemperatures() {
		FlexBoxLayout temperature = new FlexBoxLayout(
				createHeader(VaadinIcon.SUN_O, "Variazioni Temperatura Ambientale"),
				createTemperaturesVariationChart());
		temperature.setBoxSizing(BoxSizing.BORDER_BOX);
		temperature.setDisplay(Display.BLOCK);
		temperature.setMargin(Top.L);
		temperature.setMaxWidth(MAX_WIDTH);
		temperature.setPadding(Horizontal.RESPONSIVE_L);
		temperature.setWidthFull();
		return temperature;
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
	
	// Crea il layout in cui aggiungere la tabella di tutte le classificazioni
	private Component createGridClassifications() {
		FlexBoxLayout allClassifications = new FlexBoxLayout(
				createHeader(VaadinIcon.FORWARD, "Tutte le Classificazioni"),
				createGrid());
		allClassifications.setBoxSizing(BoxSizing.BORDER_BOX);
		allClassifications.setDisplay(Display.BLOCK);
		allClassifications.setMargin(Top.L);
		allClassifications.setMaxWidth(MAX_WIDTH);
		allClassifications.setPadding(Horizontal.RESPONSIVE_L);
		allClassifications.setWidthFull();
		return allClassifications;
	}


	// Crea la chart delle ultime 4 classificazioni
	private Component createClassificationsChart() {
		Chart chart = new Chart();

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
        
        ListSeries eccesso = new ListSeries("Eccesso di acqua", 8, 10, 0, 15);
        PlotOptionsSeries posEccesso = new PlotOptionsSeries();
        posEccesso.setColorIndex(4);
        eccesso.setPlotOptions(posEccesso);
        configuration.addSeries(eccesso);
        
        PlotOptionsBar plotOptions = new PlotOptionsBar();
        DataLabels dataLabels = new DataLabels();
        dataLabels.setEnabled(true);
        plotOptions.setDataLabels(dataLabels);
        configuration.setPlotOptions(plotOptions);
        
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
	
	// Crea la tabella con tutte le classificazioni
	private Component createGrid() {
		grid = new Grid<>();
		grid.setSelectionMode(SelectionMode.SINGLE);

		
		dataProvider = DataProvider.ofCollection(CLASSIFICATIONS);
		
		grid.setDataProvider(dataProvider);
		grid.setSizeFull();

		grid.addColumn(Classification::getDate)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setFrozen(true)
				.setHeader("Data Volo")
				.setSortable(true);
		grid.addColumn(Classification::getPercCarenza)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Carenza di acqua [%]")
				.setTextAlign(ColumnTextAlign.CENTER)
				.setSortable(true);
		grid.addColumn(Classification::getPercSane)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Sane [%]")
				.setTextAlign(ColumnTextAlign.CENTER)
				.setSortable(true);
		grid.addColumn(Classification::getPercEccesso)
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Eccesso di acqua [%]")
				.setTextAlign(ColumnTextAlign.CENTER)
				.setSortable(true);
		grid.addColumn(new ComponentRenderer<>(this::createStatusLabel))
				.setAutoWidth(true)
				.setFlexGrow(1)
				.setHeader("Stato")
				.setTextAlign(ColumnTextAlign.CENTER);
		
		
		grid.setVerticalScrollingEnabled(true);
		FlexBoxLayout card = new FlexBoxLayout(grid);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setBorderRadius(BorderRadius.S);
		card.setBoxSizing(BoxSizing.BORDER_BOX);
		card.setPadding(Uniform.M);
		card.setShadow(Shadow.XS);
		card.setHeight("400px");
		card.setMargin(Bottom.XL);
		return card;
	}

	
	private Component createStatusLabel(Classification classif) {
		TextColor textColor;
		if(classif.getStatus().equals(Status.CARENZA.getName())) textColor = TextColor.ERROR;
		else if(classif.getStatus().equals(Status.ECCESSO.getName())) textColor = TextColor.PRIMARY;
		else textColor = TextColor.SUCCESS;
		
		FlexBoxLayout status = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, textColor, VaadinIcon.CIRCLE),
				UIUtils.createLabel(FontSize.S, classif.getStatus()));
		status.setSpacing(Right.L);
		return status;
	}
	
	// Crea la chart della variazione della temperatura ambientale
	public Component createTemperaturesVariationChart() {
        final Chart chart = new Chart(ChartType.AREASPLINERANGE);

        Configuration configuration = chart.getConfiguration();
        

        Tooltip tooltip = configuration.getTooltip();
        tooltip.setValueSuffix("°C");
        
        
        

        DataSeries dataSeries = new DataSeries("Variazione Temperatura");
        if(samples == null) samples = new ArrayList<Sample>();
        for (Sample sample : samples) {
            dataSeries.add(new DataSeriesItem(sample.getTimestamp(), sample.getMisure(), sample.getMisure()));
        }
        configuration.setSeries(dataSeries);
        
        chart.setTimeline(true);
        
        PlotOptionsAreasplinerange opt = new PlotOptionsAreasplinerange();
        configuration.setPlotOptions(opt);
       
        FlexBoxLayout card = new FlexBoxLayout(chart);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setBorderRadius(BorderRadius.S);
		card.setBoxSizing(BoxSizing.BORDER_BOX);
		card.setHeight("500px");
		card.setPadding(Uniform.M);
		card.setShadow(Shadow.XS);
		card.setMargin(Bottom.XL);
		return card;
    }
	

}
