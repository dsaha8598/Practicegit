package com.ey.in.tds.ingestion.web.rest.generalledger;

import com.ey.in.tds.common.repository.common.Pagination;

public class GeneralLedgerSummaryDTO {

	private Integer year;
	private Integer month;
	private String quarter;
	private Pagination pagination;

	public Pagination getPagination() {
		return pagination;
	}

	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public String getQuarter() {
		return quarter;
	}

	public void setQuarter(String quarter) {
		this.quarter = quarter;
	}

}
