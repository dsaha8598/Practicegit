package com.ey.in.tds.web.rest;

import java.text.ParseException;
import java.util.List;

import javax.validation.Valid;

import org.apache.tika.Tika;
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
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.FineRateMaster;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.core.errors.FieldValidator;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.service.FineRateMasterService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;

/**
 * REST controller for managing FineRateMaster.
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters")
public class FineRateMasterResource extends BaseResource {

	private final FineRateMasterService fineRateMasterService;

	public FineRateMasterResource(FineRateMasterService fineRateMasterService) {
		this.fineRateMasterService = fineRateMasterService;
	}

	/**
	 * POST/fine-rate-masters - this method is used to create a new Fine Rate Record
	 * 
	 * @param fineRateMaster
	 * @return
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 * @throws ApiStatus
	 */
	@PostMapping(value = "/fine-rate", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<FineRateMaster>> createFineRateMaster(
			@Valid @RequestBody FineRateMaster fineRateMaster, @RequestHeader("X-USER-EMAIL") String userName)
			throws IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (fineRateMaster.getId() != null) {
			throw new CustomException("Record already cannot have an Id ", HttpStatus.BAD_REQUEST);
		}

		if (fineRateMaster.getApplicableTo() != null
				&& (fineRateMaster.getApplicableFrom().equals(fineRateMaster.getApplicableTo())
						|| fineRateMaster.getApplicableFrom().isAfter(fineRateMaster.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be greater than Applicable From ", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		MasterESAPIValidation.fineRateMasterInputValidation(fineRateMaster);
		FineRateMaster response = fineRateMasterService.save(fineRateMaster, userName);
		ApiStatus<FineRateMaster> apiStatus = new ApiStatus<FineRateMaster>(HttpStatus.CREATED,
				"To create a Fine Rate Master Record", "Fine Rate Master Record Created ", response);
		return new ResponseEntity<ApiStatus<FineRateMaster>>(apiStatus, HttpStatus.OK);
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
	public ResponseEntity<ApiStatus<FineRateMaster>> updateFineRateMaster(
			@Valid @RequestBody FineRateMaster fineRateMaster, @RequestHeader("X-USER-EMAIL") String userName)
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
		MasterESAPIValidation.fineRateMasterInputValidation(fineRateMaster);
		FineRateMaster response = fineRateMasterService.updateFineRateMaster(fineRateMaster, userName);
		ApiStatus<FineRateMaster> apiStatus = new ApiStatus<FineRateMaster>(HttpStatus.CREATED,
				"To update a Fine Rate Master Record", "Fine Rate Master Record Updated ", response);

		return new ResponseEntity<ApiStatus<FineRateMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET/fine-rate-masters To get all record
	 * 
	 * @param pageable
	 * @return
	 */
	@GetMapping(value = "/fine-rate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<FineRateMaster>>> getAllFineRateMasters() {
		MultiTenantContext.setTenantId("master");
		List<FineRateMaster> listOfFineRates = fineRateMasterService.findAll();
		info("REST response to get list of FineRateMaster records : {} ", listOfFineRates);
		ApiStatus<List<FineRateMaster>> apiStatus = new ApiStatus<List<FineRateMaster>>(HttpStatus.OK,
				"To get List of Fine Rate Master Records", "List of Fine Rate Master Records", listOfFineRates);
		return new ResponseEntity<ApiStatus<List<FineRateMaster>>>(apiStatus, HttpStatus.OK);
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
	public ResponseEntity<ApiStatus<FineRateMaster>> getFineRateMaster(@PathVariable Long id)
			throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		FineRateMaster fineRateMaster = null;
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		} else {
			fineRateMaster = fineRateMasterService.findOne(id);
		}
		ApiStatus<FineRateMaster> apiStatus = new ApiStatus<FineRateMaster>(HttpStatus.OK,
				"To get a Single Fine Rate Master Record", "Single Fine Rate Record", fineRateMaster);
		return new ResponseEntity<ApiStatus<FineRateMaster>>(apiStatus, HttpStatus.OK);
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
			fineRateMasterService.delete(id);
		}
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.OK, "To delete a Single Fine Rate Master Record",
				"Fine Rate Master Record Deleted", id);
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
	public ResponseEntity<ApiStatus<FineRateMaster>> getFineRateMasterBasedonLateFiling(@PathVariable String filing)
			throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		FineRateMaster fineRateMaster = null;
		if (filing == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		} else {
			fineRateMaster = fineRateMasterService.findRecordBasedonLateFiling(filing);
		}
		ApiStatus<FineRateMaster> apiStatus = new ApiStatus<FineRateMaster>(HttpStatus.OK,
				"To get a Single Fine Rate Master Record", "Single Fine Rate Record", fineRateMaster);
		return new ResponseEntity<ApiStatus<FineRateMaster>>(apiStatus, HttpStatus.OK);
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
	@PostMapping("/fine-rate/upload/excel")
	public ResponseEntity<ApiStatus<MasterBatchUpload>> findRateMasteruploadExcel(
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
				masterBatchUpload = fineRateMasterService.saveFileData(file, assesssmentYear, assessmentMonth,
						userName);
			} catch (Exception e) {
				throw new CustomException("Unable to process the file", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.warn("Unsupported file.");
			throw new CustomException("Invalid file, only Excel files are allowed", HttpStatus.BAD_REQUEST);
		}
		ApiStatus<MasterBatchUpload> apiStatus = new ApiStatus<MasterBatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED FINE RATE MASTER FILE SUCCESSFULLY ", masterBatchUpload);
		return new ResponseEntity<ApiStatus<MasterBatchUpload>>(apiStatus, HttpStatus.OK);
	}

}
