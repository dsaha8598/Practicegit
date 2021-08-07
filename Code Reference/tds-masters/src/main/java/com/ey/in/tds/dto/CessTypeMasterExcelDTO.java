package com.ey.in.tds.dto;

import java.io.Serializable;
import java.util.Date;

public class CessTypeMasterExcelDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String cessType;
	private Date applicableFrom;
	private Date applicableTo;

	public String getCessType() {
		return cessType;
	}

	public void setCessType(String cessType) {
		this.cessType = cessType;
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
