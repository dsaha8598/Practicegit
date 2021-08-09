package com.ey.in.tds.tcs.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import javax.validation.constraints.NotNull;
/**
 * A DTO for the TCSCessMaster entity.
 */
public class TCSCessMasterDTO implements Serializable {
	
	private static final long serialVersionUID = 4061449866101622859L;

	private Long id;
	private Boolean isCessApplicable;
	private Long cessTypeId;
	private String cessTypeName;
	private BigDecimal rate;
	private BigDecimal amount;
	private String collecteeStatus;
	private String  collecteeResidentialStatus;
	private String natureOfIncome;
	private Boolean active;
	@NotNull(message = "applicable from date is mandatory")
	private Instant applicableFrom;
	private Instant applicableTo;
    private List<TCSBAsisOfCessDetails> basisOfCessDetails;	
    
    
	public Long getCessTypeId() {
		return cessTypeId;
	}
	public void setCessTypeId(Long cessTypeId) {
		this.cessTypeId = cessTypeId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Boolean getIsCessApplicable() {
		return isCessApplicable;
	}
	public void setIsCessApplicable(Boolean isCessApplicable) {
		this.isCessApplicable = isCessApplicable;
	}
	public String getCessTypeName() {
		return cessTypeName;
	}
	public void setCessTypeName(String cessTypeName) {
		this.cessTypeName = cessTypeName;
	}
	public BigDecimal getRate() {
		return rate;
	}
	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public Instant getApplicableFrom() {
		return applicableFrom;
	}
	public void setApplicableFrom(Instant applicableFrom) {
		this.applicableFrom = applicableFrom;
	}
	public Instant getApplicableTo() {
		return applicableTo;
	}
	public void setApplicableTo(Instant applicableTo) {
		this.applicableTo = applicableTo;
	}
	public List<TCSBAsisOfCessDetails> getBasisOfCessDetails() {
		return basisOfCessDetails;
	}
	public void setBasisOfCessDetails(List<TCSBAsisOfCessDetails> basisOfCessDetails) {
		this.basisOfCessDetails = basisOfCessDetails;
	}
	public String getCollecteeStatus() {
		return collecteeStatus;
	}
	public void setCollecteeStatus(String collecteeStatus) {
		this.collecteeStatus = collecteeStatus;
	}
	public String getCollecteeResidentialStatus() {
		return collecteeResidentialStatus;
	}
	public void setCollecteeResidentialStatus(String collecteeResidentialStatus) {
		this.collecteeResidentialStatus = collecteeResidentialStatus;
	}
	public String getNatureOfIncome() {
		return natureOfIncome;
	}
	public void setNatureOfIncome(String natureOfIncome) {
		this.natureOfIncome = natureOfIncome;
	}
	

}
