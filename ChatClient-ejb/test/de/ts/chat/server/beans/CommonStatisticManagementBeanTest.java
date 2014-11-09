package de.ts.chat.server.beans;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jms.JMSContext;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fh_dortmund.inf.cw.chat.server.entities.CommonStatistic;
import de.ts.chat.client.ChatTestCase;

public class CommonStatisticManagementBeanTest extends ChatTestCase {

	private static CommonStatisticManagementBean classUnderTest;

	private static EntityManager entityManagerMock;

	private static CommonStatistic currentStatistic;

	@BeforeClass
	public static void setUp() throws IllegalArgumentException,
			IllegalAccessException, NoSuchFieldException, SecurityException {

		classUnderTest = new CommonStatisticManagementBean();

		jmsContextMock = mock(JMSContext.class);
		entityManagerMock = mock(EntityManager.class);

		Field f1 = classUnderTest.getClass().getDeclaredField("jmsContext");
		f1.setAccessible(true);
		f1.set(classUnderTest, jmsContextMock);

		Field f2 = classUnderTest.getClass().getDeclaredField("entityManager");
		f2.setAccessible(true);
		f2.set(classUnderTest, entityManagerMock);
	}

	@Override
	public void beforeMethods() {
		super.beforeMethods();

		currentStatistic = new CommonStatistic();
		@SuppressWarnings("unchecked")
		TypedQuery<CommonStatistic> commonStatisticsQueryMock = mock(TypedQuery.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CommonStatistic> currentStatisticQueryMock = mock(TypedQuery.class);

		doReturn(commonStatisticsQueryMock).when(entityManagerMock)
				.createNamedQuery("getAllCommonStatistics",
						CommonStatistic.class);
		doReturn(currentStatisticQueryMock).when(entityManagerMock)
				.createNamedQuery("getCurrentCommonStatistic",
						CommonStatistic.class);

		List<CommonStatistic> expected = Collections
				.singletonList(currentStatistic);
		doReturn(expected).when(commonStatisticsQueryMock).getResultList();
		doReturn(currentStatistic).when(currentStatisticQueryMock)
				.getSingleResult();

	}

	@Test
	public void testGetCurrentStatisticEmptyDB() throws Exception {
		currentStatistic = null;
		CommonStatistic actual = classUnderTest.getCurrentStatistic();
		assertNotNull(actual);
	}

	@Test
	public void testGetCurrentStatisticMoreThanOneInDB() throws Exception {

		List<CommonStatistic> given = new ArrayList<>();
		given.add(new CommonStatistic());
		given.add(new CommonStatistic());

		@SuppressWarnings("unchecked")
		TypedQuery<CommonStatistic> currentStatisticQueryFailMock = mock(TypedQuery.class);
		doReturn(currentStatisticQueryFailMock).when(entityManagerMock)
				.createNamedQuery("getCurrentCommonStatistic",
						CommonStatistic.class);
		doThrow(NonUniqueResultException.class).when(
				currentStatisticQueryFailMock).getSingleResult();
		doReturn(given).when(currentStatisticQueryFailMock).getResultList();

		CommonStatistic actual = classUnderTest.getCurrentStatistic();

		for (CommonStatistic commonStatistic : given) {
			assertNotNull(commonStatistic.getEndDate());
		}

		assertNull(actual.getEndDate());

	}

	@Test
	public void testGetCurrentStatistic() throws Exception {
		CommonStatistic actual = classUnderTest.getCurrentStatistic();
		assertEquals(currentStatistic, actual);
	}

	@Test
	public void testUserHasLoggedIn() throws Exception {
		classUnderTest.userHasLoggedIn();
		CommonStatistic actualList = classUnderTest.getCurrentStatistic();

		verify(entityManagerMock).merge(currentStatistic);

		assertEquals(1, actualList.getLogins());
	}

	@Test
	public void testUserHasLoggedOut() throws Exception {
		classUnderTest.userHasLoggedOut();
		CommonStatistic actualList = classUnderTest.getCurrentStatistic();
		verify(entityManagerMock).merge(currentStatistic);
		assertEquals(1, actualList.getLogouts());
	}

	@Test
	public void testUserHasSendAMessage() throws Exception {
		classUnderTest.userHasSendAMessage();
		CommonStatistic actualList = classUnderTest.getCurrentStatistic();
		verify(entityManagerMock).merge(currentStatistic);
		assertEquals(1, actualList.getMessages());
	}

	@Test
	public void testGetCommonStatistics() throws Exception {

		List<CommonStatistic> actual = classUnderTest.getCommonStatistics();

		assertEquals(Collections.singletonList(currentStatistic), actual);

	}
}
