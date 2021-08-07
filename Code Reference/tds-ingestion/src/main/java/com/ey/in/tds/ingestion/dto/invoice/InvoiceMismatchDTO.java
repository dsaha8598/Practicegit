package com.ey.in.tds.ingestion.dto.invoice;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public class InvoiceMismatchDTO implements Serializable {

	private static final long serialVersionUID = 2150145308205837938L;

	private String actionReason;
	private String actionTaken;
	private UUID batchId;
	private String deducteePan;
	private String deductorTan;
	private BigDecimal finalTdsAmount;
	private BigDecimal finalTdsRate;
	private String finalTdsSection;
	private Integer lineItemId;
	private String mismatchDeductionType;
	private String mismatchInterpretation;
	private BigDecimal mismatchRate;
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
	private UUID batchUploadId;
	private Date accountingVoucherDate;
	private String accountingVoucherNumber;
	private Boolean active;
	private BigDecimal clientAmount;
	private BigDecimal clientRate;
	private String clientSection;
	private Date advanceAppliedOn;
	private Boolean isAdvanceSplitRecord;
	private BigDecimal amountPaidCredited;
	private BigDecimal cessAmount;
	private BigDecimal cessRate;
	private BigDecimal cgstAmount;
	private BigDecimal cgstRate;
	private String companyCode;
	private String companyName;
	private String createdBy;
	private Date createdDate;
	private String creditDebitNote;
	private Boolean creditable;
	private Date dateAtWhichTdsIsDeposited;
	private Date dateOnWhichTdsIsDeducted;
	private String deducteeAddress;
	private String deducteeName;
	private String deducteeTin;
	private String deductorCode;
	private BigDecimal deductorGstin;
	private BigDecimal derivedTdsAmount;
	private BigDecimal derivedTdsRate;
	private String derivedTdsSection;
	private Date documentDate;
	private String documentNumber;
	private Date documentPostingDate;
	private String documentType;
	private Boolean exists;
//	private BigDecimal finalTdsAmount;
//	private BigDecimal finalTdsRate;
//	private String finalTdsSection;
	private String glAccountCode;
	private String grossIndicator;
	private BigDecimal gstin;
	private Boolean hasAdvance;
	private Boolean hasLdc;
	private Boolean isMismatch;
	private Boolean hasProvision;
	private String hsnSacCode;
	private BigDecimal igstAmount;
	private BigDecimal igstRate;
	private BigDecimal interest;
	private BigDecimal invoiceAmount;
	private Boolean isAmendment;
	private Boolean isLdcSplitRecord;
	private Boolean isMergeRecord;
	private Boolean isProvisionSplitRecord;
	private Boolean isSplitRecord;
	private Date ldcAppliedOn;
	private Integer lineItemNumber;
	private String linkedAdvanceNumber;
	private BigDecimal migoNumber;
	private BigDecimal miroNumber;
	private String mismatchCategory;
//	private String mismatchInterpretation;
	private Date mismatchModifiedDate;
	private String modifiedBy;
	private Date modifiedDate;
	private Date originalDocumentDate;
	private String originalDocumentNumber;
	private String pan;
	private BigDecimal penalty;
	private Date poDate;
	private String poNumber;
	private String pos;
	private String processedFrom;
	private Date provisionAppliedOn;
	private Long qtySupplied;
	private String finalReason;
	private String isResident;
	private String sacDecription;
	private String section;
//	private String sequenceNumber;
	private String serviceDescriptionGl;
//	private String serviceDescriptionInvoice;
//	private String serviceDescriptionPo;
	private BigDecimal sgstAmount;
	private BigDecimal sgstRate;
	private String sourceFileName;
	private String sourceIdentifier;
	private BigDecimal surcharge;
	private BigDecimal tdsAmount;
	private BigDecimal tdsDeducted;
	private BigDecimal tdsRate;
	private String tdsSection;
	private Integer thresholdLimit;
	private String invoiceType;
	private String unitOfMeasurement;
	private String userDefinedField1;
	private String userDefinedField2;
	private String userDefinedField3;
	private BigDecimal vendorInvoiceNumber;

	public String getActionReason() {
		return actionReason;
	}

	public void setActionReason(String actionReason) {
		this.actionReason = actionReason;
	}

	public String getActionTaken() {
		return actionTaken;
	}

	public void setActionTaken(String actionTaken) {
		this.actionTaken = actionTaken;
	}

	public UUID getBatchId() {
		return batchId;
	}

	public void setBatchId(UUID batchId) {
		this.batchId = batchId;
	}

	public String getDeducteePan() {
		return deducteePan;
	}

	public void setDeducteePan(String deducteePan) {
		this.deducteePan = deducteePan;
	}

	public String getDeductorTan() {
		return deductorTan;
	}

	public void setDeductorTan(String deductorTan) {
		this.deductorTan = deductorTan;
	}

	public BigDecimal getFinalTdsAmount() {
		return finalTdsAmount;
	}

	public void setFinalTdsAmount(BigDecimal finalTdsAmount) {
		this.finalTdsAmount = finalTdsAmount;
	}

	public BigDecimal getFinalTdsRate() {
		return finalTdsRate;
	}

	public void setFinalTdsRate(BigDecimal finalTdsRate) {
		this.finalTdsRate = finalTdsRate;
	}

	public String getFinalTdsSection() {
		return finalTdsSection;
	}

	public void setFinalTdsSection(String finalTdsSection) {
		this.finalTdsSection = finalTdsSection;
	}

	public Integer getLineItemId() {
		return lineItemId;
	}

	public void setLineItemId(Integer lineItemId) {
		this.lineItemId = lineItemId;
	}

	public String getMismatchDeductionType() {
		return mismatchDeductionType;
	}

	public void setMismatchDeductionType(String mismatchDeductionType) {
		this.mismatchDeductionType = mismatchDeductionType;
	}

	public String getMismatchInterpretation() {
		return mismatchInterpretation;
	}

	public void setMismatchInterpretation(String mismatchInterpretation) {
		this.mismatchInterpretation = mismatchInterpretation;
	}

	public BigDecimal getMismatchRate() {
		return mismatchRate;
	}

	public void setMismatchRate(BigDecimal mismatchRate) {
		this.mismatchRate = mismatchRate;
	}

	public String getMismatchSection() {
		return mismatchSection;
	}

	public void setMismatchSection(String mismatchSection) {
		this.mismatchSection = mismatchSection;
	}

	public String getNameOfTheDeductee() {
		return nameOfTheDeductee;
	}

	public void setNameOfTheDeductee(String nameOfTheDeductee) {
		this.nameOfTheDeductee = nameOfTheDeductee;
	}

	public BigDecimal getSectionsTdsRate() {
		return sectionsTdsRate;
	}

	public void setSectionsTdsRate(BigDecimal sectionsTdsRate) {
		this.sectionsTdsRate = sectionsTdsRate;
	}

	public String getSectionsTdsSection() {
		return sectionsTdsSection;
	}

	public void setSectionsTdsSection(String sectionsTdsSection) {
		this.sectionsTdsSection = sectionsTdsSection;
	}

	public String getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public String getServiceDescriptionGlText() {
		return serviceDescriptionGlText;
	}

	public void setServiceDescriptionGlText(String serviceDescriptionGlText) {
		this.serviceDescriptionGlText = serviceDescriptionGlText;
	}

	public String getServiceDescriptionInvoice() {
		return serviceDescriptionInvoice;
	}

	public void setServiceDescriptionInvoice(String serviceDescriptionInvoice) {
		this.serviceDescriptionInvoice = serviceDescriptionInvoice;
	}

	public String getServiceDescriptionPo() {
		return serviceDescriptionPo;
	}

	public void setServiceDescriptionPo(String serviceDescriptionPo) {
		this.serviceDescriptionPo = serviceDescriptionPo;
	}

	public BigDecimal getToolTdsRate() {
		return toolTdsRate;
	}

	public void setToolTdsRate(BigDecimal toolTdsRate) {
		this.toolTdsRate = toolTdsRate;
	}

	public String getToolTdsSection() {
		return toolTdsSection;
	}

	public void setToolTdsSection(String toolTdsSection) {
		this.toolTdsSection = toolTdsSection;
	}

	public UUID getBatchUploadId() {
		return batchUploadId;
	}

	public void setBatchUploadId(UUID batchUploadId) {
		this.batchUploadId = batchUploadId;
	}

	public Date getAccountingVoucherDate() {
		return accountingVoucherDate;
	}

	public void setAccountingVoucherDate(Date accountingVoucherDate) {
		this.accountingVoucherDate = accountingVoucherDate;
	}

	public String getAccountingVoucherNumber() {
		return accountingVoucherNumber;
	}

	public void setAccountingVoucherNumber(String accountingVoucherNumber) {
		this.accountingVoucherNumber = accountingVoucherNumber;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public BigDecimal getClientAmount() {
		return clientAmount;
	}

	public void setClientAmount(BigDecimal clientAmount) {
		this.clientAmount = clientAmount;
	}

	public BigDecimal getClientRate() {
		return clientRate;
	}

	public void setClientRate(BigDecimal clientRate) {
		this.clientRate = clientRate;
	}

	public String getClientSection() {
		return clientSection;
	}

	public void setClientSection(String clientSection) {
		this.clientSection = clientSection;
	}

	public Date getAdvanceAppliedOn() {
		return advanceAppliedOn;
	}

	public void setAdvanceAppliedOn(Date advanceAppliedOn) {
		this.advanceAppliedOn = advanceAppliedOn;
	}

	public Boolean getIsAdvanceSplitRecord() {
		return isAdvanceSplitRecord;
	}

	public void setIsAdvanceSplitRecord(Boolean isAdvanceSplitRecord) {
		this.isAdvanceSplitRecord = isAdvanceSplitRecord;
	}

	public BigDecimal getAmountPaidCredited() {
		return amountPaidCredited;
	}

	public void setAmountPaidCredited(BigDecimal amountPaidCredited) {
		this.amountPaidCredited = amountPaidCredited;
	}

	public BigDecimal getCessAmount() {
		return cessAmount;
	}

	public void setCessAmount(BigDecimal cessAmount) {
		this.cessAmount = cessAmount;
	}

	public BigDecimal getCessRate() {
		return cessRate;
	}

	public void setCessRate(BigDecimal cessRate) {
		this.cessRate = cessRate;
	}

	public BigDecimal getCgstAmount() {
		return cgstAmount;
	}

	public void setCgstAmount(BigDecimal cgstAmount) {
		this.cgstAmount = cgstAmount;
	}

	public BigDecimal getCgstRate() {
		return cgstRate;
	}

	public void setCgstRate(BigDecimal cgstRate) {
		this.cgstRate = cgstRate;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getCreditDebitNote() {
		return creditDebitNote;
	}

	public void setCreditDebitNote(String creditDebitNote) {
		this.creditDebitNote = creditDebitNote;
	}

	public Boolean getCreditable() {
		return creditable;
	}

	public void setCreditable(Boolean creditable) {
		this.creditable = creditable;
	}

	public Date getDateAtWhichTdsIsDeposited() {
		return dateAtWhichTdsIsDeposited;
	}

	public void setDateAtWhichTdsIsDeposited(Date dateAtWhichTdsIsDeposited) {
		this.dateAtWhichTdsIsDeposited = dateAtWhichTdsIsDeposited;
	}

	public Date getDateOnWhichTdsIsDeducted() {
		return dateOnWhichTdsIsDeducted;
	}

	public void setDateOnWhichTdsIsDeducted(Date dateOnWhichTdsIsDeducted) {
		this.dateOnWhichTdsIsDeducted = dateOnWhichTdsIsDeducted;
	}

	public String getDeducteeAddress() {
		return deducteeAddress;
	}

	public void setDeducteeAddress(String deducteeAddress) {
		this.deducteeAddress = deducteeAddress;
	}

	public String getDeducteeName() {
		return deducteeName;
	}

	public void setDeducteeName(String deducteeName) {
		this.deducteeName = deducteeName;
	}

	public String getDeducteeTin() {
		return deducteeTin;
	}

	public void setDeducteeTin(String deducteeTin) {
		this.deducteeTin = deducteeTin;
	}

	public String getDeductorCode() {
		return deductorCode;
	}

	public void setDeductorCode(String deductorCode) {
		this.deductorCode = deductorCode;
	}

	public BigDecimal getDeductorGstin() {
		return deductorGstin;
	}

	public void setDeductorGstin(BigDecimal deductorGstin) {
		this.deductorGstin = deductorGstin;
	}

	public BigDecimal getDerivedTdsAmount() {
		return derivedTdsAmount;
	}

	public void setDerivedTdsAmount(BigDecimal derivedTdsAmount) {
		this.derivedTdsAmount = derivedTdsAmount;
	}

	public BigDecimal getDerivedTdsRate() {
		return derivedTdsRate;
	}

	public void setDerivedTdsRate(BigDecimal derivedTdsRate) {
		this.derivedTdsRate = derivedTdsRate;
	}

	public String getDerivedTdsSection() {
		return derivedTdsSection;
	}

	public void setDerivedTdsSection(String derivedTdsSection) {
		this.derivedTdsSection = derivedTdsSection;
	}

	public Date getDocumentDate() {
		return documentDate;
	}

	public void setDocumentDate(Date documentDate) {
		this.documentDate = documentDate;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public Date getDocumentPostingDate() {
		return documentPostingDate;
	}

	public void setDocumentPostingDate(Date documentPostingDate) {
		this.documentPostingDate = documentPostingDate;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public Boolean getExists() {
		return exists;
	}

	public void setExists(Boolean exists) {
		this.exists = exists;
	}

	public String getGlAccountCode() {
		return glAccountCode;
	}

	public void setGlAccountCode(String glAccountCode) {
		this.glAccountCode = glAccountCode;
	}

	public String getGrossIndicator() {
		return grossIndicator;
	}

	public void setGrossIndicator(String grossIndicator) {
		this.grossIndicator = grossIndicator;
	}

	public BigDecimal getGstin() {
		return gstin;
	}

	public void setGstin(BigDecimal gstin) {
		this.gstin = gstin;
	}

	public Boolean getHasAdvance() {
		return hasAdvance;
	}

	public void setHasAdvance(Boolean hasAdvance) {
		this.hasAdvance = hasAdvance;
	}

	public Boolean getHasLdc() {
		return hasLdc;
	}

	public void setHasLdc(Boolean hasLdc) {
		this.hasLdc = hasLdc;
	}

	public Boolean getIsMismatch() {
		return isMismatch;
	}

	public void setIsMismatch(Boolean isMismatch) {
		this.isMismatch = isMismatch;
	}

	public Boolean getHasProvision() {
		return hasProvision;
	}

	public void setHasProvision(Boolean hasProvision) {
		this.hasProvision = hasProvision;
	}

	public String getHsnSacCode() {
		return hsnSacCode;
	}

	public void setHsnSacCode(String hsnSacCode) {
		this.hsnSacCode = hsnSacCode;
	}

	public BigDecimal getIgstAmount() {
		return igstAmount;
	}

	public void setIgstAmount(BigDecimal igstAmount) {
		this.igstAmount = igstAmount;
	}

	public BigDecimal getIgstRate() {
		return igstRate;
	}

	public void setIgstRate(BigDecimal igstRate) {
		this.igstRate = igstRate;
	}

	public BigDecimal getInterest() {
		return interest;
	}

	public void setInterest(BigDecimal interest) {
		this.interest = interest;
	}

	public BigDecimal getInvoiceAmount() {
		return invoiceAmount;
	}

	public void setInvoiceAmount(BigDecimal invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}

	public Boolean getIsAmendment() {
		return isAmendment;
	}

	public void setIsAmendment(Boolean isAmendment) {
		this.isAmendment = isAmendment;
	}

	public Boolean getIsLdcSplitRecord() {
		return isLdcSplitRecord;
	}

	public void setIsLdcSplitRecord(Boolean isLdcSplitRecord) {
		this.isLdcSplitRecord = isLdcSplitRecord;
	}

	public Boolean getIsMergeRecord() {
		return isMergeRecord;
	}

	public void setIsMergeRecord(Boolean isMergeRecord) {
		this.isMergeRecord = isMergeRecord;
	}

	public Boolean getIsProvisionSplitRecord() {
		return isProvisionSplitRecord;
	}

	public void setIsProvisionSplitRecord(Boolean isProvisionSplitRecord) {
		this.isProvisionSplitRecord = isProvisionSplitRecord;
	}

	public Boolean getIsSplitRecord() {
		return isSplitRecord;
	}

	public void setIsSplitRecord(Boolean isSplitRecord) {
		this.isSplitRecord = isSplitRecord;
	}

	public Date getLdcAppliedOn() {
		return ldcAppliedOn;
	}

	public void setLdcAppliedOn(Date ldcAppliedOn) {
		this.ldcAppliedOn = ldcAppliedOn;
	}

	public Integer getLineItemNumber() {
		return lineItemNumber;
	}

	public void setLineItemNumber(Integer lineItemNumber) {
		this.lineItemNumber = lineItemNumber;
	}

	public String getLinkedAdvanceNumber() {
		return linkedAdvanceNumber;
	}

	public void setLinkedAdvanceNumber(String linkedAdvanceNumber) {
		this.linkedAdvanceNumber = linkedAdvanceNumber;
	}

	public BigDecimal getMigoNumber() {
		return migoNumber;
	}

	public void setMigoNumber(BigDecimal migoNumber) {
		this.migoNumber = migoNumber;
	}

	public BigDecimal getMiroNumber() {
		return miroNumber;
	}

	public void setMiroNumber(BigDecimal miroNumber) {
		this.miroNumber = miroNumber;
	}

	public String getMismatchCategory() {
		return mismatchCategory;
	}

	public void setMismatchCategory(String mismatchCategory) {
		this.mismatchCategory = mismatchCategory;
	}

	public Date getMismatchModifiedDate() {
		return mismatchModifiedDate;
	}

	public void setMismatchModifiedDate(Date mismatchModifiedDate) {
		this.mismatchModifiedDate = mismatchModifiedDate;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Date getOriginalDocumentDate() {
		return originalDocumentDate;
	}

	public void setOriginalDocumentDate(Date originalDocumentDate) {
		this.originalDocumentDate = originalDocumentDate;
	}

	public String getOriginalDocumentNumber() {
		return originalDocumentNumber;
	}

	public void setOriginalDocumentNumber(String originalDocumentNumber) {
		this.originalDocumentNumber = originalDocumentNumber;
	}

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public BigDecimal getPenalty() {
		return penalty;
	}

	public void setPenalty(BigDecimal penalty) {
		this.penalty = penalty;
	}

	public Date getPoDate() {
		return poDate;
	}

	public void setPoDate(Date poDate) {
		this.poDate = poDate;
	}

	public String getPoNumber() {
		return poNumber;
	}

	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getProcessedFrom() {
		return processedFrom;
	}

	public void setProcessedFrom(String processedFrom) {
		this.processedFrom = processedFrom;
	}

	public Date getProvisionAppliedOn() {
		return provisionAppliedOn;
	}

	public void setProvisionAppliedOn(Date provisionAppliedOn) {
		this.provisionAppliedOn = provisionAppliedOn;
	}

	public Long getQtySupplied() {
		return qtySupplied;
	}

	public void setQtySupplied(Long qtySupplied) {
		this.qtySupplied = qtySupplied;
	}

	public String getFinalReason() {
		return finalReason;
	}

	public void setFinalReason(String finalReason) {
		this.finalReason = finalReason;
	}

	public String getIsResident() {
		return isResident;
	}

	public void setIsResident(String isResident) {
		this.isResident = isResident;
	}

	public String getSacDecription() {
		return sacDecription;
	}

	public void setSacDecription(String sacDecription) {
		this.sacDecription = sacDecription;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getServiceDescriptionGl() {
		return serviceDescriptionGl;
	}

	public void setServiceDescriptionGl(String serviceDescriptionGl) {
		this.serviceDescriptionGl = serviceDescriptionGl;
	}

	public BigDecimal getSgstAmount() {
		return sgstAmount;
	}

	public void setSgstAmount(BigDecimal sgstAmount) {
		this.sgstAmount = sgstAmount;
	}

	public BigDecimal getSgstRate() {
		return sgstRate;
	}

	public void setSgstRate(BigDecimal sgstRate) {
		this.sgstRate = sgstRate;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	public String getSourceIdentifier() {
		return sourceIdentifier;
	}

	public void setSourceIdentifier(String sourceIdentifier) {
		this.sourceIdentifier = sourceIdentifier;
	}

	public BigDecimal getSurcharge() {
		return surcharge;
	}

	public void setSurcharge(BigDecimal surcharge) {
		this.surcharge = surcharge;
	}

	public BigDecimal getTdsAmount() {
		return tdsAmount;
	}

	public void setTdsAmount(BigDecimal tdsAmount) {
		this.tdsAmount = tdsAmount;
	}

	public BigDecimal getTdsDeducted() {
		return tdsDeducted;
	}

	public void setTdsDeducted(BigDecimal tdsDeducted) {
		this.tdsDeducted = tdsDeducted;
	}

	public BigDecimal getTdsRate() {
		return tdsRate;
	}

	public void setTdsRate(BigDecimal tdsRate) {
		this.tdsRate = tdsRate;
	}

	public String getTdsSection() {
		return tdsSection;
	}

	public void setTdsSection(String tdsSection) {
		this.tdsSection = tdsSection;
	}

	public Integer getThresholdLimit() {
		return thresholdLimit;
	}

	public void setThresholdLimit(Integer thresholdLimit) {
		this.thresholdLimit = thresholdLimit;
	}

	public String getInvoiceType() {
		return invoiceType;
	}

	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}

	public String getUnitOfMeasurement() {
		return unitOfMeasurement;
	}

	public void setUnitOfMeasurement(String unitOfMeasurement) {
		this.unitOfMeasurement = unitOfMeasurement;
	}

	public String getUserDefinedField1() {
		return userDefinedField1;
	}

	public void setUserDefinedField1(String userDefinedField1) {
		this.userDefinedField1 = userDefinedField1;
	}

	public String getUserDefinedField2() {
		return userDefinedField2;
	}

	public void setUserDefinedField2(String userDefinedField2) {
		this.userDefinedField2 = userDefinedField2;
	}

	public String getUserDefinedField3() {
		return userDefinedField3;
	}

	public void setUserDefinedField3(String userDefinedField3) {
		this.userDefinedField3 = userDefinedField3;
	}

	public BigDecimal getVendorInvoiceNumber() {
		return vendorInvoiceNumber;
	}

	public void setVendorInvoiceNumber(BigDecimal vendorInvoiceNumber) {
		this.vendorInvoiceNumber = vendorInvoiceNumber;
	}
}
