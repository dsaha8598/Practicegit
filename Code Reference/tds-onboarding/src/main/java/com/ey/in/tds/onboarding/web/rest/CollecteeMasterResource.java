package com.ey.in.tds.onboarding.web.rest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeMasterDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeNamePanDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetails;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.security.SecurityValidations;
import com.ey.in.tds.common.service.TCSActivityTrackerService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.util.ActivityType;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.jdbc.dao.CollecteeMasterDAO;
import com.ey.in.tds.jdbc.dao.CollectorOnBoardingInfoDAO;
import com.ey.in.tds.onboarding.service.deductee.CollecteeMasterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

/**
 * 
 * @author vamsir
 *
 */
@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin("*")
public class CollecteeMasterResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CollecteeMasterService collecteeMasterService;

	@Autowired
	private CollecteeMasterDAO collecteeMasterDAO;

	@Autowired
	private TCSActivityTrackerService tcsActivityTrackerService;
	
	@Autowired
	private CollectorOnBoardingInfoDAO collectorOnBoardingInfoDAO;

	/**
	 * 
	 * @param collecteeMaster
	 * @param deductorTan
	 * @param deductorPan
	 * @param userName
	 * @return
	 * @throws URISyntaxException
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IOException 
	 * @throws StorageException 
	 * @throws InvalidKeyException 
	 */
	@PostMapping(value = "/collectee")
	public ResponseEntity<ApiStatus<CollecteeMaster>> createDeductee(
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam("data") String collecteeMasterData,
			@RequestHeader(value = "TAN-NUMBER", required = true) String collectorTan,
			@RequestHeader("DEDUCTOR-PAN") String collectorPan, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "X-TENANT-ID") String tenantId)
			throws URISyntaxException, ValidationException, ParseException, IllegalAccessException,
			InvocationTargetException, InvalidKeyException, StorageException, IOException {

		logger.info("REST request to save collectee master : {}", collecteeMasterData);

		CollecteeMasterDTO collecteeMaster = new ObjectMapper().readValue(collecteeMasterData,
				CollecteeMasterDTO.class);

		if (collecteeMaster.getApplicableFrom() == null) {
			throw new CustomException("Applicable From should not null", HttpStatus.BAD_REQUEST);
		}
		if (collecteeMaster.getApplicableTo() != null
				&& (collecteeMaster.getApplicableFrom().equals(collecteeMaster.getApplicableTo())
						|| collecteeMaster.getApplicableFrom().after(collecteeMaster.getApplicableTo()))) {
			throw new CustomException("From Date should not be equals or greater than To Date", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		SecurityValidations.collecteeMasterInputValidation(collecteeMaster);
		CollecteeMaster result = collecteeMasterService.createCollectee(collecteeMaster, collectorPan, userName,
				collectorTan, file, tenantId);
		updateActivityStatus(collectorTan, collectorPan, userName);
		ApiStatus<CollecteeMaster> apiStatus = new ApiStatus<CollecteeMaster>(HttpStatus.OK, "SUCCESS",
				"CREATED A COLLECTEE MASTER RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<CollecteeMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param deductorPan
	 * @param userName
	 */
	private void updateActivityStatus(String deductorTan, String deductorPan, String userName) {
		Integer assessmentYear = CommonUtil.getAssessmentYear(null);
		Integer assessmentMonthPlusOne = CommonUtil.getAssessmentMonthPlusOne(null);
		String activityStatus = collecteeMasterService.getCollecteePanStatus(deductorPan, assessmentYear,
				assessmentMonthPlusOne);
		tcsActivityTrackerService.updateActivity(deductorTan, assessmentYear, assessmentMonthPlusOne, userName,
				ActivityType.PAN_VERIFICATION.getActivityType(), activityStatus);
	}

	/**
	 * 
	 * @param collectorPan
	 * @param id
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@GetMapping(value = "/collectee/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CollecteeMasterDTO>> getCollecteeMaster(
			@RequestHeader("DEDUCTOR-PAN") String collectorPan, @PathVariable Integer id,
			@RequestHeader(value = "X-TENANT-ID") String tenantId)
			throws JsonProcessingException, IllegalAccessException, InvocationTargetException {
		CollecteeMasterDTO result = collecteeMasterService.getCollectee(collectorPan, id);
		logger.info("REST response to get a collectee Record : {}", result);
		ApiStatus<CollecteeMasterDTO> apiStatus = new ApiStatus<CollecteeMasterDTO>(HttpStatus.OK, "SUCCESS",
				"TO GET A COLLECTEE RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<CollecteeMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deducteeMaster
	 * @param tenantId
	 * @param deductorTan
	 * @param deductorPan
	 * @param userName
	 * @return
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IOException 
	 * @throws StorageException 
	 * @throws URISyntaxException 
	 * @throws InvalidKeyException 
	 */
	@PutMapping(value = "/collectee")
	public ResponseEntity<ApiStatus<CollecteeMaster>> updateCollecteeMaster(
			@RequestParam("data") String collecteeMasterData, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader(value = "USER_NAME") String userName)
			throws ValidationException, ParseException, IllegalAccessException,
			InvocationTargetException, InvalidKeyException, URISyntaxException, StorageException, IOException {
		logger.info("REST request to save collectee master : {}", collecteeMasterData);
		CollecteeMasterDTO collecteeMasterDTO = new ObjectMapper().readValue(collecteeMasterData,
				CollecteeMasterDTO.class);
		// ESAPI Validating user input
		SecurityValidations.collecteeMasterInputValidation(collecteeMasterDTO);
		CollecteeMaster collecteeMaster = collecteeMasterService.updateCollecteeMaster(collecteeMasterDTO, deductorPan,
				userName,file);
		ApiStatus<CollecteeMaster> apiStatus = new ApiStatus<CollecteeMaster>(HttpStatus.OK, "SUCCESS",
				"UPDATED A COLLECTEE MASTER RECORDS SUCCESSFULLY ", collecteeMaster);
		return new ResponseEntity<ApiStatus<CollecteeMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api for get all collectee name's
	 * 
	 * @param deductorPan
	 * @param pagination
	 * @param deducteeName
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@PostMapping(value = "/collectee/all/{collecteeName}/{collecteeCode}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<CollecteeMasterDTO>>> getCollecteeMaster(
			@RequestHeader("DEDUCTOR-PAN") String collectorPan, @RequestBody Pagination pagination,
			@PathVariable String collecteeName, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@PathVariable String collecteeCode)
			throws JsonMappingException, JsonProcessingException, IllegalAccessException, InvocationTargetException {
		ApiStatus<CommonDTO<CollecteeMasterDTO>> apiStatus = new ApiStatus<CommonDTO<CollecteeMasterDTO>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT",
				collecteeMasterService.getListOfCollectee(collectorPan, pagination, collecteeName, collecteeCode));
		return new ResponseEntity<ApiStatus<CommonDTO<CollecteeMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	@GetMapping(value = "/collecteenames", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Set<String>>> getCollecteeNames(@RequestHeader("DEDUCTOR-PAN") String collectorPan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		ApiStatus<Set<String>> apiStatus = new ApiStatus<Set<String>>(HttpStatus.OK, "SUCCESS",
				"LIST OF COLLECTEE NAMES", collecteeMasterService.getCollecteeNames(collectorPan));
		return new ResponseEntity<ApiStatus<Set<String>>>(apiStatus, HttpStatus.OK);
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
	@PostMapping(value = "/collectee/upload/excel")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> uploadExcel(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String collectorTan,
			@RequestHeader(value = "DEDUCTOR-PAN") String collectorPan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		TCSBatchUpload batchUpload = null;
		logger.info("Testing Something in Upload Excel");
		logger.info("TAN-NUMBER: {}", collectorTan);
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			batchUpload = collecteeMasterService.saveFileData(file, collectorTan, assesssmentYear, assessmentMonth,
					userName, tenantId, collectorPan);
		}
		ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED COLLECTEE FILE SUCCESSFULLY ", batchUpload);
		return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param file
	 * @param tan
	 * @param collectorPan
	 * @param userName
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param tenantId
	 * @param batchId
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/collectee/import/csv")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> readImportedCsvData(@PathVariable("file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String collectorPan,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestParam(value = "batchId") Integer batchId)
			throws Exception {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		logger.info("Tenant ID-----: {}", tenantId);
		TCSBatchUpload response = collecteeMasterService.readCsvFile(file, tan, tenantId, assesssmentYear,
				assessmentMonth, userName, collectorPan, batchId);
		ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED COLLECTEE CSV FILE SUCCESSFULLY ", response);
		return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * fiegn call to get list of collectee pan based on collector pan
	 * 
	 * @param panStatus
	 * @param collectorPan
	 * @return
	 */
	@GetMapping(value = "/collecteePans")
	public ResponseEntity<ApiStatus<List<CollecteeMaster>>> getListOfCollecteePans(
			@RequestParam("pan") String collectorPan, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("fiegn call executing to get list of deductee pans  {}");
		List<CollecteeMaster> list = collecteeMasterDAO.findAllByCollecteePans(collectorPan);
		ApiStatus<List<CollecteeMaster>> apiStatus = new ApiStatus<List<CollecteeMaster>>(HttpStatus.OK, "SUCCESS",
				"RETRIEVED COLLECTEE PAN DETAILS SUCCESSFULLY ", list);
		logger.info("List of collectee pans are    {}" + list);
		return new ResponseEntity<ApiStatus<List<CollecteeMaster>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api for get all colllectee codes
	 * 
	 * @param deductorPan
	 * @return
	 */
	@GetMapping(value = "/collecteecodes", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Set<String>>> getCollecteeCodes(@RequestHeader("DEDUCTOR-PAN") String collectorPan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		ApiStatus<Set<String>> apiStatus = new ApiStatus<Set<String>>(HttpStatus.OK, "SUCCESS",
				"LIST OF COLLECTEE CODES", collecteeMasterService.getCollecteeCodes(collectorPan));
		return new ResponseEntity<ApiStatus<Set<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call to get the collectee status based on collectee code
	 * 
	 * @param collecteeCode
	 * @param tenantId
	 * @param tan
	 * @param pan
	 * @return
	 */
	@GetMapping(value = "/collecteeType")
	public ResponseEntity<ApiStatus<String>> getCollecteeTypeBasedOnCollecteeCode(
			@RequestHeader("COLLECTEE_CODE") String collecteeCode,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("tan") String tan,
			@RequestHeader("pan") String pan) {
		logger.info("Feign calll executing to get Collectee Status {}");
		MultiTenantContext.setTenantId(tenantId);
		String collecteeStatus = collecteeMasterService.getCollecteeType(collecteeCode, tan, pan);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "Collectee status",
				collecteeStatus);
		logger.info("Collectee Status From feign call is returned as {}" + collecteeStatus);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api for get collectee master data based on enter keyword
	 * 
	 * @param collectorPan
	 * @param keyenetered
	 * @param tenantId
	 * @param type
	 * @return
	 */
	@GetMapping(value = "/collectee/suggestions/{keyenetered}/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Set<String>>> getCollecteeMasterBasedOnKeyEntered(
			@RequestHeader("DEDUCTOR-PAN") String collectorPan, @PathVariable String keyenetered,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @PathVariable String type) {
		ApiStatus<Set<String>> apiStatus = new ApiStatus<Set<String>>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				collecteeMasterService.getCollecteeMasterBasedOnKeyEntered(collectorPan, keyenetered, type));
		return new ResponseEntity<ApiStatus<Set<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api for get all collectee names and pans based on collector pan.
	 * 
	 * @param collectorPan
	 * @param keyenetered
	 * @param tenantId
	 * @param type
	 * @return
	 */
	@GetMapping(value = "/collectee/names/pans", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<CollecteeNamePanDTO>>> getCollecteeMasterNamesPans(
			@RequestHeader("DEDUCTOR-PAN") String collectorPan, @RequestHeader(value = "X-TENANT-ID") String tenantId) {
		ApiStatus<List<CollecteeNamePanDTO>> apiStatus = new ApiStatus<List<CollecteeNamePanDTO>>(HttpStatus.OK,
				"GET COLLECTEE MASTER NAMES AND PANS", "NO ALERT",
				collecteeMasterService.getCollecteeMaster(collectorPan));
		return new ResponseEntity<ApiStatus<List<CollecteeNamePanDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param year
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/collectee/pan/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> getCollecteePanStatus(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestParam(value = "assessmentMonth", required = true) Integer assessmentMonth) {
		String result = collecteeMasterService.getCollecteePanStatus(deductorPan, assessmentYear, assessmentMonth);
		logger.info("REST response to get a Deductee Resident Record : {}", result);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"TO GET A DEDUCTEE RESIDENT RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param collectorTan
	 * @param pan
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws JsonProcessingException
	 */
	@GetMapping(value = "/collection/route")
	public ResponseEntity<ApiStatus<String>> collectionroute(
			@RequestHeader(value = "TAN-NUMBER") String collectorTan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName)
			throws JsonProcessingException {

		String collectionReferenceId = collectorOnBoardingInfoDAO.getCollectionReferenceId(pan);
		String status = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(collectionReferenceId)) {
			status = "Collection Route";
		} else {
			status = "Invoice Route";
		}
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"Collection route status ", status);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	
	/**
	 * This API for move to next year all collectee master details
	 * 
	 * @param deductorTan
	 * @param year
	 * @param tenantId
	 * @return
	 */
	@PostMapping(value = "/tcs/collectee/nextyear", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSBatchUpload>> getAllCollecteeMoveToNextYear(
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER", required = true) String collectorTan,
			@RequestHeader("DEDUCTOR-PAN") String collectorPan,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestHeader(value = "USER_NAME") String userName) {
		MultiTenantContext.setTenantId(tenantId);
		TCSBatchUpload tcsBatchUpload = collecteeMasterService.collecteeMoveToNextYear(collectorPan, assessmentYear,
				collectorTan, userName);
		ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
				"ALL COLLECTEE MASTER MOVED TO NEXT YEAR SUCCESSFULLY ", tcsBatchUpload);
		return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method for get kyc details based on collectee pan
	 * 
	 * @param deductorPan
	 * @param tenantId
	 * @param deducteePan
	 * @return
	 */
	@GetMapping(value = "/tcs/collectee/kyc", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<KYCDetails>> getKYCDetailsBasedOnPan(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam("collecteePan") String collecteePan, @RequestHeader(value = "TAN-NUMBER") String tan) {
		MultiTenantContext.setTenantId(tenantId);
		KYCDetails kycDetails = collecteeMasterService.getKycDetailsBasedOnPan(deductorPan, collecteePan, tan);
		ApiStatus<KYCDetails> apiStatus = new ApiStatus<KYCDetails>(HttpStatus.OK, "SUCCESS",
				"GET KYC DETAILS SUCCESSFULLY", kycDetails);
		return new ResponseEntity<ApiStatus<KYCDetails>>(apiStatus, HttpStatus.OK);
	}

}
