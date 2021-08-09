package com.ey.in.tds.ingestion.service.uploadrecordprocess;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class UploadRecordProcessServiceTest {
  
  @Autowired
  UploadRecordProcessService uploadRecordProcessService;
  
  @Test
  public void createAndGet() {
   /* UploadRecordProcess uploadRecordProcess = TestUtil.getRandomUploadRecordProcess();
    uploadRecordProcessService.create(uploadRecordProcess);
    assertNotNull(uploadRecordProcessService.get(uploadRecordProcess.getKey()));
 */ }
  
  @Test
  public void getUploadRecordProcessByAssessmentYear() {
 /*   int assessmentYear = new Random().nextInt(2000);
    UploadRecordProcess uploadRecordProcess = TestUtil.getRandomUploadRecordProcess();
    uploadRecordProcess.getKey().setAssessmentYear(assessmentYear);
    uploadRecordProcessService.create(uploadRecordProcess);
    assertTrue(uploadRecordProcessService.getUploadRecordProcessByAssessmentYear(assessmentYear, Pagination.DEFAULT).getData().size() > 0);
  */}
  
}
