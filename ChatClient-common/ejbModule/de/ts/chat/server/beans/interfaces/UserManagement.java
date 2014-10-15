package de.ts.chat.server.beans.interfaces;

import java.util.List;

import de.ts.server.beans.entities.User;

public interface UserManagement {

	public int getNumberOfOnlineUsers();

	public int getNumberOfRegisteredUsers();

	public List<String> getOnlineUsers();

	public User login(String userName, String password) throws Exception;

	public void register(String userName, String password) throws Exception;

}
