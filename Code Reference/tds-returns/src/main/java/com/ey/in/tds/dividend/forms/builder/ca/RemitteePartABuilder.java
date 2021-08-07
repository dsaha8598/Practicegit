package com.ey.in.tds.dividend.forms.builder.ca;

public interface RemitteePartABuilder {

	public RemittancePartABuilder remiteePA(String name, String pan, String email, String phone,
			String remittanceMadeInCountry, final String flatDoorBuilding, String roadStreet,
			String premisesBuildingVillage, final String townCityDistrict, final String areaLocality,
			final String zipCode, final String state, final String country);
}
