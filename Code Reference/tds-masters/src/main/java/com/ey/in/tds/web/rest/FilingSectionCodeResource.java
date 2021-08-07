package com.ey.in.tds.web.rest;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.FilingSectionCode;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.service.FilingSectionCodeService;

@RestController
@RequestMapping("/api/masters")
public class FilingSectionCodeResource {

	@Autowired
	private FilingSectionCodeService filingSectionCodeService;

	/**
	 * This API for create Section code
	 * 
	 * @param sectionCode
	 * @param userName
	 * @return
	 */
	@PostMapping(value = "/filing/sectioncode", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<FilingSectionCode>> createSectionCode(
			@Valid @RequestBody FilingSectionCode sectionCode, @RequestHeader(value = "USER_NAME") String userName) {
		MultiTenantContext.setTenantId("master");
		FilingSectionCode result = filingSectionCodeService.saveSectionCode(sectionCode, userName);
		ApiStatus<FilingSectionCode> apiStatus = new ApiStatus<FilingSectionCode>(HttpStatus.CREATED, "SUCCESS",
				"SECTION CODE IS CREATED", result);
		return new ResponseEntity<ApiStatus<FilingSectionCode>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get All Section code
	 * 
	 * @return
	 */
	@GetMapping(value = "/filing/sectioncode")
	public ResponseEntity<ApiStatus<List<FilingSectionCode>>> getAllSectionCode(@RequestHeader(value = "X-TENANT-ID",required = false) String tenantId) {
		MultiTenantContext.setTenantId("master");
		List<FilingSectionCode> result = filingSectionCodeService.getAllSectionCode();
		ApiStatus<List<FilingSectionCode>> apiStatus = new ApiStatus<List<FilingSectionCode>>(HttpStatus.OK, "SUCCESS",
				" GET ALL SECTION CODES RECORDS", result);
		return new ResponseEntity<ApiStatus<List<FilingSectionCode>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get Section code based on Id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/filing/sectioncode/{id}")
	public ResponseEntity<ApiStatus<FilingSectionCode>> getBySectionCodeId(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		FilingSectionCode result = filingSectionCodeService.getBySectionCodeId(id);
		ApiStatus<FilingSectionCode> apiStatus = new ApiStatus<FilingSectionCode>(HttpStatus.OK, "SUCCESS",
				" GET BY SECTION CODE RECORD BASED ON ID", result);
		return new ResponseEntity<ApiStatus<FilingSectionCode>>(apiStatus, HttpStatus.OK);
	}

}
