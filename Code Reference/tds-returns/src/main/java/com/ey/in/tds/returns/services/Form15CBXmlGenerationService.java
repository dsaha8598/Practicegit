package com.ey.in.tds.returns.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.ey.in.tcs.common.domain.dividend.Form15FileFormat;
import com.ey.in.tcs.common.domain.dividend.Form15FileType;
import com.ey.in.tcs.common.domain.dividend.Form15FilingStatus;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.masters.DividendOnboardingInfoMetaData;
import com.ey.in.tds.common.onboarding.response.dto.DeductorOnboardingInformationResponseDTO;
import com.ey.in.tds.common.returns.jdbc.dto.Form15FilingDetails;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.ReturnType;
import com.ey.in.tds.dividend.forms.Form15Generator;
import com.ey.in.tds.dividend.forms.builder.Generatable;
import com.ey.in.tds.dividend.forms.builder.cb.RemitterBuilder;
import com.ey.in.tds.feign.client.IngestionClient;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.jdbc.dao.Form15FilingDetailsDAO;
import com.ey.in.tds.returns.dividend.validator.Errors;
import com.ey.in.tds.returns.dividend.validator.Validator;
import com.ey.in.tds.returns.dto.Form15CBDTO;
import com.ey.in.tds.returns.dto.Form15XmlUtil;
import com.microsoft.azure.storage.StorageException;

