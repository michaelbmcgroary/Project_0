package com.revature.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.revature.dao.AccountRepository;
import com.revature.dao.ClientRepository;
import com.revature.dto.PostAccountDTO;
import com.revature.exceptions.AccountAddException;
import com.revature.exceptions.AccountClientMismatchException;
import com.revature.exceptions.AccountNotFoundException;
import com.revature.exceptions.AddAccountException;
import com.revature.exceptions.BadParameterException;
import com.revature.exceptions.ClientAlreadyExistsException;
import com.revature.exceptions.ClientNotFoundException;
import com.revature.exceptions.EmptyParameterException;
import com.revature.exceptions.couldNotConnectToDatabaseException;
import com.revature.model.Account;
import com.revature.util.ConnectionUtil;

public class AccountServiceTest {

	// AccountRepository relies on the creation of the account repository, so both are mocked
	private static ClientRepository mockClientRepository;
	private static AccountRepository mockAccountRepository;
	private static Connection mockConnection;
	
	// The system under test, our AccountService instance
	private AccountService accountService;
	
	@BeforeClass
	public static void setUp() throws couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		mockClientRepository = mock(ClientRepository.class);
		mockAccountRepository = mock(AccountRepository.class);
		
		when(mockAccountRepository.getAccountVia2IDs(eq(1), eq(1)))
				.thenReturn(new Account(1, 1, 2000));
		
		when(mockAccountRepository.getAccountVia2IDs(eq(2), eq(3)))
				.thenThrow(new AccountClientMismatchException());
		
		when(mockAccountRepository.makeAccountForClient(eq(500), eq(1)))
				.thenReturn(new Account(1, 1, 500));
		
		when(mockAccountRepository.updateAccountVia2IDs(eq(1), eq(1), eq(500)))
				.thenReturn(new Account(1, 1, 500));
		
		when(mockAccountRepository.updateAccountVia2IDs(eq(2), eq(1), eq(500)))
				.thenThrow(new ClientNotFoundException("The client with the ID 2 was not found"));
		
		when(mockAccountRepository.updateAccountVia2IDs(eq(2), eq(3), eq(500)))
				.thenThrow(new AccountClientMismatchException());
		
		when(mockAccountRepository.deleteAccountVia2IDs(eq(1), eq(1)))
				.thenReturn(new Account(1, 1, 500));
		
		when(mockAccountRepository.deleteAccountVia2IDs(eq(2), eq(3)))
				.thenThrow(new AccountClientMismatchException());
		
		when(mockAccountRepository.deleteAccountVia2IDs(eq(3), eq(4)))
				.thenThrow(new AccountNotFoundException());
		
		when(mockAccountRepository.deleteAccountVia2IDs(eq(4), eq(4)))
				.thenThrow(new ClientNotFoundException());
		
		ArrayList<Account> fullList = new ArrayList<Account>(3);
		fullList.add(new Account(1, 1, 1000));
		fullList.add(new Account(2, 1, 5000));
		fullList.add(new Account(3, 2, 3000));
		when(mockAccountRepository.getAccountsViaClientID(eq(1)))
				.thenReturn(fullList);
		
		when(mockAccountRepository.getAccountsBetweenTwoValues(eq(1), eq(500), eq(6000)))
				.thenReturn(fullList);
		
		when(mockAccountRepository.getAccountsBetweenTwoValues(eq(1), eq(500), eq(900)))
				.thenThrow(new AccountNotFoundException("No accounts with amounts between 400 and 2000 were found."));
		
