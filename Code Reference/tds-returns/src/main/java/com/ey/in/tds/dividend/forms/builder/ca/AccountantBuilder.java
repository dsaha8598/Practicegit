package com.ey.in.tds.dividend.forms.builder.ca;

import java.util.Date;

public interface AccountantBuilder {

	public AOOrderBuilder accountant(String name, String firmName, String membershipNumber, String certificateNo,
			Date certificateDate, final String flatDoorBuilding, String roadStreet, String premisesBuildingVillage,
			final String townCityDistrict, final String areaLocality, final String pinCode, final String state,
			final String country);
}
