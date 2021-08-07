package com.ey.in.tds.web.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.Classification;
import com.ey.in.tds.common.domain.CollecteeExempt;
import com.ey.in.tds.common.domain.Country;
import com.ey.in.tds.common.domain.ErrorCode;
import com.ey.in.tds.common.domain.FilingDeductorCollector;
import com.ey.in.tds.common.domain.ModeOfPayment;
import com.ey.in.tds.common.domain.Tan;
import com.ey.in.tds.common.domain.dividend.DividendDeductorType;
import com.ey.in.tds.common.domain.dividend.DividendInstrumentsMapping;
import com.ey.in.tds.common.domain.dividend.DividendRateTreaty;
import com.ey.in.tds.common.domain.dividend.ShareholderCategory;
import com.ey.in.tds.common.domain.dividend.ShareholderExemptedCategory;
import com.ey.in.tds.common.domain.dividend.ShareholderType;
import com.ey.in.tds.common.domain.transactions.jdbc.tcs.dto.TCSStdCodesAndCitys;
import com.ey.in.tds.common.dto.CustomNatureDTO;
import com.ey.in.tds.common.dto.CustomNatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.NatureAndRateDto;
import com.ey.in.tds.common.dto.NatureAndSectionDTO;
import com.ey.in.tds.common.dto.NatureAndTaxRateDTO;
import com.ey.in.tds.common.dto.SectionNatureDTO;
import com.ey.in.tds.common.repository.ClassificationRepository;
import com.ey.in.tds.common.repository.CollecteeExemptRepository;
import com.ey.in.tds.common.service.ErrorCodeService;
import com.ey.in.tds.common.tcs.repository.TCSNatureOfIncomeRepository;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.dto.CessTypesDTO;
import com.ey.in.tds.dto.ResidentialStatusDTO;
import com.ey.in.tds.dto.StateDTO;
import com.ey.in.tds.dto.StatusDTO;
import com.ey.in.tds.dto.SubNaturePaymentMasterDTO;
import com.ey.in.tds.service.CommonAPIService;
import com.ey.in.tds.service.FilingDeductorCollectorService;
import com.ey.in.tds.service.StateService;
import com.ey.in.tds.tcs.repository.TCSStdCodesRepository;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/masters")
public class CommonAPIResource extends BaseResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final StateService stateService;

	@Autowired
	private final CommonAPIService commonAPIService;

	@Autowired
	private FilingDeductorCollectorService filingDeductorCollectorService;

	@Autowired
	private TCSNatureOfIncomeRepository tcsNatureOfIncomeRepository;

	@Autowired
	private ErrorCodeService errorCodeService;

	@Autowired
	private ClassificationRepository classificationRepository;

	@Autowired
	private TCSStdCodesRepository tcsStdCodesRepository;

	@Autowired
	private CollecteeExemptRepository collecteeExemptRepository;

	public CommonAPIResource(StateService stateService, CommonAPIService commonAPIService) {
		this.stateService = stateService;
		this.commonAPIService = commonAPIService;
	}

	@GetMapping("/getallstates")
	@Timed
	public ResponseEntity<ApiStatus<List<StateDTO>>> getAllStates() {
		MultiTenantContext.setTenantId("master");
		List<StateDTO> allStates = stateService.getStates();
		ApiStatus<List<StateDTO>> apiStatus = new ApiStatus<List<StateDTO>>(HttpStatus.OK, "SUCCESS",
				"GET ALL LDC MASTER RECORD ", allStates);
		return new ResponseEntity<ApiStatus<List<StateDTO>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/getmodeofpayment")
	@Timed
	public ResponseEntity<ApiStatus<List<ModeOfPayment>>> getModeofpayments() {
		MultiTenantContext.setTenantId("master");
		List<ModeOfPayment> modeOfPaymentList = commonAPIService.findAllModeOfPayments();
		ApiStatus<List<ModeOfPayment>> apiStatus = new ApiStatus<List<ModeOfPayment>>(HttpStatus.OK, "SUCCESS",
				"LIST OF MODE OF PAYMENT RECORDS ", modeOfPaymentList);
		return new ResponseEntity<ApiStatus<List<ModeOfPayment>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping("/getdeductortype")
	@Timed
	public ResponseEntity<ApiStatus<List<FilingDeductorCollector>>> getDeductortype() {
		MultiTenantContext.setTenantId("master");
		List<FilingDeductorCollector> deductorTypeList = filingDeductorCollectorService.getAllDeductorCollector();
		ApiStatus<List<FilingDeductorCollector>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"LIST OF DEDUCTOR TYPE RECORDS ", deductorTypeList);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);

	}

	@GetMapping("/getresidentialstatus")
	@Timed
	public ResponseEntity<ApiStatus<List<ResidentialStatusDTO>>> getResidentialstatus() {
		MultiTenantContext.setTenantId("master");
		List<ResidentialStatusDTO> residentialStatusList = commonAPIService.findAllResidentialStatuses();
		ApiStatus<List<ResidentialStatusDTO>> apiStatus = new ApiStatus<List<ResidentialStatusDTO>>(HttpStatus.OK,
				"SUCCESS", "LIST OF RESIDENTAIL STATUS RECORDS ", residentialStatusList);
		return new ResponseEntity<ApiStatus<List<ResidentialStatusDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * This api returns the list of id,nature and section
	 * 
	 * @return
	 */
	@GetMapping("/getnatureofpayment")
	public ResponseEntity<ApiStatus<List<CustomNatureDTO>>> getNatureofpayment() {
		MultiTenantContext.setTenantId("master");
		info("REST request to get nature and section list");
		List<CustomNatureDTO> natureOfPaymentList = commonAPIService.findAllNatureOfPaymentMaster();
		ApiStatus<List<CustomNatureDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"LIST OF NATURE OF PAYMENT RECORDS", natureOfPaymentList);
		return new ResponseEntity<ApiStatus<List<CustomNatureDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * This api returns the list of sections
	 * 
	 * @return
	 */
	@GetMapping("/getnatureofpaymentsections")
	public ResponseEntity<ApiStatus<List<String>>> getnatureofpaymentsections() {

		info("REST request to get nature and section list");
		MultiTenantContext.setTenantId("master");
		List<String> natureOfPaymentList = commonAPIService.findAllNatureOfPaymentSections();
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS",
				"LIST OF NATURE OF PAYMENT SECTIONS", natureOfPaymentList);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping("/getsubnatureofpayment")
	@Timed
	public ResponseEntity<ApiStatus<List<SubNaturePaymentMasterDTO>>> getSubNatureofpayment() {
		MultiTenantContext.setTenantId("master");
		List<SubNaturePaymentMasterDTO> subNatureOfPaymentList = commonAPIService.findAllSubNatureOfPaymentMaster();
		ApiStatus<List<SubNaturePaymentMasterDTO>> apiStatus = new ApiStatus<List<SubNaturePaymentMasterDTO>>(
				HttpStatus.OK, "SUCCESS", "LIST OF SUB-NATURE OF PAYMENT RECORDS", subNatureOfPaymentList);
		return new ResponseEntity<ApiStatus<List<SubNaturePaymentMasterDTO>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping("/getstatus")
	@Timed
	public ResponseEntity<ApiStatus<List<StatusDTO>>> getAllStatus() {
		MultiTenantContext.setTenantId("master");
		List<StatusDTO> statusList = commonAPIService.getAllStatus();
		ApiStatus<List<StatusDTO>> apiStatus = new ApiStatus<List<StatusDTO>>(HttpStatus.OK, "SUCCESS",
				"LIST OF STATUS RECORDS", statusList);
		return new ResponseEntity<ApiStatus<List<StatusDTO>>>(apiStatus, HttpStatus.OK);

	}
	
	//Feign client
	@GetMapping("/status")
	public ResponseEntity<Map<String, String>> getAllDeducteeStatus() {
		MultiTenantContext.setTenantId("master");
		Map<String, String> statusList = commonAPIService.getDeducteeStatus();
		return new ResponseEntity<Map<String, String>>(statusList, HttpStatus.OK);
	}

	@GetMapping("/getcesstypes")
	@Timed
	public ResponseEntity<ApiStatus<List<CessTypesDTO>>> getCessTypeMasters() {
		MultiTenantContext.setTenantId("master");
		List<CessTypesDTO> cessTypeMasters = commonAPIService.getCessTypeMasters();
		ApiStatus<List<CessTypesDTO>> apiStatus = new ApiStatus<List<CessTypesDTO>>(HttpStatus.OK, "SUCCESS",
				"LIST OF CESS TYPE RECORDS", cessTypeMasters);
		return new ResponseEntity<ApiStatus<List<CessTypesDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * 
	 * @return
	 */
	@GetMapping("/getcountries")
	@Timed
	public ResponseEntity<ApiStatus<List<Country>>> getCountries() {
		MultiTenantContext.setTenantId("master");
		List<Country> countries = commonAPIService.getCountries();
		ApiStatus<List<Country>> apiStatus = new ApiStatus<List<Country>>(HttpStatus.OK, "SUCCESS",
				"LIST OF COUNTRY RECORDS ", countries);
		return new ResponseEntity<ApiStatus<List<Country>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * This api for get all citys and std codes based on state.
	 * 
	 * @return
	 */
	@GetMapping("/getcitys/{state}")
	public ResponseEntity<ApiStatus<List<TCSStdCodesAndCitys>>> getCitys(@PathVariable String state) {
		MultiTenantContext.setTenantId("master");
		List<TCSStdCodesAndCitys> stdCodes = tcsStdCodesRepository.findAllCitysAndCodes(state);
		ApiStatus<List<TCSStdCodesAndCitys>> apiStatus = new ApiStatus<List<TCSStdCodesAndCitys>>(HttpStatus.OK,
				"SUCCESS", "LIST OF CITY AND STD CODES RECORDS ", stdCodes);
		return new ResponseEntity<ApiStatus<List<TCSStdCodesAndCitys>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * This api for get state based on country.
	 * 
	 * @param state
	 * @return
	 */
	@GetMapping("/getstates/{country}")
	public ResponseEntity<ApiStatus<List<String>>> getStates(@PathVariable String country) {
		MultiTenantContext.setTenantId("master");
		List<String> stdCodes = commonAPIService.findAllStates(country);
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS",
				"LIST OF STATES RECORDS ", stdCodes);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping("/gettans")
	public ResponseEntity<ApiStatus<List<Tan>>> getTans(
			@RequestHeader(value = "DEDUCTOR-ID", required = true) Long deductorId) {
		MultiTenantContext.setTenantId("master");
		List<Tan> listTan = commonAPIService.getTan(deductorId);
		ApiStatus<List<Tan>> apiStatus = new ApiStatus<List<Tan>>(HttpStatus.OK, "SUCCESS", "LIST OF TAN RECORDS ",
				listTan);
		return new ResponseEntity<ApiStatus<List<Tan>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * This api returns deductor name based on tenant name
	 * 
	 * @param tenantName
	 * @return
	 */
	@GetMapping("/getdeductorname")
	public ResponseEntity<ApiStatus<String>> getDeductorName(@RequestHeader("tenantName") String tenantName) {
		MultiTenantContext.setTenantId("master");
		String deductorName = commonAPIService.getDeductorName(tenantName);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"Deductor Name based on tenant Name", deductorName);

		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns the list of sections related to NR
	 * 
	 * @return
	 */
	@GetMapping("/getNonResidentSections")
	public ResponseEntity<ApiStatus<List<CustomNatureOfPaymentMasterDTO>>> getNonResidentSections() {
		MultiTenantContext.setTenantId("master");
		List<CustomNatureOfPaymentMasterDTO> natureOfPaymentList = commonAPIService.getNonResidentSections();
		ApiStatus<List<CustomNatureOfPaymentMasterDTO>> apiStatus = new ApiStatus<List<CustomNatureOfPaymentMasterDTO>>(
				HttpStatus.OK, "SUCCESS", "List of sections for non resident", natureOfPaymentList);
		return new ResponseEntity<ApiStatus<List<CustomNatureOfPaymentMasterDTO>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping("/getResidentSections")
	public ResponseEntity<ApiStatus<List<CustomNatureOfPaymentMasterDTO>>> getResidentSections() {
		MultiTenantContext.setTenantId("master");
		List<CustomNatureOfPaymentMasterDTO> natureOfPaymentList = commonAPIService.getResidentSections();
		ApiStatus<List<CustomNatureOfPaymentMasterDTO>> apiStatus = new ApiStatus<List<CustomNatureOfPaymentMasterDTO>>(
				HttpStatus.OK, "SUCCESS", "List of sections for Resident", natureOfPaymentList);
		return new ResponseEntity<ApiStatus<List<CustomNatureOfPaymentMasterDTO>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping("/getpanname/{code}")
	public ResponseEntity<String> getPanNameBasedOnPanCode(@PathVariable String code) {
		MultiTenantContext.setTenantId("master");
		String panName = null;
		if (code != null) {
			panName = commonAPIService.getPanNameByPanCode(code);
		}
		return new ResponseEntity<String>(panName, HttpStatus.OK);
	}

	/**
	 * This API for get all deductee status.
	 */
	@GetMapping("/deductee/status")
	public ResponseEntity<ApiStatus<List<String>>> getDeducteeStatus() {
		MultiTenantContext.setTenantId("master");
		List<String> code = commonAPIService.getAllDeducteeStatus();
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS",
				"LIST OF COUNTRY CODE ", code);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);

	}
	
	/**
	 * This API for get all country code.
	 */
	@GetMapping("/country/currency")
	public ResponseEntity<ApiStatus<List<String>>> getCountryCodes() {
		MultiTenantContext.setTenantId("master");
		List<String> code = commonAPIService.getCountryCodes();
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS",
				"LIST OF COUNTRY CODE ", code);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping("/sections/based/on/{deducteeStatus}/{deducteeResidentialStatus}")
	public ResponseEntity<ApiStatus<List<SectionNatureDTO>>> getSectionsforResidentStatus(
			@PathVariable String deducteeStatus, @PathVariable String deducteeResidentialStatus) {
		MultiTenantContext.setTenantId("master");
		if (commonAPIService.isNull(deducteeStatus) && commonAPIService.isNull(deducteeResidentialStatus)) {
			throw new CustomException("Please select atleast one Status", HttpStatus.BAD_REQUEST);
		}

		List<SectionNatureDTO> sections = commonAPIService.getStatusbasedOnStatusAndResidentialStatus(deducteeStatus,
				deducteeResidentialStatus);

		ApiStatus<List<SectionNatureDTO>> apiStatus = new ApiStatus<List<SectionNatureDTO>>(HttpStatus.OK, "SUCCESS",
				"LIST OF SECTIONS RETRIEVING SUCCESSFULLY", sections);
		return new ResponseEntity<ApiStatus<List<SectionNatureDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get Nature based on Section and residentalStatus and
	 * deducteeStatus.
	 * 
	 * @param section
	 * @return
	 */
	@GetMapping("nop/based/on/{section}/{residentalStatus}/{deducteeStatus}")
	public ResponseEntity<ApiStatus<List<String>>> getNopBasedOnSection(
			@PathVariable(value = "section", required = true) String section,
			@PathVariable(value = "residentalStatus", required = true) String residentalStatus,
			@PathVariable(value = "deducteeStatus", required = true) String deducteeStatus) {
		MultiTenantContext.setTenantId("master");
		List<String> sections = commonAPIService.getNopBasedOnSection(section, residentalStatus, deducteeStatus);
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS", "NO ALERT", sections);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
	}
	
	@GetMapping("nop/based/on/edit/{section}/{residentalStatus}/{deducteeStatus}")
	public ResponseEntity<ApiStatus<List<String>>> getNopBasedOnSectionForEdit(
			@PathVariable(value = "section", required = true) String section,
			@PathVariable(value = "residentalStatus", required = true) String residentalStatus,
			@PathVariable(value = "deducteeStatus", required = true) String deducteeStatus) {
		MultiTenantContext.setTenantId("master");
		List<String> sections = commonAPIService.getNopBasedOnSectionForEdit(section, residentalStatus, deducteeStatus);
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS", "NO ALERT", sections);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("nop/sections")
	public ResponseEntity<ApiStatus<List<String>>> getAllSection() {
		MultiTenantContext.setTenantId("master");
		List<String> sections = commonAPIService.getAllSection();
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS", "NO ALERT", sections);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("nop/sections/{section}")
	public ResponseEntity<ApiStatus<List<String>>> getAllNopBasedOnSection(
			@PathVariable(value = "section", required = true) String section) {
		MultiTenantContext.setTenantId("master");
		List<String> nops = commonAPIService.getAllNopBasedOnSection(section);
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS", "NO ALERT", nops);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @return
	 */
	@GetMapping("/tcs/getcesstypes")
	@Timed
	public ResponseEntity<ApiStatus<List<CessTypesDTO>>> getTcsCessTypeMasters() {
		MultiTenantContext.setTenantId("master");
		List<CessTypesDTO> cessTypeMasters = commonAPIService.getTcsCessTypeMasters();
		ApiStatus<List<CessTypesDTO>> apiStatus = new ApiStatus<List<CessTypesDTO>>(HttpStatus.OK, "SUCCESS",
				"LIST OF CESS TYPE RECORDS", cessTypeMasters);
		return new ResponseEntity<ApiStatus<List<CessTypesDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * This API for get Nature based on collectee status and collectee indicator.
	 * 
	 * @param collecteeStatus
	 * @param collecteeIndicator
	 * @return
	 */
	@GetMapping("/tcs/sections/based/on/{collecteeStatus}/{collecteeIndicator}")
	public ResponseEntity<ApiStatus<List<SectionNatureDTO>>> getTcsSectionsforResidentStatus(
			@PathVariable String collecteeStatus, @PathVariable Boolean collecteeIndicator) {
		MultiTenantContext.setTenantId("master");
		List<SectionNatureDTO> sections = commonAPIService.getSectionAndNOI(collecteeStatus, collecteeIndicator);
		ApiStatus<List<SectionNatureDTO>> apiStatus = new ApiStatus<List<SectionNatureDTO>>(HttpStatus.OK, "SUCCESS",
				"LIST OF SECTIONS RETRIEVING SUCCESSFULLY", sections);
		return new ResponseEntity<ApiStatus<List<SectionNatureDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param section
	 * @param collecteeIndicator
	 * @param collecteeStatus
	 * @return
	 */
	@GetMapping("/tcs/noi/based/on/{section}/{collecteeIndicator}/{collecteeStatus}")
	public ResponseEntity<ApiStatus<List<String>>> getTcsNoiBasedOnSection(
			@PathVariable(value = "section", required = true) String section,
			@PathVariable(value = "collecteeIndicator", required = true) Boolean collecteeIndicator,
			@PathVariable(value = "collecteeStatus", required = true) String collecteeStatus) {
		MultiTenantContext.setTenantId("master");
		List<String> sections = commonAPIService.getTcsNoiBasedOnSection(section, collecteeIndicator, collecteeStatus);
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS", "NO ALERT", sections);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
	}

	// feign client for tcs

	/**
	 * 
	 * @param section
	 * @param deducteeStatus
	 * @param residentStatus
	 * @return
	 */
	@GetMapping(value = "/tcs/section/deducteeStatus/residentStatus")
	public ResponseEntity<ApiStatus<List<NatureAndTaxRateDTO>>> getTcsListOfNatureAndRate(
			@RequestParam("section") String section, @RequestParam("residentStatus") String residentStatus) {
		logger.info("Inside the API for getting Nature and Rate");
		MultiTenantContext.setTenantId("master");
		List<NatureAndRateDto> list = tcsNatureOfIncomeRepository.getTcsListOfNatureAndRate(section);
		List<NatureAndTaxRateDTO> returnList = new ArrayList<>();
		for (NatureAndRateDto natureAndRateDto : list) {
			NatureAndTaxRateDTO rateDTO = new NatureAndTaxRateDTO();
			rateDTO.setNature(natureAndRateDto.getNature());
			rateDTO.setRate(natureAndRateDto.getRate());
			returnList.add(rateDTO);
		}

		ApiStatus<List<NatureAndTaxRateDTO>> apiStatus = new ApiStatus<List<NatureAndTaxRateDTO>>(HttpStatus.OK,
				"SUCCESS", "Resident Sections", returnList);
		logger.info("Completed the API call before the return - Getting Nature and Rate");
		return new ResponseEntity<ApiStatus<List<NatureAndTaxRateDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param section
	 * @param residentStatus
	 * @return
	 */
	@GetMapping(value = "/tcs/section/residentStatus")
	public ResponseEntity<ApiStatus<List<String>>> getNOIBasedOnSectionAndResidentialStatus(
			@RequestParam("section") String section, @RequestParam("residentStatus") String residentStatus) {
		logger.info("Inside the API for getNOIBasedOnSectionAndResidentialStatus");
		MultiTenantContext.setTenantId("master");
		List<String> list = tcsNatureOfIncomeRepository.getNOIBasedOnSectionAndResidentialStatus(section);
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS", "Resident Sections",
				list);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param residentalStatus
	 * @param section
	 * @param deducteeStatus
	 * @return
	 */
	@GetMapping(value = "/tcs/basedon/section/deducteeStatus/ResidentStatus")
	public ResponseEntity<ApiStatus<Boolean>> getNOIBasedOnStatusAndSectionResidentStatus(
			@RequestParam("ResidentStatus") String residentalStatus, @RequestParam("section") String section) {
		MultiTenantContext.setTenantId("master");
		Optional<SectionNatureDTO> response = tcsNatureOfIncomeRepository
				.getNOIBasedOnStatusAndSectionResidentStatus(section);
		Boolean isSectionFound = false;
		if (response.isPresent()) {
			isSectionFound = true;
		}
		ApiStatus<Boolean> apiStatus = new ApiStatus<Boolean>(HttpStatus.OK, "SUCCESS", "SUCCESS", isSectionFound);
		return new ResponseEntity<ApiStatus<Boolean>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @return
	 */
	@GetMapping("/tcs/getnatureofincome")
	public ResponseEntity<ApiStatus<List<CustomNatureOfPaymentMasterDTO>>> getTcsNatureofIncome() {
		MultiTenantContext.setTenantId("master");
		info("REST request to get nature and section list");
		List<CustomNatureOfPaymentMasterDTO> natureOfPaymentList = commonAPIService.findAllTcsNatureOfIncome();
		ApiStatus<List<CustomNatureOfPaymentMasterDTO>> apiStatus = new ApiStatus<List<CustomNatureOfPaymentMasterDTO>>(
				HttpStatus.OK, "SUCCESS", "LIST OF NATURE OF INCOME RECORDS", natureOfPaymentList);
		return new ResponseEntity<ApiStatus<List<CustomNatureOfPaymentMasterDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * This api for get collectee type
	 * 
	 * @return
	 */
	@GetMapping("/tcs/getcollecteetype")
	public ResponseEntity<ApiStatus<String>> getTcsCollecteeType(
			@RequestParam("collecteeStatus") String collecteeStatus) {
		MultiTenantContext.setTenantId("master");
		info("REST request to get collectee type");
		String collecteeType = commonAPIService.getCollecteeType(collecteeStatus);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"LIST OF NATURE OF INCOME RECORDS", collecteeType);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping("/allErrorCode")
	@Timed
	public ResponseEntity<ApiStatus<List<ErrorCode>>> getAllErrorCodes()
			throws JsonParseException, JsonMappingException, IOException {
		MultiTenantContext.setTenantId("master");
		if (logger.isDebugEnabled())
			logger.debug("Fetching error code details for : all");
		List<ErrorCode> errorCodes = errorCodeService.findAll();
		ApiStatus<List<ErrorCode>> apiStatus = new ApiStatus<List<ErrorCode>>(HttpStatus.OK, "SUCCESS",
				"GOT ERROR DETAILS OBJECTS SUCCESSFULLY", errorCodes);
		return new ResponseEntity<ApiStatus<List<ErrorCode>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call to return classifiacation to onbording
	 * 
	 * @param classification
	 * @return
	 */
	@GetMapping("/get/classification")
	public ResponseEntity<ApiStatus<Optional<Classification>>> findByClassificationCode(
			@RequestParam("Classification") String classification) {
		MultiTenantContext.setTenantId("master");
		logger.debug("Feign call executing to get classifiacation {}");
		Optional<Classification> optional = classificationRepository.findByClassificationCode(classification);
		logger.debug("Retrieved classification  {}", classification);
		ApiStatus<Optional<Classification>> apiStatus = new ApiStatus<Optional<Classification>>(HttpStatus.OK,
				"SUCCESS", "GOT Classification DETAILS OBJECTS SUCCESSFULLY", optional);
		return new ResponseEntity<ApiStatus<Optional<Classification>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * feign call to return classifiacation to onbording
	 * 
	 * @param classification
	 * @return
	 */
	@GetMapping("/all/classification")
	public ResponseEntity<ApiStatus<List<Classification>>> getAllClassificationCode() {
		MultiTenantContext.setTenantId("master");
		logger.debug("Feign call executing to get classifiacation {}");
		List<Classification> list = classificationRepository.findAllClassifications();
		logger.debug("Retrieved classification  {}", list);
		ApiStatus<List<Classification>> apiStatus = new ApiStatus<List<Classification>>(HttpStatus.OK,
				"SUCCESS", "GET ALL Classification DETAILS OBJECTS SUCCESSFULLY", list);
		return new ResponseEntity<ApiStatus<List<Classification>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("get/collecteeExempt")
	public ResponseEntity<ApiStatus<List<CollecteeExempt>>> getCollecteeExempt(
			@RequestParam("collecteeStatus") String collecteeStatus, @RequestParam("section") String section) {
		logger.info("Feign call executing to get Collectee Exempt Data {}");
		MultiTenantContext.setTenantId("master");
		List<CollecteeExempt> list = collecteeExemptRepository
				.findCollecteExemptListBasedOnSectionAndCollecteeType(collecteeStatus, section);
		ApiStatus<List<CollecteeExempt>> apiStatus = new ApiStatus<List<CollecteeExempt>>(HttpStatus.OK, "SUCCESS",
				"GOT Collectee Exempt DETAILS OBJECTS SUCCESSFULLY", list);
		logger.info("Collectee Exempt Data returned from Feign call {}", list);
		return new ResponseEntity<ApiStatus<List<CollecteeExempt>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("get/collecteeExempt/all")
	public ResponseEntity<ApiStatus<List<CollecteeExempt>>> getCollecteeExemptAll() {
		logger.info("Feign call executing to get Collectee Exempt Data {}");
		MultiTenantContext.setTenantId("master");
		List<CollecteeExempt> list = collecteeExemptRepository.findCollecteExemptList();
		ApiStatus<List<CollecteeExempt>> apiStatus = new ApiStatus<List<CollecteeExempt>>(HttpStatus.OK, "SUCCESS",
				"GOT Collectee Exempt DETAILS OBJECTS SUCCESSFULLY", list);
		logger.info("Collectee Exempt Data returned from Feign call {}", list);
		return new ResponseEntity<ApiStatus<List<CollecteeExempt>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/dividendDeductorTypes")
	@Timed
	public ResponseEntity<ApiStatus<List<DividendDeductorType>>> getDividendDeductorTypes() {
		MultiTenantContext.setTenantId("master");
		List<DividendDeductorType> countries = commonAPIService.getAllDividendDeductorTypes();
		ApiStatus<List<DividendDeductorType>> apiStatus = new ApiStatus<List<DividendDeductorType>>(HttpStatus.OK,
				"SUCCESS", "LIST OF DIVIDEND DEDUCTOR TYPES", countries);
		return new ResponseEntity<ApiStatus<List<DividendDeductorType>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/shareholderTypes")
	@Timed
	public ResponseEntity<ApiStatus<List<ShareholderType>>> getShareholderTypes() {
		MultiTenantContext.setTenantId("master");
		List<ShareholderType> shareholderTypes = commonAPIService.getAllShareholderTypes();
		ApiStatus<List<ShareholderType>> apiStatus = new ApiStatus<List<ShareholderType>>(HttpStatus.OK, "SUCCESS",
				"LIST OF SHAREHOLDER TYPES", shareholderTypes);
		return new ResponseEntity<ApiStatus<List<ShareholderType>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/shareholderCategories")
	@Timed
	public ResponseEntity<ApiStatus<List<ShareholderCategory>>> getShareholderCategories() {
		MultiTenantContext.setTenantId("master");
		List<ShareholderCategory> countries = commonAPIService.getAllShareholderCategories(Optional.empty());
		ApiStatus<List<ShareholderCategory>> apiStatus = new ApiStatus<List<ShareholderCategory>>(HttpStatus.OK,
				"SUCCESS", "LIST OF SHAREHOLDER CATEGORIES", countries);
		return new ResponseEntity<ApiStatus<List<ShareholderCategory>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/dividendInstrumentsMappings")
	@Timed
	public ResponseEntity<ApiStatus<List<DividendInstrumentsMapping>>> getDividendInstrumentsMappings(
			@ApiParam(name = "dividendDeductorTypeId", value = "Dividend Deductor Type Id", example = "2") @RequestParam(value = "dividendDeductorTypeId", required = false) final Long dividendDeductorTypeId,
			@ApiParam(name = "shareholderCategoryId", value = "Shareholder Category Id", example = "3") @RequestParam(value = "shareholderCategoryId", required = false) final Long shareholderCategoryId,
			@ApiParam(name = "residentialStatus", value = "Residential Status: Resident or Non Resident", example = "RES", allowableValues = "RES,NR") @RequestParam(value = "residentialStatus", required = false) final String residentialStatus) {
		MultiTenantContext.setTenantId("master");
		List<DividendInstrumentsMapping> mappings = commonAPIService
				.getAllDividendInstrumentsMapping(dividendDeductorTypeId, shareholderCategoryId, residentialStatus);
		ApiStatus<List<DividendInstrumentsMapping>> apiStatus = new ApiStatus<List<DividendInstrumentsMapping>>(
				HttpStatus.OK, "SUCCESS", "LIST OF DIVIDEND INSTRUMENTS MAPPING", mappings);
		return new ResponseEntity<ApiStatus<List<DividendInstrumentsMapping>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/exemptedshareholderCategories")
	@Timed
	public ResponseEntity<ApiStatus<List<ShareholderExemptedCategory>>> getShareholderExemptedCategories() {
		MultiTenantContext.setTenantId("master");
		List<ShareholderExemptedCategory> exemptedCategories = commonAPIService.getAllShareholderExemptedCategories();
		ApiStatus<List<ShareholderExemptedCategory>> apiStatus = new ApiStatus<List<ShareholderExemptedCategory>>(
				HttpStatus.OK, "SUCCESS", "LIST OF Exempted SHAREHOLDER CATEGORIES", exemptedCategories);
		return new ResponseEntity<ApiStatus<List<ShareholderExemptedCategory>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/exemptedShareholderCategory")
	@Timed
	public ResponseEntity<ApiStatus<Boolean>> getShareholderExemptedCategory(
			@RequestParam(value = "dividendDeductorType", required = false) final String dividendDeductorType,
			@RequestParam(value = "shareholderCategory", required = false) final String shareholderCategory) {
		MultiTenantContext.setTenantId("master");
		Boolean exemptedCategory = commonAPIService.getShareholderExemptedCategory(dividendDeductorType,
				shareholderCategory);
		ApiStatus<Boolean> apiStatus = new ApiStatus<Boolean>(HttpStatus.OK, "SUCCESS", "Exempted Category",
				exemptedCategory);
		return new ResponseEntity<ApiStatus<Boolean>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping("/getTreatyByCountry")
	public ResponseEntity<ApiStatus<DividendRateTreaty>> getTreatyBenefit(
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId, @RequestBody Country country) {
		MultiTenantContext.setTenantId("master");
		DividendRateTreaty code = commonAPIService.getDividendRateByCountryId(country);
		ApiStatus<DividendRateTreaty> apiStatus = new ApiStatus<DividendRateTreaty>(HttpStatus.OK, "SUCCESS",
				"LIST OF COUNTRY CODE ", code);
		return new ResponseEntity<ApiStatus<DividendRateTreaty>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/sectionsByResidentialStatus")
	@Timed
	public ResponseEntity<ApiStatus<List<String>>> getSectionByResidentialStatus(
			@ApiParam(name = "residentialStatus", value = "Residential Status: Resident or Non Resident", example = "RES", allowableValues = "RES,NR") @RequestParam(value = "residentialStatus", required = false) final String residentialStatus) {
		MultiTenantContext.setTenantId("master");
		List<DividendInstrumentsMapping> mappings = commonAPIService.getAllDividendInstrumentsMapping(null, null,
				residentialStatus);
		List<String> sections = mappings.stream().map(DividendInstrumentsMapping::getSection).distinct().sorted()
				.collect(Collectors.toList());
		sections.addAll(Arrays.asList("195(2)", "195(3)", "197"));
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS",
				"LIST OF SECTIONS BY RESIDENTIAL STATUS", sections);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client
	 * 
	 * @param deducteeStatus
	 * @param deducteeResidentialStatus
	 * @return
	 */
	@GetMapping("/sections/nops")
	public ResponseEntity<ApiStatus<List<NatureAndSectionDTO>>> getNatureAndSection(
			@RequestParam("deducteeStatus") String deducteeStatus, @RequestParam("resStatus") String resStatus) {
		MultiTenantContext.setTenantId("master");
		List<NatureAndSectionDTO> sections = commonAPIService.getNatuesAndSections(deducteeStatus, resStatus);
		ApiStatus<List<NatureAndSectionDTO>> apiStatus = new ApiStatus<List<NatureAndSectionDTO>>(HttpStatus.OK,
				"SUCCESS", "LIST OF SECTIONS RETRIEVING SUCCESSFULLY", sections);
		return new ResponseEntity<ApiStatus<List<NatureAndSectionDTO>>>(apiStatus, HttpStatus.OK);
	}

}
