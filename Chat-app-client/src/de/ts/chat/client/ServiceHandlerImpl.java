package de.ts.chat.client;

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
import de.fh_dortmund.inf.cw.chat.client.shared.UserSessionHandler;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessage;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessageType;
import de.ts.chat.server.beans.interfaces.UserManagementRemote;
import de.ts.chat.server.beans.interfaces.UserSessionRemote;

public class ServiceHandlerImpl extends ServiceHandler implements
		MessageListener, UserSessionHandler, ChatMessageHandler {

	private Context ctx;

	private JMSContext jmsContext;
	private Queue chatMessageQueue;
	private Topic chatMessageTopic;

	private UserManagementRemote userManagement;
	private UserSessionRemote userSession;

	private static ServiceHandlerImpl instance;

	private ServiceHandlerImpl() {
		try {
			ctx = new InitialContext();

			userManagement = (UserManagementRemote) ctx
					.lookup("java:global/ChatClient-ear/ChatClient-ejb/UserManagementBean!de.ts.chat.server.beans.interfaces.UserManagementRemote");
			userSession = (UserSessionRemote) ctx
					.lookup("java:global/ChatClient-ear/ChatClient-ejb/UserSessionBean!de.ts.chat.server.beans.interfaces.UserSessionRemote");

			initJMSConnection();
		} catch (NamingException ex) {
			System.err.println(ex.getMessage());
		}
	}

	private void initJMSConnection() {
		try {
			ConnectionFactory connectionFactory = (ConnectionFactory) ctx
					.lookup("java:comp/DefaultJMSConnectionFactory");

			jmsContext = connectionFactory.createContext();
			chatMessageTopic = (Topic) ctx
					.lookup("java:global/jms/ChatMessageTopic");

			chatMessageQueue = (Queue) ctx
					.lookup("java:global/jms/ChatMessageQueue");
			// String selector = "(CHATMESSAGE_TYPE = "
			// + ChatMessageType.DISCONNECT.ordinal()
			// + " and CHATMESSAGE_SENDER = " + userSession.getUserName()
			// + ") OR CHATMESSAGE_TYPE in ( "
			// + ChatMessageType.TEXT.ordinal() + ")";
			// JMSConsumer consumer =
			// jmsContext.createConsumer(chatMessageTopic,
			// selector);
			JMSConsumer consumer = jmsContext.createConsumer(chatMessageTopic);
			consumer.setMessageListener(this);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	private void notifyViaChatMessageQueue(String messageText, String sender) {

		try {
			Message message = jmsContext.createMessage();
			message.setIntProperty("CHATMESSAGE_TYPE",
					ChatMessageType.TEXT.ordinal());
			message.setStringProperty("CHATMESSAGE_SENDER", sender);
			message.setStringProperty("CHATMESSAGE_TEXT", messageText);
			message.setJMSDeliveryMode(Message.DEFAULT_DELIVERY_MODE);
			jmsContext.createProducer().send(chatMessageQueue, message);
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
		userSession.delete(password);
	}

	@Override
	public void disconnect() {
		userSession.disconnect();
	}

	@Override
	public int getNumberOfOnlineUsers() {
		return userManagement.getNumberOfOnlineUsers();
	}

	@Override
	public int getNumberOfRegisteredUsers() {
		// TODO Auto-generated method stub
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
		userSession.login(userName, password);
	}

	@Override
	public void logout() throws Exception {
		userSession.logout();
	}

	@Override
	public void register(String name, String password) throws Exception {
		userManagement.register(name, password);
	}

	@Override
	public void sendChatMessage(String message) {
		message = cleanUpMessage(message);
		notifyViaChatMessageQueue(message, userSession.getUserName());
	}

	private String cleanUpMessage(String message) {
		// TODO Schimpfwort filter
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

			String sender = message.getStringProperty("CHATMESSAGE_SENDER");
			String text = message.getStringProperty("CHATMESSAGE_TEXT");
			Date date = new Date(message.getJMSTimestamp());

			ChatMessage chatMessage = new ChatMessage(type, sender, text, date);
			setChanged();
			notifyObservers(chatMessage);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
