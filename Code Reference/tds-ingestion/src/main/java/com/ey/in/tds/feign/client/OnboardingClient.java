package com.ey.in.tds.feign.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.AoMasterDTO;
import com.ey.in.tds.common.dto.DeducteeMasterNonResidentialDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterNonResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorOnboardingInformationDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorTanAddress;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMaster;
import com.ey.in.tds.common.onboarding.response.dto.DeductorOnboardingInformationResponseDTO;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.config.MultipartConfig;
import com.microsoft.azure.storage.StorageException;

@FeignClient(url = "${feign.onboarding.app.url}", name = "onboarding", configuration = MultipartConfig.class)
public interface OnboardingClient {

	// Deductor onboarding Details based on Deductor Id
	@PostMapping(value = "/getDeductorOnboardingInfo")
	public ResponseEntity<ApiStatus<Optional<DeductorOnboardingInformationResponseDTO>>> getDeductorOnboardingInfo(
			@RequestBody Map<String, String> requestParams, @RequestHeader(value = "X-TENANT-ID") String tenantId);

	@GetMapping("/getDeducteeBasedOnPanAndName")
	public ResponseEntity<ApiStatus<DeducteeMasterNonResidentialDTO>> getDeducteeBasedOnPanAndName(
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
			@RequestParam(name = "deducteePan", required = false) String deducteePan,
			@RequestParam(name = "deducteeName", required = false) String deducteeName,
			@RequestHeader(value = "X-TENANT-ID") String tenantId);

	@PutMapping(value = "/aomaster", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<AoMasterDTO>> update(@RequestBody AoMasterDTO aoMasterDTO,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "X-TENANT-ID") String tenantId)
			throws URISyntaxException;

	@PostMapping(value = "/aomaster", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<AoMasterDTO>> create(@RequestBody AoMasterDTO aoMaster,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "X-TENANT-ID") String tenantId)
			throws URISyntaxException;

	@GetMapping("/getaomasterbydeducteenameaomasterid")
	public ResponseEntity<ApiStatus<AoMasterDTO>> getAoMasterBasedOnDeducteeNameAoMasterId(
			@RequestParam(value = "deducteeName") String deducteeName, @RequestParam(value = "aoMasterId") UUID id,
			@RequestHeader(value = "X-TENANT-ID") String tenantId);

	// for SAP deductee Resident and Non Resident.
	@PostMapping(value = "/deductee/import/csv", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiStatus<BatchUpload>> readImportedCsvData(@PathVariable(value = "file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestParam(value = "batchId") Integer batchId);

	@GetMapping(value = "/deductorbypan")
	public ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductorByPan(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId);

	@PostMapping(value = "/chartofaccounts/upload/excel", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiStatus<BatchUpload>> createdChartOfAccounts(
			@PathVariable(value = "file") MultipartFile file, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam("year") int assessmentYear, @RequestParam("type") String type,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName, @RequestParam("batchId") Integer batchId)
			throws InvalidKeyException, InvalidFormatException, IOException, URISyntaxException, StorageException;

	@GetMapping("/ldc-master/by/certificateNo")
	public ResponseEntity<ApiStatus<List<LdcMaster>>> getLdcByCertificateNo(@RequestParam("TAN") String deductorTan,
			@RequestParam("CERTIFICATENO") String certificateNo, @RequestHeader("TENANT") String tenantId);

	@PostMapping(value = "/kyc/import")
	public ResponseEntity<ApiStatus<BatchUpload>> uploadKycExcel(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @RequestParam(value = "batchId") Integer batchId,
			@RequestParam(value = "type", required = true) String type);

	@PostMapping(value = "/kyc/upload/finalReport")
	public ResponseEntity<ApiStatus<BatchUpload>> uploadKycFinalReportExcel(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @RequestParam(value = "batchId") Integer batchId,
			@RequestParam(value = "type", required = true) String type);

	// tcs feign clients

	@PostMapping(value = "/tcs/chartofaccounts/upload/excel", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiStatus<TCSBatchUpload>> tcsCreatedChartOfAccounts(
			@PathVariable(value = "file") MultipartFile file, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam("year") int assessmentYear, @RequestParam("type") String type,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName, @RequestParam("batchId") Integer batchId)
			throws InvalidKeyException, InvalidFormatException, IOException, URISyntaxException, StorageException;

	// for SAP collectee master.
	@PostMapping(value = "/tcs/deductee/import/csv", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiStatus<TCSBatchUpload>> tcsReadImportedCsvData(
			@PathVariable(value = "file") MultipartFile file, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader(value = "DEDUCTOR-PAN") String pan, @RequestHeader(value = "USER_NAME") String userName,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestParam(value = "batchId") Integer batchId);

	@GetMapping(value = "/collecteeType")
	public ResponseEntity<ApiStatus<String>> getCollecteeTypeBasedOnCollecteeCode(
			@RequestHeader("COLLECTEE_CODE") String collecteeCode,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("tan") String tan,
			@RequestHeader("pan") String pan);

	@PostMapping(value = "/keywords/import", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiStatus<BatchUpload>> uploadKeywordsExcel(@PathVariable("file") MultipartFile file,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "batchId") Integer batchId);

	@GetMapping(value = "/deductee/pan/code/name")
	public ResponseEntity<ApiStatus<List<DeducteeMasterNonResidential>>> findAllByDeducteePanModifiedName(
			@RequestParam String deductorPan, @RequestParam String modifiedName,
			@RequestHeader("X-TENANT-ID") String tenantId);

	@GetMapping(value = "/getdeductor/tan")
	public ResponseEntity<ApiStatus<List<DeductorTanAddress>>> getDeductorBasedOnTan(@RequestParam("tan") String tan,
			@RequestHeader("X-TENANT-ID") String tenantId);

	@GetMapping(value = "/getDeductorPan")
	public ResponseEntity<ApiStatus<List<DeductorOnboardingInformationDTO>>> getDeductorOnboardingInfo(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "DEDUCTOR-PAN") String deductorPan);

	@GetMapping("/valid/ldc/by/certificateNo")
	public ResponseEntity<ApiStatus<List<LdcMaster>>> getValidLdcByCertificateNo(
			@RequestParam("TAN") String deductorTan, @RequestParam("CERTIFICATENO") String certificateNo,
			@RequestHeader("TENANT") String tenantId);
	
	@GetMapping("/deductor-data/{pan}")
	public DeductorMasterDTO getDeductorMasterData(@RequestHeader("X-TENANT-ID") String tenantId,
			@PathVariable String pan);
	
	@GetMapping(value = "/nr/deductees", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<DeducteeMasterNonResidential>>> getNrDeductees(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId);

}