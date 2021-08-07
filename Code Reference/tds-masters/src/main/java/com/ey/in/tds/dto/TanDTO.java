package com.ey.in.tds.dto;

import java.io.Serializable;

public class TanDTO implements Serializable {

	private static final long serialVersionUID = 6795079363676217572L;

	private Long id;

	private String label;

	private AddressDTO address;

	private boolean active;

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AddressDTO getAddress() {
		return address;
	}

	public void setAddress(AddressDTO address) {
		this.address = address;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "TanDTO [id=" + id + ", label=" + label + ", address=" + address + ", active=" + active + "]";
	}

}
