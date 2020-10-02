package com.innovare.middleware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.innovare.model.Role;
import com.innovare.model.User;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;

public class MainVerticle extends AbstractVerticle {

	
	/*
	 * IMPORTANTE:Il tipo password utilizzato per i json per rendere sicure le password non è supportato e se utilizzato 
	 * 			  manda in crash il programma.
	 * 			
	 * 			Le get del login in fase di test hanno generato una risposta strana da parte del server che rispondeva
	 * 			correttamente con codice di ritorno 200 ma generava anche un successivo codice 500 internal server error.
	 * 			
	 */
	
	
	final List<JsonObject> configuration = new ArrayList<>(Arrays.asList(
		    new JsonObject().put("Gateway", "prova").put("Model", "prova")	    
		  ));
	
	private User userLog;
	/*
	 * Usare jackson per fare mapping java / json
	 * 
	 * 
	 * 
	 * 
	 * Aggiungere end point login che prende oggetto Utente{nome,password}
	 * 
	 */
	
	
	private HttpServer server;
	
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
	  
	  /*  
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    }).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
    */
	  
    OpenAPI3RouterFactory.create(this.vertx, "resources/InnovareMiddleware.yaml", ar -> {
    	  if (ar.succeeded()) {
    		  
    		//Router per la richiesta della configuratone attuale  
    	    OpenAPI3RouterFactory routerFactory = ar.result(); // (1)
    	    routerFactory.addHandlerByOperationId("actualConfiguration", routingContext ->
    	    	routingContext
    	    	 .response()
    	    	 .setStatusCode(200)
    	    	 .putHeader(HttpHeaders.CONTENT_TYPE, "applivation/json")
    	    	 .end(new JsonArray(getAllConfiguration()).encode())
    	    		);
    	    
    	    //Login     
    	    routerFactory.addHandlerByOperationId("login", routingContext ->{
    	    	String username= routingContext.request().getParam("username").toString();
    	    	String password= routingContext.request().getParam("password").toString();
    	    	System.out.print(username+" "+password);
    	    	/*RequestParameters params = routingContext.get("parsedParameters"); // (1)
    	    	String username = params.pathParameter("username").toString();
    	    	String password = params.pathParameter("password").toString();*/
    	    	
    	    	routingContext
   	    	 	.response()
   	    	 	.setStatusCode(200)
   	    	 	.end("Conferma login effettuato");
    	    		
    	    	/*
    	    	 * Dalla dashboard dobbiamo capire se si sta cercando di entrare come admin o user. 
    	    	 * Da definire i dati comunicati dalla dashboard, questo è solo un test.
    	    	 * 
    	    	 *
    	    	 * 
    	    	 * 
    	    	try {
					userLog= User.convertJsonToUser(userJson.toString());	//variabile dove si salva l'utente loggato correttamente
					routingContext
	    	    	 .response()
	    	    	 .setStatusCode(200)
	    	    	 .end("Conferma login effettu");
				} catch (JsonMappingException e1) {
					// TODO Auto-generated catch block
					System.out.println("Errore");
					e1.printStackTrace();
				} catch (JsonProcessingException e1) {
					// TODO Auto-generated catch block
					System.out.println("Errore");
					e1.printStackTrace();
				}
    	    	/*
    	    	 * Aggiungere la verifica sul database.
    	    	 */  	    	
    	    });
    	  
    	 
    	    
    	    
    	    Router router = routerFactory.getRouter(); // <1>
            router.errorHandler(404, routingContext -> { // <2>
              JsonObject errorObject = new JsonObject() // <3>
                .put("code", 404)
                .put("message",
                  (routingContext.failure() != null) ?
                    routingContext.failure().getMessage() :
                    "Not Found"
                );
              routingContext
                .response()
                .setStatusCode(404)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(errorObject.encode()); // <4>
            });
            router.errorHandler(400, routingContext -> {
              JsonObject errorObject = new JsonObject()
                .put("code", 400)
                .put("message",
                  (routingContext.failure() != null) ?
                    routingContext.failure().getMessage() :
                    "Validation Exception"
                );
              routingContext
                .response()
                .setStatusCode(400)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(errorObject.encode());
            });
    	    
            server = vertx.createHttpServer(new HttpServerOptions().setPort(8888).setHost("localhost")); // <5>
            server.requestHandler(router).listen(); // <6>
            
            // end::routerGen[]
            startPromise.complete(); // Complete the verticle start
    	    
    	  } else {
			// Something went wrong during router factory initialization
    	    //future.fail(ar.cause()); // (2)
    		 System.out.println("Errore creazione verticle");
    		 startPromise.fail(ar.cause());
    	  }
    	});
    
   
    
  }
  
  
  public static void main(String[] args) {
	    Vertx vertx = Vertx.vertx();
	    vertx.deployVerticle(new MainVerticle());
  }
  
  
  
 
  
  public JsonObject getConfigurazione(){
	  return null;
  }
  

  private List<JsonObject> getAllConfiguration() {
    return this.configuration;
  }
  
  
}
