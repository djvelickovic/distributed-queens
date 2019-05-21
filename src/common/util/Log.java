package common.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

	enum Level {
		DEBUG(-1),
		INFO(0),
		WARN(1),
		ERROR(2);

		private int priority;

		Level(int priority) {
			this.priority = priority;
		}

		public int priority() {
			return priority;
		}
	}

	private static Level level = Level.INFO;

	public static void debug(String message) {
		if (level.priority() > Level.DEBUG.priority()){
			return;
		}

		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();

		System.out.println(timeFormat.format(now) + " - DEBUG - " + message);
	}

	public static void info(String message) {
		if (level.priority() > Level.INFO.priority()){
			return;
		}

		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.out.println(timeFormat.format(now) + " - INFO  - " + message);
	}

	public static void warn(String message) {
		if (level.priority() > Level.WARN.priority()){
			return;
		}

		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();

		System.out.println(timeFormat.format(now) + " -  WARN  - " + message);
	}

	public static void error(String message) {
		if (level.priority() > Level.ERROR.priority()){
			return;
		}

		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.out.println(timeFormat.format(now) + " - ERROR - " + message);
	}

	public static void stdout(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();

		System.out.println(timeFormat.format(now) + " - STDOUT - " + message);
	}
}
