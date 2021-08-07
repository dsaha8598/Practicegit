package com.ey.in.tds.authorization.web.rest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.admin.service.TenantService;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.Tenant;
import com.ey.in.tds.common.domain.TenantConfig;
import com.ey.in.tds.common.dto.TenantConfigReturnDTO;
import com.ey.in.tds.core.dto.TenantConfigDTO;
import com.ey.in.tds.core.dto.TenantDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.IllegalConfigValueException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.ApiStatus;

@RestController
@RequestMapping("/api/authorization")
public class TenantConfigController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TenantService tenantService;

	/**
	 * POST /tenantconfig : Create a new Tenant Config
	 *
	 * @param tenantConfig object with deductor id
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         TenantConfig
	 * @throws NoRecordFoundException
	 * @throws IllegalConfigValueException
	 * @throws RecordNotFoundException
	 */
	@PostMapping(value = "/tenantconfig")
	public ResponseEntity<ApiStatus<List<TenantConfig>>> createTenantConfig(
			@Valid @RequestBody TenantConfigDTO tenantConfig, @RequestHeader(value = "X-USER-EMAIL") String userName)
			throws IllegalConfigValueException, RecordNotFoundException {

		List<TenantConfig> result = tenantService.createTenantConfiguration(tenantConfig, userName);

		Map<String, Map<String, String>> response = new LinkedHashMap<>();
		tenantService.notifyTenantAddition(tenantConfig.getTenantInfo().getTenantName(), response);

		ApiStatus<List<TenantConfig>> apiStatus = new ApiStatus<List<TenantConfig>>(HttpStatus.CREATED,
				"To create tenant config  ", "Tenant config record created successfully", result);
		return new ResponseEntity<ApiStatus<List<TenantConfig>>>(apiStatus, HttpStatus.CREATED);
	}

	/**
	 * POST /createtenant : Create a new Tenant
	 *
	 * @param tenantName object with deductor id
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         TenantConfig
	 * @throws RecordNotFoundException
	 * @throws NoRecordFoundException
	 */
	@PostMapping(value = "/createtenant")
	public ResponseEntity<ApiStatus<Tenant>> createTenant(@Valid @RequestBody TenantDTO tenant,
			@RequestHeader(value = "X-USER-EMAIL") String userName) throws RecordNotFoundException {

		if (logger.isDebugEnabled())
			logger.debug("Entered to created new Tenant");

		Tenant result = tenantService.createTenant(tenant, userName);

		ApiStatus<Tenant> apiStatus = new ApiStatus<Tenant>(HttpStatus.CREATED, "To create a Tenant",
				"Tenant record Created", result);
		return new ResponseEntity<ApiStatus<Tenant>>(apiStatus, HttpStatus.CREATED);

	}

	/**
	 * GET /gettenant : Get a Tenant Record by deductorId.
	 *
	 * @param deductorId
	 * 
	 * @return the ResponseEntity with status 200 (OK) and with result
	 * 
	 */
	@GetMapping(value = "/gettenant/{deductorId}")
	public ResponseEntity<ApiStatus<List<?>>> getTenantByDeductorId(@PathVariable Long deductorId) {

		if (deductorId == null) {
			logger.error("Deductor Id is null");
			throw new CustomException("Deductor Id cannot be null", HttpStatus.BAD_REQUEST);
		}
		List<?> tenant = tenantService.getTenantByDeductorId(deductorId);

		ApiStatus<List<?>> apiStatus = new ApiStatus<List<?>>(HttpStatus.OK, "To get a Single Tenant",
				"Tenant Single Record", tenant);

		return new ResponseEntity<ApiStatus<List<?>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /gettenant : Get a TenantConfig Records by deductorId.
	 *
	 * @param deductorId
	 * 
	 * @return the ResponseEntity with status 200 (OK) and with result
	 * 
	 */
	@GetMapping(value = "/gettenantconfig/{deductorId}")
	public ResponseEntity<ApiStatus<List<?>>> getTenantConfigByTenantId(@PathVariable Long deductorId) {
		if (logger.isDebugEnabled())
			logger.debug("Entered to get Tenant Config By Tenant Id");

		if (deductorId == null) {
			logger.error("Deductor Id is null");
			throw new CustomException("Deductor Id cannot be null", HttpStatus.BAD_REQUEST);
		}
		List<?> tenantConfig = tenantService.getTenantConfigByTenantId(deductorId);

		ApiStatus<List<?>> apiStatus = new ApiStatus<List<?>>(HttpStatus.OK,
				"To get a Tenant record based on tenant id", "Tenant Record based on tenant id", tenantConfig);

		return new ResponseEntity<ApiStatus<List<?>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * Get a TenantConfig Records by tenantId.
	 * 
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/getTenantConfigDetailsByTenantId/{tenantId}")
	public ResponseEntity<ApiStatus<TenantConfigReturnDTO>> getTenantConfigDetailsByTenantId(
			@PathVariable Long tenantId, @RequestHeader(value = "DEDUCTOR-PAN") String deductorPan) {
		if (logger.isDebugEnabled())
			logger.debug("Entered to get Tenant Config By Tenant Id");

		if (tenantId == null) {
			logger.error("Tenant Id is null");
			throw new CustomException("Deductor Id cannot be null", HttpStatus.BAD_REQUEST);
		}
		TenantConfigReturnDTO tenantConfigReturnDTO = tenantService.getTenantConfigDetailsByTenantId(tenantId);

		ApiStatus<TenantConfigReturnDTO> apiStatus = new ApiStatus<TenantConfigReturnDTO>(HttpStatus.OK,
				"To get a Tenant record based on tenant id", "Tenant Record based on tenant id", tenantConfigReturnDTO);

		return new ResponseEntity<ApiStatus<TenantConfigReturnDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /gettenantconfigtype : Get a TenantConfig Records by deductorId and type
	 * .
	 *
	 * @param deductorId and type
	 * 
	 * @return the ResponseEntity with status 200 (OK) and with result
	 * 
	 */
	@GetMapping(value = "/gettenantconfigtype")
	public ResponseEntity<ApiStatus<List<TenantConfig>>> getTenantConfigByTenantIdAndType(
			@RequestParam("deductorId") Long deductorId,
			@Valid @NotBlank @RequestParam(value = "type", required = true) String type) {

		if (deductorId == null && type.isEmpty()) {
			throw new CustomException("Deductor Id and type cannot be null", HttpStatus.BAD_REQUEST);
		}
		List<TenantConfig> tenantConfig = tenantService.getTenantConfigByTenantIdAndType(type, deductorId);

		ApiStatus<List<TenantConfig>> apiStatus = new ApiStatus<List<TenantConfig>>(HttpStatus.OK,
				"To get a Tenant record based on tenant id", "Tenant Record based on tenant id", tenantConfig);
		return new ResponseEntity<ApiStatus<List<TenantConfig>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /gettenantnames : Get the tenant names associated with deductor ids
	 * 
	 * @return the ResponseEntity with status 200 (OK) and with result
	 */
	@GetMapping(value = "/gettenantnames")
	public ResponseEntity<ApiStatus<List<Tenant>>> getTenantNames() {

		MultiTenantContext.setTenantId("master");
		List<Tenant> tenantConfig = tenantService.getTenantNames();

		ApiStatus<List<Tenant>> apiStatus = new ApiStatus<List<Tenant>>(HttpStatus.OK,
				"To get a Tenant all tenant names based on tenant id", "Tenant Names", tenantConfig);
		return new ResponseEntity<ApiStatus<List<Tenant>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * Get the Azure key vault values associated with tenant id
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/encryptionsalt")
	public ResponseEntity<ApiStatus<String>> getEncryptionSaltValue(
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {

		String encryptionValue = tenantService.getEncryptionSaltValue(tenantId);

		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK,
				"To get a Tenant all tenant names based on tenant id", "Tenant Names", encryptionValue);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

}
