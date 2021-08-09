package com.ey.in.tds.returns.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tcs.common.domain.dividend.ActSummary;
import com.ey.in.tcs.common.domain.dividend.Form15FileFormat;
import com.ey.in.tcs.common.domain.dividend.Form15FileType;
import com.ey.in.tcs.common.domain.dividend.Form15FilingStatus;
import com.ey.in.tcs.common.domain.dividend.InvoiceShareholderNonResident;
import com.ey.in.tcs.common.domain.dividend.NonResidentWithholdingDetails;
import com.ey.in.tcs.common.domain.dividend.TreatySummary;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.Country;
import com.ey.in.tds.common.domain.dividend.DividendRateTreaty;
import com.ey.in.tds.common.domain.masters.DividendOnboardingInfoMetaData;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterDTO;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterNonResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorTanAddress;
import com.ey.in.tds.common.onboarding.response.dto.DeductorOnboardingInformationResponseDTO;
import com.ey.in.tds.common.returns.jdbc.dto.Form15FilingDetails;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.ReturnType;
import com.ey.in.tds.feign.client.IngestionClient;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.jdbc.dao.Form15FilingDetailsDAO;
import com.ey.in.tds.returns.dividend.validator.Errors;
import com.ey.in.tds.returns.dto.AuthorisedPersonDTO;
import com.ey.in.tds.returns.dto.Form15CAPartADTO;
import com.ey.in.tds.returns.dto.Form15CAPartBDTO;
import com.ey.in.tds.returns.dto.Form15CAPartCDTO;
import com.ey.in.tds.returns.dto.Form15CAPartDDTO;
import com.microsoft.azure.storage.StorageException;

@Service
public class Filing15caService {

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	private IngestionClient ingestionClient;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	protected BlobStorage blobStorage;

	@Autowired
	private Form15FilingDetailsDAO form15FilingDetailsDAO;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private Form15CAXmlGenerationService form15CAXmlGenerationService;

	@Autowired
	private DividendFilingCommonErrorFileService dividendFilingCommonErrorFileService;

	@Autowired
	@Value("${app.ack.num.enabled}")
	private boolean shouldfetchAckNum;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Transactional
	public String generate15CAExcelFilingReport(String deductorTan, String deductorPan, String dateOfPosting,
			String tenantId, String userName, Integer assessmentYear) throws Exception {
		logger.info("Filing 15CA Excel: Started generation for TAN: {}, date of posting : {} ", deductorTan,
				dateOfPosting);

		createFilingReportStatus(deductorTan, assessmentYear, dateOfPosting, Form15FileType.CA, Form15FileFormat.EXCEL,
				ReturnType.REGULAR, userName);

		CompletableFuture.runAsync(() -> this.create15CAFilingReport(deductorTan, deductorPan, dateOfPosting, tenantId,
				userName, assessmentYear));

		return "Request for generating Form 15CA Excel submitted successfully";
	}

	private void create15CAFilingReport(String deductorTan, String deductorPan, String postingDate, String tenantId,
			String userName, Integer assessmentYear) {
		logger.info("Filing 15CA : Started {} generation for TAN: {}, posting Date: {}", "15CA", deductorTan,
				postingDate);
		MultiTenantContext.setTenantId(tenantId);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.executeWithoutResult((status) -> {
			try {
				List<Form15CAPartADTO> form15CAPartADTOS = new ArrayList<>();
				List<Form15CAPartBDTO> form15CAPartBDTOS = new ArrayList<>();
				List<Form15CAPartCDTO> form15CAPartCDTOS = new ArrayList<>();
				List<Form15CAPartDDTO> form15CAPartDDTOS = new ArrayList<>();
				List<Errors> errorList = new ArrayList<>();
				File multipartFile = null;
				AuthorisedPersonDTO authorisedPersonDTO = generateAuthorisedPersonDto(deductorTan, deductorPan,
						tenantId, errorList);
				logger.info("Generation of autherised person dto for 15 CA successfull{}");

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate dateTime = LocalDate.parse(postingDate, formatter);
				List<InvoiceShareholderNonResident> nonResidentShareholders = form15FilingDetailsDAO
						.findAllNonResidentByDateOfPosting(deductorPan, dateTime, assessmentYear);
				logger.info("Feign call successfull to get invoice share holder data {}");
				if (nonResidentShareholders.isEmpty()) {
					Errors error = new Errors();
					error.setMessage("No Shareholder Found For Deductor Pan=" + deductorPan + " ,Financial Year="
							+ assessmentYear + " And Date Of Posting=" + dateTime);
					errorList.add(error);

				}

				if (errorList.isEmpty()) { // if authorized person data is missing do not generate
					for (InvoiceShareholderNonResident nonResidentShareholder : nonResidentShareholders) {
						if (check15CACBFlag(nonResidentShareholder, tenantId, deductorPan).equalsIgnoreCase("Yes")) {
							boolean is15cbGenerated = false;
							boolean isDividendLessThanFiveLakh = (nonResidentShareholder.getDividendAmountRs()
									.compareTo(BigDecimal.valueOf(500000))) < 0;
							boolean isDividendIncomeMoreThan5Lakh = (nonResidentShareholder.getDividendAmountRs()
									.compareTo(BigDecimal.valueOf(500000))) > 0;
							boolean isDividendEqualsToFivelakh = (nonResidentShareholder.getDividendAmountRs()
									.compareTo(BigDecimal.valueOf(500000))) == 0;
							boolean isLdcApplied = false;
							boolean isTreatyApplied = false;
							boolean isActApplied = false;
							if (!Objects.isNull(nonResidentShareholder.getWithholdingDetails())) {
								NonResidentWithholdingDetails withholdingDetails = nonResidentShareholder
										.getWithholdingDetails();
								isLdcApplied = checkBooleanForNull(withholdingDetails.isLdcApplied());
								isTreatyApplied = checkBooleanForNull(withholdingDetails.isTreatyApplied());
								isActApplied = checkBooleanForNull(withholdingDetails.isActApplied());
							}
							if (checkOnboardingInformation(deductorPan, tenantId) && isDividendIncomeMoreThan5Lakh
									&& (!isLdcApplied || (isLdcApplied && isTreatyApplied))) {
								is15cbGenerated = true;
							}

							if (nonResidentShareholder.getIsExempt() == true) {
								Form15CAPartDDTO partD = generate15CAPartDDto(nonResidentShareholder, deductorTan,
										deductorPan, tenantId, errorList);
								if (partD != null)
									form15CAPartDDTOS.add(partD);
							} else if (is15cbGenerated) {
								Form15CAPartCDTO partC = generate15CAPartCDto(nonResidentShareholder, deductorTan,
										deductorPan, tenantId, errorList);
								if (partC != null)
									form15CAPartCDTOS.add(partC);
							} else if (isDividendLessThanFiveLakh || isDividendEqualsToFivelakh) {
								Form15CAPartADTO partA = generate15CAPartADtos(nonResidentShareholder, deductorTan,
										deductorPan, tenantId, errorList);
								if (partA != null)
									form15CAPartADTOS.add(partA);
							} else if (!isDividendLessThanFiveLakh && !isDividendEqualsToFivelakh && isLdcApplied
									&& !isTreatyApplied) {
								Form15CAPartBDTO partB = generate15CAPartBDto(nonResidentShareholder, deductorTan,
										deductorPan, tenantId, errorList);
								if (partB != null)
									form15CAPartBDTOS.add(partB);
							} else {
								logger.info("No matching conditions found for generating 15CA for transaction id : {}",
										nonResidentShareholder.getId());
								Errors error = new Errors();
								error.setCompleteMessage(nonResidentShareholder.getId() + "");
								error.setId(nonResidentShareholder.getFolioNumber());
								error.setMessage("No matching conditions found for generating 15CA");
								errorList.add(error);
							}
						} else {
							Errors error = new Errors();
							error.setCompleteMessage(nonResidentShareholder.getId() + "");
							error.setId(nonResidentShareholder.getFolioNumber());
							error.setMessage("Form 15 CA/CB Is Not Applicable ");
							errorList.add(error);
						}
					}
				}

				// merging the records based on pan
				Map<String, Form15CAPartCDTO> map = new HashMap<>();
				if (!form15CAPartCDTOS.isEmpty()) {
					form15CAPartCDTOS.stream().forEach(n -> {
						if (map.containsKey(n.getShareholderPan().trim())) {
							Form15CAPartCDTO dto = map.get(n.getShareholderPan().trim());
							n.setAmountPayableInInr(n.getAmountPayableInInr() == null ? BigDecimal.ZERO
									: n.getAmountPayableInInr()
											.add(dto.getAmountPayableInInr() == null ? BigDecimal.ZERO
													: dto.getAmountPayableInInr()));
							n.setIncomeChargeableToTax(n.getIncomeChargeableToTax() == null ? BigDecimal.ZERO
									: n.getIncomeChargeableToTax()
											.add(dto.getIncomeChargeableToTax() == null ? BigDecimal.ZERO
													: dto.getIncomeChargeableToTax()));
							n.setTaxLiability(n.getTaxLiability() == null ? BigDecimal.ZERO
									: n.getTaxLiability().add(
											dto.getTaxLiability() == null ? BigDecimal.ZERO : dto.getTaxLiability())); // getTaxLiabilityPerDTAA
							n.setTaxLiabilityPerDTAA(n.getTaxLiabilityPerDTAA() == null ? BigDecimal.ZERO
									: n.getTaxLiabilityPerDTAA()
											.add(dto.getTaxLiabilityPerDTAA() == null ? BigDecimal.ZERO
													: dto.getTaxLiabilityPerDTAA()));
							n.setTdsAmountInInr(n.getTdsAmountInInr() == null ? BigDecimal.ZERO
									: n.getTdsAmountInInr().add(dto.getTdsAmountInInr() == null ? BigDecimal.ZERO
											: dto.getTdsAmountInInr()));
							Double foreignCurency1 = Double
									.parseDouble(StringUtils.isNotBlank(n.getAmountPayableInForeignCurrency())
											? n.getAmountPayableInForeignCurrency()
											: "0");
							Double foreignCurency2 = Double
									.parseDouble(StringUtils.isNotBlank(dto.getAmountPayableInForeignCurrency())
											? dto.getAmountPayableInForeignCurrency()
											: "0");
							n.setAmountPayableInForeignCurrency(
									BigDecimal.valueOf(foreignCurency1 + foreignCurency2).toString());
							map.put(n.getShareholderPan().trim(), n);
						} else {
							map.put(n.getShareholderPan().trim(), n);
						}
					});
				}
				if (!map.isEmpty()) {
					form15CAPartCDTOS.clear();
					map.entrySet().stream().forEach(n -> form15CAPartCDTOS.add(n.getValue()));
				}

				logger.info("Alll DTO's Are prepared successfully to generate 15 CA {}");
				multipartFile = generateExcelFor15ca(authorisedPersonDTO, form15CAPartADTOS, form15CAPartBDTOS,
						form15CAPartCDTOS, form15CAPartDDTOS, errorList);
				String uri = blobStorage.uploadExcelToBlobWithFile(multipartFile, tenantId);
				updateFilingReportStatus(deductorTan, assessmentYear, postingDate, Form15FileType.CA,
						Form15FileFormat.EXCEL, ReturnType.REGULAR, Form15FilingStatus.GENERATED, uri, "", userName);

			} catch (Exception e) {
				logger.error("Error occured while Generating Form 15CA excel", e);
				updateFilingReportStatus(deductorTan, assessmentYear, postingDate, Form15FileType.CA,
						Form15FileFormat.EXCEL, ReturnType.REGULAR, Form15FilingStatus.ERROR, null, e.getMessage(),
						userName);
			}
		});
	}

	private String check15CACBFlag(InvoiceShareholderNonResident nonResidentShareholder, String tenantId,
			String deductorPan) {
		ResponseEntity<ApiStatus<ShareholderMasterNonResidential>> shareholderNonResident = null;
		if (!Objects.isNull(nonResidentShareholder.getShareholderId())) {
			shareholderNonResident = onboardingClient.getNonResidentialShareholderById(tenantId, deductorPan,
					nonResidentShareholder.getShareholderId());
		} else {
			throw new CustomException("Shareholder not valid", HttpStatus.BAD_REQUEST);
		}
		ShareholderMasterNonResidential data = Objects.requireNonNull(shareholderNonResident.getBody()).getData();
		return Objects.isNull(data.getForm15CACBApplicable()) ? "No" : data.getForm15CACBApplicable();
//        return "Yes";
	}

	private boolean checkOnboardingInformation(String deductorPan, String tenantId) {
		boolean onboardingCondition = false;
		Map<String, String> requestParams = new HashMap<>();
		requestParams.put("pan", deductorPan);
		DeductorOnboardingInformationResponseDTO deductorOnboardingInformation = null;
		ResponseEntity<ApiStatus<Optional<DeductorOnboardingInformationResponseDTO>>> deductorOnboardingInfo = onboardingClient
				.getDeductorOnboardingInfo(requestParams, tenantId);
		Optional<DeductorOnboardingInformationResponseDTO> onboardingInformation = Objects
				.requireNonNull(deductorOnboardingInfo.getBody()).getData();
		if (onboardingInformation.isPresent()) {
			deductorOnboardingInformation = onboardingInformation.get();
			onboardingCondition = deductorOnboardingInformation.getDvndPrepForm15CaCb()
					.equals(DividendOnboardingInfoMetaData.PrepForm15CaCb.ONLY_PART_C_OF_FORM_15_CA);
		}
		return onboardingCondition;
	}

	private boolean checkBooleanForNull(Boolean value) {
		if (Objects.isNull(value)) {
			return false;
		} else {
			return value;
		}
	}

	private File generateExcelFor15ca(AuthorisedPersonDTO authorisedPersonDTO, List<Form15CAPartADTO> form15CAPartADTOS,
			List<Form15CAPartBDTO> form15CAPartBDTOS, List<Form15CAPartCDTO> form15CAPartCDTOS,
			List<Form15CAPartDDTO> form15CAPartDDTOS, List<Errors> errors)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {

		logger.info("Started Preparing the file {}");
		Resource resource = resourceLoader.getResource("classpath:templates/" + "Form15CA.xlsx");
		InputStream input = resource.getInputStream();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

		try (XSSFWorkbook workbook = new XSSFWorkbook(input)) {

			// 15 CA Part A
			XSSFSheet worksheet = workbook.getSheetAt(0);
			int totalRows = 2;
			XSSFRow row = worksheet.createRow(totalRows);
			acceptAuthorisedPersonData(authorisedPersonDTO, row);

			totalRows = 7;
			for (Form15CAPartADTO record : form15CAPartADTOS) {
				row = worksheet.createRow(totalRows);
				acceptForm15CAPartAData(record, row, formatter);
				totalRows++;
			}

			// 15 CA Part B
			XSSFSheet worksheet2 = workbook.getSheetAt(1);

			int totalRowsPartB = 2;
			XSSFRow rowPartB = worksheet2.createRow(totalRowsPartB);
			acceptAuthorisedPersonData(authorisedPersonDTO, rowPartB);

			totalRowsPartB = 7;
			for (Form15CAPartBDTO record : form15CAPartBDTOS) {
				rowPartB = worksheet2.createRow(totalRowsPartB);
				acceptForm15CAPartBData(record, rowPartB, formatter);
				totalRowsPartB++;
			}

			// 15 CA Part C
			XSSFSheet worksheet3 = workbook.getSheetAt(2);

			int totalRowsPartC = 2;
			XSSFRow rowPartC = worksheet3.createRow(totalRowsPartC);
			acceptAuthorisedPersonData(authorisedPersonDTO, rowPartC);

			totalRowsPartC = 7;
			for (Form15CAPartCDTO record : form15CAPartCDTOS) {
				rowPartC = worksheet3.createRow(totalRowsPartC);
				acceptForm15CAPartCData(record, rowPartC, formatter);
				totalRowsPartC++;
			}

			// 15 CA Part D
			XSSFSheet worksheet4 = workbook.getSheetAt(3);

			int totalRowsPartD = 2;
			XSSFRow rowPartD = worksheet4.createRow(totalRowsPartD);
			acceptAuthorisedPersonData(authorisedPersonDTO, rowPartD);

			totalRowsPartD = 7;
			for (Form15CAPartDDTO record : form15CAPartDDTOS) {
				rowPartD = worksheet4.createRow(totalRowsPartC);
				acceptForm15CAPartDData(record, rowPartD, formatter);
				totalRowsPartC++;
			}
			logger.debug("File Writing End");

			File file = new File("Form 15CA" + new Date().getTime() + ".xlsx");
			OutputStream out = new FileOutputStream(file);
			workbook.write(out);
			out.close();

			File zipFile = new File("FORM 15 CA" + System.currentTimeMillis() + ".zip");
			FileOutputStream outputStream = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(outputStream);
			zos.putNextEntry(new ZipEntry(file.getName()));
			byte[] bytes1 = Files.readAllBytes(file.toPath());
			zos.write(bytes1, 0, bytes1.length);
			zos.closeEntry();

			// generating error file
			if (!errors.isEmpty()) {
				File errorFile = dividendFilingCommonErrorFileService.generateErrorFile(errors);
				zos.putNextEntry(new ZipEntry(errorFile.getName()));
				byte[] bytes2 = Files.readAllBytes(errorFile.toPath());
				zos.write(bytes2, 0, bytes2.length);
				zos.closeEntry();
			}

			zos.close();
			outputStream.close();

			return zipFile;
		} catch (IOException ex) {
			logger.info("Exception occured while preparing 15CA file " + ex);
			throw new RuntimeException(ex);
		}
	}

