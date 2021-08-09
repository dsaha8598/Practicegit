package com.ey.in.tds.ingestion.web.rest.invoice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.dto.invoice.InvoiceMismatchUpdateDTO;
import com.ey.in.tds.ingestion.service.sap.SapMismatchService;

@RestController
@RequestMapping("/api/ingestion")
public class SapMismatchResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SapMismatchService sapMismatchService;

	/**
	 * Update mismatch table based on Mismatch category
	 * 
	 * @param mismatchcategory
	 * @param invoiceMismatchUpdateDTO
	 * @return
	 */
	@PutMapping("sapmismatches/{mismatchcategory}/action")
	public ResponseEntity<ApiStatus<InvoiceMismatchUpdateDTO>> uploadPDF(@PathVariable String mismatchcategory,
			InvoiceMismatchUpdateDTO invoiceMismatchUpdateDTO) {
		sapMismatchService.updateMismatchByActionForSap(invoiceMismatchUpdateDTO);

		logger.info("Button value----: {}", invoiceMismatchUpdateDTO.getActionTaken());
		logger.info("Action Tds Rate----: {}", invoiceMismatchUpdateDTO.getFinalTdsRate());
		logger.info("Action Final Reason---: {}", invoiceMismatchUpdateDTO.getActionReason());
		logger.info("Action Final Section Rate---: {}", invoiceMismatchUpdateDTO.getFinalTdsSection());
		ApiStatus<InvoiceMismatchUpdateDTO> apiStatus = new ApiStatus<InvoiceMismatchUpdateDTO>(HttpStatus.OK,
				"SUCCESS", "UPDATE MISMATCH UPDATED SUCCESSFULLY", invoiceMismatchUpdateDTO);
		return new ResponseEntity<ApiStatus<InvoiceMismatchUpdateDTO>>(apiStatus, HttpStatus.OK);
	}

	// Import and Export Remediation Report
	//TODO NEED TO CHANGE FOR SQL
/*	@PostMapping("sapmismatches/remediation/import")
	public ResponseEntity<ApiStatus<BatchUpload>> getStatusOfSapRemediationReport(
			@RequestParam("mismatchCategory") String mismatchCategory, @RequestParam("file") MultipartFile file,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			BatchUpload batchUpload = sapMismatchService.importToBatchUploadForSap(file, tenantId);

			logger.info("SAP remediation report status---: {}", batchUpload);
			ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
					"UPLOAD REMEDIATION REPORT FOR SAP BASED ON BATCH ID", batchUpload);
			return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	} */

	@GetMapping("sapmismatchesreport/{batchId}/{mismatchcategory}/export")
	public ResponseEntity<InputStreamResource> exportSapRemediationReport(@PathVariable UUID batchId,
			@PathVariable String mismatchcategory) throws IOException {
		ByteArrayInputStream in = sapMismatchService.exportRemediationReport(batchId, mismatchcategory);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Remediation_Report.xlsx");

		logger.info("SAP remediation report export---: {}", in);
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
	}

	//TODO NEED TO CHANGE FOR SQL
/*	@PutMapping("sapmismatches/updateremediationreport/import")
	public ResponseEntity<ApiStatus<String>> updateSapRemediationReport(@RequestParam("file") MultipartFile file,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		String response = null;
		if (file.isEmpty()) {
			logger.error("Please select the file");
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {

			String contentType = new Tika().detect(file.getInputStream());
			if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
					|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
				response = sapMismatchService.updateRemediationReportForSap(file, tenantId, deductorTan);
				logger.info("Update SAP remediation report---: {}", response);
				ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
						"UPDATE MISMATCHES FOR UPLOADED REMEDIATION FILE", response);
				return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
			} else {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
			}
		}
	}  */
}
