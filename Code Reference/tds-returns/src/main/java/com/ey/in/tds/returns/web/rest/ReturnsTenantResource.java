package com.ey.in.tds.returns.web.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.TDSReturnsApplication;
import com.ey.in.tds.common.config.SQLDataSourceProperties;
import com.ey.in.tds.common.config.TenantProperties;
import com.ey.in.tds.common.config.TenantRoutingDataSource;
import com.ey.in.tds.core.util.AppUtils;
import com.ey.in.tds.core.util.TenantConfiguration;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api/returns")
public class ReturnsTenantResource {

	@Autowired
	TenantProperties tenantProperties;
	
	@Autowired
	SQLDataSourceProperties dataSourceProperties;

	@Autowired
	TenantRoutingDataSource tenantDataSource;


	@PostMapping("/tenant")
	public void add(@RequestBody TenantConfiguration tenantConfiguration) {
		tenantProperties.addTenant(tenantConfiguration);
		dataSourceProperties.refreshDataSources(tenantConfiguration.getTenantId());
		tenantDataSource.afterPropertiesSet();
	}

	@GetMapping("/tenant/{tenantId}")
	public TenantConfiguration get(@PathVariable String tenantId) {
		if (!tenantProperties.tenantIds().contains(tenantId)) {
			throw new RuntimeException("No tenant configuration found for id : " + tenantId);
		}
		return tenantProperties.getConfiguration(tenantId);
	}

	@GetMapping("/build-number")
	@Timed
	public ResponseEntity<Map<String, String>> buildNumber() {
		Map<String, String> response = new AppUtils().getManifestAttributes(TDSReturnsApplication.class, "tds-returns");
		return new ResponseEntity<Map<String, String>>(response, HttpStatus.OK);
	}
}
