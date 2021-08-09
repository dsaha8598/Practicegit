package com.ey.in.tds.ingestion.service.invoiceshareholder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.tika.Tika;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tcs.common.domain.dividend.DividendPayoutState;
import com.ey.in.tcs.common.domain.dividend.InvoiceShareholderNonResident;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.dashboard.dto.ActivityTracker;
import com.ey.in.tds.common.dashboards.jdbc.dao.ActivityTrackerDAO;
import com.ey.in.tds.common.domain.dividend.InvoiceShareholderResident;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.DeducteeDetailDTO;
import com.ey.in.tds.common.dto.DividendInvoiceDTO;
import com.ey.in.tds.common.dto.dividend.Form15CBDetails;
import com.ey.in.tds.common.dto.dividend.InvoiceShareHolderErrorReportCsvDTO;
import com.ey.in.tds.common.dto.dividend.InvoiceShareholderNonResidentDTO;
import com.ey.in.tds.common.dto.dividend.InvoiceShareholderResidentDTO;
import com.ey.in.tds.common.dto.dividend.SummeryReportErrorCSVDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.ingestion.response.dto.SparkResponseDTO;
import com.ey.in.tds.common.model.job.DividendJob;
import com.ey.in.tds.common.model.job.DividendNoteBookParam;
import com.ey.in.tds.common.model.job.NoteBookBatch;
import com.ey.in.tds.common.onboarding.response.dto.DeductorOnboardingInformationResponseDTO;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.ErrorReportService;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ActivityTrackerStatus;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.config.SparkNotebooks;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceShareholderNonResidentDAO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceShareholderResidentDAO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

@Service
public class InvoiceShareholderService {
	private static final String AckDate_Input_Format = "yyyy-MM-dd hh:mm:ss";
	private static final String AckDate_Output_DB_Format = "yyyy-MM-dd hh:mm:ss.SSSZ";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Sha256SumService sha256SumService;

	@Value("${databricks.key}")
	private String dataBricksKey;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private ActivityTrackerDAO activityTrackerDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private SparkNotebooks sparkNotebooks;

	@Autowired
	private InvoiceShareholderNonResidentDAO invoiceShareholderNonResidentDAO;

	@Autowired
	private InvoiceShareholderResidentDAO invoiceShareholderResidentDAO;

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private InvoiceShareHolderBulkService invoiceShareHolderBulkService;

