package com.ey.in.tds.onboarding.dto.ao;

import java.io.Serializable;
import java.math.BigDecimal;

public class AoLdcMasterDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String assessmentYear;
	private String certificateNumber;
	private String deductorName;
	private String deductorTAN;
	private String deducteeName;
	private String deducteePAN;
	private String natureOfPayment;
	private String section;
	private BigDecimal amount;
	private BigDecimal rate;
	private BigDecimal limitUtilisedAmount;

	public String getAssessmentYear() {
		return assessmentYear;
	}

	public void setAssessmentYear(String assessmentYear) {
		this.assessmentYear = assessmentYear;
	}

	public String getCertificateNumber() {
		return certificateNumber;
	}

	public void setCertificateNumber(String certificateNumber) {
		this.certificateNumber = certificateNumber;
	}

	public String getDeductorName() {
		return deductorName;
	}

	public void setDeductorName(String deductorName) {
		this.deductorName = deductorName;
	}

	public String getDeductorTAN() {
		return deductorTAN;
	}

	public void setDeductorTAN(String deductorTAN) {
		this.deductorTAN = deductorTAN;
	}

	public String getDeducteeName() {
		return deducteeName;
	}

	public void setDeducteeName(String deducteeName) {
		this.deducteeName = deducteeName;
	}

	public String getDeducteePAN() {
		return deducteePAN;
	}

	public void setDeducteePAN(String deducteePAN) {
		this.deducteePAN = deducteePAN;
	}

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

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal bigDecimal) {
		this.amount = bigDecimal;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal bigDecimal) {
		this.rate = bigDecimal;
	}

	public BigDecimal getLimitUtilisedAmount() {
		return limitUtilisedAmount;
	}

	public void setLimitUtilisedAmount(BigDecimal bigDecimal) {
		this.limitUtilisedAmount = bigDecimal;
	}

	@Override
	public String toString() {
		return "AoMasterValidationDTO [assessmentYear=" + assessmentYear + ", certificateNumber=" + certificateNumber
				+ ", deductorName=" + deductorName + ", deductorTAN=" + deductorTAN + ", deducteeName=" + deducteeName
				+ ", deducteePAN=" + deducteePAN + ", natureOfPayment=" + natureOfPayment + ", section=" + section
				+ ", amount=" + amount + ", rate=" + rate + ", limitUtilisedAmount=" + limitUtilisedAmount + "]";
	}

}
