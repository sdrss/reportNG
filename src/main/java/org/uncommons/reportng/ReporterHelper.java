package org.uncommons.reportng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.uncommons.reportng.annotations.KnownDefect;
import org.uncommons.reportng.dto.IssueDTO;
import org.uncommons.reportng.dto.IssuesDTO;
import org.uncommons.reportng.dto.ResultsDTO;

public class ReporterHelper {
	
	public static ResultsDTO checkAttribute(List<ISuite> suites) {
		ArrayList<Date> startDate = new ArrayList<Date>();
		ArrayList<Date> endDate = new ArrayList<Date>();
		ResultsDTO results = new ResultsDTO();
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> item = tempISuite.getResults().entrySet().iterator();
			while (item.hasNext()) {
				Entry<String, ISuiteResult> pair = item.next();
				ISuiteResult result = pair.getValue();
				startDate.add(result.getTestContext().getStartDate());
				endDate.add(result.getTestContext().getEndDate());
				if (ReportNGUtils.isRegression(result.getTestContext())) {
					results.setRegressionPass(results.getRegressionPass() + ReportNGUtils.getPassed(result.getTestContext()).size());
					results.setRegressionFail(results.getRegressionFail() + ReportNGUtils.getFailed(result.getTestContext()).size());
					results.setRegressionSkip(results.getRegressionSkip() + ReportNGUtils.getSkip(result.getTestContext()).size());
					results.setRegressionKnownDefect(results.getRegressionKnownDefect() + ReportNGUtils.getKnownDefect(result.getTestContext()).size());
					results.setRegressionFixed(results.getRegressionFixed() + ReportNGUtils.getFixed(result.getTestContext()).size());
				} else {
					results.setNewFeaturesPass(results.getNewFeaturesPass() + ReportNGUtils.getPassed(result.getTestContext()).size());
					results.setNewFeaturesFail(results.getNewFeaturesFail() + ReportNGUtils.getFailed(result.getTestContext()).size());
					results.setNewFeaturesSkip(results.getNewFeaturesSkip() + ReportNGUtils.getSkip(result.getTestContext()).size());
					results.setNewFeaturesKnownDefect(results.getNewFeaturesKnownDefect() + ReportNGUtils.getKnownDefect(result.getTestContext()).size());
					results.setNewFeaturesFixed(results.getNewFeaturesFixed() + ReportNGUtils.getFixed(result.getTestContext()).size());
				}
			}
		}
		// Calculate Total
		results.setTotalPass(results.getNewFeaturesPass() + results.getRegressionPass());
		results.setTotalFail(results.getNewFeaturesFail() + results.getRegressionFail());
		results.setTotalSkip(results.getNewFeaturesSkip() + results.getRegressionSkip());
		results.setTotalKnownDefect(results.getNewFeaturesKnownDefect() + results.getRegressionKnownDefect());
		results.setTotalFixed(results.getNewFeaturesFixed() + results.getRegressionFixed());
		
		// Calculate Summary
		results.setSummaryRegression(
				results.getRegressionFail() + results.getRegressionFixed() + results.getRegressionKnownDefect() + results.getRegressionPass() + results.getRegressionSkip());
		results.setSummaryNewFeature(
				results.getNewFeaturesFail() + results.getNewFeaturesFixed() + results.getNewFeaturesKnownDefect() + results.getNewFeaturesPass() + results.getNewFeaturesSkip());
		results.setSummaryTotal(results.getTotalFail() + results.getTotalFixed() + results.getTotalKnownDefect() + results.getTotalPass() + results.getTotalSkip());
		
		// Calculate Success Rate
		double regression;
		try {
			regression = (results.getSummaryRegression() - results.getRegressionFail() - results.getRegressionSkip()) * 100 / results.getSummaryRegression();
			results.setRegression(regression);
		} catch (Exception ex) {
			results.setRegression(0);
		}
		
		double newFeatures;
		try {
			newFeatures = (results.getSummaryNewFeature() - results.getNewFeaturesFail() - results.getNewFeaturesSkip()) * 100 / results.getSummaryNewFeature();
			results.setNewFeatures(newFeatures);
		} catch (Exception ex) {
			results.setNewFeatures(0);
		}
		double total;
		try {
			total = (results.getSummaryTotal() - results.getTotalFail() - results.getTotalSkip()) * 100 / results.getSummaryTotal();
			results.setTotal(total);
		} catch (Exception ex) {
			results.setTotal(0);
		}
		
		// Calculate Start End Date
		Date tempStartDate = startDate.get(0);
		for (Date tempDate : startDate) {
			if (tempDate.before(tempStartDate)) {
				tempStartDate = tempDate;
			}
		}
		results.setStartDate(tempStartDate);
		
		Date tempEndDate = endDate.get(0);
		for (Date tempDate : endDate) {
			if (tempDate.after(tempEndDate)) {
				tempEndDate = tempDate;
			}
		}
		results.setEndDate(tempEndDate);
		
