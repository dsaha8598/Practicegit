package com.ey.in.tds.onboarding.web.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.dto.PermissionsDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.RoleDTO;
import com.ey.in.tds.common.onboarding.response.dto.RoleResponseDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.onboarding.dto.role.RolePermisionDTO;
import com.ey.in.tds.onboarding.service.role.PermissionServiceCassandra;

@RestController
@RequestMapping("/api/onboarding")
public class RoleControllerCassandra {

	@Autowired
	private PermissionServiceCassandra permissionService;

	@PostMapping("/role/permission")
	public ResponseEntity<ApiStatus<RoleResponseDTO>> createRolePermission(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestBody RolePermisionDTO rolePermisionDTO,
			@RequestHeader("USER_NAME") String userName) {
		RoleDTO dto = permissionService.saveRolePermisionInCassandra(rolePermisionDTO, userName);
		RoleResponseDTO response = permissionService.copyToEntity(dto);
		ApiStatus<RoleResponseDTO> apiStatus = new ApiStatus<RoleResponseDTO>(HttpStatus.OK,
				"Request to create a Role Record", "Role Record Created", response);
		return new ResponseEntity<ApiStatus<RoleResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/role/{id}")
	public ResponseEntity<ApiStatus<RoleResponseDTO>> getRolePermission(@PathVariable Integer id,
			@RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		List<RoleDTO> listDto = permissionService.fetchRolePermision(id);
		if (!listDto.isEmpty()) {
			RoleResponseDTO response = permissionService.copyToEntity(listDto.get(0));
			ApiStatus<RoleResponseDTO> apiStatus = new ApiStatus<RoleResponseDTO>(HttpStatus.OK,
					"Request to get a Role Record", "Role Record Retrieved", response);
			return new ResponseEntity<ApiStatus<RoleResponseDTO>>(apiStatus, HttpStatus.OK);
		} else {
			throw new CustomException("Role Record not found in system", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/roles")
	public ResponseEntity<ApiStatus<List<RoleResponseDTO>>> getAllRoles() {
		List<RoleDTO> response = permissionService.fetchAllRoles("1"); // Hard coded value to return all TDS roles.
		List<RoleResponseDTO> listResponse = permissionService.copyToListEntity(response);
		ApiStatus<List<RoleResponseDTO>> apiStatus = new ApiStatus<List<RoleResponseDTO>>(HttpStatus.OK,
				"Get all roles", "Get all roles", listResponse);
		return new ResponseEntity<ApiStatus<List<RoleResponseDTO>>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping("/getRolesByDeductorPan")
	public ResponseEntity<ApiStatus<List<RoleResponseDTO>>> getRolesByDeductorPan(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestBody Map<String, String> requestParams) {
		String deductorPan = requestParams.get("deductorPan");
		List<RoleDTO> listDTO = permissionService.getAllRolesByDeductorPan(deductorPan,"1"); // Hard coded value to get
																								// TDS roles using
																								// deductor PAN.
		List<RoleResponseDTO> listResponse = permissionService.copyToListEntity(listDTO);
		ApiStatus<List<RoleResponseDTO>> apiStatus = new ApiStatus<List<RoleResponseDTO>>(HttpStatus.OK,
				"Get roles based on deductor pan", "NO ALERT", listResponse);
		return new ResponseEntity<ApiStatus<List<RoleResponseDTO>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/getAllroles")
	public ResponseEntity<ApiStatus<List<RoleDTO>>> fetchAllRoles(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam String roleId) {
		List<RoleDTO> roles = permissionService.fetchAllRoles(roleId);
		ApiStatus<List<RoleDTO>> apiStatus = new ApiStatus<List<RoleDTO>>(HttpStatus.OK, "Get all roles",
				"Get all roles", roles);
		return new ResponseEntity<ApiStatus<List<RoleDTO>>>(apiStatus, HttpStatus.OK);
	}
	
	@PostMapping("/updatepermissions")
	public ResponseEntity<ApiStatus<Boolean>> updatepermissions(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestBody PermissionsDTO permissionsDTO) {
		Boolean response = permissionService.updatePermissions(tenantId, permissionsDTO);
		ApiStatus<Boolean> apiStatus = new ApiStatus<Boolean>(HttpStatus.OK, "SUCCESS", "Updating role permissions",
				response);
		return new ResponseEntity<ApiStatus<Boolean>>(apiStatus, HttpStatus.OK);
	}

}
