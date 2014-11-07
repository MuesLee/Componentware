package de.ts.chat.server.beans;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
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

	private static final Logger log = Logger.getLogger(UserManagementBean.class
			.getName());

	private static final long serialVersionUID = -8960059270336029913L;

	@PersistenceContext
	private EntityManager entityManager;

	public UserManagementBean() {
		// TODO Auto-generated constructor stub
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<ChatUser> getUsers() {
		TypedQuery<ChatUser> createNamedQuery = entityManager.createNamedQuery(
				"getAllUser", ChatUser.class);
		List<ChatUser> resultList = createNamedQuery.getResultList();
		return resultList;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<String> getOnlineUsers() {

		Query createNamedQuery = entityManager
				.createNamedQuery("getNameOfOnlineUser");
		@SuppressWarnings("unchecked")
		List<String> resultList = createNamedQuery.getResultList();

		return resultList;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public int getNumberOfOnlineUsers() {
		Query createNamedQuery = entityManager
				.createNamedQuery("getNumberOfOnlineUser");
		Long number = (long) createNamedQuery.getSingleResult();
		return number.intValue();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
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

		log.info("Registrierungsversuch für Benutzer: " + name);

		ChatUser user = entityManager.find(ChatUser.class, name);
		if (user != null) {
			throw new Exception("Username: " + name + " ist bereits vergeben");
		}

		String hashedPassword = UserSessionBean.generateHash(password);
		user = new ChatUser(name, hashedPassword);
		user.setUserStatistic(new UserStatistic());

		entityManager.persist(user);
		entityManager.flush();
		log.info("Registrierung erfolgreich für Benutzer: " + name);

	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ChatUser login(String userName, String password)
			throws InvalidLoginException, MultipleLoginException {
		log.info("Loginversuch für Benutzer: " + userName);
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

				log.info("Login erfolgreich für Benutzer: " + userName);
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
		log.info("Löschanfrage für " + user.getName());
		user = entityManager.find(ChatUser.class, user.getName(),
				LockModeType.PESSIMISTIC_WRITE);
		entityManager.remove(user);
		entityManager.flush();
		log.info("Löschung des Users " + user + " erfolgreich");
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void logout(ChatUser user) {
		log.info("User " + user.getName() + " will sich ausloggen.");
		ChatUser derp = entityManager.find(ChatUser.class, user.getName(),
				LockModeType.PESSIMISTIC_WRITE);
		derp.setOnline(false);
		entityManager.merge(derp);

		entityManager.flush();
		log.info("User " + derp.getName() + " hat sich ausgeloggt");

	}

}
