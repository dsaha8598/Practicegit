package com.ey.in.tds.dividend.forms.builder.gh;

public interface FilingDetailsBuilder<T> {

	public T original(String tan, Quarter quarter, int financialYear, String acknowlegementNo);

	public T correction(String tan, Quarter quarter, int financialYear, String acknowlegementNo,
			CorrectionType correctionType);
}
