package com.ey.in.tds.onboarding.web.rest;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.onboarding.service.deductee.CollecteeSectionThresholdLedgerService;

@RestController
@RequestMapping("/api/onboarding/tcs")
public class CollecteeSectionThresholdLedgerResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CollecteeSectionThresholdLedgerService collecteeSectionThresholdLedgerService;

	@PostMapping(value = "/collecteeledger/upload/excel")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> createCollectorOnboardingInfo(
			@RequestParam("files") MultipartFile file,
			@RequestParam(value = "year", required = false) Integer assessmentYear,
			@RequestParam(value = "type", required = false) String typeOfFile,
			@RequestHeader("X-TENANT-ID") String tenantId) throws Exception {
		logger.info("REST request to save the Collectee Ledger Report");
		TCSBatchUpload collecteeSectionThresholdLedger = null;
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			collecteeSectionThresholdLedger = collecteeSectionThresholdLedgerService.saveFileData(file, tenantId,
					typeOfFile, assessmentYear);
		}
		ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED COLLECTEE FILE SUCCESSFULLY ", collecteeSectionThresholdLedger);
		return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
	}

}
