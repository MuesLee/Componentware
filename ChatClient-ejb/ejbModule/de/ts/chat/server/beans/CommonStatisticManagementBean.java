package de.ts.chat.server.beans;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private static final Logger log = Logger
			.getLogger(CommonStatisticManagementBean.class.getName());

	private static final String COMMON_STATISTC_SENDER_TIMER = "Common Statistc Sender Timer";

	@Inject
	private JMSContext jmsContext;

	@Resource
	private TimerService timerService;

	@PersistenceContext
	private EntityManager entityManager;

	public CommonStatisticManagementBean() {
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	void createTimer() {
		boolean timerActive = false;

		Collection<Timer> timers = timerService.getAllTimers();
		for (Timer timer : timers) {
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

		log.info("Halbstuendlicher Stat-Versand beginnt: "
				+ initStartCal.getTime() + "");
		Timer createdIntervalTimer = timerService.createIntervalTimer(
				initStartCal.getTime(), intervalOneHour, timerConfig);

		log.info("Timer: " + createdIntervalTimer.getInfo()
				+ " wurde erstellt und wird dann klingeln: "
				+ createdIntervalTimer.getNextTimeout());

		String timerInfo = "";
		timers = timerService.getTimers();
		for (Timer timer : timers) {
			timerInfo = timerInfo + "Timer: " + timer.getInfo() + " klingelt: "
					+ timer.getNextTimeout() + "\n";
		}

		log.info("Aktuell sind " + timers.size() + " Timer aktiv:\n"
				+ timerInfo);
	}

	@Schedule(minute = "0", hour = "*")
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void aggregateCurrentStatistic() {
		CommonStatistic currentStatistic = getCurrentStatistic();
		Calendar cal = new GregorianCalendar();
		currentStatistic = entityManager.merge(currentStatistic);
		entityManager.lock(currentStatistic, LockModeType.PESSIMISTIC_WRITE);

		currentStatistic.setEndDate(cal.getTime());

		entityManager.merge(currentStatistic);
		entityManager.flush();
		sendStatistics(currentStatistic);

		currentStatistic = new CommonStatistic();
		entityManager.persist(currentStatistic);
		entityManager.flush();
		log.info("Allgemeine Statistiken wurden zusammengefasst und eine neue Statistik wurde angelegt.");
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
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
			entityManager.persist(currentStatistic);
			log.info("Es existierte bisher keine allgemeine Statistik. Eine neue wurde angelegt.");
			;
		} catch (NonUniqueResultException e) {
			List<CommonStatistic> resultList = createNamedQuery.getResultList();
			for (CommonStatistic commonStatistic : resultList) {
				entityManager.persist(commonStatistic);
				commonStatistic.setEndDate(new GregorianCalendar().getTime());
			}

			currentStatistic = new CommonStatistic();
			entityManager.persist(currentStatistic);
			entityManager.flush();
			log.log(Level.SEVERE,
					"Es existierten mehr als eine aktuelle allgemeine Statistik. Sie wurden alle beendet und eine neue angelegt.");
		}

		log.log(Level.FINER,
				"Die aktuellste Commonstatistik wurde aus der DB geladen.");
		return currentStatistic;
	}

	@Timeout
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void timeout(Timer timer) {

		if (!timer.getInfo().equals(COMMON_STATISTC_SENDER_TIMER)) {
			return;
		}
		log.info("Timeout called!");

		sendStatistics(getCurrentStatistic());

	}

	private void sendStatistics(CommonStatistic commonStatistic) {
		try {
			InitialContext ctx = new InitialContext();
			Topic chatMessageTopic = (Topic) ctx
					.lookup("java:global/jms/ChatMessageTopic");

			String messageText = getMessageTextForCurrentStatistic(commonStatistic);

			Message message = jmsContext.createMessage();
			message.setIntProperty("CHATMESSAGE_TYPE",
					ChatMessageType.STATISTIC.ordinal());
			message.setStringProperty("CHATMESSAGE_SENDER", "SYSTEM");
			message.setStringProperty("CHATMESSAGE_TEXT", messageText);
			message.setJMSDeliveryMode(Message.DEFAULT_DELIVERY_MODE);

			jmsContext.createProducer().send(chatMessageTopic, message);
			log.log(Level.FINER,
					"Die aktuelle Statistik wurde an die Clients verteilt.");
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getMessageTextForCurrentStatistic(
			CommonStatistic commonStatistic) {

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

		Date startDate = commonStatistic.getStartingDate();
		Date endDate = commonStatistic.getEndDate();
		if (endDate == null) {
			Calendar cal = new GregorianCalendar();
			endDate = cal.getTime();
		}

		String text = "Statistik von " + sdf.format(startDate) + " Uhr bis "
				+ sdf.format(endDate) + " Uhr\nAnzahl der Anmeldungen: "
				+ commonStatistic.getLogins() + "\nAnzahl der Abmeldungen: "
				+ commonStatistic.getLogouts()
				+ "\nAnzahl der geschriebenen Nachrichten: "
				+ commonStatistic.getLogins();

		return text;
	}

	public List<CommonStatistic> getCommonStatistics() {

		TypedQuery<CommonStatistic> createNamedQuery = entityManager
				.createNamedQuery("getAllCommonStatistics",
						CommonStatistic.class);

		List<CommonStatistic> resultList = createNamedQuery.getResultList();

		return resultList;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void userHasSendAMessage() {

		CommonStatistic currentStatistic = getCurrentStatistic();

		int messages = currentStatistic.getMessages();
		currentStatistic.setMessages(++messages);

		entityManager.merge(currentStatistic);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void userHasLoggedIn() {
		CommonStatistic currentStatistic = getCurrentStatistic();
		int logins = currentStatistic.getLogins();

		currentStatistic.setLogins(++logins);
		entityManager.merge(currentStatistic);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void userHasLoggedOut() {

		CommonStatistic currentStatistic = getCurrentStatistic();

		int logouts = currentStatistic.getLogouts();
		currentStatistic.setLogouts(++logouts);
		entityManager.merge(currentStatistic);
	}
}
