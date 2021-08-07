package com.ey.in.tds.ingestion.service.glmismatch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
import com.aspose.cells.WorksheetCollection;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.ErrorCode;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.GeneralLedgerStagingDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.GeneralLedger;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.GeneralLedgerStaging;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.model.generalledger.GeneralLedgerErrorReportCsvDTO;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.ErrorCodeService;
import com.ey.in.tds.common.service.ErrorReportService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.GlobalConstants;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.jdbc.dao.GeneralLedgerDAO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceLineItemDAO;
import com.ey.in.tds.ingestion.web.rest.generalledger.GeneralLedgerSummaryDTO;
import com.microsoft.azure.storage.StorageException;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

@Service
public class GeneralLedgerService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	private ErrorCodeService errorCodeService;

	@Autowired
	protected BlobStorage blob;

	@Autowired
	GeneralLedgerDAO generalLedgerDAO;

	@Autowired
	InvoiceLineItemDAO invoiceLineItemDAO;

	@Autowired
	BatchUploadDAO batchUploadDAO;

	@Autowired
	GeneralLedgerStagingDAO generalLedgerStagingDAO;

	@Autowired
	MastersClient mastersClient;

	@Value("${page_size}")
	protected int pageSize;

	private String invoiceColumnsForGlSummary = "invoice_line_item_deductee_name,invoice_line_item_service_description_invoice,"
			+ "invoice_line_item_service_description_gl,"
			+ "invoice_line_item_final_tds_section,invoice_line_item_final_tds_rate, "
			+ "invoice_line_item_final_tds_amount,invoice_line_item_tds_amount, invoice_line_item_actual_tds_amount,"
			+ "invoice_line_item_actual_tds_section, invoice_line_item_actual_tds_rate,invoice_line_item_deductor_gstin, "
			+ "deductor_master_tan,invoice_line_item_deductor_pan,invoice_line_item_derived_tds_amount,invoice_line_item_derived_tds_rate,"
			+ " invoice_line_item_derived_tds_section";

	private String glColumnsForGlSummary = "general_ledger_assessment_year,general_ledger_assessment_month,deductor_master_tan,"
			+ "general_ledger_client_amount,general_ledger_client_tds_section,general_ledger_deductee_gstin,general_ledger_deductee_name,"
			+ " general_ledger_deductee_pan,general_ledger_deductee_tin,general_ledger_deductor_gstin,general_ledger_deductor_pan,"
			+ "general_ledger_deductor_tan, general_ledger_deductor_tin,general_ledger_final_amount,general_ledger_final_tds_section,"
			+ "general_ledger_name_of_the_deductee,general_ledger_service_description, general_ledger_tool_derived_amount,"
			+ "general_ledger_tool_derived_tds_section";

	public PagedData<GeneralLedger> invoiceFoundInGL(String tan, Integer batchId, Integer year, List<Integer> months,
			boolean tdsExist, boolean notAbleToDetermine, boolean isAccountingCode, boolean recordFound,
			boolean mismatch, boolean amountMismatch, Pagination pagination) {
		List<GeneralLedger> list = new ArrayList<>();
		BigInteger count = BigInteger.ZERO;
		if (batchId != null) {
			list = generalLedgerDAO.invoicesFoundInGLByBatchId(batchId, tdsExist, notAbleToDetermine, isAccountingCode,
					recordFound, mismatch, amountMismatch, pagination);
			count = generalLedgerDAO.getCountOFinvoicesFoundInGLByBatchId(batchId, tdsExist, notAbleToDetermine,
					isAccountingCode, recordFound, mismatch, amountMismatch);

		} else if (!months.isEmpty()) {
			list = generalLedgerDAO.invoicesFoundInGLByYearMonthTan(year, months, tan, tdsExist, notAbleToDetermine,
					isAccountingCode, recordFound, mismatch, amountMismatch, pagination);
			count = generalLedgerDAO.getCountOFinvoicesFoundInGLByYearMonthTan(year, months, tan, tdsExist,
					notAbleToDetermine, isAccountingCode, recordFound, mismatch, amountMismatch, pagination);
		} else {
			list = generalLedgerDAO.invoicesFoundInGLByYear(year, tan, tdsExist, notAbleToDetermine, isAccountingCode,
					recordFound, mismatch, amountMismatch, pagination);
			count = generalLedgerDAO.getCountOFinvoicesFoundInGLByYear(year, tan, tdsExist, notAbleToDetermine,
					isAccountingCode, recordFound, mismatch, amountMismatch, pagination);
		}
		PagedData<GeneralLedger> pagedData = new PagedData<>(list, list.size(), pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		return pagedData;
	}

	// TDS Found
	public PagedData<GeneralLedger> tdsFound(String tan, Integer batchId, Integer year, List<Integer> months,
			Pagination pagination) {
		List<GeneralLedger> list = new ArrayList<>();
		BigInteger count = BigInteger.ZERO;
		if (batchId != null) {
			list = generalLedgerDAO.findByBatchUploadIdAndTdsFound(batchId, true, pagination);
			count = generalLedgerDAO.findByBatchUploadIdAndTdsFoundCount(batchId, true);
		} else if (!months.isEmpty()) {
			list = generalLedgerDAO.findByYearMonthTanAndTdsFound(year, months, tan, true, pagination);
			count = generalLedgerDAO.getCountOffindByYearMonthTanAndTdsFound(year, months, tan, true);
		} else {
			list = generalLedgerDAO.findByYearAndTdsFound(year, tan, true, pagination);
			count = generalLedgerDAO.getCountOffindByYearAndTdsFound(year, tan, true);
		}
		PagedData<GeneralLedger> pagedData = new PagedData<>(list, list.size(), pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		return pagedData;
	}

	// Not Able to determine and Account code exists
	public PagedData<GeneralLedger> notAbleToDetermineAndAccountCodeExists(String tan, Integer batchId, Integer year,
			List<Integer> months, boolean isAccountingCode, Pagination pagination) {
		List<GeneralLedger> list = null;
		BigInteger count = BigInteger.ZERO;
		if (batchId != null) {
			list = generalLedgerDAO.nadAndAccountCodeExists(batchId, true, isAccountingCode, pagination);
			count = generalLedgerDAO.getCountOfNadAndAccountCodeExists(batchId, true, isAccountingCode);
		} else if (!months.isEmpty()) {
			list = generalLedgerDAO.nadAndAccountCodeExistsByYearMonthTan(year, months, tan, true, isAccountingCode,
					pagination);
			count = generalLedgerDAO.getCountOfNadAndAccountCodeExistsByYearMonthTan(year, months, tan, true,
					isAccountingCode);
		} else {
			list = generalLedgerDAO.nadAndAccountCodeExistsByYear(year, tan, true, isAccountingCode, pagination);
			count = generalLedgerDAO.getCountOfNadAndAccountCodeExistsByYear(year, tan, true, isAccountingCode);
		}
		PagedData<GeneralLedger> pagedData = new PagedData<>(list, list.size(), pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		return pagedData;
	}

	public List<InvoiceLineItem> invoicesNotFoundInGL(String tan, Integer batchId, Integer year, List<Integer> months,
			Pagination pagination) {
		if (batchId != null) {
			return invoiceLineItemDAO.findByBatchUploadIdAndGLFound(batchId, false, pagination);
		} else if (!months.isEmpty()) {
			return invoiceLineItemDAO.findByYearMonthTanAndGLFound(year, months, tan, false, pagination);
		} else {
			return invoiceLineItemDAO.findByYearTanAndGLFound(year, tan, false, pagination);
		}

	}

	/*
	 * public GLCategoryDTO convertGLModelToDTO(GeneralLedgerD generalLedger) {
	 * GLCategoryDTO gLCategoryDTO = null; try { if (generalLedger != null) {
	 * gLCategoryDTO = new GLCategoryDTO();
	 * gLCategoryDTO.setId(generalLedger.getKey().getId());
	 * gLCategoryDTO.setSourceIdentifier(generalLedger.getSourceIdentifier());
	 * gLCategoryDTO.setSourceFileName(generalLedger.getSourceFileName());
	 * gLCategoryDTO.setCompanyCode(generalLedger.getCompanyCode());
	 * gLCategoryDTO.setNameOfTheCompanyCode(generalLedger.getNameOfTheCompanyCode()
	 * ); gLCategoryDTO.setDeductorPAN(generalLedger.getDeductorPan());
	 * gLCategoryDTO.setDeductorTAN(generalLedger.getDeductorTan());
	 * gLCategoryDTO.setDeductorGSTIN(generalLedger.getDeductorGstin());
	 * gLCategoryDTO.setDeductorCode(generalLedger.getDeductorCode());
	 * gLCategoryDTO.setNameOfTheDeductee(generalLedger.getNameOfTheDeductee());
	 * gLCategoryDTO.setNonResidentDeducteeIndicator(generalLedger.
	 * getNonResidentDeducteeIndicator());
	 * gLCategoryDTO.setDeducteePAN(generalLedger.getDeducteePan());
	 * gLCategoryDTO.setDeducteeTIN(generalLedger.getDeducteeTin());
	 * gLCategoryDTO.setDeducteeGSTIN(generalLedger.getDeducteeGstin());
	 * gLCategoryDTO.setDocumentNumber(generalLedger.getDocumentNumber());
	 * gLCategoryDTO.setDocumentType(generalLedger.getDocumentType());
	 * gLCategoryDTO.setDocumentDate(generalLedger.getDocumentDate());
	 * gLCategoryDTO.setPostingDateOfDocument(generalLedger.getPostingDate());
	 * gLCategoryDTO.setLineItemNumber(generalLedger.getLineItemNumber());
	 * gLCategoryDTO.setAccountCode(generalLedger.getAccountCode());
	 * gLCategoryDTO.setAccountDescription(generalLedger.getAccountDescription());
	 * gLCategoryDTO.setTdsSection(generalLedger.getFinalTdsSection());
	 * gLCategoryDTO.setInvoiceAmountInLocalCurrency(generalLedger.
	 * getInvoiceAmountInLocalCurrency());
	 * gLCategoryDTO.setMiroMumber(generalLedger.getMiroDocumentNumber());
	 * gLCategoryDTO.setMigoNumber(generalLedger.getMigoDocumentNumber());
	 * gLCategoryDTO.setReferenceNumber(generalLedger.getReferenceNumber());
	 * gLCategoryDTO.setUserDefinedField1(generalLedger.getUserDefinedField_1());
	 * gLCategoryDTO.setUserDefinedField2(generalLedger.getUserDefinedField_2());
	 * gLCategoryDTO.setUserDefinedField3(generalLedger.getUserDefinedField_3());
	 * gLCategoryDTO.setClientAmount(
	 * BigDecimal.valueOf(generalLedger.getClientAmount()).setScale(2,
	 * RoundingMode.UP).doubleValue());
	 * gLCategoryDTO.setClientSection(generalLedger.getClientTdsSection());
	 * gLCategoryDTO.setToolDerivedAmount(BigDecimal.valueOf(generalLedger.
	 * getToolDerivedAmount()) .setScale(2, RoundingMode.UP).doubleValue());
	 * gLCategoryDTO.setToolDerivedSection(generalLedger.getToolDerivedTdsSection())
	 * ; } } catch (Exception e) {
	 * logger.error("Exception occurred while convertGLModelToDTO : ", e); } return
	 * gLCategoryDTO; }
	 */

	// Error Report
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public File convertGlCsvToXlsx(File csvFile, String deductorTan, String tenantId, String deductorPan,
			Integer assesmentyear, Integer batchId) throws Exception {

		List<GeneralLedgerStaging> errorRecords = null;
		Reader reader = null;
		CsvToBean<GeneralLedgerErrorReportCsvDTO> csvToBean = null;

		logger.info("Retrieveing GL error records from DB {}");

		errorRecords = generalLedgerStagingDAO.getGLErrorRecords(assesmentyear, batchId);
		logger.info("Number of GL error records retrieved is " + errorRecords.size() + "{}");
		if (errorRecords.isEmpty()) {
			reader = new FileReader(csvFile);
			csvToBean = new CsvToBeanBuilder(reader).withType(GeneralLedgerErrorReportCsvDTO.class)
					.withIgnoreLeadingWhiteSpace(true).build();
		}

		Workbook workbook = glXlsxReport(csvToBean != null ? csvToBean.parse() : null, errorRecords, deductorTan,
				tenantId, deductorPan);
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxInvoiceFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()) + "_Error_Report",
				".xlsx");

		FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
		baout.close();
		xlsxInvoiceFile.deleteOnExit();
		return xlsxInvoiceFile;
	}

	public Workbook glXlsxReport(List<GeneralLedgerErrorReportCsvDTO> glErrorReportsCsvList,
			List<GeneralLedgerStaging> errorRecords, String tan, String tenantId, String deductorPan) throws Exception {

		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		worksheet.getCells().importArray(ErrorReportService.glheaderNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (errorRecords.isEmpty()) {
			setExtractDataForGl(glErrorReportsCsvList, worksheet);
		} else {
			setExtractDataForGlWithRecordsFromDB(errorRecords, worksheet, tan,deductorData.getDeductorName());
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

		// Style for D6 to BI6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:AH6");
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
		cellA1.setValue("General Ledger Error Report (Dated: " + date + ")");
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
		String lastHeaderCellName = "AH6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:AH6");

		return workbook;
	}

	private void setExtractDataForGl(List<GeneralLedgerErrorReportCsvDTO> glErrorReportsCsvList, Worksheet worksheet)
			throws Exception {

		logger.info("Preparing error report from CSV file {}");
		if (!glErrorReportsCsvList.isEmpty()) {
			int rowIndex = 6;
			List<ErrorCode> errorCodesObjs = mastersClient.getAllErrorCodes().getBody().getData();
			Map<String, String> errorDescription = new HashMap<>();
			for (ErrorCode errorCodesObj : errorCodesObjs) {
				errorDescription.put(errorCodesObj.getCode(), errorCodesObj.getDescription());
			}
			int index = 0;
			for (GeneralLedgerErrorReportCsvDTO errorReportsDTO : glErrorReportsCsvList) {
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

				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorTan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorTan());
				rowData.add(StringUtils.isBlank(errorCodesWithNewLine) ? StringUtils.EMPTY : errorCodesWithNewLine);
				rowData.add(index + "");// Sequence Number
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceIdentifier()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceIdentifier());// Source Identifier
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceFileName()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceFileName());// Source File Name
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorCode()); // DeductorCode
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorName()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorName());// DeductorName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorPan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorPan());// Deductor PAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorTan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorTan());// Deductor TAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorGstin());// Deductor GSTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeCode()); // DeducteeCode
				rowData.add(StringUtils.isBlank(errorReportsDTO.getNameOfTheDeductee()) ? StringUtils.EMPTY
						: errorReportsDTO.getNameOfTheDeductee()); // DeducteeName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteePan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteePan()); // Deductee Pan
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeTin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeTin()); // Deductee Tin
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeGstin()); // Deductee Gstin
				rowData.add(StringUtils.isBlank(errorReportsDTO.getVendorInvoiceNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getVendorInvoiceNumber()); // VendorInvoiceNumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentDate()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentDate()); // DocumentDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentNumber()); // ERPDocumentNumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPostingDateOfDocument()) ? StringUtils.EMPTY
						: errorReportsDTO.getPostingDateOfDocument()); // PostingDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getMigoNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getMigoNumber());// MIGONumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getMiroNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getMiroNumber()); // MIRONumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentType()); // ERPDocumentType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getLineItemNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getLineItemNumber()); // LineItemNumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getHsnsac()) ? StringUtils.EMPTY
						: errorReportsDTO.getHsnsac()); // HSNorSAC
				rowData.add(StringUtils.isBlank(errorReportsDTO.getHsnOrSacDesc()) ? StringUtils.EMPTY
						: errorReportsDTO.getHsnOrSacDesc()); // HSNorSACDesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getServiceDescriptionInvoice()) ? StringUtils.EMPTY
						: errorReportsDTO.getServiceDescriptionInvoice()); // InvoiceDesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGlAccountCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getGlAccountCode()); // GLAccountCode
				rowData.add(StringUtils.isBlank(errorReportsDTO.getServiceDescriptionGl()) ? StringUtils.EMPTY
						: errorReportsDTO.getServiceDescriptionGl()); // GLDesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTdsSection()) ? StringUtils.EMPTY
						: errorReportsDTO.getTdsSection()); // TDSSection
				rowData.add(StringUtils.isBlank(errorReportsDTO.getInvoiceAmountInLocalCurrency()) ? StringUtils.EMPTY
						: errorReportsDTO.getInvoiceAmountInLocalCurrency()); // InvoiceAmountLocCurrency
				rowData.add(StringUtils.isBlank(errorReportsDTO.getRefKey3()) ? StringUtils.EMPTY
						: errorReportsDTO.getRefKey3()); // RefKey3
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField1()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField1());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField2()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField2());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField3()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField3());

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);

			}
		}
	}

	private void setExtractDataForGlWithRecordsFromDB(List<GeneralLedgerStaging> errorRecords, Worksheet worksheet,
			String deductorTan,String deductorNme) throws Exception {

		logger.info("Preparing error report from DB records {}");
		if (!errorRecords.isEmpty()) {
			int rowIndex = 6;
			List<ErrorCode> errorCodesObjs = mastersClient.getAllErrorCodes().getBody().getData();
			Map<String, String> errorDescription = new HashMap<>();
			for (ErrorCode errorCodesObj : errorCodesObjs) {
				errorDescription.put(errorCodesObj.getCode(), errorCodesObj.getDescription());
			}
			int index = 0;
			for (GeneralLedgerStaging errorReportsDTO : errorRecords) {
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

				rowData.add(deductorTan);
				rowData.add(StringUtils.isBlank(errorCodesWithNewLine) ? StringUtils.EMPTY : errorCodesWithNewLine);
				rowData.add(index + "");// Sequence Number
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceIdentifier()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceIdentifier());// Source Identifier
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceFileName()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceFileName());// Source File Name
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorCode()); // DeductorCode
				rowData.add(StringUtils.isBlank(deductorNme)?StringUtils.EMPTY:deductorNme);// DeductorName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorPan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorPan());// Deductor PAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorTan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorTan());// Deductor TAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorGstin());// Deductor GSTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeCode()); // DeducteeCode
				rowData.add(StringUtils.isBlank(errorReportsDTO.getNameOfTheDeductee()) ? StringUtils.EMPTY
						: errorReportsDTO.getNameOfTheDeductee()); // DeducteeName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteePan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteePan()); // Deductee Pan
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeTin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeTin()); // Deductee Tin
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeGstin()); // Deductee Gstin
				rowData.add(StringUtils.isBlank(errorReportsDTO.getVendorInvoiceNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getVendorInvoiceNumber()); // VendorInvoiceNumber
				rowData.add(errorReportsDTO.getDocumentDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentDate().toString()); // DocumentDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentNumber()); // ERPDocumentNumber
				rowData.add(errorReportsDTO.getPostingDateOfDocument() == null ? StringUtils.EMPTY
						: errorReportsDTO.getPostingDateOfDocument().toString()); // PostingDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getMigoNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getMigoNumber());// MIGONumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getMiroNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getMiroNumber()); // MIRONumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentType()); // ERPDocumentType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getLineItemNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getLineItemNumber()); // LineItemNumber
				rowData.add(
						StringUtils.isBlank(errorReportsDTO.getHsn()) ? StringUtils.EMPTY : errorReportsDTO.getHsn()); // HSNorSAC
				rowData.add(StringUtils.isBlank(errorReportsDTO.getHsnOrSac()) ? StringUtils.EMPTY
						: errorReportsDTO.getHsnOrSac()); // HSNorSACDesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getServiceDescriptionInvoice()) ? StringUtils.EMPTY
						: errorReportsDTO.getServiceDescriptionInvoice()); // InvoiceDesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGlAccountCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getGlAccountCode()); // GLAccountCode
				rowData.add(StringUtils.isBlank(errorReportsDTO.getServiceDescriptionGl()) ? StringUtils.EMPTY
						: errorReportsDTO.getServiceDescriptionGl()); // GLDesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTdsSection()) ? StringUtils.EMPTY
						: errorReportsDTO.getTdsSection()); // TDSSection
				rowData.add(StringUtils.isBlank(errorReportsDTO.getInvoiceAmountInLocalCurrency()) ? StringUtils.EMPTY
						: errorReportsDTO.getInvoiceAmountInLocalCurrency()); // InvoiceAmountLocCurrency
				rowData.add(StringUtils.isBlank(errorReportsDTO.getRefKey3()) ? StringUtils.EMPTY
						: errorReportsDTO.getRefKey3()); // RefKey3
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField1()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField1());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField2()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField2());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getUserDefinedField3()) ? StringUtils.EMPTY
						: errorReportsDTO.getUserDefinedField3());

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);

			}
		}
	}

	public CommonDTO<GeneralLedger> getGeneralLedgerSummaryData(Integer batchId, String type, Pagination pagination) {
		PagedData<GeneralLedger> generalLedgerList = null;
		CommonDTO<GeneralLedger> generalLedgerDTO = new CommonDTO<GeneralLedger>();
		BigInteger count = BigInteger.ZERO;
		switch (type) {
		case "nomismatches":
			// Invoice Found in GL but No mismatches
			generalLedgerList = invoiceFoundInGL(null, batchId, 0, null, false, false, false, true, false, false,
					pagination);
			// count = generalLedgerDAO.getCountOFinvoicesFoundInGLByBatchId(batchId, false,
			// false, false, true,
			// false, false);
			break;
		case "smmam":
			// Invoice Found in GL but SMMAM
			generalLedgerList = invoiceFoundInGL(null, batchId, 0, null, false, false, false, true, true, false,
					pagination);
			// count = generalLedgerDAO.getCountOFinvoicesFoundInGLByBatchId(batchId, false,
			// false, false, true,
			// true, false);
			break;
		case "smamm":
			// Invoice Found in GL but SMAMM
			generalLedgerList = invoiceFoundInGL(null, batchId, 0, null, false, false, false, true, false, true,
					pagination);
			// count = generalLedgerDAO.getCountOFinvoicesFoundInGLByBatchId(batchId, false,
			// false, false, true,
			// false, true);
			break;
		case "smmamm":
			// Invoice Found in GL but SMMAMM
			generalLedgerList = invoiceFoundInGL(null, batchId, 0, null, false, false, false, true, true, true,
					pagination);
			// count = generalLedgerDAO.getCountOFinvoicesFoundInGLByBatchId(batchId, false,
			// false, false, true,
			// true, true);
			break;
		case "notds":
			// No TDS
			generalLedgerList = invoiceFoundInGL(null, batchId, 0, null, false, false, false, false, false, false,
					pagination);
			// count = generalLedgerDAO.getCountOFinvoicesFoundInGLByBatchId(batchId, false,
			// false, false, false,
			// false, false);
			break;
		case "tdsapplicable":
			// TDS Found
			generalLedgerList = tdsFound(null, batchId, 0, null, pagination);
			// count = generalLedgerDAO.findByBatchUploadIdAndTdsFoundCount(batchId, true);
			break;
		case "nadwithaccountcodeexists":
			// Not Able to determine and Account code exists true
			generalLedgerList = notAbleToDetermineAndAccountCodeExists(null, batchId, 0, null, true, pagination);
			// count = generalLedgerRepository.getCountOfNadAndAccountCodeExists(batchId,
			// true, true);
			break;
		case "nadwithoutaccountcodeexists":
			// Not Able to determine and Account code exists false
			generalLedgerList = notAbleToDetermineAndAccountCodeExists(null, batchId, 0, null, false, pagination);
			// count = generalLedgerRepository.getCountOfNadAndAccountCodeExists(batchId,
			// true, false);
			break;
		default:
			break;
		}
		if (generalLedgerList != null) {
			for (int index = 0; index < generalLedgerList.getData().size(); index++) {
				GeneralLedger ledgerList = generalLedgerList.getData().get(index);
				BigDecimal clientAmount = ledgerList.getClientAmount() != null
						? ledgerList.getClientAmount().setScale(2, RoundingMode.UP)
						: BigDecimal.ZERO;
				BigDecimal finalAmount = ledgerList.getFinalAmount() != null
						? ledgerList.getFinalAmount().setScale(2, RoundingMode.UP)
						: BigDecimal.ZERO;

				ledgerList.setClientAmount(clientAmount);
				ledgerList.setFinalAmount(finalAmount);
			}
		}
		generalLedgerDTO.setResultsSet(generalLedgerList);
		generalLedgerDTO.setCount(count);
		return generalLedgerDTO;
	}

