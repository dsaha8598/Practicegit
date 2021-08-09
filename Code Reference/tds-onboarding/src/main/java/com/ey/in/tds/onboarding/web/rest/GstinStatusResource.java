package com.ey.in.tds.onboarding.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.onboarding.service.deductee.GstinStatusService;

/**
 * 
 * @author vamsir
 *
 */
@RestController
@RequestMapping("/api/onboarding")
public class GstinStatusResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GstinStatusService gstinStatusService;

	/**
	 * 
	 * @param tan
	 * @param deductorPan
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/gstin/deductee/status/validation")
	public ResponseEntity<ApiStatus<String>> getGstinStatusValidation(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestHeader(value = "USER_NAME") String userName, @RequestParam(value = "year") String year,
			@RequestParam(value = "month") int month) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Enterted into /gstin/deductee/status/validation API");
		gstinStatusService.getAsyncgetGstinStatusValidation(deductorTan, deductorPan, userName, year, month, tenantId);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"GSTIN STATUS GENERATED SUCCESSFULLY", "GSTIN STATUS GENERATED SUCCESSFULLY");
		logger.info("Exited into /gstin/deductee/status/validation API");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tan
	 * @param deductorPan
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/gstin/collectee/status/validation")
	public ResponseEntity<ApiStatus<String>> getGstinCollecteeStatusValidation(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestHeader(value = "USER_NAME") String userName, @RequestParam(value = "year") String year,
			@RequestParam(value = "month") int month) throws Exception {
		logger.info("Enterted into /gstin/collectee/status/validation API");
		MultiTenantContext.setTenantId(tenantId);
		gstinStatusService.getAsyncGstinCollecteeStatusValidation(deductorTan, deductorPan, userName, year, month,
				tenantId);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"GSTIN STATUS GENERATED SUCCESSFULLY", "GSTIN STATUS GENERATED SUCCESSFULLY");
		logger.info("Exited into /gstin/collectee/status/validation API");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tan
	 * @param deductorPan
	 * @param year
	 * @param tenantId
	 * @param userName
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/gstin/shareholder/report")
	public ResponseEntity<ApiStatus<String>> getGstinShareholderReportExcel(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "year") int year, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName) throws Exception {
		gstinStatusService.getAsyncGstinShareholderReportExcel(tan, year, deductorPan, tenantId, userName);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"gstin repoort generated successfully", "gstin repoort generated successfully");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
}
