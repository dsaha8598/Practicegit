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
import com.ey.in.tds.common.domain.CessTypeMaster;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.errors.FieldValidator;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.dto.CessTypeMasterDTO;
import com.ey.in.tds.dto.CessTypeMasterErrorRepotDTO;
import com.ey.in.tds.dto.CessTypeMasterExcelDTO;
import com.ey.in.tds.repository.CessTypeMasterRepository;
import com.ey.in.tds.repository.MasterBatchUploadRepository;
import com.ey.in.tds.service.util.excel.CessTypeMasterExcel;
import com.ey.in.tds.service.util.excel.MasterExcel;
import com.ey.in.tds.web.rest.util.CommonValidations;
import com.microsoft.azure.storage.StorageException;

/**
 * Service Implementation for managing CessTypeMaster.
 */
/**
 * @author Admin
 *
 */
@Service
@Transactional
public class CessTypeMasterService {

	private final Logger logger = LoggerFactory.getLogger(CessTypeMasterService.class);

	private final CessTypeMasterRepository cessTypeMasterRepository;

	public CessTypeMasterService(CessTypeMasterRepository cessTypeMasterRepository) {
		this.cessTypeMasterRepository = cessTypeMasterRepository;
	}

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private MasterBatchUploadService masterBatchUploadService;

	@Autowired
	private MasterBatchUploadRepository masterBatchUploadRepository;

	/**
	 * This will create CESS Master Record.
	 * 
	 * @param cessTypeMasterDTO
	 * @param userName
	 * @return
	 * @throws FieldValidator
	 */
	public CessTypeMaster save(CessTypeMasterDTO cessTypeMasterDTO, String userName) {
		logger.info("REST request to save CessTypeMaster : {}", cessTypeMasterDTO);
		Optional<CessTypeMaster> cessTypeMasterDb = cessTypeMasterRepository
				.findByCessType(cessTypeMasterDTO.getCessType());
		if (cessTypeMasterDb.isPresent()) {
			CommonValidations.validateApplicableFields(cessTypeMasterDb.get().getApplicableTo(),
					cessTypeMasterDTO.getApplicableFrom());
		}
		CessTypeMaster cessTypeMaster = new CessTypeMaster();
		cessTypeMaster.setCessType(cessTypeMasterDTO.getCessType());
		cessTypeMaster.setApplicableFrom(cessTypeMasterDTO.getApplicableFrom());
		cessTypeMaster.setApplicableTo(cessTypeMasterDTO.getApplicableTo());
		cessTypeMaster.setActive(true);
		cessTypeMaster.setCreatedBy(userName);
		cessTypeMaster.setCreatedDate(Instant.now());
		cessTypeMaster.setModifiedBy(userName);
		cessTypeMaster.setModifiedDate(Instant.now());
		cessTypeMaster = cessTypeMasterRepository.save(cessTypeMaster);
		return cessTypeMaster;
	}

