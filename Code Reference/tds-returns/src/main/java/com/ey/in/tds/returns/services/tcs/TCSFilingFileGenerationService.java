package com.ey.in.tds.returns.services.tcs;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tcs.common.domain.TCSLedger;
import com.ey.in.tcs.common.domain.TcsReceipt;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.FilingSectionCode;
import com.ey.in.tds.common.domain.FilingStateCode;
import com.ey.in.tds.common.domain.tcs.CollecteeDetailsDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSLccMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsPaymentDTO;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.dto.masters.deductor.TanAddressDTO;
import com.ey.in.tds.common.dto.returns.ChallanReceiptDTO;
import com.ey.in.tds.common.dto.returns.tcs.TCSFilingFilesDTO;
import com.ey.in.tds.common.jdbc.returns.dao.TCSFilingFilesDAO;
import com.ey.in.tds.common.jdbc.returns.dao.TCSFilingStatusDAO;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccMaster;
import com.ey.in.tds.common.returns.jdbc.dto.FilingFiles;
import com.ey.in.tds.common.returns.jdbc.dto.TCSFilingFiles;
import com.ey.in.tds.common.returns.jdbc.dto.TCSFilingStatus;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.FilingFilesType;
import com.ey.in.tds.core.util.ReturnType;
import com.ey.in.tds.feign.client.ChallansClient;
import com.ey.in.tds.feign.client.IngestionClient;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.returns.domain.tcs.TCSFilingBatchHeaderBean;
import com.ey.in.tds.returns.domain.tcs.TCSFilingChallanDetailBean;
import com.ey.in.tds.returns.domain.tcs.TCSFilingDeducteeDetailBean;
import com.ey.in.tds.returns.domain.tcs.TCSFilingFileBean;
import com.microsoft.azure.storage.StorageException;

