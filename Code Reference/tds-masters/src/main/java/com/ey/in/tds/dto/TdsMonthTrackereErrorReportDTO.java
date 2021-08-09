package com.ey.in.tds.dto;

import java.io.Serializable;

public class TdsMonthTrackereErrorReportDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String year;
	private String month;
	private String dueDateForChallanPayment;
	private String monthClosureForProcessing;
	private String dueDateForFiling;
	private String applicableFrom;
	private String applicableTo;
	private String reason;
	private String serialNumber;
	
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public String getDueDateForChallanPayment() {
		return dueDateForChallanPayment;
	}
	public void setDueDateForChallanPayment(String dueDateForChallanPayment) {
		this.dueDateForChallanPayment = dueDateForChallanPayment;
	}
	public String getMonthClosureForProcessing() {
		return monthClosureForProcessing;
	}
	public void setMonthClosureForProcessing(String monthClosureForProcessing) {
		this.monthClosureForProcessing = monthClosureForProcessing;
	}
	public String getDueDateForFiling() {
		return dueDateForFiling;
	}
	public void setDueDateForFiling(String dueDateForFiling) {
		this.dueDateForFiling = dueDateForFiling;
	}
	public String getApplicableFrom() {
		return applicableFrom;
	}
	public void setApplicableFrom(String applicableFrom) {
		this.applicableFrom = applicableFrom;
	}
	public String getApplicableTo() {
		return applicableTo;
	}
	public void setApplicableTo(String applicableTo) {
		this.applicableTo = applicableTo;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

}
