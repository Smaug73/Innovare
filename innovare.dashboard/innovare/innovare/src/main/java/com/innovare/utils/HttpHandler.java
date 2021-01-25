package com.innovare.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.Classification;
import com.innovare.model.ConfigurationItem;
import com.innovare.model.Irrigazione;
import com.innovare.model.Model;
import com.innovare.model.Sample;

public class HttpHandler {
	private final static String HOST = "localhost:8888";
	private final static String ALL = "/allsample/";
	private final static String LAST = "/lastsample/";
	//private final static String LAST_MULTI = "/lastsamples/";
	private final static String MODELS = "/models";
	private final static String CONFIGURATION = "/configurations";
	private final static String ALL_CLASSIF = "/classifications";
	private final static String LAST_CLASSIF = "/lastClassification";
	private final static String ALL_IRR_STATES = "/allIrrigationStates";
	private final static String CURR_IRR_STATE = "/statoIrrigazione";
	private final static String START_IRR = "/startIrrigation";
	private final static String STOP_IRR = "/stopIrrigation";
	private final static String SET_MODEL = "/setModel/";
	private final static String SELECTED_MODEL = "/models/selected";
	private final static String LAST_IRR = "/lastIrrigation";
	private final static String NEW_MODEL = "/newModel/";
	private final static String NEW_CLASSIF = "/newClassification/";
	private final static String ACTIVE_CHANNELS = "/channelsNumber";
	private final static String IRRIG_TIME = "/irrigationTime";
	private final static String SET_IRRIG_TIME = "/setIrrigationTime/";
	


	// Crea l'URIBuilder con l'uri a cui inviare la richiesta
	private static URIBuilder createURIBuilder(String path) {
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(HOST).setPath(path);
		return builder;
	}