		when(mockAccountRepository.getAccountsBetweenTwoValues(eq(2), eq(500), eq(900)))
				.thenThrow(new ClientNotFoundException("The client with the ID 2 was not found"));
	}
	
	@Before
	public void beforeTest() {
		accountService = new AccountService(mockClientRepository, mockAccountRepository); 
	}
	
	@Test
	public void test_makeAccountForClient_NoIssue() throws couldNotConnectToDatabaseException, ClientNotFoundException, BadParameterException, AccountAddException, EmptyParameterException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			String actual = accountService.addAccount(new PostAccountDTO("500"), "1");
			String expected = "Account 1 with amount of 500 for Client 1 was added successfully.";
			assertEquals(expected, actual);
		}
	}
	
	@Test
	public void test_makeAccountForClient_NonIntegerClientID() throws couldNotConnectToDatabaseException, ClientNotFoundException, AccountAddException, EmptyParameterException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.addAccount(new PostAccountDTO("500"), "abc");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client ID and amount in the account must be int values. User provided abc for client ID and 500 for amount in the account");
			}
		}
	}
	

	@Test
	public void test_makeAccountForClient_NonIntegerAmount() throws couldNotConnectToDatabaseException, ClientNotFoundException, AccountAddException, EmptyParameterException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.addAccount(new PostAccountDTO("abc"), "1");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client ID and amount in the account must be int values. User provided 1 for client ID and abc for amount in the account");
			}
			
		}
	}
	
	@Test
	public void test_makeAccountForClient_BlankClientIDParam_NoSpaces() throws couldNotConnectToDatabaseException, ClientNotFoundException, AccountAddException, EmptyParameterException, BadParameterException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.addAccount(new PostAccountDTO("500"), "");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to create an account, one of the parameters was left blank.");
			}
		}
	}
	
	@Test
	public void test_makeAccountForClient_BlankClientIDParam_WithSpaces() throws couldNotConnectToDatabaseException, ClientNotFoundException, AccountAddException, EmptyParameterException, BadParameterException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.addAccount(new PostAccountDTO("500"), "      ");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to create an account, one of the parameters was left blank.");
			}
		}
	}
	
	@Test
	public void test_makeAccountForClient_BlankAmountParam_NoSpaces() throws couldNotConnectToDatabaseException, ClientNotFoundException, AccountAddException, EmptyParameterException, BadParameterException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.addAccount(new PostAccountDTO(""), "1");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to create an account, one of the parameters was left blank.");
			}
		}
	}
	
	@Test
	public void test_makeAccountForClient_BlankAmountParam_WithSpaces() throws couldNotConnectToDatabaseException, ClientNotFoundException, AccountAddException, EmptyParameterException, BadParameterException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.addAccount(new PostAccountDTO("   "), "1");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to create an account, one of the parameters was left blank.");
			}
		}
	}
	
	@Test
	public void test_makeAccountForClient_BlankParameters_NoSpaces() throws couldNotConnectToDatabaseException, ClientNotFoundException, AccountAddException, EmptyParameterException, BadParameterException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.addAccount(new PostAccountDTO(""), "");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to create an account, one of the parameters was left blank.");
			}
		}
	}
	
	@Test
	public void test_makeAccountForClient_BlankParameters_WithSpaces() throws couldNotConnectToDatabaseException, ClientNotFoundException, AccountAddException, EmptyParameterException, BadParameterException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.addAccount(new PostAccountDTO("    "), " ");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to create an account, one of the parameters was left blank.");
			}
		}
	}
	
	
	@Test
	public void test_getAccountsViaClientID_NoIssue() throws BadParameterException, AccountNotFoundException, couldNotConnectToDatabaseException, EmptyParameterException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			ArrayList<Account> expected = new ArrayList<Account>(3);
			expected.add(new Account(1, 1, 1000));
			expected.add(new Account(2, 1, 5000));
			expected.add(new Account(3, 2, 3000));
			ArrayList<Account> actual = (ArrayList<Account>) accountService.getAccountsByClientID("1");
			assertEquals(expected, actual);
		}
	}
	
	@Test
	public void test_getAccountsViaClientID_NonIntegerClientID() throws AccountNotFoundException, couldNotConnectToDatabaseException, EmptyParameterException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountsByClientID("abc");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client ID must be int value. User provided abc");
			}
		}
	}
	
	@Test
	public void test_getAccountsViaClientID_BlankClientID_NoSpaces() throws BadParameterException, AccountNotFoundException, couldNotConnectToDatabaseException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountsByClientID("");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to get all of the accounts of a specific client, the Client ID parameter was left blank.");
			}
		}
	}
	
	@Test
	public void test_getAccountsViaClientID_BlankClientID_WithSpaces() throws BadParameterException, AccountNotFoundException, couldNotConnectToDatabaseException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountsByClientID("    ");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to get all of the accounts of a specific client, the Client ID parameter was left blank.");
			}
		}
	}
	
	@Test
	public void test_getAccountVia2IDs_NoIssue() throws BadParameterException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, AccountNotFoundException, EmptyParameterException, AccountClientMismatchException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			Account expected = new Account(1,1,2000);
			Account actual = accountService.getAccountBy2IDs("1", "1");
			assertEquals(expected, actual);
		}
	}
	
	@Test
	public void test_getAccountVia2IDs_AccountDoesNotExist() throws BadParameterException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, EmptyParameterException, AccountClientMismatchException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountBy2IDs("1", "2");
				fail("AccountNotFoundException was not thrown");
			} catch (AccountNotFoundException e) {
				assertEquals(e.getMessage(), "Account with id of 2 and client ID of 1 was not found");
			}
		}
	}
	
	@Test
	public void test_getAccountVia2IDs_ClientDoesNotExist() throws BadParameterException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, EmptyParameterException, AccountClientMismatchException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountBy2IDs("2", "1");
				fail("AccountNotFoundException was not thrown");
			} catch (AccountNotFoundException e) {
				assertEquals(e.getMessage(), "Account with id of 1 and client ID of 2 was not found");
			}
		}
	}
	
	@Test
	public void test_getAccountVia2IDs_AccountDoesNotBelongToClient() throws BadParameterException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, EmptyParameterException, AccountClientMismatchException, AccountNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountBy2IDs("2", "3");
				fail("AccountClientMismatchException was not thrown");
			} catch (AccountClientMismatchException e) {
				assertEquals(e.getMessage(), "The account asked for does not match the client asked for.");
			}
		}
	}
	
	@Test
	public void test_getAccountVia2IDs_NonIntegerClientID() throws ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, AccountNotFoundException, EmptyParameterException, AccountClientMismatchException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountBy2IDs("abc", "1");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client and Account ID's must be int values. User provided abc and 1");
			}
		}
	}
	
	@Test
	public void test_getAccountVia2IDs_NonIntegerAccountID() throws ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, AccountNotFoundException, EmptyParameterException, AccountClientMismatchException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountBy2IDs("1", "abc");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client and Account ID's must be int values. User provided 1 and abc");
			}
		}
	}
	
	@Test
	public void test_getAccountVia2IDs_NonIntegerMultipleIDs() throws ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, AccountNotFoundException, EmptyParameterException, AccountClientMismatchException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountBy2IDs("abc", "abc");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client and Account ID's must be int values. User provided abc and abc");
			}
		}
	}
	
	@Test
	public void test_getAccountVia2IDs_BlankClientID_NoSpaces() throws BadParameterException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, AccountNotFoundException, AccountClientMismatchException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountBy2IDs("", "1");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to get an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_getAccountVia2IDs_BlankAccountID_NoSpaces() throws BadParameterException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, AccountNotFoundException, AccountClientMismatchException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountBy2IDs("1", "");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to get an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_getAccountVia2IDs_BlankMultipleIDs_NoSpaces() throws BadParameterException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, AccountNotFoundException, AccountClientMismatchException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountBy2IDs("", "");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to get an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_getAccountVia2IDs_BlankClientID_WithSpaces() throws BadParameterException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, AccountNotFoundException, AccountClientMismatchException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountBy2IDs("     ", "1");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to get an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_getAccountVia2IDs_BlankAccountID_WithSpaces() throws BadParameterException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, AccountNotFoundException, AccountClientMismatchException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountBy2IDs("1", "       ");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to get an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_getAccountVia2IDs_BlankMultipleIDs_WithSpaces() throws BadParameterException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, AccountNotFoundException, AccountClientMismatchException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountBy2IDs("     ", " ");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to get an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_getAccountsBetweenTwoValues_NoIssue() throws BadParameterException, AccountNotFoundException, couldNotConnectToDatabaseException, EmptyParameterException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			ArrayList<Account> expected = new ArrayList<Account>(3);
			expected.add(new Account(1, 1, 1000));
			expected.add(new Account(2, 1, 5000));
			expected.add(new Account(3, 2, 3000));
			ArrayList<Account> actual = (ArrayList<Account>) accountService.getAccountsBetweenTwoValues("1", "500", "6000");
			assertEquals(expected, actual);
		}
	}
	
	@Test
	public void test_getAccountsBetweenTwoValues_LowValGreaterThanHighVal() throws AccountNotFoundException, couldNotConnectToDatabaseException, EmptyParameterException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountsBetweenTwoValues("1", "6000", "500");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "The lower value parameter must be lower than the higher value parameter, the user input 6000 as higher than 500");
			}
		}
	}
	
	@Test
	public void test_getAccountsBetweenTwoValues_NonIntegerClientID() throws AccountNotFoundException, couldNotConnectToDatabaseException, EmptyParameterException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountsBetweenTwoValues("abc", "500", "6000");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client ID must be int value. User provided abc");
			}
		}
	}
	
	@Test
	public void test_getAccountsBetweenTwoValues_NonIntegerLowVal() throws AccountNotFoundException, couldNotConnectToDatabaseException, EmptyParameterException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountsBetweenTwoValues("1", "abc", "6000");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Lower Value must be int value. User provided abc");
			}
		}
	}
	
	@Test
	public void test_getAccountsBetweenTwoValues_NonIntegerHighVal() throws AccountNotFoundException, couldNotConnectToDatabaseException, EmptyParameterException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountsBetweenTwoValues("1", "500", "abc");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Higher Value must be int value. User provided abc");
			}
		}
	}
	
	@Test
	public void test_getAccountsBetweenTwoValues_BlankClientID() throws BadParameterException, AccountNotFoundException, couldNotConnectToDatabaseException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountsBetweenTwoValues("", "500", "6000");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to get accounts between certain amounts using the client ID, one or more of the parameters were left blank.");
			}	
		}
	}
	
	@Test
	public void test_getAccountsBetweenTwoValues_BlankLowVal() throws BadParameterException, AccountNotFoundException, couldNotConnectToDatabaseException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountsBetweenTwoValues("1", "", "6000");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to get accounts between certain amounts using the client ID, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_getAccountsBetweenTwoValues_BlankHighVal() throws BadParameterException, AccountNotFoundException, couldNotConnectToDatabaseException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountsBetweenTwoValues("1", "500", "");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to get accounts between certain amounts using the client ID, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_getAccountsBetweenTwoValues_ClientDoesNotExist() throws BadParameterException, couldNotConnectToDatabaseException, EmptyParameterException, AccountNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountsBetweenTwoValues("2", "500", "900");
				fail("ClientNotFoundException was not thrown");
			} catch (ClientNotFoundException e) {
				assertEquals(e.getMessage(), "The client with the ID 2 was not found");
			}
		}
	}
	
	@Test
	public void test_getAccountsBetweenTwoValues_NoAccountsFound() throws BadParameterException, AccountNotFoundException, couldNotConnectToDatabaseException, EmptyParameterException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.getAccountsBetweenTwoValues("1", "500", "900");
				fail("AccountNotFoundException was not thrown");
			} catch (AccountNotFoundException e) {
				assertEquals(e.getMessage(), "No accounts with amounts between 400 and 2000 were found.");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_NoIssue() throws BadParameterException, EmptyParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			String expected = "The Account, 1, has had their amount updated to 500";
			String actual = accountService.updateAccountBy2IDs("1", "1", "500");
			assertEquals(expected, actual);
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_AccountDoesNotExist() throws BadParameterException, EmptyParameterException, couldNotConnectToDatabaseException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("1", "2", "500");
				fail("AccountNotFoundException was not thrown");
			} catch (AccountNotFoundException e) {
				assertEquals(e.getMessage(), "Account with ID 2 could not be found");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_ClientDoesNotExist() throws BadParameterException, EmptyParameterException, couldNotConnectToDatabaseException, AccountClientMismatchException, ClientNotFoundException, AccountNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("2", "1", "500");
				fail("ClientNotFoundException was not thrown");
			} catch (ClientNotFoundException e) {
				assertEquals(e.getMessage(), "The client with the ID 2 was not found");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_AccountDoesNotBelongToClient() throws BadParameterException, EmptyParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("2", "3", "500");
				fail("AccountClientMismatchException was not thrown");
			} catch (AccountClientMismatchException e) {
				assertEquals(e.getMessage(), "The account asked for does not match the client asked for.");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_NonIntegerClientID() throws EmptyParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("abc", "1", "500");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client ID must be int value. User provided abc");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_NonIntegerAccountID() throws EmptyParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("1", "abc", "500");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Account ID must be int value. User provided abc");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_NonIntegerAmount() throws EmptyParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("1", "1", "abc");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Amount must be int value. User provided abc");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_NonIntegerMultipleIDs() throws EmptyParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("abc", "abc", "abc");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client ID must be int value. User provided abc");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_BlankClientID_NoSpaces() throws BadParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("", "1", "500");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to update an account, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_BlankAccountID_NoSpaces() throws BadParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("1", "", "500");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to update an account, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_BlankMultipleIDs_NoSpaces() throws BadParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("", "", "500");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to update an account, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_Blankamount_NoSpaces() throws BadParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("1", "1", "");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to update an account, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_BlankClientID_WithSpaces() throws BadParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("    ", "1", "500");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to update an account, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_BlankAccountID_WithSpaces() throws BadParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("1", "    ", "500");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to update an account, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_updateAccountVia2IDs_BlankMultipleIDs_WithSpaces() throws BadParameterException, couldNotConnectToDatabaseException, AccountNotFoundException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.updateAccountBy2IDs("   ", " ", "");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to update an account, one or more of the parameters were left blank.");
			}
		}
	}	
	
	
	@Test
	public void test_deleteAccountVia2IDs_NoIssue() throws BadParameterException, EmptyParameterException, AccountClientMismatchException, AccountNotFoundException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			String expected = "Account 1 with amount of 500 for Client 1 was deleted successfully.";
			String actual = accountService.deleteAccount("1", "1");
			assertEquals(expected, actual);
		}
	}
	
	@Test
	public void test_deleteAccountVia2IDs_AccountDoesNotExist() throws BadParameterException, EmptyParameterException, AccountClientMismatchException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.deleteAccount("3", "4");
				fail("AccountNotFoundException was not thrown");
			} catch (AccountNotFoundException e) {
				assertEquals(e.getMessage(), "User tried to delete account with ID 4 which does not exist");
			}
		}
	}
	
	@Test
	public void test_deleteAccountVia2IDs_ClientDoesNotExist() throws BadParameterException, EmptyParameterException, AccountClientMismatchException, AccountNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.deleteAccount("4", "4");
				fail("ClientNotFoundException was not thrown");
			} catch (ClientNotFoundException e) {
				assertEquals(e.getMessage(), "User tried to delete account with client ID 4 which does not exist");
			}	
		}
	}
	
	@Test
	public void test_deleteAccountVia2IDs_AccountDoesNotBelongToClient() throws BadParameterException, EmptyParameterException, AccountNotFoundException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.deleteAccount("2", "3");
				fail("AccountClientMismatchException was not thrown");
			} catch (AccountClientMismatchException e) {
				assertEquals(e.getMessage(), "User tried to delete account, 3, and gave the client ID, 2, which the account does not belong to");
			}
		}
	}
	
	@Test
	public void test_deleteAccountVia2IDs_NonIntegerClientID() throws EmptyParameterException, AccountClientMismatchException, AccountNotFoundException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.deleteAccount("abc", "1");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client and Account ID's must be int values. User provided abc and 1");
			}
		}
	}
	
	@Test
	public void test_deleteAccountVia2IDs_NonIntegerAccountID() throws EmptyParameterException, AccountClientMismatchException, AccountNotFoundException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.deleteAccount("1", "abc");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client and Account ID's must be int values. User provided 1 and abc");
			}
		}
	}
	
	@Test
	public void test_deleteAccountVia2IDs_NonIntegerMultipleIDs() throws EmptyParameterException, AccountClientMismatchException, AccountNotFoundException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.deleteAccount("abc", "abc");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client and Account ID's must be int values. User provided abc and abc");
			}
		}
	}
	
	@Test
	public void test_deleteAccountVia2IDs_BlankClientID_NoSpaces() throws BadParameterException, AccountClientMismatchException, AccountNotFoundException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.deleteAccount("", "1");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to delete an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_deleteAccountVia2IDs_BlankAccountID_NoSpaces() throws BadParameterException, AccountClientMismatchException, AccountNotFoundException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.deleteAccount("1", "");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to delete an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_deleteAccountVia2IDs_BlankMultipleIDs_NoSpaces() throws BadParameterException, AccountClientMismatchException, AccountNotFoundException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.deleteAccount("", "");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to delete an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_deleteAccountVia2IDs_BlankClientID_WithSpaces() throws BadParameterException, AccountClientMismatchException, AccountNotFoundException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.deleteAccount("  ", "1");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to delete an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_deleteAccountVia2IDs_BlankAccountID_WithSpaces() throws BadParameterException, AccountClientMismatchException, AccountNotFoundException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.deleteAccount("1", "    ");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to delete an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_deleteAccountVia2IDs_BlankMultipleIDs_WithSpaces() throws BadParameterException, AccountClientMismatchException, AccountNotFoundException, ClientNotFoundException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				accountService.deleteAccount("   ", "   ");
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to delete an account using the client ID and account ID, one or more of the parameters were left blank.");
			}
		}
	}	
	
	
}