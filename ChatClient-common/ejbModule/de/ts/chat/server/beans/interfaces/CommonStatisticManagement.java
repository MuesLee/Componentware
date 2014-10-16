package de.ts.chat.server.beans.interfaces;

import java.util.List;

import de.fh_dortmund.inf.cw.chat.server.entities.CommonStatistic;

public interface CommonStatisticManagement {

	public List<CommonStatistic> getCommonStatistics();

	public void userHasSendAMessage();

	public void userHasLoggedIn();

	public void userHasLoggedOut();

}
