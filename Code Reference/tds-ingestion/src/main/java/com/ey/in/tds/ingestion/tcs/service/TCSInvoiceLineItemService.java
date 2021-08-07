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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.CollecteeExempt;
import com.ey.in.tds.common.domain.Currency;
import com.ey.in.tds.common.domain.ErrorCode;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.tcs.TCSInvoiceLineItem;
import com.ey.in.tds.common.domain.tcs.TCSNatureOfIncome;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSLccUtilizationDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsPaymentDTO;
import com.ey.in.tds.common.dto.CRDTO;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.CustomSectionRateDTO;
import com.ey.in.tds.common.dto.InvoiceLineItemCRDTO;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.csvfile.CsvFileDTO;
import com.ey.in.tds.common.dto.invoice.InvoiceMismatchByBatchIdDTO;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.ingestion.response.dto.InvoiceLineItemResponseDTO;
import com.ey.in.tds.common.model.invoicelineitem.InvoiceErrorReportCsvDTO;
import com.ey.in.tds.common.model.invoicelineitem.InvoicePdfCsvDTO;
import com.ey.in.tds.common.model.invoicelineitem.InvoicePdfList;
import com.ey.in.tds.common.model.invoicelineitem.NORemittanceDTO;
import com.ey.in.tds.common.model.invoicelineitem.TcsInvoiceErrorReportCsvDTO;
import com.ey.in.tds.common.model.job.TCSNoteBookParam;
import com.ey.in.tds.common.model.job.TcsJob;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccUtilization;
import com.ey.in.tds.common.repository.CurrencyRepository;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.service.ErrorReportService;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.ActivityTrackerStatus;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.config.SparkNotebooks;
import com.ey.in.tds.ingestion.dto.invoice.InvoiceCrDtO;
import com.ey.in.tds.ingestion.dto.invoice.InvoiceMismatchesDTO;
import com.ey.in.tds.ingestion.dto.invoice.UpdateOnScreenDTO;
import com.ey.in.tds.ingestion.service.tdsmismatch.TcsMismatchService;
import com.ey.in.tds.ingestion.tcs.dao.PaymentDAO;
import com.ey.in.tds.ingestion.tcs.dao.TCSInvoiceLineItemDAO;
import com.ey.in.tds.ingestion.tcs.dto.TCSInvoiceMismatchesDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

/**
 * 
 * @author scriptbees
 *
 */
