package com.ey.in.tds.tcs.web.rest;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;

import javax.validation.Valid;

import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
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
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSCessMaster;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.tcs.dto.TCSCessMasterDTO;
import com.ey.in.tds.tcs.service.TCSCessMasterService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;

import io.micrometer.core.annotation.Timed;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters/tcs")
public class TCSCessMasterResource extends BaseResource {

	@Autowired
	private final TCSCessMasterService tcsCessMasterService;

	public TCSCessMasterResource(TCSCessMasterService tcsCessMasterService) {
		this.tcsCessMasterService = tcsCessMasterService;
	}

	/**
	 * POST /cess-masters : Create a new tcsCessMaster.
	 *
	 * @param cessMasterDTO the tcsCessMaster to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         cessMaster
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 */
	@PostMapping(value = "/cess", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<Long>> createCessMaster(@Valid @RequestBody TCSCessMasterDTO cessMasterDTO,
			@RequestHeader("X-USER-EMAIL") String userName) throws URISyntaxException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (cessMasterDTO.getId() != null) {
			throw new CustomException("cess master should not have ID", HttpStatus.BAD_REQUEST);
		}

		if (cessMasterDTO.getApplicableTo() != null
				&& (cessMasterDTO.getApplicableFrom().equals(cessMasterDTO.getApplicableTo())
						|| cessMasterDTO.getApplicableFrom().isAfter(cessMasterDTO.getApplicableTo()))) {
			throw new CustomException("From date cannot be equal to or greater than To Date");
		}
		//ESAPI Validating user input
		MasterESAPIValidation.cessMasterInputValidation(cessMasterDTO);
		Long result = tcsCessMasterService.saveTCSCessMaster(cessMasterDTO,userName);
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.CREATED, "To create a Tcs Cess Master",
				"Tcs Cess Master Created", result);
		return new ResponseEntity<ApiStatus<Long>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /cess-masters : get all the cessMasters.
	 *
	 * @param pageable the pagination information
	 * @return the ResponseEntity with status 200 (OK) and the list of cessMasters
	 *         in body
	 */
	@GetMapping(value = "/cess", produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<List<TCSCessMasterDTO>>> getAllCessMasters() {
		MultiTenantContext.setTenantId("master");
		List<TCSCessMasterDTO> cessMasterDTO = tcsCessMasterService.findAll();
		info("REST response to get a page of SurchargeMasters : {} ", cessMasterDTO);
		ApiStatus<List<TCSCessMasterDTO>> apiStatus = new ApiStatus<List<TCSCessMasterDTO>>(HttpStatus.OK,
				"To get a List of Tcs Cess Master's", "Tcs Cess Master List Records", cessMasterDTO);
		return new ResponseEntity<ApiStatus<List<TCSCessMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /cess-masters/:id : get the "id" tcsCessMaster.
	 *
	 * @param id the id of the tcsCessMaster to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the cessMaster,
	 *         or with status 404 (Not Found)
	 * @throws RecordNotFoundException
	 */
	@GetMapping(value = "/cess/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<TCSCessMasterDTO>> getCessMaster(@PathVariable Long id)
			throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}
		TCSCessMasterDTO tcsCessMaster = tcsCessMasterService.findOne(id);
		ApiStatus<TCSCessMasterDTO> apiStatus = new ApiStatus<TCSCessMasterDTO>(HttpStatus.OK, "To get a Single Tcs Cess Master",
				"Tcs Cess Master Single Record", tcsCessMaster);
		return new ResponseEntity<ApiStatus<TCSCessMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * PUT /cess-masters : Updates an existing tcsCessMaster.
	 *
	 * @param tcsCessMaster the tcsCessMaster to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         cessMaster, or with status 500 (Internal Server Error) if the
	 *         tcsCessMaster couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 */
	@PutMapping(value = "/cess", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<TCSCessMaster>> updateCessMaster(@Valid @RequestBody TCSCessMasterDTO cessMasterDTO,
			@RequestHeader("X-USER-EMAIL") String userName) throws URISyntaxException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (cessMasterDTO.getId() == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}
		if (cessMasterDTO.getApplicableTo() != null
				&& (cessMasterDTO.getApplicableFrom().equals(cessMasterDTO.getApplicableTo())
						|| cessMasterDTO.getApplicableFrom().isAfter(cessMasterDTO.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		//ESAPI Validating user input
		MasterESAPIValidation.cessMasterInputValidation(cessMasterDTO);
		TCSCessMaster result = tcsCessMasterService.updateTCSCessMaster(cessMasterDTO,userName);
		ApiStatus<TCSCessMaster> apiStatus = new ApiStatus<TCSCessMaster>(HttpStatus.CREATED,
				"To update a Single Tcs Cess Master", "Tcs Cess Master Updated", result);
		return new ResponseEntity<ApiStatus<TCSCessMaster>>(apiStatus, HttpStatus.OK);
	}
}
