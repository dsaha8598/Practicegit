package com.ey.in.tds.dividend.forms.builder;

import com.ey.in.tds.dividend.forms.builder.gh.TaxDetailsBuilder;

public interface AddressBuilder<T> {

	public TaxDetailsBuilder<T> address(final String flatDoorBuilding, String roadStreet,
			String premisesBuildingVillage, final String townCityDistrict, final String areaLocality,
			final String pinCode, final String state);
}
