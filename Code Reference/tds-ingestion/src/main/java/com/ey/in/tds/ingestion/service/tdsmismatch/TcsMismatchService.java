package com.ey.in.tds.ingestion.service.tdsmismatch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.CommonUtil;
import com.microsoft.azure.storage.StorageException;

@Service
public class TcsMismatchService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${page_size}")
	protected int pageSize;

	@Autowired
	protected BlobStorage blob;

	@Autowired
	protected TCSBatchUploadDAO tcsBatchUploadDAO;

	/**
	 * 
	 * @param byteArray
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected File getConvertedExcelFile(byte[] byteArray, String fileName) throws FileNotFoundException, IOException {
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
	 * @param collectorTan
	 * @param tenantId
	 * @param assessmentYear
	 * @param out
	 * @param noOfRows
	 * @param uploadType
	 * @param status
	 * @param month
	 * @param userName
	 * @param batchId
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws StorageException
	 */
	public TCSBatchUpload saveMismatchReport(String collectorTan, String tenantId, int assessmentYear,
			ByteArrayOutputStream out, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId)
			throws FileNotFoundException, IOException, URISyntaxException, InvalidKeyException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		String path = null;
		String fileName = null;
		if (out != null) {
			fileName = uploadType.toLowerCase() + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
			File file = getConvertedExcelFile(out.toByteArray(), fileName);
			path = blob.uploadExcelToBlobWithFile(file, tenantId);
			logger.info("Mismatch report {} completed for : {}", uploadType, userName);
		} else {
			logger.info("Mismatch report {} started for : {}", uploadType, userName);
		}
		TCSBatchUpload batchUpload = new TCSBatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setCollectorMasterTan(collectorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setId(batchId);
		batchUpload.setFileName(fileName);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		batchUpload.setCreatedDate(new Date());
		logger.info("Mismatch time: " + batchUpload.getCreatedDate());
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		List<TCSBatchUpload> response = null;
		if (batchId != null) {
			response = tcsBatchUploadDAO.findById(assessmentYear, collectorTan, uploadType, batchId);
		}
		if (response != null && !response.isEmpty()) {
			batchUpload = response.get(0);
			if (out != null) {
				batchUpload.setModifiedDate(new Date());
				batchUpload.setProcessEndTime(new Date());
				batchUpload.setFileName(fileName);
				batchUpload.setFilePath(path);
				batchUpload.setModifiedBy(userName);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchUpload.setStatus("Processed");
			} else {
				batchUpload.setModifiedDate(null);
				batchUpload.setProcessStartTime(new Date());
				batchUpload.setProcessEndTime(new Date());
				batchUpload.setRowsCount(0l);
			}
			batchUpload.setModifiedBy(userName);
			logger.info("Mismatch time update: " + batchUpload.getCreatedDate());
			return tcsBatchUploadDAO.update(batchUpload);
		} else {
			logger.info("Mismatch time save: " + batchUpload.getCreatedDate());
			return tcsBatchUploadDAO.save(batchUpload);
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
	protected TCSBatchUpload saveMatrixReport(String deductorTan, String tenantId, int assessmentYear,
			ByteArrayOutputStream out, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId, String fileName)
			throws FileNotFoundException, IOException, URISyntaxException, InvalidKeyException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Matrix report {} started for : {}", uploadType, userName);
		String path = null;
		TCSBatchUpload batchUpload = new TCSBatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setCollectorMasterTan(deductorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setId(batchId);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setSuccessFileUrl(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		batchUpload.setProcessed(noOfRows.intValue());
		batchUpload.setUploadType(uploadType);
		batchUpload.setCreatedDate(new Date());
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		if (StringUtils.isNotBlank(fileName)) {
			fileName = fileName + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
			batchUpload.setFileName(fileName);
		}
		List<TCSBatchUpload> response = null;
		if (batchId != null) {
			response = tcsBatchUploadDAO.findById(assessmentYear, deductorTan, uploadType, batchId);
		}
		if (response != null && !response.isEmpty()) {
			batchUpload = response.get(0);
			if (out != null) {
				File file = getConvertedExcelFile(out.toByteArray(), batchUpload.getFileName());
				path = blob.uploadExcelToBlobWithFile(file, tenantId);
				batchUpload.setFilePath(path);
				batchUpload.setSuccessFileUrl(path);
				batchUpload.setModifiedDate(new Date());
				batchUpload.setProcessEndTime(new Date());
			} else {
				batchUpload.setCreatedDate(new Date());
				batchUpload.setCreatedBy(userName);
				batchUpload.setModifiedDate(null);
				batchUpload.setProcessStartTime(new Date());
				batchUpload.setProcessEndTime(new Date());
				batchUpload.setRowsCount(0l);
			}
			batchUpload.setStatus(status);
			batchUpload.setModifiedBy(userName);
			batchUpload.setProcessed(noOfRows.intValue());
			batchUpload = tcsBatchUploadDAO.update(batchUpload);
		} else {
			batchUpload = tcsBatchUploadDAO.save(batchUpload);
		}

		logger.info("Matrix report {} completed for : {}", uploadType, userName);
		return batchUpload;
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
	 */
	protected void updateMismatchExportReportStatus(int processedRows, long totalRows, TCSBatchUpload batchUpload)
			throws FileNotFoundException, IOException, URISyntaxException, InvalidKeyException, StorageException {
		if (batchUpload.getId() != null) {
			batchUpload.setModifiedDate(new Date());
			batchUpload.setRowsCount(totalRows);
			batchUpload.setProcessed(processedRows);
			tcsBatchUploadDAO.update(batchUpload);
		}
	}

	/**
	 * This method for spark reports file download.
	 * 
	 * @param collectorTan
	 * @param tenantId
	 * @param assessmentYear
	 * @param out
	 * @param noOfRows
	 * @param uploadType
	 * @param status
	 * @param month
	 * @param userName
	 * @param batchId
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws StorageException
	 */
	public TCSBatchUpload saveAndUpdateMismatchReport(String collectorTan, String tenantId, int assessmentYear,
			String path, Long noOfRows, String uploadType, String status, int month, String userName, Integer batchId,
			String fileName) {
		MultiTenantContext.setTenantId(tenantId);

		TCSBatchUpload batchUpload = new TCSBatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setCollectorMasterTan(collectorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setId(batchId);
		batchUpload.setFileName(fileName);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setSuccessFileUrl(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		batchUpload.setCreatedDate(new Date());
		logger.info("Mismatch time: " + batchUpload.getCreatedDate());
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		List<TCSBatchUpload> response = null;
		if (batchId != null) {
			response = tcsBatchUploadDAO.findById(assessmentYear, collectorTan, uploadType, batchId);
		}
		if (response != null && !response.isEmpty()) {
			batchUpload = response.get(0);
			if (StringUtils.isNotBlank(path)) {
				batchUpload.setModifiedDate(new Date());
				batchUpload.setProcessEndTime(new Date());
				batchUpload.setFileName(fileName);
				batchUpload.setFilePath(path);
				batchUpload.setSuccessFileUrl(path);
				batchUpload.setModifiedBy(userName);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchUpload.setStatus("Processed");
			} else {
				batchUpload.setModifiedDate(null);
				batchUpload.setProcessStartTime(new Date());
				batchUpload.setProcessEndTime(new Date());
				batchUpload.setRowsCount(1l);
			}
			batchUpload.setModifiedBy(userName);
			logger.info("Mismatch time update: " + batchUpload.getCreatedDate());
			return tcsBatchUploadDAO.update(batchUpload);
		} else {
			logger.info("Mismatch time save: " + batchUpload.getCreatedDate());
			return tcsBatchUploadDAO.save(batchUpload);
		}
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public TCSBatchUpload getTCSBatchUpload(Integer id) {
		List<TCSBatchUpload> batchUploadResponse = tcsBatchUploadDAO.findById(id);
		if (!batchUploadResponse.isEmpty() && batchUploadResponse != null) {
			return batchUploadResponse.get(0);
		} else {
			throw new CustomException("Did not find a BatchUpload with the passed in criteria", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param tcsBatchUpload
	 * @return
	 */
	public TCSBatchUpload update(TCSBatchUpload tcsBatchUpload) {
		return tcsBatchUploadDAO.update(tcsBatchUpload);
	}
}
