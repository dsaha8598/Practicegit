package com.ey.in.tds.dividend.forms.builder.ca;

import java.util.Date;

import com.ey.in.tds.dividend.forms.builder.Generatable;

public interface DeclarationBuilder {

	public Generatable declaration(String salutation, String name, String gaurdian, String designation,
			Date verificationDate, String verificationPlace);
}
