package de.ts.chat.server.beans.interfaces;

import de.fh_dortmund.inf.cw.chat.server.entities.UserStatistic;

public interface UserStatisticManagement {

	public UserStatistic getStatisticForUser(String userName);

	public void userHasSendAMessage(String user);

	public void userHasLoggedIn(String user);

	public void userHasLoggedOut(String user);
}
