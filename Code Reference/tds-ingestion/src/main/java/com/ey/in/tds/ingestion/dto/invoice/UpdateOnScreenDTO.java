package com.ey.in.tds.ingestion.dto.invoice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class UpdateOnScreenDTO {

	private Integer id;

	private BigDecimal finalRate;

	private String invoiceAmount;

	private String finalSection;

	private BigDecimal taxableValue;

	private String actionType;

	private String reason;

	private int assessmentYear;

	private int assessmentMonth;

	private String section;
	
	private Date documentPostingDate;
	
	private BigDecimal finalAmount;
	
	private List<UpdateOnScreenDTO> data;
	
	private BigDecimal anyOtherAmount;
	
	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public int getAssessmentYear() {
		return assessmentYear;
	}

	public void setAssessmentYear(int assessmentYear) {
		this.assessmentYear = assessmentYear;
	}

	public int getAssessmentMonth() {
		return assessmentMonth;
	}

	public void setAssessmentMonth(int assessmentMonth) {
		this.assessmentMonth = assessmentMonth;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public BigDecimal getFinalRate() {
		return finalRate;
	}

	public void setFinalRate(BigDecimal finalRate) {
		this.finalRate = finalRate;
	}

	public String getInvoiceAmount() {
		return this.invoiceAmount;
	}

	public void setInvoiceAmount(String invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}

	public String getFinalSection() {
		return finalSection;
	}

	public void setFinalSection(String finalSection) {
		this.finalSection = finalSection;
	}

	public BigDecimal getTaxableValue() {
		return this.taxableValue;
	}

	public void setTaxableValue(BigDecimal taxableValue) {
		this.taxableValue = taxableValue;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Date getDocumentPostingDate() {
		return documentPostingDate;
	}

	public void setDocumentPostingDate(Date documentPostingDate) {
		this.documentPostingDate = documentPostingDate;
	}

	public List<UpdateOnScreenDTO> getData() {
		return data;
	}

	public void setData(List<UpdateOnScreenDTO> data) {
		this.data = data;
	}

	public BigDecimal getFinalAmount() {
		return finalAmount;
	}

	public void setFinalAmount(BigDecimal finalAmount) {
		this.finalAmount = finalAmount;
	}

	public BigDecimal getAnyOtherAmount() {
		return anyOtherAmount;
	}

	public void setAnyOtherAmount(BigDecimal anyOtherAmount) {
		this.anyOtherAmount = anyOtherAmount;
	}

}
