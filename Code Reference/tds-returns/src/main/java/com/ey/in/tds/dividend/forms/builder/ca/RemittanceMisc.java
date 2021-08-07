package com.ey.in.tds.dividend.forms.builder.ca;

import java.math.BigDecimal;

public interface RemittanceMisc {

	public TDSBuilder miscIncomeTaxableInIndia(String natureOfRemittance, BigDecimal rateAsPerDTAA);

	public TDSBuilder miscIncomeNonTaxableInIndia(String natureOfRemittance, String articleOfDTAA);

	public TDSBuilder noMiscIncome();
}
