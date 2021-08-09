package com.ey.in.tds.onboarding.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.onboarding.service.deducteecsv.DeducteeCsvService;

@RestController
@RequestMapping("/api/onboarding")
public class DeducteeCsvController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DeducteeCsvService deducteeCsvService;

	/**
	 * 
	 * @param file
	 * @param tan
	 * @param pan
	 * @param userName
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param tenantId
	 * @param batchId
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/deductee/import/csv")
	public ResponseEntity<ApiStatus<BatchUpload>> readImportedCsvData(@PathVariable("file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestParam(value = "batchId") Integer batchId)
			throws Exception {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		logger.info("Tenant ID-----: {}", tenantId);
		BatchUpload response = deducteeCsvService.readCsvFile(file, tan, tenantId, assesssmentYear, assessmentMonth,
				userName, pan, batchId);
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED DEDUCTEE CSV FILE SUCCESSFULLY ", response);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}

}
