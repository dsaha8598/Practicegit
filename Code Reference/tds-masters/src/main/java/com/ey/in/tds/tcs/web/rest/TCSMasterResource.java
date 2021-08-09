package com.ey.in.tds.tcs.web.rest;


import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.ey.in.tcs.common.TCSMasterDTO;
import com.ey.in.tds.AppMastersApplication;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSRateMaster;
import com.ey.in.tds.core.errors.FieldValidator;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.AppUtils;
import com.ey.in.tds.tcs.service.TCSMasterService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.micrometer.core.annotation.Timed;

/**
 * REST controller for managing TCSMaster.
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters")
public class TCSMasterResource extends BaseResource {

	@Autowired
	private TCSMasterService tcsMasterService;


	/**
	 * This method is used to create new TCS Rate Master record
	 * 
	 * @param tcsMasterDTO
	 * @return
	 * @throws RecordNotFoundException
	 * @throws JsonProcessingException
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 * @throws URISyntaxException
	 */
	@PostMapping(value = "/tcs", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Long>> createTCSMaster(@Valid @RequestBody TCSMasterDTO tcsMasterDTO,
			@RequestHeader("X-USER-EMAIL") String userName) throws RecordNotFoundException, JsonProcessingException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (tcsMasterDTO.getId() != null) {
			throw new CustomException("A new Tcs Master cannot have a id already", HttpStatus.BAD_REQUEST);
		}

		if (tcsMasterDTO.getApplicableFrom() == null) {
			throw new CustomException("Applicable From should not null", HttpStatus.BAD_REQUEST);
		}

		if (tcsMasterDTO.getApplicableTo() != null
				&& (tcsMasterDTO.getApplicableFrom().equals(tcsMasterDTO.getApplicableTo())
						|| tcsMasterDTO.getApplicableFrom().isAfter(tcsMasterDTO.getApplicableTo()))) {
			throw new CustomException("From Date should not be equals or greater than To Date", HttpStatus.BAD_REQUEST);
		}
		//ESAPI Validating user input
		MasterESAPIValidation.tcsRateInputValidation(tcsMasterDTO);
		Long result = tcsMasterService.createTCSMaster(tcsMasterDTO, userName);
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.CREATED, "To create a Tcs Master  Record",
				"Tcs Master Record Created ", result);
		return new ResponseEntity<ApiStatus<Long>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method is used to get all TCS Rate Master record
	 * 
	 * @param pageable
	 * @return
	 * @throws RecordNotFoundException
	 */
	@GetMapping(value = "/tcs", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<TCSMasterDTO>>> getAllTCSMasters() {
		MultiTenantContext.setTenantId("master");
		List<TCSMasterDTO> listTdsDTO = tcsMasterService.findAll();
		info("REST respone to get list of TDSMaster : {}", listTdsDTO);
		ApiStatus<List<TCSMasterDTO>> apiStatus = new ApiStatus<List<TCSMasterDTO>>(HttpStatus.OK,
				"To get List of Tcs Master Records", "List of Tcs Master Records", listTdsDTO);
		return new ResponseEntity<ApiStatus<List<TCSMasterDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * This method is used to get TCS Rate Master record based on id
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 * @throws FieldValidator
	 */
	@GetMapping(value = "/tcs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSMasterDTO>> getTCSMaster(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		TCSMasterDTO tcsMaster = null;
		if (id == null) {
			throw new CustomException("Id should not be null, Please enter the valid id", HttpStatus.BAD_REQUEST);
		} else {
			tcsMaster = tcsMasterService.findOne(id);
		}
		ApiStatus<TCSMasterDTO> apiStatus = new ApiStatus<TCSMasterDTO>(HttpStatus.OK, "To get a Tcs Master Record",
				"Single Tcs Master ", tcsMaster);
		return new ResponseEntity<ApiStatus<TCSMasterDTO>>(apiStatus, HttpStatus.OK);
	}
	
	@GetMapping(value = "/tcs/rate-master/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSMasterDTO>> getRateMasterByNoiId(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		TCSMasterDTO tcsMaster = null;
		if (id == null) {
			throw new CustomException("Id should not be null, Please enter the valid id", HttpStatus.BAD_REQUEST);
		} else {
			tcsMaster = tcsMasterService.finByNoiId(id);
		}
		ApiStatus<TCSMasterDTO> apiStatus = new ApiStatus<TCSMasterDTO>(HttpStatus.OK, "To get a Tcs Master Record",
				"Single Tcs Master ", tcsMaster);
		return new ResponseEntity<ApiStatus<TCSMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method is used to update TCS Rate Master record
	 * 
	 * @param tcsMaster
	 * @return
	 * @throws RecordNotFoundException
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 * @throws FieldValidator
	 */
	@PutMapping(value = "/tcs", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSRateMaster>> updateTCSMaster(@Valid @RequestBody TCSMasterDTO tcsMasterDTO,
			@RequestHeader("X-USER-EMAIL") String userName) throws RecordNotFoundException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (tcsMasterDTO == null) {
			throw new CustomException("Please the form data", HttpStatus.BAD_REQUEST);
		}

		if (tcsMasterDTO.getApplicableTo() != null
				&& (tcsMasterDTO.getApplicableFrom().equals(tcsMasterDTO.getApplicableTo())
						|| tcsMasterDTO.getApplicableFrom().isAfter(tcsMasterDTO.getApplicableTo()))) {
			throw new CustomException("From Date should not be equals or greater than To Date", HttpStatus.BAD_REQUEST);
		}
		//ESAPI Validating user input
		MasterESAPIValidation.tcsRateInputValidation(tcsMasterDTO);
		TCSRateMaster tcsMasterResponse = tcsMasterService.updateTCSMaster(tcsMasterDTO, userName);
		ApiStatus<TCSRateMaster> apiStatus = new ApiStatus<TCSRateMaster>(HttpStatus.CREATED, "To update a Tcs Master Record",
				"Tcs Master Record update", tcsMasterResponse);
		return new ResponseEntity<ApiStatus<TCSRateMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method is used to delete TCS Rate Master record
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 * @throws FieldValidator
	 */
	@DeleteMapping(value = "/tcs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Long>> deleteTCSMaster(@PathVariable Long id) throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		if (id == null) {
			throw new CustomException("Id should not be null, Please enter the valid id", HttpStatus.BAD_REQUEST);
		} else {
			tcsMasterService.delete(id);
		}
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.OK, "To delete a Single Tcs Master Record",
				"Tcs Master Record Deleted", id);
		return new ResponseEntity<ApiStatus<Long>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * API to get TCS Rate Master record based on section
	 * 
	 * @param section
	 * @param residentialStatus
	 * @return
	 */
	@GetMapping(value = "/tcs/bysection", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<TCSRateMaster>>> getTCSMasterBySection(@RequestParam("section") String section,
			@RequestParam("residentialStatus") String residentialStatus, @RequestParam("status") String status) {
		MultiTenantContext.setTenantId("master");
		List<TCSRateMaster> tcsMaster = tcsMasterService.getTCSMasterBySection(section, residentialStatus, status);
		ApiStatus<List<TCSRateMaster>> apiStatus = new ApiStatus<List<TCSRateMaster>>(HttpStatus.OK,
				"To get a Tcs Master Record", "Single Tcs Master ", tcsMaster);
		return new ResponseEntity<ApiStatus<List<TCSRateMaster>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/tcs/build-number")
	@Timed
	public ResponseEntity<Map<String, String>> buildNumber() {
		Map<String, String> response = new AppUtils().getManifestAttributes(AppMastersApplication.class, "tcs-masters");
		return new ResponseEntity<Map<String, String>>(response, HttpStatus.OK);
	}
}
