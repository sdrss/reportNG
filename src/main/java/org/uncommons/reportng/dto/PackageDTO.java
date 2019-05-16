package org.uncommons.reportng.dto;

public class PackageDTO {

	private int pass;
	private int fail;
	private int skip;
	private int known;
	private int fixed;
	private String duration;

	public PackageDTO() {

	}

	public PackageDTO(int pass, int fail, int skip, int known, int fixed, String duration) {
		setPass(pass);
		setFail(fail);
		setSkip(skip);
		setKnown(known);
		setFixed(fixed);
		setDuration(duration);
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

}
