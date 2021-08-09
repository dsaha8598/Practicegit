package com.ey.in.tds.onboarding.util;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;

import com.ey.in.tds.common.dto.AoMasterDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMaster;
import com.ey.in.tds.onboarding.service.deductee.DeducteeMasterService;

public class TestUtil {

	@Autowired
	static DeducteeMasterService deducteeMasterService;
	
/*	public static AoMaster getRandomAoMaster() {

		AoMaster.Key key = new AoMaster.Key(new Random().nextInt(2000), "ABCDE1234T", "Deductee name", UUID.randomUUID());
		AoMaster aoMaster = new AoMaster();
		aoMaster.setKey(key);

		return aoMaster;
	}  */

	public static AoMasterDTO getRandomAoMasterDTO() {
		AoMasterDTO aoMasterDTO = new AoMasterDTO();

		aoMasterDTO.setDeducteeName("Test E2E");
		aoMasterDTO.setDeducteePan("ABCDE1234F");
		aoMasterDTO.setDeductorTan("ZAQWS1234X");
		aoMasterDTO.setAmount(new BigDecimal(100));
		aoMasterDTO.setApplicableFrom(new Date());
		aoMasterDTO.setApplicableTo(new Date());
		aoMasterDTO.setAoCertificateNumber("ABCDEFG");
		aoMasterDTO.setLimitUtilised(new BigDecimal(100.0));
		aoMasterDTO.setNatureOfPaymentSection("Challan Payment");
		aoMasterDTO.setAoRate(new BigDecimal(10.0));

		return aoMasterDTO;
	}

	public static LdcMaster getRandomLdcMaster() {

	//	LdcMaster.Key key = new LdcMaster.Key(new Random().nextInt(2000), "ABCDE1234F", "ZAQWS1234E", UUID.randomUUID());
		LdcMaster ldcMaster = new LdcMaster();
		ldcMaster.setLdcMasterID(new Random().nextInt(10) );
		ldcMaster.setPan( "ABCDE1234F");
		ldcMaster.setTanNumber("ZAQWS1234E");
		ldcMaster.setAssessmentYear(new Random().nextInt(2000));

		ldcMaster.setAmount(BigDecimal.valueOf(Double.MAX_VALUE));
		return ldcMaster;
	}

	public static String getE2EURL(String uri) {
		return "http://localhost:8005/api/onboarding" + (uri.startsWith("/") ? uri : "/" + uri);
	}

}
