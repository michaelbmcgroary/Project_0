package com.revature.exceptions;


public class ClientAlreadyExistsException extends Exception {

	public ClientAlreadyExistsException() {
		super();
	}

	public ClientAlreadyExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

	public ClientAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientAlreadyExistsException(String message) {
		super(message);
	}

	public ClientAlreadyExistsException(Throwable cause) {
		super(cause);
	}

}
