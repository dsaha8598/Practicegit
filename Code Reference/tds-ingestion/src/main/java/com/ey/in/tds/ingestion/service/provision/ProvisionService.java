package com.ey.in.tds.ingestion.service.provision;

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
import com.ey.in.tds.common.domain.ErrorCode;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.model.provision.ProvisionsErrorReportCsvDTO;
import com.ey.in.tds.common.service.ErrorReportService;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.jdbc.dao.ProvisionDAO;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

@Service
public class ProvisionService {

	@Autowired
	private ErrorReportService errorReportService;

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private ProvisionDAO provisionDAO;

	static Map<String, String> excelHeaderMap = new HashMap<String, String>();

	static {
		excelHeaderMap.put("Source Identifier", "D");
		excelHeaderMap.put("Source File Name", "E");
		excelHeaderMap.put("Name of the Company Code", "F");
		excelHeaderMap.put("Deductor PAN", "G");
		excelHeaderMap.put("Deductor TAN", "H");
		excelHeaderMap.put("Deductor GSTIN", "I");
		excelHeaderMap.put("Deductee Code", "J");
		excelHeaderMap.put("Name of the Deductee", "K");
		excelHeaderMap.put("Non-Resident Deductee Indicator", "L");
		excelHeaderMap.put("Deductee PAN", "M");
		excelHeaderMap.put("Deductee TIN", "N");
		excelHeaderMap.put("Deductee GSTIN", "O");
		excelHeaderMap.put("Document Number", "P");
		excelHeaderMap.put("Document Type", "Q");
		excelHeaderMap.put("Document Date", "R");
		excelHeaderMap.put("Posting Date of the Document", "S");
		excelHeaderMap.put("Entry Date of Provision Made for TDS Payable on SAC Expenses", "T");
		excelHeaderMap.put("Line Item Number", "U");
		excelHeaderMap.put("HSN/SAC", "V");
		excelHeaderMap.put("SAC Description", "W");
		excelHeaderMap.put("Service Description - Invoice", "X");
		excelHeaderMap.put("Service Description - PO", "Y");
		excelHeaderMap.put("Provision G/L Account Code", "Z");
		excelHeaderMap.put("Provision G/L Account Description", "AA");
		excelHeaderMap.put("Provisional Amount", "AB");
		excelHeaderMap.put("Section Code", "AC");
		excelHeaderMap.put("POS", "AD");
		excelHeaderMap.put("Withholding Section", "AE");
		excelHeaderMap.put("Withholding Rate/Withholding Tax Code", "AF");
		excelHeaderMap.put("Withholding Amount", "AG");
		excelHeaderMap.put("PO Number", "AH");
		excelHeaderMap.put("PO Date", "AI");
		excelHeaderMap.put("Type of PO", "AJ");
		excelHeaderMap.put("Linking of Invoice with PO", "AK");
		excelHeaderMap.put("Duplicates", "AL");
		excelHeaderMap.put("Entry Date Of Provision", "AM");
		excelHeaderMap.put("Mis Match", "AN");
		excelHeaderMap.put("Processed", "AO");
		excelHeaderMap.put("Reason", "AP");
		excelHeaderMap.put("Result", "AQ");
		excelHeaderMap.put("With Holding Rate", "AR");
		excelHeaderMap.put("User Defined Field 1", "AS");
		excelHeaderMap.put("User Defined Field 2", "AT");
		excelHeaderMap.put("User Defined Field 3", "AU");
	};

