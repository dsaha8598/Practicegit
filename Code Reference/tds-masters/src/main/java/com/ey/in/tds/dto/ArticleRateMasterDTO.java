package com.ey.in.tds.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
/**
 * 
 * @author vamsir
 *
 */
public class ArticleRateMasterDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String country;
	private String articleNumber;
	private String articleName;
	private String isInclusionOrExclusion;
	private BigDecimal articleRate;
	private Date applicableFrom;
	private Date applicableTo;
	private Boolean active;
	private String natureOfRemittance;
	private Boolean makeAvailableConditionSatisfied;
	private Boolean mfnClauseExists;
	private String mfnApplicableToScopeOrRate;
	private Boolean mliPrinciplePurpose;
	private Boolean mliSimplifiedLimitation;
	private BigDecimal mfnClauseIsAvailed;
	private BigDecimal mfnClauseIsNotAvailed;
	private String natureOfPayment;
	private String typeOfPayee;
	private String remarks;
	private Boolean nonExempt;
	private String detailedCondition;
	private Boolean conditionApplicable;
	private String condition;
	
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getArticleNumber() {
		return articleNumber;
	}
	public void setArticleNumber(String articleNumber) {
		this.articleNumber = articleNumber;
	}
	public String getArticleName() {
		return articleName;
	}
	public void setArticleName(String articleName) {
		this.articleName = articleName;
	}
	public String getIsInclusionOrExclusion() {
		return isInclusionOrExclusion;
	}
	public void setIsInclusionOrExclusion(String isInclusionOrExclusion) {
		this.isInclusionOrExclusion = isInclusionOrExclusion;
	}
	public BigDecimal getArticleRate() {
		return articleRate;
	}
	public void setArticleRate(BigDecimal articleRate) {
		this.articleRate = articleRate;
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
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public String getNatureOfRemittance() {
		return natureOfRemittance;
	}
	public void setNatureOfRemittance(String natureOfRemittance) {
		this.natureOfRemittance = natureOfRemittance;
	}
	public Boolean getMakeAvailableConditionSatisfied() {
		return makeAvailableConditionSatisfied;
	}
	public void setMakeAvailableConditionSatisfied(Boolean makeAvailableConditionSatisfied) {
		this.makeAvailableConditionSatisfied = makeAvailableConditionSatisfied;
	}
	public Boolean getMfnClauseExists() {
		return mfnClauseExists;
	}
	public void setMfnClauseExists(Boolean mfnClauseExists) {
		this.mfnClauseExists = mfnClauseExists;
	}
	public String getMfnApplicableToScopeOrRate() {
		return mfnApplicableToScopeOrRate;
	}
	public void setMfnApplicableToScopeOrRate(String mfnApplicableToScopeOrRate) {
		this.mfnApplicableToScopeOrRate = mfnApplicableToScopeOrRate;
	}
	public Boolean getMliPrinciplePurpose() {
		return mliPrinciplePurpose;
	}
	public void setMliPrinciplePurpose(Boolean mliPrinciplePurpose) {
		this.mliPrinciplePurpose = mliPrinciplePurpose;
	}
	public Boolean getMliSimplifiedLimitation() {
		return mliSimplifiedLimitation;
	}
	public void setMliSimplifiedLimitation(Boolean mliSimplifiedLimitation) {
		this.mliSimplifiedLimitation = mliSimplifiedLimitation;
	}
	public BigDecimal getMfnClauseIsAvailed() {
		return mfnClauseIsAvailed;
	}
	public void setMfnClauseIsAvailed(BigDecimal mfnClauseIsAvailed) {
		this.mfnClauseIsAvailed = mfnClauseIsAvailed;
	}
	public BigDecimal getMfnClauseIsNotAvailed() {
		return mfnClauseIsNotAvailed;
	}
	public void setMfnClauseIsNotAvailed(BigDecimal mfnClauseIsNotAvailed) {
		this.mfnClauseIsNotAvailed = mfnClauseIsNotAvailed;
	}
	public String getNatureOfPayment() {
		return natureOfPayment;
	}
	public void setNatureOfPayment(String natureOfPayment) {
		this.natureOfPayment = natureOfPayment;
	}
	public String getTypeOfPayee() {
		return typeOfPayee;
	}
	public void setTypeOfPayee(String typeOfPayee) {
		this.typeOfPayee = typeOfPayee;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public Boolean getNonExempt() {
		return nonExempt;
	}
	public void setNonExempt(Boolean nonExempt) {
		this.nonExempt = nonExempt;
	}
	public String getDetailedCondition() {
		return detailedCondition;
	}
	public void setDetailedCondition(String detailedCondition) {
		this.detailedCondition = detailedCondition;
	}
	public Boolean getConditionApplicable() {
		return conditionApplicable;
	}
	public void setConditionApplicable(Boolean conditionApplicable) {
		this.conditionApplicable = conditionApplicable;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}

}
