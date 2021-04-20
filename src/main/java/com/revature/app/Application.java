package com.revature.app;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revature.controller.*;

import io.javalin.Javalin;
import com.revature.exceptions.*;


public class Application {

	private static Javalin app;
	private static Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) throws couldNotConnectToDatabaseException, ClientNotFoundException, ClientAlreadyExistsException, SQLException {
		
		app = Javalin.create();

		app.before(ctx -> {
			String URI = ctx.req.getRequestURI();
			String httpMethod = ctx.req.getMethod();
			logger.info(httpMethod + "request to endpoint " + URI + " recieved");
		});

		ClientController clientController = new ClientController();
		//The reason this is done this way is so that the Account Controller gets made after
		//any control of the clients because the client table must exist for the account
		//table to work
		mapControllers(clientController, new ProjExceptionHandler(), new AccountController(clientController.getRepo()));

		app.start(7000);
		
	}

	
	
	public static void mapControllers(Controller... controllers) {
		for (int i = 0; i < controllers.length; i++) {
			controllers[i].mapEndpoints(app);
		}
	}
	
}
