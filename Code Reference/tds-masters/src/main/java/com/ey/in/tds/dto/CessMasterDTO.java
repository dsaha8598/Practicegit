package com.ey.in.tds.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import javax.validation.constraints.NotNull;

public class CessMasterDTO implements Serializable {

	private static final long serialVersionUID = 523325319677770997L;
	/**
	 * A DTO for the CessMaster entity.
	 */

	private Long id;
	private Boolean isCessApplicable;
	private Long cessTypeId;

	private String cessTypeName;

	private BigDecimal rate;
	private Boolean bocNatureOfPayment;
	private Boolean bocDeducteeStatus;
	private Boolean bocDeducteeResidentialStatus;
	private Boolean bocInvoiceSlab;
	private Boolean active;

	@NotNull(message = "applicable from date is mandatory")
	private Instant applicableFrom;
	private Instant applicableTo;
	private List<BasisOfCessDetailsDTO> basisOfCessDetails;

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getCessTypeName() {
		return cessTypeName;
	}

	public void setCessTypeName(String cessTypeName) {
		this.cessTypeName = cessTypeName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getIsCessApplicable() {
		return isCessApplicable;
	}

	public void setIsCessApplicable(Boolean isCessApplicable) {
		this.isCessApplicable = isCessApplicable;
	}

	public Long getCessTypeId() {
		return cessTypeId;
	}

	public void setCessTypeId(Long cessTypeId) {
		this.cessTypeId = cessTypeId;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public Boolean getBocNatureOfPayment() {
		return bocNatureOfPayment;
	}

	public void setBocNatureOfPayment(Boolean bocNatureOfPayment) {
		this.bocNatureOfPayment = bocNatureOfPayment;
	}

	public Boolean getBocDeducteeStatus() {
		return bocDeducteeStatus;
	}

	public void setBocDeducteeStatus(Boolean bocDeducteeStatus) {
		this.bocDeducteeStatus = bocDeducteeStatus;
	}

	public Boolean getBocDeducteeResidentialStatus() {
		return bocDeducteeResidentialStatus;
	}

	public void setBocDeducteeResidentialStatus(Boolean bocDeducteeResidentialStatus) {
		this.bocDeducteeResidentialStatus = bocDeducteeResidentialStatus;
	}

	public Boolean getBocInvoiceSlab() {
		return bocInvoiceSlab;
	}

	public void setBocInvoiceSlab(Boolean bocInvoiceSlab) {
		this.bocInvoiceSlab = bocInvoiceSlab;
	}

	public Instant getApplicableFrom() {
		return applicableFrom;
	}

	public void setApplicableFrom(Instant applicableFrom) {
		this.applicableFrom = applicableFrom;
	}

	public Instant getApplicableTo() {
		return applicableTo;
	}

	public void setApplicableTo(Instant applicableTo) {
		this.applicableTo = applicableTo;
	}

	public List<BasisOfCessDetailsDTO> getBasisOfCessDetails() {
		return basisOfCessDetails;
	}

	public void setBasisOfCessDetails(List<BasisOfCessDetailsDTO> basisOfCessDetails) {
		this.basisOfCessDetails = basisOfCessDetails;
	}

	@Override
	public String toString() {
		return "CessMasterDTO [id=" + id + ", isCessApplicable=" + isCessApplicable + ", cessTypeId=" + cessTypeId
				+ ", cessTypeName=" + cessTypeName + ", rate=" + rate + ", bocNatureOfPayment=" + bocNatureOfPayment
				+ ", bocDeducteeStatus=" + bocDeducteeStatus + ", bocDeducteeResidentialStatus="
				+ bocDeducteeResidentialStatus + ", bocInvoiceSlab=" + bocInvoiceSlab + ", active=" + active
				+ ", applicableFrom=" + applicableFrom + ", applicableTo=" + applicableTo + ", basisOfCessDetails="
				+ basisOfCessDetails + "]";
	}

}
