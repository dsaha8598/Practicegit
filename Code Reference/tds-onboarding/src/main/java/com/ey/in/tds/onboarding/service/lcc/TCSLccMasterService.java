package com.ey.in.tds.onboarding.service.lcc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
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

import com.aspose.cells.BackgroundType;
import com.aspose.cells.BorderType;
import com.aspose.cells.CellBorderType;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSLccMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSLccUtilizationDAO;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.model.ldc.LccErrorReportCsvDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.LccUtilizationDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcCountDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccUtilization;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.CommonValidationsCassandra;
import com.ey.in.tds.common.util.ValueProcessor;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.util.ActivityTrackerStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.jdbc.dao.CollecteeMasterDAO;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.onboarding.dto.ao.AoLdcMasterDTO;
import com.ey.in.tds.onboarding.service.ldc.ErrorFileLdcUpload;
import com.ey.in.tds.onboarding.service.util.excel.ldc.LccExcel;
import com.microsoft.azure.storage.StorageException;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

/**
 * 
 * @author vamsir
 *
 */
@Service
public class TCSLccMasterService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BlobStorage blob;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private ErrorFileLdcUpload errorFileLdcUpload;

	@Autowired
	private TCSLccMasterDAO tcsLccMasterDao;

	@Autowired
	private TCSBatchUploadDAO tcsBatchUploadDAO;

	@Autowired
	private TCSLccUtilizationDAO tcsLccUtilizationDAO;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private CollecteeMasterDAO collecteeMasterDAO;

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	/**
	 * 
	 * @param lccMasterDTO
	 * @param deductorTan
	 * @param assessmentYear
	 * @param userName
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public TCSLccMaster create(TCSLccMaster lccMasterDTO, String deductorTan, Integer assessmentYear, String userName)
			throws IllegalAccessException, InvocationTargetException {
		TCSLccMaster lccMaster = new TCSLccMaster();
		List<TCSLccMaster> listLdcMsater = tcsLccMasterDao.getLccBycertificateNoPanTanSection(
				lccMasterDTO.getCertificateNumber(), lccMasterDTO.getLccMasterPan(),
				lccMasterDTO.getSectionAsPerTraces(), deductorTan);
		if (!listLdcMsater.isEmpty()) {
			throw new CustomException(
					"Already record present with Certificate Number, section and Pan Combination, Cannot add duplicate Record",
					HttpStatus.INTERNAL_SERVER_ERROR);
		} else {
			lccMaster.setAssessmentYear(assessmentYear);
			lccMaster.setLccMasterPan(lccMasterDTO.getLccMasterPan());
			lccMaster.setCollectorMasterTan(deductorTan);
			List<TCSLccMaster> ldcMasterDb = tcsLccMasterDao.getLccBycertificateNoAndDeducteePan(
					lccMasterDTO.getCertificateNumber(), lccMasterDTO.getLccMasterPan());
			for (TCSLccMaster ldc : ldcMasterDb) {
				CommonValidationsCassandra.validateApplicableFields(ldc.getApplicableTo(),
						lccMasterDTO.getApplicableFrom());
			}
			lccMaster.setCertificateNumber(lccMasterDTO.getCertificateNumber());
			lccMaster.setCollecteeName(lccMasterDTO.getCollecteeName());
			lccMaster.setAmount(lccMasterDTO.getAmount());
			lccMaster.setInitialUtilizedAmount(lccMasterDTO.getUtilizedAmount());
			// remaining amount 
			if (lccMasterDTO.getInitialUtilizedAmount() != null) {
				lccMaster
						.setRemainingAmount(lccMasterDTO.getAmount().subtract(lccMasterDTO.getUtilizedAmount()));
			}
			lccMaster.setSectionAsPerTraces(lccMasterDTO.getSectionAsPerTraces());
			lccMaster.setLccSection(lccMasterDTO.getSectionAsPerTraces());
			lccMaster.setApplicableFrom(lccMasterDTO.getApplicableFrom());
			lccMaster.setLccApplicableFrom(lccMasterDTO.getApplicableFrom());
			lccMaster.setApplicableTo(lccMasterDTO.getApplicableTo());
			lccMaster.setLccApplicableTo(lccMasterDTO.getApplicableTo());
			lccMaster.setRateAsPerTraces(lccMasterDTO.getRateAsPerTraces().setScale(4, BigDecimal.ROUND_HALF_DOWN));
			lccMaster.setLccRate(lccMasterDTO.getRateAsPerTraces().setScale(4, BigDecimal.ROUND_HALF_DOWN));
			if (lccMasterDTO.getUtilizedAmount() != null) {
				lccMaster.setUtilizedAmount(lccMasterDTO.getUtilizedAmount());
			}
			lccMaster.setActive(true);
			lccMaster.setCreatedBy(userName);
			lccMaster.setCreatedDate(new Timestamp(new Date().getTime()));
			lccMaster.setModifiedBy(userName);
			lccMaster.setModifiedDate(new Timestamp(new Date().getTime()));
			lccMaster.setValidationStatus(false);
			lccMaster = tcsLccMasterDao.save(lccMaster);
			return lccMaster;
		}
	}

	/**
	 * 
	 * @param lccMasterDTO
	 * @param deductorTan
	 * @param assessmentYear
	 * @param userName
	 * @return
	 */
	public TCSLccMaster update(TCSLccMaster lccMasterDTO, String deductorTan, Integer assessmentYear, String userName) {
		List<TCSLccMaster> listLcc = tcsLccMasterDao.getLccById(lccMasterDTO.getId(), deductorTan);
		TCSLccMaster lccMaster = new TCSLccMaster();
		if (!listLcc.isEmpty()) {
			lccMaster = listLcc.get(0);
			lccMaster.setActive(true);
			lccMaster.setAmount(lccMasterDTO.getAmount());
			lccMaster.setApplicableFrom(lccMasterDTO.getApplicableFrom());
			lccMaster.setApplicableTo(lccMasterDTO.getApplicableTo());
			lccMaster.setCertificateNumber(lccMasterDTO.getCertificateNumber());
			lccMaster.setModifiedBy(userName);
			lccMaster.setModifiedDate(new Timestamp(new Date().getTime()));
			lccMaster.setLccApplicableFrom(lccMasterDTO.getApplicableFrom());
			lccMaster.setLccApplicableTo(lccMasterDTO.getApplicableTo());
			tcsLccMasterDao.update(lccMaster);
		}
		return lccMaster;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public List<TCSLccMaster> getListOfLcc(String deductorTan, String tenantId)
			throws IllegalAccessException, InvocationTargetException {

		List<TCSLccMaster> listLdc = new ArrayList<>();
		List<TCSLccMaster> lccList = tcsLccMasterDao.getLccByTan(deductorTan, tenantId);
		logger.info("size of lcc list---{}", lccList.size());
		for (TCSLccMaster lccMaster : lccList) {
			TCSLccMaster lccMasterDTO = new TCSLccMaster();
			BeanUtils.copyProperties(lccMasterDTO, lccMaster);
			Double ldcUtilizationAmount = tcsLccMasterDao.getTotalLccUtilizationAmount(lccMaster.getId(), deductorTan);
			double initialUtilisedAmount = 0;
			if (lccMaster.getUtilizedAmount() != null) {
				initialUtilisedAmount = lccMaster.getUtilizedAmount().doubleValue();
			}
			lccMasterDTO
					.setUtilizedAmount(BigDecimal.valueOf(ldcUtilizationAmount.doubleValue() + initialUtilisedAmount));
			listLdc.add(lccMasterDTO);
		}

		return listLdc;
	}

	/**
	 * 
	 * @param deductorTan
	 * @param ldcId
	 * @param assessmentYear
	 * @return
	 */
	public TCSLccMaster getLccMasterDTO(String deductorTan, Integer lccId, Integer assessmentYear) {
		List<TCSLccMaster> response = tcsLccMasterDao.getLccById(lccId, deductorTan);
		TCSLccMaster lcc = new TCSLccMaster();
		if (!response.isEmpty()) {
			return lcc = response.get(0);
		} else {
			return lcc;
		}
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public ByteArrayInputStream exportToExcel() throws IOException {
		String tenantId = MultiTenantContext.getTenantId();
		logger.info("tenantId :{}", tenantId);
		String[] COLUMNS = { "Certification Number", "Collectee Name", "Amount", "Limit Utilised", "Rate",
				"Assessment Year" };
		try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {

			XSSFSheet sheet = workbook.createSheet("Lcc_Master");
			sheet.setDefaultColumnWidth(14);
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setColor(IndexedColors.BLUE.getIndex());
			XSSFCellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			// Row for Header
			XSSFRow headerRow = sheet.createRow(0);
			// Header
			for (int col = 0; col < COLUMNS.length; col++) {
				Cell cell = headerRow.createCell(col);
				cell.setCellValue(COLUMNS[col]);
				cell.setCellStyle(headerCellStyle);
			}
			List<TCSLccMaster> listTcsLcc = tcsLccMasterDao.findAll();

			XSSFCellStyle cellStyle = workbook.createCellStyle();
			int rowIdx = 1;
			for (TCSLccMaster ldc : listTcsLcc) {
				XSSFRow row = sheet.createRow(rowIdx++);
				row.createCell(0).setCellValue(ldc.getCertificateNumber());
				row.createCell(1).setCellValue(ldc.getCollecteeName());
				if (ldc.getAmount() != null) {
					row.createCell(2).setCellValue(ldc.getAmount().doubleValue());
				}
				if (ldc.getUtilizedAmount() != null) {
					row.createCell(3).setCellValue(ldc.getUtilizedAmount().doubleValue());
				}
				if (ldc.getLccRate() != null) {
					row.createCell(4).setCellValue(ldc.getRateAsPerTraces().doubleValue());
				}
				row.createCell(5).setCellValue(ldc.getAssessmentYear());
				Cell cell = row.createCell(6);
				cell.setCellStyle(cellStyle);
			}
			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		}
	}

	/**
	 * 
	 * @param file
	 * @param tan
	 * @param assessmentYear
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public TCSBatchUpload saveToBatchUploadExcel(MultipartFile file, String tan, Integer assessmentYear,
			String userName) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		String path = blob.uploadExcelToBlob(file);

		TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Please select the excel file only");
		} else {
			tcsBatchUpload.setFileName(file.getOriginalFilename());
			tcsBatchUpload.setFilePath(path);
			String sha256 = sha256SumService.getSHA256Hash(file);
			List<TCSBatchUpload> batch = tcsBatchUploadDAO.getSha256Records(sha256);
			if (batch.isEmpty()) {
				tcsBatchUpload.setAssessmentYear(assessmentYear);
				tcsBatchUpload.setCollectorMasterTan(tan);
				tcsBatchUpload.setUploadType(UploadTypes.LDC_EXCEL.name());
				tcsBatchUpload.setStatus("Uploaded");
			} else {
				tcsBatchUpload.setAssessmentYear(assessmentYear);
				tcsBatchUpload.setCollectorMasterTan(tan);
				tcsBatchUpload.setUploadType(UploadTypes.LDC_EXCEL.name());
				tcsBatchUpload.setStatus("Duplicate");
				tcsBatchUpload.setReferenceId(batch.get(0).getId());
			}

			tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setActive(true);
			tcsBatchUpload.setCreatedBy(userName);
			tcsBatchUpload.setModifiedBy(userName);
			tcsBatchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setSuccessCount(0L);
			tcsBatchUpload.setFailedCount(0L);
			tcsBatchUpload.setRowsCount(0L);
			tcsBatchUpload.setMismatchCount(0L);
			tcsBatchUpload.setDuplicateCount(0L);
			tcsBatchUpload = tcsBatchUploadDAO.save(tcsBatchUpload);
			saveExcelData(file, assessmentYear);

			return tcsBatchUpload;
		}
	}

	/**
	 * 
	 * @param file
	 * @param assessmentYear
	 * @throws IOException
	 */
	public void saveExcelData(MultipartFile file, Integer assessmentYear) throws IOException {

		TCSLccMaster lccMaster = new TCSLccMaster();
		List<AoLdcMasterDTO> validationList = new ArrayList<>();
		try {
			int in = 1;
			XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
			XSSFSheet worksheet = workbook.getSheetAt(0);
			while (in <= worksheet.getLastRowNum()) {
				AoLdcMasterDTO aoLdcValidationDTO = new AoLdcMasterDTO();
				XSSFRow row = worksheet.getRow(in++);
				logger.info("Row num : " + row.getRowNum());
				if (row.getRowNum() == 1) {
					aoLdcValidationDTO.setAssessmentYear(row.getCell(0).getStringCellValue());
					aoLdcValidationDTO.setCertificateNumber(row.getCell(1).getStringCellValue());
					aoLdcValidationDTO.setDeductorName(row.getCell(2).getStringCellValue());
					aoLdcValidationDTO.setDeductorTAN(row.getCell(3).getStringCellValue());
					aoLdcValidationDTO.setDeducteeName(row.getCell(4).getStringCellValue());
					aoLdcValidationDTO.setDeducteePAN(row.getCell(5).getStringCellValue());
					aoLdcValidationDTO.setNatureOfPayment(row.getCell(6).getStringCellValue());
					aoLdcValidationDTO.setSection(row.getCell(7).getStringCellValue());
					aoLdcValidationDTO.setAmount(BigDecimal.valueOf(row.getCell(8).getNumericCellValue()));
					aoLdcValidationDTO.setRate(BigDecimal.valueOf(row.getCell(9).getNumericCellValue()));
					aoLdcValidationDTO
							.setLimitUtilisedAmount(BigDecimal.valueOf(row.getCell(10).getNumericCellValue()));

					validationList.add(aoLdcValidationDTO);
				}
			}
			workbook.close();
		} catch (Exception e) {
			logger.error("Error occured at saveExceldata", e);
		}
		for (AoLdcMasterDTO ldcMasterData : validationList) {
			lccMaster.setAssessmentYear(assessmentYear);
			lccMaster.setLccMasterPan(ldcMasterData.getDeducteePAN());
			lccMaster.setCertificateNumber(ldcMasterData.getCertificateNumber());
			lccMaster.setAmount(ldcMasterData.getAmount());
			lccMaster.setNatureOfIncome(ldcMasterData.getNatureOfPayment());
			lccMaster.setSectionAsPerTraces(ldcMasterData.getSection());
			lccMaster.setRateAsPerTraces(ldcMasterData.getRate());
			lccMaster.setUtilizedAmount(ldcMasterData.getLimitUtilisedAmount());
			try {
				tcsLccMasterDao.save(lccMaster);
			} catch (Exception e) {
				logger.error("Error occured at saveExcelData", e);
			}
		}
	}

	/**
	 * 
	 * @param assesssmentYear
	 * @param deductorTan
	 * @return
	 */
	public TCSLccMaster getOneLdcMaste(Integer assesssmentYear, String deductorTan) {
		List<TCSLccMaster> lccMasterObj = tcsLccMasterDao.getLccByYearAndTan(assesssmentYear, deductorTan);
		if (!lccMasterObj.isEmpty()) {
			return lccMasterObj.get(0);
		}
		return new TCSLccMaster();
	}

	/**
	 * 
	 * @param deductorTan
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public String getLdcMasterStatus(String deductorTan, String startDate, String endDate) {
		long countValidStatus = 0l;
		long countInValidStatus = 0l;
		long countEmptyStatus = 0l;
		LdcCountDTO dto = null;
		dto = tcsLccMasterDao.getLccStatusCount(deductorTan, startDate, endDate);
		if (dto != null) {
			countValidStatus = dto.getValidStatusCount();
			countInValidStatus = dto.getInvalidStatusCount();
			countEmptyStatus = dto.getEmptyStatusCount();
		}
		logger.info("Total lcc master valid status:" + countValidStatus + ", Invalid status: " + countInValidStatus
				+ ", empty status:" + countEmptyStatus);

		if (countValidStatus == 0 && countInValidStatus == 0 && countEmptyStatus == 0) {
			return ActivityTrackerStatus.NORECORDS.name();
		} else if (countInValidStatus > 0 || countEmptyStatus > 0) {
			return ActivityTrackerStatus.PENDING.name();
		} else if (countValidStatus > 0 && countInValidStatus == 0 && countEmptyStatus == 0) {
			return ActivityTrackerStatus.VALIDATED.name();
		} else {
			return ActivityTrackerStatus.NORECORDS.name();
		}
	}

	/**
	 * 
	 * @param sha256Sum
	 * @return
	 */
	private boolean isAlreadyProcessed(String sha256Sum) {

		List<TCSBatchUpload> sha256Record = tcsBatchUploadDAO.getSha256Records(sha256Sum);

		return sha256Record != null && !sha256Record.isEmpty();
	}

	/**
	 * checking the uniqueness of the file and processing it
	 * 
	 * @param multiPartFile
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public TCSBatchUpload saveFileData(MultipartFile multiPartFile, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonthPlusOne, String userName, String tenantId, String deductorPan) throws Exception {

		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);
		if (isAlreadyProcessed(sha256)) { //
			TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
			tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setCreatedBy(userName);
			tcsBatchUpload.setSuccessCount(0L);
			tcsBatchUpload.setFailedCount(0L);
			tcsBatchUpload.setRowsCount(0L);
			tcsBatchUpload.setProcessed(0);
			tcsBatchUpload.setMismatchCount(0L);
			tcsBatchUpload.setSha256sum(sha256);
			tcsBatchUpload.setStatus("Duplicate");
			tcsBatchUpload.setNewStatus("Duplicate");
			tcsBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			tcsBatchUpload = lccBatchUpload(tcsBatchUpload, multiPartFile, deductorTan, assesssmentYear,
					assessmentMonthPlusOne, userName, null, tenantId);
			return tcsBatchUpload;
		}

		// checking if file is not duplicate
		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {

			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = Excel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count : ", headersCount);
			if (headersCount != LccExcel.fieldMappings.size()) {
				TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
				tcsBatchUpload.setProcessStartTime(new Date());
				tcsBatchUpload.setSuccessCount(0L);
				tcsBatchUpload.setFailedCount(0L);
				tcsBatchUpload.setRowsCount(0L);
				tcsBatchUpload.setProcessed(0);
				tcsBatchUpload.setMismatchCount(0L);
				tcsBatchUpload.setSha256sum(sha256);
				tcsBatchUpload.setStatus("Failed");
				tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				tcsBatchUpload.setCreatedBy(userName);
				tcsBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return lccBatchUpload(tcsBatchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonthPlusOne, userName, null, tenantId);
			} else {
				return processLcc(workbook, multiPartFile, sha256, deductorTan, assesssmentYear, assessmentMonthPlusOne,
						userName, tenantId, deductorPan);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process ldc master data ", e);
		}
	}

	/**
	 * Thisbatch_upload method is to insert the record into the batch_upload table
	 * 
	 * @param tcsBatchUpload
	 * @param mFile
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param file
	 * @param tenant
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public TCSBatchUpload lccBatchUpload(TCSBatchUpload tcsBatchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {

		logger.info("batch", tcsBatchUpload);
		if (file != null) {
			String errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
			tcsBatchUpload.setErrorFilePath(errorFp);
		}
		String path = blob.uploadExcelToBlob(mFile);
		int month = assessmentMonthPlusOne;
		tcsBatchUpload.setAssessmentMonth(month);
		if (FilenameUtils.getExtension(mFile.getOriginalFilename()).equalsIgnoreCase("pdf")) {
			tcsBatchUpload.setAssessmentYear(assesssmentYear);
			tcsBatchUpload.setUploadType(UploadTypes.LCC_PDF.name());
			tcsBatchUpload.setCollectorMasterTan(tan);
		} else {
			tcsBatchUpload.setAssessmentYear(assesssmentYear);
			tcsBatchUpload.setUploadType(UploadTypes.LCC_EXCEL.name());
			tcsBatchUpload.setCollectorMasterTan(tan);
		}
		tcsBatchUpload.setFileName(mFile.getOriginalFilename());
		tcsBatchUpload.setFilePath(path);
		tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
		tcsBatchUpload.setCreatedBy(userName);
		tcsBatchUpload.setActive(true);
		try {
			tcsBatchUpload = tcsBatchUploadDAO.save(tcsBatchUpload);
		} catch (Exception e) {
			logger.error("error while saving  file } " + e);
		}
		return tcsBatchUpload;
	}

	/**
	 * This mewthod is to process get the records from the LdcExcell File an inserts
	 * record into ldc_master table
	 * 
	 * @param workbook
	 * @param uploadedFile
	 * @param sha256
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @return ldcBatchUpload(batchUpload, uploadedFile, deductorTan,
	 *         assesssmentYear, assessmentMonthPlusOne, userName, ldcErrorFile,
	 *         tenantId);
	 * @throws Exception
	 */
	@Async
	private TCSBatchUpload processLcc(XSSFWorkbook workbook, MultipartFile uploadedFile, String sha256,
			String deductorTan, Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName,
			String tenantId, String deductorPan) throws Exception {
		TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
		tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
		tcsBatchUpload.setSuccessCount(0L);
		tcsBatchUpload.setFailedCount(0L);
		tcsBatchUpload.setRowsCount(0L);
		tcsBatchUpload.setProcessed(0);
		tcsBatchUpload.setMismatchCount(0L);
		ArrayList<LccErrorReportCsvDTO> errorList = new ArrayList<>();

		File lccErrorFile = null;
		try {
			LccExcel data = new LccExcel(workbook);

			tcsBatchUpload.setSha256sum(sha256);
			tcsBatchUpload.setMismatchCount(0L);
			long dataRowsCount = data.getDataRowsCount();
			tcsBatchUpload.setRowsCount(dataRowsCount);

			int errorCount = 0;
			int successCount = 0;
			int duplicateCount = 0;
			boolean isDuplicate = false;
			List<TCSLccMaster> ldcList = new ArrayList<>(); // to be checked
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<LccErrorReportCsvDTO> errorDTO = null;

				try {
					errorDTO = data.validate(rowIndex);
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
						TCSLccMaster lcc = data.get(rowIndex);
						lcc.setActive(true);
						lcc.setAssessmentYear(assesssmentYear);
						lcc.setCreatedBy(userName);
						lcc.setCreatedDate(new Timestamp(new Date().getTime()));
						lcc.setInitialUtilizedAmount(lcc.getUtilizedAmount());
						lcc.setAmount(lcc.getAmount());
						// remaining amount
						if(lcc.getUtilizedAmount() != null) {
							lcc.setRemainingAmount(lcc.getAmount().subtract(lcc.getUtilizedAmount()));
						}
						// utilized amount
						if(lcc.getUtilizedAmount() != null) {
							lcc.setUtilizedAmount(lcc.getUtilizedAmount());
						}
						lcc.setLccApplicableFrom(lcc.getApplicableFrom());
						lcc.setLccApplicableTo(lcc.getApplicableTo());
						lcc.setLccRate(lcc.getRateAsPerTraces());
						lcc.setLccSection(lcc.getSectionAsPerTraces());
						lcc.setValidationStatus(false);
						// feign client for sections
						List<NatureOfPaymentMasterDTO> response = mastersClient.tcsFindAll().getBody().getData();
						Set<String> sections = response.parallelStream().map(NatureOfPaymentMasterDTO::getSection)
								.distinct().collect(Collectors.toSet());

						Double count = collecteeMasterDAO.getCollecteeBasedOnPanAndDeductorPan(lcc.getLccMasterPan(),
								deductorPan);
						logger.info("No of Records Present {}", count);

						LccErrorReportCsvDTO lccErrorReport = new LccErrorReportCsvDTO();
						boolean error = false;
						String reason = "";
						if (!lcc.getCollectorMasterTan().equalsIgnoreCase(deductorTan)) {
							error = true;
							reason = reason + "Collector tan " + lcc.getCollectorMasterTan() + " not match." + "\n";
						}

						if (lcc.getCertificateNumber().length() != 10) {
							error = true;
							reason = reason + "Lcc Certificate Number " + lcc.getCertificateNumber()
									+ " should contain 10 characters." + "\n";
						}

						if (!sections.contains(lcc.getSectionAsPerTraces())) {
							error = true;
							reason = reason + "Lcc Section " + lcc.getSectionAsPerTraces()
									+ " not found in system or Not related to collectee section." + "\n";
						}

						if (count == 0.0) {
							error = true;
							reason = reason + "Collectee pan " + lcc.getLccMasterPan() + " not found in system." + "\n";
						}
						if (lcc.getApplicableFrom() != null && lcc.getApplicableTo() != null) {
							if (!lcc.getApplicableFrom().before(lcc.getApplicableTo())) {
								error = true;
								reason = reason + " applicable from date " + lcc.getApplicableFrom()
										+ "should be less than applicable to date" + "\n";
							}
						}
						if (lcc.getAmount() != null && lcc.getUtilizedAmount() != null) {
							if (lcc.getUtilizedAmount().doubleValue() < 0) {
								error = true;
								reason = reason + "Limit utilized amount" + lcc.getUtilizedAmount()
										+ " should not contain -ve value" + "\n";
							}
							if (lcc.getAmount().doubleValue() < 0) {
								error = true;
								reason = reason + "Amount Consumed" + lcc.getAmount() + " should not contain -ve value"
										+ "\n";
							}
							if (lcc.getUtilizedAmount().doubleValue() > lcc.getAmount().doubleValue()) {
								error = true;
								reason = reason + " Limit utilised amount " + lcc.getUtilizedAmount()
										+ " should not be greater than certificate amount " + lcc.getAmount() + "\n";
							}
						}
						if (!error) {
							ldcList.add(lcc);
						}
						if (error == true && isDuplicate == false) {
							lccErrorReport = data.getErrorDTO(rowIndex);
							lccErrorReport.setReason(reason);
							errorList.add(lccErrorReport);
							++errorCount;
						}

					} catch (Exception e) { // inner catch
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						LccErrorReportCsvDTO problematicDataError = data.getErrorDTO(rowIndex);
						if (StringUtils.isEmpty(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			} // for 1

			List<TCSLccMaster> savedLccList = new ArrayList<>();
			for (TCSLccMaster lcc : ldcList) {
				if (lcc.getApplicableFrom() != null) {
					lcc.setLccApplicableFrom(lcc.getApplicableFrom());
				}
				if (lcc.getApplicableTo() != null) {
					lcc.setLccApplicableTo(lcc.getApplicableTo());
				}
				if (lcc.getLccSection() != null) {
					lcc.setLccSection(lcc.getLccSection());
				}
				if (lcc.getLccRate() != null) {
					lcc.setLccRate(lcc.getLccRate());
				}
				if (tcsLccMasterDao.getLccBycertificateNoPanTanSection(lcc.getCertificateNumber(),
						lcc.getLccMasterPan(), lcc.getSectionAsPerTraces(), deductorTan).isEmpty()) {
					TCSLccMaster savedLdc = tcsLccMasterDao.save(lcc);
					savedLccList.add(savedLdc);
					++successCount;
				} else {
					++duplicateCount;
				}
			}
			tcsBatchUpload.setSuccessCount((long) successCount);
			tcsBatchUpload.setFailedCount((long) errorCount);
			tcsBatchUpload.setProcessed(successCount);
			tcsBatchUpload.setDuplicateCount((long) duplicateCount);
			tcsBatchUpload.setStatus("Processed");
			tcsBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setCreatedBy(userName);

			if (errorList.size() > 0) {
				lccErrorFile = errorFileLdcUpload.lccErrorFile(uploadedFile.getOriginalFilename(), deductorTan,
						deductorPan, errorList, new ArrayList<>(data.getHeaders()));
			}
			// Generate LCC validation file only when entry is saved in DB
			if (!savedLccList.isEmpty()) {
				// Generating ldc validation file while uploading LCC Master upload
				MultipartFile file = errorFileLdcUpload.generateLccPanXlsxReport(savedLccList);
				String panUrl = blob.uploadExcelToBlob(file, tenantId);
				tcsBatchUpload.setOtherFileUrl(panUrl);
			}
		} catch (Exception e) { // catch block for outer try
			logger.error("File Reading Error", e);
		}
		return lccBatchUpload(tcsBatchUpload, uploadedFile, deductorTan, assesssmentYear, assessmentMonthPlusOne,
				userName, lccErrorFile, tenantId);
	}

	/**
	 * 
	 * @param uploadedFile
	 * @param sha256
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 * @throws Exception
	 */
	public TCSBatchUpload savePdfData(MultipartFile uploadedFile, String sha256, String deductorTan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, String tenantId,
			String deductorPan) throws Exception {

		TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
		tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
		tcsBatchUpload.setSuccessCount(0L);
		tcsBatchUpload.setFailedCount(0L);
		tcsBatchUpload.setRowsCount(0L);
		tcsBatchUpload.setProcessed(0);
		tcsBatchUpload.setMismatchCount(0L);
		tcsBatchUpload.setSha256sum(sha256);

		// converting multipart file to File
		File convFile = new File(uploadedFile.getOriginalFilename());
		convFile.createNewFile();
		try (FileOutputStream fos = new FileOutputStream(convFile)) {
			fos.write(uploadedFile.getBytes());
		}

		List<Integer> listSuccessCount = new ArrayList<>();
		List<Integer> listFailCount = new ArrayList<>();
		List<Integer> totalRecordCount = new ArrayList<>();

		PDDocument pd = PDDocument.load(convFile);
		ObjectExtractor oe = new ObjectExtractor(pd);
		SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();

		int count = pd.getNumberOfPages();
		String text = new PDFTextStripper().getText(pd);
		String tan = processField("\\bTAN\\/PAN\\s:\\s([\\S\\s^]*?)[\\n]", text, "TAN/PAN", "\\bTAN\\/PAN\\s:\\s");

		if (StringUtils.isNotBlank(tan) && tan.trim().equalsIgnoreCase(deductorTan)) {

			for (int j = 1; j <= count; j++) {
				Page page = oe.extract(j); // extract pages
				List<Table> listTable = sea.extract(page);
				logger.debug("No of Tables in Page:" + j + ":" + listTable.size());

				// iterating each table
				for (Table table : listTable) {
					final AtomicInteger rowIndexHolder = new AtomicInteger();
					int rowSize = table.getRows().size();
					table.getRows().forEach(row -> {
						TCSLccMaster lccMaster = new TCSLccMaster();
						lccMaster.setActive(true);
						lccMaster.setAssessmentYear(assesssmentYear);
						lccMaster.setCollectorMasterTan(deductorPan);
						lccMaster.setLccMasterPan(deductorPan);
						lccMaster.setCreatedBy(userName);
						lccMaster.setCreatedDate(new Timestamp(new Date().getTime()));
						lccMaster.setUtilizedAmount(BigDecimal.ZERO);

						int rowIndex = rowIndexHolder.getAndIncrement();
						final AtomicInteger indexHolder = new AtomicInteger();
						// restricting to iterate header row
						if (rowIndex > 0 && rowSize >= rowIndex) {
							row.forEach(cell -> {
								int columnIndex = indexHolder.getAndIncrement();
								if (columnIndex == 1) {
									lccMaster.setCertificateNumber(cell.getText());
								} else if (columnIndex == 2) {
									lccMaster.setLccMasterPan(cell.getText());
								} else if (columnIndex == 3) {
									lccMaster.setCollecteeName((cell.getText()));
								} else if (columnIndex == 4) {
									lccMaster.setLccSection(cell.getText());
								} else if (columnIndex == 5) {
									lccMaster.setNatureOfIncome(cell.getText());
								} else if (columnIndex == 6) {
									if (StringUtils.isNotBlank(cell.getText())) {
										try {
											lccMaster.setAmount(
													new BigDecimal((cell.getText())).setScale(4, BigDecimal.ROUND_UP));
										} catch (Exception e) {
											logger.error("error occured while parsing ammount.", e);
										}
									}
								} else if (columnIndex == 7) {
									if (StringUtils.isNotBlank(cell.getText())) {
										try {
											lccMaster.setLccRate((new BigDecimal((cell.getText())).setScale(4,
													BigDecimal.ROUND_HALF_DOWN)));
										} catch (Exception e) {
											logger.error("error occured while parsing ammount.", e);
										}
									}
								} else if (columnIndex == 8) {
									try {
										lccMaster.setApplicableFrom(
												new SimpleDateFormat("dd-MMM-yyyy").parse(cell.getText()));
									} catch (Exception e) {
										logger.error("Error occured while parsing valid from date.", e);
										lccMaster.setApplicableFrom(new Date());
									}
								} else if (columnIndex == 9) {
									try {
										lccMaster.setApplicableTo(
												new SimpleDateFormat("dd-MMM-yyyy").parse(cell.getText()));
									} catch (Exception e) {
										logger.error("Error occured while parsing valid till date.", e);
										lccMaster.setApplicableTo(new Date());
									}
								}

							});// inner forEach

							if (StringUtils.isNotBlank(lccMaster.getCertificateNumber()) && tcsLccMasterDao
									.getLccBycertificateNoPanTanSection(lccMaster.getCertificateNumber(),
											lccMaster.getLccMasterPan(), lccMaster.getNatureOfIncome(), deductorTan)
									.isEmpty()) {
								totalRecordCount.add(1); // counting no of records present in table
								try {
									tcsLccMasterDao.save(lccMaster);
									listSuccessCount.add(1);
									tcsBatchUpload.setStatus("Processed");
								} catch (Exception e) {
									logger.error("error while saving record :" + e);
									listFailCount.add(1);
								}

							} else {
								tcsBatchUpload.setStatus("Duplicate");
							}

						} // if block
					}); // outer forEach for
				} // inner for
			} // outer for

		} else {
			tcsBatchUpload.setStatus("Incorrect TAN");
		}
		tcsBatchUpload.setRowsCount((long) totalRecordCount.size());
		tcsBatchUpload.setSuccessCount((long) listSuccessCount.size());
		tcsBatchUpload.setFailedCount((long) listFailCount.size());
		tcsBatchUpload.setProcessed(listSuccessCount.size());
		tcsBatchUpload.setDuplicateCount(0L);
		tcsBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
		tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
		tcsBatchUpload.setCreatedBy(userName);
		return lccBatchUpload(tcsBatchUpload, uploadedFile, deductorTan, assesssmentYear, assessmentMonthPlusOne,
				userName, null, tenantId);

	}

	/**
	 * 
	 * @param multiPartFile
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public TCSBatchUpload saveLdcPdfData(MultipartFile multiPartFile, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonthPlusOne, String userName, String tenantId, String deductorPan) throws Exception {

		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);

		if (isAlreadyProcessed(sha256)) {
			TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
			tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setCreatedBy(userName);
			tcsBatchUpload.setSuccessCount(0L);
			tcsBatchUpload.setFailedCount(0L);
			tcsBatchUpload.setRowsCount(0L);
			tcsBatchUpload.setProcessed(0);
			tcsBatchUpload.setMismatchCount(0L);
			tcsBatchUpload.setSha256sum(sha256);
			tcsBatchUpload.setStatus("Duplicate");
			tcsBatchUpload.setNewStatus("Duplicate");
			tcsBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			TCSBatchUpload batchUploadResponse = lccBatchUpload(tcsBatchUpload, multiPartFile, deductorTan,
					assesssmentYear, assessmentMonthPlusOne, userName, null, tenantId);
			return batchUploadResponse;
		} else {
			return savePdfData(multiPartFile, sha256, deductorTan, assesssmentYear, assessmentMonthPlusOne, userName,
					tenantId, deductorPan);
		}
	}

	/**
	 * this is to generate excell file having Ldc_Master data
	 * 
	 * @param deductorTan
	 * @param tenantId
	 * @param deductorPan
	 * @param year
	 * @param month
	 * @return
	 * @throws IOException
	 */
	public ByteArrayInputStream exportLccMasterData(String deductorTan, String tenantId) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		List<TCSLccMaster> listOfLdc = null;
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			XSSFSheet sheet = wb.createSheet("LDC_MASTER_DATA");
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);
			sheet.setDefaultColumnWidth(25);

			XSSFRow row1 = sheet.createRow(0);
			XSSFCellStyle style0 = wb.createCellStyle();

			style0.setBorderTop(BorderStyle.MEDIUM);
			style0.setBorderBottom(BorderStyle.MEDIUM);
			style0.setBorderRight(BorderStyle.MEDIUM);
			style0.setAlignment(HorizontalAlignment.CENTER);
			style0.setVerticalAlignment(VerticalAlignment.CENTER);

			CreationHelper creationHelper = wb.getCreationHelper();
			CellStyle style1 = wb.createCellStyle();
			style1.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mm-yyyy"));
			style1.setAlignment(HorizontalAlignment.LEFT);
			style1.setVerticalAlignment(VerticalAlignment.CENTER);

			Font font = wb.createFont();
			font.setBold(true);
			font.setFontName("Arial");
			style0.setFont(font);
			DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();
			style0.setFillForegroundColor(new XSSFColor(new java.awt.Color(169, 209, 142), defaultIndexedColorMap));
			style0.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			row1.createCell(0).setCellValue("LCC Certificate number");
			row1.getCell(0).setCellStyle(style0);
			row1.createCell(1).setCellValue("Collector TAN");
			row1.getCell(1).setCellStyle(style0);
			row1.createCell(2).setCellValue("Collectee Name");
			row1.getCell(2).setCellStyle(style0);
			row1.createCell(3).setCellValue("Collectee PAN");
			row1.getCell(3).setCellStyle(style0);
			row1.createCell(4).setCellValue("Amount");
			row1.getCell(4).setCellStyle(style0);
			row1.createCell(5).setCellValue("Rate");
			row1.getCell(5).setCellStyle(style0);
			row1.createCell(6).setCellValue("Applicable From");
			row1.getCell(6).setCellStyle(style0);
			row1.createCell(7).setCellValue("Applicable To");
			row1.getCell(7).setCellStyle(style0);
			row1.createCell(8).setCellValue("Status");
			row1.getCell(8).setCellStyle(style0);
			row1.createCell(9).setCellValue("Validation Date");
			row1.getCell(9).setCellStyle(style0);
			row1.createCell(10).setCellValue("Limit Utilised");
			row1.getCell(10).setCellStyle(style0);
			row1.createCell(11).setCellValue("Traces Limit Utilised");
			row1.getCell(11).setCellStyle(style0);
			row1.createCell(12).setCellValue("Section");
			row1.getCell(12).setCellStyle(style0);
			try {
				listOfLdc = tcsLccMasterDao.getLccByTan(deductorTan, tenantId);
			} catch (Exception e) {
				logger.error("error while getting Ldc_master data :" + e);
			}
			sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 12));
			int rowNo = 1;
			for (TCSLccMaster lccMaster : listOfLdc) {
				XSSFRow row = sheet.createRow(rowNo++);
				row.createCell(0).setCellValue(lccMaster.getCertificateNumber());
				row.createCell(1).setCellValue(lccMaster.getCollectorMasterTan());
				row.createCell(2).setCellValue(lccMaster.getCollecteeName());
				row.createCell(3).setCellValue(lccMaster.getLccMasterPan());
				row.createCell(4).setCellValue(lccMaster.getAmount().toString());
				row.createCell(5).setCellValue(lccMaster.getRateAsPerTraces().toString());
				row.createCell(6).setCellValue(lccMaster.getApplicableFrom());
				row.getCell(6).setCellStyle(style1);
				row.createCell(7).setCellValue(lccMaster.getApplicableTo());
				row.getCell(7).setCellStyle(style1);
				row.createCell(8).setCellValue(lccMaster.getValidationStatus());
				row.createCell(9).setCellValue(lccMaster.getValidationDate());
				row.getCell(9).setCellStyle(style1);
				Double ldcUtilizationAmount = tcsLccMasterDao.getTotalLccUtilizationAmount(lccMaster.getId(),
						deductorTan);
				row.createCell(10).setCellValue(ldcUtilizationAmount.toString());
				row.createCell(11).setCellValue(lccMaster.getTracesUtilizedAmount().toString());
				row.createCell(12).setCellValue(lccMaster.getSectionAsPerTraces());
			}
			sheet.autoSizeColumn(2);
			wb.write(out);
		} // try with resource
		return new ByteArrayInputStream(out.toByteArray());
	}

	/**
	 * This method for deactive ldc certificate.
	 * 
	 * @param deductorTan
	 * @param id
	 * @param ldcPan
	 * @return
	 */
	public TCSLccMaster deactivateLccCertificate(String ldcPan, String deductorTan, Integer id) {
		List<TCSLccMaster> ldcMaster = tcsLccMasterDao.getLccById(id, deductorTan);
		TCSLccMaster lcc = new TCSLccMaster();
		if (!ldcMaster.isEmpty()) {
			lcc = ldcMaster.get(0);
			Double ldcUtilizationAmount = tcsLccMasterDao.getTotalLccUtilizationAmount(id, deductorTan);
			logger.info("ldc utilization amount is: {}", ldcUtilizationAmount);
			if (ldcUtilizationAmount <= 0) {
				lcc.setActive(false);
				tcsLccMasterDao.update(lcc);
			} else {
				throw new CustomException("UTILIZATION AMOUNT NOT EQUAL TO ZERO, CAN'T DEACTIVATE LDC",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return lcc;
	}

	/**
	 * 
	 * @param pattern
	 * @param fileContent
	 * @param fieldName
	 * @param replacement
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private String processField(String pattern, String fileContent, String fieldName, String replacement)
			throws InterruptedException, ExecutionException {
		ExecutorService pool = Executors.newFixedThreadPool(1);
		List<Callable<String>> callables = new ArrayList<>();
		callables.add(new ValueProcessor(fileContent, pattern, false, replacement));
		List<Future<String>> futures = pool.invokeAll(callables);
		awaitTerminationAfterShutdown(pool);
		String value = "";
		for (Future<String> future : futures) {
			if (future != null && StringUtils.isNotBlank(future.get())) {
				value = future.get();
			}
		}
		return value;
	}

	public void awaitTerminationAfterShutdown(ExecutorService threadPool) {
		threadPool.shutdown();
		try {
			if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
				threadPool.shutdownNow();
			}
		} catch (InterruptedException ex) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * 
	 * @param ldcPan
	 * @param deductorTan
	 * @param id
	 * @return
	 */
	public List<TCSLccUtilization> tcsLccUtilization(String deductorTan, Integer lccMasterId) {
		List<TCSLccUtilization> utilizationList = new ArrayList<>();
		utilizationList = tcsLccUtilizationDAO.findByTcsLccMaster(deductorTan, lccMasterId);
		if (!utilizationList.isEmpty()) {
			return utilizationList;
		} else {
			return utilizationList;
		}
	}

	/**
	 * 
	 * @param lccId
	 * @param tenantId
	 * @param tanNumber
	 * @param userName
	 * @param collectorPan
	 * @throws Exception
	 */
	@Async
	public void generateLccUtilizationFileAsync(Integer lccId, String tenantId, String tanNumber, String userName,
			String collectorPan) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		Integer year = CommonUtil.getAssessmentYear(null);
		TCSBatchUpload batchUpload = new TCSBatchUpload();
		batchUpload = saveConsolidatedInBatchUpload(null, "LCC_UTILIZATION_REPORT", "Processing", tenantId, 0L,
				userName, null, tanNumber, year);
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setGridlinesVisible(false);
		worksheet.autoFitColumns();

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		Style style0 = workbook.createStyle();
		style0.setForegroundColor(Color.fromArgb(159, 223, 159));
		style0.setPattern(BackgroundType.SOLID);
		style0.getFont().setBold(true);
		style0.getFont().setSize(10);
		style0.setHorizontalAlignment(TextAlignmentType.CENTER);
		// setting borders to cells
		setBorderToCell(style0);

		Style style1 = workbook.createStyle();
		style1.setPattern(BackgroundType.SOLID);
		style1.setHorizontalAlignment(TextAlignmentType.LEFT);
		setBorderToCell(style1);

		Style style2 = workbook.createStyle();
		style2.setPattern(BackgroundType.SOLID);
		style2.setHorizontalAlignment(TextAlignmentType.LEFT);
		style2.getFont().setBold(true);
		setBorderToCell(style2);

		Range headerColorRange1 = worksheet.getCells().createRange("A5:R5");
		headerColorRange1.setStyle(style0);

		worksheet.setName("lcc_utiliation_report");

		String msg = getErrorReportMsg(tanNumber, tenantId, collectorPan);
		worksheet.getCells().get("A1").setValue(msg);
		worksheet.getCells().merge(0, 0, 4, 5);
		headerColorRange1 = worksheet.getCells().createRange("A1:A1");
		headerColorRange1.setStyle(style2);

		String[] headers = { "Certificate No", "Assesment Year", "Assesment Month", "LCC Rate", "LCC Section",
				"LCC Master Total Amount", "LCC Applicable From", "LCC Applicable To", "Document Number",
				"Document Date", "Line Item Number", "Final TCS Rate", "Final TCS Section", "Final TCS Amount",
				"Applicable Total Taxable Amount", "Utilized Amount", "Remaining Amount","Initial Utilized Amount" };
		worksheet.getCells().importArray(headers, 4, 0, false);
		// TODO need to get data from db
		List<LccUtilizationDTO> list = tcsLccUtilizationDAO.getLccUtilizationRecordsById(lccId);
		int rowIndex = 5;
		for (LccUtilizationDTO data : list) {
			List<Object> rowData = new ArrayList<>();
			// Certificate No
			rowData.add(
					StringUtils.isEmpty(data.getCertificateNumber()) ? StringUtils.EMPTY : data.getCertificateNumber());
			// Assesment Year
			rowData.add(data.getAssesmentYear() == null ? StringUtils.EMPTY : data.getAssesmentYear().toString());
			// Assesment Month
			rowData.add(data.getChallanMonth() == null ? StringUtils.EMPTY : data.getChallanMonth().toString());
			// LCC Rate
			rowData.add(data.getLccRate() == null ? "0.0" : data.getLccRate());
			// LCC Section","
			rowData.add(StringUtils.isEmpty(data.getLccSection()) ? StringUtils.EMPTY : data.getLccSection());
			// LCC Master Total Amount
			rowData.add(data.getLccMasterTotalAmount() == null ? "0.0" : data.getLccMasterTotalAmount().toString());
			// LCC Applicable From",
			rowData.add(
					data.getLccApplicableFrom() == null ? StringUtils.EMPTY : data.getLccApplicableFrom().toString());
			// "LCC Applicable To","
			rowData.add(data.getLccApplicableTo() == null ? StringUtils.EMPTY : data.getLccApplicableTo().toString());
			// Document Number",""
			rowData.add(StringUtils.isEmpty(data.getDocumentNumber()) ? StringUtils.EMPTY : data.getDocumentNumber());
			// Document Date
			rowData.add(data.getDocumentDate() == null ? StringUtils.EMPTY : data.getDocumentDate().toString());
			// Line Item Number","",
			rowData.add(StringUtils.isEmpty(data.getLineItemNumber()) ? StringUtils.EMPTY : data.getLineItemNumber());
			// Final TCS Rate
			rowData.add(data.getFinalTcsRate() == null ? "0.0" : data.getFinalTcsRate().toString());
			// Final TCS Section
			rowData.add(
					StringUtils.isEmpty(data.getFinalTcscSection()) ? StringUtils.EMPTY : data.getFinalTcscSection());
			// ","Final TCS Amount
			rowData.add(data.getFinalTcsAmount() == null ? "0.0"
					: data.getFinalTcsAmount().setScale(2, BigDecimal.ROUND_HALF_DOWN).toString());
			// ","Applicable Total Taxable Amount"
			rowData.add(data.getApplicableTotalTaxableAmount() == null ? "0.0"
					: data.getApplicableTotalTaxableAmount().toString());
			// Utilized Amount",""
			rowData.add(data.getUtilizedAmount() == null ? "0.0" : data.getUtilizedAmount().toString());
			// Remaining Amount
			rowData.add(data.getRemainingAmount() == null ? StringUtils.EMPTY : data.getRemainingAmount().toString());
			// initial utilized amount
			rowData.add(data.getInitialUtilizedAmount() == null ? StringUtils.EMPTY
					: data.getInitialUtilizedAmount().toString());

			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
			Range headerColorRange2 = worksheet.getCells().createRange("A" + rowIndex + ":" + "R" + rowIndex + "");
			headerColorRange2.setStyle(style1);
		}
		worksheet.autoFitColumns();
		worksheet.autoFitRows();

		File generatedExcelFile = new File("LCC_Utilization_Report" + new Date().getTime() + ".xlsx");
		OutputStream out = new FileOutputStream(generatedExcelFile);
		workbook.save(out, SaveFormat.XLSX);
		saveConsolidatedInBatchUpload(batchUpload.getId(), "LCC_UTILIZATION_REPORT", "Processed", tenantId,
				(long) list.size(), userName, generatedExcelFile, tanNumber, year);

	}

	public TCSBatchUpload saveConsolidatedInBatchUpload(Integer batchUploadId, String uploadType, String status,
			String tenantId, Long rowCount, String userName, File file, String tan, Integer year)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		String fileName = "";
		String filePath = "";
		TCSBatchUpload batchupload = null;
		if (file != null) {
			fileName = uploadType.toLowerCase() + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
			filePath = blob.uploadExcelToBlobWithFile(file, tenantId);
			logger.info(" Consolidated report {} completed for : {}", uploadType, userName);
		} else {
			logger.info(" Consolidated report {} started for : {}", uploadType, userName);
		}
		if (batchUploadId != null) {
			batchupload = tcsBatchUploadDAO.findById(batchUploadId).get(0);
			batchupload.setProcessEndTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			batchupload.setModifiedBy(userName);
			batchupload.setModifiedDate(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			batchupload.setFileName(fileName);
			batchupload.setRowsCount(rowCount);
			batchupload.setStatus(status);
			batchupload.setFilePath(filePath);
			return tcsBatchUploadDAO.update(batchupload);
		} else {

			batchupload = new TCSBatchUpload();
			batchupload.setCollectorMasterTan(tan);
			batchupload.setAssessmentYear(year);
			batchupload.setActive(true);
			batchupload.setStatus(status);
			batchupload.setCreatedBy(userName);
			batchupload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchupload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchupload.setUploadType(uploadType);
			return tcsBatchUploadDAO.save(batchupload);
		}
	}

	private Style setBorderToCell(Style style) {
		style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());
		return style;
	}

	public String getErrorReportMsg(String collectorTan, String tenantId, String collectorPan) {
		DeductorMaster deductorData = deductorMasterDAO.findBasedOnDeductorPan(collectorPan).get(0);
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return "TCS Mismatch Report (Dated: " + date + ")\n Client Name: " + deductorData.getName() + "\n";
	}
}
