package com.ey.in.tds.returns.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.ey.in.tcs.common.domain.dividend.NonResidentWithholdingDetails;
import com.ey.in.tds.common.challan.dto.Receipt;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.dashboard.dto.ActivityTracker;
import com.ey.in.tds.common.dashboards.jdbc.dao.ActivityTrackerDAO;
import com.ey.in.tds.common.domain.FilingDeductorCollector;
import com.ey.in.tds.common.domain.FilingMinistryCode;
import com.ey.in.tds.common.domain.FilingMinorHeadCode;
import com.ey.in.tds.common.domain.FilingSectionCode;
import com.ey.in.tds.common.domain.FilingStateCode;
import com.ey.in.tds.common.domain.dividend.ResidentWithholdingDetails;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.Form16DetailsDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.NrTransactionsMetaDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.Form16Details;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.dto.DeducteeDetailDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.dto.masters.deductor.TanAddressDTO;
import com.ey.in.tds.common.dto.onboarding.deductee.DeducteeMasterDTO;
import com.ey.in.tds.common.dto.returns.ChallanReceiptDTO;
import com.ey.in.tds.common.dto.returns.FilingFilesComparator;
import com.ey.in.tds.common.dto.returns.FilingFilesDTO;
import com.ey.in.tds.common.jdbc.returns.dao.FilingFilesDAO;
import com.ey.in.tds.common.model.emailnotification.Email;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorOnboardingInformationDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.NrTransactionsMeta;
import com.ey.in.tds.common.resturns.response.dto.FilingFilesResponseDTO;
import com.ey.in.tds.common.resturns.response.dto.FilingStatusResponseDTO;
import com.ey.in.tds.common.returns.jdbc.dto.FilingFiles;
import com.ey.in.tds.common.returns.jdbc.dto.FilingStatus;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.service.EmailService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ActivityTrackerStatus;
import com.ey.in.tds.core.util.ActivityType;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.FilingFileRemittance;
import com.ey.in.tds.core.util.FilingFilesType;
import com.ey.in.tds.core.util.ReturnType;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.jdbc.dao.FilingStatusDAO;
import com.ey.in.tds.returns.domain.FilingBatchHeaderBean;
import com.ey.in.tds.returns.domain.FilingChallanDetailBean;
import com.ey.in.tds.returns.domain.FilingChallanDetailComparator;
import com.ey.in.tds.returns.domain.FilingDeducteeDetailBean;
import com.ey.in.tds.returns.domain.FilingFileBean;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.storage.StorageException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

