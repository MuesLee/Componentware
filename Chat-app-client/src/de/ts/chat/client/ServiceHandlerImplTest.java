package de.ts.chat.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.Queue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import de.fh_dortmund.inf.cw.chat.server.entities.UserStatistic;
import de.ts.chat.server.beans.interfaces.CommonStatisticManagementRemote;
import de.ts.chat.server.beans.interfaces.UserManagementRemote;
import de.ts.chat.server.beans.interfaces.UserSessionRemote;
import de.ts.chat.server.beans.interfaces.UserStatisticManagementRemote;

public class ServiceHandlerImplTest {

	private static ServiceHandlerImpl classUnderTest;

	private static UserManagementRemote userManagementMock;
	private static UserSessionRemote userSessionMock;
	private static UserStatisticManagementRemote userStatisticMock;
	private static CommonStatisticManagementRemote commonStatisticMock;

	private static JMSContext jmsContextMock;
	private static Destination chatMessageQueueMock;

	@BeforeClass
	public static void setUp() throws IllegalArgumentException,
			IllegalAccessException, NoSuchFieldException, SecurityException {
		classUnderTest = ServiceHandlerImpl.getInstance();

		userManagementMock = mock(UserManagementRemote.class);
		Field f1 = classUnderTest.getClass().getDeclaredField("userManagement");
		f1.setAccessible(true);
		f1.set(classUnderTest, userManagementMock);

		userSessionMock = mock(UserSessionRemote.class);
		Field f2 = classUnderTest.getClass().getDeclaredField("userSession");
		f2.setAccessible(true);
		f2.set(classUnderTest, userSessionMock);
		userStatisticMock = mock(UserStatisticManagementRemote.class);
		Field f3 = classUnderTest.getClass().getDeclaredField("userStatistic");
		f3.setAccessible(true);
		f3.set(classUnderTest, userStatisticMock);
		commonStatisticMock = mock(CommonStatisticManagementRemote.class);
		Field f4 = classUnderTest.getClass()
				.getDeclaredField("commonStatistic");
		f4.setAccessible(true);
		f4.set(classUnderTest, commonStatisticMock);

		jmsContextMock = mock(JMSContext.class);
		Field f5 = classUnderTest.getClass().getDeclaredField("jmsContext");
		f5.setAccessible(true);
		f5.set(classUnderTest, jmsContextMock);

		chatMessageQueueMock = mock(Queue.class);
		Field f6 = classUnderTest.getClass().getDeclaredField(
				"chatMessageQueue");
		f6.setAccessible(true);
		f6.set(classUnderTest, chatMessageQueueMock);

	}

	@Test
	public void testMeanWordsFilter() {

		String given = "Fuck Baum Muschi Kartoffel";
		String expected = "**** Baum **** Kartoffel";
		String actual = classUnderTest.cleanUpMessage(given);
		assertEquals(expected, actual);
	}

	@Test
	public void testLogin() throws Exception {
		String userName = "Derp";
		String password = "123";

		classUnderTest.login(userName, password);

		Message messageMock = mock(Message.class);
		Mockito.doReturn(messageMock).when(jmsContextMock).createMessage();

		verify(userSessionMock).login(userName, password);
		verify(jmsContextMock).createProducer().send(chatMessageQueueMock,
				messageMock);
		verify(userStatisticMock).userHasLoggedIn(userName);
		verify(commonStatisticMock).userHasLoggedIn();
	}

	@Test
	public void testGetUserStatisticForDerp() throws Exception {

		UserStatistic expected = new UserStatistic();
		Mockito.doReturn("Derp").when(userSessionMock).getUserName();
		Mockito.doReturn(expected).when(userStatisticMock)
				.getStatisticForUser("Derp");
		UserStatistic actual = classUnderTest.getUserStatistic();
		assertEquals(expected, actual);

	}

	@Test
	public void testGetNumberOfRegisteredUsers() throws Exception {

		int expected = 2;
		Mockito.doReturn(expected).when(userManagementMock)
				.getNumberOfRegisteredUsers();
		int actual = classUnderTest.getNumberOfRegisteredUsers();
		assertEquals(expected, actual);

	}

	@Test
	public void testGetOnlineUser() throws Exception {
		List<String> expected = Collections.singletonList("Derp");
		Mockito.doReturn(expected).when(userManagementMock).getOnlineUsers();
		List<String> actual = classUnderTest.getOnlineUsers();
		assertEquals(expected, actual);
	}

	@Test
	public void testGetNumberOfOnlineUser() throws Exception {
		int expected = 1;
		Mockito.doReturn(expected).when(userManagementMock)
				.getNumberOfOnlineUsers();
		int actual = classUnderTest.getNumberOfOnlineUsers();
		assertEquals(expected, actual);
	}

}
