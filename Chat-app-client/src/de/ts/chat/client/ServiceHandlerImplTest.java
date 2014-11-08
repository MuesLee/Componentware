package de.ts.chat.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;

import org.junit.BeforeClass;
import org.junit.Test;

import de.ts.chat.server.beans.interfaces.CommonStatisticManagementRemote;
import de.ts.chat.server.beans.interfaces.UserManagementRemote;
import de.ts.chat.server.beans.interfaces.UserSessionRemote;
import de.ts.chat.server.beans.interfaces.UserStatisticManagementRemote;

public class ServiceHandlerImplTest {

	private static UserManagementRemote userManagementMock;
	private static UserSessionRemote userSessionMock;
	private static UserStatisticManagementRemote userStatisticMock;
	private static CommonStatisticManagementRemote commonStatisticMock;

	private static ServiceHandlerImpl classUnderTest;

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

	}

	@Test
	public void testMeanWordsFilter() {

		String given = "Fuck Baum Muschi Kartoffel";
		String expected = "**** Baum **** Kartoffel";
		String actual = classUnderTest.cleanUpMessage(given);
		assertEquals(expected, actual);

	}

}
