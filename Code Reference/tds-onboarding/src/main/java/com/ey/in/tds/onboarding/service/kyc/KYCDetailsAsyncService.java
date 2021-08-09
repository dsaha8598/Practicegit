package com.ey.in.tds.onboarding.service.kyc;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.onboarding.service.util.excel.deductee.CollecteeDeclarationExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.CollecteeThresholdExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.CustomerKYCDetailsExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.DeducteeDeclarationExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.DeducteeThresholdExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.ShareholderDeclarationExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.ShareholderKYCDetalsExcle;
import com.ey.in.tds.onboarding.service.util.excel.deductee.VendorKYCDetailsExcle;
import com.microsoft.azure.storage.StorageException;

/**
 *
 * @author vamsir
 *
 */
@Service
public class KYCDetailsAsyncService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private KYCDetailsService kycDetailsService;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private TCSBatchUploadDAO tcsBatchUploadDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	DeclarationIntegrationService declarationIntegrationService;

	/**
	 * 
	 * @param multiPartFile
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param type
	 * @return
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	public BatchUpload saveFileData(MultipartFile multiPartFile, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonth, String userName, String tenantId, String deductorPan, String type)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);
		BatchUpload batchUpload = new BatchUpload();
		if (isAlreadyProcessed(sha256)) {
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
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
			batchUpload = KycDetailsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
					assessmentMonth, userName, null, tenantId, type);
			return batchUpload;
		}

		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {

			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = Excel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			if (headersCount != CustomerKYCDetailsExcel.fieldMappings.size()
					|| headersCount != VendorKYCDetailsExcle.fieldMappings.size()
					|| headersCount != ShareholderKYCDetalsExcle.fieldMappings.size()) {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setStatus("Failed");
				batchUpload.setCreatedBy(userName);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return KycDetailsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear, assessmentMonth,
						userName, null, tenantId, type);
			} else {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setStatus("Processing");
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload = KycDetailsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonth, userName, null, tenantId, type);
			}
			if (headersCount == CustomerKYCDetailsExcel.fieldMappings.size()
					|| headersCount == VendorKYCDetailsExcle.fieldMappings.size()
					|| headersCount == ShareholderKYCDetalsExcle.fieldMappings.size()) {
				kycDetailsService.asyncProcessKycDetails(deductorTan, assesssmentYear, assessmentMonth, userName,
						tenantId, deductorPan, batchUpload, type);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process deductee data ", e);
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param batchUpload
	 * @param mFile
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param file
	 * @param tenant
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public BatchUpload KycDetailsBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant,
			String type) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
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
		if ("CUSTOMER".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.CUSTOMER_KYC_DETAILS_UPLOAD.name());
		} else if ("VENDOR".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.VENDOR_KYC_DETAILS_UPLOAD.name());
		} else if ("SHAREHOLDER".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.SHAREHOLDER_KYC_DETAILS_UPLOAD.name());
		} else if ("DEDUCTEE_THRESHOLD_UPDATE".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.DEDUCTEE_THRESHOLD_UPDATE.name());
		} else if ("DEDUCTEE_DECLARATION".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.DEDUCTEE_DECLARATION.name());
		} else if ("SHAREHOLDER_DECLARATION".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.SHAREHOLDER_DECLARATION.name());
		}
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		if (batchUpload.getBatchUploadID() != null) {
			batchUpload.setBatchUploadID(batchUpload.getBatchUploadID());
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	private boolean isAlreadyProcessed(String sha256Sum) {

		List<BatchUpload> sha256Record = batchUploadDAO.getSha256Records(sha256Sum);

		return sha256Record != null && !sha256Record.isEmpty();
	}

	/**
	 * 
	 * @param sha256Sum
	 * @return
	 */
	private boolean isAlreadyProcessedTCS(String sha256Sum) {
		List<TCSBatchUpload> sha256Record = tcsBatchUploadDAO.getSha256Records(sha256Sum);
		return sha256Record != null && !sha256Record.isEmpty();
	}

	/**
	 * 
	 * @param file
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @param pan
	 * @return
	 */
	public BatchUpload declaration(MultipartFile multiPartFile, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonth, String userName, String tenantId, String deductorPan, String type)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);
		BatchUpload batchUpload = new BatchUpload();
		if (isAlreadyProcessed(sha256)) {
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
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
			batchUpload = KycDetailsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
					assessmentMonth, userName, null, tenantId, type);
			return batchUpload;
		}

		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = Excel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			if (headersCount != DeducteeDeclarationExcel.fieldMappings.size()) {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setStatus("Failed");
				batchUpload.setCreatedBy(userName);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return KycDetailsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear, assessmentMonth,
						userName, null, tenantId, type);
			} else {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setStatus("Processing");
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload = KycDetailsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonth, userName, null, tenantId, type);
			}
			if (headersCount == DeducteeDeclarationExcel.fieldMappings.size()) {
				declarationIntegrationService.asyncProcessDeducteeDeclaration(userName, tenantId, deductorPan,
						batchUpload);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process deductee data {}", e.getCause());
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param multiPartFile
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param type
	 * @return
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	public TCSBatchUpload colleteeThresholdUpdate(MultipartFile multiPartFile, String deductorTan,
			Integer assesssmentYear, Integer assessmentMonth, String userName, String tenantId, String deductorPan,
			String type) throws InvalidKeyException, IOException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);
		TCSBatchUpload batchUpload = new TCSBatchUpload();
		if (isAlreadyProcessedTCS(sha256)) {
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessed(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setSha256sum(sha256);
			batchUpload.setStatus("Duplicate");
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload = kycDetailsTcsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
					assessmentMonth, userName, null, tenantId, type);
			return batchUpload;
		}

		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = Excel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			if (headersCount != CollecteeThresholdExcel.fieldMappings.size()) {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessed(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setStatus("Failed");
				batchUpload.setCreatedBy(userName);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return kycDetailsTcsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonth, userName, null, tenantId, type);
			} else {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setStatus("Processing");
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessed(0);
				batchUpload.setMismatchCount(0L);
				batchUpload = kycDetailsTcsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonth, userName, null, tenantId, type);
			}
			if (headersCount == CollecteeThresholdExcel.fieldMappings.size()
					&& "COLLECTEE_THRESHOLD_UPDATE".equals(type)) {
				declarationIntegrationService.asyncProcessColleteeThresholdUpdate(userName, tenantId, deductorPan,
						batchUpload);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process deductee data ", e);
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param multiPartFile
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param type
	 * @return
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	public TCSBatchUpload tcsDeclaration(MultipartFile multiPartFile, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonth, String userName, String tenantId, String deductorPan, String type)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);
		TCSBatchUpload batchUpload = new TCSBatchUpload();
		if (isAlreadyProcessedTCS(sha256)) {
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessed(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setSha256sum(sha256);
			batchUpload.setStatus("Duplicate");
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload = kycDetailsTcsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
					assessmentMonth, userName, null, tenantId, type);
			return batchUpload;
		}

		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = Excel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			if (headersCount != CollecteeDeclarationExcel.fieldMappings.size()) {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessed(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setStatus("Failed");
				batchUpload.setCreatedBy(userName);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return kycDetailsTcsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonth, userName, null, tenantId, type);
			} else {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setStatus("Processing");
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessed(0);
				batchUpload.setMismatchCount(0L);
				batchUpload = kycDetailsTcsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonth, userName, null, tenantId, type);
			}
			if (headersCount == CollecteeDeclarationExcel.fieldMappings.size()
					&& "COLLECTEE_DECLARATION".equals(type)) {
				declarationIntegrationService.asyncProcessCollecteeDeclaration(userName, tenantId, deductorPan,
						batchUpload);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process deductee data ", e);
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param batchUpload
	 * @param mFile
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param file
	 * @param tenant
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public TCSBatchUpload kycDetailsTcsBatchUpload(TCSBatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant,
			String type) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
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
		batchUpload.setCollectorMasterTan(tan);

		if ("COLLECTEE_DECLARATION".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.COLLECTEE_DECLARATION.name());
		} else if ("COLLECTEE_THRESHOLD_UPDATE".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.COLLECTEE_THRESHOLD_UPDATE.name());
		}

		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		if (batchUpload.getId() != null) {
			batchUpload.setId(batchUpload.getId());
			batchUpload = tcsBatchUploadDAO.update(batchUpload);
		} else {
			batchUpload = tcsBatchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tan
	 * @param tenantId
	 * @param type
	 * @param batchId
	 * @return
	 */
	public void asyncBatchMailTriggred(String deductorPan, String tan, String tenantId, String type, int batchId) {
		kycDetailsService.batchMailTriggred(deductorPan, tan, tenantId, type, batchId);
	}

	/**
	 * @param tenantId
	 * @param deductorTan
	 * @param deductorPan
	 * @param userName
	 * @throws Exception
	 */
	public void asyncGeneratetanLevelFile(String tenantId, String deductorTan, String deductorPan, String userName)
			throws Exception {
		kycDetailsService.generatetanLevelFile(tenantId, deductorTan, deductorPan, userName);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tan
	 * @param tenantId
	 * @param type
	 * @param year
	 * @param mailType
	 */
	public void asyncControlTotalMailTriggred(String deductorPan, String tan, String tenantId, String type, int year,
			String mailType) {
		kycDetailsService.controlTotalMailTriggred(deductorPan, tan, tenantId, type, year, mailType);

	}

	/**
	 * 
	 * @param file
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @param pan
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	public BatchUpload deducteeThresholdUpdate(MultipartFile multiPartFile, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonth, String userName, String tenantId, String deductorPan, String type)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);
		BatchUpload batchUpload = new BatchUpload();
		if (isAlreadyProcessed(sha256)) {
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
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
			batchUpload = KycDetailsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
					assessmentMonth, userName, null, tenantId, type);
			return batchUpload;
		}

		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = Excel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			if (headersCount != DeducteeThresholdExcel.fieldMappings.size()) {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setStatus("Failed");
				batchUpload.setCreatedBy(userName);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return KycDetailsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear, assessmentMonth,
						userName, null, tenantId, type);
			} else {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setStatus("Processing");
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload = KycDetailsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonth, userName, null, tenantId, type);
			}
			if (headersCount == DeducteeThresholdExcel.fieldMappings.size()) {
				declarationIntegrationService.asyncProcessDeducteeThreholdExcel(userName, tenantId, deductorPan,
						batchUpload);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process deductee data ", e.getCause());
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param multiPartFile
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param type
	 * @return
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	public BatchUpload shareholderdeclaration(MultipartFile multiPartFile, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonth, String userName, String tenantId, String deductorPan, String type)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);
		BatchUpload batchUpload = new BatchUpload();
		if (isAlreadyProcessed(sha256)) {
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
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
			batchUpload = KycDetailsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
					assessmentMonth, userName, null, tenantId, type);
			return batchUpload;
		}

		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = Excel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			if (headersCount != ShareholderDeclarationExcel.fieldMappings.size()) {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setStatus("Failed");
				batchUpload.setCreatedBy(userName);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return KycDetailsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear, assessmentMonth,
						userName, null, tenantId, type);
			} else {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setStatus("Processing");
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload = KycDetailsBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonth, userName, null, tenantId, type);
			}
			if (headersCount == ShareholderDeclarationExcel.fieldMappings.size()) {
				declarationIntegrationService.asyncProcessShareholderDeclaration(userName, tenantId, deductorPan,
						batchUpload);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process deductee data {}", e.getCause());
		}
		return batchUpload;
	}

}
