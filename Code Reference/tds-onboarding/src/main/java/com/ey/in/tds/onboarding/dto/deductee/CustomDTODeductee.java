package com.ey.in.tds.onboarding.dto.deductee;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public class CustomDTODeductee implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UUID id;

	private String sourceIdentifier;

	private String sourceFileName;

	private String deducteeCode;

	private String nonResidentDeducteeIndicator;

	private String deducteePAN;

	private String deducteeTin;

	private String deducteeName;

	private String deducteeStatus;

	private String deducteeResidentialStatus;

	private Boolean isTRCAvailable;

	private Date trcApplicableFrom;

	private Date trcApplicableTo;

	private Boolean isTenFAvailable;

	private Date tenFApplicableFrom;

	private Date tenFApplicableTo;

	private Boolean weatherPEInIndia;

	private Date wpeApplicableFrom;

	private Date wpeApplicableTo;

	private Boolean noPEDocumentAvaliable;

	private Boolean isPOEMavailable;

	private Date poemApplicableFrom;

	private Date poemApplicableTo;

	private String countryOfResidence;

	private String defaultRate1;

	private String defaultRate2;

	private String emailAddress;

	private String phoneNumber;

	private String flatDoorBlockNo;

	private String nameBuildingVillage;

	private String roadStreetPostoffice;

	private String areaLocality;

	private String townCityDistrict;

	private String stateName;

	private String country;

	private String pinCode;

	private String section;

	private BigDecimal rate;

	private Date applicableFrom1;

	private Date applicableTo1;

	// private Boolean isDeducteeHasAdditionalSections;

	// private Set<AdditionalSectionsDTO> additionalSections = new HashSet<>();

	private String section2;

	private String rate2;

	private Date applicableFrom2;

	private Date applicableTo2;

	private String section3;

	private String rate3;

	private Date applicableFrom3;

	private Date applicableTo3;

	private String section4;

	private String rate4;

	private Date applicableFrom4;

	private Date applicableTo4;

	private String Udf1;

	private String Udf2;

	private String Udf3;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public String getDeducteeCode() {
		return deducteeCode;
	}

	public void setDeducteeCode(String deducteeCode) {
		this.deducteeCode = deducteeCode;
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

	public String getDeducteeTin() {
		return deducteeTin;
	}

	public void setDeducteeTin(String deducteeTin) {
		this.deducteeTin = deducteeTin;
	}

	public String getDeducteeName() {
		return deducteeName;
	}

	public void setDeducteeName(String deducteeName) {
		this.deducteeName = deducteeName;
	}

	public String getDeducteeStatus() {
		return deducteeStatus;
	}

	public void setDeducteeStatus(String deducteeStatus) {
		this.deducteeStatus = deducteeStatus;
	}

	public String getDeducteeResidentialStatus() {
		return deducteeResidentialStatus;
	}

	public void setDeducteeResidentialStatus(String deducteeResidentialStatus) {
		this.deducteeResidentialStatus = deducteeResidentialStatus;
	}

	public Boolean getIsTRCAvailable() {
		return isTRCAvailable;
	}

	public void setIsTRCAvailable(Boolean isTRCAvailable) {
		this.isTRCAvailable = isTRCAvailable;
	}

	public Date getTrcApplicableFrom() {
		return trcApplicableFrom;
	}

	public void setTrcApplicableFrom(Date trcApplicableFrom) {
		this.trcApplicableFrom = trcApplicableFrom;
	}

	public Date getTrcApplicableTo() {
		return trcApplicableTo;
	}

	public void setTrcApplicableTo(Date trcApplicableTo) {
		this.trcApplicableTo = trcApplicableTo;
	}

	public Boolean getIsTenFAvailable() {
		return isTenFAvailable;
	}

	public void setIsTenFAvailable(Boolean isTenFAvailable) {
		this.isTenFAvailable = isTenFAvailable;
	}

	public Date getTenFApplicableFrom() {
		return tenFApplicableFrom;
	}

	public void setTenFApplicableFrom(Date tenFApplicableFrom) {
		this.tenFApplicableFrom = tenFApplicableFrom;
	}

	public Date getTenFApplicableTo() {
		return tenFApplicableTo;
	}

	public void setTenFApplicableTo(Date tenFApplicableTo) {
		this.tenFApplicableTo = tenFApplicableTo;
	}

	public Boolean getWeatherPEInIndia() {
		return weatherPEInIndia;
	}

	public void setWeatherPEInIndia(Boolean weatherPEInIndia) {
		this.weatherPEInIndia = weatherPEInIndia;
	}

	public Date getWpeApplicableFrom() {
		return wpeApplicableFrom;
	}

	public void setWpeApplicableFrom(Date wpeApplicableFrom) {
		this.wpeApplicableFrom = wpeApplicableFrom;
	}

	public Date getWpeApplicableTo() {
		return wpeApplicableTo;
	}

	public void setWpeApplicableTo(Date wpeApplicableTo) {
		this.wpeApplicableTo = wpeApplicableTo;
	}

	public Boolean getNoPEDocumentAvaliable() {
		return noPEDocumentAvaliable;
	}

	public void setNoPEDocumentAvaliable(Boolean noPEDocumentAvaliable) {
		this.noPEDocumentAvaliable = noPEDocumentAvaliable;
	}

	public Boolean getIsPOEMavailable() {
		return isPOEMavailable;
	}

	public void setIsPOEMavailable(Boolean isPOEMavailable) {
		this.isPOEMavailable = isPOEMavailable;
	}

	public Date getPoemApplicableFrom() {
		return poemApplicableFrom;
	}

	public void setPoemApplicableFrom(Date poemApplicableFrom) {
		this.poemApplicableFrom = poemApplicableFrom;
	}

	public Date getPoemApplicableTo() {
		return poemApplicableTo;
	}

	public void setPoemApplicableTo(Date poemApplicableTo) {
		this.poemApplicableTo = poemApplicableTo;
	}

	public String getCountryOfResidence() {
		return countryOfResidence;
	}

	public void setCountryOfResidence(String countryOfResidence) {
		this.countryOfResidence = countryOfResidence;
	}

	public String getDefaultRate1() {
		return defaultRate1;
	}

	public void setDefaultRate1(String defaultRate1) {
		this.defaultRate1 = defaultRate1;
	}

	public String getDefaultRate2() {
		return defaultRate2;
	}

	public void setDefaultRate2(String defaultRate2) {
		this.defaultRate2 = defaultRate2;
	}

	public void setRate3(String rate3) {
		this.rate3 = rate3;
	}

	public void setRate4(String rate4) {
		this.rate4 = rate4;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String string) {
		this.phoneNumber = string;
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

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
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

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public String getSection2() {
		return section2;
	}

	public void setSection2(String section2) {
		this.section2 = section2;
	}

	public String getRate2() {
		return rate2;
	}

	public void setRate2(String string) {
		this.rate2 = string;
	}

	public String getSection3() {
		return section3;
	}

	public void setSection3(String section3) {
		this.section3 = section3;
	}

	public String getSection4() {
		return section4;
	}

	public void setSection4(String section4) {
		this.section4 = section4;
	}

	public String getRate3() {
		return rate3;
	}

	public String getRate4() {
		return rate4;
	}

	public String getUdf1() {
		return Udf1;
	}

	public void setUdf1(String udf1) {
		Udf1 = udf1;
	}

	public String getUdf2() {
		return Udf2;
	}

	public void setUdf2(String udf2) {
		Udf2 = udf2;
	}

	public String getUdf3() {
		return Udf3;
	}

	public void setUdf3(String udf3) {
		Udf3 = udf3;
	}

	public Date getApplicableFrom1() {
		return applicableFrom1;
	}

	public void setApplicableFrom1(Date applicableFrom1) {
		this.applicableFrom1 = applicableFrom1;
	}

	public Date getApplicableTo1() {
		return applicableTo1;
	}

	public void setApplicableTo1(Date applicableTo1) {
		this.applicableTo1 = applicableTo1;
	}

	public Date getApplicableFrom2() {
		return applicableFrom2;
	}

	public void setApplicableFrom2(Date applicableFrom2) {
		this.applicableFrom2 = applicableFrom2;
	}

	public Date getApplicableTo2() {
		return applicableTo2;
	}

	public void setApplicableTo2(Date applicableTo2) {
		this.applicableTo2 = applicableTo2;
	}

	public Date getApplicableFrom3() {
		return applicableFrom3;
	}

	public void setApplicableFrom3(Date applicableFrom3) {
		this.applicableFrom3 = applicableFrom3;
	}

	public Date getApplicableTo3() {
		return applicableTo3;
	}

	public void setApplicableTo3(Date applicableTo3) {
		this.applicableTo3 = applicableTo3;
	}

	public Date getApplicableFrom4() {
		return applicableFrom4;
	}

	public void setApplicableFrom4(Date applicableFrom4) {
		this.applicableFrom4 = applicableFrom4;
	}

	public Date getApplicableTo4() {
		return applicableTo4;
	}

	public void setApplicableTo4(Date applicableTo4) {
		this.applicableTo4 = applicableTo4;
	}

	@Override
	public String toString() {
		return "CustomDTODeductee [id=" + id + ", sourceIdentifier=" + sourceIdentifier + ", sourceFileName="
				+ sourceFileName + ", deducteeCode=" + deducteeCode + ", nonResidentDeducteeIndicator="
				+ nonResidentDeducteeIndicator + ", deducteePAN=" + deducteePAN + ", deducteeTin=" + deducteeTin
				+ ", deducteeName=" + deducteeName + ", deducteeStatus=" + deducteeStatus
				+ ", deducteeResidentialStatus=" + deducteeResidentialStatus + ", isTRCAvailable=" + isTRCAvailable
				+ ", trcApplicableFrom=" + trcApplicableFrom + ", trcApplicableTo=" + trcApplicableTo
				+ ", isTenFAvailable=" + isTenFAvailable + ", tenFApplicableFrom=" + tenFApplicableFrom
				+ ", tenFApplicableTo=" + tenFApplicableTo + ", weatherPEInIndia=" + weatherPEInIndia
				+ ", wpeApplicableFrom=" + wpeApplicableFrom + ", wpeApplicableTo=" + wpeApplicableTo
				+ ", noPEDocumentAvaliable=" + noPEDocumentAvaliable + ", isPOEMavailable=" + isPOEMavailable
				+ ", poemApplicableFrom=" + poemApplicableFrom + ", poemApplicableTo=" + poemApplicableTo
				+ ", countryOfResidence=" + countryOfResidence + ", defaultRate1=" + defaultRate1 + ", defaultRate2="
				+ defaultRate2 + ", emailAddress=" + emailAddress + ", phoneNumber=" + phoneNumber
				+ ", flatDoorBlockNo=" + flatDoorBlockNo + ", nameBuildingVillage=" + nameBuildingVillage
				+ ", roadStreetPostoffice=" + roadStreetPostoffice + ", areaLocality=" + areaLocality
				+ ", townCityDistrict=" + townCityDistrict + ", stateName=" + stateName + ", country=" + country
				+ ", pinCode=" + pinCode + ", section=" + section + ", rate=" + rate + ", applicableFrom1="
				+ applicableFrom1 + ", applicableTo1=" + applicableTo1 + ", section2=" + section2 + ", rate2=" + rate2
				+ ", applicableFrom2=" + applicableFrom2 + ", applicableTo2=" + applicableTo2 + ", section3=" + section3
				+ ", rate3=" + rate3 + ", applicableFrom3=" + applicableFrom3 + ", applicableTo3=" + applicableTo3
				+ ", section4=" + section4 + ", rate4=" + rate4 + ", applicableFrom4=" + applicableFrom4
				+ ", applicableTo4=" + applicableTo4 + ", Udf1=" + Udf1 + ", Udf2=" + Udf2 + ", Udf3=" + Udf3 + "]";
	}
}
