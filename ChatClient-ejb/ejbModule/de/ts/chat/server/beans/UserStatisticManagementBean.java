package de.ts.chat.server.beans;

import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import de.fh_dortmund.inf.cw.chat.server.entities.UserStatistic;
import de.ts.chat.server.beans.interfaces.UserStatisticManagementLocal;
import de.ts.chat.server.beans.interfaces.UserStatisticManagementRemote;

@Stateless
public class UserStatisticManagementBean implements
		UserStatisticManagementLocal, UserStatisticManagementRemote {

	private static final Logger log = Logger
			.getLogger(UserStatisticManagementBean.class.getName());

	public UserStatisticManagementBean() {
	}

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public UserStatistic getStatisticForUser(String userName) {

		log.log(Level.FINER, "Die Benutzerstatistik von " + userName
				+ " wurde angefordert.");
		TypedQuery<UserStatistic> createNamedQuery = entityManager
				.createNamedQuery("getStatisticForChatUser",
						UserStatistic.class);
		createNamedQuery.setParameter("userName", userName);

		UserStatistic userStatistic = null;
		try {
			userStatistic = createNamedQuery.getSingleResult();

		} catch (NoResultException e) {
			userStatistic = new UserStatistic();
			entityManager.persist(userStatistic);
			entityManager.flush();
		}

		log.log(Level.FINER, "Die Benutzerstatistik von " + userName
				+ " wurde ausgeliefert.");

		return userStatistic;
	}

	@Override
	public void userHasSendAMessage(String userName) {

		log.log(Level.FINEST, "Der Benutzer " + userName
				+ " hat eine Nachricht geschrieben.");

		UserStatistic userStatistic = getStatisticForUser(userName);

		int sentMessages = userStatistic.getMessages();
		userStatistic.setMessages(sentMessages + 1);

		entityManager.merge(userStatistic);
		entityManager.flush();
		log.log(Level.FINEST, "Die Nachricht des Benutzers " + userName
				+ " ist in die Statistik eingeflossen.");
	}

	@Override
	public void userHasLoggedIn(String userName) {
		log.log(Level.FINEST, "Der Benutzer " + userName
				+ " hat sich eingeloggt.");
		UserStatistic userStatistic = getStatisticForUser(userName);

		int logins = userStatistic.getLogins();
		userStatistic.setLogins(logins + 1);
		userStatistic.setLastLogin(new GregorianCalendar().getTime());

		entityManager.merge(userStatistic);
		entityManager.flush();
		log.log(Level.FINEST, "Der Login des Benutzers " + userName
				+ " ist in die Statistik eingeflossen.");

	}

	@Override
	public void userHasLoggedOut(String userName) {
		log.log(Level.FINEST, "Der Benutzer " + userName
				+ " hat sich ausgeloggt.");
		UserStatistic userStatistic = getStatisticForUser(userName);
		int logouts = userStatistic.getLogouts();
		userStatistic.setLogouts(logouts + 1);

		entityManager.merge(userStatistic);
		entityManager.flush();
		log.log(Level.FINEST, "Der Logout des Benutzers " + userName
				+ " ist in die Statistik eingeflossen.");
	}

}
