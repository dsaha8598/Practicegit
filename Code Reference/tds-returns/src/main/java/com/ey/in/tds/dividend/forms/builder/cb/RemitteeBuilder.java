package com.ey.in.tds.dividend.forms.builder.cb;

import com.ey.in.tds.dividend.fifteen.cb.RemitteeDetls;

public interface RemitteeBuilder {

	public RemittanceBuilder remittee(final RemitteeDetls remitteeDetails);

	public RemittanceBuilder remittee(final String remitteeName, final String flatDoorBuilding, String roadStreet,
			String premisesBuildingVillage, final String townCityDistrict, final String areaLocality,
			final String zipCode, final String state, final String country);
}
