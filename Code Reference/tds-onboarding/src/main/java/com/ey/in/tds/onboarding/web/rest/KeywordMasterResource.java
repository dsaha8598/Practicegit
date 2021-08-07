package com.ey.in.tds.onboarding.web.rest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

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
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.KeywordMaster;
import com.ey.in.tds.common.model.keywordmaster.KeywordMasterDTO;
import com.ey.in.tds.common.security.SecurityValidations;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.onboarding.service.keyword.KeywordMasterService;

@RestController
@RequestMapping("/api/onboarding")
public class KeywordMasterResource {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private KeywordMasterService keywordMasterService;

	/**
	 * This api returns for crate KeywordMaster
	 * 
	 * @param keywordMasterDTO
	 * @param userName
	 * @param deductorPan
	 * @return
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */
	@PostMapping(value = "/keywordmaster", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<KeywordMaster>> createdKeywordMaster(@RequestBody KeywordMasterDTO keywordMasterDTO,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("DEDUCTOR-PAN") String deductorPan)
			throws IntrusionException, ValidationException, ParseException {
		if (keywordMasterDTO == null) {
			if (logger.isErrorEnabled())
				logger.error("KEYWORD MASTER DTO NOT PRESENT");
			throw new CustomException("KEYWORD MASTER DTO NOT PRESENT", HttpStatus.BAD_REQUEST);
		} else {
			if (keywordMasterDTO.getApplicableTo() != null
					&& (keywordMasterDTO.getApplicableFrom().after(keywordMasterDTO.getApplicableTo())
							|| keywordMasterDTO.getApplicableFrom().equals(keywordMasterDTO.getApplicableTo()))) {
				throw new CustomException("Applicable To cannot be greater than Applicable From",
						HttpStatus.BAD_REQUEST);
			}
			// ESAPI Validating user input
			SecurityValidations.keywordMasterInputValidation(keywordMasterDTO);
			KeywordMaster response = keywordMasterService.createdKeywordMaster(keywordMasterDTO, userName,
					deductorPan);
			
			if (logger.isInfoEnabled()) {
				logger.info("Keyword Master Response is: {}", response);
			}

			ApiStatus<KeywordMaster> apiStatus = new ApiStatus<KeywordMaster>(HttpStatus.OK, "SUCCESS",
					"CREATED KEYWORD MASTER RECORD SUCCESSFULLY", response);
			return new ResponseEntity<ApiStatus<KeywordMaster>>(apiStatus, HttpStatus.OK);
		}
	}   

	/**
	 * This api returns for update KeywordMaster
	 * 
	 * @param keywordMasterDTO
	 * @param userName
	 * @param deductorPan
	 * @return
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */
	@PutMapping(value = "/keywordmaster", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<KeywordMaster>> updateKeywordMaster(@RequestBody KeywordMasterDTO keywordMasterDTO,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("DEDUCTOR-PAN") String deductorPan)
			throws IntrusionException, ValidationException, ParseException {
		if (keywordMasterDTO == null && logger.isErrorEnabled()) {
			logger.error("KEYWORD MASTER DTO NOT PRESENT");
		}
		// ESAPI Validating user input
		SecurityValidations.keywordMasterInputValidation(keywordMasterDTO);
		KeywordMaster response = keywordMasterService.updateKeywordMaster(keywordMasterDTO, userName, deductorPan);
		
		if (logger.isInfoEnabled()) {
			logger.info("Keyword Master Response is: {}", response);
		}
		ApiStatus<KeywordMaster> apiStatus = new ApiStatus<KeywordMaster>(HttpStatus.OK, "SUCCESS",
				"UPDATE KEYWORD MASTER RECORD SUCCESSFULLY", response);
		return new ResponseEntity<ApiStatus<KeywordMaster>>(apiStatus, HttpStatus.OK);
	}   

	/**
	 * This api returns for get by id KeywordMaster
	 * 
	 * @param deductorPan
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/keywordmaster/{id}")
	public ResponseEntity<ApiStatus<KeywordMasterDTO>> keywordMasterGetById(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @PathVariable Integer id) {
		if (id == null && logger.isErrorEnabled()) {
			logger.error("KEYWORD MASTER ID NOT PRESENT");
		}
		KeywordMasterDTO keywordMasterDTO = keywordMasterService.keywordMasterGetById(deductorPan, id);
		if (logger.isInfoEnabled()) {
			logger.info("Keyword Master Response is: {}", keywordMasterDTO);
		}
		ApiStatus<KeywordMasterDTO> apiStatus = new ApiStatus<KeywordMasterDTO>(HttpStatus.OK, "SUCCESS",
				"KEYWORD MASTER GET BY ID RECORD SUCCESSFULLY", keywordMasterDTO);
		return new ResponseEntity<ApiStatus<KeywordMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns for getting all KeywordMaster
	 * @param deductorPan
	 * @return
	 */

	@GetMapping(value = "/keywordmaster/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<KeywordMasterDTO>>> keywordMasterGet(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan) {
		List<KeywordMasterDTO> keywordMasterDTO = keywordMasterService.getAllKeywordMaster(deductorPan);
		if (logger.isInfoEnabled()) {
			logger.info("Keyword Master Response is: {}", keywordMasterDTO);
		}
		ApiStatus<List<KeywordMasterDTO>> apiStatus = new ApiStatus<List<KeywordMasterDTO>>(HttpStatus.OK, "SUCCESS",
				"KEYWORD MASTER GETTING ALL RECORD SUCCESSFULLY", keywordMasterDTO);
		return new ResponseEntity<ApiStatus<List<KeywordMasterDTO>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * This API is for keywords import
	 * 
	 * @param file
	 * @param deductorPan
	 * @param userName
	 * @param tenantId
	 * @param batchUpload
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/keywords/import", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiStatus<BatchUpload>> uploadKeywordsExcel(@PathVariable("file") MultipartFile file,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan, @RequestParam(value = "batchId") Integer batchId)
			throws Exception {
		logger.info("Tenant ID {}", tenantId);
		BatchUpload response = keywordMasterService.createKeywordMaster(file, tenantId, userName, deductorPan,
				batchId);
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api is to delete keywords for a particular NOP and Section
	 * 
	 * @param id
	 * @param userName
	 * @param deductorPan
	 * @return
	 */
	@PostMapping(value = "/deleteKeywords/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<KeywordMaster>> deleteKeywords(@PathVariable("id") Integer keywordId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader("X-TENANT-ID") String tenantId) {
		KeywordMaster keywordMaster = keywordMasterService.deleteKeywords(keywordId, userName, deductorPan);
		if (logger.isInfoEnabled()) {
			logger.info("Keyword Master Response is: {}", keywordMaster);
		}
		ApiStatus<KeywordMaster> apiStatus = new ApiStatus<KeywordMaster>(HttpStatus.OK, "SUCCESS",
				"KEYWORDS DELETED SUCCESSFULLY.", keywordMaster);
		return new ResponseEntity<ApiStatus<KeywordMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API is to get nature of payments from keyword master
	 * 
	 * @param deductorPan
	 * @return
	 */
	@GetMapping(value = "/getnatureofpaymentkeywords", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<String>>> getnatureofpaymentkeywords(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId) {
		List<String> keywordMasterDTO = keywordMasterService.getnatureofpaymentkeywords(deductorPan);
		if (logger.isInfoEnabled()) {
			logger.info("Keyword Master Response is: {}", keywordMasterDTO);
		}
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				keywordMasterDTO);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * This API is for keywords export
	 * @param tan
	 * @param pan
	 * @param tenantId
	 * @param natureOfPayments
	 * @return
	 * @throws IOException
	 * @throws TanNotFoundException
	 */
	@PostMapping(value = "/existingkeywordsreport/export", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<InputStreamResource> exportRemediationReport(
			@RequestParam(value = "tan", required = true) String tan,
			@RequestParam(value = "pan", required = true) String pan,
			@RequestParam(value = "tenantId", required = true) String tenantId,
			@RequestParam(value = "natureOfPayments", required = false) String natureOfPayments)
			throws IOException {
		logger.info("TAN: {}", tan);
		MultiTenantContext.setTenantId(tenantId);
		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		MultipartFile file = keywordMasterService.exportExistingNOPKeywords(tenantId, pan, natureOfPayments);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=" + file.getOriginalFilename() + ".xlsx");
		logger.info("Existing NOP keywords report export done ");
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(file.getInputStream()));
	}

}