@Service
public class TCSInvoiceLineItemService extends TcsMismatchService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BlobStorage blob;

	@Value("${databricks.key}")
	private String dataBricksKey;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private TCSBatchUploadDAO tcsBatchUploadDAO;

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	private ErrorReportService errorReportService;

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private TCSInvoiceLineItemDAO tcsInvoiceLineItemDAO;

	@Autowired
	private PaymentDAO tcsPaymentDAO;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private TCSLccUtilizationDAO tcsLccUtilizationDAO;

	@Autowired
	private SparkNotebooks sparkNotebooks;

	@Value("${application.url}")
	private String applicationUrl;

	@Value("${page_size}")
	protected int pageSize;

	Map<String, String> excelHeaderMap = new HashMap<String, String>() {
		private static final long serialVersionUID = -6921991234774788483L;
		{
			put("Source Identifier", "D");
			put("Source File Name", "E");
			put("Deductor PAN", "F");
			put("Collector TAN", "G");
			put("Collector GSTIN", "H");
			put("Non-Resident Deductee Indicator", "I");
			put("Collectee PAN", "J");
			put("Collectee TIN", "K");
			put("Collectee GSTIN", "L");
			put("Name of the Collectee", "M");
			put("Collectee Address", "N");
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
			put("Collectee Code", "AR");
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
			put("Supply Type", "BJ");
		}
	};

	public static String[] invoiceOtherErrorReportheaderNames = new String[] { "Source File Name", "Company Code",
			"Name Of The Company Code", "Collector PAN", "Collector TAN", "Collector GSTIN", "Collectee Code",
			"Non-Resident Collectee Indicator", "Collectee PAN", "Collectee TIN", "Collectee GSTIN",
			"Name of the Collectee", "Collectee Address", "Vendor Invoice Number", "Erp Document Number", "Miro Number",
			"Migo Number", "Document Type", "Erp Document Number", "Document Date", "Posting Date of Document",
			"Line Item Number", "HSN/SAC", "Sac Description", "Service Description - Invoice",
			"Service Description - PO", "Service Description - GL Text", "Taxable value", "IGST Rate", "IGST Amount",
			"CGST Rate", "CGST Amount", "SGST Rate", "SGST Amount", "Cess Rate", "Cess Amount", "Creditable (Y/N)",
			"Section Code", "POS", "TDS Section", "TDS Rate", "TDS Amount", "PO number", "PO date",
			"Linked advance Number", "Grossing up Indicator", "Original Document Number", "Original Document Date",
			"User Defined Field 1", "User Defined Field 2", "User Defined Field 3", "Section Predection" };

	/**
	 * 
	 * @param invoiceLineItem
	 * @return
	 */
	public TCSInvoiceLineItem create(TCSInvoiceLineItem invoiceLineItem) {
		invoiceLineItem.setActive(true);
		return tcsInvoiceLineItemDAO.save(invoiceLineItem);
	}

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
		listMisMatchBybatchDTO.add(groupMismatchesByType(tan, "SMM-RM", batchId, 0, 0));
		listMisMatchBybatchDTO.add(groupMismatchesByType(tan, "SMM-RMM", batchId, 0, 0));
		listMisMatchBybatchDTO.add(groupMismatchesByType(tan, "NAD", batchId, 0, 0));
		return listMisMatchBybatchDTO;
	}

	// tds mismatches all
	public List<InvoiceMismatchByBatchIdDTO> getInvoiceMismatchAll(String tan, int year, int month) {
		logger.info("REST request of Tan to get List of InvoiceMismatches : {}", tan);
		List<InvoiceMismatchByBatchIdDTO> listMisMatchAllDTO = new ArrayList<>();
		listMisMatchAllDTO.add(groupMismatchesByType(tan, "SM-RMM", null, year, month));
		listMisMatchAllDTO.add(groupMismatchesByType(tan, "SMM-RM", null, year, month));
		listMisMatchAllDTO.add(groupMismatchesByType(tan, "SMM-RMM", null, year, month));
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
		return tcsInvoiceLineItemDAO.getInvoiceMismatchSummary(year, month, tan, batchId, type);
	}

	/**
	 * Get List of Invoice Mis Matches based on Batch Upload Id, Mis matches
	 * 
	 * @param batchId
	 * @param mismatchCategory
	 * @return
	 */
	public CommonDTO<TCSInvoiceLineItem> getInvoiceMismatchByBatchUploadIDMismatchCategory(Integer batchId,
			String mismatchCategory, String collectorTan, Pagination pagination) {
		logger.info("REST request of batchId ,mismatchCategory and tan : {} , {} , {} ", batchId, mismatchCategory,
				collectorTan);
		BigInteger count = BigInteger.ZERO;
		CommonDTO<TCSInvoiceLineItem> comonDTO = new CommonDTO<>();
		List<TCSInvoiceLineItem> listMisMatch = tcsInvoiceLineItemDAO
				.getInvoiceMismatchesByAssessmentYearAssessmentMonthBatchIdAndMismatchcategory(batchId,
						mismatchCategory, collectorTan, pagination);
		count = tcsInvoiceLineItemDAO.getTcsInvoiceMismatchesBatchIdAndMismatchcategoryCount(batchId, mismatchCategory,
				collectorTan);
		List<TCSInvoiceLineItem> listResponse = new ArrayList<>();
		for (TCSInvoiceLineItem dto : listMisMatch) {
			TCSInvoiceLineItem response = new TCSInvoiceLineItem();
			BeanUtils.copyProperties(dto, response);
			listResponse.add(response);
		}
		PagedData<TCSInvoiceLineItem> pageData = null;
		if (!listResponse.isEmpty()) {
			pageData = new PagedData<>(listResponse, listMisMatch.size(), pagination.getPageNumber(),
					count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		}
		comonDTO.setCount(count);
		comonDTO.setResultsSet(pageData);
		if (logger.isDebugEnabled()) {
			logger.debug("Response : {}", listMisMatch);
		}
		return comonDTO;
	}

	/**
	 * Update InvoiceMismatchUpadteDTO for remediation report
	 * 
	 * @param invoiceMismatchUpdateDTO
	 * @throws RecordNotFoundException
	 * @throws URISyntaxException
	 */
	public UpdateOnScreenDTO updateMismatchByAction(String tan, UpdateOnScreenDTO invoiceMismatchUpdateDTO,
			String token, String deductorPan, String tenantId) throws RecordNotFoundException, URISyntaxException {
		List<TCSInvoiceMismatchesDTO> mismatchesList = new ArrayList<>();
		for (UpdateOnScreenDTO tcsInvoiceLineItem : invoiceMismatchUpdateDTO.getData()) {
			List<TCSInvoiceLineItem> invoiceLineItemData = tcsInvoiceLineItemDAO.findTcsInvoiceByYearAndTanAndId(
					invoiceMismatchUpdateDTO.getAssessmentYear(), tan, tcsInvoiceLineItem.getId(), false);
			BigDecimal finalAmount = BigDecimal.ZERO;
			if (!invoiceLineItemData.isEmpty()) {
				TCSInvoiceLineItem invoiceLineItem = invoiceLineItemData.get(0);
				invoiceLineItem.setFinalReason(invoiceMismatchUpdateDTO.getReason());
				if (tcsInvoiceLineItem.getFinalRate() != null && tcsInvoiceLineItem.getFinalSection() != null) {
					invoiceLineItem.setFinalTcsSection(tcsInvoiceLineItem.getFinalSection());
					invoiceLineItem.setFinalTcsRate(tcsInvoiceLineItem.getFinalRate());
					finalAmount = invoiceLineItem.getApplicableTotalTaxableAmount()
							.multiply(tcsInvoiceLineItem.getFinalRate()).divide(BigDecimal.valueOf(100));
				} else {
					if (invoiceLineItem.getFinalTcsRate() != null) {
						finalAmount = invoiceLineItem.getApplicableTotalTaxableAmount()
								.multiply(invoiceLineItem.getFinalTcsRate()).divide(BigDecimal.valueOf(100));
					}
				}
				if (invoiceLineItem.getHasLcc() != null && invoiceLineItem.getHasLcc().equals(true)) {
					invoiceLineItem = lccRateCalculation(invoiceLineItem);
				}
				invoiceLineItem.setFinalTcsAmount(finalAmount);
				invoiceLineItem.setAction(invoiceMismatchUpdateDTO.getActionType());
				invoiceLineItem.setHasMismatch(false);
				invoiceLineItem.setModifiedDate(new Date());
				invoiceLineItem.setActive(true);
				if (StringUtils.isNotBlank(invoiceLineItem.getFinalTcsSection())
						&& invoiceLineItem.getFinalTcsSection().equals("206C(1H)")
						&& invoiceLineItem.getSupplyType().equals("EXP")) {
					invoiceLineItem.setIsExempted(true);
				}
				if (invoiceLineItem.getFinalTcsRate() != null && invoiceLineItem.getFinalTcsSection() != null) {
					// feighn call to get collectee status
					String collecteeType = onboardingClient
							.getCollecteeTypeBasedOnCollecteeCode(invoiceLineItem.getCollecteeCode(), tenantId,
									invoiceLineItem.getCollectorTan(), invoiceLineItem.getCollectorPan())
							.getBody().getData();
					if (StringUtils.isNotBlank(collecteeType)) {
						List<CollecteeExempt> list = mastersClient
								.getCollecteeExempt(collecteeType, invoiceLineItem.getFinalTcsSection()).getBody()
								.getData();
						if (!list.isEmpty()) {
							invoiceLineItem.setIsExempted(true);
						}
					}

					List<Double> listRates = mastersClient.getRatesBasaedOnSection(invoiceLineItem.getFinalTcsSection())
							.getBody().getData();
					Double closestRate = closest(invoiceLineItem.getFinalTcsRate().doubleValue(), listRates);

					TCSNatureOfIncome nature = mastersClient
							.getNatureOfIncomeBasedOnSectionAndRate(invoiceLineItem.getFinalTcsSection(), closestRate)
							.getBody().getData();

					invoiceLineItem.setNatureOfIncome(nature.getNature());
					invoiceLineItem.setNoiId(nature.getId());
				}
				// Calculated cess and surcharge amounts
				if (invoiceLineItem.getItcessRate() != null && invoiceLineItem.getFinalTcsAmount() != null) {
					invoiceLineItem.setItcessAmount(invoiceLineItem.getFinalTcsAmount()
							.multiply(invoiceLineItem.getItcessRate()).multiply(new BigDecimal(0.01)));
				}
				if (invoiceLineItem.getSurchargeRate() != null && invoiceLineItem.getFinalTcsAmount() != null) {
					invoiceLineItem.setSurchargeAmount(invoiceLineItem.getFinalTcsAmount()
							.multiply(invoiceLineItem.getSurchargeRate()).multiply(new BigDecimal(0.01)));
				}

				tcsInvoiceLineItemDAO.update(invoiceLineItem);
				TCSInvoiceMismatchesDTO invoiceMismatchesDTO = new TCSInvoiceMismatchesDTO();
				invoiceMismatchesDTO.setAssessmentYear(invoiceLineItem.getAssessmentYear());
				invoiceLineItem.setCollectorTan(invoiceLineItem.getCollectorTan());
				invoiceLineItem.setLineNumber(invoiceLineItem.getLineNumber());
				invoiceLineItem.setPostingDate(invoiceLineItem.getPostingDate());
				mismatchesList.add(invoiceMismatchesDTO);
			} else {
				logger.error("No Record for Invoice Line item to Update");
				throw new CustomException("No Record for Invoice Line item to Update",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		if (token != null) {
			try {
				String url = applicationUrl + "/api/flask/invoice/tracking";
				URI uri = new URI(url);
				RestTemplate restTemplate = getRestTemplate();
				HttpHeaders headers = new HttpHeaders();
				headers.add("Authorization", token);
				headers.add("Content-Type", "application/json");
				headers.add("TAN-NUMBER", tan);
				headers.add("DEDUCTOR-PAN", deductorPan);
				HttpEntity<List<TCSInvoiceMismatchesDTO>> request = new HttpEntity<>(mismatchesList, headers);
				restTemplate.postForEntity(uri, request, String.class);
			} catch (Exception e) {
				logger.error("Exception occurred while requesting invoice tracking api", e);
			}
		}
		logger.info("Update mismatch by action  : {}", invoiceMismatchUpdateDTO);
		return invoiceMismatchUpdateDTO;
	}

	/**
	 * 
	 * @param invoiceLineItem
	 * @return
	 */
	private TCSInvoiceLineItem lccRateCalculation(TCSInvoiceLineItem invoiceLineItem) {
		long documentDate = invoiceLineItem.getDocumentDate().getTime();
		List<TCSLccMaster> lccMasterResponse = tcsPaymentDAO.getLccRecordByTanPanSectionDocumentDate(
				invoiceLineItem.getCollectorTan(), invoiceLineItem.getCollecteePan(),
				invoiceLineItem.getFinalTcsSection(), documentDate);
		if (!lccMasterResponse.isEmpty()) {
			TCSLccMaster lccMaster = lccMasterResponse.get(0);
			BigDecimal remainingBalance = tcsPaymentDAO.getLccRemainigBalance(invoiceLineItem.getCollectorTan(),
					invoiceLineItem.getCollecteePan(), invoiceLineItem.getFinalTcsSection(), documentDate);
			if (remainingBalance.doubleValue() > invoiceLineItem.getTcsAmount().doubleValue()) {
				invoiceLineItem.setFinalTcsRate(lccMaster.getLccRate().setScale(4, BigDecimal.ROUND_HALF_DOWN));
				TCSLccUtilization lccUtilization = new TCSLccUtilization();
				lccUtilization.setAssessmentYear(invoiceLineItem.getAssessmentYear());
				lccUtilization.setChallanMonth(invoiceLineItem.getChallanMonth());
				lccUtilization.setCollectorMasterTan(invoiceLineItem.getCollectorTan());
				lccUtilization.setLccMasterTotalAmount(lccMaster.getAmount() == null ? BigDecimal.ZERO
						: lccMaster.getAmount().setScale(2, RoundingMode.UP));
				lccUtilization.setRemainingAmount(
						remainingBalance.subtract(invoiceLineItem.getTcsAmount()).setScale(2, RoundingMode.UP));
				lccUtilization.setLccMasterUtilizedAmount(invoiceLineItem.getTcsAmount().setScale(2, RoundingMode.UP));
				lccUtilization.setActive(true);
				lccUtilization.setConsumedFrom(UploadTypes.INVOICE.name());
				lccUtilization.setCreatedDate(new Date());
				lccUtilization.setChallanMonth(invoiceLineItem.getChallanMonth());
				lccUtilization.setLccMasterId(lccMaster.getId());
				lccUtilization.setLccMasterPan(lccMaster.getLccMasterPan());
				lccUtilization.setModifiedDate(new Date());
				lccUtilization.setLccMasterLccApplicableFrom(lccMaster.getApplicableFrom());
				lccUtilization.setLccMasterLccApplicableTo(lccMaster.getApplicableTo());
				lccUtilization.setLccMasterTotalAmount(lccMaster.getAmount() == null ? BigDecimal.ZERO
						: lccMaster.getAmount().setScale(2, RoundingMode.UP));
				lccUtilization.setInvoiceLineItemId(invoiceLineItem.getId());
				lccUtilization.setInvoiceProcessDate(invoiceLineItem.getDocumentDate());
				tcsLccUtilizationDAO.save(lccUtilization);
			}
		}
		return invoiceLineItem;
	}

	/**
	 * 
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
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

	/**
	 * 
	 * @param residentType
	 * @param filetype
	 * @param collectorTan
	 * @param year
	 * @param month
	 * @param pagination
	 * @param collecteeName
	 * @return
	 */
	public CommonDTO<TCSInvoiceLineItem> getResidentAndNonresident(String residentType, String filetype,
			String collectorTan, int year, int month, Pagination pagination, String collecteeName) {
		BigInteger count = BigInteger.ZERO;
		CommonDTO<TCSInvoiceLineItem> invoiceLineItemList = new CommonDTO<>();
		logger.info("tan : {} year : {} month : {} resident type : {} deductee name : {}", collectorTan, year, month,
				residentType, collecteeName);
		List<TCSInvoiceLineItem> invoiceLineItemListData = new ArrayList<>();
		if ("nocollecteefilter".equalsIgnoreCase(collecteeName)) {
			invoiceLineItemListData = tcsInvoiceLineItemDAO.findAllResidentAndNonResident(year, month, collectorTan,
					residentType, filetype, pagination);
			count = tcsInvoiceLineItemDAO.findAllResidentAndNonResidentCount(year, month, collectorTan, residentType,
					filetype);
		} else {
			invoiceLineItemListData = tcsInvoiceLineItemDAO.findAllResidentAndNonResidentByDeductee(year, month,
					collectorTan, residentType, filetype, collecteeName, pagination);
			count = tcsInvoiceLineItemDAO.findAllResidentAndNonResidentByDeducteeCount(year, month, collectorTan,
					residentType, collecteeName, filetype);
		}
		PagedData<TCSInvoiceLineItem> pageData = new PagedData<>(invoiceLineItemListData,
				invoiceLineItemListData.size(), pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		invoiceLineItemList.setCount(count);
		invoiceLineItemList.setResultsSet(pageData);
		return invoiceLineItemList;
	}

	/**
	 * get the Mismatches based on the Category
	 * 
	 * @param mismatchCategory
	 * @return
	 */
	public CommonDTO<TCSInvoiceLineItem> getInvoiceMismatchBasedOnMismatchCategory(String mismatchCategory, String tan,
			int year, int challanMonth, MismatchesFiltersDTO filters) {
		CommonDTO<TCSInvoiceLineItem> invoiceLineItemData = new CommonDTO<>();
		List<TCSInvoiceLineItem> listInvoiceResponse = new ArrayList<TCSInvoiceLineItem>();
		List<TCSInvoiceLineItem> listMisMatch = tcsInvoiceLineItemDAO
				.getInvoicesByAssessmentYearChallanMonthByTanMismatchCategory(year, challanMonth, tan, mismatchCategory,
						filters);
		BigInteger count = tcsInvoiceLineItemDAO.getInvoicesCountByYearMonthAndTanMismatchCategory(year, challanMonth,
				tan, mismatchCategory, filters);
		for (int index = 0; index < listMisMatch.size(); index++) {
			TCSInvoiceLineItem invoiceLineItem = listMisMatch.get(index);

			invoiceLineItem.setDerivedTcsAmount(invoiceLineItem.getDerivedTcsAmount());
			invoiceLineItem.setFinalTcsAmount(invoiceLineItem.getFinalTcsAmount());
			// invoiceLineItem.setTcsDeducted(invoiceLineItem.getTcsDeducted());
			invoiceLineItem.setIgstAmount(invoiceLineItem.getIgstAmount());
			invoiceLineItem.setCgstAmount(invoiceLineItem.getCgstAmount());
			invoiceLineItem.setSgstAmount(invoiceLineItem.getSgstAmount());
			invoiceLineItem.setItcessAmount(invoiceLineItem.getItcessAmount());
			invoiceLineItem.setTcsAmount(invoiceLineItem.getTcsAmount());
			// invoiceLineItem.setAmountPaidCredited(invoiceLineItem.getAmountPaidCredited());
			invoiceLineItem.setActualTcsAmount(invoiceLineItem.getActualTcsAmount());
			// invoiceLineItem.setInterest(invoiceLineItem.getInterest());
			// invoiceLineItem.setInvoiceAmount(invoiceLineItem.getInvoiceAmount());
			// invoiceLineItem.setPenalty(invoiceLineItem.getPenalty());
			invoiceLineItem.setSurchargeAmount(invoiceLineItem.getSurchargeAmount());
			listInvoiceResponse.add(invoiceLineItem);
		}
		PagedData<TCSInvoiceLineItem> pagedData = null;
		if (!listInvoiceResponse.isEmpty()) {
			pagedData = new PagedData<TCSInvoiceLineItem>(listInvoiceResponse, listInvoiceResponse.size(),
					filters.getPagination().getPageNumber(),
					count.intValue() > (filters.getPagination().getPageSize() * filters.getPagination().getPageNumber())
							? false
							: true);
		}
		invoiceLineItemData.setResultsSet(pagedData);
		invoiceLineItemData.setCount(count);
		return invoiceLineItemData;
	}

	/**
	 * Update the Invoice Line Item Based on tan and id.
	 *
	 * @param tan
	 * @param invoiceLineItemDTO
	 * @return
	 */
	public TCSInvoiceLineItem updateInvoiceLineItemById(String tan, TCSInvoiceLineItem invoiceLineItemDTO,
			String userName) {
		logger.info("Request InvoiceLineItem is: {}", invoiceLineItemDTO);
		List<TCSInvoiceLineItem> invoiceLine = tcsInvoiceLineItemDAO.findByYearTanDocumentPostingDateId(
				invoiceLineItemDTO.getAssessmentYear(), tan, invoiceLineItemDTO.getPostingDate(),
				invoiceLineItemDTO.getId());
		TCSInvoiceLineItem invoiceLineItemData = null;
		if (!invoiceLine.isEmpty()) {
			invoiceLineItemData = invoiceLine.get(0);
			BeanUtils.copyProperties(invoiceLineItemDTO, invoiceLineItemData);
			tcsInvoiceLineItemDAO.update(invoiceLineItemData);
		} else {
			throw new CustomException("Data is not present with  " + invoiceLineItemDTO.getId(), HttpStatus.NOT_FOUND);
		}
		return invoiceLineItemData;
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
	 * @param invoiceLineItem
	 * @return
	 */
	public InvoiceMismatchesDTO convertTOInvoiceMismatchesDTO(InvoiceLineItemResponseDTO invoiceLineItem) {
		InvoiceMismatchesDTO invoiceMismatchesDTO = new InvoiceMismatchesDTO();
		invoiceMismatchesDTO.setAssessmentYear(invoiceLineItem.getAssessmentYear());
		invoiceMismatchesDTO.setDeductorTan(invoiceLineItem.getDeductorTan());
		// TODO DYNAMIC ID NEED TO BE ASSIGNED
		invoiceMismatchesDTO.setInvoiceLineItemId(null);// invoiceLineItem.getKey().getId()
		Date date = invoiceLineItem.getDocumentPostingDate();
		String postingDocumentDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
		invoiceMismatchesDTO.setDocumentPostingDate(postingDocumentDate);
		return invoiceMismatchesDTO;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public File convertInvoiceCsvToxlsx(File csvFile, String tan, String tenantId, String deductorPan,
			Integer assesmentYear, TCSBatchUpload batchUpload) throws Exception {
		// Aspose method to convert
		Workbook workbook = invoiceXlsxReport(csvFile, tan, tenantId, deductorPan, assesmentYear, batchUpload);
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

	public Workbook invoiceXlsxReport(File csvFile, String tan, String tenantId, String deductorPan,
			Integer assesmentYear, TCSBatchUpload batchUpload) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		worksheet.getCells().importArray(ErrorReportService.tcsInvoiceLineItemheaderNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		// CR error reports data
		List<TCSInvoiceLineItem> crErrorRecords = tcsInvoiceLineItemDAO.getCrErrorRecords(tan, assesmentYear,
				batchUpload.getId());

		if (crErrorRecords.isEmpty()) {
			Reader reader = new FileReader(csvFile);
			@SuppressWarnings("unchecked")
			CsvToBean<TcsInvoiceErrorReportCsvDTO> csvToBean = new CsvToBeanBuilder(reader)
					.withType(TcsInvoiceErrorReportCsvDTO.class).withIgnoreLeadingWhiteSpace(true).build();
			setExtractDataForInvoiceFromCsv(csvToBean.parse(), worksheet);
		} else {
			setExtractDataForInvoice(crErrorRecords, worksheet);
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

		// Style for D6 to BI6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:BJ6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

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
		String lastHeaderCellName = "BJ6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:BJ6");
		return workbook;
	}

	private int setExtractDataForInvoiceFromCsv(List<TcsInvoiceErrorReportCsvDTO> invoiceErrorReportsCsvList,
			Worksheet worksheet) throws Exception {
		int rowIndex = 6;
		if (!invoiceErrorReportsCsvList.isEmpty()) {
			List<ErrorCode> errorCodesObjs = mastersClient.getAllErrorCodes().getBody().getData();
			Map<String, String> errorDescription = new HashMap<>();
			for (ErrorCode errorCodesObj : errorCodesObjs) {
				errorDescription.put(errorCodesObj.getCode(), errorCodesObj.getDescription());
			}
			for (TcsInvoiceErrorReportCsvDTO errorReportsDTO : invoiceErrorReportsCsvList) {
				// startDate = new Date();
				ArrayList<Object> rowData = new ArrayList<Object>();
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
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorTan()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorTan());
				rowData.add(StringUtils.isBlank(errorCodesWithNewLine) ? StringUtils.EMPTY : errorCodesWithNewLine);
				rowData.add(errorReportsDTO.getIndex());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceIdentifier()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceIdentifier());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceFileName()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceFileName());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorCode());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorPan()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorPan());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorTan()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorTan());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorGstin());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollecteeCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollecteeCode());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollecteePan()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollecteePan());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollecteeGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollecteeGstin());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentType());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSupplyType()) ? StringUtils.EMPTY
						: errorReportsDTO.getSupplyType());
				rowData.add(errorReportsDTO.getDocumentNumber() == null ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentNumber());
				rowData.add(errorReportsDTO.getDocumentDate());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getOriginalDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getOriginalDocumentNumber());
				rowData.add(errorReportsDTO.getOriginalDocumentDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getOriginalDocumentDate());
				rowData.add(errorReportsDTO.getAccountingDocumentNumber() == null ? StringUtils.EMPTY
						: errorReportsDTO.getAccountingDocumentNumber());
				rowData.add(errorReportsDTO.getPostingDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getPostingDate());
				rowData.add(
						errorReportsDTO.getLineNumber() == null ? StringUtils.EMPTY : errorReportsDTO.getLineNumber());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getHsnOrSac()) ? StringUtils.EMPTY
						: errorReportsDTO.getHsnOrSac());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSoDesc()) ? StringUtils.EMPTY
						: errorReportsDTO.getSoDesc());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getInvoiceDesc()) ? StringUtils.EMPTY
						: errorReportsDTO.getInvoiceDesc());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSoDesc()) ? StringUtils.EMPTY
						: errorReportsDTO.getSoDesc());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGlAccountCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getGlAccountCode());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGlDesc()) ? StringUtils.EMPTY
						: errorReportsDTO.getGlDesc());
				rowData.add(errorReportsDTO.getTaxableValue() == null ? StringUtils.EMPTY
						: errorReportsDTO.getTaxableValue());
				rowData.add(
						errorReportsDTO.getIgstAmount() == null ? StringUtils.EMPTY : errorReportsDTO.getIgstAmount());
				rowData.add(
						errorReportsDTO.getCgstAmount() == null ? StringUtils.EMPTY : errorReportsDTO.getCgstAmount());
				rowData.add(
						errorReportsDTO.getSgstAmount() == null ? StringUtils.EMPTY : errorReportsDTO.getSgstAmount());
				rowData.add(errorReportsDTO.getDiscount() == null ? StringUtils.EMPTY : errorReportsDTO.getDiscount());
				rowData.add(errorReportsDTO.getInvoiceValue() == null ? StringUtils.EMPTY
						: errorReportsDTO.getInvoiceValue());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTdsSection()) ? StringUtils.EMPTY
						: errorReportsDTO.getTdsSection());
				rowData.add(errorReportsDTO.getTdsRate() == null ? StringUtils.EMPTY : errorReportsDTO.getTdsRate());
				rowData.add(
						errorReportsDTO.getTdsAmount() == null ? StringUtils.EMPTY : errorReportsDTO.getTdsAmount());
				rowData.add(errorReportsDTO.getSurchargeRate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getSurchargeRate());
				rowData.add(errorReportsDTO.getSurchargeAmount() == null ? StringUtils.EMPTY
						: errorReportsDTO.getSurchargeAmount());
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

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}
		return rowIndex;
	}

	private int setExtractDataForInvoice(List<TCSInvoiceLineItem> invoiceErrorReportsCsvList, Worksheet worksheet)
			throws Exception {
		int rowIndex = 6;
		Integer sequenceNumber = 1;
		if (!invoiceErrorReportsCsvList.isEmpty()) {
			List<ErrorCode> errorCodesObjs = mastersClient.getAllErrorCodes().getBody().getData();
			Map<String, String> errorDescription = new HashMap<>();
			for (ErrorCode errorCodesObj : errorCodesObjs) {
				errorDescription.put(errorCodesObj.getCode(), errorCodesObj.getDescription());
			}
			for (TCSInvoiceLineItem errorReportsDTO : invoiceErrorReportsCsvList) {
				// startDate = new Date();
				ArrayList<Object> rowData = new ArrayList<Object>();
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
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorTan()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorTan());
				rowData.add(StringUtils.isBlank(errorCodesWithNewLine) ? StringUtils.EMPTY : errorCodesWithNewLine);
				rowData.add(sequenceNumber++);
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceIdentifier()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceIdentifier());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceFileName()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceFileName());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorCode());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorPan()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorPan());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorTan()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorTan());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorGstin());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollecteeCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollecteeCode());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollecteePan()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollecteePan());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollecteeGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollecteeGstin());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentType());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSupplyType()) ? StringUtils.EMPTY
						: errorReportsDTO.getSupplyType());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentNumber());
				rowData.add(errorReportsDTO.getDocumentDate());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getOriginalDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getOriginalDocumentNumber());
				rowData.add(errorReportsDTO.getOriginalDocumentDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getOriginalDocumentDate());
				rowData.add(errorReportsDTO.getAccountingDocumentNumber() == null ? StringUtils.EMPTY
						: errorReportsDTO.getAccountingDocumentNumber());
				rowData.add(errorReportsDTO.getPostingDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getPostingDate());
				rowData.add(
						errorReportsDTO.getLineNumber() == null ? StringUtils.EMPTY : errorReportsDTO.getLineNumber());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getHsnOrSac()) ? StringUtils.EMPTY
						: errorReportsDTO.getHsnOrSac());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSoDesc()) ? StringUtils.EMPTY
						: errorReportsDTO.getSoDesc());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getInvoiceDesc()) ? StringUtils.EMPTY
						: errorReportsDTO.getInvoiceDesc());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSoDesc()) ? StringUtils.EMPTY
						: errorReportsDTO.getSoDesc());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGlAccountCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getGlAccountCode());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGlDesc()) ? StringUtils.EMPTY
						: errorReportsDTO.getGlDesc());
				rowData.add(errorReportsDTO.getTaxableValue() == null ? StringUtils.EMPTY
						: errorReportsDTO.getTaxableValue());
				rowData.add(
						errorReportsDTO.getIgstAmount() == null ? StringUtils.EMPTY : errorReportsDTO.getIgstAmount());
				rowData.add(
						errorReportsDTO.getCgstAmount() == null ? StringUtils.EMPTY : errorReportsDTO.getCgstAmount());
				rowData.add(
						errorReportsDTO.getSgstAmount() == null ? StringUtils.EMPTY : errorReportsDTO.getSgstAmount());
				rowData.add(errorReportsDTO.getDiscount() == null ? StringUtils.EMPTY : errorReportsDTO.getDiscount());
				rowData.add(errorReportsDTO.getInvoiceValue() == null ? StringUtils.EMPTY
						: errorReportsDTO.getInvoiceValue());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getActualTcsSection()) ? StringUtils.EMPTY
						: errorReportsDTO.getActualTcsSection());
				rowData.add(errorReportsDTO.getActualTcsRate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getActualTcsRate());
				rowData.add(errorReportsDTO.getActualTcsAmount() == null ? StringUtils.EMPTY
						: errorReportsDTO.getActualTcsAmount());
				rowData.add(errorReportsDTO.getSurchargeRate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getSurchargeRate());
				rowData.add(errorReportsDTO.getSurchargeAmount() == null ? StringUtils.EMPTY
						: errorReportsDTO.getSurchargeAmount());
				rowData.add(
						errorReportsDTO.getItcessRate() == null ? StringUtils.EMPTY : errorReportsDTO.getItcessRate());
				rowData.add(errorReportsDTO.getItcessAmount() == null ? StringUtils.EMPTY
						: errorReportsDTO.getItcessAmount());
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

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
				sequenceNumber++;
			}
		}
		return rowIndex;
	}

	private void setCrErrorRecordsData(List<TCSInvoiceLineItem> invoiceErrorReportsCsvList, Worksheet worksheet,
			int rowIndex) throws Exception {
		Integer sequenceNumber = 1;
		if (!invoiceErrorReportsCsvList.isEmpty()) {
			for (TCSInvoiceLineItem errorReportsDTO : invoiceErrorReportsCsvList) {
				// startDate = new Date();
				ArrayList<Object> rowData = new ArrayList<Object>();

				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorTan()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorTan());
				rowData.add(errorReportsDTO.getErrorReason());
				rowData.add(sequenceNumber++);
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceIdentifier()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceIdentifier());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceFileName()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceFileName());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorCode());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorPan()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorPan());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorTan()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorTan());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollectorGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollectorGstin());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollecteeCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollecteeCode());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollecteePan()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollecteePan());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCollecteeGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getCollecteeGstin());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentType());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSupplyType()) ? StringUtils.EMPTY
						: errorReportsDTO.getSupplyType());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentNumber());
				rowData.add(errorReportsDTO.getDocumentDate());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getOriginalDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getOriginalDocumentNumber());
				rowData.add(errorReportsDTO.getOriginalDocumentDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getOriginalDocumentDate());
				rowData.add(errorReportsDTO.getAccountingDocumentNumber() == null ? StringUtils.EMPTY
						: errorReportsDTO.getAccountingDocumentNumber());
				rowData.add(errorReportsDTO.getPostingDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getPostingDate());
				rowData.add(
						errorReportsDTO.getLineNumber() == null ? StringUtils.EMPTY : errorReportsDTO.getLineNumber());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getHsnOrSac()) ? StringUtils.EMPTY
						: errorReportsDTO.getHsnOrSac());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSoDesc()) ? StringUtils.EMPTY
						: errorReportsDTO.getSoDesc());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getInvoiceDesc()) ? StringUtils.EMPTY
						: errorReportsDTO.getInvoiceDesc());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSoDesc()) ? StringUtils.EMPTY
						: errorReportsDTO.getSoDesc());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGlAccountCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getGlAccountCode());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGlDesc()) ? StringUtils.EMPTY
						: errorReportsDTO.getGlDesc());
				rowData.add(errorReportsDTO.getTaxableValue() == null ? StringUtils.EMPTY
						: errorReportsDTO.getTaxableValue());
				rowData.add(
						errorReportsDTO.getIgstAmount() == null ? StringUtils.EMPTY : errorReportsDTO.getIgstAmount());
				rowData.add(
						errorReportsDTO.getCgstAmount() == null ? StringUtils.EMPTY : errorReportsDTO.getCgstAmount());
				rowData.add(
						errorReportsDTO.getSgstAmount() == null ? StringUtils.EMPTY : errorReportsDTO.getSgstAmount());
				rowData.add(errorReportsDTO.getDiscount() == null ? StringUtils.EMPTY : errorReportsDTO.getDiscount());
				rowData.add(errorReportsDTO.getInvoiceValue() == null ? StringUtils.EMPTY
						: errorReportsDTO.getInvoiceValue());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTcsSection()) ? StringUtils.EMPTY
						: errorReportsDTO.getTcsSection());
				rowData.add(errorReportsDTO.getTcsRate() == null ? StringUtils.EMPTY : errorReportsDTO.getTcsRate());
				rowData.add(
						errorReportsDTO.getTcsAmount() == null ? StringUtils.EMPTY : errorReportsDTO.getTcsAmount());
				rowData.add(errorReportsDTO.getSurchargeRate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getSurchargeRate());
				rowData.add(errorReportsDTO.getSurchargeAmount() == null ? StringUtils.EMPTY
						: errorReportsDTO.getSurchargeAmount());
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

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
				sequenceNumber++;
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
		List<TCSBatchUpload> batchUploadData = tcsBatchUploadDAO.findById(assessmentYear, tan,
				UploadTypes.INVOICE_PDF.name(), batchId);
		if (!batchUploadData.isEmpty()) {
			TCSBatchUpload batchData = batchUploadData.get(0);
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

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param deductorTan
	 * @param challanPaid
	 * @param isForNonResidents
	 * @return
	 */
	public List<TCSInvoiceLineItem> getInvoiceLineItemByAssessmentYearChallanMonthDeductorTan(int assessmentYear,
			int challanMonth, List<String> deductorTan, boolean challanPaid, boolean isForNonResidents) {
		List<TCSInvoiceLineItem> listResponse = new ArrayList<TCSInvoiceLineItem>();
		List<TCSInvoiceLineItem> response = tcsInvoiceLineItemDAO
				.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(assessmentYear, Arrays.asList(challanMonth),
						deductorTan, challanPaid, isForNonResidents);
		logger.info("response size : {}", response.size());
		response.forEach(invoice -> {
			TCSInvoiceLineItem invoiceLineItemDTO = new TCSInvoiceLineItem();
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

	/**
	 * 
	 * @param csvFile
	 * @param batchUpload
	 * @return
	 * @throws Exception
	 */
	public InvoicePdfList readCsvForInvoicePdfProcess(File csvFile, TCSBatchUpload batchUpload) throws Exception {
		Reader reader = new FileReader(csvFile);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		CsvToBean<InvoicePdfCsvDTO> csvToBean = new CsvToBeanBuilder(reader).withType(InvoicePdfCsvDTO.class)
				.withIgnoreLeadingWhiteSpace(true).build();
		InvoicePdfList invoicePdfList = new InvoicePdfList();
		invoicePdfList.setBatchUploadUrl(batchUpload.getFilePath());
		invoicePdfList.setInvoiceList(csvToBean.parse());

		return invoicePdfList;
	}

	public CommonDTO<TCSInvoiceLineItem> getInvoiceByType(String tan, String type, Pagination pagination, int year,
			int month) {
		BigInteger count = BigInteger.ZERO;
		count = tcsInvoiceLineItemDAO.getInvoceCountByType(tan, type, year, month);
		logger.info("count is {}", count);
		CommonDTO<TCSInvoiceLineItem> invoices = new CommonDTO<>();
		List<TCSInvoiceLineItem> list = tcsInvoiceLineItemDAO.getInvoiceByType(tan, type, year, month, pagination);
		List<TCSInvoiceLineItem> listResponse = new ArrayList<>();
		for (TCSInvoiceLineItem dto : list) {
			TCSInvoiceLineItem response = new TCSInvoiceLineItem();
			BeanUtils.copyProperties(dto, response);
			listResponse.add(response);
		}
		PagedData<TCSInvoiceLineItem> pagedData = null;
		if (!listResponse.isEmpty()) {
			pagedData = new PagedData<>(listResponse, listResponse.size(), pagination.getPageNumber(),
					count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		}
		invoices.setCount(count);
		invoices.setResultsSet(pagedData);
		return invoices;
	}

	public TCSInvoiceLineItem getPdfTransactionStatus(Integer year, String tan) {
		List<TCSInvoiceLineItem> invoiceLineItemObj = tcsInvoiceLineItemDAO.invoiceLineItemBasedOnTanAndYear(year, tan);
		if (!invoiceLineItemObj.isEmpty()) {
			return invoiceLineItemObj.get(0);
		}
		return new TCSInvoiceLineItem();
	}

	public List<TCSInvoiceLineItem> getAllInvoiceLineItemData(Integer year, String tan) {
		List<TCSInvoiceLineItem> listOfInvoice = new ArrayList<>();
		List<TCSInvoiceLineItem> lisofObj = tcsInvoiceLineItemDAO.getAllInvoiceTanAndYear(year, tan);
		if (!lisofObj.isEmpty()) {
			lisofObj.forEach(invoiceData -> {
				listOfInvoice.add(invoiceData);
			});
			return listOfInvoice;
		}
		return listOfInvoice;
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
		long trueValue = tcsInvoiceLineItemDAO.getTdsCalculationStatus(assessmentYear, tans, firstDate, lastDate, true);
		long falseValue = tcsInvoiceLineItemDAO.getTdsCalculationStatus(assessmentYear, tans, firstDate, lastDate,
				false);
		if (trueValue > 0 && falseValue > 0) {
			return ActivityTrackerStatus.PENDING.name();
		} else if (trueValue > 0 && falseValue == 0) {
			return ActivityTrackerStatus.VALIDATED.name();
		}
		return ActivityTrackerStatus.NORECORDS.name();
	}

	/**
	 * 
	 * @param crData
	 * @return
	 */
	public CommonDTO<TCSInvoiceLineItem> getInvoicesByCrData(CRDTO crData) {
		CommonDTO<TCSInvoiceLineItem> commonDTO = new CommonDTO<>();
		List<TCSInvoiceLineItem> crInvoiceData = tcsInvoiceLineItemDAO.findById(crData.getCr().getAssessmentYear(),
				crData.getCr().getDeductorTan(), crData.getCr().getDocumentPostingDate(), crData.getCr().getId(),
				false);
		if (!crInvoiceData.isEmpty()) {
			Date date = crInvoiceData.get(0).getOriginalDocumentDate();
			String orginalDocumentDate = new SimpleDateFormat("MM/dd/yyyy").format(date);
			PagedData<TCSInvoiceLineItem> invoices = tcsInvoiceLineItemDAO.getInvoicesByCRData(crData,
					crData.getPagination(), crInvoiceData.get(0).getOriginalDocumentDate());
			commonDTO.setResultsSet(invoices);
			commonDTO.setCount(tcsInvoiceLineItemDAO.getInvoicesCountByCRData(crData, orginalDocumentDate));
		}
		return commonDTO;
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

	/**
	 * 
	 * @param assessmentYear
	 * @param month
	 * @param deducteeName
	 * @param tan
	 * @param pagination
	 * @return
	 */
	public CommonDTO<TCSInvoiceLineItem> getInvoicesByCurrentMonth(int assessmentYear, int month, String deducteeName,
			String tan, Pagination pagination) {
		CommonDTO<TCSInvoiceLineItem> invoices = new CommonDTO<>();
		logger.info("tan : {} year : {} month : {} deductee name : {}", tan, assessmentYear, month, deducteeName);
		List<TCSInvoiceLineItem> invoiceLineItemList = tcsInvoiceLineItemDAO.getInvoicesByCurrentMonth(assessmentYear,
				month, tan, deducteeName, pagination);
		BigInteger invoicesCount = tcsInvoiceLineItemDAO.getInvoicesCountByCurrentMonth(assessmentYear, month, tan,
				deducteeName);
		List<TCSInvoiceLineItem> listResponse = new ArrayList<>();
		for (TCSInvoiceLineItem dto : invoiceLineItemList) {
			TCSInvoiceLineItem response = new TCSInvoiceLineItem();
			BeanUtils.copyProperties(dto, response);
			listResponse.add(response);
		}
		logger.info("Total Invoices Count : {}", invoicesCount);
		PagedData<TCSInvoiceLineItem> pagedData = null;
		if (!listResponse.isEmpty()) {
			pagedData = new PagedData<>(listResponse, listResponse.size(), pagination.getPageNumber(),
					invoicesCount.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		}
		invoices.setResultsSet(pagedData);
		invoices.setCount(invoicesCount);
		return invoices;
	}

	/**
	 * 
	 * @param list
	 * @return
	 */
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

	/**
	 * 
	 * @param collectorTan
	 * @param collecteeType
	 * @param type
	 * @param year
	 * @param month
	 * @param isMismatch
	 * @return
	 */
	public Set<String> getCollectees(String collectorTan, String type, int year, int month, boolean isMismatch) {
		/*
		 * if ("resident".equalsIgnoreCase(collecteeType)) { collecteeType = "N"; } else
		 * { collecteeType = "Y"; }
		 */
		if ("invoice".equalsIgnoreCase(type)) {
			return tcsInvoiceLineItemDAO.getInvoiceCollectees(collectorTan, year, month, isMismatch).parallelStream()
					.map(TCSInvoiceLineItem::getCollecteeName).distinct().collect(Collectors.toSet());
		} else if ("payment".equalsIgnoreCase(type)) {
			return tcsPaymentDAO.getTcsPaymentCollectees(collectorTan, year, month, isMismatch).parallelStream()
					.map(TcsPaymentDTO::getCollecteeName).distinct().collect(Collectors.toSet());
		} else {
			return null;
		}
	}

	/**
	 * Update InvoiceMismatchUpadteDTO for remediation report
	 * 
	 * @param invoiceMismatchUpdateDTO
	 * @throws RecordNotFoundException
	 */
	public TCSInvoiceLineItem updateMismatchByActionForPdf(String tan, TCSInvoiceLineItem invoiceMismatchUpdateDTO)
			throws RecordNotFoundException {
		// logger.info("SEQUENCE NUMBER : {}",
		// invoiceMismatchUpdateDTO.getSequenceNumber());
		logger.info("Batch ID  : {} ", invoiceMismatchUpdateDTO.getBatchUploadId());
		logger.info("Line Item ID : {}", invoiceMismatchUpdateDTO.getLineNumber());
		List<TCSInvoiceLineItem> invoiceLineItem = tcsInvoiceLineItemDAO.findByYearTanDocumentPostingDateIdActive(
				Calendar.getInstance().get(Calendar.YEAR), tan, invoiceMismatchUpdateDTO.getPostingDate(),
				invoiceMismatchUpdateDTO.getLineNumber(), false);
		if (!invoiceLineItem.isEmpty()) {
			invoiceLineItem.get(0).setAction(invoiceMismatchUpdateDTO.getAction());
			invoiceLineItem.get(0).setFinalTcsRate(invoiceMismatchUpdateDTO.getFinalTcsRate());
			invoiceLineItem.get(0).setFinalTcsSection(invoiceMismatchUpdateDTO.getFinalTcsSection());
			tcsInvoiceLineItemDAO.update(invoiceLineItem.get(0));
		} else {
			throw new RecordNotFoundException("No Record for Invoice Line item");
		}
		return invoiceMismatchUpdateDTO;
	}

	/**
	 * 
	 * @param file
	 * @return BatchUpload Object
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	public TCSBatchUpload importToBatchUpload(MultipartFile file, String tenantId, String userName)
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
		batchUpload.setCollectorMasterTan("missingCollectorMasterTan");
		batchUpload.setUploadType(UploadTypes.INV_REM.name());
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
		if (!batch.isEmpty()) {
			logger.info("Duplicate PDF Record inserting : {}", file.getOriginalFilename());
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setReferenceId(batch.get(0).getId());
			batchUpload = tcsBatchUploadDAO.update(batchUpload);
		} else {
			batchUpload.setNewStatus("Uploaded");
			logger.info("Unique record creating : {}", file.getOriginalFilename());
			batchUpload = tcsBatchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param tan
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
	public ByteArrayInputStream asyncExportRemediationReport(String tan, String tenantId, String deductorPan, int year,
			int month, String userName) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		return exportRemediationReport(tan, tenantId, deductorPan, year, month, userName);
	}

	/**
	 * 
	 * @param tan
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
	public ByteArrayInputStream exportRemediationReport(String tan, String tenantId, String deductorPan, int year,
			int month, String userName) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TCSBatchUpload batchUpload = saveMismatchReport(tan, tenantId, year, out, 0L,
				UploadTypes.INVOICE_MISMATCH_REPORT.name(), "Processing", month, userName, null);
		String type = "invoice_mismatch";
		// Invocking spark Job id
		logger.info("Notebook type : {}", type);
		TCSNoteBookParam noteBookParam = createNoteBook(year, tan, tenantId, userName, month, batchUpload);
		SparkNotebooks.Notebook notebook = sparkNotebooks.getNotebooks().get("tcs_" + type.toLowerCase());
		logger.info("Notebook url : {}", notebook.getUrl());
		triggerSparkNotebook(notebook.getUrl(), notebook.getJobId(), noteBookParam, month, year, tenantId, tan,
				userName);

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
	 * @param token
	 * @param year
	 * @return
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 * @throws SQLException
	 */

	@Async
	public TCSBatchUpload asyncUpdateRemediationReport(String tan, TCSBatchUpload batchUpload, String path,
			String tenantId, String deductorPan, String token, int year, String userEmail, Integer month)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Came to invoice method");
		logger.info("Batch upload 1 : {}", batchUpload);
		logger.info("Batch upload 1: " + batchUpload);
		if (batchUpload != null) {
			logger.info("batchOptional 2: " + batchUpload);
			batchUpload.setStatus("Processing");
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			tcsBatchUploadDAO.update(batchUpload);
			batchUpload = updateRemediationReport(tan, path, tenantId, deductorPan, token, year, batchUpload, userEmail,
					month);
		}
		logger.info("Processed the mismatch file");
		return batchUpload;
	}

	/**
	 * 
	 * @param tan
	 * @param batchId
	 * @param file
	 * @param tenantId
	 * @param deductorPan
	 * @param token
	 * @param year
	 * @param tcsBatchUpload
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 * @throws SQLException
	 */
	public TCSBatchUpload updateRemediationReport(String tan, String path, String tenantId, String deductorPan,
			String token, int year, TCSBatchUpload tcsBatchUpload, String userEmail, Integer month)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String otherFilepath = null;
		Biff8EncryptionKey.setCurrentUserPassword("password");
		// Load the input Excel file
		Workbook workbook;
		try {
			logger.info("Mismatch file path" + path);
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
			List<Integer> invoiceIds = new ArrayList<>();
			boolean isCancel = false;
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
			List<TCSInvoiceLineItem> tcsInvoiceLineItems = new ArrayList<>();
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
				// SupplyType
				String supplyTypeFromExcel = row.getField("Supply Type");
				// CollecteeType
				String collecteeTypeFromExcel = row.getField("Collectee Type");

				String userAction = row.getField("Action");
				String reason = row.getField("Reason");
				Integer invoiceLineItemId = StringUtils.isNotBlank(row.getField("Invoice Id"))
						? Integer.parseInt(row.getField("Invoice Id"))
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
				Boolean isError = false;
				BigDecimal cessAmount = BigDecimal.ZERO;
				BigDecimal surchargeAmount = BigDecimal.ZERO;
				String natureOfIncome = null;
				Long noiId = null;

				if (userAction != null && !userAction.isEmpty()) {
					BigDecimal finalAmount = new BigDecimal(0);
					BigDecimal taxableAmount = new BigDecimal(0);
					taxableAmount = StringUtils.isNotBlank(row.getField("Applicable Total Taxable Amount"))
							? new BigDecimal(row.getField("Applicable Total Taxable Amount"))
							: BigDecimal.ZERO;
					if (invoiceLineItemId != null) {
						TCSInvoiceLineItem tcsInvoiceLineItem = new TCSInvoiceLineItem();
						if (userAction != null && !userAction.isEmpty()) {
							if ("Accept".equalsIgnoreCase(userAction)) {
								if (derivedTcsSectionFromExcel != null && derivedTcsRateFromExcel != null
										&& derivedTcsAmountFromExcel != null) {
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
								if (actualTcsSectionFromExcel != null && actualTcsRateFromExcel != null) {
									finalTcsRate = actualTcsRateFromExcel;
									finalTcsSection = actualTcsSectionFromExcel;
									isMismatch = false;
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
								tcsInvoiceLineItem.setErrorReason("Canceled record-ERR029");
								invoiceIds.add(invoiceLineItemId);
							}

							if (!userAction.equalsIgnoreCase("Cancel")) {
								if (finalTcsSection != null && finalTcsSection.equals("206C(1H)")
										&& supplyTypeFromExcel.equals("EXP")) {
									isExempt = true;
								}
								// feign call to get collectee status
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
							tcsInvoiceLineItem.setFinalReason(reason);
							tcsInvoiceLineItem.setActive(active);
							tcsInvoiceLineItem.setIsExempted(isExempt);
							tcsInvoiceLineItem.setFinalTcsAmount(finalAmount);
							tcsInvoiceLineItem.setFinalTcsRate(finalTcsRate);
							tcsInvoiceLineItem.setFinalTcsSection(finalTcsSection);
							tcsInvoiceLineItem.setHasMismatch(isMismatch);
							tcsInvoiceLineItem.setItcessAmount(cessAmount);
							tcsInvoiceLineItem.setSurchargeAmount(surchargeAmount);
							if (StringUtils.isNoneBlank(natureOfIncome)) {
								tcsInvoiceLineItem.setNatureOfIncome(natureOfIncome);
							}
							if (noiId != null) {
								tcsInvoiceLineItem.setNoiId(noiId);
							}
							tcsInvoiceLineItem.setAction(userAction);
							tcsInvoiceLineItem.setModifiedDate(new Date());
							tcsInvoiceLineItem.setModifiedBy(userEmail);
							tcsInvoiceLineItem.setId(invoiceLineItemId);
							tcsInvoiceLineItem.setPostingDate(documentPostingDate);
							if (!isError) {
								tcsInvoiceLineItems.add(tcsInvoiceLineItem);
								processedCount++;
							}
						}
					} else {
						errorCount++;
						errorList.add(row);
						errorMessages.add("Invoice mismatch id not found in system");
					}
				}
			}

			if (errorCount > 0) {
				ByteArrayOutputStream bytes = generateErrorReport(errorList, errorMessages, tan, tenantId, deductorPan);
				if (bytes.size() != 0) {
					errorFilepath = sendFileToBlobStorage(bytes, tenantId);
				}
			}
			// batch update for invoices
			if (!tcsInvoiceLineItems.isEmpty()) {
				logger.info("started batchupdate method");
				tcsInvoiceLineItemDAO.batchUpdate(tcsInvoiceLineItems);
			}
			if (isCancel) {
				MultipartFile cancelledInvoicesFile = generateCancelledInvoiceExcell(invoiceIds, tan, deductorPan);
				otherFilepath = blob.uploadExcelToBlob(cancelledInvoicesFile);

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
			tcsInvoiceLineItemDAO.invoiceMismatchCountUpdate(year, month, tan, deductorPan);

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
		File someFile = new File("Invoice_RemediationError_Report.xlsx");
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
	 */
	private ByteArrayOutputStream generateErrorReport(List<CsvRow> cslRowList, List<String> listErrorMessages,
			String tan, String tenantId, String deductorPan) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();
		try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
			SXSSFSheet sheet = wb.createSheet("Invoice_Remediation_Error_Report");
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
			row1.getCell(1).setCellStyle(style0);

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

			row1.createCell(37).setCellValue("Invoice Id");
			row1.getCell(37).setCellStyle(style003); // AJ

			row1.createCell(38).setCellValue("Applicable Total Taxable Amount");
			row1.getCell(38).setCellStyle(style3); // AM

			row1.createCell(39).setCellValue("Cess TCS Rate");
			row1.getCell(39).setCellStyle(style0); // AN

			row1.createCell(40).setCellValue("Surcharge TCS Rate");
			row1.getCell(40).setCellStyle(style0); // AO

			row1.createCell(41).setCellValue("Supply Type");
			row1.getCell(41).setCellStyle(style0); // AP

			row1.createCell(42).setCellValue("Collectee Type");
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
				row2.createCell(37).setCellValue(row.getField("Invoice Id"));
				row2.getCell(37).setCellStyle(style5);
				row2.createCell(38).setCellValue(row.getField("Applicable Total Taxable Amount"));
				row2.getCell(38).setCellStyle(style5);
				row2.createCell(39).setCellValue(row.getField("Cess TCS Rate"));
				row2.getCell(39).setCellStyle(style5);
				row2.createCell(40).setCellValue(row.getField("Surcharge TCS Rate"));
				row2.getCell(40).setCellStyle(style5);
				row2.createCell(41).setCellValue(row.getField("Supply Type"));
				row2.getCell(41).setCellStyle(style5);
				row2.createCell(42).setCellValue(row.getField("Collectee Type"));
				row2.getCell(42).setCellStyle(style5);
				index++;
				sequenceNumber++;
			}
			wb.write(out);
		}
		return out;
	}

	/**
	 * 
	 * @param collectorTan
	 * @param tenantId
	 * @param collectorPan
	 * @return
	 */
	public String getErrorReportMsg(String collectorTan, String tenantId, String collectorPan) {
		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(collectorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return "TCS Mismatch Report (Dated: " + date + ")\n Client Name: " + deductorData.getDeductorName() + "\n";
	}

	/**
	 * 
	 * @param cancelList
	 * @return
	 */
	public MultipartFile generateCancelledInvoiceExcell(List<Integer> invoiceIds, String tan, String collectorPan) {

		MultipartFile multipartFile = null;
		String[] invoiceHeadersFile = new String[] { "SourceIdentifier", "SourceFileName", "CollectorCode",
				"CollectorPAN", "CollectorTAN", "CollectorGSTIN", "CollecteeCode", "CollecteePAN", "CollecteeGSTIN",
				"DocumentType", "SupplyType", "DocumentNumber", "DocumentDate", "OriginalDocumentNumber",
				"OriginalDocumentDate", "AccountingDocumentNumber", "PostingDate", "LineNumber", "HSNorSAC",
				"HSNorSACDesc", "Invoic Desc", "SODesc", "GLAccountCode", "GLDesc", "TaxableValue", "IGSTAmount",
				"CGSTAmount", "SGSTAmount", "Discount", "InvoiceValue", "TCSSection", "TCSRate", "TCSAmount",
				"SurchargeRate", "SurchargeAmount", "ITCESSRate", "ITCESSAmount", "UserDefinedField1",
				"UserDefinedField2", "UserDefinedField3", "UserDefinedField4", "UserDefinedField5", "UserDefinedField6",
				"UserDefinedField7", "UserDefinedField8" };
		try {
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			worksheet.setName("Imported Invoice");
			worksheet.autoFitColumns();
			worksheet.autoFitRows();
			worksheet.getCells().importArray(invoiceHeadersFile, 0, 0, false);
			List<TCSInvoiceLineItem> cancelList = tcsInvoiceLineItemDAO.getInvoiceBasedOnIds(invoiceIds, tan,
					collectorPan);
			logger.info("tcs invoice cancel list is: {}", cancelList.size());
			setInvoiceHeaders(cancelList, worksheet);

			worksheet.autoFitColumns();

			// Style for A1 to AS1 headers
			Style style5 = workbook.createStyle();
			style5.setForegroundColor(Color.fromArgb(91, 155, 213));
			style5.setPattern(BackgroundType.SOLID);
			style5.getFont().setBold(true);
			style5.setHorizontalAlignment(TextAlignmentType.CENTER);
			Range headerColorRange1 = worksheet.getCells().createRange("A1:AS1");
			headerColorRange1.setStyle(style5);

			// Creating AutoFilter by giving the cells range
			AutoFilter autoFilter = worksheet.getAutoFilter();
			autoFilter.setRange("A1:AS1");

			File file = new File("Cancelled_Invoices_" + UUID.randomUUID() + ".xlsx");
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
	public void setInvoiceHeaders(List<TCSInvoiceLineItem> cancelList, Worksheet worksheet) throws Exception {
		int rowIndex = 1;
		for (TCSInvoiceLineItem canceledInvoice : cancelList) {
			List<Object> rowData = new ArrayList<>();
			// Source Identifier
			rowData.add(StringUtils.isBlank(canceledInvoice.getSourceIdentifier()) ? StringUtils.EMPTY
					: canceledInvoice.getSourceIdentifier());
			// Source File Name
			rowData.add(StringUtils.isBlank(canceledInvoice.getSourceFileName()) ? StringUtils.EMPTY
					: canceledInvoice.getSourceFileName());
			// Name of the Company code
			rowData.add(StringUtils.isBlank(canceledInvoice.getCollectorCode()) ? StringUtils.EMPTY
					: canceledInvoice.getCollectorCode());
			// Collector Pan
			rowData.add(StringUtils.isBlank(canceledInvoice.getCollectorPan()) ? StringUtils.EMPTY
					: canceledInvoice.getCollectorPan());
			// Collector tan
			rowData.add(StringUtils.isBlank(canceledInvoice.getCollectorTan()) ? StringUtils.EMPTY
					: canceledInvoice.getCollectorTan());
			// Collector Gstin
			rowData.add(StringUtils.isBlank(canceledInvoice.getCollectorGstin()) ? StringUtils.EMPTY
					: canceledInvoice.getCollectorGstin());
			// Collectee Code
			rowData.add(StringUtils.isBlank(canceledInvoice.getCollecteeCode()) ? StringUtils.EMPTY
					: canceledInvoice.getCollecteeCode());
			// Collectee Pan
			rowData.add(StringUtils.isBlank(canceledInvoice.getCollecteePan()) ? StringUtils.EMPTY
					: canceledInvoice.getCollecteePan());
			// Collectee GSTIN
			rowData.add(StringUtils.isBlank(canceledInvoice.getCollecteeGstin()) ? StringUtils.EMPTY
					: canceledInvoice.getCollecteeGstin());
			// Document Type
			rowData.add(StringUtils.isBlank(canceledInvoice.getDocumentType()) ? StringUtils.EMPTY
					: canceledInvoice.getDocumentType());
			// supply Type
			rowData.add(StringUtils.isBlank(canceledInvoice.getSupplyType()) ? StringUtils.EMPTY
					: canceledInvoice.getSupplyType());
			// Document Number
			rowData.add(StringUtils.isBlank(canceledInvoice.getDocumentNumber()) ? StringUtils.EMPTY
					: canceledInvoice.getDocumentNumber());
			// Document Date
			rowData.add(
					canceledInvoice.getDocumentDate() == null ? StringUtils.EMPTY : canceledInvoice.getDocumentDate());
			// Original Document Number
			rowData.add(canceledInvoice.getOriginalDocumentNumber() == null ? StringUtils.EMPTY
					: canceledInvoice.getOriginalDocumentNumber());
			// Original Document Date
			rowData.add(canceledInvoice.getOriginalDocumentDate() == null ? StringUtils.EMPTY
					: canceledInvoice.getOriginalDocumentDate());
			// Accounting Document Number
			rowData.add(canceledInvoice.getAccountingDocumentNumber() == null ? StringUtils.EMPTY
					: canceledInvoice.getAccountingDocumentNumber());
			// Posting Date
			rowData.add(
					canceledInvoice.getPostingDate() == null ? StringUtils.EMPTY : canceledInvoice.getPostingDate());
			// "Line Item Number
			rowData.add(StringUtils.isBlank(canceledInvoice.getDocumentNumber()) ? StringUtils.EMPTY
					: canceledInvoice.getDocumentNumber());
			// HSN/SAC
			rowData.add(StringUtils.isBlank(canceledInvoice.getHsnOrSac()) ? StringUtils.EMPTY
					: canceledInvoice.getHsnOrSac());
			// SAC Description
			rowData.add(StringUtils.isBlank(canceledInvoice.getHsnOrSacDesc()) ? StringUtils.EMPTY
					: canceledInvoice.getHsnOrSacDesc());
			// Service Description Invoice
			rowData.add(StringUtils.isBlank(canceledInvoice.getInvoiceDesc()) ? StringUtils.EMPTY
					: canceledInvoice.getInvoiceDesc());
			// Service Description PO
			rowData.add(
					StringUtils.isBlank(canceledInvoice.getSoDesc()) ? StringUtils.EMPTY : canceledInvoice.getSoDesc());
			// Service Description GL Text
			rowData.add(StringUtils.isBlank(canceledInvoice.getGlAccountCode()) ? StringUtils.EMPTY
					: canceledInvoice.getGlAccountCode());
			// Service Description GL Text
			rowData.add(
					StringUtils.isBlank(canceledInvoice.getGlDesc()) ? StringUtils.EMPTY : canceledInvoice.getGlDesc());
			// Taxable value
			rowData.add(
					canceledInvoice.getTaxableValue() == null ? StringUtils.EMPTY : canceledInvoice.getTaxableValue());
			// IGST Amount
			rowData.add(canceledInvoice.getIgstAmount() == null ? StringUtils.EMPTY : canceledInvoice.getIgstAmount());
			// CGST Amount
			rowData.add(canceledInvoice.getCgstAmount() == null ? StringUtils.EMPTY : canceledInvoice.getCgstAmount());
			// SGST Amount
			rowData.add(canceledInvoice.getSgstAmount() == null ? StringUtils.EMPTY : canceledInvoice.getSgstAmount());
			// Discount
			rowData.add(canceledInvoice.getDiscount() == null ? StringUtils.EMPTY : canceledInvoice.getDiscount());
			// Invoice Value
			rowData.add(
					canceledInvoice.getInvoiceValue() == null ? StringUtils.EMPTY : canceledInvoice.getInvoiceValue());
			// TDS Section
			rowData.add(StringUtils.isBlank(canceledInvoice.getTcsSection()) ? StringUtils.EMPTY
					: canceledInvoice.getTcsSection());
			// TDS Rate
			rowData.add(canceledInvoice.getTcsRate() == null ? StringUtils.EMPTY : canceledInvoice.getTcsRate());
			// TDS Amount
			rowData.add(canceledInvoice.getTcsAmount() == null ? StringUtils.EMPTY : canceledInvoice.getTcsAmount());
			// Surcharge Rate
			rowData.add(canceledInvoice.getSurchargeRate() == null ? StringUtils.EMPTY
					: canceledInvoice.getSurchargeRate());
			// SurchargeAmount
			rowData.add(canceledInvoice.getSurchargeAmount() == null ? StringUtils.EMPTY
					: canceledInvoice.getSurchargeAmount());
			// Itcess Rate
			rowData.add(canceledInvoice.getItcessRate() == null ? StringUtils.EMPTY : canceledInvoice.getItcessRate());
			// Itcess Amount
			rowData.add(
					canceledInvoice.getItcessAmount() == null ? StringUtils.EMPTY : canceledInvoice.getItcessAmount());

			rowData.add(StringUtils.isBlank(canceledInvoice.getUserDefinedField1()) ? StringUtils.EMPTY // User Defined
																										// Field 1
					: canceledInvoice.getUserDefinedField1());
			rowData.add(StringUtils.isBlank(canceledInvoice.getUserDefinedField2()) ? StringUtils.EMPTY // User Defined
																										// Field 2
					: canceledInvoice.getUserDefinedField2());
			rowData.add(StringUtils.isBlank(canceledInvoice.getUserDefinedField3()) ? StringUtils.EMPTY // User Defined
																										// Field 3
					: canceledInvoice.getUserDefinedField3());
			rowData.add(StringUtils.isBlank(canceledInvoice.getUserDefinedField4()) ? StringUtils.EMPTY // User Defined
																										// Field 4
					: canceledInvoice.getUserDefinedField4());
			rowData.add(StringUtils.isBlank(canceledInvoice.getUserDefinedField5()) ? StringUtils.EMPTY // User Defined
																										// Field 5
					: canceledInvoice.getUserDefinedField5());
			rowData.add(StringUtils.isBlank(canceledInvoice.getUserDefinedField6()) ? StringUtils.EMPTY // User Defined
																										// Field 6
					: canceledInvoice.getUserDefinedField6());
			rowData.add(StringUtils.isBlank(canceledInvoice.getUserDefinedField7()) ? StringUtils.EMPTY // User Defined
																										// Field 7
					: canceledInvoice.getUserDefinedField7());
			rowData.add(StringUtils.isBlank(canceledInvoice.getUserDefinedField8()) ? StringUtils.EMPTY // User Defined
																										// Field 8
					: canceledInvoice.getUserDefinedField8());

			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
		}
	}

	/**
	 * 
	 * @param collectorTan
	 * @param year
	 * @param month
	 * @param id
	 * @param type
	 * @param documentPostingDate
	 * @return
	 */
	public Map<String, Object> getLineItemData(String collectorTan, Integer year, Integer month, Integer id,
			String type, Long documentPostingDate) {
		List<TCSInvoiceLineItem> invoiceData = null;
		TCSInvoiceLineItem invoiceLine = null;
		// ProvisionDTO provision = null;
		TcsPaymentDTO payment = null;
		if ("INVOICE".equalsIgnoreCase(type)) {
			invoiceData = tcsInvoiceLineItemDAO.findByYearPanInvoiceId(year, collectorTan, id, documentPostingDate);
		} else if ("ADVANCE".equalsIgnoreCase(type)) {
			List<TcsPaymentDTO> response = tcsPaymentDAO.findByAdvanceId(id);
			if (!response.isEmpty()) {
				payment = response.get(0);
			} else {
				new RuntimeException("Did not find an Advance with the passed in criteria : " + id.toString());
			}
		}
		boolean isMetaDataExists = false;
		Map<String, Object> map = new HashMap<>();
		Map<String, Integer> remittanceMap = new HashMap<>();
		// InvoiceMetaNr invoiceMetaNrObj = new InvoiceMetaNr();
		// InvoiceMetaNrDTO invoiceMetaNrDTO = new InvoiceMetaNrDTO();
		List<NORemittanceDTO> norList = new ArrayList<>();
		String pan = null;
		String deducteeName = null;
		Integer lineItemNumber = null;
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
			deducteeName = invoiceLine.getCollecteeName();
			pan = invoiceLine.getCollecteePan();
			lineItemNumber = invoiceLine.getLineNumber();
			lineItemDate = invoiceLine.getDocumentDate();
			serviceDescription = invoiceLine.getInvoiceDesc();
			serviceDescriptionGl = invoiceLine.getGlDesc();
			serviceDescriptionPo = invoiceLine.getSoDesc();
			serviceDescriptionInvoice = invoiceLine.getInvoiceDesc();
			sacDecription = invoiceLine.getHsnOrSacDesc();
			// lineItemAmount = invoiceLine.getInvoiceAmount();
			// deducteeTin = invoiceLine.getDeducteeTin();
			clientSection = invoiceLine.getActualTcsSection();
		} else if (payment != null) {
			deducteeName = payment.getCollecteeName();
			pan = payment.getCollecteePan();
			lineItemDate = payment.getDocumentDate();

			clientSection = payment.getActualTcsSection();
		}

		if (StringUtils.isNotBlank(deducteeName) && StringUtils.isNotBlank(deducteeTin)) {

			map.put("natureOfRemittance", norList);
			map.put("deducteeName", deducteeName);
			map.put("pan", pan);
			map.put("lineItemNumber", lineItemNumber);
			map.put("lineItemDate", lineItemDate);
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
		return map;
	}

	/**
	 * prepares the exempted report
	 * 
	 * @param tan
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
	public ByteArrayInputStream asyncTCExportExemptionReport(String tan, String tenantId, String deductorPan, int year,
			int month, String userName) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		return exportExemtiontionReport(tan, tenantId, deductorPan, year, month, userName);
	}

	/**
	 * async method to fetch the exempted invoice records and generate a excel file
	 * 
	 * @param tan
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
	public ByteArrayInputStream exportExemtiontionReport(String collecteeTan, String tenantId, String collectorPan,
			int year, int month, String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// saving the record in batch upload
		TCSBatchUpload batchUpload = saveOrUpdateExemptionReport(collecteeTan, tenantId, year, null, 0L,
				UploadTypes.TCS_EXEMPTED_REPORT.name(), "Processing", month, userName, null, 0);

		// TODO here the DB call will happen to get the data
		List<TCSInvoiceLineItem> invoiceList = tcsInvoiceLineItemDAO.getInvoiceByExempt(year, month, collecteeTan,
				collectorPan);

		List<TcsPaymentDTO> paymentList = tcsInvoiceLineItemDAO.getPaymentByExempt(year, month, collecteeTan,
				collectorPan);

		Integer invoiceCount = invoiceList.isEmpty() ? 0 : invoiceList.size();
		Integer paymentCount = paymentList.isEmpty() ? 0 : paymentList.size();

		Integer processedCount = invoiceCount + paymentCount;

		// setting the excel file headers
		String[] exemptHeadersFile = new String[] { "SourceIdentifier", "SourceFileName", "CollectorCode",
				"CollectorPAN", "CollectorTAN", "CollectorGSTIN", "CollecteeCode", "CollecteePAN", "CollecteeGSTIN",
				"DocumentType", "SupplyType", "DocumentNumber", "DocumentDate", "OriginalDocumentNumber",
				"OriginalDocumentDate", "AccountingDocumentNumber", "PostingDate", "LineNumber", "HSNorSAC",
				"InvoiceDesc", "SODesc", "GLAccountCode", "GLDesc", "TaxableValue ", "IGSTAmount", "CGSTAmount",
				"SGSTAmount", "Discount", "InvoiceValue", "TCSSection", "TCSRate", "TCSAmount", "SurchargeRate",
				"SurchargeAmount", "ITCESSRate", "ITCESSAmount", "Userdefinedfield1", "Userdefinedfield2",
				"Userdefinedfield3", "Userdefinedfield4", "Userdefinedfield5", "Userdefinedfield6", "Userdefinedfield7",
				"Userdefinedfield8" };
		try {
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			worksheet.setName("Exempt Trans.-Return Filing-Inv");
			worksheet.autoFitColumns();
			worksheet.autoFitRows();
			worksheet.getCells().importArray(exemptHeadersFile, 5, 0, false);
			worksheet.setGridlinesVisible(false);
			worksheet.autoFitColumns();

			// creating style
			Style style2 = workbook.createStyle();
			style2.setPattern(BackgroundType.SOLID);
			style2.getFont().setBold(true);
			style2.getFont().setSize(12);
			style2.setHorizontalAlignment(TextAlignmentType.GENERAL);

			// merging cells to write the header content
			worksheet.getCells().merge(0, 0, 4, 20);
			worksheet.getCells().get("A1").setValue(getErrorReportMsg(collecteeTan, tenantId, collectorPan));
			worksheet.getCells().get("A1").setStyle(style2);

			// Style for B6 to AR6 headers
			Style style1 = workbook.createStyle();
			style1.setForegroundColor(Color.fromArgb(255, 169, 94));
			style1.setPattern(BackgroundType.SOLID);
			style1.getFont().setBold(true);
			style1.setHorizontalAlignment(TextAlignmentType.CENTER);
			style1 = setBorderToCells(style1);
			Range headerColorRange1 = worksheet.getCells().createRange("B6:AR6");
			headerColorRange1.setStyle(style1);
			headerColorRange1.setColumnWidth(18);
			headerColorRange1.setRowHeight(14);
			Integer cellRange = processedCount + 6;

			Style style4 = workbook.createStyle();
			style4 = setBorderToCells(style4);
			Range headerColorRange2 = worksheet.getCells().createRange("A" + cellRange + ":" + "AR" + cellRange);
			headerColorRange2.setStyle(style4);

			Style style3 = workbook.createStyle();
			style3.setForegroundColor(Color.fromArgb(0, 0, 0));
			style3.setPattern(BackgroundType.SOLID);
			style3.getFont().setBold(true);
			style3.getFont().setColor(Color.fromArgb(255, 255, 255));
			style3.setHorizontalAlignment(TextAlignmentType.CENTER);
			worksheet.getCells().get("A6").setStyle(style3);

			// Creating AutoFilter by giving the cells range
			AutoFilter autoFilter = worksheet.getAutoFilter();
			autoFilter.setRange("A6:AR6");

			// setting value to the excel headers
			setValueToExemptHeaders(invoiceList, worksheet, paymentList);

			File createdFile = new File("Exempt Transaction Report_" + UUID.randomUUID() + ".xlsx");
			OutputStream outputStream = new FileOutputStream(createdFile);
			workbook.save(outputStream, SaveFormat.XLSX);

			// converting to multipart file
			FileInputStream input = new FileInputStream(createdFile);
			MultipartFile multipartFile = new MockMultipartFile("file", createdFile.getName(), "text/plain",
					IOUtils.toByteArray(input));

			batchUpload = saveOrUpdateExemptionReport(collecteeTan, tenantId, year, multipartFile, 0L,
					UploadTypes.TCS_EXEMPTED_REPORT.name(), "Processed", month, userName, batchUpload.getId(),
					processedCount);

		} catch (Exception exception) {
			logger.info("Exception occured while generating excell report for cancelled invoices");
		}

		return null;
	}

	/**
	 * to set the borders to the excel cells
	 * 
	 * @param style
	 * @return
	 */
	public Style setBorderToCells(Style style) {
		Style newStyle = style;
		newStyle.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
		newStyle.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
		newStyle.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
		newStyle.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());
		return newStyle;
	}

	/**
	 * to insert and update batch upload record for exempt report
	 * 
	 * @param collectorTan
	 * @param tenantId
	 * @param assessmentYear
	 * @param file
	 * @param noOfRows
	 * @param uploadType
	 * @param status
	 * @param month
	 * @param userName
	 * @param batchId
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws StorageException
	 */
	protected TCSBatchUpload saveOrUpdateExemptionReport(String collectorTan, String tenantId, int assessmentYear,
			MultipartFile file, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId, Integer processedCout)
			throws FileNotFoundException, IOException, URISyntaxException, InvalidKeyException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		String path = null;
		TCSBatchUpload batchUpload = new TCSBatchUpload();
		if (file != null) {
			batchUpload.setFileName(file.getOriginalFilename());
			path = blob.uploadExcelToBlob(file, tenantId);
			logger.info("Exempted report {} completed for : {}", uploadType, userName);
		} else {
			logger.info("Exempted report {} started for : {}", uploadType, userName);
		}
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setCollectorMasterTan(collectorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setId(batchId);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		batchUpload.setCreatedDate(new Date());
		batchUpload.setCreatedBy(userName);
		List<TCSBatchUpload> response = null;
		if (batchId != null) {
			response = tcsBatchUploadDAO.findById(assessmentYear, collectorTan, uploadType, batchId);
		}
		if (response != null && !response.isEmpty()) {
			batchUpload = response.get(0);
			if (file != null) {
				batchUpload.setModifiedDate(new Date());
				batchUpload.setProcessEndTime(new Date());
				batchUpload.setFilePath(path);
				batchUpload.setFileName(file.getOriginalFilename());
				batchUpload.setStatus(status);
				batchUpload.setRowsCount((long) processedCout);
				batchUpload.setProcessed(processedCout);
			} else {
				batchUpload.setModifiedDate(null);
				batchUpload.setProcessStartTime(new Date());
				batchUpload.setProcessEndTime(new Date());
				batchUpload.setRowsCount(0l);
			}
			batchUpload.setModifiedBy(userName);
			return tcsBatchUploadDAO.update(batchUpload);
		} else {
			return tcsBatchUploadDAO.save(batchUpload);
		}
	}

	public void setValueToExemptHeaders(List<TCSInvoiceLineItem> exemptedList, Worksheet worksheet,
			List<TcsPaymentDTO> paymentList) throws Exception {
		int rowIndex = 6;
		for (TCSInvoiceLineItem invoice : exemptedList) {
			List<Object> rowData = new ArrayList<>();

			rowData.add(StringUtils.isBlank(invoice.getSourceIdentifier()) ? StringUtils.EMPTY // SourceIdentifier
					: invoice.getSourceIdentifier());

			rowData.add(StringUtils.isBlank(invoice.getSourceFileName()) ? StringUtils.EMPTY // SourceFileName
					: invoice.getSourceFileName());

			rowData.add(StringUtils.isBlank(invoice.getCollectorCode()) ? StringUtils.EMPTY // CollectorCode
					: invoice.getCollectorCode());

			rowData.add(StringUtils.isBlank(invoice.getCollectorPan()) ? StringUtils.EMPTY // CollectorPAN
					: invoice.getCollectorPan());

			rowData.add(StringUtils.isBlank(invoice.getCollectorTan()) ? StringUtils.EMPTY // CollectorTAN
					: invoice.getCollectorTan());

			rowData.add(StringUtils.isBlank(invoice.getCollectorGstin()) ? StringUtils.EMPTY // CollectorGSTIN
					: invoice.getCollectorGstin());

			rowData.add(StringUtils.isBlank(invoice.getCollecteeCode()) ? StringUtils.EMPTY // CollecteeCode
					: invoice.getCollecteeCode());

			rowData.add(StringUtils.isBlank(invoice.getCollecteePan()) ? StringUtils.EMPTY // CollecteePAN
					: invoice.getCollecteePan());

			rowData.add(StringUtils.isBlank(invoice.getCollecteeGstin()) ? StringUtils.EMPTY // CollecteeGSTIN
					: invoice.getCollecteeGstin());

			rowData.add(StringUtils.isBlank(invoice.getDocumentType()) ? StringUtils.EMPTY // DocumentType
					: invoice.getDocumentType());

			rowData.add(StringUtils.isBlank(invoice.getSupplyType()) ? StringUtils.EMPTY // SupplyType
					: invoice.getSupplyType());

			rowData.add(StringUtils.isBlank(invoice.getDocumentNumber()) ? StringUtils.EMPTY // DocumentNumber
					: invoice.getDocumentNumber());

			rowData.add(invoice.getDocumentDate() == null ? StringUtils.EMPTY // DocumentDate
					: invoice.getDocumentDate());

			rowData.add(StringUtils.isBlank(invoice.getOriginalDocumentNumber()) ? StringUtils.EMPTY // OriginalDocumentNumber
					: invoice.getOriginalDocumentNumber());

			rowData.add(invoice.getOriginalDocumentDate() == null ? StringUtils.EMPTY // OriginalDocumentDate
					: invoice.getOriginalDocumentDate());

			rowData.add(invoice.getAccountingDocumentNumber() == null ? StringUtils.EMPTY // AccountingDocumentNumber
					: invoice.getAccountingDocumentNumber());

			rowData.add(invoice.getPostingDate() == null ? StringUtils.EMPTY // PostingDate
					: invoice.getPostingDate());

			rowData.add(invoice.getLineNumber() == null ? StringUtils.EMPTY // LineNumber
					: invoice.getLineNumber());

			rowData.add(StringUtils.isBlank(invoice.getHsnOrSac()) ? StringUtils.EMPTY // HSNorSAC
					: invoice.getHsnOrSac());

			rowData.add(StringUtils.isBlank(invoice.getInvoiceDesc()) ? StringUtils.EMPTY // InvoiceDesc
					: invoice.getInvoiceDesc());

			rowData.add(StringUtils.isBlank(invoice.getSoDesc()) ? StringUtils.EMPTY // SODesc
					: invoice.getSoDesc());

			rowData.add(StringUtils.isBlank(invoice.getGlAccountCode()) ? StringUtils.EMPTY // GLAccountCode
					: invoice.getGlAccountCode());

			rowData.add(StringUtils.isBlank(invoice.getGlDesc()) ? StringUtils.EMPTY // GLDesc
					: invoice.getGlDesc());

			rowData.add(invoice.getTaxableValue() == null ? StringUtils.EMPTY // TaxableValue
					: invoice.getTaxableValue());

			rowData.add(invoice.getIgstAmount() == null ? StringUtils.EMPTY // IGSTAmount
					: invoice.getIgstAmount());

			rowData.add(invoice.getCgstAmount() == null ? StringUtils.EMPTY // CGSTAmount
					: invoice.getCgstAmount());

			rowData.add(invoice.getSgstAmount() == null ? StringUtils.EMPTY // SGSTAmount
					: invoice.getSgstAmount());

			rowData.add(invoice.getDiscount() == null ? StringUtils.EMPTY // Discount
					: invoice.getDiscount());

			rowData.add(invoice.getInvoiceValue() == null ? StringUtils.EMPTY // InvoiceValue
					: invoice.getInvoiceValue());

			rowData.add(StringUtils.isBlank(invoice.getTcsSection()) ? StringUtils.EMPTY // TCSSection
					: invoice.getTcsSection());

			rowData.add(invoice.getTcsRate() == null ? StringUtils.EMPTY // TCSRate
					: invoice.getTcsRate());

			rowData.add(invoice.getTcsAmount() == null ? StringUtils.EMPTY // TCSAmount
					: invoice.getTcsAmount());

			rowData.add(invoice.getSurchargeRate() == null ? StringUtils.EMPTY // SurchargeRate
					: invoice.getSurchargeRate());

			rowData.add(invoice.getSurchargeAmount() == null ? StringUtils.EMPTY // SurchargeAmount
					: invoice.getSurchargeAmount());

			rowData.add(invoice.getItcessRate() == null ? StringUtils.EMPTY // ITCESSRate
					: invoice.getItcessRate());

			rowData.add(invoice.getItcessAmount() == null ? StringUtils.EMPTY // ITCESSAmount
					: invoice.getItcessAmount());

			rowData.add(StringUtils.isBlank(invoice.getUserDefinedField1()) ? StringUtils.EMPTY // Userdefinedfield1
					: invoice.getUserDefinedField1());

			rowData.add(StringUtils.isBlank(invoice.getUserDefinedField2()) ? StringUtils.EMPTY // Userdefinedfield2
					: invoice.getUserDefinedField2());

			rowData.add(StringUtils.isBlank(invoice.getUserDefinedField3()) ? StringUtils.EMPTY // Userdefinedfield3
					: invoice.getUserDefinedField3());

			rowData.add(StringUtils.isBlank(invoice.getUserDefinedField4()) ? StringUtils.EMPTY // Userdefinedfield4
					: invoice.getUserDefinedField4());

			rowData.add(StringUtils.isBlank(invoice.getUserDefinedField5()) ? StringUtils.EMPTY // Userdefinedfield5
					: invoice.getUserDefinedField5());

			rowData.add(StringUtils.isBlank(invoice.getUserDefinedField6()) ? StringUtils.EMPTY // Userdefinedfield6
					: invoice.getUserDefinedField6());

			rowData.add(StringUtils.isBlank(invoice.getUserDefinedField7()) ? StringUtils.EMPTY // Userdefinedfield7
					: invoice.getUserDefinedField7());

			rowData.add(StringUtils.isBlank(invoice.getUserDefinedField8()) ? StringUtils.EMPTY // Userdefinedfield8
					: invoice.getUserDefinedField8());

			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
		}

		for (TcsPaymentDTO payment : paymentList) {
			List<Object> rowData = new ArrayList<>();

			rowData.add(StringUtils.isBlank(payment.getSourceIdentifier()) ? StringUtils.EMPTY // SourceIdentifier
					: payment.getSourceIdentifier());

			rowData.add(StringUtils.isBlank(payment.getSourceFileName()) ? StringUtils.EMPTY // SourceFileName
					: payment.getSourceFileName());

			rowData.add(StringUtils.isBlank(payment.getCollectorCode()) ? StringUtils.EMPTY // CollectorCode
					: payment.getCollectorCode());

			rowData.add(StringUtils.isBlank(payment.getCollectorPan()) ? StringUtils.EMPTY // CollectorPAN
					: payment.getCollectorPan());

			rowData.add(StringUtils.isBlank(payment.getCollectorTan()) ? StringUtils.EMPTY // CollectorTAN
					: payment.getCollectorTan());

			rowData.add(StringUtils.isBlank(payment.getCollectorGstin()) ? StringUtils.EMPTY // CollectorGSTIN
					: payment.getCollectorGstin());

			rowData.add(StringUtils.isBlank(payment.getCollecteeCode()) ? StringUtils.EMPTY // CollecteeCode
					: payment.getCollecteeCode());

			rowData.add(StringUtils.isBlank(payment.getCollecteePan()) ? StringUtils.EMPTY // CollecteePAN
					: payment.getCollecteePan());

			rowData.add(StringUtils.isBlank(payment.getCollecteeGstin()) ? StringUtils.EMPTY // CollecteeGSTIN
					: payment.getCollecteeGstin());

			rowData.add(StringUtils.isBlank(payment.getDocumentType()) ? StringUtils.EMPTY // DocumentType
					: payment.getDocumentType());

			rowData.add(StringUtils.isBlank("") ? StringUtils.EMPTY // SupplyType
					: "");

			rowData.add(StringUtils.isBlank(payment.getDocumentNumber()) ? StringUtils.EMPTY // DocumentNumber
					: payment.getDocumentNumber());

			rowData.add(payment.getDocumentDate() == null ? StringUtils.EMPTY // DocumentDate
					: payment.getDocumentDate());

			rowData.add(StringUtils.isBlank(payment.getDocumentNumber()) ? StringUtils.EMPTY // OriginalDocumentNumber
					: payment.getDocumentNumber());

			rowData.add(payment.getDocumentDate() == null ? StringUtils.EMPTY // OriginalDocumentDate
					: payment.getDocumentDate());

			rowData.add(payment.getDocumentNumber() == null ? StringUtils.EMPTY // AccountingDocumentNumber
					: payment.getDocumentNumber());

			rowData.add(payment.getPostingDate() == null ? StringUtils.EMPTY // PostingDate
					: payment.getPostingDate());

			rowData.add(payment.getLineNumber() == null ? StringUtils.EMPTY // LineNumber
					: payment.getLineNumber());

			// TODO
			rowData.add(StringUtils.isBlank("") ? StringUtils.EMPTY // HSNorSAC
					: "");

			rowData.add(StringUtils.isBlank(payment.getPaymentDesc()) ? StringUtils.EMPTY // InvoiceDesc
					: payment.getPaymentDesc());

			// TODO
			rowData.add(StringUtils.isBlank("") ? StringUtils.EMPTY // SODesc
					: "");

			rowData.add(StringUtils.isBlank(payment.getGlAccountCode()) ? StringUtils.EMPTY // GLAccountCode
					: payment.getGlAccountCode());

			rowData.add(StringUtils.isBlank(payment.getGlDesc()) ? StringUtils.EMPTY // GLDesc
					: payment.getGlDesc());

			rowData.add(payment.getAmount() == null ? StringUtils.EMPTY // TaxableValue
					: payment.getAmount());

			rowData.add(""); // IGSTAmount

			rowData.add(""); // CGSTAmount

			rowData.add(""); // SGSTAmount

			rowData.add(""); // Discount

			rowData.add(payment.getAmount() == null ? StringUtils.EMPTY // InvoiceValue
					: payment.getAmount());

			rowData.add(StringUtils.isBlank(payment.getTcsSection()) ? StringUtils.EMPTY // TCSSection
					: payment.getTcsSection());

			rowData.add(payment.getTcsRate() == null ? StringUtils.EMPTY // TCSRate
					: payment.getTcsRate());

			rowData.add(payment.getTcsAmount() == null ? StringUtils.EMPTY // TCSAmount
					: payment.getTcsAmount());

			rowData.add(payment.getSurchargeRate() == null ? StringUtils.EMPTY // SurchargeRate
					: payment.getSurchargeRate());

			rowData.add(payment.getSurchargeAmount() == null ? StringUtils.EMPTY // SurchargeAmount
					: payment.getSurchargeAmount());

			rowData.add(payment.getItcessRate() == null ? StringUtils.EMPTY // ITCESSRate
					: payment.getItcessRate());

			rowData.add(payment.getItcessAmount() == null ? StringUtils.EMPTY // ITCESSAmount
					: payment.getItcessAmount());

			rowData.add(StringUtils.isBlank(payment.getUserDefinedField1()) ? StringUtils.EMPTY // Userdefinedfield1
					: payment.getUserDefinedField1());

			rowData.add(StringUtils.isBlank(payment.getUserDefinedField2()) ? StringUtils.EMPTY // Userdefinedfield2
					: payment.getUserDefinedField2());

			rowData.add(StringUtils.isBlank(payment.getUserDefinedField3()) ? StringUtils.EMPTY // Userdefinedfield3
					: payment.getUserDefinedField3());

			rowData.add(StringUtils.isBlank(payment.getUserDefinedField4()) ? StringUtils.EMPTY // Userdefinedfield4
					: payment.getUserDefinedField4());

			rowData.add(StringUtils.isBlank(payment.getUserDefinedField5()) ? StringUtils.EMPTY // Userdefinedfield5
					: payment.getUserDefinedField5());

			rowData.add(StringUtils.isBlank(payment.getUserDefinedField6()) ? StringUtils.EMPTY // Userdefinedfield6
					: payment.getUserDefinedField6());

			// TODO
			rowData.add(StringUtils.isBlank("") ? StringUtils.EMPTY // Userdefinedfield7
					: "");

			// TODO
			rowData.add(StringUtils.isBlank("") ? StringUtils.EMPTY // Userdefinedfield8
					: "");

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
	 * @param type
	 * @param tan
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public String getPdfActivityTrackerResult(Integer assessmentYear, String type, String tan, String startDate,
			String endDate) {
		long res = tcsInvoiceLineItemDAO.getPdfTransactionStatus(assessmentYear, type, tan, startDate, endDate);
		if (res == 0) {
			return ActivityTrackerStatus.NORECORDS.name();
		} else {
			return ActivityTrackerStatus.VALIDATED.name();
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

	public ByteArrayInputStream generateExcel(int csvLinesSize, CsvContainer csv, String tan, String tenantId,
			String deductorPan) throws IOException {
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

			String value1 = "NOTE: If you select 'Accept' under 'Action' (Column AF), then providing information under column AH to AJ is not mandatory.";
			// String value2 = "NOTE: If you select 'Reject' under 'Action' (Column AF),
			// then providing information under 'Reason' (Column AG) is mandatory column AH
			// to AJ is not mandatory.";
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

			row1.createCell(36).setCellValue("Invoice Id");
			row1.getCell(36).setCellStyle(style003); // AJ

			row1.createCell(38).setCellValue("Applicable Total Taxable Amount");
			row1.getCell(38).setCellStyle(style3); // AM

			row1.createCell(39).setCellValue("Cess TCS Rate");
			row1.getCell(39).setCellStyle(style0); // AN

			row1.createCell(40).setCellValue("Surcharge TCS Rate");
			row1.getCell(40).setCellStyle(style0); // AO

			row1.createCell(41).setCellValue("Supply Type");
			row1.getCell(41).setCellStyle(style0); // AP

			row1.createCell(42).setCellValue("Collectee Type");
			row1.getCell(42).setCellStyle(style0); // AQ

			// Auto Filter Option
			sheet.setAutoFilter(new CellRangeAddress(5, 42, 0, 42));
			sheet.createFreezePane(0, 1);

			// sheet.createFreezePane(colSplit, rowSplit, leftmostColumn, topRow)-this
			// method is used for scrolling rows and column vertically and horizontally
			// sheet.createFreezePane(4, 5, 5, 5);

			// long size =
			// invoiceLineItemRepository.getAllInvoiceMismatchesCount(tan.trim(), year,
			// month);
			long size = csvLinesSize;
			// tcsInvoiceLineItemDAO.getAllInvoiceMismatchesCount(tan.trim(),
			// year, month);
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
				createCell(style5, row2, 38, row.getField("applicable_total_taxable_amount"));
				createCell(style5, row2, 39, row.getField("itcess_rate"));
				createCell(style5, row2, 40, row.getField("surcharge_rate"));
				createCell(style5, row2, 41, row.getField("supply_type"));
				createCell(style5, row2, 42, row.getField("collectee_type"));

			}

			wb.write(out);

			return new ByteArrayInputStream(out.toByteArray());
		}
	}

	private void createCell(XSSFCellStyle style, SXSSFRow row, int cellNumber, String value) {
		row.createCell(cellNumber).setCellValue(StringUtils.isBlank(value) == true ? StringUtils.EMPTY : value);
		row.getCell(cellNumber).setCellStyle(style); // K
	}

	private void setCellColorAndBoarder(DefaultIndexedColorMap defaultIndexedColorMap, XSSFCellStyle style, Integer r,
			Integer g, Integer b) {
		style.setFillForegroundColor(new XSSFColor(new java.awt.Color(r, g, b), defaultIndexedColorMap));
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);

	}

	public void awaitTerminationAfterShutdown(ExecutorService threadPool) {
		threadPool.shutdown();
		try {
			if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
				threadPool.shutdownNow();
			}
		} catch (InterruptedException ex) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

}
