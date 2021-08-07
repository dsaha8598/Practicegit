package com.ey.in.tds.ingestion.repository.batchupload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.ingestion.TdsIntegrationTest;
import com.ey.in.tds.ingestion.util.TestUtil;


@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class BatchUploadRepositoryTest {
  @Autowired
  BatchUploadDAO batchUploadDAO;
  
  @Test
  public void insert() {
    assertNotNull(batchUploadDAO.save(TestUtil.getRandomBatchUpload()));
  }
  
  @Test
  public void get() {
    BatchUpload batchUpload = batchUploadDAO.save(TestUtil.getRandomBatchUpload());
    int year = batchUpload.getAssessmentYear();
    String tan = batchUpload.getDeductorMasterTan();
    String type = batchUpload.getUploadType();
    Integer id = batchUpload.getBatchUploadID();
    assertNotNull(batchUploadDAO.findById(year, tan ,type, id));
  }
  
  @Test
  public void update() {
    BatchUpload batchUpload = TestUtil.getRandomBatchUpload();
    
    batchUploadDAO.save(batchUpload);
    batchUpload.setCreatedBy("Test User");
    batchUploadDAO.save(batchUpload);
    int year = batchUpload.getAssessmentYear();
    String tan = batchUpload.getDeductorMasterTan();
    String type = batchUpload.getUploadType();
    Integer id = batchUpload.getBatchUploadID();
	assertEquals("Test User", batchUploadDAO.findById(year, tan, type, id).get(0).getCreatedBy());
  }
  
  @Test
  public void getBatchUploadsByAssessmentYearDeductorTan() {
    int year = new Random().nextInt(2000);
    BatchUpload first = TestUtil.getRandomBatchUpload();
    first.setAssessmentYear(year);
    first.setDeductorMasterTan(TestUtil.TAN_NUMBER);
    BatchUpload second = TestUtil.getRandomBatchUpload();
    second.setAssessmentYear(year);
    second.setDeductorMasterTan(TestUtil.TAN_NUMBER);
    
  //  batchUploadDAO.saveAll(Arrays.asList(first, second));
    
    assertTrue(batchUploadDAO.getBatchUploadsByAssessmentYearDeductorTan(year, Arrays.asList(TestUtil.TAN_NUMBER), Pagination.UNPAGED).size() > 0);
  }
}