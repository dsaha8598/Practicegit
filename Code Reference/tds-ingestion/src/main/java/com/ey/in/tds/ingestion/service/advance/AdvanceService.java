package com.ey.in.tds.ingestion.service.advance;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
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
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.ErrorCode;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.LDCUtilizationDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.LdcMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.NrTransactionsMetaDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceUtilizationDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.CustomSectionRateDTO;
import com.ey.in.tds.common.dto.CustomThresholdGroupLimitDto;
import com.ey.in.tds.common.dto.SurchargeAndCessDTO;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.model.advance.AdvanceMismatchByBatchIdDTO;
import com.ey.in.tds.common.model.advance.AdvancesErrorReportCsvDto;
import com.ey.in.tds.common.model.job.NoteBookParam;
import com.ey.in.tds.common.model.provision.MatrixDTO;
import com.ey.in.tds.common.model.provision.MatrixFileDTO;
import com.ey.in.tds.common.model.provision.UtilizationFileDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeDetailsDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMasterDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMasterUtilizationDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcUtilization;
import com.ey.in.tds.common.onboarding.jdbc.dto.NrTransactionsMeta;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.service.ErrorReportService;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.config.SparkNotebooks;
import com.ey.in.tds.ingestion.dto.invoice.UpdateOnScreenDTO;
import com.ey.in.tds.ingestion.jdbc.dao.AdvanceDAO;
import com.ey.in.tds.ingestion.jdbc.dao.AdvanceUtilizationDAO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceLineItemDAO;
import com.ey.in.tds.ingestion.service.tdsmismatch.TdsMismatchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

@Service
public class AdvanceService extends TdsMismatchService {
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
	private AdvanceDAO advanceDAO;

	@Autowired
	private LDCUtilizationDAO lDCUtilizationDAO;

	@Autowired
	private AdvanceUtilizationDAO advanceUtilizationDAO;

	@Autowired
	private InvoiceLineItemDAO invoiceLineItemDAO;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private SparkNotebookService sparkNotebookService;

	@Autowired
	private SparkNotebooks sparkNotebooks;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private LdcMasterDAO ldcMasterDAO;

	@Autowired
	private NrTransactionsMetaDAO nrTransactionsMetaDAO;

