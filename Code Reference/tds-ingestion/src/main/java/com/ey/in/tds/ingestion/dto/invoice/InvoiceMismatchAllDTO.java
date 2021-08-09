package com.ey.in.tds.ingestion.dto.invoice;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class InvoiceMismatchAllDTO implements Serializable {

	private static final long serialVersionUID = 6319843633641579730L;
	private BigDecimal excessDeduction;
	private UUID id;
	private BigDecimal invoiceValue;
	private String mismatchcategory;
	private BigDecimal shortDeduction;
	private BigDecimal tdsClientAmount;

	private BigDecimal tdsSystemAmount;

	private BigDecimal totalRecords;

	public BigDecimal getExcessDeduction() {
		return excessDeduction;
	}

	public UUID getId() {
		return id;
	}

	public BigDecimal getInvoiceValue() {
		return invoiceValue;
	}

	public String getMismatchcategory() {
		return mismatchcategory;
	}

	public BigDecimal getShortDeduction() {
		return shortDeduction;
	}

	public BigDecimal getTdsClientAmount() {
		return tdsClientAmount;
	}

	public BigDecimal getTdsSystemAmount() {
		return tdsSystemAmount;
	}

	public BigDecimal getTotalRecords() {
		return totalRecords;
	}

	public void setExcessDeduction(BigDecimal excessDeduction) {
		this.excessDeduction = excessDeduction;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setInvoiceValue(BigDecimal invoiceValue) {
		this.invoiceValue = invoiceValue;
	}

	public void setMismatchcategory(String mismatchcategory) {
		this.mismatchcategory = mismatchcategory;
	}

	public void setShortDeduction(BigDecimal shortDeduction) {
		this.shortDeduction = shortDeduction;
	}

	public void setTdsClientAmount(BigDecimal tdsClientAmount) {
		this.tdsClientAmount = tdsClientAmount;
	}

	public void setTdsSystemAmount(BigDecimal tdsSystemAmount) {
		this.tdsSystemAmount = tdsSystemAmount;
	}

	public void setTotalRecords(BigDecimal totalRecords) {
		this.totalRecords = totalRecords;
	}

}
