package com.ey.in.tds.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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
import com.ey.in.tds.common.domain.FineRateMaster;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.core.exceptions.BadRequestAlertException;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.dto.FineRateMasterDTO;
import com.ey.in.tds.dto.FineRateMasterErrorReportDTO;
import com.ey.in.tds.repository.FineRateMasterRepository;
import com.ey.in.tds.repository.MasterBatchUploadRepository;
import com.ey.in.tds.service.util.excel.MasterExcel;
import com.ey.in.tds.service.util.excel.TdsFineRateMasterExcel;
import com.ey.in.tds.web.rest.util.CommonValidations;
import com.microsoft.azure.storage.StorageException;

@Service
public class FineRateMasterService {
	private final Logger logger = LoggerFactory.getLogger(FineRateMasterService.class);

	private final FineRateMasterRepository fineRateMasterRepository;

	public FineRateMasterService(FineRateMasterRepository fineRateMasterRepository) {
		this.fineRateMasterRepository = fineRateMasterRepository;
	}

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private MasterBatchUploadService masterBatchUploadService;

	@Autowired
	private MasterBatchUploadRepository masterBatchUploadRepository;

	/**
	 * This method is used to create new Fine Rate Master record
	 * 
	 * @param userName
	 * 
	 * @param fineRateMasterfromui
	 * @return
	 * @throws BadRequestAlertException
	 */
	public FineRateMaster save(FineRateMaster fineRateMaster, String userName) {
		logger.info("REST request to save a FineRateMaster : {} ", fineRateMaster);
		Optional<FineRateMaster> fineRateMasterDb = fineRateMasterRepository.findByInterestTypeAndInterestCalculation(
				fineRateMaster.getInterestType(), fineRateMaster.getTypeOfIntrestCalculation());
		if (fineRateMasterDb.isPresent()) {
			CommonValidations.validateApplicableFields(fineRateMasterDb.get().getApplicableTo(),
					fineRateMaster.getApplicableFrom());
		}
		FineRateMaster fineRateMasters = new FineRateMaster();
		fineRateMasters.setApplicableFrom(fineRateMaster.getApplicableFrom());
		fineRateMasters.setApplicableTo(fineRateMaster.getApplicableTo());
		fineRateMasters.setTypeOfIntrestCalculation(fineRateMaster.getTypeOfIntrestCalculation());

		if (("Late Filing").equalsIgnoreCase(fineRateMaster.getInterestType())
				&& (fineRateMaster.getFinePerDay() == null || fineRateMaster.getFinePerDay() == 0)) {
			throw new CustomException("If Late Filing Selected, Fine Per Day should not be empty",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (!("Late Filing").equalsIgnoreCase(fineRateMaster.getInterestType())
				&& (fineRateMaster.getRate() == null || fineRateMaster.getRate().compareTo(BigDecimal.ZERO) == 0)) {
			throw new CustomException("If Late Filing Selected, Rate should not be empty",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		fineRateMasters.setRate(fineRateMaster.getRate());
		fineRateMasters.setFinePerDay(fineRateMaster.getFinePerDay());
		fineRateMasters.setInterestType(fineRateMaster.getInterestType());
		fineRateMasters.setActive(true);
		fineRateMasters.setCreatedBy(userName);
		fineRateMasters.setCreatedDate(Instant.now());
		fineRateMasters.setModifiedBy(userName);
		fineRateMasters.setModifiedDate(Instant.now());
		fineRateMasters = fineRateMasterRepository.save(fineRateMasters);
		return fineRateMasters;
	}

	/**
	 * This method is used to get all Fine Rate Master record
	 * 
	 * @param pageable
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public List<FineRateMaster> findAll() {
		List<FineRateMaster> fineRateList = fineRateMasterRepository.findAll();

		return fineRateList;
	}

	/**
	 * This method is used to get Fine Rate Master record based on id
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public FineRateMaster findOne(Long id) {
		logger.info("REST request id to get a record of FineRateMaster : {}", id);
		FineRateMaster fineRateMasters = null;
		Optional<FineRateMaster> fineRateMaster = fineRateMasterRepository.findById(id);
		if (fineRateMaster.isPresent()) {
			fineRateMasters = fineRateMaster.get();
		}
		return fineRateMasters;
	}

	/**
	 * This method is used to get Fine Rate Master record based on Late Filing
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public FineRateMaster findRecordBasedonLateFiling(String filing) {
		logger.info("REST request id to get a record of FineRateMaster : {}", filing);
		FineRateMaster fineRateMasters = null;
		Optional<FineRateMaster> fineRateMaster = fineRateMasterRepository.findByInterestType(filing);
		if (fineRateMaster.isPresent()) {
			fineRateMasters = fineRateMaster.get();
		}

//		List<FineRateMaster> listOfFines = fineRateMasterRepository.findByIntrestTypeOfFine(filing);
//		if(listOfFines.size()>0) {
//			fineRateMasters = listOfFines.get(0);
//		} else {
//			fineRateMasters = new FineRateMaster();
//		}

		return fineRateMasters;
	}

	/**
	 * This method is used to delete Fine Rate Master record based on id
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 */
	public String delete(Long id) {
		logger.info("REST request id to delete a FineRateMaster record : {}", id);
		Optional<FineRateMaster> fineRateMasters = fineRateMasterRepository.findById(id);
		if (fineRateMasters.isPresent()) {
			fineRateMasterRepository.deleteById(id);
		} else {
			throw new CustomException("No record found  with id " + id, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return "The deletion request has been succeded";
	}

	/**
	 * This method is used to update Fine Rate Master record
	 * 
	 * @param userName
	 * 
	 * @param fineRateMasterfromui
	 * @return
	 * @throws RecordNotFoundException
	 */
	public FineRateMaster updateFineRateMaster(@Valid FineRateMaster fineRateMaster, String userName)
			throws RecordNotFoundException {
		logger.info("REST request to update a FineRateMaster : {} ", fineRateMaster);
		FineRateMaster response = null;
		Optional<FineRateMaster> fineRateMasters = fineRateMasterRepository.findById(fineRateMaster.getId());
		if (fineRateMasters.isPresent()) {
			FineRateMaster fineRateMaste = fineRateMasters.get();
			fineRateMaste.setApplicableFrom(fineRateMaster.getApplicableFrom());
			fineRateMaste.setApplicableTo(fineRateMaster.getApplicableTo());
			fineRateMaste.setId(fineRateMaster.getId());
			fineRateMaste.setTypeOfIntrestCalculation(fineRateMaster.getTypeOfIntrestCalculation());
			fineRateMaste.setModifiedBy(userName);
			fineRateMaste.setModifiedDate(Instant.now());
			if (fineRateMaste.getInterestType().equalsIgnoreCase("Late filing")) {
				fineRateMaste.setRate(fineRateMaster.getRate());
			} else {
				fineRateMaste.setFinePerDay(fineRateMaster.getFinePerDay());
			}
			fineRateMaste.setInterestType(fineRateMaster.getInterestType());
			response = fineRateMasterRepository.save(fineRateMaste);
		} else {
			throw new CustomException("No record found  with id " + fineRateMaster.getId(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
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

		String uploadType = UploadTypes.TDS_FINE_RATE_MASTER_EXCEL.name();
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
			int headersCount = TdsFineRateMasterExcel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			if (headersCount != TdsFineRateMasterExcel.fieldMappings.size()) {
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
			if (headersCount == TdsFineRateMasterExcel.fieldMappings.size()) {
				return saveTdsRateMasterData(workbook, file, sha256, assesssmentYear, assessmentMonth, userName,
						masterBatchUpload, uploadType);
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process fine rate master data ", e);
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
	private MasterBatchUpload saveTdsRateMasterData(XSSFWorkbook workbook, MultipartFile file, String sha256,
			Integer assesssmentYear, Integer assessmentMonth, String userName, MasterBatchUpload masterBatchUpload,
			String uploadType) throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		File rateMasterErrorFile = null;
		ArrayList<FineRateMasterErrorReportDTO> errorList = new ArrayList<>();
		try {
			TdsFineRateMasterExcel excelData = new TdsFineRateMasterExcel(workbook);
			masterBatchUpload.setSha256sum(sha256);
			masterBatchUpload.setMismatchCount(0L);
			long dataRowsCount = excelData.getDataRowsCount();
			masterBatchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			Long duplicateCount = 0L;
			int processedRecordsCount = 0;
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<FineRateMasterErrorReportDTO> errorDTO = null;
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
						FineRateMasterDTO fineRateMasterDTO = excelData.get(rowIndex);

						Optional<FineRateMaster> fineRateMasterDb = fineRateMasterRepository
								.findByInterestTypeAndInterestCalculation(fineRateMasterDTO.getInterestType(),
										fineRateMasterDTO.getTypeOfIntrestCalculation());
						if (fineRateMasterDb.isPresent()) {
							CommonValidations.validateApplicableFields(fineRateMasterDb.get().getApplicableTo(),
									fineRateMasterDTO.getApplicableFrom().toInstant());
						}
						FineRateMaster fineRateMasters = new FineRateMaster();
						fineRateMasters.setApplicableFrom(fineRateMasterDTO.getApplicableFrom().toInstant());
						if (fineRateMasterDTO.getApplicableTo() != null) {
							fineRateMasters.setApplicableTo(fineRateMasterDTO.getApplicableTo().toInstant());
						}
						fineRateMasters
								.setTypeOfIntrestCalculation(fineRateMasterDTO.getTypeOfIntrestCalculation().trim());
						if (("Late Filing").equalsIgnoreCase(fineRateMasterDTO.getInterestType())
								&& (fineRateMasterDTO.getFinePerDay() == null
										|| fineRateMasterDTO.getFinePerDay() == 0)) {
							throw new CustomException("If late filing selected, Fine Per Day should not be empty",
									HttpStatus.INTERNAL_SERVER_ERROR);
						}
						if (!("Late Filing").equalsIgnoreCase(fineRateMasterDTO.getInterestType())
								&& (fineRateMasterDTO.getRate() == null
										|| fineRateMasterDTO.getRate().compareTo(BigDecimal.ZERO) == 0)) {
							throw new CustomException("If late filing selected, Rate should not be empty",
									HttpStatus.INTERNAL_SERVER_ERROR);
						}
						fineRateMasters.setRate(fineRateMasterDTO.getRate());
						fineRateMasters.setFinePerDay(fineRateMasterDTO.getFinePerDay());
						fineRateMasters.setInterestType(fineRateMasterDTO.getInterestType().trim());
						fineRateMasters.setActive(true);
						fineRateMasters.setCreatedBy(userName);
						fineRateMasters.setCreatedDate(Instant.now());
						fineRateMasters.setModifiedBy(userName);
						fineRateMasters.setModifiedDate(Instant.now());
						try {
							fineRateMasterRepository.save(fineRateMasters);
							++processedRecordsCount;
						} catch (Exception e) {
							logger.error("Exception occurred while inserting data:", e);
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						FineRateMasterErrorReportDTO problematicDataError = excelData.getErrorDTO(rowIndex);
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
				rateMasterErrorFile = preparFineRateMasterErrorFile(file.getOriginalFilename(), errorList,
						new ArrayList<>(excelData.getHeaders()));
			}

		} catch (Exception e) {
			masterBatchUpload.setStatus("Process failed");
			logger.error("Exception occurred :", e);

		}
		return masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear, assessmentMonth,
				userName, rateMasterErrorFile, uploadType);
	}

	/**
	 * 
	 * @param originalFilename
	 * @param errorList
	 * @param arrayList
	 * @return
	 * @throws Exception
	 */
	private File preparFineRateMasterErrorFile(String originalFilename,
			ArrayList<FineRateMasterErrorReportDTO> errorList, ArrayList<String> headersName) throws Exception {
		try {
			headersName.addAll(0, MasterExcel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = fineRateMasterXlsxReport(errorList, headersName);
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
	 * @param headersName
	 * @return
	 * @throws Exception
	 */
	private Workbook fineRateMasterXlsxReport(ArrayList<FineRateMasterErrorReportDTO> errorList,
			ArrayList<String> headersName) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headersName, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForFineRateMaster(errorList, worksheet, headersName);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A6:B6");
		headerColorRange1.setStyle(style1);

		// Style for C6 to H6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("C6:H6");
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
		String lastHeaderCellName = "H6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:H6");
		worksheet.autoFitColumn(1);
		worksheet.autoFitRows();
		return workbook;
	}

	/**
	 * 
	 * @param errorList
	 * @param worksheet
	 * @param headersName
	 * @throws Exception
	 */
	private void setExtractDataForFineRateMaster(ArrayList<FineRateMasterErrorReportDTO> errorList, Worksheet worksheet,
			ArrayList<String> headersName) throws Exception {
		if (!errorList.isEmpty()) {
			int dataRowsStartIndex = 6;
			int serialNumber = 1;
			for (int i = 0; i < errorList.size(); i++) {
				FineRateMasterErrorReportDTO errorDTO = errorList.get(i);
				ArrayList<String> rowData = MasterExcel.getValues(errorDTO, TdsFineRateMasterExcel.fieldMappings,
						headersName);
				rowData.set(0, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(1, String.valueOf(serialNumber));
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				MasterExcel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headersName, errorDTO.getReason());
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
