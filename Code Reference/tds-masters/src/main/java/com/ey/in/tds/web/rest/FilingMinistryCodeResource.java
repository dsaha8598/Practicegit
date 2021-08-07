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
import com.ey.in.tds.common.domain.FilingMinistryCode;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.service.FilingMinistryCodeService;

@RestController
@RequestMapping("/api/masters")
public class FilingMinistryCodeResource {

	@Autowired
	private FilingMinistryCodeService filingMinistryCodeService;

	/**
	 * This API for create Ministry Code 
	 * 
	 * @param ministryCode
	 * @param userName
	 * @return
	 */
	@PostMapping(value = "/filing/ministrycode", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<FilingMinistryCode>> createMinistryCode(
			@Valid @RequestBody FilingMinistryCode ministryCode, @RequestHeader(value = "USER_NAME") String userName) {
		MultiTenantContext.setTenantId("master");
		FilingMinistryCode result = filingMinistryCodeService.saveMinistryCode(ministryCode, userName);
		ApiStatus<FilingMinistryCode> apiStatus = new ApiStatus<FilingMinistryCode>(HttpStatus.CREATED, "SUCCESS",
				"MINISTRY CODE IS CREATED", result);
		return new ResponseEntity<ApiStatus<FilingMinistryCode>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get All Ministry Code 
	 * 
	 * @return
	 */
	@GetMapping(value = "/filing/ministrycode")
	public ResponseEntity<ApiStatus<List<FilingMinistryCode>>> getAllMinistryCode(@RequestHeader(value = "X-TENANT-ID",required = false) String tenantId) {
		MultiTenantContext.setTenantId("master");
		List<FilingMinistryCode> result = filingMinistryCodeService.getAllMinistryCode();
		ApiStatus<List<FilingMinistryCode>> apiStatus = new ApiStatus<List<FilingMinistryCode>>(HttpStatus.OK, "SUCCESS",
				" GET ALL MINISTRY CODES RECORDS", result);
		return new ResponseEntity<ApiStatus<List<FilingMinistryCode>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get Ministry Code  based on Id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/filing/ministrycode/{id}")
	public ResponseEntity<ApiStatus<FilingMinistryCode>> getByMinistryCodeId(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		FilingMinistryCode result = filingMinistryCodeService.getByMinistryCodeId(id);
		ApiStatus<FilingMinistryCode> apiStatus = new ApiStatus<FilingMinistryCode>(HttpStatus.OK, "SUCCESS",
				" GET BY MINISTRY CODE RECORD BASED ON ID", result);
		return new ResponseEntity<ApiStatus<FilingMinistryCode>>(apiStatus, HttpStatus.OK);
	}

}
