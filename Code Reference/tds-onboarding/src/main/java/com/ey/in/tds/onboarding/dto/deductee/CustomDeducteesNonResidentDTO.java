package com.ey.in.tds.onboarding.dto.deductee;

import java.io.Serializable;

public class CustomDeducteesNonResidentDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Integer id;
	
	private String deducteeNames;
	
	private String deducteePan;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDeducteeNames() {
		return deducteeNames;
	}

	public void setDeducteeNames(String deducteeNames) {
		this.deducteeNames = deducteeNames;
	}

	public String getDeducteePan() {
		return deducteePan;
	}

	public void setDeducteePan(String deducteePan) {
		this.deducteePan = deducteePan;
	}

}
