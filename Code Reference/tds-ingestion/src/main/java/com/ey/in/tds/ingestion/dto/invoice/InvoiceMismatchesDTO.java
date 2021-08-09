package com.ey.in.tds.ingestion.dto.invoice;

public class InvoiceMismatchesDTO {

	private Integer assessmentYear;
	private String deductorTan;
	private Integer invoiceLineItemId;
	private String documentPostingDate;

	public String getDocumentPostingDate() {
		return documentPostingDate;
	}

	public void setDocumentPostingDate(String documentPostingDate) {
		this.documentPostingDate = documentPostingDate;
	}

	public Integer getAssessmentYear() {
		return assessmentYear;
	}

	public void setAssessmentYear(Integer assessmentYear) {
		this.assessmentYear = assessmentYear;
	}

	public String getDeductorTan() {
		return deductorTan;
	}

	public void setDeductorTan(String deductorTan) {
		this.deductorTan = deductorTan;
	}

	public Integer getInvoiceLineItemId() {
		return invoiceLineItemId;
	}

	public void setInvoiceLineItemId(Integer invoiceLineItemId) {
		this.invoiceLineItemId = invoiceLineItemId;
	}

}
