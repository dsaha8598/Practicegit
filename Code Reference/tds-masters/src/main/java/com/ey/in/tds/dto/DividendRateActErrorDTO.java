package com.ey.in.tds.dto;

import java.math.BigDecimal;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import com.ey.in.tds.common.domain.dividend.DividendDeductorType;
import com.ey.in.tds.common.domain.dividend.ShareholderCategory;

public class DividendRateActErrorDTO {

	private String dividendDeductorType;
	private String shareholderCategory;
	private String residentialStatus;
	private String threshHoldLimit;
	private String applicaationRate;
	private String applicableFrom;
	private String applicableTo;
	private String  reason;
	
	public String getDividendDeductorType() {
		return dividendDeductorType;
	}
	public void setDividendDeductorType(String dividendDeductorType) {
		this.dividendDeductorType = dividendDeductorType;
	}
	public String getShareholderCategory() {
		return shareholderCategory;
	}
	public void setShareholderCategory(String shareholderCategory) {
		this.shareholderCategory = shareholderCategory;
	}
	public String getResidentialStatus() {
		return residentialStatus;
	}
	public void setResidentialStatus(String residentialStatus) {
		this.residentialStatus = residentialStatus;
	}
	public String getThreshHoldLimit() {
		return threshHoldLimit;
	}
	public void setThreshHoldLimit(String threshHoldLimit) {
		this.threshHoldLimit = threshHoldLimit;
	}
	public String getApplicaationRate() {
		return applicaationRate;
	}
	public void setApplicaationRate(String applicaationRate) {
		this.applicaationRate = applicaationRate;
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
	@Override
	public String toString() {
		return "DividendRateActExcelDTO [dividendDeductorType=" + dividendDeductorType + ", shareholderCategory="
				+ shareholderCategory + ", residentialStatus=" + residentialStatus + ", threshHoldLimit="
				+ threshHoldLimit + ", applicaationRate=" + applicaationRate + ", applicableFrom=" + applicableFrom
				+ ", applicableTo=" + applicableTo + "]";
	}
	
	

}