	/**
	 * This will update CESS Type Master.
	 * 
	 * @param cessTypeMasterDTO
	 * @param userName
	 * @return
	 * @throws RecordNotFoundException
	 */
	public CessTypeMaster update(CessTypeMasterDTO cessTypeMasterDTO, String userName) {
		logger.info("REST request to update CessTypeMaster : {}", cessTypeMasterDTO);
		Optional<CessTypeMaster> optionalCessTypeMaster = cessTypeMasterRepository.findById(cessTypeMasterDTO.getId());
		CessTypeMaster cessTypeMaster = new CessTypeMaster();

		if (optionalCessTypeMaster.isPresent()) {
			cessTypeMaster.setId(cessTypeMasterDTO.getId());
			cessTypeMaster.setApplicableFrom(cessTypeMasterDTO.getApplicableFrom());
			cessTypeMaster.setApplicableTo(cessTypeMasterDTO.getApplicableTo());
			cessTypeMaster.setCessType(cessTypeMasterDTO.getCessType());
			cessTypeMaster.setActive(true);
			cessTypeMaster.setModifiedBy(userName);
			cessTypeMaster.setModifiedDate(Instant.now());
			cessTypeMaster = cessTypeMasterRepository.save(cessTypeMaster);
		} else {
			throw new CustomException("No record with this id " + cessTypeMasterDTO.getId() + " to update",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return cessTypeMaster;
	}

	/**
	 * Get all the cessTypeMasters.
	 *
	 * @param pageable the pagination information
	 * @return the list of entities
	 */
	@Transactional(readOnly = true)
	public List<CessTypeMaster> findAll() {

		List<CessTypeMaster> listCessType = cessTypeMasterRepository.findAll();
		return listCessType;
	}

	/**
	 * Get one cessTypeMaster by id.
	 *
	 * @param id the id of the entity
	 * @return the entity
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public Optional<CessTypeMaster> findOne(Long id) throws RecordNotFoundException {

		Optional<CessTypeMaster> cessTypeMaster = null;
		logger.info("REST request id to get CessTypeMaster : {}", id);
		cessTypeMaster = cessTypeMasterRepository.findById(id);
		if (cessTypeMaster.isPresent()) {
			return cessTypeMaster;
		}
		return cessTypeMaster;
	}

	/**
	 * Delete the cessTypeMaster by id.
	 *
	 * @param id the id of the entity
	 */
	public void delete(Long id) {
		logger.info("REST request id to delete CessTypeMaster : {}", id);
		cessTypeMasterRepository.deleteById(id);
	}

	@Transactional
	public MasterBatchUpload saveFileData(MultipartFile file, Integer assesssmentYear, Integer assessmentMonth,
			String userName) throws IOException, InvalidKeyException, URISyntaxException, StorageException {

		String uploadType = UploadTypes.CESS_TYPE_MASTER_EXCEL.name();
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
			int headersCount = CessTypeMasterExcel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			if (headersCount != CessTypeMasterExcel.fieldMappings.size()) {
				masterBatchUpload.setCreatedDate(Instant.now());
				masterBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				masterBatchUpload.setSuccessCount(0L);
				masterBatchUpload.setFailedCount(0L);
				masterBatchUpload.setRowsCount(0L);
				masterBatchUpload.setProcessed(0);
				masterBatchUpload.setMismatchCount(0L);
				masterBatchUpload.setSha256sum(sha256);
				masterBatchUpload.setStatus("Failed");
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
			if (headersCount == CessTypeMasterExcel.fieldMappings.size()) {
				return saveCessTypeMasterData(workbook, file, sha256, assesssmentYear, assessmentMonth, userName,
						masterBatchUpload, uploadType);
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process cess type master data ", e);
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
	private MasterBatchUpload saveCessTypeMasterData(XSSFWorkbook workbook, MultipartFile file, String sha256,
			Integer assesssmentYear, Integer assessmentMonth, String userName, MasterBatchUpload masterBatchUpload,
			String uploadType) throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		File cessTypeErrorFile = null;
		ArrayList<CessTypeMasterErrorRepotDTO> errorList = new ArrayList<>();
		try {
			CessTypeMasterExcel excelData = new CessTypeMasterExcel(workbook);
			masterBatchUpload.setSha256sum(sha256);
			masterBatchUpload.setMismatchCount(0L);
			long dataRowsCount = excelData.getDataRowsCount();
			masterBatchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			Long duplicateCount = 0L;
			int processedRecordsCount = 0;
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<CessTypeMasterErrorRepotDTO> errorDTO = null;
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
						CessTypeMasterExcelDTO cessTypeMasterExcelDTO = excelData.get(rowIndex);

						Optional<CessTypeMaster> cessTypeMasterDb = cessTypeMasterRepository
								.findByCessType(cessTypeMasterExcelDTO.getCessType());
						if (cessTypeMasterDb.isPresent()) {
							CommonValidations.validateApplicableFields(cessTypeMasterDb.get().getApplicableTo(),
									cessTypeMasterExcelDTO.getApplicableFrom().toInstant());
						}
						CessTypeMaster cessTypeMaster = new CessTypeMaster();
						cessTypeMaster.setCessType(cessTypeMasterExcelDTO.getCessType().trim());
						cessTypeMaster.setApplicableFrom(cessTypeMasterExcelDTO.getApplicableFrom().toInstant());
						if (cessTypeMasterExcelDTO.getApplicableTo() != null) {
							cessTypeMaster.setApplicableTo(cessTypeMasterExcelDTO.getApplicableTo().toInstant());
						}
						cessTypeMaster.setActive(true);
						cessTypeMaster.setCreatedBy(userName);
						cessTypeMaster.setCreatedDate(Instant.now());
						cessTypeMaster.setModifiedBy(userName);
						cessTypeMaster.setModifiedDate(Instant.now());
						try {
							cessTypeMasterRepository.save(cessTypeMaster);
							++processedRecordsCount;
						} catch (Exception e) {
							logger.error("Exception occurred while inserting data:", e);
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						CessTypeMasterErrorRepotDTO problematicDataError = excelData.getErrorDTO(rowIndex);
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
				cessTypeErrorFile = prepareCessTypeErrorFile(file.getOriginalFilename(), errorList,
						new ArrayList<>(excelData.getHeaders()));
			}

		} catch (Exception e) {
			masterBatchUpload.setStatus("Process failed");
			logger.error("Exception occurred :", e);

		}
		return masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear, assessmentMonth,
				userName, cessTypeErrorFile, uploadType);
	}

	/**
	 * 
	 * @param originalFilename
	 * @param errorList
	 * @param arrayList
	 * @return
	 * @throws Exception
	 */
	private File prepareCessTypeErrorFile(String originalFilename, ArrayList<CessTypeMasterErrorRepotDTO> errorList,
			ArrayList<String> headerNames) throws Exception {
		try {
			headerNames.addAll(0, MasterExcel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = cessTypeXlsxReport(errorList, headerNames);
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
	 * @param headerNames
	 * @return
	 * @throws Exception
	 */
	private Workbook cessTypeXlsxReport(ArrayList<CessTypeMasterErrorRepotDTO> errorList, ArrayList<String> headerNames)
			throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForCessType(errorList, worksheet, headerNames);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A6:B6");
		headerColorRange1.setStyle(style1);

		// Style for C6 to E6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("C6:E6");
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
		String lastHeaderCellName = "E6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:E6");
		worksheet.autoFitColumn(1);
		worksheet.autoFitRows();
		return workbook;
	}

	/**
	 * 
	 * @param errorList
	 * @param worksheet
	 * @param headerNames
	 * @throws Exception
	 */
	private void setExtractDataForCessType(ArrayList<CessTypeMasterErrorRepotDTO> errorList, Worksheet worksheet,
			ArrayList<String> headerNames) throws Exception {
		if (!errorList.isEmpty()) {
			int dataRowsStartIndex = 6;
			int serialNumber = 1;
			for (int i = 0; i < errorList.size(); i++) {
				CessTypeMasterErrorRepotDTO errorDTO = errorList.get(i);
				ArrayList<String> rowData = MasterExcel.getValues(errorDTO, CessTypeMasterExcel.fieldMappings,
						headerNames);
				rowData.set(0, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(1, String.valueOf(serialNumber));
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				MasterExcel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
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
}
