package com.ey.in.tds.ingestion.service.panerrorreport;

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
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.dto.pan.PanErrorReportCsvDTO;
import com.ey.in.tds.common.dto.pan.PanSucessReportCsvDTO;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

@Service
public class PanReportsService {

	@Autowired
	private OnboardingClient onboardingClient;

	static Map<String, String> excelHeaderMap = new HashMap<String, String>();

	static {
		excelHeaderMap.put("PAN NUMBER", "D");
	};

	public File convertPanCsvToxlsx(File csvFile, String deductorTan, String tenantId, String deductorPan)
			throws Exception {

		Reader reader = new FileReader(csvFile);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		CsvToBean<PanErrorReportCsvDTO> csvToBean = new CsvToBeanBuilder(reader).withType(PanErrorReportCsvDTO.class)
				.withIgnoreLeadingWhiteSpace(true).build();

		// Aspose method to convert
		Workbook workbook = panXlsxReport(csvToBean.parse(), deductorTan, tenantId, deductorPan);

		// Write the output to the file
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxPanFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()) + "_Error_Report", ".xlsx");

		FileUtils.writeByteArrayToFile(xlsxPanFile, baout.toByteArray());
		baout.close();
		return xlsxPanFile;

	}

	public Workbook panXlsxReport(List<PanErrorReportCsvDTO> ldcErrorReportsCsvList, String deductorTan,
			String tenantId, String deductorPan) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		String[] headerNames = new String[] { "Deductor TAN", "ERROR MESSAGE", "sequence Number", "PAN NUMBER",
				"Vender Name", "Status", "Score" };

		worksheet.getCells().importArray(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForPan(ldcErrorReportsCsvList, worksheet, deductorTan);

		// Style for A6 to D6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange = worksheet.getCells().createRange("A6:B6");
		headerColorRange.setStyle(style1);

		Cell cellC6 = worksheet.getCells().get("C6");
		Style styleC6 = cellC6.getStyle();
		styleC6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleC6.setPattern(BackgroundType.SOLID);
		styleC6.getFont().setBold(true);
		styleC6.getFont().setColor(Color.getWhite());
		styleC6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellC6.setStyle(styleC6);

		// Style for D6 to E6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:G6");
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
		cellA1.setValue("PAN Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setBold(true);
		a1Style.getFont().setSize(14);
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
		String lastHeaderCellName = "H6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:G6");

		return workbook;
	}

	private void setExtractDataForPan(List<PanErrorReportCsvDTO> panErrorReportsCsvList, Worksheet worksheet,
			String deductorTan) throws Exception {
		if (!panErrorReportsCsvList.isEmpty()) {
			int rowIndex = 6;
			for (PanErrorReportCsvDTO errorReportsDTO : panErrorReportsCsvList) {
				ArrayList<String> rowData = new ArrayList<String>();

				for (Map.Entry<String, String> entry : excelHeaderMap.entrySet()) {
					Cell cell = worksheet.getCells().get(entry.getValue() + (rowIndex + 1));
					Style style = cell.getStyle();
					style.setForegroundColor(Color.fromArgb(255, 0, 0));
					style.setPattern(BackgroundType.SOLID);
					cell.setStyle(style);
				}
				rowData.add(StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.add(StringUtils.isBlank(errorReportsDTO.getErrorMessage()) ? StringUtils.EMPTY
						: errorReportsDTO.getErrorMessage());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSequenceNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getSequenceNumber());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPanNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getPanNumber());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getVendorName()) ? StringUtils.EMPTY
						: errorReportsDTO.getVendorName());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getStatus()) ? StringUtils.EMPTY
						: errorReportsDTO.getStatus());
				rowData.add(StringUtils.isBlank(errorReportsDTO.getScore()) ? StringUtils.EMPTY
						: errorReportsDTO.getScore());

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);

			}
		}
	}

	public File convertPanSucessCsvToxlsx(File csvFile) throws Exception {

		Reader reader = new FileReader(csvFile);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		CsvToBean<PanSucessReportCsvDTO> csvToBean = new CsvToBeanBuilder(reader).withType(PanSucessReportCsvDTO.class)
				.withIgnoreLeadingWhiteSpace(true).build();

		// Aspose method to convert
		Workbook workbook = panSucessXlsxReport(csvToBean.parse());

		// Write the output to the file
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxPanFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()), ".xlsx");

		FileUtils.writeByteArrayToFile(xlsxPanFile, baout.toByteArray());
		baout.close();
		return xlsxPanFile;

	}

	public Workbook panSucessXlsxReport(List<PanSucessReportCsvDTO> panList) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Report Details");

		String[] headerNames = new String[] { "Pan Number", "Vendor Name", "Traces Name", "Confidence", "Status",
				"Score" };

		worksheet.getCells().importArray(headerNames, 0, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForPanSucess(panList, worksheet);

		// Style for A6 to F6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange = worksheet.getCells().createRange("A1:F1");
		headerColorRange.setStyle(style1);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:F1");

		return workbook;
	}

	private void setExtractDataForPanSucess(List<PanSucessReportCsvDTO> panList, Worksheet worksheet) throws Exception {
		if (!panList.isEmpty()) {
			int rowIndex = 1;
			for (PanSucessReportCsvDTO dto : panList) {
				ArrayList<String> rowData = new ArrayList<String>();

				rowData.add(StringUtils.isBlank(dto.getPanNumber()) ? StringUtils.EMPTY : dto.getPanNumber());
				rowData.add(StringUtils.isBlank(dto.getVendorName()) ? StringUtils.EMPTY : dto.getVendorName());
				rowData.add(StringUtils.isBlank(dto.getTracesName()) ? StringUtils.EMPTY : dto.getTracesName());
				rowData.add(StringUtils.isBlank(dto.getConfidence()) ? StringUtils.EMPTY : dto.getConfidence());
				rowData.add(StringUtils.isBlank(dto.getStatus()) ? StringUtils.EMPTY : dto.getStatus());
				rowData.add(StringUtils.isBlank(dto.getScore()) ? StringUtils.EMPTY : dto.getScore());

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);

			}
		}
	}
}
