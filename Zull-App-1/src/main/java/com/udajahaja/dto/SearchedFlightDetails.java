package com.udajahaja.dto;

import java.util.Date;

public class SearchedFlightDetails {

	private int id;
	public int airlineId;
	public int flightId;
	public String fromPlace;
	public String toPlace;
	public String departureTime;
	public String arrivalTime;
	public String economyTicketCost;
	public String businessTicketCost;
	private String airlineName;
	private String flightName;
	private String isReturn;
	private Date startDate;
	private Date returnDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
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

	public String getFromPlace() {
		return fromPlace;
	}

	public void setFromPlace(String fromPlace) {
		this.fromPlace = fromPlace;
	}

	public String getToPlace() {
		return toPlace;
	}

	public void setToPlace(String toPlace) {
		this.toPlace = toPlace;
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

	public String getEconomyTicketCost() {
		return economyTicketCost;
	}

	public void setEconomyTicketCost(String economyTicketCost) {
		this.economyTicketCost = economyTicketCost;
	}

	public String getBusinessTicketCost() {
		return businessTicketCost;
	}

	public void setBusinessTicketCost(String businessTicketCost) {
		this.businessTicketCost = businessTicketCost;
	}

	public String getAirlineName() {
		return airlineName;
	}

	public void setAirlineName(String airlineName) {
		this.airlineName = airlineName;
	}

	public String getFlightName() {
		return flightName;
	}

	public void setFlightName(String flightName) {
		this.flightName = flightName;
	}

	public String getIsReturn() {
		return isReturn;
	}

	public void setIsReturn(String isReturn) {
		this.isReturn = isReturn;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(Date returnDate) {
		this.returnDate = returnDate;
	}
	

}
