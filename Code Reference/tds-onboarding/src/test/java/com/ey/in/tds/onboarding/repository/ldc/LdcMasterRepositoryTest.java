package com.ey.in.tds.onboarding.repository.ldc;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.onboarding.TdsIntegrationTest;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class LdcMasterRepositoryTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//TODO NEED TO CHANGE FOR SQL
/*	@Autowired
	LdcMasterRepository ldcMasterRepository;

	@Test
	public void insert() {
		assertNotNull(ldcMasterRepository.insert(TestUtil.getRandomLdcMaster()));
	}

	@Test
	public void get() {
		LdcMaster ldcMaster = ldcMasterRepository.insert(TestUtil.getRandomLdcMaster());
		assertNotNull(ldcMasterRepository.findById(ldcMaster.getKey()));
	}

	@Test
	public void update() {
		LdcMaster ldcMaster = TestUtil.getRandomLdcMaster();

		ldcMasterRepository.insert(ldcMaster);
		ldcMaster.setCreatedBy("Test User");
		ldcMasterRepository.insert(ldcMaster);
		assertEquals("Test User", ldcMasterRepository.findById(ldcMaster.getKey()).get().getCreatedBy());
	}

	@Test
	public void getLdcMastersByAssessmentYearPanDeductorTan() {
		int year = new Random().nextInt(2000);
		LdcMaster first = TestUtil.getRandomLdcMaster();
		first.getKey().setAssessmentYear(year);
		LdcMaster second = TestUtil.getRandomLdcMaster();
		second.getKey().setAssessmentYear(year);

		ldcMasterRepository.saveAll(Arrays.asList(first, second));

		assertTrue(ldcMasterRepository.getLdcMastersByAssessmentYearPanDeductorTan(year, first.getKey().getPan(),
			 	first.getKey().getTanNumber(), Pagination.DEFAULT).getData().size() > 0);
	}   */

	@Test
	public void testGetCountOfLdcMasterRecords() {
		int month = Calendar.getInstance().get(Calendar.MONTH);
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date monthStartDate = calendar.getTime();
		calendar.add(Calendar.MONTH, 1);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date monthEndDate = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = format.format(monthStartDate);
		String endDate = format.format(monthEndDate);
		String currentDate = format.format(new Date());
		String deductorTan = "AHOY00000J";
		//TODO NEED TO CHANGE FOR SQL
		//assertTrue(ldcMasterRepository.getCountOfLdcMasterRecords(deductorTan, startDate, endDate, currentDate) >= 0);
	}

	@Test
	public void testGetCountOfLdcMasterInValidStatus() {
		int month = Calendar.getInstance().get(Calendar.MONTH);
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date monthStartDate = calendar.getTime();
		calendar.add(Calendar.MONTH, 1);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date monthEndDate = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = format.format(monthStartDate);
		String endDate = format.format(monthEndDate);
		String deductorTan = "AHOY00000J";
		//TODO NEED TO CHANGE FOR SQLS
		//assertTrue(ldcMasterRepository.getCountOfLdcMasterInValidStatus(deductorTan, startDate, endDate) >= 0);
	}

	@Test
	public void testGetCountOfLdcMasterValidStatus() {
		int month = Calendar.getInstance().get(Calendar.MONTH);
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date monthStartDate = calendar.getTime();
		calendar.add(Calendar.MONTH, 1);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date monthEndDate = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = format.format(monthStartDate);
		String endDate = format.format(monthEndDate);
		String deductorTan = "AHOY00000J";
		//TODO NEED TO CHANGE FOR SQL
	//	assertTrue(ldcMasterRepository.getCountOfLdcMasterValidStatus(deductorTan, startDate, endDate) >= 0);
	}

	@Test
	public void testGetCountOfLdcMasterEmptyStatus() {
		int month = Calendar.getInstance().get(Calendar.MONTH);
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date monthStartDate = calendar.getTime();
		calendar.add(Calendar.MONTH, 1);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date monthEndDate = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = format.format(monthStartDate);
		String endDate = format.format(monthEndDate);
		String deductorTan = "AHOY00000J";
		//TODO NEED TO CHANGE FOR SQL
	//	assertTrue(ldcMasterRepository.getCountOfLdcMasterEmptyStatus(deductorTan, startDate, endDate) >= 0);
	}
	
	@Test
	public void testGetLdcMasterStatus() {
		
		int month = Calendar.getInstance().get(Calendar.MONTH);
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date monthStartDate = calendar.getTime();
		calendar.add(Calendar.MONTH, 1);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date monthEndDate = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = format.format(monthStartDate);
		String endDate = format.format(monthEndDate);
		String deductorTan = "AHOY00000J";
		
	/*	long countValidStatus = ldcMasterRepository.getCountOfLdcMasterValidStatus(deductorTan, startDate, endDate);
		logger.info("Total ldc master valid status: {}", countValidStatus);
		long countInValidStatus = ldcMasterRepository.getCountOfLdcMasterInValidStatus(deductorTan, startDate, endDate);
		logger.info("Total ldc master Invalid status: {}", countInValidStatus);
		long countEmptyStatus = ldcMasterRepository.getCountOfLdcMasterEmptyStatus(deductorTan, startDate, endDate);
		logger.info("Total ldc master empty status: {}", countEmptyStatus);  
		String name = "";
		if (countValidStatus == 0 && countInValidStatus == 0 && countEmptyStatus == 0 ) {
			name = "NORECORDS";
			logger.info("no records: {}", name);
		} else if (countInValidStatus > 0 || countEmptyStatus > 0) {
			name = "PENDING";
			logger.info("pending records: {}", name);
		} else if (countValidStatus > 0 && countInValidStatus == 0 && countEmptyStatus == 0 ) {
			name = "SUCCESS";
			logger.info("success records: {}", name);
		}
		assertTrue(StringUtils.isNotBlank(name));   */
	}
}
