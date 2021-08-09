package com.ey.in.tds.dividend.forms.builder.ca;

public interface RemitteePartCBuilder {

	public RemittancePartCBuilder remiteePC(String name, String pan, String email, String phone, String status,
			String principlePlaceOfBiz, final String flatDoorBuilding, String roadStreet,
			String premisesBuildingVillage, final String townCityDistrict,
			final String areaLocality, final String zipCode, final String state, final String country);
}
