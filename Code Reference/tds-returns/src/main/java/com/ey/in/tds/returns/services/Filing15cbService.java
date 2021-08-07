package com.ey.in.tds.returns.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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

import javax.management.openmbean.InvalidKeyException;

import org.apache.commons.lang.StringUtils;
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
import com.ey.in.tds.common.dto.dividend.Form15FilingDetailsDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterNonResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorTanAddress;
import com.ey.in.tds.common.onboarding.response.dto.DeductorOnboardingInformationResponseDTO;
import com.ey.in.tds.common.returns.jdbc.dto.Form15FilingDetails;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.ReturnType;
import com.ey.in.tds.dividend.forms.Form15Generator;
import com.ey.in.tds.dividend.forms.builder.Generatable;
import com.ey.in.tds.dividend.forms.builder.cb.RemitterBuilder;
import com.ey.in.tds.dividend.forms.builder.cb.TDSRateType;
import com.ey.in.tds.feign.client.IngestionClient;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.jdbc.dao.Form15FilingDetailsDAO;
import com.ey.in.tds.returns.dividend.validator.Errors;
import com.ey.in.tds.returns.dividend.validator.Validator;
import com.ey.in.tds.returns.dto.Form15CBDTO;
import com.ey.in.tds.returns.dto.Form15XmlUtil;
import com.microsoft.azure.storage.StorageException;

@Service
public class Filing15cbService {

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	private IngestionClient ingestionClient;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	protected BlobStorage blobStorage;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private Form15FilingDetailsDAO form15FilingDetailsDAO;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Value("${report.io.dir.path}")
	private String basePath;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private DividendFilingCommonErrorFileService dividendFilingCommonErrorFileService;

	@Transactional
	public String generate15CBExcelFilingReport(String deductorTan, String deductorPan, String dateOfPosting,
			String tenantId, String userName, Integer assessmentYear) throws Exception {

		logger.info("Filing 15CB Excel: Started generation for TAN: {}, date of posting : {} ", deductorTan,
				dateOfPosting);

		createFilingReportStatus(deductorTan, assessmentYear, dateOfPosting, Form15FileType.CB, Form15FileFormat.EXCEL,
				ReturnType.REGULAR, userName);

		try {
			checkOnboardingInformation(deductorPan, tenantId);
		} catch (Exception e) {
			logger.error("Error occured while Generating Form 15CB excel", e);
			updateFilingReportStatus(deductorTan, assessmentYear, dateOfPosting, Form15FileType.CB,
					Form15FileFormat.EXCEL, ReturnType.REGULAR, Form15FilingStatus.ERROR, null, e.getMessage(),
					userName);
		}

		CompletableFuture.runAsync(() -> this.create15CBExcelFilingReport(deductorTan, deductorPan, dateOfPosting,
				tenantId, userName, assessmentYear));

		return "Request for generating Form 15CB Excel submitted successfully";
	}

	public Form15FilingDetails createFilingReportStatus(String deductorTan, Integer assessmentYear,
			String dateOfPosting, Form15FileType fileType, Form15FileFormat fileFormat, ReturnType returnType,
			String userName) {

		List<Form15FilingDetails> existingFilings = form15FilingDetailsDAO.findFilingFiles(deductorTan, assessmentYear,
				dateOfPosting, fileType, fileFormat);
		Form15FilingDetails filingReportStatus = existingFilings.isEmpty() ? null : existingFilings.get(0);

		Date today = new Date();
		if (filingReportStatus == null) {
			logger.info("creating new filing record {}");
			filingReportStatus = new Form15FilingDetails();
			filingReportStatus.setAssessmentYear(assessmentYear);
			filingReportStatus.setDateOfPosting(dateOfPosting);
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
			return form15FilingDetailsDAO.save(filingReportStatus);
		} else {
			filingReportStatus.setFilingFilesDeductorTan(deductorTan);
			filingReportStatus.setActive(true);
			filingReportStatus.setStringFilingType(returnType.name());
			filingReportStatus.setStringFileType(fileType.name());
			filingReportStatus.setStringFileFormat(fileFormat.name());
			filingReportStatus.setStringFileStatus(Form15FilingStatus.PROCESSING.name());
			filingReportStatus.setStatus(Form15FilingStatus.PROCESSING);
			filingReportStatus.setIsRequested(true);
			filingReportStatus.setCreatedBy(userName);
			filingReportStatus.setCreatedDate(today);
			logger.info("Updating existing filing record to Processing {}");
			return form15FilingDetailsDAO.update(filingReportStatus);
		}
	}

	public void checkOnboardingInformation(String deductorPan, String tenantId) {
		Boolean onboardingCondition = false;
		Map<String, String> requestParams = new HashMap<>();
		requestParams.put("pan", deductorPan);
		DeductorOnboardingInformationResponseDTO deductorOnboardingInformation = null;
		ResponseEntity<ApiStatus<Optional<DeductorOnboardingInformationResponseDTO>>> deductorOnboardingInfo = onboardingClient
				.getDeductorOnboardingInfo(requestParams, tenantId);
		Optional<DeductorOnboardingInformationResponseDTO> onboardingInformation = deductorOnboardingInfo.getBody()
				.getData();
		if (onboardingInformation.isPresent()) {
			deductorOnboardingInformation = onboardingInformation.get();
			onboardingCondition = deductorOnboardingInformation.getDvndPrepForm15CaCb()
					.equals(DividendOnboardingInfoMetaData.PrepForm15CaCb.ONLY_PART_C_OF_FORM_15_CA);
		}
		if (onboardingCondition.equals(Boolean.FALSE))
			throw new CustomException("Deductor Onboarding information is not present for 15 CB generation");
	}

