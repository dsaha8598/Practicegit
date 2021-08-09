package com.ey.in.tds.ingestion.dto.invoice;

import java.io.Serializable;

/**
 * 
 * @author scriptbees
 *
 */
public class NrExcelErrorDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String serialNumber;
	private String reason;
	private String deductorMasterTan;
	private String assessmentYear;
	private String documentPostingDate;
	private String amountInForeignCurrency;
	private String beneficialOwnerOfIncome;
	private String dateOfDeductionOfTax;
	private String dateOfDepositOfTax;
	private String dateOfRemittance;
	private String isGAARComplaString;
	private String isMliOrPptSlob;
	private String isBeneficialOwnershipOfDeclaration;
	private String isFixedbaseAvailbleIndia;
	private String isFixedbaseApplicableFrom;
	private String isFixedbaseApplicableTo;
	private String isNoFixedBaseDeclaration;
	private String noPeDocumentApplicableFrom;
	private String noPeDocumentApplicableTo;
	private String isNoPeDocumentAvailable;
	private String isPoemOfDeductee;
	private String isNoPoemApplicableFrom;
	private String isNoPoemApplicableTo;
	private String isNoPoemAvailable;
	private String isNoPoemDeclarationInFuture;
	private String mliPptConditionSatisifed;
	private String mliSlobConditionSatisifed;
	private String natureOfPayment;
	private String active;
	private String amountInInr;
	private String amountOfIncometax;
	private String country;
	private String countryToRemittance;
	private String createdBy;
	private String createdDate;
	private String currency;
	private String deducteeCode;
	private String deducteeName;
	private String deducteePan;
	private String deductorPan;
	private String eductaionCess;
	private String eductaionFee;
	private String interest;
	private String erpDocumentNo;
	private String isGrossedUp;
	private String isPeIndia;
	private String isResident;
	private String isTenfApplicableFrom;
	private String isTenfApplicableTo;
	private String isTenfAvailable;
	private String isTenfFuture;
	private String isTrcApplicableFrom;
	private String isTrcApplicableTo;
	private String isTrcAvailable;
	private String isTrcFuture;
	private String noPoemFutureDate;
	private String poDate;
	private String poNumber;
	private String poType;
	private String articleOfDtaa;
	private String rateAsPerIncometax;
	private String surcharge;
	private String tdsAmount;
	private String tenfFutureDate;
	private String tin;
	private String trcFutureDate;
	private String vendorDocumentDate;
	private String vendorDocumentNo;
	private String stayPeriodFinancialYear;
	private String tdsAmountInForeignCurrency;
	private String whetherIncomeReceived;
	private String documentType;
	private String challanPaid;
	private String challanGeneratedDate;
	private String advanceCanAdjust;
	private String provisionCanAdjust;
	private String deducteeKey;
	private String eductaionInterest;
	// new column's
	private String isFixedBaseAvailableApplicableFrom;
	private String isFixedBaseAvailableApplicableTo;
	private String isFixedBaseAvailable;
	private String tdsSection;
	private String updateInTreatyEligibilityConditions;
	private String isDeducteeMasterUpdated;
	private String peInIndiaFromDate;
	private String peInIndiaToDate;
	private String noPeDeclarationAvailableInFuture;
	private String noPeDeclarationAvailableFutureDate;
	private String noFixedBaseDeclarationAvailableInFuture;
	private String noFixedBaseDeclarationAvailableFutureDate;
	private String noFixedBaseDeclarationFromDate;
	private String noFixedBaseDeclarationToDate;
	private String poemApplicableFromDate;
	private String poemApplicableToDate;
	private String detailedDescription;
	private String userDefinedField1;
	private String userDefinedField2;
	private String userDefinedField3;
	private String aggregateAmountOfRemittanceByFy;
	private String relevantPurposeCodeAsPerRbi;
	private String stringOfPaymentOrCredit;
	private String uniqueAcknowledgementOfTheCorrespondingForm15ca;
	private String sourceIdentifier;
	private String sourceFilename;
	private String deductorCode;
	private String deductorName;
	private String deductorGstin;
	private String deducteeGstin;
	private String supplyType;
	private String migoNumber;
	private String miroNumber;
	private String erpDocumentType;
	private String lineItemNumber;
	private String hsnOrSac;
	private String hsnOrSacDesc;
	private String invoiceDesc;
	private String glAccountCode;
	private String glAccountName;
	private String poDesc;
	private String igstRate;
	private String igstAmount;
	private String cgstRate;
	private String cgstAmount;
	private String sgstRate;
	private String sgstAmount;
	private String cessRate;
	private String cessAmount;
	private String pos;
	private String tdsTaxCodeErp;
	private String linkedAdvanceIndicator;
	private String linkedProvisionIndicator;
	private String originalDocumentNumber;
	private String originalDocumentDate;
	private String RefKey3;
	private String businessPlace;
	private String businessArea;
	private String plant;
	private String profitCenter;
	private String assignmentNumber;
	private String tdsBaseValue;
	private String poItemNo;
	private String typeOfTransaction;
	private String userName;
	private String exchangeRate;
	private String itemCode;
	private String invoiceValue;
	private String saaNumber;
	private String tdsRemittanceDate;
	private String debitCreditIndicator;
	private String isNoPeDocumentApplicableFrom;
	private String isNoPeDocumentApplicableTo;
	private String isNoPoemOfDeductee;
	private String isPoemDeclarationInFuture;
	private String userDefinedField4;
	private String userDefinedField5;
	private String linkingOfInvoiceWithPo;
	private String dtaaArticleName;
	private String natureOfRemittance;

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getDeductorMasterTan() {
		return deductorMasterTan;
	}

	public void setDeductorMasterTan(String deductorMasterTan) {
		this.deductorMasterTan = deductorMasterTan;
	}

	public String getAssessmentYear() {
		return assessmentYear;
	}

	public void setAssessmentYear(String assessmentYear) {
		this.assessmentYear = assessmentYear;
	}

	public String getDocumentPostingDate() {
		return documentPostingDate;
	}

	public void setDocumentPostingDate(String documentPostingDate) {
		this.documentPostingDate = documentPostingDate;
	}

	public String getAmountInForeignCurrency() {
		return amountInForeignCurrency;
	}

	public void setAmountInForeignCurrency(String amountInForeignCurrency) {
		this.amountInForeignCurrency = amountInForeignCurrency;
	}

	public String getBeneficialOwnerOfIncome() {
		return beneficialOwnerOfIncome;
	}

	public void setBeneficialOwnerOfIncome(String beneficialOwnerOfIncome) {
		this.beneficialOwnerOfIncome = beneficialOwnerOfIncome;
	}

	public String getDateOfDeductionOfTax() {
		return dateOfDeductionOfTax;
	}

	public void setDateOfDeductionOfTax(String dateOfDeductionOfTax) {
		this.dateOfDeductionOfTax = dateOfDeductionOfTax;
	}

	public String getDateOfRemittance() {
		return dateOfRemittance;
	}

	public void setDateOfRemittance(String dateOfRemittance) {
		this.dateOfRemittance = dateOfRemittance;
	}

	public String getIsGAARComplaString() {
		return isGAARComplaString;
	}

	public void setIsGAARComplaString(String isGAARComplaString) {
		this.isGAARComplaString = isGAARComplaString;
	}

	public String getIsMliOrPptSlob() {
		return isMliOrPptSlob;
	}

	public void setIsMliOrPptSlob(String isMliOrPptSlob) {
		this.isMliOrPptSlob = isMliOrPptSlob;
	}

	public String getIsBeneficialOwnershipOfDeclaration() {
		return isBeneficialOwnershipOfDeclaration;
	}

	public void setIsBeneficialOwnershipOfDeclaration(String isBeneficialOwnershipOfDeclaration) {
		this.isBeneficialOwnershipOfDeclaration = isBeneficialOwnershipOfDeclaration;
	}

	public String getIsFixedbaseAvailbleIndia() {
		return isFixedbaseAvailbleIndia;
	}

	public void setIsFixedbaseAvailbleIndia(String isFixedbaseAvailbleIndia) {
		this.isFixedbaseAvailbleIndia = isFixedbaseAvailbleIndia;
	}

	public String getIsFixedbaseApplicableFrom() {
		return isFixedbaseApplicableFrom;
	}

	public void setIsFixedbaseApplicableFrom(String isFixedbaseApplicableFrom) {
		this.isFixedbaseApplicableFrom = isFixedbaseApplicableFrom;
	}

	public String getIsFixedbaseApplicableTo() {
		return isFixedbaseApplicableTo;
	}

	public void setIsFixedbaseApplicableTo(String isFixedbaseApplicableTo) {
		this.isFixedbaseApplicableTo = isFixedbaseApplicableTo;
	}

	public String getIsNoFixedBaseDeclaration() {
		return isNoFixedBaseDeclaration;
	}

	public void setIsNoFixedBaseDeclaration(String isNoFixedBaseDeclaration) {
		this.isNoFixedBaseDeclaration = isNoFixedBaseDeclaration;
	}

	public String getNoPeDocumentApplicableFrom() {
		return noPeDocumentApplicableFrom;
	}

	public void setNoPeDocumentApplicableFrom(String noPeDocumentApplicableFrom) {
		this.noPeDocumentApplicableFrom = noPeDocumentApplicableFrom;
	}

	public String getNoPeDocumentApplicableTo() {
		return noPeDocumentApplicableTo;
	}

	public void setNoPeDocumentApplicableTo(String noPeDocumentApplicableTo) {
		this.noPeDocumentApplicableTo = noPeDocumentApplicableTo;
	}

	public String getIsNoPeDocumentAvailable() {
		return isNoPeDocumentAvailable;
	}

	public void setIsNoPeDocumentAvailable(String isNoPeDocumentAvailable) {
		this.isNoPeDocumentAvailable = isNoPeDocumentAvailable;
	}

	public String getIsPoemOfDeductee() {
		return isPoemOfDeductee;
	}

	public void setIsPoemOfDeductee(String isPoemOfDeductee) {
		this.isPoemOfDeductee = isPoemOfDeductee;
	}

	public String getIsNoPoemApplicableFrom() {
		return isNoPoemApplicableFrom;
	}

	public void setIsNoPoemApplicableFrom(String isNoPoemApplicableFrom) {
		this.isNoPoemApplicableFrom = isNoPoemApplicableFrom;
	}

	public String getIsNoPoemApplicableTo() {
		return isNoPoemApplicableTo;
	}

	public void setIsNoPoemApplicableTo(String isNoPoemApplicableTo) {
		this.isNoPoemApplicableTo = isNoPoemApplicableTo;
	}

	public String getIsNoPoemAvailable() {
		return isNoPoemAvailable;
	}

	public void setIsNoPoemAvailable(String isNoPoemAvailable) {
		this.isNoPoemAvailable = isNoPoemAvailable;
	}

	public String getIsNoPoemDeclarationInFuture() {
		return isNoPoemDeclarationInFuture;
	}

	public void setIsNoPoemDeclarationInFuture(String isNoPoemDeclarationInFuture) {
		this.isNoPoemDeclarationInFuture = isNoPoemDeclarationInFuture;
	}

	public String getMliPptConditionSatisifed() {
		return mliPptConditionSatisifed;
	}

	public void setMliPptConditionSatisifed(String mliPptConditionSatisifed) {
		this.mliPptConditionSatisifed = mliPptConditionSatisifed;
	}

	public String getMliSlobConditionSatisifed() {
		return mliSlobConditionSatisifed;
	}

	public void setMliSlobConditionSatisifed(String mliSlobConditionSatisifed) {
		this.mliSlobConditionSatisifed = mliSlobConditionSatisifed;
	}

	public String getNatureOfPayment() {
		return natureOfPayment;
	}

	public void setNatureOfPayment(String natureOfPayment) {
		this.natureOfPayment = natureOfPayment;
	}

	public String getDtaaArticleName() {
		return dtaaArticleName;
	}

	public void setDtaaArticleName(String dtaaArticleName) {
		this.dtaaArticleName = dtaaArticleName;
	}

	public String getNatureOfRemittance() {
		return natureOfRemittance;
	}

	public void setNatureOfRemittance(String natureOfRemittance) {
		this.natureOfRemittance = natureOfRemittance;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getAmountInInr() {
		return amountInInr;
	}

	public void setAmountInInr(String amountInInr) {
		this.amountInInr = amountInInr;
	}

	public String getAmountOfIncometax() {
		return amountOfIncometax;
	}

	public void setAmountOfIncometax(String amountOfIncometax) {
		this.amountOfIncometax = amountOfIncometax;
	}


	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountryToRemittance() {
		return countryToRemittance;
	}

	public void setCountryToRemittance(String countryToRemittance) {
		this.countryToRemittance = countryToRemittance;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getDeducteeCode() {
		return deducteeCode;
	}

	public void setDeducteeCode(String deducteeCode) {
		this.deducteeCode = deducteeCode;
	}

	public String getDeducteeName() {
		return deducteeName;
	}

	public void setDeducteeName(String deducteeName) {
		this.deducteeName = deducteeName;
	}

	public String getDeducteePan() {
		return deducteePan;
	}

	public void setDeducteePan(String deducteePan) {
		this.deducteePan = deducteePan;
	}


	public String getDeductorPan() {
		return deductorPan;
	}

	public void setDeductorPan(String deductorPan) {
		this.deductorPan = deductorPan;
	}

	public String getEductaionCess() {
		return eductaionCess;
	}

	public void setEductaionCess(String eductaionCess) {
		this.eductaionCess = eductaionCess;
	}

	public String getEductaionFee() {
		return eductaionFee;
	}

	public void setEductaionFee(String eductaionFee) {
		this.eductaionFee = eductaionFee;
	}

	public String getInterest() {
		return interest;
	}

	public void setInterest(String interest) {
		this.interest = interest;
	}

	public String getErpDocumentNo() {
		return erpDocumentNo;
	}

	public void setErpDocumentNo(String erpDocumentNo) {
		this.erpDocumentNo = erpDocumentNo;
	}

	public String getIsGrossedUp() {
		return isGrossedUp;
	}

	public void setIsGrossedUp(String isGrossedUp) {
		this.isGrossedUp = isGrossedUp;
	}


	public String getIsPeIndia() {
		return isPeIndia;
	}

	public void setIsPeIndia(String isPeIndia) {
		this.isPeIndia = isPeIndia;
	}

	public String getIsResident() {
		return isResident;
	}

	public void setIsResident(String isResident) {
		this.isResident = isResident;
	}

	public String getIsTenfApplicableFrom() {
		return isTenfApplicableFrom;
	}

	public void setIsTenfApplicableFrom(String isTenfApplicableFrom) {
		this.isTenfApplicableFrom = isTenfApplicableFrom;
	}

	public String getIsTenfApplicableTo() {
		return isTenfApplicableTo;
	}

	public void setIsTenfApplicableTo(String isTenfApplicableTo) {
		this.isTenfApplicableTo = isTenfApplicableTo;
	}

	public String getIsTenfAvailable() {
		return isTenfAvailable;
	}

	public void setIsTenfAvailable(String isTenfAvailable) {
		this.isTenfAvailable = isTenfAvailable;
	}

	public String getIsTenfFuture() {
		return isTenfFuture;
	}

	public void setIsTenfFuture(String isTenfFuture) {
		this.isTenfFuture = isTenfFuture;
	}

	public String getIsTrcApplicableFrom() {
		return isTrcApplicableFrom;
	}

	public void setIsTrcApplicableFrom(String isTrcApplicableFrom) {
		this.isTrcApplicableFrom = isTrcApplicableFrom;
	}

	public String getIsTrcApplicableTo() {
		return isTrcApplicableTo;
	}

	public void setIsTrcApplicableTo(String isTrcApplicableTo) {
		this.isTrcApplicableTo = isTrcApplicableTo;
	}

	public String getIsTrcAvailable() {
		return isTrcAvailable;
	}

	public void setIsTrcAvailable(String isTrcAvailable) {
		this.isTrcAvailable = isTrcAvailable;
	}

	public String getIsTrcFuture() {
		return isTrcFuture;
	}

	public void setIsTrcFuture(String isTrcFuture) {
		this.isTrcFuture = isTrcFuture;
	}

	public String getNoPoemFutureDate() {
		return noPoemFutureDate;
	}

	public void setNoPoemFutureDate(String noPoemFutureDate) {
		this.noPoemFutureDate = noPoemFutureDate;
	}

	public String getPoDate() {
		return poDate;
	}

	public void setPoDate(String poDate) {
		this.poDate = poDate;
	}

	public String getPoNumber() {
		return poNumber;
	}

	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}

	public String getArticleOfDtaa() {
		return articleOfDtaa;
	}

	public void setArticleOfDtaa(String articleOfDtaa) {
		this.articleOfDtaa = articleOfDtaa;
	}

	public String getRateAsPerIncometax() {
		return rateAsPerIncometax;
	}

	public void setRateAsPerIncometax(String rateAsPerIncometax) {
		this.rateAsPerIncometax = rateAsPerIncometax;
	}


	public String getSurcharge() {
		return surcharge;
	}

	public void setSurcharge(String surcharge) {
		this.surcharge = surcharge;
	}

	public String getTdsAmount() {
		return tdsAmount;
	}

	public void setTdsAmount(String tdsAmount) {
		this.tdsAmount = tdsAmount;
	}

	public String getTenfFutureDate() {
		return tenfFutureDate;
	}

	public void setTenfFutureDate(String tenfFutureDate) {
		this.tenfFutureDate = tenfFutureDate;
	}

	public String getTin() {
		return tin;
	}

	public void setTin(String tin) {
		this.tin = tin;
	}

	public String getTrcFutureDate() {
		return trcFutureDate;
	}

	public void setTrcFutureDate(String trcFutureDate) {
		this.trcFutureDate = trcFutureDate;
	}

	public String getVendorDocumentDate() {
		return vendorDocumentDate;
	}

	public void setVendorDocumentDate(String vendorDocumentDate) {
		this.vendorDocumentDate = vendorDocumentDate;
	}

	public String getVendorDocumentNo() {
		return vendorDocumentNo;
	}

	public void setVendorDocumentNo(String vendorDocumentNo) {
		this.vendorDocumentNo = vendorDocumentNo;
	}

	public String getStayPeriodFinancialYear() {
		return stayPeriodFinancialYear;
	}

	public void setStayPeriodFinancialYear(String stayPeriodFinancialYear) {
		this.stayPeriodFinancialYear = stayPeriodFinancialYear;
	}

	public String getTdsAmountInForeignCurrency() {
		return tdsAmountInForeignCurrency;
	}

	public void setTdsAmountInForeignCurrency(String tdsAmountInForeignCurrency) {
		this.tdsAmountInForeignCurrency = tdsAmountInForeignCurrency;
	}

	public String getWhetherIncomeReceived() {
		return whetherIncomeReceived;
	}

	public void setWhetherIncomeReceived(String whetherIncomeReceived) {
		this.whetherIncomeReceived = whetherIncomeReceived;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getChallanPaid() {
		return challanPaid;
	}

	public void setChallanPaid(String challanPaid) {
		this.challanPaid = challanPaid;
	}

	public String getChallanGeneratedDate() {
		return challanGeneratedDate;
	}

	public void setChallanGeneratedDate(String challanGeneratedDate) {
		this.challanGeneratedDate = challanGeneratedDate;
	}

	public String getAdvanceCanAdjust() {
		return advanceCanAdjust;
	}

	public void setAdvanceCanAdjust(String advanceCanAdjust) {
		this.advanceCanAdjust = advanceCanAdjust;
	}

	public String getProvisionCanAdjust() {
		return provisionCanAdjust;
	}

	public void setProvisionCanAdjust(String provisionCanAdjust) {
		this.provisionCanAdjust = provisionCanAdjust;
	}

	public String getDeducteeKey() {
		return deducteeKey;
	}

	public void setDeducteeKey(String deducteeKey) {
		this.deducteeKey = deducteeKey;
	}

	public String getEductaionInterest() {
		return eductaionInterest;
	}

	public void setEductaionInterest(String eductaionInterest) {
		this.eductaionInterest = eductaionInterest;
	}

	public String getIsFixedBaseAvailableApplicableFrom() {
		return isFixedBaseAvailableApplicableFrom;
	}

	public void setIsFixedBaseAvailableApplicableFrom(String isFixedBaseAvailableApplicableFrom) {
		this.isFixedBaseAvailableApplicableFrom = isFixedBaseAvailableApplicableFrom;
	}

	public String getIsFixedBaseAvailableApplicableTo() {
		return isFixedBaseAvailableApplicableTo;
	}

	public void setIsFixedBaseAvailableApplicableTo(String isFixedBaseAvailableApplicableTo) {
		this.isFixedBaseAvailableApplicableTo = isFixedBaseAvailableApplicableTo;
	}

	public String getIsFixedBaseAvailable() {
		return isFixedBaseAvailable;
	}

	public void setIsFixedBaseAvailable(String isFixedBaseAvailable) {
		this.isFixedBaseAvailable = isFixedBaseAvailable;
	}

	public String getTdsSection() {
		return tdsSection;
	}

	public void setTdsSection(String tdsSection) {
		this.tdsSection = tdsSection;
	}

	public String getUpdateInTreatyEligibilityConditions() {
		return updateInTreatyEligibilityConditions;
	}

	public void setUpdateInTreatyEligibilityConditions(String updateInTreatyEligibilityConditions) {
		this.updateInTreatyEligibilityConditions = updateInTreatyEligibilityConditions;
	}

	public String getIsDeducteeMasterUpdated() {
		return isDeducteeMasterUpdated;
	}

	public void setIsDeducteeMasterUpdated(String isDeducteeMasterUpdated) {
		this.isDeducteeMasterUpdated = isDeducteeMasterUpdated;
	}

	public String getPeInIndiaFromDate() {
		return peInIndiaFromDate;
	}

	public void setPeInIndiaFromDate(String peInIndiaFromDate) {
		this.peInIndiaFromDate = peInIndiaFromDate;
	}

	public String getPeInIndiaToDate() {
		return peInIndiaToDate;
	}

	public void setPeInIndiaToDate(String peInIndiaToDate) {
		this.peInIndiaToDate = peInIndiaToDate;
	}

	public String getNoPeDeclarationAvailableInFuture() {
		return noPeDeclarationAvailableInFuture;
	}

	public void setNoPeDeclarationAvailableInFuture(String noPeDeclarationAvailableInFuture) {
		this.noPeDeclarationAvailableInFuture = noPeDeclarationAvailableInFuture;
	}

	public String getNoPeDeclarationAvailableFutureDate() {
		return noPeDeclarationAvailableFutureDate;
	}

	public void setNoPeDeclarationAvailableFutureDate(String noPeDeclarationAvailableFutureDate) {
		this.noPeDeclarationAvailableFutureDate = noPeDeclarationAvailableFutureDate;
	}

	public String getNoFixedBaseDeclarationAvailableInFuture() {
		return noFixedBaseDeclarationAvailableInFuture;
	}

	public void setNoFixedBaseDeclarationAvailableInFuture(String noFixedBaseDeclarationAvailableInFuture) {
		this.noFixedBaseDeclarationAvailableInFuture = noFixedBaseDeclarationAvailableInFuture;
	}

	public String getNoFixedBaseDeclarationAvailableFutureDate() {
		return noFixedBaseDeclarationAvailableFutureDate;
	}

	public void setNoFixedBaseDeclarationAvailableFutureDate(String noFixedBaseDeclarationAvailableFutureDate) {
		this.noFixedBaseDeclarationAvailableFutureDate = noFixedBaseDeclarationAvailableFutureDate;
	}

	public String getNoFixedBaseDeclarationFromDate() {
		return noFixedBaseDeclarationFromDate;
	}

	public void setNoFixedBaseDeclarationFromDate(String noFixedBaseDeclarationFromDate) {
		this.noFixedBaseDeclarationFromDate = noFixedBaseDeclarationFromDate;
	}

	public String getNoFixedBaseDeclarationToDate() {
		return noFixedBaseDeclarationToDate;
	}

	public void setNoFixedBaseDeclarationToDate(String noFixedBaseDeclarationToDate) {
		this.noFixedBaseDeclarationToDate = noFixedBaseDeclarationToDate;
	}

	public String getPoemApplicableFromDate() {
		return poemApplicableFromDate;
	}

	public void setPoemApplicableFromDate(String poemApplicableFromDate) {
		this.poemApplicableFromDate = poemApplicableFromDate;
	}

	public String getPoemApplicableToDate() {
		return poemApplicableToDate;
	}

	public void setPoemApplicableToDate(String poemApplicableToDate) {
		this.poemApplicableToDate = poemApplicableToDate;
	}

	public String getDetailedDescription() {
		return detailedDescription;
	}

	public void setDetailedDescription(String detailedDescription) {
		this.detailedDescription = detailedDescription;
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

	public String getAggregateAmountOfRemittanceByFy() {
		return aggregateAmountOfRemittanceByFy;
	}

	public void setAggregateAmountOfRemittanceByFy(String aggregateAmountOfRemittanceByFy) {
		this.aggregateAmountOfRemittanceByFy = aggregateAmountOfRemittanceByFy;
	}

	public String getRelevantPurposeCodeAsPerRbi() {
		return relevantPurposeCodeAsPerRbi;
	}

	public void setRelevantPurposeCodeAsPerRbi(String relevantPurposeCodeAsPerRbi) {
		this.relevantPurposeCodeAsPerRbi = relevantPurposeCodeAsPerRbi;
	}

	public String getStringOfPaymentOrCredit() {
		return stringOfPaymentOrCredit;
	}

	public void setStringOfPaymentOrCredit(String stringOfPaymentOrCredit) {
		this.stringOfPaymentOrCredit = stringOfPaymentOrCredit;
	}

	public String getUniqueAcknowledgementOfTheCorrespondingForm15ca() {
		return uniqueAcknowledgementOfTheCorrespondingForm15ca;
	}

	public void setUniqueAcknowledgementOfTheCorrespondingForm15ca(
			String uniqueAcknowledgementOfTheCorrespondingForm15ca) {
		this.uniqueAcknowledgementOfTheCorrespondingForm15ca = uniqueAcknowledgementOfTheCorrespondingForm15ca;
	}


	public String getSourceIdentifier() {
		return sourceIdentifier;
	}

	public void setSourceIdentifier(String sourceIdentifier) {
		this.sourceIdentifier = sourceIdentifier;
	}

	public String getSourceFilename() {
		return sourceFilename;
	}

	public void setSourceFilename(String sourceFilename) {
		this.sourceFilename = sourceFilename;
	}

	public String getDeductorCode() {
		return deductorCode;
	}

	public void setDeductorCode(String deductorCode) {
		this.deductorCode = deductorCode;
	}

	public String getDeductorName() {
		return deductorName;
	}

	public void setDeductorName(String deductorName) {
		this.deductorName = deductorName;
	}

	public String getDeductorGstin() {
		return deductorGstin;
	}

	public void setDeductorGstin(String deductorGstin) {
		this.deductorGstin = deductorGstin;
	}

	public String getDeducteeGstin() {
		return deducteeGstin;
	}

	public void setDeducteeGstin(String deducteeGstin) {
		this.deducteeGstin = deducteeGstin;
	}

	public String getSupplyType() {
		return supplyType;
	}

	public void setSupplyType(String supplyType) {
		this.supplyType = supplyType;
	}

	public String getMigoNumber() {
		return migoNumber;
	}

	public void setMigoNumber(String migoNumber) {
		this.migoNumber = migoNumber;
	}

	public String getMiroNumber() {
		return miroNumber;
	}

	public void setMiroNumber(String miroNumber) {
		this.miroNumber = miroNumber;
	}

	public String getErpDocumentType() {
		return erpDocumentType;
	}

	public void setErpDocumentType(String erpDocumentType) {
		this.erpDocumentType = erpDocumentType;
	}

	public String getLineItemNumber() {
		return lineItemNumber;
	}

	public void setLineItemNumber(String lineItemNumber) {
		this.lineItemNumber = lineItemNumber;
	}

	public String getHsnOrSac() {
		return hsnOrSac;
	}

	public void setHsnOrSac(String hsnOrSac) {
		this.hsnOrSac = hsnOrSac;
	}

	public String getHsnOrSacDesc() {
		return hsnOrSacDesc;
	}

	public void setHsnOrSacDesc(String hsnOrSacDesc) {
		this.hsnOrSacDesc = hsnOrSacDesc;
	}

	public String getInvoiceDesc() {
		return invoiceDesc;
	}

	public void setInvoiceDesc(String invoiceDesc) {
		this.invoiceDesc = invoiceDesc;
	}

	public String getGlAccountCode() {
		return glAccountCode;
	}

	public void setGlAccountCode(String glAccountCode) {
		this.glAccountCode = glAccountCode;
	}

	public String getGlAccountName() {
		return glAccountName;
	}

	public void setGlAccountName(String glAccountName) {
		this.glAccountName = glAccountName;
	}

	public String getPoDesc() {
		return poDesc;
	}

	public void setPoDesc(String poDesc) {
		this.poDesc = poDesc;
	}

	public String getIgstRate() {
		return igstRate;
	}

	public void setIgstRate(String igstRate) {
		this.igstRate = igstRate;
	}

	public String getIgstAmount() {
		return igstAmount;
	}

	public void setIgstAmount(String igstAmount) {
		this.igstAmount = igstAmount;
	}

	public String getCgstRate() {
		return cgstRate;
	}

	public void setCgstRate(String cgstRate) {
		this.cgstRate = cgstRate;
	}

	public String getCgstAmount() {
		return cgstAmount;
	}

	public void setCgstAmount(String cgstAmount) {
		this.cgstAmount = cgstAmount;
	}

	public String getSgstRate() {
		return sgstRate;
	}

	public void setSgstRate(String sgstRate) {
		this.sgstRate = sgstRate;
	}

	public String getSgstAmount() {
		return sgstAmount;
	}

	public void setSgstAmount(String sgstAmount) {
		this.sgstAmount = sgstAmount;
	}

	public String getCessRate() {
		return cessRate;
	}

	public void setCessRate(String cessRate) {
		this.cessRate = cessRate;
	}

	public String getCessAmount() {
		return cessAmount;
	}

	public void setCessAmount(String cessAmount) {
		this.cessAmount = cessAmount;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getTdsTaxCodeErp() {
		return tdsTaxCodeErp;
	}

	public void setTdsTaxCodeErp(String tdsTaxCodeErp) {
		this.tdsTaxCodeErp = tdsTaxCodeErp;
	}

	public String getLinkedAdvanceIndicator() {
		return linkedAdvanceIndicator;
	}

	public void setLinkedAdvanceIndicator(String linkedAdvanceIndicator) {
		this.linkedAdvanceIndicator = linkedAdvanceIndicator;
	}

	public String getLinkedProvisionIndicator() {
		return linkedProvisionIndicator;
	}

	public void setLinkedProvisionIndicator(String linkedProvisionIndicator) {
		this.linkedProvisionIndicator = linkedProvisionIndicator;
	}

	public String getOriginalDocumentNumber() {
		return originalDocumentNumber;
	}

	public void setOriginalDocumentNumber(String originalDocumentNumber) {
		this.originalDocumentNumber = originalDocumentNumber;
	}

	public String getOriginalDocumentDate() {
		return originalDocumentDate;
	}

	public void setOriginalDocumentDate(String originalDocumentDate) {
		this.originalDocumentDate = originalDocumentDate;
	}

	public String getRefKey3() {
		return RefKey3;
	}

	public void setRefKey3(String refKey3) {
		RefKey3 = refKey3;
	}

	public String getBusinessPlace() {
		return businessPlace;
	}

	public void setBusinessPlace(String businessPlace) {
		this.businessPlace = businessPlace;
	}

	public String getBusinessArea() {
		return businessArea;
	}

	public void setBusinessArea(String businessArea) {
		this.businessArea = businessArea;
	}

	public String getPlant() {
		return plant;
	}

	public void setPlant(String plant) {
		this.plant = plant;
	}

	public String getProfitCenter() {
		return profitCenter;
	}

	public void setProfitCenter(String profitCenter) {
		this.profitCenter = profitCenter;
	}

	public String getAssignmentNumber() {
		return assignmentNumber;
	}

	public void setAssignmentNumber(String assignmentNumber) {
		this.assignmentNumber = assignmentNumber;
	}

	public String getTdsBaseValue() {
		return tdsBaseValue;
	}

	public void setTdsBaseValue(String tdsBaseValue) {
		this.tdsBaseValue = tdsBaseValue;
	}

	public String getPoItemNo() {
		return poItemNo;
	}

	public void setPoItemNo(String poItemNo) {
		this.poItemNo = poItemNo;
	}

	public String getTypeOfTransaction() {
		return typeOfTransaction;
	}

	public void setTypeOfTransaction(String typeOfTransaction) {
		this.typeOfTransaction = typeOfTransaction;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(String exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getInvoiceValue() {
		return invoiceValue;
	}

	public void setInvoiceValue(String invoiceValue) {
		this.invoiceValue = invoiceValue;
	}

	public String getSaaNumber() {
		return saaNumber;
	}

	public void setSaaNumber(String saaNumber) {
		this.saaNumber = saaNumber;
	}

	public String getTdsRemittanceDate() {
		return tdsRemittanceDate;
	}

	public void setTdsRemittanceDate(String tdsRemittanceDate) {
		this.tdsRemittanceDate = tdsRemittanceDate;
	}

	public String getDebitCreditIndicator() {
		return debitCreditIndicator;
	}

	public void setDebitCreditIndicator(String debitCreditIndicator) {
		this.debitCreditIndicator = debitCreditIndicator;
	}

	public String getIsNoPeDocumentApplicableFrom() {
		return isNoPeDocumentApplicableFrom;
	}

	public void setIsNoPeDocumentApplicableFrom(String isNoPeDocumentApplicableFrom) {
		this.isNoPeDocumentApplicableFrom = isNoPeDocumentApplicableFrom;
	}

	public String getIsNoPeDocumentApplicableTo() {
		return isNoPeDocumentApplicableTo;
	}

	public void setIsNoPeDocumentApplicableTo(String isNoPeDocumentApplicableTo) {
		this.isNoPeDocumentApplicableTo = isNoPeDocumentApplicableTo;
	}

	public String getIsNoPoemOfDeductee() {
		return isNoPoemOfDeductee;
	}

	public void setIsNoPoemOfDeductee(String isNoPoemOfDeductee) {
		this.isNoPoemOfDeductee = isNoPoemOfDeductee;
	}

	public String getIsPoemDeclarationInFuture() {
		return isPoemDeclarationInFuture;
	}

	public void setIsPoemDeclarationInFuture(String isPoemDeclarationInFuture) {
		this.isPoemDeclarationInFuture = isPoemDeclarationInFuture;
	}

	public String getUserDefinedField4() {
		return userDefinedField4;
	}

	public void setUserDefinedField4(String userDefinedField4) {
		this.userDefinedField4 = userDefinedField4;
	}

	public String getUserDefinedField5() {
		return userDefinedField5;
	}

	public void setUserDefinedField5(String userDefinedField5) {
		this.userDefinedField5 = userDefinedField5;
	}

	public String getLinkingOfInvoiceWithPo() {
		return linkingOfInvoiceWithPo;
	}

	public void setLinkingOfInvoiceWithPo(String linkingOfInvoiceWithPo) {
		this.linkingOfInvoiceWithPo = linkingOfInvoiceWithPo;
	}

	public String getPoType() {
		return poType;
	}

	public void setPoType(String poType) {
		this.poType = poType;
	}

	public String getDateOfDepositOfTax() {
		return dateOfDepositOfTax;
	}

	public void setDateOfDepositOfTax(String dateOfDepositOfTax) {
		this.dateOfDepositOfTax = dateOfDepositOfTax;
	}

}
