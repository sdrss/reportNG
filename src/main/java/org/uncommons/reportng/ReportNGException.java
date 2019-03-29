package org.uncommons.reportng;

/**
 * Unchecked exception thrown when an unrecoverable error occurs during report
 * generation.
 */
public class ReportNGException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ReportNGException(String string) {
		super(string);
	}

	public ReportNGException(String string, Throwable throwable) {
		super(string, throwable);
	}
}