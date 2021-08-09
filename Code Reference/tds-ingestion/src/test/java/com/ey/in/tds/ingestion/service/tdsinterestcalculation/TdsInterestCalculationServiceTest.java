package com.ey.in.tds.ingestion.service.tdsinterestcalculation;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class TdsInterestCalculationServiceTest {
  
  @Autowired
  TdsInterestCalculationService tdsInterestCalculationService;
  
  @Test
  public void createAndGet() {
  /*  TdsInterestCalculation tdsInterestCalculation = TestUtil.getRandomTdsInterestCalculation();
    tdsInterestCalculationService.create(tdsInterestCalculation);
    assertNotNull(tdsInterestCalculationService.get(tdsInterestCalculation.getKey()));
 */ }
  
  @Test
  public void getTdsInterestCalculationByAssessmentYear() {
    int assessmentYear = new Random().nextInt(2000);
    int month = new Random().nextInt(12);
 /*   TdsInterestCalculation tdsInterestCalculation = TestUtil.getRandomTdsInterestCalculation();
    tdsInterestCalculation.getKey().setAssessmentYear(assessmentYear);
    tdsInterestCalculation.getKey().setAssessmentMonth(month);
    tdsInterestCalculationService.create(tdsInterestCalculation);
    assertTrue(tdsInterestCalculationService.getTdsInterestCalculationByAssessmentYear(assessmentYear, month, Pagination.DEFAULT).getData().size() > 0);
 */ }
  
}