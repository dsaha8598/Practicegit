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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.admin.domain.UserRole;
import com.ey.in.tds.common.admin.service.UserRoleService;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api")
public class UserRoleController {

	@Autowired
	private UserRoleService userRoleService;

	@PostMapping("/userrole")
	@Timed
	public ResponseEntity<UserRole> createUserRole(@Valid @RequestBody UserRole userRole) {
		UserRole userRoleResponse = userRoleService.save(userRole);
		return new ResponseEntity<>(userRoleResponse, HttpStatus.OK);
	}

	@PutMapping("/userrole")
	@Timed
	public ResponseEntity<UserRole> updateUserRole(@Valid @RequestBody UserRole userRole) {
		UserRole userRoleResponse = userRoleService.save(userRole);
		return new ResponseEntity<>(userRoleResponse, HttpStatus.OK);
	}

	@GetMapping("/userrole")
	@Timed
	public ResponseEntity<List<UserRole>> getAllUserRoles() {
		List<UserRole> listOfUserRole = userRoleService.findAll();
		return new ResponseEntity<>(listOfUserRole, HttpStatus.OK);
	}

	@GetMapping("/userrole/{id}")
	@Timed
	public ResponseEntity<?> getUserRole(@PathVariable Long id) {
		UserRole userRole = userRoleService.findOne(id);
		return new ResponseEntity<>(userRole, HttpStatus.OK);
	}

}
