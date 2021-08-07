package com.ey.in.tds.onboarding.web.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tika.Tika;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.DeducteeMasterNonResidentialDTO;
import com.ey.in.tds.common.dto.onboarding.deductee.DeducteeMasterDTO;
import com.ey.in.tds.common.dto.onboarding.deductee.DeducteeMasterResidentDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterNonResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetails;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.security.SecurityValidations;
import com.ey.in.tds.common.service.ActivityTrackerService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ActivityType;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.jdbc.dao.DeducteeMasterNonResidentialDAO;
import com.ey.in.tds.jdbc.dao.DeducteeMasterResidentialDAO;
import com.ey.in.tds.onboarding.dto.deductee.CustomDeducteesDTO;
import com.ey.in.tds.onboarding.dto.deductee.CustomDeducteesNonResidentDTO;
import com.ey.in.tds.onboarding.service.deductee.DeducteeMasterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin("*")
public class DeducteeMasterResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DeducteeMasterService deducteeMasterService;

	@Autowired
	private ActivityTrackerService activityTrackerService;

	@Autowired
	private DeducteeMasterResidentialDAO deducteeMasterResidentialDAO;
	
	@Autowired
	private DeducteeMasterNonResidentialDAO deducteeMasterNonResidentialDAO;

	/**
	 * POST /deductee/resident : Create a new deductee master for residents.
	 *
	 * @param deducteeMasterResidential
	 * 
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         deducteeMasterResidential
	 * @throws URISyntaxException      if the Location URI syntax is incorrect
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 * @throws JsonProcessingException
	 */
	@PostMapping(value = "/deductee/resident", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DeducteeMasterResidential>> createDeductee(
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestBody DeducteeMasterResidentDTO deducteeMasterResidential,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader(value = "USER_NAME") String userName)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException,
			JsonProcessingException {

		MultiTenantContext.setTenantId(tenantId);
		if (deducteeMasterResidential.getApplicableFrom() == null) {
			throw new CustomException("Applicable From should not null", HttpStatus.BAD_REQUEST);
		}

		if (deducteeMasterResidential.getApplicableTo() != null && (deducteeMasterResidential.getApplicableFrom()
				.equals(deducteeMasterResidential.getApplicableTo())
				|| deducteeMasterResidential.getApplicableFrom().after(deducteeMasterResidential.getApplicableTo()))) {
			throw new CustomException("From Date should not be equals or greater than To Date", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		SecurityValidations.deducteeResidentialInputValidation(deducteeMasterResidential);
		DeducteeMasterResidential result = deducteeMasterService.createResident(deducteeMasterResidential, deductorPan,
				userName, deductorTan);

		updateActivityStatus(deductorTan, deductorPan, userName);

		ApiStatus<DeducteeMasterResidential> apiStatus = new ApiStatus<DeducteeMasterResidential>(HttpStatus.OK,
				"SUCCESS", "CREATED A DEDUCTEE RESIDENT RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<DeducteeMasterResidential>>(apiStatus, HttpStatus.OK);
	}

	private void updateActivityStatus(String deductorTan, String deductorPan, String userName) {
		Integer assessmentYear = CommonUtil.getAssessmentYear(null);
		Integer assessmentMonthPlusOne = CommonUtil.getAssessmentMonthPlusOne(null);
		String activityStatus = deducteeMasterService.getDeducteePanStatus(deductorPan, assessmentYear,
				assessmentMonthPlusOne);
		activityTrackerService.updateActivity(deductorTan, assessmentYear, assessmentMonthPlusOne, userName,
				ActivityType.PAN_VERIFICATION.getActivityType(), activityStatus);
	}

	/**
	 * GET /deductee/resident:id : get the "id" deducteeMasterResident.
	 *
	 * @param id the id of the deducteeMasterResident to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         deducteeMasterResident, or with status 404 (Not Found)
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	@GetMapping(value = "/deductee/resident/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DeducteeMasterDTO>> getResidentialDeductee(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@PathVariable Integer id) throws JsonMappingException, JsonProcessingException {
		MultiTenantContext.setTenantId(tenantId);
		DeducteeMasterDTO result = deducteeMasterService.getResidential(deductorPan, id);
		logger.info("REST response to get a Deductee Resident Record : {}", result);
		ApiStatus<DeducteeMasterDTO> apiStatus = new ApiStatus<DeducteeMasterDTO>(HttpStatus.OK, "SUCCESS",
				"TO GET A DEDUCTEE RESIDENT RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<DeducteeMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /deductee/resident/all : get all the deducteeMasterResident.
	 * 
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         deducteeMasterResident in body
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	@PostMapping(value = "/deductee/resident/all/{deducteeName}/{deducteeCode}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<DeducteeMasterDTO>>> getResidentialDeductee(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestBody Pagination pagination, @PathVariable String deducteeName, @PathVariable String deducteeCode)
			throws JsonMappingException, JsonProcessingException {
		MultiTenantContext.setTenantId(tenantId);
		ApiStatus<CommonDTO<DeducteeMasterDTO>> apiStatus = new ApiStatus<CommonDTO<DeducteeMasterDTO>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", deducteeMasterService.getListOfResidentialDeductees(deductorPan, pagination,
						deducteeName, deducteeCode));
		return new ResponseEntity<ApiStatus<CommonDTO<DeducteeMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * PUT /deductee/resident : Updates an existing deducteeMasterResident.
	 *
	 * @param deducteeMasterResident the deducteeMasterResident to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         deducteeMasterResident, or with status 400 (Bad Request) if the
	 *         deducteeMasterResident is not valid, or with status 500 (Internal
	 *         Server Error) if the deducteeMasterResident couldn't be updated
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 * @throws JsonProcessingException
	 * @throws URISyntaxException      if the Location URI syntax is incorrect
	 */
	@PutMapping(value = "/deductee/resident", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DeducteeMasterResidential>> updateResidentDeductee(
			@RequestBody DeducteeMasterResidentDTO deducteeMaster,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader(value = "USER_NAME") String userName)
			throws IntrusionException, ValidationException, ParseException, JsonProcessingException {
		// ESAPI Validating user input
		SecurityValidations.deducteeResidentialInputValidation(deducteeMaster);
		ApiStatus<DeducteeMasterResidential> apiStatus = new ApiStatus<DeducteeMasterResidential>(HttpStatus.OK,
				"SUCCESS", "UPDATED A DEDUCTEE RESIDENT RECORDS SUCCESSFULLY ",
				deducteeMasterService.updateResident(deducteeMaster, deductorPan, userName));
		updateActivityStatus(deductorTan, deductorPan, userName);
		return new ResponseEntity<ApiStatus<DeducteeMasterResidential>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * POST /deductee/nonresident : Create a new deductee master for non-residents.
	 *
	 * @param deducteeMasterNonResidential
	 * 
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         deducteeMasterResidential
	 * 
	 * @param deducteeMasterNonResidentDTO
	 * @param trcFile
	 * @param tenFFile
	 * @param wpeFile
	 * @param noPEFile
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */
	@PostMapping(value = "/deductee/nonresident")
	public @ResponseBody ResponseEntity<ApiStatus<DeducteeMasterNonResidential>> createDeductee(
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestParam("data") String deducteeMasterNonResident,
			@RequestParam(value = "trcFile", required = false) MultipartFile trcFile,
			@RequestParam(value = "tenFFile", required = false) MultipartFile tenFFile,
			@RequestParam(value = "wpeFile", required = false) MultipartFile wpeFile,
			@RequestParam(value = "noPEFile", required = false) MultipartFile noPEFile,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
			@RequestParam(value = "fixedBasedIndiaFile", required = false) MultipartFile isFixedBasedIndiaFile,
			@RequestHeader(value = "USER_NAME") String userName) throws InvalidKeyException, URISyntaxException,
			StorageException, IOException, IntrusionException, ValidationException, ParseException {

		logger.info("REST request to save Deductee Non Resident : {}", deducteeMasterNonResident);

		MultiTenantContext.setTenantId(tenantId);
		DeducteeMasterNonResidentialDTO deducteeMaster = new ObjectMapper().readValue(deducteeMasterNonResident,
				DeducteeMasterNonResidentialDTO.class);
		if (deducteeMaster.getApplicableFrom() == null) {
			throw new CustomException("Applicable From should not null", HttpStatus.BAD_REQUEST);
		}

		if (deducteeMaster.getApplicableTo() != null
				&& (deducteeMaster.getApplicableFrom().equals(deducteeMaster.getApplicableTo())
						|| deducteeMaster.getApplicableFrom().after(deducteeMaster.getApplicableTo()))) {
			throw new CustomException("From Date should not be equals or greater than To Date", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		SecurityValidations.deducteeResidentialNonInputValidation(deducteeMaster);
		DeducteeMasterNonResidential result = deducteeMasterService.createNonResident(deducteeMaster, trcFile, tenFFile,
				wpeFile, noPEFile, deductorPan, userName, isFixedBasedIndiaFile);
		ApiStatus<DeducteeMasterNonResidential> apiStatus = new ApiStatus<DeducteeMasterNonResidential>(HttpStatus.OK,
				"SUCCESS", "Non Resident Deductee added successfully", result);
		return new ResponseEntity<ApiStatus<DeducteeMasterNonResidential>>(apiStatus, HttpStatus.OK);
	}

	public boolean validateFile(MultipartFile file) throws IOException {
		if (file == null) {
			return true;
		} else {
			String contentType = new Tika().detect(file.getInputStream());
			if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
					|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * @param id
	 * @return
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	@GetMapping(value = "/deductee/nonresident/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DeducteeMasterDTO>> getNonResidentialDeductee(
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan, @PathVariable Integer id)
			throws JsonMappingException, JsonProcessingException {
		ApiStatus<DeducteeMasterDTO> apiStatus = new ApiStatus<DeducteeMasterDTO>(HttpStatus.OK, "SUCCESS",
				"RETRIEVED SINGLE RECORD SUCCESSFULLY ", deducteeMasterService.getNonResidential(deductorPan, id));
		return new ResponseEntity<ApiStatus<DeducteeMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @return
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	@PostMapping(value = "/deductee/nonresident/all/{deducteeName}/{deducteeCode}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<DeducteeMasterDTO>>> getNonResidentialDeductee(
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
			@RequestBody Pagination pagination, @PathVariable String deducteeName, @PathVariable String deducteeCode)
			throws JsonMappingException, JsonProcessingException {
		ApiStatus<CommonDTO<DeducteeMasterDTO>> apiStatus = new ApiStatus<CommonDTO<DeducteeMasterDTO>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", deducteeMasterService.getListOfNonResidentialDeductees(deductorPan, pagination,
						deducteeName, deducteeCode));
		return new ResponseEntity<ApiStatus<CommonDTO<DeducteeMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @param deducteeMaster
	 * @return
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */

	@PutMapping(value = "/deductee/nonresident")
	public ResponseEntity<ApiStatus<DeducteeMasterNonResidential>> updateNonResidentDeductee(
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
			@RequestParam(value = "trcFile", required = false) MultipartFile trcFile,
			@RequestParam(value = "tenFFile", required = false) MultipartFile tenFFile,
			@RequestParam(value = "wpeFile", required = false) MultipartFile wpeFile,
			@RequestParam(value = "noPEFile", required = false) MultipartFile noPEFile,
			@RequestParam(value = "data", required = true) String deducteeMasterData,
			@RequestHeader(value = "USER_NAME") String userName) throws InvalidKeyException, URISyntaxException,
			StorageException, IOException, IntrusionException, ValidationException, ParseException {
		DeducteeMasterNonResidentialDTO deducteeMasterNonResidentialDTO = new ObjectMapper()
				.readValue(deducteeMasterData, DeducteeMasterNonResidentialDTO.class);
		// ESAPI Validating user input
		SecurityValidations.deducteeResidentialNonInputValidation(deducteeMasterNonResidentialDTO);
		DeducteeMasterNonResidential deducteeMasterNon = deducteeMasterService
				.updateNonResident(deducteeMasterNonResidentialDTO, deductorPan, trcFile, tenFFile, wpeFile, noPEFile);
		logger.info(String.format("Response is: %s", deducteeMasterNon));
		ApiStatus<DeducteeMasterNonResidential> apiStatus = new ApiStatus<DeducteeMasterNonResidential>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", deducteeMasterNon);
		return new ResponseEntity<ApiStatus<DeducteeMasterNonResidential>>(apiStatus, HttpStatus.OK);
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
	@PostMapping(value = "deductee/resident/upload/excel")
	public ResponseEntity<ApiStatus<BatchUpload>> uploadExcel(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		BatchUpload batchUpload = null;
		logger.info("TAN-NUMBER: {}", tan);
		boolean validFiles = true;
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (file.getSize() > 0) {
			String contentType = new Tika().detect(file.getInputStream(), file.getOriginalFilename());
			if (!AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
					&& !AllowedMimeTypes.OOXML.getMimeType().equals(contentType)
					&& !AllowedMimeTypes.CSV.getMimeType().equals(contentType)) {
				validFiles = false;
			}
			if (!validFiles) {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx and csv files are allowed",
						HttpStatus.BAD_REQUEST);
			}
			batchUpload = deducteeMasterService.saveFileData(file, tan, assesssmentYear, assessmentMonth, userName,
					tenantId, pan);
		}
		updateActivityStatus(tan, pan, userName);
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED DEDUCTEE FILE SUCCESSFULLY ", batchUpload);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tan
	 * @return
	 */
	@GetMapping(value = "deductees/nonresident", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<CustomDeducteesNonResidentDTO>>> getListOfOnlyDeductees(
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan) {
		List<CustomDeducteesNonResidentDTO> nrDeductees = deducteeMasterService.getListOfOnlyNonResidentDeductees(deductorPan);
		ApiStatus<List<CustomDeducteesNonResidentDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"LIST OF NON RESIDENT DEDUCTEES ", nrDeductees);
		return new ResponseEntity<ApiStatus<List<CustomDeducteesNonResidentDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get deductee Names and Pans based on deductor pan.
	 * 
	 * @param deductorPan
	 * @return
	 */
	@GetMapping(value = "deductees/resident", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<CustomDeducteesDTO>>> getListOfOnlyResidentDeductees(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan) {
		List<CustomDeducteesDTO> residentDeductees = deducteeMasterService.getListOfOnlyResidentDeductees(deductorPan);
		ApiStatus<List<CustomDeducteesDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"LIST OF RESIDENT DEDUCTEES", residentDeductees);
		return new ResponseEntity<ApiStatus<List<CustomDeducteesDTO>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "deductees/{deducteetype}/{type}/{year}/{month}/{ismismatch}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<String>>> getDeductees(@RequestHeader("TAN-NUMBER") String deductorTan,
			@PathVariable(value = "deducteetype", required = true) String deducteeType,
			@PathVariable(value = "type", required = true) String type,
			@PathVariable(value = "year", required = true) int year,
			@PathVariable(value = "month", required = true) int month,
			@PathVariable(value = "ismismatch", required = true) boolean isMismatch) {
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS",
				"LIST OF " + deducteeType.toUpperCase() + " DEDUCTEES",
				deducteeMasterService.getDeductees(deductorTan, deducteeType, type, year, month, isMismatch));
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api will return all the deductee names under the deductor pan.
	 * 
	 * @param type
	 * @param deductorPan
	 * @return
	 */
	@GetMapping(value = "deducteenames/{deducteetype}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Set<String>>> getDeducteeNames(
			@PathVariable(value = "deducteetype", required = true) String deducteeType,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan) {
		ApiStatus<Set<String>> apiStatus = new ApiStatus<Set<String>>(HttpStatus.OK, "SUCCESS",
				"LIST OF " + deducteeType.toUpperCase() + " DEDUCTEES",
				deducteeMasterService.getDeducteeNames(deductorPan, deducteeType));
		return new ResponseEntity<ApiStatus<Set<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /deductee/resident/all : get all the deducteeMasterResident. for returns
	 * module
	 * 
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         deducteeMasterResident in body
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	@GetMapping(value = "/data/deductee", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<DeducteeMasterDTO>>> getResidentialDeducteeForReturns(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId)
			throws JsonMappingException, JsonProcessingException {
		ApiStatus<List<DeducteeMasterDTO>> apiStatus = new ApiStatus<List<DeducteeMasterDTO>>(HttpStatus.OK, "SUCCESS",
				"GET LIST OF DEDUCTEES", deducteeMasterService.getListOfResidentialDeductees(deductorPan));
		return new ResponseEntity<ApiStatus<List<DeducteeMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param pan
	 * @return
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	@PostMapping("/deductee")
	public ResponseEntity<ApiStatus<Map<String, Object>>> getDeducteeBasedOnPan(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
			@RequestBody Map<String, String> requestParams) throws JsonMappingException, JsonProcessingException {
		String deducteePan = requestParams.get("deducteePan");
		String deducteeName = requestParams.get("deducteeName");
		String lineItemId = requestParams.get("invoiceLineItemId");
		String deducteeYear = requestParams.get("year");
		Integer invoiceLineItemId = Integer.parseInt(lineItemId);
		int year = Integer.parseInt(deducteeYear);
		String section = requestParams.get("clientSection");

		if (deductorPan == null) {
			throw new CustomException("Invalid PAN Number is should not be null", HttpStatus.BAD_REQUEST);
		}
		Map<String, Object> response = deducteeMasterService.getDeducteesByPanAndName(deductorTan, deducteePan,
				deducteeName, invoiceLineItemId, year, deductorPan, section);
		logger.info("Getting The Deductee Details Based on Pan: {}", response);
		ApiStatus<Map<String, Object>> apiStatus = new ApiStatus<Map<String, Object>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", response);
		return new ResponseEntity<ApiStatus<Map<String, Object>>>(apiStatus, HttpStatus.OK);
	} 

	/**
	 * 
	 * @param deductorTan
	 * @param pan
	 * @return
	 */
	@GetMapping("/getDeducteeBasedOnPanAndName")
	public ResponseEntity<ApiStatus<DeducteeMasterNonResidentialDTO>> getDeducteeBasedOnPanAndName(
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
			@RequestParam(name = "deducteePan", required = false) String deducteePan,
			@RequestParam(name = "deducteeName", required = false) String deducteeName) {
		if (deductorPan == null) {
			throw new CustomException("Invalid PAN Number is should not be null", HttpStatus.BAD_REQUEST);
		}
		DeducteeMasterNonResidentialDTO deducteeMasterNonResidential = deducteeMasterService
				.getDeducteeBasedOnPanAndName(deductorPan, deducteePan, deducteeName);
		logger.info("Getting The Deductee Details Based on Pan: {}", deducteeMasterNonResidential);
		ApiStatus<DeducteeMasterNonResidentialDTO> apiStatus = new ApiStatus<DeducteeMasterNonResidentialDTO>(
				HttpStatus.OK, "SUCCESS", "GET DEDUCTEE DETAILS BASED ON PAN/NAME", deducteeMasterNonResidential);
		return new ResponseEntity<ApiStatus<DeducteeMasterNonResidentialDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param year
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/deductee/resident", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DeducteeMasterResidential>> getDeducteeMasterResidential(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		DeducteeMasterResidential result = deducteeMasterService.getDeducteeMasterResidential(deductorPan,
				CommonUtil.getAssessmentYear(assessmentYear), tenantId);
		logger.info("REST response to get a Deductee Resident Record : {}", result);
		ApiStatus<DeducteeMasterResidential> apiStatus = new ApiStatus<DeducteeMasterResidential>(HttpStatus.OK,
				"SUCCESS", "TO GET A DEDUCTEE RESIDENT RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<DeducteeMasterResidential>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param year
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/deductee/pan/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> getDeducteePanStatus(@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestParam(value = "assessmentMonth", required = true) Integer assessmentMonth) {
		String result = deducteeMasterService.getDeducteePanStatus(deductorPan, assessmentYear, assessmentMonth);
		logger.info("REST response to get a Deductee Resident Record : {}", result);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"TO GET A DEDUCTEE RESIDENT RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * fiegn call to get the count of pans based on status
	 * 
	 * @param panStatus
	 * @param deductorPan
	 * @return
	 */
	@GetMapping(value = "/pan/count")
	public ResponseEntity<ApiStatus<Long>> getStatusCount(@RequestParam("status") String panStatus,
			@RequestParam("Pan") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		Long count = deducteeMasterResidentialDAO.countPanBasedOnStatus(panStatus, deductorPan);
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.OK, "SUCCESS", "RETRIEVED COUNT SUCCESSFULLY ",
				count);
		return new ResponseEntity<ApiStatus<Long>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * fiegn call to get list of deductee pans based on deductor pan
	 * 
	 * @param deductorPan
	 * @return
	 */
	@GetMapping(value = "/deducteePans")
	public ResponseEntity<ApiStatus<List<DeducteeMasterResidential>>> getListOfPans(
			@RequestParam("pan") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("fiegn call executing to get list of deductee pans  {}");
		List<DeducteeMasterResidential> list = deducteeMasterResidentialDAO.findAllByDeductorPan(deductorPan);

		ApiStatus<List<DeducteeMasterResidential>> apiStatus = new ApiStatus<List<DeducteeMasterResidential>>(
				HttpStatus.OK, "SUCCESS", "RETRIEVED COUNT SUCCESSFULLY ", list);
		logger.info("List of deductee pans are    {}" + list);
		return new ResponseEntity<ApiStatus<List<DeducteeMasterResidential>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	@GetMapping(value = "nonResident/deducteePans")
	public ResponseEntity<ApiStatus<List<DeducteeMasterNonResidential>>> getListOfNonResidentPans(
			@RequestParam("pan") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId) {
		logger.info("fiegn call executing to get list of deductee pans  {}");
		List<DeducteeMasterNonResidential> list = deducteeMasterNonResidentialDAO.findAllByDeductorPan(deductorPan);

		ApiStatus<List<DeducteeMasterNonResidential>> apiStatus = new ApiStatus<List<DeducteeMasterNonResidential>>(
				HttpStatus.OK, "SUCCESS", "RETRIEVED COUNT SUCCESSFULLY ", list);
		logger.info("List of deductee pans are    {}" + list);
		return new ResponseEntity<ApiStatus<List<DeducteeMasterNonResidential>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * This API for download pan validation report
	 * 
	 * @param deductorPan
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/deductee/panvalidation/report")
	public ResponseEntity<ApiStatus<String>> deducteePANValidationReport(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "DEDUCTOR-PAN") String pan)
			throws Exception {
		deducteeMasterService.asyncDeducteePANValidationReport(pan, tenantId, tan, userName);
		String message = "Deductee pan validation report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * This API for download non resident pan validation report
	 * 
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param pan
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/deductee/nonresident/panvalidation/report")
	public ResponseEntity<ApiStatus<String>> deducteeNonResidentPANValidationReport(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "DEDUCTOR-PAN") String pan)
			throws Exception {
		deducteeMasterService.asyncNRDeducteePANValidationReport(pan, tenantId, tan, userName);
		String message = "Deductee nr pan validation report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * fiegn call to get list of deductee master non residential
	 * 
	 * @param deductorPan
	 * @return
	 */
	@GetMapping(value = "/deductee/pan/code/name")
	public ResponseEntity<ApiStatus<List<DeducteeMasterNonResidential>>> findAllByDeducteePanModifiedName(
			@RequestParam String deductorPan, @RequestParam String modifiedName,
			@RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("fiegn call executing to get list of deductee pans  {}");
		List<DeducteeMasterNonResidential> list = deducteeMasterService.findAllByDeducteePanModifiedName(deductorPan,
				modifiedName);
		ApiStatus<List<DeducteeMasterNonResidential>> apiStatus = new ApiStatus<List<DeducteeMasterNonResidential>>(
				HttpStatus.OK, "SUCCESS", "RETRIEVED COUNT SUCCESSFULLY ", list);
		logger.info("List of deductee pans are    {}" + list);
		return new ResponseEntity<ApiStatus<List<DeducteeMasterNonResidential>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tenantId
	 * @param deducteePan
	 * @return
	 */
	@GetMapping(value = "/deductee/emails", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<DeducteeMasterResidential>>> getDeducteeEmails(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam("deducteePan") String deducteePan) {
		MultiTenantContext.setTenantId(tenantId);
		ApiStatus<List<DeducteeMasterResidential>> apiStatus = new ApiStatus<List<DeducteeMasterResidential>>(
				HttpStatus.OK, "SUCCESS", "NO ALERT",
				deducteeMasterService.getDeducteeEmails(deductorPan, deducteePan));
		return new ResponseEntity<ApiStatus<List<DeducteeMasterResidential>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param deductorPan
	 * @param tenantId
	 * @param deducteePan
	 * @return
	 */
	@GetMapping(value = "/non-residnet/deductee/emails", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<DeducteeMasterNonResidential>>> getDeducteeNonResidentEmails(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam("deducteePan") String deducteePan) {
		MultiTenantContext.setTenantId(tenantId);
		ApiStatus<List<DeducteeMasterNonResidential>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				deducteeMasterService.getDeducteeNonResidentEmails(deductorPan, deducteePan));
		return new ResponseEntity<ApiStatus<List<DeducteeMasterNonResidential>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param deductorPan
	 * @param type
	 * @return
	 */
	@GetMapping(value = "/deductee/count/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Map<String, Integer>>> getActiveAndInactiveDeducteeCount(
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@PathVariable(value = "type", required = true) String type) {
		ApiStatus<Map<String, Integer>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", " ",
				deducteeMasterService.getActiveAndInactiveDeducteeCounts(deductorPan, type));
		return new ResponseEntity<ApiStatus<Map<String, Integer>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method for get kyc details based on deductee pan
	 * 
	 * @param deductorPan
	 * @param tenantId
	 * @param deducteePan
	 * @return
	 */
	@GetMapping(value = "/deductee/kyc", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<KYCDetails>> getKYCDetailsBasedOnPan(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam("deducteePan") String deducteePan, @RequestHeader(value = "TAN-NUMBER") String tan) {
		MultiTenantContext.setTenantId(tenantId);
		KYCDetails kycDetails = deducteeMasterService.getKycDetailsBasedOnPan(deductorPan, deducteePan, tan);
		ApiStatus<KYCDetails> apiStatus = new ApiStatus<KYCDetails>(HttpStatus.OK, "SUCCESS",
				"GET KYC DETAILS SUCCESSFULLY", kycDetails);
		return new ResponseEntity<ApiStatus<KYCDetails>>(apiStatus, HttpStatus.OK);
	}

	
	/**
	 * This API for download ldc validation report
	 * 
	 * @param deductorPan
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/ldc/validation/report")
	public ResponseEntity<ApiStatus<String>> ldcValidationReport(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "DEDUCTOR-PAN") String pan)
			throws Exception {
		deducteeMasterService.asyncLdcValidationReport(pan, tenantId, tan, userName);
		String message = "Ldc validation report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}
	
	@PostMapping(value = "/deductee/nr/stagging", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<BatchUpload>> generateDeducteeNrStaggingFile(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader(value = "DEDUCTOR-PAN") String pan, @RequestBody Map<String, Object> invoiceObj)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		Integer year = (Integer) invoiceObj.get("year");
		Integer month = (Integer) invoiceObj.get("month");
		year = CommonUtil.getAssessmentYear(year);
		month = CommonUtil.getAssessmentMonth(month);
		BatchUpload batchUpload = deducteeMasterService.generateDeducteeNrStaggingFile(tan, pan, tenantId, year, month);

		ApiStatus<BatchUpload> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"Generating deductee stagging file.", batchUpload);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}
	
	@GetMapping(value = "/deducteesName", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<CustomDeducteesDTO>>> getListOfResidentAndNonResidentDeductees(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan) {
		List<CustomDeducteesDTO> residentDeductees = deducteeMasterService.getListOfOnlyResidentDeductees(deductorPan);
		ApiStatus<List<CustomDeducteesDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"LIST OF RESIDENT DEDUCTEES", residentDeductees);
		return new ResponseEntity<ApiStatus<List<CustomDeducteesDTO>>>(apiStatus, HttpStatus.OK);
	}
	
	// Feign client
	@GetMapping(value = "/nr/deductees", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<DeducteeMasterNonResidential>>> getNrDeductees(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId) {
		List<DeducteeMasterNonResidential> residentDeductees = deducteeMasterService.getNRDeductees(deductorPan);
		ApiStatus<List<DeducteeMasterNonResidential>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				residentDeductees);
		return new ResponseEntity<ApiStatus<List<DeducteeMasterNonResidential>>>(apiStatus, HttpStatus.OK);
	}
}