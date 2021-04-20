package com.revature.exceptions;


public class AccountClientMismatchException extends Exception {

	public AccountClientMismatchException() {
	}

	public AccountClientMismatchException(String message) {
		super(message);
	}

	public AccountClientMismatchException(Throwable cause) {
		super(cause);
	}

	public AccountClientMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccountClientMismatchException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
