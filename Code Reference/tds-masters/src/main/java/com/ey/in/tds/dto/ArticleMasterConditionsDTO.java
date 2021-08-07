package com.ey.in.tds.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ArticleMasterConditionsDTO implements Serializable {

	private static final long serialVersionUID = 5532270361232098783L;

	/**
	 * DTO for ArticleMasterConditions Entity
	 */

	private Long id;

	@NotNull(message = "condition is mandatory")
	@NotBlank(message = "condition is mandatory")
	@Size(max = 256)
	private String condition;

	private Boolean isDetailedConditionApplicable;

	private Set<ArticleMasterDetailedConditionsDTO> articleMasterDetailedConditions = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public Boolean getIsDetailedConditionApplicable() {
		return isDetailedConditionApplicable;
	}

	public void setIsDetailedConditionApplicable(Boolean isDetailedConditionApplicable) {
		this.isDetailedConditionApplicable = isDetailedConditionApplicable;
	}

	public Set<ArticleMasterDetailedConditionsDTO> getArticleMasterDetailedConditions() {
		return articleMasterDetailedConditions;
	}

	public void setArticleMasterDetailedConditions(
			Set<ArticleMasterDetailedConditionsDTO> articleMasterDetailedConditions) {
		this.articleMasterDetailedConditions = articleMasterDetailedConditions;
	}

	@Override
	public String toString() {
		return "ArticleMasterConditionsDTO [id=" + id + ", condition=" + condition + ", isDetailedConditionApplicable="
				+ isDetailedConditionApplicable + ", articleMasterDetailedConditions=" + articleMasterDetailedConditions
				+ "]";
	}

}
