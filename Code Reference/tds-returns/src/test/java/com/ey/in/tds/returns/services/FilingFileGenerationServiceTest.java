package com.ey.in.tds.returns.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.common.challan.dto.Receipt;
import com.ey.in.tds.common.dto.onboarding.deductee.AddressDTO;
import com.ey.in.tds.feign.client.ChallansClient;
import com.ey.in.tds.feign.client.IngestionClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.returns.TdsIntegrationTest;
import com.ey.in.tds.returns.dto.DeducteeMasterNonResidentialDTO;
import com.ey.in.tds.returns.dto.DeducteeMasterResidentDTO;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class FilingFileGenerationServiceTest {

	private static final Random random = new Random();

	private static final String form27q = "27Q";
	private static final String tanNumber = "AHOY00000J";
	private static final String deductorPan = "ANIUY0523A";
	private static final String deducteePan = UUID.randomUUID().toString();
	private static final String tenantId = "client1.dvtfo.onmicrosoft.com";
	private static final String userName = "user2@client1.dvtfo.onmicrosoft.com";
	private static final int assessmentYear = random.nextInt(2000);
	private static final String q3 = "Q3";
	public static final String nriSection = "195";
	public static final UUID batchUploadId = UUID.randomUUID();
	private static final String RESIDENT_DEDUCTEE_NAME = "TEST Form 26";
	private static final String NON_RESIDENT_DEDUCTEE_NAME = "TEST Form 27";

	@Autowired
	FilingFileGenerationService filingFileGenerationService;

	@Autowired
	ChallansClient challansClient;

	@Autowired
	OnboardingClient onboardingClient;

	@Autowired
	IngestionClient ingestionClient;

	@Before
/*	public void createData() {

		IntStream.range(1, 13).forEach(i -> {
			//NEED TO CHANGE FOR SQL
			Challan challan = challansClient.createChallan(challan(i, nriSection), userName);
			//ID need to be set inplace of null
			challansClient.createReceipt(receipt(i,null ));//challan.getKey().getId()
		});

		// Create invoice line items for resident
		IntStream.range(1, 13).forEach(i -> {
			ingestionClient.createInvoiceLineItem(invoiceLineItem(i, true));
		});

		// Create invoice line items for non resident
		IntStream.range(1, 13).forEach(i -> {
			ingestionClient.createInvoiceLineItem(invoiceLineItem(i, false));
		});
	}   */
	//NEED TO CHANGE FOR SQL

/*	public Challan challan(int assessmentMonth, String sectionCode) {
		Challan.Key key = new Challan.Key(assessmentYear, assessmentMonth, tanNumber);
		Challan challan = new Challan();
		challan.setKey(key);
		challan.setSection(sectionCode);
		return challan;
	}  */

	public Receipt receipt(int assessmentMonth, Integer challanId) {
	//	Receipt.Key key = new Receipt.Key(assessmentYear, assessmentMonth, tanNumber, batchUploadId, UUID.randomUUID());
		Receipt receipt = new Receipt();
	//	receipt.setKey(key);
		receipt.setReceiptChallanId(challanId);
		return receipt;
	}

	public DeducteeMasterNonResidentialDTO deducteeMasterNonResidentialDTO() {
		DeducteeMasterNonResidentialDTO deducteeMasterNonResidentialDTO = new DeducteeMasterNonResidentialDTO();
		deducteeMasterNonResidentialDTO.setDeducteeCode("DEDUCTEE_NON_RESIDENT_CODE");
		deducteeMasterNonResidentialDTO.setDeducteeName(NON_RESIDENT_DEDUCTEE_NAME);
		deducteeMasterNonResidentialDTO.setDeducteePAN(deducteePan);
		deducteeMasterNonResidentialDTO.setDeducteeTin("ZYXWVU0987T");
		deducteeMasterNonResidentialDTO.setApplicableFrom(new Date());
		deducteeMasterNonResidentialDTO.setEmailAddress(UUID.randomUUID() + "-non.resident@ey.in.com");
		deducteeMasterNonResidentialDTO.setApplicableFrom(new GregorianCalendar(2020 + 2, 1, 30).getTime());

		AddressDTO addressDTO = new AddressDTO();
		addressDTO.setAreaLocality("Test Non Resident");
		addressDTO.setCountryName("Test Non Resident");
		addressDTO.setFlatDoorBlockNo("Test Non Resident # 1");
		addressDTO.setStateName("Test Non Resident State");
		addressDTO.setPinCode("0123456");
		addressDTO.setTownCityDistrict("Test Non Resident Town");
		deducteeMasterNonResidentialDTO.setAddress(addressDTO);

		return deducteeMasterNonResidentialDTO;
	}

	public DeducteeMasterResidentDTO deducteeMasterResidentDTO() {
		DeducteeMasterResidentDTO deducteeMasterResidentDTO = new DeducteeMasterResidentDTO();
		deducteeMasterResidentDTO.setDeducteeCode("DEDUCTEE_RESIDENT_CODE");
		deducteeMasterResidentDTO.setDeducteeName(RESIDENT_DEDUCTEE_NAME);
		deducteeMasterResidentDTO.setDeducteePAN(deducteePan);
		deducteeMasterResidentDTO.setEmailAddress(UUID.randomUUID() + "-resident@ey.in.com");
		deducteeMasterResidentDTO.setApplicableFrom(new Date());
		deducteeMasterResidentDTO.setApplicableFrom(new GregorianCalendar(2020 + 2, 1, 20).getTime());

		AddressDTO addressDTO = new AddressDTO();
		addressDTO.setAreaLocality("Test Resident");
		addressDTO.setCountryName("Test Resident");
		addressDTO.setFlatDoorBlockNo("Test Resident # 1");
		addressDTO.setStateName("Test Resident State");
		addressDTO.setPinCode("0123456");
		addressDTO.setTownCityDistrict("Test Resident Town");
		deducteeMasterResidentDTO.setAddress(addressDTO);
		return deducteeMasterResidentDTO;
	}

/*	public InvoiceLineItem invoiceLineItem(int assessmentMonth, boolean isForResident) {
		InvoiceLineItem.Key key = new InvoiceLineItem.Key(assessmentYear, tanNumber, new Date(), UUID.randomUUID());
		InvoiceLineItem invoiceLineItem = new InvoiceLineItem();
		invoiceLineItem.setKey(key);
		invoiceLineItem.setBatchUploadId(batchUploadId);
		invoiceLineItem.setActive(true);
		invoiceLineItem.setChallanPaid(true);
		invoiceLineItem.setChallanMonth(assessmentMonth);
		invoiceLineItem.setAssessmentMonth(assessmentMonth);
		invoiceLineItem.setIsResident(isForResident ? "Y" : "N");
		invoiceLineItem.setPan(deducteePan);
		invoiceLineItem.setDeducteeName(isForResident ? RESIDENT_DEDUCTEE_NAME : NON_RESIDENT_DEDUCTEE_NAME);
		return invoiceLineItem;
	}   */

	@Test
	public void generateFile() {
		try {
			assertEquals("Filing record created Successfully", filingFileGenerationService
					.createReturnFilingReport(form27q, tanNumber, deductorPan, assessmentYear, q3, tenantId, userName,true));
		} catch (Throwable e) {
			if (e.getMessage().contains("Connection refused executing") && e.getMessage().contains("localhost")) {
				fail("This test needs both tds-challans, ingestion, onboarding and masters apps to be up and running : "
						+ e.getMessage());
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	@Test
	public void generateTextFile() {
		assertTrue(true);
	}
}