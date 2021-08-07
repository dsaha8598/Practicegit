package com.ey.in.tds.dividend.forms.builder.gh;

import com.ey.in.tds.dividend.forms.builder.g.ShareholderType;

public enum CorrectionType {

	ADDITION("A"), DELETION("D"), UPDATE("U");

	private final String numCode;

	private CorrectionType(final String numCode) {
		this.numCode = numCode;
	}

	public String numCode() {
		return this.numCode;
	}
}
