package com.ey.in.tds.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.onboarding.config.MultipartConfig;

@FeignClient(url = "${feign.ingestion.app.url}", name = "ingestion", configuration = MultipartConfig.class)
public interface IngestionClient {

	@PostMapping(value = "/batch/excel/panvalidation", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiStatus<BatchUpload>> uploadExcel(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam("year") int assessmentYear, @RequestParam("type") String type,
			@PathVariable(value = "file") MultipartFile file, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "DEDUCTOR-PAN") String pan);

}
