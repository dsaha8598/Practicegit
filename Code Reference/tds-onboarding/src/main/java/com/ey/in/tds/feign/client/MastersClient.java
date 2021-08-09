package com.ey.in.tds.feign.client;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.ey.in.tcs.common.TCSMasterDTO;
import com.ey.in.tds.common.domain.BasisOfCessDetails;
import com.ey.in.tds.common.domain.BasisOfSurchargeDetails;
import com.ey.in.tds.common.domain.Classification;
import com.ey.in.tds.common.domain.Country;
import com.ey.in.tds.common.domain.DeductorTanDetails;
import com.ey.in.tds.common.domain.MasterTdsHsnCode;
import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.domain.TdsMaster;
import com.ey.in.tds.common.domain.ThresholdGroupAndNopMapping;
import com.ey.in.tds.common.domain.ThresholdLimitGroupMaster;
import com.ey.in.tds.common.domain.dividend.DividendDeductorType;
import com.ey.in.tds.common.domain.dividend.DividendRateAct;
import com.ey.in.tds.common.domain.dividend.DividendRateTreaty;
import com.ey.in.tds.common.domain.dividend.ShareholderCategory;
import com.ey.in.tds.common.domain.dividend.ShareholderType;
import com.ey.in.tds.common.dto.CustomSectionRateDTO;
import com.ey.in.tds.common.dto.NatureAndSectionDTO;
import com.ey.in.tds.common.dto.NatureAndTaxRateDTO;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.challan.calculation.NopCessRateSurageRateDTO;
import com.ey.in.tds.common.dto.challan.fineratemaster.FineRateMasterDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.core.util.ApiStatus;

@FeignClient(url = "${feign.masters.app.url}", name = "masters")
public interface MastersClient {

	// Deductor Record based on Deductor Id
	@GetMapping(value = "/deductor-user/{pan}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductorMasterRecord(
			@PathVariable(value = "pan") String pan);

	// Deductor TAN Details based on Deductor Id
	@GetMapping(value = "/get-deductor-tan/{id}")
	public ResponseEntity<ApiStatus<List<DeductorTanDetails>>> getDeductorTanDetailsBasedOnDeductorId(
			@PathVariable(value = "id") Long id);

	// Nature Of Payment based on Section
	@GetMapping(value = "/nature-of-payment-section/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>> getNatureOfPaymentMasterRecord(
			@PathVariable(value = "id") Long id);

	@GetMapping(value = "/tcs/nature-of-income/section/{section}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>> getNatureOfIncomeBySection(
			@PathVariable(value = "section") String section);

