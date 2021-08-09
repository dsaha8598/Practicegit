package com.ey.in.tds.dividend.forms.builder.g;

import com.ey.in.tds.dividend.forms.builder.AddressBuilder;
import com.ey.in.tds.dividend.forms.builder.gh.AddMoreBuilder;

public interface Form15GBasicDetailsBuilder {

	public AddressBuilder<AddMoreBuilder<Form15GBasicDetailsBuilder>> basicDetails(String assesseeName, String assesseePAN, ShareholderType shareholderType,
			int previousYear, String email, String stdCode, String telephone, String mobile);
}
