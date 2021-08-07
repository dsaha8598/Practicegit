package com.ey.in.tds.returns.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Form15CAPartCDTO implements Serializable {
    private static final long serialVersionUID = -8449702596386556635L;

    private Integer id;
    
    private Integer shareHolderId;

    private String folioNo;

    private String form15CBAcknowledgementNumber;

    private String remitterName;

    private String remitterPan;

    private String remitterTan;

    private String remitterAreaCode;

    private String remitterAOType;

    private String remitterRangeCode;

    private String remitterAONumber;

    private String remitterPrincipalAreaOfBusiness;

    private String remitterStatus;

    private String remitterResidentialStatus;

    private String remitterFlatDoorBlockNo;

    private String remitterNameBuildingVillage;

    private String remitterRoadStreetPostoffice;

    private String remitterAreaLocality;

    private String remitterTownCityDistrict;

    private String remitterState;

    private String remitterCountry;

    private String remitterPinCode;

    private String remitterEmail;

    private String remitterPhoneNumber;

    private String shareholderName;

    private String shareholderPan;

    private String shareholderStatus;

    private String shareHolderRemittanceCountry;

    private String shareholderPrincipalPlaceOfBusiness;

    private String shareholderEmail;

    private String shareholderIsdCodePhoneNumber;

    private String flatDoorBlockNo;

    private String nameBuildingVillage;

    private String roadStreetPostoffice;

    private String areaLocality;

    private String townCityDistrict;

    private String state;

    private String country;

    private String pinCode;

    private String accountantName;

    private String caNameOfProprietorship;

    private String accountantFlatDoorBlockNo;

    private String accountantNameBuildingVillage;

    private String accountantRoadStreetPostoffice;

    private String accountantAreaLocality;

    private String accountantTownCityDistrict;

    private String accountantState;

    private String accountantCountry;

    private String accountantPinCode;

    private String caMembershipNumber;

    private String caRegistrationNumber;

    private String certificateNumber;

    private Date certificateNumberDate;

    private Boolean isOrderOrCertificateObtainedFromAO;

    private String orderOrCertificateSection;

    private String assessingOfficerName;

    private String assessingOfficerDesignation;

    private String orderOrCertificateDate;

    private String orderOrCertificateNumber;

    private String remittanceCountry;

    private String currency;

    private String amountPayableInForeignCurrency;

    private BigDecimal amountPayableInInr;

    private String nameOfBank;

    private String branchOfBank;

    private String bsrCodeOfBankBranch;

    private Date proposedDateOfRemittance;

    private String natureOfRemittance;

    private String rbiPurposeCode;

    private Boolean isTaxPayableGrossedUp;

    private String remittanceSectionOfAct;

    private BigDecimal incomeChargeableToTax;

    private BigDecimal taxLiability;

    private String basisOfIncomeAndTaxLiability;

    private Boolean isRemittanceRecipientTRCAvailable;

    private String relevantDTAA;

    private String relevantDTAAArticle;

    private BigDecimal taxableIncomePerDTAA;

    private BigDecimal taxLiabilityPerDTAA;

    private Boolean isRemittanceForRoyalties;

    private String dTAAArticle;

    private BigDecimal tdsRatePerDTAA;

    private String tdsAmountInForeignCurrency;

    private BigDecimal tdsAmountInInr;

    private BigDecimal ratePerDTAA;

    private BigDecimal tdsRate;

    private String actualRemittanceAmountAfterTdsInForeignCurrency;

    private Date taxAtSourceDeductionDate;
    
    private BigDecimal taxableIncomeAsPerDATAA;
    
    private BigDecimal theTaxableLiability;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getShareHolderId() {
		return shareHolderId;
	}

	public void setShareHolderId(Integer shareHolderId) {
		this.shareHolderId = shareHolderId;
	}

	public String getForm15CBAcknowledgementNumber() {
        return form15CBAcknowledgementNumber;
    }

    public void setForm15CBAcknowledgementNumber(String form15CBAcknowledgementNumber) {
        this.form15CBAcknowledgementNumber = form15CBAcknowledgementNumber;
    }

    public String getCaMembershipNumber() {
        return caMembershipNumber;
    }

    public void setCaMembershipNumber(String caMembershipNumber) {
        this.caMembershipNumber = caMembershipNumber;
    }

    public String getRemitterName() {
        return remitterName;
    }

    public void setRemitterName(String remitterName) {
        this.remitterName = remitterName;
    }

    public String getRemitterPan() {
        return remitterPan;
    }

    public void setRemitterPan(String remitterPan) {
        this.remitterPan = remitterPan;
    }

    public String getRemitterTan() {
        return remitterTan;
    }

    public void setRemitterTan(String remitterTan) {
        this.remitterTan = remitterTan;
    }

    public String getRemitterAreaCode() {
        return remitterAreaCode;
    }

    public void setRemitterAreaCode(String remitterAreaCode) {
        this.remitterAreaCode = remitterAreaCode;
    }

    public String getRemitterAOType() {
        return remitterAOType;
    }

    public void setRemitterAOType(String remitterAOType) {
        this.remitterAOType = remitterAOType;
    }

    public String getRemitterRangeCode() {
        return remitterRangeCode;
    }

    public void setRemitterRangeCode(String remitterRangeCode) {
        this.remitterRangeCode = remitterRangeCode;
    }

    public String getRemitterAONumber() {
        return remitterAONumber;
    }

    public void setRemitterAONumber(String remitterAONumber) {
        this.remitterAONumber = remitterAONumber;
    }

    public String getRemitterPrincipalAreaOfBusiness() {
        return remitterPrincipalAreaOfBusiness;
    }

    public void setRemitterPrincipalAreaOfBusiness(String remitterPrincipalAreaOfBusiness) {
        this.remitterPrincipalAreaOfBusiness = remitterPrincipalAreaOfBusiness;
    }

    public String getRemitterStatus() {
        return remitterStatus;
    }

    public void setRemitterStatus(String remitterStatus) {
        this.remitterStatus = remitterStatus;
    }

    public String getRemitterResidentialStatus() {
        return remitterResidentialStatus;
    }

    public void setRemitterResidentialStatus(String remitterResidentialStatus) {
        this.remitterResidentialStatus = remitterResidentialStatus;
    }

    public String getRemitterFlatDoorBlockNo() {
        return remitterFlatDoorBlockNo;
    }

    public void setRemitterFlatDoorBlockNo(String remitterFlatDoorBlockNo) {
        this.remitterFlatDoorBlockNo = remitterFlatDoorBlockNo;
    }

    public String getRemitterNameBuildingVillage() {
        return remitterNameBuildingVillage;
    }

    public void setRemitterNameBuildingVillage(String remitterNameBuildingVillage) {
        this.remitterNameBuildingVillage = remitterNameBuildingVillage;
    }

    public String getRemitterRoadStreetPostoffice() {
        return remitterRoadStreetPostoffice;
    }

    public String getOrderOrCertificateSection() {
        return orderOrCertificateSection;
    }

    public void setOrderOrCertificateSection(String orderOrCertificateSection) {
        this.orderOrCertificateSection = orderOrCertificateSection;
    }

    public void setRemitterRoadStreetPostoffice(String remitterRoadStreetPostoffice) {
        this.remitterRoadStreetPostoffice = remitterRoadStreetPostoffice;
    }

    public String getRemitterAreaLocality() {
        return remitterAreaLocality;
    }

    public void setRemitterAreaLocality(String remitterAreaLocality) {
        this.remitterAreaLocality = remitterAreaLocality;
    }

    public String getRemitterTownCityDistrict() {
        return remitterTownCityDistrict;
    }

    public void setRemitterTownCityDistrict(String remitterTownCityDistrict) {
        this.remitterTownCityDistrict = remitterTownCityDistrict;
    }

    public String getRemitterState() {
        return remitterState;
    }

    public void setRemitterState(String remitterState) {
        this.remitterState = remitterState;
    }

    public String getRemitterCountry() {
        return remitterCountry;
    }

    public void setRemitterCountry(String remitterCountry) {
        this.remitterCountry = remitterCountry;
    }

    public String getRemitterPinCode() {
        return remitterPinCode;
    }

    public void setRemitterPinCode(String remitterPinCode) {
        this.remitterPinCode = remitterPinCode;
    }

    public String getRemitterEmail() {
        return remitterEmail;
    }

    public void setRemitterEmail(String remitterEmail) {
        this.remitterEmail = remitterEmail;
    }

    public String getRemitterPhoneNumber() {
        return remitterPhoneNumber;
    }

    public void setRemitterPhoneNumber(String remitterPhoneNumber) {
        this.remitterPhoneNumber = remitterPhoneNumber;
    }

    public String getShareholderName() {
        return shareholderName;
    }

    public void setShareholderName(String shareholderName) {
        this.shareholderName = shareholderName;
    }

    public String getShareholderPan() {
        return shareholderPan;
    }

    public void setShareholderPan(String shareholderPan) {
        this.shareholderPan = shareholderPan;
    }

    public String getShareholderStatus() {
        return shareholderStatus;
    }

    public void setShareholderStatus(String shareholderStatus) {
        this.shareholderStatus = shareholderStatus;
    }

    public String getShareHolderRemittanceCountry() {
        return shareHolderRemittanceCountry;
    }

    public void setShareHolderRemittanceCountry(String shareHolderRemittanceCountry) {
        this.shareHolderRemittanceCountry = shareHolderRemittanceCountry;
    }

    public String getShareholderPrincipalPlaceOfBusiness() {
        return shareholderPrincipalPlaceOfBusiness;
    }

    public void setShareholderPrincipalPlaceOfBusiness(String shareholderPrincipalPlaceOfBusiness) {
        this.shareholderPrincipalPlaceOfBusiness = shareholderPrincipalPlaceOfBusiness;
    }

    public String getShareholderEmail() {
        return shareholderEmail;
    }

    public void setShareholderEmail(String shareholderEmail) {
        this.shareholderEmail = shareholderEmail;
    }

    public String getShareholderIsdCodePhoneNumber() {
        return shareholderIsdCodePhoneNumber;
    }

    public void setShareholderIsdCodePhoneNumber(String shareholderIsdCodePhoneNumber) {
        this.shareholderIsdCodePhoneNumber = shareholderIsdCodePhoneNumber;
    }

    public String getFlatDoorBlockNo() {
        return flatDoorBlockNo;
    }

    public void setFlatDoorBlockNo(String flatDoorBlockNo) {
        this.flatDoorBlockNo = flatDoorBlockNo;
    }

    public String getNameBuildingVillage() {
        return nameBuildingVillage;
    }

    public void setNameBuildingVillage(String nameBuildingVillage) {
        this.nameBuildingVillage = nameBuildingVillage;
    }

    public String getRoadStreetPostoffice() {
        return roadStreetPostoffice;
    }

    public void setRoadStreetPostoffice(String roadStreetPostoffice) {
        this.roadStreetPostoffice = roadStreetPostoffice;
    }

    public String getAreaLocality() {
        return areaLocality;
    }

    public void setAreaLocality(String areaLocality) {
        this.areaLocality = areaLocality;
    }

    public String getTownCityDistrict() {
        return townCityDistrict;
    }

    public void setTownCityDistrict(String townCityDistrict) {
        this.townCityDistrict = townCityDistrict;
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

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getAccountantName() {
        return accountantName;
    }

    public void setAccountantName(String accountantName) {
        this.accountantName = accountantName;
    }

    public String getCaNameOfProprietorship() {
        return caNameOfProprietorship;
    }

    public void setCaNameOfProprietorship(String caNameOfProprietorship) {
        this.caNameOfProprietorship = caNameOfProprietorship;
    }

    public String getAccountantFlatDoorBlockNo() {
        return accountantFlatDoorBlockNo;
    }

    public void setAccountantFlatDoorBlockNo(String accountantFlatDoorBlockNo) {
        this.accountantFlatDoorBlockNo = accountantFlatDoorBlockNo;
    }

    public String getAccountantNameBuildingVillage() {
        return accountantNameBuildingVillage;
    }

    public void setAccountantNameBuildingVillage(String accountantNameBuildingVillage) {
        this.accountantNameBuildingVillage = accountantNameBuildingVillage;
    }

    public String getAccountantRoadStreetPostoffice() {
        return accountantRoadStreetPostoffice;
    }

    public void setAccountantRoadStreetPostoffice(String accountantRoadStreetPostoffice) {
        this.accountantRoadStreetPostoffice = accountantRoadStreetPostoffice;
    }

    public String getAccountantAreaLocality() {
        return accountantAreaLocality;
    }

    public void setAccountantAreaLocality(String accountantAreaLocality) {
        this.accountantAreaLocality = accountantAreaLocality;
    }

    public String getAccountantTownCityDistrict() {
        return accountantTownCityDistrict;
    }

    public void setAccountantTownCityDistrict(String accountantTownCityDistrict) {
        this.accountantTownCityDistrict = accountantTownCityDistrict;
    }

    public String getAccountantState() {
        return accountantState;
    }

    public void setAccountantState(String accountantState) {
        this.accountantState = accountantState;
    }

    public String getAccountantCountry() {
        return accountantCountry;
    }

    public void setAccountantCountry(String accountantCountry) {
        this.accountantCountry = accountantCountry;
    }

    public String getAccountantPinCode() {
        return accountantPinCode;
    }

    public void setAccountantPinCode(String accountantPinCode) {
        this.accountantPinCode = accountantPinCode;
    }

    public String getCaRegistrationNumber() {
        return caRegistrationNumber;
    }

    public void setCaRegistrationNumber(String caRegistrationNumber) {
        this.caRegistrationNumber = caRegistrationNumber;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public Date getCertificateNumberDate() {
        return certificateNumberDate;
    }

    public void setCertificateNumberDate(Date certificateNumberDate) {
        this.certificateNumberDate = certificateNumberDate;
    }

    public Boolean getOrderOrCertificateObtainedFromAO() {
        return isOrderOrCertificateObtainedFromAO;
    }

    public void setOrderOrCertificateObtainedFromAO(Boolean orderOrCertificateObtainedFromAO) {
        isOrderOrCertificateObtainedFromAO = orderOrCertificateObtainedFromAO;
    }

    public String getAssessingOfficerName() {
        return assessingOfficerName;
    }

    public void setAssessingOfficerName(String assessingOfficerName) {
        this.assessingOfficerName = assessingOfficerName;
    }

    public String getAssessingOfficerDesignation() {
        return assessingOfficerDesignation;
    }

    public void setAssessingOfficerDesignation(String assessingOfficerDesignation) {
        this.assessingOfficerDesignation = assessingOfficerDesignation;
    }

    public String getOrderOrCertificateDate() {
        return orderOrCertificateDate;
    }

    public void setOrderOrCertificateDate(String orderOrCertificateDate) {
        this.orderOrCertificateDate = orderOrCertificateDate;
    }

    public String getOrderOrCertificateNumber() {
        return orderOrCertificateNumber;
    }

    public void setOrderOrCertificateNumber(String orderOrCertificateNumber) {
        this.orderOrCertificateNumber = orderOrCertificateNumber;
    }

    public String getRemittanceCountry() {
        return remittanceCountry;
    }

    public void setRemittanceCountry(String remittanceCountry) {
        this.remittanceCountry = remittanceCountry;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmountPayableInForeignCurrency() {
        return amountPayableInForeignCurrency;
    }

    public void setAmountPayableInForeignCurrency(String amountPayableInForeignCurrency) {
        this.amountPayableInForeignCurrency = amountPayableInForeignCurrency;
    }

    public BigDecimal getAmountPayableInInr() {
        return amountPayableInInr;
    }

    public void setAmountPayableInInr(BigDecimal amountPayableInInr) {
        this.amountPayableInInr = amountPayableInInr;
    }

    public String getNameOfBank() {
        return nameOfBank;
    }

    public void setNameOfBank(String nameOfBank) {
        this.nameOfBank = nameOfBank;
    }

    public String getBranchOfBank() {
        return branchOfBank;
    }

    public void setBranchOfBank(String branchOfBank) {
        this.branchOfBank = branchOfBank;
    }

    public String getBsrCodeOfBankBranch() {
        return bsrCodeOfBankBranch;
    }

    public void setBsrCodeOfBankBranch(String bsrCodeOfBankBranch) {
        this.bsrCodeOfBankBranch = bsrCodeOfBankBranch;
    }

    public Date getProposedDateOfRemittance() {
        return proposedDateOfRemittance;
    }

    public void setProposedDateOfRemittance(Date proposedDateOfRemittance) {
        this.proposedDateOfRemittance = proposedDateOfRemittance;
    }

    public String getNatureOfRemittance() {
        return natureOfRemittance;
    }

    public void setNatureOfRemittance(String natureOfRemittance) {
        this.natureOfRemittance = natureOfRemittance;
    }

    public String getRbiPurposeCode() {
        return rbiPurposeCode;
    }

    public void setRbiPurposeCode(String rbiPurposeCode) {
        this.rbiPurposeCode = rbiPurposeCode;
    }

    public Boolean getTaxPayableGrossedUp() {
        return isTaxPayableGrossedUp;
    }

    public void setTaxPayableGrossedUp(Boolean taxPayableGrossedUp) {
        isTaxPayableGrossedUp = taxPayableGrossedUp;
    }

    public String getRemittanceSectionOfAct() {
        return remittanceSectionOfAct;
    }

    public void setRemittanceSectionOfAct(String remittanceSectionOfAct) {
        this.remittanceSectionOfAct = remittanceSectionOfAct;
    }

    public BigDecimal getIncomeChargeableToTax() {
        return incomeChargeableToTax;
    }

    public void setIncomeChargeableToTax(BigDecimal incomeChargeableToTax) {
        this.incomeChargeableToTax = incomeChargeableToTax;
    }

    public BigDecimal getTaxLiability() {
        return taxLiability;
    }

    public void setTaxLiability(BigDecimal taxLiability) {
        this.taxLiability = taxLiability;
    }

    public String getBasisOfIncomeAndTaxLiability() {
        return basisOfIncomeAndTaxLiability;
    }

    public void setBasisOfIncomeAndTaxLiability(String basisOfIncomeAndTaxLiability) {
        this.basisOfIncomeAndTaxLiability = basisOfIncomeAndTaxLiability;
    }

    public Boolean getRemittanceRecipientTRCAvailable() {
        return isRemittanceRecipientTRCAvailable;
    }

    public void setRemittanceRecipientTRCAvailable(Boolean remittanceRecipientTRCAvailable) {
        isRemittanceRecipientTRCAvailable = remittanceRecipientTRCAvailable;
    }

    public String getRelevantDTAA() {
        return relevantDTAA;
    }

    public void setRelevantDTAA(String relevantDTAA) {
        this.relevantDTAA = relevantDTAA;
    }

    public String getRelevantDTAAArticle() {
        return relevantDTAAArticle;
    }

    public void setRelevantDTAAArticle(String relevantDTAAArticle) {
        this.relevantDTAAArticle = relevantDTAAArticle;
    }

    public BigDecimal getTaxableIncomePerDTAA() {
        return taxableIncomePerDTAA;
    }

    public void setTaxableIncomePerDTAA(BigDecimal taxableIncomePerDTAA) {
        this.taxableIncomePerDTAA = taxableIncomePerDTAA;
    }

    public BigDecimal getTaxLiabilityPerDTAA() {
        return taxLiabilityPerDTAA;
    }

    public void setTaxLiabilityPerDTAA(BigDecimal taxLiabilityPerDTAA) {
        this.taxLiabilityPerDTAA = taxLiabilityPerDTAA;
    }

    public Boolean getRemittanceForRoyalties() {
        return isRemittanceForRoyalties;
    }

    public void setRemittanceForRoyalties(Boolean remittanceForRoyalties) {
        isRemittanceForRoyalties = remittanceForRoyalties;
    }

    public String getdTAAArticle() {
        return dTAAArticle;
    }

    public void setdTAAArticle(String dTAAArticle) {
        this.dTAAArticle = dTAAArticle;
    }

    public BigDecimal getTdsRatePerDTAA() {
        return tdsRatePerDTAA;
    }

    public void setTdsRatePerDTAA(BigDecimal tdsRatePerDTAA) {
        this.tdsRatePerDTAA = tdsRatePerDTAA;
    }

    public String getTdsAmountInForeignCurrency() {
        return tdsAmountInForeignCurrency;
    }

    public void setTdsAmountInForeignCurrency(String tdsAmountInForeignCurrency) {
        this.tdsAmountInForeignCurrency = tdsAmountInForeignCurrency;
    }

    public BigDecimal getTdsAmountInInr() {
        return tdsAmountInInr;
    }

    public void setTdsAmountInInr(BigDecimal tdsAmountInInr) {
        this.tdsAmountInInr = tdsAmountInInr;
    }

    public BigDecimal getRatePerDTAA() {
        return ratePerDTAA;
    }

    public void setRatePerDTAA(BigDecimal ratePerDTAA) {
        this.ratePerDTAA = ratePerDTAA;
    }

    public BigDecimal getTdsRate() {
        return tdsRate;
    }

    public void setTdsRate(BigDecimal tdsRate) {
        this.tdsRate = tdsRate;
    }

    public String getActualRemittanceAmountAfterTdsInForeignCurrency() {
        return actualRemittanceAmountAfterTdsInForeignCurrency;
    }

    public void setActualRemittanceAmountAfterTdsInForeignCurrency(String actualRemittanceAmountAfterTdsInForeignCurrency) {
        this.actualRemittanceAmountAfterTdsInForeignCurrency = actualRemittanceAmountAfterTdsInForeignCurrency;
    }

    public Date getTaxAtSourceDeductionDate() {
        return taxAtSourceDeductionDate;
    }

    public void setTaxAtSourceDeductionDate(Date taxAtSourceDeductionDate) {
        this.taxAtSourceDeductionDate = taxAtSourceDeductionDate;
    }

    public String getFolioNo() {
        return folioNo;
    }

    public void setFolioNo(String folioNo) {
        this.folioNo = folioNo;
    }
    
	public BigDecimal getTaxableIncomeAsPerDATAA() {
		return taxableIncomeAsPerDATAA;
	}

	public void setTaxableIncomeAsPerDATAA(BigDecimal taxableIncomeAsPerDATAA) {
		this.taxableIncomeAsPerDATAA = taxableIncomeAsPerDATAA;
	}
	

	public BigDecimal getTheTaxableLiability() {
		return theTaxableLiability;
	}

	public void setTheTaxableLiability(BigDecimal theTaxableLiability) {
		this.theTaxableLiability = theTaxableLiability;
	}

	@Override
	public String toString() {
		return "Form15CAPartCDTO [id=" + id + ", folioNo=" + folioNo + ", form15CBAcknowledgementNumber="
				+ form15CBAcknowledgementNumber + ", remitterName=" + remitterName + ", remitterPan=" + remitterPan
				+ ", remitterTan=" + remitterTan + ", remitterAreaCode=" + remitterAreaCode + ", remitterAOType="
				+ remitterAOType + ", remitterRangeCode=" + remitterRangeCode + ", remitterAONumber=" + remitterAONumber
				+ ", remitterPrincipalAreaOfBusiness=" + remitterPrincipalAreaOfBusiness + ", remitterStatus="
				+ remitterStatus + ", remitterResidentialStatus=" + remitterResidentialStatus
				+ ", remitterFlatDoorBlockNo=" + remitterFlatDoorBlockNo + ", remitterNameBuildingVillage="
				+ remitterNameBuildingVillage + ", remitterRoadStreetPostoffice=" + remitterRoadStreetPostoffice
				+ ", remitterAreaLocality=" + remitterAreaLocality + ", remitterTownCityDistrict="
				+ remitterTownCityDistrict + ", remitterState=" + remitterState + ", remitterCountry=" + remitterCountry
				+ ", remitterPinCode=" + remitterPinCode + ", remitterEmail=" + remitterEmail + ", remitterPhoneNumber="
				+ remitterPhoneNumber + ", shareholderName=" + shareholderName + ", shareholderPan=" + shareholderPan
				+ ", shareholderStatus=" + shareholderStatus + ", shareHolderRemittanceCountry="
				+ shareHolderRemittanceCountry + ", shareholderPrincipalPlaceOfBusiness="
				+ shareholderPrincipalPlaceOfBusiness + ", shareholderEmail=" + shareholderEmail
				+ ", shareholderIsdCodePhoneNumber=" + shareholderIsdCodePhoneNumber + ", flatDoorBlockNo="
				+ flatDoorBlockNo + ", nameBuildingVillage=" + nameBuildingVillage + ", roadStreetPostoffice="
				+ roadStreetPostoffice + ", areaLocality=" + areaLocality + ", townCityDistrict=" + townCityDistrict
				+ ", state=" + state + ", country=" + country + ", pinCode=" + pinCode + ", accountantName="
				+ accountantName + ", caNameOfProprietorship=" + caNameOfProprietorship + ", accountantFlatDoorBlockNo="
				+ accountantFlatDoorBlockNo + ", accountantNameBuildingVillage=" + accountantNameBuildingVillage
				+ ", accountantRoadStreetPostoffice=" + accountantRoadStreetPostoffice + ", accountantAreaLocality="
				+ accountantAreaLocality + ", accountantTownCityDistrict=" + accountantTownCityDistrict
				+ ", accountantState=" + accountantState + ", accountantCountry=" + accountantCountry
				+ ", accountantPinCode=" + accountantPinCode + ", caMembershipNumber=" + caMembershipNumber
				+ ", caRegistrationNumber=" + caRegistrationNumber + ", certificateNumber=" + certificateNumber
				+ ", certificateNumberDate=" + certificateNumberDate + ", isOrderOrCertificateObtainedFromAO="
				+ isOrderOrCertificateObtainedFromAO + ", orderOrCertificateSection=" + orderOrCertificateSection
				+ ", assessingOfficerName=" + assessingOfficerName + ", assessingOfficerDesignation="
				+ assessingOfficerDesignation + ", orderOrCertificateDate=" + orderOrCertificateDate
				+ ", orderOrCertificateNumber=" + orderOrCertificateNumber + ", remittanceCountry=" + remittanceCountry
				+ ", currency=" + currency + ", amountPayableInForeignCurrency=" + amountPayableInForeignCurrency
				+ ", amountPayableInInr=" + amountPayableInInr + ", nameOfBank=" + nameOfBank + ", branchOfBank="
				+ branchOfBank + ", bsrCodeOfBankBranch=" + bsrCodeOfBankBranch + ", proposedDateOfRemittance="
				+ proposedDateOfRemittance + ", natureOfRemittance=" + natureOfRemittance + ", rbiPurposeCode="
				+ rbiPurposeCode + ", isTaxPayableGrossedUp=" + isTaxPayableGrossedUp + ", remittanceSectionOfAct="
				+ remittanceSectionOfAct + ", incomeChargeableToTax=" + incomeChargeableToTax + ", taxLiability="
				+ taxLiability + ", basisOfIncomeAndTaxLiability=" + basisOfIncomeAndTaxLiability
				+ ", isRemittanceRecipientTRCAvailable=" + isRemittanceRecipientTRCAvailable + ", relevantDTAA="
				+ relevantDTAA + ", relevantDTAAArticle=" + relevantDTAAArticle + ", taxableIncomePerDTAA="
				+ taxableIncomePerDTAA + ", taxLiabilityPerDTAA=" + taxLiabilityPerDTAA + ", isRemittanceForRoyalties="
				+ isRemittanceForRoyalties + ", dTAAArticle=" + dTAAArticle + ", tdsRatePerDTAA=" + tdsRatePerDTAA
				+ ", tdsAmountInForeignCurrency=" + tdsAmountInForeignCurrency + ", tdsAmountInInr=" + tdsAmountInInr
				+ ", ratePerDTAA=" + ratePerDTAA + ", tdsRate=" + tdsRate
				+ ", actualRemittanceAmountAfterTdsInForeignCurrency=" + actualRemittanceAmountAfterTdsInForeignCurrency
				+ ", taxAtSourceDeductionDate=" + taxAtSourceDeductionDate + "]";
	}
    
    
}
