package com.ey.in.tds.web.rest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
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
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.dto.CustomNatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.CustomSectionRateDTO;
import com.ey.in.tds.common.repository.NatureOfPaymentMasterRepository;
import com.ey.in.tds.core.errors.FieldValidator;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.dto.NopCessRateSurageRateDTO;
import com.ey.in.tds.service.NatureOfPaymentMasterService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * REST controller for managing NatureOfPaymentMaster.
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters")
public class NatureOfPaymentMasterResource extends BaseResource {

	private final NatureOfPaymentMasterService natureOfPaymentMasterService;

	@Autowired
	private NatureOfPaymentMasterRepository natureOfPaymentMasterRepository;

	public NatureOfPaymentMasterResource(NatureOfPaymentMasterService natureOfPaymentMasterService) {
		this.natureOfPaymentMasterService = natureOfPaymentMasterService;
	}

	/**
	 * This method is used to create new Nature of Payment record
	 * 
	 * @param natureOfPaymentMasterfromui
	 * @return
	 * @throws JsonProcessingException
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 * @throws FieldValidator
	 */
	@PostMapping(value = "/nature-of-payment", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NatureOfPaymentMaster>> createNatureOfPaymentMaster(
			@Valid @RequestBody NatureOfPaymentMaster natureOfPaymentMaster,
			@RequestHeader("X-USER-EMAIL") String userName)
			throws JsonProcessingException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if ((natureOfPaymentMaster.getApplicableTo() != null) && (natureOfPaymentMaster.getApplicableFrom()
				.equals(natureOfPaymentMaster.getApplicableTo())
				|| natureOfPaymentMaster.getApplicableFrom().isAfter(natureOfPaymentMaster.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be Greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		MasterESAPIValidation.natureOfPaymentMasterInputValidation(natureOfPaymentMaster);
		NatureOfPaymentMaster response = natureOfPaymentMasterService.save(natureOfPaymentMaster, userName);
		ApiStatus<NatureOfPaymentMaster> apiStatus = new ApiStatus<NatureOfPaymentMaster>(HttpStatus.CREATED,
				"To create a record for Nature of Payment", "Created record of Nature Of Payment", response);

		return new ResponseEntity<ApiStatus<NatureOfPaymentMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method is used to update Nature of Payment record
	 * 
	 * @param natureOfPaymentMaster
	 * @return
	 * @throws JsonProcessingException
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 * @throws FieldValidator
	 * @throws RecordNotFoundException
	 */
	@PutMapping(value = "/nature-of-payment", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NatureOfPaymentMaster>> updateNatureOfPaymentMaster(
			@Valid @RequestBody NatureOfPaymentMaster natureOfPaymentMaster,
			@RequestHeader("X-USER-EMAIL") String userName)
			throws JsonProcessingException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (natureOfPaymentMaster.getId() == null) {
			throw new CustomException("Cannot update Nature Of Payment if Id is null", HttpStatus.BAD_REQUEST);

		}
		if ((natureOfPaymentMaster.getApplicableTo() != null) && (natureOfPaymentMaster.getApplicableFrom()
				.equals(natureOfPaymentMaster.getApplicableTo())
				|| natureOfPaymentMaster.getApplicableFrom().isAfter(natureOfPaymentMaster.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		MasterESAPIValidation.natureOfPaymentMasterInputValidation(natureOfPaymentMaster);
		NatureOfPaymentMaster response = natureOfPaymentMasterService.updateNatureOfPaymentMaster(natureOfPaymentMaster,
				userName);
		ApiStatus<NatureOfPaymentMaster> apiStatus = new ApiStatus<NatureOfPaymentMaster>(HttpStatus.CREATED,
				"To Update a Nature Of Payment", "Updated Successfully", response);

		return new ResponseEntity<ApiStatus<NatureOfPaymentMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method is used to get all Nature of Payment record
	 * 
	 * @return
	 * @throws RecordNotFoundException
	 */
	@GetMapping(value = "/nature-of-payment", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<NatureOfPaymentMasterDTO>>> findAll() throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		List<NatureOfPaymentMasterDTO> list = natureOfPaymentMasterService.findAll();
		debug("number of NatureOfPaymentMasters : {} ", list.size());
		ApiStatus<List<NatureOfPaymentMasterDTO>> apiStatus = new ApiStatus<List<NatureOfPaymentMasterDTO>>(
				HttpStatus.OK, "Request to get List of Nature Of Payments", "Nature Of Payment List", list);
		return new ResponseEntity<ApiStatus<List<NatureOfPaymentMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method is used to get Nature of Payment record based on id
	 * 
	 * @param id
	 * @return
	 * @throws FieldValidator
	 * @throws RecordNotFoundException
	 */
	@GetMapping(value = "/nature-of-payment/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>> getNatureOfPaymentMaster(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		NatureOfPaymentMasterDTO natureOfPaymentMaster = null;
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		} else {
			natureOfPaymentMaster = natureOfPaymentMasterService.findOne(id);
		}
		ApiStatus<NatureOfPaymentMasterDTO> apiStatus = new ApiStatus<NatureOfPaymentMasterDTO>(HttpStatus.OK,
				"Request to get a Nature Of Payment Record", "List of Nature of Payments", natureOfPaymentMaster);
		return new ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	// Feign client communication based on id
	@GetMapping(value = "/nature-of-payment-section/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>> getNatureOfPaymentMasterRecord(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		NatureOfPaymentMasterDTO natureOfPaymentMaster = null;
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		} else {
			natureOfPaymentMaster = natureOfPaymentMasterService.findOne(id);
		}
		ApiStatus<NatureOfPaymentMasterDTO> apiStatus = new ApiStatus<NatureOfPaymentMasterDTO>(HttpStatus.OK,
				"Request to get a Single Nature Of Payment Record", "Single Nature Of Payment Record",
				natureOfPaymentMaster);
		return new ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	// Feign Client based on Section and get cess Rate and surchargeRate
	@GetMapping(value = "/nature-of-payment-section/section/{section}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NopCessRateSurageRateDTO>> getNatureOfPaymentMasterRecordBasedonSection(
			@PathVariable String section) {
		MultiTenantContext.setTenantId("master");
		NopCessRateSurageRateDTO natureOfPaymentMaster = null;
		if (section == null) {
			throw new CustomException("Section cannot be Null", HttpStatus.BAD_REQUEST);
		} else {
			natureOfPaymentMaster = natureOfPaymentMasterService.findCessRateSurchargeRateNOPIdBasedonSection(section);
		}
		ApiStatus<NopCessRateSurageRateDTO> apiStatus = new ApiStatus<NopCessRateSurageRateDTO>(HttpStatus.OK,
				"Request to get a Single Nature Of Payment Record", "Single Nature Of Payment Record",
				natureOfPaymentMaster);
		return new ResponseEntity<ApiStatus<NopCessRateSurageRateDTO>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/residentSections")
	public ResponseEntity<ApiStatus<List<String>>> getResidentSecions() {
		MultiTenantContext.setTenantId("master");
		List<String> list = new ArrayList<String>();
		List<CustomNatureOfPaymentMasterDTO> retrievedList = natureOfPaymentMasterRepository.getResidentSections();
		if (!retrievedList.isEmpty()) {
			for (CustomNatureOfPaymentMasterDTO dto : retrievedList) {
				list.add(dto.getSection());
			}
		}

		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS", "Resident Sections",
				list);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call fot onboarding
	 * 
	 * @param residentalStatus
	 * @param section
	 * @param deducteeStatus
	 * @return
	 */
	@GetMapping(value = "/residentSections/basedOn/deducteeStatus/ResidentStatus")
	public ResponseEntity<ApiStatus<Boolean>> getNOPBasedOnStatusAndSectionResidentStatus(
			@RequestParam("Reesidential-status") String residentalStatus, @RequestParam("section") String section,
			@RequestParam("deductee-status") String deducteeStatus) {
		MultiTenantContext.setTenantId("master");
		Boolean response = natureOfPaymentMasterService.getNOPBasedOnStatusAndSectionResidentStatus(residentalStatus,
				section, deducteeStatus);
		ApiStatus<Boolean> apiStatus = new ApiStatus<Boolean>(HttpStatus.OK, "SUCCESS", "SUCCESS", response);
		return new ResponseEntity<ApiStatus<Boolean>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/sections/nop/basedon/residentialStatus")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getNOPAndSectionsResidentialStatus(
			@RequestParam("residentialStatus") String residentalStatus) {
		MultiTenantContext.setTenantId("master");
		List<Map<String, Object>> nopAndSections = natureOfPaymentMasterService
				.getNOPAndSectionsResidentialStatus(residentalStatus);
		ApiStatus<List<Map<String, Object>>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "SUCCESS",
				nopAndSections);
		return new ResponseEntity<ApiStatus<List<Map<String, Object>>>>(apiStatus, HttpStatus.OK);
	}
	
	@GetMapping(value = "/sections/basedon/residentialStatus")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getSectionAndDeducteeStatusBasedOnStatus(
			@RequestParam("residentialStatus") String residentalStatus) {
		MultiTenantContext.setTenantId("master");
		List<Map<String, Object>> nopAndSections = natureOfPaymentMasterService
				.getSectionAndDeducteeStatusBasedOnStatus(residentalStatus);
		ApiStatus<List<Map<String, Object>>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "SUCCESS",
				nopAndSections);
		return new ResponseEntity<ApiStatus<List<Map<String, Object>>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call for deductee master
	 * 
	 * @param section
	 * @param deducteeStatus
	 * @param residentStatus
	 * @return
	 */
	@GetMapping(value = "/section/deducteeStatus/residentStatus")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getListOfNatureAndRate(
			@RequestParam("section") String section, @RequestParam("deducteeStatus") String deducteeStatus,
			@RequestParam("residentStatus") String residentStatus) {
		MultiTenantContext.setTenantId("master");

		List<Map<String, Object>> list = natureOfPaymentMasterService.getListOfNatureAndRate(section, deducteeStatus,
				residentStatus);

		ApiStatus<List<Map<String, Object>>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "Resident Sections",
				list);
		return new ResponseEntity<ApiStatus<List<Map<String, Object>>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call for deductee master
	 * 
	 * @param section
	 * @param deducteeStatus
	 * @param residentStatus
	 * @return
	 */
	@GetMapping(value = "/section/deducteeStatus/")
	public ResponseEntity<ApiStatus<List<String>>> getNOPBasedOnSectionAndResidentialStatus(
			@RequestParam("section") String section, @RequestParam("residentStatus") String residentStatus) {
		MultiTenantContext.setTenantId("master");

		List<String> list = natureOfPaymentMasterRepository.getNOPBasedOnSectionAndResidentialStatus(section,
				residentStatus);

		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS", "Resident Sections",
				list);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call for nature of payment and section.
	 * 
	 * @param section
	 * @param nature
	 * @return
	 */
	@PostMapping(value = "/nature-of-payment/section")
	public ResponseEntity<ApiStatus<Optional<NatureOfPaymentMaster>>> getNOPBasedOnSectionAndNature(
			@RequestBody Map<String, String> nopMap) {
		MultiTenantContext.setTenantId("master");
		String nature = nopMap.get("nop");
		String section = nopMap.get("section");
		Optional<NatureOfPaymentMaster> list = natureOfPaymentMasterService.findBySectionAndNOP(section, nature);
		ApiStatus<Optional<NatureOfPaymentMaster>> apiStatus = new ApiStatus<Optional<NatureOfPaymentMaster>>(
				HttpStatus.OK, "SUCCESS", "Resident Sections", list);
		return new ResponseEntity<ApiStatus<Optional<NatureOfPaymentMaster>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call for nature of payment.
	 * 
	 * @param section
	 * @param nature
	 * @return
	 */
	@PostMapping(value = "/nature-of-payment/nature")
	public ResponseEntity<ApiStatus<Optional<NatureOfPaymentMaster>>> getNOPBasedOnNature(
			@RequestBody Map<String, String> nopMap) {
		MultiTenantContext.setTenantId("master");
		String nature = nopMap.get("nop");
		Optional<NatureOfPaymentMaster> list = natureOfPaymentMasterService.findByNOP(nature);
		ApiStatus<Optional<NatureOfPaymentMaster>> apiStatus = new ApiStatus<Optional<NatureOfPaymentMaster>>(
				HttpStatus.OK, "SUCCESS", "Resident Sections", list);
		return new ResponseEntity<ApiStatus<Optional<NatureOfPaymentMaster>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * uploading NOP file, inserting record in nature_of_payment_master and
	 * tds_master table
	 * 
	 * @param file
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/nature-of-payment/upload/excel")
	public ResponseEntity<ApiStatus<MasterBatchUpload>> uploadNOPExcel(@RequestParam("file") MultipartFile file,
			@RequestHeader("X-USER-EMAIL") String userName) throws Exception {
		MultiTenantContext.setTenantId("master");
		MasterBatchUpload masterBatchUpload = null;
		int assesssmentYear = CommonUtil.getAssessmentYear(null);
		int assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(null);
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			String contentType = new Tika().detect(file.getInputStream());
			if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
					|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
				masterBatchUpload = natureOfPaymentMasterService.saveFileData(file, assesssmentYear, assessmentMonth,
						userName);
				ApiStatus<MasterBatchUpload> apiStatus = new ApiStatus<MasterBatchUpload>(HttpStatus.OK, "SUCCESS",
						"UPLOADED NOP FILE SUCCESSFULLY ", masterBatchUpload);
				return new ResponseEntity<ApiStatus<MasterBatchUpload>>(apiStatus, HttpStatus.OK);
			} else {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
			}
		}
	}

	/**
	 * This API for fegin client
	 * 
	 * @return
	 */
	@GetMapping(value = "/section/rate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<CustomSectionRateDTO>>> getNatureOfPaymentMasterRecord() {
		MultiTenantContext.setTenantId("master");

		List<Map<String, Object>> natureOfPaymentMaster = natureOfPaymentMasterService.getSectionAndRate();

		List<CustomSectionRateDTO> dtoList = new ArrayList<>();

		for (Map<String, Object> customSectionRate : natureOfPaymentMaster) {
			CustomSectionRateDTO customSectionRateDTO = new CustomSectionRateDTO();
			customSectionRateDTO.setNature((String) customSectionRate.get("nature"));
			customSectionRateDTO.setNoiId((BigInteger) customSectionRate.get("id"));
			customSectionRateDTO.setRate(((BigDecimal) customSectionRate.get("rate")).doubleValue());
			customSectionRateDTO.setSection((String) customSectionRate.get("section"));
			dtoList.add(customSectionRateDTO);
		}

		ApiStatus<List<CustomSectionRateDTO>> apiStatus = new ApiStatus<List<CustomSectionRateDTO>>(HttpStatus.OK,
				"Request to get a Single Nature Of Payment Record", "Single Nature Of Payment Record", dtoList);
		return new ResponseEntity<ApiStatus<List<CustomSectionRateDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for fegin client
	 * 
	 * @return
	 */
	@GetMapping(value = "/tds/rate/section")
	public ResponseEntity<ApiStatus<List<CustomSectionRateDTO>>> findTdsSectionRates() {
		logger.info("Feign call executing to get List Of Rates Based  on Section {}");
		MultiTenantContext.setTenantId("master");
		List<Map<String, Object>> list = natureOfPaymentMasterRepository.findSectionRates();
		List<CustomSectionRateDTO> dtoList = new ArrayList<>();
		for (Map<String, Object> customSectionRate : list) {
			CustomSectionRateDTO customSectionRateDTO = new CustomSectionRateDTO();
			customSectionRateDTO.setNature((String) customSectionRate.get("nature"));
			customSectionRateDTO.setNoiId((BigInteger) customSectionRate.get("id"));
			if (customSectionRate.get("rate") != null) {
				customSectionRateDTO.setRate(((BigDecimal) customSectionRate.get("rate")).doubleValue());
			}
			customSectionRateDTO.setSection((String) customSectionRate.get("section"));
			if (customSectionRate.get("rate_for_no_pan") != null) {
				customSectionRateDTO
						.setNoPanRate(((BigDecimal) customSectionRate.get("rate_for_no_pan")).doubleValue());
			}
			if (customSectionRate.get("no_itr_rate") != null) {
				customSectionRateDTO.setNoItrRate(((BigDecimal) customSectionRate.get("no_itr_rate")).doubleValue());
			}
			if (customSectionRate.get("no_pan_rate_and_no_itr_rate") != null) {
				customSectionRateDTO.setNoPanNoItrRate(
						((BigDecimal) customSectionRate.get("no_pan_rate_and_no_itr_rate")).doubleValue());
			}
			customSectionRateDTO.setDeducteeStatus((String) customSectionRate.get("status"));
			dtoList.add(customSectionRateDTO);
		}
		ApiStatus<List<CustomSectionRateDTO>> apiStatus = new ApiStatus<List<CustomSectionRateDTO>>(HttpStatus.OK,
				"SUCCESS", "Nature Of Payment Data", dtoList);
		logger.info("Retrieved List of Rates {}", dtoList);
		return new ResponseEntity<ApiStatus<List<CustomSectionRateDTO>>>(apiStatus, HttpStatus.OK);
	}

}
