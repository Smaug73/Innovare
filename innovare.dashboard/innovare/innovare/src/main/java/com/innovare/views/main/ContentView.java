package com.innovare.views.main;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.applayout.AppLayout.Section;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabVariant;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.Theme;
import com.innovare.views.home.HomeView;
import com.innovare.views.storico.StoricoView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.innovare.utils.SecurityUtils;
import com.innovare.views.configurazione.ConfigurazioneView;
import com.innovare.views.innovare.InnovareView;

@Route(value = "content", layout = MainView.class)
public class ContentView extends MainView{
	
	private H1 viewTitle;
	private final Tabs menu;
	
	public ContentView ( ){

		setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        menu = createMenu();
        addToDrawer(createDrawerContent(menu));
			 
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

    private Component createDrawerContent(Tabs menu) {
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
        layout.add(logoLayout, menu);
        return layout;
    }
    
    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_SMALL);
        tabs.setId("tabs");
        tabs.add(createMenuItems());
        return tabs;
    }

	private Tab[] createMenuItems() {
		final List<Tab> tabs = new ArrayList<>(4);
		 tabs.add(createTab(VaadinIcon.HOME, "Home", HomeView.class));
		 tabs.add(createTab(VaadinIcon.ARCHIVE, "Storico", StoricoView.class));
		 tabs.add(createTab(VaadinIcon.QUESTION, "About", InnovareView.class));
		 VaadinSession vs = VaadinSession.getCurrent();
		 String username = null;
		 if(vs != null) {
			 username = (String) vs.getAttribute("username");
		 }
		 if (username != null && username.contains("admin")) {
			 tabs.add(createTab(VaadinIcon.COG, "Configurazione",ConfigurazioneView.class));
		 }
		 final String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
		 final Tab logoutTab = createTab(createLogoutLink(contextPath));
		 tabs.add(logoutTab);
		 
		 return tabs.toArray(new Tab[tabs.size()]);
	}

	private static Anchor createLogoutLink(String contextPath) {
			final Anchor a = populateLink(new Anchor(), VaadinIcon.ARROW_RIGHT, "Logout");
			a.setHref(contextPath + "/login");
			return a;
	 }
	 
	 private static Tab createTab(VaadinIcon icon, String title, Class<? extends Component> viewClass) {
			return createTab(populateLink(new RouterLink(null, viewClass), icon, title));
	 }
	 
	 private static Tab createTab(Component content) {
			final Tab tab = new Tab();
			tab.add(content);
			return tab;
	 }
	 
	 private static <T extends HasComponents> T populateLink(T a, VaadinIcon icon, String title) {
			a.add(icon.create());
			a.add(title);
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