	@GetMapping(value = "/tcs/rate-master/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSMasterDTO>> getRateMasterByNoiId(@PathVariable(value = "id") Long id);

	// BasisOfSurcharge
	@GetMapping(value = "/nature-of-payment-section/section/{section}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NopCessRateSurageRateDTO>> getNatureOfPaymentMasterRecordBasedonSection(
			@PathVariable(value = "section") String section);

	// BasisOfSurcharge get Surcharge Rate based on nature of Payment Id
	@GetMapping("/basis-of-surcharge-nature-of-payment/{id}")
	public ResponseEntity<ApiStatus<BasisOfSurchargeDetails>> getNatureOfPaymentBasedOnNatureOfPaymentId(
			@PathVariable(value = "id") Long id);

	// BasisOfCess get Cess Rate based on nature of Payment Id
	@GetMapping(value = "/basis-of-cess-nature-of-payment/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<BasisOfCessDetails>> getBasisOfCessNatureOfPaymentBasedonNatureOfPaymentId(
			@PathVariable(value = "id") Long id);

	// Late Filing from Fine Rate Master Table
	@GetMapping(value = "/fine-rate-section/{filing}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<FineRateMasterDTO>> getFineRateMasterBasedonLateFiling(
			@PathVariable(value = "filing") String filing);

	// Top get Deductor Record based on Deductor Id
	@GetMapping(path = "/deductor/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductorMaster(@PathVariable(value = "id") Long id);

	@GetMapping(path = "/deductor-data/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public DeductorMasterDTO getDeductorMasterData(@RequestHeader("X-TENANT-ID") String tenantId,
			@PathVariable(value = "id") Long id);

	// To Get Pan Name based on pan code
	@GetMapping("/getpanname/{code}")
	public ResponseEntity<String> getPanNameBasedOnPanCode(@PathVariable(value = "code") String code);

	@GetMapping("/getstatus")
	public ResponseEntity<Map<String, String>> getAllStatus();

	@GetMapping("/deductee/status")
	public ResponseEntity<ApiStatus<List<String>>> getDeducteeStatus();

	@GetMapping("/status")
	public ResponseEntity<Map<String, String>> getAllDeducteeStatus();

	// to get resident sections
	@GetMapping(value = "/residentSections")
	public ResponseEntity<ApiStatus<List<String>>> getResidentSecions();

	@GetMapping(value = "/residentSections/basedOn/deducteeStatus/ResidentStatus")
	public ResponseEntity<ApiStatus<Boolean>> getNOPBasedOnStatusAndSectionResidentStatus(
			@RequestParam("Reesidential-status") String residentalStatus, @RequestParam("section") String section,
			@RequestParam("deductee-status") String deducteeStatus);

	@GetMapping(value = "/section/deducteeStatus/residentStatus")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getListOfNatureAndRate(
			@RequestParam("section") String section, @RequestParam("deducteeStatus") String deducteeStatus,
			@RequestParam("residentStatus") String residentStatus);

	@GetMapping(value = "/section/deducteeStatus/")
	public ResponseEntity<ApiStatus<List<String>>> getNOPBasedOnSectionAndResidentialStatus(
			@RequestParam("section") String section, @RequestParam("residentStatus") String residentStatus);

	@GetMapping(value = "/sections/nop/basedon/residentialStatus")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getNOPAndSectionsResidentialStatus(
			@RequestParam("residentialStatus") String residentalStatus);

	@GetMapping(value = "/tds/nature", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Optional<TdsMaster>>> getTdsRateBasedOnNatureId(
			@RequestParam("natureId") Long natureId);

	@GetMapping("/threshold-group/group-id")
	public ResponseEntity<ApiStatus<Optional<ThresholdGroupAndNopMapping>>> getThresholdGroupId(
			@RequestParam(value = "natureid") Long natureid);

	@GetMapping("/threshold/group/data")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getThresholdGroupData();

	@GetMapping("/sections/nops")
	public ResponseEntity<ApiStatus<List<NatureAndSectionDTO>>> getNatureAndSection(
			@RequestParam("deducteeStatus") String deducteeStatus, @RequestParam("resStatus") String resStatus);

	@GetMapping("/tds/master/hsn")
	public ResponseEntity<ApiStatus<List<MasterTdsHsnCode>>> getAllHsnDetalis();

	@GetMapping(value = "/tds/rate/section")
	public ResponseEntity<ApiStatus<List<CustomSectionRateDTO>>> findTdsSectionRates();

	// tcs feign client

	@GetMapping(value = "/tcs/rate/section")
	public ResponseEntity<ApiStatus<List<CustomSectionRateDTO>>> findSectionRates();

	@GetMapping(value = "/tcs/basedon/section/deducteeStatus/ResidentStatus")
	public ResponseEntity<ApiStatus<Boolean>> getNOIBasedOnStatusAndSectionResidentStatus(
			@RequestParam("ResidentStatus") String residentalStatus, @RequestParam("section") String section);

	@GetMapping(value = "/tcs/section/residentStatus")
	public ResponseEntity<ApiStatus<List<String>>> getNOIBasedOnSectionAndResidentialStatus(
			@RequestParam("section") String section, @RequestParam("residentStatus") String residentStatus);

	@GetMapping(value = "/tcs/section/deducteeStatus/residentStatus")
	public ResponseEntity<ApiStatus<List<NatureAndTaxRateDTO>>> getTcsListOfNatureAndRate(
			@RequestParam("section") String section, @RequestParam("residentStatus") String residentStatus);

	@GetMapping("/tcs/getcollecteetype")
	public ResponseEntity<ApiStatus<String>> getTcsCollecteeType(
			@RequestParam("collecteeStatus") String collecteeStatus);

	// feign call to get classification
	@GetMapping("/get/classification")
	public ResponseEntity<ApiStatus<Optional<Classification>>> findByClassificationCode(
			@RequestParam("Classification") String classification);

	// feign call to get classification
	@GetMapping("/all/classification")
	public ResponseEntity<ApiStatus<List<Classification>>> findAllClassificationCode();

	@PostMapping(value = "/nature-of-payment/section")
	public ResponseEntity<ApiStatus<Optional<NatureOfPaymentMaster>>> getNOPBasedOnSectionAndNature(
			@RequestBody Map<String, String> nopMap);

	@PostMapping(value = "/nature-of-payment/nature")
	public ResponseEntity<ApiStatus<Optional<NatureOfPaymentMaster>>> getNOPBasedOnNature(
			@RequestBody Map<String, String> nopMap);

	@GetMapping(value = "/nature-of-payment", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<NatureOfPaymentMasterDTO>>> findAll();

	@GetMapping("/getnatureofpaymentsections")
	public ResponseEntity<ApiStatus<List<String>>> getnatureofpaymentsections();

	@GetMapping(value = "/tcs/nature-of-income", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<NatureOfPaymentMasterDTO>>> tcsFindAll();

	@GetMapping("/getcountries")
	public ResponseEntity<ApiStatus<List<Country>>> getCountries();

	@GetMapping("/dividendDeductorTypes")
	public ResponseEntity<ApiStatus<List<DividendDeductorType>>> getDividendDeductorTypes();

	// Get Shareholder Types
	@GetMapping("/shareholderTypes")
	public ResponseEntity<ApiStatus<List<ShareholderType>>> getShareholderTypes();

	@GetMapping("/shareholderCategories")
	public ResponseEntity<ApiStatus<List<ShareholderCategory>>> getShareholderCategories();

	@GetMapping(value = "/dividendRateActs")
	public ResponseEntity<ApiStatus<List<DividendRateAct>>> getAllDividendRateActs();

	@GetMapping(value = "/dividendRateTreaties")
	public ResponseEntity<ApiStatus<List<DividendRateTreaty>>> getAllDividendRateTreaties();

	@GetMapping("/threshold/limit/group")
	public ResponseEntity<ApiStatus<List<ThresholdLimitGroupMaster>>> getThresholdGroupByIds();

	@GetMapping("/threshold/nop/group/master")
	public ResponseEntity<ApiStatus<List<ThresholdGroupAndNopMapping>>> getThresholdNopGroupData(
			@RequestParam("nopId") Long nopId);

	@GetMapping(value = "/residentialStatus/for/all/sections", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Map<String, String>>> getResidentialStatusForSections();

	@GetMapping(value = "/residentialStatus/by/section", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> getTdsResidentialStatusBySection(@RequestParam("section") String section);

}
