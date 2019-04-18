package org.uncommons.reportng;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.testng.IClass;
import org.testng.IInvokedMethod;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.internal.ResultMap;
import org.testng.xml.XmlClass;
import org.uncommons.reportng.annotations.Feature;
import org.uncommons.reportng.annotations.KnownDefect;
import org.uncommons.reportng.annotations.NewFeature;
import org.uncommons.reportng.dto.IssueDTO;
import org.uncommons.reportng.dto.IssuesDTO;
import org.uncommons.reportng.dto.ResultStatus;
import org.uncommons.reportng.dto.ResultsDTO;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class that provides various helper methods that can be invoked from a
 * Velocity template.
 */
public class ReportNGUtils {

	public static final String TEST = "test";
	public static final String FIXED = "fixed";
	public static final String KNOWN = "known";
	private static final NumberFormat DURATION_FORMAT = new DecimalFormat("#0.000");
	private static final String KNOWNDEFECT = KnownDefect.class.getName();
	private static final String NEW_FEATURE = NewFeature.class.getName();
	private static final String FEATURE = Feature.class.getName();

	public static String getExternalLinks() {
		String response = "";
		String externalLinks = System.getProperty(HTMLReporter.EXTERNAL_LINKS);
		if (externalLinks != null && !externalLinks.isEmpty()) {
			// Serialize
			ObjectMapper mapper = new ObjectMapper();
			try {
				Map<String, String> resultMap = mapper.readValue(externalLinks, new TypeReference<Map<String, String>>() {
				});
				response += "<a href=\"#pageSubmenuExtLinks\" class=\"list-group-item\" data-toggle=\"collapse\" aria-expanded=\"false\">\n";
				response += "<span class=\"glyphicon glyphicon-paperclip\"></span>&nbsp;&nbsp;External Links</a>\n";
				response += "<ul class=\"collapse list-unstyled\" id=\"pageSubmenuExtLinks\">\n";
				Iterator<Map.Entry<String, String>> itr = resultMap.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry<String, String> entry = itr.next();
					response += "<a href=\"" + entry.getValue() + "\" class=\"list-group-item\" target=\"overview\">&nbsp;&nbsp;&nbsp;" + entry.getKey()
							+ "</a>" + "\n";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}

	private static int getNumberofRegressionIssues(IssuesDTO issuesDTO) {
		int regressionCounter = 0;
		Iterator<Entry<String, List<IssueDTO>>> it = issuesDTO.getNewIssues().entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<IssueDTO>> pair = it.next();
			for (IssueDTO temp : pair.getValue()) {
				if (temp.isRegression()) {
					regressionCounter++;
					break;
				}
			}
		}
		return regressionCounter;
	}

	private static int getNumberofNewFeatureIssues(IssuesDTO issuesDTO) {
		int regressionCounter = 0;
		Iterator<Entry<String, List<IssueDTO>>> it = issuesDTO.getNewIssues().entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<IssueDTO>> pair = it.next();
			for (IssueDTO temp : pair.getValue()) {
				if (!temp.isRegression()) {
					regressionCounter++;
					break;
				}
			}
		}
		return regressionCounter;
	}

	public static String getReleaseStatus(List<ISuite> suites) {
		String status = "";
		if ("true".equalsIgnoreCase(System.getProperty(HTMLReporter.SKIP_EXECUTION))) {
			status += "<b><font color=\"red\">Skip Execution due to \"Skip Execution Mode\".</font></b><br>";
		} else {
			boolean releaseRegression = true;
			boolean emptyReport = false;
			boolean skippedReport = false;
			String responseStatus = "";
			String responseNewFeatures = "";
			ResultsDTO resultsDTO = HTMLReporter.getResults();
			IssuesDTO issuesDTO = HTMLReporter.getIssuesDTO();
			if (resultsDTO.getSummaryTotal() == 0) {
				// No tests , empty report
				releaseRegression = false;
				emptyReport = true;
			} else if (resultsDTO.getSummaryTotal() == resultsDTO.getTotalSkip()) {
				// All tests skipped
				releaseRegression = false;
				skippedReport = true;
			} else {
				int numberofRegressionIssues = getNumberofRegressionIssues(issuesDTO);
				if (numberofRegressionIssues == 1) {
					responseStatus += "<li>There is <a href=\"newIssues.html\"/>one</a> regression failure, affecting ";
					if (resultsDTO.getRegressionFail() == 1) {
						responseStatus += resultsDTO.getRegressionFail() + " test.</li>";
					} else {
						responseStatus += resultsDTO.getRegressionFail() + " tests.</li>";
					}
					releaseRegression = false;
				} else if (numberofRegressionIssues > 1) {
					responseStatus += "<li>There are <a href=\"newIssues.html\"/>" + numberofRegressionIssues + "</a> regression failures, affecting ";
					if (resultsDTO.getRegressionFail() == 1) {
						responseStatus += resultsDTO.getRegressionFail() + " test.</li>";
					} else {
						responseStatus += resultsDTO.getRegressionFail() + " tests.</li>";
					}
					releaseRegression = false;
				} else if (resultsDTO.getRegressionFail() == 0 && resultsDTO.getRegressionPass() > 0) {
					responseStatus += "<li>There are no Regression Issues.</li>";
				}
				if (resultsDTO.getNewFeatures() > 0) {
					int numberofNewFeatureIssues = getNumberofNewFeatureIssues(issuesDTO);
					if (releaseRegression && resultsDTO.getRegressionPass() == 0 && numberofNewFeatureIssues > 0) {
						releaseRegression = false;
					}
					if (numberofNewFeatureIssues == 1) {
						responseStatus += "<li>There is <a href=\"newIssues.html\"/>one</a> failure related with new features, affecting ";
						if (resultsDTO.getNewFeaturesFail() == 1) {
							responseStatus += resultsDTO.getNewFeaturesFail() + " test.</li>";
						} else {
							responseStatus += resultsDTO.getNewFeaturesFail() + " tests.</li>";
						}
					} else if (numberofNewFeatureIssues > 1) {
						responseStatus += "<li>There are <a href=\"newIssues.html\"/>" + numberofNewFeatureIssues
								+ "</a> failures related with new features, affecting ";
						if (resultsDTO.getNewFeaturesFail() == 1) {
							responseStatus += resultsDTO.getNewFeaturesFail() + " test.</li>";
						} else {
							responseStatus += resultsDTO.getNewFeaturesFail() + " tests.</li>";
						}
					} else if (resultsDTO.getNewFeaturesFail() == 0 && resultsDTO.getNewFeaturesPass() > 0) {
					}
					Iterator<Entry<String, List<IssueDTO>>> it = issuesDTO.getNewFeature().entrySet().iterator();
					responseNewFeatures += "";
					while (it.hasNext()) {
						Entry<String, List<IssueDTO>> pair = it.next();
						ResultStatus overAllStatus = ResultStatus.PASS;
						for (IssueDTO temp : pair.getValue()) {
							if (ResultStatus.FAIL.equals(temp.getStatus())) {
								overAllStatus = ResultStatus.FAIL;
							} else if (ResultStatus.PASS_WITH_FIXED_ISSUES.equals(temp.getStatus())) {
								overAllStatus = ResultStatus.PASS_WITH_FIXED_ISSUES;
							} else if (ResultStatus.PASS_WITH_KNOWN_ISSUES.equals(temp.getStatus())) {
								overAllStatus = ResultStatus.PASS_WITH_KNOWN_ISSUES;
							} else if (ResultStatus.SKIP.equals(temp.getStatus())) {
								overAllStatus = ResultStatus.SKIP;
							}
						}
						if (ResultStatus.PASS.equals(overAllStatus) || ResultStatus.PASS_WITH_FIXED_ISSUES.equals(overAllStatus)) {
							responseNewFeatures += "<li>The new feature with description '<a href=\"" + HTMLReporter.FEATURES + "#" + pair.getKey()
									+ "\" style=\"color:green\">" + pair.getKey()
									+ "</a>' has no failures and can be announced !</li>";
						} else if (ResultStatus.FAIL.equals(overAllStatus)) {
							responseNewFeatures += "<li>The new feature with description '<a href=\"" + HTMLReporter.FEATURES + "#" + pair.getKey()
									+ "\" style=\"color:red\">"
									+ pair.getKey()
									+ "</a>' has failures and should not be announced !</li>";
						} else if (ResultStatus.PASS_WITH_KNOWN_ISSUES.equals(overAllStatus)) {
							responseNewFeatures += "<li>The new feature with description '<a href=\"" + HTMLReporter.FEATURES + "#" + pair.getKey()
									+ "\" style=\"color:orange\">" + pair.getKey()
									+ "</a>' has Known issues and should not be announced !</li>";
						} else if (ResultStatus.SKIP.equals(overAllStatus)) {
							responseNewFeatures += "<li>The new feature with description '<a href=\"" + HTMLReporter.FEATURES + "#" + pair.getKey()
									+ "\" style=\"color:yellow\">" + pair.getKey()
									+ "</a>' has skip test cases and should not be announced !</li>";
						}

					}
					responseNewFeatures += "";
				}
			}
			responseStatus += "";
			if (releaseRegression) {
				status += "<font color=\"green\">You can Proceed with a new Release !</font><br>";
				status += "<ul>";
				status += responseStatus;
				status += responseNewFeatures;
				status += "</ul>";
			} else {
				if (emptyReport) {
					status += "<font color=\"black\">Empty Report !</font><br>";
					status += "<ul>";
					status += responseStatus;
					status += responseNewFeatures;
					status += "</ul>";
				} else if (skippedReport) {
					status += "<font color=\"red\">All test are Skipped !</font><br>";
					status += "<ul>";
					status += responseStatus;
					status += responseNewFeatures;
					status += "</ul>";
				} else {
					status += "<font color=\"red\">You should not Proceed with a new Release !</font><br>";
					status += "<ul>";
					status += responseStatus;
					status += responseNewFeatures;
					status += "</ul>";
				}
			}

		}
		return status;
	}

