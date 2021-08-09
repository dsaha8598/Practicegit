package com.ey.in.tds.dto;

import java.io.Serializable;

public class CessTypeMasterErrorRepotDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String cessType;
	private String applicableFrom;
	private String applicableTo;
	private String reason;
	private String serialNumber;

	public String getCessType() {
		return cessType;
	}

	public void setCessType(String cessType) {
		this.cessType = cessType;
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
