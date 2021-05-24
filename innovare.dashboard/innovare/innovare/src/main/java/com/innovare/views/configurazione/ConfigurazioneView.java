package com.innovare.views.configurazione;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.innovare.model.ConfigurationItem;
import com.innovare.model.IrrigationState;
import com.innovare.model.Irrigazione;
import com.innovare.model.Model;
import com.innovare.model.Property;
import com.innovare.ui.utils.BorderRadius;
import com.innovare.ui.utils.Bottom;
import com.innovare.ui.utils.BoxSizing;
import com.innovare.ui.utils.Display;
import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.ui.utils.FontSize;
import com.innovare.ui.utils.Horizontal;
import com.innovare.ui.utils.IconSize;
import com.innovare.ui.utils.Left;
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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;

import org.json.simple.parser.ParseException;

@Route(value = "config", layout = ContentView.class)
@PageTitle("Configurazione")
@CssImport("./styles/views/innovare/innovare-view.css")
public class ConfigurazioneView extends Div {

	private ArrayList<ConfigurationItem> configurationItems;
	private ArrayList<Model> models;
	private Model selectedModel;
	private String isIrrigationOn;
	private Irrigazione lastIrrigation;
	private UploadI18N i18n;
	private UploadBuffer buffer;
	private Collection<String> modelsNames;
	private ComboBox<String> modelSelection;
	private LocalTime irrigationTime;
	
	private float quantitaAttuale;
	private FlexBoxLayout quantitaL;

	private FlexBoxLayout cardLastIrrigation;
	private FlexBoxLayout cardIrrigationState;
	
	private String state;
	private String colorState;

	public ConfigurazioneView() throws IOException, InterruptedException, URISyntaxException, ParseException {
		setId("config-view");

		getData();

		buffer = new UploadBuffer();

		// UploadI18N permette di personalizzare i messaggi visibili nel componente di upload
		i18n = new UploadI18N();
		i18n.setDropFiles(
				new UploadI18N.DropFiles().setOne("Trascina il file qui...")
				.setMany("Trascina il file qui..."))
				.setAddFiles(new UploadI18N.AddFiles()
				.setOne("Scegli il file").setMany("Aggiungi file"))
				.setCancel("Annulla")
				.setError(new UploadI18N.Error()
				.setTooManyFiles("Troppi file")
				.setFileIsTooBig("File troppo grande")
				.setIncorrectFileType("Tipo del file sbagliato"))
				.setUploading(new UploadI18N.Uploading()
				.setStatus(new UploadI18N.Uploading.Status()
				.setConnecting("Connessione...")
				.setStalled("Caricamento annullato")
				.setProcessing("Esecuzione..."))
				.setRemainingTime(new UploadI18N.Uploading.RemainingTime()
						.setPrefix("tempo rimanente: ")
						.setUnknown("tempo rimanente non disponibile"))
				.setError(new UploadI18N.Uploading.Error()
				.setServerUnavailable("Impossibile collegarsi al server")
				.setUnexpectedServerError("Errore inaspettato del server")
				.setForbidden("Caricamento vietato")))
				.setUnits(Stream.of("B", "KByte", "MByte", "GByte", "TByte", "PByte",
						"EByte", "ZByte", "YByte").collect(Collectors.toList()));

		add(createContent());

	}

