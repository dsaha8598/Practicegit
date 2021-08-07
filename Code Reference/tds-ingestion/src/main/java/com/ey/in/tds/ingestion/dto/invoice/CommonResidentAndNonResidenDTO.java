package com.ey.in.tds.ingestion.dto.invoice;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public class CommonResidentAndNonResidenDTO implements Serializable {

	private static final long serialVersionUID = 4686338890725088468L;

	private String grossUpIndicator;

	private String vendorName;

	private String invoiceNumber;

	private Date invoiceDate;

	private String pan;

	private String serviceDescription;
	
	private UUID lineItemId;

	private Date dateOfPaymentCreditInBookOfAccounts;
	private BigDecimal amountPaidCredited;
	private BigDecimal actualTdsDeducted;
	
	private Integer assessmentYear;
	private Integer assessmentMonth;
	private String deductorTan;

	
	
	public Integer getAssessmentYear() {
		return assessmentYear;
	}
	public void setAssessmentYear(Integer assessmentYear) {
		this.assessmentYear = assessmentYear;
	}
	public Integer getAssessmentMonth() {
		return assessmentMonth;
	}
	public void setAssessmentMonth(Integer assessmentMonth) {
		this.assessmentMonth = assessmentMonth;
	}
	public String getDeductorTan() {
		return deductorTan;
	}
	public void setDeductorTan(String deductorTan) {
		this.deductorTan = deductorTan;
	}
	public UUID getLineItemId() {
		return lineItemId;
	}
	public void setLineItemId(UUID lineItemId) {
		this.lineItemId = lineItemId;
	}
	public CommonResidentAndNonResidenDTO() {
		super();
	}
	public String getGrossUpIndicator() {
		return grossUpIndicator;
	}
	public void setGrossUpIndicator(String grossUpIndicator) {
		this.grossUpIndicator = grossUpIndicator;
	}
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public Date getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public String getPan() {
		return pan;
	}
	public void setPan(String pan) {
		this.pan = pan;
	}
	public String getServiceDescription() {
		return serviceDescription;
	}
	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}
	public Date getDateOfPaymentCreditInBookOfAccounts() {
		return dateOfPaymentCreditInBookOfAccounts;
	}
	public void setDateOfPaymentCreditInBookOfAccounts(Date dateOfPaymentCreditInBookOfAccounts) {
		this.dateOfPaymentCreditInBookOfAccounts = dateOfPaymentCreditInBookOfAccounts;
	}
	public BigDecimal getAmountPaidCredited() {
		return amountPaidCredited;
	}
	public void setAmountPaidCredited(BigDecimal amountPaidCredited) {
		this.amountPaidCredited = amountPaidCredited;
	}
	public BigDecimal getActualTdsDeducted() {
		return actualTdsDeducted;
	}
	public void setActualTdsDeducted(BigDecimal actualTdsDeducted) {
		this.actualTdsDeducted = actualTdsDeducted;
	}
	@Override
	public String toString() {
		return "CommonResidentAndNonResidenDTO [grossUpIndicator=" + grossUpIndicator + ", vendorName=" + vendorName
				+ ", invoiceNumber=" + invoiceNumber + ", invoiceDate=" + invoiceDate + ", pan=" + pan
				+ ", serviceDescription=" + serviceDescription + ", dateOfPaymentCreditInBookOfAccounts="
				+ dateOfPaymentCreditInBookOfAccounts + ", amountPaidCredited=" + amountPaidCredited
				+ ", actualTdsDeducted=" + actualTdsDeducted + "]";
	}	

}
