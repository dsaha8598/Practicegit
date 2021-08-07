package com.ey.in.tds.onboarding.web.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ValidationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.owasp.esapi.errors.IntrusionException;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.Pair;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterDTO;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterNonResidentDTO;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterNonResidential;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterResidentDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderMasterResidential;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.security.SecurityValidations;
import com.ey.in.tds.common.service.ActivityTrackerService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ActivityType;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.jdbc.dao.ShareholderMasterNonResidentialDAO;
import com.ey.in.tds.onboarding.service.shareholder.ShareholderMasterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

/**
 * 
 * @author dipak
 *
 */
@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin("*")
public class ShareholderMasterResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ShareholderMasterService shareholderMasterService;

	@Autowired
	ActivityTrackerService activityTrackerService;
	
	@Autowired
	private ShareholderMasterNonResidentialDAO shareholderMasterNonResidentialDAO;

	@PostMapping(value = "/shareholder/resident")
	public @ResponseBody ResponseEntity<ApiStatus<ShareholderMasterResidential>> createShareholder(
			@RequestParam(value = "data", required = true) String data,
			@RequestParam(value = "form15GHFile", required = false) MultipartFile form15GHFile,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader(value = "X-USER-EMAIL") String userName,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException, IOException,
			InvalidKeyException, StorageException, org.owasp.esapi.errors.ValidationException {

		//MultiTenantContext.setTenantId(deductorTan);
		boolean validFiles = true;
		validFiles = validateFile(form15GHFile);

		if (!validFiles) {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}

		ShareholderMasterResidentDTO shareholderResident = new ObjectMapper().readValue(data,
				ShareholderMasterResidentDTO.class);

		// ESAPI Validating user input
		SecurityValidations.shareholderResidentialInputValidation(shareholderResident);
		ShareholderMasterResidential result = shareholderMasterService.createResidentShareHolder(shareholderResident,
				deductorPan, userName, form15GHFile);

		shareholderMasterService.updateActivityStatusResident(deductorTan, deductorPan, userName);
		ApiStatus<ShareholderMasterResidential> apiStatus = new ApiStatus<ShareholderMasterResidential>(HttpStatus.OK,
				"SUCCESS", "CREATED A Shareholder RESIDENT RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<ShareholderMasterResidential>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/shareholder/resident/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<ShareholderMasterDTO>> getResidentialShareholder(@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @PathVariable Integer id) {
		ShareholderMasterDTO result = shareholderMasterService.getResidentialShareholder(deductorPan, id);
		logger.info("REST response to get a Shareholder Resident Record : {}", result);
		ApiStatus<ShareholderMasterDTO> apiStatus = new ApiStatus<ShareholderMasterDTO>(HttpStatus.OK, "SUCCESS",
				"TO GET A SHAREHOLDER RESIDENT RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<ShareholderMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/shareholder/resident/all/{shareholderName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<ShareholderMasterDTO>>> getResidentialShareholderByName(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestBody Pagination pagination,
			@PathVariable String shareholderName) {
		logger.info("Tenenat Id ", MultiTenantContext.getTenantId());
		ApiStatus<CommonDTO<ShareholderMasterDTO>> apiStatus = new ApiStatus<CommonDTO<ShareholderMasterDTO>>(
				HttpStatus.OK, "SUCCESS", "NO ALERT",
				shareholderMasterService.getListOfResidentialShareholder(deductorPan, pagination, shareholderName));
		return new ResponseEntity<ApiStatus<CommonDTO<ShareholderMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	@PutMapping(value = "/shareholder/resident")
	public ResponseEntity<ApiStatus<ShareholderMasterResidential>> updateResidentShareholder(
			@RequestParam(value = "data", required = true) String data,
			@RequestParam(value = "form15GHFile", required = false) MultipartFile form15GHFile,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader(value = "X-USER-EMAIL") String userName,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan)
			throws IntrusionException, ValidationException, ParseException, IOException, StorageException,
			InvalidKeyException, URISyntaxException, org.owasp.esapi.errors.ValidationException {

		boolean validFiles = true;
		validFiles = validFiles && validateFile(form15GHFile);

		if (!validFiles) {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}

		ShareholderMasterResidentDTO shareholderMasterResidentDTO = new ObjectMapper().readValue(data,
				ShareholderMasterResidentDTO.class);
		// ESAPI Validating user input
		SecurityValidations.shareholderResidentialInputValidation(shareholderMasterResidentDTO);
		ApiStatus<ShareholderMasterResidential> apiStatus = new ApiStatus<ShareholderMasterResidential>(HttpStatus.OK,
				"SUCCESS", "UPDATED A SHAREHOLDER RESIDENT RECORDS SUCCESSFULLY ", shareholderMasterService
						.updateResidentShareholder(shareholderMasterResidentDTO, deductorPan, userName, form15GHFile));
		shareholderMasterService.updateActivityStatusResident(deductorTan, deductorPan, userName);
		return new ResponseEntity<ApiStatus<ShareholderMasterResidential>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/shareholder/nonresident")
	public @ResponseBody ResponseEntity<ApiStatus<ShareholderMasterNonResidential>> createNonResidentShareholder(
			@RequestParam(value = "data", required = true) String data,
			@RequestParam(value = "trcFile", required = false) MultipartFile trcFile,
			@RequestParam(value = "tenFFile", required = false) MultipartFile tenFFile,
			@RequestParam(value = "noPEFile", required = false) MultipartFile noPEFile,
			@RequestParam(value = "noPoemFile", required = false) MultipartFile noPoemFile,
			@RequestParam(value = "mliFile", required = false) MultipartFile mliFile,
			@RequestParam(value = "beneficialOwnershipFile", required = false) MultipartFile beneficialOwnershipFile,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
			@RequestHeader(value = "X-USER-EMAIL") String userName,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan) throws InvalidKeyException,
			URISyntaxException, StorageException, IOException, IntrusionException, ValidationException, ParseException {
        //MultiTenantContext.setTenantId("client2.dvtfo.onmicrosoft.com");
		boolean validFiles = true;
		validFiles = validFiles && validateFile(trcFile);
		validFiles = validFiles && validateFile(tenFFile);
		validFiles = validFiles && validateFile(noPoemFile);
		validFiles = validFiles && validateFile(noPEFile);
		validFiles = validFiles && validateFile(mliFile);
		validFiles = validFiles && validateFile(beneficialOwnershipFile);

		if (!validFiles) {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}

		ShareholderMasterNonResidentDTO shareholderNonResident = new ObjectMapper().readValue(data,
				ShareholderMasterNonResidentDTO.class);

		logger.info("REST request to save Shareholder Non Resident :{}", shareholderNonResident);

		// ESAPI Validating user input
		SecurityValidations.shareholderNonResidentialInputValidation(shareholderNonResident);

		ShareholderMasterNonResidential result = shareholderMasterService.createNonResident(shareholderNonResident,
				trcFile, tenFFile, noPEFile, noPoemFile, mliFile, beneficialOwnershipFile, deductorPan, userName);

		shareholderMasterService.updateActivityStatusNonResident(deductorTan, deductorPan, userName);
		ApiStatus<ShareholderMasterNonResidential> apiStatus = new ApiStatus<ShareholderMasterNonResidential>(
				HttpStatus.OK, "SUCCESS", "Non Resident Shareholder added successfully", result);
		return new ResponseEntity<ApiStatus<ShareholderMasterNonResidential>>(apiStatus, HttpStatus.OK);
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

	 @PutMapping(value = "/shareholder/nonresident")
	    public ResponseEntity<ApiStatus<ShareholderMasterNonResidential>> updateNonResidentShareholder(
	            @RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
	            @RequestParam(value = "trcFile", required = false) MultipartFile trcFile,
	            @RequestParam(value = "tenFFile", required = false) MultipartFile tenFFile,
	            @RequestParam(value = "noPEFile", required = false) MultipartFile noPEFile,
	            @RequestParam(value = "noPoemFile", required = false) MultipartFile noPoemFile,
	            @RequestParam(value = "mliFile", required = false) MultipartFile mliFile,
	            @RequestParam(value = "beneficialOwnershipFile", required = false) MultipartFile beneficialOwnershipFile,
	            @RequestParam(value = "data", required = true) String data,
	            @RequestHeader(value = "X-USER-EMAIL") String userName,
	            @RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan) throws InvalidKeyException, URISyntaxException,
	            StorageException, IOException, IntrusionException, ValidationException, ParseException {

	        boolean validFiles = true;
	        validFiles = validFiles && validateFile(trcFile);
	        validFiles = validFiles && validateFile(tenFFile);
	        validFiles = validFiles && validateFile(noPoemFile);
	        validFiles = validFiles && validateFile(noPEFile);
	        validFiles = validFiles && validateFile(mliFile);
	        validFiles = validFiles && validateFile(beneficialOwnershipFile);

	        if (!validFiles) {
	            logger.warn("Unsupported file upload attempt by {}", userName);
	            throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
	        }

	        ShareholderMasterNonResidentDTO shareholderNonResident = new ObjectMapper()
	                .readValue(data, ShareholderMasterNonResidentDTO.class);

	        logger.info("REST request to save Shareholder Non Resident :{}", shareholderNonResident);

	        // ESAPI Validating user input
	        SecurityValidations.shareholderNonResidentialInputValidation(shareholderNonResident);
	        ShareholderMasterNonResidential shareholderMasterNonResidential = shareholderMasterService
	                .updateNonResident(shareholderNonResident, deductorPan, trcFile, tenFFile, noPEFile, noPoemFile, mliFile, beneficialOwnershipFile, userName);
	        logger.info(String.format("Response is: %s", shareholderMasterNonResidential));

	        updateActivityStatusNonResident(deductorTan, deductorPan, userName);
	        ApiStatus<ShareholderMasterNonResidential> apiStatus = new ApiStatus<ShareholderMasterNonResidential>(HttpStatus.OK,
	                "SUCCESS", "UPDATED NON-RESIDENT RECORD SUCCESSFULLY ", shareholderMasterNonResidential);
	        return new ResponseEntity<ApiStatus<ShareholderMasterNonResidential>>(apiStatus, HttpStatus.OK);
	    }
	@PostMapping(value = "shareholder/resident/upload/excel")
	public ResponseEntity<ApiStatus<BatchUpload>> uploadExcel(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "X-USER-EMAIL") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
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
			batchUpload = shareholderMasterService.saveFileData(file, tan, assesssmentYear, assessmentMonth, userName,
					tenantId, pan);
		}

		shareholderMasterService.updateActivityStatusResident(tan, pan, userName);
		shareholderMasterService.updateActivityStatusNonResident(tan, pan, userName);
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED SHAREHOLDER FILE SUCCESSFULLY ", batchUpload);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "shareholdernames/{shareholdertype}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Set<String>>> getShareholderNames(
			@PathVariable(value = "shareholdertype", required = true) String shareholderType,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan) {
		ApiStatus<Set<String>> apiStatus = new ApiStatus<Set<String>>(HttpStatus.OK, "SUCCESS",
				"LIST OF " + shareholderType.toUpperCase() + "SHAREHOLDERS",
				shareholderMasterService.getShareholderNames(deductorPan, shareholderType));
		return new ResponseEntity<ApiStatus<Set<String>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/shareholder/resident/pan/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> getShareholderPanStatus(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestParam(value = "assessmentMonth", required = true) Integer assessmentMonth) {
		String result = shareholderMasterService.getShareholderResidentPanStatus(deductorPan, assessmentYear,
				assessmentMonth);
		logger.info("REST response to get a Shareholder Pan status : {}", result);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"TO GET A SHAREHOLDER RESIDENT PAN STATUS SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/shareholder/nonresident/pan/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> getDeducteePanStatus(@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestParam(value = "assessmentMonth", required = true) Integer assessmentMonth) {
		String result = shareholderMasterService.getShareholderNonResidentPanStatus(deductorPan, assessmentYear,
				assessmentMonth);
		logger.info("REST response to get a Shareholder Pan status : {}", result);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"TO GET A SHAREHOLDER NON RESIDENT PAN STATUS SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}


	private void updateActivityStatusNonResident(String deductorTan, String deductorPan, String userName) {
		Integer assessmentYear = CommonUtil.getAssessmentYear(null);
		Integer assessmentMonthPlusOne = CommonUtil.getAssessmentMonthPlusOne(null);
		String activityStatus = shareholderMasterService.getShareholderNonResidentPanStatus(deductorPan, assessmentYear,
				assessmentMonthPlusOne);
		activityTrackerService.updateActivity(deductorTan, assessmentYear, assessmentMonthPlusOne, userName,
				ActivityType.PAN_VERIFICATION.getActivityType(), activityStatus);
	}

	/**
	 * This API for get Shareholder details based on Residential status.
	 *
	 * @param Residential Status( RES/NR)
	 * @return
	 */
	@GetMapping("/shareholderByResidentialStatus")
	public ResponseEntity<ApiStatus<List<Pair<String, String>>>> getShareholderBasedOnResidentialStatus(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,@RequestParam("residentialStatus")String residentialStatus) {
		List<Pair<String, String>> shareholderByResidentialStatus = shareholderMasterService
				.getShareholderByResidentialStatus(deductorPan,residentialStatus);
		logger.info("Get the Shareholder names based on deductor pan {}", deductorPan);
		ApiStatus<List<Pair<String, String>>> apiStatus = new ApiStatus<List<Pair<String, String>>>(HttpStatus.OK,
				"SUCCESS", "GET SHAREHOLDER DETAILS BASED ON RESIDENTIAL STATUS", shareholderByResidentialStatus);
		return new ResponseEntity<ApiStatus<List<Pair<String, String>>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/shareholderNonResidentByFolioNumber")
	public ResponseEntity<ApiStatus<ShareholderMasterNonResidential>> getShareholderNonResidentByFolioNumber(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam("Folio number") String foliono) {
		ShareholderMasterNonResidential shareholder = shareholderMasterService
				.getShareholderNonResidentByFolioNumber(deductorPan, tenantId, foliono);
		ApiStatus<ShareholderMasterNonResidential> apiStatus = new ApiStatus<ShareholderMasterNonResidential>(
				HttpStatus.OK, "SUCCESS", "GET SHAREHOLDER DETAILS BASED ON folio number", shareholder);
		return new ResponseEntity<ApiStatus<ShareholderMasterNonResidential>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/shareholder/nonresident/all/{shareholderName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<ShareholderMasterDTO>>> getNonResidentialShareholderByName(
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
			@RequestBody Pagination pagination, @PathVariable String shareholderName,
			@RequestHeader("X-TENANT-ID") String tenantId) {

		ApiStatus<CommonDTO<ShareholderMasterDTO>> apiStatus = new ApiStatus<CommonDTO<ShareholderMasterDTO>>(
				HttpStatus.OK, "SUCCESS", "NO ALERT", shareholderMasterService
						.getListOfNonResidentialShareholders(deductorPan, pagination, shareholderName, tenantId));
		return new ResponseEntity<ApiStatus<CommonDTO<ShareholderMasterDTO>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping(value = "/shareholder/nonresident/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<ShareholderMasterDTO>> getNonResidentialShareholder(@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan, @PathVariable Integer id) {
		ApiStatus<ShareholderMasterDTO> apiStatus = new ApiStatus<ShareholderMasterDTO>(HttpStatus.OK, "SUCCESS",
				"RETRIEVED SINGLE RECORD SUCCESSFULLY ", shareholderMasterService.getNonResidential(id));
		return new ResponseEntity<ApiStatus<ShareholderMasterDTO>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * fiegn call to get NR share holder for filings
	 * @param tenantId
	 * @param deductorPan
	 * @param id
	 * @return
	 */
	@GetMapping(value = "fiegn/shareholder/nonresident/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<ShareholderMasterNonResidential>> getNonResidentialShareholderForFilings(@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan, @PathVariable Integer id) {
		ApiStatus<ShareholderMasterNonResidential> apiStatus = new ApiStatus<ShareholderMasterNonResidential>(HttpStatus.OK, "SUCCESS",
				"RETRIEVED SINGLE RECORD SUCCESSFULLY ", shareholderMasterService.getNonResidentShareHolderById(id));
		return new ResponseEntity<ApiStatus<ShareholderMasterNonResidential>>(apiStatus, HttpStatus.OK);
	}
	
	@GetMapping(value = "/shareholders/resident")
	public ResponseEntity<ApiStatus<List<ShareholderMasterResidential>>> getResidentShareholdersByPan(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "shareHolderPan") String shareHolderPan) {
		ApiStatus<List<ShareholderMasterResidential>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				shareholderMasterService.getResidentShareholdersByPan(deductorPan, shareHolderPan));
		return new ResponseEntity<ApiStatus<List<ShareholderMasterResidential>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/shareholders/nonresident")
	public ResponseEntity<ApiStatus<List<ShareholderMasterNonResidential>>> getNonResidentShareholdersByPan(
			@PathVariable(value = "shareholdertype", required = true) String shareholderType,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan) {
		ApiStatus<List<ShareholderMasterNonResidential>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", shareholderMasterService.getNonResidentShareholdersByPan(deductorPan, shareholderType));
		return new ResponseEntity<ApiStatus<List<ShareholderMasterNonResidential>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * getting NR shareholder by id for 15CA
	 * @param tenantId
	 * @param deductorPan
	 * @param id
	 * @return
	 */
	 @GetMapping(value = "/shareholder/nonresident")
	    public ResponseEntity<ApiStatus<ShareholderMasterNonResidential>> getNonResidentialShareholderById(
	            @RequestHeader(value = "X-TENANT-ID") String tenantId,
	            @RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
	           @RequestHeader(value = "Id", required = true) Integer id) {

	        ApiStatus<ShareholderMasterNonResidential> apiStatus = new ApiStatus<ShareholderMasterNonResidential>(HttpStatus.OK, "SUCCESS",
	                "RETRIEVED SINGLE RECORD SUCCESSFULLY ",
	                shareholderMasterService.getNonResidentialById(id).get(0));
	        return new ResponseEntity<ApiStatus<ShareholderMasterNonResidential>>(apiStatus, HttpStatus.OK);
	    }
	 
	 
	 @GetMapping("/shareholder/panValidation/report")
	    public ResponseEntity<InputStreamResource> shareholderPANValidationReport(@RequestParam("pan") String deductorPan)
	            throws Exception {

	        if (deductorPan == null) {
	            throw new CustomException("No Pan Found to continue the download", HttpStatus.BAD_REQUEST);
	        }
	        Workbook workbook = shareholderMasterService.shareholderPANValidationReport(deductorPan);
	        HttpHeaders headers = new HttpHeaders();

	        ByteArrayOutputStream baout = new ByteArrayOutputStream();
	        workbook.save(baout, SaveFormat.XLSX);

	        // Get bytes and create byte array input stream  
	        byte[] bts = baout.toByteArray();
	        ByteArrayInputStream bain = new ByteArrayInputStream(bts);

	        headers.add("Content-Disposition", "attachment; filename=Shareholder_PAN_Validation_Report.xlsx");

	        logger.info("Shareholder pan validation report : {} ", bain);
	        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(bain));
	    }
	 
	 @PutMapping(value = "/batch/update/nonresident")
		public ResponseEntity<ApiStatus<Integer>> batchUpdateNonResident(
				@RequestParam(value = "list", required = true) List<ShareholderMasterNonResidential> shareholder,@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);	
		logger.info("Feign call executing to update list of share holder {}" );
		 ApiStatus<Integer> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
					"NO ALERT", shareholderMasterNonResidentialDAO.batchUpdate(shareholder));
			return new ResponseEntity<ApiStatus<Integer>>(apiStatus, HttpStatus.OK);
		}
	 
	 @GetMapping(value = "/shareholder/nonresident/feign")
	    public ResponseEntity<ApiStatus<List<ShareholderMasterNonResidential>>> getNonResidentialShareholderByIdsFeign(
	            @RequestHeader(value = "X-TENANT-ID") String tenantId,
	            @RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
	           @RequestHeader(value = "Ids", required = true) String ids) {
		 MultiTenantContext.setTenantId(tenantId);
	        ApiStatus<List<ShareholderMasterNonResidential>> apiStatus = new ApiStatus<List<ShareholderMasterNonResidential>>(HttpStatus.OK, "SUCCESS",
	                "RETRIEVED SINGLE RECORD SUCCESSFULLY ",
	                shareholderMasterNonResidentialDAO.findShareholderByIdAndPan(ids, deductorPan));
	        return new ResponseEntity<ApiStatus<List<ShareholderMasterNonResidential>>>(apiStatus, HttpStatus.OK);
	    }

}
