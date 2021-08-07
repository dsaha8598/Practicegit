package com.ey.in.tds.onboarding.dto.ao;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class CustomAoLdcMasterDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String filePath;

	private Set<AoLdcCsvDataDTO> csvData = new HashSet<>();

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Set<AoLdcCsvDataDTO> getCsvData() {
		return csvData;
	}

	public void setCsvData(Set<AoLdcCsvDataDTO> csvData) {
		this.csvData = csvData;
	}

	@Override
	public String toString() {
		return "CustomAoLdcMasterDTO [filePath=" + filePath + ", csvData=" + csvData + "]";
	}
}
