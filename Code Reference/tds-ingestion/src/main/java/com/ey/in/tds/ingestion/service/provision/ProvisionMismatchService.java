package com.ey.in.tds.ingestion.service.provision;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
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
import com.ey.in.tds.common.domain.transactions.jdbc.dao.LDCUtilizationDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.LdcMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.NrTransactionsMetaDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionUtilizationDTO;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.CustomSectionRateDTO;
import com.ey.in.tds.common.dto.CustomThresholdGroupLimitDto;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.model.job.NoteBookParam;
import com.ey.in.tds.common.model.provision.MatrixDTO;
import com.ey.in.tds.common.model.provision.MatrixFileDTO;
import com.ey.in.tds.common.model.provision.ProvisionMismatchByBatchIdDTO;
import com.ey.in.tds.common.model.provision.UtilizationFileDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeDetailsDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMasterDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcUtilization;
import com.ey.in.tds.common.onboarding.jdbc.dto.NrTransactionsMeta;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
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
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceLineItemDAO;
import com.ey.in.tds.ingestion.jdbc.dao.ProvisionDAO;
import com.ey.in.tds.ingestion.jdbc.dao.ProvisionMismatchDAO;
import com.ey.in.tds.ingestion.jdbc.dao.ProvisionUtilizationDAO;
import com.ey.in.tds.ingestion.service.advance.AdvanceService;
import com.ey.in.tds.ingestion.service.advance.SparkNotebookService;
import com.ey.in.tds.ingestion.service.tdsmismatch.TdsMismatchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

