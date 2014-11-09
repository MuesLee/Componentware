package de.ts.chat.client;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;

import org.junit.Before;
import org.mockito.Mockito;

public class ChatTestCase {

	protected static JMSContext jmsContextMock;
	protected static Queue chatMessageQueueMock;
	protected static Topic chatMessageTopicMock;

	protected static Message messageMock;
	protected static JMSProducer producerMock;

	protected static JMSConsumer consumerMock;

	@Before
	public void beforeMethods() {
		messageMock = mock(Message.class);
		producerMock = mock(JMSProducer.class);
		consumerMock = mock(JMSConsumer.class);

		if (jmsContextMock != null) {
			doReturn(messageMock).when(jmsContextMock).createMessage();
			doReturn(producerMock).when(jmsContextMock).createProducer();
			doReturn(consumerMock).when(jmsContextMock).createConsumer(
					Mockito.any(Destination.class), Mockito.anyString());

		}

		doReturn(producerMock).when(producerMock).send(chatMessageQueueMock,
				messageMock);
		doReturn(producerMock).when(producerMock).send(chatMessageTopicMock,
				messageMock);

	}

}
