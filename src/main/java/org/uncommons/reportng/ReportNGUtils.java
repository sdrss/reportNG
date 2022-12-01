package org.uncommons.reportng;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
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
import org.uncommons.reportng.dto.PackageDetailsDTO;
import org.uncommons.reportng.dto.ResultStatus;
import org.uncommons.reportng.dto.ResultsDTO;
import org.uncommons.reportng.dto.SuiteConfigurationDTO;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

/**
 * Utility class that provides various helper methods that can be invoked from a Velocity template.
 */
public class ReportNGUtils {
	
	public static final String TEST = "test";
	public static final String FIXED = "fixed";
	public static final String KNOWN = "known";
	private static final NumberFormat DURATION_FORMAT = new DecimalFormat("#0.000");
	private static final String KNOWNDEFECT = KnownDefect.class.getName();
	private static final String NEW_FEATURE = NewFeature.class.getName();
	private static final String FEATURE = Feature.class.getName();
	private static boolean showHideReportFeatureFlag = false;
	
	public enum SuiteConfigurationType {
		AFTERSUITE,
		BEFORESUITE
	}
	
	public static ResultsDTO checkAttribute(List<ISuite> suites) {
		return HTMLReporter.getResults();
	}
	
	public static IssuesDTO issues(List<ISuite> suites) {
		return HTMLReporter.getIssuesDTO();
	}
	
