package de.ts.chat.server.beans;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import de.fh_dortmund.inf.cw.chat.server.entities.CommonStatistic;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessageType;
import de.ts.chat.server.beans.interfaces.CommonStatisticManagementLocal;
import de.ts.chat.server.beans.interfaces.CommonStatisticManagementRemote;

@Stateless
public class CommonStatisticManagementBean implements
		CommonStatisticManagementLocal, CommonStatisticManagementRemote {

	private static final String COMMON_STATISTC_SENDER_TIMER = "Common Statistc Sender Timer";

	@Inject
	private JMSContext jmsContext;

	@Resource
	private TimerService timerService;

	@PersistenceContext
	private EntityManager entityManager;

	public CommonStatisticManagementBean() {
	}

	private void createTimer() {
		boolean timerActive = false;

		for (Timer timer : timerService.getTimers()) {
			if (timer.getInfo() == COMMON_STATISTC_SENDER_TIMER) {
				timerActive = true;
				break;
			}
		}

		if (timerActive) {
			return;
		}

		TimerConfig timerConfig = new TimerConfig();
		timerConfig.setInfo(COMMON_STATISTC_SENDER_TIMER);
		timerConfig.setPersistent(true);
		Calendar initStartCal = new GregorianCalendar();
		int min = initStartCal.get(Calendar.MINUTE);
		if (min < 30) {
			min = 30 - min;
		} else {
			min = 60 - (min - 30);
		}
		initStartCal.set(Calendar.SECOND, 0);
		initStartCal.add(Calendar.MINUTE, min);

		final long intervalOneHour = 1000 * 60 * 60;

		System.out
				.println("############# Halbstuendlicher Stat-Versand beginnt: "
						+ initStartCal.getTime() + "");
		timerService.createIntervalTimer(initStartCal.getTime(),
				intervalOneHour, timerConfig);
	}

	@Schedule(minute = "0", hour = "*")
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void aggregateCurrentStatistic() {
		CommonStatistic currentStatistic = getCurrentStatistic();
		entityManager.lock(currentStatistic, LockModeType.PESSIMISTIC_WRITE);
		Calendar cal = new GregorianCalendar();

		currentStatistic.setEndDate(cal.getTime());

		entityManager.merge(currentStatistic);
		sendStatistics(null);

		currentStatistic = new CommonStatistic();
		entityManager.persist(currentStatistic);
		entityManager.flush();
	}

	public CommonStatistic getCurrentStatistic() {

		TypedQuery<CommonStatistic> createNamedQuery = entityManager
				.createNamedQuery("getCurrentCommonStatistic",
						CommonStatistic.class);

		CommonStatistic currentStatistic = null;

		try {
			currentStatistic = createNamedQuery.getSingleResult();
		} catch (NoResultException e) {
			createTimer();
			currentStatistic = new CommonStatistic();
		} catch (NonUniqueResultException e) {
			List<CommonStatistic> resultList = createNamedQuery.getResultList();
			for (CommonStatistic commonStatistic : resultList) {
				commonStatistic.setEndDate(new GregorianCalendar().getTime());
				entityManager.merge(commonStatistic);
			}

			currentStatistic = new CommonStatistic();
			entityManager.persist(currentStatistic);
			entityManager.flush();
		}
		return currentStatistic;
	}

	@Timeout
	public void sendStatistics(Timer timer) {
		if (timer != null) {
			if (timer.getInfo() != COMMON_STATISTC_SENDER_TIMER) {
				return;
			}
		}

		try {
			InitialContext ctx = new InitialContext();
			Topic chatMessageTopic = (Topic) ctx
					.lookup("java:global/jms/ChatMessageTopic");

			String messageText = getMessageTextForCurrentStatistic();

			Message message = jmsContext.createMessage();
			message.setIntProperty("CHATMESSAGE_TYPE",
					ChatMessageType.STATISTIC.ordinal());
			message.setStringProperty("CHATMESSAGE_SENDER", "SYSTEM");
			message.setStringProperty("CHATMESSAGE_TEXT", messageText);
			message.setJMSDeliveryMode(Message.DEFAULT_DELIVERY_MODE);

			jmsContext.createProducer().send(chatMessageTopic, message);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getMessageTextForCurrentStatistic() {

		CommonStatistic stat = getCurrentStatistic();

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

		Date startDate = stat.getStartingDate();
		Date endDate = stat.getEndDate();
		if (endDate == null) {
			Calendar cal = new GregorianCalendar();
			endDate = cal.getTime();
		}

		String text = "Statistik von " + sdf.format(startDate) + " bis "
				+ sdf.format(endDate) + "\nAnzahl der Anmeldungen: "
				+ stat.getLogins() + "\nAnzahl der Abmeldungen: "
				+ stat.getLogouts()
				+ "\nAnzahl der geschriebenen Nachrichten: " + stat.getLogins();

		return text;
	}

	public List<CommonStatistic> getCommonStatistics() {

		TypedQuery<CommonStatistic> createNamedQuery = entityManager
				.createNamedQuery("getAllCommonStatistics",
						CommonStatistic.class);

		List<CommonStatistic> resultList = createNamedQuery.getResultList();

		return resultList;
	}

	public void userHasSendAMessage() {

		CommonStatistic currentStatistic = getCurrentStatistic();

		int messages = currentStatistic.getMessages();
		currentStatistic.setMessages(++messages);

		entityManager.merge(currentStatistic);
	}

	@Override
	public void userHasLoggedIn() {

		CommonStatistic currentStatistic = getCurrentStatistic();

		int logins = currentStatistic.getLogins();

		currentStatistic.setLogins(++logins);
		entityManager.merge(currentStatistic);

	}

	@Override
	public void userHasLoggedOut() {

		CommonStatistic currentStatistic = getCurrentStatistic();

		int logouts = currentStatistic.getLogouts();
		currentStatistic.setLogouts(++logouts);
		entityManager.merge(currentStatistic);
	}
}
