package com.udajahaja.entity;

import java.io.File;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="aeroplane")
public class AeroplaneEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(name="aeroplane_number")
	private String aeroplaneNumber;
	@Column(name="business_class_count")
	private int businessClassCount;
	@Column(name="economy_classCount")
	private int economyClassCount;
	@Column(name="start_date")
	private Date startDate;
	@Column(name="end_date")
	private Date endDate;
	@Column(name="active")
	private int active;
	@Column(name="airline_id")
	private Long airlineId;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
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
	public int getActive() {
		return active;
	}
	public void setActive(int active) {
		this.active = active;
	}
	public Long getAirlineId() {
		return airlineId;
	}
	public void setAirlineId(Long airlineId) {
		this.airlineId = airlineId;
	}
	
	
	
	
}
