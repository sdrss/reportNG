package org.uncommons.reportng;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Provides access to static information useful when generating a report.
 */
public final class ReportMetadata {

	static final String PROPERTY_KEY_PREFIX = "org.uncommons.reportng.";
	static final String TITLE_KEY = PROPERTY_KEY_PREFIX + "title";
	static final String ARGUMENTS_KEY = PROPERTY_KEY_PREFIX + "arguments";
	static final String DEFAULT_TITLE = "Test Results Report";
	static final String COVERAGE_KEY = PROPERTY_KEY_PREFIX + "coverage-report";
	static final String EXCEPTIONS_KEY = PROPERTY_KEY_PREFIX + "show-expected-exceptions";
	static final String OUTPUT_KEY = PROPERTY_KEY_PREFIX + "escape-output";
	static final String XML_DIALECT_KEY = PROPERTY_KEY_PREFIX + "xml-dialect";
	static final String LOCALE_KEY = PROPERTY_KEY_PREFIX + "locale";
	static final String VELOCITY_LOG_KEY = PROPERTY_KEY_PREFIX + "velocity-log";
	static final String LOGOUTPUTREPORT = PROPERTY_KEY_PREFIX + "logOutputReport";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEEE dd MMMM yyyy");
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm z");

	/**
	 * The date/time at which this report is being generated.
	 */
	private final Date reportTime = new Date();

	/**
	 * @return A String representation of the report date.
	 * @see #getReportTime()
	 */
	public String getReportDate() {
		return DATE_FORMAT.format(reportTime);
	}

	/**
	 * @return A String representation of the report time.
	 * @see #getReportDate()
	 */
	public String getReportTime() {
		return TIME_FORMAT.format(reportTime);
	}

	public String getReportTitle() {
		return System.getProperty(TITLE_KEY, DEFAULT_TITLE);
	}

	public String getArguments() {
		return System.getProperty(ARGUMENTS_KEY, DEFAULT_TITLE);
	}

	/**
	 * @return The URL (absolute or relative) of an HTML coverage report
	 *         associated with the test run. Null if there is no coverage
	 *         report.
	 */
	public String getCoverageLink() {
		return System.getProperty(COVERAGE_KEY);
	}

	/**
	 * Returns false (the default) if stack traces should not be shown for
	 * expected exceptions.
	 * 
	 * @return True if stack traces should be shown even for expected
	 *         exceptions, false otherwise.
	 */
	public boolean shouldShowExpectedExceptions() {
		return System.getProperty(EXCEPTIONS_KEY, "false").equalsIgnoreCase("true");
	}

	public boolean showLogOutPutReport() {
		return System.getProperty(LOGOUTPUTREPORT, "false").equalsIgnoreCase("true");
	}

	/**
	 * Returns true (the default) if log text should be escaped when displayed
	 * in a report. Turning off escaping allows you to do something link
	 * inserting link tags into HTML reports, but it also means that other
	 * output could accidentally corrupt the mark-up.
	 * 
	 * @return True if reporter log output should be escaped when displayed in a
	 *         report, false otherwise.
	 */
	public boolean shouldEscapeOutput() {
		return System.getProperty(OUTPUT_KEY, "true").equalsIgnoreCase("true");
	}

	public boolean allowSkippedTestsInXML() {
		return !System.getProperty(XML_DIALECT_KEY, "testng").equalsIgnoreCase("junit");
	}

	public boolean shouldGenerateVelocityLog() {
		return System.getProperty(VELOCITY_LOG_KEY, "false").equalsIgnoreCase("true");
	}

	public String getUser() throws UnknownHostException {
		String user = System.getProperty("user.name");
		String host = InetAddress.getLocalHost().getHostName();
		return user + '@' + host;
	}

	public String getJavaInfo() {
		return String.format("Java %s (%s)", System.getProperty("java.version"), System.getProperty("java.vendor"));
	}

	public String getPlatform() {
		return String.format("%s %s (%s)", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
	}

	public Locale getLocale() {
		if (System.getProperties().containsKey(LOCALE_KEY)) {
			String locale = System.getProperty(LOCALE_KEY);
			String[] components = locale.split("_", 3);
			switch (components.length) {
			case 1:
				return new Locale(locale);
			case 2:
				return new Locale(components[0], components[1]);
			case 3:
				return new Locale(components[0], components[1], components[2]);
			default:
				System.err.println("Invalid locale specified: " + locale);
			}
		}
		return Locale.getDefault();
	}
}