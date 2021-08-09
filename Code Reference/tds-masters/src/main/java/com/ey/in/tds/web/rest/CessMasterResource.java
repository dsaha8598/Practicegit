package com.ey.in.tds.web.rest;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;

import javax.validation.Valid;

import org.apache.tika.Tika;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.BasisOfCessDetails;
import com.ey.in.tds.common.domain.CessMaster;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.dto.SurchargeAndCessDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.dto.CessMasterDTO;
import com.ey.in.tds.service.CessMasterService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;

import io.micrometer.core.annotation.Timed;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters")
public class CessMasterResource extends BaseResource {

	@Autowired
	private final CessMasterService cessMasterService;

	public CessMasterResource(CessMasterService cessMasterService) {
		this.cessMasterService = cessMasterService;
	}

	/**
	 * POST /cess-masters : Create a new cessMaster.
	 *
	 * @param cessMasterDTO the cessMaster to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         cessMaster
	 * @throws URISyntaxException  if the Location URI syntax is incorrect
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */
	@PostMapping(value = "/cess", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<Long>> createCessMaster(@Valid @RequestBody CessMasterDTO cessMasterDTO,
			@RequestHeader("X-USER-EMAIL") String userName)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (cessMasterDTO.getId() != null) {
			throw new CustomException("cess master should not have ID", HttpStatus.BAD_REQUEST);
		}
		if (cessMasterDTO.getApplicableTo() != null
				&& (cessMasterDTO.getApplicableFrom().equals(cessMasterDTO.getApplicableTo())
						|| cessMasterDTO.getApplicableFrom().isAfter(cessMasterDTO.getApplicableTo()))) {
			throw new CustomException("From date cannot be equal to or greater than To Date");
		}
		// ESAPI Validating user input
		MasterESAPIValidation.cessMasterInputValidation(cessMasterDTO);
		Long result = cessMasterService.saveCessMaster(cessMasterDTO, userName);
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.CREATED, "To create a Cess Master",
				"Cess Master Created", result);
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
	public ResponseEntity<ApiStatus<List<CessMasterDTO>>> getAllCessMasters() {
		MultiTenantContext.setTenantId("master");
		List<CessMasterDTO> cessMasterDTO = cessMasterService.findAll();
		info("REST response to get a page of SurchargeMasters : {} ", cessMasterDTO);
		ApiStatus<List<CessMasterDTO>> apiStatus = new ApiStatus<List<CessMasterDTO>>(HttpStatus.OK,
				"To get a List of Cess Master's", "Cess Master List Records", cessMasterDTO);
		return new ResponseEntity<ApiStatus<List<CessMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /cess-masters/:id : get the "id" cessMaster.
	 *
	 * @param id the id of the cessMaster to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the cessMaster,
	 *         or with status 404 (Not Found)
	 * @throws RecordNotFoundException
	 */
	@GetMapping(value = "/cess/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<CessMasterDTO>> getCessMaster(@PathVariable Long id)
			throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}
		CessMasterDTO cessMaster = cessMasterService.findOne(id);
		ApiStatus<CessMasterDTO> apiStatus = new ApiStatus<CessMasterDTO>(HttpStatus.OK, "To get a Single Cess Master",
				"Cess Master Single Record", cessMaster);
		return new ResponseEntity<ApiStatus<CessMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * PUT /cess-masters : Updates an existing cessMaster.
	 *
	 * @param cessMaster the cessMaster to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         cessMaster, or with status 500 (Internal Server Error) if the
	 *         cessMaster couldn't be updated
	 * @throws URISyntaxException  if the Location URI syntax is incorrect
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */
	@PutMapping(value = "/cess", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<CessMaster>> updateCessMaster(@Valid @RequestBody CessMasterDTO cessMasterDTO,
			@RequestHeader("X-USER-EMAIL") String userName)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (cessMasterDTO.getId() == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}
		if (cessMasterDTO.getApplicableTo() != null
				&& (cessMasterDTO.getApplicableFrom().equals(cessMasterDTO.getApplicableTo())
						|| cessMasterDTO.getApplicableFrom().isAfter(cessMasterDTO.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		MasterESAPIValidation.cessMasterInputValidation(cessMasterDTO);
		CessMaster result = cessMasterService.updateCessMaster(cessMasterDTO, userName);
		ApiStatus<CessMaster> apiStatus = new ApiStatus<CessMaster>(HttpStatus.CREATED,
				"To update a Single Cess Master", "Cess Master Updated", result);
		return new ResponseEntity<ApiStatus<CessMaster>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/basis-of-cess-nature-of-payment/{natureOfPaymentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<BasisOfCessDetails>> getBasisOfCessNatureOfPaymentBasedonNatureOfPaymentId(
			@PathVariable Long natureOfPaymentId) {
		MultiTenantContext.setTenantId("master");
		BasisOfCessDetails basisOfCess = cessMasterService.getBasisOfCessDetails(natureOfPaymentId);
		ApiStatus<BasisOfCessDetails> apiStatus = new ApiStatus<BasisOfCessDetails>(HttpStatus.CREATED,
				"To get a Basis Of Cess Nature Record", "Get Basis of Cess Nature Record", basisOfCess);
		return new ResponseEntity<ApiStatus<BasisOfCessDetails>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param section
	 * @param deducteeStatus
	 * @param residentialStatus
	 * @return
	 */
	@GetMapping("/cessdetails")
	public ResponseEntity<ApiStatus<List<SurchargeAndCessDTO>>> getCessDetailsBySectionDeducteeStatus(
			@RequestParam(value = "section") String section,
			@RequestParam(value = "deducteeStatus") String deducteeStatus,
			@RequestParam(value = "residentialStatus") String residentialStatus) {
		MultiTenantContext.setTenantId("master");
		List<SurchargeAndCessDTO> cessMasterList = cessMasterService.getCessDetailsBySectionDeducteeStatus(section,
				deducteeStatus, residentialStatus);
		ApiStatus<List<SurchargeAndCessDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"List of cess master details.", cessMasterList);
		return new ResponseEntity<ApiStatus<List<SurchargeAndCessDTO>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param cessType
	 * @return
	 */
	@GetMapping("/cessdetails/cesstype")
	public ResponseEntity<ApiStatus<List<SurchargeAndCessDTO>>> getCessDetailsByCessType(
			@RequestParam(value = "cessType") String cessType) {
		MultiTenantContext.setTenantId("master");
		List<SurchargeAndCessDTO> cessMasterList = cessMasterService.getCessDetailsByCessType(cessType);
		ApiStatus<List<SurchargeAndCessDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"List of cess master details.", cessMasterList);
		return new ResponseEntity<ApiStatus<List<SurchargeAndCessDTO>>>(apiStatus, HttpStatus.OK);
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
	@PostMapping("/cess/upload/excel")
	public ResponseEntity<ApiStatus<MasterBatchUpload>> cessMasterUploadExcel(
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
				masterBatchUpload = cessMasterService.saveFileData(file, assesssmentYear, assessmentMonth, userName);
			} catch (Exception e) {
				throw new CustomException("Unable to process the file", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.warn("Unsupported file.");
			throw new CustomException("Invalid file, only Excel files are allowed", HttpStatus.BAD_REQUEST);
		}
		ApiStatus<MasterBatchUpload> apiStatus = new ApiStatus<MasterBatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED CESS MASTER FILE SUCCESSFULLY ", masterBatchUpload);
		return new ResponseEntity<ApiStatus<MasterBatchUpload>>(apiStatus, HttpStatus.OK);
	}

}
