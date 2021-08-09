package com.ey.in.tds.ingestion.tcs.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.aspose.cells.BackgroundType;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.Form26ASFileReportDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.Form26ASFinalResultFileReportDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.GstPrFileReportDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.Tcs26AsInputDto;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsGstPurchaseRegister;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsGstPurchaseRegisterReportDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsReciverLedger;
import com.ey.in.tds.common.ingestion.response.dto.From26AsInput;
import com.ey.in.tds.common.ingestion.response.dto.From26AsJson;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.ingestion.service.tdsmismatch.TcsMismatchService;
import com.ey.in.tds.ingestion.tcs.dao.TcsGstPurchaseRegisterDAO;
import com.ey.in.tds.ingestion.tcs.dao.TcsReciverLedgerDAO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

/**
 * 
 * @author vamsir
 *
 */
@Service
public class TCSPurchaseRegisterService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TcsGstPurchaseRegisterDAO tcsGstPurchaseRegisterDAO;

	@Autowired
	private TcsReciverLedgerDAO tcsReciverLedgerDAO;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private TcsMismatchService tcsMismatchService;

	@Value("${databricks.key}")
	private String dataBricksKey;

	@Value("${application.url}")
	private String applicationUrl;

	public static String[] gstPrHeaderNames = new String[] { "ReturnPeriod", "DocumentType", "SupplyType",
			"DocumentNumber", "DocumentDate", "SupplierGSTIN", "PAN", "SupplierName", "HSN or SAC", "Quantity",
			"TaxableValue", "IntegratedTaxAmount", "CentralTaxAmount", "StateUTTaxAmount", "TotalAmount",
			"InvoiceValue", "PurchaseVoucherDate", "PaymentDate", "PurchaseVoucherNumber", "Extractor Month",
			"PVN Match with TCS Ledger", "TCS Amount as per TCS Ledger", "PVN Match" };

	public static String[] tcsRlHeaderNames = new String[] { "G/L Account", "Document Header Text", "Document Number",
			"Customer", "Vendor", "Posting Date", "Document Date", "Reference", "Business place", "Order",
			"Document type", "Business Area", "Profit Center", "Amount in local currency", "Local Currency", "Text",
			"Offsetting Account", "Name of offsetting account", "Cost Center", "Plant", "User Name",
			"Offsett.account type", "Material", "Invoice reference", "Collective invoice", "Year/month",
			"Billing Document", "PurchaseVoucherNumber", "Match" };

	public static String[] tcsGstPrAndRlHeaderNames = new String[] { "ReturnPeriod", "DocumentType", "SupplyType",
			"DocumentNumber", "DocumentDate", "SupplierGSTIN", "PAN", "SupplierName", "HSN or SAC", "Quantity",
			"TaxableValue", "IntegratedTaxAmount", "CentralTaxAmount", "StateUTTaxAmount", "TotalAmount",
			"InvoiceValue", "PurchaseVoucherDate", "PaymentDate", "PurchaseVoucherNumber", "Extractor Month",
			"PVN Match with TCS Ledger", "TCS Amount as per TCS Ledger", "PVN Match", "Payment Voucher Number",
			"TCS Amount" };

	public static String[] form26AsHeaders = new String[] { "S.No.", "Name as per 26AS", "TAN", "Transaction Value",
			"Tax Collected", "PAN", "NAME as per TRACES" };

	public static String[] gstPrHeaders = new String[] { "Name of supplier", "Sales turnover",
			"Tax paid in Q3 - TCS Receivable ledger", "Name as per TRACES" };

	public static String[] form26ASFinalResultHeaders = new String[] { "Result", "Supplier Name (26AS)",
			"Supplier Name (GSTPR)", "Section Code (26AS)", "Document No. (GSTPR)", "Purchase Voucher No. (GSTPR)",
			"Transaction Date (26AS)", "Transaction Date (GSTPR)", "Transaction Amount (26AS)",
			"Transaction Amount (GSTPR)", "Taxable Amount (GSTPR)", "TCS (26AS)", "TCS (TCSRL)", "Case" };

	/**
	 * 
	 * @param collectorTan
	 * @param collectorPan
	 * @param batchId
	 * @param assessmentYear
	 * @param userName
	 * @param tenantId
	 * @param uploadType
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	@Async
	public TCSBatchUpload saveGstPrAndTcsRlData(String collectorTan, String collectorPan, Integer batchId,
			Integer assessmentYear, String userName, String tenantId, String uploadType)
			throws IOException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		TCSBatchUpload batchUpload = tcsMismatchService.getTCSBatchUpload(batchId);
		File csvFile = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getOtherFileUrl());

		batchUpload.setMismatchCount(0L);
		batchUpload.setModifiedBy(userName);
		batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
		Integer processedRecords = 0;

		CsvReader csvReader = new CsvReader();
		csvReader.setContainsHeader(true);
		CsvContainer csv = csvReader.read(csvFile, StandardCharsets.UTF_8);
		int rowCount = csv.getRowCount();
		logger.info("rowCount :{}", rowCount);
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setGroupingSeparator(',');
		symbols.setDecimalSeparator('.');
		String pattern = "#,##0.0#";
		DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
		decimalFormat.setParseBigDecimal(true);
		// reading csv file and processing records
		try {
			if ("PURCHASE_REGISTER_BATCH".equalsIgnoreCase(uploadType)) {
				for (CsvRow tcsGstPrRow : csv.getRows()) {
					if (StringUtils.isNotBlank(StringUtils.join(tcsGstPrRow.getFields(), ", ").replaceAll(",", ""))) {
						TcsGstPurchaseRegister tcsPr = new TcsGstPurchaseRegister();
						tcsPr.setActive(true);
						tcsPr.setCreatedBy(userName);
						tcsPr.setModifiedBy(userName);
						tcsPr.setCreatedDate(new Timestamp(new Date().getTime()));
						tcsPr.setModifiedDate(new Timestamp(new Date().getTime()));
						tcsPr.setCollectorPan(collectorPan);
						tcsPr.setCollectorTan(collectorTan);
						tcsPr.setAssessmentYear(assessmentYear);
						if (StringUtils.isNotBlank(tcsGstPrRow.getField("DocumentDate"))) {
							String dateFormat = tcsGstPrRow.getField("DocumentDate").replace("/", "-");
							Date documentDate = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT).parse(dateFormat);
							tcsPr.setDocumentDate(documentDate);
						}
						tcsPr.setDocumentNumber(StringUtils.isNotBlank(tcsGstPrRow.getField("DocumentNumber"))
								? tcsGstPrRow.getField("DocumentNumber")
								: StringUtils.EMPTY);
						tcsPr.setDocumentType(StringUtils.isNotBlank(tcsGstPrRow.getField("DocumentType"))
								? tcsGstPrRow.getField("DocumentType")
								: StringUtils.EMPTY);
						tcsPr.setExtractorMonth(StringUtils.isNotBlank(tcsGstPrRow.getField("Extractor Month"))
								? tcsGstPrRow.getField("Extractor Month")
								: StringUtils.EMPTY);
						tcsPr.setHsnOrSac(StringUtils.isNotBlank(tcsGstPrRow.getField("HSN or SAC"))
								? tcsGstPrRow.getField("HSN or SAC")
								: StringUtils.EMPTY);
						// BigDecimal amounts
						String integratedAmount = tcsGstPrRow.getField("IntegratedTaxAmount").replaceAll("[^0-9.]", "")
								.trim();
						if (StringUtils.isNotBlank(integratedAmount)) {
							tcsPr.setIntegratedTaxamount((BigDecimal) decimalFormat.parse(integratedAmount));
						}
						String invoiceValue = tcsGstPrRow.getField("Invoice Value (for recon)")
								.replaceAll("[^0-9.]", "").trim();
						if (StringUtils.isNotBlank(invoiceValue)) {
							tcsPr.setInvoiceValue((BigDecimal) decimalFormat.parse(invoiceValue));
						}
						String taxableValue = tcsGstPrRow.getField("TaxableValue").replaceAll("[^0-9.]", "").trim();
						if (StringUtils.isNotBlank(taxableValue)) {
							tcsPr.setTaxableValue((BigDecimal) decimalFormat.parse(taxableValue));
						}
						String stateUTTaxAmount = tcsGstPrRow.getField("StateUTTaxAmount").replaceAll("[^0-9.]", "")
								.trim();
						if (StringUtils.isNotBlank(stateUTTaxAmount)) {
							tcsPr.setStateUttaxAmount((BigDecimal) decimalFormat.parse(stateUTTaxAmount));
						}
						String centralTaxAmount = tcsGstPrRow.getField("CentralTaxAmount").replaceAll("[^0-9.]", "")
								.trim();
						if (StringUtils.isNotBlank(centralTaxAmount)) {
							tcsPr.setCentralTaxAmount((BigDecimal) decimalFormat.parse(centralTaxAmount));
						}
						tcsPr.setPan(StringUtils.isNotBlank(tcsGstPrRow.getField("PAN")) ? tcsGstPrRow.getField("PAN")
								: StringUtils.EMPTY);
						if (StringUtils.isNotBlank(tcsGstPrRow.getField("PaymentDate"))) {
							String dateFormat = tcsGstPrRow.getField("PaymentDate").replace("/", "-");
							Date paymentDate = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT).parse(dateFormat);
							tcsPr.setPaymentDate(paymentDate);
						}
						tcsPr.setPrIsMatched(false);
						if (StringUtils.isNotBlank(tcsGstPrRow.getField("PurchaseVoucherDate"))) {
							String dateFormat = tcsGstPrRow.getField("PurchaseVoucherDate").replace("/", "-");
							Date purchsedDate = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT).parse(dateFormat);
							tcsPr.setPurchaseVoucherDate(purchsedDate);
						}
						tcsPr.setPurchaseVoucherNumber(
								StringUtils.isNotBlank(tcsGstPrRow.getField("PurchaseVoucherNumber"))
										? tcsGstPrRow.getField("PurchaseVoucherNumber")
										: StringUtils.EMPTY);
						tcsPr.setPvnMatch(Boolean.valueOf(StringUtils.isNotBlank(tcsGstPrRow.getField("PVN Match"))
								? tcsGstPrRow.getField("PVN Match")
								: StringUtils.EMPTY));
						tcsPr.setPvnMatchWithTcsLedger(
								StringUtils.isNotBlank(tcsGstPrRow.getField("PVN Match with TCS Ledger"))
										? tcsGstPrRow.getField("PVN Match with TCS Ledger")
										: StringUtils.EMPTY);
						String quantity = tcsGstPrRow.getField("Quantity").replaceAll("[^0-9.]", "").trim();
						tcsPr.setQuantity(
								(BigDecimal) decimalFormat.parse(StringUtils.isNotBlank(quantity) ? quantity : "0"));
						tcsPr.setReturnPeriod(
								Integer.valueOf(StringUtils.isNotBlank(tcsGstPrRow.getField("ReturnPeriod"))
										? tcsGstPrRow.getField("ReturnPeriod")
										: StringUtils.EMPTY));
						tcsPr.setSupplierGstin(StringUtils.isNotBlank(tcsGstPrRow.getField("SupplierGSTIN"))
								? tcsGstPrRow.getField("SupplierGSTIN")
								: StringUtils.EMPTY);
						tcsPr.setSupplierName(StringUtils.isNotBlank(tcsGstPrRow.getField("SupplierName"))
								? tcsGstPrRow.getField("SupplierName")
								: StringUtils.EMPTY);
						tcsPr.setSupplyType(StringUtils.isNotBlank(tcsGstPrRow.getField("SupplyType"))
								? tcsGstPrRow.getField("SupplyType")
								: StringUtils.EMPTY);

						// save tcs gst purchase register
						tcsGstPurchaseRegisterDAO.save(tcsPr);
						processedRecords++;
					}

				} // for end
				logger.info("Original row count: {}", processedRecords);
			} else if ("TCS_RECEIVABLE_LEDGER".equalsIgnoreCase(uploadType)) {
				for (CsvRow tcsRlRow : csv.getRows()) {
					if (StringUtils.isNotBlank(StringUtils.join(tcsRlRow.getFields(), ", ").replaceAll(",", ""))) {
						TcsReciverLedger tcsRl = new TcsReciverLedger();
						tcsRl.setActive(true);
						tcsRl.setCreatedBy(userName);
						tcsRl.setModifiedBy(userName);
						tcsRl.setCreatedDate(new Timestamp(new Date().getTime()));
						tcsRl.setModifiedDate(new Timestamp(new Date().getTime()));
						tcsRl.setCollectorPan(collectorPan);
						tcsRl.setCollectorTan(collectorTan);
						tcsRl.setAssessmentYear(assessmentYear);
						tcsRl.setGlAccount(StringUtils.isNotBlank(tcsRlRow.getField("G/L Account"))
								? tcsRlRow.getField("G/L Account")
								: StringUtils.EMPTY);
						tcsRl.setDocumentHeaderText(StringUtils.isNotBlank(tcsRlRow.getField("Document Header Text"))
								? tcsRlRow.getField("Document Header Text")
								: StringUtils.EMPTY);
						tcsRl.setCustomer(
								StringUtils.isNotBlank(tcsRlRow.getField("Customer")) ? tcsRlRow.getField("Customer")
										: StringUtils.EMPTY);
						tcsRl.setVendor(
								StringUtils.isNotBlank(tcsRlRow.getField("Vendor")) ? tcsRlRow.getField("Vendor")
										: StringUtils.EMPTY);
						tcsRl.setReference(
								StringUtils.isNotBlank(tcsRlRow.getField("Reference")) ? tcsRlRow.getField("Reference")
										: StringUtils.EMPTY);
						tcsRl.setBusinessPlace(StringUtils.isNotBlank(tcsRlRow.getField("Business place"))
								? tcsRlRow.getField("Business place")
								: StringUtils.EMPTY);
						tcsRl.setOrders(StringUtils.isNotBlank(tcsRlRow.getField("Order")) ? tcsRlRow.getField("Order")
								: StringUtils.EMPTY);
						tcsRl.setBusinessArea(StringUtils.isNotBlank(tcsRlRow.getField("Business Area"))
								? tcsRlRow.getField("Business Area")
								: StringUtils.EMPTY);
						// BigDecimal amounts
						String amountInLocal = tcsRlRow.getField("Amount in local currency").replaceAll("[^0-9]", "")
								.trim();
						if (StringUtils.isNotBlank(amountInLocal)) {
							tcsRl.setAmountInLocalCurrency((BigDecimal) decimalFormat.parse(amountInLocal));
						}
						String profitCenter = tcsRlRow.getField("Profit Center").replaceAll("[^0-9.]", "").trim();
						if (StringUtils.isNotBlank(profitCenter)) {
							tcsRl.setProfitCenter((BigDecimal) decimalFormat.parse(profitCenter));
						}
						String costCenter = tcsRlRow.getField("Cost Center").replaceAll("[^0-9.]", "").trim();
						if (StringUtils.isNotBlank(costCenter)) {
							tcsRl.setCostCenter((BigDecimal) decimalFormat.parse(costCenter));
						}
						tcsRl.setLocalCurrency(StringUtils.isNotBlank(tcsRlRow.getField("Local Currency"))
								? tcsRlRow.getField("Local Currency")
								: StringUtils.EMPTY);
						tcsRl.setText(StringUtils.isNotBlank(tcsRlRow.getField("Text")) ? tcsRlRow.getField("Text")
								: StringUtils.EMPTY);
						tcsRl.setOffsettingAccount(StringUtils.isNotBlank(tcsRlRow.getField("Offsetting Account"))
								? tcsRlRow.getField("Offsetting Account")
								: StringUtils.EMPTY);
						tcsRl.setNameOfOffsettingAccount(
								StringUtils.isNotBlank(tcsRlRow.getField("Name of offsetting account"))
										? tcsRlRow.getField("Name of offsetting account")
										: StringUtils.EMPTY);
						tcsRl.setPlant(StringUtils.isNotBlank(tcsRlRow.getField("Plant")) ? tcsRlRow.getField("Plant")
								: StringUtils.EMPTY);
						tcsRl.setUserName(
								StringUtils.isNotBlank(tcsRlRow.getField("User Name")) ? tcsRlRow.getField("User Name")
										: StringUtils.EMPTY);
						tcsRl.setOffsettAccountType(StringUtils.isNotBlank(tcsRlRow.getField("Offsett.account type"))
								? tcsRlRow.getField("Offsett.account type")
								: StringUtils.EMPTY);
						tcsRl.setMaterial(
								StringUtils.isNotBlank(tcsRlRow.getField("Material")) ? tcsRlRow.getField("Material")
										: StringUtils.EMPTY);
						tcsRl.setInvoiceReference(StringUtils.isNotBlank(tcsRlRow.getField("Invoice reference"))
								? tcsRlRow.getField("Invoice reference")
								: StringUtils.EMPTY);
						tcsRl.setCollectiveInvoice(
								Boolean.valueOf(StringUtils.isNotBlank(tcsRlRow.getField("Collective invoice"))
										? tcsRlRow.getField("Collective invoice")
										: StringUtils.EMPTY));
						tcsRl.setYearMonth(StringUtils.isNotBlank(tcsRlRow.getField("Year/month"))
								? tcsRlRow.getField("Year/month")
								: StringUtils.EMPTY);
						tcsRl.setBillingDocument(StringUtils.isNotBlank(tcsRlRow.getField("Billing Document"))
								? tcsRlRow.getField("Billing Document")
								: StringUtils.EMPTY);
						tcsRl.setPurchaseVoucherNumber(StringUtils.isNotBlank(tcsRlRow.getField("SupplyType"))
								? tcsRlRow.getField("SupplyType")
								: StringUtils.EMPTY);
						tcsRl.setMatch(tcsRlRow.getField("Match") == "Yes" ? true : false);
						if (StringUtils.isNotBlank(tcsRlRow.getField("Posting Date"))) {
							String dateFormat = tcsRlRow.getField("Posting Date").replace("/", "-");
							Date postingDate = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT).parse(dateFormat);
							tcsRl.setPostingDate(postingDate);
						}
						if (StringUtils.isNotBlank(tcsRlRow.getField("Document Date"))) {
							String dateFormat = tcsRlRow.getField("Document Date").replace("/", "-");
							Date docmentDate = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT).parse(dateFormat);
							tcsRl.setDocumentDate(docmentDate);
						}
						tcsRl.setDocumentNumber(StringUtils.isNotBlank(tcsRlRow.getField("Document Number"))
								? tcsRlRow.getField("Document Number")
								: StringUtils.EMPTY);
						tcsRl.setDocumentType(StringUtils.isNotBlank(tcsRlRow.getField("Document type"))
								? tcsRlRow.getField("Document type")
								: StringUtils.EMPTY);
						tcsRl.setRlIsMatched(false);
						tcsRl.setPurchaseVoucherNumber(
								StringUtils.isNotBlank(tcsRlRow.getField("PurchaseVoucherNumber"))
										? tcsRlRow.getField("PurchaseVoucherNumber")
										: StringUtils.EMPTY);
						// save tcs reciver ledger
						tcsReciverLedgerDAO.save(tcsRl);
						processedRecords++;
					} // for end
				}
			}
		} catch (Exception e) {
			logger.info("exceptin {}", e.getMessage());
			throw new CustomException(e.getMessage());
		}
		batchUpload.setRowsCount(Long.valueOf(processedRecords));
		batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
		batchUpload.setProcessed(processedRecords);
		batchUpload.setStatus("Processed");
		return tcsMismatchService.update(batchUpload);
	}

	/**
	 * 
	 * @param collectorTan
	 * @param assesmentYear
	 * @param collectorPan
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	public void getReconciliationExcelDownload(String collectorTan, Integer assesmentYear, String collectorPan,
			String tenantId, String userName) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		TCSBatchUpload tcsBatchUplod = tcsMismatchService.saveMismatchReport(collectorTan, tenantId, assesmentYear,
				null, 0L, UploadTypes.LEDGER_RECIVABLE_RECONCILIATION_REPORT.name(), "uploaded", month, userName, null);
		try {
			tcsGstPurchaseRegisterDAO.USP26ASReconciliationForm(assesmentYear, collectorTan, collectorPan);
		} catch (Exception e) {
			logger.error("Error ocuured while running USPLCCInvoiceAdjustments", e);
		}
		// get purchase register data
		List<TcsGstPurchaseRegister> gstPrList = tcsGstPurchaseRegisterDAO.getGstPurchasedData(collectorTan,
				assesmentYear, collectorPan, false);

		// get tcs reciver ledger data
		List<TcsReciverLedger> tcsRlList = tcsReciverLedgerDAO.getTcsReciverLedgerData(collectorTan, assesmentYear,
				collectorPan, false);

		// get purchase register and reciver ledger matched data
		List<TcsGstPurchaseRegisterReportDTO> gstPrAndRlMatchList = tcsGstPurchaseRegisterDAO
				.getGstPurchasedAndRlData(collectorTan, assesmentYear, collectorPan);

		Workbook workbook = new Workbook();
		// App in PR not in TCSRL sheet
		Worksheet worksheet1 = workbook.getWorksheets().get(0);
		worksheet1.setName("App in PR not in TCSRL");
		// App in TCSRL not in PR sheet
		Worksheet worksheet2 = workbook.getWorksheets().add("App in TCSRL not in PR");
		// App in TCSRL and PR
		Worksheet worksheet3 = workbook.getWorksheets().add("App in TCSRL and PR");

		worksheet1.getCells().importArray(gstPrHeaderNames, 0, 0, false);
		worksheet2.getCells().importArray(tcsRlHeaderNames, 0, 0, false);
		worksheet3.getCells().importArray(tcsGstPrAndRlHeaderNames, 0, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForGstPr(gstPrList, worksheet1);
		setExtractDataForTcsRL(tcsRlList, worksheet2);
		setExtractDataForGstPrAndTcsRlMatchedData(gstPrAndRlMatchList, worksheet3);

		// Style for A1 to W1 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(164, 160, 160));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet1.getCells().createRange("A1:W1");
		headerColorRange1.setStyle(style1);

		// Style for A1 to AC1 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(164, 160, 160));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet2.getCells().createRange("A1:AC1");
		headerColorRange2.setStyle(style2);

		// Style for A1 to Y1 headers
		Style style3 = workbook.createStyle();
		style3.setForegroundColor(Color.fromArgb(164, 160, 160));
		style3.setPattern(BackgroundType.SOLID);
		style3.getFont().setBold(true);
		style3.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange3 = worksheet3.getCells().createRange("A1:Y1");
		headerColorRange3.setStyle(style3);

		worksheet1.autoFitColumns();
		worksheet1.autoFitRows();

		worksheet2.autoFitColumns();
		worksheet2.autoFitRows();

		worksheet3.autoFitColumns();
		worksheet3.autoFitRows();

		workbook.save(out, SaveFormat.XLSX);

		tcsMismatchService.saveMismatchReport(collectorTan, tenantId, assesmentYear, out, 0L,
				UploadTypes.LEDGER_RECIVABLE_RECONCILIATION_REPORT.name(), "processed", month, userName,
				tcsBatchUplod.getId());
	}

	/**
	 * 
	 * @param tcsPrAndRlMatchedList
	 * @param tcsRlMatchList
	 * @param worksheet3
	 * @param collectorTan
	 * @param tcsGstPrAndRlHeaderNames2
	 */
	private void setExtractDataForGstPrAndTcsRlMatchedData(List<TcsGstPurchaseRegisterReportDTO> tcsPrAndRlMatchedList,
			Worksheet worksheet3) {
		int rowIndex = 1;
		for (TcsGstPurchaseRegisterReportDTO tcsPrAndRl : tcsPrAndRlMatchedList) {
			ArrayList<Object> rowData = new ArrayList<Object>();
			rowData.add(tcsPrAndRl.getReturnPeriod());
			rowData.add(StringUtils.isBlank(tcsPrAndRl.getDocumentType()) ? StringUtils.EMPTY
					: tcsPrAndRl.getDocumentType());
			rowData.add(
					StringUtils.isBlank(tcsPrAndRl.getSupplyType()) ? StringUtils.EMPTY : tcsPrAndRl.getSupplyType());
			rowData.add(StringUtils.isBlank(tcsPrAndRl.getDocumentNumber()) ? StringUtils.EMPTY
					: tcsPrAndRl.getDocumentNumber());
			rowData.add(
					tcsPrAndRl.getDocumentDate() == null ? StringUtils.EMPTY : tcsPrAndRl.getDocumentDate().toString());
			rowData.add(StringUtils.isBlank(tcsPrAndRl.getSupplierGstin()) ? StringUtils.EMPTY
					: tcsPrAndRl.getSupplierGstin());
			rowData.add(StringUtils.isBlank(tcsPrAndRl.getPan()) ? StringUtils.EMPTY : tcsPrAndRl.getPan());
			rowData.add(StringUtils.isBlank(tcsPrAndRl.getSupplierName()) ? StringUtils.EMPTY
					: tcsPrAndRl.getSupplierName());
			rowData.add(StringUtils.isBlank(tcsPrAndRl.getHsnOrSac()) ? StringUtils.EMPTY : tcsPrAndRl.getHsnOrSac());
			rowData.add(tcsPrAndRl.getQuantity() == null ? StringUtils.EMPTY : tcsPrAndRl.getQuantity());
			rowData.add(tcsPrAndRl.getTaxableValue() == null ? StringUtils.EMPTY : tcsPrAndRl.getTaxableValue());
			rowData.add(tcsPrAndRl.getIntegratedTaxamount() == null ? StringUtils.EMPTY
					: tcsPrAndRl.getIntegratedTaxamount());
			rowData.add(
					tcsPrAndRl.getCentralTaxAmount() == null ? StringUtils.EMPTY : tcsPrAndRl.getCentralTaxAmount());
			rowData.add(
					tcsPrAndRl.getStateUttaxAmount() == null ? StringUtils.EMPTY : tcsPrAndRl.getStateUttaxAmount());
			BigDecimal totalAmount = BigDecimal.ZERO;
			if (tcsPrAndRl.getTaxableValue() != null && tcsPrAndRl.getIntegratedTaxamount() != null
					&& tcsPrAndRl.getCentralTaxAmount() != null && tcsPrAndRl.getStateUttaxAmount() != null) {
				totalAmount = tcsPrAndRl.getTaxableValue().add(tcsPrAndRl.getIntegratedTaxamount())
						.add(tcsPrAndRl.getCentralTaxAmount()).add(tcsPrAndRl.getStateUttaxAmount());
			}
			rowData.add(totalAmount);
			rowData.add(tcsPrAndRl.getInvoiceValue() == null ? StringUtils.EMPTY : tcsPrAndRl.getInvoiceValue());
			rowData.add(tcsPrAndRl.getPurchaseVoucherDate() == null ? StringUtils.EMPTY
					: tcsPrAndRl.getPurchaseVoucherDate().toString());
			rowData.add(
					tcsPrAndRl.getPaymentDate() == null ? StringUtils.EMPTY : tcsPrAndRl.getPaymentDate().toString());
			rowData.add(StringUtils.isBlank(tcsPrAndRl.getPurchaseVoucherNumber()) ? StringUtils.EMPTY
					: tcsPrAndRl.getPurchaseVoucherNumber());
			rowData.add(StringUtils.isBlank(tcsPrAndRl.getExtractorMonth()) ? StringUtils.EMPTY
					: tcsPrAndRl.getExtractorMonth());
			rowData.add(StringUtils.isBlank(tcsPrAndRl.getPvnMatchWithTcsLedger()) ? StringUtils.EMPTY
					: tcsPrAndRl.getPvnMatchWithTcsLedger());
			// TCS Amount as per TCS Ledger
			rowData.add(StringUtils.EMPTY);
			// Pvn Match
			rowData.add(tcsPrAndRl.getPvnMatch() == null ? StringUtils.EMPTY : tcsPrAndRl.getPvnMatch());
			// Payment Voucher Number
			rowData.add(StringUtils.EMPTY);
			// TCS Amount
			rowData.add(tcsPrAndRl.getAmountInLocalCurrency() == null ? StringUtils.EMPTY
					: tcsPrAndRl.getAmountInLocalCurrency());
			worksheet3.getCells().importArrayList(rowData, rowIndex++, 0, false);
		}
	}

	/**
	 * 
	 * @param tcsRlList
	 * @param worksheet2
	 */
	private void setExtractDataForTcsRL(List<TcsReciverLedger> tcsRlList, Worksheet worksheet2) {
		int rowIndex = 1;
		for (TcsReciverLedger tcsRl : tcsRlList) {
			ArrayList<Object> rowData = new ArrayList<Object>();
			rowData.add(StringUtils.isBlank(tcsRl.getGlAccount()) ? StringUtils.EMPTY : tcsRl.getGlAccount());
			rowData.add(StringUtils.isBlank(tcsRl.getDocumentHeaderText()) ? StringUtils.EMPTY
					: tcsRl.getDocumentHeaderText());
			rowData.add(StringUtils.isBlank(tcsRl.getDocumentNumber()) ? StringUtils.EMPTY : tcsRl.getDocumentNumber());
			rowData.add(StringUtils.isBlank(tcsRl.getCustomer()) ? StringUtils.EMPTY : tcsRl.getCustomer());
			rowData.add(StringUtils.isBlank(tcsRl.getVendor()) ? StringUtils.EMPTY : tcsRl.getVendor());
			rowData.add(tcsRl.getPostingDate() == null ? StringUtils.EMPTY : tcsRl.getPostingDate().toString());
			rowData.add(tcsRl.getDocumentDate() == null ? StringUtils.EMPTY : tcsRl.getDocumentDate().toString());
			rowData.add(StringUtils.isBlank(tcsRl.getReference()) ? StringUtils.EMPTY : tcsRl.getReference());
			rowData.add(StringUtils.isBlank(tcsRl.getBusinessPlace()) ? StringUtils.EMPTY : tcsRl.getBusinessPlace());
			rowData.add(StringUtils.isBlank(tcsRl.getOrders()) ? StringUtils.EMPTY : tcsRl.getOrders());
			rowData.add(StringUtils.isBlank(tcsRl.getDocumentType()) ? StringUtils.EMPTY : tcsRl.getDocumentType());
			rowData.add(StringUtils.isBlank(tcsRl.getBusinessArea()) ? StringUtils.EMPTY : tcsRl.getBusinessArea());
			rowData.add(tcsRl.getProfitCenter() == null ? StringUtils.EMPTY : tcsRl.getProfitCenter());
			rowData.add(
					tcsRl.getAmountInLocalCurrency() == null ? StringUtils.EMPTY : tcsRl.getAmountInLocalCurrency());
			rowData.add(StringUtils.isBlank(tcsRl.getLocalCurrency()) ? StringUtils.EMPTY : tcsRl.getLocalCurrency());
			rowData.add(StringUtils.isBlank(tcsRl.getText()) ? StringUtils.EMPTY : tcsRl.getText());
			rowData.add(StringUtils.isBlank(tcsRl.getOffsettingAccount()) ? StringUtils.EMPTY
					: tcsRl.getOffsettingAccount());
			rowData.add(StringUtils.isBlank(tcsRl.getNameOfOffsettingAccount()) ? StringUtils.EMPTY
					: tcsRl.getNameOfOffsettingAccount());
			rowData.add(tcsRl.getCostCenter() == null ? StringUtils.EMPTY : tcsRl.getCostCenter());
			rowData.add(tcsRl.getPlant() == null ? StringUtils.EMPTY : tcsRl.getPlant());
			rowData.add(tcsRl.getUserName() == null ? StringUtils.EMPTY : tcsRl.getUserName());
			rowData.add(StringUtils.isBlank(tcsRl.getOffsettAccountType()) ? StringUtils.EMPTY
					: tcsRl.getOffsettAccountType());
			rowData.add(StringUtils.isBlank(tcsRl.getMaterial()) ? StringUtils.EMPTY : tcsRl.getMaterial());
			rowData.add(
					StringUtils.isBlank(tcsRl.getInvoiceReference()) ? StringUtils.EMPTY : tcsRl.getInvoiceReference());
			rowData.add(tcsRl.getCollectiveInvoice() == null ? StringUtils.EMPTY : tcsRl.getCollectiveInvoice());
			rowData.add(StringUtils.isBlank(tcsRl.getYearMonth()) ? StringUtils.EMPTY : tcsRl.getYearMonth());
			rowData.add(tcsRl.getBillingDocument() == null ? StringUtils.EMPTY : tcsRl.getBillingDocument());
			rowData.add(
					tcsRl.getPurchaseVoucherNumber() == null ? StringUtils.EMPTY : tcsRl.getPurchaseVoucherNumber());
			rowData.add(tcsRl.getMatch() == null ? StringUtils.EMPTY : tcsRl.getMatch());

			worksheet2.getCells().importArrayList(rowData, rowIndex++, 0, false);
		}
	}

	/**
	 * 
	 * @param gstPrList
	 * @param worksheet1
	 * @param collectorTan
	 */
	private void setExtractDataForGstPr(List<TcsGstPurchaseRegister> gstPrList, Worksheet worksheet1) {
		int rowIndex = 1;
		for (TcsGstPurchaseRegister gstPr : gstPrList) {
			ArrayList<Object> rowData = new ArrayList<Object>();
			rowData.add(gstPr.getReturnPeriod());
			rowData.add(StringUtils.isBlank(gstPr.getDocumentType()) ? StringUtils.EMPTY : gstPr.getDocumentType());
			rowData.add(StringUtils.isBlank(gstPr.getSupplyType()) ? StringUtils.EMPTY : gstPr.getSupplyType());
			rowData.add(StringUtils.isBlank(gstPr.getDocumentNumber()) ? StringUtils.EMPTY : gstPr.getDocumentNumber());
			rowData.add(gstPr.getDocumentDate() == null ? StringUtils.EMPTY : gstPr.getDocumentDate().toString());
			rowData.add(StringUtils.isBlank(gstPr.getSupplierGstin()) ? StringUtils.EMPTY : gstPr.getSupplierGstin());
			rowData.add(StringUtils.isBlank(gstPr.getPan()) ? StringUtils.EMPTY : gstPr.getPan());
			rowData.add(StringUtils.isBlank(gstPr.getSupplierName()) ? StringUtils.EMPTY : gstPr.getSupplierName());
			rowData.add(StringUtils.isBlank(gstPr.getHsnOrSac()) ? StringUtils.EMPTY : gstPr.getHsnOrSac());
			rowData.add(gstPr.getQuantity() == null ? StringUtils.EMPTY : gstPr.getQuantity());
			rowData.add(gstPr.getTaxableValue() == null ? StringUtils.EMPTY : gstPr.getTaxableValue());
			rowData.add(gstPr.getIntegratedTaxamount() == null ? StringUtils.EMPTY : gstPr.getIntegratedTaxamount());
			rowData.add(gstPr.getCentralTaxAmount() == null ? StringUtils.EMPTY : gstPr.getCentralTaxAmount());
			rowData.add(gstPr.getStateUttaxAmount() == null ? StringUtils.EMPTY : gstPr.getStateUttaxAmount());
			BigDecimal totalAmount = BigDecimal.ZERO;
			if (gstPr.getTaxableValue() != null && gstPr.getIntegratedTaxamount() != null
					&& gstPr.getCentralTaxAmount() != null && gstPr.getStateUttaxAmount() != null) {
				totalAmount = gstPr.getTaxableValue().add(gstPr.getIntegratedTaxamount())
						.add(gstPr.getCentralTaxAmount()).add(gstPr.getStateUttaxAmount());
			}
			rowData.add(totalAmount);
			rowData.add(gstPr.getInvoiceValue() == null ? StringUtils.EMPTY : gstPr.getInvoiceValue());
			rowData.add(gstPr.getPurchaseVoucherDate() == null ? StringUtils.EMPTY
					: gstPr.getPurchaseVoucherDate().toString());
			rowData.add(gstPr.getPaymentDate() == null ? StringUtils.EMPTY : gstPr.getPaymentDate().toString());
			rowData.add(StringUtils.isBlank(gstPr.getPurchaseVoucherNumber()) ? StringUtils.EMPTY
					: gstPr.getPurchaseVoucherNumber());
			rowData.add(StringUtils.isBlank(gstPr.getExtractorMonth()) ? StringUtils.EMPTY : gstPr.getExtractorMonth());
			rowData.add(StringUtils.isBlank(gstPr.getPvnMatchWithTcsLedger()) ? StringUtils.EMPTY
					: gstPr.getPvnMatchWithTcsLedger());
			rowData.add(StringUtils.EMPTY);
			rowData.add(gstPr.getPvnMatch() == null ? StringUtils.EMPTY : gstPr.getPvnMatch());

			worksheet1.getCells().importArrayList(rowData, rowIndex++, 0, false);
		}
	}

	/**
	 * 
	 * @param from26asFinalResultList
	 * @param worksheet3
	 */
	private void setExtractDataForForm26ASFinalResult(Set<Form26ASFinalResultFileReportDTO> from26asFinalResultList,
			Worksheet worksheet3) {
		int rowIndex = 1;
		for (Form26ASFinalResultFileReportDTO form26ASFinalResult : from26asFinalResultList) {
			ArrayList<Object> rowData = new ArrayList<Object>();
			// Results
			rowData.add(StringUtils.EMPTY);
			rowData.add(StringUtils.isBlank(form26ASFinalResult.getSupplierName26AS()) ? StringUtils.EMPTY
					: form26ASFinalResult.getSupplierName26AS());
			rowData.add(StringUtils.isBlank(form26ASFinalResult.getSupplierNameGSTPR()) ? StringUtils.EMPTY
					: form26ASFinalResult.getSupplierNameGSTPR());
			rowData.add(StringUtils.isBlank(form26ASFinalResult.getSectionCode26AS()) ? StringUtils.EMPTY
					: form26ASFinalResult.getSectionCode26AS());
			rowData.add(StringUtils.isBlank(form26ASFinalResult.getDocumentNoGSTPR()) ? StringUtils.EMPTY
					: form26ASFinalResult.getDocumentNoGSTPR());
			rowData.add(StringUtils.isBlank(form26ASFinalResult.getPurchaseVoucherNoGSTPR()) ? StringUtils.EMPTY
					: form26ASFinalResult.getPurchaseVoucherNoGSTPR());
			rowData.add(form26ASFinalResult.getTransactionDate26AS() == null ? StringUtils.EMPTY
					: form26ASFinalResult.getTransactionDate26AS());
			rowData.add(form26ASFinalResult.getTransactionDateGSTPR() == null ? StringUtils.EMPTY
					: form26ASFinalResult.getTransactionDateGSTPR());
			rowData.add(form26ASFinalResult.getTransactionAmount26AS() == null ? StringUtils.EMPTY
					: form26ASFinalResult.getTransactionAmount26AS());
			rowData.add(form26ASFinalResult.getTransactionAmountGSTPR() == null ? StringUtils.EMPTY
					: form26ASFinalResult.getTransactionAmountGSTPR());
			rowData.add(form26ASFinalResult.getTaxableAmountGSTPR() == null ? StringUtils.EMPTY
					: form26ASFinalResult.getTaxableAmountGSTPR());
			rowData.add(
					form26ASFinalResult.getTcs26AS() == null ? StringUtils.EMPTY : form26ASFinalResult.getTcs26AS());
			rowData.add(
					form26ASFinalResult.getTcsTCSRL() == null ? StringUtils.EMPTY : form26ASFinalResult.getTcsTCSRL());
			rowData.add(form26ASFinalResult.getType() == null ? StringUtils.EMPTY : form26ASFinalResult.getType());
			worksheet3.getCells().importArrayList(rowData, rowIndex++, 0, false);
		}

	}

	/**
	 * 
	 * @param gstPrList
	 * @param worksheet2
	 */
	private void setExtractDataForGstPrList(Set<GstPrFileReportDTO> gstPrList, Worksheet worksheet2) {
		int rowIndex = 1;
		for (GstPrFileReportDTO gstPr : gstPrList) {
			ArrayList<Object> rowData = new ArrayList<Object>();
			rowData.add(StringUtils.isBlank(gstPr.getNameOfSupplier()) ? StringUtils.EMPTY : gstPr.getNameOfSupplier());
			rowData.add(gstPr.getSalesTurnover() == null ? StringUtils.EMPTY : gstPr.getSalesTurnover());
			rowData.add(gstPr.getTaxPaidTCSReceivableLedger() == null ? StringUtils.EMPTY
					: gstPr.getTaxPaidTCSReceivableLedger());
			rowData.add(
					StringUtils.isBlank(gstPr.getNameAsPerTraces()) ? StringUtils.EMPTY : gstPr.getNameAsPerTraces());
			worksheet2.getCells().importArrayList(rowData, rowIndex++, 0, false);
		}

	}

	/**
	 * 
	 * @param form26AsList
	 * @param worksheet1
	 */
	private void setExtractDataForForm26AS(Set<Form26ASFileReportDTO> form26AsList, Worksheet worksheet1) {
		int rowIndex = 1;
		int serialNumber = 1;
		for (Form26ASFileReportDTO form26As : form26AsList) {
			ArrayList<Object> rowData = new ArrayList<Object>();
			rowData.add(serialNumber++);
			rowData.add(
					StringUtils.isBlank(form26As.getNameAsPer26As()) ? StringUtils.EMPTY : form26As.getNameAsPer26As());
			rowData.add(StringUtils.isBlank(form26As.getTan()) ? StringUtils.EMPTY : form26As.getTan());
			rowData.add(form26As.getTransactionValue() == null ? StringUtils.EMPTY : form26As.getTransactionValue());
			rowData.add(form26As.getTaxCollected() == null ? StringUtils.EMPTY : form26As.getTaxCollected());
			rowData.add(StringUtils.isBlank(form26As.getPan()) ? StringUtils.EMPTY : form26As.getPan());
			rowData.add(StringUtils.isBlank(form26As.getNameAsPerTRACES()) ? StringUtils.EMPTY
					: form26As.getNameAsPerTRACES());
			worksheet1.getCells().importArrayList(rowData, rowIndex++, 0, false);
		}
	}

	/**
	 * 
	 * @param collectorTan
	 * @param assessmentYear
	 * @param collectorPan
	 * @param tenantId
	 * @param userName
	 * @param type
	 * @param batchId
	 */
	public void getForm26ASFinalResultReportWithSpark(String collectorTan, Integer assesmentYear, String collectorPan,
			String tenantId, String userName, String type, Integer batchId) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		// save tcs batch upload table
		TCSBatchUpload tcsBatchUplod = tcsMismatchService.saveMismatchReport(collectorTan, tenantId, assesmentYear,
				null, 0L, UploadTypes.FORM_26AS_RECONCILIATION_REPORT.name(), "uploaded", month, userName, null);
		TCSBatchUpload batchUpload = tcsMismatchService.getTCSBatchUpload(batchId);

		// get form input file unmatched data sheet 1
		File form26AsunmatchedData = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getErrorFilePath());
		// get form input file unmatched data sheet 2
		File gstPrUnmatchedData = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getSourceFilePath());
		// get form 26AS final result data sheet 3
		File finalResultedCsvData = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getSuccessFileUrl());

		batchUpload.setMismatchCount(0L);
		batchUpload.setModifiedBy(userName);
		batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
		CsvReader csvReader = new CsvReader();
		csvReader.setContainsHeader(true);
		CsvContainer finalResultedCsvContainer = csvReader.read(finalResultedCsvData, StandardCharsets.UTF_8);
		CsvContainer form26AsunmatchedCsvContainer = csvReader.read(form26AsunmatchedData, StandardCharsets.UTF_8);
		CsvContainer gstPrUnmatchedCsvContainer = csvReader.read(gstPrUnmatchedData, StandardCharsets.UTF_8);
		int rowCount1 = finalResultedCsvContainer.getRowCount();
		logger.info("rowCount :{}", rowCount1);
		int rowCount2 = form26AsunmatchedCsvContainer.getRowCount();
		logger.info("rowCount :{}", rowCount2);
		int rowCount3 = gstPrUnmatchedCsvContainer.getRowCount();
		logger.info("rowCount :{}", rowCount3);
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setGroupingSeparator(',');
		symbols.setDecimalSeparator('.');
		String pattern = "#,##0.0#";
		DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
		decimalFormat.setParseBigDecimal(true);
		Set<Form26ASFileReportDTO> form26AsList = new HashSet<>();
		Set<Form26ASFinalResultFileReportDTO> from26ASFinalResultList = new HashSet<>();
		Set<GstPrFileReportDTO> gstPrList = new HashSet<>();
		// sheet 1 data
		for (CsvRow from26AsrowData : form26AsunmatchedCsvContainer.getRows()) {
			Form26ASFileReportDTO from26As = new Form26ASFileReportDTO();
			from26As.setNameAsPer26As(StringUtils.isNotBlank(from26AsrowData.getField("Name as per 26AS"))
					? from26AsrowData.getField("Name as per 26AS")
					: StringUtils.EMPTY);
			from26As.setTan(StringUtils.isNotBlank(from26AsrowData.getField("TAN")) ? from26AsrowData.getField("TAN")
					: StringUtils.EMPTY);
			// BigDecimal amounts
			if (StringUtils.isNotBlank(from26AsrowData.getField("Transaction Value"))) {
				from26As.setTaxCollected(
						(BigDecimal) decimalFormat.parse(from26AsrowData.getField("Transaction Value")));
			}
			if (StringUtils.isNotBlank(from26AsrowData.getField("Tax Collected"))) {
				from26As.setTransactionValue(
						(BigDecimal) decimalFormat.parse(from26AsrowData.getField("Tax Collected")));
			}
			from26As.setPan(StringUtils.isNotBlank(from26AsrowData.getField("PAN")) ? from26AsrowData.getField("PAN")
					: StringUtils.EMPTY);
			from26As.setNameAsPerTRACES(StringUtils.isNotBlank(from26AsrowData.getField("NAME as per TRACES"))
					? from26AsrowData.getField("NAME as per TRACES")
					: StringUtils.EMPTY);
			form26AsList.add(from26As);
		}

		// sheet 2 data
		for (CsvRow gstprRowData : gstPrUnmatchedCsvContainer.getRows()) {
			GstPrFileReportDTO from26As = new GstPrFileReportDTO();
			from26As.setNameOfSupplier(StringUtils.isNotBlank(gstprRowData.getField("Name of supplier"))
					? gstprRowData.getField("Name of supplier")
					: StringUtils.EMPTY);
			if (StringUtils.isNotBlank(gstprRowData.getField("Sales turnover"))) {
				from26As.setSalesTurnover((BigDecimal) decimalFormat.parse(gstprRowData.getField("Sales turnover")));
			}
			if (StringUtils.isNotBlank(gstprRowData.getField("Tax paid in Q3 - TCS Receivable ledger"))) {
				from26As.setTaxPaidTCSReceivableLedger((BigDecimal) decimalFormat
						.parse(gstprRowData.getField("Tax paid in Q3 - TCS Receivable ledger")));
			}
			from26As.setNameAsPerTraces(StringUtils.isNotBlank(gstprRowData.getField("Name as per TRACES"))
					? gstprRowData.getField("Name as per TRACES")
					: StringUtils.EMPTY);
			gstPrList.add(from26As);
		}
		// sheet 3
		for (CsvRow sucessdata : finalResultedCsvContainer.getRows()) {
			Form26ASFinalResultFileReportDTO from26ASFinalResul = new Form26ASFinalResultFileReportDTO();
			from26ASFinalResul.setSupplierName26AS(StringUtils.isNotBlank(sucessdata.getField("Supplier Name (26AS)"))
					? sucessdata.getField("Supplier Name (26AS)")
					: StringUtils.EMPTY);
			from26ASFinalResul.setSupplierNameGSTPR(StringUtils.isNotBlank(sucessdata.getField("Supplier Name (GSTPR)"))
					? sucessdata.getField("Supplier Name (GSTPR)")
					: StringUtils.EMPTY);
			from26ASFinalResul.setSectionCode26AS(StringUtils.isNotBlank(sucessdata.getField("Section Code (26AS)"))
					? sucessdata.getField("Section Code (26AS)")
					: StringUtils.EMPTY);
			// BigDecimal amounts
			if (StringUtils.isNotBlank(sucessdata.getField("Transaction Amount (26AS)"))) {
				from26ASFinalResul.setTransactionAmount26AS(
						(BigDecimal) decimalFormat.parse(sucessdata.getField("Transaction Amount (26AS)")));
			}
			if (StringUtils.isNotBlank(sucessdata.getField("Transaction Amount (GSTPR)"))) {
				from26ASFinalResul.setTransactionAmountGSTPR(
						(BigDecimal) decimalFormat.parse(sucessdata.getField("Transaction Amount (GSTPR)")));
			}
			if (StringUtils.isNotBlank(sucessdata.getField("Taxable Amount (GSTPR)"))) {
				from26ASFinalResul.setTaxableAmountGSTPR(
						(BigDecimal) decimalFormat.parse(sucessdata.getField("Taxable Amount (GSTPR)")));
			}
			if (StringUtils.isNotBlank(sucessdata.getField("TCS (26AS)"))) {
				from26ASFinalResul.setTcs26AS((BigDecimal) decimalFormat.parse(sucessdata.getField("TCS (26AS)")));
			}
			if (StringUtils.isNotBlank(sucessdata.getField("TCS (TCSRL)"))) {
				from26ASFinalResul.setTcsTCSRL((BigDecimal) decimalFormat.parse(sucessdata.getField("TCS (TCSRL)")));
			}
			if (StringUtils.isNotBlank(sucessdata.getField("Transaction Date (26AS)"))) {
				Date paymentDate = new SimpleDateFormat("yyyy-MM-dd")
						.parse(sucessdata.getField("Transaction Date (26AS)"));
				from26ASFinalResul.setTransactionDate26AS(paymentDate);
			}
			if (StringUtils.isNotBlank(sucessdata.getField("Transaction Date (GSTPR)"))) {
				Date purchsedDate = new SimpleDateFormat("yyyy-MM-dd")
						.parse(sucessdata.getField("Transaction Date (GSTPR)"));
				from26ASFinalResul.setTransactionDateGSTPR(purchsedDate);
			}
			from26ASFinalResul.setDocumentNoGSTPR(StringUtils.isNotBlank(sucessdata.getField("Document No. (GSTPR)"))
					? sucessdata.getField("Document No. (GSTPR)")
					: StringUtils.EMPTY);
			from26ASFinalResul.setPurchaseVoucherNoGSTPR(
					StringUtils.isNotBlank(sucessdata.getField("Purchase Voucher No. (GSTPR)"))
							? sucessdata.getField("Purchase Voucher No. (GSTPR)")
							: StringUtils.EMPTY);
			from26ASFinalResultList.add(from26ASFinalResul);
		}

		generateForm26FinalReportExcel(form26AsList, from26ASFinalResultList, gstPrList, tcsBatchUplod, tenantId,
				userName);

	}

	/**
	 * 
	 * @param form26AsList
	 * @param from26asFinalResultList
	 * @param gstPrList
	 * @param tcsBatchUplod
	 * @param tenantId
	 * @param userName
	 * @throws Exception
	 */
	private void generateForm26FinalReportExcel(Set<Form26ASFileReportDTO> form26AsList,
			Set<Form26ASFinalResultFileReportDTO> from26asFinalResultList, Set<GstPrFileReportDTO> gstPrList,
			TCSBatchUpload tcsBatchUplod, String tenantId, String userName) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		Workbook workbook = new Workbook();
		// Appearing in 26AS, not in GSTPR
		Worksheet worksheet1 = workbook.getWorksheets().get(0);
		worksheet1.setName("Appearing in 26AS, not in GSTPR");
		// Appearing in GSTPR, not in 26AS
		Worksheet worksheet2 = workbook.getWorksheets().add("Appearing in GSTPR, not in 26AS");
		// Form26AS Final Result
		Worksheet worksheet3 = workbook.getWorksheets().add("Form26AS Final Result");

		worksheet1.getCells().importArray(form26AsHeaders, 0, 0, false);
		worksheet2.getCells().importArray(gstPrHeaders, 0, 0, false);
		worksheet3.getCells().importArray(form26ASFinalResultHeaders, 0, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForForm26AS(form26AsList, worksheet1);
		setExtractDataForGstPrList(gstPrList, worksheet2);
		setExtractDataForForm26ASFinalResult(from26asFinalResultList, worksheet3);

		// Style for A1 to G1 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(164, 160, 160));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet1.getCells().createRange("A1:G1");
		headerColorRange1.setStyle(style1);

		// Style for A1 to D1 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(164, 160, 160));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet2.getCells().createRange("A1:D1");
		headerColorRange2.setStyle(style2);

		// Style for A1 to M1 headers
		Style style3 = workbook.createStyle();
		style3.setForegroundColor(Color.fromArgb(164, 160, 160));
		style3.setPattern(BackgroundType.SOLID);
		style3.getFont().setBold(true);
		style3.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange3 = worksheet3.getCells().createRange("A1:N1");
		headerColorRange3.setStyle(style3);

		worksheet1.autoFitColumns();
		worksheet1.autoFitRows();

		worksheet2.autoFitColumns();
		worksheet2.autoFitRows();

		worksheet3.autoFitColumns();
		worksheet3.autoFitRows();

		workbook.save(out, SaveFormat.XLSX);

		tcsMismatchService.saveMismatchReport(tcsBatchUplod.getCollectorMasterTan(), tenantId,
				tcsBatchUplod.getAssessmentYear(), out, 0L, UploadTypes.FORM_26AS_RECONCILIATION_REPORT.name(),
				"processed", tcsBatchUplod.getAssessmentMonth(), userName, tcsBatchUplod.getId());
	}

	@Async
	public TCSBatchUpload process26As(String collectorTan, String collectorPan, Integer batchId, Integer assessmentYear,
			String userName, String tenantId, String uploadType, String token) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		TCSBatchUpload batchUpload = tcsMismatchService.getTCSBatchUpload(batchId);
		File csvFile1 = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getOtherFileUrl());

		// CSV reader
		CsvReader csvReader = new CsvReader();
		csvReader.setContainsHeader(true);
		CsvContainer csv = csvReader.read(csvFile1, StandardCharsets.UTF_8);
		Integer processedRecords = 0;

		List<Tcs26AsInputDto> tcs26AsList = new ArrayList<>();
		for (CsvRow row : csv.getRows()) {

			String part = row.getField("Part");
			String header = row.getField("Sr. No. (Header)");
			String child = row.getField("Sr. No.(Child)");
			String deductorName = row.getField("Name of Deductor");
			String deductorTan = row.getField("TAN of Deductor");
			BigDecimal totalAmountPaid = StringUtils.isNotBlank(row.getField("Total Amount Paid / Credited(Rs.)"))
					? new BigDecimal(row.getField("Total Amount Paid / Credited(Rs.)"))
					: null;
			BigDecimal totalTaxDeducted = StringUtils.isNotBlank(row.getField("Total Tax Deducted(Rs.)"))
					? new BigDecimal(row.getField("Total Tax Deducted(Rs.)"))
					: null;
			BigDecimal totalTdsDeposited = StringUtils.isNotBlank(row.getField("Total TDS Deposited(Rs.)"))
					? new BigDecimal(row.getField("Total TDS Deposited(Rs.)"))
					: null;
			String section = row.getField("Section");
			Date transactionDate = null;
			if (StringUtils.isNotBlank(row.getField("Transaction Date"))) {
				String dateFormat = row.getField("Transaction Date").replace("/", "-");
				transactionDate = new SimpleDateFormat("dd-MMM-yyyy").parse(dateFormat);
			}
			String statusOfBooking = row.getField("Status of Booking");
			Date dateOfBooking = null;
			if (StringUtils.isNotBlank(row.getField("Date of Booking"))) {
				String dateFormat = row.getField("Date of Booking").replace("/", "-");
				dateOfBooking = new SimpleDateFormat("dd-MMM-yyyy").parse(dateFormat);
			}
			String remarks = row.getField("Remarks");
			BigDecimal amountPaid = StringUtils.isNotBlank(row.getField("Amount Paid / Credited(Rs.)"))
					? new BigDecimal(row.getField("Amount Paid / Credited(Rs.)"))
					: null;
			BigDecimal taxDeducted = StringUtils.isNotBlank(row.getField("Tax Deducted(Rs.)"))
					? new BigDecimal(row.getField("Tax Deducted(Rs.)"))
					: null;
			BigDecimal tdsDeposited = StringUtils.isNotBlank(row.getField("TDS Deposited(Rs.)"))
					? new BigDecimal(row.getField("TDS Deposited(Rs.)"))
					: null;

			Tcs26AsInputDto tcs26AsInputDto = new Tcs26AsInputDto();
			tcs26AsInputDto.setPart(part);
			tcs26AsInputDto.setHeader(Integer.valueOf(header));
			tcs26AsInputDto.setChild(Integer.valueOf(child));
			tcs26AsInputDto.setCollectorName(deductorName);
			tcs26AsInputDto.setCollectorTan(deductorTan);
			tcs26AsInputDto.setTotalAmountPaid(totalAmountPaid);
			tcs26AsInputDto.setTotalTaxDeducted(totalTaxDeducted);
			tcs26AsInputDto.setTotalTdsDeposited(totalTdsDeposited);
			tcs26AsInputDto.setSection(section);
			tcs26AsInputDto.setTransactionDate(transactionDate);
			tcs26AsInputDto.setStatusOfBooking(statusOfBooking);
			tcs26AsInputDto.setDateOfBooking(dateOfBooking);
			tcs26AsInputDto.setRemarks(remarks);
			tcs26AsInputDto.setAmountPaid(amountPaid);
			tcs26AsInputDto.setTaxDeducted(taxDeducted);
			tcs26AsInputDto.setTdsDeposited(tdsDeposited);
			tcs26AsInputDto.setActive(true);
			tcs26AsInputDto.setCreatedBy(userName);
			tcs26AsInputDto.setModifiedBy(userName);
			tcs26AsInputDto.setCreatedDate(new Timestamp(new Date().getTime()));
			tcs26AsInputDto.setModifiedDate(new Timestamp(new Date().getTime()));
			tcs26AsInputDto.setCollectorPan(collectorPan);
			tcs26AsInputDto.setCollectorTan(collectorTan);
			tcs26AsInputDto.setAssessmentYear(assessmentYear);
			// save tcs form 26AS input data
			// tcsForm26ASDAO.save(tcs26AsInputDto);
			tcs26AsList.add(tcs26AsInputDto);
			processedRecords++;
		}

		// get purchase register and reciver ledger matched data
		List<TcsGstPurchaseRegisterReportDTO> gstPrAndRlList = tcsGstPurchaseRegisterDAO
				.getGstPurchasedAndRlData(collectorTan, assessmentYear, collectorPan);

		// declaring maps for storing amounts
		Map<String, List<BigDecimal>> tcs26AsMap = new HashMap<String, List<BigDecimal>>();
		Map<String, List<BigDecimal>> gstPrAndRlMap = new HashMap<String, List<BigDecimal>>();

		Map<String, List<Tcs26AsInputDto>> tcs26AsObjMap = new HashMap<String, List<Tcs26AsInputDto>>();
		Map<String, List<TcsGstPurchaseRegisterReportDTO>> gstPrAndRlObjMap = new HashMap<String, List<TcsGstPurchaseRegisterReportDTO>>();

		Set<Form26ASFileReportDTO> form26ASFileReportList = new HashSet<>(); // sheet 1
		Set<GstPrFileReportDTO> gstPrFileReportList = new HashSet<>(); // sheet 2
		Set<Form26ASFinalResultFileReportDTO> form26ASFinalResultFileReportDTOList = new HashSet<>(); // sheet 3

		// separating by Collector name
		for (Tcs26AsInputDto tcs26AsInputDto : tcs26AsList) {

			String collectorName = tcs26AsInputDto.getCollectorName().trim().toUpperCase();
			List<BigDecimal> amountPaidList = new ArrayList<>();
			List<Tcs26AsInputDto> l = new ArrayList<>();
			if (tcs26AsMap.get(collectorName) != null) {
				amountPaidList = tcs26AsMap.get(collectorName);
				l = tcs26AsObjMap.get(collectorName);
			}
			amountPaidList.add(tcs26AsInputDto.getAmountPaid());
			tcs26AsMap.put(collectorName, amountPaidList);

			// Object
			l.add(tcs26AsInputDto);
			tcs26AsObjMap.put(collectorName, l);

		}
		// separating by Collector name
		for (TcsGstPurchaseRegisterReportDTO tcsGstPurchaseRegisterReportDTO : gstPrAndRlList) {

			String collectorName = tcsGstPurchaseRegisterReportDTO.getSupplierName().trim().toUpperCase();
			List<BigDecimal> amountPaidList = new ArrayList<>();
			List<TcsGstPurchaseRegisterReportDTO> l = new ArrayList<>();
			if (gstPrAndRlMap.get(collectorName) != null) {
				amountPaidList = gstPrAndRlMap.get(collectorName);
				l = gstPrAndRlObjMap.get(collectorName);
			}
			amountPaidList.add(tcsGstPurchaseRegisterReportDTO.getInvoiceValue());
			gstPrAndRlMap.put(collectorName, amountPaidList);

			// Object
			l.add(tcsGstPurchaseRegisterReportDTO);
			gstPrAndRlObjMap.put(collectorName, l);

		}

		// calling python api with same deductor name list of amounts
		for (Map.Entry<String, List<BigDecimal>> entry : tcs26AsMap.entrySet()) {

			// These 2 will act as unnmatch list
			List<Tcs26AsInputDto> tcs26AsListByDeductor = tcs26AsObjMap.get(entry.getKey());
			List<TcsGstPurchaseRegisterReportDTO> gstPrAndRlMatchListByDeductor = gstPrAndRlObjMap.get(entry.getKey());

			if (tcs26AsListByDeductor != null && gstPrAndRlMatchListByDeductor != null
					&& !tcs26AsListByDeductor.isEmpty() && !gstPrAndRlMatchListByDeductor.isEmpty()) {
				// calling python API
				List<From26AsJson> from26AsJsonList = form26AsLogicApi(entry.getValue(),
						gstPrAndRlMap.get(entry.getKey()), token, collectorTan, collectorPan);

				// separating match and unmatch
				for (From26AsJson from26AsJson : from26AsJsonList) {
					Collections.sort(from26AsJson.getInput());
					Collections.sort(from26AsJson.getDb_matched());
					if (from26AsJson.getType().equalsIgnoreCase("ManyToMany")
							&& from26AsJson.getDb_matched().equals(from26AsJson.getInput())) {
						from26AsJson.setDb_matched(new ArrayList<>(new HashSet<>(from26AsJson.getDb_matched())));
						from26AsJson.setInput(new ArrayList<>(new HashSet<>(from26AsJson.getInput())));
						from26AsJson.setType("OneToOne");
					}

					List<Tcs26AsInputDto> tcs26AsMatch = new ArrayList<>();
					List<TcsGstPurchaseRegisterReportDTO> prMatch = new ArrayList<>();

					for (BigDecimal match26As : from26AsJson.getInput()) {
						for (Iterator<Tcs26AsInputDto> iterator = tcs26AsListByDeductor.iterator(); iterator
								.hasNext();) {
							Tcs26AsInputDto tcs26AsInputDto = iterator.next();
							if (tcs26AsInputDto.getAmountPaid().compareTo(match26As) == 0) {
								// tcs26AsMatchedUnMathedMap.computeIfAbsent("Matched", k -> new ArrayList<>())
								// .add(tcs26AsInputDto);
								tcs26AsMatch.add(tcs26AsInputDto);
								iterator.remove();
								break;
							}
						}

					} // end for

					for (BigDecimal matchDb : from26AsJson.getDb_matched()) {
						for (Iterator<TcsGstPurchaseRegisterReportDTO> iterator = gstPrAndRlMatchListByDeductor
								.iterator(); iterator.hasNext();) {
							TcsGstPurchaseRegisterReportDTO tcsGstPurchaseRegisterReportDTO = iterator.next();

							Calendar cal = Calendar.getInstance();
							cal.setTime(tcsGstPurchaseRegisterReportDTO.getDocumentDate());
							int rlMonth = cal.get(Calendar.MONTH) + 1;
							cal.setTime(tcsGstPurchaseRegisterReportDTO.getRlDocumentDate());
							int prMonth = cal.get(Calendar.MONTH) + 1;
							if (!tcsGstPurchaseRegisterReportDTO.getSupplyType().equalsIgnoreCase("CAN")
									&& !tcsGstPurchaseRegisterReportDTO.getHsnOrSac().startsWith("99") && rlMonth >= 10
									&& prMonth >= 10) {
								if (tcsGstPurchaseRegisterReportDTO.getInvoiceValue().compareTo(matchDb) == 0) {
									// gstPrAndRlMatchedUnMathedMap.computeIfAbsent("Matched", k -> new
									// ArrayList<>())
									// .add(tcsGstPurchaseRegisterReportDTO);
									prMatch.add(tcsGstPurchaseRegisterReportDTO);
									iterator.remove();
									break;
								}
							}
						}

					} // end for
					if (from26AsJson.getType().equalsIgnoreCase("OneToOne")) {
						if(prMatch.size() > 0 && tcs26AsMatch.size() > 0) {
							setFinal26AsOneToOneData(form26ASFinalResultFileReportDTOList, tcs26AsMatch.get(0),
									prMatch.get(0), from26AsJson.getType());
						}
					} else if (from26AsJson.getType().equalsIgnoreCase("OneToMany")) {
						if(prMatch.size() > 0 && tcs26AsMatch.size() > 0) {
							setFinal26AsOneToManyData(form26ASFinalResultFileReportDTOList, tcs26AsMatch.get(0), prMatch,
									from26AsJson.getType());
						}
					} else if (from26AsJson.getType().equalsIgnoreCase("ManyToOne")) {
						if(prMatch.size() > 0 && tcs26AsMatch.size() > 0) {
							setFinal26AsManyToOneData(form26ASFinalResultFileReportDTOList, tcs26AsMatch, prMatch.get(0),
									from26AsJson.getType());
						}
					} else if (from26AsJson.getType().equalsIgnoreCase("ManyToMany")) {
						if(prMatch.size() > 0 && tcs26AsMatch.size() > 0) {
							for (Tcs26AsInputDto t : tcs26AsMatch) {
								setFinal26AsOneToOneData(form26ASFinalResultFileReportDTOList, t,
										new TcsGstPurchaseRegisterReportDTO(), from26AsJson.getType());
							}
							for (TcsGstPurchaseRegisterReportDTO pr : prMatch) {
								setFinal26AsOneToOneData(form26ASFinalResultFileReportDTOList, new Tcs26AsInputDto(), pr,
										from26AsJson.getType());
							}
						}
					}

				}
			}

			// 2 unmachted lists
			if (tcs26AsListByDeductor != null && !tcs26AsListByDeductor.isEmpty()) {
				for (Tcs26AsInputDto tcs26As : tcs26AsListByDeductor) {
					Form26ASFileReportDTO form26As = new Form26ASFileReportDTO();
					form26As.setNameAsPer26As(tcs26As.getCollectorName());
					form26As.setNameAsPerTRACES(tcs26As.getCollectorName());
					form26As.setPan(collectorPan);
					form26As.setTan(tcs26As.getCollectorTan());
					form26As.setTaxCollected(tcs26As.getTdsDeposited());
					form26As.setTransactionValue(tcs26As.getAmountPaid());
					form26ASFileReportList.add(form26As);
				}
			}
			if (gstPrAndRlMatchListByDeductor != null && !gstPrAndRlMatchListByDeductor.isEmpty()) {
				for (TcsGstPurchaseRegisterReportDTO tcsGstPr : gstPrAndRlMatchListByDeductor) {
					GstPrFileReportDTO gstPr = new GstPrFileReportDTO();
					gstPr.setNameAsPerTraces(tcsGstPr.getSupplierName());
					gstPr.setNameOfSupplier(tcsGstPr.getSupplierName());
					gstPr.setSalesTurnover(tcsGstPr.getInvoiceValue());
					gstPr.setTaxPaidTCSReceivableLedger(tcsGstPr.getTcsAmount());
					gstPrFileReportList.add(gstPr);
				}
			}
		}

		// generate final result report excel
		getForm26ASFinalResultReport(collectorTan, assessmentYear, collectorPan, tenantId, userName,
				form26ASFileReportList, gstPrFileReportList, form26ASFinalResultFileReportDTOList);

		batchUpload.setMismatchCount(0L);
		batchUpload.setModifiedBy(userName);
		batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
		batchUpload.setRowsCount(Long.valueOf(processedRecords));
		batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
		batchUpload.setProcessed(processedRecords);
		batchUpload.setStatus("Processed");
		return tcsMismatchService.update(batchUpload);
	}

	private void setFinal26AsManyToOneData(Set<Form26ASFinalResultFileReportDTO> form26asFinalResultFileReportDTOList,
			List<Tcs26AsInputDto> list, TcsGstPurchaseRegisterReportDTO tcsGstPurchaseRegisterReportDTO, String type) {
		for (Tcs26AsInputDto tcs26AsInputDto : list) {
			Form26ASFinalResultFileReportDTO form26ASFinalResultFileReportDTO = new Form26ASFinalResultFileReportDTO();
			form26ASFinalResultFileReportDTO.setDocumentNoGSTPR(tcsGstPurchaseRegisterReportDTO.getDocumentNumber());
			form26ASFinalResultFileReportDTO
					.setPurchaseVoucherNoGSTPR(tcsGstPurchaseRegisterReportDTO.getPurchaseVoucherNumber());
			form26ASFinalResultFileReportDTO.setSectionCode26AS(tcs26AsInputDto.getSection());
			form26ASFinalResultFileReportDTO.setSupplierName26AS(tcs26AsInputDto.getCollectorName());
			form26ASFinalResultFileReportDTO.setSupplierNameGSTPR(tcsGstPurchaseRegisterReportDTO.getSupplierName());
			form26ASFinalResultFileReportDTO.setTaxableAmountGSTPR(tcsGstPurchaseRegisterReportDTO.getTaxableValue());
			form26ASFinalResultFileReportDTO.setTcs26AS(tcs26AsInputDto.getTdsDeposited());
			form26ASFinalResultFileReportDTO.setTcsTCSRL(tcsGstPurchaseRegisterReportDTO.getAmountInLocalCurrency());
			form26ASFinalResultFileReportDTO
					.setTransactionAmount26AS(tcs26AsInputDto.getAmountPaid());
			form26ASFinalResultFileReportDTO
					.setTransactionAmountGSTPR(tcsGstPurchaseRegisterReportDTO.getInvoiceValue());
			form26ASFinalResultFileReportDTO
					.setTransactionDate26AS(tcs26AsInputDto.getTransactionDate());
			form26ASFinalResultFileReportDTO
					.setTransactionDateGSTPR(tcsGstPurchaseRegisterReportDTO.getPurchaseVoucherDate());
			form26ASFinalResultFileReportDTO.setType(type);
			form26asFinalResultFileReportDTOList.add(form26ASFinalResultFileReportDTO);
		}

	}

	private void setFinal26AsOneToManyData(Set<Form26ASFinalResultFileReportDTO> form26ASFinalResultFileReportDTOList,
			Tcs26AsInputDto tcs26AsInputDto, List<TcsGstPurchaseRegisterReportDTO> list, String type) {

		for (TcsGstPurchaseRegisterReportDTO tcsGstPurchaseRegisterReportDTO : list) {
			Form26ASFinalResultFileReportDTO form26ASFinalResultFileReportDTO = new Form26ASFinalResultFileReportDTO();
			form26ASFinalResultFileReportDTO.setDocumentNoGSTPR(tcsGstPurchaseRegisterReportDTO.getDocumentNumber());
			form26ASFinalResultFileReportDTO
					.setPurchaseVoucherNoGSTPR(tcsGstPurchaseRegisterReportDTO.getPurchaseVoucherNumber());
			form26ASFinalResultFileReportDTO.setSectionCode26AS(tcs26AsInputDto.getSection());
			form26ASFinalResultFileReportDTO.setSupplierName26AS(tcs26AsInputDto.getCollectorName());
			form26ASFinalResultFileReportDTO.setSupplierNameGSTPR(tcsGstPurchaseRegisterReportDTO.getSupplierName());
			form26ASFinalResultFileReportDTO.setTaxableAmountGSTPR(tcsGstPurchaseRegisterReportDTO.getTaxableValue());
			form26ASFinalResultFileReportDTO.setTcs26AS(tcs26AsInputDto.getTdsDeposited());
			form26ASFinalResultFileReportDTO.setTcsTCSRL(tcsGstPurchaseRegisterReportDTO.getAmountInLocalCurrency());
			form26ASFinalResultFileReportDTO
					.setTransactionAmount26AS(tcs26AsInputDto.getAmountPaid());
			form26ASFinalResultFileReportDTO
					.setTransactionAmountGSTPR(tcsGstPurchaseRegisterReportDTO.getInvoiceValue());
			form26ASFinalResultFileReportDTO
					.setTransactionDate26AS(tcs26AsInputDto.getTransactionDate());
			form26ASFinalResultFileReportDTO
					.setTransactionDateGSTPR(tcsGstPurchaseRegisterReportDTO.getPurchaseVoucherDate());
			form26ASFinalResultFileReportDTO.setType(type);
			form26ASFinalResultFileReportDTOList.add(form26ASFinalResultFileReportDTO);
		}

	}

	private void setFinal26AsOneToOneData(Set<Form26ASFinalResultFileReportDTO> form26ASFinalResultFileReportDTOList,
			Tcs26AsInputDto tcs26AsInputDto, TcsGstPurchaseRegisterReportDTO tcsGstPurchaseRegisterReportDTO,
			String type) {
		Form26ASFinalResultFileReportDTO form26ASFinalResultFileReportDTO = new Form26ASFinalResultFileReportDTO();
		form26ASFinalResultFileReportDTO.setDocumentNoGSTPR(tcsGstPurchaseRegisterReportDTO.getDocumentNumber());
		form26ASFinalResultFileReportDTO
				.setPurchaseVoucherNoGSTPR(tcsGstPurchaseRegisterReportDTO.getPurchaseVoucherNumber());
		form26ASFinalResultFileReportDTO.setSectionCode26AS(tcs26AsInputDto.getSection());
		form26ASFinalResultFileReportDTO.setSupplierName26AS(tcs26AsInputDto.getCollectorName());
		form26ASFinalResultFileReportDTO.setSupplierNameGSTPR(tcsGstPurchaseRegisterReportDTO.getSupplierName());
		form26ASFinalResultFileReportDTO.setTaxableAmountGSTPR(tcsGstPurchaseRegisterReportDTO.getTaxableValue());
		form26ASFinalResultFileReportDTO.setTcs26AS(tcs26AsInputDto.getTdsDeposited());
		form26ASFinalResultFileReportDTO.setTcsTCSRL(tcsGstPurchaseRegisterReportDTO.getAmountInLocalCurrency());
		form26ASFinalResultFileReportDTO.setTransactionAmount26AS(tcs26AsInputDto.getAmountPaid());
		form26ASFinalResultFileReportDTO.setTransactionAmountGSTPR(tcsGstPurchaseRegisterReportDTO.getInvoiceValue());
		form26ASFinalResultFileReportDTO
				.setTransactionDate26AS(tcs26AsInputDto.getTransactionDate());
		form26ASFinalResultFileReportDTO
				.setTransactionDateGSTPR(tcsGstPurchaseRegisterReportDTO.getPurchaseVoucherDate());
		form26ASFinalResultFileReportDTO.setType(type);
		form26ASFinalResultFileReportDTOList.add(form26ASFinalResultFileReportDTO);
	}

	public void getForm26ASFinalResultReport(String collectorTan, Integer assesmentYear, String collectorPan,
			String tenantId, String userName, Set<Form26ASFileReportDTO> form26asFileReportList,
			Set<GstPrFileReportDTO> gstPrFileReportList,
			Set<Form26ASFinalResultFileReportDTO> form26asFinalResultFileReportDTOList) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		TCSBatchUpload tcsBatchUplod = tcsMismatchService.saveMismatchReport(collectorTan, tenantId, assesmentYear,
				null, 0L, UploadTypes.FORM_26AS_RECONCILIATION_REPORT.name(), "uploaded", month, userName, null);

		// get form 26AS data list
		Set<Form26ASFileReportDTO> form26AsList = form26asFileReportList;

		// get gst pr data list
		Set<GstPrFileReportDTO> gstPrList = gstPrFileReportList;

		// get form 26AS final result list
		Set<Form26ASFinalResultFileReportDTO> from26ASFinalResultList = form26asFinalResultFileReportDTOList;

		Workbook workbook = new Workbook();
		// Appearing in 26AS, not in GSTPR
		Worksheet worksheet1 = workbook.getWorksheets().get(0);
		worksheet1.setName("Appearing in 26AS, not in GSTPR");
		// Appearing in GSTPR, not in 26AS
		Worksheet worksheet2 = workbook.getWorksheets().add("Appearing in GSTPR, not in 26AS");
		// Form26AS Final Result
		Worksheet worksheet3 = workbook.getWorksheets().add("Form26AS Final Result");

		worksheet1.getCells().importArray(form26AsHeaders, 0, 0, false);
		worksheet2.getCells().importArray(gstPrHeaders, 0, 0, false);
		worksheet3.getCells().importArray(form26ASFinalResultHeaders, 0, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForForm26AS(form26AsList, worksheet1);
		setExtractDataForGstPrList(gstPrList, worksheet2);
		setExtractDataForForm26ASFinalResult(from26ASFinalResultList, worksheet3);

		// Style for A1 to G1 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(164, 160, 160));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet1.getCells().createRange("A1:G1");
		headerColorRange1.setStyle(style1);

		// Style for A1 to D1 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(164, 160, 160));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet2.getCells().createRange("A1:D1");
		headerColorRange2.setStyle(style2);

		// Style for A1 to M1 headers
		Style style3 = workbook.createStyle();
		style3.setForegroundColor(Color.fromArgb(164, 160, 160));
		style3.setPattern(BackgroundType.SOLID);
		style3.getFont().setBold(true);
		style3.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange3 = worksheet3.getCells().createRange("A1:N1");
		headerColorRange3.setStyle(style3);

		worksheet1.autoFitColumns();
		worksheet1.autoFitRows();

		worksheet2.autoFitColumns();
		worksheet2.autoFitRows();

		worksheet3.autoFitColumns();
		worksheet3.autoFitRows();

		workbook.save(out, SaveFormat.XLSX);

		tcsMismatchService.saveMismatchReport(collectorTan, tenantId, assesmentYear, out, 0L,
				UploadTypes.FORM_26AS_RECONCILIATION_REPORT.name(), "processed", month, userName,
				tcsBatchUplod.getId());
	}

	public List<From26AsJson> form26AsLogicApi(List<BigDecimal> tcs26AsList, List<BigDecimal> prList, String token,
			String tan, String pan) throws JsonProcessingException, URISyntaxException, KeyManagementException,
			KeyStoreException, NoSuchAlgorithmException {

		// String url = "https://f62870c63fdb.ngrok.io" +
		// "/api/flask/form26as/relations";

		String url = applicationUrl + "/api/flask/form26as/relations";

		URI uri = new URI(url);

		RestTemplate restTemplate = getRestTemplate();

		HttpHeaders headers = new HttpHeaders();
		// headers.add("Authorization", token);
		headers.add("Content-Type", "application/json");
		// headers.add("TAN-NUMBER", tan);
		// headers.add("DEDUCTOR-PAN", pan);

		From26AsInput from26AsInput = new From26AsInput();
		from26AsInput.setList1(tcs26AsList);
		from26AsInput.setList2(prList);
		HttpEntity<From26AsInput> request = new HttpEntity<>(from26AsInput, headers);

		ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		/*
		 * String body =
		 * "[{\"type\": \"OneToOne\", \"input\": [189504.0], \"db_matched\":" +
		 * " [189504.0]}, {\"type\": \"OneToOne\", \"input\": [356370.0], " +
		 * "\"db_matched\": [356370.0]}, {\"type\": \"OneToOne\", \"input\":" +
		 * " [436590.0], \"db_matched\": [436590.0]}, {\"type\": \"OneToOne\", " +
		 * "\"input\": [577038.0], \"db_matched\": [577038.0]}, {\"type\": \"ManyToMany\", "
		 * +
		 * "\"input\": [356370.0, 356370.0], \"db_matched\": [356370.0, 356370.0]}, {\"type\": \"ManyToMany\","
		 * + " \"input\": [436590.0, 436590.0], \"db_matched\": [436590.0, 436590.0]}]";
		 */

		logger.info("Response from 26AS python API {}", result.getBody());
		List<From26AsJson> runStatus = objectMapper.readValue(result.getBody(),
				new TypeReference<List<From26AsJson>>() {
				});

		return runStatus;
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
}
