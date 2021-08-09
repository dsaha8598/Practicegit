package com.ey.in.tds.onboarding.service.deductee;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.CellsHelper;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tcs.common.TCSMasterDTO;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsPaymentDTO;
import com.ey.in.tds.common.dto.NatureAndTaxRateDTO;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.model.deductee.CollecteeMasterErrorReportCsvDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeNoiThresholdLedger;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.service.TCSActivityTrackerService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.util.ActivityTrackerStatus;
import com.ey.in.tds.core.util.ActivityType;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.jdbc.dao.CollecteeMasterDAO;
import com.ey.in.tds.jdbc.dao.CollecteeNoiThresholdLedgerDAO;
import com.ey.in.tds.jdbc.dao.CollectorOnBoardingInfoDAO;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.onboarding.service.util.excel.deductee.CollecteeMasterExcel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

/**
 * 
 * @author vamsir
 *
 */
@Service
public class CollecteeBulkService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TCSBatchUploadDAO tcsBatchUploadDAO;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private CollecteeMasterDAO collecteeMasterDAO;

	@Autowired
	private CollecteeNoiThresholdLedgerDAO collecteeNoiThresholdLedgerDAO;

	@Autowired
	private CollectorOnBoardingInfoDAO collectorOnBoardingInfoDAO;

	@Autowired
	private TCSActivityTrackerService tcsActivityTrackerService;

	ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 
	 * @param workbook
	 * @param uploadedFile
	 * @param sha256
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param tenantId
	 * @param collectorPan
	 * @param tcsBatchUpload
	 * @return
	 * @throws Exception
	 */
	@Async
	public TCSBatchUpload processCollecteeMaster(XSSFWorkbook workbook, MultipartFile uploadedFile, String sha256,
			String collectorTan, Integer assesssmentYear, Integer assessmentMonth, String userName, String tenantId,
			String collectorPan, TCSBatchUpload tcsBatchUpload) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		ArrayList<CollecteeMasterErrorReportCsvDTO> errorList = new ArrayList<>();
		File deducteeErrorFile = null;
		try {
			CollecteeMasterExcel excelData = new CollecteeMasterExcel(workbook);

			tcsBatchUpload.setSha256sum(sha256);
			tcsBatchUpload.setMismatchCount(0L);
			long dataRowsCount = excelData.getDataRowsCount();
			tcsBatchUpload.setRowsCount(dataRowsCount);

			Long errorCount = 0L;
			Long duplicateCount = 0L;
			List<CollecteeMaster> collecteeList = new ArrayList<>();
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				boolean isNotValid = false;
				boolean isResStatusValid = true;
				Optional<CollecteeMasterErrorReportCsvDTO> errorDTO = null;
				try {
					errorDTO = excelData.validate(rowIndex);
				} catch (Exception e) {
					logger.error("Unable validate row number " + rowIndex + " due to "
							+ ExceptionUtils.getRootCauseMessage(e), e);
					++errorCount;
					continue;
				}
				if (errorDTO.isPresent()) {
					++errorCount;
					errorList.add(errorDTO.get());
				} else {
					try {
						CollecteeMaster collectee = excelData.get(rowIndex);
						/*
						 * //validating collectee excell data
						 * SecurityValidations.collecteeMasterInputValidationForExcelUpload(collectee);
						 */
						collectee.setActive(true);
						collectee.setIsEligibleForMultipleSections(false);
						collectee.setCreatedBy(userName);
						collectee.setCreatedDate(new Date());
						collectee.setBatchUploadId(tcsBatchUpload.getId());
						if (StringUtils.isNotBlank(collectee.getCollecteePan())) {
							collectee.setPanVerifyStatus(false);
						}
						// Error report dto
						CollecteeMasterErrorReportCsvDTO collecteeErrorDTO = excelData.getErrorDTO(rowIndex);
						boolean residentStatus = false;
						if (collectee.getNonResidentCollecteeIndicator()
								|| "Y".equals(collectee.getNonResidentCollecteeIndicator())) {
							residentStatus = true;
						} else if (!collectee.getNonResidentCollecteeIndicator()
								|| "N".equals(collectee.getNonResidentCollecteeIndicator())) {
							residentStatus = false;
						}
						collectee.setNonResidentCollecteeIndicator(residentStatus);
						if (StringUtils.isNotBlank(collectee.getNameOfTheCollectee())) {
							collectee.setNameOfTheCollectee(collectee.getNameOfTheCollectee().trim());
						}
						if (StringUtils.isNotBlank(collectee.getNatureOfIncome())) {
							collectee.setNatureOfIncome(collectee.getNatureOfIncome().trim());
						}

						if (StringUtils.isBlank(collecteeErrorDTO.getReason())) {
							collecteeErrorDTO.setReason("");
						}
						// Check for collector code
						String collectorCode = StringUtils.EMPTY;
						List<DeductorMaster> deductorMaster = deductorMasterDAO.findByDeductorPan(collectorPan);
						if (!deductorMaster.isEmpty()) {
							collectorCode = deductorMaster.get(0).getCode();
						}
						if (!collectorCode.equalsIgnoreCase(collectee.getCollectorCode())) {
							collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() + "Collector Code "
									+ collectee.getCollectorCode() + " is not match." + "\n");
							isNotValid = true;
						}
						// Check for collector pan
						if (StringUtils.isNotBlank(collectee.getCollectorPan())
								&& !collectorPan.equalsIgnoreCase(collectee.getCollectorPan())) {
							collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() + "Collector PAN "
									+ collectee.getCollectorPan() + " is not match." + "\n");
							isNotValid = true;
						}
						// Check for gstin number
						if (StringUtils.isNotBlank(collectee.getGstinNumber()) && !collectee.getGstinNumber()
								.matches("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")) {
							collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() + "GSTIN Number "
									+ collectee.getGstinNumber() + " is invalid (Ex:06AAAAA6385P6Z2)." + "\n");
							isNotValid = true;
						}

						// Check for Collectee Aadhar Number Validation
						if (StringUtils.isNotBlank(collectee.getCollecteeAadharNumber())
								&& !collectee.getCollecteeAadharNumber().matches("[0-9]{12}")) {
							collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() + "Collectee Aadhar Number "
									+ collectee.getCollecteeAadharNumber() + " is not valid, enter 12 digit number."
									+ "\n");
							isNotValid = true;
							isResStatusValid = false;
						}
						// year and month validation check
						if (collectee.getCurrentBalanceYear() > 0
								&& !String.valueOf(collectee.getCurrentBalanceYear()).matches("[0-9]{4}")) {
							collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() + "Current Balance year "
									+ collectee.getCurrentBalanceYear() + " is not valid." + "\n");
						}
						if (collectee.getCurrentBalanceMonth() > 0 && !String
								.valueOf(collectee.getCurrentBalanceMonth()).matches("^([1-9]|[0-1][0-2])$")) {
							collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() + "Current Balance month "
									+ collectee.getCurrentBalanceMonth() + " is not valid." + "\n");
							isNotValid = true;
						}

						if (collectee.getPreviousBalanceYear() > 0
								&& !String.valueOf(collectee.getPreviousBalanceYear()).matches("[0-9]{4}")) {
							collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() + "Previous Balance year "
									+ collectee.getPreviousBalanceYear() + " is not valid." + "\n");
							isNotValid = true;
						}

						if (collectee.getPreviousBalanceMonth() > 0 && !String
								.valueOf(collectee.getPreviousBalanceMonth()).matches("^([1-9]|[0-1][0-2])$")) {
							collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() + "Previous Balance month "
									+ collectee.getPreviousBalanceMonth() + " is not valid." + "\n");
							isNotValid = true;
						}

						if (collectee.getBalancesForSection206c() != BigDecimal.ZERO
								|| collectee.getAdvanceBalancesForSection206c() != BigDecimal.ZERO
								|| collectee.getCollectionsBalancesForSection206c() != BigDecimal.ZERO) {
							if (collectee.getCurrentBalanceYear() == 0) {
								collecteeErrorDTO.setReason(
										collecteeErrorDTO.getReason() + "Current balance year is mandatory " + "\n");
								isNotValid = true;
							}
							if (collectee.getCurrentBalanceMonth() == 0) {
								collecteeErrorDTO.setReason(
										collecteeErrorDTO.getReason() + "Current balance month is mandatory " + "\n");
								isNotValid = true;
							} else if (collectee.getCurrentBalanceYear() == 0
									&& collectee.getCurrentBalanceMonth() == 0) {
								collecteeErrorDTO.setReason(collecteeErrorDTO.getReason()
										+ "Current balance year and current balance month is mandatory " + "\n");
								isNotValid = true;
							}
						}

						if (collectee.getAdvancesAsOfMarch() != BigDecimal.ZERO) {
							if (collectee.getPreviousBalanceYear() == 0) {
								collecteeErrorDTO.setReason(
										collecteeErrorDTO.getReason() + "Previous balance year is mandatory " + "\n");
								isNotValid = true;
							}
							if (collectee.getPreviousBalanceMonth() == 0) {
								collecteeErrorDTO.setReason(
										collecteeErrorDTO.getReason() + "Previous balance month is mandatory " + "\n");
								isNotValid = true;
							} else if (collectee.getPreviousBalanceMonth() == 0
									&& collectee.getPreviousBalanceYear() == 0) {
								collecteeErrorDTO.setReason(collecteeErrorDTO.getReason()
										+ "Previous balance year and Previous balance month is mandatory " + "\n");
								isNotValid = true;
							}
						}
						// check for collectee pan
						if (StringUtils.isNotBlank(collectee.getCollecteePan())) {
							collectee.setCollecteePan(collectee.getCollecteePan().toUpperCase());
							if (collectee.getCollecteePan().matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
								// feign client to get PanName by pan code
								String collecteeStatus = mastersClient
										.getPanNameBasedOnPanCode(String.valueOf(collectee.getCollecteePan().charAt(3)))
										.getBody();
								if (StringUtils.isBlank(collecteeStatus)) {
									collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() + "PAN 4th character "
											+ collectee.getCollecteePan().charAt(3) + " is invalid." + "\n");
									isNotValid = true;
									isResStatusValid = false;
								}
								collectee.setCollecteeStatus(collecteeStatus);
								logger.info("PAN NUMBER : {}", collectee.getCollecteePan());
							} else {
								collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() + "PAN "
										+ collectee.getCollecteePan() + " is not valid." + "\n");
								isNotValid = true;
								isResStatusValid = false;
							}
						} /*
							 * else if (StringUtils.isNotBlank(collectee.getCollecteeStatus())) { String
							 * deducteeStatus = mastersClient .getPanNameBasedOnPanCode(String.valueOf(
							 * collectee.getCollecteeStatus().trim().substring(0, 1).toUpperCase()))
							 * .getBody(); if (StringUtils.isBlank(deducteeStatus)) {
							 * collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() +
							 * "COLLECTEE STATUS " + collectee.getCollecteeStatus() + " IS NOT VALID." +
							 * "\n"); isNotValid = true; isResStatusValid = false; } else {
							 * collectee.setCollecteeStatus(deducteeStatus); } } else if
							 * (StringUtils.isBlank(collectee.getCollecteePan()) &&
							 * StringUtils.isBlank(collectee.getCollecteeStatus())) { collecteeErrorDTO
							 * .setReason(collecteeErrorDTO.getReason() + "COLLECTEE STATUS IS MANDATORY" +
							 * "\n"); isNotValid = true; isResStatusValid = false; }
							 */
						// check for collectee type
						if (StringUtils.isNotBlank(collectee.getCollecteeType())) {
							String collecteeType = mastersClient.getTcsCollecteeType(collectee.getCollecteeType())
									.getBody().getData();
							if (StringUtils.isNotBlank(collecteeType)) {
								collectee.setCollecteeType(collecteeType);
							} else {
								collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() + "Collectee type "
										+ collectee.getCollecteeType() + " is not match" + "\n");
								isNotValid = true;

								isResStatusValid = false;
							}
						}
						if (collectee.getNoCollectionDeclarationAsPerForm27c() != null) {
							collectee.setNoCollectionDeclarationAsPerForm27c(
									collectee.getNoCollectionDeclarationAsPerForm27c());
						} else {
							collectee.setNoCollectionDeclarationAsPerForm27c(false);
						}
						if (StringUtils.isNotEmpty(collectee.getNatureOfIncome())
								&& StringUtils.isEmpty(collectee.getTcsSection())) {
							// error report
							collecteeErrorDTO.setReason(collecteeErrorDTO.getReason()
									+ "Nature of income is required if there is a TCS section." + "\n");
							isNotValid = true;

						}
						// set section and rate
						if (StringUtils.isBlank(collectee.getTcsSection())) {
							collectee.setTcsRate(BigDecimal.ZERO);
						} else if (collectee.getTcsRate() != null) {
							collectee.setTcsRate(collectee.getTcsRate());
						} else {
							collectee.setTcsRate(BigDecimal.ZERO);
						}
						if (isResStatusValid && StringUtils.isNotBlank(collectee.getTcsSection())) {
							boolean nopSection;
							if (residentStatus) {
								nopSection = mastersClient
										.getNOIBasedOnStatusAndSectionResidentStatus("NR", collectee.getTcsSection())
										.getBody().getData();
							} else {
								nopSection = mastersClient
										.getNOIBasedOnStatusAndSectionResidentStatus("RES", collectee.getTcsSection())
										.getBody().getData();
							}
							if (!nopSection) {
								collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() + "TCS section "
										+ collectee.getTcsSection() + " not found in system." + "\n");
								isNotValid = true;
							} else if (StringUtils.isBlank(collectee.getNatureOfIncome())) {
								// get nature of payment and tds rate based on section
								List<NatureAndTaxRateDTO> nopRateList = new ArrayList<>();
								if (residentStatus) {
									nopRateList = mastersClient
											.getTcsListOfNatureAndRate(collectee.getTcsSection(), "NR").getBody()
											.getData();
								} else {
									nopRateList = mastersClient
											.getTcsListOfNatureAndRate(collectee.getTcsSection(), "RES").getBody()
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
								if (collectee.getTcsRate() != null) {
									// section contains mutiple NOP then get the NOP based on closest rate passed in
									// the excel
									Double tcsRate = collectee.getTcsRate().doubleValue();
									Optional<Double> rate = rates.parallelStream()
											.min(Comparator.comparingDouble(i -> Math.abs(i - (tcsRate))));
									collectee.setNatureOfIncome(rateMap.get(rate.isPresent() ? rate.get() : 0.0));
								} else {
									collectee.setNatureOfIncome(rateMap.get(highestRate));
								}
							} else if (StringUtils.isNotBlank(collectee.getNatureOfIncome())) {
								List<String> nops = new ArrayList<>();
								if (residentStatus) {
									nops = mastersClient
											.getNOIBasedOnSectionAndResidentialStatus(collectee.getTcsSection(), "NR")
											.getBody().getData();
								} else {
									nops = mastersClient
											.getNOIBasedOnSectionAndResidentialStatus(collectee.getTcsSection(), "RES")
											.getBody().getData();
								}
								boolean isNopValid = false;
								for (String nop : nops) {
									if (nop.equalsIgnoreCase(collectee.getNatureOfIncome())) {
										isNopValid = true;
										break;
									}
								}
								if (!isNopValid) {
									collecteeErrorDTO.setReason(collecteeErrorDTO.getReason() + "Nature of income "
											+ collectee.getNatureOfIncome() + " not found in system." + "\n");
									isNotValid = true;
								}
							}
						}
						if (StringUtils.isBlank(collecteeErrorDTO.getReason())) {
							collecteeList.add(collectee);
						}
						if (isNotValid) {
							++errorCount;
							errorList.add(collecteeErrorDTO);
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						CollecteeMasterErrorReportCsvDTO problematicDataError = excelData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to process row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			} // for 1
			int duplicateRecordsCount = processCollecteeMasterRecords(collecteeList, collectorPan, tcsBatchUpload,
					tenantId, userName, collectorTan);
			// increment duplicate count if same deductee in database exists with same
			// sections and pan then
			// marking as duplicate.
			duplicateCount += duplicateRecordsCount;
			int processedRecordsCount = collecteeList.size();
			tcsBatchUpload.setSuccessCount(dataRowsCount);
			tcsBatchUpload.setFailedCount(errorCount);
			tcsBatchUpload.setProcessed(processedRecordsCount);
			tcsBatchUpload.setDuplicateCount(0L);
			tcsBatchUpload.setStatus("Processed");
			tcsBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			tcsBatchUpload.setModifiedBy(userName);
			if (!errorList.isEmpty()) {
				deducteeErrorFile = prepareCollecteeMasterErrorFile(uploadedFile.getOriginalFilename(), collectorTan,
						collectorPan, errorList, new ArrayList<>(excelData.getHeaders()));
			}
			// Generating deductee pan file and uploading to pan validation
			MultipartFile file = generateCollecteePanXlsxReport(collecteeList);
			String panUrl = blob.uploadExcelToBlob(file, tenantId);
			tcsBatchUpload.setOtherFileUrl(panUrl);

		} catch (Exception e) {
			tcsBatchUpload.setStatus("File Error");
			logger.error("Exception occurred while uploading collectee:" + ExceptionUtils.getRootCauseMessage(e));

		}

		return collecteeBatchUpload(tcsBatchUpload, null, collectorTan, assesssmentYear, assessmentMonth, userName,
				deducteeErrorFile, tenantId);
	}

	/**
	 * 
	 * @param originalFileName
	 * @param collectorTan
	 * @param collectorPan
	 * @param errorList
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public File prepareCollecteeMasterErrorFile(String originalFileName, String collectorTan, String collectorPan,
			ArrayList<CollecteeMasterErrorReportCsvDTO> errorList, ArrayList<String> headers) throws Exception {
		try {
			headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = collecteeMasterXlsxReport(errorList, collectorTan, collectorPan, headers);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			wkBook.save(baout, SaveFormat.XLSX);
			File errorFile = new File(
					FilenameUtils.getBaseName(originalFileName) + "_" + UUID.randomUUID() + "_Error_Report.xlsx");
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
	 * @param errorDTOs
	 * @param deductorTan
	 * @param deductorPan
	 * @param headerNames
	 * @return
	 * @throws Exception
	 */
	public Workbook collecteeMasterXlsxReport(List<CollecteeMasterErrorReportCsvDTO> errorDTOs, String deductorTan,
			String collectorPan, ArrayList<String> headerNames) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForCollecteeMaster(errorDTOs, worksheet, deductorTan, headerNames);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A6:B6");
		headerColorRange1.setStyle(style1);

		Cell cellD6 = worksheet.getCells().get("C6");
		Style styleD6 = cellD6.getStyle();
		styleD6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleD6.setPattern(BackgroundType.SOLID);
		styleD6.getFont().setBold(true);
		styleD6.getFont().setColor(Color.getWhite());
		styleD6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellD6.setStyle(styleD6);

		// Style for D6 to AR6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:AW6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		List<DeductorMaster> response = deductorMasterDAO.findByDeductorPan(collectorPan);

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		if (!response.isEmpty()) {
			cellA2.setValue("Client Name:" + response.get(0).getName());
		} else {
			cellA2.setValue("Client Name:" + StringUtils.EMPTY);
		}

		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B5 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "AW6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:AW6");
		worksheet.autoFitColumn(1);
		worksheet.autoFitRows();
		return workbook;
	}

	/**
	 * 
	 * @param errorDTOs
	 * @param worksheet
	 * @param deductorTan
	 * @param headerNames
	 * @throws Exception
	 */
	public void setExtractDataForCollecteeMaster(List<CollecteeMasterErrorReportCsvDTO> errorDTOs, Worksheet worksheet,
			String deductorTan, List<String> headerNames) throws Exception {
		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			int serialNumber = 1;
			for (int i = 0; i < errorDTOs.size(); i++) {
				CollecteeMasterErrorReportCsvDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = Excel.getValues(errorDTO, CollecteeMasterExcel.fieldMappings, headerNames);
				rowData.set(0, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(2, String.valueOf(serialNumber));
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
				serialNumber++;
			}
		}
	}

	/**
	 * 
	 * @param collecteePans
	 * @return
	 * @throws Exception
	 */
	public MultipartFile generateCollecteePanXlsxReport(List<CollecteeMaster> collecteePans) throws Exception {

		// for unique pan's
		Map<String, CollecteeMaster> collecteeMap = new HashMap<>();
		for (CollecteeMaster collecteePan : collecteePans) {
			collecteeMap.put(collecteePan.getCollecteePan(), collecteePan);
		}
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		String[] headerNames = new String[] { "PAN", "GIVEN NAME" };
		worksheet.getCells().importArray(headerNames, 0, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);
		if (!collecteePans.isEmpty()) {
			int rowIndex = 1;
			for (Map.Entry<String, CollecteeMaster> entry : collecteeMap.entrySet()) {
				CollecteeMaster collecteePan = entry.getValue();

				ArrayList<String> rowData = new ArrayList<>();
				rowData.add(StringUtils.isBlank(collecteePan.getCollecteePan()) ? StringUtils.EMPTY
						: collecteePan.getCollecteePan());
				rowData.add(StringUtils.isBlank(collecteePan.getNameOfTheCollectee()) ? StringUtils.EMPTY
						: collecteePan.getNameOfTheCollectee());
				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}

		}
		File file = new File("pan_upload_template_" + UUID.randomUUID() + ".xlsx");
		OutputStream out = new FileOutputStream(file);
		workbook.save(out, SaveFormat.XLSX);
		InputStream inputstream = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "application/vnd.ms-excel",
				IOUtils.toByteArray(inputstream));
		inputstream.close();
		out.close();

		return multipartFile;
	}

	/**
	 * 
	 * @param collecteeList
	 * @param collectorPan
	 * @param batch
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	public int processCollecteeMasterRecords(List<CollecteeMaster> collecteeList, String collectorPan,
			TCSBatchUpload batch, String tenantId, String userName, String collectorTan) throws Exception {
		NatureOfPaymentMasterDTO natureOfPaymentMasterDTO = mastersClient.getNatureOfIncomeBySection("206C(1H)")
				.getBody().getData();
		TCSMasterDTO tCSMasterDTO = mastersClient.getRateMasterByNoiId(natureOfPaymentMasterDTO.getId()).getBody()
				.getData();
		for (CollecteeMaster collectee : collecteeList) {
			List<CollecteeMaster> listCollecteeData = collecteeMasterDAO.getColleteeData(collectorPan,
					collectee.getCollecteeCode());
			// get threshold ledger table data based on collectee pan.
			List<CollecteeNoiThresholdLedger> collecteeNoiThresholdLedgerList = new ArrayList<>();
			Integer noiYear = collectee.getCurrentBalanceYear();
			if (collectee.getCurrentBalanceYear() == null) {
				noiYear = CommonUtil.getAssessmentYear(null);
			}

			collecteeNoiThresholdLedgerList = collecteeNoiThresholdLedgerDAO.findByCollecteeCodeOrCollecteePan(
					collectee.getCollecteeCode(), collectorPan, collectee.getCollecteePan(), noiYear);

			// get collection reference id based on collector pan.
			String collectionReferenceId = collectorOnBoardingInfoDAO.getCollectionReferenceId(collectorPan);
			BigDecimal amount = BigDecimal.ZERO;
			if (StringUtils.isNotBlank(collectionReferenceId)) {
				amount = collectee.getAdvanceBalancesForSection206c()
						.add(collectee.getCollectionsBalancesForSection206c());
				logger.info("amount is {}: ", amount);
			} else {
				amount = collectee.getBalancesForSection206c().add(collectee.getAdvanceBalancesForSection206c())
						.add(collectee.getCollectionsBalancesForSection206c());
				logger.info("amount is {}: ", amount);
			}
			// get buyer Threshold Computation Id based on collector pan
			String buyerThresholdComputationId = collectorOnBoardingInfoDAO
					.getBuyerThresholdComputationId(collectorPan);
			if (buyerThresholdComputationId != null) {

			}
			CollecteeNoiThresholdLedger collecteeNoiThresholdLedger = new CollecteeNoiThresholdLedger();
			// update into collectee noi threshold
			// collectee NOI save
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
				collecteeNoiThresholdLedgerSave(natureOfPaymentMasterDTO, collectee, collecteeNoiThresholdLedger,
						userName, amount, noiYear);
			}
			// check of advance balances for section 206c amount
			BigDecimal advanceAmount = collectee.getAdvanceBalancesForSection206c();
			logger.info("advance Amount is {}: ", advanceAmount);
			// check greater then zero
			int advanceAmountCheck = advanceAmount.compareTo(BigDecimal.valueOf(0));
			if (advanceAmountCheck == 1) {
				// payment save
				paymentSave(userName, natureOfPaymentMasterDTO, collectee, tCSMasterDTO,
						collectee.getCurrentBalanceMonth(), collectee.getAdvanceBalancesForSection206c(), noiYear);
			}
			// check for advance as of march amount
			BigDecimal advanceAsOfMarchAmount = collectee.getAdvancesAsOfMarch();
			logger.info("advance As Of Amount is {}: ", advanceAsOfMarchAmount);
			// check greater then zero
			int advanceAsOfMarchAmountCheck = advanceAsOfMarchAmount.compareTo(BigDecimal.valueOf(0));
			if (advanceAsOfMarchAmountCheck == 1) {
				// payment save
				paymentSave(userName, natureOfPaymentMasterDTO, collectee, tCSMasterDTO,
						collectee.getPreviousBalanceMonth(), advanceAsOfMarchAmount,
						collectee.getPreviousBalanceYear());
			}

			Map<String, BigDecimal> additionalSection = new HashMap<>();
			if (!listCollecteeData.isEmpty()) {
				collectee.setId(listCollecteeData.get(0).getId());
				if (StringUtils.isNotBlank(listCollecteeData.get(0).getTcsSection())) {
					String dbAdditionalSections = StringUtils.EMPTY;
					if (StringUtils.isBlank(collectee.getTcsSection())) {
						collectee.setTcsSection(listCollecteeData.get(0).getTcsSection());
						collectee.setTcsRate(listCollecteeData.get(0).getTcsRate());
						collectee.setNatureOfIncome(listCollecteeData.get(0).getNatureOfIncome());
						if (!listCollecteeData.get(0).getAdditionalSections().equals("{}")) {
							dbAdditionalSections = listCollecteeData.get(0).getAdditionalSections();
							logger.info("DB Additional Seciton is :{}", dbAdditionalSections);
							collectee.setAdditionalSections(dbAdditionalSections);
							collectee.setIsEligibleForMultipleSections(true);
						}
					} else if (!listCollecteeData.get(0).getTcsSection().equals(collectee.getTcsSection())
							&& !listCollecteeData.get(0).getNatureOfIncome().equals(collectee.getNatureOfIncome())) {
						String sectionAndNoi = collectee.getTcsSection() + "-" + collectee.getNatureOfIncome();
						additionalSection.put(sectionAndNoi, collectee.getTcsRate());
						String newSection = objectMapper.writeValueAsString(additionalSection);
						if (!listCollecteeData.get(0).getAdditionalSections().equals("{}")) {
							dbAdditionalSections = listCollecteeData.get(0).getAdditionalSections();
							logger.info("DB Additional Seciton is :{}", dbAdditionalSections);
						}
						logger.info("New Additional Seciton is :{}", newSection);
						String sections = newSection.concat(dbAdditionalSections);
						String addtionalSections = sections.replace("}{", ",");
						logger.info("Additional Seciton is :{}", addtionalSections);
						collectee.setAdditionalSections(addtionalSections);
						collectee.setIsEligibleForMultipleSections(true);
						collectee.setTcsSection(listCollecteeData.get(0).getTcsSection());
						collectee.setTcsRate(listCollecteeData.get(0).getTcsRate());
						collectee.setNatureOfIncome(listCollecteeData.get(0).getNatureOfIncome());
					}
				}
				collecteeMasterDAO.updateCollecteeExcel(collectee);
			} else {
				collecteeMasterDAO.save(collectee);
			}
		}
		// update tcs activity tracker
		updateActivityStatus(collectorTan, collectorPan, userName);
		return 0;
	}

	/**
	 * 
	 * @param deductorTan
	 * @param deductorPan
	 * @param userName
	 */
	private void updateActivityStatus(String deductorTan, String deductorPan, String userName) {
		Integer assessmentYear = CommonUtil.getAssessmentYear(null);
		Integer assessmentMonth = CommonUtil.getAssessmentMonth(null);
		String startDate = CommonUtil.getQueryDate(CommonUtil.getMonthStartDate(assessmentYear, assessmentMonth));
		String endDate = CommonUtil.getQueryDate(CommonUtil.getMonthEndDate(assessmentYear, assessmentMonth));
		String activityStatus = getCollecteePansStatus(deductorPan, startDate, endDate);
		tcsActivityTrackerService.updateActivity(deductorTan, assessmentYear, assessmentMonth + 1, userName,
				ActivityType.PAN_VERIFICATION.getActivityType(), activityStatus);
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

	/**
	 * 
	 * @param userName
	 * @param natureOfPaymentMasterDTO
	 * @param collectee
	 * @param tCSMasterDTO
	 * @param amount
	 * @param month
	 */
	private void paymentSave(String userName, NatureOfPaymentMasterDTO natureOfPaymentMasterDTO,
			CollecteeMaster collectee, TCSMasterDTO tCSMasterDTO, int month, BigDecimal balanceAmount,
			Integer assessmentYear) {

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
			tcsPaymentDTO.setDocumentNumber(collectee.getCollecteeCode() + collectee.getCollecteePan());
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
			logger.info("tcs Payment record saved in payment table :{}");
		}

	}

	/**
	 * This method for save Threshold ledger
	 * 
	 * @param natureOfPaymentMasterDTO
	 * @param collectee
	 * @param collecteeNoiThresholdLedger
	 */
	private void collecteeNoiThresholdLedgerSave(NatureOfPaymentMasterDTO natureOfPaymentMasterDTO,
			CollecteeMaster collectee, CollecteeNoiThresholdLedger collecteeNoiThresholdLedger, String userName,
			BigDecimal amount, Integer currentYear) {
		collecteeNoiThresholdLedger.setActive(true);
		collecteeNoiThresholdLedger.setCreatedDate(new Timestamp(new Date().getTime()));
		collecteeNoiThresholdLedger.setCreatedBy(userName);
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
		collecteeNoiThresholdLedger.setYear(currentYear == 0 ? CommonUtil.getAssessmentYear(null) : currentYear);
		collecteeNoiThresholdLedgerDAO.save(collecteeNoiThresholdLedger);
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
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("batch", tcsBatchUpload);
		if (file != null) {
			String errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
			tcsBatchUpload.setErrorFilePath(errorFp);
		}
		if (mFile != null) {
			String path = blob.uploadExcelToBlob(mFile);
			tcsBatchUpload.setFileName(mFile.getOriginalFilename());
			tcsBatchUpload.setFilePath(path);
		}
		int month = assessmentMonthPlusOne;
		tcsBatchUpload.setAssessmentMonth(month);
		tcsBatchUpload.setAssessmentYear(assesssmentYear);
		tcsBatchUpload.setCollectorMasterTan(tan);
		tcsBatchUpload.setUploadType(UploadTypes.COLLECTEE_EXCEL.name());
		tcsBatchUpload.setCreatedBy(userName);
		tcsBatchUpload.setActive(true);
		if (tcsBatchUpload.getId() != null) {
			tcsBatchUpload.setId(tcsBatchUpload.getId());
			return tcsBatchUploadDAO.update(tcsBatchUpload);
		} else {
			return tcsBatchUploadDAO.save(tcsBatchUpload);
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
		if (collecteeNoiThresholdLedger.getCollecteeCode() != null && collecteePan != null) {
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
}
