package com.ey.in.tds.onboarding.service.ldc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.LDCUtilizationDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.LdcMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.LdcMasterDTO;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.NewLDCMasterTracesDTO;
import com.ey.in.tds.common.ingestion.response.dto.BatchUploadResponseDTO;
import com.ey.in.tds.common.model.ldc.LdcErrorReportCsvDTO;
import com.ey.in.tds.common.model.ldc.TracesLdcMasterErrorReportDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.EmailNotificationConfiguration;
import com.ey.in.tds.common.onboarding.jdbc.dto.EmailNotificationConfiguration.Types;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcCountDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcUtilization;
import com.ey.in.tds.common.onboarding.jdbc.dto.NewLDCMasterTraces;
import com.ey.in.tds.common.onboarding.response.dto.LdcResponseDTO;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.CommonValidationsCassandra;
import com.ey.in.tds.common.util.ValueProcessor;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.dto.EmailNotificationConfigurationDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.util.ActivityTrackerStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.jdbc.dao.DeducteeMasterNonResidentialDAO;
import com.ey.in.tds.jdbc.dao.DeducteeMasterResidentialDAO;
import com.ey.in.tds.jdbc.dao.EmailNotificationConfigurationDAO;
import com.ey.in.tds.jdbc.dao.ShareholderMasterNonResidentialDAO;
import com.ey.in.tds.jdbc.dao.ShareholderMasterResidentialDAO;
import com.ey.in.tds.onboarding.dto.ao.AoLdcMasterDTO;
import com.ey.in.tds.onboarding.service.util.excel.ldc.LdcExcel;
import com.ey.in.tds.onboarding.service.util.excel.ldc.TracesLdcMasterExcel;
import com.microsoft.azure.storage.StorageException;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

