package org.uncommons.reportng.dto;

import org.uncommons.reportng.annotations.Feature;
import org.uncommons.reportng.annotations.KnownDefect;
import org.uncommons.reportng.annotations.NewFeature;

public class TestNGSemantics {
	
	public static final String FAILED = "testng.Failed";
	public static final String SKIP = "testng.Skipped";
	public static final String KNOWN_DEFECT = "testng." + KnownDefect.class.getSimpleName();
	public static final String PASS = "testng.Passed";
	public static final String FIXED = "testng.Fixed";
	public static final String FEATURE = "testng." + Feature.class.getSimpleName();
	public static final String NEWFEATURE = "testng." + NewFeature.class.getSimpleName();
	
	public static final String STRIPES = "==============================================================================================";
}
