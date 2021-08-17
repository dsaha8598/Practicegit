package com.udajahaja.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="booking")
public class Booking {

	   @Id
	   @GeneratedValue(strategy = GenerationType.IDENTITY)
	   private Integer id;
	   
	   @Column(name="airline_id")
	   private int airlineId;
	   
	   @Column(name="fight_id")
	   private int flightId;
	   
	   @Column(name="journey_date")
	   private Date journeydate;
	   
	   @Column(name="from_place")
	   private String from;
	   
	   @Column(name="to_place")
	   private String to;
	   
	   @Column(name="departure_time")
	   private String departureTime;
	   
	   @Column(name="arrival_time")
	   private String arrivalTime;
	   
	   @Column(name="is_economy")
	   private int isEconomy;
	   
	   @Column(name="is_businessclass")
	   private int isBusinessClass;
	   
	   @Column(name="total_price")
	   private double totalPrice;
	   
	   @Column(name="email")
	   private String email;
	   
	   @Column(name="active")
	   private boolean active;
	   
	   @Column(name="pnr_number")
	   private String pnrNumber;
	   
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
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
	public int getIsEconomy() {
		return isEconomy;
	}
	public void setIsEconomy(int isEconomy) {
		this.isEconomy = isEconomy;
	}
	public int getIsBusinessClass() {
		return isBusinessClass;
	}
	public void setIsBusinessClass(int isBusinessClass) {
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
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public String getPnrNumber() {
		return pnrNumber;
	}
	public void setPnrNumber(String pnrNumber) {
		this.pnrNumber = pnrNumber;
	}
	
	   
	   
	   
}
