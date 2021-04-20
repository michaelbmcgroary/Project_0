package com.revature.exceptions;


public class EmptyParameterException extends Exception {

	public EmptyParameterException() {
	}

	public EmptyParameterException(String message) {
		super(message);
	}

	public EmptyParameterException(Throwable cause) {
		super(cause);
	}

	public EmptyParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	public EmptyParameterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
