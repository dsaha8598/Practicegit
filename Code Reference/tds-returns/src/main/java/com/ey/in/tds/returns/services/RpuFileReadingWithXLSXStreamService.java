package com.ey.in.tds.returns.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.BorderType;
import com.aspose.cells.CellBorderType;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.FilingSectionCode;
import com.ey.in.tds.common.domain.FilingStateCode;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.jdbc.returns.dao.FilingFilesDAO;
import com.ey.in.tds.common.returns.jdbc.dto.FilingFiles;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.FilingFileRemittance;
import com.ey.in.tds.feign.client.ChallansClient;
import com.ey.in.tds.feign.client.IngestionClient;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.jdbc.dao.FilingStatusDAO;
import com.ey.in.tds.returns.domain.BatchHeaderErrorBean;
import com.ey.in.tds.returns.domain.FilingBatchHeaderBean;
import com.ey.in.tds.returns.domain.FilingChallanDetailBean;
import com.ey.in.tds.returns.domain.FilingChallanErrorBean;
import com.ey.in.tds.returns.domain.FilingDeducteeDetailBean;
import com.ey.in.tds.returns.domain.FilingDeducteeErrorBean;
import com.ey.in.tds.returns.domain.FilingFileBean;
import com.ey.in.tds.returns.domain.FilingHeaderBean;
import com.monitorjbl.xlsx.StreamingReader;

import de.siegmar.fastcsv.reader.CsvRow;

