package com.ey.in.tds.dividend.forms.builder.ca;

import java.math.BigDecimal;
import java.util.Date;

public interface RemittancePartBBuilder {

	public AOOrderBuilder remittance(String countryRemittanceMade, String currencyRemittanceMade,
			BigDecimal amountPayableInForegnCurrency, BigDecimal amountPayableInINR, String bankNameCode,
			String bankBranch, String bankBranchBSRCode, Date proposedDateOfRemittance, String natureOfRemittance,
			BigDecimal amountOfTDS, BigDecimal rateOfTDS, Date dateOfDeduction, String releventPurposeCategoryRBI,
			String releventPurposeCodeRBI);
}
