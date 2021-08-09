package com.ey.in.tds.ingestion.service.tdsexcess;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class TdsExcessServiceTest {
  
  @Autowired
  TdsExcessService tdsExcessService;
  
  @Test
  public void createAndGet() {
/*    TdsExcess tdsExcess = TestUtil.getRandomTdsExcess();
    tdsExcessService.create(tdsExcess);
    assertNotNull(tdsExcessService.get(tdsExcess.getKey()));
*/  }
  
  @Test
  public void getTdsExcessByAssessmentYear() {
    int assessmentYear = new Random().nextInt(2000);
    int month = new Random().nextInt(12);
 /*   String deductorTan = TestUtil.TAN_NUMBER;
    TdsExcess tdsExcess = TestUtil.getRandomTdsExcess();
    tdsExcess.getKey().setAssessmentYear(assessmentYear);
    tdsExcess.getKey().setAssessmentMonth(month);
    tdsExcess.getKey().setDeductorMasterTan(deductorTan);
    tdsExcessService.create(tdsExcess);
    assertTrue(tdsExcessService.getTdsExcesssByAssessmentYearAssessmentMonthDeductorTan(assessmentYear, month, Arrays.asList(TestUtil.TAN_NUMBER), Pagination.DEFAULT).getData().size() > 0);
 */ }
}