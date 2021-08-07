package com.ey.in.tds.onboarding.service.deductee;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.ey.in.tcs.common.TCSMasterDTO;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.CollecteeMasterCsvFile;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsPaymentDTO;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.NatureAndTaxRateDTO;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.model.deductee.CollecteeMasterErrorReportCsvDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeMasterDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeNamePanDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeNoiThresholdLedger;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetails;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSAdditionalSectionDTO;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ActivityTrackerStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.jdbc.dao.CollecteeMasterDAO;
import com.ey.in.tds.jdbc.dao.CollecteeNoiThresholdLedgerDAO;
import com.ey.in.tds.jdbc.dao.CollectorOnBoardingInfoDAO;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.jdbc.dao.KYCDetailsDAO;
import com.ey.in.tds.onboarding.service.util.excel.deductee.CollecteeMasterExcel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

/**
 * 
 * @author vamsir
 *
 */

@Service
public class CollecteeMasterService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CollecteeMasterDAO collecteeMasterDAO;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private TCSBatchUploadDAO tcsBatchUploadDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private CollecteeBulkService collecteeBulkService;

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private CollecteeNoiThresholdLedgerDAO collecteeNoiThresholdLedgerDAO;

	@Autowired
	private CollectorOnBoardingInfoDAO collectorOnBoardingInfoDAO;

	@Autowired
	private KYCDetailsDAO kycDetailsDAO;

	ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 
	 * @param collecteeMaster
	 * @param deductorPan
	 * @param userName
	 * @param collectorTan
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	public CollecteeMaster createCollectee(CollecteeMasterDTO collecteeMasterDTO, String collectorPan, String userName,
			String collectorTan, MultipartFile nccCertificateFile, String tenantId) throws IllegalAccessException,
			InvocationTargetException, InvalidKeyException, URISyntaxException, StorageException, IOException {
		logger.info("REST request to save a deductee residential Record : {}", collecteeMasterDTO);
		CollecteeMaster collectee = new CollecteeMaster();
		BeanUtils.copyProperties(collectee, collecteeMasterDTO);
		collectee.setCollectorPan(collectorPan);
		collectee.setCollectorTan(collectorTan);
		collectee.setNameOfTheCollectee(collecteeMasterDTO.getNameOfTheCollectee().trim());
		collectee.setCollecteePan(collecteeMasterDTO.getCollecteePan().trim());
		collectee.setCollecteeCode(collecteeMasterDTO.getCollecteeCode().trim());
		collectee.setCreatedDate(new Date());
		collectee.setCreatedBy(userName);
		collectee.setModifiedBy(userName);
		collectee.setModifiedDate(new Date());
		collectee.setActive(true);
		collectee.setCurrentBalanceMonth(collecteeMasterDTO.getCurrentBalanceMonth());
		collectee.setCurrentBalanceYear(collecteeMasterDTO.getCurrentBalanceYear());
		collectee.setPreviousBalanceMonth(collecteeMasterDTO.getPreviousBalanceMonth());
		collectee.setPreviousBalanceYear(collecteeMasterDTO.getPreviousBalanceYear());
		collectee.setGstinNumber(collecteeMasterDTO.getGstinNumber());
		if (StringUtils.isNotBlank(collectee.getCollecteePan())) {
			collectee.setPanVerifyStatus(false);
		}
		if (nccCertificateFile != null) {
			String nccCertificateUrl = blob.uploadExcelToBlob(nccCertificateFile);
			logger.info("nccCertificateUrl: {}", nccCertificateUrl);
			collectee.setNccCertificateUrl(nccCertificateUrl);
		}
		// additional section logic
		collectee.setIsEligibleForMultipleSections(collecteeMasterDTO.getIsEligibleForMultipleSections());
		Map<String, BigDecimal> additionalSection = new HashMap<>();
		boolean isAdditionalSection = false;
		Map<String, BigDecimal> newSections = new HashMap<>();
		if (!collecteeMasterDTO.getIsEligibleForMultipleSections().equals(false)) {
			for (TCSAdditionalSectionDTO additionalSections : collecteeMasterDTO.getTcsadditionalSections()) {
				if (StringUtils.isBlank(collecteeMasterDTO.getTcsSection())) {
					if (StringUtils.isNotBlank(additionalSections.getSection())) {
						collectee.setTcsSection(additionalSections.getSection());
						collectee.setNatureOfIncome(additionalSections.getNatureOfIncome());
						collectee.setIsEligibleForMultipleSections(isAdditionalSection);
						if (additionalSections.getRate() != BigDecimal.ZERO) {
							collectee.setTcsRate(additionalSections.getRate());
						} else {
							collectee.setTcsRate(BigDecimal.ZERO);
						}
						newSections.put(collectee.getTcsSection(), collectee.getTcsRate());
					}
				} else if (StringUtils.isNotBlank(collecteeMasterDTO.getTcsSection())
						&& collecteeMasterDTO.getTcsSection().equals(additionalSections.getSection())) {
					throw new CustomException("Duplicate sections are not allowed", HttpStatus.BAD_REQUEST);
				} else if (StringUtils.isNotBlank(additionalSections.getSection())) {
					String sectionAndNoi = additionalSections.getSection() + "-"
							+ additionalSections.getNatureOfIncome();
					if (additionalSections.getRate() != BigDecimal.ZERO) {
						additionalSection.put(sectionAndNoi, additionalSections.getRate());
					} else {
						additionalSection.put(sectionAndNoi, BigDecimal.ZERO);
					}
					newSections.put(additionalSections.getSection(), additionalSections.getRate());
				}
			}
		}

		List<DeductorMaster> collectorMaster = deductorMasterDAO.findBasedOnDeductorPan(collectorPan);
		if (!collectorMaster.isEmpty() && collectorMaster != null) {
			collectee.setCollectorCode(collectorMaster.get(0).getCode());
		}
		List<CollecteeMaster> collecteeDB = collecteeMasterDAO.getColleteeData(collectorPan,
				collectee.getCollecteeCode());
		logger.info("list collectee data size :{}", collecteeDB.size());

		String additionalSections = objectMapper.writeValueAsString(additionalSection);
		collectee.setAdditionalSections(additionalSections);
		collectee.setApplicableFrom(collecteeMasterDTO.getApplicableFrom());
		collectee.setApplicableTo(collecteeMasterDTO.getApplicableTo());

		// feign client for noi
		NatureOfPaymentMasterDTO natureOfPaymentMasterDTO = mastersClient.getNatureOfIncomeBySection("206C(1H)")
				.getBody().getData();

		TCSMasterDTO tcsMasterDTO = mastersClient.getRateMasterByNoiId(natureOfPaymentMasterDTO.getId()).getBody()
				.getData();

		List<CollecteeNoiThresholdLedger> collecteeNoiThresholdLedgerList = new ArrayList<>();
		Integer noiYear = collectee.getCurrentBalanceYear();
		if (collectee.getCurrentBalanceYear() == null) {
			noiYear = CommonUtil.getAssessmentYear(null);
		}

		// get threshold ledger data based on collectee pan
		collecteeNoiThresholdLedgerList = collecteeNoiThresholdLedgerDAO.findByCollecteeCodeOrCollecteePan(
				collectee.getCollecteeCode(), collectorPan, collectee.getCollecteePan(), noiYear);

		CollecteeNoiThresholdLedger collecteeNoiThresholdLedger = new CollecteeNoiThresholdLedger();

		// get collection reference id based on collector pan.
		String collectionReferenceId = collectorOnBoardingInfoDAO.getCollectionReferenceId(collectorPan);
		BigDecimal amount = BigDecimal.ZERO;
		if (StringUtils.isNotBlank(collectionReferenceId)) {
			amount = collectee.getAdvanceBalancesForSection206c().add(collectee.getCollectionsBalancesForSection206c());
			logger.info("amount is {}: ", amount);
		} else {
			amount = collectee.getBalancesForSection206c().add(collectee.getAdvanceBalancesForSection206c())
					.add(collectee.getCollectionsBalancesForSection206c());
			logger.info("amount is {}: ", amount);
		}
		// get buyer Threshold Computation Id based on collector pan
		String buyerThresholdComputationId = collectorOnBoardingInfoDAO.getBuyerThresholdComputationId(collectorPan);
		if (buyerThresholdComputationId != null) {

		}
		if (!collecteeNoiThresholdLedgerList.isEmpty()) {

			/**
			 * case-1 if the record is updated with a different collectee code but existing
			 * pan,then we will be having 2 record, case-2 if the record is created with
			 * diferent colectee code but existing pan then we will be having one record for
			 * case2 we are taking the record having collectee code
			 * 
			 * case-3 if the NOI update is already done for the perticular pan, then check
			 * in DB with pan and collectee code combination, if any record is present then
			 * do not execute the update logic once again
			 */

			if (collecteeNoiThresholdLedgerList.size() == 1) {
				collecteeNoiThresholdLedger = collecteeNoiThresholdLedgerList.get(0);
			} else {
				collecteeNoiThresholdLedger = collecteeNoiThresholdLedgerList.stream()
						.filter(x -> x.getCollecteeCode() != null).collect(Collectors.toList()).get(0);
			}
			Integer count = 0;
			if (collectee.getCollecteePan() != null) {
				count = collecteeMasterDAO.getCountByCollecteeCodeAndPan(collectee.getCollecteeCode(),
						collectee.getCollecteePan(), collectorPan);
			}
			if (count == 0) {
				collecteeNoiThresholdLedgerUpdate(collecteeNoiThresholdLedger, amount, userName,
						collectee.getCollecteePan(), collectorPan);
			}
		} else {
			collecteeeNoiLedgerSave(collectee, natureOfPaymentMasterDTO, collecteeNoiThresholdLedger, userName, amount,
					noiYear);
		}
		// check for advance balances for section 206c amount
		BigDecimal advanceAmount = collectee.getAdvanceBalancesForSection206c();
		logger.info("advance march Amount is {}: ", advanceAmount);
		// check greater then zero
		int advanceAmountCheck = advanceAmount.compareTo(BigDecimal.valueOf(0));
		if (advanceAmountCheck == 1) {
			// payment save
			paymentSave(userName, collectee, natureOfPaymentMasterDTO, tcsMasterDTO,
					collecteeMasterDTO.getCurrentBalanceMonth(), collectee.getAdvanceBalancesForSection206c(), noiYear);
		}
		// check for advance as of march amount
		BigDecimal advanceAsOfMarchAmount = collectee.getAdvancesAsOfMarch();
		logger.info("advance as of march Amount is {}: ", advanceAsOfMarchAmount);
		// check greater then zero
		int advanceAsofMarchAmountCheck = advanceAsOfMarchAmount.compareTo(BigDecimal.valueOf(0));
		if (advanceAsofMarchAmountCheck == 1) {
			// payment save
			paymentSave(userName, collectee, natureOfPaymentMasterDTO, tcsMasterDTO,
					collecteeMasterDTO.getPreviousBalanceMonth(), advanceAsOfMarchAmount,
					collecteeMasterDTO.getPreviousBalanceYear());
		}
		if (!collecteeDB.isEmpty()) {
			collectee.setId(collecteeDB.get(0).getId());
			if (StringUtils.isNotBlank(collecteeDB.get(0).getTcsSection())) {
				String dbAdditionalSections = StringUtils.EMPTY;
				if (StringUtils.isBlank(collectee.getTcsSection())) {
					collectee.setTcsSection(collecteeDB.get(0).getTcsSection());
					collectee.setTcsRate(collecteeDB.get(0).getTcsRate());
					collectee.setNatureOfIncome(collecteeDB.get(0).getNatureOfIncome());
					if (!collecteeDB.get(0).getAdditionalSections().equals("{}")) {
						dbAdditionalSections = collecteeDB.get(0).getAdditionalSections();
						logger.info("DB Additional Seciton is :{}", dbAdditionalSections);
						collectee.setAdditionalSections(dbAdditionalSections);
						collectee.setIsEligibleForMultipleSections(true);
					}
				} else if (!collecteeDB.get(0).getTcsSection().equals(collectee.getTcsSection())
						&& !collecteeDB.get(0).getNatureOfIncome().equals(collectee.getNatureOfIncome())) {
					String sectionAndNoi = collectee.getTcsSection() + "-" + collectee.getNatureOfIncome();
					additionalSection.put(sectionAndNoi, collectee.getTcsRate());
					String newSection = objectMapper.writeValueAsString(additionalSection);
					if (!collecteeDB.get(0).getAdditionalSections().equals("{}")) {
						dbAdditionalSections = collecteeDB.get(0).getAdditionalSections();
						logger.info("DB Additional Seciton is :{}", dbAdditionalSections);
					}
					logger.info("New Additional Seciton is :{}", newSection);
					String sections = newSection.concat(dbAdditionalSections);
					String addtionalSections = sections.replace("}{", ",");
					logger.info("Additional Seciton is :{}", addtionalSections);
					collectee.setAdditionalSections(addtionalSections);
					collectee.setIsEligibleForMultipleSections(true);
					collectee.setTcsSection(collecteeDB.get(0).getTcsSection());
					collectee.setTcsRate(collecteeDB.get(0).getTcsRate());
					collectee.setNatureOfIncome(collecteeDB.get(0).getNatureOfIncome());
				} else {
					throw new CustomException("Duplicate sections are not allowed", HttpStatus.BAD_REQUEST);
				}
			}
			return collecteeMasterDAO.update(collectee);
		} else {
			return collecteeMasterDAO.save(collectee);
		}
	}

	/**
	 * 
	 * @param userName
	 * @param collectee
	 * @param natureOfPaymentMasterDTO
	 * @param tCSMasterDTO
	 * @param amount
	 * @param month
	 */
	private void paymentSave(String userName, CollecteeMaster collectee,
			NatureOfPaymentMasterDTO natureOfPaymentMasterDTO, TCSMasterDTO tCSMasterDTO, int month,
			BigDecimal balanceAmount, Integer assessmentYear) {

		String documentNumber = collectee.getCollecteeCode() + collectee.getCollecteePan();

		Long count = collecteeMasterDAO.getPaymentbyIntialBalance(collectee.getCollectorPan(), assessmentYear, month,
				documentNumber, collectee.getCollectorTan());
		if (count == 0) {
			// payment save
			TcsPaymentDTO tcsPaymentDTO = new TcsPaymentDTO();
			tcsPaymentDTO.setCollecteeCode(collectee.getCollecteeCode());
			tcsPaymentDTO.setAssessmentYear(assessmentYear);
			tcsPaymentDTO.setChallanMonth(month);
			tcsPaymentDTO.setAmount(balanceAmount);
			tcsPaymentDTO.setCollectorCode(collectee.getCollectorCode());
			tcsPaymentDTO.setCollectorTan(collectee.getCollectorTan());
			tcsPaymentDTO.setCollectorPan(collectee.getCollectorPan());
			tcsPaymentDTO.setCollecteeName(collectee.getNameOfTheCollectee());
			tcsPaymentDTO.setDocumentType("ADV");
			tcsPaymentDTO.setCollecteePan(collectee.getCollecteePan());

			LocalDate localDate = LocalDate.of(CommonUtil.getCurrentYearByAssessmentYear(assessmentYear, month), month,
					1);
			LocalDate monthDate = localDate.plusDays(localDate.lengthOfMonth() - 1);
			int monthLastDay = monthDate.getDayOfMonth();
			logger.info("month last day: {}", monthLastDay);
			LocalDate localDate1 = LocalDate.of(CommonUtil.getCurrentYearByAssessmentYear(assessmentYear, month), month,
					monthLastDay);
			ZoneId defaultZoneId = ZoneId.systemDefault();
			tcsPaymentDTO.setDocumentDate(Date.from((localDate1.atStartOfDay(defaultZoneId).toInstant())));
			tcsPaymentDTO.setDocumentNumber(documentNumber);
			tcsPaymentDTO.setLineNumber(1);
			tcsPaymentDTO.setNatureOfIncome(natureOfPaymentMasterDTO.getNature());
			tcsPaymentDTO.setNatureOfIncomeId(natureOfPaymentMasterDTO.getId());
			tcsPaymentDTO.setActive(true);
			tcsPaymentDTO.setIsParent(false);
			tcsPaymentDTO.setIsExempted(false);
			tcsPaymentDTO.setChallanPaid(false);
			tcsPaymentDTO.setApprovedForChallan(false);
			tcsPaymentDTO.setIsChallanGenerated(false);
			tcsPaymentDTO.setCreatedBy(userName);
			tcsPaymentDTO.setCreatedDate(new Date());
			tcsPaymentDTO.setModifiedBy(userName);
			tcsPaymentDTO.setModifiedDate(new Date());
			tcsPaymentDTO.setPostingDate(Date.from((localDate1.atStartOfDay(defaultZoneId).toInstant())));
			tcsPaymentDTO.setHasMismatch(false);
			tcsPaymentDTO.setUnderThreshold(false);
			tcsPaymentDTO.setNoiId(natureOfPaymentMasterDTO.getId());

			// 9 columns
			tcsPaymentDTO.setActualTcsSection("206C(1H)");
			tcsPaymentDTO.setDerivedTcsSection("206C(1H)");
			tcsPaymentDTO.setFinalTcsSection("206C(1H)");

			tcsPaymentDTO.setActualTcsRate(tCSMasterDTO.getRate());
			tcsPaymentDTO.setDerivedTcsRate(tCSMasterDTO.getRate());
			tcsPaymentDTO.setFinalTcsRate(tCSMasterDTO.getRate());

			tcsPaymentDTO
					.setActualTcsAmount(balanceAmount.multiply(tCSMasterDTO.getRate()).divide(new BigDecimal(100)));
			tcsPaymentDTO
					.setDerivedTcsAmount(balanceAmount.multiply(tCSMasterDTO.getRate()).divide(new BigDecimal(100)));
			tcsPaymentDTO.setFinalTcsAmount(balanceAmount.multiply(tCSMasterDTO.getRate()).divide(new BigDecimal(100)));

			collecteeMasterDAO.paymentSave(tcsPaymentDTO);

		}
	}

	/**
	 * 
	 * @param collectee
	 * @param natureOfPaymentMasterDTO
	 * @param collecteeNoiThresholdLedger
	 * @param currentYear
	 */
	private void collecteeeNoiLedgerSave(CollecteeMaster collectee, NatureOfPaymentMasterDTO natureOfPaymentMasterDTO,
			CollecteeNoiThresholdLedger collecteeNoiThresholdLedger, String userName, BigDecimal amount,
			int currentYear) {
		collecteeNoiThresholdLedger.setActive(true);
		collecteeNoiThresholdLedger.setCreatedDate(new Date());
		collecteeNoiThresholdLedger.setCreatedBy(userName);
		collecteeNoiThresholdLedger.setModifiedBy(userName);
		collecteeNoiThresholdLedger.setModifiedDate(new Date());
		if (StringUtils.isNotBlank(collectee.getCollecteePan())) {
			collecteeNoiThresholdLedger.setCollecteePan(collectee.getCollecteePan());
		} else {
			collecteeNoiThresholdLedger.setCollecteeCode(collectee.getCollecteeCode());
		}
		collecteeNoiThresholdLedger.setCollectorPan(collectee.getCollectorPan());
		logger.info("amount is {}: ", amount);
		collecteeNoiThresholdLedger.setAmountUtilized(amount);
		int value = collecteeNoiThresholdLedger.getAmountUtilized().compareTo(BigDecimal.valueOf(5000000));
		if (value == 1) {
			collecteeNoiThresholdLedger.setThresholdReached(true);
		} else {
			collecteeNoiThresholdLedger.setThresholdReached(false);
		}
		collecteeNoiThresholdLedger.setCollecteeSection("206C(1H)");
		collecteeNoiThresholdLedger.setLastUpdatedDate(new Date());
		collecteeNoiThresholdLedger.setNatureOfIncome(natureOfPaymentMasterDTO.getNature());
		collecteeNoiThresholdLedger.setNoiId(natureOfPaymentMasterDTO.getId().intValue());
		collecteeNoiThresholdLedger.setYear(currentYear);
		collecteeNoiThresholdLedgerDAO.save(collecteeNoiThresholdLedger);
	}

	/**
	 * 
	 * @param collectorPan
	 * @param id
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public CollecteeMasterDTO getCollectee(String collectorPan, Integer id)
			throws IllegalAccessException, InvocationTargetException, JsonMappingException, JsonProcessingException {
		CollecteeMasterDTO collecteeMasterDTO = new CollecteeMasterDTO();
		List<CollecteeMaster> listCollectee = collecteeMasterDAO.getCollectee(collectorPan, id);
		CollecteeMaster collecteeMaster = new CollecteeMaster();
		Set<TCSAdditionalSectionDTO> additionalSectionsSet = new HashSet<>();
		TCSAdditionalSectionDTO additionalSections = null;
		if (!listCollectee.isEmpty() && listCollectee != null) {
			collecteeMaster = listCollectee.get(0);
			BeanUtils.copyProperties(collecteeMasterDTO, collecteeMaster);

			Map<String, BigDecimal> additionalSectionss = null;
			if (collecteeMaster.getAdditionalSections() != null) {
				additionalSectionss = objectMapper.readValue(collecteeMaster.getAdditionalSections(),
						new TypeReference<Map<String, BigDecimal>>() {
						});
			}
			if (collecteeMaster.getIsEligibleForMultipleSections() != null && additionalSectionss != null) {
				for (Map.Entry<String, BigDecimal> entry : additionalSectionss.entrySet()) {
					additionalSections = new TCSAdditionalSectionDTO();
					String section = StringUtils.substringBefore(entry.getKey(), "-");
					String noi = StringUtils.substringAfter(entry.getKey(), "-");
					additionalSections.setSection(section);
					additionalSections.setNatureOfIncome(noi);
					additionalSections.setRate(entry.getValue());
					additionalSectionsSet.add(additionalSections);
				}
			}
			collecteeMasterDTO.setTcsadditionalSections(additionalSectionsSet);
			String nccCertificateUrl = collecteeMasterDTO.getNccCertificateUrl();
			if (StringUtils.isNotBlank(nccCertificateUrl)) {
				String nccFileName = nccCertificateUrl.substring(nccCertificateUrl.lastIndexOf('/') + 1);
				collecteeMasterDTO.setNccCertificateFileName(nccFileName);
				collecteeMasterDTO.setNccCertificateUrl(collecteeMasterDTO.getNccCertificateUrl());
			}
		}
		return collecteeMasterDTO;
	}

	/**
	 * 
	 * @param collecteeMasterDTO
	 * @param deductorPan
	 * @param userName
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	public CollecteeMaster updateCollecteeMaster(CollecteeMasterDTO collecteeMasterDTO, String deductorPan,
			String userName, MultipartFile nccCertificateFile) throws IllegalAccessException, InvocationTargetException,
			InvalidKeyException, URISyntaxException, StorageException, IOException {
		logger.info("REST request to save a deductee residential Record : {}", collecteeMasterDTO);
		CollecteeMaster collectee = new CollecteeMaster();
		BeanUtils.copyProperties(collectee, collecteeMasterDTO);
		if (nccCertificateFile != null) {
			String nccCertificateUrl = blob.uploadExcelToBlob(nccCertificateFile);
			logger.info("nccCertificateUrl: {}", nccCertificateUrl);
			collectee.setNccCertificateUrl(nccCertificateUrl);
		}

		collectee.setModifiedBy(userName);
		collectee.setModifiedDate(new Date());
		return collecteeMasterDAO.update(collectee);
	}

	/**
	 * 
	 * @param file
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @param pan
	 * @return
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InvalidKeyException
	 */
	@Transactional
	public TCSBatchUpload saveFileData(MultipartFile multiPartFile, String collectorTan, Integer assesssmentYear,
			Integer assessmentMonth, String userName, String tenantId, String collectorPan)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);
		if (isAlreadyProcessed(sha256)) {
			TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
			tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setCreatedBy(userName);
			tcsBatchUpload.setSuccessCount(0L);
			tcsBatchUpload.setFailedCount(0L);
			tcsBatchUpload.setRowsCount(0L);
			tcsBatchUpload.setProcessed(0);
			tcsBatchUpload.setMismatchCount(0L);
			tcsBatchUpload.setSha256sum(sha256);
			tcsBatchUpload.setStatus("Duplicate");
			tcsBatchUpload.setNewStatus("Duplicate");
			tcsBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			tcsBatchUpload = collecteeBatchUpload(tcsBatchUpload, multiPartFile, collectorTan, assesssmentYear,
					assessmentMonth, userName, null, tenantId);
			return tcsBatchUpload;
		}
		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = Excel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count {}:", headersCount);
			TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
			if (headersCount != CollecteeMasterExcel.fieldMappings.size()) {
				tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				tcsBatchUpload.setSuccessCount(0L);
				tcsBatchUpload.setFailedCount(0L);
				tcsBatchUpload.setRowsCount(0L);
				tcsBatchUpload.setProcessed(0);
				tcsBatchUpload.setMismatchCount(0L);
				tcsBatchUpload.setSha256sum(sha256);
				tcsBatchUpload.setStatus("Failed");
				tcsBatchUpload.setCreatedBy(userName);
				tcsBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return collecteeBatchUpload(tcsBatchUpload, multiPartFile, collectorTan, assesssmentYear,
						assessmentMonth, userName, null, tenantId);
			} else {
				tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				tcsBatchUpload.setStatus("Processing");
				tcsBatchUpload.setSuccessCount(0L);
				tcsBatchUpload.setFailedCount(0L);
				tcsBatchUpload.setRowsCount(0L);
				tcsBatchUpload.setProcessed(0);
				tcsBatchUpload.setMismatchCount(0L);
				tcsBatchUpload = collecteeBatchUpload(tcsBatchUpload, multiPartFile, collectorTan, assesssmentYear,
						assessmentMonth, userName, null, tenantId);
			}
			if (headersCount == CollecteeMasterExcel.fieldMappings.size()) {
				return collecteeBulkService.processCollecteeMaster(workbook, multiPartFile, sha256, collectorTan,
						assesssmentYear, assessmentMonth, userName, tenantId, collectorPan, tcsBatchUpload);
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process collectee data ", e);
		}
	}

	private boolean isAlreadyProcessed(String sha256Sum) {
		List<TCSBatchUpload> sha256Record = tcsBatchUploadDAO.getSha256Records(sha256Sum);
		return sha256Record != null && !sha256Record.isEmpty();
	}

	/**
	 * 
	 * @param tcsBatchUpload
	 * @param mFile
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param file
	 * @param tenant
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public TCSBatchUpload collecteeBatchUpload(TCSBatchUpload tcsBatchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonth, String userName, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("batch", tcsBatchUpload);
		String errorFp = null;
		String path = null;
		if (file != null) {
			errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
			tcsBatchUpload.setErrorFilePath(errorFp);
		}
		if (mFile != null) {
			path = blob.uploadExcelToBlob(mFile);
			tcsBatchUpload.setFileName(mFile.getOriginalFilename());
			tcsBatchUpload.setFilePath(path);
		}
		tcsBatchUpload.setAssessmentMonth(assessmentMonth);
		tcsBatchUpload.setAssessmentYear(assesssmentYear);
		tcsBatchUpload.setCollectorMasterTan(tan);
		tcsBatchUpload.setUploadType(UploadTypes.COLLECTEE_EXCEL.name());
		tcsBatchUpload.setCreatedBy(userName);
		tcsBatchUpload.setActive(true);
		if (tcsBatchUpload.getId() != null) {
			tcsBatchUpload.setId(tcsBatchUpload.getId());
			return tcsBatchUpload = tcsBatchUploadDAO.update(tcsBatchUpload);
		} else {
			return tcsBatchUpload = tcsBatchUploadDAO.save(tcsBatchUpload);
		}
	}

	/**
	 * 
	 * @param file
	 * @param collectorTan
	 * @param tenantId
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param pan
	 * @param batchId
	 * @return
	 * @throws Exception
	 */
	public TCSBatchUpload readCsvFile(MultipartFile file, String collectorTan, String tenantId, Integer assessmentYear,
			Integer assessmentMonth, String userName, String collectorPan, Integer batchId) throws Exception {

		ArrayList<CollecteeMasterErrorReportCsvDTO> listErrorReports = new ArrayList<>();
		TCSBatchUpload tcsBatch = new TCSBatchUpload();

		Long processedCount = 0L;
		int serialNumberForErrorCount = 0;
		Long errorCount = 0L;
		Long totalRecordsCount = 0L;
		Long duplicateCount = 0L;
		boolean residentIndicator = false;
		tcsBatch.setMismatchCount(0L);

		tcsBatch.setSha256sum(sha256SumService.getSHA256Hash(file));
		tcsBatch.setAssessmentYear(assessmentYear);
		tcsBatch.setCollectorMasterTan(collectorTan);
		tcsBatch.setUploadType(UploadTypes.COLLECTEE_SAP.name());
		tcsBatch.setId(batchId);
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();

		// reading csv file to get header count
		Reader headerCountReader = new FileReader(convFile);
		CSVReader csvReader = new CSVReader(headerCountReader);
		String[] csvHeaders = csvReader.readNext();
		int csvHeaderCount = csvHeaders.length;
		csvReader.close();

		Reader reader = new FileReader(convFile);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		// reading csv file and processing records
		CsvToBean<CollecteeMasterCsvFile> csvToBean = new CsvToBeanBuilder(reader)
				.withType(CollecteeMasterCsvFile.class).withIgnoreLeadingWhiteSpace(true).build();
		Iterator<CollecteeMasterCsvFile> collecteeRows = csvToBean.iterator();
		List<CollecteeMaster> listCollectee = new ArrayList<>();
		Set<String> collecteeMasterSet = new HashSet<>();
		if (csvHeaderCount == 37) {
			while (collecteeRows.hasNext()) {
				serialNumberForErrorCount++;
				CollecteeMasterCsvFile collecteeRow = collecteeRows.next();
				// List of Records iterate
				logger.info("collectee residential status: {}", collecteeRow.getCollecteeResidentIndicator());
				if ("RESIDENT".equalsIgnoreCase(collecteeRow.getCollecteeResidentIndicator())
						|| "RES".equalsIgnoreCase(collecteeRow.getCollecteeResidentIndicator())
						|| "N".equalsIgnoreCase(collecteeRow.getCollecteeResidentIndicator())
						|| "NON-RESIDENT".equalsIgnoreCase(collecteeRow.getCollecteeResidentIndicator())
						|| "NR".equalsIgnoreCase(collecteeRow.getCollecteeResidentIndicator())
						|| "Y".equalsIgnoreCase(collecteeRow.getCollecteeResidentIndicator())) {
					residentIndicator = true;
					boolean isNotValid = false;
					boolean isResStatusValid = true;
					++totalRecordsCount;
					CollecteeMasterErrorReportCsvDTO errorReportCollecteeDTO = new CollecteeMasterErrorReportCsvDTO();
					errorReportCollecteeDTO.setReason("");
					CollecteeMaster collecteeMaster = new CollecteeMaster();
					collecteeMaster.setActive(true);
					if (StringUtils.isNotBlank(collecteeRow.getCollecteePAN())) {
						collecteeMaster.setPanVerifyStatus(false);
					}
					collecteeMaster.setSourceIdentifier(collecteeRow.getSourceIdentifier());
					collecteeMaster.setSourceFileName(collecteeRow.getSourceFileName());
					// RES is false , NR is true
					boolean residentStatus = false;
					if ("NON-RESIDENT".equalsIgnoreCase(collecteeRow.getCollecteeResidentIndicator())
							|| "NR".equalsIgnoreCase(collecteeRow.getCollecteeResidentIndicator())
							|| "Y".equalsIgnoreCase(collecteeRow.getCollecteeResidentIndicator())) {
						residentStatus = true;
					}
					collecteeMaster.setNonResidentCollecteeIndicator(residentStatus);
					// check for collectee code
					if (StringUtils.isBlank(collecteeRow.getCollecteeCode().trim())) {
						errorReportCollecteeDTO
								.setReason(errorReportCollecteeDTO.getReason() + "COLLECTEE CODE IS MANDATORY." + "\n");
						isNotValid = true;
					} else {
						collecteeMaster.setCollecteeCode(collecteeRow.getCollecteeCode().trim());
					}
					collecteeMaster.setNameOfTheCollector(collecteeRow.getNameOfTheCollector());
					collecteeMaster.setCollectorPan(collecteeRow.getCollectorPAN());
					// Check for collector pan
					if (StringUtils.isBlank(collecteeMaster.getCollectorPan())) {
						errorReportCollecteeDTO
								.setReason(errorReportCollecteeDTO.getReason() + "COLLECTOR PAN IS MANDATORY." + "\n");
						isNotValid = true;
					} else if (StringUtils.isNotBlank(collecteeMaster.getCollectorPan())
							&& !collectorPan.equalsIgnoreCase(collecteeRow.getCollectorPAN())) {
						errorReportCollecteeDTO.setReason(errorReportCollecteeDTO.getReason() + "COLLECTOR PAN "
								+ collectorPan + " IS NOT MATCH." + "\n");
						isNotValid = true;
					}
					// Check for Company Code
					List<DeductorMaster> deductorMaster = deductorMasterDAO.findByDeductorPan(collectorPan);
					if (!deductorMaster.isEmpty()) {
						collecteeMaster.setCollectorCode(deductorMaster.get(0).getCode());
					}
					if (StringUtils.isEmpty(collecteeRow.getCollectorCode())) {
						// error report
						errorReportCollecteeDTO
								.setReason(errorReportCollecteeDTO.getReason() + "COLLECTOR CODE IS MANDATORY." + "\n");
						isNotValid = true;
					} else if (StringUtils.isNotEmpty(collecteeRow.getCollectorCode())) {
						if (!deductorMaster.isEmpty()
								&& !deductorMaster.get(0).getCode().equalsIgnoreCase(collecteeRow.getCollectorCode())) {
							// error report
							errorReportCollecteeDTO.setReason(errorReportCollecteeDTO.getReason() + "COLLECTOR CODE "
									+ collecteeRow.getCollectorCode() + " IS NOT MATCH" + "\n");
							isNotValid = true;
						}
						collecteeMaster.setCollectorCode(collecteeRow.getCollectorCode());
					}
					if (StringUtils.isNotBlank(collecteeRow.getNameOfTheCollectee().trim())) {
						collecteeMaster.setNameOfTheCollectee(collecteeRow.getNameOfTheCollectee().trim());
					} else {
						errorReportCollecteeDTO.setNameOfTheCollectee("");
						errorReportCollecteeDTO.setReason(
								errorReportCollecteeDTO.getReason() + "NAME OF THE COLLECTEE IS NOT PRESENT" + "\n");
						isNotValid = true;
						isResStatusValid = false;
					}

					if (collecteeRow.getBalancesForSection206C() != null
							|| collecteeRow.getAdvanceBalancesForSection206C() != null
							|| collecteeRow.getCollectionsBalancesForSection206C() != null) {
						if (collecteeRow.getCurrentBalanceYear() == null) {
							errorReportCollecteeDTO.setReason(
									errorReportCollecteeDTO.getReason() + "Current balance year is mandatory " + "\n");
							isNotValid = true;
						}
						if (collecteeRow.getCurrentBalanceMonth() == null) {
							errorReportCollecteeDTO.setReason(
									errorReportCollecteeDTO.getReason() + "Current balance month is mandatory " + "\n");
							isNotValid = true;
						} else if (collecteeRow.getCurrentBalanceYear() == null
								&& collecteeRow.getCurrentBalanceMonth() == null) {
							errorReportCollecteeDTO.setReason(errorReportCollecteeDTO.getReason()
									+ "Current balance year and current balance month is mandatory " + "\n");
							isNotValid = true;

						}
					}

					if (collecteeRow.getAdvancesAsOfMarch() != null) {
						if (collecteeRow.getPreviousBalanceYear() == null) {
							errorReportCollecteeDTO.setReason(
									errorReportCollecteeDTO.getReason() + "Previous balance year is mandatory " + "\n");
							isNotValid = true;
						}
						if (collecteeRow.getPreviousBalanceMonth() == null) {
							errorReportCollecteeDTO.setReason(errorReportCollecteeDTO.getReason()
									+ "Previous balance month is mandatory " + "\n");
							isNotValid = true;
						} else if (collecteeRow.getPreviousBalanceYear() == null
								&& collecteeRow.getPreviousBalanceMonth() == null) {
							errorReportCollecteeDTO.setReason(errorReportCollecteeDTO.getReason()
									+ "Previous balance year and Previous balance month is mandatory " + "\n");
							isNotValid = true;
						}
					}
					collecteeMaster.setEmailAddress(collecteeRow.getEmailAddress());
					collecteeMaster.setPhoneNumber(collecteeRow.getPhoneNumber());
					collecteeMaster.setFlatDoorBlockNo(collecteeRow.getFlatOrDoorOrBlockNo());
					collecteeMaster.setNameOfTheBuildingVillage(collecteeRow.getNameOfPremisesOrBuildingOrVillage());
					collecteeMaster.setRoadStreetPostoffice(collecteeRow.getRoadOrStreetOrPostOffice());
					collecteeMaster.setAreaLocality(collecteeRow.getAreaOrLocality());
					collecteeMaster.setTownCityDistrict(collecteeRow.getTownOrCityOrDistrict());
					collecteeMaster.setState(collecteeRow.getState());
					collecteeMaster.setCountry(collecteeRow.getCountry());
					collecteeMaster.setPinCode(collecteeRow.getPinCode());
					collecteeMaster.setTdsCode(collecteeRow.getTaxCode());

					if (collecteeRow.getNoCollectionDeclarationAsPerForm27C() != null) {
						collecteeMaster.setNoCollectionDeclarationAsPerForm27c(true);
					} else {
						collecteeMaster.setNoCollectionDeclarationAsPerForm27c(false);
					}
					if (StringUtils.isBlank(collecteeRow.getApplicableFrom())) {
						errorReportCollecteeDTO.setApplicableFrom("");
						errorReportCollecteeDTO.setReason(
								errorReportCollecteeDTO.getReason() + "APPLICABLE FROM DATE IS MANDATORY" + "\n");
						isNotValid = true;
					} else if (StringUtils.isNotBlank(collecteeRow.getApplicableFrom())) {
						Date applicableFrom = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT)
								.parse(collecteeRow.getApplicableFrom());
						collecteeMaster.setApplicableFrom(applicableFrom);
					}
					if (StringUtils.isNotBlank(collecteeRow.getApplicableTo())) {
						Date applicableTo = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT)
								.parse(collecteeRow.getApplicableTo());
						collecteeMaster.setApplicableTo(applicableTo);
					}
					collecteeMaster.setUserDefinedField1(collecteeRow.getUserDefinedField1());
					collecteeMaster.setUserDefinedField2(collecteeRow.getUserDefinedField2());
					collecteeMaster.setUserDefinedField3(collecteeRow.getUserDefinedField3());
					collecteeMaster.setUserDefinedField4(collecteeRow.getUserDefinedField4());
					collecteeMaster.setUserDefinedField5(collecteeRow.getUserDefinedField5());
					collecteeMaster.setCreatedBy(userName);
					collecteeMaster.setCreatedDate(new Date());
					collecteeMaster.setDistributionChannel(collecteeRow.getDistributionChannel());
					collecteeMaster.setCollectorTan(collectorTan);
					if (StringUtils.isNotBlank(collecteeRow.getCollecteePAN())) {
						collecteeRow.setCollecteePAN(collecteeRow.getCollecteePAN().toUpperCase());
						// feign client to get PanName based on pan code
						if (collecteeRow.getCollecteePAN().matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
							collecteeMaster.setCollecteePan(collecteeRow.getCollecteePAN().toUpperCase());
							String collecteeStatus = mastersClient
									.getPanNameBasedOnPanCode(String.valueOf(collecteeRow.getCollecteePAN().charAt(3)))
									.getBody();
							if (StringUtils.isBlank(collecteeStatus)) {
								errorReportCollecteeDTO.setCollecteeStatus(collecteeStatus);
								errorReportCollecteeDTO
										.setReason(errorReportCollecteeDTO.getReason() + "Pan 4th character "
												+ collecteeRow.getCollecteePAN().charAt(3) + " is Invalid " + "\n");
								isNotValid = true;
								isResStatusValid = false;
							} else {
								collecteeMaster.setCollecteeStatus(collecteeStatus);
							}
						} else {
							errorReportCollecteeDTO.setCollecteePan(collecteeRow.getCollecteePAN());
							errorReportCollecteeDTO.setReason(
									errorReportCollecteeDTO.getReason() + "COLLECTEE PAN IS NOT VALID " + "\n");
							isNotValid = true;
							isResStatusValid = false;
						}
					} /*
						 * else if (StringUtils.isNotBlank(collecteeRow.getCollecteeStatus())) { String
						 * collecteeStatus = mastersClient .getPanNameBasedOnPanCode(String.valueOf(
						 * collecteeRow.getCollecteeStatus().trim().substring(0, 1).toUpperCase()))
						 * .getBody(); if (StringUtils.isBlank(collecteeStatus)) {
						 * errorReportCollecteeDTO.setCollecteeStatus(collecteeRow.getCollecteeStatus())
						 * ; errorReportCollecteeDTO.setReason(errorReportCollecteeDTO.getReason() +
						 * "COLLECTEE STATUS " + collecteeRow.getCollecteeStatus() + " IS NOT VALID " +
						 * "\n"); isNotValid = true; isResStatusValid = false; } else {
						 * collecteeMaster.setCollecteeStatus(collecteeStatus); } } else if
						 * (StringUtils.isBlank(collecteeRow.getCollecteePAN()) &&
						 * StringUtils.isBlank(collecteeRow.getCollecteeStatus())) {
						 * errorReportCollecteeDTO.setCollecteeStatus(collecteeRow.getCollecteeStatus())
						 * ; errorReportCollecteeDTO.setReason( errorReportCollecteeDTO.getReason() +
						 * "COLLECTEE STATUS IS MANDATORY " + "\n"); isNotValid = true; isResStatusValid
						 * = false; }
						 */
					// check for collectee type
					if (StringUtils.isNotBlank(collecteeRow.getCollecteeType())) {
						String collecteeType = mastersClient.getTcsCollecteeType(collecteeRow.getCollecteeType())
								.getBody().getData();
						if (StringUtils.isNotBlank(collecteeType)) {
							collecteeMaster.setCollecteeType(collecteeType);
						} else {
							errorReportCollecteeDTO.setReason(errorReportCollecteeDTO.getReason() + "COLLECTEE TYPE"
									+ collecteeRow.getCollecteeType() + "IS NOT MATCH" + "\n");
							isNotValid = true;
							isResStatusValid = false;
						}
					}
					// TCS Section based logic
					if (StringUtils.isNotEmpty(collecteeRow.getNatureOfIncome())
							&& StringUtils.isEmpty(collecteeRow.getTcsSection())) {
						// error report
						errorReportCollecteeDTO.setReason(errorReportCollecteeDTO.getReason()
								+ "NATURE OF INCOME IS ALLOWED IF THERE IS A SECTION." + "\n");
						isNotValid = true;
					}
					if (isResStatusValid && StringUtils.isNotBlank(collecteeRow.getTcsSection())) {
						boolean nopSection;
						if (residentStatus) {
							nopSection = mastersClient
									.getNOIBasedOnStatusAndSectionResidentStatus("NR", collecteeRow.getTcsSection())
									.getBody().getData();
						} else {
							nopSection = mastersClient
									.getNOIBasedOnStatusAndSectionResidentStatus("RES", collecteeRow.getTcsSection())
									.getBody().getData();
						}
						if (!nopSection) {
							errorReportCollecteeDTO.setTcsSection(collecteeRow.getTcsSection());
							errorReportCollecteeDTO.setReason(errorReportCollecteeDTO.getReason() + "TCS SECTION "
									+ collecteeRow.getTcsSection() + " NOT FOUND IN SYSTEM " + "\n");
							isNotValid = true;
						} else if (StringUtils.isBlank(collecteeRow.getNatureOfIncome())) {
							// get nature of payment and tds rate based on section
							List<NatureAndTaxRateDTO> nopRateList = new ArrayList<>();
							if (residentStatus) {
								nopRateList = mastersClient
										.getTcsListOfNatureAndRate(collecteeRow.getTcsSection(), "NR").getBody()
										.getData();
							} else {
								nopRateList = mastersClient
										.getTcsListOfNatureAndRate(collecteeRow.getTcsSection(), "RES").getBody()
										.getData();
							}
							Map<Double, String> rateMap = new HashMap<>();
							List<Double> rates = new ArrayList<>();
							Double highestRate = 0.0;
							if (!nopRateList.isEmpty() && nopRateList != null) {
								for (NatureAndTaxRateDTO natureAndRate : nopRateList) {
									rateMap.put(natureAndRate.getRate(), natureAndRate.getNature());
									rates.add(natureAndRate.getRate());
								}
								// section contains mutiple NOP then get the NOP based on highest rate
								highestRate = Collections.max(rates);
							}
							if (collecteeRow.getTcsRate() != null) {
								// section contains mutiple NOP then get the NOP based on closest rate passed in
								// the excel
								Optional<Double> rate = rates.parallelStream().min(Comparator.comparingDouble(
										i -> Math.abs(i - (Double.valueOf(collecteeRow.getTcsRate())))));
								collecteeMaster.setNatureOfIncome(rateMap.get(rate.isPresent() ? rate.get() : 0.0));
							} else {
								collecteeMaster.setNatureOfIncome(rateMap.get(highestRate));
							}
						} else if (StringUtils.isNotBlank(collecteeRow.getNatureOfIncome())) {
							List<String> nops = new ArrayList<>();
							if (residentStatus) {
								nops = mastersClient
										.getNOIBasedOnSectionAndResidentialStatus(collecteeRow.getTcsSection(), "NR")
										.getBody().getData();
							} else {
								nops = mastersClient
										.getNOIBasedOnSectionAndResidentialStatus(collecteeRow.getTcsSection(), "RES")
										.getBody().getData();
							}
							boolean isNopValid = false;
							for (String nop : nops) {
								if (nop.equalsIgnoreCase(collecteeRow.getNatureOfIncome().trim())) {
									isNopValid = true;
									break;
								}
							}
							if (!isNopValid) {
								errorReportCollecteeDTO
										.setReason(errorReportCollecteeDTO.getReason() + "NATURE OF INCOME "
												+ collecteeRow.getNatureOfIncome() + " NOT FOUND IN SYSTEM." + "\n");
								isNotValid = true;
							} else {
								collecteeMaster.setNatureOfIncome(collecteeRow.getNatureOfIncome().trim());
							}
						}
					}
					collecteeMaster.setTdsIndicator(Boolean.valueOf(collecteeRow.getTdsIndicator()));
					collecteeMaster.setTcsSection(collecteeRow.getTcsSection());
					if (collecteeRow.getTcsRate() != null && collecteeRow.getTcsRate().equals(BigDecimal.ZERO)) {
						collecteeMaster.setTcsRate(BigDecimal.ZERO);
					} else {
						collecteeMaster.setTcsRate(new BigDecimal(collecteeRow.getTcsRate()));
					}
					collecteeMaster.setTdsCode(collecteeRow.getTaxCode());
					if (collecteeRow.getNoCollectionDeclarationAsPerForm27C() != null) {
						collecteeMaster.setNoCollectionDeclarationAsPerForm27c(
								Boolean.valueOf(collecteeRow.getNoCollectionDeclarationAsPerForm27C()));
					}
					if (collecteeRow.getBalancesForSection206C() != null) {
						collecteeMaster
								.setBalancesForSection206c(new BigDecimal(collecteeRow.getBalancesForSection206C())
										.setScale(2, BigDecimal.ROUND_UP));
					}
					collecteeMaster.setCollecteeAadharNumber(collecteeRow.getCollecteeAadharNumber());
					collecteeMaster.setCollectionsBalancesForSection206c(
							new BigDecimal(collecteeRow.getCollectionsBalancesForSection206C()));
					collecteeMaster.setBalancesForSection206c(new BigDecimal(collecteeRow.getBalancesForSection206C()));
					collecteeMaster.setAdvanceBalancesForSection206c(
							new BigDecimal(collecteeRow.getAdvanceBalancesForSection206C()));
					collecteeMaster.setAdvancesAsOfMarch(new BigDecimal(collecteeRow.getAdvancesAsOfMarch()));

					if (isNotValid) {
						++errorCount;
					}
					if (errorCount > 0 && StringUtils.isNotBlank(errorReportCollecteeDTO.getReason())) {
						logger.info("IN COLLECTEE ERROR ");
						BeanUtils.copyProperties(collecteeRow, errorReportCollecteeDTO);
						errorReportCollecteeDTO.setCollectorCode(collecteeRow.getCollectorCode());
						errorReportCollecteeDTO.setSerialNumber(String.valueOf(serialNumberForErrorCount));
						errorReportCollecteeDTO.setNameOfTheCollectee(collecteeRow.getNameOfTheCollectee());
						listErrorReports.add(errorReportCollecteeDTO);
					} else {
						listCollectee.add(collecteeMaster);
						collecteeMasterSet.add(collecteeMaster.getCollecteePan() + "-" + collecteeMaster.getTcsSection()
								+ "-" + collecteeMaster.getCollectorPan() + "-" + collecteeMaster.getCollecteeCode());
					}
				} else {
					++errorCount;
					++totalRecordsCount;
					if (csvHeaderCount == 37) {
						logger.info("IN COLLECTEE ERROR ");
						residentIndicator = true;
						CollecteeMasterErrorReportCsvDTO errorCollecteeMasterDTO = new CollecteeMasterErrorReportCsvDTO();
						BeanUtils.copyProperties(collecteeRow, errorCollecteeMasterDTO);
						errorCollecteeMasterDTO.setReason("COLLECTEE STATUS IS EMPTY OR INVALID");
						errorCollecteeMasterDTO.setCollectorCode(collecteeRow.getCollectorCode());
						errorCollecteeMasterDTO.setSerialNumber(String.valueOf(serialNumberForErrorCount));
						errorCollecteeMasterDTO.setNameOfTheCollectee(collecteeRow.getNameOfTheCollectee());
						listErrorReports.add(errorCollecteeMasterDTO);
					}
				}
			} // end of while
		} else {
			tcsBatch.setStatus("Failed");
		}
		if (!listCollectee.isEmpty()) {
			int duplicateRecordsCount = collecteeBulkService.processCollecteeMasterRecords(listCollectee, collectorPan,
					tcsBatch, tenantId, userName, collectorTan);
			// increment duplicate count if same deductee in database exists with same
			// sections and pan then
			// marking as duplicate.
			duplicateCount += duplicateRecordsCount;
			int processedRecordsCount = listCollectee.size();

			// Generating deductee pan file and uploading to pan validation
			MultipartFile panFile = collecteeBulkService.generateCollecteePanXlsxReport(listCollectee);
			String panUrl = blob.uploadExcelToBlob(panFile, tenantId);
			tcsBatch.setOtherFileUrl(panUrl);
			processedCount = (long) processedRecordsCount;
			tcsBatch.setStatus("Processed");
		}
		logger.info("Processed Count : {}", processedCount);
		tcsBatch.setRowsCount(totalRecordsCount);
		tcsBatch.setSuccessCount(totalRecordsCount);
		tcsBatch.setFailedCount(errorCount);
		tcsBatch.setProcessed(processedCount.intValue());
		tcsBatch.setDuplicateCount(0L);
		tcsBatch.setProcessEndTime(new Date());
		tcsBatch.setModifiedDate(new Date());

		File deducteeCsvFile = null;
		if (residentIndicator && !listErrorReports.isEmpty()) {

			ArrayList<String> headerNames = new ArrayList<>(Arrays.asList("COLLECTOR TAN", "ERROR MESSAGE",
					"SEQUENCE NUMBER", "SOURCE IDENTIFIER", "SOURCE FILE NAME", "COLLECTOR CODE",
					"NAME OF THE COLLECTOR", "COLLECTOR PAN", "COLLECTOR TAN", "COLLECTEE CODE",
					"NAME OF THE COLLECTEE", "NON-RESIDENT COLLECTEE INDICATOR", "COLLECTEE PAN",
					"COLLECTEE AADHAR NUMBER", "COLLECTEE STATUS", "COLLECTEE TYPE", "DISTRIBUTION CHANNEL",
					"EMAIL ADDRESS", "PHONE NUMBER", "FLAT DOOR No", "NAME BUILDING VILLAGE", "ROAD STREET",
					"AREA LOCALITY", "TOWN DISTRICT", "STATE", "COUNTRY", "PIN CODE", "NATURE OF INCOME", "TCS SECTION",
					"TCS RATE", "TDS SECTION", "TDS RATE", "APPLICABLE FROM", "APPLICABLE TO",
					"Number of the certificate u/s 206C issued by the AO for lower collection of tax",
					"NO COLLECTTION DECLARATION AS PER FORM 27C", "BALANCES FOR SECTION 206C",
					"Advance Balances as on 30 September 2020 for section 206C(1H),",
					"Collections Balances as on 30 September 2020 for section 206C(1H)", "Current Balance Year",
					"Current Balance Month", "Advances As Of March", "Previous Balance Year", "Previous Balance Month",
					"USER DEFINED 1", "USER DEFINED 2", "USER DEFINED 3", "USER DEFINED 4", "USER DEFINED 5"));
			Workbook wrkbook = collecteeBulkService.collecteeMasterXlsxReport(listErrorReports, collectorTan,
					collectorPan, headerNames);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			wrkbook.save(baout, SaveFormat.XLSX);
			deducteeCsvFile = new File(FilenameUtils.getBaseName(file.getOriginalFilename()) + "_" + UUID.randomUUID()
					+ "_CSV_Error_Report.xlsx");
			FileUtils.writeByteArrayToFile(deducteeCsvFile, baout.toByteArray());
			baout.close();
			tcsBatch.setStatus("Processed");
		}
		tcsBatch = collecteeTcsBatchUpload(tcsBatch, file, collectorTan, assessmentYear, assessmentMonth, userName,
				deducteeCsvFile, tenantId);
		return tcsBatch;

	}

	/**
	 * 
	 * @param tcsBatch
	 * @param mFile
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param file
	 * @param tenant
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public TCSBatchUpload collecteeTcsBatchUpload(TCSBatchUpload tcsBatch, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		String errorFp = null;
		if (file != null) {
			errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
		}
		int month = assessmentMonthPlusOne;
		tcsBatch.setAssessmentMonth(month);
		tcsBatch.setErrorFilePath(errorFp);
		tcsBatch.setModifiedDate(new Timestamp(new Date().getTime()));
		tcsBatch.setModifiedBy(userName);
		tcsBatch.setActive(true);
		logger.info("Saving batch upload entity : {}", tcsBatch);
		tcsBatch = tcsBatchUploadDAO.save(tcsBatch);
		return tcsBatch;
	}

	/**
	 * 
	 * @param collectorPan
	 * @param pagination
	 * @param collecteeName
	 * @param collecteeCode
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public CommonDTO<CollecteeMasterDTO> getListOfCollectee(String collectorPan, Pagination pagination,
			String collecteeName, String collecteeCode)
			throws IllegalAccessException, InvocationTargetException, JsonMappingException, JsonProcessingException {
		List<CollecteeMasterDTO> listCollecteeMasterDTO = new ArrayList<>();
		CollecteeMasterDTO collecteeMasterDTO = null;
		logger.info("collectee name ---- : {}", collecteeName);
		logger.info("collectee code ---- : {}", collecteeCode);
		List<CollecteeMaster> collecteeList = new ArrayList<>();
		if ("nocollecteename".equalsIgnoreCase(collecteeName) && "nocollecteecode".equalsIgnoreCase(collecteeCode)) {
			collecteeList = collecteeMasterDAO.findAllByPan(collectorPan, pagination);
		} else if (!"nocollecteename".equalsIgnoreCase(collecteeName)
				&& "nocollecteecode".equalsIgnoreCase(collecteeCode)) {
			collecteeList = collecteeMasterDAO.findAllByCollecteeNamePan(collectorPan, collecteeName, pagination);
		} else if (!"nocollecteecode".equalsIgnoreCase(collecteeCode)
				&& "nocollecteename".equalsIgnoreCase(collecteeName)) {
			collecteeList = collecteeMasterDAO.findAllByCollecteeCode(collectorPan, collecteeCode, pagination);
		} else if (!"nocollecteename".equalsIgnoreCase(collecteeName)
				&& !"nocollecteecode".equalsIgnoreCase(collecteeCode)) {
			collecteeList = collecteeMasterDAO.findAllByCollecteeNameAndCode(collecteeName, collectorPan, collecteeCode,
					pagination);
		}
		logger.info("COLLECTEES SIZE: {}", collecteeList.size());
		if (!collecteeList.isEmpty() && collecteeList != null) {
			for (CollecteeMaster collecteeMaster : collecteeList) {
				collecteeMasterDTO = new CollecteeMasterDTO();
				TCSAdditionalSectionDTO additionalSections = null;
				Set<TCSAdditionalSectionDTO> additionalSectionsSet = new HashSet<>();
				BeanUtils.copyProperties(collecteeMasterDTO, collecteeMaster);
				Map<String, BigDecimal> additionalSectionss = null;
				if (collecteeMaster.getAdditionalSections() != null) {
					additionalSectionss = objectMapper.readValue(collecteeMaster.getAdditionalSections(),
							new TypeReference<Map<String, BigDecimal>>() {
							});
				}
				if (collecteeMaster.getIsEligibleForMultipleSections() != null && additionalSectionss != null) {
					for (Map.Entry<String, BigDecimal> entry : additionalSectionss.entrySet()) {
						additionalSections = new TCSAdditionalSectionDTO();
						String section = StringUtils.substringBefore(entry.getKey(), "-");
						String noi = StringUtils.substringAfter(entry.getKey(), "-");
						additionalSections.setSection(section);
						additionalSections.setNatureOfIncome(noi);
						additionalSections.setRate(entry.getValue());
						additionalSectionsSet.add(additionalSections);
					}
				}
				collecteeMasterDTO.setTcsadditionalSections(additionalSectionsSet);
				listCollecteeMasterDTO.add(collecteeMasterDTO);
			}
		}
		BigInteger collecteeCount = collecteeMasterDAO.getAllCollecteeMasterCount(collectorPan);
		PagedData<CollecteeMasterDTO> pagedData = new PagedData<>(listCollecteeMasterDTO, collecteeList.size(),
				pagination.getPageNumber(),
				collecteeCount.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		CommonDTO<CollecteeMasterDTO> collecteeDTO = new CommonDTO<>();
		collecteeDTO.setResultsSet(pagedData);
		collecteeDTO.setCount(collecteeCount);
		return collecteeDTO;
	}

	/**
	 * 
	 * @param collectorPan
	 * @return
	 */
	public Set<String> getCollecteeNames(String collectorPan) {
		Set<String> collecteeNames = new HashSet<>();
		List<CollecteeMaster> listOfCollectee = collecteeMasterDAO.getCollectorPan(collectorPan);
		if (!listOfCollectee.isEmpty() && listOfCollectee != null) {
			for (CollecteeMaster collectee : listOfCollectee) {
				collecteeNames.add(collectee.getNameOfTheCollectee());
			}
		}
		return collecteeNames;
	}

	/**
	 * This method for get all collectee codes.
	 * 
	 * @param collectorPan
	 * @return
	 */
	public Set<String> getCollecteeCodes(String collectorPan) {
		Set<String> collecteeCode = new HashSet<>();
		List<CollecteeMaster> listOfCollectee = collecteeMasterDAO.getCollectorPan(collectorPan);
		if (!listOfCollectee.isEmpty() && listOfCollectee != null) {
			for (CollecteeMaster collectee : listOfCollectee) {
				collecteeCode.add(collectee.getCollecteeCode());
			}
		}
		return collecteeCode;
	}

	/**
	 * gets collectee status based on collectee code
	 * 
	 * @param collectorCode
	 * @param tan
	 * @param pan
	 * @return
	 */
	public String getCollecteeType(String collectorCode, String tan, String pan) {
		List<String> listCollecteeStatus = collecteeMasterDAO.getCollecteeType(collectorCode, tan, pan);
		if (!listCollecteeStatus.isEmpty()) {
			return listCollecteeStatus.get(0);
		} else {
			return StringUtils.EMPTY;
		}
	}

	/**
	 * This method for get collectee master data based on enter keyword
	 * 
	 * @param collectorPan
	 * @param keyenetered
	 * @param type
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public Set<String> getCollecteeMasterBasedOnKeyEntered(String collectorPan, String keyenetered, String type) {
		List<CollecteeMaster> response = new ArrayList<>();
		Set<String> collecteeList = new HashSet<>();
		if ("collecteeName".equalsIgnoreCase(type)) {
			response = collecteeMasterDAO.getCollecteeMasterBasedOnKeyEnteredName(collectorPan, keyenetered);
			logger.info("collectee response ---- : {}", response);
			if (!response.isEmpty()) {
				for (CollecteeMaster collectee : response) {
					collecteeList.add(collectee.getNameOfTheCollectee());
				}
			}
		} else {
			response = collecteeMasterDAO.getCollecteeMasterBasedOnKeyEnteredCode(collectorPan, keyenetered);
			logger.info("collectee response ---- : {}", response);
			if (!response.isEmpty()) {
				for (CollecteeMaster collectee : response) {
					collecteeList.add(collectee.getCollecteeCode());
				}
			}
		}
		return collecteeList;
	}

	/**
	 * 
	 * @param collectorPan
	 * @return
	 */
	public List<CollecteeNamePanDTO> getCollecteeMaster(String collectorPan) {
		List<CollecteeNamePanDTO> collecteeDto = new ArrayList<>();
		List<CollecteeMaster> listCollectee = collecteeMasterDAO.findAllByCollecteePans(collectorPan);
		if (!listCollectee.isEmpty()) {
			for (CollecteeMaster collectee : listCollectee) {
				if (StringUtils.isNotBlank(collectee.getCollecteePan())) {
					CollecteeNamePanDTO dto = new CollecteeNamePanDTO();
					dto.setId(collectee.getId());
					dto.setCollecteeName(collectee.getNameOfTheCollectee());
					dto.setCollecteePan(collectee.getCollecteePan());
					collecteeDto.add(dto);
				}
			}
		}
		return collecteeDto;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param year
	 * @param month
	 * @return
	 */
	public String getCollecteePanStatus(String deductorPan, Integer year, Integer month) {
		String startDate = CommonUtil.getQueryDate(CommonUtil.getMonthStartDate(year, month - 1));
		String endDate = CommonUtil.getQueryDate(CommonUtil.getMonthEndDate(year, month - 1));
		return getCollecteePansStatus(deductorPan, startDate, endDate);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public String getCollecteePansStatus(String deductorPan, String startDate, String endDate) {
		long countValidPan = collecteeMasterDAO.countCollecteeMasterPanStatusValid(deductorPan, startDate, endDate);
		logger.info("Total collectee master valid pan status: {}", countValidPan);

		long countInValidPan = collecteeMasterDAO.countCollecteeMasterPanStatusInValid(deductorPan, startDate, endDate);
		logger.info("Total collectee master invalid pan status: {}", countInValidPan);

		if (countValidPan == 0 && countInValidPan == 0) {
			return ActivityTrackerStatus.NORECORDS.name();
		} else if (countInValidPan > 0) {
			return ActivityTrackerStatus.PENDING.name();
		} else if (countValidPan > 0 && countInValidPan == 0) {
			return ActivityTrackerStatus.VALIDATED.name();
		} else {
			return StringUtils.EMPTY;
		}
	}

	public void collecteeNoiThresholdLedgerUpdate(CollecteeNoiThresholdLedger collecteeNoiThresholdLedger,
			BigDecimal amount, String userName, String collecteePan, String collectorPan) {
		/**
		 * step-1 check the collectee code is present or not,if present then the user is
		 * just updating the pan for 1st time step-2 check with the pan if any record is
		 * there in NOI table or not step-3 if no record present then just update the
		 * NOI table without adding amount step-4 if record is present in NOI then the
		 * pan is associated with another collectee,now deactivate the record having
		 * current collectee code,add the amount to existing NOI record having the pan
		 * and update that record
		 * 
		 */

		collecteeNoiThresholdLedger.setId(collecteeNoiThresholdLedger.getId());
		collecteeNoiThresholdLedger.setModifiedDate(new Timestamp(new Date().getTime()));
		collecteeNoiThresholdLedger.setModifiedBy(userName);
		collecteeNoiThresholdLedger.setLastUpdatedDate(new Timestamp(new Date().getTime()));

		// if null user is editing the record with pan
		if (collecteeNoiThresholdLedger.getCollecteeCode() != null) {
			List<CollecteeNoiThresholdLedger> listcntl = collecteeNoiThresholdLedgerDAO.findByCollecteePan(collecteePan,
					collectorPan);

			// update the record with pan
			if (listcntl.isEmpty()) {
				collecteeNoiThresholdLedger.setCollecteePan(collecteePan);
				collecteeNoiThresholdLedger.setCollecteeCode(null);
				collecteeNoiThresholdLedgerDAO.update(collecteeNoiThresholdLedger);
			} else {// if pan is present deactivate the current record
				collecteeNoiThresholdLedger.setActive(false);
				collecteeNoiThresholdLedgerDAO.update(collecteeNoiThresholdLedger);

				// update the existing record by adding the ammount
				CollecteeNoiThresholdLedger noiLedger = listcntl.get(0);
				noiLedger.setModifiedDate(new Timestamp(new Date().getTime()));
				noiLedger.setModifiedBy(userName);
				noiLedger.setLastUpdatedDate(new Timestamp(new Date().getTime()));
				BigDecimal amountUtilized = noiLedger.getAmountUtilized() == null ? BigDecimal.ZERO
						: noiLedger.getAmountUtilized();
				BigDecimal amountUtilizedFromCurrentRecord = collecteeNoiThresholdLedger.getAmountUtilized() == null
						? BigDecimal.ZERO
						: collecteeNoiThresholdLedger.getAmountUtilized();
				BigDecimal finalAmount = amountUtilized.add(amountUtilizedFromCurrentRecord);
				int value = (finalAmount).compareTo(BigDecimal.valueOf(5000000));
				if (value == 1) {
					noiLedger.setThresholdReached(true);
				} else {
					noiLedger.setThresholdReached(false);
				}
				noiLedger.setAmountUtilized(finalAmount);
				collecteeNoiThresholdLedgerDAO.update(noiLedger);

			}
		} else {

			BigDecimal amountUtilized = collecteeNoiThresholdLedger.getAmountUtilized() == null ? BigDecimal.ZERO
					: collecteeNoiThresholdLedger.getAmountUtilized();
			BigDecimal finalAmount = amountUtilized.add(amount);
			int value = (finalAmount).compareTo(BigDecimal.valueOf(5000000));
			if (value == 1) {
				collecteeNoiThresholdLedger.setThresholdReached(true);
			} else {
				collecteeNoiThresholdLedger.setThresholdReached(false);
			}
			collecteeNoiThresholdLedger.setAmountUtilized(finalAmount);
			collecteeNoiThresholdLedgerDAO.update(collecteeNoiThresholdLedger);
		}

	}

	/**
	 * 
	 * @param deductorPan
	 * @param assessmentYear
	 * @return
	 */
	public TCSBatchUpload collecteeMoveToNextYear(String collectorPan, Integer assessmentYear, String collectorTan,
			String userName) {

		Integer totalCount = collecteeNoiThresholdLedgerDAO.insertSelectForNoi(assessmentYear, collectorPan);

		logger.info("{} records moved from {} to {}", totalCount, assessmentYear, assessmentYear + 1);

		TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
		tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
		tcsBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
		tcsBatchUpload.setCreatedBy(userName);
		tcsBatchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
		tcsBatchUpload.setActive(true);
		tcsBatchUpload.setCollectorMasterTan(collectorTan);
		tcsBatchUpload.setAssessmentMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
		tcsBatchUpload.setAssessmentYear(assessmentYear);
		tcsBatchUpload.setSuccessCount(0L);
		tcsBatchUpload.setFailedCount(0L);
		tcsBatchUpload.setRowsCount(0L);
		tcsBatchUpload.setProcessed(0);
		tcsBatchUpload.setMismatchCount(0L);
		tcsBatchUpload.setSha256sum(null);
		tcsBatchUpload.setStatus("Processed");
		tcsBatchUpload.setUploadType(UploadTypes.MOVE_TO_NEXT_YEAR.name());
		tcsBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
		tcsBatchUploadDAO.save(tcsBatchUpload);
		return tcsBatchUpload;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param collecteePan
	 * @param tan
	 * @return
	 */
	public KYCDetails getKycDetailsBasedOnPan(String deductorPan, String collecteePan, String tan) {
		List<KYCDetails> kyc = kycDetailsDAO.getKycDetailsBasedOnPan(deductorPan, tan, collecteePan);
		if (!kyc.isEmpty()) {
			return kyc.get(0);
		} else {
			return new KYCDetails();
		}
	}

}
