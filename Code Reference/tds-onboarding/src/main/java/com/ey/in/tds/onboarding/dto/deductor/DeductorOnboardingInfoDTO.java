package com.ey.in.tds.onboarding.dto.deductor;

import java.io.Serializable;

public class DeductorOnboardingInfoDTO implements Serializable {

	/**
	 * DTO for TenantConfig entity
	 */
	private static final long serialVersionUID = 1L;
	private String pan;
	private OnboardingConfigValuesDTO onboardingConfigValues;

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}
	
	public OnboardingConfigValuesDTO getOnboardingConfigValues() {
		return onboardingConfigValues;
	}

	public void setOnboardingConfigValues(OnboardingConfigValuesDTO onboardingConfigValues) {
		this.onboardingConfigValues = onboardingConfigValues;
	}

}
