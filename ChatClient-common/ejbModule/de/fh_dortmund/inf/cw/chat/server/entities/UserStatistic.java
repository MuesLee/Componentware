package de.fh_dortmund.inf.cw.chat.server.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import de.ts.server.beans.entities.ChatUser;

@Entity
@NamedQueries({ @NamedQuery(name = "getStatisticForChatUser", query = "SELECT u.userStatistic FROM ChatUser u WHERE u.name = :userName") })
public class UserStatistic extends Statistic {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9155603997970446277L;
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastLogin;

	public UserStatistic() {

	}

	@OneToOne(mappedBy = "userStatistic")
	private ChatUser chatUser;

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

}
