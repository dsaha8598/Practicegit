package com.ey.in.tds;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.ey.in.tds.common.admin.filters.ZuulAuthFilter;
import com.ey.in.tds.common.admin.security.EndPointAuthorizations;
import com.ey.in.tds.core.swagger.SwaggerConfig;
import com.ey.in.tds.core.util.AppUtils;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.feign.client.FeignErrorDecoder;

import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableZuulProxy
@EnableConfigurationProperties(EndPointAuthorizations.class)
@EnableFeignClients
@EnableSwagger2
@SpringBootApplication
public class AuthorizationApplication {
	private static final Logger logger = LoggerFactory.getLogger(AuthorizationApplication.class);

	@Value("${spring.application.name}")
	private String applicationName;

	public static void main(String[] args) throws FileNotFoundException {
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Starting application : %s ", AuthorizationApplication.class.getSimpleName()));
		}
		Map<String, String> appSummary = new TreeMap<String, String>();
		Map<String, String> appBuildInfo = new TreeMap<String, String>();
		try {
			Map<String, String> attributes = new AppUtils().getManifestAttributes(AuthorizationApplication.class,
					"tds-authorization");
			for (Object key : attributes.keySet()) {
				appBuildInfo.put(key.toString(), attributes.get(key.toString()));
			}
			if (appBuildInfo != null) {
				String key = null;
				String value = null;
				if (appBuildInfo.get("Implementation-Title") != null) {
					key = appBuildInfo.get("Implementation-Title").toString();
					value = appBuildInfo.get("Build-Id").toString();
					if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
						appSummary.put(key, value);
					}
				}
			}

		} catch (Exception e) {
			// do nothing other than warn
			appSummary.put("tds-authorization", StringUtils.EMPTY);
			logger.warn(e.getMessage());
		}
		SpringApplication.run(AuthorizationApplication.class, args);
		CommonUtil.setAsposeLicense();
	}

	@Bean
	public Docket api() {
		return new SwaggerConfig().getSwaggerDocker(applicationName);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Building: %s ", RestTemplate.class.getSimpleName()));
		}
		return builder.build();
	}

	@Bean
	public ZuulAuthFilter authFilter() {
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Initializing: %s ", ZuulAuthFilter.class.getSimpleName()));
		}
		return new ZuulAuthFilter();
	}

	@Bean
	public FeignErrorDecoder errorDecoder() {
		return new FeignErrorDecoder();
	}
	
	
}
