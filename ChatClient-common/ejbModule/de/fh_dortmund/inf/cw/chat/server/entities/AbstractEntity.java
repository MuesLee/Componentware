package de.fh_dortmund.inf.cw.chat.server.entities;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@MappedSuperclass
public abstract class AbstractEntity {

	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	@PrePersist
	private void hasBeenCreated() {
		createdAt = new GregorianCalendar().getTime();
	}

	@PreUpdate
	private void hasBeenUpdated() {
		updatedAt = new GregorianCalendar().getTime();
	}

}
