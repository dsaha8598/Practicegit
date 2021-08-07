package com.ey.in.tds.dividend.forms.builder.cb;

import com.ey.in.tds.dividend.fifteen.cb.AcctntDetls;
import com.ey.in.tds.dividend.forms.builder.Generatable;

public interface AccountantBuilder {

	public Generatable accountant(final AcctntDetls accountantDetails);

	public Generatable accountant(final String name, final String firmName, final String membershipNumber,
			final String registrationNumber, final String flatDoorBuilding, String roadStreet,
			String premisesBuildingVillage, final String townCityDistrict, final String areaLocality,
			final String pinCode, final String state, final String country);
}
