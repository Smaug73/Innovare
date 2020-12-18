package com.innovare.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpHandler {
	private final static String HOST = "localhost:8888";
	private final static String ALL = "/allsample/";
	private final static String LAST = "/lastsample/";
	private final static String LAST_MULTI = "/lastsamples/";
	private final static String MODELS = "/models";
	private final static String CONFIGURATION = "/configurations";
	private final static String ALL_CLASSIF = "/classifications";
	private final static String LAST_CLASSIF = "/lastClassifications";
	private final static String ALL_IRR_STATES = "/allIrrigationStates";
	private final static String CURR_IRR_STATE = "/statoIrrigazione";
	private final static String START_IRR = "/startIrrigation";
	private final static String STOP_IRR = "/stopIrrigation";
	private final static String SET_MODEL = "/setModel/";
	private final static String SELECTED_MODEL = "/models/selected";
	private final static String LAST_IRR = "/lastIrrigation";
	private final static String NEW_MODEL = "/newModel/";
	private final static String NEW_CLASSIF = "/newClassification/";

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
	private static Boolean getIrrigationState(HttpResponse<String> response){
		boolean irrState;
		try {
			irrState = new ObjectMapper().readValue(response.body(), new TypeReference<Boolean>(){});
			return irrState;
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
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

	// Recupera lo storico delle umidità ambientali dal middleware
	public static ArrayList<Sample> getAllHumAmb() {
		String path = ALL + Channel.HUMIDITY_AMB.getValue();
		HttpResponse<String> response = sendRequest(path);
		return getSamples(response);
	}

	// Recupera l'ultimo valore registrato dell'umidità ambientale dal middleware
	public static Sample getLastHumAmb() {
		String path = LAST + Channel.HUMIDITY_AMB.getValue();
		HttpResponse<String> response = sendRequest(path);
		return getSample(response);
	}

	// Recupera lo storico delle temperature ambientali dal middleware
	public static ArrayList<Sample> getAllTempAmb() {
		String path = ALL + Channel.TEMPERATURE_AMB.getValue();
		HttpResponse<String> response = sendRequest(path);
		return getSamples(response);
	}

	// Recupera l'ultimo valore registrato della temperatura ambientale dal middleware
	public static Sample getLastTempAmb() {
		String path = LAST + Channel.TEMPERATURE_AMB.getValue();
		HttpResponse<String> response = sendRequest(path);
		return getSample(response);
	}

	// Recupera lo storico delle piogge dal middleware
	public static ArrayList<Sample> getAllRainData() {
		String path = ALL + Channel.RAIN.getValue();
		HttpResponse<String> response = sendRequest(path);
		return getSamples(response);
	}

	// Recupera l'ultimo valore registrato sulle piogge dal middleware
	public static Sample getLastRainData() {
		String path = LAST + Channel.RAIN.getValue();
		HttpResponse<String> response = sendRequest(path);
		return getSample(response);
	}

	// Recupera lo storico delle temperature del suolo dal middleware
	public static ArrayList<Sample> getAllTempSuolo() {
		String path = ALL + Channel.TEMPERATURE_SUOLO.getValue();
		HttpResponse<String> response = sendRequest(path);
		return getSamples(response);
	}

	// Recupera l'ultimo valore registrato della temperatura del suolo dal middleware
	public static ArrayList<Sample> getLastTempSuolo() {
		String path = LAST_MULTI + Channel.TEMPERATURE_SUOLO.getValue();
		HttpResponse<String> response = sendRequest(path);
		return getSamples(response);
	}

	// Recupera lo storico delle umidità del suolo dal middleware
	public static ArrayList<Sample> getAllHumSuolo() {
		String path = ALL + Channel.HUMIDITY_SUOLO.getValue();
		HttpResponse<String> response = sendRequest(path);
		return getSamples(response);
	}

	// Recupera l'ultimo valore registrato dell'umidità del suolo dal middleware
	public static ArrayList<Sample> getLastHumSuolo() {
		String path = LAST_MULTI + Channel.HUMIDITY_SUOLO.getValue();
		HttpResponse<String> response = sendRequest(path);
		return getSamples(response);
	}

	// Recupera lo storico dei venti dal middleware
	public static ArrayList<Sample> getAllWindData() {
		String path = ALL + Channel.WIND.getValue();
		HttpResponse<String> response = sendRequest(path);
		return getSamples(response);

	}

	// Recupera l'ultimo valore registrato del vento dal middleware
	public static Sample getLastWindData() {
		String path = LAST + Channel.WIND.getValue();
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
	public static boolean getCurrentIrrigationState() {
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
}
