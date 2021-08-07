package com.ey.in.tds.onboarding.dto.deductee;

import java.io.Serializable;

public class CustomDeducteesDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private Integer id;

	private String deducteeName;

	private String deducteePan;



	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDeducteeName() {
		return deducteeName;
	}

	public void setDeducteeName(String deducteeName) {
		this.deducteeName = deducteeName;
	}

	@Override
	public String toString() {
		return "CustomDeducteesDTO [id=" + id + ", deducteeName=" + deducteeName + ", deducteePan=" + deducteePan + "]";
	}

	public String getDeducteePan() {
		return deducteePan;
	}

	public void setDeducteePan(String deducteePan) {
		this.deducteePan = deducteePan;
	}
}
