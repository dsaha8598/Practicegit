package com.ey.in.tds.ingestion.repository.tdsexcess;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;


@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class TdsExcessRepositoryTest {
  @Test
  public void insert() {
   // assertNotNull(tdsExcessRepository.insert(TestUtil.getRandomTdsExcess()));
  }
  
  @Test
  public void get() {
  //  TdsExcess tdsExcess = tdsExcessRepository.insert(TestUtil.getRandomTdsExcess());
  //  assertNotNull(tdsExcessRepository.findById(tdsExcess.getKey()));
  }
  
  @Test
  public void update() {
 /*   TdsExcess tdsExcess = TestUtil.getRandomTdsExcess();
    
    tdsExcessRepository.insert(tdsExcess);
    tdsExcess.setCreatedBy("Test User");
    tdsExcessRepository.insert(tdsExcess);
    assertEquals("Test User", tdsExcessRepository.findById(tdsExcess.getKey()).get().getCreatedBy());
  */}
  
  @Test
  public void getTdsExcesssByAssessmentYearAssessmentMonth() {
    int year = new Random().nextInt(2000);
    int month = new Random().nextInt(12);
  /*  String deductorTan = TestUtil.TAN_NUMBER;
    TdsExcess first = TestUtil.getRandomTdsExcess();
    first.getKey().setAssessmentYear(year);
    first.getKey().setAssessmentMonth(month);
    first.getKey().setDeductorMasterTan(deductorTan);
    TdsExcess second = TestUtil.getRandomTdsExcess();
    second.getKey().setAssessmentYear(year);
    second.getKey().setAssessmentMonth(month);
    second.getKey().setDeductorMasterTan(deductorTan);
    
    tdsExcessRepository.saveAll(Arrays.asList(first, second));
  
    assertTrue(tdsExcessRepository.getTdsExcesssByAssessmentYearAssessmentMonthDeductorTan(year, month, Arrays.asList(TestUtil.TAN_NUMBER), Pagination.DEFAULT).getData().size() > 0);
 */ }
  
  @Test
  public void nullableLong() {
  //  TdsExcess tdsExcess = TestUtil.getRandomTdsExcess();
  //  tdsExcessRepository.insert(tdsExcess);
   // assertNull(tdsExcess.getChallanId());
  }
}
