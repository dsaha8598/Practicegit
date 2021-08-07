package com.ey.in.tds.returns.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tcs.common.domain.dividend.Form15FileFormat;
import com.ey.in.tcs.common.domain.dividend.Form15FileType;
import com.ey.in.tcs.common.domain.dividend.Form15FilingStatus;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.FilingStateCode;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.ReturnType;
import com.ey.in.tds.dividend.common.Form15XmlUtil;
import com.ey.in.tds.dividend.forms.Form15Generator;
import com.ey.in.tds.dividend.forms.builder.g.Form15GBasicDetailsBuilder;
import com.ey.in.tds.dividend.forms.builder.g.ShareholderType;
import com.ey.in.tds.dividend.forms.builder.gh.AddMoreBuilder;
import com.ey.in.tds.dividend.forms.builder.gh.CorrectionType;
import com.ey.in.tds.dividend.forms.builder.gh.Quarter;
import com.ey.in.tds.dividend.forms.builder.h.Form15HBasicDetailsBuilder;
import com.ey.in.tds.feign.client.IngestionClient;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.jdbc.dao.Form15FilingDetailsDAO;
import com.ey.in.tds.returns.dividend.validator.Errors;
import com.ey.in.tds.returns.dividend.validator.Validator;
import com.ey.in.tds.returns.dto.Form15GDTO;
import com.ey.in.tds.returns.dto.Form15HDTO;
import com.microsoft.azure.storage.StorageException;

