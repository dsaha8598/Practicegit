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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import com.ey.in.tds.common.dashboard.dto.TCSActivityTracker;
import com.ey.in.tds.common.dashboards.jdbc.dao.TCSActivityTrackerDAO;
import com.ey.in.tds.common.domain.FilingSectionCode;
import com.ey.in.tds.common.domain.FilingStateCode;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ActivityTrackerStatus;
import com.ey.in.tds.core.util.ActivityType;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.ReturnType;
import com.ey.in.tds.returns.domain.tcs.TCSFilingBatchHeaderBean;
import com.ey.in.tds.returns.domain.tcs.TCSFilingChallanDetailBean;
import com.ey.in.tds.returns.domain.tcs.TCSFilingDeducteeDetailBean;
import com.ey.in.tds.returns.domain.tcs.TCSFilingFileBean;
import com.ey.in.tds.returns.services.tcs.TCSRawFileGenerationService;
import com.microsoft.azure.storage.StorageException;
import com.poiji.bind.Poiji;
import com.poiji.option.PoijiOptions;
import com.poiji.option.PoijiOptions.PoijiOptionsBuilder;

@Service
public class TCSRpuFileReadingService extends TCSRawFileGenerationService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private TCSActivityTrackerDAO tcsActivityTrackerDAO;
	
	@Autowired
	private TCSBatchUploadDAO tCSBatchUploadDAO;

	@Async
	@Transactional(propagation = Propagation.SUPPORTS)
	public TCSFilingFileBean generateFile(File file, MultipartFile multipartFile, String fileType, String quarter,
			String deductorPan, String tanNumber, Integer assessmentYear, String tenantId, String userName,
			String formType, boolean isCorrection)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, InvalidFormatException {
		MultiTenantContext.setTenantId(tenantId);
		// get the data in entities from multipart file
		TCSFilingFileBean filingFileBean = readRPUFile(file, multipartFile, fileType, tenantId, assessmentYear,
				isCorrection);

		// generating the text file
		String textUrl = generateTextFile(filingFileBean, tenantId, fileType);

		filingLogic(fileType, quarter, assessmentYear, tanNumber, userName, textUrl, formType);

		saveInFilingStatus(assessmentYear, quarter, deductorPan, tanNumber, ReturnType.REGULAR.name(), tenantId,
				userName, fileType);
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		List<TCSActivityTracker> tracker = tcsActivityTrackerDAO.getActivityTrackerByTanYearTypeAndMonth(tanNumber,
				assessmentYear, ActivityType.QUARTERLY_TDS_FILING.getActivityType(), month);
		if (!tracker.isEmpty()) {
			tracker.get(0).setStatus(ActivityTrackerStatus.VALIDATED.name());
			tracker.get(0).setModifiedBy(userName);
			tracker.get(0).setModifiedDate(new Date());
			tcsActivityTrackerDAO.update(tracker.get(0));
		}
		logger.info("Completed generateFile method.");
		return filingFileBean;
	}

	public TCSFilingFileBean readRPUFile(File file, MultipartFile multipartFile, String formType, String tenantId,
			Integer assessmentYear, boolean isCorrection) throws IOException, InvalidFormatException {
		TCSFilingFileBean filingFileBean = new TCSFilingFileBean();

		logger.info("Entered in readRPUFile()");

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
		XSSFSheet deductorMasterSheet = workbook.getSheet("Collector Master");
		TCSFilingBatchHeaderBean tcsFilingBatchHeader = new TCSFilingBatchHeaderBean();
		// below field form number will specify the form type is 26Q or 27Q
		int lineCount = 2;
		tcsFilingBatchHeader.setLineNo(StringUtils.EMPTY + lineCount); // 1
		tcsFilingBatchHeader.setRecordType("BH"); // 2
		tcsFilingBatchHeader.setBatchNo("1"); // 3
		tcsFilingBatchHeader.setChallanCount(StringUtils.EMPTY);// 4 Calculated after processing
		tcsFilingBatchHeader.setFormNo(formType); // 5
		if (isCorrection) {
			// REMARKS FOR CORRECTION C3 - DEDUCTOR (EXCLUDING TAN), AND/OR CHALLAN, AND/OR
			// DEDUCTEE DETAILS
			tcsFilingBatchHeader.setTransactionType("C3"); // 6
			/**
			 * If there are updations in "BH" (Batch Header) - deductor details except TAN,
			 * then value should be "1" else it should be "0". If value is "0" then no
			 * updations can be done in the BH.
			 */
			tcsFilingBatchHeader.setBatchUpdationIndicator(String.valueOf(1)); // 7
		} else {
			tcsFilingBatchHeader.setTransactionType(StringUtils.EMPTY); // 6
			tcsFilingBatchHeader.setBatchUpdationIndicator(StringUtils.EMPTY); // 7
		}
		tcsFilingBatchHeader.setOriginalRrrNo(StringUtils.EMPTY); // 8
		if (StringUtils.isBlank(getCellValue(deductorMasterSheet, 44))) {
			tcsFilingBatchHeader.setPreviousRrrNo(StringUtils.EMPTY); // 9
		} else {
			tcsFilingBatchHeader.setPreviousRrrNo(getCellValue(deductorMasterSheet, 44)); // 9
		}
		tcsFilingBatchHeader.setRrrNo(StringUtils.EMPTY); // 10
		tcsFilingBatchHeader.setRrrDate(StringUtils.EMPTY); // 11
		if (isCorrection) {
			tcsFilingBatchHeader.setLastTanOfCollector(getCellValue(deductorMasterSheet, 5)); // 12
			tcsFilingBatchHeader.setOriginalRrrNo(getCellValue(deductorMasterSheet, 44)); // 8
		} else {
			tcsFilingBatchHeader.setLastTanOfCollector(StringUtils.EMPTY); // 12
		}
		tcsFilingBatchHeader.setLastTanOfCollector(getCellValue(deductorMasterSheet, 5)); // 13

		// NA 14
		tcsFilingBatchHeader.setPanOfDeductor(getCellValue(deductorMasterSheet, 6)); // 15
		tcsFilingBatchHeader.setAssessmentYr(getCellValue(deductorMasterSheet, 8)); // 17
		tcsFilingBatchHeader.setFinancialYr(getCellValue(deductorMasterSheet, 4)); // 13
		tcsFilingBatchHeader.setPeriod(getCellValue(deductorMasterSheet, 3));// 18
		tcsFilingBatchHeader.setEmployerName(getCellValue(deductorMasterSheet, 11));// 19
		if (StringUtils.isBlank(getCellValue(deductorMasterSheet, 12))) {
			tcsFilingBatchHeader.setEmployerBranchDiv("NA"); // 20
		} else {
			tcsFilingBatchHeader.setEmployerBranchDiv(getCellValue(deductorMasterSheet, 12)); // 20
		}
		tcsFilingBatchHeader.setEmployerAddr1(getCellValue(deductorMasterSheet, 13)); // 21

		tcsFilingBatchHeader.setEmployerAddr2(getCellValue(deductorMasterSheet, 14)); // 22
		tcsFilingBatchHeader.setEmployerAddr3(getCellValue(deductorMasterSheet, 15)); // 23
		tcsFilingBatchHeader.setEmployerAddr4(getCellValue(deductorMasterSheet, 16)); // 24
		tcsFilingBatchHeader.setEmployerAddr5(getCellValue(deductorMasterSheet, 17)); // 25
		String stateCode = getNullSafeStateCode(getCellValue(deductorMasterSheet, 18), stateMap);
		if (StringUtils.isNotBlank(getCellValue(deductorMasterSheet, 18))) {
			tcsFilingBatchHeader.setEmployerState(CommonUtil.d2.format(Double.valueOf(stateCode))); // 26
		} else {
			tcsFilingBatchHeader.setEmployerState(StringUtils.EMPTY); // 26
		}
		tcsFilingBatchHeader.setEmployerPin(getCellValue(deductorMasterSheet, 19)); // 27
		tcsFilingBatchHeader.setEmployerEmail(getCellValue(deductorMasterSheet, 24)); // 28

		String phoneNo = getCellValue(deductorMasterSheet, 21);
		if (StringUtils.isBlank(phoneNo)) {
			tcsFilingBatchHeader.setEmployerStd(StringUtils.EMPTY); // 29
		} else {
			if (phoneNo.contains("-")) {
				tcsFilingBatchHeader.setEmployerStd(phoneNo.substring(0, phoneNo.indexOf("-"))); // 29
			} else {
				tcsFilingBatchHeader.setEmployerStd(StringUtils.EMPTY); // 29
			}
		}
		if (StringUtils.isBlank(phoneNo)) {
			tcsFilingBatchHeader.setEmployerPhone(StringUtils.EMPTY); // 30
		} else {
			if (phoneNo.contains("-")) {
				tcsFilingBatchHeader.setEmployerPhone(phoneNo.substring(phoneNo.indexOf("-") + 1, phoneNo.length())); // 30
			} else {
				tcsFilingBatchHeader.setEmployerPhone(StringUtils.EMPTY); // 30
			}
		}
		String altPhoneNo = getCellValue(deductorMasterSheet, 22);
		if (StringUtils.isBlank(altPhoneNo)) {
			tcsFilingBatchHeader.setEmployerSTDAlt(StringUtils.EMPTY); // 29
		} else {
			if (altPhoneNo.contains("-")) {
				tcsFilingBatchHeader.setEmployerSTDAlt(altPhoneNo.substring(0, altPhoneNo.indexOf("-"))); // 29
			} else {
				tcsFilingBatchHeader.setEmployerSTDAlt(StringUtils.EMPTY); // 29
			}
		}
		if (StringUtils.isBlank(altPhoneNo)) {
			tcsFilingBatchHeader.setEmployerPhoneAlt(StringUtils.EMPTY); // 30
		} else {
			if (altPhoneNo.contains("-")) {
				tcsFilingBatchHeader
						.setEmployerPhoneAlt(altPhoneNo.substring(altPhoneNo.indexOf("-") + 1, altPhoneNo.length())); // 30
			} else {
				tcsFilingBatchHeader.setEmployerPhoneAlt(StringUtils.EMPTY); // 30
			}
		}
		String addressChangeIndicator = getCellValue(deductorMasterSheet, 20);
		if (StringUtils.isNotBlank(addressChangeIndicator)) {

			if (addressChangeIndicator.equalsIgnoreCase("yes") || addressChangeIndicator.equalsIgnoreCase("y")) {
				tcsFilingBatchHeader.setEmployerAddrChange("Y"); // 31
			} else {
				tcsFilingBatchHeader.setEmployerAddrChange("N"); // 31
			}

		} else {
			tcsFilingBatchHeader.setEmployerAddrChange("N"); // 31
		}
		// feign call to get description catagory
		ResponseEntity<ApiStatus<String>> catagoryValueResponse = mastersClient.getCatagoryValue(
				deductorMasterSheet.getRow(9).getCell(2, MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue());
		if (catagoryValueResponse.getBody().getData() != null) {
			tcsFilingBatchHeader.setCollectorType(catagoryValueResponse.getBody().getData());
		} else {
			workbook.close();
			throw new CustomException("Invalid Deductor Type mentioned in RPU file", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		tcsFilingBatchHeader.setNameofPersonResponsilbleForTaxCollection(getCellValue(deductorMasterSheet, 27)); // 33
		tcsFilingBatchHeader.setDesignationofPersonResponsilbleForTaxCollection(getCellValue(deductorMasterSheet, 29)); // 34
		tcsFilingBatchHeader.setPersonResponsilbleAddr1(getCellValue(deductorMasterSheet, 30));

		tcsFilingBatchHeader.setPersonResponsilbleAddr2(getCellValue(deductorMasterSheet, 31)); // 36
		tcsFilingBatchHeader.setPersonResponsilbleAddr3(getCellValue(deductorMasterSheet, 32)); // 37
		tcsFilingBatchHeader.setPersonResponsilbleAddr4(getCellValue(deductorMasterSheet, 33)); // 38
		tcsFilingBatchHeader.setPersonResponsilbleAddr5(getCellValue(deductorMasterSheet, 34)); // 39

		stateCode = getNullSafeStateCode(getCellValue(deductorMasterSheet, 35), stateMap);
		if (StringUtils.isNotBlank(getCellValue(deductorMasterSheet, 35))) {
			tcsFilingBatchHeader.setPersonResponsilbleState(CommonUtil.d2.format(Double.valueOf(stateCode))); // 40
		} else {
			tcsFilingBatchHeader.setPersonResponsilbleState(StringUtils.EMPTY); // 40
		}
		tcsFilingBatchHeader.setPersonResponsilblePin(getCellValue(deductorMasterSheet, 36)); // 41
		tcsFilingBatchHeader.setPersonResponsilbleEmailId1(getCellValue(deductorMasterSheet, 40)); // 42
		tcsFilingBatchHeader.setMobileNumber(getCellValue(deductorMasterSheet, 39)); // 43

		String personResponsiblePhoneNo = getCellValue(deductorMasterSheet, 37);
		if (StringUtils.isBlank(personResponsiblePhoneNo)) {
			tcsFilingBatchHeader.setPersonResponsilbleSTDCode(StringUtils.EMPTY); // 44
		} else {
			if (personResponsiblePhoneNo.contains("-")) {
				tcsFilingBatchHeader.setPersonResponsilbleSTDCode(
						personResponsiblePhoneNo.substring(0, personResponsiblePhoneNo.indexOf("-"))); // 44
			} else {
				tcsFilingBatchHeader.setPersonResponsilbleSTDCode(StringUtils.EMPTY); // 44
			}

		}
		if (StringUtils.isBlank(personResponsiblePhoneNo)) {
			tcsFilingBatchHeader.setPersonResponsilbleTelePhone(StringUtils.EMPTY); // 45
		} else {
			if (personResponsiblePhoneNo.contains("-")) {
				tcsFilingBatchHeader.setPersonResponsilbleTelePhone(personResponsiblePhoneNo
						.substring(personResponsiblePhoneNo.indexOf("-") + 1, personResponsiblePhoneNo.length())); // 45
			} else {
				tcsFilingBatchHeader.setPersonResponsilbleTelePhone(StringUtils.EMPTY); // 45
			}

		}

		String personResponsibleAltPhoneNo = getCellValue(deductorMasterSheet, 38);
		if (StringUtils.isBlank(personResponsibleAltPhoneNo)) {
			tcsFilingBatchHeader.setPersonResponsilbleSTDCodeAlt(StringUtils.EMPTY); // 44
		} else {
			if (personResponsibleAltPhoneNo.contains("-")) {
				tcsFilingBatchHeader.setPersonResponsilbleSTDCodeAlt(
						personResponsibleAltPhoneNo.substring(0, personResponsibleAltPhoneNo.indexOf("-"))); // 44
			} else {
				tcsFilingBatchHeader.setPersonResponsilbleSTDCodeAlt(StringUtils.EMPTY); // 44
			}

		}
		if (StringUtils.isBlank(personResponsibleAltPhoneNo)) {
			tcsFilingBatchHeader.setPersonResponsilbleTelePhoneAlt(StringUtils.EMPTY); // 45
		} else {
			if (personResponsibleAltPhoneNo.contains("-")) {
				tcsFilingBatchHeader.setPersonResponsilbleTelePhoneAlt(personResponsibleAltPhoneNo
						.substring(personResponsibleAltPhoneNo.indexOf("-") + 1, personResponsibleAltPhoneNo.length())); // 45
			} else {
				tcsFilingBatchHeader.setPersonResponsilbleTelePhoneAlt(StringUtils.EMPTY); // 45
			}

		}
		tcsFilingBatchHeader.setPersonResponsilbleAddrChange(getCellValue(deductorMasterSheet, 42)); // 46
		// Total/sum *(Challan: Column L)
		tcsFilingBatchHeader.setGrossTdsTotalAsPerChallan(StringUtils.EMPTY); // 47
		tcsFilingBatchHeader.setUnMatchedChalanCnt(StringUtils.EMPTY); // 48
		tcsFilingBatchHeader.setCountOfSalaryDetailRec(StringUtils.EMPTY); // 49
		tcsFilingBatchHeader.setGrossTotalIncomeSd(StringUtils.EMPTY); // 50
		tcsFilingBatchHeader.setApprovalTaken("N"); // 51

		// Whether regular statement for Form 26Q filed for earlier period
		if (isCorrection) {
			tcsFilingBatchHeader.setApprovalTaken(StringUtils.EMPTY);
			// TODO need to add dynamically
			tcsFilingBatchHeader.setLastCollectorType("K"); // 53
		} else {
			if (StringUtils.isBlank(tcsFilingBatchHeader.getPreviousRrrNo())) {
				tcsFilingBatchHeader.setIsRegularStatementForForm27EQFiledForEArlierPeriod("N"); // 52
			} else {
				tcsFilingBatchHeader.setIsRegularStatementForForm27EQFiledForEArlierPeriod("Y"); // 52
			}
			tcsFilingBatchHeader.setLastCollectorType(StringUtils.EMPTY); // 53
		}

		if (tcsFilingBatchHeader.getCollectorType().equals("S") || tcsFilingBatchHeader.getCollectorType().equals("E")
				|| tcsFilingBatchHeader.getCollectorType().equals("H")
				|| tcsFilingBatchHeader.getCollectorType().equals("N")) {
			stateCode = getNullSafeStateCode(getCellValue(deductorMasterSheet, 18), stateMap);
			tcsFilingBatchHeader.setStateName(CommonUtil.d2.format(Double.valueOf(stateCode))); // 54
		} else {
			tcsFilingBatchHeader.setStateName(StringUtils.EMPTY); // 54 State Name
		}
		tcsFilingBatchHeader.setPaoCode(StringUtils.EMPTY); // 55 TODO - Future
		tcsFilingBatchHeader.setDdoCode(StringUtils.EMPTY); // 56 TODO - Future
		tcsFilingBatchHeader.setMinistryName(StringUtils.EMPTY); // 57
		tcsFilingBatchHeader.setMinistryNameOther(StringUtils.EMPTY); // 58
		if (StringUtils.isBlank(getCellValue(deductorMasterSheet, 28))) {
			tcsFilingBatchHeader.setpANOfResponsiblePerson(StringUtils.EMPTY); // 59
		} else {
			tcsFilingBatchHeader.setpANOfResponsiblePerson(getCellValue(deductorMasterSheet, 28)); // 59
		}
		tcsFilingBatchHeader.setPaoRegistrationNo(StringUtils.EMPTY); // 60
		tcsFilingBatchHeader.setDdoRegistrationNo(StringUtils.EMPTY); // 61
		tcsFilingBatchHeader.setEmployerEmailAlt(StringUtils.EMPTY); // 64
		tcsFilingBatchHeader.setPersonResponsilbleEmailIdAlt(StringUtils.EMPTY); // 67
		tcsFilingBatchHeader.setaIN(StringUtils.EMPTY); // 68
		tcsFilingBatchHeader.setgSTN(StringUtils.EMPTY); // 69
		tcsFilingBatchHeader.setBatchHash(StringUtils.EMPTY); // 70

		logger.info("Processing the challan details");
		// Reading Challan details
		PoijiOptions options = PoijiOptionsBuilder.settings().sheetIndex(workbook.getSheetIndex("Challan details"))
				.build();
		List<TCSFilingChallanDetailBean> filingChallanDetails = Poiji.fromExcel(file, TCSFilingChallanDetailBean.class,
				options);

		logger.info("Processing Deductee details");
		PoijiOptions options2 = PoijiOptionsBuilder.settings().sheetIndex(workbook.getSheetIndex("Deductee details"))
				.build();
		List<TCSFilingDeducteeDetailBean> filingDeducteeDetails = Poiji.fromExcel(file,
				TCSFilingDeducteeDetailBean.class, options2);
		logger.info("No of challans rows in file: {}", filingChallanDetails.size());
		logger.info("No of deductee rows in file : {}", filingDeducteeDetails.size());
		filingFileBean.setBatchHeaderBean(tcsFilingBatchHeader);
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
		Map<String, List<TCSFilingDeducteeDetailBean>> challanDeducteesMap = new HashMap<>();
		for (TCSFilingDeducteeDetailBean filingDeducteeDetail : filingDeducteeDetails) {
			filingDeducteeDetail.setFormType(formType);
			List<TCSFilingDeducteeDetailBean> challanDeductees = challanDeducteesMap
					.get(filingDeducteeDetail.getChallanRecordNo());
			if (challanDeductees == null) {
				challanDeductees = new ArrayList<>();
				challanDeducteesMap.put(filingDeducteeDetail.getChallanRecordNo(), challanDeductees);
			}
			challanDeductees.add(filingDeducteeDetail);
		}
		for (TCSFilingChallanDetailBean filingChallanDetail : filingChallanDetails) {
			// Set this initially
			filingChallanDetail
					.setChallanMonth(Integer.parseInt(filingChallanDetail.getDateOfBankChallanNo().substring(3, 5)));
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
			// 16 populated from file
			// 17 Nothing to do
			filingChallanDetail.setDateOfBankChallanNo(filingChallanDetail.getDateOfBankChallanNo()); // 18
			// 19 Nothing to do
			// 20 Nothing to do
			filingChallanDetail.setSection(StringUtils.EMPTY);
			// 22 'Oltas TDS / TCS -Income Tax '
			filingChallanDetail.setOltasTCSIncomeTax(getFormattedValue(filingChallanDetail.getTcsIncomeTaxC()));
			// 23 'Oltas TDS / TCS -Surcharge '
			filingChallanDetail.setOltasTCSSurcharge(getFormattedValue(filingChallanDetail.getTcsSurchargeC()));
			// 24 'Oltas TDS / TCS - Cess'
			filingChallanDetail.setOltasTCSCess(getFormattedValue(filingChallanDetail.getTcsCessC()));
			// 25 Oltas TDS / TCS - Interest Amount
			filingChallanDetail.setOltasTCSInterest(getFormattedValue(filingChallanDetail.getTdsInterest()));
			// 26 Oltas TDS / TCS - Others (amount)
			filingChallanDetail.setOltasTCSOthers(getFormattedValue(filingChallanDetail.getTdsOthers()));
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

			List<TCSFilingDeducteeDetailBean> challanDeductees = challanDeducteesMap
					.get(filingChallanDetail.getChallanDetailRecordNo());
			logger.info("Processing challan {} and deductees {}", filingChallanDetail.getChBatchNo(), challanDeductees);
			if (challanDeductees == null) {
				challanDeductees = new ArrayList<>();
			}

			double totalTaxDepositedAsPerDeducteeAnex = 0;
			double tdsIncomeTaxC = 0;
			double tdsSurchargeC = 0;
			double tdsCessC = 0;
			double sumTotalIncTaxDedAtSource = 0;

			if (challanDeductees != null && !challanDeductees.isEmpty()) {
				for (TCSFilingDeducteeDetailBean filingDeducteeDetail : challanDeductees) {
					// 1 populated from file
					lineCount++;
					filingDeducteeDetail.setLineNo(StringUtils.EMPTY + lineCount);
					// 2
					filingDeducteeDetail.setRecType("DD");
					// 3
					filingDeducteeDetail.setDdBatchNo(filingChallanDetail.getChBatchNo());
					// 4 populated from file
					// 5
					filingDeducteeDetail.setCollecteeDetailRecNo(
							StringUtils.EMPTY + (filingChallanDetail.getDeducteeDetailBeanList().size() + 1));
					// 6
					// 7
					filingDeducteeDetail.setDeducteeSerialNo(
							StringUtils.EMPTY + (filingDeducteeDetails.indexOf(filingDeducteeDetail) + 1));
					// 8
					// 9
					// 10 populated from file
					if (StringUtils.isBlank(filingDeducteeDetail.getCollecteePan())) {
						filingDeducteeDetail.setCollecteePan("PANNOTAVBL");
					}
					// 11
					// 12
					if ("PANNOTAVBL".equals(filingDeducteeDetail.getCollecteePan())) {
						long number = (long) Math.floor(Math.random() * 9000000000L) + 1000000000L;
						filingDeducteeDetail.setCollecteeRefNo(Long.valueOf(number).toString());
					} else {
						filingDeducteeDetail.setCollecteeRefNo(StringUtils.EMPTY);
					}
					// 13

					// 14
					filingDeducteeDetail.setTcsIncomeTaxDD(getFormattedValue(filingDeducteeDetail.getTcsIncomeTaxDD()));
					tdsIncomeTaxC = tdsIncomeTaxC + Double.parseDouble(filingDeducteeDetail.getTcsIncomeTaxDD());

					// 15
					filingDeducteeDetail.setTcsSurchargeDD(getFormattedValue(filingDeducteeDetail.getTcsSurchargeDD()));
					tdsSurchargeC = tdsSurchargeC + Double.parseDouble(filingDeducteeDetail.getTcsSurchargeDD());

					// 16
					filingDeducteeDetail.setTcsCessDD(getFormattedValue(filingDeducteeDetail.getTcsCessDD()));
					tdsCessC = tdsCessC + Double.parseDouble(filingDeducteeDetail.getTcsCessDD());
					// 17
					filingDeducteeDetail
							.setTotalIncomeTaxCollected((getFormattedValue(filingDeducteeDetail.getTcsIncomeTaxDD())));
					sumTotalIncTaxDedAtSource = sumTotalIncTaxDedAtSource
							+ Double.parseDouble(filingDeducteeDetail.getTcsIncomeTaxDD());

					// 18

					// 19
					filingDeducteeDetail
							.setTotalTaxDeposited(getFormattedValue(filingDeducteeDetail.getTotalTaxDeposited()));
					totalTaxDepositedAsPerDeducteeAnex = totalTaxDepositedAsPerDeducteeAnex
							+ Double.parseDouble(filingDeducteeDetail.getTotalTaxDeposited());
					// 20
					// 21
					// 22
					filingDeducteeDetail
							.setAmountReceived((getFormattedValue(filingDeducteeDetail.getAmountReceived())));
					// 23

					// 26
					filingDeducteeDetail.setRateAtWhichTaxCollected(
							CommonUtil.df4.format(Double.valueOf(filingDeducteeDetail.getRateAtWhichTaxCollected())));
					filingChallanDetail.getDeducteeDetailBeanList().add(filingDeducteeDetail);

					// 30
					if ("PANNOTAVBL".equals(filingDeducteeDetail.getCollecteePan())) {
						filingDeducteeDetail.setReason("C");
					}

					// 33
					String section = filingDeducteeDetail.getCollectionCode();
					if (assessmentYear <= 2013) {
						section = (("194I".equals(section) || "94I".equals(section)) ? "94I" : section);
					} else {
						section = (("194I".equals(section) || "94I".equals(section)) ? "194IB" : section);
					}
					if (sectionCodeMap.get(section) == null) {
						filingDeducteeDetail.setCollectionCode(section);
					} else {
						filingDeducteeDetail.setCollectionCode(sectionCodeMap.get(section));
					}
				}
			}
			// TODO
			if (isCorrection) {
				totalTaxDepositedAsPerDeducteeAnex = 0;
				tdsIncomeTaxC = 0;
				tdsSurchargeC = 0;
				tdsCessC = 0;
				sumTotalIncTaxDedAtSource = 0;
			}
			// 29
			filingChallanDetail
					.setTotalTaxDepositedAsPerCollecteeAnex(getFormattedValue(totalTaxDepositedAsPerDeducteeAnex));
			// 30
			filingChallanDetail.setTcsIncomeTaxC(getFormattedValue(tdsIncomeTaxC));
			// 31
			filingChallanDetail.setTcsSurchargeC(getFormattedValue(tdsSurchargeC));
			// 32
			filingChallanDetail.setTcsCessC(getFormattedValue(tdsCessC));
			// 33
			filingChallanDetail.setSumTotalIncTaxDeductedAtSource(getFormattedValue(sumTotalIncTaxDedAtSource));

			filingChallanDetail.setCountOfCollecteeDetail(
					StringUtils.EMPTY + filingChallanDetail.getDeducteeDetailBeanList().size());
		}
		// Calculate total and populate in batch header bean
		Double totalAmount = new Double(0);
		if (filingChallanDetails != null) {
			for (TCSFilingChallanDetailBean challan : filingChallanDetails) {
				if (challan.getTotalOfDepositAmountAsPerChallan() != null) {
					totalAmount = totalAmount + Double.parseDouble(challan.getTotalOfDepositAmountAsPerChallan());
				}
			}
		}
		tcsFilingBatchHeader.setGrossTdsTotalAsPerChallan(CommonUtil.d2.format(totalAmount) + ".00");
		tcsFilingBatchHeader.setChallanCount(StringUtils.EMPTY + filingChallanDetails.size());

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

	private String getFormattedValue(String value) {
		String formattedValue = "0.00";
		if (StringUtils.isNotBlank(value) && Double.valueOf(value) > 0) {
			formattedValue = CommonUtil.d2
					.format(BigDecimal.valueOf(Double.valueOf(value)).setScale(0, RoundingMode.UP)) + ".00";
		}
		return formattedValue;
	}

	private String getFormattedValue(double value) {
		String formattedValue = "0.00";
		if (Double.valueOf(value) > 0) {
			formattedValue = CommonUtil.d2
					.format(BigDecimal.valueOf(Double.valueOf(value)).setScale(0, RoundingMode.UP)) + ".00";
		}
		return formattedValue;
	}

	private String convertToYearRange(String value) {
		if (StringUtils.isNotBlank(value) && value.length() == 6) {
			return value.substring(0, 4) + "-" + value.substring(4, value.length());
		}
		return value;
	}

	private String convertToDate(String value) {
		if (StringUtils.isNotBlank(value) && value.length() == 8) {
			return value.substring(0, 2) + "/" + value.substring(2, 4) + "/" + value.substring(4, value.length());
		}
		return value;
	}

	public TCSBatchUpload generateExcellFromText(String fileUrl, String tenantID,String tanNumber,String userName) throws Exception {
		File file = null;
		String line = null;
		String uploadedfileUrl = null;
		TCSBatchUpload tcsBatchUpload=null;
		file = blobStorageService.getFileFromBlobUrl(tenantID, fileUrl);
		tcsBatchUpload=saveToBatchUpload(null, "Processing", null,tanNumber,userName);
		logger.info("Batch Upload "+tcsBatchUpload+"{}");
		if (file != null) {

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
				// generating form sheet
				generateFormSheet(workbook, formSheetData, reverseStateCodeMap);
				// generating challan sheet
				generateChalanSheet(workbook, chalanSheetData);
				// generating deductee sheet
				generateDeducteeSheet(workbook, deducteeSheetData);

				File generatedExcelFile = new File("27EQ_Excel" + new Date().getTime() + ".xlsx");
				OutputStream out = new FileOutputStream(generatedExcelFile);
				workbook.save(out, SaveFormat.XLSX);
				tcsBatchUpload=saveToBatchUpload(tcsBatchUpload, "Processed", generatedExcelFile,tanNumber,userName);

				//uploadedfileUrl = blob.uploadExcelToBlobWithFile(generatedExcelFile, tenantID);
				logger.info("Uploaded file url {}", uploadedfileUrl);

			} catch (Exception exception) {
				logger.info("exception occured while reading the text file {}" + exception);
			}
			return tcsBatchUpload;
		} else {
			throw new CustomException("No files found");
		}

	}

	private Workbook generateFormSheet(Workbook workbook, List<String> formSheetData,
			Map<String, String> reverseStateCodeMap) throws Exception {
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

		worksheet.setName("Collector Master");

		String[] headers = { "Collector Master", "Particulars of statement", "Quarter", "FY", "TAN", "PAN",
				"Is this a revised return (Yes / No)", "AY", "Type of deductor", "Collector Details", "Name",
				"Branch/Division", "Flat/Door/Block No.", "Name of the Building", "Street/Road Name", "Area", "City",
				"State", "Pincode", "Address Change", "Telephone", "Telephone (Alternate)", "Fax", "E-Mail",
				"E-Mail (Alternate)", "Responsible Person Details", "Name", "PAN", "Designation", "Flat/Door/Block No.",
				"Name of Building", "Street Name/Road Name", "Area", "City", "State", "Pincode", "Telephone",
				"Telephone (Alternate)", "Mobile Number", "E-Mail", "E-Mail (Alternate)", "Address Change",
				"Has regular statement for Form 27EQ filed for the earlier period?",
				"Receipt No. of earlier statement filed for Form 27EQ" };
		worksheet.getCells().importArray(headers, 1, 1, true);
		String[] batchHeaderValues = formSheetData.get(1).split(Pattern.quote("^")); // splitting batch header

		List<Object> rowData = new ArrayList<>();
		rowData.add(batchHeaderValues[17]); // Quarter
		String fyRange = convertToYearRange(batchHeaderValues[16]);
		rowData.add(fyRange); // FY
		rowData.add(batchHeaderValues[12]); // TAN
		rowData.add(batchHeaderValues[14]); // PAN
		rowData.add("Yes"); // Is this a revised return (Yes / No)
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
		rowData.add(batchHeaderValues[19]); // Branch/Division
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
		rowData.add(batchHeaderValues[30]); // Address Change
		rowData.add(batchHeaderValues[28] + "-" + batchHeaderValues[29]); // Telephone
		rowData.add(batchHeaderValues[62] + "-" + batchHeaderValues[63]); // Telephone (Alternate)
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
		rowData.add(batchHeaderValues[42]); // Telephone",
		rowData.add(batchHeaderValues[65] + "-" + batchHeaderValues[66]); // "Telephone (Alternate)
		rowData.add(batchHeaderValues[47]); // Mobile Number
		rowData.add(batchHeaderValues[41]); // E-Mail
		rowData.add(StringUtils.EMPTY); // E-Mail (Alternate)
		rowData.add(batchHeaderValues[45]); // Address Change
		// TODO
		rowData.add(StringUtils.EMPTY); // Has regular statement for Form 26Q filed for the earlier period?
		rowData.add(batchHeaderValues[8]); // Receipt No. of earlier statement filed for Form 26Q

		worksheet.getCells().importArrayList((ArrayList<Object>) rowData, 3, 2, true);
		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		logger.info("generateFormSheet method ended");
		return workbook;
	}

	private Style setBorder(Style style) {
		style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());
		return style;
	}

	private Workbook generateChalanSheet(Workbook workbook, List<String> chalanSheetData) throws Exception {
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
			rowData.add(challanDetailData[26]); // Last total tax deposited (₹)
			rowData.add(challanDetailData[26]); // Total amount deposited
			rowData.add(challanDetailData[35]); // Cheque / DD No. (if any)
			rowData.add(challanDetailData[15]); // Last BSR Code / 24G Receipt No.
			rowData.add(challanDetailData[15]); // BSR Code / Form 24G Receipt No
			String receiptDate = convertToDate(challanDetailData[17]);
			rowData.add(receiptDate); // Last date on which tax deposited
			rowData.add(receiptDate); // Date on which amount deposited through challan / Date of Transfer Voucher
										// (DD/MM/YYYY)
			/**
			 * Mention Bank Challan Number specified in the corresponding regular or last
			 * correction statement. Mandatory if book entry flag or Nil challan indicator
			 * is "N".
			 */
			rowData.add(challanDetailData[11]); // Last DDO / Transfer Voucher / Challan serial no
			rowData.add(challanDetailData[11]); // Challan Serial No. / DDO Serial No. of Form No. 24G
			rowData.add("No"); // Mode of deposit through book adjustment (Yes/No)
			rowData.add(challanDetailData[33]); // Interest to be allocated / apportioned (₹)
			rowData.add(challanDetailData[34]); // Others (₹)
			rowData.add(challanDetailData[39]); // Minor head of challan 200-TDS payable by taxpayer 400-TDS regular
			// assessment (Raised by IT Dept)
			rowData.add(StringUtils.EMPTY); // Challan Balance as per consolidated file
			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
			Range headerColorRange2 = worksheet.getCells().createRange("A" + rowIndex + ":" + "X" + rowIndex + "");
			headerColorRange2.setStyle(style1);
		}
		logger.info("generateChalanSheet method ended");
		return workbook;
	}

	private Workbook generateDeducteeSheet(Workbook workbook, List<String> deducteeSheetData) throws Exception {
		logger.info("generateDeducteeSheet method started");
		Worksheet worksheet = workbook.getWorksheets().add("Deductee details");
		worksheet.setGridlinesVisible(false);
		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		String[] headers = { "Row Number", "Challan Number", "Update mode for collectee \n (Add / Update / PAN Update)",
				"BSR Code of \n branch where tax \n deposited",
				"Date on which" + "\n" + " tax deposited" + "\n" + " (DD/MM/YYYY)",
				"Transfer Voucher / Challan Serial" + "\n" + " No.", "Section Under" + "\n" + " Which Payment Made",
				"Total TDS to be allocated among" + "\n" + " deductees as in the" + "\n"
						+ " vertical total of col. 21 (₹)",
				"Des-TDS", "Others (₹)", "Total (7+8+9) (₹)", "Sr. No",
				"Collectee by reference number provided " + "\n" + "by the collector (if available)",
				"Last PAN of" + "\n" + " collectee", "PAN of deductee", "Name of collectee",
				"Date of payment /" + "\n" + " credit (DD/MM/YYYY)", "Amount paid /" + "\n" + " credited (₹)",
				"TDS (₹)", "Surcharge (₹)", "Education Cess (₹)",
				"Total tax " + "\n" + "deducted" + "\n" + " (18+19+20) (₹)", "Last total tax " + "\n" + "deducted (₹)",
				"Total tax" + "\n" + " deposited (₹)", "Last total tax" + "\n" + " deducted (₹)",
				"Date of deduction" + "\n" + " (DD/MM/YYYY)",
				"Remarks (Reason for non-deduction " + "\n" + "/ lower deduction / higher deduction / threshold)",
				"Collectee code (1-Company, 2-Other " + "\n" + "than Company)",
				"Rate at which" + "\n" + " tax deducted", "Paid by book" + "\n" + " entry or " + "\n" + "otherwise",
				"Certificate number issued by the assessing officer u/s 197" + "\n"
						+ " for non-deduction / lower deduction" };

		worksheet.getCells().importArray(headers, 0, 1, false);
		Style style5 = workbook.createStyle();
		style5.setForegroundColor(Color.fromArgb(91, 155, 213));
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(true);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		// setting border
		setBorder(style5);
		Range headerColorRange1 = worksheet.getCells().createRange("B1:AF1");
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
		autoFilter.setRange("B1:AF1");
		ResponseEntity<ApiStatus<List<FilingSectionCode>>> sectionCodeResponse = mastersClient
				.getAllSectionCode(MultiTenantContext.getTenantId());
		List<FilingSectionCode> sectionCodes = sectionCodeResponse.getBody().getData();
		Map<String, String> sectionCodeMap = new HashMap<>();
		for (FilingSectionCode filingSectionCode : sectionCodes) {
			sectionCodeMap.put(filingSectionCode.getSectionCode(), filingSectionCode.getSectionName());
		}
		int rowIndex = 1;
		for (String data : deducteeSheetData) {
			String[] deducteeDetailData = data.split(Pattern.quote("^"), 48);
			List<Object> rowData = new ArrayList<>();
			rowData.add(StringUtils.EMPTY); // Row Number
			rowData.add(deducteeDetailData[3]); // Challan Number
			rowData.add(StringUtils.EMPTY); // Update mode for deductee (Add / Update / PAN Update)
			rowData.add(StringUtils.EMPTY);// BSR Code of branch where tax deposited
			String taxDepositDate = convertToDate(deducteeDetailData[24]);
			rowData.add(taxDepositDate); // Date on which tax deposited (DD/MM/YYYY)
			rowData.add(StringUtils.EMPTY); // Transfer Voucher / Challan Serial No.
			if (StringUtils.isNotBlank(deducteeDetailData[33])) {
				if (StringUtils.isNotBlank(sectionCodeMap.get(deducteeDetailData[33]))) {
					rowData.add(sectionCodeMap.get(deducteeDetailData[33].trim())); // Section Under Which Payment Made
				} else {
					rowData.add(StringUtils.EMPTY);
				}
			} else {
				rowData.add(StringUtils.EMPTY);
			}
			rowData.add(deducteeDetailData[17]); // Total TDS to be allocated among deductees as in the vertical total
													// of col.
			// 21 (₹)
			rowData.add(deducteeDetailData[13]); // Des-TDS
			rowData.add(StringUtils.EMPTY); // Others (₹)
			rowData.add(deducteeDetailData[17]); // Total (7+8+9) (₹)
			rowData.add(rowIndex); // Sr. No.
			rowData.add(deducteeDetailData[4]); // Deductee by reference number provided by the deductor (if available)
			rowData.add(deducteeDetailData[9]); // Last PAN of deductee
			rowData.add(deducteeDetailData[9]); // PAN of deductee
			rowData.add(deducteeDetailData[12]); // 22 Name of deductee
			String taxPaymentDate = convertToDate(deducteeDetailData[22]);
			rowData.add(taxPaymentDate); // Date of payment / credit (DD/MM/YYYY)
			rowData.add(deducteeDetailData[21]); // Amount paid / credited (₹)
			rowData.add(deducteeDetailData[13]); // TDS (₹)
			rowData.add(deducteeDetailData[14]); // Surcharge (₹)
			rowData.add(StringUtils.EMPTY); // Education Cess (₹)
			rowData.add(deducteeDetailData[16]); // Total tax deducted (18+19+20) (₹)
			// TODO
			rowData.add(deducteeDetailData[16]); // Last total tax deducted (₹)
			rowData.add(deducteeDetailData[18]); // Total tax deposited (₹)
			// TODO
			rowData.add(deducteeDetailData[18]); // Last total tax deducted (₹)
			String taxDeductedDate = convertToDate(deducteeDetailData[23]);
			rowData.add(taxDeductedDate); // Date of deduction (DD/MM/YYYY)
			rowData.add(deducteeDetailData[29]); // Remarks (Reason for non-deduction / lower deduction / higher
													// deduction /
			// threshold)
			rowData.add(deducteeDetailData[7]); // Deductee code (1-Company, 2-Other than Company)
			rowData.add(deducteeDetailData[25]); // Rate at which tax deducted
			rowData.add(StringUtils.EMPTY); // Paid by book entry or otherwise
			rowData.add(deducteeDetailData[26]); // Certificate number issued by the assessing officer u/s 197 for
			// non-deduction / lower deduction

			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 1, false);
			Range headerColorRange2 = worksheet.getCells().createRange("B" + rowIndex + ":" + "AF" + rowIndex + "");
			headerColorRange2.setStyle(style1);
		}
		logger.info("generateDeducteeSheet method ended");
		return workbook;
	}
	
	public TCSBatchUpload saveToBatchUpload(TCSBatchUpload batch,String status,File file,String tanNumber,String userName) throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		String fileName="";
		String fileUrl="";
		TCSBatchUpload tcsBatch=null;
		if(file!=null) {
			fileUrl=blob.uploadExcelToBlobWithFile(file, MultiTenantContext.getTenantId());
			fileName=file.getName();
		}
		if(batch!=null && batch.getId()!=null) {
			//batch=tCSBatchUploadDAO.findById(batch.getId()).get(0);
			logger.info("updating generated Excel File for 27EQ in tcs batch upload {}");
			batch.setStatus(status);
			batch.setFileName(fileName);
			batch.setFilePath(fileUrl);
			batch.setSuccessFileUrl(fileUrl);
			batch.setProcessEndTime(new Timestamp(System.currentTimeMillis()));
			batch.setModifiedDate(new Timestamp(System.currentTimeMillis()));
			batch.setModifiedBy(fileUrl);
			batch.setRowsCount(0l);
			return tCSBatchUploadDAO.update(batch);
		}else {
			logger.info("Saving generated Excel File for 27EQ in tcs batch upload {}");
		    tcsBatch=new TCSBatchUpload();
			tcsBatch.setActive(true);
			tcsBatch.setStatus(status);
			tcsBatch.setProcessStartTime(new Timestamp(System.currentTimeMillis()));
			tcsBatch.setAssessmentYear(Calendar.getInstance().get(Calendar.YEAR));
			tcsBatch.setCreatedDate(new Timestamp(System.currentTimeMillis()));
			tcsBatch.setCollectorMasterTan(tanNumber);
			tcsBatch.setCreatedBy(userName);
			tcsBatch.setUploadType("27EQ_EXCEL_REPORT");
			return tCSBatchUploadDAO.save(tcsBatch);
			
		}
	}

}