//NEED TO DELETE AFTER 1 WEEK
	public CommonDTO<InvoiceLineItem> getGLSummaryDataForInvoices(Integer batchId, Pagination pagination) {
		CommonDTO<InvoiceLineItem> invoiceLineItem = new CommonDTO<InvoiceLineItem>();
		// Invioices Not found in GL
		List<InvoiceLineItem> invoices = invoicesNotFoundInGL(null, batchId, 0, null, pagination);
		BigInteger count = invoiceLineItemDAO.findByBatchUploadIdAndGLFoundCount(batchId, false);
		PagedData<InvoiceLineItem> pagedData = new PagedData<>(invoices, invoices.size(), pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);

		invoiceLineItem.setCount(count);
		invoiceLineItem.setResultsSet(pagedData);

		return invoiceLineItem;
	}

	public CommonDTO<GeneralLedger> getGLSummaryDataByYearMonth(String tan, String type,
			GeneralLedgerSummaryDTO glSummaryDTO, Pagination pagination) {
		List<Integer> months = new ArrayList<>();
		if ("Q1".equalsIgnoreCase(glSummaryDTO.getQuarter())) {
			months = GlobalConstants.Q1;
		} else if ("Q2".equalsIgnoreCase(glSummaryDTO.getQuarter())) {
			months = GlobalConstants.Q2;
		} else if ("Q3".equalsIgnoreCase(glSummaryDTO.getQuarter())) {
			months = GlobalConstants.Q3;
		} else if ("Q4".equalsIgnoreCase(glSummaryDTO.getQuarter())) {
			months = GlobalConstants.Q4;
		} else if (glSummaryDTO.getMonth() != null) {
			months = Arrays.asList(glSummaryDTO.getMonth());
		}
		PagedData<GeneralLedger> generalLedgerList = null;
		CommonDTO<GeneralLedger> generalLedgerDTO = new CommonDTO<GeneralLedger>();
		BigInteger count = BigInteger.ZERO;
		switch (type) {
		case "nomismatches":
			// Invoice Found in GL but No mismatches
			generalLedgerList = invoiceFoundInGL(tan, null, glSummaryDTO.getYear(), months, false, false, false, true,
					false, false, pagination);

			break;
		case "smmam":
			// Invoice Found in GL but SMMAM
			generalLedgerList = invoiceFoundInGL(tan, null, glSummaryDTO.getYear(), months, false, false, false, true,
					true, false, pagination);

			break;
		case "smamm":
			// Invoice Found in GL but SMAMM
			generalLedgerList = invoiceFoundInGL(tan, null, glSummaryDTO.getYear(), months, false, false, false, true,
					false, true, pagination);

			break;
		case "smmamm":
			// Invoice Found in GL but SMMAMM
			generalLedgerList = invoiceFoundInGL(tan, null, glSummaryDTO.getYear(), months, false, false, false, true,
					true, true, pagination);

			break;
		case "notds":
			// No TDS
			generalLedgerList = invoiceFoundInGL(tan, null, glSummaryDTO.getYear(), months, false, false, false, false,
					false, false, pagination);

			break;
		case "tdsapplicable":
			// TDS Found
			generalLedgerList = tdsFound(tan, null, glSummaryDTO.getYear(), months, pagination);

			break;
		case "nadwithaccountcodeexists":
			// Not Able to determine and Account code exists true
			generalLedgerList = notAbleToDetermineAndAccountCodeExists(tan, null, glSummaryDTO.getYear(), months, true,
					pagination);

			break;
		case "nadwithoutaccountcodeexists":
			// Not Able to determine and Account code exists false
			generalLedgerList = notAbleToDetermineAndAccountCodeExists(tan, null, glSummaryDTO.getYear(), months, false,
					pagination);

			break;
		default:
			break;
		}
		if (generalLedgerList != null) {
			for (int index = 0; index < generalLedgerList.getData().size(); index++) {
				GeneralLedger ledgerList = generalLedgerList.getData().get(index);
				if (ledgerList.getClientAmount() != null) {
					BigDecimal clientAmount = ledgerList.getClientAmount().setScale(2, RoundingMode.UP);
					ledgerList.setClientAmount(clientAmount);
				} else {
					ledgerList.setClientAmount(BigDecimal.ZERO);
				}

				if (ledgerList.getFinalAmount() != null) {
					BigDecimal finalAmount = ledgerList.getFinalAmount().setScale(2, RoundingMode.UP);
					ledgerList.setFinalAmount(finalAmount);
				} else {
					ledgerList.setFinalAmount(BigDecimal.ZERO);
				}
			}
		}

		generalLedgerDTO.setResultsSet(generalLedgerList);
		generalLedgerDTO.setCount(count);
		return generalLedgerDTO;
	}

	public CommonDTO<InvoiceLineItem> getGLSummaryDataForGlNotFound(GeneralLedgerSummaryDTO glSummaryDTO, String tan) {
		List<Integer> months = new ArrayList<>();
		Pagination pagination = glSummaryDTO.getPagination();
		if (StringUtils.isNotBlank(glSummaryDTO.getQuarter())) {
			if ("Q1".equalsIgnoreCase(glSummaryDTO.getQuarter())) {
				months = GlobalConstants.Q1;
			} else if ("Q2".equalsIgnoreCase(glSummaryDTO.getQuarter())) {
				months = GlobalConstants.Q2;
			} else if ("Q3".equalsIgnoreCase(glSummaryDTO.getQuarter())) {
				months = GlobalConstants.Q3;
			} else if ("Q4".equalsIgnoreCase(glSummaryDTO.getQuarter())) {
				months = GlobalConstants.Q4;
			}
		}
		if (glSummaryDTO.getMonth() != null && glSummaryDTO.getMonth().intValue() > 0) {
			months = Arrays.asList(glSummaryDTO.getMonth());
		}
		CommonDTO<InvoiceLineItem> invoiceLineItem = new CommonDTO<InvoiceLineItem>();
		// Invioices Not found in GL
		List<InvoiceLineItem> invoices = invoicesNotFoundInGL(tan, null, glSummaryDTO.getYear(), months,
				glSummaryDTO.getPagination());
		BigInteger count = invoiceLineItemDAO.invoicesNotFoundInGLCountByTanYearMonth(tan, glSummaryDTO.getYear(),
				months, false);

		PagedData<InvoiceLineItem> pagedData = new PagedData<>(invoices, invoices.size(), pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);

		invoiceLineItem.setCount(count);
		invoiceLineItem.setResultsSet(pagedData);

		return invoiceLineItem;
	}

	@Async
	public void exportGLSummaryReport(String deductorTan, String tenantId, String deductorPan, int year, int month,
			String userName, Integer glBatchId, String quarter) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		long noOfRows = 0;
		BatchUpload batchUpload = saveGlSummaryReport(deductorTan, tenantId, year, null, noOfRows,
				UploadTypes.GL_SUMMARY.name(), "Processing", month, userName, null);

		// getting GL summary data
		List<Integer> monthValues = new ArrayList<>();
		if (StringUtils.isNotBlank(quarter)) {
			if ("Q1".equals(quarter)) {
				monthValues = GlobalConstants.Q1;
			} else if ("Q2".equals(quarter)) {
				monthValues = GlobalConstants.Q2;
			} else if ("Q3".equals(quarter)) {
				monthValues = GlobalConstants.Q3;
			} else if ("Q4".equals(quarter)) {
				monthValues = GlobalConstants.Q4;
			}
		}
		Workbook workbook = new Workbook();
		Worksheet sf1 = workbook.getWorksheets().get(0);
		sf1.setName("Invoice Found in GL but No MM");
		WorksheetCollection worksheets = workbook.getWorksheets();
		Worksheet sf2 = worksheets.add("Invoice Found in GL but SMAMM");
		Worksheet sf3 = worksheets.add("No TDS");
		Worksheet sf4 = worksheets.add("TDS Found");
		Worksheet sf5 = worksheets.add("NAD and AccountCodeExists true");
		Worksheet sf6 = worksheets.add("NAD and AccountCodeExists false");
		Worksheet sf7 = worksheets.add("Invioices Not found in GL");

		// 1
		List<GeneralLedger> noMismatchSummary = new ArrayList<>();

		noMismatchSummary = generalLedgerDAO.getGlSummary(deductorTan, year, month, glColumnsForGlSummary, glBatchId,
				monthValues, false, false, true);
		setExtractDataForInvoiceGLSummary(noMismatchSummary, sf1, workbook);
		noOfRows = noOfRows + noMismatchSummary.size();

		// 2
		List<GeneralLedger> glSummaryGlButSMAMM = new ArrayList<>();

		glSummaryGlButSMAMM = generalLedgerDAO.getGlSummary(deductorTan, year, month, glColumnsForGlSummary, glBatchId,
				monthValues, false, true, true);
		setExtractDataForInvoiceGLSummary(glSummaryGlButSMAMM, sf2, workbook);
		noOfRows = noOfRows + glSummaryGlButSMAMM.size();
		// 3
		List<GeneralLedger> glSummaryNOTdsFound = new ArrayList<>();

		glSummaryNOTdsFound = generalLedgerDAO.getGlSummary(deductorTan, year, month, glColumnsForGlSummary, glBatchId,
				monthValues, false, false, false);
		setExtractDataForInvoiceGLSummary(glSummaryNOTdsFound, sf3, workbook);
		noOfRows = noOfRows + glSummaryNOTdsFound.size();

		// 4
		List<GeneralLedger> glSummaryTdsFound = new ArrayList<>();
		glSummaryTdsFound = generalLedgerDAO.getGlSummaryTdsFound(deductorTan, year, month, glColumnsForGlSummary,
				glBatchId, monthValues);
		setExtractDataForInvoiceGLSummary(glSummaryTdsFound, sf4, workbook);
		noOfRows = noOfRows + glSummaryTdsFound.size();

		// 5
		List<GeneralLedger> glSummaryCodeExistsTrue = new ArrayList<>();

		glSummaryCodeExistsTrue = generalLedgerDAO.getGlSummaryCodeExists(deductorTan, year, month,
				glColumnsForGlSummary, glBatchId, monthValues, true);

		setExtractDataForInvoiceGLSummary(glSummaryCodeExistsTrue, sf5, workbook);
		noOfRows = noOfRows + glSummaryCodeExistsTrue.size();

		// 6s
		List<GeneralLedger> glSummaryCodeExistsFalse = new ArrayList<>();

		glSummaryCodeExistsFalse = generalLedgerDAO.getGlSummaryCodeExists(deductorTan, year, month,
				glColumnsForGlSummary, glBatchId, monthValues, false);

		setExtractDataForInvoiceGLSummary(glSummaryCodeExistsFalse, sf6, workbook);
		noOfRows = noOfRows + glSummaryCodeExistsFalse.size();

		// 7
		List<InvoiceLineItem> glSummaryFoundInGl = new ArrayList<>();

		glSummaryFoundInGl = invoiceLineItemDAO.getGlSummaryFoundInGl(deductorTan, year, month,
				invoiceColumnsForGlSummary, glBatchId, monthValues);
		setExtractDataForSf7(glSummaryFoundInGl, sf7, workbook);
		noOfRows = noOfRows + glSummaryFoundInGl.size();

		// Write the output to the file
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);

		saveGlSummaryReport(deductorTan, tenantId, year, baout, noOfRows, UploadTypes.GL_SUMMARY.name(), "Processed",
				month, userName, batchUpload.getBatchUploadID());

	}

	private void setExtractDataForSf7(List<InvoiceLineItem> getGlSummaryFoundInGl, Worksheet worksheet,
			Workbook workbook) throws Exception {

		setCssForGlSummary(workbook, worksheet, ErrorReportService.invoiceGLSummaryheaderNames);

		if (!getGlSummaryFoundInGl.isEmpty()) {
			int rowIndex = 3;
			for (InvoiceLineItem InvoiceLineItem : getGlSummaryFoundInGl) {
				ArrayList<Object> rowData = new ArrayList<Object>();
				rowData.add(StringUtils.isBlank(InvoiceLineItem.getDeducteeName()) ? StringUtils.EMPTY
						: InvoiceLineItem.getDeducteeName());
				rowData.add(StringUtils.isBlank(InvoiceLineItem.getServiceDescriptionInvoice()) ? StringUtils.EMPTY
						: InvoiceLineItem.getServiceDescriptionInvoice());
				rowData.add(StringUtils.isBlank(InvoiceLineItem.getServiceDescriptionGl()) ? StringUtils.EMPTY
						: InvoiceLineItem.getServiceDescriptionGl());
				rowData.add(StringUtils.isBlank(InvoiceLineItem.getFinalTdsSection()) ? StringUtils.EMPTY
						: InvoiceLineItem.getFinalTdsSection());
				rowData.add(InvoiceLineItem.getFinalTdsRate());
				rowData.add(InvoiceLineItem.getFinalTdsAmount().setScale(2, RoundingMode.UP));
				rowData.add(InvoiceLineItem.getTdsAmount().setScale(2, RoundingMode.UP));
				rowData.add(InvoiceLineItem.getClientAmount().setScale(2, RoundingMode.UP));
				rowData.add(StringUtils.isBlank(InvoiceLineItem.getClientSection()) ? StringUtils.EMPTY
						: InvoiceLineItem.getClientSection());
				rowData.add(InvoiceLineItem.getClientRate());
				rowData.add(StringUtils.isBlank(InvoiceLineItem.getDeductorGstin()) ? StringUtils.EMPTY
						: InvoiceLineItem.getDeductorGstin());
				rowData.add(StringUtils.isBlank(InvoiceLineItem.getDeductorMasterTan()) ? StringUtils.EMPTY
						: InvoiceLineItem.getDeductorMasterTan());
				rowData.add(StringUtils.isBlank(InvoiceLineItem.getDeductorPan()) ? StringUtils.EMPTY
						: InvoiceLineItem.getDeductorPan());

				rowData.add(InvoiceLineItem.getDerivedTdsAmount().setScale(2, RoundingMode.UP));
				rowData.add(InvoiceLineItem.getDerivedTdsRate());
				rowData.add(StringUtils.isBlank(InvoiceLineItem.getDerivedTdsSection()) ? StringUtils.EMPTY
						: InvoiceLineItem.getDerivedTdsSection());
				rowData.add(StringUtils.isBlank(InvoiceLineItem.getDocumentNumber()) ? StringUtils.EMPTY
						: InvoiceLineItem.getDocumentNumber());
				if (InvoiceLineItem.getDocumentDate() != null) {
					Date date = InvoiceLineItem.getDocumentDate();
					String documentDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
					rowData.add(documentDate);
				}

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}
	}

	private void setExtractDataForInvoiceGLSummary(List<GeneralLedger> generalLedgerList, Worksheet worksheet,
			Workbook workbook) throws Exception {
		setCssForGlSummary(workbook, worksheet, ErrorReportService.glSummaryheaderNames);
		if (!generalLedgerList.isEmpty()) {
			int rowIndex = 3;

			for (GeneralLedger generalLedger : generalLedgerList) {
				ArrayList<Object> rowData = new ArrayList<Object>();
				rowData.add(generalLedger.getAssessmentYear());
				rowData.add(generalLedger.getAssessmentMonth());
				rowData.add(StringUtils.isBlank(generalLedger.getDeductorMasterTan()) ? StringUtils.EMPTY
						: generalLedger.getDeductorMasterTan());
				rowData.add(generalLedger.getClientAmount().setScale(2, RoundingMode.UP));
				rowData.add(StringUtils.isBlank(generalLedger.getTdsSection()) ? StringUtils.EMPTY
						: generalLedger.getTdsSection());
				rowData.add(StringUtils.isBlank(generalLedger.getDeductorGstin()) ? StringUtils.EMPTY
						: generalLedger.getDeductorGstin());
				rowData.add(StringUtils.isBlank(generalLedger.getDeducteeName()) ? StringUtils.EMPTY
						: generalLedger.getDeducteeName());
				rowData.add(StringUtils.isBlank(generalLedger.getDeducteePan()) ? StringUtils.EMPTY
						: generalLedger.getDeducteePan());
				rowData.add(StringUtils.isBlank(generalLedger.getDeducteeTin()) ? StringUtils.EMPTY
						: generalLedger.getDeducteeTin());
				rowData.add(StringUtils.isBlank(generalLedger.getDeductorGstin()) ? StringUtils.EMPTY
						: generalLedger.getDeductorGstin());
				rowData.add(StringUtils.isBlank(generalLedger.getDeductorPan()) ? StringUtils.EMPTY
						: generalLedger.getDeductorPan());
				rowData.add(StringUtils.isBlank(generalLedger.getDeductorTan()) ? StringUtils.EMPTY
						: generalLedger.getDeductorTan());
				rowData.add(StringUtils.isBlank(generalLedger.getDeductorTin()) ? StringUtils.EMPTY
						: generalLedger.getDeductorTin());
				if (generalLedger.getFinalAmount() != null) {
					rowData.add(generalLedger.getFinalAmount().setScale(2, RoundingMode.UP));
				} else {
					rowData.add(BigDecimal.valueOf(0.00).setScale(2, RoundingMode.UP));
				}
				rowData.add(StringUtils.isBlank(generalLedger.getFinalTdsSection()) ? StringUtils.EMPTY
						: generalLedger.getFinalTdsSection());
				rowData.add(StringUtils.isBlank(generalLedger.getNameOfTheDeductee()) ? StringUtils.EMPTY
						: generalLedger.getNameOfTheDeductee());
				rowData.add(StringUtils.isBlank(generalLedger.getServiceDescription()) ? StringUtils.EMPTY
						: generalLedger.getServiceDescription());
				if (generalLedger.getToolDerivedAmount() != null) {
					rowData.add(generalLedger.getToolDerivedAmount().setScale(2, RoundingMode.UP));
				} else {
					rowData.add(BigDecimal.valueOf(0.00).setScale(2, RoundingMode.UP));
				}
				rowData.add(StringUtils.isBlank(generalLedger.getToolDerivedTdsSection()) ? StringUtils.EMPTY
						: generalLedger.getToolDerivedTdsSection());
				rowData.add(StringUtils.isBlank(generalLedger.getDocumentNumber()) ? StringUtils.EMPTY
						: generalLedger.getDocumentNumber());
				if (generalLedger.getDocumentDate() != null) {
					Date date = generalLedger.getDocumentDate();
					String documentDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
					rowData.add(documentDate);
				}

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}
	}

	protected BatchUpload saveGlSummaryReport(String deductorTan, String tenantId, int assessmentYear,
			ByteArrayOutputStream out, long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId)
			throws FileNotFoundException, IOException, URISyntaxException, InvalidKeyException, StorageException {
		String path = null;
		String fileName = null;
		if (out != null) {
			fileName = uploadType.toLowerCase() + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
			File file = getConvertedExcelFile(out.toByteArray(), fileName);
			path = blob.uploadExcelToBlobWithFile(file, tenantId);
			logger.info("Challan Summary report {} completed for : {}", uploadType, userName);
		} else {
			logger.info("Challan Summary report {} started for : {}", uploadType, userName);
		}

		BatchUpload response = null;
		if (batchId != null) {
			response = batchUploadDAO.findByOnlyId(batchId);
		}
		BatchUpload batchUpload = null;
		if (response != null) {
			batchUpload = response;
			if (out != null) {
				batchUpload.setModifiedDate(new Date());
				batchUpload.setFilePath(path);
				batchUpload.setSuccessFileUrl(path);
				batchUpload.setFileName(fileName);
			} else {
				batchUpload.setFilePath(StringUtils.EMPTY);
				batchUpload.setSuccessFileUrl(StringUtils.EMPTY);
				batchUpload.setProcessStartTime(new Date());
				batchUpload.setProcessEndTime(null);
				batchUpload.setRowsCount(noOfRows);
			}
			batchUpload.setStatus(status);
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload.setModifiedBy(userName);
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			return batchUploadDAO.update(batchUpload);
		} else {
			batchUpload = new BatchUpload();
			batchUpload.setActive(true);
			batchUpload.setCreatedDate(new Date());
			batchUpload.setCreatedBy(userName);
			batchUpload.setStatus(status);
			batchUpload.setFileName(fileName);
			batchUpload.setAssessmentMonth(month);
			batchUpload.setStatus(status);
			batchUpload.setFailedCount(Long.valueOf(0));
			batchUpload.setProcessedCount(0);
			batchUpload.setErrorFilePath(null);
			batchUpload.setRowsCount(noOfRows);
			return batchUploadDAO.save(batchUpload);
		}
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

	private void setCssForGlSummary(Workbook workbook, Worksheet sf1, String[] headers) throws Exception {
		sf1.getCells().importArray(headers, 2, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);
		sf1.autoFitColumns();
		sf1.autoFitRows();
		sf1.freezePanes(0, 2, 0, 2);

		Cell cellA1Invoice = sf1.getCells().get("A1");
		cellA1Invoice.setValue("GL Summary");
		Style a1StyleInvoice = cellA1Invoice.getStyle();
		a1StyleInvoice.getFont().setName("Cambria");
		a1StyleInvoice.getFont().setSize(14);
		a1StyleInvoice.getFont().setBold(true);
		cellA1Invoice.setStyle(a1StyleInvoice);

		// Style for Headers
		Style style = workbook.createStyle();
		style.setForegroundColor(Color.fromArgb(252, 199, 155));
		style.setPattern(BackgroundType.SOLID);
		style.getFont().setBold(true);
		style.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range invoiceRange = sf1.getCells().createRange("A3:U3");
		invoiceRange.setStyle(style);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter1 = sf1.getAutoFilter();
		autoFilter1.setRange("A3:U3");
	}
}
