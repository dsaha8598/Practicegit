package com.ey.in.tds.returns.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
import com.ey.in.tds.common.dashboard.dto.ActivityTracker;
import com.ey.in.tds.common.dashboards.jdbc.dao.ActivityTrackerDAO;
import com.ey.in.tds.common.domain.FilingSectionCode;
import com.ey.in.tds.common.domain.FilingStateCode;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.returns.jdbc.dto.FilingFiles;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ActivityTrackerStatus;
import com.ey.in.tds.core.util.ActivityType;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.FilingFileCountry;
import com.ey.in.tds.core.util.FilingFileRemittance;
import com.ey.in.tds.core.util.ReturnType;
import com.ey.in.tds.returns.domain.BatchHeaderErrorBean;
import com.ey.in.tds.returns.domain.FilingBatchHeaderBean;
import com.ey.in.tds.returns.domain.FilingChallanDetailBean;
import com.ey.in.tds.returns.domain.FilingChallanErrorBean;
import com.ey.in.tds.returns.domain.FilingDeducteeDetailBean;
import com.ey.in.tds.returns.domain.FilingDeducteeErrorBean;
import com.ey.in.tds.returns.domain.FilingFileBean;
import com.microsoft.azure.storage.StorageException;
import com.poiji.bind.Poiji;
import com.poiji.option.PoijiOptions;
import com.poiji.option.PoijiOptions.PoijiOptionsBuilder;

