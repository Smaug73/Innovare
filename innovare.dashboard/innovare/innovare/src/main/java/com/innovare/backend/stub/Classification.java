package com.innovare.backend.stub;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.innovare.ui.utils.UIUtils;
import com.innovare.ui.utils.BadgeColor;

import java.time.LocalDate;

public class Classification {

	private Status status;
	private LocalDate date;

	public enum Status {
		CARENZA(VaadinIcon.CLOCK, "Carenza",
				"Piante con carenza di acqua",
				BadgeColor.CONTRAST), NORMALE(VaadinIcon.QUESTION_CIRCLE,
				"Sane", "Piante sane",
				BadgeColor.NORMAL), ECCESSO(VaadinIcon.CHECK,
				"Eccesso", "Piante con eccesso di acqua",
				BadgeColor.ERROR);

		private VaadinIcon icon;
		private String name;
		private String desc;
		private BadgeColor theme;

		Status(VaadinIcon icon, String name, String desc, BadgeColor theme) {
			this.icon = icon;
			this.name = name;
			this.desc = desc;
			this.theme = theme;
		}

		public Icon getIcon() {
			Icon icon;
			switch (this) {
				case CARENZA:
					icon = UIUtils.createSecondaryIcon(this.icon);
					break;
				case NORMALE:
					icon = UIUtils.createPrimaryIcon(this.icon);
					break;
				case ECCESSO:
					icon = UIUtils.createSuccessIcon(this.icon);
					break;
				default:
					icon = UIUtils.createErrorIcon(this.icon);
					break;
			}
			return icon;
		}

		public String getName() {
			return name;
		}

		public String getDesc() {
			return desc;
		}

		public BadgeColor getTheme() {
			return theme;
		}
	}

	public Classification(Status status, LocalDate date) {
		this.status = status;
		this.date = date;
	}

	public Status getStatus() {
		return status;
	}


	public LocalDate getDate() {
		return date;
	}
}
