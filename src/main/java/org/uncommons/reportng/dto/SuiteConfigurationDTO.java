package org.uncommons.reportng.dto;

import java.util.Set;

import org.testng.ITestResult;

public class SuiteConfigurationDTO {
	
	private Set<ITestResult> before;
	private Set<ITestResult> after;
	
	public SuiteConfigurationDTO() {
	}
	
	public SuiteConfigurationDTO(Set<ITestResult> before, Set<ITestResult> after) {
		setBefore(before);
		setAfter(after);
	}
	
	public Set<ITestResult> getBefore() {
		return before;
	}
	
	public void setBefore(Set<ITestResult> before) {
		this.before = before;
	}
	
	public Set<ITestResult> getAfter() {
		return after;
	}
	
	public void setAfter(Set<ITestResult> after) {
		this.after = after;
	}
}
