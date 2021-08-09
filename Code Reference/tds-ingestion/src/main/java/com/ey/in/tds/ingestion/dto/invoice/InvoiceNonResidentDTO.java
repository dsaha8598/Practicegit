package com.ey.in.tds.ingestion.dto.invoice;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class InvoiceNonResidentDTO implements Serializable {

	private static final long serialVersionUID = -99054595192282464L;

	private String grossUpIndicator;

	private String vendorName;

	private int invoiceNumber;

	private Date invoiceDate;

	private String pan;

	private String serviceDescription;

	private Date dateOfPaymentCreditInBookOfAccounts;

	private BigDecimal amountPaidCredited;

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

	public int getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(int invoiceNumber) {
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
