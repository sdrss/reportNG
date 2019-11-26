package org.uncommons.reportng.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.uncommons.reportng.HTMLReporter;
import org.uncommons.reportng.annotations.KnownDefect;

public class Retry implements IRetryAnalyzer {
	
	private int retryCount = 1;
	
	@Override
	public boolean retry(ITestResult result) {
		int maxRetryCount = 2;
		try {
			maxRetryCount = Integer.parseInt(System.getProperty(HTMLReporter.TEST_MAX_RETRY_COUNT));
		} catch (Exception ex) {
			// logger.debug("", ex);
		}
		if (result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(KnownDefect.class) == null && retryCount < maxRetryCount && maxRetryCount > 0) {
			retryCount++;
			result.getTestContext().getFailedTests().removeResult(result.getMethod());
			result.getTestContext().getSkippedTests().removeResult(result.getMethod());
			Reporter.log("Retry #" + retryCount + " for test: " + result.getMethod().getMethodName() + ", on thread: " + Thread.currentThread().getName());
			return true;
		}
		return false;
	}
}
