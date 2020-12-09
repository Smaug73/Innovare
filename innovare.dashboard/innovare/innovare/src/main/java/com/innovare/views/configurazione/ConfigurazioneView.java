package com.innovare.views.configurazione;


import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
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
import com.innovare.utils.IrrigationState;
import com.innovare.utils.Property;
import com.innovare.views.home.HomeView;
import com.innovare.views.main.ContentView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.internal.MessageDigestUtil;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Slider;

import org.json.simple.parser.ParseException;

@Route(value = "config", layout = ContentView.class)
@PageTitle("Configurazione")
@CssImport("./styles/views/innovare/innovare-view.css")
//@Secured(Role.ADMIN)
public class ConfigurazioneView extends Div {
	
	private String separator = System.getProperty("file.separator");
	private String zipDirectoryPath = System.getProperty("user.home") + separator + "InnovareZip" + separator;
	private String modelsDirectoryPath = System.getProperty("user.home") + separator + "InnovareModels" + separator;
	private ArrayList<ConfigurationItem> configurationItems = new ArrayList<ConfigurationItem>();
	public static final String MAX_WIDTH = (Toolkit.getDefaultToolkit().getScreenSize().width - 100) + "px";
	private UploadI18N i18n;
	private UploadBuffer buffer;

    public ConfigurazioneView() throws IOException, InterruptedException, URISyntaxException, ParseException {
        setId("config-view");
        
        getData();
        
        buffer = new UploadBuffer();
        
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

    private void getData() {
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
    	/*URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("localhost:8888").setPath("/configuration");
	        
	    HttpClient client = HttpClient.newHttpClient();
	        HttpRequest request;
			try {
				request = HttpRequest.newBuilder()
				        .uri(builder.build())
				        .build();
				HttpResponse<String> response = client.send(request,
				         HttpResponse.BodyHandlers.ofString());
				
				configurationItems = new ObjectMapper().readValue(response.body(), new TypeReference<ArrayList<ConfigurationItem>>(){});
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		*/
		
	}

	private Component createContent() {
		Component irrigation = createIrrigationState();
		Component lastIrrigation = createLastIrrigation();
    	Component confItemView = createConfItemView();
		Component uploads = createUploadsView();

		FlexBoxLayout content = new FlexBoxLayout(irrigation, lastIrrigation, confItemView, uploads);
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
    	lastIrr.setMaxWidth(MAX_WIDTH);
    	lastIrr.setPadding(Horizontal.RESPONSIVE_L);
    	lastIrr.setWidthFull();
  		return lastIrr;
	}

	private Component createLastIrrigationCard() {
		
		FlexBoxLayout fromLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Dalle:"));
		fromLabel.setWidth("200px");
		FlexBoxLayout fromDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, HomeView.DATE_FORMAT.format(new Date()) 
				+ " " + HomeView.TIME_FORMAT.format(new Date())));
		
		
		FlexBoxLayout from = new FlexBoxLayout(fromLabel, fromDate);
		from.setFlexDirection(FlexLayout.FlexDirection.ROW);
		from.setFlexGrow(2, fromLabel, fromDate);
		
