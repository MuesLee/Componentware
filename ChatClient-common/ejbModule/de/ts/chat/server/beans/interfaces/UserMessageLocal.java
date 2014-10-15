package de.ts.chat.server.beans.interfaces;

import javax.ejb.Local;

import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessageType;

@Local
public interface UserMessageLocal extends UserMessage {
	void sendMessage(String user, String messageText,
			ChatMessageType messageType);
}
