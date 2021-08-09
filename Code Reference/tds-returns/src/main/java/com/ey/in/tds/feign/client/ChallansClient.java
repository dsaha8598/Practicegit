package com.ey.in.tds.feign.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.ey.in.tcs.common.domain.TcsReceipt;
import com.ey.in.tds.common.challan.dto.Receipt;
import com.ey.in.tds.common.dto.receipt.ReceiptDetailsDTO;
import com.ey.in.tds.common.dto.returns.ChallanReceiptDTO;
import com.ey.in.tds.core.util.ApiStatus;

@FeignClient(url = "${feign.challans.app.url}", name = "challans")
public interface ChallansClient {

	// Get Data from challan
	@GetMapping("/data/byquarter")
	public List<ChallanReceiptDTO> getChallanData(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam("assessmentYear") int assessmentYear, @RequestParam("quarter") String quarter,
			@RequestHeader("TAN-NUMBER") String tanNumber);

	// Get challan data, receipt BSR code and receipt date
	@GetMapping("/getdata/returns")
	public List<ChallanReceiptDTO> getChallanReceiptData(@RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestParam("assessmentYear") int assessmentYear, @RequestParam("quarter") String quarter,
			@RequestHeader(value = "X-TENANT-ID") String tenantId);

	@GetMapping("getreceiptdetails/challanid")
	public List<ReceiptDetailsDTO> getReceiptDataByChallan(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam("challanid") UUID challanId, @RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestParam("assessmentMonth") int assessmentMonth,
			@RequestParam("challanSection") String challanSection);

	// TODO NEED TO DELETE IN NEXT 1 WEEK
	/*
	 * @PostMapping("/challan") public Challan createChallan(@RequestBody Challan
	 * challan, @RequestHeader(value = "USER_NAME") String userName);
	 */
	@PostMapping("/receipt")
	public Receipt createReceipt(@RequestBody Receipt receipt);

	@GetMapping(value = "/getReciept/by/year/tanNumber/quarter", produces = "application/json")
	public ResponseEntity<ApiStatus<List<Receipt>>> getRecieptByTanYearAndQuarter(
			@RequestParam("TAN-NUMBER") String tanNumber, @RequestParam("Quarter") List<Integer> quarter,
			@RequestParam("AssesmentYear") Integer year, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestParam("residentIndicator") Boolean residentIndicator);

	@GetMapping(value = "tcs/getReciept/by/year/tanNumber/quarter")
	public ResponseEntity<ApiStatus<List<TcsReceipt>>> getRecieptByTanYearAndQuarterTCS(
			@RequestParam("TAN-NUMBER") String tanNumber, @RequestParam("Quarter") List<Integer> quarter,
			@RequestParam("AssesmentYear") Integer year, @RequestHeader(value = "X-TENANT-ID") String tenantId);

	@GetMapping(value = "tcs/data/byquarter", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ChallanReceiptDTO> getChallanDataForReturnsTCS(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam("assessmentYear") int assessmentYear, @RequestParam("quarter") String quarter,
			@RequestHeader("TAN-NUMBER") String tanNumber);

}