	public static ResultsDTO checkAttribute(List<ISuite> suites) {
		return HTMLReporter.getResults();
	}

	public static IssuesDTO issues(List<ISuite> suites) {
		return HTMLReporter.getIssuesDTO();
	}

	public String runArguments(List<ISuite> suites) {
		String response = "";
		// Get Listeners
		Set<String> listeners = new HashSet<String>();
		if (suites != null && suites.size() > 0) {
			for (ISuite tempISuite : suites) {
				listeners.addAll(tempISuite.getXmlSuite().getListeners());
			}
		}
		String listenersToString = "";
		for (String temp : listeners) {
			listenersToString += temp + "<br>";
		}
		if (!listenersToString.isEmpty()) {
			response += "<tr>\n";
			response += "<td>Listeners</td>\n";
			response += "<td>" + listenersToString + "</td>\n";
			response += "</tr>\n";
		}

		// Get Include Groups
		Set<String> includeGroups = new HashSet<String>();
		if (suites != null && suites.size() > 0) {
			for (ISuite tempISuite : suites) {
				includeGroups.addAll(tempISuite.getXmlSuite().getIncludedGroups());
			}
		}
		String includeGroupsToString = "";
		for (String temp : includeGroups) {
			includeGroupsToString += temp + "<br>";
		}
		if (!includeGroupsToString.isEmpty()) {
			response += "<tr>\n";
			response += "<td>Include Groups</td>\n";
			response += "<td>" + includeGroupsToString + "</td>\n";
			response += "</tr>\n";
		}
		// Get Exclude Groups
		Set<String> excludeGroups = new HashSet<String>();
		if (suites != null && suites.size() > 0) {
			for (ISuite tempISuite : suites) {
				excludeGroups.addAll(tempISuite.getXmlSuite().getExcludedGroups());
			}
		}
		String excludeGroupsToString = "";
		for (String temp : excludeGroups) {
			excludeGroupsToString += temp + "<br>";
		}
		if (!excludeGroupsToString.isEmpty()) {
			response += "<tr>\n";
			response += "<td>Exclude Groups</td>\n";
			response += "<td>" + excludeGroupsToString + "</td>\n";
			response += "</tr>\n";
		}
		return response;
	}