@Service
public class LdcMasterService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BlobStorage blob;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	ErrorFileLdcUpload errorFileLdcUpload;

	@Autowired
	LdcMasterDAO ldcMasterDao;

	@Autowired
	private LDCUtilizationDAO ldcUtilizationDAO;

	@Autowired
	BatchUploadDAO batchUploadDAO;

	@Autowired
	MastersClient mastersClient;

	@Autowired
	private DeducteeMasterResidentialDAO deducteeMasterResidentialDAO;

	@Autowired
	private EmailNotificationConfigurationDAO emailNotificationConfigurationDAO;

	@Autowired
	private ShareholderMasterResidentialDAO shareholderMasterResidentialDAO;

	@Autowired
	private ShareholderMasterNonResidentialDAO shareholderMasterNonResidentialDAO;

	@Autowired
	private DeducteeMasterNonResidentialDAO deducteeMasterNonResidentialDAO;

	@Autowired
	private ResourceLoader resourceLoader;

	public LdcMaster create(LdcMasterDTO ldcMasterDTO, String deductorTan, Integer assessmentYear, String userName) {
		// LdcMaster ldcMaster = new LdcMaster();
		LdcMaster ldcMaster = new LdcMaster();
		if (ldcMasterDTO.getDividendProcessing() == null || ldcMasterDTO.getDividendProcessing().equals(false)) {
			List<LdcMaster> listLdcMsater = ldcMasterDao.getLdcBycertificateNoPanTanSection(
					ldcMasterDTO.getLdcCertificateNumber(), ldcMasterDTO.getDeducteePan(),
					ldcMasterDTO.getNatureOfPaymentSection(), deductorTan);
			if (!listLdcMsater.isEmpty()) {
				throw new CustomException(
						"Already record present with Certificate Number, section and Pan Combination, Cannot add duplicate Record",
						HttpStatus.INTERNAL_SERVER_ERROR);
			} else {

				List<LdcMaster> ldcMasterDb = ldcMasterDao.getLdcBycertificateNoAndDeducteePan(
						ldcMasterDTO.getLdcCertificateNumber(), ldcMasterDTO.getDeducteePan(), deductorTan);
				for (LdcMaster ldc : ldcMasterDb) {
					CommonValidationsCassandra.validateApplicableFields(ldc.getApplicableTo(),
							ldcMasterDTO.getApplicableFrom());
				}
			}
		} else {
			List<LdcMaster> ldc = ldcMasterDao.getLdcMasterBasedOnPanForDividend(ldcMasterDTO.getDeducteePan(),
					deductorTan);
			if (!ldc.isEmpty()) {
				CommonValidationsCassandra.validateApplicableFields(ldc.get(0).getApplicableTo(),
						ldcMasterDTO.getApplicableFrom());
			}
		}

		ldcMaster.setAssessmentYear(assessmentYear);
		ldcMaster.setPan(ldcMasterDTO.getDeducteePan());
		ldcMaster.setTanNumber(deductorTan);
		ldcMaster.setActive(true);
		ldcMaster.setDeducteeName(ldcMasterDTO.getDeducteeName());
		ldcMaster.setAmount(ldcMasterDTO.getAmount());
		ldcMaster.setApplicableFrom(ldcMasterDTO.getApplicableFrom());
		ldcMaster.setApplicableTo(ldcMasterDTO.getApplicableTo());
		ldcMaster.setCertificateNumber(ldcMasterDTO.getLdcCertificateNumber());
		ldcMaster.setNatureOfPayment(ldcMasterDTO.getNatureOfPaymentSection());
		ldcMaster.setSection(ldcMasterDTO.getNatureOfPaymentSection());
		ldcMaster.setRate(ldcMasterDTO.getLdcRate());
		BigDecimal limitUtilized = ldcMasterDTO.getLimitUtilised() != null ? ldcMasterDTO.getLimitUtilised()
				: BigDecimal.ZERO;
		ldcMaster.setUtilizedAmount(limitUtilized);
		ldcMaster.setInitialUtilizedAmount(limitUtilized);
		// remaining amount
		ldcMaster.setRemainingAmount(ldcMaster.getAmount().subtract(limitUtilized));
		ldcMaster.setCreatedBy(userName);
		ldcMaster.setCreatedDate(new Timestamp(new Date().getTime()));
		ldcMaster.setModifiedBy(userName);
		ldcMaster.setModifiedDate(new Timestamp(new Date().getTime()));
		ldcMaster.setDividendProcessing(ldcMasterDTO.getDividendProcessing());
		ldcMaster.setAssessingOfficerDetails(ldcMasterDTO.getAssessingOfficerDetails());
		ldcMaster.setIsResident(ldcMasterDTO.getResidentialStatus().equals("RES") ? "Yes" : "No");
		ldcMaster.setLdcSection(ldcMasterDTO.getLdcSection());
		if (StringUtils.isBlank(ldcMaster.getLdcStatus())) {
			ldcMaster.setLdcStatus("");
		}
		ldcMaster.setThresholdLimit(ldcMasterDTO.getAmount().intValue());
		ldcMaster = ldcMasterDao.save(ldcMaster);
		return ldcMaster;

	}

	public LdcMaster update(LdcMasterDTO ldcMasterDTO, String deductorTan, Integer assessmentYear, String userName) {
		LdcMaster ldc = ldcMasterDao.getLdcById(ldcMasterDTO.getId());
		if (ldc != null) {
			ldc.setActive(true);
			ldc.setAmount(ldcMasterDTO.getAmount().setScale(2, RoundingMode.UP));
			ldc.setApplicableFrom(ldcMasterDTO.getApplicableFrom());
			ldc.setApplicableTo(ldcMasterDTO.getApplicableTo());
			ldc.setCertificateNumber(ldcMasterDTO.getLdcCertificateNumber());
			ldc.setModifiedBy(userName);
			ldc.setModifiedDate(new Timestamp(new Date().getTime()));
			return ldcMasterDao.update(ldc);
		}
		return ldc;
	}

	public List<LdcMasterDTO> getListOfLdc(Integer assessmentYear, String deductorTan, String tenantId) {

		List<LdcMasterDTO> listLdc = new ArrayList<>();

		List<LdcMaster> ldcList = ldcMasterDao.getLdcByTan(deductorTan, tenantId);

		logger.info("size of ldc list---{}", ldcList.size());

		Collections.reverse(ldcList);
		for (LdcMaster ldcMaster : ldcList) {
			LdcMasterDTO ldcMasterDTO = new LdcMasterDTO();
			ldcMasterDTO.setAmount(ldcMaster.getAmount());
			ldcMasterDTO.setApplicableFrom(ldcMaster.getApplicableFrom());
			ldcMasterDTO.setApplicableTo(ldcMaster.getApplicableTo());
			ldcMasterDTO.setDeducteeName(ldcMaster.getDeducteeName());
			ldcMasterDTO.setDeductorTan(ldcMaster.getTanNumber());
			ldcMasterDTO.setLdcRate(ldcMaster.getRate());
			ldcMasterDTO.setDeducteePan(ldcMaster.getPan());
			ldcMasterDTO.setLdcCertificateNumber(ldcMaster.getCertificateNumber());
			ldcMasterDTO.setId(ldcMaster.getLdcMasterID());
			ldcMasterDTO.setNatureOfPaymentSection(ldcMaster.getSection());
			ldcMasterDTO.setDbRate(ldcMaster.getDbRate());
			ldcMasterDTO.setDbApplicableFrom(ldcMaster.getDbApplicableFrom());
			ldcMasterDTO.setDbApplicableTo(ldcMaster.getDbApplicableTo());
			ldcMasterDTO.setDbSection(ldcMaster.getDbSection());
			ldcMasterDTO.setLdcStatus(ldcMaster.getLdcStatus() != null ? ldcMaster.getLdcStatus() : "");
			ldcMasterDTO.setValidationDate(ldcMaster.getValidationDate());
			ldcMasterDTO.setCreatedDate(ldcMaster.getCreatedDate());
			ldcMasterDTO.setDividendProcessing(ldcMaster.getDividendProcessing());
			ldcMasterDTO.setIsExtend(ldcMaster.getIsExtend() != null ? ldcMaster.getIsExtend() : false);
			ldcMasterDTO.setLimitUtilised(ldcMaster.getUtilizedAmount());
			ldcMasterDTO.setAssessingOfficerDetails(ldcMaster.getAssessingOfficerDetails());
			ldcMasterDTO.setLdcSection(ldcMaster.getLdcSection());
			ldcMasterDTO.setResidentialStatus(ldcMaster.getIsResident().equals("Yes") ? "RES" : "NR");
			listLdc.add(ldcMasterDTO);
		}

		return listLdc;
	}

	public LdcMasterDTO getLdcMasterDTO(String deductorTan, Integer ldcId) {
		LdcMasterDTO ldcMasterDTO = new LdcMasterDTO();
		List<LdcMaster> response = ldcMasterDao.getLdcByIdTanYear(ldcId, deductorTan);
		if (!response.isEmpty()) {
			LdcMaster ldc = response.get(0);
			ldcMasterDTO.setId(ldc.getLdcMasterID());// ldc.getLdcMasterID()
			ldcMasterDTO.setLdcCertificateNumber(ldc.getCertificateNumber());
			ldcMasterDTO.setDeducteeName(ldc.getDeducteeName());
			ldcMasterDTO.setDeducteePan(ldc.getPan());
			ldcMasterDTO.setDeductorTan(ldc.getTanNumber());
			ldcMasterDTO.setNatureOfPaymentSection(ldc.getSection());
			ldcMasterDTO.setAmount(ldc.getAmount());
			ldcMasterDTO.setLdcRate(ldc.getRate());
			ldcMasterDTO.setApplicableFrom(ldc.getApplicableFrom());
			ldcMasterDTO.setApplicableTo(ldc.getApplicableTo());
			ldcMasterDTO.setLimitUtilised(ldc.getUtilizedAmount());
			ldcMasterDTO.setCreatedDate(ldc.getCreatedDate());
			ldcMasterDTO.setDividendProcessing(ldc.getDividendProcessing());
			ldcMasterDTO.setAssessingOfficerDetails(ldc.getAssessingOfficerDetails());
			ldcMasterDTO.setLdcSection(ldc.getLdcSection());
			ldcMasterDTO.setResidentialStatus(ldc.getIsResident().equals("Yes") ? "NR" : "RES");
		}
		return ldcMasterDTO;
	}

	public ByteArrayInputStream exportToExcel() throws IOException {

		String[] COLUMNS = { "Certification Number", "Deductee Name", "Amount", "Limit Utilised", "Rate",
				"Assessment Year" };
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {

			Sheet sheet = workbook.createSheet("Ldc_Master");
			sheet.setDefaultColumnWidth(14);
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setColor(IndexedColors.BLUE.getIndex());
			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			// Row for Header
			Row headerRow = sheet.createRow(0);
			// Header
			for (int col = 0; col < COLUMNS.length; col++) {
				Cell cell = headerRow.createCell(col);
				cell.setCellValue(COLUMNS[col]);
				cell.setCellStyle(headerCellStyle);
			}
			List<LdcMaster> ldcMasterFindAll = ldcMasterDao.findAll();

			CellStyle cellStyle = workbook.createCellStyle();
			int rowIdx = 1;
			for (LdcMaster ldc : ldcMasterFindAll) {
				Row row = sheet.createRow(rowIdx++);
				row.createCell(0).setCellValue(ldc.getCertificateNumber());
				row.createCell(1).setCellValue(ldc.getDeducteeName());
				if (ldc.getAmount() != null) {
					row.createCell(2).setCellValue(ldc.getAmount().doubleValue());
				}
				if (ldc.getUtilizedAmount() != null) {
					row.createCell(3).setCellValue(ldc.getUtilizedAmount().doubleValue());
				}
				if (ldc.getRate() != null) {
					row.createCell(4).setCellValue(ldc.getRate().doubleValue());
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
	 * To upload multiple Files of Pdfs
	 * 
	 * @param files
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public BatchUploadResponseDTO saveToBatchUploadPdfs(MultipartFile[] files, String tan, Integer assessmentYear,
			String userName) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		BatchUpload batchUpload = null;
		if (files.length == 0) {
			throw new FileStorageException("Please select atleast One file");
		} else {
			for (int i = 0; i < files.length; i++) {
				if (!files[i].getOriginalFilename().split("\\.")[1].equalsIgnoreCase("pdf")) {
					throw new FileStorageException("Cannot upload this file (" + files[i].getOriginalFilename()
							+ ") as this type of file is not accepted");
				} else {
					String sha256 = sha256SumService.getSHA256Hash(files[i]);

					List<BatchUpload> batch = batchUploadDAO.getSha256Records(sha256);
					String path = blob.uploadExcelToBlob(files[i]);
					batchUpload = new BatchUpload();
					if (batch.isEmpty()) {
						batchUpload.setAssessmentYear(assessmentYear);
						batchUpload.setDeductorMasterTan(tan);
						batchUpload.setUploadType(UploadTypes.LDC_PDF.name());
						batchUpload.setStatus("Uploaded");
					} else {
						batchUpload.setAssessmentYear(assessmentYear);
						batchUpload.setDeductorMasterTan(tan);
						batchUpload.setUploadType(UploadTypes.LDC_PDF.name());
						batchUpload.setReferenceId(batch.get(0).getBatchUploadID());
						batchUpload.setStatus("Duplicate");
					}
					batchUpload.setSha256sum(sha256);
					batchUpload.setFileName(files[i].getOriginalFilename());
					batchUpload.setFilePath(path);
					batchUpload.setCreatedBy(userName);
					batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
					batchUpload.setModifiedBy(userName);
					batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
					batchUpload.setActive(true);
					batchUpload.setSuccessCount(0L);
					batchUpload.setFailedCount(0L);
					batchUpload.setRowsCount(0L);
					batchUpload.setDuplicateCount(0L);
					batchUpload.setMismatchCount(0L);
					batchUpload.setCreatedBy(userName);
					batchUpload = batchUploadDAO.save(batchUpload);

				}
			}
		}

		return batchUploadDAO.copyToResponseDTO(batchUpload);
	}

	public BatchUploadResponseDTO saveToBatchUploadExcel(MultipartFile file, String tan, Integer assessmentYear,
			String userName) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		String path = blob.uploadExcelToBlob(file);

		BatchUpload batchUpload = null;

		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Please select the excel file only");
		} else {
			batchUpload = new BatchUpload();
			batchUpload.setFileName(file.getOriginalFilename());
			batchUpload.setFilePath(path);
			String sha256 = sha256SumService.getSHA256Hash(file);

			List<BatchUpload> batch = batchUploadDAO.getSha256Records(sha256);
			if (batch.isEmpty()) {
				batchUpload.setAssessmentYear(assessmentYear);
				batchUpload.setDeductorMasterTan(tan);
				batchUpload.setUploadType(UploadTypes.LDC_EXCEL.name());
				batchUpload.setStatus("Uploaded");
			} else {
				batchUpload.setAssessmentYear(assessmentYear);
				batchUpload.setDeductorMasterTan(tan);
				batchUpload.setUploadType(UploadTypes.LDC_EXCEL.name());
				batchUpload.setStatus("Duplicate");
				batchUpload.setReferenceId(batch.get(0).getBatchUploadID());
			}

			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setActive(true);
			batchUpload.setCreatedBy(userName);
			batchUpload.setModifiedBy(userName);
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setMismatchCount(0L);
			batchUpload.setDuplicateCount(0L);
			batchUpload = batchUploadDAO.save(batchUpload);
			saveExcelData(file, assessmentYear);

			return batchUploadDAO.copyToResponseDTO(batchUpload);
		}
	}

	@SuppressWarnings("unused")
	private void saveDataFromExcel(MultipartFile file, Integer assessmentYear) throws IOException {

		XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
		XSSFSheet worksheet = workbook.getSheetAt(0);
		int in = 1;

		while (in <= worksheet.getLastRowNum()) {

			XSSFRow row = worksheet.getRow(in++);

			logger.info("Cell 0: " + row.getCell(0).getStringCellValue());
			logger.info("Cell 0: " + row.getCell(1).getStringCellValue());
			logger.info("Cell 0: " + row.getCell(2).getNumericCellValue());
			logger.info("Cell 0: " + row.getCell(3).getNumericCellValue());
			logger.info("Cell 0: " + row.getCell(4).getStringCellValue());
			logger.info("Cell 0: " + row.getCell(5).getNumericCellValue());

			LdcMaster ldcMaster = new LdcMaster();
			String deducteePan = row.getCell(6).getStringCellValue();
			ldcMaster.setAssessmentYear(assessmentYear);
			ldcMaster.setPan(deducteePan);
			ldcMaster.setActive(true);
			ldcMaster.setCertificateNumber(row.getCell(0).getStringCellValue());
			ldcMaster.setNatureOfPayment(row.getCell(1).getStringCellValue());
			ldcMaster.setRate(
					new BigDecimal(row.getCell(2).getNumericCellValue()).setScale(4, BigDecimal.ROUND_HALF_DOWN));
			ldcMaster.setUtilizedAmount(
					new BigDecimal(row.getCell(3).getNumericCellValue()).setScale(2, BigDecimal.ROUND_UP));
			ldcMaster.setDeducteeName(row.getCell(4).getStringCellValue());
			ldcMaster.setAmount(new BigDecimal(row.getCell(5).getNumericCellValue()).setScale(2, BigDecimal.ROUND_UP));
			ldcMaster.setApplicableFrom(null);
			ldcMaster.setApplicableTo(null);
			ldcMasterDao.save(ldcMaster);
		}
		workbook.close();
	}

	public void saveExcelData(MultipartFile file, Integer assessmentYear) throws IOException {

		LdcMaster ldcMaster = new LdcMaster();

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

			ldcMaster.setAssessmentYear(assessmentYear);
			ldcMaster.setPan(ldcMasterData.getDeducteePAN());
			ldcMaster.setCertificateNumber(ldcMasterData.getCertificateNumber());
			ldcMaster.setAmount(ldcMasterData.getAmount());
			ldcMaster.setNatureOfPayment(ldcMasterData.getNatureOfPayment());
			ldcMaster.setSection(ldcMasterData.getSection());
			ldcMaster.setRate(ldcMasterData.getRate());
			ldcMaster.setUtilizedAmount(ldcMasterData.getLimitUtilisedAmount());
			try {
				ldcMasterDao.save(ldcMaster);
			} catch (Exception e) {
				logger.error("Error occured at saveExcelData", e);
			}
		}
	}

	public LdcMaster getOneLdcMaste(Integer assesssmentYear, String deductorTan) {
		List<LdcMaster> ldcMasterObj = ldcMasterDao.getLdcByYearAndTan(assesssmentYear, deductorTan);
		if (!ldcMasterObj.isEmpty()) {
			return ldcMasterObj.get(0);
		}
		return new LdcMaster();
	}

	public String getLdcMasterStatus(String deductorTan, String startDate, String endDate) {
		long countValidStatus = 0l;
		long countInValidStatus = 0l;
		long countEmptyStatus = 0l;
		LdcCountDTO dto = null;
		dto = ldcMasterDao.getLdcStatusCount(deductorTan, startDate, endDate);
		if (dto != null) {
			countValidStatus = dto.getValidStatusCount();
			countInValidStatus = dto.getInvalidStatusCount();
			countEmptyStatus = dto.getEmptyStatusCount();
		}
		logger.info("Total ldc master valid status:" + countValidStatus + ", Invalid status: " + countInValidStatus
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

		List<BatchUpload> sha256Record = batchUploadDAO.getSha256Records(sha256Sum);

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
	public BatchUploadResponseDTO saveFileData(MultipartFile multiPartFile, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonthPlusOne, String userName, String tenantId, String deductorPan) throws Exception {

		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);
		if (isAlreadyProcessed(sha256)) { //
			BatchUpload batchUpload = new BatchUpload();
			batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedBy(userName);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setSha256sum(sha256);
			batchUpload.setStatus("Duplicate");
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
			BatchUploadResponseDTO batchUploadResponse = ldcBatchUpload(batchUpload, multiPartFile, deductorTan,
					assesssmentYear, assessmentMonthPlusOne, userName, null, tenantId);
			return batchUploadResponse;
		}

		// checking if file is not duplicate
		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {

			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);

			int headersCount = Excel.getNonEmptyCellsCount(headerRow);

			logger.info("Column header count : ", headersCount);

			if (headersCount != LdcExcel.fieldMappings.size()) {// check the header count
				BatchUpload batchUpload = new BatchUpload();
				batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setStatus("Failed");
				batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setCreatedBy(userName);
				batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
				return ldcBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear, assessmentMonthPlusOne,
						userName, null, tenantId);
			}

			else {
				return processLdc(workbook, multiPartFile, sha256, deductorTan, assesssmentYear, assessmentMonthPlusOne,
						userName, tenantId, deductorPan);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to process ldc master data ", e);
		}
	}

	/**
	 * Thisbatch_upload method is to insert the record into the batch_upload table
	 * 
	 * @param batchUpload
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
	 * @throws ParseException
	 */
	public BatchUploadResponseDTO ldcBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {

		logger.info("batch", batchUpload);
		if (file != null) {
			String errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
			batchUpload.setErrorFilePath(errorFp);
		}

		String path = blob.uploadExcelToBlob(mFile);
		int month = assessmentMonthPlusOne;
		batchUpload.setAssessmentMonth(month);
		if (FilenameUtils.getExtension(mFile.getOriginalFilename()).equalsIgnoreCase("pdf")) {
			batchUpload.setAssessmentYear(assesssmentYear);
			batchUpload.setUploadType(UploadTypes.LDC_PDF.name());
			batchUpload.setDeductorMasterTan(tan);
		} else {
			batchUpload.setAssessmentYear(assesssmentYear);
			batchUpload.setUploadType(UploadTypes.LDC_EXCEL.name());
			batchUpload.setDeductorMasterTan(tan);
		}
		batchUpload.setFileName(mFile.getOriginalFilename());
		batchUpload.setFilePath(path);
		batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		try {
			batchUpload = batchUploadDAO.save(batchUpload);
		} catch (Exception e) {
			logger.error("error while saving  file } " + e);
		}

		return batchUploadDAO.copyToResponseDTO(batchUpload); // value to be change
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
	private BatchUploadResponseDTO processLdc(XSSFWorkbook workbook, MultipartFile uploadedFile, String sha256,
			String deductorTan, Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName,
			String tenantId, String deductorPan) throws Exception {
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessedCount(0);
		batchUpload.setMismatchCount(0L);
		ArrayList<LdcErrorReportCsvDTO> errorList = new ArrayList<>();

		File ldcErrorFile = null;
		try {
			LdcExcel data = new LdcExcel(workbook);

			batchUpload.setSha256sum(sha256);
			batchUpload.setMismatchCount(0L);
			long dataRowsCount = data.getDataRowsCount();
			batchUpload.setRowsCount(dataRowsCount);

			int errorCount = 0;
			int successCount = 0;
			int duplicateCount = 0;
			boolean isDuplicate = false;
			List<LdcMaster> ldcList = new ArrayList<>(); // to be checked

			// feign client for sections
			List<NatureOfPaymentMasterDTO> response = mastersClient.findAll().getBody().getData();
			Set<String> sections = response.parallelStream().map(NatureOfPaymentMasterDTO::getSection).distinct()
					.collect(Collectors.toSet());

			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<LdcErrorReportCsvDTO> errorDTO = null;
				Long count = 0l;
				LdcErrorReportCsvDTO sectionErrorDTO = new LdcErrorReportCsvDTO();
				boolean error = false;
				String reason = "";

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

						LdcMaster ldc = data.get(rowIndex);
						ldc.setTanNumber(deductorTan);
						ldc.setActive(true);
						ldc.setAssessmentYear(assesssmentYear);
						ldc.setCreatedBy(userName);
						ldc.setCreatedDate(new Timestamp(new Date().getTime()));

						if (ldc.getIsResident().equals("Yes")) {
							if (ldc.getIsDividend().equalsIgnoreCase("Yes")) {
								ldc.setDividendProcessing(true);
								count = shareholderMasterResidentialDAO
										.getShareholderBasedOnShareholderPanAndDeductorPan(ldc.getPan().trim(),
												deductorPan);

							} else {
								count = deducteeMasterResidentialDAO.getDeducteeBasedOnDeducteePanAndDeductorPan(
										ldc.getPan().trim(), deductorPan.trim());
								if (!sections.contains(ldc.getSection().trim())) {
									error = true;
									reason = reason + "Section " + ldc.getSection()
											+ " not found in system or Not related to Residence section." + "\n";
								}

							}
						} else {
							if (ldc.getIsDividend().equalsIgnoreCase("Yes")) {
								count = shareholderMasterNonResidentialDAO
										.getShareholderBasedOnShareholderPanAndDeductorPan(ldc.getPan().trim(),
												deductorPan.trim());
								ldc.setDividendProcessing(true);
							} else {
								count = deducteeMasterNonResidentialDAO
										.getDeducteeCountBasedOnDeducteePanAndDeductorPan(deductorPan.trim(),
												ldc.getPan().trim());
								if (!sections.contains(ldc.getSection())) {
									error = true;
									reason = reason + "Section " + ldc.getSection()
											+ " not found in system or Not related to Non Residence section." + "\n";
								}
							}
						}
						logger.info("No of Records Present {}", count);

						if (ldc.getCertificateNumber().length() != 10) {
							error = true;
							reason = reason + "Ldc Certificate Number " + ldc.getCertificateNumber()
									+ " should contain 10 characters." + "\n";
						}

						if (count == 0) {
							error = true;
							reason = reason + "Deductee pan " + ldc.getPan() + " not found in system." + "\n";
						}
						if (ldc.getApplicableFrom() != null && ldc.getApplicableTo() != null) {
							if (!ldc.getApplicableFrom().before(ldc.getApplicableTo())) {
								error = true;
								reason = reason + " applicable from date " + ldc.getApplicableFrom()
										+ "should be less than applicable to date" + "\n";
							}
						}
						if (ldc.getAmount() != null && ldc.getUtilizedAmount() != null) {
							if (ldc.getUtilizedAmount().doubleValue() < 0) {
								error = true;
								reason = reason + "Limit utilized amount" + ldc.getUtilizedAmount()
										+ " should not contain -ve value" + "\n";
							}
							if (ldc.getAmount().doubleValue() < 0) {
								error = true;
								reason = reason + "Amount Consumed" + ldc.getAmount() + " should not contain -ve value"
										+ "\n";
							}
							if (ldc.getUtilizedAmount().doubleValue() > ldc.getAmount().doubleValue()) {
								error = true;
								reason = reason + " Limit utilised amount " + ldc.getUtilizedAmount()
										+ " should not be greater than certificate amount " + ldc.getAmount() + "\n";
							}
						}
						if (!error) {
							ldcList.add(ldc);
						}
						if (error == true && isDuplicate == false) {
							sectionErrorDTO = data.getErrorDTO(rowIndex);
							sectionErrorDTO.setReason(reason);
							errorList.add(sectionErrorDTO);
							++errorCount;
						}

					} catch (Exception e) { // inner catch
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						LdcErrorReportCsvDTO problematicDataError = data.getErrorDTO(rowIndex);
						if (StringUtils.isEmpty(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			} // for 1

			List<LdcMaster> savedLdcList = new ArrayList<>();
			for (LdcMaster ldc : ldcList) {
				if (ldc.getApplicableFrom() != null) {
					ldc.setDbApplicableFrom(ldc.getApplicableFrom());
				}
				if (ldc.getApplicableTo() != null) {
					ldc.setDbApplicableTo(ldc.getApplicableTo());
				}
				if (ldc.getSection() != null) {
					ldc.setDbSection(ldc.getSection());
				}
				if (ldc.getRate() != null) {
					ldc.setRate(ldc.getRate());
					ldc.setDbRate(ldc.getRate());
				}
				BigDecimal limitUtilized = ldc.getUtilizedAmount() != null ? ldc.getUtilizedAmount() : BigDecimal.ZERO;
				ldc.setUtilizedAmount(limitUtilized);
				ldc.setInitialUtilizedAmount(limitUtilized);
				ldc.setThresholdLimit(ldc.getAmount().intValue());
				// remaining amount
				ldc.setRemainingAmount(ldc.getAmount().subtract(limitUtilized));
				if (ldcMasterDao.getLdcBycertificateNoPanTanSection(ldc.getCertificateNumber(), ldc.getPan(),
						ldc.getSection(), deductorTan).isEmpty()) {
					if (StringUtils.isBlank(ldc.getLdcStatus())) {
						ldc.setLdcStatus("");
					}
					LdcMaster savedLdc = ldcMasterDao.save(ldc);
					savedLdcList.add(savedLdc);
					++successCount;
				} else {
					++duplicateCount;
				}
			}

			batchUpload.setSuccessCount((long) savedLdcList.size());
			batchUpload.setProcessed(savedLdcList.size());
			batchUpload.setFailedCount((long) errorCount);
			batchUpload.setProcessedCount(savedLdcList.size());
			batchUpload.setDuplicateCount((long) duplicateCount);
			batchUpload.setStatus("Processed");
			batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedBy(userName);

			if (errorList.size() > 0) {
				ldcErrorFile = errorFileLdcUpload.ldcErrorFile(uploadedFile.getOriginalFilename(), deductorTan,
						deductorPan, errorList, new ArrayList<>(data.getHeaders()));
			}

			// Generate LDC validation file only when entry is saved in DB
			if (!savedLdcList.isEmpty()) {
				// Generating ldc validation file while uploading LDC Master upload
				MultipartFile file = errorFileLdcUpload.generateLdcPanXlsxReport(savedLdcList);
				String panUrl = blob.uploadExcelToBlob(file, tenantId);
				batchUpload.setOtherFileUrl(panUrl);
			}
		} catch (Exception e) { // catch block for outer try
			logger.error("File Reading Error", e);
		}
		return ldcBatchUpload(batchUpload, uploadedFile, deductorTan, assesssmentYear, assessmentMonthPlusOne, userName,
				ldcErrorFile, tenantId);
	}

	public BatchUploadResponseDTO savePdfData(MultipartFile uploadedFile, String sha256, String deductorTan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, String tenantId,
			String deductorPan) throws Exception {

		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessedCount(0);
		batchUpload.setMismatchCount(0L);
		batchUpload.setSha256sum(sha256);

		// converting multipart file to File
		File convFile = new File(uploadedFile.getOriginalFilename());
		convFile.createNewFile();
		try (FileOutputStream fos = new FileOutputStream(convFile)) {
			fos.write(uploadedFile.getBytes());
		}

		List<Integer> listSuccessCount = new ArrayList<Integer>();
		List<Integer> listFailCount = new ArrayList<Integer>();
		List<Integer> totalRecordCount = new ArrayList<Integer>();

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

						LdcMaster ldcMaster = new LdcMaster();
						ldcMaster.setActive(true);
						ldcMaster.setAssessmentYear(assesssmentYear);
						ldcMaster.setTanNumber(deductorTan);
						ldcMaster.setPan(deductorPan);
						ldcMaster.setCreatedBy(userName);
						ldcMaster.setCreatedDate(new Timestamp(new Date().getTime()));
						ldcMaster.setUtilizedAmount(BigDecimal.ZERO);

						int rowIndex = rowIndexHolder.getAndIncrement();
						final AtomicInteger indexHolder = new AtomicInteger();
						// restricting to iterate header row
						if (rowIndex > 0 && rowSize >= rowIndex) {
							row.forEach(cell -> {
								int columnIndex = indexHolder.getAndIncrement();
								if (columnIndex == 1) {
									ldcMaster.setCertificateNumber(cell.getText());
								} else if (columnIndex == 2) {
									ldcMaster.setPan(cell.getText());
								} else if (columnIndex == 3) {
									ldcMaster.setDeducteeName((cell.getText()));
								} else if (columnIndex == 4) {
									ldcMaster.setSection(cell.getText());
								} else if (columnIndex == 5) {
									ldcMaster.setNatureOfPayment(cell.getText());
								} else if (columnIndex == 6) {
									if (StringUtils.isNotBlank(cell.getText())) {
										try {
											ldcMaster.setAmount(
													new BigDecimal((cell.getText())).setScale(4, BigDecimal.ROUND_UP));
										} catch (Exception e) {
											logger.error("error occured while parsing ammount.", e);
										}
									}
								} else if (columnIndex == 7) {
									if (StringUtils.isNotBlank(cell.getText())) {
										try {
											ldcMaster.setRate((new BigDecimal((cell.getText())).setScale(4,
													BigDecimal.ROUND_HALF_DOWN)));
										} catch (Exception e) {
											logger.error("error occured while parsing ammount.", e);
										}
									}
								} else if (columnIndex == 8) {
									try {
										ldcMaster.setApplicableFrom(
												new SimpleDateFormat("dd-MMM-yyyy").parse(cell.getText()));
									} catch (Exception e) {
										logger.error("Error occured while parsing valid from date.", e);
										ldcMaster.setApplicableFrom(new Date());
									}
								} else if (columnIndex == 9) {
									try {
										ldcMaster.setApplicableTo(
												new SimpleDateFormat("dd-MMM-yyyy").parse(cell.getText()));
									} catch (Exception e) {
										logger.error("Error occured while parsing valid till date.", e);
										ldcMaster.setApplicableTo(new Date());
									}
								}

							});// inner forEach

							if (StringUtils.isNotBlank(ldcMaster.getCertificateNumber())
									&& ldcMasterDao
											.getLdcMasterBasedOnCerificateNumber(ldcMaster.getCertificateNumber(),
													ldcMaster.getPan(), ldcMaster.getSection(), deductorTan)
											.isEmpty()) {
								totalRecordCount.add(1); // counting no of records present in table
								try {
									ldcMasterDao.save(ldcMaster);
									listSuccessCount.add(1);
									batchUpload.setStatus("Processed");
								} catch (Exception e) {
									logger.error("error while saving record :" + e);
									listFailCount.add(1);
								}

							} else {
								batchUpload.setStatus("Duplicate");
							}

						} // if block
					}); // outer forEach for
				} // inner for
			} // outer for

		} else {
			batchUpload.setStatus("Incorrect TAN");
		}
		batchUpload.setRowsCount((long) totalRecordCount.size());
		batchUpload.setSuccessCount((long) listSuccessCount.size());
		batchUpload.setFailedCount((long) listFailCount.size());
		batchUpload.setProcessedCount(listSuccessCount.size());
		batchUpload.setDuplicateCount(0L);
		batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
		batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
		batchUpload.setCreatedBy(userName);
		return ldcBatchUpload(batchUpload, uploadedFile, deductorTan, assesssmentYear, assessmentMonthPlusOne, userName,
				null, tenantId);

	}

	@Transactional
	public BatchUploadResponseDTO saveLdcPdfData(MultipartFile multiPartFile, String deductorTan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, String tenantId,
			String deductorPan) throws Exception {

		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);

		if (isAlreadyProcessed(sha256)) {
			BatchUpload batchUpload = new BatchUpload();
			batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedBy(userName);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setSha256sum(sha256);
			batchUpload.setStatus("Duplicate");
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
			BatchUploadResponseDTO batchUploadResponse = ldcBatchUpload(batchUpload, multiPartFile, deductorTan,
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
	public ByteArrayInputStream exportLdcMasterData(String deductorTan, String tenantId) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// List<LdcMaster> listOfLdc = null;
		List<LdcMaster> listOfLdc = null;
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

			row1.createCell(0).setCellValue("LDC Certificate number");
			row1.getCell(0).setCellStyle(style0);
			row1.createCell(1).setCellValue("Deductor TAN");
			row1.getCell(1).setCellStyle(style0);
			row1.createCell(2).setCellValue("Deductee Name");
			row1.getCell(2).setCellStyle(style0);
			row1.createCell(3).setCellValue("Deductee PAN");
			row1.getCell(3).setCellStyle(style0);
			row1.createCell(4).setCellValue("Amount");
			row1.getCell(4).setCellStyle(style0);
			row1.createCell(5).setCellValue("LDC Rate");
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
				// listOfLdc = ldcMasterRepository.getLdcMastersByDeductorPan(deductorTan);
				listOfLdc = ldcMasterDao.getLdcByTan(deductorTan, tenantId);
			} catch (Exception e) {
				logger.error("error while getting Ldc_master data :" + e);
			}
			sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 12));
			int rowNo = 1;
			for (LdcMaster ldcMaster : listOfLdc) {
				XSSFRow row = sheet.createRow(rowNo++);
				row.createCell(0).setCellValue(ldcMaster.getCertificateNumber());
				row.createCell(1).setCellValue(ldcMaster.getTanNumber());
				row.createCell(2).setCellValue(ldcMaster.getDeducteeName());
				row.createCell(3).setCellValue(ldcMaster.getPan());
				row.createCell(4).setCellValue(ldcMaster.getAmount().toString());
				row.createCell(5).setCellValue(ldcMaster.getRate().toString());
				row.createCell(6).setCellValue(ldcMaster.getApplicableFrom());
				row.getCell(6).setCellStyle(style1);
				row.createCell(7).setCellValue(ldcMaster.getApplicableTo());
				row.getCell(7).setCellStyle(style1);
				row.createCell(8).setCellValue(ldcMaster.getLdcStatus());
				row.createCell(9).setCellValue(ldcMaster.getValidationDate());
				row.getCell(9).setCellStyle(style1);
				row.createCell(10).setCellValue(ldcMaster.getUtilizedAmount().toString());
				row.createCell(11).setCellValue(ldcMaster.getTracesUtilizedAmount().toString());
				row.createCell(12).setCellValue(ldcMaster.getSection());
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
	public LdcMaster deactivateLdcCertificate(String ldcPan, String deductorTan, Integer id) {
		List<LdcMaster> ldcMaster = ldcMasterDao.getLdcByDeductorTanPanId(id);
		LdcMaster ldc = new LdcMaster();
		if (!ldcMaster.isEmpty()) {
			ldc = ldcMaster.get(0);
			Double ldcUtilizationAmount = ldcMasterDao.getTotalLdcUtilizationAmount(id, deductorTan);
			logger.info("ldc utilization amount is: {}", ldcUtilizationAmount);
			if (ldcUtilizationAmount <= 0) {
				ldc.setActive(false);
				ldcMasterDao.update(ldc);
			} else {
				throw new CustomException("UTILIZATION AMOUNT NOT EQUAL TO ZERO, CAN'T DEACTIVATE LDC",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return ldc;
	}

	public LdcMaster copyValuesToDTO(LdcResponseDTO entity) {
		LdcMaster dto = new LdcMaster();
		dto.setAmount(entity.getAmount());
		dto.setApplicableFrom(entity.getApplicableFrom());
		dto.setApplicableTo(entity.getApplicableTo());
		dto.setAssessmentYear(entity.getAssessmentYear());
		dto.setCertificateNumber(entity.getCertificateNumber());
		dto.setCreatedBy(entity.getCreatedBy());
		dto.setCreatedDate(entity.getCreatedDate());
		dto.setDbApplicableFrom(entity.getDbApplicableFrom());
		dto.setDbApplicableTo(entity.getDbApplicableTo());
		dto.setDbRate(entity.getDbRate());
		dto.setDbSection(entity.getDbSection());
		dto.setDeducteeName(entity.getDeducteeName());
		dto.setLdcStatus(entity.getLdcStatus());
		dto.setMatchScore(entity.getMatchScore());
		dto.setModifiedBy(entity.getModifiedBy());
		dto.setModifiedDate(entity.getModifiedDate());
		dto.setNameAsPerTraces(entity.getNameAsPerTraces());
		dto.setNatureOfPayment(entity.getNatureOfPayment());
		dto.setPan(entity.getPan());
		dto.setRate(entity.getRate());
		dto.setRemainingAmount(entity.getRemainingAmount());
		dto.setRemarksAsPerTraces(entity.getRemarksAsPerTraces());
		dto.setSection(entity.getSection());
		dto.setTanNumber(entity.getTanNumber());
		dto.setThresholdLimit(entity.getThresholdLimit());
		dto.setTracesUtilizedAmount(entity.getTracesUtilizedAmount());
		dto.setUtilizedAmount(entity.getUtilizedAmount());
		dto.setValidationDate(entity.getValidationDate());

		return dto;
	}

	/**
	 * copies values from dto to entity
	 * 
	 * @param dto
	 * @return
	 */
	public LdcResponseDTO copyToEntity(LdcMaster dto) {
		LdcResponseDTO entity = new LdcResponseDTO();
		entity.setAssessmentYear(dto.getAssessmentYear());
		entity.setId(dto.getLdcMasterID());
		entity.setPan(dto.getPan());
		entity.setTanNumber(dto.getTanNumber());
		entity.setActive(dto.getActive());
		entity.setAmount(dto.getAmount());
		entity.setApplicableFrom(dto.getApplicableFrom());
		entity.setApplicableTo(dto.getApplicableTo());
		entity.setCertificateNumber(dto.getCertificateNumber());
		entity.setCreatedBy(dto.getCreatedBy());
		entity.setCreatedDate(dto.getCreatedDate());
		entity.setDbApplicableFrom(dto.getDbApplicableFrom());
		entity.setDbApplicableTo(dto.getDbApplicableTo());
		entity.setDbRate(dto.getRate());
		entity.setDbSection(dto.getSection());
		entity.setDeducteeName(dto.getDeducteeName());
		entity.setLdcStatus(dto.getLdcStatus());
		entity.setMatchScore(dto.getMatchScore());
		entity.setModifiedBy(dto.getModifiedBy());
		entity.setModifiedDate(dto.getModifiedDate());
		entity.setNameAsPerTraces(dto.getNameAsPerTraces());
		entity.setNatureOfPayment(dto.getNatureOfPayment());
		entity.setRate(dto.getRate());
		entity.setSection(dto.getSection());
		entity.setThresholdLimit(dto.getThresholdLimit());
		entity.setTracesUtilizedAmount(dto.getTracesUtilizedAmount());
		entity.setUtilizedAmount(dto.getUtilizedAmount());
		entity.setValidationDate(dto.getValidationDate());
		entity.setAssessingOfficerDetails(dto.getAssessingOfficerDetails());
		entity.setLdcSection(dto.getLdcSection());
		entity.setIsResident(dto.getIsResident());
		return entity;
	}

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

	public List<EmailNotificationConfiguration> createNotification(
			List<EmailNotificationConfigurationDTO> emailNotificationConfigDTOList, String userName) {
		EmailNotificationConfiguration emailNotificationConfiguration = null;
		List<EmailNotificationConfiguration> insertValueList = new ArrayList<>();
		for (EmailNotificationConfigurationDTO emailNotificationConfig : emailNotificationConfigDTOList) {
			boolean validType = false;
			for (Types configType : EmailNotificationConfiguration.Types.values()) {
				if (configType.name().equalsIgnoreCase(emailNotificationConfig.getType())) {
					validType = true;
				}
			}
			if (!validType) {
				throw new CustomException("Not a valid configuration type : " + emailNotificationConfig.getType());
			}
			if (emailNotificationConfig.getId() == null) {
				emailNotificationConfiguration = new EmailNotificationConfiguration();
				emailNotificationConfiguration.setCreatedBy(userName);
				emailNotificationConfiguration.setCreatedDate(new Timestamp(new Date().getTime()));
				emailNotificationConfiguration.setActive(true);
				emailNotificationConfiguration.setPercentage(emailNotificationConfig.getPercentage());
				emailNotificationConfiguration.setType(emailNotificationConfig.getType().toUpperCase());
				emailNotificationConfiguration = emailNotificationConfigurationDAO.save(emailNotificationConfiguration);
				insertValueList.add(emailNotificationConfiguration);
			} else {

				List<EmailNotificationConfiguration> entity = emailNotificationConfigurationDAO
						.findById(emailNotificationConfig.getId());
				if (!entity.isEmpty()) {
					emailNotificationConfiguration = entity.get(0);
					emailNotificationConfiguration.setModifiedBy(userName);
					emailNotificationConfiguration.setModifiedDate(new Timestamp(new Date().getTime()));
					emailNotificationConfiguration.setPercentage(emailNotificationConfig.getPercentage());
					emailNotificationConfiguration.setType(emailNotificationConfig.getType().toUpperCase());
					emailNotificationConfigurationDAO.update(emailNotificationConfiguration);
					insertValueList.add(emailNotificationConfiguration);
				} else {
					throw new CustomException(
							"No email notification configuration exists for Id: {}" + emailNotificationConfig.getId());
				}
			}

		} // for loop

		return insertValueList;
	}

	public List<EmailNotificationConfigurationDTO> getAllNotification() {
		List<EmailNotificationConfigurationDTO> emailNotificationConfigurationDTOList = new ArrayList<>();
		List<EmailNotificationConfiguration> emailNotificationConfigurationList = emailNotificationConfigurationDAO
				.findAll();
		Collections.sort(emailNotificationConfigurationList, new Comparator<EmailNotificationConfiguration>() {
			@Override
			public int compare(EmailNotificationConfiguration id1, EmailNotificationConfiguration id2) {
				return (id1.getCreatedDate() == null || id2.getCreatedDate() == null) ? 0
						: id2.getCreatedDate().compareTo(id1.getCreatedDate());
			}
		});

		EmailNotificationConfigurationDTO emailNotificationConfigurationDTO = null;
		for (EmailNotificationConfiguration emailNotificationConfiguration : emailNotificationConfigurationList) {
			emailNotificationConfigurationDTO = new EmailNotificationConfigurationDTO();
			emailNotificationConfigurationDTO.setId(emailNotificationConfiguration.getId());
			if (emailNotificationConfiguration.getPercentage() != null) {
				emailNotificationConfigurationDTO.setPercentage(emailNotificationConfiguration.getPercentage());
			} else {
				emailNotificationConfigurationDTO.setPercentage(BigDecimal.ZERO);
			}
			emailNotificationConfigurationDTO.setActive(emailNotificationConfiguration.getActive());
			emailNotificationConfigurationDTO.setType(emailNotificationConfiguration.getType());
			emailNotificationConfigurationDTO.setCreatedBy(emailNotificationConfiguration.getCreatedBy());
			emailNotificationConfigurationDTO.setUpdatedBy(emailNotificationConfiguration.getModifiedBy());
			emailNotificationConfigurationDTO.setCreatedDate(emailNotificationConfiguration.getCreatedDate());
			emailNotificationConfigurationDTO.setUpdatedDate(emailNotificationConfiguration.getModifiedDate());
			emailNotificationConfigurationDTOList.add(emailNotificationConfigurationDTO);
		}

		return emailNotificationConfigurationDTOList;
	}

	/**
	 * 
	 * @param type
	 * @param tenantId
	 * @return
	 */
	public List<EmailNotificationConfiguration> findByType(String type, String tenantId) {
		List<EmailNotificationConfiguration> listEmail = new ArrayList<>();
		listEmail = emailNotificationConfigurationDAO.findByType(type, tenantId);
		if (!listEmail.isEmpty()) {
			return listEmail;
		}
		return listEmail;
	}

	/**
	 * 
	 * @param ldcId
	 * @param deductorTan
	 * @return
	 */
	public LdcMaster extendLdcCertificate(int ldcId, String deductorTan) {
		LdcMaster ldcMaster = ldcMasterDao.getLdcById(ldcId);
		return ldcMasterDao.extendLdcCertificate(ldcId, deductorTan, ldcMaster);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param ldcMasterId
	 * @return
	 */
	public List<LdcUtilization> ldcUtilization(String deductorTan, Integer ldcMasterId) {
		List<LdcUtilization> ldcUtilizationList = new ArrayList<>();
		ldcUtilizationList = ldcUtilizationDAO.findByLdcMaster(deductorTan, ldcMasterId);
		if (!ldcUtilizationList.isEmpty()) {
			return ldcUtilizationList;
		} else {
			return ldcUtilizationList;
		}
	}

	public List<NewLDCMasterTracesDTO> getNewLdcsByTan(String deductorTan) {

		return ldcMasterDao.getNewLdcsByTan(deductorTan);
	}

	public void approveFromTraces(List<NewLDCMasterTracesDTO> newLdcList, String deductorTan, Integer assessmentYear,
			String userName) {
		for (NewLDCMasterTracesDTO ldcMasterDTO : newLdcList) {
			List<LdcMaster> ldcMasterList = ldcMasterDao.getLdcMasterBasedOnCerificateNumber(
					ldcMasterDTO.getLdcCertificateNumber(), ldcMasterDTO.getDeducteePan(),
					ldcMasterDTO.getNatureOfPaymentSection(), deductorTan);
			if (!ldcMasterList.isEmpty()) {
				LdcMaster ldc = ldcMasterList.get(0);
				ldc.setActive(true);
				ldc.setAmount(ldcMasterDTO.getAmount());
				ldc.setApplicableFrom(ldcMasterDTO.getApplicableFrom());
				ldc.setApplicableTo(ldcMasterDTO.getApplicableTo());
				ldc.setModifiedBy(userName);
				ldc.setValidationDate(ldcMasterDTO.getValidationDate());
				ldc.setLdcStatus(ldcMasterDTO.getLdcStatus());
				ldc.setModifiedDate(new Timestamp(new Date().getTime()));
				ldc.setLdcSection(ldcMasterDTO.getLdcSection());
				ldc.setAssessingOfficerDetails(ldcMasterDTO.getAssessingOfficerDetails());
				ldc.setIsDividend(ldcMasterDTO.getIsDividend());
				ldc.setIsResident(ldcMasterDTO.getIsResident());
				ldcMasterDao.update(ldc);
			} else {
				LdcMaster ldcMaster = new LdcMaster();
				ldcMaster.setPan(ldcMasterDTO.getDeducteePan());
				ldcMaster.setTanNumber(deductorTan);
				ldcMaster.setAssessmentYear(assessmentYear);
				ldcMaster.setActive(true);
				ldcMaster.setDeducteeName(ldcMasterDTO.getDeducteeName());
				ldcMaster.setAmount(ldcMasterDTO.getAmount());
				ldcMaster.setApplicableFrom(ldcMasterDTO.getApplicableFrom());
				ldcMaster.setApplicableTo(ldcMasterDTO.getApplicableTo());
				ldcMaster.setCertificateNumber(ldcMasterDTO.getLdcCertificateNumber());
				ldcMaster.setNatureOfPayment(ldcMasterDTO.getNatureOfPayment());
				ldcMaster.setSection(ldcMasterDTO.getNatureOfPaymentSection());
				ldcMaster.setRate(ldcMasterDTO.getLdcRate());
				ldcMaster.setLdcSection(ldcMasterDTO.getLdcSection());
				ldcMaster.setAssessingOfficerDetails(ldcMasterDTO.getAssessingOfficerDetails());
				ldcMaster.setIsDividend(ldcMasterDTO.getIsDividend());
				ldcMaster.setIsResident(ldcMasterDTO.getIsResident());

				ldcMaster.setTracesUtilizedAmount(
						ldcMasterDTO.getTracesUtilizedAmount() != null ? ldcMasterDTO.getTracesUtilizedAmount()
								: BigDecimal.ZERO);
				ldcMaster.setUtilizedAmount(
						ldcMasterDTO.getTracesUtilizedAmount() != null ? ldcMasterDTO.getTracesUtilizedAmount()
								: BigDecimal.ZERO);
				ldcMaster.setInitialUtilizedAmount(
						ldcMasterDTO.getTracesUtilizedAmount() != null ? ldcMasterDTO.getTracesUtilizedAmount()
								: BigDecimal.ZERO);
				ldcMaster.setCreatedBy(userName);
				ldcMaster.setCreatedDate(new Timestamp(new Date().getTime()));
				ldcMaster.setModifiedBy(userName);
				ldcMaster.setModifiedDate(new Timestamp(new Date().getTime()));
				ldcMaster.setLdcStatus(ldcMasterDTO.getLdcStatus());
				ldcMaster.setValidationDate(ldcMasterDTO.getValidationDate());
				ldcMaster.setThresholdLimit(ldcMasterDTO.getAmount().intValue());
				BigDecimal amount = ldcMasterDTO.getAmount() != null ? ldcMasterDTO.getAmount() : BigDecimal.ZERO;
				ldcMaster.setRemainingAmount(amount.subtract(ldcMaster.getUtilizedAmount()));
				ldcMasterDao.save(ldcMaster);
			}
			ldcMasterDao.updateNewLdcApprovalStatus(ldcMasterDTO.getId(), userName);
		}
	}

	public void deleteNewLdcByID(String ldcPan, String deductorTan, Integer id, Integer assesmentYear) {
		try {
			ldcMasterDao.deleteNewLdcByID(deductorTan, id);
			logger.info("new ldc record got deleted successfully with id {}", id);
		} catch (Exception e) {
			logger.error("Exception occured while deleting new ldc record with id " + id + "{}", e.getMessage());
			throw new RuntimeException();
		}
	}

	/**
	 * 
	 * @param deductorTan
	 * @return
	 */
	@Async
	public List<NewLDCMasterTraces> getNewLdcsTracesByTan(String deductorTan, String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		List<NewLDCMasterTraces> ldcList = ldcMasterDao.getListNewLdcsCertificateBasedOnTan(deductorTan);
		logger.info("ldc master list {}", ldcList.size());
		List<NewLDCMasterTraces> existingLdcsList = new ArrayList<>();
		if (!ldcList.isEmpty()) {
			for (NewLDCMasterTraces newLdcMasterTraces : ldcList) {
				List<LdcMaster> listLdc = ldcMasterDao.listLdcMasterCertificate(deductorTan,
						newLdcMasterTraces.getCertificateNumber());
				if (!listLdc.isEmpty()) {
					existingLdcsList.add(newLdcMasterTraces);
				}
			}
		}
		logger.info("existing ldc master list {}", existingLdcsList.size());
		// batch update for is_updated
		if (!existingLdcsList.isEmpty()) {
			ldcMasterDao.batchUpdateForisUpdatedLdcMaster(existingLdcsList);
		}
		return existingLdcsList;
	}

	public CommonDTO<LdcMasterDTO> getListOfLdcByTypeAndResidentialStatus(Integer assessmentYear, String deductorTan,
			String tenantId, String type, String residentailStatus, Pagination pagination, String ldcMasterPan) {

		List<LdcMasterDTO> listLdc = new ArrayList<>();

		List<LdcMaster> ldcList = ldcMasterDao.getLdcByTanByTypeAndResidentialStatus(deductorTan, tenantId, type,
				residentailStatus, pagination, ldcMasterPan);

		logger.info("size of ldc list---{}", ldcList.size());

		Collections.reverse(ldcList);
		for (LdcMaster ldcMaster : ldcList) {
			LdcMasterDTO ldcMasterDTO = new LdcMasterDTO();
			ldcMasterDTO.setAmount(ldcMaster.getAmount());
			ldcMasterDTO.setApplicableFrom(ldcMaster.getApplicableFrom());
			ldcMasterDTO.setApplicableTo(ldcMaster.getApplicableTo());
			ldcMasterDTO.setDeducteeName(ldcMaster.getDeducteeName());
			ldcMasterDTO.setDeductorTan(ldcMaster.getTanNumber());
			ldcMasterDTO.setLdcRate(ldcMaster.getRate());
			ldcMasterDTO.setDeducteePan(ldcMaster.getPan());
			ldcMasterDTO.setLdcCertificateNumber(ldcMaster.getCertificateNumber());
			ldcMasterDTO.setId(ldcMaster.getLdcMasterID());
			ldcMasterDTO.setNatureOfPaymentSection(ldcMaster.getSection());
			ldcMasterDTO.setDbRate(ldcMaster.getDbRate());
			ldcMasterDTO.setDbApplicableFrom(ldcMaster.getDbApplicableFrom());
			ldcMasterDTO.setDbApplicableTo(ldcMaster.getDbApplicableTo());
			ldcMasterDTO.setDbSection(ldcMaster.getDbSection());
			ldcMasterDTO.setLdcStatus(ldcMaster.getLdcStatus() != null ? ldcMaster.getLdcStatus() : "");
			ldcMasterDTO.setValidationDate(ldcMaster.getValidationDate());
			ldcMasterDTO.setCreatedDate(ldcMaster.getCreatedDate());
			ldcMasterDTO.setDividendProcessing(ldcMaster.getDividendProcessing());
			ldcMasterDTO.setIsExtend(ldcMaster.getIsExtend() != null ? ldcMaster.getIsExtend() : false);
			ldcMasterDTO.setLimitUtilised(ldcMaster.getUtilizedAmount());
			ldcMasterDTO.setAssessingOfficerDetails(ldcMaster.getAssessingOfficerDetails());
			ldcMasterDTO.setLdcSection(ldcMaster.getLdcSection());
			ldcMasterDTO.setResidentialStatus(ldcMaster.getIsResident().equals("Yes") ? "NR" : "RES");
			listLdc.add(ldcMasterDTO);
		}
		Long ldcCount = ldcMasterDao.getLdcCountByTanByTypeAndResidentialStatus(deductorTan, tenantId, type,
				residentailStatus);
		PagedData<LdcMasterDTO> pagedData = new PagedData<>(listLdc, listLdc.size(), pagination.getPageNumber(),
				ldcCount.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		CommonDTO<LdcMasterDTO> ldcData = new CommonDTO<>();
		ldcData.setResultsSet(pagedData);
		ldcData.setCount(BigInteger.valueOf(ldcCount));

		return ldcData;
	}

	public List<LdcMasterDTO> getLdcByshareHolderOrDeducteePan(String deductorTan, String tenantId, String ldcPan) {

		List<LdcMasterDTO> listLdc = new ArrayList<>();

		List<LdcMaster> ldcList = ldcMasterDao.getLdcByshareHolderOrDeducteePan(deductorTan, ldcPan);

		logger.info("size of ldc list---{}", ldcList.size());

		for (LdcMaster ldcMaster : ldcList) {
			LdcMasterDTO ldcMasterDTO = new LdcMasterDTO();
			ldcMasterDTO.setAmount(ldcMaster.getAmount());
			ldcMasterDTO.setApplicableFrom(ldcMaster.getApplicableFrom());
			ldcMasterDTO.setApplicableTo(ldcMaster.getApplicableTo());
			ldcMasterDTO.setDeducteeName(ldcMaster.getDeducteeName());
			ldcMasterDTO.setDeductorTan(ldcMaster.getTanNumber());
			ldcMasterDTO.setLdcRate(ldcMaster.getRate());
			ldcMasterDTO.setDeducteePan(ldcMaster.getPan());
			ldcMasterDTO.setLdcCertificateNumber(ldcMaster.getCertificateNumber());
			ldcMasterDTO.setId(ldcMaster.getLdcMasterID());
			ldcMasterDTO.setNatureOfPaymentSection(ldcMaster.getSection());
			ldcMasterDTO.setDbRate(ldcMaster.getDbRate());
			ldcMasterDTO.setDbApplicableFrom(ldcMaster.getDbApplicableFrom());
			ldcMasterDTO.setDbApplicableTo(ldcMaster.getDbApplicableTo());
			ldcMasterDTO.setDbSection(ldcMaster.getDbSection());
			ldcMasterDTO.setLdcStatus(ldcMaster.getLdcStatus() != null ? ldcMaster.getLdcStatus() : "");
			ldcMasterDTO.setValidationDate(ldcMaster.getValidationDate());
			ldcMasterDTO.setCreatedDate(ldcMaster.getCreatedDate());
			ldcMasterDTO.setDividendProcessing(ldcMaster.getDividendProcessing());
			ldcMasterDTO.setIsExtend(ldcMaster.getIsExtend() != null ? ldcMaster.getIsExtend() : false);
			ldcMasterDTO.setLimitUtilised(ldcMaster.getUtilizedAmount());
			ldcMasterDTO.setAssessingOfficerDetails(ldcMaster.getAssessingOfficerDetails());
			ldcMasterDTO.setLdcSection(ldcMaster.getLdcSection());
			ldcMasterDTO.setResidentialStatus(ldcMaster.getIsResident().equals("Yes") ? "NR" : "RES");
			listLdc.add(ldcMasterDTO);
		}

		return listLdc;
	}

	/**
	 * 
	 * @param tenantId
	 * @param deductorTan
	 * @param userName
	 * @param year
	 * @param deductorPan
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InvalidKeyException
	 */
	@Async
	public void newTracesLdcMasterReport(String tenantId, String deductorTan, String userName, int year,
			String deductorPan) throws InvalidKeyException, IOException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		generateNewTracesResport(tenantId, deductorTan, userName, year, deductorPan);
	}

	/**
	 * 
	 * @param tenantId
	 * @param deductorTan
	 * @param userName
	 * @param year
	 * @param deductorPan
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InvalidKeyException
	 */
	private void generateNewTracesResport(String tenantId, String deductorTan, String userName, int year,
			String deductorPan) throws InvalidKeyException, IOException, URISyntaxException, StorageException {

		MultiTenantContext.setTenantId(tenantId);
		String fileName = "new_traces_ldc_master_report";
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		BatchUpload batchUpload = saveBatchUploadReport(deductorTan, tenantId, year, null, 0L,
				UploadTypes.NEW_TRACES_LDC_MASTER_REPORT.name(), "Processing", month, userName, null, fileName);
		List<NewLDCMasterTraces> ldcTracesList = ldcMasterDao.getListNewLdcsCertificateBasedOnTan(deductorTan);
		int count = ldcTracesList.size();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = resourceLoader.getResource("classpath:templates/" + "new_traces_ldc_master_report.xlsx");
		InputStream input = resource.getInputStream();
		try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
			XSSFSheet sheet = wb.getSheetAt(0);
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);

			Font fonts = wb.createFont();
			fonts.setBold(true);

			XSSFCellStyle style = wb.createCellStyle();
			style.setFont(fonts);
			style.setWrapText(true);
			style.setBorderLeft(BorderStyle.MEDIUM);
			style.setBorderTop(BorderStyle.MEDIUM);
			style.setBorderBottom(BorderStyle.MEDIUM);
			style.setBorderRight(BorderStyle.MEDIUM);
			style.setAlignment(HorizontalAlignment.LEFT);
			style.setVerticalAlignment(VerticalAlignment.CENTER);

			Font fonts2 = wb.createFont();
			fonts2.setBold(false);

			XSSFCellStyle style1 = wb.createCellStyle();
			style1.setFont(fonts);
			style1.setWrapText(true);
			style1.setFont(fonts2);
			style1.setLocked(true);

			XSSFCellStyle style2 = (XSSFCellStyle) wb.createCellStyle();
			style2.setFont(fonts);
			style2.setWrapText(true);
			style2.setFont(fonts2);
			style2.setLocked(false);

			String patternDate = "dd/MM/yyyy";
			SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(patternDate);
			int rowindex = 1;
			for (NewLDCMasterTraces traces : ldcTracesList) {
				String fromDate = StringUtils.EMPTY;
				String toDate = StringUtils.EMPTY;
				String dateOfIssue = StringUtils.EMPTY;
				if (traces.getApplicableFrom() != null) {
					fromDate = simpleDateFormat1.format(traces.getApplicableFrom());
				}
				if (traces.getApplicableTo() != null) {
					toDate = simpleDateFormat1.format(traces.getApplicableTo());
				}
				if (traces.getValidationDate() != null) {
					dateOfIssue = simpleDateFormat1.format(traces.getValidationDate());
				}
				XSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));

				createSXSSFCell(style1, row1, 0, traces.getDeductorMasterTan());
				createSXSSFCell(style2, row1, 1, ""); // IsLDCIssuedForDividend
				createSXSSFCell(style2, row1, 2, ""); // IsResident
				createSXSSFCell(style1, row1, 3, traces.getCertificateNumber());
				createSXSSFCell(style1, row1, 4,
						traces.getAssessmentYear() != null ? traces.getAssessmentYear().toString() : "");
				createSXSSFCell(style1, row1, 5, traces.getPan());
				createSXSSFCell(style1, row1, 6, traces.getDeducteeName());
				createSXSSFCell(style2, row1, 7, dateOfIssue); // DateOfIssue
				createSXSSFCell(style2, row1, 8, fromDate); // ValidFrom
				createSXSSFCell(style2, row1, 9, ""); // CancelDate
				createSXSSFCell(style1, row1, 10, toDate); // ValidTo
				createSXSSFCell(style1, row1, 11, traces.getSection()); // TDSSection
				createSXSSFCell(style1, row1, 12, traces.getNatureOfPayment());
				if (traces.getRate() != null) {
					int val = traces.getRate().compareTo(BigDecimal.ZERO);
					createSXSSFCell(style1, row1, 13,
							val == 1 ? traces.getRate().setScale(4, RoundingMode.UP).toString() : "");
				} else {
					createSXSSFCell(style2, row1, 13, "");
				}

				if (traces.getAmount() != null) {
					int val = traces.getAmount().compareTo(BigDecimal.ZERO);
					createSXSSFCell(style1, row1, 14,
							val == 1 ? traces.getAmount().setScale(2, RoundingMode.UP).toString() : "");
				} else {
					createSXSSFCell(style2, row1, 14, "");
				}
				if (traces.getUtilizedAmount() != null) {
					int val = traces.getUtilizedAmount().compareTo(BigDecimal.ZERO);
					createSXSSFCell(style1, row1, 15,
							val == 1 ? traces.getUtilizedAmount().setScale(2, RoundingMode.UP).toString() : "");
				} else {
					createSXSSFCell(style2, row1, 15, "");
				}
				createSXSSFCell(style2, row1, 16, traces.getLdcSection()); // LDCSectionDetails
				createSXSSFCell(style2, row1, 17, traces.getAssessingOfficerDetails()); // AssessingOfficerDetails
				createSXSSFCell(style2, row1, 18, ""); // Approved

			}
			wb.write(out);
			saveBatchUploadReport(deductorTan, tenantId, year, out, Long.valueOf(count),
					UploadTypes.NEW_TRACES_LDC_MASTER_REPORT.name(), "Processed", month, userName,
					batchUpload.getBatchUploadID(), null);
		} catch (Exception e) {
			logger.info("Exception occured while preparing error file {} ", e.getMessage());
		}
	}

	private BatchUpload saveBatchUploadReport(String deductorTan, String tenantId, int assessmentYear,
			ByteArrayOutputStream out, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId, String fileName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		String path = null;
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setSuccessFileUrl(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		batchUpload.setActive(true);
		if (StringUtils.isNotBlank(fileName)) {
			fileName = fileName + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
			batchUpload.setFileName(fileName);
		}
		List<BatchUpload> response = new ArrayList<>();
		if (batchId != null) {
			response = batchUploadDAO.findById(assessmentYear, deductorTan, uploadType, batchId);
		}
		if (!response.isEmpty()) {
			batchUpload = response.get(0);
			if (out != null) {
				File file = getConvertedExcelFile(out.toByteArray(), batchUpload.getFileName());
				path = blob.uploadExcelToBlobWithFile(file, tenantId);
				batchUpload.setStatus(status);
				batchUpload.setFilePath(path);
				batchUpload.setSuccessFileUrl(path);
				batchUpload.setRowsCount(noOfRows);
				batchUpload.setSuccessCount(noOfRows);
				batchUpload.setModifiedBy(userName);
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			} else {
				batchUpload.setModifiedBy(userName);
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchUpload.setRowsCount(0l);
			}
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	/**
	 *
	 * @param byteArray
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	protected File getConvertedExcelFile(byte[] byteArray, String fileName) throws IOException {
		File someFile = new File(fileName);
		try (FileOutputStream fos = new FileOutputStream(someFile)) {
			fos.write(byteArray);
			fos.flush();
			fos.close();
			return someFile;
		}
	}

	/**
	 *
	 * @param style
	 * @param row
	 * @param cellNumber
	 * @param value
	 */
	private void createSXSSFCell(XSSFCellStyle style, XSSFRow row, int cellNumber, String value) {
		row.createCell(cellNumber).setCellValue(StringUtils.isBlank(value) == true ? StringUtils.EMPTY : value);
		row.getCell(cellNumber).setCellStyle(style); // K
	}

	/**
	 * 
	 * @param multiPartFile
	 * @param deductorTan
	 * @param year
	 * @param month
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public BatchUpload newTracesLdcUploadExcel(MultipartFile multiPartFile, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonthPlusOne, String userName, String tenantId, String deductorPan) throws Exception {
		String uploadType = UploadTypes.NEW_TRACES_LDC_MASTER_EXCEL.name();
		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);
		if (isAlreadyProcessed(sha256)) { //
			BatchUpload batchUpload = new BatchUpload();
			batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedBy(userName);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setSha256sum(sha256);
			batchUpload.setUploadType(uploadType);
			batchUpload.setStatus("Duplicate");
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
			BatchUpload batchUploadResponse = deducteeBatchUpload(batchUpload, multiPartFile, deductorTan,
					assesssmentYear, assessmentMonthPlusOne, userName, null, tenantId, uploadType);
			return batchUploadResponse;
		}

		// checking if file is not duplicate
		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {

			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);

			int headersCount = Excel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count : {}", headersCount);
			if (headersCount != TracesLdcMasterExcel.fieldMappings.size()) {// check the header count
				BatchUpload batchUpload = new BatchUpload();
				batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setStatus("Failed");
				batchUpload.setUploadType(uploadType);
				batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setCreatedBy(userName);
				batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
				return deducteeBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonthPlusOne, userName, null, tenantId, uploadType);
			} else {
				return asyncNewTracesLdcReport(workbook, multiPartFile, sha256, deductorTan, assesssmentYear,
						assessmentMonthPlusOne, userName, tenantId, deductorPan, uploadType);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process ldc master data {}", e);
		}
	}

	@Async
	public BatchUpload asyncNewTracesLdcReport(XSSFWorkbook workbook, MultipartFile uploadedFile, String sha256,
			String deductorTan, Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName,
			String tenantId, String deductorPan, String uploadType)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException {

		MultiTenantContext.setTenantId(tenantId);
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessedCount(0);
		batchUpload.setMismatchCount(0L);
		ArrayList<TracesLdcMasterErrorReportDTO> errorList = new ArrayList<>();

		File ldcErrorFile = null;
		try {
			TracesLdcMasterExcel data = new TracesLdcMasterExcel(workbook);

			batchUpload.setSha256sum(sha256);
			batchUpload.setMismatchCount(0L);
			long dataRowsCount = data.getDataRowsCount();
			batchUpload.setRowsCount(dataRowsCount);
			int errorCount = 0;

			List<NewLDCMasterTracesDTO> tracesLdcList = new ArrayList<>();

			List<LdcMaster> ldcList = ldcMasterDao.getLdcByTan(deductorTan, tenantId);

			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<TracesLdcMasterErrorReportDTO> errorDTO = null;
				NewLDCMasterTracesDTO tracesDto = new NewLDCMasterTracesDTO();
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
						NewLDCMasterTraces ldc = data.get(rowIndex);
						if (ldc.getIsApproved() != null && !ldc.getIsApproved().equals(false)) {
							List<NewLDCMasterTraces> ldcTraces = ldcMasterDao.getListNewLdcTracesList(deductorTan,
									ldc.getCertificateNumber(), ldc.getSection(), ldc.getDeducteeName(), ldc.getPan());
							if (!ldcTraces.isEmpty()) {
								tracesDto.setId(ldcTraces.get(0).getNewLdcMasterID());
							}
							tracesDto.setDeductorTan(ldc.getDeductorMasterTan());
							tracesDto.setDeducteePan(ldc.getPan());
							tracesDto.setLdcCertificateNumber(ldc.getCertificateNumber());
							tracesDto.setNatureOfPaymentSection(ldc.getSection());
							tracesDto.setAmount(ldc.getAmount());
							tracesDto.setDeducteeName(ldc.getDeducteeName());
							tracesDto.setNatureOfPayment(ldc.getNatureOfPayment());
							tracesDto.setNatureOfPaymentSection(ldc.getSection());
							tracesDto.setLdcRate(ldc.getRate());
							tracesDto.setTracesUtilizedAmount(ldc.getTracesUtilizedAmount());
							tracesDto.setUtilizedAmount(ldc.getUtilizedAmount());
							tracesDto.setApplicableFrom(ldc.getApplicableFrom());
							tracesDto.setApplicableTo(ldc.getApplicableTo());
							tracesDto.setLdcStatus(ldc.getStatus());
							tracesDto.setValidationDate(ldc.getValidationDate());
							tracesDto.setLdcSection(ldc.getLdcSection());
							tracesDto.setAssessingOfficerDetails(ldc.getAssessingOfficerDetails());
							tracesDto.setIsApproved(ldc.getIsApproved());
							tracesDto.setIsDividend(ldc.getIsLDCIssuedForDividend());
							tracesDto.setIsResident(ldc.getIsResident());

							tracesLdcList.add(tracesDto);
						}

					} catch (Exception e) { // inner catch
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						TracesLdcMasterErrorReportDTO problematicDataError = data.getErrorDTO(rowIndex);
						if (StringUtils.isEmpty(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
						}

					}
				}
			} // for 1

			if (!tracesLdcList.isEmpty()) {
				approveFromTraces(tracesLdcList, deductorTan, assesssmentYear, userName);
			}
			batchUpload.setSuccessCount((long) tracesLdcList.size());
			batchUpload.setProcessed(tracesLdcList.size());
			batchUpload.setFailedCount((long) errorCount);
			batchUpload.setProcessedCount(tracesLdcList.size());
			batchUpload.setDuplicateCount(0L);
			batchUpload.setStatus("Processed");
			batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedBy(userName);

			if (errorList.size() > 0) {
				ldcErrorFile = errorFileLdcUpload.ldcTracesErrorFile(uploadedFile.getOriginalFilename(), deductorTan,
						deductorPan, errorList, new ArrayList<>(data.getHeaders()));
			}

		} catch (Exception e) { // catch block for outer try
			logger.error("File Reading Error", e);
		}

		return deducteeBatchUpload(batchUpload, uploadedFile, deductorTan, assesssmentYear, assessmentMonthPlusOne,
				userName, ldcErrorFile, tenantId, uploadType);

	}

	public BatchUpload deducteeBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant,
			String uploadType) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("batch", batchUpload);
		String errorFp = null;
		String path = null;
		if (file != null) {
			errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
			batchUpload.setErrorFilePath(errorFp);
		}
		if (mFile != null) {
			path = blob.uploadExcelToBlob(mFile);
			batchUpload.setFileName(mFile.getOriginalFilename());
			batchUpload.setFilePath(path);
		}
		int month = assessmentMonthPlusOne;
		batchUpload.setAssessmentMonth(month);
		batchUpload.setAssessmentYear(assesssmentYear);
		batchUpload.setDeductorMasterTan(tan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		if (batchUpload.getBatchUploadID() != null) {
			batchUpload.setBatchUploadID(batchUpload.getBatchUploadID());
			batchUpload.setModifiedBy(userName);
			batchUpload.setModifiedDate(CommonUtil.fileUploadTime(new Date()));
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

}
