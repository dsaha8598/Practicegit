package com.ey.in.tds.dividend.forms.builder.gh;

import java.math.BigDecimal;
import java.util.Date;

public interface TaxDetailsBuilder<T> {

	public IncomeDetailsBuilder<T> taxDetails(boolean taxableUnderItAct, Integer latestAssesmentYear,
			BigDecimal estimatedIncome, BigDecimal estimatedTotalIncomePY, BigDecimal totalFormsFiled,
			BigDecimal aggregateAmountFormFiledFor, Date declarationDate, BigDecimal incomePaid, Date incomePaidDate);
}
