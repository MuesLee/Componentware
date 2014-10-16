package de.ts.chat.server.beans.interfaces;

import de.ts.chat.server.beans.exception.InvalidLoginException;
import de.ts.chat.server.beans.exception.MultipleLoginException;

public interface UserSession {

	public void changePassword(String oldPW, String newPW) throws Exception;

	public void disconnect() throws Exception;

	public String getUserName();

	public void logout() throws Exception;

	public void delete(String password) throws Exception;

	public void login(String userName, String password)
			throws MultipleLoginException, InvalidLoginException;

	public void remove();

}
