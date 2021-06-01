package com.innovare.views.configurazione;

import com.innovare.model.IrrigationState;
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
import com.innovare.utils.Campo;
import com.innovare.utils.Constants;
import com.innovare.utils.HttpHandler;
import com.innovare.views.main.ContentView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "irrigTradizionale", layout = ContentView.class)
@PageTitle("Irrigazione del Campo Tradizionale")
@CssImport("./styles/views/innovare/innovare-view.css")
public class IrrigazioneCampoManualeView extends Div {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String isIrrigationOn;
	
	private FlexBoxLayout cardIrrigationState;
	
	private String state;
	private String colorState;
	
	public IrrigazioneCampoManualeView() {
        setId("campo-tradizionale-view");
        getData();
        
        add(createContent());
        
    }
	
	private void getData() {
/*
		isIrrigationOn = "ON";
*/	
		   	
		isIrrigationOn = HttpHandler.getCurrentIrrigationState(Campo.MANUAL);
		if(isIrrigationOn == null) {
			isIrrigationOn = "UNKNOWN";
		}
	}
	
	private Component createContent() {
		cardIrrigationState = new FlexBoxLayout();
		Component irrigationView = createIrrigationState();

		FlexBoxLayout content = new FlexBoxLayout(irrigationView);
		content.setAlignItems(FlexComponent.Alignment.CENTER);
		content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		return content;
		
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
		
		//RadioButtonGroup<String> on_off = new RadioButtonGroup<>();
		//on_off.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
		//on_off.setItems("ON", "OFF");
		
		Button on_off = new Button();
		

		if(isIrrigationOn.equals("ON")) {
			state = IrrigationState.ACCESO.getName();
			colorState = IrrigationState.ACCESO.getColor();
			on_off.setText("STOP");
		}
		else if(isIrrigationOn.equals("OFF")){
			state = IrrigationState.SPENTO.getName();
			colorState = IrrigationState.SPENTO.getColor();
			on_off.setText("START");
		}
		else {
			state = Constants.erroreConnessione;
			colorState = TextColor.DISABLED.getValue();
			on_off.setEnabled(false);
		}
		
		FlexBoxLayout descLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Stato dell'impianto irriguo:"));
		descLabel.setWidth("200px");
		descLabel.setAlignItems(Alignment.CENTER);

		Icon icon = UIUtils.createIcon(IconSize.S, colorState, VaadinIcon.CIRCLE);
		Label label = UIUtils.createLabel(FontSize.L, state);
		FlexBoxLayout status = new FlexBoxLayout(icon, label);
		status.setSpacing(Right.S);
		status.setAlignItems(Alignment.CENTER);
		
		
		on_off.addClickListener(event -> {
			int statusCode = 401;
			
			
			isIrrigationOn = HttpHandler.getCurrentIrrigationState(Campo.AUTO);
			//isIrrigationOn = "ON";
			if(isIrrigationOn != null) {
				if(on_off.getText().equalsIgnoreCase("START")) {
					if(!isIrrigationOn.equalsIgnoreCase("ON")) {
						statusCode = HttpHandler.startIrrigation(Campo.MANUAL);
						//on_off.setText("STOP");
						
						//newIrrigation = new Irrigazione(System.currentTimeMillis(), System.currentTimeMillis() ,Float.parseFloat("58.34") );	
						
					}
				}
				else {
					if(!isIrrigationOn.equalsIgnoreCase("OFF")) {
						statusCode = HttpHandler.stopIrrigation(Campo.MANUAL);
						//on_off.setText("START");
						
						//newIrrigation = new Irrigazione(System.currentTimeMillis(), System.currentTimeMillis() ,Float.parseFloat("58.34") );	
					}
				}
				
				if(statusCode == 200) {
					
					if(on_off.getText().equalsIgnoreCase("START")) {
						isIrrigationOn="ON";
						on_off.setText("STOP");
						state = IrrigationState.ACCESO.getName();
						colorState = IrrigationState.ACCESO.getColor();
					}	
					else {
						isIrrigationOn="OFF";
						on_off.setText("START");
						state = IrrigationState.SPENTO.getName();
						colorState = IrrigationState.SPENTO.getColor();
					}
					
					/*
					if(isIrrigationOn.equals("ON")) {
						state = IrrigationState.ACCESO.getName();
						colorState = IrrigationState.ACCESO.getColor();
					}
					else if(isIrrigationOn.equals("OFF")){
						state = IrrigationState.SPENTO.getName();
						colorState = IrrigationState.SPENTO.getColor();
					}
					else {
						state = Constants.erroreConnessione;
						colorState = TextColor.DISABLED.getValue();
					}*/
				}else {
					state = Constants.erroreConnessione;
					colorState = TextColor.DISABLED.getValue();
				}
				
				
				cardIrrigationState.removeAll();
				cardIrrigationState= (FlexBoxLayout) createIrrigationStateCard();
				
			}
			else {
				isIrrigationOn = "UNKNOWN";
				on_off.setEnabled(false);
			}
			
		});

	
		
		
		cardIrrigationState.add(descLabel, status , on_off);
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

	private FlexBoxLayout createHeader(VaadinIcon icon, String title) {
		FlexBoxLayout header = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.M, TextColor.TERTIARY.getValue(), icon),
				UIUtils.createH3Label(title));
		header.setAlignItems(FlexComponent.Alignment.CENTER);
		header.setMargin(Horizontal.RESPONSIVE_L);
		header.setSpacing(Right.L);
		header.setMargin(Bottom.L);
		return header;
	}
}
