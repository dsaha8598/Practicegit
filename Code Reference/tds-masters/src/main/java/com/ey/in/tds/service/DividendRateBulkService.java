package com.ey.in.tds.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.Country;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.domain.dividend.CountrySpecificRules;
import com.ey.in.tds.common.domain.dividend.DividendDeductorType;
import com.ey.in.tds.common.domain.dividend.DividendInstrumentsMapping;
import com.ey.in.tds.common.domain.dividend.DividendRateAct;
import com.ey.in.tds.common.domain.dividend.DividendRateTreaty;
import com.ey.in.tds.common.domain.dividend.Range;
import com.ey.in.tds.common.domain.dividend.ShareholderCategory;
import com.ey.in.tds.common.domain.dividend.CountrySpecificRules.Type;
import com.ey.in.tds.dto.DividendRateActErrorDTO;
import com.ey.in.tds.dto.DividendRateActExcelDTO;
import com.ey.in.tds.dto.DividendRateTreatyErrorDTO;
import com.ey.in.tds.repository.DividendRateRepository;
import com.ey.in.tds.service.util.excel.DividendRateActExcel;
import com.microsoft.azure.storage.StorageException;
/**
 * this class is responsible for processing and generation of excel file asynchronusly
 * @author dipak
 *
 */
@Service
public class DividendRateBulkService {
	
	private final Logger logger = LoggerFactory.getLogger(DividendRateService.class);
	@Autowired
	private DividendRateRepository dividendRateRepository;


	@Autowired
	private MasterBatchUploadService masterBatchUploadService;

