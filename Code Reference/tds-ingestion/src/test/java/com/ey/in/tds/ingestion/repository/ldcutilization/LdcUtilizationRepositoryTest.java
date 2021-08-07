package com.ey.in.tds.ingestion.repository.ldcutilization;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;


@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class LdcUtilizationRepositoryTest {
	
	//TODO NEED TO CHANGE FOR SQL
  
/*  @Autowired
  LdcUtilizationRepository ldcUtilizationRepository;
  
  @Test
  public void insert() {
    assertNotNull(ldcUtilizationRepository.insert(TestUtil.getRandomLdcUtilization()));
  }
  
  @Test
  public void get() {
    LdcUtilization ldcUtilization = ldcUtilizationRepository.insert(TestUtil.getRandomLdcUtilization());
    assertNotNull(ldcUtilizationRepository.findById(ldcUtilization.getKey()));
  }
  
  @Test
  public void update() {
    LdcUtilization ldcUtilization = TestUtil.getRandomLdcUtilization();
    
    ldcUtilizationRepository.insert(ldcUtilization);
    ldcUtilization.setCreatedBy("Test User");
    ldcUtilizationRepository.insert(ldcUtilization);
    assertEquals("Test User", ldcUtilizationRepository.findById(ldcUtilization.getKey()).get().getCreatedBy());
  }
  
  @Test
  public void getLdcUtilizationsByAssessmentYearMonthDeductorTan() {
    int year = new Random().nextInt(2000);
    int month = new Random().nextInt(12);
    String deductorTan = TestUtil.TAN_NUMBER;
    LdcUtilization first = TestUtil.getRandomLdcUtilization();
    first.getKey().setAssessmentYear(year);
    first.getKey().setAssessmentMonth(month);
    first.getKey().setDeductorMasterTan(deductorTan);
    LdcUtilization second = TestUtil.getRandomLdcUtilization();
    second.getKey().setAssessmentYear(year);
    second.getKey().setAssessmentMonth(month);
    second.getKey().setDeductorMasterTan(deductorTan);
    ldcUtilizationRepository.saveAll(Arrays.asList(first, second));
  
    assertTrue(ldcUtilizationRepository.getLdcUtilizationsByAssessmentYearMonthDeductorTan(year, month, Arrays.asList(TestUtil.TAN_NUMBER), Pagination.DEFAULT).getData().size() > 0);
  }  */
}
