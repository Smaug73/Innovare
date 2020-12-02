package com.innovare.views.storico;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.innovare.ui.utils.BorderRadius;
import com.innovare.ui.utils.Bottom;
import com.innovare.ui.utils.BoxSizing;
import com.innovare.ui.utils.Display;
import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.ui.utils.Horizontal;
import com.innovare.ui.utils.LumoStyles;
import com.innovare.ui.utils.Shadow;
import com.innovare.ui.utils.Top;
import com.innovare.ui.utils.Uniform;
import com.vaadin.addon.charts.ChartOptions;
import com.vaadin.addon.charts.model.Lang;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

public abstract class StoricoView extends Div{
	
	public static final String MAX_WIDTH = (Toolkit.getDefaultToolkit().getScreenSize().width - 100) + "px";
	
	public StoricoView() {
		
		getData();
		add(createContent());
	}

	// Aggiunge tutte le chart alla view
	private Component createContent() {
		Component comp = createComponent();

		FlexBoxLayout content = new FlexBoxLayout(comp);
		content.setAlignItems(FlexComponent.Alignment.CENTER);
		content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		content.setMinHeight(screenSize.height - 200 + "px");
		return content;
	}
	
	// Crea il componente in cui inserire il grafico o la tabella
	private Component createComponent() {
		FlexBoxLayout chart = new FlexBoxLayout(createChart());
		chart.setBoxSizing(BoxSizing.BORDER_BOX);
		chart.setDisplay(Display.BLOCK);
		chart.setMargin(Top.L);
		chart.setMaxWidth(MAX_WIDTH);
		chart.setPadding(Horizontal.RESPONSIVE_L);
		chart.setWidthFull();
		return chart;
	}
	
	
	// Crea la card, ovvero il componente avente bordi arrotondati e ombreggiature
	// in cui viene inserito il grafico o la tabella
	public Component createCard(Component comp) {
		FlexBoxLayout card = new FlexBoxLayout(comp);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setBorderRadius(BorderRadius.S);
		card.setBoxSizing(BoxSizing.BORDER_BOX);
		card.setPadding(Uniform.M);
		card.setShadow(Shadow.XS);
		card.setHeightFull();
		card.setMargin(Bottom.XL);
		return card;
	}

	protected abstract void getData();
	protected abstract Component createChart();
}
