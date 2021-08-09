package com.ey.in.tds.tcs.dto;

import java.math.BigDecimal;

public class TCSBAsisOfCessDetails {

	private Integer id;
	private Long collecteeStatusId;
	private Long collecteeResidentialStatusId;
	private String collecteeResidentialStatus;
	private Long invoiceSlabFrom;
	private Long invoiceSlabTo;
	private Long natureOfIncomeMasterId;
	private String nature;
	private String collecteeStatus;
	private BigDecimal rate;
	private Long cessTypeId;
	private String cessTypeName;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Long getCollecteeStatusId() {
		return collecteeStatusId;
	}
	public void setCollecteeStatusId(Long collecteeStatusId) {
		this.collecteeStatusId = collecteeStatusId;
	}
	public Long getCollecteeResidentialStatusId() {
		return collecteeResidentialStatusId;
	}
	public void setCollecteeResidentialStatusId(Long collecteeResidentialStatusId) {
		this.collecteeResidentialStatusId = collecteeResidentialStatusId;
	}
	public String getCollecteeResidentialStatus() {
		return collecteeResidentialStatus;
	}
	public void setCollecteeResidentialStatus(String collecteeResidentialStatus) {
		this.collecteeResidentialStatus = collecteeResidentialStatus;
	}
	public Long getInvoiceSlabFrom() {
		return invoiceSlabFrom;
	}
	public void setInvoiceSlabFrom(Long invoiceSlabFrom) {
		this.invoiceSlabFrom = invoiceSlabFrom;
	}
	public Long getInvoiceSlabTo() {
		return invoiceSlabTo;
	}
	public void setInvoiceSlabTo(Long invoiceSlabTo) {
		this.invoiceSlabTo = invoiceSlabTo;
	}
	public Long getNatureOfIncomeMasterId() {
		return natureOfIncomeMasterId;
	}
	public void setNatureOfIncomeMasterId(Long natureOfIncomeMasterId) {
		this.natureOfIncomeMasterId = natureOfIncomeMasterId;
	}
	public String getNature() {
		return nature;
	}
	public void setNature(String nature) {
		this.nature = nature;
	}
	public String getCollecteeStatus() {
		return collecteeStatus;
	}
	public void setCollecteeStatus(String collecteeStatus) {
		this.collecteeStatus = collecteeStatus;
	}
	public BigDecimal getRate() {
		return rate;
	}
	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}
	public Long getCessTypeId() {
		return cessTypeId;
	}
	public void setCessTypeId(Long cessTypeId) {
		this.cessTypeId = cessTypeId;
	}
	public String getCessTypeName() {
		return cessTypeName;
	}
	public void setCessTypeName(String cessTypeName) {
		this.cessTypeName = cessTypeName;
	}
	
}