	// Recupera i dati da mostrare: i configuration item, i modelli disponibili per la classificazione, 
	// lo stato dell'irrigazione e le info sull'ultima irrigazione (quella attuale se l'irrigazione è in corso)
	private void getData() {
/*
 		configurationItems = new ArrayList<ConfigurationItem>();
    	ConfigurationItem item = new ConfigurationItem();
    	item.setId("Gateway");
    	Property prop1 = new Property();
    	prop1.setId("Indirizzo IP");
    	prop1.setValue("79.10.71.133");
    	Property prop2 = new Property();
    	prop2.setId("Porta");
    	prop2.setValue("8080");
    	Property[] properties = new Property[2];
    	properties[0] = prop1;
    	properties[1] = prop2;
    	item.setProperties(properties);
    	ConfigurationItem item2 = new ConfigurationItem();
    	item2.setId("conf item");
    	item2.setProperties(properties);
    	configurationItems.add(item);
    	configurationItems.add(item2);
    	lastIrrigation = new Irrigazione();
    	isIrrigationOn = "OFF";
    	models = new ArrayList<Model>();
    	Model model = new Model("modello 1");
    	models.add(model);

		irrigationTime = LocalTime.now();
*/
		
		configurationItems = HttpHandler.getAllConfigurationItems();
		isIrrigationOn = HttpHandler.getCurrentIrrigationState();
		if(isIrrigationOn == null) {
			isIrrigationOn = "UNKNOWN";
		}
		lastIrrigation = HttpHandler.getLastIrrigation();
		selectedModel = HttpHandler.getSelectedModel();
		models = HttpHandler.getAllModels();
		lastIrrigation = HttpHandler.getLastIrrigation();
		irrigationTime = HttpHandler.getIrrigTime();


	}

