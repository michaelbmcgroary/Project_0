package com.revature.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revature.controller.ProjExceptionHandler;
import com.revature.dto.PostClientDTO;
import com.revature.exceptions.ClientAddException;
import com.revature.exceptions.ClientAlreadyExistsException;
import com.revature.exceptions.ClientNotFoundException;
import com.revature.exceptions.couldNotConnectToDatabaseException;
import com.revature.model.Client;
import com.revature.util.ConnectionUtil;

public class ClientRepository {
	
	private Logger logger = LoggerFactory.getLogger(ProjExceptionHandler.class);
	
	
	
	public ClientRepository() throws couldNotConnectToDatabaseException{
		//Creates the SQL tables to hold the clients and maybe their accounts, but I may also create an accountRepository
		try(Connection connection = ConnectionUtil.getConnection()){
			String closeAccountTable = "DROP TABLE IF EXISTS accountTbl";
			//Want to start fresh with each instance, but I need to check to make sure that there are no foreign key dependencies
			String closeClientTable = "DROP TABLE IF EXISTS clientTbl";
			String createClientTable = "CREATE TABLE clientTbl (clientID INT NOT NULL AUTO_INCREMENT, clientFirstName VARCHAR(100) NOT NULL, clientLastName VARCHAR(100) NOT NULL, PRIMARY KEY(clientID));";
			String populateClientTable1 = "INSERT INTO clientTbl (clientFirstName, clientLastName) VALUES ('George', 'Lucas')";
			String populateClientTable2 = "INSERT INTO clientTbl (clientFirstName, clientLastName) VALUES ('Johnny', 'Depp')";
			String populateClientTable3 = "INSERT INTO clientTbl (clientFirstName, clientLastName) VALUES ('Owen', 'Wilson')";
			String populateClientTable4 = "INSERT INTO clientTbl (clientFirstName, clientLastName) VALUES ('Nicholas', 'Cage')";
			Statement statement = connection.createStatement();
			statement.addBatch(closeAccountTable);
			statement.addBatch(closeClientTable);
			statement.addBatch(createClientTable);
			statement.addBatch(populateClientTable1);
			statement.addBatch(populateClientTable2);
			statement.addBatch(populateClientTable3);
			statement.addBatch(populateClientTable4);
			statement.executeBatch();
			//Tried to include some confirmation that it worked, but tested it and it does and if something goes wrong, the try/catch will throw exception
			logger.info("Created the clientTbl in the Client Repository");
		} catch (SQLException e) {
			throw new couldNotConnectToDatabaseException("An error occured with the SQL syntax");
		} 
		
	}
	
	public void closeRepository() throws SQLException {
		//add in stuff about clearing data
	}
	
	public Client getClientById(int id) throws couldNotConnectToDatabaseException, ClientNotFoundException, ClientAlreadyExistsException {
		Client client = null;
		try(Connection connection = ConnectionUtil.getConnection()){
			String sql = "SELECT * FROM clientTbl WHERE clientID = " + id;
			Statement statement = connection.createStatement();
			ResultSet results = statement.executeQuery(sql);
			
			if(results.next()) {
				int clientID = results.getInt("clientID");
				String firstName = results.getString("clientFirstName");
				String lastName = results.getString("clientLastName");
				client = new Client(clientID, firstName, lastName);
			}
		} catch (SQLException e) {
			throw new couldNotConnectToDatabaseException("Couldn't connect to database");
		}
		return client;
	}
	
	
	public List<Client> getAllClients() throws couldNotConnectToDatabaseException {
		
		ArrayList<Client> listOfClients = new ArrayList<Client>();
		//System.out.println(connection);
		try(Connection connection = ConnectionUtil.getConnection()){
			String sql = "SELECT * FROM clientTbl";
			Statement statement = connection.createStatement();
			
			ResultSet results = statement.executeQuery(sql);
			
			while(results.next()) {
				int clientID = results.getInt("clientID");
				String firstName = results.getString("clientFirstName");
				String lastName = results.getString("clientLastName");
				Client client = new Client(clientID, firstName, lastName);
				listOfClients.add(client);
			}
		} catch (SQLException e) {
			throw new couldNotConnectToDatabaseException("Couldn't connect to database");
		}
		return listOfClients;
	}
	
	
	public Client newClient(PostClientDTO client) throws SQLException, couldNotConnectToDatabaseException, ClientAddException {
		//creates the new client while auto incrementing the ID
		try(Connection connection = ConnectionUtil.getConnection()){
			String prepStatement = "INSERT INTO clientTbl (clientFirstName, clientLastName) VALUES (?, ?)";
			PreparedStatement statement = connection.prepareStatement(prepStatement);
			statement.setString(1, client.getFirstName());
			statement.setString(2, client.getLastName());
			int rows = statement.executeUpdate();
			
			if(rows != 0) {
				String queryStatementString = "SELECT * FROM clientTbl WHERE clientFirstName = '" + client.getFirstName() + "' AND clientLastName = '" + client.getLastName() + "';";
				Statement queryStatement = connection.createStatement();
				ResultSet results = queryStatement.executeQuery(queryStatementString);
				int id = -1;
				//running a while loop to deal with the case where two clients happen to have the same first and last name so it
				//outputs the correct id rather than the first, but returns a client with an id of -1 if something goes wrong
				while(results.next()) {
					id = results.getInt("clientID");
				} 
				
				return new Client(id, client.getFirstName(), client.getLastName());
			} else {
				throw new ClientAddException("Something went wrong and the client could not be added.");
			}
		} catch (SQLException e) {
			throw new couldNotConnectToDatabaseException("An error occured with the SQL syntax");
		}
	}
	
	
	public Client newClientByID(int clientID, String firstName, String lastName) throws SQLException, ClientAlreadyExistsException, couldNotConnectToDatabaseException {
		//creates the new client with a specific ID
		try(Connection connection = ConnectionUtil.getConnection()){
			String prepStatement = "INSERT INTO clientTbl (clientID, clientFirstName, clientLastName) VALUES (?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(prepStatement);
			
			statement.setInt(1, clientID);
			statement.setString(2, firstName);
			statement.setString(3, lastName);
			int rows = statement.executeUpdate();
			System.out.println("test");
			if(rows >0) {
				return new Client(clientID, firstName, lastName);
			} else {
				return null;
			}
		} catch(SQLIntegrityConstraintViolationException e) {
			throw new ClientAlreadyExistsException("Client with ID " + clientID + " already exists");
		} catch (SQLException e) {
			throw new couldNotConnectToDatabaseException("An error occured with the SQL syntax");
		} 
	}
	
	
	public Client updateClient(int id, String newFirstName, String newLastName) throws SQLException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, ClientNotFoundException {
		//updates the first and last name of a user by their ID
		try(Connection connection = ConnectionUtil.getConnection()){
			Statement statement = connection.createStatement();
			String firstNameStatement = "UPDATE clientTbl SET clientFirstName = '" + newFirstName + "' WHERE clientID = " + id;
			String lasNameStatement = "UPDATE clientTbl SET clientLastName = '" + newLastName + "' WHERE clientID = " + id;
			int rows = statement.executeUpdate(firstNameStatement);
			rows += statement.executeUpdate(lasNameStatement);
			if(rows >0) {
				return new Client(id, newFirstName, newLastName);
			} else {
				return null;
			}
		} catch(SQLIntegrityConstraintViolationException e) {
			throw new ClientNotFoundException("Client with ID " + id + " does not exist");
		} catch (SQLException e) {
			throw new couldNotConnectToDatabaseException("Couldn't connect to database");
		}
		
	}

	
}
