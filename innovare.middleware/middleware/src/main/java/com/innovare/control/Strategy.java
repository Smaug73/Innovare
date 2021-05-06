package com.innovare.control;

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
	public abstract Irrigazione strategy(ClassificationSint cs);
	
}
