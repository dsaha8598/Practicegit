package com.ey.in.tds.dividend.forms.builder.ca;

import java.math.BigDecimal;

public interface RemittanceRoyaltyDividendBuilder {

	public RemittanceBusinessIncomeBuilder notForRoyaltyOrDividend();

	public RemittanceBusinessIncomeBuilder royaltyOrDividend(String articleOfDTAA, BigDecimal rateAsPerDTAA);
}
