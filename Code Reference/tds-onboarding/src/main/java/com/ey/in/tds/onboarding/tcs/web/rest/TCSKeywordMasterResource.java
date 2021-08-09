package com.ey.in.tds.onboarding.tcs.web.rest;

import java.text.ParseException;
import java.util.List;

import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSKeywordMaster;
import com.ey.in.tds.common.domain.transactions.jdbc.tcs.dto.TCSKeywordMasterDTO;
import com.ey.in.tds.common.security.SecurityValidations;
import com.ey.in.tds.common.tcs.service.TCSKeywordMasterService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;

@RestController
@RequestMapping("/api/onboarding/tcs")
public class TCSKeywordMasterResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TCSKeywordMasterService tcsKeywordMasterService;

	/**
	 * This api returns for crate KeywordMaster
	 * 
	 * @param tcsKeywordMasterDTO
	 * @param userName
	 * @param deductorPan
	 * @return
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */
	@PostMapping(value = "/keywordmaster", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSKeywordMaster>> createdKeywordMaster(
			@RequestBody TCSKeywordMasterDTO tcsKeywordMasterDTO, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId)
			throws IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		if (tcsKeywordMasterDTO == null) {
			if (logger.isErrorEnabled())
				logger.error("KEYWORD MASTER DTO NOT PRESENT");
			throw new CustomException("KEYWORD MASTER DTO NOT PRESENT", HttpStatus.BAD_REQUEST);
		} else {
			if (tcsKeywordMasterDTO.getApplicableTo() != null
					&& (tcsKeywordMasterDTO.getApplicableFrom().after(tcsKeywordMasterDTO.getApplicableTo())
							|| tcsKeywordMasterDTO.getApplicableFrom().equals(tcsKeywordMasterDTO.getApplicableTo()))) {
				throw new CustomException("Applicable To cannot be greater than Applicable From",
						HttpStatus.BAD_REQUEST);
			}
			// ESAPI Validating user input
			SecurityValidations.tcsKeywordMasterInputValidation(tcsKeywordMasterDTO);
			TCSKeywordMaster response = tcsKeywordMasterService.createdKeywordMaster(tcsKeywordMasterDTO, userName,
					deductorPan);

			if (logger.isInfoEnabled()) {
				logger.info("Keyword Master Response is: {}", response);
			}
			ApiStatus<TCSKeywordMaster> apiStatus = new ApiStatus<TCSKeywordMaster>(HttpStatus.OK, "SUCCESS",
					"CREATED KEYWORD MASTER RECORD SUCCESSFULLY", response);
			return new ResponseEntity<ApiStatus<TCSKeywordMaster>>(apiStatus, HttpStatus.OK);
		}
	}

	/**
	 * This api returns for update KeywordMaster
	 * 
	 * @param tcsKeywordMasterDTO
	 * @param userName
	 * @param collectorPan
	 * @return
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */
	@PutMapping(value = "/keywordmaster", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSKeywordMaster>> updateKeywordMaster(
			@RequestBody TCSKeywordMasterDTO tcsKeywordMasterDTO, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader("DEDUCTOR-PAN") String collectorPan, @RequestHeader("X-TENANT-ID") String tenantId)
			throws IntrusionException, ValidationException, ParseException {
		if (tcsKeywordMasterDTO == null && logger.isErrorEnabled()) {
			logger.error("TCS KEYWORD MASTER DTO NOT PRESENT");
		}
		// ESAPI Validating user input
		SecurityValidations.tcsKeywordMasterInputValidation(tcsKeywordMasterDTO);
		TCSKeywordMaster response = tcsKeywordMasterService.updateKeywordMaster(tcsKeywordMasterDTO, userName,
				collectorPan);
		if (logger.isInfoEnabled()) {
			logger.info("Keyword Master Response is: {}", response);
		}
		ApiStatus<TCSKeywordMaster> apiStatus = new ApiStatus<TCSKeywordMaster>(HttpStatus.OK, "SUCCESS",
				"UPDATE TCS KEYWORD MASTER RECORD SUCCESSFULLY", response);
		return new ResponseEntity<ApiStatus<TCSKeywordMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns for get by id KeywordMaster
	 * 
	 * @param collectorPan
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/keywordmaster/{id}")
	public ResponseEntity<ApiStatus<TCSKeywordMasterDTO>> keywordMasterGetById(
			@RequestHeader("DEDUCTOR-PAN") String collectorPan, @RequestHeader("X-TENANT-ID") String tenantId,
			@PathVariable Integer id) {
		if (id == null && logger.isErrorEnabled()) {
			logger.error("TCS KEYWORD MASTER ID NOT PRESENT");
		}
		TCSKeywordMasterDTO keywordMasterDTO = tcsKeywordMasterService.keywordMasterGetById(collectorPan, id);
		if (logger.isInfoEnabled()) {
			logger.info("Tcs Keyword Master Response is: {}", keywordMasterDTO);
		}
		ApiStatus<TCSKeywordMasterDTO> apiStatus = new ApiStatus<TCSKeywordMasterDTO>(HttpStatus.OK, "SUCCESS",
				"TCS KEYWORD MASTER GET BY ID RECORD SUCCESSFULLY", keywordMasterDTO);
		return new ResponseEntity<ApiStatus<TCSKeywordMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns for getting all KeywordMaster
	 * 
	 * @param deductorPan
	 * @return
	 */

	@GetMapping(value = "/keywordmaster/all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<TCSKeywordMasterDTO>>> keywordMasterGet(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		List<TCSKeywordMasterDTO> keywordMasterDTO = tcsKeywordMasterService.getAllKeywordMaster(deductorPan);
		if (logger.isInfoEnabled()) {
			logger.info("tcs Keyword Master Response is: {}", keywordMasterDTO);
		}
		ApiStatus<List<TCSKeywordMasterDTO>> apiStatus = new ApiStatus<List<TCSKeywordMasterDTO>>(HttpStatus.OK,
				"SUCCESS", "TCS KEYWORD MASTER GETTING ALL RECORD SUCCESSFULLY", keywordMasterDTO);
		return new ResponseEntity<ApiStatus<List<TCSKeywordMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

}
