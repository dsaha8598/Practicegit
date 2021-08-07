package com.ey.in.tds.dividend.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    /**
     *  Input: DD-MM-YYYY
     *  Output: DD/MM/YYYY
     * @param inputDate
     * @return
     * @throws ParseException
     */
    public static String convertDateFormat(String inputDate) throws ParseException {
        DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = originalFormat.parse(inputDate);
        return targetFormat.format(date);
    }
}
