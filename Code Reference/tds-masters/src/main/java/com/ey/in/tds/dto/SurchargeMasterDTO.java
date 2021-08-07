package com.ey.in.tds.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class SurchargeMasterDTO implements Serializable {

	private static final long serialVersionUID = -4841386771707832111L;
	private Long id;
	private BigDecimal surchargeRate;
	private Instant applicableFrom;
	private Instant applicableTo;
	private boolean isSurchargeApplicable;

	private boolean bocNatureOfPayment;
	private boolean bocDeducteeStatus;
	private boolean bocInvoiceSlab;
	private boolean bocDeducteeResidentialStatus;
	private boolean bocShareholderCatagory;
    private boolean bocShareholderType;

	private List<BasisOfSurchargeDetailsDTO> basisOfSurchargeDetails;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getSurchargeRate() {
		return surchargeRate;
	}

	public void setSurchargeRate(BigDecimal surchargeRate) {
		this.surchargeRate = surchargeRate;
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

	public boolean isSurchargeApplicable() {
		return isSurchargeApplicable;
	}

	public void setSurchargeApplicable(boolean isSurchargeApplicable) {
		this.isSurchargeApplicable = isSurchargeApplicable;
	}

	public boolean isBocNatureOfPayment() {
		return bocNatureOfPayment;
	}

	public void setBocNatureOfPayment(boolean bocNatureOfPayment) {
		this.bocNatureOfPayment = bocNatureOfPayment;
	}

	public boolean isBocDeducteeStatus() {
		return bocDeducteeStatus;
	}

	public void setBocDeducteeStatus(boolean bocDeducteeStatus) {
		this.bocDeducteeStatus = bocDeducteeStatus;
	}

	public boolean isBocInvoiceSlab() {
		return bocInvoiceSlab;
	}

	public void setBocInvoiceSlab(boolean bocInvoiceSlab) {
		this.bocInvoiceSlab = bocInvoiceSlab;
	}

	public boolean isBocDeducteeResidentialStatus() {
		return bocDeducteeResidentialStatus;
	}

	public void setBocDeducteeResidentialStatus(boolean bocDeducteeResidentialStatus) {
		this.bocDeducteeResidentialStatus = bocDeducteeResidentialStatus;
	}

	public List<BasisOfSurchargeDetailsDTO> getBasisOfSurchargeDetails() {
		return basisOfSurchargeDetails;
	}

	public void setBasisOfSurchargeDetails(List<BasisOfSurchargeDetailsDTO> basisOfSurchargeDetails) {
		this.basisOfSurchargeDetails = basisOfSurchargeDetails;
	}
	
	public boolean isBocShareholderCatagory() {
		return bocShareholderCatagory;
	}

	public void setBocShareholderCatagory(boolean bocShareholderCatagory) {
		this.bocShareholderCatagory = bocShareholderCatagory;
	}

	public boolean isBocShareholderType() {
		return bocShareholderType;
	}

	public void setBocShareholderType(boolean bocShareholderType) {
		this.bocShareholderType = bocShareholderType;
	}

	@Override
	public String toString() {
		return "SurchargeMasterDTO [id=" + id + ", surchargeRate=" + surchargeRate + ", applicableFrom="
				+ applicableFrom + ", applicableTo=" + applicableTo + ", isSurchargeApplicable=" + isSurchargeApplicable
				+ ", bocNatureOfPayment=" + bocNatureOfPayment + ", bocDeducteeStatus=" + bocDeducteeStatus
				+ ", bocInvoiceSlab=" + bocInvoiceSlab + ", bocDeducteeResidentialStatus="
				+ bocDeducteeResidentialStatus + ", basisOfSurchargeDetails=" + basisOfSurchargeDetails + "]";
	}

}
