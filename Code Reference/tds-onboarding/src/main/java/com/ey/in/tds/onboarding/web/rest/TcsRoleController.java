package com.ey.in.tds.onboarding.web.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.onboarding.jdbc.dto.RoleDTO;
import com.ey.in.tds.common.onboarding.response.dto.RoleResponseDTO;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.onboarding.dto.role.RolePermisionDTO;
import com.ey.in.tds.onboarding.service.role.PermissionServiceCassandra;

@RestController
@RequestMapping("/api/onboarding")
public class TcsRoleController {

	@Autowired
	private PermissionServiceCassandra permissionService;

	@PostMapping("/tcs/role/permission")
	public ResponseEntity<ApiStatus<RoleResponseDTO>> createTcsRolePermission(
			@RequestBody RolePermisionDTO rolePermisionDTO, @RequestHeader("USER_NAME") String userName) {
		RoleDTO dto = permissionService.saveRolePermisionInCassandra(rolePermisionDTO, userName);
		RoleResponseDTO response = permissionService.copyToEntity(dto);
		ApiStatus<RoleResponseDTO> apiStatus = new ApiStatus<RoleResponseDTO>(HttpStatus.OK,
				"Request to create a Role Record", "Role Record Created", response);
		return new ResponseEntity<ApiStatus<RoleResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/tcs/roles")
	public ResponseEntity<ApiStatus<List<RoleResponseDTO>>> getAllRoles() {
		List<RoleDTO> response = permissionService.fetchAllRoles("2"); // Hard coded the value to return all TCS roles.
		List<RoleResponseDTO> listResponse = permissionService.copyToListEntity(response);
		ApiStatus<List<RoleResponseDTO>> apiStatus = new ApiStatus<List<RoleResponseDTO>>(HttpStatus.OK,
				"Get all roles", "Get all roles", listResponse);
		return new ResponseEntity<ApiStatus<List<RoleResponseDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param collectorPAN
	 * @return
	 */
	@PostMapping("/tcs/getRolesByCollectorPan")
	public ResponseEntity<ApiStatus<List<RoleResponseDTO>>> getRolesByCollectorPan(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestBody Map<String, String> requestParams) {
		String collectorPan = requestParams.get("deductorPan");
		List<RoleDTO> listDTO = permissionService.getAllRolesByDeductorPan(collectorPan, "2"); // Hard coded value to bring
																							// all TCS roles using
																							// collector PAN.
		List<RoleResponseDTO> listResponse = permissionService.copyToListEntity(listDTO);
		ApiStatus<List<RoleResponseDTO>> apiStatus = new ApiStatus<List<RoleResponseDTO>>(HttpStatus.OK,
				"Get roles based on deductor pan", "NO ALERT", listResponse);
		return new ResponseEntity<ApiStatus<List<RoleResponseDTO>>>(apiStatus, HttpStatus.OK);
	}
}