@Component
public class Form15XmlGenerationService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Filing15cbService filing15cbService;

	@Autowired
	private Filing15caService filing15caService;

	@Autowired
	private Filing15GService filing15GService;

	@Autowired
	private Filing15HService filing15HService;

	@Autowired
	private IngestionClient ingestionClient;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private Form15FilingDetailsDAO form15FilingDetailsDAO;

	@Autowired
	protected BlobStorage blobStorage;

	@Autowired
	private DividendFilingCommonErrorFileService dividendFilingCommonErrorFileService;

	@Autowired
	private PlatformTransactionManager transactionManager;

	// @Value("${report.io.dir.path}")
	private String basePath;

	@Transactional
	public String create15GXmlFilingReport(String deductorTan, String deductorPan, Quarter quarter, String tenantId,
			String userName, int assessmentYear) {
		logger.info("Filing 15G XML: Started generation for TAN: {}, Quarter : {} ", deductorTan, quarter);
		try {
			form15FilingDetailsDAO.createFilingReportStatus(deductorTan, assessmentYear,
					quarter.startingDate(assessmentYear), Form15FileType.G, Form15FileFormat.XML, ReturnType.REGULAR,
					userName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		CompletableFuture.runAsync(
				() -> this.generateForm15G(deductorTan, deductorPan, quarter, tenantId, userName, assessmentYear));

		return "Request for generating Form 15G XML submitted successfully";
	}

	private void generateForm15G(String deductorTan, String deductorPan, Quarter quarter, String tenantId,
			String userName, int assessmentYear) {
		MultiTenantContext.setTenantId(tenantId);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.executeWithoutResult((status) -> {
			try {
				List<Errors> errors = new ArrayList<>();
				List<Form15GDTO> form15GDTOs = this.filing15GService.generate15GDTOs(deductorTan, deductorPan,
						quarter.name(), tenantId, userName, assessmentYear, errors);
				if (!form15GDTOs.isEmpty()) {

					List<Form15GDTO> original15GDTOs = new ArrayList<>();
					List<Form15GDTO> correction15GDTOs = new ArrayList<>();

					List<File> files = new ArrayList<>();

					List<Form15GDTO> validatedOriginal15GDTOs;
					List<Form15GDTO> validatedCorrection15GDTOs;

					form15GDTOs.stream().filter(Objects::nonNull).forEach(dto -> {
						if (StringUtils.isNotBlank(dto.getFilingType()) &&  dto.getFilingType().equalsIgnoreCase("Correction")) {
							correction15GDTOs.add(dto);
						} else {
							original15GDTOs.add(dto);
						}
					});
					validatedOriginal15GDTOs = validateOriginal15Gdtos(original15GDTOs, errors, deductorTan,
							deductorPan, quarter, tenantId, userName, assessmentYear);
					validatedCorrection15GDTOs = validateCorrection15Gdtos(correction15GDTOs, errors, deductorTan,
							deductorPan, quarter, tenantId, userName, assessmentYear);

					Map<String, List<Form15GDTO>> correctionMap = validatedCorrection15GDTOs.stream()
							.collect(Collectors.groupingBy(Form15GDTO::getAcknowledgementNumber, Collectors.toList()));

					if (!validatedOriginal15GDTOs.isEmpty()) {

						String fileName = Form15XmlUtil.randomSuffixedFileName(Form15XmlUtil.FORM_15_G_FILE_PREFIX);
						Form15GBasicDetailsBuilder form15GBasicDetailsBuilder = Form15Generator
								.form15G(assessmentYear, fileName)
								.original(deductorTan, quarter, assessmentYear - 1, null);
						AddMoreBuilder<Form15GBasicDetailsBuilder> builder = null;

						for (Form15GDTO form15GDTO : validatedOriginal15GDTOs) {
							builder = form15GBasicDetailsBuilder
									.basicDetails(form15GDTO.getAssesseeName(), form15GDTO.getAssesseePan(),
											ShareholderType.byShareholderType(form15GDTO.getAssesseeStatus()),
											form15GDTO.getPreviousYearOfDeclaration(), form15GDTO.getEmail(), null,
											form15GDTO.getTelephoneNumber(), form15GDTO.getMobileNumber())
									.address(form15GDTO.getFlatDoorBlockNo(), form15GDTO.getRoadStreetPostoffice(),
											form15GDTO.getNameBuildingVillage(), form15GDTO.getTownCityDistrict(),
											form15GDTO.getAreaLocality(), form15GDTO.getPinCode(),
											form15GDTO.getState())
									.taxDetails(form15GDTO.isAssessedToTax(), form15GDTO.getAssessedYear(),
											form15GDTO.getDeclaredIncome(),
											form15GDTO.getTotalIncomeWhereDeclaredIncomeIncluded(),
											form15GDTO.getNumberOf15GFiled(), form15GDTO.getAggregateIncomeOf15GFiled(),
											form15GDTO.getDeclarationDate(), form15GDTO.getIncomePaid(),
											form15GDTO.getIncomePaidDate())
									.incomeDetails(form15GDTO.getFolioNo().toString(),
											form15GDTO.getFolioNo().toString(),
											form15GDTO.getNatureOfIncome(), form15GDTO.getSectionOfAct(),
											form15GDTO.getAmountOfIncome());
						}

						File file = builder.generate();
						files.add(file);
					}
					if (!validatedCorrection15GDTOs.isEmpty()) {

						for (Map.Entry<String, List<Form15GDTO>> entry : correctionMap.entrySet()) {
							List<Form15GDTO> form15GDTOS = entry.getValue();

							String fileName = Form15XmlUtil.randomSuffixedFileName(Form15XmlUtil.FORM_15_G_FILE_PREFIX);
							Form15GBasicDetailsBuilder form15GBasicDetailsBuilder = Form15Generator
									.form15G(assessmentYear, fileName).correction(deductorTan, quarter,
											assessmentYear - 1, form15GDTOS.get(0).getAcknowledgementNumber(),
											StringUtils.isBlank(form15GDTOS.get(0).getRecordType())?null:CorrectionType.valueOf(form15GDTOS.get(0).getRecordType())
											);
							//CorrectionType.valueOf(StringUtils.isBlank(form15GDTOS.get(0).getRecordType())?"ADDITION":form15GDTOS.get(0).getRecordType().toUpperCase())
							AddMoreBuilder<Form15GBasicDetailsBuilder> builder = null;

							for (Form15GDTO form15GDTO : form15GDTOS) {
								builder = form15GBasicDetailsBuilder
										.basicDetails(form15GDTO.getAssesseeName(), form15GDTO.getAssesseePan(),
												ShareholderType.byShareholderType(form15GDTO.getAssesseeStatus()),
												form15GDTO.getPreviousYearOfDeclaration(), form15GDTO.getEmail(), null,
												form15GDTO.getTelephoneNumber(), form15GDTO.getMobileNumber())
										.address(form15GDTO.getFlatDoorBlockNo(), form15GDTO.getRoadStreetPostoffice(),
												form15GDTO.getNameBuildingVillage(), form15GDTO.getTownCityDistrict(),
												form15GDTO.getAreaLocality(), form15GDTO.getPinCode(),
												form15GDTO.getState())
										.taxDetails(form15GDTO.isAssessedToTax(), form15GDTO.getAssessedYear(),
												form15GDTO.getDeclaredIncome(),
												form15GDTO.getTotalIncomeWhereDeclaredIncomeIncluded(),
												form15GDTO.getNumberOf15GFiled(),
												form15GDTO.getAggregateIncomeOf15GFiled(),
												form15GDTO.getDeclarationDate(), form15GDTO.getIncomePaid(),
												form15GDTO.getIncomePaidDate())
										.incomeDetails(form15GDTO.getFolioNo().toString(),
												form15GDTO.getFolioNo().toString(),
												form15GDTO.getNatureOfIncome(), form15GDTO.getSectionOfAct(),
												form15GDTO.getAmountOfIncome());
							}
							File file = builder.generate();
							files.add(file);
						}
					}

					if (!errors.isEmpty()) {
						File errorFile = dividendFilingCommonErrorFileService.generateErrorFile(errors);
						// generateErrorFile(errors, Form15FileType.G);
						files.add(errorFile);
					}

					if (!files.isEmpty()) {
						try {
							String zipFileName = quarter + "_15_G_FORMS";
							File gFormsZip = Form15XmlUtil.zip(files, zipFileName);

							File blobFile = new File(zipFileName + new Date().getTime() + ".zip");
							try (InputStream inputstream = new FileInputStream(gFormsZip);) {
								java.nio.file.Files.copy(inputstream, blobFile.toPath(),
										StandardCopyOption.REPLACE_EXISTING);
								String blobUrl = this.blobStorage.uploadExcelToBlobWithFile(blobFile, tenantId);
								form15FilingDetailsDAO.updateFilingReportStatus(deductorTan, assessmentYear,
										quarter.startingDate(assessmentYear), Form15FileType.G, Form15FileFormat.XML,
										ReturnType.REGULAR, Form15FilingStatus.GENERATED, blobUrl, "", userName);
							}
						} catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
							logger.error("Could not generate form 15 G", e);
						}
					}
				} else {
					File errorFile = dividendFilingCommonErrorFileService.generateErrorFile(errors);
					String url = blobStorage.uploadExcelToBlobWithFile(errorFile, tenantId);
					form15FilingDetailsDAO.updateFilingReportStatus(deductorTan, assessmentYear,
							quarter.startingDate(assessmentYear), Form15FileType.G, Form15FileFormat.XML,
							ReturnType.REGULAR, Form15FilingStatus.ERROR, url,
							"No Data found for given Quarter: " + quarter, userName);
				}
			} catch (Exception e) {
				logger.error("Error occured while Generating Form 15G XML", e);
				form15FilingDetailsDAO.updateFilingReportStatus(deductorTan, assessmentYear,
						quarter.startingDate(assessmentYear), Form15FileType.G, Form15FileFormat.EXCEL,
						ReturnType.REGULAR, Form15FilingStatus.ERROR, null, e.getMessage(), userName);
			}
		});
	}

	private List<Form15GDTO> validateCorrection15Gdtos(List<Form15GDTO> correction15GDTOs, List<Errors> errors,
			String deductorTan, String deductorPan, Quarter quarter, String tenantId, String userName,
			int assessmentYear) {

		List<Form15GDTO> validated = new ArrayList<>();
		ResponseEntity<ApiStatus<List<FilingStateCode>>> allStateCode = mastersClient.getAllStateCode(tenantId);
		List<FilingStateCode> filingStateCodes = allStateCode.getBody().getData();

		if (!correction15GDTOs.isEmpty()) {
			for (Form15GDTO form15GDTO : correction15GDTOs) {
				boolean valid = true;

				String fileName = Form15XmlUtil.randomSuffixedFileName(Form15XmlUtil.FORM_15_G_FILE_PREFIX);
				Form15GBasicDetailsBuilder form15GBasicDetailsBuilder = Form15Generator
						.form15G(assessmentYear, fileName).correction(deductorTan, quarter, assessmentYear,
								form15GDTO.getAcknowledgementNumber(), StringUtils.isBlank(form15GDTO.getRecordType())?null:CorrectionType.valueOf(form15GDTO.getRecordType()));
				AddMoreBuilder<Form15GBasicDetailsBuilder> builder = null;
				Optional<FilingStateCode> stateCode = filingStateCodes.stream().filter(
						filingStateCode -> filingStateCode.getStateName().equalsIgnoreCase(form15GDTO.getState()))
						.findAny();
				FilingStateCode filingStateCode = null;
				if (stateCode.isPresent()) {
					filingStateCode = stateCode.get();
					if (!Objects.isNull(filingStateCode.getStateCode())) {
						if (filingStateCode.getStateCode().length() == 1)
							filingStateCode.setStateCode("0".concat(filingStateCode.getStateCode()));
					}
					form15GDTO.setState(filingStateCode.getStateCode());
				} else {
					form15GDTO.setState(null);
				}
				builder = form15GBasicDetailsBuilder
						.basicDetails(form15GDTO.getAssesseeName(), form15GDTO.getAssesseePan(),
								ShareholderType.byShareholderType(form15GDTO.getAssesseeStatus()),
								form15GDTO.getPreviousYearOfDeclaration(), form15GDTO.getEmail(), null,
								form15GDTO.getTelephoneNumber(), form15GDTO.getMobileNumber())
						.address(form15GDTO.getFlatDoorBlockNo(), form15GDTO.getRoadStreetPostoffice(),
								form15GDTO.getNameBuildingVillage(), form15GDTO.getTownCityDistrict(),
								form15GDTO.getAreaLocality(), form15GDTO.getPinCode(), form15GDTO.getState())
						.taxDetails(form15GDTO.isAssessedToTax(), form15GDTO.getAssessedYear(),
								form15GDTO.getDeclaredIncome(), form15GDTO.getTotalIncomeWhereDeclaredIncomeIncluded(),
								form15GDTO.getNumberOf15GFiled(), form15GDTO.getAggregateIncomeOf15GFiled(),
								form15GDTO.getDeclarationDate(), form15GDTO.getIncomePaid(),
								form15GDTO.getIncomePaidDate())
						.incomeDetails(form15GDTO.getIdentificationNumber().toString(),
								form15GDTO.getIdentificationNumber().toString(), form15GDTO.getNatureOfIncome(),
								form15GDTO.getSectionOfAct(), form15GDTO.getAmountOfIncome());
				File file = builder.generate();
				try {
					List<Errors> form15G = Validator.validate(file, getXSDPath("Form15G"), form15GDTO.getFolioNo());
					if (form15G.size() > 0) {
						valid = false;
					}
					errors.addAll(form15G);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (valid) {
					validated.add(form15GDTO);
				}
			}
		}
		return validated;
	}

	private List<Form15GDTO> validateOriginal15Gdtos(List<Form15GDTO> original15GDTOs, List<Errors> errors,
			String deductorTan, String deductorPan, Quarter quarter, String tenantId, String userName,
			int assessmentYear) {
		List<Form15GDTO> validated = new ArrayList<>();
		ResponseEntity<ApiStatus<List<FilingStateCode>>> allStateCode = mastersClient.getAllStateCode(tenantId);
		List<FilingStateCode> filingStateCodes = allStateCode.getBody().getData();
		if (!original15GDTOs.isEmpty()) {
			for (Form15GDTO form15GDTO : original15GDTOs) {
				boolean valid = true;
				String fileName = Form15XmlUtil.randomSuffixedFileName(Form15XmlUtil.FORM_15_G_FILE_PREFIX);
				Form15GBasicDetailsBuilder form15GBasicDetailsBuilder = Form15Generator
						.form15G(assessmentYear, fileName).original(deductorTan, quarter, assessmentYear, null);
				AddMoreBuilder<Form15GBasicDetailsBuilder> builder = null;
				Optional<FilingStateCode> stateCode = filingStateCodes.stream().filter(
						filingStateCode1 -> filingStateCode1.getStateName().equalsIgnoreCase(form15GDTO.getState()))
						.findAny();
				FilingStateCode filingStateCode = null;
				if (stateCode.isPresent()) {
					filingStateCode = stateCode.get();
					if (!Objects.isNull(filingStateCode.getStateCode())) {
						if (filingStateCode.getStateCode().length() == 1)
							filingStateCode.setStateCode("0".concat(filingStateCode.getStateCode()));
					}
					form15GDTO.setState(filingStateCode.getStateCode());
				} else {
					form15GDTO.setState(null);
				}

				// FilingStateCode filingStateCode = mastersClient.getStateCodeByName(tenantId,
				// form15GDTO.getState()).getBody().getData();

				builder = form15GBasicDetailsBuilder
						.basicDetails(form15GDTO.getAssesseeName(), form15GDTO.getAssesseePan(),
								ShareholderType.byShareholderType(form15GDTO.getAssesseeStatus()),
								form15GDTO.getPreviousYearOfDeclaration(), form15GDTO.getEmail(), null,
								form15GDTO.getTelephoneNumber(), form15GDTO.getMobileNumber())
						.address(form15GDTO.getFlatDoorBlockNo(), form15GDTO.getRoadStreetPostoffice(),
								form15GDTO.getNameBuildingVillage(), form15GDTO.getTownCityDistrict(),
								form15GDTO.getAreaLocality(), form15GDTO.getPinCode(), form15GDTO.getState())
						.taxDetails(form15GDTO.isAssessedToTax(), form15GDTO.getAssessedYear(),
								form15GDTO.getDeclaredIncome(), form15GDTO.getTotalIncomeWhereDeclaredIncomeIncluded(),
								form15GDTO.getNumberOf15GFiled(), form15GDTO.getAggregateIncomeOf15GFiled(),
								form15GDTO.getDeclarationDate(), form15GDTO.getIncomePaid(),
								form15GDTO.getIncomePaidDate())
						.incomeDetails(form15GDTO.getIdentificationNumber().toString(),
								form15GDTO.getIdentificationNumber().toString(), form15GDTO.getNatureOfIncome(),
								form15GDTO.getSectionOfAct(), form15GDTO.getAmountOfIncome());

				File file = builder.generate();
				try {
					List<Errors> form15G = Validator.validate(file, getXSDPath("Form15G"), form15GDTO.getFolioNo());
					if (form15G.size() > 0) {
						valid = false;
					}
					errors.addAll(form15G);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (valid) {
					validated.add(form15GDTO);
				}
			}
		}
		return validated;
	}

	@Transactional
	public String create15HXmlFilingReport(String deductorTan, String deductorPan, Quarter quarter, String tenantId,
			String userName, int assessmentYear) {
		logger.info("Filing 15H XML: Started generation for TAN: {}, Quarter : {} ", deductorTan, quarter);
		try {
			form15FilingDetailsDAO.createFilingReportStatus(deductorTan, assessmentYear,
					quarter.startingDate(assessmentYear), Form15FileType.H, Form15FileFormat.XML, ReturnType.REGULAR,
					userName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		CompletableFuture.runAsync(
				() -> this.generateForm15H(deductorTan, deductorPan, quarter, tenantId, userName, assessmentYear));

		return "Request for generating Form 15H XML submitted successfully";
	}

	private void generateForm15H(String deductorTan, String deductorPan, Quarter quarter, String tenantId,
			String userName, int assessmentYear) {
		MultiTenantContext.setTenantId(tenantId);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.executeWithoutResult((status) -> {
			try {
				List<Errors> errors = new ArrayList<>();
				List<Form15HDTO> form15HDTOs = filing15HService.generate15HDTOs(deductorTan, deductorPan,
						quarter.name(), tenantId, userName, assessmentYear, errors);
				if (!form15HDTOs.isEmpty()) {

					List<Form15HDTO> validatedOriginal15HDTOs = new ArrayList<>();
					List<Form15HDTO> validatedCorrection15HDTOs = new ArrayList<>();

					List<Form15HDTO> original15HDTOs = new ArrayList<>();
					List<Form15HDTO> correction15HDTOs = new ArrayList<>();
					List<File> files = new ArrayList<>();

					form15HDTOs.stream().filter(Objects::nonNull).forEach(dto -> {
						if (dto.getFilingType().equalsIgnoreCase("Correction")) {
							correction15HDTOs.add(dto);
						} else {
							original15HDTOs.add(dto);
						}
					});
					validatedOriginal15HDTOs = validateOriginal15HDtos(original15HDTOs, errors, deductorTan,
							deductorPan, quarter, tenantId, userName, assessmentYear);
					validatedCorrection15HDTOs = validateCorrection15HDtos(correction15HDTOs, errors, deductorTan,
							deductorPan, quarter, tenantId, userName, assessmentYear);

					Map<String, List<Form15HDTO>> correctionMap = validatedCorrection15HDTOs.stream()
							.collect(Collectors.groupingBy(Form15HDTO::getAcknowledgementNumber, Collectors.toList()));

					if (!validatedOriginal15HDTOs.isEmpty()) {

						String fileName = Form15XmlUtil.randomSuffixedFileName(Form15XmlUtil.FORM_15_H_FILE_PREFIX);
						Form15HBasicDetailsBuilder Form15HBasicDetailsBuilder = Form15Generator
								.form15H(assessmentYear, fileName)
								.original(deductorTan, quarter, assessmentYear - 1, null);
						AddMoreBuilder<Form15HBasicDetailsBuilder> builder = null;
						for (Form15HDTO form15HDTO : validatedOriginal15HDTOs) {
							builder = Form15HBasicDetailsBuilder
									.basicDetails(form15HDTO.getAssesseeName(), form15HDTO.getAssesseePan(),
											form15HDTO.getDateOfBirth(), form15HDTO.getPreviousYearOfDeclaration(),
											form15HDTO.getEmail(), null, form15HDTO.getTelephoneNumber(),
											form15HDTO.getMobileNumber())
									.address(form15HDTO.getFlatDoorBlockNo(), form15HDTO.getRoadStreetPostoffice(),
											form15HDTO.getNameBuildingVillage(), form15HDTO.getTownCityDistrict(),
											form15HDTO.getAreaLocality(), form15HDTO.getPinCode(),
											form15HDTO.getState())
									.taxDetails(form15HDTO.isAssessedToTax(), form15HDTO.getAssessedYear(),
											form15HDTO.getDeclaredIncome(),
											form15HDTO.getTotalIncomeWhereDeclaredIncomeIncluded(),
											form15HDTO.getNumberOf15HFiled(), form15HDTO.getAggregateIncomeOf15HFiled(),
											form15HDTO.getDeclarationDate(), form15HDTO.getIncomePaid(),
											form15HDTO.getIncomePaidDate())
									.incomeDetails(form15HDTO.getFolioNo().toString(),
											form15HDTO.getIdentificationNumber().toString(),
											form15HDTO.getNatureOfIncome(), form15HDTO.getSectionOfAct(),
											form15HDTO.getAmountOfIncome());
						}

						File file = builder.generate();
						files.add(file);
					}
					if (!validatedCorrection15HDTOs.isEmpty()) {

						for (Map.Entry<String, List<Form15HDTO>> entry : correctionMap.entrySet()) {
							List<Form15HDTO> form15HDTOS = entry.getValue();
							String fileName = Form15XmlUtil.randomSuffixedFileName(Form15XmlUtil.FORM_15_H_FILE_PREFIX);
							Form15HBasicDetailsBuilder Form15HBasicDetailsBuilder = Form15Generator
									.form15H(assessmentYear, fileName).correction(deductorTan, quarter,
											assessmentYear - 1, form15HDTOS.get(0).getAcknowledgementNumber(),
											StringUtils.isBlank(form15HDTOS.get(0).getRecordType())?null:CorrectionType.valueOf(form15HDTOS.get(0).getRecordType()));
							AddMoreBuilder<Form15HBasicDetailsBuilder> builder = null;

							for (Form15HDTO form15HDTO : form15HDTOS) {
								builder = Form15HBasicDetailsBuilder
										.basicDetails(form15HDTO.getAssesseeName(), form15HDTO.getAssesseePan(),
												form15HDTO.getDateOfBirth(), form15HDTO.getPreviousYearOfDeclaration(),
												form15HDTO.getEmail(), null, form15HDTO.getTelephoneNumber(),
												form15HDTO.getMobileNumber())
										.address(form15HDTO.getFlatDoorBlockNo(), form15HDTO.getRoadStreetPostoffice(),
												form15HDTO.getNameBuildingVillage(), form15HDTO.getTownCityDistrict(),
												form15HDTO.getAreaLocality(), form15HDTO.getPinCode(),
												form15HDTO.getState())
										.taxDetails(form15HDTO.isAssessedToTax(), form15HDTO.getAssessedYear(),
												form15HDTO.getDeclaredIncome(),
												form15HDTO.getTotalIncomeWhereDeclaredIncomeIncluded(),
												form15HDTO.getNumberOf15HFiled(),
												form15HDTO.getAggregateIncomeOf15HFiled(),
												form15HDTO.getDeclarationDate(), form15HDTO.getIncomePaid(),
												form15HDTO.getIncomePaidDate())
										.incomeDetails(form15HDTO.getIdentificationNumber().toString(),
												form15HDTO.getIdentificationNumber().toString(),
												form15HDTO.getNatureOfIncome(), form15HDTO.getSectionOfAct(),
												form15HDTO.getAmountOfIncome());
							}
							File file = builder.generate();
							files.add(file);
						}
					}
					if (!errors.isEmpty()) {
						File errorFile = dividendFilingCommonErrorFileService.generateErrorFile(errors);
						files.add(errorFile);
					}

					if (!files.isEmpty()) {
						try {
							String zipFileName = quarter + "_15_H_FORMS";
							File hFormsZip = Form15XmlUtil.zip(files, zipFileName);

							File blobFile = new File(zipFileName + new Date().getTime() + ".zip");
							try (InputStream inputstream = new FileInputStream(hFormsZip);) {

								java.nio.file.Files.copy(inputstream, blobFile.toPath(),
										StandardCopyOption.REPLACE_EXISTING);

								String blobUrl = this.blobStorage.uploadExcelToBlobWithFile(blobFile, tenantId);
								form15FilingDetailsDAO.updateFilingReportStatus(deductorTan, assessmentYear,
										quarter.startingDate(assessmentYear), Form15FileType.H, Form15FileFormat.XML,
										ReturnType.REGULAR, Form15FilingStatus.GENERATED, blobUrl, "", userName);
							}
						} catch (IOException | InvalidKeyException | URISyntaxException | StorageException e) {
							logger.error("Could not generate form 15 H", e);
						}
					}
				} else {
					File errorFile = dividendFilingCommonErrorFileService.generateErrorFile(errors);
					String url = blobStorage.uploadExcelToBlobWithFile(errorFile, tenantId);
					form15FilingDetailsDAO.updateFilingReportStatus(deductorTan, assessmentYear,
							quarter.startingDate(assessmentYear), Form15FileType.H, Form15FileFormat.XML,
							ReturnType.REGULAR, Form15FilingStatus.ERROR, url,
							"No Data found for given Quarter: " + quarter, userName);
				}
			} catch (Exception e) {
				logger.error("Error occured while Generating Form 15H XML", e);
				form15FilingDetailsDAO.updateFilingReportStatus(deductorTan, assessmentYear,
						quarter.startingDate(assessmentYear), Form15FileType.H, Form15FileFormat.EXCEL,
						ReturnType.REGULAR, Form15FilingStatus.ERROR, null, e.getMessage(), userName);
			}
		});
	}

	private List<Form15HDTO> validateCorrection15HDtos(List<Form15HDTO> correction15HDTOs, List<Errors> errors,
			String deductorTan, String deductorPan, Quarter quarter, String tenantId, String userName,
			int assessmentYear) {
		List<Form15HDTO> validated = new ArrayList<>();
		ResponseEntity<ApiStatus<List<FilingStateCode>>> allStateCode = mastersClient.getAllStateCode(tenantId);
		List<FilingStateCode> filingStateCodes = allStateCode.getBody().getData();
		if (!correction15HDTOs.isEmpty()) {
			for (Form15HDTO form15HDTO : correction15HDTOs) {
				boolean valid = true;
				String fileName = Form15XmlUtil.randomSuffixedFileName(Form15XmlUtil.FORM_15_H_FILE_PREFIX);
				Form15HBasicDetailsBuilder Form15HBasicDetailsBuilder = Form15Generator
						.form15H(assessmentYear, fileName).correction(deductorTan, quarter, assessmentYear,
								form15HDTO.getAcknowledgementNumber(),StringUtils.isBlank(form15HDTO.getRecordType())?null:CorrectionType.valueOf(form15HDTO.getRecordType()));
				AddMoreBuilder<Form15HBasicDetailsBuilder> builder = null;

				Optional<FilingStateCode> stateCode = filingStateCodes.stream().filter(
						filingStateCode -> filingStateCode.getStateName().equalsIgnoreCase(form15HDTO.getState()))
						.findAny();
				FilingStateCode filingStateCode = null;
				if (stateCode.isPresent()) {
					filingStateCode = stateCode.get();
					if (!Objects.isNull(filingStateCode.getStateCode())) {
						if (filingStateCode.getStateCode().length() == 1)
							filingStateCode.setStateCode("0".concat(filingStateCode.getStateCode()));
					}
					form15HDTO.setState(filingStateCode.getStateCode());
				} else {
					form15HDTO.setState(null);
				}
				builder = Form15HBasicDetailsBuilder
						.basicDetails(form15HDTO.getAssesseeName(), form15HDTO.getAssesseePan(),
								form15HDTO.getDateOfBirth(), form15HDTO.getPreviousYearOfDeclaration(),
								form15HDTO.getEmail(), null, form15HDTO.getTelephoneNumber(),
								form15HDTO.getMobileNumber())
						.address(form15HDTO.getFlatDoorBlockNo(), form15HDTO.getRoadStreetPostoffice(),
								form15HDTO.getNameBuildingVillage(), form15HDTO.getTownCityDistrict(),
								form15HDTO.getAreaLocality(), form15HDTO.getPinCode(), form15HDTO.getState())
						.taxDetails(form15HDTO.isAssessedToTax(), form15HDTO.getAssessedYear(),
								form15HDTO.getDeclaredIncome(), form15HDTO.getTotalIncomeWhereDeclaredIncomeIncluded(),
								form15HDTO.getNumberOf15HFiled(), form15HDTO.getAggregateIncomeOf15HFiled(),
								form15HDTO.getDeclarationDate(), form15HDTO.getIncomePaid(),
								form15HDTO.getIncomePaidDate())
						.incomeDetails(form15HDTO.getIdentificationNumber().toString(),
								form15HDTO.getIdentificationNumber().toString(), form15HDTO.getNatureOfIncome(),
								form15HDTO.getSectionOfAct(), form15HDTO.getAmountOfIncome());
				File file = builder.generate();
				try {
					List<Errors> form15H = Validator.validate(file, getXSDPath("Form15H"), form15HDTO.getFolioNo());
					if (form15H.size() > 0) {
						valid = false;
					}
					errors.addAll(form15H);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (valid) {
					validated.add(form15HDTO);
				}
			}
		}
		return validated;
	}

	private List<Form15HDTO> validateOriginal15HDtos(List<Form15HDTO> original15HDTOs, List<Errors> errors,
			String deductorTan, String deductorPan, Quarter quarter, String tenantId, String userName,
			int assessmentYear) {
		List<Form15HDTO> validated = new ArrayList<>();
		ResponseEntity<ApiStatus<List<FilingStateCode>>> allStateCode = mastersClient.getAllStateCode(tenantId);
		List<FilingStateCode> filingStateCodes = allStateCode.getBody().getData();
		if (!original15HDTOs.isEmpty()) {
			for (Form15HDTO form15HDTO : original15HDTOs) {
				boolean valid = true;
				String fileName = Form15XmlUtil.randomSuffixedFileName(Form15XmlUtil.FORM_15_H_FILE_PREFIX);
				Form15HBasicDetailsBuilder Form15HBasicDetailsBuilder = Form15Generator
						.form15H(assessmentYear, fileName).original(deductorTan, quarter, assessmentYear, null);
				AddMoreBuilder<Form15HBasicDetailsBuilder> builder = null;

				Optional<FilingStateCode> stateCode = filingStateCodes.stream().filter(
						filingStateCode -> filingStateCode.getStateName().equalsIgnoreCase(form15HDTO.getState()))
						.findAny();
				FilingStateCode filingStateCode = null;
				if (stateCode.isPresent()) {
					filingStateCode = stateCode.get();
					if (!Objects.isNull(filingStateCode.getStateCode())) {
						if (filingStateCode.getStateCode().length() == 1)
							filingStateCode.setStateCode("0".concat(filingStateCode.getStateCode()));
					}
					form15HDTO.setState(filingStateCode.getStateCode());
				} else {
					form15HDTO.setState(null);
				}
				builder = Form15HBasicDetailsBuilder
						.basicDetails(form15HDTO.getAssesseeName(), form15HDTO.getAssesseePan(),
								form15HDTO.getDateOfBirth(), form15HDTO.getPreviousYearOfDeclaration(),
								form15HDTO.getEmail(), null, form15HDTO.getTelephoneNumber(),
								form15HDTO.getMobileNumber())
						.address(form15HDTO.getFlatDoorBlockNo(), form15HDTO.getRoadStreetPostoffice(),
								form15HDTO.getNameBuildingVillage(), form15HDTO.getTownCityDistrict(),
								form15HDTO.getAreaLocality(), form15HDTO.getPinCode(), form15HDTO.getState())
						.taxDetails(form15HDTO.isAssessedToTax(), form15HDTO.getAssessedYear(),
								form15HDTO.getDeclaredIncome(), form15HDTO.getTotalIncomeWhereDeclaredIncomeIncluded(),
								form15HDTO.getNumberOf15HFiled(), form15HDTO.getAggregateIncomeOf15HFiled(),
								form15HDTO.getDeclarationDate(), form15HDTO.getIncomePaid(),
								form15HDTO.getIncomePaidDate())
						.incomeDetails(form15HDTO.getIdentificationNumber().toString(),
								form15HDTO.getIdentificationNumber().toString(), form15HDTO.getNatureOfIncome(),
								form15HDTO.getSectionOfAct(), form15HDTO.getAmountOfIncome());

				File file = builder.generate();
				try {
					List<Errors> form15H = Validator.validate(file, getXSDPath("Form15H"), form15HDTO.getFolioNo());
					if (form15H.size() > 0) {
						valid = false;
					}
					errors.addAll(form15H);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (valid) {
					validated.add(form15HDTO);
				}
			}
		}
		return validated;
	}

	public File generateErrorFile(List<Errors> errors, Form15FileType fileType) throws Exception {
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error File");
		String[] mainHeaderNames = new String[] { "Error Id", "Error Message", "Error Type", "Complete Error Message" };
		worksheet.getCells().importArray(mainHeaderNames, 0, 0, false);
		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (!errors.isEmpty()) {
			int rowIndex = 1;
			for (Errors error : errors) {
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add(StringUtils.isBlank(error.getId()) ? StringUtils.EMPTY : error.getId());
				rowData.add(StringUtils.isBlank(error.getMessage()) ? StringUtils.EMPTY : error.getMessage());
				rowData.add(StringUtils.isBlank(error.getType().getErrorType()) ? StringUtils.EMPTY
						: error.getType().getErrorType());

				rowData.add(StringUtils.isBlank(error.getCompleteMessage()) ? StringUtils.EMPTY
						: error.getCompleteMessage());
				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}

		Cell a1 = worksheet.getCells().get("A1");
		Style style1 = a1.getStyle();
		style1.setForegroundColor(Color.fromArgb(157, 195, 230));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		a1.setStyle(style1);

		Cell a2 = worksheet.getCells().get("B1");
		Style style2 = a2.getStyle();
		style2.setForegroundColor(Color.fromArgb(157, 195, 230));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		a2.setStyle(style2);

		Cell a3 = worksheet.getCells().get("C1");
		Style style3 = a3.getStyle();
		style3.setForegroundColor(Color.fromArgb(157, 195, 230));
		style3.setPattern(BackgroundType.SOLID);
		style3.getFont().setBold(true);
		style3.setHorizontalAlignment(TextAlignmentType.CENTER);
		a3.setStyle(style3);

		Cell a4 = worksheet.getCells().get("D1");
		Style style4 = a4.getStyle();
		style4.setForegroundColor(Color.fromArgb(157, 195, 230));
		style4.setPattern(BackgroundType.SOLID);
		style4.getFont().setBold(true);
		style4.setHorizontalAlignment(TextAlignmentType.CENTER);
		a4.setStyle(style4);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(true);

		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File errorFile = new File(FilenameUtils.getBaseName("Form 15") + fileType.toString() + "_"
				+ new Date().getTime() + "_Error_Report.xlsx");
		FileUtils.writeByteArrayToFile(errorFile, baout.toByteArray());
		baout.close();
		return errorFile;
	}

	private File getXSDPath(String type) throws URISyntaxException {
//        URL res = getClass().getClassLoader().getResource("xsd/" + type + ".xsd");
//        File file = Paths.get(res.toURI()).toFile();
//        return file;
		File file = new File(basePath + "/xsd/" + type + ".xsd");
		return file;
	}
}