	private void acceptForm15CAPartAData(Form15CAPartADTO record, XSSFRow row, SimpleDateFormat formatter) {
		row.createCell(0).setCellValue(
				StringUtils.isBlank(record.getRemitterName()) ? StringUtils.EMPTY : record.getRemitterName());
		row.createCell(1).setCellValue(
				StringUtils.isBlank(record.getRemitterPan()) ? StringUtils.EMPTY : record.getRemitterPan());
		row.createCell(2).setCellValue(
				StringUtils.isBlank(record.getRemitterTan()) ? StringUtils.EMPTY : record.getRemitterTan());
		row.createCell(3).setCellValue(
				StringUtils.isBlank(record.getRemitterStatus()) ? StringUtils.EMPTY : record.getRemitterStatus());
		row.createCell(4).setCellValue(StringUtils.isBlank(record.getRemitterResidentialStatus()) ? StringUtils.EMPTY
				: record.getRemitterResidentialStatus());
		row.createCell(5).setCellValue(
				StringUtils.isBlank(record.getRemitterEmail()) ? StringUtils.EMPTY : record.getRemitterEmail());
		row.createCell(6).setCellValue(StringUtils.isBlank(record.getRemitterPhoneNumber()) ? StringUtils.EMPTY
				: record.getRemitterPhoneNumber());
		row.createCell(7).setCellValue(StringUtils.isBlank(record.getRemitterFlatDoorBlockNo()) ? StringUtils.EMPTY
				: record.getRemitterFlatDoorBlockNo());
		row.createCell(8).setCellValue(StringUtils.isBlank(record.getRemitterNameBuildingVillage()) ? StringUtils.EMPTY
				: record.getRemitterNameBuildingVillage());
		row.createCell(9).setCellValue(StringUtils.isBlank(record.getRemitterRoadStreetPostoffice()) ? StringUtils.EMPTY
				: record.getRemitterRoadStreetPostoffice());
		row.createCell(10).setCellValue(StringUtils.isBlank(record.getRemitterAreaLocality()) ? StringUtils.EMPTY
				: record.getRemitterAreaLocality());
		row.createCell(11).setCellValue(StringUtils.isBlank(record.getRemitterTownCityDistrict()) ? StringUtils.EMPTY
				: record.getRemitterTownCityDistrict());
		row.createCell(12).setCellValue(
				StringUtils.isBlank(record.getRemitterState()) ? StringUtils.EMPTY : record.getRemitterState());
		row.createCell(13).setCellValue(
				StringUtils.isBlank(record.getRemitterCountry()) ? StringUtils.EMPTY : record.getRemitterCountry());
		row.createCell(14).setCellValue(
				StringUtils.isBlank(record.getRemitterPinCode()) ? StringUtils.EMPTY : record.getRemitterPinCode());
		row.createCell(15).setCellValue(
				StringUtils.isBlank(record.getShareholderName()) ? StringUtils.EMPTY : record.getShareholderName());
		row.createCell(16).setCellValue(
				StringUtils.isBlank(record.getShareholderPan()) ? StringUtils.EMPTY : record.getShareholderPan());
		row.createCell(17).setCellValue(
				StringUtils.isBlank(record.getShareholderEmail()) ? StringUtils.EMPTY : record.getShareholderEmail());
		row.createCell(18).setCellValue(StringUtils.isBlank(record.getShareholderPhoneNumber()) ? StringUtils.EMPTY
				: record.getShareholderPhoneNumber());
		row.createCell(19).setCellValue(
				StringUtils.isBlank(record.getRemittanceCountry()) ? StringUtils.EMPTY : record.getRemittanceCountry());
		row.createCell(20).setCellValue(
				StringUtils.isBlank(record.getFlatDoorBlockNo()) ? StringUtils.EMPTY : record.getFlatDoorBlockNo());
		row.createCell(21).setCellValue(StringUtils.isBlank(record.getNameBuildingVillage()) ? StringUtils.EMPTY
				: record.getNameBuildingVillage());
		row.createCell(22).setCellValue(StringUtils.isBlank(record.getRoadStreetPostoffice()) ? StringUtils.EMPTY
				: record.getRoadStreetPostoffice());
		row.createCell(23).setCellValue(
				StringUtils.isBlank(record.getAreaLocality()) ? StringUtils.EMPTY : record.getAreaLocality());
		row.createCell(24).setCellValue(
				StringUtils.isBlank(record.getTownCityDistrict()) ? StringUtils.EMPTY : record.getTownCityDistrict());
		row.createCell(25).setCellValue(StringUtils.isBlank(record.getState()) ? StringUtils.EMPTY : record.getState());
		row.createCell(26)
				.setCellValue(StringUtils.isBlank(record.getCountry()) ? StringUtils.EMPTY : record.getCountry());
		row.createCell(27)
				.setCellValue(StringUtils.isBlank(record.getPinCode()) ? StringUtils.EMPTY : record.getPinCode());
		row.createCell(28).setCellValue(checkBigdecimal(record.getAmountPayableBeforeTdsInInr()));
		row.createCell(29).setCellValue(checkBigdecimal(record.getAggregateRemittanceAmount()));
		row.createCell(30)
				.setCellValue(StringUtils.isBlank(record.getNameOfBank()) ? StringUtils.EMPTY : record.getNameOfBank());
		row.createCell(31).setCellValue(
				StringUtils.isBlank(record.getBranchOfBank()) ? StringUtils.EMPTY : record.getBranchOfBank());
		row.createCell(32).setCellValue(Objects.isNull(record.getProposedDateOfRemittance()) ? "  "
				: formatter.format(record.getProposedDateOfRemittance()));
		row.createCell(33).setCellValue(StringUtils.isBlank(record.getNatureOfRemittance()) ? StringUtils.EMPTY
				: record.getNatureOfRemittance());
		row.createCell(34).setCellValue(
				StringUtils.isBlank(record.getRbiPurposeCode()) ? StringUtils.EMPTY : record.getRbiPurposeCode());
		row.createCell(35).setCellValue(checkBigdecimal(record.getTdsAmount()));
		row.createCell(36).setCellValue(checkBigdecimal(record.getTdsRate()));
		row.createCell(37).setCellValue(
				Objects.isNull(record.getDeductionDate()) ? "  " : formatter.format(record.getDeductionDate()));
		row.createCell(38).setCellValue(record.getId());
		row.createCell(39).setCellValue(record.getShareHolderId());
	}

	private void acceptForm15CAPartBData(Form15CAPartBDTO record, XSSFRow row, SimpleDateFormat formatter) {
		row.createCell(0).setCellValue(
				StringUtils.isBlank(record.getRemitterName()) ? StringUtils.EMPTY : record.getRemitterName());
		row.createCell(1).setCellValue(
				StringUtils.isBlank(record.getRemitterPan()) ? StringUtils.EMPTY : record.getRemitterPan());
		row.createCell(2).setCellValue(
				StringUtils.isBlank(record.getRemitterTan()) ? StringUtils.EMPTY : record.getRemitterTan());
		row.createCell(3).setCellValue(
				StringUtils.isBlank(record.getRemitterStatus()) ? StringUtils.EMPTY : record.getRemitterStatus());
		row.createCell(4).setCellValue(StringUtils.isBlank(record.getRemitterResidentialStatus()) ? StringUtils.EMPTY
				: record.getRemitterResidentialStatus());
		row.createCell(5).setCellValue(
				StringUtils.isBlank(record.getRemitterEmail()) ? StringUtils.EMPTY : record.getRemitterEmail());
		row.createCell(6).setCellValue(StringUtils.isBlank(record.getRemitterPhoneNumber()) ? StringUtils.EMPTY
				: record.getRemitterPhoneNumber());
		row.createCell(7).setCellValue(StringUtils.isBlank(record.getRemitterFlatDoorBlockNo()) ? StringUtils.EMPTY
				: record.getRemitterFlatDoorBlockNo());
		row.createCell(8).setCellValue(StringUtils.isBlank(record.getRemitterNameBuildingVillage()) ? StringUtils.EMPTY
				: record.getRemitterNameBuildingVillage());
		row.createCell(9).setCellValue(StringUtils.isBlank(record.getRemitterRoadStreetPostoffice()) ? StringUtils.EMPTY
				: record.getRemitterRoadStreetPostoffice());
		row.createCell(10).setCellValue(StringUtils.isBlank(record.getRemitterAreaLocality()) ? StringUtils.EMPTY
				: record.getRemitterAreaLocality());
		row.createCell(11).setCellValue(StringUtils.isBlank(record.getRemitterTownCityDistrict()) ? StringUtils.EMPTY
				: record.getRemitterTownCityDistrict());
		row.createCell(12).setCellValue(
				StringUtils.isBlank(record.getRemitterState()) ? StringUtils.EMPTY : record.getRemitterState());
		row.createCell(13).setCellValue(
				StringUtils.isBlank(record.getRemitterCountry()) ? StringUtils.EMPTY : record.getRemitterCountry());
		row.createCell(14).setCellValue(
				StringUtils.isBlank(record.getRemitterPinCode()) ? StringUtils.EMPTY : record.getRemitterPinCode());
		row.createCell(15).setCellValue(
				StringUtils.isBlank(record.getShareholderName()) ? StringUtils.EMPTY : record.getShareholderName());
		row.createCell(16).setCellValue(
				StringUtils.isBlank(record.getShareholderPan()) ? StringUtils.EMPTY : record.getShareholderPan());
		row.createCell(17).setCellValue(
				StringUtils.isBlank(record.getShareholderEmail()) ? StringUtils.EMPTY : record.getShareholderEmail());
		row.createCell(18)
				.setCellValue(StringUtils.isBlank(record.getShareholderIsdCodePhoneNumber()) ? StringUtils.EMPTY
						: record.getShareholderIsdCodePhoneNumber());
		row.createCell(19).setCellValue(
				StringUtils.isBlank(record.getFlatDoorBlockNo()) ? StringUtils.EMPTY : record.getFlatDoorBlockNo());
		row.createCell(20).setCellValue(StringUtils.isBlank(record.getNameBuildingVillage()) ? StringUtils.EMPTY
				: record.getNameBuildingVillage());
		row.createCell(21).setCellValue(StringUtils.isBlank(record.getRoadStreetPostoffice()) ? StringUtils.EMPTY
				: record.getRoadStreetPostoffice());
		row.createCell(22).setCellValue(
				StringUtils.isBlank(record.getAreaLocality()) ? StringUtils.EMPTY : record.getAreaLocality());
//        row.createCell(23).setCellValue(
//                StringUtils.isBlank(record.getTownCityDistrict()) ? StringUtils.EMPTY : record.getTownCityDistrict());
		row.createCell(23).setCellValue(StringUtils.isBlank(record.getState()) ? StringUtils.EMPTY : record.getState());
		row.createCell(24)
				.setCellValue(StringUtils.isBlank(record.getCountry()) ? StringUtils.EMPTY : record.getCountry());
		row.createCell(25)
				.setCellValue(StringUtils.isBlank(record.getPinCode()) ? StringUtils.EMPTY : record.getPinCode());
		row.createCell(26).setCellValue(StringUtils.isBlank(record.getOrderOrCertificateSection()) ? StringUtils.EMPTY
				: record.getOrderOrCertificateSection());
		row.createCell(27).setCellValue(StringUtils.isBlank(record.getAssessingOfficerName()) ? StringUtils.EMPTY
				: record.getAssessingOfficerName());
		row.createCell(28).setCellValue(StringUtils.isBlank(record.getAssessingOfficerDesignation()) ? StringUtils.EMPTY
				: record.getAssessingOfficerDesignation());
		row.createCell(29).setCellValue(StringUtils.isBlank(record.getOrderOrCertificateDate()) ? StringUtils.EMPTY
				: record.getOrderOrCertificateDate());
		row.createCell(30).setCellValue(StringUtils.isBlank(record.getOrderOrCertificateNumber()) ? StringUtils.EMPTY
				: record.getOrderOrCertificateNumber());
		row.createCell(31).setCellValue(
				StringUtils.isBlank(record.getRemittanceCountry()) ? StringUtils.EMPTY : record.getRemittanceCountry());
		row.createCell(32)
				.setCellValue(StringUtils.isBlank(record.getCurrency()) ? StringUtils.EMPTY : record.getCurrency());
		row.createCell(33)
				.setCellValue(StringUtils.isBlank(record.getAmountPayableInForeignCurrency()) ? StringUtils.EMPTY
						: record.getAmountPayableInForeignCurrency());
		row.createCell(34).setCellValue(checkBigdecimal(record.getAmountPayableInInr()));
		row.createCell(35)
				.setCellValue(StringUtils.isBlank(record.getNameOfBank()) ? StringUtils.EMPTY : record.getNameOfBank());
		row.createCell(36).setCellValue(
				StringUtils.isBlank(record.getBranchOfBank()) ? StringUtils.EMPTY : record.getBranchOfBank());
		row.createCell(37).setCellValue(StringUtils.isBlank(record.getBsrCodeOfBankBranch()) ? StringUtils.EMPTY
				: record.getBsrCodeOfBankBranch());
		row.createCell(38).setCellValue(Objects.isNull(record.getProposedDateOfRemittance()) ? "  "
				: formatter.format(record.getProposedDateOfRemittance()));
		row.createCell(39).setCellValue(StringUtils.isBlank(record.getNatureOfRemittance()) ? StringUtils.EMPTY
				: record.getNatureOfRemittance());
		row.createCell(40).setCellValue(
				StringUtils.isBlank(record.getRbiPurposeCode()) ? StringUtils.EMPTY : record.getRbiPurposeCode());
		row.createCell(41).setCellValue(checkBigdecimal(record.getTdsAmount()));
		row.createCell(42).setCellValue(checkBigdecimal(record.getTdsRate()));
		row.createCell(43).setCellValue(
				Objects.isNull(record.getDeductionDate()) ? "  " : formatter.format(record.getDeductionDate()));
		row.createCell(44).setCellValue(record.getId());
		row.createCell(45).setCellValue(record.getShareHolderId());
	}

	private void acceptForm15CAPartCData(Form15CAPartCDTO record, XSSFRow row, SimpleDateFormat formatter) {
		row.createCell(0).setCellValue(
				StringUtils.isBlank(record.getRemitterName()) ? StringUtils.EMPTY : record.getRemitterName());
		row.createCell(1).setCellValue(
				StringUtils.isBlank(record.getRemitterPan()) ? StringUtils.EMPTY : record.getRemitterPan());
		row.createCell(2).setCellValue(
				StringUtils.isBlank(record.getRemitterTan()) ? StringUtils.EMPTY : record.getRemitterTan());
		row.createCell(7)
				.setCellValue(StringUtils.isBlank(record.getRemitterPrincipalAreaOfBusiness()) ? StringUtils.EMPTY
						: record.getRemitterPrincipalAreaOfBusiness());
		row.createCell(8).setCellValue(
				StringUtils.isBlank(record.getRemitterStatus()) ? StringUtils.EMPTY : record.getRemitterStatus());
		row.createCell(9).setCellValue(StringUtils.isBlank(record.getRemitterResidentialStatus()) ? StringUtils.EMPTY
				: record.getRemitterResidentialStatus());
		row.createCell(10).setCellValue(StringUtils.isBlank(record.getRemitterFlatDoorBlockNo()) ? StringUtils.EMPTY
				: record.getRemitterFlatDoorBlockNo());
		row.createCell(11).setCellValue(StringUtils.isBlank(record.getRemitterNameBuildingVillage()) ? StringUtils.EMPTY
				: record.getRemitterNameBuildingVillage());
		row.createCell(12)
				.setCellValue(StringUtils.isBlank(record.getRemitterRoadStreetPostoffice()) ? StringUtils.EMPTY
						: record.getRemitterRoadStreetPostoffice());
		row.createCell(13).setCellValue(StringUtils.isBlank(record.getRemitterAreaLocality()) ? StringUtils.EMPTY
				: record.getRemitterAreaLocality());
		row.createCell(14).setCellValue(StringUtils.isBlank(record.getRemitterTownCityDistrict()) ? StringUtils.EMPTY
				: record.getRemitterTownCityDistrict());
		row.createCell(15).setCellValue(
				StringUtils.isBlank(record.getRemitterState()) ? StringUtils.EMPTY : record.getRemitterState());
		row.createCell(16).setCellValue(
				StringUtils.isBlank(record.getRemitterCountry()) ? StringUtils.EMPTY : record.getRemitterCountry());
		row.createCell(17).setCellValue(
				StringUtils.isBlank(record.getRemitterPinCode()) ? StringUtils.EMPTY : record.getRemitterPinCode());
		row.createCell(18).setCellValue(
				StringUtils.isBlank(record.getRemitterEmail()) ? StringUtils.EMPTY : record.getRemitterEmail());
		row.createCell(19).setCellValue(StringUtils.isBlank(record.getRemitterPhoneNumber()) ? StringUtils.EMPTY
				: record.getRemitterPhoneNumber());
		row.createCell(20).setCellValue(
				StringUtils.isBlank(record.getShareholderName()) ? StringUtils.EMPTY : record.getShareholderName());
		row.createCell(21).setCellValue(
				StringUtils.isBlank(record.getShareholderPan()) ? StringUtils.EMPTY : record.getShareholderPan());
		row.createCell(22).setCellValue(
				StringUtils.isBlank(record.getShareholderStatus()) ? StringUtils.EMPTY : record.getShareholderStatus());
		row.createCell(23).setCellValue(
				StringUtils.isBlank(record.getRemittanceCountry()) ? StringUtils.EMPTY : record.getRemittanceCountry());
		row.createCell(24)
				.setCellValue(StringUtils.isBlank(record.getShareholderPrincipalPlaceOfBusiness()) ? StringUtils.EMPTY
						: record.getShareholderPrincipalPlaceOfBusiness());
		row.createCell(25).setCellValue(
				StringUtils.isBlank(record.getShareholderEmail()) ? StringUtils.EMPTY : record.getShareholderEmail());
		row.createCell(26)
				.setCellValue(StringUtils.isBlank(record.getShareholderIsdCodePhoneNumber()) ? StringUtils.EMPTY
						: record.getShareholderIsdCodePhoneNumber());
		row.createCell(27).setCellValue(
				StringUtils.isBlank(record.getFlatDoorBlockNo()) ? StringUtils.EMPTY : record.getFlatDoorBlockNo());
		row.createCell(28).setCellValue(StringUtils.isBlank(record.getNameBuildingVillage()) ? StringUtils.EMPTY
				: record.getNameBuildingVillage());
		row.createCell(29).setCellValue(StringUtils.isBlank(record.getRoadStreetPostoffice()) ? StringUtils.EMPTY
				: record.getRoadStreetPostoffice());
		row.createCell(30).setCellValue(
				StringUtils.isBlank(record.getAreaLocality()) ? StringUtils.EMPTY : record.getAreaLocality());
		row.createCell(31).setCellValue(
				StringUtils.isBlank(record.getTownCityDistrict()) ? StringUtils.EMPTY : record.getTownCityDistrict());
		row.createCell(32).setCellValue(StringUtils.isBlank(record.getState()) ? StringUtils.EMPTY : record.getState());
		row.createCell(33)
				.setCellValue(StringUtils.isBlank(record.getCountry()) ? StringUtils.EMPTY : record.getCountry());
		row.createCell(34)
				.setCellValue(StringUtils.isBlank(record.getPinCode()) ? StringUtils.EMPTY : record.getPinCode());
		row.createCell(35).setCellValue(
				StringUtils.isBlank(record.getAccountantName()) ? StringUtils.EMPTY : record.getAccountantName());
		row.createCell(36).setCellValue(StringUtils.isBlank(record.getCaNameOfProprietorship()) ? StringUtils.EMPTY
				: record.getCaNameOfProprietorship());
		row.createCell(37).setCellValue(StringUtils.isBlank(record.getAccountantFlatDoorBlockNo()) ? StringUtils.EMPTY
				: record.getAccountantFlatDoorBlockNo());
		row.createCell(38)
				.setCellValue(StringUtils.isBlank(record.getAccountantNameBuildingVillage()) ? StringUtils.EMPTY
						: record.getAccountantNameBuildingVillage());
		row.createCell(39)
				.setCellValue(StringUtils.isBlank(record.getAccountantRoadStreetPostoffice()) ? StringUtils.EMPTY
						: record.getAccountantRoadStreetPostoffice());
		row.createCell(40).setCellValue(StringUtils.isBlank(record.getAccountantAreaLocality()) ? StringUtils.EMPTY
				: record.getAccountantAreaLocality());
		row.createCell(41).setCellValue(StringUtils.isBlank(record.getAccountantTownCityDistrict()) ? StringUtils.EMPTY
				: record.getAccountantTownCityDistrict());
		row.createCell(42).setCellValue(
				StringUtils.isBlank(record.getAccountantState()) ? StringUtils.EMPTY : record.getAccountantState());
		row.createCell(43).setCellValue(
				StringUtils.isBlank(record.getAccountantCountry()) ? StringUtils.EMPTY : record.getAccountantCountry());
		row.createCell(44).setCellValue(
				StringUtils.isBlank(record.getAccountantPinCode()) ? StringUtils.EMPTY : record.getAccountantPinCode());
		row.createCell(45).setCellValue(StringUtils.isBlank(record.getCaRegistrationNumber()) ? StringUtils.EMPTY
				: record.getCaRegistrationNumber());
		row.createCell(46).setCellValue(
				StringUtils.isBlank(record.getCertificateNumber()) ? StringUtils.EMPTY : record.getCertificateNumber());
		row.createCell(47).setCellValue(Objects.isNull(record.getCertificateNumberDate()) ? "  "
				: formatter.format(record.getCertificateNumberDate()));
		row.createCell(48).setCellValue(checkBoolean(record.getOrderOrCertificateObtainedFromAO()));
		row.createCell(49).setCellValue(StringUtils.isBlank(record.getOrderOrCertificateSection()) ? StringUtils.EMPTY
				: record.getOrderOrCertificateSection());
		row.createCell(50).setCellValue(StringUtils.isBlank(record.getAssessingOfficerName()) ? StringUtils.EMPTY
				: record.getAssessingOfficerName());
		row.createCell(51).setCellValue(StringUtils.isBlank(record.getAssessingOfficerDesignation()) ? StringUtils.EMPTY
				: record.getAssessingOfficerDesignation());
		row.createCell(52).setCellValue(StringUtils.isBlank(record.getOrderOrCertificateDate()) ? StringUtils.EMPTY
				: record.getOrderOrCertificateDate());
		row.createCell(53).setCellValue(StringUtils.isBlank(record.getOrderOrCertificateNumber()) ? StringUtils.EMPTY
				: record.getOrderOrCertificateNumber());
		row.createCell(54).setCellValue(
				StringUtils.isBlank(record.getRemittanceCountry()) ? StringUtils.EMPTY : record.getRemittanceCountry());
		row.createCell(55)
				.setCellValue(StringUtils.isBlank(record.getCurrency()) ? StringUtils.EMPTY : record.getCurrency());
		row.createCell(56)
				.setCellValue((StringUtils.isBlank(record.getAmountPayableInForeignCurrency())
						|| record.getAmountPayableInForeignCurrency().equals("0.0")) ? "0"
								: record.getAmountPayableInForeignCurrency());
		row.createCell(57).setCellValue(checkBigdecimal(record.getAmountPayableInInr()));
		row.createCell(58)
				.setCellValue(StringUtils.isBlank(record.getNameOfBank()) ? StringUtils.EMPTY : record.getNameOfBank());
		row.createCell(59).setCellValue(
				StringUtils.isBlank(record.getBranchOfBank()) ? StringUtils.EMPTY : record.getBranchOfBank());
		row.createCell(60).setCellValue(StringUtils.isBlank(record.getBsrCodeOfBankBranch()) ? StringUtils.EMPTY
				: record.getBsrCodeOfBankBranch());
		row.createCell(61).setCellValue(Objects.isNull(record.getProposedDateOfRemittance()) ? "  "
				: formatter.format(record.getProposedDateOfRemittance()));
		row.createCell(62).setCellValue(StringUtils.isBlank(record.getNatureOfRemittance()) ? StringUtils.EMPTY
				: record.getNatureOfRemittance());
		row.createCell(63).setCellValue(
				StringUtils.isBlank(record.getRbiPurposeCode()) ? StringUtils.EMPTY : record.getRbiPurposeCode());
		row.createCell(64).setCellValue(checkBoolean(record.getTaxPayableGrossedUp()));
		row.createCell(65).setCellValue(StringUtils.isBlank(record.getRemittanceSectionOfAct()) ? StringUtils.EMPTY
				: record.getRemittanceSectionOfAct());
		row.createCell(66).setCellValue(checkBigdecimal(record.getIncomeChargeableToTax()));
		row.createCell(67).setCellValue(checkBigdecimal(record.getTheTaxableLiability()));
		row.createCell(68).setCellValue("AS PER INCOME-TAX ACT");
		row.createCell(69).setCellValue(checkBoolean(record.getRemittanceRecipientTRCAvailable()));
		row.createCell(70).setCellValue(
				StringUtils.isBlank(record.getRelevantDTAA()) ? StringUtils.EMPTY : record.getRelevantDTAA());
		row.createCell(71).setCellValue(StringUtils.isBlank(record.getRelevantDTAAArticle()) ? StringUtils.EMPTY
				: record.getRelevantDTAAArticle());
		row.createCell(72).setCellValue(checkBigdecimal(record.getTaxableIncomeAsPerDATAA()));// record.getTaxableIncomeAsPerDATAA()
		if (checkBoolean(record.getRemittanceForRoyalties()).equals("Yes")) {
			row.createCell(73).setCellValue(checkBigdecimal(record.getTaxLiabilityPerDTAA()));
		} else {
			row.createCell(73).setCellValue(" ");
		}
		row.createCell(74).setCellValue(checkBoolean(record.getRemittanceForRoyalties()));
		row.createCell(75).setCellValue(
				StringUtils.isBlank(record.getdTAAArticle()) ? StringUtils.EMPTY : record.getdTAAArticle());
		row.createCell(76).setCellValue(checkBigdecimal(record.getTdsRatePerDTAA()));
		row.createCell(77).setCellValue("No");
		row.createCell(79).setCellValue("No");
		row.createCell(84).setCellValue("No");
		row.createCell(88).setCellValue("NA");
		row.createCell(89).setCellValue(StringUtils.isBlank(record.getTdsAmountInForeignCurrency()) ? StringUtils.EMPTY
				: record.getTdsAmountInForeignCurrency());
		row.createCell(90).setCellValue(checkBigdecimal(record.getTdsAmountInInr()));
		row.createCell(91)
				.setCellValue(StringUtils.isNotBlank(record.getBasisOfIncomeAndTaxLiability())
						? record.getBasisOfIncomeAndTaxLiability()
						: "");
		row.createCell(92).setCellValue(checkBigdecimal(record.getTdsRate()));
		row.createCell(93)
				.setCellValue(StringUtils.isBlank(record.getActualRemittanceAmountAfterTdsInForeignCurrency())
						? StringUtils.EMPTY
						: record.getActualRemittanceAmountAfterTdsInForeignCurrency());
		row.createCell(94).setCellValue(Objects.isNull(record.getTaxAtSourceDeductionDate()) ? "  "
				: formatter.format(record.getTaxAtSourceDeductionDate()));
		row.createCell(95).setCellValue(record.getId());
		row.createCell(96).setCellValue(record.getShareHolderId());
	}

