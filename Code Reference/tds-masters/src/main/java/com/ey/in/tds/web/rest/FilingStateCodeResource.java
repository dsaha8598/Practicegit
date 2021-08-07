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
import com.ey.in.tds.common.domain.FilingStateCode;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.service.FilingStateCodeService;

@RestController
@RequestMapping("/api/masters")
public class FilingStateCodeResource {

	@Autowired
	private FilingStateCodeService filingStateCodeService;

	/**
	 * This API for create State Code
	 * 
	 * @param stateCode
	 * @param userName
	 * @return
	 */
	@PostMapping(value = "/filing/statecode", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<FilingStateCode>> createStateCode(@Valid @RequestBody FilingStateCode stateCode,
			@RequestHeader(value = "USER_NAME") String userName) {
		MultiTenantContext.setTenantId("master");
		FilingStateCode result = filingStateCodeService.saveStateCode(stateCode, userName);
		ApiStatus<FilingStateCode> apiStatus = new ApiStatus<FilingStateCode>(HttpStatus.CREATED, "SUCCESS",
				"STATE CODE IS CREATED", result);
		return new ResponseEntity<ApiStatus<FilingStateCode>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get all State Codes
	 * 
	 * @return
	 */
	@GetMapping(value = "/filing/statecode")
	public ResponseEntity<ApiStatus<List<FilingStateCode>>> getAllStateCode(
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId) {
		MultiTenantContext.setTenantId("master");
		List<FilingStateCode> result = filingStateCodeService.getAllStateCode();
		ApiStatus<List<FilingStateCode>> apiStatus = new ApiStatus<List<FilingStateCode>>(HttpStatus.OK, "SUCCESS",
				" GET ALL STATE CODES RECORDS", result);
		return new ResponseEntity<ApiStatus<List<FilingStateCode>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get State Code based on Id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/filing/statecode/{id}")
	public ResponseEntity<ApiStatus<FilingStateCode>> getByStateCodeId(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		FilingStateCode result = filingStateCodeService.getByStateCodeId(id);
		ApiStatus<FilingStateCode> apiStatus = new ApiStatus<FilingStateCode>(HttpStatus.OK, "SUCCESS",
				" GET BY STATE CODE RECORD BASED ON ID", result);
		return new ResponseEntity<ApiStatus<FilingStateCode>>(apiStatus, HttpStatus.OK);
	}

}
