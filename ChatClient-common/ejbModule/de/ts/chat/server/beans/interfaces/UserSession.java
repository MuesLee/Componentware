package de.ts.chat.server.beans.interfaces;

public interface UserSession {

	public void changePassword(String oldPW, String newPW) throws Exception;

	public void disconnect();

	public String getUserName();

	public void logout() throws Exception;

	public void delete(String password) throws Exception;

	public void login(String userName, String password) throws Exception;

	public void remove();

}
