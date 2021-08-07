package com.ey.in.tds.dto;

import java.io.Serializable;

public class StatusDTO implements Serializable {

	private static final long serialVersionUID = -5840276103218562244L;

	private Long id;

	private String status;

	private String panCode;

	private boolean active;

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPanCode() {
		return panCode;
	}

	public void setPanCode(String panCode) {
		this.panCode = panCode;
	}

	@Override
	public String toString() {
		return "StatusDTO [id=" + id + ", status=" + status + ", panCode=" + panCode + ", active=" + active + "]";
	}

}