	// Provision changes for Error Report
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public File convertProvisionCsvToXlsx(File csvFile, String tan, String tenantId, String deductorPan,
			Integer assesmentYear, Integer batchId) throws Exception {

		List<ProvisionDTO> errorRecords = null;
		Reader reader = null;
		CsvToBean<ProvisionsErrorReportCsvDTO> csvToBean = null;

		errorRecords = provisionDAO.getProvisionErrorRecords(tan, assesmentYear, batchId);
		if (errorRecords.isEmpty()) {
			reader = new FileReader(csvFile);
			csvToBean = new CsvToBeanBuilder(reader).withType(ProvisionsErrorReportCsvDTO.class)
					.withIgnoreLeadingWhiteSpace(true).build();
		}

		// Aspose method to convert
		Workbook workbook = provisionXlsxReport(csvToBean != null ? csvToBean.parse() : null, errorRecords, tan,
				tenantId, deductorPan);

		// Write the output to the file
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxProvisionFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()) + "_Error_Report",
				".xlsx");

		FileUtils.writeByteArrayToFile(xlsxProvisionFile, baout.toByteArray());
		baout.close();
		return xlsxProvisionFile;
	}

	public Workbook provisionXlsxReport(List<ProvisionsErrorReportCsvDTO> provisionErrorReportsCsvList,
			List<ProvisionDTO> errorRecords, String tan, String tenantId, String deductorPan) throws Exception {

		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		String[] headerNames = new String[] { "Deductor TAN", "Error Message", "Sequence Number", "Source Identifier",
				"Source File Name", "DeductorCode", "DeductorName", "DeductorPAN", "DeductorTAN", "DeductorGSTIN",
				"DeducteeCode", "DeducteeName", "DeducteePAN", "DeducteeTIN", "DeducteeGSTIN", "ERPDocumentNumber",
				"DocumentDate", "PostingDate", "TDSDeductionDate", "DocumentType", "SupplyType", "LineItemNumber",
				"GLAccountCode", "GLAccountName", "ERPDocumentType", "HSNorSAC", "HSNorSACDesc", "ProvisionsDesc",
				"PODesc", "TaxableValue", "TDSTaxCodeERP", "TDSSection", "TDSRate", "TDSAmount", "PONumber", "PODate",
				"POType", "LinkingofInvoicewithPO", "ChallanPaidFlag", "ChallanPaidDate", "UserDefinedField1",
				"UserDefinedField2", "UserDefinedField3", "No TDS Reason"};

		worksheet.getCells().importArray(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (errorRecords.isEmpty()) {
			setExtractDataForProvision(provisionErrorReportsCsvList, worksheet, tan, deductorData.getDeductorName());
		} else {
			setExtractDataForProvisionFromDB(errorRecords, worksheet, tan, deductorData.getDeductorName(),
					deductorData.getDeductorCode());
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

		// Style for E6 to AR6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:AR6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 3, 0, 3);
		TimeZone.setDefault(TimeZone.getTimeZone("IST"));
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("Provision Error Report (Dated: " + date + ")");
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

		// column B2 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information codes");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);
		Range heardersRange = worksheet.getCells().createRange("B5:B5");
		heardersRange.merge();

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "AR6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:AR6");

		return workbook;
	}

	private void setExtractDataForProvision(List<ProvisionsErrorReportCsvDTO> provisionErrorReportsCsvList,
			Worksheet worksheet, String tan, String deductorName) throws Exception {

		if (!provisionErrorReportsCsvList.isEmpty()) {
			int rowIndex = 6;
			List<ErrorCode> errorCodesObjs = mastersClient.getAllErrorCodes().getBody().getData();
			// errorCodeService.findAll();
			Map<String, String> errorDescription = new HashMap<>();
			for (ErrorCode errorCodesObj : errorCodesObjs) {
				errorDescription.put(errorCodesObj.getCode(), errorCodesObj.getDescription());
			}
			int index = 0;
			for (ProvisionsErrorReportCsvDTO errorReportsDTO : provisionErrorReportsCsvList) {
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

				rowData.add(tan);// Deductor TAN
				rowData.add(StringUtils.isBlank(errorCodesWithNewLine) ? StringUtils.EMPTY : errorCodesWithNewLine);// Error
																													// Message
				rowData.add(index + "");// Sequence Number
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceIdentifier()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceIdentifier());// Source Identifier
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceFileName()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceFileName());// Source File Name
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorCode());// DeductorCode
				rowData.add(deductorName);// DeductorName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorPan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorPan()); // DeductorPAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorTan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorTan());// DeductorTAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorGstin()); // DeductorGSTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeCode()); // DeducteeCode
				rowData.add(StringUtils.isBlank(errorReportsDTO.getNameOfTheDeductee()) ? StringUtils.EMPTY
						: errorReportsDTO.getNameOfTheDeductee()); // DeducteeName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteePan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteePan());// DeducteePAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeTin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeTin()); // DeducteeTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeGstin());// DeducteeGSTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentNumber());// ERPDocumentNumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentDate()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentDate()); // DocumentDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPostingDateOfTheDocument()) ? StringUtils.EMPTY
						: errorReportsDTO.getPostingDateOfTheDocument()); // PostingDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTdsDeductionDate()) ? StringUtils.EMPTY
						: errorReportsDTO.getTdsDeductionDate()); // TDSDeductionDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentType()); // DocumentType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSupplyType()) ? StringUtils.EMPTY
						: errorReportsDTO.getSupplyType());// SupplyType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getLineItemNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getLineItemNumber()); // LineItemNumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getProvisionGlAccountCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getProvisionGlAccountCode()); // GLAccountCode
				rowData.add(""); // GLAccountName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentType()); // ERPDocumentType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getHsnsac()) ? StringUtils.EMPTY
						: errorReportsDTO.getHsnsac()); // HSNorSAC
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSacDescription()) ? StringUtils.EMPTY
						: errorReportsDTO.getSacDescription()); // HSNorSACDesc
				rowData.add(""); // ProvisionsDesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getServiceDescriptionPo()) ? StringUtils.EMPTY
						: errorReportsDTO.getServiceDescriptionPo());// PODesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getProvisionalAmount()) ? StringUtils.EMPTY
						: errorReportsDTO.getProvisionalAmount());// TaxableValue
				rowData.add("");// TDSTaxCodeERP
				rowData.add(""); // TDSSection
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTdsRate()) ? StringUtils.EMPTY
						: errorReportsDTO.getTdsRate()); // TDSRate
				rowData.add(""); // TDSAmount
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPoNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getPoNumber());// PONumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPoDate()) ? StringUtils.EMPTY
						: errorReportsDTO.getPoDate()); // PODate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getTypeOfPo()) ? StringUtils.EMPTY
						: errorReportsDTO.getTypeOfPo());// POType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getLinkingOfInvoiceWithPo()) ? StringUtils.EMPTY
						: errorReportsDTO.getLinkingOfInvoiceWithPo()); // LinkingofInvoicewithPO
				rowData.add(errorReportsDTO.getIsChallanPaid().equals("0") ? "Y" : "N"); // ChallanPaidFlag
				rowData.add(""); // ChallanPaidDate
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

	private void setExtractDataForProvisionFromDB(List<ProvisionDTO> provisionErrorReportsCsvList, Worksheet worksheet,
			String tan, String deductorName, String deductorCode) throws Exception {

		if (!provisionErrorReportsCsvList.isEmpty()) {
			int rowIndex = 6;
			List<ErrorCode> errorCodesObjs = mastersClient.getAllErrorCodes().getBody().getData();
			// errorCodeService.findAll();
			Map<String, String> errorDescription = new HashMap<>();
			for (ErrorCode errorCodesObj : errorCodesObjs) {
				errorDescription.put(errorCodesObj.getCode(), errorCodesObj.getDescription());
			}
			int index = 0;
			for (ProvisionDTO errorReportsDTO : provisionErrorReportsCsvList) {
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

				rowData.add(tan);// Deductor TAN
				rowData.add(StringUtils.isBlank(errorCodesWithNewLine) ? StringUtils.EMPTY : errorCodesWithNewLine);// Error
																													// Message
				rowData.add(index + "");// Sequence Number
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceIdentifier()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceIdentifier());// Source Identifier
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSourceFileName()) ? StringUtils.EMPTY
						: errorReportsDTO.getSourceFileName());// Source File Name
				rowData.add(deductorCode);// DeductorCode
				rowData.add(deductorName);// DeductorName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorPan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorPan()); // DeductorPAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorTan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorTan());// DeductorTAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeductorGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeductorGstin()); // DeductorGSTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeCode()); // DeducteeCode
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeName()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeName()); // DeducteeName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteePan()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteePan());// DeducteePAN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeTin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeTin()); // DeducteeTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDeducteeGstin()) ? StringUtils.EMPTY
						: errorReportsDTO.getDeducteeGstin());// DeducteeGSTIN
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentNumber());// ERPDocumentNumber
				rowData.add(errorReportsDTO.getDocumentDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentDate().toString()); // DocumentDate
				rowData.add(errorReportsDTO.getPostingDateOfDocument() == null ? StringUtils.EMPTY
						: errorReportsDTO.getPostingDateOfDocument().toString()); // PostingDate
				rowData.add(errorReportsDTO.getTdsDeductionDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getTdsDeductionDate().toString()); // TDSDeductionDate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentType()); // DocumentType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSupplyType()) ? StringUtils.EMPTY
						: errorReportsDTO.getSupplyType());// SupplyType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getLineItemNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getLineItemNumber()); // LineItemNumber
				rowData.add(StringUtils.isBlank(errorReportsDTO.getGlAccountCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getGlAccountCode()); // GLAccountCode
				rowData.add(StringUtils.isBlank(errorReportsDTO.getServiceDescriptionGl()) ? StringUtils.EMPTY
						: errorReportsDTO.getServiceDescriptionGl()); // GLAccountName
				rowData.add(StringUtils.isBlank(errorReportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: errorReportsDTO.getDocumentType()); // ERPDocumentType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getHsnOrSac()) ? StringUtils.EMPTY
						: errorReportsDTO.getHsnOrSac()); // HSNorSAC
				rowData.add(StringUtils.isBlank(errorReportsDTO.getHsnsacDescription()) ? StringUtils.EMPTY
						: errorReportsDTO.getHsnsacDescription()); // HSNorSACDesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getServiceDescription()) ? StringUtils.EMPTY
						: errorReportsDTO.getServiceDescription()); // ProvisionsDesc
				rowData.add(StringUtils.isBlank(errorReportsDTO.getServiceDescriptionPo()) ? StringUtils.EMPTY
						: errorReportsDTO.getServiceDescriptionPo());// PODesc
				rowData.add(errorReportsDTO.getProvisionalAmount() == null ? StringUtils.EMPTY
						: errorReportsDTO.getProvisionalAmount().toString());// TaxableValue
				rowData.add(StringUtils.isBlank(errorReportsDTO.getSectionCode()) ? StringUtils.EMPTY
						: errorReportsDTO.getSectionCode());// TDSTaxCodeERP
				rowData.add(StringUtils.isBlank(errorReportsDTO.getWithholdingSection()) ? StringUtils.EMPTY
						: errorReportsDTO.getWithholdingSection()); // TDSSection
				rowData.add(errorReportsDTO.getTdsRate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getTdsRate().toString()); // TDSRate
				rowData.add(errorReportsDTO.getWithholdingAmount()==null ? StringUtils.EMPTY
						: errorReportsDTO.getWithholdingAmount().toString()); // TDSAmount
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPoNumber()) ? StringUtils.EMPTY
						: errorReportsDTO.getPoNumber());// PONumber
				rowData.add(errorReportsDTO.getPoDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getPoDate().toString()); // PODate
				rowData.add(StringUtils.isBlank(errorReportsDTO.getPoType()) ? StringUtils.EMPTY
						: errorReportsDTO.getPoType());// POType
				rowData.add(StringUtils.isBlank(errorReportsDTO.getLinkingInvoicePo()) ? StringUtils.EMPTY
						: errorReportsDTO.getLinkingInvoicePo()); // LinkingofInvoicewithPO
				rowData.add(errorReportsDTO.getChallanPaid() == true ? "Y" : "N"); // ChallanPaidFlag
				rowData.add(errorReportsDTO.getChallanPaidDate() == null ? StringUtils.EMPTY
						: errorReportsDTO.getChallanPaidDate().toString()); // ChallanPaidDate
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

	/**
	 * This method for convert provision csv to xlsx file.
	 * 
	 * @param csvFile
	 * @param tan
	 * @return
	 * @throws Exception
	 */
	public File convertProvisionOtherReportToXlsx(File csvFile, String tan) throws Exception {

		// Reading csv from file path to java objects
		Reader reader = new FileReader(csvFile);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		CsvToBean<ProvisionsErrorReportCsvDTO> csvToBean = new CsvToBeanBuilder(reader)
				.withType(ProvisionsErrorReportCsvDTO.class).withIgnoreLeadingWhiteSpace(true).build();

		// Aspose method to convert
		Workbook workbook = generateProvisionOtherXlsxReport(csvToBean.parse(), tan);

		// Write the output to the file
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File xlsxProvisionFile = File.createTempFile(FilenameUtils.getBaseName(csvFile.getName()) + "_Other", ".xlsx");
		FileUtils.writeByteArrayToFile(xlsxProvisionFile, baout.toByteArray());
		baout.close();
		return xlsxProvisionFile;
	}

	/**
	 * 
	 * @param provisionCsvDTO
	 * @param tan
	 * @return
	 * @throws Exception
	 */
	public Workbook generateProvisionOtherXlsxReport(List<ProvisionsErrorReportCsvDTO> provisionCsvDTO, String tan)
			throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error Details");

		String[] headerNames = new String[] { "Source Identifier", "Source File Name", "Name of the Company Code",
				"Deductor PAN", "Deductor TAN", "Deductor GSTIN", "Deductee Code", "Name of the Deductee",
				"Non-Resident Deductee Indicator", "Deductee PAN", "Deductee TIN", "Deductee GSTIN", "Document Number",
				"Document Type", "Document Date", "Posting Date of the Document",
				"Entry Date of Provision Made for TDS Payable on SAC Expenses", "Line Item Number", "HSN/SAC",
				"SAC Description", "Service Description - Invoice", "Service Description - PO",
				"Provision G/L Account Code", "Provision G/L Account Description", "Provisional Amount", "Section Code",
				"POS", "Withholding Section", "Withholding Rate/Withholding Tax Code", "Withholding Amount",
				"PO Number", "PO Date", "Type of PO", "Linking of Invoice with PO", "User Defined Field 1",
				"User Defined Field 2", "User Defined Field 3", "Section Predection" };

		worksheet.getCells().importArray(headerNames, 0, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForProvisionOtherReport(provisionCsvDTO, worksheet);

		// Style for A1 to AK1 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(91, 155, 213));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A1:AL1");
		headerColorRange1.setStyle(style1);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A1";
		String lastHeaderCellName = "AL1";
		String firstDataCellName = "A2";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:AL1");

		return workbook;
	}

	/**
	 * 
	 * @param provisionCsvDTO
	 * @param worksheet
	 * @throws Exception
	 */
	private void setExtractDataForProvisionOtherReport(List<ProvisionsErrorReportCsvDTO> provisionCsvDTO,
			Worksheet worksheet) throws Exception {

		if (!provisionCsvDTO.isEmpty()) {
			int rowIndex = 1;
			for (ProvisionsErrorReportCsvDTO reportsDTO : provisionCsvDTO) {
				ArrayList<String> rowData = new ArrayList<String>();

				rowData.add(StringUtils.isBlank(reportsDTO.getSourceIdentifier()) ? StringUtils.EMPTY
						: reportsDTO.getSourceIdentifier());
				rowData.add(StringUtils.isBlank(reportsDTO.getSourceFileName()) ? StringUtils.EMPTY
						: reportsDTO.getSourceFileName());
				rowData.add(StringUtils.isBlank(reportsDTO.getNameOfTheCompanyCode()) ? StringUtils.EMPTY
						: reportsDTO.getNameOfTheCompanyCode());
				rowData.add(StringUtils.isBlank(reportsDTO.getDeductorPan()) ? StringUtils.EMPTY
						: reportsDTO.getDeductorPan());
				rowData.add(StringUtils.isBlank(reportsDTO.getDeductorTan()) ? StringUtils.EMPTY
						: reportsDTO.getDeductorTan());
				rowData.add(StringUtils.isBlank(reportsDTO.getDeductorGstin()) ? StringUtils.EMPTY
						: reportsDTO.getDeductorGstin());
				rowData.add(StringUtils.isBlank(reportsDTO.getDeducteeCode()) ? StringUtils.EMPTY
						: reportsDTO.getDeducteeCode());
				rowData.add(StringUtils.isBlank(reportsDTO.getNameOfTheDeductee()) ? StringUtils.EMPTY
						: reportsDTO.getNameOfTheDeductee());
				rowData.add(StringUtils.isBlank(reportsDTO.getNonresidentDeducteeIndicator()) ? StringUtils.EMPTY
						: reportsDTO.getNonresidentDeducteeIndicator());
				rowData.add(StringUtils.isBlank(reportsDTO.getDeducteePan()) ? StringUtils.EMPTY
						: reportsDTO.getDeducteePan());
				rowData.add(StringUtils.isBlank(reportsDTO.getDeducteeTin()) ? StringUtils.EMPTY
						: reportsDTO.getDeducteeTin());
				rowData.add(StringUtils.isBlank(reportsDTO.getDeducteeGstin()) ? StringUtils.EMPTY
						: reportsDTO.getDeducteeGstin());
				rowData.add(StringUtils.isBlank(reportsDTO.getDocumentNumber()) ? StringUtils.EMPTY
						: reportsDTO.getDocumentNumber());
				rowData.add(StringUtils.isBlank(reportsDTO.getDocumentType()) ? StringUtils.EMPTY
						: reportsDTO.getDocumentType());
				rowData.add(StringUtils.isBlank(reportsDTO.getDocumentDate()) ? StringUtils.EMPTY
						: reportsDTO.getDocumentDate());
				rowData.add(StringUtils.isBlank(reportsDTO.getPostingDateOfTheDocument()) ? StringUtils.EMPTY
						: reportsDTO.getPostingDateOfTheDocument());
				rowData.add(StringUtils.isBlank(reportsDTO.getEntryDateOfProvision()) ? StringUtils.EMPTY
						: reportsDTO.getEntryDateOfProvision());
				rowData.add(StringUtils.isBlank(reportsDTO.getLineItemNumber()) ? StringUtils.EMPTY
						: reportsDTO.getLineItemNumber());
				rowData.add(StringUtils.isBlank(reportsDTO.getHsnsac()) ? StringUtils.EMPTY : reportsDTO.getHsnsac());
				rowData.add(StringUtils.isBlank(reportsDTO.getSacDescription()) ? StringUtils.EMPTY
						: reportsDTO.getSacDescription());
				rowData.add(StringUtils.isBlank(reportsDTO.getServiceDescriptionInvoice()) ? StringUtils.EMPTY
						: reportsDTO.getServiceDescriptionInvoice());
				rowData.add(StringUtils.isBlank(reportsDTO.getServiceDescriptionPo()) ? StringUtils.EMPTY
						: reportsDTO.getServiceDescriptionPo());
				rowData.add(StringUtils.isBlank(reportsDTO.getProvisionGlAccountCode()) ? StringUtils.EMPTY
						: reportsDTO.getProvisionGlAccountCode());
				rowData.add(StringUtils.isBlank(reportsDTO.getProvisionGlAccountDescription()) ? StringUtils.EMPTY
						: reportsDTO.getProvisionGlAccountDescription());
				rowData.add(StringUtils.isBlank(reportsDTO.getProvisionalAmount()) ? StringUtils.EMPTY
						: reportsDTO.getProvisionalAmount());
				rowData.add(StringUtils.isBlank(reportsDTO.getSectionCode()) ? StringUtils.EMPTY
						: reportsDTO.getSectionCode());
				rowData.add(StringUtils.isBlank(reportsDTO.getPos()) ? StringUtils.EMPTY : reportsDTO.getPos());
				rowData.add(StringUtils.isBlank(reportsDTO.getWithholdingSection()) ? StringUtils.EMPTY
						: reportsDTO.getWithholdingSection());
				rowData.add(StringUtils.isBlank(reportsDTO.getWithholdingRate()) ? StringUtils.EMPTY
						: reportsDTO.getWithholdingRate());
				rowData.add(StringUtils.isBlank(reportsDTO.getWithholdingAmount()) ? StringUtils.EMPTY
						: reportsDTO.getWithholdingAmount());
				rowData.add(
						StringUtils.isBlank(reportsDTO.getPoNumber()) ? StringUtils.EMPTY : reportsDTO.getPoNumber());
				rowData.add(StringUtils.isBlank(reportsDTO.getPoDate()) ? StringUtils.EMPTY : reportsDTO.getPoDate());
				rowData.add(
						StringUtils.isBlank(reportsDTO.getTypeOfPo()) ? StringUtils.EMPTY : reportsDTO.getTypeOfPo());
				rowData.add(StringUtils.isBlank(reportsDTO.getLinkingOfInvoiceWithPo()) ? StringUtils.EMPTY
						: reportsDTO.getLinkingOfInvoiceWithPo());
				rowData.add(StringUtils.isBlank(reportsDTO.getUserDefinedField1()) ? StringUtils.EMPTY
						: reportsDTO.getUserDefinedField1());
				rowData.add(StringUtils.isBlank(reportsDTO.getUserDefinedField2()) ? StringUtils.EMPTY
						: reportsDTO.getUserDefinedField2());
				rowData.add(StringUtils.isBlank(reportsDTO.getUserDefinedField3()) ? StringUtils.EMPTY
						: reportsDTO.getUserDefinedField3());
				rowData.add(StringUtils.isBlank(reportsDTO.getTdsSectionPrediction()) ? StringUtils.EMPTY
						: reportsDTO.getTdsSectionPrediction());

				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}
	}
}
