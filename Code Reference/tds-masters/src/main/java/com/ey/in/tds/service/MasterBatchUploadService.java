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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.util.MasterBlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.repository.MasterBatchUploadRepository;
import com.microsoft.azure.storage.StorageException;

/***
 * 
 * @author vamsir
 *
 */
@Service
public class MasterBatchUploadService {

	private final Logger logger = LoggerFactory.getLogger(MasterBatchUploadService.class);

	@Autowired
	private MasterBlobStorage blob;

	@Autowired
	private MasterBatchUploadRepository masterBatchUploadRepository;

	/**
	 * 
	 * @param masterBatchUpload
	 * @param file
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param object
	 * @return
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	public MasterBatchUpload masterBatchUpload(MasterBatchUpload masterBatchUpload, MultipartFile mFile,
			Integer assesssmentYear, Integer assessmentMonth, String userName, File file, String uploadType)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		logger.info("batch", masterBatchUpload);
		String errorFp = null;
		String path = null;
		if (file != null) {
			errorFp = blob.uploadExcelToBlobWithFile(file);
			masterBatchUpload.setErrorFilePath(errorFp);
		}
		if (mFile != null) {
			path = blob.uploadExcelToBlob(mFile);
			masterBatchUpload.setFileName(mFile.getOriginalFilename());
			masterBatchUpload.setFilePath(path);
		}
		masterBatchUpload.setAssessmentMonth(assessmentMonth);
		masterBatchUpload.setAssessmentYear(assesssmentYear);
		masterBatchUpload.setUploadType(uploadType);
		masterBatchUpload.setCreatedBy(userName);
		masterBatchUpload.setModifiedBy(userName);
		masterBatchUpload.setModifiedDate(Instant.now());
		masterBatchUpload.setActive(true);
		masterBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
		return masterBatchUploadRepository.save(masterBatchUpload);
	}

	/**
	 * 
	 * @param type
	 * @param year
	 * @return
	 */
	public List<MasterBatchUpload> getListOfBatchUploadFiles(String type, int year) {
		List<MasterBatchUpload> listBatch = new ArrayList<MasterBatchUpload>();
		listBatch = masterBatchUploadRepository.getBatchUplodFiles(type, year);
		if (!listBatch.isEmpty() && listBatch != null) {
			return listBatch;
		} else {
			return listBatch;
		}
	}

	/**
	 * 
	 * @param assesmentYear
	 * @param uploadType
	 * @param batchId
	 * @return
	 */
	public MasterBatchUpload getFileDetails(Integer assesmentYear, String uploadType, Integer batchId) {
		Optional<MasterBatchUpload> batchUploadResponse = masterBatchUploadRepository.findById(assesmentYear,
				uploadType, batchId);
		if (batchUploadResponse.isPresent()) {
			return batchUploadResponse.get();
		} else {
			throw new CustomException("Did not find a master batch upload with the passed in criteria",
					HttpStatus.BAD_REQUEST);
		}
	}

}
