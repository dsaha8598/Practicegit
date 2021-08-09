package com.ey.in.tds.ingestion.service.invoicelineitem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.NrTransactionsMetaDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceStagging;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceStagging;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.dto.SectionDeterminationDTO;
import com.ey.in.tds.common.dto.SurchargeAndCessDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterNonResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorOnboardingInformationDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorTanAddress;
import com.ey.in.tds.common.onboarding.jdbc.dto.NrTransactionsMeta;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.service.ErrorReportService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.dto.invoice.NrExcelErrorDTO;
import com.ey.in.tds.ingestion.jdbc.dao.AdvanceDAO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceLineItemDAO;
import com.ey.in.tds.ingestion.jdbc.dao.ProvisionDAO;
import com.microsoft.azure.storage.StorageException;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

@Service
public class NonResidentService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private InvoiceLineItemDAO invoiceLineItemDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private AdvanceDAO advanceDAO;

	@Autowired
	private ProvisionDAO provisionDAO;

	@Autowired
	private NrTransactionsMetaDAO nrTransactionsMetaDAO;

	@Value("${application.url}")
	private String applicationUrl;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private ErrorReportService errorReportService;
	
	
	@Async
	public BatchUpload asyncProcessLineItems(String tenantId, String deductorPan, BatchUpload batchUpload, String token)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return processLineItems(tenantId, deductorPan, batchUpload, token);
	}

	/**
	 * 
	 * @param tenantId
	 * @param deductorPan
	 * @param batchUpload
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public BatchUpload processLineItems(String tenantId, String deductorPan, BatchUpload batchUpload, String token)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		String deductorTan = batchUpload.getDeductorMasterTan();
		File csvFile = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getOtherFileUrl());
		CsvReader csvReader = new CsvReader();
		csvReader.setContainsHeader(true);
		CsvContainer csv = csvReader.read(csvFile, StandardCharsets.UTF_8);
		int errorCount = 0;
		int duplicateCount = 0;
		int processedRecordsCount = 0;
		File errorFile = null;
		ArrayList<NrExcelErrorDTO> errorList = new ArrayList<>();
		Set<String> invoiceSet = new HashSet<>();
		// Deductor tan details
		ResponseEntity<ApiStatus<List<DeductorTanAddress>>> deducteeTanDetails = onboardingClient
				.getDeductorBasedOnTan(batchUpload.getDeductorMasterTan(), tenantId);
		List<DeductorTanAddress> deductorTanAddressResponse = deducteeTanDetails.getBody().getData();
		// Deductor onboarding info
		DeductorOnboardingInformationDTO deductorOnboardingInfo = onboardingClient
				.getDeductorOnboardingInfo(tenantId, deductorPan).getBody().getData().get(0);
		// Deductee status data
		Map<String, String> deducteeStatusMap = mastersClient.getAllDeducteeStatus().getBody();
		List<InvoiceLineItem> batchInvoiceSave = new ArrayList<>();
		List<AdvanceDTO> batchAdvanceSave = new ArrayList<>();
		List<ProvisionDTO> batchProvisionSave = new ArrayList<>();
		List<NrTransactionsMeta> batchNrMetaSave = new ArrayList<>();
		List<DeducteeMasterNonResidential> nrDeducteeBatchUpdate = new ArrayList<>();
		// Nature of payment and section data
		List<Map<String, Object>> nopMap = mastersClient.getSectionAndDeducteeStatusBasedOnStatus("NR").getBody()
				.getData();
		Map<String, List<Map<String, Object>>> sectionMap = new HashMap<>();
		for (Map<String, Object> map : nopMap) {
			String deducteeStatus = (String) map.get("status");
			String section = (String) map.get("section");
			Integer nopId = (Integer) map.get("natureOfPaymentId");
			String key = section + "-" + nopId + "-" + deducteeStatus;
			if (!sectionMap.containsKey(key)) {
				sectionMap.put(key, new ArrayList<>());
			}
			sectionMap.get(key).add(map);
		}
		// Currency converter data
		List<Map<String, Object>> currencyData = mastersClient.getAllCurrencyData().getBody().getData();
		Map<String, Double> currencyMap = new HashMap<>();
		for (Map<String, Object> map : currencyData) {
			String currencyName = (String) map.get("currencyName");
			Double buyingTT = (Double) map.get("buyingTT");
			if (StringUtils.isNotBlank(currencyName)) {
				currencyMap.put(currencyName, buyingTT);
			}
		}
		// Due date tracker data
		List<Map<String, Object>> tdsMonthlyTrackerData = mastersClient.getAllTdsMonthTrackerData().getBody().getData();
		Map<String, Date> monthClosureMap = new HashMap<>();
		Map<String, Integer> dueDatePaymentMap = new HashMap<>();
		for (Map<String, Object> map : tdsMonthlyTrackerData) {
			String monthClosure = (String) map.get("monthClosure");
			Integer year = (Integer) map.get("year");
			Integer month = (Integer) map.get("month");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String challanPayment = (String) map.get("challanPayment");
			Date dueDate = formatter.parse(challanPayment);
			String key = year + "-" + month;
			dueDatePaymentMap.put(monthClosure, month);
			monthClosureMap.put(key, dueDate);
		}

		// Cess details
		ResponseEntity<ApiStatus<List<SurchargeAndCessDTO>>> cessMasterDetails = mastersClient
				.getCessDetailsByCessType("Health and Education Cess");
		List<SurchargeAndCessDTO> cessMasterList = cessMasterDetails.getBody().getData();
		// Surcharge details
		List<Map<String, Object>> surchargeList = mastersClient.getAllSurchargeDetails("NR").getBody().getData();
		Map<String, List<Map<String, Object>>> surchargeMap = new HashMap<>();
		for (Map<String, Object> surcharge : surchargeList) {
			String deducteeStatus = (String) surcharge.get("status");
			String section = (String) surcharge.get("section");
			String key = section + "-" + deducteeStatus;
			if (!surchargeMap.containsKey(key)) {
				surchargeMap.put(key, new ArrayList<>());
			}
			surchargeMap.get(key).add(surcharge);
		}
		// Article master details
		List<Map<String, Object>> articleMasterList = mastersClient.getArticleMasterData().getBody().getData();
		Map<String, List<Map<String, Object>>> natureOfRemittanceMap = new HashMap<>();
		Map<String, List<Map<String, Object>>> articleNumberMap = new HashMap<>();
		for (Map<String, Object> articleMaster : articleMasterList) {
			String country = (String) articleMaster.get("country");
			String natureOfRemittance = (String) articleMaster.get("natureOfRemittance");
			String articleNumber = (String) articleMaster.get("number");
			String natureOfRemittanceKey = country.toLowerCase() + "-" + natureOfRemittance.toLowerCase();
			if (!natureOfRemittanceMap.containsKey(natureOfRemittanceKey)) {
				natureOfRemittanceMap.put(natureOfRemittanceKey, new ArrayList<>());
			}
			natureOfRemittanceMap.get(natureOfRemittanceKey).add(articleMaster);
			String articleNokey = articleNumber.toLowerCase() + "-" + country.toLowerCase();
			if (!articleNumberMap.containsKey(articleNokey)) {
				articleNumberMap.put(articleNokey, new ArrayList<>());
			}
			articleNumberMap.get(articleNokey).add(articleMaster);
		}
		// Deductee details
		Map<String, List<DeducteeMasterNonResidential>> deducteeNRMap = getDeducteeNonResidentRecords(deductorPan,
				tenantId);
		int serialNumber = 1;
		int totalRecords = 0;
		LongAdder count = new LongAdder();
		for (CsvRow row : csv.getRows()) {
			if (StringUtils.isNotBlank(StringUtils.join(row.getFields(), ", ").replace(",", ""))) {
				++totalRecords;
				BigDecimal dtaaRate = BigDecimal.ZERO;
				BigDecimal tdsRate = BigDecimal.ZERO;
				BigDecimal rate = BigDecimal.ZERO;
				BigDecimal surchargeAmount = BigDecimal.ZERO;
				BigDecimal cessRate = BigDecimal.ZERO;
				BigDecimal cessAmount = BigDecimal.ZERO;
				try {
					NrExcelErrorDTO nrErrorDTO = new NrExcelErrorDTO();
					if (StringUtils.isBlank(nrErrorDTO.getReason())) {
						nrErrorDTO.setReason("");
					}
					NrTransactionsMeta nrTransactionsMetaData = getNRTransactionsCsvMapping(row, nrErrorDTO,
							batchUpload.getUploadType());
					int challanMonth = 0;
					// validating invoice record

					if (StringUtils.isBlank(nrErrorDTO.getReason())) {
						// set deductee key
						String deducteeKey = getDeducteeKey(nrTransactionsMetaData.getDeducteeCode(),
								nrTransactionsMetaData.getDeducteeName(), nrTransactionsMetaData.getDeducteePan());
						nrTransactionsMetaData.setDeducteeKey(deducteeKey);
						// data validation
						validatingInvoiceRecord(nrTransactionsMetaData, nrErrorDTO, tenantId,
								deductorTanAddressResponse, deducteeStatusMap);
					}
					nrTransactionsMetaData.setChallanPaid(isNull(nrTransactionsMetaData.getChallanPaid()));
					if (StringUtils.isBlank(nrErrorDTO.getReason())) {
						// challan month calculation
						challanMonth = challanMonthCalculation(nrTransactionsMetaData.getDocumentPostingDate(),
								nrTransactionsMetaData.getChallanGeneratedDate(),
								nrTransactionsMetaData.getChallanPaid(), monthClosureMap, dueDatePaymentMap);
						if (challanMonth == 0) {
							nrErrorDTO.setReason("Incorrect ChallanPaidDate. Pl refer Due date tracker." + "\n");
						}
					}

					if (StringUtils.isBlank(nrErrorDTO.getReason())) {
						String nrDataStr = getDuplicateLogicFields(nrTransactionsMetaData, batchUpload.getUploadType());
						if (!invoiceSet.contains(nrDataStr)) {
							invoiceSet.add(nrDataStr);
							nrTransactionsMetaData.setChallanPaid(isNull(nrTransactionsMetaData.getChallanPaid()));
							// amount in Inr amount calculation
							if (nrTransactionsMetaData.getAmountInInr().compareTo(BigDecimal.ZERO) == 0
									&& StringUtils.isNotBlank(nrTransactionsMetaData.getCurrency())) {
								BigDecimal converstionAmount = getCurrencyAmountCalculation(currencyMap,
										nrTransactionsMetaData.getCurrency(),
										nrTransactionsMetaData.getAmountInForeignCurrency());
								nrTransactionsMetaData.setAmountInInr(converstionAmount);
							}

							// setting hours, minutes, seconds to 00:00:00
							if (nrTransactionsMetaData.getVendorDocumentDate() != null) {
								nrTransactionsMetaData.setVendorDocumentDate(
										CommonUtil.setDateTime(nrTransactionsMetaData.getVendorDocumentDate()));
							}
							if (nrTransactionsMetaData.getPoDate() != null) {
								nrTransactionsMetaData
										.setPoDate(CommonUtil.setDateTime(nrTransactionsMetaData.getPoDate()));
							}
							Date postingDate = CommonUtil.setDateTime(nrTransactionsMetaData.getDocumentPostingDate());
							nrTransactionsMetaData.setAssessmentYear(batchUpload.getAssessmentYear());
							nrTransactionsMetaData.setDocumentPostingDate(postingDate);
							nrTransactionsMetaData.setHasDtaa(false);

							// deductee information
							List<DeducteeMasterNonResidential> nrDeducteeList = deducteeNRMap
									.get(nrTransactionsMetaData.getDeducteeKey()) != null
											? deducteeNRMap.get(nrTransactionsMetaData.getDeducteeKey())
											: new ArrayList<>();
							DeducteeMasterNonResidential deducteeMasterNonResidential = null;
							if (nrDeducteeList != null && !nrDeducteeList.isEmpty()) {
								deducteeMasterNonResidential = nrDeducteeList.get(0);
							}

							// update deductee master details
							nrTransactionsMetaData = updateDeducteeDetails(deducteeMasterNonResidential,
									nrTransactionsMetaData, nrDeducteeBatchUpdate);

							String derivedSection = nrTransactionsMetaData.getTdsSection();
							SectionDeterminationDTO sectionDeterminationDTO = new SectionDeterminationDTO();
							if (!"CAN".equalsIgnoreCase(nrTransactionsMetaData.getSupplyType())) {
								// section determination api request
								sectionDeterminationDTO = sectionDeterminationAPI(nrTransactionsMetaData, token,
										deductorTan, deductorPan, tenantId);
								derivedSection = sectionDeterminationDTO.getSection();
								int nop = sectionDeterminationDTO.getNature();
								logger.info("derived section : {}", derivedSection);
								logger.info("nop id : {}", sectionDeterminationDTO.getNature());

								// Null check handling
								setNrTransactionsMetaData(nrTransactionsMetaData);

								// Derived rate calculation
								// TDS rate
								Boolean isValid = rateMasterValidation(nrTransactionsMetaData,
										deducteeMasterNonResidential);
								if (deducteeMasterNonResidential != null && isValid) {
									String rateMasterKey = derivedSection + "-" + nop + "-"
											+ deducteeMasterNonResidential.getDeducteeStatus();
									if (sectionMap.get(rateMasterKey) != null) {
										List<Map<String, Object>> sectionList = sectionMap.get(rateMasterKey);
										BigDecimal tdsMasterRate = getTdsMasterDetails(sectionList,
												nrTransactionsMetaData.getDocumentPostingDate(),
												nrTransactionsMetaData);
										tdsRate = tdsMasterRate;
									}
									rate = tdsRate;
									// Cess and surcharge calculation
									BigDecimal actTdsAmount = rate.multiply(nrTransactionsMetaData.getAmountInInr())
											.divide(BigDecimal.valueOf(100));
									surchargeAmount = surchargeCalculation(derivedSection, actTdsAmount,
											nrTransactionsMetaData.getAmountInInr(),
											nrTransactionsMetaData.getDeducteeStatus(), surchargeMap);
									cessRate = cessCalculation(actTdsAmount, cessMasterList);
									cessAmount = (actTdsAmount.add(surchargeAmount).multiply(cessRate))
											.divide(new BigDecimal(100));
									/*
									 * BigDecimal derivedEffectiveAmount = actTdsAmount.add(surchargeAmount)
									 * .add(cessAmount); rate = (derivedEffectiveAmount.multiply(new
									 * BigDecimal(100))) .divide(nrTransactionsMetaData.getAmountInInr());
									 */
									logger.info("tds rate : {}", tdsRate);

								} else {
									tdsRate = new BigDecimal(20);
									logger.info("tds rate : {}", tdsRate);
									rate = tdsRate;
								}
								// No need to consider treaty rate if nature of remittance is Business Income
								if (!"Business Income".equalsIgnoreCase(nrTransactionsMetaData.getNatureOfRemittance())
										&& !nrTransactionsMetaData.getIsPoemOfDeductee()
										&& !nrTransactionsMetaData.getIsPeIndia()) {

									boolean isTreaty = treatyMasterValidations(nrTransactionsMetaData);
									logger.info(
											"erp document no : {} vendor document no : {} and treaty vaidations :{}",
											nrTransactionsMetaData.getErpDocumentNo(),
											nrTransactionsMetaData.getVendorDocumentNo(), isTreaty);
									// DTAA rate
									if (isTreaty) {
										String key;
										if (StringUtils.isNotBlank(nrTransactionsMetaData.getArticleOfDtaa())) {
											key = nrTransactionsMetaData.getArticleOfDtaa().toLowerCase() + "-"
													+ nrTransactionsMetaData.getCountry().toLowerCase();
										} else {
											key = nrTransactionsMetaData.getCountry().toLowerCase() + "-"
													+ nrTransactionsMetaData.getNatureOfRemittance().toLowerCase();
										}
										Double articleRate = null;
										if (articleNumberMap.get(key) != null) {
											articleRate = (Double) articleNumberMap.get(key).get(0).get("rate");
										} else if (natureOfRemittanceMap.get(key) != null) {
											articleRate = (Double) natureOfRemittanceMap.get(key).get(0).get("rate");
										}
										if (articleRate != null) {
											dtaaRate = BigDecimal.valueOf(articleRate);
											logger.info("dtaa rate : {}", dtaaRate);
											rate = rate.min(dtaaRate);
											if (rate.compareTo(dtaaRate) == 0) {
												nrTransactionsMetaData.setHasDtaa(true);
											}
										} else {
											rate = BigDecimal.ZERO;
										}
									}
								}
								logger.info("derived tds rate : {}", rate);
							}
							// save invoice meta data
							nrTransactionsMetaData.setBatchUploadId(batchUpload.getBatchUploadID());
							batchNrMetaSave.add(nrTransactionsMetaData);
							if ("INVOICE_NR_EXCEL".equalsIgnoreCase(batchUpload.getUploadType())) {
								// save invoice record
								createInvoiceLineItem(batchUpload, rate, derivedSection, nrTransactionsMetaData,
										sectionDeterminationDTO, challanMonth, batchInvoiceSave,
										deductorOnboardingInfo.getRoundoff(), count, surchargeAmount, cessRate,
										cessAmount);
							} else if ("PROVISION_NR_EXCEL".equalsIgnoreCase(batchUpload.getUploadType())) {
								createProvision(batchUpload, rate, derivedSection, nrTransactionsMetaData,
										sectionDeterminationDTO, challanMonth, batchProvisionSave,
										deductorOnboardingInfo.getRoundoff(), count, surchargeAmount, cessRate,
										cessAmount);
							} else if ("ADVANCE_NR_EXCEL".equalsIgnoreCase(batchUpload.getUploadType())) {
								createAdvance(batchUpload, rate, derivedSection, nrTransactionsMetaData,
										sectionDeterminationDTO, challanMonth, batchAdvanceSave,
										deductorOnboardingInfo.getRoundoff(), count, surchargeAmount, cessRate,
										cessAmount);
							}
						} else {
							++duplicateCount;
						}
					} else {
						++errorCount;
						nrErrorDTO.setSerialNumber(String.valueOf(serialNumber));
						nrErrorDTO = getNRTransactionsErrorCsvMapping(row, nrErrorDTO);
						errorList.add(nrErrorDTO);
					}
				} catch (Exception e) {
					logger.error("Unable to process row number {} due to {}", serialNumber, e.getMessage());
					++errorCount;
				}
				serialNumber++;
			}
		}
		// batch nr meta save
		if (!batchNrMetaSave.isEmpty()) {
			nrTransactionsMetaDAO.nrMetaBatchSave(batchNrMetaSave);
		}
		// nr deductee batch update
		if (!nrDeducteeBatchUpdate.isEmpty()) {
			invoiceLineItemDAO.nrDeducteeBatchUpdate(nrDeducteeBatchUpdate);
		}
		// batch invoice save
		if (!batchInvoiceSave.isEmpty()) {
			invoiceLineItemDAO.invoiceBatchSave(batchInvoiceSave);
			// update ancestor id
			invoiceLineItemDAO.updateInvoiceAncestorId(batchUpload.getBatchUploadID(), deductorTan);
			// update nr transactionsmeta id
			invoiceLineItemDAO.updateInvoiceMetaNrId(batchUpload.getBatchUploadID(), deductorTan);
			// Ldc rate mapping SP
			invoiceLineItemDAO.USPTDSLDCInvoiceRateMappingNR(batchUpload.getBatchUploadID());
		}
		// batch advance save
		if (!batchAdvanceSave.isEmpty()) {
			advanceDAO.advanceBatchSave(batchAdvanceSave);
			// update ancestor id
			advanceDAO.updateAdvanceAncestorId(batchUpload.getBatchUploadID(), deductorTan);
			// update nr transactionsmeta id
			advanceDAO.updateAdvanceMetaNrId(batchUpload.getBatchUploadID(), deductorTan);
			// Ldc rate mapping SP
			invoiceLineItemDAO.USPTDSLDCAdvanceRateMappingNR(batchUpload.getBatchUploadID());
		}
		// batch provision save
		if (!batchProvisionSave.isEmpty()) {
			provisionDAO.provisionBatchSave(batchProvisionSave);
			// update ancestor id
			provisionDAO.updateProvisionAncestorId(batchUpload.getBatchUploadID(), deductorTan);
			// update nr transactionsmeta id
			provisionDAO.updateProvisionMetaNrId(batchUpload.getBatchUploadID(), deductorTan);
			// Ldc rate mapping SP
			invoiceLineItemDAO.USPTDSLDCProvisionRateMappingNR(batchUpload.getBatchUploadID());
		}

		if (!errorList.isEmpty()) {
			errorFile = prepareNonResidentErrorFile(batchUpload.getFileName(), deductorTan, deductorPan, errorList,
					batchUpload.getUploadType());
		}
		if (errorFile != null) {
			String errorFilePath = blob.uploadExcelToBlobWithFile(errorFile, tenantId);
			batchUpload.setErrorFilePath(errorFilePath);
		}
		// Total processed records
		processedRecordsCount += totalRecords - errorCount - duplicateCount;
		batchUpload.setRowsCount(Long.valueOf(totalRecords));
		batchUpload.setSuccessCount(Long.valueOf(totalRecords));
		batchUpload.setFailedCount((long) errorCount);
		batchUpload.setProcessedCount(processedRecordsCount);
		batchUpload.setDuplicateCount((long) duplicateCount);
		batchUpload.setStatus("Processed");
		batchUpload.setMismatchCount(count.longValue());
		batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
		batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
		batchUpload.setModifiedBy(batchUpload.getCreatedBy());
		batchUploadDAO.update(batchUpload);

		return batchUpload;
	}

	private String getDuplicateLogicFields(NrTransactionsMeta nrTransactionsMetaData, String uploadType) {
		String nrDataStr = StringUtils.EMPTY;
		Date date = nrTransactionsMetaData.getDocumentPostingDate();
		String documentPostingDate = new SimpleDateFormat("yyy-MM-dd").format(date);
		if ("INVOICE_NR_EXCEL".equalsIgnoreCase(uploadType)) {
			nrDataStr = nrTransactionsMetaData.getVendorDocumentNo() + "-" + nrTransactionsMetaData.getErpDocumentNo()
					+ "-" + nrTransactionsMetaData.getDeductorMasterTan() + "-"
					+ nrTransactionsMetaData.getLineItemNumber() + "-" + documentPostingDate + "-"
					+ nrTransactionsMetaData.getDocumentType() + "-" + nrTransactionsMetaData.getSupplyType();
		} else if ("PROVISION_NR_EXCEL".equalsIgnoreCase(uploadType)
				|| "ADVANCE_NR_EXCEL".equalsIgnoreCase(uploadType)) {
			nrDataStr = nrTransactionsMetaData.getErpDocumentNo() + "-" + nrTransactionsMetaData.getDeductorMasterTan()
					+ "-" + nrTransactionsMetaData.getLineItemNumber() + "-" + documentPostingDate + "-"
					+ nrTransactionsMetaData.getDocumentType() + "-" + nrTransactionsMetaData.getSupplyType();
		}
		return nrDataStr;
	}

	/**
	 * 
	 * @param row
	 * @param errorDTO
	 * @return
	 */
	private NrTransactionsMeta getNRTransactionsCsvMapping(CsvRow row, NrExcelErrorDTO errorDTO, String uploadType) {
		NrTransactionsMeta nrTransactionsMeta = new NrTransactionsMeta();

		nrTransactionsMeta.setSourceIdentifier(row.getField("SourceIdentifier"));
		nrTransactionsMeta.setSourceFilename(row.getField("SourceFileName"));
		nrTransactionsMeta.setDeductorCode(row.getField("DeductorCode"));
		nrTransactionsMeta.setDeductorName(row.getField("DeductorName"));
		if (StringUtils.isNotBlank(row.getField("DeductorPAN"))) {
			nrTransactionsMeta.setDeductorPan(row.getField("DeductorPAN"));
		} else {
			errorDTO.setReason(errorDTO.getReason() + "DeductorPAN is mandatory." + "\n");
		}
		if (StringUtils.isNotBlank(row.getField("DeductorTAN"))) {
			nrTransactionsMeta.setDeductorMasterTan(row.getField("DeductorTAN"));
		} else {
			errorDTO.setReason(errorDTO.getReason() + "DeductorTAN is mandatory." + "\n");
		}
		nrTransactionsMeta.setDeductorGstin(row.getField("DeductorGSTIN"));
		nrTransactionsMeta.setDeducteeCode(row.getField("DeducteeCode"));

		if (StringUtils.isNotBlank(row.getField("DeducteeName"))) {
			nrTransactionsMeta.setDeducteeName(row.getField("DeducteeName"));
		} else if (UploadTypes.PROVISION_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			errorDTO.setReason(errorDTO.getReason() + "DeducteeName is mandatory." + "\n");
		}
		nrTransactionsMeta.setDeducteePan(row.getField("DeducteePAN"));
		nrTransactionsMeta.setTin(row.getField("DeducteeTIN"));
		nrTransactionsMeta.setDeducteeGstin(row.getField("DeducteeGSTIN"));
		if (StringUtils.isNotBlank(row.getField("VendorInvoiceNumber"))) {
			nrTransactionsMeta.setVendorDocumentNo(row.getField("VendorInvoiceNumber"));
		} else if (UploadTypes.INVOICE_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			errorDTO.setReason(errorDTO.getReason() + "VendorInvoiceNumber is mandatory." + "\n");
		}

		if (StringUtils.isNotBlank(row.getField("DocumentDate"))) {
			Date documentDate = dateFormatValidation(row.getField("DocumentDate"), errorDTO, "DocumentDate");
			nrTransactionsMeta.setVendorDocumentDate(documentDate);
		} else if (UploadTypes.PROVISION_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			errorDTO.setReason(errorDTO.getReason() + "DocumentDate is mandatory." + "\n");
		}

		if (StringUtils.isNotBlank(row.getField("ERPDocumentNumber"))) {
			nrTransactionsMeta.setErpDocumentNo(row.getField("ERPDocumentNumber"));
		} else {
			errorDTO.setReason(errorDTO.getReason() + "ERPDocumentNumber is mandatory." + "\n");
		}
		if (StringUtils.isNotBlank(row.getField("PostingDate"))) {
			Date postingDate = dateFormatValidation(row.getField("PostingDate"), errorDTO, "PostingDate");
			nrTransactionsMeta.setDocumentPostingDate(postingDate);
		} else {
			errorDTO.setReason(errorDTO.getReason() + "PostingDate is mandatory." + "\n");
		}
		Date paymentDate = dateFormatValidation(row.getField("PaymentDate"), errorDTO, "PaymentDate");
		nrTransactionsMeta.setDateOfPaymentOrCredit(paymentDate);
		Date tdsDeductionDate = dateFormatValidation(row.getField("TDSDeductionDate"), errorDTO, "TDSDeductionDate");
		nrTransactionsMeta.setDateOfDeductionOfTax(tdsDeductionDate);
		if (StringUtils.isNotBlank(row.getField("DocumentType"))) {
			nrTransactionsMeta.setDocumentType(row.getField("DocumentType"));
		} else {
			errorDTO.setReason(errorDTO.getReason() + "DocumentType is mandatory." + "\n");
		}
		if (StringUtils.isNotBlank(row.getField("SupplyType"))) {
			nrTransactionsMeta.setSupplyType(row.getField("SupplyType"));
		} else if (!UploadTypes.PROVISION_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			errorDTO.setReason(errorDTO.getReason() + "SupplyType is mandatory." + "\n");
		}
		nrTransactionsMeta.setMigoNumber(row.getField("MIGONumber"));
		nrTransactionsMeta.setMiroNumber(row.getField("MIRONumber"));
		nrTransactionsMeta.setErpDocumentType(row.getField("ERPDocumentType"));
		if (StringUtils.isNotBlank(row.getField("LineItemNumber"))) {
			nrTransactionsMeta.setLineItemNumber(row.getField("LineItemNumber"));
		} else {
			errorDTO.setReason(errorDTO.getReason() + "LineItemNumber is mandatory." + "\n");
		}
		nrTransactionsMeta.setHsnOrSac(row.getField("HSNorSAC"));
		nrTransactionsMeta.setHsnOrSacDesc(row.getField("HSNorSACDesc"));
		String invoiceDesc = row.getField("InvoiceDesc");
		if (UploadTypes.ADVANCE_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			invoiceDesc = row.getField("AdvanceDesc");
		}
		nrTransactionsMeta.setInvoiceDesc(invoiceDesc);
		nrTransactionsMeta.setGlAccountCode(row.getField("GLAccountCode"));
		nrTransactionsMeta.setGlAccountName(row.getField("GLAccountName"));
		nrTransactionsMeta.setPoNumber(row.getField("PONumber"));
		Date poDate = dateFormatValidation(row.getField("PODate"), errorDTO, "PODate");
		nrTransactionsMeta.setPoDate(poDate);
		nrTransactionsMeta.setPoDesc(row.getField("PODesc"));
		nrTransactionsMeta.setPoType(row.getField("POType"));
		nrTransactionsMeta.setLinkingOfInvoiceWithPo(row.getField("LinkingofInvoicewithPO"));
		if (StringUtils.isNotBlank(row.getField("TaxableValue"))) {
			BigDecimal taxableValue = StringUtils.isNotBlank(row.getField("TaxableValue"))
					? new BigDecimal(row.getField("TaxableValue").trim().replace(",", ""))
					: BigDecimal.ZERO;
			nrTransactionsMeta.setAmountInInr(taxableValue);
		} else {
			errorDTO.setReason(errorDTO.getReason() + "TaxableValue is mandatory." + "\n");
		}

		BigDecimal igstRate = StringUtils.isNotBlank(row.getField("IGSTRate"))
				? new BigDecimal(row.getField("IGSTRate").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setIgstRate(igstRate);

		BigDecimal igstAmount = StringUtils.isNotBlank(row.getField("IGSTAmount"))
				? new BigDecimal(row.getField("IGSTAmount").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setIgstAmount(igstAmount);

		BigDecimal cgstRate = StringUtils.isNotBlank(row.getField("CGSTRate"))
				? new BigDecimal(row.getField("CGSTRate").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setCgstRate(cgstRate);

		BigDecimal cgstAmount = StringUtils.isNotBlank(row.getField("CGSTAmount"))
				? new BigDecimal(row.getField("CGSTAmount").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setCgstAmount(cgstAmount);

		BigDecimal sgstRate = StringUtils.isNotBlank(row.getField("SGSTRate"))
				? new BigDecimal(row.getField("SGSTRate").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setSgstRate(sgstRate);

		BigDecimal sgstAmount = StringUtils.isNotBlank(row.getField("SGSTAmount"))
				? new BigDecimal(row.getField("SGSTAmount").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setSgstAmount(sgstAmount);

		BigDecimal cessRate = StringUtils.isNotBlank(row.getField("CESSRate"))
				? new BigDecimal(row.getField("CESSRate").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setCessRate(cessRate);
		BigDecimal cessAmount = StringUtils.isNotBlank(row.getField("CESSAmount"))
				? new BigDecimal(row.getField("CESSAmount").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setCessAmount(cessAmount);
		nrTransactionsMeta.setPos(row.getField("POS"));
		nrTransactionsMeta.setTdsTaxCodeErp(row.getField("TDSTaxCodeERP"));
		nrTransactionsMeta.setTdsSection(row.getField("TDSSection"));
		BigDecimal tdsRate = StringUtils.isNotBlank(row.getField("TDSRate"))
				? new BigDecimal(row.getField("TDSRate").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setRateAsPerIncometax(tdsRate);
		BigDecimal tdsAmount = StringUtils.isNotBlank(row.getField("TDSAmount"))
				? new BigDecimal(row.getField("TDSAmount").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setTdsAmount(tdsAmount);
		nrTransactionsMeta.setLinkedAdvanceIndicator(row.getField("LinkedAdvanceIndicator"));
		nrTransactionsMeta.setLinkedProvisionIndicator(row.getField("LinkedProvisionIndicator"));
		nrTransactionsMeta.setProvisionCanAdjust(row.getField("ProvisionAdjustmentFlag"));
		nrTransactionsMeta.setAdvanceCanAdjust(row.getField("AdvanceAdjustmentFlag"));
		Boolean challanPaidFlag = StringUtils.isNotBlank(row.getField("ChallanPaidFlag"))
				&& row.getField("ChallanPaidFlag").equals("Y");
		nrTransactionsMeta.setChallanPaid(challanPaidFlag);
		Date challanProcessingDate = dateFormatValidation(row.getField("ChallanProcessingDate"), errorDTO,
				"ChallanProcessingDate");
		nrTransactionsMeta.setChallanGeneratedDate(challanProcessingDate);
		Boolean grossUpIndicator = StringUtils.isNotBlank(row.getField("GrossUpIndicator"))
				? row.getField("GrossUpIndicator").equals("Y")
				: null;
		nrTransactionsMeta.setIsGrossedUp(grossUpIndicator);
		String originalDocumentNumber = row.getField("OriginalDocumentNumber");
		if (UploadTypes.ADVANCE_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			originalDocumentNumber = row.getField("OriginalDocNumber");
		}
		nrTransactionsMeta.setOriginalDocumentNumber(originalDocumentNumber);
		Date originalDocumentDate = dateFormatValidation(row.getField("OriginalDocumentDate"), errorDTO,
				"OriginalDocumentDate");
		nrTransactionsMeta.setOriginalDocumentDate(originalDocumentDate);
		nrTransactionsMeta.setRefKey3(row.getField("RefKey3"));
		nrTransactionsMeta.setBusinessPlace(row.getField("BusinessPlace"));
		nrTransactionsMeta.setBusinessArea(row.getField("BusinessArea"));
		nrTransactionsMeta.setPlant(row.getField("Plant"));
		nrTransactionsMeta.setProfitCenter(row.getField("ProfitCenter"));
		nrTransactionsMeta.setAssignmentNumber(row.getField("AssignmentNumber"));
		BigDecimal tdsBaseValue = StringUtils.isNotBlank(row.getField("TDSBaseValue"))
				? new BigDecimal(row.getField("TDSBaseValue").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setTdsBaseValue(tdsBaseValue);
		String poItemNo = StringUtils.isNotBlank(row.getField("POItemNo")) ? row.getField("POItemNo") : null;
		nrTransactionsMeta.setPoItemNo(poItemNo);
		nrTransactionsMeta.setTypeOfTransaction(row.getField("TypeOfTransaction"));
		nrTransactionsMeta.setUserName(row.getField("UserName"));
		BigDecimal amountForeignCurrency = StringUtils.isNotBlank(row.getField("AmountForeignCurrency"))
				? new BigDecimal(row.getField("AmountForeignCurrency").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setAmountInForeignCurrency(amountForeignCurrency);
		BigDecimal exchangeRate = StringUtils.isNotBlank(row.getField("ExchangeRate"))
				? new BigDecimal(row.getField("ExchangeRate").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setExchangeRate(exchangeRate);
		nrTransactionsMeta.setCurrency(row.getField("Currency"));
		String itemCode = StringUtils.isNotBlank(row.getField("ItemCode")) ? row.getField("ItemCode") : null;
		nrTransactionsMeta.setItemCode(itemCode);
		BigDecimal invoiceValue = StringUtils.isNotBlank(row.getField("InvoiceValue"))
				? new BigDecimal(row.getField("InvoiceValue").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setInvoiceValue(invoiceValue);
		int saaNumber = StringUtils.isNotBlank(row.getField("SAAnumber")) ? Integer.parseInt(row.getField("SAAnumber"))
				: 0;
		nrTransactionsMeta.setSaaNumber(saaNumber);
		if (StringUtils.isNotBlank(row.getField("NRIndicator"))) {
			nrTransactionsMeta.setIsResident(row.getField("NRIndicator"));
		} else if (!UploadTypes.PROVISION_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			errorDTO.setReason(errorDTO.getReason() + "NRIndicator is mandatory." + "\n");
		}
		nrTransactionsMeta.setDebitCreditIndicator(row.getField("DebitCreditIndicator"));
		nrTransactionsMeta.setUserDefinedField1(row.getField("UserDefinedField1"));
		nrTransactionsMeta.setUserDefinedField2(row.getField("UserDefinedField2"));
		nrTransactionsMeta.setUserDefinedField3(row.getField("UserDefinedField3"));
		nrTransactionsMeta.setCountry(row.getField("DeducteeCountry"));
		// Nature of payment
		if (StringUtils.isNotBlank(row.getField("NatureOfPayment"))) {
			nrTransactionsMeta.setNatureOfPayment(row.getField("NatureOfPayment"));
		} else {
			errorDTO.setReason(errorDTO.getReason() + "NatureOfPayment is mandatory." + "\n");
		}
		// NatureOfRemittance
		if (StringUtils.isNotBlank(row.getField("NatureOfRemittance"))) {
			nrTransactionsMeta.setNatureOfRemittance(row.getField("NatureOfRemittance"));
		} else {
			errorDTO.setReason(errorDTO.getReason() + "NatureOfRemittance is mandatory." + "\n");
		}

		nrTransactionsMeta.setDetailedDescription(row.getField("DetailedDescription"));
		BigDecimal incomeOnWhichTaxIsToBeDeducted = StringUtils
				.isNotBlank(row.getField("IncomeOnWhichTaxIsToBeDeducted"))
						? new BigDecimal(row.getField("IncomeOnWhichTaxIsToBeDeducted").trim().replace(",", ""))
						: BigDecimal.ZERO;
		nrTransactionsMeta.setAmountOfIncometax(incomeOnWhichTaxIsToBeDeducted);
		BigDecimal surcharge = StringUtils.isNotBlank(row.getField("Surcharge"))
				? new BigDecimal(row.getField("Surcharge").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setSurcharge(surcharge);
		BigDecimal educationCess = StringUtils.isNotBlank(row.getField("Health&EducationCess"))
				? new BigDecimal(row.getField("Health&EducationCess").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setEductaionCess(educationCess);
		BigDecimal interest = StringUtils.isNotBlank(row.getField("Interest"))
				? new BigDecimal(row.getField("Interest").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setInterest(interest);
		BigDecimal fee = StringUtils.isNotBlank(row.getField("Fee"))
				? new BigDecimal(row.getField("Fee").trim().replace(",", ""))
				: BigDecimal.ZERO;
		nrTransactionsMeta.setEductaionFee(fee);
		nrTransactionsMeta.setArticleOfDtaa(row.getField("ArticleOfDTAA"));
		// DTAAArticleName
		nrTransactionsMeta.setDtaaArticleName(row.getField("DTAAArticleName"));
		Date dateOfDepositOfTaxAtSource = dateFormatValidation(row.getField("TDSremittancedate"), errorDTO,
				"TDSremittancedate");
		nrTransactionsMeta.setDateOfDepositOfTax(dateOfDepositOfTaxAtSource);
		if (StringUtils.isNotBlank(row.getField("UpdateInTreatyEligibilityConditions"))) {
			Boolean updateInTreatyEligibilityConditions = StringUtils
					.isNotBlank(row.getField("UpdateInTreatyEligibilityConditions"))
					&& row.getField("UpdateInTreatyEligibilityConditions").equals("Y");
			nrTransactionsMeta.setUpdateInTreatyEligibilityConditions(updateInTreatyEligibilityConditions);
		} else {
			errorDTO.setReason(errorDTO.getReason() + "UpdateInTreatyEligibilityConditions is mandatory." + "\n");
		}

		if (StringUtils.isNotBlank(row.getField("IsDeducteeMasterUpdated"))) {
			Boolean isDeducteeMasterUpdated = StringUtils.isNotBlank(row.getField("IsDeducteeMasterUpdated"))
					&& row.getField("IsDeducteeMasterUpdated").equals("Y");
			nrTransactionsMeta.setIsDeducteeMasterUpdated(isDeducteeMasterUpdated);
		} else {
			errorDTO.setReason(errorDTO.getReason() + "IsDeducteeMasterUpdated is mandatory." + "\n");
		}
		Boolean isTRCAvailable = StringUtils.isNotBlank(row.getField("IsTRCAvailable"))
				? row.getField("IsTRCAvailable").equals("Y")
				: null;
		nrTransactionsMeta.setIsTrcAvailable(isTRCAvailable);
		Boolean trcAvailableInFuture = StringUtils.isNotBlank(row.getField("TRCAvailableInFuture"))
				? row.getField("TRCAvailableInFuture").equals("Y")
				: null;
		nrTransactionsMeta.setIsTrcFuture(trcAvailableInFuture);
		Date trcAvailableFutureDate = dateFormatValidation(row.getField("TRCAvailableFutureDate"), errorDTO,
				"TRCAvailableFutureDate");
		nrTransactionsMeta.setTrcFutureDate(trcAvailableFutureDate);
		Date trcApplicableFromDate = dateFormatValidation(row.getField("TRCApplicableFromDate"), errorDTO,
				"TRCApplicableFromDate");
		nrTransactionsMeta.setIsTrcApplicableFrom(trcApplicableFromDate);
		Date trcApplicableToDate = dateFormatValidation(row.getField("TRCApplicableToDate"), errorDTO,
				"TRCApplicableToDate");
		nrTransactionsMeta.setIsTrcApplicableTo(trcApplicableToDate);
		Boolean isForm10FAvailable = StringUtils.isNotBlank(row.getField("IsForm10FAvailable"))
				? row.getField("IsForm10FAvailable").equals("Y")
				: null;
		nrTransactionsMeta.setIsTenfAvailable(isForm10FAvailable);
		Boolean form10FAvailableInFuture = StringUtils.isNotBlank(row.getField("Form10FAvailableInFuture"))
				? row.getField("Form10FAvailableInFuture").equals("Y")
				: null;
		nrTransactionsMeta.setIsTenfFuture(form10FAvailableInFuture);
		Date form10FAvailableFutureDate = dateFormatValidation(row.getField("Form10FAvailableFutureDate"), errorDTO,
				"Form10FAvailableFutureDate");
		nrTransactionsMeta.setTenfFutureDate(form10FAvailableFutureDate);
		Date form10FApplicableFromDate = dateFormatValidation(row.getField("Form10FApplicableFromDate"), errorDTO,
				"Form10FApplicableFromDate");
		nrTransactionsMeta.setIsTenfApplicableFrom(form10FApplicableFromDate);
		Date form10FApplicableToDate = dateFormatValidation(row.getField("Form10FApplicableToDate"), errorDTO,
				"Form10FApplicableToDate");
		nrTransactionsMeta.setIsTenfApplicableTo(form10FApplicableToDate);
		Boolean whetherPEInIndia = StringUtils.isNotBlank(row.getField("WhetherPEInIndia"))
				? row.getField("WhetherPEInIndia").equals("Y")
				: null;
		nrTransactionsMeta.setIsPeIndia(whetherPEInIndia);
		Date peInIndiaFromDate = dateFormatValidation(row.getField("PEInIndiaFromDate"), errorDTO, "PEInIndiaFromDate");
		nrTransactionsMeta.setPeInIndiaFromDate(peInIndiaFromDate);
		Date peInIndiaToDate = dateFormatValidation(row.getField("PEInIndiaToDate"), errorDTO, "PEInIndiaToDate");
		nrTransactionsMeta.setPeInIndiaToDate(peInIndiaToDate);
		Boolean isNoPEDeclarationAvailable = StringUtils.isNotBlank(row.getField("IsNoPEDeclarationAvailable"))
				? row.getField("IsNoPEDeclarationAvailable").equals("Y")
				: null;
		nrTransactionsMeta.setIsNoPeDocumentAvailable(isNoPEDeclarationAvailable);
		Boolean noPEDeclarationAvailableInFuture = StringUtils
				.isNotBlank(row.getField("NoPEDeclarationAvailableInFuture"))
						? row.getField("NoPEDeclarationAvailableInFuture").equals("Y")
						: null;
		nrTransactionsMeta.setNoPeDeclarationAvailableInFuture(noPEDeclarationAvailableInFuture);
		Date noPEDeclarationAvailableFutureDate = dateFormatValidation(
				row.getField("NoPEDeclarationAvailableFutureDate"), errorDTO, "NoPEDeclarationAvailableFutureDate");
		nrTransactionsMeta.setNoPeDeclarationAvailableFutureDate(noPEDeclarationAvailableFutureDate);
		Date noPEDeclarationApplicableFromDate = dateFormatValidation(row.getField("NoPEDeclarationApplicableFromDate"),
				errorDTO, "NoPEDeclarationApplicableFromDate");
		nrTransactionsMeta.setNoPeDocumentApplicableFrom(noPEDeclarationApplicableFromDate);
		Date noPEDeclarationApplicableToDate = dateFormatValidation(row.getField("NoPEDeclarationApplicableToDate"),
				errorDTO, "NoPEDeclarationApplicableToDate");
		nrTransactionsMeta.setNoPeDocumentApplicableTo(noPEDeclarationApplicableToDate);
		Boolean whetherIncomeReceivedIsConnectedWithPE = StringUtils
				.isNotBlank(row.getField("WhetherIncomeReceivedIsConnectedWithPE"))
						? row.getField("WhetherIncomeReceivedIsConnectedWithPE").equals("Y")
						: null;
		nrTransactionsMeta.setWhetherIncomeReceived(whetherIncomeReceivedIsConnectedWithPE);
		// IsPOEMApplicable
		Boolean isPOEMApplicable = StringUtils.isNotBlank(row.getField("IsPOEMApplicable"))
				? row.getField("IsPOEMApplicable").equals("Y")
				: null;
		nrTransactionsMeta.setIsPoemOfDeductee(isPOEMApplicable);
		Date poemApplicableFromDate = dateFormatValidation(row.getField("POEMApplicableFromDate"), errorDTO,
				"POEMApplicableFromDate");
		nrTransactionsMeta.setPoemApplicableFromDate(poemApplicableFromDate);
		Date poemApplicableToDate = dateFormatValidation(row.getField("POEMApplicableToDate"), errorDTO,
				"POEMApplicableToDate");
		nrTransactionsMeta.setPoemApplicableToDate(poemApplicableToDate);
		// IsNoPOEMDeclarationAvailable
		Boolean isNoPOEMDeclarationAvailable = StringUtils.isNotBlank(row.getField("IsNoPOEMDeclarationAvailable"))
				? row.getField("IsNoPOEMDeclarationAvailable").equals("Y")
				: null;
		nrTransactionsMeta.setIsNoPoemAvailable(isNoPOEMDeclarationAvailable);
		Boolean noPOEMDeclarationAvailableInFuture = StringUtils
				.isNotBlank(row.getField("NoPOEMDeclarationAvailableInFuture"))
						? row.getField("NoPOEMDeclarationAvailableInFuture").equals("Y")
						: null;
		nrTransactionsMeta.setIsNoPoemDeclarationInFuture(noPOEMDeclarationAvailableInFuture);
		// NoPOEMDeclarationAvailableFutureDate
		Date noPOEMDeclarationAvailableFutureDate = dateFormatValidation(
				row.getField("NoPOEMDeclarationAvailableFutureDate"), errorDTO, "NoPOEMDeclarationAvailableFutureDate");
		nrTransactionsMeta.setNoPoemFutureDate(noPOEMDeclarationAvailableFutureDate);
		// NoPOEMDeclarationApplicableFromDate
		Date noPOEMDeclarationApplicableFromDate = dateFormatValidation(
				row.getField("NoPOEMDeclarationApplicableFromDate"), errorDTO, "NoPOEMDeclarationApplicableFromDate");
		nrTransactionsMeta.setIsNoPoemApplicableFrom(noPOEMDeclarationApplicableFromDate);
		// NoPOEMDeclarationApplicableToDate
		Date noPOEMDeclarationApplicableToDate = dateFormatValidation(row.getField("NoPOEMDeclarationApplicableToDate"),
				errorDTO, "NoPOEMDeclarationApplicableToDate");
		nrTransactionsMeta.setIsNoPoemApplicableTo(noPOEMDeclarationApplicableToDate);
		Boolean isFixedBaseAvailableInIndia = StringUtils.isNotBlank(row.getField("IsFixedBaseAvailableInIndia"))
				? row.getField("IsFixedBaseAvailableInIndia").equals("Y")
				: null;
		nrTransactionsMeta.setIsFixedBaseAvailable(isFixedBaseAvailableInIndia);
		Date fixedBaseInIndiaFromDate = dateFormatValidation(row.getField("FixedBaseInIndiaFromDate"), errorDTO,
				"FixedBaseInIndiaFromDate");
		nrTransactionsMeta.setIsFixedbaseApplicableFrom(fixedBaseInIndiaFromDate);
		// FixedBaseInIndiaToDate
		Date fixedBaseInIndiaToDate = dateFormatValidation(row.getField("FixedBaseInIndiaToDate"), errorDTO,
				"FixedBaseInIndiaToDate");
		nrTransactionsMeta.setIsFixedbaseApplicableTo(fixedBaseInIndiaToDate);
		// IsNoFixedBaseDeclarationAvailable
		Boolean isNoFixedBaseDeclarationAvailable = StringUtils
				.isNotBlank(row.getField("IsNoFixedBaseDeclarationAvailable"))
						? row.getField("IsNoFixedBaseDeclarationAvailable").equals("Y")
						: null;
		nrTransactionsMeta.setIsNoFixedBaseDeclaration(isNoFixedBaseDeclarationAvailable);
		Boolean noFixedBaseDeclarationAvailableInFuture = StringUtils
				.isNotBlank(row.getField("NoFixedBaseDeclarationAvailableInFuture"))
						? row.getField("NoFixedBaseDeclarationAvailableInFuture").equals("Y")
						: null;
		nrTransactionsMeta.setNoFixedBaseDeclarationAvailableInFuture(noFixedBaseDeclarationAvailableInFuture);
		// NoFixedBaseDeclarationAvailableFutureDate
		Date noFixedBaseDeclarationAvailableFutureDate = dateFormatValidation(
				row.getField("NoFixedBaseDeclarationAvailableFutureDate"), errorDTO,
				"NoFixedBaseDeclarationAvailableFutureDate");
		nrTransactionsMeta.setNoFixedBaseDeclarationAvailableFutureDate(noFixedBaseDeclarationAvailableFutureDate);
		Date noFixedBaseDeclarationFromDate = dateFormatValidation(row.getField("NoFixedBaseDeclarationFromDate"),
				errorDTO, "NoFixedBaseDeclarationFromDate");
		nrTransactionsMeta.setNoFixedBaseDeclarationFromDate(noFixedBaseDeclarationFromDate);
		Date noFixedBaseDeclarationToDate = dateFormatValidation(row.getField("NoFixedBaseDeclarationToDate"), errorDTO,
				"NoFixedBaseDeclarationToDate");
		nrTransactionsMeta.setNoFixedBaseDeclarationToDate(noFixedBaseDeclarationToDate);
		nrTransactionsMeta.setStayPeriodFinancialYear(row.getField("PeriodOfStayInIndia"));
		Boolean beneficialOwnerOfIncome = StringUtils.isNotBlank(row.getField("BeneficialOwnerOfIncome"))
				? row.getField("BeneficialOwnerOfIncome").equals("Y")
				: null;
		nrTransactionsMeta.setBeneficialOwnerOfIncome(beneficialOwnerOfIncome);
		Boolean isBeneficialOwnershipDeclarationAvailable = StringUtils
				.isNotBlank(row.getField("IsBeneficialOwnershipDeclarationAvailable"))
						? row.getField("IsBeneficialOwnershipDeclarationAvailable").equals("Y")
						: null;
		nrTransactionsMeta.setIsBeneficialOwnershipOfDeclaration(isBeneficialOwnershipDeclarationAvailable);
		Boolean mlipptConditionSatisfied = StringUtils.isNotBlank(row.getField("MLIPPTConditionSatisfied"))
				? row.getField("MLIPPTConditionSatisfied").equals("Y")
				: null;
		nrTransactionsMeta.setMliPptConditionSatisifed(mlipptConditionSatisfied);
		// MLISLOBConditionSatisfied
		Boolean mlisLobConditionSatisfied = StringUtils.isNotBlank(row.getField("MLISLOBConditionSatisfied"))
				? row.getField("MLISLOBConditionSatisfied").equals("Y")
				: null;
		nrTransactionsMeta.setMliSlobConditionSatisifed(mlisLobConditionSatisfied);
		Boolean isMLIPPTOrSLOBDeclarationAvailable = StringUtils
				.isNotBlank(row.getField("IsMLIPPTOrSLOBDeclarationAvailable"))
				&& row.getField("IsMLIPPTOrSLOBDeclarationAvailable").equals("Y");
		nrTransactionsMeta.setIsMliOrPptSlob(isMLIPPTOrSLOBDeclarationAvailable);
		Boolean isTheTransactionGAARCompliant = StringUtils.isNotBlank(row.getField("IsTheTransactionGAARCompliant"))
				? row.getField("IsTheTransactionGAARCompliant").equals("Y")
				: null;
		nrTransactionsMeta.setIsGAARComplaint(isTheTransactionGAARCompliant);
		nrTransactionsMeta.setCountryToRemittance(row.getField("CountryToWhichRemittanceIsMade"));
		BigDecimal actualAmountOfRemittanceAfterTDSInForeignCurrency = StringUtils
				.isNotBlank(row.getField("ActualAmountOfRemittanceAfterTDSInForeignCurrency"))
						? new BigDecimal(row.getField("ActualAmountOfRemittanceAfterTDSInForeignCurrency").trim()
								.replace(",", ""))
						: BigDecimal.ZERO;
		nrTransactionsMeta.setAmountInForeignCurrency(actualAmountOfRemittanceAfterTDSInForeignCurrency); // ActualAmountOfRemittanceAfterTDSInForeignCurrency
		Date proposedDateOfRemittance = dateFormatValidation(row.getField("ProposedDateOfRemittance"), errorDTO,
				"ProposedDateOfRemittance");
		nrTransactionsMeta.setDateOfRemittance(proposedDateOfRemittance);
		BigDecimal aggregateAmountOfRemittanceMadeDuringTheFY = StringUtils
				.isNotBlank(row.getField("AggregateAmountOfRemittanceMadeDuringTheFY"))
						? new BigDecimal(
								row.getField("AggregateAmountOfRemittanceMadeDuringTheFY").trim().replace(",", ""))
						: BigDecimal.ZERO;
		nrTransactionsMeta.setAggregateAmountOfRemittanceByFy(aggregateAmountOfRemittanceMadeDuringTheFY); // AggregateAmountOfRemittanceMadeDuringTheFY
		nrTransactionsMeta.setRelevantPurposeCodeAsPerRbi(row.getField("RelevantPurposeCodeAsPerRBI"));
		nrTransactionsMeta.setUniqueAcknowledgementOfTheCorrespondingForm15ca(
				row.getField("UniqueAcknowledgementOfTheCorrespondingForm15CA")); // UniqueAcknowledgementOfTheCorrespondingForm15CA

		return nrTransactionsMeta;
	}

	/**
	 * 
	 * @param dateField
	 * @param nrExcelErrorDTO
	 * @param filedType
	 * @return
	 */
	private Date dateFormatValidation(String dateField, NrExcelErrorDTO nrExcelErrorDTO, String filedType) {
		Date value = null;
		if (StringUtils.isNotBlank(dateField)) {
			String dateString = dateField.replace("/", "-");
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = sdf.parse(dateString);
				if (!dateString.equals(sdf.format(date))) {
					nrExcelErrorDTO.setReason(
							nrExcelErrorDTO.getReason() + filedType + " date should be in YYYY-MM-DD format." + "\n");
				}
				value = date;
			} catch (Exception e) {
				nrExcelErrorDTO.setReason(
						nrExcelErrorDTO.getReason() + filedType + " date should be in YYYY-MM-DD format." + "\n");
			}
		}
		return value;
	}

	/**
	 * 
	 * @param row
	 * @param errorDTO
	 * @return
	 */
	private NrExcelErrorDTO getNRTransactionsErrorCsvMapping(CsvRow row, NrExcelErrorDTO errorDTO) {
		errorDTO.setSourceIdentifier(row.getField("SourceIdentifier"));
		errorDTO.setSourceFilename(row.getField("SourceFileName"));
		errorDTO.setDeductorCode(row.getField("DeductorCode"));
		errorDTO.setDeductorName(row.getField("DeductorName"));
		errorDTO.setDeductorPan(row.getField("DeductorPAN"));
		errorDTO.setDeductorMasterTan(row.getField("DeductorTAN"));
		errorDTO.setDeductorGstin(row.getField("DeductorGSTIN"));
		errorDTO.setDeducteeCode(row.getField("DeducteeCode"));
		errorDTO.setDeducteeName(row.getField("DeducteeName"));
		errorDTO.setDeducteePan(row.getField("DeducteePAN"));
		errorDTO.setTin(row.getField("DeducteeTIN"));
		errorDTO.setDeducteeGstin(row.getField("DeducteeGSTIN"));
		errorDTO.setVendorDocumentNo(row.getField("VendorInvoiceNumber"));
		errorDTO.setVendorDocumentDate(row.getField("DocumentDate"));
		errorDTO.setErpDocumentNo(row.getField("ERPDocumentNumber"));
		errorDTO.setDocumentPostingDate(row.getField("PostingDate"));
		errorDTO.setStringOfPaymentOrCredit(row.getField("PaymentDate"));
		errorDTO.setDateOfDeductionOfTax(row.getField("TDSDeductionDate"));
		errorDTO.setDocumentType(row.getField("DocumentType"));
		errorDTO.setSupplyType(row.getField("SupplyType"));
		errorDTO.setMigoNumber(row.getField("MIGONumber"));
		errorDTO.setMiroNumber(row.getField("MIRONumber"));
		errorDTO.setErpDocumentType(row.getField("ERPDocumentType"));
		errorDTO.setLineItemNumber(row.getField("LineItemNumber"));
		errorDTO.setHsnOrSac(row.getField("HSNorSAC"));
		errorDTO.setHsnOrSacDesc(row.getField("HSNorSACDesc"));
		errorDTO.setInvoiceDesc(row.getField("InvoiceDesc"));
		errorDTO.setGlAccountCode(row.getField("GLAccountCode"));
		errorDTO.setGlAccountName(row.getField("GLAccountName"));
		errorDTO.setPoNumber(row.getField("PONumber"));
		errorDTO.setPoDate(row.getField("PODate"));
		errorDTO.setPoDesc(row.getField("PODesc"));
		errorDTO.setPoType(row.getField("POType"));
		errorDTO.setLinkingOfInvoiceWithPo(row.getField("LinkingofInvoicewithPO"));
		errorDTO.setAmountInInr(row.getField("TaxableValue"));
		errorDTO.setIgstRate(row.getField("IGSTRate"));
		errorDTO.setIgstAmount(row.getField("IGSTAmount"));
		errorDTO.setCgstRate(row.getField("CGSTRate"));
		errorDTO.setCgstAmount(row.getField("CGSTAmount"));
		errorDTO.setSgstRate(row.getField("SGSTRate"));
		errorDTO.setSgstAmount(row.getField("SGSTAmount"));
		errorDTO.setCessRate(row.getField("CESSRate"));
		errorDTO.setCessAmount(row.getField("CESSAmount"));
		errorDTO.setPos(row.getField("POS"));
		errorDTO.setTdsTaxCodeErp(row.getField("TDSTaxCodeERP"));
		errorDTO.setTdsSection(row.getField("TDSSection"));
		errorDTO.setRateAsPerIncometax(row.getField("TDSRate"));
		errorDTO.setTdsAmount(row.getField("TDSAmount"));
		errorDTO.setLinkedAdvanceIndicator(row.getField("LinkedAdvanceIndicator"));
		errorDTO.setLinkedProvisionIndicator(row.getField("LinkedProvisionIndicator"));
		errorDTO.setProvisionCanAdjust(row.getField("ProvisionAdjustmentFlag"));
		errorDTO.setAdvanceCanAdjust(row.getField("AdvanceAdjustmentFlag"));
		errorDTO.setChallanPaid(row.getField("ChallanPaidFlag"));
		errorDTO.setChallanGeneratedDate(row.getField("ChallanProcessingDate"));
		errorDTO.setIsGrossedUp(row.getField("GrossUpIndicator"));
		errorDTO.setOriginalDocumentNumber(row.getField("OriginalDocumentNumber"));
		errorDTO.setOriginalDocumentDate(row.getField("OriginalDocumentDate"));
		errorDTO.setRefKey3(row.getField("RefKey3"));
		errorDTO.setBusinessPlace(row.getField("BusinessPlace"));
		errorDTO.setBusinessArea(row.getField("BusinessArea"));
		errorDTO.setPlant(row.getField("Plant"));
		errorDTO.setProfitCenter(row.getField("ProfitCenter"));
		errorDTO.setAssignmentNumber(row.getField("AssignmentNumber"));
		errorDTO.setTdsBaseValue(row.getField("TDSBaseValue"));
		errorDTO.setPoItemNo(row.getField("POItemNo"));
		errorDTO.setTypeOfTransaction(row.getField("TypeOfTransaction"));
		errorDTO.setUserName(row.getField("UserName"));
		errorDTO.setAmountInForeignCurrency(row.getField("AmountForeignCurrency"));
		errorDTO.setExchangeRate(row.getField("ExchangeRate"));
		errorDTO.setCurrency(row.getField("Currency"));
		errorDTO.setItemCode(row.getField("ItemCode"));
		errorDTO.setInvoiceValue(row.getField("InvoiceValue"));
		errorDTO.setSaaNumber(row.getField("SAAnumber"));
		errorDTO.setIsResident(row.getField("NRIndicator")); // NRIndicator
		errorDTO.setTdsRemittanceDate(row.getField("TDSremittancedate"));
		errorDTO.setDebitCreditIndicator(row.getField("DebitCreditIndicator"));
		errorDTO.setUserDefinedField1(row.getField("UserDefinedField1"));
		errorDTO.setUserDefinedField2(row.getField("UserDefinedField2"));
		errorDTO.setUserDefinedField3(row.getField("UserDefinedField3"));
		errorDTO.setCountry(row.getField("DeducteeCountry"));
		// Nature of payment
		errorDTO.setNatureOfPayment(row.getField("NatureOfPayment"));
		errorDTO.setNatureOfRemittance(row.getField("NatureOfRemittance"));
		errorDTO.setDetailedDescription(row.getField("DetailedDescription"));
		errorDTO.setAmountOfIncometax(row.getField("IncomeOnWhichTaxIsToBeDeducted"));
		errorDTO.setSurcharge(row.getField("Surcharge"));
		errorDTO.setEductaionCess(row.getField("Health&EducationCess"));
		errorDTO.setInterest(row.getField("Interest"));
		errorDTO.setEductaionFee(row.getField("Fee"));
		errorDTO.setArticleOfDtaa(row.getField("ArticleOfDTAA"));
		// DTAAArticleName
		errorDTO.setDtaaArticleName(row.getField("DTAAArticleName"));
		errorDTO.setDateOfDepositOfTax(row.getField("TDSremittancedate")); // TDSremittancedate
		errorDTO.setUpdateInTreatyEligibilityConditions(row.getField("UpdateInTreatyEligibilityConditions"));
		errorDTO.setIsDeducteeMasterUpdated(row.getField("IsDeducteeMasterUpdated"));
		errorDTO.setIsTrcAvailable(row.getField("IsTRCAvailable"));
		errorDTO.setIsTrcFuture(row.getField("TRCAvailableInFuture"));
		errorDTO.setTrcFutureDate(row.getField("TRCAvailableFutureDate"));
		errorDTO.setIsTrcApplicableFrom(row.getField("TRCApplicableFromDate"));
		errorDTO.setIsTrcApplicableTo(row.getField("TRCApplicableToDate"));
		errorDTO.setIsTenfAvailable(row.getField("IsForm10FAvailable"));
		errorDTO.setIsTenfFuture(row.getField("Form10FAvailableInFuture"));
		errorDTO.setTenfFutureDate(row.getField("Form10FAvailableFutureDate"));
		errorDTO.setIsTenfApplicableFrom(row.getField("Form10FApplicableFromDate"));
		errorDTO.setIsTenfApplicableTo(row.getField("Form10FApplicableToDate"));
		errorDTO.setIsPeIndia(row.getField("WhetherPEInIndia"));
		errorDTO.setPeInIndiaFromDate(row.getField("PEInIndiaFromDate"));
		errorDTO.setPeInIndiaToDate(row.getField("PEInIndiaToDate"));
		errorDTO.setIsNoPeDocumentAvailable(row.getField("IsNoPEDeclarationAvailable"));
		errorDTO.setNoPeDeclarationAvailableInFuture(row.getField("NoPEDeclarationAvailableInFuture"));
		errorDTO.setNoPeDeclarationAvailableFutureDate(row.getField("NoPEDeclarationAvailableFutureDate"));
		errorDTO.setNoPeDocumentApplicableFrom(row.getField("NoPEDeclarationApplicableFromDate"));
		errorDTO.setNoPeDocumentApplicableTo(row.getField("NoPEDeclarationApplicableToDate"));
		errorDTO.setWhetherIncomeReceived(row.getField("WhetherIncomeReceivedIsConnectedWithPE"));
		errorDTO.setIsPoemOfDeductee(row.getField("IsPOEMApplicable"));
		errorDTO.setPoemApplicableFromDate(row.getField("POEMApplicableFromDate"));
		errorDTO.setPoemApplicableToDate(row.getField("POEMApplicableToDate"));
		errorDTO.setIsNoPoemAvailable(row.getField("IsNoPOEMDeclarationAvailable"));
		errorDTO.setIsNoPoemDeclarationInFuture(row.getField("NoPOEMDeclarationAvailableInFuture"));
		errorDTO.setNoPoemFutureDate(row.getField("NoPOEMDeclarationAvailableFutureDate"));
		errorDTO.setIsNoPoemApplicableFrom(row.getField("NoPOEMDeclarationApplicableFromDate"));
		errorDTO.setIsNoPoemApplicableTo(row.getField("NoPOEMDeclarationApplicableToDate"));
		errorDTO.setIsFixedBaseAvailable(row.getField("IsFixedBaseAvailableInIndia"));
		errorDTO.setIsFixedbaseApplicableFrom(row.getField("FixedBaseInIndiaFromDate"));
		errorDTO.setIsFixedbaseApplicableTo(row.getField("FixedBaseInIndiaToDate"));
		errorDTO.setIsNoFixedBaseDeclaration(row.getField("IsNoFixedBaseDeclarationAvailable"));
		errorDTO.setNoFixedBaseDeclarationAvailableInFuture(row.getField("NoFixedBaseDeclarationAvailableInFuture"));
		errorDTO.setNoFixedBaseDeclarationAvailableFutureDate(
				row.getField("NoFixedBaseDeclarationAvailableFutureDate"));
		errorDTO.setNoFixedBaseDeclarationFromDate(row.getField("NoFixedBaseDeclarationFromDate"));
		errorDTO.setNoFixedBaseDeclarationToDate(row.getField("NoFixedBaseDeclarationToDate"));
		errorDTO.setStayPeriodFinancialYear(row.getField("PeriodOfStayInIndia"));
		errorDTO.setBeneficialOwnerOfIncome(row.getField("BeneficialOwnerOfIncome"));
		errorDTO.setIsBeneficialOwnershipOfDeclaration(row.getField("IsBeneficialOwnershipDeclarationAvailable"));
		errorDTO.setMliPptConditionSatisifed(row.getField("MLIPPTConditionSatisfied"));
		errorDTO.setMliSlobConditionSatisifed(row.getField("MLISLOBConditionSatisfied"));
		errorDTO.setIsMliOrPptSlob(row.getField("IsMLIPPTOrSLOBDeclarationAvailable"));
		errorDTO.setIsGAARComplaString(row.getField("IsTheTransactionGAARCompliant"));
		errorDTO.setCountryToRemittance(row.getField("CountryToWhichRemittanceIsMade"));
		errorDTO.setAmountInForeignCurrency(row.getField("ActualAmountOfRemittanceAfterTDSInForeignCurrency")); // ActualAmountOfRemittanceAfterTDSInForeignCurrency
		errorDTO.setDateOfRemittance(row.getField("ProposedDateOfRemittance"));
		errorDTO.setAggregateAmountOfRemittanceByFy(row.getField("AggregateAmountOfRemittanceMadeDuringTheFY")); // AggregateAmountOfRemittanceMadeDuringTheFY
		errorDTO.setRelevantPurposeCodeAsPerRbi(row.getField("RelevantPurposeCodeAsPerRBI"));
		errorDTO.setUniqueAcknowledgementOfTheCorrespondingForm15ca(
				row.getField("UniqueAcknowledgementOfTheCorrespondingForm15CA")); // UniqueAcknowledgementOfTheCorrespondingForm15CA

		return errorDTO;
	}

	private BigDecimal getCurrencyAmountCalculation(Map<String, Double> currencyMap, String currency,
			BigDecimal foreignCurrency) {
		BigDecimal conversionAmount = BigDecimal.ZERO;
		if (currencyMap.get(currency) != null) {
			conversionAmount = foreignCurrency.multiply(BigDecimal.valueOf(currencyMap.get(currency)));
		}
		return conversionAmount;
	}

	private Map<String, List<DeducteeMasterNonResidential>> getDeducteeNonResidentRecords(String deductorPan,
			String tenantId) {
		Map<String, List<DeducteeMasterNonResidential>> deducteeMap = new HashMap<>();
		List<DeducteeMasterNonResidential> deductees = onboardingClient.getNrDeductees(deductorPan, tenantId).getBody()
				.getData();
		for (DeducteeMasterNonResidential deductee : deductees) {
			if (!deducteeMap.containsKey(deductee.getDeducteeKey())) {
				deducteeMap.put(deductee.getDeducteeKey(), new ArrayList<>());
			}
			deducteeMap.get(deductee.getDeducteeKey()).add(deductee);
		}
		return deducteeMap;
	}

	private Boolean rateMasterValidation(NrTransactionsMeta nrTransactionsMetaData,
			DeducteeMasterNonResidential deducteeMaster) {
		Boolean isValid = false;
		if (StringUtils.isNotBlank(deducteeMaster.getDeducteePAN())) {
			isValid = true;
		} else if (StringUtils.isNotBlank(nrTransactionsMetaData.getDeducteeName())
				&& StringUtils.isNotBlank(deducteeMaster.getEmailAddress())
				&& StringUtils.isNotBlank(deducteeMaster.getPhoneNumber())
				&& (nrTransactionsMetaData.getIsTrcAvailable() || (nrTransactionsMetaData.getIsTrcFuture()
						&& nrTransactionsMetaData.getTrcFutureDate() != null))
				&& StringUtils.isNotBlank(nrTransactionsMetaData.getTin())) {
			isValid = true;
		}
		return isValid;
	}

	/**
	 * 
	 * @param nrTransactionsMetaData
	 * @return
	 */
	private String getDeducteeKey(String deducteeCode, String deducteeName, String deducteePan) {
		// set deducteee key
		String deducteeKey = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(deducteeCode)) {
			deducteeKey = deducteeCode.trim();
		} else {
			String name = deducteeName.trim().toLowerCase();
			name = name.replaceAll("[^a-z0-9 ]", "").replace(" ", "-");
			if (StringUtils.isNotBlank(deducteePan)) {
				deducteeKey = name.concat(deducteePan.trim());
			} else {
				deducteeKey = name;
			}
		}
		return deducteeKey;
	}

	private boolean treatyMasterValidations(NrTransactionsMeta nrTransactionsMetaData) {
		Date postingDocumentDate = nrTransactionsMetaData.getDocumentPostingDate();
		boolean isValid = false;
		boolean isTrcValid = false;
		boolean isTenfValid = false;
		boolean isPEValid = false;
		boolean isPoemValid = false;
		boolean isFixedBaseValid = false;
		Integer currentYear = CommonUtil.getAssessmentYear(null);
		Calendar cal = Calendar.getInstance();
		if (nrTransactionsMetaData.getIsTrcAvailable()) {
			isTrcValid = postingDocumentValidation(nrTransactionsMetaData.getIsTrcApplicableFrom(),
					nrTransactionsMetaData.getIsTrcApplicableTo(), postingDocumentDate);
		} else if (nrTransactionsMetaData.getIsTrcFuture() && nrTransactionsMetaData.getTrcFutureDate() != null) {
			cal.setTime(nrTransactionsMetaData.getTrcFutureDate());
			int futureDateMonth = cal.get(Calendar.MONTH) + 1;
			Integer futureDateYear = CommonUtil.getAssessmentYearByYear(cal.get(Calendar.YEAR), futureDateMonth);
			isTrcValid = futureDateYear.equals(currentYear);
		}
		if (isTrcValid) {
			if (nrTransactionsMetaData.getIsTenfAvailable()) {
				isTenfValid = postingDocumentValidation(nrTransactionsMetaData.getIsTenfApplicableFrom(),
						nrTransactionsMetaData.getIsTenfApplicableTo(), postingDocumentDate);
			} else if (nrTransactionsMetaData.getIsTenfFuture() && nrTransactionsMetaData.getTenfFutureDate() != null) {
				cal.setTime(nrTransactionsMetaData.getTenfFutureDate());
				int futureDateMonth = cal.get(Calendar.MONTH) + 1;
				Integer futureDateYear = CommonUtil.getAssessmentYearByYear(cal.get(Calendar.YEAR), futureDateMonth);
				isTenfValid = futureDateYear.equals(currentYear);
			}
			if (isTenfValid) {
				if (!nrTransactionsMetaData.getIsPeIndia() && nrTransactionsMetaData.getIsNoPeDocumentAvailable()) {
					isPEValid = postingDocumentValidation(nrTransactionsMetaData.getNoPeDocumentApplicableFrom(),
							nrTransactionsMetaData.getNoPeDocumentApplicableTo(), postingDocumentDate);
				}
				if (isPEValid) {
					if (!nrTransactionsMetaData.getIsPoemOfDeductee()
							&& nrTransactionsMetaData.getIsNoPoemAvailable()) {
						isPoemValid = postingDocumentValidation(nrTransactionsMetaData.getIsNoPoemApplicableFrom(),
								nrTransactionsMetaData.getIsNoPoemApplicableTo(), postingDocumentDate);
					} else if (!nrTransactionsMetaData.getIsPoemOfDeductee()
							&& !nrTransactionsMetaData.getIsNoPoemAvailable()
							&& nrTransactionsMetaData.getIsNoPoemDeclarationInFuture()
							&& nrTransactionsMetaData.getNoPoemFutureDate() != null) {
						cal.setTime(nrTransactionsMetaData.getNoPoemFutureDate());
						int futureDateMonth = cal.get(Calendar.MONTH) + 1;
						Integer futureDateYear = CommonUtil.getAssessmentYearByYear(cal.get(Calendar.YEAR),
								futureDateMonth);
						isPoemValid = futureDateYear.equals(currentYear);
					}
					if (isPoemValid) {
						if (!nrTransactionsMetaData.getIsFixedbaseAvailbleIndia()) {
							isFixedBaseValid = true;
						} else if (nrTransactionsMetaData.getIsNoFixedBaseDeclaration()) {
							isFixedBaseValid = postingDocumentValidation(
									nrTransactionsMetaData.getIsFixedbaseApplicableFrom(),
									nrTransactionsMetaData.getIsFixedbaseApplicableTo(), postingDocumentDate);
						}
					}
				}
			}
		}
		isValid = isTrcValid && isTenfValid && isPEValid && isPoemValid && isFixedBaseValid
				&& nrTransactionsMetaData.getIsGAARComplaint() && nrTransactionsMetaData.getMliPptConditionSatisifed()
				&& nrTransactionsMetaData.getMliSlobConditionSatisifed();
		return isValid;
	}

	public boolean postingDocumentValidation(Date applicableFrom, Date applicableTo, Date postingDocumentDate) {
		boolean isValid = false;
		if (applicableFrom != null) {
			applicableFrom = CommonUtil.getMonthStartDate(applicableFrom);
			postingDocumentDate = CommonUtil.getMonthStartDate(postingDocumentDate);
			if (postingDocumentDate.getTime() >= applicableFrom.getTime() && applicableTo == null) {
				isValid = true;
			} else if (postingDocumentDate.getTime() >= applicableFrom.getTime() && applicableTo != null) {
				postingDocumentDate = CommonUtil.getMonthEndDate(postingDocumentDate);
				applicableTo = CommonUtil.getMonthEndDate(applicableTo);
				if (applicableTo.getTime() >= postingDocumentDate.getTime()) {
					isValid = true;
				}
			}
		}
		return isValid;
	}

	private NrTransactionsMeta setNrTransactionsMetaData(NrTransactionsMeta nrTransactionsMetaData) {
		nrTransactionsMetaData.setIsTrcAvailable(isNull(nrTransactionsMetaData.getIsTrcAvailable()));
		nrTransactionsMetaData.setIsTrcFuture(isNull(nrTransactionsMetaData.getIsTrcFuture()));
		nrTransactionsMetaData.setIsTenfAvailable(isNull(nrTransactionsMetaData.getIsTenfAvailable()));
		nrTransactionsMetaData.setIsTenfFuture(isNull(nrTransactionsMetaData.getIsTenfFuture()));
		nrTransactionsMetaData.setIsNoPoemAvailable(isNull(nrTransactionsMetaData.getIsNoPoemAvailable()));
		nrTransactionsMetaData.setIsPoemOfDeductee(isNull(nrTransactionsMetaData.getIsPoemOfDeductee()));
		nrTransactionsMetaData
				.setIsNoPoemDeclarationInFuture(isNull(nrTransactionsMetaData.getIsNoPoemDeclarationInFuture()));
		nrTransactionsMetaData
				.setIsFixedbaseAvailbleIndia(isNull(nrTransactionsMetaData.getIsFixedbaseAvailbleIndia()));
		nrTransactionsMetaData
				.setIsNoFixedBaseDeclaration(isNull(nrTransactionsMetaData.getIsNoFixedBaseDeclaration()));
		nrTransactionsMetaData.setIsGAARComplaint(isNull(nrTransactionsMetaData.getIsGAARComplaint()));
		nrTransactionsMetaData.setIsPeIndia(isNull(nrTransactionsMetaData.getIsPeIndia()));
		nrTransactionsMetaData.setIsNoPeDocumentAvailable(isNull(nrTransactionsMetaData.getIsNoPeDocumentAvailable()));
		nrTransactionsMetaData.setActive(true);
		nrTransactionsMetaData
				.setMliPptConditionSatisifed(isNull(nrTransactionsMetaData.getMliPptConditionSatisifed()));
		nrTransactionsMetaData
				.setMliSlobConditionSatisifed(isNull(nrTransactionsMetaData.getMliSlobConditionSatisifed()));
		nrTransactionsMetaData.setIsMliOrPptSlob(isNull(nrTransactionsMetaData.getIsMliOrPptSlob()));
		return nrTransactionsMetaData;
	}

	public boolean isNull(Boolean isValue) {
		return isValue != null ? isValue : false;
	}

	public InvoiceLineItem createInvoiceLineItem(BatchUpload batchUpload, BigDecimal rate, String derivedSection,
			NrTransactionsMeta invoiceMetaData, SectionDeterminationDTO sectionDeterminationDTO, int challanMonth,
			List<InvoiceLineItem> batchInvoiceSave, String roundingOff, LongAdder num, BigDecimal surcharge,
			BigDecimal cessRate, BigDecimal cessAmount) {
		InvoiceLineItem invoiceLineItemData = new InvoiceLineItem();
		String keyword = invoiceMetaData.getNatureOfPayment().trim();
		invoiceLineItemData.setNrTransactionsMetaId(invoiceMetaData.getId());
		invoiceLineItemData.setProcessedFrom(UploadTypes.INVOICE_NR_EXCEL.name());
		invoiceLineItemData.setSourceIdentifier(invoiceMetaData.getSourceIdentifier());
		invoiceLineItemData.setSourceFileName(invoiceMetaData.getSourceFilename());
		invoiceLineItemData.setBatchUploadId(batchUpload.getBatchUploadID());
		invoiceLineItemData.setDeducteeName(invoiceMetaData.getDeducteeName());
		invoiceLineItemData.setDeducteeCode(invoiceMetaData.getDeducteeCode());
		String deducteeKey = getDeducteeKey(invoiceMetaData.getDeducteeCode(), invoiceMetaData.getDeducteeName(),
				invoiceMetaData.getDeducteePan());
		invoiceLineItemData.setDeducteeKey(deducteeKey);
		invoiceLineItemData.setClientRate(invoiceMetaData.getRateAsPerIncometax());

		//Client effect rate calculation
		BigDecimal clientEffectiveAmount = invoiceMetaData.getTdsAmount().add(invoiceMetaData.getSurcharge())
				.add(invoiceMetaData.getEductaionCess());
		BigDecimal clientEffectiveRate = (clientEffectiveAmount.multiply(new BigDecimal(100)))
				.divide(invoiceMetaData.getAmountInInr());
		invoiceLineItemData.setClientEffectiveTdsRate(clientEffectiveRate);
		invoiceLineItemData.setClientSection(invoiceMetaData.getTdsSection());
		invoiceLineItemData.setSection(invoiceMetaData.getTdsSection());
		invoiceLineItemData.setTdsRate(invoiceMetaData.getRateAsPerIncometax());
		invoiceLineItemData.setTdsSection(invoiceMetaData.getTdsSection());
		invoiceLineItemData.setInvoiceNpId(sectionDeterminationDTO.getNature());
		invoiceLineItemData.setIsError(false);
		invoiceLineItemData.setInvoiceValue(invoiceMetaData.getInvoiceValue());
		invoiceLineItemData.setSaaNumber(invoiceMetaData.getSaaNumber());
		invoiceLineItemData.setTdsRemittancedate(invoiceMetaData.getDateOfDepositOfTax());
		invoiceLineItemData.setDebitCreditIndicator(invoiceMetaData.getDebitCreditIndicator());
		invoiceLineItemData.setItemCode(invoiceMetaData.getItemCode());
		invoiceLineItemData.setTypeOfTransaction(invoiceMetaData.getTypeOfTransaction());
		invoiceLineItemData.setPoItemNo(invoiceMetaData.getPoItemNo());
		invoiceLineItemData.setTdsBaseValue(invoiceMetaData.getTdsBaseValue());
		invoiceLineItemData.setAssignmentNumber(invoiceMetaData.getAssignmentNumber());
		invoiceLineItemData.setProfitCenter(invoiceMetaData.getProfitCenter());
		invoiceLineItemData.setPlant(invoiceMetaData.getPlant());
		invoiceLineItemData.setBusinessArea(invoiceMetaData.getBusinessArea());
		invoiceLineItemData.setBusinessPlace(invoiceMetaData.getBusinessPlace());
		invoiceLineItemData.setRefKey3(invoiceMetaData.getRefKey3());
		invoiceLineItemData.setTdsTaxCodeErp(invoiceMetaData.getTdsTaxCodeErp());
		invoiceLineItemData.setPos(invoiceMetaData.getPos());
		invoiceLineItemData.setSgstRate(invoiceMetaData.getSgstRate());
		invoiceLineItemData.setSgstAmount(invoiceMetaData.getSgstAmount());
		invoiceLineItemData.setCgstRate(invoiceMetaData.getSgstRate());
		invoiceLineItemData.setCgstAmount(invoiceMetaData.getCgstAmount());
		invoiceLineItemData.setIgstRate(invoiceMetaData.getIgstRate());
		invoiceLineItemData.setIgstAmount(invoiceMetaData.getIgstAmount());
		invoiceLineItemData.setGlAccountCode(invoiceMetaData.getGlAccountCode());
		invoiceLineItemData.setMigoNumber(invoiceMetaData.getMigoNumber());
		invoiceLineItemData.setMiroNumber(invoiceMetaData.getMiroNumber());
		invoiceLineItemData.setGstin(invoiceMetaData.getDeducteeGstin());
		invoiceLineItemData.setDeductorGstin(invoiceMetaData.getDeductorGstin());
		invoiceLineItemData.setActionType("");
		if ("CAN".equalsIgnoreCase(invoiceMetaData.getSupplyType())) {
			invoiceLineItemData.setDerivedTdsSection(invoiceMetaData.getTdsSection());
			invoiceLineItemData.setDerivedTdsRate(invoiceMetaData.getRateAsPerIncometax());
		} else if (StringUtils.isNotBlank(derivedSection) && !"NAD".equalsIgnoreCase(derivedSection)) {
			invoiceLineItemData.setDerivedTdsSection(derivedSection);
			invoiceLineItemData.setConfidence(sectionDeterminationDTO.getConfidence());
			invoiceLineItemData.setDerivedTdsRate(rate);
			if (!invoiceMetaData.getHasDtaa()) {
				invoiceLineItemData.setSurcharge(surcharge);
				invoiceLineItemData.setCessAmount(cessAmount);
				invoiceLineItemData.setCessRate(cessRate);
			}
			// TDS rate or dtaa rate
			invoiceLineItemData.setTdsRateLdc(rate);
		} else {
			invoiceLineItemData.setDerivedTdsSection("NAD");
			invoiceLineItemData.setConfidence("");
			invoiceLineItemData.setDerivedTdsRate(BigDecimal.ZERO);
		}
		invoiceLineItemData.setHasDtaa(invoiceMetaData.getHasDtaa());
		invoiceLineItemData.setDocumentDate(invoiceMetaData.getVendorDocumentDate());
		invoiceLineItemData.setInvoiceAmount(invoiceMetaData.getAmountInInr());
		invoiceLineItemData.setServiceDescriptionInvoice(keyword);
		invoiceLineItemData.setServiceDescriptionPo(keyword);
		invoiceLineItemData.setServiceDescriptionGl(keyword);

		invoiceLineItemData.setInterest(invoiceMetaData.getInterest());
		invoiceLineItemData.setPoDate(invoiceMetaData.getPoDate());
		invoiceLineItemData.setPoNumber(invoiceMetaData.getPoNumber());
		invoiceLineItemData.setDeducteeTin(invoiceMetaData.getTin());
		// amount calculation
		invoiceLineItemData.setDerivedTdsAmount(invoiceLineItemData.getDerivedTdsRate()
				.multiply(invoiceLineItemData.getInvoiceAmount()).divide(BigDecimal.valueOf(100)));
		invoiceLineItemData.setTdsAmount(invoiceMetaData.getTdsAmount());
		invoiceLineItemData.setClientAmount(invoiceMetaData.getTdsAmount());
		invoiceLineItemData.setCreatedDate(new Timestamp(new Date().getTime()));
		invoiceLineItemData.setCreatedBy(batchUpload.getCreatedBy());
		invoiceLineItemData.setPan(invoiceMetaData.getDeducteePan());
		invoiceLineItemData.setDeductorPan(invoiceMetaData.getDeductorPan());
		invoiceLineItemData.setDocumentNumber(invoiceMetaData.getErpDocumentNo());
		invoiceLineItemData.setVendorInvoiceNumber(invoiceMetaData.getVendorDocumentNo());
		invoiceLineItemData.setLineItemNumber(Integer.valueOf(invoiceMetaData.getLineItemNumber()));
		invoiceLineItemData.setOriginalDocumentNumber(invoiceMetaData.getOriginalDocumentNumber());
		invoiceLineItemData.setSupplyType(invoiceMetaData.getSupplyType());
		invoiceLineItemData.setIsParent(false);
		invoiceLineItemData.setIsKeyDuplicate(false);
		invoiceLineItemData.setChallanPaid(false);
		invoiceLineItemData.setIsChallanGenerated(false);
		invoiceLineItemData.setHasProvision(false);
		invoiceLineItemData.setHasAdvance(false);
		invoiceLineItemData.setApprovedForChallan(false);
		invoiceLineItemData.setIsMismatch(false);
		invoiceLineItemData.setIsExempted(false);
		invoiceLineItemData.setLinkedAdvanceNumber(invoiceMetaData.getLinkedAdvanceIndicator());
		invoiceLineItemData.setLinkedProvisionNumber(invoiceMetaData.getLinkedProvisionIndicator());
		String category = StringUtils.EMPTY;
		if (!"CAN".equalsIgnoreCase(invoiceMetaData.getSupplyType())) {
			category = getMismatchCategory(invoiceLineItemData.getClientSection(),
					invoiceMetaData.getRateAsPerIncometax(), invoiceLineItemData.getClientAmount(), derivedSection,
					invoiceLineItemData.getDerivedTdsRate(), invoiceLineItemData.getDerivedTdsAmount(), roundingOff);
			invoiceLineItemData.setMismatchCategory(category);
		}
		// Final Tds calculation
		if (StringUtils.isAllBlank(category) || "CAN".equalsIgnoreCase(invoiceMetaData.getSupplyType())) {
			invoiceLineItemData.setFinalTdsSection(invoiceLineItemData.getDerivedTdsSection());
			invoiceLineItemData.setFinalTdsAmount(invoiceLineItemData.getDerivedTdsAmount());
			invoiceLineItemData.setFinalTdsRate(invoiceLineItemData.getDerivedTdsRate());
			invoiceLineItemData.setMismatchCategory("SM-RM");
			invoiceLineItemData.setActive(true);
		} else {
			num.increment();
			invoiceLineItemData.setActive(false);
			invoiceLineItemData.setIsMismatch(true);
		}
		invoiceLineItemData.setProvisionCanAdjust(true);
		invoiceLineItemData.setAdvanceCanAdjust(true);
		if (StringUtils.isNotBlank(invoiceMetaData.getProvisionCanAdjust())
				&& !"Y".equalsIgnoreCase(invoiceMetaData.getProvisionCanAdjust())) {
			invoiceLineItemData.setProvisionCanAdjust(false);
		}
		if (StringUtils.isNotBlank(invoiceMetaData.getAdvanceCanAdjust())
				&& !"Y".equalsIgnoreCase(invoiceMetaData.getAdvanceCanAdjust())) {
			invoiceLineItemData.setAdvanceCanAdjust(false);
		}
		invoiceLineItemData.setHasLdc(false);
		invoiceLineItemData.setDocumentType(invoiceMetaData.getDocumentType());
		invoiceLineItemData.setIsResident("Y");
		invoiceLineItemData.setDeductorMasterTan(invoiceMetaData.getDeductorMasterTan());
		invoiceLineItemData.setAssessmentYear(invoiceMetaData.getAssessmentYear());
		invoiceLineItemData.setDocumentPostingDate(invoiceMetaData.getDocumentPostingDate());
		invoiceLineItemData.setTdsDeductionDate(invoiceMetaData.getDateOfDeductionOfTax());
		invoiceLineItemData.setPaymentDate(invoiceMetaData.getDateOfDepositOfTax());
		invoiceLineItemData.setUserDefinedField1(invoiceMetaData.getUserDefinedField1());
		invoiceLineItemData.setUserDefinedField2(invoiceMetaData.getUserDefinedField2());
		invoiceLineItemData.setUserDefinedField3(invoiceMetaData.getUserDefinedField3());
		Calendar cal = Calendar.getInstance();
		cal.setTime(invoiceLineItemData.getDocumentPostingDate());
		int postingDateMonth = cal.get(Calendar.MONTH) + 1;
		invoiceLineItemData.setAssessmentMonth(postingDateMonth);
		invoiceLineItemData.setChallanMonth(challanMonth);
		batchInvoiceSave.add(invoiceLineItemData);
		return invoiceLineItemData;
	}

	private ProvisionDTO createProvision(BatchUpload batchUpload, BigDecimal rate, String derivedSection,
			NrTransactionsMeta provisionMetaData, SectionDeterminationDTO sectionDeterminationDTO, int challanMonth,
			List<ProvisionDTO> batchProvisionSave, String roundingOff, LongAdder num, BigDecimal surcharge,
			BigDecimal cessRate, BigDecimal cessAmount) {
		ProvisionDTO provisionData = new ProvisionDTO();
		String keyword = provisionMetaData.getNatureOfPayment().trim();
		provisionData.setNrTransactionsMetaId(provisionMetaData.getId());
		provisionData.setProcessedFrom(UploadTypes.PROVISION_NR_EXCEL.name());
		provisionData.setSourceIdentifier(provisionMetaData.getSourceIdentifier());
		provisionData.setSourceFileName(provisionMetaData.getSourceFilename());
		provisionData.setBatchUploadId(batchUpload.getBatchUploadID());
		provisionData.setDeducteeName(provisionMetaData.getDeducteeName());
		provisionData.setDeducteeCode(provisionMetaData.getDeducteeCode());
		String deducteeKey = getDeducteeKey(provisionMetaData.getDeducteeCode(), provisionMetaData.getDeducteeName(),
				provisionMetaData.getDeducteePan());
		provisionData.setDeducteeKey(deducteeKey);
		provisionData.setClientRate(provisionMetaData.getRateAsPerIncometax());
		// Client effect rate calculation
		BigDecimal clientEffectiveAmount = provisionMetaData.getTdsAmount().add(provisionMetaData.getSurcharge())
				.add(provisionMetaData.getEductaionCess());
		BigDecimal clientEffectiveRate = (clientEffectiveAmount.multiply(new BigDecimal(100)))
				.divide(provisionMetaData.getAmountInInr());
		provisionData.setClientEffectiveTdsRate(clientEffectiveRate);
		provisionData.setSection(provisionMetaData.getTdsSection());
		provisionData.setWithholdingSection(provisionMetaData.getTdsSection());
		provisionData.setRate(provisionMetaData.getRateAsPerIncometax());
		provisionData.setProvisionNpId(sectionDeterminationDTO.getNature());
		provisionData.setIsError(false);
		provisionData.setActionType("");
		if ("CAN".equalsIgnoreCase(provisionMetaData.getSupplyType())) {
			provisionData.setDerivedTdsSection(provisionMetaData.getTdsSection());
			provisionData.setDerivedTdsRate(provisionMetaData.getRateAsPerIncometax());
		} else if (StringUtils.isNotBlank(derivedSection) && !"NAD".equalsIgnoreCase(derivedSection)) {
			provisionData.setDerivedTdsSection(derivedSection);
			provisionData.setConfidence(sectionDeterminationDTO.getConfidence());
			provisionData.setDerivedTdsRate(rate);
			if (!provisionMetaData.getHasDtaa()) {
				provisionData.setSurcharge(surcharge);
				provisionData.setCessAmount(cessAmount);
				provisionData.setCessRate(cessRate);
			}
			// TDS rate or dtaa rate
			provisionData.setTdsRate(rate);
		} else {
			provisionData.setDerivedTdsSection("NAD");
			provisionData.setConfidence("");
			provisionData.setDerivedTdsRate(BigDecimal.ZERO);
		}
		provisionData.setHasDtaa(provisionMetaData.getHasDtaa());
		provisionData.setDocumentDate(provisionMetaData.getVendorDocumentDate());
		provisionData.setProvisionalAmount(provisionMetaData.getAmountInInr());
		provisionData.setServiceDescription(keyword);
		provisionData.setServiceDescriptionPo(keyword);
		provisionData.setServiceDescriptionGl(keyword);

		provisionData.setInterest(provisionMetaData.getInterest());
		provisionData.setPoDate(provisionMetaData.getPoDate());
		provisionData.setPoNumber(provisionMetaData.getPoNumber());
		provisionData.setDeducteeTin(provisionMetaData.getTin());
		// amount calculation
		provisionData.setDerivedTdsAmount(provisionData.getDerivedTdsRate()
				.multiply(provisionData.getProvisionalAmount()).divide(BigDecimal.valueOf(100)));
		provisionData.setWithholdingAmount(provisionMetaData.getTdsAmount());
		provisionData.setClientAmount(provisionMetaData.getTdsAmount());
		provisionData.setCreatedDate(new Timestamp(new Date().getTime()));
		provisionData.setCreatedBy(batchUpload.getCreatedBy());
		provisionData.setDeducteePan(provisionMetaData.getDeducteePan());
		provisionData.setDeductorPan(provisionMetaData.getDeductorPan());
		provisionData.setDocumentNumber(provisionMetaData.getErpDocumentNo());
		provisionData.setLineItemNumber(String.valueOf(provisionMetaData.getLineItemNumber()));
		provisionData.setSupplyType(provisionMetaData.getSupplyType());
		provisionData.setIsParent(false);
		provisionData.setChallanPaid(false);
		provisionData.setIsChallanGenerated(false);
		provisionData.setApprovedForChallan(false);
		provisionData.setMismatch(false);
		provisionData.setIsExempted(false);
		String category = StringUtils.EMPTY;
		if (!"CAN".equalsIgnoreCase(provisionMetaData.getSupplyType())) {
			category = getMismatchCategory(provisionData.getClientSection(), provisionMetaData.getRateAsPerIncometax(),
					provisionData.getClientAmount(), derivedSection, provisionData.getDerivedTdsRate(),
					provisionData.getDerivedTdsAmount(), roundingOff);
			provisionData.setMismatchCategory(category);
		}

		// Final Tds calculation
		if (StringUtils.isAllBlank(category) || "CAN".equalsIgnoreCase(provisionMetaData.getSupplyType())) {
			provisionData.setFinalTdsSection(provisionData.getDerivedTdsSection());
			provisionData.setFinalTdsAmount(provisionData.getDerivedTdsAmount());
			provisionData.setFinalTdsRate(provisionData.getDerivedTdsRate());
			provisionData.setMismatchCategory("SM-RM");
			provisionData.setActive(true);
		} else {
			num.increment();
			provisionData.setActive(false);
			provisionData.setMismatch(true);
		}
		provisionData.setHasLdc(false);
		provisionData.setDocumentType(provisionMetaData.getDocumentType());
		provisionData.setIsResident("Y");
		provisionData.setDeductorTan(provisionMetaData.getDeductorMasterTan());
		provisionData.setDeductorMasterTan(provisionMetaData.getDeductorMasterTan());
		provisionData.setAssessmentYear(provisionMetaData.getAssessmentYear());
		provisionData.setPostingDateOfDocument(provisionMetaData.getDocumentPostingDate());
		provisionData.setTdsDeductionDate(provisionMetaData.getDateOfDeductionOfTax());
		provisionData.setPaymentDate(provisionMetaData.getDateOfDepositOfTax());
		Calendar cal = Calendar.getInstance();
		cal.setTime(provisionData.getPostingDateOfDocument());
		int postingDateMonth = cal.get(Calendar.MONTH) + 1;
		provisionData.setAssessmentMonth(postingDateMonth);
		provisionData.setChallanMonth(challanMonth);
		batchProvisionSave.add(provisionData);
		return provisionData;
	}

	private AdvanceDTO createAdvance(BatchUpload batchUpload, BigDecimal rate, String derivedSection,
			NrTransactionsMeta advanceMetaData, SectionDeterminationDTO sectionDeterminationDTO, int challanMonth,
			List<AdvanceDTO> batchAdvanceSave, String roundingOff, LongAdder num, BigDecimal surcharge,
			BigDecimal cessRate, BigDecimal cessAmount) {
		AdvanceDTO advanceData = new AdvanceDTO();
		String keyword = advanceMetaData.getNatureOfPayment().trim();
		advanceData.setNrTransactionsMetaId(advanceMetaData.getId());
		advanceData.setProcessedFrom(UploadTypes.ADVANCE_NR_EXCEL.name());
		advanceData.setSourceIdentifiers(advanceMetaData.getSourceIdentifier());
		advanceData.setSourceFileName(advanceMetaData.getSourceFilename());
		advanceData.setBatchUploadId(batchUpload.getBatchUploadID());
		advanceData.setDeducteeName(advanceMetaData.getDeducteeName());
		advanceData.setDeducteeCode(advanceMetaData.getDeducteeCode());
		String deducteeKey = getDeducteeKey(advanceMetaData.getDeducteeCode(), advanceMetaData.getDeducteeName(),
				advanceMetaData.getDeducteePan());
		advanceData.setDeducteeKey(deducteeKey);
		advanceData.setWithholdingRate(advanceMetaData.getRateAsPerIncometax());
		// Client effect rate calculation
		BigDecimal clientEffectiveAmount = advanceMetaData.getTdsAmount().add(advanceMetaData.getSurcharge())
				.add(advanceMetaData.getEductaionCess());
		BigDecimal clientEffectiveRate = (clientEffectiveAmount.multiply(new BigDecimal(100)))
				.divide(advanceMetaData.getAmountInInr());
		advanceData.setClientEffectiveTdsRate(clientEffectiveRate);
		advanceData.setWithholdingSection(advanceMetaData.getTdsSection());
		advanceData.setSection(advanceMetaData.getTdsSection());
		advanceData.setAdvanceNpId(sectionDeterminationDTO.getNature());
		advanceData.setIsError(false);
		advanceData.setAction("");
		if ("CAN".equalsIgnoreCase(advanceMetaData.getSupplyType())) {
			advanceData.setDerivedTdsSection(advanceMetaData.getTdsSection());
			advanceData.setDerivedTdsRate(advanceMetaData.getRateAsPerIncometax());
		} else if (StringUtils.isNotBlank(derivedSection) && !"NAD".equalsIgnoreCase(derivedSection)) {
			advanceData.setDerivedTdsSection(derivedSection);
			advanceData.setConfidence(sectionDeterminationDTO.getConfidence());
			advanceData.setDerivedTdsRate(rate);
			if (!advanceMetaData.getHasDtaa()) {
				advanceData.setSurcharge(surcharge);
				advanceData.setCessAmount(cessAmount);
				advanceData.setCessRate(cessRate);
			}
			// TDS rate or dtaa rate
			advanceData.setTdsRate(rate);
		} else {
			advanceData.setDerivedTdsSection("NAD");
			advanceData.setConfidence("");
			advanceData.setDerivedTdsRate(BigDecimal.ZERO);
		}
		advanceData.setHasDtaa(advanceMetaData.getHasDtaa());
		advanceData.setDocumentDate(advanceMetaData.getVendorDocumentDate());
		advanceData.setAmount(advanceMetaData.getAmountInInr());
		advanceData.setServiceDescription(keyword);
		advanceData.setServiceDescriptionPo(keyword);
		advanceData.setServiceDescriptionGl(keyword);

		advanceData.setInterest(advanceMetaData.getInterest());
		advanceData.setPoDate(advanceMetaData.getPoDate());
		advanceData.setPoNumber(advanceMetaData.getPoNumber());
		advanceData.setDeducteeTin(advanceMetaData.getTin());
		// amount calculation
		advanceData.setDerivedTdsAmount(
				advanceData.getDerivedTdsRate().multiply(advanceData.getAmount()).divide(BigDecimal.valueOf(100)));
		advanceData.setClientAmount(advanceMetaData.getTdsAmount());
		advanceData.setCreatedDate(new Timestamp(new Date().getTime()));
		advanceData.setCreatedBy(batchUpload.getCreatedBy());
		advanceData.setDeducteePan(advanceMetaData.getDeducteePan());
		advanceData.setDeductorPan(advanceMetaData.getDeductorPan());
		advanceData.setDocumentNumber(advanceMetaData.getErpDocumentNo());
		advanceData.setLineItemNumber(String.valueOf(advanceMetaData.getLineItemNumber()));
		advanceData.setSupplyType(advanceMetaData.getSupplyType());
		advanceData.setIsParent(false);
		advanceData.setChallanPaid(false);
		advanceData.setIsChallanGenerated(false);
		advanceData.setApprovedForChallan(false);
		advanceData.setMismatch(false);
		advanceData.setIsExempted(false);
		String category = StringUtils.EMPTY;
		if (!"CAN".equalsIgnoreCase(advanceMetaData.getSupplyType())) {
			category = getMismatchCategory(advanceData.getSection(), advanceMetaData.getRateAsPerIncometax(),
					advanceData.getClientAmount(), derivedSection, advanceData.getDerivedTdsRate(),
					advanceData.getDerivedTdsAmount(), roundingOff);
			advanceData.setMismatchCategory(category);
		}
		// Final Tds calculation
		if (StringUtils.isAllBlank(category) || "CAN".equalsIgnoreCase(advanceMetaData.getSupplyType())) {
			advanceData.setFinalTdsSection(advanceData.getDerivedTdsSection());
			advanceData.setFinalTdsAmount(advanceData.getDerivedTdsAmount());
			advanceData.setFinalTdsRate(advanceData.getDerivedTdsRate());
			advanceData.setMismatchCategory("SM-RM");
			advanceData.setActive(true);
		} else {
			num.increment();
			advanceData.setActive(false);
			advanceData.setMismatch(true);
		}
		advanceData.setHasLdc(false);
		advanceData.setDocumentType(advanceMetaData.getDocumentType());
		advanceData.setIsResident("Y");
		advanceData.setDeductorMasterTan(advanceMetaData.getDeductorMasterTan());
		advanceData.setAssessmentYear(advanceMetaData.getAssessmentYear());
		advanceData.setPostingDateOfDocument(advanceMetaData.getDocumentPostingDate());
		advanceData.setTdsDeductionDate(advanceMetaData.getDateOfDeductionOfTax());
		advanceData.setPaymentDate(advanceMetaData.getDateOfDepositOfTax());
		advanceData.setTdsRemittancedate(advanceMetaData.getDateOfDepositOfTax());
		advanceData.setDebitCreditIndicator(advanceMetaData.getDebitCreditIndicator());
		advanceData.setTdsBaseValue(advanceMetaData.getTdsBaseValue());
		advanceData.setAssignmentNumber(advanceMetaData.getAssignmentNumber());
		advanceData.setProfitCenter(advanceMetaData.getProfitCenter());
		advanceData.setPlant(advanceMetaData.getPlant());
		advanceData.setBusinessArea(advanceMetaData.getBusinessArea());
		advanceData.setBusinessPlace(advanceMetaData.getBusinessPlace());
		advanceData.setGlAccountCode(advanceMetaData.getGlAccountCode());
		advanceData.setDeductorGstin(advanceMetaData.getDeductorGstin());
		advanceData.setSupplyType(advanceMetaData.getSupplyType());
		Calendar cal = Calendar.getInstance();
		cal.setTime(advanceData.getPostingDateOfDocument());
		int postingDateMonth = cal.get(Calendar.MONTH) + 1;
		advanceData.setAssessmentMonth(postingDateMonth);
		advanceData.setChallanMonth(challanMonth);
		advanceData.setUserDefinedField1(advanceMetaData.getUserDefinedField1());
		advanceData.setUserDefinedField2(advanceMetaData.getUserDefinedField2());
		advanceData.setUserDefinedField3(advanceMetaData.getUserDefinedField3());
		advanceData.setUserDefinedField4(advanceMetaData.getUserDefinedField4());
		advanceData.setUserDefinedField5(advanceMetaData.getUserDefinedField5());
		advanceData.setLinkingOfInvoiceWithPo(advanceMetaData.getLinkingOfInvoiceWithPo());
		batchAdvanceSave.add(advanceData);
		return advanceData;
	}

	public NrTransactionsMeta updateDeducteeDetails(DeducteeMasterNonResidential deducteeMasterNonResidential,
			NrTransactionsMeta nrTransactionsMetaData, List<DeducteeMasterNonResidential> nrDeducteeBatchUpdate) {
		// details like TRC, Tenf are available in transaction and not available in
		// deductee master then update the deductee master.. vice versa.
		if (deducteeMasterNonResidential != null) {
			if (nrTransactionsMetaData.getDeducteeCode() != null) {
				deducteeMasterNonResidential.setDeducteeCode(nrTransactionsMetaData.getDeducteeCode());
			} else {
				nrTransactionsMetaData.setDeducteeCode(deducteeMasterNonResidential.getDeducteeCode());
			}
			if (deducteeMasterNonResidential.getDeducteeStatus() != null) {
				nrTransactionsMetaData.setDeducteeStatus(deducteeMasterNonResidential.getDeducteeStatus());
			}
			if (StringUtils.isNotBlank(nrTransactionsMetaData.getTin())) {
				deducteeMasterNonResidential.setDeducteeTin(nrTransactionsMetaData.getTin());
			} else {
				nrTransactionsMetaData.setTin(deducteeMasterNonResidential.getDeducteeTin());
			}
			if (nrTransactionsMetaData.getIsTrcAvailable() != null) {
				deducteeMasterNonResidential.setIsTRCAvailable(nrTransactionsMetaData.getIsTrcAvailable());
				deducteeMasterNonResidential.setTrcApplicableFrom(nrTransactionsMetaData.getIsTrcApplicableFrom());
				deducteeMasterNonResidential.setTrcApplicableTo(nrTransactionsMetaData.getIsTrcApplicableTo());
			} else {
				nrTransactionsMetaData.setIsTrcAvailable(deducteeMasterNonResidential.getIsTRCAvailable());
				nrTransactionsMetaData.setIsTrcApplicableFrom(deducteeMasterNonResidential.getTrcApplicableFrom());
				nrTransactionsMetaData.setIsTrcApplicableTo(deducteeMasterNonResidential.getTrcApplicableTo());
			}
			if (nrTransactionsMetaData.getIsTenfAvailable() != null) {
				deducteeMasterNonResidential.setIsTenFAvailable(nrTransactionsMetaData.getIsTenfAvailable());
				deducteeMasterNonResidential.setTenFApplicableFrom(nrTransactionsMetaData.getIsTenfApplicableFrom());
				deducteeMasterNonResidential.setTenFApplicableTo(nrTransactionsMetaData.getIsTenfApplicableTo());
			} else {
				nrTransactionsMetaData.setIsTenfAvailable(deducteeMasterNonResidential.getIsTenFAvailable());
				nrTransactionsMetaData.setIsTenfApplicableFrom(deducteeMasterNonResidential.getTenFApplicableFrom());
				nrTransactionsMetaData.setIsTenfApplicableTo(deducteeMasterNonResidential.getTenFApplicableTo());
			}
			if (nrTransactionsMetaData.getIsNoPeDocumentAvailable() != null) {
				deducteeMasterNonResidential
						.setNoPEDocumentAvailable(nrTransactionsMetaData.getIsNoPeDocumentAvailable());
				deducteeMasterNonResidential
						.setNoPEApplicableFrom(nrTransactionsMetaData.getNoPeDocumentApplicableFrom());
				deducteeMasterNonResidential.setNoPEApplicableTo(nrTransactionsMetaData.getNoPeDocumentApplicableTo());
			} else {
				nrTransactionsMetaData
						.setIsNoPeDocumentAvailable(deducteeMasterNonResidential.getNoPEDocumentAvailable());
				nrTransactionsMetaData
						.setNoPeDocumentApplicableFrom(deducteeMasterNonResidential.getNoPEApplicableFrom());
				nrTransactionsMetaData.setNoPeDocumentApplicableTo(deducteeMasterNonResidential.getNoPEApplicableTo());
			}
			if (nrTransactionsMetaData.getIsPeIndia() != null) {
				deducteeMasterNonResidential.setWhetherPEInIndia(nrTransactionsMetaData.getIsPeIndia());
				deducteeMasterNonResidential
						.setWhetherPEInIndiaApplicableFrom(nrTransactionsMetaData.getPeInIndiaFromDate());
				deducteeMasterNonResidential
						.setWhetherPEInIndiaApplicableTo(nrTransactionsMetaData.getPeInIndiaToDate());
			} else {
				nrTransactionsMetaData.setIsPeIndia(deducteeMasterNonResidential.getWhetherPEInIndia());
				nrTransactionsMetaData
						.setPeInIndiaFromDate(deducteeMasterNonResidential.getWhetherPEInIndiaApplicableFrom());
				nrTransactionsMetaData
						.setPeInIndiaToDate(deducteeMasterNonResidential.getWhetherPEInIndiaApplicableTo());
			}
			if (nrTransactionsMetaData.getIsFixedbaseAvailbleIndia() != null) {
				deducteeMasterNonResidential
						.setIsFixedbaseAvailbleIndia(nrTransactionsMetaData.getIsFixedbaseAvailbleIndia());
				deducteeMasterNonResidential
						.setFixedbaseAvailbleIndiaApplicableFrom(nrTransactionsMetaData.getIsFixedbaseApplicableFrom());
				deducteeMasterNonResidential
						.setFixedbaseAvailbleIndiaApplicableTo(nrTransactionsMetaData.getIsFixedbaseApplicableTo());
			} else {
				nrTransactionsMetaData
						.setIsFixedbaseAvailbleIndia(deducteeMasterNonResidential.getIsFixedbaseAvailbleIndia());
				nrTransactionsMetaData.setIsFixedbaseApplicableFrom(
						deducteeMasterNonResidential.getFixedbaseAvailbleIndiaApplicableFrom());
				nrTransactionsMetaData.setIsFixedbaseApplicableTo(
						deducteeMasterNonResidential.getFixedbaseAvailbleIndiaApplicableTo());
			}
			if (nrTransactionsMetaData.getIsNoFixedBaseDeclaration() != null) {
				deducteeMasterNonResidential
						.setIsNoFixedBaseDeclarationAvailable(nrTransactionsMetaData.getIsNoFixedBaseDeclaration());
				deducteeMasterNonResidential
						.setNoFixedBaseDeclarationFromDate(nrTransactionsMetaData.getNoFixedBaseDeclarationFromDate());
				deducteeMasterNonResidential
						.setNoFixedBaseDeclarationToDate(nrTransactionsMetaData.getNoFixedBaseDeclarationToDate());
			} else {
				nrTransactionsMetaData.setIsNoFixedBaseDeclaration(
						deducteeMasterNonResidential.getIsNoFixedBaseDeclarationAvailable());
				nrTransactionsMetaData.setNoFixedBaseDeclarationFromDate(
						deducteeMasterNonResidential.getNoFixedBaseDeclarationFromDate());
				nrTransactionsMetaData.setNoFixedBaseDeclarationToDate(
						deducteeMasterNonResidential.getNoFixedBaseDeclarationToDate());
			}
			if (nrTransactionsMetaData.getStayPeriodFinancialYear() != null) {
				deducteeMasterNonResidential
						.setStayPeriodFinancialYear(nrTransactionsMetaData.getStayPeriodFinancialYear());
			} else {
				nrTransactionsMetaData
						.setStayPeriodFinancialYear(deducteeMasterNonResidential.getStayPeriodFinancialYear());
			}
			if (nrTransactionsMetaData.getIsPoemOfDeductee() != null) {
				deducteeMasterNonResidential.setIsPOEMavailable(nrTransactionsMetaData.getIsPoemOfDeductee());
				deducteeMasterNonResidential.setPoemApplicableFrom(nrTransactionsMetaData.getPoemApplicableFromDate());
				deducteeMasterNonResidential.setPoemApplicableTo(nrTransactionsMetaData.getPoemApplicableToDate());
			} else {
				nrTransactionsMetaData.setIsPoemOfDeductee(deducteeMasterNonResidential.getIsPOEMavailable());
				nrTransactionsMetaData.setPoemApplicableFromDate(deducteeMasterNonResidential.getPoemApplicableFrom());
				nrTransactionsMetaData.setPoemApplicableToDate(deducteeMasterNonResidential.getPoemApplicableTo());
			}
			if (nrTransactionsMetaData.getIsBeneficialOwnershipOfDeclaration() != null) {
				deducteeMasterNonResidential.setIsBeneficialOwnershipOfDeclaration(
						nrTransactionsMetaData.getIsBeneficialOwnershipOfDeclaration());
				if (nrTransactionsMetaData.getBeneficialOwnerOfIncome() != null) {
					deducteeMasterNonResidential
							.setBeneficialOwnerOfIncome(nrTransactionsMetaData.getBeneficialOwnerOfIncome());
				}
			} else {
				nrTransactionsMetaData.setIsBeneficialOwnershipOfDeclaration(
						deducteeMasterNonResidential.getIsBeneficialOwnershipOfDeclaration());
				nrTransactionsMetaData
						.setBeneficialOwnerOfIncome(deducteeMasterNonResidential.getBeneficialOwnerOfIncome());
			}
			if (nrTransactionsMetaData.getWhetherIncomeReceived() != null) {
				deducteeMasterNonResidential.setIsPEamountReceived(nrTransactionsMetaData.getWhetherIncomeReceived());
			} else {
				nrTransactionsMetaData.setWhetherIncomeReceived(deducteeMasterNonResidential.getIsPEamountReceived());
			}
			if (nrTransactionsMetaData.getIsGrossedUp() != null) {
				deducteeMasterNonResidential.setIsGrossingUp(nrTransactionsMetaData.getIsGrossedUp());
			} else {
				nrTransactionsMetaData.setIsGrossedUp(deducteeMasterNonResidential.getIsGrossingUp());
			}
			if (nrTransactionsMetaData.getMliSlobConditionSatisifed() != null) {
				deducteeMasterNonResidential
						.setMliSlobConditionSatisifed(nrTransactionsMetaData.getMliSlobConditionSatisifed());
			} else {
				nrTransactionsMetaData
						.setMliSlobConditionSatisifed(deducteeMasterNonResidential.getMliSlobConditionSatisifed());
			}
			if (nrTransactionsMetaData.getMliPptConditionSatisifed() != null) {
				deducteeMasterNonResidential
						.setMliPptConditionSatisifed(nrTransactionsMetaData.getMliPptConditionSatisifed());
			} else {
				nrTransactionsMetaData
						.setMliPptConditionSatisifed(deducteeMasterNonResidential.getMliPptConditionSatisifed());
			}
			if (nrTransactionsMetaData.getIsMliOrPptSlob() != null) {
				deducteeMasterNonResidential.setIsMliPptSlob(nrTransactionsMetaData.getIsMliOrPptSlob());
			} else {
				nrTransactionsMetaData.setIsMliOrPptSlob(deducteeMasterNonResidential.getIsMliPptSlob());
			}
			if (nrTransactionsMetaData.getIsTrcFuture() != null) {
				deducteeMasterNonResidential.setIstrcFuture(nrTransactionsMetaData.getIsTrcFuture());
				deducteeMasterNonResidential.setTrcFutureDate(nrTransactionsMetaData.getTrcFutureDate());
			} else {
				nrTransactionsMetaData.setIsTrcFuture(deducteeMasterNonResidential.getIstrcFuture());
				nrTransactionsMetaData.setTrcFutureDate(deducteeMasterNonResidential.getTrcFutureDate());
			}
			if (nrTransactionsMetaData.getIsTenfFuture() != null) {
				deducteeMasterNonResidential.setIstenfFuture(nrTransactionsMetaData.getIsTenfFuture());
				deducteeMasterNonResidential.setTenfFutureDate(nrTransactionsMetaData.getTenfFutureDate());
			} else {
				nrTransactionsMetaData.setIsTenfFuture(deducteeMasterNonResidential.getIstrcFuture());
				nrTransactionsMetaData.setTenfFutureDate(deducteeMasterNonResidential.getTenfFutureDate());
			}
			if (nrTransactionsMetaData.getNoFixedBaseDeclarationAvailableInFuture() != null) {
				deducteeMasterNonResidential.setNoFixedBaseDeclarationAvailableInFuture(
						nrTransactionsMetaData.getNoFixedBaseDeclarationAvailableInFuture());
				deducteeMasterNonResidential.setNoFixedBaseDeclarationAvailableFutureDate(
						nrTransactionsMetaData.getNoFixedBaseDeclarationAvailableFutureDate());
			} else {
				nrTransactionsMetaData.setNoFixedBaseDeclarationAvailableInFuture(
						deducteeMasterNonResidential.getNoFixedBaseDeclarationAvailableInFuture());
				nrTransactionsMetaData.setNoFixedBaseDeclarationAvailableFutureDate(
						deducteeMasterNonResidential.getNoFixedBaseDeclarationAvailableFutureDate());
			}

			if (nrTransactionsMetaData.getIsNoPoemDeclarationInFuture() != null) {
				deducteeMasterNonResidential
						.setIsPoemDeclaration(nrTransactionsMetaData.getIsNoPoemDeclarationInFuture());
				deducteeMasterNonResidential.setPoemFutureDate(nrTransactionsMetaData.getNoPoemFutureDate());
			} else {
				nrTransactionsMetaData
						.setIsNoPoemDeclarationInFuture(deducteeMasterNonResidential.getIsPoemDeclaration());
				nrTransactionsMetaData.setNoPoemFutureDate(deducteeMasterNonResidential.getPoemFutureDate());
			}
			if (nrTransactionsMetaData.getDeducteeGstin() != null) {
				deducteeMasterNonResidential.setDeducteeGSTIN(nrTransactionsMetaData.getDeducteeGstin());
			} else {
				nrTransactionsMetaData.setDeducteeGstin(deducteeMasterNonResidential.getDeducteeGSTIN());
			}
			if (nrTransactionsMetaData.getIsNoPoemAvailable() != null) {
				deducteeMasterNonResidential
						.setIsNoPOEMDeclarationAvailable(nrTransactionsMetaData.getIsNoPoemAvailable());
				deducteeMasterNonResidential
						.setNoPOEMDeclarationApplicableFromDate(nrTransactionsMetaData.getIsNoPoemApplicableFrom());
				deducteeMasterNonResidential
						.setNoPOEMDeclarationApplicableToDate(nrTransactionsMetaData.getIsNoPoemApplicableTo());
			} else {
				nrTransactionsMetaData.setIsNoPoemAvailable(deducteeMasterNonResidential.getIsNoPOEMDeclarationAvailable());
				nrTransactionsMetaData.setIsNoPoemApplicableFrom(
						deducteeMasterNonResidential.getNoPOEMDeclarationApplicableFromDate());
				nrTransactionsMetaData
						.setIsNoPoemApplicableTo(deducteeMasterNonResidential.getNoPOEMDeclarationApplicableToDate());
			}

			// update Deductee Master Non Residential table
			nrDeducteeBatchUpdate.add(deducteeMasterNonResidential);
		}
		return nrTransactionsMetaData;
	}

	/**
	 * 
	 * @param sectionList
	 * @param postingDocumentDate
	 * @param nrTransactionsMetaData
	 * @return
	 * @throws ParseException
	 */
	private BigDecimal getTdsMasterDetails(List<Map<String, Object>> sectionList, Date postingDocumentDate,
			NrTransactionsMeta nrTransactionsMetaData) throws ParseException {
		BigDecimal tdsRate = BigDecimal.ZERO;
		for (Map<String, Object> nopData : sectionList) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String rateApplicableFrom = (String) nopData.get("applicableFrom");
			String rateApplicableTo = (String) nopData.get("applicableTo");
			Date applicableFrom = formatter.parse(rateApplicableFrom);
			Date applicableTo = null;
			if (StringUtils.isNotBlank(rateApplicableTo)) {
				applicableTo = formatter.parse(rateApplicableTo);
			}
			Integer perTransactionLimit = (Integer) nopData.get("perTransactionLimit");
			Double rate = (Double) nopData.get("rate");
			Integer isPerTransactionLimit = (Integer) nopData.get("isPerTransactionLimit");
			boolean isPerTransactionApplicable = isPerTransactionLimit != null && isPerTransactionLimit.equals(1) ? true
					: false;
			boolean isValid = false;
			applicableFrom = CommonUtil.getMonthStartDate(applicableFrom);
			postingDocumentDate = CommonUtil.getMonthStartDate(postingDocumentDate);
			if (postingDocumentDate.getTime() >= applicableFrom.getTime() && applicableTo == null) {
				isValid = true;
			} else if (postingDocumentDate.getTime() >= applicableFrom.getTime() && applicableTo != null) {
				postingDocumentDate = CommonUtil.getMonthEndDate(postingDocumentDate);
				applicableTo = CommonUtil.getMonthEndDate(applicableTo);
				if (applicableTo.getTime() >= postingDocumentDate.getTime()) {
					isValid = true;
				}
			}
			if (isValid) {
				if (isPerTransactionApplicable) {
					if (nrTransactionsMetaData.getAmountInInr().doubleValue() > perTransactionLimit.doubleValue()) {
						tdsRate = BigDecimal.valueOf(rate);
					}
				} else {
					tdsRate = BigDecimal.valueOf(rate);
				}
				break;
			}
		}
		return tdsRate;
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
	public File prepareNonResidentErrorFile(String originalFileName, String deductorTan, String deductorPan,
			ArrayList<NrExcelErrorDTO> errorList, String uploadType) throws Exception {
		try {
			ArrayList<String> headers = new ArrayList<>();
			if (UploadTypes.INVOICE_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
				headers = (ArrayList<String>) errorReportService.getNRInvoiceHeaderFields();
			} else if (UploadTypes.ADVANCE_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
				headers = (ArrayList<String>) errorReportService.getNRAdvanceHeaderFields();
			} else {
				headers = (ArrayList<String>) errorReportService.getNRProvisionHeaderFields();
			}
			headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = nonResidentXlsxReport(errorList, deductorTan, deductorPan, headers, uploadType);
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
	public Workbook nonResidentXlsxReport(List<NrExcelErrorDTO> errorDTOs, String deductorTan, String deductorPan,
			ArrayList<String> headerNames, String uploadType) throws Exception {
		String tenantId = MultiTenantContext.getTenantId();
		logger.info("tenant Id :{}", tenantId);
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (UploadTypes.INVOICE_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			setExtractDataForNonResidentInvoice(errorDTOs, worksheet, deductorTan, headerNames);
		} else if (UploadTypes.ADVANCE_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			setExtractDataForNonResidentAdvance(errorDTOs, worksheet, deductorTan, headerNames);
		} else {
			setExtractDataForNonResidentProvision(errorDTOs, worksheet, deductorTan, headerNames);
		}
		String range = "DA6";
		if (UploadTypes.INVOICE_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			range = "EK6";
		} else if (UploadTypes.ADVANCE_NR_EXCEL.name().equalsIgnoreCase(uploadType)) {
			range = "DT6";
		}

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

		// Style for D6 to ES6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:" + range);
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		// feign client for get Deductor Name
		DeductorMasterDTO deductorData = onboardingClient.getDeductorByPan(deductorPan, tenantId).getBody().getData();

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		if (deductorData != null) {
			cellA2.setValue("Client Name:" + deductorData.getDeductorName());
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
		String lastHeaderCellName = range;
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:" + range);
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
	public void setExtractDataForNonResidentInvoice(List<NrExcelErrorDTO> errorDTOs, Worksheet worksheet,
			String deductorTan, List<String> headerNames) throws Exception {
		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			for (int i = 0; i < errorDTOs.size(); i++) {
				NrExcelErrorDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = new ArrayList<>();
				rowData.add(StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.add(StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.add(errorDTO.getSerialNumber());
				rowData.add(errorDTO.getSourceIdentifier());
				rowData.add(errorDTO.getSourceFilename());
				rowData.add(errorDTO.getDeductorCode());
				rowData.add(errorDTO.getDeductorName());
				rowData.add(errorDTO.getDeductorPan());
				rowData.add(errorDTO.getDeductorMasterTan());
				rowData.add(errorDTO.getDeductorGstin());
				rowData.add(errorDTO.getDeducteeCode());
				rowData.add(errorDTO.getDeducteeName());
				rowData.add(errorDTO.getDeducteePan());
				rowData.add(errorDTO.getTin());
				rowData.add(errorDTO.getDeducteeGstin());
				rowData.add(errorDTO.getVendorDocumentNo()); // VendorInvoiceNumber
				rowData.add(errorDTO.getVendorDocumentDate());// DocumentDate
				rowData.add(errorDTO.getErpDocumentNo());
				rowData.add(errorDTO.getDocumentPostingDate());
				rowData.add(errorDTO.getStringOfPaymentOrCredit());// PaymentDate
				rowData.add(errorDTO.getDateOfDeductionOfTax()); // TDSDeductionDate
				rowData.add(errorDTO.getDocumentType());// DocumentType
				rowData.add(errorDTO.getSupplyType());
				rowData.add(errorDTO.getMigoNumber());
				rowData.add(errorDTO.getMiroNumber());
				rowData.add(errorDTO.getErpDocumentType());
				rowData.add(errorDTO.getLineItemNumber());
				rowData.add(errorDTO.getHsnOrSac());
				rowData.add(errorDTO.getHsnOrSacDesc());
				rowData.add(errorDTO.getInvoiceDesc());// InvoiceDesc
				rowData.add(errorDTO.getGlAccountCode());
				rowData.add(errorDTO.getGlAccountName());
				rowData.add(errorDTO.getPoNumber());
				rowData.add(errorDTO.getPoDate());
				rowData.add(errorDTO.getPoDesc());
				rowData.add(errorDTO.getAmountInInr());// TaxableValue
				rowData.add(errorDTO.getIgstRate());
				rowData.add(errorDTO.getIgstAmount());
				rowData.add(errorDTO.getCgstRate());
				rowData.add(errorDTO.getCgstAmount());
				rowData.add(errorDTO.getSgstRate());
				rowData.add(errorDTO.getSgstAmount());
				rowData.add(errorDTO.getCessRate());
				rowData.add(errorDTO.getCessAmount());
				rowData.add(errorDTO.getPos());
				rowData.add(errorDTO.getTdsTaxCodeErp());
				rowData.add(errorDTO.getTdsSection());
				rowData.add(errorDTO.getRateAsPerIncometax());// TDSRate
				rowData.add(errorDTO.getTdsAmount());
				rowData.add(errorDTO.getLinkedAdvanceIndicator());
				rowData.add(errorDTO.getLinkedProvisionIndicator());
				rowData.add(errorDTO.getProvisionCanAdjust());// ProvisionAdjustmentFlag
				rowData.add(errorDTO.getAdvanceCanAdjust());// AdvanceAdjustmentFlag
				rowData.add(errorDTO.getChallanPaid());
				rowData.add(errorDTO.getChallanGeneratedDate());// ChallanProcessingDate
				rowData.add(errorDTO.getIsGrossedUp());
				rowData.add(errorDTO.getOriginalDocumentNumber());
				rowData.add(errorDTO.getOriginalDocumentDate());
				rowData.add(errorDTO.getRefKey3());
				rowData.add(errorDTO.getBusinessPlace());
				rowData.add(errorDTO.getBusinessArea());
				rowData.add(errorDTO.getPlant());
				rowData.add(errorDTO.getProfitCenter());
				rowData.add(errorDTO.getAssignmentNumber());
				rowData.add(errorDTO.getTdsBaseValue());
				rowData.add(errorDTO.getPoItemNo());
				rowData.add(errorDTO.getTypeOfTransaction());
				rowData.add(errorDTO.getUserName());
				rowData.add(errorDTO.getAmountInForeignCurrency());
				rowData.add(errorDTO.getExchangeRate());
				rowData.add(errorDTO.getCurrency());
				rowData.add(errorDTO.getItemCode());
				rowData.add(errorDTO.getInvoiceValue());
				rowData.add(errorDTO.getSaaNumber());
				rowData.add(errorDTO.getIsResident());// isResident
				rowData.add(errorDTO.getTdsRemittanceDate());
				rowData.add(errorDTO.getDebitCreditIndicator());
				rowData.add(errorDTO.getUserDefinedField1());
				rowData.add(errorDTO.getUserDefinedField2());
				rowData.add(errorDTO.getUserDefinedField3());
				//rowData.add(errorDTO.getDeducteeStatus());
				rowData.add(errorDTO.getCountry());// DeducteeCountry
				rowData.add(errorDTO.getNatureOfPayment());// NatureOfPayment
				rowData.add(errorDTO.getNatureOfRemittance());// NatureOfRemittance
				rowData.add(errorDTO.getDetailedDescription());
				rowData.add(errorDTO.getAmountOfIncometax()); // IncomeOnWhichTaxIsToBeDeducted
				rowData.add(errorDTO.getSurcharge());
				rowData.add(errorDTO.getEductaionCess());
				rowData.add(errorDTO.getInterest());
				rowData.add(errorDTO.getEductaionFee());// fee
				//rowData.add(errorDTO.getIsLdcApplied());
				//rowData.add(errorDTO.getCertificateNo());// OrderOrCertificateNumber
				//rowData.add(errorDTO.getRateOfLdc());
				rowData.add(errorDTO.getArticleOfDtaa());
				rowData.add(errorDTO.getDtaaArticleName());// DTAAArticleName
				//rowData.add(errorDTO.getDateOfDepositOfTax()); // DateOfDepositOfTaxAtSource
				rowData.add(errorDTO.getUpdateInTreatyEligibilityConditions());
				rowData.add(errorDTO.getIsDeducteeMasterUpdated());
				rowData.add(errorDTO.getIsTrcAvailable());
				rowData.add(errorDTO.getIsTrcFuture());
				rowData.add(errorDTO.getTrcFutureDate());// TRCAvailableFutureDate
				rowData.add(errorDTO.getIsTrcApplicableFrom());
				rowData.add(errorDTO.getIsTrcApplicableTo());
				rowData.add(errorDTO.getIsTenfAvailable());
				rowData.add(errorDTO.getIsTenfFuture());
				rowData.add(errorDTO.getTenfFutureDate());// Form10FAvailableFutureDate
				rowData.add(errorDTO.getIsTenfApplicableFrom());
				rowData.add(errorDTO.getIsTenfApplicableTo());
				rowData.add(errorDTO.getIsPeIndia()); // WhetherPEInIndia
				rowData.add(errorDTO.getPeInIndiaFromDate());
				rowData.add(errorDTO.getPeInIndiaToDate());
				rowData.add(errorDTO.getIsNoPeDocumentAvailable()); // IsNoPEDeclarationAvailable
				rowData.add(errorDTO.getNoPeDeclarationAvailableInFuture());
				rowData.add(errorDTO.getNoPeDeclarationAvailableFutureDate());
				rowData.add(errorDTO.getNoPeDocumentApplicableFrom());// NoPEDeclarationApplicableFromDate
				rowData.add(errorDTO.getNoPeDocumentApplicableTo()); // NoPEDeclarationApplicableToDate
				rowData.add(errorDTO.getWhetherIncomeReceived());
				rowData.add(errorDTO.getIsNoPoemAvailable()); // IsPOEMApplicable
				rowData.add(errorDTO.getPoemApplicableFromDate());
				rowData.add(errorDTO.getPoemApplicableToDate());
				rowData.add(errorDTO.getIsNoPoemAvailable());// IsNoPOEMDeclarationAvailable
				rowData.add(errorDTO.getIsNoPoemDeclarationInFuture());
				rowData.add(errorDTO.getNoPoemFutureDate()); // NoPOEMDeclarationAvailableFutureDate
				rowData.add(errorDTO.getIsNoPoemApplicableFrom());// NoPOEMDeclarationApplicableFromDate
				rowData.add(errorDTO.getIsNoPoemApplicableTo()); // NoPOEMDeclarationApplicableToDate
				rowData.add(errorDTO.getIsFixedBaseAvailable());
				rowData.add(errorDTO.getIsFixedbaseApplicableFrom());
				rowData.add(errorDTO.getIsFixedbaseApplicableTo()); // FixedBaseInIndiaToDate
				rowData.add(errorDTO.getIsNoFixedBaseDeclaration()); // IsNoFixedBaseDeclarationAvailable
				rowData.add(errorDTO.getNoFixedBaseDeclarationAvailableInFuture());
				rowData.add(errorDTO.getNoFixedBaseDeclarationAvailableFutureDate()); // NoFixedBaseDeclarationAvailableFutureDate
				rowData.add(errorDTO.getNoFixedBaseDeclarationFromDate());
				rowData.add(errorDTO.getNoFixedBaseDeclarationToDate());
				rowData.add(errorDTO.getStayPeriodFinancialYear());
				rowData.add(errorDTO.getBeneficialOwnerOfIncome());
				rowData.add(errorDTO.getIsBeneficialOwnershipOfDeclaration());
				rowData.add(errorDTO.getMliPptConditionSatisifed());
				rowData.add(errorDTO.getMliSlobConditionSatisifed()); // MLISLOBConditionSatisfied
				rowData.add(errorDTO.getIsMliOrPptSlob());
				rowData.add(errorDTO.getIsGAARComplaString());
				//rowData.add(errorDTO.getBsrCode()); // BSRCodeOrForm24GReceiptNo
				//rowData.add(errorDTO.getInr1croreAsReferredToInSection194n()); // AmountOfCashWithdrawalInExcessOfINR1CroreAsReferredToInSection194N
				rowData.add(errorDTO.getCountryToRemittance());
				rowData.add(errorDTO.getAmountInForeignCurrency()); // ActualAmountOfRemittanceAfterTDSInForeignCurrency
				rowData.add(errorDTO.getDateOfRemittance());
				rowData.add(errorDTO.getAggregateAmountOfRemittanceByFy()); // AggregateAmountOfRemittanceMadeDuringTheFY
				rowData.add(errorDTO.getRelevantPurposeCodeAsPerRbi());
				rowData.add(errorDTO.getUniqueAcknowledgementOfTheCorrespondingForm15ca()); // UniqueAcknowledgementOfTheCorrespondingForm15CA
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
			}
		}
	}

	public void validatingInvoiceRecord(NrTransactionsMeta nrExcelData, NrExcelErrorDTO nrErrorDTO, String tenantId,
			List<DeductorTanAddress> deductorTanAddressResponse, Map<String, String> deducteeStatusMap) {
		if (!deductorTanAddressResponse.isEmpty()) {
			DeductorTanAddress deductorTanAddress = deductorTanAddressResponse.get(0);
			if (!deductorTanAddress.getTan().equalsIgnoreCase(nrExcelData.getDeductorMasterTan())) {
				nrErrorDTO.setReason("Deductor TAN is not linked to Deductor PAN." + "\n");
			}
		} else {
			nrErrorDTO.setReason("Deductor TAN is not linked to Deductor PAN." + "\n");
		}
		BigInteger deducteeCount = invoiceLineItemDAO.getDeducteeCountByDeducteeKey(nrExcelData.getDeductorPan(),
				nrExcelData.getDeducteeKey());
		if (deducteeCount.compareTo(BigInteger.ZERO) == 0) {
			nrErrorDTO.setReason(nrErrorDTO.getReason() + "Name not found in Deductee master." + "\n");
		}
		if (StringUtils.isNotBlank(nrExcelData.getDeducteePan())) {
			nrExcelData.setDeducteePan(nrExcelData.getDeducteePan().toUpperCase());
			if (nrExcelData.getDeducteePan().matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
				if (deducteeStatusMap.get(String.valueOf(nrExcelData.getDeducteePan().charAt(3))) != null) {
					nrExcelData.setDeducteeStatus(
							deducteeStatusMap.get(String.valueOf(nrExcelData.getDeducteePan().charAt(3))));
				} else {
					nrErrorDTO.setReason(nrErrorDTO.getReason() + "Pan 4th character "
							+ nrExcelData.getDeducteePan().charAt(3) + " is Invalid." + "\n");
				}
				logger.info("PAN NUMBER : {}", nrExcelData.getDeducteePan());
			} else {
				nrErrorDTO.setReason(
						nrErrorDTO.getReason() + "Pan " + nrExcelData.getDeducteePan() + " is not valid." + "\n");
			}
		} else if (StringUtils.isNotBlank(nrExcelData.getDeducteeStatus())) {
			if ("Artificial Jurisdical Person".equalsIgnoreCase(nrExcelData.getDeducteeStatus())) {
				nrExcelData.setDeducteeStatus(deducteeStatusMap
						.get(String.valueOf(nrExcelData.getDeducteeStatus().trim().substring(11, 12).toUpperCase())));
			} else if (deducteeStatusMap.get(
					String.valueOf(nrExcelData.getDeducteeStatus().trim().substring(0, 1).toUpperCase())) != null) {
				nrExcelData.setDeducteeStatus(deducteeStatusMap
						.get(String.valueOf(nrExcelData.getDeducteeStatus().trim().substring(0, 1).toUpperCase())));
			} else {
				nrErrorDTO.setReason(nrErrorDTO.getReason() + "Deductee Status " + nrExcelData.getDeducteeStatus()
						+ " is not valid." + "\n");
			}
		}
		if (StringUtils.isBlank(nrExcelData.getDocumentType())) {
			nrErrorDTO.setReason(nrErrorDTO.getReason() + "Document type is mandatory" + "\n");
		}

	}

	public SectionDeterminationDTO sectionDeterminationAPI(NrTransactionsMeta nrTransactionsMetaData, String token,
			String deductorTan, String deductorPan, String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		SectionDeterminationDTO sectionDeterminationDTO = new SectionDeterminationDTO();
		String nop = nrTransactionsMetaData.getNatureOfPayment().trim();
		if (token != null) {
			try {
				String url = applicationUrl + "/api/flask/non-resident/section";
				URI uri = new URI(url);

				RestTemplate restTemplate = getRestTemplate();

				HttpHeaders headers = new HttpHeaders();
				headers.add("Authorization", token);
				headers.add("Content-Type", "application/json");
				headers.add("TAN-NUMBER", deductorTan);
				headers.add("deductor-pan", deductorPan);
				Map<String, Object> invoiceMap = new HashMap<>();
				invoiceMap.put("invoiceDate", nrTransactionsMetaData.getDocumentPostingDate());
				invoiceMap.put("descriptions", nop);
				invoiceMap.put("pannumber", nrTransactionsMetaData.getDeducteePan());
				invoiceMap.put("vendorName", nrTransactionsMetaData.getDeducteeName());
				invoiceMap.put("deducteeKey", nrTransactionsMetaData.getDeducteeKey());
				HttpEntity<Map<String, Object>> request = new HttpEntity<>(invoiceMap, headers);
				List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
				// Add the Jackson Message converter
				MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

				// Note: here we are making this converter to process any kind of response,
				// not only application/*json, which is the default behaviour
				converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
				messageConverters.add(converter);
				restTemplate.setMessageConverters(messageConverters);
				ResponseEntity<SectionDeterminationDTO> response = restTemplate.postForEntity(uri, request,
						SectionDeterminationDTO.class);
				sectionDeterminationDTO.setConfidence(response.getBody().getConfidence());
				sectionDeterminationDTO.setSection(response.getBody().getSection());
				sectionDeterminationDTO.setNature(response.getBody().getNature());

			} catch (Exception e) {
				logger.error("Exception occurred while requesting section determination api", e);
				throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return sectionDeterminationDTO;
	}

	/**
	 * 
	 * @param clientSection
	 * @param clientRate
	 * @param clientAmount
	 * @param derivedSection
	 * @param derivedRate
	 * @param derivedAmount
	 * @param roundingOff
	 * @return
	 */
	public String getMismatchCategory(String clientSection, BigDecimal clientRate, BigDecimal clientAmount,
			String derivedSection, BigDecimal derivedRate, BigDecimal derivedAmount, String roundingOff) {
		String mismatchCategory = null;
		boolean rateMatch = false;
		if ("Decimal".equalsIgnoreCase(roundingOff)
				&& clientRate.setScale(2, RoundingMode.UP).compareTo(derivedRate.setScale(2, RoundingMode.UP)) == 0) {
			rateMatch = true;
		} else if (("Roundoffto1".equalsIgnoreCase(roundingOff) || "Roundoffto10".equalsIgnoreCase(roundingOff))
				&& clientAmount.subtract(derivedAmount).abs().compareTo(BigDecimal.ONE) <= 1) {
			rateMatch = true;
		}
		if ("NAD".equalsIgnoreCase(derivedSection)) {
			mismatchCategory = "NAD";
		} else if (clientSection.equals(derivedSection) && !rateMatch) {
			mismatchCategory = "SM-RMM";
		} else if (!clientSection.equals(derivedSection) && !rateMatch) {
			mismatchCategory = "SMM-RMM";
		} else if (!clientSection.equals(derivedSection) && rateMatch) {
			mismatchCategory = "SMM-RM";
		}
		return mismatchCategory;
	}

	public RestTemplate getRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
				return true;
			}
		};
		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();
		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		return restTemplate;
	}

	// Tds month tracker logic
	private int challanMonthCalculation(Date documentPostingDate, Date challanGenerateDate, boolean challanPaid,
			Map<String, Date> monthClosureMap, Map<String, Integer> dueDatePaymentMap) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(documentPostingDate);
		int postingDateMonth = cal.get(Calendar.MONTH) + 1;
		int postingDateYear = cal.get(Calendar.YEAR);
		Date monthClosureForProcessing = null;
		if (monthClosureMap.get(postingDateYear + "-" + postingDateMonth) != null) {
			monthClosureForProcessing = monthClosureMap.get(postingDateYear + "-" + postingDateMonth);
		}
		int challanMonth = 0;
		Date currentDate = CommonUtil.getMonthStartDate(new Date());
		// current date
		cal.setTime(new Date());
		int currentMonth = cal.get(Calendar.MONTH) + 1;
		int currentDay = cal.get(Calendar.DAY_OF_MONTH);
		if (challanPaid) {
			if (challanGenerateDate != null) {
				String challanGeneratedDate = new SimpleDateFormat("yyyy-MM-dd").format(challanGenerateDate);
				if (dueDatePaymentMap.get(challanGeneratedDate) != null
						&& dueDatePaymentMap.get(challanGeneratedDate) > 0) {
					challanMonth = dueDatePaymentMap.get(challanGeneratedDate).intValue();
				}
			}
		} else if (monthClosureForProcessing != null) {
			Date monthClosureDate = CommonUtil.setDateTime(monthClosureForProcessing);
			if (monthClosureDate.getTime() >= currentDate.getTime()) {
				challanMonth = postingDateMonth;
			} else {
				challanMonth = currentMonth;
			}
		} else if (currentDay < 6) {
			if (currentMonth == 1) {
				challanMonth = 12;
			} else {
				challanMonth = currentMonth - 1;
			}
		} else {
			challanMonth = currentMonth;
		}
		return challanMonth;
	}

	public BigDecimal cessCalculation(BigDecimal tdsAmount, List<SurchargeAndCessDTO> cessMasterList) {
		SurchargeAndCessDTO cess = null;
		BigDecimal cessRate = BigDecimal.ZERO;
		if (!cessMasterList.isEmpty()) {
			for (SurchargeAndCessDTO cessMaster : cessMasterList) {
				Long cessSlabFrom = cessMaster.getInvoiceSlabFrom() != null ? cessMaster.getInvoiceSlabFrom() : 0L;
				Long cessSlabTo = cessMaster.getInvoiceSlabTo() != null ? cessMaster.getInvoiceSlabTo() : 0L;
				if (cessSlabFrom == 0 || (Double.valueOf(cessSlabFrom)) < tdsAmount.doubleValue()
						&& ((cessSlabTo == 0) || (cessSlabTo.doubleValue() > tdsAmount.doubleValue()))) {
					cess = cessMaster;
					break;
				}
			}
			if (cess != null) {
				cessRate = cess.getRate() != null ? cess.getRate() : BigDecimal.ZERO;
			}
		}
		return cessRate;
	}

	// Surcharge calculation
	public BigDecimal surchargeCalculation(String finalTdsSection, BigDecimal finalTdsAmount, BigDecimal taxableAmount,
			String deducteeStatus, Map<String, List<Map<String, Object>>> surchargeMap) {
		BigDecimal surchargeRate = BigDecimal.ZERO;
		BigDecimal surcharge = BigDecimal.ZERO;
		String surchargeKey = finalTdsSection + "-" + deducteeStatus;
		if (surchargeMap.get(surchargeKey) != null) {
			for (Map<String, Object> surchargeData : surchargeMap.get(surchargeKey)) {
				Integer surchargeSlabFrom = (Integer) surchargeData.get("invoiceSlabFrom");
				Integer surchargeSlabTo = (Integer) surchargeData.get("invoiceSlabTo");
				if (surchargeSlabFrom == 0 || (Double.valueOf(surchargeSlabFrom) < taxableAmount.doubleValue()
						&& ((surchargeSlabTo == 0) || (surchargeSlabTo.doubleValue() > taxableAmount.doubleValue())))) {
					Double rate = (Double) surchargeData.get("rate");
					surchargeRate = rate != null ? BigDecimal.valueOf(rate) : BigDecimal.ZERO;
					break;
				}
			}
			surcharge = finalTdsAmount.multiply(surchargeRate).divide(BigDecimal.valueOf(100));
			logger.info("Surcharge amount : {}", surcharge);
		}
		return surcharge;
	}

	@Async
	public void generateInvoiceStaggingFile(BatchUpload batchUpload, String pan, String tenantId)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = resourceLoader.getResource("classpath:templates/" + "non_resident_upload_template.xlsx");
		InputStream input = resource.getInputStream();
		try (SXSSFWorkbook wb = new SXSSFWorkbook(new XSSFWorkbook(input))) {
			SXSSFSheet sheet = wb.getSheetAt(0);
			List<InvoiceStagging> invoiceStaggingList = invoiceLineItemDAO.getInvoiceStaggingRecords(
					batchUpload.getDeductorMasterTan(), pan, batchUpload.getAssessmentYear(),
					batchUpload.getAssessmentMonth());
			int rowindex = 1;

			XSSFCellStyle style1 = (XSSFCellStyle) wb.createCellStyle();
			style1.setWrapText(true);
			XSSFFont fonts = (XSSFFont) wb.createFont();
			fonts.setBold(false);
			style1.setFont(fonts);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			for (InvoiceStagging invoice : invoiceStaggingList) {
				SXSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
				createSXSSFCell(style1, row1, 0, invoice.getDeductorTan());
				createSXSSFCell(style1, row1, 1, invoice.getDeductorPan());
				createSXSSFCell(style1, row1, 2, invoice.getDeducteeCode());
				createSXSSFCell(style1, row1, 3, invoice.getDeducteePan());
				createSXSSFCell(style1, row1, 4, invoice.getDeducteeName());
				createSXSSFCell(style1, row1, 5, "");
				createSXSSFCell(style1, row1, 6, "INV");
				createSXSSFCell(style1, row1, 7, "");
				createSXSSFCell(style1, row1, 8, "");
				createSXSSFCell(style1, row1, 9, "");
				createSXSSFCell(style1, row1, 10, "");
				createSXSSFCell(style1, row1, 11, "");
				createSXSSFCell(style1, row1, 12, "");
				createSXSSFCell(style1, row1, 13, "");
				createSXSSFCell(style1, row1, 14, "");
				createSXSSFCell(style1, row1, 15, "");
				createSXSSFCell(style1, row1, 16, "");
				createSXSSFCell(style1, row1, 17, invoice.getDeducteeTin());
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
				createSXSSFCell(style1, row1, 44, invoice.getPoNumber());
				if (invoice.getPoDate() != null) {
					String poDate = simpleDateFormat.format(invoice.getPoDate());
					createSXSSFCell(style1, row1, 45, poDate);
				} else {
					createSXSSFCell(style1, row1, 45, "");
				}
				createSXSSFCell(style1, row1, 46, invoice.getDocumentNumber());
				if (invoice.getPostingDate() != null) {
					String postingDate = simpleDateFormat.format(invoice.getPostingDate());
					createSXSSFCell(style1, row1, 47, postingDate);
				} else {
					createSXSSFCell(style1, row1, 47, "");
				}
				createSXSSFCell(style1, row1, 48, invoice.getVendorInvoiceNumber());
				if (invoice.getDocumentDate() != null) {
					String documentDate = simpleDateFormat.format(invoice.getDocumentDate());
					createSXSSFCell(style1, row1, 49, documentDate);
				} else {
					createSXSSFCell(style1, row1, 49, "");
				}
				createSXSSFCell(style1, row1, 50, getFormattedValue(invoice.getAmountForeignCurrency()));
				createSXSSFCell(style1, row1, 51, invoice.getCurrency());
				createSXSSFCell(style1, row1, 52, getFormattedValue(invoice.getTaxablevalue()));
				createSXSSFCell(style1, row1, 53, "");
				createSXSSFCell(style1, row1, 54, "");
				createSXSSFCell(style1, row1, 55, "");
				createSXSSFCell(style1, row1, 56, "");
				createSXSSFCell(style1, row1, 57, getFormattedValue(invoice.getTdsRate()));
				createSXSSFCell(style1, row1, 58, "");
				if (invoice.getTdsDeductionDate() != null) {
					String dateOfDeduction = simpleDateFormat.format(invoice.getTdsDeductionDate());
					createSXSSFCell(style1, row1, 59, dateOfDeduction);
				} else {
					createSXSSFCell(style1, row1, 59, "");
				}
				createSXSSFCell(style1, row1, 60, "");
				createSXSSFCell(style1, row1, 61, getFormattedValue(invoice.getTdsAmount()));
				createSXSSFCell(style1, row1, 62, "");
				createSXSSFCell(style1, row1, 63, getFormattedValue(invoice.getCessAmount()));
				createSXSSFCell(style1, row1, 64, "");
				createSXSSFCell(style1, row1, 65, "");
				createSXSSFCell(style1, row1, 66, "");
				createSXSSFCell(style1, row1, 67, "");
				createSXSSFCell(style1, row1, 68, "");
				createSXSSFCell(style1, row1, 69, invoice.getGrossUpIndicator());
				createSXSSFCell(style1, row1, 70, "");
				createSXSSFCell(style1, row1, 71, "");
				createSXSSFCell(style1, row1, 72, "");
				createSXSSFCell(style1, row1, 73, "");
				createSXSSFCell(style1, row1, 74, "");
				createSXSSFCell(style1, row1, 75, "");
				createSXSSFCell(style1, row1, 76, "");
				createSXSSFCell(style1, row1, 77, invoice.getChallanPaid());
				if (invoice.getChallanProcessingDate() != null) {
					String challanPaidDate = simpleDateFormat.format(invoice.getChallanProcessingDate());
					createSXSSFCell(style1, row1, 78, challanPaidDate);
				} else {
					createSXSSFCell(style1, row1, 78, "");
				}
				createSXSSFCell(style1, row1, 79, invoice.getProvisionAdjustmentFlag());
				createSXSSFCell(style1, row1, 80, invoice.getAdvanceAdjustmentFlag());
			}
			wb.write(out);
			batchUpload.setProcessedCount(invoiceStaggingList.size());
			batchUpload.setRowsCount(Long.valueOf(invoiceStaggingList.size()));
		}
		String filePath = sendFileToBlobStorage(out, tenantId);
		batchUpload.setFilePath(filePath);
		batchUpload.setStatus("Processed");
		batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
		batchUploadDAO.update(batchUpload);
	}

	@Async
	public void generateAdvanceStaggingFile(BatchUpload batchUpload, String pan, String tenantId)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = resourceLoader.getResource("classpath:templates/" + "non_resident_upload_template.xlsx");
		InputStream input = resource.getInputStream();
		try (SXSSFWorkbook wb = new SXSSFWorkbook(new XSSFWorkbook(input))) {
			SXSSFSheet sheet = wb.getSheetAt(0);
			List<AdvanceStagging> advanceStaggingList = advanceDAO.getAdvanceStaggingRecords(
					batchUpload.getDeductorMasterTan(), pan, batchUpload.getAssessmentYear(),
					batchUpload.getAssessmentMonth());
			int rowindex = 1;

			XSSFCellStyle style1 = (XSSFCellStyle) wb.createCellStyle();
			style1.setWrapText(true);
			XSSFFont fonts = (XSSFFont) wb.createFont();
			fonts.setBold(false);
			style1.setFont(fonts);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			for (AdvanceStagging advance : advanceStaggingList) {
				SXSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
				createSXSSFCell(style1, row1, 0, advance.getDeductorTan());
				createSXSSFCell(style1, row1, 1, advance.getDeductorPan());
				createSXSSFCell(style1, row1, 2, advance.getDeducteeCode());
				createSXSSFCell(style1, row1, 3, advance.getDeducteePan());
				createSXSSFCell(style1, row1, 4, advance.getDeducteeName());
				createSXSSFCell(style1, row1, 5, "");
				createSXSSFCell(style1, row1, 6, "ADV");
				createSXSSFCell(style1, row1, 7, "");
				createSXSSFCell(style1, row1, 8, "");
				createSXSSFCell(style1, row1, 9, "");
				createSXSSFCell(style1, row1, 10, "");
				createSXSSFCell(style1, row1, 11, "");
				createSXSSFCell(style1, row1, 12, "");
				createSXSSFCell(style1, row1, 13, "");
				createSXSSFCell(style1, row1, 14, "");
				createSXSSFCell(style1, row1, 15, "");
				createSXSSFCell(style1, row1, 16, "");
				createSXSSFCell(style1, row1, 17, advance.getDeducteeTin());
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
				createSXSSFCell(style1, row1, 44, advance.getPoNumber());
				if (advance.getPoDate() != null) {
					String poDate = simpleDateFormat.format(advance.getPoDate());
					createSXSSFCell(style1, row1, 45, poDate);
				} else {
					createSXSSFCell(style1, row1, 45, "");
				}
				createSXSSFCell(style1, row1, 46, advance.getErpDocumentNumber());
				if (advance.getPostingDate() != null) {
					String postingDate = simpleDateFormat.format(advance.getPostingDate());
					createSXSSFCell(style1, row1, 47, postingDate);
				} else {
					createSXSSFCell(style1, row1, 47, "");
				}
				createSXSSFCell(style1, row1, 48, "");
				if (advance.getDocumentDate() != null) {
					String documentDate = simpleDateFormat.format(advance.getDocumentDate());
					createSXSSFCell(style1, row1, 49, documentDate);
				} else {
					createSXSSFCell(style1, row1, 49, "");
				}
				createSXSSFCell(style1, row1, 50, getFormattedValue(advance.getAmountForeignCurrency()));
				createSXSSFCell(style1, row1, 51, advance.getCurrency());
				createSXSSFCell(style1, row1, 52, getFormattedValue(advance.getTaxablevalue()));
				createSXSSFCell(style1, row1, 53, "");
				createSXSSFCell(style1, row1, 54, "");
				createSXSSFCell(style1, row1, 55, "");
				createSXSSFCell(style1, row1, 56, "");
				createSXSSFCell(style1, row1, 57, getFormattedValue(advance.getTdsRate()));
				createSXSSFCell(style1, row1, 58, "");
				if (advance.getTdsDeductionDate() != null) {
					String dateOfDeduction = simpleDateFormat.format(advance.getTdsDeductionDate());
					createSXSSFCell(style1, row1, 59, dateOfDeduction);
				} else {
					createSXSSFCell(style1, row1, 59, "");
				}
				createSXSSFCell(style1, row1, 60, "");
				createSXSSFCell(style1, row1, 61, getFormattedValue(advance.getTdsAmount()));
				createSXSSFCell(style1, row1, 62, "");
				createSXSSFCell(style1, row1, 63, getFormattedValue(advance.getCessAmount()));
				createSXSSFCell(style1, row1, 64, "");
				createSXSSFCell(style1, row1, 65, "");
				createSXSSFCell(style1, row1, 66, "");
				createSXSSFCell(style1, row1, 67, "");
				createSXSSFCell(style1, row1, 68, "");
				createSXSSFCell(style1, row1, 69, advance.getGrossUpIndicator());
				createSXSSFCell(style1, row1, 70, "");
				createSXSSFCell(style1, row1, 71, "");
				createSXSSFCell(style1, row1, 72, "");
				createSXSSFCell(style1, row1, 73, "");
				createSXSSFCell(style1, row1, 74, "");
				createSXSSFCell(style1, row1, 75, "");
				createSXSSFCell(style1, row1, 76, "");
				createSXSSFCell(style1, row1, 77, advance.getChallanPaid());
				if (advance.getChallanProcessingDate() != null) {
					String challanPaidDate = simpleDateFormat.format(advance.getChallanProcessingDate());
					createSXSSFCell(style1, row1, 78, challanPaidDate);
				} else {
					createSXSSFCell(style1, row1, 78, "");
				}
				createSXSSFCell(style1, row1, 79, "");
				createSXSSFCell(style1, row1, 80, "");
			}
			wb.write(out);
			batchUpload.setProcessedCount(advanceStaggingList.size());
			batchUpload.setRowsCount(Long.valueOf(advanceStaggingList.size()));
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
		File someFile = new File("Invoice_Stagging_Report.xlsx");
		try (FileOutputStream fos = new FileOutputStream(someFile)) {
			fos.write(bytes);
			fos.flush();
			fos.close();
			return someFile;
		}
	}

	/**
	 * 
	 * @param errorDTOs
	 * @param worksheet
	 * @param deductorTan
	 * @param headerNames
	 * @throws Exception
	 */
	public void setExtractDataForNonResidentAdvance(List<NrExcelErrorDTO> errorDTOs, Worksheet worksheet,
			String deductorTan, List<String> headerNames) throws Exception {
		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			for (int i = 0; i < errorDTOs.size(); i++) {
				NrExcelErrorDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = new ArrayList<>();
				rowData.add(StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.add(StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.add(errorDTO.getSerialNumber());
				rowData.add(errorDTO.getSourceIdentifier());
				rowData.add(errorDTO.getSourceFilename());
				rowData.add(errorDTO.getDeductorCode());
				rowData.add(errorDTO.getDeductorName());
				rowData.add(errorDTO.getDeductorPan());
				rowData.add(errorDTO.getDeductorMasterTan());
				rowData.add(errorDTO.getDeductorGstin());
				rowData.add(errorDTO.getDeducteeCode());
				rowData.add(errorDTO.getDeducteeName());
				rowData.add(errorDTO.getDeducteePan());
				rowData.add(errorDTO.getTin());
				rowData.add(errorDTO.getDeducteeGstin());
				rowData.add(errorDTO.getErpDocumentNo()); // VendorInvoiceNumber
				rowData.add(errorDTO.getVendorDocumentDate());// DocumentDate
				rowData.add(errorDTO.getDocumentPostingDate());
				rowData.add(errorDTO.getDateOfDeductionOfTax()); // TDSDeductionDate
				rowData.add(errorDTO.getDocumentType());// DocumentType
				rowData.add(errorDTO.getSupplyType());
				rowData.add(errorDTO.getLineItemNumber());
				rowData.add(errorDTO.getGlAccountCode());
				rowData.add(errorDTO.getGlAccountName());
				rowData.add(errorDTO.getErpDocumentType());
				rowData.add(errorDTO.getHsnOrSac());
				rowData.add(errorDTO.getHsnOrSacDesc());
				rowData.add(errorDTO.getInvoiceDesc());// AdvanceDesc
				rowData.add(errorDTO.getPoDesc());
				rowData.add(errorDTO.getAmountInInr());// TaxableValue
				rowData.add(errorDTO.getTdsTaxCodeErp());
				rowData.add(errorDTO.getTdsSection());
				rowData.add(errorDTO.getRateAsPerIncometax());// TDSRate
				rowData.add(errorDTO.getTdsAmount());
				rowData.add(errorDTO.getPoNumber());
				rowData.add(errorDTO.getPoDate());
				rowData.add(errorDTO.getLinkingOfInvoiceWithPo());
				rowData.add(errorDTO.getChallanPaid());
				rowData.add(errorDTO.getChallanGeneratedDate());// ChallanProcessingDate
				rowData.add(errorDTO.getOriginalDocumentNumber()); // OriginalDocNumber
				rowData.add(errorDTO.getOriginalDocumentDate()); // OriginalDocPostingDate
				rowData.add(errorDTO.getPoItemNo()); // Polineitemnumber
				rowData.add(errorDTO.getTdsBaseValue());
				rowData.add(errorDTO.getBusinessPlace());
				rowData.add(errorDTO.getBusinessArea());
				rowData.add(errorDTO.getPlant());
				rowData.add(errorDTO.getProfitCenter());
				rowData.add(errorDTO.getAssignmentNumber());
				rowData.add(errorDTO.getUserName());
				rowData.add(errorDTO.getIsResident());// NRIndicator
				rowData.add(errorDTO.getIsGrossedUp());
				rowData.add(errorDTO.getTdsRemittanceDate());
				rowData.add(errorDTO.getDebitCreditIndicator());
				rowData.add(errorDTO.getCessRate());
				rowData.add(errorDTO.getCessAmount());
				rowData.add(errorDTO.getAmountInForeignCurrency());
				rowData.add(errorDTO.getExchangeRate());
				rowData.add(errorDTO.getCurrency());
				rowData.add(errorDTO.getUserDefinedField1());
				rowData.add(errorDTO.getUserDefinedField2());
				rowData.add(errorDTO.getUserDefinedField3());
				rowData.add(errorDTO.getUserDefinedField4());
				rowData.add(errorDTO.getUserDefinedField5());
				//rowData.add(errorDTO.getDeducteeStatus());
				rowData.add(errorDTO.getCountry());// DeducteeCountry
				rowData.add(errorDTO.getNatureOfPayment());// NatureOfPayment
				rowData.add(errorDTO.getNatureOfRemittance());// NatureOfRemittance
				rowData.add(errorDTO.getDetailedDescription());
				rowData.add(errorDTO.getAmountOfIncometax()); // IncomeOnWhichTaxIsToBeDeducted
				rowData.add(errorDTO.getSurcharge());
				rowData.add(errorDTO.getEductaionCess());
				rowData.add(errorDTO.getInterest());
				rowData.add(errorDTO.getEductaionFee());// fee
				//rowData.add(errorDTO.getIsLdcApplied());
				//rowData.add(errorDTO.getCertificateNo());// OrderOrCertificateNumber
				//rowData.add(errorDTO.getRateOfLdc());
				rowData.add(errorDTO.getArticleOfDtaa());
				rowData.add(errorDTO.getDtaaArticleName());// DTAAArticleName
				//rowData.add(errorDTO.getDateOfDepositOfTax()); // DateOfDepositOfTaxAtSource
				rowData.add(errorDTO.getUpdateInTreatyEligibilityConditions());
				rowData.add(errorDTO.getIsDeducteeMasterUpdated());
				rowData.add(errorDTO.getIsTrcAvailable());
				rowData.add(errorDTO.getIsTrcFuture());
				rowData.add(errorDTO.getTrcFutureDate());// TRCAvailableFutureDate
				rowData.add(errorDTO.getIsTrcApplicableFrom());
				rowData.add(errorDTO.getIsTrcApplicableTo());
				rowData.add(errorDTO.getIsTenfAvailable());
				rowData.add(errorDTO.getIsTenfFuture());
				rowData.add(errorDTO.getTenfFutureDate());// Form10FAvailableFutureDate
				rowData.add(errorDTO.getIsTenfApplicableFrom());
				rowData.add(errorDTO.getIsTenfApplicableTo());
				rowData.add(errorDTO.getIsPeIndia()); // WhetherPEInIndia
				rowData.add(errorDTO.getPeInIndiaFromDate());
				rowData.add(errorDTO.getPeInIndiaToDate());
				rowData.add(errorDTO.getIsNoPeDocumentAvailable()); // IsNoPEDeclarationAvailable
				rowData.add(errorDTO.getNoPeDeclarationAvailableInFuture());
				rowData.add(errorDTO.getNoPeDeclarationAvailableFutureDate());
				rowData.add(errorDTO.getNoPeDocumentApplicableFrom());// NoPEDeclarationApplicableFromDate
				rowData.add(errorDTO.getNoPeDocumentApplicableTo()); // NoPEDeclarationApplicableToDate
				rowData.add(errorDTO.getWhetherIncomeReceived());
				rowData.add(errorDTO.getIsNoPoemAvailable()); // IsPOEMApplicable
				rowData.add(errorDTO.getPoemApplicableFromDate());
				rowData.add(errorDTO.getPoemApplicableToDate());
				rowData.add(errorDTO.getIsNoPoemAvailable());// IsNoPOEMDeclarationAvailable
				rowData.add(errorDTO.getIsNoPoemDeclarationInFuture());
				rowData.add(errorDTO.getNoPoemFutureDate()); // NoPOEMDeclarationAvailableFutureDate
				rowData.add(errorDTO.getIsNoPoemApplicableFrom());// NoPOEMDeclarationApplicableFromDate
				rowData.add(errorDTO.getIsNoPoemApplicableTo()); // NoPOEMDeclarationApplicableToDate
				rowData.add(errorDTO.getIsFixedBaseAvailable());
				rowData.add(errorDTO.getIsFixedbaseApplicableFrom());
				rowData.add(errorDTO.getIsFixedbaseApplicableTo()); // FixedBaseInIndiaToDate
				rowData.add(errorDTO.getIsNoFixedBaseDeclaration()); // IsNoFixedBaseDeclarationAvailable
				rowData.add(errorDTO.getNoFixedBaseDeclarationAvailableInFuture());
				rowData.add(errorDTO.getNoFixedBaseDeclarationAvailableFutureDate()); // NoFixedBaseDeclarationAvailableFutureDate
				rowData.add(errorDTO.getNoFixedBaseDeclarationFromDate());
				rowData.add(errorDTO.getNoFixedBaseDeclarationToDate());
				rowData.add(errorDTO.getStayPeriodFinancialYear());
				rowData.add(errorDTO.getBeneficialOwnerOfIncome());
				rowData.add(errorDTO.getIsBeneficialOwnershipOfDeclaration());
				rowData.add(errorDTO.getMliPptConditionSatisifed());
				rowData.add(errorDTO.getMliSlobConditionSatisifed()); // MLISLOBConditionSatisfied
				rowData.add(errorDTO.getIsMliOrPptSlob());
				rowData.add(errorDTO.getIsGAARComplaString());
				//rowData.add(errorDTO.getBsrCode()); // BSRCodeOrForm24GReceiptNo
				//rowData.add(errorDTO.getInr1croreAsReferredToInSection194n()); // AmountOfCashWithdrawalInExcessOfINR1CroreAsReferredToInSection194N
				rowData.add(errorDTO.getCountryToRemittance());
				rowData.add(errorDTO.getAmountInForeignCurrency()); // ActualAmountOfRemittanceAfterTDSInForeignCurrency
				rowData.add(errorDTO.getDateOfRemittance());
				rowData.add(errorDTO.getAggregateAmountOfRemittanceByFy()); // AggregateAmountOfRemittanceMadeDuringTheFY
				rowData.add(errorDTO.getRelevantPurposeCodeAsPerRbi());
				rowData.add(errorDTO.getUniqueAcknowledgementOfTheCorrespondingForm15ca()); // UniqueAcknowledgementOfTheCorrespondingForm15CA
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
			}
		}
	}

	/**
	 * 
	 * @param errorDTOs
	 * @param worksheet
	 * @param deductorTan
	 * @param headerNames
	 * @throws Exception
	 */
	public void setExtractDataForNonResidentProvision(List<NrExcelErrorDTO> errorDTOs, Worksheet worksheet,
			String deductorTan, List<String> headerNames) throws Exception {
		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			for (int i = 0; i < errorDTOs.size(); i++) {
				NrExcelErrorDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = new ArrayList<>();
				rowData.add(StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.add(StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.add(errorDTO.getSerialNumber());
				rowData.add(errorDTO.getSourceIdentifier());
				rowData.add(errorDTO.getSourceFilename());
				rowData.add(errorDTO.getDeductorCode());
				rowData.add(errorDTO.getDeductorName());
				rowData.add(errorDTO.getDeductorPan());
				rowData.add(errorDTO.getDeductorMasterTan());
				rowData.add(errorDTO.getDeductorGstin());
				rowData.add(errorDTO.getDeducteeCode());
				rowData.add(errorDTO.getDeducteeName());
				rowData.add(errorDTO.getDeducteePan());
				rowData.add(errorDTO.getTin());
				rowData.add(errorDTO.getDeducteeGstin());
				rowData.add(errorDTO.getErpDocumentNo()); // VendorInvoiceNumber
				rowData.add(errorDTO.getVendorDocumentDate());// DocumentDate
				rowData.add(errorDTO.getDocumentPostingDate());
				rowData.add(errorDTO.getDateOfDeductionOfTax()); // TDSDeductionDate
				rowData.add(errorDTO.getDocumentType());// DocumentType
				rowData.add(errorDTO.getSupplyType());
				rowData.add(errorDTO.getLineItemNumber());
				rowData.add(errorDTO.getGlAccountCode());
				rowData.add(errorDTO.getGlAccountName());
				rowData.add(errorDTO.getErpDocumentType());
				rowData.add(errorDTO.getHsnOrSac());
				rowData.add(errorDTO.getHsnOrSacDesc());
				rowData.add(errorDTO.getInvoiceDesc());// ProvisionsDesc
				rowData.add(errorDTO.getPoDesc());
				rowData.add(errorDTO.getAmountInInr());// TaxableValue
				rowData.add(errorDTO.getTdsTaxCodeErp());
				rowData.add(errorDTO.getTdsSection());
				rowData.add(errorDTO.getRateAsPerIncometax());// TDSRate
				rowData.add(errorDTO.getTdsAmount());
				rowData.add(errorDTO.getPoNumber());
				rowData.add(errorDTO.getPoDate());
				rowData.add(errorDTO.getPoType()); // POType
				rowData.add(errorDTO.getLinkingOfInvoiceWithPo());
				rowData.add(errorDTO.getChallanPaid());
				rowData.add(errorDTO.getChallanGeneratedDate());// ChallanProcessingDate
				rowData.add(errorDTO.getUserDefinedField1());
				rowData.add(errorDTO.getUserDefinedField2());
				rowData.add(errorDTO.getUserDefinedField3());
				//rowData.add(errorDTO.getDeducteeStatus());
				rowData.add(errorDTO.getCountry());// DeducteeCountry
				rowData.add(errorDTO.getNatureOfPayment());// NatureOfPayment
				rowData.add(errorDTO.getNatureOfRemittance());// NatureOfRemittance
				rowData.add(errorDTO.getDetailedDescription());
				rowData.add(errorDTO.getAmountOfIncometax()); // IncomeOnWhichTaxIsToBeDeducted
				rowData.add(errorDTO.getSurcharge());
				rowData.add(errorDTO.getEductaionCess());
				rowData.add(errorDTO.getInterest());
				rowData.add(errorDTO.getEductaionFee());// fee
				//rowData.add(errorDTO.getIsLdcApplied());
				//rowData.add(errorDTO.getCertificateNo());// OrderOrCertificateNumber
				//rowData.add(errorDTO.getRateOfLdc());
				rowData.add(errorDTO.getArticleOfDtaa());
				rowData.add(errorDTO.getDtaaArticleName());// DTAAArticleName
				//rowData.add(errorDTO.getDateOfDepositOfTax()); // DateOfDepositOfTaxAtSource
				rowData.add(errorDTO.getUpdateInTreatyEligibilityConditions());
				rowData.add(errorDTO.getIsDeducteeMasterUpdated());
				rowData.add(errorDTO.getIsTrcAvailable());
				rowData.add(errorDTO.getIsTrcFuture());
				rowData.add(errorDTO.getTrcFutureDate());// TRCAvailableFutureDate
				rowData.add(errorDTO.getIsTrcApplicableFrom());
				rowData.add(errorDTO.getIsTrcApplicableTo());
				rowData.add(errorDTO.getIsTenfAvailable());
				rowData.add(errorDTO.getIsTenfFuture());
				rowData.add(errorDTO.getTenfFutureDate());// Form10FAvailableFutureDate
				rowData.add(errorDTO.getIsTenfApplicableFrom());
				rowData.add(errorDTO.getIsTenfApplicableTo());
				rowData.add(errorDTO.getIsPeIndia()); // WhetherPEInIndia
				rowData.add(errorDTO.getPeInIndiaFromDate());
				rowData.add(errorDTO.getPeInIndiaToDate());
				rowData.add(errorDTO.getIsNoPeDocumentAvailable()); // IsNoPEDeclarationAvailable
				rowData.add(errorDTO.getNoPeDeclarationAvailableInFuture());
				rowData.add(errorDTO.getNoPeDeclarationAvailableFutureDate());
				rowData.add(errorDTO.getNoPeDocumentApplicableFrom());// NoPEDeclarationApplicableFromDate
				rowData.add(errorDTO.getNoPeDocumentApplicableTo()); // NoPEDeclarationApplicableToDate
				rowData.add(errorDTO.getWhetherIncomeReceived());
				rowData.add(errorDTO.getIsNoPoemAvailable()); // IsPOEMApplicable
				rowData.add(errorDTO.getPoemApplicableFromDate());
				rowData.add(errorDTO.getPoemApplicableToDate());
				rowData.add(errorDTO.getIsNoPoemAvailable());// IsNoPOEMDeclarationAvailable
				rowData.add(errorDTO.getIsNoPoemDeclarationInFuture());
				rowData.add(errorDTO.getNoPoemFutureDate()); // NoPOEMDeclarationAvailableFutureDate
				rowData.add(errorDTO.getIsNoPoemApplicableFrom());// NoPOEMDeclarationApplicableFromDate
				rowData.add(errorDTO.getIsNoPoemApplicableTo()); // NoPOEMDeclarationApplicableToDate
				rowData.add(errorDTO.getIsFixedBaseAvailable());
				rowData.add(errorDTO.getIsFixedbaseApplicableFrom());
				rowData.add(errorDTO.getIsFixedbaseApplicableTo()); // FixedBaseInIndiaToDate
				rowData.add(errorDTO.getIsNoFixedBaseDeclaration()); // IsNoFixedBaseDeclarationAvailable
				rowData.add(errorDTO.getNoFixedBaseDeclarationAvailableInFuture());
				rowData.add(errorDTO.getNoFixedBaseDeclarationAvailableFutureDate()); // NoFixedBaseDeclarationAvailableFutureDate
				rowData.add(errorDTO.getNoFixedBaseDeclarationFromDate());
				rowData.add(errorDTO.getNoFixedBaseDeclarationToDate());
				rowData.add(errorDTO.getStayPeriodFinancialYear());
				rowData.add(errorDTO.getBeneficialOwnerOfIncome());
				rowData.add(errorDTO.getIsBeneficialOwnershipOfDeclaration());
				rowData.add(errorDTO.getMliPptConditionSatisifed());
				rowData.add(errorDTO.getMliSlobConditionSatisifed()); // MLISLOBConditionSatisfied
				rowData.add(errorDTO.getIsMliOrPptSlob());
				rowData.add(errorDTO.getIsGAARComplaString());
				rowData.add(errorDTO.getCountryToRemittance());
				rowData.add(errorDTO.getAmountInForeignCurrency()); // ActualAmountOfRemittanceAfterTDSInForeignCurrency
				rowData.add(errorDTO.getDateOfRemittance());
				rowData.add(errorDTO.getAggregateAmountOfRemittanceByFy()); // AggregateAmountOfRemittanceMadeDuringTheFY
				rowData.add(errorDTO.getRelevantPurposeCodeAsPerRbi());
				rowData.add(errorDTO.getUniqueAcknowledgementOfTheCorrespondingForm15ca()); // UniqueAcknowledgementOfTheCorrespondingForm15CA
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
			}
		}
	}
}
