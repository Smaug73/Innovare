package com.innovare.model;

import java.util.ArrayList;

public class Evapotranspiration {
	
	public static final double eliofania =2.71;//  (6.5/24) 
	public static double calculate(double temperatura,double umidRel,ArrayList<Sample> temperature,double pressione,double vento) {
		//La temperatura passata in questa formuale e' in centrigradi quindi viene convertita a kelvin
		//convertiamo il vento da mph ad ms(metri al secondo) nella formula
		return  ( 0.408*delta(temperatura)*( Rn(umidRel,temperatura)-G()  ) +  psicrometricaCost(pressione,temperatura)*( 900/(temperatura+273.15))*(vento*0.44704)*(eaed(umidRel,temperatura)) )/
				(  delta(temperatura)+ psicrometricaCost(pressione,temperatura)*(1+0.34*(vento*0.44704) )  );
	}
	
	
	public static double ea(double temperatura) {
		return 0.6108*Math.exp((17.27*temperatura)/(temperatura+237.3));
	}
	
	
	public static double eaed(double umidRel,double temperatura) {
		double ea=0.6108*Math.exp((17.27*temperatura)/(temperatura+237.3));
		double ed = ea*(umidRel/100);
		return ea-ed;
	}
	
	//tensione di vapore effettiva
	//Si calcola partendo dall'umidita' relativa ed ea
	public static double ed(double umidRel,double ea) {
		return ea*(umidRel/100);
	}
	
	//delta
	public static double delta(double temperatura) {
		return ( 4098 * ( 0.6108*Math.exp((temperatura*17.27)/(temperatura+237.3))) ) / (Math.pow((temperatura+237.3),2));
	}
	
	//Radiazione netta??
	public static double Rn(double umidRel,double temperature) {
		return ( 0.1+(0.9*eliofania))*(0.34-(0.14*Math.sqrt(ed(umidRel,ea(temperature)))))*(4.903*Math.pow(10, -9)*Math.pow(temperature, 4));
			
	}
	
	//Flusso di calore G chiedere
	public static double G() {
		return 0; // riferito su calcolo giornaliero
	}
	
	public static double psicrometricaCost(double pressione,double temperatura) {
		return 0.00163*(pressione/Evapotranspiration.calLatVapori(temperatura));
		
	}
	public static double calLatVapori(double temperatura) {
		 return 2.501-( 2.361*0.001 )*temperatura;
	}
	
	
	//Attenzione temperature medie riferito a quale instante temporale
	public static double temperaturaMedia(ArrayList<Sample> temperature) {
		float media=0;
		for(Sample d: temperature)
			media=media+d.getMisure();
		return media/temperature.size();
	}
	
	
	
}
