package de.ts.chat.client;

import java.util.Date;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
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
import de.ts.chat.server.beans.interfaces.UserMessageRemote;
import de.ts.chat.server.beans.interfaces.UserSessionRemote;

public class ServiceHandlerImpl extends ServiceHandler implements
		MessageListener, UserSessionHandler, ChatMessageHandler {

	private Context ctx;

	private JMSContext jmsContext;
	private Topic chatMessageTopic;

	private UserManagementRemote userManagement;
	private UserSessionRemote userSession;
	private UserMessageRemote userMessage;

	private static ServiceHandlerImpl instance;

	private ServiceHandlerImpl() {
		try {
			ctx = new InitialContext();

			userManagement = (UserManagementRemote) ctx
					.lookup("java:global/ChatClient-ear/ChatClient-ejb/UserManagementBean!de.ts.chat.server.beans.interfaces.UserManagementRemote");
			userSession = (UserSessionRemote) ctx
					.lookup("java:global/ChatClient-ear/ChatClient-ejb/UserSessionBean!de.ts.chat.server.beans.interfaces.UserSessionRemote");
			userMessage = (UserMessageRemote) ctx
					.lookup("java:global/ChatClient-ear/ChatClient-ejb/UserMessageBean!de.ts.chat.server.beans.interfaces.UserMessageRemote");
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
			jmsContext.createConsumer(chatMessageTopic)
					.setMessageListener(this);
		} catch (NamingException e) {
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
		userMessage.sendMessage(userSession.getUserName(), message);
	}

	@Override
	public void onMessage(Message message) {
		try {
			if (!message.getJMSDestination().equals(chatMessageTopic)) {
				return;
			}

			int chatMessageType = message.getIntProperty("CHATMESSAGE_TYPE");

			String sender = message.getStringProperty("CHATMESSAGE_SENDER");
			String text = message.getStringProperty("CHATMESSAGE_TEXT");
			Date date = new Date(message.getJMSDeliveryTime());

			if (chatMessageType == ChatMessageType.TEXT.ordinal()) {
				setChanged();

				ChatMessage chatMessage = new ChatMessage(ChatMessageType.TEXT,
						sender, text, date);
				notifyObservers(chatMessage);
			}

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
