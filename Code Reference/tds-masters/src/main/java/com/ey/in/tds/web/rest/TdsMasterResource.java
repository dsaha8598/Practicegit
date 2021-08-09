package com.ey.in.tds.web.rest;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.AppMastersApplication;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.TdsMaster;
import com.ey.in.tds.core.errors.FieldValidator;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.AppUtils;
import com.ey.in.tds.dto.TdsMasterDTO;
import com.ey.in.tds.service.TdsMasterService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.micrometer.core.annotation.Timed;

/**
 * REST controller for managing TdsMaster.
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters")
public class TdsMasterResource extends BaseResource {

	private final TdsMasterService tdsMasterService;

	public TdsMasterResource(TdsMasterService tdsMasterService) {
		this.tdsMasterService = tdsMasterService;
	}

	/**
	 * This method is used to create new TDS Rate Master record
	 * 
	 * @param tdsMasterDTO
	 * @return
	 * @throws RecordNotFoundException
	 * @throws JsonProcessingException
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 * @throws URISyntaxException
	 */
	@PostMapping(value = "/tds", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Long>> createTdsMaster(@Valid @RequestBody TdsMasterDTO tdsMasterDTO,
			@RequestHeader("X-USER-EMAIL") String userName) throws RecordNotFoundException, JsonProcessingException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (tdsMasterDTO.getId() != null) {
			throw new CustomException("A new Tds Master cannot have a id already", HttpStatus.BAD_REQUEST);
		}

		if (tdsMasterDTO.getApplicableFrom() == null) {
			throw new CustomException("Applicable From should not null", HttpStatus.BAD_REQUEST);
		}
		
		//
		//if (tdsMasterDTO.getPerTransactionLimit() != null && tdsMasterDTO.getAnnualTransactionLimit() != null
			//	&& tdsMasterDTO.getPerTransactionLimit() > 0 && tdsMasterDTO.getAnnualTransactionLimit() > 0
				//&& tdsMasterDTO.getPerTransactionLimit() >= tdsMasterDTO.getAnnualTransactionLimit()) {
		//	throw new CustomException(
			//		"Per Transaction limit should not be greater than or equals to Annual transaction limit");
		//}

		if (tdsMasterDTO.getApplicableTo() != null
				&& (tdsMasterDTO.getApplicableFrom().equals(tdsMasterDTO.getApplicableTo())
						|| tdsMasterDTO.getApplicableFrom().isAfter(tdsMasterDTO.getApplicableTo()))) {
			throw new CustomException("From Date should not be equals or greater than To Date", HttpStatus.BAD_REQUEST);
		}
		//ESAPI Validating user input
		MasterESAPIValidation.tdsRateInputValidation(tdsMasterDTO);
		Long result = tdsMasterService.createTdsMaster(tdsMasterDTO, userName);
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.CREATED, "To create a Tds Master  Record",
				"Tds Master Record Created ", result);
		return new ResponseEntity<ApiStatus<Long>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method is used to get all TDS Rate Master record
	 * 
	 * @param pageable
	 * @return
	 * @throws RecordNotFoundException
	 */
	@GetMapping(value = "/tds", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<TdsMasterDTO>>> getAllTdsMasters() {
		MultiTenantContext.setTenantId("master");
		List<TdsMasterDTO> listTdsDTO = tdsMasterService.findAll();
		info("REST respone to get list of TDSMaster : {}", listTdsDTO);
		ApiStatus<List<TdsMasterDTO>> apiStatus = new ApiStatus<List<TdsMasterDTO>>(HttpStatus.OK,
				"To get List of Tds Master Records", "List of Tds Master Records", listTdsDTO);
		return new ResponseEntity<ApiStatus<List<TdsMasterDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * This method is used to get TDS Rate Master record based on id
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 * @throws FieldValidator
	 */
	@GetMapping(value = "/tds/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TdsMasterDTO>> getTdsMaster(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		TdsMasterDTO tdsMaster = null;
		if (id == null) {
			throw new CustomException("Id should not be null, Please enter the valid id", HttpStatus.BAD_REQUEST);
		} else {
			tdsMaster = tdsMasterService.findOne(id);
		}
		ApiStatus<TdsMasterDTO> apiStatus = new ApiStatus<TdsMasterDTO>(HttpStatus.OK, "To get a Tds Master Record",
				"Single Tds Master ", tdsMaster);
		return new ResponseEntity<ApiStatus<TdsMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method is used to update TDS Rate Master record
	 * 
	 * @param tdsMaster
	 * @return
	 * @throws RecordNotFoundException
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 * @throws FieldValidator
	 */
	@PutMapping(value = "/tds", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TdsMaster>> updateTdsMaster(@Valid @RequestBody TdsMasterDTO tdsMasterDTO,
			@RequestHeader("X-USER-EMAIL") String userName) throws RecordNotFoundException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (tdsMasterDTO == null) {
			throw new CustomException("Please the form data", HttpStatus.BAD_REQUEST);
		}

		if (tdsMasterDTO.getApplicableTo() != null
				&& (tdsMasterDTO.getApplicableFrom().equals(tdsMasterDTO.getApplicableTo())
						|| tdsMasterDTO.getApplicableFrom().isAfter(tdsMasterDTO.getApplicableTo()))) {
			throw new CustomException("From Date should not be equals or greater than To Date", HttpStatus.BAD_REQUEST);
		}
		//ESAPI Validating user input
		MasterESAPIValidation.tdsRateInputValidation(tdsMasterDTO);
		TdsMaster tdsMasterResponse = tdsMasterService.updateTdsMaster(tdsMasterDTO, userName);
		ApiStatus<TdsMaster> apiStatus = new ApiStatus<TdsMaster>(HttpStatus.CREATED, "To update a Tds Master Record",
				"Tds Master Record update", tdsMasterResponse);
		return new ResponseEntity<ApiStatus<TdsMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method is used to delete TDS Rate Master record
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 * @throws FieldValidator
	 */
	@DeleteMapping(value = "/tds/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Long>> deleteTdsMaster(@PathVariable Long id) throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		if (id == null) {
			throw new CustomException("Id should not be null, Please enter the valid id", HttpStatus.BAD_REQUEST);
		} else {
			tdsMasterService.delete(id);
		}
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.OK, "To delete a Single Tds Master Record",
				"Tds Master Record Deleted", id);
		return new ResponseEntity<ApiStatus<Long>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * API to get TDS Rate Master record based on section
	 * 
	 * @param section
	 * @param residentialStatus
	 * @return
	 */
	@GetMapping(value = "/tds/bysection", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<TdsMaster>>> getTdsMasterBySection(@RequestParam("section") String section,
			@RequestParam("nopId") int nopId, @RequestParam("residentialStatus") String residentialStatus,
			@RequestParam("status") String status) {
		MultiTenantContext.setTenantId("master");
		List<TdsMaster> tdsMaster = tdsMasterService.getTdsMasterBySection(section, nopId, residentialStatus, status);
		ApiStatus<List<TdsMaster>> apiStatus = new ApiStatus<List<TdsMaster>>(HttpStatus.OK,
				"To get a Tds Master Record", "Single Tds Master ", tdsMaster);
		return new ResponseEntity<ApiStatus<List<TdsMaster>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param section
	 * @return
	 */
	@GetMapping(value = "/residentialStatus/by/section", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> getTdsResidentialStatusBySection(@RequestParam("section") String section) {
		MultiTenantContext.setTenantId("master");
		String residentialStatus = tdsMasterService.getTdsResidentialStatusBySection(section);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK,
				"To get a Tds Master Record", "Single Tds Master ", residentialStatus);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/build-number")
	@Timed
	public ResponseEntity<Map<String, String>> buildNumber() {
		Map<String, String> response = new AppUtils().getManifestAttributes(AppMastersApplication.class, "tds-masters");
		return new ResponseEntity<Map<String, String>>(response, HttpStatus.OK);
	}
	
	/**
	 * This API for feign client
	 * 
	 * @param section
	 * @return
	 */
	@GetMapping(value = "/tds/nature", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Optional<TdsMaster>>> getTdsRateBasedOnNatureId(
			@RequestParam("natureId") Long natureId) {
		MultiTenantContext.setTenantId("master");
		Optional<TdsMaster> tdsMasterObject = tdsMasterService.getTdsRateBasedOnNatureId(natureId);
		ApiStatus<Optional<TdsMaster>> apiStatus = new ApiStatus<Optional<TdsMaster>>(HttpStatus.OK,
				"To get a Tds Master Record", "Single Tds Master ", tdsMasterObject);
		return new ResponseEntity<ApiStatus<Optional<TdsMaster>>>(apiStatus, HttpStatus.OK);
	}
	
	// Feign client
	@GetMapping(value = "/residentialStatus/for/all/sections", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Map<String, String>>> getResidentialStatusForSections() {
		MultiTenantContext.setTenantId("master");
		Map<String, String> residentialStatus = tdsMasterService.getResidentialStatusForSections();
		ApiStatus<Map<String, String>> apiStatus = new ApiStatus<>(HttpStatus.OK, "To get a Tds Master Record",
				"Single Tds Master ", residentialStatus);
		return new ResponseEntity<ApiStatus<Map<String, String>>>(apiStatus, HttpStatus.OK);
	}
}
