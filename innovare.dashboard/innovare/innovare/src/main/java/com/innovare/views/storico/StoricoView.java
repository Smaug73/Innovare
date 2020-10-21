package com.innovare.views.storico;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Background;
import com.vaadin.flow.component.charts.model.BackgroundShape;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Pane;
import com.vaadin.flow.component.charts.model.PlotOptionsSolidgauge;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.innovare.backend.stub.Classification;
import com.innovare.ui.utils.DataSeriesItemWithRadius;
import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.ui.utils.Bottom;
import com.innovare.ui.utils.Horizontal;
import com.innovare.ui.utils.Right;
import com.innovare.ui.utils.Top;
import com.innovare.ui.utils.Uniform;
import com.innovare.ui.utils.FontSize;
import com.innovare.ui.utils.IconSize;
import com.innovare.ui.utils.LumoStyles;
import com.innovare.ui.utils.TextColor;
import com.innovare.ui.utils.UIUtils;
import com.innovare.ui.utils.BorderRadius;
import com.innovare.ui.utils.BoxSizing;
import com.innovare.ui.utils.Display;
import com.innovare.ui.utils.Position;
import com.innovare.ui.utils.Shadow;
import com.innovare.views.main.ContentView;

@Route(value = "storico", layout = ContentView.class)
@PageTitle("Storico")
@CssImport(value = "./styles/views/storico/storico-view.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class StoricoView extends Div{

	private static final String CLASS_NAME = "storico";
	public static final String MAX_WIDTH = "1024px";

	public StoricoView() {
		setId("storico-view");
		add(createContent());
	}

	private Component createContent() {
		Component classifications = createClassifications();

		FlexBoxLayout content = new FlexBoxLayout(classifications);
		content.setAlignItems(FlexComponent.Alignment.CENTER);
		content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		return content;
	}

	private Component createClassifications() {
		FlexBoxLayout classifications = new FlexBoxLayout(
				createHeader(VaadinIcon.FORWARD, "Classificazioni"),
				createClassificationsCharts());
		classifications.setBoxSizing(BoxSizing.BORDER_BOX);
		classifications.setDisplay(Display.BLOCK);
		classifications.setMargin(Top.L);
		classifications.setMaxWidth(MAX_WIDTH);
		classifications.setPadding(Horizontal.RESPONSIVE_L);
		classifications.setWidthFull();
		return classifications;
	}

	private FlexBoxLayout createHeader(VaadinIcon icon, String title) {
		FlexBoxLayout header = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, icon),
				UIUtils.createH3Label(title));
		header.setAlignItems(FlexComponent.Alignment.CENTER);
		header.setMargin(Bottom.L, Horizontal.RESPONSIVE_L);
		header.setSpacing(Right.L);
		return header;
	}

	private Component createClassificationsCharts() {
		Row charts = new Row();
		UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, charts);
		UIUtils.setBorderRadius(BorderRadius.S, charts);
		UIUtils.setShadow(Shadow.XS, charts);

		for (Classification.Status status : Classification.Status.values()) {
			charts.add(createClassificationChart(status));
		}

		return charts;
	}

	private Component createClassificationChart(Classification.Status status) {
		int value;

		switch (status) {
			case CARENZA:
				value = 24;
				break;

			case NORMALE:
				value = 70;
				break;

			default:
				value = 6;
				break;
		}

		FlexBoxLayout textContainer = new FlexBoxLayout(
				UIUtils.createH2Label(Integer.toString(value)),
				UIUtils.createLabel(FontSize.S, "%"));
		textContainer.setAlignItems(FlexComponent.Alignment.BASELINE);
		textContainer.setPosition(Position.ABSOLUTE);
		textContainer.setSpacing(Right.XS);

		Chart chart = createProgressChart(status, value);

		FlexBoxLayout chartContainer = new FlexBoxLayout(chart, textContainer);
		chartContainer.setAlignItems(FlexComponent.Alignment.CENTER);
		chartContainer
				.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		chartContainer.setPosition(Position.RELATIVE);
		chartContainer.setHeight("120px");
		chartContainer.setWidth("120px");

		FlexBoxLayout classificationChart = new FlexBoxLayout(
				new Label(status.getName()), chartContainer);
		classificationChart.addClassName(CLASS_NAME + "__payment-chart");
		classificationChart.setAlignItems(FlexComponent.Alignment.CENTER);
		classificationChart.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		classificationChart.setPadding(Bottom.S, Top.M);
		return classificationChart;
	}

	private Chart createProgressChart(Classification.Status status, int value) {
		Chart chart = new Chart();
		chart.addClassName(status.getName().toLowerCase());
		chart.setSizeFull();

		Configuration configuration = chart.getConfiguration();
		configuration.getChart().setType(ChartType.SOLIDGAUGE);
		configuration.setTitle("");
		configuration.getTooltip().setEnabled(false);

		configuration.getyAxis().setMin(0);
		configuration.getyAxis().setMax(100);
		configuration.getyAxis().getLabels().setEnabled(false);

		PlotOptionsSolidgauge opt = new PlotOptionsSolidgauge();
		opt.getDataLabels().setEnabled(false);
		configuration.setPlotOptions(opt);

		DataSeriesItemWithRadius point = new DataSeriesItemWithRadius();
		point.setY(value);
		point.setInnerRadius("100%");
		point.setRadius("110%");
		configuration.setSeries(new DataSeries(point));

		Pane pane = configuration.getPane();
		pane.setStartAngle(0);
		pane.setEndAngle(360);

		Background background = new Background();
		background.setShape(BackgroundShape.ARC);
		background.setInnerRadius("100%");
		background.setOuterRadius("110%");
		pane.setBackground(background);

		return chart;
	}
   

}
