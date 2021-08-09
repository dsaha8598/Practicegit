package com.ey.in.tds.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.RedisUtil;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.dto.sac.ServicesAccountingCode;
import com.ey.in.tds.common.model.sac.SacErrorReportCsvDTO;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.repository.MasterBatchUploadRepository;
import com.ey.in.tds.service.sac.SacExcel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

@Service
public class SacService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RedisUtil<Map<String, Object>> redisUtilUserTenantInfo;
	
	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private MasterBatchUploadRepository masterBatchUploadRepository;

	@Autowired
	private MasterBatchUploadService masterBatchUploadService;
	
	/**
	 * 
	 * @param file
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	@Transactional
	public MasterBatchUpload saveFileData(MultipartFile file, Integer assesssmentYear, Integer assessmentMonth,
			String userName) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		
		String uploadType = UploadTypes.SAC_EXCEL.name();
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
			int headersCount = SacExcel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			if (headersCount != SacExcel.fieldMappings.size()) {
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
			if (headersCount == SacExcel.fieldMappings.size()) {
				return processSacData(workbook, file, sha256, assesssmentYear, assessmentMonth, userName,
						masterBatchUpload, uploadType);
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process sac data ", e);
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
	 * @param workbook
	 * @param uploadedFile
	 * @param sha256
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param masterBatchUpload
	 * @return
	 * @throws Exception
	 */
	@Async
	public MasterBatchUpload processSacData(XSSFWorkbook workbook, MultipartFile uploadedFile, String sha256,
			Integer assessmentYear, Integer assessmentMonth, String userName, MasterBatchUpload masterBatchUpload,
			String uploadType) throws Exception {
		File sacErrorFile = null;
		try {
			SacExcel data = new SacExcel(workbook);
			masterBatchUpload.setSha256sum(sha256);
			masterBatchUpload.setMismatchCount(0L);
			long dataRowsCount = data.getDataRowsCount();
			masterBatchUpload.setRowsCount(dataRowsCount);
			List<ServicesAccountingCode> sacList = new ArrayList<>();
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				try {
					ServicesAccountingCode sac = data.get(rowIndex);
					sacList.add(sac);
				} catch (Exception e) {
					logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
					SacErrorReportCsvDTO problematicDataError = data.getErrorDTO(rowIndex);
					if (StringUtils.isBlank(problematicDataError.getReason())) {
						problematicDataError.setReason(
								"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
					}
				}
			}
			ObjectMapper mapper = new ObjectMapper();
			String jsonString = mapper.writeValueAsString(sacList);
			String redisKey = "ref_new";
			logger.info("sac data {}", jsonString);
			logger.info("json string : {}", jsonString);
			redisUtilUserTenantInfo.putValue(redisKey, jsonString);

			int processedRecordsCount = sacList.size();
			masterBatchUpload.setSuccessCount(dataRowsCount);
			masterBatchUpload.setFailedCount(0L);
			masterBatchUpload.setProcessed(processedRecordsCount);
			masterBatchUpload.setDuplicateCount(0L);
			masterBatchUpload.setStatus("Processed");
			masterBatchUpload.setCreatedDate(Instant.now());
			masterBatchUpload.setProcessEndTime(new Date());
			masterBatchUpload.setCreatedBy(userName);
		} catch (
		Exception e) {
			logger.error("Exception occurred :", e);
		}
		return masterBatchUploadService.masterBatchUpload(masterBatchUpload, uploadedFile, assessmentYear, assessmentMonth,
				userName, sacErrorFile, uploadType);
	}

	// To Json

	public String convertObjects2JsonString(ServicesAccountingCode sacs) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "";

		try {
			jsonString = mapper.writeValueAsString(sacs);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		logger.info(jsonString);
		return jsonString;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<ServicesAccountingCode> getAllSacs() throws Exception {

		List<ServicesAccountingCode> sacs = new ArrayList<>();
		try {
			sacs.addAll(redisUtilUserTenantInfo.fetchSACRedisData("ref_new"));
		} catch (Exception e) {
			logger.info("Data not found in redis");
		}
		logger.info("Retrieved data : {}", sacs);
		return sacs;

	}

}
