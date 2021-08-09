package com.ey.in.tds.dto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 * @author scriptbees
 *
 */
public interface SurchargeAndCessRateDTO {

	Long getInvoiceSlabFrom();

	Long getInvoiceSlabTo();

	Date getApplicableFrom();

	Date getApplicableTo();

	BigDecimal getRate();
}
