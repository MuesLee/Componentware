package de.ts.chat.server.beans;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import de.fh_dortmund.inf.cw.chat.server.entities.CommonStatistic;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessageType;
import de.ts.chat.server.beans.interfaces.CommonStatisticManagementLocal;
import de.ts.chat.server.beans.interfaces.CommonStatisticManagementRemote;

@Singleton
public class CommonStatisticManagementBean implements
		CommonStatisticManagementLocal, CommonStatisticManagementRemote {

	private static final String COMMON_STATISTC_SENDER_TIMER = "Common Statistc Sender Timer";
	private List<CommonStatistic> commonStatistics;
	private CommonStatistic currentStatistic;

	@Inject
	private JMSContext jmsContext;

	@Resource
	private TimerService timerService;

	@PostConstruct
	private void init() {
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
		Timer createIntervalTimer = timerService.createIntervalTimer(
				initStartCal.getTime(), intervalOneHour, timerConfig);
	}

	public CommonStatisticManagementBean() {

		this.commonStatistics = new ArrayList<>();

	}

	@Schedule(minute = "0", hour = "*")
	public void aggregateCurrentStatistic() {
		Calendar cal = new GregorianCalendar();

		currentStatistic.setEndDate(cal.getTime());
		commonStatistics.add(currentStatistic);
		sendStatistics(null);
		currentStatistic = new CommonStatistic();
		currentStatistic.setStartingDate(cal.getTime());
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

			String messageText = getMessageText(currentStatistic);

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

	private String getMessageText(CommonStatistic stat) {

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
		return commonStatistics;
	}

	public void setCommonStatistics(List<CommonStatistic> commonStatistics) {
		this.commonStatistics = commonStatistics;
	}

	public void userHasSendAMessage() {
		int messages = currentStatistic.getMessages();
		currentStatistic.setMessages(++messages);
	}

	@Override
	public void userHasLoggedIn() {

		if (currentStatistic == null) {
			currentStatistic = new CommonStatistic();
			Calendar cal = new GregorianCalendar();
			currentStatistic.setStartingDate(cal.getTime());
		}

		int logins = currentStatistic.getLogins();
		currentStatistic.setLogins(++logins);

	}

	@Override
	public void userHasLoggedOut() {
		int logouts = currentStatistic.getLogouts();
		currentStatistic.setLogouts(++logouts);

	}
}
