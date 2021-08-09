package com.ey.in.tds.dividend.forms.builder.cb;

import java.math.BigDecimal;

public interface RemittanceBusinessIncomeBuilder {

	public RemittanceCapitalGainsBuilder businessIncome(BigDecimal taxableIncomeInIndia, String basisOfDetermination);

	public RemittanceCapitalGainsBuilder noBusinessIncome();
}
