package com.ey.in.tds.dividend.forms.builder.ca;

import java.math.BigDecimal;

public interface TDSBuilder {

	public AccountantBuilder tdsDetails(String tdsRateSection, BigDecimal taxAmountINR, BigDecimal taxAmountInForeignCurrency,
			BigDecimal tdsRate, BigDecimal remittanceAmountIncludingTDSInForeignCurrency);
}

