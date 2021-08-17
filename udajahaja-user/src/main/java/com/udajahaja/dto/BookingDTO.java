package com.udajahaja.dto;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;

public class BookingDTO {

	   @Column(name="airline_id")
	   private int airlineId;
	   
	   private int flightId;
	   
	   private Date journeydate;
	   
	   private String from;
	   
	   private String to;
	   
	   private String departureTime;
	   
	   private String arrivalTime;
	   
	   private Boolean isEconomy;
	   
	   private Boolean isBusinessClass;
	   
	   private double totalPrice;
	   
	   private String email;

	public int getAirlineId() {
		return airlineId;
	}

	public void setAirlineId(int airlineId) {
		this.airlineId = airlineId;
	}

	public int getFlightId() {
		return flightId;
	}

	public void setFlightId(int flightId) {
		this.flightId = flightId;
	}

	public Date getJourneydate() {
		return journeydate;
	}

	public void setJourneydate(Date journeydate) {
		this.journeydate = journeydate;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}

	public String getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public Boolean getIsEconomy() {
		return isEconomy;
	}

	public void setIsEconomy(Boolean isEconomy) {
		this.isEconomy = isEconomy;
	}

	public Boolean getIsBusinessClass() {
		return isBusinessClass;
	}

	public void setIsBusinessClass(Boolean isBusinessClass) {
		this.isBusinessClass = isBusinessClass;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	   
	   
	   
}
