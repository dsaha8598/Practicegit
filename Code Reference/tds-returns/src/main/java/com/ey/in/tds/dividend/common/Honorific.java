package com.ey.in.tds.dividend.common;

public enum Honorific {
	MR("Mr", "1"), MS("Ms", "2"), M_S("M/s", "3");

	private final String salutation;

	private final String numCode;

	private Honorific(final String salutation, final String numCode) {
		this.numCode = numCode;
		this.salutation = numCode;
	}

	public static Honorific bySalutation(String salutation) {
		for (Honorific honorific : Honorific.values()) {
			if (honorific.salutation.equalsIgnoreCase(salutation))
				return honorific;
		}
		throw new IllegalArgumentException("Invalid Salutation: " + salutation);
	}

	public String numCode() {
		return this.numCode;
	}
}
