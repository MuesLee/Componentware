package de.ts.chat.server.beans;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;

import javax.jms.JMSContext;
import javax.persistence.EntityManager;

import org.junit.BeforeClass;

import de.ts.chat.client.ChatTestCase;

public class CommonStatisticManagementBeanTest extends ChatTestCase {

	private static CommonStatisticManagementBean classUnderTest;

	private static EntityManager entityManagerMock;

	@BeforeClass
	public static void setUp() throws IllegalArgumentException,
			IllegalAccessException, NoSuchFieldException, SecurityException {

		jmsContextMock = mock(JMSContext.class);
		entityManagerMock = mock(EntityManager.class);

		Field f1 = classUnderTest.getClass().getDeclaredField("jmsContext");
		f1.setAccessible(true);
		f1.set(classUnderTest, jmsContextMock);

		Field f2 = classUnderTest.getClass().getDeclaredField("entityManager");
		f2.setAccessible(true);
		f2.set(classUnderTest, entityManagerMock);
	}

}
