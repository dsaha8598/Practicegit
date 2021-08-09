package com.ey.in.tds.web.rest;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.ThresholdGroupAndNopMapping;
import com.ey.in.tds.common.domain.ThresholdLimitGroupMaster;
import com.ey.in.tds.common.dto.CustomThresholdGroupLimit;
import com.ey.in.tds.common.dto.CustomThresholdGroupLimitDto;
import com.ey.in.tds.common.dto.CustomThresholdGroupLimitInterface;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.service.ThresholdLimitGroupMasterService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 
 * @author vamsir
 *
 */
@RestController
@RequestMapping("/api/masters")
public class ThresholdLimitGroupMasterResource extends BaseResource {

	@Autowired
	private ThresholdLimitGroupMasterService thresholdLimitGroupMasterService;

	/**
	 * This API for save threshold group
	 * 
	 * @param thresholdGroup
	 * @param userName
	 * @return
	 * @throws JsonProcessingException
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	@PostMapping("/threshold-group")
	public ResponseEntity<ApiStatus<CustomThresholdGroupLimit>> createThresholdGroup(
			@Valid @RequestBody CustomThresholdGroupLimit thresholdGroup,
			@RequestHeader(value = "USER_NAME") String userName)
			throws JsonProcessingException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (thresholdGroup.getApplicableFrom() == null) {
			throw new CustomException("Applicable From cannot be empty", HttpStatus.BAD_REQUEST);
		}
		if ((thresholdGroup.getApplicableTo() != null)
				&& (thresholdGroup.getApplicableFrom().equals(thresholdGroup.getApplicableTo())
						|| thresholdGroup.getApplicableFrom().isAfter(thresholdGroup.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be Greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		MasterESAPIValidation.tdsThresholdGroup(thresholdGroup);
		CustomThresholdGroupLimit result = thresholdLimitGroupMasterService.save(thresholdGroup, userName);
		ApiStatus<CustomThresholdGroupLimit> apiStatus = new ApiStatus<CustomThresholdGroupLimit>(HttpStatus.CREATED,
				"SUCCESS", "THRESHOLD GROUP CREATED SUCCESSFULLY", result);
		return new ResponseEntity<ApiStatus<CustomThresholdGroupLimit>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get all threshold group
	 * 
	 * @return
	 */
	@GetMapping("get/threshold/limit/group")
	public ResponseEntity<ApiStatus<List<CustomThresholdGroupLimitDto>>> getThresholdLimitGroup() {
		logger.info("Enterted into getThresholdLimitGroup API");
		MultiTenantContext.setTenantId("master");
		List<CustomThresholdGroupLimitInterface> returnList = thresholdLimitGroupMasterService.getAllThresholdGroup();

		List<CustomThresholdGroupLimitDto> thresholdGroupList = new ArrayList<>();
		for (CustomThresholdGroupLimitInterface customThresholdGroupLimitInterface : returnList) {
			CustomThresholdGroupLimitDto customThresholdGroupLimitDto = new CustomThresholdGroupLimitDto();
			customThresholdGroupLimitDto.setGroupId(customThresholdGroupLimitInterface.getId());
			customThresholdGroupLimitDto.setThresholdAmount(customThresholdGroupLimitInterface.getThresholdAmount());
			customThresholdGroupLimitDto.setGroupName(customThresholdGroupLimitInterface.getGroupName());
			customThresholdGroupLimitDto.setNature(customThresholdGroupLimitInterface.getNature());
			customThresholdGroupLimitDto.setNopId(customThresholdGroupLimitInterface.getNopId());
			customThresholdGroupLimitDto.setApplicableFrom(customThresholdGroupLimitInterface.getApplicableFrom());
			customThresholdGroupLimitDto.setApplicableTo(customThresholdGroupLimitInterface.getApplicableTo());

			thresholdGroupList.add(customThresholdGroupLimitDto);

		}

		ApiStatus<List<CustomThresholdGroupLimitDto>> apiStatus = new ApiStatus<List<CustomThresholdGroupLimitDto>>(
				HttpStatus.OK, "SUCCESS", "GET ALL THRESHOLD LIMIT DETAILS SUCCESSFULLY", thresholdGroupList);
		logger.info("Exited into getThresholdLimitGroup API");
		return new ResponseEntity<ApiStatus<List<CustomThresholdGroupLimitDto>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("get/threshold/limit/group/{nopId}")
	public ResponseEntity<ApiStatus<List<CustomThresholdGroupLimitInterface>>> getThresholdLimitGroupByNopId(
			@PathVariable Long nopId) {
		logger.info("Enterted into getThresholdLimitGroup API");
		MultiTenantContext.setTenantId("master");
		List<CustomThresholdGroupLimitInterface> returnList = thresholdLimitGroupMasterService
				.getThresholdLimitGroupByNopId(nopId);
		ApiStatus<List<CustomThresholdGroupLimitInterface>> apiStatus = new ApiStatus<List<CustomThresholdGroupLimitInterface>>(
				HttpStatus.OK, "SUCCESS", "GET ALL THRESHOLD LIMIT DETAILS SUCCESSFULLY", returnList);
		logger.info("Exited into getThresholdLimitGroup API");
		return new ResponseEntity<ApiStatus<List<CustomThresholdGroupLimitInterface>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get by id
	 * 
	 * @return
	 */
	@GetMapping("/threshold-group/{id}")
	public ResponseEntity<ApiStatus<CustomThresholdGroupLimit>> getThresholdLimitGroupById(@PathVariable Long id) {
		logger.info("Enterted into getThresholdLimitGroup API");
		MultiTenantContext.setTenantId("master");
		CustomThresholdGroupLimit returnOptional = thresholdLimitGroupMasterService.getById(id);
		ApiStatus<CustomThresholdGroupLimit> apiStatus = new ApiStatus<CustomThresholdGroupLimit>(HttpStatus.OK,
				"SUCCESS", "GET THRESHOLD GROUP DETAILS SUCCESSFULLY", returnOptional);
		logger.info("Exited into getThresholdLimitGroup API");
		return new ResponseEntity<ApiStatus<CustomThresholdGroupLimit>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for update threshold group
	 * 
	 * @param thresholdGroup
	 * @param userName
	 * @return
	 * @throws JsonProcessingException
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	@PutMapping("/threshold-group")
	public ResponseEntity<ApiStatus<CustomThresholdGroupLimit>> updateThresholdGroup(
			@Valid @RequestBody CustomThresholdGroupLimit thresholdGroup,
			@RequestHeader(value = "USER_NAME") String userName)
			throws JsonProcessingException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (thresholdGroup.getApplicableFrom() == null) {
			throw new CustomException("Applicable From cannot be empty", HttpStatus.BAD_REQUEST);
		}
		if ((thresholdGroup.getApplicableTo() != null)
				&& (thresholdGroup.getApplicableFrom().equals(thresholdGroup.getApplicableTo())
						|| thresholdGroup.getApplicableFrom().isAfter(thresholdGroup.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be Greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		MasterESAPIValidation.tdsThresholdGroup(thresholdGroup);
		CustomThresholdGroupLimit result = thresholdLimitGroupMasterService.update(thresholdGroup, userName);
		ApiStatus<CustomThresholdGroupLimit> apiStatus = new ApiStatus<CustomThresholdGroupLimit>(HttpStatus.CREATED,
				"SUCCESS", "THRESHOLD GROUP UPDATED SUCCESSFULLY", result);
		return new ResponseEntity<ApiStatus<CustomThresholdGroupLimit>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/threshold/limit/group")
	public ResponseEntity<ApiStatus<List<ThresholdLimitGroupMaster>>> getThresholdGroupByIds() {
		logger.info("Enterted into getThresholdLimitGroup API");
		MultiTenantContext.setTenantId("master");
		List<ThresholdLimitGroupMaster> returnOptional = thresholdLimitGroupMasterService.findByGroupIds();
		ApiStatus<List<ThresholdLimitGroupMaster>> apiStatus = new ApiStatus<List<ThresholdLimitGroupMaster>>(
				HttpStatus.OK, "SUCCESS", "GET THRESHOLD GROUP DETAILS SUCCESSFULLY", returnOptional);
		logger.info("Exited into getThresholdLimitGroup API");
		return new ResponseEntity<ApiStatus<List<ThresholdLimitGroupMaster>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get threshold group id based on nature id
	 * 
	 * @return
	 */
	@GetMapping("/threshold-group/group-id")
	public ResponseEntity<ApiStatus<Optional<ThresholdGroupAndNopMapping>>> getThresholdGroupId(
			@RequestParam(value = "natureid") Long natureid) {
		logger.info("Enterted into getThresholdLimitGroup API");
		MultiTenantContext.setTenantId("master");
		Optional<ThresholdGroupAndNopMapping> thresholdObj = thresholdLimitGroupMasterService
				.getThresholdGroupId(natureid);
		ApiStatus<Optional<ThresholdGroupAndNopMapping>> apiStatus = new ApiStatus<Optional<ThresholdGroupAndNopMapping>>(
				HttpStatus.OK, "SUCCESS", "GET THRESHOLD GROUP DETAILS SUCCESSFULLY", thresholdObj);
		logger.info("Exited into getThresholdLimitGroup API");
		return new ResponseEntity<ApiStatus<Optional<ThresholdGroupAndNopMapping>>>(apiStatus, HttpStatus.OK);
	}
	
	@GetMapping("/threshold/group/data")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getThresholdGroupData() {
		MultiTenantContext.setTenantId("master");
		List<Map<String, Object>> thresholdObj = thresholdLimitGroupMasterService.getThresholdGroupData();
		ApiStatus<List<Map<String, Object>>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"GET THRESHOLD GROUP DETAILS SUCCESSFULLY", thresholdObj);
		logger.info("Exited into getThresholdLimitGroup API");
		return new ResponseEntity<ApiStatus<List<Map<String, Object>>>>(apiStatus, HttpStatus.OK);
	}
	
	// Feign client
	@GetMapping("/threshold/nop/group/master")
	public ResponseEntity<ApiStatus<List<ThresholdGroupAndNopMapping>>> getThresholdNopGroupData(
			@RequestParam("nopId") Long nopId) {
		MultiTenantContext.setTenantId("master");
		List<ThresholdGroupAndNopMapping> thresholdObj = thresholdLimitGroupMasterService.getThresholdNopGroupData(nopId);
		ApiStatus<List<ThresholdGroupAndNopMapping>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				thresholdObj);
		return new ResponseEntity<ApiStatus<List<ThresholdGroupAndNopMapping>>>(apiStatus, HttpStatus.OK);
	}

}
