package com.ey.in.tds.onboarding.service.ldc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
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
import com.ey.in.tds.common.model.ao.AoExcelErrorDTO;
import com.ey.in.tds.common.model.ldc.LccErrorReportCsvDTO;
import com.ey.in.tds.common.model.ldc.LdcErrorReportCsvDTO;
import com.ey.in.tds.common.model.ldc.TracesLdcMasterErrorReportDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccMaster;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.onboarding.service.util.excel.ao.AoExcel;
import com.ey.in.tds.onboarding.service.util.excel.ldc.LccExcel;
import com.ey.in.tds.onboarding.service.util.excel.ldc.LdcExcel;
import com.ey.in.tds.onboarding.service.util.excel.ldc.TracesLdcMasterExcel;

@Component
public class ErrorFileLdcUpload {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	File ldcErrorFile(String originalFileName, String deductorTan, String deductorPan,
			ArrayList<LdcErrorReportCsvDTO> errorList, ArrayList<String> headers) throws Exception {
		try {
			headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = ldcMasterXlsxReport(errorList, deductorTan, deductorPan, headers);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			wkBook.save(baout, SaveFormat.XLSX);
			File errorFile = new File(
					FilenameUtils.getBaseName(originalFileName) + "_" + UUID.randomUUID() + "_Error_Report.xlsx");
			FileUtils.writeByteArrayToFile(errorFile, baout.toByteArray());
			baout.close();
			return errorFile;
		} catch (Exception e) {
			logger.error("Encountered an error while preparing error file", e);
			throw e;
		}
	}

	public Workbook ldcMasterXlsxReport(List<LdcErrorReportCsvDTO> errorDTOs, String deductorTan, String deductorPan,
			ArrayList<String> headerNames) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForLdcMaster(errorDTOs, worksheet, deductorTan, headerNames);

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

