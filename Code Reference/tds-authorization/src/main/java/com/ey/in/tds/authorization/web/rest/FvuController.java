package com.ey.in.tds.authorization.web.rest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.feign.client.FvuClient;

@RestController
@RequestMapping("/api")
public class FvuController extends BaseResource {

	@Autowired
	private FvuClient fvuClient;

	@GetMapping("/fvu-proxy/generate")
	public void get(@RequestParam(value = "X-TENANT-ID") String tenantId,
			@RequestParam("assessmentYear") String assessmentYear, @RequestParam("quarter") String quarter,
			@RequestParam("filingType") String filingType, @RequestParam("filingFormType") String filingFormType,
			@RequestParam("isCorrection") boolean isCorrection,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan) {
		debug("Getting details for tenant: " + tenantId);
		if (StringUtils.isBlank(tenantId)) {
			throw new CustomException("Expecting tenantId header");
		}
		if (StringUtils.isBlank(assessmentYear)) {
			throw new CustomException("Expecting assessmentYear");
		}
		if (StringUtils.isBlank(quarter)) {
			throw new CustomException("Expecting quarter");
		}
		fvuClient.generateFvu(tenantId, assessmentYear, quarter, isCorrection, deductorTan, filingType, filingFormType);
	}
	
	@GetMapping("fvu-proxy/generate/tcs")
	public void getTcs(@RequestParam(value = "X-TENANT-ID") String tenantId,
			@RequestParam("assessmentYear") String assessmentYear, @RequestParam("quarter") String quarter,
			@RequestParam("filingType") String filingType, @RequestParam("filingFormType") String filingFormType,
			@RequestParam("isCorrection") boolean isCorrection,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan) {
		debug("Getting details for tenant: " + tenantId);
		if (StringUtils.isBlank(tenantId)) {
			throw new CustomException("Expecting tenantId header");
		}
		if (StringUtils.isBlank(assessmentYear)) {
			throw new CustomException("Expecting assessmentYear");
		}
		if (StringUtils.isBlank(quarter)) {
			throw new CustomException("Expecting quarter");
		}
		fvuClient.generateTcsFvu(tenantId, assessmentYear, quarter, isCorrection, deductorTan, filingType, filingFormType);
	}

}
