package de.ts.server.beans.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

import de.fh_dortmund.inf.cw.chat.server.entities.UserStatistic;

@Entity
@NamedQueries({
		@NamedQuery(name = "getNameOfOnlineUser", query = "SELECT u.name FROM ChatUser u WHERE u.online = true"),
		@NamedQuery(name = "getNameOfAllUser", query = "SELECT u.name FROM ChatUser u"),
		@NamedQuery(name = "getNumberOfOnlineUser", query = "SELECT count(u.name) FROM ChatUser u WHERE u.online =true"),
		@NamedQuery(name = "getNumberOfAllUser", query = "SELECT count(u.name) FROM ChatUser u"),
		@NamedQuery(name = "getAllUser", query = "SELECT u FROM ChatUser u") })
public class ChatUser implements Serializable {

	private static final long serialVersionUID = -3775255478452699913L;
	@Id
	private String name;
	@Column(nullable = false)
	private String passwordHash;

	@Column
	private boolean online;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	@JoinColumn(name = "USERSTATISTIC_ID", unique = true)
	private UserStatistic userStatistic;

	public ChatUser() {
	}

	public ChatUser(String name, String passwordHash) {
		this.setName(name);
		this.setPasswordHash(passwordHash);
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((passwordHash == null) ? 0 : passwordHash.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChatUser other = (ChatUser) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (passwordHash == null) {
			if (other.passwordHash != null)
				return false;
		} else if (!passwordHash.equals(other.passwordHash))
			return false;
		return true;
	}

	public UserStatistic getUserStatistic() {
		return userStatistic;
	}

	public void setUserStatistic(UserStatistic userStatistic) {
		this.userStatistic = userStatistic;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}
}
