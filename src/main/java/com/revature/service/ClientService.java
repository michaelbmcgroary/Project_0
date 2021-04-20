package com.revature.service;

import java.sql.SQLException;
import java.util.List;

import com.revature.dao.*;
import com.revature.dto.PostClientDTO;
import com.revature.exceptions.BadParameterException;
import com.revature.exceptions.ClientAddException;
import com.revature.exceptions.ClientAlreadyExistsException;
import com.revature.exceptions.ClientNotFoundException;
import com.revature.exceptions.EmptyParameterException;
import com.revature.exceptions.couldNotConnectToDatabaseException;
import com.revature.model.*;

public class ClientService {

	private ClientRepository clientRepository;
	
	public ClientService() throws couldNotConnectToDatabaseException {
		clientRepository = new ClientRepository();
	}
	
	//Normally, we don't need this and would use the other one, but for testing, we need this to "inject" the mock object into this service
	public ClientService(ClientRepository clientRepository) {
		this.clientRepository = clientRepository;
	}
	
	public Client getClientById(String stringId) throws BadParameterException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException {
		try {
			int id = Integer.parseInt(stringId);
			Client client = null;
			client = clientRepository.getClientById(id);
			if(client == null) {
				throw new ClientNotFoundException("Client with id of " + id + " was not found");
			}
			return client;
		} catch (NumberFormatException e) {
			throw new BadParameterException("Client id must be an int. User provided " + stringId);
		}
	}
	
	
	
	
	public List<Client> getAllClients() throws couldNotConnectToDatabaseException {
		return clientRepository.getAllClients();
	}
	
	
	
	
	public String addClient(PostClientDTO client) throws ClientAddException, EmptyParameterException {
		try {
			if(client.getFirstName().isBlank() || client.getLastName().isBlank()) {
				throw new EmptyParameterException("When trying to add a Client, one or more of the parameters were left blank.");
			} else {
				Client retClient = clientRepository.newClient(client);
				
				if(retClient != null && retClient.getId() != -1) {
					return "Client " + retClient.getFirstName() + " " + retClient.getLastName() + " with ID of " + retClient.getId() + " was added successfully.";
				} else {
					throw new ClientAddException("Something went wrong and the client could not be added.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (couldNotConnectToDatabaseException e) {
			e.printStackTrace();
		}
		return "Shouldn't trigger but the IDE yells at me if this isn't here";
		
	}
	
	
	public String addClientByID(Client client) {
		try {
			client = clientRepository.newClientByID(client.getId(), client.getFirstName(), client.getLastName());
			if(client != null) {
				return "The clientID, " + client.getId() + " and client name, " + client.getFirstName() + " " + client.getLastName() + ", has been added.";
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (couldNotConnectToDatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "User could not be added";
	}
	
	
	
	public String updateClientByID(PostClientDTO client, String clientID) throws EmptyParameterException, BadParameterException, SQLException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException {
		try {
			if(client.getFirstName().isBlank() || client.getLastName().isBlank()) {
				throw new EmptyParameterException("When trying to update a Client, one or more of the parameters were left blank.");
			}
			int id = Integer.parseInt(clientID);
			Client retClient;
			retClient = clientRepository.updateClient(id, client.getFirstName(), client.getLastName());
			if(retClient != null) {
				return "The clientID, " + retClient.getId() + " has had their name updated to " + retClient.getFirstName() + " " + retClient.getLastName();
			}
		} catch (NumberFormatException e) {
			throw new BadParameterException("Client id must be an int. User provided " + clientID);
		} catch (ClientNotFoundException e) {
			return e.getMessage();
		}
		return "User could not be updated";
	}
	
	public ClientRepository getRepo() {
		return clientRepository;
	}
	
}

