package com.ey.in.tds.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class NopCessRateSurageRateDTO implements Serializable {

	private static final long serialVersionUID = -8544324979665411813L;

	private Long id;

	private BigDecimal cessRate;

	private BigDecimal surchargeRate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getCessRate() {
		return cessRate;
	}

	public void setCessRate(BigDecimal cessRate) {
		this.cessRate = cessRate;
	}

	public BigDecimal getSurchargeRate() {
		return surchargeRate;
	}

	public void setSurchargeRate(BigDecimal surchargeRate) {
		this.surchargeRate = surchargeRate;
	}

	@Override
	public String toString() {
		return "NopCessRateSurageRateDTO [id=" + id + ", cessRate=" + cessRate + ", surchargeRate=" + surchargeRate
				+ "]";
	}

}
