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
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.tcs.dto.TCSSurchargeMasterDTO;
import com.ey.in.tds.tcs.service.TCSSurchargeMasterService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;

import io.micrometer.core.annotation.Timed;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters/tcs")
public class TCSSurchargeMasterResource extends BaseResource {

	@Autowired
	private TCSSurchargeMasterService tcsSurchargeMasterService;


	/**
	 * POST /tcs-surcharge-masters : Create a new tcsSurchargeMaster.
	 *
	 * @param tcsSurchargeMaster the tcsSurchargeMaster to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         tcsSurchargeMaster, or with status 400 (Bad Request) if the
	 *         tcsSurchargeMaster has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 */
	@PostMapping(path = "/surcharge", consumes = "application/json")
	@Timed
	public ResponseEntity<ApiStatus<TCSSurchargeMasterDTO>> createSurchargeMaster(
			@Valid @RequestBody TCSSurchargeMasterDTO tcsSurchargeMasterDTO, @RequestHeader("X-USER-EMAIL") String userName)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (tcsSurchargeMasterDTO.getId() != null) {
			throw new CustomException("Record already cannot have an Id ", HttpStatus.BAD_REQUEST);
		}

		if (tcsSurchargeMasterDTO.getApplicableTo() != null
				&& tcsSurchargeMasterDTO.getApplicableFrom().isAfter(tcsSurchargeMasterDTO.getApplicableTo())
				|| tcsSurchargeMasterDTO.getApplicableFrom().equals(tcsSurchargeMasterDTO.getApplicableTo())) {
			throw new CustomException("Applicable To cannot be greater than Applicable From ", HttpStatus.BAD_REQUEST);
		}

/*		if (tcsSurchargeMasterDTO.isSurchargeApplicable() && tcsSurchargeMasterDTO.getSurchargeRate() == null) {
			throw new CustomException("Surcharge Rate cannot be empty, when isSurchargeApplicable is true ",
					HttpStatus.BAD_REQUEST);
		}  */
		//ESAPI Validating user input
		MasterESAPIValidation.surchargeMasterInputValidation(tcsSurchargeMasterDTO);
		TCSSurchargeMasterDTO result = tcsSurchargeMasterService.createSurchargeMaster(tcsSurchargeMasterDTO,userName);
		ApiStatus<TCSSurchargeMasterDTO> apiStatus = new ApiStatus<TCSSurchargeMasterDTO>(HttpStatus.CREATED,
				"To create a TCS Surcharge Master Record", "Surcharge Master Record Created ", result);
		return new ResponseEntity<ApiStatus<TCSSurchargeMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /tcs-surcharge-masters : get all the tcsSurchargeMasters.
	 *
	 * @param pageable the pagination information
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         tcsSurchargeMasters in body
	 */
	@GetMapping(path = "/surcharge", produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<List<TCSSurchargeMasterDTO>>> getAllSurchargeMasters() {
		MultiTenantContext.setTenantId("master");
		List<TCSSurchargeMasterDTO> tcsSurchargeMasterListDTO = tcsSurchargeMasterService.findAll();
		info("REST respone to get list of SurchargeMasters : {}", tcsSurchargeMasterListDTO);
		ApiStatus<List<TCSSurchargeMasterDTO>> apiStatus = new ApiStatus<List<TCSSurchargeMasterDTO>>(HttpStatus.OK,
				"To get List of TCS Surcharge Master Records", "List of TCS Surcharge Master Records", tcsSurchargeMasterListDTO);
		return new ResponseEntity<ApiStatus<List<TCSSurchargeMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * PUT /tcs-surcharge-masters : Updates an existing tcsSurchargeMaster.
	 *
	 * @param tcsSurchargeMaster the tcsSurchargeMaster to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         tcsSurchargeMaster, or with status 400 (Bad Request) if the
	 *         tcsSurchargeMaster is not valid, or with status 500 (Internal Server
	 *         Error) if the tcsSurchargeMaster couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 */
	@PutMapping(value = "/surcharge", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<Long>> updateSurchargeMaster(
			@Valid @RequestBody TCSSurchargeMasterDTO tcsSurchargeMasterDTO, @RequestHeader("X-USER-EMAIL") String userName)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (tcsSurchargeMasterDTO.getId() == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}

		if (tcsSurchargeMasterDTO.getApplicableTo() != null
				&& tcsSurchargeMasterDTO.getApplicableFrom().isAfter(tcsSurchargeMasterDTO.getApplicableTo())
				|| tcsSurchargeMasterDTO.getApplicableFrom().equals(tcsSurchargeMasterDTO.getApplicableTo())) {
			throw new CustomException("Applicable To cannot be greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		//ESAPI Validating user input
		MasterESAPIValidation.surchargeMasterInputValidation(tcsSurchargeMasterDTO);
		Long result = tcsSurchargeMasterService.updateTCSSurchargeMaster(tcsSurchargeMasterDTO,userName);
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.OK, "To update a TCS Surcharge Master Record",
				"Surcharge Master Record update", result);
		return new ResponseEntity<ApiStatus<Long>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /tcs-surcharge-masters/:id : get the "id" tcsSurchargeMaster.
	 *
	 * @param id the id of the tcsSurchargeMaster to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         tcsSurchargeMaster, or with status 404 (Not Found)
	 */
	@GetMapping("/surcharge/{id}")
	@Timed
	public ResponseEntity<ApiStatus<TCSSurchargeMasterDTO>> getSurchargeMaster(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}
		TCSSurchargeMasterDTO tcsSurchargeMasterDTO = tcsSurchargeMasterService.findOne(id);
		ApiStatus<TCSSurchargeMasterDTO> apiStatus = new ApiStatus<>(HttpStatus.OK,
				"To get a TCS Surcharge Master Record", "Single TCS Surcharge Master ", tcsSurchargeMasterDTO);
		return new ResponseEntity<ApiStatus<TCSSurchargeMasterDTO>>(apiStatus, HttpStatus.OK);
	}
}
