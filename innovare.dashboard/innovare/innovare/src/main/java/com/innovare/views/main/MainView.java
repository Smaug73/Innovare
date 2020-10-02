package com.innovare.views.main;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.Theme;
import com.innovare.views.main.MainView;
import com.innovare.views.home.HomeView;
import com.innovare.views.storico.StoricoView;
import com.innovare.views.login.LoginView;
import com.innovare.views.innovare.InnovareView;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * The main view is a top-level placeholder for other views.
 */
//@JsModule("./styles/shared-styles.js")
//@Theme(value = Lumo.class, variant = Lumo.DARK)
//@CssImport("./styles/views/main/main-view.css")
@Route("")
@PWA(name = "Innovare", shortName = "Innovare", enableInstallPrompt = false)
public class MainView extends VerticalLayout {

    //private final Tabs menu;
    //private H1 viewTitle;

    public MainView() {
        /*setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        menu = createMenu();
        addToDrawer(createDrawerContent(menu));*/
    	System.out.println("Sono in MainView");
    	this.display();
    }
    
    private void display ( )
    {
        if ( Objects.isNull( VaadinSession.getCurrent().getAttribute( User.class ) ) )
        {
            this.removeAll();
            this.add( this.makeLoginForm() );
        } else
        { // Else we have a User, so must be authenticated.
            this.removeAll();
            this.add( new ContentView() );
        }
    }

    private LoginForm makeLoginForm ( )
    {
        Authenticator authenticator = new Authenticator();
        LoginForm component = new LoginForm();
        component.addLoginListener( ( AbstractLogin.LoginEvent loginEvent ) -> {
        	URIBuilder builder = new URIBuilder();
        	builder.setScheme("http").setHost("localhost:8888").setPath("/login")
        		.setParameter("username", loginEvent.getUsername())
        		.setParameter("password", loginEvent.getPassword());
        	
        	URI uri = null;
        	try {
				uri = builder.build();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	HttpGet get = new HttpGet(uri);
        	CloseableHttpClient client = HttpClients.createDefault();
        	
        	try(CloseableHttpResponse response = client.execute(get)){
        		String status = response.getStatusLine().toString();
        		System.out.println(status);
        		if(status.contains("200")) {
        			System.out.println("OK!");
        			Optional < User > user = authenticator.authenticate( loginEvent.getUsername() , loginEvent.getPassword() );
        			VaadinSession.getCurrent().setAttribute( User.class , user.get() );
                    this.display();
        		}else
                {
        			System.out.println("Errore");
                    component.setError( true );
                }
        	} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Errore");
			}
        	
        	
            /*Optional < User > user = authenticator.authenticate( loginEvent.getUsername() , loginEvent.getPassword() );
            if ( user.isPresent() )
            {
                VaadinSession.getCurrent().setAttribute( User.class , user.get() );
                this.display();
            } else
            {
                component.setError( true );
            }*/
        } );
        return component;
    }

    /*private Component createHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setId("header");
        layout.getThemeList().set("dark", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(new DrawerToggle());
        viewTitle = new H1();
        layout.add(viewTitle);
        layout.add(new Image("images/user.svg", "Avatar"));
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
        logoLayout.add(new Image("images/logo.png", "Innovare logo"));
        logoLayout.add(new H1("Innovare"));
        layout.add(logoLayout, menu);
        return layout;
    }

    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        tabs.add(createMenuItems());
        return tabs;
    }

    private Component[] createMenuItems() {
        return new Tab[] {
            createTab("Home", HomeView.class),
            createTab("Storico", StoricoView.class),
            createTab("Login", LoginView.class),
            createTab("Innovare", InnovareView.class)
        };
    }

    private static Tab createTab(String text, Class<? extends Component> navigationTarget) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        return tab;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        getTabForComponent(getContent()).ifPresent(menu::setSelectedTab);
        viewTitle.setText(getCurrentPageTitle());
    }

    private Optional<Tab> getTabForComponent(Component component) {
        return menu.getChildren()
                .filter(tab -> ComponentUtil.getData(tab, Class.class)
                        .equals(component.getClass()))
                .findFirst().map(Tab.class::cast);
    }

    private String getCurrentPageTitle() {
        return getContent().getClass().getAnnotation(PageTitle.class).value();
    }*/
}
