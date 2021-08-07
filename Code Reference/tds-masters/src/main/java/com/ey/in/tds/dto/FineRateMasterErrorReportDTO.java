package com.ey.in.tds.dto;

import java.io.Serializable;

/**
 * 
 * @author vamsir
 *
 */
public class FineRateMasterErrorReportDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String interestType;
	private String rate;
	private String finePerDay;
	private String typeOfIntrestCalculation;
	private String applicableFrom;
	private String applicableTo;
	private String reason;
	private String serialNumber;

	public String getInterestType() {
		return interestType;
	}

	public void setInterestType(String interestType) {
		this.interestType = interestType;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getFinePerDay() {
		return finePerDay;
	}

	public void setFinePerDay(String finePerDay) {
		this.finePerDay = finePerDay;
	}

	public String getTypeOfIntrestCalculation() {
		return typeOfIntrestCalculation;
	}

	public void setTypeOfIntrestCalculation(String typeOfIntrestCalculation) {
		this.typeOfIntrestCalculation = typeOfIntrestCalculation;
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
