package org.uncommons.reportng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.testng.IConfigurationListener2;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.TestListenerAdapter;
import org.uncommons.reportng.dto.TestNGSemantics;

/**
 * Convenient base class for the ReportNG reporters. Provides common
 * functionality.
 */
public abstract class AbstractReporter extends TestListenerAdapter implements IReporter, ISuiteListener, IConfigurationListener2 {

	private static final String ENCODING = "UTF-8";
	protected static final String TEMPLATE_EXTENSION = ".vm";
	private static final String META_KEY = "meta";
	protected static final ReportMetadata META = new ReportMetadata();
	private static final String UTILS_KEY = "utils";
	private static final ReportNGUtils UTILS = new ReportNGUtils();
	private static final String MESSAGES_KEY = "messages";
	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("org.uncommons.reportng.messages.reportng", META.getLocale());
	private final String classpathPrefix;

	/**
	 * @param classpathPrefix
	 *            Where in the classpath to load templates from.
	 */
	protected AbstractReporter(String classpathPrefix) {
		this.classpathPrefix = classpathPrefix;
		Velocity.setProperty("resource.loader", "classpath");
		Velocity.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		if (!META.shouldGenerateVelocityLog()) {
			Velocity.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
		}
		try {
			Velocity.init();
		} catch (Exception ex) {
			throw new ReportNGException("Failed to initialise Velocity.", ex);
		}
	}

	/**
	 * Helper method that creates a Velocity context and initialises it with a
	 * reference to the ReportNG utils, report metadata and localised messages.
	 * 
	 * @return An initialised Velocity context.
	 */
	protected VelocityContext createContext() {
		VelocityContext context = new VelocityContext();
		context.put(META_KEY, META);
		context.put(UTILS_KEY, UTILS);
		context.put(MESSAGES_KEY, MESSAGES);
		return context;
	}

	/**
	 * Generate the specified output file by merging the specified Velocity
	 * template with the supplied context.
	 */
	protected void generateFile(File file, String templateName, VelocityContext context) throws Exception {
		Writer writer = new BufferedWriter(new FileWriter(file));
		try {
			Velocity.mergeTemplate(classpathPrefix + templateName, ENCODING, context, writer);
			writer.flush();
		} finally {
			writer.close();
		}
	}

