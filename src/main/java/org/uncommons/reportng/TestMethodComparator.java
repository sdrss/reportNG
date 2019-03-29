package org.uncommons.reportng;

import java.util.Comparator;

import org.testng.ITestNGMethod;

/**
 * Comparator for sorting TestNG test methods. Sorts method alphabeticaly (first
 * by fully-qualified class name, then by method name).
 */
class TestMethodComparator implements Comparator<ITestNGMethod> {
	@Override
	public int compare(ITestNGMethod method1, ITestNGMethod method2) {
		int compare = Long.toString(method1.getDate()).compareTo(Long.toString(method2.getDate()));
		if (compare == 0) {
			compare = method1.getMethodName().compareTo(method2.getMethodName());
		}
		return compare;
	}
}