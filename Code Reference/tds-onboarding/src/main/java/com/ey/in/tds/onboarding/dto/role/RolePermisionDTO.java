package com.ey.in.tds.onboarding.dto.role;

import java.util.List;

public class RolePermisionDTO {

	private Integer roleId;
	private String roleName;
	private List<Long> permissionIds;
	private List<String> permissionNames;
	private String deductorPan;
	private Integer moduleType;

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public List<String> getPermissionNames() {
		return permissionNames;
	}

	public void setPermissionNames(List<String> permissionNames) {
		this.permissionNames = permissionNames;
	}

	public RolePermisionDTO() {

	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public List<Long> getPermissionIds() {
		return permissionIds;
	}

	public void setPermissionIds(List<Long> permissionIds) {
		this.permissionIds = permissionIds;
	}

	public String getDeductorPan() {
		return deductorPan;
	}

	public void setDeductorPan(String deductorPan) {
		this.deductorPan = deductorPan;
	}

	public Integer getModuleType() {
		return moduleType;
	}

	public void setModuleType(Integer moduleType) {
		this.moduleType = moduleType;
	}
}
