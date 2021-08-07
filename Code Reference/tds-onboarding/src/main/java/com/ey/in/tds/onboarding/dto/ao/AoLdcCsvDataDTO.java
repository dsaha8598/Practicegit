package com.ey.in.tds.onboarding.dto.ao;

import java.io.Serializable;

public class AoLdcCsvDataDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String certificateNumber;
	private String tanOrpan;
	private String tanOrPanName;
	private String section;
	private String amount;
	private String certificateRate;
	private String validFromdate;
	private String validTillDate;

	public String getCertificateNumber() {
		return certificateNumber;
	}

	public void setCertificateNumber(String certificateNumber) {
		this.certificateNumber = certificateNumber;
	}

	public String getTanOrpan() {
		return tanOrpan;
	}

	public void setTanOrpan(String tanOrpan) {
		this.tanOrpan = tanOrpan;
	}

	public String getTanOrPanName() {
		return tanOrPanName;
	}

	public void setTanOrPanName(String tanOrPanName) {
		this.tanOrPanName = tanOrPanName;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCertificateRate() {
		return certificateRate;
	}

	public void setCertificateRate(String certificateRate) {
		this.certificateRate = certificateRate;
	}

	public String getValidFromdate() {
		return validFromdate;
	}

	public void setValidFromdate(String validFromdate) {
		this.validFromdate = validFromdate;
	}

	public String getValidTillDate() {
		return validTillDate;
	}

	public void setValidTillDate(String validTillDate) {
		this.validTillDate = validTillDate;
	}

	@Override
	public String toString() {
		return "AoLdcCsvDataDTO [certificateNumber=" + certificateNumber + ", tanOrpan=" + tanOrpan + ", tanOrPanName="
				+ tanOrPanName + ", section=" + section + ", amount=" + amount + ", certificateRate=" + certificateRate
				+ ", validFromdate=" + validFromdate + ", validTillDate=" + validTillDate + "]";
	}
}
