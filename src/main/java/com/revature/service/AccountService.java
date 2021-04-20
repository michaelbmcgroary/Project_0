package com.revature.service;

import java.util.ArrayList;
import java.util.List;

import com.revature.dao.*;
import com.revature.dto.PostAccountDTO;
import com.revature.exceptions.AccountAddException;
import com.revature.exceptions.AccountClientMismatchException;
import com.revature.exceptions.AccountNotFoundException;
import com.revature.exceptions.BadParameterException;
import com.revature.exceptions.ClientAlreadyExistsException;
import com.revature.exceptions.ClientNotFoundException;
import com.revature.exceptions.EmptyParameterException;
import com.revature.exceptions.couldNotConnectToDatabaseException;
import com.revature.model.*;

public class AccountService {

	private ClientRepository clientRepository;
	private AccountRepository accountRepository;
	
	public AccountService(ClientRepository clientRepository) throws couldNotConnectToDatabaseException {
		this.clientRepository = clientRepository;
		boolean created = false;
		accountRepository = new AccountRepository(clientRepository);
	}
	
	//Normally, we don't need this and would use the other one, but for testing, we need this to "inject" the mock object into this service
	public AccountService(ClientRepository clientRepository, AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}
	
	
	public String updateAccountBy2IDs(String clientID, String accountID, String amount) throws BadParameterException, EmptyParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		int errorCulprit = 1;
		try {
			if(clientID.isBlank() || accountID.isBlank() || amount.isBlank()) {
				throw new EmptyParameterException("When trying to update an account, one or more of the parameters were left blank.");
			}
			int cID = Integer.parseInt(clientID);
			errorCulprit++;
			int aID = Integer.parseInt(accountID);
			errorCulprit++;
			int amountInt = Integer.parseInt(amount);
			Account account = null;
			account = accountRepository.updateAccountVia2IDs(cID, aID, amountInt);
			if(account != null) {
				return "The Account, " + account.getAccountID() + ", has had their amount updated to " + account.getAmount();
			} else {
				throw new AccountNotFoundException("Account with ID " + accountID + " could not be found");
			}
		} catch (NumberFormatException e) {
			if(errorCulprit == 1) {
				throw new BadParameterException("Client ID must be int value. User provided " + clientID);
			} else if(errorCulprit == 2) {
				throw new BadParameterException("Account ID must be int value. User provided " + accountID);
			} else {
				throw new BadParameterException("Amount must be int value. User provided " + amount);
			}
		} catch (ClientNotFoundException e) {
			throw new ClientNotFoundException("The client with the ID " + clientID + " was not found");
		} catch (AccountClientMismatchException e) {
			throw new AccountClientMismatchException("The account asked for does not match the client asked for.");
		}
	}
	
	public String addAccount(PostAccountDTO account, String clientID) throws couldNotConnectToDatabaseException, ClientNotFoundException, BadParameterException, AccountAddException, EmptyParameterException {
		try {
			if(clientID.isBlank() || account.getAmount().isBlank()) {
				throw new EmptyParameterException("When trying to create an account, one of the parameters was left blank.");
			}
			
			int id = Integer.parseInt(clientID);
			int amount = Integer.parseInt(account.getAmount());
			Account retAccount = accountRepository.makeAccountForClient(amount, id);
			return "Account " + retAccount.getAccountID() + " with amount of " + retAccount.getAmount() + " for Client " + retAccount.getClientID() + " was added successfully.";
		} catch (NumberFormatException e) {
			throw new BadParameterException("Client ID and amount in the account must be int values. User provided " + clientID + " for client ID and " + account.getAmount() + " for amount in the account");
		}
	}
	
	public List<Account> getAccountsByClientID(String clientID) throws BadParameterException, AccountNotFoundException, couldNotConnectToDatabaseException, EmptyParameterException{
		try {
			if(clientID.isBlank()) {
				throw new EmptyParameterException("When trying to get all of the accounts of a specific client, the Client ID parameter was left blank.");
			}
			int cID = Integer.parseInt(clientID);
			List<Account> accountList = new ArrayList<Account>();
			accountList = accountRepository.getAccountsViaClientID(cID);
			if(accountList.size() == 0) {
				throw new AccountNotFoundException("No accounts could be found for this user");
			}
			return accountList;
		} catch (NumberFormatException e) {
			throw new BadParameterException("Client ID must be int value. User provided " + clientID);
		}
	}
	
	public Account getAccountBy2IDs(String clientID, String accountID) throws BadParameterException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, AccountNotFoundException, EmptyParameterException, AccountClientMismatchException {
		try {
			if(clientID.isBlank() || accountID.isBlank()) {
				throw new EmptyParameterException("When trying to get an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
			int cID = Integer.parseInt(clientID);
			int aID = Integer.parseInt(accountID);
			Account account = null;
			account = accountRepository.getAccountVia2IDs(cID, aID);
			if(account == null || account.getAccountID() == -1) {
				throw new AccountNotFoundException("Account with id of " + accountID + " and client ID of " + clientID + " was not found");
			}
			return account;
		} catch (NumberFormatException e) {
			throw new BadParameterException("Client and Account ID's must be int values. User provided " + clientID + " and " + accountID);
		} catch (AccountClientMismatchException e) {
			throw new AccountClientMismatchException("The account asked for does not match the client asked for.");
		}
	}
	
	public List<Account> getAccountsBetweenTwoValues(String clientID, String lowVal, String highVal) throws BadParameterException, AccountNotFoundException, couldNotConnectToDatabaseException, EmptyParameterException, ClientNotFoundException{
		int errorCulprit = 1;
		try {
			if(clientID.isBlank() || lowVal.isBlank() || highVal.isBlank()) {
				throw new EmptyParameterException("When trying to get accounts between certain amounts using the client ID, one or more of the parameters were left blank.");
			}
			int cID = Integer.parseInt(clientID);
			errorCulprit++;
			int betweenLow = Integer.parseInt(lowVal);
			errorCulprit++;
			int betweenHigh = Integer.parseInt(highVal);
			if(betweenLow >= betweenHigh) {
				throw new BadParameterException("The lower value parameter must be lower than the higher value parameter, the user input " + lowVal + " as higher than " + highVal);
			}
			List<Account> accountList = new ArrayList<Account>();
			accountList = accountRepository.getAccountsBetweenTwoValues(cID, betweenLow, betweenHigh);
			if(accountList.size() == 0) {
				throw new AccountNotFoundException("No accounts could be found for this user");
			}
			return accountList;
		} catch (NumberFormatException e) {
			if(errorCulprit == 1) {
				throw new BadParameterException("Client ID must be int value. User provided " + clientID);
			} else if(errorCulprit == 2) {
				throw new BadParameterException("Lower Value must be int value. User provided " + lowVal);
			} else {
				throw new BadParameterException("Higher Value must be int value. User provided " + highVal);
			}
		} catch (ClientNotFoundException e) {
			throw new ClientNotFoundException("The client with the ID " + clientID + " was not found");
		}
	}
	
	public String deleteAccount(String clientID, String accountID) throws BadParameterException, EmptyParameterException, AccountClientMismatchException, AccountNotFoundException, ClientNotFoundException {
		try {
			if(clientID.isBlank() || accountID.isBlank()) {
				throw new EmptyParameterException("When trying to delete an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
			int cID = Integer.parseInt(clientID);
			int aID = Integer.parseInt(accountID);
			Account account = null;
			account = accountRepository.deleteAccountVia2IDs(cID, aID);
			if(account != null) {
				return "Account " + account.getAccountID() + " with amount of " + account.getAmount() + " for Client " + account.getClientID() + " was deleted successfully.";
			}
		} catch (NumberFormatException e) {
			throw new BadParameterException("Client and Account ID's must be int values. User provided " + clientID + " and " + accountID);
		} catch (couldNotConnectToDatabaseException e) {
			e.printStackTrace();
		} catch (AccountNotFoundException e) {
			throw new AccountNotFoundException("User tried to delete account with ID " + accountID + " which does not exist");
		} catch (ClientNotFoundException e) {
			throw new ClientNotFoundException("User tried to delete account with client ID " + clientID + " which does not exist");
		} catch (AccountClientMismatchException e) {
			throw new AccountClientMismatchException("User tried to delete account, " + accountID + ", and gave the client ID, " + clientID + ", which the account does not belong to");
		}
		return "Account could not be deleted.";
	}
	
}

