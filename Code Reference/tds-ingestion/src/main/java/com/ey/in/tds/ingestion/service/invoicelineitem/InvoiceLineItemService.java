package com.ey.in.tds.ingestion.service.invoicelineitem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
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
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bouncycastle.crypto.RuntimeCryptoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
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
import com.ey.in.tds.common.dashboard.dto.ActivityTracker;
import com.ey.in.tds.common.dashboards.jdbc.dao.ActivityTrackerDAO;
import com.ey.in.tds.common.domain.Currency;
import com.ey.in.tds.common.domain.ErrorCode;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadGroupDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.NrTransactionsMetaDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUploadGroup;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceMetaNr;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.dto.CRDTO;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.CustomSectionRateDTO;
import com.ey.in.tds.common.dto.CustomThresholdGroupLimitDto;
import com.ey.in.tds.common.dto.InvoiceDTO;
import com.ey.in.tds.common.dto.InvoiceKeyDTO;
import com.ey.in.tds.common.dto.InvoiceLineItemCRDTO;
import com.ey.in.tds.common.dto.csvfile.CsvFileDTO;
import com.ey.in.tds.common.dto.invoice.InvoiceMismatchByBatchIdDTO;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.ingestion.response.dto.InvoiceLineItemResponseDTO;
import com.ey.in.tds.common.ingestion.response.dto.InvoiceMetaNrResponseDTO;
import com.ey.in.tds.common.model.invoicelineitem.InvoiceErrorReportCsvDTO;
import com.ey.in.tds.common.model.invoicelineitem.InvoiceLineItemDTO;
import com.ey.in.tds.common.model.invoicelineitem.InvoiceMetaNrDTO;
import com.ey.in.tds.common.model.invoicelineitem.InvoicePDFDTO;
import com.ey.in.tds.common.model.invoicelineitem.InvoicePdfCsvDTO;
import com.ey.in.tds.common.model.invoicelineitem.InvoicePdfList;
import com.ey.in.tds.common.model.invoicelineitem.NORemittanceDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.NrTransactionsMeta;
import com.ey.in.tds.common.repository.CurrencyRepository;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.service.ErrorReportService;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.ActivityTrackerStatus;
import com.ey.in.tds.core.util.ActivityType;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.dto.invoice.InvoiceCrDtO;
import com.ey.in.tds.ingestion.dto.invoice.InvoiceMismatchesDTO;
import com.ey.in.tds.ingestion.dto.invoice.UpdateOnScreenDTO;
import com.ey.in.tds.ingestion.jdbc.dao.AdvanceDAO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceLineItemDAO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceMetaNrDAO;
import com.ey.in.tds.ingestion.jdbc.dao.ProvisionDAO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.storage.StorageException;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

