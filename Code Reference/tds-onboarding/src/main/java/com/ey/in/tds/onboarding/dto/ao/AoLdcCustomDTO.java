package com.ey.in.tds.onboarding.dto.ao;

import java.io.Serializable;
import java.util.UUID;

public class AoLdcCustomDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int assessmentYear;
	private int assessmentMonth;
	private String uploadType;
	private String status;
	private String sha256sum;
	private UUID id;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public int getAssessmentYear() {
		return assessmentYear;
	}

	public void setAssessmentYear(int assessmentYear) {
		this.assessmentYear = assessmentYear;
	}

	public int getAssessmentMonth() {
		return assessmentMonth;
	}

	public void setAssessmentMonth(int assessmentMonth) {
		this.assessmentMonth = assessmentMonth;
	}

	public String getUploadType() {
		return uploadType;
	}

	public void setUploadType(String uploadType) {
		this.uploadType = uploadType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSha256sum() {
		return sha256sum;
	}

	public void setSha256sum(String sha256sum) {
		this.sha256sum = sha256sum;
	}

	@Override
	public String toString() {
		return "AoLdcCustomDTO [assessmentYear=" + assessmentYear + ", assessmentMonth=" + assessmentMonth
				+ ", uploadType=" + uploadType + ", status=" + status + ", sha256sum=" + sha256sum + ", id=" + id + "]";
	}
}
