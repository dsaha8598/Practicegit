package com.ey.in.tds.authorization.web.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.AuthorizationApplication;
import com.ey.in.tds.common.admin.service.AppService;
import com.ey.in.tds.common.admin.service.TenantService;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.config.RedisUtil;
import com.ey.in.tds.common.config.TenantProperties;
import com.ey.in.tds.common.domain.Classification;
import com.ey.in.tds.common.domain.TdsMonthTracker;
import com.ey.in.tds.common.domain.Tenant;
import com.ey.in.tds.common.dto.NatureOfPaymentRate;
import com.ey.in.tds.common.repository.ClassificationRepository;
import com.ey.in.tds.common.repository.NatureOfPaymentMasterRepository;
import com.ey.in.tds.common.repository.TdsMonthTrackerRepository;
import com.ey.in.tds.common.repository.TenantRepository;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.AppUtils;
import com.ey.in.tds.core.util.RedisKeys;
import com.ey.in.tds.core.util.TenantConfiguration;
import com.ey.in.tds.feign.client.ChallansClient;
import com.ey.in.tds.feign.client.FvuClient;
import com.ey.in.tds.feign.client.IngestionClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.feign.client.ReportsClient;
import com.ey.in.tds.feign.client.ReturnsClient;
import com.ey.in.tds.feign.client.ValidationClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api")
public class AdminController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RedisUtil<Map<String, Object>> redisUtilUserTenantInfo;

	@Value("${tds.props.clientID}")
	private String clientId;
	@Value("${tds.props.authority}")
	private String authority;
	@Value("${tds.props.validateAuthority}")
	private String validateAuthority;
	@Value("${tds.props.redirectUri}")
	private String redirectUri;
	@Value("${tds.props.postLogoutRedirectUri}")
	private String postLogoutRedirectUri;
	@Value("${tds.props.scope}")
	private String scope;
	
	@Value("${tds.props.hostName}")
	private String hostName;

	@Autowired
	TenantRepository tenantRepository;

	@Autowired
	TenantProperties tenantProperties;

	@Autowired
	IngestionClient ingestionClient;

	@Autowired
	ChallansClient challansClient;

	@Autowired
	FvuClient fvuClient;

	@Autowired
	OnboardingClient onboardingClient;

	@Autowired
	ReportsClient reportsClient;

	@Autowired
	ReturnsClient returnsClient;

	@Autowired
	ValidationClient validationClient;

	@Autowired
	TenantService tenantService;

	@Autowired
	private NatureOfPaymentMasterRepository natureOfPaymentMasterRepository;

	@Autowired
	private AppService appService;

	@Autowired
	private TdsMonthTrackerRepository tdsMonthTrackerRepository;

	@Autowired
	private ClassificationRepository classificationRepository;

	@GetMapping("/admin/redis-data/flush")
	@Timed
	public ResponseEntity<Map<String, String>> redisDataFlush() {
		Map<String, String> response = new HashMap<>();
		redisUtilUserTenantInfo.flushAll();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/administration/props")
	@Timed
	public ResponseEntity<Map<String, Object>> getBackEndProps(
			@RequestHeader(value = "referer", required = false) final String referer) {
		Map<String, Object> response = new HashMap<>();
		response.put("clientID", clientId);
		response.put("authority", authority);
		response.put("validateAuthority", validateAuthority);
		response.put("redirectUri", redirectUri);
		response.put("hostName", hostName);
		response.put("postLogoutRedirectUri", postLogoutRedirectUri);
		//This is mainly for the Developers who are accessing Dev API's from Localhost
		if (referer!=null && referer.contains("localhost")) {
			response.put("redirectUri", "http://localhost:4200");
			response.put("postLogoutRedirectUri", "http://localhost:4200");
		} else {
			response.put("redirectUri", redirectUri);
			response.put("postLogoutRedirectUri", postLogoutRedirectUri);
		}

		response.put("navigateToLoginRequestUrl", "true");
		response.put("cacheLocation", "localStorage");

		List<String> scopeList = new ArrayList<>();
		scopeList.add("user.read");
		scopeList.add("openid");
		scopeList.add("profile");
		scopeList.add(scope);
		response.put("consentScopes", scopeList);

		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@GetMapping("/admin/redis-data/fetch")
	@Timed
	public ResponseEntity<Map<String, Object>> fetchRedisData() throws IOException {
		Map<String, Object> response = new HashMap<>();
		response.put("redisData", redisUtilUserTenantInfo.fetchAll());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/admin/redis-data/refresh/{redisKey}")
	@Timed
	public ResponseEntity<Map<String, Object>> refreshRedisData(@PathVariable String redisKey) throws IOException {
		Map<String, Object> response = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		if (RedisKeys.NATUREOFPAYMENT.name().equals(redisKey)) {
			redisUtilUserTenantInfo.deleteByKey(RedisKeys.NATUREOFPAYMENT);
			List<NatureOfPaymentRate> natureOfPaymentRates = natureOfPaymentMasterRepository.findNatureOfPaymentRates();
			for (NatureOfPaymentRate natureOfPaymentRate : natureOfPaymentRates) {
				String jsonString = mapper.writeValueAsString(natureOfPaymentRate);
				redisUtilUserTenantInfo.putMap(RedisKeys.NATUREOFPAYMENT.name(),
						natureOfPaymentRate.getNatureOfPaymentId().toString() + "_"
								+ natureOfPaymentRate.getDeducteeStatus() + "_"
								+ natureOfPaymentRate.getTdsMasterId().toString(),
						jsonString);
			}
		} else if (RedisKeys.TENANTINFO.name().equals(redisKey)) {
			MultiTenantContext.setTenantId("master");
			redisUtilUserTenantInfo.deleteByKey(RedisKeys.TENANTINFO);
			List<Tenant> tenants = tenantRepository.findAll();
			for (Tenant tenant : tenants) {
				// repopulate from database and then get data.
				TenantConfiguration tenantConfiguration = tenantService.loadTenantConfiguration(tenant.getTenantName());
				String jsonString = mapper.writeValueAsString(tenantConfiguration);
				redisUtilUserTenantInfo.putMap(RedisKeys.TENANTINFO.name(), tenantConfiguration.getTenantId(),
						jsonString);
			}

		} else if (RedisKeys.TDSMONTHTRACKER.name().equals(redisKey)) {
			redisUtilUserTenantInfo.deleteByKey(RedisKeys.TDSMONTHTRACKER);
			List<TdsMonthTracker> tdsMonthTrackers = tdsMonthTrackerRepository.getAllTdsMonthTracker();
			for (TdsMonthTracker tdsMonthTracker : tdsMonthTrackers) {
				String jsonString = mapper.writeValueAsString(tdsMonthTracker);
				redisUtilUserTenantInfo.putMap(RedisKeys.TDSMONTHTRACKER.name(),
						tdsMonthTracker.getYear() + "-" + tdsMonthTracker.getMonth(), jsonString);
			}

		} else if (RedisKeys.TDSCLASSIFICATION.name().equals(redisKey)) {
			redisUtilUserTenantInfo.deleteByKey(RedisKeys.TDSCLASSIFICATION);
			List<Classification> classifications = classificationRepository.findAllClassifications();
			for (Classification classification : classifications) {
				String jsonString = mapper.writeValueAsString(classification);
				redisUtilUserTenantInfo.putMap(RedisKeys.TDSCLASSIFICATION.name(),
						classification.getClassificationName(), jsonString);
			}
		}
		response.put("redisData", redisUtilUserTenantInfo.fetchByRedisKey(redisKey));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/admin/test/custom-exception")
	@Timed
	public ResponseEntity<Map<String, Object>> testCutomException() {
		throw new CustomException("test exception");
	}

	@GetMapping("/admin/notify/new/tenant")
	@Timed
	public ResponseEntity<Map<String, Map<String, String>>> fetchQueryData() {
		Map<String, Map<String, String>> response = new LinkedHashMap<>();
		List<Tenant> tenants = tenantRepository.findAll();
		String tenantId = "";
		for (Tenant tenant : tenants) {
			if (tenant.getTenantName().toLowerCase().equals("default")) {
				// Do nothing.
			} else {
				tenantId = tenant.getTenantName();
				tenantService.notifyTenantAddition(tenantId, response);
			}
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/authorization/build-number")
	public ResponseEntity<Map<String, String>> buildNumber() {
		Map<String, String> response = new AppUtils().getManifestAttributes(AuthorizationApplication.class,
				"tds-authorization");
		return new ResponseEntity<Map<String, String>>(response, HttpStatus.OK);
	}

	@GetMapping("/build-number")
	public ResponseEntity<Map<String, String>> appBuildNumber() {
		if (logger.isDebugEnabled()) {
			logger.debug("Fetching application modules build numbers");
		}
		return new ResponseEntity<>(appService.appBuildSummary(AuthorizationApplication.class), HttpStatus.OK);
	}

	@GetMapping("/test")
	public ResponseEntity<Map<String, String>> test() throws InterruptedException {
		Map<String, String> response = new HashMap<>();
		if (logger.isInfoEnabled())
			logger.info("About to flush redis data");
		Thread.sleep(300000);
		System.out.println("Thread '" + Thread.currentThread().getName() + "' is woken after sleeping for 1 second");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
