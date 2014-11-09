package de.ts.chat.server.beans;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;

import org.junit.BeforeClass;
import org.junit.Test;

import de.ts.chat.client.ChatTestCase;
import de.ts.chat.server.beans.exception.InvalidLoginException;
import de.ts.server.beans.entities.ChatUser;

public class UserManagementBeanTest extends ChatTestCase {

	private static UserManagementBean classUnderTest;
	private static EntityManager entityManagerMock;

	private static String userName = "Derp";
	private static String password = "123";

	@BeforeClass
	public static void setUp() throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		classUnderTest = new UserManagementBean();

		entityManagerMock = mock(EntityManager.class);
		Field f1 = classUnderTest.getClass().getDeclaredField("entityManager");
		f1.setAccessible(true);
		f1.set(classUnderTest, entityManagerMock);
	}

	@Test(expected = InvalidLoginException.class)
	public void testLoginWrongPW() throws Exception {
		String hash = UserSessionBean.generateHash(password + "asdf");
		ChatUser expected = new ChatUser(userName, hash);
		doReturn(expected).when(entityManagerMock).find(ChatUser.class,
				userName);
		classUnderTest.login(userName, password);

	}

	@Test(expected = InvalidLoginException.class)
	public void testLoginWrongUsername() throws Exception {
		doReturn(null).when(entityManagerMock).find(ChatUser.class, userName);
		classUnderTest.login(userName, password);

	}

	@Test(expected = Exception.class)
	public void testRegisterUsernameTaken() throws Exception {
		doReturn(new ChatUser(userName, "1asd2ads3")).when(entityManagerMock)
				.find(ChatUser.class, userName);
		classUnderTest.register(userName, password);
	}

	@Test
	public void testValidLogin() throws Exception {

		String hash = UserSessionBean.generateHash(password);
		ChatUser expected = new ChatUser(userName, hash);
		doReturn(expected).when(entityManagerMock).find(ChatUser.class,
				userName);

		ChatUser actual = classUnderTest.login(userName, password);

		assertEquals(expected, actual);
	}

}
