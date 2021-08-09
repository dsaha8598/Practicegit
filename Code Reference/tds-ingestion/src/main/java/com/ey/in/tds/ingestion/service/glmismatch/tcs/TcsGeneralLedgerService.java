package com.ey.in.tds.ingestion.service.glmismatch.tcs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.model.generalledger.TcsGeneralLedgerOtherReportCsvDTO;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.jdbc.dao.GeneralLedgerDAO;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvRow;

@Service
public class TcsGeneralLedgerService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	protected BlobStorage blob;

	@Autowired
	GeneralLedgerDAO generalLedgerDAO;

	@Value("${page_size}")
	protected int pageSize;

	public static String[] glOtherReportHeaders = new String[] { "Document Type", "Document Number",
			"Tcs Document Type", "Total Doc Value", "Document Date", "Posting Date", "Tcs Gl Account Code",
			"Gl Document Number", "Profit Center", "Gl Doc Value", "Gl Doc Type", "Tcs Gl Code", "Document Currency",
			"Amount In Document Currency", "Amount In Local Currency", "Category" };

	public static String[] errorGSTvsTCSCsvToXlsxHeader = new String[] { "supplier_gstin", "document_number",
			"document_type", "profit_centre", "total_doc_value", "plant_code", "customer_code", "product_code",
			"product_description", "accounting_voucher_number", "accounting_voucher_date", "customer_gstin",
			"sgst_amount", "cgst_amount", "igst_amount" };

	public static String[] sucessGSTvsTCSCsvToXlsxHeader = new String[] { "ClientCode", "CustomerCode",
			"DocumentNumber", "DocumentDate", "AccountingDocumentNumber", "AccountingDocumentDate", "DocumentType",
			"GSTDocumentType", "TaxableAmount", "CGST", "SGST", "IGST", "TCSAmount", "CollectorGSTIN", "CustomerGSTIN",
			"PlantCode", "ProfitCentre","Category","SubCategory","Reason" };

	public static String[] otherGSTvsTCSCsvToXlsxHeader = new String[] { "collector_code", "document_type",
			"document_number", "total_doc_value", "posting_date", "gl_account_code", "collectee_code",
			"accounting_document_date", "cgst_amount", "igst_amount", "sgst_amount", "final_tcs_amount",
			"collectee_gstin", "taxable_value", "under_threshold", "is_exempted","Category","Reason" };

	// Error Report
	public File convertGlCsvOtherToXlsx(File csvFile, String deductorTan, String tenantId, String deductorPan)
			throws Exception {
		// TODO: Create a error Report for GL
		Reader reader = new FileReader(csvFile);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		CsvToBean<TcsGeneralLedgerOtherReportCsvDTO> csvToBean = new CsvToBeanBuilder(reader)
				.withType(TcsGeneralLedgerOtherReportCsvDTO.class).withIgnoreLeadingWhiteSpace(true).build();

		Workbook workbook = glOtherXlsxReport(csvToBean.parse(), deductorTan, tenantId, deductorPan);
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxInvoiceFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()) + "_Other_Report",
				".xlsx");

		FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
		baout.close();
		xlsxInvoiceFile.deleteOnExit();
		return xlsxInvoiceFile;
	}

	public Workbook glOtherXlsxReport(List<TcsGeneralLedgerOtherReportCsvDTO> glErrorReportsCsvList, String tan,
			String tenantId, String deductorPan) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("GL Details");

		worksheet.getCells().importArray(glOtherReportHeaders, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForGl(glErrorReportsCsvList, worksheet);

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
		Range headerColorRange2 = worksheet.getCells().createRange("D6:P6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		// worksheet.freezePanes(0, 2, 0, 2);

		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("General Ledger Report (Dated: " + date + ")");
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

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "P6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:P6");

		return workbook;
	}

	private void setExtractDataForGl(List<TcsGeneralLedgerOtherReportCsvDTO> glErrorReportsCsvList, Worksheet worksheet)
			throws Exception {

		if (!glErrorReportsCsvList.isEmpty()) {
			int rowIndex = 6;
			for (TcsGeneralLedgerOtherReportCsvDTO errorReportsDTO : glErrorReportsCsvList) {
				ArrayList<String> rowData = new ArrayList<String>();

				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentType());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentNumber());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTcsDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getTcsDocumentType());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTotalDocValue()) ? StringUtils.EMPTY
						: errorReportsDTO.getTotalDocValue());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentDate()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentDate());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPostingDate()) ? StringUtils.EMPTY
						: errorReportsDTO.getPostingDate());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTcsGlAccountCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getTcsGlAccountCode());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGlDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getGlDocumentNumber());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getProfitCenter()) ? StringUtils.EMPTY
						: errorReportsDTO.getProfitCenter());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGlDocValue()) ? StringUtils.EMPTY
						: errorReportsDTO.getGlDocValue());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGlDocType()) ? StringUtils.EMPTY
						: errorReportsDTO.getGlDocType());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTcsGlCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getTcsGlCode());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentCurrency()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentCurrency());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getAmountInDocumentCurrency()) ? StringUtils.EMPTY
						: errorReportsDTO.getAmountInDocumentCurrency());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getAmountInLocalCurrency()) ? StringUtils.EMPTY
						: errorReportsDTO.getAmountInLocalCurrency());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCategory()) ? StringUtils.EMPTY
						: errorReportsDTO.getCategory());

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);

			}
		}
	}

	public File convertSuccessGSTvsTCSCsvToXlsx(CsvContainer csvFile, String deductorTan, String tenantId,
			String deductorPan, String fileName) throws Exception {

		Workbook workbook = sucessGSTvsTCSXlsxReport(csvFile, deductorTan, tenantId, deductorPan);
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxInvoiceFile = File.createTempFile(FilenameUtils.getBaseName(fileName) + "_Sucess_Report", ".xlsx");

		FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
		baout.close();
		xlsxInvoiceFile.deleteOnExit();
		return xlsxInvoiceFile;
	}

	public File convertOtherGSTvsTCSCsvToXlsx(CsvContainer csvFile, String deductorTan, String tenantId,
			String deductorPan, String fileName) throws Exception {

		Workbook workbook = otherGSTvsTCSXlsxReport(csvFile, deductorTan, tenantId, deductorPan);
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxInvoiceFile = File.createTempFile(FilenameUtils.getBaseName(fileName) + "_Other_Report", ".xlsx");

		FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
		baout.close();
		xlsxInvoiceFile.deleteOnExit();
		return xlsxInvoiceFile;
	}

	public File convertErrorGSTvsTCSCsvToXlsx(CsvContainer csvFile, String deductorTan, String tenantId,
			String deductorPan, String fileName) throws Exception {

		Workbook workbook = errorGSTvsTCSXlsxReport(csvFile, deductorTan, tenantId, deductorPan);
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxInvoiceFile = File.createTempFile(FilenameUtils.getBaseName(fileName) + "_Error_Report", ".xlsx");

		FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
		baout.close();
		xlsxInvoiceFile.deleteOnExit();
		return xlsxInvoiceFile;
	}

	public Workbook sucessGSTvsTCSXlsxReport(CsvContainer glErrorReportsCsvList, String tan, String tenantId,
			String deductorPan) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("In TCS as well as GST ");

		worksheet.getCells().importArray(sucessGSTvsTCSCsvToXlsxHeader, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setSucessGSTvsTCSExtractDataForGl(glErrorReportsCsvList, worksheet);

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
		Range headerColorRange2 = worksheet.getCells().createRange("D6:T6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		// worksheet.freezePanes(0, 2, 0, 2);

		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("Transaction appearing in TCS template as well as GST template (Dated: " + date + ")");
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

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "T6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:S6");

		return workbook;
	}

	public Workbook errorGSTvsTCSXlsxReport(CsvContainer glErrorReportsCsvList, String tan, String tenantId,
			String deductorPan) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Transaction in GST not in TCS");

		worksheet.getCells().importArray(errorGSTvsTCSCsvToXlsxHeader, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setErrorGSTvsTCSExtractDataForGl(glErrorReportsCsvList, worksheet);

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
		Range headerColorRange2 = worksheet.getCells().createRange("D6:P6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		// worksheet.freezePanes(0, 2, 0, 2);

		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("Transaction in GST template and not in TCS template Report (Dated: " + date + ")");
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

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "P6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:P6");

		return workbook;
	}

	public Workbook otherGSTvsTCSXlsxReport(CsvContainer glErrorReportsCsvList, String tan, String tenantId,
			String deductorPan) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("In TCS not in GST");

		worksheet.getCells().importArray(otherGSTvsTCSCsvToXlsxHeader, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setOtherGSTvsTCSExtractDataForGl(glErrorReportsCsvList, worksheet);

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
		Range headerColorRange2 = worksheet.getCells().createRange("D6:Q6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		// worksheet.freezePanes(0, 2, 0, 2);

		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("Transaction in TCS template and not in GST template Report (Dated: " + date + ")");
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

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "Q6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:Q6");

		return workbook;
	}

	private void setErrorGSTvsTCSExtractDataForGl(CsvContainer csvContainer, Worksheet worksheet) throws Exception {
		int rowIndex = 6;
		for (CsvRow row : csvContainer.getRows()) {
			ArrayList<String> rowData = new ArrayList<String>();

			rowData.add(StringUtils.isBlank(row.getField("supplier_gstin")) ? StringUtils.EMPTY
					: row.getField("supplier_gstin"));
			rowData.add(StringUtils.isBlank(row.getField("document_number")) ? StringUtils.EMPTY
					: row.getField("document_number"));
			rowData.add(StringUtils.isBlank(row.getField("document_type")) ? StringUtils.EMPTY
					: row.getField("document_type"));
			rowData.add(StringUtils.isBlank(row.getField("profit_centre")) ? StringUtils.EMPTY
					: row.getField("profit_centre"));
			rowData.add(StringUtils.isBlank(row.getField("total_doc_value")) ? StringUtils.EMPTY
					: row.getField("total_doc_value"));
			rowData.add(
					StringUtils.isBlank(row.getField("plant_code")) ? StringUtils.EMPTY : row.getField("plant_code"));
			rowData.add(StringUtils.isBlank(row.getField("customer_code")) ? StringUtils.EMPTY
					: row.getField("customer_code"));
			rowData.add(StringUtils.isBlank(row.getField("product_code")) ? StringUtils.EMPTY
					: row.getField("product_code"));
			rowData.add(StringUtils.isBlank(row.getField("product_description")) ? StringUtils.EMPTY
					: row.getField("product_description"));
			rowData.add(StringUtils.isBlank(row.getField("accounting_voucher_number")) ? StringUtils.EMPTY
					: row.getField("accounting_voucher_number"));
			rowData.add(StringUtils.isBlank(row.getField("accounting_voucher_date")) ? StringUtils.EMPTY
					: row.getField("accounting_voucher_date"));
			rowData.add(StringUtils.isBlank(row.getField("customer_gstin")) ? StringUtils.EMPTY
					: row.getField("customer_gstin"));
			rowData.add(
					StringUtils.isBlank(row.getField("sgst_amount")) ? StringUtils.EMPTY : row.getField("sgst_amount"));
			rowData.add(
					StringUtils.isBlank(row.getField("cgst_amount")) ? StringUtils.EMPTY : row.getField("cgst_amount"));
			rowData.add(
					StringUtils.isBlank(row.getField("igst_amount")) ? StringUtils.EMPTY : row.getField("igst_amount"));

			worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);

		}
	}

	private void setOtherGSTvsTCSExtractDataForGl(CsvContainer csvContainer, Worksheet worksheet) throws Exception {
		int rowIndex = 6;
		for (CsvRow row : csvContainer.getRows()) {
			ArrayList<String> rowData = new ArrayList<String>();

			rowData.add(StringUtils.isBlank(row.getField("collector_code")) ? StringUtils.EMPTY
					: row.getField("collector_code"));
			rowData.add(StringUtils.isBlank(row.getField("document_type")) ? StringUtils.EMPTY
					: row.getField("document_type"));
			rowData.add(StringUtils.isBlank(row.getField("document_number")) ? StringUtils.EMPTY
					: row.getField("document_number"));
			rowData.add(StringUtils.isBlank(row.getField("total_doc_value")) ? StringUtils.EMPTY
					: row.getField("total_doc_value"));
			rowData.add(StringUtils.isBlank(row.getField("posting_date")) ? StringUtils.EMPTY
					: row.getField("posting_date"));
			rowData.add(StringUtils.isBlank(row.getField("gl_account_code")) ? StringUtils.EMPTY
					: row.getField("gl_account_code"));
			rowData.add(StringUtils.isBlank(row.getField("collectee_code")) ? StringUtils.EMPTY
					: row.getField("collectee_code"));
			rowData.add(StringUtils.isBlank(row.getField("accounting_document_date")) ? StringUtils.EMPTY
					: row.getField("accounting_document_date"));
			rowData.add(
					StringUtils.isBlank(row.getField("cgst_amount")) ? StringUtils.EMPTY : row.getField("cgst_amount"));
			rowData.add(
					StringUtils.isBlank(row.getField("igst_amount")) ? StringUtils.EMPTY : row.getField("igst_amount"));
			rowData.add(
					StringUtils.isBlank(row.getField("sgst_amount")) ? StringUtils.EMPTY : row.getField("sgst_amount"));
			rowData.add(StringUtils.isBlank(row.getField("final_tcs_amount")) ? StringUtils.EMPTY
					: row.getField("final_tcs_amount"));
			rowData.add(StringUtils.isBlank(row.getField("collectee_gstin")) ? StringUtils.EMPTY
					: row.getField("collectee_gstin"));
			rowData.add(StringUtils.isBlank(row.getField("taxable_value")) ? StringUtils.EMPTY
					: row.getField("taxable_value"));
			rowData.add(StringUtils.isBlank(row.getField("under_threshold")) ? StringUtils.EMPTY
					: row.getField("under_threshold"));
			rowData.add(
					StringUtils.isBlank(row.getField("is_exempted")) ? StringUtils.EMPTY : row.getField("is_exempted"));
			rowData.add(StringUtils.isBlank(row.getField("category")) ? StringUtils.EMPTY : row.getField("category"));

			worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);

		}
	}

	private void setSucessGSTvsTCSExtractDataForGl(CsvContainer csvContainer, Worksheet worksheet) throws Exception {
		int rowIndex = 6;
		for (CsvRow row : csvContainer.getRows()) {
			ArrayList<String> rowData = new ArrayList<String>();

			rowData.add(
					StringUtils.isBlank(row.getField("ClientCode")) ? StringUtils.EMPTY : row.getField("ClientCode"));
			rowData.add(StringUtils.isBlank(row.getField("CustomerCode")) ? StringUtils.EMPTY
					: row.getField("CustomerCode"));
			rowData.add(StringUtils.isBlank(row.getField("DocumentNumber")) ? StringUtils.EMPTY
					: row.getField("DocumentNumber"));
			rowData.add(StringUtils.isBlank(row.getField("DocumentDate")) ? StringUtils.EMPTY
					: row.getField("DocumentDate"));
			rowData.add(StringUtils.isBlank(row.getField("AccountingDocumentNumber")) ? StringUtils.EMPTY
					: row.getField("AccountingDocumentNumber"));
			rowData.add(StringUtils.isBlank(row.getField("AccountingDocumentDate")) ? StringUtils.EMPTY
					: row.getField("AccountingDocumentDate"));
			rowData.add(StringUtils.isBlank(row.getField("DocumentType")) ? StringUtils.EMPTY
					: row.getField("DocumentType"));
			rowData.add(StringUtils.isBlank(row.getField("GSTDocumentType")) ? StringUtils.EMPTY
					: row.getField("GSTDocumentType"));
			rowData.add(StringUtils.isBlank(row.getField("TaxableAmount")) ? StringUtils.EMPTY
					: row.getField("TaxableAmount"));
			rowData.add(StringUtils.isBlank(row.getField("CGST")) ? StringUtils.EMPTY : row.getField("CGST"));
			rowData.add(StringUtils.isBlank(row.getField("SGST")) ? StringUtils.EMPTY : row.getField("SGST"));
			rowData.add(StringUtils.isBlank(row.getField("IGST")) ? StringUtils.EMPTY : row.getField("IGST"));
			rowData.add(StringUtils.isBlank(row.getField("TCSAmount")) ? StringUtils.EMPTY : row.getField("TCSAmount"));
			rowData.add(StringUtils.isBlank(row.getField("CollectorGSTIN")) ? StringUtils.EMPTY
					: row.getField("CollectorGSTIN"));
			rowData.add(StringUtils.isBlank(row.getField("CustomerGSTIN")) ? StringUtils.EMPTY
					: row.getField("CustomerGSTIN"));
			rowData.add(StringUtils.isBlank(row.getField("PlantCode")) ? StringUtils.EMPTY : row.getField("PlantCode"));
			rowData.add(StringUtils.isBlank(row.getField("ProfitCentre")) ? StringUtils.EMPTY
					: row.getField("ProfitCentre"));
			rowData.add(StringUtils.isBlank(row.getField("category")) ? StringUtils.EMPTY : row.getField("category"));
			rowData.add(StringUtils.isBlank(row.getField("sub_category")) ? StringUtils.EMPTY
					: row.getField("sub_category"));

			worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);

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

}
