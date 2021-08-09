package com.ey.in.tds.onboarding.web.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

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
import com.ey.in.tds.common.domain.transactions.jdbc.dao.LdcMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.LdcMasterDTO;
import com.ey.in.tds.common.dto.NewLDCMasterTracesDTO;
import com.ey.in.tds.common.dto.NewLdcTracesDTO;
import com.ey.in.tds.common.ingestion.response.dto.BatchUploadResponseDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.EmailNotification;
import com.ey.in.tds.common.onboarding.jdbc.dto.EmailNotificationConfiguration;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcUtilization;
import com.ey.in.tds.common.onboarding.response.dto.LdcResponseDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.security.SecurityValidations;
import com.ey.in.tds.common.service.EmailNotificationService;
import com.ey.in.tds.core.dto.EmailNotificationConfigurationDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.onboarding.service.ldc.LdcMasterService;
import com.microsoft.azure.storage.StorageException;

@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin("*")
public class LdcMasterResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private final LdcMasterService ldcMasterService;

	@Autowired
	private LdcMasterDAO ldcMasterDAO;

	@Autowired
	EmailNotificationService emailNotificationService;

	@Autowired
	public LdcMasterResource(LdcMasterService ldcMasterService) {
		this.ldcMasterService = ldcMasterService;
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
	@PostMapping("/ldc-master")
	public ResponseEntity<ApiStatus<LdcResponseDTO>> createLDC(@RequestBody LdcMasterDTO ldcMasterDTO,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName)
			throws IntrusionException, ValidationException, ParseException {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);

		if (ldcMasterDTO.getApplicableFrom() == null) {
			throw new CustomException("Applicable From should not null", HttpStatus.BAD_REQUEST);
		}

		if (ldcMasterDTO.getApplicableTo() != null
				&& (ldcMasterDTO.getApplicableFrom().equals(ldcMasterDTO.getApplicableTo())
						|| ldcMasterDTO.getApplicableFrom().after(ldcMasterDTO.getApplicableTo()))) {
			throw new CustomException("From  Date should not be equals or greater than To Date",
					HttpStatus.BAD_REQUEST);
		}
		logger.info("amount is {}: ", ldcMasterDTO.getAmount());
		int checkAmount = ldcMasterDTO.getAmount().compareTo(BigDecimal.valueOf(0));
		if (ldcMasterDTO.getAmount() != null && ldcMasterDTO.getLimitUtilised() != null) {
			if (checkAmount == -1) {
				throw new CustomException("Amount " + ldcMasterDTO.getAmount() + " should not contain -ve value",
						HttpStatus.BAD_REQUEST);
			}
			BigDecimal utilizedAmount = ldcMasterDTO.getLimitUtilised();
			logger.info("amount is {}: ", utilizedAmount);
			int checkUtilizedAmount = utilizedAmount.compareTo(BigDecimal.valueOf(0));
			if (checkUtilizedAmount == -1) {
				throw new CustomException(
						"Limit utilized amount " + ldcMasterDTO.getLimitUtilised() + "should not contain -ve value",
						HttpStatus.BAD_REQUEST);
			}
			int checkAmountAndUtilized = ldcMasterDTO.getLimitUtilised().compareTo(ldcMasterDTO.getAmount());

			if (checkAmountAndUtilized == 1) {
				throw new CustomException("Limit utilised amount should not be greater than certificate amount",
						HttpStatus.BAD_REQUEST);
			}
		}
		// ESAPI Validating user input
		SecurityValidations.ldcMasterInputValidation(ldcMasterDTO);
		LdcMaster responseDto = ldcMasterService.create(ldcMasterDTO, deductorTan, assesssmentYear, userName);
		LdcResponseDTO response = ldcMasterService.copyToEntity(responseDto);
		ApiStatus<LdcResponseDTO> apiStatus = new ApiStatus<LdcResponseDTO>(HttpStatus.OK, "SUCCESS",
				"CREATED LDC MASTER RECORD SUCCESSFULLY ", response);
		return new ResponseEntity<ApiStatus<LdcResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	@PutMapping("/ldc-master")
	public ResponseEntity<ApiStatus<LdcResponseDTO>> updateLDC(@RequestBody LdcMasterDTO ldcMasterDTO,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName)
			throws IntrusionException, ValidationException, ParseException {

		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		// ESAPI Validating user input
		SecurityValidations.ldcMasterInputValidation(ldcMasterDTO);
		LdcMaster responseDto = ldcMasterService.update(ldcMasterDTO, deductorTan, assesssmentYear, userName);
		LdcResponseDTO response = ldcMasterService.copyToEntity(responseDto);
		ApiStatus<LdcResponseDTO> apiStatus = new ApiStatus<LdcResponseDTO>(HttpStatus.OK, "SUCCESS",
				"UPDATED LDC MASTER RECORD SUCCESSFULLY ", response);
		return new ResponseEntity<ApiStatus<LdcResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping("ldc/import/pdf")
	public ResponseEntity<ApiStatus<BatchUploadResponseDTO>> uploadPdf(@RequestHeader("TAN-NUMBER") String tan,
			@RequestHeader("USER_NAME") String userName, @RequestParam("files") MultipartFile[] files,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		boolean validFiles = true;
		if (files != null && files.length > 0) {
			for (MultipartFile file : files) {
				String contentType = new Tika().detect(file.getInputStream());
				if (!AllowedMimeTypes.PDF.getMimeType().equals(contentType)) {
					validFiles = false;
				}
			}
			if (!validFiles) {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only pdf files are allowed", HttpStatus.BAD_REQUEST);
			}

			BatchUploadResponseDTO batchUpload = ldcMasterService.saveToBatchUploadPdfs(files, tan, assesssmentYear,
					userName);
			ApiStatus<BatchUploadResponseDTO> apiStatus = new ApiStatus<BatchUploadResponseDTO>(HttpStatus.OK,
					"SUCCESS", "UPDATED LDC MASTER RECORD SUCCESSFULLY ", batchUpload);
			return new ResponseEntity<ApiStatus<BatchUploadResponseDTO>>(apiStatus, HttpStatus.OK);

		} else {
			throw new IllegalArgumentException("Atleast one file must be uploaded.");
		}
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
	@PostMapping("ldc/validation/upload/excel")
	public ResponseEntity<ApiStatus<BatchUploadResponseDTO>> upload(@RequestHeader("TAN-NUMBER") String tan,
			@RequestHeader("USER_NAME") String userName, @RequestParam("file") MultipartFile file,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, EncryptedDocumentException,
			InvalidFormatException {
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
			assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
			BatchUploadResponseDTO batchUpload = ldcMasterService.saveToBatchUploadExcel(file, tan, assesssmentYear,
					userName);
			ApiStatus<BatchUploadResponseDTO> apiStatus = new ApiStatus<BatchUploadResponseDTO>(HttpStatus.OK,
					"SUCCESS", "UPLOADED LDC EXCEL FILE SUCCESSFULLY ", batchUpload);
			return new ResponseEntity<ApiStatus<BatchUploadResponseDTO>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping("/ldc-master/getldc")
	public ResponseEntity<ApiStatus<LdcMasterDTO>> getLDC(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestBody Map<String, String> requestParams) {

		Integer id = Integer.parseInt(requestParams.get("id"));
		LdcMasterDTO ldcMasterDTO = ldcMasterService.getLdcMasterDTO(deductorTan, id);

		ApiStatus<LdcMasterDTO> apiStatus = new ApiStatus<LdcMasterDTO>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				ldcMasterDTO);
		return new ResponseEntity<ApiStatus<LdcMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/ldc-master/{id}")
	public ResponseEntity<ApiStatus<LdcMasterDTO>> getLDC(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan, @PathVariable Integer id, // UUID
																												// id,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth) {
		LdcMasterDTO ldcMasterDTO = ldcMasterService.getLdcMasterDTO(deductorTan, id);

		ApiStatus<LdcMasterDTO> apiStatus = new ApiStatus<LdcMasterDTO>(HttpStatus.OK, "SUCCESS",
				"Retrieved single LDC record successfully", ldcMasterDTO);
		return new ResponseEntity<ApiStatus<LdcMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param assesssmentYear
	 * @param deductorTan     [ * @return
	 */
	@GetMapping(value = "/ldc-master", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<LdcMasterDTO>>> getListOfLdc(
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		List<LdcMasterDTO> listMasterDTOs = ldcMasterService.getListOfLdc(assesssmentYear, deductorTan, tenantId);
		ApiStatus<List<LdcMasterDTO>> apiStatus = new ApiStatus<List<LdcMasterDTO>>(HttpStatus.OK, "SUCCESS",
				"Retrieved list of LDC records successfully", listMasterDTOs);
		return new ResponseEntity<ApiStatus<List<LdcMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	// EXPORT EXCEL FILE
	/**
	 * @return
	 * @throws IOException
	 */
	@GetMapping(value = "validation/ldc/exporttoexcel")
	public ResponseEntity<InputStreamResource> exportToExcels() throws IOException {
		ByteArrayInputStream in = ldcMasterService.exportToExcel();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=ldc.xlsx");
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
	}

	@PostMapping(value = "/createnotification", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<EmailNotificationConfiguration>>> createNotifications(
			@Valid @RequestBody List<EmailNotificationConfigurationDTO> emailNotificationConfigDTOList,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		List<EmailNotificationConfiguration> response = ldcMasterService
				.createNotification(emailNotificationConfigDTOList, userName);
		ApiStatus<List<EmailNotificationConfiguration>> apiStatus = new ApiStatus<List<EmailNotificationConfiguration>>(
				HttpStatus.OK, "SUCCESS", "NOTIFICATION  RECORD CREATION SUCCESSFULLY ", response);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/getnotification", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<EmailNotificationConfigurationDTO>>> getAllNotification() {
		List<EmailNotificationConfigurationDTO> response = ldcMasterService.getAllNotification();
		ApiStatus<List<EmailNotificationConfigurationDTO>> apiStatus = new ApiStatus<List<EmailNotificationConfigurationDTO>>(
				HttpStatus.OK, "SUCCESS", "GETING ALL RECORD SUCCESSFULLY ", response);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param assesssmentYear
	 * @param deductorTan
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/ldcmaster", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<LdcResponseDTO>> getOneLdcMaster(
			@RequestParam(value = "assessmentYear", required = false) Integer assessmentYear,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		LdcMaster listMasterDto = ldcMasterService.getOneLdcMaste(CommonUtil.getAssessmentYear(assessmentYear),
				deductorTan);
		LdcResponseDTO listMasterDTOs = ldcMasterService.copyToEntity(listMasterDto);
		ApiStatus<LdcResponseDTO> apiStatus = new ApiStatus<LdcResponseDTO>(HttpStatus.OK, "SUCCESS",
				"Retrieved LDC records successfully", listMasterDTOs);
		return new ResponseEntity<ApiStatus<LdcResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param assesssmentYear
	 * @param deductorTan
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/ldc/validation/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> getLdcValidationStatus(
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestParam(value = "assessmentYear", required = false) Integer assessmentYear,
			@RequestParam(value = "assessmentMonth", required = true) Integer assessmentMonth) {
		String startDate = CommonUtil.getQueryDate(CommonUtil.getMonthStartDate(assessmentYear, assessmentMonth - 1));
		String endDate = CommonUtil.getQueryDate(CommonUtil.getMonthEndDate(assessmentYear, assessmentMonth - 1));
		String result = ldcMasterService.getLdcMasterStatus(deductorTan, startDate, endDate);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "Fetched LDC validation successfully",
				result);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * To accept the uploaded LdcMaster Excel file and process it
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

	@PostMapping(value = "/ldc/master/upload/excel")
	public ResponseEntity<ApiStatus<BatchUploadResponseDTO>> uploadExcel(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		BatchUploadResponseDTO batchUpload = null;
		MultiTenantContext.setTenantId(tenantId);
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
				batchUpload = ldcMasterService.saveFileData(file, tan, assesssmentYear, assessmentMonth, userName,
						tenantId, pan);
				ApiStatus<BatchUploadResponseDTO> apiStatus = new ApiStatus<BatchUploadResponseDTO>(HttpStatus.OK,
						"SUCCESS", "NO ALERT", batchUpload);
				return new ResponseEntity<ApiStatus<BatchUploadResponseDTO>>(apiStatus, HttpStatus.OK);
			} else {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
			}
		}
	}

	@PostMapping(value = "/ldc/master/upload/pdf")
	public ResponseEntity<ApiStatus<BatchUploadResponseDTO>> uploadPdf(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		BatchUploadResponseDTO batchUpload = null;
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
				batchUpload = ldcMasterService.saveLdcPdfData(file, tan, assesssmentYear, assessmentMonth, userName,
						tenantId, pan);
				ApiStatus<BatchUploadResponseDTO> apiStatus = new ApiStatus<BatchUploadResponseDTO>(HttpStatus.OK,
						"SUCCESS", "UPLOADED LDC PDF FILE SUCCESSFULLY ", batchUpload);
				return new ResponseEntity<ApiStatus<BatchUploadResponseDTO>>(apiStatus, HttpStatus.OK);
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
	@PostMapping(value = "/ldcMasterData/export", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<InputStreamResource> exportLdcReport(
			@RequestParam(value = "tan", required = true) String deductorTan,
			@RequestParam(value = "tenantId", required = true) String tenantId)
			throws IOException, TanNotFoundException {

		logger.info("TAN: {}", deductorTan);
		MultiTenantContext.setTenantId(tenantId);
		if (deductorTan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}

		ByteArrayInputStream in = ldcMasterService.exportLdcMasterData(deductorTan, tenantId);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=LDC_MASTER_DATA_" + deductorTan + ".xlsx");

		logger.info("LdcMaster data export done ");
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
	@PostMapping("/ldc-master/deactive")
	public ResponseEntity<ApiStatus<LdcResponseDTO>> deactivateLdcCertificate(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestBody Map<String, String> requestParams, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader("X-TENANT-ID") String tenantId) {
		Integer id = Integer.parseInt(requestParams.get("id"));
		String ldcPan = requestParams.get("ldcPan");
		LdcMaster ldcMasterDto = ldcMasterService.deactivateLdcCertificate(ldcPan, deductorTan, id);
		LdcResponseDTO ldcMaster = ldcMasterService.copyToEntity(ldcMasterDto);
		ApiStatus<LdcResponseDTO> apiStatus = new ApiStatus<LdcResponseDTO>(HttpStatus.OK, "SUCCESS",
				"DEACTIVATE LDC RECORD SUCCESSFULLY", ldcMaster);
		return new ResponseEntity<ApiStatus<LdcResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call to get ldc for tds-ingestion
	 * 
	 * @param deductorTan
	 * @param certificateNo
	 * @return
	 */
	@GetMapping("/ldc-master/by/certificateNo")
	public ResponseEntity<ApiStatus<List<LdcMaster>>> getLdcByCertificateNo(@RequestParam("TAN") String deductorTan,
			@RequestParam("CERTIFICATENO") String certificateNo, @RequestHeader("TENANT") String tenantId) {
		// setting tenant id
		MultiTenantContext.setTenantId(tenantId);
		List<LdcMaster> list = ldcMasterDAO.getLdcBycertificateNoAndTan(certificateNo, deductorTan);
		ApiStatus<List<LdcMaster>> apiStatus = new ApiStatus<List<LdcMaster>>(HttpStatus.OK, "SUCCESS",
				"DEACTIVATE LDC RECORD SUCCESSFULLY", list);
		return new ResponseEntity<ApiStatus<List<LdcMaster>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call to get valid ldc records for ldc matrix
	 * 
	 * @param deductorTan
	 * @param certificateNo
	 * @param tenantId
	 * @return
	 */
	@GetMapping("/valid/ldc/by/certificateNo")
	public ResponseEntity<ApiStatus<List<LdcMaster>>> getValidLdcByCertificateNo(
			@RequestParam("TAN") String deductorTan, @RequestParam("CERTIFICATENO") String certificateNo,
			@RequestHeader("TENANT") String tenantId) {
		// setting tenant id
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Feign call to get valid LDC record with tenant {}", tenantId);
		List<LdcMaster> list = ldcMasterDAO.getValidLdcBycertificateNoAndTan(certificateNo, deductorTan);
		ApiStatus<List<LdcMaster>> apiStatus = new ApiStatus<List<LdcMaster>>(HttpStatus.OK, "SUCCESS",
				"DEACTIVATE LDC RECORD SUCCESSFULLY", list);
		return new ResponseEntity<ApiStatus<List<LdcMaster>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client get ldc email notification
	 * 
	 * @param type
	 * @param tenantId
	 * @return
	 */
	@GetMapping("/getemail/notificatoin")
	public ResponseEntity<ApiStatus<List<EmailNotificationConfiguration>>> findByType(@RequestParam("type") String type,
			@RequestHeader("X-TENANT-ID") String tenantId) {
		List<EmailNotificationConfiguration> response = ldcMasterService.findByType(type, tenantId);
		ApiStatus<List<EmailNotificationConfiguration>> apiStatus = new ApiStatus<List<EmailNotificationConfiguration>>(
				HttpStatus.OK, "SUCCESS", "GET ALL RECORD SUCCESSFULLY ", response);
		return new ResponseEntity<ApiStatus<List<EmailNotificationConfiguration>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param ldcId
	 * @return
	 */
	@PostMapping("/ldc-master/extendldc/{ldcId}")
	public ResponseEntity<ApiStatus<LdcMaster>> extendLdcCertificate(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@PathVariable("ldcId") int ldcId) {
		LdcMaster ldcMaster = ldcMasterService.extendLdcCertificate(ldcId, deductorTan);
		ApiStatus<LdcMaster> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"Extended ldc certificate successfully.", ldcMaster);
		return new ResponseEntity<ApiStatus<LdcMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client
	 * 
	 * @param deductorTan
	 * @param lccMasterId
	 * @return
	 */
	@GetMapping("/ldc-utilization")
	public ResponseEntity<ApiStatus<List<LdcUtilization>>> ldcUtilization(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader Integer ldcMasterId, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		List<LdcUtilization> lccUtilizationList = ldcMasterService.ldcUtilization(deductorTan, ldcMasterId);
		ApiStatus<List<LdcUtilization>> apiStatus = new ApiStatus<List<LdcUtilization>>(HttpStatus.OK, "SUCCESS",
				"GET LDC UTILIZATION RECORD SUCCESSFULLY", lccUtilizationList);
		return new ResponseEntity<ApiStatus<List<LdcUtilization>>>(apiStatus, HttpStatus.OK);
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
	@PostMapping("/emailnotification")
	public ResponseEntity<ApiStatus<EmailNotification>> createEmailNotification(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestBody EmailNotification emailNotification) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		EmailNotification result = emailNotificationService.create(emailNotification, tenantId);
		ApiStatus<EmailNotification> apiStatus = new ApiStatus<EmailNotification>(HttpStatus.CREATED, "SUCCESS",
				"Email created successfully", result);
		return new ResponseEntity<ApiStatus<EmailNotification>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client
	 * 
	 * @param deductorTan
	 * @param tenantId
	 * @param userName
	 * @param pan
	 * @param emailNotification
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/emailnotification")
	public ResponseEntity<ApiStatus<EmailNotification>> getEmailNotification(
			@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorMasterPan,
			@RequestParam(value = "ldcMasterId") Integer ldcMasterId, @RequestParam(value = "rate") int rate,
			@RequestParam(value = "type") String type) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		EmailNotification emailNotification = emailNotificationService
				.getNotificationAtLdcThresholdRate(deductorMasterPan, ldcMasterId, type, rate, tenantId);
		ApiStatus<EmailNotification> apiStatus = new ApiStatus<EmailNotification>(HttpStatus.OK, "SUCCESS",
				"get email notification successfully", emailNotification);
		return new ResponseEntity<ApiStatus<EmailNotification>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client for update email notification
	 * 
	 * @param deductorTan
	 * @param tenantId
	 * @param pan
	 * @param emailNotification
	 * @return
	 * @throws Exception
	 */
	@PutMapping("/emailnotification")
	public ResponseEntity<ApiStatus<EmailNotification>> updateEmailNotification(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestBody EmailNotification emailNotification) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		EmailNotification result = emailNotificationService.upadte(emailNotification, tenantId);
		ApiStatus<EmailNotification> apiStatus = new ApiStatus<EmailNotification>(HttpStatus.CREATED, "SUCCESS",
				"TCS Email created successfully", result);
		return new ResponseEntity<ApiStatus<EmailNotification>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorTan
	 * @return
	 */
	@GetMapping(value = "/ldc-master/new-traces", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<NewLDCMasterTracesDTO>>> getListOfNewLdcMaster(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan) {
		List<NewLDCMasterTracesDTO> newLdcs = ldcMasterService.getNewLdcsByTan(deductorTan);
		ApiStatus<List<NewLDCMasterTracesDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"Retrieved list of LDC records successfully", newLdcs);
		return new ResponseEntity<ApiStatus<List<NewLDCMasterTracesDTO>>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping("/ldc/approvefromtraces")
	public ResponseEntity<ApiStatus<String>> approveFromTraces(@RequestBody NewLdcTracesDTO newLdcTracesDTO,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName)
			throws IntrusionException, ValidationException, ParseException {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);

		ldcMasterService.approveFromTraces(newLdcTracesDTO.getLdc(), deductorTan, assesssmentYear, userName);

		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"CREATED LDC MASTER RECORD SUCCESSFULLY ", "");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping("/new-ldc/delete/{id}")
	public ResponseEntity<ApiStatus<String>> deleteNewLdc(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestBody Map<String, String> requestParams, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader("X-TENANT-ID") String tenantId, @PathVariable("id") Integer id) {
		MultiTenantContext.setTenantId(tenantId);
		String ldcPan = requestParams.get("ldcPan");
		Integer assesssmentYear = Integer.parseInt(requestParams.get("assesssmentYear"));
		ldcMasterService.deleteNewLdcByID(ldcPan, deductorTan, id, assesssmentYear);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"DELETED NEW LDC RECORD SUCCESSFULLY", "SUCCESS");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorTan
	 * @return
	 */
	@PutMapping(value = "/ldc-master/traces", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> getListOfNewLdcMasterTraces(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader("X-TENANT-ID") String tenantId) {
		ldcMasterService.getNewLdcsTracesByTan(deductorTan, tenantId);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"Updated new ldc master traces successfully");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/ldc-master/type/residentialStatus/{ldcMasterPan}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<LdcMasterDTO>>> getListOfLdcByTypeResidentialStatus(
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestBody Pagination pagination, @RequestParam(value = "type") String type,
			@RequestParam(value = "residentialStatus") String residentialStatus,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @PathVariable("ldcMasterPan") String ldcMasterPan) {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		CommonDTO<LdcMasterDTO> listMasterDTOs = ldcMasterService.getListOfLdcByTypeAndResidentialStatus(
				assesssmentYear, deductorTan, tenantId, type, residentialStatus, pagination, ldcMasterPan);
		ApiStatus<CommonDTO<LdcMasterDTO>> apiStatus = new ApiStatus<CommonDTO<LdcMasterDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", listMasterDTOs);
		return new ResponseEntity<ApiStatus<CommonDTO<LdcMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/ldc-master/searchByPAN", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<LdcMasterDTO>>> getLdcByDeducteeOrShareHolderPan(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestParam(value = "PAN") String ldcPan) {
		List<LdcMasterDTO> listMasterDTOs = ldcMasterService.getLdcByshareHolderOrDeducteePan(deductorTan, tenantId,
				ldcPan);
		ApiStatus<List<LdcMasterDTO>> apiStatus = new ApiStatus<List<LdcMasterDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", listMasterDTOs);
		return new ResponseEntity<ApiStatus<List<LdcMasterDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * 
	 * @param year
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/ldc-master/traces/report")
	public ResponseEntity<ApiStatus<String>> newTracesLdcMasterReport(@RequestParam(value = "year") int year,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		ldcMasterService.newTracesLdcMasterReport(tenantId, deductorTan, userName, year, deductorPan);
		String message = "new traces ldc master requested successfully and will be available shortly";
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
	@PostMapping(value = "/ldc-master/traces/report/import")
	public ResponseEntity<ApiStatus<BatchUpload>> uploadNewTracesLdcExcel(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		int assesssmentYear = CommonUtil.getAssessmentYear(null);
		int assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(null);
		BatchUpload batchUpload = null;
		MultiTenantContext.setTenantId(tenantId);
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			String contentType = new Tika().detect(file.getInputStream());
			if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
					|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
				batchUpload = ldcMasterService.newTracesLdcUploadExcel(file, deductorTan, assesssmentYear,
						assessmentMonth, userName, tenantId, deductorPan);
				ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS", "NO ALERT",
						batchUpload);
				return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
			} else {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
			}
		}
	}

}
