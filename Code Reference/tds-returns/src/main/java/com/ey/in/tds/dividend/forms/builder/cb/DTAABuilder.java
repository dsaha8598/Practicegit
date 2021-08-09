package com.ey.in.tds.dividend.forms.builder.cb;

import java.math.BigDecimal;

import com.ey.in.tds.dividend.fifteen.cb.DTAADetails;

public interface DTAABuilder {

    public TDSBuilder dtaa(DTAADetails dtaaDetails);

    public TDSBuilder dtaa(String taxResidencyCertificateAvailable, String relevantDtaa, String RelevantArtDtaa,
                           BigDecimal TaxIncDtaa, BigDecimal TaxLiablDtaa, String RemForRoyFlg,
                           String ArtDtaa, BigDecimal RateTdsADtaa,String RemAcctBusIncFlg,
                           String RemOnCapGainFlg,String OtherRemDtaa, String TaxIndDtaaFlg);

    public RemittanceRoyaltyDividendBuilder ldcApplied(String releventDTAA, String natureOfPaymentDTAA,
                                                       BigDecimal taxableIncomeAsPerDTAAINR, BigDecimal taxAsPerDTAAINR);

    public RemittanceRoyaltyDividendBuilder noLDC();

}