@Service
public class RPUFileReadingService extends RawFileGenerationService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private BlobStorage blob;
	
	@Autowired
	private ActivityTrackerDAO activityTrackerDAO;
	
	@Autowired
	private BatchUploadDAO batchUploadDAO;
	
	@Autowired
	private RpuFileReadingWithXLSXStreamService rpuFileReadingWithXLSXStreamService;

	/**
	 * Below method will call rpuFileReadingService to get the excel data into beans
	 * and writing file data into txt file
	 * 
	 * @param inputStream
	 *
	 * @param multipartFile
	 * @param fileType
	 * @param quarter
	 * @param assessmentYear
	 * @param tanNumber
	 * @return TDS27QFileBean
	 * @throws Exception 
	 */
	@Async
	@Transactional(propagation = Propagation.SUPPORTS)
	public FilingFileBean generateFile(File file, MultipartFile multipartFile, String fileType, String quarter,
			String deductorPan, String tanNumber, Integer assessmentYear, String tenantId, String userName,
			String formType, boolean isCorrection) throws Exception {
		MultiTenantContext.setTenantId(tenantId);

		logger.info("Entered RPUFileReadingService.generateFile");
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		String uploadType = "RPU_FILE_UPLOAD";
		String fileName = FilenameUtils.getBaseName(multipartFile.getOriginalFilename()) + "_" + formType +"_"+ new Date().getTime()
				+ ".xlsx";
		BatchUpload batchUpload = saveBatchUpload(tanNumber, tenantId, assessmentYear, uploadType, "Processing", month,
				userName, null, fileName, file);

		// get the data in entities from multipart file
		FilingFileBean filingFileBean = rpuFileReadingWithXLSXStreamService.readRPUFileWithString(file, fileType, tenantId, assessmentYear, isCorrection, batchUpload, tanNumber);
		
		batchUpload.setSourceIdentifier(formType);
		if (StringUtils.isBlank(batchUpload.getErrorFilePath())) {
			// generating the text file
			String textUrl = generateTextFileWithStringData(filingFileBean, tenantId, fileType, isCorrection, assessmentYear,
					quarter);

			filingLogic(fileType, quarter, assessmentYear, tanNumber, userName, textUrl, formType);

			saveInFilingStatus(assessmentYear, quarter, deductorPan, tanNumber, ReturnType.REGULAR.name(), tenantId,
					userName, fileType);
			logger.info("Generating "+formType+" From RPU file is sucessfull {}");
			List<ActivityTracker> tracker = activityTrackerDAO.getActivityTrackerByTanYearTypeAndMonth(tanNumber,
					assessmentYear, ActivityType.QUARTERLY_TDS_FILING.getActivityType(), month);
			if (!tracker.isEmpty()) {
				tracker.get(0).setStatus(ActivityTrackerStatus.VALIDATED.name());
				tracker.get(0).setModifiedBy(userName);
				tracker.get(0).setModifiedDate(new Date());
				activityTrackerDAO.update(tracker.get(0));
			}
		}
		saveBatchUpload(tanNumber, tenantId, assessmentYear, uploadType, "Processed", month, userName,
				batchUpload, fileName, null);
		logger.info("Completed generateFile method.");
		return filingFileBean;
	}

	/**
	 * This method will read the multipart data and store that into beans
	 * 
	 * @param file
	 * @param formType
	 * @param tenantId
	 * @param assessmentYear
	 * @param isCorrection
	 * @param batchUpload
	 * @return
	 * @throws Exception
	 */
	public FilingFileBean readRPUFile(File file, String formType, String tenantId,
			Integer assessmentYear, boolean isCorrection, BatchUpload batchUpload, String deductorTan) throws Exception {
		FilingFileBean filingFileBean = new FilingFileBean();

		logger.info("Entered in readRPUFile()");
		
		if (isCorrection && formType.contains("26Q")) {
			formType = "26Q";
		} else if (isCorrection && formType.contains("27Q")) {
			formType = "27Q";
		}
		boolean isForNonResident = "27Q".equalsIgnoreCase(formType);
		XSSFWorkbook workbook = null;

		ResponseEntity<ApiStatus<List<FilingStateCode>>> stateResponse = mastersClient.getAllStateCode(tenantId);
		List<FilingStateCode> states = stateResponse.getBody().getData();
		Map<String, String> stateMap = new HashMap<>();
		for (FilingStateCode filingStateCode : states) {
			stateMap.put(filingStateCode.getStateName().trim().toUpperCase(), filingStateCode.getStateCode().trim());
		}

		// Create Workbook instance holding reference to .xlsx file
		workbook = new XSSFWorkbook(file);
		// Get first/desired sheet from the workbook
		XSSFSheet deductorMasterSheet = workbook.getSheet("Deductor Master");
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
		if (StringUtils.isBlank(getCellValue(deductorMasterSheet, 44))) {
			filingBatchHeader.setPreviousRrrNo(StringUtils.EMPTY); // 9
		} else {
			filingBatchHeader.setPreviousRrrNo(getCellValue(deductorMasterSheet, 44)); // 9
		}
		filingBatchHeader.setRrrNo(StringUtils.EMPTY); // 10
		filingBatchHeader.setRrrDate(StringUtils.EMPTY); // 11
		if (isCorrection) {
			filingBatchHeader.setLastTanOfDeductor(getCellValue(deductorMasterSheet, 5)); // 12
			filingBatchHeader.setOriginalRrrNo(getCellValue(deductorMasterSheet, 44)); // 8
		} else {
			filingBatchHeader.setLastTanOfDeductor(StringUtils.EMPTY); // 12
		}
		filingBatchHeader.setTanOfDeductor(getCellValue(deductorMasterSheet, 5)); // 13

		// NA 14
		filingBatchHeader.setPanOfDeductor(getCellValue(deductorMasterSheet, 6)); // 15
		filingBatchHeader.setAssessmentYr(getCellValue(deductorMasterSheet, 8)); // 17
		filingBatchHeader.setFinancialYr(getCellValue(deductorMasterSheet, 4)); // 13
		filingBatchHeader.setPeriod(getCellValue(deductorMasterSheet, 3));// 18
		filingBatchHeader.setEmployerName(getCellValue(deductorMasterSheet, 11));// 19
		if (StringUtils.isBlank(getCellValue(deductorMasterSheet, 12))) {
			filingBatchHeader.setEmployerBranchDiv("NA"); // 20
		} else {
			filingBatchHeader.setEmployerBranchDiv(getCellValue(deductorMasterSheet, 12)); // 20
		}
		filingBatchHeader.setEmployerAddr1(getCellValue(deductorMasterSheet, 13)); // 21

		filingBatchHeader.setEmployerAddr2(getCellValue(deductorMasterSheet, 14)); // 22
		filingBatchHeader.setEmployerAddr3(getCellValue(deductorMasterSheet, 15)); // 23
		filingBatchHeader.setEmployerAddr4(getCellValue(deductorMasterSheet, 16)); // 24
		filingBatchHeader.setEmployerAddr5(getCellValue(deductorMasterSheet, 17)); // 25
		String stateCode = getNullSafeStateCode(getCellValue(deductorMasterSheet, 18), stateMap);
		if (StringUtils.isNotBlank(getCellValue(deductorMasterSheet, 18))) {
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
		filingBatchHeader.setEmployerPin(getCellValue(deductorMasterSheet, 19)); // 27
		filingBatchHeader.setEmployerEmail(getCellValue(deductorMasterSheet, 24)); // 28

		String phoneNo = getCellValue(deductorMasterSheet, 21);
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
		String altPhoneNo = getCellValue(deductorMasterSheet, 22);
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
		String addressChangeIndicator = getCellValue(deductorMasterSheet, 20);
		if (StringUtils.isNotBlank(addressChangeIndicator)) {

			if (addressChangeIndicator.equalsIgnoreCase("yes") || addressChangeIndicator.equalsIgnoreCase("y")) {
				filingBatchHeader.setEmployerAddrChange("Y"); // 31
			} else {
				filingBatchHeader.setEmployerAddrChange("N"); // 31
			}

		} else {
			filingBatchHeader.setEmployerAddrChange("N"); // 31
		}

		if (StringUtils.isNotBlank(getCellValue(deductorMasterSheet, 9))) {
			// feign call to get description catagory
			try {
				ResponseEntity<ApiStatus<String>> catagoryValueResponse = mastersClient
						.getCatagoryValue(deductorMasterSheet.getRow(9)
								.getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
				if (catagoryValueResponse.getBody().getData() != null) {
					filingBatchHeader.setDeductorType(catagoryValueResponse.getBody().getData());
				} else {
					isValidBHRecord = false;
					batchHeaderErrorReport.setReason(
							batchHeaderErrorReport.getReason() + "Invalid Deductor Type mentioned in RPU file." + "\n");
					filingBatchHeader.setDeductorType(getCellValue(deductorMasterSheet, 9));
				}
			} catch (Exception e) {
				isValidBHRecord = false;
				batchHeaderErrorReport.setReason(
						batchHeaderErrorReport.getReason() + "Invalid Deductor Type mentioned in RPU file." + "\n");
				filingBatchHeader.setDeductorType(getCellValue(deductorMasterSheet, 9));
			}
		} else {
			isValidBHRecord = false;
			batchHeaderErrorReport.setReason(batchHeaderErrorReport.getReason() + "Deductor Type is mandatory." + "\n");
		}

		filingBatchHeader.setNameofPersonResponsilbleForSal(getCellValue(deductorMasterSheet, 27)); // 33
		filingBatchHeader.setDesignationofPersonResponsilbleForSal(getCellValue(deductorMasterSheet, 29)); // 34
		filingBatchHeader.setPersonResponsilbleAddr1(getCellValue(deductorMasterSheet, 30));

		filingBatchHeader.setPersonResponsilbleAddr2(getCellValue(deductorMasterSheet, 31)); // 36
		filingBatchHeader.setPersonResponsilbleAddr3(getCellValue(deductorMasterSheet, 32)); // 37
		filingBatchHeader.setPersonResponsilbleAddr4(getCellValue(deductorMasterSheet, 33)); // 38
		filingBatchHeader.setPersonResponsilbleAddr5(getCellValue(deductorMasterSheet, 34)); // 39

		stateCode = getNullSafeStateCode(getCellValue(deductorMasterSheet, 35), stateMap);
		if (StringUtils.isNotBlank(getCellValue(deductorMasterSheet, 35))) {
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
		filingBatchHeader.setPersonResponsilblePin(getCellValue(deductorMasterSheet, 36)); // 41
		filingBatchHeader.setPersonResponsilbleEmailId1(getCellValue(deductorMasterSheet, 40)); // 42
		filingBatchHeader.setMobileNumber(getCellValue(deductorMasterSheet, 39)); // 43

		String personResponsiblePhoneNo = getCellValue(deductorMasterSheet, 37);
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

		String personResponsibleAltPhoneNo = getCellValue(deductorMasterSheet, 38);
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
		filingBatchHeader.setPersonResponsilbleAddrChange(getCellValue(deductorMasterSheet, 42)); // 46
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
			stateCode = getNullSafeStateCode(getCellValue(deductorMasterSheet, 18), stateMap);
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
		if (StringUtils.isBlank(getCellValue(deductorMasterSheet, 28))) {
			filingBatchHeader.setpANOfResponsiblePerson(StringUtils.EMPTY); // 59
		} else {
			filingBatchHeader.setpANOfResponsiblePerson(getCellValue(deductorMasterSheet, 28)); // 59
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
			batchHeaderErrorReport.setQuarter(getCellValue(deductorMasterSheet, 3));
			batchHeaderErrorReport.setFy(getCellValue(deductorMasterSheet, 4));
			batchHeaderErrorReport.setAy(getCellValue(deductorMasterSheet, 8));
			generateBatchHeaderErrorReport(errorWorkBook, batchHeaderErrorReport);
		}

		logger.info("Processing the challan details");
		// Reading Challan details
		PoijiOptions options = PoijiOptionsBuilder.settings().sheetIndex(workbook.getSheetIndex("Challan details"))
				.build();
		List<FilingChallanDetailBean> filingChallanDetails = Poiji.fromExcel(file, FilingChallanDetailBean.class,
				options);

		logger.info("Processing Deductee details");
		PoijiOptions options2 = PoijiOptionsBuilder.settings().sheetIndex(workbook.getSheetIndex("Deductee details"))
				.build();
		List<FilingDeducteeDetailBean> filingDeducteeDetails = Poiji.fromExcel(file, FilingDeducteeDetailBean.class,
				options2);
		logger.info("No of challans rows in file: {}", filingChallanDetails.size());
		logger.info("No of deductee rows in file : {}", filingDeducteeDetails.size());
		filingFileBean.setBatchHeaderBean(filingBatchHeader);
		filingFileBean.setChallanDetailBeanList(filingChallanDetails);

		ResponseEntity<ApiStatus<List<FilingSectionCode>>> sectionCodeResponse = mastersClient
				.getAllSectionCode(MultiTenantContext.getTenantId());
		List<FilingSectionCode> sectionCodes = sectionCodeResponse.getBody().getData();
		Map<String, String> sectionCodeMap = new HashMap<>();
		for (FilingSectionCode filingSectionCode : sectionCodes) {
			sectionCodeMap.put(filingSectionCode.getSectionName(), filingSectionCode.getSectionCode());
		}

		// Iterate over the challans and populate challan assessment month & challan
		// section
		Map<String, List<FilingDeducteeDetailBean>> challanDeducteesMap = new HashMap<>();
		List<FilingDeducteeErrorBean> deducteeErrorList = new ArrayList<>();
		int count  = 0;
		for (FilingDeducteeDetailBean filingDeducteeDetail : filingDeducteeDetails) {
			filingDeducteeDetail.setFormType(formType);
			++count;
			if (StringUtils.isBlank(filingDeducteeDetail.getChallanRecordNo())) {
				FilingDeducteeErrorBean deducteeErrorBean = new FilingDeducteeErrorBean();
				BeanUtils.copyProperties(filingDeducteeDetail, deducteeErrorBean);
				deducteeErrorBean.setReason("Challan Number is mandatory." + "\n");
				deducteeErrorBean.setSerialNumber(String.valueOf(count));
				deducteeErrorBean.setDeductorMasterTan(deductorTan);
				deducteeErrorList.add(deducteeErrorBean);
				++errorCount;
			}
			List<FilingDeducteeDetailBean> challanDeductees = challanDeducteesMap
					.get(filingDeducteeDetail.getChallanRecordNo());
			if (challanDeductees == null) {
				challanDeductees = new ArrayList<>();
				challanDeducteesMap.put(filingDeducteeDetail.getChallanRecordNo(), challanDeductees);
			}
			challanDeductees.add(filingDeducteeDetail);
		}
		int challanSerialNumber = 0;
		List<FilingChallanErrorBean> challanErrorList = new ArrayList<>();
		int deducteeSerialNumber = 0;
		for (FilingChallanDetailBean filingChallanDetail : filingChallanDetails) {
			
			// Error report for challan.
			FilingChallanErrorBean filingChallanErrorBean = new FilingChallanErrorBean();
			filingChallanErrorBean.setReason("");
			boolean isValidChallanRecord = true;
			challanSerialNumber++;
			
			try {
				// Set this initially
				filingChallanDetail.setChallanMonth(
						Integer.parseInt(filingChallanDetail.getDateOfBankChallanNo().substring(3, 5)));
			} catch (Exception e) {
				logger.error("Exception occured while fetching challan month.");
				isValidChallanRecord = false;
				filingChallanErrorBean
						.setReason("Date on which amount deposited through challan should be DD/MM/YYYY format" + "\n");
			}
			
			lineCount++;
			filingChallanDetail.setLineNo(StringUtils.EMPTY + lineCount);
			// 1 populated from file
			filingChallanDetail.setRecType("CD"); // 2
			filingChallanDetail.setChBatchNo("1"); // 3
			filingChallanDetail.setChallanDetailRecordNo(
					StringUtils.EMPTY + (filingChallanDetails.indexOf(filingChallanDetail) + 1)); // 4
			// 5 TODO fix later
			filingChallanDetail.setNillChallanIndicator("N"); // 6 TODO fix later
			if (isCorrection) {
				/**
				 * If there are updations in "CD" (Challan Details), value should be "1" else it
				 * should be "0". If value is "0" no updations can be done in CD.
				 */
				filingChallanDetail.setChallanUpdationIndicator(String.valueOf(1)); // 7
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
			if (StringUtils.isBlank(filingChallanDetail.getBankBranchCode())) {
				isValidChallanRecord = false;
				filingChallanErrorBean.setReason(filingChallanErrorBean.getReason() + "BSR Code is mandatory." + "\n");
			}
			// 16 populated from file
			// 17 Nothing to do
			filingChallanDetail.setDateOfBankChallanNo(filingChallanDetail.getDateOfBankChallanNo()); // 18
			if (StringUtils.isBlank(filingChallanDetail.getBankChallanNo())) {
				isValidChallanRecord = false;
				filingChallanErrorBean
						.setReason(filingChallanErrorBean.getReason() + "Challan Serial No is mandatory." + "\n");
			}
			// 19 Nothing to do
			// 20 Nothing to do
			filingChallanDetail.setSection(StringUtils.EMPTY);
			// 22 'Oltas TDS / TCS -Income Tax '
			filingChallanDetail.setOltasIncomeTax(getFormattedValue(filingChallanDetail.getTdsIncomeTaxC()));
			// 23 'Oltas TDS / TCS -Surcharge '
			filingChallanDetail.setOltasSurcharge(getFormattedValue(filingChallanDetail.getTdsSurchargeC()));
			// 24 'Oltas TDS / TCS - Cess'
			filingChallanDetail.setOltasCess(getFormattedValue(filingChallanDetail.getTdsCessC()));
			// 25 Oltas TDS / TCS - Interest Amount
			filingChallanDetail.setOltasInterest(getFormattedValue(filingChallanDetail.getTdsInterest()));
			// 26 Oltas TDS / TCS - Others (amount)
			filingChallanDetail.setOltasOthers(getFormattedValue(filingChallanDetail.getTdsOthers()));
			// 27 populated from file
			filingChallanDetail.setTotalOfDepositAmountAsPerChallan(
					getFormattedValue(filingChallanDetail.getTotalOfDepositAmountAsPerChallan()));
			// 28 populated from file

			// 34
			filingChallanDetail.setTdsInterest(getFormattedValue(filingChallanDetail.getTdsInterest()));
			filingChallanDetail.setTdsOthers(getFormattedValue(filingChallanDetail.getTdsOthers()));
			filingChallanDetail.setBookCash("N"); // 37
			filingChallanDetail.setRemark(StringUtils.EMPTY); // 38
			filingChallanDetail.setLateFee(getFormattedValue(filingChallanDetail.getLateFee())); // 39
			if (filingChallanDetail.getNillChallanIndicator().equals("Y")) {
				// 40 Minor Head of Challan
				filingChallanDetail.setMinorHeadCodeChallan(StringUtils.EMPTY);
			} else {
				// 40 Minor Head of Challan
				filingChallanDetail.setMinorHeadCodeChallan("200");
			}
			filingChallanDetail.setChallanHash(StringUtils.EMPTY);

			List<FilingDeducteeDetailBean> challanDeductees = challanDeducteesMap
					.get(filingChallanDetail.getChallanDetailRecordNo());
			logger.info("Processing challan {} and deductees {}", filingChallanDetail.getChBatchNo(), challanDeductees);
			if (challanDeductees == null) {
				challanDeductees = new ArrayList<>();
			}

			BigDecimal totalTaxDepositedAsPerDeducteeAnex = BigDecimal.ZERO;
			BigDecimal tdsIncomeTaxC = BigDecimal.ZERO;
			BigDecimal tdsSurchargeC = BigDecimal.ZERO;
			BigDecimal tdsCessC = BigDecimal.ZERO;
			BigDecimal sumTotalIncTaxDedAtSource = BigDecimal.ZERO;

			if (challanDeductees != null && !challanDeductees.isEmpty()) {
				for (FilingDeducteeDetailBean filingDeducteeDetail : challanDeductees) {
					
					// Error report for deductee.
					FilingDeducteeErrorBean deducteeErrorBean = new FilingDeducteeErrorBean();
					deducteeErrorBean.setReason("");
					boolean isValidDeducteeRecord = true;
					deducteeSerialNumber++;
					
					// 1 populated from file
					lineCount++;
					filingDeducteeDetail.setLineNo(StringUtils.EMPTY + lineCount);
					// 2
					filingDeducteeDetail.setRecType("DD");
					// 3
					filingDeducteeDetail.setDdBatchNo(filingChallanDetail.getChBatchNo());
					// 4 populated from file
					// 5
					filingDeducteeDetail.setDeducteeDetailRecNo(
							StringUtils.EMPTY + (filingChallanDetail.getDeducteeDetailBeanList().size() + 1));
					// 6
					// 7
					filingDeducteeDetail.setDeducteeSerialNo(
							StringUtils.EMPTY + (filingDeducteeDetails.indexOf(filingDeducteeDetail) + 1));

					if (isCorrection) {
						filingDeducteeDetail.setMode("U");
					} else {
						filingDeducteeDetail.setMode("O");
					}
					// 8
					// 9
					// 10 populated from file
					if (StringUtils.isBlank(filingDeducteeDetail.getDeducteePan())) {
						filingDeducteeDetail.setDeducteePan("PANNOTAVBL");
					}
					// 11
					// 12
					if ("PANNOTAVBL".equals(filingDeducteeDetail.getDeducteePan())) {
						if(StringUtils.isBlank(filingDeducteeDetail.getDeducteeRefNo())) {
							long number = (long) Math.floor(Math.random() * 9000000000L) + 1000000000L;
							filingDeducteeDetail.setDeducteeRefNo(Long.valueOf(number).toString());
						}
					} else {
						filingDeducteeDetail.setDeducteeRefNo(StringUtils.EMPTY);
					}
					// 13

					// 14
					filingDeducteeDetail.setTdsIncomeTaxDD(getFormattedValue(filingDeducteeDetail.getTdsIncomeTaxDD()));
					tdsIncomeTaxC = tdsIncomeTaxC.add(new BigDecimal(filingDeducteeDetail.getTdsIncomeTaxDD()));

					// 15
					filingDeducteeDetail.setTdsSurchargeDD(getFormattedValue(filingDeducteeDetail.getTdsSurchargeDD()));
					tdsSurchargeC = tdsSurchargeC.add(new BigDecimal(filingDeducteeDetail.getTdsSurchargeDD()));

					// 16
					filingDeducteeDetail.setTdsCessDD(getFormattedValue(filingDeducteeDetail.getTdsCessDD()));
					tdsCessC = tdsCessC.add(new BigDecimal(filingDeducteeDetail.getTdsCessDD()));
					// 17
					filingDeducteeDetail.setTotalIncomeTaxDeductedAtSource(
							getFormattedValue(filingDeducteeDetail.getTotalIncomeTaxDeductedAtSource()));
					sumTotalIncTaxDedAtSource = sumTotalIncTaxDedAtSource
							.add(new BigDecimal(filingDeducteeDetail.getTotalIncomeTaxDeductedAtSource()));

					// 18

					// 19
					filingDeducteeDetail
							.setTotalTaxDeposited(getFormattedValue(filingDeducteeDetail.getTotalTaxDeposited()));
					totalTaxDepositedAsPerDeducteeAnex = totalTaxDepositedAsPerDeducteeAnex
							.add(new BigDecimal(filingDeducteeDetail.getTotalTaxDeposited()));
					// 20
					// 21
					// 22
					filingDeducteeDetail
							.setAmountOfPayment(getFormattedValue(filingDeducteeDetail.getAmountOfPayment()));
					// 23

					// 26
					try {
						filingDeducteeDetail.setRateAtWhichTaxDeducted(CommonUtil.df4
								.format(new BigDecimal(filingDeducteeDetail.getRateAtWhichTaxDeducted())));
						filingChallanDetail.getDeducteeDetailBeanList().add(filingDeducteeDetail);
					} catch (Exception e) {
						isValidDeducteeRecord = false;
						deducteeErrorBean.setReason("Rate at which tax deducted value is mandatory." + "\n");
					}

					// 30
					if ("PANNOTAVBL".equals(filingDeducteeDetail.getDeducteePan())) {
						filingDeducteeDetail.setRemark1("C");
					}
					
					if (StringUtils.isBlank(filingDeducteeDetail.getDeducteeName())) {
						isValidDeducteeRecord = false;
						deducteeErrorBean
								.setReason(deducteeErrorBean.getReason() + "Deductee name is mandatory." + "\n");
					}
					
					if (StringUtils.isBlank(filingDeducteeDetail.getDateOnWhichAmountPaid())) {
						isValidDeducteeRecord = false;
						deducteeErrorBean.setReason(
								deducteeErrorBean.getReason() + "Date of payment / credit is mandatory." + "\n");
					}
					
					if (new BigDecimal(filingDeducteeDetail.getTotalTaxDeposited()).compareTo(BigDecimal.ZERO) > 0
							&& StringUtils.isBlank(filingDeducteeDetail.getDateOnWhichTaxDeducted())) {
						isValidDeducteeRecord = false;
						deducteeErrorBean
								.setReason(deducteeErrorBean.getReason() + "Date of deduction is mandatory." + "\n");
					}
					
					// 33
					String section = filingDeducteeDetail.getSectionCode();
					if (assessmentYear <= 2013) {
						section = (("194I".equals(section) || "94I".equals(section)) ? "94I" : section);
					} else {
						section = (("194I".equals(section) || "94I".equals(section)) ? "194IB" : section);
					}
					if (sectionCodeMap.get(section) == null) {
						filingDeducteeDetail.setSectionCode(section);
					} else {
						filingDeducteeDetail.setSectionCode(sectionCodeMap.get(section));
					}
					if (!validateRemarks(filingDeducteeDetail.getRemark1())) {
						isValidDeducteeRecord = false;
						deducteeErrorBean.setReason(deducteeErrorBean.getReason()
								+ "Invalid remarks value. It should be either one of these A/B/C/S/N." + "\n");
					}
					if (StringUtils.isNotBlank(filingDeducteeDetail.getGrossingUpIndicator())
							&& !"Y".equalsIgnoreCase(filingDeducteeDetail.getGrossingUpIndicator())
							&& !"N".equalsIgnoreCase(filingDeducteeDetail.getGrossingUpIndicator())) {
						isValidDeducteeRecord = false;
						deducteeErrorBean.setReason(deducteeErrorBean.getReason()
								+ "Invalid value in Grossing up indicator Column.It should be either one of these Y or N."
								+ "\n");
					}
					if (isForNonResident) {
						boolean isValidNop = validateNatureOfRemittance(filingDeducteeDetail.getNatureOfRemittance());
						if (!isValidNop) {
							isValidDeducteeRecord = false;
							deducteeErrorBean.setReason(
									deducteeErrorBean.getReason() + "Invalid nature of remittance value." + "\n");
						}
						if (StringUtils.isNotBlank(filingDeducteeDetail.getIsDTAA())
								&& !"A".equalsIgnoreCase(filingDeducteeDetail.getIsDTAA())
								&& !"B".equalsIgnoreCase(filingDeducteeDetail.getIsDTAA())) {
							isValidDeducteeRecord = false;
							deducteeErrorBean.setReason(deducteeErrorBean.getReason()
									+ "Invalid value in whether TDS rate of TDS is “IT act(A)” or “DTAA(B)” Column.It should be either one of these A or B."
									+ "\n");
						}
					}
					if (!isValidDeducteeRecord) {
						++errorCount;
						BeanUtils.copyProperties(filingDeducteeDetail, deducteeErrorBean);
						deducteeErrorBean.setSerialNumber(String.valueOf(deducteeSerialNumber));
						deducteeErrorBean.setDeductorMasterTan(deductorTan);
						deducteeErrorList.add(deducteeErrorBean);
					}
				}
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
			filingChallanDetail
					.setTotalTaxDepositedAsPerDeducteeAnex(getFormattedValue(totalTaxDepositedAsPerDeducteeAnex));
			// 30
			filingChallanDetail.setTdsIncomeTaxC(getFormattedValue(tdsIncomeTaxC));
			// 31
			filingChallanDetail.setTdsSurchargeC(getFormattedValue(tdsSurchargeC));
			// 32
			filingChallanDetail.setTdsCessC(getFormattedValue(tdsCessC));
			// 33
			filingChallanDetail.setSumTotalIncTaxDedAtSource(getFormattedValue(sumTotalIncTaxDedAtSource));

			filingChallanDetail.setCountOfDeducteeDetail(
					StringUtils.EMPTY + filingChallanDetail.getDeducteeDetailBeanList().size());
			if (!isValidChallanRecord) {
				++errorCount;
				BeanUtils.copyProperties(filingChallanDetail, filingChallanErrorBean);
				filingChallanErrorBean.setSerialNumber(String.valueOf(challanSerialNumber));
				filingChallanErrorBean.setDeductorMasterTan(deductorTan);
				challanErrorList.add(filingChallanErrorBean);
			}
		}
		// Calculate total and populate in batch header bean
		BigDecimal totalAmount = BigDecimal.ZERO;
		if (filingChallanDetails != null) {
			for (FilingChallanDetailBean challan : filingChallanDetails) {
				if (challan.getTotalOfDepositAmountAsPerChallan() != null) {
					totalAmount = totalAmount.add(new BigDecimal(challan.getTotalOfDepositAmountAsPerChallan()));
				}
			}
		}
		if (!challanErrorList.isEmpty()) {
			generateChalanErrorReport(errorWorkBook, challanErrorList);
		}
		if (!deducteeErrorList.isEmpty()) {
			generateDeducteeErrorReport(errorWorkBook, deducteeErrorList, isForNonResident);
		}
		filingBatchHeader.setGrossTdsTotalAsPerChallan(CommonUtil.d2.format(totalAmount) + ".00");
		filingBatchHeader.setChallanCount(StringUtils.EMPTY + filingChallanDetails.size());
		
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

	private String getCellValue(XSSFSheet sheet, int rowNum) {
		if (CellType.STRING
				.equals(sheet.getRow(rowNum).getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType())) {
			return sheet.getRow(rowNum).getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
		} else if (CellType.NUMERIC
				.equals(sheet.getRow(rowNum).getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK).getCellType())) {
			return new BigDecimal(
					sheet.getRow(rowNum).getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK).getNumericCellValue() + "")
							.longValue()
					+ "";
		} else {
			return StringUtils.EMPTY;
		}
	}
	
	@Async
	@Transactional(propagation = Propagation.SUPPORTS)
	public String generateExcellFromText(String fileUrl, String tenantID, boolean isCorrection, String formType,
			String tanNumber, String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantID);
		String uploadType = formType + "_EXCEL_REPORT";
		int assessmentYear = CommonUtil.getAssessmentYear(null);
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		String fileName = FilenameUtils.getBaseName(formType + "_EXCEL_REPORT" + "_" + UUID.randomUUID());
		BatchUpload batchUpload = saveBatchUpload(tanNumber, tenantID, assessmentYear, uploadType, "Processing", month,
				userName, null, fileName, null);
		File file = null;
		String line = null;
		String uploadedfileUrl = null;
		file = blobStorageService.getFileFromBlobUrl(tenantID, fileUrl);
		if (file != null) {
			boolean isForNonResident = "27Q".equalsIgnoreCase(formType);
			ResponseEntity<ApiStatus<List<FilingStateCode>>> stateResponse = mastersClient.getAllStateCode(tenantID);
			List<FilingStateCode> states = stateResponse.getBody().getData();
			Map<String, String> reverseStateCodeMap = new HashMap<>();
			for (FilingStateCode filingStateCode : states) {
				reverseStateCodeMap.put(filingStateCode.getStateCode(), filingStateCode.getStateName().toUpperCase());
			}

			List<String> formSheetData = new ArrayList<String>();
			List<String> chalanSheetData = new ArrayList<String>();
			List<String> deducteeSheetData = new ArrayList<String>();
			try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));) {
				while ((line = bufferedReader.readLine()) != null) {
					if (line.split(Pattern.quote("^"))[1].equals("FH")
							|| line.split(Pattern.quote("^"))[1].equals("BH")) {
						formSheetData.add(line);
					} else if (line.split(Pattern.quote("^"))[1].equals("CD")) {
						chalanSheetData.add(line);
					} else if (line.split(Pattern.quote("^"))[1].equals("DD")) {
						deducteeSheetData.add(line);
					}
				} // END OF WHILE

				Workbook workbook = new Workbook();
				Map<String, Map<String, String>> challanMap = new HashMap<>();
				// generating form sheet
				generateFormSheet(workbook, formSheetData, reverseStateCodeMap, isCorrection);
				// generating challan sheet
				generateChalanSheet(workbook, chalanSheetData, isCorrection, challanMap);
				// generating deductee sheet
				generateDeducteeSheet(workbook, deducteeSheetData, isCorrection, isForNonResident, challanMap);

				File generatedExcelFile = new File(formType+"_EXCEL" + new Date() + ".xlsx");
				OutputStream out = new FileOutputStream(generatedExcelFile);
				workbook.save(out, SaveFormat.XLSX);

				uploadedfileUrl = blob.uploadExcelToBlobWithFile(generatedExcelFile, tenantID);
				logger.info("Uploaded file url {}", uploadedfileUrl);
				batchUpload.setSuccessFileUrl(uploadedfileUrl);
				saveBatchUpload(tanNumber, tenantID, assessmentYear, uploadType, "Processed", month, userName,
						batchUpload, fileName, null);

			} catch (Exception exception) {
				logger.info("exception occured while reading the text file {}" + exception);
			}
			return uploadedfileUrl;
		} else {
			throw new CustomException("No files found");
		}

	}
	
	public String generateExcelFromTdsTextFile(String fileUrl, String tenantID, FilingFiles filingFiles,
			boolean isCorrection) throws Exception {
		File file = null;
		String line = null;
		String uploadedfileUrl = null;
		file = blobStorageService.getFileFromBlobUrl(tenantID, fileUrl);
		if (file != null) {
			boolean isForNonResident = "27Q".equalsIgnoreCase(filingFiles.getFormType());
			ResponseEntity<ApiStatus<List<FilingStateCode>>> stateResponse = mastersClient.getAllStateCode(tenantID);
			List<FilingStateCode> states = stateResponse.getBody().getData();
			Map<String, String> reverseStateCodeMap = new HashMap<>();
			for (FilingStateCode filingStateCode : states) {
				reverseStateCodeMap.put(filingStateCode.getStateCode(), filingStateCode.getStateName().toUpperCase());
			}

			List<String> formSheetData = new ArrayList<String>();
			List<String> chalanSheetData = new ArrayList<String>();
			List<String> deducteeSheetData = new ArrayList<String>();
			try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));) {
				while ((line = bufferedReader.readLine()) != null) {
					if (line.split(Pattern.quote("^"))[1].equals("FH")
							|| line.split(Pattern.quote("^"))[1].equals("BH")) {
						formSheetData.add(line);
					} else if (line.split(Pattern.quote("^"))[1].equals("CD")) {
						chalanSheetData.add(line);
					} else if (line.split(Pattern.quote("^"))[1].equals("DD")) {
						deducteeSheetData.add(line);
					}
				} // END OF WHILE

				// saving consolidated file hash
				String[] fileHeaderValues = formSheetData.get(0).split(Pattern.quote("^"));
				filingFiles.setConsolidatedFileHash(fileHeaderValues[14]);
				filingFileDAO.update(filingFiles);

				Workbook workbook = new Workbook();
				Map<String, Map<String, String>> challanMap = new HashMap<>();
				// generating form sheet
				generateFormSheet(workbook, formSheetData, reverseStateCodeMap, isCorrection);
				// generating challan sheet
				generateChalanSheet(workbook, chalanSheetData, isCorrection, challanMap);
				// generating deductee sheet
				generateDeducteeSheet(workbook, deducteeSheetData, isCorrection, isForNonResident, challanMap);

				File generatedExcelFile = new File("26Q_Excel" + new Date().getTime() + ".xlsx");
				OutputStream out = new FileOutputStream(generatedExcelFile);
				workbook.save(out, SaveFormat.XLSX);

				uploadedfileUrl = blob.uploadExcelToBlobWithFile(generatedExcelFile, tenantID);
				logger.info("Uploaded file url {}", uploadedfileUrl);

			} catch (Exception exception) {
				logger.info("exception occured while reading the text file {}" + exception);
			}
			return uploadedfileUrl;
		} else {
			throw new CustomException("No files found");
		}

	}
	
	private Workbook generateFormSheet(Workbook workbook, List<String> formSheetData,
			Map<String, String> reverseStateCodeMap, boolean isCorrection) throws Exception {
		logger.info("generateFormSheet method started");
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setGridlinesVisible(false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		Style style5 = workbook.createStyle();
		style5.setForegroundColor(Color.fromArgb(91, 155, 213));
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(true);
		style5.setHorizontalAlignment(TextAlignmentType.LEFT);
		// setting borders to cells
		setBorder(style5);

		Range headerColorRange1 = worksheet.getCells().createRange("B2:B45");
		headerColorRange1.setStyle(style5);

		Style style1 = workbook.createStyle();
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.LEFT);
		// setting borders to cells
		setBorder(style1);
		Range headerColorRange2 = worksheet.getCells().createRange("C2:C45");
		headerColorRange2.setStyle(style1);

		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(198, 206, 213));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.LEFT);
		// setting borders to cells
		setBorder(style2);
		Range headerColorRange3 = worksheet.getCells().createRange("B2:B2");
		headerColorRange3.setStyle(style2);
		Range headerColorRange5 = worksheet.getCells().createRange("B3:B3");
		headerColorRange5.setStyle(style2);
		Range headerColorRange6 = worksheet.getCells().createRange("B11:B11");
		headerColorRange6.setStyle(style2);
		Range headerColorRange7 = worksheet.getCells().createRange("B27:B27");
		headerColorRange7.setStyle(style2);

		Style style3 = workbook.createStyle();
		style3.setForegroundColor(Color.fromArgb(213, 149, 91));
		style3.setPattern(BackgroundType.SOLID);
		style3.getFont().setBold(true);
		style3.setHorizontalAlignment(TextAlignmentType.LEFT);
		setBorder(style3);
		Range headerColorRange4 = worksheet.getCells().createRange("B13:B13");
		headerColorRange4.setStyle(style3);
		Range headerColorRange8 = worksheet.getCells().createRange("B21:B21");
		headerColorRange8.setStyle(style3);
		Range headerColorRange9 = worksheet.getCells().createRange("B43:B43");
		headerColorRange9.setStyle(style3);

		worksheet.setName("Deductor Master");

		String[] headers = { "Deductor Master", "Particulars of statement", "Quarter", "FY", "TAN", "PAN",
				"Is this a revised return (Yes / No)", "AY", "Type of deductor", "Deductor Details", "Name",
				"Branch/Division", "Flat/Door/Block No.", "Name of the Building", "Street/Road Name", "Area", "City",
				"State", "Pincode", "Address Change", "Telephone", "Telephone (Alternate)", "Fax", "E-Mail",
				"E-Mail (Alternate)", "Responsible Person Details", "Name", "PAN", "Designation", "Flat/Door/Block No.",
				"Name of Building", "Street Name/Road Name", "Area", "City", "State", "Pincode", "Telephone",
				"Telephone (Alternate)", "Mobile Number", "E-Mail", "E-Mail (Alternate)", "Address Change",
				"Has regular statement for Form 26Q filed for the earlier period?",
				"Receipt No. of earlier statement filed for Form 26Q" };
		worksheet.getCells().importArray(headers, 1, 1, true);
		String[] batchHeaderValues = formSheetData.get(1).split(Pattern.quote("^")); // splitting batch header

		List<Object> rowData = new ArrayList<>();
		rowData.add(batchHeaderValues[17]); // Quarter
		String fyRange = convertToYearRange(batchHeaderValues[16]);
		rowData.add(fyRange); // FY
		rowData.add(batchHeaderValues[12]); // TAN
		rowData.add(batchHeaderValues[14]); // PAN
		if (isCorrection) {
			rowData.add("Yes"); // Is this a revised return (Yes / No)
		} else {
			rowData.add("No");
		}
		String ayRange = convertToYearRange(batchHeaderValues[15]);
		rowData.add(ayRange); // AY
		// feign call to get description catagory
		ResponseEntity<ApiStatus<String>> catagoryValueResponse = mastersClient
				.getCatagoryDescription(batchHeaderValues[31]);
		if (catagoryValueResponse.getBody().getData() != null) {
			rowData.add(catagoryValueResponse.getBody().getData()); // Type of deductor
		} else {
			rowData.add(StringUtils.EMPTY);
		}
		rowData.add(StringUtils.EMPTY); // Deductor Details
		rowData.add(batchHeaderValues[18]); // Deductor Name
		if ("NA".equalsIgnoreCase(batchHeaderValues[19])) {
			rowData.add(StringUtils.EMPTY);
		} else {
			rowData.add(batchHeaderValues[19]); // Branch/Division
		}
		rowData.add(batchHeaderValues[20]); // Flat/Door/Block No
		rowData.add(batchHeaderValues[21]); // Name of the Building
		rowData.add(batchHeaderValues[22]); // Street/Road Name
		rowData.add(batchHeaderValues[23]); // Area
		rowData.add(batchHeaderValues[24]); // City
		if (StringUtils.isNotBlank(batchHeaderValues[25])) {
			String stateName = reverseStateCodeMap.get(batchHeaderValues[25]);
			rowData.add(stateName); // State
		} else {
			rowData.add(StringUtils.EMPTY); // State
		}
		rowData.add(batchHeaderValues[26]); // Pincode
		if ("N".equalsIgnoreCase(batchHeaderValues[30])) { // Address Change
			rowData.add("No");
		} else if ("Y".equalsIgnoreCase(batchHeaderValues[30])) {
			rowData.add("Yes");
		} else {
			rowData.add(StringUtils.EMPTY);
		}
		
		rowData.add(batchHeaderValues[28] + "-" + batchHeaderValues[29]); // Telephone
		if (isCorrection) {
			try {
				rowData.add(batchHeaderValues[62] + "-" + batchHeaderValues[63]); // Telephone (Alternate)
			} catch (Exception e) {
				rowData.add(StringUtils.EMPTY);
			}
		} else {
			try {
				rowData.add(batchHeaderValues[61] + "-" + batchHeaderValues[62]); // Telephone (Alternate)
			} catch (Exception e) {
				rowData.add(StringUtils.EMPTY);
			}
		}
		rowData.add(StringUtils.EMPTY); // Fax
		rowData.add(batchHeaderValues[27]); // E-MAIL
		rowData.add(StringUtils.EMPTY); // EMAIL(ALTERNATE)
		rowData.add(StringUtils.EMPTY); // Responsible Person Details
		rowData.add(batchHeaderValues[32]); // NAME
		try {
			rowData.add(batchHeaderValues[58]); // PAN
		} catch (Exception e) {
			rowData.add(StringUtils.EMPTY);
			// Person responsible pan is optional
			logger.error("Person responsible pan not available");
		}
		rowData.add(batchHeaderValues[33]); // DESIGNATION
		rowData.add(batchHeaderValues[34]); // Flat/Door/Block No
		rowData.add(batchHeaderValues[35]); // Name of Building
		rowData.add(batchHeaderValues[36]); // Street Name/Road Name
		rowData.add(batchHeaderValues[37]); // Area
		rowData.add(batchHeaderValues[38]); // City
		if (StringUtils.isNotBlank(batchHeaderValues[39])) {
			String stateName = reverseStateCodeMap.get(batchHeaderValues[39].trim());
			rowData.add(stateName); // State
		} else {
			rowData.add(StringUtils.EMPTY); // State
		}
		rowData.add(batchHeaderValues[40]); // Pincode
		rowData.add(batchHeaderValues[43] + "-" + batchHeaderValues[44]); // Telephone",
		if (isCorrection) {
			try {
				rowData.add(batchHeaderValues[65] + "-" + batchHeaderValues[66]); // "Telephone (Alternate)
			} catch (Exception e) {
				rowData.add(StringUtils.EMPTY);
			}
			rowData.add(batchHeaderValues[47]); // Mobile Number
		} else {
			try {
				rowData.add(batchHeaderValues[64] + "-" + batchHeaderValues[65]); // "Telephone (Alternate)
			} catch (Exception e) {
				rowData.add(StringUtils.EMPTY);
			}
			rowData.add(batchHeaderValues[42]); // Mobile Number
		}
		rowData.add(batchHeaderValues[41]); // E-Mail
		rowData.add(StringUtils.EMPTY); // E-Mail (Alternate)
		
		if ("N".equalsIgnoreCase(batchHeaderValues[45])) { // Address Change
			rowData.add("No");
		} else if ("Y".equalsIgnoreCase(batchHeaderValues[45])) {
			rowData.add("Yes");
		} else {
			rowData.add(StringUtils.EMPTY);
		}
		if ("N".equalsIgnoreCase(batchHeaderValues[51])) { // Has regular statement for Form 26Q filed for the earlier period?
			rowData.add("No");
		} else if ("Y".equalsIgnoreCase(batchHeaderValues[51])) {
			rowData.add("Yes");
		} else {
			rowData.add(StringUtils.EMPTY);
		}
		if ("Y".equalsIgnoreCase(batchHeaderValues[51])) { // Receipt No. of earlier statement filed for Form 26Q
			rowData.add(batchHeaderValues[8]);
		} else {
			rowData.add(StringUtils.EMPTY);
		}

		worksheet.getCells().importArrayList((ArrayList<Object>) rowData, 3, 2, true);
		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		logger.info("generateFormSheet method ended");
		return workbook;
	}

	private Workbook generateChalanSheet(Workbook workbook, List<String> chalanSheetData, boolean isCorrection,
			Map<String, Map<String, String>> challanMap) throws Exception {
		logger.info("generateChalanSheet method started");
		Worksheet worksheet = workbook.getWorksheets().add("Challan details");
		worksheet.setGridlinesVisible(false);
		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);
		String[] headers = { "Remarks", "Serial No.", "Update Mode for Challan", "Section Code", "TDS (₹)",
				"Surcharge (₹)", "Education Cess (₹)", "Interest (₹)", "Fee (₹)", "Penalty/Others",
				"Last total tax deposited (₹)",
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
		Range headerColorRange1 = worksheet.getCells().createRange("A1:X1");
		headerColorRange1.setStyle(style5);

		Style style1 = workbook.createStyle();
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(false);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		// setting borders to cells
		setBorder(style1);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:X1");

		int rowIndex = 1;
		for (String data : chalanSheetData) {
			String[] challanDetailData = data.split(Pattern.quote("^"), 41);
			Map<String, String> challanDetailsMap = new HashMap<>();
			List<Object> rowData = new ArrayList<>();
			rowData.add(StringUtils.EMPTY);// Remarks
			rowData.add(challanDetailData[3]);// Serial no
			rowData.add(StringUtils.EMPTY); // Update Mode for Challan
			// TODO
			rowData.add(challanDetailData[20]); // Section Code
			rowData.add(challanDetailData[21]); // TDS (₹)
			rowData.add(challanDetailData[22]); // Surcharge
			rowData.add(challanDetailData[23]); // Education Cess
			rowData.add(challanDetailData[24]); // Interest
			rowData.add(challanDetailData[38]); // fee
			rowData.add(challanDetailData[25]); // Penalyt/Others
			if (isCorrection) {
				rowData.add(challanDetailData[26]); // Last total tax deposited (₹)
			} else {
				rowData.add(StringUtils.EMPTY);
			}
			rowData.add(challanDetailData[26]); // Total amount deposited
			rowData.add(challanDetailData[35]); // Cheque / DD No. (if any)
			if (isCorrection) {
				rowData.add(challanDetailData[15]); // Last BSR Code / 24G Receipt No.
			} else {
				rowData.add(StringUtils.EMPTY);
			}
			rowData.add(challanDetailData[15]); // BSR Code / Form 24G Receipt No
			challanDetailsMap.put("bsrCode", challanDetailData[15]);
			String receiptDate = convertToDate(challanDetailData[17]);
			if (isCorrection) {
				rowData.add(receiptDate); // Last date on which tax deposited
			} else {
				rowData.add(StringUtils.EMPTY);
			}
			rowData.add(receiptDate); // Date on which amount deposited through challan / Date of Transfer Voucher
											// (DD/MM/YYYY)
			challanDetailsMap.put("receiptDate", receiptDate);
			if (isCorrection) {
				/**
				 * Mention Bank Challan Number specified in the corresponding regular or last
				 * correction statement. Mandatory if book entry flag or Nil challan indicator
				 * is "N".
				 */
				rowData.add(challanDetailData[11]); // Last DDO / Transfer Voucher / Challan serial no
			} else {
				rowData.add(StringUtils.EMPTY);
			}
			rowData.add(challanDetailData[11]); // Challan Serial No. / DDO Serial No. of Form No. 24G
			challanDetailsMap.put("challanSerialNo", challanDetailData[11]);
			rowData.add("No"); // Mode of deposit through book adjustment (Yes/No)
			rowData.add(challanDetailData[33]); // Interest to be allocated / apportioned (₹)
			rowData.add(challanDetailData[34]); // Others (₹)
			rowData.add(challanDetailData[39]); // Minor head of challan 200-TDS payable by taxpayer 400-TDS regular
											// assessment (Raised by IT Dept)
			rowData.add(StringUtils.EMPTY); // Challan Balance as per consolidated file
			challanMap.put(challanDetailData[3], challanDetailsMap);
			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
			Range headerColorRange2 = worksheet.getCells().createRange("A" + rowIndex + ":" + "X" + rowIndex + "");
			headerColorRange2.setStyle(style1);
		}
		logger.info("generateChalanSheet method ended");
		return workbook;
	}

	private Workbook generateDeducteeSheet(Workbook workbook, List<String> deducteeSheetData, boolean isCorrection,
			boolean isForNonResident, Map<String, Map<String, String>> challanMap) throws Exception {
		logger.info("generateDeducteeSheet method started");
		Worksheet worksheet = workbook.getWorksheets().add("Deductee details");
		worksheet.setGridlinesVisible(false);
		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);
		
		if (isForNonResident) {
			String[] nrHeaders = { "Row Number", "Challan Number",
					"Update mode for deductee \n (Add / Update / PAN Update)",
					"BSR Code of \n branch where tax \n deposited",
					"Date on which" + "\n" + " tax deposited" + "\n" + " (DD/MM/YYYY)",
					"Transfer Voucher / Challan Serial" + "\n" + " No.", "Section Under" + "\n" + " Which Payment Made",
					"Total TDS to be allocated among" + "\n" + " deductees as in the" + "\n"
							+ " vertical total of col. 21 (₹)",
					"Des-TDS", "Others (₹)", "Total (7+8+9) (₹)", "Sr. No",
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
			worksheet.getCells().importArray(nrHeaders, 0, 1, false);
		} else {
			String[] resHeaders = { "Row Number", "Challan Number",
					"Update mode for deductee \n (Add / Update / PAN Update)",
					"BSR Code of \n branch where tax \n deposited",
					"Date on which" + "\n" + " tax deposited" + "\n" + " (DD/MM/YYYY)",
					"Transfer Voucher / Challan Serial" + "\n" + " No.", "Section Under" + "\n" + " Which Payment Made",
					"Total TDS to be allocated among" + "\n" + " deductees as in the" + "\n"
							+ " vertical total of col. 21 (₹)",
					"Des-TDS", "Others (₹)", "Total (7+8+9) (₹)", "Sr. No",
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
			worksheet.getCells().importArray(resHeaders, 0, 1, false);
		}
		String range = "AH";
		if (isForNonResident) {
			range = "AP";
		}
		Style style5 = workbook.createStyle();
		style5.setForegroundColor(Color.fromArgb(91, 155, 213));
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(true);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		// setting border
		setBorder(style5);
		Range headerColorRange1 = worksheet.getCells().createRange("B1:" + range + "1");
		headerColorRange1.setStyle(style5);

		Style style1 = workbook.createStyle();
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(false);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		// setting borders to cells
		setBorder(style1);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("B1:" + range + "1");
		int rowIndex = 1;
		for (String data : deducteeSheetData) {
			String[] deducteeDetailData = data.split(Pattern.quote("^"), 48);
			String challanSerialNo = StringUtils.EMPTY;
			String receiptDate = StringUtils.EMPTY;
			List<Object> rowData = new ArrayList<>();
			rowData.add(StringUtils.EMPTY); // Row Number
			rowData.add(deducteeDetailData[3]); // Challan Number
			rowData.add(StringUtils.EMPTY); // Update mode for deductee (Add / Update / PAN Update)
			if (challanMap != null) {
				for (Entry<String, Map<String, String>> entry : challanMap.entrySet()) {
					if (entry.getKey().equals(deducteeDetailData[3])) {
						rowData.add(entry.getValue().get("bsrCode"));
						receiptDate = entry.getValue().get("receiptDate");
						challanSerialNo = entry.getValue().get("challanSerialNo");
						break;
					}
				}
			} else {
				rowData.add(StringUtils.EMPTY);
			}// BSR Code of branch where tax deposited
			rowData.add(receiptDate); // Date on which tax deposited (DD/MM/YYYY)
			rowData.add(challanSerialNo); // Transfer Voucher / Challan Serial No.
			if (isCorrection) {
				rowData.add(deducteeDetailData[33]);
			} else {
				rowData.add(deducteeDetailData[32]);
			}
			rowData.add(deducteeDetailData[17]); // Total TDS to be allocated among deductees as in the vertical total
													// of col.
			// 21 (₹)
			rowData.add(StringUtils.EMPTY); // Des-TDS
			rowData.add(StringUtils.EMPTY); // Others (₹)
			rowData.add(deducteeDetailData[17]); // Total (7+8+9) (₹)
			rowData.add(rowIndex); // Sr. No.
			rowData.add(deducteeDetailData[11]); // Deductee by reference number provided by the deductor (if available)
			if (isCorrection) {
				rowData.add(deducteeDetailData[9]); // Last PAN of deductee
			} else {
				rowData.add(StringUtils.EMPTY); // Last PAN of deductee
			}
			rowData.add(deducteeDetailData[9]); // PAN of deductee
			rowData.add(deducteeDetailData[12]); // 22 Name of deductee
			String taxPaymentDate = convertToDate(deducteeDetailData[22]);
			rowData.add(taxPaymentDate); // Date of payment / credit (DD/MM/YYYY)
			rowData.add(deducteeDetailData[21]); // Amount paid / credited (₹)
			rowData.add(deducteeDetailData[13]); // TDS (₹)
			rowData.add(deducteeDetailData[14]); // Surcharge (₹)
			rowData.add(deducteeDetailData[15]); // Education Cess (₹)
			rowData.add(deducteeDetailData[16]); // Total tax deducted (18+19+20) (₹)
			if (isCorrection) {
				rowData.add(deducteeDetailData[16]); // Last total tax deducted (₹)
			} else {
				rowData.add(StringUtils.EMPTY);
			}
			rowData.add(deducteeDetailData[18]); // Total tax deposited (₹)
			if (isCorrection) {
				rowData.add(deducteeDetailData[18]); // Last total tax deducted (₹)
			} else {
				rowData.add(StringUtils.EMPTY);
			}
			String taxDeductedDate = convertToDate(deducteeDetailData[23]);
			rowData.add(taxDeductedDate); // Date of deduction (DD/MM/YYYY)
			rowData.add(deducteeDetailData[29]); // Remarks (Reason for non-deduction / lower deduction / higher
													// deduction /
			// threshold)
			rowData.add(deducteeDetailData[7]); // Deductee code (1-Company, 2-Other than Company)
			rowData.add(deducteeDetailData[25]); // Rate at which tax deducted
			rowData.add(StringUtils.EMPTY); // Paid by book entry or otherwise
			rowData.add(deducteeDetailData[33]); // Certificate number issued by the assessing officer u/s 197 for
			// non-deduction / lower deduction

			rowData.add(deducteeDetailData[26]);// Grossing up indicator
			rowData.add(deducteeDetailData[44]);// Amount of cash withdrawal " + "\n" + "in excess of Rs. 1 crore as
												// referred to in section 194N
			if (isForNonResident) {
				rowData.add(deducteeDetailData[34]);// Whether TDS rate of TDS is “IT act(A)” or “DTAA(B)”
				try {
					if (StringUtils.isNotBlank(deducteeDetailData[35])) {
						rowData.add(getNatureOfRemittance().get(deducteeDetailData[35]));// Nature of Remittance
					} else {
						rowData.add(StringUtils.EMPTY);// Nature of Remittance
					}
				} catch (Exception e) {
					rowData.add(StringUtils.EMPTY);
				}
				rowData.add(deducteeDetailData[36]);// Unique acknowledgement of the corresponding
				try {
					String country = getCountry(deducteeDetailData[37]);
					if (StringUtils.isNotBlank(country)) {
						country = country.replace("_", " ");
						rowData.add(country);// Country
					} else {
						rowData.add(StringUtils.EMPTY);// Country
					}
				} catch (Exception e) {
					rowData.add(StringUtils.EMPTY);
				}
				rowData.add(deducteeDetailData[38]);// Email Id
				rowData.add(deducteeDetailData[39]);// Contact No
				rowData.add(deducteeDetailData[40]);// Address
				rowData.add(deducteeDetailData[41]);// Tin
			}
			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 1, false);
			Range headerColorRange2 = worksheet.getCells().createRange("B" + rowIndex + ":" + range + rowIndex + "");
			headerColorRange2.setStyle(style1);
		}
		logger.info("generateDeducteeSheet method ended");
		return workbook;
	}

	private Style setBorder(Style style) {
		style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());
		return style;
	}

	public String getCountry(String countryCode) {
		if (StringUtils.isNotBlank(countryCode)) {
			for (FilingFileCountry filingFileCountry : FilingFileCountry.values()) {
				if (countryCode.equalsIgnoreCase(filingFileCountry.getfilingFileCountryCode())) {
					return filingFileCountry.name();
				}
			}
		}
		return "";
	}

	private String getFormattedValue(String value) {
		String formattedValue = "0.00";
		if (StringUtils.isNotBlank(value) && new BigDecimal(value).compareTo(BigDecimal.ZERO) > 0) {
			formattedValue = (new BigDecimal(value)).setScale(2, RoundingMode.UP).toString();
		}
		return formattedValue;
	}

	private String getFormattedValue(BigDecimal value) {
		String formattedValue = "0.00";
		if (value.compareTo(BigDecimal.ZERO) > 0) {
			formattedValue = (value.setScale(2, RoundingMode.UP)).toString();
		}
		return formattedValue;
	}
	
	private String convertToYearRange(String value) {
		if(StringUtils.isNotBlank(value) && value.length() == 6) {
			return value.substring(0, 4) + "-" + value.substring(4, value.length()); 
		}
		return value ;
	}
	
	private String convertToDate(String value) {
		if(StringUtils.isNotBlank(value) && value.length() == 8 ) {
			return value.substring(0, 2) + "/" + value.substring(2, 4) + "/" + value.substring(4, value.length());
		}
		return value;
	}
	
	protected BatchUpload saveBatchUpload(String deductorTan, String tenantId, int assessmentYear, String uploadType,
			String status, int month, String userName, BatchUpload dbBatch, String fileName, File file)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setFileName(fileName);
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setActive(true);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		if (file != null) {
			String uploadFilePath =blob.uploadExcelToBlobWithFile(file, tenantId);
			batchUpload.setFilePath(uploadFilePath);
		}
		if (dbBatch != null && dbBatch.getBatchUploadID() != null) {
			batchUpload = dbBatch;
			batchUpload.setModifiedDate(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setFileName(fileName);
			batchUpload.setStatus(status);
			batchUpload.setModifiedBy(userName);
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedBy(userName);
			batchUpload.setModifiedDate(null);
			batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setRowsCount(1l);
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}
	
	private Workbook generateBatchHeaderErrorReport(Workbook workbook, BatchHeaderErrorBean batchHeaderErrorReport)
			throws Exception {
		logger.info("generateBatchHeaderErrorReport method started");
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setGridlinesVisible(false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		Style style5 = workbook.createStyle();
		style5.setForegroundColor(Color.fromArgb(91, 155, 213));
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(true);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		// setting borders to cells
		setBorder(style5);
		Range headerColorRange1 = worksheet.getCells().createRange("A1:AN1");
		headerColorRange1.setStyle(style5);

		Style style1 = workbook.createStyle();
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(false);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		// setting borders to cells
		setBorder(style1);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:AN1");
		worksheet.setName("Deductor Master");

		String[] headers = { "Deductor TAN", "ERROR MESSAGE", "SEQUENCE NUMBER", "Quarter", "FY", "TAN", "PAN",
				"Is this a revised return (Yes / No)", "AY", "Type of deductor", "Name", "Flat/Door/Block No.",
				"Name of the Building", "Street/Road Name", "Area", "City", "State", "Pincode", "Telephone",
				"Telephone (Alternate)", "Fax", "E-Mail", "E-Mail (Alternate)", "Name", "PAN", "Designation",
				"Flat/Door/Block No.", "Name of Building", "Street Name/Road Name", "Area", "City", "State", "Pincode",
				"Telephone", "Telephone (Alternate)", "Mobile Number", "E-Mail", "E-Mail (Alternate)",
				"Has regular statement for Form 26Q filed for the earlier period?",
				"Receipt No. of earlier statement filed for Form 26Q" };
		worksheet.getCells().importArray(headers, 0, 0, false);
		List<Object> rowData = new ArrayList<>();
		rowData.add(batchHeaderErrorReport.getTanOfDeductor()); // Deductor Tan
		rowData.add(batchHeaderErrorReport.getReason()); // ERROR MESSAGE
		rowData.add(batchHeaderErrorReport.getSerialNumber()); // SEQUENCE NUMBER
		rowData.add(batchHeaderErrorReport.getQuarter()); // Quarter
		rowData.add(batchHeaderErrorReport.getFy()); // FY
		rowData.add(batchHeaderErrorReport.getTanOfDeductor()); // TAN
		rowData.add(batchHeaderErrorReport.getPanOfDeductor()); // PAN
		rowData.add(StringUtils.EMPTY); // Is this a revised return (Yes / No)
		rowData.add(batchHeaderErrorReport.getAy()); // AY
		rowData.add(batchHeaderErrorReport.getDeductorType()); // Type of deductor
		rowData.add(batchHeaderErrorReport.getEmployerName()); // Deductor Name
		rowData.add(batchHeaderErrorReport.getEmployerAddr1()); // Flat/Door/Block No
		rowData.add(batchHeaderErrorReport.getEmployerAddr2()); // Name of the Building
		rowData.add(batchHeaderErrorReport.getEmployerAddr3()); // Street/Road Name
		rowData.add(batchHeaderErrorReport.getEmployerAddr4()); // Area
		rowData.add(batchHeaderErrorReport.getEmployerAddr5()); // City
		rowData.add(batchHeaderErrorReport.getEmployerState()); // State
		rowData.add(batchHeaderErrorReport.getEmployerPin()); // Pincode
		rowData.add(batchHeaderErrorReport.getEmployerStd()); // Telephone
		rowData.add(batchHeaderErrorReport.getEmployerSTDAlt()); // Telephone (Alternate)
		rowData.add(StringUtils.EMPTY); // Fax
		rowData.add(batchHeaderErrorReport.getEmployerEmail()); // E-MAIL
		rowData.add(batchHeaderErrorReport.getEmployerEmailAlt()); // EMAIL(ALTERNATE)
		rowData.add(batchHeaderErrorReport.getNameofPersonResponsilbleForSal()); // NAME
		rowData.add(batchHeaderErrorReport.getpANOfResponsiblePerson()); // PAN
		rowData.add(batchHeaderErrorReport.getDesignationofPersonResponsilbleForSal()); // DESIGNATION
		rowData.add(batchHeaderErrorReport.getPersonResponsilbleAddr1()); // Flat/Door/Block No
		rowData.add(batchHeaderErrorReport.getPersonResponsilbleAddr2()); // Name of Building
		rowData.add(batchHeaderErrorReport.getPersonResponsilbleAddr3()); // Street Name/Road Name
		rowData.add(batchHeaderErrorReport.getPersonResponsilbleAddr4()); // Area
		rowData.add(batchHeaderErrorReport.getPersonResponsilbleAddr5()); // City
		rowData.add(batchHeaderErrorReport.getPersonResponsilbleState()); // State
		rowData.add(batchHeaderErrorReport.getPersonResponsilblePin()); // Pincode
		rowData.add(batchHeaderErrorReport.getPersonResponsilbleSTDCode()); // Telephone",
		rowData.add(batchHeaderErrorReport.getPersonResponsilbleSTDCodeAlt()); // "Telephone (Alternate)
		rowData.add(batchHeaderErrorReport.getMobileNumber()); // Mobile Number
		rowData.add(batchHeaderErrorReport.getPersonResponsilbleEmailId1()); // E-Mail
		rowData.add(StringUtils.EMPTY); // E-Mail (Alternate)
		rowData.add(StringUtils.EMPTY); // Has regular statement for Form 26Q filed for the earlier period?
		rowData.add(batchHeaderErrorReport.getPreviousRrrNo()); // Receipt No. of earlier statement filed for Form 26Q

		worksheet.getCells().importArrayList((ArrayList<Object>) rowData, 1, 0, false);
		Range headerColorRange2 = worksheet.getCells().createRange("A2:N2");
		headerColorRange2.setStyle(style1);
		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.freezePanes(0, 2, 0, 2);
		logger.info("generateBatchHeaderErrorReport method ended");
		return workbook;
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
	
	public boolean validateRemarks(String remarks) {
		boolean isValid = false;
		if (StringUtils.isBlank(remarks) || "A".equalsIgnoreCase(remarks) || "B".equalsIgnoreCase(remarks)
				|| "C".equalsIgnoreCase(remarks) || "S".equalsIgnoreCase(remarks) || "N".equalsIgnoreCase(remarks)) {
			isValid = true;
		}
		return isValid;
	}
}
