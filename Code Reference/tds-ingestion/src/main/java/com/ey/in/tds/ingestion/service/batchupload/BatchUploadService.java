package com.ey.in.tds.ingestion.service.batchupload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
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
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
import com.ey.in.tds.common.dashboard.dto.ActivityTracker;
import com.ey.in.tds.common.dashboards.jdbc.dao.ActivityTrackerDAO;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.ReconciliationTriggerRequest;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadGroupDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUploadGroup;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.dto.BatchProcessDTO;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.dto.receipt.ReceiptDataDTO;
import com.ey.in.tds.common.dto.sftp.SftpPathDTO;
import com.ey.in.tds.common.ingestion.response.dto.BatchUploadResponseDTO;
import com.ey.in.tds.common.ingestion.response.dto.RunStatus;
import com.ey.in.tds.common.ingestion.response.dto.SparkResponseDTO;
import com.ey.in.tds.common.ingestion.response.dto.SparkState;
import com.ey.in.tds.common.model.job.JarJob;
import com.ey.in.tds.common.model.job.Job;
import com.ey.in.tds.common.model.job.NoteBookBatch;
import com.ey.in.tds.common.model.job.NoteBookParam;
import com.ey.in.tds.common.onboarding.response.dto.DeductorOnboardingInformationResponseDTO;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.service.Sha256SumService;
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
import com.ey.in.tds.ingestion.jdbc.dao.AdvanceDAO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceLineItemDAO;
import com.ey.in.tds.ingestion.jdbc.dao.ProvisionDAO;
import com.ey.in.tds.ingestion.service.advance.AdvanceService;
import com.ey.in.tds.ingestion.service.invoicelineitem.InvoiceLineItemService;
import com.ey.in.tds.ingestion.service.invoicemismatch.InvoiceMismatchService;
import com.ey.in.tds.ingestion.service.provision.ProvisionMismatchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;

