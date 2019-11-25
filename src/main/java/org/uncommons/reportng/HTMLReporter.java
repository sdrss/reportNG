package org.uncommons.reportng;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.velocity.VelocityContext;
import org.testng.IClass;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;
import org.uncommons.reportng.dto.IssuesDTO;
import org.uncommons.reportng.dto.PackageDetailsDTO;
import org.uncommons.reportng.dto.ResultsDTO;

/**
 * Enhanced HTML reporter for TestNG that uses Velocity templates to generate its output.
 */
public class HTMLReporter extends AbstractReporter {
	
	// System Variables
	public static final String REPORTNG_TITLE = "org.uncommons.reportng.title";
	public static final String LOG_OUTPUT_REPORT = "org.uncommons.reportng.logOutputReport";
	public static final String KWOWNDEFECTSMODE = "org.uncommons.reportng.knownDefectsMode";
	public static final String EXTERNAL_LINKS = "org.uncommons.reportng.externalLinks";
	public static final String ESCAPE_OUTPUT = "org.uncommons.reportng.escape-output";
	public static final String SHOW_PASSED_CONFIGURATIONS = "org.uncommons.reportng.show-passed-configuration-methods";
	public static final String TEMPLATES_PATH = "org/uncommons/reportng/templates/html/";
	public static final String SKIP_EXECUTION = "org.uncommons.reportng.skip.execution";
	
	// HTML pages
	public static final String INDEX_FILE = "index.html";
	public static final String SUITES_FILE = "suites.html";
	public static final String MENU_FILE = "menu.html";
	public static final String SUITES_OVERVIEW_FILE = "suites_overview.html";
	public static final String OVERVIEW_FILE = "overview.html";
	public static final String GROUPS_FILE = "groups.html";
	public static final String RESULTS_FILE = "results.html";
	public static final String OUTPUT_FILE = "output.html";
	public static final String GRAPHS_TIME = "graphsTime.html";
	public static final String GRAPHS_RESULTS_PER_CLASS = "graphsRClass.html";
	public static final String GRAPHS_RESULTS_PER_SUITE = "graphsRSuite.html";
	public static final String ISSUES_NEW = "newIssues.html";
	public static final String ISSUES_KNOWN = "knownIssues.html";
	public static final String ISSUES_FIXED = "fixedIssues.html";
	public static final String RUN_ARGUMENTS = "runArguments.html";
	public static final String REGRESSION = "regression.html";
	public static final String FEATURES = "newFeatures.html";
	public static final String PACKAGES = "packages.html";
	public static final String GROUPS = "groupsresults.html";
	// JS scripts
	public static final String CANVAS_FILE = "canvas.js";
	// Keys
	public static final String SUITE_KEY = "suite";
	public static final String SUITES_KEY = "suites";
	public static final String GROUPS_KEY = "groups";
	public static final String RESULT_KEY = "result";
	public static final String FAILED_CONFIG_KEY = "failedConfigurations";
	public static final String SKIPPED_CONFIG_KEY = "skippedConfigurations";
	public static final String PASSED_CONFIG_KEY = "passedConfigurations";
	public static final String FAILED_TESTS_KEY = "failedTests";
	public static final String SKIPPED_TESTS_KEY = "skippedTests";
	public static final String PASSED_TESTS_KEY = "passedTests";
	public static final String REPORT_DIRECTORY = "html";
	
	public static final Comparator<ITestNGMethod> METHOD_COMPARATOR = new TestMethodComparator();
	public static final Comparator<ITestResult> RESULT_COMPARATOR = new TestResultComparator();
	public static final Comparator<IClass> CLASS_COMPARATOR = new TestClassComparator();
	public static long totalDuration = 0;
	public static String suiteName = "";
	public static String OUTPUTDIRECTORY = "";
	public static String OUTPUTDIRECTORY_ABSOLUTE = "";
	
