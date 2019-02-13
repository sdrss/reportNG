## reportNG ##

![](https://github.com/sdrss/test/blob/master/SampleOverview.png)
ReportNG is a simple HTML reporting plug-in for the TestNG unit-testing framework. It is intended as a replacement for the default TestNG HTML report. The default report is comprehensive but is not so easy to understand at-a-glance. ReportNG provides a simple, colour-coded view of the test results.

## Based on ReportNG v1.1.4 this is a new ReportNG with : ##
 - new HTML layout
 - new annotations for known Defects, new and regression features
 - graphs of test execution
 - various fixes
 

 ## Supported System Properties ##
 * org.uncommons.reportng.title : Used to over-ride the report title.
 * org.uncommons.reportng.show-passed-configuration-methods : Set to "true" or "false" to specify whether the pass Configuration methods (@BeforeClass,@AfterClass etc.) should be included in test output. Failures are reported by default always.
 * org.uncommons.reportng.knownDefectsMode : Set to "true" or "false" to specify if tests with @KnownDefect is marked as Known or Fixed Defect according to test Result.
 * org.uncommons.reportng.logOutputReport : Set to "true" or "false" to specify if a summary of log output is generated and linked in test report.
 
 ## How to use ReportNG ##
 
 To use the reporting plug-in, set the listeners attribute of the testng element. The class names for the ReportNG reporters are:

    org.uncommons.reportng.HTMLReporter
    org.uncommons.reportng.JUnitXMLReporter
 You may also want to disable the default TestNG reporters by setting the useDefaultListeners attribute to "false".

 ## Usage of @KnownDefect
 
  	@KnownDefect(description="Jira Ticket XXXX")
	  @Test(description = "Test2")
	  public void test1() throws Exception {
         /*Test Code that eventually will produce an Exception*/
		     throw new Exception("Assert Error3");
	  }
    
  By enabling the "org.uncommons.reportng.knownDefectsMode" the above test will be marked as Known Defect.
  If test doesn't throw any Exception then the test will be marked as Fixed.
    
 ## Usage of @Feature
 
     @Feature(description = "This is a Feature")
     public class Test1 {
	   @Test(priority = 1, description = "Test1")
	   public void test1() throws Exception {
         /*Test Code*/
	   }
	   }
     
   Test Classes with @Feature will be reported as Regression tests.
     
  ## Usage of @NewFeature
 
     @NewFeature(description = "This is a Feature")
     public class Test1 {
	   @Test(priority = 1, description = "Test1")
	   public void test1() throws Exception {
         /*Test Code*/
	   }
	   }
     
   Test Classes with @NewFeature will be reported as new Features tests.