@Service
public class InvoiceLineItemService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BlobStorage blob;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	private ErrorReportService errorReportService;

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private BatchUploadGroupDAO batchUploadGroupDAO;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private InvoiceLineItemDAO invoiceLineItemDAO;

	@Autowired
	private AdvanceDAO advanceDAO;

	@Autowired
	private InvoiceMetaNrDAO invoiceMetaNrDAO;

	@Autowired
	private ProvisionDAO provisionDAO;

	@Autowired
	private ActivityTrackerDAO activityTrackerDAO;

	@Autowired
	private NonResidentService invoiceNonResidentService;

	@Autowired
	private Sha256SumService sha256SumService;

	@Value("${application.url}")
	private String applicationUrl;
	
	@Autowired
	private NrTransactionsMetaDAO nrTransactionsMetaDAO;

	Map<String, String> excelHeaderMap = new HashMap<String, String>() {
		private static final long serialVersionUID = -6921991234774788483L;
		{
			put("Source Identifier", "D");
			put("Source File Name", "E");
			put("Deductor PAN", "F");
			put("Deductor TAN", "G");
			put("Deductor GSTIN", "H");
			put("Non-Resident Deductee Indicator", "I");
			put("Deductee PAN", "J");
			put("Deductee TIN", "K");
			put("Deductee GSTIN", "L");
			put("Name of the Deductee", "M");
			put("Deductee Address", "N");
			put("Document Type", "O");
			put("Document Number", "P");
			put("Document Date", "Q");
			put("Posting Date of Document", "R");
			put("Line Item Number", "S");
			put("HSN/SAC", "T");
			put("Service Description - Invoice", "U");
			put("Service Description - PO", "V");
			put("Service Description - GL Text", "W");
			put("Taxable value", "X");
			put("IGST Rate", "Y");
			put("IGST Amount", "Z");
			put("CGST Rate", "AA");
			put("CGST Amount", "AB");
			put("SGST Rate", "AC");
			put("SGST Amount", "AD");
			put("Cess Rate", "AE");
			put("Cess Amount", "AF");
			put("Creditable (Y/N)", "AG");
			put("TDS Section", "AH");
			put("TDS Rate", "AI");
			put("TDS Amount", "AJ");
			put("PO number", "AK");
			put("PO date", "AL");
			put("Linked advance Number", "AM");
			put("Grossing up Indicator", "AN");
			put("Original Document Number", "AO");
			put("Original Document Date", "AP");
			put("Company Code", "AQ");
			put("Deductee Code", "AR");
			put("Duplicates", "AS");
			put("Erp Document Number", "AT");
			put("Migo Number", "AU");
			put("Miro Number", "AV");
			put("MisMatch", "AW");
			put("Name Of The Company Code", "AX");
			put("Pos", "AY");
			put("Processed", "AZ");
			put("Reason", "BA");
			put("Reject", "BB");
			put("Sac Description", "BC");
			put("Section Code", "BD");
			put("Vendor Invoice Number", "BE");
			put("Vendor Master", "BF");
			put("User Defined Field 1", "BG");
			put("User Defined Field 2", "BH");
			put("User Defined Field 3", "BI");
		}
	};

	public static String[] invoiceOtherErrorReportheaderNames = new String[] { "Source File Name", "Company Code",
			"Name Of The Company Code", "Deductor PAN", "Deductor TAN", "Deductor GSTIN", "Deductee Code",
			"Non-Resident Deductee Indicator", "Deductee PAN", "Deductee TIN", "Deductee GSTIN", "Name of the Deductee",
			"Deductee Address", "Vendor Invoice Number", "Erp Document Number", "Miro Number", "Migo Number",
			"Document Type", "Erp Document Number", "Document Date", "Posting Date of Document", "Line Item Number",
			"HSN/SAC", "Sac Description", "Service Description - Invoice", "Service Description - PO",
			"Service Description - GL Text", "Taxable value", "IGST Rate", "IGST Amount", "CGST Rate", "CGST Amount",
			"SGST Rate", "SGST Amount", "Cess Rate", "Cess Amount", "Creditable (Y/N)", "Section Code", "POS",
			"TDS Section", "TDS Rate", "TDS Amount", "PO number", "PO date", "Linked advance Number",
			"Grossing up Indicator", "Original Document Number", "Original Document Date", "User Defined Field 1",
			"User Defined Field 2", "User Defined Field 3", "Section Predection" };

	/*
	 * public InvoiceLineItem create(InvoiceLineItem invoiceLineItem) {
	 * invoiceLineItem.getKey().setId(UUID.randomUUID());
	 * invoiceLineItem.setActive(true); return
	 * invoiceLineItemRepository.insert(invoiceLineItem); }
	 */

	/*
	 * public InvoiceLineItem get(InvoiceLineItem.Key key) {
	 * Optional<InvoiceLineItem> response = invoiceLineItemRepository.findById(key);
	 * return response.orElseThrow(() -> new RuntimeException(
	 * "Did not find an InvoiceLineItem with the passed in criteria : " +
	 * key.toString())); }
	 */

	/**
	 * Get List of Invoice Mis Matches based on Batch Upload Id
	 * 
	 * @param batchId
	 * @return
	 */
	public List<InvoiceMismatchByBatchIdDTO> getInvoiceMismatchsByBatchUploadIDType(Integer batchId, String tan) {
		logger.info("REST request of BatchID and Tan to get List of InvoiceMismatches : {} , {}", batchId, tan);
		List<InvoiceMismatchByBatchIdDTO> listMisMatchBybatchDTO = new ArrayList<>();

		listMisMatchBybatchDTO.add(groupMismatchesByType(tan, "SM-RMM", batchId, 0, 0));
		listMisMatchBybatchDTO.add(groupMismatchesByType(tan, "SMM-RMM", batchId, 0, 0));
		listMisMatchBybatchDTO.add(groupMismatchesByType(tan, "SMM-RM", batchId, 0, 0));
		listMisMatchBybatchDTO.add(groupMismatchesByType(tan, "NAD", batchId, 0, 0));

		return listMisMatchBybatchDTO;
	}

	// tds mismatches all
	public List<InvoiceMismatchByBatchIdDTO> getInvoiceMismatchAll(String tan, int year, int month) {
		logger.info("REST request of Tan to get List of InvoiceMismatches : {}", tan);
		List<InvoiceMismatchByBatchIdDTO> listMisMatchAllDTO = new ArrayList<>();

		listMisMatchAllDTO.add(groupMismatchesByType(tan, "SM-RMM", null, year, month));
		listMisMatchAllDTO.add(groupMismatchesByType(tan, "SMM-RMM", null, year, month));
		listMisMatchAllDTO.add(groupMismatchesByType(tan, "SMM-RM", null, year, month));
		listMisMatchAllDTO.add(groupMismatchesByType(tan, "NAD", null, year, month));

		return listMisMatchAllDTO;
	}

	/**
	 * To get the summary of mismatches based on batchId and type
	 * 
	 * @param tan
	 * @param type
	 * @param id
	 * @param batchId
	 * @param year
	 * @param month
	 * @return
	 */

	public InvoiceMismatchByBatchIdDTO groupMismatchesByType(String tan, String type, Integer batchId, int year,
			int month) {
		return invoiceLineItemDAO.getInvoiceMismatchSummary(year, month, tan, batchId, type);
	}

	/**
	 * Get List of Invoice Mis Matches based on Batch Upload Id, Mis matches
	 * 
	 * @param batchId
	 * @param mismatchCategory
	 * @return
	 */
	public CommonDTO<InvoiceLineItem> getInvoiceMismatchByBatchUploadIDMismatchCategory(Integer batchId,
			String mismatchCategory, List<String> tan, Pagination pagination) {
		logger.info("REST request of batchId ,mismatchCategory and tan : {} , {} , {} ", batchId, mismatchCategory,
				tan);
		BigInteger count = BigInteger.ZERO;

		List<InvoiceLineItem> listMisMatch = invoiceLineItemDAO
				.getInvoiceMismatchesByAssessmentYearAssessmentMonthBatchIdAndMismatchcategory(batchId,
						mismatchCategory, tan, pagination);

		count = invoiceLineItemDAO.getCountOfInvoiceMismatchesByAssessmentYearAssessmentMonthBatchIdAndMismatchcategory(
				batchId, mismatchCategory, tan, pagination);
		PagedData<InvoiceLineItem> pagedData = new PagedData<>(listMisMatch, listMisMatch.size(),
				pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		CommonDTO<InvoiceLineItem> invoiceData = new CommonDTO<>();
		invoiceData.setResultsSet(pagedData);
		invoiceData.setCount(count);
		return invoiceData;
	}

	/**
	 * Update InvoiceMismatchUpadteDTO for remediation report
	 * 
	 * @param invoiceMismatchUpdateDTO
	 * @throws RecordNotFoundException
	 * @throws URISyntaxException
	 */
	public UpdateOnScreenDTO updateMismatchByAction(String tan, UpdateOnScreenDTO invoiceMismatchUpdateDTO,
			String token, String deductorPan) throws RecordNotFoundException, URISyntaxException {
		List<CustomSectionRateDTO> listRatesSections = mastersClient.getNatureOfPaymentMasterRecord().getBody()
				.getData();

		Map<String, List<Double>> ratesMap = new HashMap<String, List<Double>>();

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
			sectionRateNopId.put(customSectionRateDTO.getSection() + "-" + customSectionRateDTO.getRate(),
					customSectionRateDTO.getNoiId());
		}
		List<CustomThresholdGroupLimitDto> customThresholdGroupLimitList = mastersClient.getThresholdLimitGroup()
				.getBody().getData();
		Map<Integer, Integer> nopGroupMap = new HashMap<Integer, Integer>();

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
		for (UpdateOnScreenDTO invoiceMismatch : invoiceMismatchUpdateDTO.getData()) {
			BigDecimal finalAmount = BigDecimal.ZERO;
			Integer nopId = 0;
			Integer nopGroupId = 0;
			List<InvoiceLineItem> invoiceLineItemData = invoiceLineItemDAO.findByYearTanDocumentPostingDateIdActive(
					invoiceMismatchUpdateDTO.getAssessmentYear(), tan, invoiceMismatch.getDocumentPostingDate(),
					invoiceMismatch.getId(), false);
			if (!invoiceLineItemData.isEmpty()) {
				InvoiceLineItem invoiceLineItem = invoiceLineItemData.get(0);
				invoiceLineItem.setFinalReason(invoiceMismatchUpdateDTO.getReason());
				invoiceLineItem.setFinalTdsSection(invoiceMismatch.getFinalSection());
				invoiceLineItem.setFinalTdsRate(invoiceMismatch.getFinalRate());
				invoiceLineItem.setIsExempted(false);
				if ("ACCEPT".equalsIgnoreCase(invoiceMismatchUpdateDTO.getActionType())) {
					invoiceLineItem.setFinalTdsAmount(invoiceLineItem.getDerivedTdsAmount());
				} else if ("REJECT".equalsIgnoreCase(invoiceMismatchUpdateDTO.getActionType())) {
					invoiceLineItem.setFinalTdsAmount(invoiceLineItem.getClientAmount());
				} else if (invoiceMismatch.getFinalAmount() != null
						&& invoiceMismatch.getFinalAmount().compareTo(BigDecimal.ZERO) > 0) {
					invoiceLineItem.setFinalTdsAmount(invoiceMismatch.getFinalAmount());
				} else if ("Modify - TDS base value".equalsIgnoreCase(invoiceMismatchUpdateDTO.getActionType())) {
					invoiceLineItem.setClientTaxableAmount(invoiceLineItem.getInvoiceAmount());
					invoiceLineItem.setInvoiceAmount(invoiceLineItem.getTdsBaseValue());
					finalAmount = finalAmount.add(invoiceLineItem.getFinalTdsRate()
							.multiply(invoiceLineItem.getInvoiceAmount()).divide(BigDecimal.valueOf(100)));
					invoiceLineItem.setFinalTdsAmount(finalAmount);
				} else if ("Modify - Taxable value".equalsIgnoreCase(invoiceMismatchUpdateDTO.getActionType())) {
					finalAmount = invoiceMismatch.getTaxableValue().multiply(invoiceLineItem.getFinalTdsRate())
							.divide(BigDecimal.valueOf(100));
					invoiceLineItem.setFinalTdsAmount(finalAmount);
				} else if ("Modify - Any other amount".equalsIgnoreCase(invoiceMismatchUpdateDTO.getActionType())) {
					invoiceLineItem.setClientTaxableAmount(invoiceLineItem.getInvoiceAmount());
					invoiceLineItem.setInvoiceAmount(invoiceMismatch.getAnyOtherAmount());
					finalAmount = finalAmount.add(invoiceLineItem.getFinalTdsRate()
							.multiply(invoiceLineItem.getInvoiceAmount()).divide(BigDecimal.valueOf(100)));
					invoiceLineItem.setFinalTdsAmount(finalAmount);
				}
				if ("INVOICE_NR_EXCEL".equalsIgnoreCase(invoiceLineItem.getProcessedFrom())
						&& !"Cancel".equalsIgnoreCase(invoiceMismatchUpdateDTO.getActionType())
						&& !"ACCEPT".equalsIgnoreCase(invoiceMismatchUpdateDTO.getActionType())
						&& invoiceLineItem.getHasDtaa() != null && invoiceLineItem.getHasDtaa().equals(false)) {
					List<NrTransactionsMeta> nrTransactionList = nrTransactionsMetaDAO.findById(tan,
							invoiceMismatchUpdateDTO.getAssessmentYear(), invoiceLineItem.getNrTransactionsMetaId());
					NrTransactionsMeta nrTransaction = nrTransactionList.get(0);
					if (!nrTransactionList.isEmpty()) {
						invoiceLineItem.setSurcharge(invoiceNonResidentService.surchargeCalculation(
								invoiceLineItem.getFinalTdsSection(), invoiceLineItem.getFinalTdsAmount(),
								invoiceLineItem.getInvoiceAmount(), nrTransaction.getDeducteeStatus(), surchargeMap));
						BigDecimal cessAmount = invoiceLineItem.getSurcharge().add(invoiceLineItem.getFinalTdsAmount());
						invoiceLineItem.setCessAmount(
								cessAmount.multiply(invoiceLineItem.getCessRate()).divide(BigDecimal.valueOf(100)));
					}
				}
				invoiceLineItem.setActionType(invoiceMismatchUpdateDTO.getActionType());
				invoiceLineItem.setIsMismatch(false);
				invoiceLineItem.setModifiedDate(new Date());
				invoiceLineItem.setActive(true);
				if (!"Cancel".equalsIgnoreCase(invoiceMismatchUpdateDTO.getActionType())
						&& !"Accept".equalsIgnoreCase(invoiceMismatchUpdateDTO.getActionType())) {
					if (ratesMap != null && ratesMap.get(invoiceLineItem.getFinalTdsSection()) != null) {
						Double closestRate = closest(invoiceLineItem.getFinalTdsRate().doubleValue(),
								ratesMap.get(invoiceLineItem.getFinalTdsSection()));
						BigInteger nopIdInt = sectionRateNopId
								.get(invoiceLineItem.getFinalTdsSection() + "-" + closestRate);
						nopId = nopIdInt.compareTo(BigInteger.ZERO) > 0 ? nopIdInt.intValue() : 0;
					}
					if (nopId != null) {
						invoiceLineItem.setInvoiceNpId(nopId);
						nopGroupId = nopGroupMap.get(nopId);
					}
					if (nopGroupId != null) {
						invoiceLineItem.setGroupId(nopGroupId);
					}
				}
				if ("IMPG".equalsIgnoreCase(invoiceLineItem.getSupplyType())) {
					if ("194Q".equalsIgnoreCase(invoiceMismatch.getFinalSection())
							|| "NOTDS".equalsIgnoreCase(invoiceMismatch.getFinalSection())
							|| StringUtils.isBlank(invoiceMismatch.getFinalSection())) {
						invoiceLineItem.setIsExempted(true);
						invoiceLineItem.setErrorReason("Transaction out of Scope - Import of goods");
					}
				} else if (invoiceLineItem.getPan().equalsIgnoreCase(invoiceLineItem.getDeductorPan())) {
					if ("NOTDS".equalsIgnoreCase(invoiceMismatch.getFinalSection())
							|| StringUtils.isBlank(invoiceMismatch.getFinalSection())) {
						invoiceLineItem.setIsExempted(true);
						invoiceLineItem.setErrorReason("Transaction out of Scope - Stock Transfer");
					}
				} else if ("DLC".equalsIgnoreCase(invoiceLineItem.getDocumentType())) {
					if ("NOTDS".equalsIgnoreCase(invoiceMismatch.getFinalSection())
							|| StringUtils.isBlank(invoiceMismatch.getFinalSection())) {
						invoiceLineItem.setIsExempted(true);
						invoiceLineItem.setErrorReason("Transaction out of Scope - Delivery Challan");
					}
				} else if ("NOTDS".equalsIgnoreCase(invoiceMismatch.getFinalSection())
						|| StringUtils.isBlank(invoiceMismatch.getFinalSection())) {
					invoiceLineItem.setIsExempted(true);
					invoiceLineItem.setErrorReason("Transaction out of Scope - Scrape Sale");
				}
				invoiceLineItemDAO.update(invoiceLineItem);

			} else {
				logger.error("No Record for Invoice Line item to Update");
				throw new CustomException("No Record for Invoice Line item to Update",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return invoiceMismatchUpdateDTO;
	}

	public RestTemplate getRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
				return true;
			}
		};
		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();
		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		return restTemplate;
	}

	public CommonDTO<InvoiceLineItem> getResidentAndNonresident(String residentType, String filetype, String tan,
			int year, int month, Pagination pagination, String deducteeName) {
		CommonDTO<InvoiceLineItem> invoiceLineItemList = new CommonDTO<>();

		logger.info("tan : {} year : {} month : {} resident type : {} deductee name : {}", tan, year, month,
				residentType, deducteeName);

		List<InvoiceLineItem> invoiceList = invoiceLineItemDAO.findAllResidentAndNonResidentByDeductee(year, month, tan,
				residentType, filetype, deducteeName, pagination);
		BigInteger invoicesCount = invoiceLineItemDAO.findAllResidentAndNonResidentByDeducteeCount(year, month, tan,
				residentType, deducteeName, filetype);
		PagedData<InvoiceLineItem> pagedData = new PagedData<>(invoiceList, invoiceList.size(),
				pagination.getPageNumber(),
				invoicesCount.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		invoiceLineItemList.setCount(invoicesCount);
		invoiceLineItemList.setResultsSet(pagedData);
		return invoiceLineItemList;
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
	 * get the Mismatches based on the Category
	 * 
	 * @param mismatchCategory
	 * @return
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public CommonDTO<InvoiceLineItem> getInvoiceMismatchBasedOnMismatchCategory(String mismatchCategory, String tan,
			int year, int challanMonth, MismatchesFiltersDTO filters)
			throws JsonMappingException, JsonProcessingException {
		CommonDTO<InvoiceLineItem> invoiceLineItemData = new CommonDTO<>();
		List<InvoiceLineItem> resultedList = new ArrayList<>();

		List<InvoiceLineItem> invoiceList = invoiceLineItemDAO
				.getInvoicesByAssessmentYearChallanMonthByTanMismatchCategory(year, challanMonth, tan, mismatchCategory,
						filters);
		BigInteger count = invoiceLineItemDAO.getInvoicesCountByYearMonthAndTanMismatchCategory(year, challanMonth, tan,
				mismatchCategory, filters);

		for (InvoiceLineItem dto : invoiceList) {
			List<String> sections = advanceDAO.getVendorSections(dto.getDeducteeKey(), dto.getIsResident());
			dto.setVendorsectionCount((long) sections.size());
			dto.setVendorsectionList(sections.isEmpty() ? Arrays.asList("No Section") : sections);
			resultedList.add(dto);
		}

		PagedData<InvoiceLineItem> pagedData = new PagedData<InvoiceLineItem>(resultedList, resultedList.size(),
				filters.getPagination().getPageNumber(),
				count.intValue() > (filters.getPagination().getPageSize() * filters.getPagination().getPageNumber())
						? false
						: true);
		invoiceLineItemData.setResultsSet(pagedData);
		invoiceLineItemData.setCount(count);
		return invoiceLineItemData;
	}

	/**
	 * Get the Invoice Line Item Based on tan and id.
	 * 
	 * @param id
	 * @return
	 */
	public Map<String, Object> getLineItemData(String tan, int year, int month, Integer id, String type,
			Long documentPostingDate) {
		// TODO data type of id needs to be changed
		List<InvoiceLineItem> invoiceData = null;
		InvoiceLineItemResponseDTO invoiceLine = null;
		ProvisionDTO provision = null;
		AdvanceDTO advance = null;
		if ("INVOICE".equalsIgnoreCase(type)) {
			// invoiceData = invoiceLineItemRepository.findByYearPanInvoiceId(year, tan,
			// id,documentPostingDate);
			invoiceData = invoiceLineItemDAO.findByYearPanInvoiceId(year, tan, id, documentPostingDate);
			if (!invoiceData.isEmpty()) {
				invoiceLine = copyToResponse(invoiceData).get(0);
			}
		} else if ("PROVISION".equalsIgnoreCase(type)) {
			// Optional<Provision> response = provisionRepository.findByProvisionId(id);
			List<ProvisionDTO> response = provisionDAO.findByProvisionId(id);
			if (!response.isEmpty()) {
				provision = response.get(0);
			} else {
				new RuntimeException("Did not find an Provision with the passed in criteria : " + id.toString());
			}
		} else if ("ADVANCE".equalsIgnoreCase(type)) {
			// Optional<Advance> response = advanceRepository.findByAdvanceId(id);
			List<AdvanceDTO> response = advanceDAO.findByAdvanceId(id);
			if (!response.isEmpty()) {
				advance = response.get(0);
			} else {
				new RuntimeException("Did not find an Advance with the passed in criteria : " + id.toString());
			}
		}
		boolean isMetaDataExists = false;
		Map<String, Object> map = new HashMap<>();
		Map<String, Integer> remittanceMap = new HashMap<>();
		// InvoiceMetaNr invoiceMetaNrObj = new InvoiceMetaNr();
		InvoiceMetaNr invoiceMetaNrObj = new InvoiceMetaNr();
		InvoiceMetaNrDTO invoiceMetaNrDTO = new InvoiceMetaNrDTO();
		List<NORemittanceDTO> norList = new ArrayList<>();
		String pan = null;
		String deducteeName = null;
		String lineItemNumber = null;
		Date lineItemDate = null;
		String serviceDescription = null;
		String serviceDescriptionGl = null;
		String serviceDescriptionPo = null;
		String serviceDescriptionInvoice = null;
		BigDecimal lineItemAmount = null;
		String sacDecription = null;
		String deducteeTin = null;
		String clientSection = null;
		if (invoiceLine != null) {
			deducteeName = invoiceLine.getDeducteeName();
			pan = invoiceLine.getPan();
			lineItemNumber = invoiceLine.getVendorInvoiceNumber();
			lineItemDate = invoiceLine.getDocumentDate();
			serviceDescription = invoiceLine.getServiceDescriptionInvoice();
			serviceDescriptionGl = invoiceLine.getServiceDescriptionGl();
			serviceDescriptionPo = invoiceLine.getServiceDescriptionPo();
			serviceDescriptionInvoice = invoiceLine.getServiceDescriptionInvoice();
			sacDecription = invoiceLine.getSacDecription();
			lineItemAmount = invoiceLine.getInvoiceAmount();
			deducteeTin = invoiceLine.getDeducteeTin();
			clientSection = invoiceLine.getClientSection();
		} else if (provision != null) {
			deducteeName = provision.getDeducteeName();
			pan = provision.getDeducteePan();
			lineItemDate = provision.getDocumentDate();
			serviceDescription = provision.getServiceDescription();
			serviceDescriptionGl = provision.getServiceDescriptionGl();
			serviceDescriptionPo = provision.getServiceDescriptionPo();
			serviceDescriptionInvoice = provision.getServiceDescription();
			sacDecription = provision.getHsnsacDescription();
			lineItemAmount = provision.getProvisionalAmount();
			deducteeTin = provision.getDeducteeTin();
			clientSection = provision.getClientSection();
		} else if (advance != null) {
			deducteeName = advance.getDeducteeName();
			pan = advance.getDeducteePan();
			lineItemDate = advance.getDocumentDate();
			serviceDescription = advance.getServiceDescription();
			serviceDescriptionGl = advance.getServiceDescriptionGl();
			serviceDescriptionPo = advance.getServiceDescriptionPo();
			serviceDescriptionInvoice = advance.getServiceDescription();
			sacDecription = advance.getSacDescription();
			lineItemAmount = advance.getAmount();
			deducteeTin = advance.getDeducteeTin();
			clientSection = advance.getWithholdingSection();
		}
		// Optional<InvoiceMetaNr> invoiceMeta =
		// invoiceMetaNrRepository.findByLineItemId(id);
		// TODO dynamic id need to e asigned
		List<InvoiceMetaNr> invoiceMeta = invoiceMetaNrDAO.findByLineItemId(id);
		if (!invoiceMeta.isEmpty()) {
			isMetaDataExists = true;
			invoiceMetaNrObj = invoiceMeta.get(0);
			BeanUtils.copyProperties(invoiceMetaNrObj, invoiceMetaNrDTO);

			if (StringUtils.isNotBlank(invoiceMeta.get(0).getIdKsDocsRefered())) {
				String idKsDocsReferedUrl = invoiceMeta.get(0).getIdKsDocsRefered();
				String idKsDocsReferedFileName = idKsDocsReferedUrl.substring(idKsDocsReferedUrl.lastIndexOf('/') + 1,
						idKsDocsReferedUrl.length());
				invoiceMetaNrDTO.setIdKsDocsRefered(idKsDocsReferedFileName);
				invoiceMetaNrDTO.setIdKsDocsReferedUrl(idKsDocsReferedUrl);
			}
			if (StringUtils.isNotBlank(invoiceMeta.get(0).getInvoiceNumberFile())) {
				String invoiceNumberFileUrl = invoiceMeta.get(0).getInvoiceNumberFile();
				String invoiceNumberFileName = invoiceNumberFileUrl.substring(invoiceNumberFileUrl.lastIndexOf('/') + 1,
						invoiceNumberFileUrl.length());
				invoiceMetaNrDTO.setInvoiceNumberFile(invoiceNumberFileName);
				invoiceMetaNrDTO.setInvoiceNumberFileUrl(invoiceNumberFileUrl);
			}
			if (StringUtils.isNotBlank(invoiceMeta.get(0).getReleventForm15CBDoc())) {
				String releventFormUrl = invoiceMeta.get(0).getReleventForm15CBDoc();
				String releventFormFileName = releventFormUrl.substring(releventFormUrl.lastIndexOf('/') + 1,
						releventFormUrl.length());
				invoiceMetaNrDTO.setReleventForm15CBDoc(releventFormFileName);
				invoiceMetaNrDTO.setReleventForm15CBDocUrl(releventFormUrl);
			}
			if (StringUtils.isNotBlank(invoiceMeta.get(0).getReleventOpinionDoc())) {
				String releventOpinionUrl = invoiceMeta.get(0).getReleventOpinionDoc();
				String releventOpinionFileName = releventOpinionUrl.substring(releventOpinionUrl.lastIndexOf('/') + 1,
						releventOpinionUrl.length());
				invoiceMetaNrDTO.setReleventOpinionDoc(releventOpinionFileName);
				invoiceMetaNrDTO.setReleventOpinionDocUrl(releventOpinionUrl);
			}
			if (StringUtils.isNotBlank(invoiceMeta.get(0).getReleventOthersDoc())) {
				String releventOthersUrl = invoiceMeta.get(0).getReleventOthersDoc();
				String releventOthersFileName = releventOthersUrl.substring(releventOthersUrl.lastIndexOf('/') + 1,
						releventOthersUrl.length());
				invoiceMetaNrDTO.setReleventOthersDoc(releventOthersFileName);
				invoiceMetaNrDTO.setReleventOthersDocUrl(releventOthersUrl);
			}
			if (StringUtils.isNotBlank(invoiceMeta.get(0).getRevelantDoc())) {
				String revelantDocUrl = invoiceMeta.get(0).getRevelantDoc();
				String revelantDocFileName = revelantDocUrl.substring(revelantDocUrl.lastIndexOf('/') + 1,
						revelantDocUrl.length());
				invoiceMetaNrDTO.setRevelantDoc(revelantDocFileName);
				invoiceMetaNrDTO.setRevelantDocUrl(revelantDocUrl);
			}
			if (StringUtils.isNotBlank(invoiceMeta.get(0).getCertificationDocumentFile())) {
				String certificationDocumentUrl = invoiceMeta.get(0).getCertificationDocumentFile();
				String certificationDocumentFileName = certificationDocumentUrl
						.substring(certificationDocumentUrl.lastIndexOf('/') + 1, certificationDocumentUrl.length());
				invoiceMetaNrDTO.setCertificationDocumentFile(certificationDocumentFileName);
				invoiceMetaNrDTO.setCertificationDocumentFileUrl(certificationDocumentUrl);
			}

		}
		if (StringUtils.isNotBlank(deducteeName) && StringUtils.isNotBlank(deducteeTin)) {
			// List<InvoiceMetaNr> invoiceMetaNrList = invoiceMetaNrRepository
			// .findByDeducteeNameTin(deducteeName, deducteeTin,
			// Pagination.UNPAGED).getData();
			List<InvoiceMetaNr> invoiceMetaNrList = invoiceMetaNrDAO.findByDeducteeNameTin(deducteeName, deducteeTin,
					Pagination.UNPAGED);
			if (invoiceMetaNrList != null && !invoiceMetaNrList.isEmpty()) {
				Collections.sort(invoiceMetaNrList, new Comparator<InvoiceMetaNr>() {
					@Override
					public int compare(InvoiceMetaNr id1, InvoiceMetaNr id2) {
						return (id1.getCreatedDate() == null || id2.getCreatedDate() == null) ? 0
								: id1.getCreatedDate().compareTo(id2.getCreatedDate());
					}
				});
			}

			for (InvoiceMetaNr invoiceMetaNr : invoiceMetaNrList) {
				remittanceMap.put(invoiceMetaNr.getInvoiceNatureOfRemittance(), invoiceMetaNr.getInvoiceMetaNrId());
			}
			for (Map.Entry<String, Integer> entry : remittanceMap.entrySet()) {
				NORemittanceDTO nOR = new NORemittanceDTO();
				nOR.setInvoiceNatureOfRemittance(entry.getKey());
				nOR.setId(entry.getValue()); // entry.getValue()
				norList.add(nOR);
			}
		}
		map.put("natureOfRemittance", norList);
		map.put("deducteeName", deducteeName);
		map.put("pan", pan);
		map.put("lineItemNumber", lineItemNumber);
		map.put("lineItemDate", lineItemDate);
		map.put("invoiceMetaInr", invoiceMetaNrDTO);
		map.put("isMetaDataExists", isMetaDataExists);
		map.put("serviceDescription", serviceDescription);
		map.put("serviceDescriptionGl", serviceDescriptionGl);
		map.put("serviceDescriptionPo", serviceDescriptionPo);
		map.put("serviceDescriptionInvoice", serviceDescriptionInvoice);
		map.put("sacDecription", sacDecription);
		map.put("lineItemAmount", lineItemAmount);
		map.put("clientSection", clientSection);
		return map;
	}

	/**
	 * Get the Invoice Line Data Based on Nature of remittance.
	 * 
	 * @return
	 */
	public Map<String, Object> getInvoiceMetaByNatureOfRemittance(String natureOfRemittance, Integer invoiceMetaNrId) {

		Map<String, Object> map = new HashMap<>();
		InvoiceMetaNr invoiceMetaNrObj = new InvoiceMetaNr();
		InvoiceMetaNrResponseDTO response = new InvoiceMetaNrResponseDTO();
		// Optional<InvoiceMetaNr> invoiceMeta =
		// invoiceMetaNrRepository.findByNatureOfRemittance(invoiceMetaNrId,
		// natureOfRemittance);
		List<InvoiceMetaNr> invoiceMeta = invoiceMetaNrDAO.findByNatureOfRemittance(invoiceMetaNrId,
				natureOfRemittance);
		if (!invoiceMeta.isEmpty()) {
			invoiceMetaNrObj = invoiceMeta.get(0);
			BeanUtils.copyProperties(invoiceMetaNrObj, response);
		}

		map.put("invoiceMetaInr", response);
		return map;
	}

	/**
	 * Update the Invoice Line Item Based on tan and id.
	 *
	 * @param tan
	 * @param invoiceLineItemDTO
	 * @return
	 */
	public InvoiceLineItem updateInvoiceLineItemById(String tan, InvoiceLineItemDTO invoiceLineItemDTO,
			String userName) {

		logger.info("Request InvoiceLineItem is: {}", invoiceLineItemDTO);
		// InvoiceLineItem.Key lineItemKey = new
		// InvoiceLineItem.Key(invoiceLineItemDTO.getKey().getAssessmentYear(), tan,
		// invoiceLineItemDTO.getDocumentPostingDate(),
		// invoiceLineItemDTO.getKey().getId());

		// Optional<InvoiceLineItem> invoiceLine =
		// invoiceLineItemRepository.findById(lineItemKey);
		List<InvoiceLineItem> invoiceLine = invoiceLineItemDAO.findByYearTanDocumentPostingDateId(
				invoiceLineItemDTO.getAssessmentYear(), tan, invoiceLineItemDTO.getDocumentPostingDate(),
				invoiceLineItemDTO.getId());
		InvoiceLineItem invoiceLineItemData = null;
		if (!invoiceLine.isEmpty()) {
			invoiceLineItemData = invoiceLine.get(0);

			invoiceLineItemData.setActive(invoiceLineItemDTO.getActive());
			invoiceLineItemData.setAccountingVoucherDate(invoiceLineItemDTO.getAccountingVoucherDate());
			invoiceLineItemData.setAccountingVoucherNumber(invoiceLineItemDTO.getAccountingVoucherNumber());
			invoiceLineItemData.setAdvanceAppliedOn(invoiceLineItemDTO.getAdvanceAppliedOn());
			invoiceLineItemData.setAmountPaidCredited(invoiceLineItemDTO.getAmountPaidCredited());
			invoiceLineItemData.setBatchUploadId(invoiceLineItemDTO.getBatchUploadId());
			invoiceLineItemData.setCessAmount(invoiceLineItemDTO.getCessAmount());
			invoiceLineItemData.setCessRate(invoiceLineItemDTO.getCessRate());
			invoiceLineItemData.setCgstAmount(invoiceLineItemDTO.getCgstAmount());
			invoiceLineItemData.setCgstRate(invoiceLineItemDTO.getCgstRate());
			invoiceLineItemData.setClientAmount(invoiceLineItemDTO.getClientAmount());
			invoiceLineItemData.setClientRate(invoiceLineItemDTO.getClientRate());
			invoiceLineItemData.setClientSection(invoiceLineItemDTO.getClientSection());
			invoiceLineItemData.setCompanyCode(invoiceLineItemDTO.getCompanyCode());
			invoiceLineItemData.setCompanyName(invoiceLineItemDTO.getCompanyName());
			invoiceLineItemData.setCreditable(invoiceLineItemDTO.getCreditable());
			invoiceLineItemData.setCreditDebitNote(invoiceLineItemDTO.getCreditDebitNote());
			invoiceLineItemData.setDateAtWhichTdsIsDeposited(invoiceLineItemDTO.getDateAtWhichTdsIsDeposited());
			invoiceLineItemData.setDateOnWhichTdsIsDeducted(invoiceLineItemDTO.getDateOnWhichTdsIsDeducted());
			invoiceLineItemData.setDeducteeAddress(invoiceLineItemDTO.getDeducteeAddress());
			invoiceLineItemData.setDeducteeName(invoiceLineItemDTO.getDeducteeName());
			invoiceLineItemData.setDeducteeTin(invoiceLineItemDTO.getDeducteeTin());
			invoiceLineItemData.setDeductorCode(invoiceLineItemDTO.getDeductorCode());
			invoiceLineItemData.setDeductorGstin(invoiceLineItemDTO.getDeductorGstin());
			invoiceLineItemData.setDerivedTdsAmount(invoiceLineItemDTO.getDerivedTdsAmount());
			invoiceLineItemData.setDerivedTdsRate(invoiceLineItemDTO.getDerivedTdsRate());
			invoiceLineItemData.setDerivedTdsSection(invoiceLineItemDTO.getDerivedTdsSection());
			invoiceLineItemData.setDocumentDate(invoiceLineItemDTO.getDocumentDate());
			invoiceLineItemData.setDocumentNumber(invoiceLineItemDTO.getDocumentNumber());
			invoiceLineItemData.setDocumentType(invoiceLineItemDTO.getDocumentType());
			invoiceLineItemData.setFinalReason(invoiceLineItemDTO.getFinalReason());
			invoiceLineItemData.setFinalTdsAmount(invoiceLineItemDTO.getFinalTdsAmount());
			invoiceLineItemData.setFinalTdsRate(invoiceLineItemDTO.getFinalTdsRate());
			invoiceLineItemData.setFinalTdsSection(invoiceLineItemDTO.getFinalTdsSection());
			invoiceLineItemData.setGlAccountCode(invoiceLineItemDTO.getGlAccountCode());
			invoiceLineItemData.setGrossIndicator(invoiceLineItemDTO.getGrossIndicator());
			invoiceLineItemData.setGstin(invoiceLineItemDTO.getGstin());
			invoiceLineItemData.setHasAdvance(invoiceLineItemDTO.getHasAdvance());
			invoiceLineItemData.setHasLdc(invoiceLineItemDTO.getHasLdc());
			invoiceLineItemData.setHasProvision(invoiceLineItemDTO.getHasProvision());
			invoiceLineItemData.setHsnSacCode(invoiceLineItemDTO.getHsnSacCode());
			invoiceLineItemData.setIgstRate(invoiceLineItemDTO.getIgstRate());
			invoiceLineItemData.setIgstAmount(invoiceLineItemDTO.getIgstAmount());
			invoiceLineItemData.setIsAmendment(invoiceLineItemDTO.getIsAdvanceSplitRecord());
			invoiceLineItemData.setAdvanceIsSplitRecord(invoiceLineItemDTO.getIsAdvanceSplitRecord());
			invoiceLineItemData.setIsLdcSplitRecord(invoiceLineItemDTO.getIsLdcSplitRecord());
			invoiceLineItemData.setIsMergeRecord(invoiceLineItemDTO.getIsMergeRecord());
			invoiceLineItemData.setInterest(invoiceLineItemDTO.getInterest());
			invoiceLineItemData.setInvoiceAmount(invoiceLineItemDTO.getInvoiceAmount());
			invoiceLineItemData.setInvoiceType(invoiceLineItemDTO.getInvoiceType());
			invoiceLineItemData.setIsMismatch(invoiceLineItemDTO.getIsMismatch());
			invoiceLineItemData.setIsProvisionSplitRecord(invoiceLineItemDTO.getIsProvisionSplitRecord());
			invoiceLineItemData.setIsResident(invoiceLineItemDTO.getIsResident());
			invoiceLineItemData.setIsSplitRecord(invoiceLineItemDTO.getIsSplitRecord());
			invoiceLineItemData.setLdcAppliedOn(invoiceLineItemDTO.getLdcAppliedOn());
			invoiceLineItemData.setLineItemNumber(invoiceLineItemDTO.getLineItemNumber());
			invoiceLineItemData.setLinkedAdvanceNumber(invoiceLineItemDTO.getLinkedAdvanceNumber());
			invoiceLineItemData.setMigoNumber(invoiceLineItemDTO.getMigoNumber());
			invoiceLineItemData.setMiroNumber(invoiceLineItemDTO.getMiroNumber());
			invoiceLineItemData.setMismatchCategory(invoiceLineItemDTO.getMismatchCategory());
			invoiceLineItemData.setMismatchInterpretation(invoiceLineItemDTO.getMismatchInterpretation());
			invoiceLineItemData.setMismatchModifiedDate(invoiceLineItemDTO.getMismatchModifiedDate());
			invoiceLineItemData.setModifiedBy(userName);
			invoiceLineItemData.setModifiedDate(new Date());
			invoiceLineItemData.setOriginalDocumentDate(invoiceLineItemDTO.getOriginalDocumentDate());
			invoiceLineItemData.setOriginalDocumentNumber(invoiceLineItemDTO.getOriginalDocumentNumber());
			invoiceLineItemData.setPan(invoiceLineItemDTO.getPan());
			invoiceLineItemData.setPenalty(invoiceLineItemDTO.getPenalty());
			invoiceLineItemData.setPoDate(invoiceLineItemDTO.getPoDate());
			invoiceLineItemData.setPoNumber(invoiceLineItemDTO.getPoNumber());
			invoiceLineItemData.setPos(invoiceLineItemDTO.getPos());
			invoiceLineItemData.setProcessedFrom(invoiceLineItemDTO.getProcessedFrom());
			invoiceLineItemData.setProvisionAppliedOn(invoiceLineItemDTO.getProvisionAppliedOn());
			invoiceLineItemData.setQtySupplied(invoiceLineItemDTO.getQtySupplied());
			invoiceLineItemData.setSacDecription(invoiceLineItemDTO.getSacDecription());
			invoiceLineItemData.setSection(invoiceLineItemDTO.getSection());
			invoiceLineItemData.setSequenceNumber(invoiceLineItemDTO.getSequenceNumber());
			invoiceLineItemData.setServiceDescriptionGl(invoiceLineItemDTO.getServiceDescriptionGl());
			invoiceLineItemData.setServiceDescriptionInvoice(invoiceLineItemDTO.getServiceDescriptionInvoice());
			invoiceLineItemData.setServiceDescriptionPo(invoiceLineItemDTO.getServiceDescriptionPo());
			invoiceLineItemData.setSgstAmount(invoiceLineItemDTO.getSgstAmount());
			invoiceLineItemData.setSgstRate(invoiceLineItemDTO.getSgstRate());
			invoiceLineItemData.setSourceFileName(invoiceLineItemDTO.getSourceFileName());
			invoiceLineItemData.setSurcharge(invoiceLineItemDTO.getSurcharge());
			invoiceLineItemData.setTdsAmount(invoiceLineItemDTO.getTdsAmount());
			invoiceLineItemData.setTdsDeducted(invoiceLineItemDTO.getTdsDeducted());
			invoiceLineItemData.setTdsRate(invoiceLineItemDTO.getTdsRate());
			invoiceLineItemData.setTdsSection(invoiceLineItemDTO.getTdsSection());
			invoiceLineItemData.setThresholdLimit(invoiceLineItemDTO.getThresholdLimit());
			invoiceLineItemData.setUnitOfMeasurement(invoiceLineItemDTO.getUnitOfMeasurement());
			invoiceLineItemData.setUserDefinedField1(invoiceLineItemDTO.getUserDefinedField1());
			invoiceLineItemData.setUserDefinedField2(invoiceLineItemDTO.getUserDefinedField2());
			invoiceLineItemData.setUserDefinedField3(invoiceLineItemDTO.getUserDefinedField3());
			invoiceLineItemData.setVendorInvoiceNumber(invoiceLineItemDTO.getVendorInvoiceNumber());

			invoiceLineItemDAO.update(invoiceLineItemData);
		} else {
			throw new CustomException("Data is not present with  " + invoiceLineItemDTO.getId(), HttpStatus.NOT_FOUND);
		}
		return invoiceLineItemData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public File convertInvoiceCsvToxlsx(File csvFile, String tan, String tenantId, String deductorPan,
			Integer assesmentYear, Integer batchID) throws Exception {
		Workbook workbook = null;
		Reader reader = null;
		CsvToBean<InvoiceErrorReportCsvDTO> csvToBean = null;
		List<InvoiceLineItem> errorRecords = null;

		errorRecords = invoiceLineItemDAO.getInvoiceErrorRecords(tan, assesmentYear, batchID);
		if (errorRecords.isEmpty()) {
			reader = new FileReader(csvFile);
			csvToBean = new CsvToBeanBuilder(reader).withType(InvoiceErrorReportCsvDTO.class)
					.withIgnoreLeadingWhiteSpace(true).build();
		}
		workbook = invoiceXlsxReport(csvToBean != null ? csvToBean.parse() : null, errorRecords, tan, tenantId,
				deductorPan, assesmentYear, batchID);
		// Write the output to the file
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxInvoiceFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()) + "_Error_Report",
				".xlsx");

		FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
		baout.close();
		xlsxInvoiceFile.deleteOnExit();
		return xlsxInvoiceFile;

	}

	public Workbook invoiceXlsxReport(List<InvoiceErrorReportCsvDTO> invoiceErrorReportsCsvList,
			List<InvoiceLineItem> errorRecords, String tan, String tenantId, String deductorPan, Integer assesmentYear,
			Integer batchID) throws Exception {

		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		worksheet.getCells().importArray(ErrorReportService.invoiceLineItemheaderNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (errorRecords.isEmpty()) {
			setExtractDataForInvoice(invoiceErrorReportsCsvList, worksheet, tan, deductorData.getDeductorName());
		} else {
			setExtractDataForInvoiceWithDataFromDB(errorRecords, worksheet, tan, deductorData.getDeductorName());
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

		// Style for D6 to CI6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:CI6");
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
		cellA1.setValue("Invoice Error Report (Dated: " + date + ")");
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

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "CI6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:CI6");
		return workbook;
	}

	private void setExtractDataForInvoice(List<InvoiceErrorReportCsvDTO> invoiceErrorReportsCsvList,
			Worksheet worksheet, String tan, String deductorName) throws Exception {
		if (!invoiceErrorReportsCsvList.isEmpty()) {
			int rowIndex = 6;
			List<ErrorCode> errorCodesObjs = mastersClient.getAllErrorCodes().getBody().getData();
			// errorCodeService.findAll();
			Map<String, String> errorDescription = new HashMap<>();
			for (ErrorCode errorCodesObj : errorCodesObjs) {
				errorDescription.put(errorCodesObj.getCode(), errorCodesObj.getDescription());
			}
			int index = 0;
			for (InvoiceErrorReportCsvDTO errorReportsDTO : invoiceErrorReportsCsvList) {
				// startDate = new Date();
				index++;
				ArrayList<String> rowData = new ArrayList<String>();
				String errorCodesWithNewLine = StringUtils.EMPTY;
				// getting error codes and spliting
				Map<String, String> errorCodesMap = new HashMap<>();
				List<String> givenCodes = new ArrayList<>();
				List<String> errorCodesWithOnlyDes = new ArrayList<>();
				if (StringUtils.isEmpty(errorReportsDTO.getReason()) || !errorReportsDTO.getReason().contains("-")) {
					errorCodesWithNewLine = StringUtils.isBlank(errorReportsDTO.getReason()) ? StringUtils.EMPTY
							: errorReportsDTO.getReason().trim().endsWith("/")
									? errorReportsDTO.getReason().trim().substring(0,
											errorReportsDTO.getReason().trim().length() - 1)
									: errorReportsDTO.getReason().trim();
				} else {
					String[] errorWithColumns = errorReportsDTO.getReason().split("/");
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

				rowData.add(tan); // Deductor TAN
				rowData.add(StringUtils.isBlank(errorCodesWithNewLine) ? StringUtils.EMPTY : errorCodesWithNewLine); // Error
																														// Message
				rowData.add(index + ""); // Sequence Number
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceIdentifier()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceIdentifier()); // Source Identifier
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceFileName()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceFileName()); // Source File Name
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorCode()); // DeductorCode
				rowData.add(deductorName); // DeductorName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorPan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorPan()); // Deductor PAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorTan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorTan()); // Deductor TAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorGstin()); // Deductor GSTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeCode()); // DeducteeCode
				rowData.add(StringUtils.isBlank(errorReportsDTO.getNameOfTheDeductee()) ? StringUtils.EMPTY
						: errorReportsDTO.getNameOfTheDeductee()); // DeducteeName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteePan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteePan()); // DeducteePAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeTin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeTin()); // DeducteeTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeGstin()); // DeducteeGSTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getVendorInvoiceNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getVendorInvoiceNumber()); // VendorInvoiceNumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentDate()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentDate()); // DocumentDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getErpDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getErpDocumentNumber()); // ERPDocumentNumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPostingDateOfDocument()) ? StringUtils.EMPTY
						: errorReportsDTO.getPostingDateOfDocument()); // PostingDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPaymentdate()) ? StringUtils.EMPTY
						: errorReportsDTO.getPaymentdate()); // PaymentDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTdsDeductionDate()) ? StringUtils.EMPTY
						: errorReportsDTO.getTdsDeductionDate()); // TDSDeductionDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentType()); // DocumentType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSupplytype()) ? StringUtils.EMPTY
						: errorReportsDTO.getSupplytype()); // SupplyType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getMigoNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getMigoNumber()); // MIGONumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getMiroNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getMiroNumber()); // MIRONumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getErpDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getErpDocumentType()); // ERPDocumentType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getLineItemNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getLineItemNumber()); // LineItemNumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getHsnSac()) ? StringUtils.EMPTY
						: errorReportsDTO.getHsnSac()); // HSNorSAC
				rowData.add(""); // HSNorSACDesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getServiceDescriptionInvoice()) ? StringUtils.EMPTY
						: errorReportsDTO.getServiceDescriptionInvoice()); // InvoiceDesc
				rowData.add(""); // GLAccountCode
				rowData.add(""); // GLAccountName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPoNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getPoNumber()); // PONumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPoDate()) ? StringUtils.EMPTY
						: errorReportsDTO.getPoDate()); // PODate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getServiceDescriptionPo()) ? StringUtils.EMPTY
						: errorReportsDTO.getServiceDescriptionPo());// PODesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTaxableValue()) ? StringUtils.EMPTY
						: errorReportsDTO.getTaxableValue()); // TaxableValue
				rowData.add(StringUtils.isBlank(errorReportsDTO.getIgstRate()) ? StringUtils.EMPTY
						: errorReportsDTO.getIgstRate()); // IGSTRate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getIgstAmount()) ? StringUtils.EMPTY
						: errorReportsDTO.getIgstAmount()); // IGSTAmount
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCgstRate()) ? StringUtils.EMPTY
						: errorReportsDTO.getCgstRate()); // CGSTRate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCgstAmount()) ? StringUtils.EMPTY
						: errorReportsDTO.getCgstAmount()); // CGSTAmount
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSgstRate()) ? StringUtils.EMPTY
						: errorReportsDTO.getSgstRate()); // SGSTRate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSgstAmount()) ? StringUtils.EMPTY
						: errorReportsDTO.getSgstAmount()); // SGSTAmount
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCessRate()) ? StringUtils.EMPTY
						: errorReportsDTO.getCessRate()); // CESSRate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCessAmount()) ? StringUtils.EMPTY
						: errorReportsDTO.getCessAmount()); // CESSAmount
				rowData.add(
						StringUtils.isBlank(errorReportsDTO.getPos()) ? StringUtils.EMPTY : errorReportsDTO.getPos()); // POS
				rowData.add(""); // TDSTaxCodeERP
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTdsSection()) ? StringUtils.EMPTY
						: errorReportsDTO.getTdsSection()); // TDSSection
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTdsRate()) ? StringUtils.EMPTY
						: errorReportsDTO.getTdsRate()); // TDSRate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTdsAmount()) ? StringUtils.EMPTY
						: errorReportsDTO.getTdsAmount()); // TDSAmount
				rowData.add(""); // LinkedAdvanceIndicator
				rowData.add(""); // LinkedProvisionIndicator
				rowData.add(""); // ProvisionAdjustmentFlag
				rowData.add(StringUtils.isBlank(errorReportsDTO.getAdvanceAdjustmentFlag()) ? StringUtils.EMPTY
						: errorReportsDTO.getAdvanceAdjustmentFlag()); // AdvanceAdjustmentFlag
				rowData.add(""); // ChallanPaidFlag
				rowData.add(""); // ChallanPaidDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGrossingUpIndicator()) ? StringUtils.EMPTY
						: errorReportsDTO.getGrossingUpIndicator()); // GrossUpIndicator
				rowData.add(StringUtils.isBlank(errorReportsDTO.getOriginalDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getOriginalDocumentNumber()); // OriginalDocumentNumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getOriginalDocumentDate()) ? StringUtils.EMPTY
						: errorReportsDTO.getOriginalDocumentDate()); // OriginalDocumentDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getRefey3()) ? StringUtils.EMPTY
						: errorReportsDTO.getRefey3()); // RefKey3
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField1()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField1());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField2()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField2());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField3()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField3());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getNoTdsReason()) ? StringUtils.EMPTY
						: errorReportsDTO.getNoTdsReason());

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}
	}

	private void setExtractDataForInvoiceWithDataFromDB(List<InvoiceLineItem> invoiceErrorRecords, Worksheet worksheet,
			String tan, String deductorName) throws Exception {
		logger.info("Report is preparing with records from DB  {}");
		if (!invoiceErrorRecords.isEmpty()) {
			int rowIndex = 6;
			List<ErrorCode> errorCodesObjs = mastersClient.getAllErrorCodes().getBody().getData();
			// errorCodeService.findAll();
			Map<String, String> errorDescription = new HashMap<>();
			for (ErrorCode errorCodesObj : errorCodesObjs) {
				errorDescription.put(errorCodesObj.getCode(), errorCodesObj.getDescription());
			}
			int index = 0;
			for (InvoiceLineItem errorReportsDTO : invoiceErrorRecords) {
				// startDate = new Date();
				index++;
				ArrayList<String> rowData = new ArrayList<String>();
				String errorCodesWithNewLine = StringUtils.EMPTY;
				// getting error codes and spliting
				Map<String, String> errorCodesMap = new HashMap<>();
				List<String> givenCodes = new ArrayList<>();
				List<String> errorCodesWithOnlyDes = new ArrayList<>();
				if (StringUtils.isEmpty(errorReportsDTO.getErrorReason())
						|| !errorReportsDTO.getErrorReason().contains("-")) {
					errorCodesWithNewLine = StringUtils.isBlank(errorReportsDTO.getErrorReason()) ? StringUtils.EMPTY
							: errorReportsDTO.getErrorReason().trim().endsWith("/")
									? errorReportsDTO.getErrorReason().trim().substring(0,
											errorReportsDTO.getErrorReason().trim().length() - 1)
									: errorReportsDTO.getErrorReason().trim();
				} else {
					String[] errorWithColumns = errorReportsDTO.getErrorReason().split("/");
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
				rowData.add(tan); // Deductor TAN
				rowData.add(StringUtils.isBlank(errorCodesWithNewLine) ? StringUtils.EMPTY : errorCodesWithNewLine); // Error
																														// Message
				rowData.add(index + ""); // Sequence Number
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceIdentifier()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceIdentifier()); // Source Identifier
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceFileName()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceFileName()); // Source File Name
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorCode()); // DeductorCode
				rowData.add(deductorName); // DeductorName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorPan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorPan()); // Deductor PAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorTan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorTan()); // Deductor TAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorGstin()); // Deductor GSTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeCode()); // DeducteeCode
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeName()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeName()); // DeducteeName
				rowData.add(
						StringUtils.isBlank(errorReportsDTO.getPan()) ? StringUtils.EMPTY : errorReportsDTO.getPan()); // DeducteePAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeTin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeTin()); // DeducteeTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getGstin()); // DeducteeGSTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getVendorInvoiceNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getVendorInvoiceNumber()); // VendorInvoiceNumber
				rowData.add(errorReportsDTO.getDocumentDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentDate().toString()); // DocumentDate
				rowData.add(errorReportsDTO.getDocumentNumber() == null ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentNumber().toString()); // ERPDocumentNumber
				rowData.add(errorReportsDTO.getDocumentPostingDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentPostingDate().toString()); // PostingDate
				rowData.add(errorReportsDTO.getPaymentDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getPaymentDate().toString()); // PaymentDate
				rowData.add(errorReportsDTO.getTdsDeductionDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getTdsDeductionDate().toString()); // TDSDeductionDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentType()); // DocumentType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSupplyType()) ? StringUtils.EMPTY
						: errorReportsDTO.getSupplyType()); // SupplyType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getMigoNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getMigoNumber()); // MIGONumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getMiroNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getMiroNumber()); // MIRONumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentType()); // ERPDocumentType
				rowData.add(errorReportsDTO.getLineItemNumber() == null ? StringUtils.EMPTY
						: errorReportsDTO.getLineItemNumber().toString()); // LineItemNumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getHsnSacCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getHsnSacCode()); // HSNorSAC
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSacDecription()) ? StringUtils.EMPTY
						: errorReportsDTO.getSacDecription()); // HSNorSACDesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getServiceDescriptionInvoice()) ? StringUtils.EMPTY
						: errorReportsDTO.getServiceDescriptionInvoice()); // InvoiceDesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGlAccountCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getGlAccountCode()); // GLAccountCode
				rowData.add(StringUtils.isBlank(errorReportsDTO.getServiceDescriptionGl()) ? StringUtils.EMPTY
						: errorReportsDTO.getServiceDescriptionGl()); // GLAccountName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPoNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getPoNumber()); // PONumber
				rowData.add(errorReportsDTO.getPoDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getPoDate().toString()); // PODate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getServiceDescriptionPo()) ? StringUtils.EMPTY
						: errorReportsDTO.getServiceDescriptionPo());// PODesc
				rowData.add(errorReportsDTO.getInvoiceAmount() == null ? StringUtils.EMPTY
						: errorReportsDTO.getInvoiceAmount().toString()); // TaxableValue
				rowData.add(errorReportsDTO.getIgstRate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getIgstRate().toString()); // IGSTRate
				rowData.add(errorReportsDTO.getIgstAmount() == null ? StringUtils.EMPTY
						: errorReportsDTO.getIgstAmount().toString()); // IGSTAmount
				rowData.add(errorReportsDTO.getCgstRate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getCgstRate().toString()); // CGSTRate
				rowData.add(errorReportsDTO.getCgstAmount() == null ? StringUtils.EMPTY
						: errorReportsDTO.getCgstAmount().toString()); // CGSTAmount
				rowData.add(errorReportsDTO.getSgstRate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getSgstRate().toString()); // SGSTRate
				rowData.add(errorReportsDTO.getSgstAmount() == null ? StringUtils.EMPTY
						: errorReportsDTO.getSgstAmount().toString()); // SGSTAmount
				rowData.add(errorReportsDTO.getCessRate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getCessRate().toString()); // CESSRate
				rowData.add(errorReportsDTO.getCessAmount() == null ? StringUtils.EMPTY
						: errorReportsDTO.getCessAmount().toString()); // CESSAmount
				rowData.add(
						StringUtils.isBlank(errorReportsDTO.getPos()) ? StringUtils.EMPTY : errorReportsDTO.getPos()); // POS
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSectionCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getSectionCode()); // TDSTaxCodeERP
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTdsSection()) ? StringUtils.EMPTY
						: errorReportsDTO.getTdsSection()); // TDSSection
				rowData.add(errorReportsDTO.getTdsRate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getTdsRate().toString()); // TDSRate
				rowData.add(errorReportsDTO.getTdsAmount() == null ? StringUtils.EMPTY
						: errorReportsDTO.getTdsAmount().toString()); // TDSAmount
				rowData.add(StringUtils.isBlank(errorReportsDTO.getLinkedAdvanceNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getLinkedAdvanceNumber()); // LinkedAdvanceIndicator
				rowData.add(StringUtils.isBlank(errorReportsDTO.getLinkedProvisionNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getLinkedProvisionNumber()); // LinkedProvisionIndicator
				rowData.add(errorReportsDTO.getProvisionCanAdjust() != null
						&& errorReportsDTO.getProvisionCanAdjust() == true ? "Y" : "N"); // ProvisionAdjustmentFlag
				rowData.add(
						errorReportsDTO.getAdvanceCanAdjust() != null && errorReportsDTO.getAdvanceCanAdjust() == true
								? "Y"
								: "N"); // AdvanceAdjustmentFlag
				rowData.add(StringUtils.EMPTY); // ChallanPaidFlag
				rowData.add(StringUtils.EMPTY); // ChallanPaidDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGrossingUpIndicator()) ? StringUtils.EMPTY
						: errorReportsDTO.getGrossingUpIndicator()); // GrossUpIndicator
				rowData.add(StringUtils.isBlank(errorReportsDTO.getOriginalDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getOriginalDocumentNumber()); // OriginalDocumentNumber
				rowData.add(errorReportsDTO.getOriginalDocumentDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getOriginalDocumentDate().toString()); // OriginalDocumentDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getRefKey3()) ? StringUtils.EMPTY
						: errorReportsDTO.getRefKey3()); // RefKey3
				rowData.add(StringUtils.isBlank(errorReportsDTO.getBusinessPlace()) ? StringUtils.EMPTY
						: errorReportsDTO.getBusinessPlace());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getBusinessArea()) ? StringUtils.EMPTY
						: errorReportsDTO.getBusinessArea());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPlant()) ? StringUtils.EMPTY
						: errorReportsDTO.getPlant());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getProfitCenter()) ? StringUtils.EMPTY
						: errorReportsDTO.getProfitCenter());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getAssignmentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getAssignmentNumber());
				rowData.add(errorReportsDTO.getTdsBaseValue() == null ? StringUtils.EMPTY
						: errorReportsDTO.getTdsBaseValue().toString());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPoItemNo()) ? StringUtils.EMPTY
						: errorReportsDTO.getPoItemNo());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTypeOfTransaction()) ? StringUtils.EMPTY
						: errorReportsDTO.getTypeOfTransaction());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserName()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserName());
				rowData.add(StringUtils.EMPTY);
				rowData.add(StringUtils.EMPTY);
				rowData.add(StringUtils.EMPTY);
				rowData.add(StringUtils.isBlank(errorReportsDTO.getItemCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getItemCode());
				rowData.add(errorReportsDTO.getInvoiceValue() == null ? StringUtils.EMPTY
						: errorReportsDTO.getInvoiceValue().toString());
				rowData.add(errorReportsDTO.getSaaNumber() == null ? StringUtils.EMPTY
						: errorReportsDTO.getSaaNumber().toString());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getResident()) ? StringUtils.EMPTY
						: errorReportsDTO.getResident());
				rowData.add(errorReportsDTO.getTdsRemittancedate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getTdsRemittancedate().toString());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDebitCreditIndicator()) ? StringUtils.EMPTY
						: errorReportsDTO.getDebitCreditIndicator());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField1()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField1());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField2()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField2());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField3()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField3());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField4()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField4());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField5()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField5());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField6()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField6());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField7()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField7());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField8()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField8());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField9()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField9());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField10()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField10());

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}
	}

	/**
	 * This method for Convert CSV To Invoic Excel File
	 * 
	 * @param csvFile
	 * @param tan
	 * @return
	 * @throws Exception
	 */
	public File convertInvoiceOtherReportToXlsx(File csvFile, String tan) throws Exception {

		Reader reader = new FileReader(csvFile);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		CsvToBean<InvoiceErrorReportCsvDTO> csvToBean = new CsvToBeanBuilder(reader)
				.withType(InvoiceErrorReportCsvDTO.class).withIgnoreLeadingWhiteSpace(true).build();

		// Aspose method to convert
		Workbook workbook = generateInvoiceOtherXlsxReport(csvToBean.parse(), tan);

		// Write the output to the file
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxInvoiceFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()) + "_Other", ".xlsx");
		FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
		baout.close();
		xlsxInvoiceFile.deleteOnExit();
		return xlsxInvoiceFile;
	}

	/**
	 * This method for Set Cell Headers and Colors.
	 * 
	 * @param invoiceExcelCsvDTO
	 * @param tan
	 * @return
	 * @throws Exception
	 */
	public Workbook generateInvoiceOtherXlsxReport(List<InvoiceErrorReportCsvDTO> invoiceExcelCsvDTO, String tan)
			throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		worksheet.getCells().importArray(invoiceOtherErrorReportheaderNames, 0, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForInvoiceOtherReport(invoiceExcelCsvDTO, worksheet);

		// Style for A1 to AY1 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(91, 155, 213));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A1:AZ1");
		headerColorRange1.setStyle(style1);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A1";
		String lastHeaderCellName = "AZ1";
		String firstDataCellName = "A2";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);
		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:AZ1");

		return workbook;
	}

	/**
	 * This method for set from CSV Data to Excel Data.
	 * 
	 * @param invoiceExcelCsvDTO
	 * @param worksheet
	 * @throws Exception
	 */
	private void setExtractDataForInvoiceOtherReport(List<InvoiceErrorReportCsvDTO> invoiceExcelCsvDTO,
			Worksheet worksheet) throws Exception {

		if (!invoiceExcelCsvDTO.isEmpty()) {
			int rowIndex = 1;
			for (InvoiceErrorReportCsvDTO excelReportsDTO : invoiceExcelCsvDTO) {
				List<String> rowData = new ArrayList<String>();

				rowData.add(StringUtils.isBlank(excelReportsDTO.getSourceFileName()) ? StringUtils.EMPTY
						: excelReportsDTO.getSourceFileName());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getCompanyCode()) ? StringUtils.EMPTY
						: excelReportsDTO.getCompanyCode());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getNameOfTheCompanyCode()) ? StringUtils.EMPTY
						: excelReportsDTO.getNameOfTheCompanyCode());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getDeductorPan()) ? StringUtils.EMPTY
						: excelReportsDTO.getDeductorPan());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getDeductorTan()) ? StringUtils.EMPTY
						: excelReportsDTO.getDeductorTan());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getDeductorGstin()) ? StringUtils.EMPTY
						: excelReportsDTO.getDeductorGstin());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getDeducteeCode()) ? StringUtils.EMPTY
						: excelReportsDTO.getDeducteeCode());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getNonResidentDeducteeIndicator()) ? StringUtils.EMPTY
						: excelReportsDTO.getNonResidentDeducteeIndicator());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getDeducteePan()) ? StringUtils.EMPTY
						: excelReportsDTO.getDeducteePan());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getDeducteeTin()) ? StringUtils.EMPTY
						: excelReportsDTO.getDeducteeTin());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getDeducteeGstin()) ? StringUtils.EMPTY
						: excelReportsDTO.getDeducteeGstin());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getNameOfTheDeductee()) ? StringUtils.EMPTY
						: excelReportsDTO.getNameOfTheDeductee());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getDeducteeAddress()) ? StringUtils.EMPTY
						: excelReportsDTO.getDeducteeAddress());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getVendorInvoiceNumber()) ? StringUtils.EMPTY
						: excelReportsDTO.getVendorInvoiceNumber());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getErpDocumentNumber()) ? StringUtils.EMPTY
						: excelReportsDTO.getErpDocumentNumber());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getMiroNumber()) ? StringUtils.EMPTY
						: excelReportsDTO.getMiroNumber());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getMigoNumber()) ? StringUtils.EMPTY
						: excelReportsDTO.getMigoNumber());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: excelReportsDTO.getDocumentType());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getErpDocumentNumber()) ? StringUtils.EMPTY
						: excelReportsDTO.getErpDocumentNumber());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getDocumentDate()) ? StringUtils.EMPTY
						: excelReportsDTO.getDocumentDate());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getPostingDateOfDocument()) ? StringUtils.EMPTY
						: excelReportsDTO.getPostingDateOfDocument());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getLineItemNumber()) ? StringUtils.EMPTY
						: excelReportsDTO.getLineItemNumber());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getHsnSac()) ? StringUtils.EMPTY
						: excelReportsDTO.getHsnSac());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getSacDescription()) ? StringUtils.EMPTY
						: excelReportsDTO.getSacDescription());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getServiceDescriptionInvoice()) ? StringUtils.EMPTY
						: excelReportsDTO.getServiceDescriptionInvoice());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getServiceDescriptionPo()) ? StringUtils.EMPTY
						: excelReportsDTO.getServiceDescriptionPo());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getServiceDescriptionGlText()) ? StringUtils.EMPTY
						: excelReportsDTO.getServiceDescriptionGlText());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getTaxableValue()) ? StringUtils.EMPTY
						: excelReportsDTO.getTaxableValue());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getIgstRate()) ? StringUtils.EMPTY
						: excelReportsDTO.getIgstRate());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getIgstAmount()) ? StringUtils.EMPTY
						: excelReportsDTO.getIgstAmount());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getCgstRate()) ? StringUtils.EMPTY
						: excelReportsDTO.getCgstRate());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getCgstAmount()) ? StringUtils.EMPTY
						: excelReportsDTO.getCgstAmount());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getSgstRate()) ? StringUtils.EMPTY
						: excelReportsDTO.getSgstRate());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getSgstAmount()) ? StringUtils.EMPTY
						: excelReportsDTO.getSgstAmount());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getCessRate()) ? StringUtils.EMPTY
						: excelReportsDTO.getCessRate());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getCessAmount()) ? StringUtils.EMPTY
						: excelReportsDTO.getCessAmount());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getCreditableYn()) ? StringUtils.EMPTY
						: excelReportsDTO.getCreditableYn());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getSectionCode()) ? StringUtils.EMPTY
						: excelReportsDTO.getSectionCode());
				rowData.add(
						StringUtils.isBlank(excelReportsDTO.getPos()) ? StringUtils.EMPTY : excelReportsDTO.getPos());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getTdsSection()) ? StringUtils.EMPTY
						: excelReportsDTO.getTdsSection());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getTdsRate()) ? StringUtils.EMPTY
						: excelReportsDTO.getTdsRate());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getTdsAmount()) ? StringUtils.EMPTY
						: excelReportsDTO.getTdsAmount());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getPoNumber()) ? StringUtils.EMPTY
						: excelReportsDTO.getPoNumber());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getPoDate()) ? StringUtils.EMPTY
						: excelReportsDTO.getPoDate());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getLinkedAdvanceNumber()) ? StringUtils.EMPTY
						: excelReportsDTO.getLinkedAdvanceNumber());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getGrossingUpIndicator()) ? StringUtils.EMPTY
						: excelReportsDTO.getGrossingUpIndicator());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getOriginalDocumentNumber()) ? StringUtils.EMPTY
						: excelReportsDTO.getOriginalDocumentNumber());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getOriginalDocumentDate()) ? StringUtils.EMPTY
						: excelReportsDTO.getOriginalDocumentDate());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getUserDefinedField1()) ? StringUtils.EMPTY
						: excelReportsDTO.getUserDefinedField1());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getUserDefinedField2()) ? StringUtils.EMPTY
						: excelReportsDTO.getUserDefinedField2());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getUserDefinedField3()) ? StringUtils.EMPTY
						: excelReportsDTO.getUserDefinedField3());
				rowData.add(StringUtils.isBlank(excelReportsDTO.getTdsSectionPrediction()) ? StringUtils.EMPTY
						: excelReportsDTO.getTdsSectionPrediction());

				worksheet.getCells().importArrayList((ArrayList<String>) rowData, rowIndex++, 0, false);

			}
		}
	}

	// Get Csv File Data based on Csv File
	public List<CsvFileDTO> getCsvDataBasedOnFile(String tenantId, String tan, Integer batchId) throws IOException {
		List<CsvFileDTO> listCsvData = new ArrayList<>();
		File file = null;
		int assessmentYear = Calendar.getInstance().get(Calendar.YEAR);
		List<BatchUpload> batchUploadData = batchUploadDAO.findById(assessmentYear, tan, UploadTypes.INVOICE_PDF.name(),
				batchId);
		if (!batchUploadData.isEmpty()) {
			BatchUpload batchData = batchUploadData.get(0);
			file = blobStorageService.getFileFromBlobUrl(tenantId, batchData.getFilePath());

			Reader reader = new FileReader(file);
			@SuppressWarnings({ "rawtypes", "unchecked" })
			CsvToBean<CsvFileDTO> csvToBean = new CsvToBeanBuilder(reader).withType(CsvFileDTO.class)
					.withIgnoreLeadingWhiteSpace(true).build();

			Iterator<CsvFileDTO> csvUserIterator = csvToBean.iterator();
			while (csvUserIterator.hasNext()) {
				CsvFileDTO csvUser = csvUserIterator.next();
				listCsvData.add(csvUser);
			}
			return listCsvData;
		} else {
			throw new CustomException("Cannot Get Data as no Record", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}// end of method

	public List<InvoiceLineItemDTO> getInvoiceLineItemByAssessmentYearChallanMonthDeductorTan(int assessmentYear,
			int challanMonth, List<String> deductorTan, boolean challanPaid, boolean isForNonResidents,
			Pagination pagination) {
		List<InvoiceLineItemDTO> listResponse = new ArrayList<InvoiceLineItemDTO>();
		// List<InvoiceLineItem> response = invoiceLineItemRepository
		// .getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(assessmentYear,
		// Arrays.asList(challanMonth), deductorTan,
		// challanPaid, isForNonResidents, pagination)
		List<InvoiceLineItem> response = invoiceLineItemDAO.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(
				assessmentYear, Arrays.asList(challanMonth), deductorTan, challanPaid, isForNonResidents, pagination);
		logger.info("response size : {}", response.size());
		response.forEach(invoice -> {
			InvoiceLineItemDTO invoiceLineItemDTO = new InvoiceLineItemDTO();
			BeanUtils.copyProperties(invoice, invoiceLineItemDTO);
			listResponse.add(invoiceLineItemDTO);
		});
		logger.info("List response size : {}", listResponse.size());
		return listResponse;
	}

	public BigDecimal getCurrencyRate(BigDecimal amount, String currencyType) {
		Optional<Currency> currency = currencyRepository.getByCurrentDateRate(currencyType);
		Currency currencyRate = null;
		BigDecimal rate = BigDecimal.ZERO;
		if (currency.isPresent()) {
			currencyRate = currency.get();
			rate = amount.multiply(new BigDecimal(currencyRate.getBuyingTT()));
		}
		return rate;
	}

	public InvoicePdfList readCsvForInvoicePdfProcess(File csvFile, BatchUpload batchUpload) throws Exception {

		Reader reader = new FileReader(csvFile);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		CsvToBean<InvoicePdfCsvDTO> csvToBean = new CsvToBeanBuilder(reader).withType(InvoicePdfCsvDTO.class)
				.withIgnoreLeadingWhiteSpace(true).build();

		InvoicePdfList invoicePdfList = new InvoicePdfList();
		invoicePdfList.setBatchUploadUrl(batchUpload.getFilePath());
		invoicePdfList.setInvoiceList(csvToBean.parse());

		return invoicePdfList;
	}

	public List<InvoicePDFDTO> invoicePdfSave(List<InvoicePDFDTO> invoicePdfSaveDTOs, String userName, String tan,
			Integer batchId, Integer batchUploadGroupId, String deductorPan, String token) throws ParseException {

		List<BatchUploadGroup> batchUploadGroup = batchUploadGroupDAO
				.findById(Calendar.getInstance().get(Calendar.YEAR), tan, batchUploadGroupId);

		List<InvoiceMismatchesDTO> mismatchesList = new ArrayList<>();
		for (InvoicePDFDTO invoicePdfSaveDTO : invoicePdfSaveDTOs) {
			InvoiceLineItem invoiceLineItemData = new InvoiceLineItem();
			invoiceLineItemData.setActive(true);
			invoiceLineItemData.setProcessedFrom(UploadTypes.INVOICE_PDF.name());
			invoiceLineItemData.setSourceIdentifier(UploadTypes.INVOICE_PDF.name());
			invoiceLineItemData.setIsKeyDuplicate(false);
			if (!batchUploadGroup.isEmpty()) {
				invoiceLineItemData.setIsResident(batchUploadGroup.get(0).getResidentType());
			}
			invoiceLineItemData.setBatchUploadId(batchId);
			invoiceLineItemData.setDeducteeName(invoicePdfSaveDTO.getVendorName());
			invoiceLineItemData.setClientRate(invoicePdfSaveDTO.getRate());
			invoiceLineItemData.setClientSection(invoicePdfSaveDTO.getSection());
			invoiceLineItemData.setSection(invoicePdfSaveDTO.getSection());
			invoiceLineItemData.setTdsRate(invoicePdfSaveDTO.getRate());
			invoiceLineItemData.setTdsSection(invoicePdfSaveDTO.getSection());
			invoiceLineItemData.setDerivedTdsRate(invoicePdfSaveDTO.getRate());
			invoiceLineItemData.setDerivedTdsSection(invoicePdfSaveDTO.getSection());
			invoiceLineItemData.setDocumentDate(invoicePdfSaveDTO.getInvoiceDate());
			invoiceLineItemData.setInvoiceAmount(new BigDecimal(invoicePdfSaveDTO.getItemTotal()));
			invoiceLineItemData.setServiceDescriptionInvoice(invoicePdfSaveDTO.getItemDescription());
			invoiceLineItemData.setServiceDescriptionPo(invoicePdfSaveDTO.getItemDescription());
			invoiceLineItemData.setServiceDescriptionGl(invoicePdfSaveDTO.getItemDescription());
			invoiceLineItemData.setFinalTdsRate(invoicePdfSaveDTO.getRate());
			invoiceLineItemData.setFinalTdsSection(invoicePdfSaveDTO.getSection());
			// amount calculation
			invoiceLineItemData.setDerivedTdsAmount((invoiceLineItemData.getDerivedTdsRate()
					.multiply(invoiceLineItemData.getInvoiceAmount()).divide(BigDecimal.valueOf(100))));
			invoiceLineItemData.setFinalTdsAmount((invoiceLineItemData.getFinalTdsRate()
					.multiply(invoiceLineItemData.getInvoiceAmount()).divide(BigDecimal.valueOf(100))));
			invoiceLineItemData.setTdsAmount(invoiceLineItemData.getTdsRate()
					.multiply(invoiceLineItemData.getInvoiceAmount()).divide(BigDecimal.valueOf(100)));
			invoiceLineItemData.setClientAmount((invoiceLineItemData.getClientRate()
					.multiply(invoiceLineItemData.getInvoiceAmount()).divide(BigDecimal.valueOf(100))));
			invoiceLineItemData.setCreatedDate(new Date());
			invoiceLineItemData.setCreatedBy(userName);
			invoiceLineItemData.setModifiedBy(userName);
			invoiceLineItemData.setModifiedDate(new Date());
			invoiceLineItemData.setPan(invoicePdfSaveDTO.getPanNumber());
			invoiceLineItemData.setDeductorPan(deductorPan);
			invoiceLineItemData.setVendorInvoiceNumber(invoicePdfSaveDTO.getInvoiceNumber());
			invoiceLineItemData.setIsParent(false);
			invoiceLineItemData.setIsKeyDuplicate(false);
			invoiceLineItemData.setChallanPaid(false);
			invoiceLineItemData.setIsChallanGenerated(false);
			invoiceLineItemData.setHasProvision(false);
			invoiceLineItemData.setHasAdvance(false);
			invoiceLineItemData.setApprovedForChallan(false);
			invoiceLineItemData.setHasAo(false);
			invoiceLineItemData.setHasLdc(false);
			invoiceLineItemData.setDocumentType("INV");
			invoiceLineItemData.setConfidence(invoicePdfSaveDTO.getConfidence());
			// TODO need to get resident type dynamically.
			invoiceLineItemData.setIsResident("N");
			Calendar cal = Calendar.getInstance();
			cal.setTime(invoicePdfSaveDTO.getInvoiceDate());
			int month = cal.get(Calendar.MONTH);
			invoiceLineItemData.setAssessmentMonth(month + 1);
			invoiceLineItemData.setChallanMonth(invoicePdfSaveDTO.getChallanMonth());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			// formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date postingDate = formatter.parse(invoicePdfSaveDTO.getPostingDate());
			logger.info("Posting document date: {}", postingDate);
			invoiceLineItemData.setAssessmentYear(invoicePdfSaveDTO.getAssessmentYear());
			invoiceLineItemData.setDeductorMasterTan(tan);
			invoiceLineItemData.setDocumentPostingDate(postingDate);
			invoiceLineItemData = invoiceLineItemDAO.save(invoiceLineItemData);
			logger.info("BATCH ID: {}", invoiceLineItemData.getBatchUploadId());

			InvoiceMismatchesDTO invoiceMismatchesDTO = new InvoiceMismatchesDTO();
			invoiceMismatchesDTO.setAssessmentYear(invoiceLineItemData.getAssessmentYear());
			invoiceMismatchesDTO.setDeductorTan(invoiceLineItemData.getDeductorMasterTan());
			invoiceMismatchesDTO.setInvoiceLineItemId(invoiceLineItemData.getId());
			Date date = invoiceLineItemData.getDocumentPostingDate();
			String postingDocumentDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
			logger.info("Posting document date for invoice api: {}", postingDocumentDate);
			invoiceMismatchesDTO.setDocumentPostingDate(postingDocumentDate);
			mismatchesList.add(invoiceMismatchesDTO);

		}

		if (!invoicePdfSaveDTOs.isEmpty() && batchId != null) {
			// Updating batch upload status
			BatchUpload batchData = new BatchUpload();
			List<BatchUpload> batch = batchUploadDAO.getBatchUploadByTypeAndId(UploadTypes.INVOICE_PDF.name(), batchId);
			if (!batch.isEmpty()) {
				logger.info("RECORD PRESENT : {}", batch);
				batchData = batch.get(0);
				batchData.setStatus("Verified");
				batchUploadDAO.save(batchData);
			}
		}

		try {
			if (!mismatchesList.isEmpty()) {
				RestTemplate restTemplate = getRestTemplate();
				String url = applicationUrl + "/api/flask/invoice/tracking";
				URI uri = new URI(url);
				HttpHeaders headers = new HttpHeaders();
				headers.add("Authorization", token);
				headers.add("Content-Type", "application/json");
				headers.add("TAN-NUMBER", tan);
				headers.add("DEDUCTOR-PAN", deductorPan);
				HttpEntity<List<InvoiceMismatchesDTO>> request = new HttpEntity<>(mismatchesList, headers);

				restTemplate.postForEntity(uri, request, String.class);
			}
		} catch (Exception e) {
			logger.error("Exception occurred while requesting invoice tracking api", e);
		}
		// activity tracker update
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		List<ActivityTracker> tracker = activityTrackerDAO.getActivityTrackerByTanYearTypeAndMonth(tan, year,
				ActivityType.PDF_TRANSACTION.getActivityType(), month);
		if (!tracker.isEmpty()) {
			tracker.get(0).setStatus(ActivityTrackerStatus.VALIDATED.name());
			tracker.get(0).setModifiedBy(userName);
			tracker.get(0).setModifiedDate(new Date());
			activityTrackerDAO.update(tracker.get(0));
		}
		return invoicePdfSaveDTOs;
	}

	public CommonDTO<InvoiceLineItemResponseDTO> getInvoiceByType(String tan, String type, Pagination pagination,
			int year, int month) {
		logger.info("getInvoiceByType method execution started {}");
		int count = 0;

		CommonDTO<InvoiceLineItemResponseDTO> invoices = new CommonDTO<>();
		List<InvoiceLineItem> list = new ArrayList<>();
		List<InvoiceLineItemResponseDTO> listResponse = new ArrayList<>();
		list = invoiceLineItemDAO.getInvoiceByType(tan, type, year, month, pagination);
		count = list.size();
		logger.info("Retrieved data {}", list);

		for (InvoiceLineItem dto : list) {
			InvoiceLineItemResponseDTO response = new InvoiceLineItemResponseDTO();
			BeanUtils.copyProperties(dto, response);
			listResponse.add(response);
		}
		PagedData<InvoiceLineItemResponseDTO> pagedData = new PagedData<>(listResponse, list.size(),
				pagination.getPageNumber(),
				count > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		invoices.setCount(BigInteger.valueOf(count));
		invoices.setResultsSet(pagedData);
		return invoices;
	}

	public CommonDTO<InvoiceLineItemResponseDTO> getCRInvoices(String tan, String type, Pagination pagination, int year,
			int month) {
		int count = 0;

		CommonDTO<InvoiceLineItemResponseDTO> invoices = new CommonDTO<>();
		List<InvoiceLineItem> list = new ArrayList<>();
		List<InvoiceLineItemResponseDTO> listResponse = new ArrayList<>();
		list = invoiceLineItemDAO.getCRInvoices(tan, type, year, month, pagination);
		count = list.size();
		logger.info("Retrieved data {}", list);

		for (InvoiceLineItem dto : list) {
			InvoiceLineItemResponseDTO response = new InvoiceLineItemResponseDTO();
			BeanUtils.copyProperties(dto, response);
			listResponse.add(response);
		}
		PagedData<InvoiceLineItemResponseDTO> pagedData = new PagedData<>(listResponse, list.size(),
				pagination.getPageNumber(),
				count > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		invoices.setCount(BigInteger.valueOf(count));
		invoices.setResultsSet(pagedData);
		return invoices;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param tans
	 * @param firstDate
	 * @param lastDate
	 * @return
	 */
	public String getTdsCalculationForInvoice(Integer assessmentYear, String tans, String firstDate, String lastDate) {
		long trueValue = invoiceLineItemDAO.getTdsCalculationStatus(assessmentYear, tans, firstDate, lastDate, true);
		long falseValue = invoiceLineItemDAO.getTdsCalculationStatus(assessmentYear, tans, firstDate, lastDate, false);
		if (trueValue > 0 && falseValue > 0) {
			return ActivityTrackerStatus.PENDING.name();
		} else if (trueValue > 0 && falseValue == 0) {
			return ActivityTrackerStatus.VALIDATED.name();
		}
		return ActivityTrackerStatus.NORECORDS.name();
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param type
	 * @param tan
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public String getPdfActivityTrackerResult(Integer assessmentYear, String type, String tan, String startDate,
			String endDate) {
		long res = invoiceLineItemDAO.getPdfTransactionStatus(assessmentYear, type, tan, startDate, endDate);
		if (res == 0) {
			return ActivityTrackerStatus.NORECORDS.name();
		} else {
			return ActivityTrackerStatus.VALIDATED.name();
		}
	}

	/**
	 * 
	 * @param crData
	 * @return
	 */
	public CommonDTO<InvoiceLineItem> getInvoicesByCrData(CRDTO crData) {
		CommonDTO<InvoiceLineItem> invoiceData = null;
		List<InvoiceLineItem> crInvoiceList = invoiceLineItemDAO.findById(crData.getCr().getDeductorTan(),
				crData.getCr().getAssessmentYear(), crData.getCr().getId(), false);
		if (!crInvoiceList.isEmpty()) {
			InvoiceLineItem crInvoiceData = crInvoiceList.get(0);
			invoiceData = invoiceLineItemDAO.getInvoicesByCRData(crInvoiceData, crData.getPagination());
		}
		return invoiceData;
	}

	/**
	 * 
	 * @param crData
	 * @param token
	 * @param deductorPan
	 * @param tenantId
	 * @return
	 */
	public InvoiceLineItemCRDTO adjustments(CRDTO crData, String token, String deductorPan, String tenantId) {
		List<InvoiceLineItem> crInvoiceList = invoiceLineItemDAO.findByOnlyId(crData.getCr().getId());
		InvoiceLineItemCRDTO invoiceLineItemCRDTO = new InvoiceLineItemCRDTO();
		if (!crInvoiceList.isEmpty()) {
			InvoiceLineItem crInvoiceData = crInvoiceList.get(0);
			InvoiceKeyDTO invoiceCRDTO = new InvoiceKeyDTO();
			invoiceCRDTO.setAssessmentYear(crInvoiceData.getAssessmentYear());
			Date postingDate = crInvoiceData.getDocumentPostingDate();
			String postingDocumentDate = new SimpleDateFormat("MM/dd/yyyy").format(postingDate);
			invoiceCRDTO.setDocumentPostingDate(postingDocumentDate);
			invoiceCRDTO.setDeductorTan(crInvoiceData.getDeductorMasterTan());
			invoiceCRDTO.setInvoiceLineItemId(crInvoiceData.getId());
			List<InvoiceLineItem> invoiceList = invoiceLineItemDAO.getInvoicesByCRData(crInvoiceData);
			List<InvoiceKeyDTO> invoices = invoiceList.parallelStream().map(this::convertTOInvoiceKeyDTO)
					.collect(Collectors.toList());
			if (!invoices.isEmpty()) {
				invoiceLineItemCRDTO.setCr(invoiceCRDTO);
				invoiceLineItemCRDTO.setInvoice(invoices);
				adjustmentCr(invoiceLineItemCRDTO, token, crData.getCr().getDeductorTan(), deductorPan, tenantId);
			}
		}
		return invoiceLineItemCRDTO;
	}

	public void adjustmentCr(InvoiceLineItemCRDTO invoiceLineItemCRDTO, String token, String deductorTan,
			String deductorPan, String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		if (token != null) {
			try {
				String url = applicationUrl + "/api/flask/invoice/cr_tracking";
				URI uri = new URI(url);

				RestTemplate restTemplate = getRestTemplate();

				HttpHeaders headers = new HttpHeaders();
				headers.add("Authorization", token);
				headers.add("Content-Type", "application/json");
				headers.add("TAN-NUMBER", deductorTan);
				headers.add("DEDUCTOR-PAN", deductorPan);
				HttpEntity<InvoiceLineItemCRDTO> request = new HttpEntity<>(invoiceLineItemCRDTO, headers);

				ResponseEntity<InvoiceCrDtO> response = restTemplate.postForEntity(uri, request, InvoiceCrDtO.class);
				if (response.getBody().isAmount_less()) {
					logger.info("CR Amount is greater than the INV amount");
					throw new CustomException("CR Amount is greater than the INV amount",
							HttpStatus.INTERNAL_SERVER_ERROR);
				} else if (response.getBody().isReject_result()) {
					logger.info("Section not matching with original invoice");
					throw new CustomException("Section not matching with original invoice",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} catch (Exception e) {
				logger.error("Exception occurred while requesting cr tracking api", e);
				throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	public InvoiceLineItemCRDTO adjustmentsForCurrentMonth(InvoiceLineItemCRDTO invoicesAndCrData, String token,
			String deductorPan, String tenantId) {
		adjustmentCr(invoicesAndCrData, token, invoicesAndCrData.getCr().getDeductorTan(), deductorPan, tenantId);
		return invoicesAndCrData;
	}

	public InvoiceKeyDTO convertTOInvoiceKeyDTO(InvoiceLineItem invoice) {
		InvoiceKeyDTO invoiceKeyDTO = new InvoiceKeyDTO();
		invoiceKeyDTO.setAssessmentYear(invoice.getAssessmentYear());
		invoiceKeyDTO.setDeductorTan(invoice.getDeductorMasterTan());
		Date postingDate = invoice.getDocumentPostingDate();
		String postingDocumentDate = new SimpleDateFormat("MM/dd/yyyy").format(postingDate);
		invoiceKeyDTO.setDocumentPostingDate(postingDocumentDate);
		invoiceKeyDTO.setInvoiceLineItemId(invoice.getId());
		return invoiceKeyDTO;
	}

	public Integer updateInvoices(InvoiceDTO invoiceData) {
		return invoiceLineItemDAO.deleteById(invoiceData.getId());
	}

	public CommonDTO<InvoiceLineItem> getInvoicesByCurrentMonth(int assessmentYear, int month, String deducteeName,
			String tan, Pagination pagination, String section) {
		CommonDTO<InvoiceLineItem> invoices = new CommonDTO<>();

		logger.info("tan : {} year : {} month : {} deductee name : {}", tan, assessmentYear, month, deducteeName);

		List<InvoiceLineItem> invoiceList = invoiceLineItemDAO.getInvoicesByCurrentMonth(assessmentYear, month, tan,
				deducteeName, pagination, section);
		BigInteger invoicesCount = invoiceLineItemDAO.getInvoicesCountByCurrentMonth(assessmentYear, month, tan,
				deducteeName, section);
		logger.info("Total Invoices Count : {}", invoicesCount);
		PagedData<InvoiceLineItem> pagedData = new PagedData<>(invoiceList, invoiceList.size(),
				pagination.getPageNumber(),
				invoicesCount.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		invoices.setResultsSet(pagedData);
		invoices.setCount(invoicesCount);
		return invoices;
	}

	public List<InvoiceLineItemResponseDTO> copyToResponse(List<InvoiceLineItem> list) {
		List<InvoiceLineItemResponseDTO> listResponse = new ArrayList<InvoiceLineItemResponseDTO>();
		if (!list.isEmpty()) {
			for (InvoiceLineItem dto : list) {
				InvoiceLineItemResponseDTO response = new InvoiceLineItemResponseDTO();
				response.setAssessmentYear(dto.getAssessmentYear());
				response.setDeductorTan(dto.getDeductorMasterTan());
				response.setDocumentPostingDate(dto.getDocumentPostingDate());
				response.setId(dto.getId());
				response.setAccountingVoucherDate(dto.getAccountingVoucherDate());
				response.setAccountingVoucherNumber(dto.getAccountingVoucherNumber());
				response.setActionType(dto.getActionType());
				response.setActive(dto.getActive());
				response.setAdvanceAppliedOn(dto.getAdvanceAppliedOn());
				response.setAmountPaidCredited(dto.getAmountPaidCredited());
				response.setApprovedForChallan(dto.getApprovedForChallan());
				response.setAssessmentMonth(dto.getAssessmentMonth());
				response.setBatchUploadId(dto.getBatchUploadId());
				response.setCessAmount(dto.getCessAmount());
				response.setCessRate(dto.getCessRate());
				response.setCgstAmount(dto.getCgstAmount());
				response.setCgstRate(dto.getCessRate());
				response.setChallanGeneratedDate(dto.getChallanGeneratedDate());
				response.setChallanMonth(dto.getChallanMonth());
				response.setChallanPaid(dto.getChallanPaid());
				response.setChallanPaidDate(dto.getChallanPaidDate());
				response.setChallanSerialNo(dto.getChallanSerialNo());
				response.setChallanSplit(dto.getChallanSplit());
				response.setClientAmount(dto.getClientAmount());
				response.setClientRate(dto.getClientRate());
				response.setClientSection(dto.getClientSection());
				response.setCompanyCode(dto.getCompanyCode());
				response.setCompanyName(dto.getCompanyName());
				response.setConfidence(dto.getConfidence());
				response.setCreatedBy(dto.getCreatedBy());
				response.setCreatedDate(dto.getCreatedDate());
				response.setCreditable(dto.getCreditable());
				response.setCreditDebitNote(dto.getCreditDebitNote());
				response.setDateAtWhichTdsIsDeposited(dto.getDateAtWhichTdsIsDeposited());
				response.setDateOnWhichTdsIsDeducted(dto.getDateOnWhichTdsIsDeducted());
				response.setDeducteeAddress(dto.getDeducteeAddress());
				response.setDeducteeCode(dto.getDeducteeCode());
				response.setDeducteeName(dto.getDeducteeName());
				response.setDeducteeTin(dto.getDeducteeTin());
				response.setDeductorCode(dto.getDeductorCode());
				response.setDeductorGstin(dto.getDeductorGstin());
				response.setDeductorPan(dto.getDeductorPan());
				response.setDerivedTdsAmount(dto.getDerivedTdsAmount());
				response.setDerivedTdsRate(dto.getDerivedTdsRate());
				response.setDerivedTdsSection(dto.getDerivedTdsSection());
				response.setDocumentDate(dto.getDocumentDate());
				response.setDocumentNumber(dto.getDocumentNumber());
				response.setDocumentType(dto.getDocumentType());
				response.setFinalReason(dto.getFinalReason());
				response.setFinalTdsAmount(dto.getFinalTdsAmount());
				response.setFinalTdsRate(dto.getFinalTdsRate());
				response.setFinalTdsSection(dto.getFinalTdsSection());
				response.setIgstAmount(dto.getIgstAmount());
				response.setIgstRate(dto.getIgstRate());
				response.setInterest(dto.getInterest());
				response.setInvoiceAmount(dto.getInvoiceAmount());
				response.setInvoiceFound(dto.getInvoiceFound());
				response.setInvoiceType(dto.getInvoiceType());
				response.setIsAdvanceSplitRecord(dto.getAdvanceIsSplitRecord());
				response.setIsAmendment(dto.getIsAmendment());
				response.setIsChallanGenerated(dto.getIsChallanGenerated());
				response.setIsKeyDuplicate(dto.getIsKeyDuplicate());
				response.setIsLdcSplitRecord(dto.getIsLdcSplitRecord());
				response.setIsMergeRecord(dto.getIsMergeRecord());
				response.setIsMismatch(dto.getIsMismatch());
				response.setIsParent(dto.getIsParent());
				response.setIsProvisionSplitRecord(dto.getIsProvisionSplitRecord());
				response.setIsRejected(dto.getIsRejected());
				response.setIsResident(dto.getIsResident());
				response.setIsSplitRecord(dto.getIsSplitRecord());
				response.setGlAccountCode(dto.getGlAccountCode());
				response.setGlFound(dto.getGlFound());
				response.setGrossIndicator(dto.getGrossIndicator());
				response.setGstin(dto.getGstin());
				response.setHasAdvance(dto.getHasAdvance());
				response.setHasAo(dto.getHasAo());
				response.setHasLdc(dto.getHasLdc());
				response.setHasProvision(dto.getHasProvision());
				response.setLdcAppliedOn(dto.getLdcAppliedOn());
				response.setLineItemNumber(dto.getLineItemNumber());
				response.setLinkedAdvanceNumber(dto.getLinkedAdvanceNumber());
				response.setMigoNumber(dto.getMigoNumber());
				response.setMiroNumber(dto.getMiroNumber());
				// TODO value needs to be set
				// response.setMismatch(dto.getmi);
				response.setMismatchCategory(dto.getMismatchCategory());
				response.setMismatchInterpretation(dto.getMismatchInterpretation());
				response.setMismatchModifiedDate(dto.getMismatchModifiedDate());
				response.setOriginalDocumentDate(dto.getOriginalDocumentDate());
				response.setOriginalDocumentNumber(dto.getOriginalDocumentNumber());
				response.setPan(dto.getPan());
				response.setParentId(dto.getParentId());
				response.setPenalty(dto.getPenalty());
				response.setPoDate(dto.getPoDate());
				response.setPoNumber(dto.getPoNumber());
				response.setPos(dto.getPos());
				response.setProcessedFrom(dto.getProcessedFrom());
				response.setProvisionAppliedOn(dto.getProvisionAppliedOn());
				response.setQtySupplied(dto.getQtySupplied());
				response.setReceiptBsrCode(dto.getReceiptBsrCode());
				response.setSacDecription(dto.getSacDecription());
				response.setSection(dto.getSection());
				response.setSequenceNumber(dto.getSequenceNumber());
				response.setServiceDescriptionGl(dto.getServiceDescriptionGl());
				response.setServiceDescriptionInvoice(dto.getServiceDescriptionInvoice());
				response.setServiceDescriptionPo(dto.getServiceDescriptionPo());
				response.setSgstAmount(dto.getSgstAmount());
				response.setSgstRate(dto.getSgstRate());
				response.setSourceFileName(dto.getSourceFileName());
				response.setSourceIdentifier(dto.getSourceIdentifier());
				response.setSurcharge(dto.getSurcharge());
				response.setSystemChallanSerialNumber(dto.getSystemChallanSerialNumber());
				response.setTdsAmount(dto.getTdsAmount());
				response.setTdsDeducted(dto.getTdsDeducted());
				response.setTdsDeducted(dto.getTdsDeducted());
				response.setTdsRate(dto.getTdsRate());
				response.setTdsSection(dto.getTdsSection());
				response.setThresholdLimit(dto.getThresholdLimit());
				response.setUnitOfMeasurement(dto.getUnitOfMeasurement());
				response.setUserDefinedField1(dto.getUserDefinedField1());
				response.setUserDefinedField2(dto.getUserDefinedField2());
				response.setUserDefinedField3(dto.getUserDefinedField3());
				response.setVendorInvoiceNumber(dto.getVendorInvoiceNumber());
				listResponse.add(response);
			}
		}

		return listResponse;
	}

	public Set<String> getDeductees(String deductorTan, String deducteeType, String type, int year, int month,
			boolean isMismatch) {
		if ("resident".equalsIgnoreCase(deducteeType)) {
			deducteeType = "N";
		} else {
			deducteeType = "Y";
		}
		if ("invoice".equalsIgnoreCase(type)) {
			return invoiceLineItemDAO.getInvoiceDeductees(deductorTan, deducteeType, year, month, isMismatch)
					.parallelStream().map(InvoiceLineItem::getDeducteeName).distinct().collect(Collectors.toSet());
		} else if ("advance".equalsIgnoreCase(type)) {
			return advanceDAO.getAdvanceDeductees(deductorTan, deducteeType, year, month, isMismatch).parallelStream()
					.map(AdvanceDTO::getDeducteeName).distinct().collect(Collectors.toSet());
		} else {
			return null; // NEED TO CHANGE FOR SQL
			// provisionDAO.getProvisionDeductees(deductorTan, deducteeType, year, month,
			// isMismatch)
			// .parallelStream().map(Provision::getDeducteeName).distinct().collect(Collectors.toSet());
		}
	}

	/**
	 * 
	 * @param multiPartFile
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param token
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	@Transactional
	public BatchUpload saveInvoiceData(MultipartFile multiPartFile, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonthPlusOne, String userName, String tenantId, String deductorPan, String token,
			String type) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		type = getNRUploadType(type);
		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);
		if (isAlreadyProcessed(sha256)) {
			BatchUpload batchUpload = new BatchUpload();
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setSha256sum(sha256);
			batchUpload.setStatus("Duplicate");
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setProcessEndTime(new Date());
			batchUpload.setUploadType(type);
			batchUpload = invoiceBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
					assessmentMonthPlusOne, userName, null, tenantId);
			return batchUpload;
		}
		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = Excel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count {}:", headersCount);
			BatchUpload batchUpload = new BatchUpload();
			ArrayList<String> fileHeaders = new ArrayList<>();
			for (org.apache.poi.ss.usermodel.Cell cell : headerRow) {
				fileHeaders.add(cell.getStringCellValue());
			}
			// Headers validation
			ArrayList<String> differenceHeaders = getNRdifferenceHeaders(fileHeaders, batchUpload.getUploadType(),
					headersCount);
			logger.info("difference headers  :{}", differenceHeaders);
			if (!differenceHeaders.isEmpty()) {
				return batchUploadData(batchUpload, multiPartFile, deductorTan, assesssmentYear, assessmentMonthPlusOne,
						userName, tenantId, sha256);
			} else {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setStatus("Processing");
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setUploadType(type);
				batchUpload = invoiceBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonthPlusOne, userName, null, tenantId);
			}
			// Converting excel file to csv
			File file = new File(multiPartFile.getOriginalFilename());
			OutputStream os = new FileOutputStream(file);
			os.write(multiPartFile.getBytes());
			Workbook csvWorkBook = new Workbook(file.getAbsolutePath());
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			csvWorkBook.save(baout, SaveFormat.CSV);
			File csvFile = new File(FilenameUtils.removeExtension(file.getName()) + ".csv");

			FileUtils.writeByteArrayToFile(csvFile, baout.toByteArray());
			String csvPath = blob.uploadExcelToBlobWithFile(csvFile, tenantId);
			batchUpload.setOtherFileUrl(csvPath);
			return invoiceNonResidentService.asyncProcessLineItems(tenantId, deductorPan, batchUpload, token);
		} catch (Exception e) {
			logger.error("Exception occurred while uploading invoice ", e);
			return batchUploadData(new BatchUpload(), multiPartFile, deductorTan, assesssmentYear,
					assessmentMonthPlusOne, userName, tenantId, sha256);
		}
	}
	
	//NR transaction header validations
	private ArrayList<String> getNRdifferenceHeaders(ArrayList<String> fileHeaders, String uploadType,
			int headersCount) {
		ArrayList<String> differenceHeaders = new ArrayList<>();
		if (UploadTypes.INVOICE_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			ArrayList<String> nrInvoiceHeaders = (ArrayList<String>) errorReportService.getNRInvoiceHeaderFields();
			if (headersCount == nrInvoiceHeaders.size()) {
				fileHeaders.stream().forEach(header -> {
					if (!nrInvoiceHeaders.contains(header)) {
						differenceHeaders.add(header);
					}
				});
			}
		} else if (UploadTypes.ADVANCE_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			ArrayList<String> nrAdvanceHeaders = (ArrayList<String>) errorReportService.getNRAdvanceHeaderFields();
			if (headersCount == nrAdvanceHeaders.size()) {
				fileHeaders.stream().forEach(header -> {
					if (!nrAdvanceHeaders.contains(header)) {
						differenceHeaders.add(header);
					}
				});
			}
		} else if (UploadTypes.PROVISION_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			ArrayList<String> nrProvisionHeaders = (ArrayList<String>) errorReportService.getNRProvisionHeaderFields();
			if (headersCount == nrProvisionHeaders.size()) {
				fileHeaders.stream().forEach(header -> {
					if (!nrProvisionHeaders.contains(header)) {
						differenceHeaders.add(header);
					}
				});
			}
		}
		return differenceHeaders;
	}

	private String getNRUploadType(String type) {
		if ("INVOICE_EXCEL".equalsIgnoreCase(type)) {
			type = UploadTypes.INVOICE_NR_EXCEL.name();
		} else if ("ADVANCES".equalsIgnoreCase(type)) {
			type = UploadTypes.ADVANCE_NR_EXCEL.name();
		} else if ("PROVISIONS".equalsIgnoreCase(type)) {
			type = UploadTypes.PROVISION_NR_EXCEL.name();
		}
		return type;
	}

	/**
	 * 
	 * @param sha256Sum
	 * @return
	 */
	private boolean isAlreadyProcessed(String sha256Sum) {
		List<BatchUpload> sha256Record = batchUploadDAO.getSha256Records(sha256Sum);
		return sha256Record != null && !sha256Record.isEmpty();
	}

	/**
	 * 
	 * @param batchUpload
	 * @param mFile
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param file
	 * @param tenant
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public BatchUpload invoiceBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonth, String userName, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("batch", batchUpload);
		if (file != null) {
			String errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
			batchUpload.setErrorFilePath(errorFp);
		}
		if (mFile != null) {
			String path = blob.uploadExcelToBlob(mFile);
			batchUpload.setFileName(mFile.getOriginalFilename());
			batchUpload.setFilePath(path);
		}
		batchUpload.setAssessmentMonth(assessmentMonth);
		batchUpload.setAssessmentYear(assesssmentYear);
		batchUpload.setDeductorMasterTan(tan);
		batchUpload.setCreatedBy(userName);
		batchUpload.setCreatedDate(new Date());
		batchUpload.setActive(true);
		if (batchUpload.getBatchUploadID() != null) {
			batchUpload.setModifiedBy(userName);
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param batchUpload
	 * @param multiPartFile
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param tenantId
	 * @param sha256
	 * @return
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public BatchUpload batchUploadData(BatchUpload batchUpload, MultipartFile multiPartFile, String deductorTan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, String tenantId, String sha256)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException {
		batchUpload.setCreatedDate(new Date());
		batchUpload.setProcessStartTime(new Date());
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessedCount(0);
		batchUpload.setMismatchCount(0L);
		batchUpload.setSha256sum(sha256);
		batchUpload.setStatus("Failed");
		batchUpload.setCreatedBy(userName);
		batchUpload.setProcessEndTime(new Date());
		return invoiceBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear, assessmentMonthPlusOne,
				userName, null, tenantId);
	}

	@Async
	public void asyncExportInterestComputationReport(String tan, Integer year, String userName, String deductorPan,
			Integer month, String tenantId)
			throws InvalidKeyException, FileNotFoundException, IOException, URISyntaxException, StorageException {
		logger.info("Entered into async method for generating interest computation report {}");
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
				UploadTypes.INVOICE_INTEREST_COMPUTATION_REPORT.name(), "Processing", month, userName, null);

		try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {

			SXSSFSheet sheet = workbook.createSheet("Interese_Computation_Report");
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);
			sheet.protectSheet("password");
			sheet.setDisplayGridlines(false);
			sheet.setDefaultColumnWidth(20);
			sheet.trackAllColumnsForAutoSizing();
			sheet.setDefaultRowHeightInPoints(25);
			sheet.setColumnHidden(24, true);

			sheet.setAutoFilter(new CellRangeAddress(3, 3, 0, 23));

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

			SXSSFRow row1 = sheet.createRow(3);
			row1.createCell(0).setCellValue("Sequence Number");
			row1.getCell(0).setCellStyle(style1);
			row1.createCell(1).setCellValue("Client TDS Amount");
			row1.getCell(1).setCellStyle(style1);
			row1.createCell(2).setCellValue("Client TDS Section");
			row1.getCell(2).setCellStyle(style1);
			row1.createCell(3).setCellValue("Client TDS Rate");
			row1.getCell(3).setCellStyle(style1);
			row1.createCell(4).setCellValue("Derived TDS Amount");
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
			row1.createCell(18).setCellValue("Confidence");
			row1.getCell(18).setCellStyle(style1);
			row1.createCell(19).setCellValue("ERP Document Number");
			row1.getCell(19).setCellStyle(style1);
			row1.createCell(20).setCellValue("Interest");
			row1.getCell(20).setCellStyle(style1);

			row1.createCell(21).setCellValue("Action");
			row1.getCell(21).setCellStyle(styleGreen);

			row1.createCell(22).setCellValue("Reason");
			row1.getCell(22).setCellStyle(styleGreen);

			row1.createCell(23).setCellValue("Final Interest Amount");
			row1.getCell(23).setCellStyle(styleGreen);
			row1.createCell(24).setCellValue("Invoice Id");
			row1.getCell(24).setCellStyle(style1);

			List<InvoiceLineItem> list = invoiceLineItemDAO.getInvoicesWithInterestComputed(tan, year, month);
			long size = list.size();

			validationHelper = sheet.getDataValidationHelper();
			CellRangeAddressList addressList = new CellRangeAddressList(4, (int) size + 4, 21, 21);
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
			for (InvoiceLineItem listData : list) {
				index++;
				SXSSFRow row2 = sheet.createRow(index);
				row2.createCell(0).setCellValue(listData.getSequenceNumber());
				row2.getCell(0).setCellStyle(style2);
				row2.createCell(1).setCellValue(listData.getClientAmount().toString());
				row2.getCell(1).setCellStyle(style2);
				row2.createCell(2).setCellValue(listData.getTdsSection());
				row2.getCell(2).setCellStyle(style2);
				row2.createCell(3).setCellValue(listData.getTdsRate().toString());
				row2.getCell(3).setCellStyle(style2);
				row2.createCell(4).setCellValue(listData.getDerivedTdsAmount().toString());
				row2.getCell(4).setCellStyle(style2);
				row2.createCell(5).setCellValue(listData.getDerivedTdsSection());
				row2.getCell(5).setCellStyle(style2);
				row2.createCell(6).setCellValue(
						listData.getDerivedTdsRate() == null ? "0.00" : listData.getDerivedTdsRate().toString());
				row2.getCell(6).setCellStyle(style2);
				row2.createCell(7).setCellValue(
						listData.getInvoiceAmount() == null ? " " : listData.getInvoiceAmount().toString());
				row2.getCell(7).setCellStyle(style2);
				row2.createCell(8).setCellValue(listData.getTdsSection());
				row2.getCell(8).setCellStyle(style2);
				row2.createCell(9).setCellValue(listData.getTdsRate().toString());
				row2.getCell(9).setCellStyle(style2);
				row2.createCell(10).setCellValue(StringUtils.EMPTY);
				row2.getCell(10).setCellStyle(style2);
				row2.createCell(11).setCellValue(listData.getDeductorMasterTan());
				row2.getCell(11).setCellStyle(style2);
				row2.createCell(12).setCellValue(listData.getPan());
				row2.getCell(12).setCellStyle(style2);
				row2.createCell(13).setCellValue(listData.getDeducteeName());
				row2.getCell(13).setCellStyle(style2);

				row2.createCell(14).setCellValue(listData.getServiceDescriptionInvoice());
				row2.getCell(14).setCellStyle(style2);
				row2.createCell(15).setCellValue(listData.getServiceDescriptionPo());
				row2.getCell(15).setCellStyle(style2);
				row2.createCell(16).setCellValue(listData.getServiceDescriptionGl());
				row2.getCell(16).setCellStyle(style2);
				row2.createCell(17).setCellValue(StringUtils.EMPTY);
				row2.getCell(17).setCellStyle(style2);
				row2.createCell(18).setCellValue(listData.getConfidence());
				row2.getCell(18).setCellStyle(style2);
				row2.createCell(19).setCellValue(listData.getDocumentNumber());
				row2.getCell(19).setCellStyle(style2);
				// unlocked data
				row2.createCell(20).setCellValue(listData.getInterest() == null ? "0.00"
						: listData.getInterest().setScale(2, BigDecimal.ROUND_HALF_DOWN).toString());
				row2.getCell(20).setCellStyle(style2);
				row2.createCell(21).setCellValue("");// action
				row2.getCell(21).setCellStyle(style3);
				row2.createCell(22).setCellValue("");// reasonn
				row2.getCell(22).setCellStyle(style3);
				row2.createCell(23).setCellValue("");// final interest amount
				row2.getCell(23).setCellStyle(style3);
				row2.createCell(24).setCellValue(listData.getId());// invoice id
				row2.getCell(24).setCellStyle(style3);

			}

			workbook.write(out);
			saveInterestComputationReport(tan, tenantId, year, out, Long.valueOf(size),
					UploadTypes.INVOICE_INTEREST_COMPUTATION_REPORT.name(), "Processed", month, userName,
					batchUpload.getBatchUploadID());

		} catch (Exception e) {
			logger.info("Exception occured while generating the Itereset Computation Report {}" + e.getMessage());
			throw new RuntimeCryptoException();
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
		return "Interest Computation Report (Dated: " + date + ")\n Client Name: " + deductorData.getDeductorName()
				+ "\n";
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
		batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));

		// do not update count with 0 for async reports
		// batchUpload.setProcessedCount(noOfRows);
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

	/**
	 * async call to process the interest report
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

	@Async
	public void asyncProcessInvoiceComputationFile(String tenantId, String userName, String deductorTan,
			String deductorPan, Integer year, Integer month, MultipartFile file, String uploadType,
			BatchUpload batchUpload) throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Async method executing to process the interest report {}");
		processInvoiceComputationFile(tenantId, userName, deductorTan, deductorPan, year, month, uploadType,
				batchUpload);
		;
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
	public void processInvoiceComputationFile(String tenantId, String userName, String deductorTan, String deductorPan,
			Integer year, Integer month, String uploadType, BatchUpload batchUpload)
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

				String userAction = formatter.formatCellValue(row.getCell(21));
				String finalReason = formatter.formatCellValue(row.getCell(22));
				Integer invoiceLineItemId = null;
				if (StringUtils.isNotBlank(formatter.formatCellValue(row.getCell(24)))) {
					invoiceLineItemId = Integer.parseInt(formatter.formatCellValue(row.getCell(24)));

				}

				if (StringUtils.isNotBlank(userAction) && invoiceLineItemId != null) {
					InvoiceLineItem invoice = null;
					List<InvoiceLineItem> list = invoiceLineItemDAO.getInvoicesWithInterestById(invoiceLineItemId);
					if (!list.isEmpty()) {
						invoice = list.get(0);
						BigDecimal finalAmount = BigDecimal.ZERO;
						if (userAction.equalsIgnoreCase("Accept")) {
							// "Accept", "Modify", "Reject"
							finalAmount = new BigDecimal(formatter.formatCellValue(row.getCell(20)).toString());

						} else if (userAction.equalsIgnoreCase("Modify")) {
							finalAmount = new BigDecimal(formatter.formatCellValue(row.getCell(23)).toString());
						} else if (userAction.equalsIgnoreCase("Reject")) {
							finalAmount = BigDecimal.ZERO;
						}
						invoice.setFinalTdsAmount(finalAmount);
						invoice.setModifiedBy(userName);
						invoice.setModifiedDate(new Timestamp(new Date().getTime()));
						invoice.setActionType(userAction);
						invoice.setFinalReason(finalReason);
						invoiceLineItemDAO.updateInvoiceInterest(invoice);
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
			logger.info("Updated Batch upload successfully for invoice interest report upload{}");
		}

	}

	public CommonDTO<InvoiceLineItem> getInvoiceInterestRecords(String tan, int year, int challanMonth,
			MismatchesFiltersDTO filters) {
		CommonDTO<InvoiceLineItem> invoiceLineItemData = new CommonDTO<>();

		List<InvoiceLineItem> invoiceList = invoiceLineItemDAO.getInvoicesWithInterestComputedWithPagination(tan, year,
				challanMonth, filters.getDeducteeName(), filters.getResidentType(), filters.getPagination());
		BigInteger count = invoiceLineItemDAO.getCountOfInvoicesWithInterestComputed(tan, year, challanMonth,
				filters.getDeducteeName(), filters.getResidentType());

		PagedData<InvoiceLineItem> pagedData = new PagedData<InvoiceLineItem>(invoiceList, invoiceList.size(),
				filters.getPagination().getPageNumber(),
				count.intValue() > (filters.getPagination().getPageSize() * filters.getPagination().getPageNumber())
						? false
						: true);
		invoiceLineItemData.setResultsSet(pagedData);
		invoiceLineItemData.setCount(count);
		return invoiceLineItemData;

	}

	/**
	 * to perform onscreen accept reject and modify for interest records
	 * 
	 * @param tan
	 * @param invoiceMismatchUpdateDTO
	 * @param pan
	 */
	public void updateInterestrecords(String tan, UpdateOnScreenDTO updateOnScreenDTO, String pan) {

		for (UpdateOnScreenDTO invoiceInterest : updateOnScreenDTO.getData()) {
			if (invoiceInterest.getId() != null) {
				InvoiceLineItem invoice = null;
				logger.info("Fetching invoice interest records {}");
				List<InvoiceLineItem> list = invoiceLineItemDAO.getInvoicesWithInterestById(invoiceInterest.getId());

				if (!list.isEmpty()) {
					invoice = list.get(0);
					logger.info("Retrieved  invoice interest record {}" + invoice);
					String userAction = updateOnScreenDTO.getActionType();
					BigDecimal finalTdsAmount = invoice.getInterest();

					if (userAction.equalsIgnoreCase("modify")) {
						finalTdsAmount = invoiceInterest.getFinalAmount();
					} else if (userAction.equalsIgnoreCase("Reject")) {
						finalTdsAmount = BigDecimal.ZERO;
					}
					invoice.setFinalTdsAmount(finalTdsAmount);
					invoice.setFinalReason(updateOnScreenDTO.getReason());
					invoice.setActionType(updateOnScreenDTO.getActionType());
					invoiceLineItemDAO.updateInvoiceInterest(invoice);
					logger.info("  invoice interest record updated successfully with final amount as {}"
							+ invoice.getFinalTdsAmount());
				}

			}
		}
		// return invoiceMismatchUpdateDTO;
	}

	@Transactional
	public BatchUpload generateNrStaggingFile(String tan, String pan, Integer year, Integer month, String tenantId,
			String type) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
		batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
		batchUpload.setStatus("Processing");
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessedCount(0);
		batchUpload.setMismatchCount(0L);
		batchUpload.setDeductorMasterTan(tan);
		batchUpload.setActive(true);
		batchUpload.setAssessmentYear(year);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setUploadType("NR_STAGGING");
		batchUpload.setFileName(type + "-" + year + "-" + month + "-" + new Date());
		batchUpload = batchUploadDAO.save(batchUpload);
		if (StringUtils.isNotBlank(type) && "advance".equalsIgnoreCase(type)) {
			invoiceNonResidentService.generateAdvanceStaggingFile(batchUpload, pan, tenantId);
		} else {
			invoiceNonResidentService.generateInvoiceStaggingFile(batchUpload, pan, tenantId);
		}

		return batchUpload;
	}
	
	@Async
	public void retriggerdNrTransactions(Integer batchId, String deductorTan, String token, String tenantId,
			String deductorPan) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		List<BatchUpload> batchList = batchUploadDAO.getBatchListBasedOnTanAndGroupId(deductorTan, batchId);
		if (!batchList.isEmpty()) {
			BatchUpload batchUpload = batchList.get(0);
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setProcessEndTime(null);
			batchUpload.setStatus("Processing");
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload = batchUploadDAO.update(batchUpload);
			invoiceNonResidentService.processLineItems(tenantId, deductorPan, batchUpload, token);
		}
	}

}
