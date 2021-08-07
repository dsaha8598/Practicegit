package com.ey.in.tds.dividend.forms.builder.ca;

import java.math.BigDecimal;

public interface DTAABuilder {

	public RemittanceRoyaltyDividendBuilder ldcApplied(String releventDTAA, String natureOfPaymentDTAA, BigDecimal taxableIncomeAsPerDTAAINR,
			BigDecimal taxAsPerDTAAINR);

	public RemittanceRoyaltyDividendBuilder noLDC();

	public TDSBuilder dtaa(String taxResidencyCertificateAvailable, String relevantDtaa, String RelevantArtDtaa,
						   BigDecimal TaxIncDtaa, BigDecimal TaxLiablDtaa, String RemForRoyFlg,
						   String ArtDtaa, BigDecimal RateTdsADtaa, String RemAcctBusIncFlg,
						   String RemOnCapGainFlg, String OtherRemDtaa, String TaxIndDtaaFlg);
}
