package com.ey.in.tds.dto;

import java.io.Serializable;

public class AddressDTO implements Serializable {

	private static final long serialVersionUID = 5407049775492037616L;

	private Long id;

	private String flatDoorBlockNo;

	private String nameBuildingVillage;

	private String roadStreetPostoffice;

	private String townCityDistrict;

	private String areaLocality;

	private String pinCode;

	private Long stateId;

	private String stateName;

	private String countryName;

	private Long countryId;

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public Long getCountryId() {
		return countryId;
	}

	public void setCountryId(Long countryId) {
		this.countryId = countryId;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public Long getStateId() {
		return stateId;
	}

	public void setStateId(Long stateId) {
		this.stateId = stateId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFlatDoorBlockNo() {
		return flatDoorBlockNo;
	}

	public void setFlatDoorBlockNo(String flatDoorBlockNo) {
		this.flatDoorBlockNo = flatDoorBlockNo;
	}

	public String getNameBuildingVillage() {
		return nameBuildingVillage;
	}

	public void setNameBuildingVillage(String nameBuildingVillage) {
		this.nameBuildingVillage = nameBuildingVillage;
	}

	public String getRoadStreetPostoffice() {
		return roadStreetPostoffice;
	}

	public void setRoadStreetPostoffice(String roadStreetPostoffice) {
		this.roadStreetPostoffice = roadStreetPostoffice;
	}

	public String getTownCityDistrict() {
		return townCityDistrict;
	}

	public void setTownCityDistrict(String townCityDistrict) {
		this.townCityDistrict = townCityDistrict;
	}

	public String getAreaLocality() {
		return areaLocality;
	}

	public void setAreaLocality(String areaLocality) {
		this.areaLocality = areaLocality;
	}

	public String getPinCode() {
		return pinCode;
	}

	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}

	@Override
	public String toString() {
		return "AddressDTO [id=" + id + ", flatDoorBlockNo=" + flatDoorBlockNo + ", nameBuildingVillage="
				+ nameBuildingVillage + ", roadStreetPostoffice=" + roadStreetPostoffice + ", townCityDistrict="
				+ townCityDistrict + ", areaLocality=" + areaLocality + ", pinCode=" + pinCode + ", stateId=" + stateId
				+ ", stateName=" + stateName + ", countryName=" + countryName + ", countryId=" + countryId + "]";
	}

}
