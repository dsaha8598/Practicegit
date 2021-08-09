package com.ey.in.tds.returns.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

public class AbstractFilingBean implements Serializable {

	private static final long serialVersionUID = 2010363036155379244L;

	public static void main(String[] args) {
	}

	public String getNullSafeValue(String value) {
		return StringUtils.isBlank(value) ? "" : value;
	}

	public String trim(String value, int length) {
		if (StringUtils.isBlank(value)) {
			return StringUtils.EMPTY;
		} else {
			if (value.trim().length() > length) {
				return value.trim().substring(0, length);
			} else {
				return value.trim();
			}
		}
	}
	
	protected String cleanseData(String text) {
		return text.replaceAll("[^a-zA-Z0-9\\s]", StringUtils.EMPTY).trim();
	}
}
