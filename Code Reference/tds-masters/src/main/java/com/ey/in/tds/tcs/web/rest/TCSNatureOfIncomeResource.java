package com.ey.in.tds.tcs.web.rest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSNatureOfIncome;
import com.ey.in.tds.common.dto.CustomNatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.CustomSectionRateDTO;
import com.ey.in.tds.common.tcs.repository.TCSNatureOfIncomeRepository;
import com.ey.in.tds.core.errors.FieldValidator;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.tcs.service.TCSNatureOfIncomeService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * REST controller for managing TCSNatureOfIncome.
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters")
public class TCSNatureOfIncomeResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TCSNatureOfIncomeService tcsNatureOfIncomeService;

	@Autowired
	private TCSNatureOfIncomeRepository tcsNatureOfIncomeRepository;

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
	@PostMapping(value = "/tcs/nature-of-income", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSNatureOfIncome>> createNatureOfPaymentMaster(
			@Valid @RequestBody TCSNatureOfIncome tcsNatureOfIncome, @RequestHeader("X-USER-EMAIL") String userName)
			throws JsonProcessingException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if ((tcsNatureOfIncome.getApplicableTo() != null)
				&& (tcsNatureOfIncome.getApplicableFrom().equals(tcsNatureOfIncome.getApplicableTo())
						|| tcsNatureOfIncome.getApplicableFrom().isAfter(tcsNatureOfIncome.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be Greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		MasterESAPIValidation.natureOfIncomeInputValidation(tcsNatureOfIncome);
		TCSNatureOfIncome response = tcsNatureOfIncomeService.save(tcsNatureOfIncome, userName);
		ApiStatus<TCSNatureOfIncome> apiStatus = new ApiStatus<TCSNatureOfIncome>(HttpStatus.CREATED,
				"To create a record for Nature of income", "Created record of Nature Of income", response);
		return new ResponseEntity<ApiStatus<TCSNatureOfIncome>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method is used to update Nature of Payment record
	 * 
	 * @param tcsNatureOfIncome
	 * @return
	 * @throws JsonProcessingException
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 * @throws FieldValidator
	 * @throws RecordNotFoundException
	 */
	@PutMapping(value = "/tcs/nature-of-income", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSNatureOfIncome>> updateNatureOfPaymentMaster(
			@Valid @RequestBody TCSNatureOfIncome tcsNatureOfIncome, @RequestHeader("X-USER-EMAIL") String userName)
			throws JsonProcessingException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (tcsNatureOfIncome.getId() == null) {
			throw new CustomException("Cannot update Nature Of Payment if Id is null", HttpStatus.BAD_REQUEST);
		}
		if ((tcsNatureOfIncome.getApplicableTo() != null)
				&& (tcsNatureOfIncome.getApplicableFrom().equals(tcsNatureOfIncome.getApplicableTo())
						|| tcsNatureOfIncome.getApplicableFrom().isAfter(tcsNatureOfIncome.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		MasterESAPIValidation.natureOfIncomeInputValidation(tcsNatureOfIncome);
		TCSNatureOfIncome response = tcsNatureOfIncomeService.updateNatureOfPaymentMaster(tcsNatureOfIncome, userName);
		ApiStatus<TCSNatureOfIncome> apiStatus = new ApiStatus<TCSNatureOfIncome>(HttpStatus.CREATED,
				"To Update a Nature Of income", "Updated Successfully", response);

		return new ResponseEntity<ApiStatus<TCSNatureOfIncome>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method is used to get all Nature of Payment record
	 * 
	 * @return
	 * @throws RecordNotFoundException
	 */
	@GetMapping(value = "/tcs/nature-of-income", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<NatureOfPaymentMasterDTO>>> tcsFindAll() throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		List<NatureOfPaymentMasterDTO> list = tcsNatureOfIncomeService.findAll();
		logger.debug("number of NatureOfPaymentMasters : {} ", list.size());
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
	@GetMapping(value = "/tcs/nature-of-income/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>> getNatureOfPaymentMaster(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		NatureOfPaymentMasterDTO natureOfPaymentMaster = null;
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		} else {
			natureOfPaymentMaster = tcsNatureOfIncomeService.findOne(id);
		}
		ApiStatus<NatureOfPaymentMasterDTO> apiStatus = new ApiStatus<NatureOfPaymentMasterDTO>(HttpStatus.OK,
				"Request to get a Nature Of Payment Record", "List of Nature of Payments", natureOfPaymentMaster);
		return new ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	// Feign client communication based on id
	@GetMapping(value = "/tcs/nature-of-income-section/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>> getNatureOfPaymentMasterRecord(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		NatureOfPaymentMasterDTO natureOfPaymentMaster = null;
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		} else {
			natureOfPaymentMaster = tcsNatureOfIncomeService.findOne(id);
		}
		ApiStatus<NatureOfPaymentMasterDTO> apiStatus = new ApiStatus<NatureOfPaymentMasterDTO>(HttpStatus.OK,
				"Request to get a Single Nature Of Payment Record", "Single Nature Of Payment Record",
				natureOfPaymentMaster);
		return new ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/tcs/nature-of-income/section/{section}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>> getNatureOfPaymentMasterRecord(
			@PathVariable String section) {
		MultiTenantContext.setTenantId("master");
		NatureOfPaymentMasterDTO natureOfPaymentMaster = null;
		if (section == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		} else {
			natureOfPaymentMaster = tcsNatureOfIncomeService.findBySection(section);
		}
		ApiStatus<NatureOfPaymentMasterDTO> apiStatus = new ApiStatus<NatureOfPaymentMasterDTO>(HttpStatus.OK,
				"Request to get a Single Nature Of Payment Record", "Single Nature Of Payment Record",
				natureOfPaymentMaster);
		return new ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @return
	 */
	@GetMapping(value = "/tcs/residentSections")
	public ResponseEntity<ApiStatus<List<String>>> getResidentSecions() {
		MultiTenantContext.setTenantId("master");
		List<CustomNatureOfPaymentMasterDTO> list = tcsNatureOfIncomeRepository.getResidentSections();
		List<String> listOfSection = new ArrayList<String>();
		for (CustomNatureOfPaymentMasterDTO dto : list) {
			listOfSection.add(dto.getSection());
		}
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS", "Resident Sections",
				listOfSection);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call to get the nature of income id and nature for mismatch upload
	 * 
	 * @param section
	 * @param rate
	 * @return
	 */
	@GetMapping(value = "/tcs/NOI/id")
	public ResponseEntity<ApiStatus<TCSNatureOfIncome>> getNatureOfIncomeBasedOnSectionAndRate(
			@RequestParam("section") String section, @RequestParam("rate") Double rate) {
		logger.info("Feign call executing to get Nature of income data  Rates Based  on Section {}");
		MultiTenantContext.setTenantId("master");
		List<Map<String, Object>> listMap = tcsNatureOfIncomeRepository.findByRateAndSection(rate, section);
		// List<TCSNatureOfIncome> response=new ArrayList<>();
		TCSNatureOfIncome nature = new TCSNatureOfIncome();
		if (!listMap.isEmpty()) {

			BigInteger id = (BigInteger) listMap.get(0).get("id");
			nature.setId(id.longValue());
			nature.setNature((String) listMap.get(0).get("nature"));
			// response.add(nature);
		}
		ApiStatus<TCSNatureOfIncome> apiStatus = new ApiStatus<TCSNatureOfIncome>(HttpStatus.OK, "SUCCESS",
				"Nature Of Income Data", nature);
		logger.info("Nture of Income Data retrieved {}", nature);
		return new ResponseEntity<ApiStatus<TCSNatureOfIncome>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/tcs/rate/by/section")
	public ResponseEntity<ApiStatus<List<Double>>> getRatesBasaedOnSection(@RequestParam("section") String section) {
		logger.info("Feign call executing to get List Of Rates Based  on Section {}");
		MultiTenantContext.setTenantId("master");
		List<Double> list = tcsNatureOfIncomeRepository.findRatesBasedOnSection(section);

		ApiStatus<List<Double>> apiStatus = new ApiStatus<List<Double>>(HttpStatus.OK, "SUCCESS",
				"Nature Of Income Data", list);
		logger.info("Retrieved List of Rates {}", list);
		return new ResponseEntity<ApiStatus<List<Double>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/tcs/rate/section")
	public ResponseEntity<ApiStatus<List<CustomSectionRateDTO>>> findSectionRates() {
		logger.info("Feign call executing to get List Of Rates Based  on Section {}");
		MultiTenantContext.setTenantId("master");
		List<Map<String, Object>> list = tcsNatureOfIncomeRepository.findSectionRates();

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
			dtoList.add(customSectionRateDTO);
		}

		ApiStatus<List<CustomSectionRateDTO>> apiStatus = new ApiStatus<List<CustomSectionRateDTO>>(HttpStatus.OK,
				"SUCCESS", "Nature Of Income Data", dtoList);
		logger.info("Retrieved List of Rates {}", dtoList);
		return new ResponseEntity<ApiStatus<List<CustomSectionRateDTO>>>(apiStatus, HttpStatus.OK);
	}

}
