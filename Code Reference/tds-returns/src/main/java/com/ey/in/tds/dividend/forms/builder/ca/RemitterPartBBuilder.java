package com.ey.in.tds.dividend.forms.builder.ca;

public interface RemitterPartBBuilder {

	public RemitteePartBBuilder remitterPB(String name, String pan, String tan, String email, String phone, String status,
			String domestic, final String flatDoorBuilding, String roadStreet,
			String premisesBuildingVillage, final String townCityDistrict, final String areaLocality,
			final String zipCode, final String state, final String country);
}
