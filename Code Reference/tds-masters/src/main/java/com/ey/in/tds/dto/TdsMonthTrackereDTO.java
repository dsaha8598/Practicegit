package com.ey.in.tds.dto;

import java.io.Serializable;
import java.util.Date;

public class TdsMonthTrackereDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int year;
	private int month;
	private Date dueDateForChallanPayment;
	private Date monthClosureForProcessing;
	private Date dueDateForFiling;
	private Date applicableFrom;
	private Date applicableTo;

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public Date getDueDateForChallanPayment() {
		return dueDateForChallanPayment;
	}

	public void setDueDateForChallanPayment(Date dueDateForChallanPayment) {
		this.dueDateForChallanPayment = dueDateForChallanPayment;
	}

	public Date getMonthClosureForProcessing() {
		return monthClosureForProcessing;
	}

	public void setMonthClosureForProcessing(Date monthClosureForProcessing) {
		this.monthClosureForProcessing = monthClosureForProcessing;
	}

	public Date getDueDateForFiling() {
		return dueDateForFiling;
	}

	public void setDueDateForFiling(Date dueDateForFiling) {
		this.dueDateForFiling = dueDateForFiling;
	}

	public Date getApplicableFrom() {
		return applicableFrom;
	}

	public void setApplicableFrom(Date applicableFrom) {
		this.applicableFrom = applicableFrom;
	}

	public Date getApplicableTo() {
		return applicableTo;
	}

	public void setApplicableTo(Date applicableTo) {
		this.applicableTo = applicableTo;
	}

}
