package com.ey.in.tds.dto;

import java.math.BigDecimal;
import java.util.Date;

public class NatureOfPaymentMasterExcelDTO {

	private String natureOfPayment;
	private String section;
	private String sacCode;
	private String deducteeStatus;
	private String residentialStatus;
	private Boolean isAnnualTransactionLimitApplicable;
	private BigDecimal annualTransactionLimit;
	private Boolean isPerTransactionLimitApplicable;
	private BigDecimal perTansactionLimitAmount;
	private BigDecimal rate;
	private Date applicableFrom;
	private Date applicableTo;
	private String displayValue;
	private String considerDateofPayment;
	private BigDecimal noPanRate;
	private BigDecimal noItrRate;
	private BigDecimal noPanRateAndNoItrRate;

	public String getNatureOfPayment() {
		return natureOfPayment;
	}

	public void setNatureOfPayment(String natureOfPayment) {
		this.natureOfPayment = natureOfPayment;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getSacCode() {
		return sacCode;
	}

	public void setSacCode(String sacCode) {
		this.sacCode = sacCode;
	}

	public String getDeducteeStatus() {
		return deducteeStatus;
	}

	public void setDeducteeStatus(String deducteeStatus) {
		this.deducteeStatus = deducteeStatus;
	}

	public String getResidentialStatus() {
		return residentialStatus;
	}

	public void setResidentialStatus(String residentialStatus) {
		this.residentialStatus = residentialStatus;
	}

	public Boolean getIsAnnualTransactionLimitApplicable() {
		return isAnnualTransactionLimitApplicable;
	}

	public void setIsAnnualTransactionLimitApplicable(Boolean isAnnualTransactionLimitApplicable) {
		this.isAnnualTransactionLimitApplicable = isAnnualTransactionLimitApplicable;
	}

	public BigDecimal getAnnualTransactionLimit() {
		return annualTransactionLimit;
	}

	public void setAnnualTransactionLimit(BigDecimal annualTransactionLimit) {
		this.annualTransactionLimit = annualTransactionLimit;
	}

	public Boolean getIsPerTransactionLimitApplicable() {
		return isPerTransactionLimitApplicable;
	}

	public void setIsPerTransactionLimitApplicable(Boolean isPerTransactionLimitApplicable) {
		this.isPerTransactionLimitApplicable = isPerTransactionLimitApplicable;
	}

	public BigDecimal getPerTansactionLimitAmount() {
		return perTansactionLimitAmount;
	}

	public void setPerTansactionLimitAmount(BigDecimal perTansactionLimitAmount) {
		this.perTansactionLimitAmount = perTansactionLimitAmount;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
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

	public String getDisplayValue() {
		return displayValue;
	}

	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}

	public String getConsiderDateofPayment() {
		return considerDateofPayment;
	}

	public void setConsiderDateofPayment(String considerDateofPayment) {
		this.considerDateofPayment = considerDateofPayment;
	}

	public BigDecimal getNoPanRate() {
		return noPanRate;
	}

	public void setNoPanRate(BigDecimal noPanRate) {
		this.noPanRate = noPanRate;
	}

	public BigDecimal getNoItrRate() {
		return noItrRate;
	}

	public void setNoItrRate(BigDecimal noItrRate) {
		this.noItrRate = noItrRate;
	}

	public BigDecimal getNoPanRateAndNoItrRate() {
		return noPanRateAndNoItrRate;
	}

	public void setNoPanRateAndNoItrRate(BigDecimal noPanRateAndNoItrRate) {
		this.noPanRateAndNoItrRate = noPanRateAndNoItrRate;
	}

	@Override
	public String toString() {
		return "NatureOfPaymentMasterExcelDTO [natureOfPayment=" + natureOfPayment + ", section=" + section
				+ ", sacCode=" + sacCode + ", deducteeStatus=" + deducteeStatus + ", residentialStatus="
				+ residentialStatus + ", isAnnualTransactionLimitApplicable=" + isAnnualTransactionLimitApplicable
				+ ", annualTransactionLimit=" + annualTransactionLimit + ", isPerTransactionLimitApplicable="
				+ isPerTransactionLimitApplicable + ", perTansactionLimitAmount=" + perTansactionLimitAmount + ", rate="
				+ rate + ", applicableFrom=" + applicableFrom + ", applicableTo=" + applicableTo + ", displayValue="
				+ displayValue + ", considerDateofPayment=" + considerDateofPayment + ", noPanRate=" + noPanRate
				+ ", noItrRate=" + noItrRate + ", noPanRateAndNoItrRate=" + noPanRateAndNoItrRate
				+ ", getNatureOfPayment()=" + getNatureOfPayment() + ", getSection()=" + getSection()
				+ ", getSacCode()=" + getSacCode() + ", getDeducteeStatus()=" + getDeducteeStatus()
				+ ", getResidentialStatus()=" + getResidentialStatus() + ", getIsAnnualTransactionLimitApplicable()="
				+ getIsAnnualTransactionLimitApplicable() + ", getAnnualTransactionLimit()="
				+ getAnnualTransactionLimit() + ", getIsPerTransactionLimitApplicable()="
				+ getIsPerTransactionLimitApplicable() + ", getPerTansactionLimitAmount()="
				+ getPerTansactionLimitAmount() + ", getRate()=" + getRate() + ", getApplicableFrom()="
				+ getApplicableFrom() + ", getApplicableTo()=" + getApplicableTo() + ", getDisplayValue()="
				+ getDisplayValue() + ", getConsiderDateofPayment()=" + getConsiderDateofPayment() + ", getNoPanRate()="
				+ getNoPanRate() + ", getNoItrRate()=" + getNoItrRate() + ", getNoPanRateAndNoItrRate()="
				+ getNoPanRateAndNoItrRate() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}

	
}
