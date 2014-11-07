package de.fh_dortmund.inf.cw.chat.server.entities;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@NamedQueries({
		@NamedQuery(name = "getCurrentCommonStatistic", query = "SELECT s FROM CommonStatistic AS s WHERE s.endDate is null"),
		@NamedQuery(name = "getAllCommonStatistics", query = "SELECT s FROM CommonStatistic AS s WHERE s.endDate is not null") })
public class CommonStatistic extends Statistic {

	private static final long serialVersionUID = 5338809182417258539L;
	@Temporal(TemporalType.TIMESTAMP)
	private Date startingDate;
	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	public CommonStatistic() {
		GregorianCalendar gc = new GregorianCalendar();
		startingDate = gc.getTime();
	}

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
