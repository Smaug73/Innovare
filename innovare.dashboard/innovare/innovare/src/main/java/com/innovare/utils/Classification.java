package com.innovare.utils;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.innovare.ui.utils.UIUtils;
import com.innovare.ui.utils.BadgeColor;

import java.sql.Timestamp;
import java.time.LocalDate;

public class Classification {

	private Status status;
	private Timestamp date;
	private int percCarenza;
	private int percSane;
	private int percEccesso;
	private int percScartate;
	private int percInfestanti;
	private String model;

	

	public enum Status {
		CARENZA("Carenza",
				"Piante con carenza di acqua"), 
		NORMALE("Sane", "Piante sane"), 
		ECCESSO("Eccesso", "Piante con eccesso di acqua"),
		SCARTATE("Scartate", "Immagini scartate"),
		INFESTANTI("Infestanti", "Piante infestanti");

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
	}

	

	public Classification() {}
	
	public Classification(Status status, Timestamp date, int percCarenza, int percSane, 
			int percEccesso, int percScartate, int percInfestanti, String model) {
		super();
		this.status = status;
		this.date = date;
		this.percCarenza = percCarenza;
		this.percSane = percSane;
		this.percEccesso = percEccesso;
		this.percScartate = percScartate;
		this.percInfestanti = percInfestanti;
		this.model = model;
	}


	public String getStatus() {
		return status.name;
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

	public void setPercScartate(int percScartate) {
		this.percScartate = percScartate;
	}

	public int getPercScartate() {
		return percScartate;
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

	@Override
	public String toString() {
		return "Classification\n[status=" + status + ",\ndate=" + date + ",\npercCarenza=" + percCarenza + ",\npercSane="
				+ percSane + ",\npercEccesso=" + percEccesso + ",\npercScartate=" + percScartate + ",\npercInfestanti="
				+ percInfestanti + ",\nmodel=" + model + "]";
	}


	
}
