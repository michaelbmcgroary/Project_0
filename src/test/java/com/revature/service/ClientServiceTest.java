  
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

import com.revature.dao.ClientRepository;
import com.revature.dto.PostClientDTO;
import com.revature.exceptions.BadParameterException;
import com.revature.exceptions.ClientAddException;
import com.revature.exceptions.ClientAlreadyExistsException;
import com.revature.exceptions.ClientNotFoundException;
import com.revature.exceptions.EmptyParameterException;
import com.revature.exceptions.couldNotConnectToDatabaseException;
import com.revature.model.Client;
import com.revature.util.ConnectionUtil;

public class ClientServiceTest {

	private static ClientRepository mockClientRepository;
	private static Connection mockConnection;
	
	private ClientService clientService;
	
	@BeforeClass
	public static void setUp() throws couldNotConnectToDatabaseException, SQLException, ClientAddException, ClientNotFoundException, ClientAlreadyExistsException {
		mockClientRepository = mock(ClientRepository.class);
		mockConnection = mock(Connection.class);
		
		mockConnection = mock(Connection.class);
		
		when(mockClientRepository.newClient(eq(new PostClientDTO("George", "Lucas"))))
				.thenReturn(new Client(1, "George", "Lucas"));
		
		when(mockClientRepository.getClientById(eq(1)))
				.thenReturn(new Client(1, "George", "Lucas"));
		
		when(mockClientRepository.updateClient(eq(2), eq("Tommy"), eq("Wiseau")))
				.thenReturn(new Client(2, "Tommy", "Wiseau"));
		
		ArrayList<Client> fullList = new ArrayList<Client>(2);
		fullList.add(new Client(1, "George", "Lucas"));
		fullList.add(new Client(2, "Johnny", "Depp"));
		when(mockClientRepository.getAllClients()).thenReturn(fullList);
	}
	
	@Before
	public void beforeTest() {
		clientService = new ClientService(mockClientRepository);
		
	}
	
