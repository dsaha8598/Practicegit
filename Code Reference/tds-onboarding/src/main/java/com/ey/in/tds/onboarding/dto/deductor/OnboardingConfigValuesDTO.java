package com.ey.in.tds.onboarding.dto.deductor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ey.in.tds.common.domain.masters.DividendOnboardingInfoMetaData.ClientSpecificRule;
import com.ey.in.tds.common.domain.masters.DividendOnboardingInfoMetaData.PrepForm15CaCb;
import com.ey.in.tds.common.domain.masters.DividendOnboardingInfoMetaData.RuleApplicability;

public class OnboardingConfigValuesDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private List<Integer> ipp; // Scope Process
	private List<Integer> ppa; // Invoice Process
	private String cnp; // Credit Note Processing
	private List<Integer> tif; // Type of Invoice Process
	private Boolean cp; // Challan Processing
	private List<String> accountNumber;
	private String provisionProcessing;
	private String provisionTracking;
	private List<ConfigValues> priority;
	private Date applicableFrom;
	private Date applicableTo;
	private String roundoff;
	private String pertransactionlimit;
	private List<String> selectedSectionsForTransactionLimit;
	private String interestCalculationType;
	private Boolean dvndEnabled;
	private Map<ClientSpecificRule, RuleApplicability> dvndClientSpecificRules;
	private PrepForm15CaCb dvndPrepForm15CaCb;
	private Boolean dvndDdtPaidBeforeEOY;
	private Boolean dvndFileForm15gh;
	private String advanceProcessing;

	public List<Integer> getIpp() {
		return ipp;
	}

	public void setIpp(List<Integer> ipp) {
		this.ipp = ipp;
	}

	public List<Integer> getPpa() {
		return ppa;
	}

	public void setPpa(List<Integer> ppa) {
		this.ppa = ppa;
	}

	public List<Integer> getTif() {
		return tif;
	}

	public void setTif(List<Integer> tif) {
		this.tif = tif;
	}

	public List<String> getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(List<String> accountNumber) {
		this.accountNumber = accountNumber;
	}

	public List<ConfigValues> getPriority() {
		return priority;
	}

	public void setPriority(List<ConfigValues> priority) {
		this.priority = priority;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCnp() {
		return cnp;
	}

	public void setCnp(String cnp) {
		this.cnp = cnp;
	}

	public Boolean getCp() {
		return cp;
	}

	public void setCp(Boolean cp) {
		this.cp = cp;
	}

	public String getProvisionProcessing() {
		return provisionProcessing;
	}

	public void setProvisionProcessing(String provisionProcessing) {
		this.provisionProcessing = provisionProcessing;
	}

	public String getProvisionTracking() {
		return provisionTracking;
	}

	public void setProvisionTracking(String provisionTracking) {
		this.provisionTracking = provisionTracking;
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

	public String getRoundoff() {
		return roundoff;
	}

	public void setRoundoff(String roundoff) {
		this.roundoff = roundoff;
	}

	public String getPertransactionlimit() {
		return pertransactionlimit;
	}

	public void setPertransactionlimit(String pertransactionlimit) {
		this.pertransactionlimit = pertransactionlimit;
	}

	

	public List<String> getSelectedSectionsForTransactionLimit() {
		return selectedSectionsForTransactionLimit;
	}

	public void setSelectedSectionsForTransactionLimit(List<String> selectedSectionsForTransactionLimit) {
		this.selectedSectionsForTransactionLimit = selectedSectionsForTransactionLimit;
	}

	public String getInterestCalculationType() {
		return interestCalculationType;
	}

	public void setInterestCalculationType(String interestCalculationType) {
		this.interestCalculationType = interestCalculationType;
	}
	

	public Boolean getDvndEnabled() {
		return dvndEnabled;
	}

	public void setDvndEnabled(Boolean dvndEnabled) {
		this.dvndEnabled = dvndEnabled;
	}

	public Map<ClientSpecificRule, RuleApplicability> getDvndClientSpecificRules() {
		return dvndClientSpecificRules;
	}

	public void setDvndClientSpecificRules(Map<ClientSpecificRule, RuleApplicability> dvndClientSpecificRules) {
		this.dvndClientSpecificRules = dvndClientSpecificRules;
	}

	public PrepForm15CaCb getDvndPrepForm15CaCb() {
		return dvndPrepForm15CaCb;
	}

	public void setDvndPrepForm15CaCb(PrepForm15CaCb dvndPrepForm15CaCb) {
		this.dvndPrepForm15CaCb = dvndPrepForm15CaCb;
	}

	public Boolean getDvndDdtPaidBeforeEOY() {
		return dvndDdtPaidBeforeEOY;
	}

	public void setDvndDdtPaidBeforeEOY(Boolean dvndDdtPaidBeforeEOY) {
		this.dvndDdtPaidBeforeEOY = dvndDdtPaidBeforeEOY;
	}

	public Boolean getDvndFileForm15gh() {
		return dvndFileForm15gh;
	}

	public void setDvndFileForm15gh(Boolean dvndFileForm15gh) {
		this.dvndFileForm15gh = dvndFileForm15gh;
	}

	@Override
	public String toString() {
		return "OnboardingConfigValuesDTO [id=" + id + ", ipp=" + ipp + ", ppa=" + ppa + ", cnp=" + cnp + ", tif=" + tif
				+ ", cp=" + cp + ", accountNumber=" + accountNumber + ", provisionProcessing=" + provisionProcessing
				+ ", provisionTracking=" + provisionTracking + ", priority=" + priority + ", applicableFrom="
				+ applicableFrom + ", applicableTo=" + applicableTo + ", roundoff=" + roundoff
				+ ", pertransactionlimit=" + pertransactionlimit + ", selectedSectionsForTransactionLimit="
				+ selectedSectionsForTransactionLimit + ", interestCalculationType=" + interestCalculationType
				+ ", dvndEnabled=" + dvndEnabled + ", dvndClientSpecificRules=" + dvndClientSpecificRules
				+ ", dvndPrepForm15CaCb=" + dvndPrepForm15CaCb + ", dvndDdtPaidBeforeEOY=" + dvndDdtPaidBeforeEOY
				+ ", dvndFileForm15gh=" + dvndFileForm15gh + "]";
	}

	public String getAdvanceProcessing() {
		return advanceProcessing;
	}

	public void setAdvanceProcessing(String advanceProcessing) {
		this.advanceProcessing = advanceProcessing;
	}

	

	
	
	

}