	/**
	 * getting all non resident invoice share holder with pagination
	 * 
	 * @param deductorPan
	 * @param year
	 * @param month
	 * @param pagination
	 * @param section
	 * @return
	 */
	public CommonDTO<InvoiceShareholderNonResidentDTO> getNonResidentShareholder(String deductorPan, int year,
			int month, Pagination pagination, String section) {
		BigInteger count = BigInteger.ZERO;
		CommonDTO<InvoiceShareholderNonResidentDTO> invoiceShareholderList = new CommonDTO<>();
		List<InvoiceShareholderNonResidentDTO> shareholderNonResidentDTOList = new ArrayList<>();

		logger.info("Pan : {} year : {} month : {} of nonresident shareholder section : {}", deductorPan, year, month,
				section);

		List<InvoiceShareholderNonResident> invoiceShareholderListData = null;
		invoiceShareholderListData = invoiceShareholderNonResidentDAO.findAllNonResident(year, month, deductorPan,
				pagination, section);
		count = invoiceShareholderNonResidentDAO.findCountOfAllNonResident(year, month, deductorPan, section);

		for (InvoiceShareholderNonResident invoiceNonResident : invoiceShareholderListData) {
			InvoiceShareholderNonResidentDTO shareholderDto = new InvoiceShareholderNonResidentDTO();
			BeanUtils.copyProperties(invoiceNonResident, shareholderDto);
			shareholderDto.setId(invoiceNonResident.getId());
			shareholderDto.setDeductorPan(invoiceNonResident.getDeductorPan());
			shareholderDto.setDateOfPosting(invoiceNonResident.getDateOfPosting());
			shareholderDto.setWithholdingDetails(invoiceNonResident.getWithholdingDetails());
			shareholderDto.setDeductorDividendType(invoiceNonResident.getDeductorDividendType());
			shareholderNonResidentDTOList.add(shareholderDto);
		}
		PagedData<InvoiceShareholderNonResidentDTO> pagedData = new PagedData<>(shareholderNonResidentDTOList,
				shareholderNonResidentDTOList.size(), pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		invoiceShareholderList.setResultsSet(pagedData);
		invoiceShareholderList.setCount(count);
		return invoiceShareholderList;
	}

	/**
	 * to get the resident records with pagination
	 * 
	 * @param deductorPan
	 * @param year
	 * @param month
	 * @param pagination
	 * @param section
	 * @return
	 */
	public CommonDTO<InvoiceShareholderResidentDTO> getResidentShareholder(String deductorPan, int year, int month,
			Pagination pagination, String section) {
		BigInteger count = BigInteger.ZERO;
		CommonDTO<InvoiceShareholderResidentDTO> invoiceShareholderList = new CommonDTO<>();
		List<InvoiceShareholderResidentDTO> shareholderResidentDTOList = new ArrayList<>();

		logger.info("Pan : {} year : {} month : {} of resident shareholder section : {}", deductorPan, year, month,
				section);

		List<InvoiceShareholderResident> invoiceShareholderListData = null;
		invoiceShareholderListData = invoiceShareholderResidentDAO.findAllResident(year, month, deductorPan, pagination,
				section);
		logger.info("DAO execution got successfull to get th resident records {}");
		count = invoiceShareholderResidentDAO.findAllResidentCount(year, month, deductorPan, section);

		for (InvoiceShareholderResident invoiceResident : invoiceShareholderListData) {
			InvoiceShareholderResidentDTO shareholderDto = new InvoiceShareholderResidentDTO();
			BeanUtils.copyProperties(invoiceResident, shareholderDto);
			shareholderDto.setId(invoiceResident.getId());
			shareholderDto.setDeductorPan(invoiceResident.getDeductorPan());
			shareholderDto.setWithholdingDetails(invoiceResident.getWithholdingDetails());
			shareholderDto.setDeductorDividendType(invoiceResident.getDeductorDividendType());
			shareholderResidentDTOList.add(shareholderDto);
		}
		PagedData<InvoiceShareholderResidentDTO> pagedData = new PagedData<>(shareholderResidentDTOList,
				shareholderResidentDTOList.size(), pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		invoiceShareholderList.setResultsSet(pagedData);
		invoiceShareholderList.setCount(count);
		return invoiceShareholderList;
	}

	public InvoiceShareholderResidentDTO getResidentialShareholder(String deductorPan, Integer id) {
		InvoiceShareholderResidentDTO shareholderDto = new InvoiceShareholderResidentDTO();
		InvoiceShareholderResident invoiceResident = null;

		List<InvoiceShareholderResident> invoiceShareholderResidentOptional = invoiceShareholderResidentDAO
				.findByIdPan(id, deductorPan);

		if (!invoiceShareholderResidentOptional.isEmpty()) {
			invoiceResident = invoiceShareholderResidentOptional.get(0);
			logger.info("Retrieved resident record with id " + id + " is " + invoiceResident + "{}");
			BeanUtils.copyProperties(invoiceResident, shareholderDto);
			shareholderDto.setId(invoiceResident.getId());
			shareholderDto.setDeductorPan(invoiceResident.getDeductorPan());
			shareholderDto.setWithholdingDetails(invoiceResident.getWithholdingDetails());
		}
		return shareholderDto;
	}

	/**
	 * to get non resident invoice share holder
	 * 
	 * @param deductorPan
	 * @param id
	 * @return
	 */
	public InvoiceShareholderNonResidentDTO getNonResidentialShareholder(String deductorPan, Integer id) {
		InvoiceShareholderNonResidentDTO shareholderDto = new InvoiceShareholderNonResidentDTO();
		InvoiceShareholderNonResident invoiceNonResident = null;

		List<InvoiceShareholderNonResident> invoiceShareholderNonResidentOptional = invoiceShareholderNonResidentDAO
				.findByIdPan(id, deductorPan);
		logger.info("Retrieved InvoiceShareholderNonResident sucessfully with count ="
				+ invoiceShareholderNonResidentOptional.size() + "{}");
		if (!invoiceShareholderNonResidentOptional.isEmpty()) {
			invoiceNonResident = invoiceShareholderNonResidentOptional.get(0);
			BeanUtils.copyProperties(invoiceNonResident, shareholderDto);
			shareholderDto.setId(invoiceNonResident.getId());
			shareholderDto.setDeductorPan(invoiceNonResident.getDeductorPan());
			shareholderDto.setDateOfPosting(invoiceNonResident.getDateOfPosting());
			shareholderDto.setWithholdingDetails(invoiceNonResident.getWithholdingDetails());

		}
		return shareholderDto;
	}

	// TODO save the
	public InvoiceShareholderResident createShareholderResident(InvoiceShareholderResident invoiceShareholderResident) {
		InvoiceShareholderResident resident = null;
		// invoiceShareholderResidentDAO.save(invoiceShareholderResident);
		return resident;

	}

	public InvoiceShareholderNonResident createShareholderNonResident(
			InvoiceShareholderNonResident invoiceShareholderNonResident) {
		InvoiceShareholderNonResident shareholderNonResident = null;
		// invoiceShareholderNonResidentDAO
		// .save(invoiceShareholderNonResident);
		return shareholderNonResident;
	}

	@Transactional
	public BatchUpload processApproveDividendProcessing(int assessmentYear, MultipartFile[] files, String tenantId,
			String userEmail, String deductorPan, String deductorTan, String type, String residentType)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, InvalidFormatException,
			ParseException, IntrusionException, ValidationException {
		MultipartFile file = null;
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		String path;
		String uploadedFilePath;
		BatchUpload batchUpload = null;

		String dividendDeductorType = "";
		ResponseEntity<ApiStatus<DeductorMasterDTO>> responseEntity = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);

		DeductorMasterDTO deductorMasterDTO = responseEntity.getBody().getData();
		dividendDeductorType = deductorMasterDTO.getDeductorTypeName();

		if (Strings.isBlank(dividendDeductorType)) {
			return batchUpload;
		}

		if (files != null && files.length == 1) {
			String sparkResponse = null;
			file = files[0];
			logger.info(file.getOriginalFilename());
			String sha256 = sha256SumService.getSHA256Hash(file);
			String contentType = new Tika().detect(file.getInputStream(), file.getOriginalFilename());

			List<BatchUpload> batch = batchUploadDAO.getSha256Records(sha256);
			uploadedFilePath=blob.uploadExcelToBlob(file, tenantId);

			if (AllowedMimeTypes.CSV.getMimeType().equals(contentType)) {
				path = uploadedFilePath;
			} else {

				File csvFile = convertingToCSV(file);
				path = blob.uploadExcelToBlobWithFile(csvFile, tenantId);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Batch Object : {}", batch);
			}
			boolean isDuplicate = !batch.isEmpty();

			if (type.equalsIgnoreCase("dividend_payout_bulk_approve")) {
				batchUpload = saveBatchUpload(assessmentYear, userEmail, file, path, month, sha256, batch, isDuplicate,
						deductorTan,
						residentType.equalsIgnoreCase("resident") ? "RESIDENT_UPLOAD_SUMMERY" : "NR_UPLOAD_SUMMERY",uploadedFilePath);
			} else {
				batchUpload = saveBatchUpload(assessmentYear, userEmail, file, path, month, sha256, batch, isDuplicate,
						deductorTan, type,uploadedFilePath);
			}

			boolean isUnique = batchUpload.getStatus().equalsIgnoreCase("Uploaded");

			logger.info("Notebook type : {}", type);
			DividendNoteBookParam dividendNoteBookParam = createNoteBook(assessmentYear, deductorTan, tenantId, type,
					userEmail, month, batchUpload, dividendDeductorType, deductorPan);
			if (isUnique && (("dividend_payout_invoice_processing".equalsIgnoreCase(type))
					|| ("dividend_payout_bulk_approve".equalsIgnoreCase(type)))) {
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
										SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks()
												.get(type.toLowerCase());
										logger.info("Notebook 0 url : {} with jobId : {}", notebook.getUrl(), jobId);
										sparkResponse = triggerSparkNotebook(notebook.getUrl(), jobId,
												dividendNoteBookParam, month, assessmentYear, tenantId, deductorTan,
												userEmail);
										SparkResponseDTO sparkResponseDTO = new ObjectMapper().readValue(sparkResponse,
												SparkResponseDTO.class);
										batchUpload = updateBatchUpload(Integer.valueOf(sparkResponseDTO.getRun_id()),
												batchUpload);

										List<ActivityTracker> tracker = activityTrackerDAO
												.getActivityTrackerByTanYearTypeAndMonth(deductorTan, assessmentYear,
														"Dividend Payout Calculation", month);
										if (!tracker.isEmpty()) {
											tracker.get(0).setStatus(ActivityTrackerStatus.VALIDATED.name());
											tracker.get(0).setModifiedBy(userEmail);
											tracker.get(0).setModifiedDate(new Date());
											activityTrackerDAO.save(tracker.get(0));
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
					SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get(type);
					logger.info("Notebook url : {}", notebook.getUrl()+"job id ="+notebook.getJobId());
					sparkResponse = triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), dividendNoteBookParam,
							month, assessmentYear, tenantId, deductorTan, userEmail);
					SparkResponseDTO sparkResponseDTO = new ObjectMapper().readValue(sparkResponse,
							SparkResponseDTO.class);
					batchUpload = updateBatchUpload(Integer.valueOf(sparkResponseDTO.getRun_id()), batchUpload);
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
				boolean isDuplicate = !batch.isEmpty();

				batchUpload = saveBatchUpload(assessmentYear, userEmail, file, path, month, sha256, batch, isDuplicate,
						deductorTan, "dividend_payout_invoice_processing","");

				boolean isUnique = batchUpload.getStatus().equalsIgnoreCase("Uploaded");

				logger.info("Notebook type : {}", type);

				if (isUnique && (("dividend_payout_invoice_processing".equalsIgnoreCase(type))
						|| ("dividend_payout_bulk_approve".equalsIgnoreCase(type)))) {
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
						if (StringUtils.isNotBlank(batchUpload.getFileName())) {
							noteBookBatch.setFileName(batchUpload.getFileName());
						} else {
							noteBookBatch.setFileName("");
						}
					}
					batchEntries.add(noteBookBatch);
				}
			}
			DividendNoteBookParam dividendNoteBookParam = createNoteBook(assessmentYear, deductorTan, tenantId,
					"dividend_payout_invoice_processing", userEmail, month, batchUpload, dividendDeductorType,
					deductorPan);
			ObjectMapper objectMapper = new ObjectMapper();
			String batchEntriesJson = objectMapper.writeValueAsString(batchEntries);
			dividendNoteBookParam
					.setBatchEntries(StringUtils.isBlank(batchEntriesJson) ? StringUtils.EMPTY : batchEntriesJson);
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
								triggerSparkNotebook(notebook.getUrl(), jobId, dividendNoteBookParam, month,
										assessmentYear, tenantId, deductorTan, userEmail);

								List<ActivityTracker> tracker = activityTrackerDAO
										.getActivityTrackerByTanYearTypeAndMonth(deductorTan, assessmentYear,
												"Dividend Calculation", month);
								if (!tracker.isEmpty()) {
									tracker.get(0).setStatus(ActivityTrackerStatus.VALIDATED.name());
									tracker.get(0).setModifiedBy(userEmail);
									tracker.get(0).setModifiedDate(new Date());
									activityTrackerDAO.save(tracker.get(0));
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
				triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), dividendNoteBookParam, month,
						assessmentYear, tenantId, deductorTan, userEmail);
			}
		}
		return batchUpload;
	}

	private BatchUpload saveBatchUpload(int assessmentYear, String userEmail, MultipartFile file, String path,
			int month, String sha256, List<BatchUpload> batch, boolean isDuplicate, String deductorTan,
			String fileType,String uploadedFilePath) throws ParseException {
		BatchUpload batchUpload;
		batchUpload = new BatchUpload();
		if (isDuplicate) {
			logger.info("Duplicate Record inserting : {}", file.getOriginalFilename());
			batchUpload.setStatus("Duplicate");
			batchUpload.setSha256sum(sha256);
			// batchUpload.setReferenceId(batch.get(0).getKey().getId());

		} else {
			logger.info("Unique record  inserting  : {}", file.getOriginalFilename());
			batchUpload.setStatus("Uploaded");
			batchUpload.setSha256sum(sha256);
		}
		batchUpload.setUploadType(fileType);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setFileName(file.getOriginalFilename());
		batchUpload.setAssessmentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
		batchUpload.setFilePath(path);
		batchUpload.setOtherFileUrl(uploadedFilePath);
		batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
		batchUpload.setCreatedBy(userEmail);
		batchUpload.setActive(true);
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessedCount(0);
		batchUpload.setDuplicateCount(0L);
		batchUpload.setMismatchCount(0L);
		batchUpload = batchUploadDAO.save(batchUpload);
		return batchUpload;
	}

	private DividendNoteBookParam createNoteBook(int assessmentYear, String tan, String tenantId, String type,
			String userEmail, int month, BatchUpload batchUpload, String dividendDeductorType, String deductorPan) {
		DividendNoteBookParam dividendNoteBookParam = new DividendNoteBookParam();
		dividendNoteBookParam.setDeductorTan(tan);
		dividendNoteBookParam.setAssessmentMonth(month);
		dividendNoteBookParam.setAssessmentYear(assessmentYear);
		if (batchUpload != null) {
			dividendNoteBookParam.setBatchId(batchUpload.getBatchUploadID());
			dividendNoteBookParam.setStatus(batchUpload.getStatus());
			dividendNoteBookParam.setType(batchUpload.getUploadType());
			if (StringUtils.isNotBlank(batchUpload.getSha256sum())) {
				dividendNoteBookParam.setSha256(batchUpload.getSha256sum());
			} else {
				dividendNoteBookParam.setSha256("");
			}
			if (StringUtils.isNotBlank(batchUpload.getFileName())) {
				dividendNoteBookParam.setFileName(batchUpload.getFileName());
			} else {
				dividendNoteBookParam.setFileName("");
			}
		}
		dividendNoteBookParam.setType(type);
		dividendNoteBookParam.setDeductorPan(deductorPan);
		dividendNoteBookParam.setTenantId(tenantId);
		dividendNoteBookParam.setUserEmail(userEmail);
		dividendNoteBookParam.setDividendDeductorType(dividendDeductorType);
		return dividendNoteBookParam;
	}

	public InvoiceShareholderResident approveOrModifyResidentInvoice(DividendInvoiceDTO invoiceDTO) {
		InvoiceShareholderResident invoiceShareholderResident = null;

		List<InvoiceShareholderResident> invoiceShareholderResidentOptional = invoiceShareholderResidentDAO
				.findByIdPan(invoiceDTO.getId(), invoiceDTO.getDeductorPan());
		if (!invoiceShareholderResidentOptional.isEmpty()) {
			if (invoiceDTO.getPayoutState().equals(DividendPayoutState.APPROVED)) {
				invoiceShareholderResident = invoiceShareholderResidentOptional.get(0);
				invoiceShareholderResident.setStringPayoutState(DividendPayoutState.APPROVED.name());
				invoiceShareholderResident.setShareholderActive(true);
				invoiceShareholderResident.setRemarks(invoiceDTO.getRemarks());
				invoiceShareholderResidentDAO.updateInvoiceShareholderResident(invoiceShareholderResident);
			}
			if (invoiceDTO.getPayoutState().equals(DividendPayoutState.MODIFIED)) {
				invoiceShareholderResident = invoiceShareholderResidentOptional.get(0);
				invoiceShareholderResident.modifyRate(invoiceDTO.getClientOverriddenRate());
				invoiceShareholderResident
						.setFinalDividendWithholding(invoiceShareholderResident.getClientOverriddenWithholding());
				invoiceShareholderResident.setStringPayoutState(DividendPayoutState.MODIFIED.name());
				invoiceShareholderResident.setClientOverriddenRate(invoiceDTO.getClientOverriddenRate());
				invoiceShareholderResident.setRemarks(invoiceDTO.getRemarks());
				invoiceShareholderResidentDAO.updateInvoiceShareholderResident(invoiceShareholderResident);
			}
			if (invoiceDTO.getPayoutState().equals(DividendPayoutState.APPROVED_MODIFIED)) {
				invoiceShareholderResident = invoiceShareholderResidentOptional.get(0);
				invoiceShareholderResident.modifyRate(invoiceDTO.getClientOverriddenRate());
				invoiceShareholderResident
						.setFinalDividendWithholding(invoiceShareholderResident.getClientOverriddenWithholding());
				invoiceShareholderResident.setStringPayoutState(DividendPayoutState.APPROVED.name());
				invoiceShareholderResident.setShareholderActive(true);
				invoiceShareholderResident.setClientOverriddenRate(invoiceDTO.getClientOverriddenRate());
				invoiceShareholderResident.setRemarks(invoiceDTO.getRemarks());
				invoiceShareholderResidentDAO.updateInvoiceShareholderResident(invoiceShareholderResident);
			}
			if (invoiceDTO.getPayoutState().equals(DividendPayoutState.REJECT)) {
				invoiceShareholderResident = invoiceShareholderResidentOptional.get(0);
				invoiceShareholderResident.setShareholderActive(false);
				invoiceShareholderResident.setRemarks(invoiceDTO.getRemarks());
				invoiceShareholderResidentDAO.updateInvoiceShareholderResident(invoiceShareholderResident);
			}
		}

		return invoiceShareholderResident;
	}

	public InvoiceShareholderNonResident approveOrModifyNonResidentInvoice(DividendInvoiceDTO invoiceDTO) {
		InvoiceShareholderNonResident invoiceShareholderNonResident = null;

		List<InvoiceShareholderNonResident> invoiceShareholderNonResidentOptional = invoiceShareholderNonResidentDAO
				.findByIdPan(invoiceDTO.getId(), invoiceDTO.getDeductorPan());
		if (!invoiceShareholderNonResidentOptional.isEmpty()) {
			if (invoiceDTO.getPayoutState().equals(DividendPayoutState.APPROVED)) {
				invoiceShareholderNonResident = invoiceShareholderNonResidentOptional.get(0);
				//invoiceShareholderNonResident.setPayoutState(DividendPayoutState.APPROVED);
				invoiceShareholderNonResident.setStringPayoutState(DividendPayoutState.APPROVED.name());
				invoiceShareholderNonResident.setShareholderActive(true);
				invoiceShareholderNonResident.setRemarks(invoiceDTO.getRemarks());
				invoiceShareholderNonResidentDAO.updateInvoiceShareholderNonResident(invoiceShareholderNonResident);
			}
			if (invoiceDTO.getPayoutState().equals(DividendPayoutState.MODIFIED)) {
				invoiceShareholderNonResident = invoiceShareholderNonResidentOptional.get(0);
				invoiceShareholderNonResident.modifyRate(invoiceDTO.getClientOverriddenRate());
				invoiceShareholderNonResident.setFinalDividendWithholdingDecimal(
						invoiceShareholderNonResident.getClientOverriddenWithholding());
				//invoiceShareholderNonResident.setPayoutState(DividendPayoutState.MODIFIED);
				invoiceShareholderNonResident.setStringPayoutState(DividendPayoutState.MODIFIED.name());
				invoiceShareholderNonResident.setClientOverriddenRate(invoiceDTO.getClientOverriddenRate());
				invoiceShareholderNonResident.setRemarks(invoiceDTO.getRemarks());
				invoiceShareholderNonResidentDAO.updateInvoiceShareholderNonResident(invoiceShareholderNonResident);
			}
			if (invoiceDTO.getPayoutState().equals(DividendPayoutState.APPROVED_MODIFIED)) {
				invoiceShareholderNonResident = invoiceShareholderNonResidentOptional.get(0);
				invoiceShareholderNonResident.modifyRate(invoiceDTO.getClientOverriddenRate());
				invoiceShareholderNonResident.setFinalDividendWithholdingDecimal(
						invoiceShareholderNonResident.getClientOverriddenWithholding());
				//invoiceShareholderNonResident.setPayoutState(DividendPayoutState.APPROVED);
				invoiceShareholderNonResident.setStringPayoutState(DividendPayoutState.APPROVED_MODIFIED.name());
				invoiceShareholderNonResident.setShareholderActive(true);
				invoiceShareholderNonResident.setClientOverriddenRate(invoiceDTO.getClientOverriddenRate());
				invoiceShareholderNonResident.setRemarks(invoiceDTO.getRemarks());
				invoiceShareholderNonResidentDAO.updateInvoiceShareholderNonResident(invoiceShareholderNonResident);
			}
		}

		return invoiceShareholderNonResident;
	}

	public List<InvoiceShareholderNonResident> getNonResidentShareholderfor15CB(String pan, String dateOfPosting,
			Integer year) throws ParseException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate dateTime = LocalDate.parse(dateOfPosting, formatter);
		return invoiceShareholderNonResidentDAO.findAllNonResidentByDateOfPosting(pan, dateTime, year);

	}

	public List<InvoiceShareholderNonResident> getNonResidentShareholderfor15CB(int assessmentYear)
			throws ParseException {
		return invoiceShareholderNonResidentDAO.findAllNonResidentByDateOfPosting(assessmentYear);

	}

	/**
	 * to perform batch update in InvoiceShareholderNonResident table
	 * 
	 * @param form15CBDetailsList
	 * @return
	 * @throws ParseException
	 */
	public List<InvoiceShareholderNonResident> updateNonResidentShareholderfor15CB(
			List<Form15CBDetails> form15CBDetailsList) throws ParseException {
		List<InvoiceShareholderNonResident> invoiceShareholderNonResidentListToUpdate = new ArrayList<>();
		for (Form15CBDetails form15CBDetails : form15CBDetailsList) {
			List<InvoiceShareholderNonResident> invoiceShareholderNonResidentOptional = invoiceShareholderNonResidentDAO
					.findByIdPan(form15CBDetails.getInvoiceId(), form15CBDetails.getDeductorPan());
			logger.info("Retrieved InvoiceShareholderNonResident records with count ="
					+ invoiceShareholderNonResidentOptional.size() + "{}");
			if (invoiceShareholderNonResidentOptional.isEmpty()) {
				InvoiceShareholderNonResident invoiceShareholderNonResidentEntity = invoiceShareholderNonResidentOptional
						.get(0);
				invoiceShareholderNonResidentEntity.setForm15CBAcknowledgementNo(form15CBDetails.getAckNum());
				invoiceShareholderNonResidentEntity
						.setForm15CBGenartionDate(convertDateFormat(form15CBDetails.getAckDate()));
				invoiceShareholderNonResidentListToUpdate.add(invoiceShareholderNonResidentEntity);
			}
		}
		if (invoiceShareholderNonResidentListToUpdate.size() > 0)
			invoiceShareholderNonResidentDAO
					.batchUpdateInvoiceShareholderNonResident(invoiceShareholderNonResidentListToUpdate);
		return invoiceShareholderNonResidentListToUpdate;
	}

	/**
	 * updating invoiceShareholderNonResidentOptional using id
	 * 
	 * @param deductorPan
	 * @param id
	 * @throws ParseException
	 */
	public void updateNonResidentShareholder15CBFlag(String deductorPan, Integer id) throws ParseException {
		List<InvoiceShareholderNonResident> invoiceShareholderNonResidentOptional = invoiceShareholderNonResidentDAO
				.findByIdPan(id, deductorPan);
		if (!invoiceShareholderNonResidentOptional.isEmpty()) {
			InvoiceShareholderNonResident invoiceShareholderNonResidentEntity = invoiceShareholderNonResidentOptional
					.get(0);
			invoiceShareholderNonResidentEntity.setIfForm15CBgenerated(Boolean.TRUE);
			invoiceShareholderNonResidentEntity.setForm15CBAcknowledgementNo("NA");
			invoiceShareholderNonResidentDAO.updateInvoiceShareholderNonResident(invoiceShareholderNonResidentEntity);
		}
	}

	public Set<String> getDateOfPostingList(String pan, Integer assesmentYear) {
		List<LocalDate> listOfDistintKey = invoiceShareholderNonResidentDAO.findDistinctDate(pan);
		Set<String> stringSet = new HashSet<>();
		Integer pastYear = assesmentYear - 1;
		List<String> validMonthsForFinancialYear = new ArrayList<>();
		validMonthsForFinancialYear.add("01");
		validMonthsForFinancialYear.add("02");
		validMonthsForFinancialYear.add("03");
		List<String> validMonthsForPastYear = new ArrayList<>();
		validMonthsForPastYear.add("04");
		validMonthsForPastYear.add("05");
		validMonthsForPastYear.add("06");
		validMonthsForPastYear.add("07");
		validMonthsForPastYear.add("08");
		validMonthsForPastYear.add("09");
		validMonthsForPastYear.add("10");
		validMonthsForPastYear.add("11");
		validMonthsForPastYear.add("12");
		for (LocalDate localDate : listOfDistintKey) {
			String format = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String year = format.substring(0, 4);
			String month = format.substring(5, 7);
			if (year.equalsIgnoreCase(assesmentYear.toString()) && validMonthsForFinancialYear.contains(month))
				stringSet.add(format);

			if (year.equalsIgnoreCase(pastYear.toString()) && validMonthsForPastYear.contains(month))
				stringSet.add(format);
		}
		return stringSet;
	}

	public List<InvoiceShareholderResident> getResidentShareholderfor15GH(String tan, String quarter,
			int assessmentYear) {

		List<InvoiceShareholderResident> allResidentByQuarter = new ArrayList<>();
		if (quarter.equalsIgnoreCase("Q1")) {
			allResidentByQuarter = invoiceShareholderResidentDAO.findAllResidentByQuarter(tan, assessmentYear, 4, 5, 6);
		} else if (quarter.equalsIgnoreCase("Q2")) {
			allResidentByQuarter = invoiceShareholderResidentDAO.findAllResidentByQuarter(tan, assessmentYear, 7, 8, 9);

		} else if (quarter.equalsIgnoreCase("Q3")) {
			allResidentByQuarter = invoiceShareholderResidentDAO.findAllResidentByQuarter(tan, assessmentYear, 10, 11,
					12);

		} else if (quarter.equalsIgnoreCase("Q4")) {
			allResidentByQuarter = invoiceShareholderResidentDAO.findAllResidentByQuarter(tan, assessmentYear, 1, 2, 3);

		} else {
			throw new CustomException("Invalid quarter");
		}
		return allResidentByQuarter;
	}

	public String triggerSparkNotebook(String api, int jobId, DividendNoteBookParam noteBookParam, int month,
			int assessmentYear, String tenantId, String tan, String userEmail) throws JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
