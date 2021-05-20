package com.innovare.control;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.Sample;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class SensorDataControllerSync {

	private String uriDB="mongodb://localhost:27017";
	private String databaseName="innovare";
	
	private MongoClient mongoclientSyn;
	private MongoDatabase database;
	
	public SensorDataControllerSync() {
		this.mongoclientSyn= MongoClients.create(this.uriDB);
		this.database= this.mongoclientSyn.getDatabase(this.databaseName);
	}
	
	/*
	 * Last sample di un canale identificato tramite index
	 */
	public Sample getLastSamplesFromMongoSynch(int index) throws JsonMappingException, JsonProcessingException {
		//Accediamo al document del canale selezionato
		MongoCollection<Document> collection = database.getCollection("channel-"+index);
		
		Document samplemax=null;
		//Preleviamo tutti i domenti presenti
		for(Document cur: collection.find()) {
			if(samplemax==null)
				samplemax=cur;
			else if(cur.getLong("timestamp")>samplemax.getLong("timestamp"))
				samplemax=cur;
		}
		
		return new ObjectMapper().readValue(samplemax.toJson(), new TypeReference<Sample>(){});
	}
	
	
	
	/*
	 * Recupero di tutti i framework per un dato lasso di tempo a partire dal timestamp attuale
	 * timeDistance: distanza temporale da considerare da ora
	 * index: indice del canale da prelevare
	 */
	public ArrayList<Sample> getAllSamplesFromTimeFrame(Long timeDistance, int index) throws JsonMappingException, JsonProcessingException {
		//Prendiamo in considerazione il tempo attuale
		long tmp= System.currentTimeMillis();
		//Sottraiamo la distanza temporale
		long tmpMin = tmp- timeDistance;
		
		//Accediamo al document del canale selezionato
		MongoCollection<Document> collection = database.getCollection("channel-"+index);
		
		ArrayList<JsonObject> samplesDoc= new ArrayList<JsonObject>();
		collection.find(com.mongodb.client.model.Filters.gt("timestamp",tmpMin)).forEach(doc->{
			samplesDoc.add(new JsonObject(doc.toJson()));
		});
		
		return new ObjectMapper().readValue(samplesDoc.toString(), new TypeReference<ArrayList<Sample>>(){});
		
	}
	
	
}
