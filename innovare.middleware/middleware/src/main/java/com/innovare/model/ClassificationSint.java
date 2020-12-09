package com.innovare.model;

import java.time.LocalDate;

public class ClassificationSint {


	private Status status;
	private LocalDate date;
	private int percCarenza;
	private int percSane;
	private int percEccesso;

	public enum Status {
		CARENZA("Carenza",
				"Piante con carenza di acqua"), 
		NORMALE("Sane", "Piante sane"), 
		ECCESSO("Eccesso", "Piante con eccesso di acqua"),
		SCARTATE("Scartate","Immagini scartate");

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

	public ClassificationSint() {}

	public ClassificationSint(Status status, LocalDate date, int percCarenza, int percSane, int percEccesso) {
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

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setDate(LocalDate date) {
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

	@Override
	public String toString() {
		return "ClassificationSint [status=" + status + ", date=" + date + ", percCarenza=" + percCarenza
				+ ", percSane=" + percSane + ", percEccesso=" + percEccesso + "]";
	}
	
	

	
}