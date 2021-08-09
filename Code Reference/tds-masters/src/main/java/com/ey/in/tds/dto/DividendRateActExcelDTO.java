package com.ey.in.tds.dto;

import java.math.BigDecimal;
import java.util.Date;

public class DividendRateActExcelDTO {

	private String dividendDeductorType;
	private String shareholderCategory;
	private String residentialStatus;
	private BigDecimal threshHoldLimit;
	private BigDecimal applicaationRate;
	private String applicableFrom;
	private String applicableTo;
	
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
	public BigDecimal getThreshHoldLimit() {
		return threshHoldLimit;
	}
	public void setThreshHoldLimit(BigDecimal threshHoldLimit) {
		this.threshHoldLimit = threshHoldLimit;
	}
	public BigDecimal getApplicaationRate() {
		return applicaationRate;
	}
	public void setApplicaationRate(BigDecimal applicaationRate) {
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
	
	@Override
	public String toString() {
		return "DividendRateActExcelDTO [dividendDeductorType=" + dividendDeductorType + ", shareholderCategory="
				+ shareholderCategory + ", residentialStatus=" + residentialStatus + ", threshHoldLimit="
				+ threshHoldLimit + ", applicaationRate=" + applicaationRate + ", applicableFrom=" + applicableFrom
				+ ", applicableTo=" + applicableTo + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((applicaationRate == null) ? 0 : applicaationRate.hashCode());
		result = prime * result + ((applicableFrom == null) ? 0 : applicableFrom.hashCode());
		result = prime * result + ((applicableTo == null) ? 0 : applicableTo.hashCode());
		result = prime * result + ((dividendDeductorType == null) ? 0 : dividendDeductorType.hashCode());
		result = prime * result + ((residentialStatus == null) ? 0 : residentialStatus.hashCode());
		result = prime * result + ((shareholderCategory == null) ? 0 : shareholderCategory.hashCode());
		result = prime * result + ((threshHoldLimit == null) ? 0 : threshHoldLimit.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DividendRateActExcelDTO other = (DividendRateActExcelDTO) obj;
		if (applicaationRate == null) {
			if (other.applicaationRate != null)
				return false;
		} else if (!applicaationRate.equals(other.applicaationRate))
			return false;
		if (applicableFrom == null) {
			if (other.applicableFrom != null)
				return false;
		} else if (!applicableFrom.equals(other.applicableFrom))
			return false;
		if (applicableTo == null) {
			if (other.applicableTo != null)
				return false;
		} else if (!applicableTo.equals(other.applicableTo))
			return false;
		if (dividendDeductorType == null) {
			if (other.dividendDeductorType != null)
				return false;
		} else if (!dividendDeductorType.equals(other.dividendDeductorType))
			return false;
		if (residentialStatus == null) {
			if (other.residentialStatus != null)
				return false;
		} else if (!residentialStatus.equals(other.residentialStatus))
			return false;
		if (shareholderCategory == null) {
			if (other.shareholderCategory != null)
				return false;
		} else if (!shareholderCategory.equals(other.shareholderCategory))
			return false;
		if (threshHoldLimit == null) {
			if (other.threshHoldLimit != null)
				return false;
		} else if (!threshHoldLimit.equals(other.threshHoldLimit))
			return false;
		return true;
	}
	
	
	
	
	
}
