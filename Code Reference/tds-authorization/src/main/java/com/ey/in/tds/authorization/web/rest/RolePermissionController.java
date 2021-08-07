package com.ey.in.tds.authorization.web.rest;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.ey.in.tds.common.admin.domain.RolePermission;
import com.ey.in.tds.common.admin.service.RolePermissionService;
import com.ey.in.tds.core.dto.RolePermisionDTO;
import com.ey.in.tds.core.dto.RolePermissionsDTO;
import com.ey.in.tds.core.util.ApiStatus;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api/authorization")
public class RolePermissionController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private RolePermissionService rolePermissionService;

	@PostMapping("/rolepermission2")
	@Timed
	public ResponseEntity<RolePermission> createRolePermission(@Valid @RequestBody RolePermission role,
			@RequestHeader(value = "X-USER-EMAIL") String userName) {
		logger.info("Role ID : " + role.getRoleId());
		logger.info("Permission ID : " + role.getPermissionId());

		RolePermission roleResponse = rolePermissionService.save(role, userName);
		return new ResponseEntity<>(roleResponse, HttpStatus.OK);
	}

	/**
	 * This api creates the permission
	 * 
	 * @param rolePermission
	 * @return
	 */
	@PostMapping(value = "/rolepermission", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Permission>> createRolePermissions(
			@Valid @RequestBody RolePermissionsDTO rolePermission,
			@RequestHeader(value = "X-USER-EMAIL") String userName) {

		Permission result = rolePermissionService.createRolePermissions(rolePermission, userName);

		ApiStatus<Permission> apiStatus = new ApiStatus<Permission>(HttpStatus.OK, "SUCCESS",
				"Role permission created successfully", result);
		return new ResponseEntity<ApiStatus<Permission>>(apiStatus, HttpStatus.OK);
	}

	@PutMapping("/rolepermission")
	@Timed
	public ResponseEntity<RolePermission> updateCessTypeMaster(@Valid @RequestBody RolePermission role,
			@RequestHeader(value = "X-USER-EMAIL") String userName) {
		RolePermission roleResponse = rolePermissionService.save(role, userName);
		return new ResponseEntity<>(roleResponse, HttpStatus.OK);
	}

	@GetMapping("/rolepermission")
	@Timed
	public ResponseEntity<List<RolePermission>> getAllCessTypeMasters() {
		List<RolePermission> listOfRole = rolePermissionService.findAll();
		return new ResponseEntity<>(listOfRole, HttpStatus.OK);
	}

	@GetMapping("/rolepermission/{id}")
	@Timed
	public ResponseEntity<?> getCessTypeMaster(@PathVariable Long id) {
		RolePermission role = rolePermissionService.findOne(id);
		return new ResponseEntity<>(role, HttpStatus.OK);
	}

	/**
	 * This API will get all roles and permissions
	 * 
	 * @return
	 */
	@GetMapping("/rolepermissions")
	@Timed
	public ResponseEntity<List<RolePermisionDTO>> getAllRolesAndPermissions() {
		List<RolePermisionDTO> listOfRole = rolePermissionService.getAllRolesAndPermissions();
		return new ResponseEntity<>(listOfRole, HttpStatus.OK);
	}

}
