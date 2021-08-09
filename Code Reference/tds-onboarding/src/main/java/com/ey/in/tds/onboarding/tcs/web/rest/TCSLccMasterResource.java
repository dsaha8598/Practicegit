package com.ey.in.tds.onboarding.tcs.web.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.tika.Tika;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSEmailNotification;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccUtilization;
import com.ey.in.tds.common.security.SecurityValidations;
import com.ey.in.tds.common.service.EmailNotificationService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.onboarding.service.lcc.TCSLccMasterService;
import com.microsoft.azure.storage.StorageException;

@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin("*")
public class TCSLccMasterResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TCSLccMasterService tcsLccMasterService;
	
	@Autowired
	EmailNotificationService emailNotificationService;

	/**
	 * 
	 * @param lccMasterDTO
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @return
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@PostMapping("/tcs/lcc-master")
	public ResponseEntity<ApiStatus<TCSLccMaster>> createLCC(@RequestBody TCSLccMaster lccMasterDTO,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "X-TENANT-ID") String tenantId)
			throws IntrusionException, ValidationException, ParseException, IllegalAccessException,
			InvocationTargetException {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		if (lccMasterDTO.getApplicableFrom() == null) {
			throw new CustomException("Applicable From should not null", HttpStatus.BAD_REQUEST);
		}

		if (lccMasterDTO.getApplicableTo() != null
				&& (lccMasterDTO.getApplicableFrom().equals(lccMasterDTO.getApplicableTo())
						|| lccMasterDTO.getApplicableFrom().after(lccMasterDTO.getApplicableTo()))) {
			throw new CustomException("From Date should not be equals or greater than To Date", HttpStatus.BAD_REQUEST);
		}
		if (lccMasterDTO.getAmount() != null && lccMasterDTO.getUtilizedAmount() != null) {
			BigDecimal lccAmount = lccMasterDTO.getAmount();
			logger.info("amount is {}: ", lccAmount);
			int checkAmount = lccAmount.compareTo(BigDecimal.valueOf(0));
			if (checkAmount == -1) {
				throw new CustomException("Amount " + lccMasterDTO.getAmount() + " should not contain -ve value",
						HttpStatus.BAD_REQUEST);
			}
			BigDecimal utilizedAmount = lccMasterDTO.getUtilizedAmount();
			logger.info("amount is {}: ", utilizedAmount);
			int checkUtilizedAmount = utilizedAmount.compareTo(BigDecimal.valueOf(0));
			if (checkUtilizedAmount == -1) {
				throw new CustomException(
						"Limit utilized amount " + lccMasterDTO.getUtilizedAmount() + "should not contain -ve value",
						HttpStatus.BAD_REQUEST);
			}
			int checkAmountAndUtilized = lccMasterDTO.getUtilizedAmount().compareTo(lccMasterDTO.getAmount());

			if (checkAmountAndUtilized == 1) {
				throw new CustomException("Limit utilised amount should not be greater than certificate amount",
						HttpStatus.BAD_REQUEST);
			}
		}
		// ESAPI Validating user input
		SecurityValidations.lccMasterInputValidation(lccMasterDTO);
		TCSLccMaster response = tcsLccMasterService.create(lccMasterDTO, deductorTan, assesssmentYear, userName);
		ApiStatus<TCSLccMaster> apiStatus = new ApiStatus<TCSLccMaster>(HttpStatus.CREATED, "SUCCESS",
				"CREATED LCC MASTER RECORD SUCCESSFULLY ", response);
		return new ResponseEntity<ApiStatus<TCSLccMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param ldcMasterDTO
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @return
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	@PutMapping("/tcs/lcc-master")
	public ResponseEntity<ApiStatus<TCSLccMaster>> updateLCC(@RequestBody TCSLccMaster ldcMasterDTO,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "X-TENANT-ID") String tenantId)
			throws IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		// ESAPI Validating user input
		SecurityValidations.lccMasterInputValidation(ldcMasterDTO);
		TCSLccMaster response = tcsLccMasterService.update(ldcMasterDTO, deductorTan, assesssmentYear, userName);
		ApiStatus<TCSLccMaster> apiStatus = new ApiStatus<TCSLccMaster>(HttpStatus.OK, "SUCCESS",
				"UPDATED LCC MASTER RECORD SUCCESSFULLY ", response);
		return new ResponseEntity<ApiStatus<TCSLccMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param file
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 * @throws EncryptedDocumentException
	 * @throws InvalidFormatException
	 */
	@PostMapping("/tcs/lcc/validation/upload/excel")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> upload(@RequestHeader("TAN-NUMBER") String tan,
			@RequestHeader("USER_NAME") String userName, @RequestParam("file") MultipartFile file,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) throws InvalidKeyException, URISyntaxException,
			StorageException, IOException, EncryptedDocumentException, InvalidFormatException {
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
			assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
			TCSBatchUpload batchUpload = tcsLccMasterService.saveToBatchUploadExcel(file, tan, assesssmentYear,
					userName);
			ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
					"UPLOADED LDC EXCEL FILE SUCCESSFULLY ", batchUpload);
			return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}

	}

	/**
	 * 
	 * @param deductorTan
	 * @param requestParams
	 * @return
	 */
	@PostMapping("/tcs/lcc-master/getldc")
	public ResponseEntity<ApiStatus<TCSLccMaster>> getLCC(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestBody Map<String, String> requestParams) {
		MultiTenantContext.setTenantId(tenantId);
		String year = requestParams.get("assesssmentYear");
		Integer id = Integer.parseInt(requestParams.get("id"));
		Integer assesssmentYear = null;
		if (year != null) {
			assesssmentYear = Integer.parseInt(year);
		}
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		TCSLccMaster ldcMasterDTO = tcsLccMasterService.getLccMasterDTO(deductorTan, id, assesssmentYear);
		ApiStatus<TCSLccMaster> apiStatus = new ApiStatus<TCSLccMaster>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				ldcMasterDTO);
		return new ResponseEntity<ApiStatus<TCSLccMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param id
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @return
	 */
	@GetMapping("/tcs/lcc-master/{id}")
	public ResponseEntity<ApiStatus<TCSLccMaster>> getLCC(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan, @PathVariable Integer id,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		TCSLccMaster ldcMasterDTO = tcsLccMasterService.getLccMasterDTO(deductorTan, id, assesssmentYear);
		ApiStatus<TCSLccMaster> apiStatus = new ApiStatus<TCSLccMaster>(HttpStatus.OK, "SUCCESS",
				"Retrieved single LCC record successfully", ldcMasterDTO);
		return new ResponseEntity<ApiStatus<TCSLccMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get All Lcc master data.
	 * 
	 * @param assesssmentYear
	 * @param deductorTan
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@GetMapping(value = "/tcs/lcc-master", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<TCSLccMaster>>> getListOfLcc(
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan)
			throws IllegalAccessException, InvocationTargetException {
		MultiTenantContext.setTenantId(tenantId);
		List<TCSLccMaster> listMasterDTOs = tcsLccMasterService.getListOfLcc(deductorTan, tenantId);
		ApiStatus<List<TCSLccMaster>> apiStatus = new ApiStatus<List<TCSLccMaster>>(HttpStatus.OK, "SUCCESS",
				"Retrieved list of LCC records successfully", listMasterDTOs);
		return new ResponseEntity<ApiStatus<List<TCSLccMaster>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get one LCC Master data
	 * 
	 * @param assesssmentYear
	 * @param deductorTan
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/tcs/lccmaster", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSLccMaster>> getOneLccMaster(
			@RequestParam(value = "assessmentYear", required = false) Integer assessmentYear,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		int year = CommonUtil.getAssessmentYear(assessmentYear);
		TCSLccMaster listMaster = tcsLccMasterService.getOneLdcMaste(year, deductorTan);
		ApiStatus<TCSLccMaster> apiStatus = new ApiStatus<TCSLccMaster>(HttpStatus.OK, "SUCCESS",
				"Retrieved LDC records successfully", listMaster);
		return new ResponseEntity<ApiStatus<TCSLccMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param assesssmentYear
	 * @param deductorTan
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/tcs/lcc/validation/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> getLccValidationStatus(
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestParam(value = "assessmentYear", required = false) Integer assessmentYear,
			@RequestParam(value = "assessmentMonth", required = true) Integer assessmentMonth) {
		MultiTenantContext.setTenantId(tenantId);
		String startDate = CommonUtil.getQueryDate(CommonUtil.getMonthStartDate(assessmentYear, assessmentMonth - 1));
		String endDate = CommonUtil.getQueryDate(CommonUtil.getMonthEndDate(assessmentYear, assessmentMonth - 1));
		String result = tcsLccMasterService.getLdcMasterStatus(deductorTan, startDate, endDate);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "Fetched LCC validation successfully",
				result);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * EXPORT EXCEL FILE
	 * 
	 * @return
	 * @throws IOException
	 */
	@GetMapping(value = "/tcs/validation/lcc/exporttoexcel")
	public ResponseEntity<InputStreamResource> exportToExcels() throws IOException {
		ByteArrayInputStream in = tcsLccMasterService.exportToExcel();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=ldc.xlsx");
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
	}

	/**
	 * To accept the uploaded LdcMaster Excel file and processed
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

	@PostMapping(value = "/tcs/lcc/master/upload/excel")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> uploadExcel(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		TCSBatchUpload tcsBatchUpload = null;
		logger.info("Testing Something in Upload Excel");
		logger.info("TAN-NUMBER: {}", tan);
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			String contentType = new Tika().detect(file.getInputStream());
			if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
					|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
				tcsBatchUpload = tcsLccMasterService.saveFileData(file, tan, assesssmentYear, assessmentMonth, userName,
						tenantId, pan);
				ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
						"NO ALERT", tcsBatchUpload);
				return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
			} else {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
			}
		}
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
	@PostMapping(value = "/tcs/lcc/master/upload/pdf")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> uploadPdf(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		TCSBatchUpload tcsBatchUpload = null;
		logger.info("Testing Something in Upload Excel");
		logger.info("TAN-NUMBER: {}", tan);
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("pdf")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			String contentType = new Tika().detect(file.getInputStream());
			if (AllowedMimeTypes.PDF.getMimeType().equals(contentType)) {
				tcsBatchUpload = tcsLccMasterService.saveLdcPdfData(file, tan, assesssmentYear, assessmentMonth,
						userName, tenantId, pan);
				ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
						"UPLOADED LCC PDF FILE SUCCESSFULLY ", tcsBatchUpload);
				return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
			} else {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only pdf files are allowed", HttpStatus.BAD_REQUEST);
			}
		}

	}

	/**
	 * this is to export ldc masster data based on deductor tan
	 * 
	 * @param tan
	 * @param pan
	 * @param tenantId
	 * @param year
	 * @param month
	 * @return
	 * @throws IOException
	 * @throws TanNotFoundException
	 */
	@PostMapping(value = "/tcs/lccMasterData/export", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<InputStreamResource> exportLccReport(@RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) throws IOException, TanNotFoundException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("TAN: {}", deductorTan);
		MultiTenantContext.setTenantId(tenantId);
		if (deductorTan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}

		ByteArrayInputStream in = tcsLccMasterService.exportLccMasterData(deductorTan, tenantId);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=TCS_LCC_MASTER_DATA_" + deductorTan + ".xlsx");

		logger.info("LCC Master data export done ");
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
	}

	/**
	 * This API for deactive ldc certificate.
	 * 
	 * @param deductorTan
	 * @param id
	 * @param ldcPan
	 * @return
	 */
	@PostMapping("/tcs/lcc-master/deactive")
	public ResponseEntity<ApiStatus<TCSLccMaster>> deactivateLccCertificate(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestBody Map<String, String> requestParams, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		Integer id = Integer.parseInt(requestParams.get("id"));
		String ldcPan = requestParams.get("ldcPan");
		TCSLccMaster ldcMaster = tcsLccMasterService.deactivateLccCertificate(ldcPan, deductorTan, id);
		ApiStatus<TCSLccMaster> apiStatus = new ApiStatus<TCSLccMaster>(HttpStatus.OK, "SUCCESS",
				"DEACTIVATE LCC RECORD SUCCESSFULLY", ldcMaster);
		return new ResponseEntity<ApiStatus<TCSLccMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client
	 * 
	 * @param deductorTan
	 * @param lccMasterId
	 * @return
	 */
	@GetMapping("/tcs/lcc-utilization")
	public ResponseEntity<ApiStatus<List<TCSLccUtilization>>> tcsLccUtilization(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader Integer lccMasterId, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		List<TCSLccUtilization> lccUtilizationList = tcsLccMasterService.tcsLccUtilization(deductorTan, lccMasterId);
		ApiStatus<List<TCSLccUtilization>> apiStatus = new ApiStatus<List<TCSLccUtilization>>(HttpStatus.OK, "SUCCESS",
				"GET LCC UTILIZATION RECORD SUCCESSFULLY", lccUtilizationList);
		return new ResponseEntity<ApiStatus<List<TCSLccUtilization>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param lccMasterId
	 * @param tenantId
	 * @param userName
	 * @param pan
	 * @return
	 * @throws Exception
	 */
	@PostMapping("tcs/lcc-utilization/export/{id}")
	public ResponseEntity<ApiStatus<String>> tcsLccUtilizationFileExport(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@PathVariable("id") Integer lccMasterId, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "DEDUCTOR-PAN") String pan)
			throws Exception {
		tcsLccMasterService.generateLccUtilizationFileAsync(lccMasterId, tenantId, deductorTan, userName, pan);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", " SUCCESS",
				"LCC file Requested successfully");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * This API for feign client.
	 * 
	 * @param deductorTan
	 * @param tenantId
	 * @param userName
	 * @param pan
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/tcs/emailnotification")
	public ResponseEntity<ApiStatus<TCSEmailNotification>> tcsEmailNotification(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestBody TCSEmailNotification tcsEmailNotification) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		TCSEmailNotification emailNotification = emailNotificationService
				.createTcsEmailNotification(tcsEmailNotification, tenantId);
		ApiStatus<TCSEmailNotification> apiStatus = new ApiStatus<TCSEmailNotification>(HttpStatus.CREATED, "SUCCESS",
				"TCS Email created successfully", emailNotification);
		return new ResponseEntity<ApiStatus<TCSEmailNotification>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client
	 * 
	 * @param deductorTan
	 * @param tenantId
	 * @param userName
	 * @param pan
	 * @param tcsEmailNotification
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/tcs/emailnotification")
	public ResponseEntity<ApiStatus<TCSEmailNotification>> getTcsEmailNotification(
			@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorMasterPan,
			@RequestParam(value = "lccMasterId") Integer lccMasterId, @RequestParam(value = "rate") int rate,
			@RequestParam(value = "type") String type) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		TCSEmailNotification emailNotification = emailNotificationService
				.getNotificationAtLccThresholdRate(deductorMasterPan, lccMasterId, type, rate, tenantId);
		ApiStatus<TCSEmailNotification> apiStatus = new ApiStatus<TCSEmailNotification>(HttpStatus.OK, "SUCCESS",
				"get tcs email notification successfully", emailNotification);
		return new ResponseEntity<ApiStatus<TCSEmailNotification>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client for update tcs email notification
	 * 
	 * @param deductorTan
	 * @param tenantId
	 * @param pan
	 * @param tcsEmailNotification
	 * @return
	 * @throws Exception
	 */
	@PutMapping("/tcs/emailnotification")
	public ResponseEntity<ApiStatus<TCSEmailNotification>> updateTcsEmailNotification(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestBody TCSEmailNotification tcsEmailNotification) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		TCSEmailNotification emailNotification = emailNotificationService.upadte(tcsEmailNotification, tenantId);
		ApiStatus<TCSEmailNotification> apiStatus = new ApiStatus<TCSEmailNotification>(HttpStatus.CREATED, "SUCCESS",
				"TCS Email created successfully", emailNotification);
		return new ResponseEntity<ApiStatus<TCSEmailNotification>>(apiStatus, HttpStatus.OK);
	}

}
