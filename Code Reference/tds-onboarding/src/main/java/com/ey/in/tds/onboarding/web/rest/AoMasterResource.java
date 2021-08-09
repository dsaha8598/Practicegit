package com.ey.in.tds.onboarding.web.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.ey.in.tds.common.dto.AoMasterDTO;
import com.ey.in.tds.common.ingestion.response.dto.BatchUploadResponseDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.AoMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.AoUtilization;
import com.ey.in.tds.common.security.SecurityValidations;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.onboarding.dto.ao.CustomAoLdcMasterDTO;
import com.ey.in.tds.onboarding.service.ao.AoMasterService;
import com.microsoft.azure.storage.StorageException;


@RestController
@RequestMapping("/api/onboarding")
public class AoMasterResource {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final AoMasterService aoMasterService;

	public AoMasterResource(AoMasterService aoMasterService) {
		this.aoMasterService = aoMasterService;
	}

	/**
	 * 
	 * @param aoMaster
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param userName
	 * @return
	 * @throws URISyntaxException
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	@PostMapping(value = "/aomaster", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<AoMasterDTO>> create(@RequestBody AoMasterDTO aoMaster,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestHeader(value = "USER_NAME") String userName)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);

		if (aoMaster.getApplicableFrom() == null) {
			throw new CustomException("Applicable From should not null", HttpStatus.BAD_REQUEST);
		}

		if (aoMaster.getApplicableTo() != null && (aoMaster.getApplicableFrom().equals(aoMaster.getApplicableTo())
				|| aoMaster.getApplicableFrom().after(aoMaster.getApplicableTo()))) {
			throw new CustomException("From Date should not be equals or greater than To Date", HttpStatus.BAD_REQUEST);
		}

		if (aoMaster.getAmount() != null && aoMaster.getLimitUtilised() != null
				&& aoMaster.getLimitUtilised().doubleValue() > aoMaster.getAmount().doubleValue()) {
			throw new CustomException("Limit utilised amount should not be greater than certificate amount",
					HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		SecurityValidations.aoMasterInputValidation(aoMaster);
		AoMaster response = aoMasterService.create(aoMaster, deductorTan, assesssmentYear, userName);

		// TODO copying values from dto to entity
		AoMasterDTO aoMasterDTO = aoMasterService.copyToEntity(response);

		ApiStatus<AoMasterDTO> apiStatus = new ApiStatus<AoMasterDTO>(HttpStatus.OK, "SUCCESS",
				"CREATED AO MASTER RECORD SUCCESSFULLY", aoMasterDTO);
		return new ResponseEntity<ApiStatus<AoMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	@PutMapping(value = "/aomaster", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<AoMasterDTO>> update(@RequestBody AoMasterDTO aoMasterDTO,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestHeader(value = "USER_NAME") String userName)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		if (aoMasterDTO.getApplicableTo() != null && (aoMasterDTO.getApplicableFrom().equals(aoMasterDTO.getApplicableTo())
				|| aoMasterDTO.getApplicableFrom().after(aoMasterDTO.getApplicableTo()))) {
			throw new CustomException("From Date should not be equals or greater than To Date", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		SecurityValidations.aoMasterInputValidation(aoMasterDTO);
		AoMaster response = aoMasterService.update(aoMasterDTO, deductorTan, assesssmentYear, userName);
		
		// TODO copying values from dto to entity
		AoMasterDTO aoMaster = aoMasterService.copyToEntity(response);
		ApiStatus<AoMasterDTO> apiStatus = new ApiStatus<AoMasterDTO>(HttpStatus.OK, "SUCCESS",
				"UPDATED AO MASTER RECORD SUCCESSFULLY", aoMaster);
		return new ResponseEntity<ApiStatus<AoMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * Upload multiple (PDF) Files
	 * 
	 * @param files
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 */
	//TODO NEED TO CHANGE FOR SQL
/*	@PostMapping("/ao/import/pdf")
	public ResponseEntity<ApiStatus<BatchUpload>> uploadPdf(@RequestHeader("TAN-NUMBER") String tan,
			@RequestParam("files") MultipartFile[] files,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader("USER_NAME") String userName)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		boolean validFiles = true;
		if (files != null && files.length > 0) {
			for (MultipartFile file : files) {
				String contentType = new Tika().detect(file.getInputStream());
				if (!AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
						&& !AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
					validFiles = false;
				}
			}
			if (!validFiles) {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
			}
			assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
			assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
			BatchUpload batchUpload = aoMasterService.saveToBatchUploadPdf(files, tan, assesssmentYear, assessmentMonth,
					userName);

			ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
					"IMPORTED AO MASTER FILE SUCCESSFULLY", batchUpload);
			return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			throw new IllegalArgumentException("Atleast one file must be uploaded.");
		}
	}

	/*
	 * Api to be invoked by python consumer
	 * 
	 */
	//TODO NEED TO CHANGE FOR SQL
/*	@PostMapping("/ao/batchupload/details")
	public ResponseEntity<ApiStatus<BatchUpload>> saveCsvRecords(@RequestHeader("TAN-NUMBER") String tan,
			@RequestBody AoLdcCustomDTO aoLdcCustomDTO) throws IOException {
		BatchUpload batchUpload = aoMasterService.saveCsvDataToDb(aoLdcCustomDTO, tan);
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPDATED UPLOADED AO MASTER FILE SUCCESSFULLY", batchUpload);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}  */

