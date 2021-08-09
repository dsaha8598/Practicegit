package com.ey.in.tds.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class FineRateMasterDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String interestType;
	private BigDecimal rate;
	private Long finePerDay;
	private String typeOfIntrestCalculation;
	private Date applicableFrom;
	private Date applicableTo;

	public String getInterestType() {
		return interestType;
	}

	public void setInterestType(String interestType) {
		this.interestType = interestType;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public Long getFinePerDay() {
		return finePerDay;
	}

	public void setFinePerDay(Long finePerDay) {
		this.finePerDay = finePerDay;
	}

	public String getTypeOfIntrestCalculation() {
		return typeOfIntrestCalculation;
	}

	public void setTypeOfIntrestCalculation(String typeOfIntrestCalculation) {
		this.typeOfIntrestCalculation = typeOfIntrestCalculation;
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
