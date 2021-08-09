package com.ey.in.tds.ingestion.service.invoicemismatch;

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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
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
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.Color;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.LdcMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.dto.CustomSectionRateDTO;
import com.ey.in.tds.common.dto.CustomThresholdGroupLimitDto;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.model.job.NoteBookParam;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeDetailsDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMasterDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.config.SparkNotebooks;
import com.ey.in.tds.ingestion.dto.invoice.InvoiceMismatchDTO;
import com.ey.in.tds.ingestion.jdbc.dao.AdvanceDAO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceLineItemDAO;
import com.ey.in.tds.ingestion.service.advance.SparkNotebookService;
import com.ey.in.tds.ingestion.service.invoicelineitem.NonResidentService;
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
public class InvoiceMismatchService extends TdsMismatchService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private InvoiceLineItemDAO invoiceLineItemDAO;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private AdvanceDAO advanceDAO;

	@Value("${application.url}")
	private String applicationUrl;

	@Autowired
	private NonResidentService invoiceNonResidentService;

	@Autowired
	private SparkNotebookService sparkNotebookService;

	@Autowired
	private SparkNotebooks sparkNotebooks;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private LdcMasterDAO ldcMasterDAO;

	/**
	 * Update InvoiceMismatchUpadteDTO for remediation report
	 * 
	 * @param invoiceMismatchUpdateDTO
	 * @throws RecordNotFoundException
	 */
	public InvoiceMismatchDTO updateMismatchByActionForPdf(String tan, InvoiceMismatchDTO invoiceMismatchUpdateDTO)
			throws RecordNotFoundException {
		logger.info("SEQUENCE NUMBER : {}", invoiceMismatchUpdateDTO.getSequenceNumber());
		logger.info("Batch ID  : {} ", invoiceMismatchUpdateDTO.getBatchId());
		logger.info("Line Item ID : {}", invoiceMismatchUpdateDTO.getLineItemId());
		List<InvoiceLineItem> invoiceLineItem = invoiceLineItemDAO.findByYearTanDocumentPostingDateIdActive(
				Calendar.getInstance().get(Calendar.YEAR), tan, invoiceMismatchUpdateDTO.getDocumentPostingDate(),
				invoiceMismatchUpdateDTO.getLineItemId(), false);
		if (!invoiceLineItem.isEmpty()) {
			invoiceLineItem.get(0).setFinalReason(invoiceMismatchUpdateDTO.getActionReason());
			invoiceLineItem.get(0).setFinalTdsRate(invoiceMismatchUpdateDTO.getFinalTdsRate());
			invoiceLineItem.get(0).setFinalTdsSection(invoiceMismatchUpdateDTO.getFinalTdsSection());

			// invoiceLineItemRepository.save(invoiceLineItem.get());
			invoiceLineItemDAO.update(invoiceLineItem.get(0));
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
	public BatchUpload importToBatchUpload(MultipartFile file, String tenantId, String userName)
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
	@Async
	public ByteArrayInputStream asyncExportRemediationReport(String tan, String tenantId, String deductorPan, int year,
			int month, String userName, boolean isMismatch)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		return exportRemediationReport(tan, tenantId, deductorPan, year, month, userName, isMismatch);
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
			fileName = UploadTypes.INVOICE_REPORT.name().toLowerCase() + "_"
					+ CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		} else {
			fileName = UploadTypes.INVOICE_MISMATCH_REPORT.name().toLowerCase() + "_"
					+ CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		}
		BatchUpload batchUpload = saveMismatchReport(tan, tenantId, year, out, 0L,
				UploadTypes.INVOICE_MISMATCH_REPORT.name(), "Processing", month, userName, null, fileName);
		String type = "invoice_mismatch";
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
	 * @throws ParseException 
	 */
	public ByteArrayInputStream exportRemediationReport1(String tan, String tenantId, String deductorPan, int year,
			int month, String userName) throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();
		String fileName = UploadTypes.INVOICE_MISMATCH_REPORT.name() + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		BatchUpload batchUpload = saveMismatchReport(tan, tenantId, year, null, 0L,
				UploadTypes.INVOICE_MISMATCH_REPORT.name(), "Processing", month, userName, null, fileName);
		try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
			SXSSFSheet sheet = wb.createSheet("Invoice_Remediation_Report");
			sheet.setColumnHidden(20, true);
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
			String msg = getErrorReportMsg(tan, tenantId, deductorPan);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);
			sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, 20));

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
			row03.createCell(23).setCellValue("Client Response");
			row03.getCell(23).setCellStyle(style03);
			CellRangeAddress cellRangeAddress4 = new CellRangeAddress(3, 3, 23, 27);
			sheet.addMergedRegion(cellRangeAddress4);

			SXSSFRow row1 = sheet.createRow(4);
			sheet.setDefaultColumnWidth(25);
			XSSFCellStyle style0 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style1 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style2 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style3 = (XSSFCellStyle) wb.createCellStyle();
			XSSFCellStyle style003 = (XSSFCellStyle) wb.createCellStyle();
			
			// setting fonts to the style
						XSSFFont font = (XSSFFont) wb.createFont();
						font.setBold(true);
						font.setFontName("Arial");

			// setting border and color
			setMediumBorder(defaultIndexedColorMap, style0, 46, 134, 193,font);
			setMediumBorder(defaultIndexedColorMap, style1, 174, 170, 170,font);
			setMediumBorder(defaultIndexedColorMap, style2, 180, 198, 231,font);
			setMediumBorder(defaultIndexedColorMap, style3, 255, 255, 0,font);
			setMediumBorder(defaultIndexedColorMap, style003, 255, 230, 153,font);

			
			style0.setFont(font);
			style1.setFont(font);
			style2.setFont(font);
			style3.setFont(font);
			style003.setFont(font);

			// Create a cell
			row1.createCell(0).setCellValue("Sequence Number");
			row1.getCell(0).setCellStyle(style0);
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
			row1.createCell(11).setCellValue("Mismatch Interpretation");
			row1.getCell(11).setCellStyle(style1);
			row1.createCell(12).setCellValue("Deductor TAN");
			row1.getCell(12).setCellStyle(style2);
			row1.createCell(13).setCellValue("Deductee PAN");
			row1.getCell(13).setCellStyle(style3);
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
			row1.createCell(21).setCellValue("Confidence");
			row1.getCell(21).setCellStyle(style1);
			row1.createCell(22).setCellValue("ERP Document Number");
			row1.getCell(22).setCellStyle(style3);

			// Mismatch Category
			row1.createCell(23).setCellValue("Action");
			row1.getCell(23).setCellStyle(style1);
			row1.createCell(24).setCellValue("Reason");
			row1.getCell(24).setCellStyle(style1);
			row1.createCell(25).setCellValue("Final TdsRate");
			row1.getCell(25).setCellStyle(style1);
			row1.createCell(26).setCellValue("Final TdsSection");
			row1.getCell(26).setCellStyle(style1);
			row1.createCell(27).setCellValue("Final Amount");
			row1.getCell(27).setCellStyle(style1);

			// Adding hidden columns
			row1.createCell(28).setCellValue("Assessment Year");
			row1.getCell(28).setCellStyle(style1);
			row1.createCell(29).setCellValue("Document Posting Date");
			row1.getCell(29).setCellStyle(style1);

			sheet.setColumnHidden(28, true);
			sheet.setColumnHidden(29, true);

			// Auto Filter Option
			sheet.setAutoFilter(new CellRangeAddress(4, 25, 0, 27));
			sheet.createFreezePane(0, 1);

			// sheet.createFreezePane(colSplit, rowSplit, leftmostColumn, topRow)-this
			// method is used for scrolling rows and column vertically and horizontally
			sheet.createFreezePane(3, 5, 5, 5);

			List<InvoiceLineItem> invoiceMismatchList = invoiceLineItemDAO.getAllInvoiceMismatchesPage(tan.trim(), year,
					month, Pagination.UNPAGED);
			long size = invoiceMismatchList.size();

			validationHelper = sheet.getDataValidationHelper();
			CellRangeAddressList addressList = new CellRangeAddressList(4, (int) size + 4, 23, 23);
			constraint = validationHelper
					.createExplicitListConstraint(new String[] { "Accept", "Modify", "Reject", "Cancel" });
			dataValidation = validationHelper.createValidation(constraint, addressList);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.setShowPromptBox(true);
			sheet.addValidationData(dataValidation);

			validationHelper = sheet.getDataValidationHelper();
			CellRangeAddressList addressList2 = new CellRangeAddressList(4, (int) size + 4, 26, 26);
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
			style5.setAlignment(HorizontalAlignment.LEFT);
			setCellColorAndBoarder(defaultIndexedColorMap, style5, 191, 201, 202);

			XSSFCellStyle style4 = (XSSFCellStyle) wb.createCellStyle();
			XSSFFont font4 = (XSSFFont) wb.createFont();
			style4.setFont(font4);
			style4.setLocked(false);
			setCellColorAndBoarder(defaultIndexedColorMap, style5, 255, 255, 255);

			int rowindex = 5;
			if (!invoiceMismatchList.isEmpty()) {
				logger.info("total results size : {}", invoiceMismatchList.size());
				for (InvoiceLineItem listData : invoiceMismatchList) {
					List<String> sections = new ArrayList<>();
					SXSSFRow row2 = sheet.createRow(rowindex++);
					row2.createCell(0).setCellValue(listData.getSequenceNumber());
					row2.getCell(0).setCellStyle(style5);
					row2.createCell(1).setCellValue(listData.getClientAmount().toString());
					row2.getCell(1).setCellStyle(style5);
					row2.createCell(2).setCellValue(listData.getActualTdsSection());
					row2.getCell(2).setCellStyle(style5);
					row2.createCell(3).setCellValue(listData.getActualTdsRate().toString());
					row2.getCell(3).setCellStyle(style5);
					row2.createCell(4).setCellValue(listData.getDerivedTdsAmount().toString());
					row2.getCell(4).setCellStyle(style5);
					row2.createCell(5).setCellValue(listData.getDerivedTdsSection());
					row2.getCell(5).setCellStyle(style5);
					if (listData.getDerivedTdsRate() != null) {
						row2.createCell(6).setCellValue(listData.getDerivedTdsRate().toString());
						row2.getCell(6).setCellStyle(style5);
					}
					row2.createCell(7).setCellValue(
							listData.getInvoiceAmount() == null ? " " : listData.getInvoiceAmount().toString());
					row2.getCell(7).setCellStyle(style5);
					row2.createCell(8).setCellValue(listData.getActualTdsSection());
					row2.getCell(8).setCellStyle(style5);
					row2.createCell(9).setCellValue(listData.getActualTdsRate().toString());
					row2.getCell(9).setCellStyle(style5);
					row2.createCell(10).setCellValue(listData.getMismatchCategory());
					row2.getCell(10).setCellStyle(style5);
					row2.createCell(11).setCellValue(listData.getMismatchInterpretation());
					row2.getCell(11).setCellStyle(style5);
					row2.createCell(12).setCellValue(listData.getDeductorMasterTan());
					row2.getCell(12).setCellStyle(style5);
					row2.createCell(13).setCellValue(listData.getPan());
					row2.getCell(13).setCellStyle(style5);
					row2.createCell(14).setCellValue(listData.getDeducteeName());

					sections = advanceDAO.getVendorSections(listData.getDeducteeKey(), listData.getIsResident());
					row2.createCell(15).setCellValue(sections.size() + "");
					row2.getCell(15).setCellStyle(style5);
					row2.createCell(16).setCellValue(
							sections.isEmpty() ? "No Section" : sections.toString().replace("[", "").replace("]", ""));
					row2.getCell(16).setCellStyle(style5);

					row2.createCell(17).setCellValue(listData.getServiceDescriptionInvoice());
					row2.getCell(17).setCellStyle(style5);
					row2.createCell(18).setCellValue(listData.getServiceDescriptionPo());
					row2.getCell(18).setCellStyle(style5);
					row2.createCell(19).setCellValue(listData.getServiceDescriptionGl());
					row2.getCell(19).setCellStyle(style5);
					row2.createCell(20).setCellValue(listData.getId().toString());
					row2.getCell(20).setCellStyle(style5);
					row2.createCell(21).setCellValue(listData.getConfidence());
					row2.getCell(21).setCellStyle(style5);
					row2.createCell(22).setCellValue(listData.getDocumentNumber());
					row2.getCell(22).setCellStyle(style5);
					// unlocked data
					row2.createCell(23).setCellValue("");
					row2.getCell(23).setCellStyle(style4);
					row2.createCell(24).setCellValue("");
					row2.getCell(24).setCellStyle(style4);
					row2.createCell(25).setCellValue("");
					row2.getCell(25).setCellStyle(style4);
					row2.createCell(26).setCellValue("");
					row2.getCell(26).setCellStyle(style4);
					row2.createCell(27).setCellValue("");
					row2.getCell(27).setCellStyle(style4);

					row2.createCell(28).setCellValue(
							listData.getAssessmentYear() == null ? " " : listData.getAssessmentYear().toString());
					row2.getCell(28).setCellStyle(style4);
					row2.createCell(29).setCellValue(listData.getDocumentPostingDate() == null ? " "
							: listData.getDocumentPostingDate().toString());
					row2.getCell(29).setCellStyle(style4);

				}
				updateMismatchExportReportStatus(invoiceMismatchList.size(), size, batchUpload);
			}
			wb.write(out);
			saveMismatchReport(tan, tenantId, year, out, Long.valueOf(size), UploadTypes.INVOICE_MISMATCH_REPORT.name(),
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
	 * @param token
	 * @param year
	 * @return
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	@Async
	public BatchUpload asyncUpdateRemediationReport(String tan, BatchUpload batchUpload, String path, String tenantId,
			String deductorPan, String token, int year, String userEmail, int month)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		if (batchUpload != null) {
			batchUpload.setFailedCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setRowsCount(0L);
			batchUpload.setStatus("Processing");
			batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
			batchUploadDAO.update(batchUpload);
			batchUpload = updateMismatchRemediationReport(tan, path, tenantId, deductorPan, token, year, batchUpload, userEmail,
					month);
		}
		return batchUpload;
	}

	public BatchUpload updateMismatchRemediationReport(String tan, String path, String tenantId, String deductorPan,
			String token, int year, BatchUpload batchUpload, String userEmail, int month)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String otherFilepath = null;
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

			String errorFilepath = null;
			int processedCount = 0;
			int errorCount = 0;
			List<InvoiceLineItem> invoiceList = new ArrayList<>();
			List<CsvRow> errorList = new ArrayList<>();
			List<String> errorMessages = new ArrayList<>();
			List<Integer> invoiceIds = new ArrayList<>();
			boolean isCancel = false;

			// Rate and section change
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
			//Surcharge details
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
					// Actual
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
					// Action
					String userAction = row.getField("Action");
					// Reason
					String reason = row.getField("Reason");
					// id
					Integer invoiceId = StringUtils.isNotBlank(row.getField("Line  Item Hash Code"))
							? Integer.parseInt(row.getField("Line  Item Hash Code"))
							: null;

					// Amount
					BigDecimal taxableAmount = StringUtils.isNotBlank(row.getField("TaxableValue"))
							? new BigDecimal(row.getField("TaxableValue"))
							: null;

					Integer assessmentYear = StringUtils.isNotBlank(row.getField("Assessment Year"))
							? Integer.parseInt(row.getField("Assessment Year"))
							: null;

					Boolean hasLdc = StringUtils.isNotBlank(row.getField("Is LDC applicable?"))
							? Boolean.parseBoolean(row.getField("Is LDC applicable?"))
							: null;

					String isResidential = row.getField("NRIndicator");

					// Document posting date
					Date documentPostingDate = StringUtils.isNotBlank(row.getField("PostingDate"))
							? new SimpleDateFormat("yyyy-MM-dd").parse(row.getField("PostingDate"))
							: null;

					// nrTransactionsMetaId
					BigDecimal nrTransactionsMetaId = StringUtils.isNotBlank(row.getField("Nr Tansactions Meta Id"))
							? new BigDecimal(row.getField("Nr Tansactions Meta Id"))
							: null;

					Integer challanMonth = StringUtils.isNotBlank(row.getField("Challan Month"))
							? Integer.parseInt(row.getField("Challan Month"))
							: null;

					String deducteePan = row.getField("DeducteePAN");

					String processedFrom = row.getField("Processed From");

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

					Integer invoiceGroupId = StringUtils.isNotBlank(row.getField("GroupId"))
							? Integer.parseInt(row.getField("GroupId"))
							: null;

					Integer invoiceNopId = StringUtils.isNotBlank(row.getField("NopId"))
							? Integer.parseInt(row.getField("NopId"))
							: null;
					String deducteeStatus = StringUtils.isNotBlank(row.getField("DeducteeStatus"))
							? row.getField("DeducteeStatus")
							: StringUtils.EMPTY;
					Boolean isError = false;

					if (StringUtils.isNotBlank(userAction)) {

						BigDecimal finalAmount = new BigDecimal(0);
						Integer nopId = 0;
						Integer nopGroupId = 0;

						if (invoiceId != null) {
							InvoiceLineItem invoiceLineItemData = new InvoiceLineItem();
							invoiceLineItemData.setDocumentPostingDate(documentPostingDate);
							invoiceLineItemData.setId(invoiceId);
							invoiceLineItemData.setFinalReason(reason);
							invoiceLineItemData.setIsResident(isResidential);
							invoiceLineItemData.setAssessmentYear(assessmentYear);
							invoiceLineItemData.setNrTransactionsMetaId(
									nrTransactionsMetaId != null ? nrTransactionsMetaId.intValue() : null);
							invoiceLineItemData.setProcessedFrom(processedFrom);
							invoiceLineItemData.setChallanMonth(challanMonth);
							invoiceLineItemData.setDeductorMasterTan(tan);
							invoiceLineItemData.setDeductorPan(deductorPan);
							invoiceLineItemData.setInvoiceAmount(taxableAmount);
							invoiceLineItemData.setHasLdc(hasLdc);
							invoiceLineItemData.setModifiedBy(userEmail);
							invoiceLineItemData.setPan(deducteePan);
							invoiceLineItemData.setInterest(interest);
							invoiceLineItemData.setCessAmount(cessAmount);
							invoiceLineItemData.setCessRate(cessRate);
							invoiceLineItemData.setSurcharge(surcharge);
							invoiceLineItemData.setActionType(userAction);
							invoiceLineItemData.setIsExempted(false);
							invoiceLineItemData.setGroupId(invoiceGroupId);
							invoiceLineItemData.setInvoiceNpId(invoiceNopId);
							
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
									invoiceLineItemData.setFinalTdsAmount(derivedTdsAmountFromExcel);
									invoiceLineItemData.setFinalTdsSection(derivedTdsSectionFromExcel);
									invoiceLineItemData.setFinalTdsRate(derivedTdsRateFromExcel);
									invoiceLineItemData.setActive(true);
									invoiceLineItemData.setIsMismatch(false);
									if ("IMPG".equalsIgnoreCase(supplyType)) {
										if ("194Q".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
												|| "NOTDS".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
												|| StringUtils.isBlank(invoiceLineItemData.getFinalTdsSection())) {
											invoiceLineItemData.setIsExempted(true);
											invoiceLineItemData
													.setErrorReason("Transaction out of Scope - Import of goods");
										}
									} else if (invoiceLineItemData.getPan()
											.equalsIgnoreCase(invoiceLineItemData.getDeductorPan())) {
										if ("NOTDS".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
												|| StringUtils.isBlank(invoiceLineItemData.getFinalTdsSection())) {
											invoiceLineItemData.setIsExempted(true);
											invoiceLineItemData
													.setErrorReason("Transaction out of Scope - Stock Transfer");
										}
									} else if ("DLC".equalsIgnoreCase(documentType)) {
										if ("NOTDS".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
												|| StringUtils.isBlank(invoiceLineItemData.getFinalTdsSection())) {
											invoiceLineItemData.setIsExempted(true);
											invoiceLineItemData
													.setErrorReason("Transaction out of Scope - Delivery Challan");
										}
									} else if ("NOTDS".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
											|| StringUtils.isBlank(invoiceLineItemData.getFinalTdsSection())) {
										invoiceLineItemData.setIsExempted(true);
										invoiceLineItemData.setErrorReason("Transaction out of Scope - Scrape Sale");
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
									invoiceLineItemData.setFinalReason(reason);
									invoiceLineItemData.setFinalTdsRate(finalTdsRateFromExcel);
									invoiceLineItemData.setFinalTdsSection(finalTdsSectionFromExcel);
									invoiceLineItemData.setActive(true);
									invoiceLineItemData.setIsMismatch(false);
									if (finalTdsAmountFromExcel != null) {
										invoiceLineItemData.setFinalTdsAmount(finalTdsAmountFromExcel);
									} else if ("Modify - TDS base value".equalsIgnoreCase(userAction)) {
										invoiceLineItemData.setClientTaxableAmount(taxableAmount);
										invoiceLineItemData.setInvoiceAmount(tdsBaseValue);
										finalAmount = finalAmount.add(invoiceLineItemData.getFinalTdsRate()
												.multiply(tdsBaseValue).divide(BigDecimal.valueOf(100)));
										invoiceLineItemData.setFinalTdsAmount(finalAmount);
									} else if ("Modify - Taxable value".equalsIgnoreCase(userAction)) {
										finalAmount = finalAmount.add(invoiceLineItemData.getFinalTdsRate()
												.multiply(taxableAmount).divide(BigDecimal.valueOf(100)));
										invoiceLineItemData.setFinalTdsAmount(finalAmount);
									} else if ("Modify - Any other amount".equalsIgnoreCase(userAction)) {
										invoiceLineItemData.setClientTaxableAmount(taxableAmount);
										invoiceLineItemData.setInvoiceAmount(anyOtherAmount);
										finalAmount = finalAmount.add(invoiceLineItemData.getFinalTdsRate()
												.multiply(anyOtherAmount).divide(BigDecimal.valueOf(100)));
										invoiceLineItemData.setFinalTdsAmount(finalAmount);
									}
									if ("IMPG".equalsIgnoreCase(supplyType)) {
										if ("194Q".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
												|| "NOTDS".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
												|| StringUtils.isBlank(invoiceLineItemData.getFinalTdsSection())) {
											invoiceLineItemData.setIsExempted(true);
											invoiceLineItemData
													.setErrorReason("Transaction out of Scope - Import of goods");
										}
									} else if (invoiceLineItemData.getPan()
											.equalsIgnoreCase(invoiceLineItemData.getDeductorPan())) {
										if ("NOTDS".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
												|| StringUtils.isBlank(invoiceLineItemData.getFinalTdsSection())) {
											invoiceLineItemData.setIsExempted(true);
											invoiceLineItemData
													.setErrorReason("Transaction out of Scope - Stock Transfer");
										}
									} else if ("DLC".equalsIgnoreCase(documentType)) {
										if ("NOTDS".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
												|| StringUtils.isBlank(invoiceLineItemData.getFinalTdsSection())) {
											invoiceLineItemData.setIsExempted(true);
											invoiceLineItemData
													.setErrorReason("Transaction out of Scope - Delivery Challan");
										}
									} else if ("NOTDS".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
											|| StringUtils.isBlank(invoiceLineItemData.getFinalTdsSection())) {
										invoiceLineItemData.setIsExempted(true);
										invoiceLineItemData.setErrorReason("Transaction out of Scope - Scrape Sale");
									}
								} else {
									isError = true;
									errorCount++;
									errorList.add(row);
									String message = StringUtils.EMPTY;
									if (StringUtils.isEmpty(finalTdsSectionFromExcel)) {
										message = " Final TDS Section is empty " + "\n";
									}
									if (finalTdsRateFromExcel == null) {
										message = message + " Final TDS Rate is empty " + "\n";
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
									invoiceLineItemData.setFinalTdsSection(actualTdsSectionFromExcel);
									invoiceLineItemData.setFinalTdsRate(actualTdsRateFromExcel);
									invoiceLineItemData.setFinalTdsAmount(actualTdsAmountFromExcel);
									invoiceLineItemData.setActive(true);
									invoiceLineItemData.setIsMismatch(false);
									if ("IMPG".equalsIgnoreCase(supplyType)) {
										if ("194Q".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
												|| "NOTDS".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
												|| StringUtils.isBlank(invoiceLineItemData.getFinalTdsSection())) {
											invoiceLineItemData.setIsExempted(true);
											invoiceLineItemData
													.setErrorReason("Transaction out of Scope - Import of goods");
										}
									} else if (invoiceLineItemData.getPan()
											.equalsIgnoreCase(invoiceLineItemData.getDeductorPan())) {
										if ("NOTDS".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
												|| StringUtils.isBlank(invoiceLineItemData.getFinalTdsSection())) {
											invoiceLineItemData.setIsExempted(true);
											invoiceLineItemData
													.setErrorReason("Transaction out of Scope - Stock Transfer");
										}
									} else if ("DLC".equalsIgnoreCase(documentType)) {
										if ("NOTDS".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
												|| StringUtils.isBlank(invoiceLineItemData.getFinalTdsSection())) {
											invoiceLineItemData.setIsExempted(true);
											invoiceLineItemData
													.setErrorReason("Transaction out of Scope - Delivery Challan");
										}
									} else if ("NOTDS".equalsIgnoreCase(invoiceLineItemData.getFinalTdsSection())
											|| StringUtils.isBlank(invoiceLineItemData.getFinalTdsSection())) {
										invoiceLineItemData.setIsExempted(true);
										invoiceLineItemData.setErrorReason("Transaction out of Scope - Scrape Sale");
									}
								} else {
									isError = true;
									errorCount++;
									errorList.add(row);
									if (StringUtils.isEmpty(actualTdsSectionFromExcel)) {
										message = " Actual TDS Section is empty " + "\n";
									}
									if (actualTdsRateFromExcel == null) {
										message = message + " Actual TDS Rate is empty " + "\n";
									}
									if (actualTdsAmountFromExcel == null) {
										message = message + " Actual TDS Amount is empty " + "\n";
									}
								}
								errorMessages.add(message);
							} else if ("Cancel".equalsIgnoreCase(userAction)) {
								invoiceLineItemData.setActive(false);
								invoiceLineItemData.setIsMismatch(false);
								isCancel = true;
								invoiceLineItemData.setFinalReason("USER REQUESTED TO CANCEL");
								invoiceLineItemData.setErrorReason("Canceled record-ERR029");
								invoiceIds.add(invoiceId);
							} else {
								isError = true;
								errorCount++;
								errorList.add(row);
							}
							if (!"Cancel".equalsIgnoreCase(userAction) && !"Accept".equalsIgnoreCase(userAction)) {
								if (ratesMap != null
										&& ratesMap.get(invoiceLineItemData.getFinalTdsSection()) != null) {
									Double closestRate = closest(invoiceLineItemData.getFinalTdsRate().doubleValue(),
											ratesMap.get(invoiceLineItemData.getFinalTdsSection()));
									BigInteger nopIdInt = sectionRateNopId
											.get(invoiceLineItemData.getFinalTdsSection() + "-" + closestRate);
									nopId = nopIdInt.compareTo(BigInteger.ZERO) > 0 ? nopIdInt.intValue() : 0;
								}
								if (nopId != null) {
									invoiceLineItemData.setInvoiceNpId(nopId);
									nopGroupId = nopGroupMap.get(nopId);
								}
								if (nopGroupId != null) {
									invoiceLineItemData.setGroupId(nopGroupId);
								}
							}
							if (!isError) {
								if (UploadTypes.INVOICE_NR_EXCEL.name()
										.equalsIgnoreCase(invoiceLineItemData.getProcessedFrom())
										&& !"Cancel".equalsIgnoreCase(userAction)
										&& !"ACCEPT".equalsIgnoreCase(userAction)
										&& invoiceLineItemData.getHasDtaa() != null
										&& invoiceLineItemData.getHasDtaa().equals(false)) {
									invoiceLineItemData.setSurcharge(invoiceNonResidentService.surchargeCalculation(
											invoiceLineItemData.getFinalTdsSection(),
											invoiceLineItemData.getFinalTdsAmount(),
											invoiceLineItemData.getInvoiceAmount(), deducteeStatus, surchargeMap));
									cessAmount = invoiceLineItemData.getSurcharge()
											.add(invoiceLineItemData.getFinalTdsAmount());
									invoiceLineItemData.setCessAmount(
											cessAmount.multiply(cessRate).divide(BigDecimal.valueOf(100)));
								}
								invoiceList.add(invoiceLineItemData);
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

			// batch update
			if (!invoiceList.isEmpty()) {
				invoiceLineItemDAO.batchUpdateInvoiceMismatch(invoiceList);
			}

			if (isCancel) {
				MultipartFile cancelledInvoicesFile = generateCancelledInvoiceExcell(invoiceIds, deductorPan, tan);
				otherFilepath = blob.uploadExcelToBlob(cancelledInvoicesFile);
			}
			if (errorCount > 0 && !errorList.isEmpty()) {
				logger.info("invoice remediation error records size: {}", errorList.size());
				ByteArrayOutputStream bytes = generateMismatchErrorReport(errorList, tan, tenantId, deductorPan,
						errorMessages);
				if (bytes.size() != 0) {
					errorFilepath = sendFileToBlobStorage(bytes, tenantId);
				}
			}
			if (batchUpload.getBatchUploadID() != null) {
				batchUpload.setFailedCount(Long.valueOf(errorCount));
				batchUpload.setProcessedCount(processedCount);
				batchUpload.setErrorFilePath(errorFilepath);
				batchUpload.setRowsCount((long) processedCount + errorCount);
				batchUpload.setStatus("Processed");
				batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setOtherFileUrl(otherFilepath);
				batchUploadDAO.update(batchUpload);
			}
		} catch (Exception e1) {
			logger.error("Exception occurred while updating remediation invoice report {}", e1.getMessage());
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

	private String sendFileToBlobStorage(ByteArrayOutputStream out, String tenantId)
			throws IOException, URISyntaxException, InvalidKeyException, StorageException {
		File file = getConvertedExcelFile(out);
		logger.info("Original file   : {}", file.getName());
		String paths = blob.uploadExcelToBlobWithFile(file, tenantId);
		return paths;
	}

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
	
	public String getErrorReportMsg(String deductorMasterTan, String tenantId, String deductorPan) {
		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return "Invoice Remediation Report (Dated: " + date + ")\n Client Name: " + deductorData.getDeductorName()
				+ "\n";
	}

	public MultipartFile generateCancelledInvoiceExcell(List<Integer> invoiceIds, String deductorPan, String tan) {

		MultipartFile multipartFile = null;
		String[] invoiceHeadersFile = new String[] { "Source File Name", "Name of the Company Code", "Document Type",
				"Supply Type", "Deductor TAN ", "HSN/SAC", "Deductee PAN", "Deductee TIN", "Deductee GSTIN",
				"Name of the Deductee", "Vendor Invoice Number ", "Taxable value", "TDS Section", "TDS Rate",
				"TDS Amount", "Service Description - Invoice", "Service Description – PO",
				"Service Description - GL Text", "SAC Description", "Deductor GSTIN", "Non-Resident Deductee Indicator",
				"Deductor PAN", "Deductee Address", "ERP Document Number", "MIRO Number", "MIGO Number",
				"Document Number", "Document Date", "Company Code", "Deductee Code", "Posting Date of Document",
				"PaymentDate", "TDSDeductionDate", "Line Item Number", "IGST Rate", "IGST Amount", "CGST Rate",
				"CGST Amount", "SGST Rate", "SGST Amount", "Cess Rate ", "Cess Amount", "Creditable (Y/N)",
				"Section code", "POS", "PO number", "PO date", "Linked advance Number", "Grossing up Indicator",
				"Original Document Number", "Original Document Date", "User Defined Field 1", "User Defined Field 2",
				"User Defined Field 3", "Challan Paid", "Challan Generated Date", "Provision Can Adjust",
				"Advance Can Adjust" };
		try {
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			worksheet.setName("Imported Invoice");
			worksheet.autoFitColumns();
			worksheet.autoFitRows();
			worksheet.getCells().importArray(invoiceHeadersFile, 0, 0, false);
			List<InvoiceLineItem> cancelList = invoiceLineItemDAO.getInvoiceList(tan, deductorPan, invoiceIds);
			logger.info("invoice cancelList size : {}", cancelList.size());
			setInvoiceHeaders(cancelList, worksheet);

			worksheet.autoFitColumns();

			// Style for E1 to BC1 headers
			Style style5 = workbook.createStyle();
			style5.setForegroundColor(Color.fromArgb(91, 155, 213));
			style5.setPattern(BackgroundType.SOLID);
			style5.getFont().setBold(true);
			style5.setHorizontalAlignment(TextAlignmentType.CENTER);
			Range headerColorRange1 = worksheet.getCells().createRange("A1:BF1");
			headerColorRange1.setStyle(style5);

			// Creating AutoFilter by giving the cells range
			AutoFilter autoFilter = worksheet.getAutoFilter();
			autoFilter.setRange("A1:BF1");

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

	public void setInvoiceHeaders(List<InvoiceLineItem> cancelList, Worksheet worksheet) throws Exception {
		int rowIndex = 1;
		for (InvoiceLineItem canceledInvoice : cancelList) {
			List<Object> rowData = new ArrayList<>();
			rowData.add(StringUtils.isBlank(canceledInvoice.getSourceFileName()) ? StringUtils.EMPTY // Source File Name
					: canceledInvoice.getSourceFileName());
			rowData.add(StringUtils.isBlank(canceledInvoice.getCompanyCode()) ? StringUtils.EMPTY // Name of the Company
					: canceledInvoice.getCompanyCode());
			rowData.add(StringUtils.isBlank(canceledInvoice.getDocumentType()) ? StringUtils.EMPTY // Document Type
					: canceledInvoice.getDocumentType());
			rowData.add(StringUtils.isBlank(canceledInvoice.getSupplyType()) ? StringUtils.EMPTY // Document Type
					: canceledInvoice.getSupplyType());
			rowData.add(StringUtils.isBlank(canceledInvoice.getDeductorMasterTan()) ? StringUtils.EMPTY // Deductor TAN
					: canceledInvoice.getDeductorMasterTan());
			rowData.add(StringUtils.isBlank(canceledInvoice.getHsnSacCode()) ? StringUtils.EMPTY // HSN/SAC
					: canceledInvoice.getHsnSacCode());
			rowData.add(StringUtils.isBlank(canceledInvoice.getPan()) ? StringUtils.EMPTY // Deductee PAN
					: canceledInvoice.getPan());
			rowData.add(StringUtils.isBlank(canceledInvoice.getDeducteeTin()) ? StringUtils.EMPTY // Deductee TIN
					: canceledInvoice.getDeducteeTin());
			rowData.add(StringUtils.isBlank(canceledInvoice.getGstin()) ? StringUtils.EMPTY // Deductee GSTIN
					: canceledInvoice.getGstin());
			rowData.add(StringUtils.isBlank(canceledInvoice.getDeducteeName()) ? StringUtils.EMPTY // Name of the
					: canceledInvoice.getDeducteeName());
			rowData.add(StringUtils.isBlank(canceledInvoice.getVendorInvoiceNumber()) ? StringUtils.EMPTY // Vendor
					: canceledInvoice.getVendorInvoiceNumber());
			rowData.add(canceledInvoice.getInvoiceAmount() == null ? StringUtils.EMPTY // Taxable value
					: canceledInvoice.getInvoiceAmount());
			rowData.add(StringUtils.isBlank(canceledInvoice.getTdsSection()) ? StringUtils.EMPTY // TDS Section"
					: canceledInvoice.getTdsSection());
			rowData.add(canceledInvoice.getTdsRate() == null ? StringUtils.EMPTY // TDS Rate
					: canceledInvoice.getTdsRate());
			rowData.add(canceledInvoice.getTdsAmount() == null ? StringUtils.EMPTY // TDS Amount
					: canceledInvoice.getTdsAmount());
			rowData.add(StringUtils.isBlank(canceledInvoice.getServiceDescriptionInvoice()) ? StringUtils.EMPTY // Service
																												// Description
																												// -
																												// Invoice
					: canceledInvoice.getServiceDescriptionInvoice());
			rowData.add(StringUtils.isBlank(canceledInvoice.getServiceDescriptionPo()) ? StringUtils.EMPTY // Service
																											// Description–
																											// PO
					: canceledInvoice.getServiceDescriptionPo());
			rowData.add(StringUtils.isBlank(canceledInvoice.getServiceDescriptionGl()) ? StringUtils.EMPTY // Service
																											// Description
																											// - GL Text
					: canceledInvoice.getServiceDescriptionGl());
			rowData.add(StringUtils.isBlank(canceledInvoice.getSacDecription()) ? StringUtils.EMPTY // SAC Description
					: canceledInvoice.getSacDecription());
			rowData.add(StringUtils.isBlank(canceledInvoice.getDeductorGstin()) ? StringUtils.EMPTY // Deductor GSTIN
					: canceledInvoice.getDeductorGstin());
			rowData.add(StringUtils.isBlank(canceledInvoice.getIsResident()) ? StringUtils.EMPTY // Non-Resident
																									// Deductee
																									// Indicator
					: canceledInvoice.getIsResident());
			rowData.add(StringUtils.isBlank(canceledInvoice.getDeductorPan()) ? StringUtils.EMPTY // Deductor PAN
					: canceledInvoice.getDeductorPan());
			rowData.add(StringUtils.isBlank(canceledInvoice.getDeducteeAddress()) ? StringUtils.EMPTY // Deductee
																										// Address
					: canceledInvoice.getDeducteeAddress());
			rowData.add(StringUtils.isBlank(canceledInvoice.getDocumentNumber()) ? StringUtils.EMPTY // ERP Document
																										// Number
					: canceledInvoice.getDocumentNumber());
			rowData.add(canceledInvoice.getMiroNumber() == null ? StringUtils.EMPTY // "MIRO Number
					: canceledInvoice.getMiroNumber());
			rowData.add(StringUtils.isBlank(canceledInvoice.getMigoNumber()) ? StringUtils.EMPTY // MIGO Number
					: canceledInvoice.getMigoNumber());
			rowData.add(StringUtils.isBlank(canceledInvoice.getDocumentNumber()) ? StringUtils.EMPTY // Document Number
					: canceledInvoice.getDocumentNumber());
			rowData.add(canceledInvoice.getDocumentDate() == null ? StringUtils.EMPTY // Document Date
					: canceledInvoice.getDocumentDate());
			rowData.add(StringUtils.isBlank(canceledInvoice.getCompanyCode()) ? StringUtils.EMPTY // Company Code
					: canceledInvoice.getCompanyCode());
			rowData.add(StringUtils.isBlank(canceledInvoice.getDeducteeCode()) ? StringUtils.EMPTY //// Deductee Code
					: canceledInvoice.getDeducteeCode());
			rowData.add(canceledInvoice.getDocumentPostingDate() == null ? StringUtils.EMPTY // Posting Date of Document
					: canceledInvoice.getDocumentPostingDate());
			rowData.add(canceledInvoice.getPaymentDate() == null ? StringUtils.EMPTY // payment date
					: canceledInvoice.getPaymentDate());
			rowData.add(canceledInvoice.getTdsDeductionDate() == null ? StringUtils.EMPTY // Tds Deduction Date
					: canceledInvoice.getTdsDeductionDate());
			rowData.add(StringUtils.isBlank(canceledInvoice.getDocumentNumber()) ? StringUtils.EMPTY // "Line Item
																										// Number
					: canceledInvoice.getDocumentNumber());
			rowData.add(canceledInvoice.getIgstRate() == null ? StringUtils.EMPTY // IGST Rate
					: canceledInvoice.getIgstRate());
			rowData.add(canceledInvoice.getIgstAmount() == null ? StringUtils.EMPTY // IGST Amount
					: canceledInvoice.getIgstAmount());
			rowData.add(canceledInvoice.getCgstRate() == null ? StringUtils.EMPTY // CGST Rate
					: canceledInvoice.getCgstRate());
			rowData.add(canceledInvoice.getCgstAmount() == null ? StringUtils.EMPTY // CGST Amount
					: canceledInvoice.getCgstAmount());
			rowData.add(canceledInvoice.getSgstRate() == null ? StringUtils.EMPTY // SGST Rate
					: canceledInvoice.getSgstRate());
			rowData.add(canceledInvoice.getSgstAmount() == null ? StringUtils.EMPTY // SGST Amount
					: canceledInvoice.getSgstAmount());
			rowData.add(canceledInvoice.getCessRate() == null ? StringUtils.EMPTY // Cess Rate
					: canceledInvoice.getCessRate());
			rowData.add(canceledInvoice.getCessAmount() == null ? StringUtils.EMPTY // Cess Amount
					: canceledInvoice.getCessAmount());
			if (canceledInvoice.getCreditable() != null) {
				rowData.add(canceledInvoice.getCreditable() == true ? 'Y' : 'N'); // Creditable (Y/N)
			} else {
				rowData.add(StringUtils.EMPTY); // Creditable (Y/N)
			}
			rowData.add(StringUtils.isBlank(canceledInvoice.getSectionCode()) ? StringUtils.EMPTY
					: canceledInvoice.getSectionCode()); // Section code
			rowData.add(StringUtils.isBlank(canceledInvoice.getPos()) ? StringUtils.EMPTY // POS
					: canceledInvoice.getPos());
			rowData.add(canceledInvoice.getPoNumber() == null ? StringUtils.EMPTY // PO number
					: canceledInvoice.getPoNumber());
			rowData.add(canceledInvoice.getPoDate() == null ? StringUtils.EMPTY // PO date
					: canceledInvoice.getPoDate());
			rowData.add(StringUtils.isBlank(canceledInvoice.getLinkedAdvanceNumber()) ? StringUtils.EMPTY // Linked
																											// advance
																											// Number
					: canceledInvoice.getLinkedAdvanceNumber());
			rowData.add(StringUtils.isBlank(canceledInvoice.getGrossIndicator()) ? StringUtils.EMPTY // Grossing up
																										// Indicator
					: canceledInvoice.getGrossIndicator());
			rowData.add(canceledInvoice.getOriginalDocumentNumber() == null ? StringUtils.EMPTY // Original Document
																								// Number
					: canceledInvoice.getOriginalDocumentNumber());
			rowData.add(canceledInvoice.getOriginalDocumentDate() == null ? StringUtils.EMPTY // Original Document Date
					: canceledInvoice.getOriginalDocumentDate());
			rowData.add(StringUtils.isBlank(canceledInvoice.getUserDefinedField1()) ? StringUtils.EMPTY // User Defined
																										// Field 1
					: canceledInvoice.getUserDefinedField1());
			rowData.add(StringUtils.isBlank(canceledInvoice.getUserDefinedField2()) ? StringUtils.EMPTY // User Defined
																										// Field 2
					: canceledInvoice.getUserDefinedField2());
			rowData.add(StringUtils.isBlank(canceledInvoice.getUserDefinedField1()) ? StringUtils.EMPTY // User Defined
																										// Field 3
					: canceledInvoice.getUserDefinedField3());
			rowData.add(
					canceledInvoice.getChallanPaid() == null ? StringUtils.EMPTY : canceledInvoice.getChallanPaid());
			rowData.add(canceledInvoice.getChallanGeneratedDate() == null ? StringUtils.EMPTY
					: canceledInvoice.getChallanGeneratedDate());
			rowData.add(canceledInvoice.getProvisionCanAdjust() == null ? StringUtils.EMPTY
					: canceledInvoice.getProvisionCanAdjust());
			rowData.add(canceledInvoice.getAdvanceCanAdjust() == null ? StringUtils.EMPTY
					: canceledInvoice.getAdvanceCanAdjust());

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
			Integer g, Integer b,XSSFFont font) {
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderRight(BorderStyle.MEDIUM);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillForegroundColor(new XSSFColor(new java.awt.Color(r, g, b), defaultIndexedColorMap));
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setFont(font);
		style.setLocked(true);
		style.setBottomBorderColor(IndexedColors.WHITE.getIndex());
		style.setTopBorderColor(IndexedColors.WHITE.getIndex());
		style.setLeftBorderColor(IndexedColors.WHITE.getIndex());
		style.setRightBorderColor(IndexedColors.WHITE.getIndex());
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

	@SuppressWarnings("unchecked")
	public ByteArrayInputStream generateMismatchExcel(int csvLinesSize, CsvContainer csv, String tan, String tenantId,
			String deductorPan) throws IOException, ParseException {
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
					createSXSSFCell(style, row1, 6, row.getField("pan"));
					String panValidationStatus = StringUtils.EMPTY;
					String gstinNumber = StringUtils.EMPTY;
					String deducteeStatus = StringUtils.EMPTY;
					String aadharNo = StringUtils.EMPTY;
					String tdsApplicabilityUnderSection = StringUtils.EMPTY;
					String grOrIRIndicator = StringUtils.EMPTY;
					if (row.getField("resident").equals("N") && !deducteeResMap.isEmpty()
							&& deducteeResMap.get(row.getField("deductee_key")) != null) {
						DeducteeDetailsDTO deducteeDetailData = deducteeResMap.get(row.getField("deductee_key")).get(0);
						panValidationStatus = deducteeDetailData.getPanValidationStatus();
						gstinNumber = deducteeDetailData.getGstinNumber();
						deducteeStatus = deducteeDetailData.getDeducteeStatus();
						aadharNo = deducteeDetailData.getAdharNo();
						tdsApplicabilityUnderSection = deducteeDetailData.getTdApplicabilityUnderSection();
						grOrIRIndicator = deducteeDetailData.getGrOrIRIndicator();

					} else if (row.getField("resident").equals("Y") && !deducteeNRMap.isEmpty()
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
					if (row.getField("resident").equals("N") && !resSectionMap.isEmpty()
							&& resSectionMap.get(row.getField("deductee_key")) != null) {
						sections = resSectionMap.get(row.getField("deductee_key")).stream()
								.collect(Collectors.toList());
					} else if (row.getField("resident").equals("Y") && !nrSectionMap.isEmpty()
							&& nrSectionMap.get(row.getField("deductee_key")) != null) {
						sections = nrSectionMap.get(row.getField("deductee_key")).stream().collect(Collectors.toList());
					}
					createSXSSFCell(style, row1, 14, sections.size() + "");
					createSXSSFCell(style, row1, 15,
							sections.isEmpty() ? "No Section" : sections.toString().replace("[", "").replace("]", ""));
					String postingDocDate = row.getField("document_posting_date");
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
					createSXSSFCell(style, row1, 17, row.getField("vendor_invoice_number"));
					createSXSSFCell(style, row1, 18, row.getField("document_date"));
					createSXSSFCell(style, row1, 19, row.getField("document_number"));
					createSXSSFCell(style, row1, 20, row.getField("document_posting_date"));
					createSXSSFCell(style, row1, 21, row.getField("document_type"));
					createSXSSFCell(style, row1, 22, row.getField("supply_type"));
					createSXSSFCell(style, row1, 23, row.getField("document_type"));
					createSXSSFCell(style, row1, 24, row.getField("line_item_number"));
					createSXSSFCell(style, row1, 25, row.getField("original_document_number"));
					createSXSSFCell(style, row1, 26, row.getField("original_document_date"));
					createSXSSFCell(style, row1, 27, row.getField("hsn_sac_code"));
					createSXSSFCell(style, row1, 28, row.getField("sac_description"));
					createSXSSFCell(style, row1, 29, row.getField("service_description_invoice"));
					createSXSSFCell(style, row1, 30, row.getField("gl_account_code"));
					createSXSSFCell(style, row1, 31, row.getField(""));// GLAccountName
					createSXSSFCell(style, row1, 32, row.getField("po_number"));
					createSXSSFCell(style, row1, 33, row.getField("po_line_item_number"));
					createSXSSFCell(style, row1, 34, row.getField("po_date"));
					createSXSSFCell(style, row1, 35, row.getField("service_description_po"));
					createSXSSFCell(style, row1, 36, row.getField("resident"));
					createSXSSFCell(style, row1, 37, row.getField("debit_credit_indicator"));
					createSXSSFCell(style, row1, 38, row.getField("invoice_amount"));
					createSXSSFCell(style, row1, 39, row.getField("invoice_value"));
					createSXSSFCell(style, row1, 40, row.getField("tds_base_value"));
					createSXSSFCell(style, row1, 41, row.getField("tds_tax_code_erp"));
					createSXSSFCell(style, row1, 42, row.getField("actual_tds_section"));
					createSXSSFCell(style, row1, 43, row.getField("actual_tds_rate"));
					createSXSSFCell(style, row1, 44, row.getField("actual_tds_amount"));
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
								} else if ("service_description_gl_text".equalsIgnoreCase(entry.getKey())) {
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
					createSXSSFCell(style, row1, 75, row.getField("migo_number"));
					createSXSSFCell(style, row1, 76, row.getField("miro_number"));
					createSXSSFCell(style, row1, 77, getFormattedValue(row.getField("igst_rate")));
					createSXSSFCell(style, row1, 78, getFormattedValue(row.getField("igst_amount")));
					createSXSSFCell(style, row1, 79, getFormattedValue(row.getField("cgst_rate")));
					createSXSSFCell(style, row1, 80, getFormattedValue(row.getField("cgst_amount")));
					createSXSSFCell(style, row1, 81, getFormattedValue(row.getField("sgst_rate")));
					createSXSSFCell(style, row1, 82, getFormattedValue(row.getField("sgst_amount")));
					createSXSSFCell(style, row1, 83, getFormattedValue(row.getField("cess_rate")));
					createSXSSFCell(style, row1, 84, getFormattedValue(row.getField("cess_amount")));
					createSXSSFCell(style, row1, 85, row.getField("pos"));
					createSXSSFCell(style, row1, 86, row.getField("linked_advance_number"));
					createSXSSFCell(style, row1, 87, row.getField("linked_provision_number"));
					String provisionAdjustmentFlag = StringUtils.isNotBlank(row.getField("provision_can_adjust"))
							&& row.getField("provision_can_adjust").equals("1") ? "Y" : "N";
					createSXSSFCell(style, row1, 88, provisionAdjustmentFlag);
					String advanceAdjustmentFlag = StringUtils.isNotBlank(row.getField("advance_can_adjust"))
							&& row.getField("advance_can_adjust").equals("1") ? "Y" : "N";
					createSXSSFCell(style, row1, 89, advanceAdjustmentFlag);
					createSXSSFCell(style, row1, 90, "");
					createSXSSFCell(style, row1, 91, "");
					createSXSSFCell(style, row1, 92, row.getField("grossing_up_indicator"));
					createSXSSFCell(style, row1, 93, "");
					createSXSSFCell(style, row1, 94, "");
					createSXSSFCell(style, row1, 95, "");
					createSXSSFCell(style, row1, 96, row.getField("item_code"));
					createSXSSFCell(style, row1, 97, row.getField("tds_remittance_date"));
					createSXSSFCell(style, row1, 98, grOrIRIndicator);
					createSXSSFCell(style, row1, 99, row.getField("type_of_transaction"));
					createSXSSFCell(style, row1, 100, row.getField("saa_number"));
					createSXSSFCell(style, row1, 101, row.getField("ref_key3"));
					createSXSSFCell(style, row1, 102, row.getField("user_defined_field1"));
					createSXSSFCell(style, row1, 103, row.getField("user_defined_field2"));
					createSXSSFCell(style, row1, 104, row.getField("user_defined_field3"));
					createSXSSFCell(style, row1, 105, row.getField("user_defined_field_4"));
					createSXSSFCell(style, row1, 106, row.getField("user_defined_field_5"));
					createSXSSFCell(style, row1, 107, row.getField("user_defined_field_6"));
					createSXSSFCell(style, row1, 108, row.getField("user_defined_field_7"));
					createSXSSFCell(style, row1, 109, row.getField("user_defined_field_8"));
					createSXSSFCell(style, row1, 110, row.getField("user_defined_field_9"));
					createSXSSFCell(style, row1, 111, row.getField("user_defined_field_10"));
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
					Integer groupId = (StringUtils.isNotBlank(row.getField("invoice_groupid"))
							? new Double(row.getField("invoice_groupid")).intValue()
							: 0);
					createSXSSFCell(style, row1, 122, groupId.toString());
					Integer nopId = (StringUtils.isNotBlank(row.getField("invoice_npid"))
							? new Double(row.getField("invoice_npid")).intValue()
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

	private void createSXSSFCell(XSSFCellStyle style, SXSSFRow row, int cellNumber, String value) {
		row.createCell(cellNumber).setCellValue(StringUtils.isBlank(value) == true ? StringUtils.EMPTY : value);
		row.getCell(cellNumber).setCellStyle(style); // K
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
		return "Invoice Remediation Report (Dated: " + date + ")\n Client Name: " + deductorName + "\n";
	}
}