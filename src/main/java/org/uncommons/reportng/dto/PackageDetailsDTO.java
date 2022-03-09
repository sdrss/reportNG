package org.uncommons.reportng.dto;

public class PackageDetailsDTO {
	
	private String className;
	private String packageName;
	private String url;
	private int pass;
	private int fail;
	private int skip;
	private int known;
	private int fixed;
	private String duration;
	private Long startMillis;
	private Long endMillis;
	
	public PackageDetailsDTO() {
		
	}
	
	public PackageDetailsDTO(String className, String packageName, int pass, int fail, int skip, int known, int fixed, String duration, String url) {
		setClassName(className);
		setPackageName(packageName);
		setPass(pass);
		setFail(fail);
		setSkip(skip);
		setKnown(known);
		setFixed(fixed);
		setDuration(duration);
		setUrl(url);
		setStartMillis(0L);
		setEndMillis(0L);
	}
	
	public int getPass() {
		return pass;
	}
	
	public void setPass(int pass) {
		this.pass = pass;
	}
	
	public int getFail() {
		return fail;
	}
	
	public void setFail(int fail) {
		this.fail = fail;
	}
	
	public int getSkip() {
		return skip;
	}
	
	public void setSkip(int skip) {
		this.skip = skip;
	}
	
	public int getKnown() {
		return known;
	}
	
	public void setKnown(int known) {
		this.known = known;
	}
	
	public int getFixed() {
		return fixed;
	}
	
	public void setFixed(int fixed) {
		this.fixed = fixed;
	}
	
	public String getDuration() {
		return duration;
	}
	
	public void setDuration(String duration) {
		this.duration = duration;
	}
	
	public String getClassΝame() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public Long getStartMillis() {
		return startMillis;
	}
	
	public void setStartMillis(Long startMillis) {
		this.startMillis = startMillis;
	}
	
	public Long getEndMillis() {
		return endMillis;
	}
	
	public void setEndMillis(Long endMillis) {
		this.endMillis = endMillis;
	}
	
}