@Service
public class BatchUploadService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private BlobStorage blob;

	@Value("${databricks.key}")
	private String dataBricksKey;

	@Autowired
	private ChallanClient challanClient;

	@Autowired
	private InvoiceMismatchService invoiceMismatchService;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private SparkNotebooks sparkNotebooks;

	@Autowired
	private AdvanceService advanceService;

	@Autowired
	private ProvisionMismatchService provisionMismatchService;

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	TenantProperties tenantProperties;

	@Autowired
	private MastersClient masterClient;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private ActivityTrackerDAO activityTrackerDAO;

	@Autowired
	private BatchUploadGroupDAO batchUploadGroupDAO;

	@Autowired
	TCSBatchUploadDAO tCSBatchUploadDAO;

	@Autowired
	InvoiceLineItemService invoiceLineItemService;

	@Autowired
	private InvoiceLineItemDAO invoiceLineItemDAO;

	@Autowired
	private ProvisionDAO provisionDAO;

	@Autowired
	private AdvanceDAO advanceDAO;

	@Autowired
	private ResourceLoader resourceLoader;

	@Value("${sparkjob.runstaus.url}")
	private String runStatusUrl;

	public BatchUpload create(BatchUpload batchUpload) {
		return batchUploadDAO.save(batchUpload);
	}

	public BatchUpload update(BatchUpload batchUpload) {
		return batchUploadDAO.update(batchUpload);
	}

	public BatchUpload get(int assessmentYear, String deductorTan, String type, Integer id) {
		List<BatchUpload> batchUploadResponse = batchUploadDAO.findById(assessmentYear, deductorTan, type, id);
		if (!batchUploadResponse.isEmpty() && batchUploadResponse != null) {
			return batchUploadResponse.get(0);
		} else {
			throw new CustomException("Did not find a BatchUpload with the passed in criteria", HttpStatus.BAD_REQUEST);
		}
	}

	public List<BatchUpload> getBatchUploadByAssessmentYear(int assessmentYear, Pagination pagination) {
		return batchUploadDAO.getBatchUploadsByAssessmentYear(assessmentYear);
	}

	public List<BatchUpload> getBatchUploadByAssessmentYearDeductorTan(int assessmentYear, List<String> deductorTan,
			Pagination pagination) {
		return batchUploadDAO.getBatchUploadsByAssessmentYearDeductorTan(assessmentYear, deductorTan, pagination);
	}

	public void deleteById(int assessmentYear, String deductorTan, String type, Integer id) {
		batchUploadDAO.deleteById(id);
	}

	public List<BatchUploadDTO> getListOfBatchUploadFiles(String type, List<String> tan, int year,
			Pagination pagination) {

		List<BatchUploadDTO> batchListDTO = new LinkedList<BatchUploadDTO>();
		logger.info("Tenant Tan ---: {}", tan);
		List<BatchUpload> batchUploadList = new ArrayList<>();
		List<String> uploadType = new ArrayList<>();
		if ("DEDUCTEE_EXCEL".equalsIgnoreCase(type)) {
			uploadType.add("DEDUCTEE_SAP");
		} if ("ADVANCES".equalsIgnoreCase(type)) {
			uploadType.add("ADVANCE_SAP");
		} else if ("PROVISIONS".equalsIgnoreCase(type)) {
			uploadType.add("PROVISION_SAP");
		} else if ("GL".equalsIgnoreCase(type)) {
			uploadType.add("GL_SAP");
		}
		uploadType.add(type);
		batchUploadList = batchUploadDAO.getBatchFilesList(year, tan, uploadType);

		for (BatchUpload batchUpload : batchUploadList) {
			BatchUploadDTO batchDTO = new BatchUploadDTO();
			batchDTO.setId(batchUpload.getBatchUploadID());
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
			if (batchUpload.getProcessedCount() != null) {
				batchDTO.setProcessedRecords(batchUpload.getProcessedCount());
			} else {
				batchDTO.setProcessedRecords(0L);
			}
			if (batchUpload.getProcessed() != null) {
				batchDTO.setProcessedRecords(batchUpload.getProcessed());
			} 
			batchDTO.setSuccessCount(batchUpload.getSuccessCount());
			batchDTO.setErrorFileDownloadUrl(batchUpload.getErrorFilePath());
			batchDTO.setSucessFileDownloadUrl(batchUpload.getSuccessFileUrl());
			batchDTO.setOtherFileDownloadUrl(batchUpload.getOtherFileUrl());
			batchDTO.setYear(batchUpload.getAssessmentYear());
			batchUpload.setAssessmentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
			batchDTO.setMonth(batchUpload.getAssessmentMonth());
			batchDTO.setTan(batchUpload.getDeductorMasterTan());
			batchDTO.setRunId(batchUpload.getRunId());
			batchDTO.setNotdsCount(batchUpload.getNotdsCount());
			batchDTO.setNotdsFileUrl(batchUpload.getNotdsFileUrl());
			if (batchUpload.getSourceIdentifier() != null && batchUpload.getUploadType().equals("reconciliation")) {
				batchDTO.setFileType(batchUpload.getSourceIdentifier());
			} else {
				batchDTO.setFileType(batchUpload.getUploadType());
			}
			batchListDTO.add(batchDTO);
		}

		return batchListDTO;
	}

	public List<BatchUpload> getBatchUploads(String type, List<String> tan, int year, Pagination pagination) {
		return batchUploadDAO.getBatchList(year, tan, type);
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
	public BatchUpload saveUploadPDF(Integer assessmentYear, MultipartFile[] files, String tan, String type,
			String tenantId, String userName, String groupName, String postingDate, Pagination pagination,
			String residentType, String pan)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, ParseException {
		if (logger.isDebugEnabled()) {
			logger.debug("REST request for file upload Pdf's : {}, {}", files, tan);
		}
		int assessmentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
		String path;
		BatchUpload batchUpload = null;
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
				List<BatchUpload> batch = batchUploadDAO.getSha256Records(sha256);

				path = blob.uploadExcelToBlob(files[i], tenantId);
				batchUpload = new BatchUpload();
				batchUpload.setAssessmentYear(assessmentYear);
				batchUpload.setDeductorMasterTan(tan);
				batchUpload.setUploadType(type);
				if (!batch.isEmpty()) {
					logger.info("Duplicate PDF file Uploaded (: {} ) ", files[i].getOriginalFilename());
					batchUpload.setStatus("Duplicate");
					batchUpload.setSha256sum(sha256);
					batchUpload.setReferenceId(batch.get(0).getBatchUploadID());
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
				batchUpload.setProcessedCount(1);
				batchUpload.setDuplicateCount(0L);
				batchUpload.setMismatchCount(0L);
				batchUpload.setBatchUploadGroupId(batchGroup.getBatchUploadGroupID());
				batchUpload = batchUploadDAO.save(batchUpload);
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
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws InvalidFormatException
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */
	public BatchUpload saveUploadExcel(int assessmentYear, MultipartFile[] files, String tan, String type,
			String tenantId, String userEmail, String deductorPan, Integer keywordId, String token)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, InvalidFormatException,
			ParseException, IntrusionException, ValidationException {
		MultipartFile file = null;
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		String path;
		String csvPath = null;
		BatchUpload batchUpload = null;

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

				List<BatchUpload> batch = batchUploadDAO.getSha256Records(sha256);

				path = blob.uploadExcelToBlob(file, tenantId);
				if (logger.isDebugEnabled()) {
					logger.debug("Batch Object : {}", batch);
				}
				boolean isDuplicate = !batch.isEmpty() && !"invoice_remediation_import".equalsIgnoreCase(type)
						&& !"advance_remediation_import".equalsIgnoreCase(type)
						&& !"provision_remediation_import".equalsIgnoreCase(type)
						&& !"CHART_OF_ACCOUNTS".equalsIgnoreCase(type) && !"KEYWORDS_EXCEL".equalsIgnoreCase(type);
				boolean isJavaProcess = "invoice_remediation_import".equalsIgnoreCase(type)
						|| "advance_remediation_import".equalsIgnoreCase(type)
						|| "provision_remediation_import".equalsIgnoreCase(type)
						|| "CHART_OF_ACCOUNTS".equalsIgnoreCase(type) || "KEYWORDS_EXCEL".equalsIgnoreCase(type);

				// converting to CSV
				if (("invoice_excel".equalsIgnoreCase(type) || "provisions".equalsIgnoreCase(type)
						|| "advances".equalsIgnoreCase(type) || "gl".equalsIgnoreCase(type))
						&& FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {

					File f = new File(file.getOriginalFilename());
					try (OutputStream os = new FileOutputStream(f)) {
						os.write(file.getBytes());
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

				batchUpload = saveBatchUpload(assessmentYear, tan, type, userEmail, file, path, month, sha256, batch,
						isDuplicate, isJavaProcess, csvPath);

				boolean isUnique = batchUpload.getStatus().equalsIgnoreCase("Uploaded");

				logger.info("Notebook type : {}", type);
				NoteBookParam noteBookParam = createNoteBook(assessmentYear, tan, tenantId, userEmail, month,
						batchUpload, "", "");

				if (isUnique && ("invoice_excel".equalsIgnoreCase(type) || "provisions".equalsIgnoreCase(type)
						|| "advances".equalsIgnoreCase(type) || "gl".equalsIgnoreCase(type)
						|| "invoice_sap".equalsIgnoreCase(type) || "advance_sap".equalsIgnoreCase(type)
						|| "provision_sap".equalsIgnoreCase(type) || "gl_sap".equalsIgnoreCase(type))) {

					// Note book 0 trigger
					boolean isNotebook0Exists = false;
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
								if (StringUtils.isNotBlank(
										deductorOnboardingInformation.getCustomJobs().get(type.toLowerCase()))) {
									try {
										int jobId = Integer.valueOf(
												deductorOnboardingInformation.getCustomJobs().get(type.toLowerCase()))
												.intValue();
										if (jobId > 0) {
											isNotebook0Exists = true;
											// converting the type
											type = convertType(type);

											SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks()
													.get(type.toLowerCase());
											logger.info("Notebook 0 url : {} with jobId : {}", notebook.getUrl(),
													jobId);
											sparkResponse = triggerSparkNotebook(notebook.getUrl(), jobId,
													noteBookParam, month, assessmentYear, tenantId, tan, userEmail);
											SparkResponseDTO sparkResponseDTO = new ObjectMapper()
													.readValue(sparkResponse, SparkResponseDTO.class);
											batchUpload = updateBatchUpload(
													Integer.valueOf(sparkResponseDTO.getRun_id()), batchUpload);

											List<ActivityTracker> listTracker = activityTrackerDAO
													.getActivityTrackerByTanYearTypeAndMonth(tan, assessmentYear,
															"TDS Calculation", month);
											if (!listTracker.isEmpty()) {
												listTracker.get(0).setStatus(ActivityTrackerStatus.VALIDATED.name());
												listTracker.get(0).setModifiedBy(userEmail);
												listTracker.get(0).setModifiedDate(new Date());
												activityTrackerDAO.save(listTracker.get(0));
											}
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
						// converting the type
						type = convertType(type);
						SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get(type.toLowerCase());
						logger.info("Notebook url : {}", notebook.getUrl());
						sparkResponse = triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam,
								month, assessmentYear, tenantId, tan, userEmail);
						SparkResponseDTO sparkResponseDTO = new ObjectMapper().readValue(sparkResponse,
								SparkResponseDTO.class);
						batchUpload = updateBatchUpload(Integer.valueOf(sparkResponseDTO.getRun_id()), batchUpload);
					}
				} else if ("invoice_remediation_import".equalsIgnoreCase(type)) {
					batchUpload = invoiceMismatchService.asyncUpdateRemediationReport(tan, batchUpload, path, tenantId,
							deductorPan, token, assessmentYear, userEmail, month);
				} else if ("advance_remediation_import".equalsIgnoreCase(type)) {
					batchUpload = advanceService.asyncUpdateRemediationReport(tan, batchUpload, path, tenantId,
							deductorPan, assessmentYear, userEmail);
				} else if ("provision_remediation_import".equalsIgnoreCase(type)) {
					batchUpload = provisionMismatchService.asyncUpdateProvisionRemediationReport(tan, batchUpload, path,
							tenantId, deductorPan, assessmentYear, userEmail);
				} else if ("KEYWORDS_EXCEL".equalsIgnoreCase(type)) {
					ResponseEntity<ApiStatus<BatchUpload>> batchData = onboardingClient.uploadKeywordsExcel(file,
							tenantId, userEmail, deductorPan, batchUpload.getBatchUploadID());
					batchUpload = batchData.getBody().getData();
				} else if ("CHART_OF_ACCOUNTS".equalsIgnoreCase(type)) {
					ResponseEntity<ApiStatus<BatchUpload>> response = onboardingClient.createdChartOfAccounts(file, tan,
							assessmentYear, type, tenantId, deductorPan, userEmail, batchUpload.getBatchUploadID());
					batchUpload = response.getBody().getData();
				} else if ("CUSTOMER_KYC_REMEDIATION_IMPORT".equalsIgnoreCase(type)) {
					ResponseEntity<ApiStatus<BatchUpload>> response = onboardingClient.uploadKycExcel(tenantId,
							userEmail, deductorPan, tan, batchUpload.getBatchUploadID(), type);
					batchUpload = response.getBody().getData();
				} else if ("CUSTOMER_KYC_FINAL_RESPONSE_REPORT".equalsIgnoreCase(type)) {
                    ResponseEntity<ApiStatus<BatchUpload>> response = onboardingClient.uploadKycFinalReportExcel(tenantId,
                            userEmail, deductorPan, tan, batchUpload.getBatchUploadID(), type);
                    batchUpload = response.getBody().getData();
                } else if ("VENDOR_KYC_FINAL_RESPONSE_REPORT".equalsIgnoreCase(type)) {
                    ResponseEntity<ApiStatus<BatchUpload>> response = onboardingClient.uploadKycFinalReportExcel(tenantId,
                            userEmail, deductorPan, tan, batchUpload.getBatchUploadID(), type);
                    batchUpload = response.getBody().getData();
                } else if ("SHAREHOLDER_KYC_FINAL_RESPONSE_REPORT".equalsIgnoreCase(type)) {
                    ResponseEntity<ApiStatus<BatchUpload>> response = onboardingClient.uploadKycFinalReportExcel(tenantId,
                            userEmail, deductorPan, tan, batchUpload.getBatchUploadID(), type);
                    batchUpload = response.getBody().getData();
                } else if ("PO_UPDATE_UPLOAD".equalsIgnoreCase(type)) {
					batchUpload = advanceService.asyncUpdatePORemediationReport(tan, batchUpload, path, tenantId,
							deductorPan, token, assessmentYear, userEmail, month);
				}
			}
		} else if (files != null && files.length > 1) {
			boolean isNotebook0Exists = false;
			List<NoteBookBatch> batchEntries = new ArrayList<>();
			for (int i = 0; i < files.length; i++) {
				file = files[i];
				logger.info("FILE NAME: {}", file.getOriginalFilename());

				logger.info(file.getOriginalFilename());
				String sha256 = sha256SumService.getSHA256Hash(file);

				List<BatchUpload> batch = batchUploadDAO.getSha256Records(sha256);

				path = blob.uploadExcelToBlob(file, tenantId);
				if (logger.isDebugEnabled()) {
					logger.debug("Batch Object : {}", batch);
				}
				boolean isDuplicate = !batch.isEmpty() && !"invoice_remediation_import".equalsIgnoreCase(type)
						&& !"advance_remediation_import".equalsIgnoreCase(type)
						&& !"provision_remediation_import".equalsIgnoreCase(type)
						&& !"CHART_OF_ACCOUNTS".equalsIgnoreCase(type);
				boolean isJavaProcess = "invoice_remediation_import".equalsIgnoreCase(type)
						|| "advance_remediation_import".equalsIgnoreCase(type)
						|| "provision_remediation_import".equalsIgnoreCase(type)
						|| "CHART_OF_ACCOUNTS".equalsIgnoreCase(type);

				// converting to CSV
				if (("invoice_excel".equalsIgnoreCase(type) || "provisions".equalsIgnoreCase(type)
						|| "advances".equalsIgnoreCase(type) || "gl".equalsIgnoreCase(type))
						&& FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {

					File f = new File(file.getOriginalFilename());
					try (OutputStream os = new FileOutputStream(f)) {
						os.write(file.getBytes());
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

				batchUpload = saveBatchUpload(assessmentYear, tan, type, userEmail, file, path, month, sha256, batch,
						isDuplicate, isJavaProcess, csvPath);

				boolean isUnique = batchUpload.getStatus().equalsIgnoreCase("Uploaded");

				logger.info("Notebook type : {}", type);

				if (isUnique && ("invoice_excel".equalsIgnoreCase(type) || "provisions".equalsIgnoreCase(type)
						|| "advances".equalsIgnoreCase(type) || "gl".equalsIgnoreCase(type))) {
					NoteBookBatch noteBookBatch = new NoteBookBatch();
					if (batchUpload != null) {
						noteBookBatch.setId(batchUpload.getBatchUploadID());
						noteBookBatch.setStatus(batchUpload.getStatus());
						noteBookBatch.setType(batchUpload.getUploadType());
						if (StringUtils.isNotBlank(batchUpload.getSha256sum())) {
							noteBookBatch.setSha256(batchUpload.getSha256sum());
						} else {
							noteBookBatch.setSha256("");
						}
						if (StringUtils.isNotBlank(batchUpload.getFilePath())) {
							// extracting file path from the file url
							noteBookBatch.setFileName(batchUpload.getFilePath()
									.substring(batchUpload.getFilePath().lastIndexOf("/") + 1));
						} else {
							noteBookBatch.setFileName("");
						}
					}
					batchEntries.add(noteBookBatch);
				}
			}
			NoteBookParam noteBookParam = createNoteBook(assessmentYear, tan, tenantId, userEmail, month, batchUpload,
					"", "");
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

								List<ActivityTracker> listTracker = activityTrackerDAO
										.getActivityTrackerByTanYearTypeAndMonth(tan, assessmentYear, "TDS Calculation",
												month);
								if (!listTracker.isEmpty()) {
									listTracker.get(0).setStatus(ActivityTrackerStatus.VALIDATED.name());
									listTracker.get(0).setModifiedBy(userEmail);
									listTracker.get(0).setModifiedDate(new Date());
									activityTrackerDAO.update(listTracker.get(0));
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
		return batchUpload;
	}

	private BatchUpload saveBatchUpload(int assessmentYear, String tan, String type, String userEmail,
			MultipartFile file, String path, int month, String sha256, List<BatchUpload> batch, boolean isDuplicate,
			boolean isJavaProcess, String csvPath) {
		BatchUpload batchUpload;
		batchUpload = new BatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(tan);
		batchUpload.setUploadType(type);
		batchUpload.setProcessStartTime(new Timestamp(System.currentTimeMillis()));
		if (isDuplicate) {
			logger.info("Duplicate Record inserting : {}", file.getOriginalFilename());
			batchUpload.setStatus("Duplicate");
			batchUpload.setSha256sum(sha256);
			batchUpload.setReferenceId(batch.get(0).getBatchUploadID());

		} else {
			logger.info("Unique record  inserting  : {}", file.getOriginalFilename());
			batchUpload.setStatus("Uploaded");
			batchUpload.setSha256sum(sha256);
		}
		batchUpload.setAssessmentMonth(month);
		batchUpload.setFileName(file.getOriginalFilename());
		if (StringUtils.isBlank(csvPath)) {
			batchUpload.setFilePath(path);
		} else {
			batchUpload.setFilePath(csvPath);
		}
		batchUpload.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		batchUpload.setProcessStartTime(new Timestamp(System.currentTimeMillis()));
		batchUpload.setCreatedBy(userEmail);
		batchUpload.setActive(true);
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessedCount(0);
		batchUpload.setDuplicateCount(0L);
		batchUpload.setMismatchCount(0L);
		if (isJavaProcess) {
			batchUpload.setProcessStartTime(new Timestamp(System.currentTimeMillis()));
		}
		batchUpload.setSourceUrl(path);
		batchUpload = batchUploadDAO.save(batchUpload);
		return batchUpload;
	}

	public NoteBookParam createNoteBook(int assessmentYear, String tan, String tenantId, String userEmail, int month,
			BatchUpload batchUpload, String dueDate, String challanMonth) {
		NoteBookParam noteBookParam = new NoteBookParam();
		noteBookParam.setAssessmentMonth(month);
		noteBookParam.setAssessmentYear(assessmentYear);
		if (batchUpload != null) {
			noteBookParam.setId(batchUpload.getBatchUploadID());
			noteBookParam.setStatus(batchUpload.getStatus());
			noteBookParam.setType(batchUpload.getUploadType());
			if (StringUtils.isNotBlank(batchUpload.getSha256sum())) {
				noteBookParam.setSha256(batchUpload.getSha256sum());
			} else {
				noteBookParam.setSha256("");
			}
			if (StringUtils.isNotBlank(batchUpload.getFilePath())) {
				// extracting the file name from file url
				noteBookParam.setFileName(
						batchUpload.getFilePath().substring(batchUpload.getFilePath().lastIndexOf("/") + 1));
			} else if (StringUtils.isNotBlank(batchUpload.getFileName())) {
				noteBookParam.setFileName(batchUpload.getFileName());
			} else {
				noteBookParam.setFileName("");
			}
		}
		noteBookParam.setChallanDueDate(dueDate);
		if (StringUtils.isNotBlank(challanMonth)) {
			noteBookParam.setChallanMonth(Integer.valueOf(challanMonth));
		} else {
			noteBookParam.setChallanMonth(0);
		}
		noteBookParam.setTenantId(tenantId);
		noteBookParam.setTan(tan);
		if (noteBookParam.getIsMismatch() == null) {
			noteBookParam.setIsMismatch(false);
		}
		noteBookParam.setApplicationURL("");
		// These two are added for Drools and also UserObject.
		noteBookParam.setUserEmail(userEmail);
		return noteBookParam;
	}

	public String triggerSparkNotebook(String api, int jobId, NoteBookParam noteBookParam, int month,
			int assessmentYear, String tenantId, String tan, String userEmail) throws JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		// Made the Databricks Key Dynamic
		headers.add("Authorization", "Bearer " + this.dataBricksKey);
		headers.add("Content-Type", "application/json");
		RestTemplate restTemplate = new RestTemplate();
		Job job = new Job();

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
		HttpEntity<Job> entity = new HttpEntity<>(job, headers);
		String response = restTemplate.postForObject(dataBricks, entity, String.class);

		logger.info("Response : {}", response);
		return response;
	}

	public void triggerSparkNotebook(String api, int jobId, NoteBookParam noteBookParam, String tenantId)
			throws JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		// Made the Databricks Key Dynamic
		headers.add("Authorization", "Bearer " + this.dataBricksKey);
		headers.add("Content-Type", "application/json");
		RestTemplate restTemplate = new RestTemplate();
		Job job = new Job();

		job.setJob_id(jobId);
		job.setNotebook_params(noteBookParam);

		String dataBricks = api;
		HttpEntity<String> entity = new HttpEntity<>(job.toString(), headers);
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
	public List<BatchUploadGroupCustomDTO> getBatchDataByGroupId(String tan, Integer groupId, Pagination pagination) {
		List<BatchUpload> getBatchRecordsBasedOnTanGroupID = batchUploadDAO.getBatchListBasedOnTanAndGroupId(tan,
				groupId);

		List<BatchUploadGroupCustomDTO> listOfBatchRecords = new ArrayList<BatchUploadGroupCustomDTO>();
		for (BatchUpload batchUpload : getBatchRecordsBasedOnTanGroupID) {
			BatchUploadGroupCustomDTO batchDTO = new BatchUploadGroupCustomDTO();
			batchDTO.setBatchId(batchUpload.getBatchUploadID());
			batchDTO.setFileName(batchUpload.getFileName());
			batchDTO.setStatus(batchUpload.getStatus());
			batchDTO.setFilePath(batchUpload.getFilePath());
			batchDTO.setAssessmentYear(batchUpload.getAssessmentYear());
			listOfBatchRecords.add(batchDTO);
		}
		return listOfBatchRecords;
	}

	public BatchUpload uploadFilesToFileShare(BatchUpload batchUpload, String tenantId, String pan, String tan)
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
				.getFileReference(batchUpload.getBatchUploadID() + "_" + tenantId + "_" + batchUpload.getFileName());
		cloudFile.uploadFromFile(file.getAbsolutePath());

		return batchUpload;
	}

	public BatchUpload downloadFilesFromFileShare(UUID batchId, String tenantId, String fileName)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		BatchUpload batchUpload = new BatchUpload();
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

	public BatchUpload uploadInvoicePdfBlobUrlToBatch(SftpPathDTO sftpPathDTO, String tenantId)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		logger.info("Blob URL :  {}", sftpPathDTO.getSrcPath());
		Integer batchId = null;
		String errorCode = null;
		String type = UploadTypes.INVOICE_PDF.name();

		// convert url to file
		String[] strs = sftpPathDTO.getSrcPath().replace("\\", "/").split("/");
		String fileName = strs[strs.length - 1];
		String[] names = fileName.split("_");
		if (FilenameUtils.getExtension(fileName).equalsIgnoreCase("csv")) {
			batchId = Integer.valueOf(names[0]);
		} else {
			errorCode = names[0];
			batchId = Integer.valueOf(names[1]);
		}
		if (StringUtils.isNotBlank(tenantId)) {
			MultiTenantContext.setTenantId(tenantId);
		} else {
			throw new CustomException("There is no Tenant ID in file Name");
		}

		FileShareConfig fileShareConfig = tenantProperties.getConfiguration(tenantId).getFileShareConfig();

		TenantConfiguration.BlobStorage blobStorage = tenantProperties.getConfiguration(tenantId).getBlobStorage();
		String blobURL = blobStorage.getProtocol() + "://" + blobStorage.getAccountName() + ".blob.core.windows.net/"
				+ blobStorage.getContainer() + "/" + fileShareConfig.getOutputPath() + "/" + fileName;

		List<BatchUpload> batchOptional = batchUploadDAO.getBatchUploadByTypeAndId(type, batchId);

		logger.info("Updating batch upload received with csv url--{}", blobURL);
		BatchUpload batchUpload = new BatchUpload();
		if (!batchOptional.isEmpty()) {
			logger.warn("Requested batch upload entry is found in system for type {} and batch id {}", type, batchId);
			// batch upload
			batchUpload = batchOptional.get(0);

			List<BatchUploadGroup> listGroupBatch = batchUploadGroupDAO.findById(batchUpload.getAssessmentYear(),
					batchUpload.getDeductorMasterTan(), batchUpload.getBatchUploadGroupId());

			if (!listGroupBatch.isEmpty()) {
				BatchUploadGroup batchUploadGroup = listGroupBatch.get(0);
				batchUpload.setFileName(batchOptional.get(0).getFileName());
				batchUpload.setSuccessFileUrl(blobURL);

				if (StringUtils.isNotBlank(errorCode)) {
					if ("INV".equalsIgnoreCase(errorCode)) {
						batchUpload.setStatus("Invalid");
					} else if ("UNC".equalsIgnoreCase(errorCode)) {
						batchUpload.setStatus("Unclassified");
					} else if ("NP".equalsIgnoreCase(errorCode)) {
						batchUpload.setStatus("Not processed");
					}
					batchUploadGroup.setGroupTotalErrorCount(batchUploadGroup.getGroupTotalErrorCount() + 1);
					batchUpload.setFailedCount(1L);
				} else {
					batchUploadGroup.setGroupTotalProcessedCount(batchUploadGroup.getGroupTotalProcessedCount() + 1);
					batchUpload.setStatus("Processed");
					batchUpload.setProcessedCount(1);
				}
				logger.info("File status {}", batchUpload.getStatus());
				batchUpload.setSourceFilePath(fileShareConfig.getOutputPath() + "/" + fileName);
				batchUpload.setSha256sum(batchOptional.get(0).getSha256sum());
				batchUpload.setReferenceId(batchOptional.get(0).getBatchUploadID());
				batchUpload.setAssessmentMonth(batchOptional.get(0).getAssessmentMonth());
				batchUpload.setAssessmentMonth(batchOptional.get(0).getAssessmentMonth());
				batchUpload.setFilePath(batchOptional.get(0).getFilePath());
				batchUpload.setCreatedDate(batchOptional.get(0).getCreatedDate());
				batchUpload.setCreatedBy(batchOptional.get(0).getCreatedBy());
				batchUpload.setActive(true);
				batchUpload.setSuccessCount(1L);
				batchUpload.setRowsCount(1L);
				batchUpload.setBatchUploadGroupId(batchOptional.get(0).getBatchUploadGroupId());
				batchUploadDAO.update(batchUpload);
				// save batch group data
				batchUploadGroupDAO.update(batchUploadGroup);
			}
		} else {
			logger.warn("Requested batch upload entry is not found in system for type {} and batch id {}", type,
					batchId);
		}
		logger.info("Batch upload record updated sucessfully");

		return batchUpload;
	}

	public BatchUpload uploadBlobUrlToBatch(int assessmentYear, MultipartFile file, String tan, String type,
			String tenantId, String userEmail, String blobUrl, String deductorPan, String filePath)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		if (logger.isDebugEnabled()) {
			logger.debug("REST request for file upload Excel : {}, {}", file, tan);
		}
		String path = StringUtils.EMPTY;
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		BatchUpload batchUpload = null;
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else {
			logger.info(file.getOriginalFilename());
			String sha256 = sha256SumService.getSHA256Hash(file);
			path = blob.uploadExcelToBlob(file, tenantId);
			logger.info("path is: {}", path);
			batchUpload = new BatchUpload();
			logger.info("Unique record  inserting  : {}", file.getOriginalFilename());
			logger.info("Unique file : {}", file.getOriginalFilename());
			List<BatchUpload> batcList = batchUploadDAO.getSha256Records(sha256SumService.getSHA256Hash(file));
			batchUpload.setAssessmentYear(assessmentYear);
			batchUpload.setDeductorMasterTan(tan);
			batchUpload.setUploadType(type);
			if (!batcList.isEmpty()) {
				logger.info("Duplicate Record inserting : {}", file.getOriginalFilename());
				batchUpload.setStatus("Duplicate");
				batchUpload.setSha256sum(sha256);
				batchUpload.setReferenceId(batcList.get(0).getBatchUploadID());

			} else {
				logger.info("Unique record  inserting  : {}", file.getOriginalFilename());
				if ("DEDUCTEE_SAP".equalsIgnoreCase(type) || "DEDUCTEE_EXCEL".equalsIgnoreCase(type)) {
					batchUpload.setStatus("Processing");
				} else {
					batchUpload.setStatus("Uploaded");
				}
				batchUpload.setSha256sum(sha256);
			}
			batchUpload.setAssessmentMonth(month);
			// for SAP file name should include file path. Don't change the following two
			// lines.
			batchUpload.setFileName(filePath);
			batchUpload.setSourceFilePath(filePath);
			batchUpload.setFilePath(blobUrl);
			batchUpload.setAssessmentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userEmail);
			batchUpload.setActive(true);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setDuplicateCount(0L);
			batchUpload.setMismatchCount(0L);
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setFilePath(path);
			batchUpload = batchUploadDAO.save(batchUpload);
			boolean isUnique = batchUpload.getStatus().equalsIgnoreCase("Uploaded");
			logger.info("SAP Notebook type : {}", type);

			NoteBookParam noteBookParam = createNoteBook(assessmentYear, tan, tenantId, userEmail, month, batchUpload,
					"", "");
			String fileType = convertType(type);
			if (!batchUpload.getStatus().equalsIgnoreCase("Duplicate")
					&& ("DEDUCTEE_SAP".equalsIgnoreCase(type) || "DEDUCTEE_EXCEL".equalsIgnoreCase(type))) {
				ResponseEntity<ApiStatus<BatchUpload>> batchData = onboardingClient.readImportedCsvData(file, tan,
						deductorPan, userEmail, assessmentYear, Calendar.getInstance().get(Calendar.MONTH) + 1,
						tenantId, batchUpload.getBatchUploadID());
				batchUpload = batchData.getBody().getData();
			} else if (isUnique
					&& (!"DEDUCTEE_SAP".equalsIgnoreCase(type) && !"DEDUCTEE_EXCEL".equalsIgnoreCase(type))) {
				SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get(fileType.toLowerCase());
				logger.info("Notebook url : {}", notebook.getUrl());
				String sparkResponse = triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam,
						month, assessmentYear, tenantId, tan, userEmail);
				SparkResponseDTO sparkResponseDTO = new ObjectMapper().readValue(sparkResponse, SparkResponseDTO.class);
				batchUpload = updateBatchUpload(Integer.valueOf(sparkResponseDTO.getRun_id()), batchUpload);
			}
		}
		return batchUpload;
	}

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
		List<BatchUpload> batchList = batchUploadDAO.getBatchList(year, tan, type);
		if (batchList != null && !batchList.isEmpty()) {
			Collections.sort(batchList, new Comparator<BatchUpload>() {
				@Override
				public int compare(BatchUpload id1, BatchUpload id2) {
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
			for (BatchUpload batch : batchList) {
				// Call Feign for getting ReceiptData
				logger.info("INTEGER : {}", batch.getBatchUploadID());
				ResponseEntity<ApiStatus<ReceiptDataDTO>> receiptData = challanClient
						.getReceiptDataBasedOnBatchId(tenantId, batch.getBatchUploadID());
				receipt = receiptData.getBody().getData();

				ReceiptBatchUploadDTO receiptbatchData = new ReceiptBatchUploadDTO();
				BatchUpload batchUpload = batch;
				receiptbatchData.setId(batchUpload.getBatchUploadID());
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
				if (batchUpload.getProcessedCount() != null) {
					receiptbatchData.setProcessedRecords(batchUpload.getProcessedCount());
				} else {
					receiptbatchData.setProcessedRecords(0L);
				}
				receiptbatchData.setErrorFileDownloadUrl(batchUpload.getErrorFilePath());
				receiptbatchData.setYear(batchUpload.getAssessmentYear());
				batchUpload.setAssessmentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
				receiptbatchData.setMonth(batchUpload.getAssessmentMonth());
				receiptbatchData.setTan(batchUpload.getDeductorMasterTan());
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

	public BatchUpload batchProcess(String userEmail, String tan, String tenantId, BatchProcessDTO batchProcessDTO)
			throws JsonProcessingException, ParseException {
		String jobType = "BATCHPROCESS";
		String dueDate = "";
		String challanMonth = "";
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setAssessmentYear(batchProcessDTO.getYear());
		batchUpload.setDeductorMasterTan(tan);
		batchUpload.setUploadType(batchProcessDTO.getType());
		batchUpload.setStatus("Uploaded");
		batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
		if ("NR_BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchProcessDTO.getType())
				|| "BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchProcessDTO.getType())) {
			batchUpload.setAssessmentMonth(batchProcessDTO.getMonth());
		} else {
			batchUpload.setAssessmentMonth(month);
		}
		batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
		batchUpload.setCreatedBy(userEmail);
		batchUpload.setActive(true);
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessedCount(0);
		batchUpload.setDuplicateCount(0L);
		batchUpload.setMismatchCount(0L);
		if ("BATCHPROCESS_INTEREST".equalsIgnoreCase(batchProcessDTO.getType())) {
			jobType = "INTEREST";
			if (batchProcessDTO.getDueDate() != null) {
				dueDate = new SimpleDateFormat("dd-MM-yyyy").format(batchProcessDTO.getDueDate());
				challanMonth = String.valueOf(batchProcessDTO.getMonth());
			}
			batchUpload.setFileName("interest_calculation" + new Date());
		} else if ("BATCHPROCESS_GLRECON".equalsIgnoreCase(batchProcessDTO.getType())) {
			jobType = "GL_RECONCILATION";
			if (StringUtils.isNotBlank(batchProcessDTO.getQuarter()) && batchProcessDTO.getMonth() > 0) {
				batchUpload.setFileName("GLRECONCILATION" + "-" + batchProcessDTO.getYear() + "-"
						+ batchProcessDTO.getQuarter() + "-" + batchProcessDTO.getMonth());
			} else if (StringUtils.isNotBlank(batchProcessDTO.getQuarter())) {
				batchUpload.setFileName(
						"GLRECONCILATION" + "-" + batchProcessDTO.getYear() + "-" + batchProcessDTO.getQuarter());
			} else {
				batchUpload.setFileName("GLRECONCILATION" + "-" + batchProcessDTO.getYear());
			}
		} else if ("BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchProcessDTO.getType())) {
			jobType = "TDS_LIABILITY_RECONCILATION";
			batchUpload.setFileName(
					"LIABILITY_RECONCILATION" + "_" + batchProcessDTO.getYear() + "_" + batchProcessDTO.getMonth());
		} else if ("NR_BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchProcessDTO.getType())) {
			jobType = "TDS_LIABILITY_RECONCILATION";
			batchUpload.setFileName(
					"NR_LIABILITY_RECONCILATION" + "_" + batchProcessDTO.getYear() + "_" + batchProcessDTO.getMonth());
		}
		batchUploadDAO.save(batchUpload);
		if ("BATCHPROCESS_GLRECON".equalsIgnoreCase(batchProcessDTO.getType())) {
			batchUpload.setUploadType(batchProcessDTO.getUploadType());
		}
		SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get(jobType.toLowerCase());
		logger.info("Notebook 0 url : {} with jobId : {}", notebook.getUrl(), notebook.getJobId());

		NoteBookParam noteBookParam = createNoteBook(batchProcessDTO.getYear(), tan, tenantId, userEmail, month,
				batchUpload, dueDate, challanMonth);
		if ("BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchProcessDTO.getType())) {
			noteBookParam.setResidentialStatus("RES");
		} else if ("NR_BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchProcessDTO.getType())) {
			noteBookParam.setResidentialStatus("NR");
		}
		triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam, month, batchProcessDTO.getYear(),
				tenantId, tan, userEmail);
		return batchUpload;
	}

	/**
	 *
	 * @param tan
	 * @param tenantId
	 * @param deductorPan
	 * @param year
	 * @param month
	 * @return
	 * @throws IOException
	 */
	public ByteArrayInputStream exportNOPKeywords(String tan, String tenantId, String deductorPan, int year, int month)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			XSSFSheet sheet = wb.createSheet("NOP Keywords File");
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);
			
			// creating the header row and applying style to the header
			XSSFRow row = sheet.createRow(0);
			sheet.setDefaultColumnWidth(25);
			sheet.setAutoFilter(CellRangeAddress.valueOf("A1:C1"));
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

			// Black colour
			style0.setFillForegroundColor(new XSSFColor(new java.awt.Color(46, 134, 193), defaultIndexedColorMap));
			style0.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			// creating cells for the header
			row.createCell(0).setCellValue("NatureOfPayment");
			row.getCell(0).setCellStyle(style0);
			row.createCell(1).setCellValue("Keywords");
			row.getCell(1).setCellStyle(style0);
			row.createCell(2).setCellValue("IsResident");
			row.getCell(2).setCellStyle(style0);

			CellStyle unlockedCellStyle = wb.createCellStyle();
			unlockedCellStyle.setLocked(false); // true or false based on the cell.
			
			// sheet2
			XSSFSheet sheet1 = wb.createSheet("Nature Of Payment");
			sheet1.lockAutoFilter(false);
			sheet1.lockSort(false);
			// protection enable
			sheet1.protectSheet("password");

			// creating the header row and applying style to the header
			XSSFRow row1 = sheet1.createRow(0);
			// sheet1.setDefaultColumnWidth(45);
			XSSFCellStyle style1 = wb.createCellStyle();

			style1.setBorderTop(BorderStyle.MEDIUM);
			style1.setBorderBottom(BorderStyle.MEDIUM);
			style1.setBorderRight(BorderStyle.MEDIUM);
			style1.setAlignment(HorizontalAlignment.CENTER);
			style1.setVerticalAlignment(VerticalAlignment.CENTER);

			Font font1 = wb.createFont();
			font1.setBold(true);
			font1.setFontName("Arial");
			style1.setFont(font1);

			// Black colour
			style1.setFillForegroundColor(new XSSFColor(new java.awt.Color(46, 134, 193), defaultIndexedColorMap));
			style1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			// feign client for get all nop
			List<NatureOfPaymentMasterDTO> response = masterClient.findAll().getBody().getData();
			logger.info("nop response size is :{}", response.size());
			// creating cells for the header
			row1.createCell(0).setCellValue("NOP ID");
			row1.getCell(0).setCellStyle(style1);
			row1.createCell(1).setCellValue("NATURE OF PAYMENT");
			row1.getCell(1).setCellStyle(style1);

			int rowNumber = 1;
			if (!response.isEmpty()) {
				for (NatureOfPaymentMasterDTO nop : response) {
					String nopAndSection = nop.getNature() + " - " + nop.getSection();
					XSSFRow row2 = sheet1.createRow(rowNumber);
					row2.createCell(0).setCellValue(nop.getId());
					row2.getCell(0).setCellStyle(unlockedCellStyle);
					row2.createCell(1).setCellValue(nopAndSection);
					rowNumber++;
				}
			}
			// protection enable
			sheet1.protectSheet("password");
			sheet1.autoSizeColumn(1);
			sheet1.setAutoFilter(CellRangeAddress.valueOf("A1:B1"));
			// hiding the column from the sheet
			sheet1.setColumnHidden(0, true);
			wb.write(out);
		}
		return new ByteArrayInputStream(out.toByteArray());

	}

	public BatchUpload saveUploadExcelForReconciliation(int assessmentYear, int assessmentMonth, MultipartFile file,
			String tan, String tenantId, String userEmail, String token, String fileType)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, InvalidFormatException {

		if (logger.isDebugEnabled()) {
			logger.debug("REST request for file : {} upload Reconcilitation Excel : {}, {}", fileType, file, tan);
		}

		String path;

		logger.info("FILE NAME: {}", file.getOriginalFilename());

		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx") && !FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("csv")) {
			throw new FileStorageException("Please select the xlsx or csv file only");
		} else {
			logger.info(file.getOriginalFilename());
			String sha256Key = sha256SumService.getSHA256Hash(file);

			List<BatchUpload> batch = batchUploadDAO
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
//				batch.forEach(_b -> this.batchUploadDAO.delete(_b));
//			}

			logger.info("Unique record  inserting  : {}", file.getOriginalFilename());

			// TODO : Need to change to work with multiple assessmentMonths.
			BatchUpload batchUpload = new BatchUpload();
			batchUpload.setAssessmentYear(assessmentYear);
			batchUpload.setDeductorMasterTan(tan);
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
			batchUpload.setProcessedCount(0);
			batchUpload.setDuplicateCount(0L);
			batchUpload.setMismatchCount(0L);
			batchUpload.setProcessStartTime(new Date());
			batchUpload = batchUploadDAO.save(batchUpload);
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
		List<BatchUpload> batchUploads = batchUploadDAO.getBatchList(assessmentYear, Arrays.asList(tan),
				"reconciliation");

		logger.info("Notebook type---: reconciliation");
		SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks()
				.get(ReconciliationTriggerRequest.KEY.UPLOAD_KEY.getFileKey());
		logger.info("Notebook url : {} ", notebook.getUrl());
		ReconciliationTriggerRequest reconciliationFiles = new ReconciliationTriggerRequest().build(
				new PagedData<>(batchUploads, 0, new ArrayList<String>()), assessmentYear, tan,
				ReconciliationTriggerRequest.KEY.PROCESSED_KEY, tenantId);
		return triggerReconciliationSparkNotebook(notebook.getUrl(), notebook.getJobId(), reconciliationFiles);
	}

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

	public String triggerPowerBiSparkNoteBook(String tenantId, String tan, String userEmail)
			throws JsonProcessingException {

		SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get("power_Bi");
		logger.info("Notebook 0 url : {} with jobId : {}", notebook.getUrl(), notebook.getJobId());
		BatchUpload batchUpload = new BatchUpload();
		int year = CommonUtil.getAssessmentYear(null);
		batchUpload.setAssessmentYear(year);
		batchUpload.setDeductorMasterTan(tan);
		batchUpload.setUploadType("SYNC_DB");
		batchUpload.setStatus("Uploaded");
		batchUpload.setProcessStartTime(new Date());
		int month = CommonUtil.getAssessmentMonth(null);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setCreatedDate(new Date());
		batchUpload.setCreatedBy("");
		batchUpload.setActive(true);
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessedCount(0);
		batchUpload.setDuplicateCount(0L);
		batchUpload.setMismatchCount(0L);
		batchUpload.setFileName("interest_calculation" + new Date());
		NoteBookParam noteBookParam = createNoteBook(year, tan, tenantId, userEmail, month, batchUpload, "", "");
		triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam, tenantId);
		batchUploadDAO.save(batchUpload);
		return "SUCCESS";
	}

	public String uploadFileTOBlob(MultipartFile file, String tenantId)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		String blobUrl = blob.uploadExcelToBlob(file, tenantId);
		return blobUrl;
	}

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

	public BatchUploadResponseDTO copyToResponseDTO(BatchUpload dto) {
		BatchUploadResponseDTO response = new BatchUploadResponseDTO();
		// BatchUploadResponseDTO.Key key = new
		// BatchUploadResponseDTO.Key(dto.getAssessmentYear(),
		// dto.getDeductorMasterTan(), dto.getUploadType(), dto.getBatchUploadID());
		response.setActive(dto.getActive());
		response.setAssessmentMonth(dto.getAssessmentMonth());
		response.setBalanceAmount(dto.getLdcMasterBalanceAmount());
		response.setBatchUploadGroupId(dto.getBatchUploadGroupId());
		response.setCreatedBy(dto.getCreatedBy());
		response.setCreatedDate(dto.getCreatedDate());
		response.setDuplicateCount(dto.getDuplicateCount());
		response.setErrorFilePath(dto.getErrorFilePath());
		response.setFailedCount(dto.getFailedCount());
		response.setFileName(dto.getFileName());
		response.setFilePath(dto.getFilePath());
		response.setMismatchCount(dto.getMismatchCount());
		response.setModifiedBy(dto.getModifiedBy());
		response.setModifiedDate(dto.getModifiedDate());
		response.setNewStatus(dto.getNewStatus());
		response.setOtherFileCreatedDate(dto.getOtherFileCreatedDate());
		response.setOtherFileUrl(dto.getOtherFileUrl());
		response.setProcessedCount(dto.getProcessedCount());
		response.setProcessEndTime(dto.getProcessEndTime());
		response.setProcessStartTime(dto.getProcessStartTime());
		response.setReferenceId(dto.getReferenceId());
		response.setRowsCount(dto.getRowsCount());
		response.setSha256sum(dto.getSha256sum());
		response.setSourceFilePath(dto.getSourceFilePath());
		response.setSourceIdentifier(dto.getSourceIdentifier());
		response.setStatus(dto.getStatus());
		response.setSuccessCount(dto.getSuccessCount());
		response.setSuccessFileCreatedDate(dto.getSuccessFileCreatedDate());
		response.setSuccessFileUrl(dto.getSuccessFileUrl());

		return response;
	}

	/**
	 * returns batch upload record
	 *
	 * @param assessmentYear
	 * @param deductorTan
	 * @param type
	 * @param id
	 * @return
	 */
	public TCSBatchUpload getTCSBatchUpload(int assessmentYear, String deductorTan, String type, Integer id) {
		List<TCSBatchUpload> batchUploadResponse = tCSBatchUploadDAO.findById(assessmentYear, deductorTan, type, id);
		if (!batchUploadResponse.isEmpty() && batchUploadResponse != null) {
			return batchUploadResponse.get(0);
		} else {
			throw new CustomException("Did not find a BatchUpload with the passed in criteria", HttpStatus.BAD_REQUEST);
		}
	}

	public TCSBatchUpload createTCSBatchUpload(TCSBatchUpload batchUpload) {
		return tCSBatchUploadDAO.save(batchUpload);
	}

	public TCSBatchUpload updateTCSBatchUpload(TCSBatchUpload batchUpload) {
		return tCSBatchUploadDAO.update(batchUpload);
	}

	public List<BatchUpload> getBatchUploadByTypeAndId(String type, Integer batchId) {
		return batchUploadDAO.getBatchUploadByTypeAndId(type, batchId);
	}

	private BatchUpload updateBatchUpload(Integer runId, BatchUpload batchUpload) {
		batchUpload.setRunId(runId);
		batchUpload = batchUploadDAO.update(batchUpload);
		return batchUpload;
	}

	/**
	 * to get job status
	 *
	 * @param runId
	 * @return
	 * @throws JsonProcessingException
	 */
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

	public void asyncProcessInterestComputationFile(String tenantId, String userName, String deductorTan,
			String deductorPan, Integer year, Integer month, MultipartFile file, String uploadType)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Async method executing to process the interest report {}");
		BatchUpload batchUplaod = null;
		batchUplaod = saveOrUpdateBatchUpload(file, userName, deductorTan, year, "Processing", null, uploadType, 0,
				month);
		if (uploadType.equals("INVOICE_COMPUTATION_UPLOAD")) {
			invoiceLineItemService.asyncProcessInvoiceComputationFile(tenantId, userName, deductorTan, deductorPan,
					year, month, file, uploadType, batchUplaod);

		} else if (uploadType.equals("ADVANCE_COMPUTATION_UPLOAD")) {
			advanceService.asyncProcessAdvanceComputationFile(tenantId, userName, deductorTan, deductorPan, year, month,
					uploadType, batchUplaod);
		} else if (uploadType.equals("PROVISION_COMPUTATION_UPLOAD")) {
			provisionMismatchService.asyncProcesspProvisionComputationFile(tenantId, userName, deductorTan, deductorPan,
					year, month, uploadType, batchUplaod);
		}
	}

	public BatchUpload saveOrUpdateBatchUpload(MultipartFile file, String userName, String tan, Integer year,
			String status, Integer id, String uploadType, Integer successCount, Integer month)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		String filePath = "";
		String filename = "";
		BatchUpload batchUpload = null;
		if (file != null) {
			filename = file.getOriginalFilename();
			filePath = blob.uploadExcelToBlob(file);

		}
		batchUpload = new BatchUpload();
		batchUpload.setStatus(status);
		batchUpload.setDeductorMasterTan(tan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setActive(true);
		batchUpload.setFileName(filename);
		batchUpload.setFilePath(filePath);
		batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
		batchUpload.setCreatedBy(userName);
		batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
		batchUpload.setAssessmentMonth(month);
		batchUpload.setAssessmentYear(year);
		batchUploadDAO.save(batchUpload);
		logger.info("Invoice Interest computation report upload updated in batch upload successfully{}");

		return batchUpload;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param tenantId
	 * @param pan
	 * @param challanMonth
	 * @param userName
	 * @param type
	 * @return
	 * @throws JsonProcessingException
	 * @throws ParseException
	 */
	public List<BatchUpload> liabilityCaluclation(Integer assessmentYear, String deductorTan, String tenantId,
			String pan, Integer challanMonth, String userName, String type)
			throws JsonProcessingException, ParseException {
		MultiTenantContext.setTenantId(tenantId);

		String status = "Processed";
		String batchType = UploadTypes.BATCHPROCESS_LIABILITYRECON.name();
		BatchUpload batchUpload = new BatchUpload();
		String reverseLiability = "REVERSE_LIABILITY_" + assessmentYear + "_" + challanMonth;
		String can = "LIABILITY_CAN_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String cr = "LIABILITY_CR_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String advance = "LIABILITY_ADVANCE_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String provision = "LIABILITY_PROVISION_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String ldc = "LIABILITY_LDC_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String threshold = "LIABILITY_RECONCILATION_" + assessmentYear + "_" + challanMonth;

		List<String> fileNames = new ArrayList<>();
		fileNames.add(can);
		fileNames.add(cr);
		fileNames.add(advance);
		fileNames.add(provision);
		fileNames.add(ldc);
		fileNames.add(threshold);
		fileNames.add(reverseLiability);
		if (type.equalsIgnoreCase("REVERSE_LIABILITY")) {
			List<BatchUpload> batchUploadAllList = batchUploadDAO.getByFileNames(assessmentYear, deductorTan, fileNames,
					challanMonth);
			if (batchUploadAllList.size() == 7) {
				// inactivating old records
				inactivatingLiabilityRecords(assessmentYear, deductorTan, tenantId, pan, challanMonth, userName,
						batchUploadAllList, batchType);
			}

			List<BatchUpload> batchUploads = batchUploadDAO.getByFileName(assessmentYear, deductorTan, reverseLiability,
					challanMonth);
			if (batchUploads.isEmpty()) {
				batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setModifiedDate(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setFileName(reverseLiability);
				batchUpload.setCreatedBy(userName);
				batchUpload.setModifiedBy(userName);
				batchUpload.setRowsCount(0L);
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setStatus("Processing");
				batchUpload = liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName,
						tenantId, false, status, batchType);

				try {
					invoiceLineItemDAO.USPReverseLiabilityCaluclation(assessmentYear, challanMonth, deductorTan, pan);
				} catch (Exception e) {
					status = "Failed";
					logger.error("Error ocuured while running USPReverseLiabilityCaluclation", e);
				}

				batchUpload = liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName,
						tenantId, false, status, batchType);

			} else {
				batchUpload = batchUploads.get(0);
			}
		} else if (type.equalsIgnoreCase("LIABILITY_CAN_ADJUSTMENT")) {
			batchUpload = liabilityCanProcess(assessmentYear, deductorTan, tenantId, pan, challanMonth, userName,
					batchType);
		} else if (type.equalsIgnoreCase("LIABILITY_CR_ADJUSTMENT")) {
			batchUpload = liabilityCrProcess(assessmentYear, deductorTan, tenantId, pan, challanMonth, userName,
					batchType);
		} else if (type.equalsIgnoreCase("LIABILITY_ADVANCE_ADJUSTMENT")) {
			batchUpload = liabilityAdvanceProcess(assessmentYear, deductorTan, tenantId, pan, challanMonth, userName,
					batchType);
		} else if (type.equalsIgnoreCase("LIABILITY_PROVISION_ADJUSTMENT")) {
			batchUpload = liabilityProvisionProcess(assessmentYear, deductorTan, tenantId, pan, challanMonth, userName,
					batchType);
		} else if (type.equalsIgnoreCase("LIABILITY_LDC_ADJUSTMENT")) {
			batchUpload = liabilityLdcAdjustmentProcess(assessmentYear, deductorTan, tenantId, pan, challanMonth,
					userName, batchType);
		} else if (type.equalsIgnoreCase("LIABILITY_RECONCILATION")) {
			List<BatchUpload> batchUploadList = batchUploadDAO.getByFileName(assessmentYear, deductorTan, threshold,
					challanMonth);
			if (batchUploadList.isEmpty()) {
				BatchProcessDTO batchProcessDTO = new BatchProcessDTO();
				batchProcessDTO.setYear(assessmentYear);
				batchProcessDTO.setMonth(challanMonth);
				batchProcessDTO.setType(batchType);
				batchUpload = batchProcess(userName, deductorTan, tenantId, batchProcessDTO);
			}
		}
		List<BatchUpload> batchUploadAllList = batchUploadDAO.getByFileNames(assessmentYear, deductorTan, fileNames,
				challanMonth);
		return batchUploadAllList;
	}
	
	// NR Liability calculation
	public List<BatchUpload> nrLiabilityCaluclation(Integer assessmentYear, String deductorTan, String tenantId,
			String pan, Integer challanMonth, String userName, String type)
			throws JsonProcessingException, ParseException {
		MultiTenantContext.setTenantId(tenantId);

		String status = "Processed";
		String batchType = "NR_BATCHPROCESS_LIABILITYRECON";
		BatchUpload batchUpload = new BatchUpload();
		String reverseLiability = "NR_REVERSE_LIABILITY_" + assessmentYear + "_" + challanMonth;
		String can = "NR_LIABILITY_CAN_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String cr = "NR_LIABILITY_CR_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String advance = "NR_LIABILITY_ADVANCE_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String provision = "NR_LIABILITY_PROVISION_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String ldc = "NR_LIABILITY_LDC_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		String threshold = "NR_LIABILITY_RECONCILATION_" + assessmentYear + "_" + challanMonth;

		List<String> fileNames = new ArrayList<>();
		fileNames.add(can);
		fileNames.add(cr);
		fileNames.add(advance);
		fileNames.add(provision);
		fileNames.add(ldc);
		fileNames.add(threshold);
		fileNames.add(reverseLiability);
		if (type.equalsIgnoreCase("NR_REVERSE_LIABILITY")) {
			List<BatchUpload> batchUploadAllList = batchUploadDAO.getByFileNames(assessmentYear, deductorTan, fileNames,
					challanMonth);
			if (batchUploadAllList.size() == 7) {
				// inactivating old records
				inactivatingLiabilityRecords(assessmentYear, deductorTan, tenantId, pan, challanMonth, userName,
						batchUploadAllList, batchType);
			}

			List<BatchUpload> batchUploads = batchUploadDAO.getByFileName(assessmentYear, deductorTan, reverseLiability,
					challanMonth);
			if (batchUploads.isEmpty()) {
				batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setModifiedDate(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setFileName(reverseLiability);
				batchUpload.setCreatedBy(userName);
				batchUpload.setModifiedBy(userName);
				batchUpload.setRowsCount(0L);
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setStatus("Processing");
				liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName, tenantId, false,
						status, batchType);

				try {
					invoiceLineItemDAO.nrUSPReverseLiabilityCalculation(assessmentYear, challanMonth, deductorTan, pan);
				} catch (Exception e) {
					status = "Failed";
					logger.error("Error ocuured while running USPReverseLiabilityCaluclation", e);
				}

				liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName, tenantId, false,
						status, batchType);

			} else {
				batchUpload = batchUploads.get(0);
			}
		} else if (type.equalsIgnoreCase("NR_LIABILITY_CAN_ADJUSTMENT")) {
			liabilityCanProcess(assessmentYear, deductorTan, tenantId, pan, challanMonth, userName, batchType);
		} else if (type.equalsIgnoreCase("NR_LIABILITY_CR_ADJUSTMENT")) {
			liabilityCrProcess(assessmentYear, deductorTan, tenantId, pan, challanMonth, userName, batchType);
		} else if (type.equalsIgnoreCase("NR_LIABILITY_ADVANCE_ADJUSTMENT")) {
			liabilityAdvanceProcess(assessmentYear, deductorTan, tenantId, pan, challanMonth, userName, batchType);
		} else if (type.equalsIgnoreCase("NR_LIABILITY_PROVISION_ADJUSTMENT")) {
			liabilityProvisionProcess(assessmentYear, deductorTan, tenantId, pan, challanMonth, userName, batchType);
		} else if (type.equalsIgnoreCase("NR_LIABILITY_LDC_ADJUSTMENT")) {
			liabilityLdcAdjustmentProcess(assessmentYear, deductorTan, tenantId, pan, challanMonth, userName,
					batchType);
		} else if (type.equalsIgnoreCase("NR_LIABILITY_RECONCILATION")) {
			List<BatchUpload> batchUploadList = batchUploadDAO.getByFileName(assessmentYear, deductorTan, threshold,
					challanMonth);
			if (batchUploadList.isEmpty()) {
				BatchProcessDTO batchProcessDTO = new BatchProcessDTO();
				batchProcessDTO.setYear(assessmentYear);
				batchProcessDTO.setMonth(challanMonth);
				batchProcessDTO.setType(batchType);
				batchProcess(userName, deductorTan, tenantId, batchProcessDTO);
			}
		}
		return batchUploadDAO.getByFileNames(assessmentYear, deductorTan, fileNames, challanMonth);
	}

	/**
	 *
	 * @param assessmentYear
	 * @param deductorTan
	 * @param tenantId
	 * @param pan
	 * @param challanMonth
	 * @param userName
	 * @param batchUploadList
	 */
	public void inactivatingLiabilityRecords(Integer assessmentYear, String deductorTan, String tenantId, String pan,
			Integer challanMonth, String userName, List<BatchUpload> batchUploadList, String batchType) {
		MultiTenantContext.setTenantId(tenantId);
		if (!batchUploadList.isEmpty()) {
			for (BatchUpload batchUpload : batchUploadList) {
				liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName, tenantId, true,
						"Processed", batchType);
			}
		}
	}

	/**
	 *
	 * @param batchUpload
	 * @param deductorTan
	 * @param assessmentYear
	 * @param challanMonth
	 * @param userName
	 * @param tenantId
	 * @param isInactivate
	 * @param status
	 * @return
	 */
	private BatchUpload liabilityBatchUpload(BatchUpload batchUpload, String deductorTan, Integer assessmentYear,
			Integer challanMonth, String userName, String tenantId, boolean isInactivate, String status, String type) {
		logger.info("batch: {}", batchUpload);
		MultiTenantContext.setTenantId(tenantId);
		batchUpload.setAssessmentMonth(challanMonth);
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setUploadType(type);
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		if (batchUpload.getBatchUploadID() != null) {
			if (isInactivate) {
				batchUpload.setActive(false);
			} else {
				batchUpload.setBatchUploadID(batchUpload.getBatchUploadID());
				batchUpload.setStatus(status);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			}
			batchUploadDAO.update(batchUpload);
		} else {
			batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	/**
	 *
	 * @param assessmentYear
	 * @param deductorTan
	 * @param tenantId
	 * @param pan
	 * @param challanMonth
	 * @param userName
	 * @return
	 */
	public BatchUpload liabilityCanProcess(Integer assessmentYear, String deductorTan, String tenantId, String pan,
			Integer challanMonth, String userName, String batchType) {
		MultiTenantContext.setTenantId(tenantId);
		String status = "Processed";
		String type = "LIABILITY_CAN_ADJUSTMENT_";
		if ("NR_BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchType)) {
			type = "NR_LIABILITY_CAN_ADJUSTMENT_";
		}
		String fileName = type + assessmentYear + "_" + challanMonth;
		List<BatchUpload> batchUploadList = batchUploadDAO.getByFileName(assessmentYear, deductorTan, fileName,
				challanMonth);
		BatchUpload batchUpload = new BatchUpload();
		if (batchUploadList.isEmpty()) {
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setFileName(fileName);
			batchUpload.setCreatedBy(userName);
			batchUpload.setModifiedBy(userName);
			batchUpload.setRowsCount(0L);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setStatus("Processing");
			batchUpload = liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status, batchType);
			try {
				if ("NR_BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchType)) {
					invoiceLineItemDAO.nrUSPCANAdjustments(assessmentYear, challanMonth, deductorTan, pan);
				} else {
					invoiceLineItemDAO.USPCANAdjustments(assessmentYear, challanMonth, deductorTan, pan);
				}
			} catch (Exception e) {
				status = "Failed";
				logger.error("Error ocuured while running USPCANAdjustments", e);
			}

			batchUpload = liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status, batchType);
		} else {
			batchUpload = batchUploadList.get(0);
		}

		return batchUpload;
	}

	/**
	 *
	 * @param assessmentYear
	 * @param deductorTan
	 * @param tenantId
	 * @param pan
	 * @param challanMonth
	 * @param userName
	 * @return
	 */
	public BatchUpload liabilityCrProcess(Integer assessmentYear, String deductorTan, String tenantId, String pan,
			Integer challanMonth, String userName, String batchType) {

		String status = "Processed";
		MultiTenantContext.setTenantId(tenantId);
		String type = "LIABILITY_CR_ADJUSTMENT_";
		if ("NR_BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchType)) {
			type = "NR_LIABILITY_CR_ADJUSTMENT_";
		}
		String fileName = type + assessmentYear + "_" + challanMonth;
		List<BatchUpload> batchUploadList = batchUploadDAO.getByFileName(assessmentYear, deductorTan, fileName,
				challanMonth);
		BatchUpload batchUpload = new BatchUpload();
		if (batchUploadList.isEmpty()) {
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setFileName(fileName);
			batchUpload.setCreatedBy(userName);
			batchUpload.setModifiedBy(userName);
			batchUpload.setRowsCount(0L);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setStatus("Processing");
			batchUpload = liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status, batchType);
			try {
				if ("NR_BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchType)) {
					invoiceLineItemDAO.nrUSPCRAdjustments(assessmentYear, challanMonth, deductorTan, pan);
				} else {
					invoiceLineItemDAO.USPCRAdjustments(assessmentYear, challanMonth, deductorTan, pan);
				}
			} catch (Exception e) {
				status = "Failed";
				logger.error("Error ocuured while running USPCRAdjustments", e);
			}

			batchUpload = liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status, batchType);
		} else {
			batchUpload = batchUploadList.get(0);
		}
		return batchUpload;
	}

	/**
	 *
	 * @param assessmentYear
	 * @param deductorTan
	 * @param tenantId
	 * @param pan
	 * @param challanMonth
	 * @param userName
	 * @return
	 */
	public BatchUpload liabilityAdvanceProcess(Integer assessmentYear, String deductorTan, String tenantId, String pan,
			Integer challanMonth, String userName, String batchType) {
		MultiTenantContext.setTenantId(tenantId);
		String status = "Processed";
		String type = "LIABILITY_ADVANCE_ADJUSTMENT_";
		if ("NR_BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchType)) {
			type = "NR_LIABILITY_ADVANCE_ADJUSTMENT_";
		}
		String fileName = type + assessmentYear + "_" + challanMonth;
		List<BatchUpload> batchUploadList = batchUploadDAO.getByFileName(assessmentYear, deductorTan, fileName,
				challanMonth);
		BatchUpload batchUpload = new BatchUpload();
		if (batchUploadList.isEmpty()) {
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setFileName(fileName);
			batchUpload.setCreatedBy(userName);
			batchUpload.setModifiedBy(userName);
			batchUpload.setRowsCount(0L);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setStatus("Processing");
			batchUpload = liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status, batchType);

			try {
				if ("NR_BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchType)) {
					invoiceLineItemDAO.nrUSPAdvanceUtilization(assessmentYear, challanMonth, deductorTan, pan);
				} else {
					invoiceLineItemDAO.USPAdvanceUtilization(assessmentYear, challanMonth, deductorTan, pan);
				}
			} catch (Exception e) {
				status = "Failed";
				logger.error("Error ocuured while running USPPaymentUtilization", e);
			}

			batchUpload = liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status, batchType);
		} else {
			batchUpload = batchUploadList.get(0);
		}

		return batchUpload;
	}

	/**
	 *
	 * @param assessmentYear
	 * @param deductorTan
	 * @param tenantId
	 * @param pan
	 * @param challanMonth
	 * @param userName
	 * @return
	 */
	public BatchUpload liabilityProvisionProcess(Integer assessmentYear, String deductorTan, String tenantId,
			String pan, Integer challanMonth, String userName, String batchType) {
		MultiTenantContext.setTenantId(tenantId);
		String status = "Processed";
		String type = "LIABILITY_PROVISION_ADJUSTMENT_";
		if ("NR_BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchType)) {
			type = "NR_LIABILITY_PROVISION_ADJUSTMENT_";
		}
		String fileName = type + assessmentYear + "_" + challanMonth;
		List<BatchUpload> batchUploadList = batchUploadDAO.getByFileName(assessmentYear, deductorTan, fileName,
				challanMonth);
		BatchUpload batchUpload = new BatchUpload();
		if (batchUploadList.isEmpty()) {
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setFileName(fileName);
			batchUpload.setCreatedBy(userName);
			batchUpload.setModifiedBy(userName);
			batchUpload.setRowsCount(0L);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setStatus("Processing");
			batchUpload = liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status, batchType);
			try {
				if ("NR_BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchType)) {
					invoiceLineItemDAO.nrUSPProvisionUtilization(assessmentYear, challanMonth, deductorTan, pan);
				} else {
					invoiceLineItemDAO.USPProvisionUtilization(assessmentYear, challanMonth, deductorTan, pan);
				}
			} catch (Exception e) {
				status = "Failed";
				logger.error("Error ocuured while running USPPaymentUtilization", e);
			}
			batchUpload = liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status, batchType);
		} else {
			batchUpload = batchUploadList.get(0);
		}

		return batchUpload;
	}

	/**
	 *
	 * @param assessmentYear
	 * @param deductorTan
	 * @param tenantId
	 * @param pan
	 * @param challanMonth
	 * @param userName
	 * @return
	 */
	public BatchUpload liabilityLdcAdjustmentProcess(Integer assessmentYear, String deductorTan, String tenantId,
			String pan, Integer challanMonth, String userName, String batchType) {
		String status = "Processed";
		String type = "LIABILITY_LDC_ADJUSTMENT_";
		if ("NR_BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchType)) {
			type = "NR_LIABILITY_LDC_ADJUSTMENT_";
		}
		String fileName = type + assessmentYear + "_" + challanMonth;
		List<BatchUpload> batchUploadList = batchUploadDAO.getByFileName(assessmentYear, deductorTan, fileName,
				challanMonth);
		BatchUpload batchUpload = new BatchUpload();
		if (batchUploadList.isEmpty()) {
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setFileName(fileName);
			batchUpload.setCreatedBy(userName);
			batchUpload.setModifiedBy(userName);
			batchUpload.setRowsCount(0L);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setStatus("Processing");
			batchUpload = liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status, batchType);

			try {
				if ("NR_BATCHPROCESS_LIABILITYRECON".equalsIgnoreCase(batchType)) {
					invoiceLineItemDAO.USPLDCAdjustmentsProcess(assessmentYear, challanMonth, deductorTan, pan);
				} else {
					invoiceLineItemDAO.USPLDCAdjustmentsProcess(assessmentYear, challanMonth, deductorTan, pan);
				}
			} catch (Exception e) {
				status = "Failed";
				logger.error("Error ocuured while running USPLDCInvoiceAdjustments", e);
			}

			batchUpload = liabilityBatchUpload(batchUpload, deductorTan, assessmentYear, challanMonth, userName,
					tenantId, false, status, batchType);
		} else {
			batchUpload = batchUploadList.get(0);
		}

		return batchUpload;
	}

	/**
	 *
	 * @param batchUploadList
	 * @return
	 */
	public List<BatchUploadDTO> setBatchDto(List<BatchUpload> batchUploadList) {
		// adding to DTO
		List<BatchUploadDTO> batchUploadDTOList = new ArrayList<>();
		for (BatchUpload batchUpload : batchUploadList) {
			BatchUploadDTO batchDTO = new BatchUploadDTO();
			batchDTO.setId(batchUpload.getBatchUploadID());
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
			if (batchUpload.getProcessedCount() != null) {
				batchDTO.setProcessedRecords(batchUpload.getProcessedCount());
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
			batchDTO.setTan(batchUpload.getDeductorMasterTan());
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

	public BatchUpload getBatchUpload(Integer batchId) {
		return batchUploadDAO.findByOnlyId(batchId);
	}

	/**
	 * 
	 * @param fileType
	 * @param batchId
	 * @param tan
	 * @param assessmentYear
	 * @param tenantId
	 * @param userName
	 * @param deductorPan
	 * @param token
	 * @return
	 * @throws Exception 
	 */
	public String getBatchUploadDetails(String fileType, int batchId, String tan, Integer assessmentYear,
			String tenantId, String userName, String deductorPan, String token) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		String returnMessage = "LIABLITY_CALCULATED";
		NoteBookParam noteBookParam = null;
		List<BatchUpload> batchData = batchUploadDAO.getBatchListBasedOnTanAndGroupId(tan, batchId);
		List<BatchUpload> batchList = new ArrayList<>();
		String bacthType = getBatchType(fileType);
		if (!batchData.isEmpty()) {
			noteBookParam = createNoteBook(assessmentYear, tan, tenantId, userName,
					batchData.get(0).getAssessmentMonth(), batchData.get(0), "", "");
			if ("INVOICE_EXCEL".equalsIgnoreCase(fileType) || "INVOICE_NR_EXCEL".equalsIgnoreCase(fileType)) {
				List<Integer> invoiceMonths = invoiceLineItemDAO.getInvoiceChallanMonthsBasedOnBatchId(batchId, tan);
				if (!invoiceMonths.isEmpty()) {
					batchList = batchUploadDAO.findBatchRecordsByChallanMonthsTan(tan, invoiceMonths, assessmentYear,
							bacthType);
					if (batchList.isEmpty()) {
						invoiceLineItemDAO.deleteByBatchId(batchId, tan, assessmentYear);
						if (!"INVOICE_NR_EXCEL".equalsIgnoreCase(fileType)) {
							SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks()
									.get(fileType.toLowerCase());
							logger.info("Notebook url : {}", notebook.getUrl());
							triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam,
									batchData.get(0).getAssessmentMonth(), assessmentYear, tenantId, tan, userName);
						}
						returnMessage = "RETRIGGERED";
					}
				}
			} else if ("PROVISIONS".equalsIgnoreCase(fileType) || "PROVISION_NR_EXCEL".equalsIgnoreCase(fileType)) {
				List<Integer> provisionMonths = invoiceLineItemDAO.getProvisionMonthsByBatchId(batchId, tan);
				if (!provisionMonths.isEmpty()) {
					batchList = batchUploadDAO.findBatchRecordsByChallanMonthsTan(tan, provisionMonths, assessmentYear,
							bacthType);
					if (batchList.isEmpty()) {
						invoiceLineItemDAO.deleteProvisionBasedOnBatchId(batchId, tan, assessmentYear);
						if (!"PROVISION_NR_EXCEL".equalsIgnoreCase(fileType)) {
							SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks()
									.get(fileType.toLowerCase());
							logger.info("Notebook url : {}", notebook.getUrl());
							triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam,
									batchData.get(0).getAssessmentMonth(), assessmentYear, tenantId, tan, userName);
						}
						returnMessage = "RETRIGGERED";
					}
				}
			} else {
				List<Integer> advanceMonths = invoiceLineItemDAO.getAdvanceMonthsBasedOnBatchId(batchId, tan);
				if (!advanceMonths.isEmpty()) {
					batchList = batchUploadDAO.findBatchRecordsByChallanMonthsTan(tan, advanceMonths, assessmentYear,
							bacthType);
					if (batchList.isEmpty()) {
						invoiceLineItemDAO.deleteAdvacneBasedOnBatchId(batchId, tan, assessmentYear);
						if (!"ADVANCE_NR_EXCEL".equalsIgnoreCase(fileType)) {
							SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks()
									.get(fileType.toLowerCase());
							logger.info("Notebook url : {}", notebook.getUrl());
							triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam,
									batchData.get(0).getAssessmentMonth(), assessmentYear, tenantId, tan, userName);
						}
						returnMessage = "RETRIGGERED";
					}
				}
			}
			if (batchList.isEmpty()
					&& ("INVOICE_NR_EXCEL".equalsIgnoreCase(fileType) || "PROVISION_NR_EXCEL".equalsIgnoreCase(fileType)
							|| "ADVANCE_NR_EXCEL".equalsIgnoreCase(fileType))) {
				invoiceLineItemDAO.deleteNrMetaTransactionsByBatchId(batchId, tan);
				invoiceLineItemService.retriggerdNrTransactions(batchId, tan, token, tenantId, deductorPan);
			}
		}
		return returnMessage;
	}

	private String getBatchType(String fileType) {
		String batchType = "BATCHPROCESS_LIABILITYRECON";
		if ("INVOICE_NR_EXCEL".equalsIgnoreCase(fileType) || "PROVISION_NR_EXCEL".equalsIgnoreCase(fileType)
				|| "ADVANCE_NR_EXCEL".equalsIgnoreCase(fileType)) {
			batchType = "NR_BATCHPROCESS_LIABILITYRECON";
		}
		return batchType;
	}

	/**
	 *
	 * @param assessmentMonth
	 * @param tan
	 * @param assessmentYear
	 * @param deductorPan
	 * @return
	 */
	public String revertAllMismatchs(int assessmentMonth, String tan, int assessmentYear, String deductorPan,
			String fileType) {
		String returnMessage = "LIABLITY_CALCULATED";
		if ("INVOICE_EXCEL".equalsIgnoreCase(fileType)) {
			List<InvoiceLineItem> updateInvoiceList = new ArrayList<>();
			List<InvoiceLineItem> invoiceList = invoiceLineItemDAO.getUpdateInvoiceRecords(tan, assessmentYear,
					assessmentMonth, deductorPan);
			if (!invoiceList.isEmpty()) {
				List<BatchUpload> batchList = batchUploadDAO.findBatchUploadBy(assessmentYear, tan, assessmentMonth);
				if (batchList.isEmpty()) {
					for (InvoiceLineItem invoice : invoiceList) {
						invoice.setActionType(StringUtils.EMPTY);
						invoice.setFinalReason(StringUtils.EMPTY);
						invoice.setFinalTdsAmount(BigDecimal.ZERO);
						invoice.setFinalTdsRate(BigDecimal.ZERO);
						invoice.setFinalTdsSection(StringUtils.EMPTY);
						invoice.setActive(false);
						invoice.setHasMismatch(true);
						updateInvoiceList.add(invoice);
					}
					if(!updateInvoiceList.isEmpty()) {
						invoiceLineItemDAO.batchUpdateInvoiceMismatch(updateInvoiceList);
					}
					returnMessage = "REVERT";
				}
			}
		} else if ("PROVISIONS".equalsIgnoreCase(fileType)) {
			List<ProvisionDTO> updateProvisionList = new ArrayList<>();
			List<ProvisionDTO> provisionList = provisionDAO.getUpdatedProvisionRecords(tan, assessmentYear,
					assessmentMonth, deductorPan);
			if (!provisionList.isEmpty()) {
				List<BatchUpload> batchList = batchUploadDAO.findBatchUploadBy(assessmentYear, tan, assessmentMonth);
				if (batchList.isEmpty()) {
					for (ProvisionDTO provision : provisionList) {
						provision.setActionType(StringUtils.EMPTY);
						provision.setReason(StringUtils.EMPTY);
						provision.setFinalTdsAmount(BigDecimal.ZERO);
						provision.setFinalTdsRate(BigDecimal.ZERO);
						provision.setFinalTdsSection(StringUtils.EMPTY);
						provision.setActive(false);
						provision.setMismatch(true);
						updateProvisionList.add(provision);
					}
					if(!updateProvisionList.isEmpty()) {
						provisionDAO.batchUpdateProvisionMismatch(updateProvisionList);
					}
					returnMessage = "REVERT";
				}
			}
		} else {
			List<AdvanceDTO> updateAdvanceList = new ArrayList<>();
			List<AdvanceDTO> advanceList = advanceDAO.getUpdatedAdvanceRecords(tan, assessmentYear,
					assessmentMonth, deductorPan);
			if (!advanceList.isEmpty()) {
				List<BatchUpload> batchList = batchUploadDAO.findBatchUploadBy(assessmentYear, tan,
						assessmentMonth);
				if (batchList.isEmpty()) {
					for (AdvanceDTO advance : advanceList) {
						advance.setAction(StringUtils.EMPTY);
						advance.setReason(StringUtils.EMPTY);
						advance.setFinalTdsAmount(BigDecimal.ZERO);
						advance.setFinalTdsRate(BigDecimal.ZERO);
						advance.setFinalTdsSection(StringUtils.EMPTY);
						advance.setActive(false);
						advance.setMismatch(true);
						updateAdvanceList.add(advance);
					}
					if(!updateAdvanceList.isEmpty()) {
						advanceDAO.batchUpdateAdvacneMismatch(updateAdvanceList);
					}
					returnMessage = "REVERT";
				}
			}
		}
		return returnMessage;
	}

	/**
	 * 
	 * @param deductorTan
	 * @param batchId
	 * @param deductorPan
	 * @param tenantId
	 * @param type
	 * @param userName
	 * @param year2 
	 * @return
	 * @throws Exception 
	 */
	public String getNotdsReport(String deductorTan, int batchId, String deductorPan, String tenantId, String type,
			String userName, int year) throws Exception {
		Resource resource = null;
		String msg = StringUtils.EMPTY;

		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		String fileName = UploadTypes.NO_TDS_FILE_REPORT.name() + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date())
				+ ".xlsx";
		BatchUpload batchUpload = saveBatchUploadReport(deductorTan, tenantId, year, null, 0L,
				UploadTypes.NO_TDS_FILE_REPORT.name(), "Processing", month, userName, null, fileName);

		DeductorMasterDTO deductor = onboardingClient.getDeductorMasterData(tenantId, deductorPan);
		if ("INVOICE".equalsIgnoreCase(type)) {
			List<InvoiceLineItem> invoiceList = invoiceLineItemDAO.getInvoiceBasedOnBatchId(batchId, deductorTan, year);
			resource = resourceLoader.getResource("classpath:templates/" + "invoice_no_tds_report.xlsx");
			msg = getErrorReportMsg(tenantId, deductorPan, "Invoice NOTDS Report");
			InputStream input = resource.getInputStream();
			try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
				XSSFSheet sheet1 = wb.getSheetAt(0);
				sheet1.lockAutoFilter(false);
				sheet1.lockSort(false);
				sheet1.autoSizeColumn(1);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				Font fonts = wb.createFont();
				fonts.setBold(true);

				XSSFCellStyle style = wb.createCellStyle();
				style.setFont(fonts);
				style.setWrapText(true);
				style.setBorderLeft(BorderStyle.MEDIUM);
				style.setBorderTop(BorderStyle.MEDIUM);
				style.setBorderBottom(BorderStyle.MEDIUM);
				style.setBorderRight(BorderStyle.MEDIUM);
				style.setAlignment(HorizontalAlignment.LEFT);
				style.setVerticalAlignment(VerticalAlignment.CENTER);

				Font fonts2 = wb.createFont();
				fonts2.setBold(false);

				XSSFCellStyle style1 = wb.createCellStyle();
				style1.setFont(fonts);
				style1.setWrapText(true);
				style1.setFont(fonts2);
				style1.setLocked(true);

				XSSFRow row1 = sheet1.createRow(0);
				row1.createCell(0).setCellValue(msg);
				row1.getCell(0).setCellStyle(style);

				int rowindexForInvoice = 5;
				int squenceNumberInvoice = 0;

				if (!invoiceList.isEmpty()) {
					for (InvoiceLineItem invoice : invoiceList) {
						XSSFRow row001 = sheet1.createRow(rowindexForInvoice++);
						row1.setHeightInPoints((2 * sheet1.getDefaultRowHeightInPoints()));
						// Deductor TAN
						createSXSSFCell(style1, row001, 0, invoice.getDeductorTan());
						// No TDS Reason
						createSXSSFCell(style1, row001, 1, invoice.getErrorReason());
						// Sequence Number
						createSXSSFCell(style1, row001, 2, String.valueOf(++squenceNumberInvoice));
						// Source Identifier
						createSXSSFCell(style1, row001, 3, invoice.getSourceIdentifier());
						// Source File Name
						createSXSSFCell(style1, row001, 4, invoice.getSourceFileName());
						// DeductorCode
						createSXSSFCell(style1, row001, 5, invoice.getDeductorCode());
						// DeductorName
						createSXSSFCell(style1, row001, 6, deductor.getDeductorName());
						// Deductor PAN
						createSXSSFCell(style1, row001, 7, invoice.getDeductorPan());
						// Deductor TAN
						createSXSSFCell(style1, row001, 8, invoice.getDeductorTan());
						// Deductor GSTIN
						createSXSSFCell(style1, row001, 9, invoice.getDeductorGstin());
						// DeducteeCode
						createSXSSFCell(style1, row001, 10, invoice.getDeducteeCode());
						// DeducteeName
						createSXSSFCell(style1, row001, 11, invoice.getDeducteeName());
						// DeducteePAN
						createSXSSFCell(style1, row001, 12, invoice.getPan());
						// DeducteeTIN
						createSXSSFCell(style1, row001, 13, invoice.getDeducteeTin());
						// DeducteeGSTIN
						createSXSSFCell(style1, row001, 14, invoice.getGstin());
						// VendorInvoiceNumber
						createSXSSFCell(style1, row001, 15, invoice.getVendorInvoiceNumber());
						// DocumentDate
						createSXSSFCell(style1, row001, 16, invoice.getDocumentDate() == null ? StringUtils.EMPTY
								: invoice.getDocumentDate().toString());
						// ERPDocumentNumber
						createSXSSFCell(style1, row001, 17, invoice.getDocumentNumber());
						// PostingDate
						createSXSSFCell(style1, row001, 18, invoice.getDocumentPostingDate() == null ? StringUtils.EMPTY
								: invoice.getDocumentPostingDate().toString());
						// PaymentDate
						createSXSSFCell(style1, row001, 19, invoice.getPaymentDate() == null ? StringUtils.EMPTY
								: invoice.getPaymentDate().toString());
						// TDSDeductionDate
						createSXSSFCell(style1, row001, 20, invoice.getTdsDeductionDate() == null ? StringUtils.EMPTY
								: invoice.getTdsDeductionDate().toString());
						// DocumentType
						createSXSSFCell(style1, row001, 21, invoice.getDocumentType());
						// SupplyType
						createSXSSFCell(style1, row001, 22, invoice.getSupplyType());
						// MIGONumber
						createSXSSFCell(style1, row001, 23, invoice.getMigoNumber());
						// MIRONumber
						createSXSSFCell(style1, row001, 24, invoice.getMiroNumber());
						// ERPDocumentType
						createSXSSFCell(style1, row001, 25, invoice.getDocumentType());
						// LineItemNumber
						createSXSSFCell(style1, row001, 26, invoice.getLineItemNumber() == null ? StringUtils.EMPTY
								: invoice.getLineItemNumber().toString());
						// HSNorSAC
						createSXSSFCell(style1, row001, 27, invoice.getHsnSacCode());
						// HSNorSACDesc
						createSXSSFCell(style1, row001, 28, invoice.getSacDecription());
						// InvoiceDesc
						createSXSSFCell(style1, row001, 29, invoice.getServiceDescriptionInvoice());
						// GLAccountCode
						createSXSSFCell(style1, row001, 30, invoice.getGlAccountCode());
						// GLAccountName
						createSXSSFCell(style1, row001, 31, invoice.getServiceDescriptionGl());
						// PONumber
						createSXSSFCell(style1, row001, 32, invoice.getPoNumber());
						// PODate
						createSXSSFCell(style1, row001, 33,
								invoice.getPoDate() == null ? StringUtils.EMPTY : invoice.getPoDate().toString());
						// PODesc
						createSXSSFCell(style1, row001, 34, invoice.getServiceDescriptionPo());
						// TaxableValue
						createSXSSFCell(style1, row001, 35, invoice.getInvoiceAmount() == null ? StringUtils.EMPTY
								: invoice.getInvoiceAmount().toString());
						// IGSTRate
						createSXSSFCell(style1, row001, 36,
								invoice.getIgstRate() == null ? StringUtils.EMPTY : invoice.getIgstRate().toString());
						// IGSTAmount
						createSXSSFCell(style1, row001, 37, invoice.getIgstAmount() == null ? StringUtils.EMPTY
								: invoice.getIgstAmount().toString());
						// CGSTRate
						createSXSSFCell(style1, row001, 38,
								invoice.getCgstRate() == null ? StringUtils.EMPTY : invoice.getCgstRate().toString());
						// CGSTAmount
						createSXSSFCell(style1, row001, 39, invoice.getCgstAmount() == null ? StringUtils.EMPTY
								: invoice.getCgstAmount().toString());
						// SGSTRate
						createSXSSFCell(style1, row001, 40,
								invoice.getSgstRate() == null ? StringUtils.EMPTY : invoice.getSgstRate().toString());
						// SGSTAmount
						createSXSSFCell(style1, row001, 41, invoice.getSgstAmount() == null ? StringUtils.EMPTY
								: invoice.getSgstAmount().toString());
						// CESSRate
						createSXSSFCell(style1, row001, 42,
								invoice.getCessRate() == null ? StringUtils.EMPTY : invoice.getCessRate().toString());
						// CESSAmount
						createSXSSFCell(style1, row001, 43, invoice.getCessAmount() == null ? StringUtils.EMPTY
								: invoice.getCessAmount().toString());
						// POS
						createSXSSFCell(style1, row001, 44, invoice.getPos());
						// TDSTaxCodeERP
						createSXSSFCell(style1, row001, 45, invoice.getSectionCode());
						// TDSSection
						createSXSSFCell(style1, row001, 46, invoice.getTdsSection());
						// TDSRate
						createSXSSFCell(style1, row001, 47,
								invoice.getTdsRate() == null ? StringUtils.EMPTY : invoice.getTdsRate().toString());
						// TDSAmount
						createSXSSFCell(style1, row001, 48,
								invoice.getTdsAmount() == null ? StringUtils.EMPTY : invoice.getTdsAmount().toString());
						// LinkedAdvanceIndicator
						createSXSSFCell(style1, row001, 49, StringUtils.EMPTY);
						// LinkedProvisionIndicator
						createSXSSFCell(style1, row001, 50, StringUtils.EMPTY);
						// ProvisionAdjustmentFlag
						createSXSSFCell(style1, row001, 51, invoice.getProvisionCanAdjust() == null ? StringUtils.EMPTY
								: invoice.getProvisionCanAdjust().toString());
						// AdvanceAdjustmentFlag
						createSXSSFCell(style1, row001, 52, invoice.getAdvanceCanAdjust() == null ? StringUtils.EMPTY
								: invoice.getAdvanceCanAdjust().toString());
						// ChallanPaidFlag
						createSXSSFCell(style1, row001, 53, invoice.getChallanPaid() == null ? StringUtils.EMPTY
								: invoice.getChallanPaid().toString());
						// ChallanPaidDate
						createSXSSFCell(style1, row001, 54, invoice.getChallanPaidDate());
						// GrossUpIndicator
						createSXSSFCell(style1, row001, 55, invoice.getGrossIndicator());
						// OriginalDocumentNumber
						createSXSSFCell(style1, row001, 56, invoice.getOriginalDocumentNumber());
						// OriginalDocumentDate
						createSXSSFCell(style1, row001, 57,
								invoice.getOriginalDocumentDate() == null ? StringUtils.EMPTY
										: invoice.getOriginalDocumentDate().toString());
						// RefKey3
						createSXSSFCell(style1, row001, 58, StringUtils.EMPTY);
						// UserDefinedField1
						createSXSSFCell(style1, row001, 59, invoice.getUserDefinedField1());
						// UserDefinedField2
						createSXSSFCell(style1, row001, 60, invoice.getUserDefinedField2());
						// UserDefinedField3
						createSXSSFCell(style1, row001, 61, invoice.getUserDefinedField3());

					}
				}
				wb.write(out);
				saveBatchUploadReport(deductorTan, tenantId, year, out, Long.valueOf(invoiceList.size()),
						UploadTypes.NO_TDS_FILE_REPORT.name(), "Processed", month, userName,
						batchUpload.getBatchUploadID(), null);
			}
		} else if ("PROVISION".equalsIgnoreCase(type)) {
			List<ProvisionDTO> provisionList = invoiceLineItemDAO.getProvisionBasedOnBatchId(batchId, deductorTan,
					year);
			resource = resourceLoader.getResource("classpath:templates/" + "provision_no_tds_report.xlsx");
			msg = getErrorReportMsg(tenantId, deductorPan, "Provision NOTDS Report");
			InputStream input = resource.getInputStream();
			try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
				XSSFSheet sheet1 = wb.getSheetAt(0);
				sheet1.lockAutoFilter(false);
				sheet1.lockSort(false);
				sheet1.autoSizeColumn(1);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				Font fonts = wb.createFont();
				fonts.setBold(true);

				XSSFCellStyle style = wb.createCellStyle();
				style.setFont(fonts);
				style.setWrapText(true);
				style.setBorderLeft(BorderStyle.MEDIUM);
				style.setBorderTop(BorderStyle.MEDIUM);
				style.setBorderBottom(BorderStyle.MEDIUM);
				style.setBorderRight(BorderStyle.MEDIUM);
				style.setAlignment(HorizontalAlignment.LEFT);
				style.setVerticalAlignment(VerticalAlignment.CENTER);

				Font fonts2 = wb.createFont();
				fonts2.setBold(false);

				XSSFCellStyle style1 = wb.createCellStyle();
				style1.setFont(fonts);
				style1.setWrapText(true);
				style1.setFont(fonts2);
				style1.setLocked(true);

				XSSFRow row1 = sheet1.createRow(0);
				row1.createCell(0).setCellValue(msg);
				row1.getCell(0).setCellStyle(style);

				int rowindexForProvision = 5;
				int squenceNumberProvision = 0;
				if (!provisionList.isEmpty()) {
					for (ProvisionDTO provision : provisionList) {
						XSSFRow row001 = sheet1.createRow(rowindexForProvision++);
						row1.setHeightInPoints((2 * sheet1.getDefaultRowHeightInPoints()));

						// Deductor TAN
						createSXSSFCell(style1, row001, 0, provision.getDeductorMasterTan());
						// No TDS Reason
						createSXSSFCell(style1, row001, 1, provision.getErrorReason());
						// Sequence Number
						createSXSSFCell(style1, row001, 2, String.valueOf(++squenceNumberProvision));
						// Source Identifier
						createSXSSFCell(style1, row001, 3, provision.getSourceIdentifier());
						// Source File Name
						createSXSSFCell(style1, row001, 4, provision.getSourceFileName());
						// DeductorCode
						createSXSSFCell(style1, row001, 5, deductor.getDeductorCode());
						// DeductorName
						createSXSSFCell(style1, row001, 6, deductor.getDeductorName());
						// Deductor PAN
						createSXSSFCell(style1, row001, 7, provision.getDeductorPan());
						// Deductor TAN
						createSXSSFCell(style1, row001, 8, provision.getDeductorMasterTan());
						// Deductor GSTIN
						createSXSSFCell(style1, row001, 9, provision.getDeductorGstin());
						// DeducteeCode
						createSXSSFCell(style1, row001, 10, provision.getDeducteeCode());
						// DeducteeName
						createSXSSFCell(style1, row001, 11, provision.getDeducteeName());
						// DeducteePAN
						createSXSSFCell(style1, row001, 12, provision.getDeducteePan());
						// DeducteeTIN
						createSXSSFCell(style1, row001, 13, provision.getDeducteeTin());
						// DeducteeGSTIN
						createSXSSFCell(style1, row001, 14, provision.getDeducteeGstin());
						// ERPDocumentNumber
						createSXSSFCell(style1, row001, 15, provision.getDocumentNumber());
						// DocumentDate
						createSXSSFCell(style1, row001, 16, provision.getDocumentDate() == null ? StringUtils.EMPTY
								: provision.getDocumentDate().toString());
						// PostingDate
						createSXSSFCell(style1, row001, 17,
								provision.getPostingDateOfDocument() == null ? StringUtils.EMPTY
										: provision.getPostingDateOfDocument().toString());
						// TDSDeductionDate
						createSXSSFCell(style1, row001, 18, provision.getTdsDeductionDate() == null ? StringUtils.EMPTY
								: provision.getTdsDeductionDate().toString());
						// DocumentType
						createSXSSFCell(style1, row001, 19, provision.getDocumentType());
						// SupplyType
						createSXSSFCell(style1, row001, 20, provision.getSupplyType());
						// LineItemNumber
						createSXSSFCell(style1, row001, 21, provision.getLineItemNumber() == null ? StringUtils.EMPTY
								: provision.getLineItemNumber().toString());
						// GLAccountCode
						createSXSSFCell(style1, row001, 22, provision.getGlAccountCode());
						// GLAccountName
						createSXSSFCell(style1, row001, 23, provision.getServiceDescriptionGl());
						// ERPDocumentType
						createSXSSFCell(style1, row001, 24, provision.getDocumentType());
						// HSNorSAC
						createSXSSFCell(style1, row001, 25, provision.getHsnOrSac());
						// HSNorSACDesc
						createSXSSFCell(style1, row001, 26, provision.getHsnsacDescription());
						// ProvisionsDesc
						createSXSSFCell(style1, row001, 27, provision.getDescription());
						// PODesc
						createSXSSFCell(style1, row001, 28, provision.getServiceDescriptionPo());
						// TaxableValue
						createSXSSFCell(style1, row001, 29, provision.getProvisionalAmount() == null ? StringUtils.EMPTY
								: provision.getProvisionalAmount().toString());
						// TDSTaxCodeERP
						createSXSSFCell(style1, row001, 30, provision.getSectionCode());
						// TDSSection
						createSXSSFCell(style1, row001, 31, provision.getSection());
						// TDSRate
						createSXSSFCell(style1, row001, 32,
								provision.getTdsRate() == null ? StringUtils.EMPTY : provision.getTdsRate().toString());
						// TDSAmount
						createSXSSFCell(style1, row001, 33, provision.getClientAmount() == null ? StringUtils.EMPTY
								: provision.getClientAmount().toString());
						// PONumber
						createSXSSFCell(style1, row001, 34, provision.getPoNumber());
						// PODate
						createSXSSFCell(style1, row001, 35,
								provision.getPoDate() == null ? StringUtils.EMPTY : provision.getPoDate().toString());
						// POType
						createSXSSFCell(style1, row001, 36, provision.getPoType());
						// LinkingofInvoicewithPO
						createSXSSFCell(style1, row001, 37, provision.getLinkingInvoicePo());
						// ChallanPaidFlag
						createSXSSFCell(style1, row001, 38, provision.getChallanPaid() == null ? StringUtils.EMPTY
								: provision.getChallanPaid().toString());
						// ChallanPaidDate
						createSXSSFCell(style1, row001, 39, provision.getChallanPaidDate());
						// UserDefinedField1
						createSXSSFCell(style1, row001, 40, provision.getUserDefinedField1());
						// UserDefinedField2
						createSXSSFCell(style1, row001, 41, provision.getUserDefinedField2());
						// UserDefinedField3
						createSXSSFCell(style1, row001, 42, provision.getUserDefinedField3());
					}
				}
				wb.write(out);
				saveBatchUploadReport(deductorTan, tenantId, year, out, Long.valueOf(provisionList.size()),
						UploadTypes.NO_TDS_FILE_REPORT.name(), "Processed", month, userName,
						batchUpload.getBatchUploadID(), null);
			}
		} else if ("ADVANCE".equalsIgnoreCase(type)) {
			List<AdvanceDTO> advanceList = invoiceLineItemDAO.getAdvacneBasedOnBatchId(batchId, deductorTan, year);
			resource = resourceLoader.getResource("classpath:templates/" + "advance_no_tds_report.xlsx");
			msg = getErrorReportMsg(tenantId, deductorPan, "Advance NOTDS Report");
			InputStream input = resource.getInputStream();
			try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
				XSSFSheet sheet1 = wb.getSheetAt(0);
				sheet1.lockAutoFilter(false);
				sheet1.lockSort(false);
				sheet1.autoSizeColumn(1);

				ByteArrayOutputStream out = new ByteArrayOutputStream();

				Font fonts = wb.createFont();
				fonts.setBold(true);

				XSSFCellStyle style = wb.createCellStyle();
				style.setFont(fonts);
				style.setWrapText(true);
				style.setBorderLeft(BorderStyle.MEDIUM);
				style.setBorderTop(BorderStyle.MEDIUM);
				style.setBorderBottom(BorderStyle.MEDIUM);
				style.setBorderRight(BorderStyle.MEDIUM);
				style.setAlignment(HorizontalAlignment.LEFT);
				style.setVerticalAlignment(VerticalAlignment.CENTER);

				Font fonts2 = wb.createFont();
				fonts2.setBold(false);

				XSSFCellStyle style1 = wb.createCellStyle();
				style1.setFont(fonts);
				style1.setWrapText(true);
				style1.setFont(fonts2);
				style1.setLocked(true);

				XSSFRow row1 = sheet1.createRow(0);
				row1.createCell(0).setCellValue(msg);
				row1.getCell(0).setCellStyle(style);

				int rowindexForAdvance = 5;
				int squenceNumberAdvance = 0;
				if (!advanceList.isEmpty()) {
					for (AdvanceDTO advance : advanceList) {
						XSSFRow row001 = sheet1.createRow(rowindexForAdvance++);
						row1.setHeightInPoints((2 * sheet1.getDefaultRowHeightInPoints()));
						// Deductor TAN
						createSXSSFCell(style1, row001, 0, advance.getDeductorMasterTan());
						// No TDS Reason
						createSXSSFCell(style1, row001, 1, advance.getErrorReason());
						// Sequence Number
						createSXSSFCell(style1, row001, 2, String.valueOf(++squenceNumberAdvance));
						// Source Identifier
						createSXSSFCell(style1, row001, 3, advance.getSourceIdentifiers());
						// Source File Name
						createSXSSFCell(style1, row001, 4, advance.getSourceFileName());
						// DeductorCode
						createSXSSFCell(style1, row001, 5, deductor.getDeductorCode());
						// DeductorName
						createSXSSFCell(style1, row001, 6, deductor.getDeductorName());
						// Deductor PAN
						createSXSSFCell(style1, row001, 7, advance.getDeductorPan());
						// Deductor TAN
						createSXSSFCell(style1, row001, 8, advance.getDeductorMasterTan());
						// Deductor GSTIN
						createSXSSFCell(style1, row001, 9, advance.getDeductorGstin());
						// DeducteeCode
						createSXSSFCell(style1, row001, 10, advance.getDeducteeCode());
						// DeducteeName
						createSXSSFCell(style1, row001, 11, advance.getDeducteeName());
						// DeducteePAN
						createSXSSFCell(style1, row001, 12, advance.getDeducteePan());
						// DeducteeTIN
						createSXSSFCell(style1, row001, 13, advance.getDeducteeTin());
						// DeducteeGSTIN
						createSXSSFCell(style1, row001, 14, advance.getDeducteeGstin());
						// ERPDocumentNumber
						createSXSSFCell(style1, row001, 15, advance.getDocumentNumber());
						// DocumentDate
						createSXSSFCell(style1, row001, 16, advance.getDocumentDate() == null ? StringUtils.EMPTY
								: advance.getDocumentDate().toString());
						// PostingDate
						createSXSSFCell(style1, row001, 17,
								advance.getPostingDateOfDocument() == null ? StringUtils.EMPTY
										: advance.getPostingDateOfDocument().toString());
						// TDSDeductionDate
						createSXSSFCell(style1, row001, 18, advance.getTdsDeductionDate() == null ? StringUtils.EMPTY
								: advance.getTdsDeductionDate().toString());
						// DocumentType
						createSXSSFCell(style1, row001, 19, advance.getDocumentType());
						// SupplyType
						createSXSSFCell(style1, row001, 20, advance.getSupplyType());
						// LineItemNumber
						createSXSSFCell(style1, row001, 21, advance.getLineItemNumber() == null ? StringUtils.EMPTY
								: advance.getLineItemNumber().toString());
						// GLAccountCode
						createSXSSFCell(style1, row001, 22, advance.getGlAccountCode());
						// GLAccountName
						createSXSSFCell(style1, row001, 23, advance.getServiceDescriptionGl());
						// ERPDocumentType
						createSXSSFCell(style1, row001, 24, StringUtils.EMPTY);
						// HSNorSAC
						createSXSSFCell(style1, row001, 25, advance.getHsnOrSac());
						// HSNorSACDesc
						createSXSSFCell(style1, row001, 26, advance.getSacDescription());
						// AdvanceDesc
						createSXSSFCell(style1, row001, 27, advance.getServiceDescription());
						// PODesc
						createSXSSFCell(style1, row001, 28, advance.getServiceDescriptionPo());
						// TaxableValue
						createSXSSFCell(style1, row001, 29,
								advance.getAmount() == null ? StringUtils.EMPTY : advance.getAmount().toString());
						// TDSTaxCodeERP
						createSXSSFCell(style1, row001, 30, advance.getSectionCode());
						// TDSSection
						createSXSSFCell(style1, row001, 31, advance.getSection());
						// TDSRate
						createSXSSFCell(style1, row001, 32,
								advance.getTdsRate() == null ? StringUtils.EMPTY : advance.getTdsRate().toString());
						// TDSAmount
						createSXSSFCell(style1, row001, 33,
								advance.getAmount() == null ? StringUtils.EMPTY : advance.getAmount().toString());
						// PONumber
						createSXSSFCell(style1, row001, 34, advance.getPoNumber());
						// PODate
						createSXSSFCell(style1, row001, 35,
								advance.getPoDate() == null ? StringUtils.EMPTY : advance.getPoDate().toString());
						// LinkingofInvoicewithPO
						createSXSSFCell(style1, row001, 36, advance.getLinkingOfInvoiceWithPo());
						// ChallanPaidFlag
						createSXSSFCell(style1, row001, 37, StringUtils.EMPTY);
						// ChallanPaidDate
						createSXSSFCell(style1, row001, 38, advance.getChallanPaidDate());
						// UserDefinedField1
						createSXSSFCell(style1, row001, 39, advance.getUserDefinedField1());
						// UserDefinedField2
						createSXSSFCell(style1, row001, 40, advance.getUserDefinedField2());
						// UserDefinedField3
						createSXSSFCell(style1, row001, 41, advance.getUserDefinedField3());
					}
				}
				wb.write(out);
				saveBatchUploadReport(deductorTan, tenantId, year, out, Long.valueOf(advanceList.size()),
						UploadTypes.NO_TDS_FILE_REPORT.name(), "Processed", month, userName,
						batchUpload.getBatchUploadID(), null);
			}
		}

		return StringUtils.EMPTY;
	}

	/**
	 * 
	 * @param deductorTan
	 * @param tenantId
	 * @param assessmentYear
	 * @param out
	 * @param noOfRows
	 * @param uploadType
	 * @param status
	 * @param month
	 * @param userName
	 * @param batchId
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws StorageException
	 * @throws ParseException
	 */
	public BatchUpload saveBatchUploadReport(String deductorTan, String tenantId, int assessmentYear,
			ByteArrayOutputStream out, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId, String fileName) throws FileNotFoundException, IOException, URISyntaxException,
			InvalidKeyException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String path = null;
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setSuccessFileUrl(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		batchUpload.setActive(true);
		if (StringUtils.isNotBlank(fileName)) {
			fileName = fileName + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
			batchUpload.setFileName(fileName);
		}
		List<BatchUpload> response = new ArrayList<>();
		if (batchId != null) {
			response = batchUploadDAO.findById(assessmentYear, deductorTan, uploadType, batchId);
		}
		if (!response.isEmpty()) {
			batchUpload = response.get(0);
			if (out != null) {
				File file = getConvertedExcelFile(out.toByteArray(), batchUpload.getFileName());
				path = blob.uploadExcelToBlobWithFile(file, tenantId);
				batchUpload.setStatus(status);
				batchUpload.setFilePath(path);
				batchUpload.setSuccessFileUrl(path);
				batchUpload.setRowsCount(noOfRows);
				batchUpload.setSuccessCount(noOfRows);
				batchUpload.setModifiedBy(userName);
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			} else {
				batchUpload.setModifiedBy(userName);
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchUpload.setRowsCount(0l);
			}
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}
	
	/**
	 * 
	 * @param byteArray
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	protected File getConvertedExcelFile(byte[] byteArray, String fileName) throws IOException {
		File someFile = new File(fileName);
		try (FileOutputStream fos = new FileOutputStream(someFile)) {
			fos.write(byteArray);
			fos.flush();
			fos.close();
			return someFile;
		}
	}
	
	/**
	 * 
	 * @param tenantId
	 * @param deductorPan
	 * @param fileType
	 * @return
	 */
	public String getErrorReportMsg(String tenantId, String deductorPan, String fileType) {
		MultiTenantContext.setTenantId(tenantId);
		DeductorMasterDTO deductor = onboardingClient.getDeductorMasterData(tenantId, deductorPan);
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return fileType + " (Dated: " + date + ")\n Client Name: " + deductor.getDeductorName() + "\n";
	}

	
	/**
	 *
	 * @param style
	 * @param row
	 * @param cellNumber
	 * @param value
	 */
	private void createSXSSFCell(XSSFCellStyle style, XSSFRow row, int cellNumber, String value) {
		row.createCell(cellNumber).setCellValue(StringUtils.isBlank(value) == true ? StringUtils.EMPTY : value);
		row.getCell(cellNumber).setCellStyle(style); // K
	}
}