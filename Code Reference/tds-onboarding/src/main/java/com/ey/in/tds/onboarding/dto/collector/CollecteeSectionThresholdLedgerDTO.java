package com.ey.in.tds.onboarding.dto.collector;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.poi.hpsf.Decimal;

public class CollecteeSectionThresholdLedgerDTO implements Serializable {

	/**
	 * DTO for Collectee entity
	 */
	private static final long serialVersionUID = 1L;
	private Integer Id;
	private String serialNumber;
	private String collecteeCode;
	private String collecteeSection;
	private Integer year;
	private BigDecimal amountUtilized;
	private Integer thresholdReached;
	private Integer active;
	private String createdBy;
	private String modifiedBy;
	private Date createdDate;
	private Date modifiedDate;
	private BigDecimal advancePending;
	private String collectorPan;

	public Integer getId() {
		return Id;
	}

	public void setId(Integer id) {
		Id = id;
	}

	public String getCollecteeCode() {
		return collecteeCode;
	}

	public void setCollecteeCode(String collecteeCode) {
		this.collecteeCode = collecteeCode;
	}

	public String getCollecteeSection() {
		return collecteeSection;
	}

	public void setCollecteeSection(String collecteeSection) {
		this.collecteeSection = collecteeSection;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getThresholdReached() {
		return thresholdReached;
	}

	public void setThresholdReached(Integer thresholdReached) {
		this.thresholdReached = thresholdReached;
	}

	public Integer getActive() {
		return active;
	}

	public void setActive(Integer active) {
		this.active = active;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getCollectorPan() {
		return collectorPan;
	}

	public void setCollectorPan(String collectorPan) {
		this.collectorPan = collectorPan;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public BigDecimal getAmountUtilized() {
		return amountUtilized;
	}

	public void setAmountUtilized(BigDecimal amountUtilized) {
		this.amountUtilized = amountUtilized;
	}

	public BigDecimal getAdvancePending() {
		return advancePending;
	}

	public void setAdvancePending(BigDecimal advancePending) {
		this.advancePending = advancePending;
	}

	@Override
	public String toString() {
		return "CollecteeSectionThresholdLedgerDTO [Id=" + Id + ", serialNumber=" + serialNumber + ", collecteeCode="
				+ collecteeCode + ", collecteeSection=" + collecteeSection + ", year=" + year + ", amountUtilized="
				+ amountUtilized + ", thresholdReached=" + thresholdReached + ", active=" + active + ", createdBy="
				+ createdBy + ", modifiedBy=" + modifiedBy + ", createdDate=" + createdDate + ", modifiedDate="
				+ modifiedDate + ", advancePending=" + advancePending + ", collectorPan=" + collectorPan + "]";
	}
}
