package com.ey.in.tds.ingestion.web.rest;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.ingestion.service.ao.AOUtilizationService;

import bsh.ParseException;

@RestController
@RequestMapping("/api/ingestion")
public class AoUtilizationResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AOUtilizationService aoUtilizationService;

	/**
	 * This method is used to get ao matrix values
	 * 
	 * @param deductorTan
	 * @return
	 * @throws CustomException
	 * @throws IOException
	 * @throws ParseException
	 * @throws ParseException
	 */
	@GetMapping("ao/matrix/{year}/{certificateNumber}")
	public ResponseEntity<ApiStatus<Map<Integer, Object>>> getAOMatrix(
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @PathVariable int year,
			@PathVariable String certificateNumber) throws CustomException, IOException, ParseException {
		if (deductorTan == null) {
			if (logger.isErrorEnabled()) {
				logger.error("DeductorTan Not Found");
			}
			throw new CustomException("DeductorTan Not present to proceed..", HttpStatus.BAD_REQUEST);
		}
		Map<Integer, Object> ao = aoUtilizationService.getAOMatrix(deductorTan, year, certificateNumber);

		if (logger.isInfoEnabled()) {
			logger.info("Ao all matrix values is done ");
		}
		ApiStatus<Map<Integer, Object>> apiStatus = new ApiStatus<Map<Integer, Object>>(HttpStatus.OK, "SUCCESS",
				"LIST OF AO MATRIX DATA", ao);
		return new ResponseEntity<ApiStatus<Map<Integer, Object>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method downloads file for AO adjusted amount
	 * 
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param ldcData
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/ao/matrix/adjusted/file/download")
	public ResponseEntity<ApiStatus<String>> aoMatrixAdjustedFileDownload(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> aoData)
			throws Exception {

		Integer year = Integer.valueOf(aoData.get("year"));
		Integer month = Integer.valueOf(aoData.get("month"));
		String cerificateNumber = aoData.get("cerificateNumber");

		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		aoUtilizationService.asyncAoMatrixAdjustedFileDownload(tan, year, month, cerificateNumber, tenantId, userName);
		String message = "Ao adjusted report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/ao/matrix/closingAmount/file/download")
	public ResponseEntity<ApiStatus<String>> aoMatrixClosingAmountDownload(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> aoData)
			throws Exception {

		Integer year = Integer.valueOf(aoData.get("year"));
		Integer month = Integer.valueOf(aoData.get("month"));
		String cerificateNumber = aoData.get("cerificateNumber");
		String type = aoData.get("type");

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		aoUtilizationService.asyncAoMatrixClosingAmountDownload(tan, year, month, cerificateNumber, type, tenantId,
				userName);
		String fileType = "";
		if (UploadTypes.AO_OPENING_REPORT.name().equalsIgnoreCase(type)) {
			fileType = "opening";
		} else {
			fileType = "closing";
		}
		String message = "Ao " + fileType + " report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * to download the AO utilization report using ldc id
	 * 
	 * @param deductorTan
	 * @param lccMasterId
	 * @param tenantId
	 * @param userName
	 * @param pan
	 * @return
	 * @throws Exception
	 */
	@PostMapping("ao-utilization/export/{id}")
	public ResponseEntity<ApiStatus<String>> tcsLccUtilizationFileExport(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@PathVariable("id") Integer lccMasterId, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "DEDUCTOR-PAN") String pan)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		aoUtilizationService.generateAOUtilizationFileAsync(lccMasterId, tenantId, deductorTan, userName, pan);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				" LDC file Requested successfully", "SUCCESS");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
}
