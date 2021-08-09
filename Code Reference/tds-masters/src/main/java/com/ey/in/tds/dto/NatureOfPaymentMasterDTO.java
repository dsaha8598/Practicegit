package com.ey.in.tds.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;

public class NatureOfPaymentMasterDTO implements Serializable {

	private static final long serialVersionUID = -1348707634740341269L;

	private Long id;

	@NotNull(message = "Section should not be null")
	@Size(max = 10, message = "Section should not be greter 10 character")
	private String section;

	@NotNull(message = "Nature should not be null")
	private String nature;

	@NotNull(message = "Display Value should not be null")
	@Size(max = 10, message = "Display value should not be greter 10 character")
	private String displayValue;

	@NotNull(message = "Applicable From should not be null")
	private Instant applicableFrom;

	private Instant applicableTo;

	private Boolean isSubNaturePaymentApplies;
	
	private Boolean considerDateofPayment;


	private List<SubNaturePaymentMasterDTO> subNaturePaymentMasters;

	public NatureOfPaymentMasterDTO() {
		super();
		this.section = StringUtils.EMPTY;
		this.nature = StringUtils.EMPTY;
		this.displayValue = StringUtils.EMPTY;
		this.applicableFrom = Instant.now();
	}

	public NatureOfPaymentMasterDTO(String section, String nature, String displayValue, Instant applicableFrom) {
		super();
		this.section = section;
		this.nature = nature;
		this.displayValue = displayValue;
		this.applicableFrom = applicableFrom;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public String getDisplayValue() {
		return displayValue;
	}

	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
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

	public Boolean getIsSubNaturePaymentApplies() {
		return isSubNaturePaymentApplies;
	}

	public void setIsSubNaturePaymentApplies(Boolean isSubNaturePaymentApplies) {
		this.isSubNaturePaymentApplies = isSubNaturePaymentApplies;
	}
	
	public Boolean getConsiderDateofPayment() {
		return considerDateofPayment;
	}

	public void setConsiderDateofPayment(Boolean considerDateofPayment) {
		this.considerDateofPayment = considerDateofPayment;
	}

	public List<SubNaturePaymentMasterDTO> getSubNaturePaymentMasters() {
		return subNaturePaymentMasters;
	}

	public void setSubNaturePaymentMasters(List<SubNaturePaymentMasterDTO> subNaturePaymentMasters) {
		this.subNaturePaymentMasters = subNaturePaymentMasters;
	}

	@Override
	public String toString() {
		return "NatureOfPaymentMasterDTO [id=" + id + ", section=" + section + ", nature=" + nature + ", displayValue="
				+ displayValue + ", applicableFrom=" + applicableFrom + ", applicableTo=" + applicableTo
				+ ", isSubNaturePaymentApplies=" + isSubNaturePaymentApplies + ", considerDateofPayment="
				+ considerDateofPayment + ", subNaturePaymentMasters=" + subNaturePaymentMasters + "]";
	}

	

}
