package com.ey.in.tds.ingestion.service.batchupload;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.ingestion.TdsIntegrationTest;
import com.ey.in.tds.ingestion.util.TestUtil;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class BatchUploadServiceTest {

  @Autowired
  BatchUploadService batchUploadService;

	@Test
	public void createAndGet() {
		BatchUpload batchUpload = TestUtil.getRandomBatchUpload();
		batchUploadService.create(batchUpload);
		assertNotNull(batchUploadService.get(batchUpload.getAssessmentYear(), batchUpload.getDeductorMasterTan(),
				batchUpload.getUploadType(), batchUpload.getBatchUploadID()));
	}

  @Test
  public void getBatchUploadByAssessmentYear() {
    int assessmentYear = new Random().nextInt(2000);
    BatchUpload batchUpload = TestUtil.getRandomBatchUpload();
    batchUpload.setAssessmentYear(assessmentYear);
    batchUploadService.create(batchUpload);
    assertTrue(batchUploadService.getBatchUploadByAssessmentYear(assessmentYear, Pagination.DEFAULT).size() > 0);
  }

  @Test
  public void getBatchUploadByAssessmentYearAssessmentMonth() {
  
    int assessmentYear = new Random().nextInt(2000);
    BatchUpload batchUpload = TestUtil.getRandomBatchUpload();
    batchUpload.setAssessmentYear(assessmentYear);
    batchUpload.setDeductorMasterTan(TestUtil.TAN_NUMBER);
    batchUploadService.create(batchUpload);
    assertTrue(batchUploadService.getBatchUploadByAssessmentYearDeductorTan(assessmentYear, Arrays.asList(TestUtil.TAN_NUMBER), Pagination.DEFAULT).size() > 0);
  }

}
