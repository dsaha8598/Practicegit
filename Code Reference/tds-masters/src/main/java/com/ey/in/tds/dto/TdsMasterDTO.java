package com.ey.in.tds.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.ey.in.tds.common.dto.DeducteeStatusDTO;

public class TdsMasterDTO implements Serializable {

	private static final long serialVersionUID = -342587080461037993L;

	private Long id;

	@NotNull(message = "Rate should not be null")
	private BigDecimal rate;

	private Boolean isPerTransactionLimitApplicable;

	private Boolean isAnnualTransactionLimitApplicable;

	private Long annualTransactionLimit;

	private Long perTransactionLimit;

	private Long deducteeStatusId;

	private List<DeducteeStatusDTO> deducteeStatus;

	private Long deducteeResidentialStatusId;

	private String residentialStatusName;

	private String statusName;

	private String saccode;

	private Boolean isSubNaturePaymentMaster;

	@NotNull(message = "Applicable from should not be null")
	private Instant applicableFrom;

	private Instant applicableTo;

	private Long natureOfPaymentId;

	private String natureOfPaymentMaster;

	private Long subNatureOfPaymentId;

	private String subNaturePaymentMaster;

	private BigDecimal rateForNoPan;

	private BigDecimal noItrRate;
	
	private BigDecimal noPanRateAndNoItrRate;
	
	public BigDecimal getRateForNoPan() {
		return rateForNoPan;
	}

	public void setRateForNoPan(BigDecimal rateForNoPan) {
		this.rateForNoPan = rateForNoPan;
	}

	public Long getDeducteeStatusId() {
		return deducteeStatusId;
	}

	public void setDeducteeStatusId(Long deducteeStatusId) {
		this.deducteeStatusId = deducteeStatusId;
	}

	public Long getAnnualTransactionLimit() {
		return annualTransactionLimit;
	}

	public void setAnnualTransactionLimit(Long annualTransactionLimit) {
		this.annualTransactionLimit = annualTransactionLimit;
	}

	public TdsMasterDTO rate(BigDecimal rate) {
		this.rate = rate;
		return this;
	}

	public String getResidentialStatusName() {
		return residentialStatusName;
	}

	public void setResidentialStatusName(String residentialStatusName) {
		this.residentialStatusName = residentialStatusName;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public String getNatureOfPaymentMaster() {
		return natureOfPaymentMaster;
	}

	public void setNatureOfPaymentMaster(String natureOfPaymentMaster) {
		this.natureOfPaymentMaster = natureOfPaymentMaster;
	}

	public String getSubNaturePaymentMaster() {
		return subNaturePaymentMaster;
	}

	public void setSubNaturePaymentMaster(String subNaturePaymentMaster) {
		this.subNaturePaymentMaster = subNaturePaymentMaster;
	}

	public List<DeducteeStatusDTO> getDeducteeStatus() {
		return deducteeStatus;
	}

	public void setDeducteeStatus(List<DeducteeStatusDTO> deducteeStatus) {
		this.deducteeStatus = deducteeStatus;
	}

	public Long getDeducteeResidentialStatusId() {
		return deducteeResidentialStatusId;
	}

	public void setDeducteeResidentialStatusId(Long deducteeResidentialStatusId) {
		this.deducteeResidentialStatusId = deducteeResidentialStatusId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public Boolean getIsPerTransactionLimitApplicable() {
		return isPerTransactionLimitApplicable;
	}

	public void setIsPerTransactionLimitApplicable(Boolean isPerTransactionLimitApplicable) {
		this.isPerTransactionLimitApplicable = isPerTransactionLimitApplicable;
	}

	public Boolean getIsAnnualTransactionLimitApplicable() {
		return isAnnualTransactionLimitApplicable;
	}

	public void setIsAnnualTransactionLimitApplicable(Boolean isAnnualTransactionLimitApplicable) {
		this.isAnnualTransactionLimitApplicable = isAnnualTransactionLimitApplicable;
	}

	public Long getPerTransactionLimit() {
		return perTransactionLimit;
	}

	public void setPerTransactionLimit(Long perTransactionLimit) {
		this.perTransactionLimit = perTransactionLimit;
	}

	public String getSaccode() {
		return saccode;
	}

	public void setSaccode(String saccode) {
		this.saccode = saccode;
	}

	public Boolean getIsSubNaturePaymentMaster() {
		return isSubNaturePaymentMaster;
	}

	public void setIsSubNaturePaymentMaster(Boolean isSubNaturePaymentMaster) {
		this.isSubNaturePaymentMaster = isSubNaturePaymentMaster;
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

	public Long getNatureOfPaymentId() {
		return natureOfPaymentId;
	}

	public void setNatureOfPaymentId(Long natureOfPaymentId) {
		this.natureOfPaymentId = natureOfPaymentId;
	}

	public Long getSubNatureOfPaymentId() {
		return subNatureOfPaymentId;
	}

	public void setSubNatureOfPaymentId(Long subNatureOfPaymentId) {
		this.subNatureOfPaymentId = subNatureOfPaymentId;
	}

	public BigDecimal getNoItrRate() {
		return noItrRate;
	}

	public void setNoItrRate(BigDecimal noItrRate) {
		this.noItrRate = noItrRate;
	}

	public BigDecimal getNoPanRateAndNoItrRate() {
		return noPanRateAndNoItrRate;
	}

	public void setNoPanRateAndNoItrRate(BigDecimal noPanRateAndNoItrRate) {
		this.noPanRateAndNoItrRate = noPanRateAndNoItrRate;
	}

	@Override
	public String toString() {
		return "TdsMasterDTO [id=" + id + ", rate=" + rate + ", isPerTransactionLimitApplicable="
				+ isPerTransactionLimitApplicable + ", isAnnualTransactionLimitApplicable="
				+ isAnnualTransactionLimitApplicable + ", annualTransactionLimit=" + annualTransactionLimit
				+ ", perTransactionLimit=" + perTransactionLimit + ", deducteeStatusId=" + deducteeStatusId
				+ ", deducteeStatus=" + deducteeStatus + ", deducteeResidentialStatusId=" + deducteeResidentialStatusId
				+ ", residentialStatusName=" + residentialStatusName + ", statusName=" + statusName + ", saccode="
				+ saccode + ", isSubNaturePaymentMaster=" + isSubNaturePaymentMaster + ", applicableFrom="
				+ applicableFrom + ", applicableTo=" + applicableTo + ", natureOfPaymentId=" + natureOfPaymentId
				+ ", natureOfPaymentMaster=" + natureOfPaymentMaster + ", subNatureOfPaymentId=" + subNatureOfPaymentId
				+ ", subNaturePaymentMaster=" + subNaturePaymentMaster + ", rateForNoPan=" + rateForNoPan
				+ ", noItrRate=" + noItrRate + ", noPanRateAndNoItrRate=" + noPanRateAndNoItrRate + "]";
	}


}
