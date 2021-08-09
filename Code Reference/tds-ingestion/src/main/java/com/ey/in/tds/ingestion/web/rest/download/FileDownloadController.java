package com.ey.in.tds.ingestion.web.rest.download;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aspose.cells.Cell;
import com.aspose.cells.Cells;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.service.advance.AdvanceService;
import com.ey.in.tds.ingestion.service.batchupload.BatchUploadService;
import com.ey.in.tds.ingestion.service.batchupload.TCSBatchUploadService;
import com.ey.in.tds.ingestion.service.error.download.FileDownloadService;
import com.ey.in.tds.ingestion.service.glmismatch.GeneralLedgerService;
import com.ey.in.tds.ingestion.service.glmismatch.tcs.TcsGeneralLedgerService;
import com.ey.in.tds.ingestion.service.invoicelineitem.InvoiceLineItemService;
import com.ey.in.tds.ingestion.service.ldcerrorreport.LdcReportsService;
import com.ey.in.tds.ingestion.service.panerrorreport.PanReportsService;
import com.ey.in.tds.ingestion.service.provision.ProvisionService;
import com.ey.in.tds.ingestion.tcs.service.PaymentService;
import com.ey.in.tds.ingestion.tcs.service.TCSInvoiceLineItemService;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;

@Controller
@RequestMapping("/api/ingestion")
public class FileDownloadController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BatchUploadService batchUploadService;

	@Autowired
	private TCSBatchUploadService tcsBatchUploadService;

	@Autowired
	private InvoiceLineItemService invoiceLineItemService;

	@Autowired
	private TCSInvoiceLineItemService tcsInvoiceLineItemService;

	@Autowired
	private ProvisionService provisionService;

	@Autowired
	private AdvanceService advanceService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private LdcReportsService ldcErrorReportService;

	@Autowired
	private PanReportsService panErrorReportService;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private GeneralLedgerService generalLedgerService;

	@Autowired
	private FileDownloadService fileDownloadService;

	@Autowired
	private TcsGeneralLedgerService tcsGeneralLedgerService;

	@PostMapping("/batch/download/request")
	public ResponseEntity<ApiStatus<String>> downloadFileRequest(@RequestHeader("TAN-NUMBER") String deductorTan,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> requestObj)
			throws Exception {
		String batchId = requestObj.get("batchId");
		String typeOfDownload = requestObj.get("typeOfDownload");
		Integer assessmentYear = Integer.parseInt(requestObj.get("assessmentYear"));
		String uploadType = requestObj.get("uploadType");

		fileDownloadService.asyncDownLoadFile(deductorTan, deductorPan, typeOfDownload, uploadType, assessmentYear,
				batchId, tenantId, userName);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "Generating report", "SUCCESS");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/batch/download", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<Resource> downloadFile(@RequestParam("deductorTan") String deductorTan,
			@RequestParam("deductorPan") String deductorPan, @RequestParam("typeOfDownload") String typeOfDownload,
			@RequestParam("uploadType") String uploadType, @RequestParam("assesmentYear") Integer assesmentYear,
			@RequestParam("batchId") String batchId, @RequestHeader("X-TENANT-ID") String tenantId) throws Exception {

		HttpHeaders header = new HttpHeaders();
		try {
			File file = null;
			Integer intgerBatchId = Integer.valueOf(batchId);

			logger.info("typeOfDownload :{} uploadType :{}", typeOfDownload, uploadType);
			logger.info("deductorTan :{}", deductorTan);

			BatchUpload batchUpload = batchUploadService.get(assesmentYear, deductorTan, uploadType, intgerBatchId);

			if (batchUpload == null) {
				throw new CustomException("No Record found for Batch Upload", HttpStatus.BAD_REQUEST);
			}
			if (typeOfDownload.equalsIgnoreCase("UPLOADED")) {
				if (StringUtils.isNotBlank(uploadType) && uploadType.contains("SAP")
						&& !"INVOICE_PDF".equalsIgnoreCase(uploadType)) {
					file = blobStorageService.getFileFromBlobFileName(tenantId, batchUpload.getFilePath(),
							batchUpload.getFileName());
				}else if(batchUpload.getUploadType().equals("NR_DOWNLOAD_SUMMERY") || batchUpload.getUploadType().equals("RESIDENT_DOWNLOAD_SUMMERY")
						|| batchUpload.getUploadType().equals("dividend_payout_invoice_processing")) {
					file=blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getOtherFileUrl());
				}
				else {
					file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());
				}
			} else if (typeOfDownload.equalsIgnoreCase("SUCCESS")) {
				file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getSuccessFileUrl());
				if (uploadType.equalsIgnoreCase("PAN_VALIDATION_EXCEL") || uploadType.equalsIgnoreCase("SHAREHOLDER_PAN_VALIDATION_EXCEL")
						||uploadType.equalsIgnoreCase("SHAREHOLDER_EXCEL") || uploadType.equalsIgnoreCase("DEDUCTEE_EXCEL")) {
					file = panErrorReportService.convertPanSucessCsvToxlsx(file);
				} else if (uploadType.equalsIgnoreCase("LDC_VALIDATION_EXCEL")) {
					file = ldcErrorReportService.convertLdcSucessCsvToxlsx(file);
				}
			} else if (typeOfDownload.equalsIgnoreCase("OTHER")) {
				file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getOtherFileUrl());
				if (uploadType.equalsIgnoreCase("INVOICE_EXCEL") || "INVOICE_SAP".equalsIgnoreCase(uploadType)) {
					file = invoiceLineItemService.convertInvoiceOtherReportToXlsx(file, deductorTan);
				} else if (uploadType.equalsIgnoreCase("PROVISIONS") || "PROVISION_SAP".equalsIgnoreCase(uploadType)) {
					file = provisionService.convertProvisionOtherReportToXlsx(file, deductorTan);
				} else if (uploadType.equalsIgnoreCase("ADVANCES") || "ADVANCE_SAP".equalsIgnoreCase(uploadType)) {
					file = advanceService.convertAdvanceOtherReportToXlsx(file, deductorTan);
				} else if (uploadType.equalsIgnoreCase("LDC_EXCEL")) {
					file = ldcErrorReportService.convertLdcCsvToxlsx(file, deductorTan, tenantId, deductorPan);
				}
			} else if (typeOfDownload.equalsIgnoreCase("ERROR")) {
				if (StringUtils.isBlank(batchUpload.getErrorFilePath())) {
					throw new CustomException("No errors exists");
				}
				file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getErrorFilePath());
				if (uploadType.equalsIgnoreCase("INVOICE_EXCEL") || "INVOICE_SAP".equalsIgnoreCase(uploadType)) {
					file = invoiceLineItemService.convertInvoiceCsvToxlsx(file, deductorTan, tenantId, deductorPan,
							batchUpload.getAssessmentYear(), batchUpload.getBatchUploadID());
				} else if (uploadType.equalsIgnoreCase("PROVISIONS") || "PROVISION_SAP".equalsIgnoreCase(uploadType)) {
					file = provisionService.convertProvisionCsvToXlsx(file, deductorTan, tenantId, deductorPan,
							batchUpload.getAssessmentYear(), batchUpload.getBatchUploadID());
				} else if (uploadType.equalsIgnoreCase("ADVANCES") || "ADVANCE_SAP".equalsIgnoreCase(uploadType)) {
					file = advanceService.convertAdvanceCsvToXlsx(file, deductorTan, tenantId, deductorPan,
							batchUpload.getAssessmentYear(), batchUpload.getBatchUploadID());
				} else if (uploadType.equalsIgnoreCase("LDC_VALIDATION_EXCEL")) {
					file = ldcErrorReportService.convertLdcCsvToxlsx(file, deductorTan, tenantId, deductorPan);
				} else if (uploadType.equalsIgnoreCase("PAN_VALIDATION_EXCEL")) {
					file = panErrorReportService.convertPanCsvToxlsx(file, deductorTan, tenantId, deductorPan);
				} else if (uploadType.equalsIgnoreCase("GL") || "GL_SAP".equalsIgnoreCase(uploadType)) {
					file = generalLedgerService.convertGlCsvToXlsx(file, deductorTan, tenantId, deductorPan,
							batchUpload.getAssessmentYear(), batchUpload.getBatchUploadID());
				}
			} else if (typeOfDownload.equalsIgnoreCase("NO_TDS")) {
				if (StringUtils.isBlank(batchUpload.getNotdsFileUrl())) {
					throw new CustomException("No TDS exists");
				}
				file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getNotdsFileUrl());
				if (uploadType.equalsIgnoreCase("INVOICE_EXCEL") || "INVOICE_SAP".equalsIgnoreCase(uploadType)) {
					file = invoiceLineItemService.convertInvoiceCsvToxlsx(file, deductorTan, tenantId, deductorPan,
							batchUpload.getAssessmentYear(), batchUpload.getBatchUploadID());
				} else if (uploadType.equalsIgnoreCase("PROVISIONS") || "PROVISION_SAP".equalsIgnoreCase(uploadType)) {
					file = provisionService.convertProvisionCsvToXlsx(file, deductorTan, tenantId, deductorPan,
							batchUpload.getAssessmentYear(), batchUpload.getBatchUploadID());
				} else if (uploadType.equalsIgnoreCase("ADVANCES") || "ADVANCE_SAP".equalsIgnoreCase(uploadType)) {
					file = advanceService.convertAdvanceCsvToXlsx(file, deductorTan, tenantId, deductorPan,
							batchUpload.getAssessmentYear(), batchUpload.getBatchUploadID());
				}
			}
			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		} catch (Exception e) {
			logger.error("Exception occured while fetching the file from blob", e);
			// Downloading exception in file
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			Cells cells = worksheet.getCells();
			worksheet.setName("Error Details");
			Cell cell = cells.get("A1");
			cell.setValue("Error Message");
			// Adding a double value to the cell
			cell = cells.get("A2");
			cell.setValue(e.getMessage());
			// Write the output to the file
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.XLSX);
			File file = File.createTempFile("Error_File", ".xlsx");
			FileUtils.writeByteArrayToFile(file, baout.toByteArray());

			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		}

	}

	@PostMapping(value = "/gl/download/zip", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<Resource> downloadFillingFiles(@RequestParam("deductorTan") String deductorTan,
			@RequestParam("deductorPan") String deductorPan, @RequestParam("typeOfDownload") String typeOfDownload,
			@RequestParam("uploadType") String uploadType, @RequestParam("assesmentYear") Integer assesmentYear,
			@RequestParam("batchId") String batchId, @RequestHeader("X-TENANT-ID") String tenantId) throws Exception {

		HttpHeaders header = new HttpHeaders();
		try {
			File file = null;
			List<BatchUpload> batchUploadResponse = batchUploadService.getBatchUploadByTypeAndId(uploadType,
					Integer.valueOf(batchId));
			String zipFileName = "GL_" + batchId.concat(".zip");
			// generating zip file
			FileOutputStream fos = new FileOutputStream(zipFileName);
			ZipOutputStream zos = new ZipOutputStream(fos);
			if (!batchUploadResponse.isEmpty()) {
				BatchUpload batchUpload = batchUploadResponse.get(0);
				List<String> glBlobUrls = new ArrayList<>();
				glBlobUrls.add(batchUpload.getFilePath());
				glBlobUrls.add(batchUpload.getSuccessFileUrl());
				glBlobUrls.add(batchUpload.getOtherFileUrl());
				glBlobUrls.add(batchUpload.getSourceFilePath());
				glBlobUrls.add(batchUpload.getErrorFilePath());

				for (String blobUrl : glBlobUrls) {
					if (StringUtils.isNotBlank(blobUrl)) {
						if (StringUtils.isNotBlank(uploadType) && uploadType.contains("SAP")) {
							file = blobStorageService.getFileFromBlobFileName(tenantId, blobUrl,
									batchUpload.getFileName());
						} else {
							file = blobStorageService.getFileFromBlobUrl(tenantId, blobUrl);
						}
						logger.info("Blob url : {}", blobUrl);
						zos.putNextEntry(new ZipEntry(file.getName()));
						byte[] bytes = Files.readAllBytes(file.toPath());
						zos.write(bytes, 0, bytes.length);
						zos.closeEntry();
					}
				}
				zos.close();
				fos.close();
			}

			FileSystemResource resource = new FileSystemResource(zipFileName);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentType(MediaType.parseMediaType("application/octet-stream"))
					.body(resource);
		} catch (Exception e) {
			logger.error("Exception occured while fetching the file from blob", e);
			// Downloading exception in file
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			Cells cells = worksheet.getCells();
			worksheet.setName("Error Details");
			Cell cell = cells.get("A1");
			cell.setValue("Error Message");
			// Adding a double value to the cell
			cell = cells.get("A2");
			cell.setValue(e);
			// Write the output to the file
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.XLSX);
			File file = File.createTempFile("Error_File", ".xlsx");
			FileUtils.writeByteArrayToFile(file, baout.toByteArray());

			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		}
	}

	@PostMapping(value = "/blob/download", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<Resource> downloadFile(@RequestParam("url") String blobURL,
			@RequestHeader("X-TENANT-ID") String tenantId) throws Exception {

		HttpHeaders header = new HttpHeaders();
		try {

			logger.info("BlobURL : {}", blobURL);

			File file = blobStorageService.getFileFromBlobUrl(tenantId, blobURL);

			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		} catch (Exception e) {
			logger.error("Exception occured while fetching the file from blob", e);
			// Downloading exception in file
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			Cells cells = worksheet.getCells();
			worksheet.setName("Error Details");
			Cell cell = cells.get("A1");
			cell.setValue("Error Message");
			// Adding a double value to the cell
			cell = cells.get("A2");
			cell.setValue(e);
			// Write the output to the file
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.XLSX);
			File file = File.createTempFile("Error_File", ".xlsx");
			FileUtils.writeByteArrayToFile(file, baout.toByteArray());

			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		}

	}

	@GetMapping(value = "/blob/download/pdf")
	public ResponseEntity<Resource> downloadPDFFile(@RequestParam(value = "blobUrl") String blobURL,
			@RequestParam(value = "tenantId") String tenantId, @RequestParam(value = "token") String token)
			throws IOException {
		HttpHeaders header = new HttpHeaders();

		logger.info("BlobURL : {}", blobURL);

		File file = blobStorageService.getFileFromBlobUrl(tenantId, blobURL);

		Resource resource = new FileSystemResource(file);

		header.add("Cache-Control", "no-cache, no-store, must-revalidate");
		header.add("Pragma", "no-cache");
		header.add("Expires", "0");

		return ResponseEntity.ok().headers(header).contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);

	}

	/**
	 * conatains logic download the files for TCS module
	 * 
	 * @param deductorTan
	 * @param deductorPan
	 * @param typeOfDownload
	 * @param uploadType
	 * @param assesmentYear
	 * @param batchId
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/tcs/batch/download", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<Resource> downloadTcsModuleFile(@RequestParam("deductorTan") String deductorTan,
			@RequestParam("deductorPan") String deductorPan, @RequestParam("typeOfDownload") String typeOfDownload,
			@RequestParam("uploadType") String uploadType, @RequestParam("assesmentYear") Integer assesmentYear,
			@RequestParam("batchId") String batchId, @RequestHeader("X-TENANT-ID") String tenantId) throws Exception {

		HttpHeaders header = new HttpHeaders();
		try {
			File file = null;
			Integer intgerBatchId = Integer.valueOf(batchId);

			logger.info("typeOfDownload :{} uploadType :{}", typeOfDownload, uploadType);
			logger.info("deductorTan :{}", deductorTan);

			TCSBatchUpload batchUpload = tcsBatchUploadService.getTCSBatchUpload(assesmentYear, deductorTan, uploadType,
					intgerBatchId);

			if (batchUpload == null) {
				throw new CustomException("No Record found for Batch Upload", HttpStatus.BAD_REQUEST);
			}
			if (typeOfDownload.equalsIgnoreCase("UPLOADED")) {
				if (StringUtils.isNotBlank(uploadType) && uploadType.contains("SAP")
						&& !"INVOICE_PDF".equalsIgnoreCase(uploadType)) {
					file = blobStorageService.getFileFromBlobFileName(tenantId, batchUpload.getFilePath(),
							batchUpload.getFileName());
				} else {
					file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());
				}
			} else if (typeOfDownload.equalsIgnoreCase("SUCCESS")) {
				file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getSuccessFileUrl());
				if (uploadType.equalsIgnoreCase("PAN_VALIDATION_EXCEL")) {
					file = panErrorReportService.convertPanSucessCsvToxlsx(file);
				} else if (uploadType.equalsIgnoreCase("LDC_VALIDATION_EXCEL")) {
					file = ldcErrorReportService.convertLdcSucessCsvToxlsx(file);
				}
			} else if (typeOfDownload.equalsIgnoreCase("OTHER")) {
				file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getOtherFileUrl());
				if (uploadType.equalsIgnoreCase("INVOICE_EXCEL") || "INVOICE_SAP".equalsIgnoreCase(uploadType)) {
					file = invoiceLineItemService.convertInvoiceOtherReportToXlsx(file, deductorTan);
				} else if (uploadType.equalsIgnoreCase("PROVISIONS") || "PROVISION_SAP".equalsIgnoreCase(uploadType)) {
					file = provisionService.convertProvisionOtherReportToXlsx(file, deductorTan);
				} else if (uploadType.equalsIgnoreCase("PAYMENT") || "PAYMENT_SAP".equalsIgnoreCase(uploadType)) {
					file = advanceService.convertAdvanceOtherReportToXlsx(file, deductorTan);
				} else if (uploadType.equalsIgnoreCase("GL")) {
					file = tcsGeneralLedgerService.convertGlCsvOtherToXlsx(file, deductorTan, tenantId, deductorPan);
				}
			} else if (typeOfDownload.equalsIgnoreCase("ERROR")) {
				if (StringUtils.isBlank(batchUpload.getErrorFilePath())) {
					throw new CustomException("No errors exists");
				}
				file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getErrorFilePath());
				if (uploadType.equalsIgnoreCase("INVOICE_EXCEL") || "INVOICE_SAP".equalsIgnoreCase(uploadType)) {
					file = tcsInvoiceLineItemService.convertInvoiceCsvToxlsx(file, deductorTan, tenantId, deductorPan,
							assesmentYear, batchUpload);
				} else if (uploadType.equalsIgnoreCase("ADVANCES") || "ADVANCE_SAP".equalsIgnoreCase(uploadType)
						|| uploadType.equalsIgnoreCase("COLLECTIONS")) {
					file = paymentService.convertPaymentCsvToXlsx(file, deductorTan, tenantId, deductorPan,
							assesmentYear, batchUpload);
				} else if (uploadType.equalsIgnoreCase("LDC_VALIDATION_EXCEL")) {
					file = ldcErrorReportService.convertLdcCsvToxlsx(file, deductorTan, tenantId, deductorPan);
				} else if (uploadType.equalsIgnoreCase("PAN_VALIDATION_EXCEL")) {
					file = panErrorReportService.convertPanCsvToxlsx(file, deductorTan, tenantId, deductorPan);
				} else if (uploadType.equalsIgnoreCase("GL") || "GL_SAP".equalsIgnoreCase(uploadType)) {
					file = generalLedgerService.convertGlCsvToXlsx(file, deductorTan, tenantId, deductorPan,batchUpload.getAssessmentYear(),batchUpload.getId());
				}

			}
			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		} catch (Exception e) {
			logger.error("Exception occured while fetching the file from blob", e);
			// Downloading exception in file
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			Cells cells = worksheet.getCells();
			worksheet.setName("Error Details");
			Cell cell = cells.get("A1");
			cell.setValue("Error Message");
			// Adding a double value to the cell
			cell = cells.get("A2");
			cell.setValue(e.getMessage());
			// Write the output to the file
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.XLSX);
			File file = File.createTempFile("Error_File", ".xlsx");
			FileUtils.writeByteArrayToFile(file, baout.toByteArray());

			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		}

	}

	/**
	 * prepares the error report,uploads to blob and creates record in batch upload
	 * 
	 * @param deductorTan
	 * @param deductorPan
	 * @param tenantId
	 * @param userName
	 * @param requestObj
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/tcs/batch/download/request")
	public ResponseEntity<ApiStatus<String>> downloadTCSModuleFileRequest(
			@RequestHeader("TAN-NUMBER") String deductorTan, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestBody Map<String, String> requestObj) throws Exception {
		String batchId = requestObj.get("batchId");
		String typeOfDownload = requestObj.get("typeOfDownload");
		Integer assessmentYear = Integer.parseInt(requestObj.get("assessmentYear"));
		String uploadType = requestObj.get("uploadType");
		fileDownloadService.asyncDownLoadTCSModuleFile(deductorTan, deductorPan, typeOfDownload, uploadType,
				assessmentYear, batchId, tenantId, userName);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "Generating report", "SUCCESS");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for download deductor excel files.
	 * 
	 * @param deductorTan
	 * @param deductorPan
	 * @param typeOfDownload
	 * @param uploadType
	 * @param assesmentYear
	 * @param batchId
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/deductor/batch/download", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<Resource> deductorDownloadFile(@RequestParam("deductorTan") String deductorTan,
			@RequestParam("deductorPan") String deductorPan, @RequestParam("typeOfDownload") String typeOfDownload,
			@RequestParam("uploadType") String uploadType, @RequestParam("assesmentYear") Integer assesmentYear,
			@RequestParam("batchId") String batchId, @RequestParam("tenantId") String tenantId) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("tenantId :{}", tenantId);
		HttpHeaders header = new HttpHeaders();
		try {
			File file = null;
			Integer intgerBatchId = Integer.valueOf(batchId);
			logger.info("typeOfDownload :{} uploadType :{}", typeOfDownload, uploadType);
			logger.info("deductorTan :{}", deductorTan);
			BatchUpload batchUpload = batchUploadService.get(assesmentYear, deductorTan, uploadType, intgerBatchId);

			if (batchUpload == null) {
				throw new CustomException("No Record found for Batch Upload", HttpStatus.BAD_REQUEST);
			}
			if (typeOfDownload.equalsIgnoreCase("UPLOADED")) {
				file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());
			} else if (typeOfDownload.equalsIgnoreCase("ERROR")) {
				if (StringUtils.isBlank(batchUpload.getErrorFilePath())) {
					throw new CustomException("No errors exists");
				}
				file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getErrorFilePath());
			}
			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		} catch (Exception e) {
			logger.error("Exception occured while fetching the file from blob", e);
			// Downloading exception in file
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			Cells cells = worksheet.getCells();
			worksheet.setName("Error Details");
			Cell cell = cells.get("A1");
			cell.setValue("Error Message");
			// Adding a double value to the cell
			cell = cells.get("A2");
			cell.setValue(e.getMessage());
			// Write the output to the file
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.XLSX);
			File file = File.createTempFile("Error_File", ".xlsx");
			FileUtils.writeByteArrayToFile(file, baout.toByteArray());

			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		}
	}

	@PostMapping(value = "/tcs/batch/download/gl", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<Resource> downloadTcsGLFile(@RequestParam("deductorTan") String deductorTan,
			@RequestParam("deductorPan") String deductorPan, @RequestParam("typeOfDownload") String typeOfDownload,
			@RequestParam("uploadType") String uploadType, @RequestParam("assesmentYear") Integer assesmentYear,
			@RequestParam("batchId") String batchId, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam(value = "blobUrl") String blobURL) throws Exception {

		HttpHeaders header = new HttpHeaders();
		try {
			File file = null;
			Integer intgerBatchId = Integer.valueOf(batchId);

			logger.info("typeOfDownload :{} uploadType :{}", typeOfDownload, uploadType);
			logger.info("deductorTan :{}", deductorTan);

			TCSBatchUpload batchUpload = tcsBatchUploadService.getTCSBatchUpload(assesmentYear, deductorTan, uploadType,
					intgerBatchId);

			if (batchUpload == null) {
				throw new CustomException("No Record found for Batch Upload", HttpStatus.BAD_REQUEST);
			}

			if (StringUtils.isBlank(blobURL)) {
				throw new CustomException("No errors exists");
			}
			file = blobStorageService.getFileFromBlobUrl(tenantId, blobURL);

			CsvReader csvReader = new CsvReader();
			csvReader.setContainsHeader(true);
			CsvContainer csv = csvReader.read(file, StandardCharsets.UTF_8);
			if (typeOfDownload.equalsIgnoreCase("SUCCESS")) {
				file = tcsGeneralLedgerService.convertSuccessGSTvsTCSCsvToXlsx(csv, deductorTan, tenantId, deductorPan,
						file.getName());
			} else if (typeOfDownload.equalsIgnoreCase("OTHER")) {
				file = tcsGeneralLedgerService.convertOtherGSTvsTCSCsvToXlsx(csv, deductorTan, tenantId, deductorPan,
						file.getName());
			} else if (typeOfDownload.equalsIgnoreCase("ERROR")) {
				file = tcsGeneralLedgerService.convertErrorGSTvsTCSCsvToXlsx(csv, deductorTan, tenantId, deductorPan,
						file.getName());
			}

			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		} catch (Exception e) {
			logger.error("Exception occured while fetching the file from blob", e);
			// Downloading exception in file
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			Cells cells = worksheet.getCells();
			worksheet.setName("Error Details");
			Cell cell = cells.get("A1");
			cell.setValue("Error Message");
			// Adding a double value to the cell
			cell = cells.get("A2");
			cell.setValue(e.getMessage());
			// Write the output to the file
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.XLSX);
			File file = File.createTempFile("Error_File", ".xlsx");
			FileUtils.writeByteArrayToFile(file, baout.toByteArray());

			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		}

	}

}