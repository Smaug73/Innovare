package com.innovare.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovare.model.Sample;
import com.mongodb.client.MongoClients;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.core.Promise;



public class SensorDataController {
	
/*
 * Classe che si occuppa della prelevazione dal mongoDb dei sample necessari
 */
	private MongoClient mongoClient;
	private ArrayList<Integer> listChannelsId; 
	private ArrayList<Sample> samplesResult= new ArrayList<Sample>();
	private HashMap<Integer, Sample> samplesMapResult= new HashMap<Integer, Sample>();
	
	/*
	 * Recuperiamo la lista degli ID dei canali da utilizzare dal ConfigurationController
	 */
	public SensorDataController(MongoClient mongo, ArrayList<Integer> listChannelsId) {
		this.mongoClient=mongo;
		
		/*
		 * Recupertiamo informazioni riguardo i canali da andare a considerare
		 */
		this.listChannelsId= listChannelsId;
	}
	
	
	/*
	 * Il metodo ritornera' il risultato di tutte le chiamate eseguite su mongo
	 */
	public Future<HashMap<Integer, Sample>> getLastSamples() throws Exception{
		
		Promise<HashMap<Integer, Sample>> resultPromise = Promise.promise();
		
		if(this.listChannelsId.size()==0) throw new Exception("ERRORE: Nessun canale considerato per la classificazione..");
		
		HashMap<Integer, Sample> samplesMap= new HashMap<Integer, Sample>();
		
		//Creo una lista di future per ogni canale preso in considetazione
		//In questo modo eseguiro' tutte le find sul mongo client
		List<Future> futures= new ArrayList<Future>();
		//Creiamo una lista di futureObject
		for(Integer i: this.listChannelsId)
			futures.add(this.getLastSampleFromMongo(i));
		
		//Creiamo una composizione con i future Object che ci dovranno ritornare
		//Usiamo join il che vuol dire che si attendera' la fine di tutti i metodi Future
		CompositeFuture tasks= CompositeFuture.join(futures).onComplete(res ->{
			if(res.succeeded()) {
				Logger.getLogger().print("Risultato: "+res.result().toString());
				resultPromise.complete(samplesMapResult);
			}
		});
		
		return resultPromise.future();
		
	}
	
	
	
	
	/*
	 * Metodo per il prelevamento dati dei Sample dal mongoDB 
	 * id e' l'id del canale da andare a prelevare da mongo
	 */
	private Future<Sample> getLastSampleFromMongo(int index) {
		
		Promise<Sample> resultPromise = Promise.promise();
		
		JsonObject q= new JsonObject();
		Logger.getLogger().print("DEBUG SensorDataController id prelevato: "+index);
		FindOptions fd = new FindOptions();
		//Opzione per fare il sorting del risultato in modo crescente??
		fd.setSort(new JsonObject().put("timestamp", 1));
    	//this.mongoClient.find("channel-"+index,q, res-> {
		this.mongoClient.findWithOptions("channel-"+index, q, fd, res-> {
    		/*
    		 * Successo nel trovare i sample nel db
    		 */
    		if(res.succeeded()) {
    			
    			Logger.getLogger().print("DEBUG risultato: "+res.result().toString());
    			
    			//Array per raccolta risultati, convertiamo l'array risultato in un ArrayList di Sample
				ArrayList<Sample> results;
				try {
					results = new ObjectMapper().readValue(res.result().toString(), new TypeReference<ArrayList<Sample>>(){});
					
					//Se il numero di sample e' pari a zero ignoriamo questo canale
					if(results.size()==0) throw new Exception("Numero di sample uguale a 0.");
					
					//Cerchiamo l'ultimo Sample prodotto per il canale selezionato
					Sample lastSample=null;
					for(int i=0; i<results.size(); i++) {
						if(lastSample==null )
							lastSample= results.get(i);
						else
							//Se il Sample letto e' piu' recente aggiorniamo lastSample
							if(lastSample.compareTo(results.get(i)) == -1 )
								lastSample = results.get(i);			
					}
					
					//Aggiungo l'ultimo Sample ai risultati
					this.samplesMapResult.put(index, lastSample);
					
					//Imposto promise con il risultato
					resultPromise.complete(lastSample);
					Logger.getLogger().print("promise impostata");
					
				} catch (JsonMappingException e1) {
					Logger.getLogger().print("Errore jsonMapping sample prelevato in getDataFromMongo");
					Logger.getLogger().print("Nessuna prelevazione per il channel "+index);
					e1.printStackTrace();
				} catch (JsonProcessingException e1) {
					Logger.getLogger().print("Errore jsonProcessing sample prelevato in getDataFromMongo");
					Logger.getLogger().print("Nessuna prelevazione per il channel "+index);
					e1.printStackTrace();
				}
				catch (Exception e1) {
					Logger.getLogger().print("Errore generico parsing Json in SensorDataController");
					Logger.getLogger().print("ERRORE: "+e1.getMessage());
					Logger.getLogger().print("Nessuna prelevazione per il channel "+index);
					
				}
				
    			
    		}
    		/*
    		 * Caso di fallimento
    		 */
    		else {
    			Logger.getLogger().print("DEBUG SensorDataController: Errore recupero del Sample da MongoDB");
    			resultPromise.fail("DEBUG SensorDataController: Errore recupero del Sample da MongoDB");
    		}
    	});
		
		return  resultPromise.future();
		
	}
	
	
	
}
