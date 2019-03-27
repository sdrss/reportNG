## reportNG ##

![](https://github.com/sdrss/test/blob/master/SampleOverview.png)
 
ReportNG is a simple HTML reporting plug-in for the TestNG unit-testing framework. It is intended as a replacement for the default TestNG HTML report.

## Based on ReportNG v1.1.4 this is a ReportNG with : ##
 - new HTML layout
 - new semantics for Known and Fixed issues
 - summary report for Regression and New Features tests
 - graphs of Test Execution
 - and various fixes
 
See a sample report [here](https://sdrss.github.io/test/)
  
 ## Supported System Properties ##
 * org.uncommons.reportng.escape-output : Used to turn off escaping for log output in the reports (not recommended). The default is for output to be escaped, since this prevents characters such as '<' and '&' from causing mark-up problems. If escaping is turned off, then log text is included as raw HTML/XML, which allows for the insertion of hyperlinks and other nasty hacks.
 * org.uncommons.reportng.title : Used to over-ride the report title.
 * org.uncommons.reportng.show-passed-configuration-methods : Set to "true" or "false" to specify whether the pass Configuration methods (@BeforeClass,@AfterClass etc.) should be included in test output. Failures are reported by default always.
 * org.uncommons.reportng.knownDefectsMode : Set to "true" or "false" to specify if tests with @KnownDefect is marked as Known or Fixed Defect according to test Result.
 * org.uncommons.reportng.logOutputReport : Set to "true" or "false" to specify if a summary of log output is generated and linked in test report.
 * org.uncommons.reportng.locale
Over-rides the default locale for localised messages in generated reports. If not specified, the JVM default locale is used. If there are no translations available for the selected locale the default English messages are used instead. This property should be set to an ISO language code (e.g. "en" or "fr") or to an ISO language code and an ISO country code separated by an underscore (e.g. "en_US" or "fr_CA").
 * org.uncommons.reportng.skip.execution : Set to "true" whenever you need to skip the rest testNG execution.
 
 ## How to use ReportNG ##
 
 To use the reporting plug-in, set the listeners attribute of the testng element. The class names for the ReportNG reporters are:

    org.uncommons.reportng.HTMLReporter
    org.uncommons.reportng.JUnitXMLReporter
 You may also want to disable the default TestNG reporters by setting the useDefaultListeners attribute to "false".

 ## Usage of @KnownDefect

    import org.uncommons.reportng.annotations.KnownDefect;
    
    @KnownDefect(description="Jira Ticket XXXX")
    @Test(description = "Test1")
    public void test1() throws Exception {
        /*Test Code that eventually will produce an Exception*/
	     new Exception("Assert Error");
    }
    
  By enabling the "org.uncommons.reportng.knownDefectsMode" the above test will be marked as Known Defect.
  If test doesn't throw any Exception then the test will be marked as Fixed.
    
 ## Usage of @Feature
 
    import org.uncommons.reportng.annotations.Feature;
    
    @Feature(description = "This is a Feature")
    public class Test1 {
    	@Test(description = "Test1")
    	public void test1() throws Exception {
		/*Test Code*/
	}
    }
     
   Test Classes with @Feature will be reported as Regression tests.
     
  ## Usage of @NewFeature
    
    import org.uncommons.reportng.annotations.NewFeature;
     
    @NewFeature(description = "This is a new Feature")
    public class Test1 {
    	
	@Test(description = "Test1")
	public void test1() throws Exception {
        	/*Test Code*/
	}
    }
     
   Test Classes with @NewFeature will be reported as new Features tests.

  ## Usage of Skip Execution
   At any point but it’s advisable to be the first case in suite xml, you can set this system variable to "true" and skip the rest of  test execution, for example :
   
    @Test(description = "In case of a failure Skip Execution")
    public void testLoginPage() throws Exception {
        try{
	   loginToMyTestEnv();
	}catch(Exception ex){
	  System.setProperty("org.uncommons.reportng.skip.execution","true");
	  throw ex;
	}
    }
    
  By enabling this system property all of the rest testNG tests will be skipped and the genareted report will have on overview page the root cause of the failure providing the message "Skip Execution due to Skip Execution Mode".
  Alternative you can use this in your TestListener and Skip test execution on the first failure. This will work as Fail Fast Mode. See [Wiki](https://github.com/sdrss/reportNG/wiki) for example.
     
 ## Mvn dependency : 
      
      <dependency>
	   <groupId>com.github.sdrss</groupId>
	   <artifactId>reportng</artifactId>
	   <version>2.2.0</version>
      </dependency>
