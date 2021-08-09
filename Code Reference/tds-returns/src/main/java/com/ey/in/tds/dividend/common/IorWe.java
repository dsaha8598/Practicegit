package com.ey.in.tds.dividend.common;

public enum IorWe {

	I("1"), WE("2");

	private final String numCode;

	private IorWe(final String numCode) {
		this.numCode = numCode;
	}

	public String numCode() {
		return this.numCode;
	}
}