// Made the Databricks Key Dynamic
		headers.add("Authorization", "Bearer " + this.dataBricksKey);
		headers.add("Content-Type", "application/json");
		RestTemplate restTemplate = new RestTemplate();
		DividendJob dividendJob = new DividendJob();

		dividendJob.setJob_id(jobId);
		dividendJob.setNotebook_params(noteBookParam);

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.writeValueAsString(dividendJob);
		if (logger.isInfoEnabled()) {
			logger.info("Note Book Object : {}", objMapper.writeValueAsString(dividendJob));
		}
		logger.info(dividendJob.getJob_id().toString());
		logger.info("token : {}", this.dataBricksKey);

		String dataBricks = api;
		HttpEntity<DividendJob> entity = new HttpEntity<>(dividendJob, headers);
		String response = restTemplate.postForObject(dataBricks, entity, String.class);

		logger.info("Response : {}", response);
		return response;
	}

	public static Date convertDateFormat(String inputDate) throws ParseException {
		DateFormat originalFormat = new SimpleDateFormat(AckDate_Input_Format);
		String dateAfterAlteration = inputDate.concat(" 16:00:00");
		originalFormat.setTimeZone(TimeZone.getDefault());
		Date dateAfterChange = originalFormat.parse(dateAfterAlteration);
		return dateAfterChange;
	}

	public List<DeducteeDetailDTO> getResidentShareholders(String receiptSerailNo, String bsrCode, String receiptDate,
			Integer assessmentYear, String tan) {
		return invoiceShareholderResidentDAO.findByReceiptDetails(receiptSerailNo, bsrCode, receiptDate, assessmentYear,
				tan);
	}

	public List<DeducteeDetailDTO> getNonResidentShareholders(String receiptSerailNo, String bsrCode,
			String receiptDate, Integer assessmentYear, String tan) {
		return invoiceShareholderNonResidentDAO.findByReceiptDetails(receiptSerailNo, bsrCode, receiptDate,
				assessmentYear, tan);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public File convertIvoiceShareHolderCsvToXlsx(File csvFile, String deductorTan, String tenantId, String deductorPan,
			Integer assesmentyear, Integer batchId) throws Exception {

		List<InvoiceShareholderNonResident> errorRecords = null;
		CsvToBean<InvoiceShareHolderErrorReportCsvDTO> csvToBean = null;
		List<CsvRow> listCsvRows=null;

		logger.info("Retrieveing GL error records from DB {}");

		// TODO make DB call to get error records from DB
		errorRecords = new ArrayList<>();
		logger.info("Number of GL error records retrieved is " + errorRecords.size() + "{}");
			CsvReader csvReader = new CsvReader();
			csvReader.setContainsHeader(true);
			CsvContainer csv = csvReader.read(csvFile, StandardCharsets.UTF_8);
		   listCsvRows = csv.getRows();
		SXSSFWorkbook workbook = invoiceShareHolderXlsxReportWithSXSSFWorkbook(listCsvRows, errorRecords,
				deductorTan, tenantId, deductorPan);
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.write(baout);
		File xlsxInvoiceFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()) + "_Error_Report",
				".xlsx");

		FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
		baout.close();
		xlsxInvoiceFile.deleteOnExit();
		return xlsxInvoiceFile;
	}

	public Workbook invoiceShareHolderXlsxReport(List<InvoiceShareHolderErrorReportCsvDTO> glErrorReportsCsvList,
			List<InvoiceShareholderNonResident> errorRecords, String tan, String tenantId, String deductorPan)
			throws Exception {

		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		worksheet.getCells().importArray(ErrorReportService.shareHolderHeaderNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (errorRecords.isEmpty()) {
			setExtractDataForInvoiceShareHolder(glErrorReportsCsvList, worksheet,tan);
			logger.info("Sheet is prepared sucessfully{}");
		} else {
			// setExtractDataForGlWithRecordsFromDB(errorRecords, worksheet,
			// tan,deductorData.getDeductorName());
		}

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		style1.setTextWrapped(true);
		Range headerColorRange1 = worksheet.getCells().createRange("A6:B6");
		headerColorRange1.setStyle(style1);

		Cell cellD6 = worksheet.getCells().get("C6");
		Style styleD6 = cellD6.getStyle();
		styleD6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleD6.setPattern(BackgroundType.SOLID);
		styleD6.getFont().setBold(true);
		styleD6.getFont().setColor(Color.getWhite());
		styleD6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellD6.setStyle(styleD6);

		// Style for D6 to BI6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		style2.setTextWrapped(true);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:BZ6");
		headerColorRange2.setStyle(style2);

		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);
		worksheet.getCells().setRowHeight(5, 45);
		worksheet.getCells().setStandardWidth(24.5);
		worksheet.getCells().setColumnWidth(1, 60.5);
		worksheet.getCells().setColumnWidth(2, 18);
		worksheet.getCells().setColumnWidth(4, 18);

		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("Invoice ShareHolder Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		cellA2.setValue("Client Name:" + deductorData.getDeductorName());
		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setBold(true);
		a2Style.getFont().setSize(11);
		cellA2.setStyle(a2Style);

		// column B2 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information codes");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, "A6", "BZ6", "A7", "BZ"+glErrorReportsCsvList.size()+7);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:BZ6");

		return workbook;
	}

	private void setExtractDataForInvoiceShareHolder(List<InvoiceShareHolderErrorReportCsvDTO> errorReportsCsvList,
			Worksheet worksheet, String tan) throws Exception {

		logger.info("Preparing error report from CSV file {}");
		if (!errorReportsCsvList.isEmpty()) {
			int rowIndex = 6;
			int index = 0;
			for (InvoiceShareHolderErrorReportCsvDTO errorReportsDTO : errorReportsCsvList) {
				index++;
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add(tan); // deductor tan
				rowData.add(nullCheck(errorReportsDTO.getErrorReason()));
				rowData.add(index + "");// Sequence Number
				rowData.add(nullCheck(errorReportsDTO.getFolioNumber()));
				rowData.add(nullCheck(errorReportsDTO.getUniqueIdentificationNumber()));
				rowData.add(nullCheck(errorReportsDTO.getSalutation()));
				rowData.add(nullCheck(errorReportsDTO.getShareHolderName()));
				rowData.add(nullCheck(errorReportsDTO.getShareHolderCataory()));
				rowData.add(nullCheck(errorReportsDTO.getTypeOfShareHolder()));
				rowData.add(nullCheck(errorReportsDTO.getKeyShareHolder()));
				rowData.add(nullCheck(errorReportsDTO.getAadharNumber()));
				rowData.add(nullCheck(errorReportsDTO.getPanOfShareHolder()));
				rowData.add(nullCheck(errorReportsDTO.getTaxIdentificationNumber()));
				rowData.add(nullCheck(errorReportsDTO.getResidentialStatus()));
				rowData.add(nullCheck(errorReportsDTO.getFlatDoorBlockNo()));
				rowData.add(nullCheck(errorReportsDTO.getNameOfPreises()));
				rowData.add(nullCheck(errorReportsDTO.getRoadStreet()));
				rowData.add(nullCheck(errorReportsDTO.getAreaLocaality()));
				rowData.add(nullCheck(errorReportsDTO.getTownCityDistrict()));
				rowData.add(nullCheck(errorReportsDTO.getState()));
				rowData.add(nullCheck(errorReportsDTO.getPinOrZip()));
				rowData.add(nullCheck(errorReportsDTO.getCountry()));
				rowData.add(nullCheck(errorReportsDTO.getCountryToWhichRemitanceIsMade()));
				rowData.add(nullCheck(errorReportsDTO.getPrinciplePlaceOfBusiness()));
				rowData.add(nullCheck(errorReportsDTO.getShareTransferAgentName()));
				rowData.add(nullCheck(errorReportsDTO.getDeamatAcountNumber()));
				rowData.add(nullCheck(errorReportsDTO.getTotalNumberOfShareHeld()));
				rowData.add(nullCheck(errorReportsDTO.getPercentageOfShareHeld()));
				rowData.add(nullCheck(errorReportsDTO.getDurationOfShareHeldFromDate()));
				rowData.add(nullCheck(errorReportsDTO.getDurationOfShareHeldToDate()));
				rowData.add(nullCheck(errorReportsDTO.getPercentageOfDividendDeclared()));
				rowData.add(nullCheck(errorReportsDTO.getDateOfPosting()));
				rowData.add(nullCheck(errorReportsDTO.getDateOfDistribution()));
				rowData.add(nullCheck(errorReportsDTO.getProposedDateOfRemmitance()));
				rowData.add(nullCheck(errorReportsDTO.getDividendWarrantNumber()));
				rowData.add(nullCheck(errorReportsDTO.getAmountOfDividend()));
				
				rowData.add(nullCheck(errorReportsDTO.getDividendAmountInLastTransaction()));
				rowData.add(nullCheck(errorReportsDTO.getTdaRateAppliedInInLastTransaction()));
				
				rowData.add(nullCheck(errorReportsDTO.getAmountOfDividendForeignCcurrency()));
				rowData.add(nullCheck(errorReportsDTO.getAggregateAmountOfRemitance()));
				rowData.add(nullCheck(errorReportsDTO.getActualAmountAmountOfRemitanceafterTds()));
				rowData.add(nullCheck(errorReportsDTO.getInCaseTheRemittanceisNetOfTaxes()));
				rowData.add(nullCheck(errorReportsDTO.getIsForm15GHAvailable()));
				rowData.add(nullCheck(errorReportsDTO.getDateOfBirth()));
				rowData.add(nullCheck(errorReportsDTO.getForm15GHUIN()));
				rowData.add(nullCheck(errorReportsDTO.getFilingType()));
				rowData.add(nullCheck(errorReportsDTO.getAcknowledgementNumber()));
				rowData.add(nullCheck(errorReportsDTO.getRecordType()));
				rowData.add(nullCheck(errorReportsDTO.getDateOnWhichDeclarationIsRecieved()));
				rowData.add(nullCheck(errorReportsDTO.getWhetherAssessedToTaxUnderTheIncomeTaxAct1961()));
				rowData.add(nullCheck(errorReportsDTO.getIfAnswerToPreviousColumnYes()));
				rowData.add(nullCheck(errorReportsDTO.getEstimatedIncomeForWhichThisDeclarationIsMade()));
				rowData.add(nullCheck(errorReportsDTO.getEstimatedTotalIncomeOfThePY()));
				rowData.add(nullCheck(errorReportsDTO.getIsTaxResidencyCertificateAvailable()));
				rowData.add(nullCheck(errorReportsDTO.getTrcAvailableFromDate()));
				rowData.add(nullCheck(errorReportsDTO.getTrcAvailableToDate()));
				rowData.add(nullCheck(errorReportsDTO.getIsForm10FAvailable()));
				rowData.add(nullCheck(errorReportsDTO.getForm10FAvailableFromDate()));
				rowData.add(nullCheck(errorReportsDTO.getForm10FAvailableToDate()));
				rowData.add(nullCheck(errorReportsDTO.getIsThereAPermanentEstablishmentPeInIndia()));
				rowData.add(nullCheck(errorReportsDTO.getIsNoPEDeclarationAvailable()));
				rowData.add(nullCheck(errorReportsDTO.getPeriodOfNoPEDeclarationFromDate()));
				rowData.add(nullCheck(errorReportsDTO.getPeriodOfNoPEDeclarationToDate()));
				rowData.add(nullCheck(errorReportsDTO.getIsThereAPlaceOfEffectiveManagementOfShareHolder()));
				rowData.add(nullCheck(errorReportsDTO.getIsNoPOEMDeclarationAavailable()));
				rowData.add(nullCheck(errorReportsDTO.getPeriodOfNoPEDeclarationFromDate()));
				rowData.add(nullCheck(errorReportsDTO.getPeriodOfNoPEDeclarationToDate()));
				rowData.add(nullCheck(errorReportsDTO.getIsMultiLateralInstrmentPrinciplePurposeTest()));
				rowData.add(nullCheck(errorReportsDTO.getBenefecialOwnerIncome()));
				rowData.add(nullCheck(errorReportsDTO.getIsBenefecialOwnershipDeclarationAvailable()));
				rowData.add(nullCheck(errorReportsDTO.getIsTheTransactionGaarCompalint()));
				rowData.add(nullCheck(errorReportsDTO.getWheatherCommercialIdentityAvailable()));
				rowData.add(nullCheck(errorReportsDTO.getIfCountryOfShareHolderIsKuwait()));
				rowData.add(nullCheck(errorReportsDTO.getIfTheShareHolderIsAnInvestmentVehicleFromUK()));
				rowData.add(nullCheck(errorReportsDTO.getIfTheShareHolderIsACompany()));
				rowData.add(nullCheck(errorReportsDTO.getIsDividevdTaxableAtARateLower()));
				rowData.add(nullCheck(errorReportsDTO.getForm15CACBppliccable()));
				rowData.add(nullCheck(errorReportsDTO.getRemarks()));
			
				worksheet.getCells().setRowHeight(rowIndex, 25);
				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
				

			}
		}
	}

	// for null check
	public String nullCheck(String data) {
		return StringUtils.isBlank(data) ? StringUtils.EMPTY : data;
	}

	/**
	 * to generate the dividend summery report asynchronusly
	 * 
	 * @param deductorPan
	 * @param residentType
	 * @param year
	 * @throws Exception
	 */
	public void dividendDownloadSummary(String deductorPan, String residentType, Integer year, String tan,String userName)
			throws Exception {

		BatchUpload batchupload = new BatchUpload();
		batchupload.setDeductorMasterTan(tan);
		batchupload.setAssessmentYear(year);
		batchupload.setActive(true);
		batchupload.setAssessmentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
		batchupload.setStatus("Processing");
		batchupload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
		batchupload.setUploadType(
				residentType.equals("nonresident") ? "NR_DOWNLOAD_SUMMERY" : "RESIDENT_DOWNLOAD_SUMMERY");
		batchupload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
		batchupload.setCreatedBy(userName);
		batchupload = batchUploadDAO.save(batchupload);
		invoiceShareHolderBulkService.AsyncDownloadDividendSummeryReport(batchupload, residentType, deductorPan, year,
				MultiTenantContext.getTenantId());
	}

	public void dividendLiabilityReport(String deductorPan, Integer year, String tan,String level) throws Exception {
		BatchUpload batchupload = new BatchUpload();
		batchupload.setDeductorMasterTan(tan);
		batchupload.setAssessmentYear(year);
		batchupload.setActive(true);
		batchupload.setAssessmentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
		batchupload.setStatus("Processing");
		batchupload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
		batchupload.setUploadType("DIVIDEND_LIABILITY_REPORT");
		batchupload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
		batchupload = batchUploadDAO.save(batchupload);
		invoiceShareHolderBulkService.asyncDownloadDividendLiabilityReport(batchupload, deductorPan, year,
				MultiTenantContext.getTenantId(),level);
	}

	private BatchUpload updateBatchUpload(Integer runId, BatchUpload batchUpload) {
		batchUpload.setRunId(runId);
		batchUpload = batchUploadDAO.update(batchUpload);
		return batchUpload;
	}

	protected BatchUpload savesUmmeryReport(String deductorTan, String tenantId, int assessmentYear,
			ByteArrayOutputStream out, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId) throws FileNotFoundException, IOException, URISyntaxException, InvalidKeyException,
			StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String path = null;
		String fileName = null;
		if (out != null) {
			fileName = uploadType.toLowerCase() + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
			File file = getConvertedExcelFile(out.toByteArray(), fileName);
			path = blob.uploadExcelToBlobWithFile(file, tenantId);
			logger.info(uploadType + " report {} completed for : {}", uploadType, userName);
		} else {
			logger.info(uploadType + " report {} started for : {}", uploadType, userName);
		}
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setBatchUploadID(batchId);
		batchUpload.setActive(true);
		List<BatchUpload> response = null;
		if (batchId != null) {
			response = batchUploadDAO.findById(assessmentYear, deductorTan, uploadType, batchId);
		}
		if (response != null && !response.isEmpty()) {
			batchUpload = response.get(0);
			if (out != null) {
				batchUpload.setModifiedDate(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setFileName(fileName);
				batchUpload.setStatus("Processed");
				batchUpload.setFilePath(path);

			} else {
				batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setCreatedBy(userName);
				batchUpload.setModifiedDate(null);
				batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setRowsCount(0l);
			}
			batchUpload.setModifiedBy(userName);
			return batchUploadDAO.update(batchUpload);
		} else {
			batchUpload.setAssessmentYear(assessmentYear);
			batchUpload.setDeductorMasterTan(deductorTan);
			batchUpload.setUploadType(uploadType);
			batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedBy(userName);
		}
		batchUpload.setFileName(fileName);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		// do not update count with 0 for async reports
		// batchUpload.setProcessedCount(0);
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		return batchUploadDAO.save(batchUpload);
	}

	protected File getConvertedExcelFile(byte[] byteArray, String fileName) throws FileNotFoundException, IOException {
		File someFile = new File(fileName);
		try (FileOutputStream fos = new FileOutputStream(someFile)) {
			fos.write(byteArray);
			fos.flush();
			fos.close();
			return someFile;
		}
	}

	private File convertingToCSV(MultipartFile file) throws FileNotFoundException, IOException {
		File f = new File(file.getOriginalFilename());
		File glCsvFile = null;
		try (OutputStream os = new FileOutputStream(f)) {
			os.write(file.getBytes());
		}
		try {
			Workbook workbook = new Workbook(f.getAbsolutePath());
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.CSV);
			glCsvFile = new File(FilenameUtils.removeExtension(f.getName()) + ".csv");

			FileUtils.writeByteArrayToFile(glCsvFile, baout.toByteArray());
		} catch (Exception e) {
			logger.error("Error ocuured while converting xlsx to csv", e);
		}
		return glCsvFile;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public File generateSummeryErrorReport(File file, String deductorTan,String tenantId,String deductorPan,Integer assessmentYear,Integer batchId) throws Exception {
		
		Reader reader = null;
		CsvToBean<SummeryReportErrorCSVDTO> csvToBean = null;
		List<SummeryReportErrorCSVDTO> list=null;
		
		// feign client to get Deductor Name
				ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
						tenantId);
				DeductorMasterDTO deductorData = getDeductor.getBody().getData();
		
		reader = new FileReader(file);
		csvToBean = new CsvToBeanBuilder(reader).withType(SummeryReportErrorCSVDTO.class)
				.withIgnoreLeadingWhiteSpace(true).build();
		if(csvToBean!=null) {
			list=csvToBean.parse();
		}
		
		
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		worksheet.getCells().importArray(new String[] {"Serial No","Transaction Id","Error Decsription"}, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);
		
		if (!list.isEmpty()) {
			int rowIndex = 6;
			int index = 0;
			for (SummeryReportErrorCSVDTO errorReportsDTO : list) {
				index++;
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add(index+"");
				rowData.add(nullCheck(errorReportsDTO.getTransactionId())); 
				rowData.add(nullCheck(errorReportsDTO.getErrorDescription()));
				worksheet.getCells().setRowHeight(rowIndex, 25);
				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}
		
		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("Invoice ShareHolder Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		cellA2.setValue("Client Name:" + deductorData.getDeductorName());
		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setBold(true);
		a2Style.getFont().setSize(11);
		cellA2.setStyle(a2Style);

		// column B2 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information codes");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);
		
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("B6:C6");
		headerColorRange1.setStyle(style1);
		
		Cell cellD6 = worksheet.getCells().get("A6");
		Style styleD6 = cellD6.getStyle();
		styleD6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleD6.setPattern(BackgroundType.SOLID);
		styleD6.getFont().setBold(true);
		styleD6.getFont().setColor(Color.getWhite());
		styleD6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellD6.setStyle(styleD6);
		
		worksheet.setGridlinesVisible(false);
		worksheet.getCells().setRowHeight(5, 45);
		worksheet.getCells().setStandardWidth(24.5);
		worksheet.getCells().setColumnWidth(0, 20);
		worksheet.getCells().setColumnWidth(1, 40);
		worksheet.getCells().setColumnWidth(2, 70);
		
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:C6");
		
		CommonUtil.setBoardersForAsposeXlsx(worksheet, "A6", "C6", "A7", "C"+list.size()+7);
		
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxInvoiceFile = File.createTempFile(FilenameUtils.getBaseName(file.getName()) + "_Error_Report",
				".xlsx");

		FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
		baout.close();
		xlsxInvoiceFile.deleteOnExit();
		return xlsxInvoiceFile;
	}
	
	 public Map<String, Integer> getActiveAndInactiveShareHolderCounts(String deductorPan, String type) {
			Map<String, Integer> deducteeCounts = null;
			if ("resident".equalsIgnoreCase(type)) {
				deducteeCounts = invoiceShareholderResidentDAO.getActiveAndInactiveResidentShareHoldersCounts(deductorPan, type);
			} else {
				deducteeCounts = invoiceShareholderNonResidentDAO.getActiveAndInactiveNonResidentShareHoldersCounts(deductorPan,
						type);
			}

			return deducteeCounts;
		}
	 
	 public SXSSFWorkbook invoiceShareHolderXlsxReportWithSXSSFWorkbook(List<CsvRow> listCsvRows,
				List<InvoiceShareholderNonResident> errorRecords, String tan, String tenantId, String deductorPan)
				throws Exception {

			// feign client to get Deductor Name
			ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
					tenantId);
			DeductorMasterDTO deductorData = getDeductor.getBody().getData();
			logger.info("Feign call succeded to get deductor info {}");
			
			SXSSFWorkbook workbook = new SXSSFWorkbook();
			SXSSFSheet worksheet = workbook.createSheet("Error Details");
			worksheet.setDisplayGridlines(false);
			DefaultIndexedColorMap defaultIndexedColorMap=new DefaultIndexedColorMap();
			
			//creating font with bold
			Font font = workbook.createFont();
			font.setBold(true);
			font.setColor(IndexedColors.BLACK.getIndex());
			font.setFontHeightInPoints((short) 11);
			
			//creating font with bit large font
			Font font2 = workbook.createFont();
			font2.setBold(true);
			font2.setColor(IndexedColors.BLACK.getIndex());
			font2.setFontHeightInPoints((short) 16);
			
			
			// Style for A5 to B5 headers
			XSSFCellStyle style=(XSSFCellStyle) workbook.createCellStyle();
			font.setBold(true);
			style.setFont(font);
			style.setWrapText(true);
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style.setAlignment(HorizontalAlignment.CENTER);
			style.setVerticalAlignment(VerticalAlignment.CENTER);
			style.setFillForegroundColor(new XSSFColor(new java.awt.Color(169, 209, 142),defaultIndexedColorMap ));
			style.setBorderBottom(BorderStyle.MEDIUM);
			style.setBorderLeft(BorderStyle.MEDIUM);
			style.setBorderRight(BorderStyle.MEDIUM);
			style.setBorderTop(BorderStyle.MEDIUM);
			
			//style for rest of the headers
			XSSFCellStyle style2=(XSSFCellStyle) workbook.createCellStyle();
			style2.setFont(font);
			style2.setWrapText(true);
			style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style2.setAlignment(HorizontalAlignment.CENTER);
			style2.setVerticalAlignment(VerticalAlignment.CENTER);
			style2.setFillForegroundColor(new XSSFColor(new java.awt.Color(252, 199, 155),defaultIndexedColorMap ));
			style2.setBorderBottom(BorderStyle.MEDIUM);
			style2.setBorderLeft(BorderStyle.MEDIUM);
			style2.setBorderRight(BorderStyle.MEDIUM);
			style2.setBorderTop(BorderStyle.MEDIUM);
			
			//style with blue color
			XSSFCellStyle style3=(XSSFCellStyle) workbook.createCellStyle();
			style3.setFont(font);
			style3.setWrapText(true);
			style3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style3.setAlignment(HorizontalAlignment.CENTER);
			style3.setVerticalAlignment(VerticalAlignment.CENTER);
			style3.setFillForegroundColor(new XSSFColor(new java.awt.Color(180, 199, 231),defaultIndexedColorMap ));
			
			//style with no color 
			XSSFCellStyle style4=(XSSFCellStyle) workbook.createCellStyle();
			style4.setFont(font2);
			
			//preparing header message and printing to file
			TimeZone.setDefault(TimeZone.getTimeZone("IST"));
			String pattern = "dd-MM-yyyy hh:mm aaa";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			String date = simpleDateFormat.format(new Date());
			String errorReportHeaderMessage="Invoice ShareHolder Error Report (Dated: " + date + ") \n"+"Client Name:" + deductorData.getDeductorName();
			worksheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 7));
			worksheet.createRow(0).createCell(0).setCellValue(errorReportHeaderMessage);
			worksheet.getRow(0).getCell(0).setCellStyle(style4);
			
			
			worksheet.createRow(3).createCell(1).setCellValue("Error/Information codes");
			worksheet.getRow(3).getCell(1).setCellStyle(style3);
			
			//Preparing header of error report
			logger.info("Preparing header of error report {}");
			AtomicInteger index=new AtomicInteger();
			SXSSFRow headerRow=worksheet.createRow(4);
			Stream.of(ErrorReportService.shareHolderHeaderNames).forEach(header->{
				if(index.get()==1 || index.get()==0) {
				headerRow.createCell(index.get()).setCellValue(header);
				headerRow.getCell(index.getAndAdd(1)).setCellStyle(style);
				}else {
					headerRow.createCell(index.get()).setCellValue(header);
					headerRow.getCell(index.getAndAdd(1)).setCellStyle(style2);
				}
			});
			logger.info("Header Row is Prepared SucessFully{}");
			
			if (errorRecords.isEmpty()) {
				setExtractDataForInvoiceShareHolderWithCSVRow(listCsvRows, worksheet, tan, workbook);
				logger.info("Data rows Are prepared sucessfully{}");
			} else {
				// setExtractDataForGlWithRecordsFromDB(errorRecords, worksheet,
				// tan,deductorData.getDeductorName());
			}
			
			worksheet.setDefaultColumnWidth(35);
			worksheet.setColumnWidth(5, 14*256);
			worksheet.setColumnWidth(0, 16*256);
			worksheet.setColumnWidth(1, 55*256);
			worksheet.setColumnWidth(2, 14*256);
			worksheet.setDefaultRowHeightInPoints((float) (worksheet.getDefaultRowHeightInPoints()*(1.6)));
			worksheet.setAutoFilter(new CellRangeAddress(4, 4, 0, 77));
			logger.info("Sheet is prepared sucessfully{}");
			return workbook;
			
		}
		

		private void populateValueToCell(SXSSFRow row,int cellIndex,XSSFCellStyle style,String value) {
			row.createCell(cellIndex).setCellValue(value);
			row.getCell(cellIndex).setCellStyle(style);
		}
		
		private void setExtractDataForInvoiceShareHolderWithCSVRow(List<CsvRow> listCsvRows,
				SXSSFSheet worksheet, String tan,SXSSFWorkbook workbook) throws Exception {

			logger.info("Preparing error report from CSV file {}");
			
			Font font = workbook.createFont();
			font.setBold(true);
			font.setColor(IndexedColors.BLACK.getIndex());
			font.setFontHeightInPoints((short) 9);
			
			XSSFCellStyle style=(XSSFCellStyle) workbook.createCellStyle();
			font.setBold(false);
			style.setFont(font);
			style.setWrapText(true);
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style.setAlignment(HorizontalAlignment.LEFT);
			style.setVerticalAlignment(VerticalAlignment.CENTER);
			style.setFillForegroundColor(IndexedColors.WHITE.index);
			style.setBorderBottom(BorderStyle.THIN);
			style.setBorderLeft(BorderStyle.THIN);
			style.setBorderRight(BorderStyle.THIN);
			style.setBorderTop(BorderStyle.THIN);
			
			AtomicInteger rowIndex=new AtomicInteger(5);
			AtomicInteger index=new AtomicInteger();
			listCsvRows.stream().forEach(errorRecord->{
				SXSSFRow row=worksheet.createRow(rowIndex.getAndAdd(1));
				index.addAndGet(1);
				populateValueToCell(row, 0, style, nullCheck(tan));
				populateValueToCell(row, 1, style, nullCheck(errorRecord.getField("Error Description")).trim());
				populateValueToCell(row, 2, style, index.get() + "");
				populateValueToCell(row, 3, style, nullCheck(errorRecord.getField("FOLIO NUMBER")));
				populateValueToCell(row, 4, style, nullCheck(errorRecord.getField("UNIQUE SHAREHOLDER IDENTIFICATION NUMBER")));
				populateValueToCell(row, 5, style, nullCheck(errorRecord.getField("SALUTATION")));
				populateValueToCell(row, 6, style, nullCheck(errorRecord.getField("SHAREHOLDER NAME")));
				populateValueToCell(row, 7, style, nullCheck(errorRecord.getField("CATEGORY OF SHAREHOLDER")));
				populateValueToCell(row, 8, style, nullCheck(errorRecord.getField("TYPE OF SHAREHOLDER")));
				populateValueToCell(row, 9, style, nullCheck(errorRecord.getField("KEY SHAREHOLDER (AFFILIATED/ RELATED ENTITY)")));
				populateValueToCell(row, 10, style, nullCheck(errorRecord.getField("AADHAR NUMBER")));
				populateValueToCell(row, 11, style, nullCheck(errorRecord.getField("PAN OF SHAREHOLDER")));
				populateValueToCell(row, 12, style, nullCheck(errorRecord.getField("TAX IDENTIFICATION NUMBER (NON-RESIDENT)")));
				populateValueToCell(row, 13, style, nullCheck(errorRecord.getField("RESIDENTIAL_STATUS")));
				populateValueToCell(row, 14, style, nullCheck(errorRecord.getField("FLAT/ DOOR/ BUILDING NUMBER")));
				populateValueToCell(row, 15, style, nullCheck(errorRecord.getField("NAME OF PREMISES/ BUILDING/ VILLAGE")));
				populateValueToCell(row, 16, style, nullCheck(errorRecord.getField("ROAD/ STREET")));
				populateValueToCell(row, 17, style, nullCheck(errorRecord.getField("AREA/ LOCALITY")));
				populateValueToCell(row, 18, style, nullCheck(errorRecord.getField("TOWN/ CITY/ DISTRICT")));
				populateValueToCell(row, 19, style, nullCheck(errorRecord.getField("STATE")));
				populateValueToCell(row, 20, style, nullCheck(errorRecord.getField("PIN/ ZIP CODE")));
				populateValueToCell(row, 21, style, nullCheck(errorRecord.getField("COUNTRY")));
				populateValueToCell(row, 22, style, nullCheck(errorRecord.getField("COUNTRY TO WHICH REMITTANCE IS MADE")));
				populateValueToCell(row, 23, style, nullCheck(errorRecord.getField("PRINCIPAL PLACE OF BUSINESS")));
				populateValueToCell(row, 24, style, nullCheck(errorRecord.getField("SHARE TRANSFER AGENT NAME")));
				populateValueToCell(row, 25, style, nullCheck(errorRecord.getField("DEMAT ACCOUNT NUMBER")));
				populateValueToCell(row, 26, style, nullCheck(errorRecord.getField("TOTAL NUMBER OF SHARES HELD")));
				populateValueToCell(row, 27, style, nullCheck(errorRecord.getField("PERCENTAGE OF SHARES HELD ")));
				populateValueToCell(row, 28, style, nullCheck(errorRecord.getField("DURATION OF SHARES HELD (FROM DATE) YYYY-MM-DD")));
				populateValueToCell(row, 29, style, nullCheck(errorRecord.getField("DURATION OF SHARES HELD (TO DATE) YYYY-MM-DD")));
				populateValueToCell(row, 30, style, nullCheck(errorRecord.getField("PERCENTAGE OF DIVIDEND DECLARED")));
				populateValueToCell(row, 31, style, nullCheck(errorRecord.getField("DATE OF POSTING YYYY-MM-DD")));
				populateValueToCell(row, 32, style, nullCheck(errorRecord.getField("DATE OF DISTRIBUTION/ PAYMENT OF DIVIDEND YYYY-MM-DD")));
				populateValueToCell(row, 33, style, nullCheck(errorRecord.getField("PROPOSED DATE OF REMMITANCE YYYY-MM-DD")));
				populateValueToCell(row, 34, style, nullCheck(errorRecord.getField("DIVIDEND WARRANT NUMBER")));
				populateValueToCell(row, 35, style, nullCheck(errorRecord.getField("AMOUNT OF DIVIDEND (RS.)")));
				populateValueToCell(row, 36, style, nullCheck(errorRecord.getField("DIVIDEND AMOUNT IN LAST TRANSACTION")));
				populateValueToCell(row, 37, style, nullCheck(errorRecord.getField("TDS RATE APPLIED IN LAST TRANSACTION")));
				populateValueToCell(row, 38, style, nullCheck(errorRecord.getField("AMOUNT OF DIVIDEND (FOREIGN CURRENCY)")));
				populateValueToCell(row, 39, style, nullCheck(errorRecord.getField("AGGREGATE AMOUNT OF REMITTANCE MADE DURING THE FINANCIAL YEAR INCLUDING THIS PROPOSED REMITTANCE")));
				populateValueToCell(row, 40, style, nullCheck(errorRecord.getField("ACTUAL AMOUNT OF REMITTANCE AFTER TDS (IN FOREIGN CURRENCY)")));
				populateValueToCell(row, 41, style, nullCheck(errorRecord.getField("IN CASE THE REMITTANCE IS NET OF TAXES, WHETHER TAX PAYABLE HAS BEEN GROSSED UP?")));
				populateValueToCell(row, 42, style, nullCheck(errorRecord.getField("IS FORM 15G/H AVAILABLE")));
				populateValueToCell(row, 43, style, nullCheck(errorRecord.getField("DATE OF BIRTH YYYY-MM-DD")));
				populateValueToCell(row, 44, style, nullCheck(errorRecord.getField("FORM 15G/H UIN")));
				populateValueToCell(row, 45, style, nullCheck(errorRecord.getField("FILING TYPE")));
				populateValueToCell(row, 46, style, nullCheck(errorRecord.getField("ACKNOWLEDGEMENT NUMBER")));
				populateValueToCell(row, 47, style, nullCheck(errorRecord.getField("RECORD TYPE")));
				populateValueToCell(row, 48, style, nullCheck(errorRecord.getField("DATE ON WHICH DECLARATION (FORM 15H/15H) IS RECEIVED YYYY-MM-DD")));
				populateValueToCell(row, 49, style, nullCheck(errorRecord.getField("WHETHER ASSESSED TO TAX UNDER THE INCOME-TAX ACT,1961")));
				populateValueToCell(row, 50, style, nullCheck(errorRecord.getField("IF ANSWER TO PREVIOUS COLUMN IS YES (Y), LATEST ASSESSMENT YEAR FOR WHICH ASSESSED (YYYY)")));
				populateValueToCell(row, 51, style, nullCheck(errorRecord.getField("ESTIMATED INCOME FOR WHICH THIS DECLARATION IS MADE")));
				populateValueToCell(row, 52, style, nullCheck(errorRecord.getField("ESTIMATED TOTAL INCOME OF THE P.Y. IN WHICH ESTIMATED INCOME FOR WHICH THIS DECLARATION IS MADE TO BE INCLUDED")));
				populateValueToCell(row, 53, style, nullCheck(errorRecord.getField("IS TAX RESIDENCY CERTIFICATE ('TRC') AVAILABLE")));
				populateValueToCell(row, 54, style, nullCheck(errorRecord.getField("TRC AVAILABLE (FROM DATE) YYYY-MM-DD")));
				populateValueToCell(row, 55, style, nullCheck(errorRecord.getField("TRC AVAILABLE (TO DATE) YYYY-MM-DD")));
				populateValueToCell(row, 56, style, nullCheck(errorRecord.getField("IS FORM 10F AVAILABLE")));
				populateValueToCell(row, 57, style, nullCheck(errorRecord.getField("FORM 10F AVAILABLE (FROM DATE) YYYY-MM-DD")));
				populateValueToCell(row, 58, style, nullCheck(errorRecord.getField("FORM 10F AVAILABLE (TO DATE) YYYY-MM-DD")));
				populateValueToCell(row, 59, style, nullCheck(errorRecord.getField("IS THERE A PERMANENT ESTABLISHMENT (PE) IN INDIA")));
				populateValueToCell(row, 60, style, nullCheck(errorRecord.getField("IS NO PE DECLARATION AVAILABLE")));
				populateValueToCell(row, 61, style, nullCheck(errorRecord.getField("PERIOD OF NO PE DECLARATION (FROM DATE) YYYY-MM-DD")));
				populateValueToCell(row, 62, style, nullCheck(errorRecord.getField("PERIOD OF NO PE DECLARATION (TO DATE) YYYY-MM-DD")));
				populateValueToCell(row, 63, style, nullCheck(errorRecord.getField("IS THERE A PLACE OF EFFECTIVE MANAGEMENT (POEM) OF SHAREHOLDER IN INDIA")));
				populateValueToCell(row, 64, style, nullCheck(errorRecord.getField("IS NO POEM DECLARATION AVAILABLE")));
				populateValueToCell(row, 65, style, nullCheck(errorRecord.getField("PERIOD OF NO POEM IN INDIA (FROM DATE) YYYY-MM-DD")));
				populateValueToCell(row, 66, style, nullCheck(errorRecord.getField("PERIOD OF NO POEM IN INDIA (TO DATE) YYYY-MM-DD")));
				populateValueToCell(row, 67, style, nullCheck(errorRecord.getField("IS MULTILATREAL INSTRUMENT- PRINCIPLE PURPOSE TEST / SIMPLIFIED LIMTATION OF BENEFITS SATISFACTION DECLARATION AVAILABLE")));
				populateValueToCell(row, 68, style, nullCheck(errorRecord.getField("BENEFICIAL OWNER OF INCOME")));
				populateValueToCell(row, 69, style, nullCheck(errorRecord.getField("IS BENEFICIAL OWNERSHIP DECLARATION AVAILABLE")));
				populateValueToCell(row, 70, style, nullCheck(errorRecord.getField("IS THE TRANSACTION GAAR COMPLIANT")));
				populateValueToCell(row, 71, style, nullCheck(errorRecord.getField("WHETHER COMMERCIAL INDEMNITY AVAILABLE")));
				populateValueToCell(row, 72, style, nullCheck(errorRecord.getField("IF COUNTRY OF SHAREHOLDER IS KUWAIT- WHETHER IT IS FOREIGN GOVERNMENT OR POLITICAL SUB DIVISION OR A LOCAL AUTHORITY OR THE CENTRAL BANK  OR OTHER GOVERNMENTAL AGENCIES OR GOVERNMENTAL FINANCIAL INSTITUTIONS")));
				populateValueToCell(row, 73, style, nullCheck(errorRecord.getField("IF THE SHAREHOLDER IS AN INVESTMENT VEHICLE FROM UK- CHECK WHETHER DIVIDEND DERIVED FROM IMMOVABLE PROPERTY AND INCOME FROM SUCH IMMOVABLE PROPERTY IS EXEMPT FROM TAX")));
				populateValueToCell(row, 74, style, nullCheck(errorRecord.getField("IF THE SHAREHOLDER IS AN ICELAND COMPANY- CHECK WHETHER THE SHAREHOLDING IN ICELAND COMPANY BY PERSONS OTHER THAN RESIDENT INDIVIDUALS EXCEEDS 25%. IF YES PROVIDE RATE OF DIVIDEND TAXATION")));
				populateValueToCell(row, 75, style, nullCheck(errorRecord.getField("IS_DIVIDEND_TAXABLE_AT_A_RATE_LOWER_THAN_CORPORATE_TAX_RATE")));
				populateValueToCell(row, 76, style, nullCheck(errorRecord.getField("FORM 15 CA/ CB APPLICABLE")));
				populateValueToCell(row, 77, style, nullCheck(errorRecord.getField("REMARKS")));
				
			});
		}

}
