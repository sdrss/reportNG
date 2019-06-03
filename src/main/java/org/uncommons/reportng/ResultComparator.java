package org.uncommons.reportng;

import java.util.Comparator;

import org.uncommons.reportng.dto.PackageDetailsDTO;

class ResultComparator implements Comparator<PackageDetailsDTO> {
	@Override
	public int compare(PackageDetailsDTO packageDetails1, PackageDetailsDTO packageDetails2) {
		return packageDetails1.getPackageName().compareTo(packageDetails2.getPackageName());
	}
}