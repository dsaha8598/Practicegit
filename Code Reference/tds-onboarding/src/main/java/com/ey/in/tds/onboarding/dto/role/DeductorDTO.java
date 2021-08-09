package com.ey.in.tds.onboarding.dto.role;

import java.util.List;
import java.util.Set;

public class DeductorDTO {

	private Set<String> tans;
	private List<RoleDto> roles;
	private List<RoleDto> rolesTcs;
	

	public Set<String> getTans() {
		return tans;
	}

	public void setTans(Set<String> tans) {
		this.tans = tans;
	}

	public List<RoleDto> getRoles() {
		return roles;
	}

	public void setRoles(List<RoleDto> roles) {
		this.roles = roles;
	}

	public List<RoleDto> getRolesTcs() {
		return rolesTcs;
	}

	public void setRolesTcs(List<RoleDto> rolesTcs) {
		this.rolesTcs = rolesTcs;
	}
	
}
