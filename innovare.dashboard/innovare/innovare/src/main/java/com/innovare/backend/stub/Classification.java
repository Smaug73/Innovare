package com.innovare.backend.stub;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.innovare.ui.utils.UIUtils;
import com.innovare.ui.utils.BadgeColor;

import java.time.LocalDate;

public class Classification {

	private Status status;
	private LocalDate date;
	private int percCarenza;
	private int percSane;
	private int percEccesso;

	public enum Status {
		CARENZA("Carenza",
				"Piante con carenza di acqua"), 
		NORMALE("Sane", "Piante sane"), 
		ECCESSO("Eccesso", "Piante con eccesso di acqua");

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

	

	public Classification(Status status, LocalDate date, int percCarenza, int percSane, int percEccesso) {
		super();
		this.status = status;
		this.date = date;
		this.percCarenza = percCarenza;
		this.percSane = percSane;
		this.percEccesso = percEccesso;
	}


	public String getStatus() {
		return status.name;
	}


	public LocalDate getDate() {
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

	
}
