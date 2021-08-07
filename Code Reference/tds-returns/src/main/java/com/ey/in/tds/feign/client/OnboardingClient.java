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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.dto.onboarding.deductee.DeducteeMasterDTO;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterDTO;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterNonResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterNonResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorOnboardingInformationDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorTanAddress;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderMasterResidential;
import com.ey.in.tds.common.onboarding.response.dto.DeductorOnboardingInformationResponseDTO;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.returns.dto.DeducteeMasterResidentDTO;

@FeignClient(url = "${feign.onboarding.app.url}", name = "onboarding")
public interface OnboardingClient {

	// Get Data from onboarding
	@GetMapping("/data/deductee")
	public List<DeducteeMasterDTO> getResidentDeducteeData(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader("TAN-NUMBER") String deductorTan);

	// Get Data from onboarding
	@PostMapping("/deductee/nonresident/all")
	public List<DeducteeMasterDTO> getNonResidentDeducteeData(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader("TAN-NUMBER") String deductorTan);

	// Top get Deductor Record based on Deductor pan
	@GetMapping(value = "/deductorbypan")
	public ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductorByPan(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan);

	@PostMapping(value = "/deductee/resident")
	public ResponseEntity<ApiStatus<DeducteeMasterResidential>> createResidentialDeductee(
			@RequestBody DeducteeMasterResidentDTO deducteeMasterResidential,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestHeader(value = "USER_NAME") String userName);

	@PostMapping(value = "/deductee/nonresident")
	public @ResponseBody ResponseEntity<ApiStatus<DeducteeMasterNonResidential>> createNonResidentialDeductee(
			@RequestParam("data") String deducteeMasterNonResident,
			@RequestParam(value = "trcFile", required = false) MultipartFile trcFile,
			@RequestParam(value = "tenFFile", required = false) MultipartFile tenFFile,
			@RequestParam(value = "wpeFile", required = false) MultipartFile wpeFile,
			@RequestParam(value = "noPEFile", required = false) MultipartFile noPEFile,
			@RequestHeader(value = "TAN-NUMBER", required = true) String deductorTan,
			@RequestParam(value = "fixedBasedIndiaFile", required = false) MultipartFile isFixedBasedIndiaFile,
			@RequestHeader(value = "USER_NAME") String userName);

	@GetMapping(value = "/deductee/emails", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<DeducteeMasterResidential>>> getDeducteeEmails(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam("deducteePan") String deducteePan);

	@GetMapping(value = "/deductorname/by/pan")
	public ResponseEntity<ApiStatus<String>> getDeductorNameBasedOnPan(@RequestParam("deductorPan") String pan,
			@RequestHeader("X-TENANT-ID") String tenantId);

	@GetMapping(value = "/non-residnet/deductee/emails", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<DeducteeMasterNonResidential>>> getDeducteeNonResidentEmails(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam("deducteePan") String deducteePan);

	@PostMapping(value = "/getDeductorOnboardingInfo")
	public ResponseEntity<ApiStatus<Optional<DeductorOnboardingInformationResponseDTO>>> getDeductorOnboardingInfo(
			@RequestBody Map<String, String> requestParams, @RequestHeader("X-TENANT-ID") String tenantId);

	@GetMapping(value = "/shareholder/nonresident")
    public ResponseEntity<ApiStatus<ShareholderMasterNonResidential>> getNonResidentialShareholderById(
            @RequestHeader(value = "X-TENANT-ID") String tenantId,
            @RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
           @RequestHeader(value = "Id", required = true) Integer id);

	@GetMapping(value = "/shareholder/resident/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<ShareholderMasterDTO>> getResidentialShareholder(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@PathVariable Integer id);

	@GetMapping(value = "/deductorTanAddress")
	public ResponseEntity<ApiStatus<DeductorTanAddress>> getDeductorTanAddressByPanTan(
			@RequestParam("deductorTan") String deductorTan, @RequestParam("deductorPan") String deductorPan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId);
	
	@GetMapping(value = "/shareholder/nonresident/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiStatus<ShareholderMasterDTO>> getNonResidentialShareholder(
            @RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
            @RequestHeader(value = "X-TENANT-ID", required = true) String tenantId,
            @PathVariable Integer id);
	
	@GetMapping(value = "/getDeductorPan")
	public ResponseEntity<ApiStatus<List<DeductorOnboardingInformationDTO>>> getDeductorOnboardingInfo(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "DEDUCTOR-PAN") String deductorPan);
	
	@GetMapping(value = "/shareholders/resident")
	public ResponseEntity<ApiStatus<List<ShareholderMasterResidential>>> getResidentShareholdersByPan(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "shareHolderPan") String shareHolderPan);

	@GetMapping(value = "/shareholders/nonresident")
	public ResponseEntity<ApiStatus<List<ShareholderMasterNonResidential>>> getNonResidentShareholdersByPan(
			@PathVariable(value = "shareholdertype", required = true) String shareholderType,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan);
	
	
	@GetMapping(value = "fiegn/shareholder/nonresident/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<ShareholderMasterNonResidential>> getNonResidentialShareholderForFilings(@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan, @PathVariable Integer id) ;
	
	 @PutMapping(value = "/batch/update/nonresident")
		public ResponseEntity<ApiStatus<Integer>> batchUpdateNonResident(
				@RequestParam(value = "list", required = true) List<ShareholderMasterNonResidential> shareholder,@RequestHeader(value = "X-TENANT-ID") String tenantId);
	 
	 @GetMapping(value = "/shareholder/nonresident/feign")
	    public ResponseEntity<ApiStatus<List<ShareholderMasterNonResidential>>> getNonResidentialShareholderByIdsFeign(
	            @RequestHeader(value = "X-TENANT-ID") String tenantId,
	            @RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
	           @RequestHeader(value = "Ids", required = true) String ids) ;

}
