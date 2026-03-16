package org.uncommons.reportng.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;
import org.uncommons.reportng.HTMLReporter;

public class IAnnotationTransformerListener implements IAnnotationTransformer {
	
	private final long defaultTestTimeout;

	public IAnnotationTransformerListener() {
		long timeout = 600000;
		String prop = System.getProperty(HTMLReporter.TEST_TIMEOUT);
		if (prop != null && !prop.isEmpty()) {
			try {
				timeout = Long.parseLong(prop);
			} catch (NumberFormatException ex) {
				// keep default
			}
		}
		this.defaultTestTimeout = timeout;
	}

	@Override
	public void transform(ITestAnnotation annotation, @SuppressWarnings("rawtypes") Class testClass, @SuppressWarnings("rawtypes") Constructor testConstructor, Method testMethod) {
		// Retry
		Class<? extends IRetryAnalyzer> retry = annotation.getRetryAnalyzerClass();
		if (retry == null) {
			annotation.setRetryAnalyzer(Retry.class);
		}
		// Test Time Out
		if (defaultTestTimeout > 0) {
			annotation.setTimeOut(defaultTestTimeout);
		}
	}
	
}
