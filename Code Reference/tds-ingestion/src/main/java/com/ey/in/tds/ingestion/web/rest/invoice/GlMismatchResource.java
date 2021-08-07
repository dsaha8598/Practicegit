package com.ey.in.tds.ingestion.web.rest.invoice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.dto.invoice.InvoiceMismatchUpdateDTO;
import com.ey.in.tds.ingestion.service.glmismatch.GlMismatchService;
import com.ey.in.tds.ingestion.service.invoicemismatch.InvoiceMismatchService;
import com.microsoft.azure.storage.StorageException;

@RestController
@RequestMapping("/api/ingestion")
public class GlMismatchResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private InvoiceMismatchService invoiceMismatchService;

	@Autowired
	private GlMismatchService glMismtachService;

	/**
	 * Update mismatch table based on Mismatch category
	 * 
	 * @param mismatchcategory
	 * @param invoiceMismatchUpdateDTO
	 * @return
	 */
	@PutMapping("glmismatches/{mismatchcategory}/action")
	public ResponseEntity<ApiStatus<InvoiceMismatchUpdateDTO>> uploadPDF(@PathVariable String mismatchcategory,
			InvoiceMismatchUpdateDTO invoiceMismatchUpdateDTO) {
		glMismtachService.updateMismatchByActionForGl(invoiceMismatchUpdateDTO);

		logger.info("Button value----: {}", invoiceMismatchUpdateDTO.getActionTaken());
		logger.info("Action Tds Rate----: {}", invoiceMismatchUpdateDTO.getFinalTdsRate());
		logger.info("Action Final Reason ---: {}", invoiceMismatchUpdateDTO.getActionReason());
		logger.info("Action Final Section Rate----: {}", invoiceMismatchUpdateDTO.getFinalTdsSection());
		ApiStatus<InvoiceMismatchUpdateDTO> apiStatus = new ApiStatus<InvoiceMismatchUpdateDTO>(HttpStatus.OK,
				"SUCCESS", "UPDATED MISMATCHES UPLOADED RECORDS", invoiceMismatchUpdateDTO);
		return new ResponseEntity<ApiStatus<InvoiceMismatchUpdateDTO>>(apiStatus, HttpStatus.OK);
	}

	//TODO NEED TO CHANGE FOR SQL
/*	@GetMapping("glmismatches/all/{year}/{month}")
	public ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>> listOfMisMatchesForExcelBasedonBatchUploadId(
			@PathVariable int year, @PathVariable int month) {
		List<InvoiceMismatchByBatchIdDTO> listMismatchesAll = glMismtachService
				.getInvoiceMismatchAllForGl(year, month, Pagination.UNPAGED).getData();
		logger.info("List of all glmismatches : {} ", listMismatchesAll);
		ApiStatus<List<InvoiceMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<InvoiceMismatchByBatchIdDTO>>(
				HttpStatus.OK, "SUCCESS", "LIST OF ALL GL MISMATCHES ", listMismatchesAll);
		return new ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);
	}  */

	// Import and Export Remediation Report as on 27-07-2019
	@PostMapping("glmismatches/remediation/import")
	public ResponseEntity<ApiStatus<BatchUpload>> getStatusOfRemediationReport(@RequestParam("file") MultipartFile file,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		BatchUpload batchUpload = invoiceMismatchService.importToBatchUpload(file, tenantId, userName);
		// TODO: To get the imported file and store it in another table for
		// batch_upload_remediation table

		logger.info("Remediation report upload----: {} ", batchUpload);
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED REMEDIATION RECORD FOR GL MISMATCHES", batchUpload);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	@PutMapping("glmismatchesreport/updateremediationreport/import")
	public ResponseEntity<ApiStatus<List<InvoiceMismatchUpdateDTO>>> updateRemediationReport(
			@RequestParam("file") MultipartFile file, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		List<InvoiceMismatchUpdateDTO> response = null;
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {

			String contentType = new Tika().detect(file.getInputStream());
			if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
					|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
				response = null;
				//TODO NEED TO CHANGE FOR SQL
						//glMismtachService.updateRemediationReportForGl(file, tenantId, userName);

				logger.info("Remediation report update : {}", response);
				ApiStatus<List<InvoiceMismatchUpdateDTO>> apiStatus = new ApiStatus<List<InvoiceMismatchUpdateDTO>>(
						HttpStatus.OK, "SUCCESS", "UPLOADED AND UPDATED REMEDIATION RECORD FOR GL MISMATCHES",
						response);
				return new ResponseEntity<ApiStatus<List<InvoiceMismatchUpdateDTO>>>(apiStatus, HttpStatus.OK);
			} else {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
			}
		}
	}

}
