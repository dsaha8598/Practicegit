package com.ey.in.tds.authorization.web.rest;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.admin.service.TenantService;
import com.ey.in.tds.common.domain.Tenant;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api")
public class TenantController {

	@Autowired
	private TenantService tenantService;

	@PostMapping("/tenant")
	@Timed
	public ResponseEntity<Tenant> createTenant(@Valid @RequestBody Tenant tenant,
			@RequestHeader(value = "X-USER-EMAIL") String userName) {
		Tenant tenantResponse = tenantService.save(tenant, userName);
		return new ResponseEntity<>(tenantResponse, HttpStatus.OK);
	}

	@PutMapping("/tenant")
	@Timed
	public ResponseEntity<Tenant> updateTenant(@Valid @RequestBody Tenant tenant,
			@RequestHeader(value = "X-USER-EMAIL") String userName) {
		Tenant tenantResponse = tenantService.save(tenant, userName);
		return new ResponseEntity<>(tenantResponse, HttpStatus.OK);
	}

	@GetMapping("/tenant")
	@Timed
	public ResponseEntity<List<Tenant>> getAllUsers() {
		List<Tenant> listOfTenants = tenantService.findAll();
		return new ResponseEntity<>(listOfTenants, HttpStatus.OK);
	}

}
