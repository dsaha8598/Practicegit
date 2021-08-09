package com.ey.in.tds.dividend.common;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class Form15DataUtil {

	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	public static XMLGregorianCalendar nowXmlDate() {
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(df.format(new Date()));
		} catch (DatatypeConfigurationException e) {
			throw new IllegalArgumentException("Error while creating XML date", e);
		}
	}

	public static XMLGregorianCalendar toXmlDate(Date date) {
		try {
			return date != null ? DatatypeFactory.newInstance().newXMLGregorianCalendar(df.format(date)) : null;
		} catch (DatatypeConfigurationException e) {
			throw new IllegalArgumentException("Invalid date: " + date, e);
		}
	}

	public static XMLGregorianCalendar toXmlDate(String date) {
		try {
			return date != null ? DatatypeFactory.newInstance().newXMLGregorianCalendar(date) : null;
		} catch (DatatypeConfigurationException e) {
			throw new IllegalArgumentException("Invalid date: " + date, e);
		}
	}

	public static String iOrWe(String salutation) {
		if (salutation != null) {
			if (salutation.equalsIgnoreCase("Mr") || salutation.equalsIgnoreCase("Ms")
					|| salutation.equalsIgnoreCase("M/s")) {
				return "01";
			} else {
				return "02";
			}
		} else {
			return null;
		}
	}

    public static String iOrWeForXmlGeneration(String salutation) {
        if (salutation != null) {
            if (salutation.equalsIgnoreCase("I")) {
                return "1";
            } else if (salutation.equalsIgnoreCase("We")) {
                return "2";
            }
        }
        return null;
    }

	public static BigDecimal bigDecimal(String val) {
		return val != null ? new BigDecimal(val) : null;
	}

	public static BigDecimal bigDecimal(Double val) {
		return val != null ? BigDecimal.valueOf(val) : null;
	}

	public static String yOrN(boolean flag) {
		return flag ? "Y" : "N";
	}

	public static boolean nullSafeBoolean(Boolean boo) {
		return boo == null || !boo ? false : true;
	}

}
