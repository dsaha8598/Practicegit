package com.ey.in.tds.returns.domain;

import java.util.Comparator;

public class FilingChallanDetailComparator implements Comparator<FilingChallanDetailBean> {
	@Override
	public int compare(FilingChallanDetailBean filingChallan1, FilingChallanDetailBean filingChallan2) {
		// for comparison
		int sectionCompare = filingChallan1.getChallanSection().compareTo(filingChallan2.getChallanSection());
		int monthCompare = filingChallan1.getChallanMonth() - filingChallan2.getChallanMonth();

		// 2-level comparison
		if (sectionCompare == 0) {
			return ((monthCompare == 0) ? sectionCompare : monthCompare);
		} else {
			return sectionCompare;
		}
	}
}