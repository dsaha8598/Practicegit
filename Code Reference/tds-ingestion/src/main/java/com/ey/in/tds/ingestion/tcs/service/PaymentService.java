package com.ey.in.tds.ingestion.tcs.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.BorderType;
import com.aspose.cells.Cell;
import com.aspose.cells.CellBorderType;
import com.aspose.cells.CellsHelper;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tcs.common.model.payment.PaymentErrorReportCsvDto;
import com.ey.in.tcs.common.model.payment.PaymentMismatchByBatchIdDTO;
import com.ey.in.tcs.common.model.payment.TcsMatrixDTO;
import com.ey.in.tcs.common.model.payment.TcsMatrixFileDTO;
import com.ey.in.tcs.common.model.payment.TcsPaymentUtilization;
import com.ey.in.tcs.common.model.payment.TcsUtilizationFileDTO;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.CollecteeExempt;
import com.ey.in.tds.common.domain.ErrorCode;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.tcs.TCSInvoiceLineItem;
import com.ey.in.tds.common.domain.tcs.TCSNatureOfIncome;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSLccUtilizationDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsPaymentDTO;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.CustomSectionRateDTO;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.model.job.TCSNoteBookParam;
import com.ey.in.tds.common.model.job.TcsJob;
import com.ey.in.tds.common.model.provision.MatrixDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccUtilization;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.service.ErrorReportService;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.config.SparkNotebooks;
import com.ey.in.tds.ingestion.dto.invoice.UpdateOnScreenDTO;
import com.ey.in.tds.ingestion.service.tdsmismatch.TcsMismatchService;
import com.ey.in.tds.ingestion.tcs.dao.PaymentDAO;
import com.ey.in.tds.ingestion.tcs.dao.PaymentUtilizationDAO;
import com.ey.in.tds.ingestion.tcs.dao.TCSInvoiceLineItemDAO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

