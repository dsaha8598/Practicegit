package com.ey.in.tds.web.rest;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.tika.Tika;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.domain.TdsMonthTracker;
import com.ey.in.tds.common.dto.tdsmonthlytracker.MonthTrackerDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.service.TdsMonthTrackerService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/api/masters")
public class TdsMonthTrackerResource extends BaseResource {

	@Autowired
	private TdsMonthTrackerService tdsMonthTrackerService;

	/**
	 * This api for create a TdsMonthTracker.
	 * 
	 * @param tdsMonthTracker
	 * @return
	 * @throws JsonProcessingException
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 */
	@PostMapping("/monthly-tracker")
	public ResponseEntity<ApiStatus<TdsMonthTracker>> createMonthTracker(
			@Valid @RequestBody TdsMonthTracker tdsMonthTracker, @RequestHeader(value = "USER_NAME") String userName)
			throws JsonProcessingException, IntrusionException, ValidationException, ParseException {

		MultiTenantContext.setTenantId("master");
		if (tdsMonthTracker.getApplicableFrom() == null) {
			throw new CustomException("Applicable From cannot be empty", HttpStatus.BAD_REQUEST);
		}

		if ((tdsMonthTracker.getApplicableTo() != null)
				&& (tdsMonthTracker.getApplicableFrom().equals(tdsMonthTracker.getApplicableTo())
						|| tdsMonthTracker.getApplicableFrom().isAfter(tdsMonthTracker.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be Greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		//ESAPI Validating user input
		MasterESAPIValidation.tdsMonthTrackerInputValidation(tdsMonthTracker);
		TdsMonthTracker monthTracker = tdsMonthTrackerService.save(tdsMonthTracker, userName);
		ApiStatus<TdsMonthTracker> apiStatus = new ApiStatus<TdsMonthTracker>(HttpStatus.CREATED, "SUCCESS",
				"TDS MONTH TRACKER CREATED SUCCESSFULLY", monthTracker);
		return new ResponseEntity<ApiStatus<TdsMonthTracker>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api for get all TdsMonthTracker
	 * 
	 * @return
	 */
	@GetMapping("/monthly-tracker")
	public ResponseEntity<ApiStatus<List<MonthTrackerDTO>>> getAllTdsMonthTracker() {
		MultiTenantContext.setTenantId("master");
		List<MonthTrackerDTO> tracker = tdsMonthTrackerService.findAll();
		ApiStatus<List<MonthTrackerDTO>> apiStatus = new ApiStatus<List<MonthTrackerDTO>>(HttpStatus.OK, "SUCCESS",
				" GET ALL TDS MONTH TRACKER DATA", tracker);
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
		MonthTrackerDTO tracker = tdsMonthTrackerService.findById(id);
		ApiStatus<MonthTrackerDTO> apiStatus = new ApiStatus<MonthTrackerDTO>(HttpStatus.OK, "SUCCESS",
				"GET BY ID TDS MONTH TRACKER", tracker);
		return new ResponseEntity<ApiStatus<MonthTrackerDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api for update TdsMonthTracker Based on id
	 * 
	 * @param userName
	 * @param id
	 * @param tdsMonthTracker
	 * @return
	 * @throws JsonProcessingException
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 */
	@PutMapping("/monthly-tracker")
	public ResponseEntity<ApiStatus<TdsMonthTracker>> updateMonthTracker(
			@RequestHeader(value = "USER_NAME") String userName, @Valid @RequestBody TdsMonthTracker tdsMonthTracker)
			throws JsonProcessingException, IntrusionException, ValidationException, ParseException {

		MultiTenantContext.setTenantId("master");
		if (tdsMonthTracker.getApplicableFrom() == null) {
			throw new CustomException("Applicable From cannot be empty", HttpStatus.BAD_REQUEST);
		}
		if ((tdsMonthTracker.getApplicableTo() != null)
				&& (tdsMonthTracker.getApplicableFrom().equals(tdsMonthTracker.getApplicableTo())
						|| tdsMonthTracker.getApplicableFrom().isAfter(tdsMonthTracker.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be Greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		//ESAPI Validating user input
		MasterESAPIValidation.tdsMonthTrackerInputValidation(tdsMonthTracker);
		TdsMonthTracker monthTracker = tdsMonthTrackerService.update(tdsMonthTracker, userName);
		ApiStatus<TdsMonthTracker> apiStatus = new ApiStatus<TdsMonthTracker>(HttpStatus.OK, "SUCCESS",
				" UPDATED TDS MONTH TRACKER SUCCESSFULLY", monthTracker);
		return new ResponseEntity<ApiStatus<TdsMonthTracker>>(apiStatus, HttpStatus.OK);
	}

	// Feign Client
	@GetMapping("/monthly-tracker/{year}/{month}")
	public ResponseEntity<ApiStatus<TdsMonthTracker>> findByAssessmentYearMonth(@PathVariable Integer year,
			@PathVariable Integer month) {
		MultiTenantContext.setTenantId("master");
		TdsMonthTracker tracker = tdsMonthTrackerService.findByAssessmentYearMonth(year, month);
		ApiStatus<TdsMonthTracker> apiStatus = new ApiStatus<TdsMonthTracker>(HttpStatus.OK, "SUCCESS",
				"List Of Tds monthly Tracker Records", tracker);
		return new ResponseEntity<ApiStatus<TdsMonthTracker>>(apiStatus, HttpStatus.OK);
	}
	
	@GetMapping("/monthly-tracker/challanGeneratedDate")
	public ResponseEntity<ApiStatus<TdsMonthTracker>> findByDueDateChallanPayment(
			@RequestParam(value = "challanGeneratedDate") String challanGeneratedDate) {
		MultiTenantContext.setTenantId("master");
		TdsMonthTracker tracker = tdsMonthTrackerService.findByDueDateChallanPayment(challanGeneratedDate);
		ApiStatus<TdsMonthTracker> apiStatus = new ApiStatus<TdsMonthTracker>(HttpStatus.OK, "SUCCESS",
				"List Of Tds monthly Tracker Records", tracker);
		return new ResponseEntity<ApiStatus<TdsMonthTracker>>(apiStatus, HttpStatus.OK);
	}
	
	//Feign client
	@GetMapping("/monthly-tracker/data")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getAllTdsMonthTrackerData() {
		MultiTenantContext.setTenantId("master");
		List<Map<String, Object>> tracker = tdsMonthTrackerService.getAllTdsMonthTrackerData();
		ApiStatus<List<Map<String, Object>>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", tracker);
		return new ResponseEntity<ApiStatus<List<Map<String, Object>>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param file
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/monthly-tracker/upload/excel")
	public ResponseEntity<ApiStatus<MasterBatchUpload>> monthlyTrackerUploadExcel(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "X-USER-EMAIL") String userName) throws Exception {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		MasterBatchUpload masterBatchUpload = null;
		logger.info("Testing Something in Upload Excel");
		MultiTenantContext.setTenantId("master");
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			try {
				masterBatchUpload = tdsMonthTrackerService.saveFileData(file, assesssmentYear, assessmentMonth, userName);
			} catch (Exception e) {
				throw new CustomException("Unable to process the file", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.warn("Unsupported file.");
			throw new CustomException("Invalid file, only Excel files are allowed", HttpStatus.BAD_REQUEST);
		}
		ApiStatus<MasterBatchUpload> apiStatus = new ApiStatus<MasterBatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED TDS MONTHLY TRACKER MASTER FILE SUCCESSFULLY ", masterBatchUpload);
		return new ResponseEntity<ApiStatus<MasterBatchUpload>>(apiStatus, HttpStatus.OK);
	}
}
