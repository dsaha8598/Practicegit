package com.ey.in.tds.dto;

import java.io.Serializable;

public class CessTypesDTO implements Serializable {

	private static final long serialVersionUID = 9072453218525486351L;

	private Long id;

	private String cessType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCessType() {
		return cessType;
	}

	public void setCessType(String cessType) {
		this.cessType = cessType;
	}

	@Override
	public String toString() {
		return "CessTypesDTO [id=" + id + ", cessType=" + cessType + "]";
	}

}
