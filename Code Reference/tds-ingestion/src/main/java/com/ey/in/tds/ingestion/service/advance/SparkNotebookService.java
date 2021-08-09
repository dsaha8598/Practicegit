package com.ey.in.tds.ingestion.service.advance;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.model.job.Job;
import com.ey.in.tds.common.model.job.NoteBookParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author vamsir
 *
 */
@Service
public class SparkNotebookService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${databricks.key}")
	private String dataBricksKey;

	/**
	 * 
	 * @param assessmentYear
	 * @param tan
	 * @param tenantId
	 * @param userEmail
	 * @param month
	 * @param batchUpload
	 * @param dueDate
	 * @param challanMonth
	 * @return
	 */
	public NoteBookParam createNoteBook(int assessmentYear, String tan, String tenantId, String userEmail, int month,
			BatchUpload batchUpload, String dueDate, String challanMonth) {
		NoteBookParam noteBookParam = new NoteBookParam();
		noteBookParam.setAssessmentMonth(month);
		noteBookParam.setAssessmentYear(assessmentYear);
		if (batchUpload != null) {
			noteBookParam.setId(batchUpload.getBatchUploadID());
			noteBookParam.setStatus(batchUpload.getStatus());
			noteBookParam.setType(batchUpload.getUploadType());
			if (StringUtils.isNotBlank(batchUpload.getSha256sum())) {
				noteBookParam.setSha256(batchUpload.getSha256sum());
			} else {
				noteBookParam.setSha256("");
			}
			if (StringUtils.isNotBlank(batchUpload.getFilePath())) {
				// extracting the file name from file url
				noteBookParam.setFileName(
						batchUpload.getFilePath().substring(batchUpload.getFilePath().lastIndexOf("/") + 1));
			} else if (StringUtils.isNotBlank(batchUpload.getFileName())) {
				noteBookParam.setFileName(batchUpload.getFileName());
			} else {
				noteBookParam.setFileName("");
			}
		}
		noteBookParam.setChallanDueDate(dueDate);
		if (StringUtils.isNotBlank(challanMonth)) {
			noteBookParam.setChallanMonth(Integer.valueOf(challanMonth));
		} else {
			noteBookParam.setChallanMonth(0);
		}
		noteBookParam.setTenantId(tenantId);
		noteBookParam.setTan(tan);
		noteBookParam.setApplicationURL("");
		// These two are added for Drools and also UserObject.
		noteBookParam.setUserEmail(userEmail);
		return noteBookParam;
	}

	/**
	 * 
	 * @param api
	 * @param jobId
	 * @param noteBookParam
	 * @param month
	 * @param assessmentYear
	 * @param tenantId
	 * @param tan
	 * @param userEmail
	 * @return
	 * @throws JsonProcessingException
	 */
	public String triggerSparkNotebook(String api, int jobId, NoteBookParam noteBookParam, int month,
			int assessmentYear, String tenantId, String tan, String userEmail) throws JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		// Made the Databricks Key Dynamic
		headers.add("Authorization", "Bearer " + this.dataBricksKey);
		headers.add("Content-Type", "application/json");
		RestTemplate restTemplate = new RestTemplate();
		Job job = new Job();

		job.setJob_id(jobId);
		job.setNotebook_params(noteBookParam);

		ObjectMapper objMapper = new ObjectMapper();
		objMapper.writeValueAsString(job);
		if (logger.isInfoEnabled()) {
			logger.info("Note Book Object : {}", objMapper.writeValueAsString(job));
		}
		logger.info(job.getJob_id().toString());
		logger.info("token : {}", this.dataBricksKey);

		String dataBricks = api;
		HttpEntity<Job> entity = new HttpEntity<>(job, headers);
		String response = restTemplate.postForObject(dataBricks, entity, String.class);

		logger.info("Response : {}", response);
		return response;
	}

}
