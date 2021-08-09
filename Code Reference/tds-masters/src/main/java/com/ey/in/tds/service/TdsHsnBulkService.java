package com.ey.in.tds.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.domain.MasterTdsHsnCode;
import com.ey.in.tds.common.dto.TdsHsnCodeErrorReportDTO;
import com.ey.in.tds.common.dto.TdsHsnCodeExcelDTO;
import com.ey.in.tds.common.repository.NatureOfPaymentMasterRepository;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.repository.TdsHsnCodeRepository;
import com.ey.in.tds.service.util.excel.MasterExcel;
import com.ey.in.tds.service.util.excel.TdsHsnCodeExcel;
import com.microsoft.azure.storage.StorageException;

@Service
public class TdsHsnBulkService {
	
	@Autowired
	private TdsHsnCodeRepository tdsHsnCodeRepository;

	@Autowired
	private NatureOfPaymentMasterRepository natureOfPaymentMasterRepository;

	@Autowired
	private MasterBatchUploadService masterBatchUploadService;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Async
	public MasterBatchUpload  saveTdsHsnCode(XSSFWorkbook workbook, MultipartFile file, String sha256,
			Integer assesssmentYear, Integer assessmentMonth, String userName, MasterBatchUpload masterBatchUpload,
			String uploadType) throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		MultiTenantContext.setTenantId("master");
		File tdsHsnCodeErrorFile = null;
		ArrayList<TdsHsnCodeErrorReportDTO> errorList = new ArrayList<>();
		try {
			TdsHsnCodeExcel excelData = new TdsHsnCodeExcel(workbook);
			masterBatchUpload.setSha256sum(sha256);
			masterBatchUpload.setMismatchCount(0L);
			long dataRowsCount = excelData.getDataRowsCount();
			masterBatchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			int processedRecordsCount = 0;
			Long duplicateCount = 0L;
			List<String> nops = natureOfPaymentMasterRepository.findAllNOP();
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<TdsHsnCodeErrorReportDTO> errorDTO = null;
				boolean isValid = true;
				try {
					errorDTO = excelData.validate(rowIndex);
				} catch (Exception e) {
					logger.error("Unable validate row number " + rowIndex + " due to "
							+ ExceptionUtils.getRootCauseMessage(e), e);
					++errorCount;
					continue;
				}
				if (errorDTO.isPresent()) {
					++errorCount;
					errorList.add(errorDTO.get());
				} else {
					try {
						TdsHsnCodeExcelDTO tdsHsnDto = excelData.get(rowIndex);
						MasterTdsHsnCode tdsHsnCode = new MasterTdsHsnCode();
						TdsHsnCodeErrorReportDTO hsnCodeError = excelData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(hsnCodeError.getReason())) {
							hsnCodeError.setReason("");
						}
						if (String.valueOf(tdsHsnDto.getHsnCode()).matches("[0-9]{1,8}")) {
							tdsHsnCode.setHsnCode(tdsHsnDto.getHsnCode());
						} else {
							hsnCodeError.setReason(
									hsnCodeError.getReason() + "HSN/SAC Code enter max size is 8 digit number" + "\n");
							errorCount++;
							errorList.add(hsnCodeError);
							isValid = false;
						}
						if (StringUtils.isNotBlank(tdsHsnDto.getTdsSection())
								&& StringUtils.isBlank(tdsHsnDto.getNatureOfPayment())) {
							hsnCodeError.setReason("Nature of payment is mandatory.");
							errorCount++;
							errorList.add(hsnCodeError);
							isValid = false;
						}
						if (StringUtils.isNotBlank(tdsHsnDto.getNatureOfPayment())) {
							if (nops.contains(tdsHsnDto.getNatureOfPayment().trim())) {
								tdsHsnCode.setNatureOfPayment(tdsHsnDto.getNatureOfPayment().trim());
							} else {
								hsnCodeError.setReason(hsnCodeError.getReason()
										+ "Nature of payment master not found in system " + "\n");
								errorCount++;
								errorList.add(hsnCodeError);
								isValid = false;
							}
						}
						if (isValid) {
							tdsHsnCode.setDescription(tdsHsnDto.getDesc().trim());
							tdsHsnCode.setTdsSection(
									tdsHsnDto.getTdsSection() == null ? null : tdsHsnDto.getTdsSection().trim());
							tdsHsnCode.setActive(true);
							tdsHsnCode.setCreatedBy(userName);
							tdsHsnCode.setCreatedDate(Instant.now());
							tdsHsnCode.setModifiedBy(userName);
							tdsHsnCode.setModifiedDate(Instant.now());
							// save in tds_hcs_code
							try {
								tdsHsnCodeRepository.save(tdsHsnCode);
								++processedRecordsCount;
							} catch (Exception e) {
								logger.error("Duplicate hsn code not allowed");
								duplicateCount++;
							}
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						TdsHsnCodeErrorReportDTO problematicDataError = excelData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to process row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			}
			masterBatchUpload.setSuccessCount(Long.valueOf(processedRecordsCount));
			masterBatchUpload.setFailedCount(errorCount);
			masterBatchUpload.setProcessed(processedRecordsCount);
			masterBatchUpload.setDuplicateCount(duplicateCount);
			masterBatchUpload.setStatus("Processed");
			masterBatchUpload.setCreatedDate(Instant.now());
			masterBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			masterBatchUpload.setCreatedBy(userName);
			if (!errorList.isEmpty()) {
				tdsHsnCodeErrorFile = prepareHsnCodeErrorFile(file.getOriginalFilename(), errorList,
						new ArrayList<>(excelData.getHeaders()));
			}

		} catch (Exception e) {
			masterBatchUpload.setStatus("Process failed");
			logger.error("Exception occurred :", e);

		}
		return masterBatchUploadService.masterBatchUpload(masterBatchUpload, null, assesssmentYear, assessmentMonth,
				userName, tdsHsnCodeErrorFile, uploadType);
	}
	
