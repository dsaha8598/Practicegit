package com.ey.in.tds.ingestion.dto.generalledger;

import java.util.List;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;


public class GeneralLedgerDTO {
	List<GLCategoryDTO> invoiceFoundWithNoMismatches;
	List<GLCategoryDTO> invoiceFoundWithMismatches;
	List<GLCategoryDTO> invoiceFoundWithAmountMismatches;
	List<GLCategoryDTO> invoiceFoundWithBothTypeMismatches;
	List<GLCategoryDTO> tdsNotApplicable;
	List<GLCategoryDTO> tdsApplicable;
	List<GLCategoryDTO> notAbleToDetermineWithoutAccountCodeExists;
	List<GLCategoryDTO> notAbleToDetermineWithAccountCodeExists;
	List<InvoiceLineItem> recordNotFound;

	public List<GLCategoryDTO> getInvoiceFoundWithNoMismatches() {
		return invoiceFoundWithNoMismatches;
	}

	public void setInvoiceFoundWithNoMismatches(List<GLCategoryDTO> invoiceFoundWithNoMismatches) {
		this.invoiceFoundWithNoMismatches = invoiceFoundWithNoMismatches;
	}

	public List<GLCategoryDTO> getInvoiceFoundWithMismatches() {
		return invoiceFoundWithMismatches;
	}

	public void setInvoiceFoundWithMismatches(List<GLCategoryDTO> invoiceFoundWithMismatches) {
		this.invoiceFoundWithMismatches = invoiceFoundWithMismatches;
	}

	public List<GLCategoryDTO> getInvoiceFoundWithAmountMismatches() {
		return invoiceFoundWithAmountMismatches;
	}

	public void setInvoiceFoundWithAmountMismatches(List<GLCategoryDTO> invoiceFoundWithAmountMismatches) {
		this.invoiceFoundWithAmountMismatches = invoiceFoundWithAmountMismatches;
	}

	public List<GLCategoryDTO> getInvoiceFoundWithBothTypeMismatches() {
		return invoiceFoundWithBothTypeMismatches;
	}

	public void setInvoiceFoundWithBothTypeMismatches(List<GLCategoryDTO> invoiceFoundWithBothTypeMismatches) {
		this.invoiceFoundWithBothTypeMismatches = invoiceFoundWithBothTypeMismatches;
	}

	public List<GLCategoryDTO> getTdsNotApplicable() {
		return tdsNotApplicable;
	}

	public void setTdsNotApplicable(List<GLCategoryDTO> tdsNotApplicable) {
		this.tdsNotApplicable = tdsNotApplicable;
	}

	public List<GLCategoryDTO> getTdsApplicable() {
		return tdsApplicable;
	}

	public void setTdsApplicable(List<GLCategoryDTO> tdsApplicable) {
		this.tdsApplicable = tdsApplicable;
	}

	public List<GLCategoryDTO> getNotAbleToDetermineWithoutAccountCodeExists() {
		return notAbleToDetermineWithoutAccountCodeExists;
	}

	public void setNotAbleToDetermineWithoutAccountCodeExists(
			List<GLCategoryDTO> notAbleToDetermineWithoutAccountCodeExists) {
		this.notAbleToDetermineWithoutAccountCodeExists = notAbleToDetermineWithoutAccountCodeExists;
	}

	public List<GLCategoryDTO> getNotAbleToDetermineWithAccountCodeExists() {
		return notAbleToDetermineWithAccountCodeExists;
	}

	public void setNotAbleToDetermineWithAccountCodeExists(
			List<GLCategoryDTO> notAbleToDetermineWithAccountCodeExists) {
		this.notAbleToDetermineWithAccountCodeExists = notAbleToDetermineWithAccountCodeExists;
	}

	public List<InvoiceLineItem> getRecordNotFound() {
		return recordNotFound;
	}

	public void setRecordNotFound(List<InvoiceLineItem> recordNotFound) {
		this.recordNotFound = recordNotFound;
	}

}
