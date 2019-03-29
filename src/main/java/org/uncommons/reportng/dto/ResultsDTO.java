package org.uncommons.reportng.dto;

import java.util.Date;

public class ResultsDTO {
	
	private int totalPass;
	private int totalFail;
	private int totalSkip;
	private int totalKnownDefect;
	private int totalFixed;
	
	private int newFeaturesPass;
	private int newFeaturesFail;
	private int newFeaturesSkip;
	private int newFeaturesKnownDefect;
	private int newFeaturesFixed;
	
	private int regressionPass;
	private int regressionFail;
	private int regressionSkip;
	private int regressionKnownDefect;
	private int regressionFixed;
	
	private int summaryRegression;
	private int summaryNewFeature;
	private int summaryTotal;
	
	private double regression;
	private double newFeatures;
	private double total;
	
	private Date startDate;
	private Date endDate;
	private String executionTime;
	
	public ResultsDTO() {
		
	}
	
	public int getTotalPass() {
		return totalPass;
	}
	
	public void setTotalPass(int totalPass) {
		this.totalPass = totalPass;
	}
	
	public int getTotalFail() {
		return totalFail;
	}
	
	public void setTotalFail(int totalFail) {
		this.totalFail = totalFail;
	}
	
	public int getTotalSkip() {
		return totalSkip;
	}
	
	public void setTotalSkip(int totalSkip) {
		this.totalSkip = totalSkip;
	}
	
	public int getTotalKnownDefect() {
		return totalKnownDefect;
	}
	
	public void setTotalKnownDefect(int totalKnownDefect) {
		this.totalKnownDefect = totalKnownDefect;
	}
	
	public int getTotalFixed() {
		return totalFixed;
	}
	
	public void setTotalFixed(int totalFixed) {
		this.totalFixed = totalFixed;
	}
	
	public int getNewFeaturesPass() {
		return newFeaturesPass;
	}
	
	public void setNewFeaturesPass(int newFeaturesPass) {
		this.newFeaturesPass = newFeaturesPass;
	}
	
	public int getNewFeaturesFail() {
		return newFeaturesFail;
	}
	
	public void setNewFeaturesFail(int newFeaturesFail) {
		this.newFeaturesFail = newFeaturesFail;
	}
	
	public int getNewFeaturesSkip() {
		return newFeaturesSkip;
	}
	
	public void setNewFeaturesSkip(int newFeaturesSkip) {
		this.newFeaturesSkip = newFeaturesSkip;
	}
	
	public int getNewFeaturesKnownDefect() {
		return newFeaturesKnownDefect;
	}
	
	public void setNewFeaturesKnownDefect(int newFeaturesKnownDefect) {
		this.newFeaturesKnownDefect = newFeaturesKnownDefect;
	}
	
	public int getNewFeaturesFixed() {
		return newFeaturesFixed;
	}
	
	public void setNewFeaturesFixed(int newFeaturesFixed) {
		this.newFeaturesFixed = newFeaturesFixed;
	}
	
	public int getRegressionPass() {
		return regressionPass;
	}
	
	public void setRegressionPass(int regressionPass) {
		this.regressionPass = regressionPass;
	}
	
	public int getRegressionFail() {
		return regressionFail;
	}
	
	public void setRegressionFail(int regressionFail) {
		this.regressionFail = regressionFail;
	}
	
	public int getRegressionSkip() {
		return regressionSkip;
	}
	
	public void setRegressionSkip(int regressionSkip) {
		this.regressionSkip = regressionSkip;
	}
	
	public int getRegressionKnownDefect() {
		return regressionKnownDefect;
	}
	
	public void setRegressionKnownDefect(int regressionKnownDefect) {
		this.regressionKnownDefect = regressionKnownDefect;
	}
	
	public int getRegressionFixed() {
		return regressionFixed;
	}
	
	public void setRegressionFixed(int regressionFixed) {
		this.regressionFixed = regressionFixed;
	}
	
	public double getRegression() {
		return regression;
	}
	
	public void setRegression(double regression) {
		this.regression = regression;
	}
	
	public double getNewFeatures() {
		return newFeatures;
	}
	
	public void setNewFeatures(double newFeatures) {
		this.newFeatures = newFeatures;
	}
	
	public double getTotal() {
		return total;
	}
	
	public void setTotal(double total) {
		this.total = total;
	}
	
	public int getSummaryRegression() {
		return summaryRegression;
	}
	
	public void setSummaryRegression(int summaryRegression) {
		this.summaryRegression = summaryRegression;
	}
	
	public int getSummaryNewFeature() {
		return summaryNewFeature;
	}
	
	public void setSummaryNewFeature(int summaryNewFeature) {
		this.summaryNewFeature = summaryNewFeature;
	}
	
	public int getSummaryTotal() {
		return summaryTotal;
	}
	
	public void setSummaryTotal(int summaryTotal) {
		this.summaryTotal = summaryTotal;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public String getExecutionTime() {
		return executionTime;
	}
	
	public void setExecutionTime(String executionTime) {
		this.executionTime = executionTime;
	}
}