	/*
	 * Api to return pdf url along with ao mastervalidation excel data
	 */
	@GetMapping(value = "/ao/pdfdetails/{id}")
	public ResponseEntity<ApiStatus<CustomAoLdcMasterDTO>> getAoMasterPdfData(@PathVariable UUID id) {
		logger.info("REST  request id to get a record of AoMaster : {}", id);
		CustomAoLdcMasterDTO customAoLdcMasterDTO = aoMasterService.getAoMasterPdfData(id);
		logger.info("REST response record to get a record of AoMaster : {}", id);
		ApiStatus<CustomAoLdcMasterDTO> apiStatus = new ApiStatus<CustomAoLdcMasterDTO>(HttpStatus.OK, "SUCCESS",
				"GET A SINGLE PDF AO MASTER RECORD SUCCESSFULLY", customAoLdcMasterDTO);
		return new ResponseEntity<ApiStatus<CustomAoLdcMasterDTO>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * Upload Excel File
	 * 
	 * @param file
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 */
	//TODO NEED TO CHANGE FOR SQL
/*	@PostMapping(value = "ao/validation/upload/excel", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiStatus<BatchUpload>> uploadExcel(@RequestHeader("TAN-NUMBER") String tan,
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader("USER_NAME") String userName)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			logger.info("REST request to save BatchUpload : {}", file);
			assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
			assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
			logger.info("Entered");
			BatchUpload batchUpload = aoMasterService.saveToBatchUploadExcel(file, tan, assesssmentYear,
					assessmentMonth, userName);
			ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
					"UPLOAD EXCEL FOR AO MASTER SUCCESSFULLY", batchUpload);
			return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	} */

	@GetMapping(value = "/aomaster/{deducteeName}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<AoMasterDTO>> getSingleRecord(@PathVariable String deducteeName,
			@PathVariable Integer id, @RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear) {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		AoMasterDTO aoMaster = aoMasterService.getAoMAsterDTO(deductorTan, deducteeName, id, assesssmentYear);
		ApiStatus<AoMasterDTO> apiStatus = new ApiStatus<AoMasterDTO>(HttpStatus.OK, "SUCCESS",
				"GET A AO MASTER RECORD BASED ON DEDUCTEE SUCCESSFULLY", aoMaster);
		return new ResponseEntity<ApiStatus<AoMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/aomaster")
	public ResponseEntity<ApiStatus<List<AoMasterDTO>>> getListOfAo(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		List<AoMasterDTO> aoList = aoMasterService.getListOfAo(deductorTan);
		logger.info("REST response to get List of AoMaster Records : {}", aoList);
		ApiStatus<List<AoMasterDTO>> apiStatus = new ApiStatus<List<AoMasterDTO>>(HttpStatus.OK, "SUCCESS",
				"GET A AO MASTER RECORD BASED ON DEDUCTEE SUCCESSFULLY", aoList);
		return new ResponseEntity<ApiStatus<List<AoMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * @return
	 * @throws IOException
	 */
	@GetMapping(value = "validation/ao/exporttoexcel")
	public ResponseEntity<InputStreamResource> exportToExcels() throws IOException {
		ByteArrayInputStream in = aoMasterService.exportToExcel();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=ao.xlsx");
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
	}

	/**
	 * This API for get AOMaster details based on DeducteeName and AoMasterId.
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/getaomasterbydeducteenameaomasterid")
	public ResponseEntity<ApiStatus<AoMasterDTO>> getAoMasterBasedOnDeducteeNameAoMasterId(
			@RequestParam(value = "deducteeName") String deducteeName, @RequestParam(value = "aoMasterId") Integer id) {
		AoMasterDTO aoMaster = aoMasterService.getAoMasterBasedOnId(deducteeName, id);
		logger.info("Get the Ao master details based on deducteeName and aoMasterId: {}", aoMaster);
		ApiStatus<AoMasterDTO> apiStatus = new ApiStatus<AoMasterDTO>(HttpStatus.OK, "SUCCESS",
				"GET AOMASTER DETAILS BASED ON DEDUCTEENAME AND AOMASTERID", aoMaster);
		return new ResponseEntity<ApiStatus<AoMasterDTO>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * API for uploading AO master
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
	@PostMapping(value = "/ao/master/upload/excel")
	public ResponseEntity<ApiStatus<BatchUploadResponseDTO>> uploadExcel(@RequestParam("files") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "X-USER-EMAIL") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		BatchUploadResponseDTO batchUpload = null;
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
				batchUpload = aoMasterService.saveFileData(file, tan, assesssmentYear, assessmentMonth, userName,
						tenantId, pan);
				ApiStatus<BatchUploadResponseDTO> apiStatus = new ApiStatus<BatchUploadResponseDTO>(HttpStatus.OK, "SUCCESS", "NO ALERT",
						batchUpload);
				return new ResponseEntity<ApiStatus<BatchUploadResponseDTO>>(apiStatus, HttpStatus.OK);
			} else {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
			}
		}
	}
	
	/**
	 * This API for feign client
	 * 
	 * @param deductorTan
	 * @param lccMasterId
	 * @return
	 */
	@GetMapping("/ao-utilization")
	public ResponseEntity<ApiStatus<List<AoUtilization>>> ldcUtilization(
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestHeader Integer ldcMasterId, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		List<AoUtilization> lccUtilizationList = aoMasterService.aoUtilization(deductorTan, ldcMasterId);
		ApiStatus<List<AoUtilization>> apiStatus = new ApiStatus<List<AoUtilization>>(HttpStatus.OK, "SUCCESS",
				"GET LDC UTILIZATION RECORD SUCCESSFULLY", lccUtilizationList);
		return new ResponseEntity<ApiStatus<List<AoUtilization>>>(apiStatus, HttpStatus.OK);
	}

}