@Service
public class PaymentService extends TcsMismatchService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private ErrorReportService errorReportService;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	private PaymentDAO paymentDAO;

	@Autowired
	private TCSLccUtilizationDAO tcsLccUtilizationDAO;

	@Autowired
	private PaymentUtilizationDAO paymentUtilizationDAO;

	@Autowired
	private TCSInvoiceLineItemDAO tcsInvoiceLineItemDAO;

	@Autowired
	private TCSBatchUploadDAO tcsBatchUploadDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private SparkNotebooks sparkNotebooks;

	@Value("${page_size}")
	protected int pageSize;

	@Value("${databricks.key}")
	private String dataBricksKey;

	Map<String, String> excelHeaderMap = new HashMap<String, String>() {
		private static final long serialVersionUID = -2138022865773603800L;

		{
			put("Source Identifier", "D");
			put("Source File Name", "E");
			put("Company Code", "F");
			put("Name of the Company Code", "G");
			put("Collector PAN", "H");
			put("Collector TAN", "I");
			put("Collector GSTIN", "J");
			put("Collectee Code", "K");
			put("Name of the Collectee", "L");
			put("Non-Resident Collectee Indicator", "M");
			put("Collectee PAN", "N");
			put("Collectee TIN", "O");
			put("Collectee GSTIN", "P");
			put("Document Number", "Q");
			put("Document Type", "R");
			put("Document Date", "S");
			put("Posting Date of the Document", "T");
			put("Entry Date of Payment Made", "U");
			put("Line item Number in the Accounting Document", "V");
			put("Payment Amount", "W");
			put("HSN/SAC", "X");
			put("SAC Description", "Y");
			put("Payment to Vendor G/L Account Code", "Z");
			put("Payment to Vendor G/L Account Description", "AA");
			put("Withholding Tax Section", "AB");
			put("Withholding Tax Rate", "AC");
			put("Withholding Tax Amount", "AD");
			put("PO Number", "AE");
			put("PO Date", "AF");
			put("Linking of Invoice with PO", "AG");
			put("Duplicates", "AH");
			put("Mis Match", "AI");
			put("Processed", "AJ");
			put("Reason", "AK");
			put("Result", "AL");
			put("serviceDescription", "AM");
			put("User Defined Field 1", "AN");
			put("User Defined Field 2", "AO");
			put("User Defined Field 3", "AP");
		}
	};

	/**
	 * Update UpdateOnScreenDTO for remediation report
	 * 
	 * @param advanceMismatchUpdateDTO
	 * @throws RecordNotFoundException
	 */
	public UpdateOnScreenDTO updateMismatchByAction(String tan, UpdateOnScreenDTO advanceMismatchUpdateDTO,
			String userName, String tenantId) throws RecordNotFoundException {

		for (UpdateOnScreenDTO tcsPayment : advanceMismatchUpdateDTO.getData()) {
			List<TcsPaymentDTO> paymentData = paymentDAO.findPaymentByYearAndTanAndId(
					advanceMismatchUpdateDTO.getAssessmentYear(), tan, tcsPayment.getId(), false);
			BigDecimal finalAmount = BigDecimal.ZERO;
			if (!paymentData.isEmpty()) {
				TcsPaymentDTO payment = paymentData.get(0);
				BigDecimal consumedAmount = payment.getConsumedAmount() == null ? BigDecimal.ZERO
						: payment.getConsumedAmount();
				if (tcsPayment.getFinalRate() != null && tcsPayment.getFinalSection() != null) {
					payment.setFinalTcsSection(tcsPayment.getFinalSection());
					payment.setFinalTcsRate(tcsPayment.getFinalRate());
					finalAmount = (payment.getAmount().subtract(consumedAmount)).multiply(tcsPayment.getFinalRate())
							.divide(BigDecimal.valueOf(100));
				} else {
					if (payment.getFinalTcsRate() != null) {
						finalAmount = (payment.getAmount().subtract(consumedAmount)).multiply(payment.getFinalTcsRate())
								.divide(BigDecimal.valueOf(100));
					}
				}
				if (payment.getHasLcc() != null && payment.getHasLcc().equals(true)) {
					payment = lccRateCalculation(payment);
				}

				payment.setFinalTcsAmount(finalAmount);
				payment.setFinalReason(advanceMismatchUpdateDTO.getReason());
				payment.setHasMismatch(false);
				payment.setModifiedBy(userName);
				payment.setMismatchModifiedDate(new Date());
				payment.setActive(true);
				payment.setAction(advanceMismatchUpdateDTO.getActionType());

				// checking for exempt
				if (StringUtils.isNotBlank(payment.getCollecteeCode())) {
					// retrieving collectee status using collectee code
					Integer status = paymentDAO.getCollecteeResidentialStatus(payment.getCollecteeCode(),
							payment.getCollectorTan());
					if (status == 1 && payment.getFinalTcsSection().equals("206C 1H")) {
						payment.setIsExempted(true);
					}
				}
				if (payment.getFinalTcsSection() != null && payment.getFinalTcsRate() != null) {
					String collecteeType = onboardingClient
							.getCollecteeTypeBasedOnCollecteeCode(payment.getCollecteeCode(), tenantId,
									payment.getCollectorTan(), payment.getCollectorPan())
							.getBody().getData();
					if (StringUtils.isNotBlank(collecteeType)) {
						List<CollecteeExempt> list = mastersClient
								.getCollecteeExempt(collecteeType, payment.getFinalTcsSection()).getBody().getData();
						if (!list.isEmpty()) {
							payment.setIsExempted(true);
						}
					}

					List<Double> listRates = mastersClient.getRatesBasaedOnSection(payment.getFinalTcsSection())
							.getBody().getData();
					Double closestRate = closest(payment.getFinalTcsRate().doubleValue(), listRates);

					TCSNatureOfIncome nature = mastersClient
							.getNatureOfIncomeBasedOnSectionAndRate(payment.getFinalTcsSection(), closestRate).getBody()
							.getData();

					payment.setNatureOfIncome(nature.getNature());
					payment.setNatureOfIncomeId(nature.getId());
				}
				// Calculated cess and surcharge amount
				if (payment.getItcessRate() != null && payment.getFinalTcsAmount() != null) {
					payment.setItcessAmount(payment.getFinalTcsAmount().multiply(payment.getItcessRate())
							.multiply(new BigDecimal(0.01)));
				}
				if (payment.getSurchargeRate() != null && payment.getFinalTcsAmount() != null) {
					payment.setSurchargeAmount(payment.getFinalTcsAmount().multiply(payment.getSurchargeRate())
							.multiply(new BigDecimal(0.01)));
				}
				logger.info("final Object Update --- : {}", payment);
				paymentDAO.update(payment);
			} else {
				logger.error("No Record for Payment Line item to Update");
				throw new CustomException("No Record for Payment Line item to Update",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Update mismatch by action  : {}", advanceMismatchUpdateDTO);
		}
		return advanceMismatchUpdateDTO;
	}

	// Service for getting Payment mismatches summary by TAN and ID;
	public List<PaymentMismatchByBatchIdDTO> getPaymentMismatchSummaryByTanAndId(String deductorMasterTan,
			Integer batchUploadId) {

		List<PaymentMismatchByBatchIdDTO> listMisMatchBybatchDTO = new ArrayList<>();

		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "SM-RMM", batchUploadId, 0, 0));
		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "SMM-RMM", batchUploadId, 0, 0));
		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "SMM-RM", batchUploadId, 0, 0));
		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "NAD", batchUploadId, 0, 0));

		return listMisMatchBybatchDTO;
	}

	private PaymentMismatchByBatchIdDTO groupMismatchesSummary(String tan, String type, Integer batchUploadId, int year,
			int month) {
		return paymentDAO.getPaymentMismatchSummary(year, month, tan, batchUploadId, type);
	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @param batchUploadId
	 * @param mismatchCategory
	 * @param pagination
	 * @return
	 */
	// Service for getting Payment mismatches by TAN and ID;
	public CommonDTO<TcsPaymentDTO> getPaymentMismatchByTanBatchIdMismatchCategory(String deductorMasterTan,
			Integer batchUploadId, String mismatchCategory, Pagination pagination) {
		BigInteger count = BigInteger.ZERO;
		logger.info("All Payments in Mismatch Summary by BatchID done.");
		List<TcsPaymentDTO> listMisMatch = paymentDAO.getPaymentDetailsByTanBatchIdAndMismatchCategory(
				deductorMasterTan, mismatchCategory, batchUploadId, pagination);
		count = paymentDAO.getPaymentBatchIdAndMismatchCategoryCount(deductorMasterTan, mismatchCategory,
				batchUploadId);
		PagedData<TcsPaymentDTO> pagedData = new PagedData<>(listMisMatch, listMisMatch.size(),
				pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		CommonDTO<TcsPaymentDTO> tcsPayment = new CommonDTO<>();
		tcsPayment.setResultsSet(pagedData);
		tcsPayment.setCount(count);

		return tcsPayment;
	}

	// Service for getting All Payment mismatches summary by TAN and Active;
	public List<PaymentMismatchByBatchIdDTO> getAllPaymentMismatchSummaryByTanAndActive(String deductorMasterTan,
			int assessmentYear, int month) {

		List<PaymentMismatchByBatchIdDTO> listMisMatchBybatchDTO = new ArrayList<>();

		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "SM-RMM", null, assessmentYear, month));
		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "SMM-RMM", null, assessmentYear, month));
		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "SMM-RM", null, assessmentYear, month));
		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "NAD", null, assessmentYear, month));

		return listMisMatchBybatchDTO;
	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @param mismatchCategory
	 * @param assessmentYear
	 * @param month
	 * @param filters
	 * @return
	 */
	public CommonDTO<TcsPaymentDTO> getAllPaymentMismatchByTanAndMismatchCategory(String deductorMasterTan,
			String mismatchCategory, int assessmentYear, int month, MismatchesFiltersDTO filters) {
		CommonDTO<TcsPaymentDTO> advanceData = new CommonDTO<>();
		BigInteger count = BigInteger.ZERO;
		List<TcsPaymentDTO> listMisMatch = paymentDAO.getPaymentDetailsByTanActiveAndMismatchCategory(assessmentYear,
				month, deductorMasterTan, mismatchCategory, filters);
		count = paymentDAO.getPaymentMismatchCategoryCount(assessmentYear, month, deductorMasterTan, mismatchCategory,
				filters);
		for (int index = 0; index < listMisMatch.size(); index++) {
			TcsPaymentDTO advance = listMisMatch.get(index);
			BigDecimal derivedTDSAmount = advance.getDerivedTcsAmount().setScale(2, RoundingMode.UP);
			BigDecimal finalTDSAmount = advance.getFinalTcsAmount().setScale(2, RoundingMode.UP);
			// BigDecimal clientAmount = advance.getClientAmount().setScale(4,
			// RoundingMode.UP);
			BigDecimal actualTdsAmount = advance.getActualTcsAmount().setScale(2, RoundingMode.UP);
			BigDecimal amount = advance.getAmount().setScale(2, RoundingMode.UP);
			// BigDecimal withholdingAmount = new
			// BigDecimal(advance.getWithholdingAmount()).setScale(4, RoundingMode.UP);

			advance.setDerivedTcsAmount(derivedTDSAmount);
			advance.setFinalTcsAmount(finalTDSAmount);
			// TODO to add withholding amount
			// advance.setClientAmount(clientAmount);
			advance.setActualTcsAmount(actualTdsAmount);
			advance.setAmount(amount);
			// advance.setWithholdingAmount(withholdingAmount.doubleValue());

		}
		advanceData.setResultsSet(new PagedData<>(listMisMatch, listMisMatch.size(),
				filters.getPagination().getPageNumber(),
				count.intValue() > (filters.getPagination().getPageSize() * filters.getPagination().getPageNumber())
						? false
						: true));
		advanceData.setCount(count);
		return advanceData;
	}

	public TCSBatchUpload importToPaymentBatchUpload(MultipartFile file, String tenantId, String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {

		int year = Calendar.getInstance().get(Calendar.YEAR);
		int month = Calendar.getInstance().get(Calendar.YEAR) + 1;

		logger.info(file.getOriginalFilename());

		String sha256 = sha256SumService.getSHA256Hash(file);
		TCSBatchUpload batchUpload = new TCSBatchUpload();

		List<TCSBatchUpload> batch = tcsBatchUploadDAO.getSha256RecordsBasedonYearMonth(year, month,
				UploadTypes.INV_REM.name(), sha256);

		String path = blob.uploadExcelToBlob(file, tenantId);
		batchUpload.setAssessmentYear(year);
		batchUpload.setCollectorMasterTan("missingDeductorTan");
		batchUpload.setUploadType(UploadTypes.INV_REM.name());
		if (!batch.isEmpty()) {
			logger.info("Duplicate PDF Record inserting : {}", file.getOriginalFilename());
			batchUpload.setAssessmentYear(year);
			batchUpload.setCollectorMasterTan("missingDeductorTan");
			batchUpload.setUploadType(UploadTypes.INV_REM.name());
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setReferenceId(batch.get(0).getId());
		} else {
			batchUpload.setNewStatus("Uploaded");
			logger.info("Unique record creating : {}", file.getOriginalFilename());
		}
		batchUpload.setFileName(file.getOriginalFilename());
		batchUpload.setFilePath(path);
		batchUpload.setCreatedDate(new Date());
		batchUpload.setCreatedBy(userName);
		batchUpload.setModifiedBy(userName);
		batchUpload.setModifiedDate(new Date());
		batchUpload.setActive(true);
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload = tcsBatchUploadDAO.save(batchUpload);

		return batchUpload;
	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @param tenantId
	 * @param deductorPan
	 * @param year
	 * @param month
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	@Async
	public ByteArrayInputStream asyncExportRemediationReport(String deductorMasterTan, String tenantId,
			String deductorPan, int year, int month, String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		return exportRemediationReport(deductorMasterTan, tenantId, deductorPan, year, month, userName);
	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @param tenantId
	 * @param deductorPan
	 * @param year
	 * @param month
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public ByteArrayInputStream exportRemediationReport(String deductorMasterTan, String tenantId, String deductorPan,
			int year, int month, String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TCSBatchUpload batchUpload = saveMismatchReport(deductorMasterTan, tenantId, year, out, 0L,
				UploadTypes.PAYMENT_MISMATCH_REPORT.name(), "Processing", month, userName, null);
		String type = "payment_mismatch";
		// Invocking spark Job id
		logger.info("Notebook type : {}", type);
		TCSNoteBookParam noteBookParam = createNoteBook(year, deductorMasterTan, tenantId, userName, month,
				batchUpload);
		SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get("tcs_" + type.toLowerCase());
		logger.info("Notebook url : {}", notebook.getUrl());
		triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam, month, year, tenantId,
				deductorMasterTan, userName);

		return null;
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
	 * @return
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
			TCSBatchUpload batchUpload) {
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
	 * @param tan
	 * @param batchId
	 * @param file
	 * @param tenantId
	 * @param deductorPan
	 * @param year
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	@Async
	public TCSBatchUpload asyncUpdateRemediationReport(String tan, TCSBatchUpload tcsBatchUpload, String path,
			String tenantId, String deductorPan, int year, String userEmail, Integer month)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Batch upload : {}", tcsBatchUpload);
		if (tcsBatchUpload != null) {
			logger.info("batchOptional : {}", tcsBatchUpload);
			tcsBatchUpload.setStatus("Processing");
			tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			tcsBatchUploadDAO.update(tcsBatchUpload);
			tcsBatchUpload = updateRemediationReport(tan, tcsBatchUpload, path, tenantId, deductorPan, year, userEmail,
					month);
		}
		logger.info("Processed the mismatch file");
		return tcsBatchUpload;
	}

	/**
	 * 
	 * @param tan
	 * @param batchId
	 * @param file
	 * @param tenantId
	 * @param deductorPan
	 * @param year
	 * @param batchUpload
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	public TCSBatchUpload updateRemediationReport(String tan, TCSBatchUpload tcsBatchUpload, String path,
			String tenantId, String deductorPan, int year, String userEmail, Integer month)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String otherFilepath = null;
		Biff8EncryptionKey.setCurrentUserPassword("password");
		// Load the input Excel file
		Workbook workbook;
		try {
			logger.info("Mismatch file path : {}", path);
			File file = blobStorageService.getFileFromBlobUrl(tenantId, path);
			workbook = new Workbook(file.getAbsolutePath());
			Worksheet worksheet = workbook.getWorksheets().get(0);
			worksheet.getCells().deleteRows(0, 5);

			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.CSV);
			File xlsxInvoiceFile = new File("TestCsvFile");

			FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());

			CsvReader csvReader = new CsvReader();
			csvReader.setContainsHeader(true);

			CsvContainer csv = csvReader.read(xlsxInvoiceFile, StandardCharsets.UTF_8);

			String errorFilepath = null;
			int processedCount = 0;
			int errorCount = 0;
			boolean isCancel = false;
			List<Integer> paymentIds = new ArrayList<>();
			// exempt case
			List<CollecteeExempt> collecteeExempts = mastersClient.getCollecteeExemptAll().getBody().getData();
			Map<String, Boolean> exemptMap = new HashMap<>();
			for (CollecteeExempt collecteeExempt : collecteeExempts) {
				exemptMap.put(collecteeExempt.getSection() + "-" + collecteeExempt.getCollecteeStatus(), true);
			}
			// Rate and section change
			List<CustomSectionRateDTO> listRatesSections = mastersClient.findSectionRates().getBody().getData();
			Map<String, List<Double>> ratesMap = new HashMap<String, List<Double>>();

			Map<String, String> sectionRateNature = new HashMap<String, String>();
			Map<String, BigInteger> sectionRateNoiId = new HashMap<String, BigInteger>();
			for (CustomSectionRateDTO customSectionRateDTO : listRatesSections) {
				String section = customSectionRateDTO.getSection();
				List<Double> rates = new ArrayList<>();
				if (ratesMap.get(section) != null) {
					rates = ratesMap.get(section);
				}
				rates.add(customSectionRateDTO.getRate());
				ratesMap.put(section, rates);
				// section rate
				sectionRateNature.put(customSectionRateDTO.getSection() + "-" + customSectionRateDTO.getRate(),
						customSectionRateDTO.getNature());
				sectionRateNoiId.put(customSectionRateDTO.getSection() + "-" + customSectionRateDTO.getRate(),
						customSectionRateDTO.getNoiId());
			}
			List<TcsPaymentDTO> tcsPaymentList = new ArrayList<>();
			// retrieving collectee status using collectee code
			List<String> statusList = paymentDAO.getCollecteeNonResidentialStatus(tan);
			List<CsvRow> errorList = new ArrayList<>();
			List<String> errorMessages = new ArrayList<>();
			for (CsvRow row : csv.getRows()) {
				// Derived
				String derivedTcsSectionFromExcel = row.getField("Derived TCS Section");
				BigDecimal derivedTcsRateFromExcel = StringUtils.isNotBlank(row.getField("Derived TCS Rate"))
						? new BigDecimal(row.getField("Derived TCS Rate"))
						: null;
				BigDecimal derivedTcsAmountFromExcel = StringUtils.isNotBlank(row.getField("Derived TCS Amount"))
						? new BigDecimal(row.getField("Derived TCS Amount"))
						: null;
				// Actual
				String actualTcsSectionFromExcel = row.getField("Client TCS Section");
				BigDecimal actualTcsRateFromExcel = StringUtils.isNotBlank(row.getField("Client TCS Rate"))
						? new BigDecimal(row.getField("Client TCS Rate"))
						: null;
				BigDecimal actualTcsAmountFromExcel = StringUtils.isNotBlank(row.getField("Client TCS Amount"))
						? new BigDecimal(row.getField("Client TCS Amount"))
						: null;
				// Final
				String finalTcsSectionFromExcel = row.getField("Final TCS Section");
				BigDecimal finalTcsRateFromExcel = StringUtils.isNotBlank(row.getField("Final TCS Rate"))
						? new BigDecimal(row.getField("Final TCS Rate"))
						: null;
				BigDecimal finalTcsAmountFromExcel = StringUtils.isNotBlank(row.getField("Final TCS Amount"))
						? new BigDecimal(row.getField("Final TCS Amount"))
						: null;
				// cess and surcharge
				BigDecimal cessRateFromExcel = StringUtils.isNotBlank(row.getField("Cess TCS Rate"))
						? new BigDecimal(row.getField("Cess TCS Rate"))
						: null;
				BigDecimal surchargeRateFromExcel = StringUtils.isNotBlank(row.getField("Surcharge TCS Rate"))
						? new BigDecimal(row.getField("Surcharge TCS Rate"))
						: null;
				// Collectee code
				String collecteeCode = row.getField("Collectee Code");
				// CollecteeType
				String collecteeTypeFromExcel = row.getField("Collectee Type");
				// Action
				String userAction = row.getField("Action");
				// Reason
				String reason = row.getField("Reason");
				Integer paymentId = StringUtils.isNotBlank(row.getField("Payment Id"))
						? Integer.parseInt(row.getField("Payment Id"))
						: null;

				// Document posting date
				Date documentPostingDate = StringUtils.isNotBlank(row.getField("Document Posting Date"))
						? new SimpleDateFormat("yyyy-MM-dd").parse(row.getField("Document Posting Date"))
						: null;

				BigDecimal finalTcsRate = BigDecimal.ZERO;
				String finalTcsSection = StringUtils.EMPTY;
				Boolean active = true;
				boolean isMismatch = true;
				Boolean isExempt = false;
				BigDecimal cessAmount = BigDecimal.ZERO;
				BigDecimal surchargeAmount = BigDecimal.ZERO;
				String natureOfIncome = null;
				Long noiId = null;
				Boolean isError = false;

				if (userAction != null && !userAction.isEmpty()) {
					BigDecimal finalAmount = new BigDecimal(0);
					BigDecimal taxableAmount = new BigDecimal(0);
					BigDecimal amount = BigDecimal.ZERO;
					BigDecimal consumedAmount = BigDecimal.ZERO;
					amount = StringUtils.isNotBlank(row.getField("Payment Amount"))
							? new BigDecimal(row.getField("Payment Amount"))
							: BigDecimal.ZERO;
					consumedAmount = StringUtils.isNotBlank(row.getField("Consumed Amount"))
							? new BigDecimal(row.getField("Consumed Amount"))
							: BigDecimal.ZERO;
					taxableAmount = amount.subtract(consumedAmount);
					logger.info("taxable amount is: {}", taxableAmount);
					if (paymentId != null) {
						TcsPaymentDTO tcsPaymentDTO = new TcsPaymentDTO();
						if (userAction != null && !userAction.isEmpty()) {
							if ("Accept".equalsIgnoreCase(userAction)) {
								if (derivedTcsSectionFromExcel != null && derivedTcsRateFromExcel != null
										&& derivedTcsAmountFromExcel != null
										&& ratesMap.containsKey(derivedTcsSectionFromExcel)) {
									isMismatch = false;
									finalTcsSection = derivedTcsSectionFromExcel;
									finalTcsRate = derivedTcsRateFromExcel;
									// caluclated cess and surcharge
									if (cessRateFromExcel != null) {
										cessAmount = derivedTcsAmountFromExcel.multiply((cessRateFromExcel))
												.multiply(new BigDecimal(0.01));
									}
									if (surchargeRateFromExcel != null) {
										surchargeAmount = derivedTcsAmountFromExcel.multiply(surchargeRateFromExcel)
												.multiply(new BigDecimal(0.01));
									}
								} else {
									isError = true;
									errorCount++;
									errorList.add(row);
									String message = StringUtils.EMPTY;
									if (StringUtils.isEmpty(derivedTcsSectionFromExcel)) {
										message = " Derived TCS Section is empty or null" + "\n";
									}
									if (derivedTcsRateFromExcel == null) {
										message = message + " Derived TCS Rate is empty or null" + "\n";
									}
									if (derivedTcsAmountFromExcel == null) {
										message = message + " Derived TCS Amount is empty or null" + "\n";
									}
									errorMessages.add(message);
								}
							} else if ("Modify".equalsIgnoreCase(userAction)) {
								if (finalTcsAmountFromExcel != null && finalTcsRateFromExcel != null
										&& finalTcsSectionFromExcel != null
										&& ratesMap.containsKey(finalTcsSectionFromExcel)) {
									isMismatch = false;
									finalTcsRate = finalTcsRateFromExcel;
									finalTcsSection = finalTcsSectionFromExcel;
									// caluclated cess and surcharge
									if (cessRateFromExcel != null) {
										cessAmount = finalTcsAmountFromExcel.multiply((cessRateFromExcel))
												.multiply(new BigDecimal(0.01));
									}
									if (surchargeRateFromExcel != null) {
										surchargeAmount = finalTcsAmountFromExcel.multiply(surchargeRateFromExcel)
												.multiply(new BigDecimal(0.01));
									}
								} else {
									isError = true;
									errorCount++;
									errorList.add(row);
									String message = StringUtils.EMPTY;
									if (StringUtils.isEmpty(finalTcsSectionFromExcel)) {
										message = " Final TCS Section is empty or null" + "\n";
									}
									if (finalTcsRateFromExcel == null) {
										message = message + " Final TCS Rate is empty or null" + "\n";
									}
									if (finalTcsAmountFromExcel == null) {
										message = message + " Final TCS Amount is empty or null" + "\n";
									}
									errorMessages.add(message);
								}
							} else if ("Reject".equalsIgnoreCase(userAction)) {
								if (actualTcsSectionFromExcel != null && actualTcsRateFromExcel != null
										&& ratesMap.containsKey(actualTcsSectionFromExcel)) {
									isMismatch = false;
									finalTcsRate = actualTcsRateFromExcel;
									finalTcsSection = actualTcsSectionFromExcel;
									// caluclated cess and surcharge
									if (cessRateFromExcel != null) {
										cessAmount = actualTcsAmountFromExcel.multiply((cessRateFromExcel))
												.multiply(new BigDecimal(0.01));
									}
									if (surchargeRateFromExcel != null) {
										surchargeAmount = actualTcsAmountFromExcel.multiply(surchargeRateFromExcel)
												.multiply(new BigDecimal(0.01));
									}
								} else {
									isError = true;
									errorCount++;
									errorList.add(row);
									String message = StringUtils.EMPTY;
									if (StringUtils.isEmpty(actualTcsSectionFromExcel)) {
										message = " Actual TCS Section is empty or null" + "\n";
									}
									if (actualTcsRateFromExcel == null) {
										message = message + " Actual TCS Rate is empty or null" + "\n";
									}
									if (actualTcsAmountFromExcel == null) {
										message = message + " Actual TCS Amount is empty or null" + "\n";
									}
									errorMessages.add(message);
								}
							} else if ("Cancel".equalsIgnoreCase(userAction)) {
								reason = "USER REQUESTED TO CANCEL";
								active = false;
								isCancel = true;
								tcsPaymentDTO.setErrorReason("Canceled record-ERR029");
								paymentIds.add(paymentId);
							}

							if (!"Cancel".equalsIgnoreCase(userAction)) {
								if (StringUtils.isNotBlank(collecteeCode)) {
									if (statusList.contains(collecteeCode) && finalTcsSection.equals("206C(1H)")) {
										isExempt = true;
									}
								}
								// check for collectee type and finalTcsSection
								if (StringUtils.isNotBlank(collecteeTypeFromExcel)) {
									Boolean exemptCase = exemptMap.get(finalTcsSection + "-" + collecteeTypeFromExcel);
									if (exemptCase != null && exemptCase == true) {
										isExempt = true;
									}
								}

								if (ratesMap != null && ratesMap.get(finalTcsSection) != null) {
									Double closestRate = closest(finalTcsRate.doubleValue(),
											ratesMap.get(finalTcsSection));
									natureOfIncome = sectionRateNature.get(finalTcsSection + "-" + closestRate);
									BigInteger noiIdInt = sectionRateNoiId.get(finalTcsSection + "-" + closestRate);
									noiId = noiIdInt != null ? noiIdInt.longValue() : null;
								}

							}

							// Final TDS Calculation
							finalAmount = finalAmount
									.add(finalTcsRate.multiply(taxableAmount).divide(BigDecimal.valueOf(100)));
							tcsPaymentDTO.setFinalReason(reason);
							tcsPaymentDTO.setActive(active);
							tcsPaymentDTO.setIsExempted(isExempt);
							tcsPaymentDTO.setFinalTcsAmount(finalAmount);
							tcsPaymentDTO.setFinalTcsRate(finalTcsRate);
							tcsPaymentDTO.setFinalTcsSection(finalTcsSection);
							tcsPaymentDTO.setHasMismatch(isMismatch);
							tcsPaymentDTO.setItcessAmount(cessAmount);
							tcsPaymentDTO.setSurchargeAmount(surchargeAmount);
							if (StringUtils.isNotBlank(natureOfIncome)) {
								tcsPaymentDTO.setNatureOfIncome(natureOfIncome);
							}
							if (noiId != null) {
								tcsPaymentDTO.setNoiId(noiId);
							}
							tcsPaymentDTO.setAction(userAction);
							tcsPaymentDTO.setModifiedDate(new Date());
							tcsPaymentDTO.setModifiedBy(userEmail);
							tcsPaymentDTO.setId(paymentId);
							tcsPaymentDTO.setPostingDate(documentPostingDate);
							if (!isError) {
								tcsPaymentList.add(tcsPaymentDTO);
								processedCount++;
							}
						}
					} else {
						errorCount++;
						errorList.add(row);
						errorMessages.add("payment mismatch id not found in system");
					}
				}
				
			}
			// payment mismatch error report generate
			if (errorCount > 0) {
				ByteArrayOutputStream bytes = generateErrorReport(errorList, errorMessages, tan, tenantId, deductorPan);
				if (bytes.size() != 0) {
					errorFilepath = sendFileToBlobStorage(bytes, tenantId);
				}
			}
			// batch update for payment
			if (!tcsPaymentList.isEmpty()) {
				logger.info("started batchupdate method");
				paymentDAO.batchUpdate(tcsPaymentList);
			}
			if (isCancel) {
				MultipartFile cancelledPaymentsFile = generateCancelledPaymentExcell(paymentIds, tan, deductorPan);
				otherFilepath = blob.uploadExcelToBlob(cancelledPaymentsFile);
			}
			if (tcsBatchUpload.getId() != null) {
				tcsBatchUpload.setFilePath(path);
				tcsBatchUpload.setFailedCount(Long.valueOf(errorCount));
				tcsBatchUpload.setProcessed(processedCount);
				tcsBatchUpload.setErrorFilePath(errorFilepath);
				tcsBatchUpload.setRowsCount((long) processedCount + errorCount);
				tcsBatchUpload.setStatus("Processed");
				tcsBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				tcsBatchUpload.setOtherFileUrl(otherFilepath);
				tcsBatchUploadDAO.update(tcsBatchUpload);
			}
			// extra query for mismatch
			tcsInvoiceLineItemDAO.paymentMismatchCountUpdate(year, month, tan, deductorPan);

		} catch (Exception e1) {
			logger.error("Exception occurred while updating remediation report", e1);
		}
		return tcsBatchUpload;
	}

	/**
	 * 
	 * @param out
	 * @param tenantId
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws StorageException
	 */
	private String sendFileToBlobStorage(ByteArrayOutputStream out, String tenantId)
			throws IOException, URISyntaxException, InvalidKeyException, StorageException {
		File file = getConvertedExcelFile(out);
		logger.info("Original file   : {}", file.getName());
		String paths = blob.uploadExcelToBlobWithFile(file, tenantId);
		return paths;
	}

	/**
	 * 
	 * @param out
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private File getConvertedExcelFile(ByteArrayOutputStream out) throws FileNotFoundException, IOException {
		byte[] bytes = out.toByteArray();
		File someFile = new File("Payment_Remediation_Error_Report.xlsx");
		try (FileOutputStream fos = new FileOutputStream(someFile)) {
			fos.write(bytes);
			fos.flush();
			fos.close();
			return someFile;
		}
	}

	/**
	 * 
	 * @param file
	 * @param tan
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	private ByteArrayOutputStream generateErrorReport(List<CsvRow> cslRowList, List<String> listErrorMessages, String tan,
			String tenantId, String deductorPan)
			throws IOException, InvalidFormatException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();
		try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
			SXSSFSheet sheet = wb.createSheet("Payment_Remediation_Error_Report");
			sheet.protectSheet("password");
			sheet.setRandomAccessWindowSize(1000);
			sheet.setDisplayGridlines(false);
			sheet.setColumnHidden(37, true);
			sheet.setColumnHidden(38, true);
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);
			DataValidation dataValidation = null;
			DataValidationConstraint constraint = null;
			DataValidationHelper validationHelper = null;

			SXSSFRow row0 = sheet.createRow(0);
			XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();

			style.setWrapText(true);
			Font fonts = wb.createFont();
			fonts.setBold(true);
			style.setFont(fonts);

			style.setVerticalAlignment(VerticalAlignment.BOTTOM);
			style.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));

			sheet.createRow(1);
			XSSFCellStyle style01 = (XSSFCellStyle) wb.createCellStyle();
			style01.setBorderLeft(BorderStyle.MEDIUM);
			style01.setBorderTop(BorderStyle.MEDIUM);
			style01.setBorderBottom(BorderStyle.MEDIUM);
			style01.setBorderRight(BorderStyle.MEDIUM);
			style01.setAlignment(HorizontalAlignment.LEFT);
			style01.setVerticalAlignment(VerticalAlignment.CENTER);
			style01.setFillForegroundColor(new XSSFColor(new java.awt.Color(102, 194, 255), defaultIndexedColorMap));
			Font font01 = wb.createFont();
			font01.setBold(true);
			style01.setFont(font01);

			sheet.createRow(2);
			XSSFCellStyle style02 = (XSSFCellStyle) wb.createCellStyle();
			style02.setBorderLeft(BorderStyle.MEDIUM);
			style02.setBorderTop(BorderStyle.MEDIUM);
			style02.setBorderBottom(BorderStyle.MEDIUM);
			style02.setBorderRight(BorderStyle.MEDIUM);
			style02.setAlignment(HorizontalAlignment.LEFT);
			style02.setVerticalAlignment(VerticalAlignment.CENTER);
			style02.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));
			Font font02 = wb.createFont();
			font02.setBold(true);
			style02.setFont(font02);

			row0.setHeightInPoints((3 * sheet.getDefaultRowHeightInPoints()));
			String msg = getErrorReportMsg(tan, tenantId, deductorPan);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

			XSSFCellStyle styleForStaticValues = (XSSFCellStyle) wb.createCellStyle();
			styleForStaticValues.setVerticalAlignment(VerticalAlignment.CENTER);
			styleForStaticValues.setAlignment(HorizontalAlignment.LEFT);
			Font fonts2 = wb.createFont();
			fonts2.setBold(false);
			styleForStaticValues.setFont(fonts2);

			// value4
			SXSSFRow row03 = sheet.createRow(4);

			// header colors
			XSSFCellStyle style0 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style1 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style2 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style3 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style003 = (XSSFCellStyle) wb.createCellStyle();

			style0.setAlignment(HorizontalAlignment.CENTER);
			style0.setVerticalAlignment(VerticalAlignment.CENTER);
			style0.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));

			style1.setAlignment(HorizontalAlignment.CENTER);
			style1.setVerticalAlignment(VerticalAlignment.CENTER);
			style1.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));

			style2.setAlignment(HorizontalAlignment.CENTER);
			style2.setVerticalAlignment(VerticalAlignment.CENTER);
			style2.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));

			style3.setAlignment(HorizontalAlignment.CENTER);
			style3.setVerticalAlignment(VerticalAlignment.CENTER);
			style3.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));

			style003.setAlignment(HorizontalAlignment.CENTER);
			style003.setVerticalAlignment(VerticalAlignment.CENTER);
			style003.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));

			Font font = wb.createFont();
			font.setBold(true);
			font.setFontName("Arial");
			style0.setFont(font);
			style1.setFont(font);
			style2.setFont(font);
			style3.setFont(font);
			style003.setFont(font);
			// Green colour
			setCellColorAndBoarder(defaultIndexedColorMap, style0, 169, 209, 142);
			// Light orange-color
			setCellColorAndBoarder(defaultIndexedColorMap, style1, 251, 229, 214);
			// Light yellow color
			setCellColorAndBoarder(defaultIndexedColorMap, style2, 255, 242, 204);
			// Light blue-color
			setCellColorAndBoarder(defaultIndexedColorMap, style3, 222, 235, 247);
			// yellow
			setCellColorAndBoarder(defaultIndexedColorMap, style003, 255, 192, 0);

			row03.createCell(5).setCellValue("Error Type");
			row03.getCell(5).setCellStyle(style0);
			CellRangeAddress cellRangeAddressErrorType = new CellRangeAddress(4, 4, 5, 7);
			sheet.addMergedRegion(cellRangeAddressErrorType);

			row03.createCell(20).setCellValue("Client provided data");
			row03.getCell(20).setCellStyle(style1);
			CellRangeAddress cellRangeAddress1 = new CellRangeAddress(4, 4, 20, 22);
			sheet.addMergedRegion(cellRangeAddress1);

			row03.createCell(23).setCellValue("Tool Derived Sections/Rate");
			row03.getCell(23).setCellStyle(style2);
			CellRangeAddress cellRangeAddress2 = new CellRangeAddress(4, 4, 23, 25);
			sheet.addMergedRegion(cellRangeAddress2);

			row03.createCell(26).setCellValue("Mismatch Category");
			row03.getCell(26).setCellStyle(style3);
			CellRangeAddress cellRangeAddress3 = new CellRangeAddress(4, 4, 26, 29);
			sheet.addMergedRegion(cellRangeAddress3);

			row03.createCell(32).setCellValue("Client Response");
			row03.getCell(32).setCellStyle(style003);
			CellRangeAddress cellRangeAddress4 = new CellRangeAddress(4, 4, 32, 36);
			sheet.addMergedRegion(cellRangeAddress4);

			SXSSFRow row1 = sheet.createRow(5);
			sheet.setDefaultColumnWidth(25);
			// setting column width for error message column
			sheet.setColumnWidth(3, sheet.getColumnWidth(3) * 3);

			// Create a cell
			row1.createCell(0).setCellValue("Sequence Number");
			row1.getCell(0).setCellStyle(style0); // A

			row1.createCell(1).setCellValue("Errors");
			row1.getCell(1).setCellStyle(style0); // A

			row1.createCell(2).setCellValue("Collector TAN");
			row1.getCell(2).setCellStyle(style0); // B

			row1.createCell(3).setCellValue("Collector GSTIN");
			row1.getCell(3).setCellStyle(style0); // C

			row1.createCell(4).setCellValue("ERROR MESSAGE");
			row1.getCell(4).setCellStyle(style0); // D

			// error types
			row1.createCell(5).setCellValue("Section");
			row1.getCell(5).setCellStyle(style0);// E

			row1.createCell(6).setCellValue("Rate");
			row1.getCell(6).setCellStyle(style0);// F

			row1.createCell(7).setCellValue("Amount");
			row1.getCell(7).setCellStyle(style0); // G

			row1.createCell(8).setCellValue("Collectee Code");
			row1.getCell(8).setCellStyle(style0); // H

			row1.createCell(9).setCellValue("Collectee PAN");
			row1.getCell(9).setCellStyle(style0); // I

			row1.createCell(10).setCellValue("Collectee Name");
			row1.getCell(10).setCellStyle(style0); // J

			row1.createCell(11).setCellValue("Document Number");
			row1.getCell(11).setCellStyle(style0); // K

			row1.createCell(12).setCellValue("Accunting Document Number");
			row1.getCell(12).setCellStyle(style0); // L

			row1.createCell(13).setCellValue("Line Number");
			row1.getCell(13).setCellStyle(style0); // M

			row1.createCell(14).setCellValue("Document Posting Date");
			row1.getCell(14).setCellStyle(style0); // N

			row1.createCell(15).setCellValue("Document Type");
			row1.getCell(15).setCellStyle(style0); // O

			row1.createCell(16).setCellValue("Invoice Description");
			row1.getCell(16).setCellStyle(style0); // P

			row1.createCell(17).setCellValue("SO Description");
			row1.getCell(17).setCellStyle(style0); // Q

			row1.createCell(18).setCellValue("GL Description");
			row1.getCell(18).setCellStyle(style0); // R

			row1.createCell(19).setCellValue("HSN Code/SAC Code");
			row1.getCell(19).setCellStyle(style0); // S

			row1.createCell(20).setCellValue("Client TCS Section");
			row1.getCell(20).setCellStyle(style1); // T

			row1.createCell(21).setCellValue("Client TCS Rate");
			row1.getCell(21).setCellStyle(style1); // U

			row1.createCell(22).setCellValue("Client TCS Amount");
			row1.getCell(22).setCellStyle(style1); // V

			row1.createCell(23).setCellValue("Derived TCS Section");
			row1.getCell(23).setCellStyle(style2); // W

			row1.createCell(24).setCellValue("Derived TCS Rate");
			row1.getCell(24).setCellStyle(style2); // X

			row1.createCell(25).setCellValue("Derived TCS Amount");
			row1.getCell(25).setCellStyle(style2); // Y

			row1.createCell(26).setCellValue("Section");
			row1.getCell(26).setCellStyle(style3); // Z

			row1.createCell(27).setCellValue("Rate");
			row1.getCell(27).setCellStyle(style3); // AA

			row1.createCell(28).setCellValue("Amount");
			row1.getCell(28).setCellStyle(style3); // AB

			row1.createCell(29).setCellValue("Collection Type");
			row1.getCell(29).setCellStyle(style3); // AC

			row1.createCell(30).setCellValue("Mismatch Interpretation");
			row1.getCell(30).setCellStyle(style0); // AD

			row1.createCell(31).setCellValue("Confidence");
			row1.getCell(31).setCellStyle(style0); // AE

			row1.createCell(32).setCellValue("Action");
			row1.getCell(32).setCellStyle(style003); // AF

			row1.createCell(33).setCellValue("Reason");
			row1.getCell(33).setCellStyle(style003); // AG

			row1.createCell(34).setCellValue("Final TCS Section");
			row1.getCell(34).setCellStyle(style003); // AH

			row1.createCell(35).setCellValue("Final TCS Rate");
			row1.getCell(35).setCellStyle(style003); // AI

			row1.createCell(36).setCellValue("Final TCS Amount");
			row1.getCell(36).setCellStyle(style003); // AJ

			row1.createCell(37).setCellValue("Payment Id");
			row1.getCell(37).setCellStyle(style003); // AJ

			row1.createCell(38).setCellValue("Payment Amount");
			row1.getCell(38).setCellStyle(style3); // AM

			row1.createCell(39).setCellValue("Cess TCS Rate");
			row1.getCell(39).setCellStyle(style0); // AN

			row1.createCell(40).setCellValue("Surcharge TCS Rate");
			row1.getCell(40).setCellStyle(style0); // AO

			row1.createCell(41).setCellValue("Collectee Type");
			row1.getCell(41).setCellStyle(style0); // AQ

			row1.createCell(42).setCellValue("Consumed Amount");
			row1.getCell(42).setCellStyle(style0); // AQ

			// Auto Filter Option
			sheet.setAutoFilter(new CellRangeAddress(5, 42, 0, 42));
			sheet.createFreezePane(0, 1);

			validationHelper = sheet.getDataValidationHelper();
			CellRangeAddressList addressList = new CellRangeAddressList(6, 32, 32, 32);
			constraint = validationHelper.createExplicitListConstraint(new String[] { "Accept", "Modify", "Cancel" });
			dataValidation = validationHelper.createValidation(constraint, addressList);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.setShowPromptBox(true);
			sheet.addValidationData(dataValidation);

			validationHelper = sheet.getDataValidationHelper();
			CellRangeAddressList addressList2 = new CellRangeAddressList(6, 34, 34, 34);
			// Feign Client to get list of sections from nature of payment
			ResponseEntity<ApiStatus<List<NatureOfPaymentMasterDTO>>> natureOfPayament = mastersClient
					.getTcsNatureOfPayment();

			List<NatureOfPaymentMasterDTO> natureOfPayamentList = natureOfPayament.getBody().getData();
			List<String> sectionArray = new ArrayList<>();
			for (NatureOfPaymentMasterDTO nopMaster : natureOfPayamentList) {
				sectionArray.add(nopMaster.getSection());
			}

			constraint = validationHelper
					.createExplicitListConstraint(sectionArray.toArray(new String[sectionArray.size()]));
			dataValidation = validationHelper.createValidation(constraint, addressList2);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.setShowPromptBox(true);
			sheet.addValidationData(dataValidation);

			XSSFCellStyle style5 = (XSSFCellStyle) wb.createCellStyle();
			Font font05 = wb.createFont();
			style5.setFont(font05);
			style5.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));
			style5.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style5.setBorderLeft(BorderStyle.THIN);
			style5.setBorderTop(BorderStyle.THIN);
			style5.setBorderBottom(BorderStyle.THIN);
			style5.setBorderRight(BorderStyle.THIN);
			style5.setVerticalAlignment(VerticalAlignment.CENTER);
			style5.setAlignment(HorizontalAlignment.LEFT);

			XSSFCellStyle style4 = (XSSFCellStyle) wb.createCellStyle();
			Font font4 = wb.createFont();
			style4.setFont(font4);
			style4.setLocked(false);
			style4.setBorderLeft(BorderStyle.THIN);
			style4.setBorderTop(BorderStyle.THIN);
			style4.setBorderBottom(BorderStyle.THIN);
			style4.setBorderRight(BorderStyle.THIN);

			Biff8EncryptionKey.setCurrentUserPassword("password");
			int rowindex = 6;
			int index = 0;
			int sequenceNumber = 1;
			for (CsvRow row : cslRowList) {
				SXSSFRow row2 = sheet.createRow(rowindex++);
				row2.createCell(0).setCellValue(sequenceNumber);
				row2.getCell(0).setCellStyle(style5);
				row2.createCell(1).setCellValue(listErrorMessages.get(index));
				row2.getCell(1).setCellStyle(style4);
				row2.createCell(2).setCellValue(row.getField("Collector TAN"));
				row2.getCell(2).setCellStyle(style5);
				row2.createCell(3).setCellValue(row.getField("Collector GSTIN"));
				row2.getCell(3).setCellStyle(style5);
				row2.createCell(4).setCellValue(row.getField("ERROR MESSAGE"));
				row2.getCell(4).setCellStyle(style5);
				row2.createCell(5).setCellValue(row.getField("Section"));
				row2.getCell(5).setCellStyle(style5);
				row2.createCell(6).setCellValue(row.getField("Rate"));
				row2.getCell(6).setCellStyle(style5);
				row2.createCell(7).setCellValue(row.getField("Amount"));
				row2.getCell(7).setCellStyle(style5);
				row2.createCell(8).setCellValue(row.getField("Collectee Code"));
				row2.getCell(8).setCellStyle(style5);
				row2.createCell(9).setCellValue(row.getField("Collectee PAN"));
				row2.getCell(9).setCellStyle(style5);
				row2.createCell(10).setCellValue(row.getField("Collectee Name"));
				row2.getCell(10).setCellStyle(style5);
				row2.createCell(11).setCellValue(row.getField("Document Number"));
				row2.getCell(11).setCellStyle(style5);
				row2.createCell(12).setCellValue(row.getField("Accunting Document Number"));
				row2.getCell(12).setCellStyle(style5);
				row2.createCell(13).setCellValue(row.getField("Line Number"));
				row2.getCell(13).setCellStyle(style5);
				row2.createCell(14).setCellValue(row.getField("Document Posting Date"));
				row2.getCell(14).setCellStyle(style5);
				row2.createCell(15).setCellValue(row.getField("Document Type"));
				row2.getCell(15).setCellStyle(style5);
				row2.createCell(16).setCellValue(row.getField("Invoice Description"));
				row2.getCell(16).setCellStyle(style5);
				row2.createCell(17).setCellValue(row.getField("SO Description"));
				row2.getCell(17).setCellStyle(style5);
				row2.createCell(18).setCellValue(row.getField("GL Description"));
				row2.getCell(18).setCellStyle(style5);
				row2.createCell(19).setCellValue(row.getField("HSN Code/SAC Code"));
				row2.getCell(19).setCellStyle(style5);
				row2.createCell(20).setCellValue(row.getField("Client TCS Section"));
				row2.getCell(20).setCellStyle(style5);
				row2.createCell(21).setCellValue(row.getField("Client TCS Rate"));
				row2.getCell(21).setCellStyle(style5);
				row2.createCell(22).setCellValue(row.getField("Client TCS Amount"));
				row2.getCell(22).setCellStyle(style5);
				row2.createCell(23).setCellValue(row.getField("Derived TCS Section"));
				row2.getCell(23).setCellStyle(style5);
				row2.createCell(24).setCellValue(row.getField("Derived TCS Rate"));
				row2.getCell(24).setCellStyle(style5);
				row2.createCell(25).setCellValue(row.getField("Derived TCS Amount"));
				row2.getCell(25).setCellStyle(style5);
				row2.createCell(26).setCellValue(row.getField("Section"));
				row2.getCell(26).setCellStyle(style5);
				row2.createCell(27).setCellValue(row.getField("Rate"));
				row2.getCell(27).setCellStyle(style5);
				row2.createCell(28).setCellValue(row.getField("Amount"));
				row2.getCell(28).setCellStyle(style5);
				row2.createCell(29).setCellValue(row.getField("Collection Type"));
				row2.getCell(29).setCellStyle(style5);
				row2.createCell(30).setCellValue(row.getField("Mismatch Interpretation"));
				row2.getCell(30).setCellStyle(style5);
				row2.createCell(31).setCellValue(row.getField("Confidence"));
				row2.getCell(31).setCellStyle(style5);
				row2.createCell(32).setCellValue(row.getField("Action"));
				row2.getCell(32).setCellStyle(style4);
				row2.createCell(33).setCellValue(row.getField("Reason"));
				row2.getCell(33).setCellStyle(style4);
				row2.createCell(34).setCellValue(row.getField("Final TCS Section"));
				row2.getCell(34).setCellStyle(style4);
				row2.createCell(35).setCellValue(row.getField("Final TCS Rate"));
				row2.getCell(35).setCellStyle(style4);
				row2.createCell(36).setCellValue(row.getField("Final TCS Amount"));
				row2.getCell(36).setCellStyle(style4);
				row2.createCell(37).setCellValue(row.getField("Payment Id"));
				row2.getCell(37).setCellStyle(style5);
				row2.createCell(38).setCellValue(row.getField("Payment Amount"));
				row2.getCell(38).setCellStyle(style5);
				row2.createCell(39).setCellValue(row.getField("Cess TCS Rate"));
				row2.getCell(39).setCellStyle(style5);
				row2.createCell(40).setCellValue(row.getField("Surcharge TCS Rate"));
				row2.getCell(40).setCellStyle(style5);
				row2.createCell(41).setCellValue(row.getField("Collectee Type"));
				row2.getCell(41).setCellStyle(style5);
				row2.createCell(42).setCellValue(row.getField("Consumed Amount"));
				row2.getCell(42).setCellStyle(style5);
				index++;
				sequenceNumber++;
			}
			wb.write(out);
		}
		return out;
	}

	/**
	 * calculating advance matrix values
	 * 
	 * @param deductorTan
	 * @return
	 */
	public Map<Integer, Object> getPaymentMatrix(String deductorTan, int yearFromUI) {

		Map<Integer, Object> advanceMatrixValues = new HashMap<>();

		List<TcsMatrixDTO> matrixValues = paymentDAO.getPaymentMatrix(deductorTan, yearFromUI);

		for (TcsMatrixDTO matrixDTO : matrixValues) {
			advanceMatrixValues.put(matrixDTO.getMonth(), matrixDTO);
		}

		/*
		 * BigDecimal sumOfPaymentOpeningAmount = new BigDecimal(
		 * advanceRepository.getTotalPaymentAmount(yearFromUI, deductorTan));
		 * 
		 * BigDecimal sumOfPaymentUtilizationAmount = new BigDecimal(
		 * advanceUtilizationRepository.getTotalPaymentUtilizedAmount(yearFromUI,
		 * deductorTan));
		 * 
		 * BigDecimal finalOpeningAmount =
		 * sumOfPaymentOpeningAmount.subtract(sumOfPaymentUtilizationAmount);
		 * 
		 * for (int month = 4; month <= 12; month++) {
		 * 
		 * finalOpeningAmount = matrixCaluclationByMonth(deductorTan,
		 * advanceMatrixValues, yearFromUI, finalOpeningAmount, month); }
		 * 
		 * for (int month = 1; month <= 3; month++) {
		 * 
		 * finalOpeningAmount = matrixCaluclationByMonth(deductorTan,
		 * advanceMatrixValues, yearFromUI, finalOpeningAmount, month); }
		 */

		return advanceMatrixValues;

	}

	/*
	 * private BigDecimal matrixCaluclationByMonth(String deductorTan, Map<Integer,
	 * Object> advanceMatrixValues, Integer assessmentYear, BigDecimal
	 * finalOpeningAmount, int month) { BigDecimal sumOfPaymentFTMs = new
	 * BigDecimal( advanceRepository.getTotalPaymentAmountByMonth(deductorTan,
	 * assessmentYear, month));
	 * 
	 * BigDecimal sumOfPaymentAdjustmentAmount = new BigDecimal(
	 * advanceUtilizationRepository.getTotalPaymentUtilizedAmountByMonth(
	 * assessmentYear, month, deductorTan)); BigDecimal closingAmount =
	 * (finalOpeningAmount.add(sumOfPaymentFTMs)).subtract(
	 * sumOfPaymentAdjustmentAmount); MatrixDTO matrixDTO = new MatrixDTO();
	 * matrixDTO.setOpeningAmount(finalOpeningAmount.setScale(2, RoundingMode.UP));
	 * matrixDTO.setFtm(sumOfPaymentFTMs.setScale(2, RoundingMode.UP));
	 * matrixDTO.setAdjustmentAmount(sumOfPaymentAdjustmentAmount.setScale(2,
	 * RoundingMode.UP)); matrixDTO.setClosingAmount(closingAmount.setScale(2,
	 * RoundingMode.UP)); advanceMatrixValues.put(month, matrixDTO);
	 * finalOpeningAmount = closingAmount; return finalOpeningAmount; }
	 */

	public Map<Integer, Object> getPaymentMatrixByDeducteePan(String deductorTan, int yearFromUI, String deducteePan) {

		Map<Integer, Object> advanceMatrixValues = new HashMap<>();
		// initial sum values

		BigDecimal sumOfPaymentOpeningAmount = null;// TODO NEED TO CHANGE FOR SQL
		// new BigDecimal(
		// advanceRepository.getTotalPaymentAmountByPan(yearFromUI, deductorTan,
		// deducteePan));

		// list of advance utilization amount

		BigDecimal sumOfPaymentUtilizationAmount = null;
		// new BigDecimal(
		// advanceUtilizationRepository.getTotalPaymentUtilizedAmountBypan(yearFromUI,
		// deductorTan, deducteePan));

		BigDecimal finalOpeningAmount = null;
		// sumOfPaymentOpeningAmount.subtract(sumOfPaymentUtilizationAmount);

		for (int month = 4; month <= 12; month++) {

			finalOpeningAmount = matrixCaluclationByMonthAndPan(deductorTan, advanceMatrixValues, yearFromUI,
					finalOpeningAmount, month, deducteePan);
		}

		for (int month = 1; month <= 3; month++) {

			finalOpeningAmount = matrixCaluclationByMonthAndPan(deductorTan, advanceMatrixValues, yearFromUI,
					finalOpeningAmount, month, deducteePan);
		}

		return advanceMatrixValues;

	}

	private BigDecimal matrixCaluclationByMonthAndPan(String deductorTan, Map<Integer, Object> advanceMatrixValues,
			Integer assessmentYear, BigDecimal finalOpeningAmount, int month, String deducteePan) {

		// list of ftm amount

		BigDecimal sumOfPaymentFTMs = null;// TODO NEED TO DELETE AFTER 1 WEEK
		// new BigDecimal(advanceRepository
		// .getTotalPaymentAmountByMonthByDeducteePan(deductorTan, assessmentYear,
		// month, deducteePan));

		// list of Adjustment amount

		BigDecimal sumOfPaymentAdjustmentAmount = null; // TODO NEED TO DELETE AFTER 1 WEEK
		// new BigDecimal(advanceUtilizationRepository
		// .getTotalPaymentUtilizedAmountByMonthAndPan(assessmentYear, month,
		// deductorTan, deducteePan));
		BigDecimal closingAmount = (finalOpeningAmount.add(sumOfPaymentFTMs)).subtract(sumOfPaymentAdjustmentAmount);
		MatrixDTO matrixDTO = new MatrixDTO();
		matrixDTO.setOpeningAmount(finalOpeningAmount.setScale(2, RoundingMode.UP));
		matrixDTO.setFtm(sumOfPaymentFTMs.setScale(2, RoundingMode.UP));
		matrixDTO.setAdjustmentAmount(sumOfPaymentAdjustmentAmount.setScale(2, RoundingMode.UP));
		matrixDTO.setClosingAmount(closingAmount.setScale(2, RoundingMode.UP));
		advanceMatrixValues.put(month, matrixDTO);
		finalOpeningAmount = closingAmount;
		return finalOpeningAmount;
	}

	/**
	 * 
	 * @param deductorTan
	 * @param assessmentYear
	 * @param month
	 * @param isOpening
	 * @param tenantId
	 * @param userName
	 * @param type
	 * @param batchUploadYear
	 * @return
	 * @throws Exception
	 */
	@Async
	public ByteArrayInputStream asyncPaymentMatrixOpeningAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, boolean isOpening, String tenantId, String userName, String type, Integer batchUploadYear)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return advanceMatrixClosingAmountDownload(deductorTan, assessmentYear, month, isOpening, tenantId, userName,
				type, batchUploadYear);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param assessmentYear
	 * @param month
	 * @param isOpening
	 * @param tenantId
	 * @param userName
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@Async
	public ByteArrayInputStream asyncPaymentMatrixClosingAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, boolean isOpening, String tenantId, String userName, String type) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return advanceMatrixClosingAmountDownload(deductorTan, assessmentYear, month, isOpening, tenantId, userName,
				type, assessmentYear);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param assessmentYear
	 * @param month
	 * @param isOpening
	 * @param tenantId
	 * @param userName
	 * @param type
	 * @param batchUploadYear
	 * @return
	 * @throws Exception
	 */
	public ByteArrayInputStream advanceMatrixClosingAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, boolean isOpening, String tenantId, String userName, String type, Integer batchUploadYear)
			throws Exception {
		logger.info("Closing payment amount download request for Year : {} Month : {}", assessmentYear, month);
		String fileName = "";
		if (UploadTypes.PAYMENT_CLOSING_REPORT.name().equalsIgnoreCase(type)) {
			fileName = "YTD_Payment_Closing_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		} else {
			fileName = "YTD_Payment_Opening_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TCSBatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, batchUploadYear, null, 0L, type,
				"Processing", month, userName, null, fileName);
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Payment Matrix");

		String[] headerNames = new String[] { "Collector Tan", "Closing Amount", "Adjusted Amount", "Closing Amount",
				"Collectee Pan", "Colectee Name", "Posting Date", "TCS Amount" };
		if (isOpening) {
			headerNames = new String[] { "Collector Tan", "Opening Amount", "Colectee Pan", "Collectee Name",
					"Posting Date", "TCS Amount" };
		}

		List<TcsMatrixDTO> matrixValues = paymentDAO.getPaymentClosingMatrixReport(deductorTan, assessmentYear, month);

		worksheet.getCells().importArray(headerNames, 0, 0, false);
		// list of advance ftm amounts
		int rowIndex = 1;
		BigDecimal ftmSum = BigDecimal.valueOf(0);
		BigDecimal closingSum = BigDecimal.valueOf(0);
		BigDecimal adjustedSum = BigDecimal.valueOf(0);

		for (TcsMatrixDTO matrixDTO : matrixValues) {

			List<Object> rowData = new ArrayList<>();
			rowData.add(StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
			if (!isOpening) {
				rowData.add(matrixDTO.getOpeningAmount());
				rowData.add(matrixDTO.getAdjustmentAmount());
			}
			rowData.add(matrixDTO.getClosingAmount());
			rowData.add(
					StringUtils.isBlank(matrixDTO.getDeducteePan()) ? StringUtils.EMPTY : matrixDTO.getDeducteePan());
			rowData.add(
					StringUtils.isBlank(matrixDTO.getDeducteeName()) ? StringUtils.EMPTY : matrixDTO.getDeducteeName());
			rowData.add(matrixDTO.getPostingDateOfDocument() == null ? StringUtils.EMPTY
					: matrixDTO.getPostingDateOfDocument());
			rowData.add(matrixDTO.getTdsAmount() == null ? StringUtils.EMPTY : matrixDTO.getTdsAmount());

			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);

			if (matrixDTO.getFtm() != null && !isOpening) {
				ftmSum = ftmSum.add(matrixDTO.getFtm());
				adjustedSum = adjustedSum.add(matrixDTO.getAdjustmentAmount());
			}
			closingSum = closingSum.add(matrixDTO.getClosingAmount());
		}

		// Sum of Adjustement Amount and Utilization amount
		rowIndex = rowIndex + 2;
		List<Object> rowData = new ArrayList<>();
		rowData.add("Total");
		if (!isOpening) {
			rowData.add(ftmSum);
			rowData.add(adjustedSum);
		}
		rowData.add(closingSum);
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
		worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();

		// Style for A1 to B1 headers
		Cell a1 = worksheet.getCells().get("A1");
		Style style1 = a1.getStyle();
		style1.setForegroundColor(Color.fromArgb(189, 215, 238));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		Range headerColorRange = worksheet.getCells().createRange("A1:B1");
		headerColorRange.setStyle(style1);

		Cell a3 = worksheet.getCells().get("C1");
		Style style3 = a3.getStyle();
		style3.setForegroundColor(Color.fromArgb(255, 255, 0));
		style3.setPattern(BackgroundType.SOLID);
		style3.getFont().setBold(true);
		a3.setStyle(style3);

		Cell a4 = worksheet.getCells().get("D1");
		Style style4 = a4.getStyle();
		style4.setForegroundColor(Color.fromArgb(255, 230, 153));
		style4.setPattern(BackgroundType.SOLID);
		style4.getFont().setBold(true);
		a4.setStyle(style4);

		// Style for E1 to F1 headers
		Style style5 = workbook.createStyle();
		style5.setForegroundColor(Color.fromArgb(255, 255, 0));
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(true);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("E1:F1");
		headerColorRange1.setStyle(style5);

		if (!isOpening) {

			Cell a7 = worksheet.getCells().get("G1");
			a7.setStyle(style5);

			Cell a8 = worksheet.getCells().get("H1");
			a8.setStyle(style5);
		}

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		if (isOpening) {
			autoFilter.setRange("A1:F1");
		} else {
			autoFilter.setRange("A1:H1");
		}

		workbook.save(out, SaveFormat.XLSX);
		saveMatrixReport(deductorTan, tenantId, batchUploadYear, out, 1L, type, "Processed", month, userName,
				batchUpload.getId(), null);
		return new ByteArrayInputStream(out.toByteArray());
	}

	/**
	 * 
	 * @param deductorTan
	 * @param assessmentYear
	 * @param month
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@Async
	public ByteArrayInputStream asyncPaymentMatrixFtmDownload(String deductorTan, Integer assessmentYear, Integer month,
			String tenantId, String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return advanceMatrixFtmDownload(deductorTan, assessmentYear, month, tenantId, userName);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param assessmentYear
	 * @param month
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public ByteArrayInputStream advanceMatrixFtmDownload(String deductorTan, int assessmentYear, int month,
			String tenantId, String userName) throws Exception {

		logger.info("Payment matrix ftm download request for Year : {} Month : {}", assessmentYear, month);
		String fileName = "FTM_Payment_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TCSBatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, assessmentYear, null, 0L,
				UploadTypes.PAYMENT_FTM_REPORT.name(), "Processing", month, userName, null, fileName);

		// list of advance ftm amounts
		List<TcsPaymentDTO> advanceFTMs = new ArrayList<>();

		List<TcsPaymentDTO> advances = paymentDAO.getPaymentFTM(deductorTan, assessmentYear, month);
		advanceFTMs.addAll(advances);

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("payment Matrix FTM");
		ArrayList<TcsMatrixFileDTO> matrixFileDTOList = new ArrayList<>();

		for (TcsPaymentDTO advance : advanceFTMs) {
			matrixFileDTOList.add(new TcsMatrixFileDTO(advance.getCollectorPan(), advance.getCollectorTan(),
					/* advance.getDeductorGstin() */"", advance.getCollecteePan(), /* advance.getDeducteeTin() */"",
					/* advance.getDeducteeGstin() */"", advance.getAmount(), new BigDecimal(0)));

		}

		ImportTableOptions tableOptions = new ImportTableOptions();

		if (!matrixFileDTOList.isEmpty()) {
			// Insert data to excel template
			worksheet.getCells().importCustomObjects(matrixFileDTOList, 0, 0, tableOptions);
		}
		// setting colors to the first columns
		Cell a1 = worksheet.getCells().get("A1");

		Style style1 = a1.getStyle();
		style1.setForegroundColor(Color.fromArgb(189, 215, 238));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		a1.setStyle(style1);

		Cell a2 = worksheet.getCells().get("B1");
		Style style2 = a2.getStyle();
		style2.setForegroundColor(Color.fromArgb(189, 215, 238));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		a2.setStyle(style2);

		Cell a3 = worksheet.getCells().get("C1");
		Style style3 = a3.getStyle();
		style3.setForegroundColor(Color.fromArgb(255, 255, 0));
		style3.setPattern(BackgroundType.SOLID);
		style3.getFont().setBold(true);
		a3.setStyle(style3);

		Cell a4 = worksheet.getCells().get("D1");
		Style style4 = a4.getStyle();
		style4.setForegroundColor(Color.fromArgb(255, 230, 153));
		style4.setPattern(BackgroundType.SOLID);
		style4.getFont().setBold(true);
		a4.setStyle(style4);

		Cell a5 = worksheet.getCells().get("E1");
		Style style5 = a5.getStyle();
		style5.setForegroundColor(Color.fromArgb(255, 255, 0));
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(true);
		a5.setStyle(style5);

		Cell a6 = worksheet.getCells().get("F1");
		Style style6 = a6.getStyle();
		style6.setForegroundColor(Color.fromArgb(255, 255, 0));
		style6.setPattern(BackgroundType.SOLID);
		style6.getFont().setBold(true);
		a6.setStyle(style6);

		Cell a7 = worksheet.getCells().get("G1");

		Style style7 = a7.getStyle();
		style7.setForegroundColor(Color.fromArgb(255, 255, 0));
		style7.setPattern(BackgroundType.SOLID);
		style7.getFont().setBold(true);
		a7.setStyle(style7);

		Cell a8 = worksheet.getCells().get("H1");
		Style style8 = a8.getStyle();
		style8.setForegroundColor(Color.fromArgb(255, 255, 0));
		style8.setPattern(BackgroundType.SOLID);
		style8.getFont().setBold(true);
		a8.setStyle(style8);

		if (matrixFileDTOList.isEmpty()) {
			a1.putValue("Collector PAN");
			a2.putValue("Collector TAN");
			a3.putValue("Collector GSTIN");
			a4.putValue("Collectee PAN");
			a5.putValue("Collectee TIN");
			a6.putValue("Collectee GSTIN");
			a7.putValue("Amount");
			a8.putValue("Utilization Amount");
			// Insert data to excel template
			worksheet.getCells().importCustomObjects(matrixFileDTOList, 0, 0, tableOptions);
		}

		worksheet.autoFitColumns();
		worksheet.setGridlinesVisible(false);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String cellname = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);
		Range range;
		if (!cellname.equalsIgnoreCase("h1")) {
			range = worksheet.getCells().createRange("A2:" + cellname);
			Style style = workbook.createStyle();
			style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
			style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
			style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
			style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

			Iterator<?> cellArray = range.iterator();
			while (cellArray.hasNext()) {
				Cell temp = (Cell) cellArray.next();
				// Saving the modified style to the cell.
				temp.setStyle(style);
			}
		} else {
			range = worksheet.getCells().createRange("A1:" + cellname);
			range.setOutlineBorders(CellBorderType.THIN, Color.getBlack());
		}

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:H1");

		workbook.save(out, SaveFormat.XLSX);
		saveMatrixReport(deductorTan, tenantId, assessmentYear, out, 1L, UploadTypes.PAYMENT_FTM_REPORT.name(),
				"Processed", month, userName, batchUpload.getId(), null);
		return new ByteArrayInputStream(out.toByteArray());
	}

	/**
	 * 
	 * @param deductorTan
	 * @param assessmentYear
	 * @param month
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@Async
	public ByteArrayInputStream asyncPaymentMatrixAdjustedFileDownload(String deductorTan, Integer assessmentYear,
			Integer month, String tenantId, String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return advanceMatrixAdjustedFileDownload(deductorTan, assessmentYear, month, tenantId, userName);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param assessmentYear
	 * @param month
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public ByteArrayInputStream advanceMatrixAdjustedFileDownload(String deductorTan, int assessmentYear, int month,
			String tenantId, String userName) throws Exception {
		logger.info("Payment matrix adjusted download request for Year : {} Month : {}", assessmentYear, month);
		String fileName = "Payment_Adjusted_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TCSBatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, assessmentYear, null, 1L,
				UploadTypes.PAYMENT_ADJUSTED_REPORT.name(), "Processing", month, userName, null, fileName);

		// list of advance ftm amounts
		List<TcsPaymentUtilization> advanceAdjustments = new ArrayList<>();
		List<TcsPaymentUtilization> advanceUtilizations = paymentUtilizationDAO.getPaymentAdjustment(deductorTan,
				assessmentYear, month);

		advanceAdjustments.addAll(advanceUtilizations);

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Payment Matrix Adjustsments");
		worksheet.freezePanes(0, 4, 0, 4);
		worksheet.autoFitColumns();
		worksheet.autoFitRows();

		ArrayList<TcsUtilizationFileDTO> listutilizationFileDTO = new ArrayList<>();

		for (TcsPaymentUtilization advanceUtilization : advanceAdjustments) {

			TcsUtilizationFileDTO utilizationFileDTO = new TcsUtilizationFileDTO();

			// get the data based on invoice line item id.
			List<TCSInvoiceLineItem> invoiceLineItem = tcsInvoiceLineItemDAO
					.findTcsInvoiceId(advanceUtilization.getInvoiceLineItemId());

			utilizationFileDTO.setCollectorMasterTan(advanceUtilization.getCollectorMasterTan());
			utilizationFileDTO.setMasterPan(advanceUtilization.getPaymentMasterPan());
			utilizationFileDTO.setRemainingAmount(advanceUtilization.getRemainingAmount());
			utilizationFileDTO.setUtilizedAmount(advanceUtilization.getUtilizedAmount());
			if (!invoiceLineItem.isEmpty()) {
				BeanUtils.copyProperties(invoiceLineItem.get(0), utilizationFileDTO);
			}

			listutilizationFileDTO.add(utilizationFileDTO);

		}
		String[] advanceMatrixheaderNames = new String[] { "Collector master tan", "Payment master pan",
				"Remaining amount", "Utilized amount", "Source file name", "Company code", "Name of the company code",
				"Collector TAN", "Collector GSTIN", "Collectee PAN", "Collectee GSTIN", "Name of the collectee",
				"Collectee address", "Vendor invoice number", "Miro number", "Migo number", "Document type",
				"Document date", "Posting date of document", "Line item number", "HSN/SAC", "Sac description",
				"Service description - invoice", "Service description - so", "Service description - gl text",
				"Taxable value", "IGST rate", "IGST amount", "CGST rate", "CGST amount", "SGST rate", "SGST amount",
				"Cess rate", "Cess amount", "Creditable (Y/N)", "POS", "TCS section", "TCS rate", "TCS amount",
				"PO number", "PO date", "Linked advance number", "Grossing up indicator", "Original document number",
				"Original document date", "User defined field 1", "User defined field 2", "User defined field 3",
				"Final TCS amount", "Final TCS rate", "Final TCS section", "Actual TCS amount", "Actual TCS rate",
				"Actual TCS section", "Derived TCS amount", "Derived TCS rate", "Derived TCS section" };

		worksheet.getCells().importArray(advanceMatrixheaderNames, 0, 0, false);

		setPaymentMatrixHeaders(listutilizationFileDTO, worksheet);

		Cell a1 = worksheet.getCells().get("A1");
		Style style1 = a1.getStyle();
		style1.setForegroundColor(Color.fromArgb(189, 215, 238));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		a1.setStyle(style1);

		Cell a2 = worksheet.getCells().get("B1");
		Style style2 = a2.getStyle();
		style2.setForegroundColor(Color.fromArgb(189, 215, 238));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		a2.setStyle(style2);

		Cell a3 = worksheet.getCells().get("C1");
		Style style3 = a3.getStyle();
		style3.setForegroundColor(Color.fromArgb(255, 255, 0));
		style3.setPattern(BackgroundType.SOLID);
		style3.getFont().setBold(true);
		a3.setStyle(style3);

		Cell a4 = worksheet.getCells().get("D1");
		Style style4 = a4.getStyle();
		style4.setForegroundColor(Color.fromArgb(255, 230, 153));
		style4.setPattern(BackgroundType.SOLID);
		style4.getFont().setBold(true);
		a4.setStyle(style4);

		// Style for E1 to AK1 headers
		Style style5 = workbook.createStyle();
		style5.setForegroundColor(Color.fromArgb(91, 155, 213));
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(true);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("E1:BE1");
		headerColorRange1.setStyle(style5);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:BE1");

		workbook.save(out, SaveFormat.XLSX);
		saveMatrixReport(deductorTan, tenantId, assessmentYear, out, 1L, UploadTypes.PAYMENT_ADJUSTED_REPORT.name(),
				"Processed", month, userName, batchUpload.getId(), null);
		return new ByteArrayInputStream(out.toByteArray());
	}

	/**
	 * 
	 * @param listutilizationFileDTO
	 * @param worksheet
	 * @throws Exception
	 */
	private void setPaymentMatrixHeaders(ArrayList<TcsUtilizationFileDTO> listutilizationFileDTO, Worksheet worksheet)
			throws Exception {

		if (!listutilizationFileDTO.isEmpty()) {
			int rowIndex = 1;
			for (TcsUtilizationFileDTO utilizationFileDTO : listutilizationFileDTO) {
				List<Object> rowData = new ArrayList<>();
				setInvoiceDataForMatrixReports(utilizationFileDTO, rowData);
				worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
			}
		}
	}

	/**
	 * 
	 * @param utilizationFileDTO
	 * @param rowData
	 */
	public void setInvoiceDataForMatrixReports(TcsUtilizationFileDTO utilizationFileDTO, List<Object> rowData) {
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getCollectorMasterTan()) ? StringUtils.EMPTY
				: utilizationFileDTO.getCollectorMasterTan());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getMasterPan()) ? StringUtils.EMPTY
				: utilizationFileDTO.getMasterPan());
		rowData.add(utilizationFileDTO.getRemainingAmount());
		rowData.add(utilizationFileDTO.getUtilizedAmount());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getSourceFileName()) ? StringUtils.EMPTY
				: utilizationFileDTO.getSourceFileName());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getCollecteeCode()) ? StringUtils.EMPTY
				: utilizationFileDTO.getCollecteeCode());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getCollecteeCode()) ? StringUtils.EMPTY
				: utilizationFileDTO.getCollecteeCode());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getCollectorMasterTan()) ? StringUtils.EMPTY
				: utilizationFileDTO.getCollectorMasterTan());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getCollectorGstin()) ? StringUtils.EMPTY
				: utilizationFileDTO.getCollectorGstin());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getMasterPan()) ? StringUtils.EMPTY
				: utilizationFileDTO.getMasterPan());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getCollecteeGstin()) ? StringUtils.EMPTY
				: utilizationFileDTO.getCollecteeGstin());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getCollecteeName()) ? StringUtils.EMPTY
				: utilizationFileDTO.getCollecteeName());
		// setting as empty for now
		rowData.add(StringUtils.isBlank(/* utilizationFileDTO.getDeducteeAddress() */"") ? StringUtils.EMPTY
				: /* utilizationFileDTO.getDeducteeAddress() */"");
		rowData.add(StringUtils.isBlank(/* utilizationFileDTO.getVendorInvoiceNumber() */"") ? StringUtils.EMPTY
				: /* utilizationFileDTO.getVendorInvoiceNumber() */"");
		rowData.add(StringUtils.isBlank(/* utilizationFileDTO.getMiroNumber() */"") ? StringUtils.EMPTY
				: /* utilizationFileDTO.getMiroNumber() */"");
		rowData.add(StringUtils.isBlank(/* utilizationFileDTO.getMigoNumber() */"") ? StringUtils.EMPTY
				: /* utilizationFileDTO.getMigoNumber() */"");
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getDocumentType()) ? StringUtils.EMPTY
				: utilizationFileDTO.getDocumentType());
		rowData.add(utilizationFileDTO.getDocumentDate() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getDocumentDate());
		rowData.add((utilizationFileDTO.getPostingDate() == null) ? StringUtils.EMPTY
				: utilizationFileDTO.getPostingDate());
		rowData.add(
				utilizationFileDTO.getLineNumber() == null ? StringUtils.EMPTY : utilizationFileDTO.getLineNumber());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getHsnOrSac()) ? StringUtils.EMPTY
				: utilizationFileDTO.getHsnOrSac());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getHsnOrSacDesc()) ? StringUtils.EMPTY
				: utilizationFileDTO.getHsnOrSacDesc());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getInvoiceDesc()) ? StringUtils.EMPTY
				: utilizationFileDTO.getInvoiceDesc());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getSoDesc()) ? StringUtils.EMPTY
				: utilizationFileDTO.getSoDesc());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getGlDesc()) ? StringUtils.EMPTY
				: utilizationFileDTO.getGlDesc());
		rowData.add(utilizationFileDTO.getTcsAmount() == null ? StringUtils.EMPTY : utilizationFileDTO.getTcsAmount());
		rowData.add(
				utilizationFileDTO.getItcessRate() == null ? StringUtils.EMPTY : utilizationFileDTO.getItcessRate());
		rowData.add(
				utilizationFileDTO.getIgstAmount() == null ? StringUtils.EMPTY : utilizationFileDTO.getIgstAmount());
		// rowData.add(utilizationFileDTO.getCgstRate() == null ? StringUtils.EMPTY :
		// utilizationFileDTO.getCgstRate());
		// setting cgst rate as empty
		rowData.add(StringUtils.EMPTY);
		rowData.add(
				utilizationFileDTO.getCgstAmount() == null ? StringUtils.EMPTY : utilizationFileDTO.getCgstAmount());
		// rowData.add(utilizationFileDTO.getSgstRate() == null ? StringUtils.EMPTY :
		// utilizationFileDTO.getSgstRate());
		// setting cgst amount as empty
		rowData.add(StringUtils.EMPTY);
		rowData.add(
				utilizationFileDTO.getSgstAmount() == null ? StringUtils.EMPTY : utilizationFileDTO.getSgstAmount());
		rowData.add(
				utilizationFileDTO.getItcessRate() == null ? StringUtils.EMPTY : utilizationFileDTO.getItcessRate());
		rowData.add(utilizationFileDTO.getItcessAmount() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getItcessAmount());
		// rowData.add(utilizationFileDTO.getCreditable() == null ? StringUtils.EMPTY :
		// utilizationFileDTO.getCreditable());
		// setting creditable amount as empty
		rowData.add(StringUtils.EMPTY);

		// rowData.add(StringUtils.isBlank(utilizationFileDTO.getPos()) ?
		// StringUtils.EMPTY : utilizationFileDTO.getPos());
		// setting pos amount as empty
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getTcsSection()) ? StringUtils.EMPTY
				: utilizationFileDTO.getTcsSection());
		rowData.add(utilizationFileDTO.getTcsRate() == null ? StringUtils.EMPTY : utilizationFileDTO.getTcsRate());
		rowData.add(utilizationFileDTO.getTcsAmount() == null ? StringUtils.EMPTY : utilizationFileDTO.getTcsAmount());
		// rowData.add(StringUtils.isBlank(utilizationFileDTO.getPoNumber()) ?
		// StringUtils.EMPTY
		// : utilizationFileDTO.getPoNumber());
		// setting po number amount as empty
		rowData.add(StringUtils.EMPTY);
		// setting po date amount as empty
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getLinkedPaymentNumber()) ? StringUtils.EMPTY
				: utilizationFileDTO.getLinkedPaymentNumber());
		// rowData.add(StringUtils.isBlank(utilizationFileDTO.getGrossIndicator()) ?
		// StringUtils.EMPTY
		// : utilizationFileDTO.getGrossIndicator());
		// setting gross indicator amount as empty
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getOriginalDocumentNumber()) ? StringUtils.EMPTY
				: utilizationFileDTO.getOriginalDocumentNumber());
		rowData.add(utilizationFileDTO.getOriginalDocumentDate() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getOriginalDocumentDate());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getUserDefinedField1()) ? StringUtils.EMPTY
				: utilizationFileDTO.getUserDefinedField1());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getUserDefinedField2()) ? StringUtils.EMPTY
				: utilizationFileDTO.getUserDefinedField2());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getUserDefinedField3()) ? StringUtils.EMPTY
				: utilizationFileDTO.getUserDefinedField3());
		// Added 9 extra columns
		// Final Tcs Amount
		rowData.add(utilizationFileDTO.getFinalTcsAmount() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getFinalTcsAmount());
		rowData.add(utilizationFileDTO.getFinalTcsRate() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getFinalTcsRate());
		rowData.add(utilizationFileDTO.getFinalTcsSection() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getFinalTcsSection());
		// Actual Tcs Amunt
		rowData.add(utilizationFileDTO.getTcsAmount() == null ? StringUtils.EMPTY : utilizationFileDTO.getTcsAmount());
		rowData.add(utilizationFileDTO.getTcsRate() == null ? StringUtils.EMPTY : utilizationFileDTO.getTcsRate());
		rowData.add(
				utilizationFileDTO.getTcsSection() == null ? StringUtils.EMPTY : utilizationFileDTO.getTcsSection());
		// Derived Tcs Amount
		rowData.add(utilizationFileDTO.getDerivedTcsAmount() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getDerivedTcsAmount());
		rowData.add(utilizationFileDTO.getDerivedTcsRate() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getDerivedTcsRate());
		rowData.add(utilizationFileDTO.getDerivedTcsSection() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getDerivedTcsSection());

	}

	/**
	 * 
	 * @param csvFile
	 * @param deductorTan
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 * @throws Exception
	 */
	// Payment error Report generate
	public File convertPaymentCsvToXlsx(File csvFile, String deductorTan, String tenantId, String deductorPan,
			Integer assesmentYear, TCSBatchUpload batchUpload) throws Exception {

		// Aspose method to convert
		Workbook workbook = advanceXlsxReport(csvFile, deductorTan, tenantId, deductorPan, assesmentYear, batchUpload);

		// Write the output to the file
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxPaymentFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()) + "_Error_Report",
				".xlsx");

		FileUtils.writeByteArrayToFile(xlsxPaymentFile, baout.toByteArray());
		baout.close();
		return xlsxPaymentFile;
	}

	/**
	 * 
	 * @param advanceErrorReportsCsvList
	 * @param tan
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 * @throws Exception
	 */
	public Workbook advanceXlsxReport(File csvFile, String tan, String tenantId, String deductorPan,
			Integer assesmentYear, TCSBatchUpload batchUpload) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		String[] headerNames = new String[] { "Collector TAN", "Error message", "Sequence number", "SourceIdentifier",
				"SourceFileName", "CollectorCode", "CollectorPAN", "CollectorTAN", "CollecteeCode", "CollecteePAN",
				"DocumentType", "DocumentNumber", "DocumentDate", "ClearingDocumentNumber", "ClearingDate",
				"InvoiceDocumentNumber", "InvoiceDate", "LineNumber", "PaymentDesc", "GLAccountCode", "GLDesc",
				" Amount ", "TCSSection", "TCSRate", "TCSAmount", "SurchargeRate", "SurchargeAmount", "ITCESSRate",
				"ITCESSAmount", "TDSSection", "TDSRate", "TDSAmount", "Userdefinedfield1", "Userdefinedfield2",
				"Userdefinedfield3", "Userdefinedfield4", "Userdefinedfield5", "Userdefinedfield6" };

		worksheet.getCells().importArray(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		// CR error reports data
		List<TcsPaymentDTO> crErrorRecords = paymentDAO.getCrErrorRecords(tan, assesmentYear, batchUpload.getId());

		if (crErrorRecords.isEmpty()) {
			Reader reader = new FileReader(csvFile);
			@SuppressWarnings({ "unchecked", "rawtypes" })
			CsvToBean<PaymentErrorReportCsvDto> csvToBean = new CsvToBeanBuilder(reader)
					.withType(PaymentErrorReportCsvDto.class).withIgnoreLeadingWhiteSpace(true).build();
			setExtractDataForPaymentFromCsv(csvToBean.parse(), worksheet);
		} else {
			setExtractDataForPayment(crErrorRecords, worksheet);
		}
		// int rowIndex = 6;
		// setCrErrorRecordsData(crErrorRecords, worksheet, rowIndex);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
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

		// Style for E6 to AT6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:AL6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());
		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("Payment Error Report (Dated: " + date + ")");
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
		Range heardersRange = worksheet.getCells().createRange("B5:B5");
		heardersRange.merge();

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "AL6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:AL6");

		return workbook;
	}

	private Integer setExtractDataForPaymentFromCsv(List<PaymentErrorReportCsvDto> advanceErrorReportsCsvList,
			Worksheet worksheet) throws Exception {

		int rowIndex = 6;
		List<ErrorCode> errorCodesObjs = mastersClient.getAllErrorCodes().getBody().getData();
		Map<String, String> errorDescription = new HashMap<>();
		for (ErrorCode errorCodesObj : errorCodesObjs) {
			errorDescription.put(errorCodesObj.getCode(), errorCodesObj.getDescription());
		}
		for (PaymentErrorReportCsvDto advanceErrorReport : advanceErrorReportsCsvList) {
			ArrayList<Object> rowData = new ArrayList<Object>();
			String errorCodesWithNewLine = StringUtils.EMPTY;
			// getting error codes and spliting
			Map<String, String> errorCodesMap = new HashMap<>();
			List<String> givenCodes = new ArrayList<>();
			List<String> errorCodesWithOnlyDes = new ArrayList<>();
			if (StringUtils.isEmpty(advanceErrorReport.getReason()) || !advanceErrorReport.getReason().contains("-")) {
				errorCodesWithNewLine = StringUtils.isBlank(advanceErrorReport.getReason()) ? StringUtils.EMPTY
						: advanceErrorReport.getReason().trim().endsWith("/")
								? advanceErrorReport.getReason().trim().substring(0,
										advanceErrorReport.getReason().trim().length() - 1)
								: advanceErrorReport.getReason().trim();
			} else {
				String[] errorWithColumns = advanceErrorReport.getReason().split("/");
				for (String errorWithColumn : errorWithColumns) {
					if (errorWithColumn.contains("-")) {
						String[] erroCodes = errorWithColumn.split("-");
						if (erroCodes.length > 1) {
							errorCodesMap.put(erroCodes[0].toLowerCase(), erroCodes[1]);
							for (String e : erroCodes[1].split("&")) {
								givenCodes.add(e);
							}
						} else if (erroCodes.length > 0) {
							errorCodesWithOnlyDes.add("Error with " + erroCodes[0]);
						}
					} else {
						errorCodesWithOnlyDes.add(errorWithColumn);
					}
				}
				List<String> errorCodes = new ArrayList<>();
				for (String givenCode : givenCodes) {
					errorCodes.add(errorDescription.get(givenCode));
				}
				errorCodes.addAll(errorCodesWithOnlyDes);
				errorCodesWithNewLine = String.join("\n", errorCodes);
			}

			for (Map.Entry<String, String> entry : excelHeaderMap.entrySet()) {
				errorReportService.setColorsBasedOnErrorCodes(worksheet, rowIndex, errorCodesMap, entry.getKey(),
						entry.getValue());
			}
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollectorTan()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollectorTan());
			rowData.add(StringUtils.isBlank(errorCodesWithNewLine) ? StringUtils.EMPTY : errorCodesWithNewLine);
			rowData.add(advanceErrorReport.getIndex());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSourceIdentifier()) ? StringUtils.EMPTY
					: advanceErrorReport.getSourceIdentifier());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSourceFileName()) ? StringUtils.EMPTY
					: advanceErrorReport.getSourceFileName());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCompanyCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getCompanyCode());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollectorPan()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollectorPan());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollectorTan()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollectorTan());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollecteeCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollecteeCode());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollecteePan()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollecteePan());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentType()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentType());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentNumber());
			rowData.add(advanceErrorReport.getDocumentDate());

			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentNumber());
			rowData.add(advanceErrorReport.getClearingDate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getClearingDate());
			rowData.add(advanceErrorReport.getInvoiceDocumentNumber() == null ? StringUtils.EMPTY
					: advanceErrorReport.getInvoiceDocumentNumber());
			rowData.add(advanceErrorReport.getInvoiceDate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getInvoiceDate());
			rowData.add(advanceErrorReport.getLineNumber() == null ? StringUtils.EMPTY
					: advanceErrorReport.getLineNumber());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getPaymentDesc()) ? StringUtils.EMPTY
					: advanceErrorReport.getPaymentDesc());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getGlAccountCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getGlAccountCode());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getGlDesc()) ? StringUtils.EMPTY
					: advanceErrorReport.getGlDesc());
			rowData.add(advanceErrorReport.getAmount() == null ? StringUtils.EMPTY : advanceErrorReport.getAmount());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getActualTcsSection()) ? StringUtils.EMPTY
					: advanceErrorReport.getActualTcsSection());
			rowData.add(advanceErrorReport.getActualTcsRate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getActualTcsRate());
			rowData.add(advanceErrorReport.getActualTcsAmount() == null ? StringUtils.EMPTY
					: advanceErrorReport.getActualTcsAmount());
			rowData.add(advanceErrorReport.getSurchargeRate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getSurchargeRate());
			rowData.add(advanceErrorReport.getSurchargeAmount() == null ? StringUtils.EMPTY
					: advanceErrorReport.getSurchargeAmount());
			rowData.add(advanceErrorReport.getItcessRate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getItcessRate());
			rowData.add(advanceErrorReport.getItcessAmount() == null ? StringUtils.EMPTY
					: advanceErrorReport.getItcessAmount());

			rowData.add(StringUtils.isBlank(advanceErrorReport.getTdsSection()) ? StringUtils.EMPTY
					: advanceErrorReport.getTdsSection());
			rowData.add(advanceErrorReport.getTdsRate() == null ? StringUtils.EMPTY : advanceErrorReport.getTdsRate());
			rowData.add(
					advanceErrorReport.getTdsAmount() == null ? StringUtils.EMPTY : advanceErrorReport.getTdsAmount());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField1()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField1());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField2()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField2());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField3()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField3());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField4()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField4());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField5()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField5());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField6()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField6());

			worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);

		}
		return rowIndex;
	}

	private Integer setExtractDataForPayment(List<TcsPaymentDTO> advanceErrorReportsCsvList, Worksheet worksheet)
			throws Exception {

		int rowIndex = 6;
		Integer sequenceNumber = 1;
		List<ErrorCode> errorCodesObjs = mastersClient.getAllErrorCodes().getBody().getData();
		Map<String, String> errorDescription = new HashMap<>();
		for (ErrorCode errorCodesObj : errorCodesObjs) {
			errorDescription.put(errorCodesObj.getCode(), errorCodesObj.getDescription());
		}
		for (TcsPaymentDTO advanceErrorReport : advanceErrorReportsCsvList) {
			ArrayList<Object> rowData = new ArrayList<Object>();
			String errorCodesWithNewLine = StringUtils.EMPTY;
			// getting error codes and spliting
			Map<String, String> errorCodesMap = new HashMap<>();
			List<String> givenCodes = new ArrayList<>();
			List<String> errorCodesWithOnlyDes = new ArrayList<>();
			if (StringUtils.isEmpty(advanceErrorReport.getErrorReason())
					|| !advanceErrorReport.getErrorReason().contains("-")) {
				errorCodesWithNewLine = StringUtils.isBlank(advanceErrorReport.getErrorReason()) ? StringUtils.EMPTY
						: advanceErrorReport.getErrorReason().trim().endsWith("/")
								? advanceErrorReport.getErrorReason().trim().substring(0,
										advanceErrorReport.getErrorReason().trim().length() - 1)
								: advanceErrorReport.getErrorReason().trim();
			} else {
				String[] errorWithColumns = advanceErrorReport.getErrorReason().split("/");
				for (String errorWithColumn : errorWithColumns) {
					if (errorWithColumn.contains("-")) {
						String[] erroCodes = errorWithColumn.split("-");
						if (erroCodes.length > 1) {
							errorCodesMap.put(erroCodes[0].toLowerCase(), erroCodes[1]);
							for (String e : erroCodes[1].split("&")) {
								givenCodes.add(e);
							}
						} else if (erroCodes.length > 0) {
							errorCodesWithOnlyDes.add("Error with " + erroCodes[0]);
						}
					} else {
						errorCodesWithOnlyDes.add(errorWithColumn);
					}
				}
				List<String> errorCodes = new ArrayList<>();
				for (String givenCode : givenCodes) {
					errorCodes.add(errorDescription.get(givenCode));
				}
				errorCodes.addAll(errorCodesWithOnlyDes);
				errorCodesWithNewLine = String.join("\n", errorCodes);
			}

			for (Map.Entry<String, String> entry : excelHeaderMap.entrySet()) {
				errorReportService.setColorsBasedOnErrorCodes(worksheet, rowIndex, errorCodesMap, entry.getKey(),
						entry.getValue());
			}

			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollectorTan()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollectorTan());
			rowData.add(StringUtils.isBlank(errorCodesWithNewLine) ? StringUtils.EMPTY : errorCodesWithNewLine);
			rowData.add(sequenceNumber);
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSourceIdentifier()) ? StringUtils.EMPTY
					: advanceErrorReport.getSourceIdentifier());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSourceFileName()) ? StringUtils.EMPTY
					: advanceErrorReport.getSourceFileName());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollectorCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollectorCode());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollectorPan()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollectorPan());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollectorTan()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollectorTan());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollecteeCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollecteeCode());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollecteePan()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollecteePan());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentType()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentType());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentNumber());
			rowData.add(advanceErrorReport.getDocumentDate());

			rowData.add(StringUtils.isBlank(advanceErrorReport.getClearingDocumentNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getClearingDocumentNumber());
			rowData.add(advanceErrorReport.getClearingDate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getClearingDate());
			rowData.add(advanceErrorReport.getInvoiceDocumentNumber() == null ? StringUtils.EMPTY
					: advanceErrorReport.getInvoiceDocumentNumber());
			rowData.add(advanceErrorReport.getInvoiceDate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getInvoiceDate());
			rowData.add(advanceErrorReport.getLineNumber() == null ? StringUtils.EMPTY
					: advanceErrorReport.getLineNumber());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getPaymentDesc()) ? StringUtils.EMPTY
					: advanceErrorReport.getPaymentDesc());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getGlAccountCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getGlAccountCode());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getGlDesc()) ? StringUtils.EMPTY
					: advanceErrorReport.getGlDesc());
			rowData.add(advanceErrorReport.getAmount() == null ? StringUtils.EMPTY : advanceErrorReport.getAmount());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getActualTcsSection()) ? StringUtils.EMPTY
					: advanceErrorReport.getActualTcsSection());
			rowData.add(advanceErrorReport.getActualTcsRate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getActualTcsRate());
			rowData.add(advanceErrorReport.getActualTcsAmount() == null ? StringUtils.EMPTY
					: advanceErrorReport.getActualTcsAmount());
			rowData.add(advanceErrorReport.getSurchargeRate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getSurchargeRate());
			rowData.add(advanceErrorReport.getSurchargeAmount() == null ? StringUtils.EMPTY
					: advanceErrorReport.getSurchargeAmount());
			rowData.add(advanceErrorReport.getItcessRate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getItcessRate());
			rowData.add(advanceErrorReport.getItcessAmount() == null ? StringUtils.EMPTY
					: advanceErrorReport.getItcessAmount());

			rowData.add(StringUtils.isBlank(advanceErrorReport.getTdsSection()) ? StringUtils.EMPTY
					: advanceErrorReport.getTdsSection());
			rowData.add(advanceErrorReport.getTdsRate() == null ? StringUtils.EMPTY : advanceErrorReport.getTdsRate());
			rowData.add(
					advanceErrorReport.getTdsAmount() == null ? StringUtils.EMPTY : advanceErrorReport.getTdsAmount());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField1()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField1());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField2()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField2());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField3()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField3());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField4()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField4());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField5()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField5());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField6()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField6());

			worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			sequenceNumber++;
		}
		return rowIndex;
	}

	private void setCrErrorRecordsData(List<TcsPaymentDTO> advanceErrorReportsCsvList, Worksheet worksheet,
			int rowIndex) throws Exception {

		Integer sequenceNumber = 1;
		for (TcsPaymentDTO advanceErrorReport : advanceErrorReportsCsvList) {
			ArrayList<Object> rowData = new ArrayList<Object>();

			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollectorTan()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollectorTan());
			rowData.add(advanceErrorReport.getErrorReason());
			rowData.add(sequenceNumber++);
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSourceIdentifier()) ? StringUtils.EMPTY
					: advanceErrorReport.getSourceIdentifier());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSourceFileName()) ? StringUtils.EMPTY
					: advanceErrorReport.getSourceFileName());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollectorCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollectorCode());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollectorPan()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollectorPan());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollectorTan()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollectorTan());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollecteeCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollecteeCode());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getCollecteePan()) ? StringUtils.EMPTY
					: advanceErrorReport.getCollecteePan());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentType()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentType());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentNumber());
			rowData.add(advanceErrorReport.getDocumentDate());

			rowData.add(StringUtils.isBlank(advanceErrorReport.getClearingDocumentNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getClearingDocumentNumber());
			rowData.add(advanceErrorReport.getClearingDate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getClearingDate());
			rowData.add(advanceErrorReport.getInvoiceDocumentNumber() == null ? StringUtils.EMPTY
					: advanceErrorReport.getInvoiceDocumentNumber());
			rowData.add(advanceErrorReport.getInvoiceDate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getInvoiceDate());
			rowData.add(advanceErrorReport.getLineNumber() == null ? StringUtils.EMPTY
					: advanceErrorReport.getLineNumber());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getPaymentDesc()) ? StringUtils.EMPTY
					: advanceErrorReport.getPaymentDesc());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getGlAccountCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getGlAccountCode());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getGlDesc()) ? StringUtils.EMPTY
					: advanceErrorReport.getGlDesc());
			rowData.add(advanceErrorReport.getAmount() == null ? StringUtils.EMPTY : advanceErrorReport.getAmount());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getFinalTcsSection()) ? StringUtils.EMPTY
					: advanceErrorReport.getFinalTcsSection());
			rowData.add(advanceErrorReport.getTcsRate() == null ? StringUtils.EMPTY : advanceErrorReport.getTcsRate());
			rowData.add(
					advanceErrorReport.getTcsAmount() == null ? StringUtils.EMPTY : advanceErrorReport.getTcsAmount());
			rowData.add(advanceErrorReport.getSurchargeRate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getSurchargeRate());
			rowData.add(advanceErrorReport.getSurchargeAmount() == null ? StringUtils.EMPTY
					: advanceErrorReport.getSurchargeAmount());
			rowData.add(advanceErrorReport.getItcessRate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getItcessRate());
			rowData.add(advanceErrorReport.getItcessAmount() == null ? StringUtils.EMPTY
					: advanceErrorReport.getItcessAmount());

			rowData.add(StringUtils.isBlank(advanceErrorReport.getTdsSection()) ? StringUtils.EMPTY
					: advanceErrorReport.getTdsSection());
			rowData.add(advanceErrorReport.getTdsRate() == null ? StringUtils.EMPTY : advanceErrorReport.getTdsRate());
			rowData.add(
					advanceErrorReport.getTdsAmount() == null ? StringUtils.EMPTY : advanceErrorReport.getTdsAmount());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField1()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField1());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField2()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField2());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField3()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField3());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField4()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField4());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField5()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField5());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField6()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField6());

			worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
		}
	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 */
	public String getErrorReportMsg(String deductorMasterTan, String tenantId, String deductorPan) {
		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();
		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());
		return "Payment Remediation Report (Dated: " + date + ")\n Client Name: " + deductorData.getDeductorName()
				+ "\n";
	}

	/**
	 * This method for convert from advance csv file to xlsx file.
	 *
	 * @param csvFile
	 * @param deductorTan
	 * @return
	 * @throws Exception
	 */
	public File convertPaymentOtherReportToXlsx(File csvFile, String deductorTan) throws Exception {

		Reader reader = new FileReader(csvFile);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		CsvToBean<PaymentErrorReportCsvDto> csvToBean = new CsvToBeanBuilder(reader)
				.withType(PaymentErrorReportCsvDto.class).withIgnoreLeadingWhiteSpace(true).build();

		// Aspose method to convert
		Workbook workbook = generatePaymentOtherXlsxReport(csvToBean.parse(), deductorTan);

		// Write the output to the file
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxPaymentFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()) + "_Other", ".xlsx");

		FileUtils.writeByteArrayToFile(xlsxPaymentFile, baout.toByteArray());
		baout.close();
		return xlsxPaymentFile;
	}

	/**
	 * This method for set headers and colors.
	 * 
	 * @param advanceCsvDTOList
	 * @param deductorTan
	 * @return
	 * @throws Exception
	 */
	private Workbook generatePaymentOtherXlsxReport(List<PaymentErrorReportCsvDto> advanceCsvDTOList,
			String deductorTan) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		String[] headerNames = new String[] { "Source Identifier", "Source File Name", "Company Code",
				"Name of the Company Code", "Deductor PAN", "Deductor TAN", "Deductor GSTIN", "Deductee Code",
				"Name of the Deductee", "Non-Resident Deductee Indicator", "Deductee PAN", "Deductee TIN",
				"Deductee GSTIN", "Document Number", "Document Type", "Document Date", "Posting Date of the Document",
				"Entry Date of Payment Made", "Line item Number in the Accounting Document", "Payment Amount",
				"HSN/SAC", "SAC Description", "Service Description", "Payment to Vendor G/L Account Code",
				"Payment to Vendor G/L Account Description", "Withholding Tax Section",
				"Withholding Tax Rate/Withholding Tax Code", "Withholding Tax Amount", "PO Number",
				"Linking of Invoice with PO", "User Defined Field 1", "User Defined Field 2", "User Defined Field 3",
				"Section Predection" };

		worksheet.getCells().importArray(headerNames, 0, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForPaymentOtherReport(advanceCsvDTOList, worksheet);

		// Style for A1 to AG1 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(91, 155, 213));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A1:AH1");
		headerColorRange1.setStyle(style1);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A1";
		String lastHeaderCellName = "AH1";
		String firstDataCellName = "A2";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:AH1");

		return workbook;
	}

	/**
	 * 
	 * @param advanceCsvDTOList
	 * @param worksheet
	 * @throws Exception
	 */
	private void setExtractDataForPaymentOtherReport(List<PaymentErrorReportCsvDto> advanceCsvDTOList,
			Worksheet worksheet) throws Exception {

		int rowIndex = 1;
		for (PaymentErrorReportCsvDto advanceReport : advanceCsvDTOList) {
			ArrayList<Object> rowData = new ArrayList<>();
			rowData.add(StringUtils.isBlank(advanceReport.getSourceIdentifier()) ? StringUtils.EMPTY
					: advanceReport.getSourceIdentifier());
			rowData.add(StringUtils.isBlank(advanceReport.getSourceFileName()) ? StringUtils.EMPTY
					: advanceReport.getSourceFileName());
			rowData.add(StringUtils.isBlank(advanceReport.getCompanyCode()) ? StringUtils.EMPTY
					: advanceReport.getCompanyCode());
			rowData.add(StringUtils.isBlank(advanceReport.getNameOfTheCompanyCode()) ? StringUtils.EMPTY
					: advanceReport.getNameOfTheCompanyCode());
			rowData.add(StringUtils.isBlank(advanceReport.getCollectorPan()) ? StringUtils.EMPTY
					: advanceReport.getCollectorPan());
			rowData.add(StringUtils.isBlank(advanceReport.getCollectorTan()) ? StringUtils.EMPTY
					: advanceReport.getCollectorTan());
			rowData.add(StringUtils.isBlank(advanceReport.getCollectorGstin()) ? StringUtils.EMPTY
					: advanceReport.getCollectorGstin());
			rowData.add(StringUtils.isBlank(advanceReport.getCollecteeCode()) ? StringUtils.EMPTY
					: advanceReport.getCollecteeCode());
			rowData.add(StringUtils.isBlank(advanceReport.getNameOfTheDeductee()) ? StringUtils.EMPTY
					: advanceReport.getNameOfTheDeductee());
			rowData.add(StringUtils.isBlank(advanceReport.getNonresidentCollecteeIndicator()) ? StringUtils.EMPTY
					: advanceReport.getNonresidentCollecteeIndicator());
			rowData.add(StringUtils.isBlank(advanceReport.getCollecteePan()) ? StringUtils.EMPTY
					: advanceReport.getCollecteePan());
			rowData.add(StringUtils.isBlank(advanceReport.getCollecteeTin()) ? StringUtils.EMPTY
					: advanceReport.getCollecteeTin());
			rowData.add(StringUtils.isBlank(advanceReport.getCollecteeGstin()) ? StringUtils.EMPTY
					: advanceReport.getCollecteeGstin());
			rowData.add(StringUtils.isBlank(advanceReport.getDocumentNumber()) ? StringUtils.EMPTY
					: advanceReport.getDocumentNumber());
			rowData.add(StringUtils.isBlank(advanceReport.getDocumentType()) ? StringUtils.EMPTY
					: advanceReport.getDocumentType());
			rowData.add(StringUtils.isBlank(advanceReport.getDocumentDate()) ? StringUtils.EMPTY
					: advanceReport.getDocumentDate());
			rowData.add(StringUtils.isBlank(advanceReport.getPostingDateOfTheDocument()) ? StringUtils.EMPTY
					: advanceReport.getPostingDateOfTheDocument());
			rowData.add(StringUtils.isBlank(advanceReport.getEntryDateOfPaymentMade()) ? StringUtils.EMPTY
					: advanceReport.getEntryDateOfPaymentMade());
			rowData.add(StringUtils.isBlank(advanceReport.getLineItemNumberInAccountingDocument()) ? StringUtils.EMPTY
					: advanceReport.getLineItemNumberInAccountingDocument());
			rowData.add(StringUtils.isBlank(advanceReport.getPaymentAmount()) ? StringUtils.EMPTY
					: advanceReport.getPaymentAmount());
			rowData.add(StringUtils.isBlank(advanceReport.getHsnsac()) ? StringUtils.EMPTY : advanceReport.getHsnsac());
			rowData.add(StringUtils.isBlank(advanceReport.getSacDescription()) ? StringUtils.EMPTY
					: advanceReport.getSacDescription());
			rowData.add(StringUtils.isBlank(advanceReport.getServiceDescription()) ? StringUtils.EMPTY
					: advanceReport.getServiceDescription());
			rowData.add(StringUtils.isBlank(advanceReport.getPaymentToVendorGlAccountCode()) ? StringUtils.EMPTY
					: advanceReport.getPaymentToVendorGlAccountCode());
			rowData.add(StringUtils.isBlank(advanceReport.getPaymentToVendorGlAccountDescription()) ? StringUtils.EMPTY
					: advanceReport.getPaymentToVendorGlAccountDescription());
			rowData.add(StringUtils.isBlank(advanceReport.getWithholdingRate()) ? StringUtils.EMPTY
					: advanceReport.getWithholdingRate());
			rowData.add(StringUtils.isBlank(advanceReport.getWithholdingRate()) ? StringUtils.EMPTY
					: advanceReport.getWithholdingRate());
			rowData.add(StringUtils.isBlank(advanceReport.getWithholdingRate()) ? StringUtils.EMPTY
					: advanceReport.getWithholdingRate());
			rowData.add(
					StringUtils.isBlank(advanceReport.getPoNumber()) ? StringUtils.EMPTY : advanceReport.getPoNumber());
			rowData.add(StringUtils.isBlank(advanceReport.getLinkingOfInvoiceWithPo()) ? StringUtils.EMPTY
					: advanceReport.getLinkingOfInvoiceWithPo());
			rowData.add(StringUtils.isBlank(advanceReport.getUserDefinedField1()) ? StringUtils.EMPTY
					: advanceReport.getUserDefinedField1());
			rowData.add(StringUtils.isBlank(advanceReport.getUserDefinedField2()) ? StringUtils.EMPTY
					: advanceReport.getUserDefinedField2());
			rowData.add(StringUtils.isBlank(advanceReport.getUserDefinedField3()) ? StringUtils.EMPTY
					: advanceReport.getUserDefinedField3());
			rowData.add(StringUtils.isBlank(advanceReport.getTdsSectionPrediction()) ? StringUtils.EMPTY
					: advanceReport.getTdsSectionPrediction());
			worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
		}
	}

	/**
	 * 
	 * @param tans
	 * @param firstDate
	 * @param lastDate
	 * @return
	 */
	public String getTdsCalculationForProvision(String tans, String firstDate, String lastDate) {
		long trueValue = 0;// TODO NEED TO CHANGE FOR SQL
		// advanceRepository.getCountOfTdsCalculationsOfPaymentForCurrentMonth(tans,
		// firstDate, lastDate,
		// true);
		long falseValue = 0; //// TODO NEED TO CHANGE FOR SQL
		// advanceRepository.getCountOfTdsCalculationsOfPaymentForCurrentMonth(tans,
		// firstDate, lastDate,
		// false);
		String value = "";
		if (trueValue == 0 && falseValue == 0) {
			value = "N/A";
		} else if (trueValue > 0 && falseValue > 0) {
			value = "Pending";
		} else if (trueValue > 0 && falseValue == 0) {
			value = "Validated";
		}
		return value;
	}

	/**
	 * 
	 * @param residentType
	 * @param tan
	 * @param year
	 * @param month
	 * @param pagination
	 * @param deducteeName
	 * @return
	 */
	public CommonDTO<TcsPaymentDTO> getResidentAndNonresident(String residentType, String tan, int year, int month,
			Pagination pagination, String deducteeName) {
		BigInteger count = BigInteger.ZERO;
		CommonDTO<TcsPaymentDTO> advanceList = new CommonDTO<>();
		logger.info("tan : {} year : {} month : {} resident type : {} deductee name : {}", tan, year, month,
				residentType, deducteeName);
		List<TcsPaymentDTO> advanceData = null;
		if ("nocollecteefilter".equalsIgnoreCase(deducteeName)) {
			advanceData = paymentDAO.findAllResidentAndNonResident(year, month, tan, residentType, pagination);
			count = paymentDAO.findAllPaymentCount(year, month, tan, residentType);
		} else {
			advanceData = paymentDAO.findAllResidentAndNonResidentByDeductee(year, month, tan, residentType,
					deducteeName, pagination);
			count = paymentDAO.findAllPaymentNamesCount(year, month, tan, residentType, deducteeName);
		}
		PagedData<TcsPaymentDTO> pagedData = new PagedData<>(advanceData, advanceData.size(),
				pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		advanceList.setCount(count);
		advanceList.setResultsSet(pagedData);
		return advanceList;
	}

	/**
	 * 
	 * @param payment
	 * @return
	 */
	public TcsPaymentDTO lccRateCalculation(TcsPaymentDTO payment) {
		long documentDate = payment.getDocumentDate().getTime();

		List<TCSLccMaster> lccMasterResponse = paymentDAO.getLccRecordByTanPanSectionDocumentDate(
				payment.getCollectorTan(), payment.getCollecteePan(), payment.getFinalTcsSection(), documentDate);

		if (!lccMasterResponse.isEmpty()) {
			TCSLccMaster lccMaster = lccMasterResponse.get(0);

			BigDecimal remainingBalance = paymentDAO.getLccRemainigBalance(payment.getCollectorTan(),
					payment.getCollecteePan(), payment.getFinalTcsSection(), documentDate);

			if (remainingBalance.doubleValue() > payment.getAmount().doubleValue()) {

				payment.setFinalTcsRate(lccMaster.getLccRate().setScale(4, BigDecimal.ROUND_HALF_DOWN));

				TCSLccUtilization lccUtilization = new TCSLccUtilization();

				lccUtilization.setAssessmentYear(payment.getAssessmentYear());
				lccUtilization.setChallanMonth(payment.getChallanMonth());
				lccUtilization.setCollectorMasterTan(payment.getCollectorTan());
				lccUtilization.setLccMasterTotalAmount(lccMaster.getAmount() == null ? BigDecimal.ZERO
						: lccMaster.getAmount().setScale(2, RoundingMode.UP));
				lccUtilization.setRemainingAmount(
						remainingBalance.subtract(payment.getAmount()).setScale(2, RoundingMode.UP));
				lccUtilization.setLccMasterUtilizedAmount(payment.getAmount().setScale(2, RoundingMode.UP));
				lccUtilization.setActive(true);
				lccUtilization.setConsumedFrom(UploadTypes.PAYMENT.name());
				lccUtilization.setCreatedDate(new Date());
				lccUtilization.setChallanMonth(payment.getChallanMonth());
				lccUtilization.setLccMasterId(lccMaster.getId());
				lccUtilization.setLccMasterPan(lccMaster.getLccMasterPan());
				lccUtilization.setModifiedDate(new Date());
				lccUtilization.setLccMasterLccApplicableFrom(lccMaster.getApplicableFrom());
				lccUtilization.setLccMasterLccApplicableTo(lccMaster.getApplicableTo());
				lccUtilization.setLccMasterTotalAmount(lccMaster.getAmount() == null ? BigDecimal.ZERO
						: lccMaster.getAmount().setScale(2, RoundingMode.UP));
				lccUtilization.setInvoiceLineItemId(payment.getId());
				lccUtilization.setInvoiceProcessDate(payment.getDocumentDate());
				tcsLccUtilizationDAO.save(lccUtilization);
			}
		}
		return payment;
	}

	/**
	 * 
	 * @param clientSection
	 * @param clientRate
	 * @param clientAmount
	 * @param derivedSection
	 * @param derivedRate
	 * @param derivedAmount
	 * @return
	 */
	public String getMismatchCategory(String clientSection, BigDecimal clientRate, BigDecimal clientAmount,
			String derivedSection, BigDecimal derivedRate, BigDecimal derivedAmount) {
		String mismatchCategory = null;

		if ("NAD".equalsIgnoreCase(derivedSection)) {
			mismatchCategory = "NAD";
		} else if (!clientSection.equals(derivedSection) && clientRate.doubleValue() != derivedRate.doubleValue()
				&& clientAmount.doubleValue() != derivedAmount.doubleValue()) {
			mismatchCategory = "SMM-RMM-AMM";
		} else if (clientSection.equals(derivedSection) && clientRate.doubleValue() != derivedRate.doubleValue()
				&& clientAmount.doubleValue() != derivedAmount.doubleValue()) {
			mismatchCategory = "SM-RMM-AMM";
		} else if (!clientSection.equals(derivedSection) && clientRate.doubleValue() == derivedRate.doubleValue()
				&& clientAmount.doubleValue() != derivedAmount.doubleValue()) {
			mismatchCategory = "SMM-RM-AMM";
		} else if (!clientSection.equals(derivedSection) && clientRate.doubleValue() != derivedRate.doubleValue()) {
			mismatchCategory = "SMM-RMM-AM";
		} else if (clientSection.equals(derivedSection) && clientRate.doubleValue() == derivedRate.doubleValue()
				&& clientAmount.doubleValue() != derivedAmount.doubleValue()) {
			mismatchCategory = "SM-RM-AMM";
		} else if (clientRate.doubleValue() != derivedRate.doubleValue()) {
			mismatchCategory = "SM-RMM-AM";
		} else if (!clientSection.equals(derivedSection)) {
			mismatchCategory = "SMM-RM-AM";
		}

		return mismatchCategory;
	}

	/**
	 * 
	 * @param cancelList
	 * @return
	 */
	public MultipartFile generateCancelledPaymentExcell(List<Integer> paymentIds, String tan, String collectorPan) {

		MultipartFile multipartFile = null;
		String[] advanceHeadersFile = new String[] { "SourceIdentifier", "SourceFileName", "CollectorCode",
				"CollectorPAN", "CollectorTAN", "CollecteeCode", "CollecteePAN", "DocumentType", "DocumentNumber",
				"DocumentDate", "ClearingDocumentNumber", "ClearingDate", "InvoiceDocumentNumber", "InvoiceDate",
				"LineNumber", "PaymentDesc", "GLAccountCode", "GLDesc", "Amount", "TCSSection", "TCSRate", "TCSAmount",
				"SurchargeRate", "SurchargeAmount", "ITCESSRate", "ITCESSAmount", "TDSSection", "TDSRate", "TDSAmount",
				"UserDefinedField1", "UserDefinedField2", "UserDefinedField3", "UserDefinedField4", "UserDefinedField5",
				"UserDefinedField6" };
		try {
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			worksheet.setName("Imported Payments");
			worksheet.autoFitColumns();
			worksheet.autoFitRows();
			worksheet.getCells().importArray(advanceHeadersFile, 0, 0, false);
			List<TcsPaymentDTO> cancelList = paymentDAO.getPaymentList(paymentIds, tan, collectorPan);
			setPaymentHeaders(cancelList, worksheet);

			worksheet.autoFitColumns();

			// Style for A1 to AI1 headers
			Style style5 = workbook.createStyle();
			style5.setForegroundColor(Color.fromArgb(91, 155, 213));
			style5.setPattern(BackgroundType.SOLID);
			style5.getFont().setBold(true);
			style5.setHorizontalAlignment(TextAlignmentType.CENTER);
			Range headerColorRange1 = worksheet.getCells().createRange("A1:AI1");
			headerColorRange1.setStyle(style5);

			// Creating AutoFilter by giving the cells range
			AutoFilter autoFilter = worksheet.getAutoFilter();
			autoFilter.setRange("A1:AI1");

			File file = new File("Cancelled_Payments_" + UUID.randomUUID() + ".xlsx");
			OutputStream out = new FileOutputStream(file);
			workbook.save(out, SaveFormat.XLSX);

			InputStream inputstream = new FileInputStream(file);
			multipartFile = new MockMultipartFile("file", file.getName(), "application/vnd.ms-excel",
					IOUtils.toByteArray(inputstream));

		} catch (Exception exception) {
			logger.info("Exception occured while generating excell report for cancelled invoices");
		}
		return multipartFile;
	}

	/**
	 * 
	 * @param cancelList
	 * @param worksheet
	 * @throws Exception
	 */
	public void setPaymentHeaders(List<TcsPaymentDTO> cancelList, Worksheet worksheet) throws Exception {
		int rowIndex = 1;
		for (TcsPaymentDTO cancelledPayment : cancelList) {
			List<Object> rowData = new ArrayList<>();
			// Source Identifier
			rowData.add(StringUtils.isBlank(cancelledPayment.getSourceIdentifier()) ? StringUtils.EMPTY
					: cancelledPayment.getSourceIdentifier());
			// Source File Name
			rowData.add(StringUtils.isBlank(cancelledPayment.getSourceFileName()) ? StringUtils.EMPTY
					: cancelledPayment.getSourceFileName());
			// Name of the Company Code
			rowData.add(StringUtils.isBlank(cancelledPayment.getCollectorCode()) ? StringUtils.EMPTY
					: cancelledPayment.getCollectorCode());
			// Deductor PAN
			rowData.add(StringUtils.isBlank(cancelledPayment.getCollectorPan()) ? StringUtils.EMPTY
					: cancelledPayment.getCollectorPan());
			// Deductor TAN
			rowData.add(StringUtils.isBlank(cancelledPayment.getCollectorTan()) ? StringUtils.EMPTY
					: cancelledPayment.getCollectorTan());
			// Company Code
			rowData.add(StringUtils.isBlank(cancelledPayment.getCollecteeCode()) ? StringUtils.EMPTY
					: cancelledPayment.getCollecteeCode());
			// Deductee PAN
			rowData.add(StringUtils.isBlank(cancelledPayment.getCollecteePan()) ? StringUtils.EMPTY
					: cancelledPayment.getCollecteePan());

			rowData.add(StringUtils.isBlank(cancelledPayment.getDocumentType()) ? StringUtils.EMPTY
					: cancelledPayment.getDocumentType());
			// Document Number
			rowData.add(StringUtils.isBlank(cancelledPayment.getDocumentNumber()) ? StringUtils.EMPTY
					: cancelledPayment.getDocumentNumber());
			// Document Date
			rowData.add(cancelledPayment.getDocumentDate() == null ? StringUtils.EMPTY
					: cancelledPayment.getDocumentDate());
			// Clearing Document Number
			rowData.add(cancelledPayment.getClearingDocumentNumber() == null ? StringUtils.EMPTY
					: cancelledPayment.getClearingDocumentNumber());
			// ClearingDate
			rowData.add(cancelledPayment.getClearingDate() == null ? StringUtils.EMPTY
					: cancelledPayment.getClearingDate());
			// Invoice Document Number
			rowData.add(cancelledPayment.getInvoiceDocumentNumber() == null ? StringUtils.EMPTY
					: cancelledPayment.getInvoiceDocumentNumber());
			// Invoice Date
			rowData.add(
					cancelledPayment.getInvoiceDate() == null ? StringUtils.EMPTY : cancelledPayment.getInvoiceDate());
			// "Line item Number in the Accounting Document
			rowData.add(
					cancelledPayment.getLineNumber() == null ? StringUtils.EMPTY : cancelledPayment.getLineNumber());
			// Payment Desc
			rowData.add(StringUtils.isBlank(cancelledPayment.getPaymentDesc()) ? StringUtils.EMPTY
					: cancelledPayment.getPaymentDesc());
			// Gl AccountCode
			rowData.add(StringUtils.isBlank(cancelledPayment.getGlAccountCode()) ? StringUtils.EMPTY
					: cancelledPayment.getGlAccountCode());
			// Payment to Vendor G/L Account Description
			rowData.add(StringUtils.isBlank(cancelledPayment.getGlDesc()) ? StringUtils.EMPTY
					: cancelledPayment.getGlDesc());
			// Payment Amount
			rowData.add(cancelledPayment.getAmount() == null ? StringUtils.EMPTY : cancelledPayment.getAmount());
			// Tcs Section
			rowData.add(StringUtils.isBlank(cancelledPayment.getTcsSection()) ? StringUtils.EMPTY
					: cancelledPayment.getTcsSection());
			// Tcs Rate
			rowData.add(cancelledPayment.getTcsRate() == null ? StringUtils.EMPTY : cancelledPayment.getTcsRate());
			// Tcs Amount
			rowData.add(cancelledPayment.getTcsAmount() == null ? StringUtils.EMPTY : cancelledPayment.getTcsAmount());
			// Surcharge Rate
			rowData.add(cancelledPayment.getSurchargeRate() == null ? StringUtils.EMPTY
					: cancelledPayment.getSurchargeRate());
			// Surcharge Amount
			rowData.add(cancelledPayment.getSurchargeAmount() == null ? StringUtils.EMPTY
					: cancelledPayment.getSurchargeAmount());
			// itcess rate
			rowData.add(
					cancelledPayment.getItcessRate() == null ? StringUtils.EMPTY : cancelledPayment.getItcessRate());
			// itcess amount
			rowData.add(cancelledPayment.getItcessAmount() == null ? StringUtils.EMPTY
					: cancelledPayment.getItcessAmount());

			// tds Section
			rowData.add(StringUtils.isBlank(cancelledPayment.getTdsSection()) ? StringUtils.EMPTY
					: cancelledPayment.getTdsSection());
			// tds Rate
			rowData.add(cancelledPayment.getTdsRate() == null ? StringUtils.EMPTY : cancelledPayment.getTdsRate());
			// tds Amount
			rowData.add(cancelledPayment.getTdsAmount() == null ? StringUtils.EMPTY : cancelledPayment.getTdsAmount());

			// User Defined Field 1
			rowData.add(StringUtils.isBlank(cancelledPayment.getUserDefinedField1()) ? StringUtils.EMPTY
					: cancelledPayment.getUserDefinedField1());
			// User Defined Field 2
			rowData.add(StringUtils.isBlank(cancelledPayment.getUserDefinedField2()) ? StringUtils.EMPTY
					: cancelledPayment.getUserDefinedField2());
			// User Defined Field 3
			rowData.add(StringUtils.isBlank(cancelledPayment.getUserDefinedField3()) ? StringUtils.EMPTY
					: cancelledPayment.getUserDefinedField3());
			// User Defined Field 4
			rowData.add(StringUtils.isBlank(cancelledPayment.getUserDefinedField4()) ? StringUtils.EMPTY
					: cancelledPayment.getUserDefinedField4());
			// User Defined Field 5
			rowData.add(StringUtils.isBlank(cancelledPayment.getUserDefinedField5()) ? StringUtils.EMPTY
					: cancelledPayment.getUserDefinedField5());
			// User Defined Field 6
			rowData.add(StringUtils.isBlank(cancelledPayment.getUserDefinedField6()) ? StringUtils.EMPTY
					: cancelledPayment.getUserDefinedField6());

			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);

		}
	}

	public Double closest(Double of, List<Double> in) {
		Double min = Double.MAX_VALUE;
		Double closest = of;
		for (Double v : in) {
			final Double diff = Math.abs(v - of);
			if (diff < min) {
				min = diff;
				closest = v;
			}
		}
		return closest;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param collectorTan
	 * @param tenantId
	 * @param pan
	 * @param challanMonth
	 * @param userName
	 * @return
	 */
	public TCSBatchUpload advanceAdjustments(Integer assessmentYear, String collectorTan, String tenantId, String pan,
			Integer challanMonth, String userName) {
		MultiTenantContext.setTenantId(tenantId);
		int count = 0;

		String fileName = "ADVANCE_ADJUSTMENT_" + assessmentYear + "_" + challanMonth;
		count = tcsBatchUploadDAO.getCountByFileName(assessmentYear, collectorTan, fileName);
		if (count != 0) {
			logger.info("record count is {}", count);
			return null;
		}

		TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
		tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
		tcsBatchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
		tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
		tcsBatchUpload.setFileName("ADVANCE_ADJUSTMENT_" + assessmentYear + "_" + challanMonth);
		tcsBatchUpload.setCreatedBy(userName);
		tcsBatchUpload.setModifiedBy(userName);
		tcsBatchUpload.setRowsCount(0L);
		tcsBatchUpload.setSuccessCount(0L);
		tcsBatchUpload.setFailedCount(0L);
		tcsBatchUpload.setRowsCount(0L);
		tcsBatchUpload.setProcessed(0);
		tcsBatchUpload.setMismatchCount(0L);
		tcsBatchUpload.setStatus("Processing");
		tcsBatchUpload = paymentBatchUpload(tcsBatchUpload, collectorTan, assessmentYear, challanMonth, userName,
				tenantId);

		paymentDAO.USPPaymentUtilization(assessmentYear, challanMonth, collectorTan, pan);

		tcsBatchUpload = paymentBatchUpload(tcsBatchUpload, collectorTan, assessmentYear, challanMonth, userName,
				tenantId);

		return tcsBatchUpload;
	}

	/**
	 * 
	 * @param tcsBatchUpload
	 * @param collectorTan
	 * @param assessmentYear
	 * @param challanMonth
	 * @param userName
	 * @param tenantId
	 * @return
	 */
	private TCSBatchUpload paymentBatchUpload(TCSBatchUpload tcsBatchUpload, String collectorTan,
			Integer assessmentYear, Integer challanMonth, String userName, String tenantId) {
		logger.info("batch", tcsBatchUpload);
		MultiTenantContext.setTenantId(tenantId);
		tcsBatchUpload.setAssessmentMonth(challanMonth);
		tcsBatchUpload.setAssessmentYear(assessmentYear);
		tcsBatchUpload.setCollectorMasterTan(collectorTan);
		tcsBatchUpload.setUploadType(UploadTypes.BATCHPROCESS_LIABILITYRECON.name());
		tcsBatchUpload.setCreatedBy(userName);
		tcsBatchUpload.setActive(true);
		if (tcsBatchUpload.getId() != null) {
			tcsBatchUpload.setId(tcsBatchUpload.getId());
			tcsBatchUpload.setStatus("Processed");
			tcsBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			tcsBatchUploadDAO.update(tcsBatchUpload);
		} else {
			tcsBatchUploadDAO.save(tcsBatchUpload);
		}
		return tcsBatchUpload;
	}

	/**
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	public void decompressGzip(Path source, Path target) throws IOException {
		try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(source.toFile()));
				FileOutputStream fos = new FileOutputStream(target.toFile())) {
			// copy GZIPInputStream to FileOutputStream
			byte[] buffer = new byte[1024];
			int len;
			while ((len = gis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
		}
	}

	/**
	 * 
	 * @param rowCount
	 * @param csv
	 * @param deductorTan
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 * @throws IOException
	 */
	public ByteArrayInputStream generatePaymentExcel(int rowCount, CsvContainer csv, String deductorTan,
			String tenantId, String deductorPan) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();

		try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
			SXSSFSheet sheet = wb.createSheet("Mismatch_Remediation_Report");
			sheet.setRandomAccessWindowSize(1000);
			sheet.setDisplayGridlines(false);
			sheet.setColumnHidden(36, true);
			sheet.setColumnHidden(37, true);
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);
			sheet.protectSheet("password");
			DataValidation dataValidation = null;
			DataValidationConstraint constraint = null;
			DataValidationHelper validationHelper = null;

			SXSSFRow row0 = sheet.createRow(0);
			XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();

			style.setWrapText(true);
			Font fonts = wb.createFont();
			fonts.setBold(true);
			style.setFont(fonts);

			style.setAlignment(HorizontalAlignment.LEFT);
			style.setVerticalAlignment(VerticalAlignment.CENTER);
			style.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));

			sheet.createRow(1);
			XSSFCellStyle style01 = (XSSFCellStyle) wb.createCellStyle();
			style01.setBorderLeft(BorderStyle.MEDIUM);
			style01.setBorderTop(BorderStyle.MEDIUM);
			style01.setBorderBottom(BorderStyle.MEDIUM);
			style01.setBorderRight(BorderStyle.MEDIUM);
			style01.setAlignment(HorizontalAlignment.LEFT);
			style01.setVerticalAlignment(VerticalAlignment.CENTER);
			style01.setFillForegroundColor(new XSSFColor(new java.awt.Color(102, 194, 255), defaultIndexedColorMap));
			Font font01 = wb.createFont();
			font01.setBold(true);
			style01.setFont(font01);

			sheet.createRow(2);
			XSSFCellStyle style02 = (XSSFCellStyle) wb.createCellStyle();
			style02.setBorderLeft(BorderStyle.MEDIUM);
			style02.setBorderTop(BorderStyle.MEDIUM);
			style02.setBorderBottom(BorderStyle.MEDIUM);
			style02.setBorderRight(BorderStyle.MEDIUM);
			style02.setAlignment(HorizontalAlignment.LEFT);
			style02.setVerticalAlignment(VerticalAlignment.CENTER);
			style02.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));
			Font font02 = wb.createFont();
			font02.setBold(true);
			style02.setFont(font02);

			row0.setHeightInPoints((3 * sheet.getDefaultRowHeightInPoints()));
			String msg = getErrorReportMsg(deductorTan, tenantId, deductorPan);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

			XSSFCellStyle styleForStaticValues = (XSSFCellStyle) wb.createCellStyle();
			styleForStaticValues.setVerticalAlignment(VerticalAlignment.CENTER);
			styleForStaticValues.setAlignment(HorizontalAlignment.LEFT);
			Font fonts2 = wb.createFont();
			fonts2.setBold(false);
			styleForStaticValues.setFont(fonts2);

			String value1 = "NOTE: If you select 'Accept' under 'Action' (Column AF), then providing information under column AH to AJ is not mandatory.";
			// String value2 = "NOTE: If you select 'Reject' under 'Action' (Column AF),
			// then providing information under 'Reason' (Column AG) is mandatory and column
			// AH to AJ is not mandatory.";
			String value2 = "NOTE: If you select 'Modify' under 'Action' (Column AF), then providing information under Column AG to AJ is mandatory.";
			String value3 = "NOTE: If you select 'Cancel' under 'Action' (Column AF), then the specified line item forming part of document number will be disregarded for any TCS compliance purpose";

			// value1
			SXSSFRow value1Row04 = sheet.createRow(1);
			value1Row04.createCell(0).setCellValue(value1);
			value1Row04.getCell(0).setCellStyle(styleForStaticValues);
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));// 0, 2, 4, 19

			// value2
			SXSSFRow value2row04 = sheet.createRow(2);
			value2row04.createCell(0).setCellValue(value2);
			value2row04.getCell(0).setCellStyle(styleForStaticValues);
			sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 3));

			// value3
			SXSSFRow value3row04 = sheet.createRow(3);
			value3row04.createCell(0).setCellValue(value3);
			value3row04.getCell(0).setCellStyle(styleForStaticValues);
			value3row04.createCell(34)
					.setCellValue("Please mention as .075 only % symbol is not allowed, e.g - .075% (not allowed)");
			value3row04.getCell(34).setCellStyle(styleForStaticValues);
			sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 3));

			// value4
			SXSSFRow row03 = sheet.createRow(4);
			/*
			 * row03.createCell(0).setCellValue(value4);
			 * row03.getCell(0).setCellStyle(styleForStaticValues);
			 * sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 3));
			 */

			// header colors
			XSSFCellStyle style0 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style1 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style2 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style3 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style003 = (XSSFCellStyle) wb.createCellStyle();

			style0.setAlignment(HorizontalAlignment.CENTER);
			style0.setVerticalAlignment(VerticalAlignment.CENTER);
			style0.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));

			style1.setAlignment(HorizontalAlignment.CENTER);
			style1.setVerticalAlignment(VerticalAlignment.CENTER);
			style1.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));

			style2.setAlignment(HorizontalAlignment.CENTER);
			style2.setVerticalAlignment(VerticalAlignment.CENTER);
			style2.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));

			style3.setAlignment(HorizontalAlignment.CENTER);
			style3.setVerticalAlignment(VerticalAlignment.CENTER);
			style3.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));

			style003.setAlignment(HorizontalAlignment.CENTER);
			style003.setVerticalAlignment(VerticalAlignment.CENTER);
			style003.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));

			Font font = wb.createFont();
			font.setBold(true);
			font.setFontName("Arial");
			style0.setFont(font);
			style1.setFont(font);
			style2.setFont(font);
			style3.setFont(font);
			style003.setFont(font);
			// Green colour
			setCellColorAndBoarder(defaultIndexedColorMap, style0, 169, 209, 142);
			// Light orange-color
			setCellColorAndBoarder(defaultIndexedColorMap, style1, 251, 229, 214);
			// Light yellow color
			setCellColorAndBoarder(defaultIndexedColorMap, style2, 255, 242, 204);
			// Light blue-color
			setCellColorAndBoarder(defaultIndexedColorMap, style3, 222, 235, 247);
			// yellow
			setCellColorAndBoarder(defaultIndexedColorMap, style003, 255, 192, 0);
			row03.createCell(4).setCellValue("Error Type");
			row03.getCell(4).setCellStyle(style0);
			CellRangeAddress cellRangeAddressErrorType = new CellRangeAddress(4, 4, 4, 6);
			sheet.addMergedRegion(cellRangeAddressErrorType);

			row03.createCell(19).setCellValue("Client provided data");
			row03.getCell(19).setCellStyle(style1);
			CellRangeAddress cellRangeAddress1 = new CellRangeAddress(4, 4, 19, 21);
			sheet.addMergedRegion(cellRangeAddress1);

			row03.createCell(22).setCellValue("Tool Derived Sections/Rate");
			row03.getCell(22).setCellStyle(style2);
			CellRangeAddress cellRangeAddress2 = new CellRangeAddress(4, 4, 22, 24);
			sheet.addMergedRegion(cellRangeAddress2);

			row03.createCell(25).setCellValue("Mismatch Category");
			row03.getCell(25).setCellStyle(style3);
			CellRangeAddress cellRangeAddress3 = new CellRangeAddress(4, 4, 25, 28);
			sheet.addMergedRegion(cellRangeAddress3);

			row03.createCell(31).setCellValue("Client Response");
			row03.getCell(31).setCellStyle(style003);
			CellRangeAddress cellRangeAddress4 = new CellRangeAddress(4, 4, 31, 35);
			sheet.addMergedRegion(cellRangeAddress4);

			SXSSFRow row1 = sheet.createRow(5);
			sheet.setDefaultColumnWidth(25);
			// setting column width for error message column
			sheet.setColumnWidth(3, sheet.getColumnWidth(3) * 3);

			// Create a cell
			row1.createCell(0).setCellValue("Sequence Number");
			row1.getCell(0).setCellStyle(style0); // A

			row1.createCell(1).setCellValue("Collector TAN");
			row1.getCell(1).setCellStyle(style0); // B

			row1.createCell(2).setCellValue("Collector GSTIN");
			row1.getCell(2).setCellStyle(style0); // C

			row1.createCell(3).setCellValue("ERROR MESSAGE");
			row1.getCell(3).setCellStyle(style0); // D

			// error types
			row1.createCell(4).setCellValue("Section");
			row1.getCell(4).setCellStyle(style0);// E

			row1.createCell(5).setCellValue("Rate");
			row1.getCell(5).setCellStyle(style0);// F

			row1.createCell(6).setCellValue("Amount");
			row1.getCell(6).setCellStyle(style0); // G

			row1.createCell(7).setCellValue("Collectee Code");
			row1.getCell(7).setCellStyle(style0); // H

			row1.createCell(8).setCellValue("Collectee PAN");
			row1.getCell(8).setCellStyle(style0); // I

			row1.createCell(9).setCellValue("Collectee Name");
			row1.getCell(9).setCellStyle(style0); // J

			row1.createCell(10).setCellValue("Document Number");
			row1.getCell(10).setCellStyle(style0); // K

			row1.createCell(11).setCellValue("Accunting Document Number");
			row1.getCell(11).setCellStyle(style0); // L

			row1.createCell(12).setCellValue("Line Number");
			row1.getCell(12).setCellStyle(style0); // M

			row1.createCell(13).setCellValue("Document Posting Date");
			row1.getCell(13).setCellStyle(style0); // N

			row1.createCell(14).setCellValue("Document Type");
			row1.getCell(14).setCellStyle(style0); // O

			row1.createCell(15).setCellValue("Invoice Description");
			row1.getCell(15).setCellStyle(style0); // P

			row1.createCell(16).setCellValue("SO Description");
			row1.getCell(16).setCellStyle(style0); // Q

			row1.createCell(17).setCellValue("GL Description");
			row1.getCell(17).setCellStyle(style0); // R

			row1.createCell(18).setCellValue("HSN Code/SAC Code");
			row1.getCell(18).setCellStyle(style0); // S

			row1.createCell(19).setCellValue("Client TCS Section");
			row1.getCell(19).setCellStyle(style1); // T

			row1.createCell(20).setCellValue("Client TCS Rate");
			row1.getCell(20).setCellStyle(style1); // U

			row1.createCell(21).setCellValue("Client TCS Amount");
			row1.getCell(21).setCellStyle(style1); // V

			row1.createCell(22).setCellValue("Derived TCS Section");
			row1.getCell(22).setCellStyle(style2); // W

			row1.createCell(23).setCellValue("Derived TCS Rate");
			row1.getCell(23).setCellStyle(style2); // X

			row1.createCell(24).setCellValue("Derived TCS Amount");
			row1.getCell(24).setCellStyle(style2); // Y

			row1.createCell(25).setCellValue("Section");
			row1.getCell(25).setCellStyle(style3); // Z

			row1.createCell(26).setCellValue("Rate");
			row1.getCell(26).setCellStyle(style3); // AA

			row1.createCell(27).setCellValue("Amount");
			row1.getCell(27).setCellStyle(style3); // AB

			row1.createCell(28).setCellValue("Collection Type");
			row1.getCell(28).setCellStyle(style3); // AC

			row1.createCell(29).setCellValue("Mismatch Interpretation");
			row1.getCell(29).setCellStyle(style0); // AD

			row1.createCell(30).setCellValue("Confidence");
			row1.getCell(30).setCellStyle(style0); // AE

			row1.createCell(31).setCellValue("Action");
			row1.getCell(31).setCellStyle(style003); // AF

			row1.createCell(32).setCellValue("Reason");
			row1.getCell(32).setCellStyle(style003); // AG

			row1.createCell(33).setCellValue("Final TCS Section");
			row1.getCell(33).setCellStyle(style003); // AH

			row1.createCell(34).setCellValue("Final TCS Rate");
			row1.getCell(34).setCellStyle(style003); // AI

			row1.createCell(35).setCellValue("Final TCS Amount");
			row1.getCell(35).setCellStyle(style003); // AJ

			row1.createCell(36).setCellValue("Payment Id");
			row1.getCell(36).setCellStyle(style003); // AJ

			row1.createCell(38).setCellValue("Payment Amount");
			row1.getCell(38).setCellStyle(style3); // AM

			row1.createCell(39).setCellValue("Cess TCS Rate");
			row1.getCell(39).setCellStyle(style0); // AN

			row1.createCell(40).setCellValue("Surcharge TCS Rate");
			row1.getCell(40).setCellStyle(style0); // AO

			row1.createCell(41).setCellValue("Collectee Type");
			row1.getCell(41).setCellStyle(style0); // AQ

			row1.createCell(42).setCellValue("Consumed Amount");
			row1.getCell(42).setCellStyle(style0); // AQ

			// Auto Filter Option
			sheet.setAutoFilter(new CellRangeAddress(5, 42, 0, 42));
			// sheet.createFreezePane(0, 1);

			// sheet.createFreezePane(colSplit, rowSplit, leftmostColumn, topRow)-this
			// method is used for scrolling rows and column vertically and horizontally
			// sheet.createFreezePane(4, 5, 5, 5);

			long size = rowCount;

			validationHelper = sheet.getDataValidationHelper();
			CellRangeAddressList addressList = new CellRangeAddressList(6, (int) size + 6, 31, 31);
			constraint = validationHelper.createExplicitListConstraint(new String[] { "Accept", "Modify", "Cancel" });
			dataValidation = validationHelper.createValidation(constraint, addressList);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.setShowPromptBox(true);
			sheet.addValidationData(dataValidation);

			validationHelper = sheet.getDataValidationHelper();
			CellRangeAddressList addressList2 = new CellRangeAddressList(6, (int) size + 6, 33, 33);
			// Feign Client to get list of sections from nature of payment
			ResponseEntity<ApiStatus<List<NatureOfPaymentMasterDTO>>> natureOfPayament = mastersClient
					.getTcsNatureOfPayment();

			List<NatureOfPaymentMasterDTO> natureOfPayamentList = natureOfPayament.getBody().getData();
			List<String> sectionArray = new ArrayList<>();
			for (NatureOfPaymentMasterDTO nopMaster : natureOfPayamentList) {
				sectionArray.add(nopMaster.getSection());
			}

			constraint = validationHelper
					.createExplicitListConstraint(sectionArray.toArray(new String[sectionArray.size()]));
			dataValidation = validationHelper.createValidation(constraint, addressList2);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.setShowPromptBox(true);
			sheet.addValidationData(dataValidation);

			XSSFCellStyle style5 = (XSSFCellStyle) wb.createCellStyle();
			Font font05 = wb.createFont();
			style5.setFont(font05);
			// style5.setLocked(false);
			style5.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));
			style5.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style5.setBorderLeft(BorderStyle.THIN);
			style5.setBorderTop(BorderStyle.THIN);
			style5.setBorderBottom(BorderStyle.THIN);
			style5.setBorderRight(BorderStyle.THIN);
			style5.setVerticalAlignment(VerticalAlignment.CENTER);
			style5.setAlignment(HorizontalAlignment.LEFT);

			XSSFCellStyle style4 = (XSSFCellStyle) wb.createCellStyle();
			Font font4 = wb.createFont();
			style4.setFont(font4);
			style4.setLocked(false);
			style4.setBorderLeft(BorderStyle.THIN);
			style4.setBorderTop(BorderStyle.THIN);
			style4.setBorderBottom(BorderStyle.THIN);
			style4.setBorderRight(BorderStyle.THIN);
			int rowindex = 6;
			Integer sequenceNumber = 0;
			for (CsvRow row : csv.getRows()) {
				sequenceNumber = sequenceNumber + 1;
				SXSSFRow row2 = sheet.createRow(rowindex++);
				String errorMessage = "";
				// Error message
				String mismatchCategory = row.getField("mismatch_category");
				if (mismatchCategory.equals("SM-RMM")) {
					errorMessage = "Section Match - Rate Mismatch - TCS amount NA";
				} else if (mismatchCategory.equals("SMM-RM")) {
					errorMessage = "Section Mismatch - Rate Match - TCS amount NA";
				} else if (mismatchCategory.equals("SMM-RMM")) {
					errorMessage = "Section Mismatch - Rate Mismatch - TCS amount NA";
				} else {
					errorMessage = "Section - not able to determine";
				}

				row2.createCell(0).setCellValue(sequenceNumber);
				row2.getCell(0).setCellStyle(style5);// A

				String collectorTan = row.getField("collector_tan");
				row2.createCell(1)
						.setCellValue(StringUtils.isBlank(collectorTan) == true ? StringUtils.EMPTY : collectorTan);
				row2.getCell(1).setCellStyle(style5); // B

				String collectorGSTIN = row.getField("collector_gstin");
				row2.createCell(2)
						.setCellValue(StringUtils.isBlank(collectorGSTIN) == true ? StringUtils.EMPTY : collectorGSTIN);
				row2.getCell(2).setCellStyle(style5);// C

				row2.createCell(3).setCellValue(errorMessage);
				row2.getCell(3).setCellStyle(style5); // D

				String[] split = errorMessage.split("-");
				String sectionErrorType = StringUtils.EMPTY;
				String rateErrorType = StringUtils.EMPTY;
				String tcsAmountErrorType = StringUtils.EMPTY;
				if (split.length > 2) {
					sectionErrorType = split[0].trim().split(" ")[1];
					rateErrorType = split[1].trim().split(" ")[1];
					tcsAmountErrorType = split[2].trim().split(" ")[2];
				}
				row2.createCell(4).setCellValue(sectionErrorType);
				row2.getCell(4).setCellStyle(style5); // E
				row2.createCell(5).setCellValue(rateErrorType);
				row2.getCell(5).setCellStyle(style5); // F
				row2.createCell(6).setCellValue(tcsAmountErrorType);
				row2.getCell(6).setCellStyle(style5); // G

				String collecteeCode = row.getField("collectee_code");
				row2.createCell(7)
						.setCellValue(StringUtils.isBlank(collecteeCode) == true ? StringUtils.EMPTY : collecteeCode);
				row2.getCell(7).setCellStyle(style5); // H

				String collecteePan = row.getField("collectee_pan");
				row2.createCell(8)
						.setCellValue(StringUtils.isBlank(collecteePan) == true ? StringUtils.EMPTY : collecteePan);
				row2.getCell(8).setCellStyle(style5); // I

				String collecteeName = row.getField("collectee_name");

				row2.createCell(9)
						.setCellValue(StringUtils.isBlank(collecteeName) == true ? StringUtils.EMPTY : collecteeName);
				row2.getCell(9).setCellStyle(style5); // J

				createCell(style5, row2, 10, row.getField("document_number"));

				createCell(style5, row2, 11, row.getField("accounting_document_number"));
				createCell(style5, row2, 12, row.getField("line_number"));
				createCell(style5, row2, 13, row.getField("posting_date"));

				createCell(style5, row2, 14, row.getField("document_type"));

				createCell(style5, row2, 15, row.getField("invoice_desc"));

				createCell(style5, row2, 16, row.getField("so_desc"));
				createCell(style5, row2, 17, row.getField("gl_desc"));
				createCell(style5, row2, 18, row.getField("hsn_or_sac"));

				createCell(style5, row2, 19, row.getField("actual_tcs_section"));
				createCell(style5, row2, 20, row.getField("actual_tcs_rate"));
				createCell(style5, row2, 21, row.getField("actual_tcs_amount"));

				createCell(style5, row2, 22, row.getField("derived_tcs_section"));
				createCell(style5, row2, 23, row.getField("derived_tcs_rate"));
				createCell(style5, row2, 24, row.getField("derived_tcs_amount"));

				createCell(style5, row2, 25, row.getField("actual_tcs_section"));
				createCell(style5, row2, 26, row.getField("actual_tcs_rate"));
				createCell(style5, row2, 27, row.getField("actual_tcs_amount"));

				createCell(style5, row2, 28, row.getField("document_type"));
				createCell(style5, row2, 29, row.getField("mismatch_interpretation"));
				createCell(style5, row2, 30, row.getField("confidence"));

				createCell(style4, row2, 31, "");
				createCell(style4, row2, 32, "");
				createCell(style4, row2, 33, "");
				createCell(style4, row2, 34, "");
				createCell(style4, row2, 35, "");
				createCell(style5, row2, 36, row.getField("id"));

				createCell(style5, row2, 37, row.getField("assessment_year"));
				createCell(style5, row2, 38, row.getField("amount"));
				createCell(style5, row2, 39, row.getField("itcess_rate"));
				createCell(style5, row2, 40, row.getField("surcharge_rate"));
				createCell(style5, row2, 41, row.getField("collectee_type"));
				createCell(style5, row2, 42, row.getField("consumed_amount"));
			}
			wb.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		}
	}

	/**
	 * 
	 * @param style
	 * @param row
	 * @param cellNumber
	 * @param value
	 */
	private void createCell(XSSFCellStyle style, SXSSFRow row, int cellNumber, String value) {
		row.createCell(cellNumber).setCellValue(StringUtils.isBlank(value) == true ? StringUtils.EMPTY : value);
		row.getCell(cellNumber).setCellStyle(style); // K
	}

	/**
	 * 
	 * @param defaultIndexedColorMap
	 * @param style
	 * @param r
	 * @param g
	 * @param b
	 */
	private void setCellColorAndBoarder(DefaultIndexedColorMap defaultIndexedColorMap, XSSFCellStyle style, Integer r,
			Integer g, Integer b) {
		style.setFillForegroundColor(new XSSFColor(new java.awt.Color(r, g, b), defaultIndexedColorMap));
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
	}

}