	/**
	 * 
	 * @param originalFilename
	 * @param errorList
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	private File prepareHsnCodeErrorFile(String originalFilename, List<TdsHsnCodeErrorReportDTO> errorList,
			ArrayList<String> headers) throws Exception {
		try {
			headers.addAll(0, MasterExcel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = hsnCodeXlsxReport(errorList, headers);
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
	
	/**
	 * 
	 * @param errorList
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	private Workbook hsnCodeXlsxReport(List<TdsHsnCodeErrorReportDTO> errorList, ArrayList<String> headers)
			throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headers, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForHsnCode(errorList, worksheet, headers);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A6:B6");
		headerColorRange1.setStyle(style1);

		// Style for C6 to F6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("C6:F6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B5 column
		Cell cellB5 = worksheet.getCells().get("A5");
		cellB5.setValue("Error/Information");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "F6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:F6");
		worksheet.autoFitColumn(1);
		worksheet.autoFitRows();
		return workbook;
	}
	
	/**
	 * 
	 * @param errorList
	 * @param worksheet
	 * @param headers
	 * @throws Exception
	 */
	private void setExtractDataForHsnCode(List<TdsHsnCodeErrorReportDTO> errorList, Worksheet worksheet,
			ArrayList<String> headers) throws Exception {
		if (!errorList.isEmpty()) {
			int dataRowsStartIndex = 6;
			int serialNumber = 1;
			for (int i = 0; i < errorList.size(); i++) {
				TdsHsnCodeErrorReportDTO errorDTO = errorList.get(i);
				ArrayList<String> rowData = MasterExcel.getValues(errorDTO, TdsHsnCodeExcel.fieldMappings, headers);
				rowData.set(0, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(1, String.valueOf(serialNumber));
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				MasterExcel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headers, errorDTO.getReason());
				serialNumber++;
			}
		}
	}

}
