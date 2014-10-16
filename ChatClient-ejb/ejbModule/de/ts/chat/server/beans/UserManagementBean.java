package de.ts.chat.server.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;

import de.ts.chat.server.beans.exception.InvalidLoginException;
import de.ts.chat.server.beans.exception.MultipleLoginException;
import de.ts.chat.server.beans.interfaces.UserManagementLocal;
import de.ts.chat.server.beans.interfaces.UserManagementRemote;
import de.ts.server.beans.entities.User;

@Singleton
public class UserManagementBean implements UserManagementLocal,
		UserManagementRemote, Serializable {

	private static final long serialVersionUID = -8960059270336029913L;

	private Set<User> users;
	private Set<User> onlineUsers;

	@PostConstruct
	private void init() {
		users = new HashSet<User>(3);
		onlineUsers = new HashSet<User>(2);
	}

	public UserManagementBean() {
		// TODO Auto-generated constructor stub
	}

	public Set<User> getUsers() {
		return users;
	}

	public List<String> getOnlineUsers() {
		List<String> userNames = new ArrayList<String>();

		for (User user : onlineUsers) {
			userNames.add(user.getName());
		}

		return userNames;
	}

	@Override
	public int getNumberOfOnlineUsers() {
		// TODO Auto-generated method stub
		return onlineUsers.size();
	}

	@Override
	public int getNumberOfRegisteredUsers() {
		// TODO Auto-generated method stub
		return users.size();
	}

	@Override
	public void register(String name, String password) throws Exception {

		for (User user : users) {
			if (user.getName().equals(name)) {
				throw new Exception("Username: " + name
						+ " ist bereits vergeben");
			}
		}

		String hashedPassword = UserSessionBean.generateHash(password);
		User user = new User(name, hashedPassword);
		users.add(user);
	}

	@Override
	public User login(String userName, String password)
			throws InvalidLoginException, MultipleLoginException {
		String hashedPassword = UserSessionBean.generateHash(password);
		User user = new User(userName, hashedPassword);
		if (users.contains(user)) {
			if (!onlineUsers.contains(user)) {
				onlineUsers.add(user);
				return user;
			} else {
				throw new MultipleLoginException(
						"Sie sind bereits eingeloggt. Alle Sitzungen werden getrennt.");
			}

		} else {
			throw new InvalidLoginException(
					"Ungültige Kombination von Username und Passwort");
		}
	}

	@Override
	public void delete(User user) {
		users.remove(user);
		onlineUsers.remove(user);

	}

	@Override
	public void logout(User user) {
		onlineUsers.remove(user);
	}

}
