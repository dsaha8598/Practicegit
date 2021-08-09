package com.ey.in.tds.ingestion.web.rest.invoice.tcs;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.tcs.service.TCSPurchaseRegisterService;

/**
 * 
 * @author vamsir
 *
 */
@RestController
@RequestMapping("/api/ingestion/tcs")
public class TCSPurchaseRegisterResource {

	@Autowired
	private TCSPurchaseRegisterService tcsPurchaseRegisterService;

	/**
	 * 
	 * @param collectorTan
	 * @param collectorPan
	 * @param tenantId
	 * @param batchId
	 * @param assessmentYear
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	@PostMapping("/purchaseregister/tcsrl")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> saveGstPrData(
			@RequestHeader(value = "TAN-NUMBER", required = true) String collectorTan,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String collectorPan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestParam("batchId") Integer batchId,
			@RequestHeader("uploadType") String uploadType, @RequestParam("assessmentYear") Integer assessmentYear,
			@RequestHeader(value = "USER_NAME") String userName) throws IOException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		TCSBatchUpload respone = tcsPurchaseRegisterService.saveGstPrAndTcsRlData(collectorTan, collectorPan, batchId,
				assessmentYear, userName, tenantId, uploadType);
		ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				respone);
		return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param collectorTan
	 * @param collectorPan
	 * @param tenantId
	 * @param batchId
	 * @param uploadType
	 * @param assessmentYear
	 * @param userName
	 * @return
	 * @throws Exception 
	 */
	@PostMapping(value = "/reconciliation/excel")
	public ResponseEntity<ApiStatus<String>> getReportsByTypeExcelDownload(
			@RequestHeader(value = "TAN-NUMBER", required = true) String collectorTan,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String collectorPan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestParam("year") Integer assessmentYear,
			@RequestHeader(value = "USER_NAME") String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		tcsPurchaseRegisterService.getReconciliationExcelDownload(collectorTan, assessmentYear, collectorPan, tenantId,
				userName);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				"Generate sucessfuly");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param collectorTan
	 * @param collectorPan
	 * @param tenantId
	 * @param assessmentYear
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/reconciliation/form26as/excel")
	public ResponseEntity<ApiStatus<String>> getForm26As(
			@RequestHeader(value = "TAN-NUMBER", required = true) String collectorTan,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String collectorPan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestBody Map<String, String> requestParams) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		Integer assessmentYear = Integer.parseInt(requestParams.get("year"));
		Integer batchId = Integer.parseInt(requestParams.get("batchId"));
		String type = requestParams.get("uploadType");
		tcsPurchaseRegisterService.getForm26ASFinalResultReportWithSpark(collectorTan, assessmentYear, collectorPan, tenantId,
				userName, type, batchId);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				"Generate sucessfuly");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

}
