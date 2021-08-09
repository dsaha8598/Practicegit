package com.ey.in.tds.returns.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.StringJoiner;

public class Form15GDTO {
    private Integer shareholderId;
    private String folioNo;
    private String quarter;
    private String financialYear;
    private String filingType;
    private String acknowledgementNumber;
    private String deductorTan;
    private BigDecimal incomePaid;
    private Date declarationDate;
    private Date incomePaidDate;
    private String assesseeName;
    private String assesseePan;
    private String assesseeStatus;
    private String residentialStatus;
    private Integer previousYearOfDeclaration;
    private String flatDoorBlockNo;
    private String nameBuildingVillage;
    private String roadStreetPostoffice;
    private String areaLocality;
    private String townCityDistrict;
    private String state;
    private String pinCode;
    private String email;
    private String telephoneNumber;
    private String mobileNumber;
    private boolean isAssessedToTax;
    private Integer assessedYear;
    private BigDecimal declaredIncome;
    private BigDecimal totalIncomeWhereDeclaredIncomeIncluded;
    private BigDecimal numberOf15GFiled;
    private BigDecimal aggregateIncomeOf15GFiled;
    private Integer identificationNumber;
    private String natureOfIncome;
    private String sectionOfAct;
    private BigDecimal amountOfIncome;
    private String recordType;

    public Integer getShareholderId() {
        return shareholderId;
    }

