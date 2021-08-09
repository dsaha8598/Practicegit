package com.ey.in.tds.dto;

public class NOPExcelErrorDTO {

	private String natureOfPayment;
	private String section;
	private String sacCode;
	private String deducteeStatus;
	private String residentialStatus;
	private String isAnnualTransactionLimitApplicable;
	private String annualTransactionLimit;
	private String isPerTransactionLimitApplicable;
	private String perTansactionLimitAmount;
	private String rate;
	private String applicableFrom;
	private String applicableTo;
	private String displayValue;
	private String considerDateofPayment;
	private String reason;
	private String serialNumber;
	private String noPanRate;
	private String noItrRate;
	private String noPanRateAndNoItrRate;

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

	public String getIsAnnualTransactionLimitApplicable() {
		return isAnnualTransactionLimitApplicable;
	}

	public void setIsAnnualTransactionLimitApplicable(String isAnnualTransactionLimitApplicable) {
		this.isAnnualTransactionLimitApplicable = isAnnualTransactionLimitApplicable;
	}

	public String getAnnualTransactionLimit() {
		return annualTransactionLimit;
	}

	public void setAnnualTransactionLimit(String annualTransactionLimit) {
		this.annualTransactionLimit = annualTransactionLimit;
	}

	public String getIsPerTransactionLimitApplicable() {
		return isPerTransactionLimitApplicable;
	}

	public void setIsPerTransactionLimitApplicable(String isPerTransactionLimitApplicable) {
		this.isPerTransactionLimitApplicable = isPerTransactionLimitApplicable;
	}

	public String getPerTansactionLimitAmount() {
		return perTansactionLimitAmount;
	}

	public void setPerTansactionLimitAmount(String perTansactionLimitAmount) {
		this.perTansactionLimitAmount = perTansactionLimitAmount;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
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

	public String getDisplayValue() {
		return displayValue;
	}

	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getConsiderDateofPayment() {
		return considerDateofPayment;
	}

	public void setConsiderDateofPayment(String considerDateofPayment) {
		this.considerDateofPayment = considerDateofPayment;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getNoPanRate() {
		return noPanRate;
	}

	public void setNoPanRate(String noPanRate) {
		this.noPanRate = noPanRate;
	}

	public String getNoItrRate() {
		return noItrRate;
	}

	public void setNoItrRate(String noItrRate) {
		this.noItrRate = noItrRate;
	}

	public String getNoPanRateAndNoItrRate() {
		return noPanRateAndNoItrRate;
	}

	public void setNoPanRateAndNoItrRate(String noPanRateAndNoItrRate) {
		this.noPanRateAndNoItrRate = noPanRateAndNoItrRate;
	}

	@Override
	public String toString() {
		return "NOPExcelErrorDTO [natureOfPayment=" + natureOfPayment + ", section=" + section + ", sacCode=" + sacCode
				+ ", deducteeStatus=" + deducteeStatus + ", residentialStatus=" + residentialStatus
				+ ", isAnnualTransactionLimitApplicable=" + isAnnualTransactionLimitApplicable
				+ ", annualTransactionLimit=" + annualTransactionLimit + ", isPerTransactionLimitApplicable="
				+ isPerTransactionLimitApplicable + ", perTansactionLimitAmount=" + perTansactionLimitAmount + ", rate="
				+ rate + ", applicableFrom=" + applicableFrom + ", applicableTo=" + applicableTo + ", displayValue="
				+ displayValue + ", considerDateofPayment=" + considerDateofPayment + ", reason=" + reason
				+ ", serialNumber=" + serialNumber + ", noPanRate=" + noPanRate + ", noItrRate=" + noItrRate
				+ ", noPanRateAndNoItrRate=" + noPanRateAndNoItrRate + "]";
	}


}
