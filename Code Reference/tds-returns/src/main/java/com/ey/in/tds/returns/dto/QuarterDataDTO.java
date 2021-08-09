package com.ey.in.tds.returns.dto;

import java.util.Date;

public class QuarterDataDTO {

	private Date filingStatusDuedateForFiling;
	private Date filingStatusQuarterEndDate;
	private Date filingStatusQuarterStartDate;

	public Date getFilingStatusDuedateForFiling() {
		return filingStatusDuedateForFiling;
	}

	public void setFilingStatusDuedateForFiling(Date filingStatusDuedateForFiling) {
		this.filingStatusDuedateForFiling = filingStatusDuedateForFiling;
	}

	public Date getFilingStatusQuarterEndDate() {
		return filingStatusQuarterEndDate;
	}

	public void setFilingStatusQuarterEndDate(Date filingStatusQuarterEndDate) {
		this.filingStatusQuarterEndDate = filingStatusQuarterEndDate;
	}

	public Date getFilingStatusQuarterStartDate() {
		return filingStatusQuarterStartDate;
	}

	public void setFilingStatusQuarterStartDate(Date filingStatusQuarterStartDate) {
		this.filingStatusQuarterStartDate = filingStatusQuarterStartDate;
	}

	@Override
	public String toString() {
		return "QuarterDataDTO [filingStatusDuedateForFiling=" + filingStatusDuedateForFiling
				+ ", filingStatusQuarterEndDate=" + filingStatusQuarterEndDate + ", filingStatusQuarterStartDate="
				+ filingStatusQuarterStartDate + "]";
	}

}
