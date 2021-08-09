package com.ey.in.tds.onboarding.dto.role;

import java.io.Serializable;

public class RoleDto implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Integer roleId;
	
	private String roleName;

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

}
