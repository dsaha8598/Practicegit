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
import com.ey.in.tds.common.domain.FilingMinorHeadCode;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.service.FilingMinorHeadCodeService;

@RestController
@RequestMapping("/api/master")
public class FilingMinorHeadCodeResource {
	
	@Autowired
	private FilingMinorHeadCodeService filingMinorHeadCodeService;

	/**
	 * This API for create Minor Head Code
	 * 
	 * @param minorHeadCode
	 * @param userName
	 * @return
	 */
	@PostMapping(value = "/filing/minorheadcode", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<FilingMinorHeadCode>> createMinorHeadCode(
			@Valid @RequestBody FilingMinorHeadCode minorHeadCode, @RequestHeader(value = "USER_NAME") String userName) {
		MultiTenantContext.setTenantId("master");
		FilingMinorHeadCode result = filingMinorHeadCodeService.saveMinorHeadCode(minorHeadCode, userName);
		ApiStatus<FilingMinorHeadCode> apiStatus = new ApiStatus<FilingMinorHeadCode>(HttpStatus.CREATED, "SUCCESS",
				"MINOR HEAD CODE IS CREATED", result);
		return new ResponseEntity<ApiStatus<FilingMinorHeadCode>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get All Minor Head Code
	 * 
	 * @return
	 */
	@GetMapping(value = "/filing/minorheadcode")
	public ResponseEntity<ApiStatus<List<FilingMinorHeadCode>>> getAllMinorHeadCode(@RequestHeader(value = "X-TENANT-ID",required = false) String tenantId) {
		MultiTenantContext.setTenantId("master");
		List<FilingMinorHeadCode> result = filingMinorHeadCodeService.getAllMinorCodeCode();
		System.out.println("GET ALL MINOR HEAD CODES RECORDS" +result);
		ApiStatus<List<FilingMinorHeadCode>> apiStatus = new ApiStatus<List<FilingMinorHeadCode>>(HttpStatus.OK, "SUCCESS",
				"GET ALL MINOR HEAD CODES RECORDS", result);
		return new ResponseEntity<ApiStatus<List<FilingMinorHeadCode>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get Minor Head Code based on Id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/filing/minorheadcode/{id}")
	public ResponseEntity<ApiStatus<FilingMinorHeadCode>> getByMinorHeadCodeId(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		FilingMinorHeadCode result = filingMinorHeadCodeService.getByMinistryCodeId(id);
		ApiStatus<FilingMinorHeadCode> apiStatus = new ApiStatus<FilingMinorHeadCode>(HttpStatus.OK, "SUCCESS",
				"GET BY MINOR HEAD CODE RECORD BASED ON ID", result);
		return new ResponseEntity<ApiStatus<FilingMinorHeadCode>>(apiStatus, HttpStatus.OK);
	}

	

}