	Map<String, String> excelHeaderMap = new HashMap<String, String>() {
		private static final long serialVersionUID = -2138022865773603800L;

		{
			put("Source Identifier", "D");
			put("Source File Name", "E");
			put("Company Code", "F");
			put("Name of the Company Code", "G");
			put("Deductor PAN", "H");
			put("Deductor TAN", "I");
			put("Deductor GSTIN", "J");
			put("Deductee Code", "K");
			put("Name of the Deductee", "L");
			put("Non-Resident Deductee Indicator", "M");
			put("Deductee PAN", "N");
			put("Deductee TIN", "O");
			put("Deductee GSTIN", "P");
			put("Document Number", "Q");
			put("Document Type", "R");
			put("Document Date", "S");
			put("Posting Date of the Document", "T");
			put("Entry Date of Advance Made", "U");
			put("Line item Number in the Accounting Document", "V");
			put("Advance Amount", "W");
			put("HSN/SAC", "X");
			put("SAC Description", "Y");
			put("Advance to Vendor G/L Account Code", "Z");
			put("Advance to Vendor G/L Account Description", "AA");
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
			String userName) throws RecordNotFoundException {

		List<CustomSectionRateDTO> listRatesSections = mastersClient.getNatureOfPaymentMasterRecord().getBody()
				.getData();
		Map<String, List<Double>> ratesMap = new HashMap<String, List<Double>>();

		Map<String, String> sectionRateNature = new HashMap<String, String>();
		Map<String, BigInteger> sectionRateNopId = new HashMap<>();
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
			sectionRateNopId.put(customSectionRateDTO.getSection() + "-" + customSectionRateDTO.getRate(),
					customSectionRateDTO.getNoiId());
		}

		Map<Integer, Integer> nopGroupMap = new HashMap<Integer, Integer>();

		List<CustomThresholdGroupLimitDto> customThresholdGroupLimitList = mastersClient.getThresholdLimitGroup()
				.getBody().getData();

		for (CustomThresholdGroupLimitDto customThresholdGroupLimitDTO : customThresholdGroupLimitList) {
			nopGroupMap.put(customThresholdGroupLimitDTO.getNopId().intValue(),
					customThresholdGroupLimitDTO.getGroupId().intValue());
		}
		// Surcharge details
		List<Map<String, Object>> surchargeList = mastersClient.getAllSurchargeDetails("NR").getBody().getData();
		Map<String, List<Map<String, Object>>> surchargeMap = new HashMap<>();
		for (Map<String, Object> surcharge : surchargeList) {
			String deducteeStatus = (String) surcharge.get("status");
			String section = (String) surcharge.get("section");
			String key = section + "-" + deducteeStatus;
			if (!surchargeMap.containsKey(key)) {
				surchargeMap.put(key, new ArrayList<>());
			}
			surchargeMap.get(key).add(surcharge);
		}
		for (UpdateOnScreenDTO advanceMismatch : advanceMismatchUpdateDTO.getData()) {

			List<AdvanceDTO> advanceData = advanceDAO.findByYearTanDocumentPostingDateIdActive(
					advanceMismatchUpdateDTO.getAssessmentYear(), tan, advanceMismatch.getDocumentPostingDate(),
					advanceMismatch.getId(), false);
			BigDecimal finalAmount = BigDecimal.ZERO;
			Integer nopId = 0;
			Integer nopGroupId = 0;
			if (!advanceData.isEmpty()) {
				AdvanceDTO advance = advanceData.get(0);
				advance.setFinalTdsSection(advanceMismatch.getFinalSection());
				advance.setFinalTdsRate(advanceMismatch.getFinalRate());
				if ("ACCEPT".equalsIgnoreCase(advanceMismatchUpdateDTO.getActionType())) {
					advance.setFinalTdsAmount(advance.getDerivedTdsAmount());
				} else if ("REJECT".equalsIgnoreCase(advanceMismatchUpdateDTO.getActionType())) {
					advance.setFinalTdsAmount(advance.getClientAmount());
				} else if (advanceMismatch.getFinalAmount() != null
						&& advanceMismatch.getFinalAmount().compareTo(BigDecimal.ZERO) > 0) {
					advance.setFinalTdsAmount(advanceMismatch.getFinalAmount());
				} else if ("Modify - TDS base value".equalsIgnoreCase(advanceMismatchUpdateDTO.getActionType())) {
					advance.setClientTaxableAmount(advance.getAmount());
					advance.setAmount(advance.getTdsBaseValue());
					finalAmount = finalAmount.add(
							advance.getFinalTdsRate().multiply(advance.getAmount()).divide(BigDecimal.valueOf(100)));
					advance.setFinalTdsAmount(finalAmount);
				} else if ("Modify - Taxable value".equalsIgnoreCase(advanceMismatchUpdateDTO.getActionType())) {
					finalAmount = advanceMismatch.getTaxableValue().multiply(advance.getFinalTdsRate())
							.divide(BigDecimal.valueOf(100));
					advance.setFinalTdsAmount(finalAmount);
				} else if ("Modify - Any other amount".equalsIgnoreCase(advanceMismatchUpdateDTO.getActionType())) {
					advance.setClientTaxableAmount(advance.getAmount());
					advance.setAmount(advanceMismatch.getAnyOtherAmount());
					finalAmount = finalAmount.add(
							advance.getFinalTdsRate().multiply(advance.getAmount()).divide(BigDecimal.valueOf(100)));
					advance.setFinalTdsAmount(finalAmount);
				}
				if (UploadTypes.ADVANCE_NR_EXCEL.name().equalsIgnoreCase(advance.getProcessedFrom())
						&& !"Cancel".equalsIgnoreCase(advanceMismatchUpdateDTO.getActionType())
						&& !"ACCEPT".equalsIgnoreCase(advanceMismatchUpdateDTO.getActionType())
						&& advance.getHasDtaa() != null && advance.getHasDtaa().equals(false)) {
					List<NrTransactionsMeta> nrTransactionList = nrTransactionsMetaDAO.findById(tan,
							advance.getAssessmentYear(), advance.getNrTransactionsMetaId());
					if (!nrTransactionList.isEmpty()) {
						NrTransactionsMeta nrTransaction = nrTransactionList.get(0);
						advance.setSurcharge(
								surchargeCalculation(advance.getFinalTdsSection(), advance.getFinalTdsAmount(),
										advance.getAmount(), nrTransaction.getDeducteeStatus(), surchargeMap));
						BigDecimal cessAmount = advance.getSurcharge().add(advance.getFinalTdsAmount());
						advance.setCessAmount(
								cessAmount.multiply(advance.getCessRate()).divide(BigDecimal.valueOf(100)));
					}
				}
				advance.setReason(advanceMismatchUpdateDTO.getReason());
				advance.setMismatch(false);
				advance.setModifiedBy(userName);
				advance.setModifiedDate(new Date());
				advance.setActive(true);
				advance.setIsExempted(false);
				advance.setAction(advanceMismatchUpdateDTO.getActionType());
				if (!"Cancel".equalsIgnoreCase(advanceMismatchUpdateDTO.getActionType())
						&& !"Accept".equalsIgnoreCase(advanceMismatchUpdateDTO.getActionType())) {
					if (ratesMap != null && ratesMap.get(advance.getFinalTdsSection()) != null) {
						Double closestRate = closest(advance.getFinalTdsRate().doubleValue(),
								ratesMap.get(advance.getFinalTdsSection()));
						BigInteger nopIdInt = sectionRateNopId.get(advance.getFinalTdsSection() + "-" + closestRate);
						nopId = nopIdInt.compareTo(BigInteger.ZERO) > 0 ? nopIdInt.intValue() : 0;
					}
					if (nopId != null) {
						advance.setAdvanceNpId(nopId);
						nopGroupId = nopGroupMap.get(nopId);
					}

					if (nopGroupId != null) {
						advance.setAdvanceGroupid(nopGroupId);
					}
				}
				if ("IMPG".equalsIgnoreCase(advance.getSupplyType())) {
					if ("194Q".equalsIgnoreCase(advanceMismatch.getFinalSection())
							|| "NOTDS".equalsIgnoreCase(advanceMismatch.getFinalSection())
							|| StringUtils.isBlank(advanceMismatch.getFinalSection())) {
						advance.setIsExempted(true);
						advance.setErrorReason("Transaction out of Scope - Import of goods");
					}
				} else if (advance.getDeducteePan().equalsIgnoreCase(advance.getDeductorPan())) {
					if ("NOTDS".equalsIgnoreCase(advanceMismatch.getFinalSection())
							|| StringUtils.isBlank(advanceMismatch.getFinalSection())) {
						advance.setIsExempted(true);
						advance.setErrorReason("Transaction out of Scope - Stock Transfer");
					}
				} else if ("DLC".equalsIgnoreCase(advance.getDocumentType())) {
					if ("NOTDS".equalsIgnoreCase(advanceMismatch.getFinalSection())
							|| StringUtils.isBlank(advanceMismatch.getFinalSection())) {
						advance.setIsExempted(true);
						advance.setErrorReason("Transaction out of Scope - Delivery Challan");
					}
				} else if ("NOTDS".equalsIgnoreCase(advanceMismatch.getFinalSection())
						|| StringUtils.isBlank(advanceMismatch.getFinalSection())) {
					advance.setIsExempted(true);
					advance.setErrorReason("Transaction out of Scope - Scrape Sale");
				}
				logger.info("final Object Update --- : {}", advance);
				advanceDAO.update(advance);
			} else {
				logger.error("No Record for Advance Line item to Update");
				throw new CustomException("No Record for Advance Line item to Update",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Update mismatch by action  : {}", advanceMismatchUpdateDTO);
		}
		return advanceMismatchUpdateDTO;
	}

	// Service for getting Advance mismatches summary by TAN and ID;
	public List<AdvanceMismatchByBatchIdDTO> getAdvanceMismatchSummaryByTanAndId(String deductorMasterTan,
			Integer batchUploadId) {

		List<AdvanceMismatchByBatchIdDTO> listMisMatchBybatchDTO = new ArrayList<>();

		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "SM-RMM", batchUploadId, 0, 0));
		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "SMM-RMM", batchUploadId, 0, 0));
		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "SMM-RM", batchUploadId, 0, 0));
		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "NAD", batchUploadId, 0, 0));

		return listMisMatchBybatchDTO;
	}

	private AdvanceMismatchByBatchIdDTO groupMismatchesSummary(String tan, String type, Integer batchUploadId, int year,
			int month) {
		return advanceDAO.getAdvanceMismatchSummary(year, month, tan, batchUploadId, type);
	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @param batchUploadId
	 * @param mismatchCategory
	 * @param pagination
	 * @return
	 */
	// Service for getting Advance mismatches by TAN and ID;
	public CommonDTO<AdvanceDTO> getAdvanceMismatchByTanBatchIdMismatchCategory(String deductorMasterTan,
			Integer batchUploadId, String mismatchCategory, Pagination pagination) {

		BigInteger count = BigInteger.ZERO;
		List<AdvanceDTO> listMisMatch = advanceDAO.getAdvanceDetailsByTanBatchIdAndMismatchCategory(deductorMasterTan,
				mismatchCategory, batchUploadId, pagination);
		count = advanceDAO.getCountoFAdvanceDetailsByTanBatchIdAndMismatchCategory(deductorMasterTan, mismatchCategory,
				batchUploadId);
		PagedData<AdvanceDTO> pagedData = new PagedData<>(listMisMatch, listMisMatch.size(), pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		CommonDTO<AdvanceDTO> advanceData = new CommonDTO<>();
		advanceData.setResultsSet(pagedData);
		advanceData.setCount(count);

		return advanceData;
	}

	// Service for getting All Advance mismatches summary by TAN and Active;
	public List<AdvanceMismatchByBatchIdDTO> getAllAdvanceMismatchSummaryByTanAndActive(String deductorMasterTan,
			int assessmentYear, int month) {

		List<AdvanceMismatchByBatchIdDTO> listMisMatchBybatchDTO = new ArrayList<>();

		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "SM-RMM", null, assessmentYear, month));
		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "SMM-RM", null, assessmentYear, month));
		listMisMatchBybatchDTO.add(groupMismatchesSummary(deductorMasterTan, "SMM-RMM", null, assessmentYear, month));
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
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public CommonDTO<AdvanceDTO> getAllAdvanceMismatchByTanAndMismatchCategory(String deductorMasterTan,
			String mismatchCategory, int assessmentYear, int month, MismatchesFiltersDTO filters)
			throws JsonMappingException, JsonProcessingException {

		CommonDTO<AdvanceDTO> advanceData = new CommonDTO<>();
		List<AdvanceDTO> resultedList = new ArrayList<>();
		List<AdvanceDTO> listMisMatch = advanceDAO.getAdvanceDetailsByTanActiveAndMismatchCategory(assessmentYear,
				month, deductorMasterTan, mismatchCategory, filters);
		for (AdvanceDTO dto : listMisMatch) {
			List<String> sections = advanceDAO.getVendorSections(dto.getDeducteeKey(), dto.getIsResident());
			dto.setVendorsectionCount((long) sections.size());
			dto.setVendorsectionList(sections.isEmpty() ? Arrays.asList("No Section") : sections);
			resultedList.add(dto);
		}

		PagedData<AdvanceDTO> pagedData = null;
		if (!resultedList.isEmpty()) {
			pagedData = new PagedData<AdvanceDTO>(resultedList, resultedList.size(),
					filters.getPagination().getPageNumber(),
					resultedList
							.size() > (filters.getPagination().getPageSize() * filters.getPagination().getPageNumber())
									? false
									: true);
		}

		advanceData.setResultsSet(pagedData);
		advanceData.setCount(advanceDAO.getAdvanceDetailsCountByTanMismatchCategory(assessmentYear, month,
				deductorMasterTan, mismatchCategory, filters));
		return advanceData;
	}

	public BatchUpload importToAdvanceBatchUpload(MultipartFile file, String tenantId, String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {

		int year = Calendar.getInstance().get(Calendar.YEAR);
		int month = Calendar.getInstance().get(Calendar.YEAR) + 1;

		logger.info(file.getOriginalFilename());

		String sha256 = sha256SumService.getSHA256Hash(file);
		BatchUpload batchUpload = new BatchUpload();

		List<BatchUpload> batch = batchUploadDAO.getSha256RecordsBasedonYearMonth(year, month,
				UploadTypes.INV_REM.name(), sha256);

		String path = blob.uploadExcelToBlob(file, tenantId);
		batchUpload.setAssessmentYear(year);
		batchUpload.setDeductorMasterTan("missingDeductorTan");
		batchUpload.setUploadType(UploadTypes.INV_REM.name());
		if (!batch.isEmpty()) {
			logger.info("Duplicate PDF Record inserting : {}", file.getOriginalFilename());
			batchUpload.setAssessmentYear(year);
			batchUpload.setDeductorMasterTan("missingDeductorTan");
			batchUpload.setUploadType(UploadTypes.INV_REM.name());
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setReferenceId(batch.get(0).getBatchUploadID());
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
		batchUpload = batchUploadDAO.save(batchUpload);

		return batchUpload;
	}

	@Async
	public ByteArrayInputStream asyncExportRemediationReport(String deductorMasterTan, String tenantId,
			String deductorPan, int year, int month, String userName, Boolean isMismatch)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		return exportRemediationReport(deductorMasterTan, tenantId, deductorPan, year, month, userName, isMismatch);
	}

	/**
	 * 
	 * @param tan
	 * @param tenantId
	 * @param deductorPan
	 * @param year
	 * @param month
	 * @param userName
	 * @param isMismatch
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	// TODO need to changes method name
	public ByteArrayInputStream exportRemediationReport(String tan, String tenantId, String deductorPan, int year,
			int month, String userName, boolean isMismatch)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (!isMismatch) {
			List<BatchUpload> batchList = batchUploadDAO.findBatchUploadBy(year, tan, month);
			if (!batchList.isEmpty()) {
				isMismatch = true;
			}
		}
		String fileName = StringUtils.EMPTY;
		if (!isMismatch) {
			fileName = UploadTypes.ADVANCE_REPORT.name().toLowerCase() + "_"
					+ CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		} else {
			fileName = UploadTypes.ADVANCE_MISMATCH_REPORT.name().toLowerCase() + "_"
					+ CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		}
		BatchUpload batchUpload = saveMismatchReport(tan, tenantId, year, out, 0L,
				UploadTypes.ADVANCE_MISMATCH_REPORT.name(), "Processing", month, userName, null, fileName);
		String type = "advance_mismatch";
		// Invocking spark Job id
		logger.info("Notebook type : {}", type);
		NoteBookParam noteBookParam = sparkNotebookService.createNoteBook(year, tan, tenantId, userName, month,
				batchUpload, "", "");
		noteBookParam.setIsMismatch(isMismatch);
		SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get(type.toLowerCase());
		logger.info("Notebook url : {}", notebook.getUrl());
		sparkNotebookService.triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam, month, year,
				tenantId, tan, userName);

		return null;
	}

	public ByteArrayInputStream exportRemediationReport1(String deductorMasterTan, String tenantId, String deductorPan,
			int year, int month, String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String fileName = UploadTypes.ADVANCE_MISMATCH_REPORT.name() + "_"
				+ CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		BatchUpload batchUpload = saveMismatchReport(deductorMasterTan, tenantId, year, null, 0L,
				UploadTypes.ADVANCE_MISMATCH_REPORT.name(), "Processing", month, userName, null, fileName);

		try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
			SXSSFSheet sheet = wb.createSheet("Advance_Remediation_Report");
			sheet.setColumnHidden(20, true);
			sheet.setColumnHidden(21, true);
			sheet.setColumnHidden(22, true);
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);
			sheet.protectSheet("password");
			sheet.setDisplayGridlines(false);
			DataValidation dataValidation = null;
			DataValidationConstraint constraint = null;
			DataValidationHelper validationHelper = null;

			SXSSFRow row0 = sheet.createRow(0);
			XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
			style.setWrapText(true);
			XSSFFont fonts = (XSSFFont) wb.createFont();
			fonts.setBold(true);
			style.setFont(fonts);

			style.setBorderLeft(BorderStyle.MEDIUM);
			style.setBorderTop(BorderStyle.MEDIUM);
			style.setBorderBottom(BorderStyle.MEDIUM);
			style.setBorderRight(BorderStyle.MEDIUM);
			style.setAlignment(HorizontalAlignment.LEFT);
			style.setVerticalAlignment(VerticalAlignment.CENTER);

			sheet.createRow(1);
			XSSFCellStyle style01 = (XSSFCellStyle) wb.createCellStyle();
			style01.setBorderLeft(BorderStyle.MEDIUM);
			style01.setBorderTop(BorderStyle.MEDIUM);
			style01.setBorderBottom(BorderStyle.MEDIUM);
			style01.setBorderRight(BorderStyle.MEDIUM);
			style01.setAlignment(HorizontalAlignment.LEFT);
			style01.setVerticalAlignment(VerticalAlignment.CENTER);
			XSSFFont font01 = (XSSFFont) wb.createFont();
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
			XSSFFont font02 = (XSSFFont) wb.createFont();
			font02.setBold(true);
			style02.setFont(font02);
			row0.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
			String msg = getErrorReportMsg(deductorMasterTan, tenantId, deductorPan);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);
			sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 22));

			SXSSFRow row03 = sheet.createRow(3);
			XSSFCellStyle style03 = (XSSFCellStyle) wb.createCellStyle();
			style03.setBorderLeft(BorderStyle.MEDIUM);
			style03.setBorderTop(BorderStyle.MEDIUM);
			style03.setBorderBottom(BorderStyle.MEDIUM);
			style03.setBorderRight(BorderStyle.MEDIUM);
			style03.setAlignment(HorizontalAlignment.CENTER);
			style03.setVerticalAlignment(VerticalAlignment.CENTER);
			DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();
			style03.setFillForegroundColor(new XSSFColor(new java.awt.Color(248, 203, 173), defaultIndexedColorMap));
			style03.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFFont font03 = (XSSFFont) wb.createFont();
			style03.setFont(font03);
			style03.setAlignment(HorizontalAlignment.CENTER);
			row03.createCell(0).setCellValue("");
			row03.createCell(1).setCellValue("Sections/Rate applied");
			row03.getCell(1).setCellStyle(style03);

			CellRangeAddress cellRangeAddress1 = new CellRangeAddress(3, 3, 1, 3);
			sheet.addMergedRegion(cellRangeAddress1);

			style03.setAlignment(HorizontalAlignment.CENTER);
			row03.createCell(4).setCellValue("Tool Derived Sections/Rate");
			row03.getCell(4).setCellStyle(style03);

			CellRangeAddress cellRangeAddress2 = new CellRangeAddress(3, 3, 4, 6);
			sheet.addMergedRegion(cellRangeAddress2);

			style03.setAlignment(HorizontalAlignment.CENTER);
			row03.createCell(7).setCellValue("Mismatch Category");
			row03.getCell(7).setCellStyle(style03);
			CellRangeAddress cellRangeAddress3 = new CellRangeAddress(3, 3, 7, 10);
			sheet.addMergedRegion(cellRangeAddress3);

			style03.setAlignment(HorizontalAlignment.CENTER);
			row03.createCell(25).setCellValue("Client Response");
			row03.getCell(25).setCellStyle(style03);
			CellRangeAddress cellRangeAddress4 = new CellRangeAddress(3, 3, 25, 29);
			sheet.addMergedRegion(cellRangeAddress4);

			SXSSFRow row1 = sheet.createRow(4);
			sheet.setDefaultColumnWidth(25);
			XSSFCellStyle style0 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style1 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style2 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style3 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style003 = (XSSFCellStyle) wb.createCellStyle();

			// setting borders and colors to the style
			setMediumBorder(defaultIndexedColorMap, style0, 46, 134, 193);
			setMediumBorder(defaultIndexedColorMap, style1, 174, 170, 170);
			setMediumBorder(defaultIndexedColorMap, style2, 180, 198, 231);
			setMediumBorder(defaultIndexedColorMap, style3, 255, 255, 0);
			setMediumBorder(defaultIndexedColorMap, style003, 255, 230, 153);

			// setting font styles
			XSSFFont font = (XSSFFont) wb.createFont();
			font.setBold(true);
			font.setFontName("Arial");
			style0.setFont(font);
			style1.setFont(font);
			style2.setFont(font);
			style3.setFont(font);
			style003.setFont(font);

			// Create a cell
			row1.createCell(0).setCellValue("Sequence Number");
			row1.getCell(0).setCellStyle(style0);
			row1.createCell(1).setCellValue("Client Amount");
			row1.getCell(1).setCellStyle(style1);
			row1.createCell(2).setCellValue("TDS Section");
			row1.getCell(2).setCellStyle(style1);
			row1.createCell(3).setCellValue("TDS Rate");
			row1.getCell(3).setCellStyle(style1);
			row1.createCell(4).setCellValue("Derived Amount");
			row1.getCell(4).setCellStyle(style1);
			row1.createCell(5).setCellValue("TDS Section");
			row1.getCell(5).setCellStyle(style1);
			row1.createCell(6).setCellValue("TDS Rate");
			row1.getCell(6).setCellStyle(style1);
			row1.createCell(7).setCellValue("Amount");
			row1.getCell(7).setCellStyle(style1);
			row1.createCell(8).setCellValue("Section");
			row1.getCell(8).setCellStyle(style1);
			row1.createCell(9).setCellValue("Rate");
			row1.getCell(9).setCellStyle(style1);
			row1.createCell(10).setCellValue("Deduction Type");
			row1.getCell(10).setCellStyle(style1);
			row1.createCell(11).setCellValue("Mismatch Interpretation");
			row1.getCell(11).setCellStyle(style1);
			row1.createCell(12).setCellValue("Deductor TAN");
			row1.getCell(12).setCellStyle(style2);
			row1.createCell(13).setCellValue("Deductee PAN");
			row1.getCell(13).setCellStyle(style003);
			row1.createCell(14).setCellValue("Name of the Deductee");
			row1.getCell(14).setCellStyle(style2);
			row1.createCell(15).setCellValue("Number of sections");
			row1.getCell(15).setCellStyle(style2);
			row1.createCell(16).setCellValue("Vendor section");
			row1.getCell(16).setCellStyle(style2);

			row1.createCell(17).setCellValue("Service Description");
			row1.getCell(17).setCellStyle(style1);
			row1.createCell(18).setCellValue("Service Description - PO");
			row1.getCell(18).setCellStyle(style3);
			row1.createCell(19).setCellValue("Service Description - GL Text");
			row1.getCell(19).setCellStyle(style3);
			row1.createCell(20).setCellValue("Invoice Line  Hash Code");
			row1.getCell(20).setCellStyle(style1);
			row1.createCell(21).setCellValue("With Holding Section");
			row1.getCell(21).setCellStyle(style1);

			row1.createCell(22).setCellValue("Advance Id");
			row1.getCell(22).setCellStyle(style1);
			row1.createCell(23).setCellValue("Confidence");
			row1.getCell(23).setCellStyle(style1);
			row1.createCell(24).setCellValue("ERP Document Number");
			row1.getCell(24).setCellStyle(style3);
			// Mismatch Category
			row1.createCell(25).setCellValue("Action");
			row1.getCell(25).setCellStyle(style1);
			row1.createCell(26).setCellValue("Reason");
			row1.getCell(26).setCellStyle(style1);
			row1.createCell(27).setCellValue("Final TdsRate");
			row1.getCell(27).setCellStyle(style1);
			row1.createCell(28).setCellValue("Final TdsSection");
			row1.getCell(28).setCellStyle(style1);
			row1.createCell(29).setCellValue("Final Amount");
			row1.getCell(29).setCellStyle(style1);

			// Adding hidden columns
			row1.createCell(30).setCellValue("Assessment Year");
			row1.getCell(30).setCellStyle(style1);
			row1.createCell(31).setCellValue("Document Posting Date");
			row1.getCell(31).setCellStyle(style1);

			sheet.setColumnHidden(30, true);
			sheet.setColumnHidden(31, true);

			// Auto Filter Option
			sheet.setAutoFilter(new CellRangeAddress(4, 27, 0, 27));
			sheet.createFreezePane(0, 1);

			// sheet.createFreezePane(colSplit, rowSplit, leftmostColumn, topRow)-this
			// method is used for scrolling rows and column vertically and horizontally
			sheet.createFreezePane(4, 5, 5, 5);
			List<AdvanceDTO> advanceList = advanceDAO.getAllAdvanceMismatches(deductorMasterTan, year, month);
			long size = advanceList.size();

			CellRangeAddressList addressList = new CellRangeAddressList(4, (int) size + 4, 25, 25);
			validationHelper = sheet.getDataValidationHelper();
			constraint = validationHelper
					.createExplicitListConstraint(new String[] { "Accept", "Modify", "Reject", "Cancel" });
			dataValidation = validationHelper.createValidation(constraint, addressList);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.setShowPromptBox(true);
			sheet.addValidationData(dataValidation);

			/*
			 * validationHelper = new XSSFDataValidationHelper(sheet); CellRangeAddressList
			 * addressList1 = new CellRangeAddressList(4, 27, 27, 27); constraint =
			 * validationHelper.createNumericConstraint(XSSFDataValidationConstraint.
			 * ValidationType.DECIMAL, XSSFDataValidationConstraint.OperatorType.BETWEEN,
			 * String.valueOf(Float.MIN_VALUE), String.valueOf(Float.MAX_VALUE));
			 * dataValidation = validationHelper.createValidation(constraint, addressList1);
			 * dataValidation.setSuppressDropDownArrow(true);
			 * dataValidation.setShowErrorBox(true); dataValidation.setShowPromptBox(true);
			 * sheet.addValidationData(dataValidation);
			 */

			validationHelper = sheet.getDataValidationHelper();
			CellRangeAddressList addressList2 = new CellRangeAddressList(4, (int) size + 4, 28, 28);
			// Feign Client to get list of sections from nature of payment
			ResponseEntity<ApiStatus<List<String>>> natureOfPayament = mastersClient.findAllNatureOfPaymentSections();
			List<String> natureOfPayamentList = natureOfPayament.getBody().getData();
			Set<String> set = new LinkedHashSet<>();
			set.addAll(natureOfPayamentList);
			natureOfPayamentList.clear();
			natureOfPayamentList.addAll(set);
			constraint = validationHelper.createExplicitListConstraint(
					natureOfPayamentList.toArray(new String[natureOfPayamentList.size()]));
			dataValidation = validationHelper.createValidation(constraint, addressList2);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.setShowPromptBox(true);
			sheet.addValidationData(dataValidation);

			XSSFCellStyle style5 = (XSSFCellStyle) wb.createCellStyle();
			XSSFFont font05 = (XSSFFont) wb.createFont();
			style5.setFont(font05);
			style5.setLocked(true);
			setCellColorAndBoarder(defaultIndexedColorMap, style5, 191, 201, 202);

			XSSFCellStyle style4 = (XSSFCellStyle) wb.createCellStyle();
			XSSFFont font4 = (XSSFFont) wb.createFont();
			style4.setFont(font4);
			style4.setLocked(false);
			setCellColorAndBoarder(defaultIndexedColorMap, style4, 255, 255, 255);

			int rowindex = 5;

			logger.info("Page size : {} actual results : {}", pageSize, advanceList.size());
			for (AdvanceDTO listData : advanceList) {
				List<String> sections = new ArrayList<>();
				logger.info(listData.getUserDefinedField1());
				SXSSFRow row2 = sheet.createRow(rowindex++);
				row2.createCell(0)
						.setCellValue(listData.getSequenceNumber() == null ? " " : listData.getSequenceNumber());
				row2.getCell(0).setCellStyle(style5);
				row2.createCell(1).setCellValue(
						listData.getWithholdingAmount() == null ? " " : listData.getWithholdingAmount().toString());
				row2.getCell(1).setCellStyle(style5);
				row2.createCell(2).setCellValue(listData.getWithholdingSection());
				row2.getCell(2).setCellStyle(style5);
				row2.createCell(3).setCellValue(
						listData.getWithholdingRate() == null ? " " : listData.getWithholdingRate().toString());
				row2.getCell(3).setCellStyle(style5);
				row2.createCell(4).setCellValue(
						listData.getDerivedTdsAmount() == null ? " " : listData.getDerivedTdsAmount().toString());
				row2.getCell(4).setCellStyle(style5);
				row2.createCell(5).setCellValue(listData.getDerivedTdsSection());
				row2.getCell(5).setCellStyle(style5);

				if (listData.getDerivedTdsRate() != null) {
					row2.createCell(6).setCellValue(listData.getDerivedTdsRate().toString());
					row2.getCell(6).setCellStyle(style5);
				}

				row2.createCell(7).setCellValue(listData.getAmount() == null ? " " : listData.getAmount().toString());
				row2.getCell(7).setCellStyle(style5);
				row2.createCell(8).setCellValue(listData.getWithholdingSection());
				row2.getCell(8).setCellStyle(style5);

				row2.createCell(9).setCellValue(
						listData.getWithholdingRate() == null ? " " : listData.getWithholdingRate().toString());
				row2.getCell(9).setCellStyle(style5);

				row2.createCell(10).setCellValue(listData.getMismatchCategory());
				row2.getCell(10).setCellStyle(style5);

				row2.createCell(11).setCellValue(listData.getMismatchInterpretation());
				row2.getCell(11).setCellStyle(style5);

				row2.createCell(12).setCellValue(listData.getDeductorMasterTan());
				row2.getCell(12).setCellStyle(style5);

				row2.createCell(13).setCellValue(listData.getDeducteePan());
				row2.getCell(13).setCellStyle(style5);

				row2.createCell(14).setCellValue(listData.getDeducteeName());
				row2.getCell(14).setCellStyle(style5);

				sections = advanceDAO.getVendorSections(listData.getDeducteeKey(), listData.getIsResident());

				row2.createCell(15).setCellValue(sections.size() + "");
				row2.getCell(15).setCellStyle(style5);

				row2.createCell(16).setCellValue(
						sections.isEmpty() ? "No Section" : sections.toString().replace("[", "").replace("]", ""));
				row2.getCell(16).setCellStyle(style5);

				row2.createCell(17).setCellValue(listData.getServiceDescription());
				row2.getCell(17).setCellStyle(style5);

				row2.createCell(18).setCellValue(listData.getServiceDescriptionPo());
				row2.getCell(18).setCellStyle(style5);

				row2.createCell(19).setCellValue(listData.getServiceDescriptionGl());
				row2.getCell(19).setCellStyle(style5);
				row2.createCell(20).setCellValue(listData.getId().toString());
				row2.getCell(20).setCellStyle(style5);
				row2.createCell(21).setCellValue(listData.getWithholdingSection());
				row2.getCell(21).setCellStyle(style5);
				row2.createCell(22).setCellValue(listData.getId().toString());
				row2.getCell(22).setCellStyle(style5);
				row2.createCell(23).setCellValue(listData.getConfidence());
				row2.getCell(23).setCellStyle(style5);
				row2.createCell(24).setCellValue(listData.getDocumentNumber());
				row2.getCell(24).setCellStyle(style5);

				// unlocked data
				row2.createCell(25).setCellValue("");
				row2.getCell(25).setCellStyle(style4);
				row2.createCell(26).setCellValue("");
				row2.getCell(26).setCellStyle(style4);
				row2.createCell(27).setCellValue("");
				row2.getCell(27).setCellStyle(style4);
				row2.createCell(28).setCellValue("");
				row2.getCell(28).setCellStyle(style4);
				row2.createCell(29).setCellValue("");
				row2.getCell(29).setCellStyle(style4);

				row2.createCell(30).setCellValue(
						listData.getAssessmentYear() == null ? " " : listData.getAssessmentYear().toString());
				row2.getCell(30).setCellStyle(style4);
				row2.createCell(31).setCellValue(listData.getPostingDateOfDocument() == null ? " "
						: listData.getPostingDateOfDocument().toString());
				row2.getCell(31).setCellStyle(style4);

			}
			logger.info("Processed records : {}", advanceList.size());
			updateMismatchExportReportStatus(advanceList.size(), size, batchUpload);
			wb.write(out);
			saveMismatchReport(deductorMasterTan, tenantId, year, out, Long.valueOf(size),
					UploadTypes.ADVANCE_MISMATCH_REPORT.name(), "Processed", month, userName,
					batchUpload.getBatchUploadID(), fileName);
		}
		return new ByteArrayInputStream(out.toByteArray());
	}

	/**
	 * 
	 * @param tan
	 * @param batchId
	 * @param file
	 * @param tenantId
	 * @param deductorPan
	 * @param year
	 * @param userEmail
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	@Async
	public BatchUpload asyncUpdateRemediationReport(String tan, BatchUpload batchUpload, String path, String tenantId,
			String deductorPan, int year, String userEmail)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		if (batchUpload != null) {
			batchUpload.setFailedCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setRowsCount(0L);
			batchUpload.setStatus("Processing");
			batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
			batchUploadDAO.update(batchUpload);
			batchUpload = updateMismatchRemediationReport(tan, batchUpload, path, tenantId, deductorPan, year,
					userEmail);
		}
		return batchUpload;
	}

	public BatchUpload updateMismatchRemediationReport(String tan, BatchUpload batchUpload, String path,
			String tenantId, String deductorPan, int year, String userEmail)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		Biff8EncryptionKey.setCurrentUserPassword("password");
		Workbook workbook;
		try {
			logger.info("Mismatch file path : {}", path);
			File file = blobStorageService.getFileFromBlobUrl(tenantId, path);
			workbook = new Workbook(file.getAbsolutePath());
			Worksheet worksheet = workbook.getWorksheets().get(0);
			worksheet.getCells().deleteRows(0, 14);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.CSV);
			File xlsxInvoiceFile = new File("TestCsvFile");
			FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
			CsvReader csvReader = new CsvReader();
			csvReader.setContainsHeader(true);
			CsvContainer csv = csvReader.read(xlsxInvoiceFile, StandardCharsets.UTF_8);

			String otherFilePath = null;
			String errorFilepath = null;
			int processedCount = 0;
			int errorCount = 0;

			List<AdvanceDTO> advanceList = new ArrayList<AdvanceDTO>();
			List<CsvRow> errorList = new ArrayList<>();
			List<String> errorMessages = new ArrayList<>();
			List<Integer> advanceIds = new ArrayList<>();
			boolean isCancel = false;

			List<LdcUtilization> ldcUtilizationList = new ArrayList<>();
			// Rate and section change
			List<CustomSectionRateDTO> listRatesSections = mastersClient.getNatureOfPaymentMasterRecord().getBody()
					.getData();
			Map<String, List<Double>> ratesMap = new HashMap<String, List<Double>>();

			Map<String, String> sectionRateNature = new HashMap<String, String>();
			Map<String, BigInteger> sectionRateNopId = new HashMap<>();
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
				sectionRateNopId.put(customSectionRateDTO.getSection() + "-" + customSectionRateDTO.getRate(),
						customSectionRateDTO.getNoiId());
			}

			Map<Integer, Integer> nopGroupMap = new HashMap<Integer, Integer>();

			List<CustomThresholdGroupLimitDto> customThresholdGroupLimitList = mastersClient.getThresholdLimitGroup()
					.getBody().getData();

			for (CustomThresholdGroupLimitDto customThresholdGroupLimitDTO : customThresholdGroupLimitList) {
				nopGroupMap.put(customThresholdGroupLimitDTO.getNopId().intValue(),
						customThresholdGroupLimitDTO.getGroupId().intValue());
			}
			// Cess details
			ResponseEntity<ApiStatus<List<SurchargeAndCessDTO>>> cessMasterDetails = mastersClient
					.getCessDetailsByCessType("nonresident");
			List<SurchargeAndCessDTO> cessMasterList = cessMasterDetails.getBody().getData();
			// Surcharge details
			List<Map<String, Object>> surchargeList = mastersClient.getAllSurchargeDetails("NR").getBody().getData();
			Map<String, List<Map<String, Object>>> surchargeMap = new HashMap<>();
			for (Map<String, Object> surcharge : surchargeList) {
				String deducteeStatus = (String) surcharge.get("status");
				String section = (String) surcharge.get("section");
				String key = section + "-" + deducteeStatus;
				if (!surchargeMap.containsKey(key)) {
					surchargeMap.put(key, new ArrayList<>());
				}
				surchargeMap.get(key).add(surcharge);
			}
			for (CsvRow row : csv.getRows()) {
				if (StringUtils.isNotBlank(StringUtils.join(row.getFields(), ", ").replace(",", ""))) {
					// derived
					String derivedTdsSectionFromExcel = row.getField("Derived TDS Section");
					BigDecimal derivedTdsRateFromExcel = StringUtils.isNotBlank(row.getField("Derived TDS Rate"))
							? new BigDecimal(row.getField("Derived TDS Rate"))
							: null;
					BigDecimal derivedTdsAmountFromExcel = StringUtils.isNotBlank(row.getField("Derived TDS Amount"))
							? new BigDecimal(row.getField("Derived TDS Amount"))
							: null;
					// Actual or withholding
					String actualTdsSectionFromExcel = row.getField("TDSSection");
					BigDecimal actualTdsRateFromExcel = StringUtils.isNotBlank(row.getField("TDSRate"))
							? new BigDecimal(row.getField("TDSRate"))
							: null;
					BigDecimal actualTdsAmountFromExcel = StringUtils.isNotBlank(row.getField("TDSAmount"))
							? new BigDecimal(row.getField("TDSAmount"))
							: null;
					// Final
					String finalTdsSectionFromExcel = row.getField("Final TDS Section");
					BigDecimal finalTdsRateFromExcel = StringUtils.isNotBlank(row.getField("Final TDS Rate"))
							? new BigDecimal(row.getField("Final TDS Rate"))
							: null;
					BigDecimal finalTdsAmountFromExcel = StringUtils.isNotBlank(row.getField("Final TDS Amount"))
							? new BigDecimal(row.getField("Final TDS Amount"))
							: null;

					// Amount
					BigDecimal taxableAmount = StringUtils.isNotBlank(row.getField("TaxableValue"))
							? new BigDecimal(row.getField("TaxableValue"))
							: null;
					// Action
					String userAction = row.getField("Action");
					// Reason
					String reason = row.getField("Reason");
					// id
					Integer advanceId = StringUtils.isNotBlank(row.getField("Line  Item Hash Code"))
							? Integer.parseInt(row.getField("Line  Item Hash Code"))
							: null;
					// nrTransactionsMetaId
					BigDecimal nrTransactionsMetaId = StringUtils.isNotBlank(row.getField("Nr Tansactions Meta Id"))
							? new BigDecimal(row.getField("Nr Tansactions Meta Id"))
							: null;

					Integer assessmentYear = StringUtils.isNotBlank(row.getField("Assessment Year"))
							? Integer.parseInt(row.getField("Assessment Year"))
							: null;

					Integer challanMonth = StringUtils.isNotBlank(row.getField("Challan Month"))
							? Integer.parseInt(row.getField("Challan Month"))
							: null;

					String isResidential = row.getField("NRIndicator");

					String deducteePan = row.getField("DeducteePAN");

					String processedFrom = row.getField("Processed From");

					Boolean hasLdc = StringUtils.isNotBlank(row.getField("Is LDC applicable?"))
							? Boolean.parseBoolean(row.getField("Is LDC applicable?"))
							: null;

					// Document posting date
					Date documentPostingDate = StringUtils.isNotBlank(row.getField("PostingDate"))
							? new SimpleDateFormat("yyyy-MM-dd").parse(row.getField("PostingDate"))
							: null;
					// surcharge
					BigDecimal surcharge = StringUtils.isNotBlank(row.getField("surcharge"))
							? new BigDecimal(row.getField("surcharge"))
							: null;

					// interest
					BigDecimal interest = StringUtils.isNotBlank(row.getField("interest"))
							? new BigDecimal(row.getField("interest"))
							: null;

					// cessAmount
					BigDecimal cessAmount = StringUtils.isNotBlank(row.getField("CESSAmount"))
							? new BigDecimal(row.getField("CESSAmount"))
							: null;

					// cessRate
					BigDecimal cessRate = StringUtils.isNotBlank(row.getField("CESSRate"))
							? new BigDecimal(row.getField("CESSRate"))
							: null;
					// Supply type
					String supplyType = row.getField("SupplyType");

					// Document type
					String documentType = row.getField("DocumentType");

					// Tds Base value
					BigDecimal tdsBaseValue = StringUtils.isNotBlank(row.getField("TDSBaseValue"))
							? new BigDecimal(row.getField("TDSBaseValue"))
							: BigDecimal.ZERO;

					BigDecimal anyOtherAmount = StringUtils.isNotBlank(row.getField("Any Other Amount"))
							? new BigDecimal(row.getField("Any Other Amount"))
							: BigDecimal.ZERO;
					Integer advanceGroupId = StringUtils.isNotBlank(row.getField("GroupId"))
							? Integer.parseInt(row.getField("GroupId"))
							: null;

					Integer advanceNopId = StringUtils.isNotBlank(row.getField("NopId"))
							? Integer.parseInt(row.getField("NopId"))
							: null;
					String deducteeStatus = StringUtils.isNotBlank(row.getField("DeducteeStatus"))
							? row.getField("DeducteeStatus")
							: StringUtils.EMPTY;

					Boolean isError = false;

					BigDecimal finalAmount = new BigDecimal(0);
					Integer nopId = 0;
					Integer nopGroupId = 0;

					if (StringUtils.isNotBlank(userAction)) {
						AdvanceDTO advanceData = new AdvanceDTO();
						if (advanceId != null) {
							advanceData.setPostingDateOfDocument(documentPostingDate);
							advanceData.setAssessmentYear(assessmentYear);
							advanceData.setId(advanceId);
							advanceData.setIsResident(isResidential);
							advanceData.setReason(reason);
							advanceData.setAction(userAction);
							advanceData.setProcessedFrom(processedFrom);
							advanceData.setHasLdc(hasLdc);
							advanceData.setDeductorMasterTan(tan);
							advanceData.setDeducteePan(deducteePan);
							advanceData.setChallanMonth(challanMonth);
							advanceData.setAmount(taxableAmount);
							advanceData.setReason(reason);
							advanceData.setNrTransactionsMetaId(
									nrTransactionsMetaId != null ? nrTransactionsMetaId.intValue() : null);
							advanceData.setModifiedBy(userEmail);
							advanceData.setSurcharge(surcharge);
							advanceData.setInterest(interest);
							advanceData.setCessRate(cessRate);
							advanceData.setCessAmount(cessAmount);
							advanceData.setIsExempted(false);
							advanceData.setAdvanceGroupid(advanceGroupId);
							advanceData.setAdvanceNpId(advanceNopId);
							if ("Accept".equalsIgnoreCase(userAction)) {
								String message = StringUtils.EMPTY;
								if (StringUtils.isNotBlank(row.getField("Mismatch Type"))
										&& row.getField("Mismatch Type").equals("NAD")) {
									isError = true;
									errorCount++;
									errorList.add(row);
									message = message
											+ "Action column can not have value as Accept while Mismatch catagory Is NAD"
											+ "\n";
								}
								if (derivedTdsSectionFromExcel != null && derivedTdsRateFromExcel != null
										&& derivedTdsAmountFromExcel != null) {
									advanceData.setFinalTdsSection(derivedTdsSectionFromExcel);
									advanceData.setFinalTdsAmount(derivedTdsAmountFromExcel);
									advanceData.setFinalTdsRate(derivedTdsRateFromExcel);
									advanceData.setActive(true);
									advanceData.setMismatch(false);
									if ("IMPG".equalsIgnoreCase(supplyType)) {
										if ("194Q".equalsIgnoreCase(advanceData.getFinalTdsSection())
												|| "NOTDS".equalsIgnoreCase(advanceData.getFinalTdsSection())
												|| StringUtils.isBlank(advanceData.getFinalTdsSection())) {
											advanceData.setIsExempted(true);
											advanceData.setErrorReason("Transaction out of Scope - Import of goods");
										}
									} else if (advanceData.getDeducteePan()
											.equalsIgnoreCase(advanceData.getDeductorPan())) {
										if ("NOTDS".equalsIgnoreCase(advanceData.getFinalTdsSection())
												|| StringUtils.isBlank(advanceData.getFinalTdsSection())) {
											advanceData.setIsExempted(true);
											advanceData.setErrorReason("Transaction out of Scope - Stock Transfer");
										}
									} else if ("DLC".equalsIgnoreCase(documentType)) {
										if ("NOTDS".equalsIgnoreCase(advanceData.getFinalTdsSection())
												|| StringUtils.isBlank(advanceData.getFinalTdsSection())) {
											advanceData.setIsExempted(true);
											advanceData.setErrorReason("Transaction out of Scope - Delivery Challan");
										}
									} else if ("NOTDS".equalsIgnoreCase(advanceData.getFinalTdsSection())
											|| StringUtils.isBlank(advanceData.getFinalTdsSection())) {
										advanceData.setIsExempted(true);
										advanceData.setErrorReason("Transaction out of Scope - Scrape Sale");
									}
								} else {
									isError = true;
									errorCount++;
									errorList.add(row);

									if (StringUtils.isEmpty(derivedTdsSectionFromExcel)) {
										message = " Derived TDS Section is empty or null" + "\n";
									}
									if (derivedTdsRateFromExcel == null) {
										message = message + " Derived TDS Rate is empty or null" + "\n";
									}
									if (derivedTdsAmountFromExcel == null) {
										message = message + " Derived TDS Amount is empty or null" + "\n";
									}

								}
								errorMessages.add(message);

							} else if ("Modify - Taxable value".equalsIgnoreCase(userAction)
									|| "Modify - TDS base value".equalsIgnoreCase(userAction)
									|| "Modify - Any other amount".equalsIgnoreCase(userAction)) {
								if (finalTdsSectionFromExcel != null && finalTdsRateFromExcel != null) {
									advanceData.setReason(reason);
									advanceData.setActive(true);
									advanceData.setMismatch(false);
									advanceData.setFinalTdsRate(finalTdsRateFromExcel);
									advanceData.setFinalTdsSection(finalTdsSectionFromExcel);
									if (finalTdsAmountFromExcel != null) {
										advanceData.setFinalTdsAmount(finalTdsAmountFromExcel);
									} else if ("Modify - TDS base value".equalsIgnoreCase(userAction)) {
										advanceData.setClientTaxableAmount(taxableAmount);
										advanceData.setAmount(tdsBaseValue);
										finalAmount = finalAmount.add(advanceData.getFinalTdsRate()
												.multiply(tdsBaseValue).divide(BigDecimal.valueOf(100)));
										advanceData.setFinalTdsAmount(finalAmount);
									} else if ("Modify - Taxable value".equalsIgnoreCase(userAction)) {
										finalAmount = finalAmount.add(advanceData.getFinalTdsRate()
												.multiply(taxableAmount).divide(BigDecimal.valueOf(100)));
										advanceData.setFinalTdsAmount(finalAmount);
									} else if ("Modify - Any other amount".equalsIgnoreCase(userAction)) {
										advanceData.setClientTaxableAmount(taxableAmount);
										advanceData.setAmount(anyOtherAmount);
										finalAmount = finalAmount.add(advanceData.getFinalTdsRate()
												.multiply(anyOtherAmount).divide(BigDecimal.valueOf(100)));
										advanceData.setFinalTdsAmount(finalAmount);
									}
									if ("IMPG".equalsIgnoreCase(supplyType)) {
										if ("194Q".equalsIgnoreCase(advanceData.getFinalTdsSection())
												|| "NOTDS".equalsIgnoreCase(advanceData.getFinalTdsSection())
												|| StringUtils.isBlank(advanceData.getFinalTdsSection())) {
											advanceData.setIsExempted(true);
											advanceData.setErrorReason("Transaction out of Scope - Import of goods");
										}
									} else if (advanceData.getDeducteePan()
											.equalsIgnoreCase(advanceData.getDeductorPan())) {
										if ("NOTDS".equalsIgnoreCase(advanceData.getFinalTdsSection())
												|| StringUtils.isBlank(advanceData.getFinalTdsSection())) {
											advanceData.setIsExempted(true);
											advanceData.setErrorReason("Transaction out of Scope - Stock Transfer");
										}
									} else if ("DLC".equalsIgnoreCase(documentType)) {
										if ("NOTDS".equalsIgnoreCase(advanceData.getFinalTdsSection())
												|| StringUtils.isBlank(advanceData.getFinalTdsSection())) {
											advanceData.setIsExempted(true);
											advanceData.setErrorReason("Transaction out of Scope - Delivery Challan");
										}
									} else if ("NOTDS".equalsIgnoreCase(advanceData.getFinalTdsSection())
											|| StringUtils.isBlank(advanceData.getFinalTdsSection())) {
										advanceData.setIsExempted(true);
										advanceData.setErrorReason("Transaction out of Scope - Scrape Sale");
									}
								} else {
									isError = true;
									errorCount++;
									errorList.add(row);
									String message = StringUtils.EMPTY;
									if (StringUtils.isEmpty(finalTdsSectionFromExcel)) {
										message = " Final TDS Section is empty or null" + "\n";
									}
									if (finalTdsRateFromExcel == null) {
										message = message + " Final TDS Rate is empty or null" + "\n";
									}
									errorMessages.add(message);
								}
							} else if ("Reject".equalsIgnoreCase(userAction)) {
								String message = StringUtils.EMPTY;
								if ((StringUtils.isBlank(actualTdsSectionFromExcel) || actualTdsRateFromExcel == null)
										&& (actualTdsAmountFromExcel != null
												&& actualTdsAmountFromExcel.compareTo(BigDecimal.ZERO) == 1)) {
									isError = true;
									errorCount++;
									errorList.add(row);
									if (StringUtils.isBlank(actualTdsSectionFromExcel)) {
										message = message
												+ "Client Amount Is Greater Than Zero While Client Section Is Empty"
												+ "\n";
									}
									if (actualTdsRateFromExcel == null) {
										message = message
												+ "Client Amount Is Greater Than Zero While Client Rate Is Empty"
												+ "\n";
									}
								}
								if (actualTdsSectionFromExcel != null && actualTdsRateFromExcel != null
										&& actualTdsAmountFromExcel != null) {
									advanceData.setFinalTdsSection(actualTdsSectionFromExcel);
									advanceData.setFinalTdsRate(actualTdsRateFromExcel);
									advanceData.setFinalTdsAmount(actualTdsAmountFromExcel);
									advanceData.setActive(true);
									advanceData.setMismatch(false);
									if ("IMPG".equalsIgnoreCase(supplyType)) {
										if ("194Q".equalsIgnoreCase(advanceData.getFinalTdsSection())
												|| "NOTDS".equalsIgnoreCase(advanceData.getFinalTdsSection())
												|| StringUtils.isBlank(advanceData.getFinalTdsSection())) {
											advanceData.setIsExempted(true);
											advanceData.setErrorReason("Transaction out of Scope - Import of goods");
										}
									} else if (advanceData.getDeducteePan()
											.equalsIgnoreCase(advanceData.getDeductorPan())) {
										if ("NOTDS".equalsIgnoreCase(advanceData.getFinalTdsSection())
												|| StringUtils.isBlank(advanceData.getFinalTdsSection())) {
											advanceData.setIsExempted(true);
											advanceData.setErrorReason("Transaction out of Scope - Stock Transfer");
										}
									} else if ("DLC".equalsIgnoreCase(documentType)) {
										if ("NOTDS".equalsIgnoreCase(advanceData.getFinalTdsSection())
												|| StringUtils.isBlank(advanceData.getFinalTdsSection())) {
											advanceData.setIsExempted(true);
											advanceData.setErrorReason("Transaction out of Scope - Delivery Challan");
										}
									} else if ("NOTDS".equalsIgnoreCase(advanceData.getFinalTdsSection())
											|| StringUtils.isBlank(advanceData.getFinalTdsSection())) {
										advanceData.setIsExempted(true);
										advanceData.setErrorReason("Transaction out of Scope - Scrape Sale");
									}
								} else {
									isError = true;
									errorCount++;
									errorList.add(row);
									if (StringUtils.isEmpty(actualTdsSectionFromExcel)) {
										message = " Actual TDS Section is empty or null" + "\n";
									}
									if (actualTdsRateFromExcel == null) {
										message = message + " Actual TDS Rate is empty or null" + "\n";
									}
									if (actualTdsAmountFromExcel == null) {
										message = message + " Actual TDS Amount is empty or null" + "\n";
									}
									errorMessages.add(message);
								}
								errorMessages.add(message);
							} else if ("Cancel".equalsIgnoreCase(userAction)) {
								advanceData.setActive(false);
								advanceData.setMismatch(false);
								advanceData.setReason("USER REQUESTED TO CANCEL");
								advanceData.setErrorReason("Canceled record-ERR029");
								isCancel = true;
								advanceIds.add(advanceId);
							} else {
								errorCount++;
								errorList.add(row);
								isError = true;
							}

							if (!"Cancel".equalsIgnoreCase(userAction) && !"Accept".equalsIgnoreCase(userAction)) {
								if (ratesMap != null && ratesMap.get(advanceData.getFinalTdsSection()) != null) {
									Double closestRate = closest(advanceData.getFinalTdsRate().doubleValue(),
											ratesMap.get(advanceData.getFinalTdsSection()));
									BigInteger nopIdInt = sectionRateNopId
											.get(advanceData.getFinalTdsSection() + "-" + closestRate);
									nopId = nopIdInt.compareTo(BigInteger.ZERO) > 0 ? nopIdInt.intValue() : 0;
								}
								if (nopId != null) {
									advanceData.setAdvanceNpId(nopId);
									nopGroupId = nopGroupMap.get(nopId);
								}

								if (nopGroupId != null) {
									advanceData.setAdvanceGroupid(nopGroupId);
								}
							}

							if (!isError) {
								if (UploadTypes.ADVANCE_NR_EXCEL.name().equalsIgnoreCase(advanceData.getProcessedFrom())
										&& !"Cancel".equalsIgnoreCase(userAction)
										&& !"ACCEPT".equalsIgnoreCase(userAction) && advanceData.getHasDtaa() != null
										&& advanceData.getHasDtaa().equals(false)) {
									advanceData.setSurcharge(surchargeCalculation(advanceData.getFinalTdsSection(),
											advanceData.getFinalTdsAmount(), advanceData.getAmount(), deducteeStatus,
											surchargeMap));
									cessAmount = advanceData.getSurcharge().add(advanceData.getFinalTdsAmount());
									advanceData.setCessAmount(
											cessAmount.multiply(cessRate).divide(BigDecimal.valueOf(100)));
								}
								advanceList.add(advanceData);
								processedCount++;
							}
						} else {
							errorList.add(row);
							errorMessages.add("invoice mismatch id not found in system");
							errorCount++;
						}
					}
				}
			}

			if (!ldcUtilizationList.isEmpty()) {
				lDCUtilizationDAO.batchUploadldcUtilizationSave(ldcUtilizationList, tenantId);
			}
			// batch update advance mismatch recodes
			if (!advanceList.isEmpty()) {
				advanceDAO.batchUpdateAdvacneMismatch(advanceList);
			}

			if (isCancel) {
				MultipartFile cancelledAdvancesFile = generateCancelledAdvanceExcell(advanceIds, tan, deductorPan);
				otherFilePath = blob.uploadExcelToBlob(cancelledAdvancesFile);
			}

			if (errorCount > 0 && !errorList.isEmpty()) {
				ByteArrayOutputStream bytes = generateMismatchErrorReport(errorList, tan, tenantId, deductorPan,
						errorMessages);
				if (bytes.size() != 0) {
					errorFilepath = sendFileToBlobStorage(bytes, tenantId);
				}
			}
			if (batchUpload.getBatchUploadID() != null) {
				batchUpload.setFilePath(batchUpload.getFilePath());
				batchUpload.setFailedCount(Long.valueOf(errorCount));
				batchUpload.setProcessedCount(processedCount);
				batchUpload.setStatus("Processed");
				batchUpload.setErrorFilePath(errorFilepath);
				batchUpload.setRowsCount((long) processedCount + errorCount);
				batchUpload.setOtherFileUrl(otherFilePath);
				batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
				batchUploadDAO.update(batchUpload);
			}
		} catch (Exception e1) {
			logger.error("Exception occurred while updating advance remediation report {}", e1.getMessage());

		}
		return batchUpload;
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

	private String sendFileToBlobStorage(ByteArrayOutputStream out, String tenantId)
			throws IOException, URISyntaxException, InvalidKeyException, StorageException {
		File file = getConvertedExcelFile(out);
		logger.info("Original file   : {}", file.getName());
		String paths = blob.uploadExcelToBlobWithFile(file, tenantId);
		return paths;
	}

	private File getConvertedExcelFile(ByteArrayOutputStream out) throws FileNotFoundException, IOException {
		byte[] bytes = out.toByteArray();
		File someFile = new File("Advance_RemediationError_Report.xlsx");
		try (FileOutputStream fos = new FileOutputStream(someFile)) {
			fos.write(bytes);
			fos.flush();
			fos.close();
			return someFile;
		}
	}

	private ByteArrayOutputStream generateMismatchErrorReport(List<CsvRow> errorList, String tan, String tenantId,
			String deductorPan, List<String> error) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = resourceLoader.getResource("classpath:templates/" + "Mismatch Error Template.xlsx");
		InputStream input = resource.getInputStream();
		try (SXSSFWorkbook wb = new SXSSFWorkbook(new XSSFWorkbook(input))) {
			SXSSFSheet sheet = wb.getSheetAt(0);
			String msg = getErrorReportMsg(tan, tenantId, deductorPan).replace("Report", "Error Report");

			XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
			style.setWrapText(true);
			XSSFCellStyle styleUnlocked = (XSSFCellStyle) wb.createCellStyle();
			styleUnlocked.setLocked(false);
			XSSFFont fonts = (XSSFFont) wb.createFont();
			fonts.setBold(false);
			style.setFont(fonts);
			XSSFSheet xssfSheet = wb.getXSSFWorkbook().getSheetAt(0);

			XSSFCell cell = xssfSheet.getRow(0).createCell(0);
			cell.setCellValue(msg);
			cell.setCellStyle(style);

			int rowindex = 5;
			for (CsvRow row : errorList) {

				SXSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * xssfSheet.getDefaultRowHeightInPoints()));

				createSXSSFCell(style, row1, 0, row.getField("Sequence Number"));
				createSXSSFCell(style, row1, 1, tan);
				createSXSSFCell(style, row1, 2, row.getField("Error Message"));
				createSXSSFCell(style, row1, 3, row.getField("DeductorCode"));
				createSXSSFCell(style, row1, 4, row.getField("DeductorTAN"));
				createSXSSFCell(style, row1, 5, row.getField("DeductorGSTIN"));
				createSXSSFCell(style, row1, 6, row.getField("DeducteeCode"));
				createSXSSFCell(style, row1, 7, row.getField("DeducteeName"));
				createSXSSFCell(style, row1, 8, row.getField("DeducteePAN"));
				createSXSSFCell(style, row1, 9, row.getField("PAN Validation Status"));
				createSXSSFCell(style, row1, 10, row.getField("DeducteeGSTIN"));
				createSXSSFCell(style, row1, 11, row.getField("DeducteeStatus"));
				createSXSSFCell(style, row1, 12, row.getField("Deductee Aadhaar"));
				createSXSSFCell(style, row1, 13, row.getField("TDSApplicabilityin194QvsTDSothersections"));
				createSXSSFCell(style, row1, 14, row.getField("Is LDC applicable?"));
				createSXSSFCell(style, row1, 15, row.getField("Declaration Module - Rate Type"));// TDS Applicability
																									// (with/without
																									// PAN/ITR)
				createSXSSFCell(style, row1, 16, "Number of sections");
				createSXSSFCell(style, row1, 17, "Vendor section");
				createSXSSFCell(style, row1, 18, "Ldc sections");
				createSXSSFCell(style, row1, 19, "VendorInvoiceNumber");// VendorInvoiceNumber
				createSXSSFCell(style, row1, 20, row.getField("DocumentDate"));
				createSXSSFCell(style, row1, 21, row.getField("ERPDocumentNumber"));
				createSXSSFCell(style, row1, 22, row.getField("PostingDate"));
				createSXSSFCell(style, row1, 23, row.getField("DocumentType"));
				createSXSSFCell(style, row1, 24, row.getField("SupplyType"));
				createSXSSFCell(style, row1, 25, row.getField("ERPDocumentType"));
				createSXSSFCell(style, row1, 26, row.getField("LineItemNumber"));
				createSXSSFCell(style, row1, 27, row.getField("OriginalDocumentNumber"));
				createSXSSFCell(style, row1, 28, "OriginalDocumentDate");// OriginalDocumentDate
				createSXSSFCell(style, row1, 29, row.getField("HSNorSAC"));
				createSXSSFCell(style, row1, 30, row.getField("HSNorSACDesc"));
				createSXSSFCell(style, row1, 31, row.getField("InvoiceDesc"));
				createSXSSFCell(style, row1, 32, row.getField("GLAccountCode"));
				createSXSSFCell(style, row1, 33, row.getField("GLAccountName"));// GLAccountName
				createSXSSFCell(style, row1, 34, row.getField("PONumber"));
				createSXSSFCell(style, row1, 35, row.getField("POItemNo"));
				createSXSSFCell(style, row1, 36, row.getField("PODate"));
				createSXSSFCell(style, row1, 37, row.getField("PODesc"));
				createSXSSFCell(style, row1, 38, row.getField("NRIndicator"));
				createSXSSFCell(style, row1, 39, row.getField("DebitCreditIndicator"));
				createSXSSFCell(style, row1, 40, row.getField("TaxableValue"));
				createSXSSFCell(style, row1, 41, "TaxableValue");// InvoiceValue
				createSXSSFCell(style, row1, 42, row.getField("TDSBaseValue"));
				createSXSSFCell(style, row1, 43, row.getField("TDSTaxCodeERP"));
				createSXSSFCell(style, row1, 44, row.getField("TDSSection"));
				createSXSSFCell(style, row1, 45, row.getField("TDSRate"));
				createSXSSFCell(style, row1, 46, row.getField("TDSAmount"));
				createSXSSFCell(style, row1, 47, row.getField("Client Effective TDS Rate"));
				createSXSSFCell(style, row1, 48, row.getField("Derived TDS Section"));
				createSXSSFCell(style, row1, 49, row.getField("Derived TDS Rate"));
				createSXSSFCell(style, row1, 50, row.getField("Derived TDS Amount"));
				createSXSSFCell(style, row1, 51, row.getField("Section"));
				createSXSSFCell(style, row1, 52, row.getField("Rate"));
				createSXSSFCell(style, row1, 53, row.getField("Mismatch Type"));
				createSXSSFCell(style, row1, 54, row.getField("Confidence Index"));
				createSXSSFCell(style, row1, 55, row.getField(""));// Derived TDS Section-Vendor Master
				createSXSSFCell(style, row1, 56, row.getField(""));// Derived TDS Section-HSN/SAC
				createSXSSFCell(style, row1, 57, row.getField(""));// Derived TDS Section-PO desc
				createSXSSFCell(style, row1, 58, row.getField(""));// Derived TDS Section-INV des
				createSXSSFCell(style, row1, 59, row.getField(""));// Derived TDS Section-GL desc
				createSXSSFCell(styleUnlocked, row1, 60, row.getField("Action"));// Action
				createSXSSFCell(styleUnlocked, row1, 61, row.getField("Reason"));// Reason
				createSXSSFCell(styleUnlocked, row1, 62, row.getField("Final TDS Section"));// Final TDS Section
				createSXSSFCell(styleUnlocked, row1, 63, row.getField("Final TDS Rate"));// Final TDS Rate
				createSXSSFCell(styleUnlocked, row1, 64, row.getField("Final TDS Rate"));// Final TDS Amount
				createSXSSFCell(styleUnlocked, row1, 65, row.getField("Any Other Amount"));// Any Other Amount
				createSXSSFCell(styleUnlocked, row1, 66, row.getField("Deductor TAN"));// Deductor TAN
				createSXSSFCell(style, row1, 67, row.getField("DeductorName"));
				createSXSSFCell(style, row1, 68, row.getField("DeductorPAN"));
				createSXSSFCell(style, row1, 69, row.getField("BusinessPlace"));
				createSXSSFCell(style, row1, 70, row.getField("BusinessArea"));
				createSXSSFCell(style, row1, 71, row.getField("Plant"));
				createSXSSFCell(style, row1, 72, row.getField("ProfitCenter"));
				createSXSSFCell(style, row1, 73, row.getField("AssignmentNumber"));
				createSXSSFCell(style, row1, 74, row.getField("UserName"));
				createSXSSFCell(style, row1, 75, row.getField("PaymentDate"));
				createSXSSFCell(style, row1, 76, row.getField("TDSDeductionDate"));
				createSXSSFCell(style, row1, 77, row.getField("MIGONumber"));// MIGONumber
				createSXSSFCell(style, row1, 78, row.getField("MIRONumber"));// MIRONumber
				createSXSSFCell(style, row1, 79, row.getField("IGSTRate"));// IGSTRate
				createSXSSFCell(style, row1, 80, row.getField("IGSTAmount"));// IGSTAmount
				createSXSSFCell(style, row1, 81, row.getField("CGSTRate"));// CGSTRate
				createSXSSFCell(style, row1, 82, row.getField("CGSTAmount"));// CGSTAmount
				createSXSSFCell(style, row1, 83, row.getField("SGSTRate"));// SGSTRate
				createSXSSFCell(style, row1, 84, row.getField("SGSTAmount"));// SGSTAmount
				createSXSSFCell(style, row1, 85, getFormattedValue(row.getField("CESSRate")));
				createSXSSFCell(style, row1, 86, getFormattedValue(row.getField("CESSAmount")));
				createSXSSFCell(style, row1, 87, row.getField("POS"));// POS
				createSXSSFCell(style, row1, 88, row.getField("LinkedAdvanceIndicator"));// LinkedAdvanceIndicator
				createSXSSFCell(style, row1, 89, row.getField("LinkedProvisionIndicator"));// LinkedProvisionIndicator
				createSXSSFCell(style, row1, 90, row.getField("ProvisionAdjustmentFlag"));// ProvisionAdjustmentFlag
				createSXSSFCell(style, row1, 91, row.getField("AdvanceAdjustmentFlag"));// AdvanceAdjustmentFlag
				createSXSSFCell(style, row1, 92, row.getField("ChallanPaidFlag"));// ChallanPaidFlag
				createSXSSFCell(style, row1, 93, row.getField("ChallanProcessingDate"));// ChallanProcessingDate
				createSXSSFCell(style, row1, 94, row.getField("GrossUpIndicator"));
				createSXSSFCell(style, row1, 95, row.getField("AmountForeignCurrency"));
				createSXSSFCell(style, row1, 96, row.getField("ExchangeRate"));
				createSXSSFCell(style, row1, 97, row.getField("Currency"));
				createSXSSFCell(style, row1, 98, row.getField("ItemCode"));// ItemCode
				createSXSSFCell(style, row1, 99, row.getField("TDSremittancedate"));
				createSXSSFCell(style, row1, 100, row.getField("GR/IRIndicator"));
				createSXSSFCell(style, row1, 101, row.getField("TypeOfTransaction"));// TypeOfTransaction
				createSXSSFCell(style, row1, 102, row.getField("SAAnumber"));// SAAnumber
				createSXSSFCell(style, row1, 103, row.getField("RefKey3"));// RefKey3
				createSXSSFCell(style, row1, 104, row.getField("UserDefinedField1"));
				createSXSSFCell(style, row1, 105, row.getField("UserDefinedField2"));
				createSXSSFCell(style, row1, 106, row.getField("UserDefinedField3"));
				createSXSSFCell(style, row1, 107, row.getField("UserDefinedField4"));
				createSXSSFCell(style, row1, 108, row.getField("UserDefinedField5"));
				createSXSSFCell(style, row1, 109, row.getField("UserDefinedField6"));
				createSXSSFCell(style, row1, 110, row.getField("UserDefinedField7"));
				createSXSSFCell(style, row1, 111, row.getField("UserDefinedField8"));
				createSXSSFCell(style, row1, 112, row.getField("UserDefinedField9"));
				createSXSSFCell(style, row1, 113, row.getField("UserDefinedField10"));
				createSXSSFCell(style, row1, 114, row.getField("SourceIdentifier"));
				createSXSSFCell(style, row1, 115, row.getField("SourceFileName"));

			}

			wb.write(out);

		} catch (Exception e) {
			logger.info("Exception occured while preparing error file " + e.getMessage() + "{}");
		}
		return out;
	}

	/**
	 * calculating advance matrix values
	 * 
	 * @param deductorTan
	 * @return
	 */
	public Map<Integer, Object> getAdvanceMatrix(String deductorTan, int yearFromUI, String type) {

		Map<Integer, Object> advanceMatrixValues = new HashMap<>();

		List<MatrixDTO> matrixValues = advanceDAO.getAdvanceMatrix(deductorTan, yearFromUI, type);

		for (MatrixDTO matrixDTO : matrixValues) {
			advanceMatrixValues.put(matrixDTO.getMonth(), matrixDTO);
		}

		/*
		 * BigDecimal sumOfAdvanceOpeningAmount = new BigDecimal(
		 * advanceRepository.getTotalAdvanceAmount(yearFromUI, deductorTan));
		 * 
		 * BigDecimal sumOfAdvanceUtilizationAmount = new BigDecimal(
		 * advanceUtilizationRepository.getTotalAdvanceUtilizedAmount(yearFromUI,
		 * deductorTan));
		 * 
		 * BigDecimal finalOpeningAmount =
		 * sumOfAdvanceOpeningAmount.subtract(sumOfAdvanceUtilizationAmount);
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

	public Map<Integer, Object> getAdvanceMatrixByDeducteePan(String deductorTan, int yearFromUI, String deducteePan) {

		Map<Integer, Object> advanceMatrixValues = new HashMap<>();

		BigDecimal finalOpeningAmount = null;

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

		BigDecimal sumOfAdvanceFTMs = null;// TODO NEED TO DELETE AFTER 1 WEEK
		// new BigDecimal(advanceRepository
		// .getTotalAdvanceAmountByMonthByDeducteePan(deductorTan, assessmentYear,
		// month, deducteePan));

		// list of Adjustment amount

		BigDecimal sumOfAdvanceAdjustmentAmount = null; // TODO NEED TO DELETE AFTER 1 WEEK
		// new BigDecimal(advanceUtilizationRepository
		// .getTotalAdvanceUtilizedAmountByMonthAndPan(assessmentYear, month,
		// deductorTan, deducteePan));
		BigDecimal closingAmount = (finalOpeningAmount.add(sumOfAdvanceFTMs)).subtract(sumOfAdvanceAdjustmentAmount);
		MatrixDTO matrixDTO = new MatrixDTO();
		matrixDTO.setOpeningAmount(finalOpeningAmount.setScale(2, RoundingMode.UP));
		matrixDTO.setFtm(sumOfAdvanceFTMs.setScale(2, RoundingMode.UP));
		matrixDTO.setAdjustmentAmount(sumOfAdvanceAdjustmentAmount.setScale(2, RoundingMode.UP));
		matrixDTO.setClosingAmount(closingAmount.setScale(2, RoundingMode.UP));
		advanceMatrixValues.put(month, matrixDTO);
		finalOpeningAmount = closingAmount;
		return finalOpeningAmount;
	}

	@Async
	public ByteArrayInputStream asyncAdvanceMatrixOpeningAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, boolean isOpening, String tenantId, String userName, String type, Integer batchUploadYear)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return advanceMatrixClosingAmountDownload(deductorTan, assessmentYear, month, isOpening, tenantId, userName,
				type, batchUploadYear);
	}

	@Async
	public ByteArrayInputStream asyncAdvanceMatrixClosingAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, boolean isOpening, String tenantId, String userName, String type) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return advanceMatrixClosingAmountDownload(deductorTan, assessmentYear, month, isOpening, tenantId, userName,
				type, assessmentYear);
	}

	public ByteArrayInputStream advanceMatrixClosingAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, boolean isOpening, String tenantId, String userName, String type, Integer batchUploadYear)
			throws Exception {
		logger.info("Closing advance amount download request for Year : {} Month : {}", assessmentYear, month);
		String fileName = "";
		if (UploadTypes.ADVANCE_CLOSING_REPORT.name().equalsIgnoreCase(type)) {
			fileName = "YTD_Advance_Closing_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		} else {
			fileName = "YTD_Advance_Opening_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, batchUploadYear, null, 0L, type, "Processing",
				month, userName, null, fileName);
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Advance Matrix");

		String[] headerNames = new String[] { "Deductor Tan", "Opening Amount", "Adjusted Amount", "Closing Amount",
				"Deductee Pan", "Deductee Name", "Posting Date", "TDS Amount", "Closing TDS Amount", "Document Number",
				"Final Tds Section", "Final Tds Rate" };
		if (isOpening) {
			headerNames = new String[] { "Deductor Tan", "Opening Amount", "Deductee Pan", "Deductee Name",
					"Posting Date", "TDS Amount", "Opening TDS Amount", "Document Number", "Final Tds Section",
					"Final Tds Rate" };
		}

		List<MatrixDTO> matrixValues = advanceDAO.getAdvanceClosingMatrixReport(deductorTan, assessmentYear, month);

		worksheet.getCells().importArray(headerNames, 0, 0, false);
		// list of advance ftm amounts
		int rowIndex = 1;
		BigDecimal ftmSum = BigDecimal.valueOf(0);
		BigDecimal closingSum = BigDecimal.valueOf(0);
		BigDecimal adjustedSum = BigDecimal.valueOf(0);
		BigDecimal tdsAmountSum = BigDecimal.valueOf(0);
		BigDecimal openingTdsAmountSum = BigDecimal.valueOf(0);
		BigDecimal closingtTdsAmountSum = BigDecimal.valueOf(0);

		for (MatrixDTO matrixDTO : matrixValues) {

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

			if (!isOpening) {
				rowData.add((matrixDTO.getClosingAmount().multiply(matrixDTO.getFinalTdsRate()))
						.divide(BigDecimal.valueOf(100)));
				closingtTdsAmountSum = closingtTdsAmountSum
						.add((matrixDTO.getClosingAmount().multiply(matrixDTO.getFinalTdsRate()))
								.divide(BigDecimal.valueOf(100)));
			} else {
				rowData.add((matrixDTO.getOpeningAmount().multiply(matrixDTO.getFinalTdsRate()))
						.divide(BigDecimal.valueOf(100)));
				openingTdsAmountSum = openingTdsAmountSum
						.add((matrixDTO.getOpeningAmount().multiply(matrixDTO.getFinalTdsRate()))
								.divide(BigDecimal.valueOf(100)));
			}
			rowData.add(matrixDTO.getDocumentNumber() == null ? StringUtils.EMPTY : matrixDTO.getDocumentNumber());
			rowData.add(matrixDTO.getFinalTdsSection() == null ? StringUtils.EMPTY : matrixDTO.getFinalTdsSection());
			rowData.add(matrixDTO.getFinalTdsRate() == null ? 0 : matrixDTO.getFinalTdsRate());

			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);

			if (!isOpening) {
				ftmSum = ftmSum.add(matrixDTO.getOpeningAmount());
				adjustedSum = adjustedSum.add(matrixDTO.getAdjustmentAmount());
			}

			tdsAmountSum = tdsAmountSum.add(matrixDTO.getTdsAmount());
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
		rowData.add(tdsAmountSum);
		worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();

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

		// Style for E1 to J1 headers
		Style style5 = workbook.createStyle();
		style5.setForegroundColor(Color.fromArgb(255, 255, 0));
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(true);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("E1:J1");
		headerColorRange1.setStyle(style5);

		if (!isOpening) {
			Cell a11 = worksheet.getCells().get("K1");
			a11.setStyle(style5);

			Cell a12 = worksheet.getCells().get("L1");
			a12.setStyle(style5);
		}

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		if (isOpening) {
			autoFilter.setRange("A1:J1");
		} else {
			autoFilter.setRange("A1:L1");
		}

		workbook.save(out, SaveFormat.XLSX);
		saveMatrixReport(deductorTan, tenantId, batchUploadYear, out, 1L, type, "Processed", month, userName,
				batchUpload.getBatchUploadID(), null);
		return new ByteArrayInputStream(out.toByteArray());
	}

	@Async
	public ByteArrayInputStream asyncAdvanceMatrixFtmDownload(String deductorTan, Integer assessmentYear, Integer month,
			String tenantId, String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return advanceMatrixFtmDownload(deductorTan, assessmentYear, month, tenantId, userName);
	}

	public ByteArrayInputStream advanceMatrixFtmDownload(String deductorTan, int assessmentYear, int month,
			String tenantId, String userName) throws Exception {

		logger.info("Advance matrix ftm download request for Year : {} Month : {}", assessmentYear, month);
		String fileName = "FTM_Advance_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, assessmentYear, null, 0L,
				UploadTypes.ADVANCE_FTM_REPORT.name(), "Processing", month, userName, null, fileName);

		// list of advance ftm amounts
		List<AdvanceDTO> advanceFTMs = new ArrayList<>();

		List<AdvanceDTO> advances = advanceDAO.getAdvanceFTM(deductorTan, assessmentYear, month);
		advanceFTMs.addAll(advances);

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Advance Matrix FTM");
		ArrayList<MatrixFileDTO> matrixFileDTOList = new ArrayList<>();

		for (AdvanceDTO advance : advanceFTMs) {
			matrixFileDTOList.add(new MatrixFileDTO(advance.getDeductorPan(), advance.getDeductorMasterTan(),
					advance.getDeductorGstin(), advance.getDeducteePan(), advance.getDeducteeTin(),
					advance.getDeducteeGstin(), advance.getAmount(), new BigDecimal(0), advance.getFinalTdsAmount()));

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

		Cell a9 = worksheet.getCells().get("I1");
		Style style9 = a9.getStyle();
		style9.setForegroundColor(Color.fromArgb(255, 255, 0));
		style9.setPattern(BackgroundType.SOLID);
		style9.getFont().setBold(true);
		a9.setStyle(style9);

		if (matrixFileDTOList.isEmpty()) {
			a1.putValue("Deductor PAN");
			a2.putValue("Deductor TAN");
			a3.putValue("Deductor GSTIN");
			a4.putValue("Deductee PAN");
			a5.putValue("Deductee TIN");
			a6.putValue("Deductee GSTIN");
			a7.putValue("Amount");
			a8.putValue("Utilization Amount");
			a9.putValue("Tds Amount");
			// Insert data to excel template
			worksheet.getCells().importCustomObjects(matrixFileDTOList, 0, 0, tableOptions);
		}

		worksheet.autoFitColumns();
		worksheet.setGridlinesVisible(false);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String cellname = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);
		Range range;
		if (!cellname.equalsIgnoreCase("I1")) {
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
		autoFilter.setRange("A1:I1");

		workbook.save(out, SaveFormat.XLSX);
		saveMatrixReport(deductorTan, tenantId, assessmentYear, out, 1L, UploadTypes.ADVANCE_FTM_REPORT.name(),
				"Processed", month, userName, batchUpload.getBatchUploadID(), null);
		return new ByteArrayInputStream(out.toByteArray());
	}

	@Async
	public ByteArrayInputStream asyncAdvanceMatrixAdjustedFileDownload(String deductorTan, Integer assessmentYear,
			Integer month, String tenantId, String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return advanceMatrixAdjustedFileDownload(deductorTan, assessmentYear, month, tenantId, userName);
	}

	public ByteArrayInputStream advanceMatrixAdjustedFileDownload(String deductorTan, int assessmentYear, int month,
			String tenantId, String userName) throws Exception {
		logger.info("Advance matrix adjusted download request for Year : {} Month : {}", assessmentYear, month);
		String fileName = "Adjusted_Advance_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, assessmentYear, null, 0L,
				UploadTypes.ADVANCE_ADJUSTED_REPORT.name(), "Processing", month, userName, null, fileName);

		// list of advance ftm amounts
		List<AdvanceUtilizationDTO> advanceAdjustments = new ArrayList<>();
		List<AdvanceUtilizationDTO> advanceUtilizations = advanceUtilizationDAO.getAdvanceAdjustment(deductorTan,
				assessmentYear, month);

		/*
		 * for(AdvanceUtilizationDTO advanceUtilizationDTO :advanceUtilizations) {
		 * lineItemIds.add(advanceUtilizationDTO.getKey().getId()); }
		 * 
		 * // get the data based on invoice line item id. List<InvoiceLineItemDto>
		 * invoiceLineItem = invoiceLineItemDAO.findByLineItemIds(assessmentYear,
		 * deductorTan, lineItemIds);
		 */

		advanceAdjustments.addAll(advanceUtilizations);

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Advance Matrix Adjustsments");
		worksheet.freezePanes(0, 4, 0, 4);
		worksheet.autoFitColumns();
		worksheet.autoFitRows();

		ArrayList<UtilizationFileDTO> listutilizationFileDTO = new ArrayList<>();

		for (AdvanceUtilizationDTO advanceUtilization : advanceAdjustments) {

			UtilizationFileDTO utilizationFileDTO = new UtilizationFileDTO();

			// get the data based on invoice line item id.
			List<InvoiceLineItem> invoiceLineItem = invoiceLineItemDAO
					.findByOnlyId(advanceUtilization.getInvoiceLineItemId());

			utilizationFileDTO.setDeductorMasterTan(advanceUtilization.getDeductorMasterTan());
			utilizationFileDTO.setMasterPan(advanceUtilization.getAdvanceMasterPan());
			utilizationFileDTO.setRemainingAmount(advanceUtilization.getRemainingAmount());
			utilizationFileDTO.setUtilizedAmount(advanceUtilization.getUtilizedAmount());
			if (!invoiceLineItem.isEmpty()) {
				BeanUtils.copyProperties(invoiceLineItem.get(0), utilizationFileDTO);
			}

			listutilizationFileDTO.add(utilizationFileDTO);

		}
		String[] advanceMatrixheaderNames = new String[] { "Deductor Master Tan", "Advance Master Pan",
				"Remaining Amount", "Utilized Amount", "Source File Name", "Company Code", "Name Of The Company Code",
				"Deductor TAN", "Deductor GSTIN", "Deductee PAN", "Deductee TIN", "Name of the Deductee",
				"Deductee Address", "Vendor Invoice Number", "Miro Number", "Migo Number", "Document Type",
				"Document Date", "Posting Date of Document", "Line Item Number", "HSN/SAC", "Sac Description",
				"Service Description - Invoice", "Service Description - PO", "Service Description - GL Text",
				"Taxable value", "IGST Rate", "IGST Amount", "CGST Rate", "CGST Amount", "SGST Rate", "SGST Amount",
				"Cess Rate", "Cess Amount", "Creditable (Y/N)", "POS", "TDS Section", "TDS Rate", "TDS Amount",
				"PO number", "PO date", "Linked advance Number", "Grossing up Indicator", "Original Document Number",
				"Original Document Date", "User Defined Field 1", "User Defined Field 2", "User Defined Field 3",
				"Final Tds Amount", "Final Tds rate", "Final Tds section", "Actual Tds Amount", "Actual Tds Rate",
				"Actual Tds Section", "Derived Tds Amount", "Derived Tds Rate", "Derived Tds Section" };

		worksheet.getCells().importArray(advanceMatrixheaderNames, 0, 0, false);

		setAdvanceMatrixHeaders(listutilizationFileDTO, worksheet);

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
		saveMatrixReport(deductorTan, tenantId, assessmentYear, out, 1L, UploadTypes.ADVANCE_ADJUSTED_REPORT.name(),
				"Processed", month, userName, batchUpload.getBatchUploadID(), null);
		return new ByteArrayInputStream(out.toByteArray());
	}

	private void setAdvanceMatrixHeaders(ArrayList<UtilizationFileDTO> listutilizationFileDTO, Worksheet worksheet)
			throws Exception {

		if (!listutilizationFileDTO.isEmpty()) {
			int rowIndex = 1;
			for (UtilizationFileDTO utilizationFileDTO : listutilizationFileDTO) {
				List<Object> rowData = new ArrayList<>();
				setInvoiceDataForMatrixReports(utilizationFileDTO, rowData);
				worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
			}
		}
	}

	public void setInvoiceDataForMatrixReports(UtilizationFileDTO utilizationFileDTO, List<Object> rowData) {

		rowData.add(StringUtils.isBlank(utilizationFileDTO.getDeductorMasterTan()) ? StringUtils.EMPTY
				: utilizationFileDTO.getDeductorMasterTan());
		// Advance Master Pan
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getMasterPan()) ? StringUtils.EMPTY
				: utilizationFileDTO.getMasterPan());
		// Remaining Amount
		rowData.add(utilizationFileDTO.getRemainingAmount());
		// Utilized Amount
		rowData.add(utilizationFileDTO.getUtilizedAmount());
		// Source File Name
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getSourceFileName()) ? StringUtils.EMPTY
				: utilizationFileDTO.getSourceFileName());
		// Company Code
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getCompanyCode()) ? StringUtils.EMPTY
				: utilizationFileDTO.getCompanyCode());
		// Name Of The Company Code
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getCompanyCode()) ? StringUtils.EMPTY
				: utilizationFileDTO.getCompanyCode());
		// "Deductor TAN
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getDeductorMasterTan()) ? StringUtils.EMPTY
				: utilizationFileDTO.getDeductorMasterTan());
		// Deductor GSTIN
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getDeductorGstin()) ? StringUtils.EMPTY
				: utilizationFileDTO.getDeductorGstin());
		// Deductee PAN
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getPan()) ? StringUtils.EMPTY : utilizationFileDTO.getPan());
		// Deductee TIN
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getDeducteeTin()) ? StringUtils.EMPTY
				: utilizationFileDTO.getDeducteeTin());
		// Name of the Deductee
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getDeducteeName()) ? StringUtils.EMPTY
				: utilizationFileDTO.getDeducteeName());
		// "Deductee Address,
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getDeducteeAddress()) ? StringUtils.EMPTY
				: utilizationFileDTO.getDeducteeAddress());
		// Vendor Invoice Number
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getVendorInvoiceNumber()) ? StringUtils.EMPTY
				: utilizationFileDTO.getVendorInvoiceNumber());
		// Miro Number
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getMiroNumber()) ? StringUtils.EMPTY
				: utilizationFileDTO.getMiroNumber());
		// Migo Number
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getMigoNumber()) ? StringUtils.EMPTY
				: utilizationFileDTO.getMigoNumber());
		// Document Type
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getDocumentType()) ? StringUtils.EMPTY
				: utilizationFileDTO.getDocumentType());
		// "Document Date
		rowData.add(utilizationFileDTO.getDocumentDate() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getDocumentDate().toString());
		// Posting Date of Document
		rowData.add((utilizationFileDTO.getDocumentPostingDate() == null) ? StringUtils.EMPTY
				: utilizationFileDTO.getDocumentPostingDate().toString());
		// Line Item Number
		rowData.add(utilizationFileDTO.getLineItemNumber() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getLineItemNumber());
		// HSN/SAC
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getHsnSacCode()) ? StringUtils.EMPTY
				: utilizationFileDTO.getHsnSacCode());
		// Sac Description
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getSacDecription()) ? StringUtils.EMPTY
				: utilizationFileDTO.getSacDecription());
		// "Service Description - Invoice
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getServiceDescriptionInvoice()) ? StringUtils.EMPTY
				: utilizationFileDTO.getServiceDescriptionInvoice());
		// Service Description - PO
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getServiceDescriptionPo()) ? StringUtils.EMPTY
				: utilizationFileDTO.getServiceDescriptionPo());
		// Service Description - GL Text
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getServiceDescriptionGl()) ? StringUtils.EMPTY
				: utilizationFileDTO.getServiceDescriptionGl());
		// Taxable value
		rowData.add(utilizationFileDTO.getInvoiceAmount() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getInvoiceAmount());
		// IGST Rate
		rowData.add(utilizationFileDTO.getIgstRate() == null ? StringUtils.EMPTY : utilizationFileDTO.getIgstRate());
		// IGST Amount
		rowData.add(
				utilizationFileDTO.getIgstAmount() == null ? StringUtils.EMPTY : utilizationFileDTO.getIgstAmount());
		// CGST Rate
		rowData.add(utilizationFileDTO.getCgstRate() == null ? StringUtils.EMPTY : utilizationFileDTO.getCgstRate());
		// CGST Amount
		rowData.add(
				utilizationFileDTO.getCgstAmount() == null ? StringUtils.EMPTY : utilizationFileDTO.getCgstAmount());
		// SGST Rate
		rowData.add(utilizationFileDTO.getSgstRate() == null ? StringUtils.EMPTY : utilizationFileDTO.getSgstRate());
		// SGST Amount
		rowData.add(
				utilizationFileDTO.getSgstAmount() == null ? StringUtils.EMPTY : utilizationFileDTO.getSgstAmount());
		// "Cess Rate", "", "", "", "", "", "",
		// "", "PO date", "Linked advance Number", "Grossing up Indicator", "Original
		// Document Number",
		// "Original Document Date", "User Defined Field 1", "User Defined Field 2",
		// "User Defined Field 3",
		rowData.add(utilizationFileDTO.getCessRate() == null ? StringUtils.EMPTY : utilizationFileDTO.getCessRate());
		// Cess Amount
		rowData.add(
				utilizationFileDTO.getCessAmount() == null ? StringUtils.EMPTY : utilizationFileDTO.getCessAmount());
		// Creditable (Y/N)
		rowData.add(
				utilizationFileDTO.getCreditable() == null ? StringUtils.EMPTY : utilizationFileDTO.getCreditable());
		// POS
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getPos()) ? StringUtils.EMPTY : utilizationFileDTO.getPos());
		// TDS Section
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getTdsSection()) ? StringUtils.EMPTY
				: utilizationFileDTO.getTdsSection());
		// TDS Rate
		rowData.add(utilizationFileDTO.getTdsRate() == null ? StringUtils.EMPTY : utilizationFileDTO.getTdsRate());
		// TDS Amount
		rowData.add(utilizationFileDTO.getClientAmount() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getClientAmount());
		// PO number
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getPoNumber()) ? StringUtils.EMPTY
				: utilizationFileDTO.getPoNumber());
		rowData.add(
				utilizationFileDTO.getPoDate() == null ? StringUtils.EMPTY : utilizationFileDTO.getPoDate().toString());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getLinkedAdvanceNumber()) ? StringUtils.EMPTY
				: utilizationFileDTO.getLinkedAdvanceNumber());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getGrossIndicator()) ? StringUtils.EMPTY
				: utilizationFileDTO.getGrossIndicator());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getOriginalDocumentNumber()) ? StringUtils.EMPTY
				: utilizationFileDTO.getOriginalDocumentNumber());
		rowData.add(utilizationFileDTO.getOriginalDocumentDate() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getOriginalDocumentDate().toString());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getUserDefinedField1()) ? StringUtils.EMPTY
				: utilizationFileDTO.getUserDefinedField1());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getUserDefinedField2()) ? StringUtils.EMPTY
				: utilizationFileDTO.getUserDefinedField2());
		rowData.add(StringUtils.isBlank(utilizationFileDTO.getUserDefinedField3()) ? StringUtils.EMPTY
				: utilizationFileDTO.getUserDefinedField3());

		// Added 9 extra columns
		// Final Tds Amount
		rowData.add(utilizationFileDTO.getFinalTdsAmount() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getFinalTdsAmount());
		rowData.add(utilizationFileDTO.getFinalTdsRate() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getFinalTdsRate());
		rowData.add(utilizationFileDTO.getFinalTdsSection() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getFinalTdsSection());

		// Actual TDS Amunt
		rowData.add(utilizationFileDTO.getClientAmount() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getClientAmount());
		rowData.add(
				utilizationFileDTO.getClientRate() == null ? StringUtils.EMPTY : utilizationFileDTO.getClientRate());
		rowData.add(utilizationFileDTO.getClientSection() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getClientSection());

		// Derived Tds Amount
		rowData.add(utilizationFileDTO.getDerivedTdsAmount() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getDerivedTdsAmount());
		rowData.add(utilizationFileDTO.getDerivedTdsRate() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getDerivedTdsRate());
		rowData.add(utilizationFileDTO.getDerivedTdsSection() == null ? StringUtils.EMPTY
				: utilizationFileDTO.getDerivedTdsSection());

	}

	// Advance error Report generate
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public File convertAdvanceCsvToXlsx(File csvFile, String deductorTan, String tenantId, String deductorPan,
			Integer assesmentYear, Integer batchId) throws Exception {

		List<AdvanceDTO> errorRecords = null;
		Reader reader = null;
		CsvToBean<AdvancesErrorReportCsvDto> csvToBean = null;

		errorRecords = advanceDAO.getAdvanceErrorRecords(deductorTan, assesmentYear, batchId);
		if (errorRecords.isEmpty()) {
			reader = new FileReader(csvFile);
			csvToBean = new CsvToBeanBuilder(reader).withType(AdvancesErrorReportCsvDto.class)
					.withIgnoreLeadingWhiteSpace(true).build();
		}

		// Aspose method to convert
		Workbook workbook = advanceXlsxReport(csvToBean != null ? csvToBean.parse() : null, errorRecords, deductorTan,
				tenantId, deductorPan);

		// Write the output to the file
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxAdvanceFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()) + "_Error_Report",
				".xlsx");

		FileUtils.writeByteArrayToFile(xlsxAdvanceFile, baout.toByteArray());
		baout.close();
		return xlsxAdvanceFile;
	}

	public Workbook advanceXlsxReport(List<AdvancesErrorReportCsvDto> advanceErrorReportsCsvList,
			List<AdvanceDTO> errorRecords, String tan, String tenantId, String deductorPan) throws Exception {

		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		worksheet.getCells().importArray(ErrorReportService.advanceHeaderNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (errorRecords.isEmpty()) {
			setExtractDataForAdvance(advanceErrorReportsCsvList, worksheet);
		} else {
			setExtractDataForAdvanceFromDB(errorRecords, worksheet, tan, deductorData.getDeductorName());
		}

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

		// Style for D6 to BK6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:BK6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("Advance Error Report (Dated: " + date + ")");
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
		String lastHeaderCellName = "BK6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:BK6");

		return workbook;
	}

	private void setExtractDataForAdvance(List<AdvancesErrorReportCsvDto> advanceErrorReportsCsvList,
			Worksheet worksheet) throws Exception {

		int rowIndex = 6;
		List<ErrorCode> errorCodesObjs = mastersClient.getAllErrorCodes().getBody().getData();
		// errorCodeService.findAll();
		Map<String, String> errorDescription = new HashMap<>();
		for (ErrorCode errorCodesObj : errorCodesObjs) {
			errorDescription.put(errorCodesObj.getCode(), errorCodesObj.getDescription());
		}
		for (AdvancesErrorReportCsvDto advanceErrorReport : advanceErrorReportsCsvList) {
			ArrayList<String> rowData = new ArrayList<String>();
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
								givenCodes.add(e.trim());
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
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeductorTan()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeductorTan());
			rowData.add(StringUtils.isBlank(errorCodesWithNewLine) ? StringUtils.EMPTY : errorCodesWithNewLine);
			rowData.add(StringUtils.isBlank(advanceErrorReport.getIndex()) ? StringUtils.EMPTY
					: advanceErrorReport.getIndex());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSourceIdentifier()) ? StringUtils.EMPTY
					: advanceErrorReport.getSourceIdentifier());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSourceFileName()) ? StringUtils.EMPTY
					: advanceErrorReport.getSourceFileName());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeductorCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeductorCode());// DeductorCode
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeductorName()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeductorName());// DeductorName
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeductorPan()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeductorPan());// DeductorPAN
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeductorTan()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeductorTan());// DeductorTAN
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeductorGstin()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeductorGstin());// DeductorGSTIN
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeducteeCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeducteeCode());// DeducteeCode
			rowData.add(StringUtils.isBlank(advanceErrorReport.getNameOfTheDeductee()) ? StringUtils.EMPTY
					: advanceErrorReport.getNameOfTheDeductee());// DeducteeName
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeducteePan()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeducteePan());// DeducteePAN
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeducteeTin()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeducteeTin());// DeducteeTIN
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeducteeGstin()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeducteeGstin());// DeducteeGSTIN
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentNumber());// ERPDocumentNumber
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentDate()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentDate());// DocumentDate
			rowData.add(StringUtils.isBlank(advanceErrorReport.getPostingDateOfTheDocument()) ? StringUtils.EMPTY
					: advanceErrorReport.getPostingDateOfTheDocument());// PostingDate
			rowData.add(StringUtils.isBlank(advanceErrorReport.getTdsDeductionDate()) ? StringUtils.EMPTY
					: advanceErrorReport.getTdsDeductionDate());// TDSDeductionDate
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentType()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentType());// DocumentType
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSupplyType()) ? StringUtils.EMPTY
					: advanceErrorReport.getSupplyType());// SupplyType
			rowData.add(StringUtils.isBlank(advanceErrorReport.getLineItemNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getLineItemNumber());// LineItemNumber
			rowData.add(StringUtils.isBlank(advanceErrorReport.getGlAccountCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getGlAccountCode()); // GLAccountCode
			rowData.add("");// GLAccountName
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentType()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentType());// ERPDocumentType
			rowData.add(StringUtils.isBlank(advanceErrorReport.getHsnsac()) ? StringUtils.EMPTY
					: advanceErrorReport.getHsnsac()); // HSNorSAC
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSacDescription()) ? StringUtils.EMPTY
					: advanceErrorReport.getSacDescription()); // HSNorSACDesc
			rowData.add(""); // AdvanceDesc
			rowData.add(StringUtils.isBlank(advanceErrorReport.getPoDesc()) ? StringUtils.EMPTY
					: advanceErrorReport.getPoDesc()); // PODesc
			rowData.add(StringUtils.isBlank(advanceErrorReport.getAdvanceAmount()) ? StringUtils.EMPTY
					: advanceErrorReport.getAdvanceAmount()); // TaxableValue
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSectionCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getSectionCode()); // TDSTaxCodeERP
			rowData.add(""); // TDSSection
			rowData.add(""); // TDSRate
			rowData.add(""); // TDSAmount
			rowData.add(StringUtils.isBlank(advanceErrorReport.getPoNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getPoNumber()); // PONumber
			rowData.add(StringUtils.isBlank(advanceErrorReport.getPoDate()) ? StringUtils.EMPTY
					: advanceErrorReport.getPoDate()); // PODate
			rowData.add(StringUtils.isBlank(advanceErrorReport.getLinkingOfInvoiceWithPo()) ? StringUtils.EMPTY
					: advanceErrorReport.getLinkingOfInvoiceWithPo()); // LinkingofInvoicewithPO
			rowData.add(advanceErrorReport.getIsChallanPaid().equals("0") ? "Y" : "N"); // ChallanPaidFlag
			rowData.add(StringUtils.isBlank(advanceErrorReport.getChallanGeneratedDate()) ? StringUtils.EMPTY
					: advanceErrorReport.getChallanGeneratedDate()); // ChallanPaidDate
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField1()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField1());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField2()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField2());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField3()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField3());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getNoTdsReason()) ? StringUtils.EMPTY
					: advanceErrorReport.getNoTdsReason());

			worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
		}
	}

	private void setExtractDataForAdvanceFromDB(List<AdvanceDTO> advanceErrorReportsCsvList, Worksheet worksheet,
			String tan, String deductorName) throws Exception {

		int rowIndex = 6;
		List<ErrorCode> errorCodesObjs = mastersClient.getAllErrorCodes().getBody().getData();
		// errorCodeService.findAll();
		Map<String, String> errorDescription = new HashMap<>();
		for (ErrorCode errorCodesObj : errorCodesObjs) {
			errorDescription.put(errorCodesObj.getCode(), errorCodesObj.getDescription());
		}
		int index = 0;
		for (AdvanceDTO advanceErrorReport : advanceErrorReportsCsvList) {
			index++;
			ArrayList<String> rowData = new ArrayList<String>();
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
								givenCodes.add(e.trim());
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

			rowData.add(tan);
			rowData.add(StringUtils.isBlank(errorCodesWithNewLine) ? StringUtils.EMPTY : errorCodesWithNewLine);
			rowData.add(index + "");
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSourceIdentifiers()) ? StringUtils.EMPTY
					: advanceErrorReport.getSourceIdentifiers());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSourceFileName()) ? StringUtils.EMPTY
					: advanceErrorReport.getSourceFileName());
			rowData.add("");// DeductorCode
			rowData.add(deductorName);// DeductorName
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeductorPan()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeductorPan());// DeductorPAN
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeductorMasterTan()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeductorMasterTan());// DeductorTAN
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeductorGstin()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeductorGstin());// DeductorGSTIN
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeducteeCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeducteeCode());// DeducteeCode
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeducteeName()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeducteeName());// DeducteeName
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeducteePan()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeducteePan());// DeducteePAN
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeducteeTin()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeducteeTin());// DeducteeTIN
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDeducteeGstin()) ? StringUtils.EMPTY
					: advanceErrorReport.getDeducteeGstin());// DeducteeGSTIN
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentNumber());// ERPDocumentNumber
			rowData.add(advanceErrorReport.getDocumentDate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentDate().toString());// DocumentDate
			rowData.add(advanceErrorReport.getPostingDateOfDocument() == null ? StringUtils.EMPTY
					: advanceErrorReport.getPostingDateOfDocument().toString());// PostingDate
			rowData.add(advanceErrorReport.getTdsDeductionDate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getTdsDeductionDate().toString());// TDSDeductionDate
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentType()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentType());// DocumentType
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSupplyType()) ? StringUtils.EMPTY
					: advanceErrorReport.getSupplyType());// SupplyType
			rowData.add(StringUtils.isBlank(advanceErrorReport.getLineItemNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getLineItemNumber());// LineItemNumber
			rowData.add(StringUtils.isBlank(advanceErrorReport.getGlAccountCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getGlAccountCode()); // GLAccountCode
			rowData.add(StringUtils.isBlank(advanceErrorReport.getServiceDescriptionGl()) ? StringUtils.EMPTY
					: advanceErrorReport.getServiceDescriptionGl());// GLAccountName
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDocumentType()) ? StringUtils.EMPTY
					: advanceErrorReport.getDocumentType());// ERPDocumentType
			rowData.add(StringUtils.isBlank(advanceErrorReport.getHsnOrSac()) ? StringUtils.EMPTY
					: advanceErrorReport.getHsnOrSac()); // HSNorSAC
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSacDescription()) ? StringUtils.EMPTY
					: advanceErrorReport.getSacDescription()); // HSNorSACDesc
			rowData.add(StringUtils.isBlank(advanceErrorReport.getServiceDescription()) ? StringUtils.EMPTY
					: advanceErrorReport.getServiceDescription()); // AdvanceDesc
			rowData.add(StringUtils.isBlank(advanceErrorReport.getServiceDescriptionPo()) ? StringUtils.EMPTY
					: advanceErrorReport.getServiceDescriptionPo()); // PODesc
			rowData.add(advanceErrorReport.getAmount() == null ? StringUtils.EMPTY
					: advanceErrorReport.getAmount().toString()); // TaxableValue
			rowData.add(StringUtils.isBlank(advanceErrorReport.getSectionCode()) ? StringUtils.EMPTY
					: advanceErrorReport.getSectionCode()); // TDSTaxCodeERP
			rowData.add(StringUtils.isBlank(advanceErrorReport.getWithholdingSection()) ? StringUtils.EMPTY
					: advanceErrorReport.getWithholdingSection()); // TDSSection
			rowData.add(advanceErrorReport.getTdsRate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getTdsRate().toString()); // TDSRate
			rowData.add(advanceErrorReport.getWithholdingAmount() == null ? StringUtils.EMPTY
					: advanceErrorReport.getWithholdingAmount().toString()); // TDSAmount
			rowData.add(StringUtils.isBlank(advanceErrorReport.getPoNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getPoNumber()); // PONumber
			rowData.add(advanceErrorReport.getPoDate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getPoDate().toString()); // PODate
			rowData.add(StringUtils.isBlank(advanceErrorReport.getLinkingOfInvoiceWithPo()) ? StringUtils.EMPTY
					: advanceErrorReport.getLinkingOfInvoiceWithPo()); // LinkingofInvoicewithPO
			rowData.add(StringUtils.EMPTY); // ChallanPaidFlag
			rowData.add(StringUtils.EMPTY); // ChallanPaidDate
			rowData.add(StringUtils.isBlank(advanceErrorReport.getOriginalDocNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getOriginalDocNumber());
			rowData.add(advanceErrorReport.getOriginalDocPostingDate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getOriginalDocPostingDate().toString());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getPoLineItemNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getPoLineItemNumber());
			rowData.add(advanceErrorReport.getTdsBaseValue() == null ? StringUtils.EMPTY
					: advanceErrorReport.getTdsBaseValue().toString());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getBusinessPlace()) ? StringUtils.EMPTY
					: advanceErrorReport.getBusinessPlace());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getBusinessArea()) ? StringUtils.EMPTY
					: advanceErrorReport.getBusinessArea());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getPlant()) ? StringUtils.EMPTY
					: advanceErrorReport.getPlant());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getProfitCenter()) ? StringUtils.EMPTY
					: advanceErrorReport.getProfitCenter());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getAssignmentNumber()) ? StringUtils.EMPTY
					: advanceErrorReport.getAssignmentNumber());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserName()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserName());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getIsResident()) ? StringUtils.EMPTY
					: advanceErrorReport.getIsResident());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getGrossupIndicator()) ? StringUtils.EMPTY
					: advanceErrorReport.getGrossupIndicator());
			rowData.add(advanceErrorReport.getTdsRemittancedate() == null ? StringUtils.EMPTY
					: advanceErrorReport.getTdsRemittancedate().toString());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getDebitCreditIndicator()) ? StringUtils.EMPTY
					: advanceErrorReport.getDebitCreditIndicator());
			rowData.add(StringUtils.EMPTY);// CESSRate
			rowData.add(StringUtils.EMPTY);// CESSAmount
			rowData.add(StringUtils.EMPTY);// AmountForeignCurrency
			rowData.add(StringUtils.EMPTY);// ExchangeRate
			rowData.add(StringUtils.EMPTY);// Currency
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
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField7()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField7());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField8()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField8());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField9()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField9());
			rowData.add(StringUtils.isBlank(advanceErrorReport.getUserDefinedField10()) ? StringUtils.EMPTY
					: advanceErrorReport.getUserDefinedField10());

			worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
		}
	}

	public String getErrorReportMsg(String deductorMasterTan, String tenantId, String deductorPan) {
		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();
		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());
		return "Advance Remediation Report (Dated: " + date + ")\n Client Name: " + deductorData.getDeductorName()
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
	public File convertAdvanceOtherReportToXlsx(File csvFile, String deductorTan) throws Exception {

		Reader reader = new FileReader(csvFile);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		CsvToBean<AdvancesErrorReportCsvDto> csvToBean = new CsvToBeanBuilder(reader)
				.withType(AdvancesErrorReportCsvDto.class).withIgnoreLeadingWhiteSpace(true).build();

		// Aspose method to convert
		Workbook workbook = generateAdvanceOtherXlsxReport(csvToBean.parse(), deductorTan);

		// Write the output to the file
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxAdvanceFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()) + "_Other", ".xlsx");

		FileUtils.writeByteArrayToFile(xlsxAdvanceFile, baout.toByteArray());
		baout.close();
		return xlsxAdvanceFile;
	}

	/**
	 * This method for set headers and colors.
	 * 
	 * @param advanceCsvDTOList
	 * @param deductorTan
	 * @return
	 * @throws Exception
	 */
	private Workbook generateAdvanceOtherXlsxReport(List<AdvancesErrorReportCsvDto> advanceCsvDTOList,
			String deductorTan) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		String[] headerNames = new String[] { "Source Identifier", "Source File Name", "Company Code",
				"Name of the Company Code", "Deductor PAN", "Deductor TAN", "Deductor GSTIN", "Deductee Code",
				"Name of the Deductee", "Non-Resident Deductee Indicator", "Deductee PAN", "Deductee TIN",
				"Deductee GSTIN", "Document Number", "Document Type", "Document Date", "Posting Date of the Document",
				"Entry Date of Advance Made", "Line item Number in the Accounting Document", "Advance Amount",
				"HSN/SAC", "SAC Description", "Service Description", "Advance to Vendor G/L Account Code",
				"Advance to Vendor G/L Account Description", "Withholding Tax Section",
				"Withholding Tax Rate/Withholding Tax Code", "Withholding Tax Amount", "PO Number",
				"Linking of Invoice with PO", "User Defined Field 1", "User Defined Field 2", "User Defined Field 3",
				"Section Predection" };

		worksheet.getCells().importArray(headerNames, 0, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForAdvanceOtherReport(advanceCsvDTOList, worksheet);

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
	private void setExtractDataForAdvanceOtherReport(List<AdvancesErrorReportCsvDto> advanceCsvDTOList,
			Worksheet worksheet) throws Exception {

		int rowIndex = 1;
		for (AdvancesErrorReportCsvDto advanceReport : advanceCsvDTOList) {
			ArrayList<Object> rowData = new ArrayList<>();
			rowData.add(StringUtils.isBlank(advanceReport.getSourceIdentifier()) ? StringUtils.EMPTY
					: advanceReport.getSourceIdentifier());
			rowData.add(StringUtils.isBlank(advanceReport.getSourceFileName()) ? StringUtils.EMPTY
					: advanceReport.getSourceFileName());
			rowData.add(StringUtils.isBlank(advanceReport.getCompanyCode()) ? StringUtils.EMPTY
					: advanceReport.getCompanyCode());
			rowData.add(StringUtils.isBlank(advanceReport.getNameOfTheCompanyCode()) ? StringUtils.EMPTY
					: advanceReport.getNameOfTheCompanyCode());
			rowData.add(StringUtils.isBlank(advanceReport.getDeductorPan()) ? StringUtils.EMPTY
					: advanceReport.getDeductorPan());
			rowData.add(StringUtils.isBlank(advanceReport.getDeductorTan()) ? StringUtils.EMPTY
					: advanceReport.getDeductorTan());
			rowData.add(StringUtils.isBlank(advanceReport.getDeductorGstin()) ? StringUtils.EMPTY
					: advanceReport.getDeductorGstin());
			rowData.add(StringUtils.isBlank(advanceReport.getDeducteeCode()) ? StringUtils.EMPTY
					: advanceReport.getDeducteeCode());
			rowData.add(StringUtils.isBlank(advanceReport.getNameOfTheDeductee()) ? StringUtils.EMPTY
					: advanceReport.getNameOfTheDeductee());
			rowData.add(StringUtils.isBlank(advanceReport.getNonresidentDeducteeIndicator()) ? StringUtils.EMPTY
					: advanceReport.getNonresidentDeducteeIndicator());
			rowData.add(StringUtils.isBlank(advanceReport.getDeducteePan()) ? StringUtils.EMPTY
					: advanceReport.getDeducteePan());
			rowData.add(StringUtils.isBlank(advanceReport.getDeducteeTin()) ? StringUtils.EMPTY
					: advanceReport.getDeducteeTin());
			rowData.add(StringUtils.isBlank(advanceReport.getDeducteeGstin()) ? StringUtils.EMPTY
					: advanceReport.getDeducteeGstin());
			rowData.add(StringUtils.isBlank(advanceReport.getDocumentNumber()) ? StringUtils.EMPTY
					: advanceReport.getDocumentNumber());
			rowData.add(StringUtils.isBlank(advanceReport.getDocumentType()) ? StringUtils.EMPTY
					: advanceReport.getDocumentType());
			rowData.add(StringUtils.isBlank(advanceReport.getDocumentDate()) ? StringUtils.EMPTY
					: advanceReport.getDocumentDate());
			rowData.add(StringUtils.isBlank(advanceReport.getPostingDateOfTheDocument()) ? StringUtils.EMPTY
					: advanceReport.getPostingDateOfTheDocument());
			rowData.add(StringUtils.isBlank(advanceReport.getEntryDateOfAdvanceMade()) ? StringUtils.EMPTY
					: advanceReport.getEntryDateOfAdvanceMade());
			rowData.add(StringUtils.isBlank(advanceReport.getLineItemNumberInAccountingDocument()) ? StringUtils.EMPTY
					: advanceReport.getLineItemNumberInAccountingDocument());
			rowData.add(StringUtils.isBlank(advanceReport.getAdvanceAmount()) ? StringUtils.EMPTY
					: advanceReport.getAdvanceAmount());
			rowData.add(StringUtils.isBlank(advanceReport.getHsnsac()) ? StringUtils.EMPTY : advanceReport.getHsnsac());
			rowData.add(StringUtils.isBlank(advanceReport.getSacDescription()) ? StringUtils.EMPTY
					: advanceReport.getSacDescription());
			rowData.add(StringUtils.isBlank(advanceReport.getServiceDescription()) ? StringUtils.EMPTY
					: advanceReport.getServiceDescription());
			rowData.add(StringUtils.isBlank(advanceReport.getAdvanceToVendorGlAccountCode()) ? StringUtils.EMPTY
					: advanceReport.getAdvanceToVendorGlAccountCode());
			rowData.add(StringUtils.isBlank(advanceReport.getAdvanceToVendorGlAccountDescription()) ? StringUtils.EMPTY
					: advanceReport.getAdvanceToVendorGlAccountDescription());
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

	public String getTdsCalculationForProvision(String tans, String firstDate, String lastDate) {
		long trueValue = 0;// TODO NEED TO CHANGE FOR SQL
		// advanceRepository.getCountOfTdsCalculationsOfAdvanceForCurrentMonth(tans,
		// firstDate, lastDate,
		// true);
		long falseValue = 0; //// TODO NEED TO CHANGE FOR SQL
		// advanceRepository.getCountOfTdsCalculationsOfAdvanceForCurrentMonth(tans,
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

	public CommonDTO<AdvanceDTO> getResidentAndNonresident(String residentType, String tan, int year, int month,
			Pagination pagination, String deducteeName) {
		BigInteger count = BigInteger.ZERO;
		CommonDTO<AdvanceDTO> advanceList = new CommonDTO<>();

		logger.info("tan : {} year : {} month : {} resident type : {} deductee name : {}", tan, year, month,
				residentType, deducteeName);

		List<AdvanceDTO> advanceData = null;
		if ("nodeducteefilter".equalsIgnoreCase(deducteeName)) {
			advanceData = advanceDAO.findAllResidentAndNonResident(year, month, tan, residentType, pagination);
			count = BigInteger.valueOf(advanceData.size());
		} else {
			advanceData = advanceDAO.findAllResidentAndNonResidentByDeductee(year, month, tan, residentType,
					deducteeName, pagination);
			count = BigInteger.valueOf(advanceData.size());
		}
		advanceList.setCount(count);
		PagedData<AdvanceDTO> pagedData = new PagedData<>(advanceData, advanceData.size(), pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		advanceList.setResultsSet(pagedData);

		return advanceList;
	}

	public MultipartFile generateCancelledAdvanceExcell(List<Integer> advanceIds, String tan, String deductorPan) {

		MultipartFile multipartFile = null;
		String[] advanceHeadersFile = new String[] { "Deductor TAN ", "Name of the Deductee", "Deductee PAN",
				"Withholding Section", "Document Type", "Supply Type", "Advance Amount", "Section Code",
				"Withholding Rate", "Withholding Tax Amount", "Advance to Vendor G/L Account Description",
				"Document Date", "Posting Date of the Document", "TDSDeductionDate", "Entry Date of Advance Made ",
				"Line item Number in the Accounting Document", "HSN/SAC", "SAC Description", "Service Description",
				"PO Number", "PO Date", "Document Number", "Non-Resident Deductee Indicator",
				"Linking of Invoice with PO", "User Defined Field 1", "User Defined Field 2", "User Defined Field 3",
				"Source Identifier", "Source File Name", "Company Code", "Deductee TIN", "Deductee GSTIN",
				"Name of the Company Code", "Deductor GSTIN", "Deductor PAN", "Deductee Code",
				"Advance to Vendor G/L Account Code", "Challan Paid", "Challan Generated Date" };

		try {
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			worksheet.setName("Imported Advances");
			worksheet.autoFitColumns();
			worksheet.autoFitRows();
			worksheet.getCells().importArray(advanceHeadersFile, 0, 0, false);
			List<AdvanceDTO> cancelList = advanceDAO.getAdvanceListBasedOnIds(advanceIds, tan, deductorPan);
			setAdvanceHeaders(cancelList, worksheet);

			worksheet.autoFitColumns();

			// Style for E1 to BC1 headers
			Style style5 = workbook.createStyle();
			style5.setForegroundColor(Color.fromArgb(91, 155, 213));
			style5.setPattern(BackgroundType.SOLID);
			style5.getFont().setBold(true);
			style5.setHorizontalAlignment(TextAlignmentType.CENTER);
			Range headerColorRange1 = worksheet.getCells().createRange("A1:AM1");
			headerColorRange1.setStyle(style5);

			// Creating AutoFilter by giving the cells range
			AutoFilter autoFilter = worksheet.getAutoFilter();
			autoFilter.setRange("A1:AM1");

			File file = new File("Cancelled_Advances_" + UUID.randomUUID() + ".xlsx");
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

	public void setAdvanceHeaders(List<AdvanceDTO> cancelList, Worksheet worksheet) throws Exception {
		int rowIndex = 1;
		for (AdvanceDTO cancelledAdvance : cancelList) {
			List<Object> rowData = new ArrayList<>();
			// Deductor TAN
			rowData.add(StringUtils.isBlank(cancelledAdvance.getDeductorMasterTan()) ? StringUtils.EMPTY
					: cancelledAdvance.getDeductorMasterTan());
			// Name of the Deductee
			rowData.add(StringUtils.isBlank(cancelledAdvance.getDeducteeName()) ? StringUtils.EMPTY
					: cancelledAdvance.getDeducteeName());
			// Deductee PAN
			rowData.add(StringUtils.isBlank(cancelledAdvance.getDeducteePan()) ? StringUtils.EMPTY
					: cancelledAdvance.getDeducteePan());
			// Withholding Section
			rowData.add(StringUtils.isBlank(cancelledAdvance.getWithholdingSection()) ? StringUtils.EMPTY
					: cancelledAdvance.getWithholdingSection());
			// Document Type
			rowData.add(StringUtils.isBlank(cancelledAdvance.getDocumentType()) ? StringUtils.EMPTY
					: cancelledAdvance.getDocumentType());
			// Supply Type
			rowData.add(StringUtils.isBlank(cancelledAdvance.getSupplyType()) ? StringUtils.EMPTY
					: cancelledAdvance.getSupplyType());
			// Advance Amount
			rowData.add(cancelledAdvance.getAmount() == null ? StringUtils.EMPTY : cancelledAdvance.getAmount());
			// Withholding Rate\\Withholding Tax Code
			rowData.add(cancelledAdvance.getWithholdingRate() == null ? StringUtils.EMPTY
					: cancelledAdvance.getWithholdingRate());
			// Withholding Tax Amount
			rowData.add(cancelledAdvance.getWithholdingAmount() == null ? StringUtils.EMPTY
					: cancelledAdvance.getWithholdingAmount());
			// Advance to Vendor G/L Account Description
			rowData.add(StringUtils.isBlank(cancelledAdvance.getToVendorAccountDescription()) ? StringUtils.EMPTY
					: cancelledAdvance.getToVendorAccountDescription());
			// Document Date
			rowData.add(cancelledAdvance.getDocumentDate() == null ? StringUtils.EMPTY
					: cancelledAdvance.getDocumentDate());
			// Posting Date of the Document
			rowData.add(cancelledAdvance.getPostingDateOfDocument() == null ? StringUtils.EMPTY
					: cancelledAdvance.getPostingDateOfDocument());
			// TDS Deduction Date
			rowData.add(cancelledAdvance.getTdsDeductionDate() == null ? StringUtils.EMPTY
					: cancelledAdvance.getTdsDeductionDate());
			// Entry Date of Advance Made
			rowData.add(cancelledAdvance.getEntryDate() == null ? StringUtils.EMPTY : cancelledAdvance.getEntryDate());
			// "Line item Number in the Accounting Document
			rowData.add(StringUtils.isBlank(cancelledAdvance.getLineItemNumber()) ? StringUtils.EMPTY
					: cancelledAdvance.getLineItemNumber());
			// HSN/SAC
			rowData.add(StringUtils.isBlank(cancelledAdvance.getHsnOrSac()) ? StringUtils.EMPTY
					: cancelledAdvance.getHsnOrSac());
			// SAC Description
			rowData.add(StringUtils.isBlank(cancelledAdvance.getSacDescription()) ? StringUtils.EMPTY
					: cancelledAdvance.getSacDescription());
			// Service Description
			rowData.add(StringUtils.isBlank(cancelledAdvance.getServiceDescription()) ? StringUtils.EMPTY
					: cancelledAdvance.getServiceDescription());
			// PO Number
			rowData.add(cancelledAdvance.getPoNumber() == null ? StringUtils.EMPTY : cancelledAdvance.getPoNumber());
			// PO Date
			rowData.add(cancelledAdvance.getPoDate() == null ? StringUtils.EMPTY : cancelledAdvance.getPoDate());
			// Document Number
			rowData.add(StringUtils.isBlank(cancelledAdvance.getDocumentNumber()) ? StringUtils.EMPTY
					: cancelledAdvance.getDocumentNumber());
			// Non-Resident Deductee Indicator
			rowData.add(StringUtils.isBlank(cancelledAdvance.getIsResident()) ? StringUtils.EMPTY
					: cancelledAdvance.getIsResident());
			// Linking of Invoice with PO
			rowData.add(StringUtils.isBlank(cancelledAdvance.getLinkingOfInvoiceWithPo()) ? StringUtils.EMPTY
					: cancelledAdvance.getLinkingOfInvoiceWithPo());
			// User Defined Field 1
			rowData.add(StringUtils.isBlank(cancelledAdvance.getUserDefinedField1()) ? StringUtils.EMPTY
					: cancelledAdvance.getUserDefinedField1());
			// User Defined Field 2
			rowData.add(StringUtils.isBlank(cancelledAdvance.getUserDefinedField2()) ? StringUtils.EMPTY
					: cancelledAdvance.getUserDefinedField2());
			// User Defined Field 3
			rowData.add(StringUtils.isBlank(cancelledAdvance.getUserDefinedField3()) ? StringUtils.EMPTY
					: cancelledAdvance.getUserDefinedField3());
			// Source Identifier
			rowData.add(StringUtils.isBlank(cancelledAdvance.getSourceIdentifiers()) ? StringUtils.EMPTY
					: cancelledAdvance.getSourceIdentifiers());
			// Source File Name
			rowData.add(StringUtils.isBlank(cancelledAdvance.getSourceFileName()) ? StringUtils.EMPTY
					: cancelledAdvance.getSourceFileName());
			// Company Code
			rowData.add(StringUtils.isBlank(cancelledAdvance.getCompanyCode()) ? StringUtils.EMPTY
					: cancelledAdvance.getCompanyCode());
			// Deductee TIN
			rowData.add(StringUtils.isBlank(cancelledAdvance.getDeducteeTin()) ? StringUtils.EMPTY
					: cancelledAdvance.getDeducteeTin());
			// Deductee GSTIN
			rowData.add(StringUtils.isBlank(cancelledAdvance.getDeducteeGstin()) ? StringUtils.EMPTY
					: cancelledAdvance.getDeducteeGstin());
			// Name of the Company Code
			rowData.add(StringUtils.isBlank(cancelledAdvance.getNameOfTheCompanyCode()) ? StringUtils.EMPTY
					: cancelledAdvance.getNameOfTheCompanyCode());
			// Deductor GSTIN
			rowData.add(StringUtils.isBlank(cancelledAdvance.getDeductorGstin()) ? StringUtils.EMPTY
					: cancelledAdvance.getDeductorGstin());
			// Deductor PAN
			rowData.add(StringUtils.isBlank(cancelledAdvance.getDeductorPan()) ? StringUtils.EMPTY
					: cancelledAdvance.getSourceFileName());
			// Deductee Code
			rowData.add(StringUtils.isBlank(cancelledAdvance.getDeducteeCode()) ? StringUtils.EMPTY
					: cancelledAdvance.getDeducteeCode());
			// Advance to Vendor G/L Account Code
			rowData.add(StringUtils.isBlank(cancelledAdvance.getToVendorAccountCode()) ? StringUtils.EMPTY
					: cancelledAdvance.getToVendorAccountCode());
			// Challan Paid
			rowData.add(
					cancelledAdvance.getChallanPaid() == null ? StringUtils.EMPTY : cancelledAdvance.getChallanPaid());
			// Challan Generated Date
			rowData.add(cancelledAdvance.getChallanGeneratedDate() == null ? StringUtils.EMPTY
					: cancelledAdvance.getChallanGeneratedDate());
			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);

		}
	}

	private static void setCellColorAndBoarder(DefaultIndexedColorMap defaultIndexedColorMap, XSSFCellStyle style,
			Integer r, Integer g, Integer b) {
		style.setFillForegroundColor(new XSSFColor(new java.awt.Color(r, g, b), defaultIndexedColorMap));
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);

	}

	private void setMediumBorder(DefaultIndexedColorMap defaultIndexedColorMap, XSSFCellStyle style, Integer r,
			Integer g, Integer b) {
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderRight(BorderStyle.MEDIUM);
		style.setFillForegroundColor(new XSSFColor(new java.awt.Color(r, g, b), defaultIndexedColorMap));
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	}

	// Surcharge calculation
	public BigDecimal surchargeCalculation(String finalTdsSection, BigDecimal finalTdsAmount, BigDecimal taxableAmount,
			String deducteeStatus, Map<String, List<Map<String, Object>>> surchargeMap) {
		BigDecimal surchargeRate = BigDecimal.ZERO;
		BigDecimal surcharge = BigDecimal.ZERO;
		String surchargeKey = finalTdsSection + "-" + deducteeStatus;
		if (surchargeMap.get(surchargeKey) != null) {
			for (Map<String, Object> surchargeData : surchargeMap.get(surchargeKey)) {
				Integer surchargeSlabFrom = (Integer) surchargeData.get("invoiceSlabFrom");
				Integer surchargeSlabTo = (Integer) surchargeData.get("invoiceSlabTo");
				if (surchargeSlabFrom == 0 || (Double.valueOf(surchargeSlabFrom) < taxableAmount.doubleValue()
						&& ((surchargeSlabTo == 0) || (surchargeSlabTo.doubleValue() > taxableAmount.doubleValue())))) {
					Double rate = (Double) surchargeData.get("rate");
					surchargeRate = rate != null ? BigDecimal.valueOf(rate) : BigDecimal.ZERO;
					break;
				}
			}
			surcharge = finalTdsAmount.multiply(surchargeRate).divide(BigDecimal.valueOf(100));
			logger.info("Surcharge amount : {}", surcharge);
		}
		return surcharge;
	}

	@Async
	public void asyncExportInterestComputationReport(String tan, Integer year, String userName, String deductorPan,
			Integer month, String tenantId)
			throws InvalidKeyException, FileNotFoundException, IOException, URISyntaxException, StorageException {
		logger.info("Entered into async method for generating interest computation report for Advance{}");
		MultiTenantContext.setTenantId(tenantId);
		exportInterestComputationReport(tan, year, userName, deductorPan, month, tenantId);
	}

	@Transactional
	public void exportInterestComputationReport(String tan, Integer year, String userName, String deductorPan,
			Integer month, String tenantId)
			throws InvalidKeyException, FileNotFoundException, IOException, URISyntaxException, StorageException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();

		BatchUpload batchUpload = saveInterestComputationReport(tan, tenantId, year, null, 0L,
				UploadTypes.ADVANCE_INTEREST_COMPUTATION_REPORT.name(), "Processing", month, userName, null);

		try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
			SXSSFSheet sheet = workbook.createSheet("Advance_Interest_Report");
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);
			sheet.protectSheet("password");
			sheet.setDisplayGridlines(false);
			sheet.setDefaultColumnWidth(20);
			sheet.trackAllColumnsForAutoSizing();
			sheet.setDefaultRowHeightInPoints(25);
			sheet.setColumnHidden(25, true);

			sheet.setAutoFilter(new CellRangeAddress(3, 3, 0, 24));

			DataValidation dataValidation = null;
			DataValidationConstraint constraint = null;
			DataValidationHelper validationHelper = null;

			XSSFFont font1 = (XSSFFont) workbook.createFont();
			font1.setBold(true);
			font1.setColor(new XSSFColor(new java.awt.Color(47, 79, 79), defaultIndexedColorMap));

			// setting the top message style,data,font and all
			XSSFCellStyle headerMsgStyle = (XSSFCellStyle) workbook.createCellStyle();
			headerMsgStyle.setFont(font1);
			headerMsgStyle.setBorderLeft(BorderStyle.MEDIUM);
			headerMsgStyle.setBorderTop(BorderStyle.MEDIUM);
			headerMsgStyle.setBorderBottom(BorderStyle.MEDIUM);
			headerMsgStyle.setBorderRight(BorderStyle.MEDIUM);
			headerMsgStyle.setAlignment(HorizontalAlignment.CENTER);
			headerMsgStyle.setVerticalAlignment(VerticalAlignment.CENTER);

			// merging cells for headers message
			sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 2));
			// header message
			String message = getInterestComputationReportMsg(tan, MultiTenantContext.getTenantId(), deductorPan);
			SXSSFRow messageRow = sheet.createRow(0);
			messageRow.createCell(0).setCellValue(message);
			messageRow.getCell(0).setCellStyle(headerMsgStyle);

			// setting style and values to the header row
			XSSFFont font2 = (XSSFFont) workbook.createFont();
			font2.setBold(true);
			font2.setColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));
			XSSFCellStyle style1 = (XSSFCellStyle) workbook.createCellStyle();
			style1.setFont(font2);
			style1.setBorderLeft(BorderStyle.MEDIUM);
			style1.setBorderTop(BorderStyle.MEDIUM);
			style1.setBorderBottom(BorderStyle.MEDIUM);
			style1.setBorderRight(BorderStyle.MEDIUM);
			style1.setLeftBorderColor(new XSSFColor(new java.awt.Color(255, 255, 225), defaultIndexedColorMap));
			style1.setRightBorderColor(new XSSFColor(new java.awt.Color(255, 255, 225), defaultIndexedColorMap));
			style1.setAlignment(HorizontalAlignment.CENTER);
			style1.setVerticalAlignment(VerticalAlignment.CENTER);
			style1.setFillForegroundColor(new XSSFColor(new java.awt.Color(32, 119, 195), defaultIndexedColorMap));
			style1.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle styleGreen = (XSSFCellStyle) workbook.createCellStyle();
			styleGreen.setFont(font2);
			styleGreen.setBorderLeft(BorderStyle.MEDIUM);
			styleGreen.setBorderTop(BorderStyle.MEDIUM);
			styleGreen.setBorderBottom(BorderStyle.MEDIUM);
			styleGreen.setBorderRight(BorderStyle.MEDIUM);
			styleGreen.setLeftBorderColor(new XSSFColor(new java.awt.Color(255, 255, 225), defaultIndexedColorMap));
			styleGreen.setRightBorderColor(new XSSFColor(new java.awt.Color(255, 255, 225), defaultIndexedColorMap));
			styleGreen.setAlignment(HorizontalAlignment.CENTER);
			styleGreen.setVerticalAlignment(VerticalAlignment.CENTER);
			styleGreen.setFillForegroundColor(new XSSFColor(new java.awt.Color(45, 134, 45), defaultIndexedColorMap));
			styleGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			// TODO ADD THE HEADERS HERE
			SXSSFRow row1 = sheet.createRow(3);
			row1.createCell(0).setCellValue("Sequence Number");
			row1.getCell(0).setCellStyle(style1);
			row1.createCell(1).setCellValue("Client Amount");
			row1.getCell(1).setCellStyle(style1);
			row1.createCell(2).setCellValue("TDS Section");
			row1.getCell(2).setCellStyle(style1);
			row1.createCell(3).setCellValue("TDS Rate");
			row1.getCell(3).setCellStyle(style1);
			row1.createCell(4).setCellValue("Derived Amount");
			row1.getCell(4).setCellStyle(style1);
			row1.createCell(5).setCellValue("TDS Section");
			row1.getCell(5).setCellStyle(style1);
			row1.createCell(6).setCellValue("TDS Rate");
			row1.getCell(6).setCellStyle(style1);
			row1.createCell(7).setCellValue("Amount");
			row1.getCell(7).setCellStyle(style1);
			row1.createCell(8).setCellValue("Section");
			row1.getCell(8).setCellStyle(style1);
			row1.createCell(9).setCellValue("Rate");
			row1.getCell(9).setCellStyle(style1);
			row1.createCell(10).setCellValue("Deduction Type");
			row1.getCell(10).setCellStyle(style1);
			row1.createCell(11).setCellValue("Deductor TAN");
			row1.getCell(11).setCellStyle(style1);
			row1.createCell(12).setCellValue("Deductee PAN");
			row1.getCell(12).setCellStyle(style1);
			row1.createCell(13).setCellValue("Name of the Deductee");
			row1.getCell(13).setCellStyle(style1);

			row1.createCell(14).setCellValue("Service Description");
			row1.getCell(14).setCellStyle(style1);
			row1.createCell(15).setCellValue("Service Description - PO");
			row1.getCell(15).setCellStyle(style1);
			row1.createCell(16).setCellValue("Service Description - GL Text");
			row1.getCell(16).setCellStyle(style1);
			row1.createCell(17).setCellValue("Invoice Line  Hash Code");
			row1.getCell(17).setCellStyle(style1);
			row1.createCell(18).setCellValue("With Holding Section");
			row1.getCell(18).setCellStyle(style1);

			row1.createCell(19).setCellValue("Confidence");
			row1.getCell(19).setCellStyle(style1);
			row1.createCell(20).setCellValue("ERP Document Number");
			row1.getCell(20).setCellStyle(style1);
			row1.createCell(21).setCellValue("Interest");
			row1.getCell(21).setCellStyle(style1);

			row1.createCell(22).setCellValue("Action");
			row1.getCell(22).setCellStyle(styleGreen);
			row1.createCell(23).setCellValue("Reason");
			row1.getCell(23).setCellStyle(styleGreen);
			row1.createCell(24).setCellValue("Final Interest Amount");
			row1.getCell(24).setCellStyle(styleGreen);
			row1.createCell(25).setCellValue("Advance ID");
			row1.getCell(25).setCellStyle(style1);

			List<AdvanceDTO> list = advanceDAO.getAdvancesWithInterestComputed(tan, year, month);
			long size = list.size();

			validationHelper = sheet.getDataValidationHelper();
			CellRangeAddressList addressList = new CellRangeAddressList(4, (int) size + 4, 22, 22);
			constraint = validationHelper.createExplicitListConstraint(new String[] { "Accept", "Modify", "Reject" });
			dataValidation = validationHelper.createValidation(constraint, addressList);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.setShowPromptBox(true);
			sheet.addValidationData(dataValidation);

			// setting style and data to value rows
			XSSFFont font3 = (XSSFFont) workbook.createFont();
			font3.setBold(false);
			XSSFCellStyle style2 = (XSSFCellStyle) workbook.createCellStyle();
			style2.setFont(font3);
			style2.setBorderLeft(BorderStyle.THIN);
			style2.setBorderTop(BorderStyle.THIN);
			style2.setBorderBottom(BorderStyle.THIN);
			style2.setBorderRight(BorderStyle.THIN);
			style2.setAlignment(HorizontalAlignment.LEFT);
			style2.setVerticalAlignment(VerticalAlignment.CENTER);
			style2.setFillForegroundColor(new XSSFColor(new java.awt.Color(230, 242, 255), defaultIndexedColorMap));
			style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			XSSFCellStyle style3 = (XSSFCellStyle) workbook.createCellStyle();
			style3.setFont(font3);
			style3.setLocked(false);
			style3.setBorderTop(BorderStyle.THIN);
			style3.setBorderBottom(BorderStyle.THIN);
			style3.setBorderRight(BorderStyle.THIN);
			style3.setAlignment(HorizontalAlignment.LEFT);
			style3.setVerticalAlignment(VerticalAlignment.CENTER);
			style3.setFillForegroundColor(new XSSFColor(new java.awt.Color(217, 242, 217), defaultIndexedColorMap));
			style3.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			int index = 3;
			for (AdvanceDTO listData : list) {
				index++;

				SXSSFRow row2 = sheet.createRow(index);
				row2.createCell(0)
						.setCellValue(listData.getSequenceNumber() == null ? " " : listData.getSequenceNumber());
				row2.getCell(0).setCellStyle(style2);
				row2.createCell(1).setCellValue(
						listData.getWithholdingAmount() == null ? " " : listData.getWithholdingAmount().toString());
				row2.getCell(1).setCellStyle(style2);
				row2.createCell(2).setCellValue(listData.getWithholdingSection());
				row2.getCell(2).setCellStyle(style2);
				row2.createCell(3).setCellValue(
						listData.getWithholdingRate() == null ? " " : listData.getWithholdingRate().toString());
				row2.getCell(3).setCellStyle(style2);
				row2.createCell(4).setCellValue(
						listData.getDerivedTdsAmount() == null ? " " : listData.getDerivedTdsAmount().toString());
				row2.getCell(4).setCellStyle(style2);
				row2.createCell(5).setCellValue(listData.getDerivedTdsSection());
				row2.getCell(5).setCellStyle(style2);

				row2.createCell(6).setCellValue(
						listData.getDerivedTdsRate() == null ? "0.00" : listData.getDerivedTdsRate().toString());
				row2.getCell(6).setCellStyle(style2);

				row2.createCell(7).setCellValue(listData.getAmount() == null ? " " : listData.getAmount().toString());
				row2.getCell(7).setCellStyle(style2);
				row2.createCell(8).setCellValue(listData.getWithholdingSection());
				row2.getCell(8).setCellStyle(style2);

				row2.createCell(9).setCellValue(
						listData.getWithholdingRate() == null ? " " : listData.getWithholdingRate().toString());
				row2.getCell(9).setCellStyle(style2);

				row2.createCell(10).setCellValue(StringUtils.EMPTY);
				row2.getCell(10).setCellStyle(style2);

				row2.createCell(11).setCellValue(listData.getDeductorMasterTan());
				row2.getCell(11).setCellStyle(style2);

				row2.createCell(12).setCellValue(listData.getDeducteePan());
				row2.getCell(12).setCellStyle(style2);

				row2.createCell(13).setCellValue(listData.getDeducteeName());
				row2.getCell(13).setCellStyle(style2);

				row2.createCell(14).setCellValue(listData.getServiceDescription());
				row2.getCell(14).setCellStyle(style2);

				row2.createCell(15).setCellValue(listData.getServiceDescriptionPo());
				row2.getCell(15).setCellStyle(style2);

				row2.createCell(16).setCellValue(listData.getServiceDescriptionGl());
				row2.getCell(16).setCellStyle(style2);
				row2.createCell(17).setCellValue(StringUtils.EMPTY);
				row2.getCell(17).setCellStyle(style2);
				row2.createCell(18).setCellValue(listData.getWithholdingSection());
				row2.getCell(18).setCellStyle(style2);
				row2.createCell(19).setCellValue(
						StringUtils.isBlank(listData.getConfidence()) ? StringUtils.EMPTY : listData.getConfidence());
				row2.getCell(19).setCellStyle(style2);
				row2.createCell(20).setCellValue(listData.getDocumentNumber());
				row2.getCell(20).setCellStyle(style2);
				row2.createCell(21).setCellValue(listData.getInterest().toString());
				row2.getCell(21).setCellStyle(style2);

				// unlocked data
				row2.createCell(22).setCellValue(""); // action
				row2.getCell(22).setCellStyle(style3);
				row2.createCell(23).setCellValue(""); // reason
				row2.getCell(23).setCellStyle(style3);
				row2.createCell(24).setCellValue("");// final interest amount
				row2.getCell(24).setCellStyle(style3);
				row2.createCell(25).setCellValue(listData.getId() + "");
				row2.getCell(25).setCellStyle(style2);

			}

			workbook.write(out);
			saveInterestComputationReport(tan, tenantId, year, out, Long.valueOf(size),
					UploadTypes.ADVANCE_INTEREST_COMPUTATION_REPORT.name(), "Processed", month, userName,
					batchUpload.getBatchUploadID());
		}

	}

	protected BatchUpload saveInterestComputationReport(String deductorTan, String tenantId, int assessmentYear,
			ByteArrayOutputStream out, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId)
			throws FileNotFoundException, IOException, URISyntaxException, InvalidKeyException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		String path = null;
		String fileName = null;
		if (out != null) {
			fileName = uploadType.toLowerCase() + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
			File file = getConvertedExcelFile(out.toByteArray(), fileName);
			path = blob.uploadExcelToBlobWithFile(file, tenantId);
			logger.info("Invoice Computation report {} completed for : {}", uploadType, userName);
		} else {
			logger.info("Invoice Computation report {} started for : {}", uploadType, userName);
		}
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setBatchUploadID(batchId);
		batchUpload.setActive(true);
		if (batchId != null) {// 11870
			batchUpload = batchUploadDAO.findByOnlyId(batchId);

			// batchUpload = response.get(0);
			if (out != null) {
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchUpload.setFileName(fileName);
				batchUpload.setStatus("Processed");
				batchUpload.setFilePath(path);
			}
			batchUpload.setModifiedBy(userName);
			return batchUploadDAO.update(batchUpload);
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
		batchUpload.setCreatedBy(userName);
		batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
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

	public String getInterestComputationReportMsg(String deductorMasterTan, String tenantId, String deductorPan) {
		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return "Advance Interest Computation Report (Dated: " + date + ")\n Client Name: "
				+ deductorData.getDeductorName() + "\n";
	}

	public CommonDTO<AdvanceDTO> getAdvanceInterestRecords(String tan, int year, int challanMonth,
			MismatchesFiltersDTO filters) {
		CommonDTO<AdvanceDTO> advanceData = new CommonDTO<>();

		List<AdvanceDTO> invoiceList = advanceDAO.getAdvancecesWithInterestComputedWithPagination(tan, year,
				challanMonth, filters.getDeducteeName(), filters.getResidentType(), filters.getPagination());
		BigInteger count = advanceDAO.getCountOfAdvancesWithInterestComputed(tan, year, challanMonth,
				filters.getDeducteeName(), filters.getResidentType());

		PagedData<AdvanceDTO> pagedData = new PagedData<AdvanceDTO>(invoiceList, invoiceList.size(),
				filters.getPagination().getPageNumber(),
				count.intValue() > (filters.getPagination().getPageSize() * filters.getPagination().getPageNumber())
						? false
						: true);
		advanceData.setResultsSet(pagedData);
		advanceData.setCount(count);
		return advanceData;

	}

	@Async
	public void asyncProcessAdvanceComputationFile(String tenantId, String userName, String deductorTan,
			String deductorPan, Integer year, Integer month, String uploadType, BatchUpload batchUpload)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Async method executing to process the interest report {}");
		processAdvanceComputationFile(tenantId, userName, deductorTan, deductorPan, year, month, uploadType,
				batchUpload);
	}

	/**
	 * this method is to read the file and process data
	 * 
	 * @param tenantId
	 * @param userName
	 * @param deductorTan
	 * @param deductorPan
	 * @param year
	 * @param month
	 * @param file
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 */
	@Transactional
	public void processAdvanceComputationFile(String tenantId, String userName, String deductorTan, String deductorPan,
			Integer year, Integer month, String uploadType, BatchUpload batchUpload)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		MultiTenantContext.setTenantId(tenantId);
		File file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());

		try (XSSFWorkbook workbook = new XSSFWorkbook(file.getAbsolutePath())) {

			XSSFSheet worksheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();
			Integer lastRowNo = 4;
			Integer successCount = 0;

			while (lastRowNo <= worksheet.getLastRowNum()) {
				XSSFRow row = worksheet.getRow(lastRowNo++);

				String userAction = formatter.formatCellValue(row.getCell(22));
				String reason = formatter.formatCellValue(row.getCell(23));
				Integer invoiceLineItemId = null;
				if (StringUtils.isNotBlank(formatter.formatCellValue(row.getCell(25)))) {
					invoiceLineItemId = Integer.parseInt(formatter.formatCellValue(row.getCell(25)));

				}

				if (StringUtils.isNotBlank(userAction) && invoiceLineItemId != null) {
					AdvanceDTO advance = null;
					List<AdvanceDTO> list = advanceDAO.getAdvancsWithInterestById(invoiceLineItemId);
					if (!list.isEmpty()) {
						advance = list.get(0);
						BigDecimal finalAmount = BigDecimal.ZERO;
						if (userAction.equalsIgnoreCase("Accept")) {
							successCount++;
							finalAmount = new BigDecimal(formatter.formatCellValue(row.getCell(21)).toString());

						} else if (userAction.equalsIgnoreCase("Modify")) {
							if (StringUtils.isNotBlank(formatter.formatCellValue(row.getCell(24)))) {
								successCount++;
								finalAmount = new BigDecimal(formatter.formatCellValue(row.getCell(24)).toString());
							}
						} else if (userAction.equalsIgnoreCase("Reject")) {
							successCount++;
							finalAmount = BigDecimal.ZERO;
						}
						advance.setFinalTdsAmount(finalAmount);
						advance.setModifiedBy(userName);
						advance.setAction(userAction);
						advance.setReason(reason);
						advance.setModifiedDate(new Timestamp(new Date().getTime()));
						advanceDAO.updateAdvanceInterest(advance);
						successCount++;
					}

				} // end of if block for useraction empty check
			}
			batchUpload.setStatus("Processed");
			batchUpload.setRowsCount((long) successCount);
			batchUpload.setSuccessCount((long) successCount);
			batchUpload.setProcessedCount(successCount);
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload.setModifiedBy(userName);
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			batchUploadDAO.update(batchUpload);
			logger.info("Updated Batch upload successfully for advance interest report upload{}");

		}
	}

	public void updateInterestrecords(String tan, UpdateOnScreenDTO updateOnScreenDTO, String pan) {

		for (UpdateOnScreenDTO invoiceInterest : updateOnScreenDTO.getData()) {
			if (invoiceInterest.getId() != null) {
				AdvanceDTO advance = null;
				logger.info("Fetching advance interest records {}");
				List<AdvanceDTO> list = advanceDAO.getAdvancsWithInterestById(invoiceInterest.getId());
				if (!list.isEmpty()) {
					advance = list.get(0);
					logger.info("Retrieved  advance interest record {}" + advance);
					String userAction = updateOnScreenDTO.getActionType();
					BigDecimal finalTdsAmount = advance.getInterest();

					if (userAction.equalsIgnoreCase("modify")) {
						finalTdsAmount = invoiceInterest.getFinalAmount();
					} else if (userAction.equalsIgnoreCase("Reject")) {
						finalTdsAmount = BigDecimal.ZERO;
					}
					advance.setFinalTdsAmount(finalTdsAmount);
					advance.setReason(updateOnScreenDTO.getReason());
					advance.setAction(updateOnScreenDTO.getActionType());
					advanceDAO.updateAdvanceInterest(advance);
					logger.info("  advance interest record updated successfully with final amount as {}"
							+ advance.getFinalTdsAmount());
				}

			}
		}
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
	 * @param csvLinesSize
	 * @param csv
	 * @param tan
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	public ByteArrayInputStream generateMismatchExcel(int csvLinesSize, CsvContainer csv, String tan, String tenantId,
			String deductorPan)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {

		MultiTenantContext.setTenantId(tenantId);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = resourceLoader.getResource("classpath:templates/" + "Mismatch_template.xlsx");
		InputStream input = resource.getInputStream();

		try (SXSSFWorkbook wb = new SXSSFWorkbook(new XSSFWorkbook(input))) {
			SXSSFSheet sheet = wb.getSheetAt(0);
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);
			sheet.protectSheet("password");
			DataValidation dataValidation = null;
			DataValidationConstraint constraint = null;
			DataValidationHelper validationHelper = null;
			long size = csvLinesSize;
			validationHelper = sheet.getDataValidationHelper();
			CellRangeAddressList addressList = new CellRangeAddressList(15, (int) size + 15, 58, 58);
			constraint = validationHelper
					.createExplicitListConstraint(new String[] { "Accept", "Modify - Taxable value",
							"Modify - TDS base value", "Modify - Any other amount", "Reject", "Cancel" });
			dataValidation = validationHelper.createValidation(constraint, addressList);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.setShowPromptBox(true);
			sheet.addValidationData(dataValidation);

			validationHelper = sheet.getDataValidationHelper();
			CellRangeAddressList addressList2 = new CellRangeAddressList(15, (int) size + 15, 60, 60);
			// Feign Client to get list of sections from nature of payment
			ResponseEntity<ApiStatus<List<String>>> natureOfPayament = mastersClient.findAllNatureOfPaymentSections();
			List<String> natureOfPayamentList = natureOfPayament.getBody().getData();
			Set<String> set = new LinkedHashSet<>();
			set.addAll(natureOfPayamentList);
			natureOfPayamentList.clear();
			natureOfPayamentList.addAll(set);
			constraint = validationHelper.createExplicitListConstraint(
					natureOfPayamentList.toArray(new String[natureOfPayamentList.size()]));
			dataValidation = validationHelper.createValidation(constraint, addressList2);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.setShowPromptBox(true);
			sheet.addValidationData(dataValidation);
			int rowindex = 15;
			Integer sequenceNumber = 1;
			logger.info("total results size : {}", size);
			Map<String, List<DeducteeDetailsDTO>> deducteeResMap = new HashMap<>();
			Map<String, Set<String>> resSectionMap = new HashMap<>();
			Map<String, List<DeducteeDetailsDTO>> deducteeNRMap = new HashMap<>();
			Map<String, Set<String>> nrSectionMap = new HashMap<>();
			List<DeducteeDetailsDTO> vendorSectionNR = advanceDAO.getDeducteeNonResidentStatusAll(deductorPan);
			List<DeducteeDetailsDTO> vendorSectionR = advanceDAO.getDeducteeResidentStatusAll(deductorPan);
			for (DeducteeDetailsDTO deducteeDetail : vendorSectionR) {
				Set<String> sections = advanceDAO.getDeducteeSections("N", deducteeDetail);
				if (resSectionMap.get(deducteeDetail.getDeducteeKey()) == null) {
					resSectionMap.put(deducteeDetail.getDeducteeKey(), new HashSet<>());
				}
				resSectionMap.get(deducteeDetail.getDeducteeKey()).addAll(sections);
				if (deducteeResMap.get(deducteeDetail.getDeducteeKey()) == null) {
					deducteeResMap.put(deducteeDetail.getDeducteeKey(), new ArrayList<>());
				}
				deducteeResMap.get(deducteeDetail.getDeducteeKey()).add(deducteeDetail);
			}
			for (DeducteeDetailsDTO deducteeDetail : vendorSectionNR) {
				Set<String> sections = advanceDAO.getDeducteeSections("Y", deducteeDetail);
				if (nrSectionMap.get(deducteeDetail.getDeducteeKey()) == null) {
					nrSectionMap.put(deducteeDetail.getDeducteeKey(), new HashSet<>());
				}
				nrSectionMap.get(deducteeDetail.getDeducteeKey()).addAll(sections);
				if (deducteeNRMap.get(deducteeDetail.getDeducteeKey()) == null) {
					deducteeNRMap.put(deducteeDetail.getDeducteeKey(), new ArrayList<>());
				}
				deducteeNRMap.get(deducteeDetail.getDeducteeKey()).add(deducteeDetail);
			}
			// Deductor details
			DeductorMaster deductorMaster = advanceDAO.findByDeductorPan(deductorPan);
			String msg = getMismatchReportMsg(deductorMaster.getName());
			// Deductor onboarding paramters
			Map<String, String> onboardingPriorities = getPriorities(
					advanceDAO.findOnboardingDetailsByDeductorPan(deductorPan));
			// get ldc records based on tan
			List<LdcMasterDTO> ldcList = ldcMasterDAO.getLdcSectionValidDate(tan, tenantId);
			Map<String, List<LdcMasterDTO>> ldcMap = new TreeMap<>();
			if (!ldcList.isEmpty()) {
				for (LdcMasterDTO ldc : ldcList) {
					if (!ldcMap.containsKey(ldc.getPan())) {
						ldcMap.put(ldc.getPan(), new ArrayList<>());
					}
					ldcMap.get(ldc.getPan()).add(ldc);
				}
			}
			XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
			style.setWrapText(true);
			XSSFCellStyle styleUnlocked = (XSSFCellStyle) wb.createCellStyle();
			styleUnlocked.setLocked(false);
			XSSFFont fonts = (XSSFFont) wb.createFont();
			fonts.setBold(false);
			style.setFont(fonts);
			sheet.setColumnHidden(114, true);
			sheet.setColumnHidden(115, true);
			sheet.setColumnHidden(116, true);
			sheet.setColumnHidden(117, true);
			sheet.setColumnHidden(118, true);
			sheet.setColumnHidden(119, true);
			sheet.setColumnHidden(120, true);
			sheet.setColumnHidden(121, true);
			sheet.setColumnHidden(122, true);
			sheet.setColumnHidden(123, true);

			XSSFSheet xssfSheet = wb.getXSSFWorkbook().getSheetAt(0);

			XSSFCell cell = xssfSheet.getRow(0).createCell(0);
			cell.setCellValue(msg);
			cell.setCellStyle(style);
			XSSFRow headerRow = xssfSheet.getRow(14);
			if (onboardingPriorities != null) {
				String priority = "Priority";
				if (onboardingPriorities.get("VendorMaster") != null) {
					headerRow.getCell(53).setCellValue(priority + onboardingPriorities.get("VendorMaster"));
				}
				if (onboardingPriorities.get("SACDesc") != null) {
					headerRow.getCell(54).setCellValue(priority + onboardingPriorities.get("SACDesc"));
				}
				if (onboardingPriorities.get("PODesc") != null) {
					headerRow.getCell(55).setCellValue(priority + onboardingPriorities.get("PODesc"));
				}
				if (onboardingPriorities.get("InvoiceDesc") != null) {
					headerRow.getCell(56).setCellValue(priority + onboardingPriorities.get("InvoiceDesc"));
				}
				if (onboardingPriorities.get("GLDesc") != null) {
					headerRow.getCell(57).setCellValue(priority + onboardingPriorities.get("GLDesc"));
				}
			}
			for (CsvRow row : csv.getRows()) {
				if (StringUtils.isNotBlank(StringUtils.join(row.getFields(), ", ").replace(",", ""))) {
					List<String> sections = new ArrayList<>();
					SXSSFRow row1 = sheet.createRow(rowindex++);
					row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));

					createSXSSFCell(style, row1, 0, sequenceNumber + "");
					sequenceNumber++;
					createSXSSFCell(style, row1, 1, deductorMaster.getCode());
					createSXSSFCell(style, row1, 2, row.getField("deductor_master_tan"));
					createSXSSFCell(style, row1, 3, row.getField("deductor_gstin"));
					createSXSSFCell(style, row1, 4, row.getField("deductee_code"));
					createSXSSFCell(style, row1, 5, row.getField("deductee_name"));
					createSXSSFCell(style, row1, 6, row.getField("deductee_pan"));
					String panValidationStatus = StringUtils.EMPTY;
					String gstinNumber = StringUtils.EMPTY;
					String deducteeStatus = StringUtils.EMPTY;
					String aadharNo = StringUtils.EMPTY;
					String tdsApplicabilityUnderSection = StringUtils.EMPTY;
					String grOrIRIndicator = StringUtils.EMPTY;
					if (row.getField("is_resident").equals("N") && !deducteeResMap.isEmpty()
							&& deducteeResMap.get(row.getField("deductee_key")) != null) {
						DeducteeDetailsDTO deducteeDetailData = deducteeResMap.get(row.getField("deductee_key")).get(0);
						panValidationStatus = deducteeDetailData.getPanValidationStatus();
						gstinNumber = deducteeDetailData.getGstinNumber();
						deducteeStatus = deducteeDetailData.getDeducteeStatus();
						aadharNo = deducteeDetailData.getAdharNo();
						tdsApplicabilityUnderSection = deducteeDetailData.getTdApplicabilityUnderSection();
						grOrIRIndicator = deducteeDetailData.getGrOrIRIndicator();

					} else if (row.getField("is_resident").equals("Y") && !deducteeNRMap.isEmpty()
							&& deducteeNRMap.get(row.getField("deductee_key")) != null) {
						DeducteeDetailsDTO deducteeDetailData = deducteeNRMap.get(row.getField("deductee_key")).get(0);
						panValidationStatus = deducteeDetailData.getPanValidationStatus();
						gstinNumber = deducteeDetailData.getGstinNumber();
						deducteeStatus = deducteeDetailData.getDeducteeStatus();
						aadharNo = deducteeDetailData.getAdharNo();
						tdsApplicabilityUnderSection = deducteeDetailData.getTdApplicabilityUnderSection();
						grOrIRIndicator = deducteeDetailData.getGrOrIRIndicator();
					}
					createSXSSFCell(style, row1, 7, panValidationStatus);
					createSXSSFCell(style, row1, 8, gstinNumber);
					createSXSSFCell(style, row1, 9, deducteeStatus);
					createSXSSFCell(style, row1, 10, aadharNo);
					createSXSSFCell(style, row1, 11, tdsApplicabilityUnderSection);
					String ldcFlag = row.getField("has_ldc") != null && row.getField("has_ldc") == "1" ? "Y" : "N";
					createSXSSFCell(style, row1, 12, ldcFlag);
					createSXSSFCell(style, row1, 13, row.getField(""));// TDS Applicability (with/without PAN/ITR)
					if (row.getField("is_resident").equals("N") && !resSectionMap.isEmpty()
							&& resSectionMap.get(row.getField("deductee_key")) != null) {
						sections = resSectionMap.get(row.getField("deductee_key")).stream()
								.collect(Collectors.toList());
					} else if (row.getField("is_resident").equals("Y") && !nrSectionMap.isEmpty()
							&& nrSectionMap.get(row.getField("deductee_key")) != null) {
						sections = nrSectionMap.get(row.getField("deductee_key")).stream().collect(Collectors.toList());
					}
					createSXSSFCell(style, row1, 14, sections.size() + "");
					createSXSSFCell(style, row1, 15,
							sections.isEmpty() ? "No Section" : sections.toString().replace("[", "").replace("]", ""));
					String postingDocDate = row.getField("posting_date_of_document");
					Date postingDate = new SimpleDateFormat("yyyy-MM-dd").parse(postingDocDate);
					if (StringUtils.isNotBlank(row.getField("pan"))) {
						Set<String> ldcSections = new HashSet<>();
						if (!ldcMap.isEmpty()) {
							List<LdcMasterDTO> ldcSetion = ldcMap.get(row.getField("pan"));
							if (ldcSetion != null && !ldcSetion.isEmpty()) {
								for (LdcMasterDTO ldc : ldcSetion) {
									if (ldc.getApplicableFrom().getTime() <= (postingDate.getTime())
											&& ldc.getApplicableTo().getTime() >= (postingDate.getTime())) {
										ldcSections.add(ldc.getSection());
									}
								}
							}
						}
						createSXSSFCell(style, row1, 16,
								ldcSections.isEmpty() ? "" : ldcSections.toString().replace("[", "").replace("]", ""));
					} else {
						createSXSSFCell(style, row1, 16, "");
					}
					createSXSSFCell(style, row1, 17, "");// VendorInvoiceNumber
					createSXSSFCell(style, row1, 18, row.getField("document_date"));
					createSXSSFCell(style, row1, 19, row.getField("document_number"));
					createSXSSFCell(style, row1, 20, row.getField("posting_date_of_document"));
					createSXSSFCell(style, row1, 21, row.getField("document_type"));
					createSXSSFCell(style, row1, 22, row.getField("supply_type"));
					createSXSSFCell(style, row1, 23, row.getField("document_type"));
					createSXSSFCell(style, row1, 24, row.getField("line_item_number"));
					createSXSSFCell(style, row1, 25, row.getField("original_document_number"));
					createSXSSFCell(style, row1, 26, "");// OriginalDocumentDate
					createSXSSFCell(style, row1, 27, row.getField("hsn_or_sac"));
					createSXSSFCell(style, row1, 28, row.getField("sac_description"));
					createSXSSFCell(style, row1, 29, row.getField("service_description_invoice"));
					createSXSSFCell(style, row1, 30, row.getField("gl_account_code"));
					createSXSSFCell(style, row1, 31, row.getField(""));// GLAccountName
					createSXSSFCell(style, row1, 32, row.getField("po_number"));
					createSXSSFCell(style, row1, 33, row.getField("po_line_item_number"));
					createSXSSFCell(style, row1, 34, row.getField("po_date"));
					createSXSSFCell(style, row1, 35, row.getField("service_description_po"));
					createSXSSFCell(style, row1, 36, row.getField("is_resident"));
					createSXSSFCell(style, row1, 37, row.getField("debit_credit_indicator"));
					createSXSSFCell(style, row1, 38, row.getField("amount"));
					createSXSSFCell(style, row1, 39, "");// InvoiceValue
					createSXSSFCell(style, row1, 40, row.getField("tds_base_value"));
					createSXSSFCell(style, row1, 41, row.getField("tds_tax_code_erp"));
					createSXSSFCell(style, row1, 42, row.getField("withholding_section"));
					createSXSSFCell(style, row1, 43, row.getField("withholding_rate"));
					createSXSSFCell(style, row1, 44, row.getField("withholding_amount"));
					createSXSSFCell(style, row1, 45, row.getField("client_effective_tds_rate"));
					createSXSSFCell(style, row1, 46, row.getField("derived_tds_section"));
					createSXSSFCell(style, row1, 47, row.getField("derived_tds_rate"));
					createSXSSFCell(style, row1, 48, row.getField("derived_tds_amount"));
					if (StringUtils.isNotBlank(row.getField("mismatch_category"))
							&& !"NAD".equalsIgnoreCase(row.getField("mismatch_category"))) {
						String mismatchCategory = row.getField("mismatch_category");
						createSXSSFCell(style, row1, 49, mismatchCategory.split("-")[0]);
						createSXSSFCell(style, row1, 50, mismatchCategory.split("-")[1]);
						createSXSSFCell(style, row1, 51, mismatchCategory);
					} else {
						createSXSSFCell(style, row1, 49, "");
						createSXSSFCell(style, row1, 50, "");
						createSXSSFCell(style, row1, 51, "");
					}
					String confidence = StringUtils.isNotBlank(row.getField("confidence")) ? row.getField("confidence")
							: "";
					createSXSSFCell(style, row1, 52, confidence);
					String sectionDeterminationData = row.getField("section_detremination_log").replace("'", "\"");
					if (StringUtils.isNotBlank(sectionDeterminationData)) {
						ObjectMapper objectMapper = new ObjectMapper();
						Map<String, Object> priorities = null;
						if (sectionDeterminationData != null) {
							priorities = objectMapper.readValue(sectionDeterminationData,
									new TypeReference<Map<String, Object>>() {
									});
						}
						if (priorities != null) {
							for (Map.Entry<String, Object> entry : priorities.entrySet()) {
								Map<String, Object> value = (HashMap<String, Object>) entry.getValue();
								String predictedSection = value.get("predected_tds_section") != null
										? value.get("predected_tds_section").toString()
										: "";
								if ("vendor_master".equalsIgnoreCase(entry.getKey())) {
									createSXSSFCell(style, row1, 53, predictedSection);
								} else if ("sac_description".equalsIgnoreCase(entry.getKey())) {
									createSXSSFCell(style, row1, 54, predictedSection);
								} else if ("service_description_po".equalsIgnoreCase(entry.getKey())) {
									createSXSSFCell(style, row1, 55, predictedSection);
								} else if ("service_description_invoice".equalsIgnoreCase(entry.getKey())) {
									createSXSSFCell(style, row1, 56, predictedSection);
								} else if ("advance_to_vendor_gl_account_description"
										.equalsIgnoreCase(entry.getKey())) {
									createSXSSFCell(style, row1, 57, predictedSection);
								}
							}
						}
					} else {
						createSXSSFCell(style, row1, 53, "");// Derived TDS Section-Vendor Master
						createSXSSFCell(style, row1, 54, "");// Derived TDS Section-HSN/SAC
						createSXSSFCell(style, row1, 55, "");// Derived TDS Section-PO desc
						createSXSSFCell(style, row1, 56, "");// Derived TDS Section-INV des
						createSXSSFCell(style, row1, 57, "");// Derived TDS Section-GL desc
					}
					createSXSSFCell(styleUnlocked, row1, 58, row.getField(""));// Action
					createSXSSFCell(styleUnlocked, row1, 59, row.getField(""));// Reason
					createSXSSFCell(styleUnlocked, row1, 60,
							StringUtils.isNotBlank(row.getField("final_tds_section"))
									? row.getField("final_tds_section")
									: "");// Final TDS Section
					createSXSSFCell(styleUnlocked, row1, 61,
							StringUtils.isNotBlank(row.getField("final_tds_rate"))
									&& new BigDecimal(row.getField("final_tds_rate")).compareTo(BigDecimal.ZERO) > 0
											? row.getField("final_tds_rate")
											: "");// Final TDS Rate
					createSXSSFCell(styleUnlocked, row1, 62,
							StringUtils.isNotBlank(row.getField("final_tds_amount"))
									&& new BigDecimal(row.getField("final_tds_amount")).compareTo(BigDecimal.ZERO) > 0
											? row.getField("final_tds_amount")
											: "");// Final TDS Amount
					createSXSSFCell(styleUnlocked, row1, 63, row.getField(""));// Any Other Amount
					createSXSSFCell(styleUnlocked, row1, 64, row.getField(""));
					createSXSSFCell(style, row1, 65, deductorMaster.getName());
					createSXSSFCell(style, row1, 66, deductorMaster.getPanField());
					createSXSSFCell(style, row1, 67, row.getField("business_place"));
					createSXSSFCell(style, row1, 68, row.getField("business_area"));
					createSXSSFCell(style, row1, 69, row.getField("plant"));
					createSXSSFCell(style, row1, 70, row.getField("profit_center"));
					createSXSSFCell(style, row1, 71, row.getField("assignment_number"));
					createSXSSFCell(style, row1, 72, row.getField("user_name"));
					createSXSSFCell(style, row1, 73, row.getField("payment_date"));
					createSXSSFCell(style, row1, 74, row.getField("tds_deduction_date"));
					createSXSSFCell(style, row1, 75, "");// MIGONumber
					createSXSSFCell(style, row1, 76, "");// MIRONumber
					createSXSSFCell(style, row1, 77, "");// IGSTRate
					createSXSSFCell(style, row1, 78, "");// IGSTAmount
					createSXSSFCell(style, row1, 79, "");// CGSTRate
					createSXSSFCell(style, row1, 80, "");// CGSTAmount
					createSXSSFCell(style, row1, 81, "");// SGSTRate
					createSXSSFCell(style, row1, 82, "");// SGSTAmount
					createSXSSFCell(style, row1, 83, getFormattedValue(row.getField("cess_rate")));
					createSXSSFCell(style, row1, 84, getFormattedValue(row.getField("cess_amount")));
					createSXSSFCell(style, row1, 85, "");// POS
					createSXSSFCell(style, row1, 86, "");// LinkedAdvanceIndicator
					createSXSSFCell(style, row1, 87, "");// LinkedProvisionIndicator
					createSXSSFCell(style, row1, 88, "");// ProvisionAdjustmentFlag
					createSXSSFCell(style, row1, 89, "");// AdvanceAdjustmentFlag
					createSXSSFCell(style, row1, 90, "");// ChallanPaidFlag
					createSXSSFCell(style, row1, 91, "");// ChallanProcessingDate
					createSXSSFCell(style, row1, 92, row.getField("grossup_indicator"));
					createSXSSFCell(style, row1, 93, "");
					createSXSSFCell(style, row1, 94, "");
					createSXSSFCell(style, row1, 95, "");
					createSXSSFCell(style, row1, 96, "");// ItemCode
					createSXSSFCell(style, row1, 97, row.getField("tds_remittance_date"));
					createSXSSFCell(style, row1, 98, grOrIRIndicator);
					createSXSSFCell(style, row1, 99, "");// TypeOfTransaction
					createSXSSFCell(style, row1, 100, "");// SAAnumber
					createSXSSFCell(style, row1, 101, "");// RefKey3
					createSXSSFCell(style, row1, 102, row.getField("user_defined_field_1"));
					createSXSSFCell(style, row1, 103, row.getField("user_defined_field_2"));
					createSXSSFCell(style, row1, 104, row.getField("user_defined_field_3"));
					createSXSSFCell(style, row1, 105, row.getField("user_defined_field_4"));
					createSXSSFCell(style, row1, 106, row.getField("user_defined_field_5"));
					createSXSSFCell(style, row1, 107, row.getField("user_defined_field_6"));
					createSXSSFCell(style, row1, 108, row.getField("user_defined_field_7"));
					createSXSSFCell(style, row1, 109, row.getField("user_defined_field_8"));
					createSXSSFCell(style, row1, 110, row.getField("user_defined_field_9"));
					createSXSSFCell(style, row1, 111, row.getField("user_defined_field_10"));
					createSXSSFCell(style, row1, 112, row.getField("source_identifiers"));
					createSXSSFCell(style, row1, 113, row.getField("source_file_name"));
					createSXSSFCell(style, row1, 114, row.getField("deductee_key"));
					Integer nrTransactionsMetaId = StringUtils.isNotBlank(row.getField("nr_transactions_meta_id"))
							? new Double(row.getField("nr_transactions_meta_id")).intValue()
							: 0;
					createSXSSFCell(style, row1, 115, nrTransactionsMetaId.toString());
					createSXSSFCell(style, row1, 116, row.getField("processed_from"));
					Integer id = (StringUtils.isNotBlank(row.getField("id")) ? new Double(row.getField("id")).intValue()
							: 0);
					createSXSSFCell(style, row1, 117, id.toString());
					createSXSSFCell(style, row1, 118, row.getField("challan_month"));
					Integer year = (StringUtils.isNotBlank(row.getField("assessment_year"))
							? new Double(row.getField("assessment_year")).intValue()
							: 0);
					createSXSSFCell(style, row1, 119, year.toString());
					createSXSSFCell(style, row1, 120, row.getField("surcharge"));
					createSXSSFCell(style, row1, 121, row.getField("interest"));
					Integer groupId = (StringUtils.isNotBlank(row.getField("advance_groupid"))
							? new Double(row.getField("advance_groupid")).intValue()
							: 0);
					createSXSSFCell(style, row1, 122, groupId.toString());
					Integer nopId = (StringUtils.isNotBlank(row.getField("advance_npid"))
							? new Double(row.getField("advance_npid")).intValue()
							: 0);
					createSXSSFCell(style, row1, 123, nopId.toString());
				}
			}
			wb.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		}
	}

	private Map<String, String> getPriorities(String onboardingPriorities)
			throws JsonMappingException, JsonProcessingException {
		Map<String, String> priorities = null;
		ObjectMapper objectMapper = new ObjectMapper();
		if (onboardingPriorities != null) {
			priorities = objectMapper.readValue(onboardingPriorities, new TypeReference<Map<String, String>>() {
			});
		}
		return priorities;
	}

	private String getFormattedValue(String value) {
		return StringUtils.isNotBlank(value) && new BigDecimal(value).compareTo(BigDecimal.ZERO) > 0 ? value
				: StringUtils.EMPTY;
	}

	public String getMismatchReportMsg(String deductorName) {

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return "Advance Remediation Report (Dated: " + date + ")\n Client Name: " + deductorName + "\n";
	}

	/**
	 * 
	 * @param advance
	 * @param ldcMasterList
	 * @param ldcUtilizationList1
	 * @param ldcUtilization
	 * @return
	 */
	public AdvanceDTO ldcRateCalculationAll(AdvanceDTO advance, List<LdcMaster> ldcMasterList,
			List<LdcMasterUtilizationDTO> balanceList, List<LdcUtilization> ldcUtilizationList) {
		logger.info("Method execution started to perform ldc rate calculation {}");
		List<LdcMaster> ldcMasterResponse = new ArrayList<>();
		for (LdcMaster ldc : ldcMasterList) {
			if (ldc.getPan().equalsIgnoreCase(advance.getDeducteePan())
					&& ldc.getSection().equalsIgnoreCase(advance.getFinalTdsSection())
					&& ldc.getApplicableFrom().getTime() <= advance.getPostingDateOfDocument().getTime()
					&& ldc.getApplicableTo().getTime() >= advance.getPostingDateOfDocument().getTime()) {
				ldcMasterResponse.add(ldc);
			}
		}
		if (!ldcMasterResponse.isEmpty()) {
			LdcMaster ldcMaster = ldcMasterResponse.get(0);
			BigDecimal remainingBalance = BigDecimal.ZERO;
			for (LdcMasterUtilizationDTO ldcUtilization : balanceList) {
				if (ldcUtilization.getLdcMasterPan().equalsIgnoreCase(advance.getDeducteePan())
						&& ldcUtilization.getSection().equalsIgnoreCase(advance.getFinalTdsSection())
						&& ldcUtilization.getApplicableFrom().getTime() <= advance.getPostingDateOfDocument().getTime()
						&& ldcUtilization.getApplicableTo().getTime() >= advance.getPostingDateOfDocument().getTime()) {
					remainingBalance = ldcUtilization.getRemainingBalance();
				}
			}
			if (remainingBalance.compareTo(advance.getAmount()) == 1) {
				LdcUtilization ldcUtilization = new LdcUtilization();
				advance.setFinalTdsRate(ldcMaster.getRate());
				ldcUtilization.setAssessmentYear(advance.getAssessmentYear());
				ldcUtilization.setChallanMonth(advance.getChallanMonth());
				ldcUtilization.setDeductorMasterTan(advance.getDeductorMasterTan());
				ldcUtilization.setLdcMasterTotalAmount(ldcMaster.getAmount());
				ldcUtilization.setRemainingAmount(remainingBalance.subtract(advance.getAmount()));
				ldcUtilization.setUtilizedAmount(advance.getAmount());
				ldcUtilization.setActive(true);
				ldcUtilization.setConsumedFrom(UploadTypes.ADVANCE.name());
				ldcUtilization.setCreatedDate(new Date());
				ldcUtilization.setLdcMasterId(ldcMaster.getLdcMasterID());
				ldcUtilization.setLdcMasterPan(ldcMaster.getPan());
				ldcUtilization.setModifiedDate(new Date());
				ldcUtilization.setLdcMasterLdcApplicableFrom(ldcMaster.getApplicableFrom());
				ldcUtilization.setLdcMasterLdcApplicableTo(ldcMaster.getApplicableTo());
				ldcUtilization.setLdcMasterTotalAmount(ldcMaster.getAmount());
				ldcUtilization.setAdvanceId(advance.getId());
				ldcUtilization.setInvoiceProcessDate(advance.getPostingDateOfDocument());
				ldcUtilizationList.add(ldcUtilization);
				logger.info("ldcRateCalculation method execution succeded , record saved in ldc utilization {}",
						ldcUtilization);
			}
		}

		return advance;
	}

	private void createSXSSFCell(XSSFCellStyle style, XSSFRow row, int cellNumber, String value) {
		row.createCell(cellNumber).setCellValue(StringUtils.isBlank(value) == true ? StringUtils.EMPTY : value);
		row.getCell(cellNumber).setCellStyle(style); // K
	}

	private void createSXSSFCell(XSSFCellStyle style, SXSSFRow row, int cellNumber, String value) {
		row.createCell(cellNumber).setCellValue(StringUtils.isBlank(value) == true ? StringUtils.EMPTY : value);
		row.getCell(cellNumber).setCellStyle(style); // K
	}

	/**
	 * 
	 * @param tenantId
	 * @param userName
	 * @param deductorPan
	 * @param deductorTan
	 * @param type
	 * @return
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvalidKeyException
	 */
	@Async
	public BatchUpload downloadPoExcle(String tenantId, String userName, String deductorPan, String deductorTan,
			String uiType) throws InvalidKeyException, IOException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		int year = CommonUtil.getAssessmentYear(null);
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		String fileName = "ADVANCE_PO_REPORT";
		String uploadType = StringUtils.EMPTY;
		String isResident = StringUtils.EMPTY;
		if ("ADVANCE_NR_EXCEL".equalsIgnoreCase(uiType)) {
			uploadType = UploadTypes.NR_ADVANCE_NO_PO_REPORT.name();
			isResident = "Y";
		} else if ("ADVANCES".equalsIgnoreCase(uiType)) {
			uploadType = UploadTypes.PO_UPDATE_DOWNLOAD.name();
			isResident = "N";
		}
		BatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, year, null, 0L, uploadType, "Processing",
				month, userName, null, fileName);
		batchUpload = downloadPoExcelProcess(batchUpload, tenantId, userName, deductorPan, deductorTan, year, month,
				isResident, uploadType);
		return batchUpload;

	}

	/**
	 * 
	 * @param batchUpload
	 * @param type
	 * @param uploadType
	 * @return
	 * @throws IOException
	 */
	private BatchUpload downloadPoExcelProcess(BatchUpload batchUpload, String tenantId, String userName,
			String deductorPan, String deductorTan, int year, int month, String isResident, String uploadType)
			throws IOException {
		List<AdvanceDTO> advanceList = advanceDAO.getAllAdvancePoData(deductorPan, deductorTan, isResident);
		int count = advanceList.size();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = resourceLoader.getResource("classpath:templates/" + "advance_po_template.xlsx");
		InputStream input = resource.getInputStream();
		try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
			XSSFSheet sheet = wb.getSheetAt(0);
			sheet.protectSheet("password");
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);
			String msg = StringUtils.EMPTY;
			if ("N".equalsIgnoreCase(isResident)) {
				msg = getErrorReportMsgAdvance(tenantId, deductorPan, "Resident Advance PO Report");
			} else if ("Y".equalsIgnoreCase(isResident)) {
				msg = getErrorReportMsgAdvance(tenantId, deductorPan, "Non-Resident Advance PO Report");
			}

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

			XSSFCellStyle style2 = (XSSFCellStyle) wb.createCellStyle();
			style2.setFont(fonts);
			style2.setWrapText(true);
			style2.setFont(fonts2);
			style2.setLocked(false);

			XSSFRow row0 = sheet.createRow(0);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);

			int rowindex = 4;
			int squenceNumber = 1;

			for (AdvanceDTO advance : advanceList) {
				XSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
				createSXSSFCell(style1, row1, 0, String.valueOf(squenceNumber++));
				createSXSSFCell(style1, row1, 1, advance.getDeducteeCode());
				createSXSSFCell(style1, row1, 2, advance.getDeducteeName());
				createSXSSFCell(style1, row1, 3, advance.getDeducteePan());
				createSXSSFCell(style1, row1, 4, advance.getDocumentNumber());
				createSXSSFCell(style1, row1, 5, advance.getDocumentDate().toString());
				createSXSSFCell(style1, row1, 6, advance.getPostingDateOfDocument().toString());
				createSXSSFCell(style1, row1, 7, advance.getServiceDescription());
				if (advance.getAmount() != null) {
					int val = advance.getAmount().compareTo(BigDecimal.ZERO);
					createSXSSFCell(style1, row1, 8,
							val == 1 ? advance.getAmount().setScale(2, RoundingMode.UP).toString() : "");
				} else {
					createSXSSFCell(style2, row1, 8, "");
				}
				createSXSSFCell(style1, row1, 9, advance.getFinalTdsSection());
				if (advance.getAmount() != null) {
					int val = advance.getFinalTdsRate().compareTo(BigDecimal.ZERO);
					createSXSSFCell(style1, row1, 10,
							val == 1 ? advance.getFinalTdsRate().setScale(2, RoundingMode.UP).toString() : "");
				} else {
					createSXSSFCell(style2, row1, 10, "");
				}
				if (advance.getAmount() != null) {
					int val = advance.getFinalTdsAmount().compareTo(BigDecimal.ZERO);
					createSXSSFCell(style1, row1, 11,
							val == 1 ? advance.getFinalTdsAmount().setScale(2, RoundingMode.UP).toString() : "");
				} else {
					createSXSSFCell(style2, row1, 11, "");
				}
				createSXSSFCell(style2, row1, 12, advance.getPoNumber());
				// hidden columns
				createSXSSFCell(style1, row1, 13, advance.getId().toString());

			}
			sheet.setColumnHidden(13, true);
			wb.write(out);
			batchUpload = saveMatrixReport(deductorTan, tenantId, year, out, Long.valueOf(count), uploadType,
					"Processed", month, userName, batchUpload.getBatchUploadID(), null);
		} catch (Exception e) {
			logger.error("Advances no po report error is:{}", e.getMessage());
		}
		return batchUpload;
	}

	public String getErrorReportMsgAdvance(String tenantId, String deductorPan, String fileType) {
		MultiTenantContext.setTenantId(tenantId);
		// feign client to get Deductor Name
		DeductorMasterDTO getDeductor = onboardingClient.getDeductorByPan(deductorPan, tenantId).getBody().getData();
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return fileType + " (Dated: " + date + ")\n Client Name: " + getDeductor.getDeductorName() + "\n";
	}

	/**
	 * 
	 * @param tan
	 * @param batchUpload
	 * @param path
	 * @param tenantId
	 * @param deductorPan
	 * @param token
	 * @param assessmentYear
	 * @param userEmail
	 * @param month
	 * @return
	 */
	@Async
	public BatchUpload asyncUpdatePORemediationReport(String tan, BatchUpload batchUpload, String path, String tenantId,
			String deductorPan, String token, int assessmentYear, String userEmail, int month) {
		MultiTenantContext.setTenantId(tenantId);
		if (batchUpload != null) {
			batchUpload.setFailedCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setRowsCount(0L);
			batchUpload.setStatus("Processing");
			batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
			batchUploadDAO.update(batchUpload);
			batchUpload = poUpdateRemediationReport(tan, batchUpload, path, tenantId, deductorPan, assessmentYear,
					userEmail);
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param tan
	 * @param batchUpload
	 * @param path
	 * @param tenantId
	 * @param deductorPan
	 * @param assessmentYear
	 * @param userEmail
	 * @return
	 */
	private BatchUpload poUpdateRemediationReport(String tan, BatchUpload batchUpload, String path, String tenantId,
			String deductorPan, int assessmentYear, String userEmail) {
		Biff8EncryptionKey.setCurrentUserPassword("password");
		Workbook workbook;
		try {
			logger.info("Mismatch file path : {}", path);
			File file = blobStorageService.getFileFromBlobUrl(tenantId, path);
			workbook = new Workbook(file.getAbsolutePath());
			Worksheet worksheet = workbook.getWorksheets().get(0);
			worksheet.getCells().deleteRows(0, 3);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.CSV);
			File xlsxInvoiceFile = new File("TestCsvFile");
			FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
			CsvReader csvReader = new CsvReader();
			csvReader.setContainsHeader(true);
			CsvContainer csv = csvReader.read(xlsxInvoiceFile, StandardCharsets.UTF_8);
			List<AdvanceDTO> advanceList = new ArrayList<>();
			for (CsvRow row : csv.getRows()) {
				if (StringUtils.isNotBlank(StringUtils.join(row.getFields(), ", ").replace(",", ""))) {
					// po number
					String poNumber = row.getField("PO Number");
					// advance Id
					Integer advanceId = StringUtils.isNotBlank(row.getField("ID"))
							? Integer.parseInt(row.getField("ID"))
							: null;
					if (StringUtils.isNotBlank(poNumber)) {
						AdvanceDTO advanceData = new AdvanceDTO();
						if (advanceId != null) {
							advanceData.setId(advanceId);
							advanceData.setPoNumber(poNumber);
							advanceList.add(advanceData);
						}
					}
				}
			}

			// batch update advance po date and po number recodes
			if (!advanceList.isEmpty()) {
				advanceDAO.batchUpdateAdvacnePoRecords(advanceList);
			}

			if (batchUpload.getBatchUploadID() != null) {
				batchUpload.setFilePath(batchUpload.getFilePath());
				batchUpload.setFailedCount(0L);
				batchUpload.setProcessedCount(advanceList.size());
				batchUpload.setStatus("Processed");
				batchUpload.setRowsCount(Long.valueOf(advanceList.size()));
				batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
				batchUploadDAO.update(batchUpload);
			}
		} catch (Exception e1) {
			logger.error("Exception occurred while updating advance po remediation report {}", e1.getMessage());

		}
		return batchUpload;
	}
}
