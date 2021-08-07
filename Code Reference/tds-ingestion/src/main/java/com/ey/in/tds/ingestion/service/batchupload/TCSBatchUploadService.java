package com.ey.in.tds.ingestion.service.batchupload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.config.TenantProperties;
import com.ey.in.tds.common.dashboard.dto.TCSActivityTracker;
import com.ey.in.tds.common.dashboards.jdbc.dao.TCSActivityTrackerDAO;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.ReconciliationTriggerRequest;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadGroupDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUploadGroup;
import com.ey.in.tds.common.dto.BatchProcessDTO;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.receipt.ReceiptDataDTO;
import com.ey.in.tds.common.ingestion.response.dto.RunStatus;
import com.ey.in.tds.common.ingestion.response.dto.SparkResponseDTO;
import com.ey.in.tds.common.ingestion.response.dto.SparkState;
import com.ey.in.tds.common.model.job.JarJob;
import com.ey.in.tds.common.model.job.NoteBookBatch;
import com.ey.in.tds.common.model.job.TCSNoteBookParam;
import com.ey.in.tds.common.model.job.TcsJob;
import com.ey.in.tds.common.onboarding.response.dto.DeductorOnboardingInformationResponseDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.tcs.service.TCSKeywordMasterService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.util.ActivityTrackerStatus;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.TenantConfiguration;
import com.ey.in.tds.core.util.TenantConfiguration.FileShareConfig;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.ChallanClient;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.config.SparkNotebooks;
import com.ey.in.tds.ingestion.dto.batch.BatchUploadDTO;
import com.ey.in.tds.ingestion.dto.batch.BatchUploadGroupCustomDTO;
import com.ey.in.tds.ingestion.dto.batch.ReceiptBatchUploadDTO;
import com.ey.in.tds.ingestion.dto.batchgroup.BatchUploadGroupDTO;
import com.ey.in.tds.ingestion.tcs.dao.TCSInvoiceLineItemDAO;
import com.ey.in.tds.ingestion.tcs.service.PaymentService;
import com.ey.in.tds.ingestion.tcs.service.TCSInvoiceLineItemService;
import com.ey.in.tds.ingestion.tcs.service.TCSPurchaseRegisterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;

/**
 * 
 * @author scriptbees
 *
 */
