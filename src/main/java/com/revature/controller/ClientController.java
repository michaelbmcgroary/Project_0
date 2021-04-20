package com.revature.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revature.exceptions.couldNotConnectToDatabaseException;
import com.revature.model.Client;
import com.revature.service.ClientService;
import com.revature.dao.ClientRepository;
import com.revature.dto.PostClientDTO;

import io.javalin.Javalin;
import io.javalin.http.Handler;

public class ClientController implements Controller {

	private Logger logger = LoggerFactory.getLogger(ClientController.class);
	private ClientService clientService;

	public ClientController() throws couldNotConnectToDatabaseException {
		this.clientService = new ClientService();
	}

	
	private Handler getClientByID = ctx -> {
		String id = ctx.pathParam("id");
		Client client = clientService.getClientById(id);
		ctx.json(client); // Serialize the data, but may update this
		ctx.status(200); // 200 means successful
	};

	
	private Handler getAllClients = ctx -> {
		ArrayList<Client> clientList = new ArrayList<Client>();
		clientList = (ArrayList<Client>) clientService.getAllClients();
		ctx.json(clientList);
		ctx.status(200);
	};

	
	private Handler postNewClient = ctx -> {
		PostClientDTO client = ctx.bodyAsClass(PostClientDTO.class);
		String outMessage = clientService.addClient(client);
		ctx.json(outMessage);
		ctx.status(201); 
	};

	
	private Handler updateClientByID = ctx -> {
		String id = ctx.pathParam("id");
		PostClientDTO client = ctx.bodyAsClass(PostClientDTO.class);
		String outMessage = clientService.updateClientByID(client, id);
		ctx.json(outMessage);
		ctx.status(202);
	};
	
	public ClientRepository getRepo() {
		return clientService.getRepo();
	}

	
	@Override
	public void mapEndpoints(Javalin app) {
		// TODO Auto-generated method stub
		app.get("/clients", getAllClients);
		app.get("/clients/:id", getClientByID);
		app.post("/clients", postNewClient);
		app.put("/clients/:id", updateClientByID);
	}

}
