package de.ts.chat.server.beans.interfaces;

import java.util.List;

import de.ts.chat.server.beans.exception.InvalidLoginException;
import de.ts.chat.server.beans.exception.MultipleLoginException;
import de.ts.server.beans.entities.User;

public interface UserManagement {

	public int getNumberOfOnlineUsers();

	public int getNumberOfRegisteredUsers();

	public List<String> getOnlineUsers();

	public User login(String userName, String password)
			throws MultipleLoginException, InvalidLoginException;

	public void register(String userName, String password) throws Exception;

}
