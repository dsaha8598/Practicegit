package com.ey.in.tds.tcs.web.rest;

import java.text.ParseException;
import java.util.List;

import javax.validation.Valid;

import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import com.ey.in.tds.common.domain.tcs.TCSMonthTracker;
import com.ey.in.tds.common.dto.tdsmonthlytracker.MonthTrackerDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.tcs.service.TCSMonthTrackerService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/api/masters/tcs")
public class TCSMonthTrackerResource {

	@Autowired
	private TCSMonthTrackerService tcsMonthTrackerService;

	/**
	 * This api for create a TCSMonthTracker.
	 * 
	 * @param tcsMonthTracker
	 * @return
	 * @throws JsonProcessingException
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 */
	@PostMapping("/monthly-tracker")
	public ResponseEntity<ApiStatus<TCSMonthTracker>> createMonthTracker(
			@Valid @RequestBody TCSMonthTracker tcsMonthTracker, @RequestHeader(value = "USER_NAME") String userName)
			throws JsonProcessingException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (tcsMonthTracker.getApplicableFrom() == null) {
			throw new CustomException("Applicable From cannot be empty", HttpStatus.BAD_REQUEST);
		}

		if ((tcsMonthTracker.getApplicableTo() != null)
				&& (tcsMonthTracker.getApplicableFrom().equals(tcsMonthTracker.getApplicableTo())
						|| tcsMonthTracker.getApplicableFrom().isAfter(tcsMonthTracker.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be Greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		//ESAPI Validating user input
		MasterESAPIValidation.tcsMonthTrackerInputValidation(tcsMonthTracker);
		TCSMonthTracker monthTracker = tcsMonthTrackerService.save(tcsMonthTracker, userName);
		ApiStatus<TCSMonthTracker> apiStatus = new ApiStatus<TCSMonthTracker>(HttpStatus.CREATED, "SUCCESS",
				"TCS MONTH TRACKER CREATED SUCCESSFULLY", monthTracker);
		return new ResponseEntity<ApiStatus<TCSMonthTracker>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api for get all TCSMonthTracker
	 * 
	 * @return
	 */
	@GetMapping("/monthly-tracker")
	public ResponseEntity<ApiStatus<List<MonthTrackerDTO>>> getAllTCSMonthTracker() {
		MultiTenantContext.setTenantId("master");
		List<MonthTrackerDTO> tracker = tcsMonthTrackerService.findAll();
		ApiStatus<List<MonthTrackerDTO>> apiStatus = new ApiStatus<List<MonthTrackerDTO>>(HttpStatus.OK, "SUCCESS",
				" GET ALL TCS MONTH TRACKER DATA", tracker);
		return new ResponseEntity<ApiStatus<List<MonthTrackerDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api for get by id.
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/monthly-tracker/{id}")
	public ResponseEntity<ApiStatus<MonthTrackerDTO>> getById(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		MonthTrackerDTO tracker = tcsMonthTrackerService.findById(id);
		ApiStatus<MonthTrackerDTO> apiStatus = new ApiStatus<MonthTrackerDTO>(HttpStatus.OK, "SUCCESS",
				"GET BY ID TCS MONTH TRACKER", tracker);
		return new ResponseEntity<ApiStatus<MonthTrackerDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api for update TCSMonthTracker Based on id
	 * 
	 * @param userName
	 * @param id
	 * @param tcsMonthTracker
	 * @return
	 * @throws JsonProcessingException
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 */
	@PutMapping("/monthly-tracker")
	public ResponseEntity<ApiStatus<TCSMonthTracker>> updateMonthTracker(
			@RequestHeader(value = "USER_NAME") String userName, @Valid @RequestBody TCSMonthTracker tcsMonthTracker)
			throws JsonProcessingException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (tcsMonthTracker.getApplicableFrom() == null) {
			throw new CustomException("Applicable From cannot be empty", HttpStatus.BAD_REQUEST);
		}
		if ((tcsMonthTracker.getApplicableTo() != null)
				&& (tcsMonthTracker.getApplicableFrom().equals(tcsMonthTracker.getApplicableTo())
						|| tcsMonthTracker.getApplicableFrom().isAfter(tcsMonthTracker.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be Greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		//ESAPI Validating user input
		MasterESAPIValidation.tcsMonthTrackerInputValidation(tcsMonthTracker);
		TCSMonthTracker monthTracker = tcsMonthTrackerService.update(tcsMonthTracker, userName);
		ApiStatus<TCSMonthTracker> apiStatus = new ApiStatus<TCSMonthTracker>(HttpStatus.OK, "SUCCESS",
				" UPDATED TCS MONTH TRACKER SUCCESSFULLY", monthTracker);
		return new ResponseEntity<ApiStatus<TCSMonthTracker>>(apiStatus, HttpStatus.OK);
	}

	// Feign Client
	@GetMapping("/monthly-tracker/{year}/{month}")
	public ResponseEntity<ApiStatus<TCSMonthTracker>> findTcsMonthTrackerByAssessmentYearMonth(@PathVariable Integer year,
			@PathVariable Integer month) {
		MultiTenantContext.setTenantId("master");
		TCSMonthTracker tracker = tcsMonthTrackerService.findByAssessmentYearMonth(year, month);
		ApiStatus<TCSMonthTracker> apiStatus = new ApiStatus<TCSMonthTracker>(HttpStatus.OK, "SUCCESS",
				"List Of Tcs monthly Tracker Records", tracker);
		return new ResponseEntity<ApiStatus<TCSMonthTracker>>(apiStatus, HttpStatus.OK);
	}
}
