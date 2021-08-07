package com.ey.in.tds.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class SubNaturePaymentMasterDTO implements Serializable {

	private static final long serialVersionUID = -3807295804902127815L;

	private Long id;

	@NotNull(message = "Nature should not be null")
	private String nature;

	private String section;

	@JsonIgnore
	private NatureOfPaymentMaster naturePaymentMaster;

	public SubNaturePaymentMasterDTO() {
		super();
		this.nature = StringUtils.EMPTY;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public NatureOfPaymentMaster getNaturePaymentMaster() {
		return naturePaymentMaster;
	}

	public void setNaturePaymentMaster(NatureOfPaymentMaster naturePaymentMaster) {
		this.naturePaymentMaster = naturePaymentMaster;
	}

	@Override
	public String toString() {
		return "SubNaturePaymentMasterDTO [id=" + id + ", nature=" + nature + ", section=" + section
				+ ", naturePaymentMaster=" + naturePaymentMaster + "]";
	}

}
