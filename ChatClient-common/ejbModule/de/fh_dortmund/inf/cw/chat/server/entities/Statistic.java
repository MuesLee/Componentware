package de.fh_dortmund.inf.cw.chat.server.entities;

import java.io.Serializable;

public class Statistic implements Serializable {
	private static final long serialVersionUID = -3884213655072759667L;
	private int logins;
	private int logouts;
	private int messages;

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

}
