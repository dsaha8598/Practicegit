package com.ey.in.tds.feign.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.ey.in.tds.common.dto.returns.ChallanReceiptDTO;

@FeignClient(url = "${feign.challans.app.url}", name = "challans")
public interface ChallansClient {

	// Get Data from challan
	@GetMapping("/data/byquarter")
	public List<ChallanReceiptDTO> getChallanData(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam("assessmentYear") int assessmentYear, @RequestParam("quarter") String quarter,
			@RequestHeader("TAN-NUMBER") String tanNumber);
}
