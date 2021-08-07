package com.ey.in.tds.onboarding.service.deductor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.masters.DividendOnboardingInfoMetaData.ClientSpecificRule;
import com.ey.in.tds.common.domain.masters.DividendOnboardingInfoMetaData.PrepForm15CaCb;
import com.ey.in.tds.common.domain.masters.DividendOnboardingInfoMetaData.RuleApplicability;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterExcelDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterExcelErrorDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterScopeExcelDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterScopeExcelErrorDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorOnboardingInformationDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorTanAddress;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.jdbc.dao.DeductorOnboardingInfoDAO;
import com.ey.in.tds.jdbc.dao.DeductorTanAddressDAO;
import com.ey.in.tds.onboarding.service.util.excel.DeductorExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.DeductorMasterExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.DeductorMasterScopeExcel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

@Service
public class DeducorMasterBulkService {
	private final Logger logger = LoggerFactory.getLogger(DeductorMasterService.class);

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	@Autowired
	private DeductorTanAddressDAO deductorTanAddressDAO;

	@Autowired
	private DeductorOnboardingInfoDAO deductorOnboardingInfoDAO;
	
	@Autowired
	private BatchUploadDAO batchUploadDAO;
	
	@Autowired
	private BlobStorage blob;
	
	@Autowired
	private MastersClient mastersClient;
	
	/**
	 * to achieve asynchronus
	 * 
	 * @param worksheet
	 * @param worksheet2
	 * @param multiPartFile
	 * @param sha256
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param batchUpload
	 * @return
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InvalidKeyException
	 */
	@Async
	public void asyncProcessDeductorMaster(XSSFSheet worksheet, XSSFSheet worksheet2,
			MultipartFile multiPartFile, String sha256, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonth, String userName, String tenantId, String deductorPan, BatchUpload batchUpload)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException {
		logger.info("Service method started to process deductor onbording excel {}");
         MultiTenantContext.setTenantId(tenantId);
		 processDeductorMaster(worksheet, worksheet2, multiPartFile, sha256, deductorTan, assesssmentYear,
				assessmentMonth, userName, tenantId, deductorPan, batchUpload);

	}

