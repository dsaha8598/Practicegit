package com.ey.in.tds.onboarding.dto.deductor;

import javax.validation.constraints.NotNull;

public class ConfigValues {

	@NotNull(message = "config code should not be null")
	private String configCode;

	@NotNull(message = "config value should not be null")
	private String configValue;

	public String getConfigCode() {
		return configCode;
	}

	public void setConfigCode(String configCode) {
		this.configCode = configCode;
	}

	public String getConfigValue() {
		return configValue;
	}

	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}

	@Override
	public String toString() {
		return "ConfigValuesDTO [configCode=" + configCode + ", configValue=" + configValue + "]";
	}

}
