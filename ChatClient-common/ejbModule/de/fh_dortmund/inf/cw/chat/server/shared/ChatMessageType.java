package de.fh_dortmund.inf.cw.chat.server.shared;

public enum ChatMessageType {
	TEXT(0), REGISTER(1), LOGIN(2), LOGOUT(3), DISCONNECT(4), STATISTIC(5);

	private int value;

	private ChatMessageType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ChatMessageType getChatMessageType(int value) {
		ChatMessageType chatMessageType = null;

		for (ChatMessageType tempChatMessageType : values()) {
			if (tempChatMessageType.getValue() == value) {
				chatMessageType = tempChatMessageType;
			}
		}

		return chatMessageType;
	}
}
