package com.ey.in.tds.dto;

import java.io.Serializable;

public class TanDeductorDTO implements Serializable {

	private static final long serialVersionUID = -4176339718081794956L;

	private Long tanId;

	private String deductorName;

	public Long getTanId() {
		return tanId;
	}

	public void setTanId(Long tanId) {
		this.tanId = tanId;
	}

	public String getDeductorName() {
		return deductorName;
	}

	public void setDeductorName(String deductorName) {
		this.deductorName = deductorName;
	}

	@Override
	public String toString() {
		return "TanDeductorDTO [tanId=" + tanId + ", deductorName=" + deductorName + "]";
	}

}