    public void setShareholderId(Integer shareholderId) {
        this.shareholderId = shareholderId;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public String getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(String financialYear) {
        this.financialYear = financialYear;
    }

    public String getFilingType() {
        return filingType;
    }

    public void setFilingType(String filingType) {
        this.filingType = filingType;
    }

    public String getAcknowledgementNumber() {
        return acknowledgementNumber;
    }

    public void setAcknowledgementNumber(String acknowledgementNumber) {
        this.acknowledgementNumber = acknowledgementNumber;
    }

    public String getDeductorTan() {
        return deductorTan;
    }

    public void setDeductorTan(String deductorTan) {
        this.deductorTan = deductorTan;
    }

    public BigDecimal getIncomePaid() {
        return incomePaid;
    }

    public void setIncomePaid(BigDecimal incomePaid) {
        this.incomePaid = incomePaid;
    }

    public Date getDeclarationDate() {
        return declarationDate;
    }

    public void setDeclarationDate(Date declarationDate) {
        this.declarationDate = declarationDate;
    }

    public Date getIncomePaidDate() {
        return incomePaidDate;
    }

    public void setIncomePaidDate(Date incomePaidDate) {
        this.incomePaidDate = incomePaidDate;
    }

    public String getAssesseeName() {
        return assesseeName;
    }

    public void setAssesseeName(String assesseeName) {
        this.assesseeName = assesseeName;
    }

    public String getAssesseePan() {
        return assesseePan;
    }

    public void setAssesseePan(String assesseePan) {
        this.assesseePan = assesseePan;
    }

    public String getAssesseeStatus() {
        return assesseeStatus;
    }

    public void setAssesseeStatus(String assesseeStatus) {
        this.assesseeStatus = assesseeStatus;
    }

    public String getResidentialStatus() {
        return residentialStatus;
    }

    public void setResidentialStatus(String residentialStatus) {
        this.residentialStatus = residentialStatus;
    }

    public Integer getPreviousYearOfDeclaration() {
        return previousYearOfDeclaration;
    }

    public void setPreviousYearOfDeclaration(Integer previousYearOfDeclaration) {
        this.previousYearOfDeclaration = previousYearOfDeclaration;
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

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public boolean isAssessedToTax() {
        return isAssessedToTax;
    }

    public void setAssessedToTax(boolean assessedToTax) {
        isAssessedToTax = assessedToTax;
    }

    public Integer getAssessedYear() {
        return assessedYear;
    }

    public void setAssessedYear(Integer assessedYear) {
        this.assessedYear = assessedYear;
    }

    public BigDecimal getDeclaredIncome() {
        return declaredIncome;
    }

    public void setDeclaredIncome(BigDecimal declaredIncome) {
        this.declaredIncome = declaredIncome;
    }

    public BigDecimal getTotalIncomeWhereDeclaredIncomeIncluded() {
        return totalIncomeWhereDeclaredIncomeIncluded;
    }

    public void setTotalIncomeWhereDeclaredIncomeIncluded(BigDecimal totalIncomeWhereDeclaredIncomeIncluded) {
        this.totalIncomeWhereDeclaredIncomeIncluded = totalIncomeWhereDeclaredIncomeIncluded;
    }

    public BigDecimal getNumberOf15GFiled() {
        return numberOf15GFiled;
    }

    public void setNumberOf15GFiled(BigDecimal numberOf15GFiled) {
        this.numberOf15GFiled = numberOf15GFiled;
    }

    public BigDecimal getAggregateIncomeOf15GFiled() {
        return aggregateIncomeOf15GFiled;
    }

    public void setAggregateIncomeOf15GFiled(BigDecimal aggregateIncomeOf15GFiled) {
        this.aggregateIncomeOf15GFiled = aggregateIncomeOf15GFiled;
    }

    public Integer getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(Integer identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getNatureOfIncome() {
        return natureOfIncome;
    }

    public void setNatureOfIncome(String natureOfIncome) {
        this.natureOfIncome = natureOfIncome;
    }

    public String getSectionOfAct() {
        return sectionOfAct;
    }

    public void setSectionOfAct(String sectionOfAct) {
        this.sectionOfAct = sectionOfAct;
    }

    public BigDecimal getAmountOfIncome() {
        return amountOfIncome;
    }

    public void setAmountOfIncome(BigDecimal amountOfIncome) {
        this.amountOfIncome = amountOfIncome;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getFolioNo() {
        return folioNo;
    }

    public void setFolioNo(String folioNo) {
        this.folioNo = folioNo;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Form15GDTO.class.getSimpleName() + "[", "]")
                .add("shareholderId=" + shareholderId)
                .add("quarter='" + quarter + "'")
                .add("financialYear='" + financialYear + "'")
                .add("filingType='" + filingType + "'")
                .add("acknowledgementNumber='" + acknowledgementNumber + "'")
                .add("deductorTan='" + deductorTan + "'")
                .add("incomePaid=" + incomePaid)
                .add("declarationDate=" + declarationDate)
                .add("incomePaidDate=" + incomePaidDate)
                .add("assesseeName='" + assesseeName + "'")
                .add("assesseePan='" + assesseePan + "'")
                .add("assesseeStatus='" + assesseeStatus + "'")
                .add("residentialStatus='" + residentialStatus + "'")
                .add("previousYearOfDeclaration=" + previousYearOfDeclaration)
                .add("flatDoorBlockNo='" + flatDoorBlockNo + "'")
                .add("nameBuildingVillage='" + nameBuildingVillage + "'")
                .add("roadStreetPostoffice='" + roadStreetPostoffice + "'")
                .add("areaLocality='" + areaLocality + "'")
                .add("townCityDistrict='" + townCityDistrict + "'")
                .add("state='" + state + "'")
                .add("pinCode='" + pinCode + "'")
                .add("email='" + email + "'")
                .add("telephoneNumber='" + telephoneNumber + "'")
                .add("mobileNumber='" + mobileNumber + "'")
                .add("isAssessedToTax=" + isAssessedToTax)
                .add("assessedYear=" + assessedYear)
                .add("declaredIncome=" + declaredIncome)
                .add("totalIncomeWhereDeclaredIncomeIncluded=" + totalIncomeWhereDeclaredIncomeIncluded)
                .add("numberOf15GFiled=" + numberOf15GFiled)
                .add("aggregateIncomeOf15GFiled=" + aggregateIncomeOf15GFiled)
                .add("identificationNumber=" + identificationNumber)
                .add("natureOfIncome='" + natureOfIncome + "'")
                .add("sectionOfAct='" + sectionOfAct + "'")
                .add("amountOfIncome=" + amountOfIncome)
                .add("recordType='" + recordType + "'")
                .toString();
    }
}
