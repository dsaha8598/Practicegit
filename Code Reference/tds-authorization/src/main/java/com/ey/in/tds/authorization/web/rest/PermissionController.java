package com.ey.in.tds.authorization.web.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.admin.domain.Permission;
import com.ey.in.tds.common.admin.service.PermissionService;
import com.ey.in.tds.core.dto.PermissionDTO;
import com.ey.in.tds.core.dto.RolePermisionDTO;
import com.ey.in.tds.core.dto.RolePermisionIDDTO;
import com.ey.in.tds.core.util.ApiStatus;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api/authorization")
public class PermissionController {

	@Autowired
	private PermissionService permissionService;

	@PostMapping("/permission")
	@Timed
	public ResponseEntity<Permission> createPermission(@Valid @RequestBody Permission permission,
			@RequestHeader(value = "X-USER-EMAIL") String userName) {
		Permission permissionResponse = permissionService.save(permission, userName);
		return new ResponseEntity<>(permissionResponse, HttpStatus.OK);
	}

	@PutMapping("/permission")
	@Timed
	public ResponseEntity<Permission> updatePermission(@Valid @RequestBody Permission permission,
			@RequestHeader(value = "X-USER-EMAIL") String userName) {
		Permission permissionResponse = permissionService.save(permission, userName);
		return new ResponseEntity<>(permissionResponse, HttpStatus.OK);
	}

	@GetMapping("/permission")
	@Timed
	public ResponseEntity<PermissionDTO> getAllPermissions() {
		PermissionDTO permission = permissionService.findAll();
		return new ResponseEntity<>(permission, HttpStatus.OK);
	}

	@GetMapping("/permission/{id}")
	@Timed
	public ResponseEntity<?> getPermission(@PathVariable Long id) {
		Permission permission = permissionService.findOne(id);
		return new ResponseEntity<>(permission, HttpStatus.OK);
	}

	@PostMapping("/role/permission")
	@Timed
	public ResponseEntity<ApiStatus<String>> createRolePermission(
			@Valid @RequestBody RolePermisionIDDTO rolePermisionDTO,
			@RequestHeader(value = "X-USER-EMAIL") String userName) {
		String response = permissionService.saveRolePermision(rolePermisionDTO, userName);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", response, response);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/role/permission/{id}")
	@Timed
	public ResponseEntity<RolePermisionDTO> getRolePermission(@PathVariable Long id) {
		RolePermisionDTO response = permissionService.fetchRolePermision(id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