@Service
public class TCSBatchUploadService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private BlobStorage blob;

	@Value("${databricks.key}")
	private String dataBricksKey;

	@Value("${sparkjob.runstaus.url}")
	private String runStatusUrl;

	@Autowired
	private ChallanClient challanClient;

	@Autowired
	private TCSInvoiceLineItemService tcsInvoiceLineItemService;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private SparkNotebooks sparkNotebooks;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	private TenantProperties tenantProperties;

	@Autowired
	private MastersClient masterClient;

	@Autowired
	private TCSKeywordMasterService tcsKeywordMasterService;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired(required = false)
	private TCSBatchUploadDAO tcsBatchUploadDAO;

	@Autowired
	private TCSActivityTrackerDAO tcsActivityTrackerDAO;

	@Autowired
	private BatchUploadGroupDAO batchUploadGroupDAO;

	@Autowired
	private TCSInvoiceLineItemDAO tcsInvoiceLineItemDAO;
	
	@Autowired
	private TCSPurchaseRegisterService tcsPurchaseRegisterService;

	/**
	 * 
	 * @param batchUpload
	 * @return
	 */
	public TCSBatchUpload create(TCSBatchUpload batchUpload) {
		return tcsBatchUploadDAO.save(batchUpload);
	}

	/**
	 * 
	 * @param tcsBatchUpload
	 * @return
	 */
	public TCSBatchUpload update(TCSBatchUpload tcsBatchUpload) {
		return tcsBatchUploadDAO.update(tcsBatchUpload);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param type
	 * @param id
	 * @return
	 */
	public TCSBatchUpload get(int assessmentYear, String deductorTan, String type, Integer id) {
		List<TCSBatchUpload> batchUploadResponse = tcsBatchUploadDAO.findById(assessmentYear, deductorTan, type, id);
		if (!batchUploadResponse.isEmpty() && batchUploadResponse != null) {
			return batchUploadResponse.get(0);
		} else {
			throw new CustomException("Did not find a BatchUpload with the passed in criteria", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param pagination
	 * @return
	 */
	public List<TCSBatchUpload> getBatchUploadByAssessmentYear(int assessmentYear, Pagination pagination) {
		return tcsBatchUploadDAO.getBatchUploadsByAssessmentYear(assessmentYear);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param pagination
	 * @return
	 */
	public List<TCSBatchUpload> getBatchUploadByAssessmentYearDeductorTan(int assessmentYear, List<String> deductorTan,
			Pagination pagination) {
		return tcsBatchUploadDAO.getBatchUploadsByAssessmentYearDeductorTan(assessmentYear, deductorTan, pagination);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param type
	 * @param id
	 */
	public void deleteById(int assessmentYear, String deductorTan, String type, Integer id) {
		tcsBatchUploadDAO.deleteById(id);
	}

	/**
	 * 
	 * @param type
	 * @param tan
	 * @param year
	 * @return
	 */
	public List<BatchUploadDTO> getListOfBatchUploadFiles(String type, List<String> tan, int year) {

		List<BatchUploadDTO> batchListDTO = new LinkedList<BatchUploadDTO>();
		logger.info("Tenant Tan ---: {}", tan);
		List<TCSBatchUpload> batchUploadList = new ArrayList<>();
		if ("COLLECTEE_EXCEL".equalsIgnoreCase(type)) {
			batchUploadList = tcsBatchUploadDAO.getBatchList(year, tan, "COLLECTEE_SAP");
		} else if ("PAYMENT".equalsIgnoreCase(type)) {
			batchUploadList = tcsBatchUploadDAO.getBatchList(year, tan, "PAYMENT_SAP");
		} else if ("PROVISIONS".equalsIgnoreCase(type)) {
			batchUploadList = tcsBatchUploadDAO.getBatchList(year, tan, "PROVISION_SAP");
		} else if ("GL".equalsIgnoreCase(type)) {
			batchUploadList = tcsBatchUploadDAO.getBatchList(year, tan, "GL_SAP");
		} else if ("INVOICE_EXCEL".equalsIgnoreCase(type)) {
			batchUploadList = tcsBatchUploadDAO.getBatchList(year, tan, "INVOICE_SAP");
		}
		batchUploadList = tcsBatchUploadDAO.getBatchList(year, tan, type);
		Collections.reverse(batchUploadList);
		for (TCSBatchUpload batchUpload : batchUploadList) {
			BatchUploadDTO batchDTO = new BatchUploadDTO();
			batchDTO.setId(batchUpload.getId());
			batchDTO.setRunId(batchUpload.getRunId());
			batchDTO.setDateOfUpload(batchUpload.getProcessStartTime());
			if (batchUpload.getUploadType().contains("SAP") && StringUtils.isNotBlank(batchUpload.getFileName())) {
				batchDTO.setFileName(
						batchUpload.getFileName().substring(batchUpload.getFileName().lastIndexOf("/") + 1));
			} else {
				batchDTO.setFileName(batchUpload.getFileName());
			}
			batchDTO.setFileStatus(batchUpload.getStatus());
			batchDTO.setUploadedFileDownloadUrl(batchUpload.getFilePath());
			batchDTO.setUploadBy(batchUpload.getCreatedBy());
			if (batchUpload.getProcessEndTime() != null) {
				batchDTO.setProcessEndTime(batchUpload.getProcessEndTime());
			} else {
				batchDTO.setProcessEndTime(null);
			}
			if (batchUpload.getRowsCount() != null) {
				batchDTO.setTotalRecords(batchUpload.getRowsCount());
			} else {
				batchDTO.setTotalRecords(0L);
			}
			if (batchUpload.getDuplicateCount() != null) {
				batchDTO.setDuplicateRecords(batchUpload.getDuplicateCount());
			} else {
				batchDTO.setDuplicateRecords(0L);
			}

			if (batchUpload.getMismatchCount() != null) {
				batchDTO.setMismatchCount(batchUpload.getMismatchCount());
			} else {
				batchDTO.setMismatchCount(0L);
			}
			if (batchUpload.getFailedCount() != null) {
				batchDTO.setErrorRecords(batchUpload.getFailedCount());
			} else {
				batchDTO.setProcessedRecords(0L);
			}
			if (batchUpload.getProcessed() != null) {
				batchDTO.setProcessedRecords(batchUpload.getProcessed());
			} else {
				batchDTO.setProcessedRecords(0L);
			}
			batchDTO.setErrorFileDownloadUrl(batchUpload.getErrorFilePath());
			batchDTO.setSucessFileDownloadUrl(batchUpload.getSuccessFileUrl());
			batchDTO.setOtherFileDownloadUrl(batchUpload.getOtherFileUrl());
			batchDTO.setYear(batchUpload.getAssessmentYear());
			batchDTO.setSuccessCount(batchUpload.getSuccessCount());
			batchUpload.setAssessmentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
			batchDTO.setMonth(batchUpload.getAssessmentMonth());
			batchDTO.setTan(batchUpload.getCollectorMasterTan());
			if (batchUpload.getSourceIdentifier() != null && batchUpload.getUploadType().equals("reconciliation")) {
				batchDTO.setFileType(batchUpload.getSourceIdentifier());
			} else {
				batchDTO.setFileType(batchUpload.getUploadType());
			}
			batchListDTO.add(batchDTO);
		}

		return batchListDTO;
	}

	public List<TCSBatchUpload> getBatchUploads(String type, List<String> tan, int year) {
		return tcsBatchUploadDAO.getBatchList(year, tan, type);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param files
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 * @throws ParseException
	 */
	@Transactional
	public TCSBatchUpload saveUploadPDF(Integer assessmentYear, MultipartFile[] files, String tan, String type,
			String tenantId, String userName, String groupName, String postingDate, String residentType, String pan)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, ParseException {
		if (logger.isDebugEnabled()) {
			logger.debug("REST request for file upload Pdf's : {}, {}", files, tan);
		}
		int assessmentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
		String path;
		TCSBatchUpload batchUpload = null;
		if (files.length == 0) {
			throw new FileStorageException("Please select atleast one File");
		} else {

			int errorCount = 0;
			// To create a group record
			BatchUploadGroup batchGroup = new BatchUploadGroup();
			batchGroup.setAssessmentYear(assessmentYear);
			batchGroup.setTan(tan);
			batchGroup.setGroupTotalProcessedCount(0);
			batchGroup.setGroupChallanMonth(assessmentMonth);
			batchGroup.setGroupPostDocumentMonth(assessmentMonth);
			batchGroup.setGroupTotalErrorCount(errorCount);
			batchGroup.setGroupTotalRecordCount(files.length);
			batchGroup.setCreatedBy(userName);
			batchGroup.setCreatedDate(new Date());
			batchGroup.setModifiedBy(userName);
			batchGroup.setModifiedDate(new Date());
			batchGroup.setGroupUnderValidationCount(0);
			batchGroup.setGroupName(groupName);
			batchGroup.setPostingDateOfTheDocument(new SimpleDateFormat("dd/MM/yyyy").parse(postingDate));
			batchGroup.setResidentType(residentType);
			batchGroup = batchUploadGroupDAO.save(batchGroup);
			for (int i = 0; i < files.length; i++) {
				boolean isPdfFile = true;
				logger.info(files[i].getOriginalFilename());
				if (!FilenameUtils.getExtension(files[i].getOriginalFilename()).equalsIgnoreCase("pdf")) {
					++errorCount;
					isPdfFile = false;
				}
				String sha256 = sha256SumService.getSHA256Hash(files[i]);
				List<TCSBatchUpload> batch = tcsBatchUploadDAO.getSha256Records(sha256);
				path = blob.uploadExcelToBlob(files[i], tenantId);
				batchUpload = new TCSBatchUpload();
				batchUpload.setAssessmentYear(assessmentYear);
				batchUpload.setCollectorMasterTan(tan);
				batchUpload.setUploadType(type);
				if (!batch.isEmpty()) {
					logger.info("Duplicate PDF file Uploaded (: {} ) ", files[i].getOriginalFilename());
					batchUpload.setStatus("Duplicate");
					batchUpload.setSha256sum(sha256);
					batchUpload.setReferenceId(batch.get(0).getId());
				} else {
					batchUpload.setStatus("Uploaded");
					batchUpload.setSha256sum(sha256);
					logger.info("Unique PDF file uploaded (: {} )", files[i].getOriginalFilename());
				}
				if (errorCount > 0 && !isPdfFile) {
					batchUpload.setStatus("Failed");
				}
				batchUpload.setAssessmentMonth(assessmentMonth);
				batchUpload.setFileName(files[i].getOriginalFilename());
				batchUpload.setFilePath(path);
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setCreatedBy(userName);
				batchUpload.setModifiedBy(userName);
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setActive(true);
				batchUpload.setSuccessCount(1L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(1L);
				batchUpload.setProcessed(1);
				batchUpload.setDuplicateCount(0L);
				batchUpload.setMismatchCount(0L);
				batchUpload.setBatchUploadGroupId(batchGroup.getBatchUploadGroupID());
				batchUpload = tcsBatchUploadDAO.save(batchUpload);
				boolean isUnique = batchUpload.getStatus().equalsIgnoreCase("Uploaded");
				if (isUnique) {
					// uploading file to file share
					uploadFilesToFileShare(batchUpload, tenantId, pan, tan);
				}
			} // end of for

		} // end of else - for file present.

		return batchUpload;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param file
	 * @param userEmail
	 * @param natureOfPaymentId
	 * @return
	 * @throws Exception 
	 */
	public TCSBatchUpload saveUploadExcel(int assessmentYear, MultipartFile[] files, String tan, String type,
			String tenantId, String userEmail, String deductorPan, Integer keywordId, String token, Integer month,
			Date startDate, Date endDate) throws Exception {
		MultipartFile file = null;
		if (month == null) {
			month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		}
		String path;
		String csvPath = null;
		TCSBatchUpload batchUpload = null;
		if (files != null && files.length == 1) {
			String sparkResponse = null;
			file = files[0];
			logger.info("FILE NAME: {}", file.getOriginalFilename());
			if (file.isEmpty()) {
				throw new FileStorageException("Please select the file");
			} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")
					&& !FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("csv")) {
				throw new FileStorageException("Please select the xlsx or csv file only");
			} else {
				logger.info(file.getOriginalFilename());
				String sha256 = sha256SumService.getSHA256Hash(file);
				List<TCSBatchUpload> batch = tcsBatchUploadDAO.getSha256Records(sha256);
				path = blob.uploadExcelToBlob(file, tenantId);
				// converting xlsx to csv
				if ("gl".equalsIgnoreCase(type) || "PURCHASE_REGISTER_BATCH".equalsIgnoreCase(type)
						|| "TCS_RECEIVABLE_LEDGER".equalsIgnoreCase(type)
						|| "FORM_26AS_REQUEST".equalsIgnoreCase(type)) {
					File f = new File(file.getOriginalFilename());
					try (OutputStream os = new FileOutputStream(f)) {
						os.write(file.getBytes());
					}
					try {
						Workbook workbook = new Workbook(f.getAbsolutePath());
						ByteArrayOutputStream baout = new ByteArrayOutputStream();
						workbook.save(baout, SaveFormat.CSV);
						File glCsvFile = new File(FilenameUtils.removeExtension(f.getName()) + ".csv");

						FileUtils.writeByteArrayToFile(glCsvFile, baout.toByteArray());
						csvPath = blob.uploadExcelToBlobWithFile(glCsvFile, tenantId);
					} catch (Exception e) {
						logger.error("Error ocuured while converting xlsx to csv", e);
					}
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Batch Object : {}", batch);
				}
				boolean isDuplicate = !batch.isEmpty() && !"INVOICE_REMEDIATION_IMPORT".equalsIgnoreCase(type)
						&& !"payment_remediation_import".equalsIgnoreCase(type)
						&& !"provision_remediation_import".equalsIgnoreCase(type)
						&& !"CHART_OF_ACCOUNTS".equalsIgnoreCase(type) && !"TCS_KEYWORDS_EXCEL".equalsIgnoreCase(type);
				boolean isJavaProcess = "INVOICE_REMEDIATION_IMPORT".equalsIgnoreCase(type)
						|| "payment_remediation_import".equalsIgnoreCase(type)
						|| "provision_remediation_import".equalsIgnoreCase(type)
						|| "CHART_OF_ACCOUNTS".equalsIgnoreCase(type) || "TCS_KEYWORDS_EXCEL".equalsIgnoreCase(type);

				batchUpload = saveBatchUpload(assessmentYear, tan, type, userEmail, file, path, month, sha256, batch,
						isDuplicate, isJavaProcess, csvPath);

				boolean isUnique = batchUpload.getStatus().equalsIgnoreCase("Uploaded");

				logger.info("Notebook type : {}", type);
				TCSNoteBookParam noteBookParam = createNoteBook(assessmentYear, tan, tenantId, userEmail, month,
						batchUpload, startDate, endDate);

				if (isUnique
						&& ("invoice_excel".equalsIgnoreCase(type) || "ADVANCES".equalsIgnoreCase(type)
								|| "gl".equalsIgnoreCase(type) || "tcs_invoice_sap".equalsIgnoreCase(type)
								|| "payment_sap".equalsIgnoreCase(type) || "gl_sap".equalsIgnoreCase(type))
						|| "collections".equalsIgnoreCase(type) || "GST_59_DF".equalsIgnoreCase(type)
						|| "GST_239_DF".equalsIgnoreCase(type) || "GST_Standard".equalsIgnoreCase(type)) {
					// converting the type
					type = convertType(type);
					SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get("tcs_" + type.toLowerCase());
					logger.info("Notebook url : {}", notebook.getUrl());
					sparkResponse = triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam, month,
							assessmentYear, tenantId, tan, userEmail);
					SparkResponseDTO sparkResponseDTO = new ObjectMapper().readValue(sparkResponse,
							SparkResponseDTO.class);
					batchUpload = updateBatchUpload(Integer.valueOf(sparkResponseDTO.getRun_id()), batchUpload);
				} else if ("INVOICE_REMEDIATION_IMPORT".equalsIgnoreCase(type)) {
					logger.info("invoice type for mismatch : {}", type);
					batchUpload = tcsInvoiceLineItemService.asyncUpdateRemediationReport(tan, batchUpload, path,
							tenantId, deductorPan, token, assessmentYear, userEmail,month);
				} else if ("payment_remediation_import".equalsIgnoreCase(type)) {
					batchUpload = paymentService.asyncUpdateRemediationReport(tan, batchUpload, path, tenantId,
							deductorPan, assessmentYear, userEmail,month);
				} else if ("TCS_KEYWORDS_EXCEL".equalsIgnoreCase(type)) {
					batchUpload = tcsKeywordMasterService.createKeywordMaster(file, keywordId, tenantId, userEmail,
							deductorPan, batchUpload);
				} else if ("CHART_OF_ACCOUNTS".equalsIgnoreCase(type)) {
					ResponseEntity<ApiStatus<TCSBatchUpload>> response = onboardingClient.tcsCreatedChartOfAccounts(
							file, tan, assessmentYear, type, tenantId, deductorPan, userEmail, batchUpload.getId());
					batchUpload = response.getBody().getData();
				} else if ("PURCHASE_REGISTER_BATCH".equalsIgnoreCase(type)
						|| "TCS_RECEIVABLE_LEDGER".equalsIgnoreCase(type)) {
					batchUpload = tcsPurchaseRegisterService.saveGstPrAndTcsRlData(tan, deductorPan,
							batchUpload.getId(), assessmentYear, userEmail, tenantId, type);
				} else if ("FORM_26AS_REQUEST".equalsIgnoreCase(type)) {
					batchUpload = tcsPurchaseRegisterService.process26As(tan, deductorPan, batchUpload.getId(),
							assessmentYear, userEmail, tenantId, type,token);
				}
					 
				logger.info("Came out of method of batch upload : {}", type);
			}
		} else if (files != null && files.length > 1) {
			boolean isNotebook0Exists = false;
			List<NoteBookBatch> batchEntries = new ArrayList<>();
			for (int i = 0; i < files.length; i++) {
				file = files[i];
				logger.info("FILE NAME: {}", file.getOriginalFilename());
				logger.info(file.getOriginalFilename());
				String sha256 = sha256SumService.getSHA256Hash(file);
				List<TCSBatchUpload> batch = tcsBatchUploadDAO.getSha256Records(sha256);
				path = blob.uploadExcelToBlob(file, tenantId);
				if (logger.isDebugEnabled()) {
					logger.debug("Batch Object : {}", batch);
				}
				boolean isDuplicate = !batch.isEmpty() && !"INVOICE_REMEDIATION_IMPORT".equalsIgnoreCase(type)
						&& !"payment_remediation_import".equalsIgnoreCase(type)
						&& !"provision_remediation_import".equalsIgnoreCase(type)
						&& !"CHART_OF_ACCOUNTS".equalsIgnoreCase(type);
				boolean isJavaProcess = "INVOICE_REMEDIATION_IMPORT".equalsIgnoreCase(type)
						|| "payment_remediation_import".equalsIgnoreCase(type)
						|| "provision_remediation_import".equalsIgnoreCase(type)
						|| "CHART_OF_ACCOUNTS".equalsIgnoreCase(type);
				batchUpload = saveBatchUpload(assessmentYear, tan, type, userEmail, file, path, month, sha256, batch,
						isDuplicate, isJavaProcess, null);
				boolean isUnique = batchUpload.getStatus().equalsIgnoreCase("Uploaded");
				logger.info("Notebook type : {}", type);
				if (isUnique && ("invoice_excel".equalsIgnoreCase(type) || "provisions".equalsIgnoreCase(type)
						|| "ADVANCES".equalsIgnoreCase(type) || "gl".equalsIgnoreCase(type)
						|| "GST_59_DF".equalsIgnoreCase(type) || "GST_239_DF".equalsIgnoreCase(type)
						|| "GST_Standard".equalsIgnoreCase(type))) {
					NoteBookBatch noteBookBatch = new NoteBookBatch();
					if (batchUpload != null) {
						noteBookBatch.setId(batchUpload.getId());
						noteBookBatch.setStatus(batchUpload.getStatus());
						noteBookBatch.setType(batchUpload.getUploadType());
						if (StringUtils.isNotBlank(batchUpload.getSha256sum())) {
							noteBookBatch.setSha256(batchUpload.getSha256sum());
						} else {
							noteBookBatch.setSha256("");
						}
						if (StringUtils.isNotBlank(batchUpload.getFilePath())) {
							noteBookBatch.setFileName(batchUpload.getFilePath()
									.substring(batchUpload.getFilePath().lastIndexOf("/") + 1));
						} else {
							noteBookBatch.setFileName("");
						}
					}
					batchEntries.add(noteBookBatch);
				}
			}
			TCSNoteBookParam noteBookParam = createNoteBook(assessmentYear, tan, tenantId, userEmail, month,
					batchUpload, startDate, endDate);
			ObjectMapper objectMapper = new ObjectMapper();
			String batchEntriesJson = objectMapper.writeValueAsString(batchEntries);
			noteBookParam.setBatchEntries(StringUtils.isBlank(batchEntriesJson) ? StringUtils.EMPTY : batchEntriesJson);
			// Note book 0 trigger
			Map<String, String> requestParams = new HashMap<>();
			requestParams.put("pan", deductorPan);
			ResponseEntity<ApiStatus<Optional<DeductorOnboardingInformationResponseDTO>>> response = onboardingClient
					.getDeductorOnboardingInfo(requestParams, tenantId);
			if (response != null && response.getBody() != null) {
				Optional<DeductorOnboardingInformationResponseDTO> optional = response.getBody().getData();
				if (optional.isPresent()) {
					DeductorOnboardingInformationResponseDTO deductorOnboardingInformation = optional.get();
					if (deductorOnboardingInformation.getCustomJobs() != null
							&& !deductorOnboardingInformation.getCustomJobs().isEmpty()) {
						if (StringUtils
								.isNotBlank(deductorOnboardingInformation.getCustomJobs().get(type.toLowerCase()))) {
							try {
								isNotebook0Exists = true;
								int jobId = Integer
										.valueOf(deductorOnboardingInformation.getCustomJobs().get(type.toLowerCase()))
										.intValue();
								SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks()
										.get(type.toLowerCase());
								logger.info("Notebook 0 url : {} with jobId : {}", notebook.getUrl(), jobId);
								triggerSparkNotebook(notebook.getUrl(), jobId, noteBookParam, month, assessmentYear,
										tenantId, tan, userEmail);
								List<TCSActivityTracker> listTracker = tcsActivityTrackerDAO
										.getActivityTrackerByTanYearTypeAndMonth(tan, assessmentYear, "TDS Calculation",
												month);
								if (!listTracker.isEmpty()) {
									listTracker.get(0).setStatus(ActivityTrackerStatus.VALIDATED.name());
									listTracker.get(0).setModifiedBy(userEmail);
									listTracker.get(0).setModifiedDate(new Date());
									tcsActivityTrackerDAO.update(listTracker.get(0));
								}
							} catch (NumberFormatException nfe) {
								// Do nothing.
								logger.error("Exception occured while processing Notebook 0 triggering", nfe);
							}
						}
					}
				}
			}
			if (!isNotebook0Exists) {
				SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get(type.toLowerCase());
				logger.info("Notebook url : {}", notebook.getUrl());
				triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam, month, assessmentYear,
						tenantId, tan, userEmail);
			}
		}
		logger.info("Returing batch upload data: {}", batchUpload);
		return batchUpload;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param tan
	 * @param type
	 * @param userEmail
	 * @param file
	 * @param path
	 * @param month
	 * @param sha256
	 * @param batch
	 * @param isDuplicate
	 * @param isJavaProcess
	 * @return
	 */
	private TCSBatchUpload saveBatchUpload(int assessmentYear, String tan, String type, String userEmail,
			MultipartFile file, String path, int month, String sha256, List<TCSBatchUpload> batch, boolean isDuplicate,
			boolean isJavaProcess, String otherFileUrl) {
		TCSBatchUpload batchUpload;
		batchUpload = new TCSBatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setCollectorMasterTan(tan);
		batchUpload.setUploadType(type);
		if (isDuplicate) {
			logger.info("Duplicate Record inserting : {}", file.getOriginalFilename());
			batchUpload.setStatus("Duplicate");
			batchUpload.setSha256sum(sha256);
			batchUpload.setReferenceId(batch.get(0).getId());
		} else {
			logger.info("Unique record  inserting  : {}", file.getOriginalFilename());
			batchUpload.setStatus("Uploaded");
			batchUpload.setSha256sum(sha256);
		}
		batchUpload.setAssessmentMonth(month);
		batchUpload.setFileName(file.getOriginalFilename());
		batchUpload.setAssessmentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
		batchUpload.setFilePath(path);
		batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
		batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
		batchUpload.setCreatedBy(userEmail);
		batchUpload.setActive(true);
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessed(0);
		batchUpload.setDuplicateCount(0L);
		batchUpload.setMismatchCount(0L);
		if (isJavaProcess) {
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
		}
		if (StringUtils.isNotEmpty(otherFileUrl)) {
			batchUpload.setOtherFileUrl(otherFileUrl);
		}
		batchUpload = tcsBatchUploadDAO.save(batchUpload);
		return batchUpload;
	}

	private TCSBatchUpload updateBatchUpload(Integer runId, TCSBatchUpload batchUpload) {
		batchUpload.setRunId(runId);
		batchUpload = tcsBatchUploadDAO.update(batchUpload);
		return batchUpload;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param tan
	 * @param tenantId
	 * @param userEmail
	 * @param month
	 * @param batchUpload
	 * @return
	 */
	public TCSNoteBookParam createNoteBook(int assessmentYear, String tan, String tenantId, String userEmail, int month,
			TCSBatchUpload batchUpload, Date startDate, Date endDate) {
		TCSNoteBookParam noteBookParam = new TCSNoteBookParam();
		noteBookParam.setAssessmentMonth(month);
		noteBookParam.setAssessmentYear(assessmentYear);
		if (batchUpload != null) {
			noteBookParam.setId(batchUpload.getId());
			noteBookParam.setStatus(batchUpload.getStatus());
			noteBookParam.setType(batchUpload.getUploadType());
			if (StringUtils.isNotBlank(batchUpload.getSha256sum())) {
				noteBookParam.setSha256(batchUpload.getSha256sum());
			} else {
				noteBookParam.setSha256("");
			}
			if (StringUtils.isNotBlank(batchUpload.getFilePath())) {
				// extracting file name from file url
				noteBookParam.setFileName(
						batchUpload.getFilePath().substring(batchUpload.getFilePath().lastIndexOf("/") + 1));
			} else {
				noteBookParam.setFileName("");
			}
		}
		noteBookParam.setTenantId(tenantId);
		noteBookParam.setTan(tan);
		noteBookParam.setApplicationURL("");
		// These two are added for Drools and also UserObject.
		noteBookParam.setUserEmail(userEmail);
		return noteBookParam;
	}

	/**
	 * 
	 * @param api
	 * @param jobId
	 * @param noteBookParam
	 * @param month
	 * @param assessmentYear
	 * @param tenantId
	 * @param tan
	 * @param userEmail
	 * @throws JsonProcessingException
	 */
	public String triggerSparkNotebook(String api, int jobId, TCSNoteBookParam noteBookParam, int month,
			int assessmentYear, String tenantId, String tan, String userEmail) throws JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		// Made the Databricks Key Dynamic
		headers.add("Authorization", "Bearer " + this.dataBricksKey);
		headers.add("Content-Type", "application/json");
		RestTemplate restTemplate = new RestTemplate();
		TcsJob job = new TcsJob();

		job.setJob_id(jobId);
		job.setNotebook_params(noteBookParam);

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.writeValueAsString(job);
		if (logger.isInfoEnabled()) {
			logger.info("Note Book Object : {}", objMapper.writeValueAsString(job));
		}
		logger.info(job.getJob_id().toString());
		logger.info("token : {}", this.dataBricksKey);

		String dataBricks = api;
		HttpEntity<TcsJob> entity = new HttpEntity<>(job, headers);
		String response = restTemplate.postForObject(dataBricks, entity, String.class);

		logger.info("Response : {}", response);
		return response;
	}

	public SparkState getSparkJobRunStatus(Integer runId) throws JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		// Made the Databricks Key Dynamic
		headers.add("Authorization", "Bearer " + this.dataBricksKey);

		headers.add("Content-Type", "application/json");
		RestTemplate restTemplate = new RestTemplate();

		HttpEntity entity = new HttpEntity(headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(runStatusUrl + runId, HttpMethod.GET, entity,
				String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		RunStatus runStatus = objectMapper.readValue(responseEntity.getBody(), RunStatus.class);

		return runStatus.getMetadata().getState();
	}

	/**
	 * 
	 * @param api
	 * @param jobId
	 * @param noteBookParam
	 * @param tenantId
	 * @throws JsonProcessingException
	 */
	public void triggerSparkNotebook(String api, int jobId, TCSNoteBookParam noteBookParam, String tenantId)
			throws JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		// Made the Databricks Key Dynamic
		headers.add("Authorization", "Bearer " + this.dataBricksKey);
		headers.add("Content-Type", "application/json");
		RestTemplate restTemplate = new RestTemplate();
		TcsJob job = new TcsJob();

		job.setJob_id(jobId);
		job.setNotebook_params(noteBookParam);

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.writeValueAsString(job);
		if (logger.isInfoEnabled()) {
			logger.info("Note Book Object : {}", objMapper.writeValueAsString(job));
		}
		logger.info(job.getJob_id().toString());
		logger.info("token : {}", this.dataBricksKey);

		String dataBricks = api;
		HttpEntity<TcsJob> entity = new HttpEntity<>(job, headers);
		String response = restTemplate.postForObject(dataBricks, entity, String.class);

		logger.info("Response : {}", response);
	}

	/**
	 * To get the List of Group Records
	 * 
	 * @param tan
	 * @return
	 */
	public List<BatchUploadGroupDTO> getGroupsBasedOnTan(String tan, Integer year) {
		List<BatchUploadGroup> getListOfGroupsBasedOnTan = batchUploadGroupDAO.getListOfGroupRecordsByTan(year, tan);
		List<BatchUploadGroupDTO> listOfGroupDTO = new ArrayList<BatchUploadGroupDTO>();
		for (BatchUploadGroup batchUploadGroup : getListOfGroupsBasedOnTan) {
			BatchUploadGroupDTO batchGroupDTO = new BatchUploadGroupDTO();
			batchGroupDTO.setGroupName(batchUploadGroup.getGroupName());
			batchGroupDTO.setCreatedDate(batchUploadGroup.getCreatedDate());
			batchGroupDTO.setErrorRecords(batchUploadGroup.getGroupTotalErrorCount());
			batchGroupDTO.setId(batchUploadGroup.getBatchUploadGroupID());
			batchGroupDTO.setProcessedRecords(batchUploadGroup.getGroupTotalProcessedCount());
			batchGroupDTO.setTotalRecords(batchUploadGroup.getGroupTotalRecordCount());
			batchGroupDTO.setUnderValidationRecords(batchUploadGroup.getGroupUnderValidationCount());
			batchGroupDTO.setPostingDateOfTheDocument(batchUploadGroup.getPostingDateOfTheDocument());
			listOfGroupDTO.add(batchGroupDTO);
		}
		return listOfGroupDTO;
	}

	/**
	 * GET List of Batch Records based on groupId
	 * 
	 * @param tan
	 * @param groupId
	 * @return
	 */
	public List<BatchUploadGroupCustomDTO> getBatchDataByGroupId(String tan, Integer groupId) {
		List<TCSBatchUpload> getBatchRecordsBasedOnTanGroupID = tcsBatchUploadDAO.getBatchListBasedOnTanAndGroupId(tan,
				groupId);
		List<BatchUploadGroupCustomDTO> listOfBatchRecords = new ArrayList<BatchUploadGroupCustomDTO>();
		for (TCSBatchUpload batchUpload : getBatchRecordsBasedOnTanGroupID) {
			BatchUploadGroupCustomDTO batchDTO = new BatchUploadGroupCustomDTO();
			batchDTO.setBatchId(batchUpload.getId());
			batchDTO.setFileName(batchUpload.getFileName());
			batchDTO.setStatus(batchUpload.getStatus());
			batchDTO.setFilePath(batchUpload.getFilePath());
			batchDTO.setAssessmentYear(batchUpload.getAssessmentYear());
			listOfBatchRecords.add(batchDTO);
		}
		return listOfBatchRecords;
	}

	/**
	 * 
	 * @param batchUpload
	 * @param tenantId
	 * @param pan
	 * @param tan
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 */
	public TCSBatchUpload uploadFilesToFileShare(TCSBatchUpload batchUpload, String tenantId, String pan, String tan)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		FileShareConfig fileShareConfig = tenantProperties.getConfiguration(tenantId).getFileShareConfig();
		String storageConnectionString = "DefaultEndpointsProtocol=" + fileShareConfig.getProtocol() + ";"
				+ "AccountName=" + fileShareConfig.getAccountName() + ";" + "AccountKey="
				+ fileShareConfig.getAccountKey() + ";";

		// Retrieve storage account from connection-string.
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

		// Create the file client.
		CloudFileClient fileClient = storageAccount.createCloudFileClient();

		// Get a reference to the file share
		CloudFileShare share = fileClient.getShareReference(fileShareConfig.getShare());

		// Get a reference to the root directory for the share.
		CloudFileDirectory rootDir = share.getRootDirectoryReference();

		// Get a reference to the sampledir directory
		CloudFileDirectory sampleDir = rootDir.getDirectoryReference(fileShareConfig.getDirectoryPath());

		File file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());

		CloudFile cloudFile = sampleDir
				.getFileReference(batchUpload.getId() + "_" + tenantId + "_" + batchUpload.getFileName());
		cloudFile.uploadFromFile(file.getAbsolutePath());

		return batchUpload;
	}

	/**
	 * 
	 * @param batchId
	 * @param tenantId
	 * @param fileName
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 */
	public TCSBatchUpload downloadFilesFromFileShare(Integer batchId, String tenantId, String fileName)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		TCSBatchUpload batchUpload = new TCSBatchUpload();
		FileShareConfig fileShareConfig = tenantProperties.getConfiguration(tenantId).getFileShareConfig();
		String storageConnectionString = "DefaultEndpointsProtocol=" + fileShareConfig.getProtocol() + ";"
				+ "AccountName=" + fileShareConfig.getAccountName() + ";" + "AccountKey="
				+ fileShareConfig.getAccountKey() + ";";

		// Retrieve storage account from connection-string.
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

		// Create the file client.
		CloudFileClient fileClient = storageAccount.createCloudFileClient();

		// Get a reference to the file share
		CloudFileShare share = fileClient.getShareReference(fileShareConfig.getShare());

		// Get a reference to the root directory for the share.
		CloudFileDirectory rootDir = share.getRootDirectoryReference();

		// Get a reference to the directory that contains the file
		CloudFileDirectory sampleDir = rootDir.getDirectoryReference("AutomationAnywhere\\outputfiles");

		// Get a reference to the file you want to download
		CloudFile file1 = sampleDir.getFileReference(fileName);
		file1.downloadToFile(fileName);
		File file = new File(file1.getName());
		URL url = new URL(file1.getUri().toURL(), "");
		FileUtils.copyURLToFile(url, file);
		blob.uploadExcelToBlobWithFile(file, tenantId);

		return batchUpload;
	}

	/**
	 * 
	 * @param type
	 * @param tenantId
	 * @param batchId
	 * @param fileName
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 */
	public TCSBatchUpload uploadInvoicePdfBlobUrlToBatch(String type, String tenantId, Integer batchId, String fileName)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		FileShareConfig fileShareConfig = tenantProperties.getConfiguration(tenantId).getFileShareConfig();

		TenantConfiguration.BlobStorage blobStorage = tenantProperties.getConfiguration(tenantId).getBlobStorage();
		String blobURL = blobStorage.getProtocol() + "://" + blobStorage.getAccountName() + ".blob.core.windows.net/"
				+ blobStorage.getContainer() + "/" + fileShareConfig.getOutputPath() + "/" + fileName;

		List<TCSBatchUpload> batchOptional = tcsBatchUploadDAO.getBatchUploadByTypeAndId(type, batchId);

		logger.info("Updating batch upload received with csv url--{}", blobURL);
		TCSBatchUpload batchUpload = new TCSBatchUpload();
		if (!batchOptional.isEmpty()) {
			logger.warn("Requested batch upload entry is found in system for type {} and batch id {}", type, batchId);
			batchUpload = batchOptional.get(0);
			// TODO need to change tcs batch upload group table.
			BatchUploadGroup batchGroup = new BatchUploadGroup();
			batchGroup.setAssessmentYear(batchUpload.getAssessmentYear());
			batchGroup.setTan(batchUpload.getCollectorMasterTan());
			batchGroup.setBatchUploadGroupID(batchUpload.getBatchUploadGroupId());
			List<BatchUploadGroup> listGroupBatch = batchUploadGroupDAO.findById(batchUpload.getAssessmentYear(),
					batchUpload.getCollectorMasterTan(), batchUpload.getBatchUploadGroupId());
			/*
			 * if (batchUploadGroupResponse.isPresent()) { BatchUploadGroup batchUploadGroup
			 * = batchUploadGroupResponse.get();
			 * batchUploadGroup.setGroupTotalProcessedCount(batchUploadGroup.
			 * getGroupTotalProcessedCount() + 1);
			 * batchUploadGroupRepository.save(batchUploadGroup); }
			 */
			if (!listGroupBatch.isEmpty()) {
				BatchUploadGroup batchUploadGroup = listGroupBatch.get(0);
				batchUploadGroup.setGroupTotalProcessedCount(batchUploadGroup.getGroupTotalProcessedCount() + 1);
				batchUploadGroupDAO.update(batchUploadGroup);
			}

			batchUpload.setFileName(batchOptional.get(0).getFileName());
			batchUpload.setSuccessFileUrl(blobURL);
			batchUpload.setStatus("Processed");
			batchUpload.setSourceFilePath(fileShareConfig.getOutputPath() + "/" + fileName);
			batchUpload.setSha256sum(batchOptional.get(0).getSha256sum());
			batchUpload.setReferenceId(batchOptional.get(0).getId());
			batchUpload.setAssessmentMonth(batchOptional.get(0).getAssessmentMonth());
			batchUpload.setAssessmentMonth(batchOptional.get(0).getAssessmentMonth());
			batchUpload.setFilePath(batchOptional.get(0).getFilePath());
			batchUpload.setCreatedDate(batchOptional.get(0).getCreatedDate());
			batchUpload.setCreatedBy(batchOptional.get(0).getCreatedBy());
			batchUpload.setActive(true);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessed(0);
			batchUpload.setDuplicateCount(0L);
			batchUpload.setMismatchCount(0L);
			batchUpload.setBatchUploadGroupId(batchOptional.get(0).getBatchUploadGroupId());
			tcsBatchUploadDAO.save(batchUpload);
		} else {
			logger.warn("Requested batch upload entry is not found in system for type {} and batch id {}", type,
					batchId);
		}
		logger.info("Batch upload record updated sucessfully");

		return batchUpload;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param file
	 * @param tan
	 * @param type
	 * @param tenantId
	 * @param userEmail
	 * @param blobUrl
	 * @param deductorPan
	 * @param filePath
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	@Transactional
	public TCSBatchUpload uploadBlobUrlToBatch(int assessmentYear, MultipartFile file, String tan, String type,
			String tenantId, String userEmail, String blobUrl, String deductorPan, String filePath)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		if (logger.isDebugEnabled()) {
			logger.debug("REST request for file upload Excel : {}, {}", file, tan);
		}
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		TCSBatchUpload batchUpload = null;
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else {

			logger.info(file.getOriginalFilename());
			String sha256 = sha256SumService.getSHA256Hash(file);

			batchUpload = new TCSBatchUpload();
			logger.info("Unique record  inserting  : {}", file.getOriginalFilename());
			logger.info("Unique file : {}", file.getOriginalFilename());
			List<TCSBatchUpload> batcList = tcsBatchUploadDAO.getSha256Records(sha256SumService.getSHA256Hash(file));
			batchUpload.setAssessmentYear(assessmentYear);
			batchUpload.setCollectorMasterTan(tan);
			batchUpload.setUploadType(type);
			if (!batcList.isEmpty()) {
				logger.info("Duplicate Record inserting : {}", file.getOriginalFilename());
				batchUpload.setStatus("Duplicate");
				batchUpload.setSha256sum(sha256);
				batchUpload.setReferenceId(batcList.get(0).getId());
			} else {
				logger.info("Unique record  inserting  : {}", file.getOriginalFilename());
				batchUpload.setStatus("Uploaded");
				batchUpload.setSha256sum(sha256);
			}
			batchUpload.setAssessmentMonth(month);
			// for SAP file name should include file path. Don't change the following two
			// lines.
			batchUpload.setFileName(filePath);
			batchUpload.setSourceFilePath(filePath);
			batchUpload.setFilePath(blobUrl);
			batchUpload.setAssessmentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
			batchUpload.setCreatedDate(new Date());
			batchUpload.setCreatedBy(userEmail);
			batchUpload.setActive(true);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessed(0);
			batchUpload.setDuplicateCount(0L);
			batchUpload.setMismatchCount(0L);
			batchUpload.setProcessStartTime(new Date());
			batchUpload = tcsBatchUploadDAO.save(batchUpload);
			boolean isUnique = batchUpload.getStatus().equalsIgnoreCase("Uploaded");
			logger.info("SAP Notebook type : {}", type);

			TCSNoteBookParam noteBookParam = createNoteBook(assessmentYear, tan, tenantId, userEmail, month,
					batchUpload, null, null);

			if (isUnique && ("INVOICE_SAP".equalsIgnoreCase(type))) {
				SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get("INVOICE_EXCEL".toLowerCase());
				logger.info("Notebook url : {}", notebook.getUrl());
				triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam, month, assessmentYear,
						tenantId, tan, userEmail);
			} else if (isUnique && "PROVISION_SAP".equalsIgnoreCase(type)) {
				SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get("PROVISIONS".toLowerCase());
				logger.info("Notebook url : {}", notebook.getUrl());
				triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam, month, assessmentYear,
						tenantId, tan, userEmail);
			} else if (isUnique && "PAYMENT_SAP".equalsIgnoreCase(type)) {
				SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get("PAYMENT".toLowerCase());
				logger.info("Notebook url : {}", notebook.getUrl());
				triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam, month, assessmentYear,
						tenantId, tan, userEmail);
			} else if (isUnique && "GL_SAP".equalsIgnoreCase(type)) {
				SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get("GL".toLowerCase());
				logger.info("Notebook url : {}", notebook.getUrl());
				triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam, month, assessmentYear,
						tenantId, tan, userEmail);
			} else if ("TCS_INVOICE_REMEDIATION_IMPORT".equalsIgnoreCase(type)) {
				batchUpload = tcsInvoiceLineItemService.asyncUpdateRemediationReport(tan, batchUpload, blobUrl,
						tenantId, deductorPan, null, assessmentYear, userEmail,month);
			} else if ("TCS_PAYMENT_REMEDIATION_IMPORT".equalsIgnoreCase(type)) {
				batchUpload = paymentService.asyncUpdateRemediationReport(tan, batchUpload, blobUrl, tenantId,
						deductorPan, assessmentYear, userEmail,month);
			} else if (isUnique && "COLLECTEE_SAP".equalsIgnoreCase(type)) {
				ResponseEntity<ApiStatus<TCSBatchUpload>> batchData = onboardingClient.tcsReadImportedCsvData(file, tan,
						deductorPan, userEmail, assessmentYear, Calendar.getInstance().get(Calendar.MONTH) + 1,
						tenantId, batchUpload.getId());
				batchUpload = batchData.getBody().getData();
			}
		}
		return batchUpload;

	}

	/**
	 * 
	 * @param type
	 * @param year
	 * @param tan
	 * @param tenantId
	 * @return
	 */
	public List<ReceiptBatchUploadDTO> getReceiptDataBasedOnBatchId(String type, int year, List<String> tan,
			String tenantId) {
		List<ReceiptBatchUploadDTO> getReceiptCompleteData = new ArrayList<>();
		ReceiptDataDTO receipt = null;
		// Get Old 2000 date
		Calendar c1 = Calendar.getInstance();
		c1.set(Calendar.MONTH, 01);
		c1.set(Calendar.DATE, 01);
		c1.set(Calendar.YEAR, 2000);
		Date dateOne = c1.getTime();
		logger.info("Tenant Tan ---: {}", tan);
		List<TCSBatchUpload> batchList = tcsBatchUploadDAO.getBatchList(year, tan, type);
		if (batchList != null && !batchList.isEmpty()) {
			Collections.sort(batchList, new Comparator<TCSBatchUpload>() {
				@Override
				public int compare(TCSBatchUpload id1, TCSBatchUpload id2) {
					if (id1.getCreatedDate() == null) {
						id1.setCreatedDate(dateOne);
					}
					if (id2.getCreatedDate() == null) {
						id2.setCreatedDate(dateOne);
					}
					return id2.getCreatedDate().compareTo(id1.getCreatedDate());
				}
			});
		}
		if (!batchList.isEmpty()) {
			for (TCSBatchUpload batch : batchList) {
				// Call Feign for getting ReceiptData
				logger.info("INTEGER : {}", batch.getId());
				ResponseEntity<ApiStatus<ReceiptDataDTO>> receiptData = challanClient
						.getReceiptDataBasedOnBatchId(tenantId, batch.getId());
				receipt = receiptData.getBody().getData();
				ReceiptBatchUploadDTO receiptbatchData = new ReceiptBatchUploadDTO();
				TCSBatchUpload batchUpload = batch;
				receiptbatchData.setId(batchUpload.getId());
				receiptbatchData.setDateOfUpload(batchUpload.getCreatedDate());
				receiptbatchData.setFileName(batchUpload.getFileName());
				receiptbatchData.setFileStatus(batchUpload.getStatus());
				receiptbatchData.setUploadedFileDownloadUrl(batchUpload.getFilePath());
				receiptbatchData.setUploadBy(batchUpload.getCreatedBy());
				receiptbatchData.setProcessEndTime(batchUpload.getProcessEndTime());
				if (batchUpload.getRowsCount() != null) {
					receiptbatchData.setTotalRecords(batchUpload.getRowsCount());
				} else {
					receiptbatchData.setTotalRecords(0L);
				}
				if (batchUpload.getDuplicateCount() != null) {
					receiptbatchData.setDuplicateRecords(batchUpload.getDuplicateCount());
				} else {
					receiptbatchData.setDuplicateRecords(0L);
				}

				if (batchUpload.getMismatchCount() != null) {
					receiptbatchData.setMismatchCount(batchUpload.getMismatchCount());
				} else {
					receiptbatchData.setMismatchCount(0L);
				}
				if (batchUpload.getFailedCount() != null) {
					receiptbatchData.setErrorRecords(batchUpload.getFailedCount());
				} else {
					receiptbatchData.setProcessedRecords(0L);
				}
				if (batchUpload.getProcessed() != null) {
					receiptbatchData.setProcessedRecords(batchUpload.getProcessed());
				} else {
					receiptbatchData.setProcessedRecords(0L);
				}
				receiptbatchData.setErrorFileDownloadUrl(batchUpload.getErrorFilePath());
				receiptbatchData.setYear(batchUpload.getAssessmentYear());
				batchUpload.setAssessmentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
				receiptbatchData.setMonth(batchUpload.getAssessmentMonth());
				receiptbatchData.setTan(batchUpload.getCollectorMasterTan());
				if (receipt != null) {
					if (receipt.getChallanMonth() != null) {
						receiptbatchData.setReceiptMonth(receipt.getChallanMonth());
					}
					if (receipt.getReceiptId() != null) {
						receiptbatchData.setReceiptId(receipt.getReceiptId());
					}
				}
				getReceiptCompleteData.add(receiptbatchData);
			}
		}
		return getReceiptCompleteData;
	}

	/**
	 * 
	 * @param userEmail
	 * @param tan
	 * @param tenantId
	 * @param batchProcessDTO
	 * @return
	 * @throws JsonProcessingException
	 */
	public TCSBatchUpload batchProcess(String userEmail, String tan, String tenantId, BatchProcessDTO batchProcessDTO)
			throws JsonProcessingException {
		String jobType = "BATCHPROCESS";
		String fileName = "";
		int month = batchProcessDTO.getMonth();
		TCSBatchUpload batchUpload = new TCSBatchUpload();
		batchUpload.setAssessmentYear(batchProcessDTO.getYear());
		batchUpload.setCollectorMasterTan(tan);
		batchUpload.setUploadType(batchProcessDTO.getType());
		batchUpload.setStatus("Uploaded");
		batchUpload.setProcessStartTime(new Date());
		batchUpload.setAssessmentMonth(month);
		batchUpload.setCreatedDate(new Date());
		batchUpload.setCreatedBy(userEmail);
		batchUpload.setActive(true);
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessed(0);
		batchUpload.setDuplicateCount(0L);
		batchUpload.setMismatchCount(0L);
		if ("BATCHPROCESS_INTEREST".equalsIgnoreCase(batchProcessDTO.getType())) {
			jobType = "INTEREST";
			// batchUpload.setFileName("INTEREST_CALCULATION" + new Date());
			fileName = "INTEREST_CALCULATION" + new Date();
		} else if ("BATCHPROCESS_GLRECON".equalsIgnoreCase(batchProcessDTO.getType())) {
			jobType = "TCS_GL_RECONCILATION";
			if (StringUtils.isNotBlank(batchProcessDTO.getQuarter()) && batchProcessDTO.getMonth() > 0) {
				batchUpload.setFileName("GLRECONCILATION" + "-" + batchProcessDTO.getYear() + "-"
						+ batchProcessDTO.getQuarter() + "-" + batchProcessDTO.getMonth());
				fileName = "GLRECONCILATION" + "-" + batchProcessDTO.getYear() + "-" + batchProcessDTO.getQuarter()
						+ "-" + batchProcessDTO.getMonth();
			} else if (StringUtils.isNotBlank(batchProcessDTO.getQuarter())) {
				batchUpload.setFileName(
						"GLRECONCILATION" + "-" + batchProcessDTO.getYear() + "-" + batchProcessDTO.getQuarter());
				fileName = "GLRECONCILATION" + "-" + batchProcessDTO.getYear() + "-" + batchProcessDTO.getQuarter();
			} else {
				batchUpload.setFileName("GLRECONCILATION" + "-" + batchProcessDTO.getYear());
				fileName = "GLRECONCILATION" + "-" + batchProcessDTO.getYear();
			}
		} else if ("BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchProcessDTO.getType())) {
			jobType = "LIABILITY_RECONCILATION";
			batchUpload.setFileName(
					"LIABILITY_RECONCILATION" + "_" + batchProcessDTO.getYear() + "_" + batchProcessDTO.getMonth());
			fileName = "LIABILITY_RECONCILATION" + "_" + batchProcessDTO.getYear() + "_" + batchProcessDTO.getMonth();

		}
		tcsBatchUploadDAO.save(batchUpload);
		if ("BATCHPROCESS_GLRECON".equalsIgnoreCase(batchProcessDTO.getType())) {
			batchUpload.setUploadType(batchProcessDTO.getUploadType());
		}
		SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get(jobType.toLowerCase());
		logger.info("Notebook 0 url : {} with jobId : {}", notebook.getUrl(), notebook.getJobId());

		TCSNoteBookParam noteBookParam = createNoteBook(batchProcessDTO.getYear(), tan, tenantId, userEmail, month,
				batchUpload, null, null);
		noteBookParam.setFileName(fileName);

		triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam, month, batchProcessDTO.getYear(),
				tenantId, tan, userEmail);
		return batchUpload;
	}

	/**
	 * 
	 * @param collectorTan
	 * @param tenantId
	 * @param collectorPan
	 * @param year
	 * @param month
	 * @return
	 * @throws IOException
	 */
	public ByteArrayInputStream exportNOIKeywords(String collectorTan, String tenantId, String collectorPan, int year,
			int month) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			XSSFSheet sheet = wb.createSheet("NOI Keywords File");
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);
			// protection enable
			DataValidation dataValidation = null;
			DataValidationConstraint constraint = null;
			DataValidationHelper validationHelper = null;
			// creating the header row and applying style to the header
			XSSFRow row1 = sheet.createRow(0);
			sheet.setDefaultColumnWidth(25);
			XSSFCellStyle style0 = wb.createCellStyle();

			style0.setBorderTop(BorderStyle.MEDIUM);
			style0.setBorderBottom(BorderStyle.MEDIUM);
			style0.setBorderRight(BorderStyle.MEDIUM);
			style0.setAlignment(HorizontalAlignment.CENTER);
			style0.setVerticalAlignment(VerticalAlignment.CENTER);

			Font font = wb.createFont();
			font.setBold(true);
			font.setFontName("Arial");
			style0.setFont(font);
			style0.setLocked(false);

			// Black colour
			style0.setFillForegroundColor(new XSSFColor(new java.awt.Color(46, 134, 193), defaultIndexedColorMap));
			style0.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			// creating cells for the header
			// row1.createCell(0).setCellValue("NOI ID");
			// row1.getCell(0).setCellStyle(style0);
			row1.createCell(0).setCellValue("NATURE OF INCOME");
			row1.getCell(0).setCellStyle(style0);
			row1.createCell(1).setCellValue("KEYWORD");
			row1.getCell(1).setCellStyle(style0);
			// row1.createCell(3).setCellValue("ACTION");
			// row1.getCell(3).setCellStyle(style0);

			// feign client for list noi in tcs natue of income table
			List<NatureOfPaymentMasterDTO> list = masterClient.getTcsNatureOfPayment().getBody().getData();

			/*
			 * validationHelper = new XSSFDataValidationHelper(sheet); CellRangeAddressList
			 * addressList = new CellRangeAddressList(1, list.size(), 3, 3); constraint =
			 * validationHelper.createExplicitListConstraint(new String[] { "Append",
			 * "Replace" }); dataValidation = validationHelper.createValidation(constraint,
			 * addressList); dataValidation.setSuppressDropDownArrow(true);
			 * dataValidation.setShowErrorBox(true); dataValidation.setShowPromptBox(true);
			 * sheet.addValidationData(dataValidation);
			 */

			XSSFSheet sheet1 = wb.createSheet("Nature Of INCOME");
			sheet1.lockAutoFilter(false);
			sheet1.lockSort(false);
			// protection enable
			sheet1.protectSheet("password");
			XSSFRow row01 = sheet1.createRow(0);
			sheet1.setDefaultColumnWidth(25);
			row01.createCell(0).setCellValue("ID");
			row01.getCell(0).setCellStyle(style0);
			row01.createCell(1).setCellValue("NATURE OF INCOME");
			row01.getCell(1).setCellStyle(style0);

			CellStyle unlockedCellStyle = wb.createCellStyle();
			unlockedCellStyle.setLocked(false); // true or false based on the cell.

			int rowNumber = 1;
			for (NatureOfPaymentMasterDTO nop : list) {
				XSSFRow row2 = sheet1.createRow(rowNumber);
				row2.createCell(0).setCellValue(nop.getId());
				row2.getCell(0).setCellStyle(unlockedCellStyle);

				String value = nop.getNature() + "-" + nop.getSection();
				row2.createCell(1).setCellValue(value);
				row2.getCell(1).setCellStyle(unlockedCellStyle);

				// row2.createCell(2).setCellValue("");
				// row2.getCell(2).setCellStyle(unlockedCellStyle);

				// row2.createCell(3).setCellValue("");
				// row2.getCell(3).setCellStyle(unlockedCellStyle);
				rowNumber++;

			}
			// hiding the column from the sheet
			sheet1.setColumnHidden(0, true);
			sheet1.autoSizeColumn(1);
			wb.write(out);
		}
		return new ByteArrayInputStream(out.toByteArray());

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param file
	 * @param tan
	 * @param tenantId
	 * @param userEmail
	 * @param token
	 * @param fileType
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws InvalidFormatException
	 */
	public TCSBatchUpload saveUploadExcelForReconciliation(int assessmentYear, int assessmentMonth, MultipartFile file,
			String tan, String tenantId, String userEmail, String token, String fileType)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, InvalidFormatException {

		if (logger.isDebugEnabled()) {
			logger.debug("REST request for file : {} upload Reconcilitation Excel : {}, {}", fileType, file, tan);
		}

		String path;

		logger.info("FILE NAME: {}", file.getOriginalFilename());

		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Please select the xlsx file only");
		} else {
			logger.info(file.getOriginalFilename());
			String sha256Key = sha256SumService.getSHA256Hash(file);

			List<TCSBatchUpload> batch = tcsBatchUploadDAO
					.getBatchList(assessmentYear, Arrays.asList(tan),
							ReconciliationTriggerRequest.KEY.UPLOAD_KEY.getFileKey())
					.stream().filter(_b -> _b.getSourceIdentifier().equalsIgnoreCase(fileType))
					.collect(Collectors.toList());

			/**
			 * Uploading files to Blob Note: Appended Year to make Files Unique for a
			 * particular year
			 */
			path = blob.uploadExcelToBlob(file, UUID.randomUUID().toString() + "-" + file.getOriginalFilename(),
					tenantId);

			if (logger.isDebugEnabled()) {
				logger.debug("Batch Object : {}", batch);
			}

			// TODO : Need to discuss, whether we want to retain old data in table or not .
			// TODO : Updated the code to retain the old files for a year to make partial
			// upload possible.
//			if (!batch.isEmpty()) {
//				logger.info("Duplicate Record Deleting : {}", file.getOriginalFilename());
//				batch.forEach(_b -> this.TcsBatchUploadDAO.delete(_b));
//			}

			logger.info("Unique record  inserting  : {}", file.getOriginalFilename());

			// TODO : Need to change to work with multiple assessmentMonths.
			TCSBatchUpload batchUpload = new TCSBatchUpload();
			batchUpload.setAssessmentYear(assessmentYear);
			batchUpload.setCollectorMasterTan(tan);
			batchUpload.setUploadType(ReconciliationTriggerRequest.KEY.UPLOAD_KEY.getFileKey());
			batchUpload.setStatus("Uploaded");
			batchUpload.setSha256sum(sha256Key);
			batchUpload.setSourceIdentifier(fileType);
			batchUpload.setAssessmentMonth(assessmentMonth);
			batchUpload.setFileName(file.getOriginalFilename());
			batchUpload.setFilePath(path);
			batchUpload.setCreatedDate(new Date());
			batchUpload.setCreatedBy(userEmail);
			batchUpload.setActive(true);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessed(0);
			batchUpload.setDuplicateCount(0L);
			batchUpload.setMismatchCount(0L);
			batchUpload.setProcessStartTime(new Date());
			batchUpload = tcsBatchUploadDAO.save(batchUpload);
			return batchUpload;
		}
	}

	/**
	 * Method to trigger reconciliation for provided AssessmentYear, Tan
	 * 
	 * @param assessmentYear -- Year for which reconciliation need to pick
	 * @param tan            -- Tan used to pick reconciliation.
	 * @param userEmail      -- Email of user who requested this trigger.
	 */

	public String processReconciliation(int assessmentYear, String tan, String userEmail, String tenantId) {

		// Need To Add Month in fetch query.
		List<TCSBatchUpload> batchUploads = tcsBatchUploadDAO.getBatchList(assessmentYear, Arrays.asList(tan),
				"reconciliation");

		logger.info("Notebook type---: reconciliation");
		SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks()
				.get(ReconciliationTriggerRequest.KEY.UPLOAD_KEY.getFileKey());
		logger.info("Notebook url : {} ", notebook.getUrl());

		ReconciliationTriggerRequest reconciliationFiles = null;
		/*
		 * ReconciliationTriggerRequest reconciliationFiles = new
		 * ReconciliationTriggerRequest().build( new PagedData<>(batchUploads, 0, new
		 * ArrayList<String>()), assessmentYear, tan,
		 * ReconciliationTriggerRequest.KEY.PROCESSED_KEY, tenantId);
		 */
		return triggerReconciliationSparkNotebook(notebook.getUrl(), notebook.getJobId(), reconciliationFiles);
	}

	/**
	 * 
	 * @param api
	 * @param jobId
	 * @param reconciliationFiles
	 * @return
	 */
	private String triggerReconciliationSparkNotebook(String api, int jobId,
			ReconciliationTriggerRequest reconciliationFiles) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + this.dataBricksKey);
		headers.add("Content-Type", "application/json");

		RestTemplate restTemplate = new RestTemplate();
		if (logger.isInfoEnabled()) {
			logger.info("Note Book Object : {}", (reconciliationFiles.toString()));
		}
		logger.info(reconciliationFiles.toString());
		logger.info("token : {}", this.dataBricksKey);

		String dataBricks = api;
		JarJob request = new JarJob();
		request.setJob_id(jobId);
		request.setJar_params(reconciliationFiles.toJSON());
		HttpEntity<JarJob> entity = new HttpEntity<>(request, headers);
		String response = restTemplate.postForObject(dataBricks, entity, String.class);
		logger.info("Response : {}", response);
		return response;
	}

	/**
	 * 
	 * @param tenantId
	 * @param tan
	 * @param userEmail
	 * @return
	 * @throws JsonProcessingException
	 */
	public String triggerPowerBiSparkNoteBook(String tenantId, String tan, String userEmail)
			throws JsonProcessingException {

		SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get("power_Bi");
		logger.info("Notebook 0 url : {} with jobId : {}", notebook.getUrl(), notebook.getJobId());
		TCSBatchUpload batchUpload = new TCSBatchUpload();
		int year = CommonUtil.getAssessmentYear(null);
		batchUpload.setAssessmentYear(year);
		batchUpload.setCollectorMasterTan(tan);
		batchUpload.setUploadType("SYNC_DB");
		batchUpload.setStatus("Uploaded");
		batchUpload.setProcessStartTime(new Date());
		int month = CommonUtil.getAssessmentMonth(year);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setCreatedDate(new Date());
		batchUpload.setCreatedBy("");
		batchUpload.setActive(true);
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessed(0);
		batchUpload.setDuplicateCount(0L);
		batchUpload.setMismatchCount(0L);
		batchUpload.setFileName("interest_calculation" + new Date());
		TCSNoteBookParam noteBookParam = createNoteBook(year, tan, tenantId, userEmail, month, batchUpload, null, null);
		triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam, tenantId);
		tcsBatchUploadDAO.save(batchUpload);
		return "SUCCESS";
	}

	/**
	 * 
	 * @param file
	 * @param tenantId
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 */
	public String uploadFileTOBlob(MultipartFile file, String tenantId)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		String blobUrl = blob.uploadExcelToBlob(file, tenantId);
		return blobUrl;
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	private String convertType(String type) {
		if (type.equalsIgnoreCase("invoice_sap")) {
			type = "invoice_excel";
		} else if (type.equalsIgnoreCase("advance_sap")) {
			type = "advances";
		} else if (type.equalsIgnoreCase("provision_sap")) {
			type = "provisions";
		} else if (type.equalsIgnoreCase("gl_sap")) {
			type = "gl";
		}
		return type;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param type
	 * @param id
	 * @return
	 */
	public TCSBatchUpload getTCSBatchUpload(int assessmentYear, String deductorTan, String type, Integer id) {
		List<TCSBatchUpload> batchUploadResponse = tcsBatchUploadDAO.findById(assessmentYear, deductorTan, type, id);
		if (!batchUploadResponse.isEmpty() && batchUploadResponse != null) {
			return batchUploadResponse.get(0);
		} else {
			throw new CustomException("Did not find a BatchUpload with the passed in criteria", HttpStatus.BAD_REQUEST);
		}
	}

	public TCSBatchUpload getTCSBatchUpload(Integer id) {
		List<TCSBatchUpload> batchUploadResponse = tcsBatchUploadDAO.findById(id);
		if (!batchUploadResponse.isEmpty() && batchUploadResponse != null) {
			return batchUploadResponse.get(0);
		} else {
			throw new CustomException("Did not find a BatchUpload with the passed in criteria", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * This method for get list of tcs batch upload types
	 * 
	 * @param types
	 * @param asList
	 * @param parseInt
	 * @return
	 */
	public List<BatchUploadDTO> getListOfBatchUploadFileTypes(List<String> types, String tan, int year,
			String tenantId) {

		List<BatchUploadDTO> batchListDTO = new LinkedList<BatchUploadDTO>();
		logger.info("Tenant Tan ---: {}", tan);
		MultiTenantContext.setTenantId(tenantId);
		List<TCSBatchUpload> batchUploadList = new ArrayList<>();
		batchUploadList = tcsBatchUploadDAO.getBatchFileTypes(year, tan, types);
		Collections.reverse(batchUploadList);
		for (TCSBatchUpload batchUpload : batchUploadList) {
			BatchUploadDTO batchDTO = new BatchUploadDTO();
			batchDTO.setId(batchUpload.getId());
			batchDTO.setRunId(batchUpload.getRunId());
			batchDTO.setDateOfUpload(batchUpload.getCreatedDate());
			batchDTO.setFileStatus(batchUpload.getStatus());
			batchDTO.setUploadType(batchUpload.getUploadType());
			batchDTO.setUploadedFileDownloadUrl(batchUpload.getFilePath());
			batchDTO.setUploadBy(batchUpload.getCreatedBy());
			batchDTO.setNew_status(batchUpload.getNewStatus());
			if (batchUpload.getUploadType().contains("SAP") && StringUtils.isNotBlank(batchUpload.getFileName())) {
				batchDTO.setFileName(
						batchUpload.getFileName().substring(batchUpload.getFileName().lastIndexOf("/") + 1));
			} else {
				batchDTO.setFileName(batchUpload.getFileName());
			}
			if (batchUpload.getProcessEndTime() != null) {
				batchDTO.setProcessEndTime(batchUpload.getProcessEndTime());
			} else {
				batchDTO.setProcessEndTime(null);
			}
			if (batchUpload.getRowsCount() != null) {
				batchDTO.setTotalRecords(batchUpload.getRowsCount());
			} else {
				batchDTO.setTotalRecords(0L);
			}
			if (batchUpload.getDuplicateCount() != null) {
				batchDTO.setDuplicateRecords(batchUpload.getDuplicateCount());
			} else {
				batchDTO.setDuplicateRecords(0L);
			}

			if (batchUpload.getMismatchCount() != null) {
				batchDTO.setMismatchCount(batchUpload.getMismatchCount());
			} else {
				batchDTO.setMismatchCount(0L);
			}
			if (batchUpload.getFailedCount() != null) {
				batchDTO.setErrorRecords(batchUpload.getFailedCount());
			} else {
				batchDTO.setProcessedRecords(0L);
			}
			if (batchUpload.getProcessed() != null) {
				batchDTO.setProcessedRecords(batchUpload.getProcessed());
			} else {
				batchDTO.setProcessedRecords(0L);
			}
			batchDTO.setErrorFileDownloadUrl(batchUpload.getErrorFilePath());
			batchDTO.setSucessFileDownloadUrl(batchUpload.getSuccessFileUrl());
			batchDTO.setOtherFileDownloadUrl(batchUpload.getOtherFileUrl());
			batchDTO.setYear(batchUpload.getAssessmentYear());
			batchDTO.setSuccessCount(batchUpload.getSuccessCount());
			batchUpload.setAssessmentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
			batchDTO.setMonth(batchUpload.getAssessmentMonth());
			batchDTO.setTan(batchUpload.getCollectorMasterTan());
			if (batchUpload.getSourceIdentifier() != null && batchUpload.getUploadType().equals("reconciliation")) {
				batchDTO.setFileType(batchUpload.getSourceIdentifier());
			} else {
				batchDTO.setFileType(batchUpload.getUploadType());
			}
			batchListDTO.add(batchDTO);
		}
		return batchListDTO;

	}

	public void InactivatingLiabilityRecords(Integer assessmentYear, String collectorTan, String tenantId, String pan,
			Integer challanMonth, String userName, List<TCSBatchUpload> batchUploadList) {
		MultiTenantContext.setTenantId(tenantId);
		if (!batchUploadList.isEmpty()) {
			for (TCSBatchUpload batchUpload : batchUploadList) {
				liabilityBatchUpload(batchUpload, collectorTan, assessmentYear, challanMonth, userName, tenantId, true,
						"Processed");
			}

		}

	}

	public List<TCSBatchUpload> liabilityCaluclation(Integer assessmentYear, String collectorTan, String tenantId,
			String pan, Integer challanMonth, String userName, String type) throws JsonProcessingException {
		MultiTenantContext.setTenantId(tenantId);

		String status = "Processed";
		TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
		String reverseLiability = "REVERSE_LIABILITY_" + assessmentYear + "_" + challanMonth;
		String can = "LIABILITY_CAN_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String cr = "LIABILITY_CR_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String payment = "LIABILITY_PAYMENT_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String lcc = "LIABILITY_LCC_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String threshold = "LIABILITY_RECONCILATION_" + assessmentYear + "_" + challanMonth;

		List<String> fileNames = new ArrayList<>();
		fileNames.add(can);
		fileNames.add(cr);
		fileNames.add(payment);
		fileNames.add(lcc);
		fileNames.add(threshold);
		fileNames.add(reverseLiability);
		if (type.equalsIgnoreCase("REVERSE_LIABILITY")) {
			List<TCSBatchUpload> batchUploadAllList = tcsBatchUploadDAO.getByFileNames(assessmentYear, collectorTan,
					fileNames, challanMonth);
			if (batchUploadAllList.size() == 6) {
				// inactivating old records
				InactivatingLiabilityRecords(assessmentYear, collectorTan, tenantId, pan, challanMonth, userName,
						batchUploadAllList);
			}

			List<TCSBatchUpload> batchUploads = tcsBatchUploadDAO.getByFileName(assessmentYear, collectorTan,
					reverseLiability, challanMonth);
			if (batchUploads.isEmpty()) {
				tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				tcsBatchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				tcsBatchUpload.setFileName(reverseLiability);
				tcsBatchUpload.setCreatedBy(userName);
				tcsBatchUpload.setModifiedBy(userName);
				tcsBatchUpload.setRowsCount(0L);
				tcsBatchUpload.setSuccessCount(0L);
				tcsBatchUpload.setFailedCount(0L);
				tcsBatchUpload.setRowsCount(0L);
				tcsBatchUpload.setProcessed(0);
				tcsBatchUpload.setMismatchCount(0L);
				tcsBatchUpload.setStatus("Processing");
				tcsBatchUpload = liabilityBatchUpload(tcsBatchUpload, collectorTan, assessmentYear, challanMonth,
						userName, tenantId, false, status);

				try {
					tcsInvoiceLineItemDAO.USPReverseLiabilityCaluclation(assessmentYear, challanMonth, collectorTan,
							pan);
				} catch (Exception e) {
					status = "Failed";
					logger.error("Error ocuured while running USPReverseLiabilityCaluclation", e);
				}

				tcsBatchUpload = liabilityBatchUpload(tcsBatchUpload, collectorTan, assessmentYear, challanMonth,
						userName, tenantId, false, status);

			} else {
				tcsBatchUpload = batchUploads.get(0);
			}
		} else if (type.equalsIgnoreCase("LIABILITY_CAN_ADJUSTMENT")) {
			tcsBatchUpload = liabilityCanProcess(assessmentYear, collectorTan, tenantId, pan, challanMonth, userName);
		} else if (type.equalsIgnoreCase("LIABILITY_CR_ADJUSTMENT")) {
			tcsBatchUpload = liabilityCrProcess(assessmentYear, collectorTan, tenantId, pan, challanMonth, userName);
		} else if (type.equalsIgnoreCase("LIABILITY_PAYMENT_ADJUSTMENT")) {
			tcsBatchUpload = liabilityAdvanceProcess(assessmentYear, collectorTan, tenantId, pan, challanMonth,
					userName);
		} else if (type.equalsIgnoreCase("LIABILITY_LCC_ADJUSTMENT")) {
			tcsBatchUpload = liabilityLccProcess(assessmentYear, collectorTan, tenantId, pan, challanMonth, userName);
		} else if (type.equalsIgnoreCase("LIABILITY_RECONCILATION")) {
			List<TCSBatchUpload> batchUploadList = tcsBatchUploadDAO.getByFileName(assessmentYear, collectorTan, threshold,
					challanMonth);
			if(batchUploadList.isEmpty()) {
				BatchProcessDTO batchProcessDTO = new BatchProcessDTO();
				batchProcessDTO.setYear(assessmentYear);
				batchProcessDTO.setMonth(challanMonth);
				batchProcessDTO.setType("BATCHPROCESS_LIABILITYRECON");
				tcsBatchUpload = batchProcess(userName, collectorTan, tenantId, batchProcessDTO);
			}
		}
		List<TCSBatchUpload> batchUploadAllList = tcsBatchUploadDAO.getByFileNames(assessmentYear, collectorTan,
				fileNames, challanMonth);
		return batchUploadAllList;
	}

	public TCSBatchUpload liabilityCanProcess(Integer assessmentYear, String collectorTan, String tenantId, String pan,
			Integer challanMonth, String userName) {
		MultiTenantContext.setTenantId(tenantId);
		String status = "Processed";

		String fileName = "LIABILITY_CAN_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		List<TCSBatchUpload> batchUploadList = tcsBatchUploadDAO.getByFileName(assessmentYear, collectorTan, fileName,
				challanMonth);
		TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
		if (batchUploadList.isEmpty()) {
			tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setFileName(fileName);
			tcsBatchUpload.setCreatedBy(userName);
			tcsBatchUpload.setModifiedBy(userName);
			tcsBatchUpload.setRowsCount(0L);
			tcsBatchUpload.setSuccessCount(0L);
			tcsBatchUpload.setFailedCount(0L);
			tcsBatchUpload.setRowsCount(0L);
			tcsBatchUpload.setProcessed(0);
			tcsBatchUpload.setMismatchCount(0L);
			tcsBatchUpload.setStatus("Processing");
			tcsBatchUpload = liabilityBatchUpload(tcsBatchUpload, collectorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status);
			try {
				tcsInvoiceLineItemDAO.USPCANAdjustments(assessmentYear, challanMonth, collectorTan, pan);
			} catch (Exception e) {
				status = "Failed";
				logger.error("Error ocuured while running USPCANAdjustments", e);
			}

			tcsBatchUpload = liabilityBatchUpload(tcsBatchUpload, collectorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status);
		} else {
			tcsBatchUpload = batchUploadList.get(0);
		}

		return tcsBatchUpload;
	}

	public TCSBatchUpload liabilityCrProcess(Integer assessmentYear, String collectorTan, String tenantId, String pan,
			Integer challanMonth, String userName) {

		String status = "Processed";
		MultiTenantContext.setTenantId(tenantId);

		String fileName = "LIABILITY_CR_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;

		List<TCSBatchUpload> batchUploadList = tcsBatchUploadDAO.getByFileName(assessmentYear, collectorTan, fileName,
				challanMonth);
		TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
		if (batchUploadList.isEmpty()) {
			tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setFileName(fileName);
			tcsBatchUpload.setCreatedBy(userName);
			tcsBatchUpload.setModifiedBy(userName);
			tcsBatchUpload.setRowsCount(0L);
			tcsBatchUpload.setSuccessCount(0L);
			tcsBatchUpload.setFailedCount(0L);
			tcsBatchUpload.setRowsCount(0L);
			tcsBatchUpload.setProcessed(0);
			tcsBatchUpload.setMismatchCount(0L);
			tcsBatchUpload.setStatus("Processing");
			tcsBatchUpload = liabilityBatchUpload(tcsBatchUpload, collectorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status);
			try {
				tcsInvoiceLineItemDAO.USPCRAdjustments(assessmentYear, challanMonth, collectorTan, pan);
			} catch (Exception e) {
				status = "Failed";
				logger.error("Error ocuured while running USPCRAdjustments", e);
			}

			tcsBatchUpload = liabilityBatchUpload(tcsBatchUpload, collectorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status);
		} else {
			tcsBatchUpload = batchUploadList.get(0);
		}

		return tcsBatchUpload;
	}

	public TCSBatchUpload liabilityAdvanceProcess(Integer assessmentYear, String collectorTan, String tenantId,
			String pan, Integer challanMonth, String userName) {

		MultiTenantContext.setTenantId(tenantId);
		String status = "Processed";

		String fileName = "LIABILITY_PAYMENT_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		List<TCSBatchUpload> batchUploadList = tcsBatchUploadDAO.getByFileName(assessmentYear, collectorTan, fileName,
				challanMonth);
		TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
		if (batchUploadList.isEmpty()) {
			tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setFileName(fileName);
			tcsBatchUpload.setCreatedBy(userName);
			tcsBatchUpload.setModifiedBy(userName);
			tcsBatchUpload.setRowsCount(0L);
			tcsBatchUpload.setSuccessCount(0L);
			tcsBatchUpload.setFailedCount(0L);
			tcsBatchUpload.setRowsCount(0L);
			tcsBatchUpload.setProcessed(0);
			tcsBatchUpload.setMismatchCount(0L);
			tcsBatchUpload.setStatus("Processing");
			tcsBatchUpload = liabilityBatchUpload(tcsBatchUpload, collectorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status);

			try {
				tcsInvoiceLineItemDAO.USPPaymentUtilization(assessmentYear, challanMonth, collectorTan, pan);
			} catch (Exception e) {
				status = "Failed";
				logger.error("Error ocuured while running USPPaymentUtilization", e);
			}

			tcsBatchUpload = liabilityBatchUpload(tcsBatchUpload, collectorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status);
		} else {
			tcsBatchUpload = batchUploadList.get(0);
		}

		return tcsBatchUpload;
	}

	public TCSBatchUpload liabilityLccProcess(Integer assessmentYear, String collectorTan, String tenantId, String pan,
			Integer challanMonth, String userName) {
		String status = "Processed";
		String fileName = "LIABILITY_LCC_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;

		List<TCSBatchUpload> batchUploadList = tcsBatchUploadDAO.getByFileName(assessmentYear, collectorTan, fileName,
				challanMonth);
		TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
		if (batchUploadList.isEmpty()) {
			tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setFileName(fileName);
			tcsBatchUpload.setCreatedBy(userName);
			tcsBatchUpload.setModifiedBy(userName);
			tcsBatchUpload.setRowsCount(0L);
			tcsBatchUpload.setSuccessCount(0L);
			tcsBatchUpload.setFailedCount(0L);
			tcsBatchUpload.setRowsCount(0L);
			tcsBatchUpload.setProcessed(0);
			tcsBatchUpload.setMismatchCount(0L);
			tcsBatchUpload.setStatus("Processing");
			tcsBatchUpload = liabilityBatchUpload(tcsBatchUpload, collectorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status);

			try {
				tcsInvoiceLineItemDAO.USPLCCInvoiceAdjustments(assessmentYear, challanMonth, collectorTan, pan);
			} catch (Exception e) {
				status = "Failed";
				logger.error("Error ocuured while running USPLCCInvoiceAdjustments", e);
			}

			tcsBatchUpload = liabilityBatchUpload(tcsBatchUpload, collectorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status);
		} else {
			tcsBatchUpload = batchUploadList.get(0);
		}

		return tcsBatchUpload;
	}

	private TCSBatchUpload liabilityBatchUpload(TCSBatchUpload tcsBatchUpload, String collectorTan,
			Integer assessmentYear, Integer challanMonth, String userName, String tenantId, Boolean isInactivate,
			String status) {
		logger.info("batch", tcsBatchUpload);
		MultiTenantContext.setTenantId(tenantId);
		tcsBatchUpload.setAssessmentMonth(challanMonth);
		tcsBatchUpload.setAssessmentYear(assessmentYear);
		tcsBatchUpload.setCollectorMasterTan(collectorTan);
		tcsBatchUpload.setUploadType(UploadTypes.BATCHPROCESS_LIABILITYRECON.name());
		tcsBatchUpload.setCreatedBy(userName);
		tcsBatchUpload.setActive(true);
		if (tcsBatchUpload.getId() != null) {
			if (isInactivate) {
				tcsBatchUpload.setActive(false);
			} else {
				tcsBatchUpload.setId(tcsBatchUpload.getId());
				tcsBatchUpload.setStatus(status);
				tcsBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			}
			tcsBatchUploadDAO.update(tcsBatchUpload);
		} else {
			tcsBatchUploadDAO.save(tcsBatchUpload);
		}
		return tcsBatchUpload;
	}

	public List<BatchUploadDTO> setBatchDto(List<TCSBatchUpload> batchUploadList) {
		// adding to DTO
		List<BatchUploadDTO> batchUploadDTOList = new ArrayList<>();
		for (TCSBatchUpload batchUpload : batchUploadList) {
			BatchUploadDTO batchDTO = new BatchUploadDTO();
			batchDTO.setId(batchUpload.getId());
			batchDTO.setRunId(batchUpload.getRunId());
			batchDTO.setDateOfUpload(batchUpload.getCreatedDate());
			batchDTO.setFileName(batchUpload.getFileName());
			batchDTO.setFileStatus(batchUpload.getStatus());
			batchDTO.setUploadedFileDownloadUrl(batchUpload.getFilePath());
			batchDTO.setUploadBy(batchUpload.getCreatedBy());
			if (batchUpload.getProcessEndTime() != null) {
				batchDTO.setProcessEndTime(batchUpload.getProcessEndTime());
			} else {
				batchDTO.setProcessEndTime(null);
			}
			if (batchUpload.getRowsCount() != null) {
				batchDTO.setTotalRecords(batchUpload.getRowsCount());
			} else {
				batchDTO.setTotalRecords(0L);
			}
			if (batchUpload.getDuplicateCount() != null) {
				batchDTO.setDuplicateRecords(batchUpload.getDuplicateCount());
			} else {
				batchDTO.setDuplicateRecords(0L);
			}

			if (batchUpload.getMismatchCount() != null) {
				batchDTO.setMismatchCount(batchUpload.getMismatchCount());
			} else {
				batchDTO.setMismatchCount(0L);
			}
			if (batchUpload.getFailedCount() != null) {
				batchDTO.setErrorRecords(batchUpload.getFailedCount());
			} else {
				batchDTO.setProcessedRecords(0L);
			}
			if (batchUpload.getProcessed() != null) {
				batchDTO.setProcessedRecords(batchUpload.getProcessed());
			} else {
				batchDTO.setProcessedRecords(0L);
			}
			batchDTO.setErrorFileDownloadUrl(batchUpload.getErrorFilePath());
			batchDTO.setSucessFileDownloadUrl(batchUpload.getSuccessFileUrl());
			batchDTO.setOtherFileDownloadUrl(batchUpload.getOtherFileUrl());
			batchDTO.setYear(batchUpload.getAssessmentYear());
			batchDTO.setSuccessCount(batchUpload.getSuccessCount());
			batchUpload.setAssessmentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
			batchDTO.setMonth(batchUpload.getAssessmentMonth());
			batchDTO.setTan(batchUpload.getCollectorMasterTan());
			if (batchUpload.getSourceIdentifier() != null && batchUpload.getUploadType().equals("reconciliation")) {
				batchDTO.setFileType(batchUpload.getSourceIdentifier());
			} else {
				batchDTO.setFileType(batchUpload.getUploadType());
			}
			batchDTO.setUploadType(batchUpload.getUploadType());
			batchUploadDTOList.add(batchDTO);
		}
		return batchUploadDTOList;
	}

}
