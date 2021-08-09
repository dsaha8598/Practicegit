package com.ey.in.tds.dividend.forms.builder.ca;

import java.util.Date;

public interface AOOrderBuilder {

	public DeclarationBuilder aoCertificate(String section, String officerName, String officerDesignation,
			Date dateOfCertificate, String certificateNumber,String orderAoFlg);
}
