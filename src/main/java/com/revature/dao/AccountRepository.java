package com.revature.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revature.controller.ProjExceptionHandler;
import com.revature.exceptions.AccountClientMismatchException;
import com.revature.exceptions.AccountNotFoundException;
import com.revature.exceptions.ClientNotFoundException;
import com.revature.exceptions.couldNotConnectToDatabaseException;
import com.revature.model.Account;
import com.revature.util.ConnectionUtil;

public class AccountRepository {
	
	private Logger logger = LoggerFactory.getLogger(ProjExceptionHandler.class);
	
	public AccountRepository(ClientRepository clientRepository) throws couldNotConnectToDatabaseException {
		//Only reason I'm asking for the clientRepository is to confirm it exists, may remove later
		if(clientRepository != null) {
			try(Connection connection = ConnectionUtil.getConnection()){			
				String createAccountTable = "CREATE TABLE accountTbl (accountID INT NOT NULL AUTO_INCREMENT, clientID INT NOT NULL, amount INT, PRIMARY KEY(accountID), FOREIGN KEY (clientID) REFERENCES clientTbl(clientID));";
				Statement statement = connection.createStatement();

				String populateClientTable1 = "INSERT INTO accountTbl (clientID, amount) VALUES (1, 500)";
				String populateClientTable2 = "INSERT INTO accountTbl (clientID, amount) VALUES (1, 5000)";
				String populateClientTable3 = "INSERT INTO accountTbl (clientID, amount) VALUES (1, 1500)";
				String populateClientTable4 = "INSERT INTO accountTbl (clientID, amount) VALUES (1, 2000)";
				String populateClientTable5 = "INSERT INTO accountTbl (clientID, amount) VALUES (2, 4000)";
				String populateClientTable6 = "INSERT INTO accountTbl (clientID, amount) VALUES (2, 2500)";
				String populateClientTable7 = "INSERT INTO accountTbl (clientID, amount) VALUES (3, 3000)";
				statement.addBatch(createAccountTable);
				statement.addBatch(populateClientTable1);
				statement.addBatch(populateClientTable2);
				statement.addBatch(populateClientTable3);
				statement.addBatch(populateClientTable4);
				statement.addBatch(populateClientTable5);
				statement.addBatch(populateClientTable6);
				statement.addBatch(populateClientTable7);
				statement.executeBatch();
				logger.info("Created the accountTbl in the Account Repository");
			} catch (SQLException e) {
				throw new couldNotConnectToDatabaseException("An error occured with the SQL syntax");
			} 
		} else {
			throw new couldNotConnectToDatabaseException("The Account Repository was created before the Client Repository");
			
		}
	}
	
	public Account makeAccountForClient(int amount, int clientID) throws couldNotConnectToDatabaseException, ClientNotFoundException {
		try (Connection connection = ConnectionUtil.getConnection()) {
			//I'm safe to use this since I checked and made sure the client
			String checkIfClientExists = "SELECT * FROM clientTbl WHERE clientID = " + clientID + ";";
			Statement statement = connection.createStatement();
			ResultSet results = statement.executeQuery(checkIfClientExists);
			if(!results.next()) {
				throw new ClientNotFoundException("Tried to make an account for client with ID " + clientID + "but client couldn't be found");
			}
			
			String makeAccount = "INSERT INTO accountTbl(clientID, amount) VALUES (?,?);";
			PreparedStatement prepStatement = connection.prepareStatement(makeAccount, Statement.RETURN_GENERATED_KEYS);
			prepStatement.setInt(1, clientID);
			prepStatement.setInt(2, amount);
			int rows = prepStatement.executeUpdate();
			if(rows >0) {
				results = prepStatement.getGeneratedKeys();
				if(results.next()) {
					return new Account(results.getInt("accountID"), clientID, results.getInt("amount"));
				} else {
					//Add in some kind of exception handling here
					throw new couldNotConnectToDatabaseException("Something happened with the database and the account could not be made despite the client existing");
				}
			}
		} catch (SQLException e) {
			throw new couldNotConnectToDatabaseException("Something happened with the database. Exception message is: " + e.getMessage());
		}
		throw new couldNotConnectToDatabaseException("Something happened with the database and the account could not be made despite the client existing");
	}
	
	public List<Account> getAccountsViaClientID (int clientID) throws couldNotConnectToDatabaseException, AccountNotFoundException {
		try (Connection connection = ConnectionUtil.getConnection()) {
			ArrayList<Account> accountList = new ArrayList<Account>();
			String getAccount = "SELECT * FROM accountTbl WHERE clientID = ?";
			PreparedStatement prepStatement = connection.prepareStatement(getAccount);
			prepStatement.setInt(1, clientID);
			ResultSet results = prepStatement.executeQuery();
			
			int numResults =  0;
			int cID, aID, amount;
			while(results.next()) {
				numResults++;
				cID = results.getInt("clientID");
				aID = results.getInt("accountID");
				amount = results.getInt("amount");
				accountList.add(new Account(aID, cID, amount));
			}
			if(numResults !=0) {
				return accountList;
			}
		
		} catch (SQLException e) {
			throw new couldNotConnectToDatabaseException("Something happened with the database. Exception message is: " + e.getMessage());
		}
		throw new AccountNotFoundException("No accounts for client id " + clientID + " were found.");
	}
	
