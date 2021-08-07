package com.ey.in.tds.ingestion.service.glinvoice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.model.job.Job;
import com.ey.in.tds.common.model.job.NoteBookParam;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.ingestion.dto.batch.BatchUploadDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

@Service
public class GlInvoiceService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private BlobStorage blob;

	@Autowired
	private Sha256SumService sha256SumService;
	
	@Autowired
	private BatchUploadDAO batchUploadDAO;
	
	@Value("${databricks.key}")
	private String dataBricksKey;
	
	@Value("${spark.notebooks.gl.job-id}")
	private long glJobId;
	
	@Value("${spark.notebooks.gl.url}")
	private String glNoteBookUrl;

	public BatchUpload saveUploadExcelInvoiceForGL(int assessmentYear, MultipartFile file,String tenantId)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		String path;

		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		BatchUpload batchUpload = null;
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Please select the excel file only");
		} else {
			logger.info(file.getOriginalFilename());
			String sha256 = sha256SumService.getSHA256Hash(file);

			List<BatchUpload> batch = batchUploadDAO.getSha256RecordsBasedonYearMonth(assessmentYear, month,
					UploadTypes.GL_INV.name(), sha256);
			path = blob.uploadExcelToBlob(file,tenantId);
			if(logger.isDebugEnabled()) {
				logger.debug("Batch Object : {}" , batch);
			}

			batchUpload = new BatchUpload();
			batchUpload.setAssessmentYear(assessmentYear);
			batchUpload.setDeductorMasterTan("missingDeductorTan");
			batchUpload.setUploadType(UploadTypes.GL_INV.name());
			if (!batch.isEmpty()) {
				logger.info("Duplicate Record inserting --{}" , file.getOriginalFilename());
				batchUpload.setNewStatus("Duplicate");
				batchUpload.setReferenceId(batch.get(0).getBatchUploadID());
			} else {
				logger.info("Unique record  inserting  : {}" , file.getOriginalFilename());
				batchUpload.setNewStatus("Uploaded");
				logger.info("Unique record creating");
			}

			batchUpload.setFileName(file.getOriginalFilename());
			batchUpload.setFilePath(path);
			batchUpload.setCreatedDate(new Date());
			batchUpload.setCreatedBy("gl invoice  excel user");
			batchUpload.setActive(true);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);

			batchUpload = batchUploadDAO.save(batchUpload);

			// Calling Spark
			if (!batchUpload.getNewStatus().equalsIgnoreCase("Duplicate")) {
				HttpHeaders headers = new HttpHeaders();
				// TODO fix this by moving to properties
				headers.add("Authorization", "Bearer "+ dataBricksKey);
				headers.add("Content-Type", "application/json");

				RestTemplate restTemplate = new RestTemplate();
				Job job = new Job();

				job.setJob_id(glJobId);
				NoteBookParam noteBookParam = new NoteBookParam();
				noteBookParam.setAssessmentMonth(month);
				noteBookParam.setAssessmentYear(assessmentYear);
				noteBookParam.setFileName(batchUpload.getFileName());
				noteBookParam.setSha256(batchUpload.getSha256sum());
				noteBookParam.setId(batchUpload.getBatchUploadID());
				noteBookParam.setStatus(batchUpload.getStatus());
				noteBookParam.setType(batchUpload.getUploadType());
				job.setNotebook_params(noteBookParam);

				ObjectMapper objMapper = new ObjectMapper();
				objMapper.writeValueAsString(job);
				if(logger.isDebugEnabled()) {
					logger.debug(" Object : {}" , objMapper.writeValueAsString(job));
					logger.debug(job.toString());
				}

				HttpEntity<Job> entity = new HttpEntity<>(job, headers);
				String response = restTemplate.postForObject(glNoteBookUrl, entity, String.class);

				logger.info("Response : {}" , response);
			}
		}

		return batchUpload;
	}

	public PagedData<BatchUploadDTO> getlistOfGlInvoiceExcelBasedOnAssessmentYearMonthType(Pagination pagination) {
		String glInvoiceExcel = "GL_INV";
		List<BatchUploadDTO> batchListDTO = new ArrayList<>();
		
		PagedData<BatchUpload> listBatch = batchUploadDAO
				.getlistOfAoLdcProvisionInvoicesBasedOnAssessmentYearMonthType(
						Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1,
						glInvoiceExcel);

		for (BatchUpload batchUpload : listBatch.getData()) {

			BatchUploadDTO batchDTO = new BatchUploadDTO();
			batchDTO.setId(batchUpload.getBatchUploadID());
			batchDTO.setDateOfUpload(batchUpload.getCreatedDate());
			batchDTO.setFileName(batchUpload.getFileName());
			if (batchUpload.getNewStatus() == null) {
				batchDTO.setFileStatus("Uploaded");
			} else {
				batchDTO.setFileStatus(batchUpload.getNewStatus());
			}
			batchDTO.setUploadedFileDownloadUrl(batchUpload.getFilePath());
			batchDTO.setUploadBy(batchUpload.getCreatedBy());
			batchDTO.setReferenceId(batchUpload.getReferenceId());
			batchDTO.setSuccessCount(batchUpload.getSuccessCount());
			batchDTO.setDuplicateRecords(batchUpload.getDuplicateCount());
			batchDTO.setMismatchCount(batchUpload.getMismatchCount());
			batchDTO.setTotalRecords(batchUpload.getRowsCount());
			batchDTO.setErrorRecords(batchUpload.getFailedCount());
			batchDTO.setProcessedRecords(batchUpload.getProcessedCount());
			batchDTO.setErrorFileDownloadUrl(batchUpload.getErrorFilePath());
			batchListDTO.add(batchDTO);
		}
		return new PagedData<>(batchListDTO, listBatch.getPageSize(), listBatch.getPageStates());
	}

}
