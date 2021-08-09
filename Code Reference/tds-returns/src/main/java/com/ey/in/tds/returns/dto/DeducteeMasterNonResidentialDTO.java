package com.ey.in.tds.returns.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.ey.in.tds.common.dto.onboarding.deductee.AdditionalSectionsDTO;
import com.ey.in.tds.common.dto.onboarding.deductee.AddressDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeducteeMasterNonResidentialDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UUID id;

	private String deducteeCode;

	private String deducteeName;

	private String deducteeStatus;

	private String deducteeResidentialStatus;

	private String deducteePAN;

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

	private String deducteeTin;

	private BigDecimal defaultRate;

	private AddressDTO address;

	private String emailAddress;

	private String phoneNumber;

	private String section;

	private BigDecimal rate;

	private Boolean isDeducteeHasAdditionalSections;

	private List<AdditionalSectionsDTO> additionalSections = new ArrayList<>();

	private Date applicableFrom;

	private Date applicableTo;

	private String relatedParty;

	private String deductor;

	private Boolean isGrossingUp;

	private Boolean isDeducteeTransparent;

	private String trcCountry;

	private String trcTin;

	private String trcAddress;

	private String trcPeriod;

	private String trcPan;

	private String trcStatus;

	private Boolean istrcFuture;

	private String tenfCountry;

	private String tenfTin;

	private String tenfAddress;

	private String tenfPeriod;

	private String tenfPan;

	private String tenfStatus;

	private Boolean istenfFuture;

	private Boolean isBusinessCarriedInIndia;

	private Boolean isPEinvoilvedInPurchaseGoods;

	private Boolean isPEamountReceived;

	private Boolean isPEdocument;

	private Date weatherPEInIndiaApplicableFrom;

	private Date weatherPEInIndiaApplicableTo;

	private Boolean isFixedbaseAvailbleIndia;

	private Date fixedbaseAvailbleIndiaApplicableFrom;

	private Date fixedbaseAvailbleIndiaApplicableTo;

	private Boolean isAmountConnectedFixedBase;

	private String nrCountryofResidence;

	private Integer nrRate;

	private String principlesOfBusinessPlace;

	private String stayPeriodFinancialYear;

	public String getStayPeriodFinancialYear() {
		return stayPeriodFinancialYear;
	}

	public void setStayPeriodFinancialYear(String stayPeriodFinancialYear) {
		this.stayPeriodFinancialYear = stayPeriodFinancialYear;
	}

	public String getPrinciplesOfBusinessPlace() {
		return principlesOfBusinessPlace;
	}

	public void setPrinciplesOfBusinessPlace(String principlesOfBusinessPlace) {
		this.principlesOfBusinessPlace = principlesOfBusinessPlace;
	}

	public String getRelatedParty() {
		return relatedParty;
	}

	public void setRelatedParty(String relatedParty) {
		this.relatedParty = relatedParty;
	}

	public String getDeductor() {
		return deductor;
	}

	public void setDeductor(String deductor) {
		this.deductor = deductor;
	}

	public Boolean getIsGrossingUp() {
		return isGrossingUp;
	}

	public void setIsGrossingUp(Boolean isGrossingUp) {
		this.isGrossingUp = isGrossingUp;
	}

	public Boolean getIsDeducteeTransparent() {
		return isDeducteeTransparent;
	}

	public void setIsDeducteeTransparent(Boolean isDeducteeTransparent) {
		this.isDeducteeTransparent = isDeducteeTransparent;
	}

	public String getTrcCountry() {
		return trcCountry;
	}

	public void setTrcCountry(String trcCountry) {
		this.trcCountry = trcCountry;
	}

	public String getTrcTin() {
		return trcTin;
	}

	public void setTrcTin(String trcTin) {
		this.trcTin = trcTin;
	}

	public String getTrcAddress() {
		return trcAddress;
	}

	public void setTrcAddress(String trcAddress) {
		this.trcAddress = trcAddress;
	}

	public String getTrcPeriod() {
		return trcPeriod;
	}

	public void setTrcPeriod(String trcPeriod) {
		this.trcPeriod = trcPeriod;
	}

	public String getTrcPan() {
		return trcPan;
	}

	public void setTrcPan(String trcPan) {
		this.trcPan = trcPan;
	}

	public String getTrcStatus() {
		return trcStatus;
	}

	public void setTrcStatus(String trcStatus) {
		this.trcStatus = trcStatus;
	}

	public Boolean getIstrcFuture() {
		return istrcFuture;
	}

	public void setIstrcFuture(Boolean istrcFuture) {
		this.istrcFuture = istrcFuture;
	}

	public String getTenfCountry() {
		return tenfCountry;
	}

	public void setTenfCountry(String tenfCountry) {
		this.tenfCountry = tenfCountry;
	}

	public String getTenfTin() {
		return tenfTin;
	}

	public void setTenfTin(String tenfTin) {
		this.tenfTin = tenfTin;
	}

	public String getTenfAddress() {
		return tenfAddress;
	}

	public void setTenfAddress(String tenfAddress) {
		this.tenfAddress = tenfAddress;
	}

	public String getTenfPeriod() {
		return tenfPeriod;
	}

	public void setTenfPeriod(String tenfPeriod) {
		this.tenfPeriod = tenfPeriod;
	}

	public String getTenfPan() {
		return tenfPan;
	}

	public void setTenfPan(String tenfPan) {
		this.tenfPan = tenfPan;
	}

	public String getTenfStatus() {
		return tenfStatus;
	}

	public void setTenfStatus(String tenfStatus) {
		this.tenfStatus = tenfStatus;
	}

	public Boolean getIstenfFuture() {
		return istenfFuture;
	}

	public void setIstenfFuture(Boolean istenfFuture) {
		this.istenfFuture = istenfFuture;
	}

	public Boolean getIsBusinessCarriedInIndia() {
		return isBusinessCarriedInIndia;
	}

	public void setIsBusinessCarriedInIndia(Boolean isBusinessCarriedInIndia) {
		this.isBusinessCarriedInIndia = isBusinessCarriedInIndia;
	}

	public Boolean getIsPEinvoilvedInPurchaseGoods() {
		return isPEinvoilvedInPurchaseGoods;
	}

	public void setIsPEinvoilvedInPurchaseGoods(Boolean isPEinvoilvedInPurchaseGoods) {
		this.isPEinvoilvedInPurchaseGoods = isPEinvoilvedInPurchaseGoods;
	}

	public Boolean getIsPEamountReceived() {
		return isPEamountReceived;
	}

	public void setIsPEamountReceived(Boolean isPEamountReceived) {
		this.isPEamountReceived = isPEamountReceived;
	}

	public Boolean getIsPEdocument() {
		return isPEdocument;
	}

	public void setIsPEdocument(Boolean isPEdocument) {
		this.isPEdocument = isPEdocument;
	}

	public Date getWeatherPEInIndiaApplicableFrom() {
		return weatherPEInIndiaApplicableFrom;
	}

	public void setWeatherPEInIndiaApplicableFrom(Date weatherPEInIndiaApplicableFrom) {
		this.weatherPEInIndiaApplicableFrom = weatherPEInIndiaApplicableFrom;
	}

	public Date getWeatherPEInIndiaApplicableTo() {
		return weatherPEInIndiaApplicableTo;
	}

	public void setWeatherPEInIndiaApplicableTo(Date weatherPEInIndiaApplicableTo) {
		this.weatherPEInIndiaApplicableTo = weatherPEInIndiaApplicableTo;
	}

	public Boolean getIsFixedbaseAvailbleIndia() {
		return isFixedbaseAvailbleIndia;
	}

	public void setIsFixedbaseAvailbleIndia(Boolean isFixedbaseAvailbleIndia) {
		this.isFixedbaseAvailbleIndia = isFixedbaseAvailbleIndia;
	}

	public Date getFixedbaseAvailbleIndiaApplicableFrom() {
		return fixedbaseAvailbleIndiaApplicableFrom;
	}

	public void setFixedbaseAvailbleIndiaApplicableFrom(Date fixedbaseAvailbleIndiaApplicableFrom) {
		this.fixedbaseAvailbleIndiaApplicableFrom = fixedbaseAvailbleIndiaApplicableFrom;
	}

	public Date getFixedbaseAvailbleIndiaApplicableTo() {
		return fixedbaseAvailbleIndiaApplicableTo;
	}

	public void setFixedbaseAvailbleIndiaApplicableTo(Date fixedbaseAvailbleIndiaApplicableTo) {
		this.fixedbaseAvailbleIndiaApplicableTo = fixedbaseAvailbleIndiaApplicableTo;
	}

	public Boolean getIsAmountConnectedFixedBase() {
		return isAmountConnectedFixedBase;
	}

	public void setIsAmountConnectedFixedBase(Boolean isAmountConnectedFixedBase) {
		this.isAmountConnectedFixedBase = isAmountConnectedFixedBase;
	}

	public String getNrCountryofResidence() {
		return nrCountryofResidence;
	}

	public void setNrCountryofResidence(String nrCountryofResidence) {
		this.nrCountryofResidence = nrCountryofResidence;
	}

	public Integer getNrRate() {
		return nrRate;
	}

	public void setNrRate(Integer nrRate) {
		this.nrRate = nrRate;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public String getDeducteePAN() {
		return deducteePAN;
	}

	public void setDeducteePAN(String deducteePAN) {
		this.deducteePAN = deducteePAN;
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

	public List<AdditionalSectionsDTO> getAdditionalSections() {
		return additionalSections;
	}

	public void setAdditionalSections(List<AdditionalSectionsDTO> additionalSections) {
		this.additionalSections = additionalSections;
	}

	public String getDeducteeTin() {
		return deducteeTin;
	}

	public void setDeducteeTin(String deducteeTin) {
		this.deducteeTin = deducteeTin;
	}

	public BigDecimal getDefaultRate() {
		return defaultRate;
	}

	public void setDefaultRate(BigDecimal defaultRate) {
		this.defaultRate = defaultRate;
	}

	public AddressDTO getAddress() {
		return address;
	}

	public void setAddress(AddressDTO address) {
		this.address = address;
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

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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

	public Boolean getIsDeducteeHasAdditionalSections() {
		return isDeducteeHasAdditionalSections;
	}

	public void setIsDeducteeHasAdditionalSections(Boolean isDeducteeHasAdditionalSections) {
		this.isDeducteeHasAdditionalSections = isDeducteeHasAdditionalSections;
	}

	public Date getApplicableFrom() {
		return applicableFrom;
	}

	public void setApplicableFrom(Date applicableFrom) {
		this.applicableFrom = applicableFrom;
	}

	public Date getApplicableTo() {
		return applicableTo;
	}

	public void setApplicableTo(Date applicableTo) {
		this.applicableTo = applicableTo;
	}

	@Override
	public String toString() {
		return "DeducteeMasterNonResidentialDTO [id=" + id + ", deducteeCode=" + deducteeCode + ", deducteeName="
				+ deducteeName + ", deducteeStatus=" + deducteeStatus + ", deducteeResidentialStatus="
				+ deducteeResidentialStatus + ", deducteePAN=" + deducteePAN + ", isTRCAvailable=" + isTRCAvailable
				+ ", trcApplicableFrom=" + trcApplicableFrom + ", trcApplicableTo=" + trcApplicableTo
				+ ", isTenFAvailable=" + isTenFAvailable + ", tenFApplicableFrom=" + tenFApplicableFrom
				+ ", tenFApplicableTo=" + tenFApplicableTo + ", weatherPEInIndia=" + weatherPEInIndia
				+ ", wpeApplicableFrom=" + wpeApplicableFrom + ", wpeApplicableTo=" + wpeApplicableTo
				+ ", noPEDocumentAvaliable=" + noPEDocumentAvaliable + ", isPOEMavailable=" + isPOEMavailable
				+ ", poemApplicableFrom=" + poemApplicableFrom + ", poemApplicableTo=" + poemApplicableTo
				+ ", countryOfResidence=" + countryOfResidence + ", deducteeTin=" + deducteeTin + ", defaultRate="
				+ defaultRate + ", address=" + address + ", emailAddress=" + emailAddress + ", phoneNumber="
				+ phoneNumber + ", section=" + section + ", rate=" + rate + ", isDeducteeHasAdditionalSections="
				+ isDeducteeHasAdditionalSections + ", additionalSections=" + additionalSections + ", applicableFrom="
				+ applicableFrom + ", applicableTo=" + applicableTo + ", relatedParty=" + relatedParty + ", deductor="
				+ deductor + ", isGrossingUp=" + isGrossingUp + ", isDeducteeTransparent=" + isDeducteeTransparent
				+ ", trcCountry=" + trcCountry + ", trcTin=" + trcTin + ", trcAddress=" + trcAddress + ", trcPeriod="
				+ trcPeriod + ", trcPan=" + trcPan + ", trcStatus=" + trcStatus + ", istrcFuture=" + istrcFuture
				+ ", tenfCountry=" + tenfCountry + ", tenfTin=" + tenfTin + ", tenfAddress=" + tenfAddress
				+ ", tenfPeriod=" + tenfPeriod + ", tenfPan=" + tenfPan + ", tenfStatus=" + tenfStatus
				+ ", istenfFuture=" + istenfFuture + ", isBusinessCarriedInIndia=" + isBusinessCarriedInIndia
				+ ", isPEinvoilvedInPurchaseGoods=" + isPEinvoilvedInPurchaseGoods + ", isPEamountReceived="
				+ isPEamountReceived + ", isPEdocument=" + isPEdocument + ", weatherPEInIndiaApplicableFrom="
				+ weatherPEInIndiaApplicableFrom + ", weatherPEInIndiaApplicableTo=" + weatherPEInIndiaApplicableTo
				+ ", isFixedbaseAvailbleIndia=" + isFixedbaseAvailbleIndia + ", fixedbaseAvailbleIndiaApplicableFrom="
				+ fixedbaseAvailbleIndiaApplicableFrom + ", fixedbaseAvailbleIndiaApplicableTo="
				+ fixedbaseAvailbleIndiaApplicableTo + ", isAmountConnectedFixedBase=" + isAmountConnectedFixedBase
				+ ", nrCountryofResidence=" + nrCountryofResidence + ", nrRate=" + nrRate
				+ ", principlesOfBusinessPlace=" + principlesOfBusinessPlace + ", stayPeriodFinancialYear="
				+ stayPeriodFinancialYear + "]";
	}

}
