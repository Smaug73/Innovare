package com.innovare.control;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.Sample;
import com.mongodb.Block;
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
	private JsonWriterSettings jsonset= JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build();
	
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
		
		Document myDoc= collection.find(com.mongodb.client.model.Filters.exists("timestamp")).sort(com.mongodb.client.model.Sorts.descending("timestamp")).first();
		
		System.out.print(myDoc.toJson(	jsonset) );
		/*
		Document samplemax=null;
		//Preleviamo tutti i domenti presenti
		for(Document cur: collection.find()) {
			if(samplemax==null)
				samplemax=cur;
			else if(cur.getLong("timestamp")>samplemax.getLong("timestamp"))
				samplemax=cur;
		}
		
		return new ObjectMapper().readValue(samplemax.toJson(), new TypeReference<Sample>(){});
		*/
		return new ObjectMapper().readValue(myDoc.toJson(jsonset), new TypeReference<Sample>(){});
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
		
		Block<Document> addBlock = new Block<Document>() {
		     @Override
		     public void apply(final Document document) {
		         Logger.getLogger().print(document.toJson(jsonset));
		         samplesDoc.add(new JsonObject(document.toJson(jsonset)));
		     }
		};
		
		/*collection.find(com.mongodb.client.model.Filters.gt("timestamp",tmpMin)).forEach(doc->{
			samplesDoc.add(new JsonObject(doc.toJson()));
		});*/
		collection.find(com.mongodb.client.model.Filters.gt("timestamp",tmpMin)).forEach(addBlock);
		
		return new ObjectMapper().readValue(samplesDoc.toString(), new TypeReference<ArrayList<Sample>>(){});
		
	}
	
	
	
	/*
	 * Recupera gli ultimi dati da i canali passati e ne calcola la media
	 */
	public float meanMeasureOfChannels(ArrayList<Integer> channelsId) throws JsonMappingException, JsonProcessingException {
		
		ArrayList<Sample> measurs= new ArrayList<Sample>();
		
		//Recuperiamo gli ultimi valori letti da questi canali 
		for(Integer id: channelsId) {
			measurs.add(this.getLastSamplesFromMongoSynch(id));
		}
		
		//Calcoliamo la media
		return this.media(measurs);
	}
	
	
	
	
	//Calcolo media delle misure passate
	public float media(ArrayList<Sample> measurs) {
		float mean=0;
		
		for(Sample s: measurs) {
			mean=mean+s.getMisure();
		}
		
		return mean/measurs.size();
		
	}
	
	
	
	
}
