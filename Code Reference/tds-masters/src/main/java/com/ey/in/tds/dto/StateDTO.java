package com.ey.in.tds.dto;

import java.io.Serializable;

public class StateDTO implements Serializable {

	private static final long serialVersionUID = 819838527038037580L;

	private Long id;

	private String name;

	private CountryDTO country;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CountryDTO getCountry() {
		return country;
	}

	public void setCountry(CountryDTO country) {
		this.country = country;
	}

	@Override
	public String toString() {
		return "StateDTO [id=" + id + ", name=" + name + ", country=" + country + "]";
	}

}
