package com.ey.in.tds.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.Month;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.domain.TdsMonthTracker;
import com.ey.in.tds.common.dto.tdsmonthlytracker.MonthTrackerDTO;
import com.ey.in.tds.common.repository.TdsMonthTrackerRepository;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.dto.TdsMonthTrackereDTO;
import com.ey.in.tds.dto.TdsMonthTrackereErrorReportDTO;
import com.ey.in.tds.repository.MasterBatchUploadRepository;
import com.ey.in.tds.service.util.excel.MasterExcel;
import com.ey.in.tds.service.util.excel.TdsMonthlyTrackerExcel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.storage.StorageException;

@Service
public class TdsMonthTrackerService {

	@Autowired
	private TdsMonthTrackerRepository tdsMonthTrackerRepository;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private MasterBatchUploadService masterBatchUploadService;

	@Autowired
	private MasterBatchUploadRepository masterBatchUploadRepository;

	private final Logger logger = LoggerFactory.getLogger(TdsMonthTrackerService.class);

	public TdsMonthTracker save(@Valid TdsMonthTracker tdsMonthTracker, String userName) {
		tdsMonthTracker.setActive(true);
		tdsMonthTracker.setCreatedBy(userName);
		tdsMonthTracker.setCreatedDate(Instant.now());
		return tdsMonthTrackerRepository.save(tdsMonthTracker);

	}

	public List<MonthTrackerDTO> findAll() {
		List<TdsMonthTracker> tdsMonthTrackers = tdsMonthTrackerRepository.getAllTdsMonthTracker();
		List<MonthTrackerDTO> tdsMonthTrackerList = new ArrayList<>();
		for (TdsMonthTracker tdsMonthTrackerObj : tdsMonthTrackers) {
			MonthTrackerDTO tdsMonthTrackerDTO = new MonthTrackerDTO();
			tdsMonthTrackerDTO.setId(tdsMonthTrackerObj.getId());
			tdsMonthTrackerDTO.setDueDateForChallanPayment(tdsMonthTrackerObj.getDueDateForChallanPayment());
			tdsMonthTrackerDTO.setDueDateForFiling(tdsMonthTrackerObj.getDueDateForFiling());
			tdsMonthTrackerDTO.setMonth(tdsMonthTrackerObj.getMonth());
			tdsMonthTrackerDTO.setMonthClosureForProcessing(tdsMonthTrackerObj.getMonthClosureForProcessing());
			tdsMonthTrackerDTO.setYear(tdsMonthTrackerObj.getYear());
			tdsMonthTrackerDTO.setApplicableFrom(tdsMonthTrackerObj.getApplicableFrom());
			tdsMonthTrackerDTO.setApplicableTo(tdsMonthTrackerObj.getApplicableTo());
			tdsMonthTrackerDTO.setMonthName(Month.of(tdsMonthTrackerObj.getMonth()).name());
			tdsMonthTrackerList.add(tdsMonthTrackerDTO);
		}
		return tdsMonthTrackerList;
	}

	public MonthTrackerDTO findById(Long id) {
		Optional<TdsMonthTracker> tdsMonthTrackerObj = tdsMonthTrackerRepository.findById(id);
		MonthTrackerDTO tdsMonthTrackerDTO = new MonthTrackerDTO();
		if (tdsMonthTrackerObj.isPresent()) {
			tdsMonthTrackerDTO.setId(tdsMonthTrackerObj.get().getId());
			tdsMonthTrackerDTO.setDueDateForChallanPayment(tdsMonthTrackerObj.get().getDueDateForChallanPayment());
			tdsMonthTrackerDTO.setDueDateForFiling(tdsMonthTrackerObj.get().getDueDateForFiling());
			tdsMonthTrackerDTO.setMonth(tdsMonthTrackerObj.get().getMonth());
			tdsMonthTrackerDTO.setMonthClosureForProcessing(tdsMonthTrackerObj.get().getMonthClosureForProcessing());
			tdsMonthTrackerDTO.setYear(tdsMonthTrackerObj.get().getYear());
			tdsMonthTrackerDTO.setApplicableFrom(tdsMonthTrackerObj.get().getApplicableFrom());
			tdsMonthTrackerDTO.setApplicableTo(tdsMonthTrackerObj.get().getApplicableTo());
			tdsMonthTrackerDTO.setMonthName(Month.of(tdsMonthTrackerObj.get().getMonth()).name());
		}
		return tdsMonthTrackerDTO;
	}

