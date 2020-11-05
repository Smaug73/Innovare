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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinSession;
import com.innovare.views.home.HomeView;
import com.innovare.views.storico.StoricoView;

import java.util.ArrayList;
import java.util.List;

import com.innovare.ui.utils.Bottom;
import com.innovare.ui.utils.FlexBoxLayout;
import com.innovare.ui.utils.FontSize;
import com.innovare.ui.utils.Horizontal;
import com.innovare.ui.utils.IconSize;
import com.innovare.ui.utils.Right;
import com.innovare.ui.utils.TextColor;
import com.innovare.ui.utils.UIUtils;
import com.innovare.utils.Role;
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
		 tabs.add(createTab(VaadinIcon.HOME_O, "Home", HomeView.class));
		 tabs.add(createTab(VaadinIcon.ARCHIVES, "Storico", StoricoView.class));
		 tabs.add(createTab(VaadinIcon.QUESTION_CIRCLE_O, "About", InnovareView.class));
		 VaadinSession vs = VaadinSession.getCurrent();
		 String role = null;
		 if(vs != null) {
			 role = (String) vs.getAttribute("role");
		 }
		 if (role != null && role.equals(Role.ADMIN)) {
			 tabs.add(createTab(VaadinIcon.COG_O, "Configurazione",ConfigurazioneView.class));
		 }
		 final String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
		 final Tab logoutTab = createTab(createLogoutLink(contextPath));
		 tabs.add(logoutTab);
		 
		 return tabs.toArray(new Tab[tabs.size()]);
	}

	private static Anchor createLogoutLink(String contextPath) {
			final Anchor a = populateLink(new Anchor(), VaadinIcon.ARROW_CIRCLE_RIGHT_O, "Logout");
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
			a.add(createContent(icon, title));
			//a.add(title);
			return a;
	 }
	 
	 private static FlexBoxLayout createContent(VaadinIcon icon, String title) {
			FlexBoxLayout header = new FlexBoxLayout(
					UIUtils.createIcon(IconSize.M, TextColor.BODY, icon),
					UIUtils.createLabel(FontSize.M, title));
			header.setAlignItems(FlexComponent.Alignment.CENTER);
			header.setSpacing(Right.L);
			return header;
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
