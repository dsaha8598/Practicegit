package com.ey.in.tds.onboarding.web.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeclarationTanFilesDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDeclarationEmailDetails;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetails;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetailsControlOutput;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetailsDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetailsFormDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.PerferencsDTO;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.onboarding.service.kyc.KYCDetailsAsyncService;
import com.ey.in.tds.onboarding.service.kyc.KYCDetailsService;
import com.ey.in.tds.onboarding.service.kyc.KYCFinalReportService;
import com.ey.in.tds.onboarding.service.kyc.KYCOTPService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

/**
 * @author vamsir
 */
@RestController
@RequestMapping("/api/onboarding")
public class KYCDetailsResource extends BaseResource {

	@Autowired
	private KYCDetailsService kycDetailsService;

	@Autowired
	private KYCDetailsAsyncService kycDetailsAsyncService;

	@Autowired
	private KYCFinalReportService kycFinalReportService;

	@Autowired
	private KYCOTPService kycotpService;

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
	@PostMapping(value = "/kyc/upload/excel")
	public ResponseEntity<ApiStatus<BatchUpload>> uploadExcel(@RequestParam("files") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam(value = "type", required = true) String type) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		BatchUpload batchUpload = null;
		logger.info("Testing Something in Upload Excel");
		logger.info("TAN-NUMBER: {}", tan);
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			batchUpload = kycDetailsAsyncService.saveFileData(file, tan, assesssmentYear, assessmentMonth, userName,
					tenantId, pan, type);
		}
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED KYC DETAILS FILE SUCCESSFULLY ", batchUpload);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param tenantId
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	@GetMapping(value = "/kyc", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<KYCDetails>>> getKycDetailsList(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam(value = "type", required = true) String type) {
		MultiTenantContext.setTenantId(tenantId);
		List<KYCDetails> result = kycDetailsService.getKycDetailsList(deductorPan, tan, type);
		ApiStatus<List<KYCDetails>> apiStatus = new ApiStatus<List<KYCDetails>>(HttpStatus.OK, "SUCCESS",
				"GET ALL KYC DETAILS RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<List<KYCDetails>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param kycDetails
	 * @param ITRFile2
	 * @param ITRFile1
	 * @param ITRFile3
	 * @param turnOverFile
	 * @param tenantId
	 * @param deductorPan
	 * @param deductorTan
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/kyc/noAuth/mail/save/{key}")
	public ResponseEntity<ApiStatus<KYCDetailsFormDTO>> downloadFile(@RequestParam(value = "data") String kycDetailsStr,
			@RequestParam(value = "itrFile2", required = false) MultipartFile itrFile2,
			@RequestParam(value = "itrFile1", required = false) MultipartFile itrFile1,
			@RequestParam(value = "itrFile3", required = false) MultipartFile itrFile3,
			@RequestParam(value = "tanFiles", required = false) List<MultipartFile> tanFiles,
			@RequestParam(value = "panFile", required = false) MultipartFile panFile,
			@RequestParam(value = "fsFile", required = false) MultipartFile fsFile, @PathVariable String key)
			throws Exception {
		KYCDetailsFormDTO kycDetails = new ObjectMapper().readValue(kycDetailsStr, KYCDetailsFormDTO.class);
		KYCDetailsFormDTO result = kycDetailsService.updateKycDetails(kycDetails, itrFile1, itrFile2, itrFile3, key,
				tanFiles, panFile, fsFile);
		ApiStatus<KYCDetailsFormDTO> apiStatus = new ApiStatus<KYCDetailsFormDTO>(HttpStatus.OK, "SUCCESS",
				"KYC DETAILS SAVED SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<KYCDetailsFormDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param pan
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/kyc/trigger/finalReport")
	public ResponseEntity<ApiStatus<KYCDetailsFormDTO>> triggerFinalReport(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "type", required = true) String type) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		kycFinalReportService.triggerFinalReport(pan, tenantId, tan, userName, assesssmentYear, type);
		ApiStatus<KYCDetailsFormDTO> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"KYC FINAL REPORT TRIGGERED SUCCESSFULLY ", null);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param tenantId
	 * @param userName
	 * @param deductorPan
	 * @param deductorTan
	 * @param batchId
	 * @param path
	 * @return
	 * @throws ParseException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws Exception
	 */

	@PostMapping(value = "/kyc/upload/finalReport")
	public ResponseEntity<ApiStatus<BatchUpload>> uploadKycFinalReportExcel(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @RequestParam(value = "batchId") Integer batchId,
			@RequestParam(value = "type", required = true) String type)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		BatchUpload response = kycFinalReportService.saveFinalReportResponseData(deductorTan, tenantId, deductorPan,
				batchId, userName, type);
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param pan
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/kyc/download/excel")
	public ResponseEntity<ApiStatus<String>> kycTdsVsTcsApplicabilityExcel(@RequestParam(value = "year") int year,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "type", required = true) String type) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		kycDetailsService.kycTdsVsTcsApplicabilityExcel(pan, tenantId, tan, userName, year, type);
		String message = "KYC TDS vs TCS Applicabilit report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param tenantId
	 * @param userName
	 * @param deductorPan
	 * @param batchId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/kyc/import")
	public ResponseEntity<ApiStatus<BatchUpload>> uploadKycExcel(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @RequestParam(value = "batchId") Integer batchId,
			@RequestParam(value = "type", required = true) String type) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		BatchUpload response = kycDetailsService.asyncUpdateKycRemediationReport(deductorTan, tenantId, deductorPan,
				batchId, userName, type);
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param tenantId
	 * @param deductorPan
	 * @param id
	 * @return
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	@GetMapping(value = "/kyc/noAuth/status/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<KYCDetailsDTO>> getKycSubmited(@PathVariable String key)
			throws JsonMappingException, JsonProcessingException {
		KYCDetailsDTO result = kycDetailsService.getKycSubmited(key);
		ApiStatus<KYCDetailsDTO> apiStatus = new ApiStatus<KYCDetailsDTO>(HttpStatus.OK, "SUCCESS",
				"GET KYC DETAILS RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<KYCDetailsDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param tan
	 * @param deductorPan
	 * @param reportsDTO
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/kyc/report/{type}/{year}")
	public ResponseEntity<ApiStatus<String>> getReportsByType(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @PathVariable(value = "year") int year,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@PathVariable String type) throws Exception {
		String message = kycDetailsService.getReportsByType(tan, year, deductorPan, tenantId, type, userName);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"Kyc Report generated successfully", message);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param tenantId
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	@GetMapping(value = "/kyc/controlTotal", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<KYCDetailsControlOutput>> getKycDetailsControlOutput(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam(value = "type", required = true) String type) {
		MultiTenantContext.setTenantId(tenantId);
		int year = CommonUtil.getAssessmentYear(null);
		KYCDetailsControlOutput result = kycDetailsService.getControlOutput(deductorPan, tan, year, type);
		ApiStatus<KYCDetailsControlOutput> apiStatus = new ApiStatus<KYCDetailsControlOutput>(HttpStatus.OK, "SUCCESS",
				"GET KYC CONTROL OUTPUT DETAILS RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<KYCDetailsControlOutput>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param key
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/kyc/noAuth/sendOTP/{key}")
	public ResponseEntity<ApiStatus<String>> sendOTP(@PathVariable String key) throws Exception {
		kycotpService.sendMailOTP(key);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "Kyc OTP Send Successfully",
				null);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param key
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/kyc/noAuth/verifyOTP/{key}/{otp}")
	public ResponseEntity<ApiStatus<String>> verifyOTP(@PathVariable String key, @PathVariable String otp)
			throws Exception {
		Map<String, String> otpVerificationMap = kycotpService.verifyMailOTP(key, otp);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK,
				Boolean.valueOf(otpVerificationMap.get("isOTPVerified")) ? "SUCCESS" : "FAILED",
				"Kyc OTP Verified Successfully", otpVerificationMap.get("data"));
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tenantId
	 * @param userName
	 * @param deductorPan
	 * @param deductorTan
	 * @param batchId
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/kyc/pan/mismatch/count")
	public ResponseEntity<ApiStatus<Integer>> getPanMismatchCount(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestParam(value = "type", required = true) String type) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		Integer response = kycDetailsService.getPanMismatchCount(deductorTan, deductorPan, type);
		ApiStatus<Integer> apiStatus = new ApiStatus<Integer>(HttpStatus.OK, "Pan Mismatch Count", "NO ALERT",
				response);
		return new ResponseEntity<ApiStatus<Integer>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tenantId
	 * @param deductorPan
	 * @param tan
	 * @param type
	 * @return
	 */
	@GetMapping(value = "/kyc/declaration/{kycId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<DeclarationTanFilesDTO>>> getDeclarationTanFileDetails(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable(value = "kycId") int kycId) {
		MultiTenantContext.setTenantId(tenantId);
		List<DeclarationTanFilesDTO> result = kycDetailsService.getDeclarationTanFileDetails(deductorPan, tan, kycId);
		ApiStatus<List<DeclarationTanFilesDTO>> apiStatus = new ApiStatus<List<DeclarationTanFilesDTO>>(HttpStatus.OK,
				"SUCCESS", "GET ALL DECLARATION TAN FILES", result);
		return new ResponseEntity<ApiStatus<List<DeclarationTanFilesDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param tenantId
	 * @param deductorPan
	 * @param tan
	 * @param kycMailDto
	 * @return
	 * @throws ParseException
	 */
	@PostMapping(value = "/kyc/controltotal/mailtriggered", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> controlTotalMailTriggred(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestParam(value = "type", required = true) String type,
			@RequestParam(value = "mailType", required = true) String mailType) throws ParseException {
		MultiTenantContext.setTenantId(tenantId);
		int year = CommonUtil.getAssessmentYear(null);
		kycDetailsAsyncService.asyncControlTotalMailTriggred(deductorPan, tan, tenantId, type, year, mailType);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "EMAILS SENT SUCCESSFULLY",
				"EMAILS SENT SUCCESSFULLY");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tenantId
	 * @param deductorPan
	 * @param tan
	 * @param type
	 * @param year
	 * @param mailType
	 * @return
	 * @throws ParseException
	 */
	@PostMapping(value = "/kyc/batch/mailtriggered", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> batchMailTriggred(@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam(value = "type", required = true) String type,
			@RequestParam(value = "batchId", required = true) int batchId) {
		MultiTenantContext.setTenantId(tenantId);
		kycDetailsAsyncService.asyncBatchMailTriggred(deductorPan, tan, tenantId, type, batchId);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "EMAILS SENT SUCCESSFULLY",
				"EMAILS SENT SUCCESSFULLY");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param tenantId
	 * @param deductorPan
	 * @param deductorTan
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/kyc/generate/tan/level/file")
	public ResponseEntity<ApiStatus<String>> generatetanLevelFile(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestHeader(value = "USER_NAME") String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		kycDetailsAsyncService.asyncGeneratetanLevelFile(tenantId, deductorTan, deductorPan, userName);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "Pan Mismatch Count", "NO ALERT",
				"File requent sent sucessfully");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param kycPerferences
	 * @param tenantId
	 * @param deductorPan
	 * @param deductorTan
	 * @param userName
	 * @return
	 */
	@PostMapping(value = "/kyc/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<KYCDeclarationEmailDetails>> createPreferences(
			@RequestBody PerferencsDTO kycPerferences, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestHeader(value = "USER_NAME") String userName) {
		MultiTenantContext.setTenantId(tenantId);
		KYCDeclarationEmailDetails result = kycDetailsService.createPreferences(deductorPan, deductorTan,
				kycPerferences, userName);
		ApiStatus<KYCDeclarationEmailDetails> apiStatus = new ApiStatus<KYCDeclarationEmailDetails>(HttpStatus.CREATED,
				"SUCCESS", "CREATED PERFERENCES RECORD SUCCESSFULLY", result);
		return new ResponseEntity<ApiStatus<KYCDeclarationEmailDetails>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tenantId
	 * @param deductorPan
	 * @param tan
	 * @param type
	 * @return
	 */
	@GetMapping(value = "/kyc/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<KYCDeclarationEmailDetails>>> getPreferences(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan) {
		MultiTenantContext.setTenantId(tenantId);
		List<KYCDeclarationEmailDetails> result = kycDetailsService.getAllPreferences(deductorPan, deductorTan);
		ApiStatus<List<KYCDeclarationEmailDetails>> apiStatus = new ApiStatus<List<KYCDeclarationEmailDetails>>(
				HttpStatus.CREATED, "SUCCESS", "CREATED PERFERENCES RECORD SUCCESSFULLY", result);
		return new ResponseEntity<ApiStatus<List<KYCDeclarationEmailDetails>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for approved.
	 * 
	 * @param tenantId
	 * @param deductorPan
	 * @param deductorTan
	 * @param userName
	 * @return
	 */
	@PostMapping(value = "/kyc/approved", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> createApproved(@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestParam(value = "type", required = true) String type) {
		MultiTenantContext.setTenantId(tenantId);
		String result = kycDetailsService.createApproved(deductorPan, deductorTan, userName, type);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.CREATED, "SUCCESS",
				"CREATED APPROVED SUCCESSFULLY", result);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tenantId
	 * @param deductorPan
	 * @param deductorTan
	 * @return
	 */
	@GetMapping(value = "/kyc/checksurveyreport", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Boolean>> getApproved(@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestParam(value = "type", required = true) String type) {
		MultiTenantContext.setTenantId(tenantId);
		Boolean result = kycDetailsService.getApproved(deductorPan, deductorTan, type);
		ApiStatus<Boolean> apiStatus = new ApiStatus<Boolean>(HttpStatus.OK, "SUCCESS",
				"GET APPROVED DETAILS SUCCESSFULLY", result);
		return new ResponseEntity<ApiStatus<Boolean>>(apiStatus, HttpStatus.OK);
	}

}
