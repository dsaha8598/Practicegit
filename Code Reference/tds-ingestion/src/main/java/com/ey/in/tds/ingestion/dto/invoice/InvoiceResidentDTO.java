package com.ey.in.tds.ingestion.dto.invoice;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class InvoiceResidentDTO implements Serializable {

	private static final long serialVersionUID = -5936173506126233833L;

	private String vendorName;

	private String invoiceNumber;

	private Date invoiceDate;

	private String pan;

	private String serviceDescription;

	private Date dateOfPaymentCreditInBookOfAccounts;

	private BigDecimal amountPaidCredited;

	private BigDecimal actualTdsDeducted;

	public BigDecimal getActualTdsDeducted() {
		return actualTdsDeducted;
	}

	public void setActualTdsDeducted(BigDecimal actualTdsDeducted) {
		this.actualTdsDeducted = actualTdsDeducted;
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

}