	@Autowired
	private CommonAPIService commonAPIService;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	public static DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Async
	public void saveDividendRateActData(XSSFWorkbook workbook, MultipartFile file, String sha256,
			Integer assesssmentYear, Integer assessmentMonth, String userName, MasterBatchUpload masterBatchUpload,
			String uploadType) throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		MultiTenantContext.setTenantId("master");
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.executeWithoutResult((status) -> {
		File articleErrorFile = null;
		ArrayList<DividendRateActErrorDTO> errorList = new ArrayList<>();
		try {

			Map<String, DividendDeductorType> dividendDeductorTypes = this.commonAPIService
					.getAllDividendDeductorTypes().stream()
					.collect(Collectors.toMap(DividendDeductorType::getName, x -> x));
			logger.info("dividendDeductorTypes retrieved sucessfully");
			Map<String, ShareholderCategory> shareholderCategories = this.commonAPIService
					.getAllShareholderCategories(Optional.empty()).stream()
					.collect(Collectors.toMap(ShareholderCategory::getName, x -> x));
			logger.info("dividendInstrumentsMappings retrieved sucessfully");
			Map<Triple<String, String, String>, String> dividendInstrumentsMappings = this.commonAPIService
					.getAllDividendInstrumentsMapping(null, null, null).stream()
					.collect(Collectors.toMap(
							x -> ImmutableTriple.of(x.getDividendDeductorType().getName(),
									x.getShareholderCategory().getName(), x.getResidentialStatus()),
							DividendInstrumentsMapping::getSection));
			List<String> residentialStatuses = Arrays.asList("Resident", "Non Resident");

			DividendRateActExcel excelData = new DividendRateActExcel(workbook);
			masterBatchUpload.setSha256sum(sha256);
			masterBatchUpload.setMismatchCount(0L);
			long dataRowsCount = excelData.getDataRowsCount();
			masterBatchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			Long duplicateCount = 0L;
			Integer successRecordsCount = 0;
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				String error = "";
				Optional<DividendRateActErrorDTO> errorDTO = null;
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
						DividendRateActExcelDTO dividendRateActExcelDTO = excelData.get(rowIndex);
						if (StringUtils.isEmpty(dividendRateActExcelDTO.getDividendDeductorType())
								|| !dividendDeductorTypes
										.containsKey(dividendRateActExcelDTO.getDividendDeductorType())) {
							error = error+"Invalid Dividend Deducter type: "
									+ dividendRateActExcelDTO.getDividendDeductorType()+"\n";
						}
						if (StringUtils.isEmpty(dividendRateActExcelDTO.getShareholderCategory())
								|| !shareholderCategories
										.containsKey(dividendRateActExcelDTO.getShareholderCategory())) {
							error = error+"Invalid Shareholder Category: " + dividendRateActExcelDTO.getShareholderCategory()+"\n";
						}
						String residentialStatus = dividendRateActExcelDTO.getResidentialStatus();
						if (StringUtils.isEmpty(residentialStatus)
								|| !residentialStatuses.contains(residentialStatus)) {
							error =error+ "Invalid Residential Status: " + dividendRateActExcelDTO.getResidentialStatus()+"\n";
						}
						residentialStatus = residentialStatus.equalsIgnoreCase("Resident") ? "RES" : "NR";
						Triple<String, String, String> combination = Triple.of(
								dividendRateActExcelDTO.getDividendDeductorType(),
								dividendRateActExcelDTO.getShareholderCategory(), residentialStatus);
						if (!dividendInstrumentsMappings.containsKey(combination)) {
							error = error+"Invalid combination of Dividend Deducter type: "
									+ dividendRateActExcelDTO.getDividendDeductorType() + ", Shareholder Category: "
									+ dividendRateActExcelDTO.getShareholderCategory() + " and Residential Status: "
									+ residentialStatus + ", as No section found for this combination"+"\n";
						}
						String section = dividendInstrumentsMappings.get(combination);

						Instant applicableTo = null;
						if (StringUtils.isNotBlank(dividendRateActExcelDTO.getApplicableTo())) {
							applicableTo = LocalDate
									.parse(dividendRateActExcelDTO.getApplicableTo(), DEFAULT_DATE_FORMATTER)
									.atStartOfDay(ZoneId.systemDefault()).toInstant();
						}

						if (StringUtils.isEmpty(error)) {
							logger.info("Checking for duplciate records {}");
							Instant applicableFrom = LocalDate
									.parse(dividendRateActExcelDTO.getApplicableFrom(), DEFAULT_DATE_FORMATTER)
									.atStartOfDay(ZoneId.systemDefault()).toInstant();
							// dividendRateActExcelDTO.getApplicableFrom().toInstant().truncatedTo(ChronoUnit.HOURS);

							boolean isDuplicate = this.dividendRateRepository.isDuplicateDividendRateAct(
									dividendDeductorTypes.get(dividendRateActExcelDTO.getDividendDeductorType())
											.getId(),
									shareholderCategories.get(dividendRateActExcelDTO.getShareholderCategory()).getId(),
									residentialStatus, section, applicableFrom, applicableTo);
							if (isDuplicate) {
								duplicateCount++;
								logger.info("Record is duplicate {}");
							} else {
								try {
								DividendRateAct dividendRateAct = DividendRateAct.of(
										dividendDeductorTypes.get(dividendRateActExcelDTO.getDividendDeductorType()),
										shareholderCategories.get(dividendRateActExcelDTO.getShareholderCategory()),
										residentialStatus, section, dividendRateActExcelDTO.getApplicaationRate(),
										dividendRateActExcelDTO.getThreshHoldLimit(), applicableFrom, applicableTo,
										userName);
								dividendRateAct = this.dividendRateRepository.saveDividendRateAct(dividendRateAct);
								successRecordsCount++;
								logger.info("Dividend Rate Act saved Sucessfully with data " + dividendRateAct);

								}catch(Exception e) {
									errorCount++;
									DividendRateActErrorDTO errorDto=excelData.getErrorDTO(rowIndex);
									errorDto.setReason(e.getMessage());
									errorList.add(errorDto);
								}
							}
						} else {
							errorCount++;
							DividendRateActErrorDTO errorDto=excelData.getErrorDTO(rowIndex);
							errorDto.setReason(error);
							errorList.add(errorDto);
						}

					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						DividendRateActErrorDTO problematicDataError = excelData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to process row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			}

			if(!errorList.isEmpty() ) {
				articleErrorFile=generateDividendRateActErrorFile(errorList, excelData.getHeaders());
			}
			masterBatchUpload.setSuccessCount((long) successRecordsCount);
			masterBatchUpload.setFailedCount(errorCount);
			masterBatchUpload.setProcessed(successRecordsCount);
			masterBatchUpload.setDuplicateCount(duplicateCount);
			masterBatchUpload.setStatus("Processed");
			//masterBatchUpload.setCreatedDate(Instant.now());
			masterBatchUpload.setProcessEndTime(new Date());
			masterBatchUpload.setCreatedBy(userName);
		

		} catch (Exception e) {
			masterBatchUpload.setStatus("Process failed");
			logger.error("Exception occurred :", e);

		}
		 try {
			masterBatchUploadService.masterBatchUpload(masterBatchUpload, null, assesssmentYear, assessmentMonth,
					userName, articleErrorFile, uploadType);
		} catch (Exception e) {
			logger.info("Exception occured while updating batch upload record :"+e.getMessage()+"{}");
		}
		});
	}
	
	public File generateDividendRateActErrorFile(ArrayList<DividendRateActErrorDTO> errorList,List<String> headers) throws Exception {
		ArrayList<String> alist=new ArrayList<>();
		headers.stream().forEach(n->alist.add(n));
		alist.addAll(1, Arrays.asList("ERROR MESSAGE"));
		Resource resource = resourceLoader.getResource("classpath:template/" + "RATE_ACT_ERROR.xlsx");
		File file = new File("Dividend Rate Act Error" + new Date().getTime() + ".xlsx");
        InputStream input = resource.getInputStream();
		try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
			XSSFSheet sheet = wb.getSheetAt(0);
		setExtractDataForDeductorMaster(errorList, sheet,  alist);
		OutputStream out2 = new FileOutputStream(file);
		wb.write(out2);
		
		}
		
		
		return file;
	}
	private Map<String,String> mapActErrorHeadersAndValue(){
		Map<String,String> map=new HashedMap<>();
		map.put("S. No", "");
		map.put("ERROR MESSAGE", "reason");
		map.put("Type of deductor", "dividendDeductorType");
		map.put("Category of shareholder (Recipient)", "shareholderCategory");
		map.put("Residential Status (Resident/ Non Resident)", "residentialStatus");
		map.put("Threshold Limit", "threshHoldLimit");
		map.put("Applicable rate (%)", "applicaationRate");
		map.put("Applicable From (yyyy-MM-dd)", "applicableFrom");
		map.put("Applicable To (yyyy-MM-dd)", "applicableTo");
		return map;
	}
	
	/**
	 * this method is responsible for populating records in data row
	 * it will fetch the error dto fields based on header name from a map which is mapped inside another method
	 * using fields it will be fetching the vaue from dto class and will write into the cells
	 * @param errorList
	 * @param worksheet
	 * @param headerNames
	 * @throws Exception
	 */
	private void setExtractDataForDeductorMaster(ArrayList<DividendRateActErrorDTO> errorList,
			XSSFSheet worksheet, ArrayList<String> headerNames) throws Exception {
		//mapping headers and fields
		Map<String,String> map=mapActErrorHeadersAndValue();
		if (!errorList.isEmpty()) {
			int dataRowsStartIndex = 1;
			
			for (int i = 0; i < errorList.size(); i++) {
				DividendRateActErrorDTO errorDTO = errorList.get(i);
				XSSFRow row1 = worksheet.createRow(dataRowsStartIndex++);
				row1.setHeightInPoints((2 * worksheet.getDefaultRowHeightInPoints()));
				int cellIndex=0;
				
				for(String header:headerNames) {
					//getting field name by passing the filed name dynamically , the field name will be fetched 
					// based on the header name which is mapped inside another method
					
					if(header.equals("S. No")) {
						row1.createCell(cellIndex).setCellValue(i+1+"");
					}else {
					Field f=DividendRateActErrorDTO.class.getDeclaredField(map.get(header).trim());
					f.setAccessible(true);
					//getting 
					String value=f.get(errorDTO)==null?"":f.get(errorDTO).toString();
					row1.createCell(cellIndex).setCellValue(value);
					}
					cellIndex++;
				}
			}
		}
	}
	
	
	@Async
	public void saveDividendRateTreatyData(XSSFWorkbook workbook, MultipartFile file, String sha256,
			Integer assesssmentYear, Integer assessmentMonth, String userName, MasterBatchUpload masterBatchUpload,
			String uploadType) throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		
		MultiTenantContext.setTenantId("master");
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.executeWithoutResult((status) -> {
		File errorFile=null;
		try {
			Integer lastRowNumber;
			Integer successCount = 0;
			Long errorCount = 0l;
			Long totalRecordCount = 0l;
			DataFormatter formatter = new DataFormatter();
			XSSFSheet sheet = workbook.getSheetAt(0);
			List<DividendRateTreatyErrorDTO> errorList=new ArrayList<>();
			

		/*	logger.info("Retrieving Dividend deductor type, share holder catagory and dividendInstrumentsMappings{}");
			Map<String, DividendDeductorType> dividendDeductorTypes = this.commonAPIService
					.getAllDividendDeductorTypes().stream()
					.collect(Collectors.toMap(DividendDeductorType::getName, x -> x));
			Map<String, ShareholderCategory> shareholderCategories = this.commonAPIService
					.getAllShareholderCategories(Optional.empty()).stream()
					.collect(Collectors.toMap(ShareholderCategory::getName, x -> x));
			Map<Triple<String, String, String>, String> dividendInstrumentsMappings = this.commonAPIService
					.getAllDividendInstrumentsMapping(null, null, null).stream()
					.collect(Collectors.toMap(
							x -> ImmutableTriple.of(x.getDividendDeductorType().getName(),
									x.getShareholderCategory().getName(), x.getResidentialStatus()),
							DividendInstrumentsMapping::getSection));
			List<String> residentialStatuses = Arrays.asList("Resident", "Non Resident");*/

			Map<String, Country> countries = commonAPIService.getCountries().stream()
					.collect(Collectors.toMap(Country::getName, x -> x));

			lastRowNumber = sheet.getLastRowNum();
			for (int index = 2; index <= lastRowNumber; index++) {
				DividendRateTreatyErrorDTO errorDto=new DividendRateTreatyErrorDTO();
				XSSFRow row = sheet.getRow(index);
				String error = "";
				if(row!=null && !isRowEmpty(row)) {
				logger.info("Iterating first row no "+index+" {}");
				totalRecordCount++;
				String countryName = formatter.formatCellValue(row.getCell(1));
				if(StringUtils.isNotBlank(countryName)) {
					countryName=countryName.trim();
				}
				errorDto.setCountryName(countryName);
				if (StringUtils.isEmpty(countryName) || !countries.containsKey(countryName.toUpperCase())) {
					error = error+" Invalid Country Name: " + countryName+"\n";
				}else {
				countryName = countryName.toUpperCase();
				}

				String taxTreatyClause = formatter.formatCellValue(row.getCell(2));
				errorDto.setTaxTreatyClause(taxTreatyClause);
				if (StringUtils.isEmpty(taxTreatyClause)) {
					error =error+ "Invalid Tax Treaty Clause: " + taxTreatyClause+"\n";
				}else {
				taxTreatyClause = taxTreatyClause.contains(".0") ? taxTreatyClause.replace(".0", "") : taxTreatyClause;
				}
				
				String mfnClauseStr = formatter.formatCellValue(row.getCell(3));
				errorDto.setMfnClauseStr(mfnClauseStr);
				boolean mfnClauseExists = false;
				if (StringUtils.isEmpty(mfnClauseStr)
						|| !(mfnClauseStr.equalsIgnoreCase("YES") || mfnClauseStr.equalsIgnoreCase("NO"))) {
					error =error+ "Invalid MFN Cluse flag: " + mfnClauseStr+"\n";
				} else {
					mfnClauseExists = mfnClauseStr.equalsIgnoreCase("YES") ? true : false;
				}

				String mliArticle8ApplicableStr = formatter.formatCellValue(row.getCell(4));
				errorDto.setMliArticle8Applicable(mliArticle8ApplicableStr);
				boolean mliArticle8Applicable = false;
				if (!(StringUtils.isEmpty(mliArticle8ApplicableStr) || mliArticle8ApplicableStr.equalsIgnoreCase("YES")
						|| mliArticle8ApplicableStr.equalsIgnoreCase("NO")
						|| mliArticle8ApplicableStr.equalsIgnoreCase("NA"))) {
					error = error+"Invalid MLI Article 8 Applicable: " + mliArticle8ApplicableStr+"\n";
				} else {
					mliArticle8Applicable = mliArticle8ApplicableStr.equalsIgnoreCase("YES") ? true : false;
				}

				String mliPptConditionSatisfiedStr = formatter.formatCellValue(row.getCell(5));
				errorDto.setMliPptConditionSatisfiedStr(mliPptConditionSatisfiedStr);
				boolean mliPptConditionSatisfied = false;
				if (!(StringUtils.isEmpty(mliPptConditionSatisfiedStr)
						|| mliPptConditionSatisfiedStr.equalsIgnoreCase("YES")
						|| mliPptConditionSatisfiedStr.equalsIgnoreCase("NO")
						|| mliPptConditionSatisfiedStr.equalsIgnoreCase("NA"))) {
					error =error+ "Invalid MLI PPT Condition Satisfied: " + mliPptConditionSatisfiedStr+"\n";
				} else {
					mliPptConditionSatisfied = mliPptConditionSatisfiedStr.equalsIgnoreCase("YES") ? true : false;
				}

				String mliSlobConditionSatisfiedStr = formatter.formatCellValue(row.getCell(6));
				errorDto.setMliSlobConditionSatisfiedStr(mliSlobConditionSatisfiedStr);
				boolean mliSlobConditionSatisfied = false;
				if (!(StringUtils.isEmpty(mliSlobConditionSatisfiedStr)
						|| mliSlobConditionSatisfiedStr.equalsIgnoreCase("YES")
						|| mliSlobConditionSatisfiedStr.equalsIgnoreCase("NO")
						|| mliSlobConditionSatisfiedStr.equalsIgnoreCase("NA"))) {
					error =error+ "Invalid MLI SLOB Condition Satisfied: " + mliSlobConditionSatisfiedStr+"\n";
				} else {
					mliSlobConditionSatisfied = mliSlobConditionSatisfiedStr.equalsIgnoreCase("YES") ? true : false;
				}

				String mfnAvailedCompanyTaxRateStr = formatter.formatCellValue(row.getCell(7));
				errorDto.setMfnAvailedCompanyTaxRateStr(mfnAvailedCompanyTaxRateStr);
				BigDecimal mfnAvailedCompanyTaxRate = null;
				try {
					mfnAvailedCompanyTaxRate = StringUtils.isEmpty(mfnAvailedCompanyTaxRateStr)
							|| mfnAvailedCompanyTaxRateStr.equalsIgnoreCase("NA")
									? null
									: new BigDecimal(mfnAvailedCompanyTaxRateStr.contains("%")
											? mfnAvailedCompanyTaxRateStr.replace("%", "").trim()
											: mfnAvailedCompanyTaxRateStr);
				} catch (NumberFormatException nfe) {
					error =error+ "Invalid Company Rax rate when MFN Clause availed: " + mfnAvailedCompanyTaxRateStr
							+ ", expected in numeric or decimal format"+"\n";
				}

				String mfnAvailedNonCompanyTaxRateStr = formatter.formatCellValue(row.getCell(8));
				errorDto.setMfnAvailedNonCompanyTaxRateStr(mfnAvailedNonCompanyTaxRateStr);
				BigDecimal mfnAvailedNonCompanyTaxRate = null;
				try {
					mfnAvailedNonCompanyTaxRate = StringUtils.isEmpty(mfnAvailedNonCompanyTaxRateStr)
							|| mfnAvailedNonCompanyTaxRateStr.equalsIgnoreCase("NA")
									? null
									: new BigDecimal(mfnAvailedNonCompanyTaxRateStr.contains("%")
											? mfnAvailedNonCompanyTaxRateStr.replace("%", "").trim()
											: mfnAvailedNonCompanyTaxRateStr);
				} catch (NumberFormatException nfe) {
					error =error+ "Invalid Non Company Rax rate when MFN Clause availed: " + mfnAvailedNonCompanyTaxRateStr
							+ ", expected in numeric or decimal format"+"\n";
				}

				String mfnNotAvailedCompanyTaxRateStr = formatter.formatCellValue(row.getCell(9));
				errorDto.setMfnNotAvailedCompanyTaxRateStr(mfnNotAvailedCompanyTaxRateStr);
				BigDecimal mfnNotAvailedCompanyTaxRate = null;
				try {
					mfnNotAvailedCompanyTaxRate = StringUtils.isEmpty(mfnNotAvailedCompanyTaxRateStr)
							|| mfnNotAvailedCompanyTaxRateStr.equalsIgnoreCase("NA")
									? null
									: new BigDecimal(mfnNotAvailedCompanyTaxRateStr.contains("%")
											? mfnNotAvailedCompanyTaxRateStr.replace("%", "").trim()
											: mfnNotAvailedCompanyTaxRateStr);
				} catch (NumberFormatException nfe) {
					error = error+"Invalid Company Rax rate when MFN Clause not availed: " + mfnNotAvailedCompanyTaxRateStr
							+ ", expected in numeric or decimal format"+"\n";
				}

				String mfnNotAvailedNonCompanyTaxRateStr = formatter.formatCellValue(row.getCell(10));
				errorDto.setMfnNotAvailedNonCompanyTaxRateStr(mfnNotAvailedNonCompanyTaxRateStr);
				BigDecimal mfnNotAvailedNonCompanyTaxRate = null;
				try {
					mfnNotAvailedNonCompanyTaxRate = StringUtils.isEmpty(mfnNotAvailedNonCompanyTaxRateStr)
							|| mfnNotAvailedNonCompanyTaxRateStr.equalsIgnoreCase("NA")
									? null
									: new BigDecimal(mfnNotAvailedNonCompanyTaxRateStr.contains("%")
											? mfnNotAvailedNonCompanyTaxRateStr.replace("%", "").trim()
											: mfnNotAvailedNonCompanyTaxRateStr);
				} catch (NumberFormatException nfe) {
					error =error+ "Invalid Non Company Rax rate when MFN Clause not availed: "
							+ mfnNotAvailedNonCompanyTaxRateStr + ", expected in numeric or decimal format"+"\n";
				}
				String foreignCompShareholdingInIndCompStr = formatter.formatCellValue(row.getCell(11));
				errorDto.setForeignCompShareholdingInIndCompStr(foreignCompShareholdingInIndCompStr);
				Range foreignCompShareholdingInIndComp = null;
				if (!StringUtils.isEmpty(foreignCompShareholdingInIndCompStr)
						&& !foreignCompShareholdingInIndCompStr.equalsIgnoreCase("NA")) {
					try {
						foreignCompShareholdingInIndComp = Range
								.fromString(foreignCompShareholdingInIndCompStr.contains("%")
										? foreignCompShareholdingInIndCompStr.replace("%", "").trim()
										: foreignCompShareholdingInIndCompStr);
					} catch (Exception e) {
					}
				}

				// Country specific rules >>>
				// 162-PORTUGAL, 31-ZAMBIA, 8-ICELAND, 153-KUWAIT, 225-UNITED KINGDOM
				CountrySpecificRules countrySpecificRules = null;
				String shareholdingInForeignCompanyByPersonsOtherThanIndividualResidentsStr="";
				if(StringUtils.isNotBlank(countryName) && countries.get(countryName.trim())!=null) {
				if ( CountrySpecificRules.SPECIFIC_RULES_COUNTRIES.contains(countries.get(countryName.trim()).getId())) {
					if (countryName.equals("PORTUGAL") || countryName.equals("ZAMBIA")) {
						String periodOfShareholdingStr = formatter.formatCellValue(row.getCell(12));
						errorDto.setPeriodOfShareholdingStr(periodOfShareholdingStr);
						if (!StringUtils.isEmpty(periodOfShareholdingStr)
								&& !periodOfShareholdingStr.equalsIgnoreCase("NA")) {
							try {
								periodOfShareholdingStr = periodOfShareholdingStr.contains("years")
										? periodOfShareholdingStr.replace("years", "").trim()
										: periodOfShareholdingStr;
								Range periodOfShareholding = Range.fromString(periodOfShareholdingStr);
								countrySpecificRules = CountrySpecificRules.of(Type.PERIOD_OF_SHAREHOLDING,
										periodOfShareholding);
							} catch (Exception e) {
							}
						}
					} else if (countryName.equals("ICELAND")) {
						 shareholdingInForeignCompanyByPersonsOtherThanIndividualResidentsStr = formatter
								.formatCellValue(row.getCell(13));
						errorDto.setShareholdingInForeignCompanyByPersonsOtherThanIndividualResidentsStr(shareholdingInForeignCompanyByPersonsOtherThanIndividualResidentsStr);
						Range shareholdingInForeignCompanyByPersonsOtherThanIndividualResidents = null;
						if (!StringUtils.isEmpty(shareholdingInForeignCompanyByPersonsOtherThanIndividualResidentsStr)
								&& !shareholdingInForeignCompanyByPersonsOtherThanIndividualResidentsStr
										.equalsIgnoreCase("NA")) {
							try {
								shareholdingInForeignCompanyByPersonsOtherThanIndividualResidentsStr = shareholdingInForeignCompanyByPersonsOtherThanIndividualResidentsStr
										.contains("%")
												? shareholdingInForeignCompanyByPersonsOtherThanIndividualResidentsStr
														.replace("%", "").trim()
												: shareholdingInForeignCompanyByPersonsOtherThanIndividualResidentsStr;
								shareholdingInForeignCompanyByPersonsOtherThanIndividualResidents = Range.fromString(
										shareholdingInForeignCompanyByPersonsOtherThanIndividualResidentsStr);
							} catch (Exception e) {
							}
						}
						String isDividendTaxableAtArateLowerThanCorporateTaxRateStr = formatter
								.formatCellValue(row.getCell(14));
						errorDto.setIsDividendTaxableAtArateLowerThanCorporateTaxRateStr(isDividendTaxableAtArateLowerThanCorporateTaxRateStr);
						boolean isDividendTaxableAtArateLowerThanCorporateTaxRate = false;
						if (!(StringUtils.isEmpty(isDividendTaxableAtArateLowerThanCorporateTaxRateStr)
								|| isDividendTaxableAtArateLowerThanCorporateTaxRateStr.equalsIgnoreCase("YES")
								|| isDividendTaxableAtArateLowerThanCorporateTaxRateStr.equalsIgnoreCase("NO")
								|| isDividendTaxableAtArateLowerThanCorporateTaxRateStr.equalsIgnoreCase("NA"))) {
							error = error+"Invalid Is Dividend Taxable At a Rate Lower Than  Corporate Tax Rate: "
									+ isDividendTaxableAtArateLowerThanCorporateTaxRateStr+"\n";
						} else {
							isDividendTaxableAtArateLowerThanCorporateTaxRate = isDividendTaxableAtArateLowerThanCorporateTaxRateStr
									.equalsIgnoreCase("YES") ? true : false;
						}
						countrySpecificRules = CountrySpecificRules.of(
								Type.SHAREHOLDING_IN_FOREIGN_COMPANY_BY_PERSONS_OTHER_THAN_INDIVIDUAL_RESIDENTS,
								shareholdingInForeignCompanyByPersonsOtherThanIndividualResidents);
						countrySpecificRules.add(Type.IS_DIVIDEND_TAXABLE_AT_A_RATE_LOWER_THAN_CORPORATE_TAX_RATE,
								isDividendTaxableAtArateLowerThanCorporateTaxRate);
					} else if (countryName.equals("KUWAIT")) {
						String beneficiaryIsForeignGovtOrPoliticalSubDivisionOrAlocalAuthorityOrCentralBankOrOtherGovtBodyStr = formatter
								.formatCellValue(row.getCell(15));
						errorDto.setBeneficiaryIsForeignGovtOrPoliticalSubDivisionOrAlocalAuthorityOrCentralBankOrOtherGovtBodyStr(beneficiaryIsForeignGovtOrPoliticalSubDivisionOrAlocalAuthorityOrCentralBankOrOtherGovtBodyStr);
						if (!(StringUtils.isEmpty(
								beneficiaryIsForeignGovtOrPoliticalSubDivisionOrAlocalAuthorityOrCentralBankOrOtherGovtBodyStr)
								|| beneficiaryIsForeignGovtOrPoliticalSubDivisionOrAlocalAuthorityOrCentralBankOrOtherGovtBodyStr
										.equalsIgnoreCase("YES")
								|| beneficiaryIsForeignGovtOrPoliticalSubDivisionOrAlocalAuthorityOrCentralBankOrOtherGovtBodyStr
										.equalsIgnoreCase("NO")
								|| beneficiaryIsForeignGovtOrPoliticalSubDivisionOrAlocalAuthorityOrCentralBankOrOtherGovtBodyStr
										.equalsIgnoreCase("NA"))) {
							error =error+ "Invalid Is Dividend Taxable At a Rate Lower Than  Corporate Tax Rate: "
									+ beneficiaryIsForeignGovtOrPoliticalSubDivisionOrAlocalAuthorityOrCentralBankOrOtherGovtBodyStr+"\n";
						} else {
							boolean beneficiaryIsForeignGovtOrPoliticalSubDivisionOrAlocalAuthorityOrCentralBankOrOtherGovtBody = beneficiaryIsForeignGovtOrPoliticalSubDivisionOrAlocalAuthorityOrCentralBankOrOtherGovtBodyStr
									.equalsIgnoreCase("YES") ? true : false;
							countrySpecificRules = CountrySpecificRules.of(
									Type.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_SUB_DIVISION_OR_A_LOCAL_AUTHORITY_OR_CENTRAL_BANK_OR_OTHER_GOVT_BODY,
									beneficiaryIsForeignGovtOrPoliticalSubDivisionOrAlocalAuthorityOrCentralBankOrOtherGovtBody);
						}
					} else if (countryName.equals("UNITED KINGDOM")) {
						String dividendDerivedFromImmovablePropertyByAnInvestmentVehicleWhoseIncomeFromSuchImmovablePropertyIsExemptFromTaxStr = formatter
								.formatCellValue(row.getCell(16));
						errorDto.setDividendDerivedFromImmovablePropertyByAnInvestmentVehicleWhoseIncomeFromSuchImmovablePropertyIsExemptFromTaxStr(dividendDerivedFromImmovablePropertyByAnInvestmentVehicleWhoseIncomeFromSuchImmovablePropertyIsExemptFromTaxStr);
						if (!(StringUtils.isEmpty(
								dividendDerivedFromImmovablePropertyByAnInvestmentVehicleWhoseIncomeFromSuchImmovablePropertyIsExemptFromTaxStr)
								|| dividendDerivedFromImmovablePropertyByAnInvestmentVehicleWhoseIncomeFromSuchImmovablePropertyIsExemptFromTaxStr
										.equalsIgnoreCase("YES")
								|| dividendDerivedFromImmovablePropertyByAnInvestmentVehicleWhoseIncomeFromSuchImmovablePropertyIsExemptFromTaxStr
										.equalsIgnoreCase("NO")
								|| dividendDerivedFromImmovablePropertyByAnInvestmentVehicleWhoseIncomeFromSuchImmovablePropertyIsExemptFromTaxStr
										.equalsIgnoreCase("NA"))) {
							error =error+ "Invalid Dividend derived from Immovable Property by an Investment Vehicle whose income from such Immovable Property is exempt from tax: "
									+ dividendDerivedFromImmovablePropertyByAnInvestmentVehicleWhoseIncomeFromSuchImmovablePropertyIsExemptFromTaxStr+"\n";
						} else {
							boolean dividendDerivedFromImmovablePropertyByAnInvestmentVehicleWhoseIncomeFromSuchImmovablePropertyIsExemptFromTax = dividendDerivedFromImmovablePropertyByAnInvestmentVehicleWhoseIncomeFromSuchImmovablePropertyIsExemptFromTaxStr
									.equalsIgnoreCase("YES") ? true : false;
							countrySpecificRules = CountrySpecificRules.of(
									Type.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_BY_AN_INVESTMENT_VEHICLE_WHOSE_INCOME_FROM_SUCH_IMMOVABLE_PROPERTY_IS_EXEMPT_FROM_TAX,
									dividendDerivedFromImmovablePropertyByAnInvestmentVehicleWhoseIncomeFromSuchImmovablePropertyIsExemptFromTax);
						}
					}
				}
			}//end of if block having null check for country name

				String applicableFromStr = formatter.formatCellValue(row.getCell(17));
				errorDto.setApplicableFromStr(applicableFromStr);
				Instant applicableFrom = null;
				try {
					if (StringUtils.isEmpty(applicableFromStr)) {
						error =error+ "Applicable From date is required"+"\n";
					} else {
						applicableFrom = LocalDate.parse(applicableFromStr, DEFAULT_DATE_FORMATTER)
								.atStartOfDay(ZoneId.systemDefault()).toInstant();
					}
				} catch (DateTimeParseException nfe) {
					error = error+"Invalid Applicable From date: " + applicableFromStr
							+ ", expected in date in format: yyyy-MM-dd"+"\n";
				}
				String applicableToStr = formatter.formatCellValue(row.getCell(18));
				errorDto.setApplicableToStr(applicableToStr);
				Instant applicableTo = null;
				try {
					if (!StringUtils.isEmpty(applicableToStr)) {
						applicableTo = LocalDate.parse(applicableToStr, DEFAULT_DATE_FORMATTER)
								.atStartOfDay(ZoneId.systemDefault()).toInstant();
						if (!applicableFrom.isBefore(applicableTo)) {
							error =error+ "Applicable From date: " + applicableFrom
									+ " must be less than Applicable To date: " + applicableTo+"\n";
						}
					}
				} catch (DateTimeParseException nfe) {
					error =error+ "Invalid Applicable From date: " + applicableToStr
							+ ", expected in date in format: yyyy-MM-dd"+"\n";
				}
				if (StringUtils.isEmpty(error)) {
					boolean isDuplicate = dividendRateRepository.isDuplicateDividendRateTreaty(
							countries.get(countryName), taxTreatyClause, mfnClauseExists, mliArticle8Applicable,
							mliPptConditionSatisfied, mliSlobConditionSatisfied, foreignCompShareholdingInIndComp,
							countrySpecificRules, applicableFrom, applicableTo,mfnNotAvailedCompanyTaxRate,mfnNotAvailedNonCompanyTaxRate);
					if (isDuplicate) {
						error = error+"Duplicate record"+"\n";
						errorCount++;	
						errorDto.setReason(error);
						errorList.add(errorDto);
					} else {
						try {
							if (mfnClauseExists) {
								DividendRateTreaty dividendRateTreaty = DividendRateTreaty
										.of(countries.get(countryName))
										.applicableDateRange(applicableFrom, applicableTo).treatyClause(taxTreatyClause)
										.mli(mliArticle8Applicable, mliPptConditionSatisfied, mliSlobConditionSatisfied)
										.mfnClauseExist()
										.mfnClauseAvailed(mfnAvailedCompanyTaxRate, mfnAvailedNonCompanyTaxRate)
										.mfnClauseNotAvailed(mfnNotAvailedCompanyTaxRate,
												mfnNotAvailedNonCompanyTaxRate)
										.foreignCompShareholdingInIndComp(foreignCompShareholdingInIndComp)
										.countrySpecificRules(countrySpecificRules).build(userName);
								dividendRateRepository.saveDividendRateTreaty(dividendRateTreaty);
							} else {
								DividendRateTreaty dividendRateTreaty = DividendRateTreaty
										.of(countries.get(countryName))
										.applicableDateRange(applicableFrom, applicableTo).treatyClause(taxTreatyClause)
										.mli(mliArticle8Applicable, mliPptConditionSatisfied, mliSlobConditionSatisfied)
										.mfnClauseDoesNotExist()
										.mfnClauseNotAvailed(mfnNotAvailedCompanyTaxRate,
												mfnNotAvailedNonCompanyTaxRate)
										.foreignCompShareholdingInIndComp(foreignCompShareholdingInIndComp)
										.countrySpecificRules(countrySpecificRules).build(userName);
								dividendRateRepository.saveDividendRateTreaty(dividendRateTreaty);
							}
							successCount++;
						} catch (Exception e) {
							errorCount++;
							errorDto.setReason(e.toString().contains(":")?e.toString().split(":")[1]:e.toString());
							errorList.add(errorDto);
						}
					}
				} else {
					errorCount++;
					errorDto.setReason(error);
					errorList.add(errorDto);
				}
				

			} // for block iterating rows
				
			}//end of if block checking non empty row 
			if(!errorList.isEmpty()) {
				errorFile=generateDividendRateTreatyErrorFile(errorList);
			}
			masterBatchUpload.setProcessed(successCount);
			masterBatchUpload.setSuccessCount((long)successCount);
			masterBatchUpload.setRowsCount(totalRecordCount);
			masterBatchUpload.setProcessEndTime(new Timestamp(System.currentTimeMillis()));
			masterBatchUpload.setModifiedBy(userName);
			masterBatchUpload.setModifiedDate(new Date().toInstant());
			masterBatchUpload.setStatus("Processed");
			masterBatchUpload.setFailedCount(errorCount);

		} catch (Exception e) {
			logger.info("Exception ocured while processing Dividend Rate Treaty file"+e.getMessage());
		}
		 try {
			masterBatchUploadService.masterBatchUpload(masterBatchUpload, null, assesssmentYear, assessmentMonth,
					userName, errorFile, uploadType);
		} catch (Exception  e) {
			logger.info("Exception occured while updating batch upload table {}");
		}
		});
	}
	
	public static boolean isRowEmpty(XSSFRow row) {
		for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
			XSSFCell cell = row.getCell(i);
			CellType cellType = cell == null ? null : cell.getCellType();
			if (cellType != null && cellType != CellType.BLANK) {
				String cellValue = cellType == CellType.STRING ? cell.getStringCellValue()
						: cell.getNumericCellValue() + "";
				if (StringUtils.isNotEmpty(cellValue)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public File generateDividendRateTreatyErrorFile(List<DividendRateTreatyErrorDTO> errorList) throws Exception {
		List<String> alist=Arrays.asList(" S. No.","ERROR MESSAGE"," Name of Country","Relevant Tax Treaty Clause","Most Favoured Nation ('MFN') Clause Exists",
				"Article 8 of Multilateral Instrument ('MLI') (>=365 days period Of shareholding)","MLI Principle Purpose Test Condition Satisfied",
				"MLI Simplified Limitation on Benefit Condition Satisifed","Withholding Tax Rate Applicable To Company","Withholding Tax Rate Applicable To Non-Company",
				"Withholding Tax Rate Applicable To Company","Withholding Tax Rate Applicable To Non-Company","Foreign Company Shareholding In Indian Company (%)",
				" Period Of Shareholding (Only for countries PORTUGAL, ZAMBIA)","Shareholding In Foreign Company by persons other than individuals residents (Only for country ICELAND) ",
				"Is Dividend Taxable At a Rate Lower Than  Corporate Tax Rate (Only for country ICELAND) ",
				"Beneficial Owner of Dividend is Foreign Government or  Political Sub Division or a Local Authority or\n"
				+ " the Central Bank  or\n"
				+ " other Governmental Agencies or Governmental Financial Institutions (Only for country KUWAIT) ",
				"Dividend derived from Immovable Property by an Investment Vehicle whose income from such Immovable Property is exempt from tax (Only for country UNITED KINGDOM) ",
				"Applicable From  (yyyy-MM-dd)","Applicable To  (yyyy-MM-dd)");
		
		Resource resource = resourceLoader.getResource("classpath:template/" + "Dividend Rate Treaty.xlsx");
		File file = new File("Dividend Rate Treaty Error" + new Date().getTime() + ".xlsx");
        InputStream input = resource.getInputStream();
		try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
			XSSFSheet sheet = wb.getSheetAt(0);
			setExtractDataForRateTreaty(errorList, sheet,  alist);
		OutputStream out2 = new FileOutputStream(file);
		wb.write(out2);
		
		}
		
		
		return file;
	}
	private Map<String,String> mapTreatyErrorHeadersAndValue(){
		Map<String,String> map=new HashedMap<>();
		map.put("S. No", "");
		map.put("ERROR MESSAGE", "reason");
		map.put(" Name of Country", "countryName");
		map.put("Relevant Tax Treaty Clause", "taxTreatyClause");
		map.put("Most Favoured Nation ('MFN') Clause Exists", "mfnClauseStr");
		map.put("Article 8 of Multilateral Instrument ('MLI') (>=365 days period Of shareholding)", "mliArticle8ApplicableStr");
		map.put("MLI Principle Purpose Test Condition Satisfied", "mliPptConditionSatisfiedStr");
		map.put("MLI Simplified Limitation on Benefit Condition Satisifed", "mliSlobConditionSatisfiedStr");
		map.put("Withholding Tax Rate Applicable To Company", "mfnAvailedCompanyTaxRateStr");
		
		map.put("Withholding Tax Rate Applicable To Non-Company", "mfnAvailedNonCompanyTaxRateStr");
		map.put("Withholding Tax Rate Applicable To Company", "mfnNotAvailedCompanyTaxRateStr");
		map.put("Withholding Tax Rate Applicable To Non-Company", "mfnNotAvailedNonCompanyTaxRateStr");
		map.put("Foreign Company Shareholding In Indian Company (%)", "foreignCompShareholdingInIndCompStr");
		map.put(" Period Of Shareholding (Only for countries PORTUGAL, ZAMBIA)", "periodOfShareholdingStr");
		map.put("Shareholding In Foreign Company by persons other than individuals residents (Only for country ICELAND) ", "shareholdingInForeignCompanyByPersonsOtherThanIndividualResidentsStr");
		map.put("Is Dividend Taxable At a Rate Lower Than  Corporate Tax Rate (Only for country ICELAND) ", "isDividendTaxableAtArateLowerThanCorporateTaxRateStr");
		map.put("Beneficial Owner of Dividend is Foreign Government or  Political Sub Division or a Local Authority or\n"
				+ " the Central Bank  or\n"
				+ " other Governmental Agencies or Governmental Financial Institutions (Only for country KUWAIT) ", "beneficiaryIsForeignGovtOrPoliticalSubDivisionOrAlocalAuthorityOrCentralBankOrOtherGovtBodyStr");
		map.put("Dividend derived from Immovable Property by an Investment Vehicle whose income from such Immovable Property is exempt from tax (Only for country UNITED KINGDOM) ", "dividendDerivedFromImmovablePropertyByAnInvestmentVehicleWhoseIncomeFromSuchImmovablePropertyIsExemptFromTaxStr");
		map.put("Applicable From  (yyyy-MM-dd)", "applicableFromStr");
		map.put("Applicable To  (yyyy-MM-dd)", "applicableToStr");
		return map;
	}
	
	/**
	 * this method is responsible for populating records in data row
	 * it will fetch the error dto fields based on header name from a map which is mapped inside another method
	 * using fields it will be fetching the vaue from dto class and will write into the cells
	 * @param errorList
	 * @param worksheet
	 * @param headerNames
	 * @throws Exception
	 */
	private void setExtractDataForRateTreaty(List<DividendRateTreatyErrorDTO> errorList,
			XSSFSheet worksheet, List<String> headerNames) throws Exception {
		//mapping headers and fields
		Map<String,String> map=mapTreatyErrorHeadersAndValue();
		if (!errorList.isEmpty()) {
			int dataRowsStartIndex = 2;
			
			for (int i = 0; i < errorList.size(); i++) {
				DividendRateTreatyErrorDTO errorDTO = errorList.get(i);
				XSSFRow row1 = worksheet.createRow(dataRowsStartIndex++);
				row1.setHeightInPoints((2 * worksheet.getDefaultRowHeightInPoints()));
				int cellIndex=0;
				
				for(String header:headerNames) {
					//getting field name by passing the filed name dynamically , the field name will be fetched 
					// based on the header name which is mapped inside another method
					
					if(header.equals(" S. No.")) {
						row1.createCell(cellIndex).setCellValue(i+1+"");
					}else {
					Field field=DividendRateTreatyErrorDTO.class.getDeclaredField(map.get(header).trim());
					field.setAccessible(true);
					//getting 
					String value=field.get(errorDTO)==null?"":field.get(errorDTO).toString();
					row1.createCell(cellIndex).setCellValue(value);
					}
					cellIndex++;
				}
			}
		}
	}
}
