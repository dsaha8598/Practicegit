package com.ey.in.tds.returns.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Form15CAPartBDTO implements Serializable {
    private static final long serialVersionUID = -8449702596386556635L;

    private Integer id;

    private Integer shareHolderId;
    
    private String folioNo;

    private String remitterName;

    private String remitterPan;

    private String remitterTan;

    private String remitterStatus;

    private String remitterResidentialStatus;

    private String remitterEmail;

    private String remitterPhoneNumber;

    private String remitterFlatDoorBlockNo;

    private String remitterNameBuildingVillage;

    private String remitterRoadStreetPostoffice;

    private String remitterAreaLocality;

    private String remitterTownCityDistrict;

    private String remitterState;

    private String remitterCountry;

    private String remitterPinCode;

    private String shareholderName;

    private String shareholderPan;

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

    private BigDecimal tdsAmount;

    private BigDecimal tdsRate;

    private Date deductionDate;

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

    public String getOrderOrCertificateSection() {
        return orderOrCertificateSection;
    }

    public void setOrderOrCertificateSection(String orderOrCertificateSection) {
        this.orderOrCertificateSection = orderOrCertificateSection;
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

    public BigDecimal getTdsAmount() {
        return tdsAmount;
    }

    public void setTdsAmount(BigDecimal tdsAmount) {
        this.tdsAmount = tdsAmount;
    }

    public BigDecimal getTdsRate() {
        return tdsRate;
    }

    public void setTdsRate(BigDecimal tdsRate) {
        this.tdsRate = tdsRate;
    }

    public Date getDeductionDate() {
        return deductionDate;
    }

    public void setDeductionDate(Date deductionDate) {
        this.deductionDate = deductionDate;
    }

    public String getFolioNo() {
        return folioNo;
    }

    public void setFolioNo(String folioNo) {
        this.folioNo = folioNo;
    }

	@Override
	public String toString() {
		return "Form15CAPartBDTO [id=" + id + ", folioNo=" + folioNo + ", remitterName=" + remitterName
				+ ", remitterPan=" + remitterPan + ", remitterTan=" + remitterTan + ", remitterStatus=" + remitterStatus
				+ ", remitterResidentialStatus=" + remitterResidentialStatus + ", remitterEmail=" + remitterEmail
				+ ", remitterPhoneNumber=" + remitterPhoneNumber + ", remitterFlatDoorBlockNo="
				+ remitterFlatDoorBlockNo + ", remitterNameBuildingVillage=" + remitterNameBuildingVillage
				+ ", remitterRoadStreetPostoffice=" + remitterRoadStreetPostoffice + ", remitterAreaLocality="
				+ remitterAreaLocality + ", remitterTownCityDistrict=" + remitterTownCityDistrict + ", remitterState="
				+ remitterState + ", remitterCountry=" + remitterCountry + ", remitterPinCode=" + remitterPinCode
				+ ", shareholderName=" + shareholderName + ", shareholderPan=" + shareholderPan + ", shareholderEmail="
				+ shareholderEmail + ", shareholderIsdCodePhoneNumber=" + shareholderIsdCodePhoneNumber
				+ ", flatDoorBlockNo=" + flatDoorBlockNo + ", nameBuildingVillage=" + nameBuildingVillage
				+ ", roadStreetPostoffice=" + roadStreetPostoffice + ", areaLocality=" + areaLocality
				+ ", townCityDistrict=" + townCityDistrict + ", state=" + state + ", country=" + country + ", pinCode="
				+ pinCode + ", orderOrCertificateSection=" + orderOrCertificateSection + ", assessingOfficerName="
				+ assessingOfficerName + ", assessingOfficerDesignation=" + assessingOfficerDesignation
				+ ", orderOrCertificateDate=" + orderOrCertificateDate + ", orderOrCertificateNumber="
				+ orderOrCertificateNumber + ", remittanceCountry=" + remittanceCountry + ", currency=" + currency
				+ ", amountPayableInForeignCurrency=" + amountPayableInForeignCurrency + ", amountPayableInInr="
				+ amountPayableInInr + ", nameOfBank=" + nameOfBank + ", branchOfBank=" + branchOfBank
				+ ", bsrCodeOfBankBranch=" + bsrCodeOfBankBranch + ", proposedDateOfRemittance="
				+ proposedDateOfRemittance + ", natureOfRemittance=" + natureOfRemittance + ", rbiPurposeCode="
				+ rbiPurposeCode + ", tdsAmount=" + tdsAmount + ", tdsRate=" + tdsRate + ", deductionDate="
				+ deductionDate + "]";
	}
    
    
}
