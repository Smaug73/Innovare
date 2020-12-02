package com.innovare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.vaadin.artur.helpers.LaunchUtil;

import com.vaadin.ui.UI;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
    	SpringApplicationBuilder builder = new SpringApplicationBuilder(Application.class);
    	builder.headless(false);

    	builder.run(args);
    	if(UI.getCurrent() != null) System.out.println("UI in application diverso da null");
        else System.out.println("UI in application uguale a null");
    }

}
