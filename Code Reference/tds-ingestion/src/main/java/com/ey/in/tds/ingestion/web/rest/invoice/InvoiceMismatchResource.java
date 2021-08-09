package com.ey.in.tds.ingestion.web.rest.invoice;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.dto.invoice.InvoiceMismatchDTO;
import com.ey.in.tds.ingestion.service.batchupload.BatchUploadService;
import com.ey.in.tds.ingestion.service.invoicemismatch.InvoiceMismatchService;
import com.microsoft.azure.storage.StorageException;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;

@RestController
@RequestMapping("/api/ingestion")
public class InvoiceMismatchResource {

	@Autowired
	private InvoiceMismatchService invoiceMismatchService;
	
	@Autowired
	private BlobStorageService blobStorageService;
	
	@Autowired
	private BatchUploadService batchUploadService;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Update mismatch table based on Mismatch category for Pdf
	 * 
	 * @param mismatchcategory
	 * @param invoiceMismatchUpdateDTO
	 * @return
	 * @throws RecordNotFoundException
	 * @throws TanNotFoundException
	 */
	@PutMapping("tdsmismatches/invoice/pdf/{mismatchcategory}/action")
	public ResponseEntity<ApiStatus<InvoiceMismatchDTO>> uploadPDF(@PathVariable String mismatchcategory,
			@RequestBody InvoiceMismatchDTO invoiceMismatchUpdateDTO, @RequestHeader(value = "TAN-NUMBER") String tan)
			throws RecordNotFoundException, TanNotFoundException {
		if (tan == null) {
			logger.error("Tan Not Found");
			throw new TanNotFoundException("Tan Not Found");
		}
		InvoiceMismatchDTO response = invoiceMismatchService.updateMismatchByActionForPdf(tan,
				invoiceMismatchUpdateDTO);

		ApiStatus<InvoiceMismatchDTO> apiStatus = new ApiStatus<InvoiceMismatchDTO>(HttpStatus.OK, "SUCCESS",
				"LIST OF MISMATCHES FOR PDF TO UPDATE", response);
		return new ResponseEntity<ApiStatus<InvoiceMismatchDTO>>(apiStatus, HttpStatus.OK);

	}

	@PostMapping("tdsmismatches/remediation/import")
	public ResponseEntity<ApiStatus<BatchUpload>> getStatusOfRemediationReport(@RequestParam("file") MultipartFile file,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			BatchUpload batchUpload = invoiceMismatchService.importToBatchUpload(file, tenantId, userName);
			// TODO: To get the imported file and store it in another table for
			// batch_upload_remediation table

			logger.info("Remediation report status: {} ", batchUpload.getFileName());

			ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
					"UPLOADED REMEDIATION FILE SUCCESSFULLY", batchUpload);
			return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * 
	 * @param tenantId
	 * @param tan
	 * @param pan
	 * @param year
	 * @param month
	 * @param isMismatch
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws TanNotFoundException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	@PostMapping(value = "/tdsmismatchesreport/async/export/{isMismatch}")
	public ResponseEntity<ApiStatus<String>> asyncExportRemediationReport(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER", required = true) String tan,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String pan,
			@RequestParam(value = "year", required = true) Integer year,
			@RequestParam(value = "month", required = true) Integer month,
			@PathVariable(value = "isMismatch", required = false) boolean isMismatch,
			@RequestHeader(value = "USER_NAME") String userName)
			throws IOException, TanNotFoundException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);

		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}

		invoiceMismatchService.asyncExportRemediationReport(tan, tenantId, pan, year, month, userName, isMismatch);

		String message = "Invoice mismatch report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param response
	 * @param deductorTan
	 * @param deductorPan
	 * @param batchId
	 * @param tenantId
	 * @return
	 * @throws IOException
	 * @throws ParseException 
	 */
	@PostMapping(value = "/invoice/download/stream", consumes = { "application/x-www-form-urlencoded" })
	ResponseEntity<StreamingResponseBody> getStream(HttpServletResponse response,
			@RequestParam("deductorTan") String deductorTan,
			@RequestParam("deductorPan") String deductorPan,
			@RequestParam("batchId") String batchId, @RequestHeader("X-TENANT-ID") String tenantId) throws IOException, ParseException {

		Calendar calendar=Calendar.getInstance();
		String fileName="invoice_mismach_report_"+deductorTan+"_"+calendar.get(Calendar.YEAR)+"_"+calendar.get(Calendar.MONTH);

		response.addHeader("Content-disposition", "attachment;filename="+fileName+".zip");
		response.setContentType("application/octet-stream");

		logger.info("date :{}", Calendar.getInstance().toInstant());

		BatchUpload batchUpload = batchUploadService.getBatchUpload(Integer.valueOf(batchId));
		File filezip = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getOtherFileUrl());

		File csvFile = File.createTempFile("blob", "csv");
		invoiceMismatchService.decompressGzip(filezip.toPath(), csvFile.toPath());

		CsvReader csvReader = new CsvReader();

		csvReader.setContainsHeader(true);

		CsvContainer csv = csvReader.read(csvFile, StandardCharsets.UTF_8);
		ByteArrayInputStream byteArrayInputStream = invoiceMismatchService.generateMismatchExcel(csv.getRowCount(), csv,
				deductorTan, tenantId, deductorPan);
		
		StreamingResponseBody stream = out -> {

			final ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());

			try {
				final ZipEntry zipEntry = new ZipEntry(fileName+".xlsx");
				zipOut.putNextEntry(zipEntry);
				byte[] bytes = new byte[1024];
				int length;
				while ((length = byteArrayInputStream.read(bytes)) >= 0) {
					zipOut.write(bytes, 0, length);
				}
				zipOut.closeEntry();
				byteArrayInputStream.close();
				zipOut.close();
				
				
			} catch (final IOException e) {
				logger.error("Exception while reading and streaming data {} ", e);
			}
		};

		// Cleaning up the Temp Files Created
		filezip.delete();
		csvFile.delete();
		response.setStatus(HttpServletResponse.SC_OK);

		return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
	}


}
