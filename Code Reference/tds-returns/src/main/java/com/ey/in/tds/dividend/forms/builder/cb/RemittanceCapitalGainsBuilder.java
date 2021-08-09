package com.ey.in.tds.dividend.forms.builder.cb;

import java.math.BigDecimal;

public interface RemittanceCapitalGainsBuilder {

	public RemittanceMisc capitalGains(BigDecimal longTermGains, BigDecimal shortTermGains, String basisOfDetermination);

	public RemittanceMisc noCapitalGains();
}
