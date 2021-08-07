package com.ey.in.tds.onboarding.web.rest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.dto.masters.deductor.CustomTransformation;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorOnboardingInformationDTO;
import com.ey.in.tds.common.onboarding.response.dto.DeductorOnboardingInformationResponseDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.onboarding.dto.deductor.DeductorOnboardingInfoDTO;
import com.ey.in.tds.onboarding.service.deductor.DeductorOnBoardingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@RestController
@RequestMapping("/api/onboarding")
public class DeductorOnBoardingController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DeductorOnBoardingService deductorOnBoardingService;

	/**
	 * POST /deductorOnboardingInfo : Create a new Deductor Onboarding Info
	 * 
	 * @param deductorId
	 * @param configValuesDTO
	 * @return
	 * @throws JsonProcessingException 
	 * @throws NoRecordFoundException
	 */
	@PostMapping(value = "/deductorOnboardingInfo")
	public ResponseEntity<ApiStatus<DeductorOnboardingInfoDTO>> createDeductorOnboardingInfo(
			@RequestHeader("X-TENANT-ID") String tenantId,
			@Valid @RequestBody DeductorOnboardingInfoDTO deductorOnboardingInfoDTO) throws JsonProcessingException {

		MultiTenantContext.setTenantId(tenantId);
		logger.info("Entered to createDeductorOnboardingInfo method");
		DeductorOnboardingInfoDTO deductorOnboardingInfo = deductorOnBoardingService
				.createDeductorOnboardingInfo(deductorOnboardingInfoDTO, tenantId);

		logger.debug("<-- createDeductorOnboardingInfo");
		ApiStatus<DeductorOnboardingInfoDTO> apiStatus = new ApiStatus<DeductorOnboardingInfoDTO>(HttpStatus.CREATED,
				"To create a Deductor Onboarding Info", "Created a deductor onboarding information.", deductorOnboardingInfo);

		return new ResponseEntity<ApiStatus<DeductorOnboardingInfoDTO>>(apiStatus, HttpStatus.CREATED);

	}
	@PostMapping(value = "/getDeductorOnboardingInfo")
	public ResponseEntity<ApiStatus<Optional<DeductorOnboardingInformationResponseDTO>>> getDeductorOnboardingInfo(
			@RequestBody Map<String, String> requestParams, @RequestHeader("X-TENANT-ID") String tenantId) throws JsonMappingException, JsonProcessingException {
		logger.info("Entered to get Deductor Onboarding Information");
		MultiTenantContext.setTenantId(tenantId);
		String pan = requestParams.get("pan");
		if (pan.isEmpty()) {
			logger.error("Deductor Pan is null");
			throw new CustomException("Deductor pan cannot be null", HttpStatus.BAD_REQUEST);
		}
		Optional<DeductorOnboardingInformationDTO> deductorOnboardingInfo = deductorOnBoardingService
				.getDeductorOnboardingInfo(pan);
		Optional<DeductorOnboardingInformationResponseDTO> response = Optional.empty();
		if (deductorOnboardingInfo.isPresent()) {
			response = deductorOnBoardingService.copyToEntity(deductorOnboardingInfo);
		}
		ApiStatus<Optional<DeductorOnboardingInformationResponseDTO>> apiStatus = new ApiStatus<Optional<DeductorOnboardingInformationResponseDTO>>(
				HttpStatus.OK, "To get a Deductor Onboarding Information based on deductor pan", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<Optional<DeductorOnboardingInformationResponseDTO>>>(apiStatus,
				HttpStatus.OK);

	}

	/**
	 * 
	 * @param tenantId
	 * @param deductorInformation
	 * @return
	 * @throws JsonProcessingException 
	 */
	@PostMapping(value = "/deductorOnboardingInfo/customTransformation")
	public ResponseEntity<ApiStatus<Map<String, String>>> updateDeductorOnboardingInfoCustomJobs(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestBody CustomTransformation customTransformation)
			throws JsonProcessingException {
		Map<String, String> deductorTanList = deductorOnBoardingService
				.getDeductorCustomJobs(customTransformation.getConfig(), customTransformation.getDeductorPan());
		ApiStatus<Map<String, String>> apiStatus = new ApiStatus<Map<String, String>>(HttpStatus.OK,
				"update deductor onboarding info custom job id", "Deductor onboarding info custom jobs",
				deductorTanList);
		return new ResponseEntity<ApiStatus<Map<String, String>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * This API for feign client.
	 * @param tenantId
	 * @param deductorTan
	 * @return
	 */
	@GetMapping(value = "/getDeductorPan")
	public ResponseEntity<ApiStatus<List<DeductorOnboardingInformationDTO>>> getDeductorOnboardingInfo(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "DEDUCTOR-PAN") String deductorPan) {
		MultiTenantContext.setTenantId(tenantId);
		if (deductorPan.isEmpty()) {
			logger.error("Deductor Pan is null");
			throw new CustomException("Deductor pan cannot be null", HttpStatus.BAD_REQUEST);
		}
		List<DeductorOnboardingInformationDTO> deductorOnboardingInfo = deductorOnBoardingService
				.getDeductorPan(deductorPan);
		ApiStatus<List<DeductorOnboardingInformationDTO>> apiStatus = new ApiStatus<List<DeductorOnboardingInformationDTO>>(
				HttpStatus.OK, "To get a Deductor Onboarding Information based on deductor pan", "NO ALERT",
				deductorOnboardingInfo);
		return new ResponseEntity<ApiStatus<List<DeductorOnboardingInformationDTO>>>(apiStatus, HttpStatus.OK);

	}
	
	 

}
