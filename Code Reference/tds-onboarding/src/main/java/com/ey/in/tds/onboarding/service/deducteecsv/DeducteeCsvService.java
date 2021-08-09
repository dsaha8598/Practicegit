package com.ey.in.tds.onboarding.service.deducteecsv;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.onboarding.service.deductee.DeducteeBulkService;
import com.microsoft.azure.storage.StorageException;

@Service
public class DeducteeCsvService {

	@Autowired
	private BlobStorage blob;
	
	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private DeducteeBulkService deducteeBulkService;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public BatchUpload readCsvFile(MultipartFile file, String tan, String tenantId, Integer assessmentYear,
			Integer assessmentMonth, String userName, String deductorPan, Integer batchId) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		BatchUpload batchUpload = batchUploadDAO.findByOnlyId(batchId);
		return deducteeBulkService.processResidentDeductees(batchUpload.getFilePath(), tan, assessmentYear,
				assessmentMonth, userName, tenantId, deductorPan, batchUpload);
	}

	public BatchUpload deducteeBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		String errorFp = null;
		if (file != null) {
			errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
		}
		int month = assessmentMonthPlusOne;
		batchUpload.setAssessmentMonth(month);
		batchUpload.setErrorFilePath(errorFp);
		batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
		batchUpload.setModifiedBy(userName);
		batchUpload.setActive(true);
		logger.info("Saving batch upload entity : {}", batchUpload);
		batchUpload = batchUploadDAO.save(batchUpload);
		return batchUpload;
	}

	public static Date subtractDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return cal.getTime();
	}

}
