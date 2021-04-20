package com.revature.exceptions;


public class couldNotConnectToDatabaseException extends Exception {
	public couldNotConnectToDatabaseException() {

	}

	public couldNotConnectToDatabaseException(String message) {
		super(message);

	}

	public couldNotConnectToDatabaseException(Throwable cause) {
		super(cause);

	}

	public couldNotConnectToDatabaseException(String message, Throwable cause) {
		super(message, cause);

	}

	public couldNotConnectToDatabaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}
}
