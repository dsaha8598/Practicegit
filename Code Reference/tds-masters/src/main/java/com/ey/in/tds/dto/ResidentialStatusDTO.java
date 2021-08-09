package com.ey.in.tds.dto;

import java.io.Serializable;

public class ResidentialStatusDTO implements Serializable {

	private static final long serialVersionUID = -8904774709573969032L;

	private Long id;
	private String status;

	public ResidentialStatusDTO() {
		super();
	}

	public ResidentialStatusDTO(Long id, String status) {
		super();
		this.id = id;
		this.status = status;
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

	@Override
	public String toString() {
		return "ResidentialStatusDTO [id=" + id + ", status=" + status + "]";
	}

}
