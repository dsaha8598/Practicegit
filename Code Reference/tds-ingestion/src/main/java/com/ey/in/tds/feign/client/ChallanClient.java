package com.ey.in.tds.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.ey.in.tds.common.dto.receipt.ReceiptDataDTO;
import com.ey.in.tds.core.util.ApiStatus;

@FeignClient(url = "${feign.challans.app.url}", name = "challans")
public interface ChallanClient {

	@GetMapping("/receipt/data/basedon/batchid")
	public ResponseEntity<ApiStatus<ReceiptDataDTO>> getReceiptDataBasedOnBatchId(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestParam("batch_id") Integer batchId);

}
