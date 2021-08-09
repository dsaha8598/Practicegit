package com.ey.in.tds.dividend.forms.builder.cb;

import java.math.BigDecimal;
import java.util.Date;

import com.ey.in.tds.dividend.fifteen.cb.RemittanceDetails;

public interface RemittanceBuilder {

	public ITActBuilder remittance(final RemittanceDetails remittanceDetails);

	public ITActBuilder remittance(final String countryRemitanceMadeIn, final String currencyRemitanceMadeIn,
			final BigDecimal amountPayableInForeignCurrency, final BigDecimal amountPayableInINR,
			final String bankNameCode, final String bankBranchName, final String bankBranchBsrCode,
			final Date proposedDateOfRemmitance, final String natureOfRemittance, String releventPurposeCategoryRBI,
			String releventPurposeCodeRBI, String taxPayableGrossedUp);
}
