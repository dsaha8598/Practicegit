package com.ey.in.tds.ingestion.dto.generalledger;

import java.math.BigDecimal;

public class GLMismatchDTO {
	private String id;
	private String batchid;
	//general_ledger_client_amount
	private BigDecimal  clientAmount;
	//general_ledger_toolderived_amount
	private BigDecimal  toolDerivedAmount;
	//general_ledger_client_tds_section,
	private String clientSection; 
	//general_ledger_toolderived_tds_section,
	private String toolDerivedSection; 
	//clientAmount - toolDerivedAmount (only if positive else set 0)
	private BigDecimal excessDeduction;
	//toolDerivedAmount - clientAmount (only if positive else set 0)
	private BigDecimal shortDeduction;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBatchid() {
		return batchid;
	}
	public void setBatchid(String batchid) {
		this.batchid = batchid;
	}
	public BigDecimal getClientAmount() {
		return clientAmount;
	}
	public void setClientAmount(BigDecimal clientAmount) {
		this.clientAmount = clientAmount;
	}
	public BigDecimal getToolDerivedAmount() {
		return toolDerivedAmount;
	}
	public void setToolDerivedAmount(BigDecimal toolDerivedAmount) {
		this.toolDerivedAmount = toolDerivedAmount;
	}
	public String getClientSection() {
		return clientSection;
	}
	public void setClientSection(String clientSection) {
		this.clientSection = clientSection;
	}
	public String getToolDerivedSection() {
		return toolDerivedSection;
	}
	public void setToolDerivedSection(String toolDerivedSection) {
		this.toolDerivedSection = toolDerivedSection;
	}
	public BigDecimal getExcessDeduction() {
		return excessDeduction;
	}
	public void setExcessDeduction(BigDecimal excessDeduction) {
		this.excessDeduction = excessDeduction;
	}
	public BigDecimal getShortDeduction() {
		return shortDeduction;
	}
	public void setShortDeduction(BigDecimal shortDeduction) {
		this.shortDeduction = shortDeduction;
	}
	
}
