package com.ey.in.tds.ingestion.web.rest.generalledger;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
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

import com.ey.in.tds.common.domain.transactions.jdbc.dto.GeneralLedger;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.service.glmismatch.GeneralLedgerService;
import com.ey.in.tds.ingestion.service.glmismatch.GlMismatchService;

@RestController
@RequestMapping("/api/ingestion")
public class MismatchResource {

	@Autowired
	private GlMismatchService glMismatchService;

	@Autowired
	private GeneralLedgerService generalLedgerService;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * This API returns the general ledger summary based on batch id and type
	 * 
	 * @param batchId
	 * @param type
	 * @param pagination
	 * @return
	 */
	@PostMapping(value = "/general-ledger-summary/{batchId}/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<GeneralLedger>>> getGeneralLedgerSummaryData(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable Integer batchId, @PathVariable String type,
			@RequestBody Pagination pagination) {
		logger.info("type: {}", type);
		CommonDTO<GeneralLedger> generalLedgerList = generalLedgerService.getGeneralLedgerSummaryData(batchId, type,
				pagination);
		logger.info("General ledger summary data count: {}", generalLedgerList.getCount());
		ApiStatus<CommonDTO<GeneralLedger>> apiStatus = new ApiStatus<CommonDTO<GeneralLedger>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", generalLedgerList);
		return new ResponseEntity<ApiStatus<CommonDTO<GeneralLedger>>>(apiStatus, HttpStatus.OK);
	}   

	/**
	 * This API returns the general ledger summary based on batch id
	 * 
	 * @param batchId
	 * @param pagination
	 * @return
	 */
	@PostMapping(value = "/general-ledger-summary/{batchId}/notfoundingl", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>> getGLSummaryDataForInvoices(@PathVariable Integer batchId,
			@RequestBody Pagination pagination) {

		CommonDTO<InvoiceLineItem> generalLedgerList = generalLedgerService.getGLSummaryDataForInvoices(batchId,
				pagination);
		logger.info("General ledger summary data count: {}", generalLedgerList.getCount());
		ApiStatus<CommonDTO<InvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<InvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", generalLedgerList);
		return new ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}  

	/**
	 * This API returns the general ledger summary based on assessment year and
	 * month and type
	 * 
	 * @param glSummaryDTO
	 * @param tan
	 * @param type
	 * @return
	 */
	@PostMapping(value = "/general-ledger-summary-by-year-month/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<GeneralLedger>>> getGLSummaryDataByType(
			@RequestBody GeneralLedgerSummaryDTO glSummaryDTO, @RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable String type) {
		logger.info("type: {}", type);
		logger.info("tan: {}", tan);
		CommonDTO<GeneralLedger> generalLedgerList = generalLedgerService.getGLSummaryDataByYearMonth(tan, type,
				glSummaryDTO, glSummaryDTO.getPagination());
		logger.info("General ledger summary data count: {}", generalLedgerList.getCount());
		ApiStatus<CommonDTO<GeneralLedger>> apiStatus = new ApiStatus<CommonDTO<GeneralLedger>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", generalLedgerList);
		return new ResponseEntity<ApiStatus<CommonDTO<GeneralLedger>>>(apiStatus, HttpStatus.OK);
	}  

	/**
	 * This API returns the general ledger summary for gl not found based on
	 * assessment year and month
	 * 
	 * @param glSummaryDTO
	 * @param tan
	 * @return
	 */
	@PostMapping(value = "/general-ledger-summary-by-year-month/notfoundingl", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>> getGLSummaryDataForGlNotFound(
			@RequestBody GeneralLedgerSummaryDTO glSummaryDTO, @RequestHeader(value = "TAN-NUMBER") String tan) {
		logger.info("tan: {}", tan);
		CommonDTO<InvoiceLineItem> generalLedgerList = generalLedgerService.getGLSummaryDataForGlNotFound(glSummaryDTO,
				tan);

		ApiStatus<CommonDTO<InvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<InvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", generalLedgerList);
		return new ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}  

	@GetMapping("/glremediationreport/export")
	public ResponseEntity<InputStreamResource> exportRemediationReport(
			@RequestParam(value = "deductorTan") String deductorTan, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader("X-TENANT-ID") String tenantId) throws IOException {
		ByteArrayInputStream in = glMismatchService.exportRemediationReport(deductorTan, tenantId, deductorPan);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=GL_Remediation_Report.xlsx");
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
	}

	// DOWNLOAD General Ledger summary report
	@PostMapping(value = "/general-ledger/summary/export", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Void>> exportProvisionMismatchReport(
			@RequestHeader(value = "TAN-NUMBER") String tan, 
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "quarter") String quarter,
			@RequestParam(value = "year", required = true) int year,
			@RequestParam(value = "month", required = false) int month,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId,
			@RequestParam(value = "glBatchId", required = false) Integer glBatchId)
			throws Exception {
		logger.info("General Ledger Summary export requested for : TAN {} Year : {} Quarter : {} Month : {} Batch : {}", tan, year, quarter, month, glBatchId);
		generalLedgerService.exportGLSummaryReport(tan, tenantId, deductorPan, year, month, userName,glBatchId, quarter);
		return new ResponseEntity<ApiStatus<Void>>(HttpStatus.OK);
	}  
}
