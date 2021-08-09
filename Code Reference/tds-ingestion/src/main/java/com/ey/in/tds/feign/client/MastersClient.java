package com.ey.in.tds.feign.client;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.ey.in.tds.common.domain.CollecteeExempt;
import com.ey.in.tds.common.domain.ErrorCode;
import com.ey.in.tds.common.domain.TdsMaster;
import com.ey.in.tds.common.domain.tcs.TCSNatureOfIncome;
import com.ey.in.tds.common.dto.CustomSectionRateDTO;
import com.ey.in.tds.common.dto.CustomThresholdGroupLimitDto;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.SurchargeAndCessDTO;
import com.ey.in.tds.common.dto.tdsmonthlytracker.MonthTrackerDTO;
import com.ey.in.tds.core.util.ApiStatus;

import io.micrometer.core.annotation.Timed;

@FeignClient(url = "${feign.masters.app.url}", name = "masters")
public interface MastersClient {

	// Getting deductor tenant information
	@GetMapping(value = "/getdeductortenant")
	public String getDeductorTenant(@RequestParam("tenantName") String tenantName,
			@RequestHeader(value = "X-TENANT-ID") String tenantId);

	// Get All Sections from DB
	@GetMapping(value = "/tcs/nature-of-income", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<NatureOfPaymentMasterDTO>>> getTcsNatureOfPayment();

	@GetMapping("/getnatureofpaymentsections")
	public ResponseEntity<ApiStatus<List<String>>> findAllNatureOfPaymentSections();

	@GetMapping(value = "/tds/bysection", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<TdsMaster>>> getTdsMasterBySection(@RequestParam("section") String section,
			@RequestParam("nopId") int nopId, @RequestParam("residentialStatus") String residentialStatus,
			@RequestParam("status") String status);

	@GetMapping("/articlemaster")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getArticleMasterData();

	// To Get Pan Name based on pan code
	@GetMapping("/getpanname/{code}")
	public ResponseEntity<String> getPanNameBasedOnPanCode(@PathVariable(value = "code") String code);

	@GetMapping("/monthly-tracker/{year}/{month}")
	public ResponseEntity<ApiStatus<MonthTrackerDTO>> findByAssessmentYearMonth(@PathVariable Integer year,
			@PathVariable Integer month);

	@GetMapping("/monthly-tracker/challanGeneratedDate")
	public ResponseEntity<ApiStatus<MonthTrackerDTO>> findByDueDateChallanPayment(
			@RequestParam(value = "challanGeneratedDate") String challanGeneratedDate);

	@GetMapping(value = "/nature-of-payment", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<NatureOfPaymentMasterDTO>>> findAll();

	// feign client for tcs

	@GetMapping(value = "/tcs/nature-of-income", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<NatureOfPaymentMasterDTO>>> tcsFindAll();

	// feign call to get error codes
	@GetMapping("/allErrorCode")
	@Timed
	public ResponseEntity<ApiStatus<List<ErrorCode>>> getAllErrorCodes();

	@GetMapping("get/collecteeExempt")
	public ResponseEntity<ApiStatus<List<CollecteeExempt>>> getCollecteeExempt(
			@RequestParam("collecteeStatus") String collecteeStatus, @RequestParam("section") String section);

	@GetMapping("get/collecteeExempt/all")
	public ResponseEntity<ApiStatus<List<CollecteeExempt>>> getCollecteeExemptAll();

	@GetMapping(value = "/tcs/rate/by/section")
	public ResponseEntity<ApiStatus<List<Double>>> getRatesBasaedOnSection(@RequestParam("section") String section);

	@GetMapping(value = "/tcs/rate/section")
	public ResponseEntity<ApiStatus<List<CustomSectionRateDTO>>> findSectionRates();

	@GetMapping(value = "/tcs/NOI/id")
	public ResponseEntity<ApiStatus<TCSNatureOfIncome>> getNatureOfIncomeBasedOnSectionAndRate(
			@RequestParam("section") String section, @RequestParam("rate") Double rate);

	@GetMapping("/surchargedetails")
	public ResponseEntity<ApiStatus<List<SurchargeAndCessDTO>>> getSurchargeDetailsBySectionDeducteeStatus(
			@RequestParam(value = "section") String section,
			@RequestParam(value = "deducteeStatus") String deducteeStatus,
			@RequestParam(value = "residentialStatus") String residentialStatus);

	@GetMapping("/cessdetails/cesstype")
	public ResponseEntity<ApiStatus<List<SurchargeAndCessDTO>>> getCessDetailsByCessType(
			@RequestParam(value = "cessType") String cessType);

	@GetMapping(value = "/get/all/currencydata")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getAllCurrencyData();

	@GetMapping(value = "/section/rate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<CustomSectionRateDTO>>> getNatureOfPaymentMasterRecord();

	@GetMapping(value = "get/threshold/limit/group", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<CustomThresholdGroupLimitDto>>> getThresholdLimitGroup();

	@GetMapping("/status")
	public ResponseEntity<Map<String, String>> getAllDeducteeStatus();

	@GetMapping(value = "/sections/basedon/residentialStatus")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getSectionAndDeducteeStatusBasedOnStatus(
			@RequestParam("residentialStatus") String residentalStatus);

	@GetMapping("/monthly-tracker/data")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getAllTdsMonthTrackerData();

	// Feign client
	@GetMapping("/surchargedetails/by/residentialstatus")
		public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getAllSurchargeDetails(
				@RequestParam(value = "residentialStatus") String residentialStatus);

}