package com.ey.in.tds.tcs.dto;

import java.math.BigDecimal;

public class TCSBasOfSurchargeDetailsDTO {

	private Long id;
	private Long collecteeStatusId;
	private  Long collecteeResidentialStatusId;
	private Long invoiceSlabFrom;
	private Long invoiceSlabTo;
	private Long natureOfIncomeMasterId;
	private String nature;
	private String status;
	private String collecteeResidentialStatus;
	private BigDecimal rate;
	

	
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
	public String getNature() {
		return nature;
	}
	public void setNature(String nature) {
		this.nature = nature;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public BigDecimal getRate() {
		return rate;
	}
	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
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
	public Long getNatureOfIncomeMasterId() {
		return natureOfIncomeMasterId;
	}
	public void setNatureOfIncomeMasterId(Long natureOfIncomeMasterId) {
		this.natureOfIncomeMasterId = natureOfIncomeMasterId;
	}
	public String getCollecteeResidentialStatus() {
		return collecteeResidentialStatus;
	}
	public void setCollecteeResidentialStatus(String collecteeResidentialStatus) {
		this.collecteeResidentialStatus = collecteeResidentialStatus;
	}
	
	
}
