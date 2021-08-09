package com.ey.in.tds.authorization.web.rest;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.admin.config.JwtTokenValidator;
import com.ey.in.tds.common.admin.service.AuthorizationService;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.config.RedisUtil;
import com.ey.in.tds.common.domain.TenantConfig;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.RedisKeys;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTParser;

@RestController
@RequestMapping("/api/authorization")
public class AuthorizationController extends BaseResource {

	public static final String AUTHORIZATION_HEADER = "Authorization";

	@Autowired
	private AuthorizationService authorizationService;
	
	@Autowired
	private JwtTokenValidator jwtTokenValidator;

	@Autowired
	private RedisUtil<Map<String, Object>> redisUtilUserTenantInfo;

	@SuppressWarnings("unchecked")
	@GetMapping(value = "/authorities", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Map<String, Object>>> getAuthorities(
			@RequestHeader(value = AUTHORIZATION_HEADER) String authorizationHeader)
			throws IOException, ParseException {

		String idToken = authorizationHeader.replaceFirst("Bearer ", StringUtils.EMPTY);
		info("idToken", idToken);

		String uniqueUserName = jwtTokenValidator.getUsername(idToken);

		info(" Unique UserName : {}", uniqueUserName);

		Map<String, Object> userTenantInfo = authorizationService.getCassandraUserTenantInformation(MultiTenantContext.getTenantId(),uniqueUserName);
		List<TenantConfig> tenantConfigList = (List<TenantConfig>) userTenantInfo.get("tenantConfig");
		debug("User Role and Permission data  : {}", userTenantInfo);
		debug("Tenant config info  : {}", tenantConfigList);

		ApiStatus<Map<String, Object>> apiStatus = new ApiStatus<Map<String, Object>>(HttpStatus.OK, "SUCCESS",
				"RETRIEVED AUTHORITIES OBJECT SUCCESSFULLY", userTenantInfo);
		return new ResponseEntity<ApiStatus<Map<String, Object>>>(apiStatus, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/refresh/token", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Map<String, Object>>> refreshToken(
			@RequestHeader(value = AUTHORIZATION_HEADER) String authorizationHeader)
			throws IOException, ParseException {

		String idToken = authorizationHeader.replaceFirst("Bearer ", StringUtils.EMPTY);
		info("idToken", idToken);

		String uniqueUserName = jwtTokenValidator.getUsername(idToken);

		String jsonString = redisUtilUserTenantInfo.getMapAsSingleEntry(RedisKeys.USERINFO.name(), uniqueUserName);

		ObjectMapper mapper = new ObjectMapper();

		Map<String, Object> existingUserTenantInfo = mapper.readValue(jsonString, Map.class);

		String nonce = (String) JWTParser.parse(idToken).getJWTClaimsSet().getClaim("nonce");
		existingUserTenantInfo.put("nonce", nonce);
		jsonString = mapper.writeValueAsString(existingUserTenantInfo);
		// Save user info in REDIS cache
		redisUtilUserTenantInfo.putMap(RedisKeys.USERINFO.name(), uniqueUserName, jsonString);

		debug("User data  : {}", existingUserTenantInfo);

		ApiStatus<Map<String, Object>> apiStatus = new ApiStatus<Map<String, Object>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", existingUserTenantInfo);
		return new ResponseEntity<ApiStatus<Map<String, Object>>>(apiStatus, HttpStatus.OK);
	}
}
