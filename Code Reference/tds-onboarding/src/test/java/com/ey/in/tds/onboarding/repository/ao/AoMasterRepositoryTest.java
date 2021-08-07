package com.ey.in.tds.onboarding.repository.ao;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.onboarding.TdsIntegrationTest;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class AoMasterRepositoryTest {
 /* 
  @Autowired
  AoMasterRepository aoMasterRepository;
  
  @Test
  public void insert() {
    assertNotNull(aoMasterRepository.insert(TestUtil.getRandomAoMaster()));
  }
  
  @Test
  public void get() {
    AoMaster aoMaster = aoMasterRepository.insert(TestUtil.getRandomAoMaster());
    assertNotNull(aoMasterRepository.findById(aoMaster.getKey()));
  }
  
  @Test
  public void update() {
    AoMaster aoMaster = TestUtil.getRandomAoMaster();
    
    aoMasterRepository.insert(aoMaster);
    aoMaster.setCreatedBy("Test User");
    aoMasterRepository.insert(aoMaster);
    assertEquals("Test User", aoMasterRepository.findById(aoMaster.getKey()).get().getCreatedBy());
  }
  
  @Test
  public void getAoMastersByAssessmentYear() {
    int year = new Random().nextInt(2000);
    int month = new Random().nextInt(12);
    String tan = "ZAQWS1234X";
    AoMaster first = TestUtil.getRandomAoMaster();
    first.getKey().setAssessmentYear(year);
    first.getKey().setTanNumber(tan);
    AoMaster second = TestUtil.getRandomAoMaster();
    second.getKey().setAssessmentYear(year);
    second.getKey().setTanNumber(tan);
    
    aoMasterRepository.saveAll(Arrays.asList(first, second));
  
    assertTrue(aoMasterRepository.getAoMastersByAssessmentYearAssessmentMonthDeductorMasterTan(year, month, Arrays.asList(tan), Pagination.DEFAULT).getData().size() > 0);
  }  */
}