	@Test
	public void test_makeNewClient_NoIssues() throws BadParameterException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, ClientAddException, EmptyParameterException {
	
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			String actual = clientService.addClient(new PostClientDTO("George", "Lucas"));
			String expected = "Client George Lucas with ID of 1 was added successfully.";
			assertEquals(expected, actual);
		}
		
	}
	
	@Test
	public void test_makeNewClient_BlankParams_NoSpaces() throws BadParameterException, couldNotConnectToDatabaseException, ClientAddException {
		
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				clientService.addClient(new PostClientDTO("", ""));
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to add a Client, one or more of the parameters were left blank.");
			}
		}
	}
	
	@Test
	public void test_makeNewClient_BlankParams_WithSpaces() throws BadParameterException, couldNotConnectToDatabaseException, ClientAddException {
		
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				clientService.addClient(new PostClientDTO("       ", " "));
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to add a Client, one or more of the parameters were left blank.");
			}
			
		}
		
	}
	
	@Test
	public void test_blankLastName_blankFirstName_lastNameWithSpaces_firstNameWithout() throws BadParameterException, couldNotConnectToDatabaseException, ClientAddException {
		
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				clientService.addClient(new PostClientDTO("", "     "));
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to add a Client, one or more of the parameters were left blank.");
			}
			
		}
		
	}
	
	@Test
	public void test_blankLastName_blankFirstName_lastNameWithoutSpaces_firstNameWith() throws BadParameterException, couldNotConnectToDatabaseException, ClientAddException {
		
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				clientService.addClient(new PostClientDTO("      ", ""));
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to add a Client, one or more of the parameters were left blank.");
			}
			
		}
		
	}
	
	@Test
	public void test_blankLastNameWithoutSpaces_NonBlankFirstName() throws BadParameterException, couldNotConnectToDatabaseException, ClientAddException {
		
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				clientService.addClient(new PostClientDTO("Tommy", ""));
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to add a Client, one or more of the parameters were left blank.");
			}
			
		}
		
	}
	
	@Test
	public void test_blankLastNameWithSpaces_NonBlankFirstName() throws BadParameterException, couldNotConnectToDatabaseException, ClientAddException {
		
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				clientService.addClient(new PostClientDTO("Tommy", "    "));
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to add a Client, one or more of the parameters were left blank.");
			}
			
		}
		
	}
	
	@Test
	public void test_NonBlankLastName_BlankFirstNameWithoutSpaces() throws BadParameterException, couldNotConnectToDatabaseException, ClientAddException {
		
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				clientService.addClient(new PostClientDTO("", "Wiseau"));
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to add a Client, one or more of the parameters were left blank.");
			}
		}
		
	}
	
	@Test
	public void test_NonBlankLastName_BlankFirstNameWithSpaces() throws BadParameterException, couldNotConnectToDatabaseException, ClientAddException {
		
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				clientService.addClient(new PostClientDTO("        ", "Wiseau"));
				fail("EmptyParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to add a Client, one or more of the parameters were left blank.");
			}
			
		}
		
	}
	
	@Test
	public void test_getClientByID_NoIssues() throws BadParameterException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			Client expected = new Client(1, "George", "Lucas");
			Client actual = clientService.getClientById("1");
			assertEquals(expected, actual);
		}
	}
	
	@Test
	public void test_getClientById_nonIntegerClientID() throws couldNotConnectToDatabaseException, ClientAddException, ClientNotFoundException, ClientAlreadyExistsException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				clientService.getClientById("abc");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client id must be an int. User provided " + "abc");
			}
			
		}
	}
	
	@Test
	public void test_getClientByID_clientDoesNotExist() throws BadParameterException, couldNotConnectToDatabaseException, ClientAlreadyExistsException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				clientService.getClientById("6");
				fail("ClientNotFoundException was not thrown");
			} catch (ClientNotFoundException e) {
				assertEquals(e.getMessage(), "Client with id of 6 was not found");
			}
			
		}
	}
	
	@Test
	public void test_updateClientByID_NoIssues() throws BadParameterException, SQLException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, EmptyParameterException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			String expected = "The clientID, 2 has had their name updated to Tommy Wiseau";
			String actual = clientService.updateClientByID(new PostClientDTO("Tommy", "Wiseau"), "2");
			assertEquals(expected, actual);
		}
	}
	
	@Test
	public void test_updateClientByID_NonIntegerClientID() throws SQLException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException, EmptyParameterException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				clientService.updateClientByID(new PostClientDTO("Tommy", "Wiseau"), "abc");
				fail("BadParameterException was not thrown");
			} catch (BadParameterException e) {
				assertEquals(e.getMessage(), "Client id must be an int. User provided " + "abc");
			}
		}
	}
	
	@Test
	public void test_updateClientByID_BlankFirstAndLastNamesNoSpaces() throws BadParameterException, SQLException, ClientNotFoundException, couldNotConnectToDatabaseException, ClientAlreadyExistsException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			try {
				clientService.updateClientByID(new PostClientDTO("", ""), "2");
				fail("BadParameterException was not thrown");
			} catch (EmptyParameterException e) {
				assertEquals(e.getMessage(), "When trying to update a Client, one or more of the parameters were left blank.");
			}
		}
	}
	
	
	
	@Test
	public void test_getAllClients_NoIssues() throws couldNotConnectToDatabaseException {
		try(MockedStatic<ConnectionUtil> mockedConnectionUtil = mockStatic(ConnectionUtil.class)) {
			mockedConnectionUtil.when(ConnectionUtil::getConnection).thenReturn(mockConnection);
			
			ArrayList<Client> expected = new ArrayList<Client>(2);
			expected.add(new Client(1, "George", "Lucas"));
			expected.add(new Client(2, "Johnny", "Depp"));
			ArrayList<Client> actual = (ArrayList<Client>) clientService.getAllClients();
			assertEquals(expected, actual);
		}
	}

}