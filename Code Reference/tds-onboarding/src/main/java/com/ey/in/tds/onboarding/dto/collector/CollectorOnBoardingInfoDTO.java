package com.ey.in.tds.onboarding.dto.collector;

import java.io.Serializable;

import com.ey.in.tds.onboarding.dto.deductor.OnboardingConfigValuesDTO;

public class CollectorOnBoardingInfoDTO implements Serializable {

	/**
	 * DTO for TenantConfig entity
	 */
	private static final long serialVersionUID = 1L;
	private String pan;
	private CollectorOnboardingConfigValuesDTO onboardingConfigValues;

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public CollectorOnboardingConfigValuesDTO getOnboardingConfigValues() {
		return onboardingConfigValues;
	}

	public void setOnboardingConfigValues(CollectorOnboardingConfigValuesDTO onboardingConfigValues) {
		this.onboardingConfigValues = onboardingConfigValues;
	}

	@Override
	public String toString() {
		return "CollectorOnBoardingInfoDTO [pan=" + pan + ", onboardingConfigValues=" + onboardingConfigValues + "]";
	}
}
