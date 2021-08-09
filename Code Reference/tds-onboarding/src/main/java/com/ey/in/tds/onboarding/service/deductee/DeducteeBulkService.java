package com.ey.in.tds.onboarding.service.deductee;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.ThresholdLimitGroupMaster;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.model.deductee.DeducteeMasterNonResidentialErrorReportCsvDTO;
import com.ey.in.tds.common.model.deductee.DeducteeMasterResidentialErrorReportCsvDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterNonResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeNopGroup;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.service.ErrorReportService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.jdbc.dao.DeducteeMasterNonResidentialDAO;
import com.ey.in.tds.jdbc.dao.DeducteeMasterResidentialDAO;
import com.ey.in.tds.jdbc.dao.DeducteeNopGroupDAO;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

@Service
public class DeducteeBulkService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DeducteeMasterResidentialDAO deducteeMasterResidentialDAO;

	@Autowired
	private DeducteeMasterNonResidentialDAO deducteeMasterNonResidentialDAO;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private DeducteeNopGroupDAO deducteeNopGroupDAO;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private ErrorReportService errorReportService;

	ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 
	 * @param filePath
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param batchUpload
	 * @return
	 * @throws Exception
	 */
	@Async
	public BatchUpload processResidentDeductees(String filePath, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonthPlusOne, String userName, String tenantId, String deductorPan,
			BatchUpload batchUpload) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		ArrayList<DeducteeMasterResidentialErrorReportCsvDTO> errorList = new ArrayList<>();
		File deducteeErrorFile = null;
		try {
			File csvFile = blobStorageService.getFileFromBlobUrl(tenantId, filePath);

			CsvReader csvReader = new CsvReader();
			csvReader.setContainsHeader(true);
			CsvContainer csv = csvReader.read(csvFile, StandardCharsets.UTF_8);

			Long errorCount = 0L;
			// deductee batch update applicable to data.
			List<DeducteeMasterResidential> batchUpdateApplicableTo = new ArrayList<>();
			// deductee batch update.
			List<DeducteeMasterResidential> deducteeBatchUpdate = new ArrayList<>();
			// deductee batch save.
			List<DeducteeMasterResidential> deducteeBatchSave = new ArrayList<>();
			// deductee nop batch save.
			List<DeducteeNopGroup> deducteeNopBatchSave = new ArrayList<>();
			// deductee nop batch update.
			List<DeducteeNopGroup> deducteeNopBatchUpdate = new ArrayList<>();
			// advance batch save.
			List<AdvanceDTO> advanceBatchSave = new ArrayList<>();
			// provision batch update.
			List<ProvisionDTO> provisionBatchSave = new ArrayList<>();

			List<DeducteeMasterResidential> deducteeStaggingInsert = new ArrayList<>();

			List<DeducteeMasterResidential> deducteeList = new ArrayList<>();
			Map<String, List<DeducteeMasterResidential>> deducteeMap = new HashMap<>();
			Set<String> resDedcuteeSet = new HashSet<>();
			Set<String> nrDedcuteeSet = new HashSet<>();
			Map<String, List<Map<String, Object>>> natureOfPaymentMap = new HashMap<>();
			Map<String, List<Map<String, Object>>> sectionMap = new HashMap<>();
			Map<String, Integer> nopAndSectionMap = new HashMap<>();
			Map<Integer, Double> nopIdMap = new HashMap<>();
			Map<String, String> deducteeStatusMap = mastersClient.getAllDeducteeStatus().getBody();
			List<Map<String, Object>> thresholdMap = mastersClient.getThresholdGroupData().getBody().getData();
			// Threshold limit group data
			List<ThresholdLimitGroupMaster> thresholdLimitMaster = mastersClient.getThresholdGroupByIds().getBody()
					.getData();
			// Nature of payment data
			List<Map<String, Object>> nopMap = mastersClient.getNOPAndSectionsResidentialStatus("RES").getBody()
					.getData();
			List<DeductorMaster> deductorMaster = deductorMasterDAO.findByDeductorPan(deductorPan);
			String deductorCode = StringUtils.EMPTY;
			String deductorName = StringUtils.EMPTY;
			if (!deductorMaster.isEmpty()) {
				deductorCode = deductorMaster.get(0).getCode();
				deductorName = deductorMaster.get(0).getName();
			}
			for (Map<String, Object> map : nopMap) {
				String nature = (String) map.get("nature");
				String section = (String) map.get("section");
				Integer nopId = (Integer) map.get("natureOfPaymentId");
				Double rate = (Double) map.get("rate");
				String key = section + "-" + nature;
				if (!natureOfPaymentMap.containsKey(key)) {
					natureOfPaymentMap.put(key, new ArrayList<>());
				}
				natureOfPaymentMap.get(key).add(map);
				if (!sectionMap.containsKey(section)) {
					sectionMap.put(section, new ArrayList<>());
				}
				sectionMap.get(section).add(map);
				nopAndSectionMap.put(key, nopId);
				nopIdMap.put(nopId, rate);
			}
			Map<String, List<DeducteeMasterResidential>> deducteeResMap = getDeducteeResidentRecords(deductorPan);
			int serialNumber = 1;
			int totalRecords = 0;
			for (CsvRow row : csv.getRows()) {
				if (StringUtils.isNotBlank(StringUtils.join(row.getFields(), ", ").replace(",", ""))) {
					++totalRecords;
					boolean isResStatusValid = true;
					DeducteeMasterResidentialErrorReportCsvDTO residentErrorDTO = new DeducteeMasterResidentialErrorReportCsvDTO();
					if (StringUtils.isBlank(residentErrorDTO.getReason())) {
						residentErrorDTO.setReason("");
					}
					DeducteeMasterResidential deductee = getDeducteeCsvMapping(row, residentErrorDTO);
					deductee.setActive(true);
					deductee.setBatchUploadId(batchUpload.getBatchUploadID());
					deductee.setIsDeducteeHasAdditionalSections(false);
					deductee.setCreatedBy(userName);
					deductee.setCreatedDate(new Timestamp(new Date().getTime()));
					deductee.setInvoiceTransactionCount(0);
					deductee.setProvisionTransactionCount(0);
					deductee.setAdvanceTransactionCount(0);
					deductee.setDeducteeCode(
							StringUtils.isNotBlank(deductee.getDeducteeCode()) ? deductee.getDeducteeCode().trim()
									: "");
					deductee.setPhoneNumber(deductee.getPhoneNumber() != null ? deductee.getPhoneNumber().trim() : "");
					deductee.setDeducteePAN(
							StringUtils.isNotBlank(deductee.getDeducteePAN()) ? deductee.getDeducteePAN().trim() : "");
					deductee.setDeductorCode(deductorCode);
					deductee.setDeductorName(deductorName);
					if (StringUtils.isNotBlank(deductee.getNatureOfPayment())) {
						deductee.setNatureOfPayment(deductee.getNatureOfPayment().trim());
					}
					if (deductee.getApplicableFrom() == null) {
						deductee.setApplicableFrom(new Timestamp(new Date().getTime()));
					}
					if (StringUtils.isBlank(deductee.getDeductorPan())) {
						residentErrorDTO.setReason(residentErrorDTO.getReason() + "Deductor pan is mandatory." + "\n");
					}
					if (StringUtils.isBlank(deductee.getDeducteeResidentialStatus())) {
						residentErrorDTO.setReason(
								residentErrorDTO.getReason() + "Deductee residential status is mandatory." + "\n");
					} else if ("RESIDENT".equalsIgnoreCase(deductee.getDeducteeResidentialStatus())
							|| "RES".equalsIgnoreCase(deductee.getDeducteeResidentialStatus())) {
						deductee.setDeducteeResidentialStatus("RES");
					} else if ("NONRESIDENT".equalsIgnoreCase(deductee.getDeducteeResidentialStatus())
							|| "NR".equalsIgnoreCase(deductee.getDeducteeResidentialStatus())) {
						deductee.setDeducteeResidentialStatus("NR");
					}
					if (StringUtils.isBlank(deductee.getDeducteeName())) {
						residentErrorDTO.setReason(residentErrorDTO.getReason() + "Deductee name is mandatory." + "\n");
					}
					if ("RES".equalsIgnoreCase(deductee.getDeducteeResidentialStatus())) {
						String deducteeName = deductee.getDeducteeName().trim().toLowerCase();
						deducteeName = deducteeName.replaceAll("[^a-z0-9 ]", "");
						deductee.setModifiedName(deducteeName);
						// deducteee key
						deductee.setDeducteeKey(getDeducteeKeyValue(deductee));
						// Deductee Enrichment Key
						if (StringUtils.isNotBlank(deductee.getDeducteeCode())) {
							deductee.setDeducteeEnrichmentKey(deductee.getDeducteeCode());
						} else {
							String name = deductee.getDeducteeName().toLowerCase();
							name = name.replaceAll("[^a-z0-9 ]", "").replace(" ", "-");
							deductee.setDeducteeEnrichmentKey(name);
						}
						// Check for Deductor Pan
						if (StringUtils.isNotBlank(deductee.getDeductorPan())
								&& !deductorPan.equalsIgnoreCase(deductee.getDeductorPan())) {
							residentErrorDTO.setReason(residentErrorDTO.getReason() + "Deductor Pan " + deductorPan
									+ " is not match." + "\n");
						}
						// Check for Deductee Aadhar Number Validation
						if (StringUtils.isNotBlank(deductee.getDeducteeAadharNumber())
								&& !deductee.getDeducteeAadharNumber().matches("[0-9]{12}")) {
							residentErrorDTO.setReason(residentErrorDTO.getReason() + "Deductee Aadhar Number "
									+ deductee.getDeducteeAadharNumber() + " is not valid, enter 12 digit number."
									+ "\n");
							isResStatusValid = false;
						}
						// Check for Deductee Code validation
						if (StringUtils.isNotBlank(deductee.getDeducteeCode())
								&& !deductee.getDeducteeCode().matches("[0-9A-Za-z- ]{1,15}")) {
							residentErrorDTO.setReason(residentErrorDTO.getReason() + "Deductee code "
									+ deductee.getDeducteeCode() + " is not valid, "
									+ "allowed only Alpha numeric, hypen and Space, max length is 15 digits" + "\n");
							isResStatusValid = false;
						}
						// Check for deductee name validation
						if (StringUtils.isNotBlank(deductee.getDeducteeName()) && !deductee.getDeducteeName()
								.matches("^[.\\p{Alnum}\\p{Space}&'-_!@#$()*,)(]{0,1024}$")) {
							residentErrorDTO.setReason(residentErrorDTO.getReason() + "Deductee name "
									+ deductee.getDeducteeName() + " is not valid, " + "\n");
							isResStatusValid = false;
						}
						// check for deductee GSTIN
						if (StringUtils.isNotBlank(deductee.getDeducteeGSTIN()) && !deductee.getDeducteeGSTIN()
								.matches("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")) {
							residentErrorDTO.setReason(
									residentErrorDTO.getReason() + "Deductee GSTIN " + deductee.getDeducteeGSTIN()
											+ " is not valid, enter 15 digit number (Ex:06AAAAA6385P6ZA)." + "\n");
						}
						if ("Other TDS section".equalsIgnoreCase(deductee.getTdsApplicabilityUnderSection())) {
							deductee.setTdsApplicabilityUnderSection("OTHER TDS");
						} else {
							deductee.setTdsApplicabilityUnderSection(deductee.getTdsApplicabilityUnderSection());
						}
						// null check
						deductee.setDeducteeMasterBalancesOf194q(isNull(deductee.getDeducteeMasterBalancesOf194q()));
						deductee.setAdvanceBalancesOf194q(isNull(deductee.getAdvanceBalancesOf194q()));
						deductee.setProvisionBalancesOf194q(isNull(deductee.getProvisionBalancesOf194q()));
						deductee.setAdvancesAsOfMarch(isNull(deductee.getAdvancesAsOfMarch()));
						deductee.setProvisionsAsOfMarch(isNull(deductee.getProvisionsAsOfMarch()));
						deductee.setCurrentBalanceYear(isNull(deductee.getCurrentBalanceYear()));
						deductee.setCurrentBalanceMonth(isNull(deductee.getCurrentBalanceMonth()));
						deductee.setPreviousBalanceYear(isNull(deductee.getPreviousBalanceYear()));
						deductee.setPreviousBalanceMonth(isNull(deductee.getPreviousBalanceMonth()));
						deductee.setOpeningBalanceCreditNote(isNull(deductee.getOpeningBalanceCreditNote()));
						if (deductee.getCurrentBalanceYear() > 0
								&& !String.valueOf(deductee.getCurrentBalanceYear()).matches("[0-9]{4}")) {
							residentErrorDTO.setReason(residentErrorDTO.getReason() + "Current Balance year "
									+ deductee.getCurrentBalanceYear() + " is not valid." + "\n");
						}
						if (deductee.getCurrentBalanceMonth() > 0
								&& !String.valueOf(deductee.getCurrentBalanceMonth()).matches("^([1-9]|[0-1][0-2])$")) {
							residentErrorDTO.setReason(residentErrorDTO.getReason() + "Current Balance month "
									+ deductee.getCurrentBalanceMonth() + " is not valid." + "\n");
						}

						if (deductee.getPreviousBalanceYear() > 0
								&& !String.valueOf(deductee.getPreviousBalanceYear()).matches("[0-9]{4}")) {
							residentErrorDTO.setReason(residentErrorDTO.getReason() + "Previous Balance year "
									+ deductee.getPreviousBalanceYear() + " is not valid." + "\n");
						}

						if (deductee.getPreviousBalanceMonth() > 0 && !String
								.valueOf(deductee.getPreviousBalanceMonth()).matches("^([1-9]|[0-1][0-2])$")) {
							residentErrorDTO.setReason(residentErrorDTO.getReason() + "Previous Balance month "
									+ deductee.getPreviousBalanceMonth() + " is not valid." + "\n");
						}

						if (StringUtils.isNotBlank(deductee.getDeducteePAN())
								&& deductee.getDeducteePAN().length() == 10) {
							deductee.setDeducteePAN(deductee.getDeducteePAN().toUpperCase());
							if (deductee.getDeducteePAN().matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
								if (deducteeStatusMap
										.get(String.valueOf(deductee.getDeducteePAN().charAt(3))) != null) {
									deductee.setDeducteeStatus(
											deducteeStatusMap.get(String.valueOf(deductee.getDeducteePAN().charAt(3))));
								} else {
									residentErrorDTO.setReason(residentErrorDTO.getReason() + "Pan 4th character "
											+ deductee.getDeducteePAN().charAt(3) + " is Invalid." + "\n");
									isResStatusValid = false;
								}
							} else {
								residentErrorDTO.setReason(residentErrorDTO.getReason() + "Pan "
										+ deductee.getDeducteePAN() + " is not valid." + "\n");
								isResStatusValid = false;
							}
						} else if (StringUtils.isNotBlank(deductee.getDeducteeStatus())) {
							if ("Artificial Jurisdical Person".equalsIgnoreCase(deductee.getDeducteeStatus())) {
								deductee.setDeducteeStatus(deducteeStatusMap.get(String
										.valueOf(deductee.getDeducteeStatus().trim().substring(11, 12).toUpperCase())));
							} else if (deducteeStatusMap.get(String.valueOf(
									deductee.getDeducteeStatus().trim().substring(0, 1).toUpperCase())) != null) {
								deductee.setDeducteeStatus(deducteeStatusMap.get(String
										.valueOf(deductee.getDeducteeStatus().trim().substring(0, 1).toUpperCase())));
							}
							if (StringUtils.isBlank(deductee.getDeducteeStatus())) {
								residentErrorDTO.setReason(residentErrorDTO.getReason() + "Deductee Status "
										+ deductee.getDeducteeStatus() + " is not valid." + "\n");
								isResStatusValid = false;
							}
						} else if ((StringUtils.isBlank(deductee.getDeducteePAN())
								&& StringUtils.isBlank(deductee.getDeducteeStatus()))
								|| (deductee.getDeducteePAN().length() != 10
										&& StringUtils.isBlank(deductee.getDeducteeStatus()))) {
							residentErrorDTO
									.setReason(residentErrorDTO.getReason() + "Deductee Status is mandatory." + "\n");
							isResStatusValid = false;
						}

						if (StringUtils.isNotEmpty(deductee.getNatureOfPayment())
								&& StringUtils.isEmpty(deductee.getSection())) {
							// error report
							residentErrorDTO.setReason(residentErrorDTO.getReason()
									+ "Nature Of Payment is allowed if there is a section." + "\n");
						}
						// set section and rate
						if (StringUtils.isBlank(deductee.getSection()) || deductee.getRate() == null) {
							deductee.setRate(BigDecimal.ZERO);
						}
						if (isResStatusValid && StringUtils.isNotBlank(deductee.getSection())) {
							if (sectionMap.get(deductee.getSection()) == null) {
								residentErrorDTO.setReason(residentErrorDTO.getReason() + "Section "
										+ deductee.getSection() + " not found in system." + "\n");
							} else if (StringUtils.isBlank(deductee.getNatureOfPayment())) {
								Map<Double, String> rateMap = new HashMap<>();
								List<Double> rates = new ArrayList<>();
								Double highestRate = 0.0;
								boolean isSectionValid = false;
								if (sectionMap.get(deductee.getSection()) != null) {
									for (Map<String, Object> nopData : sectionMap.get(deductee.getSection())) {
										String nature = (String) nopData.get("nature");
										SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
										String rateApplicableFrom = (String) nopData.get("applicableFrom");
										String rateApplicableTo = (String) nopData.get("applicableTo");
										Date applicableFrom = formatter.parse(rateApplicableFrom);
										Date applicableTo = null;
										if (StringUtils.isNotBlank(rateApplicableTo)) {
											applicableTo = formatter.parse(rateApplicableTo);
										}
										Double rate = (Double) nopData.get("rate");
										if (deductee.getApplicableFrom().getTime() >= applicableFrom.getTime()
												&& (applicableTo == null || deductee.getApplicableTo() == null
														|| deductee.getApplicableTo().getTime() <= applicableTo
																.getTime())) {
											rate = rate != null ? rate : 0.0;
											rateMap.put(rate, nature);
											rates.add(rate);
											isSectionValid = true;
										}
									}
								}
								if (!rates.isEmpty()) {
									// section contains mutiple NOP then get the NOP based on highest rate
									highestRate = Collections.max(rates);
								}
								if (!isSectionValid) {
									residentErrorDTO.setReason(residentErrorDTO.getReason()
											+ "Section is invalid for the mentioned applicable period.");
								} else if (deductee.getRate() != null && deductee.getRate().doubleValue() > 0.0) {
									// section contains mutiple NOP then get the NOP based on closest rate passed in
									// the excel
									Optional<Double> rate = rates.parallelStream().min(Comparator
											.comparingDouble(i -> Math.abs(i - (deductee.getRate().doubleValue()))));
									deductee.setNatureOfPayment(rateMap.get(rate.isPresent() ? rate.get() : 0.0));
								} else {
									deductee.setNatureOfPayment(rateMap.get(highestRate));
								}

							} else if (StringUtils.isNotBlank(deductee.getNatureOfPayment())) {
								String key = deductee.getSection() + "-" + deductee.getNatureOfPayment();
								boolean isNopValid = false;
								if (natureOfPaymentMap.get(key) != null) {
									for (Map<String, Object> nopData : natureOfPaymentMap.get(key)) {
										SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
										String rateApplicableFrom = (String) nopData.get("applicableFrom");
										String rateApplicableTo = (String) nopData.get("applicableTo");
										Date applicableFrom = formatter.parse(rateApplicableFrom);
										Date applicableTo = null;
										if (StringUtils.isNotBlank(rateApplicableTo)) {
											applicableTo = formatter.parse(rateApplicableTo);
										}
										if (deductee.getApplicableFrom().getTime() >= applicableFrom.getTime()
												&& (applicableTo == null || deductee.getApplicableTo() == null
														|| deductee.getApplicableTo().getTime() <= applicableTo
																.getTime())) {
											isNopValid = true;
											break;
										}
									}
								}
								if (!isNopValid) {
									residentErrorDTO.setReason(residentErrorDTO.getReason()
											+ "Section is invalid for the mentioned applicable period.");
								}
							}
						}
						String key = deductee.getDeducteeKey() + "-" + deductee.getSection() + "-"
								+ deductee.getDeductorPan();
						if (StringUtils.isBlank(residentErrorDTO.getReason()) && resDedcuteeSet.contains(key)) {
							residentErrorDTO.setReason(residentErrorDTO.getReason() + "Duplicate." + "\n");
						}
						if (StringUtils.isBlank(residentErrorDTO.getReason())) {
							deducteeList.add(deductee);
							resDedcuteeSet.add(key);
							String deducteeStr = deductee.getDeducteeKey();
							if (!deducteeMap.containsKey(deducteeStr)) {
								deducteeMap.put(deducteeStr, new ArrayList<>());
							}
							deducteeMap.get(deducteeStr).add(deductee);
						}
					} else if ("NR".equalsIgnoreCase(deductee.getDeducteeResidentialStatus())) {
						deductee.setDeducteeKey(getDeducteeKeyValue(deductee));
						String key = deductee.getDeducteeKey() + "-" + deductee.getSection() + "-"
								+ deductee.getDeductorPan();
						if (StringUtils.isBlank(residentErrorDTO.getReason()) && nrDedcuteeSet.contains(key)) {
							residentErrorDTO.setReason(residentErrorDTO.getReason() + "Duplicate." + "\n");
						} else {
							nrDedcuteeSet.add(key);
						}
						deducteeStaggingInsert.add(deductee);
					}
					if (StringUtils.isNotBlank(residentErrorDTO.getReason())) {
						++errorCount;
						residentErrorDTO.setSerialNumber(String.valueOf(serialNumber));
						residentErrorDTO = getDeducteeErrorCsvMapping(row, residentErrorDTO);
						errorList.add(residentErrorDTO);
					}
					serialNumber++;
				}
			} // for 1
			if (!deducteeList.isEmpty()) {
				processResidentRecords(deducteeList, deductorPan, deducteeMap, nopAndSectionMap, nopIdMap, thresholdMap,
						batchUpdateApplicableTo, deducteeBatchSave, deducteeBatchUpdate, thresholdLimitMaster,
						deducteeNopBatchSave, deducteeNopBatchUpdate, deducteeResMap, advanceBatchSave,
						provisionBatchSave);
			}

			if (!batchUpdateApplicableTo.isEmpty()) {
				// deductee batch update applicable to date.
				deducteeMasterResidentialDAO.batchUpdateApplicableToDate(batchUpdateApplicableTo);
			}
			if (!deducteeBatchSave.isEmpty()) {
				// deductee batch save.
				deducteeMasterResidentialDAO.deducteeBatchSave(deducteeBatchSave, tenantId);
			}
			if (!deducteeStaggingInsert.isEmpty()) {
				// deductee stagging batch save.
				deducteeMasterResidentialDAO.deducteeStaggingBatchSave(deducteeStaggingInsert, tenantId);
			}
			if (!deducteeBatchUpdate.isEmpty()) {
				// deductee batch update
				deducteeMasterResidentialDAO.deducteeBatchUpdate(deducteeBatchUpdate);
			}
			if (!deducteeNopBatchSave.isEmpty()) {
				// deductee nop batch save
				deducteeMasterResidentialDAO.deducteeNopBatchSave(deducteeNopBatchSave, tenantId);
			}
			if (!deducteeNopBatchUpdate.isEmpty()) {
				// deductee nop batch update
				deducteeMasterResidentialDAO.deducteeNopBatchUpdate(deducteeNopBatchUpdate);
			}
			if (!advanceBatchSave.isEmpty()) {
				// advance batch update
				deducteeMasterResidentialDAO.advanceBatchSave(advanceBatchSave);
				// update ancestor id
				deducteeMasterResidentialDAO.updateAdvanceAncestorId(batchUpload.getBatchUploadID(), deductorTan);
			}
			if (!provisionBatchSave.isEmpty()) {
				// provision batch update
				deducteeMasterResidentialDAO.provisionBatchSave(provisionBatchSave);
				// update ancestor id
				deducteeMasterResidentialDAO.updateProvisionAncestorId(batchUpload.getBatchUploadID(), deductorTan);
			}
			batchUpload.setSuccessCount(Long.valueOf(totalRecords));
			batchUpload.setRowsCount(Long.valueOf(totalRecords));
			batchUpload.setFailedCount(errorCount);
			batchUpload.setProcessedCount(resDedcuteeSet.size() + nrDedcuteeSet.size());
			batchUpload.setDuplicateCount(0L);
			batchUpload.setStatus("Processed");
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			if (!errorList.isEmpty()) {
				deducteeErrorFile = prepareResidentDeducteesErrorFile(batchUpload.getFileName(), deductorTan,
						deductorPan, errorList);
			}

			// Generating deductee pan file and uploading to pan validation
			MultipartFile file = generateDeducteePanXlsxReport(Collections.emptyList(), deducteeList);
			String panUrl = blob.uploadExcelToBlob(file, tenantId);
			batchUpload.setOtherFileUrl(panUrl);

		} catch (Exception e) {
			logger.error("Exception occurred :{}", e.getMessage());
		}

		return deducteeBatchUpload(batchUpload, null, deductorTan, assesssmentYear, assessmentMonthPlusOne, userName,
				deducteeErrorFile, tenantId);
	}

	private Map<String, List<DeducteeMasterResidential>> getDeducteeResidentRecords(String deductorPan) {
		Map<String, List<DeducteeMasterResidential>> deducteeMap = new HashMap<>();
		List<DeducteeMasterResidential> deductees = deducteeMasterResidentialDAO.getDeducteesByPan(deductorPan);
		for (DeducteeMasterResidential deductee : deductees) {
			if (!deducteeMap.containsKey(deductee.getDeducteeKey())) {
				deducteeMap.put(deductee.getDeducteeKey(), new ArrayList<>());
			}
			deducteeMap.get(deductee.getDeducteeKey()).add(deductee);
		}
		return deducteeMap;
	}

	public String getDeducteeKeyValue(DeducteeMasterResidential deductee) {
		String deducteeKey = StringUtils.EMPTY;
		// deducteee key
		if (StringUtils.isNotBlank(deductee.getDeducteeCode())) {
			deducteeKey = deductee.getDeducteeCode();
		} else {
			String name = deductee.getDeducteeName().toLowerCase();
			name = name.replaceAll("[^a-z0-9 ]", "").replace(" ", "-");
			if (StringUtils.isNotBlank(deductee.getDeducteePAN())) {
				deducteeKey = name.concat(deductee.getDeducteePAN());
			} else {
				deducteeKey = name;
			}
		}
		return deducteeKey;
	}

	private DeducteeMasterResidential getDeducteeCsvMapping(CsvRow row,
			DeducteeMasterResidentialErrorReportCsvDTO errorDTO) throws ParseException {
		DeducteeMasterResidential deducteeRow = new DeducteeMasterResidential();
		deducteeRow.setSourceIdentifier(row.getField("SourceIdentifier"));
		deducteeRow.setSourceFileName(row.getField("SourceFileName"));
		deducteeRow.setDeductorCode(row.getField("DeductorCode"));
		deducteeRow.setDeductorName(row.getField("DeductorName"));
		deducteeRow.setDeductorPan(row.getField("DeductorPAN"));
		deducteeRow.setDeductorTan(row.getField("DeductorTAN"));
		deducteeRow.setDeducteeCode(row.getField("DeducteeCode"));
		deducteeRow.setDeducteeName(row.getField("DeducteeName"));
		deducteeRow.setDeducteePAN(row.getField("DeducteePAN"));
		deducteeRow.setDeducteeAadharNumber(row.getField("DeducteeAadharNumber"));
		deducteeRow.setDeducteeStatus(row.getField("DeducteeStatus"));
		deducteeRow.setDeducteeResidentialStatus(row.getField("DeducteeResidentialStatus"));
		deducteeRow.setEmailAddress(row.getField("DeducteeEmail"));
		deducteeRow.setPhoneNumber(row.getField("DeducteePhone"));
		deducteeRow.setFlatDoorBlockNo(row.getField("DeducteeFloorNumber"));
		deducteeRow.setNameBuildingVillage(row.getField("DeducteeBuildingName"));
		deducteeRow.setRoadStreetPostoffice(row.getField("DeducteeStreet"));
		deducteeRow.setAreaLocality(row.getField("DeducteeArea"));
		deducteeRow.setTownCityDistrict(row.getField("DeducteeTown"));
		deducteeRow.setState(row.getField("DeducteeState"));
		deducteeRow.setCountry(row.getField("DeducteeCountry"));
		deducteeRow.setPinCode(row.getField("DeducteePincode"));
		deducteeRow.setSectionCode(row.getField("TDSTaxCodeERP"));
		deducteeRow.setSection(row.getField("TDSSection"));
		deducteeRow.setNatureOfPayment(row.getField("NatureOfPayment"));
		BigDecimal rate = StringUtils.isNotBlank(row.getField("TDSRate"))
				? new BigDecimal(row.getField("TDSRate").trim().replace(",", ""))
				: BigDecimal.ZERO;
		deducteeRow.setRate(rate);
		Boolean tdsExemptionFlag = StringUtils.isNotBlank(row.getField("TDSExemptionFlag"))
				&& row.getField("TDSExemptionFlag").equals("Y");
		deducteeRow.setTdsExcemptionFlag(tdsExemptionFlag);
		deducteeRow.setTdsExcemptionReason(row.getField("TDSExemptionReason"));
		Boolean isThresholdLimitApplicable = StringUtils.isNotBlank(row.getField("TDSThresholdApplicabilityFlag"))
				&& row.getField("TDSThresholdApplicabilityFlag").equals("Y");
		deducteeRow.setIsThresholdLimitApplicable(isThresholdLimitApplicable);
		String applicableFromDate = StringUtils.isNotBlank(row.getField("ApplicableFrom"))
				? row.getField("ApplicableFrom")
				: null;
		if (StringUtils.isNotBlank(applicableFromDate)) {
			String dateString = applicableFromDate.replace("/", "-");
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date applicableFrom = sdf.parse(dateString);
				if (!dateString.equals(sdf.format(applicableFrom))) {
					errorDTO.setReason(
							errorDTO.getReason() + "Applicable from date should be in YYYY-MM-DD format." + "\n");
				}
				deducteeRow.setApplicableFrom(applicableFrom);
			} catch (Exception e) {
				errorDTO.setReason(
						errorDTO.getReason() + "Applicable from date should be in YYYY-MM-DD format." + "\n");
			}
		}
		String applicableToDate = StringUtils.isNotBlank(row.getField("ApplicableTo")) ? row.getField("ApplicableTo")
				: null;
		if (StringUtils.isNotBlank(applicableToDate)) {
			String dateString = applicableToDate.replace("/", "-");
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date applicableTo = sdf.parse(dateString);
				if (!dateString.equals(sdf.format(applicableTo))) {
					errorDTO.setReason(
							errorDTO.getReason() + "Applicable to date should be in YYYY-MM-DD format." + "\n");
				}
				deducteeRow.setApplicableTo(applicableTo);
			} catch (Exception e) {
				errorDTO.setReason(errorDTO.getReason() + "Applicable to date should be in YYYY-MM-DD format." + "\n");
			}
		}
		deducteeRow.setTdsExemptionNumber(row.getField("TDSExemptionNumber"));
		deducteeRow.setDeducteeGSTIN(row.getField("DeducteeGSTIN"));
		deducteeRow.setGrOrIRIndicator(row.getField("GR/IRIndicator"));
		deducteeRow.setTdsApplicabilityUnderSection(row.getField("TDSApplicabilityin194QvsTDSothersections"));
		BigDecimal openingBalanceInvoices = StringUtils.isNotBlank(row.getField("OpeningBalanceInvoices"))
				? new BigDecimal(row.getField("OpeningBalanceInvoices").trim().replace(",", ""))
				: BigDecimal.ZERO;
		deducteeRow.setDeducteeMasterBalancesOf194q(openingBalanceInvoices);
		BigDecimal openingBalanceAdvances = StringUtils.isNotBlank(row.getField("OpeningBalanceAdvances"))
				? new BigDecimal(row.getField("OpeningBalanceAdvances").trim().replace(",", "").trim())
				: BigDecimal.ZERO;
		deducteeRow.setAdvanceBalancesOf194q(openingBalanceAdvances);
		BigDecimal openingBalanceProvisions = StringUtils.isNotBlank(row.getField("OpeningBalanceProvisions"))
				? new BigDecimal(row.getField("OpeningBalanceProvisions").trim().replace(",", "").trim())
				: BigDecimal.ZERO;
		deducteeRow.setProvisionBalancesOf194q(openingBalanceProvisions);
		BigDecimal openingBalanceCreditNote = StringUtils.isNotBlank(row.getField("OpeningBalanceCreditNote"))
				? new BigDecimal(row.getField("OpeningBalanceCreditNote").trim().replace(",", ""))
				: BigDecimal.ZERO;
		deducteeRow.setOpeningBalanceCreditNote(openingBalanceCreditNote);
		int currentBalanceMonth = StringUtils.isNotBlank(row.getField("CurrentBalanceMonth"))
				? Integer.parseInt(row.getField("CurrentBalanceMonth"))
				: 0;
		deducteeRow.setCurrentBalanceMonth(currentBalanceMonth);
		int currentBalanceYear = StringUtils.isNotBlank(row.getField("CurrentBalanceYear"))
				? Integer.parseInt(row.getField("CurrentBalanceYear"))
				: 0;
		deducteeRow.setCurrentBalanceYear(currentBalanceYear);
		BigDecimal advancesAsOfMarch = StringUtils.isNotBlank(row.getField("AdvancesAsOfMarch"))
				? new BigDecimal(row.getField("AdvancesAsOfMarch").trim().replace(",", ""))
				: BigDecimal.ZERO;
		deducteeRow.setAdvancesAsOfMarch(advancesAsOfMarch);
		BigDecimal provisionsAsOfMarch = StringUtils.isNotBlank(row.getField("ProvisionsAsOfMarch"))
				? new BigDecimal(row.getField("ProvisionsAsOfMarch").trim().replace(",", ""))
				: BigDecimal.ZERO;
		deducteeRow.setProvisionsAsOfMarch(provisionsAsOfMarch);
		int previousBalanceMonth = StringUtils.isNotBlank(row.getField("PreviousBalanceMonth"))
				? Integer.parseInt(row.getField("PreviousBalanceMonth"))
				: 0;
		deducteeRow.setPreviousBalanceMonth(previousBalanceMonth);
		int previousBalanceYear = StringUtils.isNotBlank(row.getField("PreviousBalanceYear"))
				? Integer.parseInt(row.getField("PreviousBalanceYear"))
				: 0;
		deducteeRow.setPreviousBalanceYear(previousBalanceYear);
		deducteeRow.setDeducteeTan(row.getField("DeducteeTAN"));
		deducteeRow.setTinUniqueIdentification(row.getField("DeducteeTIN"));
		deducteeRow.setTdsSectionDescription(row.getField("TDSSectionDescription"));
		deducteeRow.setUserDefinedField1(row.getField("UserDefinedField1"));
		deducteeRow.setUserDefinedField2(row.getField("UserDefinedField2"));
		deducteeRow.setUserDefinedField3(row.getField("UserDefinedField3"));
		deducteeRow.setUserDefinedField4(row.getField("UserDefinedField4"));
		deducteeRow.setUserDefinedField5(row.getField("UserDefinedField5"));
		deducteeRow.setUserDefinedField6(row.getField("UserDefinedField6"));
		deducteeRow.setUserDefinedField7(row.getField("UserDefinedField7"));
		deducteeRow.setUserDefinedField8(row.getField("UserDefinedField8"));
		deducteeRow.setUserDefinedField9(row.getField("UserDefinedField9"));
		deducteeRow.setUserDefinedField10(row.getField("UserDefinedField10"));

		return deducteeRow;
	}

	private DeducteeMasterResidentialErrorReportCsvDTO getDeducteeErrorCsvMapping(CsvRow row,
			DeducteeMasterResidentialErrorReportCsvDTO errorReportDTO) {
		errorReportDTO.setSourceIdentifier(row.getField("SourceIdentifier"));
		errorReportDTO.setSourceFileName(row.getField("SourceFileName"));
		errorReportDTO.setDeductorCode(row.getField("DeductorCode"));
		errorReportDTO.setDeductorName(row.getField("DeductorName"));
		errorReportDTO.setDeductorPan(row.getField("DeductorPAN"));
		errorReportDTO.setDeductorTan(row.getField("DeductorTAN"));
		errorReportDTO.setDeducteeCode(row.getField("DeducteeCode"));
		errorReportDTO.setDeducteeName(row.getField("DeducteeName"));
		errorReportDTO.setDeducteePAN(row.getField("DeducteePAN"));
		errorReportDTO.setDeducteeAadharNumber(row.getField("DeducteeAadharNumber"));
		errorReportDTO.setDeducteeStatus(row.getField("DeducteeStatus"));
		errorReportDTO.setDeducteeResidentialStatus(row.getField("DeducteeResidentialStatus"));
		errorReportDTO.setEmailAddress(row.getField("DeducteeEmail"));
		errorReportDTO.setPhoneNumber(row.getField("DeducteePhone"));
		errorReportDTO.setFlatDoorBlockNo(row.getField("DeducteeFloorNumber"));
		errorReportDTO.setNameBuildingVillage(row.getField("DeducteeBuildingName"));
		errorReportDTO.setRoadStreetPostoffice(row.getField("DeducteeStreet"));
		errorReportDTO.setAreaLocality(row.getField("DeducteeArea"));
		errorReportDTO.setTownCityDistrict(row.getField("DeducteeTown"));
		errorReportDTO.setState(row.getField("DeducteeState"));
		errorReportDTO.setCountry(row.getField("DeducteeCountry"));
		errorReportDTO.setPinCode(row.getField("DeducteePincode"));
		errorReportDTO.setSectionCode(row.getField("TDSTaxCodeERP"));
		errorReportDTO.setSection(row.getField("TDSSection"));
		errorReportDTO.setNatureOfPayment(row.getField("NatureOfPayment"));
		errorReportDTO.setRate(row.getField("TDSRate"));
		errorReportDTO.setTdsExcemptionFlag(row.getField("TDSExemptionFlag"));
		errorReportDTO.setTdsExemptionReason(row.getField("TDSExemptionReason"));
		errorReportDTO.setIsThresholdLimitApplicable(row.getField("TDSThresholdApplicabilityFlag"));
		errorReportDTO.setApplicableFrom(row.getField("ApplicableFrom"));
		errorReportDTO.setApplicableTo(row.getField("ApplicableTo"));
		errorReportDTO.setTdsExemptionNumber(row.getField("TDSExemptionNumber"));
		errorReportDTO.setDeducteeGSTIN(row.getField("DeducteeGSTIN"));
		errorReportDTO.setGrOrIRIndicator(row.getField("GR/IRIndicator"));
		errorReportDTO.setTdsApplicabilityUnderSection(row.getField("TDSApplicabilityin194QvsTDSothersections"));
		errorReportDTO.setDeducteeMasterBalancesOf194q(row.getField("OpeningBalanceInvoices"));
		errorReportDTO.setAdvanceBalancesOf194q(row.getField("OpeningBalanceAdvances"));
		errorReportDTO.setProvisionBalancesOf194q(row.getField("OpeningBalanceProvisions"));
		errorReportDTO.setOpeningBalanceCreditNote(row.getField("OpeningBalanceCreditNote"));
		errorReportDTO.setCurrentBalanceMonth(row.getField("CurrentBalanceMonth"));
		errorReportDTO.setCurrentBalanceYear(row.getField("CurrentBalanceYear"));
		errorReportDTO.setAdvancesAsOfMarch(row.getField("AdvancesAsOfMarch"));
		errorReportDTO.setProvisionsAsOfMarch(row.getField("ProvisionsAsOfMarch"));
		errorReportDTO.setPreviousBalanceMonth(row.getField("PreviousBalanceMonth"));
		errorReportDTO.setPreviousBalanceYear(row.getField("PreviousBalanceYear"));
		errorReportDTO.setDeducteeTan(row.getField("DeducteeTAN"));
		errorReportDTO.setDeducteeTin(row.getField("DeducteeTIN"));
		errorReportDTO.setTdsSectionDescription(row.getField("TDSSectionDescription"));
		errorReportDTO.setUserDefinedField1(row.getField("UserDefinedField1"));
		errorReportDTO.setUserDefinedField2(row.getField("UserDefinedField2"));
		errorReportDTO.setUserDefinedField3(row.getField("UserDefinedField3"));
		errorReportDTO.setUserDefinedField4(row.getField("UserDefinedField4"));
		errorReportDTO.setUserDefinedField5(row.getField("UserDefinedField5"));
		errorReportDTO.setUserDefinedField6(row.getField("UserDefinedField6"));
		errorReportDTO.setUserDefinedField7(row.getField("UserDefinedField7"));
		errorReportDTO.setUserDefinedField8(row.getField("UserDefinedField8"));
		errorReportDTO.setUserDefinedField9(row.getField("UserDefinedField9"));
		errorReportDTO.setUserDefinedField10(row.getField("UserDefinedField10"));

		return errorReportDTO;
	}

	public BigDecimal isNull(BigDecimal isValue) {
		return isValue != null ? isValue : BigDecimal.ZERO;
	}

	public int isNull(Integer isValue) {
		return isValue != null ? isValue : 0;
	}

	/**
	 * 
	 * @param deducteeList
	 * @param deductorPan
	 * @param deducteeMap
	 * @param nopAndSectionMap
	 * @param nopIdMap
	 * @param thresholdMap
	 * @param batchUpdateApplicableTo
	 * @param deducteeBatchSave
	 * @param deducteeBatchUpdate
	 * @param thresholdLimitMaster
	 * @param deducteeNopBatchSave
	 * @param deducteeNopBatchUpdate
	 * @param deducteeResMap
	 * @param advanceBatchSave
	 * @param provisionBatchSave
	 * @return
	 * @throws Exception
	 */
	public int processResidentRecords(List<DeducteeMasterResidential> deducteeList, String deductorPan,
			Map<String, List<DeducteeMasterResidential>> deducteeMap, Map<String, Integer> nopAndSectionMap,
			Map<Integer, Double> nopIdMap, List<Map<String, Object>> thresholdMap,
			List<DeducteeMasterResidential> batchUpdateApplicableTo, List<DeducteeMasterResidential> deducteeBatchSave,
			List<DeducteeMasterResidential> deducteeBatchUpdate, List<ThresholdLimitGroupMaster> thresholdLimitMaster,
			List<DeducteeNopGroup> deducteeNopBatchSave, List<DeducteeNopGroup> deducteeNopBatchUpdate,
			Map<String, List<DeducteeMasterResidential>> deducteeResMap, List<AdvanceDTO> advanceBatchSave,
			List<ProvisionDTO> provisionBatchSave) throws Exception {
		logger.info("processResidentRecords menthod started for deductorPan: {} ", deductorPan);
		int duplicateCount = 0;
		// logic added for deductee multiple sections
		Integer nopId = null;
		if (nopAndSectionMap.get("194Q-Purchase of Goods") != null) {
			nopId = nopAndSectionMap.get("194Q-Purchase of Goods");
		}
		Double thersholdRate = 0.0;
		List<Integer> groupIdList = new ArrayList<>();
		if (nopId != null) {
			thersholdRate = nopIdMap.get(nopId);
			if (!thresholdMap.isEmpty()) {
				for (Map<String, Object> map : thresholdMap) {
					Integer natureOfPaymentId = (Integer) map.get("nopId");
					if (natureOfPaymentId.equals(nopId)) {
						groupIdList.add((Integer) map.get("groupId"));
					}
				}
			}
		}
		Map<String, List<DeducteeNopGroup>> deducteeNopMap = new HashMap<>();
		for (Entry<String, List<DeducteeMasterResidential>> entry : deducteeMap.entrySet()) {
			DeducteeMasterResidential deducteeData = entry.getValue().get(0);
			// retrieving existing deductee details with out considering special characters
			// in deductee name.

			List<DeducteeMasterResidential> deducteeDbDetails = deducteeResMap
					.get(deducteeData.getDeducteeKey()) != null ? deducteeResMap.get(deducteeData.getDeducteeKey())
							: new ArrayList<>();

			DeducteeMasterResidential deducteeDB = null;
			if (!deducteeDbDetails.isEmpty()) {
				for (DeducteeMasterResidential deductee : deducteeDbDetails) {
					if (deductee.getApplicableTo() == null || deductee.getApplicableTo().after(new Date())) {
						deducteeDB = deductee;
						break;
					}
				}
			}

			Map<String, Float> dbSectionsAndNop = new HashMap<>();
			Map<String, Float> newSections = new HashMap<>();
			Map<String, Float> dbSections = new HashMap<>();

			Map<String, String> dbSectionsAndCode = new HashMap<>();
			Map<String, Integer> dbSectionsAndThresholds = new HashMap<>();

			boolean isduplicateSections = false;
			// Validating sections with deductees in db.
			if (deducteeDB != null) {
				if (StringUtils.isNotBlank(deducteeDB.getSection())) {
					dbSectionsAndNop.put(deducteeDB.getSection() + "-" + deducteeDB.getNatureOfPayment(),
							(deducteeDB.getRate() != null ? deducteeDB.getRate().floatValue() : 0));
					dbSections.put(deducteeDB.getSection(),
							(deducteeDB.getRate() != null ? deducteeDB.getRate().floatValue() : 0));
					dbSectionsAndCode.put(deducteeDB.getSectionCode(), deducteeDB.getSection());
					Integer threshold = (deducteeDB.getIsThresholdLimitApplicable() != null
							&& deducteeDB.getIsThresholdLimitApplicable().equals(true)) ? 1 : 0;
					dbSectionsAndThresholds.put(deducteeDB.getSection(), threshold);
				}
				if (StringUtils.isNotBlank(deducteeDB.getAdditionalSections())) {
					Map<String, Float> dbSectionRateMap = objectMapper.readValue(deducteeDB.getAdditionalSections(),
							new TypeReference<Map<String, Float>>() {
							});
					for (Entry<String, Float> d : dbSectionRateMap.entrySet()) {
						dbSections.put(StringUtils.substringBefore(d.getKey(), "-"), d.getValue());
					}
					dbSectionsAndNop.putAll(dbSectionRateMap);
					if (StringUtils.isNotBlank(deducteeDB.getAdditionalSectionCode())) {
						Map<String, String> dbSectionAndCode = objectMapper.readValue(
								deducteeDB.getAdditionalSectionCode(), new TypeReference<Map<String, String>>() {
								});
						for (Entry<String, String> d : dbSectionAndCode.entrySet()) {
							dbSectionsAndCode.put(d.getKey(), d.getValue());
						}
						dbSectionsAndCode.putAll(dbSectionAndCode);
					}
					if (StringUtils.isNotBlank(deducteeDB.getAdditionalSectionThresholds())) {
						Map<String, Integer> dbSectionAndThreshold = objectMapper.readValue(
								deducteeDB.getAdditionalSectionThresholds(), new TypeReference<Map<String, Integer>>() {
								});
						for (Entry<String, Integer> d : dbSectionAndThreshold.entrySet()) {
							dbSectionsAndThresholds.put(d.getKey(), d.getValue());
						}
						dbSectionsAndThresholds.putAll(dbSectionAndThreshold);
					}
				}
				for (DeducteeMasterResidential d : entry.getValue()) {
					if (d.getSection() != null) {
						newSections.put(d.getSection(), (d.getRate() != null ? d.getRate().floatValue() : 0));
					}
				}
				isduplicateSections = dbSections.keySet().equals(newSections.keySet());
			}
			Map<String, Float> sectionsAndNop = new HashMap<>();
			Map<String, Float> sections = new HashMap<>();
			Map<String, String> sectionsAndCode = new HashMap<>();
			Map<String, Integer> sectionsAndThreshold = new HashMap<>();
			if (deducteeMap.size() != deducteeList.size() && entry.getValue().size() > 1) {
				deducteeList.removeAll(entry.getValue());
				for (DeducteeMasterResidential d : entry.getValue()) {
					if (StringUtils.isNotBlank(d.getDeducteePAN())
							&& StringUtils.isBlank(deducteeData.getDeducteePAN())) {
						deducteeData.setDeducteePAN(d.getDeducteePAN());
					}
					if (StringUtils.isBlank(deducteeData.getSection()) && StringUtils.isNotBlank(d.getSection())) {
						deducteeData.setSection(d.getSection());
						deducteeData.setNatureOfPayment(d.getNatureOfPayment());
						deducteeData.setIsDeducteeHasAdditionalSections(false);
						deducteeData
								.setSectionCode(d.getSectionCode() == null ? StringUtils.EMPTY : d.getSectionCode());
						deducteeData.setRate((d.getRate() != null ? d.getRate() : BigDecimal.ZERO));
						deducteeData.setIsThresholdLimitApplicable(
								d.getIsThresholdLimitApplicable() != null ? d.getIsThresholdLimitApplicable() : false);
					} else if (StringUtils.isNotBlank(deducteeData.getSection())
							&& StringUtils.isNotBlank(d.getSection())
							&& !deducteeData.getSection().equals(d.getSection())) {
						String sectionAndNop = d.getSection() + "-" + d.getNatureOfPayment();
						sectionsAndNop.put(sectionAndNop != null ? sectionAndNop : "",
								(d.getRate() != null ? d.getRate().floatValue() : 0));
						sections.put(d.getSection() != null ? d.getSection() : "",
								(d.getRate() != null ? d.getRate().floatValue() : 0));
						sectionsAndCode.put(d.getSectionCode() != null ? d.getSectionCode() : "",
								d.getSection() != null ? d.getSection() : "");
						Integer threshold = (d.getIsThresholdLimitApplicable() != null
								&& d.getIsThresholdLimitApplicable().equals(true)) ? 1 : 0;
						sectionsAndThreshold.put(d.getSection(), threshold);
					}
				}
				if (sectionsAndNop.size() > 0) {
					String additonalSection = objectMapper.writeValueAsString(sectionsAndNop);
					deducteeData.setAdditionalSections(additonalSection);
				}
				if (sectionsAndCode.size() > 0) {
					String additonalSectionCode = objectMapper.writeValueAsString(sectionsAndCode);
					deducteeData.setAdditionalSectionCode(additonalSectionCode);
				}
				if (sectionsAndThreshold.size() > 0) {
					String additonalSectionThreshold = objectMapper.writeValueAsString(sectionsAndThreshold);
					deducteeData.setAdditionalSectionThresholds(additonalSectionThreshold);
				}
				deducteeList.add(deducteeData);
			} // for 3
			if (!dbSectionsAndNop.isEmpty() && !isduplicateSections) {
				for (Entry<String, Float> dBsectionAndNop : dbSectionsAndNop.entrySet()) {
					String dBSection = StringUtils.substringBefore(dBsectionAndNop.getKey(), "-");
					String dBNop = StringUtils.substringAfter(dBsectionAndNop.getKey(), "-");
					if (StringUtils.isBlank(deducteeData.getSection())) {
						deducteeData.setSection(dBSection);
						deducteeData.setNatureOfPayment(dBNop);
						deducteeData.setIsDeducteeHasAdditionalSections(false);
					} else if (deducteeData.getAdditionalSections() != null) {
						if (!sections.containsKey(dBSection) && !deducteeData.getSection().equals(dBSection)) {
							sectionsAndNop.put(dBsectionAndNop.getKey(), dBsectionAndNop.getValue());
							String additionalSection = objectMapper.writeValueAsString(sectionsAndNop);
							deducteeData.setAdditionalSections(additionalSection);
						}
					} else if (!deducteeData.getSection().equals(dBSection)) {
						sectionsAndNop.put(dBsectionAndNop.getKey(), dBsectionAndNop.getValue());
						String additionalSection = objectMapper.writeValueAsString(sectionsAndNop);
						deducteeData.setAdditionalSections(additionalSection);
					}
				}
				if (!dbSectionsAndCode.isEmpty()) {
					for (Entry<String, String> dbSectionAndCode : dbSectionsAndCode.entrySet()) {
						String dBSectionCode = dbSectionAndCode.getKey();
						String dBSection = dbSectionAndCode.getValue();
						if (StringUtils.isBlank(deducteeData.getSection())) {
							deducteeData.setSectionCode(dBSectionCode);
						} else if (deducteeData.getAdditionalSectionCode() != null) {
							if (!sections.containsKey(dBSection) && !deducteeData.getSection().equals(dBSection)) {
								sectionsAndCode.put(dbSectionAndCode.getKey(), dbSectionAndCode.getValue());
								String additionalSectionCode = objectMapper.writeValueAsString(sectionsAndCode);
								deducteeData.setAdditionalSectionCode(additionalSectionCode);
							}
						} else if (!deducteeData.getSection().equals(dBSection)) {
							sectionsAndCode.put(dbSectionAndCode.getKey(), dbSectionAndCode.getValue());
							String additionalSectionCode = objectMapper.writeValueAsString(sectionsAndCode);
							deducteeData.setAdditionalSectionCode(additionalSectionCode);
						}
					}
				}
				if (!dbSectionsAndThresholds.isEmpty()) {
					for (Entry<String, Integer> dbSectionsAndThreshold : dbSectionsAndThresholds.entrySet()) {
						String dBSection = dbSectionsAndThreshold.getKey();
						Integer dBThreshold = dbSectionsAndThreshold.getValue();
						if (StringUtils.isBlank(deducteeData.getSection())) {
							Boolean threshold = (dBThreshold != null && dBThreshold == 1) ? true : false;
							deducteeData.setIsThresholdLimitApplicable(threshold);
						} else if (deducteeData.getAdditionalSectionThresholds() != null) {
							if (!sections.containsKey(dBSection) && !deducteeData.getSection().equals(dBSection)) {
								Integer threshold = (dbSectionsAndThreshold.getValue() != null
										&& dbSectionsAndThreshold.getValue() == 1) ? 1 : 0;
								sectionsAndThreshold.put(dbSectionsAndThreshold.getKey(), threshold);
								String additionalSectionThreshold = objectMapper
										.writeValueAsString(sectionsAndThreshold);
								deducteeData.setAdditionalSectionThresholds(additionalSectionThreshold);
							}
						} else if (!deducteeData.getSection().equals(dBSection)) {
							Integer threshold = (dbSectionsAndThreshold.getValue() != null
									&& dbSectionsAndThreshold.getValue() == 1) ? 1 : 0;
							sectionsAndThreshold.put(dbSectionsAndThreshold.getKey(), threshold);
							String additionalSectionThreshold = objectMapper.writeValueAsString(sectionsAndThreshold);
							deducteeData.setAdditionalSectionThresholds(additionalSectionThreshold);
						}
					}
				}
			}
			boolean isDuplicateRecord = false;
			// logic for if same deductee with same section and deductee key exists then
			// marking as
			// duplicate.
			if (deducteeDB != null && (dbSectionsAndNop.isEmpty() || isduplicateSections)
					&& StringUtils.isNotBlank(deducteeDB.getDeducteeKey())
					&& deducteeData.getDeducteeKey().equals(deducteeDB.getDeducteeKey())) {
				isDuplicateRecord = true;
			}
			if (!isDuplicateRecord) {
				if (deducteeDB != null) {
					deducteeDB.setActive(false);
					deducteeDB.setApplicableTo(subtractDay(new Date()));
					// batch update applicableTo date
					batchUpdateApplicableTo.add(deducteeDB);
				}
				if (sectionsAndNop.size() > 0) {
					deducteeData.setIsDeducteeHasAdditionalSections(true);
				}

				// get threshold ledger data based on deductee pan or deductee key
				List<DeducteeNopGroup> deducteeNopList = deducteeNopGroupDAO.findByDeducteeKeyOrDeducteePan(
						deducteeData.getDeducteeKey(), deductorPan, deducteeData.getDeducteePAN(),
						deducteeData.getCurrentBalanceYear());
				// added three amounts
				BigDecimal amount = deducteeData.getDeducteeMasterBalancesOf194q()
						.add(deducteeData.getAdvanceBalancesOf194q()).add(deducteeData.getProvisionBalancesOf194q());
				if (deducteeData.getCurrentBalanceYear() == null) {
					deducteeData.setCurrentBalanceYear(CommonUtil.getAssessmentYear(null));
				}
				if (!deducteeNopList.isEmpty()) {
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
					for (DeducteeNopGroup nop : deducteeNopList) {
						deducteeNopGroupUpdate(nop, amount, deducteeData.getCreatedBy(), deducteeData.getDeducteePAN(),
								deducteeNopBatchUpdate, groupIdList, thresholdLimitMaster);
					}
				} else {
					deducteeNopGroupSave(deducteeData, amount, deducteeData.getCurrentBalanceYear(),
							thresholdLimitMaster, deducteeNopBatchSave, groupIdList, deducteeNopMap, null);
				}
				// check for advance balances of 194Q amount
				BigDecimal advanceAmount = deducteeData.getAdvanceBalancesOf194q();
				// check greater then zero
				if (advanceAmount.compareTo(BigDecimal.ZERO) > 0) {
					// advance save
					advanceSave(deducteeData, nopId, thersholdRate, deducteeData.getCurrentBalanceMonth(),
							advanceAmount, deducteeData.getCurrentBalanceYear(), groupIdList, advanceBatchSave, null);
				}
				// check for advance as of march amount
				BigDecimal advanceAsOfMarchAmount = deducteeData.getAdvancesAsOfMarch();
				// check greater then zero
				if (advanceAsOfMarchAmount.compareTo(BigDecimal.ZERO) > 0) {
					// advance save
					advanceSave(deducteeData, nopId, thersholdRate, deducteeData.getPreviousBalanceMonth(),
							advanceAsOfMarchAmount, deducteeData.getPreviousBalanceYear(), groupIdList,
							advanceBatchSave, null);

				}
				// check for provision balances of 194Q amount
				BigDecimal provisionAmount = deducteeData.getProvisionBalancesOf194q();
				// check greater then zero
				if (provisionAmount.compareTo(BigDecimal.ZERO) > 0) {
					// provision save
					provisionSave(deducteeData, nopId, thersholdRate, deducteeData.getCurrentBalanceMonth(),
							provisionAmount, deducteeData.getCurrentBalanceYear(), groupIdList, provisionBatchSave,
							null);
				}
				// check for provision as of march amount
				BigDecimal provisionsAsOfMarchAmount = deducteeData.getProvisionsAsOfMarch();
				// check greater then zero
				if (provisionsAsOfMarchAmount.compareTo(BigDecimal.ZERO) > 0) {
					// provision save
					provisionSave(deducteeData, nopId, thersholdRate, deducteeData.getPreviousBalanceMonth(),
							provisionsAsOfMarchAmount, deducteeData.getPreviousBalanceYear(), groupIdList,
							provisionBatchSave, null);

				}
				// deductee batch save
				deducteeBatchSave.add(deducteeData);
			} else if (deducteeDB != null) {
				int deducteeId = deducteeDB.getDeducteeMasterId();
				boolean isSection = deducteeDB.getIsDeducteeHasAdditionalSections();
				BeanUtils.copyProperties(deducteeData, deducteeDB);
				deducteeDB.setDeducteeMasterId(deducteeId);
				deducteeDB.setIsDeducteeHasAdditionalSections(isSection);
				deducteeDB.setApplicableTo(deducteeData.getApplicableTo());
				deducteeDB.setModifiedBy(deducteeData.getCreatedBy());
				deducteeDB.setModifiedDate(new Timestamp(new Date().getTime()));
				deducteeBatchUpdate.add(deducteeDB);
			}
		}
		logger.info("processResidentRecords menthod ended for deductorPan: {} ", deductorPan);
		return duplicateCount;
	}

	/**
	 * 
	 * @param deducteeNop
	 * @param amount
	 * @param userName
	 * @param deducteePan
	 * @param deducteeNopBatchUpdate
	 * @param groupIdList
	 * @param thresholdLimitMaster
	 */
	public void deducteeNopGroupUpdate(DeducteeNopGroup deducteeNop, BigDecimal amount, String userName,
			String deducteePan, List<DeducteeNopGroup> deducteeNopBatchUpdate, List<Integer> groupIdList,
			List<ThresholdLimitGroupMaster> thresholdLimitMaster) {
		if (!thresholdLimitMaster.isEmpty() && !groupIdList.isEmpty()) {
			for (ThresholdLimitGroupMaster threshold : thresholdLimitMaster) {
				if (groupIdList.contains(threshold.getId().intValue())
						&& groupIdList.contains(deducteeNop.getGroupNopId())) {
					deducteeNop.setId(deducteeNop.getId());
					deducteeNop.setModifiedDate(new Timestamp(new Date().getTime()));
					deducteeNop.setModifiedBy(userName);
					deducteeNop.setLastUpdatedDate(new Timestamp(new Date().getTime()));
					deducteeNop.setThresholdReached(false);
					// if null user is editing the record with pan
					if (deducteeNop.getDeducteeKey() != null) {
						// update the record with pan
						deducteeNop.setDeducteePan(deducteePan);
						deducteeNop.setDeducteeKey(null);
					}
					BigDecimal amountUtilizedDb = deducteeNop.getAmountUtilized() == null ? BigDecimal.ZERO
							: deducteeNop.getAmountUtilized();
					BigDecimal finalAmount = amount.add(amountUtilizedDb);
					deducteeNop.setAmountUtilized(finalAmount);
					if (deducteeNop.getAmountUtilized() != null
							&& deducteeNop.getAmountUtilized().compareTo(threshold.getThresholdAmount()) >= 0) {
						deducteeNop.setThresholdReached(true);
					}
					deducteeNopBatchUpdate.add(deducteeNop);
					break;
				}
			}
		}
	}

	/**
	 * 
	 * @param deductorPan
	 * @param userName
	 * @param deducteeMasterResidential
	 * @param amount
	 * @param year
	 * @param deducteeNopGroup
	 * @param deducteeNopBatchSave
	 * @param groupIdList
	 * @param thresholdMap
	 */
	private void deducteeNopGroupSave(DeducteeMasterResidential deducteeMasterResidential, BigDecimal amount, int year,
			List<ThresholdLimitGroupMaster> thresholdLimitMaster, List<DeducteeNopGroup> deducteeNopBatchSave,
			List<Integer> groupIdList, Map<String, List<DeducteeNopGroup>> deducteeNopMap,
			DeducteeMasterNonResidential deducteeMasterNonResidential) {
		String deducteeKey = StringUtils.EMPTY;
		String deductorPan = StringUtils.EMPTY;
		String deducteePAN = StringUtils.EMPTY;
		String createdBy = StringUtils.EMPTY;
		if (deducteeMasterResidential != null) {
			deducteeKey = deducteeMasterResidential.getDeducteeKey();
			deductorPan = deducteeMasterResidential.getDeductorPan();
			deducteePAN = deducteeMasterResidential.getDeducteePAN();
			createdBy = deducteeMasterResidential.getCreatedBy();
		} else {
			deducteeKey = deducteeMasterNonResidential.getDeducteeKey();
			deductorPan = deducteeMasterNonResidential.getDeductorPan();
			deducteePAN = deducteeMasterNonResidential.getDeducteePAN();
			createdBy = deducteeMasterNonResidential.getCreatedBy();
		}
		if (!thresholdLimitMaster.isEmpty()) {
			List<DeducteeNopGroup> deducteeNopGroupList = new ArrayList<>();
			for (ThresholdLimitGroupMaster threshold : thresholdLimitMaster) {
				DeducteeNopGroup deducteeNopGroup = new DeducteeNopGroup();
				deducteeNopGroup.setActive(true);
				deducteeNopGroup.setAdvancePending(BigDecimal.ZERO);
				deducteeNopGroup.setAmountUtilized(BigDecimal.ZERO);
				deducteeNopGroup.setThresholdReached(false);
				deducteeNopGroup.setThresholdLimitAmount(threshold.getThresholdAmount());
				if (groupIdList.contains(threshold.getId().intValue())) {
					deducteeNopGroup.setAmountUtilized(amount);
					if (deducteeNopGroup.getAmountUtilized().compareTo(threshold.getThresholdAmount()) >= 0) {
						deducteeNopGroup.setThresholdReached(true);
					}
				}
				deducteeNopGroup.setCreatedBy(createdBy);
				deducteeNopGroup.setCreatedDate(new Timestamp(new Date().getTime()));
				if (StringUtils.isNotBlank(deducteePAN)) {
					deducteeNopGroup.setDeducteePan(deducteePAN);
				} else {
					deducteeNopGroup.setDeducteeKey(deducteeKey);
				}
				deducteeNopGroup.setDeductorPan(deductorPan);
				deducteeNopGroup.setGroupNopId(threshold.getId().intValue());
				deducteeNopGroup.setYear(year);
				deducteeNopGroupList.add(deducteeNopGroup);
			}
			if (StringUtils.isAllBlank(deducteePAN)) {
				deducteeNopBatchSave.addAll(deducteeNopGroupList);

			} else if (deducteeNopMap.get(deducteePAN) == null) {
				deducteeNopBatchSave.addAll(deducteeNopGroupList);
				deducteeNopMap.put(deducteePAN, new ArrayList<>());
				deducteeNopMap.get(deducteePAN).addAll(deducteeNopGroupList);

			} else {
				List<DeducteeNopGroup> deducteeNopList = deducteeNopMap.get(deducteePAN);
				for (DeducteeNopGroup deducteeNopGroup : deducteeNopList) {
					if (!groupIdList.isEmpty() && groupIdList.contains(deducteeNopGroup.getGroupNopId())) {
						BigDecimal amountUtilizedDb = deducteeNopGroup.getAmountUtilized() == null ? BigDecimal.ZERO
								: deducteeNopGroup.getAmountUtilized();
						deducteeNopGroup.setAmountUtilized(amount.add(amountUtilizedDb));
						break;
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param deducteeMaster
	 * @param nopId
	 * @param rate
	 * @param month
	 * @param provisionAmount
	 * @param assessmentYear
	 * @param groupIdList
	 * @param provisionBatchSave
	 */
	private void provisionSave(DeducteeMasterResidential deducteeMaster, Integer nopId, Double rate, int month,
			BigDecimal provisionAmount, int assessmentYear, List<Integer> groupIdList,
			List<ProvisionDTO> provisionBatchSave, DeducteeMasterNonResidential deducteeMasterNonResidential) {
		String residentialStatus = StringUtils.EMPTY;
		String deducteeKey = StringUtils.EMPTY;
		String deducteeCode = StringUtils.EMPTY;
		String deductorPan = StringUtils.EMPTY;
		String deducteeName = StringUtils.EMPTY;
		String deducteePAN = StringUtils.EMPTY;
		String deductorTan = StringUtils.EMPTY;
		String createdBy = StringUtils.EMPTY;
		Integer batchUploadId = 0;
		if (deducteeMaster != null) {
			residentialStatus = "N";
			deducteeKey = deducteeMaster.getDeducteeKey();
			deducteeCode = deducteeMaster.getDeducteeCode();
			deductorPan = deducteeMaster.getDeductorPan();
			deducteeName = deducteeMaster.getDeducteeName();
			deducteePAN = deducteeMaster.getDeducteePAN();
			deductorTan = deducteeMaster.getDeductorTan();
			createdBy = deducteeMaster.getCreatedBy();
			batchUploadId = deducteeMaster.getBatchUploadId();
		} else {
			residentialStatus = "Y";
			deducteeKey = deducteeMasterNonResidential.getDeducteeKey();
			deducteeCode = deducteeMasterNonResidential.getDeducteeCode();
			deductorPan = deducteeMasterNonResidential.getDeductorPan();
			deducteeName = deducteeMasterNonResidential.getDeducteeName();
			deducteePAN = deducteeMasterNonResidential.getDeducteePAN();
			deductorTan = deducteeMasterNonResidential.getDeductorTan();
			createdBy = deducteeMasterNonResidential.getCreatedBy();
			batchUploadId = deducteeMasterNonResidential.getBatchUploadId();
		}
		ProvisionDTO provisionDto = new ProvisionDTO();
		BigDecimal thersholdRate = rate != null ? BigDecimal.valueOf(rate) : BigDecimal.ZERO;
		provisionDto.setDeducteeCode(deducteeCode);
		provisionDto.setAssessmentYear(assessmentYear);
		provisionDto.setAssessmentMonth(month);
		provisionDto.setChallanMonth(month);
		provisionDto.setProvisionalAmount(provisionAmount);
		provisionDto.setDeducteeKey(deducteeKey);
		provisionDto.setDeductorMasterTan(deductorTan);
		provisionDto.setDeductorPan(deductorPan);
		provisionDto.setDeducteeName(deducteeName);
		provisionDto.setDocumentType("PRV");
		provisionDto.setIsResident(residentialStatus);
		provisionDto.setDeducteePan(deducteePAN);
		provisionDto.setSupplyType("TAX");
		LocalDate localDate = LocalDate.of(CommonUtil.getCurrentYearByAssessmentYear(assessmentYear, month), month, 1);
		LocalDate monthDate = localDate.plusDays(localDate.lengthOfMonth() - 1);
		int monthLastDay = monthDate.getDayOfMonth();
		logger.info("month last day: {}", monthLastDay);
		LocalDate localDate1 = LocalDate.of(CommonUtil.getCurrentYearByAssessmentYear(assessmentYear, month), month,
				monthLastDay);
		ZoneId defaultZoneId = ZoneId.systemDefault();
		provisionDto.setDocumentDate(Date.from((localDate1.atStartOfDay(defaultZoneId).toInstant())));
		provisionDto.setDocumentNumber(deducteeCode + deducteePAN);
		provisionDto.setLineItemNumber("1");
		provisionDto.setProvisionNpId(nopId);
		if (!groupIdList.isEmpty()) {
			provisionDto.setProvisionGroupid(groupIdList.get(0));
		} else {
			provisionDto.setProvisionGroupid(null);
		}
		provisionDto.setActive(true);
		provisionDto.setIsParent(false);
		provisionDto.setIsExempted(false);
		provisionDto.setChallanPaid(false);
		provisionDto.setApprovedForChallan(false);
		provisionDto.setIsChallanGenerated(false);
		provisionDto.setHasLdc(false);
		provisionDto.setIsError(false);
		provisionDto.setKeyDuplicate(false);
		provisionDto.setIsInitialRecord(true);
		provisionDto.setCreatedBy(createdBy);
		provisionDto.setCreatedDate(new Timestamp(new Date().getTime()));
		provisionDto.setPostingDateOfDocument(Date.from((localDate1.atStartOfDay(defaultZoneId).toInstant())));
		provisionDto.setMismatch(false);
		provisionDto.setUnderThreshold(false);

		// 9 columns
		provisionDto.setWithholdingSection("194Q");
		provisionDto.setDerivedTdsSection("194Q");
		provisionDto.setFinalTdsSection("194Q");
		provisionDto.setWithholdingRate(thersholdRate);
		provisionDto.setDerivedTdsRate(thersholdRate);
		provisionDto.setFinalTdsRate(thersholdRate);
		provisionDto.setWithholdingAmount(provisionAmount.multiply(thersholdRate).divide(new BigDecimal(100)));
		provisionDto.setDerivedTdsAmount(provisionAmount.multiply(thersholdRate).divide(new BigDecimal(100)));
		provisionDto.setFinalTdsAmount(provisionAmount.multiply(thersholdRate).divide(new BigDecimal(100)));
		provisionDto.setBatchUploadId(batchUploadId);
		provisionBatchSave.add(provisionDto);

	}

	/**
	 * 
	 * @param deducteeMasterResidential
	 * @param nopId
	 * @param rate
	 * @param month
	 * @param advanceAmount
	 * @param assessmentYear
	 * @param groupIdList
	 * @param advanceBatchSave
	 * @param deducteeMasterNonResidential
	 */
	private void advanceSave(DeducteeMasterResidential deducteeMasterResidential, Integer nopId, Double rate, int month,
			BigDecimal advanceAmount, int assessmentYear, List<Integer> groupIdList, List<AdvanceDTO> advanceBatchSave,
			DeducteeMasterNonResidential deducteeMasterNonResidential) {
		String residentialStatus = StringUtils.EMPTY;
		String deducteeKey = StringUtils.EMPTY;
		String deducteeCode = StringUtils.EMPTY;
		String deductorPan = StringUtils.EMPTY;
		String deducteeName = StringUtils.EMPTY;
		String deducteePAN = StringUtils.EMPTY;
		String deductorTan = StringUtils.EMPTY;
		String createdBy = StringUtils.EMPTY;
		Integer batchUploadId = 0;
		if (deducteeMasterResidential != null) {
			residentialStatus = "N";
			deducteeKey = deducteeMasterResidential.getDeducteeKey();
			deducteeCode = deducteeMasterResidential.getDeducteeCode();
			deductorPan = deducteeMasterResidential.getDeductorPan();
			deducteeName = deducteeMasterResidential.getDeducteeName();
			deducteePAN = deducteeMasterResidential.getDeducteePAN();
			deductorTan = deducteeMasterResidential.getDeductorTan();
			createdBy = deducteeMasterResidential.getCreatedBy();
			batchUploadId = deducteeMasterResidential.getBatchUploadId();

		} else {
			residentialStatus = "Y";
			deducteeKey = deducteeMasterNonResidential.getDeducteeKey();
			deducteeCode = deducteeMasterNonResidential.getDeducteeCode();
			deductorPan = deducteeMasterNonResidential.getDeductorPan();
			deducteeName = deducteeMasterNonResidential.getDeducteeName();
			deducteePAN = deducteeMasterNonResidential.getDeducteePAN();
			deductorTan = deducteeMasterNonResidential.getDeductorTan();
			createdBy = deducteeMasterNonResidential.getCreatedBy();
			batchUploadId = deducteeMasterNonResidential.getBatchUploadId();
		}
		AdvanceDTO advanceDTO = new AdvanceDTO();
		BigDecimal thersholdRate = rate != null ? BigDecimal.valueOf(rate) : BigDecimal.ZERO;
		advanceDTO.setAssessmentYear(assessmentYear);
		advanceDTO.setChallanMonth(month);
		advanceDTO.setAssessmentMonth(month);
		advanceDTO.setAmount(advanceAmount);
		advanceDTO.setDeducteeKey(deducteeKey);
		advanceDTO.setDeducteeCode(deducteeCode);
		advanceDTO.setDeductorMasterTan(deductorTan);
		advanceDTO.setDeductorPan(deductorPan);
		advanceDTO.setDeducteeName(deducteeName);
		advanceDTO.setDocumentType("ADV");
		advanceDTO.setIsResident(residentialStatus);
		advanceDTO.setDeducteePan(deducteePAN);
		advanceDTO.setSupplyType("TAX");
		LocalDate localDate = LocalDate.of(CommonUtil.getCurrentYearByAssessmentYear(assessmentYear, month), month, 1);
		LocalDate monthDate = localDate.plusDays(localDate.lengthOfMonth() - 1);
		int monthLastDay = monthDate.getDayOfMonth();
		logger.info("month last day: {}", monthLastDay);
		LocalDate localDate1 = LocalDate.of(CommonUtil.getCurrentYearByAssessmentYear(assessmentYear, month), month,
				monthLastDay);
		ZoneId defaultZoneId = ZoneId.systemDefault();
		advanceDTO.setDocumentDate(Date.from((localDate1.atStartOfDay(defaultZoneId).toInstant())));
		advanceDTO.setDocumentNumber(deducteeCode + deducteePAN);
		advanceDTO.setLineItemNumber("1");
		if (!groupIdList.isEmpty()) {
			advanceDTO.setAdvanceGroupid(groupIdList.get(0));
		} else {
			advanceDTO.setAdvanceGroupid(null);
		}
		advanceDTO.setAdvanceNpId(nopId);
		advanceDTO.setActive(true);
		advanceDTO.setIsParent(false);
		advanceDTO.setIsExempted(false);
		advanceDTO.setChallanPaid(false);
		advanceDTO.setApprovedForChallan(false);
		advanceDTO.setIsChallanGenerated(false);
		advanceDTO.setHasLdc(false);
		advanceDTO.setIsError(false);
		advanceDTO.setKeyDuplicate(false);
		advanceDTO.setIsInitialRecord(true);
		advanceDTO.setCreatedBy(createdBy);
		advanceDTO.setCreatedDate(new Timestamp(new Date().getTime()));
		advanceDTO.setPostingDateOfDocument(Date.from((localDate1.atStartOfDay(defaultZoneId).toInstant())));
		advanceDTO.setMismatch(false);
		advanceDTO.setUnderThreshold(false);
		// 9 columns
		advanceDTO.setWithholdingSection("194Q");
		advanceDTO.setDerivedTdsSection("194Q");
		advanceDTO.setFinalTdsSection("194Q");
		advanceDTO.setWithholdingRate(thersholdRate);
		advanceDTO.setDerivedTdsRate(thersholdRate);
		advanceDTO.setFinalTdsRate(thersholdRate);
		advanceDTO.setWithholdingAmount(advanceAmount.multiply(thersholdRate).divide(new BigDecimal(100)));
		advanceDTO.setDerivedTdsAmount(advanceAmount.multiply(thersholdRate).divide(new BigDecimal(100)));
		advanceDTO.setFinalTdsAmount(advanceAmount.multiply(thersholdRate).divide(new BigDecimal(100)));
		advanceDTO.setBatchUploadId(batchUploadId);
		advanceBatchSave.add(advanceDTO);

	}

	/**
	 * 
	 * @param originalFileName
	 * @param deductorTan
	 * @param deductorPan
	 * @param errorList
	 * @return
	 * @throws Exception
	 */
	public File prepareResidentDeducteesErrorFile(String originalFileName, String deductorTan, String deductorPan,
			ArrayList<DeducteeMasterResidentialErrorReportCsvDTO> errorList) throws Exception {
		try {
			ArrayList<String> headers = (ArrayList<String>) errorReportService.getResDeducteeHeaderFields();
			headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = deducteeResidentXlsxReport(errorList, deductorTan, deductorPan, headers);
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

	public static Date subtractDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return cal.getTime();
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
	 * @throws ParseException
	 */
	public BatchUpload deducteeBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
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
		batchUpload.setUploadType(UploadTypes.DEDUCTEE_EXCEL.name());
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		if (batchUpload.getBatchUploadID() != null) {
			batchUpload.setBatchUploadID(batchUpload.getBatchUploadID());
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
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

	public Workbook deducteeResidentXlsxReport(List<DeducteeMasterResidentialErrorReportCsvDTO> errorDTOs,
			String deductorTan, String deductorPan, ArrayList<String> headerNames) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForDeducteeResident(errorDTOs, worksheet, deductorTan, headerNames);

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

		// Style for D6 to AU6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:BI6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		List<DeductorMaster> response = deductorMasterDAO.findByDeductorPan(deductorPan);

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
		String lastHeaderCellName = "BI6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:BI6");
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
	public void setExtractDataForDeducteeResident(List<DeducteeMasterResidentialErrorReportCsvDTO> errorDTOs,
			Worksheet worksheet, String deductorTan, List<String> headerNames) throws Exception {

		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			for (int i = 0; i < errorDTOs.size(); i++) {
				DeducteeMasterResidentialErrorReportCsvDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = new ArrayList<>();
				rowData.add(StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.add(StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.add(errorDTO.getSerialNumber());
				rowData.add(errorDTO.getSourceIdentifier());
				rowData.add(errorDTO.getSourceFileName());
				rowData.add(errorDTO.getDeductorCode());
				rowData.add(errorDTO.getDeductorName());
				rowData.add(errorDTO.getDeductorPan());
				rowData.add(errorDTO.getDeductorTan());
				rowData.add(errorDTO.getDeducteeCode());
				rowData.add(errorDTO.getDeducteeName());
				rowData.add(errorDTO.getDeducteePAN());
				rowData.add(errorDTO.getDeducteeAadharNumber());
				rowData.add(errorDTO.getDeducteeStatus());
				rowData.add(errorDTO.getDeducteeResidentialStatus());
				rowData.add(errorDTO.getEmailAddress());
				rowData.add(errorDTO.getPhoneNumber());
				rowData.add(errorDTO.getFlatDoorBlockNo());
				rowData.add(errorDTO.getNameBuildingVillage());
				rowData.add(errorDTO.getRoadStreetPostoffice());
				rowData.add(errorDTO.getAreaLocality());
				rowData.add(errorDTO.getTownCityDistrict());
				rowData.add(errorDTO.getState());
				rowData.add(errorDTO.getCountry());
				rowData.add(errorDTO.getPinCode());
				rowData.add(errorDTO.getSectionCode());
				rowData.add(errorDTO.getSection());
				rowData.add(errorDTO.getNatureOfPayment());
				rowData.add(errorDTO.getRate());
				rowData.add(errorDTO.getTdsExcemptionFlag());
				rowData.add(errorDTO.getTdsExcemptionReason());
				rowData.add(errorDTO.getIsThresholdLimitApplicable());
				rowData.add(errorDTO.getApplicableFrom());
				rowData.add(errorDTO.getApplicableTo());
				rowData.add(errorDTO.getTdsExemptionNumber());
				rowData.add(errorDTO.getDeducteeGSTIN());
				rowData.add(errorDTO.getGrOrIRIndicator());
				rowData.add(errorDTO.getTdsApplicabilityUnderSection());
				rowData.add(errorDTO.getDeducteeMasterBalancesOf194q());
				rowData.add(errorDTO.getAdvanceBalancesOf194q());
				rowData.add(errorDTO.getProvisionBalancesOf194q());
				rowData.add(errorDTO.getOpeningBalanceCreditNote());
				rowData.add(errorDTO.getCurrentBalanceMonth());
				rowData.add(errorDTO.getCurrentBalanceYear());
				rowData.add(errorDTO.getAdvancesAsOfMarch());
				rowData.add(errorDTO.getProvisionsAsOfMarch());
				rowData.add(errorDTO.getPreviousBalanceMonth());
				rowData.add(errorDTO.getPreviousBalanceYear());
				rowData.add(errorDTO.getDeducteeTan());
				rowData.add(errorDTO.getDeducteeTin());
				rowData.add(errorDTO.getTdsSectionDescription());
				rowData.add(errorDTO.getUserDefinedField1());
				rowData.add(errorDTO.getUserDefinedField2());
				rowData.add(errorDTO.getUserDefinedField3());
				rowData.add(errorDTO.getUserDefinedField4());
				rowData.add(errorDTO.getUserDefinedField5());
				rowData.add(errorDTO.getUserDefinedField6());
				rowData.add(errorDTO.getUserDefinedField7());
				rowData.add(errorDTO.getUserDefinedField8());
				rowData.add(errorDTO.getUserDefinedField9());
				rowData.add(errorDTO.getUserDefinedField10());

				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				try {
					worksheet.autoFitColumn(i);
				} catch (Exception e) {
					// kill the exception
				}
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
			}
		}
	}

	/**
	 * 
	 * @param deducteeNonResidencePans
	 * @param deducteeResidencePans
	 * @return
	 * @throws Exception
	 */
	public MultipartFile generateDeducteePanXlsxReport(List<DeducteeMasterNonResidential> deducteeNonResidencePans,
			List<DeducteeMasterResidential> deducteeResidencePans) throws Exception {

		Map<String, DeducteeMasterResidential> deducteeMasterMap = new HashMap<>();
		for (DeducteeMasterResidential deducteeResidencePan : deducteeResidencePans) {
			deducteeMasterMap.put(deducteeResidencePan.getDeducteePAN(), deducteeResidencePan);
		}

		Map<String, DeducteeMasterNonResidential> deducteeNonResidentMap = new HashMap<>();
		for (DeducteeMasterNonResidential deducteeNonResidence : deducteeNonResidencePans) {
			deducteeNonResidentMap.put(deducteeNonResidence.getDeducteePAN(), deducteeNonResidence);
		}
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		String[] headerNames = new String[] { "PAN", "Name" };
		worksheet.getCells().importArray(headerNames, 0, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (!deducteeNonResidencePans.isEmpty()) {
			int rowIndex = 1;
			for (Map.Entry<String, DeducteeMasterNonResidential> entry : deducteeNonResidentMap.entrySet()) {
				DeducteeMasterNonResidential deducteeNonResidencePan = entry.getValue();
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add(StringUtils.isBlank(deducteeNonResidencePan.getDeducteePAN()) ? StringUtils.EMPTY
						: deducteeNonResidencePan.getDeducteePAN());
				rowData.add(StringUtils.isBlank(deducteeNonResidencePan.getDeducteeName()) ? StringUtils.EMPTY
						: deducteeNonResidencePan.getDeducteeName());
				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		} else if (!deducteeResidencePans.isEmpty()) {
			int rowIndex = 1;
			for (Map.Entry<String, DeducteeMasterResidential> entry : deducteeMasterMap.entrySet()) {
				DeducteeMasterResidential deducteeResidencePan = entry.getValue();
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add(StringUtils.isBlank(deducteeResidencePan.getDeducteePAN()) ? StringUtils.EMPTY
						: deducteeResidencePan.getDeducteePAN());
				rowData.add(StringUtils.isBlank(deducteeResidencePan.getDeducteeName()) ? StringUtils.EMPTY
						: deducteeResidencePan.getDeducteeName());
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
	 * @param filePath
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param batchUpload
	 * @return
	 * @throws Exception
	 */
	@Async
	public BatchUpload processNonResidentDeductees(String filePath, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonthPlusOne, String userName, String tenantId, String deductorPan,
			BatchUpload batchUpload) throws Exception {

		MultiTenantContext.setTenantId(tenantId);
		ArrayList<DeducteeMasterNonResidentialErrorReportCsvDTO> errorList = new ArrayList<>();
		File deducteeErrorsFile = null;
		try {
			File csvFile = blobStorageService.getFileFromBlobUrl(tenantId, filePath);

			CsvReader csvReader = new CsvReader();
			csvReader.setContainsHeader(true);
			CsvContainer csv = csvReader.read(csvFile, StandardCharsets.UTF_8);

			List<DeducteeMasterNonResidential> batchSave = new ArrayList<>();
			List<DeducteeMasterNonResidential> batchUpdate = new ArrayList<>();
			List<DeducteeMasterNonResidential> batchUpdateApplicableTo = new ArrayList<>();

			Long errorCount = 0L;
			List<DeducteeMasterNonResidential> deducteeList = new ArrayList<>();
			Set<String> dedcuteesSet = new HashSet<>();
			Map<String, List<DeducteeMasterNonResidential>> deducteeMap = new HashMap<>();
			Map<String, List<Map<String, Object>>> natureOfPaymentMap = new HashMap<>();
			Map<String, List<Map<String, Object>>> sectionMap = new HashMap<>();
			Map<String, Integer> nopAndSectionMap = new HashMap<>();
			Map<Integer, Double> nopIdMap = new HashMap<>();
			// deductee nop batch save.
			List<DeducteeNopGroup> deducteeNopBatchSave = new ArrayList<>();
			// deductee nop batch update.
			List<DeducteeNopGroup> deducteeNopBatchUpdate = new ArrayList<>();
			Map<String, String> deducteeStatusMap = mastersClient.getAllDeducteeStatus().getBody();
			// Nature of payment data
			List<Map<String, Object>> nopMap = mastersClient.getNOPAndSectionsResidentialStatus("NR").getBody()
					.getData();
			// Threshold limit group data
			List<ThresholdLimitGroupMaster> thresholdLimitMaster = mastersClient.getThresholdGroupByIds().getBody()
					.getData();
			List<DeductorMaster> deductorMaster = deductorMasterDAO.findByDeductorPan(deductorPan);
			String deductorCode = StringUtils.EMPTY;
			String deductorName = StringUtils.EMPTY;
			if (!deductorMaster.isEmpty()) {
				deductorCode = deductorMaster.get(0).getCode();
				deductorName = deductorMaster.get(0).getName();
			}
			for (Map<String, Object> map : nopMap) {
				String nature = (String) map.get("nature");
				String section = (String) map.get("section");
				Integer nopId = (Integer) map.get("natureOfPaymentId");
				Double rate = (Double) map.get("rate");
				String key = section + "-" + nature;
				if (!natureOfPaymentMap.containsKey(key)) {
					natureOfPaymentMap.put(key, new ArrayList<>());
				}
				natureOfPaymentMap.get(key).add(map);
				if (!sectionMap.containsKey(section)) {
					sectionMap.put(section, new ArrayList<>());
				}
				sectionMap.get(section).add(map);
				nopAndSectionMap.put(key, nopId);
				nopIdMap.put(nopId, rate);
			}
			Map<String, List<DeducteeMasterNonResidential>> deducteeNRMap = getDeducteeNonResidentRecords(deductorPan);
			int serialNumber = 1;
			int totalRecords = 0;
			for (CsvRow row : csv.getRows()) {
				if (StringUtils.isNotBlank(StringUtils.join(row.getFields(), ", ").replace(",", ""))) {
					++totalRecords;
					boolean isResStatusValid = true;
					DeducteeMasterNonResidentialErrorReportCsvDTO nonResidentErrorDTO = new DeducteeMasterNonResidentialErrorReportCsvDTO();
					// setting empty value to reason
					if (StringUtils.isBlank(nonResidentErrorDTO.getReason())) {
						nonResidentErrorDTO.setReason("");
					}
					DeducteeMasterNonResidential deductee = getNRDeducteeCsvMapping(row, nonResidentErrorDTO);
					deductee.setActive(true);
					deductee.setIsDeducteeHasAdditionalSections(false);
					deductee.setCreatedBy(userName);
					deductee.setCreatedDate(new Timestamp(new Date().getTime()));
					deductee.setInvoiceTransactionCount(0);
					deductee.setProvisionTransactionCount(0);
					deductee.setAdvanceTransactionCount(0);
					deductee.setPanStatus("");
					deductee.setBatchUploadId(batchUpload.getBatchUploadID());
					deductee.setDeductorTan(deductorTan);

					deductee.setDeducteeCode(
							StringUtils.isNotBlank(deductee.getDeducteeCode()) ? deductee.getDeducteeCode().trim()
									: "");
					deductee.setPhoneNumber(deductee.getPhoneNumber() != null ? deductee.getPhoneNumber().trim() : "");
					deductee.setDeductorCode(deductorCode);
					deductee.setDeductorName(deductorName);
					if (deductee.getApplicableFrom() == null) {
						deductee.setApplicableFrom(new Date());
					}
					if (StringUtils.isNotBlank(deductee.getDeducteeName())) {
						deductee.setDeducteeName(deductee.getDeducteeName().trim());
					}
					if (StringUtils.isNotBlank(deductee.getNatureOfPayment())) {
						deductee.setNatureOfPayment(deductee.getNatureOfPayment().trim());
					}
					// dedcutee residential status check
					if (StringUtils.isBlank(deductee.getDeducteeResidentialStatus())) {
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Deductee Residential Status"
								+ deductee.getDeducteeResidentialStatus() + " is mandatory(NR (or) NONRESIDENT)."
								+ "\n");
					} else if (StringUtils.isNotBlank(deductee.getDeducteeResidentialStatus())
							&& !"NR".equalsIgnoreCase(deductee.getDeducteeResidentialStatus())
							&& !"NONRESIDENT".equalsIgnoreCase(deductee.getDeducteeResidentialStatus())) {
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Deductee Residential Status "
								+ deductee.getDeducteeResidentialStatus() + " is not valid (NR (or) NONRESIDENT)."
								+ "\n");
					} else {
						deductee.setDeducteeResidentialStatus("NR");
					}
					// Check for Deductor Pan
					if (StringUtils.isBlank(deductee.getDeductorPan())) {
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Deductor Pan "
								+ deductee.getDeductorPan() + " is mandatory. " + "\n");
					} else if (!deductorPan.equalsIgnoreCase(deductee.getDeductorPan())) {
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Deductor Pan " + deductorPan
								+ " is not match." + "\n");
					}
					// Check for Deductee Aadhar Number Validation
					if (StringUtils.isNotBlank(deductee.getDeducteeAadharNumber())
							&& !deductee.getDeducteeAadharNumber().trim().matches("[0-9]{12}")) {
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Deductee Aadhar Number "
								+ deductee.getDeducteeAadharNumber() + " is not valid, enter 12 digit number." + "\n");
						isResStatusValid = false;
					}
					// Check for Deductee Code validation
					if (StringUtils.isNotBlank(deductee.getDeducteeCode())
							&& !deductee.getDeducteeCode().trim().matches("[0-9A-Za-z- ]{1,15}")) {
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Deductee code "
								+ deductee.getDeducteeCode() + " is not valid, "
								+ "allowed only Alpha numeric, hypen and Space, max length is 15 digits" + "\n");
						isResStatusValid = false;
					}
					// Check for deductee name validation
					if (StringUtils.isBlank(deductee.getDeducteeName())) {
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Deductee name "
								+ deductee.getDeducteeName() + " is mandatory. " + "\n");
						isResStatusValid = false;
					} else if (StringUtils.isNotBlank(deductee.getDeducteeName())
							&& !deductee.getDeducteeName().trim().matches("^[.\\p{Alnum}\\p{Space}&'-_!@#$()*,)(]{0,1024}$")) {
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Deductee name "
								+ deductee.getDeducteeName() + " is not valid. " + "\n");
						isResStatusValid = false;
					}
					// check for deductee TIN
					if (StringUtils.isNotBlank(deductee.getDeducteeTin())
							&& !deductee.getDeducteeTin().trim().matches("[0-9A-Za-z]{11}")) {
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Deductee TIN "
								+ deductee.getDeducteeTin() + " is not valid, enter 11 digit number." + "\n");
					} else {
						deductee.setDeducteeTin(deductee.getDeducteeTin());
					}

					// check for deductee GSTIN
					if (StringUtils.isNotBlank(deductee.getDeducteeGSTIN()) && !deductee.getDeducteeGSTIN().trim()
							.matches("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")) {
						nonResidentErrorDTO.setReason(
								nonResidentErrorDTO.getReason() + "Deductee GSTIN " + deductee.getDeducteeGSTIN()
										+ " is not valid, enter 15 digit number (Ex:06AAAAA6385P6ZA)." + "\n");
					}

					if (StringUtils.isBlank(deductee.getApplicableFrom().toString())) {
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Applicable From"
								+ deductee.getApplicableFrom() + " is mandatory. " + "\n");
					}

					String deducteeName = deductee.getDeducteeName().trim().toLowerCase();
					deducteeName = deducteeName.replaceAll("[^a-z0-9 ]", "");
					deductee.setModifiedName(deducteeName);
					// deducteee key
					if (StringUtils.isNotBlank(deductee.getDeducteeCode())) {
						deductee.setDeducteeKey(deductee.getDeducteeCode());
					} else {
						String name = deductee.getDeducteeName().toLowerCase();
						name = name.replaceAll("[^a-z0-9 ]", "").replace(" ", "-");
						if (StringUtils.isNotBlank(deductee.getDeducteePAN())) {
							deductee.setDeducteeKey(name.concat(deductee.getDeducteePAN()));
						} else {
							deductee.setDeducteeKey(name);
						}
					}
					// Deductee Enrichment Key
					if (StringUtils.isNotBlank(deductee.getDeducteeCode())) {
						deductee.setDeducteeEnrichmentKey(deductee.getDeducteeCode());
					} else {
						String name = deductee.getDeducteeName().toLowerCase();
						name = name.replaceAll("[^a-z0-9 ]", "").replace(" ", "-");
						deductee.setDeducteeEnrichmentKey(name);
					}
					// Is TRC Available is true
					if (deductee.getIsTRCAvailable()) {
						if (deductee.getTrcApplicableFrom() == null) {
							// error report
							nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
									+ "TRCApplicableFromDate Date is mandatory." + "\n");
						} else if (deductee.getTrcApplicableFrom() != null && deductee.getTrcApplicableTo() != null
								&& (deductee.getTrcApplicableFrom().equals(deductee.getTrcApplicableTo())
										|| deductee.getTrcApplicableFrom().after(deductee.getTrcApplicableTo()))) {
							// error report
							nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
									+ "TRCApplicableFromDate should not be equals or greater than TRCApplicableToDate Date"
									+ "/n");
						}
					}
					// IsTrcFuture is true
					if (deductee.getIstrcFuture() && deductee.getTrcFutureDate() == null) {
						nonResidentErrorDTO
								.setReason(nonResidentErrorDTO.getReason() + "TRC Future Date is mandatory." + "\n");
					}
					// Is TenF Available is true
					if (deductee.getIsTenFAvailable()) {
						if (deductee.getTenFApplicableFrom() == null) {
							// error report
							nonResidentErrorDTO.setReason(
									nonResidentErrorDTO.getReason() + "TENF Applicable From Date is mandatory." + "\n");
						} else if (deductee.getTenFApplicableFrom() != null && deductee.getTenFApplicableTo() != null
								&& (deductee.getTenFApplicableFrom().equals(deductee.getTenFApplicableTo())
										|| deductee.getTenFApplicableFrom().after(deductee.getTenFApplicableTo()))) {
							// error report
							nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
									+ "TENF Applicable From Date should not be equals or greater than TENF Applicable To Date"
									+ "\n");
						}
					}
					// Is Tenf Future is true
					if (deductee.getIstenfFuture() && deductee.getTenfFutureDate() == null) {
						// error report
						nonResidentErrorDTO
								.setReason(nonResidentErrorDTO.getReason() + "TENF Future Date is mandatory." + "\n");
					}
					// Is Weather PE In India is true
					if (deductee.getWhetherPEInIndia()) {
						if (deductee.getWhetherPEInIndiaApplicableFrom() == null) {
							// error report
							nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
									+ "Weather PE In India Applicable From Date is mandatory." + "\n");
							errorList.add(nonResidentErrorDTO);
						} else if (deductee.getWhetherPEInIndiaApplicableTo() != null && (deductee
								.getWhetherPEInIndiaApplicableFrom().equals(deductee.getWhetherPEInIndiaApplicableTo())
								|| deductee.getWhetherPEInIndiaApplicableFrom()
										.after(deductee.getWhetherPEInIndiaApplicableTo()))) {
							// error report
							nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
									+ "Weather PE India Applicable From Date should not be equals or greater than Weather PE India Applicable To Date"
									+ "\n");
						}
					}
					// No PE Document Available is true
					if (deductee.getNoPEDocumentAvailable()) {
						if (deductee.getNoPEApplicableFrom() == null) {
							// error report
							nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
									+ "NO PE Applicable From Date is mandatory." + "\n");
						} else if (deductee.getNoPEApplicableFrom() != null && deductee.getNoPEApplicableTo() != null
								&& (deductee.getNoPEApplicableFrom().equals(deductee.getNoPEApplicableTo())
										|| deductee.getNoPEApplicableFrom().after(deductee.getNoPEApplicableTo()))) {
							// error report
							nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
									+ "NoPEDeclarationApplicableFromDate should not be equals or greater than NoPEDeclarationApplicableToDate"
									+ "\n");
						}
					}
					// IsPOEMApplicable is true
					if (deductee.getIsPOEMavailable()) {
						if (deductee.getPoemApplicableFrom() == null) {
							// error report
							nonResidentErrorDTO.setReason(
									nonResidentErrorDTO.getReason() + "POEMApplicableFromDate is mandatory" + "\n");
						} else if (deductee.getPoemApplicableFrom() != null && deductee.getPoemApplicableTo() != null
								&& (deductee.getPoemApplicableFrom().equals(deductee.getPoemApplicableTo())
										|| deductee.getPoemApplicableFrom().after(deductee.getPoemApplicableTo()))) {
							// error report
							nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
									+ "POEMApplicableFromDate should not be equals or greater than POEMApplicableToDate"
									+ "\n");
						}
					}
					// IsNoPOEMApplicable is true
					if (deductee.getIsNoPOEMDeclarationAvailable()) {
						if (deductee.getNoPOEMDeclarationApplicableFromDate() == null) {
							// error report
							nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
									+ "NoPOEMDeclarationApplicableFromDate is mandatory" + "\n");
						} else if (deductee.getNoPOEMDeclarationApplicableFromDate() != null
								&& deductee.getNoPOEMDeclarationApplicableToDate() != null
								&& (deductee.getNoPOEMDeclarationApplicableFromDate()
										.equals(deductee.getNoPOEMDeclarationApplicableToDate())
										|| deductee.getNoPOEMDeclarationApplicableFromDate()
												.after(deductee.getNoPOEMDeclarationApplicableToDate()))) {
							// error report
							nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
									+ "NoPOEMDeclarationApplicableFromDate should not be equals or greater than NoPOEMDeclarationApplicableToDate"
									+ "\n");
						}
					}
					// NoPOEMDeclarationAvailableInFuture is true
					if (deductee.getIsPoemDeclaration() && deductee.getPoemFutureDate() == null) {
						// error report
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
								+ "NoPOEMDeclarationAvailableFutureDate is mandatory." + "\n");
					}
					// NoPEDeclarationAvailableFutureDate is true
					if (deductee.getNoPEDeclarationAvailableInFuture()
							&& deductee.getNoPEDeclarationAvailableFutureDate() == null) {
						// error report
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
								+ "NoPEDeclarationAvailableFutureDate is mandatory." + "\n");
					}
					
					if (deductee.getIsFixedbaseAvailbleIndia()
							&& deductee.getFixedbaseAvailbleIndiaApplicableFrom() == null) {
						// error report
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
								+ "Fixed Base Available In India Applicable From Date is mandatory" + "\n");
					} else if (deductee.getFixedbaseAvailbleIndiaApplicableFrom() != null
							&& deductee.getFixedbaseAvailbleIndiaApplicableTo() != null
							&& (deductee.getFixedbaseAvailbleIndiaApplicableFrom()
									.equals(deductee.getFixedbaseAvailbleIndiaApplicableTo())
									|| deductee.getFixedbaseAvailbleIndiaApplicableFrom()
											.after(deductee.getFixedbaseAvailbleIndiaApplicableTo()))) {
						// error report
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
								+ "Fixed Base Available In India Applicable From Date should not be equals or greater than Fixed Base Available In India Applicable To Date"
								+ "\n");
					}
					// IsNoFixedBaseDeclarationAvailable
					if (deductee.getIsNoFixedBaseDeclarationAvailable()
							&& deductee.getNoFixedBaseDeclarationFromDate() == null) {
						// error report
						nonResidentErrorDTO.setReason(
								nonResidentErrorDTO.getReason() + "NoFixedBaseDeclarationFromDate is mandatory" + "\n");
					} else if (deductee.getNoFixedBaseDeclarationFromDate() != null
							&& deductee.getNoFixedBaseDeclarationToDate() != null
							&& (deductee.getNoFixedBaseDeclarationFromDate()
									.equals(deductee.getNoFixedBaseDeclarationToDate())
									|| deductee.getNoFixedBaseDeclarationFromDate()
											.after(deductee.getNoFixedBaseDeclarationToDate()))) {
						// error report
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
								+ "NoFixedBaseDeclarationFromDate should not be equals or greater than NoFixedBaseDeclarationToDate"
								+ "\n");
					}
					// NoFixedBaseDeclarationAvailableInFuture is true
					if (deductee.getNoFixedBaseDeclarationAvailableInFuture()
							&& deductee.getNoFixedBaseDeclarationAvailableFutureDate() == null) {
						// error report
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
								+ "NoFixedBaseDeclarationAvailableFutureDate is mandatory." + "\n");
					}
					if (deductee.getApplicableFrom() != null && deductee.getApplicableTo() != null
							&& (deductee.getApplicableFrom().equals(deductee.getApplicableTo())
									|| deductee.getApplicableFrom().after(deductee.getApplicableTo()))) {
						// error report
						nonResidentErrorDTO.setReason(
								"Applicable From Date should not be equals or greater than Applicable To Date" + "\n");
					}

					if (StringUtils.isNotBlank(deductee.getDeducteePAN())) {
						deductee.setDeducteePAN(deductee.getDeducteePAN().toUpperCase());
						if (deductee.getDeducteePAN().trim().matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
							if (deducteeStatusMap.get(String.valueOf(deductee.getDeducteePAN().charAt(3))) != null) {
								deductee.setDeducteeStatus(
										deducteeStatusMap.get(String.valueOf(deductee.getDeducteePAN().charAt(3))));
							} else {
								nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Pan 4th character "
										+ deductee.getDeducteePAN().charAt(3) + " is Invalid." + "\n");
								isResStatusValid = false;
							}
						} else {
							nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Pan "
									+ deductee.getDeducteePAN() + " is not valid." + "\n");
							isResStatusValid = false;
						}
					} else if (StringUtils.isNotBlank(deductee.getDeducteeStatus())) {
						if ("Artificial Jurisdical Person".equalsIgnoreCase(deductee.getDeducteeStatus())) {
							deductee.setDeducteeStatus(deducteeStatusMap.get(String
									.valueOf(deductee.getDeducteeStatus().trim().substring(11, 12).toUpperCase())));
						} else if (deducteeStatusMap.get(String
								.valueOf(deductee.getDeducteeStatus().trim().substring(0, 1).toUpperCase())) != null) {
							deductee.setDeducteeStatus(deducteeStatusMap.get(
									String.valueOf(deductee.getDeducteeStatus().trim().substring(0, 1).toUpperCase())));
						}
						if (StringUtils.isBlank(deductee.getDeducteeStatus())) {
							nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Deductee Status "
									+ deductee.getDeducteeStatus() + " is not valid." + "\n");
							isResStatusValid = false;
						}
					} else if (StringUtils.isBlank(deductee.getDeducteePAN())
							&& StringUtils.isBlank(deductee.getDeducteeStatus())) {
						nonResidentErrorDTO
								.setReason(nonResidentErrorDTO.getReason() + "Deductee Status is mandatory" + "\n");
						isResStatusValid = false;
					}
					if (isResStatusValid && StringUtils.isNotBlank(deductee.getSection())) {
						if (sectionMap.get(deductee.getSection()) == null) {
							nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Section "
									+ deductee.getSection() + " not found in system." + "\n");
						} else if (StringUtils.isBlank(deductee.getNatureOfPayment())) {
							Map<Double, String> rateMap = new HashMap<>();
							List<Double> rates = new ArrayList<>();
							Double highestRate = 0.0;
							boolean isSectionValid = false;
							if (sectionMap.get(deductee.getSection()) != null) {
								for (Map<String, Object> nopData : sectionMap.get(deductee.getSection())) {
									String nature = (String) nopData.get("nature");
									SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
									String rateApplicableFrom = (String) nopData.get("applicableFrom");
									String rateApplicableTo = (String) nopData.get("applicableTo");
									Date applicableFrom = formatter.parse(rateApplicableFrom);
									Date applicableTo = null;
									if (StringUtils.isNotBlank(rateApplicableTo)) {
										applicableTo = formatter.parse(rateApplicableTo);
									}
									Double rate = (Double) nopData.get("rate");
									if (deductee.getApplicableFrom().getTime() >= applicableFrom.getTime()
											&& (applicableTo == null || deductee.getApplicableTo() == null || deductee
													.getApplicableTo().getTime() <= applicableTo.getTime())) {
										rate = rate != null ? rate : 0.0;
										rateMap.put(rate, nature);
										rates.add(rate);
										isSectionValid = true;
									}
								}
							}
							if (!rates.isEmpty()) {
								// section contains mutiple NOP then get the NOP based on highest rate
								highestRate = Collections.max(rates);
							}
							if (!isSectionValid) {
								nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
										+ "Section is invalid for the mentioned applicable period.");
							} else if (deductee.getRate() != null && deductee.getRate().doubleValue() > 0.0) {
								// section contains mutiple NOP then get the NOP based on closest rate passed in
								// the excel
								Optional<Double> rate = rates.parallelStream().min(Comparator
										.comparingDouble(i -> Math.abs(i - (deductee.getRate().doubleValue()))));
								deductee.setNatureOfPayment(rateMap.get(rate.isPresent() ? rate.get() : 0.0));
							} else {
								deductee.setNatureOfPayment(rateMap.get(highestRate));
							}
						} else if (StringUtils.isNotBlank(deductee.getNatureOfPayment())) {
							String key = deductee.getSection() + "-" + deductee.getNatureOfPayment();
							boolean isNopValid = false;
							if (natureOfPaymentMap.get(key) != null) {
								for (Map<String, Object> nopData : natureOfPaymentMap.get(key)) {
									SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
									String rateApplicableFrom = (String) nopData.get("applicableFrom");
									String rateApplicableTo = (String) nopData.get("applicableTo");
									Date applicableFrom = formatter.parse(rateApplicableFrom);
									Date applicableTo = null;
									if (StringUtils.isNotBlank(rateApplicableTo)) {
										applicableTo = formatter.parse(rateApplicableTo);
									}
									if (deductee.getApplicableFrom().getTime() >= applicableFrom.getTime()
											&& (applicableTo == null || deductee.getApplicableTo() == null || deductee
													.getApplicableTo().getTime() <= applicableTo.getTime())) {
										isNopValid = true;
										break;
									}
								}
							}
							if (!isNopValid) {
								nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
										+ "Section is invalid for the mentioned applicable period.");
							}
						}
					}
					if (StringUtils.isNotEmpty(deductee.getNatureOfPayment())
							&& StringUtils.isEmpty(deductee.getSection())) {
						// error report
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason()
								+ "Nature Of Payment is allowed if there is a section." + "\n");
					}
					validate194QFields(deductee, nonResidentErrorDTO);
					String key = deductee.getDeducteeKey() + "-" + deductee.getSection() + "-"
							+ deductee.getDeductorPan();
					if (StringUtils.isBlank(nonResidentErrorDTO.getReason()) && dedcuteesSet.contains(key)) {
						nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Duplicate." + "\n");
					}
					if (StringUtils.isBlank(nonResidentErrorDTO.getReason())) {
						deducteeList.add(deductee);
						dedcuteesSet.add(key);
						String deducteeStr = deductee.getDeducteeKey();
						if (!deducteeMap.containsKey(deducteeStr)) {
							deducteeMap.put(deducteeStr, new ArrayList<>());
						}
						deducteeMap.get(deducteeStr).add(deductee);
					}
					if (StringUtils.isNotBlank(nonResidentErrorDTO.getReason())) {
						++errorCount;
						nonResidentErrorDTO.setSerialNumber(String.valueOf(serialNumber));
						nonResidentErrorDTO = getNrDeducteeErrorCsvMapping(row, nonResidentErrorDTO);
						errorList.add(nonResidentErrorDTO);
					}
				}
				serialNumber++;
			}
			if (!deducteeList.isEmpty()) {
				processNonResidentRecords(deducteeList, deducteeMap, deducteeNRMap, batchUpdateApplicableTo, batchSave,
						batchUpdate, thresholdLimitMaster, deducteeNopBatchSave, deducteeNopBatchUpdate);
			}

			// batch save
			if (!batchSave.isEmpty()) {
				deducteeMasterNonResidentialDAO.nrBatchSave(batchSave);
			}
			// batch update
			if (!batchUpdate.isEmpty()) {
				deducteeMasterNonResidentialDAO.nrBatchUpdate(batchUpdate);
			}
			// batch update applicableTo
			if (!batchUpdateApplicableTo.isEmpty()) {
				deducteeMasterNonResidentialDAO.nrBatchUpdateApplicableTo(batchUpdateApplicableTo);
			}

			if (!deducteeNopBatchSave.isEmpty()) {
				// deductee nop batch save
				deducteeMasterResidentialDAO.deducteeNopBatchSave(deducteeNopBatchSave, tenantId);
			}
			if (!deducteeNopBatchUpdate.isEmpty()) {
				// deductee nop batch update
				deducteeMasterResidentialDAO.deducteeNopBatchUpdate(deducteeNopBatchUpdate);
			}

			batchUpload.setSuccessCount(Long.valueOf(totalRecords));
			batchUpload.setRowsCount(Long.valueOf(totalRecords));
			batchUpload.setFailedCount(errorCount);
			batchUpload.setProcessedCount(dedcuteesSet.size());
			batchUpload.setDuplicateCount(0L);
			batchUpload.setStatus("Processed");
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			if (!errorList.isEmpty()) {
				deducteeErrorsFile = prepareNonResidentDeducteesErrorFile(batchUpload.getFileName(), deductorTan,
						deductorPan, errorList);
			}

			// Generating deductee pan file and uploading to pan validation
			MultipartFile file = generateDeducteePanXlsxReport(deducteeList, Collections.emptyList());
			String panUrl = blob.uploadExcelToBlob(file, tenantId);
			batchUpload.setOtherFileUrl(panUrl);
		} catch (Exception e) {
			logger.error("Exception occurred :{}", e.getMessage());
		}

		return deducteeBatchUpload(batchUpload, null, deductorTan, assesssmentYear, assessmentMonthPlusOne, userName,
				deducteeErrorsFile, tenantId);
	}

	private DeducteeMasterNonResidential validate194QFields(DeducteeMasterNonResidential deductee,
			DeducteeMasterNonResidentialErrorReportCsvDTO nonResidentErrorDTO) {
		// null check
		deductee.setDeducteeMasterBalancesOf194q(isNull(deductee.getDeducteeMasterBalancesOf194q()));
		deductee.setAdvanceBalancesOf194q(isNull(deductee.getAdvanceBalancesOf194q()));
		deductee.setProvisionBalancesOf194q(isNull(deductee.getProvisionBalancesOf194q()));
		deductee.setAdvancesAsOfMarch(isNull(deductee.getAdvancesAsOfMarch()));
		deductee.setProvisionsAsOfMarch(isNull(deductee.getProvisionsAsOfMarch()));
		deductee.setCurrentBalanceYear(isNull(deductee.getCurrentBalanceYear()));
		deductee.setCurrentBalanceMonth(isNull(deductee.getCurrentBalanceMonth()));
		deductee.setPreviousBalanceYear(isNull(deductee.getPreviousBalanceYear()));
		deductee.setPreviousBalanceMonth(isNull(deductee.getPreviousBalanceMonth()));
		deductee.setOpeningBalanceCreditNote(isNull(deductee.getOpeningBalanceCreditNote()));
		if (deductee.getCurrentBalanceYear() > 0
				&& !String.valueOf(deductee.getCurrentBalanceYear()).matches("[0-9]{4}")) {
			nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Current Balance year "
					+ deductee.getCurrentBalanceYear() + " is not valid." + "\n");
		}
		if (deductee.getCurrentBalanceMonth() > 0
				&& !String.valueOf(deductee.getCurrentBalanceMonth()).matches("^([1-9]|[0-1][0-2])$")) {
			nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Current Balance month "
					+ deductee.getCurrentBalanceMonth() + " is not valid." + "\n");
		}

		if (deductee.getPreviousBalanceYear() > 0
				&& !String.valueOf(deductee.getPreviousBalanceYear()).matches("[0-9]{4}")) {
			nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Previous Balance year "
					+ deductee.getPreviousBalanceYear() + " is not valid." + "\n");
		}

		if (deductee.getPreviousBalanceMonth() > 0
				&& !String.valueOf(deductee.getPreviousBalanceMonth()).matches("^([1-9]|[0-1][0-2])$")) {
			nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + "Previous Balance month "
					+ deductee.getPreviousBalanceMonth() + " is not valid." + "\n");
		}
		return deductee;
	}

	private DeducteeMasterNonResidential getNRDeducteeCsvMapping(CsvRow row,
			DeducteeMasterNonResidentialErrorReportCsvDTO nonResidentErrorDTO) {
		DeducteeMasterNonResidential deducteeRow = new DeducteeMasterNonResidential();
		deducteeRow.setSourceIdentifier(row.getField("SourceIdentifier"));
		deducteeRow.setSourceFileName(row.getField("SourceFileName"));
		deducteeRow.setDeductorCode(row.getField("DeductorCode"));
		deducteeRow.setDeductorName(row.getField("DeductorName"));
		deducteeRow.setDeductorPan(row.getField("DeductorPAN"));
		deducteeRow.setDeductorTan(row.getField("DeductorTAN"));
		deducteeRow.setDeducteeResidentialStatus(row.getField("DeducteeResidentialStatus"));
		deducteeRow.setDeducteeCode(row.getField("DeducteeCode"));
		deducteeRow.setDeducteeName(row.getField("DeducteeName"));
		deducteeRow.setDeducteePAN(row.getField("DeducteePAN"));
		deducteeRow.setDeducteeAadharNumber(row.getField("DeducteeAadharNumber"));
		deducteeRow.setDeducteeTin(row.getField("DeducteeTIN"));
		deducteeRow.setDeducteeStatus(row.getField("DeducteeStatus"));
		Boolean isTrcAvailable = StringUtils.isNotBlank(row.getField("IsTRCAvailable"))
				&& row.getField("IsTRCAvailable").equals("Y");
		deducteeRow.setIsTRCAvailable(isTrcAvailable);
		if (isTrcAvailable) {
			Date trcApplicableFrom = dateFormatValidation(row.getField("TRCApplicableFromDate"), nonResidentErrorDTO,
					"TRCApplicableFromDate");
			deducteeRow.setTrcApplicableFrom(trcApplicableFrom);
			Date trcApplicableTo = dateFormatValidation(row.getField("TRCApplicableToDate"), nonResidentErrorDTO,
					"TRCApplicableToDate");
			deducteeRow.setTrcApplicableTo(trcApplicableTo);
		}
		Boolean istenFAvailable = StringUtils.isNotBlank(row.getField("IsForm10FAvailable"))
				&& row.getField("IsForm10FAvailable").equals("Y");
		deducteeRow.setIsTenFAvailable(istenFAvailable);
		if (istenFAvailable) {
			Date tenFApplicableFrom = dateFormatValidation(row.getField("Form10FApplicableFromDate"),
					nonResidentErrorDTO, "Form10FApplicableFromDate");
			deducteeRow.setTenFApplicableFrom(tenFApplicableFrom);
			Date tenFApplicableTo = dateFormatValidation(row.getField("Form10FApplicableToDate"), nonResidentErrorDTO,
					"10F applicable to");
			deducteeRow.setTenFApplicableTo(tenFApplicableTo);
		}
		Boolean isTrcFuture = StringUtils.isNotBlank(row.getField("TRCAvailableInFuture"))
				&& row.getField("TRCAvailableInFuture").equals("Y");
		deducteeRow.setIstrcFuture(isTrcFuture);
		if (isTrcFuture) {
			Date trcFutureDate = dateFormatValidation(row.getField("TRCAvailableFutureDate"), nonResidentErrorDTO,
					"TRCAvailableFutureDate");
			deducteeRow.setTrcFutureDate(trcFutureDate);
		}
		Boolean isTenfFuture = StringUtils.isNotBlank(row.getField("Form10FAvailableInFuture"))
				&& row.getField("Form10FAvailableInFuture").equals("Y");
		deducteeRow.setIstenfFuture(isTenfFuture);
		if (isTenfFuture) {
			Date tenfFutureDate = dateFormatValidation(row.getField("Form10FAvailableFutureDate"), nonResidentErrorDTO,
					"Form10FAvailableFutureDate");
			deducteeRow.setTenfFutureDate(tenfFutureDate);
		}
		Boolean peIndia = StringUtils.isNotBlank(row.getField("WhetherPEInIndia"))
				&& row.getField("WhetherPEInIndia").equals("Y");
		deducteeRow.setWhetherPEInIndia(peIndia);
		if (peIndia) {
			Date peIndiaApplicableFrom = dateFormatValidation(row.getField("PEInIndiaFromDate"), nonResidentErrorDTO,
					"PEInIndiaFromDate");
			deducteeRow.setWhetherPEInIndiaApplicableFrom(peIndiaApplicableFrom);
			Date peIndiaApplicableTo = dateFormatValidation(row.getField("PEInIndiaToDate"), nonResidentErrorDTO,
					"PEInIndiaToDate");
			deducteeRow.setWhetherPEInIndiaApplicableTo(peIndiaApplicableTo);
		}
		Boolean businesCarriedInIndia = StringUtils.isNotBlank(row.getField("IsBusinessCarriedOutInIndia"))
				&& row.getField("IsBusinessCarriedOutInIndia").equals("Y");
		deducteeRow.setIsBusinessCarriedInIndia(businesCarriedInIndia);
		Boolean isPurchasegoods = StringUtils.isNotBlank(row.getField("IsPEInvolvedInPurchaseOfGoods"))
				&& row.getField("IsPEInvolvedInPurchaseOfGoods").equals("Y");
		deducteeRow.setIsPEinvoilvedInPurchaseGoods(isPurchasegoods);
		Boolean amountReceived = StringUtils.isNotBlank(row.getField("IsIncomeReceivedConnectedWithPE"))
				&& row.getField("IsIncomeReceivedConnectedWithPE").equals("Y");
		deducteeRow.setIsPEamountReceived(amountReceived);
		Boolean noPeDocumentAvailable = StringUtils.isNotBlank(row.getField("IsNoPEDeclarationAvailable"))
				&& row.getField("IsNoPEDeclarationAvailable").equals("Y");
		deducteeRow.setNoPEDocumentAvailable(noPeDocumentAvailable);
		if (noPeDocumentAvailable) {
			Date noPEDocumentApplicableFrom = dateFormatValidation(row.getField("NoPEDeclarationApplicableFromDate"),
					nonResidentErrorDTO, "NoPEDeclarationApplicableFromDate");
			deducteeRow.setNoPEApplicableFrom(noPEDocumentApplicableFrom);
			Date noPEDocumentApplicableTo = dateFormatValidation(row.getField("NoPEDeclarationApplicableToDate"),
					nonResidentErrorDTO, "NoPEDeclarationApplicableToDate");
			deducteeRow.setNoPEApplicableTo(noPEDocumentApplicableTo);
		}
		Boolean noPeDeclarationInFuture = StringUtils.isNotBlank(row.getField("NoPEDeclarationAvailableInFuture"))
				&& row.getField("NoPEDeclarationAvailableInFuture").equals("Y");
		deducteeRow.setNoPEDeclarationAvailableInFuture(noPeDeclarationInFuture);
		if (noPeDeclarationInFuture) {
			Date noPeDeclarationInFutureDate = dateFormatValidation(row.getField("NoPEDeclarationAvailableFutureDate"),
					nonResidentErrorDTO, "NoPEDeclarationAvailableFutureDate");
			deducteeRow.setNoPEDeclarationAvailableFutureDate(noPeDeclarationInFutureDate);
		}
		Boolean isPoemAvailable = StringUtils.isNotBlank(row.getField("IsPOEMApplicable"))
				&& row.getField("IsPOEMApplicable").equals("Y");
		deducteeRow.setIsPOEMavailable(isPoemAvailable);
		if (isPoemAvailable) {
			Date poemApplicableFrom = dateFormatValidation(row.getField("POEMApplicableFromDate"), nonResidentErrorDTO,
					"POEMApplicableFromDate");
			deducteeRow.setPoemApplicableFrom(poemApplicableFrom);
			Date poemApplicableTo = dateFormatValidation(row.getField("POEMApplicableToDate"), nonResidentErrorDTO,
					"POEMApplicableToDate");
			deducteeRow.setPoemApplicableTo(poemApplicableTo);
		}
		Boolean isNoPOEMDeclarationAvailable = StringUtils.isNotBlank(row.getField("IsNoPOEMDeclarationAvailable"))
				&& row.getField("IsNoPOEMDeclarationAvailable").equals("Y");
		deducteeRow.setIsNoPOEMDeclarationAvailable(isNoPOEMDeclarationAvailable);
		if (isNoPOEMDeclarationAvailable) {
			Date noPOEMApplicableFromDate = dateFormatValidation(row.getField("NoPOEMDeclarationApplicableFromDate"),
					nonResidentErrorDTO, "NoPOEMDeclarationApplicableFromDate");
			deducteeRow.setNoPOEMDeclarationApplicableFromDate(noPOEMApplicableFromDate);
			Date noPOEMApplicableToDate = dateFormatValidation(row.getField("NoPOEMDeclarationApplicableToDate"),
					nonResidentErrorDTO, "NoPOEMDeclarationApplicableToDate");
			deducteeRow.setNoPOEMDeclarationApplicableToDate(noPOEMApplicableToDate);
		}
		Boolean noPoemDeclaration = StringUtils.isNotBlank(row.getField("NoPOEMDeclarationAvailableInFuture"))
				&& row.getField("NoPOEMDeclarationAvailableInFuture").equals("Y");
		deducteeRow.setIsPoemDeclaration(noPoemDeclaration);
		if (noPoemDeclaration) {
			Date poemFutureDate = dateFormatValidation(row.getField("NoPOEMDeclarationAvailableFutureDate"),
					nonResidentErrorDTO, "POEM future");
			deducteeRow.setPoemFutureDate(poemFutureDate);
		}
		Boolean isFixedBaseAvailable = StringUtils.isNotBlank(row.getField("IsFixedBaseAvailableInIndia"))
				&& row.getField("IsFixedBaseAvailableInIndia").equals("Y");
		deducteeRow.setIsFixedbaseAvailbleIndia(isFixedBaseAvailable);
		if (isFixedBaseAvailable) {
			Date fixedBaseApplicableFrom = dateFormatValidation(row.getField("FixedBaseInIndiaFromDate"),
					nonResidentErrorDTO, "Fixed base applicable from");
			deducteeRow.setFixedbaseAvailbleIndiaApplicableFrom(fixedBaseApplicableFrom);
			Date fixedBaseApplicableTo = dateFormatValidation(row.getField("FixedBaseInIndiaToDate"),
					nonResidentErrorDTO, "Fixed base applicable to");
			deducteeRow.setFixedbaseAvailbleIndiaApplicableTo(fixedBaseApplicableTo);
		}
		Boolean isNoFixedBaseAvailable = StringUtils.isNotBlank(row.getField("IsNoFixedBaseDeclarationAvailable"))
				&& row.getField("IsNoFixedBaseDeclarationAvailable").equals("Y");
		deducteeRow.setIsNoFixedBaseDeclarationAvailable(isNoFixedBaseAvailable);
		if (isNoFixedBaseAvailable) {
			Date noFixedBaseFromDate = dateFormatValidation(row.getField("NoFixedBaseDeclarationFromDate"),
					nonResidentErrorDTO, "NoFixedBaseDeclarationFromDate");
			deducteeRow.setNoFixedBaseDeclarationFromDate(noFixedBaseFromDate);
			Date noFixedBaseToDate = dateFormatValidation(row.getField("NoFixedBaseDeclarationToDate"),
					nonResidentErrorDTO, "NoFixedBaseDeclarationToDate");
			deducteeRow.setNoFixedBaseDeclarationToDate(noFixedBaseToDate);
		}
		Boolean noFixedBaseAvailableInFuture = StringUtils
				.isNotBlank(row.getField("NoFixedBaseDeclarationAvailableInFuture"))
				&& row.getField("NoFixedBaseDeclarationAvailableInFuture").equals("Y");
		deducteeRow.setNoFixedBaseDeclarationAvailableInFuture(noFixedBaseAvailableInFuture);
		if (noFixedBaseAvailableInFuture) {
			Date noFixedBaseAvailableFutureDate = dateFormatValidation(
					row.getField("NoFixedBaseDeclarationAvailableFutureDate"), nonResidentErrorDTO,
					"NoFixedBaseDeclarationAvailableFutureDate");
			deducteeRow.setNoFixedBaseDeclarationAvailableFutureDate(noFixedBaseAvailableFutureDate);
		}
		deducteeRow.setEmailAddress(row.getField("DeducteeEmail"));
		deducteeRow.setPhoneNumber(row.getField("DeducteePhone"));
		deducteeRow.setFlatDoorBlockNo(row.getField("DeducteeFloorNumber"));
		deducteeRow.setNameBuildingVillage(row.getField("DeducteeBuildingName"));
		deducteeRow.setRoadStreetPostoffice(row.getField("DeducteeStreet"));
		deducteeRow.setAreaLocality(row.getField("DeducteeArea"));
		deducteeRow.setTownCityDistrict(row.getField("DeducteeTown"));
		deducteeRow.setState(row.getField("DeducteeState"));
		deducteeRow.setCountry(row.getField("DeducteeCountry"));
		deducteeRow.setPinCode(row.getField("DeducteePincode"));
		deducteeRow.setSection(row.getField("TDSSection"));
		deducteeRow.setNatureOfPayment(row.getField("NatureOfPayment"));
		BigDecimal rate = StringUtils.isNotBlank(row.getField("TDSRate"))
				? new BigDecimal(row.getField("TDSRate").trim().replace(",", ""))
				: BigDecimal.ZERO;
		rate = StringUtils.isNotBlank(deducteeRow.getSection()) ? rate : BigDecimal.ZERO;
		deducteeRow.setRate(rate);
		deducteeRow.setSectionCode(row.getField("TDSTaxCodeERP"));
		Boolean tdsExemptionFlag = StringUtils.isNotBlank(row.getField("TDSExemptionFlag"))
				&& row.getField("TDSExemptionFlag").equals("Y");
		deducteeRow.setTdsExemptionFlag(tdsExemptionFlag);
		deducteeRow.setTdsExemptionReason(row.getField("TDSExemptionReason"));
		Boolean isThresholdLimitApplicable = StringUtils.isNotBlank(row.getField("TDSThresholdApplicabilityFlag"))
				&& row.getField("TDSThresholdApplicabilityFlag").equals("Y");
		deducteeRow.setIsThresholdLimitApplicable(isThresholdLimitApplicable);
		Date applicableFrom = dateFormatValidation(row.getField("ApplicableFrom"), nonResidentErrorDTO,
				"ApplicableFrom");
		deducteeRow.setApplicableFrom(applicableFrom);
		Date applicableTo = dateFormatValidation(row.getField("ApplicableTo"), nonResidentErrorDTO, "ApplicableTo");
		deducteeRow.setApplicableTo(applicableTo);
		deducteeRow.setUserDefinedField1(row.getField("UserDefinedField1"));
		deducteeRow.setUserDefinedField2(row.getField("UserDefinedField2"));
		deducteeRow.setUserDefinedField3(row.getField("UserDefinedField3"));
		deducteeRow.setRelatedParty(row.getField("IsDeducteeARelatedParty"));
		Boolean isAmountConnectedFixedBase = StringUtils
				.isNotBlank(row.getField("IsAmountReceivedConnectedWithFixedBase"))
				&& row.getField("IsAmountReceivedConnectedWithFixedBase").equals("Y");
		deducteeRow.setIsAmountConnectedFixedBase(isAmountConnectedFixedBase);
		Boolean isGrossingUp = StringUtils.isNotBlank(row.getField("GrossUpIndicator"))
				&& row.getField("GrossUpIndicator").equals("Y");
		deducteeRow.setIsGrossingUp(isGrossingUp);
		Boolean isTransparentEntity = StringUtils.isNotBlank(row.getField("FiscallyTransparentEntity"))
				&& row.getField("FiscallyTransparentEntity").equals("Y");
		deducteeRow.setIsDeducteeTransparent(isTransparentEntity);
		deducteeRow.setPrinciplesOfBusinessPlace(row.getField("PrinciplePlaceOfBusiness"));
		deducteeRow.setStayPeriodFinancialYear(row.getField("PeriodOfStayInIndia"));
		deducteeRow.setCountryToRemittance(row.getField("CountryToWhichRemittanceIsMade"));
		Boolean beneficialOwnerOfIncome = StringUtils.isNotBlank(row.getField("BeneficialOwnerOfIncome"))
				&& row.getField("BeneficialOwnerOfIncome").equals("Y");
		deducteeRow.setBeneficialOwnerOfIncome(beneficialOwnerOfIncome);
		Boolean isBeneficialOwnershipOfDeclaration = StringUtils
				.isNotBlank(row.getField("IsBeneficialOwnershipDeclarationAvailable"))
				&& row.getField("IsBeneficialOwnershipDeclarationAvailable").equals("Y");
		deducteeRow.setIsBeneficialOwnershipOfDeclaration(isBeneficialOwnershipOfDeclaration);
		Boolean mliPptConditionSatisifed = StringUtils.isNotBlank(row.getField("MLIPPTConditionSatisfied"))
				&& row.getField("MLIPPTConditionSatisfied").equals("Y");
		deducteeRow.setMliPptConditionSatisifed(mliPptConditionSatisifed);
		Boolean mliSlobConditionSatisifed = StringUtils.isNotBlank(row.getField("MLISLOBConditionSatisfied"))
				&& row.getField("MLISLOBConditionSatisfied").equals("Y");
		deducteeRow.setMliSlobConditionSatisifed(mliSlobConditionSatisifed);
		Boolean isMliPptSlob = StringUtils.isNotBlank(row.getField("IsMLIPPTOrSLOBDeclarationAvailable"))
				&& row.getField("IsMLIPPTOrSLOBDeclarationAvailable").equals("Y");
		deducteeRow.setIsMliPptSlob(isMliPptSlob);
		deducteeRow.setNatureOfRemittance(row.getField("NatureOfRemittance"));
		deducteeRow.setArticleNumberDtaa(row.getField("ArticleOfDTAA"));
		deducteeRow.setSectionOfIncometaxAct(row.getField("DTAAArticleName"));
		Boolean aggreementForTransaction = StringUtils.isNotBlank(row.getField("DoYouHaveAnAgreementForTheTransaction"))
				&& row.getField("DoYouHaveAnAgreementForTheTransaction").equals("Y");
		deducteeRow.setAggreementForTransaction(aggreementForTransaction);
		deducteeRow.setCharteredAccountantNo(row.getField("CharteredAccountantNo"));
		deducteeRow.setDeducteeGSTIN(row.getField("DeducteeGSTIN"));
		deducteeRow.setGrOrIRIndicator(row.getField("GR/IRIndicator"));
		deducteeRow.setTdsApplicabilityUnderSection(row.getField("TDSApplicabilityUnderSection194QVsTDSOtherSections"));
		BigDecimal openingBalanceInvoices = StringUtils.isNotBlank(row.getField("OpeningBalanceInvoices"))
				? new BigDecimal(row.getField("OpeningBalanceInvoices").trim().replace(",", ""))
				: BigDecimal.ZERO;
		deducteeRow.setDeducteeMasterBalancesOf194q(openingBalanceInvoices);
		BigDecimal openingBalanceAdvances = StringUtils.isNotBlank(row.getField("OpeningBalanceAdvances"))
				? new BigDecimal(row.getField("OpeningBalanceAdvances").trim().replace(",", "").trim())
				: BigDecimal.ZERO;
		deducteeRow.setAdvanceBalancesOf194q(openingBalanceAdvances);
		BigDecimal openingBalanceProvisions = StringUtils.isNotBlank(row.getField("OpeningBalanceProvisions"))
				? new BigDecimal(row.getField("OpeningBalanceProvisions").trim().replace(",", "").trim())
				: BigDecimal.ZERO;
		deducteeRow.setProvisionBalancesOf194q(openingBalanceProvisions);
		BigDecimal openingBalanceCreditNote = StringUtils.isNotBlank(row.getField("OpeningBalanceCreditNote"))
				? new BigDecimal(row.getField("OpeningBalanceCreditNote").trim().replace(",", ""))
				: BigDecimal.ZERO;
		deducteeRow.setOpeningBalanceCreditNote(openingBalanceCreditNote);
		int currentBalanceMonth = StringUtils.isNotBlank(row.getField("CurrentBalanceMonth"))
				? Integer.parseInt(row.getField("CurrentBalanceMonth"))
				: 0;
		deducteeRow.setCurrentBalanceMonth(currentBalanceMonth);
		int currentBalanceYear = StringUtils.isNotBlank(row.getField("CurrentBalanceYear"))
				? Integer.parseInt(row.getField("CurrentBalanceYear"))
				: 0;
		deducteeRow.setCurrentBalanceYear(currentBalanceYear);
		BigDecimal advancesAsOfMarch = StringUtils.isNotBlank(row.getField("AdvancesAsOfMarch"))
				? new BigDecimal(row.getField("AdvancesAsOfMarch").trim().replace(",", ""))
				: BigDecimal.ZERO;
		deducteeRow.setAdvancesAsOfMarch(advancesAsOfMarch);
		BigDecimal provisionsAsOfMarch = StringUtils.isNotBlank(row.getField("ProvisionsAsOfMarch"))
				? new BigDecimal(row.getField("ProvisionsAsOfMarch").trim().replace(",", ""))
				: BigDecimal.ZERO;
		deducteeRow.setProvisionsAsOfMarch(provisionsAsOfMarch);
		int previousBalanceMonth = StringUtils.isNotBlank(row.getField("PreviousBalanceMonth"))
				? Integer.parseInt(row.getField("PreviousBalanceMonth"))
				: 0;
		deducteeRow.setPreviousBalanceMonth(previousBalanceMonth);
		int previousBalanceYear = StringUtils.isNotBlank(row.getField("PreviousBalanceYear"))
				? Integer.parseInt(row.getField("PreviousBalanceYear"))
				: 0;
		deducteeRow.setPreviousBalanceYear(previousBalanceYear);
		deducteeRow.setDeducteeTan(row.getField("DeducteeTAN"));
		deducteeRow.setTdsExemptionNumber(row.getField("TDSExemptionNumber"));
		deducteeRow.setTdsSectionDescription(row.getField("TDSSectionDescription"));

		return deducteeRow;
	}

	private DeducteeMasterNonResidentialErrorReportCsvDTO getNrDeducteeErrorCsvMapping(CsvRow row,
			DeducteeMasterNonResidentialErrorReportCsvDTO nonResidentErrorDTO) {
		nonResidentErrorDTO.setSourceIdentifier(row.getField("SourceIdentifier"));
		nonResidentErrorDTO.setSourceFileName(row.getField("SourceFileName"));
		nonResidentErrorDTO.setCompanyCode(row.getField("DeductorCode"));
		nonResidentErrorDTO.setDeductorName(row.getField("DeductorName"));
		nonResidentErrorDTO.setDeductorPan(row.getField("DeductorPAN"));
		nonResidentErrorDTO.setDeductorTan(row.getField("DeductorTAN"));
		nonResidentErrorDTO.setDeducteeResidentialStatus(row.getField("DeducteeResidentialStatus"));
		nonResidentErrorDTO.setDeducteeCode(row.getField("DeducteeCode"));
		nonResidentErrorDTO.setDeducteeName(row.getField("DeducteeName"));
		nonResidentErrorDTO.setDeducteePAN(row.getField("DeducteePAN"));
		nonResidentErrorDTO.setDeducteeAadharNumber(row.getField("DeducteeAadharNumber"));
		nonResidentErrorDTO.setDeducteeTin(row.getField("DeducteeTIN"));
		nonResidentErrorDTO.setDeducteeStatus(row.getField("DeducteeStatus"));
		nonResidentErrorDTO.setIsTRCAvailable(row.getField("IsTRCAvailable"));
		nonResidentErrorDTO.setTrcApplicableFrom(row.getField("TRCApplicableFromDate"));
		nonResidentErrorDTO.setTrcApplicableTo(row.getField("TRCApplicableToDate"));
		nonResidentErrorDTO.setIsTenFAvailable(row.getField("IsForm10FAvailable"));
		nonResidentErrorDTO.setTenFApplicableFrom(row.getField("Form10FApplicableFromDate"));
		nonResidentErrorDTO.setTenFApplicableTo(row.getField("Form10FApplicableToDate"));
		nonResidentErrorDTO.setIstrcFuture(row.getField("TRCAvailableInFuture"));
		nonResidentErrorDTO.setTrcFutureDate(row.getField("TRCAvailableFutureDate"));
		nonResidentErrorDTO.setIstenfFuture(row.getField("Form10FAvailableInFuture"));
		nonResidentErrorDTO.setTenfFutureDate(row.getField("Form10FAvailableFutureDate"));
		nonResidentErrorDTO.setWhetherPEInIndia(row.getField("WhetherPEInIndia"));
		nonResidentErrorDTO.setWhetherPEInIndiaApplicableFrom(row.getField("PEInIndiaFromDate"));
		nonResidentErrorDTO.setWhetherPEInIndiaApplicableTo(row.getField("PEInIndiaToDate"));
		nonResidentErrorDTO.setIsBusinessCarriedInIndia(row.getField("IsBusinessCarriedOutInIndia"));
		nonResidentErrorDTO.setIsPEinvoilvedInPurchaseGoods(row.getField("IsPEInvolvedInPurchaseOfGoods"));
		nonResidentErrorDTO.setIsAmountConnectedFixedBase(row.getField("IsIncomeReceivedConnectedWithPE"));
		nonResidentErrorDTO.setNoPEDocumentAvailable(row.getField("IsNoPEDeclarationAvailable"));
		nonResidentErrorDTO.setNoPEApplicableFrom(row.getField("NoPEDeclarationApplicableFromDate"));
		nonResidentErrorDTO.setNoPEApplicableTo(row.getField("NoPEDeclarationApplicableToDate"));
		nonResidentErrorDTO.setNoPEDeclarationAvailableInFuture(row.getField("NoPEDeclarationAvailableInFuture"));
		nonResidentErrorDTO.setNoPEDeclarationAvailableFutureDate(row.getField("NoPEDeclarationAvailableFutureDate"));
		nonResidentErrorDTO.setIsPOEMavailable(row.getField("IsPOEMApplicable"));
		nonResidentErrorDTO.setPoemApplicableFrom(row.getField("POEMApplicableFromDate"));
		nonResidentErrorDTO.setPoemApplicableTo(row.getField("POEMApplicableToDate"));
		nonResidentErrorDTO.setIsNoPOEMDeclarationAvailable(row.getField("IsNoPOEMDeclarationAvailable"));
		nonResidentErrorDTO.setNoPOEMDeclarationApplicableFromDate(row.getField("NoPOEMDeclarationApplicableFromDate"));
		nonResidentErrorDTO.setNoPOEMDeclarationApplicableToDate(row.getField("NoPOEMDeclarationApplicableToDate"));
		nonResidentErrorDTO.setIsPoemDeclaration(row.getField("NoPOEMDeclarationAvailableInFuture"));
		nonResidentErrorDTO.setPoemFutureDate(row.getField("NoPOEMDeclarationAvailableFutureDate"));
		nonResidentErrorDTO.setIsFixedbaseAvailbleIndia(row.getField("IsFixedBaseAvailableInIndia"));
		nonResidentErrorDTO.setFixedbaseAvailbleIndiaApplicableFrom(row.getField("FixedBaseInIndiaFromDate"));
		nonResidentErrorDTO.setFixedbaseAvailbleIndiaApplicableTo(row.getField("FixedBaseInIndiaToDate"));
		nonResidentErrorDTO.setIsNoFixedBaseDeclarationAvailable(row.getField("IsNoFixedBaseDeclarationAvailable"));
		nonResidentErrorDTO.setNoFixedBaseDeclarationFromDate(row.getField("NoFixedBaseDeclarationFromDate"));
		nonResidentErrorDTO.setNoFixedBaseDeclarationToDate(row.getField("NoFixedBaseDeclarationToDate"));
		nonResidentErrorDTO
				.setNoFixedBaseDeclarationAvailableInFuture(row.getField("NoFixedBaseDeclarationAvailableInFuture"));
		nonResidentErrorDTO.setNoFixedBaseDeclarationAvailableFutureDate(
				row.getField("NoFixedBaseDeclarationAvailableFutureDate"));

		nonResidentErrorDTO.setEmailAddress(row.getField("DeducteeEmail"));
		nonResidentErrorDTO.setPhoneNumber(row.getField("DeducteePhone"));
		nonResidentErrorDTO.setFlatDoorBlockNo(row.getField("DeducteeFloorNumber"));
		nonResidentErrorDTO.setNameBuildingVillage(row.getField("DeducteeBuildingName"));
		nonResidentErrorDTO.setRoadStreetPostoffice(row.getField("DeducteeStreet"));
		nonResidentErrorDTO.setAreaLocality(row.getField("DeducteeArea"));
		nonResidentErrorDTO.setTownCityDistrict(row.getField("DeducteeTown"));
		nonResidentErrorDTO.setState(row.getField("DeducteeState"));
		nonResidentErrorDTO.setCountry(row.getField("DeducteeCountry"));
		nonResidentErrorDTO.setPinCode(row.getField("DeducteePincode"));
		nonResidentErrorDTO.setSection(row.getField("TDSSection"));
		nonResidentErrorDTO.setNatureOfPayment(row.getField("NatureOfPayment"));
		nonResidentErrorDTO.setRate(row.getField("TDSRate"));
		nonResidentErrorDTO.setSectionCode(row.getField("TDSTaxCodeERP"));
		nonResidentErrorDTO.setTdsExemptionFlag(row.getField("TDSExemptionFlag"));
		nonResidentErrorDTO.setTdsExemptionReason(row.getField("TDSExemptionReason"));
		nonResidentErrorDTO.setIsThresholdLimitApplicable(row.getField("TDSThresholdApplicabilityFlag"));
		nonResidentErrorDTO.setApplicableFrom(row.getField("ApplicableFrom"));
		nonResidentErrorDTO.setApplicableTo(row.getField("ApplicableTo"));
		nonResidentErrorDTO.setUserDefinedField1(row.getField("UserDefinedField1"));
		nonResidentErrorDTO.setUserDefinedField2(row.getField("UserDefinedField2"));
		nonResidentErrorDTO.setUserDefinedField3(row.getField("UserDefinedField3"));
		nonResidentErrorDTO.setRelatedParty(row.getField("IsDeducteeARelatedParty"));
		nonResidentErrorDTO.setIsAmountConnectedFixedBase(row.getField("IsAmountReceivedConnectedWithFixedBase"));
		nonResidentErrorDTO.setIsGrossingUp(row.getField("GrossUpIndicator"));
		nonResidentErrorDTO.setIsDeducteeTransparent(row.getField("FiscallyTransparentEntity"));
		nonResidentErrorDTO.setPrinciplesOfBusinessPlace(row.getField("PrinciplePlaceOfBusiness"));
		nonResidentErrorDTO.setStayPeriodFinancialYear(row.getField("PeriodOfStayInIndia"));
		nonResidentErrorDTO.setCountryToRemittance(row.getField("CountryToWhichRemittanceIsMade"));
		nonResidentErrorDTO.setBeneficialOwnerOfIncome(row.getField("BeneficialOwnerOfIncome"));
		nonResidentErrorDTO
				.setIsBeneficialOwnershipOfDeclaration(row.getField("IsBeneficialOwnershipDeclarationAvailable"));
		nonResidentErrorDTO.setMliPptConditionSatisifed(row.getField("MLIPPTConditionSatisfied"));
		nonResidentErrorDTO.setMliSlobConditionSatisifed(row.getField("MLISLOBConditionSatisfied"));
		nonResidentErrorDTO.setIsMliPptSlob(row.getField("IsMLIPPTOrSLOBDeclarationAvailable"));
		nonResidentErrorDTO.setNatureOfRemittance(row.getField("NatureOfRemittance"));
		nonResidentErrorDTO.setArticleNumberDtaa(row.getField("ArticleOfDTAA"));
		nonResidentErrorDTO.setSectionOfIncometaxAct(row.getField("DTAAArticleName"));
		nonResidentErrorDTO.setAggreementForTransaction(row.getField("DoYouHaveAnAgreementForTheTransaction"));
		nonResidentErrorDTO.setCharteredAccountantNo(row.getField("CharteredAccountantNo"));
		nonResidentErrorDTO.setDeducteeGSTIN(row.getField("DeducteeGSTIN"));
		nonResidentErrorDTO.setGrOrIRIndicator(row.getField("GR/IRIndicator"));
		nonResidentErrorDTO
				.setTdsApplicabilityUnderSection(row.getField("TDSApplicabilityUnderSection194QVsTDSOtherSections"));
		nonResidentErrorDTO.setDeducteeMasterBalancesOf194q(row.getField("OpeningBalanceInvoices"));
		nonResidentErrorDTO.setAdvanceBalancesOf194q(row.getField("OpeningBalanceAdvances"));
		nonResidentErrorDTO.setProvisionBalancesOf194q(row.getField("OpeningBalanceProvisions"));
		nonResidentErrorDTO.setOpeningBalanceCreditNote(row.getField("OpeningBalanceCreditNote"));
		nonResidentErrorDTO.setCurrentBalanceMonth(row.getField("CurrentBalanceMonth"));
		nonResidentErrorDTO.setCurrentBalanceYear(row.getField("CurrentBalanceYear"));
		nonResidentErrorDTO.setAdvancesAsOfMarch(row.getField("AdvancesAsOfMarch"));
		nonResidentErrorDTO.setProvisionsAsOfMarch(row.getField("ProvisionsAsOfMarch"));
		nonResidentErrorDTO.setPreviousBalanceMonth(row.getField("PreviousBalanceMonth"));
		nonResidentErrorDTO.setPreviousBalanceYear(row.getField("PreviousBalanceYear"));
		nonResidentErrorDTO.setDeducteeTan(row.getField("DeducteeTAN"));
		nonResidentErrorDTO.setTdsExemptionNumber(row.getField("TDSExemptionNumber"));
		nonResidentErrorDTO.setTdsSectionDescription(row.getField("TDSSectionDescription"));

		return nonResidentErrorDTO;
	}

	private Date dateFormatValidation(String dateField,
			DeducteeMasterNonResidentialErrorReportCsvDTO nonResidentErrorDTO, String filedType) {
		Date value = null;
		if (StringUtils.isNotBlank(dateField)) {
			String dateString = dateField.replace("/", "-");
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = sdf.parse(dateString);
				if (!dateString.equals(sdf.format(date))) {
					nonResidentErrorDTO.setReason(nonResidentErrorDTO.getReason() + filedType
							+ " date should be in YYYY-MM-DD format." + "\n");
				}
				value = date;
			} catch (Exception e) {
				nonResidentErrorDTO.setReason(
						nonResidentErrorDTO.getReason() + filedType + " date should be in YYYY-MM-DD format." + "\n");
			}
		}
		return value;
	}

	private Map<String, List<DeducteeMasterNonResidential>> getDeducteeNonResidentRecords(String deductorPan) {
		Map<String, List<DeducteeMasterNonResidential>> deducteeMap = new HashMap<>();
		List<DeducteeMasterNonResidential> deductees = deducteeMasterNonResidentialDAO.getDeducteesByPan(deductorPan);
		for (DeducteeMasterNonResidential deductee : deductees) {
			if (!deducteeMap.containsKey(deductee.getDeducteeKey())) {
				deducteeMap.put(deductee.getDeducteeKey(), new ArrayList<>());
			}
			deducteeMap.get(deductee.getDeducteeKey()).add(deductee);
		}
		return deducteeMap;
	}

	/**
	 * 
	 * @param deducteeList
	 * @param deducteeMap
	 * @param deducteeNrDbData
	 * @param batchUpdateApplicableTo
	 * @param deducteeBatchSave
	 * @param deducteeBatchUpdate
	 * @param thresholdLimitMaster
	 * @param deducteeNopBatchSave
	 * @param deducteeNopBatchUpdate
	 * @return
	 * @throws Exception
	 */
	public int processNonResidentRecords(List<DeducteeMasterNonResidential> deducteeList,
			Map<String, List<DeducteeMasterNonResidential>> deducteeMap,
			Map<String, List<DeducteeMasterNonResidential>> deducteeNrDbData,
			List<DeducteeMasterNonResidential> batchUpdateApplicableTo,
			List<DeducteeMasterNonResidential> deducteeBatchSave,
			List<DeducteeMasterNonResidential> deducteeBatchUpdate,
			List<ThresholdLimitGroupMaster> thresholdLimitMaster, List<DeducteeNopGroup> deducteeNopBatchSave,
			List<DeducteeNopGroup> deducteeNopBatchUpdate) throws Exception {
		int duplicateCount = 0;
		List<Integer> groupIdList = new ArrayList<>();
		Map<String, List<DeducteeNopGroup>> deducteeNopMap = new HashMap<>();
		// logic added for deductee multiple sections
		for (Entry<String, List<DeducteeMasterNonResidential>> entry : deducteeMap.entrySet()) {
			DeducteeMasterNonResidential deducteeData = entry.getValue().get(0);
			// retrieving existing deductee details with out considering special characters
			// in deductee name.
			List<DeducteeMasterNonResidential> deducteeDbDetails = deducteeNrDbData
					.get(deducteeData.getDeducteeKey()) != null ? deducteeNrDbData.get(deducteeData.getDeducteeKey())
							: new ArrayList<>();
			DeducteeMasterNonResidential deducteeDB = null;
			if (!deducteeDbDetails.isEmpty()) {
				for (DeducteeMasterNonResidential deductee : deducteeDbDetails) {
					if (deductee.getApplicableTo() == null || deductee.getApplicableTo().after(new Date())) {
						deducteeDB = deductee;
						break;
					}
				}
			}
			Map<String, Float> dbSectionsAndNop = new HashMap<>();
			Map<String, Float> newSections = new HashMap<>();
			Map<String, Float> dbSections = new HashMap<>();
			Map<String, String> dbSectionsAndCode = new HashMap<>();
			Map<String, Integer> dbSectionsAndThresholds = new HashMap<>();

			boolean isduplicateSections = false;
			if (deducteeDB != null) {
				if (StringUtils.isNotBlank(deducteeDB.getSection())) {
					dbSectionsAndNop.put(deducteeDB.getSection() + "-" + deducteeDB.getNatureOfPayment(),
							(deducteeDB.getRate() != null ? deducteeDB.getRate().floatValue() : 0));
					dbSections.put(deducteeDB.getSection(),
							(deducteeDB.getRate() != null ? deducteeDB.getRate().floatValue() : 0));
					dbSectionsAndCode.put(deducteeDB.getSectionCode(), deducteeDB.getSection());
					Integer threshold = (deducteeDB.getIsThresholdLimitApplicable() != null
							&& deducteeDB.getIsThresholdLimitApplicable().equals(true)) ? 1 : 0;
					dbSectionsAndThresholds.put(deducteeDB.getSection(), threshold);
				}
				if (StringUtils.isNotBlank(deducteeDB.getAdditionalSections())) {
					Map<String, Float> dbSectionRateMap = objectMapper.readValue(deducteeDB.getAdditionalSections(),
							new TypeReference<Map<String, Float>>() {
							});
					for (Entry<String, Float> d : dbSectionRateMap.entrySet()) {
						dbSections.put(StringUtils.substringBefore(d.getKey(), "-"), d.getValue());
					}
					dbSectionsAndNop.putAll(dbSectionRateMap);
					if (StringUtils.isNotBlank(deducteeDB.getAdditionalSectionCode())) {
						Map<String, String> dbSectionAndCode = objectMapper.readValue(
								deducteeDB.getAdditionalSectionCode(), new TypeReference<Map<String, String>>() {
								});
						for (Entry<String, String> d : dbSectionAndCode.entrySet()) {
							dbSectionsAndCode.put(d.getKey(), d.getValue());
						}
						dbSectionsAndCode.putAll(dbSectionAndCode);
					}
					if (StringUtils.isNotBlank(deducteeDB.getAdditionalSectionThresholds())) {
						Map<String, Integer> dbSectionAndThreshold = objectMapper.readValue(
								deducteeDB.getAdditionalSectionThresholds(), new TypeReference<Map<String, Integer>>() {
								});
						for (Entry<String, Integer> d : dbSectionAndThreshold.entrySet()) {
							dbSectionsAndThresholds.put(d.getKey(), d.getValue());
						}
						dbSectionsAndThresholds.putAll(dbSectionAndThreshold);
					}
				}
				for (DeducteeMasterNonResidential d : entry.getValue()) {
					if (d.getSection() != null) {
						newSections.put(d.getSection(), (d.getRate() != null ? d.getRate().floatValue() : 0));
					}
				}
				isduplicateSections = dbSections.keySet().equals(newSections.keySet());
			}
			Map<String, Float> sectionsAndNop = new HashMap<>();
			Map<String, Float> sections = new HashMap<>();
			Map<String, String> sectionsAndCode = new HashMap<>();
			Map<String, Integer> sectionsAndThreshold = new HashMap<>();
			if (deducteeMap.size() != deducteeList.size() && entry.getValue().size() > 1) {
				deducteeList.removeAll(entry.getValue());
				for (DeducteeMasterNonResidential d : entry.getValue()) {
					if (StringUtils.isNotBlank(d.getDeducteePAN())
							&& StringUtils.isBlank(deducteeData.getDeducteePAN())) {
						deducteeData.setDeducteePAN(d.getDeducteePAN());
					}
					if (StringUtils.isBlank(deducteeData.getSection()) && StringUtils.isNotBlank(d.getSection())) {
						deducteeData.setSection(d.getSection());
						deducteeData.setNatureOfPayment(d.getNatureOfPayment());
						deducteeData.setIsDeducteeHasAdditionalSections(false);
						deducteeData.setSectionCode(d.getSectionCode());
						deducteeData.setRate((d.getRate() != BigDecimal.ZERO ? d.getRate() : BigDecimal.ZERO));
						deducteeData.setIsThresholdLimitApplicable(
								d.getIsThresholdLimitApplicable() != null ? d.getIsThresholdLimitApplicable() : false);
					} else if (StringUtils.isNotBlank(deducteeData.getSection())
							&& StringUtils.isNotBlank(d.getSection())
							&& !deducteeData.getSection().equals(d.getSection())) {
						String sectionAndNop = d.getSection() + "-" + d.getNatureOfPayment();
						sectionsAndNop.put(sectionAndNop != null ? sectionAndNop : "",
								(d.getRate() != null ? d.getRate().floatValue() : 0));
						sections.put(d.getSection() != null ? d.getSection() : "",
								(d.getRate() != null ? d.getRate().floatValue() : 0));
						sectionsAndCode.put(d.getSectionCode() != null ? d.getSectionCode() : "",
								d.getSection() != null ? d.getSection() : "");
						Integer threshold = (d.getIsThresholdLimitApplicable() != null
								&& d.getIsThresholdLimitApplicable().equals(true)) ? 1 : 0;
						sectionsAndThreshold.put(d.getSection(), threshold);
					}
				}
				if (sectionsAndNop.size() > 0) {
					String additionalSections = objectMapper.writeValueAsString(sectionsAndNop);
					deducteeData.setAdditionalSections(additionalSections);
				}
				if (sectionsAndNop.size() > 0) {
					String additionalSectionCode = objectMapper.writeValueAsString(sectionsAndCode);
					deducteeData.setAdditionalSectionCode(additionalSectionCode);
				}
				if (sectionsAndThreshold.size() > 0) {
					String additonalSectionThreshold = objectMapper.writeValueAsString(sectionsAndThreshold);
					deducteeData.setAdditionalSectionThresholds(additonalSectionThreshold);
				}
				deducteeList.add(deducteeData);
			} // for 3
			if (!dbSectionsAndNop.isEmpty() && !isduplicateSections) {
				for (Entry<String, Float> dBsectionAndNop : dbSectionsAndNop.entrySet()) {
					String dBSection = StringUtils.substringBefore(dBsectionAndNop.getKey(), "-");
					String dBNop = StringUtils.substringAfter(dBsectionAndNop.getKey(), "-");

					if (StringUtils.isBlank(deducteeData.getSection())) {
						deducteeData.setSection(dBSection);
						deducteeData.setNatureOfPayment(dBNop);
						deducteeData.setIsDeducteeHasAdditionalSections(false);
					} else if (deducteeData.getAdditionalSections() != null) {
						if (!sections.containsKey(dBSection) && !deducteeData.getSection().equals(dBSection)) {
							sectionsAndNop.put(dBsectionAndNop.getKey(), dBsectionAndNop.getValue());
							String additionalSections = objectMapper.writeValueAsString(sectionsAndNop);
							deducteeData.setAdditionalSections(additionalSections);
						}
					} else if (!deducteeData.getSection().equals(dBSection)) {
						sectionsAndNop.put(dBsectionAndNop.getKey(), dBsectionAndNop.getValue());
						String additionalSections = objectMapper.writeValueAsString(sectionsAndNop);
						deducteeData.setAdditionalSections(additionalSections);
					}
				}
				if (!dbSectionsAndCode.isEmpty()) {
					for (Entry<String, String> dbSectionAndCode : dbSectionsAndCode.entrySet()) {
						String dBSectionCode = dbSectionAndCode.getKey();
						String dBSection = dbSectionAndCode.getValue();
						if (StringUtils.isBlank(deducteeData.getSection())) {
							deducteeData.setSectionCode(dBSectionCode);
						} else if (deducteeData.getAdditionalSectionCode() != null) {
							if (!sections.containsKey(dBSection) && !deducteeData.getSection().equals(dBSection)) {
								sectionsAndCode.put(dbSectionAndCode.getKey(), dbSectionAndCode.getValue());
								String additionalSectionCode = objectMapper.writeValueAsString(sectionsAndCode);
								deducteeData.setAdditionalSectionCode(additionalSectionCode);
							}
						} else if (!deducteeData.getSection().equals(dBSection)) {
							sectionsAndCode.put(dbSectionAndCode.getKey(), dbSectionAndCode.getValue());
							String additionalSectionCode = objectMapper.writeValueAsString(sectionsAndCode);
							deducteeData.setAdditionalSectionCode(additionalSectionCode);
						}
					}
				}
				if (!dbSectionsAndThresholds.isEmpty()) {
					for (Entry<String, Integer> dbSectionsAndThreshold : dbSectionsAndThresholds.entrySet()) {
						String dBSection = dbSectionsAndThreshold.getKey();
						Integer dBThreshold = dbSectionsAndThreshold.getValue();
						if (StringUtils.isBlank(deducteeData.getSection())) {
							Boolean threshold = (dBThreshold != null && dBThreshold == 1) ? true : false;
							deducteeData.setIsThresholdLimitApplicable(threshold);
						} else if (deducteeData.getAdditionalSectionThresholds() != null) {
							if (!sections.containsKey(dBSection) && !deducteeData.getSection().equals(dBSection)) {
								Integer threshold = (dbSectionsAndThreshold.getValue() != null
										&& dbSectionsAndThreshold.getValue() == 1) ? 1 : 0;
								sectionsAndThreshold.put(dbSectionsAndThreshold.getKey(), threshold);
								String additionalSectionThreshold = objectMapper
										.writeValueAsString(sectionsAndThreshold);
								deducteeData.setAdditionalSectionThresholds(additionalSectionThreshold);
							}
						} else if (!deducteeData.getSection().equals(dBSection)) {
							Integer threshold = (dbSectionsAndThreshold.getValue() != null
									&& dbSectionsAndThreshold.getValue() == 1) ? 1 : 0;
							sectionsAndThreshold.put(dbSectionsAndThreshold.getKey(), threshold);
							String additionalSectionThreshold = objectMapper.writeValueAsString(sectionsAndThreshold);
							deducteeData.setAdditionalSectionThresholds(additionalSectionThreshold);
						}
					}
				}
			}
			boolean isDuplicateRecord = false;
			// logic for if same deductee with same section and pan exists then marking as
			// duplicate.
			if (deducteeDB != null && isduplicateSections
					&& deducteeData.getDeducteeKey().equals(deducteeDB.getDeducteeKey())) {
				isDuplicateRecord = true;
			}
			if (!isDuplicateRecord) {
				if (deducteeDB != null) {
					deducteeDB.setActive(false);
					deducteeDB.setApplicableTo(subtractDay(new Date()));
					batchUpdateApplicableTo.add(deducteeDB);
				}
				if (sectionsAndNop.size() > 0) {
					deducteeData.setIsDeducteeHasAdditionalSections(true);
				}
				// get threshold ledger data based on deductee pan or deductee key
				List<DeducteeNopGroup> deducteeNopList = deducteeNopGroupDAO.findByDeducteeKeyOrDeducteePan(
						deducteeData.getDeducteeKey(), deducteeData.getDeductorPan(), deducteeData.getDeducteePAN(),
						deducteeData.getCurrentBalanceYear());
				// added three amounts
				BigDecimal amount = deducteeData.getDeducteeMasterBalancesOf194q()
						.add(deducteeData.getAdvanceBalancesOf194q()).add(deducteeData.getProvisionBalancesOf194q());
				if (deducteeData.getCurrentBalanceYear() == null) {
					deducteeData.setCurrentBalanceYear(CommonUtil.getAssessmentYear(null));
				}

				if (!deducteeNopList.isEmpty()) {
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
					for (DeducteeNopGroup nop : deducteeNopList) {
						deducteeNopGroupUpdate(nop, amount, deducteeData.getCreatedBy(), deducteeData.getDeducteePAN(),
								deducteeNopBatchUpdate, groupIdList, thresholdLimitMaster);
					}
				} else {
					deducteeNopGroupSave(null, amount, deducteeData.getCurrentBalanceYear(), thresholdLimitMaster,
							deducteeNopBatchSave, groupIdList, deducteeNopMap, deducteeData);
				}
				deducteeBatchSave.add(deducteeData);
			} else if (deducteeDB != null) {
				int deducteeId = deducteeDB.getDeducteeMasterId();
				boolean isSection = deducteeDB.getIsDeducteeHasAdditionalSections();
				BeanUtils.copyProperties(deducteeData, deducteeDB);
				deducteeDB.setDeducteeMasterId(deducteeId);
				deducteeDB.setIsDeducteeHasAdditionalSections(isSection);
				deducteeDB.setApplicableTo(deducteeData.getApplicableTo());
				deducteeDB.setModifiedBy(deducteeData.getCreatedBy());
				deducteeDB.setModifiedDate(new Timestamp(new Date().getTime()));
				deducteeBatchUpdate.add(deducteeDB);
			}
		}
		return duplicateCount;
	}

	/**
	 * 
	 * @param originalFileName
	 * @param deductorTan
	 * @param deductorPan
	 * @param errorList
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public File prepareNonResidentDeducteesErrorFile(String originalFileName, String deductorTan, String deductorPan,
			ArrayList<DeducteeMasterNonResidentialErrorReportCsvDTO> errorList) throws Exception {
		ArrayList<String> headers = (ArrayList<String>) errorReportService.getNrDeducteeHeaderFields();
		headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
		Workbook wkBook = deducteeNonResidentXlsxReport(errorList, deductorTan, deductorPan, headers);
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		wkBook.save(baout, SaveFormat.XLSX);
		File deducteeErrorsFile = new File(
				FilenameUtils.getBaseName(originalFileName) + "_" + UUID.randomUUID() + "_Error_Report.xlsx");
		FileUtils.writeByteArrayToFile(deducteeErrorsFile, baout.toByteArray());
		baout.close();
		return deducteeErrorsFile;
	}

	/**
	 * 
	 * @param deducteeNonResidentialErrorReportsCsvList
	 * @param deductorTan
	 * @param deductorPan
	 * @param headerNames
	 * @return
	 * @throws Exception
	 */
	public Workbook deducteeNonResidentXlsxReport(
			ArrayList<DeducteeMasterNonResidentialErrorReportCsvDTO> deducteeNonResidentialErrorReportsCsvList,
			String deductorTan, String deductorPan, ArrayList<String> headerNames) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForDeducteeNonResident(deducteeNonResidentialErrorReportsCsvList, worksheet, deductorTan,
				headerNames);

		// Style for A6 to D6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange(5, 0, 1, 2);
		headerColorRange1.setStyle(style1);

		Cell cellD6 = worksheet.getCells().get("C6");
		Style styleD6 = cellD6.getStyle();
		styleD6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleD6.setPattern(BackgroundType.SOLID);
		styleD6.getFont().setBold(true);
		styleD6.getFont().setColor(Color.getWhite());
		styleD6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellD6.setStyle(styleD6);

		// Style for E6 to DD6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange(5, 3, 1, headerNames.size() - 3);
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		List<DeductorMaster> response = deductorMasterDAO.findByDeductorPan(deductorPan);

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
		cellB5.setValue("Error/Information codes");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "DD6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:DD6");
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
	private void setExtractDataForDeducteeNonResident(
			ArrayList<DeducteeMasterNonResidentialErrorReportCsvDTO> errorDTOs, Worksheet worksheet, String deductorTan,
			List<String> headerNames) throws Exception {

		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			for (int i = 0; i < errorDTOs.size(); i++) {
				DeducteeMasterNonResidentialErrorReportCsvDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = new ArrayList<>();
				rowData.add(StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.add(StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.add(errorDTO.getSerialNumber());
				rowData.add(errorDTO.getSourceIdentifier());
				rowData.add(errorDTO.getSourceFileName());
				rowData.add(errorDTO.getCompanyCode());
				rowData.add(errorDTO.getDeductorName());
				rowData.add(errorDTO.getDeductorPan());
				rowData.add(errorDTO.getDeductorTan());
				rowData.add(errorDTO.getDeducteeCode());
				rowData.add(errorDTO.getDeducteeName());
				rowData.add(errorDTO.getDeducteePAN());
				rowData.add(errorDTO.getDeducteeAadharNumber());
				rowData.add(errorDTO.getDeducteeStatus());
				rowData.add(errorDTO.getDeducteeResidentialStatus());
				rowData.add(errorDTO.getEmailAddress());
				rowData.add(errorDTO.getPhoneNumber());
				rowData.add(errorDTO.getFlatDoorBlockNo());
				rowData.add(errorDTO.getNameBuildingVillage());
				rowData.add(errorDTO.getRoadStreetPostoffice());
				rowData.add(errorDTO.getAreaLocality());
				rowData.add(errorDTO.getTownCityDistrict());
				rowData.add(errorDTO.getState());
				rowData.add(errorDTO.getCountry());
				rowData.add(errorDTO.getPinCode());
				rowData.add(errorDTO.getSectionCode());
				rowData.add(errorDTO.getSection());
				rowData.add(errorDTO.getNatureOfPayment());
				rowData.add(errorDTO.getRate());
				rowData.add(errorDTO.getTdsExemptionFlag());
				rowData.add(errorDTO.getTdsExemptionReason());
				rowData.add(errorDTO.getIsThresholdLimitApplicable());
				rowData.add(errorDTO.getApplicableFrom());
				rowData.add(errorDTO.getApplicableTo());
				rowData.add(errorDTO.getTdsExemptionNumber());
				rowData.add(errorDTO.getDeducteeGSTIN());
				rowData.add(errorDTO.getGrOrIRIndicator());
				rowData.add(errorDTO.getTdsApplicabilityUnderSection());
				rowData.add(errorDTO.getDeducteeMasterBalancesOf194q());
				rowData.add(errorDTO.getAdvanceBalancesOf194q());
				rowData.add(errorDTO.getProvisionBalancesOf194q());
				rowData.add(errorDTO.getOpeningBalanceCreditNote());
				rowData.add(errorDTO.getCurrentBalanceMonth());
				rowData.add(errorDTO.getCurrentBalanceYear());
				rowData.add(errorDTO.getAdvancesAsOfMarch());
				rowData.add(errorDTO.getProvisionsAsOfMarch());
				rowData.add(errorDTO.getPreviousBalanceMonth());
				rowData.add(errorDTO.getPreviousBalanceYear());
				rowData.add(errorDTO.getDeducteeTan());
				rowData.add(errorDTO.getDeducteeTin());
				rowData.add(errorDTO.getTdsSectionDescription());
				rowData.add(errorDTO.getIsGrossingUp());
				rowData.add(errorDTO.getUserDefinedField1());
				rowData.add(errorDTO.getUserDefinedField2());
				rowData.add(errorDTO.getUserDefinedField3());
				rowData.add(errorDTO.getNatureOfRemittance());
				rowData.add(errorDTO.getArticleNumberDtaa());
				rowData.add(errorDTO.getSectionOfIncometaxAct());
				rowData.add(errorDTO.getIsTRCAvailable());
				rowData.add(errorDTO.getIstrcFuture());
				rowData.add(errorDTO.getTrcFutureDate());
				rowData.add(errorDTO.getTrcApplicableFrom());
				rowData.add(errorDTO.getTrcApplicableTo());
				rowData.add(errorDTO.getIsTenFAvailable());
				rowData.add(errorDTO.getIstenfFuture());
				rowData.add(errorDTO.getTenfFutureDate());
				rowData.add(errorDTO.getTenFApplicableFrom());
				rowData.add(errorDTO.getTenFApplicableTo());
				rowData.add(errorDTO.getWhetherPEInIndia());
				rowData.add(errorDTO.getWhetherPEInIndiaApplicableFrom());
				rowData.add(errorDTO.getWhetherPEInIndiaApplicableTo());
				rowData.add(errorDTO.getNoPEDocumentAvailable());
				rowData.add(errorDTO.getNoPEDeclarationAvailableInFuture());
				rowData.add(errorDTO.getNoPEDeclarationAvailableFutureDate());
				rowData.add(errorDTO.getNoPEApplicableFrom());
				rowData.add(errorDTO.getNoPEApplicableTo());
				rowData.add(errorDTO.getIsBusinessCarriedInIndia());
				rowData.add(errorDTO.getIsPEinvoilvedInPurchaseGoods());
				rowData.add(errorDTO.getIsPEamountReceived());
				rowData.add(errorDTO.getIsPOEMavailable());
				rowData.add(errorDTO.getPoemApplicableFrom());
				rowData.add(errorDTO.getPoemApplicableTo());
				rowData.add(errorDTO.getIsNoPOEMDeclarationAvailable());
				rowData.add(errorDTO.getIsPoemDeclaration());
				rowData.add(errorDTO.getPoemFutureDate());
				rowData.add(errorDTO.getNoPOEMDeclarationApplicableFromDate());
				rowData.add(errorDTO.getNoPOEMDeclarationApplicableToDate());
				rowData.add(errorDTO.getIsFixedbaseAvailbleIndia());
				rowData.add(errorDTO.getFixedbaseAvailbleIndiaApplicableFrom());
				rowData.add(errorDTO.getFixedbaseAvailbleIndiaApplicableTo());
				rowData.add(errorDTO.getIsNoFixedBaseDeclarationAvailable());
				rowData.add(errorDTO.getNoFixedBaseDeclarationAvailableInFuture());
				rowData.add(errorDTO.getNoFixedBaseDeclarationAvailableFutureDate());
				rowData.add(errorDTO.getNoFixedBaseDeclarationFromDate());
				rowData.add(errorDTO.getNoFixedBaseDeclarationToDate());
				rowData.add(errorDTO.getIsAmountConnectedFixedBase());
				rowData.add(errorDTO.getStayPeriodFinancialYear());
				rowData.add(errorDTO.getBeneficialOwnerOfIncome());
				rowData.add(errorDTO.getIsBeneficialOwnershipOfDeclaration());
				rowData.add(errorDTO.getMliPptConditionSatisifed());
				rowData.add(errorDTO.getMliSlobConditionSatisifed());
				rowData.add(errorDTO.getIsMliPptSlob());
				rowData.add(errorDTO.getRelatedParty());
				rowData.add(errorDTO.getIsDeducteeTransparent());
				rowData.add(errorDTO.getAggreementForTransaction());
				rowData.add(errorDTO.getPrinciplesOfBusinessPlace());
				rowData.add(errorDTO.getCountryToRemittance());
				rowData.add(errorDTO.getCharteredAccountantNo());

				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				try {
					worksheet.autoFitColumn(i);
				} catch (Exception e) {
					// kill the exception
				}
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
			}
		}
	}

	@Async
	public void generateDeducteeNrStaggingFile(String tan, String pan, String tenantId, BatchUpload batchUpload)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = resourceLoader
				.getResource("classpath:templates/" + "deductee_non_residential_upload_template.xlsx");
		InputStream input = resource.getInputStream();
		try (SXSSFWorkbook wb = new SXSSFWorkbook(new XSSFWorkbook(input))) {
			SXSSFSheet sheet = wb.getSheetAt(0);
			List<DeducteeMasterResidential> deductees = deducteeMasterNonResidentialDAO
					.generateDeducteeNrStaggingFile(pan);
			int rowindex = 1;

			XSSFCellStyle style1 = (XSSFCellStyle) wb.createCellStyle();
			style1.setWrapText(true);
			XSSFFont fonts = (XSSFFont) wb.createFont();
			fonts.setBold(false);
			style1.setFont(fonts);
			for (DeducteeMasterResidential deductee : deductees) {
				SXSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));

				createSXSSFCell(style1, row1, 0, deductee.getSourceIdentifier());
				createSXSSFCell(style1, row1, 1, deductee.getSourceFileName());
				createSXSSFCell(style1, row1, 2, deductee.getDeductorCode());
				createSXSSFCell(style1, row1, 3, deductee.getDeductorName());
				createSXSSFCell(style1, row1, 4, deductee.getDeductorPan());
				createSXSSFCell(style1, row1, 5, "");
				createSXSSFCell(style1, row1, 6, deductee.getDeducteeResidentialStatus());
				createSXSSFCell(style1, row1, 7, deductee.getDeducteeCode());
				createSXSSFCell(style1, row1, 8, deductee.getDeducteeName());
				createSXSSFCell(style1, row1, 9, deductee.getDeducteePAN());
				createSXSSFCell(style1, row1, 10, deductee.getDeducteeAadharNumber());
				createSXSSFCell(style1, row1, 11, deductee.getTinUniqueIdentification());
				createSXSSFCell(style1, row1, 12, deductee.getDeducteeStatus());
				createSXSSFCell(style1, row1, 13, "");
				createSXSSFCell(style1, row1, 14, "");
				createSXSSFCell(style1, row1, 15, "");
				createSXSSFCell(style1, row1, 16, "");
				createSXSSFCell(style1, row1, 17, "");
				createSXSSFCell(style1, row1, 18, "");
				createSXSSFCell(style1, row1, 19, "");
				createSXSSFCell(style1, row1, 20, "");
				createSXSSFCell(style1, row1, 21, "");
				createSXSSFCell(style1, row1, 22, "");
				createSXSSFCell(style1, row1, 23, "");
				createSXSSFCell(style1, row1, 24, "");
				createSXSSFCell(style1, row1, 25, "");
				createSXSSFCell(style1, row1, 26, "");
				createSXSSFCell(style1, row1, 27, "");
				createSXSSFCell(style1, row1, 28, "");
				createSXSSFCell(style1, row1, 29, "");
				createSXSSFCell(style1, row1, 30, "");
				createSXSSFCell(style1, row1, 31, "");
				createSXSSFCell(style1, row1, 32, "");
				createSXSSFCell(style1, row1, 33, "");
				createSXSSFCell(style1, row1, 34, "");
				createSXSSFCell(style1, row1, 35, "");
				createSXSSFCell(style1, row1, 36, "");
				createSXSSFCell(style1, row1, 37, "");
				createSXSSFCell(style1, row1, 38, "");
				createSXSSFCell(style1, row1, 39, "");
				createSXSSFCell(style1, row1, 40, "");
				createSXSSFCell(style1, row1, 41, "");
				createSXSSFCell(style1, row1, 42, "");
				createSXSSFCell(style1, row1, 43, "");
				createSXSSFCell(style1, row1, 44, "");
				createSXSSFCell(style1, row1, 45, "");
				createSXSSFCell(style1, row1, 46, deductee.getEmailAddress());
				createSXSSFCell(style1, row1, 47, deductee.getPhoneNumber());
				createSXSSFCell(style1, row1, 48, deductee.getFlatDoorBlockNo());
				createSXSSFCell(style1, row1, 49, deductee.getNameBuildingVillage());
				createSXSSFCell(style1, row1, 50, deductee.getRoadStreetPostoffice());
				createSXSSFCell(style1, row1, 51, deductee.getAreaLocality());
				createSXSSFCell(style1, row1, 52, deductee.getTownCityDistrict());
				createSXSSFCell(style1, row1, 53, deductee.getState());
				createSXSSFCell(style1, row1, 54, deductee.getCountry());
				createSXSSFCell(style1, row1, 55, deductee.getPinCode());
				createSXSSFCell(style1, row1, 56, deductee.getSection());
				createSXSSFCell(style1, row1, 57, deductee.getNatureOfPayment());
				createSXSSFCell(style1, row1, 58, getFormattedValue(deductee.getRate()));
				createSXSSFCell(style1, row1, 59, deductee.getSectionCode());
				createSXSSFCell(style1, row1, 60, "");
				String applicableFrom = StringUtils.EMPTY;
				if (deductee.getApplicableFrom() != null) {
					applicableFrom = new SimpleDateFormat("dd-MM-yyyy").format(deductee.getApplicableFrom());
				}
				createSXSSFCell(style1, row1, 61, applicableFrom);
				String applicableTo = StringUtils.EMPTY;
				if (deductee.getApplicableTo() != null) {
					applicableTo = new SimpleDateFormat("dd-MM-yyyy").format(deductee.getApplicableTo());
				}
				createSXSSFCell(style1, row1, 62, applicableTo);
				createSXSSFCell(style1, row1, 63, deductee.getUserDefinedField1());
				createSXSSFCell(style1, row1, 64, deductee.getUserDefinedField2());
				createSXSSFCell(style1, row1, 65, deductee.getUserDefinedField3());
				createSXSSFCell(style1, row1, 66, "");
				createSXSSFCell(style1, row1, 67, "");
				createSXSSFCell(style1, row1, 68, "");
				createSXSSFCell(style1, row1, 69, "");
				createSXSSFCell(style1, row1, 70, "");
				createSXSSFCell(style1, row1, 71, "");
				createSXSSFCell(style1, row1, 72, "");
				createSXSSFCell(style1, row1, 73, "");
				createSXSSFCell(style1, row1, 74, "");
				createSXSSFCell(style1, row1, 75, "");
				createSXSSFCell(style1, row1, 76, "");
				createSXSSFCell(style1, row1, 77, "");
				createSXSSFCell(style1, row1, 78, "");
				createSXSSFCell(style1, row1, 79, "");
				createSXSSFCell(style1, row1, 80, "");
				createSXSSFCell(style1, row1, 81, "");
				createSXSSFCell(style1, row1, 82, "");
				createSXSSFCell(style1, row1, 83, "");
				createSXSSFCell(style1, row1, 84, "");
				createSXSSFCell(style1, row1, 85, "");
				createSXSSFCell(style1, row1, 86, "");
				createSXSSFCell(style1, row1, 87, "");
				createSXSSFCell(style1, row1, 88, "");
				createSXSSFCell(style1, row1, 89, "");
				createSXSSFCell(style1, row1, 90, "");
				createSXSSFCell(style1, row1, 91, "");
				createSXSSFCell(style1, row1, 92, "");
				createSXSSFCell(style1, row1, 93, "");
				createSXSSFCell(style1, row1, 94, "");
			}
			wb.write(out);
			batchUpload.setProcessedCount(deductees.size());
			batchUpload.setRowsCount(Long.valueOf(deductees.size()));
		}
		String filePath = sendFileToBlobStorage(out, tenantId);
		batchUpload.setFilePath(filePath);
		batchUpload.setStatus("Processed");
		batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
		batchUploadDAO.update(batchUpload);
	}

	private void createSXSSFCell(XSSFCellStyle style, SXSSFRow row, int cellNumber, String value) {
		row.createCell(cellNumber).setCellValue(StringUtils.isBlank(value) ? StringUtils.EMPTY : value);
		row.getCell(cellNumber).setCellStyle(style); // K
	}

	private String getFormattedValue(BigDecimal value) {
		return value != null && value.compareTo(BigDecimal.ZERO) > 0 ? value.toString() : StringUtils.EMPTY;
	}

	private String sendFileToBlobStorage(ByteArrayOutputStream out, String tenantId)
			throws IOException, URISyntaxException, InvalidKeyException, StorageException {
		File file = getConvertedExcelFile(out);
		logger.info("Original file   : {}", file.getName());
		String paths = blob.uploadExcelToBlobWithFile(file, tenantId);
		return paths;
	}

	private File getConvertedExcelFile(ByteArrayOutputStream out) throws FileNotFoundException, IOException {
		byte[] bytes = out.toByteArray();
		File someFile = new File("Deductee_NR_Stagging_Report.xlsx");
		try (FileOutputStream fos = new FileOutputStream(someFile)) {
			fos.write(bytes);
			fos.flush();
			fos.close();
			return someFile;
		}
	}
}
