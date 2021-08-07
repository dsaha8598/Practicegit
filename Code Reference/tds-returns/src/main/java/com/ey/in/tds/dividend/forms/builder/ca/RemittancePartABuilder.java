package com.ey.in.tds.dividend.forms.builder.ca;

import java.math.BigDecimal;
import java.util.Date;

public interface RemittancePartABuilder {

	public DeclarationBuilder remittance(BigDecimal amountWithoutTDS, BigDecimal accumulatedAmountInFY, String bankNameCode,
			String bankBranch, Date proposedDateOfRemittance, String natureOfRemittance, BigDecimal amountOfTDS,
			BigDecimal rateOfTDS, Date dateOfDeduction, String releventPurposeCategoryRBI,
			String releventPurposeCodeRBI);
}
