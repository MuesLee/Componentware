package de.ts.chat.server.beans;

import java.util.GregorianCalendar;

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

	public UserStatisticManagementBean() {
	}

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public UserStatistic getStatisticForUser(String userName) {

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

		return userStatistic;
	}

	@Override
	public void userHasSendAMessage(String userName) {
		UserStatistic userStatistic = getStatisticForUser(userName);

		int sentMessages = userStatistic.getMessages();
		userStatistic.setMessages(sentMessages + 1);

		entityManager.merge(userStatistic);
		entityManager.flush();
	}

	@Override
	public void userHasLoggedIn(String userName) {
		UserStatistic userStatistic = getStatisticForUser(userName);

		int logins = userStatistic.getLogins();
		userStatistic.setLogins(logins + 1);
		userStatistic.setLastLogin(new GregorianCalendar().getTime());

		entityManager.merge(userStatistic);
		entityManager.flush();

	}

	@Override
	public void userHasLoggedOut(String userName) {
		UserStatistic userStatistic = getStatisticForUser(userName);
		int logouts = userStatistic.getLogouts();
		userStatistic.setLogouts(logouts + 1);

		entityManager.merge(userStatistic);
		entityManager.flush();
	}

}
