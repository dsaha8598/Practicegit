package com.ey.in.tds.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.domain.MasterTdsHsnCode;
import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.TdsHsnCodeDTO;
import com.ey.in.tds.common.repository.NatureOfPaymentMasterRepository;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.repository.MasterBatchUploadRepository;
import com.ey.in.tds.repository.TdsHsnCodeRepository;
import com.ey.in.tds.service.util.excel.TdsHsnCodeExcel;
import com.microsoft.azure.storage.StorageException;

/**
 * 
 * @author vamsir
 *
 */
@Service
public class TdsHsnService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TdsHsnCodeRepository tdsHsnCodeRepository;

	@Autowired
	private NatureOfPaymentMasterRepository natureOfPaymentMasterRepository;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private MasterBatchUploadRepository masterBatchUploadRepository;

	@Autowired
	private MasterBatchUploadService masterBatchUploadService;
	
	@Autowired 
	private TdsHsnBulkService tdsHsnBulkService;
	

	@Transactional
	public MasterBatchUpload saveFileData(MultipartFile file, Integer assesssmentYear, Integer assessmentMonth,
			String userName) throws IOException, InvalidKeyException, URISyntaxException, StorageException {

		String uploadType = UploadTypes.TDS_HSN_CODE_EXCEL.name();

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
			int headersCount = TdsHsnCodeExcel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			if (headersCount != TdsHsnCodeExcel.fieldMappings.size()) {
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
			if (headersCount == TdsHsnCodeExcel.fieldMappings.size()) {
				return tdsHsnBulkService.saveTdsHsnCode(workbook, file, sha256, assesssmentYear, assessmentMonth, userName,
						masterBatchUpload, uploadType);
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process article master data ", e);
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


	/**
	 * 
	 * @return
	 */
	public CommonDTO<MasterTdsHsnCode> getAllHsnSac(Pagination pagination) {
		List<MasterTdsHsnCode> sacs = new ArrayList<>();
		Long listHsnCodeCount = tdsHsnCodeRepository.count();
		Pageable pageable = PageRequest.of(pagination.getPageNumber() - 1, pagination.getPageSize(),
				Sort.by("id").descending());
		Page<MasterTdsHsnCode> listHsnCodeWithPagination = tdsHsnCodeRepository.findAll(pageable);
		if (listHsnCodeCount > 0) {
			for (MasterTdsHsnCode hsnCode : listHsnCodeWithPagination.getContent()) {
				MasterTdsHsnCode hsnSacDTO = new MasterTdsHsnCode();
				hsnSacDTO.setId(hsnCode.getId());
				hsnSacDTO.setHsnCode(hsnCode.getHsnCode());
				hsnSacDTO.setDescription(hsnCode.getDescription());
				hsnSacDTO.setNatureOfPayment(hsnCode.getNatureOfPayment());
				hsnSacDTO.setTdsSection(hsnCode.getTdsSection());
				sacs.add(hsnSacDTO);
			}
		}
		PagedData<MasterTdsHsnCode> pagedData = new PagedData<>(sacs, sacs.size(), pagination.getPageNumber(),
				listHsnCodeCount > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		CommonDTO<MasterTdsHsnCode> commonDTO = new CommonDTO<>();
		commonDTO.setResultsSet(pagedData);
		commonDTO.setCount(BigInteger.valueOf(listHsnCodeCount));
		logger.info("Retrieved data : {}", sacs);

		return commonDTO;
	}

	/**
	 * 
	 * @param hsn
	 * @return
	 */
	public List<MasterTdsHsnCode> findHSNRateDetails(Long hsn) {
		List<MasterTdsHsnCode> response = tdsHsnCodeRepository.findHSNRateDetails(hsn);
		return response;
	}

	/**
	 * 
	 * @return
	 */
	public List<MasterTdsHsnCode> getAllHsnDetalis() {
		List<MasterTdsHsnCode> response = tdsHsnCodeRepository.findAll();
		return response;
	}

	/**
	 * 
	 * @param tdsSection
	 * @return
	 */
	public List<MasterTdsHsnCode> getAllHsnByTdsSeciton(String tdsSection) {
		List<MasterTdsHsnCode> response = tdsHsnCodeRepository.getAllHsnByTdsSection(tdsSection);
		return response;
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getAllTdsSection() {
		return tdsHsnCodeRepository.getAllTdsSection();
	}

	/**
	 * 
	 * @param tdsHsnCode
	 * @param deductorPan
	 * @param tan
	 * @param userName
	 * @return
	 */
	public MasterTdsHsnCode createHsnCode(TdsHsnCodeDTO tdsHsnCode, String userName) {
		MasterTdsHsnCode hsn = new MasterTdsHsnCode();
		hsn.setHsnCode(tdsHsnCode.getHsnCode());
		hsn.setNatureOfPayment(tdsHsnCode.getNatureOfPayment());
		hsn.setDescription(tdsHsnCode.getDescription());
		hsn.setTdsSection(tdsHsnCode.getTdsSection());
		hsn.setActive(true);
		hsn.setCreatedBy(userName);
		hsn.setCreatedDate(Instant.now());
		hsn.setModifiedBy(userName);
		hsn.setModifiedDate(Instant.now());
		try {
			tdsHsnCodeRepository.save(hsn);
		} catch (Exception e) {
			logger.error("Duplicate hsn code not allowed");
			throw new CustomException("Duplicate hsn code not allowed", HttpStatus.BAD_REQUEST);
		}
		return hsn;
	}

	/**
	 * 
	 * @param id
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	public Optional<MasterTdsHsnCode> fineById(Integer id) {
		Optional<MasterTdsHsnCode> hsnList = tdsHsnCodeRepository.getById(id);
		if (hsnList.isPresent()) {
			return hsnList;
		} else {
			return Optional.empty();
		}
	}

	/**
	 * 
	 * @param tdsHsnCode
	 * @param deductorPan
	 * @param tan
	 * @param userName
	 * @return
	 */
	public TdsHsnCodeDTO updateHsn(TdsHsnCodeDTO tdsHsnCode, String userName) {
		Optional<MasterTdsHsnCode> hsnList = tdsHsnCodeRepository.getById(tdsHsnCode.getId());
		if (hsnList.isPresent()) {
			hsnList.get().setDescription(tdsHsnCode.getDescription());
			hsnList.get().setHsnCode(tdsHsnCode.getHsnCode());
			hsnList.get().setNatureOfPayment(tdsHsnCode.getNatureOfPayment());
			hsnList.get().setTdsSection(tdsHsnCode.getTdsSection());
			hsnList.get().setModifiedBy(userName);
			hsnList.get().setModifiedDate(Instant.now());
			try {
				// update hsn code
				tdsHsnCodeRepository.save(hsnList.get());
			} catch (Exception e) {
				logger.error("Duplicate hsn code not allowed");
				throw new CustomException("Duplicate hsn code not allowed", HttpStatus.BAD_REQUEST);
			}
		}
		return tdsHsnCode;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public ByteArrayInputStream exportNOPAndSection() throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			XSSFSheet sheet = wb.createSheet("Nature and sections");
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);
			// protection enable
			sheet.protectSheet("password");
			// creating the header row and applying style to the header
			XSSFRow row = sheet.createRow(0);
			sheet.setDefaultColumnWidth(25);
			sheet.setAutoFilter(CellRangeAddress.valueOf("A1:B1"));
			XSSFCellStyle style0 = wb.createCellStyle();

			style0.setBorderTop(BorderStyle.MEDIUM);
			style0.setBorderBottom(BorderStyle.MEDIUM);
			style0.setBorderRight(BorderStyle.MEDIUM);
			style0.setAlignment(HorizontalAlignment.CENTER);
			style0.setVerticalAlignment(VerticalAlignment.CENTER);

			Font font = wb.createFont();
			font.setBold(true);
			font.setFontName("Arial");
			style0.setFont(font);

			// Black colour
			style0.setFillForegroundColor(new XSSFColor(new java.awt.Color(46, 134, 193), defaultIndexedColorMap));
			style0.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			// creating cells for the header
			row.createCell(0).setCellValue("NatureOfPayment");
			row.getCell(0).setCellStyle(style0);
			row.createCell(1).setCellValue("Section");
			row.getCell(1).setCellStyle(style0);

			// feign client for get all nop
			List<NatureOfPaymentMaster> response = natureOfPaymentMasterRepository.findAll();
			logger.info("nop response size is :{}", response.size());

			int rowNumber = 1;
			if (!response.isEmpty()) {
				for (NatureOfPaymentMaster nop : response) {
					XSSFRow row2 = sheet.createRow(rowNumber);
					row2.createCell(0).setCellValue(nop.getNature());
					row2.createCell(1).setCellValue(nop.getSection());
					rowNumber++;
				}
			}

			wb.write(out);
		}
		return new ByteArrayInputStream(out.toByteArray());
	}

}