		// Style for E6 to AT6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:T6");
		headerColorRange2.setStyle(style2);

		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		List<DeductorMaster> response = deductorMasterDAO.findBasedOnDeductorPan(deductorPan);

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		if (!response.isEmpty()) {
			cellA2.setValue("Client Name:" + response.get(0).getName());
		} else {
			cellA2.setValue("Client Name:" + StringUtils.EMPTY);
		}
		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B2 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

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
		autoFilter.setRange("A6:T6");
		worksheet.autoFitRows();
		worksheet.autoFitColumns();
		return workbook;
	}

	private void setExtractDataForLdcMaster(List<LdcErrorReportCsvDTO> errorDTOs, Worksheet worksheet,
			String deductorTan, List<String> headerNames) throws Exception {

		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			int serialNumber = 1;
			for (int i = 0; i < errorDTOs.size(); i++) {
				LdcErrorReportCsvDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = Excel.getValues(errorDTO, LdcExcel.fieldMappings, headerNames);
				rowData.set(0, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(2, String.valueOf(serialNumber));
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
				serialNumber++;
			}
		}
	}

	/**
	 * 
	 * @param ldcMasterPans
	 * @return
	 * @throws Exception
	 */
	MultipartFile generateLdcPanXlsxReport(List<LdcMaster> listLdc) throws Exception {
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		String[] headerNames = new String[] { "Certificate Number", "Financial Year", "Pan of Employee",
				"Name of Deductee", "Valid from", "Valid to", "Section Code", "Nature of Payment", "Rate of tds",
				"Certificate limit", "Amount Consumed", "Date of issue" };
		worksheet.getCells().importArray(headerNames, 0, 0, false);
		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);
		if (!listLdc.isEmpty()) {
			int rowIndex = 1;
			for (LdcMaster ldcMaster : listLdc) {
				ArrayList<Object> rowData = new ArrayList<Object>();
				rowData.add(StringUtils.isBlank(ldcMaster.getCertificateNumber()) ? StringUtils.EMPTY
						: ldcMaster.getCertificateNumber());
				// financial year
				rowData.add(ldcMaster.getAssessmentYear() == null ? StringUtils.EMPTY : ldcMaster.getAssessmentYear());
				rowData.add(StringUtils.isBlank(ldcMaster.getPan()) ? StringUtils.EMPTY : ldcMaster.getPan());
				rowData.add(StringUtils.isBlank(ldcMaster.getDeducteeName()) ? StringUtils.EMPTY
						: ldcMaster.getDeducteeName());
				rowData.add(ldcMaster.getApplicableFrom() == null ? StringUtils.EMPTY : ldcMaster.getApplicableFrom());
				rowData.add(ldcMaster.getApplicableTo() == null ? StringUtils.EMPTY : ldcMaster.getApplicableTo());
				rowData.add(StringUtils.isBlank(ldcMaster.getNatureOfPayment()) ? StringUtils.EMPTY
						: ldcMaster.getNatureOfPayment());
				rowData.add(StringUtils.isBlank(ldcMaster.getNatureOfPayment()) ? StringUtils.EMPTY
						: ldcMaster.getNatureOfPayment());
				rowData.add(ldcMaster.getRate() == null ? StringUtils.EMPTY : ldcMaster.getRate());
				rowData.add(ldcMaster.getAmount() == null ? StringUtils.EMPTY : ldcMaster.getAmount());
				rowData.add(ldcMaster.getUtilizedAmount() == null ? StringUtils.EMPTY : ldcMaster.getUtilizedAmount());
				rowData.add(ldcMaster.getCreatedDate() == null ? StringUtils.EMPTY : ldcMaster.getCreatedDate());

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}
		File file = new File("ldc_upload_template_" + UUID.randomUUID() + ".xlsx");
		OutputStream out = new FileOutputStream(file);
		workbook.save(out, SaveFormat.XLSX);
		InputStream inputstream = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "application/vnd.ms-excel",
				IOUtils.toByteArray(inputstream));
		inputstream.close();
		out.close();
		return multipartFile;
	}

	/**
	 * 
	 * @param listLcc
	 * @return
	 * @throws Exception
	 */
	public MultipartFile generateLccPanXlsxReport(List<TCSLccMaster> listLcc) throws Exception {
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		String[] headerNames = new String[] { "LCC Certificate Number", "Collector TAN", "Collectee Name",
				"Collectee PAN", "Section", "Nature of income", "Amount", "Certificate rate", "Valid From", "Valid To",
				"Cancel Date", "Traces validation status", "Traces validation date", "Limit utilised", "Action" };
		worksheet.getCells().importArray(headerNames, 0, 0, false);
		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);
		if (!listLcc.isEmpty()) {
			int rowIndex = 1;
			for (TCSLccMaster lccMaster : listLcc) {
				ArrayList<Object> rowData = new ArrayList<>();
				rowData.add(StringUtils.isBlank(lccMaster.getCertificateNumber()) ? StringUtils.EMPTY
						: lccMaster.getCertificateNumber());
				rowData.add(StringUtils.isBlank(lccMaster.getCollectorMasterTan()) ? StringUtils.EMPTY
						: lccMaster.getCollectorMasterTan());
				rowData.add(StringUtils.isBlank(lccMaster.getCollecteeName()) ? StringUtils.EMPTY
						: lccMaster.getCollecteeName());
				rowData.add(StringUtils.isBlank(lccMaster.getLccMasterPan()) ? StringUtils.EMPTY
						: lccMaster.getLccMasterPan());
				rowData.add(StringUtils.isBlank(lccMaster.getSectionAsPerTraces()) ? StringUtils.EMPTY
						: lccMaster.getSectionAsPerTraces());
				rowData.add(StringUtils.isBlank(lccMaster.getNatureOfIncome()) ? StringUtils.EMPTY
						: lccMaster.getNatureOfIncome());
				rowData.add(lccMaster.getAmount() == null ? StringUtils.EMPTY : lccMaster.getAmount());
				rowData.add(
						lccMaster.getRateAsPerTraces() == null ? StringUtils.EMPTY : lccMaster.getRateAsPerTraces());
				rowData.add(lccMaster.getApplicableFrom() == null ? StringUtils.EMPTY : lccMaster.getApplicableFrom());
				rowData.add(lccMaster.getApplicableTo() == null ? StringUtils.EMPTY : lccMaster.getApplicableTo());
				rowData.add(lccMaster.getApplicableTo() == null ? StringUtils.EMPTY : lccMaster.getApplicableTo());
				rowData.add(
						lccMaster.getValidationStatus() == null ? StringUtils.EMPTY : lccMaster.getValidationStatus());
				rowData.add(lccMaster.getValidationDate() == null ? StringUtils.EMPTY : lccMaster.getValidationDate());
				rowData.add(lccMaster.getUtilizedAmount() == null ? StringUtils.EMPTY : lccMaster.getUtilizedAmount());
				rowData.add(lccMaster.getAction() == null ? StringUtils.EMPTY : lccMaster.getAction());

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}
		File file = new File("lcc_upload_template_" + UUID.randomUUID() + ".xlsx");
		OutputStream out = new FileOutputStream(file);
		workbook.save(out, SaveFormat.XLSX);
		InputStream inputstream = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "application/vnd.ms-excel",
				IOUtils.toByteArray(inputstream));
		inputstream.close();
		out.close();
		return multipartFile;
	}

	/**
	 * 
	 * @param originalFileName
	 * @param deductorTan
	 * @param deductorPan
	 * @param errorList
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public File lccErrorFile(String originalFileName, String deductorTan, String deductorPan,
			ArrayList<LccErrorReportCsvDTO> errorList, ArrayList<String> headers) throws Exception {
		try {
			headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = lccMasterXlsxReport(errorList, deductorTan, deductorPan, headers);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			wkBook.save(baout, SaveFormat.XLSX);
			File errorFile = new File(
					FilenameUtils.getBaseName(originalFileName) + "_" + UUID.randomUUID() + "_Error_Report.xlsx");
			FileUtils.writeByteArrayToFile(errorFile, baout.toByteArray());
			baout.close();
			return errorFile;
		} catch (Exception e) {
			logger.error("Encountered an error while preparing error file", e);
			throw e;
		}
	}

	/**
	 * 
	 * @param errorDTOs
	 * @param deductorTan
	 * @param deductorPan
	 * @param headerNames
	 * @return
	 * @throws Exception
	 */
	public Workbook lccMasterXlsxReport(List<LccErrorReportCsvDTO> errorDTOs, String deductorTan, String deductorPan,
			ArrayList<String> headerNames) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForLccMaster(errorDTOs, worksheet, deductorTan, headerNames);

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

		// Style for D6 to R6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:R6");
		headerColorRange2.setStyle(style2);

		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		List<DeductorMaster> response = deductorMasterDAO.findBasedOnDeductorPan(deductorPan);

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		if (!response.isEmpty()) {
			cellA2.setValue("Client Name:" + response.get(0).getName());
		} else {
			cellA2.setValue("Client Name:" + StringUtils.EMPTY);
		}
		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B2 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "R6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:R6");
		worksheet.autoFitRows();
		worksheet.autoFitColumns();
		return workbook;
	}

	/**
	 * 
	 * @param errorDTOs
	 * @param worksheet
	 * @param deductorTan
	 * @param headerNames
	 * @throws Exception
	 */
	private void setExtractDataForLccMaster(List<LccErrorReportCsvDTO> errorDTOs, Worksheet worksheet,
			String deductorTan, List<String> headerNames) throws Exception {

		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			for (int i = 0; i < errorDTOs.size(); i++) {
				LccErrorReportCsvDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = Excel.getValues(errorDTO, LccExcel.fieldMappings, headerNames);
				rowData.set(0, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(2, StringUtils.isBlank(errorDTO.getSerialNumber()) ? StringUtils.EMPTY
						: errorDTO.getSerialNumber());
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
			}
		}
	}

	public File aoErrorFile(String originalFileName, String deductorTan, String deductorPan,
			ArrayList<AoExcelErrorDTO> errorList, ArrayList<String> headers) throws Exception {
		try {
			headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = aoMasterXlsxReport(errorList, deductorTan, deductorPan, headers);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			wkBook.save(baout, SaveFormat.XLSX);
			File errorFile = new File(
					FilenameUtils.getBaseName(originalFileName) + "_" + UUID.randomUUID() + "_Error_Report.xlsx");
			FileUtils.writeByteArrayToFile(errorFile, baout.toByteArray());
			baout.close();
			return errorFile;
		} catch (Exception e) {
			logger.error("Encountered an error while preparing error file", e);
			throw e;
		}
	}

	public Workbook aoMasterXlsxReport(List<AoExcelErrorDTO> errorDTOs, String deductorTan, String deductorPan,
			ArrayList<String> headerNames) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForAoMaster(errorDTOs, worksheet, deductorTan, headerNames);

		// Style for A6 to D6 headers
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

		// Style for E6 to AT6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:AB6");
		headerColorRange2.setStyle(style2);

		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		List<DeductorMaster> response = deductorMasterDAO.findBasedOnDeductorPan(deductorPan);

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		if (!response.isEmpty()) {
			cellA2.setValue("Client Name:" + response.get(0).getName());
		} else {
			cellA2.setValue("Client Name:" + StringUtils.EMPTY);
		}
		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B2 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

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
		worksheet.autoFitRows();
		worksheet.autoFitColumns();
		return workbook;
	}

	private void setExtractDataForAoMaster(List<AoExcelErrorDTO> errorDTOs, Worksheet worksheet, String deductorTan,
			List<String> headerNames) throws Exception {

		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			for (int i = 0; i < errorDTOs.size(); i++) {
				AoExcelErrorDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = Excel.getValues(errorDTO, AoExcel.fieldMappings, headerNames);
				rowData.set(0, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(2, StringUtils.isBlank(errorDTO.getSerialNumber()) ? StringUtils.EMPTY
						: errorDTO.getSerialNumber());
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
			}
		}
	}

	public File ldcTracesErrorFile(String originalFilename, String deductorTan, String deductorPan,
			ArrayList<TracesLdcMasterErrorReportDTO> errorList, ArrayList<String> headers) throws Exception {
		try {
			headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = tracesldcMasterXlsxReport(errorList, deductorTan, deductorPan, headers);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			wkBook.save(baout, SaveFormat.XLSX);
			File errorFile = new File(
					FilenameUtils.getBaseName(originalFilename) + "_" + UUID.randomUUID() + "_Error_Report.xlsx");
			FileUtils.writeByteArrayToFile(errorFile, baout.toByteArray());
			baout.close();
			return errorFile;
		} catch (Exception e) {
			logger.error("Encountered an error while preparing error file", e);
			throw e;
		}
	}

	public Workbook tracesldcMasterXlsxReport(List<TracesLdcMasterErrorReportDTO> errorDTOs, String deductorTan,
			String deductorPan, ArrayList<String> headerNames) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForTracesLdcMaster(errorDTOs, worksheet, deductorTan, headerNames);

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

		// Style for E6 to AT6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:V6");
		headerColorRange2.setStyle(style2);

		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		List<DeductorMaster> response = deductorMasterDAO.findBasedOnDeductorPan(deductorPan);

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		if (!response.isEmpty()) {
			cellA2.setValue("Client Name:" + response.get(0).getName());
		} else {
			cellA2.setValue("Client Name:" + StringUtils.EMPTY);
		}
		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B2 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "V6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:V6");
		worksheet.autoFitRows();
		worksheet.autoFitColumns();
		return workbook;
	}

	/**
	 * 
	 * @param errorDTOs
	 * @param worksheet
	 * @param deductorTan
	 * @param headerNames
	 * @throws Exception
	 */
	private void setExtractDataForTracesLdcMaster(List<TracesLdcMasterErrorReportDTO> errorDTOs, Worksheet worksheet,
			String deductorTan, List<String> headerNames) throws Exception {

		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			int serialNumber = 1;
			for (int i = 0; i < errorDTOs.size(); i++) {
				TracesLdcMasterErrorReportDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = Excel.getValues(errorDTO, TracesLdcMasterExcel.fieldMappings, headerNames);
				rowData.set(0, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(2, String.valueOf(serialNumber));
				rowData.set(3, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
				serialNumber++;
			}
		}
	}
}
