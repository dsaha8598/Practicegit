package com.ey.in.tds.ingestion.web.rest.invoice.tcs;

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

import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.ingestion.service.ldc.TCSLccUtilizationService;

import bsh.ParseException;

@RestController
@RequestMapping("/api/ingestion/tcs")
public class TCSLccResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TCSLccUtilizationService tcsLccUtilizationService;

	/**
	 * This method is used to get lcc master matrix values
	 * 
	 * @param deductorTan
	 * @return
	 * @throws CustomException
	 * @throws IOException
	 * @throws ParseException
	 * @throws ParseException
	 */
	@GetMapping("lcc/matrix/{year}/{certificateNumber}")
	public ResponseEntity<ApiStatus<Map<Integer, Object>>> getLCCMatrix(
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @PathVariable int year,
			@PathVariable String certificateNumber, @RequestHeader(value = "X-TENANT-ID") String tenantId)
			throws CustomException, IOException, ParseException {
		if (deductorTan == null) {
			if (logger.isErrorEnabled()) {
				logger.error("DeductorTan Not Found");
			}
			throw new CustomException("DeductorTan Not present to proceed..", HttpStatus.BAD_REQUEST);
		}
		Map<Integer, Object> ldc = tcsLccUtilizationService.getLCCMatrix(deductorTan, year, certificateNumber);

		if (logger.isInfoEnabled()) {
			logger.info("LCC all matrix values is done ");
		}
		ApiStatus<Map<Integer, Object>> apiStatus = new ApiStatus<Map<Integer, Object>>(HttpStatus.OK, "SUCCESS",
				"LIST OF LCC MATRIX DATA", ldc);
		return new ResponseEntity<ApiStatus<Map<Integer, Object>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method downloads file for LCC adjusted amount
	 * 
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param ldcData
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/lcc/matrix/adjusted/file/download")
	public ResponseEntity<ApiStatus<String>> lccMatrixAdjustedFileDownload(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> ldcData)
			throws Exception {

		logger.info("TAN: {}", tan);
		Integer year = Integer.valueOf(ldcData.get("year"));
		Integer month = Integer.valueOf(ldcData.get("month"));
		String cerificateNumber = ldcData.get("cerificateNumber");

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		tcsLccUtilizationService.asyncLccMatrixAdjustedFileDownload(tan, year, month, cerificateNumber, tenantId,
				userName);
		String message = "Lcc adjusted report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param ldcData
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/lcc/matrix/closingAmount/file/download")
	public ResponseEntity<ApiStatus<String>> lccMatrixClosingAmountDownload(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> ldcData)
			throws Exception {

		Integer year = Integer.valueOf(ldcData.get("year"));
		Integer month = Integer.valueOf(ldcData.get("month"));
		String cerificateNumber = ldcData.get("cerificateNumber");
		String type = ldcData.get("type");
		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		tcsLccUtilizationService.asyncLccMatrixClosingAmountDownload(tan, year, month, cerificateNumber, type, tenantId,
				userName);
		String fileType = "";
		if (UploadTypes.LCC_OPENING_REPORT.name().equalsIgnoreCase(type)) {
			fileType = "opening";
		} else {
			fileType = "closing";
		}
		String message = "Lcc " + fileType + " report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

}
