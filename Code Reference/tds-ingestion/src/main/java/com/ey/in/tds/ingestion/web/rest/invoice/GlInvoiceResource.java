package com.ey.in.tds.ingestion.web.rest.invoice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.List;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.dto.batch.BatchUploadDTO;
import com.ey.in.tds.ingestion.service.glinvoice.GlInvoiceService;
import com.microsoft.azure.storage.StorageException;

@RestController
@RequestMapping("/api/ingestion")
public class GlInvoiceResource {

	@Autowired
	private GlInvoiceService glInvoiceService;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@PostMapping("glinvoices/excel/import")
	public ResponseEntity<ApiStatus<BatchUpload>> uploadExcel(@RequestParam("file") MultipartFile file,
			@RequestParam("year") int assessmentYear, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			BatchUpload batchData = glInvoiceService.saveUploadExcelInvoiceForGL(assessmentYear, file, tenantId);

			logger.info("Batch Upload data excel---: {} ", batchData);
			ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS", "NO ALERT",
					batchData);
			return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * TO get the excel status for calls
	 * 
	 * @return
	 */
	@GetMapping("glinvoices/excel/status")
	public ResponseEntity<ApiStatus<List<BatchUploadDTO>>> lisOfbatchInvoiceExcel() {
		List<BatchUploadDTO> invoiceExcelListBatchUploads = glInvoiceService
				.getlistOfGlInvoiceExcelBasedOnAssessmentYearMonthType(Pagination.UNPAGED).getData();

		logger.info("List of Batch Upload data invoice excel---: {} ", invoiceExcelListBatchUploads);

		ApiStatus<List<BatchUploadDTO>> apiStatus = new ApiStatus<List<BatchUploadDTO>>(HttpStatus.OK, "SUCCESS",
				"LIST OF GL INVOICES STATUS", invoiceExcelListBatchUploads);
		return new ResponseEntity<ApiStatus<List<BatchUploadDTO>>>(apiStatus, HttpStatus.OK);
	}

}
