package com.ey.in.tds.ingestion.web.rest.invoice.tcs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.service.tdsmismatch.TcsMismatchService;
import com.ey.in.tds.ingestion.service.tdsmismatch.TdsMismatchService;
import com.microsoft.azure.storage.StorageException;

/**
 * 
 * @author Scriptbees.
 *
 */
@RestController
@RequestMapping("/api/ingestion")
public class TCSSparkReportsResource {

	@Autowired
	private TcsMismatchService tcsMismatchService;
	
	@Autowired
	private TdsMismatchService tdsMismatchService;
	
	/**
	 * This api for tcs mlr file download.
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param collectorTan
	 * @param uploadType
	 * @param status
	 * @param tenantId
	 * @param userName
	 * @param noOfRows
	 * @param batchId
	 * @return
	 * @throws InvalidKeyException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	@GetMapping(value = "/tcs/invoice/tcsmismatch/reports")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> getTcsMismatchReports(
			@RequestParam("assessmentYear") Integer assessmentYear,
			@RequestParam(value = "assessmentMonth") Integer assessmentMonth,
			@RequestParam("collectorTan") String collectorTan, @RequestParam("uploadType") String uploadType,
			@RequestParam("status") String status, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "X-USER-EMAIL") String userName, @RequestParam("noOfRows") Long noOfRows,
			@RequestParam("batchId") Integer batchId, @RequestParam("path") String path,
			@RequestParam("fileName") String fileName)
			throws InvalidKeyException, FileNotFoundException, IOException, URISyntaxException, StorageException {
		TCSBatchUpload list = tcsMismatchService.saveAndUpdateMismatchReport(collectorTan, tenantId, assessmentYear,
				path, noOfRows, uploadType, status, assessmentMonth, userName, batchId, fileName);
		ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
				"tcs batch upload retrieved sucessfully.", list);
		return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * This api for tds mlr file download.
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param deductorTan
	 * @param uploadType
	 * @param status
	 * @param tenantId
	 * @param userName
	 * @param noOfRows
	 * @param batchId
	 * @param path
	 * @param fileName
	 * @return
	 * @throws InvalidKeyException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	@GetMapping(value = "/invoice/tdsmismatch/reports")
	public ResponseEntity<ApiStatus<BatchUpload>> getTdsMismatchReports(
			@RequestParam("assessmentYear") Integer assessmentYear,
			@RequestParam(value = "assessmentMonth") Integer assessmentMonth,
			@RequestParam("deductorTan") String deductorTan, @RequestParam("uploadType") String uploadType,
			@RequestParam("status") String status, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "X-USER-EMAIL") String userName, @RequestParam("noOfRows") Long noOfRows,
			@RequestParam("batchId") Integer batchId, @RequestParam("path") String path,
			@RequestParam("fileName") String fileName)
			throws InvalidKeyException, FileNotFoundException, IOException, URISyntaxException, StorageException {
		BatchUpload list = tdsMismatchService.saveAndUpdateMismatchReport(deductorTan, tenantId, assessmentYear, path,
				noOfRows, uploadType, status, assessmentMonth, userName, batchId, fileName);
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"tds batch upload retrieved sucessfully.", list);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}

}
