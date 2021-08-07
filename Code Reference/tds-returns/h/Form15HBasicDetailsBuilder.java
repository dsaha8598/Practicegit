package com.ey.in.tds.dividend.forms.builder.h;

import java.util.Date;

import com.ey.in.tds.dividend.forms.builder.AddressBuilder;
import com.ey.in.tds.dividend.forms.builder.gh.AddMoreBuilder;

public interface Form15HBasicDetailsBuilder {

	public AddressBuilder<AddMoreBuilder<Form15HBasicDetailsBuilder>> basicDetails(String assesseeName, String assesseePAN, Date assesseeDOB,
			int previousYear, String email, String stdCode, String telephone, String mobile);
}
