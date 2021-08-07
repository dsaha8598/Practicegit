package com.udajahaja.dto;

import java.util.Date;

public class AeroplaneSaveDTO {

	private String aeroplaneNumber;
	private int businessClassCount;
	private int economyClassCount;
	private Date startDate;
	private Date endDate;
	
	public String getAeroplaneNumber() {
		return aeroplaneNumber;
	}
	public void setAeroplaneNumber(String aeroplaneNumber) {
		this.aeroplaneNumber = aeroplaneNumber;
	}
	public int getBusinessClassCount() {
		return businessClassCount;
	}
	public void setBusinessClassCount(int businessClassCount) {
		this.businessClassCount = businessClassCount;
	}
	public int getEconomyClassCount() {
		return economyClassCount;
	}
	public void setEconomyClassCount(int economyClassCount) {
		this.economyClassCount = economyClassCount;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	
}
