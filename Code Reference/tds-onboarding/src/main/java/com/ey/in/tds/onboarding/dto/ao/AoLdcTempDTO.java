package com.ey.in.tds.onboarding.dto.ao;

import java.io.Serializable;
import java.util.UUID;

public class AoLdcTempDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UUID id;
	private String certificateNumber;
	private String tanOrpan;
	private String tanOrPanName;
	private String section;
	private String amount;
	private String certificateRate;
	private String validFromdate;
	private String validTillDate;

	public AoLdcTempDTO() {
		super();
	}

	public AoLdcTempDTO(UUID id, String certificateNumber, String tanOrpan, String tanOrPanName, String section,
			String amount, String certificateRate, String validFromdate, String validTillDate) {
		this.id = id;
		this.certificateNumber = certificateNumber;
		this.tanOrpan = tanOrpan;
		this.tanOrPanName = tanOrPanName;
		this.section = section;
		this.amount = amount;
		this.certificateRate = certificateRate;
		this.validFromdate = validFromdate;
		this.validTillDate = validTillDate;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

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
		return "AoLdcValidationDTO [id=" + id + ", certificateNumber=" + certificateNumber + ", tanOrpan=" + tanOrpan
				+ ", tanOrPanName=" + tanOrPanName + ", section=" + section + ", amount=" + amount
				+ ", certificateRate=" + certificateRate + ", validFromdate=" + validFromdate + ", validTillDate="
				+ validTillDate + "]";
	}
}
