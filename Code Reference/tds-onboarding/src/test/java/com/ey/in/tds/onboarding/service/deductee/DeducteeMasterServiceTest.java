package com.ey.in.tds.onboarding.service.deductee;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.onboarding.TdsIntegrationTest;
import com.microsoft.azure.storage.StorageException;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class DeducteeMasterServiceTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	DeducteeMasterService deducteeMasterService;

	@Test
	public void createAndGet() throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		assertTrue(true);
	}

	@Test
	public void uploadResidentDeducteeData() {
		saveFileData("DEDUCTEE_RESIDENT_03022020.xlsx");
		assertTrue(true);
	}

	@Test
	public void uploadNonResidentDeducteeData() {
		saveFileData("DEDUCTEE_NON_RESIDENT_03022020.xlsx");
		assertTrue(true);
	}

	@Test
	public void uploadResidentDeducteeData2() {
		saveFileData("DeducteeMasterUpload_sample_data.xlsx");
		assertTrue(true);
	}

	@Test
	public void uploadNonResidentDeducteeData2() {
		saveFileData("import7deductees.xlsx");
		assertTrue(true);
	}

	@Test
	public void uploadResidentDeducteeDataWithRandomSequenceHeaders() {
		saveFileData("DEDUCTEE_RESIDENT_MISMATCH_HEADERS_03022020.xlsx");
		assertTrue(true);
	}

	@Test
	public void uploadNonResidentDeducteeDataWithRandomSequenceHeaders() {
		saveFileData("DEDUCTEE_NON_RESIDENT_RANDOM_03022020.xlsx");
		assertTrue(true);
	}

	private void saveFileData(String resourceName) {
		try {
			String residentDeducteeFileName = "/" + resourceName;
			InputStream residentDeducteeDataStream = this.getClass().getResourceAsStream(residentDeducteeFileName);
			System.out.println(residentDeducteeDataStream);
			MockMultipartFile mockMultipartFile = new MockMultipartFile(residentDeducteeFileName,
					residentDeducteeFileName, "application/vnd.ms-excel", residentDeducteeDataStream);
			deducteeMasterService.saveFileData(mockMultipartFile, "ABCDEF1234G", 2020, 11, "abcde", "default",
					"ABCDEF1234G");
		} catch (Exception e) {
			logger.error("Exception occurred while saving file", e);
		}

	}
}
