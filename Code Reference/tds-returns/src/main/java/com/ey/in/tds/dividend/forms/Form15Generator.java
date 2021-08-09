package com.ey.in.tds.dividend.forms;

import com.ey.in.tds.dividend.fifteen.ca.FORM15CA;
import com.ey.in.tds.dividend.fifteen.cb.FORM15CB;
import com.ey.in.tds.dividend.fifteen.g.FORM15G;
import com.ey.in.tds.dividend.fifteen.h.FORM15H;
import com.ey.in.tds.dividend.forms.builder.Generatable;
import com.ey.in.tds.dividend.forms.builder.ca.Form15CABuilder;
import com.ey.in.tds.dividend.forms.builder.ca.Form15CAPartBuilder;
import com.ey.in.tds.dividend.forms.builder.cb.Form15CBBuilder;
import com.ey.in.tds.dividend.forms.builder.cb.RemitterBuilder;
import com.ey.in.tds.dividend.forms.builder.g.Form15GBasicDetailsBuilder;
import com.ey.in.tds.dividend.forms.builder.g.Form15GBuilder;
import com.ey.in.tds.dividend.forms.builder.gh.FilingDetailsBuilder;
import com.ey.in.tds.dividend.forms.builder.h.Form15HBasicDetailsBuilder;
import com.ey.in.tds.dividend.forms.builder.h.Form15HBuilder;

public class Form15Generator {

	public static RemitterBuilder form15CB(int assessmentYear, String fileName) {
		return new Form15CBBuilder(assessmentYear, fileName);
	}

	public static Generatable form15CB(FORM15CB form15CB, String fileName) {
		return new Form15CBBuilder(form15CB, fileName);
	}

	public static Generatable form15CA(FORM15CA form15CA, String fileName) {
		return new Form15CABuilder(form15CA, fileName);
	}

	public static Form15CAPartBuilder form15CA(int assessmentYear, String fileName) {
		return new Form15CABuilder(assessmentYear, fileName);
	}

	public static Generatable form15G(FORM15G form15G, String fileName) {
		return new Form15GBuilder(form15G, fileName);
	}

	public static FilingDetailsBuilder<Form15GBasicDetailsBuilder> form15G(int assessmentYear, String fileName) {
		return new Form15GBuilder(assessmentYear, fileName);
	}

	public static Generatable form15H(FORM15H form15H, String fileName) {
		return new Form15HBuilder(form15H, fileName);
	}

	public static FilingDetailsBuilder<Form15HBasicDetailsBuilder> form15H(int assessmentYear, String fileName) {
		return new Form15HBuilder(assessmentYear, fileName);
	}
}
