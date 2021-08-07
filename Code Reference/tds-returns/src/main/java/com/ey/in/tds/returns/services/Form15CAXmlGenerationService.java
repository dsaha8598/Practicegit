package com.ey.in.tds.returns.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.ey.in.tcs.common.domain.dividend.Form15FileFormat;
import com.ey.in.tcs.common.domain.dividend.Form15FileType;
import com.ey.in.tcs.common.domain.dividend.Form15FilingStatus;
import com.ey.in.tcs.common.domain.dividend.InvoiceShareholderNonResident;
import com.ey.in.tcs.common.domain.dividend.NonResidentWithholdingDetails;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.masters.DividendOnboardingInfoMetaData;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterNonResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorTanAddress;
import com.ey.in.tds.common.onboarding.response.dto.DeductorOnboardingInformationResponseDTO;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.ReturnType;
import com.ey.in.tds.dividend.common.Form15XmlUtil;
import com.ey.in.tds.dividend.forms.Form15Generator;
import com.ey.in.tds.dividend.forms.builder.Generatable;
import com.ey.in.tds.dividend.forms.builder.ca.Form15CAPartBuilder;
import com.ey.in.tds.feign.client.IngestionClient;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.jdbc.dao.Form15FilingDetailsDAO;
import com.ey.in.tds.returns.dividend.validator.Errors;
import com.ey.in.tds.returns.dividend.validator.Validator;
import com.ey.in.tds.returns.dto.AuthorisedPersonDTO;
import com.ey.in.tds.returns.dto.Form15CAPartADTO;
import com.ey.in.tds.returns.dto.Form15CAPartBDTO;
import com.ey.in.tds.returns.dto.Form15CAPartCDTO;
import com.microsoft.azure.storage.StorageException;

@Component
public class Form15CAXmlGenerationService {

	private static final String FORM_15_CA_XSD = "Form15CA";
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Filing15caService filing15caService;

	@Value("${report.io.dir.path}")
	private String basePath;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private IngestionClient ingestionClient;

	@Autowired
	private Form15FilingDetailsDAO form15FilingDetailsDAO;

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	protected BlobStorage blobStorage;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private DividendFilingCommonErrorFileService dividendFilingCommonErrorFileService;

	@Autowired
	@Value("${app.ack.num.enabled}")
	private boolean shouldfetchAckNum;

