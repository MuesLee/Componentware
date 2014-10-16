package de.fh_dortmund.inf.cw.chat.server.entities;

import java.util.Date;

public class CommonStatistic extends Statistic {

	private Date startingDate;
	private Date endDate;

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getStartingDate() {
		return startingDate;
	}

	public void setStartingDate(Date startingDate) {
		this.startingDate = startingDate;
	}
}
