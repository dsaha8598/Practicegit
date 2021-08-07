package com.ey.in.tds.tcs.web.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.CollecteeType;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.tcs.service.TCSCollecteeTypeService;

/**
 * 
 * @author scriptbees
 *
 */
@RestController
@RequestMapping("/api/masters/tcs")
public class TCSCollecteeTypeResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TCSCollecteeTypeService collecteeTypeService;

	/**
	 * This api for get all collectee type
	 * 
	 * @return
	 */
	@GetMapping(value = "/collecteetypes", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<CollecteeType>>> getCollecteeType() {
		MultiTenantContext.setTenantId("master");
		List<CollecteeType> getAllStatus = collecteeTypeService.getCollecteeTypes();
		ApiStatus<List<CollecteeType>> apiStatus = new ApiStatus<List<CollecteeType>>(HttpStatus.OK, "SUCCESS",
				"LIST OF COLLECTEE TYPE STATUS", getAllStatus);
		return new ResponseEntity<ApiStatus<List<CollecteeType>>>(apiStatus, HttpStatus.OK);

	}
}
