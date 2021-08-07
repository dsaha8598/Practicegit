package com.ey.in.tds.ingestion.dto.invoice;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class InvoiceMismatchUpdateDTO implements Serializable {

	private static final long serialVersionUID = -5207230908898254093L;
	private String actionReason;
	private String actionTaken;
	private String deducteePan;
	private String deductorTan;
	private BigDecimal finalTdsAmount;
	private BigDecimal finalTdsRate;
	private String finalTdsSection;
	private String hashCode;
	private UUID id;
	private UUID lineItemId;
	private String mismatchDeductionType;
	private String mismatchInterpretation;
	private String mismatchRate;
	private String mismatchSection;
	private String nameOfTheDeductee;
	private BigDecimal sectionsTdsRate;
	private String sectionsTdsSection;
	private String sequenceNumber;
	private String serviceDescriptionGlText;
	private String serviceDescriptionInvoice;
	private String serviceDescriptionPo;
	private BigDecimal toolTdsRate;

	private String toolTdsSection;

	public InvoiceMismatchUpdateDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getActionReason() {
		return actionReason;
	}

	public String getActionTaken() {
		return actionTaken;
	}

	public String getDeducteePan() {
		return deducteePan;
	}

	public String getDeductorTan() {
		return deductorTan;
	}

	public BigDecimal getFinalTdsAmount() {
		return finalTdsAmount;
	}

	public BigDecimal getFinalTdsRate() {
		return finalTdsRate;
	}

	public String getFinalTdsSection() {
		return finalTdsSection;
	}

	public String getHashCode() {
		return hashCode;
	}

	public UUID getId() {
		return id;
	}

	public UUID getLineItemId() {
		return lineItemId;
	}

	public String getMismatchDeductionType() {
		return mismatchDeductionType;
	}

	public String getMismatchInterpretation() {
		return mismatchInterpretation;
	}

	public String getMismatchRate() {
		return mismatchRate;
	}

	public String getMismatchSection() {
		return mismatchSection;
	}

	public String getNameOfTheDeductee() {
		return nameOfTheDeductee;
	}

	public BigDecimal getSectionsTdsRate() {
		return sectionsTdsRate;
	}

	public String getSectionsTdsSection() {
		return sectionsTdsSection;
	}

	public String getSequenceNumber() {
		return sequenceNumber;
	}

	public String getServiceDescriptionGlText() {
		return serviceDescriptionGlText;
	}

	public String getServiceDescriptionInvoice() {
		return serviceDescriptionInvoice;
	}

	public String getServiceDescriptionPo() {
		return serviceDescriptionPo;
	}

	public BigDecimal getToolTdsRate() {
		return toolTdsRate;
	}

	public String getToolTdsSection() {
		return toolTdsSection;
	}

	public void setActionReason(String actionReason) {
		this.actionReason = actionReason;
	}

	public void setActionTaken(String actionTaken) {
		this.actionTaken = actionTaken;
	}

	public void setDeducteePan(String deducteePan) {
		this.deducteePan = deducteePan;
	}

	public void setDeductorTan(String deductorTan) {
		this.deductorTan = deductorTan;
	}

	public void setFinalTdsAmount(BigDecimal finalTdsAmount) {
		this.finalTdsAmount = finalTdsAmount;
	}

	public void setFinalTdsRate(BigDecimal finalTdsRate) {
		this.finalTdsRate = finalTdsRate;
	}

	public void setFinalTdsSection(String finalTdsSection) {
		this.finalTdsSection = finalTdsSection;
	}

	public void setHashCode(String hashCode) {
		this.hashCode = hashCode;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setLineItemId(UUID lineItemId) {
		this.lineItemId = lineItemId;
	}

	public void setMismatchDeductionType(String mismatchDeductionType) {
		this.mismatchDeductionType = mismatchDeductionType;
	}

	public void setMismatchInterpretation(String mismatchInterpretation) {
		this.mismatchInterpretation = mismatchInterpretation;
	}

	public void setMismatchRate(String mismatchRate) {
		this.mismatchRate = mismatchRate;
	}

	public void setMismatchSection(String mismatchSection) {
		this.mismatchSection = mismatchSection;
	}

	public void setNameOfTheDeductee(String nameOfTheDeductee) {
		this.nameOfTheDeductee = nameOfTheDeductee;
	}

	public void setSectionsTdsRate(BigDecimal sectionsTdsRate) {
		this.sectionsTdsRate = sectionsTdsRate;
	}

	public void setSectionsTdsSection(String sectionsTdsSection) {
		this.sectionsTdsSection = sectionsTdsSection;
	}

	public void setSequenceNumber(String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public void setServiceDescriptionGlText(String serviceDescriptionGlText) {
		this.serviceDescriptionGlText = serviceDescriptionGlText;
	}

	public void setServiceDescriptionInvoice(String serviceDescriptionInvoice) {
		this.serviceDescriptionInvoice = serviceDescriptionInvoice;
	}

	public void setServiceDescriptionPo(String serviceDescriptionPo) {
		this.serviceDescriptionPo = serviceDescriptionPo;
	}

	public void setToolTdsRate(BigDecimal toolTdsRate) {
		this.toolTdsRate = toolTdsRate;
	}

	public void setToolTdsSection(String toolTdsSection) {
		this.toolTdsSection = toolTdsSection;
	}

}
