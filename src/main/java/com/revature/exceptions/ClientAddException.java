package com.revature.exceptions;


public class ClientAddException extends Exception {

	public ClientAddException() {
	}

	public ClientAddException(String message) {
		super(message);
	}

	public ClientAddException(Throwable cause) {
		super(cause);
	}

	public ClientAddException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientAddException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
