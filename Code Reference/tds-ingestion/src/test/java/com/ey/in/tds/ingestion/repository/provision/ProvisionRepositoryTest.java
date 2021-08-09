package com.ey.in.tds.ingestion.repository.provision;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class ProvisionRepositoryTest {
  
  @Test
  public void insert() {
 //   assertNotNull(provisionRepository.insert(TestUtil.getRandomProvision()));
  }
  
  @Test
  public void get() {
  //  Provision provision = provisionRepository.insert(TestUtil.getRandomProvision());
  //  assertNotNull(provisionRepository.findById(provision.getKey()));
  }
  
  @Test
  public void update() {
  //  Provision provision = TestUtil.getRandomProvision();
    
  //  provisionRepository.insert(provision);
//    provision.setCreatedBy("Test User");
//    provisionRepository.insert(provision);
 //   assertEquals("Test User", provisionRepository.findById(provision.getKey()).get().getCreatedBy());
  }
  
  @Test
  public void getBatchUploadsByAssessmentYearDeductorTan() {
   /* int year = new Random().nextInt(2000);
    ProvisionDTO first = TestUtil.getRandomProvision();
    first.getKey().setAssessmentYear(year);
    first.setWithholdingSection("10C");
    first.getKey().setDeductorMasterTan(TestUtil.TAN_NUMBER);
    Provision second = TestUtil.getRandomProvision();
    second.getKey().setAssessmentYear(year);
    second.setWithholdingSection("10C");
    second.getKey().setDeductorMasterTan(TestUtil.TAN_NUMBER);
    
    provisionRepository.saveAll(Arrays.asList(first, second));
    
    assertTrue(provisionRepository.getProvisionsByAssessmentYearWithholdingSectionDeductorTan(year, "10C", Arrays.asList(TestUtil.TAN_NUMBER), Pagination.DEFAULT).getData().size() > 0);
 */
  }
  
  @Test
  public void getCountOfTdsCalculationsOfProvisionForCurrentMonth() {
		int month = Calendar.getInstance().get(Calendar.MONTH);
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date monthStartDate = calendar.getTime();
		calendar.add(Calendar.MONTH, 1);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date monthEndDate = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String firstDate = format.format(monthStartDate);
		String lastDate = format.format(monthEndDate);
		boolean isMismatch = true;
		String tans = "AHOY00000J";
//		assertTrue(provisionRepository.getCountOfTdsCalculationsOfProvisionForCurrentMonth(tans, firstDate, lastDate, isMismatch)>=0);
//		String value = provisionService.getTdsCalculationForProvision(tans, firstDate, lastDate);
//		System.out.println(value);		
  }

}
