package com.udajahaja.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

public class Scheduler {

	private int id;
	public int airlineId;
	public int flightId;
	public String fromPlace;
	public String toPlace;
	public String departureTime;
	public String arrivalTime;
	public String economyTicketCost;
	public String businessTicketCost;
	public boolean s;
	public boolean m;
	public boolean t;
	public boolean w;
	public boolean th;
	public boolean f;
	public boolean st;

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

	public boolean isS() {
		return s;
	}

	public void setS(boolean s) {
		this.s = s;
	}

	public boolean isM() {
		return m;
	}

	public void setM(boolean m) {
		this.m = m;
	}

	public boolean isT() {
		return t;
	}

	public void setT(boolean t) {
		this.t = t;
	}

	public boolean isW() {
		return w;
	}

	public void setW(boolean w) {
		this.w = w;
	}

	public boolean isTh() {
		return th;
	}

	public void setTh(boolean th) {
		this.th = th;
	}

	public boolean isF() {
		return f;
	}

	public void setF(boolean f) {
		this.f = f;
	}

	public boolean isSt() {
		return st;
	}

	public void setSt(boolean st) {
		this.st = st;
	}

}