		long executionTime = results.getEndDate().toInstant().toEpochMilli() - results.getStartDate().toInstant().toEpochMilli();
		results.setExecutionTime(ReportNGUtils.formatDurationinMinutes(executionTime));
		return results;
	}
	
	public static IssuesDTO issues(List<ISuite> suites) {
		IssuesDTO issuesDTO = new IssuesDTO();
		int suiteIndex = 1;
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> item = tempISuite.getResults().entrySet().iterator();
			int testIndex = 1;
			while (item.hasNext()) {
				Entry<String, ISuiteResult> pair = item.next();
				ISuiteResult result = pair.getValue();
				String link = "suite" + suiteIndex + "_test" + testIndex + "_results.html";
				// Calculate issues
				List<IssueDTO> knownIssues = ReportNGUtils.getKnownIssues(tempISuite.getName(), link, result.getTestContext().getPassedTests().getAllResults());
				for (IssueDTO tempIssueDTO : knownIssues) {
					if (issuesDTO.getKnownIssues().containsKey(tempIssueDTO.getIssueDescription())) {
						issuesDTO.getKnownIssues().get(tempIssueDTO.getIssueDescription()).add(tempIssueDTO);
					} else {
						issuesDTO.getKnownIssues().put(tempIssueDTO.getIssueDescription(), new ArrayList<IssueDTO>(Arrays.asList(tempIssueDTO)));
					}
				}
				issuesDTO.setKnownIssuesAmount(issuesDTO.getKnownIssues().size());
				
				List<IssueDTO> fixedIssues = ReportNGUtils.getFixedIssues(tempISuite.getName(), link, result.getTestContext().getPassedTests().getAllResults());
				for (IssueDTO tempIssueDTO : fixedIssues) {
					if (issuesDTO.getFixedIssues().containsKey(tempIssueDTO.getIssueDescription())) {
						issuesDTO.getFixedIssues().get(tempIssueDTO.getIssueDescription()).add(tempIssueDTO);
					} else {
						issuesDTO.getFixedIssues().put(tempIssueDTO.getIssueDescription(), new ArrayList<IssueDTO>(Arrays.asList(tempIssueDTO)));
					}
				}
				issuesDTO.setFixedIssuesAmount(issuesDTO.getFixedIssues().size());
				
				List<IssueDTO> newIssues = ReportNGUtils.getNewIssues(tempISuite.getName(), link, result.getTestContext().getFailedTests().getAllResults());
				for (IssueDTO tempIssueDTO : newIssues) {
					if (issuesDTO.getNewIssues().containsKey(tempIssueDTO.getIssueDescription())) {
						issuesDTO.getNewIssues().get(tempIssueDTO.getIssueDescription()).add(tempIssueDTO);
					} else {
						issuesDTO.getNewIssues().put(tempIssueDTO.getIssueDescription(), new ArrayList<IssueDTO>(Arrays.asList(tempIssueDTO)));
					}
				}
				issuesDTO.setNewIssuesAmount(issuesDTO.getNewIssues().size());
				
				// Calculate Features and newFeatures
				List<IssueDTO> newFeature = ReportNGUtils.getNewFeatures(tempISuite.getName(), link, result.getTestContext());
				if (!newFeature.isEmpty()) {
					for (IssueDTO temp : newFeature) {
						if (issuesDTO.getNewFeature().containsKey(temp.getIssueDescription())) {
							issuesDTO.getNewFeature().get(temp.getIssueDescription()).add(temp);
						} else {
							issuesDTO.getNewFeature().put(temp.getIssueDescription(), new ArrayList<IssueDTO>(Arrays.asList(temp)));
						}
					}
				}
				
				List<IssueDTO> feature = ReportNGUtils.getFeatures(tempISuite.getName(), link, result.getTestContext());
				if (!feature.isEmpty()) {
					for (IssueDTO temp : feature) {
						if (issuesDTO.getFeature().containsKey(temp.getIssueDescription())) {
							issuesDTO.getFeature().get(temp.getIssueDescription()).add(temp);
						} else {
							issuesDTO.getFeature().put(temp.getIssueDescription(), new ArrayList<IssueDTO>(Arrays.asList(temp)));
						}
					}
				}
				testIndex++;
			}
			suiteIndex++;
		}
		return issuesDTO;
	}
	
	public static ITestContext updateResults(ITestContext iTestContext) {
		String knownDefectsMode = System.getProperty(HTMLReporter.KWOWNDEFECTSMODE);
		if (knownDefectsMode == null || knownDefectsMode.isEmpty()) {
			knownDefectsMode = "false";
		}else{
			if(knownDefectsMode.equalsIgnoreCase("true")){
				IResultMap failedResultMap = iTestContext.getFailedTests();
				IResultMap passedResultMap = iTestContext.getPassedTests();
				Iterator<ITestResult> failedIterator = failedResultMap.getAllResults().iterator();
				// This will process FAIL and KFAIL
				while (failedIterator.hasNext()) {
					ITestResult testResult = failedIterator.next();
					java.lang.reflect.Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
					if (method.getAnnotation(KnownDefect.class) != null && !ReportNGUtils.KNOWN.equals(testResult.getAttribute(ReportNGUtils.TEST))) {
						// This is a KFAIL result Remove this test from the failed list
						failedResultMap.removeResult(testResult.getMethod());
						// Mark this test as passed
						testResult.setStatus(ITestResult.SUCCESS);
						testResult.setAttribute(ReportNGUtils.TEST, ReportNGUtils.KNOWN);
						// Add to PASS
						passedResultMap.addResult(testResult, testResult.getMethod());
					}
				}
				Iterator<ITestResult> passedIterator = passedResultMap.getAllResults().iterator();
				// This will process PASS and KPASS
				while (passedIterator.hasNext()) {
					ITestResult testResult = passedIterator.next();
					java.lang.reflect.Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
					if (method.getAnnotation(KnownDefect.class) != null &&
							!ReportNGUtils.KNOWN.equals(testResult.getAttribute(ReportNGUtils.TEST)) &&
							!ReportNGUtils.FIXED.equals(testResult.getAttribute(ReportNGUtils.TEST))) {
						testResult.setAttribute(ReportNGUtils.TEST, ReportNGUtils.FIXED);
					}
				}
			}
		}
		return iTestContext;
	}
}
