package de.ts.chat.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import de.fh_dortmund.inf.cw.chat.client.shared.ChatMessageHandler;
import de.fh_dortmund.inf.cw.chat.client.shared.ServiceHandler;
import de.fh_dortmund.inf.cw.chat.client.shared.StatisticHandler;
import de.fh_dortmund.inf.cw.chat.client.shared.UserSessionHandler;
import de.fh_dortmund.inf.cw.chat.server.entities.CommonStatistic;
import de.fh_dortmund.inf.cw.chat.server.entities.UserStatistic;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessage;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessageType;
import de.ts.chat.server.beans.exception.MultipleLoginException;
import de.ts.chat.server.beans.interfaces.CommonStatisticManagementRemote;
import de.ts.chat.server.beans.interfaces.UserManagementRemote;
import de.ts.chat.server.beans.interfaces.UserSessionRemote;
import de.ts.chat.server.beans.interfaces.UserStatisticManagementRemote;

public class ServiceHandlerImpl extends ServiceHandler implements
		MessageListener, UserSessionHandler, ChatMessageHandler,
		StatisticHandler {

	private Context ctx;

	private JMSContext jmsContext;
	private Queue chatMessageQueue;
	private Topic chatMessageTopic;

	private UserManagementRemote userManagement;
	private UserSessionRemote userSession;
	private UserStatisticManagementRemote userStatistic;
	private CommonStatisticManagementRemote commonStatistic;

	private static ServiceHandlerImpl instance;

	private ServiceHandlerImpl() {
		try {
			ctx = new InitialContext();

			userManagement = (UserManagementRemote) ctx
					.lookup("java:global/ChatClient-ear/ChatClient-ejb/UserManagementBean!de.ts.chat.server.beans.interfaces.UserManagementRemote");
			userSession = (UserSessionRemote) ctx
					.lookup("java:global/ChatClient-ear/ChatClient-ejb/UserSessionBean!de.ts.chat.server.beans.interfaces.UserSessionRemote");
			commonStatistic = (CommonStatisticManagementRemote) ctx
					.lookup("java:global/ChatClient-ear/ChatClient-ejb/CommonStatisticManagementBean!de.ts.chat.server.beans.interfaces.CommonStatisticManagementRemote");
			userStatistic = (UserStatisticManagementRemote) ctx
					.lookup("java:global/ChatClient-ear/ChatClient-ejb/UserStatisticManagementBean!de.ts.chat.server.beans.interfaces.UserStatisticManagementRemote");

		} catch (NamingException ex) {
			System.err.println(ex.getMessage());
		}
	}

	private void initJMSConnection(String userName) {
		try {
			ConnectionFactory connectionFactory = (ConnectionFactory) ctx
					.lookup("java:comp/DefaultJMSConnectionFactory");

			jmsContext = connectionFactory.createContext();
			chatMessageTopic = (Topic) ctx
					.lookup("java:global/jms/ChatMessageTopic");

			chatMessageQueue = (Queue) ctx
					.lookup("java:global/jms/ChatMessageQueue");

		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	private void subscribeConsumer() {
		int ordinalDisconnect = ChatMessageType.DISCONNECT.ordinal();

		String selector = "(CHATMESSAGE_TYPE = " + ordinalDisconnect
				+ " and CHATMESSAGE_SENDER = \'" + userSession.getUserName()
				+ "\') OR CHATMESSAGE_TYPE <> " + ordinalDisconnect;

		JMSConsumer consumer = jmsContext.createConsumer(chatMessageTopic,
				selector);
		consumer.setMessageListener(this);
	}

	private void notifyViaChatMessageQueue(String messageText, String sender,
			ChatMessageType type) {

		if (jmsContext == null) {
			initJMSConnection(sender);
		}

		try {
			Message message = jmsContext.createMessage();
			message.setIntProperty("CHATMESSAGE_TYPE", type.ordinal());
			message.setStringProperty("CHATMESSAGE_SENDER", sender);
			message.setStringProperty("CHATMESSAGE_TEXT", messageText);
			message.setJMSDeliveryMode(Message.DEFAULT_DELIVERY_MODE);
			jmsContext.createProducer().send(chatMessageQueue, message);

			// System.out.println("MESSAGE SENT: " + type);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public static ServiceHandlerImpl getInstance() {
		if (instance == null) {
			instance = new ServiceHandlerImpl();
		}
		return instance;
	}

	@Override
	public void changePassword(String oldPassword, String newPassword)
			throws Exception {
		userSession.changePassword(oldPassword, newPassword);
	}

	@Override
	public void delete(String password) throws Exception {
		notifyViaChatMessageQueue("Deleted", getUserName()
				+ " hat seinen Account gelöscht und", ChatMessageType.LOGOUT);
		userSession.delete(password);
	}

	@Override
	public void disconnect() {
		try {
			userSession.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getNumberOfOnlineUsers() {
		return userManagement.getNumberOfOnlineUsers();
	}

	@Override
	public int getNumberOfRegisteredUsers() {
		return userManagement.getNumberOfRegisteredUsers();
	}

	@Override
	public List<String> getOnlineUsers() {

		return userManagement.getOnlineUsers();
	}

	@Override
	public String getUserName() {

		return userSession.getUserName();
	}

	@Override
	public void login(String userName, String password) throws Exception {
		try {
			userSession.login(userName, password);
			notifyViaChatMessageQueue("LOGIN!", userName, ChatMessageType.LOGIN);
			subscribeConsumer();
			userStatistic.userHasLoggedIn(getUserName());
			commonStatistic.userHasLoggedIn();
		} catch (MultipleLoginException e) {

			notifyViaChatMessageQueue("Disconnect", userName,
					ChatMessageType.DISCONNECT);
			throw e;
		}
	}

	@Override
	public void logout() throws Exception {

		if (userSession.getUserName() == null) {
			return;
		}

		final String userName = getUserName();
		userStatistic.userHasLoggedOut(userName);
		commonStatistic.userHasLoggedOut();
		userSession.logout();
		notifyViaChatMessageQueue("Logout", userName, ChatMessageType.LOGOUT);
	}

	@Override
	public void register(String name, String password) throws Exception {
		userManagement.register(name, password);

		notifyViaChatMessageQueue("Register", name, ChatMessageType.REGISTER);
	}

	@Override
	public void sendChatMessage(String message) {
		message = cleanUpMessage(message);
		final String userName = userSession.getUserName();
		notifyViaChatMessageQueue(message, userName, ChatMessageType.TEXT);
		userStatistic.userHasSendAMessage(userName);
		commonStatistic.userHasSendAMessage();
	}

	private String cleanUpMessage(String message) {

		List<String> meanWords = new ArrayList<>();
		meanWords.add("fuck");
		meanWords.add("fick");
		meanWords.add("ficken");
		meanWords.add("fucking");
		meanWords.add("bitch");
		meanWords.add("penis");
		meanWords.add("muschi");
		meanWords.add("bastard");

		String[] wordsInMessage = message.split(" ");
		for (int i = 0; i < wordsInMessage.length; i++) {
			for (String meanWord : meanWords) {
				String lowerCaseWordToCheck = wordsInMessage[i].toLowerCase();
				if (lowerCaseWordToCheck.equals(meanWord)) {
					message = message.replaceAll(wordsInMessage[i], "****");
				}

			}

		}

		return message;
	}

	@Override
	public void onMessage(Message message) {
		try {
			if (!message.getJMSDestination().equals(chatMessageTopic)) {
				return;
			}

			ChatMessageType type;

			type = ChatMessageType.getChatMessageType(message
					.getIntProperty("CHATMESSAGE_TYPE"));

			if (type == ChatMessageType.DISCONNECT) {
				disconnect();
			}

			String sender = message.getStringProperty("CHATMESSAGE_SENDER");
			String text = message.getStringProperty("CHATMESSAGE_TEXT");
			Date date = new Date(message.getJMSTimestamp());

			ChatMessage chatMessage = new ChatMessage(type, sender, text, date);
			setChanged();
			notifyObservers(chatMessage);
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

	@Override
	public List<CommonStatistic> getStatistics() {
		return commonStatistic.getCommonStatistics();
	}

	@Override
	public UserStatistic getUserStatistic() {
		return userStatistic.getStatisticForUser(getUserName());
	}
}
