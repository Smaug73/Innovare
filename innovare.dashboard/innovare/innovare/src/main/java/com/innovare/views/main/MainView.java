package com.innovare.views.main;


import static com.vaadin.flow.server.VaadinSession.getCurrent;
import static java.util.Optional.ofNullable;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import com.innovare.views.main.MainView;
import com.innovare.views.login.LoginView;

/**
 * The main view is a top-level placeholder for other views.
 */
@JsModule("./styles/shared-styles.js")
//@Theme(value = Lumo.class, variant = Lumo.DARK)
@CssImport("./styles/views/main/main-view.css")
//@Route("")
//@PWA(name = "Innovare", shortName = "Innovare", enableInstallPrompt = false)
/*@PWA(name = "Innovare", shortName = "Innovare",
	startPath = "login",
	backgroundColor = "#227aef", themeColor = "#227aef",
	offlinePath = "offline-page.html",
	offlineResources = {"images/offline-login-banner.jpg"},
	enableInstallPrompt = false)*/
public class MainView extends AppLayout implements RouterLayout, BeforeEnterObserver{

    //private final Tabs menu;
    //private H1 viewTitle;

    public MainView() {
        /*setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        menu = createMenu();
        addToDrawer(createDrawerContent(menu));*/
    	//System.out.println("Sono in MainView");
    	//this.display();
    }

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		String path = event.getLocation().getPath();

		Boolean isAuthenticated = ofNullable((Boolean)getCurrent().getAttribute(LoginView.ATTRIBUTE_IS_AUTH))
                .orElse(Boolean.FALSE);

		 if (!isAuthenticated && !path.contains("login")) {
			      event.forwardTo(LoginView.class);
		 }
		
	}
    
    /*private void display ( )
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
    	setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();
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
				e.printStackTrace();
			} catch (IOException e) {
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
       /* } );
        return component;
    }*/

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