	// Invia la richiesta http e restituisce la relativa risposta
	private static HttpResponse<String> sendRequest(String path) {
		URIBuilder builder = createURIBuilder(path);
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request;
		try {
			request = HttpRequest.newBuilder()
					.uri(builder.build())
					.build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			return response;

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;

	}

	// Ricostruisce l'ArrayList dei Sample facendo il parsing del json nel body della risposta
	private static ArrayList<Sample> getSamples(HttpResponse<String> response){
		ArrayList<Sample> samples;
		try {
			samples = new ObjectMapper().readValue(response.body(), new TypeReference<ArrayList<Sample>>(){});
			return samples;
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Ricostruisce il Sample facendo il parsing del json nel body della risposta
	private static Sample getSample(HttpResponse<String> response){
		Sample sample;
		try {
			sample = new ObjectMapper().readValue(response.body(), new TypeReference<Sample>(){});
			return sample;
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Ricostruisce l'ArrayList dei modelli facendo il parsing del json nel body della risposta
	private static ArrayList<Model> getModels(HttpResponse<String> response){
		ArrayList<Model> models;
		try {
			models = new ObjectMapper().readValue(response.body(), new TypeReference<ArrayList<Model>>(){});
			return models;
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// Ricostruisce l'ultimo modello selezionato facendo il parsing del json nel body della risposta
	private static Model getSelectedModel(HttpResponse<String> response){
		Model model;
		try {
			model = new ObjectMapper().readValue(response.body(), new TypeReference<Model>(){});
			return model;
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Ricostruisce l'ArrayList dei Configuration item facendo il parsing del json nel body della risposta
	private static ArrayList<ConfigurationItem> getConfigItems(HttpResponse<String> response){
		ArrayList<ConfigurationItem> items;
		try {
			items = new ObjectMapper().readValue(response.body(), new TypeReference<ArrayList<ConfigurationItem>>(){});
			return items;
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Ricostruisce l'ArrayList delle classificazioni facendo il parsing del json nel body della risposta
	private static ArrayList<Classification> getClassifications(HttpResponse<String> response){
		ArrayList<Classification> classifications;
		try {
			classifications = new ObjectMapper().readValue(response.body(), new TypeReference<ArrayList<Classification>>(){});
			return classifications;
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Ricostruisce l'ArrayList degli stati passati delle irrigazioni facendo il parsing del json nel body della risposta
	private static ArrayList<Irrigazione> getIrrigationStates(HttpResponse<String> response){
		ArrayList<Irrigazione> irrigStates;
		try {
			irrigStates = new ObjectMapper().readValue(response.body(), new TypeReference<ArrayList<Irrigazione>>(){});
			return irrigStates;
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Ricostruisce lo stato dell'irrigazione facendo il parsing del json nel body della risposta
	private static String getIrrigationState(HttpResponse<String> response){
		String irrState;
		irrState = response.body();
		return irrState;
		
	}

	// Ricostruisce lo stato dell'irrigazione facendo il parsing del json nel body della risposta
	private static Irrigazione getLastIrrigation(HttpResponse<String> response){
		Irrigazione irr;
		try {
			irr = new ObjectMapper().readValue(response.body(), new TypeReference<Irrigazione>(){});
			return irr;
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// Ricostruisce l'ArrayList dei canali attivi (sensori suolo) facendo il parsing del json nel body della risposta
	private static ArrayList<Integer> getActiveChannels(HttpResponse<String> response){
		ArrayList<Integer> items;
		try {
			items = new ObjectMapper().readValue(response.body(), new TypeReference<ArrayList<Integer>>(){});
			return items;
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static LocalTime getIrrigTime(HttpResponse<String> response) {
		String time;
		try {
			time = new ObjectMapper().readValue(response.body(), new TypeReference<String>(){});
			return LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Recupera lo storico delle classificazioni dal middleware
	public static ArrayList<Classification> getAllClassifications() {
		String path = ALL_CLASSIF;
		HttpResponse<String> response = sendRequest(path);
		return getClassifications(response);
	}

	// Recupera le ultime classificazioni dal middleware
	public static ArrayList<Classification> getLastClassifications() {
		String path = LAST_CLASSIF;
		HttpResponse<String> response = sendRequest(path);
		return getClassifications(response);
	}
	
	// Recupera dal middleware tutti i valori dal canale indicato
	public static ArrayList<Sample> getAllSamples(int chan) {
		String path = ALL + chan;
		HttpResponse<String> response = sendRequest(path);
		return getSamples(response);
	}

	
	// Recupera dal middleware l'ultimo valore dal canale indicato
	public static Sample getLastSample(int chan) {
		String path = LAST + chan;
		HttpResponse<String> response = sendRequest(path);
		return getSample(response);
	}

	

	// Recupera lo storico delle irrigazioni dal middleware
	public static ArrayList<Irrigazione> getAllIrrigationStates() {
		String path = ALL_IRR_STATES;
		HttpResponse<String> response = sendRequest(path);
		return getIrrigationStates(response);
	}

	// Manda il commando di avvio/stop dell'irrigazione al middleware
	public static Irrigazione startIrrigation() {
		String path = START_IRR;
		//HttpResponse<String> response = sendRequest(path);
		return new Irrigazione(new Timestamp(System.currentTimeMillis()), null, 76.98);
	}
	
	// Manda il commando di avvio/stop dell'irrigazione al middleware
	public static Irrigazione stopIrrigation() {
		String path = STOP_IRR;
		//HttpResponse<String> response = sendRequest(path);
		return new Irrigazione(new Timestamp(System.currentTimeMillis()), 
				new Timestamp(System.currentTimeMillis() + 34375467), 76.98);
	}


	// Recupera lo stato attuale dell'irrigazione dal middleware
	public static String getCurrentIrrigationState() {
		String path = CURR_IRR_STATE;
		HttpResponse<String> response = sendRequest(path);
		return getIrrigationState(response);
	}
	
	// Recupera lo stato attuale dell'irrigazione dal middleware
	public static Irrigazione getLastIrrigation() {
		String path = LAST_IRR;
		HttpResponse<String> response = sendRequest(path);
		return getLastIrrigation(response);
	}


	// Recupera tutti i modelli di classificazione disponibili dal middleware
	public static ArrayList<Model> getAllModels() {
		String path = MODELS;
		HttpResponse<String> response = sendRequest(path);
		return getModels(response);
	}
	
	// Recupera l'ultimo modello selezionato dal middleware
	public static Model getSelectedModel() {
		String path = SELECTED_MODEL;
		HttpResponse<String> response = sendRequest(path);
		return getSelectedModel(response);
	}

	// Manda al middleware il nome del modello che si vuole usare per la classificazione
	public static int setModel(String modelName) {
		String path = SET_MODEL + modelName;
		HttpResponse<String> response = sendRequest(path);
		return response.statusCode();
	}
	
	// Avvisa il middleware del caricamento di un nuovo modello di classificazione
	public static int addModel(String fileName) {
		String path = NEW_MODEL + fileName;
		HttpResponse<String> response = sendRequest(path);
		return response.statusCode();
	}
	
	// Avvisa il middleware del caricamento di un nuovo zip di immagini
	public static int startnewClassification(String datasetName) {
		String path = NEW_CLASSIF + datasetName;
		HttpResponse<String> response = sendRequest(path);
		return response.statusCode();
	}

	// Recupera tutti i configuration item dal middleware
	public static ArrayList<ConfigurationItem> getAllConfigurationItems() {
		String path = CONFIGURATION;
		HttpResponse<String> response = sendRequest(path);
		ArrayList<ConfigurationItem> items = getConfigItems(response);
		return items;
	}
	
	// Richiede quali canali sono attivi per il recupero dei dati riguardanti il suolo
	public static ArrayList<Integer> getActiveChannels() {
		String path = ACTIVE_CHANNELS;
		HttpResponse<String> response = sendRequest(path);
		ArrayList<Integer> items = getActiveChannels(response);
		return items;
	}
	
	// Recupera l'orario in cui viene fatta partire l'irrigazione
	public static LocalTime getIrrigTime() {
		String path = IRRIG_TIME;
		HttpResponse<String> response = sendRequest(path);
		LocalTime time = getIrrigTime(response);
		return time;
	}
	
	// Recupera l'orario in cui viene fatta partire l'irrigazione
	public static int setIrrigTime(String time) {
		String path = SET_IRRIG_TIME + time;
		HttpResponse<String> response = sendRequest(path);
		return response.statusCode();
	}
}