	private void create15CBExcelFilingReport(String deductorTan, String deductorPan, String dateOfPosting,
			String tenantId, String userName, Integer assesmentYear) {
		logger.info("Entered into async method to generate the 15CB excel file {}");
		MultiTenantContext.setTenantId(tenantId);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.executeWithoutResult((status) -> {
			try {
				List<Errors> errorList = new ArrayList<>();
				List<Form15CBDTO> form15CBDTOList = generate15cbDtos(deductorTan, deductorPan, dateOfPosting, tenantId,
						userName, assesmentYear, errorList);

				Resource resource = resourceLoader.getResource("classpath:templates/" + "Form15CB.xlsx");

				try (InputStream input = resource.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(input)) {
					if (!form15CBDTOList.isEmpty()) {
						XSSFSheet worksheet = workbook.getSheetAt(0);
						ResponseEntity<ApiStatus<DeductorMasterDTO>> response = onboardingClient
								.getDeductorByPan(tenantId, deductorPan);
						DeductorMasterDTO deductorMasterDTO = response.getBody().getData();
						if (deductorMasterDTO != null) {
							XSSFRow row = worksheet.createRow(1);
							acceptDeductorData(deductorMasterDTO, row);
						} else {
							throw new CustomException("deductor not found for given pan number",
									HttpStatus.BAD_REQUEST);
						}

						int totalRows = 5;
						for (Form15CBDTO record : form15CBDTOList) {
							XSSFRow row = worksheet.createRow(totalRows);
							accept15CBData(record, row);
							totalRows++;
						}
						logger.debug("File Writing End");

						File file = new File("Form15CB" + new Date().getTime() + ".xlsx");
						OutputStream out = new FileOutputStream(file);
						workbook.write(out);
						out.close();

						File zipFile = new File("FORM 15 CB" + System.currentTimeMillis() + ".zip");
						FileOutputStream outputStream = new FileOutputStream(zipFile);
						ZipOutputStream zos = new ZipOutputStream(outputStream);
						zos.putNextEntry(new ZipEntry(file.getName()));
						byte[] bytes1 = Files.readAllBytes(file.toPath());
						zos.write(bytes1, 0, bytes1.length);
						zos.closeEntry();

						// generating error file
						if (!errorList.isEmpty()) {
							File errorFile = dividendFilingCommonErrorFileService.generateErrorFile(errorList);
							zos.putNextEntry(new ZipEntry(errorFile.getName()));
							byte[] bytes2 = Files.readAllBytes(errorFile.toPath());
							zos.write(bytes2, 0, bytes2.length);
							zos.closeEntry();
						}

						zos.close();
						outputStream.close();

						String uri = null;
						try {
							uri = blobStorage.uploadExcelToBlobWithFile(zipFile, tenantId);
							updateFilingReportStatus(deductorTan, assesmentYear, dateOfPosting, Form15FileType.CB,
									Form15FileFormat.EXCEL, ReturnType.REGULAR, Form15FilingStatus.GENERATED, uri, "",
									userName);
						} catch (IOException | URISyntaxException | InvalidKeyException | StorageException e1) {
							throw new RuntimeException(e1);
						}
					} else {
						File errorFile = dividendFilingCommonErrorFileService.generateErrorFile(errorList);
						String url = blobStorage.uploadExcelToBlobWithFile(errorFile, tenantId);
						updateFilingReportStatus(deductorTan, assesmentYear, dateOfPosting, Form15FileType.CB,
								Form15FileFormat.EXCEL, ReturnType.REGULAR, Form15FilingStatus.ERROR, url, "ERROR",
								userName);
					}

				} catch (IOException ex) {
					logger.info("Exception occured while creating file " + ex);
					throw new RuntimeException(ex);
				}
			} catch (Exception e) {
				logger.error("Error occured while Generating Form 15CB excel", e);
				updateFilingReportStatus(deductorTan, assesmentYear, dateOfPosting, Form15FileType.CB,
						Form15FileFormat.EXCEL, ReturnType.REGULAR, Form15FilingStatus.ERROR, null, e.getMessage(),
						userName);
			}
		});
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
			logger.info("Updating filing record with status as " + filingStatus.name());
			filingReportStatus.setStringFileStatus(filingStatus.name());
			filingReportStatus.setFileUrl(fileBlobUrl);
			filingReportStatus.setError(errorDescription);
			filingReportStatus.setUpdatedBy(userName);
			filingReportStatus.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
			if (filingStatus.isGenerated()) {
				filingReportStatus.setGeneratedDate(new Timestamp(System.currentTimeMillis()));
			}
			filingReportStatus = form15FilingDetailsDAO.update(filingReportStatus);
		} catch (Exception e) {
			logger.info("Exception occured while updating filing table  {}");
		}
		return filingReportStatus;
	}

	private void accept15CBData(Form15CBDTO record, XSSFRow row) {

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

		row.createCell(0).setCellValue(StringUtils.isBlank(record.getRemitterName()) ? " " : record.getRemitterName());
		row.createCell(1).setCellValue(StringUtils.isBlank(record.getRemitterPan()) ? " " : record.getRemitterPan());
		row.createCell(2).setCellValue(
				StringUtils.isBlank(record.getShareholderSalutation()) ? " " : record.getShareholderSalutation());
		row.createCell(3)
				.setCellValue(StringUtils.isBlank(record.getShareholderName()) ? " " : record.getShareholderName());
		row.createCell(4)
				.setCellValue(StringUtils.isBlank(record.getFlatDoorBlockNo()) ? " " : record.getFlatDoorBlockNo());
		row.createCell(5).setCellValue(
				StringUtils.isBlank(record.getNameBuildingVillage()) ? " " : record.getNameBuildingVillage());
		row.createCell(6).setCellValue(
				StringUtils.isBlank(record.getRoadStreetPostoffice()) ? " " : record.getRoadStreetPostoffice());
		row.createCell(7).setCellValue(StringUtils.isBlank(record.getAreaLocality()) ? " " : record.getAreaLocality());
		row.createCell(8)
				.setCellValue(StringUtils.isBlank(record.getTownCityDistrict()) ? " " : record.getTownCityDistrict());
		row.createCell(9).setCellValue(StringUtils.isBlank(record.getState()) ? " " : record.getState());
		row.createCell(10).setCellValue(StringUtils.isBlank(record.getCountry()) ? " " : record.getCountry());
		row.createCell(11).setCellValue(StringUtils.isBlank(record.getPinCode()) ? " " : record.getPinCode());
		row.createCell(12)
				.setCellValue(StringUtils.isBlank(record.getRemittanceCountry()) ? " " : record.getRemittanceCountry());
		// row.createCell(13).setCellValue(StringUtils.isBlank(record.getCurrency()) ? "
		// " : record.getCurrency());
		// TODO hrdcoded for now, dynamic vlue to be assigned
		row.createCell(13).setCellValue(record.getCurrency());
		row.createCell(14).setCellValue(StringUtils.isBlank(record.getAmountPayableInForeignCurrency()) ? "0.0"
				: record.getAmountPayableInForeignCurrency());
		row.createCell(15).setCellValue(checkBigdecimal(record.getAmountPayableInInr()));
		row.createCell(16).setCellValue(StringUtils.isBlank(record.getNameOfBank()) ? " " : record.getNameOfBank());
		row.createCell(17).setCellValue(StringUtils.isBlank(record.getBranchOfBank()) ? " " : record.getBranchOfBank());
		row.createCell(18).setCellValue(
				StringUtils.isBlank(record.getBsrCodeOfBankBranch()) ? " " : record.getBsrCodeOfBankBranch());
		row.createCell(19).setCellValue(Objects.isNull(record.getProposedDateOfRemittance()) ? " "
				: formatter.format(record.getProposedDateOfRemittance()));
		row.createCell(20).setCellValue(
				StringUtils.isBlank(record.getNatureOfRemittance()) ? " " : record.getNatureOfRemittance());
		row.createCell(21)
				.setCellValue(StringUtils.isBlank(record.getRbiPurposeCode()) ? " " : record.getRbiPurposeCode());
		row.createCell(22).setCellValue(checkBoolean(record.getTaxPayableGrossedUp()));
		row.createCell(23).setCellValue(checkBoolean(record.getRemittanceChargeableToIndiaTax()));
		row.createCell(24).setCellValue(""); // NA
		// row.createCell(25).setCellValue(
		// StringUtils.isBlank(record.getRemittanceSectionOfAct()) ? " " :
		// record.getRemittanceSectionOfAct());
		// hardcode as per palak instructions
		row.createCell(25).setCellValue("115A");
		row.createCell(26).setCellValue(checkBigdecimal(record.getIncomeChargeableToTax()));
		row.createCell(27).setCellValue(record.getTaxLiability() == null ? "0.0" : record.getTaxLiability().toString()); // Tax
																															// Liability
																															// from
																															// challan
		row.createCell(28).setCellValue("AS PER INCOME-TAX ACT");
		row.createCell(29).setCellValue(checkBoolean(record.getRemittanceRecipientTRCAvailable()));
		row.createCell(30).setCellValue(StringUtils.isBlank(record.getRelevantDTAA()) ? " " : record.getRelevantDTAA());
		row.createCell(31).setCellValue(
				StringUtils.isBlank(record.getRelevantDTAAArticle()) ? " " : record.getRelevantDTAAArticle());
		row.createCell(32).setCellValue(checkBigdecimal(record.getTaxableIncomePerDTAA()));
		row.createCell(33).setCellValue(checkBigdecimal(record.getTaxLiabilityAsPerDTAA()));
		row.createCell(34).setCellValue(checkBoolean(record.getRemittanceForRoyalties()));
		row.createCell(35).setCellValue(StringUtils.isBlank(record.getdTAAArticle()) ? " " : record.getdTAAArticle());
		row.createCell(36).setCellValue(checkBigdecimal(record.getTdsRatePerDTAA()));
		row.createCell(37).setCellValue("No"); // NA
		row.createCell(38).setCellValue(""); // NA
		row.createCell(39).setCellValue(""); // NA
		row.createCell(40).setCellValue(""); // NA
		row.createCell(41).setCellValue("No"); // NA
		row.createCell(42).setCellValue(""); // NA
		row.createCell(43).setCellValue(""); // NA
		row.createCell(44).setCellValue(""); // NA
		row.createCell(45).setCellValue("No"); // NA
		row.createCell(46).setCellValue(""); // NA
		row.createCell(47).setCellValue(""); // NA
		row.createCell(48).setCellValue(""); // NA
		row.createCell(49).setCellValue("NA"); // NA
		row.createCell(50).setCellValue(checkBigdecimal(record.getTdsAmountInForeignCurrency()));
		row.createCell(51).setCellValue(checkBigdecimal(record.getTdsAmountInInr()));
		row.createCell(52)
				.setCellValue(StringUtils.isEmpty(record.getBasisOfIncomeAndTaxLiability()) ? ""
						: (record.getBasisOfIncomeAndTaxLiability().contains("DTAA") ? "AS PER DTAA"
								: "AS PER INCOME-TAX ACT"));
		row.createCell(53).setCellValue(checkBigdecimal(record.getTdsRate()));
		row.createCell(54)
				.setCellValue(StringUtils.isBlank(record.getActualRemittanceAmountAfterTdsInForeignCurrency()) ? " "
						: record.getActualRemittanceAmountAfterTdsInForeignCurrency());
		row.createCell(55).setCellValue(Objects.isNull(record.getTaxAtSourceDeductionDate()) ? " "
				: formatter.format(record.getTaxAtSourceDeductionDate())); // Challan
		row.createCell(56)
				.setCellValue(StringUtils.isBlank(record.getAccountantName()) ? " " : record.getAccountantName());
		row.createCell(57).setCellValue(
				StringUtils.isBlank(record.getCaNameOfProprietorship()) ? " " : record.getCaNameOfProprietorship());
		row.createCell(58).setCellValue(StringUtils.isBlank(record.getCaAddress()) ? " " : record.getCaAddress());
		row.createCell(59).setCellValue(
				StringUtils.isBlank(record.getCaMembershipNumber()) ? " " : record.getCaMembershipNumber());
		row.createCell(60).setCellValue(
				StringUtils.isBlank(record.getCaRegistrationNumber()) ? " " : record.getCaRegistrationNumber());
		row.createCell(61).setCellValue(record.getId());
		row.createCell(62).setCellValue(record.getShareHolderId());
	}

	private String checkBigdecimal(BigDecimal value) {
		if (Objects.isNull(value)) {
			return " ";
		} else {
			return value.toString();
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

	private void acceptDeductorData(DeductorMasterDTO deductorMasterDTO, XSSFRow row) {
		row.createCell(0).setCellValue("We"); // Salutation of CA certified the Form
		row.createCell(1).setCellValue("M/s"); // Salutation of Deductor
		row.createCell(2).setCellValue(
				StringUtils.isBlank(deductorMasterDTO.getDeductorName()) ? " " : deductorMasterDTO.getDeductorName());
		row.createCell(3)
				.setCellValue(StringUtils.isBlank(deductorMasterDTO.getPan()) ? " " : deductorMasterDTO.getPan());
	}

	public List<Form15CBDTO> generate15cbDtos(String tanNumber, String deductorPan, String dateOfPosting,
			String tenantId, String userName, Integer assessmentYear, List<Errors> errorList) {
		logger.info("Preparing 15CB DTO {}");
		List<Form15CBDTO> form15CBDTOS = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate dateTime = LocalDate.parse(dateOfPosting, formatter);
		List<InvoiceShareholderNonResident> nonResidentShareholders = form15FilingDetailsDAO
				.findAllNonResidentByDateOfPosting(deductorPan, dateTime, assessmentYear);
		logger.info("Feign call succeeded to get invoice share holder records{}");
		for (InvoiceShareholderNonResident nonResidentShareholder : nonResidentShareholders) {
			if (check15CACBFlag(nonResidentShareholder, tenantId, deductorPan).equalsIgnoreCase("Yes")) {
				String errorMsg = "";
				boolean isDividendIncomeMoreThan5Lakh = (nonResidentShareholder.getDividendAmountRs()
						.compareTo(BigDecimal.valueOf(500000))) > 0;
				boolean isLdcApplied = false;
				boolean isTreatyApplied = false;
				boolean isActRateApplied = false;
				if (!Objects.isNull(nonResidentShareholder.getWithholdingDetails())) {
					NonResidentWithholdingDetails withholdingDetails = nonResidentShareholder.getWithholdingDetails();
					isLdcApplied = checkBooleanForNull(withholdingDetails.isLdcApplied());
					isTreatyApplied = checkBooleanForNull(withholdingDetails.isTreatyApplied());
					isActRateApplied = checkBooleanForNull(withholdingDetails.isActApplied());
				}
// Conditions
				if (isDividendIncomeMoreThan5Lakh && ((!isLdcApplied && isActRateApplied)
						|| (!isLdcApplied && isTreatyApplied) || (isLdcApplied && isTreatyApplied))) {
					// more tan 5 and act is applied
					// treaty
					// partial treaty and partial ldc

					Form15CBDTO form15CBDTO = new Form15CBDTO();
					form15CBDTO.setId(nonResidentShareholder.getId());
					form15CBDTO.setFolioNo(nonResidentShareholder.getFolioNumber());

// RemitterDetails
					populateDeductorMasterData(form15CBDTO, deductorPan, tenantId);
					if (StringUtils.isNotBlank(nonResidentShareholder.getSalutation())) {
						form15CBDTO.setShareholderSalutation(nonResidentShareholder.getSalutation());
					} else {
						errorMsg = errorMsg + "Salutation  is a Mandatory field. It should not be blank." + "\n";
					}

//RemitteeDetls and RemitteeAddrs
					errorMsg = errorMsg + populateShareholderMasterDate(form15CBDTO, deductorPan, tenantId,
							nonResidentShareholder.getShareholderId());

//RemittanceDetails
					if (StringUtils.isNotBlank(nonResidentShareholder.getRemittanceMadeCountry())) {

						form15CBDTO.setRemittanceCountry(nonResidentShareholder.getRemittanceMadeCountry());
					} else {
						errorMsg = errorMsg
								+ "Country to which remittance is made Is Required To Generate Form 15 CB Generation"
								+ "\n";
					}

					Country country = populateMasterData(form15CBDTO, nonResidentShareholder.getRemittanceMadeCountry(),
							nonResidentShareholder.getCountry());
					if (nonResidentShareholder.getDividendAmountForeignCurrency() != null) {
						form15CBDTO.setAmountPayableInForeignCurrency(
								nonResidentShareholder.getDividendAmountForeignCurrency());
					} else {
						errorMsg = errorMsg
								+ "Amount of Dividend (Foreign Currency) Is Required To Generate Form 15 CB Generation"
								+ "\n";
					}
					form15CBDTO.setAmountPayableInInr(nonResidentShareholder.getDividendAmountRs());

					errorMsg = errorMsg + populateDeductorTanAddress(form15CBDTO, deductorPan, tanNumber, tenantId);

					if (nonResidentShareholder.getProposedDateOfRemmitence() != null
							&& !nonResidentShareholder.getProposedDateOfRemmitence().before(new Date())) {
						form15CBDTO.setProposedDateOfRemittance(nonResidentShareholder.getProposedDateOfRemmitence());
					} else {
						form15CBDTO.setProposedDateOfRemittance(new Date());
					}
					form15CBDTO.setRbiPurposeCode(checkCategory(nonResidentShareholder.getShareholderCategory()));

//    ItActDetails
					form15CBDTO.setIncomeChargeableToTax(nonResidentShareholder.getDividendAmountRs());
					form15CBDTO.setTaxLiabilityAsPerDTAA(nonResidentShareholder.getFinalDividendWithholding());

					BigDecimal calculatedactAmount = nonResidentShareholder.getWithholdingDetails().getActSummary()
							.getWithholding().add(nonResidentShareholder.getCessWithholding())
							.add(nonResidentShareholder.getSurchargeWithholding());
					form15CBDTO.setTaxLiability(calculatedactAmount.setScale(2, RoundingMode.UP));

					if (isActRateApplied) {
						form15CBDTO.setBasisOfIncomeAndTaxLiability("AS PER INCOME-TAX ACT");
					} else {
						form15CBDTO.setBasisOfIncomeAndTaxLiability("AS PER DTAA");
					}
					if (isActRateApplied && isTreatyApplied) {
						TreatySummary treatySummary = nonResidentShareholder.getWithholdingDetails().getTreatySummary();
						ActSummary actSummary = nonResidentShareholder.getWithholdingDetails().getActSummary();
						if (!Objects.isNull(treatySummary.getRate()) && !Objects.isNull(actSummary.getActRate())) {
							if (treatySummary.getRate().compareTo(actSummary.getActRate()) > 0) {
								form15CBDTO.setBasisOfIncomeAndTaxLiability("AS PER INCOME-TAX ACT");
							} else {
								form15CBDTO.setBasisOfIncomeAndTaxLiability("AS PER DTAA");
							}
						}
					}

//    DTAADetails
					form15CBDTO.setRemittanceRecipientTRCAvailable(nonResidentShareholder.getIsTrcAvailable());

					if (isTreatyApplied) {
						form15CBDTO.setRelevantDTAA(!Objects.isNull(nonResidentShareholder.getCountry())
								? "India-" + nonResidentShareholder.getCountry() + " DTAA"
								: "");
						form15CBDTO.setRelevantDTAAArticle("ARTICLE "
								+ nonResidentShareholder.getWithholdingDetails().getTreatySummary().getTaxTreatyClause()
								+ " OF DTAA WITH "
								+ (!Objects.isNull(nonResidentShareholder.getCountry())
										? nonResidentShareholder.getCountry().toUpperCase()
										: ""));
						form15CBDTO.setTaxableIncomePerDTAA(nonResidentShareholder.getDividendAmountRs());
						form15CBDTO.setdTAAArticle(
								nonResidentShareholder.getWithholdingDetails().getTreatySummary().getTaxTreatyClause());
						form15CBDTO.setTdsRatePerDTAA(
								nonResidentShareholder.getWithholdingDetails().getTreatySummary().getRate());
						form15CBDTO.setRemittanceForRoyalties(true);
					} else {
//find treaty records
						if (!Objects.isNull(country)) {
							ResponseEntity<ApiStatus<DividendRateTreaty>> treatyBenefit = mastersClient
									.getTreatyBenefit(tenantId, country);
							DividendRateTreaty rateTreaty = treatyBenefit.getBody().getData();
							if (!Objects.isNull(rateTreaty)) {
								form15CBDTO.setTdsRatePerDTAA(rateTreaty.getMfnNotAvailedCompanyTaxRate());
								form15CBDTO.setdTAAArticle("ARTICLE " + rateTreaty.getTaxTreatyClause());
							} else {
								form15CBDTO.setTdsRatePerDTAA(new BigDecimal(0));
							}
						} else {
							form15CBDTO.setTdsRatePerDTAA(new BigDecimal(0));
						}
					}

//  TDSDetails
					BigDecimal dividendAmountInForeignCurrency = Form15CBXmlGenerationService
							.stringAmountToBigDecimal(nonResidentShareholder.getDividendAmountForeignCurrency());
					BigDecimal dividendAmountForeignCurrency = dividendAmountInForeignCurrency
							.multiply(nonResidentShareholder.getTdsRate()).divide(BigDecimal.valueOf(100));
					form15CBDTO.setTdsAmountInForeignCurrency(
							dividendAmountForeignCurrency.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
									: dividendAmountForeignCurrency);

//check if client overridden withholding is not null
					if (!Objects.isNull(nonResidentShareholder.getClientOverriddenWithholding())
							&& nonResidentShareholder.getClientOverriddenWithholding().compareTo(BigDecimal.ZERO) > 0) {
						form15CBDTO.setTdsAmountInInr(nonResidentShareholder.getClientOverriddenWithholding());
					} else {
						form15CBDTO.setTdsAmountInInr(nonResidentShareholder.getFinalDividendWithholding());
					}
					if (isActRateApplied) {
						form15CBDTO.setTdsRateType(TDSRateType.RATE_AS_PER_ACT);
					} else {
						form15CBDTO.setTdsRateType(TDSRateType.RATE_AS_PER_TREATY);
					}
					if (isActRateApplied && isTreatyApplied) {
						ActSummary actSummary = nonResidentShareholder.getWithholdingDetails().getActSummary();
						TreatySummary treatySummary = nonResidentShareholder.getWithholdingDetails().getTreatySummary();
						if (!Objects.isNull(actSummary.getActRate()) && !Objects.isNull(treatySummary.getRate())) {
							boolean statusCheck = (actSummary.getActRate().compareTo(treatySummary.getRate())) > 0; // act
																													// rate
																													// greater
																													// than
																													// treaty
																													// rate
							if (statusCheck) {
								form15CBDTO.setTdsRateType(TDSRateType.RATE_AS_PER_TREATY);
							} else {
								form15CBDTO.setTdsRateType(TDSRateType.RATE_AS_PER_ACT);
							}
						}
					}

//check if client overridden rate is not null
					if (!Objects.isNull(nonResidentShareholder.getClientOverriddenRate())
							&& nonResidentShareholder.getClientOverriddenRate().compareTo(BigDecimal.ZERO) > 0) {
						form15CBDTO.setTdsRate(nonResidentShareholder.getClientOverriddenRate());
					} else {
						form15CBDTO.setTdsRate(nonResidentShareholder.getTdsRate());
					}
					form15CBDTO.setActualRemittanceAmountAfterTdsInForeignCurrency(
							nonResidentShareholder.getActualRemmitanceAmountForeignCurrency());
					form15CBDTO.setTaxAtSourceDeductionDate(nonResidentShareholder.getDateOfPosting()); // Hardcoded
																										// Depends
																										// upon
																										// challan

//  AcctntDetls in populateDeductorTanAddress

					form15CBDTO.setTaxPayableGrossedUp(nonResidentShareholder.getTaxPayableGrossedUp());
					form15CBDTO.setNatureOfRemittance("Dividend income"); // Hardcoded
					form15CBDTO.setRemittanceChargeableToIndiaTax(true);

					// hardcoded as per palak instruction
					form15CBDTO.setRemittanceSectionOfAct("115A"); // nonResidentShareholder.getTdsSection()
					form15CBDTO.setCurrency("INR");
					if (!isTreatyApplied) {
						form15CBDTO.setTaxLiabilityAsPerDTAA(null);
					}
					if (form15CBDTO.getRemittanceForRoyalties() == null || !form15CBDTO.getRemittanceForRoyalties()) {
						form15CBDTO.setTdsRatePerDTAA(null);
						form15CBDTO.setdTAAArticle("");
					}
					if (form15CBDTO.getBasisOfIncomeAndTaxLiability().equals("AS PER DTAA")) {
						if (StringUtils.isNotBlank(nonResidentShareholder.getTreatyActSummery())) {
							BigDecimal taxLiaility = new JSONObject(nonResidentShareholder.getTreatyActSummery())
									.getJSONObject("Summary").getBigDecimal("total_witholding");
							form15CBDTO.setTaxLiability(taxLiaility);
						}
					}
					if (StringUtils.isBlank(errorMsg)) {
						form15CBDTOS.add(form15CBDTO);
					} else {
						Stream.of(errorMsg.split("\n")).forEach(n -> {
							Errors error = new Errors();
							error.setCompleteMessage(nonResidentShareholder.getId() + "");
							error.setId(nonResidentShareholder.getFolioNumber());
							error.setMessage(n);
							errorList.add(error);
						});

					}
				}
			} else {
				Errors error = new Errors();
				error.setCompleteMessage(nonResidentShareholder.getId() + "");
				error.setId(nonResidentShareholder.getFolioNumber());
				error.setMessage("Form 15 CA/CB Is Not Applicable ");
				errorList.add(error);
			}
		}

		if (errorList.isEmpty() && form15CBDTOS.isEmpty()) {
			Errors error = new Errors();
			error.setMessage("No Shareholder Found For Deductor Pan=" + deductorPan + " ,Financial Year="
					+ assessmentYear + " And Date Of Posting=" + dateTime);
			errorList.add(error);
		}
		logger.info("Prepared 15CB DTO' succesfully {}");

		// merging the records based on pan
		Map<String, Form15CBDTO> map = new HashMap<>();
		if (!form15CBDTOS.isEmpty()) {
			form15CBDTOS.stream().forEach(n -> {
				if (map.containsKey(n.getShareHolderPan().trim())) {
					Form15CBDTO dto = map.get(n.getShareHolderPan().trim());
					n.setAmountPayableInInr(n.getAmountPayableInInr().add(dto.getAmountPayableInInr()));
					n.setIncomeChargeableToTax(n.getIncomeChargeableToTax().add(dto.getIncomeChargeableToTax()));
					n.setTaxLiability(n.getTaxLiability().add(dto.getTaxLiability()));
					map.put(n.getShareHolderPan().trim(), n);
				} else {
					map.put(n.getShareHolderPan().trim(), n);
				}
			});
		}
		if (!map.isEmpty()) {
			form15CBDTOS.clear();
			map.entrySet().stream().forEach(n -> form15CBDTOS.add(n.getValue()));
		}
		return form15CBDTOS;
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

	private boolean checkBooleanForNull(Boolean value) {
		if (Objects.isNull(value)) {
			return false;
		} else {
			return value;
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

	private void populateDeductorMasterData(Form15CBDTO form15CBDTO, String deductorPan, String tenantId) {
		ResponseEntity<ApiStatus<DeductorMasterDTO>> response = onboardingClient.getDeductorByPan(tenantId,
				deductorPan);
		DeductorMasterDTO deductorMasterDTO = Objects.requireNonNull(response.getBody()).getData();
		if (deductorMasterDTO != null) {
			form15CBDTO.setRemitterName(!Objects.isNull(deductorMasterDTO.getDeductorName())
					? deductorMasterDTO.getDeductorName().toUpperCase()
					: "");
			form15CBDTO.setRemitterPan(
					!Objects.isNull(deductorMasterDTO.getPan()) ? deductorMasterDTO.getPan().toUpperCase() : "");
		} else {
			throw new CustomException("deductor not found for given pan number", HttpStatus.BAD_REQUEST);
		}
	}

	private String populateShareholderMasterDate(Form15CBDTO form15CBDTO, String deductorPan, String tenantId,
			Integer id) {
		ResponseEntity<ApiStatus<ShareholderMasterNonResidential>> shareholderNonResident = null;
		if (!Objects.isNull(id)) {
			shareholderNonResident = onboardingClient.getNonResidentialShareholderById(tenantId, deductorPan, id);
		} else {
			throw new CustomException("Shareholder not valid", HttpStatus.BAD_REQUEST);
		}
		ShareholderMasterNonResidential data = Objects.requireNonNull(shareholderNonResident.getBody()).getData();
		String errorMsg = "";
		form15CBDTO.setShareHolderId(id);
		form15CBDTO.setShareholderName(
				!Objects.isNull(data.getShareholderName()) ? data.getShareholderName().toUpperCase() : "");
		if (StringUtils.isNotBlank(data.getFlatDoorBlockNo())) {
			form15CBDTO.setFlatDoorBlockNo(data.getFlatDoorBlockNo());
		} else {
			errorMsg = errorMsg + "ShareHolder Flat / Door / Building Is Mandatory, It Should Not Be Blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getAreaLocality())) {
			form15CBDTO.setAreaLocality(data.getAreaLocality());
		} else {
			errorMsg = errorMsg + "ShareHolder Area / Locality Is Mandatory, It Should Not Be Blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getTownCityDistrict())) {
			form15CBDTO.setTownCityDistrict(data.getTownCityDistrict());
		} else {
			errorMsg = errorMsg += "ShareHolder Town / City / District Is Mandatory, It Should Not Be Blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getState())) {
			form15CBDTO.setState(data.getState());
		} else {
			errorMsg = errorMsg += "ShareHolder State Is Mandatory, It Should Not Be Blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getCountry())) {
			form15CBDTO.setCountry(data.getCountry());
		} else {
			errorMsg = errorMsg += "ShareHolder Country Is Mandatory, It Should Not Be Blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getPinCode())) {
			form15CBDTO.setPinCode(data.getPinCode());
		} else {
			errorMsg = errorMsg += "ShareHolder Pin Code Is Mandatory, It Should Not Be Blank" + "\n";
		}
		form15CBDTO.setNameBuildingVillage(data.getNameBuildingVillage());
		form15CBDTO.setRoadStreetPostoffice(data.getRoadStreetPostoffice());
		form15CBDTO.setShareHolderPan(data.getShareholderPan());
		return errorMsg;
	}

	private Country populateMasterData(Form15CBDTO form15CBDTO, String remittanceMadeCountry, String countryName) {
		Country country1 = null;
		ResponseEntity<ApiStatus<List<Country>>> response = mastersClient.getCountries();
		List<Country> countries = Objects.requireNonNull(response.getBody()).getData();
		Optional<Country> country = countries.stream().filter(x -> x.getName().equalsIgnoreCase(remittanceMadeCountry))
				.findFirst();
		if (country.isPresent()) {
			form15CBDTO.setCurrency(
					!Objects.isNull(country.get().getCurrency()) ? country.get().getCurrency().toUpperCase() : "");
		}
		Optional<Country> optionalCountry = countries.stream().filter(x -> x.getName().equalsIgnoreCase(countryName))
				.findFirst();
		if (optionalCountry.isPresent()) {
			country1 = optionalCountry.get();
		}
		return country1;
	}

	private String populateDeductorTanAddress(Form15CBDTO form15CBDTO, String deductorPan, String tanNumber,
			String tenantId) {
		ResponseEntity<ApiStatus<DeductorTanAddress>> deductorTanAddress = onboardingClient
				.getDeductorTanAddressByPanTan(tanNumber, deductorPan, tenantId);
		DeductorTanAddress data = Objects.requireNonNull(deductorTanAddress.getBody()).getData();
		String errorMsg = "";
		if (StringUtils.isNotBlank(data.getDvndNameOfBank())) {
			form15CBDTO.setNameOfBank(data.getDvndNameOfBank().toUpperCase());
		} else {
			errorMsg = errorMsg + "Name of Bank is a Mandatory field. It should not be blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndBranchOfBank())) {
			form15CBDTO.setBranchOfBank(data.getDvndBranchOfBank().toUpperCase());
		} else {
			errorMsg = errorMsg + "Branch of the bank is a Mandatory field. It should not be blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndBsrCodeOfBankBranch())) {
			form15CBDTO.setBsrCodeOfBankBranch(data.getDvndBsrCodeOfBankBranch());
		} else {
			errorMsg = errorMsg + "BSR code of the bank branch (7 digit) is a Mandatory field. It should not be blank"
					+ "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndAccountantName())) {
			form15CBDTO.setAccountantName(data.getDvndAccountantName().toUpperCase());
		} else {
			errorMsg = errorMsg + "Accountant Name is a Mandatory field. It should not be blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndNameOfProprietorship())) {
			form15CBDTO.setCaNameOfProprietorship(data.getDvndNameOfProprietorship().toUpperCase());
		} else {
			errorMsg = errorMsg + "Name of proprietorship/firm is a Mandatory field. It should not be blank" + "\n";
		}
		String caAddress = checkStringForNull(data.getDvndFlatDoorBlockNo())
				+ checkStringForNull(data.getDvndNameOfPremisesBuildingVillage())
				+ checkStringForNull(data.getDvndRoadStreetPostOffice()) + checkStringForNull(data.getDvndState())
				+ checkStringForNull(data.getDvndPinCode());
		if (StringUtils.isNotBlank(caAddress)) {
			form15CBDTO.setCaAddress(caAddress);
		} else {
			errorMsg = errorMsg + "Chartered Accountant's Address is Mandatory" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndMembershipNumber())) {
			form15CBDTO.setCaMembershipNumber(data.getDvndMembershipNumber());
		} else {
			errorMsg = errorMsg + "Membership Number is Mandatory . It should not be blank" + "\n";
		}
		if (StringUtils.isNotBlank(data.getDvndRegistrationNumber())) {
			form15CBDTO.setCaRegistrationNumber(data.getDvndRegistrationNumber());
		} else {
			errorMsg = errorMsg + "Registration Number is Mandatory . It should not be blank" + "\n";
		}

		form15CBDTO.setAccountantFlatDoorBlockNo(data.getDvndFlatDoorBlockNo());
		form15CBDTO.setAccountantNameBuildingVillage(data.getDvndNameOfPremisesBuildingVillage());
		form15CBDTO.setAccountantRoadStreetPostoffice(data.getDvndRoadStreetPostOffice());
		form15CBDTO.setAccountantAreaLocality(data.getDvndAreaLocality());
		form15CBDTO.setAccountantTownCityDistrict(data.getDvndTownCityDistrict());
		form15CBDTO.setAccountantState(data.getDvndState());
		form15CBDTO.setAccountantCountry(data.getDvndCountry());
		form15CBDTO.setAccountantPinCode(data.getDvndPinCode());
		return errorMsg;
	}

	private String checkStringForNull(String value) {
		if (StringUtils.isBlank(value))
			return "";
		else {
			return value;
		}

	}

	public List<Form15FilingDetailsDTO> findFilingFiles(String deductorTan, Integer assessmentYear,
			String dateOfPosting, Form15FileType fileType, Form15FileFormat fileFormat) {
		return form15FilingDetailsDAO.findFilingFiles(deductorTan, assessmentYear, dateOfPosting, fileType, fileFormat)
				.stream().sorted((f1, f2) -> {
					if (f1.getGeneratedDate() != null && f2.getGeneratedDate() != null) {
						return f1.getGeneratedDate().compareTo(f2.getGeneratedDate());
					} else {
						return f1.getDateOfPosting().compareTo(f2.getDateOfPosting());
					}
				}).map(Form15FilingDetailsDTO::of).collect(Collectors.toList());
	}

	public String generateForm15CBFromExcel(MultipartFile file, String deductorTan, String deductorPan,
			String dateOfPosting, String tenantId, String userName, Integer assessmentYear, Form15FileFormat fileFormat)
			throws IOException {
		logger.info("Filing 15CB Excel: Started generation for TAN: {}, date of posting : {} ", deductorTan,
				dateOfPosting);

		createFilingReportStatus(deductorTan, assessmentYear, dateOfPosting, Form15FileType.CB, Form15FileFormat.EXCEL,
				ReturnType.REGULAR, userName);

		InputStream stream = file.getInputStream();
		logger.info("Filing record got created/updated {}");

		try {
			checkOnboardingInformation(deductorPan, tenantId);
			logger.info("Onboarding information checked {}");
		} catch (Exception e) {
			logger.error("Error occured while Generating Form 15CB excel", e);
			updateFilingReportStatus(deductorTan, assessmentYear, dateOfPosting, Form15FileType.CB,
					Form15FileFormat.EXCEL, ReturnType.REGULAR, Form15FilingStatus.ERROR, null, e.getMessage(),
					userName);
		}

		XSSFWorkbook workbook = new XSSFWorkbook(stream);
		XSSFSheet sheet = workbook.getSheetAt(0);
		if (sheet.getRow(4).getPhysicalNumberOfCells() != 63) {
			logger.info("Invalid filw with no of header" + sheet.getRow(0).getPhysicalNumberOfCells());
			throw new RuntimeException("Invalid 15 CB File");
		}

		CompletableFuture.runAsync(() -> process15CBExcelFile(workbook, deductorTan, deductorPan, dateOfPosting,
				tenantId, userName, assessmentYear, fileFormat));

		return "Request for generating Form 15CB Excel submitted successfully";

	}

	private void process15CBExcelFile(XSSFWorkbook workbook, String deductorTan, String deductorPan,
			String dateOfPosting, String tenantId, String userName, Integer assessmentYear,
			Form15FileFormat fileFormat) {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Processing the file asynchronusly to get data and prepare DTO {}");
		XSSFSheet sheet = null;
		List<Form15CBDTO> listOf15CB = new ArrayList<>();
		List<ShareholderMasterNonResidential> listShareHolder = new ArrayList<>();
		List<InvoiceShareholderNonResident> listInvoiceShareHolder = new ArrayList<>();

		try {
			sheet = workbook.getSheetAt(0);

			listOf15CB = processSheet15CB(sheet);

			processListForm15CBDTO(listOf15CB, deductorPan, listShareHolder, listInvoiceShareHolder);

			updateListOfNRShareHolder(listShareHolder);
			updateListOfNRInvoiceShareHolder(listInvoiceShareHolder);
			if (fileFormat.isExcel()) {
				create15CBExcelFilingReport(deductorTan, deductorPan, dateOfPosting, tenantId, userName,
						assessmentYear);
			} else {
				generateForm15CB(deductorTan, deductorPan, dateOfPosting, tenantId, userName, assessmentYear);
			}

		} catch (Exception e) {
			logger.info("Exception occured while processing the file " + e + "{}");

		}
	}

	private List<Form15CBDTO> processSheet15CB(XSSFSheet sheetA) throws Exception {
		logger.info("Processing sheet A {}");
		List<Form15CBDTO> listOfForm15CBDTO = new ArrayList<>();
		int dataRowsCount = 0;
		XSSFRow headerRow = null;
		int columnCount = 0;
		List<String> headerList = null;
		Map<String, String> headerAndField = null;

		headerRow = sheetA.getRow(4);
		dataRowsCount = sheetA.getLastRowNum();
		columnCount = headerRow.getLastCellNum();

		headerList = getHeaderListOf15CB(headerRow, columnCount);
		headerAndField = getHeadersMappingForP15CB();

		/**
		 * this for loop will iterate each row having data and will pick the cell values
		 * from the row in each iteration it will be creating one Form15CAPartADTO
		 * object and will assign the values to respective fields that is mapped in
		 * getHeadersMappingForPartA() method
		 */
		for (int rowIndex = 5; rowIndex <= dataRowsCount; rowIndex++) {
			XSSFRow dataRow = sheetA.getRow(rowIndex);
			Form15CBDTO dto = new Form15CBDTO();
			Integer index = 0;

			for (int cellNo = 3; cellNo <= columnCount; cellNo++) {
				XSSFCell cell = dataRow.getCell(cellNo);

				if (cell != null && index < headerList.size()) {
					cell.setCellType(CellType.STRING);
					String value = cell.getStringCellValue();

					if (headerAndField.get(headerList.get(index).trim()) != null) {
						Field field = Form15CBDTO.class
								.getDeclaredField(headerAndField.get(headerList.get(index).trim()));
						field.setAccessible(true);
						if (field.get(dto) == null && StringUtils.isNotBlank(value)) {
							convertValueToDataTypeOfFieldAndAssignToFieldsOfDTO(field, value, dto);
						}
					}

				}
				index++;
			} // for loop iterating cells
			listOfForm15CBDTO.add(dto);
		} // for loop iterating rows
		logger.info("Sheet 15CB processed succesfully and list of DTO s prepared with size " + listOfForm15CBDTO.size()
				+ " {}");
		return listOfForm15CBDTO;

	}

	/**
	 * responsible to create a list and return having the header names of part b
	 * under which values can be modified by user
	 * 
	 * @param headerRow
	 * @param columnCount
	 * @return List<String>
	 */
	private List<String> getHeaderListOf15CB(XSSFRow headerRow, int columnCount) {
		List<String> list = new ArrayList<>();
		for (int cellNo = 3; cellNo <= columnCount; cellNo++) {
			String value = headerRow.getCell(cellNo) == null ? "" : headerRow.getCell(cellNo).getStringCellValue();
			if (StringUtils.isNotBlank(value)) {
				list.add(value);
			}
		}
		return list;

	}

	/**
	 * returns a map having the form 15 CA parta A headers in key and
	 * Form15CAPartADTO fields as value
	 * 
	 * @return
	 */
	private Map<String, String> getHeadersMappingForP15CB() {
		Map<String, String> map = new HashMap<>();
		map.put("INVOICE SHARE HOLDER ID", "id");
		map.put("Flat / Door / Building", "flatDoorBlockNo");
		map.put("Name of Premises / Building / Village", "nameBuildingVillage");
		map.put("Road / Street", "roadStreetPostoffice");
		map.put("Area / Locality", "areaLocality");
		map.put("Town / City / District", "townCityDistrict");
		map.put("State", "state");
		map.put("ZIP Code", "pinCode");
		map.put("Salutation of Shareholder", "shareholderSalutation");
		map.put("Country to which remittance is made", "remittanceCountry");
		map.put("Amount payable In foreign currency", "amountPayableInForeignCurrency");
		map.put("Currency", "currency");
		map.put("Proposed date of remittance (DD/MM/YYYY)", "proposedDateOfRemittance");
		map.put("Amount of TDS In foreign currency", "tdsAmountInForeignCurrency");
		map.put("Actual amount of remittance after TDS (in foreign currency)",
				"actualRemittanceAmountAfterTdsInForeignCurrency");
		map.put("Date of deduction of tax at source,if any", "taxAtSourceDeductionDate");//
		map.put("Shareholder Master id", "shareHolderId");
		return map;

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

	private void processListForm15CBDTO(List<Form15CBDTO> list, String deductorPan,
			List<ShareholderMasterNonResidential> listShareHolder, List<InvoiceShareholderNonResident> listnrInv) {
		logger.info("Processing list of Form15CAPartADTO to get Share Holder records {}");
		List<InvoiceShareholderNonResident> invoiceNr = null;
		List<ShareholderMasterNonResidential> nrShareholders = null;
		Map<Integer, Form15CBDTO> shareHoldermap = list.stream()
				.collect(Collectors.toMap(Form15CBDTO::getShareHolderId, Function.identity()));
		Map<Integer, Form15CBDTO> invoiceMap = list.stream()
				.collect(Collectors.toMap(Form15CBDTO::getId, Function.identity()));
		List<Integer> listInvoiceId = list.stream().map(n -> n.getId()).collect(Collectors.toList());
		List<Integer> shareholderIdlist = list.stream().map(n -> n.getShareHolderId()).collect(Collectors.toList());

		if (!listInvoiceId.isEmpty()) {
			String ids = listInvoiceId.toString().replace("[", "(").replace("]", ")");
			invoiceNr = form15FilingDetailsDAO.findByIdPan(ids, deductorPan);
			invoiceNr.stream().forEach(invoice -> {
				invoice.setRemittanceMadeCountry(invoiceMap.get(invoice.getId()).getRemittanceCountry());
				invoice.setDividendAmountForeignCurrency(
						invoiceMap.get(invoice.getId()).getAmountPayableInForeignCurrency());
				invoice.setDateOfPosting(invoiceMap.get(invoice.getId()).getTaxAtSourceDeductionDate());
				invoice.setProposedDateOfRemmitence(invoiceMap.get(invoice.getId()).getProposedDateOfRemittance());
				invoice.setActualRemmitanceAmountForeignCurrency(
						invoiceMap.get(invoice.getId()).getActualRemittanceAmountAfterTdsInForeignCurrency());
				listnrInv.add(invoice);
			});
		}

		if (!shareholderIdlist.isEmpty()) {
			String ids = shareholderIdlist.toString().replace("[", "(").replace("]", ")");
			nrShareholders = onboardingClient
					.getNonResidentialShareholderByIdsFeign(MultiTenantContext.getTenantId(), deductorPan, ids)
					.getBody().getData();
			nrShareholders.stream().forEach(share -> {
				share.setFlatDoorBlockNo(shareHoldermap.get(share.getId()).getFlatDoorBlockNo());
				share.setRoadStreetPostoffice(shareHoldermap.get(share.getId()).getRoadStreetPostoffice());
				share.setAreaLocality(shareHoldermap.get(share.getId()).getAreaLocality());
				share.setTownCityDistrict(shareHoldermap.get(share.getId()).getTownCityDistrict());
				share.setState(shareHoldermap.get(share.getId()).getState());
				share.setPinCode(shareHoldermap.get(share.getId()).getPinCode());
				share.setNameBuildingVillage(shareHoldermap.get(share.getId()).getNameBuildingVillage());
				listShareHolder.add(share);
			});
		}

	}

	private Integer updateListOfNRShareHolder(List<ShareholderMasterNonResidential> listShareHolder) {
		// Integer count=onboardingClient.batchUpdateNonResident(listShareHolder,
		// MultiTenantContext.getTenantId()).getBody().getData();
		int count = form15FilingDetailsDAO.batchUpdate(listShareHolder);
		logger.info("Updated share holder records successfully {}");
		return count;
	}

	private void generateForm15CB(String deductorTan, String deductorPan, String dateOfPosting, String tenantId,
			String userName, Integer assessmentYear) {
		logger.info("Entered into Async method to generate  15CB Excel file {}");
		MultiTenantContext.setTenantId(tenantId);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.executeWithoutResult((status) -> {
			String error = null;
			List<Errors> errors = new ArrayList<>();
			try {
				List<Form15CBDTO> data = generate15cbDtos(deductorTan, deductorPan, dateOfPosting, tenantId, userName,
						assessmentYear, errors);
				if (!data.isEmpty()) {
					Map<File, String> cbForms = new HashMap<>();
					Generatable builder = null;
					for (Form15CBDTO dto : data) {
						String fileName = "Form" + Form15XmlUtil.FORM_15_CB_FILE_PREFIX + "_" + dto.getShareholderName()
								+ "_" + dto.getId() + ".xml";
						RemitterBuilder form15CBPartBuilder = Form15Generator.form15CB(new Integer(2017), fileName);

						builder = form15CBPartBuilder
								.remitter("01", "03", dto.getRemitterName(), dto.getRemitterPan(),
										dto.getShareholderSalutation())
								.remittee(dto.getShareholderName(), dto.getFlatDoorBlockNo(),
										dto.getRoadStreetPostoffice(), dto.getNameBuildingVillage(),
										dto.getTownCityDistrict(), dto.getAreaLocality(), dto.getPinCode(),
										dto.getState(), dto.getCountry())
								.remittance(dto.getRemittanceCountry(), dto.getCurrency(),
										stringAmountToBigDecimal(dto.getAmountPayableInForeignCurrency()),
										dto.getAmountPayableInInr(), dto.getNameOfBank(), dto.getBranchOfBank(),
										dto.getBsrCodeOfBankBranch(), dto.getProposedDateOfRemittance(),
										dto.getNatureOfRemittance(), dto.getRbiPurposeCode(), dto.getRbiPurposeCode(),
										checkBoolean(dto.getTaxPayableGrossedUp()))
								.taxableAsPerItActInIndia(dto.getRemittanceSectionOfAct(),
										dto.getIncomeChargeableToTax(), dto.getTaxLiability(),
										dto.getBasisOfIncomeAndTaxLiability())
								.dtaa(checkBoolean(dto.getRemittanceRecipientTRCAvailable()), dto.getRelevantDTAA(),
										dto.getRelevantDTAAArticle(), BigDecimal.ZERO, BigDecimal.ZERO, "Y",
										dto.getRelevantDTAAArticle(), dto.getTdsRatePerDTAA(), "Y", "N", "N", "N")
								.tds(dto.getTdsRateType(), dto.getRemittanceSectionOfAct(),
										dto.getTdsAmountInForeignCurrency(), dto.getTdsAmountInInr(), dto.getTdsRate(),
										stringAmountToBigDecimal(
												dto.getActualRemittanceAmountAfterTdsInForeignCurrency()),
										dto.getTaxAtSourceDeductionDate())
								.accountant(dto.getAccountantName(), dto.getCaNameOfProprietorship(),
										dto.getCaMembershipNumber(), dto.getCaRegistrationNumber(),
										dto.getAccountantFlatDoorBlockNo(), dto.getAccountantRoadStreetPostoffice(),
										dto.getAccountantNameBuildingVillage(), dto.getAccountantTownCityDistrict(),
										dto.getAccountantAreaLocality(), dto.getAccountantPinCode(),
										dto.getAccountantState(), dto.getAccountantCountry());
						File file = builder.generate();
						cbForms.put(file, dto.getFolioNo());
					}
					for (Form15CBDTO form15CBDTO : data) {
						ingestionClient.updateNonResidentShareholder(tenantId, form15CBDTO.getRemitterPan(),
								form15CBDTO.getId());
					}
					saveCBFormsToBlob(cbForms, assessmentYear, tenantId, deductorTan, dateOfPosting, userName,
							deductorPan);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public static BigDecimal stringAmountToBigDecimal(String input) {
		BigDecimal con = null;
		try {
			con = new BigDecimal(input != null && !input.equalsIgnoreCase("") ? input : "0.0");
		} catch (NumberFormatException e) {
			con = new BigDecimal("0.0");
		}
		return con;
	}

	public void saveCBFormsToBlob(Map<File, String> consoliadtedFilesFor15CA, int assessmentYear, String tenantId,
			String deductorTan, String postingDate, String userName, String deductorPan) throws Exception {

		if (!consoliadtedFilesFor15CA.isEmpty()) {
			List<Errors> errors = new ArrayList<>();
			Iterator iterator = consoliadtedFilesFor15CA.entrySet().iterator();
			List<File> files = new ArrayList();
			while (iterator.hasNext()) {
				Map.Entry<File, String> pair = (Map.Entry<File, String>) iterator.next();
				File file = (File) pair.getKey();
				List<Errors> errorsList = Validator.validate(file, getXSDPath("Form15CB"), pair.getValue());
				if (errorsList.size() > 0) {
					iterator.remove();
					errors.addAll(errorsList);
				} else {
					files.add(file);
				}
			}
			try {
				String zipFileName = deductorTan + "_" + assessmentYear + "_15_CB_FORMS";
				File gFormsZip = Form15XmlUtil.zip(files, zipFileName);

				File blobFile = new File(zipFileName + new Date().getTime() + ".zip");
				try (InputStream inputstream = new FileInputStream(gFormsZip);) {
					// TODO assign proper value and remove null
					MultipartFile multipartFile = null;
					// new MockMultipartFile("file", blobFile.getName(), "application/zip",
					// IOUtils.toByteArray(inputstream));
					OutputStream out = new FileOutputStream(blobFile);
					out.write(multipartFile.getBytes());
					out.close();
					String blobUrl = this.blobStorage.uploadExcelToBlob(multipartFile, tenantId);
					updateFilingReportStatus(deductorTan, assessmentYear, postingDate, Form15FileType.CB,
							Form15FileFormat.XML, ReturnType.REGULAR, Form15FilingStatus.GENERATED, blobUrl, "",
							userName);
				}
			} catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
				logger.error("Could not generate form 15 CB", e);
			}

			try {
				if (!errors.isEmpty()) {
					File errorFile = Form15XmlUtil.generateErrorFile(errors, Form15FileType.CB);
					InputStream errorStream = new FileInputStream(errorFile);
					// TODO remove null and assign the file
					MultipartFile multipartErrorFile = null;
					// new MockMultipartFile("file", errorFile.getName(),
					// "application/vnd.ms-excel", IOUtils.toByteArray(errorStream));
					errorStream.close();
					String errorFileUrl = this.blobStorage.uploadExcelToBlob(multipartErrorFile, tenantId);
					updateErrorFileURL(deductorTan, assessmentYear, postingDate, Form15FileType.CB,
							Form15FileFormat.XML, errorFileUrl);
				} else {
					updateErrorFileURL(deductorTan, assessmentYear, postingDate, Form15FileType.CB,
							Form15FileFormat.XML, "");
				}
			} catch (Exception e) {
				logger.error("Could not generate form 15 CB", e);
			}
		}
	}

	private File getXSDPath(String type) throws URISyntaxException {
		File file = new File(basePath + "/xsd/" + type + ".xsd");
		return file;
	}

	public Form15FilingDetails updateErrorFileURL(String deductorTan, Integer assessmentYear, String dateOfPosting,
			Form15FileType fileType, Form15FileFormat fileFormat, String errorFileUrl) {

		List<Form15FilingDetails> existingFilings = form15FilingDetailsDAO.findFilingFiles(deductorTan, assessmentYear,
				dateOfPosting, fileType, fileFormat);
		Form15FilingDetails filingReportStatus = existingFilings.isEmpty() ? null : existingFilings.get(0);

		if (filingReportStatus == null) {
			throw new IllegalStateException("FilingFiles15 record not found for deductorTan: " + deductorTan
					+ ", assessmentYear: " + assessmentYear + ", dateOfPosting: " + dateOfPosting + ", fileType: "
					+ fileType + ", fileFormat: " + fileFormat);
		}

		filingReportStatus.setErrorFileUrl(errorFileUrl);
		return form15FilingDetailsDAO.update(filingReportStatus);
	}

	private Integer updateListOfNRInvoiceShareHolder(List<InvoiceShareholderNonResident> listShareHolder) {
		int count = form15FilingDetailsDAO.batchUpdateInvoiceShareHolder(listShareHolder);
		logger.info("Updated Share holder Record {}");
		return count;
	}
}
