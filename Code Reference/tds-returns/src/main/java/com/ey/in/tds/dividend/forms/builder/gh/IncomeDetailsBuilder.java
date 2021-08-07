package com.ey.in.tds.dividend.forms.builder.gh;

import java.math.BigDecimal;

public interface IncomeDetailsBuilder<T> {

	public T incomeDetails(String uniqueIdentifiactionNo, String investmentAccountIdNo,
			String natureOfIncome, String tdsSection, BigDecimal incomeAmount);
}
