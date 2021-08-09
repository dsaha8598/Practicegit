package com.ey.in.tds.onboarding.dto.collector;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.ey.in.tds.onboarding.dto.deductor.ConfigValues;

public class CollectorOnboardingConfigValuesDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private Date applicableFrom;
	private Date applicableTo;
	private String sectionDetermination;
	private String creditNotes;
	private String challanGeneration;
	private String gstImplication;
	private String tcsApplicability;
	private String lccTrackingNotification;
	private String buyerThresholdComputation;
	private String tdsTransaction;
	private List<String> scopeProcess;
	private List<String> invoiceProcessScope;
	private List<ConfigValues> priority;
	private String collectionReferenceId;
	private String documentOrPostingDate;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getApplicableFrom() {
		return applicableFrom;
	}

	public void setApplicableFrom(Date applicableFrom) {
		this.applicableFrom = applicableFrom;
	}

	public Date getApplicableTo() {
		return applicableTo;
	}

	public void setApplicableTo(Date applicableTo) {
		this.applicableTo = applicableTo;
	}

	public String getSectionDetermination() {
		return sectionDetermination;
	}

	public void setSectionDetermination(String sectionDetermination) {
		this.sectionDetermination = sectionDetermination;
	}

	public String getCreditNotes() {
		return creditNotes;
	}

	public void setCreditNotes(String creditNotes) {
		this.creditNotes = creditNotes;
	}

	public String getChallanGeneration() {
		return challanGeneration;
	}

	public void setChallanGeneration(String challanGeneration) {
		this.challanGeneration = challanGeneration;
	}

	public String getGstImplication() {
		return gstImplication;
	}

	public void setGstImplication(String gstImplication) {
		this.gstImplication = gstImplication;
	}

	public String getTcsApplicability() {
		return tcsApplicability;
	}

	public void setTcsApplicability(String tcsApplicability) {
		this.tcsApplicability = tcsApplicability;
	}

	public String getLccTrackingNotification() {
		return lccTrackingNotification;
	}

	public void setLccTrackingNotification(String lccTrackingNotification) {
		this.lccTrackingNotification = lccTrackingNotification;
	}

	public String getBuyerThresholdComputation() {
		return buyerThresholdComputation;
	}

	public void setBuyerThresholdComputation(String buyerThresholdComputation) {
		this.buyerThresholdComputation = buyerThresholdComputation;
	}

	public String getTdsTransaction() {
		return tdsTransaction;
	}

	public void setTdsTransaction(String tdsTransaction) {
		this.tdsTransaction = tdsTransaction;
	}

	public List<String> getScopeProcess() {
		return scopeProcess;
	}

	public void setScopeProcess(List<String> scopeProcess) {
		this.scopeProcess = scopeProcess;
	}

	public List<String> getInvoiceProcessScope() {
		return invoiceProcessScope;
	}

	public void setInvoiceProcessScope(List<String> invoiceProcessScope) {
		this.invoiceProcessScope = invoiceProcessScope;
	}

	public List<ConfigValues> getPriority() {
		return priority;
	}

	public void setPriority(List<ConfigValues> priority) {
		this.priority = priority;
	}

	public String getCollectionReferenceId() {
		return collectionReferenceId;
	}

	public void setCollectionReferenceId(String collectionReferenceId) {
		this.collectionReferenceId = collectionReferenceId;
	}

	public String getDocumentOrPostingDate() {
		return documentOrPostingDate;
	}

	public void setDocumentOrPostingDate(String documentOrPostingDate) {
		this.documentOrPostingDate = documentOrPostingDate;
	}

	@Override
	public String toString() {
		return "CollectorOnboardingConfigValuesDTO [id=" + id + ", applicableFrom=" + applicableFrom + ", applicableTo="
				+ applicableTo + ", sectionDetermination=" + sectionDetermination + ", creditNotes=" + creditNotes
				+ ", challanGeneration=" + challanGeneration + ", gstImplication=" + gstImplication
				+ ", tcsApplicability=" + tcsApplicability + ", lccTrackingNotification=" + lccTrackingNotification
				+ ", buyerThresholdComputation=" + buyerThresholdComputation + ", tdsTransaction=" + tdsTransaction
				+ ", scopeProcess=" + scopeProcess + ", invoiceProcessScope=" + invoiceProcessScope + ", priority="
				+ priority + ", collectionReferenceId=" + collectionReferenceId + ", documentOrPostingDate="
				+ documentOrPostingDate + "]";
	}
	
}