	private void acceptForm15CAPartDData(Form15CAPartDDTO record, XSSFRow row, SimpleDateFormat formatter) {
		row.createCell(0).setCellValue(
				StringUtils.isBlank(record.getRemitterName()) ? StringUtils.EMPTY : record.getRemitterName());
		row.createCell(1).setCellValue(
				StringUtils.isBlank(record.getRemitterPan()) ? StringUtils.EMPTY : record.getRemitterPan());
		row.createCell(2).setCellValue(
				StringUtils.isBlank(record.getRemitterTan()) ? StringUtils.EMPTY : record.getRemitterTan());
		row.createCell(3).setCellValue(
				StringUtils.isBlank(record.getRemitterStatus()) ? StringUtils.EMPTY : record.getRemitterStatus());
		row.createCell(4).setCellValue(StringUtils.isBlank(record.getRemitterResidentialStatus()) ? StringUtils.EMPTY
				: record.getRemitterResidentialStatus());
		row.createCell(5).setCellValue(
				StringUtils.isBlank(record.getRemitterEmail()) ? StringUtils.EMPTY : record.getRemitterEmail());
		row.createCell(6).setCellValue(StringUtils.isBlank(record.getRemitterPhoneNumber()) ? StringUtils.EMPTY
				: record.getRemitterPhoneNumber());
		row.createCell(7).setCellValue(StringUtils.isBlank(record.getRemitterFlatDoorBlockNo()) ? StringUtils.EMPTY
				: record.getRemitterFlatDoorBlockNo());
		row.createCell(8).setCellValue(StringUtils.isBlank(record.getRemitterNameBuildingVillage()) ? StringUtils.EMPTY
				: record.getRemitterNameBuildingVillage());
		row.createCell(9).setCellValue(StringUtils.isBlank(record.getRemitterRoadStreetPostoffice()) ? StringUtils.EMPTY
				: record.getRemitterRoadStreetPostoffice());
		row.createCell(10).setCellValue(StringUtils.isBlank(record.getRemitterAreaLocality()) ? StringUtils.EMPTY
				: record.getRemitterAreaLocality());
		row.createCell(11).setCellValue(StringUtils.isBlank(record.getRemitterTownCityDistrict()) ? StringUtils.EMPTY
				: record.getRemitterTownCityDistrict());
		row.createCell(12).setCellValue(
				StringUtils.isBlank(record.getRemitterState()) ? StringUtils.EMPTY : record.getRemitterState());
		row.createCell(13).setCellValue(
				StringUtils.isBlank(record.getRemitterCountry()) ? StringUtils.EMPTY : record.getRemitterCountry());
		row.createCell(14).setCellValue(
				StringUtils.isBlank(record.getRemitterPinCode()) ? StringUtils.EMPTY : record.getRemitterPinCode());
		row.createCell(15).setCellValue(
				StringUtils.isBlank(record.getShareholderName()) ? StringUtils.EMPTY : record.getShareholderName());
		row.createCell(16).setCellValue(
				StringUtils.isBlank(record.getShareholderPan()) ? StringUtils.EMPTY : record.getShareholderPan());
		row.createCell(17).setCellValue(
				StringUtils.isBlank(record.getShareholderEmail()) ? StringUtils.EMPTY : record.getShareholderEmail());
		row.createCell(18).setCellValue(StringUtils.isBlank(record.getShareholderPhoneNumber()) ? StringUtils.EMPTY
				: record.getShareholderPhoneNumber());
		row.createCell(19).setCellValue(
				StringUtils.isBlank(record.getFlatDoorBlockNo()) ? StringUtils.EMPTY : record.getFlatDoorBlockNo());

		row.createCell(20).setCellValue(StringUtils.isBlank(record.getNameBuildingVillage()) ? StringUtils.EMPTY
				: record.getNameBuildingVillage());
		row.createCell(21).setCellValue(StringUtils.isBlank(record.getRoadStreetPostoffice()) ? StringUtils.EMPTY
				: record.getRoadStreetPostoffice());
		row.createCell(22).setCellValue(
				StringUtils.isBlank(record.getAreaLocality()) ? StringUtils.EMPTY : record.getAreaLocality());
		row.createCell(23).setCellValue(
				StringUtils.isBlank(record.getTownCityDistrict()) ? StringUtils.EMPTY : record.getTownCityDistrict());
		row.createCell(24).setCellValue(StringUtils.isBlank(record.getState()) ? StringUtils.EMPTY : record.getState());
		row.createCell(25)
				.setCellValue(StringUtils.isBlank(record.getCountry()) ? StringUtils.EMPTY : record.getCountry());
		row.createCell(26)
				.setCellValue(StringUtils.isBlank(record.getPinCode()) ? StringUtils.EMPTY : record.getPinCode());
		row.createCell(27).setCellValue(
				StringUtils.isBlank(record.getRemittanceCountry()) ? StringUtils.EMPTY : record.getRemittanceCountry());

		row.createCell(28).setCellValue("");
		row.createCell(29)
				.setCellValue(StringUtils.isBlank(record.getCurrency()) ? StringUtils.EMPTY : record.getCurrency());
		row.createCell(30).setCellValue("");
		row.createCell(21)
				.setCellValue(StringUtils.isBlank(record.getCountry()) ? StringUtils.EMPTY : record.getCountry());
		row.createCell(32)
				.setCellValue(StringUtils.isBlank(record.getAmountPayableInForeignCurrency()) ? StringUtils.EMPTY
						: record.getAmountPayableInForeignCurrency());
		row.createCell(33)
				.setCellValue(record.getAmountPayableInInr() == null ? "0" : record.getAmountPayableInInr().toString());
		row.createCell(34)
				.setCellValue(StringUtils.isBlank(record.getNameOfBank()) ? StringUtils.EMPTY : record.getNameOfBank());
		row.createCell(35).setCellValue("");
		row.createCell(36).setCellValue(
				StringUtils.isBlank(record.getBranchOfBank()) ? StringUtils.EMPTY : record.getBranchOfBank());
		row.createCell(37).setCellValue(StringUtils.isBlank(record.getBsrCodeOfBankBranch()) ? StringUtils.EMPTY
				: record.getBsrCodeOfBankBranch());
		row.createCell(38).setCellValue(record.getProposedDateOfRemittance() == null ? StringUtils.EMPTY
				: record.getProposedDateOfRemittance().toString());
		row.createCell(39).setCellValue(StringUtils.isBlank(record.getNatureOfRemittance()) ? StringUtils.EMPTY
				: record.getNatureOfRemittance());
		row.createCell(40).setCellValue(
				StringUtils.isBlank(record.getPruposedRBICode()) ? StringUtils.EMPTY : record.getPruposedRBICode());

	}

