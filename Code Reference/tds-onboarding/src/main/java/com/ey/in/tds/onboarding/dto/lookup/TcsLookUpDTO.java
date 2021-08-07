package com.ey.in.tds.onboarding.dto.lookup;

import java.io.Serializable;

public class TcsLookUpDTO implements Serializable {

	/**
	 * DTO for TCS LookUp entity
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;

	private String selectedValue;

	private String module;

	private Integer isMultiSelection;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSelectedValue() {
		return selectedValue;
	}

	public void setSelectedValue(String selectedValue) {
		this.selectedValue = selectedValue;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public Integer getIsMultiSelection() {
		return isMultiSelection;
	}

	public void setIsMultiSelection(Integer isMultiSelection) {
		this.isMultiSelection = isMultiSelection;
	}

	@Override
	public String toString() {
		return "TcsLookUpDTO [id=" + id + ", selectedValue=" + selectedValue + ", module=" + module
				+ ", isMultiSelection=" + isMultiSelection + "]";
	}
}
