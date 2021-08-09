package com.ey.in.tds.ingestion.dto.generalledger;

import java.util.Date;
import java.util.UUID;

public class GLCategoryDTO {
	
	private UUID id;
	private String sourceIdentifier;
	private String sourceFileName;
	private String companyCode;
	private String nameOfTheCompanyCode;
	private String deductorPAN;
	private String deductorTAN;
	private String deductorGSTIN;
	private String deductorCode;
	private String nameOfTheDeductee;
	private String nonResidentDeducteeIndicator;
	private String deducteePAN;
	private String deducteeTIN;
	private String deducteeGSTIN;
	private String documentNumber;
	private String documentType;
	private Date documentDate;
	private Date postingDateOfDocument;
	private String lineItemNumber;
	private String accountCode;
	private String accountDescription;
	private String tdsSection;
	private String invoiceAmountInLocalCurrency;
	private String miroMumber;
	private String migoNumber;
	private String referenceNumber;
	private String userDefinedField1;
	private String userDefinedField2;
	private String userDefinedField3;
	
	private Double clientAmount;
	private Double toolDerivedAmount;
	private String clientSection ;
	private String toolDerivedSection;
	
	public UUID getId() {
		return id;
	}
	public void setId(UUID uuid) {
		this.id = uuid;
	}
	public String getSourceIdentifier() {
		return sourceIdentifier;
	}
	public void setSourceIdentifier(String sourceIdentifier) {
		this.sourceIdentifier = sourceIdentifier;
	}
	public String getSourceFileName() {
		return sourceFileName;
	}
	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}
	public String getCompanyCode() {
		return companyCode;
	}
	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}
	public String getNameOfTheCompanyCode() {
		return nameOfTheCompanyCode;
	}
	public void setNameOfTheCompanyCode(String nameOfTheCompanyCode) {
		this.nameOfTheCompanyCode = nameOfTheCompanyCode;
	}
	public String getDeductorPAN() {
		return deductorPAN;
	}
	public void setDeductorPAN(String deductorPAN) {
		this.deductorPAN = deductorPAN;
	}
	public String getDeductorTAN() {
		return deductorTAN;
	}
	public void setDeductorTAN(String deductorTAN) {
		this.deductorTAN = deductorTAN;
	}
	public String getDeductorGSTIN() {
		return deductorGSTIN;
	}
	public void setDeductorGSTIN(String deductorGSTIN) {
		this.deductorGSTIN = deductorGSTIN;
	}
	public String getDeductorCode() {
		return deductorCode;
	}
	public void setDeductorCode(String deductorCode) {
		this.deductorCode = deductorCode;
	}
	public String getNameOfTheDeductee() {
		return nameOfTheDeductee;
	}
	public void setNameOfTheDeductee(String nameOfTheDeductee) {
		this.nameOfTheDeductee = nameOfTheDeductee;
	}
	public String getNonResidentDeducteeIndicator() {
		return nonResidentDeducteeIndicator;
	}
	public void setNonResidentDeducteeIndicator(String nonResidentDeducteeIndicator) {
		this.nonResidentDeducteeIndicator = nonResidentDeducteeIndicator;
	}
	public String getDeducteePAN() {
		return deducteePAN;
	}
	public void setDeducteePAN(String deducteePAN) {
		this.deducteePAN = deducteePAN;
	}
	public String getDeducteeTIN() {
		return deducteeTIN;
	}
	public void setDeducteeTIN(String deducteeTIN) {
		this.deducteeTIN = deducteeTIN;
	}
	public String getDeducteeGSTIN() {
		return deducteeGSTIN;
	}
	public void setDeducteeGSTIN(String deducteeGSTIN) {
		this.deducteeGSTIN = deducteeGSTIN;
	}
	public String getDocumentNumber() {
		return documentNumber;
	}
	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}
	public String getDocumentType() {
		return documentType;
	}
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}
	public Date getDocumentDate() {
		return documentDate;
	}
	public void setDocumentDate(Date date) {
		this.documentDate = date;
	}
	public Date getPostingDateOfDocument() {
		return postingDateOfDocument;
	}
	public void setPostingDateOfDocument(Date date) {
		this.postingDateOfDocument = date;
	}
	public String getLineItemNumber() {
		return lineItemNumber;
	}
	public void setLineItemNumber(String lineItemNumber) {
		this.lineItemNumber = lineItemNumber;
	}
	public String getAccountCode() {
		return accountCode;
	}
	public void setAccountCode(String accountCode) {
		this.accountCode = accountCode;
	}
	public String getAccountDescription() {
		return accountDescription;
	}
	public void setAccountDescription(String accountDescription) {
		this.accountDescription = accountDescription;
	}
	public String getTdsSection() {
		return tdsSection;
	}
	public void setTdsSection(String tdsSection) {
		this.tdsSection = tdsSection;
	}
	public String getInvoiceAmountInLocalCurrency() {
		return invoiceAmountInLocalCurrency;
	}
	public void setInvoiceAmountInLocalCurrency(String invoiceAmountInLocalCurrency) {
		this.invoiceAmountInLocalCurrency = invoiceAmountInLocalCurrency;
	}
	public String getMiroMumber() {
		return miroMumber;
	}
	public void setMiroMumber(String miroMumber) {
		this.miroMumber = miroMumber;
	}
	public String getMigoNumber() {
		return migoNumber;
	}
	public void setMigoNumber(String migoNumber) {
		this.migoNumber = migoNumber;
	}
	public String getReferenceNumber() {
		return referenceNumber;
	}
	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
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
	public Double getClientAmount() {
		return clientAmount;
	}
	public void setClientAmount(Double clientAmount) {
		this.clientAmount = clientAmount;
	}
	public Double getToolDerivedAmount() {
		return toolDerivedAmount;
	}
	public void setToolDerivedAmount(Double toolDerivedAmount) {
		this.toolDerivedAmount = toolDerivedAmount;
	}
	public String getClientSection() {
		return clientSection;
	}
	public void setClientSection(String clientSection) {
		this.clientSection = clientSection;
	}
	public String getToolDerivedSection() {
		return toolDerivedSection;
	}
	public void setToolDerivedSection(String toolDerivedSection) {
		this.toolDerivedSection = toolDerivedSection;
	}

}