	public TdsMonthTracker update(@Valid TdsMonthTracker tdsMonthTracker, String userName)
			throws JsonProcessingException {
		TdsMonthTracker monthTracker = new TdsMonthTracker();
		Optional<TdsMonthTracker> monthTrackerObj = tdsMonthTrackerRepository.findById(tdsMonthTracker.getId());
		if (monthTrackerObj.isPresent()) {
			monthTracker = monthTrackerObj.get();
			monthTracker.setDueDateForChallanPayment(tdsMonthTracker.getDueDateForChallanPayment());
			monthTracker.setDueDateForFiling(tdsMonthTracker.getDueDateForFiling());
			monthTracker.setModifiedBy(userName);
			monthTracker.setModifiedDate(Instant.now());
			monthTracker.setMonth(tdsMonthTracker.getMonth());
			monthTracker.setMonthClosureForProcessing(tdsMonthTracker.getMonthClosureForProcessing());
			monthTracker.setYear(tdsMonthTracker.getYear());
			monthTracker.setApplicableFrom(tdsMonthTracker.getApplicableFrom());
			monthTracker.setApplicableTo(tdsMonthTracker.getApplicableTo());
			monthTracker = tdsMonthTrackerRepository.save(monthTracker);
		}

		return monthTracker;
	}

	public TdsMonthTracker findByAssessmentYearMonth(Integer year, Integer month) {
		TdsMonthTracker tdsMonthTracker = new TdsMonthTracker();
		Optional<TdsMonthTracker> tdsMonthTrackerObj = tdsMonthTrackerRepository.findByAssessmentYearMonth(year, month);
		if (tdsMonthTrackerObj.isPresent()) {
			return tdsMonthTrackerObj.get();
		}
		return tdsMonthTracker;
	}
	
	public TdsMonthTracker findByDueDateChallanPayment(String challanGeneratedDate) {
		TdsMonthTracker tdsMonthTracker = new TdsMonthTracker();
		Optional<TdsMonthTracker> tdsMonthTrackerObj = tdsMonthTrackerRepository
				.findByDueDateChallanPayment(challanGeneratedDate);
		if (tdsMonthTrackerObj.isPresent()) {
			return tdsMonthTrackerObj.get();
		}
		return tdsMonthTracker;
	}

