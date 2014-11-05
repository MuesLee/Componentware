package de.ts.chat.server.beans;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Singleton;

import de.fh_dortmund.inf.cw.chat.server.entities.UserStatistic;
import de.ts.chat.server.beans.interfaces.UserStatisticManagementLocal;
import de.ts.chat.server.beans.interfaces.UserStatisticManagementRemote;

@Singleton
public class UserStatisticManagementBean implements
		UserStatisticManagementLocal, UserStatisticManagementRemote {

	private Map<String, UserStatistic> userStatistics;
	private Calendar calendar;

	public UserStatisticManagementBean() {
		this.userStatistics = new HashMap<>(5);
		this.calendar = new GregorianCalendar();
	}

	@Override
	public UserStatistic getStatisticForUser(String userName) {

		return userStatistics.get(userName);
	}

	@Override
	public void userHasSendAMessage(String user) {
		UserStatistic userStatistic = userStatistics.get(user);
		int sentMessages = userStatistic.getMessages();
		userStatistic.setMessages(sentMessages + 1);
		userStatistics.put(user, userStatistic);
	}

	@Override
	public void userHasLoggedIn(String user) {
		UserStatistic userStatistic = userStatistics.get(user);
		if (userStatistic == null) {
			userStatistic = new UserStatistic();
		}

		int logins = userStatistic.getLogins();
		userStatistic.setLogins(logins + 1);
		userStatistic.setLastLogin(calendar.getTime());
		userStatistics.put(user, userStatistic);

	}

	@Override
	public void userHasLoggedOut(String user) {
		UserStatistic userStatistic = userStatistics.get(user);
		int logouts = userStatistic.getLogouts();
		userStatistic.setLogouts(logouts + 1);
		userStatistics.put(user, userStatistic);

	}

}
