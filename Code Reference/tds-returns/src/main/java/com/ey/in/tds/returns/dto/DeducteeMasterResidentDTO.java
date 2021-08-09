package com.ey.in.tds.returns.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.ey.in.tds.common.dto.onboarding.deductee.AdditionalSectionsDTO;
import com.ey.in.tds.common.dto.onboarding.deductee.AddressDTO;

public class DeducteeMasterResidentDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UUID id;

	private String deducteeCode;

	@NotBlank(message = "Deductee name is mandatory")
	private String deducteeName;

	private String deducteeStatus;

	private String deducteeResidentialStatus;

	private BigDecimal defaultRate;

	private String deducteePAN;

	private AddressDTO address;

	@NotBlank(message = "Email is mandatory")
	private String emailAddress;

	@NotBlank(message = "Phone number is mandatory")
	private String phoneNumber;

	private String section;

	private BigDecimal rate;

	private Boolean isDeducteeHasAdditionalSections;

	private List<AdditionalSectionsDTO> additionalSections = new ArrayList<>();

	@NotNull(message = "Applicable from date is mandatory")
	private Date applicableFrom;

	private Date applicableTo;

	/*
	 * private String relatedParty;
	 * 
	 * private String deductor;
	 * 
	 * private Boolean isGrossingUp;
	 * 
	 * private Boolean isDeducteeTransparent;
	 * 
	 * public String getRelatedParty() { return relatedParty; }
	 * 
	 * public void setRelatedParty(String relatedParty) { this.relatedParty =
	 * relatedParty; }
	 * 
	 * public String getDeductor() { return deductor; }
	 * 
	 * public void setDeductor(String deductor) { this.deductor = deductor; }
	 * 
	 * public Boolean getIsGrossingUp() { return isGrossingUp; }
	 * 
	 * public void setIsGrossingUp(Boolean isGrossingUp) { this.isGrossingUp =
	 * isGrossingUp; }
	 * 
	 * public Boolean getIsDeducteeTransparent() { return isDeducteeTransparent; }
	 * 
	 * public void setIsDeducteeTransparent(Boolean isDeducteeTransparent) {
	 * this.isDeducteeTransparent = isDeducteeTransparent; }
	 */

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getDeducteeCode() {
		return deducteeCode;
	}

	public void setDeducteeCode(String deducteeCode) {
		this.deducteeCode = deducteeCode;
	}

	public String getDeducteeName() {
		return deducteeName;
	}

	public void setDeducteeName(String deducteeName) {
		this.deducteeName = deducteeName;
	}

	public BigDecimal getDefaultRate() {
		return defaultRate;
	}

	public void setDefaultRate(BigDecimal defaultRate) {
		this.defaultRate = defaultRate;
	}

	public String getDeducteePAN() {
		return deducteePAN;
	}

	public void setDeducteePAN(String deducteePAN) {
		this.deducteePAN = deducteePAN;
	}

	public AddressDTO getAddress() {
		return address;
	}

	public void setAddress(AddressDTO address) {
		this.address = address;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getDeducteeStatus() {
		return deducteeStatus;
	}

	public void setDeducteeStatus(String deducteeStatus) {
		this.deducteeStatus = deducteeStatus;
	}

	public String getDeducteeResidentialStatus() {
		return deducteeResidentialStatus;
	}

	public void setDeducteeResidentialStatus(String deducteeResidentialStatus) {
		this.deducteeResidentialStatus = deducteeResidentialStatus;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public List<AdditionalSectionsDTO> getAdditionalSections() {
		return additionalSections;
	}

	public void setAdditionalSections(List<AdditionalSectionsDTO> additionalSections) {
		this.additionalSections = additionalSections;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public Boolean getIsDeducteeHasAdditionalSections() {
		return isDeducteeHasAdditionalSections;
	}

	public void setIsDeducteeHasAdditionalSections(Boolean isDeducteeHasAdditionalSections) {
		this.isDeducteeHasAdditionalSections = isDeducteeHasAdditionalSections;
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

	@Override
	public String toString() {
		return "DeducteeMasterResidentDTO [id=" + id + ", deducteeCode=" + deducteeCode + ", deducteeName="
				+ deducteeName + ", deducteeStatus=" + deducteeStatus + ", deducteeResidentialStatus="
				+ deducteeResidentialStatus + ", defaultRate=" + defaultRate + ", deducteePAN=" + deducteePAN
				+ ", address=" + address + ", emailAddress=" + emailAddress + ", phoneNumber=" + phoneNumber
				+ ", section=" + section + ", rate=" + rate + ", isDeducteeHasAdditionalSections="
				+ isDeducteeHasAdditionalSections + ", additionalSections=" + additionalSections + ", applicableFrom="
				+ applicableFrom + ", applicableTo=" + applicableTo + "]";
	}

}
