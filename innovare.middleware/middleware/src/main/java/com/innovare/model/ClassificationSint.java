package com.innovare.model;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import com.innovare.control.ConfigurationController;
import com.innovare.model.Status;
import com.innovare.utils.ClassificationException;
import com.innovare.utils.Utilities;

public class ClassificationSint implements Comparable<ClassificationSint>{


	private Status status;
	private Timestamp date;
	private int percCarenza;
	private int percSane;
	private int percEccesso;
	private int percScartate;
	private int percInfestanti;
	private String model;
	private String maxPercForIrrigation;
	private String _id;
	private Timestamp creationData;
	




	/*public enum Status {
		CARENZA("CARENZA",
				"Piante con carenza di acqua"), 
		NORMALE("NORMALE", "Piante sane"), 
		ECCESSO("ECCESSO", "Piante con eccesso di acqua"),
		SCARTATE("SCARTATE","Immagini scartate"),
		INFESTANTI("INFESTANTI","Piante infestanti");
		private String name;
		private String desc;

		Status(String name, String desc) {
			this.name = name;
			this.desc = desc;
		}

		public String getName() {
			return name;
		}

		public String getDesc() {
			return desc;
		}
	}*/

	public ClassificationSint() {}

	
	
	
	public ClassificationSint(Status status, Timestamp date, int percCarenza, int percSane, int percEccesso,
			int percScartate, int percInfestanti, String model) {
		super();
		this.status = status;
		this.date = date;
		this.percCarenza = percCarenza;
		this.percSane = percSane;
		this.percEccesso = percEccesso;
		this.percScartate = percScartate;
		this.percInfestanti = percInfestanti;
		this.model = model;
		this.creationData= new Timestamp(System.currentTimeMillis());
	}
	
	



	public ClassificationSint(Status status, Timestamp date, int percCarenza, int percSane, int percEccesso,
			int percScartate, int percInfestanti, String model, String _id) {
		super();
		this.status = status;
		this.date = date;
		this.percCarenza = percCarenza;
		this.percSane = percSane;
		this.percEccesso = percEccesso;
		this.percScartate = percScartate;
		this.percInfestanti = percInfestanti;
		this.model = model;
		this._id = _id;
		this.creationData= new Timestamp(System.currentTimeMillis());
	}

	



	public ClassificationSint(Status status, Timestamp date, int percCarenza, int percSane, int percEccesso,
			int percScartate, int percInfestanti, String model, String _id, Timestamp creationData) {
		super();
		this.status = status;
		this.date = date;
		this.percCarenza = percCarenza;
		this.percSane = percSane;
		this.percEccesso = percEccesso;
		this.percScartate = percScartate;
		this.percInfestanti = percInfestanti;
		this.model = model;
		this._id = _id;
		this.creationData = creationData;
	}




	public ClassificationSint(ArrayList<PlantClassification> classifications) throws ClassificationException {
		this._id= this.RandomId();
		
		this.creationData= new Timestamp(System.currentTimeMillis());
		
		//Variabili conteggio delle varie tipologie di immagini 
		int carenza=0;
		int eccesso=0;
		int sane=0;
		int infestanti=0; 	//da ignorare
		int ambigue=0;		//da ignorare
		for(PlantClassification p: classifications){
			ArrayList<Result> risultati=p.getClassification().getClassifications();
			//Prendiamo il risultato della classificazione
			double resultSana=0;
			double resultEccesso=0;
			double resultCarenza=0;
			double resultInfestanti=0;
			double resultAmbigue=0;
			for(Result r: risultati) {
				switch(r.getClasse()) {
					case Utilities.classeAmbigue: 
						resultAmbigue=r.getScore();
						break;
					case Utilities.classeCarenza:
						resultCarenza=r.getScore();
						break;
					case Utilities.classeEccesso:
						resultEccesso= r.getScore();
						break;
					case Utilities.classeInfestanti:
						resultInfestanti=r.getScore();
						break;
					case Utilities.classeSane:
						resultSana=r.getScore();
						break;			
				}		
			}
			//Presi i risultati dobbiamo decidere lo stato della pianta basandoci sulle soglie
			//Per ogni immagine aggiungiamo al relativo contatore a quale classe appartiene
			if(resultSana >= Utilities.sogliaClassificazione ) {
				sane+=1;
			}
			else if(resultEccesso >= Utilities.sogliaClassificazione ) {
				eccesso+=1;
			}
			else if(resultCarenza >= Utilities.sogliaClassificazione )	{
				carenza+=1;
			}
			else if(resultInfestanti >= Utilities.sogliaClassificazione ) {	
				infestanti+=1;
			}
			else { 
				ambigue+=1;
			}
		}
		//System.out.println(sane+" "+eccesso+" "+carenza+" "+infestanti+" "+ambigue);
		//Calcoliamo le percentuali dal numero di piante analizzate
		ArrayList<Integer> percentuali= new ArrayList<Integer>();
		this.percCarenza= (carenza/classifications.size())*100;
		percentuali.add(this.percCarenza);
		this.percEccesso= (eccesso/classifications.size())*100;
		percentuali.add(this.percEccesso);
		//this.percInfestanti= (infestanti/classifications.size())*100;  	IGNORIAMO QUESTA CLASSE
		//percentuali.add(this.percInfestanti);
		this.percSane= (sane/classifications.size())*100;
		percentuali.add(this.percSane);
		//this.percScartate= (ambigue/classifications.size())*100;		IGNORIAMO QUESTA CLASSE
		//percentuali.add(this.percScartate);
		
		//Confrontiamo le percentuali per scegliere quel stato ritorna la classificazione.
		//In base alla posizione restituita scegliamo lo stato
		int position= this.findMax(percentuali);
		System.out.println("Position: "+position);
		switch(position) {
			case 0:
				this.status=Status.CARENZA;
				//Verifichiamo se la percentuale massima supera la soglia altrimenti scartiamo la classificazione
				if(this.percCarenza<=ConfigurationController.sogliaclassificazione)
					//Scartiamo classification
					throw new ClassificationException("Soglia non superata");
				break;
			case 1:
				this.status=Status.ECCESSO;
				//Verifichiamo se la percentuale massima supera la soglia altrimenti scartiamo la classificazione
				if(this.percEccesso<=ConfigurationController.sogliaclassificazione)
					//Scartiamo classification
					throw new ClassificationException("Soglia non superata");
				break;
			//case 2:
				//this.status=Status.INFESTANTI;
				//break;
			case 2:
				this.status=Status.NORMALE;
				//Verifichiamo se la percentuale massima supera la soglia altrimenti scartiamo la classificazione
				if(this.percSane<=ConfigurationController.sogliaclassificazione)
					//Scartiamo classification
					throw new ClassificationException("Soglia non superata");
				break;
			//case 4:
				//this.status=Status.SCARTATE;
				//break;
		}
		
		
		
		
		//Dopo aver scelto lo stato e le percentuali impostiamo il resto 
		//Per il modello basta prendere il primo elemento che sicuramente è presente nella lista
		this.model= classifications.get(0).getModel();
		//Data
		SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
	    Date parsedDate;
		try {
			parsedDate = dateFormat.parse(classifications.get(0).getDate());
			this.date = new java.sql.Timestamp(parsedDate.getTime());
		} catch (ParseException e) {
			System.err.println("ERRORE CREAZIONE DATE");
			e.printStackTrace();
			this.date= new Timestamp(0l);
		}
	    
		
	}
	
