package com.ey.in.tds.ingestion.service.tdsmismatch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.util.CommonUtil;
import com.microsoft.azure.storage.StorageException;

@Service
public class TdsMismatchService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${page_size}")
	protected int pageSize;

	@Autowired
	protected BlobStorage blob;
	
	@Autowired
	protected BatchUploadDAO batchUploadDAO;

	protected File getConvertedExcelFile(byte[] byteArray, String fileName) throws FileNotFoundException, IOException {
		File someFile = new File(fileName);
		try (FileOutputStream fos = new FileOutputStream(someFile)) {
			fos.write(byteArray);
			fos.flush();
			fos.close();
			return someFile;
		}
	}

	protected BatchUpload saveMismatchReport(String deductorTan, String tenantId, int assessmentYear,
			ByteArrayOutputStream out, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId, String fileName) throws FileNotFoundException, IOException, URISyntaxException,
			InvalidKeyException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String path = null;
		if (out != null) {
			File file = getConvertedExcelFile(out.toByteArray(), fileName);
			path = blob.uploadExcelToBlobWithFile(file, tenantId);
			logger.info("Mismatch report {} completed for : {}", uploadType, userName);
		} else {
			logger.info("Mismatch report {} started for : {}", uploadType, userName);
		}
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setBatchUploadID(batchId);
		batchUpload.setActive(true);
		List<BatchUpload> response = null;
		if (batchId != null) {
			response = batchUploadDAO.findById(assessmentYear, deductorTan, uploadType, batchId);
		}
		if (response != null && !response.isEmpty()) {
			batchUpload = response.get(0);
			if (out != null) {
				batchUpload.setModifiedDate(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setFileName(fileName);
				batchUpload.setStatus("Processed");
				batchUpload.setFilePath(path);

			} else {
				batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setCreatedBy(userName);
				batchUpload.setModifiedDate(null);
				batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setRowsCount(0l);
			}
			batchUpload.setModifiedBy(userName);
			return batchUploadDAO.update(batchUpload);
		} else {
			batchUpload.setAssessmentYear(assessmentYear);
			batchUpload.setDeductorMasterTan(deductorTan);
			batchUpload.setUploadType(uploadType);
			batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedBy(userName);
		}
		batchUpload.setFileName(fileName);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		// do not update count with 0 for async reports
		// batchUpload.setProcessedCount(0);
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		return batchUploadDAO.save(batchUpload);
	}
	/**
	 * 
	 * @param processedRows
	 * @param totalRows
	 * @param batchUpload
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws StorageException
	 * @throws ParseException 
	 */
	protected void updateMismatchExportReportStatus(int processedRows, long totalRows, BatchUpload batchUpload)
			throws FileNotFoundException, IOException, URISyntaxException, InvalidKeyException, StorageException, ParseException {

		if (batchUpload.getBatchUploadID() != null) {
			batchUpload.setModifiedDate(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setRowsCount(totalRows);
			batchUpload.setProcessedCount(processedRows);
			batchUploadDAO.update(batchUpload);
		}
	}
	/**
	 * 
	 * @param deductorTan
	 * @param tenantId
	 * @param assessmentYear
	 * @param out
	 * @param noOfRows
	 * @param uploadType
	 * @param status
	 * @param month
	 * @param userName
	 * @param batchId
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws StorageException
	 */
	protected BatchUpload saveMatrixReport(String deductorTan, String tenantId, int assessmentYear,
			ByteArrayOutputStream out, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId, String fileName)
			throws FileNotFoundException, IOException, URISyntaxException, InvalidKeyException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Matrix report {} started for : {}", uploadType, userName);
		String path = null;
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setActive(true);
		batchUpload.setCreatedDate(new Date());
		batchUpload.setCreatedBy(userName);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setSuccessFileUrl(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		if (StringUtils.isNotBlank(fileName)) {
			fileName = fileName + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
			batchUpload.setFileName(fileName);
		}
		List<BatchUpload> response = null;
		if (batchId != null) {
			response = batchUploadDAO.findById(assessmentYear, deductorTan, uploadType, batchId);
		}
		if (response != null && !response.isEmpty()) {
			batchUpload = response.get(0);
			if (out != null) {
				File file = getConvertedExcelFile(out.toByteArray(), batchUpload.getFileName());
				path = blob.uploadExcelToBlobWithFile(file, tenantId);
				batchUpload.setFilePath(path);
				batchUpload.setSuccessFileUrl(path);
				batchUpload.setStatus(status);
				batchUpload.setModifiedDate(new Date());
				batchUpload.setProcessEndTime(new Date());
				batchUpload.setRowsCount(noOfRows);
			} else {
				batchUpload.setModifiedDate(null);
				batchUpload.setProcessStartTime(new Date());
				batchUpload.setProcessEndTime(new Date());
				batchUpload.setRowsCount(0l);
			}
			batchUpload.setModifiedBy(userName);
			batchUpload.setSuccessCount(noOfRows);
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		logger.info("Matrix report {} completed for : {}", uploadType, userName);
		return batchUpload;
	}

	/**
	 * 
	 * @param deductorTan
	 * @param tenantId
	 * @param assessmentYear
	 * @param path
	 * @param noOfRows
	 * @param uploadType
	 * @param status
	 * @param month
	 * @param userName
	 * @param batchId
	 * @param fileName
	 * @return
	 */
	public BatchUpload saveAndUpdateMismatchReport(String deductorTan, String tenantId, int assessmentYear,
			String path, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId, String fileName) {
		MultiTenantContext.setTenantId(tenantId);
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setBatchUploadID(batchId);
		batchUpload.setFileName(fileName);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setSuccessFileUrl(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
		batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		List<BatchUpload> response = null;
		if (batchId != null) {
			response = batchUploadDAO.findById(assessmentYear, deductorTan, uploadType, batchId);
		}
		if (response != null && !response.isEmpty()) {
			batchUpload = response.get(0);
			if (StringUtils.isNotBlank(path)) {
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchUpload.setFileName(fileName);
				batchUpload.setFilePath(path);
				batchUpload.setSuccessFileUrl(path);
				batchUpload.setModifiedBy(userName);
				batchUpload.setStatus(status);
			} else {
				batchUpload.setModifiedDate(null);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			}
			batchUpload.setRowsCount(noOfRows);
			batchUpload.setModifiedBy(userName);
			return batchUploadDAO.update(batchUpload);
		} else {
			return batchUploadDAO.save(batchUpload);
		}
	}
}