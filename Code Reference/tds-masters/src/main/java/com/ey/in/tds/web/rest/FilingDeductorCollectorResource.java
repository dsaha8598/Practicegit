package com.ey.in.tds.web.rest;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.FilingDeductorCollector;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.service.FilingDeductorCollectorService;

@RestController
@RequestMapping("/api/masters")
public class FilingDeductorCollectorResource {

	@Autowired
	private FilingDeductorCollectorService filingDeductorCollectorService;

	/**
	 * This API for create Deductor Collector
	 * 
	 * @param deductorCollector
	 * @param userName
	 * @return
	 */
	@PostMapping(value = "/filing/deductorcollector", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<FilingDeductorCollector>> createDeductorCollector(
			@Valid @RequestBody FilingDeductorCollector deductorCollector,
			@RequestHeader(value = "USER_NAME") String userName) {
		MultiTenantContext.setTenantId("master");
		FilingDeductorCollector result = filingDeductorCollectorService.saveDeductorCollector(deductorCollector,
				userName);
		ApiStatus<FilingDeductorCollector> apiStatus = new ApiStatus<FilingDeductorCollector>(HttpStatus.CREATED,
				"SUCCESS", "DEDUCTOR COLLECTOR IS CREATED", result);
		return new ResponseEntity<ApiStatus<FilingDeductorCollector>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get All Deductor Collector
	 * 
	 * @return
	 */
	@GetMapping(value = "/filing/deductorcollector")
	public ResponseEntity<ApiStatus<List<FilingDeductorCollector>>> getAlldeductorCollector(
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId) {
		MultiTenantContext.setTenantId("master");
		List<FilingDeductorCollector> result = filingDeductorCollectorService.getAllDeductorCollector();
		ApiStatus<List<FilingDeductorCollector>> apiStatus = new ApiStatus<List<FilingDeductorCollector>>(HttpStatus.OK,
				"SUCCESS", " GET ALL DEDUCTOR COLLECTOR RECORDS", result);
		return new ResponseEntity<ApiStatus<List<FilingDeductorCollector>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get Deductor Collector based on Id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/filing/deductorcollector/{id}")
	public ResponseEntity<ApiStatus<FilingDeductorCollector>> getBydeductorCollectorId(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		FilingDeductorCollector result = filingDeductorCollectorService.getByDeductorCollectorId(id);
		ApiStatus<FilingDeductorCollector> apiStatus = new ApiStatus<FilingDeductorCollector>(HttpStatus.OK, "SUCCESS",
				" GET BY DEDUCTOR COLLECTOR RECORD BASED ON ID", result);
		return new ResponseEntity<ApiStatus<FilingDeductorCollector>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * to get catagory value using
	 * 
	 * @param catagoryDescription
	 * @return
	 */
	@GetMapping(value = "/filling/deductorCollector/{catagoryDescription}")
	public ResponseEntity<ApiStatus<String>> getCatagoryValue(@PathVariable String catagoryDescription) {
		MultiTenantContext.setTenantId("master");
		String catagoryValue = filingDeductorCollectorService.getCatagoryValue(catagoryDescription);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				" GET BY DEDUCTOR COLLECTOR RECORD BASED ON ID", catagoryValue);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * to get catagory description using category value
	 * 
	 * @param categoryValue
	 * @return
	 */
	@GetMapping(value = "/filling/{categoryValue}")
	public ResponseEntity<ApiStatus<String>> getCatagoryDescription(@PathVariable String categoryValue) {
		MultiTenantContext.setTenantId("master");
		String catagoryDescription = filingDeductorCollectorService.getCategoryDescription(categoryValue);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				" Get catagory description based on catagory value.", catagoryDescription);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

}
