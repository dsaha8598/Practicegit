package com.udajahaja.dto;

import java.io.File;
import java.util.List;

public class AirlineSaveDTO {

	private int id;
	private String airlineName;
	private long contactNumber;
	private String contactAddress;
	private File airlineLogo;
	private List<AeroplaneSaveDTO> aeroplanes;
	
	public String getAirlineName() {
		return airlineName;
	}
	public void setAirlineName(String airlineName) {
		this.airlineName = airlineName;
	}
	public long getContactNumber() {
		return contactNumber;
	}
	public void setContactNumber(long contactNumber) {
		this.contactNumber = contactNumber;
	}
	public String getContactAddress() {
		return contactAddress;
	}
	public void setContactAddress(String contactAddress) {
		this.contactAddress = contactAddress;
	}
	public File getAirlineLogo() {
		return airlineLogo;
	}
	public void setAirlineLogo(File airlineLogo) {
		this.airlineLogo = airlineLogo;
	}
	public List<AeroplaneSaveDTO> getAeroplanes() {
		return aeroplanes;
	}
	public void setAeroplanes(List<AeroplaneSaveDTO> aeroplanes) {
		this.aeroplanes = aeroplanes;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	
}
