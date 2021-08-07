package com.ey.in.tds.onboarding.web.rest;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tcs.common.domain.CollecteeDeclaration;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.onboarding.deductee.DeducteeDeclaration;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderDeclaration;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.onboarding.service.kyc.DeclarationIntegrationService;
import com.ey.in.tds.onboarding.service.kyc.KYCDetailsAsyncService;
import com.ey.in.tds.onboarding.service.kyc.KycConsolidatedReportService;

/**
 * 
 * @author vamsir
 *
 */
@RestController
@RequestMapping("/api/onboarding")
public class DeclarationIntegrationResource extends BaseResource {

	@Autowired
	private DeclarationIntegrationService declarationIntegrationService;

	@Autowired
	private KYCDetailsAsyncService kycDetailsAsyncService;

	@Autowired
	private KycConsolidatedReportService kycConsolidatedReportService;

	/**
	 * 
	 * @param file
	 * @param tan
	 * @param pan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/tcs/declaration/excel")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> collecteeDeclaration(@RequestParam("files") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "month", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		TCSBatchUpload batchUpload = null;
		String type = UploadTypes.COLLECTEE_DECLARATION.name();
		logger.info("Testing Something in Upload Excel");
		logger.info("TAN-NUMBER: {}", tan);
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			batchUpload = kycDetailsAsyncService.tcsDeclaration(file, tan, assesssmentYear, assessmentMonth, userName,
					tenantId, pan, type);
		}
		ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED COLLECTEE DECLARATION FILE SUCCESSFULLY ", batchUpload);
		return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param file
	 * @param tan
	 * @param pan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/tds/declaration/excel")
	public ResponseEntity<ApiStatus<BatchUpload>> deducteeDeclaration(@RequestParam("files") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "month", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		BatchUpload batchUpload = null;
		String type = UploadTypes.DEDUCTEE_DECLARATION.name();
		logger.info("Testing Something in Upload Excel");
		logger.info("TAN-NUMBER: {}", tan);
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			batchUpload = kycDetailsAsyncService.declaration(file, tan, assesssmentYear, assessmentMonth, userName,
					tenantId, pan, type);
		}
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED DEDUCTEE DECLARATION FILE SUCCESSFULLY ", batchUpload);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param file
	 * @param tan
	 * @param pan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/shareholder/declaration/excel")
	public ResponseEntity<ApiStatus<BatchUpload>> shareholderDeclaration(@RequestParam("files") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "month", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		BatchUpload batchUpload = null;
		String type = UploadTypes.SHAREHOLDER_DECLARATION.name();
		logger.info("Testing Something in Upload Excel");
		logger.info("TAN-NUMBER: {}", tan);
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			batchUpload = kycDetailsAsyncService.shareholderdeclaration(file, tan, assesssmentYear, assessmentMonth, userName,
					tenantId, pan, type);
		}
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED DEDUCTEE DECLARATION FILE SUCCESSFULLY ", batchUpload);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tenantId
	 * @param deductorPan
	 * @param tan
	 * @param type
	 * @return
	 */
	@GetMapping(value = "/tcs/declaration", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<CollecteeDeclaration>>> getAllTcsDeclaration(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @RequestParam(value = "year") int year) {
		MultiTenantContext.setTenantId(tenantId);
		List<CollecteeDeclaration> result = declarationIntegrationService.getAllTcsDeclaration(deductorPan, deductorTan,
				year);
		ApiStatus<List<CollecteeDeclaration>> apiStatus = new ApiStatus<List<CollecteeDeclaration>>(HttpStatus.OK,
				"SUCCESS", "GET ALL TCS DECLARATION RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<List<CollecteeDeclaration>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tenantId
	 * @param deductorPan
	 * @param tan
	 * @param type
	 * @return
	 */
	@GetMapping(value = "/tds/declaration", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<DeducteeDeclaration>>> getAllTdsDeclaration(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @RequestParam(value = "year") int year) {
		MultiTenantContext.setTenantId(tenantId);
		List<DeducteeDeclaration> result = declarationIntegrationService.getAllTdsDeclaration(deductorPan, deductorTan,
				year);
		ApiStatus<List<DeducteeDeclaration>> apiStatus = new ApiStatus<List<DeducteeDeclaration>>(HttpStatus.OK,
				"SUCCESS", "GET ALL TDS DECLARATION DETAILS SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<List<DeducteeDeclaration>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tenantId
	 * @param deductorPan
	 * @param tan
	 * @param type
	 * @return
	 */
	@GetMapping(value = "/shareholder/declaration", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<ShareholderDeclaration>>> getAllShareholderDeclaration(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @RequestParam(value = "year") int year) {
		MultiTenantContext.setTenantId(tenantId);
		List<ShareholderDeclaration> result = declarationIntegrationService.getAllShareholderDeclaration(deductorPan,
				deductorTan, year);
		ApiStatus<List<ShareholderDeclaration>> apiStatus = new ApiStatus<List<ShareholderDeclaration>>(HttpStatus.OK,
				"SUCCESS", "GET ALL SHAREHOLDER DECLARATION DETAILS SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<List<ShareholderDeclaration>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param file
	 * @param tan
	 * @param pan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/tcs/declaration/threshold/update")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> colleteeThresholdUpdate(@RequestParam("files") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "month", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		TCSBatchUpload batchUpload = null;
		String type = UploadTypes.COLLECTEE_THRESHOLD_UPDATE.name();
		logger.info("TAN-NUMBER: {}", tan);
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			batchUpload = kycDetailsAsyncService.colleteeThresholdUpdate(file, tan, assesssmentYear, assessmentMonth,
					userName, tenantId, pan, type);
		}
		ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED THRESHOLD UPDATE FILE SUCCESSFULLY ", batchUpload);
		return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param file
	 * @param tan
	 * @param pan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/declaration/threshold/update")
	public ResponseEntity<ApiStatus<BatchUpload>> deducteeThresholdUpdate(@RequestParam("files") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "month", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		BatchUpload batchUpload = null;
		String type = UploadTypes.DEDUCTEE_THRESHOLD_UPDATE.name();
		logger.info("TAN-NUMBER: {}", tan);
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			batchUpload = kycDetailsAsyncService.deducteeThresholdUpdate(file, tan, assesssmentYear, assessmentMonth,
					userName, tenantId, pan, type);
		}
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED THRESHOLD UPDATE FILE SUCCESSFULLY ", batchUpload);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tenantId
	 * @param deductorPan
	 * @param tan
	 * @param type
	 * @param batchId
	 * @return
	 * @throws IOException
	 */
	@PostMapping(value = "/kyc/declaration/batch", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> declaration(@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestParam(value = "type", required = true) String type,
			@RequestParam(value = "blobUrl", required = true) String blobUrl,
			@RequestHeader(value = "USER_NAME") String userName) throws IOException {
		MultiTenantContext.setTenantId(tenantId);
		declarationIntegrationService.declaration(deductorPan, deductorTan, tenantId, type, blobUrl, userName);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.CREATED, "SUCCESS",
				"CREATED DECLARATION SUCCESSFULLY", "CREATED DECLARATION SUCCESSFULLY");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.CREATED);
	}

	/**
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param pan
	 * @param assesssmentYear
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/tcs/declaration/consolidated/report")
	public ResponseEntity<ApiStatus<String>> triggerConsolidatedReport(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		kycConsolidatedReportService.triggerConsolidatedReport(pan, tenantId, tan, userName, assesssmentYear);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"DECLARATION CONSOLIDATED REPORT SUCCESSFULLY ", "DECLARATION CONSOLIDATED REPORT SUCCESSFULLY ");
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param pan
	 * @param assesssmentYear
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/tds/declaration/consolidated/report")
	public ResponseEntity<ApiStatus<String>> tdsTriggerConsolidatedReport(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		kycConsolidatedReportService.tdsAsyncTriggerConsolidatedReport(pan, tenantId, tan, userName, assesssmentYear);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"DECLARATION CONSOLIDATED REPORT SUCCESSFULLY ", "DECLARATION CONSOLIDATED REPORT SUCCESSFULLY ");
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param pan
	 * @param assesssmentYear
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/shareholder/declaration/consolidated/report")
	public ResponseEntity<ApiStatus<String>> shareholderTriggerConsolidatedReport(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		kycConsolidatedReportService.shareholderAsyncTriggerConsolidatedReport(pan, tenantId, tan, userName,
				assesssmentYear);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"DECLARATION CONSOLIDATED REPORT SUCCESSFULLY ", "DECLARATION CONSOLIDATED REPORT SUCCESSFULLY ");
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

}
