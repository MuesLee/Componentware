package de.ts.chat.server.beans.interfaces;

import javax.ejb.Local;

import de.ts.server.beans.entities.User;

@Local
public interface UserManagementLocal extends UserManagement {

	void delete(User user);

}
