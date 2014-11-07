package de.fh_dortmund.inf.cw.chat.server.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Statistic implements Serializable {
	private static final long serialVersionUID = -3884213655072759667L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int statisticID;

	@Column
	private int logins;
	@Column
	private int logouts;
	@Column
	private int messages;

	public Statistic() {
	}

	public int getLogins() {
		return logins;
	}

	public void setLogins(int logins) {
		this.logins = logins;
	}

	public int getLogouts() {
		return logouts;
	}

	public void setLogouts(int logouts) {
		this.logouts = logouts;
	}

	public int getMessages() {
		return messages;
	}

	public void setMessages(int messages) {
		this.messages = messages;
	}

	public int getStatisticID() {
		return statisticID;
	}

	public void setStatisticID(int statisticID) {
		this.statisticID = statisticID;
	}

}
