package com.innovare.views.configurazione;


import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.innovare.utils.ConfigurationItem;
import com.innovare.utils.Constants;
import com.innovare.utils.HttpHandler;
import com.innovare.utils.IrrigationState;
import com.innovare.utils.Irrigazione;
import com.innovare.utils.Model;
import com.innovare.utils.Property;
import com.innovare.views.home.HomeView;
import com.innovare.views.main.ContentView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.board.Row;
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
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.json.simple.parser.ParseException;

@Route(value = "config", layout = ContentView.class)
@PageTitle("Configurazione")
@CssImport("./styles/views/innovare/innovare-view.css")
public class ConfigurazioneView extends Div {
	
	private ArrayList<ConfigurationItem> configurationItems;
	private ArrayList<Model> models;
	private Model selectedModel;
	private boolean isIrrigationOn;
	private Irrigazione lastIrrigation;
	private UploadI18N i18n;
	private UploadBuffer buffer;

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
                        .setRemainingTime(
                                new UploadI18N.Uploading.RemainingTime()
                                        .setPrefix("tempo rimanente: ")
                                        .setUnknown(
                                                "tempo rimanente non disponibile"))
                        .setError(new UploadI18N.Uploading.Error()
                                .setServerUnavailable("Impossibile collegarsi al server")
                                .setUnexpectedServerError(
                                        "Errore inaspettato del server")
                                .setForbidden("Caricamento vietato")))
                .setUnits(Stream
                        .of("B", "KByte", "MByte", "GByte", "TByte", "PByte",
                                "EByte", "ZByte", "YByte")
                        .collect(Collectors.toList()));
        
        add(createContent());
		
    }

    // Recupera i dati da mostrare: i configuration item, i modelli disponibili per la classificazione, 
    // lo stato dell'irrigazione e le info sull'ultima irrigazione (quella attuale se l'irrigazione è in corso)
    private void getData() {
    	/*configurationItems = new ArrayList<ConfigurationItem>();
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
    	configurationItems.add(item);
    	lastIrrigation = new Irrigazione(new Timestamp(System.currentTimeMillis() - 64872389),
    			new Timestamp(System.currentTimeMillis()), 58.34);
    	isIrrigationOn = true;
    	models = new ArrayList<Model>();
    	Model model = new Model("modello 1");
    	models.add(model);*/
    	
    	configurationItems = HttpHandler.getAllConfigurationItems();
    	isIrrigationOn = HttpHandler.getCurrentIrrigationState();
    	lastIrrigation = HttpHandler.getLastIrrigation();
    	selectedModel = HttpHandler.getSelectedModel();
    	models = HttpHandler.getAllModels();
		
    	
		
	}

    // Crea tutte viste che devono essere mostrate in ConfigurationView:
    // stato dell'irrigazione (con possibilità di modificarlo),
    // informazioni sull'ultima irrigazione (quella attuale se è ancora in corso),
    // la lista di tutti i configuration item con le relative informazioni,
    // il componente di upload dello zip con le immagini da classificare e la scelta del modello da usare per la classificazione,
    // il componente di upload di un nuovo modello di classificazione
	private Component createContent() {
		Component irrigationView = createIrrigationState();
		// Component lastIrrigationView = createLastIrrigation();

		// Per ogni configuration item viene creata una card che ne mostra le proprietà
		FlexBoxLayout confItemsCards = new FlexBoxLayout();
		confItemsCards.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		for(ConfigurationItem item : configurationItems) {
			confItemsCards.add(createConfItemView(item));
		}
		
		// Si crea una card per ogni upload possibile:
		//zip con immagini da classificare e zip con nuovo modello di classificazione
		Component uploads = createUploadsView();
		
		FlexBoxLayout content = new FlexBoxLayout(irrigationView, confItemsCards, uploads);
		content.setAlignItems(FlexComponent.Alignment.CENTER);
		content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		return content;
	}
	
	
	// Crea header della card relativa all'ultima irrigazione e chiama il metodo per la creazione della card stessa
	private Component createLastIrrigation() {
    	FlexBoxLayout lastIrr = new FlexBoxLayout(
  				createHeader(VaadinIcon.DROP, "Ultima Irrigazione"),
  				createLastIrrigationCard());
    	lastIrr.setBoxSizing(BoxSizing.BORDER_BOX);
    	lastIrr.setDisplay(Display.BLOCK);
    	lastIrr.setMargin(Top.L);
    	lastIrr.setMaxWidth(Constants.MAX_WIDTH);
    	lastIrr.setSizeFull();
  		return lastIrr;
	}

	// Crea la card in cui si forniscono le info sull'ultima irrigazione (quella in corso se l'irrigazione è attiva)
	private Component createLastIrrigationCard() {
		
		FlexBoxLayout fromLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Dalle:"));
		fromLabel.setWidth("200px");
		FlexBoxLayout fromDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, lastIrrigation.getInizioIrrig().toString()));
		
		
		FlexBoxLayout from = new FlexBoxLayout(fromLabel, fromDate);
		from.setFlexDirection(FlexLayout.FlexDirection.ROW);
		from.setFlexGrow(2, fromLabel, fromDate);
		
		FlexBoxLayout toLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Alle:"));
		toLabel.setWidth("200px");
		
		FlexBoxLayout toDate;
		if(lastIrrigation.getFineIrrig() != null) {
			toDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, lastIrrigation.getFineIrrig().toString()));
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

	// Crea header della card relativa allo stato dell'irrigazione e chiama il metodo per la creazione della card stessa
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

	
	// Crea la card in cui si fornisce lo stato dell'irrigazione (Attiva o Spenta);
	// inoltre è presente la possibilità di accendere o spegnere l'irrigazione
	private Component createIrrigationStateCard() {
		
		String accendi = "ON";
		String spegni = "OFF";
		String state;
		String colorState;
		RadioButtonGroup<String> on_off = new RadioButtonGroup<>();
		on_off.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
		on_off.setItems(accendi, spegni);
		
		if(isIrrigationOn) {
			state = IrrigationState.ACCESO.getName();
			colorState = IrrigationState.ACCESO.getColor();
			on_off.setValue(accendi);
		}
		else {
			state = IrrigationState.SPENTO.getName();
			colorState = IrrigationState.SPENTO.getColor();
			on_off.setValue(spegni);
		}
		
		
		FlexBoxLayout descLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Stato dell'impianto irriguo:"));
		descLabel.setWidth("200px");
		descLabel.setAlignItems(Alignment.CENTER);
		
		Icon icon = UIUtils.createIcon(IconSize.S, colorState, VaadinIcon.CIRCLE);
		Label label = UIUtils.createLabel(FontSize.L, state);
		FlexBoxLayout status = new FlexBoxLayout(icon, label);
		status.setSpacing(Right.S);
		status.setAlignItems(Alignment.CENTER);
		
		FlexBoxLayout card = new FlexBoxLayout(descLabel, status, on_off);
		card.setFlexDirection(FlexLayout.FlexDirection.ROW);
		card.setFlexGrow(1, descLabel, status);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setBorderRadius(BorderRadius.S);
		card.setBoxSizing(BoxSizing.BORDER_BOX);
		card.setPadding(Uniform.M);
		card.setShadow(Shadow.XS);
		card.setHeightFull();
		card.setMargin(Bottom.L);
		
		FlexBoxLayout lastIrrigationView = new FlexBoxLayout(createLastIrrigation());
		
		FlexBoxLayout cards = new FlexBoxLayout(card, lastIrrigationView);
		cards.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		
		on_off.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent event) {
				if(event.getValue().equals(accendi)) {
					change(event, IrrigationState.ACCESO);
				}
				else {
					change(event, IrrigationState.SPENTO);

				}
			}
			
			private void change(ValueChangeEvent event, IrrigationState irrState) {
				Irrigazione newIrrigation = HttpHandler.startIrrigation();
				// Se la richiesta va a buon fine, è necessario cambiare
				// le informazioni riguardanti lo stato attuale e l'ultima irrigazione
				if(newIrrigation != null) {
					lastIrrigation = newIrrigation;
					icon.setColor(irrState.getColor());
					label.setText(irrState.getName());
					changeLastIrrigationView();

				}
				// Se la richiesta non va a buon fine, è necessario settare
				// il valore della ComboBox al valore precedente al cambio
				else {
				
					on_off.setValue((String)event.getOldValue());
				}
			}

			private void changeLastIrrigationView() {
				cards.remove(cards.getComponentAt(1));
				cards.add(createLastIrrigation());
			}
		
		});
		
		return cards;
	}

	private Component createUploadsView() {
		Component uploadZip = createUploadZip();
		Component uploadModel = createUploadModel();

		Row uploads = new Row(uploadZip, uploadModel);
		uploads.addClassName(LumoStyles.Margin.Top.XL);
		UIUtils.setMaxWidth(Constants.MAX_WIDTH, uploads);
		uploads.setWidthFull();

		return uploads;
	}

	private Component createUploadModel() {
		FlexBoxLayout header = createHeader(VaadinIcon.FILE_TREE_SUB, "Carica Nuovo Classificatore");
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
        });
        
        uploadModel.setI18n(i18n);
        
        FlexBoxLayout card = new FlexBoxLayout(uploadModel);
        card.setPadding(Right.L, Left.L, Top.S, Bottom.S);
		card.setAlignItems(FlexComponent.Alignment.CENTER);
		card.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		card.setMinHeight("200px");
		UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, card);
		UIUtils.setBorderRadius(BorderRadius.S, card);
		UIUtils.setShadow(Shadow.XS, card);

		FlexBoxLayout reports = new FlexBoxLayout(header, card);
		reports.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		reports.setPadding(Bottom.XL, Right.RESPONSIVE_L, Left.RESPONSIVE_L);
		return reports;
	}

	private Component createUploadZip() {
		Collection<String> modelsNames = new ArrayList<String>();
		for(Model m : models) {
			modelsNames.add(m.getName());
		}
		
		ComboBox<String> modelSelection = new ComboBox<>("Seleziona il modello che vuoi usare", modelsNames);
		
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
        
		
		FlexBoxLayout header = createHeader(VaadinIcon.FILE_ZIP, "Carica Nuova Classificazione del Campo");
		
		Upload uploadZipImages = new Upload(buffer);
        uploadZipImages.setId("i18n-uploadZipImages");
        String[] ACCEPTED_MIME_TYPES = {"application/zip", "application/x-zip", "application/x-zip-compressed", "multipart/x-zip"}; 
        uploadZipImages.setAcceptedFileTypes(ACCEPTED_MIME_TYPES); 
        uploadZipImages.setAutoUpload(true);
        uploadZipImages.setSizeFull();
        uploadZipImages.setVisible(false);

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
        modelSelection.addValueChangeListener(event -> {
				uploadZipImages.setVisible(true);
        	
        });
        
        
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

		FlexBoxLayout reports = new FlexBoxLayout(header, card);
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
  		//confItem.setPadding(Horizontal.RESPONSIVE_L);
  		confItem.setHeightFull();
  		return confItem;
	}

	private Component createConfItemChart(ConfigurationItem item) {
		Property[] properties = item.getProperties();
		
		
		FlexBoxLayout propertiesLabel = new FlexBoxLayout();
		propertiesLabel.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		for(Property prop : properties) {
			FlexBoxLayout idLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L,  prop.getId() + ":"));
			idLabel.setMargin(Right.XL);
			idLabel.setMinWidth("100px");
			FlexBoxLayout valueLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L,  prop.getValue()));
			FlexBoxLayout propertyLabels = new FlexBoxLayout();
			propertyLabels.setFlexDirection(FlexLayout.FlexDirection.ROW);
			propertyLabels.add(idLabel, valueLabel);
			propertiesLabel.add(propertyLabels);
		}
		
		FlexBoxLayout card = new FlexBoxLayout(propertiesLabel);
		card.setBackgroundColor(LumoStyles.Color.BASE_COLOR);
		card.setBorderRadius(BorderRadius.S);
		card.setBoxSizing(BoxSizing.BORDER_BOX);
		card.setPadding(Uniform.M);
		card.setShadow(Shadow.XS);
		card.setHeightFull();
		card.setMargin(Bottom.XL);
		return card;
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

}

