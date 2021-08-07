package com.ey.in.tds.authorization.web.rest;

import java.util.List;

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

import com.ey.in.tds.common.admin.domain.Role;
import com.ey.in.tds.common.admin.service.RoleService;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.core.dto.RoleDTO;
import com.ey.in.tds.core.util.ApiStatus;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api/authorization")
public class RoleController {

	@Autowired
	private RoleService roleService;

	@PostMapping("/role")
	@Timed
	public ResponseEntity<Role> createRole(@Valid @RequestBody Role role,
			@RequestHeader(value = "X-USER-EMAIL") String userName) {
		Role roleResponse = roleService.save(role, userName);
		return new ResponseEntity<>(roleResponse, HttpStatus.OK);
	}

	@PutMapping("/role")
	@Timed
	public ResponseEntity<Role> updateRole(@Valid @RequestBody Role role,
			@RequestHeader(value = "X-USER-EMAIL") String userName) {
		Role roleResponse = roleService.save(role, userName);
		return new ResponseEntity<>(roleResponse, HttpStatus.OK);
	}

	@GetMapping("/role")
	@Timed
	public ResponseEntity<List<Role>> getAllRoles() {
		List<Role> listOfRole = roleService.findAll();
		return new ResponseEntity<>(listOfRole, HttpStatus.OK);
	}

	@GetMapping("/role/{id}")
	@Timed
	public ResponseEntity<?> getRole(@PathVariable Long id) {
		Role role = roleService.findOne(id);
		return new ResponseEntity<>(role, HttpStatus.OK);
	}

	@GetMapping("/rolebyname")
	@Timed
	public ResponseEntity<ApiStatus<List<RoleDTO>>> getAllRoleName() {
		List<RoleDTO> listOfRole = roleService.findAllRolesByName();
		ApiStatus<List<RoleDTO>> apiStatus = new ApiStatus<List<RoleDTO>>(HttpStatus.OK, "To get List of Roles",
				"Roles List Record", listOfRole);
		return new ResponseEntity<ApiStatus<List<RoleDTO>>>(apiStatus, HttpStatus.OK);
	}
	
	@GetMapping("/roleNames")
	public ResponseEntity<List<String>> getAllRoleNames() {
		MultiTenantContext.setTenantId("master");
		List<String> listOfRole = roleService.getAllRoleNames();
		return new ResponseEntity<>(listOfRole, HttpStatus.OK);
	}
}
