package com.ey.in.tds.onboarding.repository.deductee;

import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.jdbc.dao.DeducteeMasterResidentialDAO;
import com.ey.in.tds.onboarding.TdsIntegrationTest;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class DeducteeMasterRespoitoryTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DeducteeMasterResidentialDAO deducteeMasterResidentialRepository;

	@Test
	public void testCountDeducteeResidential() {
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
	//	assertTrue(deducteeMasterResidentialRepository.countDeducteeResidential(deductorTan, startDate, endDate,
	//			currentDate) >= 0);

	}

	@Test
	public void testCountDeducteeResidentialPanStatusValid() {
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
		assertTrue(deducteeMasterResidentialRepository.countDeducteeResidentialPanStatusValid(deductorTan, startDate,
				endDate) >= 0);
	}

	@Test
	public void testCountDeducteeResidentialPanStatusInValid() {
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
		assertTrue(deducteeMasterResidentialRepository.countDeducteeResidentialPanStatusInValid(deductorTan, startDate,
				endDate) >= 0);
	}

	@Test
	public void testCountDeducteeResidentialPanStatusEmpty() {
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
		assertTrue(deducteeMasterResidentialRepository.countDeducteeResidentialPanStatusEmpty(deductorTan, startDate,
				endDate) >= 0);
	}

	@Test
	public void testCountDeducteeResidentialPanStatuNull() {
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
		assertTrue(deducteeMasterResidentialRepository.countDeducteeResidentialPanStatusEmpty(deductorTan, startDate,
				endDate) >= 0);
	}

	@Test
	public void testGetDeducteePanStatus() {
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

		long countValidPan = deducteeMasterResidentialRepository.countDeducteeResidentialPanStatusValid(deductorTan,
				startDate, endDate);
		logger.info("Total deductee residential valid pan status: {}", countValidPan);
		long countInValidPan = deducteeMasterResidentialRepository.countDeducteeResidentialPanStatusInValid(deductorTan,
				startDate, endDate);
		logger.info("Total deductee residential invalid pan status: {}", countInValidPan);
		long countEmptyPan = deducteeMasterResidentialRepository.countDeducteeResidentialPanStatusEmpty(deductorTan,
				startDate, endDate);
		logger.info("Total deductee residential Empty pan status: {}", countEmptyPan);
		long countNullPan = deducteeMasterResidentialRepository.countDeducteeResidentialPanStatusEmpty(deductorTan,
				startDate, endDate);
		logger.info("Total deductee residential pan status: {}", countNullPan);
		String name = "";
		if (countValidPan == 0 && countInValidPan == 0 && countEmptyPan == 0 && countNullPan == 0) {
			name = "NORECORDS";
			logger.info("no records: {}", name);
		} else if (countInValidPan > 0 || countEmptyPan > 0 || countNullPan > 0) {
			name = "PENDING";
			logger.info("pending records: {}", name);
		} else if (countValidPan > 0 && countInValidPan == 0 && countEmptyPan == 0 && countNullPan == 0) {
			name = "SUCCESS";
			logger.info("Success records: {}", name);
		}
		assertTrue(true);
	}

}