@Service
public class RpuFileReadingWithXLSXStreamService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BlobStorage blob;

	@Autowired
	protected ChallansClient challansClient;

	@Autowired
	protected MastersClient mastersClient;

	@Autowired
	protected OnboardingClient onboardingClient;

	@Autowired
	protected IngestionClient ingestionClient;

	@Autowired
	protected FilingFilesDAO filingFileDAO;

	@Autowired
	protected FilingStatusDAO filingStatusDAO;


	public boolean checkNull(CsvRow row) throws IllegalAccessException {
		boolean isnull = true;
		isnull = row.getFields().stream().anyMatch(n -> StringUtils.isNotBlank(n));
		return isnull;
	}

	private String getFormattedValue(BigDecimal value) {
		String formattedValue = "0.00";
		if (value.compareTo(BigDecimal.ZERO) > 0) {
			formattedValue = (value.setScale(2, RoundingMode.UP)).toString();
		}
		return formattedValue;
	}

	private String getCellValue(Sheet sheet, Cell cell) {

		if (CellType.STRING.equals(cell.getCellType())) {
			return cell.getStringCellValue();
		} else if (CellType.NUMERIC.equals(cell.getCellType())) {
			return new BigDecimal(cell.getNumericCellValue() + "").longValue() + "";
		} else {
			return StringUtils.EMPTY;
		}
	}

	public String getNullSafeStateCode(String stateName, Map<String, String> stateCodeMap) {
		String stateCode = stateCodeMap.get(stateName.trim().toUpperCase());
		if (StringUtils.isBlank(stateCode)) {
			return StringUtils.EMPTY;
		} else {
			return stateCode;
		}
	}

	private String getFormattedValue(String value) {
		String formattedValue = "0.00";
		if (StringUtils.isNotBlank(value) && new BigDecimal(value).compareTo(BigDecimal.ZERO) > 0) {
			formattedValue = (new BigDecimal(value)).setScale(2, RoundingMode.UP).toString();
		}
		return formattedValue;
	}

	public boolean validateRemarks(String remarks) {
		boolean isValid = false;
		if (StringUtils.isBlank(remarks) || "A".equalsIgnoreCase(remarks) || "B".equalsIgnoreCase(remarks)
				|| "C".equalsIgnoreCase(remarks) || "S".equalsIgnoreCase(remarks) || "N".equalsIgnoreCase(remarks)) {
			isValid = true;
		}
		return isValid;
	}

	public String generateHeader(FilingFileBean tDS27QFileBean, boolean isCorrection, int assessmentYear,
			String quarter) {
		FilingHeaderBean filingHeaderBean = new FilingHeaderBean();

		filingHeaderBean.setLineNo(1);
		filingHeaderBean.setRecordType("FH");
		filingHeaderBean.setFileType("NS1");
		if (isCorrection) {
			filingHeaderBean.setUploadType("C");
		} else {
			filingHeaderBean.setUploadType("R");
		}
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
		String dateString = formatter.format(new Date());
		filingHeaderBean.setFileDate(dateString);

		// should be unique across all files? (all files meaning?)
		filingHeaderBean.setFileSeq(1);
		filingHeaderBean.setUploaderType("D");

		filingHeaderBean.setTanOfDeductor(tDS27QFileBean.getBatchHeaderBean().getTanOfDeductor());
		filingHeaderBean.setNoOfBatches(1);

		filingHeaderBean.setRpuName("NSDL RPU 3.5");

		filingHeaderBean.setRecordHash(StringUtils.EMPTY);
		filingHeaderBean.setFvuVersion(StringUtils.EMPTY);
		filingHeaderBean.setFileHash(StringUtils.EMPTY);
		filingHeaderBean.setSamVersion(StringUtils.EMPTY);
		filingHeaderBean.setSamHash(StringUtils.EMPTY);
		filingHeaderBean.setScmVersion(StringUtils.EMPTY);
		filingHeaderBean.setScmHash(StringUtils.EMPTY);
		filingHeaderBean.setFileHash(StringUtils.EMPTY);
		filingHeaderBean.setConsHash(StringUtils.EMPTY);
		if (isCorrection) {
			List<FilingFiles> filingFilesList = filingFileDAO.findByYearQuarterTanFileType(assessmentYear,
					tDS27QFileBean.getBatchHeaderBean().getTanOfDeductor(), quarter, "Conso");
			if (!filingFilesList.isEmpty()) {
				filingHeaderBean.setConsHash(filingFilesList.get(0).getConsolidatedFileHash());
			}
		}
		return filingHeaderBean.toString();
	}

	public String nullCheck(String value) {
		return StringUtils.isBlank(value) ? StringUtils.EMPTY : value;
	}

	public String bigdecimalNullCheck(String value) {
		return StringUtils.isBlank(value) ? "0" : value;
	}

	public FilingFileBean readRPUFileWithString(File file, String formType, String tenantId, Integer assessmentYear,
			boolean isCorrection, BatchUpload batchUpload, String deductorTan) throws Exception {
		FilingFileBean filingFileBean = new FilingFileBean();

		// logger.info("Entered in readRPUFile()");
		if (isCorrection && formType.contains("26Q")) {
			formType = "26Q";
		} else if (isCorrection && formType.contains("27Q")) {
			formType = "27Q";
		}
		boolean isForNonResident = "27Q".equalsIgnoreCase(formType);

		logger.info("Preparing workbook from file using XSLX-Stream");
		InputStream is = FileUtils.openInputStream(file);
		org.apache.poi.ss.usermodel.Workbook workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(222)
				.open(is);

		ResponseEntity<ApiStatus<List<FilingStateCode>>> stateResponse = mastersClient.getAllStateCode(tenantId);
		List<FilingStateCode> states = stateResponse.getBody().getData();
		Map<String, String> stateMap = new HashMap<>();
		for (FilingStateCode filingStateCode : states) {
			stateMap.put(filingStateCode.getStateName().trim().toUpperCase(), filingStateCode.getStateCode().trim());
		}

		// Create Workbook instance holding reference to .xlsx file
		// Get first/desired sheet from the workbook
		Sheet deductorMasterSheet = workbook.getSheet("Deductor Master");
		Map<Integer, Cell> cellMap = new HashMap<>();
		int index = 1;
		for (Row row : deductorMasterSheet) {
			cellMap.put(index, row.getCell(2));
			index++;
		}
		FilingBatchHeaderBean filingBatchHeader = new FilingBatchHeaderBean();
		// Error report for batch header.
		BatchHeaderErrorBean batchHeaderErrorReport = new BatchHeaderErrorBean();
		batchHeaderErrorReport.setReason("");
		boolean isValidBHRecord = true;
		Long errorCount = 0L;
		Workbook errorWorkBook = new Workbook();

		// below field form number will specify the form type is 26Q or 27Q
		int lineCount = 2;
		filingBatchHeader.setLineNo(StringUtils.EMPTY + lineCount); // 1
		filingBatchHeader.setRecordType("BH"); // 2
		filingBatchHeader.setBatchNo("1"); // 3
		filingBatchHeader.setChallanCount(StringUtils.EMPTY);// 4 Calculated after processing
		filingBatchHeader.setFormNo(formType); // 5
		if (isCorrection) {
			// REMARKS FOR CORRECTION C3 - DEDUCTOR (EXCLUDING TAN), AND/OR CHALLAN, AND/OR
			// DEDUCTEE DETAILS
			filingBatchHeader.setTransactionType("C3"); // 6
			/**
			 * If there are updations in "BH" (Batch Header) - deductor details except TAN,
			 * then value should be "1" else it should be "0". If value is "0" then no
			 * updations can be done in the BH.
			 */
			filingBatchHeader.setBatchUpdationIndicator(String.valueOf(1)); // 7
		} else {
			filingBatchHeader.setTransactionType(StringUtils.EMPTY); // 6
			filingBatchHeader.setBatchUpdationIndicator(StringUtils.EMPTY); // 7
		}
		filingBatchHeader.setOriginalRrrNo(StringUtils.EMPTY); // 8
		if (StringUtils.isBlank(getCellValue(deductorMasterSheet, cellMap.get(44)))) {
			filingBatchHeader.setPreviousRrrNo(StringUtils.EMPTY); // 9
		} else {
			filingBatchHeader.setPreviousRrrNo(getCellValue(deductorMasterSheet, cellMap.get(44))); // 9
		}
		filingBatchHeader.setRrrNo(StringUtils.EMPTY); // 10
		filingBatchHeader.setRrrDate(StringUtils.EMPTY); // 11
		if (isCorrection) {
			filingBatchHeader.setLastTanOfDeductor(getCellValue(deductorMasterSheet, cellMap.get(55))); // 12
			filingBatchHeader.setOriginalRrrNo(getCellValue(deductorMasterSheet, cellMap.get(44))); // 8
		} else {
			filingBatchHeader.setLastTanOfDeductor(StringUtils.EMPTY); // 12
		}
		filingBatchHeader.setTanOfDeductor(getCellValue(deductorMasterSheet, cellMap.get(5))); // 13

		// NA 14
		filingBatchHeader.setPanOfDeductor(getCellValue(deductorMasterSheet, cellMap.get(6))); // 15
		filingBatchHeader.setAssessmentYr(getCellValue(deductorMasterSheet, cellMap.get(8))); // 17
		filingBatchHeader.setFinancialYr(getCellValue(deductorMasterSheet, cellMap.get(4))); // 13
		filingBatchHeader.setPeriod(getCellValue(deductorMasterSheet, cellMap.get(3)));// 18
		filingBatchHeader.setEmployerName(getCellValue(deductorMasterSheet, cellMap.get(11)));// 19
		if (StringUtils.isBlank(getCellValue(deductorMasterSheet, cellMap.get(12)))) {
			filingBatchHeader.setEmployerBranchDiv("NA"); // 20
		} else {
			filingBatchHeader.setEmployerBranchDiv(getCellValue(deductorMasterSheet, cellMap.get(12))); // 20
		}
		filingBatchHeader.setEmployerAddr1(getCellValue(deductorMasterSheet, cellMap.get(13))); // 21

		filingBatchHeader.setEmployerAddr2(getCellValue(deductorMasterSheet, cellMap.get(14))); // 22
		filingBatchHeader.setEmployerAddr3(getCellValue(deductorMasterSheet, cellMap.get(15))); // 23
		filingBatchHeader.setEmployerAddr4(getCellValue(deductorMasterSheet, cellMap.get(16))); // 24
		filingBatchHeader.setEmployerAddr5(getCellValue(deductorMasterSheet, cellMap.get(17))); // 25
		String stateCode = getNullSafeStateCode(getCellValue(deductorMasterSheet, cellMap.get(18)), stateMap);
		if (StringUtils.isNotBlank(getCellValue(deductorMasterSheet, cellMap.get(18)))) {
			try {
				filingBatchHeader.setEmployerState(CommonUtil.d2.format(Double.valueOf(stateCode))); // 26
			} catch (Exception e) {
				isValidBHRecord = false;
				batchHeaderErrorReport.setReason("Invalid deductor state." + "\n");
			}
		} else {
			filingBatchHeader.setEmployerState(StringUtils.EMPTY); // 26
			isValidBHRecord = false;
			batchHeaderErrorReport.setReason("State is mandatory." + "\n");
		}
		filingBatchHeader.setEmployerPin(getCellValue(deductorMasterSheet, cellMap.get(19))); // 27
		filingBatchHeader.setEmployerEmail(getCellValue(deductorMasterSheet, cellMap.get(24))); // 28

		String phoneNo = getCellValue(deductorMasterSheet, cellMap.get(21));
		if (StringUtils.isBlank(phoneNo)) {
			filingBatchHeader.setEmployerStd(StringUtils.EMPTY); // 29
		} else {
			if (phoneNo.contains("-")) {
				filingBatchHeader.setEmployerStd(phoneNo.substring(0, phoneNo.indexOf("-"))); // 29
			} else {
				filingBatchHeader.setEmployerStd(StringUtils.EMPTY); // 29
			}
		}
		if (StringUtils.isBlank(phoneNo)) {
			filingBatchHeader.setEmployerPhone(StringUtils.EMPTY); // 30
		} else {
			if (phoneNo.contains("-")) {
				filingBatchHeader.setEmployerPhone(phoneNo.substring(phoneNo.indexOf("-") + 1, phoneNo.length())); // 30
			} else {
				filingBatchHeader.setEmployerPhone(StringUtils.EMPTY); // 30
			}
		}
		String altPhoneNo = getCellValue(deductorMasterSheet, cellMap.get(22));
		if (StringUtils.isBlank(altPhoneNo)) {
			filingBatchHeader.setEmployerSTDAlt(StringUtils.EMPTY); // 29
		} else {
			if (altPhoneNo.contains("-")) {
				filingBatchHeader.setEmployerSTDAlt(altPhoneNo.substring(0, altPhoneNo.indexOf("-"))); // 29
			} else {
				filingBatchHeader.setEmployerSTDAlt(StringUtils.EMPTY); // 29
			}
		}
		if (StringUtils.isBlank(altPhoneNo)) {
			filingBatchHeader.setEmployerPhoneAlt(StringUtils.EMPTY); // 30
		} else {
			if (altPhoneNo.contains("-")) {
				filingBatchHeader
						.setEmployerPhoneAlt(altPhoneNo.substring(altPhoneNo.indexOf("-") + 1, altPhoneNo.length())); // 30
			} else {
				filingBatchHeader.setEmployerPhoneAlt(StringUtils.EMPTY); // 30
			}
		}
		String addressChangeIndicator = getCellValue(deductorMasterSheet, cellMap.get(20));
		if (StringUtils.isNotBlank(addressChangeIndicator)) {

			if (addressChangeIndicator.equalsIgnoreCase("yes") || addressChangeIndicator.equalsIgnoreCase("y")) {
				filingBatchHeader.setEmployerAddrChange("Y"); // 31
			} else {
				filingBatchHeader.setEmployerAddrChange("N"); // 31
			}

		} else {
			filingBatchHeader.setEmployerAddrChange("N"); // 31
		}

		if (StringUtils.isNotBlank(getCellValue(deductorMasterSheet, cellMap.get(9)))) {
			// feign call to get description catagory
			try {
				ResponseEntity<ApiStatus<String>> catagoryValueResponse = mastersClient
						.getCatagoryValue(getCellValue(deductorMasterSheet, cellMap.get(9)));
				if (catagoryValueResponse.getBody().getData() != null) {
					filingBatchHeader.setDeductorType(catagoryValueResponse.getBody().getData());
				} else {
					isValidBHRecord = false;
					batchHeaderErrorReport.setReason(
							batchHeaderErrorReport.getReason() + "Invalid Deductor Type mentioned in RPU file." + "\n");
					filingBatchHeader.setDeductorType(getCellValue(deductorMasterSheet, cellMap.get(9)));
				}
			} catch (Exception e) {
				isValidBHRecord = false;
				batchHeaderErrorReport.setReason(
						batchHeaderErrorReport.getReason() + "Invalid Deductor Type mentioned in RPU file." + "\n");
				filingBatchHeader.setDeductorType(getCellValue(deductorMasterSheet, cellMap.get(9)));
			}
		} else {
			isValidBHRecord = false;
			batchHeaderErrorReport.setReason(batchHeaderErrorReport.getReason() + "Deductor Type is mandatory." + "\n");
		}

		filingBatchHeader.setNameofPersonResponsilbleForSal(getCellValue(deductorMasterSheet, cellMap.get(27))); // 33
		filingBatchHeader.setDesignationofPersonResponsilbleForSal(getCellValue(deductorMasterSheet, cellMap.get(29))); // 34
		filingBatchHeader.setPersonResponsilbleAddr1(getCellValue(deductorMasterSheet, cellMap.get(30)));

		filingBatchHeader.setPersonResponsilbleAddr2(getCellValue(deductorMasterSheet, cellMap.get(31))); // 36
		filingBatchHeader.setPersonResponsilbleAddr3(getCellValue(deductorMasterSheet, cellMap.get(32))); // 37
		filingBatchHeader.setPersonResponsilbleAddr4(getCellValue(deductorMasterSheet, cellMap.get(33))); // 38
		filingBatchHeader.setPersonResponsilbleAddr5(getCellValue(deductorMasterSheet, cellMap.get(34))); // 39

		stateCode = getNullSafeStateCode(getCellValue(deductorMasterSheet, cellMap.get(35)), stateMap);
		if (StringUtils.isNotBlank(getCellValue(deductorMasterSheet, cellMap.get(35)))) {
			try {
				filingBatchHeader.setPersonResponsilbleState(CommonUtil.d2.format(Double.valueOf(stateCode))); // 40
			} catch (Exception e) {
				isValidBHRecord = false;
				batchHeaderErrorReport
						.setReason(batchHeaderErrorReport.getReason() + "Invalid person responsible state." + "\n");
			}
		} else {
			filingBatchHeader.setPersonResponsilbleState(StringUtils.EMPTY); // 40
			isValidBHRecord = false;
			batchHeaderErrorReport
					.setReason(batchHeaderErrorReport.getReason() + "Person responsilble state is mandatory." + "\n");
		}
		filingBatchHeader.setPersonResponsilblePin(getCellValue(deductorMasterSheet, cellMap.get(36))); // 41
		filingBatchHeader.setPersonResponsilbleEmailId1(getCellValue(deductorMasterSheet, cellMap.get(40))); // 42
		filingBatchHeader.setMobileNumber(getCellValue(deductorMasterSheet, cellMap.get(39))); // 43

		String personResponsiblePhoneNo = getCellValue(deductorMasterSheet, cellMap.get(37));
		if (StringUtils.isBlank(personResponsiblePhoneNo)) {
			filingBatchHeader.setPersonResponsilbleSTDCode(StringUtils.EMPTY); // 44
		} else {
			if (personResponsiblePhoneNo.contains("-")) {
				filingBatchHeader.setPersonResponsilbleSTDCode(
						personResponsiblePhoneNo.substring(0, personResponsiblePhoneNo.indexOf("-"))); // 44
			} else {
				filingBatchHeader.setPersonResponsilbleSTDCode(StringUtils.EMPTY); // 44
			}

		}
		if (StringUtils.isBlank(personResponsiblePhoneNo)) {
			filingBatchHeader.setPersonResponsilbleTelePhone(StringUtils.EMPTY); // 45
		} else {
			if (personResponsiblePhoneNo.contains("-")) {
				filingBatchHeader.setPersonResponsilbleTelePhone(personResponsiblePhoneNo
						.substring(personResponsiblePhoneNo.indexOf("-") + 1, personResponsiblePhoneNo.length())); // 45
			} else {
				filingBatchHeader.setPersonResponsilbleTelePhone(StringUtils.EMPTY); // 45
			}

		}

		String personResponsibleAltPhoneNo = getCellValue(deductorMasterSheet, cellMap.get(38));
		if (StringUtils.isBlank(personResponsibleAltPhoneNo)) {
			filingBatchHeader.setPersonResponsilbleSTDCodeAlt(StringUtils.EMPTY); // 44
		} else {
			if (personResponsibleAltPhoneNo.contains("-")) {
				filingBatchHeader.setPersonResponsilbleSTDCodeAlt(
						personResponsibleAltPhoneNo.substring(0, personResponsibleAltPhoneNo.indexOf("-"))); // 44
			} else {
				filingBatchHeader.setPersonResponsilbleSTDCodeAlt(StringUtils.EMPTY); // 44
			}

		}
		if (StringUtils.isBlank(personResponsibleAltPhoneNo)) {
			filingBatchHeader.setPersonResponsilbleTelePhoneAlt(StringUtils.EMPTY); // 45
		} else {
			if (personResponsibleAltPhoneNo.contains("-")) {
				filingBatchHeader.setPersonResponsilbleTelePhoneAlt(personResponsibleAltPhoneNo
						.substring(personResponsibleAltPhoneNo.indexOf("-") + 1, personResponsibleAltPhoneNo.length())); // 45
			} else {
				filingBatchHeader.setPersonResponsilbleTelePhoneAlt(StringUtils.EMPTY); // 45
			}

		}
		filingBatchHeader.setPersonResponsilbleAddrChange(getCellValue(deductorMasterSheet, cellMap.get(42))); // 46
		// Total/sum *(Challan: Column L)
		filingBatchHeader.setGrossTdsTotalAsPerChallan(StringUtils.EMPTY); // 47
		filingBatchHeader.setUnMatchedChalanCnt(StringUtils.EMPTY); // 48
		filingBatchHeader.setCountOfSalaryDetailRec(StringUtils.EMPTY); // 49
		filingBatchHeader.setGrossTotalIncomeSd(StringUtils.EMPTY); // 50
		filingBatchHeader.setApprovalTaken("N"); // 51

		// Whether regular statement for Form 26Q filed for earlier period
		if (isCorrection) {
			filingBatchHeader.setApprovalNo(StringUtils.EMPTY);
			// TODO need to add dynamically
			filingBatchHeader.setLastDeductorType("K"); // 53
		} else {
			if (StringUtils.isBlank(filingBatchHeader.getPreviousRrrNo())) {
				filingBatchHeader.setApprovalNo("N"); // 52
			} else {
				filingBatchHeader.setApprovalNo("Y"); // 52
			}
			filingBatchHeader.setLastDeductorType(StringUtils.EMPTY); // 53
		}

		if (filingBatchHeader.getDeductorType().equals("S") || filingBatchHeader.getDeductorType().equals("E")
				|| filingBatchHeader.getDeductorType().equals("H") || filingBatchHeader.getDeductorType().equals("N")) {
			stateCode = getNullSafeStateCode(getCellValue(deductorMasterSheet, cellMap.get(18)), stateMap);
			if (StringUtils.isNotBlank(stateCode)) {
				filingBatchHeader.setStateName(CommonUtil.d2.format(Double.valueOf(stateCode))); // 54
			}
		} else {
			filingBatchHeader.setStateName(StringUtils.EMPTY); // 54 State Name
		}
		filingBatchHeader.setPaoCode(StringUtils.EMPTY); // 55 TODO - Future
		filingBatchHeader.setDdoCode(StringUtils.EMPTY); // 56 TODO - Future
		filingBatchHeader.setMinistryName(StringUtils.EMPTY); // 57
		filingBatchHeader.setMinistryNameOther(StringUtils.EMPTY); // 58
		if (StringUtils.isBlank(getCellValue(deductorMasterSheet, cellMap.get(28)))) {
			filingBatchHeader.setpANOfResponsiblePerson(StringUtils.EMPTY); // 59
		} else {
			filingBatchHeader.setpANOfResponsiblePerson(getCellValue(deductorMasterSheet, cellMap.get(28))); // 59
		}
		filingBatchHeader.setPaoRegistrationNo(StringUtils.EMPTY); // 60
		filingBatchHeader.setDdoRegistrationNo(StringUtils.EMPTY); // 61
		filingBatchHeader.setEmployerEmailAlt(StringUtils.EMPTY); // 64
		filingBatchHeader.setPersonResponsilbleEmailIdAlt(StringUtils.EMPTY); // 67
		filingBatchHeader.setaIN(StringUtils.EMPTY); // 68
		filingBatchHeader.setgSTN(StringUtils.EMPTY); // 69
		filingBatchHeader.setBatchHash(StringUtils.EMPTY); // 70
		if (!isValidBHRecord) {
			++errorCount;
			BeanUtils.copyProperties(filingBatchHeader, batchHeaderErrorReport);
			batchHeaderErrorReport.setSerialNumber(String.valueOf(1));
			batchHeaderErrorReport.setQuarter(getCellValue(deductorMasterSheet, cellMap.get(3)));
			batchHeaderErrorReport.setFy(getCellValue(deductorMasterSheet, cellMap.get(4)));
			batchHeaderErrorReport.setAy(getCellValue(deductorMasterSheet, cellMap.get(8)));
			// generateBatchHeaderErrorReport(errorWorkBook, batchHeaderErrorReport);
		}

		logger.info("Fetching the challan details headers");

		List<String> challanHeaders = new ArrayList<String>();
		for (Row row : workbook.getSheet("Challan details")) {
			for (int cellno = 0; cellno < 24; cellno++) {
				if (row.getCell(cellno) != null)
					challanHeaders.add(row.getCell(cellno).getStringCellValue());
			}
			break;
		}

		logger.info("Fetching the deductee details headers");
		List<String> deducteeHeaders = new ArrayList<String>();
		for (Row row : workbook.getSheet("Deductee details")) {// Deductee details
			for (int cellno = 0; cellno < 34; cellno++) {
				if (row.getCell(cellno) != null)
					deducteeHeaders.add(row.getCell(cellno).getStringCellValue());
			}
			break;
		}

		filingFileBean.setBatchHeaderBean(filingBatchHeader);

		ResponseEntity<ApiStatus<List<FilingSectionCode>>> sectionCodeResponse = mastersClient
				.getAllSectionCode(MultiTenantContext.getTenantId());
		List<FilingSectionCode> sectionCodes = sectionCodeResponse.getBody().getData();
		Map<String, String> sectionCodeMap = new HashMap<>();
		sectionCodeMap = sectionCodes.stream().map(n -> n)
				.collect(Collectors.toMap(FilingSectionCode::getSectionName, FilingSectionCode::getSectionCode));

		List<FilingDeducteeErrorBean> deducteeErrorList = new ArrayList<>();

		int challanSerialNumber = 0;
		List<FilingChallanErrorBean> challanErrorList = new ArrayList<>();
		int deducteeSerialNumber = 0;
		int cdRecordNumber = 0;
		List<String> challanDetails = new ArrayList<>();
		
		logger.info("Filtering Non empty rows from DEDUCTEE DETAILS sheet {}");
		List<Row> deducteeRowList = StreamSupport.stream(Spliterators
				.spliteratorUnknownSize(workbook.getSheet("Deductee details").iterator(), Spliterator.ORDERED), false)
				.filter(n -> isExcelRowHavingData(n)).collect(Collectors.toList());
		
		
		logger.info("Processing challan Details");
		for (Row challanRow : workbook.getSheet("Challan details")) {
			// FilingChallanDetailBean
			if (isExcelRowHavingData(challanRow)) {
				String challanErrorBean = "";
				boolean isValidChallanRecord = true;
				challanSerialNumber++;
				cdRecordNumber++;

				String filingChallanDetail = FilingChallanDetailBean.getStringValue();

				try {
					// Set this initially
					// Integer.parseInt(nullCheck( challanRow.get("Date on which amount deposited
					// through challan / Date of Transfer Voucher (DD/MM/YYYY)").substring(3, 5)));
				} catch (Exception e) {
					logger.error("Exception occured while fetching challan month." + e);
					isValidChallanRecord = false;
					challanErrorBean = challanErrorBean
							+ "Date on which amount deposited through challan should be DD/MM/YYYY format" + "\n";
				}

				lineCount++;
				filingChallanDetail = filingChallanDetail.replace("lineNo", (StringUtils.EMPTY + lineCount));
				// 1 populated from file
				filingChallanDetail = filingChallanDetail.replace("recType", "CD"); // 2
				filingChallanDetail = filingChallanDetail.replace("chBatchNo", "1"); // 3
				filingChallanDetail = filingChallanDetail.replace("challanDetailRecordNo",
						(StringUtils.EMPTY + cdRecordNumber));// 4
				// 5 TODO fix later
				filingChallanDetail = filingChallanDetail.replace("nillChallanIndicator", "N");
				String nillChallanIndicator = "N";
				if (isCorrection) {
					/**
					 * If there are updations in "CD" (Challan Details), value should be "1" else it
					 * should be "0". If value is "0" no updations can be done in CD.
					 */
					filingChallanDetail = filingChallanDetail.replace("challanUpdationIndicator", String.valueOf(1));
				} else {
					// 7 Nothing to do
				}
				// 8 Nothing to do
				// 9 Nothing to do
				// 10 Nothing to do
				// 11 Nothing to do
				// 12 populated from file
				// 13 Nothing to do
				// 14 Nothing to do
				// 15 Nothing to do
				if (StringUtils
						.isBlank(getCellValue(challanRow, challanHeaders.indexOf("BSR Code / Form 24G Receipt No")))) {
					isValidChallanRecord = false;
					challanErrorBean = challanErrorBean + "BSR Code is mandatory." + "\n";
				} else {
					filingChallanDetail = filingChallanDetail.replace("bankBranchCode",
							getCellValue(challanRow, challanHeaders.indexOf("BSR Code / Form 24G Receipt No")));
				}
				// 16 populated from file
				// 17 Nothing to do
				String dateOfBankChallanNo = nullCheck(getCellValue(challanRow, challanHeaders.indexOf(
						"Date on which amount deposited through challan / Date of Transfer Voucher (DD/MM/YYYY)")));
				filingChallanDetail = filingChallanDetail.replace("dateOfBankChallanNo",
						dateOfBankChallanNo.replace("/", ""));
				String bankChallanNo = getCellValue(challanRow,
						challanHeaders.indexOf("Challan Serial No. / DDO Serial No. of Form No. 24G"));
				if (StringUtils.isBlank(bankChallanNo)) {
					isValidChallanRecord = false;
					challanErrorBean = challanErrorBean + "Challan Serial No is mandatory." + "\n";
				} else {
					filingChallanDetail = filingChallanDetail.replace("bankChallanNo", bankChallanNo);
				}
				// 19 Nothing to do
				// 20 Nothing to do
				filingChallanDetail = filingChallanDetail.replace("section", StringUtils.EMPTY);
				// 22 'Oltas TDS / TCS -Income Tax '
				filingChallanDetail = filingChallanDetail.replace("oltasIncomeTax",
						getFormattedValue(getCellValue(challanRow, challanHeaders.indexOf("TDS (₹)"))));
				// 23 'Oltas TDS / TCS -Surcharge '
				filingChallanDetail = filingChallanDetail.replace("oltasSurcharge",
						getFormattedValue(getCellValue(challanRow, challanHeaders.indexOf("Surcharge (₹)"))));
				// 24 'Oltas TDS / TCS - Cess'
				filingChallanDetail = filingChallanDetail.replace("oltasCess",
						getFormattedValue(getCellValue(challanRow, challanHeaders.indexOf("Education Cess (₹)"))));
				// 25 Oltas TDS / TCS - Interest Amount
				filingChallanDetail = filingChallanDetail.replace("oltasInterest",
						getFormattedValue(getCellValue(challanRow, challanHeaders.indexOf("Interest (₹)"))));
				// 26 Oltas TDS / TCS - Others (amount)
				filingChallanDetail = filingChallanDetail.replace("oltasOthers",
						getFormattedValue(getCellValue(challanRow, challanHeaders.indexOf("Penalty/Others (₹)"))));
				// 27 populated from file
				filingChallanDetail = filingChallanDetail.replace("totalOfDepositAmountAsPerChallan",
						getFormattedValue(getCellValue(challanRow, challanHeaders
								.indexOf("Total amount deposited as per challan / book adjustment (E+F+G+H+I+J)"))));
				// 28 populated from file

				// 34
				filingChallanDetail = filingChallanDetail.replace("tdsInterest",
						getFormattedValue(getCellValue(challanRow, challanHeaders.indexOf("Interest (₹)"))));
				filingChallanDetail = filingChallanDetail.replace("tdsOthers",
						getFormattedValue(getCellValue(challanRow, challanHeaders.indexOf("Penalty/Others (₹)"))));
				filingChallanDetail = filingChallanDetail.replace("bookCash", "N");
				filingChallanDetail = filingChallanDetail.replace("remark", StringUtils.EMPTY);
				filingChallanDetail = filingChallanDetail.replace("lateFee",
						getFormattedValue(getCellValue(challanRow, challanHeaders.indexOf("Fee (₹)"))));// 39

				if (nillChallanIndicator.equals("Y")) {
					// 40 Minor Head of Challan
					filingChallanDetail = filingChallanDetail.replace("minorHeadCodeChallan", StringUtils.EMPTY);
				} else {
					// 40 Minor Head of Challan
					filingChallanDetail = filingChallanDetail.replace("minorHeadCodeChallan", "200");
				}
				filingChallanDetail = filingChallanDetail.replace("challanHash", StringUtils.EMPTY);

				BigDecimal totalTaxDepositedAsPerDeducteeAnex = BigDecimal.ZERO;
				BigDecimal tdsIncomeTaxC = BigDecimal.ZERO;
				BigDecimal tdsSurchargeC = BigDecimal.ZERO;
				BigDecimal tdsCessC = BigDecimal.ZERO;
				BigDecimal sumTotalIncTaxDedAtSource = BigDecimal.ZERO;
				int deducteeBeanListSize = 0;
				List<String> deduccteeDetailsList = new ArrayList<>();
				logger.info("Processing Deductee Details {}");
				for (Row row : deducteeRowList) {
					if (isExcelRowHavingData(row) && isRelatedToChallan(row, bankChallanNo)) {
						String deducteeErrorReason = "";
						boolean isValidDeducteeRecord = true;
						deducteeSerialNumber++;
						String value = FilingDeducteeDetailBean.getObjectInString(formType);

						// 1 populated from file
						lineCount++;
						value = value.replace("lineNo", (StringUtils.EMPTY + lineCount));
						// 2
						value = value.replace("recType", "DD");
						// 3
						value = value.replace("ddBatchNo", filingChallanDetail.split("\\^")[2]);// getChBatchNo
						// 4 populated from file
						// 5
						value = value.replace("deducteeDetailRecNo", deducteeBeanListSize + 1 + "");
						// 6
						// 7
						value = value.replace("deducteeSerialNo", "");
						value = value.replace("challanRecordNo",
								getCellValue(row, deducteeHeaders.indexOf("Challan Number")));

						if (isCorrection) {
							value = value.replace("mode", "U");
						} else {
							value = value.replace("mode", "O");
						}
						String deducteeRefNo = getCellValue(row, deducteeHeaders
								.indexOf("Deductee by reference number provided by the deductor (if available)"));
						// 8
						// 9
						// 10 populated from file
						String deducteePan = getCellValue(row, deducteeHeaders.indexOf("PAN of deductee"));
						if (StringUtils.isBlank(getCellValue(row, deducteeHeaders.indexOf("PAN of deductee")))) {
							deducteePan = "PANNOTAVBL";
						}
						value = value.replace("deducteePan", deducteePan);
						// 11
						// 12
						if ("PANNOTAVBL".equals(deducteePan)) {
							if (StringUtils.isBlank(deducteeRefNo)) {
								long number = (long) Math.floor(Math.random() * 9000000000L) + 1000000000L;
								deducteeRefNo= Long.valueOf(number).toString();
							}
						} else {
							deducteeRefNo= StringUtils.EMPTY;
						}
						value = value.replace("deducteeRefNo", trim(deducteeRefNo,10));
						// 13

						// 14
						value = value.replace("tdsIncomeTaxDD",
								getFormattedValue(trim(getCellValue(row, deducteeHeaders.indexOf("TDS (₹)")),15)));
						tdsIncomeTaxC = tdsIncomeTaxC.add(new BigDecimal(getFormattedValue(
								bigdecimalNullCheck(trim(getCellValue(row, deducteeHeaders.indexOf("TDS (₹)")),15)))));

						// 15
						value = value.replace("tdsSurchargeDD",
								getFormattedValue(trim(getCellValue(row, deducteeHeaders.indexOf("Surcharge (₹)")),15)));
						tdsSurchargeC = tdsSurchargeC.add(new BigDecimal(getFormattedValue(
								bigdecimalNullCheck(trim(getCellValue(row, deducteeHeaders.indexOf("Surcharge (₹)")),15)))));

						// 16
						value = value.replace("tdsCessDD",
								getFormattedValue(trim(getCellValue(row, deducteeHeaders.indexOf("Education Cess (₹)")),15)));
						tdsCessC = tdsCessC.add(new BigDecimal(getFormattedValue(
								bigdecimalNullCheck(trim(getCellValue(row, deducteeHeaders.indexOf("Education Cess (₹)")),15)))));
						// 17
						value = value.replace("totalIncomeTaxDeductedAtSource", getFormattedValue(
								trim(getCellValue(row, deducteeHeaders.indexOf("Total tax deducted (T+U+V) (₹)")),15)));
						sumTotalIncTaxDedAtSource = sumTotalIncTaxDedAtSource.add(new BigDecimal(getFormattedValue(bigdecimalNullCheck(
								trim(getCellValue(row, deducteeHeaders.indexOf("Total tax deducted (T+U+V) (₹)")),15)))));

						// 18

						// 19
						value = value.replace("totalTaxDeposited", getFormattedValue(
								trim(getCellValue(row, deducteeHeaders.indexOf("Total tax deposited (₹)")),15)));
						totalTaxDepositedAsPerDeducteeAnex = totalTaxDepositedAsPerDeducteeAnex
								.add(new BigDecimal(getFormattedValue(bigdecimalNullCheck(
										trim(getCellValue(row, deducteeHeaders.indexOf("Total tax deposited (₹)")),15)))));
						// 20
						// 21
						// 22
						value = value.replace("amountOfPayment", getFormattedValue(
								trim(getCellValue(row, deducteeHeaders.indexOf("Amount paid / credited (₹)")),15)));
						// 23

						// 26
						try {
							value = value.replace("rateAtWhichTaxDeducted",
									CommonUtil.df4.format(new BigDecimal(bigdecimalNullCheck(getCellValue(row,
											deducteeHeaders.indexOf("Rate at which tax deducted"))))));
						} catch (Exception e) {
							isValidDeducteeRecord = false;
							deducteeErrorReason = deducteeErrorReason + "Rate at which tax deducted value is mandatory."
									+ "\n";
						}

						// 30
						if ("PANNOTAVBL".equals(deducteePan)) {
							value = value.replace("remark1", "C");
						}
						value = value.replace("deducteeCode", trim(getCellValue(row,
								deducteeHeaders.indexOf("Deductee code (1-Company, 2-Other than Company)")),1));

						if (StringUtils.isBlank(getCellValue(row, deducteeHeaders.indexOf("Name of deductee")))) {
							isValidDeducteeRecord = false;
							deducteeErrorReason = deducteeErrorReason + "Deductee name is mandatory." + "\n";
						} else {
							value = value.replace("deducteeName",
									trim(getCellValue(row, deducteeHeaders.indexOf("Name of deductee")),75));
						}

						if (StringUtils.isBlank(
								getCellValue(row, deducteeHeaders.indexOf("Date of payment / credit (DD/MM/YYYY)")))) {
							isValidDeducteeRecord = false;
							deducteeErrorReason = deducteeErrorReason + "Date of payment / credit is mandatory." + "\n";
						} else {
							value = value.replace("dateOnWhichAmountPaid",
									getCellValue(row, deducteeHeaders.indexOf("Date of payment / credit (DD/MM/YYYY)"))
											.replace("/", ""));
						}

						if (new BigDecimal(bigdecimalNullCheck(
								getCellValue(row, deducteeHeaders.indexOf("Total tax deposited (₹)"))))
										.compareTo(BigDecimal.ZERO) > 0
								&& StringUtils.isBlank(
										getCellValue(row, deducteeHeaders.indexOf("Date of deduction (DD/MM/YYYY)")))) {
							isValidDeducteeRecord = false;
							deducteeErrorReason = deducteeErrorReason + "Date of deduction is mandatory." + "\n";
						}
						value = value.replace("dateOnWhichTaxDeducted",
								getCellValue(row, deducteeHeaders.indexOf("Date of deduction (DD/MM/YYYY)"))
										.replace("/", ""));

						// 33
						String section = getCellValue(row, deducteeHeaders.indexOf("Section Under Which Payment Made"));
						if (assessmentYear <= 2013) {
							section = (("194I".equals(section) || "94I".equals(section)) ? "94I" : section);
						} else {
							section = (("194I".equals(section) || "94I".equals(section)) ? "194IB" : section);
						}
						if (sectionCodeMap.get(section) == null) {
							value = value.replace("sectionCode", trim(section,3));
						} else {
							value = value.replace("sectionCode", trim(sectionCodeMap.get(section),3));
						}
						if (!validateRemarks(getCellValue(row, deducteeHeaders.indexOf(
								"Remarks (Reason for non-deduction / lower deduction / higher deduction / threshold)")))) {
							isValidDeducteeRecord = false;
							deducteeErrorReason = deducteeErrorReason
									+ "Invalid remarks value. It should be either one of these A/B/C/S/N." + "\n";
						}
						if (StringUtils.isNotBlank(getCellValue(row, deducteeHeaders.indexOf("Grossing up indicator")))
								&& !"Y".equalsIgnoreCase(
										getCellValue(row, deducteeHeaders.indexOf("Grossing up indicator")))
								&& !"N".equalsIgnoreCase(
										getCellValue(row, deducteeHeaders.indexOf("Grossing up indicator")))) {
							isValidDeducteeRecord = false;
							deducteeErrorReason = deducteeErrorReason
									+ "Invalid value in Grossing up indicator Column.It should be either one of these Y or N."
									+ "\n";
						}
						if (isForNonResident && deducteeHeaders.size()>34) {
							boolean isValidNop = validateNatureOfRemittance(getCellValue(row, deducteeHeaders.indexOf("Nature of remittance")));
							if (!isValidNop) {
								isValidDeducteeRecord = false;
								deducteeErrorReason = deducteeErrorReason+"Invalid nature of remittance value." + "\n";
							}
							String isDTAA=getCellValue(row, deducteeHeaders.indexOf("Whether TDS rate of TDS is “IT act(A)” or “DTAA(B)”"));
							if (StringUtils.isNotBlank(isDTAA)
									&& !"A".equalsIgnoreCase(isDTAA)
									&& !"B".equalsIgnoreCase(isDTAA)) {
								isValidDeducteeRecord = false;
								deducteeErrorReason = deducteeErrorReason+ "Invalid value in whether TDS rate of TDS is “IT act(A)” or “DTAA(B)” Column.It should be either one of these A or B."
										+ "\n";
							}
						}
						if (!isValidDeducteeRecord) {
							++errorCount;
							FilingDeducteeErrorBean deducteeErrorBean = generateDeducteeErrorDTOWithMap(row,
									deducteeHeaders, deductorTan);
							deducteeErrorBean.setSerialNumber(String.valueOf(deducteeSerialNumber));
							deducteeErrorBean.setDeductorMasterTan(deductorTan);
							deducteeErrorBean.setReason(deducteeErrorReason);
							deducteeErrorList.add(deducteeErrorBean);
						} else {
							deducteeBeanListSize++;
							deduccteeDetailsList.add(value);
						}
					} // if block

				}
				// TODO
				if (isCorrection) {
					totalTaxDepositedAsPerDeducteeAnex = BigDecimal.ZERO;
					tdsIncomeTaxC = BigDecimal.ZERO;
					tdsSurchargeC = BigDecimal.ZERO;
					tdsCessC = BigDecimal.ZERO;
					sumTotalIncTaxDedAtSource = BigDecimal.ZERO;
				}
				// 29
				filingChallanDetail = filingChallanDetail.replace("totalTaxDepositedAsPerDeducteeAnex",
						getFormattedValue(totalTaxDepositedAsPerDeducteeAnex));
				// 30
				filingChallanDetail = filingChallanDetail.replace("tdsIncomeTaxC", getFormattedValue(tdsIncomeTaxC));
				// 31
				filingChallanDetail = filingChallanDetail.replace("tdsSurchargeC", getFormattedValue(tdsSurchargeC));
				// 32
				filingChallanDetail = filingChallanDetail.replace("tdsCessC", getFormattedValue(tdsCessC));
				// 33
				filingChallanDetail = filingChallanDetail.replace("sumTotalIncTaxDedAtSource",
						getFormattedValue(sumTotalIncTaxDedAtSource));
				filingChallanDetail = filingChallanDetail.replace("countOfDeducteeDetail",
						deduccteeDetailsList.size() + "");
				if (!isValidChallanRecord) {
					++errorCount;
					FilingChallanErrorBean filingChallanErrorBean = generateChallanErrorDTOWithMap(challanRow,
							challanHeaders, deductorTan);
					filingChallanErrorBean.setSerialNumber(String.valueOf(challanSerialNumber));
					filingChallanErrorBean.setDeductorMasterTan(deductorTan);
					filingChallanErrorBean.setReason(challanErrorBean);
					challanErrorList.add(filingChallanErrorBean);
				} else {
					challanDetails.add(filingChallanDetail);
					challanDetails.addAll(deduccteeDetailsList);  
				}
			} // if block
		}
		filingFileBean.setChallanDetailsStringList(challanDetails);
		// Calculate total and populate in batch header bean
		BigDecimal totalAmount = BigDecimal.ZERO;
		int challanCount=0;
		for (String challan : challanDetails) {
			if (challan.split("\\^")[1].equals("CD")) {
				totalAmount = totalAmount.add(new BigDecimal(challan.split("\\^")[26]));
				challanCount++;
			}
		}
		
		  if (!challanErrorList.isEmpty()) {
			  generateChalanErrorReport(errorWorkBook,
		  challanErrorList); } 
		  if (!deducteeErrorList.isEmpty()) {
		  generateDeducteeErrorReport(errorWorkBook, deducteeErrorList,
		  isForNonResident); }
		 
		filingBatchHeader.setGrossTdsTotalAsPerChallan(CommonUtil.d2.format(totalAmount) + ".00");
		filingBatchHeader.setChallanCount(StringUtils.EMPTY + (challanCount));

		if (!isValidBHRecord || !challanErrorList.isEmpty() || !deducteeErrorList.isEmpty()) {
			File errorFile = new File(formType + "_Excel" + new Date().getTime() + ".xlsx");
			OutputStream out = new FileOutputStream(errorFile);
			errorWorkBook.save(out, SaveFormat.XLSX);
			String errorFilePath = blob.uploadExcelToBlobWithFile(errorFile, tenantId);
			batchUpload.setErrorFilePath(errorFilePath);
			batchUpload.setFailedCount(errorCount);
		}
		if (workbook != null) {
			workbook.close();
		}
		logger.info("Filing Content : " + filingFileBean);
		return filingFileBean;
	}

	private FilingDeducteeErrorBean generateDeducteeErrorDTOWithMap(Row row, List<String> headers, String deductorTan) {
		FilingDeducteeErrorBean error = new FilingDeducteeErrorBean();
		error.setAmountOfPayment(getCellValue(row, headers.indexOf("Amount paid / credited (₹)")));// row.get("Amount
																									// paid / credited
																									// (₹)")
		error.setBookCashEntry(getCellValue(row, headers.indexOf("Paid by book entry or otherwise")));
		error.setCertNumAo(getCellValue(row, headers.indexOf(
				"Certificate number issued by the assessing officer u/s 197 for non-deduction / lower deduction")));
		error.setChallanRecordNo(getCellValue(row, headers.indexOf("Challan Number")));
		error.setDateOnWhichAmountPaid(getCellValue(row, headers.indexOf("Date of payment / credit (DD/MM/YYYY)")));
		error.setDateOnWhichTaxDeducted(getCellValue(row, headers.indexOf("Date of deduction (DD/MM/YYYY)")));
		error.setDeducteeCode(getCellValue(row, headers.indexOf("Deductee code (1-Company, 2-Other than Company)")));
		error.setDeducteeName(getCellValue(row, headers.indexOf("Name of deductee")));
		error.setDeducteePan(getCellValue(row, headers.indexOf("PAN of deductee")));
		error.setDeducteeRefNo(getCellValue(row,
				headers.indexOf("Deductee by reference number provided by the deductor (if available)")));
		error.setDeductorMasterTan(deductorTan);
		error.setExcess94NAmount(getCellValue(row,
				headers.indexOf("Amount of cash withdrawal in excess of Rs. 1 crore as referred to in section 194N")));
		error.setGrossingUpIndicator(getCellValue(row, headers.indexOf("Grossing up indicator")));
		error.setLastDeducteePan(getCellValue(row, headers.indexOf("Last PAN of deductee")));
		error.setLastTotalIncomeTaxDeductedAtSource(getCellValue(row, headers.indexOf("Last total tax deducted (₹)")));
		error.setLastTotalTaxDeposited(getCellValue(row, headers.indexOf("Last total tax deducted (₹)")));
		error.setRateAtWhichTaxDeducted(getCellValue(row, headers.indexOf("Rate at which tax deducted")));
		error.setRemark1(getCellValue(row, headers
				.indexOf("Remarks (Reason for non-deduction / lower deduction / higher deduction / threshold)")));
		error.setSectionCode(getCellValue(row, headers.indexOf("Section Under Which Payment Made")));
		error.setTdsCessDD(getCellValue(row, headers.indexOf("Education Cess (₹)")));
		error.setTdsIncomeTaxDD(getCellValue(row, headers.indexOf("TDS (₹)")));
		error.setTdsSurchargeDD(getCellValue(row, headers.indexOf("Surcharge (₹)")));
		error.setTotalIncomeTaxDeductedAtSource(getCellValue(row, headers.indexOf("Total tax deducted (T+U+V) (₹)")));
		error.setTotalTaxDeposited(getCellValue(row, headers.indexOf("Total tax deposited (₹)")));
		if(headers.size()>24) {
			error.setLastTotalTaxDeposited(getCellValue(row, headers.indexOf("Last total tax deducted (₹)")));
			error.setDateOnWhichTaxDeducted(getCellValue(row, headers.indexOf("Date of deduction (DD/MM/YYYY)")));
			error.setRemark2(getCellValue(row, headers.indexOf("Remarks (Reason for non-deduction / lower deduction / higher deduction / threshold)")));
			error.setDeducteeCode(getCellValue(row, headers.indexOf("Deductee code (1-Company, 2-Other than Company)")));
			error.setRateAtWhichTaxDeducted(getCellValue(row, headers.indexOf("Rate at which tax deducted")));
			error.setBookCashEntry(getCellValue(row, headers.indexOf("Paid by book entry or otherwise")));
			error.setCertNumAo(getCellValue(row, headers.indexOf("Certificate number issued by the assessing officer u/s 197 for non-deduction / lower deduction")));
			error.setIsDTAA(getCellValue(row, headers.indexOf("Whether TDS rate of TDS is “IT act(A)” or “DTAA(B)”")));
			error.setNatureOfRemittance(getCellValue(row, headers.indexOf("Nature of remittance")));
			error.setUniqueAck15CA(getCellValue(row, headers.indexOf("Unique acknowledgement of the corresponding Form no. 15CA (If available)")));
			error.setEmailOfDeductee(getCellValue(row, headers.indexOf("Email ID of deducee")));
			error.setContactNumberOfDeductee(getCellValue(row, headers.indexOf("Contact number of deductee")));
			error.setAddressOfDeducteeInCountry(getCellValue(row, headers.indexOf("Address of deductee \n"
					+ "In country of residence")));
			error.setCountryOfDeductee(getCellValue(row, headers.indexOf("Country of the Residence of the deductee")));
			error.setTinOfDeductee(getCellValue(row, headers.indexOf("Tax identification Number/Unique identification number of deductee")));
		}
		return error;
	}

	private FilingChallanErrorBean generateChallanErrorDTOWithMap(Row row, List<String> headers, String deductorTan) {
		FilingChallanErrorBean error = new FilingChallanErrorBean();
		error.setRemark(getCellValue(row, headers.indexOf("Remarks")));
		error.setSerialNumber(getCellValue(row, headers.indexOf("Serial No.")));
		error.setChallanUpdationIndicator(getCellValue(row, headers.indexOf("Update Mode for Challan")));
		error.setSection(getCellValue(row, headers.indexOf("Section Code")));
		error.setTdsIncomeTaxC(getCellValue(row, headers.indexOf("TDS (₹)")));
		error.setTdsSurchargeC(getCellValue(row, headers.indexOf("Surcharge (₹)")));
		error.setTdsCessC(getCellValue(row, headers.indexOf("Education Cess (₹)")));
		error.setTdsInterest(getCellValue(row, headers.indexOf("Interest (₹)")));
		error.setLateFee(getCellValue(row, headers.indexOf("Fee (₹)")));
		error.setTdsOthers(getCellValue(row, headers.indexOf("Penalty/Others (₹)")));
		error.setLastTotalOfDepositAmountAsPerChallan(
				getCellValue(row, headers.indexOf("Last total tax deposited (₹)")));
		error.setTotalOfDepositAmountAsPerChallan(getCellValue(row,
				headers.indexOf("Total amount deposited as per challan / book adjustment (E+F+G+H+I+J)")));
		error.setChequeDDNo(getCellValue(row, headers.indexOf("Cheque / DD No. (if any)")));
		error.setLastBankBranchCode(getCellValue(row, headers.indexOf("Last BSR Code / 24G Receipt No.")));
		error.setBankBranchCode(getCellValue(row, headers.indexOf("BSR Code / Form 24G Receipt No")));
		error.setLastDateOfBankChallanNo(getCellValue(row, headers.indexOf("Last date on which tax deposited")));
		error.setDateOfBankChallanNo(getCellValue(row, headers
				.indexOf("Date on which amount deposited through challan / Date of Transfer Voucher (DD/MM/YYYY)")));
		error.setLastBankChallanNo(
				getCellValue(row, headers.indexOf("Last DDO / Transfer Voucher / Challan serial no.")));
		error.setBankChallanNo(
				getCellValue(row, headers.indexOf("Challan Serial No. / DDO Serial No. of Form No. 24G")));
		return error;
	}

	public String getCellValue(Row row, int cellnum) {
		return row.getCell(cellnum) == null ? ""
				: (row.getCell(cellnum).getStringCellValue() == null ? "" : row.getCell(cellnum).getStringCellValue());
	}

	public boolean isExcelRowHavingData(Row row) {
		// row.cellIterator().forEachRemaining(n->n.getStringCellValue());
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(row.cellIterator(), Spliterator.ORDERED), false)
				.anyMatch(n -> StringUtils.isNotBlank(n.getStringCellValue()));
	}

	public boolean isRelatedToChallan(Row row, String challanRecNo) {
		return row.getCell(6).getStringCellValue().trim().equals(challanRecNo.trim());
	}
	
	private boolean validateNatureOfRemittance(String natureOfRemittance) {
		boolean isValid = false;
		if (StringUtils.isNotBlank(natureOfRemittance) && (FilingFileRemittance.getfilingFileRemittance("DIVIDEND")
				.equalsIgnoreCase(natureOfRemittance)
				|| FilingFileRemittance
						.getfilingFileRemittance("FEES FOR TECHNICAL SERVICES/ FEES FOR INCLUDED SERVICES")
						.equalsIgnoreCase(natureOfRemittance)
				|| FilingFileRemittance.getfilingFileRemittance("INTEREST PAYMENT").equalsIgnoreCase(natureOfRemittance)
				|| FilingFileRemittance.getfilingFileRemittance("INVESTMENT INCOME")
						.equalsIgnoreCase(natureOfRemittance)
				|| FilingFileRemittance.getfilingFileRemittance("LONG TERM CAPITAL GAINS")
						.equalsIgnoreCase(natureOfRemittance)
				|| FilingFileRemittance.getfilingFileRemittance("ROYALTY").equalsIgnoreCase(natureOfRemittance)
				|| FilingFileRemittance.getfilingFileRemittance("SHORT TERM CAPITAL GAINS")
						.equalsIgnoreCase(natureOfRemittance)
				|| FilingFileRemittance.getfilingFileRemittance("OTHER INCOME / OTHER (NOT IN THE NATURE OF INCOME)")
						.equalsIgnoreCase(natureOfRemittance))) {
			isValid = true;
		}
		return isValid;
	}
	
	private Workbook generateChalanErrorReport(Workbook workbook, List<FilingChallanErrorBean> challanData)
			throws Exception {
		logger.info("generateChalanErrorReport method started");
		Worksheet worksheet = null;
		if (workbook.getWorksheets().get("Deductor Master") != null) {
			worksheet = workbook.getWorksheets().add("Challan details");
		} else {
			worksheet = workbook.getWorksheets().get(0);
			worksheet.setName("Challan details");
		}
		worksheet.setGridlinesVisible(false);
		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);
		String[] headers = { "Deductor TAN", "ERROR MESSAGE", "SEQUENCE NUMBER", "Remarks", "Serial No.",
				"Update Mode for Challan", "Section Code", "TDS (₹)", "Surcharge (₹)", "Education Cess (₹)",
				"Interest (₹)", "Fee (₹)", "Penalty/Others", "Last total tax deposited (₹)",
				"Total amount deposited " + "\n" + "as per challan / book adjustment (4+5+6+7+8+9)",
				"Cheque / DD No. (if any)", "Last BSR Code / 24G Receipt No.", "BSR Code / Form 24G Receipt No",
				"Last date on which tax deposited",
				"Date on which amount deposited" + "\n" + " through challan / Date of Transfer " + "\n"
						+ "Voucher (DD/MM/YYYY)",
				"Last DDO / Transfer Voucher /" + "\n" + " Challan serial no.",
				"Challan Serial No." + "\n" + " / DDO Serial No. of Form No. 24G",
				"Mode of deposit through book" + "\n" + " adjustment (Yes/No)",
				"Interest to be allocated /" + "\n" + " apportioned (₹)", "Others (₹)",
				"Minor head of challan\n" + "200-TDS payable by taxpayer\n"
						+ "400-TDS regular assessment (Raised by IT Dept)",
				"Challan Balance as per" + "\n" + " consolidated file" };
		worksheet.getCells().importArray(headers, 0, 0, false);
		Style style5 = workbook.createStyle();
		style5.setForegroundColor(Color.fromArgb(91, 155, 213));
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(true);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		// setting borders to cells
		setBorder(style5);
		Range headerColorRange1 = worksheet.getCells().createRange("A1:AA1");
		headerColorRange1.setStyle(style5);

		Style style1 = workbook.createStyle();
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(false);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		// setting borders to cells
		setBorder(style1);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.freezePanes(0, 2, 0, 2);
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:AA1");

		int rowIndex = 1;
		for (FilingChallanErrorBean challanDetailData : challanData) {
			List<Object> rowData = new ArrayList<>();
			rowData.add(challanDetailData.getDeductorMasterTan()); // Deductor Tan
			rowData.add(challanDetailData.getReason()); // ERROR MESSAGE
			rowData.add(challanDetailData.getSerialNumber()); // SEQUENCE NUMBER
			rowData.add(StringUtils.EMPTY);// Remarks
			rowData.add(challanDetailData.getSerialNumber());// Serial no
			rowData.add(StringUtils.EMPTY); // Update Mode for Challan
			rowData.add(challanDetailData.getSection()); // Section Code
			rowData.add(challanDetailData.getTdsIncomeTaxC()); // TDS (₹)
			rowData.add(challanDetailData.getTdsSurchargeC()); // Surcharge
			rowData.add(challanDetailData.getTdsCessC()); // Education Cess
			rowData.add(challanDetailData.getTdsInterest()); // Interest
			rowData.add(challanDetailData.getLateFee()); // fee
			rowData.add(challanDetailData.getTdsOthers()); // Penalyt/Others
			rowData.add(challanDetailData.getLastTotalOfDepositAmountAsPerChallan()); // Last total tax deposited (₹)
			rowData.add(challanDetailData.getTotalOfDepositAmountAsPerChallan()); // Total amount deposited
			rowData.add(challanDetailData.getChequeDDNo()); // Cheque / DD No. (if any)
			rowData.add(challanDetailData.getLastBankBranchCode()); // Last BSR Code / 24G Receipt No.
			rowData.add(challanDetailData.getBankBranchCode()); // BSR Code / Form 24G Receipt No
			rowData.add(challanDetailData.getLastDateOfBankChallanNo());// Last date on which tax deposited
			rowData.add(challanDetailData.getDateOfBankChallanNo()); // Date on which amount deposited through challan /
																		// Date of Transfer Voucher
			// (DD/MM/YYYY)
			rowData.add(challanDetailData.getLastBankChallanNo()); // Last DDO / Transfer Voucher / Challan serial no
			rowData.add(challanDetailData.getBankChallanNo()); // Challan Serial No. / DDO Serial No. of Form No. 24G
			rowData.add("No"); // Mode of deposit through book adjustment (Yes/No)
			rowData.add(challanDetailData.getTdsInterest()); // Interest to be allocated / apportioned (₹)
			rowData.add(challanDetailData.getTdsOthers()); // Others (₹)
			rowData.add(StringUtils.EMPTY); // Minor head of challan 200-TDS payable by taxpayer 400-TDS regular
											// assessment (Raised by IT Dept)
			rowData.add(StringUtils.EMPTY); // Challan Balance as per consolidated file
			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
			Range headerColorRange2 = worksheet.getCells().createRange("A" + rowIndex + ":" + "AA" + rowIndex + "");
			headerColorRange2.setStyle(style1);
		}
		logger.info("generateChalanErrorReport method ended");
		return workbook;
	}
	
	private Workbook generateDeducteeErrorReport(Workbook workbook,
			List<FilingDeducteeErrorBean> filingDeducteeErrorBean, boolean isForNonResident) throws Exception {
		logger.info("generateDeducteeErrorReport method started");
		Worksheet worksheet = null;
		if (workbook.getWorksheets().get("Deductor Master") != null
				|| workbook.getWorksheets().get("Challan details") != null) {
			worksheet = workbook.getWorksheets().add("Deductee details");
		} else {
			worksheet = workbook.getWorksheets().get(0);
			worksheet.setName("Deductee details");
		}
		worksheet.setGridlinesVisible(false);
		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (isForNonResident) {
			String[] nrHeaders = { "Deductor TAN", "ERROR MESSAGE", "SEQUENCE NUMBER", "Row Number", "Challan Number",
					"Update mode for deductee \n (Add / Update / PAN Update)",
					"BSR Code of \n branch where tax \n deposited",
					"Date on which" + "\n" + " tax deposited" + "\n" + " (DD/MM/YYYY)",
					"Transfer Voucher / Challan Serial" + "\n" + " No.", "Section Under" + "\n" + " Which Payment Made",
					"Total TDS to be allocated among" + "\n" + " deductees as in the" + "\n"
							+ " vertical total of col. 21 (₹)",
					"Interest", "Others (₹)", "Total (7+8+9) (₹)", "Sr. No",
					"Deductee by reference number provided " + "\n" + "by the deductor (if available)",
					"Last PAN of" + "\n" + " deductee", "PAN of deductee", "Name of deductee",
					"Date of payment /" + "\n" + " credit (DD/MM/YYYY)", "Amount paid /" + "\n" + " credited (₹)",
					"TDS (₹)", "Surcharge (₹)", "Education Cess (₹)",
					"Total tax " + "\n" + "deducted" + "\n" + " (18+19+20) (₹)",
					"Last total tax " + "\n" + "deducted (₹)", "Total tax" + "\n" + " deposited (₹)",
					"Last total tax" + "\n" + " deducted (₹)", "Date of deduction" + "\n" + " (DD/MM/YYYY)",
					"Remarks (Reason for non-deduction " + "\n" + "/ lower deduction / higher deduction / threshold)",
					"Deductee code (1-Company, 2-Other " + "\n" + "than Company)",
					"Rate at which" + "\n" + " tax deducted", "Paid by book" + "\n" + " entry or " + "\n" + "otherwise",
					"Certificate number issued by the assessing officer u/s 197" + "\n"
							+ " for non-deduction / lower deduction",
					"Grossing up indicator",
					"Amount of cash withdrawal " + "\n" + "in excess of Rs. 1 crore as referred" + "\n"
							+ " to in section 194N",
					"Whether TDS rate of TDS is “IT act(A)” or “DTAA(B)”", "Nature of remittance",
					"Unique acknowledgement of the corresponding" + "\n" + " Form no. 15CA (If available)",
					"Country of the Residence of the deductee", "Email ID of deducee", "Contact number of deductee",
					"Address of deductee \n" + "In country of residence",
					"Tax identification Number/Unique " + "\n" + " identification number of deductee" };
			worksheet.getCells().importArray(nrHeaders, 0, 0, false);
		} else {
			String[] resHeaders = { "Deductor TAN", "ERROR MESSAGE", "SEQUENCE NUMBER", "Row Number", "Challan Number",
					"Update mode for deductee \n (Add / Update / PAN Update)",
					"BSR Code of \n branch where tax \n deposited",
					"Date on which" + "\n" + " tax deposited" + "\n" + " (DD/MM/YYYY)",
					"Transfer Voucher / Challan Serial" + "\n" + " No.", "Section Under" + "\n" + " Which Payment Made",
					"Total TDS to be allocated among" + "\n" + " deductees as in the" + "\n"
							+ " vertical total of col. 21 (₹)",
					"Interest", "Others (₹)", "Total (7+8+9) (₹)", "Sr. No",
					"Deductee by reference number provided " + "\n" + "by the deductor (if available)",
					"Last PAN of" + "\n" + " deductee", "PAN of deductee", "Name of deductee",
					"Date of payment /" + "\n" + " credit (DD/MM/YYYY)", "Amount paid /" + "\n" + " credited (₹)",
					"TDS (₹)", "Surcharge (₹)", "Education Cess (₹)",
					"Total tax " + "\n" + "deducted" + "\n" + " (18+19+20) (₹)",
					"Last total tax " + "\n" + "deducted (₹)", "Total tax" + "\n" + " deposited (₹)",
					"Last total tax" + "\n" + " deducted (₹)", "Date of deduction" + "\n" + " (DD/MM/YYYY)",
					"Remarks (Reason for non-deduction " + "\n" + "/ lower deduction / higher deduction / threshold)",
					"Deductee code (1-Company, 2-Other " + "\n" + "than Company)",
					"Rate at which" + "\n" + " tax deducted", "Paid by book" + "\n" + " entry or " + "\n" + "otherwise",
					"Certificate number issued by the assessing officer u/s 197" + "\n"
							+ " for non-deduction / lower deduction",
					"Grossing up indicator", "Amount of cash withdrawal " + "\n"
							+ "in excess of Rs. 1 crore as referred" + "\n" + " to in section 194N" };
			worksheet.getCells().importArray(resHeaders, 0, 0, false);
		}
		String range = "AI";
		if (isForNonResident) {
			range = "AR";
		}
		Style style5 = workbook.createStyle();
		style5.setForegroundColor(Color.fromArgb(91, 155, 213));
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(true);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		// setting border
		setBorder(style5);
		Range headerColorRange1 = worksheet.getCells().createRange("A1:" + range + "1");
		headerColorRange1.setStyle(style5);

		Style style1 = workbook.createStyle();
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(false);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		// setting borders to cells
		setBorder(style1);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.freezePanes(0, 2, 0, 2);
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:" + range + "1");
		ResponseEntity<ApiStatus<List<FilingSectionCode>>> sectionCodeResponse = mastersClient
				.getAllSectionCode(MultiTenantContext.getTenantId());
		List<FilingSectionCode> sectionCodes = sectionCodeResponse.getBody().getData();
		Map<String, String> sectionCodeMap = new HashMap<>();
		for (FilingSectionCode filingSectionCode : sectionCodes) {
			sectionCodeMap.put(filingSectionCode.getSectionCode(), filingSectionCode.getSectionName());
		}
		int rowIndex = 1;
		for (FilingDeducteeErrorBean deducteeDetailData : filingDeducteeErrorBean) {
			List<Object> rowData = new ArrayList<>();
			rowData.add(deducteeDetailData.getDeductorMasterTan()); // Deductor Tan
			rowData.add(deducteeDetailData.getReason()); // ERROR MESSAGE
			rowData.add(deducteeDetailData.getSerialNumber()); // SEQUENCE NUMBER
			rowData.add(StringUtils.EMPTY); // Row Number
			rowData.add(deducteeDetailData.getChallanRecordNo()); // Challan Number
			rowData.add(StringUtils.EMPTY); // Update mode for deductee (Add / Update / PAN Update)
			rowData.add(StringUtils.EMPTY);// BSR Code of branch where tax deposited
			rowData.add(deducteeDetailData.getDateOfDeposit()); // Date on which tax deposited (DD/MM/YYYY)
			rowData.add(StringUtils.EMPTY); // Transfer Voucher / Challan Serial No.
			rowData.add(deducteeDetailData.getSectionCode()); // Section Under Which Payment Made
			rowData.add(StringUtils.EMPTY); // Total TDS to be allocated among deductees as in the
											// vertical total of col 21 (₹)
			rowData.add(StringUtils.EMPTY); // Interest
			rowData.add(StringUtils.EMPTY); // Others (₹)
			rowData.add(StringUtils.EMPTY); // Total (7+8+9) (₹)
			rowData.add(rowIndex); // Sr. No.
			rowData.add(deducteeDetailData.getDeducteeRefNo()); // Deductee by reference number provided by the deductor
																// (if available)
			rowData.add(deducteeDetailData.getLastDeducteePan()); // Last PAN of deductee
			rowData.add(deducteeDetailData.getDeducteePan()); // PAN of deductee
			rowData.add(deducteeDetailData.getDeducteeName()); // 22 Name of deductee
			rowData.add(deducteeDetailData.getDateOnWhichAmountPaid()); // Date of payment / credit (DD/MM/YYYY)
			rowData.add(deducteeDetailData.getAmountOfPayment()); // Amount paid / credited (₹)
			rowData.add(deducteeDetailData.getTdsIncomeTaxDD()); // TDS (₹)
			rowData.add(deducteeDetailData.getTdsSurchargeDD()); // Surcharge (₹)
			rowData.add(deducteeDetailData.getTdsCessDD()); // Education Cess (₹)
			rowData.add(deducteeDetailData.getTotalIncomeTaxDeductedAtSource()); // Total tax deducted (18+19+20) (₹)
			rowData.add(deducteeDetailData.getLastTotalIncomeTaxDeductedAtSource()); // Last total tax deducted (₹)
			rowData.add(deducteeDetailData.getTotalTaxDeposited()); // Total tax deposited (₹)
			rowData.add(deducteeDetailData.getLastTotalIncomeTaxDeductedAtSource()); // Last total tax deducted (₹)
			rowData.add(deducteeDetailData.getDateOnWhichTaxDeducted()); // Date of deduction (DD/MM/YYYY)
			rowData.add(deducteeDetailData.getRemark1()); // Remarks (Reason for non-deduction / lower deduction /
															// higher deduction /
			// threshold)
			rowData.add(deducteeDetailData.getDeducteeCode()); // Deductee code (1-Company, 2-Other than Company)
			rowData.add(deducteeDetailData.getRateAtWhichTaxDeducted()); // Rate at which tax deducted
			rowData.add(StringUtils.EMPTY); // Paid by book entry or otherwise
			rowData.add(deducteeDetailData.getCertNumAo()); // Certificate number issued by the assessing officer u/s
															// 197 for non-deduction / lower deduction
			rowData.add(deducteeDetailData.getGrossingUpIndicator());// Grossing up indicator
			rowData.add(StringUtils.EMPTY);// Amount of cash withdrawal " + "\n" + "in excess of Rs. 1 crore as referred
											// to in section 194N
			if (isForNonResident) {
				rowData.add(deducteeDetailData.getIsDTAA());// Whether TDS rate of TDS is “IT act(A)” or “DTAA(B)”
				try {
					if (StringUtils.isNotBlank(deducteeDetailData.getNatureOfRemittance())) {
						String nop = getNatureOfRemittance().get(deducteeDetailData.getNatureOfRemittance().trim());
						rowData.add(nop);// Nature of Remittance
					} else {
						rowData.add(StringUtils.EMPTY);
					}
				} catch (Exception e) {
					rowData.add(StringUtils.EMPTY);
				}
				rowData.add(deducteeDetailData.getUniqueAck15CA());// Unique acknowledgement of the corresponding
				// TODO
				rowData.add(StringUtils.EMPTY);
				rowData.add(deducteeDetailData.getEmailOfDeductee());
				rowData.add(deducteeDetailData.getContactNumberOfDeductee());
				rowData.add(deducteeDetailData.getAddressOfDeducteeInCountry());
				rowData.add(deducteeDetailData.getTinOfDeductee());
			}
			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
			Range headerColorRange2 = worksheet.getCells().createRange("A" + rowIndex + ":" + range + rowIndex + "");
			headerColorRange2.setStyle(style1);
		}
		logger.info("generateDeducteeErrorReport method ended");
		return workbook;
	}
	
	private Style setBorder(Style style) {
		style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());
		return style;
	}
	
	public Map<String, String> getNatureOfRemittance() {

		Map<String, String> nopMap = new HashMap<>();
		nopMap.put("16", "DIVIDEND");
		nopMap.put("21", "FEES FOR TECHNICAL SERVICES/ FEES FOR INCLUDED SERVICES");
		nopMap.put("27", "INTEREST PAYMENT");
		nopMap.put("28", "INVESTMENT INCOME");
		nopMap.put("31", "LONG TERM CAPITAL GAINS");
		nopMap.put("49", "ROYALTY");
		nopMap.put("52", "SHORT TERM CAPITAL GAINS");
		nopMap.put("99", "OTHER INCOME / OTHER (NOT IN THE NATURE OF INCOME)");

		return nopMap;
	}
	public String trim(String value, int length) {
		if (StringUtils.isBlank(value)) {
			return StringUtils.EMPTY;
		} else {
			if (value.trim().length() > length) {
				return value.trim().substring(0, length);
			} else {
				return value.trim();
			}
		}
	}
	}
