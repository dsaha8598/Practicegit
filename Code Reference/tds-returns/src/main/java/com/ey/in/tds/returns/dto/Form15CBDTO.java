package com.ey.in.tds.returns.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.ey.in.tds.dividend.forms.builder.cb.TDSRateType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Form15CBDTO implements Serializable {
    private static final long serialVersionUID = -8449702596386556635L;

    private String folioNo;

    private String remitterName;

    private String remitterPan;

    private String shareholderSalutation;

    private String shareholderName;

    private String flatDoorBlockNo;

    private String nameBuildingVillage;

    private String roadStreetPostoffice;

    private String areaLocality;

    private String townCityDistrict;

    private String state;

    private String country;

    private String pinCode;

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

    private Boolean isRemittanceChargeableToIndiaTax;

    private String reasonsIfRemittanceNotChargeable;

    private String remittanceSectionOfAct;

    private BigDecimal incomeChargeableToTax;

    private BigDecimal taxLiability;
    
    private BigDecimal taxLiabilityAsPerDTAA;

    private String basisOfIncomeAndTaxLiability;

    private Boolean isRemittanceRecipientTRCAvailable;

    private String relevantDTAA;

    private String relevantDTAAArticle;

    private BigDecimal taxableIncomePerDTAA;

    private BigDecimal taxLiabilityPerDTAA;

    private Boolean isRemittanceForRoyalties;

    private String dTAAArticle;

    private BigDecimal tdsRatePerDTAA;

    private BigDecimal tdsAmountInForeignCurrency;

    private BigDecimal tdsAmountInInr;

    private BigDecimal rateAsPerITActDtaa;

    private BigDecimal tdsRate;

    private TDSRateType tdsRateType;

    private String actualRemittanceAmountAfterTdsInForeignCurrency;

    private Date taxAtSourceDeductionDate;

    private String accountantName;

    private String caNameOfProprietorship;

    private String caAddress;

    private String caMembershipNumber;

    private String caRegistrationNumber;

    private String accountantFlatDoorBlockNo;

    private Integer id;
    
    private String shareHolderPan;
    
    private Integer shareHolderId;
    
    public Integer getShareHolderId() {
		return shareHolderId;
	}

	public void setShareHolderId(Integer shareHolderId) {
		this.shareHolderId = shareHolderId;
	}
    public String getShareHolderPan() {
		return shareHolderPan;
	}

	public void setShareHolderPan(String shareHolderPan) {
		this.shareHolderPan = shareHolderPan;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    private String accountantNameBuildingVillage;

    private String accountantRoadStreetPostoffice;

    private String accountantAreaLocality;

    private String accountantTownCityDistrict;

    private String accountantState;

    private String accountantCountry;

    private String accountantPinCode;

    public static long getSerialVersionUID() {
        return serialVersionUID;
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

    public String getShareholderName() {
        return shareholderName;
    }

    public void setShareholderName(String shareholderName) {
        this.shareholderName = shareholderName;
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

    public Boolean getRemittanceChargeableToIndiaTax() {
        return isRemittanceChargeableToIndiaTax;
    }

    public void setRemittanceChargeableToIndiaTax(Boolean remittanceChargeableToIndiaTax) {
        isRemittanceChargeableToIndiaTax = remittanceChargeableToIndiaTax;
    }

    public String getReasonsIfRemittanceNotChargeable() {
        return reasonsIfRemittanceNotChargeable;
    }

    public void setReasonsIfRemittanceNotChargeable(String reasonsIfRemittanceNotChargeable) {
        this.reasonsIfRemittanceNotChargeable = reasonsIfRemittanceNotChargeable;
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
    
    

    public BigDecimal getTaxLiabilityAsPerDTAA() {
		return taxLiabilityAsPerDTAA;
	}

	public void setTaxLiabilityAsPerDTAA(BigDecimal taxLiabilityAsPerDTAA) {
		this.taxLiabilityAsPerDTAA = taxLiabilityAsPerDTAA;
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

    public BigDecimal getTdsAmountInForeignCurrency() {
        return tdsAmountInForeignCurrency;
    }

    public void setTdsAmountInForeignCurrency(BigDecimal tdsAmountInForeignCurrency) {
        this.tdsAmountInForeignCurrency = tdsAmountInForeignCurrency;
    }

    public BigDecimal getTdsAmountInInr() {
        return tdsAmountInInr;
    }

    public void setTdsAmountInInr(BigDecimal tdsAmountInInr) {
        this.tdsAmountInInr = tdsAmountInInr;
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

    public void setActualRemittanceAmountAfterTdsInForeignCurrency(
            String actualRemittanceAmountAfterTdsInForeignCurrency) {
        this.actualRemittanceAmountAfterTdsInForeignCurrency = actualRemittanceAmountAfterTdsInForeignCurrency;
    }

    public Date getTaxAtSourceDeductionDate() {
        return taxAtSourceDeductionDate;
    }

    public void setTaxAtSourceDeductionDate(Date taxAtSourceDeductionDate) {
        this.taxAtSourceDeductionDate = taxAtSourceDeductionDate;
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

    public String getCaAddress() {
        return caAddress;
    }

    public void setCaAddress(String caAddress) {
        this.caAddress = caAddress;
    }

    public String getCaMembershipNumber() {
        return caMembershipNumber;
    }

    public void setCaMembershipNumber(String caMembershipNumber) {
        this.caMembershipNumber = caMembershipNumber;
    }

    public String getCaRegistrationNumber() {
        return caRegistrationNumber;
    }

    public void setCaRegistrationNumber(String caRegistrationNumber) {
        this.caRegistrationNumber = caRegistrationNumber;
    }

    public String getShareholderSalutation() {
        return shareholderSalutation;
    }

    public void setShareholderSalutation(String shareholderSalutation) {
        this.shareholderSalutation = shareholderSalutation;
    }

    public BigDecimal getRateAsPerITActDtaa() {
        return rateAsPerITActDtaa;
    }

    public void setRateAsPerITActDtaa(BigDecimal rateAsPerITActDtaa) {
        this.rateAsPerITActDtaa = rateAsPerITActDtaa;
    }

    public TDSRateType getTdsRateType() {
        return tdsRateType;
    }

    public void setTdsRateType(TDSRateType tdsRateType) {
        this.tdsRateType = tdsRateType;
    }

    public String getFolioNo() {
        return folioNo;
    }

    public void setFolioNo(String folioNo) {
        this.folioNo = folioNo;
    }
}
