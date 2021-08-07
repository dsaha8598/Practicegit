package com.ey.in.tds.dividend.forms.builder.cb;

import java.math.BigDecimal;
import java.util.Date;

import com.ey.in.tds.dividend.fifteen.cb.TDSDetails;

public interface TDSBuilder {

	public AccountantBuilder tds(final TDSDetails tdsDetails);

	public AccountantBuilder tds(TDSRateType rateType, final String tdsSection,
			final BigDecimal amountInForeignCurrency, final BigDecimal amountInINR, final BigDecimal tdsRate,
			final BigDecimal remittanceIncludingTDSInForeignCurrency, final Date deductionDate);
}