@Component
public class Form15CBXmlGenerationService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Filing15cbService filing15cbService;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private IngestionClient ingestionClient;

	// @Autowired
	// private Form15FilingDetailsRepository form15FilingDetailsRepository;

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	protected BlobStorage blobStorage;

	@Value("${report.io.dir.path}")
	private String basePath;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private Form15FilingDetailsDAO form15FilingDetailsDAO;

	@Autowired
	private DividendFilingCommonErrorFileService dividendFilingCommonErrorFileService;

	public static final SimpleDateFormat xmlFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final String FORM_15_CB_XSD = "Form15CB";

	@Transactional
	public String create15CBXmlFilingReport(String deductorTan, String deductorPan, String dateOfPosting,
			String tenantId, String userName, Integer assessmentYear) {
		logger.info("Filing 15CB XML: Started generation for TAN: {}, date of posting : {} ", deductorTan,
				dateOfPosting);

		this.filing15cbService.checkOnboardingInformation(deductorPan, tenantId);
		try {
			createFilingReportStatus(deductorTan, assessmentYear, dateOfPosting, Form15FileType.CB,
					Form15FileFormat.XML, ReturnType.REGULAR, userName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		CompletableFuture.runAsync(() -> this.generateForm15CB(deductorTan, deductorPan, dateOfPosting, tenantId,
				userName, assessmentYear));

		return "Request for generating Form 15CB XML submitted successfully";
	}

	private void generateForm15CB(String deductorTan, String deductorPan, String dateOfPosting, String tenantId,
			String userName, Integer assessmentYear) {
		logger.info("Entered into Async method to generate  15CB Excel file {}");
		MultiTenantContext.setTenantId(tenantId);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.executeWithoutResult((status) -> {
			String error = null;
			try {
				List<Errors> errorList = new ArrayList<>();
				List<Form15CBDTO> data = this.filing15cbService.generate15cbDtos(deductorTan, deductorPan,
						dateOfPosting, tenantId, userName, assessmentYear, errorList);
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
										"AS PER INCOME-TAX ACT")
								.dtaa(checkBoolean(dto.getRemittanceRecipientTRCAvailable()), dto.getRelevantDTAA(),
										dto.getRelevantDTAAArticle(), dto.getTaxableIncomePerDTAA(), dto.getTaxLiabilityAsPerDTAA(), checkBoolean(dto.getRemittanceForRoyalties()) ,
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
							deductorPan, errorList);
				} else {
					File errorFile = dividendFilingCommonErrorFileService.generateErrorFile(errorList);
					String url = blobStorage.uploadExcelToBlobWithFile(errorFile, tenantId);
					updateFilingReportStatus(deductorTan, assessmentYear, dateOfPosting, Form15FileType.CB,
							Form15FileFormat.XML, ReturnType.REGULAR, Form15FilingStatus.ERROR, url, "ERROR", userName);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void saveCBFormsToBlob(Map<File, String> consoliadtedFilesFor15CA, int assessmentYear, String tenantId,
			String deductorTan, String postingDate, String userName, String deductorPan, List<Errors> errorList)
			throws Exception {

		if (!consoliadtedFilesFor15CA.isEmpty()) {
			List<Errors> errors = new ArrayList<>();
			Iterator iterator = consoliadtedFilesFor15CA.entrySet().iterator();
			List<File> files = new ArrayList<File>();
			while (iterator.hasNext()) {
				Map.Entry<File, String> pair = (Map.Entry<File, String>) iterator.next();
				File file = (File) pair.getKey();
				List<Errors> errorsList = Validator.validate(file, getXSDPath("Form15CB"), pair.getValue());
				if (errorsList.size() < 0) {
					iterator.remove();
					errors.addAll(errorsList);
				} else {
					files.add(file);
				}
			}
			try {

				if (!errors.isEmpty()) {
					errors.forEach(n -> {
						n.setMessage(n.getCompleteMessage());
						n.setCompleteMessage("");
					});
					errorList.addAll(errors);
				}
				if (!errorList.isEmpty()) {
					File errorFile = dividendFilingCommonErrorFileService.generateErrorFile(errorList);
					// Form15XmlUtil.generateErrorFile(errors, Form15FileType.CB);
					files.add(errorFile);
				}
			} catch (Exception e) {
				logger.error("Could not generate form 15 CB", e);
			}
			try {
				String zipFileName = deductorTan + "_" + assessmentYear + "_15_CB_FORMS" + new Date().getTime()
						+ ".zip";
				File gFormsZip = Form15XmlUtil.zip(files, zipFileName);
				
				String blobUrl = this.blobStorage.uploadExcelToBlobWithFile(gFormsZip, tenantId);
				updateFilingReportStatus(deductorTan, assessmentYear, postingDate, Form15FileType.CB,
						Form15FileFormat.XML, ReturnType.REGULAR, Form15FilingStatus.GENERATED, blobUrl, "", userName);
			} catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
				logger.error("Could not generate form 15 CB", e);
			}

		}
	}

	private File getXSDPath(String type) throws URISyntaxException {
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
		if (!Objects.isNull(value)) {
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
			con = new BigDecimal(input != null && !input.equalsIgnoreCase("") && !input.equalsIgnoreCase("0")? input : "0.0");
		} catch (NumberFormatException e) {
			con = new BigDecimal("0.0");
		}
		return con;
	}

	public static String getCurrentDate() {
		return xmlFormat.format(new Date());
	}

	public Form15FilingDetails createFilingReportStatus(String deductorTan, Integer assessmentYear,
			String dateOfPosting, Form15FileType fileType, Form15FileFormat fileFormat, ReturnType returnType,
			String userName) {

		List<Form15FilingDetails> existingFilings = form15FilingDetailsDAO.findFilingFiles(deductorTan, assessmentYear,
				dateOfPosting, fileType, fileFormat);
		Form15FilingDetails filingReportStatus = existingFilings.isEmpty() ? null : existingFilings.get(0);

		Date today = new Date();
		if (filingReportStatus == null) {
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
			filingReportStatus.setFilingType(returnType);
			filingReportStatus.setFormFifteenFileType(fileType);
			filingReportStatus.setFormFifteenFileFormat(fileFormat);
			filingReportStatus.setStatus(Form15FilingStatus.PROCESSING);
			filingReportStatus.setIsRequested(true);
			filingReportStatus.setCreatedBy(userName);
			filingReportStatus.setCreatedDate(today);
			filingReportStatus.setAssessmentYear(assessmentYear);
			filingReportStatus.setDateOfPosting(dateOfPosting);

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
			filingReportStatus.setStatus(filingStatus);
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

}
