package com.ey.in.tds.ingestion.repository.tdsinterestcalculation;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;


@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class TdsInterestCalculationRepositoryTest {
  
  
  @Test
  public void insert() {
  //  assertNotNull(tdsInterestCalculationRepository.insert(TestUtil.getRandomTdsInterestCalculation()));
  }
  
  @Test
  public void get() {
 //   TdsInterestCalculation tdsInterestCalculation = tdsInterestCalculationRepository.insert(TestUtil.getRandomTdsInterestCalculation());
 //   assertNotNull(tdsInterestCalculationRepository.findById(tdsInterestCalculation.getKey()));
  }
  
  @Test
  public void update() {
 /*   TdsInterestCalculation tdsInterestCalculation = TestUtil.getRandomTdsInterestCalculation();
    
    tdsInterestCalculationRepository.insert(tdsInterestCalculation);
    tdsInterestCalculation.setCreatedBy("Test User");
    tdsInterestCalculationRepository.insert(tdsInterestCalculation);
    assertEquals("Test User", tdsInterestCalculationRepository.findById(tdsInterestCalculation.getKey()).get().getCreatedBy());
 */ }
  
  @Test
  public void getTdsInterestCalculationsByAssessmentYearAssessmentMonth() {
    int year = new Random().nextInt(2000);
    int month = new Random().nextInt(12);
/*    TdsInterestCalculation first = TestUtil.getRandomTdsInterestCalculation();
    first.getKey().setAssessmentYear(year);
    first.getKey().setAssessmentMonth(month);
    TdsInterestCalculation second = TestUtil.getRandomTdsInterestCalculation();
    second.getKey().setAssessmentYear(year);
    second.getKey().setAssessmentMonth(month);
    
    tdsInterestCalculationRepository.saveAll(Arrays.asList(first, second));
  
    assertTrue(tdsInterestCalculationRepository.getTdsInterestCalculationsByAssessmentYearAssessmentMonth(year, month, Pagination.DEFAULT).getData().size() > 0);
 */ }
}