	private String RandomId() {
		byte[] randArr= new byte[4];
		new Random().nextBytes(randArr);
		String randStr = new String(randArr,Charset.forName("UTF-8"));
		return String.valueOf(System.currentTimeMillis()+randStr);
	}

	


	public Status getStatus() {
		return this.status;
	}

	public Timestamp getDate() {
		return date;
	}

	public int getPercCarenza() {
		return percCarenza;
	}

	public int getPercSane() {
		return percSane;
	}

	public int getPercEccesso() {
		return percEccesso;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	
	public void setDate(Timestamp date) {
		this.date = date;
	}

	public void setPercCarenza(int percCarenza) {
		this.percCarenza = percCarenza;
	}

	public void setPercSane(int percSane) {
		this.percSane = percSane;
	}

	public void setPercEccesso(int percEccesso) {
		this.percEccesso = percEccesso;
	}

	public int getPercScartate() {
		return percScartate;
	}

	public void setPercScartate(int percScartate) {
		this.percScartate = percScartate;
	}

	public int getPercInfestanti() {
		return percInfestanti;
	}

	public void setPercInfestanti(int percInfestanti) {
		this.percInfestanti = percInfestanti;
	}
	
	

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}
	
	public String get_id() {
		return _id;
	}




	public void set_id(String _id) {
		this._id = _id;
	}


	public Timestamp getCreationData() {
		return creationData;
	}




	public void setCreationData(Timestamp creationData) {
		this.creationData = creationData;
	}




	@Override
	public String toString() {
		return "ClassificationSint [status=" + status + ", date=" + date + ", percCarenza=" + percCarenza
				+ ", percSane=" + percSane + ", percEccesso=" + percEccesso + ", percScartate=" + percScartate
				+ ", percInfestanti=" + percInfestanti + ", model=" + model + ", _id=" + _id + ", creationData="
				+ creationData + "]";
	}




	public int findMax(ArrayList<Integer> valori) {
		//Cerchiamo percentuali delle quali una deve avere per forza un valore maggiore di zero
		//Non cercheremo mai valori negativi
		//Restituiamo la posizione del massimo
		int max=0;
		for(int i=1; i<valori.size(); i++) {
			if(valori.get(i)>valori.get(max))
				max=i;
		}
		return max;
	}

	public Status getMaxPercForIrrigation() {
		/*
		 * Verificare se infestanti e percentuali scartate sono di interesse per l'irrigazione
		 */
		int[] perc= {this.percCarenza,this.percEccesso,this.percInfestanti,this.percSane,this.percScartate};
		int max=0;
		
		for(int i=1;i<perc.length;i++) {
			if(perc[i]>perc[max])
				max=i;
		}
		
		switch(max) {
		case 0:
			return Status.CARENZA;
		case 1:
			return Status.ECCESSO;
		case 2:
			return Status.INFESTANTI;
		case 3:
			return Status.NORMALE;
		case 4:
			return Status.SCARTATE;
		default:
			System.err.println("Errore valutazione percentuale classificazione, sara' considerato lo status del campo come normale.");
			return Status.NORMALE;
		}
		
		
	}
	


	@Override
	public int compareTo(ClassificationSint arg0) {
		 return  this.getDate().compareTo(arg0.getDate());
	}
	

	
}