		FlexBoxLayout toLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Alle:"));
		toLabel.setWidth("200px");
		/*FlexBoxLayout toDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, DATE_FORMAT.format(new Date()) 
				+ " " + TIME_FORMAT.format(new Date())));*/
		FlexBoxLayout toDate = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "In corso"));
		
		FlexBoxLayout to = new FlexBoxLayout(toLabel, toDate);
		to.setFlexDirection(FlexLayout.FlexDirection.ROW);
		to.setFlexGrow(2, toLabel, toDate);
		
		FlexBoxLayout quantitaLabel = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "Quantità:"));
		quantitaLabel.setWidth("200px");
		FlexBoxLayout quantitaL = new FlexBoxLayout(UIUtils.createLabel(FontSize.L, "230.92 L"));
		
		
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
  		irrState.setMaxWidth(MAX_WIDTH);
  		irrState.setPadding(Horizontal.RESPONSIVE_L);
  		irrState.setWidthFull();
  		return irrState;
	}

	private Component createIrrigationStateCard() {
		
		/* 
		 * Recupero stato irrigazione da middleware per iniziallizare
		 * la variabile booleana "acceso"
		 */
		boolean acceso = true;
		String accendi = "ON";
		String spegni = "OFF";
		String state;
		String colorState;
		RadioButtonGroup<String> on_off = new RadioButtonGroup<>();
		on_off.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
		on_off.setItems(accendi, spegni);
		
		if(acceso) {
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
		
		FlexBoxLayout status = new FlexBoxLayout(
				UIUtils.createIcon(IconSize.S, colorState, VaadinIcon.CIRCLE),
				UIUtils.createLabel(FontSize.L, state));
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
		return card;
	}

	private Component createUploadsView() {
		Component uploadZip = createUploadZip();
		Component uploadModel = createUploadModel();

		Row uploads = new Row(uploadZip, uploadModel);
		uploads.addClassName(LumoStyles.Margin.Top.XL);
		UIUtils.setMaxWidth(MAX_WIDTH, uploads);
		uploads.setWidthFull();

		return uploads;
	}

	private Component createUploadModel() {
		FlexBoxLayout header = createHeader(VaadinIcon.FILE_TREE_SUB, "Carica Nuovo Classificatore");
		Upload uploadModel = new Upload(buffer);
        uploadModel.setId("i18n-uploadModel");
        uploadModel.setSizeFull();
        //uploadModel.setAcceptedFileTypes("application/x-hdf"); 
        uploadModel.setAutoUpload(true);
        //Div output = new Div();

        uploadModel.addSucceededListener(event -> {
            System.out.println("Absolute path: " + buffer.getTmpFile().getAbsolutePath());
            System.out.println("MIMEtype: " + event.getMIMEType());
            String filename = buffer.getFileName();
            try {
            	File f = new File(modelsDirectoryPath);
        		if(!f.exists()) f.mkdirs();
				Files.copy(buffer.getTmpFile().toPath(), Path.of(modelsDirectoryPath + filename));
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
		Collection<String> models = new ArrayList();
		models.add("modello1");
		models.add("modello2");
		models.add("modello3");
		
		ComboBox<String> modelSelection = new ComboBox<>("Seleziona il modello che vuoi usare", models);
		 
        modelSelection.setPlaceholder("Nessun modello selezionato");
        modelSelection.setWidthFull();;
 
        
		
		FlexBoxLayout header = createHeader(VaadinIcon.FILE_ZIP, "Carica Nuova Classificazione del Campo");
		
		Upload uploadZipImages = new Upload(buffer);
        uploadZipImages.setId("i18n-uploadZipImages");
        String[] ACCEPTED_MIME_TYPES = {"application/zip", "application/x-zip", "application/x-zip-compressed", "multipart/x-zip"}; 
        uploadZipImages.setAcceptedFileTypes(ACCEPTED_MIME_TYPES); 
        uploadZipImages.setAutoUpload(true);
        uploadZipImages.setSizeFull();
        Div output = new Div();
        uploadZipImages.setVisible(false);

        uploadZipImages.addSucceededListener(event -> {
            Component component = createComponent(event.getMIMEType(),
                    event.getFileName(), buffer.getInputStream());
            showOutput(event.getFileName(), component, output);
            System.out.println("Absolute path: " + buffer.getTmpFile().getAbsolutePath());
            String filename = buffer.getFileName();
            try {
            	File f = new File(zipDirectoryPath);
        		if(!f.exists()) f.mkdirs();
				Files.copy(buffer.getTmpFile().toPath(), Path.of(zipDirectoryPath + filename));
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

	private Component createConfItemView() {
		FlexBoxLayout confItem = new FlexBoxLayout(
  				createHeader(VaadinIcon.CONNECT_O, "Gateway"),
  				createConfItemChart());
  		confItem.setBoxSizing(BoxSizing.BORDER_BOX);
  		confItem.setDisplay(Display.BLOCK);
  		confItem.setMargin(Top.L);
  		confItem.setMaxWidth(MAX_WIDTH);
  		confItem.setPadding(Horizontal.RESPONSIVE_L);
  		confItem.setWidthFull();
  		return confItem;
	}

	private Component createConfItemChart() {
		
		ConfigurationItem item = configurationItems.get(0);
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

	private Component createComponent(String mimeType, String fileName, InputStream stream) {
        Div content = new Div();
        String text = String.format("Mime type: '%s'\nSHA-256 hash: '%s'",
                mimeType, MessageDigestUtil.sha256(stream.toString()));
        content.setText(text);
        return content;

    }
    

    private void showOutput(String text, Component content,
            HasComponents outputContainer) {
        HtmlComponent p = new HtmlComponent(Tag.P);
        p.getElement().setText(text);
        outputContainer.add(p);
        outputContainer.add(content);
    }

}
