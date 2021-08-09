package com.ey.in.tds.tcs.web.rest;

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
import com.ey.in.tds.common.domain.tcs.TCSFineRateMaster;
import com.ey.in.tds.core.errors.FieldValidator;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.tcs.service.TCSFineRateMasterService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters/tcs")
public class TCSFineRateMasterResource extends BaseResource {

	@Autowired
	private TCSFineRateMasterService tcsFineRateMasterService;

	/**
	 * POST/fine-rate-masters - this method is used to create a new Fine Rate Record
	 * 
	 * @param tcsFineRateMaster
	 * @return
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 * @throws ApiStatus
	 */
	@PostMapping(value = "/fine-rate", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSFineRateMaster>> createFineRateMaster(
			@Valid @RequestBody TCSFineRateMaster tcsFineRateMaster, @RequestHeader("X-USER-EMAIL") String userName)
			throws IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (tcsFineRateMaster.getId() != null) {
			throw new CustomException("Record already cannot have an Id ", HttpStatus.BAD_REQUEST);
		}
		if (tcsFineRateMaster.getApplicableTo() != null
				&& (tcsFineRateMaster.getApplicableFrom().equals(tcsFineRateMaster.getApplicableTo())
						|| tcsFineRateMaster.getApplicableFrom().isAfter(tcsFineRateMaster.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be greater than Applicable From ", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		MasterESAPIValidation.tcsFineRateMasterInputValidation(tcsFineRateMaster);
		TCSFineRateMaster response = tcsFineRateMasterService.save(tcsFineRateMaster, userName);
		ApiStatus<TCSFineRateMaster> apiStatus = new ApiStatus<TCSFineRateMaster>(HttpStatus.CREATED,
				"To create a Tcs Fine Rate Master Record", "Tcs Fine Rate Master Record Created ", response);
		return new ResponseEntity<ApiStatus<TCSFineRateMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * PUT/fine-rate-masters To update the exisiting record based on id
	 * 
	 * @param fineRateMaster
	 * @return
	 * @throws RecordNotFoundException
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 * @throws FieldValidator
	 */
	@PutMapping(value = "/fine-rate", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSFineRateMaster>> updateFineRateMaster(
			@Valid @RequestBody TCSFineRateMaster fineRateMaster, @RequestHeader("X-USER-EMAIL") String userName)
			throws RecordNotFoundException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (fineRateMaster.getId() == null) {
			throw new CustomException("Id cannot be Null ", HttpStatus.BAD_REQUEST);
		}
		if (fineRateMaster.getApplicableTo() != null
				&& (fineRateMaster.getApplicableFrom().equals(fineRateMaster.getApplicableTo())
						|| fineRateMaster.getApplicableFrom().isAfter(fineRateMaster.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		MasterESAPIValidation.tcsFineRateMasterInputValidation(fineRateMaster);
		TCSFineRateMaster response = tcsFineRateMasterService.updateFineRateMaster(fineRateMaster, userName);
		ApiStatus<TCSFineRateMaster> apiStatus = new ApiStatus<TCSFineRateMaster>(HttpStatus.CREATED,
				"To update a Tcs Fine Rate Master Record", "Tcs Fine Rate Master Record Updated ", response);

		return new ResponseEntity<ApiStatus<TCSFineRateMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET/fine-rate-masters To get all record
	 * 
	 * @param pageable
	 * @return
	 */
	@GetMapping(value = "/fine-rate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<TCSFineRateMaster>>> getAllFineRateMasters() {
		MultiTenantContext.setTenantId("master");
		List<TCSFineRateMaster> listOfFineRates = tcsFineRateMasterService.findAll();
		info("REST response to get list of FineRateMaster records : {} ", listOfFineRates);
		ApiStatus<List<TCSFineRateMaster>> apiStatus = new ApiStatus<List<TCSFineRateMaster>>(HttpStatus.OK,
				"To get List of Tcs Fine Rate Master Records", "List of Tcs Fine Rate Master Records", listOfFineRates);
		return new ResponseEntity<ApiStatus<List<TCSFineRateMaster>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET/fine-rate-masters/{id} To get a particular record based on id
	 * 
	 * @param id
	 * @return a particular record
	 * @throws RecordNotFoundException
	 * @throws FieldValidator
	 */
	@GetMapping(value = "/fine-rate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSFineRateMaster>> getFineRateMaster(@PathVariable Long id)
			throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		TCSFineRateMaster fineRateMaster = null;
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		} else {
			fineRateMaster = tcsFineRateMasterService.findOne(id);
		}
		ApiStatus<TCSFineRateMaster> apiStatus = new ApiStatus<TCSFineRateMaster>(HttpStatus.OK,
				"To get a Single Tcs Fine Rate Master Record", "Single Tcs Fine Rate Record", fineRateMaster);
		return new ResponseEntity<ApiStatus<TCSFineRateMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET/fine-rate-masters/{id} Fine Rate master for delete a existing particular
	 * record
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 * @throws FieldValidator
	 */
	@DeleteMapping(value = "/fine-rate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Long>> deleteFineRateMaster(@PathVariable Long id) throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		} else {
			tcsFineRateMasterService.delete(id);
		}
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.OK, "To delete a Single Tcs Fine Rate Master Record",
				"Tcs Fine Rate Master Record Deleted", id);
		return new ResponseEntity<ApiStatus<Long>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET/fine-rate-masters/{id} To get a particular record based on id
	 * 
	 * @param id
	 * @return a particular record
	 * @throws RecordNotFoundException
	 * @throws FieldValidator
	 */
	@GetMapping(value = "/fine-rate-section/{filing}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSFineRateMaster>> getTcsFineRateMasterBasedonLateFiling(@PathVariable String filing)
			throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		TCSFineRateMaster fineRateMaster = null;
		if (filing == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		} else {
			fineRateMaster = tcsFineRateMasterService.findRecordBasedonLateFiling(filing);
		}
		ApiStatus<TCSFineRateMaster> apiStatus = new ApiStatus<TCSFineRateMaster>(HttpStatus.OK,
				"To get a Single Tcs Fine Rate Master Record", "Single Tcs Fine Rate Record", fineRateMaster);
		return new ResponseEntity<ApiStatus<TCSFineRateMaster>>(apiStatus, HttpStatus.OK);
	}

}
