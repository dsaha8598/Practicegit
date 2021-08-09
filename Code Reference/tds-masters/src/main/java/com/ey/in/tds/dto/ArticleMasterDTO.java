package com.ey.in.tds.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ArticleMasterDTO implements Serializable {

	private static final long serialVersionUID = -5649572241978096511L;

	/**
	 * DTO for Article Master entity
	 */

	private Long id;
	@Size(max = 64)
	private String country;
	@NotNull(message = "article number is mandatory")
	@NotBlank(message = "article number is mandatory")
	@Size(max = 64)
	private String articleNumber;
	@NotNull(message = "article name is mandatory")
	@NotBlank(message = "article name is mandatory")
	@Size(max = 128, message = "article name cannot be more than 128 characters")
	private String articleName;
	@NotNull(message = "inclusion/exclusion is mandatory")
	private Boolean isInclusionOrExclusion;
	private Set<ArticleMasterConditionsDTO> articleMasterConditions;
	@NotNull(message = "article rate is mandatory")
	private BigDecimal articleRate;
	@NotNull(message = "applicable from date is mandatory")
	private Instant applicableFrom;
	private Instant applicableTo;
	private Boolean active;
	private String natureOfRemittance;
	private Boolean makeAvailableConditionSatisfied;
	private Boolean mfnClauseExists;
	private Instant mfnApplicableTo;
	private Boolean mliPrinciplePurpose;
	private Boolean mliSimplifiedLimitation;
	private BigDecimal mfnClauseIsAvailed;
	private BigDecimal mfnClauseIsNotAvailed;
	private String remarks;
	private Boolean exempt;

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public Boolean getIsInclusionOrExclusion() {
		return isInclusionOrExclusion;
	}

	public void setIsInclusionOrExclusion(Boolean isInclusionOrExclusion) {
		this.isInclusionOrExclusion = isInclusionOrExclusion;
	}

	public Set<ArticleMasterConditionsDTO> getArticleMasterConditions() {
		return articleMasterConditions;
	}

	public void setArticleMasterConditions(Set<ArticleMasterConditionsDTO> articleMasterConditions) {
		this.articleMasterConditions = articleMasterConditions;
	}

	public BigDecimal getArticleRate() {
		return articleRate;
	}

	public void setArticleRate(BigDecimal articleRate) {
		this.articleRate = articleRate;
	}

	public Instant getApplicableFrom() {
		return applicableFrom;
	}

	public void setApplicableFrom(Instant applicableFrom) {
		this.applicableFrom = applicableFrom;
	}

	public Instant getApplicableTo() {
		return applicableTo;
	}

	public void setApplicableTo(Instant applicableTo) {
		this.applicableTo = applicableTo;
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

	public Instant getMfnApplicableTo() {
		return mfnApplicableTo;
	}

	public void setMfnApplicableTo(Instant mfnApplicableTo) {
		this.mfnApplicableTo = mfnApplicableTo;
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

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Boolean getExempt() {
		return exempt;
	}

	public void setExempt(Boolean exempt) {
		this.exempt = exempt;
	}

	@Override
	public String toString() {
		return "ArticleMasterDTO [id=" + id + ", country=" + country + ", articleNumber=" + articleNumber
				+ ", articleName=" + articleName + ", isInclusionOrExclusion=" + isInclusionOrExclusion
				+ ", articleMasterConditions=" + articleMasterConditions + ", articleRate=" + articleRate
				+ ", applicableFrom=" + applicableFrom + ", applicableTo=" + applicableTo + ", active=" + active
				+ ", natureOfRemittance=" + natureOfRemittance + ", makeAvailableConditionSatisfied="
				+ makeAvailableConditionSatisfied + ", mfnClauseExists=" + mfnClauseExists + ", mfnApplicableTo="
				+ mfnApplicableTo + ", mliPrinciplePurpose=" + mliPrinciplePurpose + ", mliSimplifiedLimitation="
				+ mliSimplifiedLimitation + ", mfnClauseIsAvailed=" + mfnClauseIsAvailed + ", mfnClauseIsNotAvailed="
				+ mfnClauseIsNotAvailed + ", remarks=" + remarks + ", exempt=" + exempt + "]";
	}

}
