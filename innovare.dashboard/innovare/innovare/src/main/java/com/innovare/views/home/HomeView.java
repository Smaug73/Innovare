package com.innovare.views.home;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.ListSeries;
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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.innovare.ui.utils.BorderRadius;
import com.innovare.ui.utils.Bottom;
import com.innovare.ui.utils.BoxSizing;
import com.innovare.ui.utils.Display;
import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.ui.utils.Horizontal;
import com.innovare.ui.utils.IconSize;
import com.innovare.ui.utils.LumoStyles;
import com.innovare.ui.utils.Right;
import com.innovare.ui.utils.Shadow;
import com.innovare.ui.utils.TextColor;
import com.innovare.ui.utils.Top;
import com.innovare.ui.utils.UIUtils;
import com.innovare.ui.utils.Uniform;
import com.innovare.views.main.ContentView;
import com.vaadin.flow.router.RouteAlias;


@CssImport("./styles/views/main/main-view.css")
@Route(value = "home", layout = ContentView.class)
@PageTitle("Home")
@RouteAlias(value = "", layout = ContentView.class)
public class HomeView extends Div {
	
	public static final String MAX_WIDTH = "1024px";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    public HomeView() {
        setId("home-view");
        add(createContent());
    }
    
    private Component createContent() {
		Component temperature = createTemperature();

		FlexBoxLayout content = new FlexBoxLayout(temperature);
		content.setAlignItems(FlexComponent.Alignment.CENTER);
		content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		return content;
	}
    
    private Component createTemperature() {
		FlexBoxLayout temperatures = new FlexBoxLayout(
				createHeader(VaadinIcon.SUN_O, "Temperatura"),
				createAreaChart());
		temperatures.setBoxSizing(BoxSizing.BORDER_BOX);
		temperatures.setDisplay(Display.BLOCK);
		temperatures.setMargin(Top.XL);
		temperatures.setMaxWidth(MAX_WIDTH);
		temperatures.setPadding(Horizontal.RESPONSIVE_L);
		temperatures.setWidthFull();
		return temperatures;
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
    
    private Component createAreaChart() {
		Chart chart = new Chart(ChartType.AREASPLINE);

		Configuration conf = chart.getConfiguration();
		conf.setTitle(DATE_FORMAT.format(new Date()));
		conf.getLegend().setEnabled(false);

		XAxis xAxis = new XAxis();
		
		xAxis.setCategories("00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00",
				"07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00",
				"15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00");
		conf.addxAxis(xAxis);

		conf.getyAxis().setTitle("Temperatura [°C]");

		conf.addSeries(new ListSeries(15, 15, 13, 12, 11, 12, 14, 15,
				15, 19, 20, 21, 25, 26, 26, 26, 27, 25, 23, 23, 22, 20, 19, 19));

		FlexBoxLayout card = new FlexBoxLayout(chart);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setBorderRadius(BorderRadius.S);
		card.setBoxSizing(BoxSizing.BORDER_BOX);
		card.setHeight("400px");
		card.setPadding(Uniform.M);
		card.setShadow(Shadow.XS);
		return card;
	}

}