	private static ResultsDTO results;
	private static IssuesDTO issuesDTO;
	private static Map<PackageDetailsDTO, List<PackageDetailsDTO>> packageDeatails;
	private static Map<PackageDetailsDTO, List<PackageDetailsDTO>> groupDetails;
	
	public HTMLReporter() {
		super(TEMPLATES_PATH);
	}
	
	/**
	 * Generates a set of HTML files that contain data about the outcome of the specified test suites.
	 */
	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectoryName) {
		OUTPUTDIRECTORY = outputDirectoryName;
		totalDuration = 0;
		File outputDirectory = new File(outputDirectoryName, REPORT_DIRECTORY);
		OUTPUTDIRECTORY_ABSOLUTE = outputDirectory.getAbsolutePath();
		try {
			System.out.println("****************************************");
			System.out.println("Generate reportNG report");
			System.out.println("Path : " + OUTPUTDIRECTORY_ABSOLUTE);
			// Sort Suites
			List<ISuite> sortedSuites = sortSuitesChronologicaly(suites);
			sortedSuites = sortResultsChronologicaly(sortedSuites);
			// Clear files
			removeEmptyDirectories(outputDirectory);
			// Generate Directory
			generateDirectory(outputDirectory);
			// Copy Resources
			copyResources(outputDirectory);
			// Update results
			setResults(ReporterHelper.checkAttribute(sortedSuites));
			setIssuesDTO(ReporterHelper.issues(sortedSuites));
			setPackageDeatails(ReporterHelper.packageDetails(sortedSuites));
			setGroupDetails(ReporterHelper.groupDetails(sortedSuites));
			// Create Frames
			createFrameset(outputDirectory);
			// Create Menu
			createMenu(sortedSuites, outputDirectory);
			// Create Overview
			createOverview(sortedSuites, outputDirectory);
			// Overview
			createTestOverview(sortedSuites, outputDirectory);
			// Suites
			createSuiteList(sortedSuites, outputDirectory);
			// Groups
			createGroups(sortedSuites, outputDirectory);
			// Results
			createTestResults(sortedSuites, outputDirectory);
			// Graphs
			createGraphs(sortedSuites, outputDirectory);
			// Issues
			createIssuesResults(sortedSuites, outputDirectory);
			// Run Arguments
			createRunArguments(sortedSuites, outputDirectory);
			// Create Features
			createFeaturesResults(sortedSuites, outputDirectory);
			// Packages
			createPackagesResults(sortedSuites, outputDirectory);
			// Groups View
			createGroupResults(sortedSuites, outputDirectory);
			// Create Log
			createReportLogOutput(outputDirectory);
			System.out.println("****************************************");
		} catch (Exception ex) {
			throw new ReportNGException("Failed generating HTML report.", ex);
		}
	}
	
	private void createReportLogOutput(File outputDirectory) throws Exception {
		VelocityContext context = createContext();
		generateFile(new File(outputDirectory, OUTPUT_FILE), OUTPUT_FILE + TEMPLATE_EXTENSION, context);
	}
	
	private void createRunArguments(List<ISuite> suites, File outputDirectory) throws Exception {
		VelocityContext context = createContext();
		context.put(SUITES_KEY, suites);
		generateFile(new File(outputDirectory, RUN_ARGUMENTS), RUN_ARGUMENTS + TEMPLATE_EXTENSION, context);
	}
	
	private void createFeaturesResults(List<ISuite> suites, File outputDirectory) throws Exception {
		VelocityContext context = createContext();
		context.put(SUITES_KEY, suites);
		generateFile(new File(outputDirectory, REGRESSION), REGRESSION + TEMPLATE_EXTENSION, context);
		generateFile(new File(outputDirectory, FEATURES), FEATURES + TEMPLATE_EXTENSION, context);
	}
	
	private void createIssuesResults(List<ISuite> suites, File outputDirectory) throws Exception {
		VelocityContext context = createContext();
		context.put(SUITES_KEY, suites);
		generateFile(new File(outputDirectory, ISSUES_NEW), ISSUES_NEW + TEMPLATE_EXTENSION, context);
		generateFile(new File(outputDirectory, ISSUES_FIXED), ISSUES_FIXED + TEMPLATE_EXTENSION, context);
		generateFile(new File(outputDirectory, ISSUES_KNOWN), ISSUES_KNOWN + TEMPLATE_EXTENSION, context);
	}
	
	private void createOverview(List<ISuite> suites, File outputDirectory) throws Exception {
		VelocityContext context = createContext();
		context.put(SUITES_KEY, suites);
		generateFile(new File(outputDirectory, OVERVIEW_FILE), OVERVIEW_FILE + TEMPLATE_EXTENSION, context);
	}
	
	private void createMenu(List<ISuite> suites, File outputDirectory) throws Exception {
		VelocityContext context = createContext();
		context.put(SUITES_KEY, suites);
		generateFile(new File(outputDirectory, MENU_FILE), MENU_FILE + TEMPLATE_EXTENSION, context);
	}
	
	private void createFrameset(File outputDirectory) throws Exception {
		VelocityContext context = createContext();
		generateFile(new File(outputDirectory, INDEX_FILE), INDEX_FILE + TEMPLATE_EXTENSION, context);
	}
	
	private void createTestOverview(List<ISuite> suites, File outputDirectory) throws Exception {
		VelocityContext context = createContext();
		context.put(SUITES_KEY, suites);
		generateFile(new File(outputDirectory, SUITES_OVERVIEW_FILE), SUITES_OVERVIEW_FILE + TEMPLATE_EXTENSION, context);
	}
	
	private void createSuiteList(List<ISuite> suites, File outputDirectory) throws Exception {
		VelocityContext context = createContext();
		context.put(SUITES_KEY, suites);
		generateFile(new File(outputDirectory, SUITES_FILE), SUITES_FILE + TEMPLATE_EXTENSION, context);
	}
	
	private void createTestResults(List<ISuite> suites, File outputDirectory) throws Exception {
		int index = 1;
		for (ISuite suite : suites) {
			int index2 = 1;
			for (ISuiteResult result : suite.getResults().values()) {
				VelocityContext context = createContext();
				context.put(RESULT_KEY, result);
				context.put(FAILED_CONFIG_KEY, sortByTestClass(result.getTestContext().getFailedConfigurations()));
				context.put(SKIPPED_CONFIG_KEY, sortByTestClass(result.getTestContext().getSkippedConfigurations()));
				context.put(PASSED_CONFIG_KEY, sortByTestClass(result.getTestContext().getPassedConfigurations()));
				context.put(FAILED_TESTS_KEY, sortByTestClass(result.getTestContext().getFailedTests()));
				context.put(SKIPPED_TESTS_KEY, sortByTestClass(result.getTestContext().getSkippedTests()));
				context.put(PASSED_TESTS_KEY, sortByTestClass(result.getTestContext().getPassedTests()));
				String fileName = String.format("suite%d_test%d_%s", index, index2, RESULTS_FILE);
				generateFile(new File(outputDirectory, fileName), RESULTS_FILE + TEMPLATE_EXTENSION, context);
				++index2;
			}
			++index;
		}
	}
	
	private void createPackagesResults(List<ISuite> suites, File outputDirectory) throws Exception {
		VelocityContext context = createContext();
		context.put(SUITES_KEY, suites);
		generateFile(new File(outputDirectory, PACKAGES), PACKAGES + TEMPLATE_EXTENSION, context);
	}
	
	private void createGroupResults(List<ISuite> suites, File outputDirectory) throws Exception {
		VelocityContext context = createContext();
		context.put(SUITES_KEY, suites);
		generateFile(new File(outputDirectory, GROUPS), GROUPS + TEMPLATE_EXTENSION, context);
	}
	
	protected void createGraphs(List<ISuite> suites, File outputDirectory) throws Exception {
		VelocityContext context = createContext();
		context.put(SUITES_KEY, suites);
		generateFile(new File(outputDirectory, GRAPHS_TIME), GRAPHS_TIME + TEMPLATE_EXTENSION, context);
		generateFile(new File(outputDirectory, GRAPHS_RESULTS_PER_CLASS), GRAPHS_RESULTS_PER_CLASS + TEMPLATE_EXTENSION, context);
		generateFile(new File(outputDirectory, GRAPHS_RESULTS_PER_SUITE), GRAPHS_RESULTS_PER_SUITE + TEMPLATE_EXTENSION, context);
	}
	
	private void createGroups(List<ISuite> suites, File outputDirectory) throws Exception {
		int index = 1;
		for (ISuite suite : suites) {
			SortedMap<String, SortedSet<ITestNGMethod>> groups = sortGroups(suite.getMethodsByGroups());
			if (!groups.isEmpty()) {
				VelocityContext context = createContext();
				context.put(SUITE_KEY, suite);
				context.put(GROUPS_KEY, groups);
				String fileName = String.format("suite%d_%s", index, GROUPS_FILE);
				generateFile(new File(outputDirectory, fileName), GROUPS_FILE + TEMPLATE_EXTENSION, context);
			}
			++index;
		}
	}
	
	private SortedMap<IClass, List<ITestResult>> sortByTestClass(IResultMap results) {
		SortedMap<IClass, List<ITestResult>> sortedResults = new TreeMap<IClass, List<ITestResult>>(CLASS_COMPARATOR);
		for (ITestResult result : results.getAllResults()) {
			List<ITestResult> resultsForClass = sortedResults.get(result.getTestClass());
			if (resultsForClass == null) {
				resultsForClass = new ArrayList<ITestResult>();
				sortedResults.put(result.getTestClass(), resultsForClass);
			}
			resultsForClass.add(0, result);
		}
		return sortedResults;
	}
	
	private SortedMap<String, SortedSet<ITestNGMethod>> sortGroups(Map<String, Collection<ITestNGMethod>> groups) {
		SortedMap<String, SortedSet<ITestNGMethod>> sortedGroups = new TreeMap<String, SortedSet<ITestNGMethod>>();
		for (Map.Entry<String, Collection<ITestNGMethod>> entry : groups.entrySet()) {
			SortedSet<ITestNGMethod> methods = new TreeSet<ITestNGMethod>(METHOD_COMPARATOR);
			methods.addAll(entry.getValue());
			sortedGroups.put(entry.getKey(), methods);
		}
		return sortedGroups;
	}
	
	private void copyResources(File outputDirectory) throws IOException {
		copyStream("css/reportng.css", "css/reportng.css", outputDirectory);
		copyStream("css/reportngClass.css", "css/reportngClass.css", outputDirectory);
		copyStream("css/bootstrap.min.css", "css/bootstrap.min.css", outputDirectory);
		copyStream("js/reportng.js", "js/reportng.js", outputDirectory);
		copyStream("js/reportngClass.js", "js/reportngClass.js", outputDirectory);
		copyStream("js/sorttable.js", "js/sorttable.js", outputDirectory);
		copyStream("js/jquery-1.12.0.min.js", "js/jquery-1.12.0.min.js", outputDirectory);
		copyStream("js/jquery-1.4.4.min.js", "js/jquery-1.4.4.min.js", outputDirectory);
		copyStream("js/jquery.tablesorter.min.js", "js/jquery.tablesorter.min.js", outputDirectory);
		copyStream("js/json-min.js", "js/json-min.js", outputDirectory);
		copyStream("js/canvasjs.min.js", "js/canvasjs.min.js", outputDirectory);
		copyStream("js/bootstrap.min.js", "js/bootstrap.min.js", outputDirectory);
		copyStream("images/testng.png", "images/testng.png", outputDirectory);
		
		copyStream("images/asc.gif", "images/asc.gif", outputDirectory);
		copyStream("images/bg.gif", "images/bg.gif", outputDirectory);
		copyStream("images/desc.gif", "images/desc.gif", outputDirectory);
		copyStream("images/fav.png", "images/fav.png", outputDirectory);
		
		copyStream("fonts/glyphicons-halflings-regular.eot", "fonts/glyphicons-halflings-regular.eot", outputDirectory);
		copyStream("fonts/glyphicons-halflings-regular.svg", "fonts/glyphicons-halflings-regular.svg", outputDirectory);
		copyStream("fonts/glyphicons-halflings-regular.ttf", "fonts/glyphicons-halflings-regular.ttf", outputDirectory);
		copyStream("fonts/glyphicons-halflings-regular.woff", "fonts/glyphicons-halflings-regular.woff", outputDirectory);
		copyStream("fonts/glyphicons-halflings-regular.woff2", "fonts/glyphicons-halflings-regular.woff2", outputDirectory);
	}
	
	private List<ISuite> sortSuitesChronologicaly(List<ISuite> suites) {
		List<ISuite> sortedSuites = new ArrayList<ISuite>();
		Map<Date, ISuite> dates = new HashMap<Date, ISuite>();
		for (ISuite temp : suites) {
			Map<String, ISuiteResult> allResults = temp.getResults();
			Iterator<Entry<String, ISuiteResult>> iter = allResults.entrySet().iterator();
			if (iter.hasNext()) {
				Map.Entry<String, ISuiteResult> mEntry = iter.next();
				dates.put(mEntry.getValue().getTestContext().getStartDate(), temp);
			}
		}
		Map<Date, ISuite> treeMap = new TreeMap<Date, ISuite>(dates);
		Iterator<Entry<Date, ISuite>> iter = treeMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Date, ISuite> mEntry = iter.next();
			sortedSuites.add(mEntry.getValue());
		}
		return sortedSuites;
	}
	
	private List<ISuite> sortResultsChronologicaly(List<ISuite> suites) {
		for (ISuite temp : suites) {
			Map<String, ISuiteResult> allResults = temp.getResults();
			Iterator<Entry<String, ISuiteResult>> iter = allResults.entrySet().iterator();
			List<ITestNGMethod> iTestNGMethod = new ArrayList<ITestNGMethod>();
			if (iter.hasNext()) {
				Map.Entry<String, ISuiteResult> mEntry = iter.next();
				iTestNGMethod = Arrays.asList(mEntry.getValue().getTestContext().getAllTestMethods());
				Collections.sort(iTestNGMethod, METHOD_COMPARATOR);
			}
		}
		return suites;
	}
	
	public static ResultsDTO getResults() {
		return results;
	}
	
	public static void setResults(ResultsDTO results) {
		HTMLReporter.results = results;
	}
	
	public static IssuesDTO getIssuesDTO() {
		return issuesDTO;
	}
	
	public static void setIssuesDTO(IssuesDTO issuesDTO) {
		HTMLReporter.issuesDTO = issuesDTO;
	}
	
	public static Map<PackageDetailsDTO, List<PackageDetailsDTO>> getPackageDeatails() {
		return packageDeatails;
	}
	
	public static void setPackageDeatails(Map<PackageDetailsDTO, List<PackageDetailsDTO>> packageDeatails) {
		HTMLReporter.packageDeatails = packageDeatails;
	}
	
	public static Map<PackageDetailsDTO, List<PackageDetailsDTO>> getGroupDetails() {
		return groupDetails;
	}
	
	public static void setGroupDetails(Map<PackageDetailsDTO, List<PackageDetailsDTO>> groupDetails) {
		HTMLReporter.groupDetails = groupDetails;
	}
	
}