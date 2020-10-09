package com.innovare.views.main;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabVariant;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServlet;
import com.innovare.views.home.HomeView;
import com.innovare.views.storico.StoricoView;
import com.innovare.utils.SecurityUtils;
import com.innovare.views.configurazione.ConfigurazioneView;
import com.innovare.views.innovare.InnovareView;

@Route(value = "content", layout = MainView.class)
public class ContentView extends MainView{
	 public ContentView ( ){
		 Tabs tabs = new Tabs();
		 tabs.add(createTab(VaadinIcon.HOME, "Home", HomeView.class));
		 tabs.add(createTab(VaadinIcon.ARCHIVE, "Storico", StoricoView.class));
		 tabs.add(createTab(VaadinIcon.QUESTION, "About", InnovareView.class));
		 //if (SecurityUtils.isAccessGranted(ConfigurazioneView.class)) {
			 tabs.add(createTab(VaadinIcon.COG, "Configurazione",ConfigurazioneView.class));
		 //}
		 final String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
		 final Tab logoutTab = createTab(createLogoutLink(contextPath));
		 tabs.add(logoutTab);
		 add(tabs);
	 }
	 
	 private static Anchor createLogoutLink(String contextPath) {
			final Anchor a = populateLink(new Anchor(), VaadinIcon.ARROW_RIGHT, "Logout");
			a.setHref(contextPath + "/logout");
			return a;
	 }
	 
	 private static Tab createTab(VaadinIcon icon, String title, Class<? extends Component> viewClass) {
			return createTab(populateLink(new RouterLink(null, viewClass), icon, title));
	 }
	 
	 private static Tab createTab(Component content) {
			final Tab tab = new Tab();
			tab.addThemeVariants(TabVariant.LUMO_ICON_ON_TOP);
			tab.add(content);
			return tab;
	 }
	 
	 private static <T extends HasComponents> T populateLink(T a, VaadinIcon icon, String title) {
			a.add(icon.create());
			a.add(title);
			return a;
	 }

}
