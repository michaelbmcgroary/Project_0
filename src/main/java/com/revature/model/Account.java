package com.revature.model;

public class Account {

	private int accountID;
	private int clientID;
	private int amount;
	
	public Account() {
		super();
	}
	
	public Account(int accountID, int clientID, int amount) {
		this.accountID = accountID;
		this.clientID = clientID;
		this.amount = amount;
	}

	public int getAccountID() {
		return accountID;
	}

	public void setAccountID(int accountID) {
		this.accountID = accountID;
	}

	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
	}
	
	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + accountID;
		result = prime * result + amount;
		result = prime * result + clientID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
		if (accountID != other.accountID)
			return false;
		if (amount != other.amount)
			return false;
		if (clientID != other.clientID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Account [accountID=" + accountID + ", clientID=" + clientID + ", amount=" + amount + "]";
	}
	
}
