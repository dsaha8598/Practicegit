package com.ey.in.tds.onboarding.service.ao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.domain.transactions.jdbc.dao.AoMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.AoUtilizationDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.AoMasterDTO;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.ingestion.response.dto.BatchUploadResponseDTO;
import com.ey.in.tds.common.model.ao.AoExcelErrorDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.AoMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.AoUtilization;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.CommonValidationsCassandra;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.jdbc.dao.DeducteeMasterNonResidentialDAO;
import com.ey.in.tds.jdbc.dao.ShareholderMasterNonResidentialDAO;
import com.ey.in.tds.onboarding.dto.ao.AoLdcTempDTO;
import com.ey.in.tds.onboarding.dto.ao.CustomAoLdcMasterDTO;
import com.ey.in.tds.onboarding.service.ldc.ErrorFileLdcUpload;
import com.ey.in.tds.onboarding.service.util.excel.ao.AoExcel;
import com.ey.in.tds.onboarding.service.util.excel.ldc.LdcExcel;
import com.microsoft.azure.storage.StorageException;
import com.opencsv.CSVReader;

@Service
public class AoMasterService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private AoMasterDAO aoMasterDAO;

	@Autowired
	private AoUtilizationDAO aoUtilizationDAO;

	@Autowired
	BatchUploadDAO batchUploadDAO;

	@Autowired
	MastersClient mastersClient;

	@Autowired
	ErrorFileLdcUpload errorFileLdcUpload;

	@Autowired
	private DeducteeMasterNonResidentialDAO deducteeMasterNonResidentialDAO;

	@Autowired
	private ShareholderMasterNonResidentialDAO shareholderMasterNonResidentialDAO;

	/**
	 * 
	 * @param aoMasterDTO
	 * @return AoMaster saved record
	 */

	public AoMaster create(AoMasterDTO aoMasterDTO, String deductorTan, Integer assesssmentYear, String userName) {
		AoMaster aoMaster = new AoMaster();
		List<AoMaster> listAoMsater = aoMasterDAO
				.getAoBycertificateNoAndDeducteePanSection(aoMasterDTO.getAoCertificateNumber(), aoMasterDTO.getDeducteePan(),
						aoMasterDTO.getNatureOfPaymentSection(), deductorTan);
		if (!listAoMsater.isEmpty()) {
			throw new CustomException(
					"Already record present with Certificate Number, section and Pan Combination, Cannot add duplicate Record",
					HttpStatus.INTERNAL_SERVER_ERROR);
		} else {
			aoMaster.setAssessmentYear(assesssmentYear);
			aoMaster.setPan(aoMasterDTO.getDeducteePan());
			aoMaster.setDeductorMasterTan(deductorTan);

			for (AoMaster ao : listAoMsater) {
				CommonValidationsCassandra.validateApplicableFields(ao.getApplicableTo(),
						aoMasterDTO.getApplicableFrom());
			}

			aoMaster.setPan(aoMasterDTO.getDeducteePan());
			aoMaster.setAmount(aoMasterDTO.getAmount());
			aoMaster.setActive(true);
			aoMaster.setApplicableFrom(aoMasterDTO.getApplicableFrom());
			aoMaster.setApplicableTo(aoMasterDTO.getApplicableTo());
			aoMaster.setCertificateNumber(aoMasterDTO.getAoCertificateNumber());
			BigDecimal limitUtilized = aoMasterDTO.getLimitUtilised() != null ? aoMasterDTO.getLimitUtilised()
					: BigDecimal.ZERO;
			aoMaster.setLimitUtilised(limitUtilized);
			aoMaster.setNatureOfPayment(aoMasterDTO.getNatureOfPaymentSection());
			aoMaster.setSection(aoMasterDTO.getNatureOfPaymentSection());
			aoMaster.setRate(aoMasterDTO.getAoRate());
			aoMaster.setCreatedBy(userName);
			aoMaster.setCreatedDate(new Date());
			aoMaster.setModifiedBy(userName);
			aoMaster.setModifiedDate(new Date());
			aoMaster.setDeducteeName(aoMasterDTO.getDeducteeName());
			aoMaster.setDividendNameOfAssigneeOfficer(aoMasterDTO.getDividendNameOfAssigneeOfficer());
			aoMaster.setDividendProcessing(aoMasterDTO.getDividendProcessing());
			aoMaster = aoMasterDAO.save(aoMaster);
			return aoMaster;
		}
	}

	public AoMaster update(AoMasterDTO aoMasterDTO, String deductorTan, Integer assesssmentYear, String userName) {

		logger.info("REST request to update AoMAster : {}", aoMasterDTO);
		AoMaster ao = aoMasterDAO.getAoById(aoMasterDTO.getId());// TODO DYNAMIC VALUE NEED TO BE ASSIGNED
																	// ldcMasterDTO.getId()
		if (ao != null) {
			ao.setActive(true);
			ao.setAmount(aoMasterDTO.getAmount());
			ao.setApplicableFrom(aoMasterDTO.getApplicableFrom());
			ao.setApplicableTo(aoMasterDTO.getApplicableTo());
			ao.setCertificateNumber(aoMasterDTO.getAoCertificateNumber());
			ao.setModifiedBy(userName);
			ao.setModifiedDate(new Date());
			// return ldcMasterRepository.insert(ldc);
			return aoMasterDAO.update(ao);
		}
		return ao;
	}

	public List<AoMasterDTO> getListOfAo(String deductorTan) {

		List<AoMasterDTO> listAoDTO = new ArrayList<>();

		List<AoMaster> listOfAo = aoMasterDAO.getAoByTan(deductorTan);
		Collections.reverse(listOfAo);

		logger.info("size of ldc list---{}", listOfAo.size());

		for (AoMaster aoMaster : listOfAo) {
			AoMasterDTO aoMasterDTO = new AoMasterDTO();

			aoMasterDTO.setAmount(aoMaster.getAmount());
			aoMasterDTO.setAoCertificateNumber(aoMaster.getCertificateNumber());
			aoMasterDTO.setAoRate(aoMaster.getRate());
			aoMasterDTO.setApplicableFrom(aoMaster.getApplicableFrom());
			aoMasterDTO.setApplicableTo(aoMaster.getApplicableTo());
			aoMasterDTO.setDeducteeName(aoMaster.getDeducteeName());
			aoMasterDTO.setNatureOfPaymentSection(aoMaster.getNatureOfPayment());
			aoMasterDTO.setDeducteePan(aoMaster.getPan());
			aoMasterDTO.setDeductorTan(aoMaster.getDeductorMasterTan());
			aoMasterDTO.setId(aoMaster.getAoMasterId());
			aoMasterDTO.setLimitUtilised(aoMaster.getLimitUtilised());
			aoMasterDTO.setDbAmount(aoMaster.getDbAmount());
			aoMasterDTO.setDbApplicableFrom(aoMaster.getDbApplicableFrom());
			aoMasterDTO.setDbApplicableTo(aoMaster.getDbApplicableTo());
			aoMasterDTO.setDbRate(aoMaster.getDbRate());
			aoMasterDTO.setDbSection(aoMaster.getDbSection());
			aoMasterDTO.setValidationDate(aoMaster.getValidationDate());
			aoMasterDTO.setDividendNameOfAssigneeOfficer(aoMaster.getDividendNameOfAssigneeOfficer());
			aoMasterDTO.setDividendProcessing(aoMaster.getDividendProcessing());

			if (aoMasterDTO.getDividendProcessing() == null || aoMasterDTO.getDividendProcessing().equals(false)) {
				Double aoUtilizationAmount = aoUtilizationDAO.getTotalUtilizationAmount(deductorTan,
						aoMaster.getAoMasterId());

				if (aoUtilizationAmount == null) {
					aoUtilizationAmount = Double.valueOf(0);
				}

				aoMasterDTO.setLimitUtilised(BigDecimal.valueOf(aoUtilizationAmount)
						.add(aoMaster.getLimitUtilised() == null ? new BigDecimal(0) : aoMaster.getLimitUtilised())
						.setScale(2, RoundingMode.UP));
			} else {
				aoMasterDTO.setLimitUtilised(aoMaster.getLimitUtilised());
			}
			listAoDTO.add(aoMasterDTO);
		}

		return listAoDTO;
	}

	public AoMasterDTO getAoMAsterDTO(String deductorTan, String deducteeName, Integer id, Integer assesssmentYear) {
		AoMasterDTO aoMasterDTO = new AoMasterDTO();
		List<AoMaster> response = aoMasterDAO.findBydeducteNameTanId(deductorTan, deducteeName, id);
		if (!response.isEmpty()) {
			AoMaster ao = response.get(0);
			aoMasterDTO.setId(ao.getAoMasterId());
			aoMasterDTO.setAoCertificateNumber(ao.getCertificateNumber());
			aoMasterDTO.setDeducteeName(ao.getDeducteeName());
			aoMasterDTO.setDeducteePan(ao.getPan());
			aoMasterDTO.setDeductorTan(ao.getDeductorMasterTan());
			aoMasterDTO.setNatureOfPaymentSection(ao.getNatureOfPayment());
			aoMasterDTO.setAmount(ao.getAmount());
			aoMasterDTO.setAoRate(ao.getRate());
			aoMasterDTO.setLimitUtilised(ao.getLimitUtilised());
			aoMasterDTO.setApplicableFrom(ao.getApplicableFrom());
			aoMasterDTO.setApplicableTo(ao.getApplicableTo());
			aoMasterDTO.setDividendNameOfAssigneeOfficer(ao.getDividendNameOfAssigneeOfficer());
			aoMasterDTO.setDividendProcessing(ao.getDividendProcessing());
		}
		return aoMasterDTO;
	}

	public ByteArrayInputStream exportToExcel() throws IOException {

		String[] COLUMNs = { "Certification Number", "Deductee Name", "Amount", "Limit Utilised", "Rate",
				"Assessment Year", "Nature Of Payment" };
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			// CreationHelper createHelper = workbook.getCreationHelper();

			Sheet sheet = workbook.createSheet("AO_Master");
			sheet.setDefaultColumnWidth(14);
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setColor(IndexedColors.BLUE.getIndex());
			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			// Row for Header
			Row headerRow = sheet.createRow(0);
			// Header
			for (int col = 0; col < COLUMNs.length; col++) {
				Cell cell = headerRow.createCell(col);
				cell.setCellValue(COLUMNs[col]);
				cell.setCellStyle(headerCellStyle);
			}
			List<AoMaster> aoMasterFindAll = aoMasterDAO.findAll();

			CellStyle cellStyle = workbook.createCellStyle();
			int rowIdx = 1;
			for (AoMaster ao : aoMasterFindAll) {
				Row row = sheet.createRow(rowIdx++);
				row.createCell(0).setCellValue(ao.getCertificateNumber());
				row.createCell(1).setCellValue(ao.getDeducteeName());
				if (ao.getAmount() != null) {
					row.createCell(2).setCellValue(ao.getAmount().doubleValue());
				}
				if (ao.getLimitUtilised() != null) {
					row.createCell(3).setCellValue(ao.getLimitUtilised().doubleValue());
				}
				if (ao.getRate() != null) {
					row.createCell(4).setCellValue(ao.getRate().doubleValue());
				}
				row.createCell(5).setCellValue(ao.getAssessmentYear());
				row.createCell(6).setCellValue(ao.getNatureOfPayment());

				Cell cell = row.createCell(7);
				cell.setCellStyle(cellStyle);
			}
			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());
		}
	}

	/**
	 * To save the excel file in Batch Upload server
	 * 
	 * @param file
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 */
	// TODO NEED TO CHANGE FOR SQL
	/*
	 * public BatchUpload saveToBatchUploadExcel(MultipartFile file, String tan,
	 * Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName)
	 * throws InvalidKeyException, URISyntaxException, StorageException, IOException
	 * {
	 * 
	 * BatchUpload batchUpload = null; String path = blob.uploadExcelToBlob(file);
	 * if (file.isEmpty()) { throw new
	 * FileStorageException("Please select the file"); } else if
	 * (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase(
	 * "xlsx")) { throw new FileStorageException("Cannot upload this file (" +
	 * file.getOriginalFilename() + ") as this type of file is not accepted"); }
	 * else { String sha256 = sha256SumService.getSHA256Hash(file);
	 * List<BatchUpload> batch =
	 * batchUploadRepository.getSha256RecordsBasedonYearMonth(assesssmentYear,
	 * assessmentMonthPlusOne, "AO_EXCEL", sha256, Pagination.DEFAULT).getData();
	 * 
	 * if (batch.isEmpty()) {
	 * 
	 * batchUpload = new BatchUpload(); // batchUpload.setKey(new
	 * BatchUpload.Key(assesssmentYear, // assessmentMonthPlusOne, "AO_EXCEL",
	 * "Uploaded", sha256, // UUID.randomUUID())); batchUpload.setKey(new
	 * BatchUpload.Key(assesssmentYear, tan, UploadTypes.AO_EXCEL.name(),
	 * UUID.randomUUID())); batchUpload.setStatus("Uploaded"); } else { batchUpload
	 * = new BatchUpload(); // batchUpload.setKey(new
	 * BatchUpload.Key(assesssmentYear, // assessmentMonthPlusOne, "AO_EXCEL",
	 * "Duplicate", sha256, // UUID.randomUUID())); batchUpload.setKey(new
	 * BatchUpload.Key(assesssmentYear, tan, "AO_EXCEL", UUID.randomUUID()));
	 * batchUpload.setStatus("Duplicate");
	 * batchUpload.setReferenceId(batch.get(0).getKey().getId()); }
	 * batchUpload.setFileName(file.getOriginalFilename());
	 * batchUpload.setFilePath(path); batchUpload.setCreatedDate(new Date());
	 * batchUpload.setActive(true); batchUpload.setMismatchCount(0L);
	 * batchUpload.setDuplicateCount(0L); batchUpload.setFailedCount(0L);
	 * batchUpload.setCreatedBy(userName); batchUpload.setModifiedBy(userName);
	 * batchUpload.setModifiedDate(new Date()); batchUpload =
	 * batchUploadRepository.save(batchUpload);
	 * 
	 * } return batchUpload; }
	 */

	/**
	 * 
	 * @param files
	 * @return
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	// TODO NEED TO CHANGE FOR SQL
	/*
	 * public BatchUpload saveToBatchUploadPdf(MultipartFile[] files, String tan,
	 * Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName)
	 * throws IOException, InvalidKeyException, URISyntaxException, StorageException
	 * { BatchUpload batchUpload = null;
	 * 
	 * if (files.length == 0) { throw new
	 * FileStorageException("Please select atleast One file"); } else { for (int i =
	 * 0; i < files.length; i++) { logger.info("i-----------------------" + i); if
	 * (!files[i].getOriginalFilename().split("\\.")[1].equalsIgnoreCase("pdf")) {
	 * throw new FileStorageException("Cannot upload this file (" +
	 * files[i].getOriginalFilename() + ") as this type of file is not accepted"); }
	 * else { String sha256 = sha256SumService.getSHA256Hash(files[i]);
	 * logger.info("sha 256---------" + sha256); logger.info("File Name---" +
	 * files[i].getOriginalFilename()); List<BatchUpload> batch =
	 * batchUploadRepository.getSha256RecordsBasedonYearMonth(assesssmentYear,
	 * assessmentMonthPlusOne, UploadTypes.AO_PDF.name(), sha256,
	 * Pagination.DEFAULT).getData(); logger.info("BatchUpload----" + batch); String
	 * path = blob.uploadExcelToBlob(files[i]); batchUpload = new BatchUpload(); if
	 * (batch.isEmpty()) {
	 * 
	 * batchUpload.setKey(new BatchUpload.Key(assesssmentYear, tan,
	 * UploadTypes.AO_PDF.name(), UUID.randomUUID()));
	 * batchUpload.setStatus("Uploaded"); } else { batchUpload.setKey(new
	 * BatchUpload.Key(assesssmentYear, tan, UploadTypes.AO_PDF.name(),
	 * UUID.randomUUID())); batchUpload.setStatus("Duplicate");
	 * batchUpload.setReferenceId(batch.get(0).getKey().getId()); }
	 * 
	 * batchUpload.setFileName(files[i].getOriginalFilename());
	 * batchUpload.setFilePath(path); batchUpload.setCreatedDate(new Date());
	 * batchUpload.setCreatedBy(userName); batchUpload.setModifiedDate(new Date());
	 * batchUpload.setModifiedBy(userName); batchUpload.setActive(true);
	 * batchUpload.setSuccessCount(0L); batchUpload.setFailedCount(0L);
	 * batchUpload.setRowsCount(0L); batchUpload.setDuplicateCount(0L);
	 * batchUpload.setMismatchCount(0L); // Need to set Duplicate column and
	 * Mismatch Column batchUpload = batchUploadRepository.save(batchUpload);
	 * 
	 * // TODO:Send Data to Python api // Send } } }
	 * 
	 * return batchUpload; }
	 */

	// TODO NEED TO CHANGE FOR SQL
	/*
	 * public BatchUpload saveCsvDataToDb(AoLdcCustomDTO aoLdcCustomDTO, String tan)
	 * throws IOException {
	 * logger.info("REST request to get Batch Upload details : {}", aoLdcCustomDTO);
	 * 
	 * // Optional<BatchUpload> BatchUpload = batchUploadRepository.findById(new
	 * BatchUpload.Key( // aoLdcCustomDTO.getAssessmentYear(),
	 * aoLdcCustomDTO.getAssessmentMonth(), aoLdcCustomDTO.getUploadType(), //
	 * aoLdcCustomDTO.getStatus(), aoLdcCustomDTO.getSha256sum(),
	 * aoLdcCustomDTO.getId()));
	 * 
	 * Optional<BatchUpload> BatchUpload = batchUploadRepository.findById(new
	 * BatchUpload.Key( aoLdcCustomDTO.getAssessmentYear(), tan,
	 * aoLdcCustomDTO.getUploadType(), aoLdcCustomDTO.getId()));
	 * 
	 * if (BatchUpload.isPresent()) { saveData(BatchUpload.get().getFilePath(),
	 * aoLdcCustomDTO.getId()); }
	 * 
	 * // by ao ldc KEY get batch upload data // get file url of blob storage // get
	 * excel from blob storage by url // // save data in db of excel which is in
	 * blob storage //
	 * 
	 * return null; }
	 */
	/*
	 * Method to convert multipartfile to file
	 */
