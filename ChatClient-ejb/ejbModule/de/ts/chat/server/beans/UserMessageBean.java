package de.ts.chat.server.beans;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@MessageDriven(mappedName = "java:global/jms/ChatMessageQueue", activationConfig = { @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), })
public class UserMessageBean implements MessageListener {

	@Inject
	private JMSContext jmsContext;

	@Override
	public void onMessage(Message message) {
		postToChatMessageTopic(message);
	}

	private void postToChatMessageTopic(Message message) {

		try {
			InitialContext ctx = new InitialContext();
			Topic chatMessageTopic = (Topic) ctx
					.lookup("java:global/jms/ChatMessageTopic");

			jmsContext.createProducer().send(chatMessageTopic, message);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
