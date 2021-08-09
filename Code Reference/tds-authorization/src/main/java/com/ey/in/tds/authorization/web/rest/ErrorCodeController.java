package com.ey.in.tds.authorization.web.rest;

import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.ey.in.tds.common.domain.ErrorCode;
import com.ey.in.tds.common.service.ErrorCodeService;
import com.ey.in.tds.core.util.ApiStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api/error-code")
public class ErrorCodeController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ErrorCodeService errorCodeService;

	@GetMapping("/{code}")
	@Timed
	public ResponseEntity<ApiStatus<ErrorCode>> getErrorCodeByCode(@PathVariable String code)
			throws JsonParseException, JsonMappingException, IOException {
		if (logger.isDebugEnabled())
			logger.debug("Fetching error code details for : {}", code);
		ErrorCode errorCode = errorCodeService.getErrorCodeByCode(code);
		ApiStatus<ErrorCode> apiStatus = new ApiStatus<ErrorCode>(HttpStatus.OK, "SUCCESS",
				"GOT ERROR DETAILS OBJECT SUCCESSFULLY", errorCode);
		return new ResponseEntity<ApiStatus<ErrorCode>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/all")
	@Timed
	public ResponseEntity<ApiStatus<List<ErrorCode>>> getAllErrorCodes()
			throws JsonParseException, JsonMappingException, IOException {
		if (logger.isDebugEnabled())
			logger.debug("Fetching error code details for : all");
		List<ErrorCode> errorCodes = errorCodeService.findAll();
		ApiStatus<List<ErrorCode>> apiStatus = new ApiStatus<List<ErrorCode>>(HttpStatus.OK, "SUCCESS",
				"GOT ERROR DETAILS OBJECTS SUCCESSFULLY", errorCodes);
		return new ResponseEntity<ApiStatus<List<ErrorCode>>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping()
	@Timed
	public ResponseEntity<ApiStatus<ErrorCode>> saveErrorCode(@Valid @RequestBody ErrorCode errorCode,
			@RequestHeader(value = "X-USER-EMAIL") String userName) {
		ErrorCode persistedErrorCode = errorCodeService.save(errorCode, userName);
		ApiStatus<ErrorCode> apiStatus = new ApiStatus<ErrorCode>(HttpStatus.OK, "SUCCESS",
				"SAVED ERROR DETAILS OBJECT SUCCESSFULLY", persistedErrorCode);
		return new ResponseEntity<ApiStatus<ErrorCode>>(apiStatus, HttpStatus.OK);
	}

	@PutMapping()
	@Timed
	public ResponseEntity<ApiStatus<ErrorCode>> updateErrorCode(@Valid @RequestBody ErrorCode errorCode,
			@RequestHeader(value = "X-USER-EMAIL") String userName) {
		ErrorCode persistedErrorCode = errorCodeService.save(errorCode, userName);
		ApiStatus<ErrorCode> apiStatus = new ApiStatus<ErrorCode>(HttpStatus.OK, "SUCCESS",
				"UPDATED ERROR DETAILS OBJECT SUCCESSFULLY", persistedErrorCode);
		return new ResponseEntity<ApiStatus<ErrorCode>>(apiStatus, HttpStatus.OK);
	}
}
