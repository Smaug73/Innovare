package com.innovare.views.main;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinSession;
import com.innovare.views.home.HomeView;
import com.innovare.views.storico.ClassificazioniView;
import com.innovare.views.storico.TempAmbView;
import com.innovare.views.storico.HumAmbView;
import com.innovare.views.storico.InfoSuoloView;
import com.innovare.views.storico.UVView;
import com.innovare.views.storico.IrrigazioneView;
import com.innovare.views.storico.RainView;
import com.innovare.views.storico.SolarRadiationView;
import com.innovare.views.storico.WindView;
import com.innovare.model.Role;
import com.innovare.views.configurazione.ConfigurazioneView;
import com.innovare.views.innovare.InnovareView;

@Route(value = "content", layout = MainView.class)
public class ContentView extends MainView{

	private H1 viewTitle;
	private NaviMenu naviMenu;

	public ContentView ( ){

		setPrimarySection(Section.DRAWER);
		addToNavbar(true, createHeaderContent());
		naviMenu = new NaviMenu();
		initNaviItems();
		addToDrawer(createDrawerContent());
	}

	private Component createHeaderContent() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setId("header");
		layout.getThemeList().set("dark", true);
		layout.setWidthFull();
		layout.setSpacing(false);
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		layout.add(new DrawerToggle());
		viewTitle = new H1();
		layout.add(viewTitle);
		return layout;
	}

	private Component createDrawerContent() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setPadding(false);
		layout.setSpacing(false);
		layout.getThemeList().set("spacing-s", true);
		layout.setAlignItems(FlexComponent.Alignment.STRETCH);
		HorizontalLayout logoLayout = new HorizontalLayout();
		logoLayout.setId("logo");
		logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		logoLayout.add(new Image("images/icona_innovare.png", "InnovareProject logo"));
		logoLayout.add(new H1("INNOVARE"));
		layout.add(logoLayout, naviMenu);
		return layout;
	}


	private void initNaviItems() {
		naviMenu.addNaviItem(VaadinIcon.HOME_O, "Home", HomeView.class);

		NaviItem storico = naviMenu.addNaviItem(VaadinIcon.ARCHIVES, "Storico", null);
		naviMenu.addNaviItem(storico, "Classificazioni", ClassificazioniView.class);
		naviMenu.addNaviItem(storico, "Irrigazioni", IrrigazioneView.class);
		naviMenu.addNaviItem(storico, "Temperatura Ambientale", TempAmbView.class);
		naviMenu.addNaviItem(storico, "Umidità Ambientale", HumAmbView.class);
		naviMenu.addNaviItem(storico, "Piogge", RainView.class);
		naviMenu.addNaviItem(storico, "Venti", WindView.class);
		naviMenu.addNaviItem(storico, "Informazioni Suolo", InfoSuoloView.class);
		naviMenu.addNaviItem(storico, "Livelli UV", UVView.class);
		naviMenu.addNaviItem(storico, "Radiazione Solare", SolarRadiationView.class);

		naviMenu.addNaviItem(VaadinIcon.QUESTION_CIRCLE_O, "About", InnovareView.class);

		VaadinSession vs = VaadinSession.getCurrent();
		String role = null;
		if(vs != null) {
			role = (String) vs.getAttribute("role");
		}
		if (role != null && role.equals(Role.ADMIN)) {
			naviMenu.addNaviItem(VaadinIcon.COG_O, "Configurazione",ConfigurazioneView.class);
		}

		final String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
		NaviItem logout = new NaviItem(VaadinIcon.ARROW_CIRCLE_RIGHT_O, "Logout", null);
		naviMenu.add(createLogoutLink(contextPath, logout));

	}


	private static Anchor createLogoutLink(String contextPath, NaviItem item) {
		final Anchor a = populateLink(new Anchor(), item);
		a.setHref(contextPath + "/login");
		return a;
	}


	private static <T extends HasComponents> T populateLink(T a, NaviItem item) {
		a.add(item);
		return a;
	}

	@Override
	protected void afterNavigation() {
		super.afterNavigation();
		viewTitle.setText(getCurrentPageTitle());
	}

	private String getCurrentPageTitle() {
		return getContent().getClass().getAnnotation(PageTitle.class).value();
	}
}
