package com.ey.in.tds.tcs.web.rest;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.ValidationException;

import org.owasp.esapi.errors.IntrusionException;
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
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSCessTypeMaster;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.dto.CessTypeMasterDTO;
import com.ey.in.tds.tcs.service.TCSCessTypeMasterService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;

import io.micrometer.core.annotation.Timed;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters/tcs")
public class TCSCessTypeMasterResource extends BaseResource {

	private final TCSCessTypeMasterService tcsCessTypeMasterService;

	public TCSCessTypeMasterResource(TCSCessTypeMasterService tcsCessTypeMasterService) {
		this.tcsCessTypeMasterService = tcsCessTypeMasterService;

	}

	/**	
	 * POST /cess-type-masters : Create a new tcsCessTypeMaster.
	 *
	 * @param tcsCessTypeMaster the tcsCessTypeMaster to create
	 * @return the Response with status 201 (Created) and with body the new
	 *         tcsCessTypeMaster, or with status 500 (Bad Request) if the
	 *         tcsCessTypeMaster has already an I D
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 * @throws org.owasp.esapi.errors.ValidationException 
	 */
	@PostMapping(value = "/cesstype", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<TCSCessTypeMaster>> createCessTypeMaster(
			@Valid @RequestBody CessTypeMasterDTO cessTypeMasterDTO, @RequestHeader("X-USER-EMAIL") String userName,
			@RequestHeader(value = "X-TENANT-ID") String tenantId)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException,
			org.owasp.esapi.errors.ValidationException {
		MultiTenantContext.setTenantId("master");
		if (cessTypeMasterDTO.getId() != null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}

		if (cessTypeMasterDTO.getApplicableFrom() == null) {
			throw new CustomException("Applicable From cannot be Null", HttpStatus.BAD_REQUEST);
		}

		if (cessTypeMasterDTO.getCessType() == null) {
			throw new CustomException("CessType cannot be Null", HttpStatus.BAD_REQUEST);
		}

		if (cessTypeMasterDTO.getApplicableTo() != null
				&& (cessTypeMasterDTO.getApplicableFrom().isAfter(cessTypeMasterDTO.getApplicableTo())
						|| cessTypeMasterDTO.getApplicableFrom().equals(cessTypeMasterDTO.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		MasterESAPIValidation.cessTypeMasterInputValidation(cessTypeMasterDTO);
		TCSCessTypeMaster tcsCessTypeMaster = tcsCessTypeMasterService.save(cessTypeMasterDTO, userName);
		ApiStatus<TCSCessTypeMaster> apiStatus = new ApiStatus<TCSCessTypeMaster>(HttpStatus.CREATED,
				"To create a Cess Type Record", "TCS Cess Type Master Record Created ", tcsCessTypeMaster);

		return new ResponseEntity<ApiStatus<TCSCessTypeMaster>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * PUT /cess-type-masters : Updates an existing tcsCessTypeMaster.
	 *
	 * @param tcsCessTypeMaster the tcsCessTypeMaster to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         tcsCessTypeMaster, or with status 400 (Bad Request) if the
	 *         tcsCessTypeMaster is not valid, or with status 500 (Internal Server
	 *         Error) if the tcsCessTypeMaster couldn't be updated
	 * @throws URISyntaxException      if the Location URI syntax is incorrect
	 * @throws RecordNotFoundException
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 * @throws org.owasp.esapi.errors.ValidationException 
	 */
	@PutMapping(value = "/cesstype", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<TCSCessTypeMaster>> updateCessTypeMaster(
			@Valid @RequestBody CessTypeMasterDTO cessTypeMasterDTO, @RequestHeader("X-USER-EMAIL") String userName,@RequestHeader(value = "X-TENANT-ID") String tenantId)
			throws URISyntaxException, RecordNotFoundException, IntrusionException, ValidationException, ParseException, org.owasp.esapi.errors.ValidationException {
		MultiTenantContext.setTenantId("master");
		if (cessTypeMasterDTO.getId() == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}
		if (cessTypeMasterDTO.getApplicableTo() != null
				&& cessTypeMasterDTO.getApplicableFrom().isAfter(cessTypeMasterDTO.getApplicableTo())
				|| cessTypeMasterDTO.getApplicableFrom().equals(cessTypeMasterDTO.getApplicableTo())) {
			throw new CustomException("Applicable To cannot be greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		//ESAPI Validating user input
		MasterESAPIValidation.cessTypeMasterInputValidation(cessTypeMasterDTO);
		TCSCessTypeMaster tcsCessTypeMaster = tcsCessTypeMasterService.update(cessTypeMasterDTO,userName);
		ApiStatus<TCSCessTypeMaster> apiStatus = new ApiStatus<TCSCessTypeMaster>(HttpStatus.CREATED,
				"To update a Cess Type Record", "TCS Cess Type Master Record Updated ", tcsCessTypeMaster);

		return new ResponseEntity<ApiStatus<TCSCessTypeMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /cess-type-masters : get all the tcsCessTypeMasters.
	 *
	 * @param pageable the pagination information
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         tcsCessTypeMasters in body
	 */
	@GetMapping("/cesstype")
	@Timed
	public ResponseEntity<ApiStatus<List<TCSCessTypeMaster>>> getAllCessTypeMasters() {
		MultiTenantContext.setTenantId("master");
		List<TCSCessTypeMaster> listOfCessType = tcsCessTypeMasterService.findAll();
		info("REST response to get list of CessTypeMasters : {} ", listOfCessType);
		
		ApiStatus<List<TCSCessTypeMaster>> apiStatus = new ApiStatus<List<TCSCessTypeMaster>>(HttpStatus.OK,
				"To get a List of TCS Cess Type Master's", "TCS Cess Type Master List Records", listOfCessType);
		return new ResponseEntity<ApiStatus<List<TCSCessTypeMaster>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /cess-type-masters/:id : get the "id" tcsCessTypeMaster.
	 *
	 * @param id the id of the tcsCessTypeMaster to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         tcsCessTypeMaster, or with status 404 (Not Found)
	 * @throws RecordNotFoundException
	 */
	@GetMapping("/cesstype/{id}")
	@Timed
	public ResponseEntity<ApiStatus<Optional<TCSCessTypeMaster>>> getCessTypeMaster(@PathVariable Long id)
			throws RecordNotFoundException {
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}
		MultiTenantContext.setTenantId("master");
		Optional<TCSCessTypeMaster> tcsCessTypeMaster = tcsCessTypeMasterService.findOne(id);
		ApiStatus<Optional<TCSCessTypeMaster>> apiStatus = new ApiStatus<Optional<TCSCessTypeMaster>>(HttpStatus.OK,
				"To get a Single Type Cess Master", "TCS Cess Type Master Single Record", tcsCessTypeMaster);
		return new ResponseEntity<ApiStatus<Optional<TCSCessTypeMaster>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * DELETE /cess-type-masters/:id : delete the "id" tcsCessTypeMaster.
	 *
	 * @param id the id of the tcsCessTypeMaster to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/cesstype/{id}")
	@Timed
	public ResponseEntity<ApiStatus<Long>> deleteCessTypeMaster(@PathVariable Long id,@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}
		MultiTenantContext.setTenantId("master");
		tcsCessTypeMasterService.delete(id);
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.OK, "To delete a Single TCS Type Cess Master",
				"TCS Cess Type Master Record Deleted", id);
		return new ResponseEntity<ApiStatus<Long>>(apiStatus, HttpStatus.OK);
	}

}
