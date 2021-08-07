package com.ey.in.tds.ingestion.service.advance;

import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;
import com.ey.in.tds.ingestion.jdbc.dao.AdvanceDAO;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class AdvanceServiceTest {

	@Autowired
	private AdvanceDAO advanceRepository;

	@Autowired
	private AdvanceService advanceService;

	@Test
	public void getCountOfTdsCalculationsOfAdvanceForCurrentMonth() {
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
		//TODO NEED TO CHANGE FOR SQL
		//assertTrue(advanceRepository.getCountOfTdsCalculationsOfAdvanceForCurrentMonth(tans, firstDate, lastDate,
			//	isMismatch) >= 0);
		String value = advanceService.getTdsCalculationForProvision(tans, firstDate, lastDate);
		assertTrue(StringUtils.isNotBlank(value));
	}
}