	private void acceptAuthorisedPersonData(AuthorisedPersonDTO authorisedPersonDTO, XSSFRow row) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		row.createCell(0)
				.setCellValue(StringUtils.isBlank(authorisedPersonDTO.getAuthorisedPersonName()) ? StringUtils.EMPTY
						: authorisedPersonDTO.getAuthorisedPersonName());
		row.createCell(1)
				.setCellValue(StringUtils.isBlank(authorisedPersonDTO.getFatherOrHusbandName()) ? StringUtils.EMPTY
						: authorisedPersonDTO.getFatherOrHusbandName());
		row.createCell(2).setCellValue(StringUtils.isBlank(authorisedPersonDTO.getDesignation()) ? StringUtils.EMPTY
				: authorisedPersonDTO.getDesignation());
		row.createCell(3).setCellValue(Objects.isNull(authorisedPersonDTO.getDateOfFiling()) ? "  "
				: formatter.format(authorisedPersonDTO.getDateOfFiling()));
		row.createCell(4).setCellValue(StringUtils.isBlank(authorisedPersonDTO.getPlace()) ? StringUtils.EMPTY
				: authorisedPersonDTO.getPlace());
	}

	public AuthorisedPersonDTO generateAuthorisedPersonDto(String tanNumber, String deductorPan, String tenantId,
			List<Errors> errorList) {
		logger.info("Generating autherised person dto for 15 CA {}");
		ResponseEntity<ApiStatus<DeductorTanAddress>> deductorTanAddress = onboardingClient
				.getDeductorTanAddressByPanTan(tanNumber, deductorPan, tenantId);
		DeductorTanAddress data = Objects.requireNonNull(deductorTanAddress.getBody()).getData();
		if (data != null) {
			AuthorisedPersonDTO authorisedPersonDTO = new AuthorisedPersonDTO();
			String errorMsg = "";
			if (StringUtils.isNotBlank(data.getPersonName())) {
				authorisedPersonDTO.setAuthorisedPersonName(data.getPersonName());
			} else {
				errorMsg = errorMsg + "Name of Authorised Peron Is Mandatory To Generate Form 15 CA" + "\n";
			}
			if (StringUtils.isNotBlank(data.getDvndFatherOrHusbandName())) {
				authorisedPersonDTO.setFatherOrHusbandName(data.getDvndFatherOrHusbandName());
			} else {
				errorMsg = errorMsg + "Father's/ Husband Name Is Mandatory To Generate Form 15 CA" + "\n";
			}
			if (StringUtils.isNotBlank(data.getPersonDesignation())) {
				authorisedPersonDTO.setDesignation(data.getPersonDesignation());
			} else {
				errorMsg = errorMsg + "Designation Is Mandatory To Generate Form 15 CA" + "\n";
			}
			authorisedPersonDTO.setDateOfFiling(new Date());
			if (StringUtils.isNotBlank(data.getDvndTownCityDistrict())) {
				authorisedPersonDTO.setPlace(data.getDvndTownCityDistrict());
			} else {
				errorMsg = errorMsg + "Place Is Mandatory To Generate Form 15 CA" + "\n";
			}
			if (StringUtils.isNotBlank(errorMsg)) {
				Stream.of(errorMsg.split("\n")).forEach(n -> {
					Errors error = new Errors();
					error.setMessage(n);
					errorList.add(error);
				});
			}
			return authorisedPersonDTO;
		} else {
			throw new CustomException("No deductor found for given pan", HttpStatus.BAD_REQUEST);
		}
	}

	public Form15CAPartCDTO generate15CAPartCDto(InvoiceShareholderNonResident nonResidentShareholder, String tanNumber,
			String deductorPan, String tenantId, List<Errors> errorList) {

		boolean isLdcApplied = false;
		boolean isTreatyApplied = false;
		boolean isActRateApplied = false;
		if (!Objects.isNull(nonResidentShareholder.getWithholdingDetails())) {
			NonResidentWithholdingDetails withholdingDetails = nonResidentShareholder.getWithholdingDetails();
			isLdcApplied = checkBooleanForNull(withholdingDetails.isLdcApplied());
			isTreatyApplied = checkBooleanForNull(withholdingDetails.isTreatyApplied());
			isActRateApplied = checkBooleanForNull(withholdingDetails.isActApplied());
		}
		String errorMsg = "";
		Form15CAPartCDTO form15CAPartCDTO = new Form15CAPartCDTO();
		form15CAPartCDTO.setId(nonResidentShareholder.getId());
		form15CAPartCDTO.setFolioNo(nonResidentShareholder.getFolioNumber());

		// RemitterDetls & RemitterAddrs
		populateDeductorMasterData(form15CAPartCDTO, deductorPan, tenantId);

		errorMsg = errorMsg + populateDeductorTanAddress(form15CAPartCDTO, deductorPan, tanNumber, tenantId);
		form15CAPartCDTO.setRemitterResidentialStatus(checkResidentialStatus(form15CAPartCDTO.getRemitterStatus(),
				form15CAPartCDTO.getRemitterCountry(), form15CAPartCDTO.getRemitterResidentialStatus()));

		// RemitteeDetls & RemitteeAddrs
		errorMsg = errorMsg + populateShareholderMasterDate(form15CAPartCDTO, deductorPan,
				nonResidentShareholder.getShareholderId(), tenantId);
		form15CAPartCDTO.setShareholderName(!Objects.isNull(nonResidentShareholder.getShareholderName())
				? nonResidentShareholder.getShareholderName().toUpperCase()
				: "");
		form15CAPartCDTO.setShareholderPan(!Objects.isNull(nonResidentShareholder.getShareholderPan())
				? nonResidentShareholder.getShareholderPan().toUpperCase()
				: "");
		if (StringUtils.isNotBlank(nonResidentShareholder.getShareholderType())) {
			form15CAPartCDTO.setShareholderStatus(checkRemitteeStatus(nonResidentShareholder.getShareholderType()));
		} else {
			errorMsg = errorMsg + "Shareolder Type Is Mandatory To Generate Form 15 CA Part C" + "\n";
		}
		if (StringUtils.isNotBlank(nonResidentShareholder.getPrincipalPlaceOfBusiness())) {
			form15CAPartCDTO.setShareholderPrincipalPlaceOfBusiness(
					nonResidentShareholder.getPrincipalPlaceOfBusiness().toUpperCase());
		} else {
			errorMsg = errorMsg + "Principal place of business Is Mandatory To Generate Form 15 CA Part C" + "\n";
		}

		// RemittanceDetls
		// in populateDeductorTanAddress
		if (nonResidentShareholder.getProposedDateOfRemmitence() != null) {
			form15CAPartCDTO.setProposedDateOfRemittance(nonResidentShareholder.getProposedDateOfRemmitence());
		} else {
			form15CAPartCDTO.setProposedDateOfRemittance(new Date());
		}
		form15CAPartCDTO.setRemittanceCountry(nonResidentShareholder.getRemittanceMadeCountry());
		Country country = populateMasterData(form15CAPartCDTO, nonResidentShareholder.getRemittanceMadeCountry(),
				nonResidentShareholder.getCountry());
		form15CAPartCDTO.setAmountPayableInForeignCurrency(nonResidentShareholder.getDividendAmountForeignCurrency());
		form15CAPartCDTO.setAmountPayableInInr(nonResidentShareholder.getDividendAmountRs());
		form15CAPartCDTO.setTaxPayableGrossedUp(nonResidentShareholder.getTaxPayableGrossedUp());
		form15CAPartCDTO.setRbiPurposeCode(checkCategory(nonResidentShareholder.getShareholderCategory()));

		BigDecimal calculatedAct = nonResidentShareholder.getWithholdingDetails().getActSummary().getWithholding()
				.add(nonResidentShareholder.getCessWithholding()).add(nonResidentShareholder.getSurchargeWithholding());
		// AcctntDetls in populateDeductorTanAddress
		// AoOrderDetls
		if (nonResidentShareholder.getWithholdingDetails().isLdcApplied()) {
			calculatedAct = calculatedAct
					.add(nonResidentShareholder.getWithholdingDetails().getLdcSummary().getWithholding());
			form15CAPartCDTO.setOrderOrCertificateSection(
					nonResidentShareholder.getWithholdingDetails().getLdcSummary().getSection());
			form15CAPartCDTO.setAssessingOfficerName(
					!Objects.isNull(nonResidentShareholder.getWithholdingDetails().getLdcSummary().getAssigneeOfficer())
							? nonResidentShareholder.getWithholdingDetails().getLdcSummary().getAssigneeOfficer()
									.toUpperCase()
							: "");
			form15CAPartCDTO.setOrderOrCertificateDate(
					nonResidentShareholder.getWithholdingDetails().getLdcSummary().getApplicableFrom());
			form15CAPartCDTO.setOrderOrCertificateNumber(
					nonResidentShareholder.getWithholdingDetails().getLdcSummary().getCertificateNumber());
		}

		form15CAPartCDTO.setTheTaxableLiability(calculatedAct.setScale(2, RoundingMode.UP));
		form15CAPartCDTO.setTaxableIncomeAsPerDATAA(nonResidentShareholder.getDividendAmountRs());
		// ItActDetails
		form15CAPartCDTO.setIncomeChargeableToTax(nonResidentShareholder.getDividendAmountRs());
		if (nonResidentShareholder.getWithholdingDetails().isActApplied())
			form15CAPartCDTO
					.setTaxLiability(nonResidentShareholder.getWithholdingDetails().getActSummary().getWithholding());

		/*
		 * if (nonResidentShareholder.getWithholdingDetails().isActApplied() ||
		 * nonResidentShareholder.getWithholdingDetails().isLdcApplied()) {
		 * form15CAPartCDTO.setBasisOfIncomeAndTaxLiability("AS PER INCOME-TAX ACT"); }
		 * else { form15CAPartCDTO.setBasisOfIncomeAndTaxLiability("AS PER DTAA"); } if
		 * (nonResidentShareholder.getWithholdingDetails().getTreatySummary() != null) {
		 * BigDecimal ldcWithholding =
		 * !Objects.isNull(nonResidentShareholder.getWithholdingDetails().getLdcSummary(
		 * )) ?
		 * nonResidentShareholder.getWithholdingDetails().getLdcSummary().getWithholding
		 * () : new BigDecimal(0); BigDecimal actLdcWithholding =
		 * BigDecimal.valueOf(nonResidentShareholder.getWithholdingDetails()
		 * .getActSummary().getWithholding().add(ldcWithholding).doubleValue()).setScale
		 * (2, RoundingMode.UP); BigDecimal treatyWithholding = BigDecimal
		 * .valueOf(nonResidentShareholder.getWithholdingDetails().getTreatySummary().
		 * getWithholding() != null ?
		 * nonResidentShareholder.getWithholdingDetails().getTreatySummary().
		 * getWithholding() .doubleValue() : 0) .add(ldcWithholding).setScale(2,
		 * RoundingMode.UP); if
		 * (actLdcWithholding.compareTo(nonResidentShareholder.getTotalWithholding()) ==
		 * 0 &&
		 * treatyWithholding.compareTo(nonResidentShareholder.getTotalWithholding()) ==
		 * 0) { form15CAPartCDTO.setBasisOfIncomeAndTaxLiability("AS PER DTAA"); } }
		 */

		// DTAA Details
		form15CAPartCDTO.setRemittanceRecipientTRCAvailable(nonResidentShareholder.getIsTrcAvailable());

		if (nonResidentShareholder.getWithholdingDetails().isTreatyApplied()) {
			form15CAPartCDTO.setRelevantDTAA(!Objects.isNull(nonResidentShareholder.getCountry())
					? "India-" + nonResidentShareholder.getCountry() + " DTAA"
					: "");
			form15CAPartCDTO.setRelevantDTAAArticle(
					"ARTICLE " + nonResidentShareholder.getWithholdingDetails().getTreatySummary().getTaxTreatyClause()
							+ " OF DTAA WITH "
							+ (!Objects.isNull(nonResidentShareholder.getCountry())
									? nonResidentShareholder.getCountry().toUpperCase()
									: ""));
			/*
			 * form15CAPartCDTO.setRelevantDTAA(!Objects.isNull(nonResidentShareholder.
			 * getCountry()) ? nonResidentShareholder.getCountry().toUpperCase() : "");
			 */
			form15CAPartCDTO.setAmountPayableInInr(nonResidentShareholder.getDividendAmountRs());
			form15CAPartCDTO.setTaxLiabilityPerDTAA(
					nonResidentShareholder.getWithholdingDetails().getTreatySummary().getWithholding());
			form15CAPartCDTO.setdTAAArticle(
					nonResidentShareholder.getWithholdingDetails().getTreatySummary().getTaxTreatyClause());
			form15CAPartCDTO.setRemittanceForRoyalties(true);

			// For Excel Only
			if (checkBooleanForNull(nonResidentShareholder.getWithholdingDetails().isTreatyApplied())) {
				form15CAPartCDTO
						.setRatePerDTAA(nonResidentShareholder.getWithholdingDetails().getTreatySummary().getRate());
			} else {
				if (!Objects.isNull(nonResidentShareholder.getWithholdingDetails().getActSummary())) {
					form15CAPartCDTO.setRatePerDTAA(
							nonResidentShareholder.getWithholdingDetails().getActSummary().getActRate());
				}
			}
			form15CAPartCDTO
					.setTdsRatePerDTAA(nonResidentShareholder.getWithholdingDetails().getTreatySummary().getRate());
		} else {
			// find treaty records
			if (!Objects.isNull(country)) {
				ResponseEntity<ApiStatus<DividendRateTreaty>> treatyBenefit = mastersClient.getTreatyBenefit(tenantId,
						country);
				DividendRateTreaty rateTreaty = treatyBenefit.getBody().getData();
				if (!Objects.isNull(rateTreaty)) {
					form15CAPartCDTO.setTdsRatePerDTAA(rateTreaty.getMfnNotAvailedCompanyTaxRate());
					form15CAPartCDTO.setdTAAArticle("ARTICLE " + rateTreaty.getTaxTreatyClause());
				} else {
					form15CAPartCDTO.setTdsRatePerDTAA(new BigDecimal(0));
				}
			} else {
				form15CAPartCDTO.setTdsRatePerDTAA(new BigDecimal(0));
			}
		}

		// TDSDetails
		form15CAPartCDTO.setTdsAmountInForeignCurrency(nonResidentShareholder.getDividendAmountForeignCurrency());

		// check if client overridden withholding is not null
		if (!Objects.isNull(nonResidentShareholder.getClientOverriddenWithholding())
				&& nonResidentShareholder.getClientOverriddenWithholding().compareTo(BigDecimal.ZERO) > 0) {
			form15CAPartCDTO.setTdsAmountInInr(nonResidentShareholder.getClientOverriddenWithholding());
		} else {
			form15CAPartCDTO.setTdsAmountInInr(nonResidentShareholder.getFinalDividendWithholding());
		}

		// check if client overridden rate is not null
		if (!Objects.isNull(nonResidentShareholder.getClientOverriddenRate())
				&& nonResidentShareholder.getClientOverriddenRate().compareTo(BigDecimal.ZERO) > 0) {
			form15CAPartCDTO.setTdsRate(nonResidentShareholder.getClientOverriddenRate());
		} else {
			form15CAPartCDTO.setTdsRate(nonResidentShareholder.getTdsRate());
		}
		form15CAPartCDTO.setAmountPayableInForeignCurrency(nonResidentShareholder.getDividendAmountForeignCurrency());

		// Declaration in populateDeductorTanAddress

		/*
		 * if
		 * (StringUtils.isNotBlank(nonResidentShareholder.getForm15CBAcknowledgementNo()
		 * )) {
		 * form15CAPartCDTO.setForm15CBAcknowledgementNumber(nonResidentShareholder.
		 * getForm15CBAcknowledgementNo()); } else { errorMsg = errorMsg +
		 * "Acknowledgment number of Form 15CB Is Mandatory To Generate Form 15 CA Part C"
		 * + "\n"; }
		 */
		NonResidentWithholdingDetails withholdingDetails = nonResidentShareholder.getWithholdingDetails();
		if (!Objects.isNull(nonResidentShareholder.getWithholdingDetails())) {
			form15CAPartCDTO.setOrderOrCertificateObtainedFromAO(withholdingDetails.isLdcApplied());
		}

		form15CAPartCDTO.setNatureOfRemittance("Dividend Income");
		form15CAPartCDTO.setRemittanceSectionOfAct(nonResidentShareholder.getTdsSection());
		form15CAPartCDTO.setTaxableIncomePerDTAA(nonResidentShareholder.getDividendAmountRs());

		if (form15CAPartCDTO.getRemittanceForRoyalties() == null
				|| form15CAPartCDTO.getRemittanceForRoyalties() == false) {
			form15CAPartCDTO.setdTAAArticle("");
			form15CAPartCDTO.setTdsRatePerDTAA(null);
		}

		/*
		 * if
		 * (StringUtils.isNotBlank(nonResidentShareholder.getForm15CBAcknowledgementNo()
		 * )) { form15CAPartCDTO.setCertificateNumber(nonResidentShareholder.
		 * getForm15CBAcknowledgementNo()); } else { errorMsg = errorMsg +
		 * "Certificate Number (Form 15CB) of Form 15CB Is Mandatory To Generate Form 15 CA Part C"
		 * + "\n"; } if (nonResidentShareholder.getForm15CBGenartionDate() != null) {
		 * form15CAPartCDTO.setCertificateNumberDate(nonResidentShareholder.
		 * getForm15CBGenartionDate()); } else { errorMsg = errorMsg +
		 * "Date of certificate number (Form 15CB) of Form 15CB Is Mandatory To Generate Form 15 CA Part C"
		 * + "\n"; }
		 */

		/**
		 * making the below changes as per palak instruction, this is just a patch of
		 * code to achieve requirement need to design this method properly
		 */
		form15CAPartCDTO.setRemittanceSectionOfAct("115A"); // nonResidentShareholder.getTdsSection()

		form15CAPartCDTO.setCurrency("INR");
		form15CAPartCDTO.setActualRemittanceAmountAfterTdsInForeignCurrency(
				nonResidentShareholder.getActualRemmitanceAmountForeignCurrency());
		form15CAPartCDTO.setTaxAtSourceDeductionDate(nonResidentShareholder.getDateOfPosting());
		if (nonResidentShareholder.getWithholdingDetails().isTreatyApplied() == false) {
			form15CAPartCDTO.setTaxableIncomeAsPerDATAA(null);
			form15CAPartCDTO.setTaxLiabilityPerDTAA(null);
		}
		if (isActRateApplied) {
			form15CAPartCDTO.setBasisOfIncomeAndTaxLiability("AS PER INCOME-TAX ACT");
		} else {
			form15CAPartCDTO.setBasisOfIncomeAndTaxLiability("AS PER DTAA");
		}
		if (isActRateApplied && isTreatyApplied) {
			TreatySummary treatySummary = nonResidentShareholder.getWithholdingDetails().getTreatySummary();
			ActSummary actSummary = nonResidentShareholder.getWithholdingDetails().getActSummary();
			if (!Objects.isNull(treatySummary.getRate()) && !Objects.isNull(actSummary.getActRate())) {
				if (treatySummary.getRate().compareTo(actSummary.getActRate()) > 0) {
					form15CAPartCDTO.setBasisOfIncomeAndTaxLiability("AS PER INCOME-TAX ACT");
				} else {
					form15CAPartCDTO.setBasisOfIncomeAndTaxLiability("AS PER DTAA");
				}
			}
		}
		if (form15CAPartCDTO.getBasisOfIncomeAndTaxLiability().equals("AS PER DTAA")) {
			if (StringUtils.isNotBlank(nonResidentShareholder.getTreatyActSummery())) {
				BigDecimal taxLiaility = new JSONObject(nonResidentShareholder.getTreatyActSummery())
						.getJSONObject("Summary").getBigDecimal("total_witholding");
				form15CAPartCDTO.setTheTaxableLiability(taxLiaility);
			}
		}
		if (StringUtils.isBlank(errorMsg)) {
			return form15CAPartCDTO;
		} else {
			Stream.of(errorMsg.split("\n")).forEach(n -> {
				Errors error = new Errors();
				error.setCompleteMessage(nonResidentShareholder.getId() + "");
				error.setId(nonResidentShareholder.getFolioNumber());
				error.setMessage(n);
				errorList.add(error);
			});
			return null;
		}

	}

	private String checkRemitteeStatus(String shareholderType) {
		if (shareholderType.equalsIgnoreCase("Company")) {
			return "Company";
		}
		if (shareholderType.equalsIgnoreCase("Person (individual)")) {
			return "Individual";
		}
		if (shareholderType.equalsIgnoreCase("Firm/ LLP")) {
			return "Firm";
		}
		return "Others";
	}

	public Form15CAPartBDTO generate15CAPartBDto(InvoiceShareholderNonResident nonResidentShareholder, String tanNumber,
			String deductorPan, String tenantId, List<Errors> errorList) {

		String errorMsg = "";
		Form15CAPartBDTO form15CAPartBDTO = new Form15CAPartBDTO();
		form15CAPartBDTO.setId(nonResidentShareholder.getId());
		form15CAPartBDTO.setFolioNo(nonResidentShareholder.getFolioNumber());
		populateDeductorMasterData(form15CAPartBDTO, deductorPan, tenantId);
		errorMsg = errorMsg + populateDeductorTanAddress(form15CAPartBDTO, deductorPan, tanNumber, tenantId);
		errorMsg = errorMsg + populateShareholderMasterDate(form15CAPartBDTO, deductorPan,
				nonResidentShareholder.getShareholderId(), tenantId);
		populateMasterData(form15CAPartBDTO, nonResidentShareholder.getRemittanceMadeCountry());

		form15CAPartBDTO.setRemitterResidentialStatus(checkResidentialStatus(form15CAPartBDTO.getRemitterStatus(),
				form15CAPartBDTO.getRemitterCountry(), form15CAPartBDTO.getRemitterResidentialStatus()));
		form15CAPartBDTO.setShareholderName(!Objects.isNull(nonResidentShareholder.getShareholderName())
				? nonResidentShareholder.getShareholderName().toUpperCase()
				: "");
		if (StringUtils.isNotBlank(nonResidentShareholder.getShareholderPan())
				&& !nonResidentShareholder.getShareholderPan().equals("None")) {
			form15CAPartBDTO.setShareholderPan(nonResidentShareholder.getShareholderPan().toUpperCase());
		}
		if (StringUtils.isNotBlank(nonResidentShareholder.getRemittanceMadeCountry())) {
			form15CAPartBDTO.setRemittanceCountry(nonResidentShareholder.getRemittanceMadeCountry());
		} else {
			errorMsg = errorMsg + "Country To Which Remmitance Is Made Is Mandatory To Generate Form 15 CA Part B"
					+ "\n";
		}
		if (nonResidentShareholder.getDividendAmountForeignCurrency() != null) {
			form15CAPartBDTO
					.setAmountPayableInForeignCurrency(nonResidentShareholder.getDividendAmountForeignCurrency());
		} else {
			errorMsg = errorMsg + "Amount of Dividend (Foreign Currency) Mandatory To Generate Form 15 CA Part B"
					+ "\n";
		}
		form15CAPartBDTO.setAmountPayableInInr(nonResidentShareholder.getDividendAmountRs());
		form15CAPartBDTO.setNatureOfRemittance("Dividend Income");
		form15CAPartBDTO.setRbiPurposeCode(checkCategory(nonResidentShareholder.getShareholderCategory()));

		// check if client overridden rate is not null
		if (!Objects.isNull(nonResidentShareholder.getClientOverriddenRate())
				&& nonResidentShareholder.getClientOverriddenRate().compareTo(BigDecimal.ZERO) > 0) {
			form15CAPartBDTO.setTdsRate(nonResidentShareholder.getClientOverriddenRate());
		} else {
			form15CAPartBDTO.setTdsRate(nonResidentShareholder.getTdsRate());
		}

		// check if client overridden withholding is not null
		if (!Objects.isNull(nonResidentShareholder.getClientOverriddenWithholding())
				&& nonResidentShareholder.getClientOverriddenWithholding().compareTo(BigDecimal.ZERO) > 0) {
			form15CAPartBDTO.setTdsAmount(nonResidentShareholder.getClientOverriddenWithholding());
		} else {
			form15CAPartBDTO.setTdsAmount(nonResidentShareholder.getFinalDividendWithholding());
		}
		if (nonResidentShareholder.getProposedDateOfRemmitence() != null) {
			form15CAPartBDTO.setProposedDateOfRemittance(nonResidentShareholder.getProposedDateOfRemmitence());
		} else {
			form15CAPartBDTO.setProposedDateOfRemittance(new Date());
		}
		if (!Objects.isNull(nonResidentShareholder.getWithholdingDetails())) {
			if (!Objects.isNull(nonResidentShareholder.getWithholdingDetails().getLdcSummary())) {
				form15CAPartBDTO.setOrderOrCertificateSection(
						nonResidentShareholder.getWithholdingDetails().getLdcSummary().getSection());
				form15CAPartBDTO.setAssessingOfficerName(!Objects
						.isNull(nonResidentShareholder.getWithholdingDetails().getLdcSummary().getAssigneeOfficer())
								? nonResidentShareholder.getWithholdingDetails().getLdcSummary().getAssigneeOfficer()
										.toUpperCase()
								: "");
				form15CAPartBDTO.setOrderOrCertificateDate(
						nonResidentShareholder.getWithholdingDetails().getLdcSummary().getApplicableFrom());
				form15CAPartBDTO.setOrderOrCertificateNumber(!Objects
						.isNull(nonResidentShareholder.getWithholdingDetails().getLdcSummary().getCertificateNumber())
								? nonResidentShareholder.getWithholdingDetails().getLdcSummary().getCertificateNumber()
										.toUpperCase()
								: "");
			}
		}
		form15CAPartBDTO.setDeductionDate(nonResidentShareholder.getDateOfPosting());
		form15CAPartBDTO.setCurrency("INR");
		if (StringUtils.isBlank(errorMsg)) {
			return form15CAPartBDTO;
		} else {
			Stream.of(errorMsg.split("\n")).forEach(n -> {
				Errors error = new Errors();
				error.setCompleteMessage(nonResidentShareholder.getId() + "");
				error.setId(nonResidentShareholder.getFolioNumber());
				error.setMessage(n);
				errorList.add(error);
			});
			return null;
		}
	}

	public Form15CAPartADTO generate15CAPartADtos(InvoiceShareholderNonResident nonResidentShareholder,
			String tanNumber, String deductorPan, String tenantId, List<Errors> errorList) {

		String errorMsg = "";
		Form15CAPartADTO form15CAPartADTO = new Form15CAPartADTO();
		form15CAPartADTO.setId(nonResidentShareholder.getId());
		form15CAPartADTO.setFolioNo(nonResidentShareholder.getFolioNumber());
		populateDeductorMasterData(form15CAPartADTO, deductorPan, tenantId);
		errorMsg = errorMsg + populateDeductorTanAddress(form15CAPartADTO, deductorPan, tanNumber, tenantId);
		errorMsg = errorMsg + populateShareholderMasterDate(form15CAPartADTO, deductorPan,
				nonResidentShareholder.getShareholderId(), tenantId);
		form15CAPartADTO.setRemitterResidentialStatus(checkResidentialStatus(form15CAPartADTO.getRemitterStatus(),
				form15CAPartADTO.getRemitterCountry(), form15CAPartADTO.getRemitterResidentialStatus()));

		form15CAPartADTO.setShareholderName(!Objects.isNull(nonResidentShareholder.getShareholderName())
				? nonResidentShareholder.getShareholderName().toUpperCase()
				: "");
		if (StringUtils.isNotBlank(nonResidentShareholder.getShareholderPan())
				&& !nonResidentShareholder.getShareholderPan().equals("None")) {
			form15CAPartADTO.setShareholderPan(nonResidentShareholder.getShareholderPan().toUpperCase());
		}

		if (StringUtils.isNotBlank(nonResidentShareholder.getRemittanceMadeCountry())) {
			form15CAPartADTO.setRemittanceCountry(nonResidentShareholder.getRemittanceMadeCountry());
		} else {
			errorMsg = errorMsg + "Country To Which Remmitance Is Made Is Mandatory To Generate Form 15 CA Part A"
					+ "\n";
		}

		form15CAPartADTO.setAmountPayableBeforeTdsInInr(nonResidentShareholder.getDividendAmountRs());
		if (nonResidentShareholder.getAggreateRemittanceAmountFY() != null) {
			form15CAPartADTO.setAggregateRemittanceAmount(nonResidentShareholder.getAggreateRemittanceAmountFY());
		} else {
			errorMsg = errorMsg
					+ "Aggregate amount of remittance made during the financial year including this proposed remittance Is Mandatory To Generate Form 15 CA Part A"
					+ "\n";
		}
		// form15CAPartADTO.setProposedDateOfRemittance(nonResidentShareholder.getProposedDateOfRemmitence());
		if (nonResidentShareholder.getProposedDateOfRemmitence() != null) {
			form15CAPartADTO.setProposedDateOfRemittance(nonResidentShareholder.getProposedDateOfRemmitence());
		} else {
			form15CAPartADTO.setProposedDateOfRemittance(new Date());
		}
		form15CAPartADTO.setNatureOfRemittance("Dividend Income");
		form15CAPartADTO.setRbiPurposeCode(checkCategory(nonResidentShareholder.getShareholderCategory()));

		// check if client overridden rate is not null
		if (!Objects.isNull(nonResidentShareholder.getClientOverriddenRate())
				&& nonResidentShareholder.getClientOverriddenRate().compareTo(BigDecimal.ZERO) > 0) {
			form15CAPartADTO.setTdsRate(nonResidentShareholder.getClientOverriddenRate());
		} else {
			form15CAPartADTO.setTdsRate(nonResidentShareholder.getTdsRate());
		}
		// check if client overridden withholding is not null
		if (!Objects.isNull(nonResidentShareholder.getClientOverriddenWithholding())
				&& nonResidentShareholder.getClientOverriddenWithholding().compareTo(BigDecimal.ZERO) > 0) {
			form15CAPartADTO.setTdsAmount(nonResidentShareholder.getClientOverriddenWithholding());
		} else {
			form15CAPartADTO.setTdsAmount(nonResidentShareholder.getFinalDividendWithholding());
		}
		form15CAPartADTO.setDeductionDate(nonResidentShareholder.getDateOfPosting());// ----challan

		if (StringUtils.isBlank(errorMsg)) {
			return form15CAPartADTO;
		} else {
			Stream.of(errorMsg.split("\n")).forEach(n -> {
				Errors error = new Errors();
				error.setCompleteMessage(nonResidentShareholder.getId() + "");
				error.setId(nonResidentShareholder.getFolioNumber());
				error.setMessage(n);
				errorList.add(error);
			});
			return null;
		}
	}

	private String checkResidentialStatus(String remitterStatus, String remitterCountry,
			String remitterResidentialStatus) {
		if (remitterStatus.equalsIgnoreCase("COMPANY")) {
			if (remitterCountry.equalsIgnoreCase("INDIA")) {
				return "Domestic Company";
			} else {
				return "Foreign Company";
			}
		} else {
			if (remitterResidentialStatus.equalsIgnoreCase("RES")) {
				return "Resident";
			} else {
				return "Non-Resident";
			}
		}
	}

	private String populateShareholderMasterDate(Form15CAPartADTO form15CAPartADTO, String deductorPan,
			Integer shareholderId, String tenantId) {
		String errorMsg = "";
		if (!Objects.isNull(shareholderId)) {
			ResponseEntity<ApiStatus<ShareholderMasterDTO>> shareholderNonResident = onboardingClient
					.getNonResidentialShareholder(deductorPan, tenantId, shareholderId);
			ShareholderMasterDTO data = Objects.requireNonNull(shareholderNonResident.getBody()).getData();
			form15CAPartADTO.setShareholderPhoneNumber(data.getContact());
			form15CAPartADTO.setShareHolderId(shareholderId);

			if (data.getAddress() != null) {
				if (StringUtils.isNotBlank(data.getAddress().getFlatDoorBlockNo())) {
					form15CAPartADTO.setFlatDoorBlockNo(data.getAddress().getFlatDoorBlockNo());
				} else {
					errorMsg = errorMsg
							+ "Share Holder Flat / Door / Building Is Mandatory to Generate FORM 15 CA Part A" + "\n";
				}
				if (StringUtils.isNotBlank(data.getAddress().getAreaLocality())) {
					form15CAPartADTO.setAreaLocality(data.getAddress().getAreaLocality());
				} else {
					errorMsg = errorMsg + "Share Holder Area / Locality Is Mandatoryto Generate FORM 15 CA Part A"
							+ "\n";
				}
				if (StringUtils.isNotBlank(data.getAddress().getTownCityDistrict())) {
					form15CAPartADTO.setTownCityDistrict(data.getAddress().getTownCityDistrict());
				} else {
					errorMsg = errorMsg
							+ "Share Holder Town / City / District Is Mandatoryto Generate FORM 15 CA Part A" + "\n";
				}

				if (StringUtils.isNotBlank(data.getAddress().getPinCode())) {
					form15CAPartADTO.setPinCode(data.getAddress().getPinCode());
				} else {
					errorMsg = errorMsg + "ZIP Code Is Mandatoryto Generate FORM 15 CA Part A" + "\n";
				}
				if (StringUtils.isNotBlank(data.getEmailId()) && data.getEmailId().equals("None")) {
					form15CAPartADTO.setShareholderEmail(data.getEmailId());
				}
				if (StringUtils.isNotBlank(data.getAddress().getCountry())) {
					form15CAPartADTO.setCountry(data.getAddress().getCountry());
				} else {
					errorMsg = errorMsg + "Shareholder Country Is Mandatoryto Generate FORM 15 CA Part B" + "\n";
				}
				if (StringUtils.isNotBlank(data.getAddress().getStateName())) {
					form15CAPartADTO.setState(data.getAddress().getStateName());
				} else {
					errorMsg = errorMsg + "Shareholder State Is Mandatoryto Generate FORM 15 CA Part B" + "\n";
				}
				form15CAPartADTO.setFlatDoorBlockNo(data.getAddress().getFlatDoorBlockNo());
				form15CAPartADTO.setNameBuildingVillage(data.getAddress().getNameBuildingVillage());
				form15CAPartADTO.setRoadStreetPostoffice(data.getAddress().getRoadStreetPostoffice());
				form15CAPartADTO.setAreaLocality(data.getAddress().getAreaLocality());
				form15CAPartADTO.setTownCityDistrict(data.getAddress().getTownCityDistrict());
				form15CAPartADTO.setPinCode(data.getAddress().getPinCode());
			} else {
				errorMsg = errorMsg + "Addres Is Required To Generate Form 15 CB Part A" + "\n";
			}
		}
		return errorMsg;
	}

	private String populateShareholderMasterData(Form15CAPartDDTO form15CAPartDDTO, String deductorPan,
			Integer shareholderId, String tenantId) {
		String errorMsg = "";
		if (!Objects.isNull(shareholderId)) {
			ResponseEntity<ApiStatus<ShareholderMasterDTO>> shareholderNonResident = onboardingClient
					.getNonResidentialShareholder(deductorPan, tenantId, shareholderId);
			ShareholderMasterDTO data = Objects.requireNonNull(shareholderNonResident.getBody()).getData();
			form15CAPartDDTO.setShareholderPhoneNumber(data.getContact());
			form15CAPartDDTO.setShareHolderId(shareholderId);

			if (data.getAddress() != null) {
				if (StringUtils.isNotBlank(data.getAddress().getFlatDoorBlockNo())) {
					form15CAPartDDTO.setFlatDoorBlockNo(data.getAddress().getFlatDoorBlockNo());
				} else {
					errorMsg = errorMsg
							+ "Share Holder Flat / Door / Building Is Mandatory to Generate FORM 15 CA Part A" + "\n";
				}
				if (StringUtils.isNotBlank(data.getAddress().getAreaLocality())) {
					form15CAPartDDTO.setAreaLocality(data.getAddress().getAreaLocality());
				} else {
					errorMsg = errorMsg + "Share Holder Area / Locality Is Mandatoryto Generate FORM 15 CA Part A"
							+ "\n";
				}
				if (StringUtils.isNotBlank(data.getAddress().getTownCityDistrict())) {
					form15CAPartDDTO.setTownCityDistrict(data.getAddress().getTownCityDistrict());
				} else {
					errorMsg = errorMsg
							+ "Share Holder Town / City / District Is Mandatoryto Generate FORM 15 CA Part A" + "\n";
				}

				if (StringUtils.isNotBlank(data.getAddress().getPinCode())) {
					form15CAPartDDTO.setPinCode(data.getAddress().getPinCode());
				} else {
					errorMsg = errorMsg + "ZIP Code Is Mandatoryto Generate FORM 15 CA Part A" + "\n";
				}
				if (StringUtils.isNotBlank(data.getEmailId()) && data.getEmailId().equals("None")) {
					form15CAPartDDTO.setShareholderEmail(data.getEmailId());
				}
				if (StringUtils.isNotBlank(data.getAddress().getCountry())) {
					form15CAPartDDTO.setCountry(data.getAddress().getCountry());
				} else {
					errorMsg = errorMsg + "Shareholder Country Is Mandatoryto Generate FORM 15 CA Part B" + "\n";
				}
				if (StringUtils.isNotBlank(data.getAddress().getStateName())) {
					form15CAPartDDTO.setState(data.getAddress().getStateName());
				} else {
					errorMsg = errorMsg + "Shareholder State Is Mandatoryto Generate FORM 15 CA Part B" + "\n";
				}
				form15CAPartDDTO.setFlatDoorBlockNo(data.getAddress().getFlatDoorBlockNo());
				form15CAPartDDTO.setNameBuildingVillage(data.getAddress().getNameBuildingVillage());
				form15CAPartDDTO.setRoadStreetPostoffice(data.getAddress().getRoadStreetPostoffice());
				form15CAPartDDTO.setAreaLocality(data.getAddress().getAreaLocality());
				form15CAPartDDTO.setTownCityDistrict(data.getAddress().getTownCityDistrict());
				form15CAPartDDTO.setPinCode(data.getAddress().getPinCode());
			} else {
				errorMsg = errorMsg + "Addres Is Required To Generate Form 15 CB Part A" + "\n";
			}
		}
		return errorMsg;
	}

	private String populateDeductorTanAddress(Form15CAPartADTO form15CAPartADTO, String deductorPan, String tanNumber,
			String tenantId) {
		ResponseEntity<ApiStatus<DeductorTanAddress>> deductorTanAddress = onboardingClient
				.getDeductorTanAddressByPanTan(tanNumber, deductorPan, tenantId);
		DeductorTanAddress data = Objects.requireNonNull(deductorTanAddress.getBody()).getData();
		String errorMsg = "";
		form15CAPartADTO.setRemitterTan(tanNumber);
		form15CAPartADTO.setRemitterFlatDoorBlockNo(data.getFlatDoorBlockNo());
		form15CAPartADTO.setRemitterNameBuildingVillage(data.getNameBuildingVillage());
		form15CAPartADTO.setRemitterRoadStreetPostoffice(data.getRoadStreetPostoffice());
		form15CAPartADTO.setRemitterAreaLocality(data.getAreaLocality());
		form15CAPartADTO.setRemitterTownCityDistrict(data.getTownCityDistrict());
		form15CAPartADTO.setRemitterState(data.getStateName());
		form15CAPartADTO.setRemitterCountry(data.getCountryName());
		form15CAPartADTO.setRemitterPinCode(data.getPinCode());
		if (StringUtils.isNotBlank(data.getDvndNameOfBank())) {
			form15CAPartADTO.setNameOfBank(data.getDvndNameOfBank().toUpperCase());
		} else {
			errorMsg = errorMsg + "Name Of The Bank is Mandatory.It Can Not Be Blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndBranchOfBank())) {
			form15CAPartADTO.setBranchOfBank(data.getDvndBranchOfBank().toUpperCase());
		} else {
			errorMsg = errorMsg + "Name of the branch of the bank is Mandatory.It Can Not Be Blank" + "\n";
		}
		return errorMsg;
	}

	private String populateDeductorTanAddress(Form15CAPartDDTO form15CAPartDDTO, String deductorPan, String tanNumber,
			String tenantId) {
		ResponseEntity<ApiStatus<DeductorTanAddress>> deductorTanAddress = onboardingClient
				.getDeductorTanAddressByPanTan(tanNumber, deductorPan, tenantId);
		DeductorTanAddress data = Objects.requireNonNull(deductorTanAddress.getBody()).getData();
		String errorMsg = "";
		form15CAPartDDTO.setRemitterTan(tanNumber);
		form15CAPartDDTO.setRemitterFlatDoorBlockNo(data.getFlatDoorBlockNo());
		form15CAPartDDTO.setRemitterNameBuildingVillage(data.getNameBuildingVillage());
		form15CAPartDDTO.setRemitterRoadStreetPostoffice(data.getRoadStreetPostoffice());
		form15CAPartDDTO.setRemitterAreaLocality(data.getAreaLocality());
		form15CAPartDDTO.setRemitterTownCityDistrict(data.getTownCityDistrict());
		form15CAPartDDTO.setRemitterState(data.getStateName());
		form15CAPartDDTO.setRemitterCountry(data.getCountryName());
		form15CAPartDDTO.setRemitterPinCode(data.getPinCode());
		if (StringUtils.isNotBlank(data.getDvndNameOfBank())) {
			form15CAPartDDTO.setNameOfBank(data.getDvndNameOfBank().toUpperCase());
		} else {
			errorMsg = errorMsg + "Name Of The Bank is Mandatory.It Can Not Be Blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndBranchOfBank())) {
			form15CAPartDDTO.setBranchOfBank(data.getDvndBranchOfBank().toUpperCase());
		} else {
			errorMsg = errorMsg + "Name of the branch of the bank is Mandatory.It Can Not Be Blank" + "\n";
		}
		return errorMsg;
	}

	private String populateDeductorTanAddress(Form15CAPartCDTO form15CAPartCDTO, String deductorPan, String tanNumber,
			String tenantId) {
		ResponseEntity<ApiStatus<DeductorTanAddress>> deductorTanAddress = onboardingClient
				.getDeductorTanAddressByPanTan(tanNumber, deductorPan, tenantId);
		DeductorTanAddress data = Objects.requireNonNull(deductorTanAddress.getBody()).getData();
		String errorMsg = "";
		form15CAPartCDTO.setRemitterTan(tanNumber);
		form15CAPartCDTO.setRemitterFlatDoorBlockNo(data.getFlatDoorBlockNo());
		form15CAPartCDTO.setRemitterNameBuildingVillage(data.getNameBuildingVillage());
		form15CAPartCDTO.setRemitterRoadStreetPostoffice(data.getRoadStreetPostoffice());
		form15CAPartCDTO.setRemitterAreaLocality(data.getAreaLocality());
		form15CAPartCDTO.setRemitterTownCityDistrict(data.getTownCityDistrict());
		form15CAPartCDTO.setRemitterState(data.getStateName());
		form15CAPartCDTO.setRemitterCountry(data.getCountryName());
		form15CAPartCDTO.setRemitterPinCode(data.getPinCode());
		if (StringUtils.isNotBlank(data.getDvndNameOfBank())) {
			form15CAPartCDTO.setNameOfBank(data.getDvndNameOfBank().toUpperCase());
		} else {
			errorMsg = errorMsg + "Name of Bank is a Mandatory field. It should not be blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndBranchOfBank())) {
			form15CAPartCDTO.setBranchOfBank(data.getDvndBranchOfBank().toUpperCase());
		} else {
			errorMsg = errorMsg + "Branch of the bank is a Mandatory field. It should not be blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndBsrCodeOfBankBranch())) {
			form15CAPartCDTO.setBsrCodeOfBankBranch(data.getDvndBsrCodeOfBankBranch());
		} else {
			errorMsg = errorMsg + "BSR code of the bank branch (7 digit) is a Mandatory field. It should not be blank"
					+ "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndAccountantName())) {
			form15CAPartCDTO.setAccountantName(data.getDvndAccountantName().toUpperCase());
		} else {
			errorMsg = errorMsg + "Accountant Name is a Mandatory field. It should not be blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndNameOfProprietorship())) {
			form15CAPartCDTO.setCaNameOfProprietorship(data.getDvndNameOfProprietorship().toUpperCase());
		} else {
			errorMsg = errorMsg + "Name of proprietorship/firm is a Mandatory field. It should not be blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndMembershipNumber())) {
			form15CAPartCDTO.setCaMembershipNumber(data.getDvndMembershipNumber());
		} else {
			errorMsg = errorMsg + "Membership Number is Mandatory . It should not be blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndRegistrationNumber())) {
			form15CAPartCDTO.setCaRegistrationNumber(data.getDvndRegistrationNumber());
		} else {
			errorMsg = errorMsg + "Registration Number is Mandatory . It should not be blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getAreaLocality())) {
			form15CAPartCDTO.setAccountantAreaLocality(data.getAreaLocality());
		} else {
			errorMsg = errorMsg + "CA Area / Locality is Mandatory . It should not be blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getFlatDoorBlockNo())) {
			form15CAPartCDTO.setAccountantFlatDoorBlockNo(data.getFlatDoorBlockNo());
		} else {
			errorMsg = errorMsg + "CA Flat / Door / Block No is Mandatory . It should not be blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getStateName())) {
			form15CAPartCDTO.setAccountantState(data.getStateName());
		} else {
			errorMsg = errorMsg + "CA State is Mandatory . It should not be blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getPinCode())) {
			form15CAPartCDTO.setAccountantPinCode(data.getPinCode());
		} else {
			errorMsg = errorMsg + "CA PIN Code is Mandatory . It should not be blank" + "\n";
		}
		form15CAPartCDTO.setRemitterPrincipalAreaOfBusiness(data.getDvndPrincipalAreaOfBusiness());
		form15CAPartCDTO.setAccountantNameBuildingVillage(data.getNameBuildingVillage());
		form15CAPartCDTO.setAccountantRoadStreetPostoffice(data.getRoadStreetPostoffice());
		form15CAPartCDTO.setAccountantTownCityDistrict(data.getTownCityDistrict());
		form15CAPartCDTO.setAccountantCountry(data.getCountryName());
		return errorMsg;
	}

	private void populateDeductorMasterData(Form15CAPartADTO form15CAPartADTO, String deductorPan, String tenantId) {
		ResponseEntity<ApiStatus<DeductorMasterDTO>> response = onboardingClient.getDeductorByPan(tenantId,
				deductorPan);
		DeductorMasterDTO deductorMasterDTO = Objects.requireNonNull(response.getBody()).getData();
		if (deductorMasterDTO != null) {
			form15CAPartADTO.setRemitterName(!Objects.isNull(deductorMasterDTO.getDeductorName())
					? deductorMasterDTO.getDeductorName().toUpperCase()
					: "");
			form15CAPartADTO.setRemitterPan(
					!Objects.isNull(deductorMasterDTO.getPan()) ? deductorMasterDTO.getPan().toUpperCase() : "");
			form15CAPartADTO.setRemitterStatus(deductorMasterDTO.getStatus());
			if (deductorMasterDTO.getStatus().equals("Company")) {
				form15CAPartADTO.setRemitterResidentialStatus("Domestic Company");
			} else {
				form15CAPartADTO.setRemitterResidentialStatus(deductorMasterDTO.getResidentialStatus());
			}
			form15CAPartADTO.setRemitterEmail(deductorMasterDTO.getEmail());
			form15CAPartADTO.setRemitterPhoneNumber(deductorMasterDTO.getPhoneNumber());
		} else {
			throw new CustomException("deductor not found for given pan number", HttpStatus.BAD_REQUEST);
		}
	}

	private void populateDeductorMasterData(Form15CAPartDDTO form15CAPartDDTO, String deductorPan, String tenantId) {
		ResponseEntity<ApiStatus<DeductorMasterDTO>> response = onboardingClient.getDeductorByPan(tenantId,
				deductorPan);
		DeductorMasterDTO deductorMasterDTO = Objects.requireNonNull(response.getBody()).getData();
		if (deductorMasterDTO != null) {
			form15CAPartDDTO.setRemitterName(!Objects.isNull(deductorMasterDTO.getDeductorName())
					? deductorMasterDTO.getDeductorName().toUpperCase()
					: "");
			form15CAPartDDTO.setRemitterPan(
					!Objects.isNull(deductorMasterDTO.getPan()) ? deductorMasterDTO.getPan().toUpperCase() : "");
			form15CAPartDDTO.setRemitterStatus(deductorMasterDTO.getStatus());
			if (deductorMasterDTO.getStatus().equals("Company")) {
				form15CAPartDDTO.setRemitterResidentialStatus("Domestic Company");
			} else {
				form15CAPartDDTO.setRemitterResidentialStatus(deductorMasterDTO.getResidentialStatus());
			}
			form15CAPartDDTO.setRemitterEmail(deductorMasterDTO.getEmail());
			form15CAPartDDTO.setRemitterPhoneNumber(deductorMasterDTO.getPhoneNumber());
			form15CAPartDDTO.setRemitterStatus(deductorMasterDTO.getStatus());
		} else {
			throw new CustomException("deductor not found for given pan number", HttpStatus.BAD_REQUEST);
		}
	}

	private String setRemitterResidentialStatus(String residentialStatus) {
		if (residentialStatus.equals("RES")) {
			return "Resident";
		}
		if (residentialStatus.equals("NR")) {
			return "Non-Resident";
		}
		return "";
	}

	private void populateDeductorMasterData(Form15CAPartCDTO form15CAPartCDTO, String deductorPan, String tenantId) {
		ResponseEntity<ApiStatus<DeductorMasterDTO>> response = onboardingClient.getDeductorByPan(tenantId,
				deductorPan);
		DeductorMasterDTO deductorMasterDTO = Objects.requireNonNull(response.getBody()).getData();
		if (deductorMasterDTO != null) {
			form15CAPartCDTO.setRemitterName(!Objects.isNull(deductorMasterDTO.getDeductorName())
					? deductorMasterDTO.getDeductorName().toUpperCase()
					: "");
			form15CAPartCDTO.setRemitterPan(
					!Objects.isNull(deductorMasterDTO.getPan()) ? deductorMasterDTO.getPan().toUpperCase() : "");
			form15CAPartCDTO.setRemitterStatus(deductorMasterDTO.getStatus());
			if (deductorMasterDTO.getStatus().equals("Company")) {
				form15CAPartCDTO.setRemitterResidentialStatus("Domestic Company");
			} else {
				form15CAPartCDTO.setRemitterResidentialStatus(deductorMasterDTO.getResidentialStatus());
			}
			form15CAPartCDTO.setRemitterEmail(deductorMasterDTO.getEmail());
			form15CAPartCDTO.setRemitterPhoneNumber(deductorMasterDTO.getPhoneNumber());
		} else {
			throw new CustomException("deductor not found for given pan number", HttpStatus.BAD_REQUEST);
		}
	}

	private String checkCategory(String shareholderCategory) {
		if (shareholderCategory.equalsIgnoreCase("Foreign Institutional Investors")
				|| shareholderCategory.equalsIgnoreCase("Foreign Portfolio Investors")) {
			return "S1412";
		} else {
			return "S1409";
		}
	}

	private void populateDeductorMasterData(Form15CAPartBDTO form15CAPartBDTO, String deductorPan, String tenantId) {
		ResponseEntity<ApiStatus<DeductorMasterDTO>> response = onboardingClient.getDeductorByPan(tenantId,
				deductorPan);
		DeductorMasterDTO deductorMasterDTO = Objects.requireNonNull(response.getBody()).getData();
		if (deductorMasterDTO != null) {
			form15CAPartBDTO.setRemitterName(!Objects.isNull(deductorMasterDTO.getDeductorName())
					? deductorMasterDTO.getDeductorName().toUpperCase()
					: "");
			form15CAPartBDTO.setRemitterPan(
					!Objects.isNull(deductorMasterDTO.getPan()) ? deductorMasterDTO.getPan().toUpperCase() : "");
			form15CAPartBDTO.setRemitterStatus(deductorMasterDTO.getStatus());
			if (deductorMasterDTO.getStatus().equals("Company")) {
				form15CAPartBDTO.setRemitterResidentialStatus("Domestic Company");
			} else {
				form15CAPartBDTO.setRemitterResidentialStatus(deductorMasterDTO.getResidentialStatus());
			}
			form15CAPartBDTO.setRemitterEmail(deductorMasterDTO.getEmail());
			form15CAPartBDTO.setRemitterPhoneNumber(deductorMasterDTO.getPhoneNumber());
		} else {
			throw new CustomException("deductor not found for given pan number", HttpStatus.BAD_REQUEST);
		}
	}

	private String populateDeductorTanAddress(Form15CAPartBDTO form15CAPartBDTO, String deductorPan, String tanNumber,
			String tenantId) {
		ResponseEntity<ApiStatus<DeductorTanAddress>> deductorTanAddress = onboardingClient
				.getDeductorTanAddressByPanTan(tanNumber, deductorPan, tenantId);
		DeductorTanAddress data = Objects.requireNonNull(deductorTanAddress.getBody()).getData();
		String errorMsg = "";
		form15CAPartBDTO.setRemitterTan(tanNumber);
		form15CAPartBDTO.setRemitterFlatDoorBlockNo(data.getFlatDoorBlockNo());
		form15CAPartBDTO.setRemitterNameBuildingVillage(data.getNameBuildingVillage());
		form15CAPartBDTO.setRemitterRoadStreetPostoffice(data.getRoadStreetPostoffice());
		form15CAPartBDTO.setRemitterAreaLocality(data.getAreaLocality());
		form15CAPartBDTO.setRemitterTownCityDistrict(data.getTownCityDistrict());
		form15CAPartBDTO.setRemitterState(data.getStateName());
		form15CAPartBDTO.setRemitterCountry(data.getCountryName());
		form15CAPartBDTO.setRemitterPinCode(data.getPinCode());
		if (StringUtils.isNotBlank(data.getDvndNameOfBank())) {
			form15CAPartBDTO.setNameOfBank(data.getDvndNameOfBank().toUpperCase());
		} else {
			errorMsg = errorMsg + "Name Of The Bank is Mandatory.It Can Not Be Blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndBranchOfBank())) {
			form15CAPartBDTO.setBranchOfBank(data.getDvndBranchOfBank().toUpperCase());
		} else {
			errorMsg = errorMsg + "Name of the branch of the bank is Mandatory.It Can Not Be Blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndBsrCodeOfBankBranch())) {
			form15CAPartBDTO.setBsrCodeOfBankBranch(data.getDvndBsrCodeOfBankBranch().toUpperCase());
		} else {
			errorMsg = errorMsg + "BSR Code of the bank branch (7 digits) is Mandatory.It Can Not Be Blank" + "\n";
		}
		return errorMsg;
	}

	private String populateShareholderMasterDate(Form15CAPartBDTO form15CAPartBDTO, String deductorPan,
			Integer shareholderId, String tenantId) {
		String errorMsg = "";
		if (!Objects.isNull(shareholderId)) {
			ResponseEntity<ApiStatus<ShareholderMasterDTO>> shareholderNonResident = onboardingClient
					.getNonResidentialShareholder(deductorPan, tenantId, shareholderId);
			ShareholderMasterDTO data = Objects.requireNonNull(shareholderNonResident.getBody()).getData();
			form15CAPartBDTO.setShareholderIsdCodePhoneNumber(data.getContact());
			form15CAPartBDTO.setShareHolderId(shareholderId);

			if (data.getAddress() != null) {
				if (StringUtils.isNotBlank(data.getAddress().getFlatDoorBlockNo())) {
					form15CAPartBDTO.setFlatDoorBlockNo(data.getAddress().getFlatDoorBlockNo());
				} else {
					errorMsg = errorMsg
							+ "Shareholder Holder Flat / Door / Building Is Mandatory to Generate FORM 15 CA Part B"
							+ "\n";
				}
				if (StringUtils.isNotBlank(data.getAddress().getAreaLocality())) {
					form15CAPartBDTO.setAreaLocality(data.getAddress().getAreaLocality());
				} else {
					errorMsg = errorMsg + "Shareholder Holder Area / Locality Is Mandatoryto Generate FORM 15 CA Part B"
							+ "\n";
				}
				form15CAPartBDTO.setTownCityDistrict(data.getAddress().getTownCityDistrict());

				if (StringUtils.isNotBlank(data.getAddress().getPinCode())) {
					form15CAPartBDTO.setPinCode(data.getAddress().getPinCode());
				} else {
					errorMsg = errorMsg + "ShareholderZIP Code Is Mandatoryto Generate FORM 15 CA Part B" + "\n";
				}
				if (StringUtils.isNotBlank(data.getAddress().getCountry())) {
					form15CAPartBDTO.setCountry(data.getAddress().getCountry());
				} else {
					errorMsg = errorMsg + "Shareholder Country Is Mandatoryto Generate FORM 15 CA Part B" + "\n";
				}
				if (StringUtils.isNotBlank(data.getAddress().getStateName())) {
					form15CAPartBDTO.setState(data.getAddress().getStateName());
				} else {
					errorMsg = errorMsg + "Shareholder State Is Mandatoryto Generate FORM 15 CA Part B" + "\n";
				}
				if (StringUtils.isNotBlank(data.getEmailId()) && data.getEmailId().equals("None")) {
					form15CAPartBDTO.setShareholderEmail(data.getEmailId());
				}
				form15CAPartBDTO.setNameBuildingVillage(data.getAddress().getNameBuildingVillage());
				form15CAPartBDTO.setRoadStreetPostoffice(data.getAddress().getRoadStreetPostoffice());

			} else {
				errorMsg = errorMsg + "Addres Is Required To Generate Form 15 CB Part B" + "\n";
			}
		}
		return errorMsg;
	}

	private String populateShareholderMasterDate(Form15CAPartCDTO form15CAPartCDTO, String deductorPan,
			Integer shareholderId, String tenantId) {
		String errorsg = "";
		if (!Objects.isNull(shareholderId)) {
			ResponseEntity<ApiStatus<ShareholderMasterDTO>> shareholderNonResident = onboardingClient
					.getNonResidentialShareholder(deductorPan, tenantId, shareholderId);
			ShareholderMasterDTO data = Objects.requireNonNull(shareholderNonResident.getBody()).getData();
			form15CAPartCDTO.setShareholderIsdCodePhoneNumber(data.getContact());
			form15CAPartCDTO.setShareHolderId(data.getId());
			if (StringUtils.isNotBlank(data.getAddress().getFlatDoorBlockNo())) {
				form15CAPartCDTO.setFlatDoorBlockNo(data.getAddress().getFlatDoorBlockNo());
			} else {
				errorsg = errorsg + "Share Holder Flat / Door / Building Is Mandatory to Generate FORM 15 CA Part C"
						+ "\n";
			}
			if (StringUtils.isNotBlank(data.getAddress().getAreaLocality())) {
				form15CAPartCDTO.setAreaLocality(data.getAddress().getAreaLocality());
			} else {
				errorsg = errorsg + "Share Holder Area / Locality Is Mandatoryto Generate FORM 15 CA Part C" + "\n";
			}
			if (StringUtils.isNotBlank(data.getAddress().getTownCityDistrict())) {
				form15CAPartCDTO.setTownCityDistrict(data.getAddress().getTownCityDistrict());
			} else {
				errorsg = errorsg + "Share Holder Town / City / District Is Mandatoryto Generate FORM 15 CA Part C"
						+ "\n";
			}

			if (StringUtils.isNotBlank(data.getAddress().getPinCode())) {
				form15CAPartCDTO.setPinCode(data.getAddress().getPinCode());
			} else {
				errorsg = errorsg + "ZIP Code Is Mandatoryto Generate FORM 15 CA Part C" + "\n";
			}
			if (StringUtils.isNotBlank(data.getAddress().getCountry())) {
				form15CAPartCDTO.setCountry(data.getAddress().getCountry());
			} else {
				errorsg = errorsg + "Shareholder Country Is Mandatoryto Generate FORM 15 CA Part B" + "\n";
			}
			if (StringUtils.isNotBlank(data.getAddress().getStateName())) {
				form15CAPartCDTO.setState(data.getAddress().getStateName());
			} else {
				errorsg = errorsg + "Shareholder State Is Mandatoryto Generate FORM 15 CA Part B" + "\n";
			}
			if (StringUtils.isNotBlank(data.getEmailId()) && !data.getEmailId().equals("None")) {
				form15CAPartCDTO.setShareholderEmail(data.getEmailId());
			}

			form15CAPartCDTO.setNameBuildingVillage(data.getAddress().getNameBuildingVillage());
			form15CAPartCDTO.setRoadStreetPostoffice(data.getAddress().getRoadStreetPostoffice());

		}
		return errorsg;
	}

	private void populateMasterData(Form15CAPartBDTO form15CAPartBDTO, String remittanceMadeCountry) {
		ResponseEntity<ApiStatus<List<Country>>> response = mastersClient.getCountries();
		List<Country> countries = Objects.requireNonNull(response.getBody()).getData();
		Optional<Country> country = countries.stream().filter(x -> x.getName().equalsIgnoreCase(remittanceMadeCountry))
				.findFirst();
		if (country.isPresent()) {
			form15CAPartBDTO.setCurrency(
					!Objects.isNull(country.get().getCurrency()) ? country.get().getCurrency().toUpperCase() : "");
		}
	}

	private Country populateMasterData(Form15CAPartCDTO form15CAPartCDTO, String remittanceMadeCountry,
			String countryName) {
		Country country1 = null;
		ResponseEntity<ApiStatus<List<Country>>> response = mastersClient.getCountries();
		List<Country> countries = Objects.requireNonNull(response.getBody()).getData();
		Optional<Country> country = countries.stream().filter(x -> x.getName().equalsIgnoreCase(remittanceMadeCountry))
				.findFirst();
		if (country.isPresent()) {
			form15CAPartCDTO.setCurrency(
					!Objects.isNull(country.get().getCurrency()) ? country.get().getCurrency().toUpperCase() : "");
		}
		Optional<Country> countryOptional = countries.stream().filter(x -> x.getName().equalsIgnoreCase(countryName))
				.findFirst();
		if (countryOptional.isPresent()) {
			country1 = countryOptional.get();
		}
		return country1;
	}

	private Country populateMasterData(Form15CAPartDDTO form15CAPartDDTO, String remittanceMadeCountry,
			String countryName) {
		Country country1 = null;
		ResponseEntity<ApiStatus<List<Country>>> response = mastersClient.getCountries();
		List<Country> countries = Objects.requireNonNull(response.getBody()).getData();
		Optional<Country> country = countries.stream().filter(x -> x.getName().equalsIgnoreCase(remittanceMadeCountry))
				.findFirst();
		if (country.isPresent()) {
			form15CAPartDDTO.setCurrency(
					!Objects.isNull(country.get().getCurrency()) ? country.get().getCurrency().toUpperCase() : "");
		}
		Optional<Country> countryOptional = countries.stream().filter(x -> x.getName().equalsIgnoreCase(countryName))
				.findFirst();
		if (countryOptional.isPresent()) {
			country1 = countryOptional.get();
		}
		return country1;
	}

	private String checkBigdecimal(BigDecimal value) {
		if (value == null) {
			return StringUtils.EMPTY;
		} else {
			return (value.compareTo(BigDecimal.valueOf(0.0)) == 0 ? BigDecimal.ZERO : value).toString();
		}

	}

	private String checkBoolean(Boolean value) {
		String str = "";
		if (Objects.isNull(value)) {
			str = "No";
		} else {
			if (value.toString().equalsIgnoreCase("True")) {
				str = "Yes";
			}
			if (value.toString().equalsIgnoreCase("False")) {
				str = "No";
			}
		}
		return str;
	}

	public Form15FilingDetails createFilingReportStatus(String deductorTan, Integer assessmentYear,
			String dateOfPosting, Form15FileType fileType, Form15FileFormat fileFormat, ReturnType returnType,
			String userName) {

		logger.info("Fetching filings record with tan,yera,date of posting,file type, file format with values "
				+ deductorTan + "," + assessmentYear + "," + dateOfPosting + "," + fileType + "," + fileFormat + "{}");
		List<Form15FilingDetails> existingFilings = form15FilingDetailsDAO.findFilingFiles(deductorTan, assessmentYear,
				dateOfPosting, fileType, fileFormat);
		Form15FilingDetails filingReportStatus = existingFilings.isEmpty() ? null : existingFilings.get(0);

		Date today = new Date();
		if (filingReportStatus == null) {
			logger.info("Creating filing record {}");
			filingReportStatus = new Form15FilingDetails();
			filingReportStatus.setGeneratedDate(null);
			filingReportStatus.setUpdatedBy(null);
			filingReportStatus.setUpdatedDate(null);
			filingReportStatus.setFilingFilesDeductorTan(deductorTan);
			filingReportStatus.setActive(true);
			filingReportStatus.setFilingType(returnType);
			filingReportStatus.setFormFifteenFileType(fileType);
			filingReportStatus.setFormFifteenFileFormat(fileFormat);
			filingReportStatus.setStatus(Form15FilingStatus.PROCESSING);
			filingReportStatus.setIsRequested(true);
			filingReportStatus.setCreatedBy(userName);
			filingReportStatus.setCreatedDate(today);
			filingReportStatus.setAssessmentYear(assessmentYear);
			filingReportStatus.setDateOfPosting(dateOfPosting);
			return form15FilingDetailsDAO.save(filingReportStatus);
		} else {
			logger.info("Updating fiing record {}");
			filingReportStatus.setFilingFilesDeductorTan(deductorTan);
			filingReportStatus.setActive(true);
			filingReportStatus.setFilingType(returnType);
			filingReportStatus.setFormFifteenFileType(fileType);
			filingReportStatus.setFormFifteenFileFormat(fileFormat);
			filingReportStatus.setStatus(Form15FilingStatus.PROCESSING);
			filingReportStatus.setIsRequested(true);
			filingReportStatus.setCreatedBy(userName);
			filingReportStatus.setCreatedDate(today);
			filingReportStatus.setStringFilingType(returnType.name());
			filingReportStatus.setStringFileStatus(Form15FilingStatus.PROCESSING.name());
			filingReportStatus.setStringFileFormat(fileFormat.name());
			filingReportStatus.setStringFilingType(returnType.name());
			return form15FilingDetailsDAO.update(filingReportStatus);
		}
	}

	public Form15FilingDetails updateFilingReportStatus(String deductorTan, Integer assessmentYear,
			String dateOfPosting, Form15FileType fileType, Form15FileFormat fileFormat, ReturnType returnType,
			Form15FilingStatus filingStatus, String fileBlobUrl, String errorDescription, String userName) {

		List<Form15FilingDetails> existingFilings = form15FilingDetailsDAO.findFilingFiles(deductorTan, assessmentYear,
				dateOfPosting, fileType, fileFormat);
		Form15FilingDetails filingReportStatus = existingFilings.isEmpty() ? null : existingFilings.get(0);

		if (filingReportStatus == null) {
			throw new IllegalStateException("FilingFiles15CACB record not found for deductorTan: " + deductorTan
					+ ", assessmentYear: " + assessmentYear + ", dateOfPosting: " + dateOfPosting + ", fileType: "
					+ fileType + ", fileFormat: " + fileFormat);
		}
		try {
			filingReportStatus.setStringFileStatus(filingStatus.name());
			filingReportStatus.setFileUrl(fileBlobUrl);
			filingReportStatus.setError(errorDescription);
			filingReportStatus.setUpdatedBy(userName);
			filingReportStatus.setUpdatedDate(CommonUtil.fileUploadTime(new Date()));
			if (filingStatus.isGenerated()) {
				filingReportStatus.setGeneratedDate(CommonUtil.fileUploadTime(new Date()));
			}
			filingReportStatus = form15FilingDetailsDAO.update(filingReportStatus);
		} catch (Exception e) {
			logger.info("Exception occured while updating filing table {}");
		}
		return filingReportStatus;
	}

	public String generateForm15CAFromExcel(MultipartFile file, String deductorTan, String deductorPan,
			String dateOfPosting, String tenantId, String userName, Integer assessmentYear, Form15FileFormat fileFormat)
			throws IOException {
		logger.info("Filing 15CB Excel: Started generation for TAN: {}, date of posting : {} ", deductorTan,
				dateOfPosting);

		createFilingReportStatus(deductorTan, assessmentYear, dateOfPosting, Form15FileType.CA, Form15FileFormat.EXCEL,
				ReturnType.REGULAR, userName);
		logger.info("Filing record got created/updated {}");
		InputStream stream = file.getInputStream();

		try {
			checkOnboardingInformation(deductorPan, tenantId);
			logger.info("Onboarding information checked {}");
		} catch (Exception e) {
			logger.error("Error occured while Generating Form 15CB excel", e);
			updateFilingReportStatus(deductorTan, assessmentYear, dateOfPosting, Form15FileType.CB,
					Form15FileFormat.EXCEL, ReturnType.REGULAR, Form15FilingStatus.ERROR, null, e.getMessage(),
					userName);
		}

		try {
			XSSFWorkbook workbook = new XSSFWorkbook(stream);
			workbook.getSheet("15CA Part A");
			workbook.getSheet("15CA Part B");
			workbook.getSheet("15CA Part C");
			CompletableFuture.runAsync(() -> process15CAExcelFile(workbook, deductorTan, deductorPan, dateOfPosting,
					tenantId, userName, assessmentYear, fileFormat));
		} catch (Exception e) {
			logger.info(e.getMessage());
			throw new RuntimeException("Invalid Form 15 CA file ");
		}

		return "Request for generating Form 15CB Excel submitted successfully";

	}

	private void process15CAExcelFile(XSSFWorkbook workbook, String deductorTan, String deductorPan,
			String dateOfPosting, String tenantId, String userName, Integer assessmentYear,
			Form15FileFormat fileFormat) {
		logger.info("Processing the file asynchronusly to get data and prepare DTO {}");
		MultiTenantContext.setTenantId(tenantId);
		try {
			XSSFSheet sheetA, sheetB, sheetC = null;
			List<Form15CAPartADTO> listOfPartADTO = new ArrayList<>();
			List<Form15CAPartBDTO> listOfPartBDTO = new ArrayList<>();
			List<Form15CAPartCDTO> listOfPartCDTO = new ArrayList<>();
			List<ShareholderMasterNonResidential> listShareHolder = new ArrayList<>();
			List<InvoiceShareholderNonResident> listInvoiceShareHolder = new ArrayList<>();

			sheetA = workbook.getSheet("15CA Part A");
			sheetB = workbook.getSheet("15CA Part B");
			sheetC = workbook.getSheet("15CA Part C");

			logger.info("Retrieved 3 sheets" + sheetA.getSheetName() + "," + sheetB.getSheetName() + ","
					+ sheetC.getSheetName() + " {}");

			listOfPartADTO = processSheetPartA(sheetA);
			listOfPartBDTO = processSheetPartB(sheetB);
			listOfPartCDTO = processSheetPartC(sheetC);

			processListForm15CAPartADTO(listOfPartADTO, deductorPan, listShareHolder, listInvoiceShareHolder);
			processListForm15CBPartBDTO(listOfPartBDTO, deductorPan, listShareHolder, listInvoiceShareHolder);
			processListForm15CBPartCDTO(listOfPartCDTO, deductorPan, listShareHolder, listInvoiceShareHolder);

			updateListOfNRShareHolder(listShareHolder);
			updateListOfNRInvoiceShareHolder(listInvoiceShareHolder);
			create15CAFilingReport(deductorTan, deductorPan, dateOfPosting, tenantId, userName, assessmentYear);
		} catch (Exception e) {
			logger.info("Exception occured while processing the file " + e + "{}");

		}
	}

	/**
	 * this method is responsible for reading sheet Part A and creating list of
	 * Form15CAPartADTO with data from uploaded excel
	 * 
	 * @param sheetA
	 * @throws Exception
	 */
	private List<Form15CAPartADTO> processSheetPartA(XSSFSheet sheetA) throws Exception {
		logger.info("Processing sheet A {}");
		List<Form15CAPartADTO> listOfPartADTO = new ArrayList<>();
		int dataRowsCount = 0;
		XSSFRow headerRow = null;
		int columnCount = 0;
		List<String> headerList = null;
		Map<String, String> headerAndField = null;

		headerRow = sheetA.getRow(6);
		dataRowsCount = sheetA.getLastRowNum();
		columnCount = headerRow.getLastCellNum();

		headerList = getHeaderListOfPartA(headerRow, columnCount);
		headerAndField = getHeadersMappingForPartA();

		/**
		 * this for loop will iterate each row having data and will pick the cell values
		 * from the row in each iteration it will be creating one Form15CAPartADTO
		 * object and will assign the values to respective fields that is mapped in
		 * getHeadersMappingForPartA() method
		 */
		for (int rowIndex = 7; rowIndex <= dataRowsCount; rowIndex++) {
			XSSFRow dataRow = sheetA.getRow(rowIndex);
			Form15CAPartADTO dto = new Form15CAPartADTO();
			Integer index = 0;

			for (int cellNo = 15; cellNo <= columnCount; cellNo++) {
				XSSFCell cell = dataRow.getCell(cellNo);

				if (cell != null && index < headerList.size()) {
					cell.setCellType(CellType.STRING);
					String value = cell.getStringCellValue();

					if (headerAndField.get(headerList.get(index).trim()) != null) {
						Field field = Form15CAPartADTO.class
								.getDeclaredField(headerAndField.get(headerList.get(index).trim()));
						field.setAccessible(true);
						if (field.get(dto) == null && StringUtils.isNotBlank(value)) {
							convertValueToDataTypeOfFieldAndAssignToFieldsOfDTO(field, value, dto);
						}
					}

				}
				index++;
			} // for loop iterating cells
			listOfPartADTO.add(dto);
		} // for loop iterating rows
		logger.info(
				"Sheet A processed succesfully and list of DTO s prepared with size " + listOfPartADTO.size() + " {}");
		return listOfPartADTO;

	}

	/**
	 * this method is responsible for reading sheet Part B and creating list of
	 * Form15CAPartBDTO with data from uploaded excel
	 * 
	 * @param sheetA
	 * @throws Exception
	 */
	private List<Form15CAPartBDTO> processSheetPartB(XSSFSheet sheetA) throws Exception {
		logger.info("Sheet B is processing {}");
		List<Form15CAPartBDTO> listOfPartBDTO = new ArrayList<>();
		int dataRowsCount = 0;
		XSSFRow headerRow = null;
		int columnCount = 0;
		List<String> headerList = null;
		Map<String, String> headerAndField = null;

		headerRow = sheetA.getRow(6);
		dataRowsCount = sheetA.getLastRowNum();
		columnCount = headerRow.getLastCellNum();

		headerList = getHeaderListOfPartB(headerRow, columnCount);
		headerAndField = getHeadersMappingForPartB();

		/**
		 * this for loop will iterate each row having data and will pick the cell values
		 * from the row in each iteration it will be creating one Form15CAPartADTO
		 * object and will assign the values to respective fields that is mapped in
		 * getHeadersMappingForPartA() method
		 */
		for (int rowIndex = 7; rowIndex <= dataRowsCount; rowIndex++) {
			XSSFRow dataRow = sheetA.getRow(rowIndex);
			Form15CAPartBDTO dto = new Form15CAPartBDTO();
			Integer index = 0;

			for (int cellNo = 15; cellNo <= columnCount; cellNo++) {
				XSSFCell cell = dataRow.getCell(cellNo);

				if (cell != null && index < headerList.size()) {
					try {

						cell.setCellType(CellType.STRING);
						String value = cell.getStringCellValue();

						if (headerAndField.get(headerList.get(index).trim()) != null) {
							Field field = Form15CAPartBDTO.class
									.getDeclaredField(headerAndField.get(headerList.get(index).trim()));
							field.setAccessible(true);
							if (field.get(dto) == null && StringUtils.isNotBlank(value)) {
								convertValueToDataTypeOfFieldAndAssignToFieldsOfDTO(field, value, dto);
							}
						}
					} catch (Exception e) {
						logger.info("Exception occured while fetching field with header name " + headerList.get(index)
								+ "{}");
						throw new NullPointerException(
								"Exception occured while fetching field with header name " + headerList.get(index));
					}

				}
				index++;
			} // for loop iterating cells
			listOfPartBDTO.add(dto);
		} // for loop iterating rows
		logger.info(
				"Sheet B processed succesfully and list of DTO s prepared with size " + listOfPartBDTO.size() + " {}");
		return listOfPartBDTO;

	}

	/**
	 * this method is responsible for reading sheet Part C and creating list of
	 * Form15CAPartCDTO with data from uploaded excel
	 * 
	 * @param sheetA
	 * @throws Exception
	 */
	private List<Form15CAPartCDTO> processSheetPartC(XSSFSheet sheetA) throws Exception {
		logger.info("Sheet C is processing {}");
		List<Form15CAPartCDTO> listOfPartBDTO = new ArrayList<>();
		int dataRowsCount = 0;
		XSSFRow headerRow = null;
		int columnCount = 0;
		List<String> headerList = null;
		Map<String, String> headerAndField = null;

		headerRow = sheetA.getRow(6);
		dataRowsCount = sheetA.getLastRowNum();
		columnCount = headerRow.getLastCellNum();

		headerList = getHeaderListOfPartC(headerRow, columnCount);
		headerAndField = getHeadersMappingForPartC();

		/**
		 * this for loop will iterate each row having data and will pick the cell values
		 * from the row in each iteration it will be creating one Form15CAPartADTO
		 * object and will assign the values to respective fields that is mapped in
		 * getHeadersMappingForPartA() method
		 */
		for (int rowIndex = 7; rowIndex <= dataRowsCount; rowIndex++) {
			XSSFRow dataRow = sheetA.getRow(rowIndex);
			Form15CAPartCDTO dto = new Form15CAPartCDTO();
			Integer index = 0;

			for (int cellNo = 21; cellNo <= columnCount; cellNo++) {
				XSSFCell cell = dataRow.getCell(cellNo);

				if (cell != null && index < headerList.size()) {
					try {
						cell.setCellType(CellType.STRING);
						String value = cell.getStringCellValue();

						if (headerAndField.get(headerList.get(index)) != null) {
							Field field = Form15CAPartCDTO.class
									.getDeclaredField(headerAndField.get(headerList.get(index)));
							field.setAccessible(true);
							if (field.get(dto) == null && StringUtils.isNotBlank(value)) { // checkin if the field is
																							// already having the value
								convertValueToDataTypeOfFieldAndAssignToFieldsOfDTO(field, value, dto);
							}
						}

					} catch (Exception e) {
						logger.info("Exception occured while fetching field with header name " + headerList.get(index)
								+ "{}" + e);
					}

				}
				index++;
			} // for loop iterating cells
			listOfPartBDTO.add(dto);
		} // for loop iterating rows
		logger.info(
				"Sheet A processed succesfully and list of DTO s prepared with size " + listOfPartBDTO.size() + " {}");
		return listOfPartBDTO;

	}

	/**
	 * responsible to create a list and return having the header names of part b
	 * under which values can be modified by user
	 * 
	 * @param headerRow
	 * @param columnCount
	 * @return List<String>
	 */
	private List<String> getHeaderListOfPartA(XSSFRow headerRow, int columnCount) {
		List<String> list = new ArrayList<>();
		for (int cellNo = 15; cellNo <= columnCount; cellNo++) {
			String value = headerRow.getCell(cellNo) == null ? "" : headerRow.getCell(cellNo).getStringCellValue();
			if (StringUtils.isNotBlank(value)) {
				list.add(value);
			}
		}
		return list;

	}

	/**
	 * responsible to create a list and return having the header names of part
	 * bunder which values can be modified by user
	 * 
	 * @param headerRow
	 * @param columnCount
	 * @return List<String>
	 */
	private List<String> getHeaderListOfPartB(XSSFRow headerRow, int columnCount) {
		List<String> list = new ArrayList<>();
		for (int cellNo = 15; cellNo <= columnCount; cellNo++) {
			String value = headerRow.getCell(cellNo) == null ? "" : headerRow.getCell(cellNo).getStringCellValue();
			if (StringUtils.isNotBlank(value)) {
				list.add(value);
			}
		}
		return list;

	}

	/**
	 * responsible to create a list and return having the header names of part
	 * bunder which values can be modified by user
	 * 
	 * @param headerRow
	 * @param columnCount
	 * @return List<String>
	 */
	private List<String> getHeaderListOfPartC(XSSFRow headerRow, int columnCount) {
		List<String> list = new ArrayList<>();
		for (int cellNo = 21; cellNo <= columnCount; cellNo++) {
			String value = headerRow.getCell(cellNo) == null ? "" : headerRow.getCell(cellNo).getStringCellValue();
			if (StringUtils.isNotBlank(value)) {
				list.add(value);
			}
		}
		return list;

	}

	/**
	 * responsible for converting value to a separate data type as per the data type
	 * of the field to which we are going to assign the values
	 * 
	 * @param field
	 * @param value
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	public Field convertValueToDataTypeOfFieldAndAssignToFieldsOfDTO(Field field, String value, Object dto)
			throws Exception {
		try {
			if (field.getType().getSimpleName().equalsIgnoreCase("String")) {
				field.set(dto, value);
			} else if (field.getType().getSimpleName().equalsIgnoreCase("integer")) {
				Integer intValue = Integer.parseInt(value);
				field.set(dto, intValue);
			} else if (field.getType().getSimpleName().equalsIgnoreCase("BigDecimal")) {//
				BigDecimal decimalValue = new BigDecimal(Double.parseDouble(value));
				field.set(dto, decimalValue);
			} else {

				Date dateValue = null;
				if (value.contains("-")) {
					dateValue = new SimpleDateFormat("dd-MM-yyyy").parse(value);
					field.set(dto, dateValue);
				} else if (value.contains("/")) {
					dateValue = new SimpleDateFormat("dd/MM/yyyy").parse(value);
					field.set(dto, dateValue);
				} else {
					dateValue = DateUtil.getJavaDate(Double.parseDouble(value));
					field.set(dto, dateValue);
				}
			}

		} catch (ParseException e) {
			logger.info("Exception occured while assigning value " + value + " to field " + field + " {}");
			throw new IllegalArgumentException(
					"Exception occured while assigning value " + value + " to field " + field + " {}");
		}
		return field;
	}

	/**
	 * returns a map having the form 15 CA parta A headers in key and
	 * Form15CAPartADTO fields as value
	 * 
	 * @return
	 */
	private Map<String, String> getHeadersMappingForPartA() {
		Map<String, String> map = new HashMap<>();
		map.put("INVOICE SHARE HOLDER ID", "id");
		map.put("Name of recipient of remittance", "shareholderName");
		map.put("PAN of the recipient of remittance (if available)", "shareholderPan");
		map.put("Email Address", "shareholderEmail");
		map.put("Phone Number", "shareholderPhoneNumber");
		map.put("Country to which remittance is made", "remittanceCountry");
		map.put("Flat / Door / Block No", "flatDoorBlockNo");
		map.put("Name of Premises / Building / Village", "nameBuildingVillage");
		map.put("Road / Street / Post Office", "roadStreetPostoffice");
		map.put("Area / Locality", "areaLocality");
		map.put("Town / City / District", "townCityDistrict");
		map.put("State", "state");
		map.put("Country", "country");
		map.put("ZIP Code", "pinCode");
		map.put("Amount Payable in foreign currency", "amountPayableInForeignCurrency");
		map.put("Amount payable before TDS (in Indian currency)", "amountPayableBeforeTdsInInr");
		map.put("Aggregate amount of remittance made during the financial year including this proposed remittance",
				"aggregateRemittanceAmount");
		map.put("Name of Bank", "nameOfBank");
		map.put("Name of the branch of the bank", "branchOfBank");
		map.put("Proposed date of remittance(DD/MM/YYYY)", "proposedDateOfRemittance");
		map.put("Nature of remittance", "natureOfRemittance");
		map.put("Please furnish the relevant purpose code as per RBI", "rbiPurposeCode");
		map.put("Amount of TDS", "tdsAmount");
		map.put("Rate of TDS", "tdsRate");
		map.put("Date of deduction", "deductionDate");
		map.put("Shareholder Master id", "shareHolderId");
		return map;

	}

	private Map<String, String> getHeadersMappingForPartB() {
		Map<String, String> map = getHeadersMappingForPartA();
		map.put("Email ID of the recipient of remittance", "shareholderEmail");
		map.put("ISD Code - Phone Number of the recipient of the remittance", "shareholderIsdCodePhoneNumber");
		map.put("Section under which order/certificate has been obtained", "orderOrCertificateSection");
		map.put("Name of the Assessing Officer who issued the  order/certificate", "assessingOfficerName");
		map.put("Designation of the Assessing Officer who issued the order/certificate", "assessingOfficerDesignation");
		map.put("Date of Order/Certificate", "orderOrCertificateDate");
		map.put("Order/Certificate Number", "orderOrCertificateNumber");
		map.put("Country to which remittance is made", "remittanceCountry");
		map.put("Currency", "currency");
		map.put("Amount Payable in foreign currency", "amountPayableInForeignCurrency");
		map.put("Amount Payable in Indian Rs", "amountPayableInInr");
		map.put("Name of Bank", "nameOfBank");
		map.put("Name of the branch of the bank", "branchOfBank");
		map.put("BSR Code of the bank branch (7 digits)", "bsrCodeOfBankBranch");
		map.put("Proposed date of remittance(DD/MM/YYYY)", "proposedDateOfRemittance");
		map.put("Nature of remittance as per agreed document", "natureOfRemittance");
		map.put("Please furnish the relevant purpose code as per RBI", "rbiPurposeCode");
		map.put("Amount of TDS", "tdsAmount");
		map.put("Rate of TDS", "tdsRate");
		map.put("Date of Deduction(DD/MM/YYYY)", "deductionDate");
		return map;
	}

	private Map<String, String> getHeadersMappingForPartC() {
		Map<String, String> map = new HashMap<>();
		map.put("INVOICE SHARE HOLDER ID", "id");
		map.put("Acknowledgment number of Form 15CB", "form15CBAcknowledgementNumber");
		map.put("Name of recipient of remittance", "shareholderName");
		map.put("PAN of the recipient of remittance (if available)", "shareholderPan");
		map.put("Status (i.e. company etc)", "shareholderStatus");
		map.put("Country to which remittance is made", "country");
		map.put("Principal place of business", "shareholderPrincipalPlaceOfBusiness");
		map.put("Email ID of the recipient of remittance", "shareholderEmail");
		map.put("ISD Code - Phone Number", "shareholderIsdCodePhoneNumber");
		map.put("Flat / Door / Block No", "flatDoorBlockNo");
		map.put("Name of Premises / Building / Village", "nameBuildingVillage");
		map.put("Road / Street / Post Office", "roadStreetPostoffice");
		map.put("Area / Locality", "areaLocality");
		map.put("Town / City / District", "townCityDistrict");
		map.put("State", "state");
		map.put("Country", "shareHolderRemittanceCountry");
		map.put("ZIP Code", "pinCode");
		map.put("Amount Payable in foreign currency", "amountPayableInForeignCurrency");
		map.put("Country to which remittance is made", "remittanceCountry");
		map.put("Principal place of business", "shareholderPrincipalPlaceOfBusiness");
		map.put("Certificate Number (Form 15CB)", "certificateNumber");
		map.put("Date of certificate number (Form 15CB)", "certificateNumberDate");
		map.put("Date of deduction of tax at source, if any (DD/MM/YYYY)", "taxAtSourceDeductionDate");
		map.put("Actual amount of remittance after TDS (in foreign currency) ",
				"actualRemittanceAmountAfterTdsInForeignCurrency");
		map.put("Shareholder Master id", "shareHolderId");
		map.put("Amount of TDS In foreign currency", "tdsAmountInForeignCurrency");

		/**
		 * map.put("Proposed date of remittance (DD/MM/YYYY)",
		 * "proposedDateOfRemittance"); map.put("Nature of remittance as per agreed
		 * document", "natureOfRemittance"); map.put("Relevant purpose code as per RBI",
		 * "rbiPurposeCode"); map.put("In case the remittance is net of taxes, whether
		 * tax payable has been grossed up?", ""); map.put("The amount of income
		 * chargeable to tax", "incomeChargeableToTax"); map.put("The tax liability",
		 * "taxLiability"); map.put("Certificate Number (Form 15CB)",
		 * "certificateNumber"); map.put("Date of certificate number (Form 15CB)",
		 * "certificateNumberDate"); map.put("Name of the Assessing Officer who issued
		 * the order/certificate", "assessingOfficerName"); map.put("Designation of the
		 * Assessing Officer who issued the order/certificate*",
		 * "assessingOfficerDesignation"); map.put("Date of Order/Certificate",
		 * "orderOrCertificateDate"); map.put("Order/Certificate Number",
		 * "orderOrCertificateNumber"); map.put("Currency", "currency");
		 */

		return map;
	}

	private void processListForm15CAPartADTO(List<Form15CAPartADTO> list, String deductorPan,
			List<ShareholderMasterNonResidential> listShareHolder, List<InvoiceShareholderNonResident> listnrInv) {
		logger.info("Processing list of Form15CAPartADTO to get Share Holder records {}");
		List<InvoiceShareholderNonResident> invoiceNr = null;
		List<ShareholderMasterNonResidential> nrShareholders = null;
		Map<Integer, Form15CAPartADTO> shareHoldermap = list.stream()
				.collect(Collectors.toMap(Form15CAPartADTO::getShareHolderId, Function.identity()));
		Map<Integer, Form15CAPartADTO> invoiceMap = list.stream()
				.collect(Collectors.toMap(Form15CAPartADTO::getId, Function.identity()));
		List<Integer> listInvoiceId = list.stream().map(n -> n.getId()).collect(Collectors.toList());
		List<Integer> shareholderIdlist = list.stream().map(n -> n.getShareHolderId()).collect(Collectors.toList());

		if (!listInvoiceId.isEmpty()) {
			String ids = listInvoiceId.toString().replace("[", "(").replace("]", ")");
			invoiceNr = form15FilingDetailsDAO.findByIdPan(ids, deductorPan);
			invoiceNr.stream().forEach(invoice -> {
				invoice.setRemittanceMadeCountry(invoiceMap.get(invoice.getId()).getRemittanceCountry());
				invoice.setProposedDateOfRemmitence(invoiceMap.get(invoice.getId()).getProposedDateOfRemittance());
				invoice.setDateOfPosting(invoiceMap.get(invoice.getId()).getDeductionDate());
				listnrInv.add(invoice);
			});
		}

		if (!shareholderIdlist.isEmpty()) {
			String ids = shareholderIdlist.toString().replace("[", "(").replace("]", ")");
			nrShareholders = onboardingClient
					.getNonResidentialShareholderByIdsFeign(MultiTenantContext.getTenantId(), deductorPan, ids)
					.getBody().getData();
			nrShareholders.stream().forEach(share -> {
				// share.setEmailId(shareHoldermap.get(share.getId()).getShareholderEmail());
				share.setFlatDoorBlockNo(shareHoldermap.get(share.getId()).getFlatDoorBlockNo());
				share.setRoadStreetPostoffice(shareHoldermap.get(share.getId()).getRoadStreetPostoffice());
				share.setAreaLocality(shareHoldermap.get(share.getId()).getAreaLocality());
				share.setTownCityDistrict(shareHoldermap.get(share.getId()).getTownCityDistrict());
				// sshare.setCountry(shareHoldermap.get(share.getId()).getCountry());
				share.setPinCode(shareHoldermap.get(share.getId()).getPinCode());
				share.setNameBuildingVillage(shareHoldermap.get(share.getId()).getNameBuildingVillage());
				share.setState(shareHoldermap.get(share.getId()).getState());
				listShareHolder.add(share);
			});
		}

	}

	private void processListForm15CBPartBDTO(List<Form15CAPartBDTO> list, String deductorPan,
			List<ShareholderMasterNonResidential> listShareHolder, List<InvoiceShareholderNonResident> listnrInv) {
		logger.info("Processing list of Form15CAPartBDTO to get Share Holder records {}");
		List<InvoiceShareholderNonResident> invoiceNr = null;
		List<ShareholderMasterNonResidential> nrShareholders = null;
		Map<Integer, Form15CAPartBDTO> shareHoldermap = list.stream()
				.collect(Collectors.toMap(Form15CAPartBDTO::getShareHolderId, Function.identity()));
		Map<Integer, Form15CAPartBDTO> invoiceMap = list.stream()
				.collect(Collectors.toMap(Form15CAPartBDTO::getId, Function.identity()));
		List<Integer> listInvoiceId = list.stream().map(n -> n.getId()).collect(Collectors.toList());
		List<Integer> shareholderIdlist = list.stream().map(n -> n.getShareHolderId()).collect(Collectors.toList());

		if (!listInvoiceId.isEmpty()) {
			String ids = listInvoiceId.toString().replace("[", "(").replace("]", ")");
			invoiceNr = form15FilingDetailsDAO.findByIdPan(ids, deductorPan);
			invoiceNr.stream().forEach(invoice -> {
				invoice.setRemittanceMadeCountry(invoiceMap.get(invoice.getId()).getRemittanceCountry());
				invoice.setDividendAmountForeignCurrency(
						invoiceMap.get(invoice.getId()).getAmountPayableInForeignCurrency());
				invoice.setDividendAmountRs(invoiceMap.get(invoice.getId()).getAmountPayableInInr());
				invoice.setProposedDateOfRemmitence(invoiceMap.get(invoice.getId()).getProposedDateOfRemittance());
				invoice.setDateOfPosting(invoiceMap.get(invoice.getId()).getDeductionDate());
				listnrInv.add(invoice);
			});
		}

		if (!shareholderIdlist.isEmpty()) {
			String ids = shareholderIdlist.toString().replace("[", "(").replace("]", ")");
			nrShareholders = onboardingClient
					.getNonResidentialShareholderByIdsFeign(MultiTenantContext.getTenantId(), deductorPan, ids)
					.getBody().getData();
			nrShareholders.stream().forEach(share -> {
				// share.setEmailId(shareHoldermap.get(share.getId()).getShareholderEmail());
				share.setFlatDoorBlockNo(shareHoldermap.get(share.getId()).getFlatDoorBlockNo());
				share.setRoadStreetPostoffice(shareHoldermap.get(share.getId()).getRoadStreetPostoffice());
				share.setAreaLocality(shareHoldermap.get(share.getId()).getAreaLocality());
				// share.setTownCityDistrict(shareHoldermap.get(share.getId()).getTownCityDistrict());
				// share.setCountry(shareHoldermap.get(share.getId()).getCountry());
				share.setPinCode(shareHoldermap.get(share.getId()).getPinCode());
				share.setNameBuildingVillage(shareHoldermap.get(share.getId()).getNameBuildingVillage());
				share.setState(shareHoldermap.get(share.getId()).getState());
				listShareHolder.add(share);
			});
		}

	}

	private void processListForm15CBPartCDTO(List<Form15CAPartCDTO> list, String deductorPan,
			List<ShareholderMasterNonResidential> listShareHolder, List<InvoiceShareholderNonResident> listnrInv) {
		logger.info("Processing list of Form15CAPartCDTO to get Share Holder records {}");

		List<InvoiceShareholderNonResident> invoiceNr = null;
		List<ShareholderMasterNonResidential> nrShareholders = null;
		Map<Integer, Form15CAPartCDTO> shareHoldermap = list.stream()
				.collect(Collectors.toMap(Form15CAPartCDTO::getShareHolderId, Function.identity()));
		Map<Integer, Form15CAPartCDTO> invoiceMap = list.stream()
				.collect(Collectors.toMap(Form15CAPartCDTO::getId, Function.identity()));
		List<Integer> listInvoiceId = list.stream().map(n -> n.getId()).collect(Collectors.toList());
		List<Integer> shareholderIdlist = list.stream().map(n -> n.getShareHolderId()).collect(Collectors.toList());

		if (!listInvoiceId.isEmpty()) {
			String ids = listInvoiceId.toString().replace("[", "(").replace("]", ")");
			invoiceNr = form15FilingDetailsDAO.findByIdPan(ids, deductorPan);
			invoiceNr.stream().forEach(invoice -> {
				invoice.setRemittanceMadeCountry(invoiceMap.get(invoice.getId()).getRemittanceCountry());
				invoice.setPrincipalPlaceOfBusiness(
						invoiceMap.get(invoice.getId()).getShareholderPrincipalPlaceOfBusiness());
				invoice.setForm15CBAcknowledgementNo(invoiceMap.get(invoice.getId()).getCertificateNumber());
				invoice.setForm15CBGenartionDate(invoiceMap.get(invoice.getId()).getCertificateNumberDate());
				invoice.setDateOfPosting(invoiceMap.get(invoice.getId()).getTaxAtSourceDeductionDate());
				invoice.setActualRemmitanceAmountForeignCurrency(
						invoiceMap.get(invoice.getId()).getActualRemittanceAmountAfterTdsInForeignCurrency());
				invoice.setDividendAmountForeignCurrency(
						invoiceMap.get(invoice.getId()).getAmountPayableInForeignCurrency());
				listnrInv.add(invoice);
			});
		}

		if (!shareholderIdlist.isEmpty()) {
			String ids = shareholderIdlist.toString().replace("[", "(").replace("]", ")");
			nrShareholders = onboardingClient
					.getNonResidentialShareholderByIdsFeign(MultiTenantContext.getTenantId(), deductorPan, ids)
					.getBody().getData();
			nrShareholders.stream().forEach(share -> {
				share.setEmailId(shareHoldermap.get(share.getId()).getShareholderEmail());
				share.setFlatDoorBlockNo(shareHoldermap.get(share.getId()).getFlatDoorBlockNo());
				share.setRoadStreetPostoffice(shareHoldermap.get(share.getId()).getRoadStreetPostoffice());
				share.setAreaLocality(shareHoldermap.get(share.getId()).getAreaLocality());
				share.setTownCityDistrict(shareHoldermap.get(share.getId()).getTownCityDistrict());
				// share.setCountry(shareHoldermap.get(share.getId()).getShareHolderRemittanceCountry());
				share.setPinCode(shareHoldermap.get(share.getId()).getPinCode());
				share.setNameBuildingVillage(shareHoldermap.get(share.getId()).getNameBuildingVillage());
				share.setState(shareHoldermap.get(share.getId()).getState());
				listShareHolder.add(share);
			});
		}

	}

	private Integer updateListOfNRShareHolder(List<ShareholderMasterNonResidential> listShareHolder) {
		int count = form15FilingDetailsDAO.batchUpdate(listShareHolder);
		logger.info("Updated Share holder Record {}");
		return count;
	}

	private Integer updateListOfNRInvoiceShareHolder(List<InvoiceShareholderNonResident> listShareHolder) {
		int count = form15FilingDetailsDAO.batchUpdateInvoiceShareHolder(listShareHolder);
		logger.info("Updated Share holder Record {}");
		return count;
	}

	public Form15CAPartDDTO generate15CAPartDDto(InvoiceShareholderNonResident nonResidentShareholder, String tanNumber,
			String deductorPan, String tenantId, List<Errors> errorList) {

		String errorMsg = "";
		Form15CAPartDDTO form15CAPartDDTO = new Form15CAPartDDTO();
		form15CAPartDDTO.setId(nonResidentShareholder.getId());
		form15CAPartDDTO.setFolioNo(nonResidentShareholder.getFolioNumber());

		// RemitterDetls & RemitterAddrs
		populateDeductorMasterData(form15CAPartDDTO, deductorPan, tenantId);

		errorMsg = errorMsg + populateDeductorTanAddress(form15CAPartDDTO, deductorPan, tanNumber, tenantId);
		form15CAPartDDTO.setRemitterResidentialStatus(checkResidentialStatus(form15CAPartDDTO.getRemitterStatus(),
				form15CAPartDDTO.getRemitterCountry(), form15CAPartDDTO.getRemitterResidentialStatus()));

		// RemitteeDetls & RemitteeAddrs
		errorMsg = errorMsg + populateShareholderMasterData(form15CAPartDDTO, deductorPan,
				nonResidentShareholder.getShareholderId(), tenantId);
		form15CAPartDDTO.setShareholderName(!Objects.isNull(nonResidentShareholder.getShareholderName())
				? nonResidentShareholder.getShareholderName().toUpperCase()
				: "");
		form15CAPartDDTO.setShareholderPan(!Objects.isNull(nonResidentShareholder.getShareholderPan())
				? nonResidentShareholder.getShareholderPan().toUpperCase()
				: "");
		/*
		 * if (StringUtils.isNotBlank(nonResidentShareholder.getShareholderType())) {
		 * form15CAPartDDTO.setShareholderStatus(checkRemitteeStatus(
		 * nonResidentShareholder.getShareholderType())); } else { errorMsg = errorMsg +
		 * "Shareolder Type Is Mandatory To Generate Form 15 CA Part C" + "\n"; } if
		 * (StringUtils.isNotBlank(nonResidentShareholder.getPrincipalPlaceOfBusiness())
		 * ) { form15CAPartDDTO.setShareholderPrincipalPlaceOfBusiness(
		 * nonResidentShareholder.getPrincipalPlaceOfBusiness().toUpperCase()); } else {
		 * errorMsg = errorMsg +
		 * "Principal place of business Is Mandatory To Generate Form 15 CA Part C" +
		 * "\n"; }
		 */

		// RemittanceDetls
		// in populateDeductorTanAddress
		if (nonResidentShareholder.getProposedDateOfRemmitence() != null) {
			form15CAPartDDTO.setProposedDateOfRemittance(nonResidentShareholder.getProposedDateOfRemmitence());
		} else {
			form15CAPartDDTO.setProposedDateOfRemittance(new Date());
		}
		form15CAPartDDTO.setRemittanceCountry(nonResidentShareholder.getRemittanceMadeCountry());
		Country country = populateMasterData(form15CAPartDDTO, nonResidentShareholder.getRemittanceMadeCountry(),
				nonResidentShareholder.getCountry());
		form15CAPartDDTO.setAmountPayableInForeignCurrency(nonResidentShareholder.getDividendAmountForeignCurrency());
		form15CAPartDDTO.setAmountPayableInInr(nonResidentShareholder.getDividendAmountRs());
		form15CAPartDDTO.setPruposedRBICode(checkCategory(nonResidentShareholder.getShareholderCategory()));
		form15CAPartDDTO.setNatureOfRemittance("Dividend Income");
		if (StringUtils.isBlank(errorMsg)) {
			return form15CAPartDDTO;
		} else {
			Stream.of(errorMsg.split("\n")).forEach(n -> {
				Errors error = new Errors();
				error.setCompleteMessage(nonResidentShareholder.getId() + "");
				error.setId(nonResidentShareholder.getFolioNumber());
				error.setMessage(n);
				errorList.add(error);
			});
			return null;
		}
	}
}