package com.ey.in.tds.tcs.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class TCSSurchargeMasterDTO  implements Serializable {
	
	private static final long serialVersionUID = -4724712539754669900L;
	
	private Long id;
	private BigDecimal rate;
	private BigDecimal bocInvoiceSlab;
	private Instant applicableFrom;
	private Instant applicableTo;
	//private boolean surchargeApplicable;
	private String collecteeStatus;
	private String collecteeResidentialStatus;
	private String natureOfIncome;
	private List<TCSBasOfSurchargeDetailsDTO> basisOfSurchargeDetails;
	

	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
/*	public BigDecimal getSurchargeRate() {
		return surchargeRate;
	}
	public void setSurchargeRate(BigDecimal surchargeRate) {
		this.surchargeRate = surchargeRate;
	}  */
	public BigDecimal getBocInvoiceSlab() {
		return bocInvoiceSlab;
	}
	public void setBocInvoiceSlab(BigDecimal bocInvoiceSlab) {
		this.bocInvoiceSlab = bocInvoiceSlab;
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
/*	public boolean isSurchargeApplicable() {
		return surchargeApplicable;
	}
	public void setSurchargeApplicable(boolean surchargeApplicable) {
		this.surchargeApplicable = surchargeApplicable;
	}  */
	
	
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
	public List<TCSBasOfSurchargeDetailsDTO> getBasisOfSurchargeDetails() {
		return basisOfSurchargeDetails;
	}
	public void setBasisOfSurchargeDetails(List<TCSBasOfSurchargeDetailsDTO> basisOfSurchargeDetails) {
		this.basisOfSurchargeDetails = basisOfSurchargeDetails;
	}
	
	public BigDecimal getRate() {
		return rate;
	}
	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}
	@Override
	public String toString() {
		return "TCSSurchargeMasterDTO [id=" + id + ", surchargeRate=" + ", bocInvoiceSlab="
				+ bocInvoiceSlab + ", applicableFrom=" + applicableFrom + ", applicableTo=" + applicableTo
				+ ", collecteeStatus=" + collecteeStatus
				+ ", collecteeResidentialStatus=" + collecteeResidentialStatus + ", natureOfIncome=" + natureOfIncome
				+ ", basisOfSurchargeDetails=" + basisOfSurchargeDetails + "]";
	}
	
}
