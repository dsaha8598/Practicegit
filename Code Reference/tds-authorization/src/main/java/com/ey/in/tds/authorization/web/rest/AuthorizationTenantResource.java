package com.ey.in.tds.authorization.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.SQLDataSourceProperties;
import com.ey.in.tds.common.config.TenantProperties;
import com.ey.in.tds.common.config.TenantRoutingDataSource;
import com.ey.in.tds.core.util.TenantConfiguration;

@RestController
@RequestMapping("/api/authorization")
public class AuthorizationTenantResource {

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

}








































































































































































