@Service
public class FilingFileGenerationService extends RawFileGenerationService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private RPUFileReadingService rpuFileReadingService;

	@Value("${page_size}")
	protected int pageSize;

	@Autowired
	protected FilingFilesDAO filingFilesDAO;

	@Autowired
	private FilingStatusDAO filingStatusDAO;

	@Autowired
	private ActivityTrackerDAO activityTrackerDAO;

	@Autowired
	private EmailService emailService;

	@Value("${tds.from-email}")
	private String mailsentfrom;

	@Autowired
	private TemplateEngine templateEngine;

	@Autowired
	private OnboardingClient onBoardingClient;

	@Autowired
	private NrTransactionsMetaDAO nrTransactionsMetaDAO;

	@Autowired
	private Form16DetailsDAO form16DetailsDAO;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private ResourceLoader resourceLoader;

	/**
	 * This method brings data from Deductor master by id and set to bean
	 * 
	 * @param assessmentYear
	 * @param noOfChallans
	 * @param tenantId
	 * @param tanNumber
	 * @param formType
	 * @param quarter
	 * @param deductorPan
	 * @return
	 */
	public FilingBatchHeaderBean generateBatchHeader(int assessmentYear, int noOfChallans, String tenantId,
			String tanNumber, String formType, String quarter, String deductorPan,
			FilingBatchHeaderBean filingBatchHeader) {
		logger.info("Filing: generate batch header for TAN: {}, year: {}, quarter: {}", tanNumber, assessmentYear,
				quarter);
		try {
			MultiTenantContext.setTenantId(tenantId);
			ResponseEntity<ApiStatus<DeductorMasterDTO>> data = onBoardingClient.getDeductorByPan(tenantId,
					deductorPan);
			DeductorMasterDTO deductorData = data.getBody().getData();
			Set<TanAddressDTO> addressTan = deductorData.getTanList();

			ResponseEntity<ApiStatus<List<FilingStateCode>>> stateResponse = mastersClient.getAllStateCode(tenantId);
			List<FilingStateCode> states = stateResponse.getBody().getData();
			Map<String, String> stateMap = new TreeMap<>();
			for (FilingStateCode filingStateCode : states) {
				stateMap.put(filingStateCode.getStateName().toUpperCase(), filingStateCode.getStateCode());
			}

			Map<String, TanAddressDTO> deductorTanMap = new TreeMap<>();
			for (TanAddressDTO tanDTO : addressTan) {
				deductorTanMap.put(tanDTO.getTan(), tanDTO);
			}

			FilingStatus previousQuarterFilingStatus = getPreviousQuarterFilingStatus(assessmentYear, quarter);

			// 1 Line Number - Running Sequence Number for each line in the file
			filingBatchHeader.setLineNo("2");
			// setting manually in bean
			filingBatchHeader.setRecordType("BH");
			// 3 Batch Number
			filingBatchHeader.setBatchNo("1");
			// 4 Count of Challan/transfer voucher Records - Count of total number of
			// challans/transfer vouchers contained within the batch. Must be equal to the
			// total number of 'Challans' included in this batch.
			filingBatchHeader.setChallanCount(StringUtils.EMPTY + noOfChallans);
			// 5
			filingBatchHeader.setFormNo(formType);
			// 6
			filingBatchHeader.setTransactionType(StringUtils.EMPTY);
			// 7
			filingBatchHeader.setBatchUpdationIndicator(StringUtils.EMPTY);
			// 8
			filingBatchHeader.setOriginalRrrNo(StringUtils.EMPTY);
			// 9 Token no. of previous regular statement (Form no. 27Q) - If value present
			// in field no. 52 is "Y", mandatory to mention 15 digit Token number of
			// immediate previous regular statement for Form 27Q, else no value to be
			// provided.
			if (previousQuarterFilingStatus != null
					&& StringUtils.isNotBlank(previousQuarterFilingStatus.getPnrOrTokenNumber())) {
				filingBatchHeader.setPreviousRrrNo(previousQuarterFilingStatus.getPnrOrTokenNumber());
			} else {
				filingBatchHeader.setPreviousRrrNo(StringUtils.EMPTY);
			}
			// 10
			filingBatchHeader.setRrrNo(StringUtils.EMPTY);
			// 11
			filingBatchHeader.setRrrDate(StringUtils.EMPTY);
			// 12
			filingBatchHeader.setLastTanOfDeductor(StringUtils.EMPTY);
			// 13 TAN of Deductor - Specifies the 10 Character TAN of the deductor. Should
			// be all CAPITALS.
			filingBatchHeader.setTanOfDeductor(tanNumber);
			// 14

			// 15 PAN of Deductor / Employer - Mandatory to mention the PAN of the Deductor.
			// If deductor is not required to have a PAN mention PANNOTREQD
			filingBatchHeader.setPanOfDeductor(deductorData.getPan());
			// 16 Assessment Yr
			filingBatchHeader.setAssessmentYr(Integer.toString(assessmentYear) + ((assessmentYear + 1) % 1000));
			// 17 Financial Yr
			Integer financialYr = Integer.valueOf(assessmentYear - 1);
			filingBatchHeader.setFinancialYr(Integer.toString(financialYr) + ((financialYr + 1) % 1000));
			// 18 Period
			filingBatchHeader.setPeriod(quarter);
			// 19 Name of Deductor
			filingBatchHeader.setEmployerName(deductorData.getDeductorName());
			// 20 Deductor / Branch/ Division
			TanAddressDTO tanAddressDTO = deductorTanMap.get(tanNumber);
			if (tanAddressDTO == null || StringUtils.isBlank(tanAddressDTO.getTownCityDistrict())) {
				filingBatchHeader.setEmployerBranchDiv("NA");
			} else {
				filingBatchHeader.setEmployerBranchDiv(tanAddressDTO.getTownCityDistrict());
			}
			// 21 Deductor's Address1
			if (tanAddressDTO == null || StringUtils.isBlank(tanAddressDTO.getFlatDoorBlockNo())) {
				filingBatchHeader.setEmployerAddr1(StringUtils.EMPTY);
			} else {
				filingBatchHeader.setEmployerAddr1(tanAddressDTO.getFlatDoorBlockNo());
			}
			// 22 Deductor's Address2
			if (tanAddressDTO == null || StringUtils.isBlank(tanAddressDTO.getNameBuildingVillage())) {
				filingBatchHeader.setEmployerAddr2(StringUtils.EMPTY);
			} else {
				filingBatchHeader.setEmployerAddr2(tanAddressDTO.getNameBuildingVillage());
			}
			// 23 Deductor's Address3
			if (tanAddressDTO == null || StringUtils.isBlank(tanAddressDTO.getRoadStreetPostoffice())) {
				filingBatchHeader.setEmployerAddr3(StringUtils.EMPTY);
			} else {
				filingBatchHeader.setEmployerAddr3(tanAddressDTO.getRoadStreetPostoffice());
			}
			// 24 Deductor's Address4
			if (tanAddressDTO == null || StringUtils.isBlank(tanAddressDTO.getAreaLocality())) {
				filingBatchHeader.setEmployerAddr4(StringUtils.EMPTY);
			} else {
				filingBatchHeader.setEmployerAddr4(tanAddressDTO.getAreaLocality());
			}
			// 25 Deductor's Address5
			if (tanAddressDTO == null || StringUtils.isBlank(tanAddressDTO.getTownCityDistrict())) {
				filingBatchHeader.setEmployerAddr5(StringUtils.EMPTY);
			} else {
				filingBatchHeader.setEmployerAddr5(tanAddressDTO.getTownCityDistrict());
			}
			// 26 Deductor's Address - State
			if (tanAddressDTO == null) {
				filingBatchHeader.setEmployerState(StringUtils.EMPTY);
			} else {
				filingBatchHeader.setEmployerState(getNullSafeStateCode(tanAddressDTO.getStateName(), stateMap));
			}
			// 27 Deductor's Address - Pincode
			if (tanAddressDTO == null) {
				filingBatchHeader.setEmployerPin(StringUtils.EMPTY);
			} else {
				filingBatchHeader.setEmployerPin(tanAddressDTO.getPinCode());
			}
			// 28 Deductor's Email ID
			filingBatchHeader.setEmployerEmail(deductorData.getEmail());
			// 29 Deductor 's STD code
			filingBatchHeader.setEmployerStd(StringUtils.EMPTY);
			// 30 Deductor 's Tel-Phone No
			filingBatchHeader.setEmployerPhone(StringUtils.EMPTY);
			// 31 Change of Address of Deductor / since last Return
			filingBatchHeader.setEmployerAddrChange("N");
			// 32 Deductor Type
			// TODO fix the following
			logger.info("Deductor type name {}", deductorData.getDeductorTypeName());
			logger.info("Deductor : {}", deductorData);
			filingBatchHeader.setDeductorType("K");
			// 33 Name of Person responsible for Deduction
			if (tanAddressDTO != null && tanAddressDTO.getPersonResponsibleDetails() != null
					&& StringUtils.isNotBlank(tanAddressDTO.getPersonResponsibleDetails().getName())) {
				filingBatchHeader
						.setNameofPersonResponsilbleForSal(tanAddressDTO.getPersonResponsibleDetails().getName());
			} else {
				filingBatchHeader.setNameofPersonResponsilbleForSal(StringUtils.EMPTY);
			}
			// 34 Designation of the Person responsible for Deduction
			if (tanAddressDTO != null && tanAddressDTO.getPersonResponsibleDetails() != null
					&& StringUtils.isNotBlank(tanAddressDTO.getPersonResponsibleDetails().getDesignation())) {
				filingBatchHeader.setDesignationofPersonResponsilbleForSal(
						tanAddressDTO.getPersonResponsibleDetails().getDesignation());
			} else {
				filingBatchHeader.setDesignationofPersonResponsilbleForSal(StringUtils.EMPTY);
			}
			// 35 Responsible Person's Address1
			if (tanAddressDTO == null) {
				filingBatchHeader.setPersonResponsilbleAddr1(StringUtils.EMPTY);
			} else {
				filingBatchHeader.setPersonResponsilbleAddr1(tanAddressDTO.getFlatDoorBlockNo());
			}
			// 36 Responsible Person's Address2
			if (tanAddressDTO == null) {
				filingBatchHeader.setPersonResponsilbleAddr2(StringUtils.EMPTY);
			} else {
				filingBatchHeader.setPersonResponsilbleAddr2(tanAddressDTO.getNameBuildingVillage());
			}
			// 37 Responsible Person's Address3
			if (tanAddressDTO == null) {
				filingBatchHeader.setPersonResponsilbleAddr3(StringUtils.EMPTY);
			} else {
				filingBatchHeader.setPersonResponsilbleAddr3(tanAddressDTO.getRoadStreetPostoffice());
			}
			// 38 Responsible Person's Address4
			if (tanAddressDTO == null) {
				filingBatchHeader.setPersonResponsilbleAddr4(StringUtils.EMPTY);
			} else {
				filingBatchHeader.setPersonResponsilbleAddr4(tanAddressDTO.getAreaLocality());
			}
			// 39 Responsible Person's Address5
			if (tanAddressDTO == null) {
				filingBatchHeader.setPersonResponsilbleAddr5(StringUtils.EMPTY);
			} else {
				filingBatchHeader.setPersonResponsilbleAddr5(tanAddressDTO.getTownCityDistrict());
			}
			// 40 Responsible Person's State
			if (tanAddressDTO == null) {
				filingBatchHeader.setPersonResponsilbleState(StringUtils.EMPTY);
			} else {
				filingBatchHeader
						.setPersonResponsilbleState(getNullSafeStateCode(tanAddressDTO.getStateName(), stateMap));
			}
			// 41 Responsible Person's PIN
			if (tanAddressDTO == null) {
				filingBatchHeader.setPersonResponsilblePin(StringUtils.EMPTY);
			} else {
				filingBatchHeader.setPersonResponsilblePin(tanAddressDTO.getPinCode());
			}
			// 42 Responsible Person's Email ID -1
			filingBatchHeader.setPersonResponsilbleEmailId1(deductorData.getEmail());
			// 43 Mobile number
			filingBatchHeader.setMobileNumber(deductorData.getPhoneNumber());

			// 44 Responsible Person's STD CODE
			filingBatchHeader.setPersonResponsilbleSTDCode(StringUtils.EMPTY);
			// 45 Responsible Person's Tel-Phone No:
			filingBatchHeader.setPersonResponsilbleTelePhone(StringUtils.EMPTY);

			// 46 Change of Address of Responsible person since last Return
			filingBatchHeader.setPersonResponsilbleAddrChange("N");

			// 47 Batch Total of - Total of Deposit Amount as per Challan -- Done at the end

			// 48 Unmatched challan count
			filingBatchHeader.setUnMatchedChalanCnt(StringUtils.EMPTY);
			// 49 Count of Salary Details Records (Not applicable)
			filingBatchHeader.setCountOfSalaryDetailRec(StringUtils.EMPTY);
			// 50 Batch Total of - Gross Total Income as per Salary Detail (Not applicable)
			filingBatchHeader.setGrossTotalIncomeSd(StringUtils.EMPTY);
			// 51 AO Approval
			filingBatchHeader.setApprovalTaken("N");
			// 52 Whether regular statement for Form 27Q filed for earlier period
			if (previousQuarterFilingStatus != null
					&& StringUtils.isNotBlank(previousQuarterFilingStatus.getPnrOrTokenNumber())) {
				filingBatchHeader.setApprovalNo("Y");
			} else {
				filingBatchHeader.setApprovalNo("N");
			}

			// 53 Last Deductor Type
			filingBatchHeader.setLastDeductorType(StringUtils.EMPTY);

			if (filingBatchHeader.getDeductorType().equals("S") || filingBatchHeader.getDeductorType().equals("E")
					|| filingBatchHeader.getDeductorType().equals("H")
					|| filingBatchHeader.getDeductorType().equals("N")) {
				for (TanAddressDTO customTanDTO : addressTan) {
					// 54 State Name
					filingBatchHeader.setStateName(getNullSafeStateCode(customTanDTO.getStateName(), stateMap));
				}
			}
			// 55 PAO Code
			filingBatchHeader.setPaoCode(StringUtils.EMPTY);
			// 56 DDO Code
			filingBatchHeader.setDdoCode(StringUtils.EMPTY);
			// 57 Ministry Name
			filingBatchHeader.setMinistryName(StringUtils.EMPTY);
			// 58 Ministry Name Other
			filingBatchHeader.setMinistryNameOther(StringUtils.EMPTY);
			// 59 PAN of Responsible Person
			// TODO FIX with new field
			if (tanAddressDTO != null && tanAddressDTO.getPersonResponsibleDetails() != null
					&& StringUtils.isNotBlank(tanAddressDTO.getPersonResponsibleDetails().getPan())) {
				filingBatchHeader.setpANOfResponsiblePerson(tanAddressDTO.getPersonResponsibleDetails().getPan());
			} else {
				filingBatchHeader.setpANOfResponsiblePerson(StringUtils.EMPTY);
			}
			// 60 PAO Registration No
			filingBatchHeader.setPaoRegistrationNo(StringUtils.EMPTY);
			// 61 DDO Registration No
			filingBatchHeader.setDdoRegistrationNo(StringUtils.EMPTY);
			// 62 Employer / Deductor's STD code (Alternate)
			filingBatchHeader.setEmployerSTDAlt(StringUtils.EMPTY);
			// 63 Employer / Deductor 's Tel-Phone No. (Alternate)
			filingBatchHeader.setEmployerPhoneAlt(StringUtils.EMPTY);
			// 64 Employer / Deductor Email ID (Alternate)
			filingBatchHeader.setEmployerEmailAlt(StringUtils.EMPTY);
			// 65 Responsible Person's STD Code (Alternate)
			filingBatchHeader.setPersonResponsilbleSTDCodeAlt(StringUtils.EMPTY);
			// 66 Responsible Person's Tel-Phone No. (Alternate)
			filingBatchHeader.setPersonResponsilbleTelePhoneAlt(StringUtils.EMPTY);
			// 67 Responsible Person's Email ID (Alternate)
			filingBatchHeader.setPersonResponsilbleEmailIdAlt(deductorData.getEmail());
			// 68 Account Office Identification Number (AIN) of PAO/ TO/ CDDO
			// 69 Goods and Service Tax Number (GSTN)
			filingBatchHeader.setgSTN(StringUtils.EMPTY);
			// 70 Record Hash (Not applicable)
			filingBatchHeader.setaIN(StringUtils.EMPTY);

		} catch (Exception e) {
			logger.error(StringUtils.EMPTY, e);
		}
		logger.info("Filing: generate batch header for TAN: {}, year: {}, quarter: {} done.", tanNumber, assessmentYear,
				quarter);
		return filingBatchHeader;

	}

	private FilingStatus getPreviousQuarterFilingStatus(int assessmentYear, String quarter) {
		int year = assessmentYear;
		if (quarter.equals("Q1")) {
			year = assessmentYear - 1;
		}
		FilingStatus previousQuarterFilingStatus = null;
		try {
			List<FilingStatus> previousQuarterList = filingStatusDAO.findByYearAndQuarterAndId(year, quarter, 1);
			if (!previousQuarterList.isEmpty()) {
				previousQuarterFilingStatus = previousQuarterList.get(0);
			}
		} catch (CustomException e) {
			logger.warn("No filing status exists for Quarter : {} - {}", assessmentYear, quarter);
		}

		return previousQuarterFilingStatus;
	}

	/**
	 * This method brings data from tds-challan module based on quarter and set to
	 * respective bean
	 *
	 * @param assessmentYear
	 * @param quarter
	 * @param tenantId
	 * @param grossTdsTotalAsPerChallan
	 * @return
	 */
	public List<FilingChallanDetailBean> generateChallanDetail(int assessmentYear, String quarter, String tenantId,
			String tanNumber, FilingBatchHeaderBean filingBatchHeader, boolean isForNonResident) {
		MultiTenantContext.setTenantId(tenantId);
		BigDecimal grossTdsTotalAsPerChallan = BigDecimal.ZERO;
		logger.info("Filing: Challan detail generation for TAN: {}, year: {}, quarter: {}", tanNumber, assessmentYear,
				quarter);
		ResponseEntity<ApiStatus<List<FilingSectionCode>>> sectionCodeResponse = mastersClient
				.getAllSectionCode(tenantId);
		List<FilingSectionCode> sectionCodes = sectionCodeResponse.getBody().getData();
		Map<String, String> sectionCodeMap = new TreeMap<>();
		for (FilingSectionCode filingSectionCode : sectionCodes) {
			sectionCodeMap.put(filingSectionCode.getSectionName(), filingSectionCode.getSectionCode());
		}

		List<FilingChallanDetailBean> filingChallanDetails = new ArrayList<>();
		int lineNo = 3;

		List<Receipt> receipts = challansClient.getRecieptByTanYearAndQuarter(tanNumber,
				CommonUtil.QUARTER_MONTHS_MAP.get(quarter), assessmentYear, tenantId, isForNonResident).getBody()
				.getData();
		if (receipts.isEmpty()) {
			throw new CustomException(String.format("There are no receipts exists for Quarter %s", quarter));
		}
		// TODO: Remove getting the Challans Altigether and get only the Receits and
		// Process them. We do not need Challans and then receipts.
		for (Receipt receiptData : receipts) {
			String challanReceiptAmount = String.valueOf(receiptData.getChallanAmountTotal());
			FilingChallanDetailBean filingChallanDetailBean = new FilingChallanDetailBean(receiptData.getChallanMonth(),
					"");
			grossTdsTotalAsPerChallan = grossTdsTotalAsPerChallan.add(receiptData.getChallanAmountTotal());
			// 1 Line Number
			filingChallanDetailBean.setLineNo(Long.toString(lineNo++));
			// 2 Record Type
			filingChallanDetailBean.setRecType("CD");
			// 3 Batch Number
			filingChallanDetailBean.setChBatchNo("1");
			// 4 Challan-Detail Record Number
			filingChallanDetailBean.setChallanDetailRecordNo(StringUtils.EMPTY);
			// 5 Count of Deductee / Party Records Set this after getting deductee list

			// 6 NIL Challan Indicator
			filingChallanDetailBean.setNillChallanIndicator("N");
			// 7 Challan Updation Indicator
			filingChallanDetailBean.setChallanUpdationIndicator(StringUtils.EMPTY);
			// 8 Filler 3
			filingChallanDetailBean.setFiller3(StringUtils.EMPTY);
			// 9 Filler 4
			filingChallanDetailBean.setFiller4(StringUtils.EMPTY);
			// 10 Filler 5
			filingChallanDetailBean.setFiller5(StringUtils.EMPTY);

			filingChallanDetailBean.setDdoSerialNumberForm24G(StringUtils.EMPTY);
			filingChallanDetailBean.setLastBankBranchCode(StringUtils.EMPTY);

			if (receiptData != null && receiptData.getId() != null) {
				filingChallanDetailBean.setDateOfBankChallanNo(receiptData.getDate().replace("/", StringUtils.EMPTY));
				if (filingChallanDetailBean.getNillChallanIndicator().equals("Y")) {
					// 11 Last Bank Challan No ( Used for Verification) (Not applicable)
					filingChallanDetailBean.setLastBankChallanNo(StringUtils.EMPTY);
					// 12 Bank Challan No
					filingChallanDetailBean.setBankChallanNo(StringUtils.EMPTY);
					// 13 Last Transfer Voucher No ( Used for Verification) (Not applicable)
					filingChallanDetailBean.setLastTransferVoucherNo(StringUtils.EMPTY);
					// 14 DDO serial number of Form No. 24G
					if (receiptData != null) {
						// 15 Last Bank-Branch Code/ Form 24G Receipt Number ( Used for Verification)
						filingChallanDetailBean.setBankBranchCode(StringUtils.EMPTY);
					}
				} else {
					// 11 Last Bank Challan No ( Used for Verification) (Not applicable)
					filingChallanDetailBean.setLastBankChallanNo(StringUtils.EMPTY);
					// 12 Bank Challan No
					filingChallanDetailBean.setBankChallanNo(receiptData.getChallanSerialNumber());
					// 13 Last Transfer Voucher No ( Used for Verification) (Not applicable)
					filingChallanDetailBean.setLastTransferVoucherNo(StringUtils.EMPTY);
					// 14 DDO serial number of Form No. 24G
					if (receiptData != null) {
						// 15 Last Bank-Branch Code/ Form 24G Receipt Number ( Used for Verification)
						filingChallanDetailBean.setBankBranchCode(receiptData.getBsrCode());
					}
				}
			} else {
				throw new CustomException("No Receipt data exists to generate file for quarter : " + quarter);
			}

			filingChallanDetailBean.setLastDateOfBankChallanNo(StringUtils.EMPTY);
			String receiptDate = receiptData.getDate();
			if (StringUtils.isNotBlank(receiptDate)) {
				receiptDate = receiptDate.replace("/", StringUtils.EMPTY).replace("-", StringUtils.EMPTY);
				logger.info("Receipt date :" + receiptDate);
				if (receiptDate.length() == 6) {
					receiptDate = receiptDate.substring(0, 4)
							+ (StringUtils.EMPTY + Calendar.getInstance().get(Calendar.YEAR)).substring(0, 2)
							+ receiptDate.substring(4, 6);
				}
				logger.info("Receipt date :" + receiptDate);
				filingChallanDetailBean.setDateOfBankChallanNo(receiptDate);
			}

			// (Not applicable)
			// 16 Bank-Branch Code/ Form 24G Receipt Number
			// 17 Last Date of 'Bank Challan No / Transfer Voucher No' ( Used for
			// Verification) (Not applicable)
			// 18 Date of 'Bank Challan No / Transfer Voucher No'
			// 19 Filler 6
			// 20 Filler 7
			// 21 Section

			// Always empty string
			// String sectionCode = sectionCodeMap.get(challan.getSection().toUpperCase());
			filingChallanDetailBean.setSection(StringUtils.EMPTY);

			// 22 'Oltas TDS / TCS -Income Tax '
			if (StringUtils.isNotBlank(challanReceiptAmount)) {
				logger.info(challanReceiptAmount);
				filingChallanDetailBean.setOltasIncomeTax(String.format("%.2f", new BigDecimal(challanReceiptAmount)));
			} else {
				filingChallanDetailBean.setOltasIncomeTax("0.00");
			}
			logger.info("Oltas tax :" + filingChallanDetailBean.getOltasIncomeTax());
			// 23 'Oltas TDS / TCS -Surcharge '
			filingChallanDetailBean.setOltasSurcharge("0.00");
			// 24 'Oltas TDS / TCS - Cess'
			filingChallanDetailBean.setOltasCess("0.00");
			// 25 Oltas TDS / TCS - Interest Amount
			filingChallanDetailBean.setOltasInterest("0.00");
			// 26 Oltas TDS / TCS - Others (amount)
			filingChallanDetailBean.setOltasOthers("0.00");
			// 27 Total of Deposit Amount as per Challan/Transfer Voucher Number ( 'Oltas
			// TDS/ TCS -Income Tax ' + 'Oltas TDS/ TCS -Surcharge ' + 'Oltas TDS/ TCS -
			// Cess' + Oltas TDS/ TCS - Interest Amount + Fee + Oltas TDS/ TCS - Others
			// (amount) )
			if (StringUtils.isNotBlank(challanReceiptAmount)) {
				logger.info(challanReceiptAmount);
				filingChallanDetailBean.setTotalOfDepositAmountAsPerChallan(
						String.format("%.2f", new BigDecimal(challanReceiptAmount)));
			} else {
				filingChallanDetailBean.setTotalOfDepositAmountAsPerChallan("0.00");
			}
			logger.info("Deposit Amount per challan :" + filingChallanDetailBean.getTotalOfDepositAmountAsPerChallan());
			// 28 Last Total of Deposit Amount as per Challan ( Used for Verification) (Not
			// applicable)
			filingChallanDetailBean.setLastTotalOfDepositAmountAsPerChallan(StringUtils.EMPTY);
			// 29 Total Tax Deposit Amount as per deductee annexure (Total Sum of 726)
			if (StringUtils.isNotBlank(challanReceiptAmount)) {
				logger.info(challanReceiptAmount);
				filingChallanDetailBean.setTotalTaxDepositedAsPerDeducteeAnex(
						String.format("%.2f", new BigDecimal(challanReceiptAmount)));
			} else {
				filingChallanDetailBean.setTotalTaxDepositedAsPerDeducteeAnex("0.00");
			}
			logger.info("Total tax deposited Per Deductee Annex :"
					+ filingChallanDetailBean.getTotalTaxDepositedAsPerDeducteeAnex());

			// 29 - 33 are set at end of deductee details processing

			// TODO fix the following value
			// 34 TDS / TCS - Interest Amount
			filingChallanDetailBean.setTdsInterest("0.00");
			// 35 TDS / TCS - Others (amount)
			filingChallanDetailBean.setTdsOthers("0.00");
			// 36 Cheque / DD No. (if any)
			filingChallanDetailBean.setChequeDDNo(StringUtils.EMPTY);
			if (filingChallanDetailBean.getNillChallanIndicator().equals("Y")) {
				// 37 By Book entry / Cash
				filingChallanDetailBean.setBookCash(StringUtils.EMPTY);
			} else {
				// 37 By Book entry / Cash
				filingChallanDetailBean.setBookCash("N");
			}
			// 38 Remarks
			filingChallanDetailBean.setRemark(StringUtils.EMPTY);
			// 39 Fee
			filingChallanDetailBean.setLateFee("0.00");

			if (filingChallanDetailBean.getNillChallanIndicator().equals("Y")) {
				// 40 Minor Head of Challan
				filingChallanDetailBean.setMinorHeadCodeChallan(StringUtils.EMPTY);
			} else {
				// 40 Minor Head of Challan
				filingChallanDetailBean.setMinorHeadCodeChallan("200");
			}

			// 41 Record Hash
			filingChallanDetailBean.setChallanHash(StringUtils.EMPTY);
			filingChallanDetails.add(filingChallanDetailBean);
		}
		// }

		filingBatchHeader.setGrossTdsTotalAsPerChallan(String.format("%.2f", grossTdsTotalAsPerChallan));

		// Sort the challans here
		Collections.sort(filingChallanDetails, new FilingChallanDetailComparator());
		for (FilingChallanDetailBean filingChallanDetail : filingChallanDetails) {
			// 4 Challan-Detail Record Number
			filingChallanDetail.setChallanDetailRecordNo(
					StringUtils.EMPTY + (filingChallanDetails.indexOf(filingChallanDetail) + 1));
		}
		logger.info("Filing: Challan detail generation for TAN: {}, year: {}, quarter: {} done", tanNumber,
				assessmentYear, quarter);
		return filingChallanDetails;
	}

	/**
	 * This method brings deductee data from tds-onboarding based on tan-number
	 * 
	 * @param tanNumber
	 * @param tenantId
	 * @param assessmentYear
	 * @param quarter
	 * @param filingChallanDetails
	 * @param isForNonResidents
	 * @param isAggriated
	 * @param deductorPan
	 */
	public void generateDeducteeDetail(String tanNumber, String tenantId, int assessmentYear, String quarter,
			List<FilingChallanDetailBean> filingChallanDetails, boolean isForNonResidents, Boolean isAggriated,
			String deductorPan) {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Filing: Deductee detail generation for TAN: {}, year: {}, quarter: {}", tanNumber, assessmentYear,
				quarter);
		final AtomicInteger rowIndexHolder = new AtomicInteger(2 + filingChallanDetails.size() + 1);
		ResponseEntity<ApiStatus<List<FilingSectionCode>>> sectionCodeResponse = mastersClient
				.getAllSectionCode(tenantId);
		List<FilingSectionCode> sectionCodes = sectionCodeResponse.getBody().getData();
		Map<String, String> sectionCodeMap = new TreeMap<>();
		for (FilingSectionCode filingSectionCode : sectionCodes) {
			sectionCodeMap.put(filingSectionCode.getSectionName(), filingSectionCode.getSectionCode());
		}
		boolean isDividendEnabled = false;
		List<DeductorOnboardingInformationDTO> deductorOnboardingList = onboardingClient
				.getDeductorOnboardingInfo(tenantId, deductorPan).getBody().getData();
		if (!deductorOnboardingList.isEmpty()) {
			DeductorOnboardingInformationDTO deductorInfo = deductorOnboardingList.get(0);
			isDividendEnabled = deductorInfo.getDvndEnabled() != null ? deductorInfo.getDvndEnabled() : false;
		}
		for (FilingChallanDetailBean filingChallanDetailBean : filingChallanDetails) {

			List<DeducteeDetailDTO> lineItemsByReceipt = getLineItemsByReceipt(tanNumber, tenantId, assessmentYear,
					isForNonResidents, filingChallanDetailBean.getBankBranchCode(),
					filingChallanDetailBean.getBankChallanNo(), filingChallanDetailBean.getDateOfBankChallanNo(),
					isDividendEnabled);

			// grouping based on name, posting date, deductee pan,section
			if (lineItemsByReceipt != null && isAggriated != null && isAggriated == true) {
				lineItemsByReceipt = getAggrigatedList(lineItemsByReceipt);
			}

			for (DeducteeDetailDTO deducteeDetailDTO : lineItemsByReceipt) { // name, posting date, deductee pan,section
				FilingDeducteeDetailBean deducteeDetail = new FilingDeducteeDetailBean(
						isForNonResidents ? "27Q" : "26Q");

				String challanDetailRecordNo = StringUtils.EMPTY;
				filingChallanDetailBean.getDeducteeDetailBeanList().add(deducteeDetail);
				challanDetailRecordNo = filingChallanDetailBean.getChallanDetailRecordNo();
				String ldcCertificateNo = StringUtils.isNotBlank(deducteeDetailDTO.getLdcCertificateNo())
						? deducteeDetailDTO.getLdcCertificateNo()
						: StringUtils.EMPTY;
				// 1 Line Number
				deducteeDetail.setLineNo(StringUtils.EMPTY + rowIndexHolder.getAndIncrement());
				// 2 Record Type (Hardcoded in bean)
				deducteeDetail.setRecType("DD");
				// 3 Batch Number
				deducteeDetail.setDdBatchNo("1");
				// 4 Challan-Detail Record Number
				deducteeDetail.setChallanRecordNo(challanDetailRecordNo);
				// 5 Deductee / Party Detail Record No
				// deducteeDetail.setDeducteeDetailRecNo("1");
				// 6 Mode (Hardcoded in bean)
				deducteeDetail.setMode("O");
				// 7 Deductee Serial No (Not applicable)
				deducteeDetail.setDeducteeSerialNo(StringUtils.EMPTY);
				// 8 Deductee code
				if (StringUtils.isNotBlank(deducteeDetailDTO.getPan())
						&& "C".equalsIgnoreCase(String.valueOf(deducteeDetailDTO.getPan().charAt(3)))) {
					deducteeDetail.setDeducteeCode("1");
				} else {
					deducteeDetail.setDeducteeCode("2");
				}
				// 9 Last Deductee PAN ( Used for Verification) (Not applicable)
				deducteeDetail.setLastDeducteePan(StringUtils.EMPTY);
				// 10 PAN of the deductee
				if (StringUtils.isBlank(deducteeDetailDTO.getPan())) {
					deducteeDetail.setDeducteePan("PANNOTAVBL");
				} else {
					deducteeDetail.setDeducteePan(deducteeDetailDTO.getPan());
				}
				// 11 Last Deductee PAN Ref. No. (Not applicable)
				deducteeDetail.setLastDeducteeRefNo(StringUtils.EMPTY);
				// 12 Deductee Ref. No.

				if ("PANNOTAVBL".equals(deducteeDetail.getDeducteePan())) {
					long number = (long) Math.floor(Math.random() * 9000000000L) + 1000000000L;
					deducteeDetail.setDeducteeRefNo(Long.valueOf(number).toString());
				} else {
					deducteeDetail.setDeducteeRefNo(StringUtils.EMPTY);
				}

				// 13 Name of the Deductee
				deducteeDetail.setDeducteeName(deducteeDetailDTO.getDeducteeName());

				BigDecimal finalTDSAmount = deducteeDetailDTO.getFinalTDSAmount();
				finalTDSAmount = finalTDSAmount.setScale(2, RoundingMode.UP);

				// 14 TDS -Income Tax for the period
				deducteeDetail.setTdsIncomeTaxDD(String.format("%.2f", finalTDSAmount));

				// 15 TDS -Surcharge for the period
				deducteeDetail.setTdsSurchargeDD("0.00");

				// 16 TDS-Cess
				deducteeDetail.setTdsCessDD("0.00");

				// 17 Total Income Tax Deducted at Source (TDS / TCS Income Tax+ TDS / TCS
				// Surcharge + TDS/TCS Cess)
				deducteeDetail.setTotalIncomeTaxDeductedAtSource(String.format("%.2f", finalTDSAmount));

				// 18 Last Total Income Tax Deducted at Source (Income Tax+Surcharge+Cess) (Used
				// for Verification) (Not applicable)
				deducteeDetail.setLastTotalIncomeTaxDeductedAtSource(StringUtils.EMPTY);

				// 19 Total Tax Deposited
				deducteeDetail.setTotalTaxDeposited(String.format("%.2f", finalTDSAmount));

				// 20 Last Total Tax Deposited ( Used for Verification) (Not applicable)
				deducteeDetail.setLastTotalTaxDeposited(StringUtils.EMPTY);
				// 21 Total Value of Purchase (Not applicable)
				deducteeDetail.setTotalValueofPurchase(StringUtils.EMPTY);
				// 22 Amount of Payment / Credit ( Rs.)
				deducteeDetail.setAmountOfPayment(String.format("%.2f", deducteeDetailDTO.getAmount()));
				// 23 Date on which Amount paid / Credited / Debited
				SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
				formatter.setTimeZone(TimeZone.getTimeZone("IST"));
				String documentPostingDate = formatter.format(deducteeDetailDTO.getDocumentPostingDate());
				deducteeDetail.setDateOnWhichAmountPaid(documentPostingDate);
				// 24 Date on which tax Deducted
				deducteeDetail.setDateOnWhichTaxDeducted(documentPostingDate);
				// 25 Date of Deposit
				deducteeDetail.setDateOfDeposit(StringUtils.EMPTY);
				// 26 Rate at which Tax Deducted
				deducteeDetail.setRateAtWhichTaxDeducted(String.format("%.4f", deducteeDetailDTO.getFinalTDSRate()));
				// 27 Grossing up Indicator
				deducteeDetail.setGrossingUpIndicator(StringUtils.EMPTY);
				// 28 Book Entry / Cash Indicator
				deducteeDetail.setBookCashEntry(StringUtils.EMPTY);
				// 29 Date of furnishing Tax Deduction Certificate (Not applicable)
				deducteeDetail.setDateOfFurnishingTaxDeductionCertificate(StringUtils.EMPTY);
				// 30 Remarks 1 (Reason for non-deduction / lower deduction/ grossing up/ higher
				// deduction)
				if ("PANNOTAVBL".equals(deducteeDetail.getDeducteePan())) {
					deducteeDetail.setRemark1("C");
				} else if (StringUtils.isNotBlank(ldcCertificateNo)) {
					deducteeDetail.setRemark1("A");
				} else {
					deducteeDetail.setRemark1(StringUtils.EMPTY);
				}
				// 31 Remarks 2 (For future use)
				deducteeDetail.setRemark2(StringUtils.EMPTY);
				// 32 Remarks 3 (For future use)
				deducteeDetail.setRemark3(StringUtils.EMPTY);
				// 33 Section Code under which payment made
				String section = deducteeDetailDTO.getFinalTDSSection();
				if (assessmentYear <= 2013) {
					section = (("194I".equals(section) || "94I".equals(section)) ? "94I" : section);
				} else {
					section = (("194I".equals(section) || "94I".equals(section)) ? "194IB" : section);
				}
				if ("194J".equalsIgnoreCase(section)) {
					section = ((deducteeDetailDTO.getFinalTDSRate() == null
							|| (deducteeDetailDTO.getFinalTDSRate().compareTo(new BigDecimal(2)) <= 0)) ? "4JA"
									: "4JB");
				}
				if (sectionCodeMap.get(section) == null) {
					deducteeDetail.setSectionCode(section); // 21
				} else {
					deducteeDetail.setSectionCode(sectionCodeMap.get(section)); // 21
				}
				// 34 Certificate number issued by the Assessing Officer u/s 197 for
				// non-deduction/lower deduction.
				deducteeDetail.setCertNumAo(ldcCertificateNo);
				// Note from 35 to 42 no need fill for 26Q
				// 35 Whether TDS rate of TDS is IT act (a) and DTAA (b)
				if (isForNonResidents) {
					// 36 Nature of remittance
					String natureOfRemittance = StringUtils.EMPTY;
					if ("REMITTANCE IS IN THE NATURE OF DIVIDEND. HOWEVER NO RELIEF CLAIMED UNDER THE DTAA"
							.equalsIgnoreCase(deducteeDetailDTO.getNatureOfRemittance())
							|| "Remittance of dividends by FDI enterprises in India (other than branches) on equity and investment fund shares"
									.equalsIgnoreCase(deducteeDetailDTO.getNatureOfRemittance())
							|| "DIVIDEND".equalsIgnoreCase(deducteeDetailDTO.getNatureOfRemittance())) {
						natureOfRemittance = "DIVIDEND";
					} else if (StringUtils.isBlank(deducteeDetailDTO.getNatureOfRemittance())
							|| "OTHER INCOME / OTHER (NOT IN THE NATURE OF INCOME)"
									.equalsIgnoreCase(deducteeDetailDTO.getNatureOfRemittance())
							|| "OTHER BUSINESS SERVICES / NOT INCLUDED ELSEWHERE"
									.equalsIgnoreCase(deducteeDetailDTO.getNatureOfRemittance())) {
						natureOfRemittance = "OTHER INCOME / OTHER (NOT IN THE NATURE OF INCOME)";
					} else if (StringUtils.isNotBlank(deducteeDetailDTO.getNatureOfRemittance())) {
						natureOfRemittance = deducteeDetailDTO.getNatureOfRemittance();
					}
					deducteeDetail.setNatureOfRemittance(natureOfRemittance);
					// 37 Unique acknowledgement of the corrosponding form no 15CA (if available)
					// 38 Country of residence of the deductee
					deducteeDetail.setCountryOfDeductee(deducteeDetailDTO.getCountry());
					if (StringUtils.isBlank(deducteeDetailDTO.getPan())
							&& "C".equalsIgnoreCase(deducteeDetail.getRemark1())
							&& deducteeDetailDTO.getFinalTDSRate().compareTo(BigDecimal.valueOf(20)) < 0) {
						// FEES FOR TECHNICAL SERVICES/ FEES FOR INCLUDED SERVICES-21
						// INTEREST PAYMENT-27
						// LONG TERM CAPITAL GAINS-31
						// ROYALTY-49
						// SHORT TERM CAPITAL GAINS-52
						String natureOfRemittanceCode = FilingFileRemittance
								.getfilingFileRemittance(natureOfRemittance);
						if (StringUtils.isNotBlank(natureOfRemittanceCode)
								&& ("27".equalsIgnoreCase(natureOfRemittanceCode)
										|| "49".equalsIgnoreCase(natureOfRemittanceCode)
										|| "21".equalsIgnoreCase(natureOfRemittanceCode)
										|| "52".equalsIgnoreCase(natureOfRemittanceCode)
										|| "31".equalsIgnoreCase(natureOfRemittanceCode))) {
							// 39 Email ID of deductee
							deducteeDetail.setEmailOfDeductee(deducteeDetailDTO.getEmail());
							// 40 Contact number of deductee
							deducteeDetail.setContactNumberOfDeductee(deducteeDetailDTO.getContactNo());
							// 41 Address of deductee in country of residence
							deducteeDetail.setAddressOfDeducteeInCountry(deducteeDetailDTO.getAddress());
							// 42 Tax Identification Number /Unique identification number of deductee
							deducteeDetail.setTinOfDeductee(deducteeDetailDTO.getTin());
						}
					}
				}
				// 43 Record Hash (Not applicable)
				deducteeDetail.setFiller1(StringUtils.EMPTY);
				deducteeDetail.setFiller2(StringUtils.EMPTY);
				deducteeDetail.setFiller3(StringUtils.EMPTY);
				deducteeDetail.setFiller4(StringUtils.EMPTY);
				deducteeDetail.setFiller5(StringUtils.EMPTY);
				deducteeDetail.setFiller6(StringUtils.EMPTY);
				deducteeDetail.setFiller7(StringUtils.EMPTY);
				deducteeDetail.setFiller8(StringUtils.EMPTY);
				if ("94N".equalsIgnoreCase(deducteeDetail.getSectionCode())) {
					deducteeDetail.setExcess94NAmount(deducteeDetail.getTdsIncomeTaxDD());
				} else {
					deducteeDetail.setExcess94NAmount(StringUtils.EMPTY);
				}
				deducteeDetail.setFiller9(StringUtils.EMPTY);
				deducteeDetail.setFiller10(StringUtils.EMPTY);
				deducteeDetail.setFiller11(StringUtils.EMPTY);
				deducteeDetail.setFiller12(StringUtils.EMPTY);
				deducteeDetail.setDeducteeHash(StringUtils.EMPTY);

			}
			// }
		}
		// 29 - 33 field calculation done here for CD
		for (FilingChallanDetailBean filingChallanDetailBean : filingChallanDetails) {
			BigDecimal totalTaxDepositedAsPerDeducteeAnex = BigDecimal.ZERO;
			BigDecimal tdsIncomeTaxC = BigDecimal.ZERO;
			BigDecimal tdsSurchargeC = BigDecimal.ZERO;
			BigDecimal tdsCessC = BigDecimal.ZERO;
			BigDecimal sumTotalIncTaxDedAtSource = BigDecimal.ZERO;
			int deducteeDetailRecNo = 1;
			for (FilingDeducteeDetailBean filingDeducteeDetailBean : filingChallanDetailBean
					.getDeducteeDetailBeanList()) {
				// 14
				tdsIncomeTaxC = tdsIncomeTaxC.add(new BigDecimal(filingDeducteeDetailBean.getTdsIncomeTaxDD()));
				// 15
				tdsSurchargeC = tdsSurchargeC.add(new BigDecimal(filingDeducteeDetailBean.getTdsSurchargeDD()));
				// 16
				tdsCessC = tdsCessC.add(new BigDecimal(filingDeducteeDetailBean.getTdsCessDD()));
				// 17
				sumTotalIncTaxDedAtSource = sumTotalIncTaxDedAtSource
						.add(new BigDecimal(filingDeducteeDetailBean.getTdsIncomeTaxDD()));
				// 19
				totalTaxDepositedAsPerDeducteeAnex = totalTaxDepositedAsPerDeducteeAnex
						.add(new BigDecimal(filingDeducteeDetailBean.getTotalTaxDeposited()));
				filingDeducteeDetailBean.setDeducteeDetailRecNo(StringUtils.EMPTY + deducteeDetailRecNo++);
			}
			// 29
			filingChallanDetailBean
					.setTotalTaxDepositedAsPerDeducteeAnex(CommonUtil.df2.format(totalTaxDepositedAsPerDeducteeAnex));
			// 30
			filingChallanDetailBean.setTdsIncomeTaxC(CommonUtil.df2.format(tdsIncomeTaxC));
			// 31
			filingChallanDetailBean.setTdsSurchargeC(CommonUtil.df2.format(tdsSurchargeC));
			// 32
			filingChallanDetailBean.setTdsCessC(CommonUtil.df2.format(tdsCessC));
			// 33
			filingChallanDetailBean.setSumTotalIncTaxDedAtSource(CommonUtil.df2.format(sumTotalIncTaxDedAtSource));

			filingChallanDetailBean.setCountOfDeducteeDetail(
					StringUtils.EMPTY + filingChallanDetailBean.getDeducteeDetailBeanList().size());

		}
		logger.info("Filing: Deductee detail generation for TAN: {}, year: {}, quarter: {} done.", tanNumber,
				assessmentYear, quarter);
	}

	@SuppressWarnings("unused")
	private void splitInvoicesByAmounts(Map<String, List<InvoiceLineItem>> invoiceMap,
			Map<String, BigDecimal> invoiceFinalTdsAmountMap, Map<String, List<FilingChallanDetailBean>> challanMap,
			Map<String, List<InvoiceLineItem>> finalInvoiceMap) {
		for (String key : challanMap.keySet()) {
			logger.info("Processing challan for : {}", key);
			for (FilingChallanDetailBean filingChallanDetailBean : challanMap.get(key)) {
				String challanKey = filingChallanDetailBean.getChallanMonth() + "-"
						+ filingChallanDetailBean.getChallanSection();
				String key1 = filingChallanDetailBean.getChallanMonth() + "-"
						+ filingChallanDetailBean.getChallanSection() + "-" + "Company";
				long size = invoiceMap.get(key1).size();
				logger.info("Company : {} {}", size, invoiceFinalTdsAmountMap.get(key1));

				BigDecimal value1 = invoiceFinalTdsAmountMap.get(key1);

				String key2 = filingChallanDetailBean.getChallanMonth() + "-"
						+ filingChallanDetailBean.getChallanSection() + "-" + "NonCompany";
				size = invoiceMap.get(key2).size();
				logger.info("NonCompany : {} {}", size, invoiceFinalTdsAmountMap.get(key2));

				logger.info("{}", filingChallanDetailBean.getTotalTaxDepositedAsPerDeducteeAnex());
				BigDecimal value2 = invoiceFinalTdsAmountMap.get(key2);
				if (finalInvoiceMap.get("" + filingChallanDetailBean.hashCode()) == null) {
					finalInvoiceMap.put("" + filingChallanDetailBean.hashCode(), new ArrayList<InvoiceLineItem>());
				}
				if (challanMap.get(challanKey).size() == 1) {
					logger.info("Got single");
					finalInvoiceMap.get("" + filingChallanDetailBean.hashCode()).addAll(invoiceMap.get(key1));
					finalInvoiceMap.get("" + filingChallanDetailBean.hashCode()).addAll(invoiceMap.get(key2));
				} else {
					BigDecimal challanTax = new BigDecimal(
							filingChallanDetailBean.getTotalTaxDepositedAsPerDeducteeAnex());
					double distance1 = challanTax.doubleValue() - value1.doubleValue();
					double distance2 = challanTax.doubleValue() - value2.doubleValue();
					double minDistance = Double.max(0, Double.min(distance1, distance2));

					logger.info("{} {} {} {}", challanTax.doubleValue(), distance1, distance2, minDistance);
					if (distance1 >= 0 && challanTax.doubleValue() == value1.doubleValue() + minDistance) {
						logger.info("Got multiple 1");
						finalInvoiceMap.put("" + filingChallanDetailBean.hashCode(), invoiceMap.get(key1));
					} else if (distance2 >= 0 && challanTax.doubleValue() == value2.doubleValue() + minDistance) {
						logger.info("Got multiple 2");
						finalInvoiceMap.put("" + filingChallanDetailBean.hashCode(), invoiceMap.get(key2));
					}
				}
			}
		}
	}

	private List<DeducteeDetailDTO> getLineItemsByReceipt(String tanNumber, String tenantId, int assessmentYear,
			boolean isForNonResidents, String bsrCode, String challanSerialNo, String receiptDate,
			boolean isDividendEnabled) {

		List<DeducteeDetailDTO> receiptInvoices = new ArrayList<DeducteeDetailDTO>();

		ResponseEntity<ApiStatus<List<InvoiceLineItem>>> invoices = ingestionClient
				.invoiceByTanYearBSRCodeSerialNoAndDate(assessmentYear, tanNumber, true, isForNonResidents, bsrCode,
						challanSerialNo, receiptDate, tenantId, false);
		List<InvoiceLineItem> invoiceLienItemData = invoices.getBody().getData();
		logger.info("invoice response size : {}", invoiceLienItemData.size());
		for (InvoiceLineItem lineItem : invoiceLienItemData) {
			DeducteeDetailDTO deducteeDetailDTO = new DeducteeDetailDTO();
			deducteeDetailDTO.setAmount(lineItem.getInvoiceAmount());
			deducteeDetailDTO.setDeducteeName(lineItem.getDeducteeName());
			deducteeDetailDTO.setPan(lineItem.getPan());
			deducteeDetailDTO.setFinalTDSAmount(lineItem.getFinalTdsAmount());
			deducteeDetailDTO.setFinalTDSRate(lineItem.getFinalTdsRate());
			deducteeDetailDTO.setFinalTDSSection(lineItem.getFinalTdsSection());
			deducteeDetailDTO.setDocumentPostingDate(lineItem.getDocumentPostingDate());
			// For Non resident 27Q data
			if ("Y".equalsIgnoreCase(lineItem.getIsResident())) {
				List<NrTransactionsMeta> nrTransactionsMetaList = nrTransactionsMetaDAO.findByNrTransactionId(tanNumber,
						lineItem.getNrTransactionsMetaId());
				if (!nrTransactionsMetaList.isEmpty()) {
					NrTransactionsMeta nrTransactionsMeta = nrTransactionsMetaList.get(0);
					deducteeDetailDTO.setCountry(nrTransactionsMeta.getCountry());
					deducteeDetailDTO.setTin(nrTransactionsMeta.getTin());
					deducteeDetailDTO.setNatureOfRemittance(nrTransactionsMeta.getNatureOfRemittance());
					String address = StringUtils.EMPTY;
					if (StringUtils.isNotBlank(nrTransactionsMeta.getCountry())) {
						address += nrTransactionsMeta.getCountry();
					}
					deducteeDetailDTO.setAddress(address);
				}
			}
			if (lineItem.getHasLdc() != null && lineItem.getHasLdc().equals(true)) {
				deducteeDetailDTO.setLdcCertificateNo(lineItem.getLdcCertificateNumber());
			}
			receiptInvoices.add(deducteeDetailDTO);
		}

		List<AdvanceDTO> advanceLineItemData = ingestionClient
				.getAdvacnceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(assessmentYear, tanNumber, true,
						isForNonResidents, bsrCode, challanSerialNo, receiptDate, tenantId)
				.getBody().getData();
		logger.info("advance response size : {}", advanceLineItemData.size());
		for (AdvanceDTO lineItem : advanceLineItemData) {
			DeducteeDetailDTO deducteeDetailDTO = new DeducteeDetailDTO();
			deducteeDetailDTO.setAmount(lineItem.getAmount());
			deducteeDetailDTO.setDeducteeName(lineItem.getDeducteeName());
			deducteeDetailDTO.setPan(lineItem.getDeducteePan());
			deducteeDetailDTO.setFinalTDSAmount(lineItem.getFinalTdsAmount());
			deducteeDetailDTO.setFinalTDSRate(lineItem.getFinalTdsRate());
			deducteeDetailDTO.setFinalTDSSection(lineItem.getFinalTdsSection());
			deducteeDetailDTO.setDocumentPostingDate(lineItem.getPostingDateOfDocument());
			// For Non resident 27Q data
			if ("Y".equalsIgnoreCase(lineItem.getIsResident())) {
				List<NrTransactionsMeta> nrTransactionsMetaList = nrTransactionsMetaDAO.findByNrTransactionId(tanNumber,
						lineItem.getNrTransactionsMetaId());
				if (!nrTransactionsMetaList.isEmpty()) {
					NrTransactionsMeta nrTransactionsMeta = nrTransactionsMetaList.get(0);
					deducteeDetailDTO.setCountry(nrTransactionsMeta.getCountry());
					deducteeDetailDTO.setTin(nrTransactionsMeta.getTin());
					deducteeDetailDTO.setNatureOfRemittance(nrTransactionsMeta.getNatureOfRemittance());
					String address = StringUtils.EMPTY;
					if (StringUtils.isNotBlank(nrTransactionsMeta.getCountry())) {
						address += nrTransactionsMeta.getCountry();
					}
					deducteeDetailDTO.setAddress(address);
				}
			}
			if (lineItem.getHasLdc() != null && lineItem.getHasLdc().equals(true)) {
				deducteeDetailDTO.setLdcCertificateNo(lineItem.getLdcCertificateNumber());
			}
			receiptInvoices.add(deducteeDetailDTO);
		}
		List<ProvisionDTO> provisionLineItemData = ingestionClient
				.getProvisionLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(assessmentYear, tanNumber, true,
						isForNonResidents, bsrCode, challanSerialNo, receiptDate, tenantId)
				.getBody().getData();
		logger.info("provision response size : {}", provisionLineItemData.size());
		for (ProvisionDTO lineItem : provisionLineItemData) {
			DeducteeDetailDTO deducteeDetailDTO = new DeducteeDetailDTO();
			deducteeDetailDTO.setAmount(lineItem.getProvisionalAmount());
			deducteeDetailDTO.setDeducteeName(lineItem.getDeducteeName());
			deducteeDetailDTO.setPan(lineItem.getDeducteePan());
			deducteeDetailDTO.setFinalTDSAmount(lineItem.getFinalTdsAmount());
			deducteeDetailDTO.setFinalTDSRate(lineItem.getFinalTdsRate());
			deducteeDetailDTO.setFinalTDSSection(lineItem.getFinalTdsSection());
			deducteeDetailDTO.setDocumentPostingDate(lineItem.getPostingDateOfDocument());
			// For Non resident 27Q data
			if ("Y".equalsIgnoreCase(lineItem.getIsResident())) {
				List<NrTransactionsMeta> nrTransactionsMetaList = nrTransactionsMetaDAO.findByNrTransactionId(tanNumber,
						lineItem.getNrTransactionsMetaId());
				if (!nrTransactionsMetaList.isEmpty()) {
					NrTransactionsMeta nrTransactionsMeta = nrTransactionsMetaList.get(0);
					deducteeDetailDTO.setCountry(nrTransactionsMeta.getCountry());
					deducteeDetailDTO.setTin(nrTransactionsMeta.getTin());
					deducteeDetailDTO.setNatureOfRemittance(nrTransactionsMeta.getNatureOfRemittance());
					String address = StringUtils.EMPTY;
					if (StringUtils.isNotBlank(nrTransactionsMeta.getCountry())) {
						address += nrTransactionsMeta.getCountry();
					}
					deducteeDetailDTO.setAddress(address);
				}
			}
			if (lineItem.getHasLdc() != null && lineItem.getHasLdc().equals(true)) {
				deducteeDetailDTO.setLdcCertificateNo(lineItem.getLdcCertificateNumber());
			}
			receiptInvoices.add(deducteeDetailDTO);
		}
		if (isDividendEnabled) {
			logger.info("Is dividends enabled: {}", isDividendEnabled);
			Map<String, String> receiptMap = new HashMap<>();
			receiptMap.put("bsrCode", bsrCode);
			receiptMap.put("receiptSerailNo", challanSerialNo);
			receiptMap.put("receiptDate", receiptDate);
			receiptMap.put("assessmentYear", String.valueOf(assessmentYear));
			List<DeducteeDetailDTO> deductees = null;
			logger.info("Dividends resident flag: {}", isForNonResidents);
			try {
				if (isForNonResidents) {

					deductees = ingestionClient.getNonResidentShareholders(tanNumber, tenantId, receiptMap).getBody()
							.getData();
					deductees.stream().forEach(n -> {
						if (n.getLdcCertificateNo() != null) {
							NonResidentWithholdingDetails withholding = NonResidentWithholdingDetails
									.fromJSON(n.getLdcCertificateNo());
							if (withholding.getLdcSummary() != null) {
								n.setLdcCertificateNo(withholding.getLdcSummary().getCertificateNumber());
							} else {
								n.setLdcCertificateNo("");
							}
						}
					});

				} else {
					deductees = ingestionClient.getResidentShareholders(tanNumber, tenantId, receiptMap).getBody()
							.getData();
					deductees.stream().forEach(n -> {
						if (n.getLdcCertificateNo() != null) {
							ResidentWithholdingDetails withholding = ResidentWithholdingDetails
									.fromJSON(n.getLdcCertificateNo());
							if (withholding.getLdcSummary() != null) {
								n.setLdcCertificateNo(withholding.getLdcSummary().getCertificateNumber());
							} else {
								n.setLdcCertificateNo("");
							}
						}
					});
				}
				if (!deductees.isEmpty()) {
					receiptInvoices.addAll(deductees);
				}
			} catch (Exception e) {
				logger.error("Error occured while getting invoice share holder data.", e);
			}
		}

		logger.info("Total responses size : {}", receiptInvoices.size());
		return receiptInvoices;
	}

	@Async
	public String asyncCreateReturnFilingReport(String formType, String tanNumber, String deductorPan,
			int assessmentYear, String quarter, String tenantId, String userName, Boolean isAggriated)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		MultiTenantContext.setTenantId(tenantId);
		createReturnFilingReport(formType, tanNumber, deductorPan, assessmentYear, quarter, tenantId, userName,
				isAggriated);
		return "Filing record created Successfully";
	}

	@Transactional
	public String createReturnFilingReport(String formType, String tanNumber, String deductorPan, int assessmentYear,
			String quarter, String tenantId, String userName, Boolean isAggriated)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Filing: Started {} generation for TAN: {}, year: {}, quarter: {}", formType, tanNumber,
				assessmentYear, quarter);
		// Parent Bean
		FilingFileBean filingFileBean = new FilingFileBean();
		FilingBatchHeaderBean filingBatchHeader = new FilingBatchHeaderBean();
		// getting data for challan
		List<FilingChallanDetailBean> filingChallanDetails = generateChallanDetail(assessmentYear, quarter, tenantId,
				tanNumber, filingBatchHeader, "27Q".equalsIgnoreCase(formType));

		// getting data for deductee set to bean
		// Change the Logic to get the Data By Receipt Serial No and BSR Code So that
		// Exact mapping will be done.
		generateDeducteeDetail(tanNumber, tenantId, assessmentYear, quarter, filingChallanDetails,
				"27Q".equalsIgnoreCase(formType), isAggriated, deductorPan);

		// getting the deductor details
		generateBatchHeader(assessmentYear, filingChallanDetails.size(), tenantId, tanNumber, formType, quarter,
				deductorPan, filingBatchHeader);

		// setting the child beans
		filingFileBean.setBatchHeaderBean(filingBatchHeader);
		filingFileBean.setChallanDetailBeanList(filingChallanDetails);

		// generate the text
		String textUrl = generateTextFile(filingFileBean, tenantId, formType, false, assessmentYear, quarter);

		filingLogic(formType, quarter, assessmentYear, tanNumber, userName, textUrl, formType);

		saveInFilingStatus(assessmentYear, quarter, deductorPan, tanNumber, ReturnType.REGULAR.name(), tenantId,
				userName, formType);

		// save in filing files tables

		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;

		List<ActivityTracker> response = activityTrackerDAO.getActivityTrackerByTanYearTypeAndMonth(tanNumber,
				assessmentYear, ActivityType.QUARTERLY_TDS_FILING.getActivityType(), month);
		if (!response.isEmpty()) {
			ActivityTracker tracker = response.get(0);
			tracker.setStatus(ActivityTrackerStatus.VALIDATED.name());
			tracker.setModifiedBy(userName);
			tracker.setModifiedDate(new Date());
			activityTrackerDAO.update(tracker);
		}
		logger.info("Filing: Done {} generation for TAN: {}, year: {}, quarter: {}", formType, tanNumber,
				assessmentYear, quarter);
		return "Filing record created Successfully";
	}

	/**
	 * This method returns deductor data
	 * 
	 * @param deductorId
	 * @param tenantId
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public DeductorMasterDTO getDeductorData(Long deductorId, String tenantId) throws IOException {

		DeductorMasterDTO data = null;

		try {
			data = mastersClient.getDeductorMasterData(tenantId, deductorId);
		} catch (Exception e) {
			logger.error("Error occured while getting deductor master record", e);
		}
		return data;

	}

	/**
	 * This method returns the challan data based on quarter
	 *
	 * @param assessmentYear
	 * @param quarter
	 * @param tenantId
	 * @param tanNumber
	 * @return
	 */
	public List<ChallanReceiptDTO> getChallanData(int assessmentYear, String quarter, String tenantId,
			String tanNumber) {

		List<ChallanReceiptDTO> list = null;
		try {
			list = challansClient.getChallanReceiptData(tanNumber, assessmentYear, quarter, tenantId);

		} catch (Exception e) {
			logger.error("Error occured while getting Challan Or receipt record");
			throw new CustomException("Error occured while retrieving challan receipt data", HttpStatus.NOT_FOUND);
		}
		return list;

	}

	/**
	 * 
	 * @param tanNumber
	 * @param tenantId
	 * @return
	 */
	public List<DeducteeMasterDTO> getResidentDeducteeData(String tanNumber, String tenantId) {

		List<DeducteeMasterDTO> deducteeData = null;
		try {
			deducteeData = onBoardingClient.getResidentDeducteeData(tenantId, tanNumber);
		} catch (Exception e) {
			logger.error("Error occured while getting deductee record");
		}
		return deducteeData;
	}

	/**
	 * This api updates the PnrOrTokenNumber and Status in filing status record
	 * 
	 * @param filingStatus
	 * @return
	 */
	@Transactional
	public FilingStatus updateFilingStatus(FilingStatus filingStatus) {
		List<FilingStatus> listStatus = filingStatusDAO.findById(filingStatus.getFilingStatusId());
		if (!listStatus.isEmpty()) {
			FilingStatus status = listStatus.get(0);
			logger.info("Record is present in filing status");
			status.setStatus(filingStatus.getStatus());
			status.setPnrOrTokenNumber(filingStatus.getPnrOrTokenNumber());
			try {
				filingStatus = filingStatusDAO.update(status);
			} catch (Exception e) {
				logger.error("Error occured while updating record in file status");
			}
			return filingStatus;
		} else {
			throw new IllegalArgumentException("Invalid filing status id:" + filingStatus.getFilingStatusId());
		}
	}

	public List<FilingFilesDTO> findFilingByYearDeductorIdQuarter(Integer assessmentYear, String deductorPan,
			String quarter, String deductorTan, String formType) {
		List<FilingFiles> filingFiles = filingFilesDAO.findByYearTanQuarterFileType(assessmentYear, deductorTan,
				quarter, formType);
		List<FilingFilesDTO> filingFilesList = new ArrayList<FilingFilesDTO>();
		for (FilingFiles filingFile : filingFiles) {
			FilingFilesDTO filingFilesDTO = new FilingFilesDTO();

			filingFilesDTO.setId(filingFile.getFilingFilesId());

			filingFilesDTO.setIsRequested(filingFile.getIsRequested());
			filingFilesDTO.setQuarter(filingFile.getQuarter());
			filingFilesDTO.setAssesmentYear(filingFile.getAssesmentYear());
			filingFilesDTO.setFilingType(
					filingFile.getFilingType() == null ? FilingFilesType.UNKNOWN.name() : filingFile.getFileType());
			filingFilesDTO.setFileStatus(filingFile.getFileStatus() == null ? "--NA--" : filingFile.getFileStatus());
			filingFilesDTO.setGeneratedDate(filingFile.getGeneratedDate());
			filingFilesDTO.setFileType(filingFile.getFileType());
			filingFilesDTO.setFileUrl(filingFile.getFileUrl());
			filingFilesDTO.setRequestNumber(filingFile.getRequestNumber());
			filingFilesDTO.setFormType(filingFile.getFormType());
			filingFilesList.add(filingFilesDTO);
		}
		Collections.sort(filingFilesList, new FilingFilesComparator());
		return filingFilesList;
	}

	public List<FilingFilesDTO> findFilingByYear(Integer assessmentYear, String deductorTan) {
		List<FilingFiles> filingFiles = filingFilesDAO.findFilingByYearAndTan(assessmentYear, deductorTan);
		List<FilingFilesDTO> filingFilesList = new ArrayList<>();
		for (FilingFiles filingFile : filingFiles) {
			FilingFilesDTO filingFilesDTO = new FilingFilesDTO();
			filingFilesDTO.setId(filingFile.getFilingFilesId());
			filingFilesDTO.setIsRequested(filingFile.getIsRequested());
			filingFilesDTO.setQuarter(filingFile.getQuarter());
			filingFilesDTO.setAssesmentYear(filingFile.getAssesmentYear());
			filingFilesDTO.setFilingType(
					filingFile.getFilingType() == null ? FilingFilesType.UNKNOWN.name() : filingFile.getFilingType());
			filingFilesDTO.setFileStatus(filingFile.getFileStatus() == null ? "--NA--" : filingFile.getFileStatus());
			filingFilesDTO.setGeneratedDate(filingFile.getGeneratedDate());
			filingFilesDTO.setFileType(filingFile.getFileType());
			filingFilesDTO.setFileUrl(filingFile.getFileUrl());
			filingFilesDTO.setRequestNumber(filingFile.getRequestNumber());
			filingFilesDTO.setFormType(filingFile.getFormType());
			filingFilesList.add(filingFilesDTO);
		}
		return filingFilesList;
	}

	public List<FilingDeductorCollector> getAlldeductorCollector(String tenantId) {
		ResponseEntity<ApiStatus<List<FilingDeductorCollector>>> filingDeductorCollector = mastersClient
				.getAlldeductorCollector(tenantId);
		List<FilingDeductorCollector> listData = filingDeductorCollector.getBody().getData();
		logger.info("List of Filing Deductor Collector Data is", listData);
		return listData;
	}

	public List<FilingMinistryCode> getAllMinistryCode(String tenantId) {
		ResponseEntity<ApiStatus<List<FilingMinistryCode>>> filingMinistryCode = mastersClient
				.getAllMinistryCode(tenantId);
		List<FilingMinistryCode> listData = filingMinistryCode.getBody().getData();
		logger.info("List of Filing Ministry Code Data is", listData);
		return listData;
	}

	public List<FilingMinorHeadCode> getAllMinorHeadCode(String tenantId) {
		ResponseEntity<ApiStatus<List<FilingMinorHeadCode>>> filingMinorHeadCode = mastersClient
				.getAllMinorHeadCode(tenantId);
		List<FilingMinorHeadCode> listData = filingMinorHeadCode.getBody().getData();
		logger.info("List of Filing Minor Head Code Data is", listData);
		return listData;
	}

	public List<FilingSectionCode> getAllSectionCode(String tenantId) {
		ResponseEntity<ApiStatus<List<FilingSectionCode>>> filingSectionCode = mastersClient
				.getAllSectionCode(tenantId);
		List<FilingSectionCode> listData = filingSectionCode.getBody().getData();
		logger.info("List of Filing Section Code Data is", listData);
		return listData;
	}

	public List<FilingStateCode> getAllStateCode(String tenantId) {
		ResponseEntity<ApiStatus<List<FilingStateCode>>> filingStateCode = mastersClient.getAllStateCode(tenantId);
		List<FilingStateCode> listData = filingStateCode.getBody().getData();
		logger.info("List of Filing State Code Data is", listData);
		return listData;
	}

	public FilingStatus findById(Integer id) {
		List<FilingStatus> response = filingStatusDAO.findById(id);
		if (!response.isEmpty()) {
			return response.get(0);
		} else {
			throw new CustomException("No status exists for the given criteria", HttpStatus.BAD_REQUEST);
		}
	}

	public FilingStatus saveFilingStatus(Integer assessmentYear, String quarter, String deductorPan, String tanNumber,
			String filingFormType, String tenantId, String userName, String pnrOrTokenNumber, String filingType) {
		FilingStatus filingStatus = new FilingStatus();
		filingStatus.setAssesmentYear(assessmentYear);
		filingStatus.setQuarter(quarter);
		filingStatus.setActive(true);
		filingStatus.setDeductorPan(deductorPan);
		filingStatus.setDeductorTan(tanNumber);
		filingStatus.setDueDateForFiling(null);
		filingStatus.setFilingType(filingType);
		filingStatus.setQuarterEndDate(null);
		filingStatus.setQuarterPeriod("3 Months");
		filingStatus.setQuarterStartDate(null);
		filingStatus.setRevisionExists(false);
		filingStatus.setStatus("submitted");
		filingStatus.setCreatedBy(userName);
		filingStatus.setCreatedDate(new Date());
		filingStatus.setPnrOrTokenNumber(pnrOrTokenNumber);
		filingStatus.setFileType(filingFormType);
		filingStatus.setFormType(filingFormType);
		return filingStatusDAO.save(filingStatus);
	}

	public FilingStatus findByYearAndQuarter(Integer assessmentYear, String quarter, String tanNumber,
			String filingFormType) {
		List<FilingStatus> response = filingStatusDAO.findByYearAndQuarterAndTan(assessmentYear, quarter, tanNumber,
				filingFormType);
		if (!response.isEmpty()) {
			return response.get(0);
		} else {
			throw new CustomException("No status exists for given criteria");
		}
	}

	private List<DeducteeDetailDTO> getAggrigatedList(List<DeducteeDetailDTO> list) {
		Map<String, DeducteeDetailDTO> mapDto = new HashMap<String, DeducteeDetailDTO>();
		List<DeducteeDetailDTO> extractedList = new ArrayList<DeducteeDetailDTO>();
		for (DeducteeDetailDTO dto : list) {
			String unique = dto.getDeducteeName() + "" + dto.getPan() + "" + dto.getFinalTDSSection() + ""
					+ dto.getDocumentPostingDate() + "".toLowerCase();
			if (mapDto.containsKey(unique)) {
				BigDecimal finalTdsAmount = mapDto.get(unique).getFinalTDSAmount().add(dto.getFinalTDSAmount());
				BigDecimal amount = mapDto.get(unique).getAmount().add(dto.getAmount());
				mapDto.get(unique).setFinalTDSAmount(finalTdsAmount);
				mapDto.get(unique).setAmount(amount);

			} else {
				mapDto.put(unique, dto);
			}
		}

		for (Map.Entry<String, DeducteeDetailDTO> set : mapDto.entrySet()) {
			extractedList.add(set.getValue());
		}
		return extractedList;
	}

	public Map<String, Object> getReceiptInvoiceDeails(String tenantId, int assessmentYear, String quarter,
			String deductorTan, String fileType) throws ParseException {
		logger.info("Service method executing to get the invoice and receipt details {}");
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> invoiceMap = new HashMap<>();
		boolean residentIndicator = "27Q".equalsIgnoreCase(fileType) ? true : false;

		List<FilingStatus> listStatus = filingStatusDAO.findByYearAndQuarterAndFileType(assessmentYear, quarter,
				deductorTan, fileType);
		if (!listStatus.isEmpty()) {
			map.put("TokenNumber", listStatus.get(0).getPnrOrTokenNumber());

			List<Receipt> listReciept = challansClient.getRecieptByTanYearAndQuarter(deductorTan,
					CommonUtil.QUARTER_MONTHS_MAP.get(quarter), assessmentYear, tenantId, residentIndicator).getBody()
					.getData();
			logger.info("No of receipts retrieved " + listReciept.size() + "{}");

			if (listReciept.isEmpty()) {
				throw new CustomException(String.format("There are no receipts exists for Quarter %s", quarter));
			}

			// getting invoices by receipt

			for (Receipt receipt : listReciept) {

				List<InvoiceLineItem> invoiceList = ingestionClient
						.invoiceByTanYearBSRCodeSerialNoAndDate(assessmentYear, deductorTan, true, residentIndicator,
								receipt.getBsrCode(), receipt.getChallanSerialNumber(), receipt.getDate(), tenantId,
								true)
						.getBody().getData();
				String receiptDateStr = receipt.getDate();
				Date receiptDate = new SimpleDateFormat("ddMMyyyy").parse(receiptDateStr);
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
				receipt.setDate(dateFormat.format(receiptDate));

				// the list is having max 3 records records
				for (InvoiceLineItem invoice : invoiceList) {
					invoiceMap.put(invoice.getPan(), invoice.getFinalTdsAmount());
				}
				// setting into map
				map.put("ReceiptDetails", receipt);
				map.put("InvoiceDetails", invoiceMap);

			}

		}

		return map;
	}

	public FilingFiles get26QFileFromConsoFile(String tenantId, String tanNumber, Integer assessmentYear,
			String quarter, String userName) throws Exception {

		String fileType = "Conso";
		List<FilingFiles> filingFilesList = filingFilesDAO.findByYearTanQuarterFileType(assessmentYear, tanNumber,
				quarter, fileType);
		if (!filingFilesList.isEmpty()) {
			try {
				FilingFiles filingFilesObj = filingFilesList.get(0);
				String password = tanNumber + "_" + filingFilesObj.getRequestNumber();
				File file = blobStorageService.getFileFromBlobUrl(tenantId, filingFilesObj.getFileUrl());

				String fileName = filingFilesObj.getFileUrl()
						.substring(filingFilesObj.getFileUrl().lastIndexOf("/") + 1);
				String destPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("."));

				ZipFile zipFile = new ZipFile(file.getAbsolutePath());
				// If it is encrypted then provide password
				if (zipFile.isEncrypted()) {
					zipFile.setPassword(password);
				}
				zipFile.extractAll(destPath);

				File tdsFile = new File(destPath + "/" + fileName.replace("zip", "tds"));

				logger.info("Extracted TDS file name {}", tdsFile.getName());
				String tdsTextfileUrl = blob.uploadExcelToBlobWithFile(tdsFile, tenantId);
				logger.info("Text file url {}", tdsTextfileUrl);

				String excel26QUrl = rpuFileReadingService.generateExcelFromTdsTextFile(filingFilesObj.getFileUrl(),
						tenantId, filingFilesObj, true);
				logger.info("Excel generated successfully with : {}", excel26QUrl);

				FilingFiles filingFiles = new FilingFiles();
				filingFiles.setAssesmentYear(assessmentYear);
				filingFiles.setQuarter(quarter);
				filingFiles.setCreatedDate(new Date());
				filingFiles.setCreatedBy(userName);

				filingFiles.setActive(true);
				filingFiles.setFileStatus("GENERATED");
				filingFiles.setFilingType(ReturnType.REGULAR.name());
				filingFiles.setDeductorTan(tanNumber);
				filingFiles.setFileType("Revised Input File");
				filingFiles.setFileUrl(excel26QUrl);
				filingFiles.setIsRequested(true);
				filingFiles.setGeneratedDate(new Date());
				filingFiles.setFormType(filingFilesObj.getFormType());
				filingFilesDAO.save(filingFiles);

				return filingFiles;
			} catch (Exception e) {
				logger.error("Exception occured while converting Conso file into excel file", e);
				throw new CustomException(
						String.format("Exception ocuured while converting Conso file into excel file"));
			}
		} else {
			throw new CustomException("There is no data in filling files");
		}

	}

	public FilingStatus findByYearAndQuarterAndFileType(String tenantId, int assessmentYear, String quarter,
			String deductorTan, String formType) {
		List<FilingStatus> filingStatusList = filingStatusDAO.findByYearAndQuarterAndTan(assessmentYear, quarter,
				deductorTan, formType);
		FilingStatus filingStatus = new FilingStatus();
		if (!filingStatusList.isEmpty()) {
			filingStatus = filingStatusList.get(0);
		}
		return filingStatus;
	}

	/**
	 * 
	 * @param dto
	 * @return
	 */
	public FilingStatusResponseDTO copyToResponseDTO(FilingStatus dto) {

		FilingStatusResponseDTO filingStatus = new FilingStatusResponseDTO();
		filingStatus.setFilingStatusAssessmentYear(dto.getAssesmentYear());
		filingStatus.setFilingStatusQuarter(dto.getQuarter());
		filingStatus.setFilingStatusId(dto.getFilingStatusId());
		filingStatus.setFilingStatusActive(dto.getActive());
		filingStatus.setFilingStatusCreatedBy(dto.getCreatedBy());
		filingStatus.setFilingStatusCreatedDate(dto.getCreatedDate());
		filingStatus.setFilingStatusDeductorPan(dto.getDeductorPan());
		filingStatus.setFilingStatusDeductorTan(dto.getDeductorTan());
		filingStatus.setFilingStatusDuedateForFiling(dto.getDueDateForFiling());
		filingStatus.setFilingStatusFileType(dto.getFileType());
		filingStatus.setFilingStatusFilingType(dto.getFilingType());
		filingStatus.setFilingStatusPnrOrTokenNumber(dto.getPnrOrTokenNumber());
		filingStatus.setFilingStatusQuarterEndDate(dto.getQuarterEndDate());
		filingStatus.setFilingStatusQuarterPeriod(dto.getQuarterPeriod());
		filingStatus.setFilingStatusQuarterStartDate(dto.getQuarterStartDate());
		filingStatus.setFilingStatusRevisionExists(dto.getRevisionExists());
		filingStatus.setFilingStatusStatus(dto.getStatus());
		filingStatus.setFilingStatusUpdatedBy(dto.getUpdatedBy());
		filingStatus.setFilingStatusUpdatedDate(dto.getUpdatedDate());
		return filingStatus;

	}

	/**
	 * copies values from dto to response dto
	 * 
	 * @param dto
	 * @return
	 */
	public FilingFilesResponseDTO copyToFilingFileResponseDTO(FilingFiles dto) {
		FilingFilesResponseDTO response = new FilingFilesResponseDTO();
		response.setFilingFilesAssesmentYear(dto.getAssesmentYear());
		response.setFilingFilesId(dto.getFilingFilesId());
		response.setFilingFilesQuarter(dto.getQuarter());
		response.setFilingFilesActive(dto.getActive());
		response.setFilingFilesCreatedBy(dto.getCreatedBy());
		response.setFilingFilesCreatedDate(dto.getCreatedDate());
		response.setFilingFilesDeductorTan(dto.getDeductorTan());
		response.setFilingFilesFileStatus(dto.getFileStatus());
		response.setFilingFilesFileType(dto.getFileType());
		response.setFilingFilesFileUrl(dto.getFileUrl());
		response.setFilingFilesFilingType(dto.getFilingType());
		response.setFilingFilesFormType(dto.getFormType());
		response.setFilingFilesGeneratedDate(dto.getGeneratedDate());
		response.setFilingFilesIsRequested(dto.getIsRequested());
		response.setFilingFilesRequestNumber(dto.getRequestNumber());
		response.setFilingFilesUpdatedby(dto.getUpdatedby());
		response.setFilingFilesUpdatedDate(dto.getUpdatedDate());
		return response;

	}

	/**
	 * 
	 * @param filingfilesId
	 * @return
	 */
	public FilingFiles findByFilingFilesId(Integer filingfilesId) {
		FilingFiles filingFiles = new FilingFiles();
		List<FilingFiles> filingFilesList = filingFilesDAO.findById(filingfilesId);
		if (!filingFilesList.isEmpty()) {
			filingFiles = filingFilesList.get(0);
		}

		return filingFiles;
	}

	/**
	 * 
	 * @param filingFilesObj
	 * @return
	 */
	public FilingFiles updateFilingFiles(FilingFiles filingFilesObj) {
		return filingFilesDAO.update(filingFilesObj);
	}

	/**
	 * 
	 * @param filingFiles
	 * @return
	 */
	public FilingFiles save(FilingFiles filingFiles) {
		return filingFilesDAO.save(filingFiles);
	}

	/**
	 * 
	 * @param year
	 * @param quarter
	 * @param deductorTan
	 * @param formType
	 * @param fileType
	 * @return
	 */
	public List<FilingFiles> findByYearQuarterDeductorTanAndFormTypeFileType(int year, String quarter,
			String deductorTan, String formType, String fileType) {
		return filingFilesDAO.findByYearAndQuarterAndFileType(year, quarter, fileType, deductorTan, formType);
	}

	@Async
	public void emailForm16Certificates(Integer year, String quarter, Integer filingId, String tanNumber,
			String tenantId, String deductorPan, String ccEmail, String contactName) throws MessagingException,
			IOException, ZipException, InvalidKeyException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);

		List<FilingFiles> filingFilesList = filingFilesDAO.findById(filingId);
		if (!filingFilesList.isEmpty()) {
			FilingFiles filingFiles = filingFilesList.get(0);
			List<Map<String, Object>> dbForm16DetailsMap = form16DetailsDAO
					.getForm16FileNamesByQuarterYearTan(tanNumber, year, quarter, filingFiles.getFormType());
			Map<String, Long> form16DetailsMap = new HashMap<>();
			for (Map<String, Object> form16Details : dbForm16DetailsMap) {
				String fileName = (String) form16Details.get("fileName");
				Long id = (Long) form16Details.get("id");
				form16DetailsMap.put(fileName, id);
			}
			String password = tanNumber + "_" + filingFiles.getRequestNumber();
			logger.info("Filing file url {}", filingFiles.getFileUrl());
			boolean isDividendEnabled = false;
			List<DeductorOnboardingInformationDTO> deductorOnboardingList = onboardingClient
					.getDeductorOnboardingInfo(tenantId, deductorPan).getBody().getData();
			if (!deductorOnboardingList.isEmpty()) {
				DeductorOnboardingInformationDTO deductorInfo = deductorOnboardingList.get(0);
				isDividendEnabled = deductorInfo.getDvndEnabled() != null ? deductorInfo.getDvndEnabled() : false;
			}
			String deductorName = filingFilesDAO.getDeductorName(deductorPan);
			File file = blobStorageService.getFileFromBlobUrl(tenantId, filingFiles.getFileUrl());

			String destPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("."));

			ZipFile zipFile = new ZipFile(file.getAbsolutePath());
			// If it is encrypted then provide password
			if (zipFile.isEncrypted()) {
				zipFile.setPassword(password);
			}
			zipFile.extractAll(destPath);

			List<File> filesInFolder = Files.walk(Paths.get(destPath)).filter(Files::isRegularFile).map(Path::toFile)
					.collect(Collectors.toList());
			logger.info("Form16 files count: {}", filesInFolder.size());
			boolean isForNonResident = "27Q".equalsIgnoreCase(filingFiles.getFormType());
			Map<String, Map<String, Object>> deducteeMap = null;
			Map<String, Map<String, Object>> shareHolderMap = null;
			List<Map<String, Object>> data = null;
			if (isForNonResident) {
				data = filingFilesDAO.getNRDeducteeEmails(deductorPan);
				deducteeMap = getDeducteeEmails(data);
				if (isDividendEnabled) {
					data = filingFilesDAO.getNrShareHolderEmails(deductorPan);
					shareHolderMap = getDeducteeEmails(data);
				}
			} else {
				data = filingFilesDAO.getResDeducteeEmails(deductorPan);
				deducteeMap = getDeducteeEmails(data);
				if (isDividendEnabled) {
					data = filingFilesDAO.getResShareHolderEmails(deductorPan);
					shareHolderMap = getDeducteeEmails(data);
				}
			}
			int successCount = 0;
			int errorCount = 0;
			List<Form16Details> form16DetailsBatchInsert = new ArrayList<>();
			List<Form16Details> form16DetailsBatchUpdate = new ArrayList<>();
			for (int i = 0; i < filesInFolder.size(); i++) {
				File pdfFiles = filesInFolder.get(i);
				if (pdfFiles != null && pdfFiles.getAbsolutePath().contains(".pdf")) {
					String[] names = pdfFiles.getName().split("_");
					if (names.length > 0) {
						String deducteePan = names[0];
						if (StringUtils.isNotBlank(deducteePan)) {
							List<String> deducteeNames = new ArrayList<>();
							List<String> emailAddress = new ArrayList<>();
							List<String> deducteeCodes = new ArrayList<>();
							Map<String, Object> deducteeData = null;
							if (deducteeMap != null && deducteeMap.get(deducteePan) != null) {
								deducteeData = deducteeMap.get(deducteePan);
								emailAddress.add((String) deducteeData.get("email"));
								deducteeNames.add((String) deducteeData.get("name"));
								deducteeCodes.add((String) deducteeData.get("code"));
							}
							if (isDividendEnabled && shareHolderMap != null
									&& shareHolderMap.get(deducteePan) != null) {
								deducteeData = shareHolderMap.get(deducteePan);
								emailAddress.add((String) deducteeData.get("email"));
								deducteeNames.add((String) deducteeData.get("name"));
							}
							if (!emailAddress.isEmpty() && StringUtils.isNotBlank(deductorName)) {
								File pdfFile = new File(pdfFiles.getAbsolutePath());
								String pdfFilePath = blob.uploadExcelToBlobWithFile(pdfFile, tenantId);
								String deducteeCode = StringUtils.EMPTY;
								if (!deducteeCodes.isEmpty()) {
									deducteeCode = deducteeCodes.get(0);
								}
								Integer assessmentYear = filingFiles.getAssesmentYear();
								String financialYear = assessmentYear + "-" + (assessmentYear + 1);
								Email email = new Email();
								Form16Details form16DetailsDTO = new Form16Details();
								try {
									Context ctx = new Context();
									email.setFrom("EY on behalf of " + deductorName + " <" + mailsentfrom + ">");
									email.setTo(emailAddress.get(0));
									if (StringUtils.isNotBlank(ccEmail)) {
										email.setCc(ccEmail);
									}
									ctx.setVariable("quarter", CommonUtil.getQuarterDetails(quarter, assessmentYear));
									ctx.setVariable("financialyear", financialYear);
									ctx.setVariable("assessmentyear", assessmentYear);
									ctx.setVariable("quarterNo", CommonUtil.getQuarterNos(quarter));
									ctx.setVariable("deductorname", deductorName);
									ctx.setVariable("deducteename", deducteeNames.get(0));
									ctx.setVariable("contactName", contactName);
									email.setSubject("Form 16A certificate for " + CommonUtil.getQuarterNos(quarter)
											+ " Quarter of FY " + (assessmentYear - 1) + "-" + (assessmentYear % 100)
											+ " (i.e. AY " + assessmentYear + "-" + (assessmentYear + 1) % 100 + ")");
									String body = templateEngine.process("form16emailtemplate", ctx);
									emailService.sendHtmlTemplateNotification(email, body, pdfFiles.getAbsolutePath());
									successCount = successCount + 1;

									form16DetailsDTO = getForm16Details(deductorPan, tanNumber, year, quarter,
											emailAddress.get(0), "Success", deducteeNames.get(0), deducteeCode,
											deducteePan);
									form16DetailsDTO.setFormType(isForNonResident ? "27Q" : "26Q");
									form16DetailsDTO.setFileName(pdfFiles.getName());
									form16DetailsDTO.setDeductorEmail(ccEmail);
									form16DetailsDTO.setDeductorName(contactName);
									form16DetailsDTO.setFileUrl(pdfFilePath);
									if (form16DetailsMap.get(pdfFiles.getName()) != null) {
										form16DetailsDTO.setId(form16DetailsMap.get(pdfFiles.getName()));
										form16DetailsBatchUpdate.add(form16DetailsDTO);
									} else {
										form16DetailsBatchInsert.add(form16DetailsDTO);
									}
								} catch (Exception e) {
									logger.error("Exception occured while sending email: {}", email.getTo());
									errorCount = errorCount + 1;
									form16DetailsDTO = getForm16Details(deductorPan, tanNumber, year, quarter,
											emailAddress.get(0), "Failure", deducteeNames.get(0), deducteeCode,
											deducteePan);
									form16DetailsDTO.setFormType(isForNonResident ? "27Q" : "26Q");
									form16DetailsDTO.setFileName(pdfFiles.getName());
									form16DetailsDTO.setDeductorEmail(ccEmail);
									form16DetailsDTO.setDeductorName(contactName);
									form16DetailsDTO.setFileUrl(pdfFilePath);
									if (form16DetailsMap.get(pdfFiles.getName()) != null) {
										form16DetailsDTO.setId(form16DetailsMap.get(pdfFiles.getName()));
										form16DetailsBatchUpdate.add(form16DetailsDTO);
									} else {
										form16DetailsBatchInsert.add(form16DetailsDTO);
									}
								}
							}
						}
					}
				}
			}
			logger.info("Emails sent successfully: {}", successCount);
			logger.info("Emails failed count: {}", errorCount);
			if (!form16DetailsBatchInsert.isEmpty()) {
				form16DetailsDAO.batchSaveForm16Details(form16DetailsBatchInsert);
			}
			if (!form16DetailsBatchUpdate.isEmpty()) {
				form16DetailsDAO.batchUpdateForm16Details(form16DetailsBatchUpdate);
			}
		}
	}

	private Form16Details getForm16Details(String deductorPan, String tan, Integer year, String quarter, String email,
			String status, String deducteeName, String deducteeCode, String deducteePan) {
		Form16Details form16Details = new Form16Details();
		form16Details.setActive(true);
		form16Details.setDeductorMasterPan(deductorPan);
		form16Details.setDeductorMasterTan(tan);
		form16Details.setYear(year);
		form16Details.setQuarter(quarter);
		form16Details.setEmail(email);
		form16Details.setStatus("Available");
		form16Details.setDeliveryStatus(status);
		if ("Success".equalsIgnoreCase(status)) {
			form16Details.setEmailDispatchDate(new Timestamp(new Date().getTime()));
		}
		form16Details.setCreatedDate(new Timestamp(new Date().getTime()));
		form16Details.setName(deducteeName);
		form16Details.setCode(deducteeCode);
		form16Details.setPan(deducteePan);

		return form16Details;
	}

	public FilingFiles uploadSignedPdfs(Integer assesmentYear, String quarter, int filingId, String userName,
			String tenantId, MultipartFile zipFile)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		FilingFiles filingFiles = null;
		List<FilingFiles> filingFilesList = filingFilesDAO.findById(filingId);
		if (!filingFilesList.isEmpty()) {
			filingFiles = filingFilesList.get(0);
			String zipFileUrl = blob.uploadExcelToBlob(zipFile, tenantId);
			filingFiles.setFileUrl(zipFileUrl);
			filingFiles.setUpdatedby(userName);
			filingFiles.setUpdatedDate(new Date());
			filingFiles = filingFilesDAO.update(filingFiles);
		}
		return filingFiles;
	}

	private Map<String, Map<String, Object>> getDeducteeEmails(List<Map<String, Object>> deductees) {
		Map<String, Map<String, Object>> deducteeMap = new HashMap<>();
		for (Map<String, Object> deductee : deductees) {
			String email = (String) deductee.get("email");
			String deducteePan = (String) deductee.get("pan");
			if (StringUtils.isNotBlank(deducteePan) && StringUtils.isNotBlank(email)) {
				deducteeMap.put(deducteePan, deductee);
			}
		}
		return deducteeMap;
	}

	@Async
	public void asyncDownloadForm16EmailReport(Integer year, String quarter, String tanNumber, String tenantId,
			String deductorPan, Integer month, String userName, String formType) throws InvalidKeyException, FileNotFoundException,
			IOException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		downloadForm16EmailReport(year, quarter, tanNumber, tenantId, deductorPan, month, userName, formType);
	}

	/**
	 * 
	 * @param year
	 * @param quarter
	 * @param tanNumber
	 * @param tenantId
	 * @param deductorPan
	 * @param month
	 * @param userName
	 * @param formType
	 * @throws InvalidKeyException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	private void downloadForm16EmailReport(Integer year, String quarter, String tanNumber, String tenantId,
			String deductorPan, Integer month, String userName, String formType) throws InvalidKeyException,
			FileNotFoundException, IOException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String uploadType = UploadTypes.FORM_16_REPORT_EMAIL_DOWNLOAD.name();
		String fileName = uploadType + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		BatchUpload batchUpload = saveBatchUploadReport(tanNumber, tenantId, year, null, 0L, uploadType, "Processing",
				month, userName, null, fileName);
		// get all form16A records
		List<Form16Details> form16AList = form16DetailsDAO.getForm16DetailsByQuarterYearTan(tanNumber, year, quarter,
				formType);

		String deductorName = filingFilesDAO.getDeductorName(deductorPan);

		int count = form16AList.size();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = null;
		resource = resourceLoader.getResource("classpath:templates/" + "form16A_report.xlsx");
		String msg = getErrorReportMsg(tenantId, deductorName, "FORM16A REPORT");
		InputStream input = resource.getInputStream();
		try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
			XSSFSheet sheet = wb.getSheetAt(0);

			Font fonts = wb.createFont();
			fonts.setBold(true);

			XSSFCellStyle style = wb.createCellStyle();
			style.setFont(fonts);
			style.setWrapText(true);
			style.setBorderLeft(BorderStyle.MEDIUM);
			style.setBorderTop(BorderStyle.MEDIUM);
			style.setBorderBottom(BorderStyle.MEDIUM);
			style.setBorderRight(BorderStyle.MEDIUM);
			style.setAlignment(HorizontalAlignment.LEFT);
			style.setVerticalAlignment(VerticalAlignment.CENTER);

			Font fonts2 = wb.createFont();
			fonts2.setBold(false);

			XSSFCellStyle style1 = wb.createCellStyle();
			style1.setFont(fonts);
			style1.setWrapText(true);
			style1.setFont(fonts2);
			style1.setLocked(true);

			XSSFRow row0 = sheet.createRow(0);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);

			Integer currentAssessmentYear = CommonUtil.getAssessmentYear(null);
			String challanAssessmentYear = String.valueOf(currentAssessmentYear - 1) + "-"
					+ String.valueOf(currentAssessmentYear).substring(2, 4);

			XSSFRow row4 = sheet.getRow(4);
			row4.createCell(1).setCellValue(challanAssessmentYear);

			int rowindex = 7;
			int serialNumber = 1;
			if (!form16AList.isEmpty()) {
				for (Form16Details form16a : form16AList) {
					XSSFRow row1 = sheet.createRow(rowindex++);
					row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));

					createSXSSFCell(style1, row1, 0, String.valueOf(serialNumber++));
					createSXSSFCell(style1, row1, 1, form16a.getCode());
					createSXSSFCell(style1, row1, 2, form16a.getName());
					createSXSSFCell(style1, row1, 3, form16a.getPan());
					createSXSSFCell(style1, row1, 4, form16a.getEmail());
					createSXSSFCell(style1, row1, 5, form16a.getFileName());
					createSXSSFCell(style1, row1, 6, form16a.getStatus());
					createSXSSFCell(style1, row1, 7,
							form16a.getEmailDispatchDate() != null ? form16a.getEmailDispatchDate().toString() : "");
					createSXSSFCell(style1, row1, 8, form16a.getDeliveryStatus());
					createSXSSFCell(style1, row1, 9, "");
				}
			}
			wb.write(out);
			saveBatchUploadReport(tanNumber, tenantId, year, out, Long.valueOf(count), uploadType, "Processed", month,
					userName, batchUpload.getBatchUploadID(), null);
		} catch (Exception e) {
			logger.info("Exception occured while preparing error file {} ", e.getMessage());
		}
	}

	/**
	 * @param style
	 * @param row
	 * @param cellNumber
	 * @param value
	 */
	private static void createSXSSFCell(XSSFCellStyle style, XSSFRow row, int cellNumber, String value) {
		Cell cell = row.createCell(cellNumber);
		cell.setCellValue(StringUtils.isBlank(value) == true ? StringUtils.EMPTY : value);
		cell.setCellStyle(style);
	}

	/**
	 *
	 * @param deductorMasterTan
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 */
	public String getErrorReportMsg(String tenantId, String deductorName, String fileType) {
		MultiTenantContext.setTenantId(tenantId);
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return fileType + " (Dated: " + date + ")\n Client Name: " + deductorName + "\n";
	}

	/**
	 * 
	 * @param deductorTan
	 * @param tenantId
	 * @param assessmentYear
	 * @param out
	 * @param noOfRows
	 * @param uploadType
	 * @param status
	 * @param month
	 * @param userName
	 * @param batchId
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws StorageException
	 * @throws ParseException
	 */
	public BatchUpload saveBatchUploadReport(String deductorTan, String tenantId, int assessmentYear,
			ByteArrayOutputStream out, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId, String fileName) throws FileNotFoundException, IOException, URISyntaxException,
			InvalidKeyException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String path = null;
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setSuccessFileUrl(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		batchUpload.setActive(true);
		if (StringUtils.isNotBlank(fileName)) {
			fileName = fileName + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
			batchUpload.setFileName(fileName);
		}
		List<BatchUpload> response = new ArrayList<>();
		if (batchId != null) {
			response = batchUploadDAO.findById(assessmentYear, deductorTan, uploadType, batchId);
		}
		if (!response.isEmpty()) {
			batchUpload = response.get(0);
			if (out != null) {
				File file = getConvertedExcelFile(out.toByteArray(), batchUpload.getFileName());
				path = blob.uploadExcelToBlobWithFile(file, tenantId);
				batchUpload.setStatus(status);
				batchUpload.setFilePath(path);
				batchUpload.setSuccessFileUrl(path);
				batchUpload.setRowsCount(noOfRows);
				batchUpload.setSuccessCount(noOfRows);
				batchUpload.setModifiedBy(userName);
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			} else {
				batchUpload.setModifiedBy(userName);
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchUpload.setRowsCount(0l);
			}
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	/**
	 *
	 * @param byteArray
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	protected File getConvertedExcelFile(byte[] byteArray, String fileName) throws IOException {
		File someFile = new File(fileName);
		try (FileOutputStream fos = new FileOutputStream(someFile)) {
			fos.write(byteArray);
			fos.flush();
			fos.close();
			return someFile;
		}
	}

	public FilingFiles requestForm16aManually(Integer assesmentYear, String quarter, String userName, String tenantId,
			String formType, Long requestNumber, String tan)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		FilingFiles filingFiles = new FilingFiles();
		filingFiles.setDeductorTan(tan);
		filingFiles.setActive(true);
		filingFiles.setAssesmentYear(assesmentYear);
		filingFiles.setQuarter(quarter);
		filingFiles.setUpdatedby(userName);
		filingFiles.setUpdatedDate(new Timestamp(new Date().getTime()));
		filingFiles.setGeneratedDate(new Timestamp(new Date().getTime()));
		filingFiles.setFormType(formType);
		filingFiles.setFileUrl(" ");
		filingFiles.setFileStatus("GENERATED");
		filingFiles.setFileType("FORM16");
		filingFiles.setFilingType("REGULAR");
		filingFiles.setIsRequested(true);
		filingFiles.setRequestNumber(requestNumber);
		filingFilesDAO.save(filingFiles);
		return filingFiles;
	}

	public Map<String, Object> getForm16AEmailDetails(String tan) {
		Map<String, Object> form16DetailsMap = new HashMap<>();
		List<Map<String, Object>> form16AList = form16DetailsDAO.getForm16EmailDetailsByQuarterYearTan(tan);
		for (Map<String, Object> form16Details : form16AList) {
			String email = (String) form16Details.get("email");
			String name = (String) form16Details.get("name");
			form16DetailsMap.put("email", email);
			form16DetailsMap.put("name", name);
		}
		return form16DetailsMap;
	}

}