	/**
	 * 
	 * @param workbook
	 * @param multiPartFile
	 * @param sha256
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param batchUpload
	 * @return
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InvalidKeyException
	 */
	@Transactional
	private void processDeductorMaster(XSSFSheet worksheet, XSSFSheet worksheet2, MultipartFile multiPartFile,
			String sha256, String deductorTan, Integer assesssmentYear, Integer assessmentMonth, String userName,
			String tenantId, String deductorPan, BatchUpload batchUpload)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		ArrayList<DeductorMasterExcelErrorDTO> deductorErrorList = new ArrayList<>();
		ArrayList<DeductorMasterScopeExcelErrorDTO> deductorScopeErrorList = new ArrayList<>();
		File deductorErrorFile = null;
		try {
			DeductorMasterExcel deductorData = new DeductorMasterExcel(worksheet);
			DeductorMasterScopeExcel deductorScopeData = new DeductorMasterScopeExcel(worksheet2);

			batchUpload.setSha256sum(sha256);
			batchUpload.setMismatchCount(0L);
			long dataRowsCount = deductorData.getDataRowsCount();
			long scopeDataRowsCount = deductorScopeData.getDataRowsCount();
			batchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			Long duplicateCount = 0L;
			Long successCount = 0L;
			Long totalRecordCount = 0l;
			// deductor master excel
			for (int rowIndex = 3; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<DeductorMasterExcelErrorDTO> errorDTO = null;
				boolean isNotValid = false;
				totalRecordCount++;
				try {
					errorDTO = deductorData.validate(rowIndex);
				} catch (Exception e) {
					logger.error("Unable validate row number " + rowIndex + " due to "
							+ ExceptionUtils.getRootCauseMessage(e), e);
					++errorCount;
					continue;
				}
				if (errorDTO.isPresent()) {
					++errorCount;
					deductorErrorList.add(errorDTO.get());
				} else {
					try {
						DeductorTanAddress deductorTanAddress = null;
						DeductorMasterExcelDTO deductor = deductorData.get(rowIndex);
						logger.info("Retrieved deductor master data from row " + rowIndex + "{}");

						DeductorMaster deductorMaster = new DeductorMaster();
						deductorMaster.setCode(deductor.getDeductorCode());
						deductorMaster.setDeductorSalutation(deductor.getDeductorSalutation());
						deductorMaster.setName(deductor.getDeductorName());
						deductorMaster.setApplicableFrom(deductor.getApplicableFrom());
						deductorMaster.setApplicableTo(deductor.getApplicableTo());
						deductorMaster.setEmail(deductor.getEmail());
						deductorMaster.setPhoneNumber(deductor.getMobileNumber());
						deductorMaster.setCreatedBy(userName);
						deductorMaster.setModifiedBy(userName);
						deductorMaster.setCreatedDate(new Date());
						deductorMaster.setModifiedDate(new Date());
						deductorMaster.setActive(true);
						if (deductor.getDeductorHaveMoreThanOneBranch() == null) {
							deductorMaster.setHaveMoreThanOneBranch(false);
						} else {
							deductorMaster.setHaveMoreThanOneBranch(deductor.getDeductorHaveMoreThanOneBranch());
						}
						deductorMaster.setResidentialStatus(deductor.getDeductorResidentialStatus());
						deductorMaster.setStatus(deductor.getDeductorStatus());
						deductorMaster.setType(deductor.getDeductorType());
						deductorMaster.setModeOfPayment(deductor.getModeOfPayment());
						deductorMaster.setDueDateOfTaxPayment(deductor.getDueDateOfTaxPayment());
						deductorMaster.setEmailAlternate(deductor.getEmailAlternate());
						deductorMaster.setPhoneNumberAlternate(deductor.getMobileNumberAlternate());
						deductorMaster.setGstin(deductor.getGoodsAndServicesTaxNumber());
						deductorMaster.setDvndDeductorTypeName(deductor.getDvndDeductorTypeName());
						DeductorMasterExcelErrorDTO deductorErrorDTO = deductorData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(deductorErrorDTO.getReason())) {
							deductorErrorDTO.setReason("");
						}
						// check for module type
						if (deductor.getTdsModule().equals("N") && deductor.getTdsModule().equals("N")) {
							deductorErrorDTO.setReason(
									deductorErrorDTO.getReason() + "Select at least one module tds or tcs" + "\n");
							isNotValid = true;
						} else if (deductor.getTdsModule().equals("Y") && deductor.getTcsModule().equals("Y")) {
							deductorMaster.setModuleType("1,2");
						} else if (deductor.getTdsModule().equals("Y")) {
							deductorMaster.setModuleType("1");
						} else {
							deductorMaster.setModuleType("2");
						}

						List<DeductorMaster> deductorMasterObj = deductorMasterDAO
								.findBasedOnDeductorCode(deductor.getDeductorCode());
						if (!deductorMasterObj.isEmpty() && deductorMasterObj != null) {
							deductorErrorDTO.setReason(
									deductorErrorDTO.getReason() + "Cannot add Duplicate Deductor Code " + "\n");
							isNotValid = true;
						}
						if (!deductor.getDeductorPan().matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
							deductorErrorDTO.setReason(deductorErrorDTO.getReason() + "Cannot add Invalid PAN " + "\n");
							isNotValid = true;
						} else {
							deductorMaster.setPanField(deductor.getDeductorPan());
						}

						// preparing and validating deductor tan address data
						deductorTanAddress = prepareTanAdressObject(deductor, deductorErrorDTO, userName, isNotValid);

						if (isNotValid == false && StringUtils.isBlank(deductorErrorDTO.getReason())) {
							deductorMaster = deductorMasterDAO.insert(deductorMaster);
							deductorTanAddressDAO.save(deductorTanAddress, deductorMaster.getDeductorMasterId());
							logger.info("Deductor record saved for row  no " + rowIndex + "{}");
							++successCount;
						} else {
							++errorCount;
							deductorErrorList.add(deductorErrorDTO);
						}

					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						DeductorMasterExcelErrorDTO problematicDataError = deductorData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
						}
						deductorErrorList.add(problematicDataError);
						++errorCount;
					}
				}
			} // end for loop

			// deductor master scope excel.
			for (int rowIndex = 3; rowIndex <= scopeDataRowsCount; rowIndex++) {
				Optional<DeductorMasterScopeExcelErrorDTO> errorDTO = null;
				boolean isNotValid = false;
				totalRecordCount++;
				try {
					errorDTO = deductorScopeData.validate(rowIndex);
					logger.info("errorDTO :{}", errorDTO.toString());
				} catch (Exception e) {
					logger.error("Unable validate row number " + rowIndex + " due to "
							+ ExceptionUtils.getRootCauseMessage(e), e);
					++errorCount;
					continue;
				}
				if (errorDTO.isPresent()) {
					++errorCount;
					deductorScopeErrorList.add(errorDTO.get());
				} else {
					try {
						DeductorMasterScopeExcelDTO deductorScope = deductorScopeData.get(rowIndex);
						DeductorOnboardingInformationDTO deductorInfo = new DeductorOnboardingInformationDTO();
						String errorReason = StringUtils.EMPTY;
						Boolean isDeductorPresent = null;
						ObjectMapper objectMapper = new ObjectMapper();
						logger.info("Retrieved deductor master data from row " + rowIndex + "{}");

						// checking whether the deductor pan is present or not in db
						isDeductorPresent = deductorMasterDAO
								.getDeductorCountByDeductorPan(deductorScope.getDeductorPan());

						if (isDeductorPresent == true) {

							deductorInfo.setPan(deductorScope.getDeductorPan());
							deductorInfo.setActive(true);
							deductorInfo.setCreatedDate(new Date());
							deductorInfo.setModifiedDate(new Date());
							deductorInfo.setCreatedBy(userName);
							deductorInfo.setModifiedBy(userName);

							// Scope Process
							// checking if any scope process is having y( any scope is selected)
							if (!(deductorScope.getPanLdcValidation()
									+ deductorScope.getInvoicesProvisionsAdvancesDetermination()
									+ deductorScope.getRatePredication() + deductorScope.getMonthlyCompliance()
									+ deductorScope.getQuarterlyReturns() + deductorScope.getClause34a()
									+ deductorScope.getTracesDefaults() + deductorScope.getDividends()
									+ deductorScope.getFilingform15CACB() + deductorScope.getFilingform15GH())
											.contains("Y")) {
								errorReason = errorReason + "Please Select at least one option in scope process" + "\n";
								isNotValid = true;
							} else {
								List<Integer> ipp = new ArrayList<>();
								if (StringUtils.isNotBlank(deductorScope.getPanLdcValidation())
										&& deductorScope.getPanLdcValidation().equals("Y")) {
									ipp.add(1);
								}
								if (StringUtils.isNotBlank(deductorScope.getInvoicesProvisionsAdvancesDetermination())
										&& deductorScope.getInvoicesProvisionsAdvancesDetermination().equals("Y")) {
									ipp.add(2);
								}
								if (StringUtils.isNotBlank(deductorScope.getRatePredication())
										&& deductorScope.getRatePredication().equals("Y")) {
									ipp.add(3);
								}
								if (StringUtils.isNotBlank(deductorScope.getMonthlyCompliance())
										&& deductorScope.getMonthlyCompliance().equals("Y")) {
									ipp.add(4);
								}
								if (StringUtils.isNotBlank(deductorScope.getQuarterlyReturns())
										&& deductorScope.getQuarterlyReturns().equals("Y")) {
									ipp.add(5);
								}
								if (StringUtils.isNotBlank(deductorScope.getClause34a())
										&& deductorScope.getClause34a().equals("Y")) {
									ipp.add(6);
								}
								if (StringUtils.isNotBlank(deductorScope.getTracesDefaults())
										&& deductorScope.getTracesDefaults().equals("Y")) {
									ipp.add(7);
								}
								if (StringUtils.isNotBlank(deductorScope.getDividends())
										&& deductorScope.getDividends().equals("Y")) {
									ipp.add(8);
									deductorInfo.setDvndEnabled(true);
								} else {
									deductorInfo.setDvndEnabled(false);
								}
								if (StringUtils.isNotBlank(deductorScope.getFilingform15CACB())
										&& deductorScope.getFilingform15CACB().equals("Y")) {
									ipp.add(9);
								}
								if (StringUtils.isNotBlank(deductorScope.getFilingform15GH())
										&& deductorScope.getFilingform15GH().equals("Y")) {
									ipp.add(10);
									deductorInfo.setDvndFileForm15gh(true);
								} else {
									deductorInfo.setDvndFileForm15gh(false);
								}
								objectMapper = new ObjectMapper();
								String ipps = objectMapper.writeValueAsString(ipp);
								logger.info("ipps :{}", ipps);
								deductorInfo.setIpp(ipps);
							}

							// Type of Invoice Process(tip)
							if (!(deductorScope.getSap() + deductorScope.getExcel() + deductorScope.getPdf())
									.contains("Y")) {
								errorReason = errorReason
										+ "Please Select at least one option in type of invoice process \n";
								isNotValid = true;
							} else {
								List<Integer> invoiceProcess = new ArrayList<>();
								if (StringUtils.isNotBlank(deductorScope.getSap())
										&& deductorScope.getSap().equals("Y")) {
									invoiceProcess.add(1);
								}
								if (StringUtils.isNotBlank(deductorScope.getExcel())
										&& deductorScope.getExcel().equals("Y")) {
									invoiceProcess.add(2);
								}
								if (StringUtils.isNotBlank(deductorScope.getPdf())
										&& deductorScope.getPdf().equals("Y")) {
									invoiceProcess.add(3);
								}
								String tip = objectMapper.writeValueAsString(invoiceProcess);
								logger.info("tip :{}", tip);
								deductorInfo.setTif(tip);
							}

							// Invoice Process Priority
							if ((deductorScope.getInvoiceDescription() == null ? 0 : 1)
									+ (deductorScope.getPoDescription() == null ? 0 : 1)
									+ (deductorScope.getGlDescription() == null ? 0 : 1)
									+ (deductorScope.getSac() == null ? 0 : 1)
									+ (deductorScope.getVendorMaster() == null ? 0 : 1) < 3) {
								errorReason = errorReason
										+ "Please Select at least Three option in type of invoice process with priorities \n";
								isNotValid = true;
							} else {
								Map<String, Integer> map = new HashMap<>();
								List<Integer> ppa = new ArrayList<>();
								if (deductorScope.getInvoiceDescription() != null) {
									map.put("InvoiceDesc", deductorScope.getInvoiceDescription());
									ppa.add(1);
								}
								if (deductorScope.getPoDescription() != null) {
									map.put("PODesc", deductorScope.getPoDescription());
									ppa.add(2);
								}
								if (deductorScope.getGlDescription() != null) {
									map.put("GLDesc", deductorScope.getGlDescription());
									ppa.add(3);
								}
								if (deductorScope.getSac() != null) {
									map.put("SACDesc", deductorScope.getSac());
									ppa.add(4);
								}
								if (deductorScope.getVendorMaster() != null) {
									map.put("VendorMaster", deductorScope.getVendorMaster());
									ppa.add(5);
								}
								String priority = map.keySet().stream()
										.map(key -> '"' + key + '"' + ":" + '"' + map.get(key) + '"')
										.collect(Collectors.joining(", ", "{", "}"));
								logger.info("priority {}:", priority);
								deductorInfo.setPriority(priority);
								String ppas = objectMapper.writeValueAsString(ppa);
								deductorInfo.setPpa(ppas);
							}
							// Provision Tracking Periods
							if (deductorScope.getProvisionTrackingPeriods() == null) {
								deductorInfo.setProvisionTracking("Monthly");
							} else if (deductorScope.getProvisionTrackingPeriods().equals("Yearly")) {
								deductorInfo.setProvisionTracking("Yearly");
							} else if (deductorScope.getProvisionTrackingPeriods().equals("Quarterly")) {
								deductorInfo.setProvisionTracking("Quarterly");
							} else {
								deductorInfo.setProvisionTracking("Monthly");
							}
							// Provision Processing
							if (deductorScope.getProvisionProcessing() == null) {
								deductorInfo.setProvisionProcessing("VENDORSECTION");
							} else if (deductorScope.getProvisionProcessing().equals("POTRACKING")) {
								deductorInfo.setProvisionProcessing("POTRACKING");
							} else if (deductorScope.getProvisionProcessing().startsWith("FIFO")) {
								deductorInfo.setProvisionProcessing("FIFO");
							} else {
								deductorInfo.setProvisionProcessing("VENDORSECTION");
							}
							
							// Advance Processing
							if (deductorScope.getAdvanceProcessing() == null) {
								deductorInfo.setAdvanceProcessing("FIFO");
							} else if (deductorScope.getAdvanceProcessing().equals("Advance indicator based")) {
								deductorInfo.setAdvanceProcessing("ADVANCEINDICATOR");
							} else if (deductorScope.getAdvanceProcessing().equalsIgnoreCase("PO Based")) {
								deductorInfo.setAdvanceProcessing("PO");
							} else if (deductorScope.getAdvanceProcessing().equalsIgnoreCase("Hybrid")) {
								deductorInfo.setAdvanceProcessing("HYBRID");
							} else {
								deductorInfo.setAdvanceProcessing("FIFO");
							}

							// Credit note processing (crnp)
							if (deductorScope.getCreditNoteProcessing() == null) {
								deductorInfo.setCrpt("A");
							} else if (deductorScope.getCreditNoteProcessing().equals("Auto Adjust Credit Notes")) {
								deductorInfo.setCrpt("AA");
							} else if (deductorScope.getCreditNoteProcessing().equals("Reject Credit Notes")) {
								deductorInfo.setCrpt("R");
							} else {
								deductorInfo.setCrpt("A");
							}
							// Consolidated challan processing (cp)
							if (StringUtils.isBlank(deductorScope.getConsolidatedChallanProcessing())) {
								deductorInfo.setCp(false);
							} else if (deductorScope.getConsolidatedChallanProcessing().startsWith("Aggregate")) {
								deductorInfo.setCp(false);
							} else {
								deductorInfo.setCp(true);
							}

							// Enable Annual / Per transaction limit for rate determination ?
							if (StringUtils.isBlank(deductorScope.getEnableAnnualTransactionPerLimit())
									|| deductorScope.getEnableAnnualTransactionPerLimit().equalsIgnoreCase("Enable")) {
								deductorInfo.setInterestCalculationType("30days");
							} else {
								deductorInfo.setInterestCalculationType("calendarMonth");
							}

							// Select type of interest calulation
							if (StringUtils.isBlank(deductorScope.getSelectTypeInterestCalculation())
									|| deductorScope.getSelectTypeInterestCalculation()
											.equalsIgnoreCase("Interest as per 30 day period")) {
								List<String> sections = mastersClient.getnatureofpaymentsections().getBody().getData();
								deductorInfo.setSelectedSectionsForTransactionLimit(sections);
								deductorInfo.setPertransactionlimit("Yes");
							} else {
								deductorInfo.setPertransactionlimit("No");
							}

							// Select rounding off
							if (StringUtils.isBlank(deductorScope.getSelectRoundingOff())
									|| deductorScope.getSelectRoundingOff().equalsIgnoreCase("Decimal Roundoff")) {
								deductorInfo.setRoundoff("Decimal");
							} else if (deductorScope.getSelectRoundingOff()
									.equalsIgnoreCase("Round to nearest 1 Rupee")) {
								deductorInfo.setRoundoff("Roundoffto1");
							} else {
								deductorInfo.setRoundoff("Roundoffto10");
							}

							// dividend processing client specific rules
							Map<ClientSpecificRule, RuleApplicability> dvndClientSpecificRules = new HashMap<>();
							RuleApplicability ruleApplicability = new RuleApplicability();
							if (deductorScope.getKeyStrategicShareHolderA() == null
									|| deductorScope.getKeyStrategicShareHolderA().equals("N")) {
								ruleApplicability.setKeyStrategicShareholders(false);
							} else {
								ruleApplicability.setKeyStrategicShareholders(true);
							}

							if (deductorScope.getNonKeyStrategicShareHolderA() == null
									|| deductorScope.getNonKeyStrategicShareHolderA().equals("N")) {
								ruleApplicability.setAllShareholders(false);
							} else {
								ruleApplicability.setAllShareholders(true);
							}
							dvndClientSpecificRules.put(ClientSpecificRule.TREATY_BENEFITS_IN_ABSENCE_OF_DOCS,
									ruleApplicability);

							ruleApplicability = new RuleApplicability();
							if (deductorScope.getKeyStrategicShareHolderB() == null
									|| deductorScope.getKeyStrategicShareHolderB().equals("N")) {
								ruleApplicability.setKeyStrategicShareholders(false);
							} else {
								ruleApplicability.setKeyStrategicShareholders(true);
							}

							if (deductorScope.getNonKeyStrategicShareHolderB() == null
									|| deductorScope.getNonKeyStrategicShareHolderB().equals("N")) {
								ruleApplicability.setAllShareholders(false);
							} else {
								ruleApplicability.setAllShareholders(true);
							}
							dvndClientSpecificRules.put(ClientSpecificRule.TREATY_BENEFITS_BY_INDEMNITY,
									ruleApplicability);

							ruleApplicability = new RuleApplicability();
							if (deductorScope.getKeyStrategicShareHolderC() == null
									|| deductorScope.getKeyStrategicShareHolderC().equals("N")) {
								ruleApplicability.setKeyStrategicShareholders(false);
							} else {
								ruleApplicability.setKeyStrategicShareholders(true);
							}

							if (deductorScope.getNonKeyStrategicShareHolderC() == null
									|| deductorScope.getNonKeyStrategicShareHolderC().equals("N")) {
								ruleApplicability.setAllShareholders(false);
							} else {
								ruleApplicability.setAllShareholders(true);
							}
							dvndClientSpecificRules.put(ClientSpecificRule.TREATY_BENEFITS_AS_PER_MFN_CLAUSE,
									ruleApplicability);

							ruleApplicability = new RuleApplicability();
							if (deductorScope.getKeyStrategicShareHolderD() == null
									|| deductorScope.getKeyStrategicShareHolderD().equals("N")) {
								ruleApplicability.setKeyStrategicShareholders(false);
							} else {
								ruleApplicability.setKeyStrategicShareholders(true);
							}

							if (deductorScope.getNonKeyStrategicShareHolderD() == null
									|| deductorScope.getNonKeyStrategicShareHolderD().equals("N")) {
								ruleApplicability.setAllShareholders(false);
							} else {
								ruleApplicability.setAllShareholders(true);
							}
							dvndClientSpecificRules.put(ClientSpecificRule.APPLY_SURCHARGE_AND_CESS_AS_PER_LDC,
									ruleApplicability);
							
							ruleApplicability = new RuleApplicability();
							if (deductorScope.getKeyStrategicShareHolderE() == null
									|| deductorScope.getKeyStrategicShareHolderE().equals("N")) {
								ruleApplicability.setKeyStrategicShareholders(false);
							} else {
								ruleApplicability.setKeyStrategicShareholders(true);
							}

							if (deductorScope.getNonKeyStrategicShareHolderE() == null
									|| deductorScope.getNonKeyStrategicShareHolderE().equals("N")) {
								ruleApplicability.setAllShareholders(false);
							} else {
								ruleApplicability.setAllShareholders(true);
							}
							dvndClientSpecificRules.put(ClientSpecificRule.TREATY_BENEFITS_TO_FII_FPI_GDR,
									ruleApplicability);
							
							ruleApplicability = new RuleApplicability();
							if (deductorScope.getKeyStrategicShareHolderF() == null
									|| deductorScope.getKeyStrategicShareHolderF().equals("N")) {
								ruleApplicability.setKeyStrategicShareholders(false);
							} else {
								ruleApplicability.setKeyStrategicShareholders(true);
							}

							if (deductorScope.getNonKeyStrategicShareHolderF() == null
									|| deductorScope.getNonKeyStrategicShareHolderF().equals("N")) {
								ruleApplicability.setAllShareholders(false);
							} else {
								ruleApplicability.setAllShareholders(true);
							}
							dvndClientSpecificRules.put(ClientSpecificRule.TREATY_BENEFITS_TO_MUTUAL_FUND_BUSINESS_TRUST_DEDUCTOR_TYPE,
									ruleApplicability);
							
							

							// converting into custom format for spark process
							String clientSpecificRule = (String) dvndClientSpecificRules.keySet().stream()
									.map(key -> '"' + key.toString() + '"' + ":" + '"'
											+ dvndClientSpecificRules.get(key) + '"')
									.collect(Collectors.joining(", ", "{", "}"));
							deductorInfo.setStringClientSpecificRules(clientSpecificRule);

							// Is Dividend Distribution Tax paid on dividend declared
							if (deductorScope.getIsDividendDistriutionTaxPaid() == null
									|| deductorScope.getIsDividendDistriutionTaxPaid().equals("N")) {
								deductorInfo.setDvndDdtPaidBeforeEOY(false);
							} else {
								deductorInfo.setDvndDdtPaidBeforeEOY(true);
							}

							// Preparation of Form 15CB
							if (deductorScope.getPreparationOfForm15CB() == null
									|| deductorScope.getPreparationOfForm15CB().startsWith("Only")) {
								deductorInfo.setDvndPrepForm15CaCb(PrepForm15CaCb.ONLY_PART_C_OF_FORM_15_CA);
							} else {
								deductorInfo.setDvndPrepForm15CaCb(PrepForm15CaCb.PART_A_OR_B_OF_FORM_15_CA);
							}

							if (isNotValid == false && StringUtils.isBlank(errorReason)) {
								// update previous and save current deductor onboarding info.
								List<DeductorOnboardingInformationDTO> deductorOnboardingInformation = deductorOnboardingInfoDAO
										.findByDeductorPan(deductorScope.getDeductorPan());
								if (!deductorOnboardingInformation.isEmpty() && deductorOnboardingInformation != null) {
									deductorOnboardingInformation.get(0).setActive(false);
									deductorOnboardingInfoDAO.update(deductorOnboardingInformation.get(0));
									logger.info("updated onbording details for pan " + deductorScope.getDeductorPan()
											+ "{}");
								}
								deductorOnboardingInfoDAO.save(deductorInfo);
								logger.info("Onbording data saved for pan =" + deductorScope.getDeductorPan()
										+ " from row no =" + rowIndex + "{}");
								++successCount;
							} else {
								++errorCount;
								DeductorMasterScopeExcelErrorDTO deductorScopeErrorDTO = deductorScopeData
										.getErrorDTO(rowIndex);
								deductorScopeErrorDTO.setReason(errorReason);
								deductorScopeErrorList.add(deductorScopeErrorDTO);
							}
						} else { // if the pan is not found in DB
							++errorCount;
							DeductorMasterScopeExcelErrorDTO deductorScopeErrorDTO = deductorScopeData
									.getErrorDTO(rowIndex);
							deductorScopeErrorDTO.setReason("No Deductor is found for this Pan");
							deductorScopeErrorList.add(deductorScopeErrorDTO);
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						DeductorMasterScopeExcelErrorDTO problematicDataError = deductorScopeData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
						}
						deductorScopeErrorList.add(problematicDataError);
						++errorCount;
					}
				}
			}
			batchUpload.setRowsCount(totalRecordCount);
			batchUpload.setSuccessCount(successCount);
			batchUpload.setFailedCount(errorCount);
			batchUpload.setProcessedCount(successCount.intValue());
			batchUpload.setDuplicateCount(duplicateCount);
			batchUpload.setStatus("Processed");
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);

			if (!deductorScopeErrorList.isEmpty() || !deductorErrorList.isEmpty()) {
				deductorErrorFile = prepareDeductorMasterErrorFile(multiPartFile.getOriginalFilename(), deductorTan,
						deductorPan, deductorErrorList, deductorScopeErrorList,
						new ArrayList<>(deductorData.getHeaders()), new ArrayList<>(deductorScopeData.getHeaders()));
			}
		} catch (Exception e) {
			logger.error("Exception occurred :", e);
		}
		 deductorBatchUpload(batchUpload, null, deductorTan, assesssmentYear, assessmentMonth, userName,
				deductorErrorFile, tenantId);
	}
	
	/**
	 * 
	 * @param batchUpload
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
	public BatchUpload deductorBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("batch", batchUpload);
		String errorFp = null;
		String path = null;
		if (file != null) {
			errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
			batchUpload.setErrorFilePath(errorFp);
		}
		if (mFile != null) {
			path = blob.uploadExcelToBlob(mFile);
			batchUpload.setFileName(mFile.getOriginalFilename());
			batchUpload.setFilePath(path);
		}
		int month = assessmentMonthPlusOne;
		batchUpload.setAssessmentMonth(month);
		batchUpload.setAssessmentYear(assesssmentYear);
		batchUpload.setDeductorMasterTan(tan);
		batchUpload.setUploadType(UploadTypes.DEDUCTOR_MASTER_EXCEL.name());
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		if (batchUpload.getBatchUploadID() != null) {
			batchUpload.setBatchUploadID(batchUpload.getBatchUploadID());
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}
	
	public DeductorTanAddress prepareTanAdressObject(DeductorMasterExcelDTO deductor,
			DeductorMasterExcelErrorDTO errorDto, String userName, Boolean isNotValid) {
		DeductorTanAddress deductorTanAddress = new DeductorTanAddress();

		deductorTanAddress.setTan(deductor.getTan());
		deductorTanAddress.setCountryName(deductor.getCountry());
		deductorTanAddress.setStateName(deductor.getStates());
		deductorTanAddress.setTownCityDistrict(deductor.getCity());
		deductorTanAddress.setStdCode(deductor.getStdCode());
		deductorTanAddress.setPinCode(deductor.getPinCode());
		deductorTanAddress.setAreaLocality(deductor.getAreaOrLocality());
		deductorTanAddress.setRoadStreetPostoffice(deductor.getRoadOrStreetOrPostOffice());
		deductorTanAddress.setNameBuildingVillage(deductor.getNameOfBuilding());
		deductorTanAddress.setFlatDoorBlockNo(deductor.getFlatOrDoorOrBlockNo());
		// person responsibily details.
		deductorTanAddress.setPersonName(deductor.getPersonName());
		deductorTanAddress.setPersonPan(deductor.getPersonPan());
		deductorTanAddress.setPersonDesignation(deductor.getPersonDesignation());
		deductorTanAddress.setPersonState(deductor.getPersonState());
		deductorTanAddress.setPersonCity(deductor.getPersonCity());
		deductorTanAddress.setPersonStdCode(deductor.getPersonStdCode());
		deductorTanAddress.setPinCode(deductor.getPersonPinCode());
		deductorTanAddress.setPersonArea(deductor.getPersonAreaOrLocality());
		deductorTanAddress.setPersonStreetName(deductor.getPersonStreetOrRoadName());
		deductorTanAddress.setPersonBuildingName(deductor.getNameOfBuilding());
		deductorTanAddress.setPersonFlatDoorBlockNo(deductor.getPersonFlateOrDoorOrBlockNo());
		deductorTanAddress.setPersonEmail(deductor.getPersonEmail());
		deductorTanAddress.setPersonAlternateEmail(deductor.getEmailAlternate());
		deductorTanAddress.setPersonTelephone(deductor.getTelephone());
		deductorTanAddress.setPersonAlternateTelephone(deductor.getTelephoneAlternate());
		deductorTanAddress.setPersonMobileNumber(deductor.getPersonMobileNumber());
		if (deductor.getAddressChange() == null) {
			deductorTanAddress.setPersonAddressChange(false);
		} else {
			deductorTanAddress.setPersonAddressChange(deductor.getAddressChange());
		}
		deductorTanAddress.setPan(deductor.getDeductorPan());
		deductorTanAddress.setActive(true);
		deductorTanAddress.setCreatedBy(userName);
		deductorTanAddress.setModifiedBy(userName);
		deductorTanAddress.setModifiedDate(new Date());
		deductorTanAddress.setCreatedDate(new Date());
		deductorTanAddress.setAccountantSalutation(deductor.getAccountantSalutation());

		if (StringUtils.isBlank(deductor.getDvndOptedFor15CaCb())) {
			isNotValid = true;
			errorDto.setReason(errorDto.getReason() + "Dividend- Opt-In for form 15CA/CB? column is Mandatory" + "\n");
		} else {
			if (deductor.getDvndOptedFor15CaCb().equals("Y")) {
				deductorTanAddress.setDvndOptedFor15CaCb(true);
				if (StringUtils.isBlank(deductor.getDvndAccountantName())) {
					isNotValid = true;
					errorDto.setReason(errorDto.getReason() + "Accountant Name column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndAccountantName(deductor.getDvndAccountantName());
				}

				if (StringUtils.isBlank(deductor.getDvndAreaLocality())) {
					isNotValid = true;
					errorDto.setReason(errorDto.getReason() + "Area / Locality  column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndAreaLocality(deductor.getDvndAreaLocality());
				}

				if (StringUtils.isBlank(deductor.getDvndBranchOfBank())) {
					isNotValid = true;
					errorDto.setReason(errorDto.getReason() + "Branch of the bank  column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndBranchOfBank(deductor.getDvndBranchOfBank());
				}

				if (StringUtils.isBlank(deductor.getDvndBsrCodeOfBankBranch())) {
					isNotValid = true;
					errorDto.setReason(
							errorDto.getReason() + "BSR code of the bank branch (7 digit)  column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndBsrCodeOfBankBranch(deductor.getDvndBsrCodeOfBankBranch());
				}

				if (StringUtils.isBlank(deductor.getDvndCountry())) {
					isNotValid = true;
					errorDto.setReason(errorDto.getReason() + "COUNTRY  column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndCountry(deductor.getDvndCountry());
				}

				if (StringUtils.isBlank(deductor.getDvndFatherOrHusbandName())) {
					isNotValid = true;
					errorDto.setReason(errorDto.getReason() + "Father's/ Husband Name  column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndFatherOrHusbandName(deductor.getDvndFatherOrHusbandName());
				}

				if (StringUtils.isBlank(deductor.getDvndFlatDoorBlockNo())) {
					isNotValid = true;
					errorDto.setReason(errorDto.getReason() + "Flat / Door / Block No  column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndFlatDoorBlockNo(deductor.getDvndFlatDoorBlockNo());
				}

				if (StringUtils.isBlank(deductor.getDvndMembershipNumber())) {
					isNotValid = true;
					errorDto.setReason(errorDto.getReason() + "Membership Number   column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndMembershipNumber(deductor.getDvndMembershipNumber());
				}

				if (StringUtils.isBlank(deductor.getDvndNameOfBank())) {
					isNotValid = true;
					errorDto.setReason(errorDto.getReason() + "Name of Bank   column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndNameOfBank(deductor.getDvndNameOfBank());
				}

				if (StringUtils.isBlank(deductor.getDvndNameOfPremisesBuildingVillage())) {
					isNotValid = true;
					errorDto.setReason(errorDto.getReason()
							+ "Name of Premises / Building / Village   column is Mandatory" + "\n");
				} else {
					deductorTanAddress
							.setDvndNameOfPremisesBuildingVillage(deductor.getDvndNameOfPremisesBuildingVillage());
				}

				if (StringUtils.isBlank(deductor.getDvndNameOfProprietorship())) {
					isNotValid = true;
					errorDto.setReason(
							errorDto.getReason() + "Name of proprietorship/firm   column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndNameOfProprietorship(deductor.getDvndNameOfProprietorship());
				}

				if (StringUtils.isBlank(deductor.getDvndPinCode())) {
					isNotValid = true;
					errorDto.setReason(errorDto.getReason() + "DIVIDEND PIN CODE   column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndPinCode(deductor.getDvndPinCode());
				}

				if (StringUtils.isBlank(deductor.getDvndPrincipalAreaOfBusiness())) {
					isNotValid = true;
					errorDto.setReason(
							errorDto.getReason() + "Principal area of business   column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndPrincipalAreaOfBusiness(deductor.getDvndPrincipalAreaOfBusiness());
				}

				if (StringUtils.isBlank(deductor.getDvndRegistrationNumber())) {
					isNotValid = true;
					errorDto.setReason(errorDto.getReason() + "Registration Number   column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndRegistrationNumber(deductor.getDvndRegistrationNumber());
				}

				if (StringUtils.isBlank(deductor.getDvndRoadStreetPostOffice())) {
					isNotValid = true;
					errorDto.setReason(
							errorDto.getReason() + "Road / Street / Post Office   column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndRoadStreetPostOffice(deductor.getDvndRoadStreetPostOffice());
				}

				if (StringUtils.isBlank(deductor.getDvndState())) {
					isNotValid = true;
					errorDto.setReason(errorDto.getReason() + "STATE   column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndState(deductor.getDvndState());
				}

				if (StringUtils.isBlank(deductor.getDvndTownCityDistrict())) {
					isNotValid = true;
					errorDto.setReason(errorDto.getReason() + "Town / City / District   column is Mandatory" + "\n");
				} else {
					deductorTanAddress.setDvndTownCityDistrict(deductor.getDvndTownCityDistrict());
				}

			} else {
				deductorTanAddress.setDvndOptedFor15CaCb(false);
			}
		}

		if (!isNotValid) {
			List<DeductorTanAddress> deductorTanAddressList = deductorTanAddressDAO.findPanNameByTan(deductor.getTan());
			boolean tanExists = false;
			if (!deductorTanAddressList.isEmpty()) {
				tanExists = deductorTanAddressList.stream().anyMatch(o -> o.getTan().equals(deductor.getTan()));
			}
			if (tanExists == true) {
				errorDto.setReason(errorDto.getReason() + "Cannot add Duplicate Tan " + "\n");
				isNotValid = true;
			}
		}
		return deductorTanAddress;
	}

	/**
	 * 
	 * @param originalFilename
	 * @param deductorTan
	 * @param deductorPan
	 * @param deductorErrorList
	 * @param arrayList
	 * @return
	 * @throws Exception
	 */
	private File prepareDeductorMasterErrorFile(String originalFilename, String deductorTan, String deductorPan,
			ArrayList<DeductorMasterExcelErrorDTO> deductorErrorList,
			ArrayList<DeductorMasterScopeExcelErrorDTO> deductorScopeErrorList, ArrayList<String> headers1,
			ArrayList<String> headers2) throws Exception {
		try {
			headers1.addAll(0, DeductorExcel.STANDARD_ADDITIONAL_HEADERS);
			headers2.addAll(0, DeductorExcel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = deductorMasterXlsxReport(deductorErrorList, deductorScopeErrorList, deductorTan,
					deductorPan, headers1, headers2);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			wkBook.save(baout, SaveFormat.XLSX);
			File errorFile = new File(
					FilenameUtils.getBaseName(originalFilename) + "_" + UUID.randomUUID() + "_Error_Report.xlsx");
			FileUtils.writeByteArrayToFile(errorFile, baout.toByteArray());
			baout.close();
			return errorFile;
		} catch (Exception e) {
			logger.error("Encountered an error while preparing error file", e);
			throw e;
		}
	}

	/**
	 * 
	 * @param deductorErrorList
	 * @param deductorTan
	 * @param deductorPan
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	private Workbook deductorMasterXlsxReport(ArrayList<DeductorMasterExcelErrorDTO> deductorErrorList,
			ArrayList<DeductorMasterScopeExcelErrorDTO> deductorScopeErrorList, String deductorTan, String deductorPan,
			ArrayList<String> headerNames1, ArrayList<String> headerNames2) throws Exception {

		Workbook workbook = new Workbook();
		// deductor master sheet
		Worksheet worksheet1 = workbook.getWorksheets().get(0);
		worksheet1.setName("deductor_fields");
		// deductor master scope sheet
		Worksheet worksheet2 = workbook.getWorksheets().add("scope_fields");

		worksheet1.getCells().importArrayList(headerNames1, 2, 0, false);
		worksheet2.getCells().importArrayList(headerNames2, 2, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForDeductorMaster(deductorErrorList, worksheet1, deductorTan, headerNames1);
		setExtractDataForDeductorMasterScope(deductorScopeErrorList, worksheet2, deductorTan, headerNames2);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		
		
		Range headerColorRangeDeductor = worksheet1.getCells().createRange("A3:B3");
		Range headerColorRangeScope = worksheet2.getCells().createRange("A3:B3");
		headerColorRangeDeductor.setStyle(style1);
		headerColorRangeScope.setStyle(style1);
		

		Cell cellD6 = worksheet1.getCells().get("C3");
		Cell deductorcellD6 = worksheet2.getCells().get("C3");
		Style styleD6 = cellD6.getStyle();
		styleD6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleD6.setPattern(BackgroundType.SOLID);
		styleD6.getFont().setBold(true);
		styleD6.getFont().setColor(Color.getWhite());
		styleD6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellD6.setStyle(styleD6);
		deductorcellD6.setStyle(styleD6);

		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		style2.setTextWrapped(true);
		
		Style style3 = workbook.createStyle();
		style3.setForegroundColor(Color.fromArgb(240,187,233));//#f0bbe9
		style3.setPattern(BackgroundType.SOLID);
		style3.getFont().setBold(false);
		style3.setHorizontalAlignment(TextAlignmentType.CENTER);
		style3.setTextWrapped(true);
		
		Style style4 = workbook.createStyle();
		style4.setForegroundColor(Color.fromArgb(194,239,153));//##c2ef99
		style4.setPattern(BackgroundType.SOLID);
		style4.getFont().setBold(false);
		style4.setHorizontalAlignment(TextAlignmentType.CENTER);
		style4.setTextWrapped(true);
		
		Style style5 = workbook.createStyle();
		style5.setForegroundColor(Color.fromArgb(248,251,103));//#f8fb67
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(false);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		style5.setTextWrapped(true);
		
		Style style6 = workbook.createStyle();
		style6.setForegroundColor(Color.fromArgb(255,255,109));//#ffff6d
		style6.setPattern(BackgroundType.SOLID);
		style6.getFont().setBold(false);
		style6.setHorizontalAlignment(TextAlignmentType.CENTER);
		style6.setTextWrapped(true);
		
		Style style7 = workbook.createStyle();
		style7.setForegroundColor(Color.fromArgb(95,154,196));//#729fcfS
		style7.setPattern(BackgroundType.SOLID);
		style7.getFont().setBold(false);
		style7.setHorizontalAlignment(TextAlignmentType.CENTER);
		style7.setTextWrapped(true);
		
		Range headerColorRange2 = worksheet1.getCells().createRange("D3:BQ3");
		
		//color ranges for scope sheet 
		Range headerColorRangescope1 = worksheet2.getCells().createRange("D3:K3");
		Range headerColorRangescope2 = worksheet2.getCells().createRange("L3:N3");
		Range headerColorRangescope3 = worksheet2.getCells().createRange("O3:S3");
		Range headerColorRangescope4 = worksheet2.getCells().createRange("X3:AR3");
		
		headerColorRangescope1.setStyle(style3);
		headerColorRangescope3.setStyle(style3);
		headerColorRangescope2.setStyle(style4);
		headerColorRangescope4.setStyle(style6);
		headerColorRange2.setStyle(style7);
		
		worksheet2.getCells().get("T3").setStyle(style3);
		worksheet2.getCells().get("U3").setStyle(style4);
		worksheet2.getCells().get("V3").setStyle(style3);
		worksheet2.getCells().get("W3").setStyle(style4);
		worksheet2.getCells().get("X3").setStyle(style3);
		worksheet2.getCells().get("Y3").setStyle(style4);
		worksheet2.getCells().get("Z3").setStyle(style3);
		
		//headerColorRange2.setStyle(style2);
		//headerColorRange3.setStyle(style2);

		worksheet1.getCells().setRowHeight(2, 17);
		worksheet1.autoFitColumns();
		worksheet1.autoFitRows();
		worksheet1.setGridlinesVisible(false);
		worksheet1.freezePanes(0, 2, 0, 2);

		worksheet2.getCells().setRowHeight(2, 17);
		worksheet2.autoFitColumns();
		worksheet2.autoFitRows();
		worksheet2.setGridlinesVisible(false);
		worksheet2.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		//List<DeductorMaster> response = deductorMasterDAO.findByDeductorPan(deductorPan);

		Cell cellA1 = worksheet1.getCells().get("A1");
		cellA1 = worksheet2.getCells().get("A1");
		cellA1.setValue("Deductor Master Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(12);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);


		// column B5 column
		Cell cellB5 = worksheet1.getCells().get("B2");
		Cell cellscopeB5 = worksheet2.getCells().get("B2");
		cellB5.setValue("Error/Information");
		cellscopeB5.setValue("Error/Information");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);
		cellscopeB5.setStyle(b5Style);

		CommonUtil.setBoardersForAsposeXlsx(worksheet2, "A3", "AR3", "A4",
				"AR"+(deductorScopeErrorList.size()+4)); // deductormaster scope
		CommonUtil.setBoardersForAsposeXlsx(worksheet1, "A3", "BQ3", "A4",
				"BQ"+(deductorErrorList.size()+4));
		

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter1 = worksheet1.getAutoFilter();
		AutoFilter autoFilter2 = worksheet2.getAutoFilter();

		 autoFilter1.setRange("A3:BQ3");
		 autoFilter2.setRange("A3:AR3");
		 

		worksheet1.autoFitColumn(1);
		worksheet1.autoFitRows();

		worksheet2.autoFitColumn(1);
		worksheet2.autoFitRows();
		
		worksheet1.getCells().setColumnWidth(1, 36);
		worksheet2.getCells().setColumnWidth(1, 36);
		return workbook;
	}

	/**
	 * 
	 * @param deductorErrorList
	 * @param worksheet
	 * @param deductorTan
	 * @param headerNames
	 * @throws Exception
	 */
	private void setExtractDataForDeductorMaster(ArrayList<DeductorMasterExcelErrorDTO> deductorErrorList,
			Worksheet worksheet, String deductorTan, ArrayList<String> headerNames) throws Exception {
		if (!deductorErrorList.isEmpty()) {
			int dataRowsStartIndex = 3;
			for (int i = 0; i < deductorErrorList.size(); i++) {
				DeductorMasterExcelErrorDTO errorDTO = deductorErrorList.get(i);
				ArrayList<String> rowData = DeductorExcel.getValues(errorDTO, DeductorMasterExcel.fieldMappings,
						headerNames);
				rowData.set(0, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(2, i + "");
				rowData.set(3,
						StringUtils.isBlank(errorDTO.getTdsModule()) ? StringUtils.EMPTY : errorDTO.getTdsModule());
				rowData.set(4,
						StringUtils.isBlank(errorDTO.getTcsModule()) ? StringUtils.EMPTY : errorDTO.getTcsModule());
				rowData.set(5, StringUtils.isBlank(errorDTO.getDeductorCode()) ? StringUtils.EMPTY
						: errorDTO.getDeductorCode());
				rowData.set(6, StringUtils.isBlank(errorDTO.getDeductorSalutation()) ? StringUtils.EMPTY
						: errorDTO.getDeductorSalutation());
				rowData.set(7, StringUtils.isBlank(errorDTO.getDeductorName()) ? StringUtils.EMPTY
						: errorDTO.getDeductorName());
				rowData.set(8,
						StringUtils.isBlank(errorDTO.getDeductorPan()) ? StringUtils.EMPTY : errorDTO.getDeductorPan());
				rowData.set(9, StringUtils.isBlank(errorDTO.getDeductorResidentialStatus()) ? StringUtils.EMPTY
						: errorDTO.getDeductorResidentialStatus());
				rowData.set(10, StringUtils.isBlank(errorDTO.getDeductorType()) ? StringUtils.EMPTY
						: errorDTO.getDeductorType());
				rowData.set(11, StringUtils.isBlank(errorDTO.getDeductorStatus()) ? StringUtils.EMPTY
						: errorDTO.getDeductorStatus());
				rowData.set(12, StringUtils.isBlank(errorDTO.getModeOfPayment()) ? StringUtils.EMPTY
						: errorDTO.getModeOfPayment());
				rowData.set(13, StringUtils.isBlank(errorDTO.getDueDateOfTaxPayment()) ? StringUtils.EMPTY
						: errorDTO.getDueDateOfTaxPayment());
				rowData.set(14, StringUtils.isBlank(errorDTO.getEmail()) ? StringUtils.EMPTY : errorDTO.getEmail());
				rowData.set(15, StringUtils.isBlank(errorDTO.getEmailAlternate()) ? StringUtils.EMPTY
						: errorDTO.getEmailAlternate());
				rowData.set(16, StringUtils.isBlank(errorDTO.getGoodsAndServicesTaxNumber()) ? StringUtils.EMPTY
						: errorDTO.getGoodsAndServicesTaxNumber());
				rowData.set(17, StringUtils.isBlank(errorDTO.getMobileNumber()) ? StringUtils.EMPTY
						: errorDTO.getMobileNumber());
				rowData.set(18, StringUtils.isBlank(errorDTO.getGoodsAndServicesTaxNumber()) ? StringUtils.EMPTY
						: errorDTO.getGoodsAndServicesTaxNumber());
				rowData.set(19, StringUtils.isBlank(errorDTO.getApplicableFrom()) ? StringUtils.EMPTY
						: errorDTO.getApplicableFrom());
				rowData.set(20, StringUtils.isBlank(errorDTO.getApplicableTo()) ? StringUtils.EMPTY
						: errorDTO.getApplicableTo());
				rowData.set(21, StringUtils.isBlank(errorDTO.getDeductorHaveMoreThanOneBranch()) ? StringUtils.EMPTY
						: errorDTO.getDeductorHaveMoreThanOneBranch());
				rowData.set(22, StringUtils.isBlank(errorDTO.getTan()) ? StringUtils.EMPTY : errorDTO.getTan());
				rowData.set(23, StringUtils.isBlank(errorDTO.getCountry()) ? StringUtils.EMPTY : errorDTO.getCountry());
				rowData.set(24, StringUtils.isBlank(errorDTO.getStates()) ? StringUtils.EMPTY : errorDTO.getStates());
				rowData.set(25, StringUtils.isBlank(errorDTO.getCity()) ? StringUtils.EMPTY : errorDTO.getCity());
				rowData.set(26, StringUtils.isBlank(errorDTO.getStdCode()) ? StringUtils.EMPTY : errorDTO.getStdCode());
				rowData.set(27, StringUtils.isBlank(errorDTO.getPinCode()) ? StringUtils.EMPTY : errorDTO.getPinCode());
				rowData.set(28, StringUtils.isBlank(errorDTO.getAreaOrLocality()) ? StringUtils.EMPTY
						: errorDTO.getAreaOrLocality());
				rowData.set(29, StringUtils.isBlank(errorDTO.getRoadOrStreetOrPostOffice()) ? StringUtils.EMPTY
						: errorDTO.getRoadOrStreetOrPostOffice());
				rowData.set(30, StringUtils.isBlank(errorDTO.getNameOfBuilding()) ? StringUtils.EMPTY
						: errorDTO.getNameOfBuilding());
				rowData.set(31, StringUtils.isBlank(errorDTO.getFlatOrDoorOrBlockNo()) ? StringUtils.EMPTY
						: errorDTO.getFlatOrDoorOrBlockNo());
				rowData.set(32,
						StringUtils.isBlank(errorDTO.getPersonName()) ? StringUtils.EMPTY : errorDTO.getPersonName());
				rowData.set(33,
						StringUtils.isBlank(errorDTO.getPersonPan()) ? StringUtils.EMPTY : errorDTO.getPersonPan());
				rowData.set(34, StringUtils.isBlank(errorDTO.getPersonDesignation()) ? StringUtils.EMPTY
						: errorDTO.getPersonDesignation());
				rowData.set(35,
						StringUtils.isBlank(errorDTO.getPersonState()) ? StringUtils.EMPTY : errorDTO.getPersonState());
				rowData.set(36,
						StringUtils.isBlank(errorDTO.getPersonCity()) ? StringUtils.EMPTY : errorDTO.getPersonCity());
				rowData.set(37, StringUtils.isBlank(errorDTO.getPersonStdCode()) ? StringUtils.EMPTY
						: errorDTO.getPersonStdCode());
				rowData.set(38, StringUtils.isBlank(errorDTO.getPersonPinCode()) ? StringUtils.EMPTY
						: errorDTO.getPersonPinCode());
				rowData.set(39, StringUtils.isBlank(errorDTO.getPersonAreaOrLocality()) ? StringUtils.EMPTY
						: errorDTO.getPersonAreaOrLocality());
				rowData.set(40, StringUtils.isBlank(errorDTO.getPersonStreetOrRoadName()) ? StringUtils.EMPTY
						: errorDTO.getPersonStreetOrRoadName());
				rowData.set(41, StringUtils.isBlank(errorDTO.getPersonNameOfBuilding()) ? StringUtils.EMPTY
						: errorDTO.getPersonNameOfBuilding());
				rowData.set(42, StringUtils.isBlank(errorDTO.getPersonFlateOrDoorOrBlockNo()) ? StringUtils.EMPTY
						: errorDTO.getPersonFlateOrDoorOrBlockNo());
				rowData.set(43,
						StringUtils.isBlank(errorDTO.getPersonEmail()) ? StringUtils.EMPTY : errorDTO.getPersonEmail());
				rowData.set(44, StringUtils.isBlank(errorDTO.getPersonEmailAlternate()) ? StringUtils.EMPTY
						: errorDTO.getPersonEmailAlternate());
				rowData.set(45,
						StringUtils.isBlank(errorDTO.getTelephone()) ? StringUtils.EMPTY : errorDTO.getTelephone());
				rowData.set(46, StringUtils.isBlank(errorDTO.getTelephoneAlternate()) ? StringUtils.EMPTY
						: errorDTO.getTelephoneAlternate());
				rowData.set(47, StringUtils.isBlank(errorDTO.getPersonMobileNumber()) ? StringUtils.EMPTY
						: errorDTO.getPersonMobileNumber());
				rowData.set(48, StringUtils.isBlank(errorDTO.getAddressChange()) ? StringUtils.EMPTY
						: errorDTO.getAddressChange());
				rowData.set(49, StringUtils.isBlank(errorDTO.getDvndDeductorTypeName()) ? StringUtils.EMPTY
						: errorDTO.getDvndDeductorTypeName());
				rowData.set(50, StringUtils.isBlank(errorDTO.getDvndOptedFor15CaCb()) ? StringUtils.EMPTY
						: errorDTO.getDvndOptedFor15CaCb());
				rowData.set(51, StringUtils.isBlank(errorDTO.getDvndPrincipalAreaOfBusiness()) ? StringUtils.EMPTY
						: errorDTO.getDvndPrincipalAreaOfBusiness());
				rowData.set(52, StringUtils.isBlank(errorDTO.getDvndNameOfBank()) ? StringUtils.EMPTY
						: errorDTO.getDvndNameOfBank());
				rowData.set(53, StringUtils.isBlank(errorDTO.getDvndBranchOfBank()) ? StringUtils.EMPTY
						: errorDTO.getDvndBranchOfBank());
				rowData.set(54, StringUtils.isBlank(errorDTO.getDvndFatherOrHusbandName()) ? StringUtils.EMPTY
						: errorDTO.getDvndFatherOrHusbandName());
				rowData.set(55, StringUtils.isBlank(errorDTO.getDvndBsrCodeOfBankBranch()) ? StringUtils.EMPTY
						: errorDTO.getDvndBsrCodeOfBankBranch());
				rowData.set(56, StringUtils.isBlank(errorDTO.getDvndNameOfPremisesBuildingVillage()) ? StringUtils.EMPTY
						: errorDTO.getDvndNameOfPremisesBuildingVillage());
				rowData.set(57, StringUtils.isBlank(errorDTO.getAccountantSalutation()) ? StringUtils.EMPTY
						: errorDTO.getAccountantSalutation());
				rowData.set(58, StringUtils.isBlank(errorDTO.getDvndAccountantName()) ? StringUtils.EMPTY
						: errorDTO.getDvndAccountantName());
				rowData.set(59, StringUtils.isBlank(errorDTO.getDvndNameOfProprietorship()) ? StringUtils.EMPTY
						: errorDTO.getDvndNameOfProprietorship());
				rowData.set(60, StringUtils.isBlank(errorDTO.getDvndFlatDoorBlockNo()) ? StringUtils.EMPTY
						: errorDTO.getDvndFlatDoorBlockNo());

				rowData.set(61, StringUtils.isBlank(errorDTO.getDvndAreaLocality()) ? StringUtils.EMPTY
						: errorDTO.getDvndAreaLocality());
				rowData.set(62, StringUtils.isBlank(errorDTO.getDvndTownCityDistrict()) ? StringUtils.EMPTY
						: errorDTO.getDvndTownCityDistrict());
				rowData.set(63,
						StringUtils.isBlank(errorDTO.getDvndPinCode()) ? StringUtils.EMPTY : errorDTO.getDvndPinCode());
				rowData.set(64,
						StringUtils.isBlank(errorDTO.getDvndCountry()) ? StringUtils.EMPTY : errorDTO.getDvndCountry());
				rowData.set(65,
						StringUtils.isBlank(errorDTO.getDvndState()) ? StringUtils.EMPTY : errorDTO.getDvndState());
				rowData.set(66, StringUtils.isBlank(errorDTO.getDvndMembershipNumber()) ? StringUtils.EMPTY
						: errorDTO.getDvndMembershipNumber());
				rowData.set(67, StringUtils.isBlank(errorDTO.getDvndRoadStreetPostOffice()) ? StringUtils.EMPTY
						: errorDTO.getDvndRoadStreetPostOffice());
				rowData.set(68, StringUtils.isBlank(errorDTO.getDvndRegistrationNumber()) ? StringUtils.EMPTY
						: errorDTO.getDvndRegistrationNumber());
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				DeductorExcel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
			}
		}
	}
	
	private void setExtractDataForDeductorMasterScope(
			ArrayList<DeductorMasterScopeExcelErrorDTO> deductorScopeErrorList, Worksheet worksheet, String deductorTan,
			ArrayList<String> headerNames) throws Exception {
		int dataRowsStartIndex = 3;
		for (int i = 0; i < deductorScopeErrorList.size(); i++) {
			DeductorMasterScopeExcelErrorDTO errorDTO = deductorScopeErrorList.get(i);
			ArrayList<String> rowData = DeductorExcel.getValues(errorDTO, DeductorMasterScopeExcel.fieldMappings,
					headerNames);
			rowData.set(0, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
			rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
			rowData.set(2, i + "");
			rowData.set(3,
					StringUtils.isBlank(errorDTO.getDeductorPan()) ? StringUtils.EMPTY : errorDTO.getDeductorPan());
			rowData.set(4, StringUtils.isBlank(errorDTO.getPanLdcValidation()) ? StringUtils.EMPTY
					: errorDTO.getPanLdcValidation());
			rowData.set(5,
					StringUtils.isBlank(errorDTO.getInvoicesProvisionsAdvancesDetermination()) ? StringUtils.EMPTY
							: errorDTO.getInvoicesProvisionsAdvancesDetermination());
			rowData.set(6, StringUtils.isBlank(errorDTO.getRatePredication()) ? StringUtils.EMPTY
					: errorDTO.getRatePredication());
			rowData.set(7, StringUtils.isBlank(errorDTO.getMonthlyCompliance()) ? StringUtils.EMPTY
					: errorDTO.getMonthlyCompliance());
			rowData.set(8, StringUtils.isBlank(errorDTO.getQuarterlyReturns()) ? StringUtils.EMPTY
					: errorDTO.getQuarterlyReturns());
			rowData.set(9, StringUtils.isBlank(errorDTO.getClause34a()) ? StringUtils.EMPTY : errorDTO.getClause34a());
			rowData.set(10, StringUtils.isBlank(errorDTO.getTracesDefaults()) ? StringUtils.EMPTY
					: errorDTO.getTracesDefaults());
			rowData.set(11, StringUtils.isBlank(errorDTO.getDividends()) ? StringUtils.EMPTY : errorDTO.getDividends());
			rowData.set(12, StringUtils.isBlank(errorDTO.getFilingform15CACB()) ? StringUtils.EMPTY
					: errorDTO.getFilingform15CACB());
			rowData.set(13, StringUtils.isBlank(errorDTO.getFilingform15GH()) ? StringUtils.EMPTY
					: errorDTO.getFilingform15GH());
			rowData.set(14, StringUtils.isBlank(errorDTO.getSap()) ? StringUtils.EMPTY : errorDTO.getSap());
			rowData.set(15, StringUtils.isBlank(errorDTO.getExcel()) ? StringUtils.EMPTY : errorDTO.getExcel());
			rowData.set(16, StringUtils.isBlank(errorDTO.getPdf()) ? StringUtils.EMPTY : errorDTO.getPdf());
			rowData.set(17, StringUtils.isBlank(errorDTO.getInvoiceDescription()) ? StringUtils.EMPTY
					: errorDTO.getInvoiceDescription());
			rowData.set(18,
					StringUtils.isBlank(errorDTO.getPoDescription()) ? StringUtils.EMPTY : errorDTO.getPoDescription());
			rowData.set(19,
					StringUtils.isBlank(errorDTO.getGlDescription()) ? StringUtils.EMPTY : errorDTO.getGlDescription());
			rowData.set(20, StringUtils.isBlank(errorDTO.getSac()) ? StringUtils.EMPTY : errorDTO.getSac());
			rowData.set(21,
					StringUtils.isBlank(errorDTO.getVendorMaster()) ? StringUtils.EMPTY : errorDTO.getVendorMaster());
			rowData.set(22, StringUtils.isBlank(errorDTO.getProvisionTrackingPeriods()) ? StringUtils.EMPTY
					: errorDTO.getProvisionTrackingPeriods());
			rowData.set(23, StringUtils.isBlank(errorDTO.getProvisionProcessing()) ? StringUtils.EMPTY
					: errorDTO.getProvisionProcessing());
			rowData.set(24, StringUtils.isBlank(errorDTO.getAdvanceProcessing()) ? StringUtils.EMPTY
					: errorDTO.getAdvanceProcessing());
			rowData.set(25, StringUtils.isBlank(errorDTO.getCreditNoteProcessing()) ? StringUtils.EMPTY
					: errorDTO.getCreditNoteProcessing());
			rowData.set(26, StringUtils.isBlank(errorDTO.getConsolidatedChallanProcessing()) ? StringUtils.EMPTY
					: errorDTO.getConsolidatedChallanProcessing());
			rowData.set(27, StringUtils.isBlank(errorDTO.getEnableAnnualTransactionPerLimit()) ? StringUtils.EMPTY
					: errorDTO.getEnableAnnualTransactionPerLimit());
			rowData.set(28, StringUtils.isBlank(errorDTO.getSelectTypeInterestCalculation()) ? StringUtils.EMPTY
					: errorDTO.getSelectTypeInterestCalculation());
			rowData.set(29, StringUtils.isBlank(errorDTO.getSelectRoundingOff()) ? StringUtils.EMPTY
					: errorDTO.getSelectRoundingOff());
			rowData.set(30, StringUtils.isBlank(errorDTO.getKeyStrategicShareHolderA()) ? StringUtils.EMPTY
					: errorDTO.getKeyStrategicShareHolderA());
			rowData.set(31, StringUtils.isBlank(errorDTO.getNonKeyStrategicShareHolderA()) ? StringUtils.EMPTY
					: errorDTO.getNonKeyStrategicShareHolderA());
			rowData.set(32, StringUtils.isBlank(errorDTO.getKeyStrategicShareHolderB()) ? StringUtils.EMPTY
					: errorDTO.getKeyStrategicShareHolderB());
			rowData.set(33, StringUtils.isBlank(errorDTO.getNonKeyStrategicShareHolderB()) ? StringUtils.EMPTY
					: errorDTO.getNonKeyStrategicShareHolderB());
			rowData.set(34, StringUtils.isBlank(errorDTO.getKeyStrategicShareHolderC()) ? StringUtils.EMPTY
					: errorDTO.getKeyStrategicShareHolderC());
			rowData.set(35, StringUtils.isBlank(errorDTO.getNonKeyStrategicShareHolderC()) ? StringUtils.EMPTY
					: errorDTO.getNonKeyStrategicShareHolderC());
			rowData.set(36, StringUtils.isBlank(errorDTO.getKeyStrategicShareHolderD()) ? StringUtils.EMPTY
					: errorDTO.getKeyStrategicShareHolderD());
			rowData.set(37, StringUtils.isBlank(errorDTO.getNonKeyStrategicShareHolderD()) ? StringUtils.EMPTY
					: errorDTO.getNonKeyStrategicShareHolderD());
			rowData.set(38, StringUtils.isBlank(errorDTO.getKeyStrategicShareHolderE()) ? StringUtils.EMPTY
					: errorDTO.getKeyStrategicShareHolderE());
			rowData.set(39, StringUtils.isBlank(errorDTO.getNonKeyStrategicShareHolderE()) ? StringUtils.EMPTY
					: errorDTO.getNonKeyStrategicShareHolderE());
			rowData.set(40, StringUtils.isBlank(errorDTO.getKeyStrategicShareHolderF()) ? StringUtils.EMPTY
					: errorDTO.getKeyStrategicShareHolderF());
			rowData.set(41, StringUtils.isBlank(errorDTO.getNonKeyStrategicShareHolderF()) ? StringUtils.EMPTY
					: errorDTO.getNonKeyStrategicShareHolderF());
			rowData.set(42, StringUtils.isBlank(errorDTO.getIsDividendDistriutionTaxPaid()) ? StringUtils.EMPTY
					: errorDTO.getIsDividendDistriutionTaxPaid());
			rowData.set(43, StringUtils.isBlank(errorDTO.getPreparationOfForm15CB()) ? StringUtils.EMPTY
					: errorDTO.getPreparationOfForm15CB());

			worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
			worksheet.autoFitRow(i + dataRowsStartIndex);
			worksheet.autoFitColumn(i);
			DeductorExcel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
		}
	}

}
