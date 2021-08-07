package com.ey.in.tds.web.rest;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.tika.Tika;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.BasisOfSurchargeDetails;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.dto.SurchargeAndCessDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.dto.SurchargeMasterDTO;
import com.ey.in.tds.service.SurchargeMasterService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;

import io.micrometer.core.annotation.Timed;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters")
public class SurchargeMasterResource extends BaseResource {

	private final SurchargeMasterService surchargeMasterService;

	public SurchargeMasterResource(SurchargeMasterService surchargeMasterService) {
		this.surchargeMasterService = surchargeMasterService;
	}

	/**
	 * POST /surcharge-masters : Create a new surchargeMaster.
	 *
	 * @param surchargeMaster the surchargeMaster to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         surchargeMaster, or with status 400 (Bad Request) if the
	 *         surchargeMaster has already an ID
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 */
	@PostMapping(path = "/surcharge", consumes = "application/json")
	@Timed
	public ResponseEntity<ApiStatus<SurchargeMasterDTO>> createSurchargeMaster(
			@Valid @RequestBody SurchargeMasterDTO surchargeMasterDTO, @RequestHeader("X-USER-EMAIL") String userName)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException {
        MultiTenantContext.setTenantId("master");
		if (surchargeMasterDTO.getId() != null) {
			throw new CustomException("Record already cannot have an Id ", HttpStatus.BAD_REQUEST);
		}

		if (surchargeMasterDTO.getApplicableTo() != null
				&& surchargeMasterDTO.getApplicableFrom().isAfter(surchargeMasterDTO.getApplicableTo())
				|| surchargeMasterDTO.getApplicableFrom().equals(surchargeMasterDTO.getApplicableTo())) {
			throw new CustomException("Applicable To cannot be greater than Applicable From ", HttpStatus.BAD_REQUEST);
		}

		if (surchargeMasterDTO.isSurchargeApplicable() && surchargeMasterDTO.getSurchargeRate() == null) {
			throw new CustomException("Surcharge Rate cannot be empty, when isSurchargeApplicable is true ",
					HttpStatus.BAD_REQUEST);
		}
		//ESAPI Validating user input
		MasterESAPIValidation.surchargeMasterInputValidation(surchargeMasterDTO);
		SurchargeMasterDTO result = surchargeMasterService.createSurchargeMaster(surchargeMasterDTO,userName);
		ApiStatus<SurchargeMasterDTO> apiStatus = new ApiStatus<SurchargeMasterDTO>(HttpStatus.CREATED,
				"To create a Surcharge Master Record", "Surcharge Master Record Created ", result);
		return new ResponseEntity<ApiStatus<SurchargeMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /surcharge-masters : get all the surchargeMasters.
	 *
	 * @param pageable the pagination information
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         surchargeMasters in body
	 */
	@GetMapping(path = "/surcharge", produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<List<SurchargeMasterDTO>>> getAllSurchargeMasters() {
		MultiTenantContext.setTenantId("master");
		List<SurchargeMasterDTO> surchargeMasterListDTO = surchargeMasterService.findAll();
		info("REST respone to get list of SurchargeMasters : {}", surchargeMasterListDTO);
		ApiStatus<List<SurchargeMasterDTO>> apiStatus = new ApiStatus<List<SurchargeMasterDTO>>(HttpStatus.OK,
				"To get List of Surcharge Master Records", "List of Surcharge Master Records", surchargeMasterListDTO);
		return new ResponseEntity<ApiStatus<List<SurchargeMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * PUT /surcharge-masters : Updates an existing surchargeMaster.
	 *
	 * @param surchargeMaster the surchargeMaster to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         surchargeMaster, or with status 400 (Bad Request) if the
	 *         surchargeMaster is not valid, or with status 500 (Internal Server
	 *         Error) if the surchargeMaster couldn't be updated
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 */
	@PutMapping(value = "/surcharge", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<Long>> updateSurchargeMaster(
			@Valid @RequestBody SurchargeMasterDTO surchargeMasterDTO, @RequestHeader("X-USER-EMAIL") String userName)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (surchargeMasterDTO.getId() == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}

		if (surchargeMasterDTO.getApplicableTo() != null
				&& surchargeMasterDTO.getApplicableFrom().isAfter(surchargeMasterDTO.getApplicableTo())
				|| surchargeMasterDTO.getApplicableFrom().equals(surchargeMasterDTO.getApplicableTo())) {
			throw new CustomException("Applicable To cannot be greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		//ESAPI Validating user input
		MasterESAPIValidation.surchargeMasterInputValidation(surchargeMasterDTO);
		Long result = surchargeMasterService.updateSurchargeMaster(surchargeMasterDTO,userName);
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.OK, "To update a Surcharge Master Record",
				"Surcharge Master Record update", result);
		return new ResponseEntity<ApiStatus<Long>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /surcharge-masters/:id : get the "id" surchargeMaster.
	 *
	 * @param id the id of the surchargeMaster to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         surchargeMaster, or with status 404 (Not Found)
	 * @throws RecordNotFoundException
	 */
	@GetMapping("/surcharge/{id}")
	@Timed
	public ResponseEntity<ApiStatus<SurchargeMasterDTO>> getSurchargeMaster(@PathVariable Long id)
			throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}
		SurchargeMasterDTO surchargeMasterDTO = surchargeMasterService.findOne(id);
		ApiStatus<SurchargeMasterDTO> apiStatus = new ApiStatus<SurchargeMasterDTO>(HttpStatus.OK,
				"To get a Surcharge Master Record", "Single Surcharge Master ", surchargeMasterDTO);
		return new ResponseEntity<ApiStatus<SurchargeMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	// Feign Client
	@GetMapping("/basis-of-surcharge-nature-of-payment/{id}")
	public ResponseEntity<ApiStatus<BasisOfSurchargeDetails>> getNatureOfPaymentBasedOnNatureOfPaymentId(
			@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		BasisOfSurchargeDetails basisOfSurcharge = surchargeMasterService
				.getBasisOfSurchargeRecordBasedOnNatureOfPaymentId(id);
		ApiStatus<BasisOfSurchargeDetails> apiStatus = new ApiStatus<BasisOfSurchargeDetails>(HttpStatus.OK,
				"To get a Bais of Surcharge Nature Of Payment Master Record",
				"Single Basis of Surcharge Nature Of payment Master ", basisOfSurcharge);
		return new ResponseEntity<ApiStatus<BasisOfSurchargeDetails>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param section
	 * @param deducteeStatus
	 * @param residentialStatus
	 * @return
	 */
	@GetMapping("/surchargedetails")
	public ResponseEntity<ApiStatus<List<SurchargeAndCessDTO>>> getSurchargeDetailsBySectionDeducteeStatus(
			@RequestParam(value = "section") String section,
			@RequestParam(value = "deducteeStatus") String deducteeStatus,
			@RequestParam(value = "residentialStatus") String residentialStatus) {
		 MultiTenantContext.setTenantId("master");
		List<SurchargeAndCessDTO> surchargeMasterList = surchargeMasterService
				.getSurchargeDetailsBySectionDeducteeStatus(section, deducteeStatus, residentialStatus);
		ApiStatus<List<SurchargeAndCessDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"List of surcharge details.", surchargeMasterList);
		return new ResponseEntity<ApiStatus<List<SurchargeAndCessDTO>>>(apiStatus, HttpStatus.OK);
	}
	
	// Feign client
	@GetMapping("/surchargedetails/by/residentialstatus")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getAllSurchargeDetails(
			@RequestParam(value = "residentialStatus") String residentialStatus) {
		MultiTenantContext.setTenantId("master");
		List<Map<String, Object>> surchargeMasterList = surchargeMasterService
				.getAllSurchargeDetails(residentialStatus);
		ApiStatus<List<Map<String, Object>>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				surchargeMasterList);
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
	@PostMapping("/surcharge/upload/excel")
	public ResponseEntity<ApiStatus<MasterBatchUpload>> surchargeMasteruploadExcel(
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
				masterBatchUpload = surchargeMasterService.saveFileData(file, assesssmentYear, assessmentMonth, userName);
			} catch (Exception e) {
				throw new CustomException("Unable to process the file", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.warn("Unsupported file.");
			throw new CustomException("Invalid file, only Excel files are allowed", HttpStatus.BAD_REQUEST);
		}
		ApiStatus<MasterBatchUpload> apiStatus = new ApiStatus<MasterBatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED SURCHARGE MASTER FILE SUCCESSFULLY ", masterBatchUpload);
		return new ResponseEntity<ApiStatus<MasterBatchUpload>>(apiStatus, HttpStatus.OK);
	}


}