	// Crea tutte viste che devono essere mostrate in ConfigurationView:
	// stato dell'irrigazione (con possibilità di modificarlo),
	// informazioni sull'ultima irrigazione (quella attuale se è ancora in corso),
	// la lista di tutti i configuration item con le relative informazioni,
	// il componente di upload dello zip con le immagini da classificare e la scelta del modello da usare per la classificazione,
	// il componente di upload di un nuovo modello di classificazione
	private Component createContent() {
		cardLastIrrigation = new FlexBoxLayout();
		cardIrrigationState = new FlexBoxLayout();
		
		Component irrigationView = createIrrigationState();
		Component lastIrrigation = createLastIrrigation();
		Component settingStartIrrigationTime = createSettingStartIrrTime();

		// Per ogni configuration item viene creata una card che ne mostra le proprietà
		FlexBoxLayout confItemsCards = new FlexBoxLayout();
		confItemsCards.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		confItemsCards.setSizeFull();
		UIUtils.setMaxWidth(Constants.MAX_WIDTH, confItemsCards);
		if(configurationItems != null) {
			for(ConfigurationItem item : configurationItems) {
				confItemsCards.add(createConfItemView(item));
			}
		}

		// Si crea una card per ogni upload possibile:
		//zip con immagini da classificare e zip con nuovo modello di classificazione
		Component uploads = createUploadsView();

		FlexBoxLayout content = new FlexBoxLayout(irrigationView, lastIrrigation, settingStartIrrigationTime, confItemsCards, uploads);
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
		
		FlexBoxLayout fromLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Dalle:"));
		fromLabel.setWidth("200px");
		FlexBoxLayout fromDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, fromString));


		FlexBoxLayout from = new FlexBoxLayout(fromLabel, fromDate);
		from.setFlexDirection(FlexLayout.FlexDirection.ROW);
		from.setFlexGrow(2, fromLabel, fromDate);

		FlexBoxLayout toLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Alle:"));
		toLabel.setWidth("200px");

		FlexBoxLayout toDate;
		if(!isIrrigationOn.equalsIgnoreCase("ON")) {
			toDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, toString));
		}
		else {
			toDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "In corso - Fine prevista: " + toString));
		}


		FlexBoxLayout to = new FlexBoxLayout(toLabel, toDate);
		to.setFlexDirection(FlexLayout.FlexDirection.ROW);
		to.setFlexGrow(2, toLabel, toDate);

		FlexBoxLayout quantitaLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Quantità (L):"));
		quantitaLabel.setWidth("200px");
		
		if(!isIrrigationOn.equalsIgnoreCase("ON")) {
			if(lastIrrigation != null) {
				quantitaL = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "" + lastIrrigation.getQuantita()));
			}
			else {
				quantitaL = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, Constants.erroreConnessione));
			}
		}
		else {
			
			long durata = (lastIrrigation.getFineIrrig() - lastIrrigation.getInizioIrrig());
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
						} while(!isIrrigationOn.equalsIgnoreCase("OFF"));
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
					} while(!isIrrigationOn.equalsIgnoreCase("OFF"));
					cardLastIrrigation.removeAll();
					cardLastIrrigation = (FlexBoxLayout) createLastIrrigationCard();
					cardIrrigationState.removeAll();
					cardIrrigationState = (FlexBoxLayout) createIrrigationStateCard();
				}
                
			}, intervallo, intervallo);
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
			Irrigazione newIrrigation = null;
			
			
			isIrrigationOn = HttpHandler.getCurrentIrrigationState();
			//isIrrigationOn = "ON";
			if(isIrrigationOn != null) {
				if(on_off.getText().equalsIgnoreCase("START")) {
					if(!isIrrigationOn.equalsIgnoreCase("ON")) {
						newIrrigation = HttpHandler.startIrrigation();
						//on_off.setText("STOP");
						
						//newIrrigation = new Irrigazione(System.currentTimeMillis(), System.currentTimeMillis() ,Float.parseFloat("58.34") );	
						
					}
				}
				else {
					if(!isIrrigationOn.equalsIgnoreCase("OFF")) {
						newIrrigation = HttpHandler.stopIrrigation();
						//on_off.setText("START");
						
						//newIrrigation = new Irrigazione(System.currentTimeMillis(), System.currentTimeMillis() ,Float.parseFloat("58.34") );	
					}
				}
				
				if(newIrrigation != null) {
					lastIrrigation = newIrrigation;
					
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
				
				
				cardLastIrrigation.removeAll();
				cardLastIrrigation = (FlexBoxLayout) createLastIrrigationCard();
				
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
	
	private Component createSettingStartIrrTime() {
		FlexBoxLayout setIrr = new FlexBoxLayout(
				createHeader(VaadinIcon.STOPWATCH, "Orario Inizio Irrigazione"),
				createSettingStartIrrTimeCard());
		setIrr.setBoxSizing(BoxSizing.BORDER_BOX);
		setIrr.setDisplay(Display.BLOCK);
		setIrr.setMargin(Top.L);
		setIrr.setMaxWidth(Constants.MAX_WIDTH);
		setIrr.setPadding(Horizontal.RESPONSIVE_L);
		setIrr.setWidthFull();
		return setIrr;
	}

	private Component createSettingStartIrrTimeCard() {
		String labelScelta = "Ogni giorno, l'irrigazione inizia alle ore: ";
		Label label = UIUtils.createLabel(FontSize.L, labelScelta);

		TimePicker timePicker = new TimePicker();
		timePicker.setValue(irrigationTime);
		timePicker.setStep(Duration.ofMinutes(30));
		
		timePicker.addValueChangeListener(new ValueChangeListener() {
			boolean isCallNeeded = true;
			@Override
			public void valueChanged(ValueChangeEvent event) {
				if(isCallNeeded) {
					//if(HttpHandler.setIrrigTime(((LocalTime) event.getValue()).getHour() + ":" + ((LocalTime) event.getValue()).getMinute() ) != 200) {
					if( HttpHandler.setIrrigTime( LocalTime.of( ((LocalTime)event.getValue()).getHour(), ((LocalTime) event.getValue()).getMinute(), 0).toString() )  != 200  ) {
						irrigationTime = (LocalTime) event.getOldValue();
						isCallNeeded = false;
						timePicker.setValue(irrigationTime);
					}
				}
				else {
					isCallNeeded = true;
				}
			}
			
		});
/*		timePicker.addValueChangeListener(event -> {
			// Deve essere fatta una chiamata http per settare il nuovo orario
			if(HttpHandler.setIrrigTime(event.getValue().getHour() + ":" + event.getValue().getMinute()) != 200) {
				irrigationTime = event.getOldValue();
				//timePicker.setValue(irrigationTime);
			}
		});
		
*/		
		FlexBoxLayout card = new FlexBoxLayout(label, timePicker);
		card.setFlexDirection(FlexLayout.FlexDirection.ROW);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setBorderRadius(BorderRadius.S);
		card.setBoxSizing(BoxSizing.BORDER_BOX);
		card.setPadding(Uniform.M);
		card.setShadow(Shadow.XS);
		card.setHeightFull();
		card.setMargin(Bottom.L);
		card.setFlexGrow(1, label, timePicker);
		card.setAlignItems(Alignment.CENTER);
		return card;
	}

	private Component createUploadsView() {
		Component uploadZipTitle = createUploadZipTitle();
		Component uploadModelTitle = createUploadModelTitle();
		Component uploadZip = createUploadZip();
		Component uploadModel = createUploadModel();

		Row titles = new Row(uploadZipTitle, uploadModelTitle);
		titles.addClassName(LumoStyles.Margin.Top.XL);
		UIUtils.setMaxWidth(Constants.MAX_WIDTH, titles);
		titles.setWidthFull();
		Row uploads = new Row(uploadZip, uploadModel);
		//uploads.addClassName(LumoStyles.Margin.Top.XL);
		UIUtils.setMaxWidth(Constants.MAX_WIDTH, uploads);
		uploads.setWidthFull();

		FlexBoxLayout uploadsCard = new FlexBoxLayout(titles, uploads);
		uploadsCard.setFlexDirection(FlexDirection.COLUMN);
		uploadsCard.setWidthFull();
		uploadsCard.setMaxWidth(Constants.MAX_WIDTH);
		return uploadsCard;
	}

	private Component createUploadModelTitle() {
		FlexBoxLayout header = createHeader(VaadinIcon.FILE_TREE_SUB, "Carica Nuovo Classificatore");
		FlexBoxLayout reports = new FlexBoxLayout(header);
		reports.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		reports.setPadding(Right.RESPONSIVE_L, Left.RESPONSIVE_L);
		return reports;
	}

	private Component createUploadZipTitle() {
		FlexBoxLayout header = createHeader(VaadinIcon.FILE_ZIP, "Carica Nuova Classificazione del Campo");
		FlexBoxLayout reports = new FlexBoxLayout(header);
		reports.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		reports.setPadding(Right.RESPONSIVE_L, Left.RESPONSIVE_L);
		return reports;
	}

	private Component createUploadModel() {
		//FlexBoxLayout header = createHeader(VaadinIcon.FILE_TREE_SUB, "Carica Nuovo Classificatore");
		Upload uploadModel = new Upload(buffer);
		uploadModel.setId("i18n-uploadModel");
		uploadModel.setSizeFull();
		String[] ACCEPTED_MIME_TYPES = {"application/zip", "application/x-zip", "application/x-zip-compressed", "multipart/x-zip"};
		uploadModel.setAcceptedFileTypes(ACCEPTED_MIME_TYPES); 
		uploadModel.setAutoUpload(true);
		//Div output = new Div();

		uploadModel.addSucceededListener(event -> {
			System.out.println("Absolute path: " + buffer.getTmpFile().getAbsolutePath());
			System.out.println("MIMEtype: " + event.getMIMEType());
			String filename = buffer.getFileName();
			try {
				File f = new File(Constants.modelPath);
				if(!f.exists()) f.mkdirs();
				Files.copy(buffer.getTmpFile().toPath(), Path.of(Constants.modelPath + filename));
				HttpHandler.addModel(filename);
				ArrayList<Model> newModels = HttpHandler.getAllModels();
				models.removeAll(models);
				models.addAll(newModels);
				//modelsNames.removeAll(modelsNames);
				for(Model m : models) {
					if(!modelsNames.contains(m.getName())) {
						modelsNames.add(m.getName());
					}
					
				}
				modelSelection.setItems(modelsNames);
				modelSelection.setValue(selectedModel.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		});

		uploadModel.setI18n(i18n);

		FlexBoxLayout card = new FlexBoxLayout(uploadModel);
		card.setPadding(Right.L, Left.L, Bottom.S, Top.S);
		card.setAlignItems(FlexComponent.Alignment.CENTER);
		card.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		card.setMinHeight("200px");
		UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, card);
		UIUtils.setBorderRadius(BorderRadius.S, card);
		UIUtils.setShadow(Shadow.XS, card);

		FlexBoxLayout reports = new FlexBoxLayout(/*header,*/ card);
		reports.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		reports.setPadding(Bottom.XL, Right.RESPONSIVE_L, Left.RESPONSIVE_L);
		return reports;
	}

	private Component createUploadZip() {
		modelsNames = new ArrayList<String>();
		if(models != null) {
			for(Model m : models) {
				modelsNames.add(m.getName());
			}
		}
		

		modelSelection = new ComboBox<>("Seleziona il modello che vuoi usare", modelsNames);

		if(selectedModel != null) {
			modelSelection.setValue(selectedModel.getName());
		}

		modelSelection.setPlaceholder("Nessun modello selezionato");
		modelSelection.setWidthFull();;
		modelSelection.addValueChangeListener(event -> {
			int statusCode = HttpHandler.setModel(event.getValue());
			if(statusCode == 200);
			else modelSelection.setValue(event.getOldValue());
		});
		
		

		Upload uploadZipImages = new Upload(buffer);
		uploadZipImages.setId("i18n-uploadZipImages");
		String[] ACCEPTED_MIME_TYPES = {"application/zip", "application/x-zip", "application/x-zip-compressed", "multipart/x-zip"}; 
		uploadZipImages.setAcceptedFileTypes(ACCEPTED_MIME_TYPES); 
		uploadZipImages.setAutoUpload(true);
		uploadZipImages.setSizeFull();

		uploadZipImages.addSucceededListener(event -> {
			System.out.println("Absolute path: " + buffer.getTmpFile().getAbsolutePath());
			String filename = buffer.getFileName();
			try {
				File f = new File(Constants.zipSourcePath);
				if(!f.exists()) f.mkdirs();
				Files.copy(buffer.getTmpFile().toPath(), Path.of(Constants.zipSourcePath + filename));
				HttpHandler.startnewClassification(filename);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		});

		uploadZipImages.setI18n(i18n);
		/*
		modelSelection.addValueChangeListener(event -> {
			String selection = event.getValue();
			HttpHandler.setModel(selection);

		});
		*/

		FlexBoxLayout modelAndZip = new FlexBoxLayout(modelSelection, uploadZipImages);
		modelAndZip.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		modelAndZip.setSizeFull();

		FlexBoxLayout card = new FlexBoxLayout(modelAndZip);
		card.setPadding(Right.XL, Left.L, Bottom.S, Top.S);
		card.setAlignItems(FlexComponent.Alignment.START);
		card.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		card.setMinHeight("200px");
		UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, card);
		UIUtils.setBorderRadius(BorderRadius.S, card);
		UIUtils.setShadow(Shadow.XS, card);
		
		FlexBoxLayout reports = new FlexBoxLayout(/*header,*/ card);
		reports.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		reports.setPadding(Bottom.XL, Right.RESPONSIVE_L, Left.RESPONSIVE_L);
		return reports;

	}

	private Component createConfItemView(ConfigurationItem item) {
		FlexBoxLayout confItem = new FlexBoxLayout(
				createHeader(VaadinIcon.CONNECT_O, item.getId()),
				createConfItemChart(item));
		confItem.setBoxSizing(BoxSizing.BORDER_BOX);
		confItem.setDisplay(Display.BLOCK);
		confItem.setMargin(Top.L);
		confItem.setMaxWidth(Constants.MAX_WIDTH);
		confItem.setPadding(Horizontal.RESPONSIVE_L);
		confItem.setWidthFull();
		return confItem;
	}

	private Component createConfItemChart(ConfigurationItem item) {
		Property[] properties = item.getProperties();


		FlexBoxLayout card = new FlexBoxLayout();
		card.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		for(Property prop : properties) {
			FlexBoxLayout idLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L,  prop.getId() + ":"));
			idLabel.setMargin(Right.XL);
			idLabel.setMinWidth("100px");
			FlexBoxLayout valueLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L,  prop.getValue()));
			FlexBoxLayout propertyLabels = new FlexBoxLayout();
			propertyLabels.setFlexDirection(FlexLayout.FlexDirection.ROW);
			propertyLabels.add(idLabel, valueLabel);
			propertyLabels.setFlexGrow(2, idLabel, valueLabel);
			card.add(propertyLabels);

		}
		
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setBorderRadius(BorderRadius.S);
		card.setBoxSizing(BoxSizing.BORDER_BOX);
		card.setPadding(Uniform.M);
		card.setShadow(Shadow.XS);
		card.setHeightFull();
		card.setMargin(Bottom.L);
		return card;
		
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