	public static String getExternalLinks() {
		StringBuilder response = new StringBuilder();
		String externalLinks = System.getProperty(HTMLReporter.EXTERNAL_LINKS);
		if (externalLinks != null && !externalLinks.isEmpty()) {
			// Serialize
			ObjectMapper mapper = new ObjectMapper();
			try {
				Map<String, String> resultMap = mapper.readValue(externalLinks, new TypeReference<Map<String, String>>() {
				});
				response.append("<a href=\"#pageSubmenuExtLinks\" class=\"list-group-item\" data-toggle=\"collapse\" aria-expanded=\"false\">\n");
				response.append("<span class=\"glyphicon glyphicon-paperclip\"></span>&nbsp;&nbsp;External Links</a>\n");
				response.append("<ul class=\"collapse list-unstyled\" id=\"pageSubmenuExtLinks\">\n");
				Iterator<Map.Entry<String, String>> itr = resultMap.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry<String, String> entry = itr.next();
					response.append("<a href=\"" + entry.getValue() +
							"\" class=\"list-group-item\" target=\"overview\">&nbsp;&nbsp;&nbsp;" + entry.getKey() + "</a>" + "\n");
				}
			} catch (Exception e) {
			}
		}
		return response.toString();
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
		StringBuilder status = new StringBuilder();
		if ("true".equalsIgnoreCase(System.getProperty(HTMLReporter.SKIP_EXECUTION))) {
			status.append("<b><font color=\"red\">Skip Execution due to \"Skip Execution Mode\".</font></b><br>");
		} else {
			boolean releaseRegression = true;
			boolean emptyReport = false;
			boolean skippedReport = false;
			StringBuilder responseStatus = new StringBuilder("");
			StringBuilder responseNewFeatures = new StringBuilder("");
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
					responseStatus.append("<li>There is <a href=\"newIssues.html\"/>one</a> regression failure, affecting ");
					if (resultsDTO.getRegressionFail() == 1) {
						responseStatus.append(resultsDTO.getRegressionFail() + " test.</li>");
					} else {
						responseStatus.append(resultsDTO.getRegressionFail() + " tests.</li>");
					}
					releaseRegression = false;
				} else if (numberofRegressionIssues > 1) {
					responseStatus.append("<li>There are <a href=\"newIssues.html\"/>" + numberofRegressionIssues + "</a> regression failures, affecting ");
					if (resultsDTO.getRegressionFail() == 1) {
						responseStatus.append(resultsDTO.getRegressionFail() + " test.</li>");
					} else {
						responseStatus.append(resultsDTO.getRegressionFail() + " tests.</li>");
					}
					releaseRegression = false;
				} else if (resultsDTO.getRegressionFail() == 0 && resultsDTO.getRegressionPass() > 0) {
					responseStatus.append("<li>There are no Regression Issues.</li>");
				}
				if (resultsDTO.getNewFeatures() > 0) {
					int numberofNewFeatureIssues = getNumberofNewFeatureIssues(issuesDTO);
					if (releaseRegression && resultsDTO.getRegressionPass() == 0 && numberofNewFeatureIssues > 0) {
						releaseRegression = false;
					}
					if (numberofNewFeatureIssues == 1) {
						responseStatus.append("<li>There is <a href=\"newIssues.html\"/>one</a> failure related with new features, affecting ");
						if (resultsDTO.getNewFeaturesFail() == 1) {
							responseStatus.append(resultsDTO.getNewFeaturesFail() + " test.</li>");
						} else {
							responseStatus.append(resultsDTO.getNewFeaturesFail() + " tests.</li>");
						}
					} else if (numberofNewFeatureIssues > 1) {
						responseStatus.append("<li>There are <a href=\"newIssues.html\"/>" + numberofNewFeatureIssues
								+ "</a> failures related with new features, affecting ");
						if (resultsDTO.getNewFeaturesFail() == 1) {
							responseStatus.append(resultsDTO.getNewFeaturesFail() + " test.</li>");
						} else {
							responseStatus.append(resultsDTO.getNewFeaturesFail() + " tests.</li>");
						}
					} else if (resultsDTO.getNewFeaturesFail() == 0 && resultsDTO.getNewFeaturesPass() > 0) {
					}
					Iterator<Entry<String, List<IssueDTO>>> it = issuesDTO.getNewFeature().entrySet().iterator();
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
							responseNewFeatures.append("<li>The new feature with description '<a href=\"" + HTMLReporter.FEATURES + "#" + pair.getKey()
									+ "\" style=\"color:green\">" + pair.getKey()
									+ "</a>' has no failures and can be announced !</li>");
						} else if (ResultStatus.FAIL.equals(overAllStatus)) {
							responseNewFeatures.append("<li>The new feature with description '<a href=\"" + HTMLReporter.FEATURES + "#" + pair.getKey()
									+ "\" style=\"color:red\">"
									+ pair.getKey()
									+ "</a>' has failures and should not be announced !</li>");
						} else if (ResultStatus.PASS_WITH_KNOWN_ISSUES.equals(overAllStatus)) {
							responseNewFeatures.append("<li>The new feature with description '<a href=\"" + HTMLReporter.FEATURES + "#" + pair.getKey()
									+ "\" style=\"color:orange\">" + pair.getKey()
									+ "</a>' has Known issues and should not be announced !</li>");
						} else if (ResultStatus.SKIP.equals(overAllStatus)) {
							responseNewFeatures.append("<li>The new feature with description '<a href=\"" + HTMLReporter.FEATURES + "#" + pair.getKey()
									+ "\" style=\"color:yellow\">" + pair.getKey()
									+ "</a>' has skip test cases and should not be announced !</li>");
						}
						
					}
					responseNewFeatures.append("");
				}
			}
			responseStatus.append("");
			if (releaseRegression) {
				// status.append("<font color=\"green\">You can Proceed with a new Release !</font><br>");
				status.append("<ul>");
				status.append(responseStatus);
				status.append(responseNewFeatures);
				status.append("</ul>");
			} else {
				if (emptyReport) {
					status.append("<font color=\"black\">Empty Report !</font><br>");
					status.append("<ul>");
					status.append(responseStatus);
					status.append(responseNewFeatures);
					status.append("</ul>");
				} else if (skippedReport) {
					status.append("<font color=\"red\">All test are Skipped !</font><br>");
					status.append("<ul>");
					status.append(responseStatus);
					status.append(responseNewFeatures);
					status.append("</ul>");
				} else {
					// status.append("<font color=\"red\">You should not Proceed with a new Release !</font><br>");
					status.append("<ul>");
					status.append(responseStatus);
					status.append(responseNewFeatures);
					status.append("</ul>");
				}
			}
			
		}
		return status.toString();
		
	}
	
	public String runArguments(List<ISuite> suites) {
		StringBuilder response = new StringBuilder();
		// Get Listeners
		Set<String> listeners = new HashSet<>();
		if (suites != null && !suites.isEmpty()) {
			for (ISuite tempISuite : suites) {
				listeners.addAll(tempISuite.getXmlSuite().getListeners());
			}
		}
		StringBuilder listenersToString = new StringBuilder();
		for (String temp : listeners) {
			listenersToString.append(temp + "<br>");
		}
		if (listenersToString != null && !Strings.isNullOrEmpty(listenersToString.toString())) {
			response.append("<tr>\n");
			response.append("<td>Listeners</td>\n");
			response.append("<td>" + listenersToString.toString() + "</td>\n");
			response.append("</tr>\n");
		}
		
		// Get Include Groups
		Set<String> includeGroups = new HashSet<>();
		if (suites != null && !suites.isEmpty()) {
			for (ISuite tempISuite : suites) {
				includeGroups.addAll(tempISuite.getXmlSuite().getIncludedGroups());
			}
		}
		StringBuilder includeGroupsToString = new StringBuilder();
		for (String temp : includeGroups) {
			includeGroupsToString.append(temp + "<br>");
		}
		if (includeGroupsToString != null && !Strings.isNullOrEmpty(includeGroupsToString.toString())) {
			response.append("<tr>\n");
			response.append("<td>Include Groups</td>\n");
			response.append("<td>" + includeGroupsToString + "</td>\n");
			response.append("</tr>\n");
		}
		// Get Exclude Groups
		Set<String> excludeGroups = new HashSet<>();
		if (suites != null && !suites.isEmpty()) {
			for (ISuite tempISuite : suites) {
				excludeGroups.addAll(tempISuite.getXmlSuite().getExcludedGroups());
			}
		}
		StringBuilder excludeGroupsToString = new StringBuilder();
		for (String temp : excludeGroups) {
			excludeGroupsToString.append(temp + "<br>");
		}
		if (excludeGroupsToString != null && !Strings.isNullOrEmpty(excludeGroupsToString.toString())) {
			response.append("<tr>\n");
			response.append("<td>Exclude Groups</td>\n");
			response.append("<td>" + excludeGroupsToString + "</td>\n");
			response.append("</tr>\n");
		}
		return response.toString();
	}
	
	public String getIssues(Map<String, List<IssueDTO>> issues) {
		StringBuilder response = new StringBuilder("");
		if (issues != null && !issues.isEmpty()) {
			Map<String, List<IssueDTO>> map = new TreeMap<>(issues);
			Iterator<Entry<String, List<IssueDTO>>> it = map.entrySet().iterator();
			int indexCounter = 0;
			while (it.hasNext()) {
				Entry<String, List<IssueDTO>> pair = it.next();
				
				UUID id = UUID.randomUUID();
				response.append("<tr class=\"parent\" id=\"row" + indexCounter
						+ "\" title=\"Click to expand/collapse\" style=\"cursor: pointer;\" onclick=\"changeIcon('span-" + id + "'); \">\n");
				response.append("<td><span id=\"span-" + id + "\" class=\"glyphicon glyphicon-minus\" style=\"color:blue\"></span></td>\n");
				response.append("<td class=\"break-word\">" + pair.getKey() + "</td>");
				response.append("<td>" + pair.getValue().size() + "</td>");
				response.append("<td></td>");
				response.append("</tr>");
				//
				response.append("<tr class=\"child-row" + indexCounter + "\" style=\"display: table-row;\">");
				response.append("<td></td>");
				response.append("<td><i>Suite Name</i></td>");
				response.append("<td><i>Test Name</i></td>");
				response.append("<td><i>Class Name</i></td>");
				response.append("</tr>");
				for (IssueDTO temp : pair.getValue()) {
					response.append("<tr class=\"child-row" + indexCounter + "\" style=\"display: table-row;\">");
					response.append("<td></td>");
					response.append("<td><a href=\"suites_overview.html#" + temp.getSuiteName() + "\">" + temp.getSuiteName() + "</a></td>");
					if (showHideReportFeatureFlag) {
						response.append("<td><a href=\"" + temp.getLink() + "\" onmouseover=\"showReport(this,'" + temp.getLink() + "')\" onmouseout = \"hideReport(this)\">" + temp.getTestName()
								+ "<iframe class=\"tipFrame\" src=\"\"></iframe></a></td>");
					} else {
						response.append("<td><a href=\"" + temp.getLink() + "\">" + temp.getTestName() + "</a></td>");
					}
					response.append("<td class=\"break-word\">" + temp.getTestClass() + "</td>");
					response.append("</tr>\n");
				}
				indexCounter++;
			}
		} else {
			response.append("<tr style=\"display: table-row;\">");
			response.append("<td>&nbsp;</td>");
			response.append("<td>&nbsp;</td>");
			response.append("<td>&nbsp;</td>");
			response.append("<td>&nbsp;</td>");
			response.append("</tr>\n");
		}
		return response.toString();
	}
	
	public String getSuites(List<ISuite> suites) {
		StringBuilder response = new StringBuilder("");
		if (suites != null) {
			int totalPass = 0;
			int totalFail = 0;
			int totalSkip = 0;
			int totalKnown = 0;
			int totalFixed = 0;
			Long totalStartDate = Long.MAX_VALUE;
			Long totalEndDate = 0L;
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
				
				Long startDate = Long.MAX_VALUE;
				Long endDate = 0L;
				for (IInvokedMethod tempIInvokedMethod : tempISuite.getAllInvokedMethods()) {
					if (tempIInvokedMethod.getDate() < startDate) {
						startDate = tempIInvokedMethod.getDate();
					}
					if (tempIInvokedMethod.getDate() > endDate) {
						endDate = tempIInvokedMethod.getDate();
					}
				}
				if (startDate != null && startDate < totalStartDate) {
					totalStartDate = startDate;
				}
				if (endDate != null && endDate > totalEndDate) {
					totalEndDate = endDate;
				}
				response.append("<tr class=\"test\">\n");
				response.append("<td>" + getDate(tempISuite) + "</td>");
				response.append("<td>" + parentSuiteName + "</td>");
				response.append("<td><a href=\"suites_overview.html#" + suiteName + "\">" + suiteName + "</a></td>");
				response.append("<td class=\"duration\">" + parallel + "</td>");
				response.append("<td class=\"duration\">" + formatDurationinMinutes(endDate - startDate) + "</td>");
				if (pass > 0) {
					response.append("<td class=\"passed number\">" + pass + "</td>");
				} else {
					response.append("<td class=\"zero number\">" + pass + "</td>");
				}
				if (skip > 0) {
					response.append("<td class=\"skipped number\">" + skip + "</td>");
				} else {
					response.append("<td class=\"zero number\">" + skip + "</td>");
				}
				if (knownDefectMode()) {
					if (known > 0) {
						response.append("<td class=\"knownDefects number\">" + known + "</td>");
					} else {
						response.append("<td class=\"zero number\">" + known + "</td>");
					}
					if (fixed > 0) {
						response.append("<td class=\"fixed number\">" + fixed + "</td>");
					} else {
						response.append("<td class=\"zero number\">" + fixed + "</td>");
					}
				}
				if (fail > 0) {
					response.append("<td class=\"failed number\">" + fail + "</td>");
				} else {
					response.append("<td class=\"zero number\">" + fail + "</td>");
				}
				response.append("<td class=\"zero number\">" + getStatusColor(getStatus(pass, fail, skip, known, fixed)) + "</td>");
				response.append("</tr>\n");
			}
			response.append("<tbody class=\"avoid-sort\">");
			response.append("<tr class=\"suite\">\n");
			response.append("<td colspan=\"4\">Total</td>");
			
			if (totalStartDate == Long.MAX_VALUE) {
				totalStartDate = 0L;
			}
			response.append("<td class=\"duration\">" + formatDurationinMinutes(totalEndDate - totalStartDate) + "</td>");
			if (totalPass > 0) {
				response.append("<td class=\"passed number\">" + totalPass + "</td>");
			} else {
				response.append("<td class=\"zero number\">" + totalPass + "</td>");
			}
			if (totalSkip > 0) {
				response.append("<td class=\"skipped number\">" + totalSkip + "</td>");
			} else {
				response.append("<td class=\"zero number\">" + totalSkip + "</td>");
			}
			if (knownDefectMode()) {
				if (totalKnown > 0) {
					response.append("<td class=\"knownDefects number\">" + totalKnown + "</td>");
				} else {
					response.append("<td class=\"zero number\">" + totalKnown + "</td>");
				}
				if (totalFixed > 0) {
					response.append("<td class=\"fixed number\">" + totalFixed + "</td>");
				} else {
					response.append("<td class=\"zero number\">" + totalFixed + "</td>");
				}
			}
			if (totalFail > 0) {
				response.append("<td class=\"failed number\">" + totalFail + "</td>");
			} else {
				response.append("<td class=\"zero number\">" + totalFail + "</td>");
			}
			response.append("<td class=\"zero number\">" + getStatusColor(getStatus(totalPass, totalFail, totalSkip, totalKnown, totalFixed)) + "</td>");
			response.append("</tr>\n");
			response.append("</tbody>\n");
		}
		return response.toString();
	}
	
	public String getPackages(List<ISuite> suites) {
		StringBuilder response = new StringBuilder("");
		int indexCounter = 0;
		for (Entry<PackageDetailsDTO, List<PackageDetailsDTO>> entry : HTMLReporter.getPackageDetails().entrySet()) {
			UUID id = UUID.randomUUID();
			response.append("<tr class=\"parent\" id=\"row" + indexCounter
					+ "\" title=\"Click to expand/collapse\" style=\"cursor: pointer;\" onclick=\"changeIcon('span-" + id + "'); \">\n");
			response.append("<td><span id=\"span-" + id + "\" class=\"glyphicon glyphicon-minus\" style=\"color:blue\"></span></td>\n");
			response.append("<td align=\"left\">" + entry.getKey().getPackageName() + "</td>");
			response.append("<td align=\"center\">" + entry.getKey().getDuration() + "</td>");
			if (entry.getKey().getPass() > 0) {
				response.append("<td align=\"center\" class=\"passed number\">" + entry.getKey().getPass() + "</td>");
			} else {
				response.append("<td align=\"center\" class=\"zero number\">" + entry.getKey().getPass() + "</td>");
			}
			if (entry.getKey().getSkip() > 0) {
				response.append("<td align=\"center\" class=\"skipped number\">" + entry.getKey().getSkip() + "</td>");
			} else {
				response.append("<td align=\"center\" class=\"zero number\">" + entry.getKey().getSkip() + "</td>");
			}
			if (entry.getKey().getFail() > 0) {
				response.append("<td align=\"center\" class=\"failed number\">" + entry.getKey().getFail() + "</td>");
			} else {
				response.append("<td align=\"center\" class=\"zero number\">" + entry.getKey().getFail() + "</td>");
			}
			if (knownDefectMode()) {
				if (entry.getKey().getKnown() > 0) {
					response.append("<td align=\"center\" class=\"knownDefects number\">" + entry.getKey().getKnown() + "</td>");
				} else {
					response.append("<td align=\"center\" class=\"zero number\">" + entry.getKey().getKnown() + "</td>");
				}
				if (entry.getKey().getFixed() > 0) {
					response.append("<td align=\"center\" class=\"fixed number\">" + entry.getKey().getFixed() + "</td>");
				} else {
					response.append("<td align=\"center\" class=\"zero number\">" + entry.getKey().getFixed() + "</td>");
				}
			}
			response.append("</tr>\n");
			
			for (PackageDetailsDTO packageDTO : entry.getValue()) {
				response.append("<tr class=\"child-row" + indexCounter + "\" style=\"display: table-row;\">");
				response.append("<td></td>");
				if (showHideReportFeatureFlag) {
					response.append("<td><a href=\"" + packageDTO.getUrl() + "\" onmouseover=\"showReport(this,'" + packageDTO.getUrl() + "')\" onmouseout = \"hideReport(this)\">" + packageDTO.getClassΝame()
							+ "<iframe class=\"tipFrame\" src=\"\"></iframe></a></td>");
				} else {
					response.append("<td><a href=\"" + packageDTO.getUrl() + "\">" + packageDTO.getClassΝame() + "</a></td>");
				}
				response.append("<td align=\"center\">" + packageDTO.getDuration() + "</td>");
				if (packageDTO.getPass() > 0) {
					response.append("<td align=\"center\" class=\"passedCell\">" + packageDTO.getPass() + "</td>");
				} else {
					response.append("<td align=\"center\" class=\"zero number\">" + packageDTO.getPass() + "</td>");
				}
				if (packageDTO.getSkip() > 0) {
					response.append("<td align=\"center\" class=\"skippedCell\">" + packageDTO.getSkip() + "</td>");
				} else {
					response.append("<td align=\"center\" class=\"zero number\">" + packageDTO.getSkip() + "</td>");
				}
				if (packageDTO.getFail() > 0) {
					response.append("<td align=\"center\" class=\"failedCell\">" + packageDTO.getFail() + "</td>");
				} else {
					response.append("<td align=\"center\" class=\"zero number\">" + packageDTO.getFail() + "</td>");
				}
				if (knownDefectMode()) {
					if (packageDTO.getKnown() > 0) {
						response.append("<td align=\"center\" class=\"knownDefectsCell\">" + packageDTO.getKnown() + "</td>");
					} else {
						response.append("<td align=\"center\" class=\"zero number\">" + packageDTO.getKnown() + "</td>");
					}
					if (packageDTO.getFixed() > 0) {
						response.append("<td align=\"center\" class=\"fixedCell\">" + packageDTO.getFixed() + "</td>");
					} else {
						response.append("<td align=\"center\" class=\"zero number\">" + packageDTO.getFixed() + "</td>");
					}
				}
			}
			indexCounter++;
		}
		response.append("\n");
		return response.toString();
	}
	
	public String getGroups(List<ISuite> suites) {
		StringBuilder response = new StringBuilder("");
		int indexCounter = 0;
		if (!HTMLReporter.getGroupDetails().isEmpty()) {
			for (Entry<PackageDetailsDTO, List<PackageDetailsDTO>> entry : HTMLReporter.getGroupDetails().entrySet()) {
				UUID id = UUID.randomUUID();
				response.append("<tr class=\"parent\" id=\"row" + indexCounter
						+ "\" title=\"Click to expand/collapse\" style=\"cursor: pointer;\" onclick=\"changeIcon('span-" + id + "'); \">\n");
				response.append("<td><span id=\"span-" + id + "\" class=\"glyphicon glyphicon-minus\" style=\"color:blue\"></span></td>\n");
				response.append("<td align=\"left\">" + entry.getKey().getPackageName() + "</td>");
				response.append("<td align=\"center\">" + entry.getKey().getDuration() + "</td>");
				if (entry.getKey().getPass() > 0) {
					response.append("<td align=\"center\" class=\"passed number\">" + entry.getKey().getPass() + "</td>");
				} else {
					response.append("<td align=\"center\" class=\"zero number\">" + entry.getKey().getPass() + "</td>");
				}
				if (entry.getKey().getSkip() > 0) {
					response.append("<td align=\"center\" class=\"skipped number\">" + entry.getKey().getSkip() + "</td>");
				} else {
					response.append("<td align=\"center\" class=\"zero number\">" + entry.getKey().getSkip() + "</td>");
				}
				if (entry.getKey().getFail() > 0) {
					response.append("<td align=\"center\" class=\"failed number\">" + entry.getKey().getFail() + "</td>");
				} else {
					response.append("<td align=\"center\" class=\"zero number\">" + entry.getKey().getFail() + "</td>");
				}
				if (knownDefectMode()) {
					if (entry.getKey().getKnown() > 0) {
						response.append("<td align=\"center\" class=\"knownDefects number\">" + entry.getKey().getKnown() + "</td>");
					} else {
						response.append("<td align=\"center\" class=\"zero number\">" + entry.getKey().getKnown() + "</td>");
					}
					if (entry.getKey().getFixed() > 0) {
						response.append("<td align=\"center\" class=\"fixed number\">" + entry.getKey().getFixed() + "</td>");
					} else {
						response.append("<td align=\"center\" class=\"zero number\">" + entry.getKey().getFixed() + "</td>");
					}
				}
				response.append("</tr>\n");
				
				for (PackageDetailsDTO packageDTO : entry.getValue()) {
					response.append("<tr class=\"child-row" + indexCounter + "\" style=\"display: table-row;\">");
					response.append("<td></td>");
					if (showHideReportFeatureFlag) {
						response.append("<td><a href=\"" + packageDTO.getUrl() + "\" onmouseover=\"showReport(this,'" + packageDTO.getUrl() + "')\" onmouseout = \"hideReport(this)\">" + packageDTO.getClassΝame()
								+ "<iframe class=\"tipFrame\" src=\"\"></iframe></a></td>");
					} else {
						response.append("<td><a href=\"" + packageDTO.getUrl() + "\">" + packageDTO.getClassΝame() + "</a></td>");
					}
					response.append("<td align=\"center\">" + packageDTO.getDuration() + "</td>");
					if (packageDTO.getPass() > 0) {
						response.append("<td align=\"center\" class=\"passedCell\">" + packageDTO.getPass() + "</td>");
					} else {
						response.append("<td align=\"center\" class=\"zero number\">" + packageDTO.getPass() + "</td>");
					}
					if (packageDTO.getSkip() > 0) {
						response.append("<td align=\"center\" class=\"skippedCell\">" + packageDTO.getSkip() + "</td>");
					} else {
						response.append("<td align=\"center\" class=\"zero number\">" + packageDTO.getSkip() + "</td>");
					}
					if (packageDTO.getFail() > 0) {
						response.append("<td align=\"center\" class=\"failedCell\">" + packageDTO.getFail() + "</td>");
					} else {
						response.append("<td align=\"center\" class=\"zero number\">" + packageDTO.getFail() + "</td>");
					}
					if (knownDefectMode()) {
						if (packageDTO.getKnown() > 0) {
							response.append("<td align=\"center\" class=\"knownDefectsCell\">" + packageDTO.getKnown() + "</td>");
						} else {
							response.append("<td align=\"center\" class=\"zero number\">" + packageDTO.getKnown() + "</td>");
						}
						if (packageDTO.getFixed() > 0) {
							response.append("<td align=\"center\" class=\"fixedCell\">" + packageDTO.getFixed() + "</td>");
						} else {
							response.append("<td align=\"center\" class=\"zero number\">" + packageDTO.getFixed() + "</td>");
						}
					}
				}
				indexCounter++;
			}
		} else {
			response.append("<tr>\n");
			response.append("<td>&nbsp;</td>\n");
			response.append("<td>&nbsp;</td>");
			response.append("<td>&nbsp;</td>");
			response.append("<td>&nbsp;</td>");
			response.append("<td>&nbsp;</td>\n");
			if (knownDefectMode()) {
				response.append("<td>&nbsp;</td>\n");
				response.append("<td>&nbsp;</td>\n");
			}
			response.append("<td>&nbsp;</td>\n");
			response.append("</tr>\n");
		}
		response.append("\n");
		return response.toString();
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
		} else if (pass == 0 && skip == 0) {
			status = ResultStatus.SKIP;
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
			return "<font color=\"blue\">" + ResultStatus.FIXED + "</font>";
		} else if (ResultStatus.PASS_WITH_KNOWN_ISSUES.equals(status)) {
			return "<font color=\"orange\">" + ResultStatus.KNOWN + "</font>";
		}
		return status.toString();
	}
	
	public long getTestDuration(ITestContext context) {
		if (!HTMLReporter.suiteName.equals(context.getSuite().getName())) {
			HTMLReporter.suiteName = context.getSuite().getName();
		}
		long duration = getTestDuration(context.getPassedTests().getAllResults());
		duration += getTestDuration(context.getSkippedTests().getAllResults());
		duration += getTestDuration(context.getFailedTests().getAllResults());
		duration += getTestDuration(context.getPassedConfigurations().getAllResults());
		duration += getTestDuration(context.getSkippedConfigurations().getAllResults());
		duration += getTestDuration(context.getFailedConfigurations().getAllResults());
		return duration;
	}
	
	public SuiteConfigurationDTO getSuiteConfiguration(ITestContext context) {
		Set<ITestResult> before = new HashSet<>();
		Set<ITestResult> after = new HashSet<>();
		Set<ITestResult> allResults = new HashSet<>();
		allResults.addAll(context.getPassedConfigurations().getAllResults());
		allResults.addAll(context.getSkippedConfigurations().getAllResults());
		allResults.addAll(context.getFailedConfigurations().getAllResults());
		for (ITestResult temp : allResults) {
			if (temp.getMethod().isBeforeSuiteConfiguration()) {
				before.add(temp);
			} else if (temp.getMethod().isAfterSuiteConfiguration()) {
				after.add(temp);
			}
		}
		return new SuiteConfigurationDTO(before, after);
	}
	
	public String getTime(ITestContext context) {
		String date = "";
		if (context.getStartDate() != null) {
			date = DateFormat.getTimeInstance().format(context.getStartDate());
		}
		return date;
	}
	
	public String getSuiteConfTime(Set<ITestResult> itestResults) {
		Long date = null;
		for (ITestResult temp : itestResults) {
			if (date == null || temp.getStartMillis() < date) {
				date = temp.getStartMillis();
			}
		}
		if (date != null) {
			return DateFormat.getTimeInstance().format(date);
		}
		return "";
	}
	
	public static String getTotalDuration(ISuite suite) {
		Map<String, ISuiteResult> map = suite.getResults();
		List<ITestContext> list = new ArrayList<>();
		for (String key : map.keySet()) {
			list.add(map.get(key).getTestContext());
		}
		Set<ITestResult> allResults = new HashSet<>();
		for (ITestContext temp : list) {
			allResults.addAll(temp.getFailedConfigurations().getAllResults());
			allResults.addAll(temp.getPassedConfigurations().getAllResults());
			allResults.addAll(temp.getSkippedConfigurations().getAllResults());
			allResults.addAll(temp.getSkippedTests().getAllResults());
			allResults.addAll(temp.getPassedTests().getAllResults());
			allResults.addAll(temp.getFailedTests().getAllResults());
		}
		return formatDurationinMinutes(getSuiteDuration(allResults));
	}
	
	public static String formatDurationinMinutes(long elapsed) {
		if (elapsed >= 0) {
			long seconds = (elapsed / 1000) % 60;
			long minutes = (elapsed / (1000 * 60)) % 60;
			long hours = (elapsed / (1000 * 60 * 60)) % 24;
			return String.format("%02d:%02d:%02d", hours, minutes, seconds);
		}
		return String.format("%02d:%02d:%02d", 0, 0, 0);
	}
	
	public static String formatDurationinMinutes(String time1, String time2) {
		String hoursString = "00";
		String minutesString = "00";
		String secondsString = "00";
		
		String[] splitter1 = time1.split(":");
		String[] splitter2 = time2.split(":");
		if (splitter1.length < 3 && splitter2.length < 3) {
			// DO nothing
		} else {
			try {
				int seconds = Integer.parseInt(splitter1[2]) + Integer.parseInt(splitter2[2]);
				int minutes = Integer.parseInt(splitter1[1]) + Integer.parseInt(splitter2[1]);
				int hours = Integer.parseInt(splitter1[0]) + Integer.parseInt(splitter2[0]);
				
				if (seconds >= 60) {
					minutes++;
					seconds = seconds - 60;
				}
				if (minutes >= 60) {
					hours++;
					minutes = minutes - 60;
				}
				
				if (hours <= 9) {
					hoursString = "0" + hours;
				} else {
					hoursString = Integer.toString(hours);
				}
				
				if (minutes <= 9) {
					minutesString = "0" + minutes;
				} else {
					minutesString = Integer.toString(minutes);
				}
				
				if (seconds <= 9) {
					secondsString = "0" + seconds;
				} else {
					secondsString = Integer.toString(seconds);
				}
			} catch (Exception ex) {
				
			}
		}
		return hoursString + ":" + minutesString + ":" + secondsString;
	}
	
	/**
	 * Returns the aggregate of the elapsed times for each test result.
	 * 
	 * @param results
	 *            A set of test results.
	 * @return The sum of the test durations.
	 */
	private long getTestDuration(Set<ITestResult> results) {
		long duration = 0;
		for (ITestResult result : results) {
			if (!(result.getMethod().isBeforeSuiteConfiguration() || result.getMethod().isAfterSuiteConfiguration())) {
				duration += (result.getEndMillis() - result.getStartMillis());
			}
		}
		if (duration < 0) {
			// Skip tests or configuration methods are have getEndMillis = 0 end getStartMillis > 0
			return 0;
		}
		return duration;
	}
	
	private static long getSuiteDuration(Set<ITestResult> results) {
		long startDate = Long.MAX_VALUE;
		long endDate = 0;
		for (ITestResult result : results) {
			if (result.getStartMillis() < startDate) {
				startDate = result.getStartMillis();
			}
			if (result.getEndMillis() > endDate) {
				endDate = result.getEndMillis();
			}
		}
		if (endDate > 0) {
			return endDate - startDate;
		}
		return 0;
	}
	
	public String getAnnotation(ITestResult result) {
		StringBuilder annotation = new StringBuilder();
		try {
			Annotation[] annotationArray = result.getMethod().getConstructorOrMethod().getMethod().getAnnotations();
			for (int i = 0; i < annotationArray.length; i++) {
				if (annotationArray[i].annotationType().getCanonicalName().startsWith("org.testng.annotations.")
						&& !annotationArray[i].annotationType().getSimpleName().equals("Parameters")) {
					annotation.append("@").append(annotationArray[i].annotationType().getSimpleName()).append(" ");
					HashMap<String, String> map = getTestAnnotationAttributes(annotationArray[i].toString());
					if (map.containsKey("dataProvider") && !map.get("dataProvider").isEmpty()) {
						annotation.append("@").append(" with DataProvider : [" + map.get("dataProvider") + "]").append(" ");
					}
				}
			}
		} catch (Exception ex) {
			
		}
		return annotation.toString();
	}
	
	private HashMap<String, String> getTestAnnotationAttributes(String annotation) {
		HashMap<String, String> map = new HashMap<>();
		try {
			annotation = annotation.substring(annotation.indexOf('(') + 1, annotation.indexOf(')'));
			String[] splitter = annotation.split(",");
			for (int i = 0; i < splitter.length; i++) {
				String[] splitMap = splitter[i].split("=");
				if (splitMap.length == 1) {
					map.put(splitMap[0].trim(), "");
				} else {
					map.put(splitMap[0].trim(), splitMap[1].trim());
				}
			}
		} catch (Exception ex) {
		}
		return map;
	}
	
	public String formatDuration(long startMillis, long endMillis) {
		long elapsed = endMillis - startMillis;
		return formatDuration(elapsed);
	}
	
	public String formatDuration(long elapsed) {
		double seconds = (double) elapsed / 1000;
		if (seconds < 0) {
			seconds = 0;
		}
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
		List<Throwable> causes = new LinkedList<>();
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
	 * Retieves the output from all calls to {@link org.testng.Reporter#log(String)} across all tests.
	 * 
	 * @return A (possibly empty) list of log messages.
	 */
	public List<String> getAllOutput() {
		if ("true".equalsIgnoreCase(System.getProperty(HTMLReporter.LOG_OUTPUT_REPORT))) {
			return Reporter.getOutput();
		}
		return new ArrayList<>(Arrays.asList("Param '" + HTMLReporter.LOG_OUTPUT_REPORT
				+ "' has neen set to 'false' , so Report Output is not generated."));
	}
	
	public boolean hasArguments(ITestResult result) {
		return result.getParameters().length > 0;
	}
	
	public String getClassName(ITestResult result) {
		String name = result.getTestClass().getName();
		String sub = "";
		try {
			sub = name.substring(0, name.indexOf('.') + 1);
			name = name.substring(name.lastIndexOf('.') + 1, name.length());
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
		try {
			if (result.getMethod().isTest() && result.getMethod().getDescription() != null) {
				return true;
			}
		} catch (NullPointerException ex) {
		}
		return false;
	}
	
	public String getClassNameFullPath(ISuiteResult result) {
		if (result != null) {
			List<XmlClass> list = result.getTestContext().getCurrentXmlTest().getClasses();
			StringBuilder classNames = new StringBuilder();
			String separateLines = "";
			if (list.size() > 1) {
				separateLines = "<br>";
			}
			for (XmlClass temp : list) {
				classNames.append(temp.getName()).append(separateLines);
			}
			return classNames.toString();
		}
		return "";
	}
	
	public String getClassName(ISuiteResult result) {
		List<XmlClass> list = result.getTestContext().getCurrentXmlTest().getClasses();
		StringBuilder classNames = new StringBuilder();
		String separateLines = "";
		if (list.size() > 1) {
			separateLines = "<br>";
		}
		for (XmlClass temp : list) {
			classNames.append(getClassFromFullClassName(temp.getName())).append(separateLines);
		}
		return classNames.toString();
	}
	
	public String getTotalTime(ISuiteResult result) {
		if (result != null) {
			Date start = result.getTestContext().getStartDate();
			Date end = result.getTestContext().getEndDate();
			long diff = end.toInstant().toEpochMilli() - start.toInstant().toEpochMilli();
			return formatDurationinMinutes(diff);
		}
		return "";
	}
	
	public int getTotalSteps(ISuiteResult result) {
		if (result != null) {
			return result.getTestContext().getAllTestMethods().length;
		}
		return 0;
	}
	
	public String getSuiteName(ISuiteResult result) {
		return result.getTestContext().getSuite().getName();
	}
	
	public String getSuiteXMLName(ISuiteResult result) {
		if (result != null) {
			return result.getTestContext().getSuite().getXmlSuite().getFileName();
		}
		return "";
	}
	
	public String getSuiteXMLFileName(ISuiteResult result) {
		String fullPathName = getSuiteXMLName(result);
		if (!Strings.isNullOrEmpty(fullPathName)) {
			return new File(fullPathName).getName();
		}
		return fullPathName;
	}
	
	public String getTestStatus(ISuiteResult result) {
		if (result != null) {
			if (result.getTestContext().getFailedTests().size() > 0) {
				return getStatusColor(ResultStatus.FAIL);
			}
			if (result.getTestContext().getSkippedTests().size() > 0) {
				return getStatusColor(ResultStatus.SKIP);
			}
			return getStatusColor(ResultStatus.PASS);
		}
		return "";
	}
	
	public String getSteps(ISuiteResult result) {
		StringBuilder steps = new StringBuilder("<table><tr>");
		Set<ITestResult> all = new HashSet<>();
		if (result.getTestContext().getFailedTests() != null && !result.getTestContext().getFailedTests().getAllResults().isEmpty()) {
			all.addAll(result.getTestContext().getFailedTests().getAllResults());
		}
		if (result.getTestContext().getPassedTests() != null && !result.getTestContext().getPassedTests().getAllResults().isEmpty()) {
			all.addAll(result.getTestContext().getPassedTests().getAllResults());
		}
		if (result.getTestContext().getSkippedTests() != null && !result.getTestContext().getSkippedTests().getAllResults().isEmpty()) {
			all.addAll(result.getTestContext().getSkippedTests().getAllResults());
		}
		
		for (ITestResult temp : all) {
			steps.append("<td>");
			if (temp.getStatus() == ITestResult.SUCCESS) {
				boolean isFixed = false;
				boolean isKnown = false;
				Annotation[] annotation = getDeclaredAnnotations(temp);
				for (Annotation tempAnnotation : annotation) {
					if (tempAnnotation.toString().contains(KNOWNDEFECT) && FIXED.equals(temp.getAttribute(TEST))) {
						steps.append("F");
						isFixed = true;
						break;
					} else if (tempAnnotation.toString().contains(KNOWNDEFECT) && KNOWN.equals(temp.getAttribute(TEST))) {
						steps.append("K");
						isKnown = true;
					}
				}
				if (!isFixed && !isKnown) {
					steps.append("P");
				}
			}
			if (temp.getStatus() == ITestResult.FAILURE) {
				steps.append("F");
			}
			if (temp.getStatus() == ITestResult.SKIP) {
				steps.append("S");
			}
			steps.append("</td>");
		}
		steps.append("</table>");
		return steps.toString();
	}
	
	public boolean hasPriority(ITestResult result) {
		try {
			if (result.getMethod().isTest() && result.getMethod().getPriority() != 0) {
				return true;
			}
		} catch (NullPointerException ex) {
		}
		return false;
	}
	
	public boolean hasGroups(ITestResult result) {
		try {
			if (result.getMethod().isTest() && result.getMethod().getGroups().length != 0) {
				return true;
			}
		} catch (NullPointerException ex) {
		}
		return false;
	}
	
	public String getGroups(ITestResult result) {
		String[] groups = null;
		StringBuilder foundGroups = new StringBuilder();
		try {
			if (result.getMethod().isTest()) {
				groups = result.getMethod().getGroups();
				if (groups.length != 0) {
					for (int i = 0; i < groups.length; i++) {
						foundGroups.append(groups[i]);
						if (i != groups.length - 1) {
							foundGroups.append(",");
						}
					}
					return foundGroups.toString();
				}
			}
		} catch (NullPointerException ex) {
		}
		return "";
	}
	
	public boolean hasInvocationCount(ITestResult result) {
		if (result.getMethod().getInvocationCount() > 1) {
			return true;
		}
		return false;
	}
	
	public String getInvocationCount(ITestResult result) {
		return Integer.toString(result.getMethod().getCurrentInvocationCount());
	}
	
	public boolean hasTimeOut(ITestResult result) {
		Annotation[] annottaions = getDeclaredAnnotations(result);
		for (Annotation tempAnnotation : annottaions) {
			try {
				if (tempAnnotation.toString().contains("timeOut=")) {
					String timeOut = tempAnnotation.toString().substring(tempAnnotation.toString().indexOf("timeOut="), tempAnnotation.toString().length());
					timeOut = timeOut.substring(timeOut.indexOf('=') + 1, timeOut.indexOf(','));
					if (Long.parseLong(timeOut) > 0) {
						return true;
					}
				}
			} catch (Exception ex) {
				
			}
		}
		return false;
	}
	
	public String getTimeOut(ITestResult result) {
		Annotation[] annottaions = getDeclaredAnnotations(result);
		for (Annotation tempAnnotation : annottaions) {
			try {
				if (tempAnnotation.toString().contains("timeOut=")) {
					String timeOut = tempAnnotation.toString().substring(tempAnnotation.toString().indexOf("timeOut="), tempAnnotation.toString().length());
					timeOut = timeOut.substring(timeOut.indexOf('=') + 1, timeOut.indexOf(','));
					if (Long.parseLong(timeOut) > 0) {
						return timeOut;
					}
				}
			} catch (Exception ex) {
				
			}
		}
		return "0";
	}
	
	/**
	 * Is there a Known Defect Description
	 * 
	 * @param result
	 * @return
	 */
	public boolean hasKnownDefectsDescription(ITestResult result) {
		if (result.getAttribute(TEST) != null && (KNOWN.equalsIgnoreCase(result.getAttribute(TEST).toString()) || FIXED.equalsIgnoreCase(result.getAttribute(TEST).toString()))) {
			return true;
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
		if (result.getAttribute(TEST) != null && KNOWN.equalsIgnoreCase(result.getAttribute(TEST).toString())) {
			return true;
		}
		return false;
	}
	
	public boolean hasFixed(ITestResult result) {
		if (result.getAttribute(TEST) != null && FIXED.equalsIgnoreCase(result.getAttribute(TEST).toString())) {
			return true;
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
	
	private static String getDescriptionSkipped(String text) {
		try {
			if (!Strings.isNullOrEmpty(text)) {
				if (text.contains("depends on not successfully finished methods")) {
					return ("Depends on not successfully finished methods");
				} else if (text.startsWith("org.testng.SkipException: ")) {
					return text.replace("org.testng.SkipException: ", "").trim();
				}
			}
		} catch (Exception ex) {
			
		}
		return text;
	}
	
	public List<ITestResult> getPassedConfigurations(IClass classTest, Map<IClass, List<ITestResult>> passedConfigurations) {
		List<ITestResult> newmethods = new ArrayList<>();
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
	 * Decorate the string representation of an argument to give some hint as to its type (e.g. render Strings in double quotes).
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
	 * @return A comma-separated string listing all dependent groups. Returns an empty string it there are no dependent groups.
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
	 * @return A comma-separated string listing all dependent methods. Returns an empty string it there are no dependent methods.
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
	 * Takes a list of Strings and combines them into a single comma-separated String.
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
	 * Replace any angle brackets, quotes, apostrophes or ampersands with the corresponding XML/HTML entities to avoid problems displaying the String in an XML document. Assumes that the String does
	 * not already contain any entities (otherwise the ampersands will be escaped again).
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
	 * Converts a char into a String that can be inserted into an XML document, replacing special characters with XML entities as required.
	 * 
	 * @param character
	 *            The character to convert.
	 * @return An XML entity representing the character (or a String containing just the character if it does not need to be escaped).
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
	 * Works like {@link #escapeString(String)} but also replaces line breaks with &lt;br /&gt; tags and preserves significant whitespace.
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
	 * TestNG returns a compound thread ID that includes the thread name and its numeric ID, separated by an 'at' sign. We only want to use the thread name as the ID is mostly unimportant and it takes
	 * up too much space in the generated report.
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
		if (suite != null) {
			return suite.getName().replaceAll(" ", "_").replaceAll(",", "_").replaceAll("\"", "_");
		}
		return "N/A";
	}
	
	public String getSuiteXMLName(ISuite suite) {
		if (suite != null) {
			return suite.getXmlSuite().getFileName();
		}
		return "";
	}
	
	public String getSuiteXMLFileName(ISuite suite) {
		String fullPathName = getSuiteXMLName(suite);
		if (!Strings.isNullOrEmpty(fullPathName)) {
			return new File(fullPathName).getName();
		}
		return fullPathName;
	}
	
	/**
	 * Returns the timestamp for the time at which the suite finished executing. This is determined by finding the latest end time for each of the individual tests in the suite.
	 * 
	 * @param suite
	 *            The suite to find the end time of.
	 * @return The end time (as a number of milliseconds since 00:00 1st January 1970 UTC).
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
				if (tempAnnotation.toString().contains(NEW_FEATURE)) {
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
				if (tempAnnotation.toString().contains(NEW_FEATURE)) {
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
				if (tempAnnotation.toString().contains(FEATURE)) {
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
				if (tempAnnotation.toString().contains(NEW_FEATURE)) {
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
				if (tempAnnotation.toString().contains(FEATURE)) {
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
				if (tempAnnotation.toString().contains(FEATURE) || tempAnnotation.toString().contains(NEW_FEATURE)) {
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
				if (tempAnnotation.toString().contains(FEATURE) || tempAnnotation.toString().contains(NEW_FEATURE)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public String getFeatures(Map<String, List<IssueDTO>> features) {
		StringBuilder response = new StringBuilder("");
		int indexCounter = 1;
		if (features != null && !features.isEmpty()) {
			Iterator<Entry<String, List<IssueDTO>>> it = features.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, List<IssueDTO>> pair = it.next();
				// Calculate overall status
				ResultStatus overAllStatus = ResultStatus.PASS;
				for (IssueDTO temp : pair.getValue()) {
					if (ResultStatus.FAIL.equals(temp.getStatus())) {
						overAllStatus = ResultStatus.FAIL;
					} else if (ResultStatus.PASS_WITH_KNOWN_ISSUES.equals(temp.getStatus())) {
						overAllStatus = ResultStatus.PASS_WITH_KNOWN_ISSUES;
					} else if (ResultStatus.PASS_WITH_FIXED_ISSUES.equals(temp.getStatus())) {
						overAllStatus = ResultStatus.PASS_WITH_FIXED_ISSUES;
					} else if (ResultStatus.SKIP.equals(temp.getStatus())) {
						overAllStatus = ResultStatus.SKIP;
					}
				}
				// Calculate Total Tests
				int totalTests = 0;
				for (IssueDTO temp : pair.getValue()) {
					totalTests += temp.getTotalNumberOfTests();
				}
				UUID id = UUID.randomUUID();
				response.append("<tr class=\"parent\" id=\"row" + indexCounter
						+ "\" title=\"Click to expand/collapse\" style=\"cursor: pointer;\" onclick=\"changeIcon('span-" + id + "'); \">\n");
				response.append("<td><span id=\"span-" + id + "\" class=\"glyphicon glyphicon-minus\" style=\"color:blue\"></span></td>\n");
				response.append("<td colspan=\"2\" id=\"" + pair.getKey() + "\" class=\"break-word\">" + pair.getKey() + "</td>");
				response.append("<td colspan=\"1\">" + totalTests + "</td>");
				response.append("<td colspan=\"2\">" + getStatusColor(overAllStatus) + "</td>\n");
				response.append("</tr>");
				//
				response.append("<tr class=\"child-row" + indexCounter + "\" style=\"display: table-row;\">");
				response.append("<td></td>");
				response.append("<td><i>Suite Name</i></td>");
				response.append("<td><i>Test Name</i></td>");
				response.append("<td><i>Class Name</i></td>");
				response.append("<td><i>Tests</i></td>");
				response.append("</tr>");
				//
				for (IssueDTO temp : pair.getValue()) {
					response.append("<tr class=\"child-row" + indexCounter + "\" style=\"display: table-row;\">");
					response.append("<td></td>");
					response.append("<td><a href=\"suites_overview.html#" + temp.getSuiteName() + "\">" + temp.getSuiteName() + "</a></td>\n");
					if (showHideReportFeatureFlag) {
						response.append("<td><a href=\"" + temp.getLink() + "\" onmouseover=\"showReport(this,'" + temp.getLink() + "')\" onmouseout = \"hideReport(this)\">" + temp.getTestName()
								+ "<iframe class=\"tipFrame\" src=\"\"></iframe></a></td>");
					} else {
						response.append("<td><a href=\"" + temp.getLink() + "\">" + temp.getTestName() + "</a></td>");
					}
					response.append("<td class=\"break-word\">" + temp.getTestClass() + "</td>\n");
					response.append("<td><div>" + temp.getResults() + "</div></td>\n");
					response.append("</tr>\n");
				}
				indexCounter++;
			}
		} else {
			response.append("<tr style=\"display: table-row;\">");
			response.append("<td>&nbsp;</td>");
			response.append("<td>&nbsp;</td>\n");
			response.append("<td>&nbsp;</td>\n");
			response.append("<td>&nbsp;</td>\n");
			response.append("<td>&nbsp;</td>\n");
			response.append("</tr>\n");
		}
		return response.toString();
	}
	
	public String getReportOutput() {
		return HTMLReporter.OUTPUTDIRECTORY_ABSOLUTE;
	}
	
	// Graphs
	public String graphTime(List<ISuite> suites) {
		StringBuilder text = generateGraph("title: \"Time \", valueFormatString: \"DD MMM hh:mm TT\"", "title: \"Number of Tests\"",
				"\"area\",showInLegend: true,name: \"Pass\",markerType: \"square\",xValueFormatString: \"DD MMM hh:mm TT\",color: \"green\",dataPoints: [");
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				Date date = iSuiteResult.getTestContext().getStartDate();
				text.append("{ x: new Date(" +
						(1900 + date.getYear()) + ", " +
						date.getMonth() + ", " +
						date.getDate() + ", " +
						date.getHours() + ", " +
						date.getMinutes() + ", " +
						date.getSeconds() + "), y: " + getPassed(iSuiteResult.getTestContext()).size() + " }," + "\n");
			}
		}
		text.append("]}," + "\n");
		
		text.append("{type: \"area\",showInLegend: true,name: \"Fixed\",markerType: \"square\",xValueFormatString: \"DD MMM hh:mm TT\",color: \"blue\",dataPoints: ["
				+ "\n");
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				Date date = iSuiteResult.getTestContext().getStartDate();
				text.append("{ x: new Date(" +
						(1900 + date.getYear()) + ", " +
						date.getMonth() + ", " +
						date.getDate() + ", " +
						date.getHours() + ", " +
						date.getMinutes() + ", " +
						date.getSeconds() + "), y: " + getFixed(iSuiteResult.getTestContext()).size() + " }," + "\n");
			}
		}
		text.append("]}," + "\n");
		
		text.append("{type: \"area\",showInLegend: true,name: \"Known Defects\",markerType: \"square\",xValueFormatString: \"DD MMM hh:mm TT\",color: \"orange\",dataPoints: ["
				+ "\n");
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				Date date = iSuiteResult.getTestContext().getStartDate();
				text.append("{ x: new Date(" +
						(1900 + date.getYear()) + ", " +
						date.getMonth() + ", " +
						date.getDate() + ", " +
						date.getHours() + ", " +
						date.getMinutes() + ", " +
						date.getSeconds() + "), y: " + getKnownDefect(iSuiteResult.getTestContext()).size() + " }," + "\n");
			}
		}
		text.append("]}," + "\n");
		
		text.append("{type: \"area\",showInLegend: true,name: \"Fail\",markerType: \"square\",xValueFormatString: \"DD MMM hh:mm TT\",color: \"red\",dataPoints: [" + "\n");
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				Date date = iSuiteResult.getTestContext().getStartDate();
				text.append("{ x: new Date(" +
						(1900 + date.getYear()) + ", " +
						date.getMonth() + ", " +
						date.getDate() + ", " +
						date.getHours() + ", " +
						date.getMinutes() + ", " +
						date.getSeconds() + "), y: " + getFailed(iSuiteResult.getTestContext()).size() + " }," + "\n");
			}
		}
		text.append("]}," + "\n");
		
		text.append("{type: \"area\",showInLegend: true,name: \"Skip\",markerType: \"square\",xValueFormatString: \"DD MMM hh:mm TT\",color: \"yellow\",dataPoints: [" + "\n");
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				Date date = iSuiteResult.getTestContext().getStartDate();
				text.append("{ x: new Date(" +
						(1900 + date.getYear()) + ", " +
						date.getMonth() + ", " +
						date.getDate() + ", " +
						date.getHours() + ", " +
						date.getMinutes() + ", " +
						date.getSeconds() + "), y: " + getSkip(iSuiteResult.getTestContext()).size() + " }," + "\n");
			}
		}
		text.append("]}" + "\n");
		text.append("]});" + "\n");
		return text.toString();
	}
	
	public String graphClass(List<ISuite> suites) {
		StringBuilder text = generateGraph("", "title: \"Class\"", "\"stackedBar\",showInLegend: true,name: \"Pass\",color: \"green\",dataPoints: [");
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				text.append("{ label: \"" + iSuiteResult.getTestContext().getName() + "\"," + " y: " + getPassed(iSuiteResult.getTestContext()).size() + " }," + "\n");
			}
		}
		text.append("]}," + "\n");
		
		text.append("{type: \"stackedBar\",showInLegend: true,name: \"Fixed\",color: \"blue\",dataPoints: [" + "\n");
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				text.append("{ label: \"" + iSuiteResult.getTestContext().getName() + "\"," + " y: " + getFixed(iSuiteResult.getTestContext()).size() + " }," + "\n");
			}
		}
		text.append("]}," + "\n");
		
		text.append("{type: \"stackedBar\",showInLegend: true,name: \"Known Defects\",color: \"orange\",dataPoints: [" + "\n");
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				text.append("{ label: \"" + iSuiteResult.getTestContext().getName() + "\"," + " y: " + getKnownDefect(iSuiteResult.getTestContext()).size() + " }," + "\n");
			}
		}
		text.append("]}," + "\n");
		
		text.append("{type: \"stackedBar\",showInLegend: true,name: \"Fail\",color: \"red\",dataPoints: [" + "\n");
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				text.append("{ label: \"" + iSuiteResult.getTestContext().getName() + "\"," + " y: " + getFailed(iSuiteResult.getTestContext()).size() + " }," + "\n");
			}
		}
		text.append("]}," + "\n");
		
		text.append("{type: \"stackedBar\",showInLegend: true,name: \"Skip\",color: \"yellow\",dataPoints: [" + "\n");
		for (ISuite tempISuite : suites) {
			Iterator<Entry<String, ISuiteResult>> it = tempISuite.getResults().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ISuiteResult> pair = (it.next());
				ISuiteResult iSuiteResult = pair.getValue();
				text.append("{ label: \"" + iSuiteResult.getTestContext().getName() + "\"," + " y: " + getSkip(iSuiteResult.getTestContext()).size() + " }," + "\n");
			}
		}
		text.append("]}" + "\n");
		text.append("]});" + "\n");
		return text.toString();
	}
	
	public String graphSuite(List<ISuite> suites) {
		StringBuilder text = generateGraph("", "title: \"Suite\"", "\"stackedBar\",showInLegend: true,name: \"Pass\",color: \"green\",dataPoints: [");
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
			text.append("{ label: \"" + suiteName + "\"," + " y: " + counter + " }," + "\n");
		}
		text.append("]}," + "\n");
		
		text.append("{type: \"stackedBar\",showInLegend: true,name: \"Fixed\",color: \"blue\",dataPoints: [" + "\n");
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
			text.append("{ label: \"" + suiteName + "\"," + " y: " + counter + " }," + "\n");
		}
		text.append("]}," + "\n");
		
		text.append("{type: \"stackedBar\",showInLegend: true,name: \"Known Defects\",color: \"orange\",dataPoints: [" + "\n");
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
			text.append("{ label: \"" + suiteName + "\"," + " y: " + counter + " }," + "\n");
		}
		text.append("]}," + "\n");
		
		text.append("{type: \"stackedBar\",showInLegend: true,name: \"Fail\",color: \"red\",dataPoints: [" + "\n");
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
			text.append("{ label: \"" + suiteName + "\"," + " y: " + counter + " }," + "\n");
		}
		text.append("]}," + "\n");
		
		text.append("{type: \"stackedBar\",showInLegend: true,name: \"Skip\",color: \"yellow\",dataPoints: [" + "\n");
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
			text.append("{ label: \"" + suiteName + "\"," + " y: " + counter + " }," + "\n");
		}
		text.append("]}" + "\n");
		text.append("]});" + "\n");
		return text.toString();
	}
	
	private StringBuilder generateGraph(String axisX, String axisY, String type) {
		StringBuilder text = new StringBuilder("");
		text.append("var chart = new CanvasJS.Chart(\"chartContainer\", {" + "\n");
		text.append("animationEnabled: true,");
		text.append("height: 600,");
		text.append("indexLabelFontSize: 16,");
		text.append("theme: \"light2\",");
		text.append("title:{text: \"\"},");
		text.append("axisX:{" + axisX + "},");
		text.append("axisY: {" + axisY + "},");
		text.append("toolTip:{shared:true},");
		text.append("legend:{cursor:\"pointer\",verticalAlign: \"top\",horizontalAlign: \"left\",dockInsidePlotArea: false, fontSize: 16},");
		text.append("data: [" + "\n");
		text.append("{type: " + type + "\n");
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
				if (tempAnnotation.toString().contains(KNOWNDEFECT) && tr.getStatus() == ITestResult.SUCCESS && knownDefectMode()) {
					addResult = false;
					break;
				}
			}
			if (addResult) {
				temp.addResult(tr);
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
				if (tempAnnotation.toString().contains(KNOWNDEFECT) && tr.getStatus() == ITestResult.FAILURE && knownDefectMode()) {
					addResult = false;
					break;
				}
			}
			if (addResult) {
				temp.addResult(tr);
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
		if (knownDefectMode()) {
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
					temp.addResult(tr);
				}
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
		if (knownDefectMode()) {
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
					temp.addResult(tr);
				}
			}
		}
		return temp;
	}
	
	public static List<IssueDTO> getKnownIssues(String suiteName, String linkName, Set<ITestResult> results) {
		List<IssueDTO> issues = new ArrayList<>();
		if (knownDefectMode()) {
			for (ITestResult tempITestResult : results) {
				for (ITestResult iTestResult : tempITestResult.getTestContext().getPassedTests().getAllResults()) {
					Annotation[] annotation = getDeclaredAnnotations(iTestResult);
					for (Annotation tempAnnotation : annotation) {
						if (tempAnnotation.toString().contains(KNOWNDEFECT) && KNOWN.equals(iTestResult.getAttribute(TEST))) {
							issues.add(new IssueDTO(suiteName, iTestResult.getTestContext().getName(), iTestResult.getInstanceName(), getDescription(tempAnnotation
									.toString()), linkName, isRegression(iTestResult.getTestContext())));
							break;
						}
					}
				}
				break;
			}
		}
		return issues;
	}
	
	public static List<IssueDTO> getFixedIssues(String suiteName, String linkName, Set<ITestResult> results) {
		List<IssueDTO> issues = new ArrayList<>();
		if (knownDefectMode()) {
			for (ITestResult tempITestResult : results) {
				for (ITestResult iTestResult : tempITestResult.getTestContext().getPassedTests().getAllResults()) {
					Annotation[] annotation = getDeclaredAnnotations(iTestResult);
					for (Annotation tempAnnotation : annotation) {
						if (tempAnnotation.toString().contains(KNOWNDEFECT) && FIXED.equals(iTestResult.getAttribute(TEST))) {
							issues.add(new IssueDTO(suiteName, iTestResult.getTestContext().getName(), iTestResult.getInstanceName(), getDescription(tempAnnotation
									.toString()), linkName, isRegression(iTestResult.getTestContext())));
							break;
						}
					}
				}
				break;
			}
		}
		return issues;
	}
	
	public static List<IssueDTO> getSkippedIssues(String suiteName, String linkName, Set<ITestResult> results) {
		List<IssueDTO> issues = new ArrayList<>();
		for (ITestResult tempITestResult : results) {
			for (ITestResult iTestResult : tempITestResult.getTestContext().getSkippedTests().getAllResults()) {
				String skippedException = null;
				try {
					skippedException = iTestResult.getThrowable().toString();
				} catch (Exception ex) {
					
				}
				issues.add(new IssueDTO(suiteName, iTestResult.getTestContext().getName(), iTestResult.getInstanceName(), getDescriptionSkipped(skippedException), linkName, isRegression(iTestResult.getTestContext())));
			}
			break;
		}
		return issues;
	}
	
	public static List<IssueDTO> getNewIssues(String suiteName, String linkName, Set<ITestResult> results) {
		List<IssueDTO> issues = new ArrayList<>();
		for (ITestResult tr : results) {
			issues.add(
					new IssueDTO(suiteName, tr.getTestContext().getName(), tr.getInstanceName(), tr.getThrowable().getMessage(), linkName, isRegression(tr.getTestContext())));
		}
		return issues;
	}
	
	public static boolean hasNewFeatures(List<ISuite> suites) {
		ResultsDTO resultsDTO = HTMLReporter.getResults();
		double newFeaturesTotal = resultsDTO.getNewFeatures() +
				resultsDTO.getNewFeaturesFail() +
				resultsDTO.getNewFeaturesFixed() +
				resultsDTO.getNewFeaturesKnownDefect() +
				resultsDTO.getNewFeaturesPass() +
				resultsDTO.getNewFeaturesSkip();
		if (newFeaturesTotal > 0) {
			return true;
		}
		return false;
	}
	
	public static boolean hasSkipped(List<ISuite> suites) {
		ResultsDTO resultsDTO = HTMLReporter.getResults();
		double skippedTotal = resultsDTO.getNewFeaturesSkip() + resultsDTO.getRegressionSkip();
		if (skippedTotal > 0) {
			return true;
		}
		return false;
	}
	
	public static List<IssueDTO> getFeatures(String suiteName, String linkName, ITestContext iTestContext) {
		List<IssueDTO> issues = new ArrayList<>();
		if (isFeature(iTestContext)) {
			Set<ITestResult> iResultMapPass = getPassed(iTestContext).getAllResults();
			Set<ITestResult> iResultMapFail = getFailed(iTestContext).getAllResults();
			Set<ITestResult> iResultMapSkip = getSkip(iTestContext).getAllResults();
			Set<ITestResult> iResultMapKnown = getKnownDefect(iTestContext).getAllResults();
			Set<ITestResult> iResultMapFixed = getFixed(iTestContext).getAllResults();
			
			Set<ITestResult> all = new HashSet<>();
			all.addAll(iResultMapPass);
			all.addAll(iResultMapFail);
			all.addAll(iResultMapSkip);
			all.addAll(iResultMapKnown);
			all.addAll(iResultMapFixed);
			int totalNumberOfTests = getTotalNumberOfTests(iResultMapPass, iResultMapFail, iResultMapSkip, iResultMapKnown, iResultMapFixed);
			String results = getFeatureResults(iResultMapPass, iResultMapFail, iResultMapSkip, iResultMapKnown, iResultMapFixed, linkName);
			ResultStatus status = getStatus(iResultMapPass.size(), iResultMapFail.size(), iResultMapSkip.size(), iResultMapKnown.size(), iResultMapFixed.size());
			for (ITestResult iTestResult : all) {
				issues.add(new IssueDTO(suiteName, iTestResult.getTestContext().getName(), iTestResult.getInstanceName(), getFeatureDescription(iTestContext),
						linkName, results, status, totalNumberOfTests));
				break;
			}
			
		}
		return issues;
	}
	
	public static List<IssueDTO> getNewFeatures(String suiteName, String linkName, ITestContext iTestContext) {
		List<IssueDTO> issues = new ArrayList<>();
		if (isNewFeature(iTestContext)) {
			Set<ITestResult> iResultMapPass = getPassed(iTestContext).getAllResults();
			Set<ITestResult> iResultMapFail = getFailed(iTestContext).getAllResults();
			Set<ITestResult> iResultMapSkip = getSkip(iTestContext).getAllResults();
			Set<ITestResult> iResultMapKnown = getKnownDefect(iTestContext).getAllResults();
			Set<ITestResult> iResultMapFixed = getFixed(iTestContext).getAllResults();
			
			Set<ITestResult> all = new HashSet<>();
			all.addAll(iResultMapPass);
			all.addAll(iResultMapFail);
			all.addAll(iResultMapSkip);
			all.addAll(iResultMapKnown);
			all.addAll(iResultMapFixed);
			
			String results = getFeatureResults(iResultMapPass, iResultMapFail, iResultMapSkip, iResultMapKnown, iResultMapFixed, linkName);
			ResultStatus status = getStatus(iResultMapPass.size(), iResultMapFail.size(), iResultMapSkip.size(), iResultMapKnown.size(), iResultMapFixed.size());
			for (ITestResult iTestResult : all) {
				boolean alreadyExists = false;
				for (IssueDTO tempIssueDTO : issues) {
					if (tempIssueDTO.getIssueDescription().equalsIgnoreCase(getNewFeatureDescription(iTestContext))) {
						alreadyExists = true;
						break;
					}
				}
				int totalNumberOfTests = getTotalNumberOfTests(iResultMapPass, iResultMapFail, iResultMapSkip, iResultMapKnown, iResultMapFixed);
				if (!alreadyExists) {
					issues.add(new IssueDTO(suiteName, iTestResult.getTestContext().getName(), iTestResult.getInstanceName(),
							getNewFeatureDescription(iTestContext), linkName, results, status, totalNumberOfTests));
					break;
				}
			}
		}
		return issues;
	}
	
	private static String getFeatureResults(Set<ITestResult> iResultMapPass, Set<ITestResult> iResultMapFail, Set<ITestResult> iResultMapSkip,
			Set<ITestResult> iResultMapKnown, Set<ITestResult> iResultMapFixed, String link) {
		StringBuilder results = new StringBuilder("<table>" + "\n");
		results.append("<tr>");
		if (!iResultMapPass.isEmpty()) {
			results.append("<td class=\"passedCell\" width=\"30px\"><a href=\"" + link + "\">" + iResultMapPass.size() + "</a></td>");
		}
		if (!iResultMapFail.isEmpty()) {
			results.append("<td class=\"failedCell\" width=\"30px\"><a href=\"" + link + "\">" + iResultMapFail.size() + "</a></td>");
		}
		if (!iResultMapSkip.isEmpty()) {
			results.append("<td class=\"skippedCell\" width=\"30px\"><a href=\"" + link + "\">" + iResultMapSkip.size() + "</a></td>");
		}
		if (!iResultMapKnown.isEmpty()) {
			results.append("<td class=\"knownDefectsCell\" width=\"30px\"><a href=\"" + link + "\">" + iResultMapKnown.size() + "</a></td>");
		}
		if (!iResultMapFixed.isEmpty()) {
			results.append("<td class=\"fixedCell\" width=\"30px\"><a href=\"" + link + "\">" + iResultMapFixed.size() + "</a></td>");
		}
		results.append("</tr>");
		results.append("</table>");
		return results.toString();
	}
	
	private static int getTotalNumberOfTests(Set<ITestResult> iResultMapPass, Set<ITestResult> iResultMapFail, Set<ITestResult> iResultMapSkip,
			Set<ITestResult> iResultMapKnown, Set<ITestResult> iResultMapFixed) {
		int totalNumberOfTests = 0;
		if (!iResultMapPass.isEmpty()) {
			totalNumberOfTests += iResultMapPass.size();
		}
		if (!iResultMapFail.isEmpty()) {
			totalNumberOfTests += iResultMapFail.size();
		}
		if (!iResultMapSkip.isEmpty()) {
			totalNumberOfTests += iResultMapSkip.size();
		}
		if (!iResultMapKnown.isEmpty()) {
			totalNumberOfTests += iResultMapKnown.size();
		}
		if (!iResultMapFixed.isEmpty()) {
			totalNumberOfTests += iResultMapFixed.size();
		}
		return totalNumberOfTests;
	}
	
	private static Annotation[] getDeclaredAnnotations(ITestResult result) {
		for (ITestNGMethod temp : result.getTestContext().getAllTestMethods()) {
			if (temp.getMethodName().equals(result.getMethod().getMethodName())) {
				return temp.getConstructorOrMethod().getMethod().getAnnotations();
			}
		}
		return null;
	}
	
	public static boolean hasKnownDefectAnnotation(ITestResult result) {
		if (result.getTestContext().getAllTestMethods()[0].getConstructorOrMethod().getMethod().getAnnotation(org.uncommons.reportng.annotations.KnownDefect.class) != null) {
			return true;
		}
		return false;
	}
	
	public String getProgress(double per) {
		if (per == 100) {
			return "<div class=\"progress\" role=\"progressbar\" style=\"width:100%;background-color:green;color:white;font-weight:bold;\">" + per + "%</div>";
		} else if (per < 0) {
			return "<div><b>-</b></div>";
		} else {
			return "<div class=\"progress\" role=\"progressbar\" style=\"width:100%;background-color:red;color:white;font-weight:bold;\">" + per + "%</div>";
		}
	}
	
	public String randomId() {
		return UUID.randomUUID().toString();
	}
	
	public static boolean knownDefectMode() {
		String knownDefectsMode = "false";
		try {
			knownDefectsMode = System.getProperty(HTMLReporter.KWOWNDEFECTSMODE);
		} catch (Exception ex) {
			
		}
		if (!Strings.isNullOrEmpty(knownDefectsMode) && knownDefectsMode.equalsIgnoreCase("true")) {
			knownDefectsMode = "true";
		}
		return Boolean.valueOf(knownDefectsMode);
	}
	
	public static boolean showRegressionColumn() {
		String regressionColumn = "false";
		try {
			regressionColumn = System.getProperty(HTMLReporter.SHOW_REGRESSION_COLUMN);
		} catch (Exception ex) {
			
		}
		if (!Strings.isNullOrEmpty(regressionColumn) && regressionColumn.equalsIgnoreCase("true")) {
			regressionColumn = "true";
		}
		return Boolean.valueOf(regressionColumn);
	}
	
	public static boolean showSuiteConfigurationMethods() {
		String showSuiteConfigurationMethods = "false";
		try {
			showSuiteConfigurationMethods = System.getProperty(HTMLReporter.SHOW_SUITE_CONFIGURATION_METHODS);
		} catch (Exception ex) {
			
		}
		if (!Strings.isNullOrEmpty(showSuiteConfigurationMethods) && showSuiteConfigurationMethods.equalsIgnoreCase("true")) {
			showSuiteConfigurationMethods = "true";
		}
		return Boolean.valueOf(showSuiteConfigurationMethods);
	}
	
	private String getClassFromFullClassName(String fullClassName) {
		String[] splitter = fullClassName.split("\\.");
		if (splitter.length > 1) {
			return splitter[splitter.length - 1];
		}
		return fullClassName;
	}
	
	public static IResultMap getTestContext(IResultMap iResultMap) {
		IResultMap map = new ResultMap();
		for (ITestResult temp : iResultMap.getAllResults()) {
			if (!temp.getMethod().isBeforeSuiteConfiguration() && !temp.getMethod().isAfterSuiteConfiguration()) {
				map.addResult(temp);
			}
		}
		return map;
	}
	
	public static IResultMap getSuiteContextBeforeSuite(IResultMap iResultMap) {
		IResultMap map = new ResultMap();
		for (ITestResult temp : iResultMap.getAllResults()) {
			if (temp.getMethod().isBeforeSuiteConfiguration()) {
				map.addResult(temp);
			}
		}
		return map;
	}
	
	public static IResultMap getSuiteContextAfterSuite(IResultMap iResultMap) {
		IResultMap map = new ResultMap();
		for (ITestResult temp : iResultMap.getAllResults()) {
			if (temp.getMethod().isAfterSuiteConfiguration()) {
				map.addResult(temp);
			}
		}
		return map;
	}
	
	public String getSuiteConfigurationBefore(int id, ISuite suite) {
		return getSuiteConfigurationData(suite, id, "BeforeSuite");
	}
	
	public String getSuiteConfigurationAfter(int id, ISuite suite) {
		return getSuiteConfigurationData(suite, id, "AfterSuite");
	}
	
	private String getSuiteConfigurationData(ISuite suite, int id, String conf) {
		if ("true".equalsIgnoreCase(System.getProperty(HTMLReporter.SHOW_SUITE_CONFIGURATION_METHODS))) {
			SuiteConfigurationType suiteConfigurationType = null;
			if (conf.equalsIgnoreCase("BeforeSuite")) {
				suiteConfigurationType = SuiteConfigurationType.BEFORESUITE;
			} else if (conf.equalsIgnoreCase("AfterSuite")) {
				suiteConfigurationType = SuiteConfigurationType.AFTERSUITE;
			}
			// Calculate Total before and after
			int totalPass = 0;
			int totalFail = 0;
			int totalSkip = 0;
			long totalDuration = 0;
			String startDateTime = "";
			Iterator<?> it = suite.getResults().entrySet().iterator();
			Set<ITestResult> suiteSetBefore = new HashSet<>();
			Set<ITestResult> suiteSetAfter = new HashSet<>();
			while (it.hasNext()) {
				Map.Entry pair = ((Map.Entry) it.next());
				ISuiteResult suiteResult = (ISuiteResult) pair.getValue();
				SuiteConfigurationDTO suiteConfigurationDTO = getSuiteConfiguration(suiteResult.getTestContext());
				if (SuiteConfigurationType.BEFORESUITE.toString().equalsIgnoreCase(conf)) {
					suiteSetBefore.addAll(suiteConfigurationDTO.getBefore());
				} else if (SuiteConfigurationType.AFTERSUITE.toString().equalsIgnoreCase(conf)) {
					suiteSetAfter.addAll(suiteConfigurationDTO.getAfter());
				}
			}
			
			Set<ITestResult> finalSet = null;
			if (SuiteConfigurationType.BEFORESUITE.equals(suiteConfigurationType)) {
				finalSet = suiteSetBefore;
			} else if (SuiteConfigurationType.AFTERSUITE.equals(suiteConfigurationType)) {
				finalSet = suiteSetAfter;
			}
			
			for (ITestResult temp : finalSet) {
				if (ITestResult.SUCCESS == temp.getStatus()) {
					totalPass++;
				} else if (ITestResult.FAILURE == temp.getStatus()) {
					totalFail++;
				} else if (ITestResult.SKIP == temp.getStatus()) {
					totalSkip++;
				}
			}
			totalDuration += getSuiteDuration(finalSet);
			startDateTime = getSuiteConfTime(finalSet);
			if (totalPass + totalFail + totalSkip > 0) {
				// Generate Code
				return generateOverviewSuiteConfiguration(id, suite, conf, totalPass, totalFail, totalSkip, totalDuration, startDateTime);
			}
		}
		return "";
		
	}
	
	private String generateOverviewSuiteConfiguration(int id, ISuite suite, String conf,
			int totalConfPass,
			int totalConfFail,
			int totalConfSkip,
			long totalDuration,
			String startDateTime) {
		StringBuilder htmlCode = new StringBuilder();
		htmlCode.append("<tr class=\"test\">");
		htmlCode.append("<td width=\"100\">" + startDateTime + "</td>");
		if (conf.equalsIgnoreCase("BeforeSuite")) {
			htmlCode.append("<td><a href=\"suite" + id + "_Before_suiteconfiguration-results.html\">@BeforeSuite" + "</a></td>");
		} else {
			htmlCode.append("<td><a href=\"suite" + id + "_After_suiteconfiguration-results.html\">@AfterSuite" + "</a></td>");
		}
		if (showRegressionColumn()) {
			htmlCode.append("<td class=\"duration\"></td>");
		}
		htmlCode.append("<td class=\"duration\">" + formatDurationinMinutes(totalDuration) + "</td>");
		// Total Pass
		if (totalConfPass > 0) {
			htmlCode.append("<td class=\"passed number\">" + totalConfPass + "</td>");
		} else {
			htmlCode.append("<td class=\"zero number\">0</td>");
		}
		// Total Skip
		if (totalConfSkip > 0) {
			htmlCode.append("<td class=\"skipped number\">" + totalConfSkip + "</td>");
		} else {
			htmlCode.append("<td class=\"zero number\">0</td>");
		}
		if (knownDefectMode()) {
			// #if ($utils.getKnownDefect($result.testContext).size() > 0)
			// htmlCode.append("<td class=\"knownDefects number\">$utils.getKnownDefect($result.testContext).size()</td>");
			htmlCode.append("<td class=\"zero number\">0</td>");
		}
		//
		if (knownDefectMode()) {
			// #if ($utils.getFixed($result.testContext).size() > 0)
			// htmlCode.append("<td class=\"fixed number\">$utils.getFixed($result.testContext).size()</td>");
			htmlCode.append("<td class=\"zero number\">0</td>");
		}
		// Total Fail
		if (totalConfFail > 0) {
			htmlCode.append("<td class=\"failed number\">" + totalConfFail + "</td>");
		} else {
			htmlCode.append("<td class=\"zero number\">0</td>");
		}
		// Calculate passRate
		int passRate = 0;
		int total = totalConfPass + totalConfSkip + totalConfFail;
		int totalPass = totalConfPass;
		if (total > 0 && totalPass > 0) {
			passRate = totalPass * 100 / total;
		}
		htmlCode.append("<td class=\"passRate\">" + passRate + "%</td>");
		htmlCode.append("</tr>");
		return htmlCode.toString();
	}
	
}