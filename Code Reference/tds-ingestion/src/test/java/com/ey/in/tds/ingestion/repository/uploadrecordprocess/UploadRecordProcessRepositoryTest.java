package com.ey.in.tds.ingestion.repository.uploadrecordprocess;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;


@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class UploadRecordProcessRepositoryTest {
  @Test
  public void insert() {
   // assertNotNull(uploadRecordProcessRepository.insert(TestUtil.getRandomUploadRecordProcess()));
  }
  
  @Test
  public void get() {
  /*  UploadRecordProcess uploadRecordProcess = uploadRecordProcessRepository.insert(TestUtil.getRandomUploadRecordProcess());
   
    assertNotNull(uploadRecordProcessRepository.findById(uploadRecordProcess.getKey()));
  */}
  
  @Test
  public void update() {
   /* UploadRecordProcess uploadRecordProcess = uploadRecordProcessRepository.insert(TestUtil.getRandomUploadRecordProcess());
    assertNull(uploadRecordProcess.getCreatedBy());
    uploadRecordProcess.setCreatedBy("Test User");
    uploadRecordProcessRepository.insert(uploadRecordProcess);
    assertEquals("Test User", uploadRecordProcessRepository.findById(uploadRecordProcess.getKey()).get().getCreatedBy());
  */
  }
  
  @Test
  public void getUploadRecordProcesssByAssessmentYear() {
    int year = new Random().nextInt(2000);
  /*  UploadRecordProcess first = TestUtil.getRandomUploadRecordProcess();
    first.getKey().setAssessmentYear(year);
    UploadRecordProcess second = TestUtil.getRandomUploadRecordProcess();
    second.getKey().setAssessmentYear(year);
    
    uploadRecordProcessRepository.saveAll(Arrays.asList(first, second));
  
    assertTrue(uploadRecordProcessRepository.getUploadRecordProcessByAssessmentYear(year, Pagination.DEFAULT).getData().size() > 0);
  */}
  
  @Test
  public void nullableInt() {
  /*  UploadRecordProcess uploadRecordProcess = TestUtil.getRandomUploadRecordProcess();
    uploadRecordProcessRepository.insert(uploadRecordProcess);
    
   assertNull(uploadRecordProcess.getRowNumber());
  */
  }
}
