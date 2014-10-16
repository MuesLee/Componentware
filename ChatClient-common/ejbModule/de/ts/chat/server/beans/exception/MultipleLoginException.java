package de.ts.chat.server.beans.exception;

public class MultipleLoginException extends Exception {
	public MultipleLoginException(String error) {
		super(error);
	}
}
