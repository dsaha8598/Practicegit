package com.ey.in.tds.dto;

import java.io.Serializable;
import java.time.Instant;

import javax.validation.constraints.Size;

public class CessTypeMasterDTO implements Serializable {

	private static final long serialVersionUID = -6479907350814211357L;

	private Long id;

	@Size(max = 128)
	private String cessType;

	private Instant applicableFrom;

	private Instant applicableTo;

	public CessTypeMasterDTO cessType(String cessType) {
		this.cessType = cessType;
		return this;
	}

	public CessTypeMasterDTO applicableFrom(Instant applicableFrom) {
		this.applicableFrom = applicableFrom;
		return this;
	}

	public CessTypeMasterDTO applicableTo(Instant applicableTo) {
		this.applicableTo = applicableTo;
		return this;
	}

	public String getCessType() {
		return cessType;
	}

	public void setCessType(String cessType) {
		this.cessType = cessType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	@Override
	public String toString() {
		return "CessTypeMasterDTO [id=" + id + ", type=" + cessType + ", applicableFrom=" + applicableFrom
				+ ", applicableTo=" + applicableTo + "]";
	}

}
