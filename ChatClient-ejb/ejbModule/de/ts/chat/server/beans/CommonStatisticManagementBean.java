package de.ts.chat.server.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Singleton;

import de.fh_dortmund.inf.cw.chat.server.entities.CommonStatistic;
import de.ts.chat.server.beans.interfaces.CommonStatisticManagementLocal;
import de.ts.chat.server.beans.interfaces.CommonStatisticManagementRemote;

@Singleton
public class CommonStatisticManagementBean implements
		CommonStatisticManagementLocal, CommonStatisticManagementRemote {

	private List<CommonStatistic> commonStatistics;
	private CommonStatistic currentStatistic;

	public CommonStatisticManagementBean() {

		this.commonStatistics = new ArrayList<>();
		this.currentStatistic = new CommonStatistic();
		this.currentStatistic.setStartingDate(new Date(System.nanoTime()));
	}

	private void aggregateCurrentStatistic() {
		currentStatistic.setEndDate(new Date(System.nanoTime()));
		commonStatistics.add(currentStatistic);
		currentStatistic = new CommonStatistic();
		currentStatistic.setStartingDate(new Date(System.nanoTime()));
	}

	public List<CommonStatistic> getCommonStatistics() {
		return commonStatistics;
	}

	public void setCommonStatistics(List<CommonStatistic> commonStatistics) {
		this.commonStatistics = commonStatistics;
	}

	public void userHasSendAMessage() {
		int messages = currentStatistic.getMessages();
		currentStatistic.setMessages(messages);
	}

	@Override
	public void userHasLoggedIn() {
		int logins = currentStatistic.getLogins();
		currentStatistic.setLogins(++logins);

	}

	@Override
	public void userHasLoggedOut() {
		int logouts = currentStatistic.getLogouts();
		currentStatistic.setLogouts(++logouts);

	}
}
