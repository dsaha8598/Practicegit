package com.ey.in.tds.ingestion.service.error.download;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.ingestion.service.advance.AdvanceService;
import com.ey.in.tds.ingestion.service.batchupload.BatchUploadService;
import com.ey.in.tds.ingestion.service.batchupload.TCSBatchUploadService;
import com.ey.in.tds.ingestion.service.glmismatch.GeneralLedgerService;
import com.ey.in.tds.ingestion.service.invoicelineitem.InvoiceLineItemService;
import com.ey.in.tds.ingestion.service.invoiceshareholder.InvoiceShareholderService;
import com.ey.in.tds.ingestion.service.ldcerrorreport.LdcReportsService;
import com.ey.in.tds.ingestion.service.panerrorreport.PanReportsService;
import com.ey.in.tds.ingestion.service.provision.ProvisionService;
import com.ey.in.tds.ingestion.tcs.service.PaymentService;
import com.ey.in.tds.ingestion.tcs.service.TCSInvoiceLineItemService;

@Service
public class FileDownloadService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BatchUploadService batchUploadService;
	
	@Autowired
	private TCSBatchUploadService tcsBatchUploadService;

	@Autowired
	private BlobStorage blobStorage;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private InvoiceLineItemService invoiceLineItemService;

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
	private GeneralLedgerService generalLedgerService;
	
	@Autowired
	private TCSInvoiceLineItemService tcsInvoiceLineItemService;
	
	@Autowired
	private InvoiceShareholderService invoiceShareholderService;

	@Async
	public void asyncDownLoadFile(String deductorTan, String deductorPan, String typeOfDownload, String uploadType,
			Integer assesmentYear, String batchId, String tenantId, String userName) throws IOException {
		
		MultiTenantContext.setTenantId(tenantId);
		
		File file = null;
		Integer integerBatchId = Integer.valueOf(batchId);

		logger.info("typeOfDownload : {}", typeOfDownload);
		logger.info("deductorTan : {}", deductorTan);
		logger.info("uploadType : {}", uploadType);

		BatchUpload batchUpload = batchUploadService
				.get(assesmentYear, deductorTan, uploadType, integerBatchId);

		if (batchUpload == null) {
			throw new CustomException("No Record found for Batch Upload", HttpStatus.BAD_REQUEST);
		}
		if ("ERROR".equalsIgnoreCase(typeOfDownload) ||
				"NO_TDS".equalsIgnoreCase(typeOfDownload)) {
			logger.info("IN Error : {}", typeOfDownload);
			
			if ("ERROR".equalsIgnoreCase(typeOfDownload)) {
				file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getErrorFilePath());
			} else if ("NO_TDS".equalsIgnoreCase(typeOfDownload)) {
				file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getNotdsFileUrl());
			}
			BatchUpload requestBatchUpload = new BatchUpload();
			
			requestBatchUpload.setAssessmentYear(assesmentYear);
			requestBatchUpload.setDeductorMasterTan(deductorTan);
			if ("ERROR".equalsIgnoreCase(typeOfDownload)) {
				requestBatchUpload.setUploadType("ERROR_REPORT");
			} else if ("NO_TDS".equalsIgnoreCase(typeOfDownload)) {
				requestBatchUpload.setUploadType("NO_TDS");
			}
			requestBatchUpload.setStatus("Requested");
			requestBatchUpload.setFileName(batchUpload.getFileName());
			requestBatchUpload.setFilePath(batchUpload.getFilePath());
			requestBatchUpload.setSuccessFileUrl("");
			requestBatchUpload.setCreatedDate(new Timestamp(System.currentTimeMillis()));
			requestBatchUpload.setCreatedBy(userName);
			requestBatchUpload.setModifiedBy(userName);
			requestBatchUpload.setModifiedDate(new Timestamp(System.currentTimeMillis()));
			requestBatchUpload.setActive(true);
			batchUpload.setProcessStartTime(new Timestamp(System.currentTimeMillis()));
			requestBatchUpload.setSuccessCount(0L);
			requestBatchUpload.setFailedCount(0L);
			requestBatchUpload.setRowsCount(1L);
			requestBatchUpload = batchUploadService.create(requestBatchUpload);
			
			/**fetching file after creating batch record, because if file with large data is there then 
			 * it  is taking time to download the file and we arae not seeing the status as processinng in UI
			 */
			if ("ERROR".equalsIgnoreCase(typeOfDownload)) {
				file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getErrorFilePath());
			} else if ("NO_TDS".equalsIgnoreCase(typeOfDownload)) {
				file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getNotdsFileUrl());
			}
			
			try {
				if (uploadType.equalsIgnoreCase("INVOICE_EXCEL") || "INVOICE_SAP".equalsIgnoreCase(uploadType)) {
					file = invoiceLineItemService.convertInvoiceCsvToxlsx(file, deductorTan, tenantId, deductorPan,batchUpload.getAssessmentYear(),batchUpload.getBatchUploadID());
				} else if (uploadType.equalsIgnoreCase("PROVISIONS") || "PROVISION_SAP".equalsIgnoreCase(uploadType)) {
					file = provisionService.convertProvisionCsvToXlsx(file, deductorTan, tenantId, deductorPan,batchUpload.getAssessmentYear(),batchUpload.getBatchUploadID());
				} else if (uploadType.equalsIgnoreCase("ADVANCES") || "ADVANCE_SAP".equalsIgnoreCase(uploadType)) {
					file = advanceService.convertAdvanceCsvToXlsx(file, deductorTan, tenantId, deductorPan,batchUpload.getAssessmentYear(),batchUpload.getBatchUploadID());
				} else if (uploadType.equalsIgnoreCase("LDC_VALIDATION_EXCEL")) {
					file = ldcErrorReportService.convertLdcCsvToxlsx(file, deductorTan, tenantId, deductorPan);
				} else if (uploadType.equalsIgnoreCase("PAN_VALIDATION_EXCEL")) {
					file = panErrorReportService.convertPanCsvToxlsx(file, deductorTan, tenantId, deductorPan);
				} else if (uploadType.equalsIgnoreCase("GL") || "GL_SAP".equalsIgnoreCase(uploadType)) {
					file = generalLedgerService.convertGlCsvToXlsx(file, deductorTan, tenantId, deductorPan,batchUpload.getAssessmentYear(),batchUpload.getBatchUploadID());
				} else if (uploadType.equalsIgnoreCase("dividend_payout_invoice_processing") || "dividend_payout_invoice_processing".equalsIgnoreCase(uploadType)) {
					logger.info("generating payout error file {}");
					file = invoiceShareholderService.convertIvoiceShareHolderCsvToXlsx(file, deductorTan, tenantId, deductorPan,batchUpload.getAssessmentYear(),batchUpload.getBatchUploadID());
				}else if (uploadType.equalsIgnoreCase("RESIDENT_UPLOAD_SUMMERY") || "NR_UPLOAD_SUMMERY".equalsIgnoreCase(uploadType)) {
					logger.info("generating payout error file {}");
					file = invoiceShareholderService.generateSummeryErrorReport(file, deductorTan, tenantId, deductorPan,batchUpload.getAssessmentYear(),batchUpload.getBatchUploadID());
				}
				logger.info("Error File got generated successfully {}");
				String path = blobStorage.uploadExcelToBlobWithFile(file, tenantId);
				requestBatchUpload.setStatus("Processed");
				requestBatchUpload.setSuccessFileUrl(path);
				requestBatchUpload.setModifiedBy(userName);
				requestBatchUpload.setModifiedDate(new Timestamp(System.currentTimeMillis()));
				requestBatchUpload.setProcessEndTime(new Timestamp(System.currentTimeMillis()));
				requestBatchUpload.setSuccessCount(1L);
				requestBatchUpload.setFailedCount(0L);
				requestBatchUpload.setRowsCount(1L);
				requestBatchUpload = batchUploadService.update(requestBatchUpload);
			} catch (Exception e) {
				logger.info(userName);
				logger.info("Exception occured -:"+e);
				requestBatchUpload.setStatus("Failed");
				requestBatchUpload.setModifiedBy(userName);
				requestBatchUpload.setModifiedDate(new Date());
				requestBatchUpload.setSuccessCount(0L);
				requestBatchUpload.setFailedCount(1L);
				requestBatchUpload.setRowsCount(1L);
				requestBatchUpload = batchUploadService.update(requestBatchUpload);
			}

		}
	}
	/**
	 * 
	 * @param deductorTan
	 * @param deductorPan
	 * @param typeOfDownload
	 * @param uploadType
	 * @param assesmentYear
	 * @param batchId
	 * @param tenantId
	 * @param userName
	 * @throws IOException
	 */
	@Async
	public void asyncDownLoadTCSModuleFile(String deductorTan, String deductorPan, String typeOfDownload, String uploadType,
			Integer assesmentYear, String batchId, String tenantId, String userName) throws IOException {
		
		MultiTenantContext.setTenantId(tenantId);
		
		File file = null;
		Integer integerBatchId = Integer.valueOf(batchId);

		logger.info("typeOfDownload : {}", typeOfDownload);
		logger.info("deductorTan : {}", deductorTan);
		logger.info("uploadType : {}", uploadType);

		TCSBatchUpload batchUpload = tcsBatchUploadService
				.getTCSBatchUpload(assesmentYear, deductorTan, uploadType, integerBatchId);

		if (batchUpload == null) {
			throw new CustomException("No Record found for Batch Upload", HttpStatus.BAD_REQUEST);
		}
		if (typeOfDownload.equalsIgnoreCase("ERROR")) {
			logger.info("IN Error : {}", typeOfDownload);
			file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getErrorFilePath());

			TCSBatchUpload requestBatchUpload = new TCSBatchUpload();
			
			requestBatchUpload.setAssessmentYear(assesmentYear);
			requestBatchUpload.setCollectorMasterTan(deductorTan);
			requestBatchUpload.setUploadType("ERROR_REPORT");
			requestBatchUpload.setStatus("Requested");
			requestBatchUpload.setFileName(batchUpload.getFileName());
			requestBatchUpload.setFilePath(batchUpload.getFilePath());
			requestBatchUpload.setSuccessFileUrl("");
			requestBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			requestBatchUpload.setCreatedBy(userName);
			requestBatchUpload.setModifiedBy(userName);
			requestBatchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			requestBatchUpload.setActive(true);
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			requestBatchUpload.setSuccessCount(0L);
			requestBatchUpload.setFailedCount(0L);
			requestBatchUpload.setRowsCount(1L);
			requestBatchUpload = batchUploadService.createTCSBatchUpload(requestBatchUpload);
			try {
				if (uploadType.equalsIgnoreCase("INVOICE_EXCEL") || "INVOICE_SAP".equalsIgnoreCase(uploadType)) {
					file = tcsInvoiceLineItemService.convertInvoiceCsvToxlsx(file, deductorTan, tenantId, deductorPan,
							assesmentYear, batchUpload);
				} else if (uploadType.equalsIgnoreCase("ADVANCES") || "ADVANCE_SAP".equalsIgnoreCase(uploadType)
							|| uploadType.equalsIgnoreCase("COLLECTIONS")) {
					file = paymentService.convertPaymentCsvToXlsx(file, deductorTan, tenantId, deductorPan,assesmentYear, batchUpload);
				} else if (uploadType.equalsIgnoreCase("LDC_VALIDATION_EXCEL")) {
					file = ldcErrorReportService.convertLdcCsvToxlsx(file, deductorTan, tenantId, deductorPan);
				} else if (uploadType.equalsIgnoreCase("PAN_VALIDATION_EXCEL")) {
					file = panErrorReportService.convertPanCsvToxlsx(file, deductorTan, tenantId, deductorPan);
				} else if (uploadType.equalsIgnoreCase("GL") || "GL_SAP".equalsIgnoreCase(uploadType)) {
					file = generalLedgerService.convertGlCsvToXlsx(file, deductorTan, tenantId, deductorPan,batchUpload.getAssessmentYear(),batchUpload.getId());
				}
				String path = blobStorage.uploadExcelToBlobWithFile(file, tenantId);
				requestBatchUpload.setStatus("Processed");
				requestBatchUpload.setSuccessFileUrl(path);
				requestBatchUpload.setModifiedBy(userName);
				requestBatchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				requestBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				requestBatchUpload.setSuccessCount(1L);
				requestBatchUpload.setFailedCount(0L);
				requestBatchUpload.setRowsCount(1L);
				requestBatchUpload = tcsBatchUploadService.update(requestBatchUpload);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Exception occured while generating error report from csv :" + e);
				requestBatchUpload.setStatus("Failed");
				requestBatchUpload.setModifiedBy(userName);
				requestBatchUpload.setModifiedDate(new Date());
				requestBatchUpload.setSuccessCount(0L);
				requestBatchUpload.setFailedCount(1L);
				requestBatchUpload.setRowsCount(1L);
				requestBatchUpload = tcsBatchUploadService.update(requestBatchUpload);
			}

		}
	}

}
