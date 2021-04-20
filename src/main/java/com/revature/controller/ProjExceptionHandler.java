package com.revature.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revature.dto.MessageDTO;
import com.revature.exceptions.*;

import io.javalin.Javalin;
import io.javalin.http.ExceptionHandler;

public class ProjExceptionHandler implements Controller {
		
	private Logger logger = LoggerFactory.getLogger(ProjExceptionHandler.class);
	
	//Exception Handler
	private ExceptionHandler<BadParameterException> badParameterExceptionHandler = (e, ctx) -> {
		logger.warn("A user passed a function with a bad parameter. Exception message is: " + e.getMessage());
		ctx.status(400);
		ctx.json(new MessageDTO(e.getMessage()));
	};
	
	private ExceptionHandler<EmptyParameterException> emptyParameterExceptionHandler = (e, ctx) -> {
		logger.warn("A user entered empty values in parameters. Exception message is: " + e.getMessage());
		ctx.status(400);
		ctx.json(new MessageDTO(e.getMessage()));
	};
	
	private ExceptionHandler<ClientNotFoundException> clientNotFoundExceptionHandler = (e, ctx) -> {
		logger.warn("A user tried to retrieve a client, but it was not found. Exception message is: " + e.getMessage());
		ctx.status(404);
		ctx.json(new MessageDTO(e.getMessage()));
	};
	
	private ExceptionHandler<AccountNotFoundException> accountNotFoundExceptionHandler = (e, ctx) -> {
		logger.warn("A user tried to retrieve an account, but it was not found. Exception message is: " + e.getMessage());
		ctx.status(404);
		ctx.json(new MessageDTO(e.getMessage()));
	};
	
	private ExceptionHandler<couldNotConnectToDatabaseException> couldNotConnectToDatabaseExceptionHandler = (e, ctx) -> {
		logger.error("The connection to MariaDB could not be completed. Exception message is: " + e.getMessage());
		ctx.status(500);
		ctx.json(new MessageDTO(e.getMessage()));
	};
	
	private ExceptionHandler<ClientAlreadyExistsException> clientAlreadyExistsExceptionHandler = (e, ctx) -> {
		logger.warn("A user tried to create a new ClientID, but the ID already exists. Exception message is: " + e.getMessage());
		ctx.status(400);
		ctx.json(new MessageDTO(e.getMessage()));
	};
	
	private ExceptionHandler<AccountAddException> accountAddExceptionHandler = (e, ctx) -> {
		logger.warn("A user tried to add an account, but it failed. Exception message is: " + e.getMessage());
		ctx.status(400);
		ctx.json(new MessageDTO(e.getMessage()));
	};
	
	private ExceptionHandler<ClientAddException> clientAddExceptionHandler = (e, ctx) -> {
		logger.warn("A user tried to add an client, but it failed. Exception message is: " + e.getMessage());
		ctx.status(400);
		ctx.json(new MessageDTO(e.getMessage()));
	};
	
	private ExceptionHandler<AccountClientMismatchException> accountClientMismatchExceptionHandler = (e, ctx) -> {
		logger.warn("A user tried to see the details of an account, but the account didn't belong to the client the User requested. Exception message is: " + e.getMessage());
		ctx.status(401);
		ctx.json(new MessageDTO(e.getMessage()));
	};
	
	
	//add in exceptions for if missing parameters for new clients/accounts
		
	public void mapEndpoints(Javalin app) {
		app.exception(BadParameterException.class, badParameterExceptionHandler);
		app.exception(ClientNotFoundException.class, clientNotFoundExceptionHandler);
		app.exception(AccountNotFoundException.class, accountNotFoundExceptionHandler);
		app.exception(ClientAlreadyExistsException.class, clientAlreadyExistsExceptionHandler);
		app.exception(couldNotConnectToDatabaseException.class, couldNotConnectToDatabaseExceptionHandler);
		app.exception(AccountAddException.class, accountAddExceptionHandler);
		app.exception(ClientAddException.class, clientAddExceptionHandler);
		app.exception(EmptyParameterException.class, emptyParameterExceptionHandler);
		app.exception(AccountClientMismatchException.class, accountClientMismatchExceptionHandler);
	}
	
	
}
