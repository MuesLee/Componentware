package de.ts.chat.server.beans;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessageType;
import de.ts.chat.server.beans.interfaces.UserMessageLocal;
import de.ts.chat.server.beans.interfaces.UserMessageRemote;

@MessageDriven(mappedName = "java:global/jms/ChatMessageTopic", activationConfig = { @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic") })
public class UserMessageBean implements UserMessageLocal, UserMessageRemote,
		MessageListener {

	@Inject
	private JMSContext jmsContext;

	@Resource(lookup = "java:global/jms/ChatMessageTopic")
	private Topic chatMessageTopic;

	@Override
	public void sendMessage(String user, String messageText) {
		sendMessage(user, messageText, ChatMessageType.TEXT);
	}

	@Override
	public void sendMessage(String user, String messageText,
			ChatMessageType messageType) {

		String sender = null;

		notifyViaChatMessageTopic(messageText, messageType, sender);
	}

	private void notifyViaChatMessageTopic(String messageText,
			ChatMessageType messageType, String sender) {

		try {
			Message message = jmsContext.createMessage();
			message.setIntProperty("CHATMESSAGE_TYPE", messageType.ordinal());
			message.setStringProperty("CHATMESSAGE_SENDER", sender);
			message.setStringProperty("CHATMESSAGE_TEXT", messageText);
			message.setJMSDeliveryMode(Message.DEFAULT_DELIVERY_MODE);
			jmsContext.createProducer().send(chatMessageTopic, message);
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onMessage(Message message) {
		// TextMessage textMessage = (TextMessage) message;
		// textMessage.getText();

	}
}
