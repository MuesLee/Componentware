package de.ts.chat.server.beans.interfaces;

import javax.ejb.Local;

import de.ts.server.beans.entities.ChatUser;

@Local
public interface UserManagementLocal extends UserManagement {

	void delete(ChatUser user);

	void logout(ChatUser user);

}