	/**
	 * 
	 * @param file
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	@Transactional
	public MasterBatchUpload saveFileData(MultipartFile file, Integer assesssmentYear, Integer assessmentMonth,
			String userName) throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		String uploadType = UploadTypes.TDS_MONTHLY_TRACKER_EXCEL.name();
		String sha256 = sha256SumService.getSHA256Hash(file);
		if (isAlreadyProcessed(sha256)) {
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			masterBatchUpload.setCreatedDate(Instant.now());
			masterBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			masterBatchUpload.setCreatedBy(userName);
			masterBatchUpload.setSuccessCount(0L);
			masterBatchUpload.setFailedCount(0L);
			masterBatchUpload.setRowsCount(0L);
			masterBatchUpload.setProcessed(0);
			masterBatchUpload.setMismatchCount(0L);
			masterBatchUpload.setSha256sum(sha256);
			masterBatchUpload.setStatus("Duplicate");
			masterBatchUpload.setNewStatus("Duplicate");
			masterBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			masterBatchUpload = masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear,
					assessmentMonth, userName, null, uploadType);
			return masterBatchUpload;
		}
		try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = TdsMonthlyTrackerExcel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			if (headersCount != TdsMonthlyTrackerExcel.fieldMappings.size()) {
				masterBatchUpload.setCreatedDate(Instant.now());
				masterBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				masterBatchUpload.setSuccessCount(0L);
				masterBatchUpload.setFailedCount(0L);
				masterBatchUpload.setRowsCount(0L);
				masterBatchUpload.setProcessed(0);
				masterBatchUpload.setMismatchCount(0L);
				masterBatchUpload.setSha256sum(sha256);
				masterBatchUpload.setStatus("File Error");
				masterBatchUpload.setCreatedBy(userName);
				masterBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear,
						assessmentMonth, userName, null, uploadType);
			} else {
				masterBatchUpload.setCreatedDate(Instant.now());
				masterBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				masterBatchUpload.setStatus("Processing");
				masterBatchUpload.setSuccessCount(0L);
				masterBatchUpload.setFailedCount(0L);
				masterBatchUpload.setRowsCount(0L);
				masterBatchUpload.setProcessed(0);
				masterBatchUpload.setMismatchCount(0L);
				masterBatchUpload = masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear,
						assessmentMonth, userName, null, uploadType);
			}
			if (headersCount == TdsMonthlyTrackerExcel.fieldMappings.size()) {
				return saveMonthTrackerData(workbook, file, sha256, assesssmentYear, assessmentMonth, userName,
						masterBatchUpload, uploadType);
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process month tracker data ", e);
		}
	}

	/**
	 * 
	 * @param workbook
	 * @param file
	 * @param sha256
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param masterBatchUpload
	 * @param uploadType
	 * @return
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	@Async
	private MasterBatchUpload saveMonthTrackerData(XSSFWorkbook workbook, MultipartFile file, String sha256,
			Integer assesssmentYear, Integer assessmentMonth, String userName, MasterBatchUpload masterBatchUpload,
			String uploadType) throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		File monthTrackerErrorFile = null;
		ArrayList<TdsMonthTrackereErrorReportDTO> errorList = new ArrayList<>();
		try {
			TdsMonthlyTrackerExcel excelData = new TdsMonthlyTrackerExcel(workbook);
			masterBatchUpload.setSha256sum(sha256);
			masterBatchUpload.setMismatchCount(0L);
			long dataRowsCount = excelData.getDataRowsCount();
			masterBatchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			int processedRecordsCount = 0;
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<TdsMonthTrackereErrorReportDTO> errorDTO = null;
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
						TdsMonthTrackereDTO monthTrackerDTO = excelData.get(rowIndex);
						TdsMonthTracker monthTracker = new TdsMonthTracker();
						monthTracker.setCreatedBy(userName);
						monthTracker.setCreatedDate(Instant.now());
						monthTracker.setModifiedBy(userName);
						monthTracker.setModifiedDate(Instant.now());
						monthTracker.setActive(true);
						monthTracker.setApplicableFrom(monthTrackerDTO.getApplicableFrom().toInstant());
						if (monthTrackerDTO.getApplicableTo() != null) {
							monthTracker.setApplicableTo(monthTrackerDTO.getApplicableTo().toInstant());
						}
						monthTracker.setDueDateForChallanPayment(monthTrackerDTO.getDueDateForChallanPayment());
						monthTracker.setDueDateForFiling(monthTrackerDTO.getDueDateForFiling());
						monthTracker.setMonth(monthTrackerDTO.getMonth());
						monthTracker.setYear(monthTrackerDTO.getYear());
						monthTracker.setMonthClosureForProcessing(monthTrackerDTO.getMonthClosureForProcessing());
						try {
							// save data in tds month tracker table.
							tdsMonthTrackerRepository.save(monthTracker);
							++processedRecordsCount;
						} catch (Exception e) {
							logger.error("Exception occurred while inserting data:", e);
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						TdsMonthTrackereErrorReportDTO problematicDataError = excelData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to process row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			} // for 1

			masterBatchUpload.setSuccessCount(Long.valueOf(processedRecordsCount));
			masterBatchUpload.setFailedCount(errorCount);
			masterBatchUpload.setProcessed(processedRecordsCount);
			masterBatchUpload.setDuplicateCount(0L);
			masterBatchUpload.setStatus("Processed");
			masterBatchUpload.setCreatedDate(Instant.now());
			masterBatchUpload.setProcessEndTime(new Date());
			masterBatchUpload.setCreatedBy(userName);

			if (!errorList.isEmpty()) {
				monthTrackerErrorFile = prepareMonthTrackerErrorFile(file.getOriginalFilename(), errorList,
						new ArrayList<>(excelData.getHeaders()));
			}

		} catch (Exception e) {
			masterBatchUpload.setStatus("Process failed");
			logger.error("Exception occurred :", e);

		}
		return masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear, assessmentMonth,
				userName, monthTrackerErrorFile, uploadType);
	}

	/**
	 * 
	 * @param originalFilename
	 * @param errorList
	 * @param arrayList
	 * @return
	 * @throws Exception
	 */
	private File prepareMonthTrackerErrorFile(String originalFilename,
			ArrayList<TdsMonthTrackereErrorReportDTO> errorList, ArrayList<String> hederNames) throws Exception {
		try {
			hederNames.addAll(0, MasterExcel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = monthTrackerXlsxReport(errorList, hederNames);
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
	 * @param hederNames
	 * @return
	 * @throws Exception
	 */
	private Workbook monthTrackerXlsxReport(ArrayList<TdsMonthTrackereErrorReportDTO> errorList,
			ArrayList<String> hederNames) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(hederNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForMonthTracker(errorList, worksheet, hederNames);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A6:B6");
		headerColorRange1.setStyle(style1);

		// Style for C6 to I6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("C6:I6");
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
		String lastHeaderCellName = "I6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:I6");
		worksheet.autoFitColumn(1);
		worksheet.autoFitRows();
		return workbook;
	}

	/**
	 * 
	 * @param errorList
	 * @param worksheet
	 * @param hederNames
	 * @throws Exception
	 */
	private void setExtractDataForMonthTracker(ArrayList<TdsMonthTrackereErrorReportDTO> errorList, Worksheet worksheet,
			ArrayList<String> hederNames) throws Exception {
		if (!errorList.isEmpty()) {
			int dataRowsStartIndex = 6;
			int serialNumber = 1;
			for (int i = 0; i < errorList.size(); i++) {
				TdsMonthTrackereErrorReportDTO errorDTO = errorList.get(i);
				ArrayList<String> rowData = MasterExcel.getValues(errorDTO, TdsMonthlyTrackerExcel.fieldMappings,
						hederNames);
				rowData.set(0, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(1, String.valueOf(serialNumber));
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				MasterExcel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, hederNames, errorDTO.getReason());
				serialNumber++;
			}
		}
	}

	/**
	 * 
	 * @param sha256Sum
	 * @return
	 */
	private boolean isAlreadyProcessed(String sha256Sum) {
		Optional<MasterBatchUpload> sha256Record = masterBatchUploadRepository.getSha256Records(sha256Sum);
		return sha256Record.isPresent();
	}
	
	public List<Map<String, Object>> getAllTdsMonthTrackerData() {
		return tdsMonthTrackerRepository.getAllTdsMonthTrackerData();
	}

}
