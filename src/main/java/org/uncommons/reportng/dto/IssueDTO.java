package org.uncommons.reportng.dto;

import org.uncommons.reportng.ReportNGUtils;

public class IssueDTO {

	private String suiteName;
	private String testName;
	private String testClass;
	private String issueDescription;
	private boolean regression;
	private String link;
	private String results;
	private ResultStatus status;
	private int totalNumberOfTests;

	public IssueDTO() {

	}

	public IssueDTO(String suiteName, String testName, String testClass, String issueDescription, String link, boolean regression) {
		setSuiteName(suiteName);
		setTestName(testName);
		setTestClass(testClass);
		setIssueDescription(ReportNGUtils.escapeString(issueDescription));
		setLink(link);
		setRegression(regression);
	}

	public IssueDTO(String suiteName, String testName, String testClass, String issueDescription, String link, String results, ResultStatus status,
			int totalNumberOfTests) {
		setSuiteName(suiteName);
		setTestName(testName);
		setTestClass(testClass);
		setIssueDescription(ReportNGUtils.escapeString(issueDescription));
		setLink(link);
		setResults(results);
		setStatus(status);
		setTotalNumberOfTests(totalNumberOfTests);
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getTestClass() {
		return testClass;
	}

	public void setTestClass(String testClass) {
		this.testClass = testClass;
	}

	public String getIssueDescription() {
		return issueDescription;
	}

	public void setIssueDescription(String issueDescription) {
		this.issueDescription = issueDescription;
	}

	public String getSuiteName() {
		return suiteName;
	}

	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getResults() {
		return results;
	}

	public void setResults(String results) {
		this.results = results;
	}

	public ResultStatus getStatus() {
		return status;
	}

	public void setStatus(ResultStatus status) {
		this.status = status;
	}

	public boolean isRegression() {
		return regression;
	}

	public void setRegression(boolean regression) {
		this.regression = regression;
	}

	public int getTotalNumberOfTests() {
		return totalNumberOfTests;
	}

	public void setTotalNumberOfTests(int totalNumberOfTests) {
		this.totalNumberOfTests = totalNumberOfTests;
	}

}
