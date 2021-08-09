package com.ey.in.tds.ingestion.tcs.dto;

import java.io.Serializable;
import java.util.Date;
/**
 * 
 * @author scriptbees
 *
 */
public class TCSInvoiceMismatchesDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Integer assessmentYear;
	private String collectorTan;
	private Integer invoiceNumber;
	private Date postingDate;
	
	public Integer getAssessmentYear() {
		return assessmentYear;
	}
	public void setAssessmentYear(Integer assessmentYear) {
		this.assessmentYear = assessmentYear;
	}
	public String getCollectorTan() {
		return collectorTan;
	}
	public void setCollectorTan(String collectorTan) {
		this.collectorTan = collectorTan;
	}
	public Integer getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(Integer invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public Date getPostingDate() {
		return postingDate;
	}
	public void setPostingDate(Date postingDate) {
		this.postingDate = postingDate;
	}
	
}
