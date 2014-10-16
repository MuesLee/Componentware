package de.fh_dortmund.inf.cw.chat.server.entities;

import java.util.Date;

public class UserStatistic extends Statistic {
	private Date lastLogin;

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

}
