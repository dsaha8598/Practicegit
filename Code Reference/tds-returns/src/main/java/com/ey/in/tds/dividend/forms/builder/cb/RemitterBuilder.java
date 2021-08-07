package com.ey.in.tds.dividend.forms.builder.cb;

import com.ey.in.tds.dividend.common.Honorific;
import com.ey.in.tds.dividend.common.IorWe;
import com.ey.in.tds.dividend.fifteen.cb.RemitterDetails;

public interface RemitterBuilder {

	public RemitteeBuilder remitter(final RemitterDetails remitterDetails);
	
	public RemitteeBuilder remitter(final String IorWe, final String remitterHonorific,
			final String remitterName, final String pan, final String beneficiaryHonorific);
}
