package com.ey.in.tds.dto;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ArticleMasterDetailedConditionsDTO implements Serializable {

	private static final long serialVersionUID = -6892558996463027559L;

	/**
	 * DTO for ArticleMasterDetailedConditions Entity
	 */

	private Long id;

	@NotNull(message = "condition is mandatory")
	@NotBlank(message = "condition is mandatory")
	@Size(max = 256)
	private String condition;

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

	@Override
	public String toString() {
		return "ArticleMasterDetailedConditionsDTO [id=" + id + ", condition=" + condition + "]";
	}
}
