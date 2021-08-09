package com.ey.in.tds.ingestion.repository.generalledger;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class GeneralLedgerRepositoryTest {
/*
  
  @Test
  public void insert() {
    assertNotNull(generalLedgerRepository.insert(TestUtil.getRandomGeneralLedger()));
  }
  
  @Test
  public void get() {
    GeneralLedger generalLedger = generalLedgerRepository.insert(TestUtil.getRandomGeneralLedger());
    assertNotNull(generalLedgerRepository.findById(generalLedger.getKey()));
  }
  
  @Test
  public void update() {
    GeneralLedger generalLedger = TestUtil.getRandomGeneralLedger();
    
    generalLedgerRepository.insert(generalLedger);
    generalLedger.setCreatedBy("Test User");
    generalLedgerRepository.insert(generalLedger);
    assertEquals("Test User", generalLedgerRepository.findById(generalLedger.getKey()).get().getCreatedBy());
  }
  
  @Test
  public void getGeneralLedgersByAssessmentYear() {
    int year = new Random().nextInt(2000);
    GeneralLedger first = TestUtil.getRandomGeneralLedger();
    first.getKey().setAssessmentYear(year);
    GeneralLedger second = TestUtil.getRandomGeneralLedger();
    second.getKey().setAssessmentYear(year);
    
    generalLedgerRepository.saveAll(Arrays.asList(first, second));
    
    assertTrue(generalLedgerRepository.getGeneralLedgerByAssessmentYear(year, Pagination.DEFAULT).getData().size() > 0);
  }
  
  @Test
  public void getGeneralLedgersByAssessmentYearAssessmentMonth() {
    int year = new Random().nextInt(2000);
    int month = new Random().nextInt(12);
    GeneralLedger first = TestUtil.getRandomGeneralLedger();
    first.getKey().setAssessmentYear(year);
    first.setAssessmentMonth(month);
    GeneralLedger second = TestUtil.getRandomGeneralLedger();
    second.getKey().setAssessmentYear(year);
    second.setAssessmentMonth(month);
    
    generalLedgerRepository.saveAll(Arrays.asList(first, second));
    
    assertTrue(generalLedgerRepository.getGeneralLedgerByAssessmentYearAssessmentMonth(year, month, Pagination.DEFAULT).getData().size() > 0);
  }  */
}