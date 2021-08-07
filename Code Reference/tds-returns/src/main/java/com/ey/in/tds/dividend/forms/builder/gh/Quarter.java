package com.ey.in.tds.dividend.forms.builder.gh;

public enum Quarter {

	Q1("-01-01"), Q2("-04-01"), Q3("-07-01"), Q4("-10-01");

	private final String startingDate;

	private Quarter(final String startingDate) {
		this.startingDate = startingDate;
	}

	public String startingDate(int year) {
		return year + this.startingDate;
	}
}
