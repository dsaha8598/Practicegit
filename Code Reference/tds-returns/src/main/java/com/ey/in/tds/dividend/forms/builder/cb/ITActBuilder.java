package com.ey.in.tds.dividend.forms.builder.cb;

import java.math.BigDecimal;

import com.ey.in.tds.dividend.fifteen.cb.ItActDetails;

public interface ITActBuilder {

	public DTAABuilder itAct(final ItActDetails itActDetails);

	public DTAABuilder notTaxableAsPerItActInIndia(String reason);

	public DTAABuilder taxableAsPerItActInIndia(String section, BigDecimal amountChargeableToTax,
			BigDecimal taxLiability, String basisOfDetermination);
}