	public String getIssues(Map<String, List<IssueDTO>> issues) {
		String response = "";
		if (issues != null) {
			Iterator<Entry<String, List<IssueDTO>>> it = issues.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, List<IssueDTO>> pair = it.next();
				response += "<tr>\n";
				response += "<td rowspan=\"" + pair.getValue().size() + "\" style=\"vertical-align:middle\" class=\"break-word\">" + pair.getKey() + "</td>";
				for (IssueDTO temp : pair.getValue()) {
					response += "<td style=\"vertical-align:middle\"><a href=\"suites_overview.html#" + temp.getSuiteName() + "\">" + temp.getSuiteName()
							+ "</a></td>";
					response += "<td style=\"vertical-align:middle\"><a href=\"" + temp.getLink() + "\">" + temp.getTestName() + "</a></td>";
					response += "<td class=\"break-word\" style=\"vertical-align:middle\">" + temp.getTestClass() + "</td>";
					response += "<td style=\"vertical-align:middle;text-align:center;\">" + temp.isRegression() + "</td>";
					response += "</tr>\n";
				}
			}
		}
		return response;
	}

	public String getSuites(List<ISuite> suites) {
		String response = "";
		if (suites != null) {
			int totalPass = 0;
			int totalFail = 0;
			int totalSkip = 0;
			int totalKnown = 0;
			int totalFixed = 0;
			Long totalStartDate = null;
			Long totalEndDate = null;
			for (ISuite tempISuite : suites) {
				String suiteName = tempISuite.getName();
				String parentSuiteName = "";
				try {
					parentSuiteName = tempISuite.getXmlSuite().getParentSuite().getName();
				} catch (Exception ex) {

				}
				String parallel = "";
				if (tempISuite.getParallel() != null && !tempISuite.getParallel().equalsIgnoreCase("none")) {
					parallel += tempISuite.getParallel() + ":" + tempISuite.getXmlSuite().getThreadCount();
				}
				Iterator<Entry<String, ISuiteResult>> item = tempISuite.getResults().entrySet().iterator();
				int pass = 0;
				int fail = 0;
				int skip = 0;
				int known = 0;
				int fixed = 0;
				while (item.hasNext()) {
					Entry<String, ISuiteResult> pair = item.next();
					ISuiteResult result = pair.getValue();
					pass += getPassed(result.getTestContext()).size();
					fail += getFailed(result.getTestContext()).size();
					skip += getSkip(result.getTestContext()).size();
					known += getKnownDefect(result.getTestContext()).size();
					fixed += getFixed(result.getTestContext()).size();
				}
				totalPass += pass;
				totalFail += fail;
				totalSkip += skip;
				totalKnown += known;
				totalFixed += fixed;

				Long startDate = null;
				Long endDate = null;
				for (IInvokedMethod tempIInvokedMethod : tempISuite.getAllInvokedMethods()) {
					if (startDate == null || tempIInvokedMethod.getDate() < startDate) {
						startDate = tempIInvokedMethod.getDate();
					}
					if (endDate == null || tempIInvokedMethod.getDate() > endDate) {
						endDate = tempIInvokedMethod.getDate();
					}
				}

				if (totalStartDate == null || totalStartDate > startDate) {
					totalStartDate = startDate;
				}
				if (totalEndDate == null || totalEndDate < endDate) {
					totalEndDate = endDate;
				}
				response += "<tr class=\"test\">\n";
				response += "<td width=\"100\">" + getDate(tempISuite) + "</td>";
				response += "<td width=\"250\">" + parentSuiteName + "</td>";
				response += "<td><a href=\"suites_overview.html#" + suiteName + "\">" + suiteName + "</a></td>";
				response += "<td class=\"duration\">" + parallel + "</td>";
				response += "<td class=\"duration\">" + formatDurationinMinutes(endDate - startDate) + "</td>";
				if (pass > 0) {
					response += "<td class=\"passed number\">" + pass + "</td>";
				} else {
					response += "<td class=\"zero number\">" + pass + "</td>";
				}
				if (skip > 0) {
					response += "<td class=\"skipped number\">" + skip + "</td>";
				} else {
					response += "<td class=\"zero number\">" + skip + "</td>";
				}
				if (known > 0) {
					response += "<td class=\"knownDefects number\">" + known + "</td>";
				} else {
					response += "<td class=\"zero number\">" + known + "</td>";
				}
				if (fixed > 0) {
					response += "<td class=\"fixed number\">" + fixed + "</td>";
				} else {
					response += "<td class=\"zero number\">" + fixed + "</td>";
				}
				if (fail > 0) {
					response += "<td class=\"failed number\">" + fail + "</td>";
				} else {
					response += "<td class=\"zero number\">" + fail + "</td>";
				}
				response += "<td class=\"zero number\">" + getStatusColor(getStatus(pass, fail, skip, known, fixed)) + "</td>";
				response += "</tr>\n";
			}
			response += "<tr class=\"suite\">\n";
			response += "<td colspan=\"4\">Total</td>";
			// In case of suite with no tests totalEndDate & totalStartDate are
			// null

			if (totalEndDate == null) {
				totalEndDate = 0L;
			}
			if (totalStartDate == null) {
				totalStartDate = 0L;
			}

			response += "<td class=\"duration\">" + formatDurationinMinutes(totalEndDate - totalStartDate) + "</td>";
			if (totalPass > 0) {
				response += "<td class=\"passed number\">" + totalPass + "</td>";
			} else {
				response += "<td class=\"zero number\">" + totalPass + "</td>";
			}
			if (totalSkip > 0) {
				response += "<td class=\"skipped number\">" + totalSkip + "</td>";
			} else {
				response += "<td class=\"zero number\">" + totalSkip + "</td>";
			}
			if (totalKnown > 0) {
				response += "<td class=\"knownDefects number\">" + totalKnown + "</td>";
			} else {
				response += "<td class=\"zero number\">" + totalKnown + "</td>";
			}
			if (totalFixed > 0) {
				response += "<td class=\"fixed number\">" + totalFixed + "</td>";
			} else {
				response += "<td class=\"zero number\">" + totalFixed + "</td>";
			}
			if (totalFail > 0) {
				response += "<td class=\"failed number\">" + totalFail + "</td>";
			} else {
				response += "<td class=\"zero number\">" + totalFail + "</td>";
			}
			response += "<td class=\"zero number\">" + getStatusColor(getStatus(totalPass, totalFail, totalSkip, totalKnown, totalFixed)) + "</td>";
			response += "</tr>\n";
		}
		return response;
	}

	private String getDate(ISuite tempISuite) {
		Date date = new Date(getStartTime(tempISuite.getAllInvokedMethods()));
		return DateFormat.getTimeInstance().format(date);
	}

	private static ResultStatus getStatus(int pass, int fail, int skip, int known, int fixed) {
		ResultStatus status = ResultStatus.PASS;
		if (fail > 0) {
			status = ResultStatus.FAIL;
		} else if (pass == 0 && skip > 0) {
			status = ResultStatus.SKIP;
		} else if (pass > 0 && known > 0) {
			status = ResultStatus.PASS_WITH_KNOWN_ISSUES;
		} else if (pass > 0 && fixed > 0) {
			status = ResultStatus.PASS_WITH_FIXED_ISSUES;
		}
		return status;
	}

	private static String getStatusColor(ResultStatus status) {
		if (ResultStatus.PASS.equals(status)) {
			return "<font color=\"green\">" + ResultStatus.PASS + "</font>";
		} else if (ResultStatus.FAIL.equals(status)) {
			return "<font color=\"red\">" + ResultStatus.FAIL + "</font>";
		} else if (ResultStatus.SKIP.equals(status)) {
			return "<font color=\"yellow\">" + ResultStatus.SKIP + "</font>";
		} else if (ResultStatus.PASS_WITH_FIXED_ISSUES.equals(status)) {
			return "<font color=\"blue\">" + ResultStatus.PASS_WITH_FIXED_ISSUES + "</font>";
		} else if (ResultStatus.PASS_WITH_KNOWN_ISSUES.equals(status)) {
			return "<font color=\"orange\">" + ResultStatus.PASS_WITH_KNOWN_ISSUES + "</font>";
		}
		return status.toString();
	}

	/**
	 * Returns the aggregate of the elapsed times for each test result.
	 * 
	 * @param context
	 *            The test results.
	 * @return The sum of the test durations.
	 */
	public long getDuration(ITestContext context) {
		if (!HTMLReporter.suiteName.equals(context.getSuite().getName())) {
			HTMLReporter.suiteName = context.getSuite().getName();
			HTMLReporter.totalDuration = 0;
		}
		long duration = getDuration(context.getPassedConfigurations().getAllResults());
		duration += getDuration(context.getPassedTests().getAllResults());
		// You would expect skipped tests to have durations of zero, but
		// apparently not.
		duration += getDuration(context.getSkippedConfigurations().getAllResults());
		duration += getDuration(context.getSkippedTests().getAllResults());
		duration += getDuration(context.getFailedConfigurations().getAllResults());
		duration += getDuration(context.getFailedTests().getAllResults());
		HTMLReporter.totalDuration = HTMLReporter.totalDuration + duration;
		return duration;
	}

	public String getTime(ITestContext context) {
		String date = "";
		if (context.getStartDate() != null) {
			date = DateFormat.getTimeInstance().format(context.getStartDate());
		}
		return date;
	}

	public String getTotalDuration() {
		return formatDurationinMinutes(HTMLReporter.totalDuration);
	}

	public static String formatDurationinMinutes(long elapsed) {
		long seconds = (elapsed / 1000) % 60;
		long minutes = (elapsed / (1000 * 60)) % 60;
		long hours = (elapsed / (1000 * 60 * 60)) % 24;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	/**
	 * Returns the aggregate of the elapsed times for each test result.
	 * 
	 * @param results
	 *            A set of test results.
	 * @return The sum of the test durations.
	 */
	private long getDuration(Set<ITestResult> results) {
		long duration = 0;
		for (ITestResult result : results) {
			duration += (result.getEndMillis() - result.getStartMillis());
		}
		return duration;
	}

	public String formatDuration(long startMillis, long endMillis) {
		long elapsed = endMillis - startMillis;
		return formatDuration(elapsed);
	}

	public String formatDuration(long elapsed) {
		double seconds = (double) elapsed / 1000;
		return DURATION_FORMAT.format(seconds);
	}

	/**
	 * Convert a Throwable into a list containing all of its causes.
	 * 
	 * @param t
	 *            The throwable for which the causes are to be returned.
	 * @return A (possibly empty) list of {@link Throwable}s.
	 */
	public List<Throwable> getCauses(Throwable t) {
		List<Throwable> causes = new LinkedList<Throwable>();
		if (t != null) {
			Throwable next = t;
			try {
				while (next.getCause() != null) {
					next = next.getCause();
					causes.add(next);
				}
			} catch (NullPointerException ex) {

			}
		}
		return causes;
	}

	/**
	 * Retrieves all log messages associated with a particular test result.
	 * 
	 * @param result
	 *            Which test result to look-up.
	 * @return A list of log messages.
	 */
	public List<String> getTestOutput(ITestResult result) {
		return Reporter.getOutput(result);
	}

	/**
	 * Retieves the output from all calls to
	 * {@link org.testng.Reporter#log(String)} across all tests.
	 * 
	 * @return A (possibly empty) list of log messages.
	 */
	public List<String> getAllOutput() {
		if ("true".equalsIgnoreCase(System.getProperty(HTMLReporter.LOG_OUTPUT_REPORT))) {
			return Reporter.getOutput();
		}
		return new ArrayList<String>(Arrays.asList("Param '" + HTMLReporter.LOG_OUTPUT_REPORT
				+ "' has neen set to 'false' , so Report Output is not generated."));
	}

	public boolean hasArguments(ITestResult result) {
		return result.getParameters().length > 0;
	}

	public String getClassName(ITestResult result) {
		String name = result.getTestClass().getName();
		String sub = "";
		try {
			sub = name.substring(0, name.indexOf(".") + 1);
			name = name.substring(name.lastIndexOf(".") + 1, name.length());
		} catch (Exception ex) {

		}
		return sub + ".." + name;
	}

	public String getRealClassName(ITestResult result) {
		return result.getTestClass().getName();
	}

	public String getArguments(ITestResult result) {
		Object[] arguments = result.getParameters();
		List<String> argumentStrings = new ArrayList<String>(arguments.length);
		for (Object argument : arguments) {
			argumentStrings.add(renderArgument(argument));
		}
		return commaSeparate(argumentStrings);
	}

	public boolean hasDescription(ITestResult result) {
		String description = "";
		try {
			if (result.getMethod().isTest()) {
				description = result.getMethod().getDescription();
				if (description != null)
					return true;
				else
					return false;
			}
		} catch (NullPointerException ex) {
		}
		return false;
	}

	public String getClassName2(ISuiteResult result) {
		List<XmlClass> list = result.getTestContext().getCurrentXmlTest().getClasses();
		String classNames = "";
		String separateLines = "";
		if (list.size() > 1) {
			separateLines = "<br>";
		}
		for (XmlClass temp : list) {
			classNames += temp.getName() + separateLines;
		}
		return classNames;
	}

	public String getTotalTime(ISuiteResult result) {
		Date start = result.getTestContext().getStartDate();
		Date end = result.getTestContext().getEndDate();
		long diff = end.toInstant().toEpochMilli() - start.toInstant().toEpochMilli();
		return formatDurationinMinutes(diff);
	}

	public int getTotalSteps(ISuiteResult result) {
		return result.getTestContext().getAllTestMethods().length;
	}

	public String getSuiteName(ISuiteResult result) {
		return result.getTestContext().getSuite().getName();
	}

	public String getSuiteXMLName(ISuiteResult result) {
		return result.getTestContext().getSuite().getXmlSuite().getFileName();
	}

	public String getTestStatus(ISuiteResult result) {
		if (result.getTestContext().getFailedTests().size() > 0) {
			return getStatusColor(ResultStatus.FAIL);
		}
		if (result.getTestContext().getSkippedTests().size() > 0) {
			return getStatusColor(ResultStatus.SKIP);
		}
		return getStatusColor(ResultStatus.PASS);
	}

	public boolean hasPriority(ITestResult result) {
		int priority = 0;
		try {
			if (result.getMethod().isTest()) {
				priority = result.getMethod().getPriority();
				if (priority != 0)
					return true;
				else
					return false;
			}
		} catch (NullPointerException ex) {
		}
		return false;
	}

	public boolean hasGroups(ITestResult result) {
		String[] groups = null;
		try {
			if (result.getMethod().isTest()) {
				groups = result.getMethod().getGroups();
				if (groups.length != 0) {
					return true;
				}
				return false;
			}
		} catch (NullPointerException ex) {
		}
		return false;
	}

	public String getGroups(ITestResult result) {
		String[] groups = null;
		String foundGroups = "";
		try {
			if (result.getMethod().isTest()) {
				groups = result.getMethod().getGroups();
				if (groups.length != 0) {
					foundGroups = "";
					for (int i = 0; i < groups.length; i++) {
						foundGroups += groups[i] + ",";
					}
					return foundGroups.substring(0, foundGroups.length() - 1);
				}
			}
		} catch (NullPointerException ex) {
		}
		return "";
	}

	/**
	 * Is there a Known Defect Description
	 * 
	 * @param result
	 * @return
	 */
	public boolean hasKnownDefectsDescription(ITestResult result) {
		if (result.getAttribute(TEST) != null) {
			if (KNOWN.equalsIgnoreCase(result.getAttribute(TEST).toString()) || FIXED.equalsIgnoreCase(result.getAttribute(TEST).toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get Known Defect Description
	 * 
	 * @param result
	 * @return
	 */
	public String getKnownDefectDescription(ITestResult result) {
		Annotation[] annotation = getDeclaredAnnotations(result);
		boolean contains = false;
		String description = "-";
		for (Annotation tempAnnotation : annotation) {
			if (tempAnnotation.toString().contains(KNOWNDEFECT)) {
				contains = true;
				description = tempAnnotation.toString();
				break;
			}
		}
		if (contains) {
			return getDescription(description);
		}
		return description;
	}

	public boolean hasKnownDefect(ITestResult result) {
		if (result.getAttribute(TEST) != null) {
			if (KNOWN.equalsIgnoreCase(result.getAttribute(TEST).toString())) {
				return true;
			}
		}
		return false;
	}

	public boolean hasFixed(ITestResult result) {
		if (result.getAttribute(TEST) != null) {
			if (FIXED.equalsIgnoreCase(result.getAttribute(TEST).toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Parse Known Defect Description
	 * 
	 * @param text
	 * @return
	 */
	private static String getDescription(String text) {
		try {
			String[] splitted = text.split("=");
			return splitted[1].substring(0, splitted[1].length() - 1);
		} catch (Exception ex) {

		}
		return text;
	}

	public List<ITestResult> getPassedConfigurations(IClass classTest, Map<IClass, List<ITestResult>> passedConfigurations) {
		List<ITestResult> newmethods = new ArrayList<ITestResult>();
		if ("true".equals(System.getProperty(HTMLReporter.SHOW_PASSED_CONFIGURATIONS))) {
			Method[] methodsInClass = classTest.getRealClass().getDeclaredMethods();
			List<ITestResult> methods = passedConfigurations.get(classTest);
			for (ITestResult temp : methods) {
				if (!temp.getMethod().isTest()) {
					for (Method tempMethod : methodsInClass) {
						if (temp.getMethod().getMethodName().equals(tempMethod.getName()) && !newmethods.contains(tempMethod.getName())) {
							newmethods.add(temp);
							break;
						}
					}
				}
			}
		}
		return newmethods;
	}

	public int getPriority(ITestResult result) {
		int priority = 0;
		try {
			priority = result.getMethod().getPriority();
			if (priority != 0) {
				return priority;
			} else {
				return 0;
			}
		} catch (NullPointerException ex) {

		}
		return 0;
	}

	/**
	 * Decorate the string representation of an argument to give some hint as to
	 * its type (e.g. render Strings in double quotes).
	 * 
	 * @param argument
	 *            The argument to render.
	 * @return The string representation of the argument.
	 */
	private String renderArgument(Object argument) {
		if (argument == null) {
			return "null";
		} else if (argument instanceof String) {
			return "\"" + argument + "\"";
		} else if (argument instanceof Character) {
			return "\'" + argument + "\'";
		} else {
			return argument.toString();
		}
	}

	/**
	 * @param result
	 *            The test result to be checked for dependent groups.
	 * @return True if this test was dependent on any groups, false otherwise.
	 */
	public boolean hasDependentGroups(ITestResult result) {
		return result.getMethod().getGroupsDependedUpon().length > 0;
	}

	/**
	 * @return A comma-separated string listing all dependent groups. Returns an
	 *         empty string it there are no dependent groups.
	 */
	public String getDependentGroups(ITestResult result) {
		String[] groups = result.getMethod().getGroupsDependedUpon();
		return commaSeparate(Arrays.asList(groups));
	}

	/**
	 * @param result
	 *            The test result to be checked for dependent methods.
	 * @return True if this test was dependent on any methods, false otherwise.
	 */
	public boolean hasDependentMethods(ITestResult result) {
		return result.getMethod().getMethodsDependedUpon().length > 0;
	}

	/**
	 * @return A comma-separated string listing all dependent methods. Returns
	 *         an empty string it there are no dependent methods.
	 */
	public String getDependentMethods(ITestResult result) {
		String[] methods = result.getMethod().getMethodsDependedUpon();
		return commaSeparate(Arrays.asList(methods));
	}

	public boolean hasSkipException(ITestResult result) {
		return result.getThrowable() instanceof SkipException;
	}

	public String getSkipExceptionMessage(ITestResult result) {
		return hasSkipException(result) ? result.getThrowable().getMessage() : "";
	}

	public boolean hasGroups(ISuite suite) {
		return !suite.getMethodsByGroups().isEmpty();
	}

	public String getClassName(ITestContext context) {
		String name = "N/A";
		try {
			name = context.getAllTestMethods()[0].getClass().toString();
		} catch (Exception ex) {
		}
		return name;
	}

	/**
	 * Takes a list of Strings and combines them into a single comma-separated
	 * String.
	 * 
	 * @param strings
	 *            The Strings to combine.
	 * @return The combined, comma-separated, String.
	 */
	private String commaSeparate(Collection<String> strings) {
		StringBuilder buffer = new StringBuilder();
		Iterator<String> iterator = strings.iterator();
		while (iterator.hasNext()) {
			String string = iterator.next();
			buffer.append(string);
			if (iterator.hasNext()) {
				buffer.append(", ");
			}
		}
		return buffer.toString();
	}

	/**
	 * Replace any angle brackets, quotes, apostrophes or ampersands with the
	 * corresponding XML/HTML entities to avoid problems displaying the String
	 * in an XML document. Assumes that the String does not already contain any
	 * entities (otherwise the ampersands will be escaped again).
	 * 
	 * @param s
	 *            The String to escape.
	 * @return The escaped String.
	 */
	public static String escapeString(String s) {
		if (s == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			buffer.append(escapeChar(s.charAt(i)));
		}
		return buffer.toString();
	}

	/**
	 * Converts a char into a String that can be inserted into an XML document,
	 * replacing special characters with XML entities as required.
	 * 
	 * @param character
	 *            The character to convert.
	 * @return An XML entity representing the character (or a String containing
	 *         just the character if it does not need to be escaped).
	 */
	private static String escapeChar(char character) {
		switch (character) {
		case '<':
			return "&lt;";
		case '>':
			return "&gt;";
		case '"':
			return "&quot;";
		case '\'':
			return "&apos;";
		case '&':
			return "&amp;";
		default:
			return String.valueOf(character);
		}
	}

	/**
	 * Works like {@link #escapeString(String)} but also replaces line breaks
	 * with &lt;br /&gt; tags and preserves significant whitespace.
	 * 
	 * @param s
	 *            The String to escape.
	 * @return The escaped String.
	 */
	public String escapeHTMLString(String s) {
		if (s == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case ' ':
				// All spaces in a block of consecutive spaces are converted to
				// non-breaking space (&nbsp;) except for the last one. This
				// allows
				// significant whitespace to be retained without prohibiting
				// wrapping.
				char nextCh = i + 1 < s.length() ? s.charAt(i + 1) : 0;
				buffer.append(nextCh == ' ' ? "&nbsp;" : " ");
				break;
			case '\n':
				buffer.append("<br/>\n");
				break;
			default:
				buffer.append(escapeChar(ch));
			}
		}
		return buffer.toString();
	}

	/**
	 * TestNG returns a compound thread ID that includes the thread name and its
	 * numeric ID, separated by an 'at' sign. We only want to use the thread
	 * name as the ID is mostly unimportant and it takes up too much space in
	 * the generated report.
	 * 
	 * @param threadId
	 *            The compound thread ID.
	 * @return The thread name.
	 */
	public String stripThreadName(String threadId) {
		if (threadId == null) {
			return null;
		} else {
			int index = threadId.lastIndexOf('@');
			return index >= 0 ? threadId.substring(0, index) : threadId;
		}
	}

	/**
	 * Find the earliest start time of the specified methods.
	 * 
	 * @param methods
	 *            A list of test methods.
	 * @return The earliest start time.
	 */
	public long getStartTime(List<IInvokedMethod> methods) {
		long startTime = System.currentTimeMillis();
		for (IInvokedMethod method : methods) {
			startTime = Math.min(startTime, method.getDate());
		}
		return startTime;
	}

	public long getEndTime(ISuite suite, IInvokedMethod method, List<IInvokedMethod> methods) {
		boolean found = false;
		for (IInvokedMethod m : methods) {
			if (m == method) {
				found = true;
			}
			// Once a method is found, find subsequent method on same thread.
			else if (found && m.getTestMethod().getId().equals(method.getTestMethod().getId())) {
				return m.getDate();
			}
		}
		return getEndTime(suite, method);
	}

	public int getSuiteState(ISuite suite) {
		int passed = 0;
		int failed = 0;
		int skip = 0;
		Map<String, ISuiteResult> map = suite.getResults();
		for (String key : map.keySet()) {
			passed += map.get(key).getTestContext().getPassedTests().size();
			failed += map.get(key).getTestContext().getFailedTests().size();
			skip += map.get(key).getTestContext().getSkippedTests().size();
		}
		return passed + failed + skip;
	}

	public String getSuiteName(ISuite suite) {
		if (suite != null)
			return suite.getName().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("\"", "_");
		else
			return "N/A";
	}

	/**
	 * Returns the timestamp for the time at which the suite finished executing.
	 * This is determined by finding the latest end time for each of the
	 * individual tests in the suite.
	 * 
	 * @param suite
	 *            The suite to find the end time of.
	 * @return The end time (as a number of milliseconds since 00:00 1st January
	 *         1970 UTC).
	 */
	private long getEndTime(ISuite suite, IInvokedMethod method) {
		// Find the latest end time for all tests in the suite.
		for (Map.Entry<String, ISuiteResult> entry : suite.getResults().entrySet()) {
			ITestContext testContext = entry.getValue().getTestContext();
			for (ITestNGMethod m : testContext.getAllTestMethods()) {
				if (method == m) {
					return testContext.getEndDate().getTime();
				}
			}
			// If we can't find a matching test method it must be a
			// configuration method.
			for (ITestNGMethod m : testContext.getPassedConfigurations().getAllMethods()) {
				if (method == m) {
					return testContext.getEndDate().getTime();
				}
			}
			for (ITestNGMethod m : testContext.getFailedConfigurations().getAllMethods()) {
				if (method == m) {
					return testContext.getEndDate().getTime();
				}
			}
		}
		throw new IllegalStateException("Could not find matching end time.");
	}

	public static boolean isRegression(ITestContext context) {
		for (ITestNGMethod tempTestMethod : context.getAllTestMethods()) {
			Annotation[] annotations = tempTestMethod.getTestClass().getRealClass().getAnnotations();
			for (Annotation tempAnnotation : annotations) {
				if (tempAnnotation.toString().contains(NEW_FEATURE.toString())) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isNewFeature(ITestContext context) {
		for (ITestNGMethod tempTestMethod : context.getAllTestMethods()) {
			Annotation[] annotations = tempTestMethod.getTestClass().getRealClass().getAnnotations();
			for (Annotation tempAnnotation : annotations) {
				if (tempAnnotation.toString().contains(NEW_FEATURE.toString())) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isFeature(ITestContext iTestContext) {
		for (ITestNGMethod tempTestMethod : iTestContext.getAllTestMethods()) {
			Annotation[] annotations = tempTestMethod.getTestClass().getRealClass().getAnnotations();
			for (Annotation tempAnnotation : annotations) {
				if (tempAnnotation.toString().contains(FEATURE.toString())) {
					return true;
				}
			}
		}
		return false;
	}

	public static String getNewFeatureDescription(ITestContext iTestContext) {
		for (ITestNGMethod tempTestMethod : iTestContext.getAllTestMethods()) {
			Annotation[] annotations = tempTestMethod.getTestClass().getRealClass().getAnnotations();
			for (Annotation tempAnnotation : annotations) {
				if (tempAnnotation.toString().contains(NEW_FEATURE.toString())) {
					return getDescription(tempAnnotation.toString());
				}
			}
		}
		return "";
	}

	public static String getFeatureDescription(ITestContext context) {
		for (ITestNGMethod tempTestMethod : context.getAllTestMethods()) {
			Annotation[] annotations = tempTestMethod.getTestClass().getRealClass().getAnnotations();
			for (Annotation tempAnnotation : annotations) {
				if (tempAnnotation.toString().contains(FEATURE.toString())) {
					return getDescription(tempAnnotation.toString());
				}
			}
		}
		return "";
	}

	public String getDescription(ISuiteResult iSuiteResult) {
		for (ITestNGMethod tempTestMethod : iSuiteResult.getTestContext().getAllTestMethods()) {
			Annotation[] annotations = tempTestMethod.getTestClass().getRealClass().getAnnotations();
			for (Annotation tempAnnotation : annotations) {
				if (tempAnnotation.toString().contains(FEATURE.toString()) || tempAnnotation.toString().contains(NEW_FEATURE.toString())) {
					return getDescription(tempAnnotation.toString());
				}
			}
		}
		return "";
	}

	public boolean hasDescription(ISuiteResult iSuiteResult) {
		for (ITestNGMethod tempTestMethod : iSuiteResult.getTestContext().getAllTestMethods()) {
			Annotation[] annotations = tempTestMethod.getTestClass().getRealClass().getAnnotations();
			for (Annotation tempAnnotation : annotations) {
				if (tempAnnotation.toString().contains(FEATURE.toString()) || tempAnnotation.toString().contains(NEW_FEATURE.toString())) {
					return true;
				}
			}
		}
		return false;
	}

	public String getFeatures(Map<String, List<IssueDTO>> features) {
		String response = "";
		if (features != null) {
			Iterator<Entry<String, List<IssueDTO>>> it = features.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, List<IssueDTO>> pair = it.next();
				response += "<tr>\n";
				response += "<td rowspan=\"" + pair.getValue().size() + "\" style=\"vertical-align:middle\" id=\"" + pair.getKey() + "\">" + pair.getKey()
						+ "</td>\n";
				ResultStatus overAllStatus = ResultStatus.PASS;
				for (IssueDTO temp : pair.getValue()) {
					if (ResultStatus.FAIL.equals(temp.getStatus())) {
						overAllStatus = ResultStatus.FAIL;
					} else if (ResultStatus.PASS_WITH_FIXED_ISSUES.equals(temp.getStatus())) {
						overAllStatus = ResultStatus.PASS_WITH_FIXED_ISSUES;
					} else if (ResultStatus.PASS_WITH_KNOWN_ISSUES.equals(temp.getStatus())) {
						overAllStatus = ResultStatus.PASS_WITH_KNOWN_ISSUES;
					} else if (ResultStatus.SKIP.equals(temp.getStatus())) {
						overAllStatus = ResultStatus.SKIP;
					}

				}
				int index = 1;
				for (IssueDTO temp : pair.getValue()) {
					response += "<td style=\"vertical-align:middle\"><a href=\"suites_overview.html#" + temp.getSuiteName() + "\">" + temp.getSuiteName()
							+ "</a></td>\n";
					response += "<td style=\"vertical-align:middle\"><a href=\"" + temp.getLink() + "\">" + temp.getTestName() + "</a></td>\n";
					response += "<td class=\"break-word\" style=\"vertical-align:middle\">" + temp.getTestClass() + "</td>\n";
					response += "<td>\n<div>" + temp.getResults() + "</div>\n</td>\n";
					if (index == 1) {
						response += "<td rowspan=\"" + pair.getValue().size() + "\" style=\"vertical-align:middle;text-align:center;\">"
								+ getStatusColor(overAllStatus) + "</td>\n";
						index++;
					} else {
					}
					response += "</tr>\n";
				}
			}
		}
		return response;
	}

	public String getReportOutput() {
		return HTMLReporter.OUTPUTDIRECTORY_ABSOLUTE;
	}

	// Graphs
	public String graphTime(List<ISuite> suites) {
		String text = "";
		text += "var chart = new CanvasJS.Chart(\"chartContainer\", {" + "\n";
		text += "animationEnabled: true," + "\n";
		text += "height: 600," + "\n";
		text += "indexLabelFontSize: 16," + "\n";
		text += "theme: \"light2\"," + "\n";
		text += "title:{text: \"\"}," + "\n";
		text += "axisX:{title: \"Time \", valueFormatString: \"DD MMM hh:mm TT\"}," + "\n";
		text += "axisY: {title: \"Number of Tests\"}," + "\n";
		text += "toolTip:{shared:true}, " + "\n";
		text += "legend:{cursor:\"pointer\",verticalAlign: \"top\",horizontalAlign: \"left\",dockInsidePlotArea: false, fontSize: 16}," + "\n";
		text += "data: [" + "\n";
		text += "{type: \"area\",showInLegend: true,name: \"Pass\",markerType: \"square\",xValueFormatString: \"DD MMM hh:mm TT\",color: \"green\",dataPoints: ["
				+ "\n";
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				Date date = iSuiteResult.getTestContext().getStartDate();
				text += "{ x: new Date(" +
						(1900 + date.getYear()) + ", " +
						date.getMonth() + ", " +
						date.getDate() + ", " +
						date.getHours() + ", " +
						date.getMinutes() + ", " +
						date.getSeconds() + "), y: " + getPassed(iSuiteResult.getTestContext()).size() + " }," + "\n";
			}
		}
		text += "]}," + "\n";

		text += "{type: \"area\",showInLegend: true,name: \"Fixed\",markerType: \"square\",xValueFormatString: \"DD MMM hh:mm TT\",color: \"blue\",dataPoints: ["
				+ "\n";
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				Date date = iSuiteResult.getTestContext().getStartDate();
				text += "{ x: new Date(" +
						(1900 + date.getYear()) + ", " +
						date.getMonth() + ", " +
						date.getDate() + ", " +
						date.getHours() + ", " +
						date.getMinutes() + ", " +
						date.getSeconds() + "), y: " + getFixed(iSuiteResult.getTestContext()).size() + " }," + "\n";
			}
		}
		text += "]}," + "\n";

		text += "{type: \"area\",showInLegend: true,name: \"Known Defects\",markerType: \"square\",xValueFormatString: \"DD MMM hh:mm TT\",color: \"orange\",dataPoints: ["
				+ "\n";
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				Date date = iSuiteResult.getTestContext().getStartDate();
				text += "{ x: new Date(" +
						(1900 + date.getYear()) + ", " +
						date.getMonth() + ", " +
						date.getDate() + ", " +
						date.getHours() + ", " +
						date.getMinutes() + ", " +
						date.getSeconds() + "), y: " + getKnownDefect(iSuiteResult.getTestContext()).size() + " }," + "\n";
			}
		}
		text += "]}," + "\n";

		text += "{type: \"area\",showInLegend: true,name: \"Fail\",markerType: \"square\",xValueFormatString: \"DD MMM hh:mm TT\",color: \"red\",dataPoints: ["
				+ "\n";
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				Date date = iSuiteResult.getTestContext().getStartDate();
				text += "{ x: new Date(" +
						(1900 + date.getYear()) + ", " +
						date.getMonth() + ", " +
						date.getDate() + ", " +
						date.getHours() + ", " +
						date.getMinutes() + ", " +
						date.getSeconds() + "), y: " + getFailed(iSuiteResult.getTestContext()).size() + " }," + "\n";
			}
		}
		text += "]}," + "\n";

		text += "{type: \"area\",showInLegend: true,name: \"Skip\",markerType: \"square\",xValueFormatString: \"DD MMM hh:mm TT\",color: \"yellow\",dataPoints: ["
				+ "\n";
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				Date date = iSuiteResult.getTestContext().getStartDate();
				text += "{ x: new Date(" +
						(1900 + date.getYear()) + ", " +
						date.getMonth() + ", " +
						date.getDate() + ", " +
						date.getHours() + ", " +
						date.getMinutes() + ", " +
						date.getSeconds() + "), y: " + getSkip(iSuiteResult.getTestContext()).size() + " }," + "\n";
			}
		}
		text += "]}" + "\n";
		text += "]});" + "\n";
		return text;
	}

	public String graphClass(List<ISuite> suites) throws IOException {
		String text = "";
		text += "var chart = new CanvasJS.Chart(\"chartContainer\", {" + "\n";
		text += "animationEnabled: true," + "\n";
		text += "indexLabelFontSize: 16," + "\n";
		text += "height: 600," + "\n";
		text += "theme: \"light2\"," + "\n";
		text += "title:{text: \"\"}," + "\n";
		text += "axisX:{}," + "\n";
		text += "axisY: {title: \"Class\"}," + "\n";
		text += "toolTip:{shared:true}, " + "\n";
		text += "legend:{cursor:\"pointer\",verticalAlign: \"top\",horizontalAlign: \"left\",dockInsidePlotArea: false, fontSize: 16}," + "\n";
		text += "data: [" + "\n";

		text += "{type: \"stackedBar\",showInLegend: true,name: \"Pass\",color: \"green\",dataPoints: [" + "\n";
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				text += "{ label: \"" + iSuiteResult.getTestContext().getName() + "\"," + " y: " + getPassed(iSuiteResult.getTestContext()).size() + " },"
						+ "\n";
			}
		}
		text += "]}," + "\n";

		text += "{type: \"stackedBar\",showInLegend: true,name: \"Fixed\",color: \"blue\",dataPoints: [" + "\n";
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				text += "{ label: \"" + iSuiteResult.getTestContext().getName() + "\"," + " y: " + getFixed(iSuiteResult.getTestContext()).size() + " },"
						+ "\n";
			}
		}
		text += "]}," + "\n";

		text += "{type: \"stackedBar\",showInLegend: true,name: \"Known Defects\",color: \"orange\",dataPoints: [" + "\n";
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				text += "{ label: \"" + iSuiteResult.getTestContext().getName() + "\"," + " y: " + getKnownDefect(iSuiteResult.getTestContext()).size() + " },"
						+ "\n";
			}
		}
		text += "]}," + "\n";

		text += "{type: \"stackedBar\",showInLegend: true,name: \"Fail\",color: \"red\",dataPoints: [" + "\n";
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				text += "{ label: \"" + iSuiteResult.getTestContext().getName() + "\"," + " y: " + getFailed(iSuiteResult.getTestContext()).size() + " },"
						+ "\n";
			}
		}
		text += "]}," + "\n";

		text += "{type: \"stackedBar\",showInLegend: true,name: \"Skip\",color: \"yellow\",dataPoints: [" + "\n";
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				text += "{ label: \"" + iSuiteResult.getTestContext().getName() + "\"," + " y: " + getSkip(iSuiteResult.getTestContext()).size() + " }," + "\n";
			}
		}
		text += "]}" + "\n";
		text += "]});" + "\n";
		return text;
	}

	public String graphSuite(List<ISuite> suites) throws IOException {
		String text = "";
		text += "var chart = new CanvasJS.Chart(\"chartContainer\", {" + "\n";
		text += "animationEnabled: true," + "\n";
		text += "indexLabelFontSize: 16," + "\n";
		text += "height: 600," + "\n";
		text += "theme: \"light2\"," + "\n";
		text += "title:{text: \"\"}," + "\n";
		text += "axisX:{}," + "\n";
		text += "axisY: {title: \"Suite\"}," + "\n";
		text += "legend:{cursor:\"pointer\",verticalAlign: \"top\",horizontalAlign: \"left\",dockInsidePlotArea: false, fontSize: 16}," + "\n";
		text += "data: [" + "\n";
		text += "{type: \"stackedBar\",showInLegend: true,name: \"Pass\",color: \"green\",dataPoints: [" + "\n";
		for (ISuite tempISuite : suites) {
			int counter = 0;
			String suiteName = null;
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			suiteName = tempISuite.getName();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				counter += getPassed(iSuiteResult.getTestContext()).size();
			}
			text += "{ label: \"" + suiteName + "\"," + " y: " + counter + " }," + "\n";
		}
		text += "]}," + "\n";

		text += "{type: \"stackedBar\",showInLegend: true,name: \"Fixed\",color: \"blue\",dataPoints: [" + "\n";
		for (ISuite tempISuite : suites) {
			int counter = 0;
			String suiteName = null;
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			suiteName = tempISuite.getName();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				counter += getFixed(iSuiteResult.getTestContext()).size();
			}
			text += "{ label: \"" + suiteName + "\"," + " y: " + counter + " }," + "\n";
		}
		text += "]}," + "\n";

		text += "{type: \"stackedBar\",showInLegend: true,name: \"Known Defects\",color: \"orange\",dataPoints: [" + "\n";
		for (ISuite tempISuite : suites) {
			int counter = 0;
			String suiteName = null;
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			suiteName = tempISuite.getName();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				counter += getKnownDefect(iSuiteResult.getTestContext()).size();
			}
			text += "{ label: \"" + suiteName + "\"," + " y: " + counter + " }," + "\n";
		}
		text += "]}," + "\n";

		text += "{type: \"stackedBar\",showInLegend: true,name: \"Fail\",color: \"red\",dataPoints: [" + "\n";
		for (ISuite tempISuite : suites) {
			int counter = 0;
			String suiteName = null;
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			suiteName = tempISuite.getName();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				counter += getFailed(iSuiteResult.getTestContext()).size();
			}
			text += "{ label: \"" + suiteName + "\"," + " y: " + counter + " }," + "\n";
		}
		text += "]}," + "\n";

		text += "{type: \"stackedBar\",showInLegend: true,name: \"Skip\",color: \"yellow\",dataPoints: [" + "\n";
		for (ISuite tempISuite : suites) {
			int counter = 0;
			String suiteName = null;
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			suiteName = tempISuite.getName();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				counter += getSkip(iSuiteResult.getTestContext()).size();
			}
			text += "{ label: \"" + suiteName + "\"," + " y: " + counter + " }," + "\n";
		}
		text += "]}" + "\n";
		text += "]});" + "\n";
		return text;
	}

	// Used from Suite Listener
	/**
	 * Pass : Pass - Known
	 * 
	 * @param iTestContext
	 * @return
	 */
	public static IResultMap getPassed(ITestContext iTestContext) {
		iTestContext = ReporterHelper.updateResults(iTestContext);
		IResultMap temp = new ResultMap();
		for (ITestResult tr : iTestContext.getPassedTests().getAllResults()) {
			Annotation[] annotation = getDeclaredAnnotations(tr);
			boolean addResult = true;
			for (Annotation tempAnnotation : annotation) {
				if (tempAnnotation.toString().contains(KNOWNDEFECT) && tr.getStatus() == ITestResult.SUCCESS) {
					addResult = false;
					break;
				}
			}
			if (addResult) {
				temp.addResult(tr, tr.getMethod());
			}
		}
		return temp;
	}

	/**
	 * Fail : Failed - Known
	 * 
	 * @param iTestContext
	 * @return
	 */
	public static IResultMap getFailed(ITestContext iTestContext) {
		iTestContext = ReporterHelper.updateResults(iTestContext);
		IResultMap temp = new ResultMap();
		for (ITestResult tr : iTestContext.getFailedTests().getAllResults()) {
			Annotation[] annotation = getDeclaredAnnotations(tr);
			boolean addResult = true;
			for (Annotation tempAnnotation : annotation) {
				if (tempAnnotation.toString().contains(KNOWNDEFECT) && tr.getStatus() == ITestResult.FAILURE) {
					addResult = false;
					break;
				}
			}
			if (addResult) {
				temp.addResult(tr, tr.getMethod());
			}
		}
		return temp;
	}

	public static IResultMap getSkip(ITestContext iTestContext) {
		return iTestContext.getSkippedTests();
	}

	/**
	 * Known : Fail with Known
	 * 
	 * @param iTestContext
	 * @return
	 */
	public static IResultMap getKnownDefect(ITestContext iTestContext) {
		iTestContext = ReporterHelper.updateResults(iTestContext);
		IResultMap temp = new ResultMap();
		for (ITestResult tr : iTestContext.getPassedTests().getAllResults()) {
			Annotation[] annotation = getDeclaredAnnotations(tr);
			boolean addResult = false;
			for (Annotation tempAnnotation : annotation) {
				if (tempAnnotation.toString().contains(KNOWNDEFECT) && KNOWN.equals(tr.getAttribute(TEST))) {
					addResult = true;
					break;
				}
			}
			if (addResult) {
				temp.addResult(tr, tr.getMethod());
			}
		}
		return temp;
	}

	/**
	 * Fixed : Pass with Known
	 * 
	 * @param iTestContext
	 * @return
	 */
	public static IResultMap getFixed(ITestContext iTestContext) {
		iTestContext = ReporterHelper.updateResults(iTestContext);
		IResultMap temp = new ResultMap();
		for (ITestResult tr : iTestContext.getPassedTests().getAllResults()) {
			Annotation[] annotation = getDeclaredAnnotations(tr);
			boolean addResult = false;
			for (Annotation tempAnnotation : annotation) {
				if (tempAnnotation.toString().contains(KNOWNDEFECT) && FIXED.equals(tr.getAttribute(TEST))) {
					addResult = true;
					break;
				}
			}
			if (addResult) {
				temp.addResult(tr, tr.getMethod());
			}
		}
		return temp;
	}

	public static List<IssueDTO> getKnownIssues(String suiteName, String linkName, Set<ITestResult> results) {
		List<IssueDTO> issues = new ArrayList<IssueDTO>();
		for (ITestResult tempITestResult : results) {
			for (ITestResult iTestResult : tempITestResult.getTestContext().getPassedTests().getAllResults()) {
				Annotation[] annotation = getDeclaredAnnotations(iTestResult);
				for (Annotation tempAnnotation : annotation) {
					if (tempAnnotation.toString().contains(KNOWNDEFECT) && KNOWN.equals(iTestResult.getAttribute(TEST))) {
						issues.add(new IssueDTO(suiteName, iTestResult.getTestContext().getName(), iTestResult.getInstanceName(), getDescription(tempAnnotation
								.toString()), linkName, isRegression(iTestResult
								.getTestContext())));
						break;
					}
				}
			}
			break;
		}
		return issues;
	}

	public static List<IssueDTO> getFixedIssues(String suiteName, String linkName, Set<ITestResult> results) {
		List<IssueDTO> issues = new ArrayList<IssueDTO>();
		for (ITestResult tempITestResult : results) {
			for (ITestResult iTestResult : tempITestResult.getTestContext().getPassedTests().getAllResults()) {
				Annotation[] annotation = getDeclaredAnnotations(iTestResult);
				for (Annotation tempAnnotation : annotation) {
					if (tempAnnotation.toString().contains(KNOWNDEFECT) && FIXED.equals(iTestResult.getAttribute(TEST))) {
						issues.add(new IssueDTO(suiteName, iTestResult.getTestContext().getName(), iTestResult.getInstanceName(), getDescription(tempAnnotation
								.toString()), linkName, isRegression(iTestResult
								.getTestContext())));
						break;
					}
				}
			}
			break;
		}
		return issues;
	}

	public static List<IssueDTO> getNewIssues(String suiteName, String linkName, Set<ITestResult> results) {
		List<IssueDTO> issues = new ArrayList<IssueDTO>();
		for (ITestResult tr : results) {
			issues.add(new IssueDTO(suiteName, tr.getTestContext().getName(), tr.getInstanceName(), tr.getThrowable().getMessage(), linkName, isRegression(tr
					.getTestContext())));
		}
		return issues;
	}

	public static List<IssueDTO> getFeatures(String suiteName, String linkName, ITestContext iTestContext) {
		List<IssueDTO> issues = new ArrayList<IssueDTO>();
		if (isFeature(iTestContext)) {
			Set<ITestResult> iResultMapPass = getPassed(iTestContext).getAllResults();
			Set<ITestResult> iResultMapFail = getFailed(iTestContext).getAllResults();
			Set<ITestResult> iResultMapSkip = getSkip(iTestContext).getAllResults();
			Set<ITestResult> iResultMapKnown = getKnownDefect(iTestContext).getAllResults();
			Set<ITestResult> iResultMapFixed = getFixed(iTestContext).getAllResults();

			Set<ITestResult> all = new HashSet<ITestResult>();
			all.addAll(iResultMapPass);
			all.addAll(iResultMapFail);
			all.addAll(iResultMapSkip);
			all.addAll(iResultMapKnown);
			all.addAll(iResultMapFixed);

			String results = getFeatureResults(iResultMapPass, iResultMapFail, iResultMapSkip, iResultMapKnown, iResultMapFixed);
			ResultStatus status = getStatus(iResultMapPass.size(), iResultMapFail.size(), iResultMapFail.size(), iResultMapKnown.size(), iResultMapFixed.size());
			for (ITestResult iTestResult : all) {
				issues.add(new IssueDTO(suiteName, iTestResult.getTestContext().getName(), iTestResult.getInstanceName(), getFeatureDescription(iTestContext),
						linkName, results, status));
				break;
			}

		}
		return issues;
	}

	public static List<IssueDTO> getNewFeatures(String suiteName, String linkName, ITestContext iTestContext) {
		List<IssueDTO> issues = new ArrayList<IssueDTO>();
		if (isNewFeature(iTestContext)) {
			Set<ITestResult> iResultMapPass = getPassed(iTestContext).getAllResults();
			Set<ITestResult> iResultMapFail = getFailed(iTestContext).getAllResults();
			Set<ITestResult> iResultMapSkip = getSkip(iTestContext).getAllResults();
			Set<ITestResult> iResultMapKnown = getKnownDefect(iTestContext).getAllResults();
			Set<ITestResult> iResultMapFixed = getFixed(iTestContext).getAllResults();

			Set<ITestResult> all = new HashSet<ITestResult>();
			all.addAll(iResultMapPass);
			all.addAll(iResultMapFail);
			all.addAll(iResultMapSkip);
			all.addAll(iResultMapKnown);
			all.addAll(iResultMapFixed);

			String results = getFeatureResults(iResultMapPass, iResultMapFail, iResultMapSkip, iResultMapKnown, iResultMapFixed);
			ResultStatus status = getStatus(iResultMapPass.size(), iResultMapFail.size(), iResultMapFail.size(), iResultMapKnown.size(), iResultMapFixed.size());
			for (ITestResult iTestResult : all) {
				boolean alreadyExists = false;
				for (IssueDTO tempIssueDTO : issues) {
					if (tempIssueDTO.getIssueDescription().equalsIgnoreCase(getNewFeatureDescription(iTestContext))) {
						alreadyExists = true;
						break;
					}
				}
				if (!alreadyExists) {
					issues.add(new IssueDTO(suiteName, iTestResult.getTestContext().getName(), iTestResult.getInstanceName(),
							getNewFeatureDescription(iTestContext), linkName, results, status));
					break;
				}
			}
		}
		return issues;
	}

	private static String getFeatureResults(Set<ITestResult> iResultMapPass, Set<ITestResult> iResultMapFail, Set<ITestResult> iResultMapSkip,
			Set<ITestResult> iResultMapKnown, Set<ITestResult> iResultMapFixed) {
		String results = "<table class=\"table\">" + "\n";
		if (iResultMapPass.size() > 0) {
			results += "<tr class=\"passed number\"><th>PASS</th>" + "<th>" + iResultMapPass.size() + "</th></tr>";
		}
		if (iResultMapFail.size() > 0) {
			results += "<tr class=\"failed number\"><th>FAIL</th>" + "<th>" + iResultMapFail.size() + "</th></tr>";
		}
		if (iResultMapSkip.size() > 0) {
			results += "<tr class=\"skipped number\"><th>SKIP</th>" + "<th>" + iResultMapSkip.size() + "</th></tr>";
		}
		if (iResultMapKnown.size() > 0) {
			results += "<tr class=\"knownDefects number\"><th>KNOWN</th>" + "<th>" + iResultMapKnown.size() + "</th></tr>";
		}
		if (iResultMapFixed.size() > 0) {
			results += "<tr class=\"fixed number\"><th>FIXED</th>" + "<th>" + iResultMapFixed.size() + "</th></tr>";
		}
		results += "</table>";
		return results;
	}

	@SuppressWarnings("deprecation")
	private static Annotation[] getDeclaredAnnotations(ITestResult result) {
		return result.getMethod().getMethod().getDeclaredAnnotations();
	}

	public String getProgress(double per) {
		if (per == 100) {
			return "<div class=\"progress\" role=\"progressbar\" style=\"width: 100%; background-color:green; color:white\">" + per + "%</div>";
		} else if (per == 0) {
			return "";
		}
		double fail = 100 - per;
		/*return "<div class=\"progress\">" +
				"<div class=\"progress-bar\" role=\"progressbar\" style=\"width: " + per + "%; background-color:green; color:white\">" + per + "%</div>" +
				"<div class=\"progress-bar\" role=\"progressbar\" style=\"width: " + fail + "%; background-color:red; color:white\">" + fail + "%</div>" +
				"</div>";*/
		return "<div class=\"progress\" role=\"progressbar\" style=\"width: 100%; background-color:red; color:white\">" + per + "%</div>";
	}

	public String randomId() {
		return UUID.randomUUID().toString();
	}
}