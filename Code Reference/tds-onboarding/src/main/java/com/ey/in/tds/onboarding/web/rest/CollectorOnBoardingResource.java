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

import com.ey.in.tds.common.onboarding.jdbc.dto.CollectorOnBoardingInformationDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.onboarding.dto.collector.CollectorOnBoardingInfoDTO;
import com.ey.in.tds.onboarding.service.collector.CollectorOnBoardingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@RestController
@RequestMapping("/api/onboarding")
public class CollectorOnBoardingResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CollectorOnBoardingService collectorOnBoardingService;

	/**
	 * POST /collectorOnboardingInfo : Create a new Collector Onboarding Info
	 * 
	 * @param collectorId
	 * @param configValuesDTO
	 * @return
	 * @throws JsonProcessingException
	 * @throws NoRecordFoundException
	 */
	@PostMapping(value = "/collectorOnboardingInfo")
	public ResponseEntity<ApiStatus<CollectorOnBoardingInfoDTO>> createCollectorOnboardingInfo(
			@RequestHeader("X-TENANT-ID") String tenantId,
			@Valid @RequestBody CollectorOnBoardingInfoDTO collectorOnboardingInfoDTO) throws JsonProcessingException {

		logger.info("Entered to createCollectorOnboardingInfo method");
		CollectorOnBoardingInfoDTO collectorOnboardingInfo = collectorOnBoardingService
				.createCollectorOnboardingInfo(collectorOnboardingInfoDTO, tenantId);

		logger.debug("<-- createCollectorOnboardingInfo");
		ApiStatus<CollectorOnBoardingInfoDTO> apiStatus = new ApiStatus<CollectorOnBoardingInfoDTO>(HttpStatus.CREATED,
				"To create a Collector Onboarding Info", "Created collector onboarding information",
				collectorOnboardingInfo);

		return new ResponseEntity<ApiStatus<CollectorOnBoardingInfoDTO>>(apiStatus, HttpStatus.CREATED);

	}

	/**
	 * 
	 * @param collectorId
	 * @return
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	@PostMapping(value = "/getCollectorOnboardingInfo")
	public ResponseEntity<ApiStatus<Optional<CollectorOnBoardingInfoDTO>>> getCollectorOnboardingInfo(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestBody Map<String, String> requestParams) throws JsonMappingException, JsonProcessingException {
		logger.info("Entered to get Collector Onboarding Information");
		String pan = requestParams.get("pan");
		if (pan.isEmpty()) {
			logger.error("Collector Pan is null");
			throw new CustomException("Collector pan cannot be null", HttpStatus.BAD_REQUEST);
		}
		Optional<CollectorOnBoardingInfoDTO> collectorOnboardingInfo = collectorOnBoardingService
				.getCollectorOnboardingInfo(pan);
		ApiStatus<Optional<CollectorOnBoardingInfoDTO>> apiStatus = new ApiStatus<Optional<CollectorOnBoardingInfoDTO>>(
				HttpStatus.OK, "To get a Collector Onboarding Information based on collector pan", "NO ALERT",
				collectorOnboardingInfo);
		return new ResponseEntity<ApiStatus<Optional<CollectorOnBoardingInfoDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client.
	 * 
	 * @param tenantId
	 * @param deductrorTan
	 * @return
	 */
	@GetMapping(value = "/getCollectorPan")
	public ResponseEntity<ApiStatus<List<CollectorOnBoardingInformationDTO>>> getCollectorOnboardingInfo(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "DEDUCTOR-PAN") String collectorPan) {
		if (collectorPan.isEmpty()) {
			logger.error("Collector Pan is null");
			throw new CustomException("Collector pan cannot be null", HttpStatus.BAD_REQUEST);
		}
		List<CollectorOnBoardingInformationDTO> deductorOnboardingInfo = collectorOnBoardingService
				.getCollectorPan(collectorPan);
		ApiStatus<List<CollectorOnBoardingInformationDTO>> apiStatus = new ApiStatus<List<CollectorOnBoardingInformationDTO>>(
				HttpStatus.OK, "To get a Collector Onboarding Information based on collector pan", "NO ALERT",
				deductorOnboardingInfo);
		return new ResponseEntity<ApiStatus<List<CollectorOnBoardingInformationDTO>>>(apiStatus, HttpStatus.OK);

	}
}