	public Account getAccountVia2IDs (int clientID, int accountID) throws couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException {
		try (Connection connection = ConnectionUtil.getConnection()) {
			String getAccount = "SELECT * FROM accountTbl WHERE accountID = ?";
			PreparedStatement prepStatement = connection.prepareStatement(getAccount);
			prepStatement.setInt(1, accountID);
			ResultSet results = prepStatement.executeQuery();			
			if (results.next()) {
				int cID = results.getInt("clientID");
				if(cID != clientID) {
					throw new AccountClientMismatchException();
				} else {
					int amount = results.getInt("amount");
					return new Account(accountID, cID, amount);
				}
			}
		
		} catch (SQLException e) {
			throw new couldNotConnectToDatabaseException("Something happened with the database. Exception message is: " + e.getMessage());
		}
		return new Account(-1, -1, -1);
	}
	
	
	public List<Account> getAccountsBetweenTwoValues (int clientID, int betweenLow, int betweenHigh) throws couldNotConnectToDatabaseException, AccountNotFoundException, ClientNotFoundException{
		try (Connection connection = ConnectionUtil.getConnection()) {
			String checkClient = "SELECT * FROM accountTbl WHERE clientID = ?";
			PreparedStatement prepStatement = connection.prepareStatement(checkClient);
			prepStatement.setInt(1, clientID);
			ResultSet results = prepStatement.executeQuery();
			if(results.next()) {
				ArrayList<Account> accountsList = new ArrayList<Account>();			
				String getAccounts = "SELECT * FROM accountTbl WHERE clientID = ? AND amount BETWEEN ? AND ?;";
				prepStatement = connection.prepareStatement(getAccounts);
				prepStatement.setInt(1, clientID);
				prepStatement.setInt(2, betweenLow);
				prepStatement.setInt(3, betweenHigh);
				results = prepStatement.executeQuery();
				int numResults =  0;
				int cID, aID, amount;
				while(results.next()) {
					numResults++;
					cID = results.getInt("clientID");
					aID = results.getInt("accountID");
					amount = results.getInt("amount");
					accountsList.add(new Account(aID, cID, amount));
				}
				if(numResults !=0) {
					return accountsList;
				}
			} else {
				throw new ClientNotFoundException();
			}
		} catch (SQLException e) {
			throw new couldNotConnectToDatabaseException("Something happened with the database. Exception message is: " + e.getMessage());
		}
		throw new AccountNotFoundException("No accounts with amounts between 400 and 2000 were found.");
	}
	
	
	public Account updateAccountVia2IDs (int clientID, int accountID, int newAmount) throws couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try (Connection connection = ConnectionUtil.getConnection()) {
			String checkClient = "SELECT * FROM accountTbl WHERE clientID = ?";
			PreparedStatement prepStatement = connection.prepareStatement(checkClient);
			prepStatement.setInt(1, clientID);
			ResultSet results = prepStatement.executeQuery();
			if(results.next()) {
				Account account;
				try {
					account = getAccountVia2IDs(clientID, accountID);
				} catch (AccountClientMismatchException e) {
					throw new AccountClientMismatchException();
				}
				if (account.getAccountID() == -1){
					throw new AccountNotFoundException ("User tried to update an account that didn't exist");
				}
				
				String update = "UPDATE accountTbl SET amount = ? WHERE clientID = ? AND accountID = ?";
				prepStatement = connection.prepareStatement(update);
				prepStatement.setInt(1, newAmount);
				prepStatement.setInt(2, clientID);
				prepStatement.setInt(3, accountID);
				int rows = prepStatement.executeUpdate();
				if(rows != 0) {
					return new Account(accountID, clientID, newAmount);
				} else {
					throw new couldNotConnectToDatabaseException("Something happened with the database, the Account exists, but it could not be deleted");
				}
			} else {
				throw new ClientNotFoundException();
			}
			
		} catch (SQLException e) {
			throw new couldNotConnectToDatabaseException("Something happened with the database. Exception message is: " + e.getMessage());
		}
	}
	
	
	public Account deleteAccountVia2IDs (int clientID, int accountID) throws couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try (Connection connection = ConnectionUtil.getConnection()) {
			String checkClient = "SELECT * FROM accountTbl WHERE clientID = ?";
			PreparedStatement prepStatement = connection.prepareStatement(checkClient);
			prepStatement.setInt(1, clientID);
			ResultSet results = prepStatement.executeQuery();
			if(results.next()) {
				Account account;
				try {
					account = getAccountVia2IDs(clientID, accountID);
				} catch (AccountClientMismatchException e) {
					throw new AccountClientMismatchException();
				}
				if (account.getAccountID() == -1){
					throw new AccountNotFoundException ();
				}
				
				String delete = "DELETE FROM accountTbl WHERE accountID = " + accountID + " AND clientID = " + clientID + ";";
				Statement statement = connection.createStatement();
				int rows = statement.executeUpdate(delete);
				if(rows != 0) {
					return account;
				} else {
					throw new couldNotConnectToDatabaseException("Something happened with the database, the Account exists, but it could not be deleted");
				}
			} else {
				throw new ClientNotFoundException();
			}
			
		} catch (SQLException e) {
			throw new couldNotConnectToDatabaseException("Something happened with the database. Exception message is: " + e.getMessage());
		}
	}
}
