package org.uncommons.reportng.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;
import org.uncommons.reportng.HTMLReporter;

public class IAnnotationTransformerListener implements IAnnotationTransformer {
	
	public long defaultTestTimeout = 600000;
	
	@Override
	public void transform(ITestAnnotation annotation, @SuppressWarnings("rawtypes") Class testClass, @SuppressWarnings("rawtypes") Constructor testConstructor, Method testMethod) {
		// Retry
		Class<? extends IRetryAnalyzer> retry = annotation.getRetryAnalyzerClass();
		if (retry == null) {
			annotation.setRetryAnalyzer(Retry.class);
		}
		// Test Time Out
		try {
			String timeout = System.getProperty(HTMLReporter.TEST_TIMEOUT);
			defaultTestTimeout = Long.parseLong(timeout);
		} catch (Exception ex) {
			
		}
		if (defaultTestTimeout > 0) {
			annotation.setTimeOut(defaultTestTimeout);
		}
	}
	
}
