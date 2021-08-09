package com.ey.in.tds.dividend.forms.builder.ca;

import java.math.BigDecimal;

public interface ITActBuilder {

	public DTAABuilder actDetail(String section, BigDecimal amountChargeableToTax, BigDecimal taxLiability,
			String basisOfDetermination);
}
