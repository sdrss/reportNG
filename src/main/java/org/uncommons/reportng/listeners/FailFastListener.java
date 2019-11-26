package org.uncommons.reportng.listeners;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.uncommons.reportng.HTMLReporter;
import org.uncommons.reportng.annotations.KnownDefect;

public class FailFastListener extends TestListenerAdapter {
	
	@Override
	public void onTestFailure(ITestResult iTestResult) {
		if (iTestResult.getMethod().getConstructorOrMethod().getMethod().getAnnotation(KnownDefect.class) == null) {
			System.setProperty(HTMLReporter.SKIP_EXECUTION, "true");
		}
	}
}
