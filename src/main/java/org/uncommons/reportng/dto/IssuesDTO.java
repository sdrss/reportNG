package org.uncommons.reportng.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssuesDTO {
	
	private Map<String, List<IssueDTO>> newIssues;
	private Map<String, List<IssueDTO>> fixedIssues;
	private Map<String, List<IssueDTO>> knownIssues;
	
	private int newIssuesAmount;
	private int fixedIssuesAmount;
	private int knownIssuesAmount;
	
	private Map<String, List<IssueDTO>> newFeature;
	private Map<String, List<IssueDTO>> feature;
	
	public IssuesDTO() {
		newIssues = new HashMap<String, List<IssueDTO>>();
		fixedIssues = new HashMap<String, List<IssueDTO>>();
		knownIssues = new HashMap<String, List<IssueDTO>>();
		newFeature = new HashMap<String, List<IssueDTO>>();
		feature = new HashMap<String, List<IssueDTO>>();
	}
	
	public Map<String, List<IssueDTO>> getNewIssues() {
		return newIssues;
	}
	
	public void setNewIssues(Map<String, List<IssueDTO>> newIssues) {
		this.newIssues = newIssues;
	}
	
	public Map<String, List<IssueDTO>> getFixedIssues() {
		return fixedIssues;
	}
	
	public void setFixedIssues(Map<String, List<IssueDTO>> fixedIssues) {
		this.fixedIssues = fixedIssues;
	}
	
	public Map<String, List<IssueDTO>> getKnownIssues() {
		return knownIssues;
	}
	
	public void setKnownIssues(Map<String, List<IssueDTO>> knownIssues) {
		this.knownIssues = knownIssues;
	}
	
	public int getNewIssuesAmount() {
		return newIssuesAmount;
	}
	
	public void setNewIssuesAmount(int newIssuesAmount) {
		this.newIssuesAmount = newIssuesAmount;
	}
	
	public int getFixedIssuesAmount() {
		return fixedIssuesAmount;
	}
	
	public void setFixedIssuesAmount(int fixedIssuesAmount) {
		this.fixedIssuesAmount = fixedIssuesAmount;
	}
	
	public int getKnownIssuesAmount() {
		return knownIssuesAmount;
	}
	
	public void setKnownIssuesAmount(int knownIssuesAmount) {
		this.knownIssuesAmount = knownIssuesAmount;
	}
	
	public Map<String, List<IssueDTO>> getNewFeature() {
		return newFeature;
	}
	
	public void setNewFeature(Map<String, List<IssueDTO>> newFeature) {
		this.newFeature = newFeature;
	}
	
	public Map<String, List<IssueDTO>> getFeature() {
		return feature;
	}
	
	public void setFeature(Map<String, List<IssueDTO>> feature) {
		this.feature = feature;
	}
}
