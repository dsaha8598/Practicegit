package com.ey.in.tds.tcs.web.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.TcsLookUp;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.tcs.service.TcsLookUpService;

@Repository
@RequestMapping("/api/masters")
public class TcsLookUpResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TcsLookUpService tcsLookUpService;

	/**
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/getAllLookupValues")
	public ResponseEntity<ApiStatus<List<TcsLookUp>>> getAllLookUpValues(
			@RequestHeader(value="X-TENANT-ID",required=false) String tenantId) {
		MultiTenantContext.setTenantId("master");

		logger.info("REST request to get all lookup values");
		List<TcsLookUp> getAllLookupValues = tcsLookUpService.getAllLookUpValue();
		ApiStatus<List<TcsLookUp>> apiStatus = new ApiStatus<List<TcsLookUp>>(HttpStatus.OK,
				"To get a Collector Look Up values", "NO ALERT", getAllLookupValues);
		return new ResponseEntity<ApiStatus<List<TcsLookUp>>>(apiStatus, HttpStatus.OK);
	}
}
