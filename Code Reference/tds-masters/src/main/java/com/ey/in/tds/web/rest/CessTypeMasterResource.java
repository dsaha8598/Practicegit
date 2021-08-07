package com.ey.in.tds.web.rest;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

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
import com.ey.in.tds.common.domain.CessTypeMaster;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.dto.CessTypeMasterDTO;
import com.ey.in.tds.service.CessTypeMasterService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;

import io.micrometer.core.annotation.Timed;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters")
public class CessTypeMasterResource extends BaseResource {

	private final CessTypeMasterService cessTypeMasterService;

	public CessTypeMasterResource(CessTypeMasterService cessTypeMasterService) {
		this.cessTypeMasterService = cessTypeMasterService;

	}

	/**
	 * POST /cess-type-masters : Create a new cessTypeMaster.
	 *
	 * @param cessTypeMaster the cessTypeMaster to create
	 * @return the Response with status 201 (Created) and with body the new
	 *         cessTypeMaster, or with status 500 (Bad Request) if the
	 *         cessTypeMaster has already an I D
	 * @throws URISyntaxException if the Location URI syntax is incorrect
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 */
	@PostMapping(value = "/cesstype", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<CessTypeMaster>> createCessTypeMaster(
			@Valid @RequestBody CessTypeMasterDTO cessTypeMasterDTO, @RequestHeader("X-USER-EMAIL") String userName)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException {
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
		//ESAPI Validating user input
		MasterESAPIValidation.cessTypeMasterInputValidation(cessTypeMasterDTO);
		CessTypeMaster cessTypeMaster = cessTypeMasterService.save(cessTypeMasterDTO,userName);
		ApiStatus<CessTypeMaster> apiStatus = new ApiStatus<CessTypeMaster>(HttpStatus.CREATED,
				"To create a Cess Type Record", "Cess Type Master Record Created ", cessTypeMaster);

		return new ResponseEntity<ApiStatus<CessTypeMaster>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * PUT /cess-type-masters : Updates an existing cessTypeMaster.
	 *
	 * @param cessTypeMaster the cessTypeMaster to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         cessTypeMaster, or with status 400 (Bad Request) if the
	 *         cessTypeMaster is not valid, or with status 500 (Internal Server
	 *         Error) if the cessTypeMaster couldn't be updated
	 * @throws URISyntaxException      if the Location URI syntax is incorrect
	 * @throws RecordNotFoundException
	 * @throws ParseException 
	 * @throws ValidationException 
	 * @throws IntrusionException 
	 */
	@PutMapping(value = "/cesstype", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<CessTypeMaster>> updateCessTypeMaster(
			@Valid @RequestBody CessTypeMasterDTO cessTypeMasterDTO, @RequestHeader("X-USER-EMAIL") String userName)
			throws URISyntaxException, RecordNotFoundException, IntrusionException, ValidationException, ParseException {
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
		CessTypeMaster cessTypeMaster = cessTypeMasterService.update(cessTypeMasterDTO,userName);
		ApiStatus<CessTypeMaster> apiStatus = new ApiStatus<CessTypeMaster>(HttpStatus.CREATED,
				"To update a Cess Type Record", "Cess Type Master Record Updated ", cessTypeMaster);

		return new ResponseEntity<ApiStatus<CessTypeMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /cess-type-masters : get all the cessTypeMasters.
	 *
	 * @param pageable the pagination information
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         cessTypeMasters in body
	 */
	@GetMapping("/cesstype")
	@Timed
	public ResponseEntity<ApiStatus<List<CessTypeMaster>>> getAllCessTypeMasters() {
		MultiTenantContext.setTenantId("master");
		List<CessTypeMaster> listOfCessType = cessTypeMasterService.findAll();
		info("REST response to get list of CessTypeMasters : {} ", listOfCessType);
		ApiStatus<List<CessTypeMaster>> apiStatus = new ApiStatus<List<CessTypeMaster>>(HttpStatus.OK,
				"To get a List of Cess Type Master's", "Cess Type Master List Records", listOfCessType);
		return new ResponseEntity<ApiStatus<List<CessTypeMaster>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /cess-type-masters/:id : get the "id" cessTypeMaster.
	 *
	 * @param id the id of the cessTypeMaster to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         cessTypeMaster, or with status 404 (Not Found)
	 * @throws RecordNotFoundException
	 */
	@GetMapping("/cesstype/{id}")
	@Timed
	public ResponseEntity<ApiStatus<Optional<CessTypeMaster>>> getCessTypeMaster(@PathVariable Long id)
			throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}
		Optional<CessTypeMaster> cessTypeMaster = cessTypeMasterService.findOne(id);
		ApiStatus<Optional<CessTypeMaster>> apiStatus = new ApiStatus<Optional<CessTypeMaster>>(HttpStatus.OK,
				"To get a Single Type Cess Master", "Cess Type Master Single Record", cessTypeMaster);
		return new ResponseEntity<ApiStatus<Optional<CessTypeMaster>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * DELETE /cess-type-masters/:id : delete the "id" cessTypeMaster.
	 *
	 * @param id the id of the cessTypeMaster to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/cesstype/{id}")
	@Timed
	public ResponseEntity<ApiStatus<Long>> deleteCessTypeMaster(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}
		cessTypeMasterService.delete(id);
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.OK, "To delete a Single Type Cess Master",
				"Cess Type Master Record Deleted", id);
		return new ResponseEntity<ApiStatus<Long>>(apiStatus, HttpStatus.OK);
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
	@PostMapping("/cesstype/upload/excel")
	public ResponseEntity<ApiStatus<MasterBatchUpload>> cessTypeMasterUploadExcel(
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
				masterBatchUpload = cessTypeMasterService.saveFileData(file, assesssmentYear, assessmentMonth, userName);
			} catch (Exception e) {
				throw new CustomException("Unable to process the file", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.warn("Unsupported file.");
			throw new CustomException("Invalid file, only Excel files are allowed", HttpStatus.BAD_REQUEST);
		}
		ApiStatus<MasterBatchUpload> apiStatus = new ApiStatus<MasterBatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED CESS TYPE MASTER FILE SUCCESSFULLY ", masterBatchUpload);
		return new ResponseEntity<ApiStatus<MasterBatchUpload>>(apiStatus, HttpStatus.OK);
	}

}
