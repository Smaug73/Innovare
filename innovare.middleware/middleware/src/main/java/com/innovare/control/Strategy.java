package com.innovare.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.innovare.model.ClassificationSint;
import com.innovare.model.Irrigazione;

public abstract class Strategy {

	/*
	 * La strategia prevede un metodo per la generazione della previosione utilizzando i dati prelevati dal gateway
	 */
	

	/*
	 * Metodo che decide se irrigare
	 * Va definito
	 */
	public abstract Irrigazione createIrrigation(ClassificationSint cs) throws Exception;
	
}
