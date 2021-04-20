package com.revature.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revature.exceptions.couldNotConnectToDatabaseException;
import com.revature.model.Account;
import com.revature.service.AccountService;
import com.revature.service.ClientService;
import com.revature.dao.ClientRepository;
import com.revature.dto.PostAccountDTO;

import io.javalin.Javalin;
import io.javalin.http.Handler;

public class AccountController implements Controller {

	private Logger logger = LoggerFactory.getLogger(AccountController.class);
	private AccountService accountService;

	//asks for the ClientRepository because it must exist for anything for account servicing to take place
	public AccountController(ClientRepository clientRepository) throws couldNotConnectToDatabaseException {
		this.accountService = new AccountService(clientRepository);
	}
	
	private Handler postNewAccountByClientID = ctx -> {
		String id = ctx.pathParam("id");
		PostAccountDTO account = ctx.bodyAsClass(PostAccountDTO.class);
		String outMessage = accountService.addAccount(account, id);
		ctx.json(outMessage);
		ctx.status(200);
	};

	
	private Handler getAllAccountsByClientID = ctx -> {
		String id = ctx.pathParam("id");
		ArrayList<Account> accountList;
		String highVal = ctx.queryParam("amountLessThan");
		String lowVal = ctx.queryParam("amountGreaterThan");
		if(!(lowVal == null) && !(highVal == null)) {
			accountList = (ArrayList<Account>) accountService.getAccountsBetweenTwoValues(id, lowVal, highVal);
		} else {
			accountList = (ArrayList<Account>) accountService.getAccountsByClientID(id);
		}
		ctx.json(accountList);
		ctx.status(200);
	};
	
	
	private Handler getAccountByIDOfAccountAndIDOfClient = ctx -> {
		String clientID = ctx.pathParam("id");
		String accountID = ctx.pathParam("accID");
		Account account = accountService.getAccountBy2IDs(clientID, accountID);
		ctx.json(account);
		ctx.status(200);
	};

	
	private Handler updateAccountByIDOfAccountAndIDOfClient = ctx -> {
		String clientID = ctx.pathParam("id");
		String accountID = ctx.pathParam("accID");
		PostAccountDTO account = ctx.bodyAsClass(PostAccountDTO.class);
		String outMessage = accountService.updateAccountBy2IDs(clientID, accountID, String.valueOf(account.getAmount()));
		ctx.json(outMessage);
		ctx.status(200);
	};

	
	private Handler deleteAccountByIDOfAccountAndIDOfClient = ctx -> {
		String clientID = ctx.pathParam("id");
		String accountID = ctx.pathParam("accID");
		String outMessage = accountService.deleteAccount(clientID, accountID);
		ctx.json(outMessage);
		ctx.status(200);
	};
	
	@Override
	public void mapEndpoints(Javalin app) {
		// TODO Auto-generated method stub
		app.post("/clients/:id/accounts", postNewAccountByClientID);
		app.get("/clients/:id/accounts", getAllAccountsByClientID);
		app.get("/clients/:id/accounts/:accID", getAccountByIDOfAccountAndIDOfClient);
		app.put("/clients/:id/accounts/:accID", updateAccountByIDOfAccountAndIDOfClient);
		app.delete("/clients/:id/accounts/:accID", deleteAccountByIDOfAccountAndIDOfClient);
	}

}
