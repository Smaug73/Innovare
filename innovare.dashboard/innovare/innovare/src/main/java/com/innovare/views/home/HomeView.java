package com.innovare.views.home;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.AxisTitle;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.DateTimeLabelFormats;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsSpline;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import com.vaadin.flow.server.Command;


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
				createHeader(VaadinIcon.SUN_O, "Temperatura Ambientale"),
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
    	final Random random = new Random();

        final Chart chart = new Chart();

        final Configuration configuration = chart.getConfiguration();
        configuration.getChart().setType(ChartType.SPLINE);
        configuration.getTitle().setText(DATE_FORMAT.format(new Date()));
        
        

        XAxis xAxis = configuration.getxAxis();
        xAxis.setType(AxisType.DATETIME);
        xAxis.setTickPixelInterval(150);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setTitle(new AxisTitle("Temperatura [°C]"));
        yAxis.setMin(0);

        configuration.getTooltip().setEnabled(false);
        configuration.getLegend().setEnabled(false);

        final DataSeries series = new DataSeries();
        series.setPlotOptions(new PlotOptionsSpline());
        series.setName("Temperatura");
        
        
        for (int i = -24; i <= 0; i++) {
            series.add(new DataSeriesItem(System.currentTimeMillis() + i * 3600000, 15 + random.nextInt(15)));
        }

        configuration.setSeries(series);
        
        configuration.getTooltip().setEnabled(true);
        configuration.getTooltip().setValueSuffix("°C");
        configuration.getTooltip().setXDateFormat("%d/%m/%Y %H:%M");;
       

        runWhileAttached(chart, () -> {
                final long x = System.currentTimeMillis();
                final double y = 15 + random.nextInt(15);
                series.add(new DataSeriesItem(x, y), true, true);
        }, 3600000, 1000);

		FlexBoxLayout card = new FlexBoxLayout(chart);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setBorderRadius(BorderRadius.S);
		card.setBoxSizing(BoxSizing.BORDER_BOX);
		card.setHeight("400px");
		card.setPadding(Uniform.M);
		card.setShadow(Shadow.XS);
		return card;
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
