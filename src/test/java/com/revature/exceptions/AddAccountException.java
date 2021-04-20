package com.revature.exceptions;

public class AddAccountException extends Exception {

	public AddAccountException() {
	}

	public AddAccountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

	public AddAccountException(String message, Throwable cause) {
		super(message, cause);

	}

	public AddAccountException(String message) {
		super(message);
	}

	public AddAccountException(Throwable cause) {
		super(cause);
	}

}
