package org.uncommons.reportng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlClass;
import org.uncommons.reportng.annotations.KnownDefect;
import org.uncommons.reportng.dto.IssueDTO;
import org.uncommons.reportng.dto.IssuesDTO;
import org.uncommons.reportng.dto.PackageDetailsDTO;
import org.uncommons.reportng.dto.ResultsDTO;

public class ReporterHelper {
	
	public static ResultsDTO checkAttribute(List<ISuite> suites) {
		ArrayList<Date> startDate = new ArrayList<>();
		ArrayList<Date> endDate = new ArrayList<>();
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
		results.setSummaryRegression(results.getRegressionFail() + results.getRegressionFixed() + results.getRegressionKnownDefect() + results.getRegressionPass() + results.getRegressionSkip());
		results.setSummaryNewFeature(results.getNewFeaturesFail() + results.getNewFeaturesFixed() + results.getNewFeaturesKnownDefect() + results.getNewFeaturesPass() + results.getNewFeaturesSkip());
		results.setSummaryTotal(results.getTotalFail() + results.getTotalFixed() + results.getTotalKnownDefect() + results.getTotalPass() + results.getTotalSkip());
		
		// Calculate Success Rate
		double regression;
		try {
			if (results.getSummaryRegression() > 0) {
				regression = (results.getSummaryRegression() - results.getRegressionFail() - results.getRegressionSkip()) * 100 / results.getSummaryRegression();
				results.setRegression(regression);
			} else {
				results.setRegression(-1);
			}
		} catch (Exception ex) {
			results.setRegression(0);
		}
		
		double newFeatures;
		try {
			if (results.getSummaryNewFeature() > 0) {
				newFeatures = (results.getSummaryNewFeature() - results.getNewFeaturesFail() - results.getNewFeaturesSkip()) * 100 / results.getSummaryNewFeature();
				results.setNewFeatures(newFeatures);
			} else {
				results.setNewFeatures(-1);
			}
		} catch (Exception ex) {
			results.setNewFeatures(0);
		}
		double total;
		try {
			if (results.getSummaryTotal() > 0) {
				total = (results.getSummaryTotal() - results.getTotalFail() - results.getTotalSkip()) * 100 / results.getSummaryTotal();
				results.setTotal(total);
			} else {
				results.setTotal(-1);
			}
		} catch (Exception ex) {
			results.setTotal(0);
		}
		
		// Calculate Start End Date
		Date tempStartDate = new Date();
		try {
			tempStartDate = startDate.get(0);
		} catch (NullPointerException | IndexOutOfBoundsException ex) {
			
		}
		for (Date tempDate : startDate) {
			if (tempDate.before(tempStartDate)) {
				tempStartDate = tempDate;
			}
		}
		results.setStartDate(tempStartDate);
		
		Date tempEndDate = new Date();
		try {
			tempEndDate = endDate.get(0);
		} catch (NullPointerException | IndexOutOfBoundsException ex) {
			
		}
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
				// Calculate Known issues
				List<IssueDTO> knownIssues = ReportNGUtils.getKnownIssues(tempISuite.getName(), link, result.getTestContext().getPassedTests().getAllResults());
				for (IssueDTO temp : knownIssues) {
					String issueDescription = "null";
					if (temp.getIssueDescription() != null && !temp.getIssueDescription().isEmpty()) {
						issueDescription = temp.getIssueDescription().trim();
					}
					if (issuesDTO.getKnownIssues().containsKey(issueDescription)) {
						issuesDTO.getKnownIssues().get(issueDescription).add(temp);
					} else {
						issuesDTO.getKnownIssues().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
					}
				}
				issuesDTO.setKnownIssuesAmount(issuesDTO.getKnownIssues().size());
				// Calculate Fixed issues
				List<IssueDTO> fixedIssues = ReportNGUtils.getFixedIssues(tempISuite.getName(), link, result.getTestContext().getPassedTests().getAllResults());
				for (IssueDTO temp : fixedIssues) {
					String issueDescription = "null";
					if (temp.getIssueDescription() != null && !temp.getIssueDescription().isEmpty()) {
						issueDescription = temp.getIssueDescription().trim();
					}
					if (issuesDTO.getFixedIssues().containsKey(issueDescription)) {
						issuesDTO.getFixedIssues().get(issueDescription).add(temp);
					} else {
						issuesDTO.getFixedIssues().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
					}
				}
				issuesDTO.setFixedIssuesAmount(issuesDTO.getFixedIssues().size());
				// Calculate skipped issues
				List<IssueDTO> skippedIssues = ReportNGUtils.getSkippedIssues(tempISuite.getName(), link, result.getTestContext().getSkippedTests().getAllResults());
				for (IssueDTO temp : skippedIssues) {
					String issueDescription = "null";
					if (temp.getIssueDescription() != null && !temp.getIssueDescription().isEmpty()) {
						issueDescription = temp.getIssueDescription().trim();
					}
					if (issuesDTO.getSkippedIssues().containsKey(issueDescription)) {
						issuesDTO.getSkippedIssues().get(issueDescription).add(temp);
					} else {
						issuesDTO.getSkippedIssues().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
					}
				}
				issuesDTO.setSkippedIssuesAmount(issuesDTO.getSkippedIssues().size());
				// Calculate New issues
				List<IssueDTO> newIssues = ReportNGUtils.getNewIssues(tempISuite.getName(), link, result.getTestContext().getFailedTests().getAllResults());
				for (IssueDTO temp : newIssues) {
					String issueDescription = "null";
					if (temp.getIssueDescription() != null && !temp.getIssueDescription().isEmpty()) {
						issueDescription = temp.getIssueDescription().trim();
					}
					if (issuesDTO.getNewIssues().containsKey(issueDescription)) {
						issuesDTO.getNewIssues().get(issueDescription).add(temp);
					} else {
						issuesDTO.getNewIssues().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
					}
				}
				issuesDTO.setNewIssuesAmount(issuesDTO.getNewIssues().size());
				
				// Calculate Features and newFeatures
				List<IssueDTO> newFeature = ReportNGUtils.getNewFeatures(tempISuite.getName(), link, result.getTestContext());
				if (!newFeature.isEmpty()) {
					for (IssueDTO temp : newFeature) {
						String issueDescription = "null";
						if (temp.getIssueDescription() != null && !temp.getIssueDescription().isEmpty()) {
							issueDescription = temp.getIssueDescription().trim();
						}
						if (issuesDTO.getNewFeature().containsKey(issueDescription)) {
							issuesDTO.getNewFeature().get(issueDescription).add(temp);
						} else {
							issuesDTO.getNewFeature().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
						}
					}
				}
				
				List<IssueDTO> feature = ReportNGUtils.getFeatures(tempISuite.getName(), link, result.getTestContext());
				if (!feature.isEmpty()) {
					for (IssueDTO temp : feature) {
						String issueDescription = "null";
						if (temp.getIssueDescription() != null && !temp.getIssueDescription().isEmpty()) {
							issueDescription = temp.getIssueDescription().trim();
						}
						if (issuesDTO.getFeature().containsKey(issueDescription)) {
							issuesDTO.getFeature().get(issueDescription).add(temp);
						} else {
							issuesDTO.getFeature().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
						}
					}
				}
				testIndex++;
			}
			suiteIndex++;
		}
		// Calculate newIssuesFeatures vs newIssuesRegression
		Iterator<Entry<String, List<IssueDTO>>> item = issuesDTO.getNewIssues().entrySet().iterator();
		while (item.hasNext()) {
			Entry<String, List<IssueDTO>> pair = item.next();
			for (IssueDTO temp : pair.getValue()) {
				String issueDescription = "null";
				if (temp.getIssueDescription() != null && !temp.getIssueDescription().isEmpty()) {
					issueDescription = temp.getIssueDescription().trim();
				}
				if (temp.isRegression()) {
					if (issuesDTO.getNewIssuesRegression().containsKey(issueDescription)) {
						issuesDTO.getNewIssuesRegression().get(issueDescription).add(temp);
					} else {
						issuesDTO.getNewIssuesRegression().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
					}
				} else {
					if (issuesDTO.getNewIssuesNewFeature().containsKey(issueDescription)) {
						issuesDTO.getNewIssuesNewFeature().get(issueDescription).add(temp);
					} else {
						issuesDTO.getNewIssuesNewFeature().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
					}
				}
			}
		}
		// Calculate KnownissuesFeatures vs knownIssuesRegression
		item = issuesDTO.getKnownIssues().entrySet().iterator();
		while (item.hasNext()) {
			Entry<String, List<IssueDTO>> pair = item.next();
			for (IssueDTO temp : pair.getValue()) {
				String issueDescription = "null";
				if (temp.getIssueDescription() != null && !temp.getIssueDescription().isEmpty()) {
					issueDescription = temp.getIssueDescription().trim();
				}
				if (temp.isRegression()) {
					if (issuesDTO.getKnownIssuesRegression().containsKey(issueDescription)) {
						issuesDTO.getKnownIssuesRegression().get(issueDescription).add(temp);
					} else {
						issuesDTO.getKnownIssuesRegression().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
					}
				} else {
					if (issuesDTO.getKnownIssuesNewFeature().containsKey(issueDescription)) {
						issuesDTO.getKnownIssuesNewFeature().get(issueDescription).add(temp);
					} else {
						issuesDTO.getKnownIssuesNewFeature().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
					}
				}
			}
		}
		// Calculate fixedissuesFeatures vs fixedIssuesRegression
		item = issuesDTO.getFixedIssues().entrySet().iterator();
		while (item.hasNext()) {
			Entry<String, List<IssueDTO>> pair = item.next();
			for (IssueDTO temp : pair.getValue()) {
				String issueDescription = "null";
				if (temp.getIssueDescription() != null && !temp.getIssueDescription().isEmpty()) {
					issueDescription = temp.getIssueDescription().trim();
				}
				if (temp.isRegression()) {
					if (issuesDTO.getFixedIssuesRegression().containsKey(issueDescription)) {
						issuesDTO.getFixedIssuesRegression().get(issueDescription).add(temp);
					} else {
						issuesDTO.getFixedIssuesRegression().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
					}
				} else {
					if (issuesDTO.getFixedIssuesNewFeature().containsKey(issueDescription)) {
						issuesDTO.getFixedIssuesNewFeature().get(issueDescription).add(temp);
					} else {
						issuesDTO.getFixedIssuesNewFeature().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
					}
				}
			}
		}
		// Calculate newSkippedFeatures vs newSkippedRegression
		item = issuesDTO.getSkippedIssues().entrySet().iterator();
		while (item.hasNext()) {
			Entry<String, List<IssueDTO>> pair = item.next();
			for (IssueDTO temp : pair.getValue()) {
				String issueDescription = "null";
				if (temp.getIssueDescription() != null && !temp.getIssueDescription().isEmpty()) {
					issueDescription = temp.getIssueDescription().trim();
				}
				if (temp.isRegression()) {
					if (issuesDTO.getSkippedIssuesRegression().containsKey(issueDescription)) {
						issuesDTO.getSkippedIssuesRegression().get(issueDescription).add(temp);
					} else {
						issuesDTO.getSkippedIssuesRegression().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
					}
				} else {
					if (issuesDTO.getSkippedIssuesNewFeature().containsKey(issueDescription)) {
						issuesDTO.getSkippedIssuesNewFeature().get(issueDescription).add(temp);
					} else {
						issuesDTO.getSkippedIssuesNewFeature().put(issueDescription, new ArrayList<IssueDTO>(Arrays.asList(temp)));
					}
				}
			}
		}
		return issuesDTO;
	}
	
	public static boolean knownDefectMode() {
		String knownDefectsMode = System.getProperty(HTMLReporter.KWOWNDEFECTSMODE);
		if (knownDefectsMode == null || knownDefectsMode.isEmpty()) {
			knownDefectsMode = "false";
		}
		if (knownDefectsMode.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}
	
	public static ITestContext updateResults(ITestContext iTestContext) {
		if (knownDefectMode()) {
			IResultMap failedResultMap = iTestContext.getFailedTests();
			IResultMap passedResultMap = iTestContext.getPassedTests();
			Iterator<ITestResult> failedIterator = failedResultMap.getAllResults().iterator();
			// This will process FAIL and KFAIL
			while (failedIterator.hasNext()) {
				ITestResult testResult = failedIterator.next();
				java.lang.reflect.Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
				if (method.getAnnotation(KnownDefect.class) != null && !ReportNGUtils.KNOWN.equals(testResult.getAttribute(ReportNGUtils.TEST))) {
					// This is a KFAIL result Remove this test from the
					// failed list
					failedResultMap.removeResult(testResult.getMethod());
					// Mark this test as passed
					testResult.setStatus(ITestResult.SUCCESS);
					testResult.setAttribute(ReportNGUtils.TEST, ReportNGUtils.KNOWN);
					// Add to PASS
					passedResultMap.addResult(testResult);
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
		return iTestContext;
	}
	
	private static Map<PackageDetailsDTO, List<PackageDetailsDTO>> sortItems(Map<PackageDetailsDTO, List<PackageDetailsDTO>> results) {
		Map<PackageDetailsDTO, List<PackageDetailsDTO>> treeMap = new TreeMap<>(new ResultComparator());
		treeMap.putAll(results);
		return treeMap;
	}
	
	public static Map<PackageDetailsDTO, List<PackageDetailsDTO>> packageDetails(List<ISuite> sortedSuites) {
		Map<PackageDetailsDTO, List<PackageDetailsDTO>> packagesFinal = new HashMap<>();
		if (sortedSuites != null) {
			Map<String, List<PackageDetailsDTO>> packages = new HashMap<>();
			int suiteIndex = 1;
			for (ISuite tempISuite : sortedSuites) {
				Map<String, ISuiteResult> results = tempISuite.getResults();
				int testIndex = 1;
				for (Map.Entry<String, ISuiteResult> entry : results.entrySet()) {
					for (XmlClass tempClass : entry.getValue().getTestContext().getCurrentXmlTest().getClasses()) {
						if (tempClass.getName() != null && !tempClass.getName().isEmpty()) {
							PackageDetailsDTO packageResults = new PackageDetailsDTO();
							packageResults.setPackageName(tempClass.getName().substring(0, tempClass.getName().lastIndexOf(".")).trim());
							packageResults.setPass(ReportNGUtils.getPassed(entry.getValue().getTestContext()).size());
							packageResults.setFail(ReportNGUtils.getFailed(entry.getValue().getTestContext()).size());
							packageResults.setSkip(ReportNGUtils.getSkip(entry.getValue().getTestContext()).size());
							packageResults.setKnown(ReportNGUtils.getKnownDefect(entry.getValue().getTestContext()).size());
							packageResults.setFixed(ReportNGUtils.getFixed(entry.getValue().getTestContext()).size());
							packageResults.setDuration(ReportNGUtils.formatDurationinMinutes(entry.getValue().getTestContext().getEndDate().getTime() - entry.getValue().getTestContext().getStartDate().getTime()));
							packageResults.setEndMillis(entry.getValue().getTestContext().getEndDate().getTime());
							packageResults.setStartMillis(entry.getValue().getTestContext().getStartDate().getTime());
							packageResults.setClassName(tempClass.getName().trim());
							packageResults.setUrl("suite" + suiteIndex + "_test" + testIndex + "_results.html");
							if (packages.containsKey(packageResults.getPackageName())) {
								packages.get(packageResults.getPackageName()).add(packageResults);
							} else {
								packages.put(packageResults.getPackageName(), new ArrayList<>(Arrays.asList(packageResults)));
							}
							
						}
						testIndex++;
					}
				}
				suiteIndex++;
			}
			// Count Summary per Package
			for (Map.Entry<String, List<PackageDetailsDTO>> entry : packages.entrySet()) {
				PackageDetailsDTO packageDetailsDTO = new PackageDetailsDTO(null, null, 0, 0, 0, 0, 0, "00:00:00", "");
				long startDate = Long.MAX_VALUE;
				long endDate = 0;
				for (PackageDetailsDTO temp : entry.getValue()) {
					if (temp.getStartMillis() < startDate) {
						startDate = temp.getStartMillis();
					}
					if (temp.getEndMillis() > endDate) {
						endDate = temp.getEndMillis();
					}
					// packageDetailsDTO.setDuration(ReportNGUtils.formatDurationinMinutes(temp.getDuration(), packageDetailsDTO.getDuration()));
					packageDetailsDTO.setDuration(ReportNGUtils.formatDurationinMinutes(endDate - startDate));
					packageDetailsDTO.setFail(temp.getFail() + packageDetailsDTO.getFail());
					packageDetailsDTO.setFixed(temp.getFixed() + packageDetailsDTO.getFixed());
					packageDetailsDTO.setKnown(temp.getKnown() + packageDetailsDTO.getKnown());
					packageDetailsDTO.setPass(temp.getPass() + packageDetailsDTO.getPass());
					packageDetailsDTO.setSkip(temp.getSkip() + packageDetailsDTO.getSkip());
					packageDetailsDTO.setPackageName(entry.getKey());
				}
				packagesFinal.put(packageDetailsDTO, entry.getValue());
			}
		}
		return sortItems(packagesFinal);
	}
	
	public static Map<PackageDetailsDTO, List<PackageDetailsDTO>> groupDetails(List<ISuite> sortedSuites) {
		Map<PackageDetailsDTO, List<PackageDetailsDTO>> packagesFinal = new HashMap<>();
		if (sortedSuites != null) {
			Map<String, List<PackageDetailsDTO>> packages = new HashMap<>();
			int suiteIndex = 1;
			for (ISuite tempISuite : sortedSuites) {
				Map<String, ISuiteResult> results = tempISuite.getResults();
				int testIndex = 1;
				for (Map.Entry<String, ISuiteResult> entry : results.entrySet()) {
					for (ITestNGMethod tempClass : entry.getValue().getTestContext().getAllTestMethods()) {
						if (tempClass.getGroups() != null && tempClass.getGroups().length > 0) {
							for (String tempGroup : tempClass.getGroups()) {
								if (tempGroup != null && !tempGroup.isEmpty()) {
									PackageDetailsDTO packageResults = new PackageDetailsDTO();
									packageResults.setPackageName(tempGroup.trim());
									packageResults.setPass(ReportNGUtils.getPassed(entry.getValue().getTestContext()).size());
									packageResults.setFail(ReportNGUtils.getFailed(entry.getValue().getTestContext()).size());
									packageResults.setSkip(ReportNGUtils.getSkip(entry.getValue().getTestContext()).size());
									packageResults.setKnown(ReportNGUtils.getKnownDefect(entry.getValue().getTestContext()).size());
									packageResults.setFixed(ReportNGUtils.getFixed(entry.getValue().getTestContext()).size());
									packageResults
											.setDuration(ReportNGUtils.formatDurationinMinutes(entry.getValue().getTestContext().getEndDate().getTime() - entry.getValue().getTestContext().getStartDate().getTime()));
									packageResults.setEndMillis(entry.getValue().getTestContext().getEndDate().getTime());
									packageResults.setStartMillis(entry.getValue().getTestContext().getStartDate().getTime());
									packageResults.setClassName(tempClass.getTestClass().getName().trim());
									packageResults.setUrl("suite" + suiteIndex + "_test" + testIndex + "_results.html");
									if (packages.containsKey(packageResults.getPackageName())) {
										boolean found = false;
										for (PackageDetailsDTO temp : packages.get(packageResults.getPackageName())) {
											if (temp.getClassΝame().equals(packageResults.getClassΝame())) {
												found = true;
											}
										}
										if (!found) {
											packages.get(packageResults.getPackageName()).add(packageResults);
										}
									} else {
										packages.put(packageResults.getPackageName(), new ArrayList<>(Arrays.asList(packageResults)));
									}
								}
							}
						}
					}
					testIndex++;
				}
				suiteIndex++;
			}
			// Count Summary per Package
			for (Map.Entry<String, List<PackageDetailsDTO>> entry : packages.entrySet()) {
				PackageDetailsDTO packageDetailsDTO = new PackageDetailsDTO(null, null, 0, 0, 0, 0, 0, "00:00:00", "");
				long startDate = Long.MAX_VALUE;
				long endDate = 0;
				for (PackageDetailsDTO temp : entry.getValue()) {
					if (packageDetailsDTO.getStartMillis() < startDate) {
						startDate = temp.getStartMillis();
					}
					if (packageDetailsDTO.getEndMillis() > endDate) {
						endDate = temp.getEndMillis();
					}
					packageDetailsDTO.setDuration(ReportNGUtils.formatDurationinMinutes(endDate - startDate));
					// packageDetailsDTO.setDuration(ReportNGUtils.formatDurationinMinutes(temp.getDuration(), packageDetailsDTO.getDuration()));
					packageDetailsDTO.setFail(temp.getFail() + packageDetailsDTO.getFail());
					packageDetailsDTO.setFixed(temp.getFixed() + packageDetailsDTO.getFixed());
					packageDetailsDTO.setKnown(temp.getKnown() + packageDetailsDTO.getKnown());
					packageDetailsDTO.setPass(temp.getPass() + packageDetailsDTO.getPass());
					packageDetailsDTO.setSkip(temp.getSkip() + packageDetailsDTO.getSkip());
					packageDetailsDTO.setPackageName(entry.getKey());
				}
				packagesFinal.put(packageDetailsDTO, entry.getValue());
			}
		}
		return sortItems(packagesFinal);
	}
}
