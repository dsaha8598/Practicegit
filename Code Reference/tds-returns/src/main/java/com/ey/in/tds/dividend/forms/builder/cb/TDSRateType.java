package com.ey.in.tds.dividend.forms.builder.cb;

public enum TDSRateType {

	RATE_AS_PER_ACT("1"), RATE_AS_PER_TREATY("2");

	private final String numCode;

	private TDSRateType(final String numCode) {
		this.numCode = numCode;
	}

	public String numCode() {
		return this.numCode;
	}
}