	public static final SimpleDateFormat xmlFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Transactional
	public String generate15CAXmlFilingReport(String deductorTan, String deductorPan, String dateOfPosting,
			String tenantId, String userName, Integer assessmentYear) throws Exception {
		logger.info("Filing 15CA Excel: Started generation for TAN: {}, date of posting : {} ", deductorTan,
				dateOfPosting);

		form15FilingDetailsDAO.createFilingReportStatus(deductorTan, assessmentYear, dateOfPosting, Form15FileType.CA,
				Form15FileFormat.XML, ReturnType.REGULAR, userName);

		CompletableFuture.runAsync(() -> this.create15CAFilingReport(deductorTan, deductorPan, dateOfPosting, tenantId,
				userName, assessmentYear));

		return "Request for generating Form 15CA XML submitted successfully";
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
				List<Errors> errorList = new ArrayList<>();

				filing15caService.generateAuthorisedPersonDto(deductorTan, deductorPan, tenantId, errorList);

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate dateTime = LocalDate.parse(postingDate, formatter);
				List<InvoiceShareholderNonResident> nonResidentShareholders = form15FilingDetailsDAO
						.findAllNonResidentByDateOfPosting(deductorPan, dateTime, assessmentYear);
				if (nonResidentShareholders.isEmpty()) {
					Errors error = new Errors();
					error.setMessage("No Shareholder Found For Deductor Pan=" + deductorPan + " ,Financial Year="
							+ assessmentYear + " And Date Of Posting=" + dateTime);
					errorList.add(error);

				}
				if (errorList.isEmpty()) {
					for (InvoiceShareholderNonResident nonResidentShareholder : nonResidentShareholders) {
						if (check15CACBFlag(nonResidentShareholder, tenantId, deductorPan).equalsIgnoreCase("Yes")) {
							boolean is15cbGenerated = false;
							boolean isDividendLessThanFiveLakh = (nonResidentShareholder.getDividendAmountRs()
									.compareTo(BigDecimal.valueOf(500000))) < 0;
							boolean isDividendEqualToFiveLakh = (nonResidentShareholder.getDividendAmountRs()
									.equals(BigDecimal.valueOf(500000)));
							boolean isDividendIncomeMoreThan5Lakh = (nonResidentShareholder.getDividendAmountRs()
									.compareTo(BigDecimal.valueOf(500000))) > 0;
							boolean isLdcApplied = false;
							boolean isTreatyApplied = false;
							if (!Objects.isNull(nonResidentShareholder.getWithholdingDetails())) {
								NonResidentWithholdingDetails withholdingDetails = nonResidentShareholder
										.getWithholdingDetails();
								isLdcApplied = checkBooleanForNull(withholdingDetails.isLdcApplied());
								isTreatyApplied = checkBooleanForNull(withholdingDetails.isTreatyApplied());
							}
							if (checkOnboardingInformation(deductorPan, tenantId) && isDividendIncomeMoreThan5Lakh
									&& (!isLdcApplied || (isLdcApplied && isTreatyApplied))) {
								if (!Objects.isNull(nonResidentShareholder.getIfForm15CBgenerated())) {
									if (nonResidentShareholder.getIfForm15CBgenerated())
										is15cbGenerated = true;
								}else {
									Errors error = new Errors();
									error.setCompleteMessage(nonResidentShareholder.getId() + "");
									error.setId(nonResidentShareholder.getFolioNumber());
									error.setMessage("Form 15 CB Is Not Generated For This Trnsaction ");
									errorList.add(error);
								}

							}

							if (is15cbGenerated) {
								/**
								 * code is commmented temporarily as acknowledgement number is not retrievable
								 */
								/*if 	if (shouldfetchAckNum) {
									(nonResidentShareholder.getForm15CBAcknowledgementNo() != null
											&& !nonResidentShareholder.getForm15CBAcknowledgementNo()
													.equalsIgnoreCase("NA")) {
										Form15CAPartCDTO partC = filing15caService.generate15CAPartCDto(
												nonResidentShareholder, deductorTan, deductorPan, tenantId, errorList);
										if (partC != null)
											form15CAPartCDTOS.add(partC);
									} else {
										Errors noAcknowledgmentErrors = new Errors();
										noAcknowledgmentErrors.setId(nonResidentShareholder.getFolioNumber());
										noAcknowledgmentErrors.setMessage("Acknowledgement Number does not exist");
										noAcknowledgmentErrors.setType(Errors.TYPE.FATAL_ERROR);
										noAcknowledgmentErrors
												.setCompleteMessage("Acknowledgement Number does not exist");
										errorList.add(noAcknowledgmentErrors);
									}
								} else {
									Form15CAPartCDTO partC = filing15caService.generate15CAPartCDto(
											nonResidentShareholder, deductorTan, deductorPan, tenantId, errorList);
									if (partC != null)
										form15CAPartCDTOS.add(partC);
								}*/
								Form15CAPartCDTO partC = filing15caService.generate15CAPartCDto(
										nonResidentShareholder, deductorTan, deductorPan, tenantId, errorList);
								if (partC != null)
									form15CAPartCDTOS.add(partC);
							} else if (isDividendLessThanFiveLakh || isDividendEqualToFiveLakh) {
								Form15CAPartADTO partA = filing15caService.generate15CAPartADtos(nonResidentShareholder,
										deductorTan, deductorPan, tenantId, errorList);
								if (partA != null)
									form15CAPartADTOS.add(partA);

							} else if (!isDividendLessThanFiveLakh && isLdcApplied && !isTreatyApplied) {
								Form15CAPartBDTO partB = filing15caService.generate15CAPartBDto(nonResidentShareholder,
										deductorTan, deductorPan, tenantId, errorList);
								if (partB != null)
									form15CAPartBDTOS.add(partB);

							} else {
								logger.info("No matching conditions found for generating 15CA for transaction id : {}",
										nonResidentShareholder.getId());
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
				//merging the records based on pan
				Map<String,Form15CAPartCDTO> map=new HashMap<>();
				if(!form15CAPartCDTOS.isEmpty()) {
					form15CAPartCDTOS.stream().forEach(n->{
						if(map.containsKey(n.getShareholderPan().trim())) {
							Form15CAPartCDTO dto=map.get(n.getShareholderPan().trim());
							n.setAmountPayableInInr(n.getAmountPayableInInr()==null?BigDecimal.ZERO:n.getAmountPayableInInr() .add(dto.getAmountPayableInInr()==null?BigDecimal.ZERO:dto.getAmountPayableInInr()));
							n.setIncomeChargeableToTax(n.getIncomeChargeableToTax()==null?BigDecimal.ZERO:n.getIncomeChargeableToTax() .add(dto.getIncomeChargeableToTax()==null?BigDecimal.ZERO:dto.getIncomeChargeableToTax()));
							n.setTaxLiability(n.getTaxLiability()==null?BigDecimal.ZERO:n.getTaxLiability() .add(dto.getTaxLiability()==null?BigDecimal.ZERO:dto.getTaxLiability())); //getTaxLiabilityPerDTAA
							n.setTaxLiabilityPerDTAA(n.getTaxLiabilityPerDTAA()==null?BigDecimal.ZERO:n.getTaxLiabilityPerDTAA() .add(dto.getTaxLiabilityPerDTAA()==null?BigDecimal.ZERO:dto.getTaxLiabilityPerDTAA()));
							n.setTdsAmountInInr(n.getTdsAmountInInr()==null?BigDecimal.ZERO:n.getTdsAmountInInr() .add(dto.getTdsAmountInInr()==null?BigDecimal.ZERO:dto.getTdsAmountInInr()));
							Double foreignCurency1=Double.parseDouble(StringUtils.isNotBlank(n.getAmountPayableInForeignCurrency())?n.getAmountPayableInForeignCurrency():"0");
							Double foreignCurency2=Double.parseDouble(StringUtils.isNotBlank(dto.getAmountPayableInForeignCurrency())?dto.getAmountPayableInForeignCurrency():"0");
							n.setAmountPayableInForeignCurrency(BigDecimal.valueOf(foreignCurency1+foreignCurency2).toString());
							map.put(n.getShareholderPan().trim(), n);
						}else {
							map.put(n.getShareholderPan().trim(), n);
						}
					});
					}
				if(!map.isEmpty()) {
					form15CAPartCDTOS.clear();
					map.entrySet().stream().forEach(n->form15CAPartCDTOS.add(n.getValue()));
				}
				generateForm15CA(form15CAPartADTOS, form15CAPartBDTOS, form15CAPartCDTOS, assessmentYear, tenantId,
						deductorTan, postingDate, userName, errorList);
			} catch (Exception e) {
				logger.error("Error occured while Generating Form 15CA xml", e);
				form15FilingDetailsDAO.updateFilingReportStatus(deductorTan, assessmentYear, postingDate,
						Form15FileType.CA, Form15FileFormat.XML, ReturnType.REGULAR, Form15FilingStatus.ERROR, null,
						e.getMessage(), userName);
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

	private void generateForm15CA(List<Form15CAPartADTO> form15CAPartADTOS, List<Form15CAPartBDTO> form15CAPartBDTOS,
			List<Form15CAPartCDTO> form15CAPartCDTOS, int assessmentYear, String tenantId, String deductorTan,
			String postingDate, String userName, List<Errors> partCACErrors) throws Exception {
		List<File> consoliadtedFilesFor15CA = new ArrayList<>();
		Map<File, String> totalCAAFiles = getPartCAAFiles(form15CAPartADTOS, assessmentYear, tenantId);
		Map<File, String> totalCABFiles = getPartCABFiles(form15CAPartBDTOS, assessmentYear, tenantId);
		Map<File, String> totalCACFiles = getPartCACFiles(form15CAPartCDTOS, assessmentYear, tenantId);
		List<Errors> errors = new ArrayList<>();
		// Adding PartCACErrors in case there is no Acknowledgement Number.
		// errors.addAll(partCACErrors);
		if (!totalCAAFiles.isEmpty()) {
			Iterator iterator = totalCAAFiles.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<File, String> pair = (Map.Entry<File, String>) iterator.next();
				File file = (File) pair.getKey();
				List<Errors> errorsList = Validator.validate(file, getXSDPath("Form15CA"), pair.getValue());
				if (errorsList.size() < 0) {
					iterator.remove();
					errors.addAll(errorsList);
				} else {
					consoliadtedFilesFor15CA.add(file);
				}
			}
		}

		if (!totalCABFiles.isEmpty()) {
			Iterator iterator = totalCABFiles.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<File, String> pair = (Map.Entry<File, String>) iterator.next();
				File file = (File) pair.getKey();
				List<Errors> errorsList = Validator.validate(file, getXSDPath("Form15CA"), pair.getValue());
				if (errorsList.size() < 0) {
					iterator.remove();
					errors.addAll(errorsList);
				} else {
					consoliadtedFilesFor15CA.add(file);
				}
			}
		}

		if (!totalCACFiles.isEmpty()) {
			Iterator iterator = totalCACFiles.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<File, String> pair = (Map.Entry<File, String>) iterator.next();
				File file = (File) pair.getKey();
				List<Errors> errorsList = Validator.validate(file, getXSDPath("Form15CA"), pair.getValue());
				if (errorsList.size() < 0) {
					iterator.remove();
					errors.addAll(errorsList);
				} else {
					consoliadtedFilesFor15CA.add(file);
				}
			}
		}

		if (!errors.isEmpty()) {
			errors.forEach(n -> {
				n.setMessage(n.getCompleteMessage());
				n.setCompleteMessage("");
			});
			partCACErrors.addAll(errors);
		}
		if (!partCACErrors.isEmpty()) {
			File errorFile = dividendFilingCommonErrorFileService.generateErrorFile(partCACErrors);
			consoliadtedFilesFor15CA.add(errorFile);
		}

		if (!consoliadtedFilesFor15CA.isEmpty()) {
			try {
				String zipFileName = deductorTan + "_" + assessmentYear + "_15_CA_FORMS" + new Date().getTime()
						+ ".zip";
				File gFormsZip = Form15XmlUtil.zip(consoliadtedFilesFor15CA, zipFileName);
				
				String blobUrl = this.blobStorage.uploadExcelToBlobWithFile(gFormsZip, tenantId);
				form15FilingDetailsDAO.updateFilingReportStatus(deductorTan, assessmentYear, postingDate,
						Form15FileType.CA, Form15FileFormat.XML, ReturnType.REGULAR, Form15FilingStatus.GENERATED,
						blobUrl, "", userName);
			} catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
				logger.error("Could not generate form 15 C", e);
			}
		}
	}

	private Map<File, String> getPartCACFiles(List<Form15CAPartCDTO> form15CAPartCDTOS, int assessmentYear,
			String tenantId) throws Exception {

		Map<File, String> partCAFiles = new HashMap<>();
		if (!form15CAPartCDTOS.isEmpty()) {
			Generatable builder = null;
			for (Form15CAPartCDTO form15CAPartC : form15CAPartCDTOS) {
				String fileName = "Form" + Form15XmlUtil.FORM_15_CA_PART_C_FILE_PREFIX + "_"
						+ form15CAPartC.getShareholderName() + "_" + form15CAPartC.getId() + ".xml";
				Form15CAPartBuilder form15CAPartBuilder = Form15Generator.form15CA(2017, fileName);
				Date orderCertificateDate = null;
				String aoDesignation = null, aoSection = "N", certificateApplied = "N";

				if (form15CAPartC.getOrderOrCertificateObtainedFromAO()) {
					certificateApplied = "Y";
				}
				if (form15CAPartC.getOrderOrCertificateDate() != null
						&& StringUtils.isNotEmpty(form15CAPartC.getOrderOrCertificateDate())) {
					orderCertificateDate = new SimpleDateFormat("yyyy-MM-dd")
							.parse(form15CAPartC.getOrderOrCertificateDate());
				}
				if (form15CAPartC.getOrderOrCertificateSection() != null
						&& !form15CAPartC.getOrderOrCertificateSection().isEmpty()) {
					aoDesignation = "DCIT (TDS)";
					aoSection = "Y";
				}

				AuthorisedPersonDTO authorisedPersonDTO = generateAuthorisedPersonDto(form15CAPartC.getRemitterTan(),
						form15CAPartC.getRemitterPan(), tenantId);
				builder = form15CAPartBuilder.partC()
						.remitterPC(form15CAPartC.getRemitterName(), form15CAPartC.getRemitterPan(),
								form15CAPartC.getRemitterTan(), form15CAPartC.getRemitterEmail(),
								form15CAPartC.getRemitterPhoneNumber(), form15CAPartC.getRemitterStatus(),
								form15CAPartC.getRemitterResidentialStatus(),
								form15CAPartC.getRemitterPrincipalAreaOfBusiness(), form15CAPartC.getRemitterAreaCode(),
								form15CAPartC.getRemitterAOType(), form15CAPartC.getRemitterRangeCode(),
								form15CAPartC.getRemitterAONumber(), form15CAPartC.getRemitterFlatDoorBlockNo(),
								form15CAPartC.getRemitterRoadStreetPostoffice(),
								form15CAPartC.getRemitterNameBuildingVillage(),
								form15CAPartC.getRemitterTownCityDistrict(), form15CAPartC.getRemitterAreaLocality(),
								form15CAPartC.getRemitterPinCode(), form15CAPartC.getRemitterState(),
								form15CAPartC.getRemitterCountry())
						.remiteePC(form15CAPartC.getShareholderName(), form15CAPartC.getShareholderPan(),
								form15CAPartC.getShareholderEmail(), form15CAPartC.getShareholderIsdCodePhoneNumber(),
								form15CAPartC.getShareholderStatus(),
								form15CAPartC.getShareholderPrincipalPlaceOfBusiness(),
								form15CAPartC.getFlatDoorBlockNo(), form15CAPartC.getRoadStreetPostoffice(),
								form15CAPartC.getNameBuildingVillage(), form15CAPartC.getTownCityDistrict(),
								form15CAPartC.getAreaLocality(), form15CAPartC.getPinCode(), form15CAPartC.getState(),
								form15CAPartC.getCountry())
						.remittance(form15CAPartC.getRemittanceCountry(), form15CAPartC.getCurrency(),
								stringAmountToBigDecimal(form15CAPartC.getAmountPayableInForeignCurrency()),
								form15CAPartC.getAmountPayableInInr(), form15CAPartC.getNameOfBank(),
								form15CAPartC.getBranchOfBank(), form15CAPartC.getBsrCodeOfBankBranch(),
								form15CAPartC.getProposedDateOfRemittance(), form15CAPartC.getNatureOfRemittance(),
								"RB-14.1", form15CAPartC.getRbiPurposeCode(),
								checkBoolean(form15CAPartC.getTaxPayableGrossedUp()))
						.actDetail("", form15CAPartC.getIncomeChargeableToTax(), form15CAPartC.getTheTaxableLiability(),
								"AS PER INCOME-TAX ACT")
						.dtaa(checkBoolean(form15CAPartC.getRemittanceRecipientTRCAvailable()),
								form15CAPartC.getRelevantDTAA(), form15CAPartC.getRelevantDTAAArticle(),
								stringAmountToBigDecimal(form15CAPartC.getdTAAArticle()),
								form15CAPartC.getTaxableIncomeAsPerDATAA(), checkBoolean(form15CAPartC.getRemittanceForRoyalties()), form15CAPartC.getdTAAArticle(),
								form15CAPartC.getTdsRatePerDTAA(), "N", "N", "N", "N")
						.tdsDetails("", form15CAPartC.getTdsAmountInInr(),
								stringAmountToBigDecimal(form15CAPartC.getTdsAmountInForeignCurrency()),
								form15CAPartC.getTdsRate(),
								stringAmountToBigDecimal(form15CAPartC.getAmountPayableInForeignCurrency()))
						.accountant(form15CAPartC.getAccountantName(), form15CAPartC.getCaNameOfProprietorship(),
								form15CAPartC.getCaMembershipNumber(), form15CAPartC.getCertificateNumber(),
								form15CAPartC.getCertificateNumberDate(), form15CAPartC.getAccountantFlatDoorBlockNo(),
								form15CAPartC.getAccountantRoadStreetPostoffice(),
								form15CAPartC.getAccountantNameBuildingVillage(),
								form15CAPartC.getAccountantTownCityDistrict(),
								form15CAPartC.getAccountantAreaLocality(), form15CAPartC.getAccountantPinCode(),
								form15CAPartC.getAccountantState(), form15CAPartC.getAccountantCountry())
						.aoCertificate(form15CAPartC.getOrderOrCertificateSection(),
								form15CAPartC.getAssessingOfficerName(), aoDesignation, orderCertificateDate,
								form15CAPartC.getOrderOrCertificateNumber(), aoSection)
						.declaration("I", authorisedPersonDTO.getAuthorisedPersonName(),
								authorisedPersonDTO.getFatherOrHusbandName(), authorisedPersonDTO.getDesignation(),
								authorisedPersonDTO.getDateOfFiling(), authorisedPersonDTO.getPlace());
				File file = builder.generate();
				partCAFiles.put(file, form15CAPartC.getFolioNo());
			}
		}
		return partCAFiles;
	}

	private Map<File, String> getPartCABFiles(List<Form15CAPartBDTO> form15CAPartBDTOS, int assessmentYear,
			String tenantId) throws Exception {
		Map<File, String> partCBFiles = new HashMap<>();
		if (!form15CAPartBDTOS.isEmpty()) {
//            String fileName = Form15XmlUtil.randomSuffixedFileName(Form15XmlUtil.FORM_15_CA_PART_B_FILE_PREFIX);
			Generatable builder = null;
			for (Form15CAPartBDTO form15CAPartB : form15CAPartBDTOS) {
				String fileName = "Form" + Form15XmlUtil.FORM_15_CA_PART_B_FILE_PREFIX + "_"
						+ form15CAPartB.getShareholderName() + "_" + form15CAPartB.getId() + ".xml";
				Form15CAPartBuilder form15CAPartBuilder = Form15Generator.form15CA(assessmentYear, fileName);
				Date orderCertificateDate = null;
				String aoDesignation = null, aoSection = "N";
				AuthorisedPersonDTO authorisedPersonDTO = generateAuthorisedPersonDto(form15CAPartB.getRemitterTan(),
						form15CAPartB.getRemitterPan(), tenantId);
				if (form15CAPartB.getOrderOrCertificateDate() != null
						&& StringUtils.isNotEmpty(form15CAPartB.getOrderOrCertificateDate())) {
					orderCertificateDate = new SimpleDateFormat("yyyy-MM-dd")
							.parse(form15CAPartB.getOrderOrCertificateDate());
				}
				if (form15CAPartB.getOrderOrCertificateSection() != null
						&& !form15CAPartB.getOrderOrCertificateSection().isEmpty()) {
					aoDesignation = "DCIT (TDS)";
					aoSection = "Y";
				}
				builder = form15CAPartBuilder.partB().remitterPB(form15CAPartB.getRemitterName(),
						form15CAPartB.getRemitterPan(), form15CAPartB.getRemitterTan(),
						form15CAPartB.getRemitterEmail(), form15CAPartB.getRemitterPhoneNumber(),
						form15CAPartB.getRemitterStatus(), form15CAPartB.getRemitterResidentialStatus(),
						form15CAPartB.getRemitterFlatDoorBlockNo(), form15CAPartB.getRemitterRoadStreetPostoffice(),
						form15CAPartB.getRemitterNameBuildingVillage(), form15CAPartB.getRemitterTownCityDistrict(),
						form15CAPartB.getRemitterAreaLocality(), form15CAPartB.getRemitterPinCode(),
						form15CAPartB.getRemitterState(), form15CAPartB.getRemitterCountry())
						.remiteePB(form15CAPartB.getShareholderName(), form15CAPartB.getShareholderPan(),
								form15CAPartB.getShareholderEmail(), form15CAPartB.getShareholderIsdCodePhoneNumber(),
								form15CAPartB.getRemittanceCountry(), form15CAPartB.getFlatDoorBlockNo(),
								form15CAPartB.getRoadStreetPostoffice(), form15CAPartB.getNameBuildingVillage(),
								form15CAPartB.getTownCityDistrict(), form15CAPartB.getAreaLocality(),
								form15CAPartB.getPinCode(), form15CAPartB.getState(), form15CAPartB.getCountry())
						.remittance(form15CAPartB.getRemittanceCountry(), form15CAPartB.getCurrency(),
								stringAmountToBigDecimal(form15CAPartB.getAmountPayableInForeignCurrency()),
								form15CAPartB.getAmountPayableInInr(), form15CAPartB.getNameOfBank(),
								form15CAPartB.getBranchOfBank(), form15CAPartB.getBsrCodeOfBankBranch(),
								form15CAPartB.getProposedDateOfRemittance(), form15CAPartB.getNatureOfRemittance(),
								form15CAPartB.getTdsAmount(), form15CAPartB.getTdsRate(),
								form15CAPartB.getDeductionDate(), "RB-14.1", form15CAPartB.getRbiPurposeCode())
						.aoCertificate(form15CAPartB.getOrderOrCertificateSection(),
								form15CAPartB.getAssessingOfficerName(), aoDesignation, orderCertificateDate,
								form15CAPartB.getOrderOrCertificateNumber(), aoSection)
						.declaration("I", authorisedPersonDTO.getAuthorisedPersonName(),
								authorisedPersonDTO.getFatherOrHusbandName(), authorisedPersonDTO.getDesignation(),
								authorisedPersonDTO.getDateOfFiling(), authorisedPersonDTO.getPlace());
				File file = builder.generate();
				partCBFiles.put(file, form15CAPartB.getFolioNo());
			}
		}
		return partCBFiles;
	}

	private Map<File, String> getPartCAAFiles(List<Form15CAPartADTO> form15CAPartADTOS, int assessmentYear,
			String tenantId) throws Exception {

		Map<File, String> partCAFiles = new HashMap<>();
		if (!form15CAPartADTOS.isEmpty()) {
//            String fileName = Form15XmlUtil.randomSuffixedFileName(Form15XmlUtil.FORM_15_CA_PART_A_FILE_PREFIX);
			Generatable builder = null;
			for (Form15CAPartADTO form15CAPartA : form15CAPartADTOS) {
				String fileName = "Form" + Form15XmlUtil.FORM_15_CA_PART_A_FILE_PREFIX + "_"
						+ form15CAPartA.getShareholderName() + "_" + form15CAPartA.getId() + ".xml";
				Form15CAPartBuilder form15CAPartBuilder = Form15Generator.form15CA(assessmentYear, fileName);
				AuthorisedPersonDTO authorisedPersonDTO = generateAuthorisedPersonDto(form15CAPartA.getRemitterTan(),
						form15CAPartA.getRemitterPan(), tenantId);
				builder = form15CAPartBuilder.partA().remitterPA(form15CAPartA.getRemitterName(),
						form15CAPartA.getRemitterPan(), form15CAPartA.getRemitterTan(),
						form15CAPartA.getRemitterEmail(), form15CAPartA.getRemitterPhoneNumber(),
						form15CAPartA.getRemitterStatus(), form15CAPartA.getRemitterResidentialStatus(),
						form15CAPartA.getRemitterFlatDoorBlockNo(), form15CAPartA.getRemitterRoadStreetPostoffice(),
						form15CAPartA.getRemitterNameBuildingVillage(), form15CAPartA.getRemitterTownCityDistrict(),
						form15CAPartA.getRemitterAreaLocality(), form15CAPartA.getRemitterPinCode(),
						form15CAPartA.getRemitterState(), form15CAPartA.getRemitterCountry())
						.remiteePA(form15CAPartA.getShareholderName(), form15CAPartA.getShareholderPan(),
								form15CAPartA.getShareholderEmail(), form15CAPartA.getShareholderPhoneNumber(),
								form15CAPartA.getRemittanceCountry(), form15CAPartA.getFlatDoorBlockNo(),
								form15CAPartA.getRoadStreetPostoffice(), form15CAPartA.getNameBuildingVillage(),
								form15CAPartA.getTownCityDistrict(), form15CAPartA.getAreaLocality(),
								form15CAPartA.getPinCode(), form15CAPartA.getState(), form15CAPartA.getCountry())
						.remittance(form15CAPartA.getAmountPayableBeforeTdsInInr(),
								form15CAPartA.getAggregateRemittanceAmount(), form15CAPartA.getNameOfBank(),
								form15CAPartA.getBranchOfBank(), form15CAPartA.getProposedDateOfRemittance(),
								form15CAPartA.getNatureOfRemittance(), form15CAPartA.getTdsAmount(),
								form15CAPartA.getTdsRate(), form15CAPartA.getDeductionDate(),
								// "RB-14.1",
								form15CAPartA.getRbiPurposeCode(), form15CAPartA.getRbiPurposeCode())
						.declaration("I", authorisedPersonDTO.getAuthorisedPersonName(),
								authorisedPersonDTO.getFatherOrHusbandName(), authorisedPersonDTO.getDesignation(),
								authorisedPersonDTO.getDateOfFiling(), authorisedPersonDTO.getPlace());
				File file = builder.generate();
				partCAFiles.put(file, form15CAPartA.getFolioNo());
			}
		}
		return partCAFiles;
	}

	private File getXSDPath(String type) throws URISyntaxException, IOException {
//        URL res = getClass().getClassLoader().getResource("xsd/" + type + ".xsd");
//        File file = Paths.get(res.toURI()).toFile();
//        return file;
		// File file = new File(
		// basePath + "/xsd/" + type + ".xsd");
		// return file;

		File file = new File(basePath + "/xsd/" + type + ".xsd");
		return file;
	}

	private boolean checkBooleanForNull(Boolean value) {
		if (Objects.isNull(value)) {
			return false;
		} else {
			return value;
		}
	}

	private String checkBoolean(Boolean value) {
		if (Objects.nonNull(value)) {
			if (value.equals(Boolean.TRUE))
				return "Y";
			if (value.equals(Boolean.FALSE))
				return "N";
		}
		return "N";
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

	public static BigDecimal stringAmountToBigDecimal(String input) {
		BigDecimal con = null;
		try {
			con = new BigDecimal(input != null && !input.equalsIgnoreCase("") && !input.equalsIgnoreCase("0.0")? input : "0");
		} catch (NumberFormatException e) {
			con = new BigDecimal("0");
		}
		return con;
	}

	public static String getCurrentDate() {
		return xmlFormat.format(new Date());
	}

	public AuthorisedPersonDTO generateAuthorisedPersonDto(String tanNumber, String deductorPan, String tenantId) {
		logger.info("Generating autherised person dto for 15 CA {}");
		ResponseEntity<ApiStatus<DeductorTanAddress>> deductorTanAddress = onboardingClient
				.getDeductorTanAddressByPanTan(tanNumber, deductorPan, tenantId);
		DeductorTanAddress data = Objects.requireNonNull(deductorTanAddress.getBody()).getData();
		if (data != null) {
			AuthorisedPersonDTO authorisedPersonDTO = new AuthorisedPersonDTO();
			authorisedPersonDTO.setAuthorisedPersonName(data.getPersonName());
			authorisedPersonDTO.setFatherOrHusbandName(data.getDvndFatherOrHusbandName());
			authorisedPersonDTO.setDesignation(data.getPersonDesignation());
			authorisedPersonDTO.setDateOfFiling(new Date());
			authorisedPersonDTO.setPlace(data.getDvndTownCityDistrict());
			return authorisedPersonDTO;
		} else {
			throw new CustomException("No deductor found for given pan", HttpStatus.BAD_REQUEST);
		}
	}
}