@Service
public class TCSFilingFileGenerationService extends TCSRawFileGenerationService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private TCSFilingFilesDAO tCSFilingDAO;

	@Autowired
	private TCSFilingStatusDAO tCSFilingStatusDAO;

	@Autowired
	protected ChallansClient challansClient;

	@Autowired
	protected MastersClient mastersClient;

	@Autowired
	protected OnboardingClient onboardingClient;

	@Autowired
	protected IngestionClient ingestionClient;

	@Autowired
	private TCSLccMasterDAO lccMasterDAO;

	public List<TCSFilingFilesDTO> findTCSFilingByYear(Integer assessmentYear, String deductorTan) {

		List<TCSFilingFiles> filingFiles = tCSFilingDAO.findTCSFilingByYearAndTan(assessmentYear, deductorTan);
		List<TCSFilingFilesDTO> filingFilesList = new ArrayList<>();
		for (TCSFilingFiles filingFile : filingFiles) {
			TCSFilingFilesDTO filingFilesDTO = new TCSFilingFilesDTO();
			filingFilesDTO.setId(filingFile.getId());
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

	public List<TCSFilingFilesDTO> findTCSFilingByYearDeductorIdQuarter(Integer assessmentYear, String deductorPan,
			String quarter, String deductorTan, String formType) {
		List<TCSFilingFiles> filingFiles = tCSFilingDAO.findByYearQuarterDeductorTan(assessmentYear, quarter,
				deductorTan);
		List<TCSFilingFilesDTO> filingFilesList = new ArrayList<TCSFilingFilesDTO>();
		for (TCSFilingFiles filingFile : filingFiles) {
			TCSFilingFilesDTO filingFilesDTO = new TCSFilingFilesDTO();

			filingFilesDTO.setId(filingFile.getId());

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
		Collections.sort(filingFilesList, new Comparator<TCSFilingFilesDTO>() {
			@Override
			public int compare(TCSFilingFilesDTO filingFile1, TCSFilingFilesDTO filingFile2) {
				// for comparison
				if (filingFile2.getGeneratedDate() != null && filingFile1.getGeneratedDate() != null
						&& StringUtils.isNotBlank(filingFile1.getFormType())
						&& StringUtils.isNotBlank(filingFile2.getFormType())) {
					return Comparator.comparing(TCSFilingFilesDTO::getFormType)
							.thenComparing(TCSFilingFilesDTO::getGeneratedDate).compare(filingFile1, filingFile2);
				} else {
					return 0;
				}

			}
		});
		return filingFilesList;
	}

	public TCSFilingStatus findTCSFilingStatusByYearAndQuarterAndFileType(String tenantId, int assessmentYear,
			String quarter, String collectorTan) {
		List<TCSFilingStatus> filingStatusList = tCSFilingStatusDAO
				.findTCSFilingStatusByYearAndQuarterAndTan(assessmentYear, quarter, collectorTan);
		TCSFilingStatus filingStatus = new TCSFilingStatus();
		if (!filingStatusList.isEmpty()) {
			filingStatus = filingStatusList.get(0);
		}
		return filingStatus;
	}

	public List<TCSFilingFilesDTO> tcsFindFilingByYearDeductorIdQuarter(Integer assessmentYear, String deductorPan,
			String quarter, String deductorTan, String formType) {
		List<TCSFilingFiles> filingFiles = tCSFilingDAO.findByYearTanQuarterFileType(assessmentYear, deductorTan,
				quarter, formType);
		List<TCSFilingFilesDTO> filingFilesList = new ArrayList<TCSFilingFilesDTO>();
		for (TCSFilingFiles filingFile : filingFiles) {
			TCSFilingFilesDTO filingFilesDTO = new TCSFilingFilesDTO();

			filingFilesDTO.setId(filingFile.getId());

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
		Collections.sort(filingFilesList, new Comparator<TCSFilingFilesDTO>() {
			@Override
			public int compare(TCSFilingFilesDTO filingFile1, TCSFilingFilesDTO filingFile2) {
				// for comparison
				if (filingFile2.getGeneratedDate() != null && filingFile1.getGeneratedDate() != null
						&& StringUtils.isNotBlank(filingFile1.getFormType())
						&& StringUtils.isNotBlank(filingFile2.getFormType())) {
					return Comparator.comparing(TCSFilingFilesDTO::getFormType)
							.thenComparing(TCSFilingFilesDTO::getGeneratedDate).compare(filingFile1, filingFile2);
				} else {
					return 0;
				}

			}
		});
		return filingFilesList;
	}

	@Async
	public String asyncCreateReturnFilingReportTCS(String formType, String tanNumber, String deductorPan,
			int assessmentYear, String quarter, String tenantId, String userName, Boolean isAggriated)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		MultiTenantContext.setTenantId(tenantId);
		createReturnFilingReportTCS(formType, tanNumber, deductorPan, assessmentYear, quarter, tenantId, userName,
				isAggriated);
		return "Filing record created Successfully";
	}

	@Transactional
	public String createReturnFilingReportTCS(String formType, String tanNumber, String deductorPan, int assessmentYear,
			String quarter, String tenantId, String userName, Boolean isAggriated)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Filing: Started {} generation for TAN: {}, year: {}, quarter: {}", formType, tanNumber,
				assessmentYear, quarter);
		// Parent Bean
		TCSFilingFileBean filingFileBean = new TCSFilingFileBean();
		TCSFilingBatchHeaderBean filingBatchHeader = new TCSFilingBatchHeaderBean();
		// getting data for challan
		List<TCSFilingChallanDetailBean> filingChallanDetails = generateChallanDetailTCS(assessmentYear, quarter,
				tenantId, tanNumber, filingBatchHeader);

		// getting data for deductee set to bean
		// Change the Logic to get the Data By Receipt Serial No and BSR Code So that
		// Exact mapping will be done.
		generateDeducteeDetailTCS(tanNumber, tenantId, assessmentYear, quarter, filingChallanDetails,
				"27EQ".equalsIgnoreCase(formType), isAggriated);

		// getting the deductor details
		generateBatchHeader(assessmentYear, filingChallanDetails.size(), tenantId, tanNumber, formType, quarter,
				deductorPan, filingBatchHeader);
		// setting the child beans
		filingFileBean.setBatchHeaderBean(filingBatchHeader);
		filingFileBean.setChallanDetailBeanList(filingChallanDetails);

		// generate the text
		String textUrl = generateTextFile(filingFileBean, tenantId, formType);
		filingLogic(formType, quarter, assessmentYear, tanNumber, userName, textUrl, formType);

		saveInFilingStatus(assessmentYear, quarter, deductorPan, tanNumber, ReturnType.REGULAR.name(), tenantId,
				userName, formType);

		return "Filing record created Successfully";
	}

	/**
	 * to set chalan details for the 27EQ file
	 * 
	 * @param assessmentYear
	 * @param quarter
	 * @param tenantId
	 * @param tanNumber
	 * @param filingBatchHeader
	 * @return
	 */
	public List<TCSFilingChallanDetailBean> generateChallanDetailTCS(int assessmentYear, String quarter,
			String tenantId, String tanNumber, TCSFilingBatchHeaderBean filingBatchHeader) {
		MultiTenantContext.setTenantId(tenantId);
		double grossTdsTotalAsPerChallan = 0;
		logger.info("Filing: Challan detail generation for TAN: {}, year: {}, quarter: {}", tanNumber, assessmentYear,
				quarter);
		ResponseEntity<ApiStatus<List<FilingSectionCode>>> sectionCodeResponse = mastersClient
				.getAllSectionCode(tenantId);
		List<FilingSectionCode> sectionCodes = sectionCodeResponse.getBody().getData();
		logger.info("Feign Call succeded to get the  filing section code data");
		Map<String, String> sectionCodeMap = new TreeMap<>();
		for (FilingSectionCode filingSectionCode : sectionCodes) {
			sectionCodeMap.put(filingSectionCode.getSectionName(), filingSectionCode.getSectionCode());
		}

		List<TCSFilingChallanDetailBean> filingChallanDetails = new ArrayList<>();
		int lineNo = 3;

		// TODO feign call to get challan
		List<TcsReceipt> listReciept = challansClient.getRecieptByTanYearAndQuarterTCS(tanNumber,
				CommonUtil.QUARTER_MONTHS_MAP.get(quarter), assessmentYear, tenantId).getBody().getData();
		if (listReciept.isEmpty()) {
			logger.info(String.format("There are no receipts exists for Quarter %s", quarter));
			throw new CustomException(String.format("There are no receipts exists for Quarter %s", quarter));
		}

		logger.info("Receipts are retrieved successfully {}");
		for (TcsReceipt receiptData : listReciept) {
			TCSFilingChallanDetailBean filingChallanDetailBean = new TCSFilingChallanDetailBean(
					receiptData.getChallanMonth(), "");
			grossTdsTotalAsPerChallan = grossTdsTotalAsPerChallan
					+ receiptData.getChallanAmountTotal().setScale(2, RoundingMode.UP).doubleValue();
			// 1 Line Number
			filingChallanDetailBean.setLineNo(Long.valueOf(lineNo++).toString());
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

			// 14 DDO serial number of Form No. 24G
			filingChallanDetailBean.setDdoSerialNumberForm24G(StringUtils.EMPTY);
			// Last Bank-Branch Code/ Form 24G Receipt Number ( Used for Verification) (Not
			// applicable)
			filingChallanDetailBean.setLastBankBranchCode(StringUtils.EMPTY);

			if (receiptData != null && receiptData.getId() != null) {
				filingChallanDetailBean
						.setDateOfBankChallanNo(receiptData.getDate().replaceAll("/", StringUtils.EMPTY));
				if (filingChallanDetailBean.getNillChallanIndicator().equals("Y")) {
					// 11 Last Bank Challan No ( Used for Verification) (Not applicable)
					filingChallanDetailBean.setLastBankChallanNo(StringUtils.EMPTY);
					// 12 Bank Challan No
					filingChallanDetailBean.setBankChallanNo(StringUtils.EMPTY);
					// 13 Last Transfer Voucher No ( Used for Verification) (Not applicable)
					filingChallanDetailBean.setLastTransferVoucherNo(StringUtils.EMPTY);
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

					if (receiptData != null) {
						// 15 Last Bank-Branch Code/ Form 24G Receipt Number ( Used for Verification)
						filingChallanDetailBean.setBankBranchCode(receiptData.getBsrCode());
					}
				}
			} else {
				throw new CustomException("No Receipt data exists to generate file for quarter : " + quarter);
			}
			// 17 Last Date of 'Bank Challan No / Transfer Voucher No' ( Used for
			// Verification) (Not applicable)
			filingChallanDetailBean.setLastDateOfBankChallanNo(StringUtils.EMPTY);
			String receiptDate = receiptData.getDate();
			if (StringUtils.isNotBlank(receiptDate)) {
				receiptDate = receiptDate.replaceAll("/", StringUtils.EMPTY).replaceAll("-", StringUtils.EMPTY);
				logger.info("Receipt date :" + receiptDate);
				if (receiptDate.length() == 6) {
					receiptDate = receiptDate.substring(0, 4)
							+ (StringUtils.EMPTY + Calendar.getInstance().get(Calendar.YEAR)).substring(0, 2)
							+ receiptDate.substring(4, 6);
				}
				logger.info("Receipt date :" + receiptDate);
				// 18 Date of 'Bank Challan / Transfer Voucher'
				// adding Zero at the intial point
				if (receiptDate.length() == 7) {
					receiptDate = "0" + receiptDate;
				}
				filingChallanDetailBean.setDateOfBankChallanNo(receiptDate);
			}

			// 19 Filler 6
			filingChallanDetailBean.setFiller6(StringUtils.EMPTY);
			// 20 Filler 7
			filingChallanDetailBean.setFiller7(StringUtils.EMPTY);
			// 21 Section / Collection Code
			filingChallanDetailBean.setSection(StringUtils.EMPTY);
			filingChallanDetailBean.setChallanSection(receiptData.getSection());

			// 22 'Oltas TDS / TCS -Income Tax '
			filingChallanDetailBean
					.setOltasTCSIncomeTax(getNullSafeRoundingUpAmount(receiptData.getChallanAmountTotal()));

			logger.info("Oltas tax :" + filingChallanDetailBean.getOltasTCSIncomeTax());
			// 23 'Oltas TDS / TCS -Surcharge '
			filingChallanDetailBean
					.setOltasTCSSurcharge(getNullSafeRoundingUpAmount(receiptData.getChallanAmountSurcharge()));
			// 24 'Oltas TDS / TCS - Cess'
			filingChallanDetailBean
					.setOltasTCSCess(getNullSafeRoundingUpAmount(receiptData.getChallanAmountEducationCess()));
			// 25 Oltas TDS / TCS - Interest Amount
			filingChallanDetailBean
					.setOltasTCSInterest(getNullSafeRoundingUpAmount(receiptData.getChallanAmountInterest()));
			// 26 Oltas TDS / TCS - Others (amount)
			filingChallanDetailBean
					.setOltasTCSOthers(getNullSafeRoundingUpAmount(receiptData.getChallanAmountOthers()));
			// 27 Total of Deposit Amount as per Challan/Transfer Voucher Number ( 'Oltas
			// TDS/ TCS -Income Tax ' + 'Oltas TDS/ TCS -Surcharge ' + 'Oltas TDS/ TCS -
			// Cess' + Oltas TDS/ TCS - Interest Amount + Fee + Oltas TDS/ TCS - Others
			// (amount) )
			if (receiptData.getChallanAmountTotal() != null) {
				logger.info(receiptData.getChallanAmountTotal() + "");
				filingChallanDetailBean.setTotalOfDepositAmountAsPerChallan(
						String.format("%.2f", receiptData.getChallanAmountTotal().setScale(2, RoundingMode.UP)));
			} else {
				filingChallanDetailBean.setTotalOfDepositAmountAsPerChallan("0.00");
			}
			logger.info("Deposit Amount per challan :" + filingChallanDetailBean.getTotalOfDepositAmountAsPerChallan());
			// 28 Last Total of Deposit Amount as per Challan
			filingChallanDetailBean.setLastTotalOfDepositAmountAsPerChallan(StringUtils.EMPTY);
			// 29 Total Tax Deposit Amount as per party annexure
			filingChallanDetailBean.setTotalTaxDepositedAsPerCollecteeAnex(
					getNullSafeRoundingUpAmount(receiptData.getChallanAmountTotal()));

			logger.info("Total tax deposited Per Deductee Annex :"
					+ filingChallanDetailBean.getTotalTaxDepositedAsPerCollecteeAnex());

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

		filingBatchHeader
				.setGrossTdsTotalAsPerChallan(String.format("%.2f", Double.valueOf(grossTdsTotalAsPerChallan)));

		// Sort the challans here
		Collections.sort(filingChallanDetails, new Comparator<TCSFilingChallanDetailBean>() {
			@Override
			public int compare(TCSFilingChallanDetailBean filingChallan1, TCSFilingChallanDetailBean filingChallan2) {
				// for comparisonfilingChallan1
				int sectionCompare = 0;
				if (StringUtils.isNotBlank(filingChallan1.getChallanSection())) {
					sectionCompare = filingChallan1.getChallanSection().compareTo(filingChallan2.getChallanSection());

				}
				int monthCompare = filingChallan1.getChallanMonth() - filingChallan2.getChallanMonth();

				// 2-level comparisonsss
				if (sectionCompare == 0) {
					return ((monthCompare == 0) ? sectionCompare : monthCompare);
				} else {
					return sectionCompare;
				}
			}
		});
		for (TCSFilingChallanDetailBean filingChallanDetail : filingChallanDetails) {
			// 4 Challan-Detail Record Number
			filingChallanDetail.setChallanDetailRecordNo(
					StringUtils.EMPTY + (filingChallanDetails.indexOf(filingChallanDetail) + 1));
		}
		logger.info("Filing: Challan detail generation for TAN: {}, year: {}, quarter: {} done", tanNumber,
				assessmentYear, quarter);
		return filingChallanDetails;
	}

	public void generateDeducteeDetailTCS(String tanNumber, String tenantId, int assessmentYear, String quarter,
			List<TCSFilingChallanDetailBean> filingChallanDetails, boolean isForNonResidents, Boolean isAggriated) {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Filing: Deductee detail generation for TAN: {}, year: {}, quarter: {}", tanNumber, assessmentYear,
				quarter);
		List<TCSFilingDeducteeDetailBean> deducteeDetailBeanList = new ArrayList<>();
		final AtomicInteger rowIndexHolder = new AtomicInteger(2 + filingChallanDetails.size() + 1);
		ResponseEntity<ApiStatus<List<FilingSectionCode>>> sectionCodeResponse = mastersClient
				.getAllSectionCode(tenantId);
		List<FilingSectionCode> sectionCodes = sectionCodeResponse.getBody().getData();
		Map<String, String> sectionCodeMap = new TreeMap<>();
		for (FilingSectionCode filingSectionCode : sectionCodes) {
			sectionCodeMap.put(filingSectionCode.getSectionName(), filingSectionCode.getSectionCode());
		}

		// section code determination
		Map<String, List<String>> sectionMap = new TreeMap<>();
		List<NatureOfPaymentMasterDTO> response = mastersClient.tcsFindAll().getBody().getData();
		for (NatureOfPaymentMasterDTO filingSectionCode : response) {
			sectionMap.computeIfAbsent(filingSectionCode.getSection(), k -> new ArrayList<>())
					.add(filingSectionCode.getNature());
		}

		for (TCSFilingChallanDetailBean filingChallanDetailBean : filingChallanDetails) {

			List<CollecteeDetailsDTO> invoicesByReceipt = getLineItemsByReceipt(tanNumber, tenantId, assessmentYear,
					isForNonResidents, filingChallanDetailBean.getBankBranchCode(),
					filingChallanDetailBean.getBankChallanNo(), filingChallanDetailBean.getDateOfBankChallanNo(),
					quarter, filingChallanDetailBean.getChallanSection());

			// grouping based on name, posting date, deductee pan,section
			if (invoicesByReceipt != null && isAggriated != null && isAggriated == true) {
				invoicesByReceipt = getAggrigatedList(invoicesByReceipt);
			}

			for (CollecteeDetailsDTO invoice : invoicesByReceipt) {
				TCSFilingDeducteeDetailBean deducteeDetail = new TCSFilingDeducteeDetailBean("27EQ");

				String lccCertificateNo = StringUtils.isNotBlank(invoice.getLccCertificateNumber())
						? invoice.getLccCertificateNumber()
						: StringUtils.EMPTY;

				String challanDetailRecordNo = StringUtils.EMPTY;
				filingChallanDetailBean.getDeducteeDetailBeanList().add(deducteeDetail);
				challanDetailRecordNo = filingChallanDetailBean.getChallanDetailRecordNo();

				// 1 Line Number
				deducteeDetail.setLineNo(StringUtils.EMPTY + rowIndexHolder.getAndIncrement());
				// 2 Record Type (Hardcoded in bean)
				deducteeDetail.setRecType("DD");
				// 3 Batch Number
				deducteeDetail.setDdBatchNo("1");
				// 4 Challan-Detail Record Number
				deducteeDetail.setChallanRecordNo(challanDetailRecordNo);
				// 5 Deductee / Party Detail Record No
				deducteeDetail.setCollecteeDetailRecNo(StringUtils.EMPTY);
				// 6 Mode (Hardcoded in bean)
				deducteeDetail.setMode("O");
				// 7 Deductee Serial No (Not applicable)
				deducteeDetail.setDeducteeSerialNo(StringUtils.EMPTY);
				// 8 collectee code

				if (invoice.getCollecteeStatus().equalsIgnoreCase("Company")) {
					deducteeDetail.setCollecteeCode("1");
				} else {
					deducteeDetail.setCollecteeCode("2");
				}
				// 9 Last Deductee PAN ( Used for Verification) (Not applicable)
				deducteeDetail.setLastCollecteePan(StringUtils.EMPTY);
				// 10 PAN of the deductee
				if (StringUtils.isBlank(invoice.getPan())) {
					deducteeDetail.setCollecteePan("PANNOTAVBL");
				} else {
					deducteeDetail.setCollecteePan(invoice.getPan());
				}
				// 11 Last Deductee PAN Ref. No. (Not applicable)
				deducteeDetail.setLastCollecteeRefNo(StringUtils.EMPTY);
				// 12 Deductee Ref. No.

				if ("PANNOTAVBL".equals(deducteeDetail.getCollecteePan())) {
					long number = (long) Math.floor(Math.random() * 9000000000L) + 1000000000L;
					deducteeDetail.setCollecteeRefNo(Long.valueOf(number).toString());
				} else {
					deducteeDetail.setCollecteeRefNo(StringUtils.EMPTY);
				}

				// 13 Name of the Deductee
				deducteeDetail.setCollecteeName(invoice.getCollecteeName());

				BigDecimal finalTDSAmount = invoice.getFinalTcSAmount();
				finalTDSAmount = finalTDSAmount.setScale(2, RoundingMode.UP);

				// 14 TDS -Income Tax for the period
				deducteeDetail.setTcsIncomeTaxDD(String.format("%.2f", finalTDSAmount));

				// 15 TDS -Surcharge for the period
				deducteeDetail.setTcsSurchargeDD("0.00");

				// 16 TDS-Cess
				deducteeDetail.setTcsCessDD("0.00");

				// 17 Total Income Tax Deducted at Source (TDS / TCS Income Tax+ TDS / TCS
				// Surcharge + TDS/TCS Cess)
				deducteeDetail.setTotalIncomeTaxCollected(String.format("%.2f", finalTDSAmount));

				// 18 Last Total Income Tax Deducted at Source (Income Tax+Surcharge+Cess) (Used
				// for Verification) (Not applicable)
				deducteeDetail.setLastTotalIncomeTaxCollectedAtSource(StringUtils.EMPTY);

				// 19 Total Tax Deposited
				deducteeDetail.setTotalTaxDeposited(String.format("%.2f", finalTDSAmount));

				// 20 Last Total Tax Deposited ( Used for Verification) (Not applicable)
				deducteeDetail.setLastTotalTaxDeposited(StringUtils.EMPTY);
				// 21 Total Value of Purchase (Not applicable)
				deducteeDetail.setTotalValueofTransaction(String.format("%.2f", invoice.getTotalTaxableValue()));
				// 22 Amount of Payment / Credit ( Rs.)
				deducteeDetail.setAmountReceived(String.format("%.2f", invoice.getAmount()));
				// 23 Date on which Amount paid / Credited / Debited
				int year = assessmentYear;
				if (!quarter.equals("Q4")) {
					year = assessmentYear - 1;
				}
				String amountPaid = QUARTER_LAST_DAY.get(quarter) + year;

				SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
				formatter.setTimeZone(TimeZone.getTimeZone("IST"));

				if (invoice.getDocumentPostingDate() == null) {
					deducteeDetail.setDateOnWhichAmountReceived(StringUtils.EMPTY);
					// 24 Date on which tax Deducted
					deducteeDetail.setDateOnWhichTaxCollected(StringUtils.EMPTY);
				} else {
					String documentPostingDate = formatter.format(invoice.getDocumentPostingDate());
					deducteeDetail.setDateOnWhichAmountReceived(documentPostingDate);
					// 24 Date on which tax Deducted
					deducteeDetail.setDateOnWhichTaxCollected(documentPostingDate);
				}

				// 25 Date of Deposit
				deducteeDetail.setDateOfDeposit(StringUtils.EMPTY);
				// 26 Rate at which Tax Deducted
				deducteeDetail.setRateAtWhichTaxCollected(String.format("%.4f", invoice.getFinalTcSRate()));
				// 27 Grossing up Indicator
				deducteeDetail.setGrossingUpIndicator(StringUtils.EMPTY);
				// 28 Book Entry / Cash Indicator
				deducteeDetail.setBookCashEntry(StringUtils.EMPTY);
				// 29 Date of furnishing Tax Deduction Certificate (Not applicable)
				deducteeDetail.setDateOfFurnishingTaxDeductionCertificate(StringUtils.EMPTY);

				// logic to get the code for reason of non collection
				String codeForRemarks = StringUtils.EMPTY;
				if (invoice.getFinalTcSSection() != null) {
					if (StringUtils.isEmpty(invoice.getPan())) {
						codeForRemarks = "C";
					}
					if (invoice.getIsExempted() == 1) {
						codeForRemarks = "B";
					}
					if (invoice.getNoCollectionDeclarationAsPerForM27c() == 1) {
						codeForRemarks = "B";
					}
					if (invoice.getHasLcc() == true) {
						codeForRemarks = "A";
					}
				}
				// 30 Remarks 1 (Reason for non-deduction / lower deduction/ grossing up/ higher
				// deduction)
				deducteeDetail.setReason(codeForRemarks);

				// 31 Deductee is Non-Resident
				if (StringUtils.isBlank(invoice.getPan())) {
					if (invoice.getCess() != null && invoice.getSurcharge() != null) {
						deducteeDetail.setIsCollecteeNonResident("Y");
					} else {
						deducteeDetail.setIsCollecteeNonResident("N");
					}
				} else {
					deducteeDetail.setIsCollecteeNonResident(StringUtils.EMPTY);
				}
				// 32 Deductee is having Permanent Establishment in India
				deducteeDetail.setIsCollecteeHavingPermanentResidentInIndia(StringUtils.EMPTY);
				// 33 Collection code

				// Code refactoring needs to be done
				deducteeDetail.setCollectionCode(sectionCodeDetermination(invoice));

				// 34 Number of the certificate u/s 206C issued by the Assessing officer for
				// lower collection of tax
				// Certificate Number
				if (StringUtils.isBlank(lccCertificateNo)) {
					deducteeDetail.setNoOfCertificateIssued(StringUtils.EMPTY);
				} else {
					deducteeDetail.setNoOfCertificateIssued(lccCertificateNo);
				}
				// 35
				deducteeDetail.setIsPaymentByCollecteeLiable(StringUtils.EMPTY);
				// 36
				deducteeDetail.setChallanNumber(StringUtils.EMPTY);
				//
				deducteeDetail.setFilter1(StringUtils.EMPTY);
				deducteeDetail.setFilter2(StringUtils.EMPTY);
				deducteeDetail.setFilter3(StringUtils.EMPTY);
				deducteeDetail.setFilter4(StringUtils.EMPTY);
				deducteeDetail.setFilter5(StringUtils.EMPTY);
				deducteeDetail.setFilter6(StringUtils.EMPTY);
				deducteeDetail.setFilter7(StringUtils.EMPTY);
				deducteeDetail.setFilter8(StringUtils.EMPTY);
				deducteeDetail.setFilter9(StringUtils.EMPTY);
				deducteeDetail.setFilter10(StringUtils.EMPTY);
				deducteeDetail.setFilter11(StringUtils.EMPTY);
				deducteeDetailBeanList.add(deducteeDetail);

			}
			// }
		}
		// 29 - 33 field calculation done here for CD
		for (TCSFilingChallanDetailBean filingChallanDetailBean : filingChallanDetails) {
			double totalTaxDepositedAsPerDeducteeAnex = 0;
			double tdsIncomeTaxC = 0;
			double tdsSurchargeC = 0;
			double tdsCessC = 0;
			double sumTotalIncTaxDedAtSource = 0;
			int deducteeDetailRecNo = 1;
			for (TCSFilingDeducteeDetailBean filingDeducteeDetailBean : filingChallanDetailBean
					.getDeducteeDetailBeanList()) {
				// 14
				tdsIncomeTaxC = tdsIncomeTaxC
						+ Double.valueOf(filingDeducteeDetailBean.getTcsIncomeTaxDD()).doubleValue();
				// 15
				tdsSurchargeC = tdsSurchargeC
						+ Double.valueOf(filingDeducteeDetailBean.getTcsSurchargeDD()).doubleValue();
				// 16
				tdsCessC = tdsCessC + Double.valueOf(filingDeducteeDetailBean.getTcsCessDD()).doubleValue();
				// 17
				sumTotalIncTaxDedAtSource = sumTotalIncTaxDedAtSource
						+ Double.valueOf(filingDeducteeDetailBean.getTcsIncomeTaxDD()).doubleValue();
				// 19
				totalTaxDepositedAsPerDeducteeAnex = totalTaxDepositedAsPerDeducteeAnex
						+ Double.valueOf(filingDeducteeDetailBean.getTotalTaxDeposited()).doubleValue();
				filingDeducteeDetailBean.setCollecteeDetailRecNo(StringUtils.EMPTY + deducteeDetailRecNo++);
			}
			// 29
			filingChallanDetailBean.setTotalTaxDepositedAsPerCollecteeAnex(
					CommonUtil.df2.format(Double.valueOf(totalTaxDepositedAsPerDeducteeAnex)));
			// 30
			filingChallanDetailBean.setTcsIncomeTaxC(CommonUtil.df2.format(Double.valueOf(tdsIncomeTaxC)));
			// 31
			filingChallanDetailBean.setTcsSurchargeC(CommonUtil.df2.format(Double.valueOf(tdsSurchargeC)));
			// 32
			filingChallanDetailBean.setTcsCessC(CommonUtil.df2.format(Double.valueOf(tdsCessC)));
			// 33
			filingChallanDetailBean.setSumTotalIncTaxDeductedAtSource(
					CommonUtil.df2.format(Double.valueOf(sumTotalIncTaxDedAtSource)));

			filingChallanDetailBean.setCountOfCollecteeDetail(
					StringUtils.EMPTY + filingChallanDetailBean.getDeducteeDetailBeanList().size());

		}
		logger.info("Filing: Deductee detail generation for TAN: {}, year: {}, quarter: {}", tanNumber, assessmentYear,
				quarter);
	}

	private String sectionCodeDetermination(CollecteeDetailsDTO invoice) {
		String sectionCode = StringUtils.EMPTY;
		if (invoice.getNoiId() != null) {
			NatureOfPaymentMasterDTO noi = mastersClient.getNatureOfIncomeMasterRecord(invoice.getNoiId()).getBody()
					.getData();
			if (invoice.getFinalTcSSection().equalsIgnoreCase("206C(1)")
					&& noi.getNature().equalsIgnoreCase("Sale of alcoholic liquor for human consumption")) {
				sectionCode = "A";
			} else if (invoice.getFinalTcSSection().equalsIgnoreCase("206C(1)") && noi.getNature()
					.equalsIgnoreCase("Sale of scrap and minerals, being coal or lignite or iron ore")) {
				sectionCode = "E"; // J may come
			} else if (invoice.getFinalTcSSection().equalsIgnoreCase("206C(1)")
					&& noi.getNature().equalsIgnoreCase("Sale of tendu leaves")) {
				sectionCode = "I";
			} else if (invoice.getFinalTcSSection().equalsIgnoreCase("206C(1)") && noi.getNature().equalsIgnoreCase(
					"Sale of timber obtained under a forest lease or any other mode, any other forest produce apart from timber and forest lease")) {
				sectionCode = "B"; // C and D may come
			} else if (invoice.getFinalTcSSection().equalsIgnoreCase("206C(1C)") && noi.getNature().equalsIgnoreCase(
					"Lease/licence/enters a contract or otherwise transfers any right or interest of Parking Lot, Toll Plaza, Mining and Quarrying")) {
				sectionCode = "F"; // G and H may come
			} else if (invoice.getFinalTcSSection().equalsIgnoreCase("206C(1F)")
					&& noi.getNature().equalsIgnoreCase("Sale of Motor vehicle")) {
				sectionCode = "L";
			} else if (invoice.getFinalTcSSection().equalsIgnoreCase("206C(1G)") && noi.getNature().equalsIgnoreCase(
					"Transactions by an authorised dealer who receives an amount for remittance out of India and seller of an overseas tour program package - Remittance out of India through Liberalised Remittance Scheme of RBI")) {
				sectionCode = "Q";
			} else if (invoice.getFinalTcSSection().equalsIgnoreCase("206C(1G)") && noi.getNature().equalsIgnoreCase(
					"Transactions by an authorised dealer who receives an amount for remittance out of India and seller of an overseas tour program package - Remittance out of India through Liberalised Remittance Scheme of RBI for sale of tour package")) {
				sectionCode = "O";
			} else if (invoice.getFinalTcSSection().equalsIgnoreCase("206C(1G)") && noi.getNature().equalsIgnoreCase(
					"Transactions by an authorised dealer who receives an amount for remittance out of India and seller of an overseas tour program package - Seller of an overseas tour program package, who receives any amount from a buyer, being the person who purchases such package")) {
				sectionCode = "O";
			} else if (invoice.getFinalTcSSection().equalsIgnoreCase("206C(1G)") && noi.getNature().equalsIgnoreCase(
					"Transactions by an authorised dealer who receives any amount for remittance through Liberalised Remittance Scheme of RBI for the purpose of an education loan")) {
				sectionCode = "P";
			} else if (invoice.getFinalTcSSection().equalsIgnoreCase("206C(1H)")
					&& noi.getNature().equalsIgnoreCase("Sale of goods")) {
				sectionCode = "R";
			}
		}
		return sectionCode;
	}

	private List<CollecteeDetailsDTO> getLineItemsByReceipt(String tanNumber, String tenantId, int assessmentYear,
			boolean isForNonResidents, String bsrCode, String challanSerialNo, String reciptDate, String quarter,
			String section) {

		List<CollecteeDetailsDTO> receiptInvoices = new ArrayList<CollecteeDetailsDTO>();
		List<Integer> monthsByQuarter = Arrays.stream(QUARTER_MONTHS_MAP.get(quarter)).boxed()
				.collect(Collectors.toList());
		boolean residentIndicator = isForNonResidents ? true : false;
		ResponseEntity<ApiStatus<List<TCSLedger>>> invoices = ingestionClient.invoiceByTanYearBSRCodeSerialNoAndDateTCS(
				assessmentYear, tanNumber, true, residentIndicator, bsrCode, challanSerialNo, reciptDate, tenantId,
				monthsByQuarter, section);
		List<TCSLedger> invoiceLienItemData = invoices.getBody().getData();

		for (TCSLedger lineItem : invoiceLienItemData) {
			CollecteeDetailsDTO collecteeMasterDTO = new CollecteeDetailsDTO();
			collecteeMasterDTO.setAmount(lineItem.getApplicableTotalTaxableAmount());
			collecteeMasterDTO.setCollecteeName(lineItem.getCollecteeName());
			collecteeMasterDTO.setCollecteeCode(lineItem.getCollecteeCode());
			collecteeMasterDTO.setCollecteeStatus(lineItem.getCollecteeStatus());
			collecteeMasterDTO.setPan(lineItem.getCollecteePan());
			collecteeMasterDTO.setFinalTcSAmount(lineItem.getFinalTcsAmount());
			collecteeMasterDTO.setFinalTcSRate(lineItem.getFinalTcsRate());
			collecteeMasterDTO.setFinalTcSSection(lineItem.getFinalTcsSection());
			collecteeMasterDTO.setDocumentPostingDate(lineItem.getPostingDate());
			collecteeMasterDTO.setIsExempted(lineItem.getIsExempted());
			collecteeMasterDTO.setNoCollectionDeclarationAsPerForM27c(lineItem.getIsParent());
			collecteeMasterDTO.setHasLcc(lineItem.getHasLcc());
			collecteeMasterDTO.setCess(lineItem.getItcessAmount());
			collecteeMasterDTO.setSurcharge(lineItem.getSurchargeAmount());
			collecteeMasterDTO.setNoiId(Long.valueOf(lineItem.getNoiId()));
			collecteeMasterDTO
					.setTotalTaxableValue(lineItem.getApplicableTotalTaxableAmount().add(lineItem.getAmount()));

			if (lineItem.getHasLcc()) {
				List<TCSLccMaster> ldcMasterList = lccMasterDAO.getLccByTanDeducteePanSection(
						lineItem.getCollecteePan(), tanNumber, lineItem.getFinalTcsSection());
				if (!ldcMasterList.isEmpty()) {
					collecteeMasterDTO.setLccCertificateNumber(ldcMasterList.get(0).getCertificateNumber());
				}
			}
			receiptInvoices.add(collecteeMasterDTO);
		}

		List<TcsPaymentDTO> advanceLineItemData = ingestionClient
				.getAdvacnceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDateTCS(assessmentYear, tanNumber, true,
						residentIndicator, bsrCode, challanSerialNo, reciptDate, tenantId, monthsByQuarter, section)
				.getBody().getData();
		for (TcsPaymentDTO lineItem : advanceLineItemData) {
			CollecteeDetailsDTO deducteeDetailDTO = new CollecteeDetailsDTO();
			if (lineItem.getAmount() == null) {
				lineItem.setAmount(BigDecimal.ZERO);
			}
			if (lineItem.getConsumedAmount() == null) {
				lineItem.setConsumedAmount(BigDecimal.ZERO);
			}
			deducteeDetailDTO.setAmount(lineItem.getAmount().subtract(lineItem.getConsumedAmount()));
			deducteeDetailDTO.setCollecteeName(lineItem.getCollecteeName());
			deducteeDetailDTO.setCollecteeCode(lineItem.getCollecteeCode());
			// while retrieving collectee status the value is stored in gl desc
			deducteeDetailDTO.setCollecteeStatus(lineItem.getGlDesc());
			deducteeDetailDTO.setPan(lineItem.getCollecteePan());
			deducteeDetailDTO.setFinalTcSAmount(lineItem.getFinalTcsAmount());
			deducteeDetailDTO.setFinalTcSRate(lineItem.getFinalTcsRate());
			deducteeDetailDTO.setFinalTcSSection(lineItem.getFinalTcsSection());
			deducteeDetailDTO.setDocumentPostingDate(lineItem.getDocumentDate());
			deducteeDetailDTO.setIsExempted(lineItem.getIsExempted() == true ? 1 : 0);
			// isparent column is having data from collectee master
			deducteeDetailDTO.setNoCollectionDeclarationAsPerForM27c(lineItem.getIsParent() == true ? 1 : 0);
			deducteeDetailDTO.setHasLcc(lineItem.getHasLcc());
			deducteeDetailDTO.setCess(lineItem.getItcessAmount());
			deducteeDetailDTO.setSurcharge(lineItem.getSurchargeAmount());
			deducteeDetailDTO.setNoiId(lineItem.getNoiId());
			deducteeDetailDTO.setTotalTaxableValue(lineItem.getAmount());

			if (lineItem.getHasLcc()) {
				List<TCSLccMaster> ldcMasterList = lccMasterDAO.getLccByTanDeducteePanSection(
						lineItem.getCollecteePan(), tanNumber, lineItem.getFinalTcsSection());
				if (!ldcMasterList.isEmpty()) {
					deducteeDetailDTO.setLccCertificateNumber(ldcMasterList.get(0).getCertificateNumber());
				}
			}
			receiptInvoices.add(deducteeDetailDTO);
		}

		logger.info("response size : {}", receiptInvoices.size());
		return receiptInvoices;
	}

	private List<CollecteeDetailsDTO> getAggrigatedList(List<CollecteeDetailsDTO> list) {
		Map<String, CollecteeDetailsDTO> mapDto = new HashMap<String, CollecteeDetailsDTO>();
		List<CollecteeDetailsDTO> extractedList = new ArrayList<CollecteeDetailsDTO>();
		for (CollecteeDetailsDTO dto : list) {
			String unique = dto.getCollecteeCode() + "" + dto.getFinalTcSSection() + "".toLowerCase();
			if (mapDto.containsKey(unique)) {
				BigDecimal finalTdsAmount = mapDto.get(unique).getFinalTcSAmount().add(dto.getFinalTcSAmount());
				BigDecimal amount = mapDto.get(unique).getAmount().add(dto.getAmount());
				mapDto.get(unique).setFinalTcSAmount(finalTdsAmount);
				mapDto.get(unique).setAmount(amount);

			} else {
				mapDto.put(unique, dto);
			}
		}

		for (Map.Entry<String, CollecteeDetailsDTO> set : mapDto.entrySet()) {
			extractedList.add(set.getValue());
		}
		return extractedList;
	}

	public TCSFilingBatchHeaderBean generateBatchHeader(int assessmentYear, int noOfChallans, String tenantId,
			String tanNumber, String formType, String quarter, String deductorPan,
			TCSFilingBatchHeaderBean filingBatchHeader) {
		try {
			MultiTenantContext.setTenantId(tenantId);
			ResponseEntity<ApiStatus<DeductorMasterDTO>> data = onboardingClient.getDeductorByPan(tenantId,
					deductorPan);
			DeductorMasterDTO deductorData = data.getBody().getData();
			Set<TanAddressDTO> addressTan = deductorData.getTanList();

			ResponseEntity<ApiStatus<List<FilingStateCode>>> stateResponse = mastersClient.getAllStateCode(tenantId);
			List<FilingStateCode> states = stateResponse.getBody().getData();
			Map<String, String> stateMap = new TreeMap<>();
			for (FilingStateCode filingStateCode : states) {
				String stateCode = filingStateCode.getStateCode();
				if (stateCode.length() == 1) {
					stateCode = "0" + stateCode;
				}
				stateMap.put(filingStateCode.getStateName().toUpperCase(), stateCode);
			}

			Map<String, TanAddressDTO> deductorTanMap = new TreeMap<>();
			for (TanAddressDTO tanDTO : addressTan) {
				deductorTanMap.put(tanDTO.getTan(), tanDTO);
			}

			TCSFilingStatus previousQuarterFilingStatus = getPreviousQuarterFilingStatus(assessmentYear, quarter);

			// 1 Line Number - Running Sequence Number for each line in the file
			filingBatchHeader.setLineNo("2");
			// setting manually in bean
			filingBatchHeader.setRecordType("BH");
			// 3 Batch Number
			filingBatchHeader.setBatchNo("1");
			// 4 Count of Challan/transfer voucher Records
			filingBatchHeader.setChallanCount(noOfChallans + "");
			// 5
			filingBatchHeader.setFormNo("27EQ");
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
			filingBatchHeader.setLastTanOfCollector(StringUtils.EMPTY);
			// 13 TAN of Deductor - Specifies the 10 Character TAN of the deductor. Should
			// be all CAPITALS.
			filingBatchHeader.setTanOfCollector(tanNumber);
			// 14
			filingBatchHeader.setReceiptNumber(StringUtils.EMPTY);
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
			// 19 Name of Collector
			filingBatchHeader.setEmployerName(deductorData.getDeductorName());
			// 20 Deductor / Branch/ Division
			TanAddressDTO tanAddressDTO = deductorTanMap.get(tanNumber);
			if (tanAddressDTO == null || StringUtils.isBlank(tanAddressDTO.getAreaLocality())) {
				filingBatchHeader.setEmployerBranchDiv("NA");
			} else {
				filingBatchHeader.setEmployerBranchDiv(tanAddressDTO.getAreaLocality());
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
			filingBatchHeader.setCollectorType("K");
			// 33 Name of Person responsible for Deduction
			if (tanAddressDTO != null && tanAddressDTO.getPersonResponsibleDetails() != null
					&& StringUtils.isNotBlank(tanAddressDTO.getPersonResponsibleDetails().getName())) {
				filingBatchHeader.setNameofPersonResponsilbleForTaxCollection(
						tanAddressDTO.getPersonResponsibleDetails().getName());
			} else {
				filingBatchHeader.setNameofPersonResponsilbleForTaxCollection(StringUtils.EMPTY);
			}
			// 34 Designation of the Person responsible for Deduction
			if (tanAddressDTO != null && tanAddressDTO.getPersonResponsibleDetails() != null
					&& StringUtils.isNotBlank(tanAddressDTO.getPersonResponsibleDetails().getDesignation())) {
				filingBatchHeader.setDesignationofPersonResponsilbleForTaxCollection(
						tanAddressDTO.getPersonResponsibleDetails().getDesignation());
			} else {
				filingBatchHeader.setDesignationofPersonResponsilbleForTaxCollection(StringUtils.EMPTY);
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

			// get challans for the quarter
			List<ChallanReceiptDTO> challans = challansClient.getChallanDataForReturnsTCS(tenantId, assessmentYear,
					quarter, tanNumber);
			BigDecimal totalAmount = new BigDecimal(0);
			if (challans != null) {
				for (ChallanReceiptDTO challan : challans) {
					if (challan.getTotal() != null) {
						totalAmount = totalAmount.add(challan.getTotal());
					}
				}
			}
			totalAmount = totalAmount.setScale(0, RoundingMode.HALF_UP);
			// 47 Batch Total of - Total of Deposit Amount as per Challan -- Done at the end
			filingBatchHeader.setGrossTdsTotalAsPerChallan(totalAmount.toString() + ".00");
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
				filingBatchHeader.setIsRegularStatementForForm27EQFiledForEArlierPeriod("Y");
			} else {
				filingBatchHeader.setIsRegularStatementForForm27EQFiledForEArlierPeriod("N");
			}

			// 53 Last Deductor Type
			filingBatchHeader.setLastCollectorType(StringUtils.EMPTY);

			if (filingBatchHeader.getCollectorType().equals("S") || filingBatchHeader.getCollectorType().equals("E")
					|| filingBatchHeader.getCollectorType().equals("H")
					|| filingBatchHeader.getCollectorType().equals("N")) {
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
			filingBatchHeader.setaIN(StringUtils.EMPTY);
			// 69 Goods and Service Tax Number (GSTN)
			filingBatchHeader.setgSTN(StringUtils.EMPTY);
			// 70 Record Hash (Not applicable)
			filingBatchHeader.setBatchHash(StringUtils.EMPTY);

		} catch (Exception e) {
			logger.error(StringUtils.EMPTY, e);
		}

		return filingBatchHeader;

	}

	private String getNullSafeRoundingUpAmount(BigDecimal amount) {
		String roundigUpAmount = "0.0";
		if (amount != null) {
			roundigUpAmount = String.format("%.2f", amount.setScale(2, RoundingMode.UP));
		}
		return roundigUpAmount;
	}

	private TCSFilingStatus getPreviousQuarterFilingStatus(int assessmentYear, String quarter) {
		int year = assessmentYear;
		if (quarter.equals("Q1")) {
			year = assessmentYear - 1;
		}
		TCSFilingStatus previousQuarterFilingStatus = null;
		try {
			List<TCSFilingStatus> previousQuarterList = tCSFilingStatusDAO.findByYearAndQuarterAndIdTCS(year, quarter,
					1);
			if (!previousQuarterList.isEmpty()) {
				previousQuarterFilingStatus = previousQuarterList.get(0);
			}
		} catch (CustomException e) {
			logger.warn("No filing status exists for Quarter : {} - {}", assessmentYear, quarter);
		}

		return previousQuarterFilingStatus;
	}

	@Transactional
	public void filingLogic(String fileType, String quarter, Integer assessmentYear, String tanNumber, String userName,
			String textUrl, String formType)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		logger.info("Entered in filingLogic method.");
		// check if the record exists or not if exists update status column
		// check with the file record
		// of insert new record in Table1
		// FilingFiles.Key key = new FilingFiles.Key(assessmentYear, quarter);

		// storing in blob excel
		// need confirmation on storing the excel uploads
		// String fileUrl = blobStorage.uploadExcelToBlob(multipartFile);

		List<TCSFilingFiles> filingFileList = tCSFilingDAO.findByYearQuarterDeductorTanAndFormType(assessmentYear,
				quarter, tanNumber, formType);
		Date today = new Date();
		if (!filingFileList.isEmpty()) {
			TCSFilingFiles filingFile = filingFileList.get(0);
			filingFile.setActive(true);
			filingFile.setFileType(ReturnType.REGULAR.name());
			filingFile.setFormType(formType);
			filingFile.setFileType(FilingFilesType.getType(textUrl, formType));
			filingFile.setFileStatus(FilingFiles.FilingFilesStatus.getStatus(textUrl));
			filingFile.setFileUrl(textUrl);
			filingFile.setIsRequested(true);
			filingFile.setUpdatedBy(userName);
			filingFile.setUpdatedDate(new Timestamp(today.getTime()));
			filingFile.setGeneratedDate(new Timestamp(today.getTime()));
			tCSFilingDAO.updateTcsFilingFiles(filingFile);
		} else {
			TCSFilingFiles filingFiles = new TCSFilingFiles();
			filingFiles.setAssesmentYear(assessmentYear);
			filingFiles.setQuarter(quarter);
			filingFiles.setActive(true);
			filingFiles.setCollectorTan(tanNumber);
			// submitted,available ,failed,not available, not available-no deductee
			// records,not available pan not present in master

			filingFiles.setFormType(formType);
			filingFiles.setFileType(FilingFilesType.getType(textUrl, formType));
			filingFiles.setFileStatus(FilingFiles.FilingFilesStatus.getStatus(textUrl));
			filingFiles.setFilingType(ReturnType.REGULAR.name());
			filingFiles.setFileUrl(textUrl);
			// revision or current regular
			filingFiles.setIsRequested(true);
			filingFiles.setGeneratedDate(new Timestamp(today.getTime()));
			filingFiles.setCreatedDate(new Timestamp(today.getTime()));
			filingFiles.setCreatedBy(userName);
			tCSFilingDAO.save(filingFiles);
			logger.info("Filing Files Record Inserted {}");
		}

		// TODO need to impelemet below logic based on requirement
		// Clear all output files if any
		/*
		 * List<FilingFiles>
		 * outputFiles=tCSFilingDAO.findByYearQuarterDeductorTanAndFormType(
		 * assessmentYear, quarter, tanNumber, formType); outputFiles.stream()
		 * .filter(filingFile ->
		 * (filingFile.getFileType().equals(FilingFilesType.LOG.name()) ||
		 * filingFile.getFileType().equals(FilingFilesType.HTML.name()) ||
		 * filingFile.getFileType().equals(FilingFilesType.FVU.name()) ||
		 * filingFile.getFileType().endsWith(FilingFilesType.PDF.name()) ||
		 * filingFile.getFileType().equals("27A") ||
		 * filingFile.getFileType().equals("PNG") ||
		 * filingFile.getFileType().equals(FilingFilesType.IMAGE.name()) ||
		 * filingFile.getFileType().endsWith(FilingFilesType.ERR.name())))
		 * .forEach(filingFile -> {
		 * filingFileDAO.deleteById(filingFile.getFilingFilesId()); });
		 */
		logger.info("Completed in filingLogic method.");
	}

	/**
	 * This method for update tcs filing files
	 * 
	 * @param filingFilesObj
	 * @return
	 */
	public TCSFilingFiles updateFilingFiles(TCSFilingFiles filingFilesObj) {
		return tCSFilingDAO.updateTcsFilingFiles(filingFilesObj);
	}

}