	/**
	 * Helper method to copy the contents of a stream to a file.
	 * 
	 * @param outputDirectory
	 *            The directory in which the new file is created.
	 * @param stream
	 *            The stream to copy.
	 * @param targetFileName
	 *            The file to write the stream contents to.
	 * @throws IOException
	 *             If the stream cannot be copied.
	 */
	protected void copyStream(File outputDirectory, InputStream stream, String targetFileName) throws IOException {
		File resourceFile = new File(outputDirectory, targetFileName);
		BufferedReader reader = null;
		Writer writer = null;
		try {
			reader = new BufferedReader(new InputStreamReader(stream));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resourceFile)));
			String line = reader.readLine();
			while (line != null) {
				writer.write(line);
				writer.write('\n');
				line = reader.readLine();
			}
			writer.flush();
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
		}
	}

	protected void copyStream(String sourceFile, String destinationFile, File outputDirectory) throws IOException {
		String resourcePath = "/" + classpathPrefix + sourceFile;
		URL inputUrl = this.getClass().getResource(resourcePath);
		File dest = new File(outputDirectory.getAbsolutePath() + "/" + destinationFile);
		FileUtils.copyURLToFile(inputUrl, dest);
	}

	protected void removeEmptyDirectories(File outputDirectory) {
		if (outputDirectory.exists()) {
			try {
				FileUtils.deleteDirectory(outputDirectory);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void generateDirectory(File file) {
		file.mkdirs();
	}

	@Override
	public void onFinish(ISuite iSuite) {
		for (Entry<String, ISuiteResult> entry : iSuite.getResults().entrySet()) {
			ITestContext testContext = entry.getValue().getTestContext();
			testContext = ReporterHelper.updateResults(testContext);
		}
	}

	@Override
	public void onStart(ISuite iSuite) {

	}

	@Override
	public void onStart(ITestContext testContext) {

	}

	@Override
	public void onFinish(ITestContext testContext) {
		super.onFinish(testContext);
		String passed = System.getProperty(TestNGSemantics.PASS);
		if (passed == null) {
			passed = "0";
			System.setProperty(TestNGSemantics.PASS, passed);
		}
		String failed = System.getProperty(TestNGSemantics.FAILED);
		if (failed == null) {
			failed = "0";
			System.setProperty(TestNGSemantics.FAILED, failed);
		}
		String skipped = System.getProperty(TestNGSemantics.SKIP);
		if (skipped == null) {
			skipped = "0";
			System.setProperty(TestNGSemantics.SKIP, skipped);
		}
		String known = System.getProperty(TestNGSemantics.KNOWN_DEFECT);
		if (known == null) {
			known = "0";
			System.setProperty(TestNGSemantics.KNOWN_DEFECT, known);
		}
		String fixed = System.getProperty(TestNGSemantics.FIXED);
		if (fixed == null) {
			fixed = "0";
			System.setProperty(TestNGSemantics.FIXED, fixed);
		}

		int passI = ReportNGUtils.getPassed(testContext).size();
		System.setProperty(TestNGSemantics.PASS, Integer.toString(passI + Integer.parseInt(passed)));

		int failI = ReportNGUtils.getFailed(testContext).size();
		System.setProperty(TestNGSemantics.FAILED, Integer.toString(failI + Integer.parseInt(failed)));

		int skipI = ReportNGUtils.getSkip(testContext).size();
		System.setProperty(TestNGSemantics.SKIP, Integer.toString(skipI + Integer.parseInt(skipped)));

		int knownI = ReportNGUtils.getKnownDefect(testContext).size();
		System.setProperty(TestNGSemantics.KNOWN_DEFECT, Integer.toString(knownI + Integer.parseInt(known)));

		int fixedI = ReportNGUtils.getFixed(testContext).size();
		System.setProperty(TestNGSemantics.FIXED, Integer.toString(fixedI + Integer.parseInt(fixed)));
		if (!"true".equalsIgnoreCase(System.getProperty(HTMLReporter.SKIP_EXECUTION))) {
			if ("true".equalsIgnoreCase(System.getProperty(HTMLReporter.KWOWNDEFECTSMODE))) {
				Reporter.log(TestNGSemantics.STRIPES, true);
				Reporter.log("Total Passed: " + System.getProperty(TestNGSemantics.PASS) + "(+" + passI + "), " +
						"Failures: " + System.getProperty(TestNGSemantics.FAILED) + "(+" + failI + "), " +
						"Skips: " + System.getProperty(TestNGSemantics.SKIP) + "(+" + skipI + "), " +
						"Known: " + System.getProperty(TestNGSemantics.KNOWN_DEFECT) + "(+" + knownI + "), " +
						"Fixed: " + System.getProperty(TestNGSemantics.FIXED) + "(+" + fixedI + ")", true);
				Reporter.log(TestNGSemantics.STRIPES, true);
			} else {
				Reporter.log(TestNGSemantics.STRIPES, true);
				Reporter.log("Total Passed: " + System.getProperty(TestNGSemantics.PASS) + "(+" + passI + "), " +
						"Failures: " + System.getProperty(TestNGSemantics.FAILED) + "(+" + failI + "), " +
						"Skips: " + System.getProperty(TestNGSemantics.SKIP) + "(+" + skipI + ")", true);
				Reporter.log(TestNGSemantics.STRIPES, true);
			}

		}
	}

	@Override
	public void onTestStart(ITestResult tr) {
		skipExecution(tr);
	}

	@Override
	public void onTestSkipped(ITestResult tr) {
	}

	@Override
	public void onTestFailure(ITestResult tr) {
	}

	@Override
	public void onTestSuccess(ITestResult tr) {
	}

	@Override
	public void onConfigurationSuccess(ITestResult tr) {
	}

	@Override
	public void onConfigurationFailure(ITestResult tr) {
	}

	@Override
	public void onConfigurationSkip(ITestResult tr) {
	}

	@Override
	public void beforeConfiguration(ITestResult tr) {
		skipExecution(tr);
	}

	public static void skipExecution(ITestResult iTestResult) {
		if ("true".equalsIgnoreCase(System.getProperty(HTMLReporter.SKIP_EXECUTION))) {
			throw new SkipException("Skipped because property [" + HTMLReporter.SKIP_EXECUTION + "=" + true + "]");
		}
	}

}