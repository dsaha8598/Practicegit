package com.ey.in.tds.ingestion.dto.invoice;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;


public class NrExcelDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String deductorPan;
	private String deductorTan;
	private String deducteeCode;
	private String deducteeName;
	private String deducteePan;
	private String deducteeStatus;
	private String flatOrDoorOrBlockNo;
	private String nameOrBuilding;
	private String roadOrStreet;
	private String areaOrLocality;
	private String townOrCityOrDistrict;
	private String state;
	private String country;
	private String zipCode;
	private String emailId;
	private String contactNumber;
	private String tin;
	private String countryToRemittance;
	private Boolean isTRCAvailable;
	private Boolean istrcFuture;
	private Date isTRCApplicableFrom;
	private Date isTRCApplicableTo;
	private Boolean isTenfAvailable;
	private Boolean isTenfFuture;
	private Date isTenfApplicableFrom;
	private Date isTenfApplicableTo;
	private Boolean isPeInIndia;
	private Boolean isNoPeDocumentAvailable;
	private Date noPeDocumentApplicableFrom;
	private Date noPeDocumentApplicableTo;
	private Boolean isFixedbaseAvailbleIndia;
	private Date isFixedbaseApplicableFrom;
	private Date isFixedbaseApplicableTo;
	private String stayPeriodFinancialYear;
	private Boolean isNoFixedBaseDeclaration;
	private Boolean isPoemOfDeductee;
	private Boolean isNoPOEMAvailable;
	private Boolean isNoPOEMDeclarationInFuture;
	private Date isNoPOEMApplicableFrom;
	private Date isNoPOEMApplicableTo;
	private Boolean beneficialOwnerOfIncome;
	private Boolean isBeneficialOwnership;
	private String natureOfPaymentOrRemittance;
	private String poNumber;
	private Date poDate;
	private String erpDocumentNumber;
	private Date documentPostingDate;
	private String vendorDocumentNumber;
	private Date vendorDocumentDate;
	private BigDecimal amountInForeignCurrency;
	private String currency;
	private BigDecimal amountInINR;
	private BigDecimal amountOfincomeTax;
	private Boolean isLDCApplied;
	private String certificateNumber;
	private BigDecimal rateOfLDC;
	private BigDecimal rateAsPerDTAA;
	private String articleOfDTAA;
	private Date dateOfDeductionOfTax;
	private Date dateOfDepositOfTax;
	private BigDecimal tdsAmount;
	private BigDecimal surcharge;
	private BigDecimal educationCess;
	private BigDecimal interest;
	private BigDecimal fee;
	private BigDecimal tdsAmountInForeignCurrency;
	private Date dateOfRemittance;
	private Boolean whetherIncomeReceived;
	private Boolean isGrossedUp;
	private String modeOfDeposit;
	private String bsrCode;
	private String challanSerialNumber;
	private Boolean mliOrPptConditionSatisfied;
	private Boolean mliOrSlobConditionSatisfied;
	private Boolean isMliOrPptSlob;
	private Boolean isGAARComplaint;
	private String documentType;
	private Boolean challanPaid;
	private Date challanGeneratedDate;
	private String advanceCanAdjust;
	private String provisionCanAdjust;

	public String getDeductorPan() {
		return deductorPan;
	}

	public void setDeductorPan(String deductorPan) {
		this.deductorPan = deductorPan;
	}

	public String getDeductorTan() {
		return deductorTan;
	}

	public void setDeductorTan(String deductorTan) {
		this.deductorTan = deductorTan;
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

	public String getDeducteeStatus() {
		return deducteeStatus;
	}

	public void setDeducteeStatus(String deducteeStatus) {
		this.deducteeStatus = deducteeStatus;
	}

	public String getFlatOrDoorOrBlockNo() {
		return flatOrDoorOrBlockNo;
	}

	public void setFlatOrDoorOrBlockNo(String flatOrDoorOrBlockNo) {
		this.flatOrDoorOrBlockNo = flatOrDoorOrBlockNo;
	}

	public String getNameOrBuilding() {
		return nameOrBuilding;
	}

	public void setNameOrBuilding(String nameOrBuilding) {
		this.nameOrBuilding = nameOrBuilding;
	}

	public String getRoadOrStreet() {
		return roadOrStreet;
	}

	public void setRoadOrStreet(String roadOrStreet) {
		this.roadOrStreet = roadOrStreet;
	}

	public String getAreaOrLocality() {
		return areaOrLocality;
	}

	public void setAreaOrLocality(String areaOrLocality) {
		this.areaOrLocality = areaOrLocality;
	}

	public String getTownOrCityOrDistrict() {
		return townOrCityOrDistrict;
	}

	public void setTownOrCityOrDistrict(String townOrCityOrDistrict) {
		this.townOrCityOrDistrict = townOrCityOrDistrict;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public String getTin() {
		return tin;
	}

	public void setTin(String tin) {
		this.tin = tin;
	}

	public String getCountryToRemittance() {
		return countryToRemittance;
	}

	public void setCountryToRemittance(String countryToRemittance) {
		this.countryToRemittance = countryToRemittance;
	}

	public Boolean getIsTRCAvailable() {
		return isTRCAvailable;
	}

	public void setIsTRCAvailable(Boolean isTRCAvailable) {
		this.isTRCAvailable = isTRCAvailable;
	}

	public Boolean getIstrcFuture() {
		return istrcFuture;
	}

	public void setIstrcFuture(Boolean istrcFuture) {
		this.istrcFuture = istrcFuture;
	}

	public Date getIsTRCApplicableFrom() {
		return isTRCApplicableFrom;
	}

	public void setIsTRCApplicableFrom(Date isTRCApplicableFrom) {
		this.isTRCApplicableFrom = isTRCApplicableFrom;
	}

	public Date getIsTRCApplicableTo() {
		return isTRCApplicableTo;
	}

	public void setIsTRCApplicableTo(Date isTRCApplicableTo) {
		this.isTRCApplicableTo = isTRCApplicableTo;
	}

	public Boolean getIsTenfAvailable() {
		return isTenfAvailable;
	}

	public void setIsTenfAvailable(Boolean isTenfAvailable) {
		this.isTenfAvailable = isTenfAvailable;
	}

	public Boolean getIsTenfFuture() {
		return isTenfFuture;
	}

	public void setIsTenfFuture(Boolean isTenfFuture) {
		this.isTenfFuture = isTenfFuture;
	}

	public Date getIsTenfApplicableFrom() {
		return isTenfApplicableFrom;
	}

	public void setIsTenfApplicableFrom(Date isTenfApplicableFrom) {
		this.isTenfApplicableFrom = isTenfApplicableFrom;
	}

	public Date getIsTenfApplicableTo() {
		return isTenfApplicableTo;
	}

	public void setIsTenfApplicableTo(Date isTenfApplicableTo) {
		this.isTenfApplicableTo = isTenfApplicableTo;
	}

	public Boolean getIsPeInIndia() {
		return isPeInIndia;
	}

	public void setIsPeInIndia(Boolean isPeInIndia) {
		this.isPeInIndia = isPeInIndia;
	}

	public Boolean getIsNoPeDocumentAvailable() {
		return isNoPeDocumentAvailable;
	}

	public void setIsNoPeDocumentAvailable(Boolean isNoPeDocumentAvailable) {
		this.isNoPeDocumentAvailable = isNoPeDocumentAvailable;
	}

	public Date getNoPeDocumentApplicableFrom() {
		return noPeDocumentApplicableFrom;
	}

	public void setNoPeDocumentApplicableFrom(Date noPeDocumentApplicableFrom) {
		this.noPeDocumentApplicableFrom = noPeDocumentApplicableFrom;
	}

	public Date getNoPeDocumentApplicableTo() {
		return noPeDocumentApplicableTo;
	}

	public void setNoPeDocumentApplicableTo(Date noPeDocumentApplicableTo) {
		this.noPeDocumentApplicableTo = noPeDocumentApplicableTo;
	}

	public Boolean getIsFixedbaseAvailbleIndia() {
		return isFixedbaseAvailbleIndia;
	}

	public void setIsFixedbaseAvailbleIndia(Boolean isFixedbaseAvailbleIndia) {
		this.isFixedbaseAvailbleIndia = isFixedbaseAvailbleIndia;
	}

	public Date getIsFixedbaseApplicableFrom() {
		return isFixedbaseApplicableFrom;
	}

	public void setIsFixedbaseApplicableFrom(Date isFixedbaseApplicableFrom) {
		this.isFixedbaseApplicableFrom = isFixedbaseApplicableFrom;
	}

	public Date getIsFixedbaseApplicableTo() {
		return isFixedbaseApplicableTo;
	}

	public void setIsFixedbaseApplicableTo(Date isFixedbaseApplicableTo) {
		this.isFixedbaseApplicableTo = isFixedbaseApplicableTo;
	}

	public String getStayPeriodFinancialYear() {
		return stayPeriodFinancialYear;
	}

	public void setStayPeriodFinancialYear(String stayPeriodFinancialYear) {
		this.stayPeriodFinancialYear = stayPeriodFinancialYear;
	}

	public Boolean getIsNoFixedBaseDeclaration() {
		return isNoFixedBaseDeclaration;
	}

	public void setIsNoFixedBaseDeclaration(Boolean isNoFixedBaseDeclaration) {
		this.isNoFixedBaseDeclaration = isNoFixedBaseDeclaration;
	}

	public Boolean getIsPoemOfDeductee() {
		return isPoemOfDeductee;
	}

	public void setIsPoemOfDeductee(Boolean isPoemOfDeductee) {
		this.isPoemOfDeductee = isPoemOfDeductee;
	}

	public Boolean getIsNoPOEMAvailable() {
		return isNoPOEMAvailable;
	}

	public void setIsNoPOEMAvailable(Boolean isNoPOEMAvailable) {
		this.isNoPOEMAvailable = isNoPOEMAvailable;
	}

	public Boolean getIsNoPOEMDeclarationInFuture() {
		return isNoPOEMDeclarationInFuture;
	}

	public void setIsNoPOEMDeclarationInFuture(Boolean isNoPOEMDeclarationInFuture) {
		this.isNoPOEMDeclarationInFuture = isNoPOEMDeclarationInFuture;
	}

	public Date getIsNoPOEMApplicableFrom() {
		return isNoPOEMApplicableFrom;
	}

	public void setIsNoPOEMApplicableFrom(Date isNoPOEMApplicableFrom) {
		this.isNoPOEMApplicableFrom = isNoPOEMApplicableFrom;
	}

	public Date getIsNoPOEMApplicableTo() {
		return isNoPOEMApplicableTo;
	}

	public void setIsNoPOEMApplicableTo(Date isNoPOEMApplicableTo) {
		this.isNoPOEMApplicableTo = isNoPOEMApplicableTo;
	}

	public Boolean getBeneficialOwnerOfIncome() {
		return beneficialOwnerOfIncome;
	}

	public void setBeneficialOwnerOfIncome(Boolean beneficialOwnerOfIncome) {
		this.beneficialOwnerOfIncome = beneficialOwnerOfIncome;
	}

	public Boolean getIsBeneficialOwnership() {
		return isBeneficialOwnership;
	}

	public void setIsBeneficialOwnership(Boolean isBeneficialOwnership) {
		this.isBeneficialOwnership = isBeneficialOwnership;
	}

	public String getNatureOfPaymentOrRemittance() {
		return natureOfPaymentOrRemittance;
	}

	public void setNatureOfPaymentOrRemittance(String natureOfPaymentOrRemittance) {
		this.natureOfPaymentOrRemittance = natureOfPaymentOrRemittance;
	}

	public String getPoNumber() {
		return poNumber;
	}

	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}

	public Date getPoDate() {
		return poDate;
	}

	public void setPoDate(Date poDate) {
		this.poDate = poDate;
	}

	public String getErpDocumentNumber() {
		return erpDocumentNumber;
	}

	public void setErpDocumentNumber(String erpDocumentNumber) {
		this.erpDocumentNumber = erpDocumentNumber;
	}

	public Date getDocumentPostingDate() {
		return documentPostingDate;
	}

	public void setDocumentPostingDate(Date documentPostingDate) {
		this.documentPostingDate = documentPostingDate;
	}

	public String getVendorDocumentNumber() {
		return vendorDocumentNumber;
	}

	public void setVendorDocumentNumber(String vendorDocumentNumber) {
		this.vendorDocumentNumber = vendorDocumentNumber;
	}

	public Date getVendorDocumentDate() {
		return vendorDocumentDate;
	}

	public void setVendorDocumentDate(Date vendorDocumentDate) {
		this.vendorDocumentDate = vendorDocumentDate;
	}

	public BigDecimal getAmountInForeignCurrency() {
		return (amountInForeignCurrency != null ? amountInForeignCurrency : BigDecimal.ZERO).setScale(2,
				RoundingMode.UP);
	}

	public void setAmountInForeignCurrency(BigDecimal amountInForeignCurrency) {
		this.amountInForeignCurrency = (amountInForeignCurrency != null ? amountInForeignCurrency : BigDecimal.ZERO);
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BigDecimal getAmountInINR() {
		return (amountInINR != null ? amountInINR : BigDecimal.ZERO).setScale(2, RoundingMode.UP);
	}

	public void setAmountInINR(BigDecimal amountInINR) {
		this.amountInINR = (amountInINR != null ? amountInINR : BigDecimal.ZERO);
	}

	public BigDecimal getAmountOfincomeTax() {
		return (amountOfincomeTax != null ? amountOfincomeTax : BigDecimal.ZERO).setScale(2, RoundingMode.UP);
	}

	public void setAmountOfincomeTax(BigDecimal amountOfincomeTax) {
		this.amountOfincomeTax = (amountOfincomeTax != null ? amountOfincomeTax : BigDecimal.ZERO);
	}

	public Boolean getIsLDCApplied() {
		return isLDCApplied;
	}

	public void setIsLDCApplied(Boolean isLDCApplied) {
		this.isLDCApplied = isLDCApplied;
	}

	public String getCertificateNumber() {
		return certificateNumber;
	}

	public void setCertificateNumber(String certificateNumber) {
		this.certificateNumber = certificateNumber;
	}

	public BigDecimal getRateOfLDC() {
		return (rateOfLDC != null ? rateOfLDC : BigDecimal.ZERO);
	}

	public void setRateOfLDC(BigDecimal rateOfLDC) {
		this.rateOfLDC = (rateOfLDC != null ? rateOfLDC : BigDecimal.ZERO);
	}

	public BigDecimal getRateAsPerDTAA() {
		return (rateAsPerDTAA != null ? rateAsPerDTAA : BigDecimal.ZERO);
	}

	public void setRateAsPerDTAA(BigDecimal rateAsPerDTAA) {
		this.rateAsPerDTAA = (rateAsPerDTAA != null ? rateAsPerDTAA : BigDecimal.ZERO);
	}

	public String getArticleOfDTAA() {
		return articleOfDTAA;
	}

	public void setArticleOfDTAA(String articleOfDTAA) {
		this.articleOfDTAA = articleOfDTAA;
	}

	public Date getDateOfDeductionOfTax() {
		return dateOfDeductionOfTax;
	}

	public void setDateOfDeductionOfTax(Date dateOfDeductionOfTax) {
		this.dateOfDeductionOfTax = dateOfDeductionOfTax;
	}

	public Date getDateOfDepositOfTax() {
		return dateOfDepositOfTax;
	}

	public void setDateOfDepositOfTax(Date dateOfDepositOfTax) {
		this.dateOfDepositOfTax = dateOfDepositOfTax;
	}

	public BigDecimal getTdsAmount() {
		return (tdsAmount != null ? tdsAmount : BigDecimal.ZERO).setScale(2, RoundingMode.UP);
	}

	public void setTdsAmount(BigDecimal tdsAmount) {
		this.tdsAmount = (tdsAmount != null ? tdsAmount : BigDecimal.ZERO);
	}

	public BigDecimal getSurcharge() {
		return (surcharge != null ? surcharge : BigDecimal.ZERO).setScale(2, RoundingMode.UP);
	}

	public void setSurcharge(BigDecimal surcharge) {
		this.surcharge = (surcharge != null ? surcharge : BigDecimal.ZERO);
	}

	public BigDecimal getEducationCess() {
		return (educationCess != null ? educationCess : BigDecimal.ZERO).setScale(2, RoundingMode.UP);
	}

	public void setEducationCess(BigDecimal educationCess) {
		this.educationCess = (educationCess != null ? educationCess : BigDecimal.ZERO);
	}

	public BigDecimal getInterest() {
		return (interest != null ? interest : BigDecimal.ZERO).setScale(2, RoundingMode.UP);
	}

	public void setInterest(BigDecimal interest) {
		this.interest = (interest != null ? interest : BigDecimal.ZERO);
	}

	public BigDecimal getFee() {
		return (fee != null ? fee : BigDecimal.ZERO).setScale(2, RoundingMode.UP);
	}

	public void setFee(BigDecimal fee) {
		this.fee = (fee != null ? fee : BigDecimal.ZERO);
	}

	public BigDecimal getTdsAmountInForeignCurrency() {
		return (tdsAmountInForeignCurrency != null ? tdsAmountInForeignCurrency : BigDecimal.ZERO).setScale(2,
				RoundingMode.UP);
	}

	public void setTdsAmountInForeignCurrency(BigDecimal tdsAmountInForeignCurrency) {
		this.tdsAmountInForeignCurrency = (tdsAmountInForeignCurrency != null ? tdsAmountInForeignCurrency
				: BigDecimal.ZERO);
	}

	public Date getDateOfRemittance() {
		return dateOfRemittance;
	}

	public void setDateOfRemittance(Date dateOfRemittance) {
		this.dateOfRemittance = dateOfRemittance;
	}

	public Boolean getWhetherIncomeReceived() {
		return whetherIncomeReceived;
	}

	public void setWhetherIncomeReceived(Boolean whetherIncomeReceived) {
		this.whetherIncomeReceived = whetherIncomeReceived;
	}

	public Boolean getIsGrossedUp() {
		return isGrossedUp;
	}

	public void setIsGrossedUp(Boolean isGrossedUp) {
		this.isGrossedUp = isGrossedUp;
	}

	public String getModeOfDeposit() {
		return modeOfDeposit;
	}

	public void setModeOfDeposit(String modeOfDeposit) {
		this.modeOfDeposit = modeOfDeposit;
	}

	public String getBsrCode() {
		return bsrCode;
	}

	public void setBsrCode(String bsrCode) {
		this.bsrCode = bsrCode;
	}

	public String getChallanSerialNumber() {
		return challanSerialNumber;
	}

	public void setChallanSerialNumber(String challanSerialNumber) {
		this.challanSerialNumber = challanSerialNumber;
	}

	public Boolean getMliOrPptConditionSatisfied() {
		return mliOrPptConditionSatisfied;
	}

	public void setMliOrPptConditionSatisfied(Boolean mliOrPptConditionSatisfied) {
		this.mliOrPptConditionSatisfied = mliOrPptConditionSatisfied;
	}

	public Boolean getMliOrSlobConditionSatisfied() {
		return mliOrSlobConditionSatisfied;
	}

	public void setMliOrSlobConditionSatisfied(Boolean mliOrSlobConditionSatisfied) {
		this.mliOrSlobConditionSatisfied = mliOrSlobConditionSatisfied;
	}

	public Boolean getIsMliOrPptSlob() {
		return isMliOrPptSlob;
	}

	public void setIsMliOrPptSlob(Boolean isMliOrPptSlob) {
		this.isMliOrPptSlob = isMliOrPptSlob;
	}

	public Boolean getIsGAARComplaint() {
		return isGAARComplaint;
	}

	public void setIsGAARComplaint(Boolean isGAARComplaint) {
		this.isGAARComplaint = isGAARComplaint;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public Boolean getChallanPaid() {
		return challanPaid;
	}

	public void setChallanPaid(Boolean challanPaid) {
		this.challanPaid = challanPaid;
	}

	public Date getChallanGeneratedDate() {
		return challanGeneratedDate;
	}

	public void setChallanGeneratedDate(Date challanGeneratedDate) {
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

}
