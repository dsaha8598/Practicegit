package com.ey.in.tds.ingestion.service.ldcutilization;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;
import com.ey.in.tds.ingestion.service.ldc.LdcUtilizationService;
import com.ey.in.tds.ingestion.util.TestUtil;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class LdcUtilizationServiceTest {
  
  @Autowired
  LdcUtilizationService ldcUtilizationService;
  
  @Test   //TODO NEED TO CHANGE FOR SQL
  public void createAndGet() {
  //  LdcUtilization ldcUtilization = TestUtil.getRandomLdcUtilization();
   // ldcUtilizationService.create(ldcUtilization);
   // assertNotNull(ldcUtilizationService.get(ldcUtilization.getKey()));
  }
  
  @Test
  public void getLdcUtilizationByAssessmentYearAssessmentMonth() {
    
    int assessmentYear = new Random().nextInt(2000);
    int assessmentMonth = new Random().nextInt(12);
    String deductorTan = TestUtil.TAN_NUMBER;
  //  LdcUtilization ldcUtilization = TestUtil.getRandomLdcUtilization();
  //  ldcUtilization.getKey().setAssessmentYear(assessmentYear);
  //  ldcUtilization.getKey().setAssessmentMonth(assessmentMonth);
  //  ldcUtilization.getKey().setDeductorMasterTan(deductorTan);
  //  ldcUtilizationService.create(ldcUtilization);
  //  assertTrue(ldcUtilizationService.getLdcUtilizationsByAssessmentYearMonthDeductorTan(assessmentYear, assessmentMonth, Arrays.asList(TestUtil.TAN_NUMBER), Pagination.DEFAULT).getData().size() > 0);
  }
  
}