@Service
public class ProvisionMismatchService extends TdsMismatchService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private AdvanceService advanceService;

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	private ProvisionDAO provisionDAO;

	@Autowired
	private AdvanceDAO advanceDAO;

	@Autowired
	private LDCUtilizationDAO lDCUtilizationDAO;

	@Autowired
	private ProvisionUtilizationDAO provisionUtilizationDAO;

	@Autowired
	private ProvisionMismatchDAO provisionMismatchDAO;

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

	/**
	 * Update UpdateOnScreenDTO for remediation report
	 * 
	 * @param provisionMismatchUpdateDTO
	 * @throws RecordNotFoundException
	 */
	public UpdateOnScreenDTO updateMismatchByAction(String tan, UpdateOnScreenDTO provisionMismatchUpdateDTO)
			throws RecordNotFoundException {

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
		for (UpdateOnScreenDTO provisonMismatch : provisionMismatchUpdateDTO.getData()) {
			Integer nopId = 0;
			Integer nopGroupId = 0;
			List<ProvisionDTO> provisonResponse = provisionDAO.findByYearTanDocumentPostingDateId(
					provisionMismatchUpdateDTO.getAssessmentYear(), tan, provisonMismatch.getDocumentPostingDate(),
					provisonMismatch.getId());

			if (!provisonResponse.isEmpty()) {
				ProvisionDTO provison = provisonResponse.get(0);
				provison.setFinalTdsSection(provisonMismatch.getFinalSection());
				provison.setFinalTdsRate(provisonMismatch.getFinalRate());
				if ("ACCEPT".equalsIgnoreCase(provisionMismatchUpdateDTO.getActionType())) {
					provison.setFinalTdsAmount(provison.getDerivedTdsAmount());
				} else if ("REJECT".equalsIgnoreCase(provisionMismatchUpdateDTO.getActionType())) {
					provison.setFinalTdsAmount(provison.getClientAmount());
				} else if (provisonMismatch.getFinalAmount() != null
						&& provisonMismatch.getFinalAmount().compareTo(BigDecimal.ZERO) > 0) {
					provison.setFinalTdsAmount(provisonMismatch.getFinalAmount());
				} else {
					BigDecimal finalAmount = provisonMismatch.getTaxableValue().multiply(provison.getFinalTdsRate())
							.divide(BigDecimal.valueOf(100));
					provison.setFinalTdsAmount(finalAmount);

				}
				if (UploadTypes.PROVISION_NR_EXCEL.name().equalsIgnoreCase(provison.getProcessedFrom())
						&& !"Cancel".equalsIgnoreCase(provisonMismatch.getActionType())
						&& !"ACCEPT".equalsIgnoreCase(provisonMismatch.getActionType()) && provison.getHasDtaa() != null
						&& provison.getHasDtaa().equals(false)) {
					List<NrTransactionsMeta> nrTransactionList = nrTransactionsMetaDAO.findById(tan,
							provisionMismatchUpdateDTO.getAssessmentYear(), provison.getNrTransactionsMetaId());
					if (!nrTransactionList.isEmpty()) {
						NrTransactionsMeta nrTransaction = nrTransactionList.get(0);
						provison.setSurcharge(surchargeCalculation(provison.getFinalTdsSection(),
								provison.getFinalTdsAmount(), provison.getProvisionalAmount(),
								nrTransaction.getDeducteeStatus(), surchargeMap));
						BigDecimal cessAmount = provison.getSurcharge().add(provison.getFinalTdsAmount());
						provison.setCessAmount(
								cessAmount.multiply(provison.getCessRate()).divide(BigDecimal.valueOf(100)));
					}
				}
				provison.setMismatch(false);
				provison.setModifiedDate(new Date());
				provison.setActive(true);
				provison.setReason(provisionMismatchUpdateDTO.getReason());
				provison.setActionType(provisionMismatchUpdateDTO.getActionType());
				provison.setIsExempted(false);

				if (!"Cancel".equalsIgnoreCase(provisionMismatchUpdateDTO.getActionType())
						&& !"Accept".equalsIgnoreCase(provisionMismatchUpdateDTO.getActionType())) {
					if (ratesMap != null && ratesMap.get(provison.getFinalTdsSection()) != null) {
						Double closestRate = closest(provison.getFinalTdsRate().doubleValue(),
								ratesMap.get(provison.getFinalTdsSection()));
						BigInteger nopIdInt = sectionRateNopId.get(provison.getFinalTdsSection() + "-" + closestRate);
						nopId = nopIdInt.compareTo(BigInteger.ZERO) > 0 ? nopIdInt.intValue() : 0;
					}
					if (nopId != null) {
						provison.setProvisionNpId(nopId);
						nopGroupId = nopGroupMap.get(nopId);
					}
					if (nopGroupId != null) {
						provison.setProvisionGroupid(nopGroupId);
					}
				}

				if ("IMPG".equalsIgnoreCase(provison.getSupplyType())) {
					if ("194Q".equalsIgnoreCase(provisonMismatch.getFinalSection())
							|| "NOTDS".equalsIgnoreCase(provisonMismatch.getFinalSection())
							|| StringUtils.isBlank(provisonMismatch.getFinalSection())) {
						provison.setIsExempted(true);
						provison.setErrorReason("Transaction out of Scope - Import of goods");
					}
				} else if (provison.getDeducteePan().equalsIgnoreCase(provison.getDeductorPan())) {
					if ("NOTDS".equalsIgnoreCase(provisonMismatch.getFinalSection())
							|| StringUtils.isBlank(provisonMismatch.getFinalSection())) {
						provison.setIsExempted(true);
						provison.setErrorReason("Transaction out of Scope - Stock Transfer");
					}
				} else if ("DLC".equalsIgnoreCase(provison.getDocumentType())) {
					if ("NOTDS".equalsIgnoreCase(provisonMismatch.getFinalSection())
							|| StringUtils.isBlank(provisonMismatch.getFinalSection())) {
						provison.setIsExempted(true);
						provison.setErrorReason("Transaction out of Scope - Delivery Challan");
					}
				} else if ("NOTDS".equalsIgnoreCase(provisonMismatch.getFinalSection())
						|| StringUtils.isBlank(provisonMismatch.getFinalSection())) {
					provison.setIsExempted(true);
					provison.setErrorReason("Transaction out of Scope - Scrape Sale");
				}
				logger.info("final Object Update --- : {}", provison);
				provisionDAO.update(provison);
			} else {
				logger.error("No Record for Invoice Line item to Update");
				throw new CustomException("No Record for Invoice Line item to Update",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		logger.info("Update mismatch by action  : {}", provisionMismatchUpdateDTO);
		return provisionMismatchUpdateDTO;
	}

	/**
	 * 
	 * @param deductorTan
	 * @param batchUploadId
	 * @return
	 */
	public List<ProvisionMismatchByBatchIdDTO> getProvisionMisMatchSummaryById(String deductorTan,
			Integer batchUploadId) {
		List<ProvisionMismatchByBatchIdDTO> listMisMatchAllDTO = new ArrayList<>();

		listMisMatchAllDTO.add(groupMismatchesByType(deductorTan, "SM-RMM", batchUploadId, 0, 0));
		listMisMatchAllDTO.add(groupMismatchesByType(deductorTan, "SMM-RMM", batchUploadId, 0, 0));
		listMisMatchAllDTO.add(groupMismatchesByType(deductorTan, "SMM-RM", batchUploadId, 0, 0));
		listMisMatchAllDTO.add(groupMismatchesByType(deductorTan, "NAD", batchUploadId, 0, 0));

		return listMisMatchAllDTO;
	}

	public CommonDTO<ProvisionDTO> getProvisionMisMatchById(String deductorTan, Integer batchUploadId,
			String mismatchCategory, Pagination pagination) {

		BigInteger count = BigInteger.ZERO;
		List<ProvisionDTO> listMisMatch = provisionDAO.getAllProvisionMisMatchListByTanYearMismatchCategoryBatchId(
				deductorTan, mismatchCategory, batchUploadId, pagination);
		count = provisionDAO.getcountOfProvisionMisMatchListByTanYearMismatchCategoryBatchId(deductorTan,
				mismatchCategory, batchUploadId);

		PagedData<ProvisionDTO> pagedData = new PagedData<>(listMisMatch, listMisMatch.size(),
				pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		CommonDTO<ProvisionDTO> provisionData = new CommonDTO<>();
		provisionData.setResultsSet(pagedData);
		provisionData.setCount(count);
		return provisionData;

	}

	private ProvisionMismatchByBatchIdDTO groupMismatchesByType(String tan, String category, Integer batchId,
			int assessmentYear, int assessmentMonth) {
		return provisionDAO.getProvisionMismatchSummary(assessmentYear, assessmentMonth, tan, batchId, category);
	}

	public List<ProvisionMismatchByBatchIdDTO> getAllProvisionMisMatchSummary(String deductorTan, int assessmentYear,
			int assessmentMonth) {

		List<ProvisionMismatchByBatchIdDTO> listMisMatchAllDTO = new ArrayList<>();

		listMisMatchAllDTO.add(groupMismatchesByType(deductorTan, "SM-RMM", null, assessmentYear, assessmentMonth));
		listMisMatchAllDTO
				.add(groupMismatchesByType(deductorTan, "SMM-RMM", null, assessmentYear, assessmentMonth));
		listMisMatchAllDTO.add(groupMismatchesByType(deductorTan, "SMM-RM", null, assessmentYear, assessmentMonth));
		listMisMatchAllDTO.add(groupMismatchesByType(deductorTan, "NAD", null, assessmentYear, assessmentMonth));

		return listMisMatchAllDTO;

	}

	public CommonDTO<ProvisionDTO> getAllProvisionMisMatch(String deductorTan, String mismatchCategory, int year,
			int month, MismatchesFiltersDTO filters) throws JsonMappingException, JsonProcessingException {
		CommonDTO<ProvisionDTO> provisionData = new CommonDTO<>();

		List<ProvisionDTO> resultedList = new ArrayList<>();
		List<ProvisionDTO> listMisMatch = provisionDAO.getProvisionDetailsByTanActiveAndMismatchCategory(year, month,
				deductorTan, mismatchCategory, filters);
		for (ProvisionDTO dto : listMisMatch) {
			List<String> sections = advanceDAO.getVendorSections(dto.getDeducteeKey(), dto.getIsResident());
			dto.setVendorsectionCount((long) sections.size());
			dto.setVendorsectionList(sections.isEmpty() ? Arrays.asList("No Section") : sections);
			resultedList.add(dto);
		}

		PagedData<ProvisionDTO> pagedData = null;
		if (!resultedList.isEmpty()) {
			pagedData = new PagedData<ProvisionDTO>(resultedList, resultedList.size(),
					filters.getPagination().getPageNumber(),
					resultedList
							.size() > (filters.getPagination().getPageSize() * filters.getPagination().getPageNumber())
									? false
									: true);
		}
		provisionData.setResultsSet(pagedData);
		provisionData.setCount(provisionDAO.getProvisionDetailsCountByTanMismatchCategory(year, month, deductorTan,
				mismatchCategory, filters));
		return provisionData;
	}

	/**
	 * 
	 * @param deductorTan
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
	@Async
	public ByteArrayInputStream asyncExportProvisionMismatchReport(String deductorTan, String tenantId,
			String deductorPan, int year, int month, String userName, boolean isMismatch)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		return exportProvisionMismatchReport(deductorTan, tenantId, deductorPan, year, month, userName, isMismatch);
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
	public ByteArrayInputStream exportProvisionMismatchReport(String tan, String tenantId, String deductorPan, int year,
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
			fileName = UploadTypes.PROVISION_REPORT.name().toLowerCase() + "_"
					+ CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		} else {
			fileName = UploadTypes.PROVISION_MISMATCH_REPORT.name().toLowerCase() + "_"
					+ CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		}
		BatchUpload batchUpload = saveMismatchReport(tan, tenantId, year, out, 0L,
				UploadTypes.PROVISION_MISMATCH_REPORT.name(), "Processing", month, userName, null, fileName);
		String type = "provision_mismatch";
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

	public ByteArrayInputStream exportProvisionMismatchReport1(String deductorTan, String tenantId, String deductorPan,
			int year, int month, String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();
		String fileName = UploadTypes.PROVISION_MISMATCH_REPORT.name() + "_"
				+ CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		BatchUpload batchUpload = saveMismatchReport(deductorTan, tenantId, year, null, 0L,
				UploadTypes.PROVISION_MISMATCH_REPORT.name(), "Processing", month, userName, null, fileName);
		try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
			SXSSFSheet sheet = wb.createSheet("Provision_Remediation_Report");

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
			String msg = getErrorReportMsg(deductorTan, tenantId, deductorPan);
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

			// setting border and color
			setMediumBorder(defaultIndexedColorMap, style0, 46, 134, 193);
			setMediumBorder(defaultIndexedColorMap, style1, 174, 170, 170);
			setMediumBorder(defaultIndexedColorMap, style2, 180, 198, 231);
			setMediumBorder(defaultIndexedColorMap, style3, 255, 255, 0);
			setMediumBorder(defaultIndexedColorMap, style003, 255, 230, 153);

			// setting fonts to style
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
			row1.createCell(2).setCellValue("Client TDS Section");
			row1.getCell(2).setCellStyle(style1);
			row1.createCell(3).setCellValue("Client TDS Rate");
			row1.getCell(3).setCellStyle(style1);
			row1.createCell(4).setCellValue("Derived Amount");
			row1.getCell(4).setCellStyle(style1);
			row1.createCell(5).setCellValue("Derived TDS Section");
			row1.getCell(5).setCellStyle(style1);
			row1.createCell(6).setCellValue("Derived TDS Rate");
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
			row1.createCell(17).setCellValue("Service Description - Invoice");
			row1.getCell(17).setCellStyle(style3);
			row1.createCell(18).setCellValue("Service Description - PO");
			row1.getCell(18).setCellStyle(style3);
			row1.createCell(19).setCellValue("Service Description - GL Text");
			row1.getCell(19).setCellStyle(style3);
			row1.createCell(20).setCellValue("Invoice Line  Hash Code");
			row1.getCell(20).setCellStyle(style1);
			row1.createCell(21).setCellValue("With Holding Section");
			row1.getCell(21).setCellStyle(style1);
			row1.createCell(22).setCellValue("Provision Id");
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
			sheet.setAutoFilter(new CellRangeAddress(4, 27, 0, 29));
			sheet.createFreezePane(0, 1);

			// sheet.createFreezePane(colSplit, rowSplit, leftmostColumn, topRow)-this
			// method is used for scrolling rows and column vertically and horizontally
			sheet.createFreezePane(4, 5, 5, 5);

			long size = provisionDAO.getProvisionMismatchCount(deductorTan.trim(), year, month);

			CellRangeAddressList addressList = new CellRangeAddressList(4, (int) size + 4, 25, 25);
			validationHelper = sheet.getDataValidationHelper();
			constraint = validationHelper
					.createExplicitListConstraint(new String[] { "Accept", "Modify", "Reject", "Cancel" });
			dataValidation = validationHelper.createValidation(constraint, addressList);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.setShowPromptBox(true);
			sheet.addValidationData(dataValidation);

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
			int pageNumer = 1;
			List<ProvisionDTO> provisionMismatchList = provisionDAO.getAllProvisionMismatches(deductorTan.trim(), year,
					month);

			if (!provisionMismatchList.isEmpty()) {

				for (ProvisionDTO listData : provisionMismatchList) {
					List<String> sections = new ArrayList<>();
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
					row2.createCell(4).setCellValue(listData.getDerivedTdsAmount().toString());
					row2.getCell(4).setCellStyle(style5);
					row2.createCell(5).setCellValue(listData.getDerivedTdsSection());
					row2.getCell(5).setCellStyle(style5);
					if (listData.getDerivedTdsRate() != null) {
						row2.createCell(6).setCellValue(listData.getDerivedTdsRate().toString());
						row2.getCell(6).setCellStyle(style5);
					}
					row2.createCell(7).setCellValue(listData.getProvisionalAmount().toString());
					row2.getCell(7).setCellStyle(style5);
					row2.createCell(8).setCellValue(
							listData.getWithholdingSection() == null ? " " : listData.getWithholdingSection());
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
					row2.createCell(20).setCellValue(listData.getInvoiceLineHashCode());
					row2.getCell(20).setCellStyle(style5);
					row2.createCell(21).setCellValue(listData.getWithholdingSection());
					row2.getCell(21).setCellStyle(style5);
					row2.createCell(22).setCellValue(listData.getId().toString());
					row2.getCell(22).setCellStyle(style5);
					row2.createCell(23).setCellValue(listData.getConfidence());
					row2.getCell(23).setCellStyle(style5);
					row2.createCell(24).setCellValue(listData.getDocumentNumber());
					row2.getCell(24).setCellStyle(style5);

					// unlocked data row2.
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
				logger.info("Processed records : {}", pageNumer, provisionMismatchList.size());
				updateMismatchExportReportStatus(provisionMismatchList.size(), size, batchUpload);
			}
			wb.write(out);
			saveMismatchReport(deductorTan, tenantId, year, out, 1L, UploadTypes.PROVISION_MISMATCH_REPORT.name(),
					"Processed", month, userName, batchUpload.getBatchUploadID(), fileName);
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
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	@Async
	public BatchUpload asyncUpdateProvisionRemediationReport(String tan, BatchUpload batchUpload, String file,
			String tenantId, String deductorPan, int year, String userEmail)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		if (batchUpload != null) {
			batchUpload.setFailedCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setRowsCount(0L);
			batchUpload.setStatus("Processing");
			batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
			batchUploadDAO.update(batchUpload);
			batchUpload = updateMismatchRemediationReport(tan, batchUpload, file, tenantId, deductorPan, year,
					userEmail);
		}
		return batchUpload;
	}

	public BatchUpload updateMismatchRemediationReport(String tan,  BatchUpload batchUpload, String path,
			String tenantId, String deductorPan, int year, String userEmail)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		String otherFileurl = null;
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

			List<ProvisionDTO> provisionList = new ArrayList<ProvisionDTO>();
			List<CsvRow> errorList = new ArrayList<>();
			List<String> errorMessages = new ArrayList<>();
			List<Integer> provisionIds = new ArrayList<>();
			boolean isCancel = false;
			String errorFilepath = null;
			int processedCount = 0;
			int errorCount = 0;
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
			
			List<CustomThresholdGroupLimitDto> customThresholdGroupLimitList = mastersClient.getThresholdLimitGroup().getBody().getData();
			for (CustomThresholdGroupLimitDto customThresholdGroupLimitDTO : customThresholdGroupLimitList) {
				nopGroupMap.put(customThresholdGroupLimitDTO.getNopId().intValue(), customThresholdGroupLimitDTO.getGroupId().intValue());
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
					BigDecimal amountFromExcel = StringUtils.isNotBlank(row.getField("TaxableValue"))
							? new BigDecimal(row.getField("TaxableValue"))
							: null;
					// Action
					String userAction = row.getField("Action");
					// Reason
					String reason = row.getField("Reason");
					// Provision Id
					Integer provisionId = StringUtils.isNotBlank(row.getField("Line  Item Hash Code"))
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
					
					// provisionalAmount
					BigDecimal provisionalAmount = StringUtils.isNotBlank(row.getField("TDSBaseValue"))
							? new BigDecimal(row.getField("TDSBaseValue"))
							: null;
					// Supply type
					String supplyType = row.getField("SupplyType");
					
					// Document type
					String documentType = row.getField("DocumentType");
					
					Integer provisionGroupId = StringUtils.isNotBlank(row.getField("GroupId"))
							? Integer.parseInt(row.getField("GroupId"))
							: null;

					Integer provisionNopId = StringUtils.isNotBlank(row.getField("NopId"))
							? Integer.parseInt(row.getField("NopId"))
							: null;
							
					String deducteeStatus = StringUtils.isNotBlank(row.getField("DeducteeStatus"))
							? row.getField("DeducteeStatus")
							: StringUtils.EMPTY;
					
					Boolean isError = false;
					
					if (StringUtils.isNotBlank(userAction)) {
						ProvisionDTO provisionData = new ProvisionDTO();
						BigDecimal finalAmount = new BigDecimal(0);
						BigDecimal taxableAmount = new BigDecimal(0);
						Integer nopId = 0;
						Integer nopGroupId = 0;
						
						if (amountFromExcel != null) {
							taxableAmount = amountFromExcel;
						}
						
						if (provisionId != null) {
							provisionData.setPostingDateOfDocument(documentPostingDate);
							provisionData.setAssessmentYear(assessmentYear);
							provisionData.setId(provisionId);
							provisionData.setIsResident(isResidential);
							provisionData.setReason(reason);
							provisionData.setActionType(userAction);
							provisionData.setProcessedFrom(processedFrom);
							provisionData.setHasLdc(hasLdc);
							provisionData.setDeductorMasterTan(tan);
							provisionData.setDeducteePan(deducteePan);
							provisionData.setChallanMonth(challanMonth);
							provisionData.setProvisionalAmount(amountFromExcel);
							provisionData.setReason(reason);
							provisionData.setNrTransactionsMetaId(
									nrTransactionsMetaId != null ? nrTransactionsMetaId.intValue() : null);
							provisionData.setModifiedBy(userEmail);
							provisionData.setSurcharge(surcharge);
							provisionData.setInterest(interest);
							provisionData.setCessAmount(cessAmount);
							provisionData.setCessRate(cessRate);
							provisionData.setProvisionalAmount(provisionalAmount);
							provisionData.setIsExempted(false);
							provisionData.setProvisionNpId(provisionNopId);
							provisionData.setProvisionGroupid(provisionGroupId);
							if ("Accept".equalsIgnoreCase(userAction)) {
								String message = StringUtils.EMPTY;
								if (StringUtils.isNotBlank(row.getField("Mismatch Type"))
										&& row.getField("Mismatch Type").equals("NAD")) {
									isError = true;
									errorCount++;
									errorList.add(row);
									message = message + "Action column can not have value as Accept while Mismatch catagory Is NAD" + "\n";
								}
								if (derivedTdsSectionFromExcel != null && derivedTdsRateFromExcel != null
										&& derivedTdsAmountFromExcel != null) {
									provisionData.setFinalTdsSection(derivedTdsSectionFromExcel);
									provisionData.setFinalTdsRate(derivedTdsRateFromExcel);
									provisionData.setFinalTdsAmount(derivedTdsAmountFromExcel);
									provisionData.setActive(true);
									provisionData.setMismatch(false);
									if ("IMPG".equalsIgnoreCase(supplyType)) {
										if ("194Q".equalsIgnoreCase(provisionData.getFinalTdsSection())
												|| "NOTDS".equalsIgnoreCase(provisionData.getFinalTdsSection())
												|| StringUtils.isBlank(provisionData.getFinalTdsSection())) {
											provisionData.setIsExempted(true);
											provisionData
													.setErrorReason("Transaction out of Scope - Import of goods");
										}
									} else if (provisionData.getDeducteePan()
											.equalsIgnoreCase(provisionData.getDeductorPan())) {
										if ("NOTDS".equalsIgnoreCase(provisionData.getFinalTdsSection())
												|| StringUtils.isBlank(provisionData.getFinalTdsSection())) {
											provisionData.setIsExempted(true);
											provisionData.setErrorReason("Transaction out of Scope - Stock Transfer");
										}
									} else if ("DLC".equalsIgnoreCase(documentType)) {
										if ("NOTDS".equalsIgnoreCase(provisionData.getFinalTdsSection())
												|| StringUtils.isBlank(provisionData.getFinalTdsSection())) {
											provisionData.setIsExempted(true);
											provisionData
													.setErrorReason("Transaction out of Scope - Delivery Challan");
										}
									} else if ("NOTDS".equalsIgnoreCase(provisionData.getFinalTdsSection())
											|| StringUtils.isBlank(provisionData.getFinalTdsSection())) {
										provisionData.setIsExempted(true);
										provisionData.setErrorReason("Transaction out of Scope - Scrape Sale");
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
							} else if ("Modify".equalsIgnoreCase(userAction)) {
								if (finalTdsSectionFromExcel != null && finalTdsRateFromExcel != null) {
									provisionData.setReason(reason);
									provisionData.setFinalTdsRate(finalTdsRateFromExcel);
									provisionData.setFinalTdsSection(finalTdsSectionFromExcel);
									provisionData.setActive(true);
									provisionData.setMismatch(false);
									if (finalTdsAmountFromExcel != null) {
										provisionData.setFinalTdsAmount(finalTdsAmountFromExcel);
									} else {
										finalAmount = finalAmount.add(provisionData.getFinalTdsRate()
												.multiply(taxableAmount).divide(BigDecimal.valueOf(100)));
										provisionData.setFinalTdsAmount(finalAmount);
									}
									if ("IMPG".equalsIgnoreCase(supplyType)) {
										if ("194Q".equalsIgnoreCase(provisionData.getFinalTdsSection())
												|| "NOTDS".equalsIgnoreCase(provisionData.getFinalTdsSection())
												|| StringUtils.isBlank(provisionData.getFinalTdsSection())) {
											provisionData.setIsExempted(true);
											provisionData
													.setErrorReason("Transaction out of Scope - Import of goods");
										}
									} else if (provisionData.getDeducteePan()
											.equalsIgnoreCase(provisionData.getDeductorPan())) {
										if ("NOTDS".equalsIgnoreCase(provisionData.getFinalTdsSection())
												|| StringUtils.isBlank(provisionData.getFinalTdsSection())) {
											provisionData.setIsExempted(true);
											provisionData.setErrorReason("Transaction out of Scope - Stock Transfer");
										}
									} else if ("DLC".equalsIgnoreCase(documentType)) {
										if ("NOTDS".equalsIgnoreCase(provisionData.getFinalTdsSection())
												|| StringUtils.isBlank(provisionData.getFinalTdsSection())) {
											provisionData.setIsExempted(true);
											provisionData
													.setErrorReason("Transaction out of Scope - Delivery Challan");
										}
									} else if ("NOTDS".equalsIgnoreCase(provisionData.getFinalTdsSection())
											|| StringUtils.isBlank(provisionData.getFinalTdsSection())) {
										provisionData.setIsExempted(true);
										provisionData.setErrorReason("Transaction out of Scope - Scrape Sale");
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
								if((StringUtils.isBlank(actualTdsSectionFromExcel) || actualTdsRateFromExcel==null)  &&
										(actualTdsAmountFromExcel!=null && actualTdsAmountFromExcel.compareTo(BigDecimal.ZERO)==1)) {
									isError = true;
									errorCount++;
									errorList.add(row);
									if(StringUtils.isBlank(actualTdsSectionFromExcel)) {
									message = message + "Client Amount Is Greater Than Zero While Client Section Is Empty" + "\n";
									}
									if(actualTdsRateFromExcel==null) {
										message = message + "Client Amount Is Greater Than Zero While Client Rate Is Empty" + "\n";
									}
								}
								if (actualTdsSectionFromExcel != null && actualTdsRateFromExcel != null
										&& actualTdsAmountFromExcel != null) {
									provisionData.setFinalTdsSection(actualTdsSectionFromExcel);
									provisionData.setFinalTdsRate(actualTdsRateFromExcel);
									provisionData.setFinalTdsAmount(actualTdsAmountFromExcel);
									provisionData.setActive(true);
									provisionData.setMismatch(false);
									if ("IMPG".equalsIgnoreCase(supplyType)) {
										if ("194Q".equalsIgnoreCase(provisionData.getFinalTdsSection())
												|| "NOTDS".equalsIgnoreCase(provisionData.getFinalTdsSection())
												|| StringUtils.isBlank(provisionData.getFinalTdsSection())) {
											provisionData.setIsExempted(true);
											provisionData
													.setErrorReason("Transaction out of Scope - Import of goods");
										}
									} else if (provisionData.getDeducteePan()
											.equalsIgnoreCase(provisionData.getDeductorPan())) {
										if ("NOTDS".equalsIgnoreCase(provisionData.getFinalTdsSection())
												|| StringUtils.isBlank(provisionData.getFinalTdsSection())) {
											provisionData.setIsExempted(true);
											provisionData.setErrorReason("Transaction out of Scope - Stock Transfer");
										}
									} else if ("DLC".equalsIgnoreCase(documentType)) {
										if ("NOTDS".equalsIgnoreCase(provisionData.getFinalTdsSection())
												|| StringUtils.isBlank(provisionData.getFinalTdsSection())) {
											provisionData.setIsExempted(true);
											provisionData
													.setErrorReason("Transaction out of Scope - Delivery Challan");
										}
									} else if ("NOTDS".equalsIgnoreCase(provisionData.getFinalTdsSection())
											|| StringUtils.isBlank(provisionData.getFinalTdsSection())) {
										provisionData.setIsExempted(true);
										provisionData.setErrorReason("Transaction out of Scope - Scrape Sale");
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
									
								}
								errorMessages.add(message);
							} else if ("Cancel".equalsIgnoreCase(userAction)) {
								provisionData.setReason("USER REQUESTED TO CANCEL");
								provisionData.setActive(false);
								provisionData.setMismatch(false);
								provisionData.setErrorReason("Canceled record-ERR029");
								provisionIds.add(provisionId);
								isCancel = true;
							} else {
								errorCount++;
								errorList.add(row);
								isError = true;
							}
							if (!"Cancel".equalsIgnoreCase(userAction)  && !"Accept".equalsIgnoreCase(userAction)) {
								if (ratesMap != null && ratesMap.get(provisionData.getFinalTdsSection()) != null) {
									Double closestRate = closest(provisionData.getFinalTdsRate().doubleValue(),
											ratesMap.get(provisionData.getFinalTdsSection()));
									BigInteger nopIdInt = sectionRateNopId
											.get(provisionData.getFinalTdsSection() + "-" + closestRate);
									nopId = nopIdInt.compareTo(BigInteger.ZERO)>0 ? nopIdInt.intValue() : 0;
								}
								if (nopId != null) {
									provisionData.setProvisionNpId(nopId);
									nopGroupId = nopGroupMap.get(nopId);
								}
								if (nopGroupId != null) {
									provisionData.setProvisionGroupid(nopGroupId);
								}
							}
							if (!isError) {
								if (UploadTypes.PROVISION_NR_EXCEL.name()
										.equalsIgnoreCase(provisionData.getProcessedFrom())
										&& !"Cancel".equalsIgnoreCase(userAction)
										&& !"ACCEPT".equalsIgnoreCase(userAction) && provisionData.getHasDtaa() != null
										&& provisionData.getHasDtaa().equals(false)) {
									provisionData.setSurcharge(surchargeCalculation(provisionData.getFinalTdsSection(),
											provisionData.getFinalTdsAmount(), provisionData.getProvisionalAmount(),
											deducteeStatus, surchargeMap));
									cessAmount = provisionData.getSurcharge().add(provisionData.getFinalTdsAmount());
									provisionData.setCessAmount(
											cessAmount.multiply(cessRate).divide(BigDecimal.valueOf(100)));
								}
								provisionList.add(provisionData);
								processedCount++;
							}
						} else {
							errorList.add(row);
							errorMessages.add("provision mismatch id not found in system");
							errorCount++;
						}
					}
				}
			}

			// batch update advance mismatch recodes
			if (!provisionList.isEmpty()) {
				provisionDAO.batchUpdateProvisionMismatch(provisionList);
			}

			if(!ldcUtilizationList.isEmpty()) {
				lDCUtilizationDAO.batchUploadldcUtilizationSave(ldcUtilizationList, tenantId);
			}
			
			if (isCancel) {
				MultipartFile cancelledProvisionsFile = generateCancelledProvisionsExcell(provisionIds, tan, deductorPan);
				otherFileurl = blob.uploadExcelToBlob(cancelledProvisionsFile);
			}

			if (errorCount > 0 && !errorList.isEmpty()) {
				ByteArrayOutputStream bytes = generateMismatchErrorReport(errorList, tan, tenantId, deductorPan,errorMessages);
				if (bytes.size() != 0) {
					errorFilepath = sendFileToBlobStorage(bytes, tenantId);
				}
			}
			if (batchUpload.getBatchUploadID() != null) {
				batchUpload.setFilePath(batchUpload.getFilePath());
				batchUpload.setProcessedCount(processedCount);
				batchUpload.setFailedCount(Long.valueOf(errorCount));
				batchUpload.setErrorFilePath(errorFilepath);
				batchUpload.setStatus("Processed");
				batchUpload.setOtherFileUrl(otherFileurl);
				batchUpload.setRowsCount((long) processedCount + errorCount);
				batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
				batchUploadDAO.update(batchUpload);
			}
		} catch (Exception e1) {
			logger.error("Exception occurred while updating provision remediation report {}" + e1);
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
		File someFile = new File("Provision_RemediationError_Report.xlsx");
		try (FileOutputStream fos = new FileOutputStream(someFile)) {
			fos.write(bytes);
			fos.flush();
			fos.close();
			return someFile;
		}
	}

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
					.createExplicitListConstraint(new String[] { "Accept", "Modify", "Reject", "Cancel" });
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
					createSXSSFCell(style, row1, 25, row.getField(""));
					createSXSSFCell(style, row1, 26, "");// OriginalDocumentDate
					createSXSSFCell(style, row1, 27, row.getField("hsn_or_sacc"));
					createSXSSFCell(style, row1, 28, row.getField("hsnsac_description"));
					createSXSSFCell(style, row1, 29, row.getField(""));
					createSXSSFCell(style, row1, 30, row.getField("gl_account_code"));
					createSXSSFCell(style, row1, 31, row.getField(""));// GLAccountName
					createSXSSFCell(style, row1, 32, row.getField("po_number"));
					createSXSSFCell(style, row1, 33, row.getField(""));
					createSXSSFCell(style, row1, 34, row.getField("po_date"));
					createSXSSFCell(style, row1, 35, row.getField("service_description_po"));
					createSXSSFCell(style, row1, 36, row.getField("is_resident"));
					createSXSSFCell(style, row1, 37, row.getField(""));
					createSXSSFCell(style, row1, 38, row.getField("provisional_amount"));
					createSXSSFCell(style, row1, 39, "");// InvoiceValue
					createSXSSFCell(style, row1, 40, row.getField(""));
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
								} else if ("provision_gl_account_description".equalsIgnoreCase(entry.getKey())) {
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
					createSXSSFCell(style, row1, 67, row.getField(""));
					createSXSSFCell(style, row1, 68, row.getField(""));
					createSXSSFCell(style, row1, 69, row.getField(""));
					createSXSSFCell(style, row1, 70, row.getField(""));
					createSXSSFCell(style, row1, 71, row.getField(""));
					createSXSSFCell(style, row1, 72, row.getField(""));
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
					createSXSSFCell(style, row1, 92, row.getField(""));
					createSXSSFCell(style, row1, 93, "");
					createSXSSFCell(style, row1, 94, "");
					createSXSSFCell(style, row1, 95, "");
					createSXSSFCell(style, row1, 96, "");// ItemCode
					createSXSSFCell(style, row1, 97, row.getField(""));
					createSXSSFCell(style, row1, 98, grOrIRIndicator);
					createSXSSFCell(style, row1, 99,  "");// TypeOfTransaction
					createSXSSFCell(style, row1, 100, "");// SAAnumber
					createSXSSFCell(style, row1, 101, "");// RefKey3
					createSXSSFCell(style, row1, 102, row.getField("user_defined_field_1"));
					createSXSSFCell(style, row1, 103, row.getField("user_defined_field_2"));
					createSXSSFCell(style, row1, 104, row.getField("user_defined_field_3"));
					createSXSSFCell(style, row1, 105, row.getField("user_defined_field_4"));
					createSXSSFCell(style, row1, 106, row.getField(""));
					createSXSSFCell(style, row1, 107, row.getField(""));
					createSXSSFCell(style, row1, 108, row.getField(""));
					createSXSSFCell(style, row1, 109, row.getField(""));
					createSXSSFCell(style, row1, 110, row.getField(""));
					createSXSSFCell(style, row1, 111, row.getField(""));
					createSXSSFCell(style, row1, 112, row.getField("source_identifier"));
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
					Integer groupId = (StringUtils.isNotBlank(row.getField("provision_groupid"))
							? new Double(row.getField("provision_groupid")).intValue()
							: 0);
					createSXSSFCell(style, row1, 122, groupId.toString());
					Integer nopId = (StringUtils.isNotBlank(row.getField("provision_npid"))
							? new Double(row.getField("provision_npid")).intValue()
							: 0);
					createSXSSFCell(style, row1, 123, nopId.toString());
				}
			}
			wb.write(out);
			return new ByteArrayInputStream(out.toByteArray());
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
				createSXSSFCell(style, row1, 15, row.getField("Declaration Module - Rate Type"));// TDS Applicability (with/without PAN/ITR)
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
				createSXSSFCell(styleUnlocked, row1, 62,row.getField("Final TDS Section"));// Final TDS Section
				createSXSSFCell(styleUnlocked, row1, 63,row.getField("Final TDS Rate"));// Final TDS Rate
				createSXSSFCell(styleUnlocked, row1, 64,row.getField("Final TDS Rate"));// Final TDS Amount
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
	 * calculating provision matrix values
	 * 
	 * @param deductorTan
	 * @return
	 * @throws ParseException
	 */
	public Map<Integer, Object> getProvisionMatrix(String deductorTan, int yearFromUI, String type)
			throws ParseException {

		Map<Integer, Object> provisionMatrixValues = new HashMap<>();

		List<MatrixDTO> matrixValues = provisionDAO.getProvisionMatrix(deductorTan, yearFromUI, type);

		for (MatrixDTO matrixDTO : matrixValues) {
			provisionMatrixValues.put(matrixDTO.getMonth(), matrixDTO);
		}

		return provisionMatrixValues;

	}


	@Async
	public ByteArrayInputStream asyncProvisionMatrixOpeningAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, boolean isOpening, String tenantId, String userName, String type, int batchUploadYear)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return provisionMatrixClosingAmountDownload(deductorTan, assessmentYear, month, isOpening, tenantId, userName,
				type, batchUploadYear);
	}

	@Async
	public ByteArrayInputStream asyncProvisionMatrixClosingAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, boolean isOpening, String tenantId, String userName, String type) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return provisionMatrixClosingAmountDownload(deductorTan, assessmentYear, month, isOpening, tenantId, userName,
				type, assessmentYear);
	}

	public ByteArrayInputStream provisionMatrixClosingAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, boolean isOpening, String tenantId, String userName, String type, Integer batchUploadYear)
			throws Exception {
		logger.info("Closing provision amount download request for Year : {} Month : {}", assessmentYear, month);
		String fileName = "";
		if (UploadTypes.PROVISION_CLOSING_REPORT.name().equalsIgnoreCase(type)) {
			fileName = "YTD_Provision_Closing_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		} else {
			fileName = "YTD_Provision_Opening_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, batchUploadYear, null, 0L, type, "Processing",
				month, userName, null, fileName);
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Provision Matrix");

		String[] headerNames = new String[] { "Deductor Tan", "Opening Amount", "Adjusted Amount", "Closing Amount",
				"Deductee Pan", "Deductee Name", "Posting Date", "TDS Amount", "Closing TDS Amount", "Document Number",
				"Final Tds Section", "Final Tds Rate" };
		if (isOpening) {
			headerNames = new String[] { "Deductor Tan", "Opening Amount", "Deductee Pan", "Deductee Name",
					"Posting Date", "TDS Amount", "Opening TDS Amount", "Document Number", "Final Tds Section",
					"Final Tds Rate" };
		}

		worksheet.getCells().importArray(headerNames, 0, 0, false);
		// list of advance ftm amounts

		int rowIndex = 1;
		BigDecimal ftmSum = BigDecimal.valueOf(0);
		BigDecimal closingSum = BigDecimal.valueOf(0);
		BigDecimal adjustedSum = BigDecimal.valueOf(0);
		BigDecimal tdsAmountSum = BigDecimal.valueOf(0);
		BigDecimal openingTdsAmountSum = BigDecimal.valueOf(0);
		BigDecimal closingtTdsAmountSum = BigDecimal.valueOf(0);

		List<MatrixDTO> matrixValues = provisionDAO.getProvisionClosingMatrixReport(deductorTan, assessmentYear, month);

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
	public ByteArrayInputStream asyncProvisionMatrixFtmDownload(String deductorTan, Integer assessmentYear,
			Integer month, String tenantId, String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return provisionMatrixFtmDownload(deductorTan, assessmentYear, month, tenantId, userName);
	}

	public ByteArrayInputStream provisionMatrixFtmDownload(String deductorTan, int assessmentYear, int month,
			String tenantId, String userName) throws Exception {
		logger.info("Provision matrix ftm download request for Year : {} Month : {}", assessmentYear, month);
		String fileName = "FTM_Provision_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, assessmentYear, null, 0L,
				UploadTypes.PROVISION_FTM_REPORT.name(), "Processing", month, userName, null, fileName);
		// list of provision ftm amounts
		List<ProvisionDTO> provisionFTMs = provisionDAO.getProvisionFTM(deductorTan, assessmentYear, month);

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Provison Matrix FTM");
		ArrayList<MatrixFileDTO> matrixFileDTOList = new ArrayList<>();

		for (ProvisionDTO provision : provisionFTMs) {
			matrixFileDTOList.add(new MatrixFileDTO(provision.getDeductorPan(), provision.getDeductorTan(),
					provision.getDeductorGstin(), provision.getDeducteePan(), provision.getDeducteeTin(),
					provision.getDeducteeGstin(), provision.getProvisionalAmount(), new BigDecimal(0),
					provision.getFinalTdsAmount()));

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
		saveMatrixReport(deductorTan, tenantId, assessmentYear, out, 1L, UploadTypes.PROVISION_FTM_REPORT.name(),
				"Processed", month, userName, batchUpload.getBatchUploadID(), null);
		return new ByteArrayInputStream(out.toByteArray());
	}

	@Async
	public ByteArrayInputStream asyncProvisionMatrixAdjustedFileDownload(String deductorTan, Integer assessmentYear,
			Integer month, String tenantId, String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return provisionMatrixAdjustedFileDownload(deductorTan, assessmentYear, month, tenantId, userName);
	}

	public ByteArrayInputStream provisionMatrixAdjustedFileDownload(String deductorTan, int assessmentYear, int month,
			String tenantId, String userName) throws Exception {

		logger.info("Provision matrix adjusted download request for Year : {} Month : {}", assessmentYear, month);
		String fileName = "Adjusted_Provision_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, assessmentYear, null, 0L,
				UploadTypes.PROVISION_ADJUSTED_REPORT.name(), "Processing", month, userName, null, fileName);

		List<ProvisionUtilizationDTO> provisionAdjustments = provisionUtilizationDAO.getProvisionAdjustment(deductorTan,
				assessmentYear, month);

		ArrayList<UtilizationFileDTO> listUtilizationFileDTO = new ArrayList<>();

		for (ProvisionUtilizationDTO provisionUtilization : provisionAdjustments) {

			UtilizationFileDTO utilizationFileDTO = new UtilizationFileDTO();

			// get the data based on invoice line item id.
			List<InvoiceLineItem> invoiceLineItem = invoiceLineItemDAO
					.findByOnlyId(provisionUtilization.getInvoiceLineItemId());

			utilizationFileDTO.setDeductorMasterTan(provisionUtilization.getDeductorMasterTan());
			utilizationFileDTO.setMasterPan(provisionUtilization.getProvisionMasterPan());
			utilizationFileDTO.setRemainingAmount(provisionUtilization.getRemainingAmount());
			utilizationFileDTO.setUtilizedAmount(provisionUtilization.getUtilizedAmount());

			if (!invoiceLineItem.isEmpty()) {
				BeanUtils.copyProperties(invoiceLineItem.get(0), utilizationFileDTO);
			}

			listUtilizationFileDTO.add(utilizationFileDTO);
		}
		String[] provisionMatrixheaderNames = new String[] { "Deductor Master Tan", "Provision Master Pan",
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

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Provision Matrix Adjustments");
		worksheet.freezePanes(0, 4, 0, 4);
		worksheet.autoFitColumns();
		worksheet.autoFitRows();

		worksheet.getCells().importArray(provisionMatrixheaderNames, 0, 0, false);

		setPrivisionMatrixHeaders(listUtilizationFileDTO, worksheet);

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

		worksheet.autoFitColumns();

		// Style for E1 to BC1 headers
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
		saveMatrixReport(deductorTan, tenantId, assessmentYear, out, 1L, UploadTypes.PROVISION_ADJUSTED_REPORT.name(),
				"Processed", month, userName, batchUpload.getBatchUploadID(), null);
		return new ByteArrayInputStream(out.toByteArray());
	}

	public String getErrorReportMsg(String deductorMasterTan, String tenantId, String deductorPan) {
		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return "Provision Remediation Report (Dated: " + date + ")\n Client Name: " + deductorData.getDeductorName()
				+ "\n";
	}

	private void setPrivisionMatrixHeaders(ArrayList<UtilizationFileDTO> listUtilizationFileDTO, Worksheet worksheet)
			throws Exception {

		if (!listUtilizationFileDTO.isEmpty()) {
			int rowIndex = 1;
			for (UtilizationFileDTO utilizationFileDTO : listUtilizationFileDTO) {
				List<Object> rowData = new ArrayList<>();

				advanceService.setInvoiceDataForMatrixReports(utilizationFileDTO, rowData);

				worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
			}
		}
	}

	/**
	 * 
	 * @param type
	 * @param tan
	 * @param pagination
	 * @return
	 */
	public CommonDTO<ProvisionDTO> getListOfProvisionAdhoc(String type, String tan, Pagination pagination) {
		logger.info("Tenant Tan ---: {}", tan);
		CommonDTO<ProvisionDTO> provisionData = new CommonDTO<>();
		BigInteger count = provisionMismatchDAO.getAdhocCount(tan, type);
		logger.info("Count : {}", count);
		List<ProvisionDTO> response = provisionMismatchDAO.getAdhocList(tan, type, pagination);
		PagedData<ProvisionDTO> pagedData = new PagedData<>(response, response.size(), pagination.getPageNumber(),
				false);
		provisionData.setCount(count);
		provisionData.setResultsSet(pagedData);
		return provisionData;
	}

	/**
	 * 
	 * @param tans
	 * @param firstDate
	 * @param lastDate
	 * @return
	 */
	public String getTdsCalculationForProvision(String tans, String firstDate, String lastDate) {
		long trueValue = provisionDAO.getCountOfTdsCalculationsOfProvisionForCurrentMonth(tans, firstDate, lastDate,
				true);
		long falseValue = provisionDAO.getCountOfTdsCalculationsOfProvisionForCurrentMonth(tans, firstDate, lastDate,
				false);
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
	public CommonDTO<ProvisionDTO> getResidentAndNonresident(String residentType, String tan, int year, int month,
			Pagination pagination, String deducteeName) {
		BigInteger count = BigInteger.ZERO;
		CommonDTO<ProvisionDTO> provisionList = new CommonDTO<>();

		logger.info("tan : {} year : {} month : {} resident type : {} deductee name : {}", tan, year, month,
				residentType, deducteeName);

		List<ProvisionDTO> responseData = new ArrayList<ProvisionDTO>();
		if ("nodeducteefilter".equalsIgnoreCase(deducteeName)) {
			responseData = provisionDAO.findAllResidentAndNonResident(year, month, tan, residentType, pagination);
			count = provisionDAO.findAllResidentAndNonResidentCount(year, month, tan, residentType);
		} else {
			responseData = provisionDAO.findAllResidentAndNonResidentByDeductee(year, month, tan, residentType,
					pagination, deducteeName);
			count = provisionDAO.findAllResidentAndNonResidentByDeducteeCount(year, month, tan, residentType,
					deducteeName);
		}
		PagedData<ProvisionDTO> pagedData = new PagedData<>(responseData, responseData.size(),
				pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		provisionList.setCount(count);
		provisionList.setResultsSet(pagedData);
		return provisionList;
	}


	public MultipartFile generateCancelledProvisionsExcell(List<Integer> provisionIds, String tan, String deductorPan) {

		MultipartFile multipartFile = null;
		String[] provisionHeadersFile = new String[] { "Source Identifier", "Source File Name",
				"Name of the Company Code", "Deductor PAN", "Deductor TAN", "Deductor GSTIN", "Deductee Code",
				"Name of the Deductee", "Non-Resident Deductee Indicator", "Deductee PAN", "Deductee TIN",
				"Deductee GSTIN", "Document Number", "Document Type", "Supply Type", "Document Date",
				"Posting Date of the Document", "TDSDeductionDate",
				"Entry Date of Provision Made for TDS Payable on SAC Expenses", "Line Item Number", "HSN/SAC",
				"SAC Description", "Service Description – Invoice", "Service Description – PO",
				"Provision G/L Account Code", "Provision G/L Account Description", "Provisional Amount", "Section Code",
				"POS", "Withholding Section", "Withholding Rate/Withholding Tax Code", "Withholding Amount",
				"PO Number", "PO Date", "Type of PO", "Linking of Invoice with PO", "User Defined Field 1",
				"User Defined Field 2", "User Defined Field 3", "Challan Paid", "Challan Generated Date" };
		try {
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			worksheet.setName("Imported Provisions");
			worksheet.autoFitColumns();
			worksheet.autoFitRows();
			worksheet.getCells().importArray(provisionHeadersFile, 0, 0, false);

			List<ProvisionDTO> cancelList = provisionDAO.getProvisionBasedOnIds(provisionIds, tan, deductorPan);
			logger.info("provision cancel list is: {}", cancelList.size());
			setProvisionHeaders(cancelList, worksheet);

			worksheet.autoFitColumns();

			// Style for A1 to AO1 headers
			Style style5 = workbook.createStyle();
			style5.setForegroundColor(Color.fromArgb(91, 155, 213));
			style5.setPattern(BackgroundType.SOLID);
			style5.getFont().setBold(true);
			style5.setHorizontalAlignment(TextAlignmentType.CENTER);
			Range headerColorRange1 = worksheet.getCells().createRange("A1:AO1");
			headerColorRange1.setStyle(style5);

			// Creating AutoFilter by giving the cells range
			AutoFilter autoFilter = worksheet.getAutoFilter();
			autoFilter.setRange("A1:AO1");

			File file = new File("Cancelled_Provisions_" + UUID.randomUUID() + ".xlsx");
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

	public void setProvisionHeaders(List<ProvisionDTO> cancelList, Worksheet worksheet) throws Exception {
		int rowIndex = 1;
		for (ProvisionDTO cancelledProvision : cancelList) {
			List<Object> rowData = new ArrayList<>();
			// Source Identifier
			rowData.add(StringUtils.isBlank(cancelledProvision.getSourceIdentifier()) ? StringUtils.EMPTY
					: cancelledProvision.getSourceIdentifier());
			// Source File Name
			rowData.add(StringUtils.isBlank(cancelledProvision.getSourceFileName()) ? StringUtils.EMPTY
					: cancelledProvision.getSourceFileName());
			// Name of the Company Code
			rowData.add(StringUtils.isBlank(cancelledProvision.getNameOfTheCompanyCode()) ? StringUtils.EMPTY
					: cancelledProvision.getNameOfTheCompanyCode());
			// Deductor PAN
			rowData.add(StringUtils.isBlank(cancelledProvision.getDeductorPan()) ? StringUtils.EMPTY
					: cancelledProvision.getDeductorPan());
			// Deductor TAN
			rowData.add(StringUtils.isBlank(cancelledProvision.getDeductorTan()) ? StringUtils.EMPTY
					: cancelledProvision.getDeductorTan());
			// Deductor GSTIN
			rowData.add(StringUtils.isBlank(cancelledProvision.getDeductorGstin()) ? StringUtils.EMPTY
					: cancelledProvision.getDeductorGstin());
			// Deductee Code
			rowData.add(StringUtils.isBlank(cancelledProvision.getDeducteeCode()) ? StringUtils.EMPTY
					: cancelledProvision.getDeducteeCode());
			// Name of the Deductee
			rowData.add(StringUtils.isBlank(cancelledProvision.getDeducteeName()) ? StringUtils.EMPTY
					: cancelledProvision.getDeducteeName());
			// Non-Resident Deductee Indicator
			rowData.add(StringUtils.isBlank(cancelledProvision.getIsResident()) ? StringUtils.EMPTY
					: cancelledProvision.getIsResident());
			// Deductee PAN
			rowData.add(StringUtils.isBlank(cancelledProvision.getDeducteePan()) ? StringUtils.EMPTY
					: cancelledProvision.getDeducteePan());
			// Deductee TIN
			rowData.add(StringUtils.isBlank(cancelledProvision.getDeducteeTin()) ? StringUtils.EMPTY
					: cancelledProvision.getDeducteeTin());
			// Deductee GSTIN
			rowData.add(StringUtils.isBlank(cancelledProvision.getDeducteeGstin()) ? StringUtils.EMPTY
					: cancelledProvision.getDeducteeGstin());
			// Document Number
			rowData.add(StringUtils.isBlank(cancelledProvision.getDocumentNumber()) ? StringUtils.EMPTY
					: cancelledProvision.getDocumentNumber());
			// Document Type
			rowData.add(StringUtils.isBlank(cancelledProvision.getDocumentType()) ? StringUtils.EMPTY
					: cancelledProvision.getDocumentType());
			// supply Type
			rowData.add(StringUtils.isBlank(cancelledProvision.getSupplyType()) ? StringUtils.EMPTY
					: cancelledProvision.getSupplyType());
			// Document Date
			rowData.add(cancelledProvision.getDocumentDate() == null ? StringUtils.EMPTY
					: cancelledProvision.getDocumentDate());
			// Posting Date of the Document
			rowData.add(cancelledProvision.getPostingDateOfDocument() == null ? StringUtils.EMPTY
					: cancelledProvision.getPostingDateOfDocument());
			// TDS Deduction Date
			rowData.add(cancelledProvision.getTdsDeductionDate() == null ? StringUtils.EMPTY
					: cancelledProvision.getTdsDeductionDate());
			// Entry Date of Provision Made for TDS Payable on SAC Expenses
			rowData.add(
					cancelledProvision.getEntryDate() == null ? StringUtils.EMPTY : cancelledProvision.getEntryDate());
			// Line Item Number
			rowData.add(StringUtils.isBlank(cancelledProvision.getLineItemNumber()) ? StringUtils.EMPTY
					: cancelledProvision.getLineItemNumber());
			// HSN/SAC
			rowData.add(StringUtils.isBlank(cancelledProvision.getHsnOrSac()) ? StringUtils.EMPTY
					: cancelledProvision.getHsnOrSac());
			// SAC Description
			rowData.add(StringUtils.isBlank(cancelledProvision.getServiceDescription()) ? StringUtils.EMPTY
					: cancelledProvision.getServiceDescription());
			// Service Description – Invoice
			rowData.add(StringUtils.isBlank(cancelledProvision.getServiceDescription()) ? StringUtils.EMPTY
					: cancelledProvision.getServiceDescription());
			// Service Description – PO
			rowData.add(StringUtils.isBlank(cancelledProvision.getServiceDescriptionPo()) ? StringUtils.EMPTY
					: cancelledProvision.getServiceDescriptionPo());
			// Provision G/L Account Code
			rowData.add(StringUtils.isBlank(cancelledProvision.getAccountCode()) ? StringUtils.EMPTY
					: cancelledProvision.getAccountCode());
			// Provision G/L Account Description
			rowData.add(StringUtils.isBlank(cancelledProvision.getAccountDescription()) ? StringUtils.EMPTY
					: cancelledProvision.getAccountDescription());
			// Provisional Amount
			rowData.add(cancelledProvision.getProvisionalAmount() == null ? StringUtils.EMPTY
					: cancelledProvision.getProvisionalAmount());
			// Section Code
			rowData.add(StringUtils.isBlank(cancelledProvision.getSectionCode()) ? StringUtils.EMPTY
					: cancelledProvision.getSectionCode());
			// POS
			rowData.add(
					StringUtils.isBlank(cancelledProvision.getPos()) ? StringUtils.EMPTY : cancelledProvision.getPos());
			// Withholding Section
			rowData.add(StringUtils.isBlank(cancelledProvision.getWithholdingSection()) ? StringUtils.EMPTY
					: cancelledProvision.getWithholdingSection());
			// Withholding Rate\\Withholding Tax Code
			rowData.add(cancelledProvision.getWithholdingRate() == null ? StringUtils.EMPTY
					: cancelledProvision.getWithholdingRate());
			// Withholding Amount
			rowData.add(cancelledProvision.getWithholdingAmount() == null ? StringUtils.EMPTY
					: cancelledProvision.getWithholdingAmount());
			// PO Number
			rowData.add(
					cancelledProvision.getPoNumber() == null ? StringUtils.EMPTY : cancelledProvision.getPoNumber());
			// PO Date
			rowData.add(cancelledProvision.getPoDate() == null ? StringUtils.EMPTY : cancelledProvision.getPoDate());
			// Type of PO
			rowData.add(StringUtils.isBlank(cancelledProvision.getPoType()) ? StringUtils.EMPTY
					: cancelledProvision.getPoType());
			// Linking of Invoice with PO
			rowData.add(StringUtils.isBlank(cancelledProvision.getLinkingInvoicePo()) ? StringUtils.EMPTY
					: cancelledProvision.getLinkingInvoicePo());
			// User Defined Field 1
			rowData.add(StringUtils.isBlank(cancelledProvision.getUserDefinedField1()) ? StringUtils.EMPTY
					: cancelledProvision.getUserDefinedField1());
			// User Defined Field 2
			rowData.add(StringUtils.isBlank(cancelledProvision.getUserDefinedField2()) ? StringUtils.EMPTY
					: cancelledProvision.getUserDefinedField2());
			// User Defined Field 3
			rowData.add(StringUtils.isBlank(cancelledProvision.getUserDefinedField3()) ? StringUtils.EMPTY
					: cancelledProvision.getUserDefinedField3());
			// Challan Paid
			rowData.add(cancelledProvision.getChallanPaid() == null ? StringUtils.EMPTY
					: cancelledProvision.getChallanPaid());
			// Challan Generated Date
			rowData.add(cancelledProvision.getChallanGeneratedDate() == null ? StringUtils.EMPTY
					: cancelledProvision.getChallanGeneratedDate());

			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
		}
	}

	private static void setCellColorAndBoarder(DefaultIndexedColorMap defaultIndexedColorMap, XSSFCellStyle style,
			Integer r, Integer g, Integer b) {
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.TOP);
		style.setFillForegroundColor(new XSSFColor(new java.awt.Color(r, g, b), defaultIndexedColorMap));
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

	}

	private void setMediumBorder(DefaultIndexedColorMap defaultIndexedColorMap, XSSFCellStyle style, Integer r,
			Integer g, Integer b) {
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderRight(BorderStyle.MEDIUM);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
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
			Integer month, String tenantId, String deducteeName, String residentType)
			throws InvalidKeyException, FileNotFoundException, IOException, URISyntaxException, StorageException {
		logger.info("Entered into async method for generating interest computation report for Advance{}");
		MultiTenantContext.setTenantId(tenantId);
		exportInterestComputationReport(tan, year, userName, deductorPan, month, tenantId, deducteeName, residentType);
	}

	@Transactional
	public void exportInterestComputationReport(String tan, Integer year, String userName, String deductorPan,
			Integer month, String tenantId, String deducteeName, String residentType)
			throws InvalidKeyException, FileNotFoundException, IOException, URISyntaxException, StorageException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();

		BatchUpload batchUpload = saveInterestComputationReport(tan, tenantId, year, null, 0L,
				UploadTypes.PROVISION_INTEREST_COMPUTATION_REPORT.name(), "Processing", month, userName, null);

		try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {

			SXSSFSheet sheet = workbook.createSheet("Provision_Interest_Report");
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
			// style1.setTopBorderColor(new XSSFColor(new java.awt.Color(255, 255, 225),
			// defaultIndexedColorMap));
			// style1.setBottomBorderColor(new XSSFColor(new java.awt.Color(255, 255, 225),
			// defaultIndexedColorMap));
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
			// styleGreen.setTopBorderColor(new XSSFColor(new java.awt.Color(255, 255, 225),
			// defaultIndexedColorMap));
			// styleGreen.setBottomBorderColor(new XSSFColor(new java.awt.Color(255, 255,
			// 225), defaultIndexedColorMap));
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
			row1.createCell(2).setCellValue("Client TDS Section");
			row1.getCell(2).setCellStyle(style1);
			row1.createCell(3).setCellValue("Client TDS Rate");
			row1.getCell(3).setCellStyle(style1);
			row1.createCell(4).setCellValue("Derived Amount");
			row1.getCell(4).setCellStyle(style1);
			row1.createCell(5).setCellValue("Derived TDS Section");
			row1.getCell(5).setCellStyle(style1);
			row1.createCell(6).setCellValue("Derived TDS Rate");
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
			row1.createCell(14).setCellValue("Service Description - Invoice");
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
			row1.createCell(25).setCellValue("Provision ID");
			row1.getCell(25).setCellStyle(style1);

			List<ProvisionDTO> list = provisionDAO.getProvisionWithInterestComputed(tan, year, month, deducteeName,
					residentType);
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
			for (ProvisionDTO listData : list) {
				index++;
				// add the values to the eaders here
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

				row2.createCell(7).setCellValue(
						listData.getProvisionalAmount() == null ? " " : listData.getProvisionalAmount().toString());
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
				row2.createCell(19)
						.setCellValue(StringUtils.isEmpty(listData.getConfidence()) ? "" : listData.getConfidence());
				row2.getCell(19).setCellStyle(style2);
				row2.createCell(20).setCellValue(listData.getDocumentNumber());
				row2.getCell(20).setCellStyle(style2);
				row2.createCell(21).setCellValue(listData.getInterest().toString());
				row2.getCell(21).setCellStyle(style2);

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
					UploadTypes.PROVISION_INTEREST_COMPUTATION_REPORT.name(), "Processed", month, userName,
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
			logger.info("Provision Computation report {} completed for : {}", uploadType, userName);
		} else {
			logger.info("Provision Computation report {} started for : {}", uploadType, userName);
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
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchUpload.setFileName(fileName);
				batchUpload.setStatus("Processed");
				batchUpload.setFilePath(path);

			} else {
				batchUpload.setCreatedDate(new Date());
				batchUpload.setCreatedBy(userName);
				batchUpload.setModifiedDate(null);
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchUpload.setRowsCount(0l);
			}
			batchUpload.setRowsCount(noOfRows);
			batchUpload.setModifiedBy(userName);
			return batchUploadDAO.update(batchUpload);
		} else {
			batchUpload.setAssessmentYear(assessmentYear);
			batchUpload.setDeductorMasterTan(deductorTan);
			batchUpload.setUploadType(uploadType);
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
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

	public String getInterestComputationReportMsg(String deductorMasterTan, String tenantId, String deductorPan) {
		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return "Provision Interest Computation Report (Dated: " + date + ")\n Client Name: "
				+ deductorData.getDeductorName() + "\n";
	}

	public CommonDTO<ProvisionDTO> getProvisionInterestRecords(String tan, int year, int challanMonth,
			MismatchesFiltersDTO filters) {
		CommonDTO<ProvisionDTO> provisionData = new CommonDTO<>();

		List<ProvisionDTO> invoiceList = provisionDAO.getProvisionWithInterestComputedWithPagination(tan, year,
				challanMonth, filters.getDeducteeName(), filters.getResidentType(), filters.getPagination());
		BigInteger count = advanceDAO.getCountOfAdvancesWithInterestComputed(tan, year, challanMonth,
				filters.getDeducteeName(), filters.getResidentType());

		PagedData<ProvisionDTO> pagedData = new PagedData<ProvisionDTO>(invoiceList, invoiceList.size(),
				filters.getPagination().getPageNumber(),
				count.intValue() > (filters.getPagination().getPageSize() * filters.getPagination().getPageNumber())
						? false
						: true);
		provisionData.setResultsSet(pagedData);
		provisionData.setCount(count);
		return provisionData;

	}

	public void asyncProcesspProvisionComputationFile(String tenantId, String userName, String deductorTan,
			String deductorPan, Integer year, Integer month, String uploadType, BatchUpload batchUplaod)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Async method executing to process the interest report {}");
		processProvisionComputationFile(tenantId, userName, deductorTan, deductorPan, year, month, uploadType,
				batchUplaod);
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
	public void processProvisionComputationFile(String tenantId, String userName, String deductorTan,
			String deductorPan, Integer year, Integer month, String uploadType, BatchUpload batchUpload)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		MultiTenantContext.setTenantId(tenantId);
		File file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());

		try (XSSFWorkbook workbook = new XSSFWorkbook(file.getAbsolutePath())) {

			XSSFSheet worksheet = workbook.getSheetAt(0);
			worksheet.setColumnHidden(20, false);
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
					ProvisionDTO provision = null;
					List<ProvisionDTO> list = provisionDAO.getProvisionsWithInterestById(invoiceLineItemId);
					if (!list.isEmpty()) {
						provision = list.get(0);
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
							finalAmount = BigDecimal.ZERO;
							successCount++;
						}
						provision.setFinalTdsAmount(finalAmount);
						provision.setReason(reason);
						provision.setActionType(userAction);
						provision.setModifiedBy(userName);
						provision.setModifiedDate(new Timestamp(new Date().getTime()));
						provisionDAO.updateProvisionInterest(provision);
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

	/**
	 * to update te onscreen interest records
	 * 
	 * @param tan
	 * @param updateOnScreenDTO
	 * @param pan
	 */
	public void updateInterestrecords(String tan, UpdateOnScreenDTO updateOnScreenDTO, String pan) {

		for (UpdateOnScreenDTO invoiceInterest : updateOnScreenDTO.getData()) {
			if (invoiceInterest.getId() != null) {
				ProvisionDTO provision = null;
				logger.info("Fetching advance interest records {}");
				List<ProvisionDTO> list = provisionDAO.getProvisionsWithInterestById(invoiceInterest.getId());
				if (!list.isEmpty()) {
					provision = list.get(0);
					logger.info("Retrieved  provision interest record {}" + provision);
					String userAction = updateOnScreenDTO.getActionType();
					BigDecimal finalTdsAmount = provision.getInterest();

					if (userAction.equalsIgnoreCase("modify")) {
						finalTdsAmount = invoiceInterest.getFinalAmount();
					} else if (userAction.equalsIgnoreCase("Reject")) {
						finalTdsAmount = BigDecimal.ZERO;
					}
					provision.setFinalTdsAmount(finalTdsAmount);
					provision.setReason(updateOnScreenDTO.getReason());
					provision.setActionType(updateOnScreenDTO.getActionType());
					provisionDAO.updateProvisionInterest(provision);
					logger.info("  provision interest record updated successfully with final amount as {}"
							+ provision.getFinalTdsAmount());
				}

			}
		}
	}

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
	
	private String getFormattedValue(String value) {
		return StringUtils.isNotBlank(value) && new BigDecimal(value).compareTo(BigDecimal.ZERO) > 0 ? value
				: StringUtils.EMPTY;
	}
	
	private void createSXSSFCell(XSSFCellStyle style, SXSSFRow row, int cellNumber, String value) {
		row.createCell(cellNumber).setCellValue(StringUtils.isBlank(value) == true ? StringUtils.EMPTY : value);
		row.getCell(cellNumber).setCellStyle(style); // K
	}
	
	public String getMismatchReportMsg(String deductorName) {

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return "Provision Remediation Report (Dated: " + date + ")\n Client Name: " + deductorName + "\n";
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
}
