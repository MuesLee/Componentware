package de.ts.chat.server.beans;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import de.fh_dortmund.inf.cw.chat.server.entities.UserStatistic;
import de.ts.chat.server.beans.exception.InvalidLoginException;
import de.ts.chat.server.beans.exception.MultipleLoginException;
import de.ts.chat.server.beans.interfaces.UserManagementLocal;
import de.ts.chat.server.beans.interfaces.UserManagementRemote;
import de.ts.server.beans.entities.ChatUser;

@Stateless
public class UserManagementBean implements UserManagementLocal,
		UserManagementRemote, Serializable {

	private static final long serialVersionUID = -8960059270336029913L;

	@PersistenceContext
	private EntityManager entityManager;

	public UserManagementBean() {
		// TODO Auto-generated constructor stub
	}

	public List<ChatUser> getUsers() {
		TypedQuery<ChatUser> createNamedQuery = entityManager.createNamedQuery(
				"getAllUser", ChatUser.class);
		List<ChatUser> resultList = createNamedQuery.getResultList();
		return resultList;
	}

	public List<String> getOnlineUsers() {

		Query createNamedQuery = entityManager
				.createNamedQuery("getNameOfOnlineUser");
		@SuppressWarnings("unchecked")
		List<String> resultList = createNamedQuery.getResultList();

		return resultList;
	}

	@Override
	public int getNumberOfOnlineUsers() {
		Query createNamedQuery = entityManager
				.createNamedQuery("getNumberOfOnlineUser");
		Long number = (long) createNamedQuery.getSingleResult();
		return number.intValue();
	}

	@Override
	public int getNumberOfRegisteredUsers() {
		// TODO Auto-generated method stub
		Query createNamedQuery = entityManager
				.createNamedQuery("getNumberOfAllUser");
		Long number = (long) createNamedQuery.getSingleResult();
		return number.intValue();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void register(String name, String password) throws Exception {

		ChatUser user = entityManager.find(ChatUser.class, name);
		if (user != null) {
			throw new Exception("Username: " + name + " ist bereits vergeben");
		}

		String hashedPassword = UserSessionBean.generateHash(password);
		user = new ChatUser(name, hashedPassword);
		user.setUserStatistic(new UserStatistic());

		entityManager.persist(user);
		entityManager.flush();
	}

	@Override
	public ChatUser login(String userName, String password)
			throws InvalidLoginException, MultipleLoginException {
		String hashedPassword = UserSessionBean.generateHash(password);
		ChatUser user = entityManager.find(ChatUser.class, userName);

		if (user != null) {

			if (!user.getPasswordHash().equals(hashedPassword)) {
				throw new InvalidLoginException(
						"Ungültige Kombination von Username und Passwort");
			}

			if (!user.isOnline()) {
				user.setOnline(true);
				entityManager.merge(user);
				entityManager.flush();
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
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void delete(ChatUser user) {

		entityManager.remove(user);
		entityManager.flush();
	}

	@Override
	public void logout(ChatUser user) {
		user.setOnline(false);
		entityManager.merge(user);
		entityManager.flush();

	}

}
