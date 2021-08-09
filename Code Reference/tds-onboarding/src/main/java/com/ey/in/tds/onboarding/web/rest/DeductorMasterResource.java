package com.ey.in.tds.onboarding.web.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterEditDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorTanAddress;
import com.ey.in.tds.common.security.SecurityValidations;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.onboarding.service.deductor.DeductorMasterService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.micrometer.core.annotation.Timed;

/**
 * REST controller for managing DeductorMaster.
 */
@RestController
@RequestMapping("/api/onboarding")
public class DeductorMasterResource extends BaseResource {

	private final DeductorMasterService deductorMasterService;
	
	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	public DeductorMasterResource(DeductorMasterService deductorMasterService) {
		this.deductorMasterService = deductorMasterService;
	}

	/**
	 * POST /deductor-masters : Create a new deductorMaster.
	 *
	 * @param deductorMasterDTO the deductorMaster to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         deductorMaster, or with status 400 (Bad Request) if the
	 *         deductorMaster has already an ID
	 * @throws URISyntaxException      if the Location URI syntax is incorrect
	 * @throws RecordNotFoundException
	 * @throws JsonProcessingException
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */
	@PostMapping("/deductor")
	public ResponseEntity<ApiStatus<DeductorMaster>> createDeductorMaster(
			@Valid @RequestBody DeductorMasterDTO deductorMasterDTO, @RequestHeader("X-USER-EMAIL") String userName)
			throws URISyntaxException, RecordNotFoundException, JsonProcessingException, IntrusionException,
			ValidationException, ParseException {

		//MultiTenantContext.setTenantId("client2.dvtfo.onmicrosoft.com");
		if (deductorMasterDTO.getApplicableTo() != null
				&& (deductorMasterDTO.getApplicableFrom().after(deductorMasterDTO.getApplicableTo())
						|| deductorMasterDTO.getApplicableFrom().equals(deductorMasterDTO.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		SecurityValidations.deductorMasterInputValidation(deductorMasterDTO);
		DeductorMaster deductorMaster = deductorMasterService.save(deductorMasterDTO, userName);
		ApiStatus<DeductorMaster> apiStatus = new ApiStatus<DeductorMaster>(HttpStatus.CREATED,
				"To create a Deductor Master Record", "Deductor/Collector Master Record Created.", deductorMaster);
		return new ResponseEntity<ApiStatus<DeductorMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorMasterDTO
	 * @param userName
	 * @return
	 * @throws RecordNotFoundException
	 * @throws JsonProcessingException
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	@PutMapping(path = "/deductor", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DeductorMasterEditDTO>> updateDeductorMaster(
			@Valid @RequestBody DeductorMasterEditDTO deductorMasterDTO, @RequestHeader("X-USER-EMAIL") String userName)
			throws RecordNotFoundException, JsonProcessingException, IntrusionException, ValidationException,
			ParseException {
	
		if (deductorMasterDTO.getEmail() == null) {
			throw new CustomException("Email Cannot be Null", HttpStatus.BAD_REQUEST);
		}

		if (deductorMasterDTO.getPhoneNumber() == null) {
			throw new CustomException("Phone Number Cannot be Null", HttpStatus.BAD_REQUEST);
		}

		if (deductorMasterDTO.getApplicableTo() != null
				&& deductorMasterDTO.getApplicableFrom().after(deductorMasterDTO.getApplicableTo())) {
			throw new CustomException("Applicable To cannot be Greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		SecurityValidations.deductorMasterInputValidationForUpdate(deductorMasterDTO);
		DeductorMasterEditDTO result = deductorMasterService.updateDeductorMaster(deductorMasterDTO, userName);
		ApiStatus<DeductorMasterEditDTO> apiStatus = new ApiStatus<DeductorMasterEditDTO>(HttpStatus.CREATED,
				"To update a Deductor Master Record", "Deductor/Collector Master Record Updated.", result);

		return new ResponseEntity<ApiStatus<DeductorMasterEditDTO>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(path = "/deductor", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<DeductorMasterDTO>>> getAllDeductorMasters()
			throws JsonParseException, JsonMappingException, IOException {
		//SETING TENANT ID TO TEST FROM SWAGGER
		//		MultiTenantContext.setTenantId("client1.dvtfo.onmicrosoft.com");
		List<DeductorMasterDTO> list = deductorMasterService.findAll();
		ApiStatus<List<DeductorMasterDTO>> apiStatus = new ApiStatus<List<DeductorMasterDTO>>(HttpStatus.OK,
				"To get List of Deductor Master Records", "List of Deductor Master Record", list);
		return new ResponseEntity<ApiStatus<List<DeductorMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(path = "/deductor/{pan}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DeductorMasterEditDTO>> getDeductorMaster(@PathVariable String pan,
			@RequestHeader("X-USER-EMAIL") String userName) {
		//SETING TENANT ID TO TEST FROM SWAGGER
		//		MultiTenantContext.setTenantId("client1.dvtfo.onmicrosoft.com");
		DeductorMasterEditDTO deductorMasterDTO = null;
		deductorMasterDTO = deductorMasterService.findOneByPan(pan);
		ApiStatus<DeductorMasterEditDTO> apiStatus = new ApiStatus<DeductorMasterEditDTO>(HttpStatus.OK,
				"To get a Single Deductor Master Record", "Single Deductor Record", deductorMasterDTO);
		return new ResponseEntity<ApiStatus<DeductorMasterEditDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tenantId
	 * @param pan
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GetMapping("/deductor-data/{pan}")
	public DeductorMasterDTO getDeductorMasterData(@RequestHeader("X-TENANT-ID") String tenantId,
			@PathVariable String pan) throws JsonParseException, JsonMappingException, IOException {
		MultiTenantContext.setTenantId(tenantId);
		DeductorMasterDTO deductorMasterDTO = null;
		deductorMasterDTO = deductorMasterService.findOne(pan);
		info("Returning deductor data", deductorMasterDTO);
		return deductorMasterDTO;
	}

	// Feign Client
	@GetMapping(path = "/deductor-user/{pan}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductorMasterRecord(@PathVariable String pan)
			throws JsonParseException, JsonMappingException, IOException {
		DeductorMasterDTO deductorMasterDTO = null;
		deductorMasterDTO = deductorMasterService.findOne(pan);

		ApiStatus<DeductorMasterDTO> apiStatus = new ApiStatus<DeductorMasterDTO>(HttpStatus.OK,
				"To get a Single Deductor Master Record", "Single Deductor Record", deductorMasterDTO);

		return new ResponseEntity<ApiStatus<DeductorMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	// This method is called by Feing Client
	@GetMapping("/deductor/clientcall/{pan}")
	public DeductorMasterDTO getDeductorMasterForClient(@PathVariable String pan)
			throws JsonParseException, JsonMappingException, IOException {
		DeductorMasterDTO deductorMasterDTO = null;
		deductorMasterDTO = deductorMasterService.findOne(pan);
		return deductorMasterDTO;
	}

	
	@GetMapping(path = "/deductor-tan-user/{pan}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductorMasterRecordForFeignById(@PathVariable String pan)
			throws JsonParseException, JsonMappingException, IOException {
		DeductorMasterDTO deductorMasterDTO = null;
		deductorMasterDTO = deductorMasterService.findOne(pan);

		ApiStatus<DeductorMasterDTO> apiStatus = new ApiStatus<DeductorMasterDTO>(HttpStatus.OK,
				"To get a Single Deductor Master Record", "Single Deductor Record", deductorMasterDTO);

		return new ResponseEntity<ApiStatus<DeductorMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/deductor-tan-list")
	public ResponseEntity<ApiStatus<Map<String, Object>>> getDeductorTanList(
			@RequestHeader("X-TENANT-ID") String tenantId) {
		Map<String, Object> deductorTanList = deductorMasterService.getDeductorTanList();

		ApiStatus<Map<String, Object>> apiStatus = new ApiStatus<Map<String, Object>>(HttpStatus.OK,
				"List of deductor tan list", "Deductor tan list", deductorTanList);

		return new ResponseEntity<ApiStatus<Map<String, Object>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param deductorPan
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/deductorbypan")
	public ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductorByPan(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		DeductorMasterDTO deductorMaster = deductorMasterService.getDeductorByPan(deductorPan);
        
		ApiStatus<DeductorMasterDTO> apiStatus = new ApiStatus<DeductorMasterDTO>(HttpStatus.OK,
				"Get deductor data by pan", "Deductor data", deductorMaster);

		return new ResponseEntity<ApiStatus<DeductorMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/getdeductorpanbytan")
	public ResponseEntity<ApiStatus<String>> getDeductorPanBasedOnTan(@RequestParam("receiptTan") String receiptTan) {

		logger.info("Request for Receipt Tan : {}", receiptTan);
		String pan = deductorMasterService.getPanByReceiptTan(receiptTan);

		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "To get a Deductor Pan based on Receipt Tan",
				"Deductor Pan based on Receipt Tan", pan);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param pans
	 * @return
	 */
	@PostMapping(value = "/deductorsbypans")
	public ResponseEntity<ApiStatus<Set<String>>> getDeductorsByPans(@RequestBody List<String> pans) {
		Set<String> deductorPanNames = deductorMasterService.getDeductorsByPans(pans);

		ApiStatus<Set<String>> apiStatus = new ApiStatus<>(HttpStatus.OK, "Get deductor data by pan", "Deductor data",
				deductorPanNames);

		return new ResponseEntity<ApiStatus<Set<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * fiegn call to get deductor master
	 * 
	 * @return
	 */
	@GetMapping(value = "/deductors/active")
	public ResponseEntity<ApiStatus<List<DeductorMaster>>> getActiveDeductor(
			@RequestHeader("X-TENANT-ID") String tenantId) {
		List<DeductorMaster> deductorMasters = deductorMasterDAO.findAll();
		ApiStatus<List<DeductorMaster>> apiStatus = new ApiStatus<List<DeductorMaster>>(HttpStatus.OK,
				"To get a Deductor Pan based on Receipt Tan", "Deductor Pan based on Receipt Tan", deductorMasters);
		return new ResponseEntity<ApiStatus<List<DeductorMaster>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param pan
	 * @param userName
	 * @return
	 */
	@GetMapping(path = "/deductortdcortcs/{pan}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Map<String,Object>>> getDeductorMasterTdsOrTcs(@PathVariable String pan,
			@RequestHeader("X-USER-EMAIL") String userName) {
		Map<String,Object> response = deductorMasterService.findByPan(pan);
		ApiStatus<Map<String,Object>> apiStatus = new ApiStatus<Map<String,Object>>(HttpStatus.OK,
				"To know Deductor is TDS or TCS", "Single Deductor Record", response);
		return new ResponseEntity<ApiStatus<Map<String,Object>>>(apiStatus, HttpStatus.OK);
	}
	/**
	 * returns the pan status 
	 * @param pan
	 * @param userName
	 * @return
	 */
	@GetMapping(path = "/pan/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> getPanStatus(@RequestParam("DEDUCTOR-PAN") String pan,
			@RequestHeader("X-USER-EMAIL") String userName) {
		logger.info("Checking the status of pan {}",pan);
		String status=deductorMasterService.getPanStatus(pan);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK,
				"SUCCESS", "Pan status", status);
		logger.info("Status returned for pan "+pan+" is {} "+status);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * feign client for deductor tan address
	 * 
	 * @param receiptTan
	 * @return
	 */
	@GetMapping(value = "/getdeductor/tan")
	public ResponseEntity<ApiStatus<List<DeductorTanAddress>>> getDeductorBasedOnTan(@RequestParam("tan") String tan,
			@RequestHeader("X-TENANT-ID") String tenantId) {
		logger.info("Request for Deductor Tan : {}", tan);
		List<DeductorTanAddress> listDeductorTanAddress = deductorMasterService.getDeductorBasedOnTan(tan);
		ApiStatus<List<DeductorTanAddress>> apiStatus = new ApiStatus<List<DeductorTanAddress>>(HttpStatus.OK,
				"To get a Deductor Pan based on Receipt Tan", "Deductor Pan based on Receipt Tan",
				listDeductorTanAddress);
		return new ResponseEntity<ApiStatus<List<DeductorTanAddress>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * feign client for deductor tan address based on pan
	 * 
	 * @param receiptTan
	 * @return
	 */
	@GetMapping(value = "/getdeductor/pan")
	public ResponseEntity<ApiStatus<List<DeductorTanAddress>>> getDeductorBasedOnPan(@RequestParam("pan") String pan,
			@RequestHeader("X-TENANT-ID") String tenantId) {
		logger.info("Request for Deductor Tan : {}", pan);
		List<DeductorTanAddress> listDeductorTanAddress = deductorMasterService.getDeductorBasedOnPan(pan, tenantId);
		ApiStatus<List<DeductorTanAddress>> apiStatus = new ApiStatus<List<DeductorTanAddress>>(HttpStatus.OK,
				"To get a Deductor tan address based on Tan", "Deductor tan address based on Tan",
				listDeductorTanAddress);
		return new ResponseEntity<ApiStatus<List<DeductorTanAddress>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param file
	 * @param tan
	 * @param pan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/deductor/upload/excel")
	public ResponseEntity<ApiStatus<BatchUpload>> uploadExcel(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		BatchUpload batchUpload = null;
		logger.info("Testing Something in Upload Excel");
		logger.info("TAN-NUMBER: {}", tan);
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			batchUpload = deductorMasterService.saveFileData(file, tan, assesssmentYear, assessmentMonth, userName,
					tenantId, pan);
		}
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED DEDUCTOR MASTER FILE SUCCESSFULLY ", batchUpload);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param pan
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/deductorname/by/pan")
	public ResponseEntity<ApiStatus<String>> getDeductorNameBasedOnPan(@RequestParam("deductorPan") String pan,
			@RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		String deductorName = deductorMasterService.getDeductorNameBasedOnPan(pan);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "NO ALERT", deductorName);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
	
	@GetMapping(value = "/deductorTanAddress")
    public ResponseEntity<ApiStatus<DeductorTanAddress>> getDeductorTanAddressByPanTan(@RequestParam("deductorTan") String deductorTan, @RequestParam("deductorPan") String deductorPan,
                                                                                       @RequestHeader(value = "X-TENANT-ID") String tenantId) {
        DeductorTanAddress deductorTanAddress = deductorMasterService.getDeductorTanAddress(deductorTan, deductorPan);

        ApiStatus<DeductorTanAddress> apiStatus = new ApiStatus<DeductorTanAddress>(HttpStatus.OK,
                "SUCCESS", "Get a Deductor Tan Address Details", deductorTanAddress);
        return new ResponseEntity<ApiStatus<DeductorTanAddress>>(apiStatus, HttpStatus.OK);

    }
	
}