//	public File convert(MultipartFile file) throws IOException {
//		File convFile = new File(file.getOriginalFilename());
//		convFile.createNewFile();
//		FileOutputStream fos = new FileOutputStream(convFile);
//		fos.write(file.getBytes());
//		fos.close();
//		return convFile;
//	}

	public void saveData(String filePath, UUID uuid) throws IOException {

		// convert file to string
		// String content = new String(file.getBytes());
		// ByteArrayInputStream stream = new
		// ByteArrayInputStream(multipartFile.getBytes());
		// String files = IOUtils.toString(stream, "UTF-8");

		// Java code to illustrate reading a
		// CSV file line by line

		/*
		 * Read from blob storage and convert to file
		 */

		File file = new File(filePath);

		try (FileReader filereader = new FileReader(file)) {

			// Create an object of filereader
			// class with CSV file as a parameter.

			// create csvReader object passing
			// file reader as a parameter
			try (CSVReader csvReader = new CSVReader(filereader)) {
				String[] nextRecord;
				List<AoLdcTempDTO> aoLdcValidationList = new ArrayList<>();
				int iteration = 0;

				// we are going to read data line by line
				while ((nextRecord = csvReader.readNext()) != null) {

					if (iteration == 0) {
						iteration++;
						continue;
					}

					AoLdcTempDTO AoLdcTempDTO = createEachRecord(nextRecord, uuid);
					logger.info(AoLdcTempDTO.toString());

					aoLdcValidationList.add(AoLdcTempDTO);

					// System.out.print(cell + "\t");
				}
				csvReader.close();
				saveRecord(aoLdcValidationList);
			}
		} catch (Exception e) {
			logger.error("Error occured at saveData", e);
		}
	}

	private void saveRecord(List<AoLdcTempDTO> aoLdcValidationList) throws ParseException {

	}

	@SuppressWarnings("unused")
	private Date convertDate(String date) throws ParseException {
		DateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
		Date convertDate = format.parse(date);
		return convertDate;
	}

	private AoLdcTempDTO createEachRecord(String[] cell, UUID uuid) {

		String certificateNumber = cell[0];
		String tanOrpan = cell[1];
		String tanOrPanName = cell[2];
		String section = cell[3];
		String amount = cell[4];
		String certificateRate = cell[5];
		String validFromdate = cell[6];
		String validTillDate = cell[7];

		return new AoLdcTempDTO(uuid, certificateNumber, tanOrpan, tanOrPanName, section, amount, certificateRate,
				validFromdate, validTillDate);
	}

	// send object to angular with pdf url and ao master data
	public CustomAoLdcMasterDTO getAoMasterPdfData(UUID id) {

		// fetch data from batch upload details
		// Key(id));
		// fetch batch upload data and send filepath url to angular

		// AoMasterTemp AoMasterTemp = aoMasterTempRepository.findById(new Key(id));

		// with reference batch upload UUID retrieve data from temp table and send it to
		// angular

		return null;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public AoMasterDTO getAoMasterBasedOnId(String deducteeName, Integer id) {
		AoMaster aoMasterObj = aoMasterDAO.getAoById(id);
		AoMasterDTO aoMasterDTO = new AoMasterDTO();
		if (aoMasterObj != null) {
			BeanUtils.copyProperties(aoMasterObj, aoMasterDTO);
			aoMasterDTO.setDeductorTan(aoMasterObj.getDeductorMasterTan());
			aoMasterDTO.setId(aoMasterObj.getAoMasterId());
			aoMasterDTO.setDeducteeName(aoMasterObj.getDeducteeName());
			aoMasterDTO.setAssessmentYear(aoMasterObj.getAssessmentYear());
			aoMasterDTO.setNatureOfPaymentSection(aoMasterObj.getNatureOfPayment());
			aoMasterDTO.setDbSection(aoMasterObj.getNatureOfPayment());
			aoMasterDTO.setDividendNameOfAssigneeOfficer(aoMasterObj.getDividendNameOfAssigneeOfficer());
			aoMasterDTO.setDividendProcessing(aoMasterObj.getDividendProcessing());
		}
		return aoMasterDTO;
	}

	/**
	 * copies values from dto to entity
	 * 
	 * @param dto
	 * @return
	 */
	public AoMasterDTO copyToEntity(AoMaster dto) {
		AoMasterDTO entity = new AoMasterDTO();
		entity.setAoCertificateNumber(dto.getCertificateNumber());
		entity.setDeductorTan(dto.getDeductorMasterTan());
		entity.setDeducteeName(dto.getDeducteeName());
		entity.setNatureOfPaymentSection(dto.getNatureOfPayment());
		entity.setAmount(dto.getAmount());
		entity.setAoRate(dto.getRate());
		entity.setLimitUtilised(dto.getLimitUtilised());
		entity.setApplicableFrom(dto.getApplicableFrom());
		entity.setApplicableTo(dto.getApplicableTo());
		entity.setDbSection(dto.getDbSection());
		entity.setDbAmount(dto.getDbAmount());
		entity.setDbRate(dto.getDbRate());
		entity.setDbApplicableFrom(dto.getDbApplicableFrom());
		entity.setDbApplicableTo(dto.getDbApplicableTo());
		entity.setValidationDate(dto.getValidationDate());
		entity.setAssessmentYear(dto.getAssessmentYear());
		return entity;
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
		if (isAlreadyProcessed(sha256)) {
			BatchUpload batchUpload = new BatchUpload();
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setSha256sum(sha256);
			batchUpload.setStatus("Duplicate");
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			BatchUploadResponseDTO batchUploadResponse = aoBatchUpload(batchUpload, multiPartFile, deductorTan,
					assesssmentYear, assessmentMonthPlusOne, userName, null, tenantId);
			return batchUploadResponse;
		}

		// checking if file is not duplicate
		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {

			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);

			int headersCount = Excel.getNonEmptyCellsCount(headerRow);

			logger.info("Column header count : ", headersCount);

			if (headersCount != AoExcel.fieldMappings.size()) {// check the header count
				BatchUpload batchUpload = new BatchUpload();
				batchUpload.setProcessStartTime(new Date());
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setStatus("Failed");
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setCreatedBy(userName);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return aoBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear, assessmentMonthPlusOne,
						userName, null, tenantId);
			}

			else {
				return processAo(workbook, multiPartFile, sha256, deductorTan, assesssmentYear, assessmentMonthPlusOne,
						userName, tenantId, deductorPan);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to process ao master data ", e);
		}
	}

	/**
	 * checks with sha256Sum whether it is duplicate or not
	 * 
	 * @param sha256Sum
	 * @return
	 */
	private boolean isAlreadyProcessed(String sha256Sum) {

		List<BatchUpload> sha256Record = batchUploadDAO.getSha256Records(sha256Sum);

		return sha256Record != null && !sha256Record.isEmpty();
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
	 */
	public BatchUploadResponseDTO aoBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {

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
			batchUpload.setUploadType(UploadTypes.AO_PDF.name());
			batchUpload.setDeductorMasterTan(tan);
		} else {
			batchUpload.setAssessmentYear(assesssmentYear);
			batchUpload.setUploadType(UploadTypes.AO_EXCEL.name());
			batchUpload.setDeductorMasterTan(tan);
		}
		batchUpload.setFileName(mFile.getOriginalFilename());
		batchUpload.setFilePath(path);
		batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		try {
			batchUpload = batchUploadDAO.save(batchUpload);
		} catch (Exception e) {
			logger.error("error while saving  file } " + e);
		}

		return batchUploadDAO.copyToResponseDTO(batchUpload);
	}

	private BatchUploadResponseDTO processAo(XSSFWorkbook workbook, MultipartFile uploadedFile, String sha256,
			String deductorTan, Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName,
			String tenantId, String deductorPan) throws Exception {
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessedCount(0);
		batchUpload.setMismatchCount(0L);
		ArrayList<AoExcelErrorDTO> errorList = new ArrayList<>();

		File aoErrorFile = null;
		try {
			AoExcel data = new AoExcel(workbook);

			batchUpload.setSha256sum(sha256);
			batchUpload.setMismatchCount(0L);
			long dataRowsCount = data.getDataRowsCount();
			batchUpload.setRowsCount(dataRowsCount);

			int errorCount = 0;
			int successCount = 0;
			int duplicateCount = 0;
			boolean isDuplicate = false;
			List<AoMaster> aoList = new ArrayList<>();
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<AoExcelErrorDTO> errorDTO = null;
				Long count = 0l;

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
						AoMaster ao = data.get(rowIndex);
						AoExcelErrorDTO sectionErrorDTO = new AoExcelErrorDTO();
						boolean error = false;
						String reason = "";
						
						ao.setDeductorMasterTan(deductorTan);
						ao.setActive(true);
						ao.setAssessmentYear(assesssmentYear);
						ao.setCreatedBy(userName);
						ao.setCreatedDate(new Timestamp(new Date().getTime()));
						// feign client for sections
						List<NatureOfPaymentMasterDTO> response = mastersClient.findAll().getBody().getData();
						Set<String> sections = response.parallelStream().map(NatureOfPaymentMasterDTO::getSection)
								.distinct().collect(Collectors.toSet());

						if (ao.getIsDividend().equalsIgnoreCase("Yes")) {
							count = shareholderMasterNonResidentialDAO
									.getShareholderBasedOnShareholderPanAndDeductorPan(
											ao.getPan().trim(),deductorPan.trim());
							ao.setDividendProcessing(true);
						} else {
							count = deducteeMasterNonResidentialDAO.getDeducteeCountBasedOnDeducteePanAndDeductorPan(
									deductorPan.trim(), ao.getPan().trim());
							if (!sections.contains(ao.getSection())) {
								error = true;
								reason = reason + "Section " + ao.getSection()
										+ " not found in system or Not related to Non Residence section." + "\n";
							}
						}
						logger.info("No of Records Present {}", count);

						

						if (ao.getCertificateNumber().length() != 10) {
							error = true;
							reason = reason + "AO Certificate Number " + ao.getCertificateNumber()
									+ " should contain 10 characters." + "\n";
						}

						

						if (count == 0) {
							error = true;
							reason = reason + "Deductee pan " + ao.getPan() + " not found in system." + "\n";
						}
						if (ao.getApplicableFrom() != null && ao.getApplicableTo() != null) {
							if (!ao.getApplicableFrom().before(ao.getApplicableTo())) {
								error = true;
								reason = reason + " applicable from date " + ao.getApplicableFrom()
										+ "should be less than applicable to date" + "\n";
							}
						}
						if (ao.getAmount() != null && ao.getLimitUtilised() != null) {
							if (ao.getLimitUtilised().doubleValue() < 0) {
								error = true;
								reason = reason + "Limit utilized amount" + ao.getLimitUtilised()
										+ " should not contain -ve value" + "\n";
							}
							if (ao.getAmount().doubleValue() < 0) {
								error = true;
								reason = reason + "Amount Consumed" + ao.getAmount() + " should not contain -ve value"
										+ "\n";
							}
							if (ao.getLimitUtilised().doubleValue() > ao.getAmount().doubleValue()) {
								error = true;
								reason = reason + " Limit utilised amount " + ao.getLimitUtilised()
										+ " should not be greater than certificate amount " + ao.getAmount() + "\n";
							}
						}
						if (!error) {
							aoList.add(ao);
						}
						if (error == true && isDuplicate == false) {
							sectionErrorDTO = data.getErrorDTO(rowIndex);
							sectionErrorDTO.setReason(reason);
							errorList.add(sectionErrorDTO);
							++errorCount;
						}

					} catch (Exception e) { // inner catch
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						AoExcelErrorDTO problematicDataError = data.getErrorDTO(rowIndex);
						if (StringUtils.isEmpty(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			} // for 1

			List<AoMaster> savedAoList = new ArrayList<>();
			for (AoMaster ao : aoList) {
				if (ao.getApplicableFrom() != null) {
					ao.setDbApplicableFrom(ao.getApplicableFrom());
				}
				if (ao.getApplicableTo() != null) {
					ao.setDbApplicableTo(ao.getApplicableTo());
				}
				if (ao.getSection() != null) {
					ao.setDbSection(ao.getSection());
				}
				if (ao.getRate() != null) {
					ao.setRate(ao.getRate());
					ao.setDbRate(ao.getRate());
				}
				BigDecimal limitUtilized = ao.getLimitUtilised() != null ? ao.getLimitUtilised() : BigDecimal.ZERO;
				ao.setLimitUtilised(limitUtilized);
				ao.setThresholdLimit(ao.getAmount().intValue());
				if (aoMasterDAO.getAoBycertificateNoPanTanSection(ao.getCertificateNumber(), ao.getPan(),
						ao.getSection(), deductorTan).isEmpty()) {

					AoMaster savedAo = aoMasterDAO.save(ao);
					savedAoList.add(savedAo);
					++successCount;
				} else {
					++duplicateCount;
				}
			}

			batchUpload.setSuccessCount((long) successCount);
			batchUpload.setFailedCount((long) errorCount);
			batchUpload.setProcessedCount(successCount);
			batchUpload.setDuplicateCount((long) duplicateCount);
			batchUpload.setStatus("Processed");
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);

			if (errorList.size() > 0) {
				aoErrorFile = errorFileLdcUpload.aoErrorFile(uploadedFile.getOriginalFilename(), deductorTan,
						deductorPan, errorList, new ArrayList<>(data.getHeaders()));
			}

		} catch (Exception e) { // catch block for outer try
			logger.error("AO File Reading Error", e);
		}
		return aoBatchUpload(batchUpload, uploadedFile, deductorTan, assesssmentYear, assessmentMonthPlusOne, userName,
				aoErrorFile, tenantId);
	}

	public List<AoUtilization> aoUtilization(String deductorTan, Integer ldcMasterId) {
		List<AoUtilization> ldcUtilizationList = new ArrayList<>();
		ldcUtilizationList = aoUtilizationDAO.findByAoMaster(deductorTan, ldcMasterId);
		if (!ldcUtilizationList.isEmpty()) {
			return ldcUtilizationList;
		} else {
			return ldcUtilizationList;
		}
	}

}
