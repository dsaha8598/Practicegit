package com.ey.in.tds.ingestion.service.ldcerrorreport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.ey.in.tds.common.dto.ldc.LdcErrorReportsCsvDTO;
import com.ey.in.tds.common.dto.ldc.LdcSucessReportsCsvDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

@Service
public class LdcReportsService {

	@Autowired
	private OnboardingClient onboardingClient;

	Map<String, String> excelHeaderMap = new HashMap<String, String>() {
		private static final long serialVersionUID = 8203589643342597052L;
		{
			put("PAN NUMBER", "D");
		}
	};

	public File convertLdcCsvToxlsx(File csvFile, String deductorTan, String tenantId, String deductorPan) throws Exception {

		Reader reader = new FileReader(csvFile);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		CsvToBean<LdcErrorReportsCsvDTO> csvToBean = new CsvToBeanBuilder(reader).withType(LdcErrorReportsCsvDTO.class)
				.withIgnoreLeadingWhiteSpace(true).build();

		// Aspose method to convert
		Workbook workbook = ldcXlsxReport(csvToBean.parse(), deductorTan, tenantId, deductorPan);

		// Write the output to the file
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxLdcFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()) + "_Error_Report", ".xlsx");

		FileUtils.writeByteArrayToFile(xlsxLdcFile, baout.toByteArray());
		baout.close();
		return xlsxLdcFile;

	}

	public Workbook ldcXlsxReport(List<LdcErrorReportsCsvDTO> ldcErrorReportsCsvList, String deductorTan,
			String tenantId, String deductorPan)
			throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		String[] headerNames = new String[] { "Deductor TAN", "ERROR MESSAGE", "SEQUENCE NUMBER", "PAN NUMBER",
				"FINANCIAL YEAR", "NAME OF DEDUCTEE", "CERTIFICATE NUMBER", "VALID FROM", "CANCEL DATE", "VALID TO",
				"SECTION CODE", "NATURE OF PAYMENT", "RATE OF TDS", "CERTIFICATE LIMIT", "AMOUNT CONSUMED",
				"DATE OF ISSUE" };

		worksheet.getCells().importArray(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan, tenantId);
		DeductorMasterDTO deductorData= getDeductor.getBody().getData();

		setExtractDataForLdc(ldcErrorReportsCsvList, worksheet, deductorTan);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range rang1 = worksheet.getCells().createRange("A6:B6");
		rang1.setStyle(style1);

		Cell cellC6 = worksheet.getCells().get("C6");
		Style styleC6 = cellC6.getStyle();
		styleC6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleC6.setPattern(BackgroundType.SOLID);
		styleC6.getFont().setBold(true);
		styleC6.getFont().setColor(Color.getWhite());
		styleC6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellC6.setStyle(styleC6);

		// Style for D6 to P6 headers
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
		worksheet.freezePanes(0, 2, 0, 2);
		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("LDC Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		cellA2.setValue("Client Name:" + deductorData.getDeductorName());
		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(11);
		a2Style.getFont().setBold(true);
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

	private void setExtractDataForLdc(List<LdcErrorReportsCsvDTO> ldcErrorReportsCsvList, Worksheet worksheet,
			String deductorTan) throws Exception {
		if (!ldcErrorReportsCsvList.isEmpty()) {
			int rowIndex = 6;
			for (LdcErrorReportsCsvDTO errorReportsDTO : ldcErrorReportsCsvList) {
				ArrayList<String> rowData = new ArrayList<String>();

				for (Map.Entry<String, String> entry : excelHeaderMap.entrySet()) {
					Cell cell = worksheet.getCells().get(entry.getValue() + (rowIndex + 1));
					Style style = cell.getStyle();
					style.setForegroundColor(Color.fromArgb(255, 0, 0));
					style.setPattern(BackgroundType.SOLID);
					cell.setStyle(style);
				}

				rowData.add(StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.add(StringUtils.isBlank(errorReportsDTO.getErroMessage()) ? StringUtils.EMPTY
						: errorReportsDTO.getErroMessage());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSequenceNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getSequenceNumber());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPanNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getPanNumber());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getFinancialYear()) ? StringUtils.EMPTY
						: errorReportsDTO.getFinancialYear());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getNameOfDeductee()) ? StringUtils.EMPTY
						: errorReportsDTO.getNameOfDeductee());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCertificateNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getCertificateNumber());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getValidFrom()) ? StringUtils.EMPTY
						: errorReportsDTO.getValidFrom());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCancelDate()) ? StringUtils.EMPTY
						: errorReportsDTO.getCancelDate());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getValidTo()) ? StringUtils.EMPTY
						: errorReportsDTO.getValidTo());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSectionCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getSectionCode());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getNatureOfPayment()) ? StringUtils.EMPTY
						: errorReportsDTO.getNatureOfPayment());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getRateOfTds()) ? StringUtils.EMPTY
						: errorReportsDTO.getRateOfTds());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getCertificateLimit()) ? StringUtils.EMPTY
						: errorReportsDTO.getCertificateLimit());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getAmountConsumed()) ? StringUtils.EMPTY
						: errorReportsDTO.getAmountConsumed());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDateOfIssue()) ? StringUtils.EMPTY
						: errorReportsDTO.getDateOfIssue());

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);

			}
		}

	}

	public File convertLdcSucessCsvToxlsx(File csvFile) throws Exception {

		Reader reader = new FileReader(csvFile);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		CsvToBean<LdcSucessReportsCsvDTO> csvToBean = new CsvToBeanBuilder(reader)
				.withType(LdcSucessReportsCsvDTO.class).withIgnoreLeadingWhiteSpace(true).build();

		// Aspose method to convert
		Workbook workbook = ldcXlsxSucessReport(csvToBean.parse());

		// Write the output to the file
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxLdcFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()), ".xlsx");

		FileUtils.writeByteArrayToFile(xlsxLdcFile, baout.toByteArray());
		baout.close();
		return xlsxLdcFile;

	}

	public Workbook ldcXlsxSucessReport(List<LdcSucessReportsCsvDTO> ldcReportsCsvList) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("ldc_excel_upload_template");

		String[] headerNames = new String[] { "Certificate Number", "Financial Year", "Pan Of Employee",
				"Name Of Deductee", "Valid From", "Cancel Date", "Valid To", "Section Code", "Nature Of Payment",
				"Rate Of Tds", "Certificate Limit", "Amount Consumed", "Date Of Issue" };

		worksheet.getCells().importArray(headerNames, 0, 0, false);

		setExtractDataForLdcSucessReport(ldcReportsCsvList, worksheet);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range rang1 = worksheet.getCells().createRange("A1:M1");
		rang1.setStyle(style1);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:M1");

		return workbook;
	}

	private void setExtractDataForLdcSucessReport(List<LdcSucessReportsCsvDTO> ldcReportsCsvList,
			Worksheet worksheet) throws Exception {
		if (!ldcReportsCsvList.isEmpty()) {
			int rowIndex = 1;
			for (LdcSucessReportsCsvDTO reportDTO : ldcReportsCsvList) {
				ArrayList<String> rowData = new ArrayList<String>();

				rowData.add(StringUtils.isBlank(reportDTO.getCertificateNumber()) ? StringUtils.EMPTY
						: reportDTO.getCertificateNumber());
				rowData.add(StringUtils.isBlank(reportDTO.getFinancialYear()) ? StringUtils.EMPTY
						: reportDTO.getFinancialYear());
				rowData.add(StringUtils.isBlank(reportDTO.getPanOfEmployee()) ? StringUtils.EMPTY
						: reportDTO.getPanOfEmployee());
				rowData.add(StringUtils.isBlank(reportDTO.getNameOfDeductee()) ? StringUtils.EMPTY
						: reportDTO.getNameOfDeductee());
				rowData.add(
						StringUtils.isBlank(reportDTO.getValidFrom()) ? StringUtils.EMPTY : reportDTO.getValidFrom());
				rowData.add(
						StringUtils.isBlank(reportDTO.getCancelDate()) ? StringUtils.EMPTY : reportDTO.getCancelDate());
				rowData.add(StringUtils.isBlank(reportDTO.getValidTo()) ? StringUtils.EMPTY : reportDTO.getValidTo());
				rowData.add(StringUtils.isBlank(reportDTO.getSectionCode()) ? StringUtils.EMPTY
						: reportDTO.getSectionCode());
				rowData.add(StringUtils.isBlank(reportDTO.getNatureOfPayment()) ? StringUtils.EMPTY
						: reportDTO.getNatureOfPayment());
				rowData.add(
						StringUtils.isBlank(reportDTO.getRateOfTds()) ? StringUtils.EMPTY : reportDTO.getRateOfTds());
				rowData.add(StringUtils.isBlank(reportDTO.getCertificateLimit()) ? StringUtils.EMPTY
						: reportDTO.getCertificateLimit());
				rowData.add(StringUtils.isBlank(reportDTO.getAmountConsumed()) ? StringUtils.EMPTY
						: reportDTO.getAmountConsumed());
				rowData.add(StringUtils.isBlank(reportDTO.getDateOfIssue()) ? StringUtils.EMPTY
						: reportDTO.getDateOfIssue());

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);

			}
		}

	}
}
