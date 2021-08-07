package com.ey.in.tds.onboarding.service.kyc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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
import com.ey.in.tds.common.config.RedisUtil;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.model.deductee.KYCDetailsErrorFilDTO;
import com.ey.in.tds.common.model.emailnotification.Email;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeclarationTanFiles;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeclarationTanFilesDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDeclarationEmailDetails;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetails;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetailsControlOutput;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetailsDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetailsFormDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCHistory;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCRedisDetails;
import com.ey.in.tds.common.onboarding.jdbc.dto.KycRedisDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.PerferencsDTO;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.service.EmailService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.RedisKeys;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.jdbc.dao.DeclarationTanFilesDAO;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.jdbc.dao.KYCDeclarationEmailDetailsDAO;
import com.ey.in.tds.jdbc.dao.KYCDetailsDAO;
import com.ey.in.tds.jdbc.dao.KYCHistoryDAO;
import com.ey.in.tds.jdbc.dao.KYCRedisDetailsDAO;
import com.ey.in.tds.onboarding.service.util.excel.deductee.CustomerKYCDetailsExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.ShareholderKYCDetalsExcle;
import com.ey.in.tds.onboarding.service.util.excel.deductee.VendorKYCDetailsExcle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

/**
 *
 * @author vamsir
 *
 */
@Service
public class KYCDetailsService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private KYCDetailsDAO kycDetailsDAO;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	@Value("${tds.from-email}")
	private String mailsentfrom;
	
	@Value("${spring.servlet.multipart.max-file-size}")
	private String fileSize;

	@Autowired
	private TemplateEngine templateEngine;

	@Autowired
	private EmailService emailService;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private DeclarationTanFilesDAO declarationTanFilesDAO;

	@Value("${tds.declaration.endpoint}")
	private String mailUrl;

	@Autowired
	private RedisUtil<Map<String, Object>> redisUtilUserTenantInfo;

	@Autowired
	private KYCDeclarationEmailDetailsDAO kycDeclarationEmailDetailsDAO;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private KYCRedisDetailsDAO kycEmailDetailsDAO;

	@Autowired
	private KYCHistoryDAO kycHistoryDAO;

	@Async
	public void asyncProcessKycDetails(String deductorTan, Integer assesssmentYear, Integer assessmentMonth,
			String userName, String tenantId, String deductorPan, BatchUpload batchUpload, String type)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		processKycDetails(deductorTan, assesssmentYear, assessmentMonth, userName, tenantId, deductorPan, batchUpload,
				type);
	}

	private void processKycDetails(String deductorTan, Integer assesssmentYear, Integer assessmentMonth,
			String userName, String tenantId, String deductorPan, BatchUpload batchUpload, String type)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException {

		MultiTenantContext.setTenantId(tenantId);
		ArrayList<KYCDetailsErrorFilDTO> errorList = new ArrayList<>();
		File kycDetailsErrorFile = null;
		File file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());
		XSSFWorkbook workbook = new XSSFWorkbook(file.getAbsolutePath());
		try {
			long dataRowsCount = 0L;
			CustomerKYCDetailsExcel customerData = null;
			VendorKYCDetailsExcle vendorData = null;
			ShareholderKYCDetalsExcle shareholderData = null;
			if ("CUSTOMER".equalsIgnoreCase(type)) {
				customerData = new CustomerKYCDetailsExcel(workbook);
				dataRowsCount = customerData.getDataRowsCount();
			} else if ("VENDOR".equalsIgnoreCase(type)) {
				vendorData = new VendorKYCDetailsExcle(workbook);
				dataRowsCount = vendorData.getDataRowsCount();
			} else {
				shareholderData = new ShareholderKYCDetalsExcle(workbook);
				dataRowsCount = shareholderData.getDataRowsCount();
			}
			batchUpload.setMismatchCount(0L);
			batchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			Long duplicateCount = 0L;
			List<KYCDetails> kycDetailsList = new ArrayList<>();
			List<KYCDetails> kycUpdateList = new ArrayList<>();
			Set<String> duplicationRecords = new HashSet<>();
			List<KYCHistory> kycHistoryList = new ArrayList<>();
			// feign client to get all deductee status
			List<String> deducteeStatusList = mastersClient.getDeducteeStatus().getBody().getData();
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<KYCDetailsErrorFilDTO> errorDTO = Optional.empty();
				boolean isNotValid = false;
				try {
					if ("CUSTOMER".equalsIgnoreCase(type)) {
						errorDTO = customerData.validate(rowIndex);
					} else if ("VENDOR".equalsIgnoreCase(type)) {
						errorDTO = vendorData.validate(rowIndex);
					} else {
						errorDTO = shareholderData.validate(rowIndex);
					}

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
						KYCDetails kycDetails = null;
						KYCDetailsErrorFilDTO kycErrorDTO = null;
						if ("CUSTOMER".equalsIgnoreCase(type)) {
							kycDetails = customerData.get(rowIndex);
							kycErrorDTO = customerData.getErrorDTO(rowIndex);
						} else if ("VENDOR".equalsIgnoreCase(type)) {
							kycDetails = vendorData.get(rowIndex);
							kycErrorDTO = vendorData.getErrorDTO(rowIndex);
						} else {
							kycDetails = shareholderData.get(rowIndex);
							kycErrorDTO = shareholderData.getErrorDTO(rowIndex);
						}

						if (StringUtils.isBlank(kycErrorDTO.getReason())) {
							kycErrorDTO.setReason("");
						}
						// email validation logic.
						try {
							String[] emailList = kycDetails.getEmailId().split(",");
							for (String mail : emailList) {
								if (!String.valueOf(mail.trim()).matches("^(.+)@(.+)$")) {
									isNotValid = true;
								}
							}
							if (isNotValid) {
								kycErrorDTO.setReason(kycErrorDTO.getReason() + "Email id " + kycDetails.getEmailId()
										+ " is not valid." + "\n");
							} else {
								kycDetails.setEmailId(kycDetails.getEmailId().trim());
							}
						} catch (Exception e) {
							isNotValid = true;
							kycErrorDTO.setReason(kycErrorDTO.getReason() + "Email id " + kycDetails.getEmailId()
									+ " is not valid." + "\n");
							logger.error("error occured while reading a emails");
						}
						// pan validation.
						if (StringUtils.isNotBlank(kycDetails.getCustomerPan())) {
							if (!kycDetails.getCustomerPan().matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
								kycErrorDTO.setReason(kycErrorDTO.getReason() + "Pan " + kycDetails.getCustomerPan()
										+ " is not valid." + "\n");
								isNotValid = true;
							} else if (!deducteeStatusList
									.contains(String.valueOf(kycDetails.getCustomerPan().charAt(3)))) {
								kycErrorDTO.setReason(kycErrorDTO.getReason() + "Pan 4th character "
										+ kycDetails.getCustomerPan().charAt(3) + " is Invalid." + "\n");
								isNotValid = true;
							} else {
								kycDetails.setCustomerPan(kycDetails.getCustomerPan().trim());
							}
						}

						if (!isNotValid) {
							kycDetails.setActive(true);
							kycDetails.setCustomerName(kycDetails.getCustomerName().trim());
							kycDetails.setCustomerCode(kycDetails.getCustomerCode().trim());
							kycDetails.setPhoneNumber(kycDetails.getPhoneNumber());
							kycDetails.setCreatedDate(new Timestamp(new Date().getTime()));
							kycDetails.setCreatedBy(userName);
							kycDetails.setModifiedBy(userName);
							kycDetails.setModifiedDate(new Timestamp(new Date().getTime()));
							kycDetails.setIsKycSubmitted(false);
							kycDetails.setAcceptTermsAndConditions(false);
							kycDetails.setDeductorMasterTan(deductorTan);
							kycDetails.setDeductorPan(deductorPan);
							kycDetails.setYear(assesssmentYear);
							kycDetails.setIsFormSubmitted(false);
							kycDetails.setIsEmailTriggered(kycDetails.getIsEmailTriggered());
							kycDetails.setBatchUploadId(batchUpload.getBatchUploadID());
							kycDetails.setType(type.toUpperCase());
							String uniqueRecord = kycDetails.getCustomerName() + "_" + kycDetails.getCustomerCode()
									+ "_" + deductorPan + "_" + deductorTan + "_" + type.toUpperCase() + "_"
									+ kycDetails.getEmailId();
							if (duplicationRecords.contains(uniqueRecord)) {
								duplicateCount++;
							} else {
								duplicationRecords.add(uniqueRecord);
								List<KYCDetails> kycList = kycDetailsDAO.getKycListByAndCode(
										kycDetails.getCustomerCode(), deductorPan, deductorTan, type);
								if (kycList.isEmpty()) {
									// add kyc details list
									kycDetailsList.add(kycDetails);
								} else {
									// kyc history
									KYCHistory kycHistory = new KYCHistory();
									kycHistory.setActive(true);
									kycHistory.setDeductorMasterTan(deductorTan);
									kycHistory.setDeductorPan(deductorPan);
									kycHistory.setYear(assesssmentYear);
									kycHistory.setCreatedBy(userName);
									kycHistory.setCreatedDate(new Timestamp(new Date().getTime()));
									kycHistory.setModifiedBy(userName);
									kycHistory.setModifiedDate(new Timestamp(new Date().getTime()));
									kycHistory.setKycDetailsId(kycList.get(0).getId());
									kycHistory.setEmail(kycList.get(0).getEmailId());

									kycList.get(0).setEmailId(kycDetails.getEmailId());
									kycList.get(0).setIsEmailTriggered(kycDetails.getIsEmailTriggered());
									kycList.get(0).setBatchUploadId(batchUpload.getBatchUploadID());
									kycList.get(0).setType(type.toUpperCase());
									kycList.get(0).setPhoneNumber(kycDetails.getPhoneNumber());
									kycList.get(0).setCustomerName(kycDetails.getCustomerName());
									// add kyc details list
									kycUpdateList.add(kycList.get(0));
									// add kyc history list
									kycHistoryList.add(kycHistory);
								}
							}
						}
						if (isNotValid) {
							++errorCount;
							errorList.add(kycErrorDTO);
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						KYCDetailsErrorFilDTO problematicDataError = null;
						if ("CUSTOMER".equalsIgnoreCase(type)) {
							problematicDataError = customerData.getErrorDTO(rowIndex);
						} else if ("VENDOR".equalsIgnoreCase(type)) {
							problematicDataError = vendorData.getErrorDTO(rowIndex);
						} else {
							problematicDataError = shareholderData.getErrorDTO(rowIndex);
						}
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			} // for 1

			if (!kycDetailsList.isEmpty()) {
				kycDetailsDAO.batchSaveKycDetails(kycDetailsList, tenantId);
			}

			if (!kycUpdateList.isEmpty()) {
				// kyc batch update
				kycDetailsDAO.batchUpdateKycDetails(kycUpdateList);
			}

			if (!kycHistoryList.isEmpty()) {
				kycHistoryDAO.batchSaveKycHistory(kycHistoryList, tenantId);
			}

			batchUpload.setSuccessCount(dataRowsCount);
			batchUpload.setFailedCount(errorCount);
			batchUpload.setProcessedCount(kycDetailsList.size() + kycUpdateList.size());
			batchUpload.setDuplicateCount(duplicateCount);
			batchUpload.setStatus("Processed");
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);

			if (!errorList.isEmpty()) {
				if ("CUSTOMER".equalsIgnoreCase(type)) {
					kycDetailsErrorFile = prepareKycDetailsErrorFile(file.getName(), deductorTan, deductorPan,
							errorList, new ArrayList<>(customerData.getHeaders()), type);
				} else if ("VENDOR".equalsIgnoreCase(type)) {
					kycDetailsErrorFile = prepareKycDetailsErrorFile(file.getName(), deductorTan, deductorPan,
							errorList, new ArrayList<>(vendorData.getHeaders()), type);
				} else {
					kycDetailsErrorFile = prepareKycDetailsErrorFile(file.getName(), deductorTan, deductorPan,
							errorList, new ArrayList<>(shareholderData.getHeaders()), type);
				}
			}

			batchUpload.setStatus("Processed");
			KycDetailsBatchUpload(batchUpload, null, deductorTan, assesssmentYear, assessmentMonth, userName,
					kycDetailsErrorFile, tenantId, type);

		} catch (Exception e) {
			logger.error("Exception occurred :{}", e.getMessage());
		}

	}

	/**
	 *
	 * @param deductorTan
	 * @param tenantId
	 * @param deductorPan
	 * @param email
	 * @param ctx
	 * @param kycIdList
	 * @param kycDetails
	 * @param deductorEmail
	 * @param isEmailCc
	 * @param redisList
	 */
	private void mailSend(String deductorTan, String tenantId, String deductorPan, Email email, Context ctx,
			KYCDetails kycDetails, String deductorName, List<KYCDetails> kycDetailsSentList, String deductorEmail,
			Boolean isEmailCc) {
		// delete redies key
		List<KYCRedisDetails> kycEmailList = kycEmailDetailsDAO.getKycEmailRediesKeys(kycDetails.getId());
		if (!kycEmailList.isEmpty()) {
			for (KYCRedisDetails kycEmail : kycEmailList) {
				KycRedisDTO dto = new KycRedisDTO(kycEmail.getEmail(), kycDetails.getId().toString(), deductorPan,
						deductorTan, tenantId, kycDetails.getCustomerName(), deductorName, kycDetails.getType());
				ObjectMapper objectMapper = new ObjectMapper();
				String customerJson = StringUtils.EMPTY;
				try {
					customerJson = objectMapper.writeValueAsString(dto);
				} catch (Exception e) {
					logger.error("Exception occured while converting KycRedisDTO to string for key {} : ",
							e.getMessage());
				}
				redisUtilUserTenantInfo.deleteMap(RedisKeys.KYC.name(), kycEmail.getRedisKey(), customerJson);
			}
			kycEmailDetailsDAO.batchUpdate(kycEmailList);
		}
		logger.info("From email: {}", mailsentfrom);
		String[] emailList = kycDetails.getEmailId().split(",");
		for (String mail : emailList) {
			mail = mail.trim();
			KYCRedisDetails kycEmail = new KYCRedisDetails();
			email.setFrom("EY on behalf of " + deductorName + " <" + mailsentfrom + ">");
			email.setTo(mail);
			// email cc check.
			if (isEmailCc.equals(true) && StringUtils.isNotBlank(deductorEmail)) {
				email.setCc(deductorEmail);
			}
			if ("CUSTOMER".equalsIgnoreCase(kycDetails.getType())) {
				email.setSubject(
						"Kind Attention: Declaration required for complying with TDS/ TCS provisions of the Income Tax Act, 1961 w.e.f 1 July 2021");
			} else {
				email.setSubject(
						"Kind Attention: Declaration required for complying with TDS provisions of the Income Tax Act, 1961 w.e.f 1 July 2021");
			}
			ctx.setVariable("name", kycDetails.getCustomerName());
			ctx.setVariable("email", mail);
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
			Date date = DateUtils.addDays(new Date(), 7);
			String newDate = dateFormat.format(date);
			ctx.setVariable("date", newDate);
			String uniqueKey = kycDetails.getDeductorMasterTan() + kycDetails.getDeductorPan() + mail
					+ kycDetails.getCustomerCode() + kycDetails.getCustomerName() + new Date().getTime();
			String encryptedEmail = encrypt(uniqueKey);
			addToRedis(encryptedEmail, mail, kycDetails.getId(), deductorPan, deductorTan, tenantId,
					kycDetails.getCustomerName(), deductorName, kycDetails.getType());
			ctx.setVariable("path", mailUrl + "kyc/" + encryptedEmail);
			ctx.setVariable("clientName", deductorName);
			String body = StringUtils.EMPTY;
			if ("CUSTOMER".equalsIgnoreCase(kycDetails.getType())) {
				body = templateEngine.process("kycvcustomermailtemplate", ctx);
			} else {
				body = templateEngine.process("kycvendoremailtemplate", ctx);
			}
			try {
				logger.info("About to send an email from mail: {} and to email {}", mailsentfrom, mail);
				emailService.sendHtmlTemplateNotification(email, body);
				logger.info("email sent sucessfully from emial: {} and to email {}", mailsentfrom, mail);
				kycDetails.setBasisEmailSent(new Timestamp(new Date().getTime()));
				kycDetails.setId(kycDetails.getId());
				kycDetailsSentList.add(kycDetails);
			} catch (MessagingException e) {
				logger.error("Exception occured while sending email : {}", e.getMessage());
			}
			// kyc email save
			kycEmail.setActive(true);
			kycEmail.setCreatedBy(kycDetails.getCreatedBy());
			kycEmail.setModifiedBy(kycDetails.getModifiedBy());
			kycEmail.setCreatedDate(new Timestamp(new Date().getTime()));
			kycEmail.setModifiedDate(new Timestamp(new Date().getTime()));
			kycEmail.setYear(kycDetails.getYear());
			kycEmail.setDeductorMasterTan(deductorTan);
			kycEmail.setDeductorPan(deductorPan);
			kycEmail.setKycDetailsId(kycDetails.getId());
			kycEmail.setRedisKey(encryptedEmail);
			kycEmail.setEmail(mail);
			kycEmailDetailsDAO.save(kycEmail);
		}
	}

	/**
	 * 
	 * @param key
	 * @param emailID
	 * @param kycId
	 * @param deductorPan
	 * @param deductorTan
	 * @param tenantId
	 * @param customerName
	 * @param deductorName
	 * @param type
	 */
	private void addToRedis(String key, String emailID, int kycId, String deductorPan, String deductorTan,
			String tenantId, String customerName, String deductorName, String type) {
		KycRedisDTO dto = new KycRedisDTO(emailID, Integer.valueOf(kycId).toString(), deductorPan, deductorTan,
				tenantId, customerName, deductorName, type);
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String customerJson = objectMapper.writeValueAsString(dto);
			logger.info("About to add data to Redis for Customer/Vendor: {}", customerName);
			redisUtilUserTenantInfo.putMap(RedisKeys.KYC.name(), key, customerJson);
			logger.info("Added data to Redis for Customer/Vendor: {}", customerName);
		} catch (JsonProcessingException e) {
			logger.error("Exception occured while converting KycRedisDTO to string for key : {} : ", key,
					e.getMessage());
		}

	}

	/**
	 *
	 * @param originalFilename
	 * @param deductorTan
	 * @param deductorPan
	 * @param errorList
	 * @param arrayList
	 * @return
	 * @throws Exception
	 */
	public File prepareKycDetailsErrorFile(String originalFileName, String deductorTan, String deductorPan,
			ArrayList<KYCDetailsErrorFilDTO> errorList, ArrayList<String> headers, String type) throws Exception {
		try {
			headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = kycDetailsXlsxReport(errorList, deductorTan, deductorPan, headers, type);
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
	 * @param errorList
	 * @param deductorTan
	 * @param deductorPan
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	private Workbook kycDetailsXlsxReport(ArrayList<KYCDetailsErrorFilDTO> errorList, String deductorTan,
			String deductorPan, ArrayList<String> headers, String type) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headers, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForKycDetails(errorList, worksheet, deductorTan, headers, type);

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

		// Style for D6 to G6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:I6");
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
		String lastHeaderCellName = "I6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:I6");
		worksheet.autoFitColumn(1);
		worksheet.autoFitRows();
		return workbook;
	}

	/**
	 *
	 * @param errorList
	 * @param worksheet
	 * @param deductorTan
	 * @param headers
	 * @throws Exception
	 */
	private void setExtractDataForKycDetails(ArrayList<KYCDetailsErrorFilDTO> errorList, Worksheet worksheet,
			String deductorTan, ArrayList<String> headers, String type) throws Exception {

		if (!errorList.isEmpty()) {
			int dataRowsStartIndex = 6;
			int serialNumber = 0;
			for (int i = 0; i < errorList.size(); i++) {
				KYCDetailsErrorFilDTO errorDTO = errorList.get(i);
				ArrayList<String> rowData = new ArrayList<>();
				if ("CUSTOMER".equalsIgnoreCase(type)) {
					rowData = Excel.getValues(errorDTO, CustomerKYCDetailsExcel.fieldMappings, headers);
				} else if ("VENDOR".equalsIgnoreCase(type)) {
					rowData = Excel.getValues(errorDTO, VendorKYCDetailsExcle.fieldMappings, headers);
				} else {
					rowData = Excel.getValues(errorDTO, ShareholderKYCDetalsExcle.fieldMappings, headers);
				}
				rowData.set(0, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(2, String.valueOf(++serialNumber));
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headers, errorDTO.getReason());
			}
		}
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
	public BatchUpload KycDetailsBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant,
			String type) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
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
		if ("CUSTOMER".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.CUSTOMER_KYC_DETAILS_UPLOAD.name());
		} else if ("VENDOR".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.VENDOR_KYC_DETAILS_UPLOAD.name());
		} else if ("SHAREHOLDER".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.SHAREHOLDER_KYC_DETAILS_UPLOAD.name());
		} else if ("DEDUCTEE_THRESHOLD_UPDATE".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.DEDUCTEE_THRESHOLD_UPDATE.name());
		} else if ("DEDUCTEE_DECLARATION".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.DEDUCTEE_DECLARATION.name());
		}
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

	/**
	 *
	 * @param deductorPan
	 * @param type
	 * @return
	 */
	public List<KYCDetails> getKycDetailsList(String deductorPan, String tan, String type) {
		List<KYCDetails> kycList = kycDetailsDAO.getKycDetailsList(deductorPan, tan, type);
		List<KYCDetails> finalList = new ArrayList<>();
		if (!kycList.isEmpty()) {
			for (KYCDetails kycDetails : kycList) {
				if (!kycDetails.getIsFormSubmitted()) {
					if (!kycDetails.getIsEmailTriggered()) {
						kycDetails.setIsEmailTriggered(null);
					}
					kycDetails.setIsAuthorizedPerson(null);
					kycDetails.setIsPanExists(null);
					finalList.add(kycDetails);
				} else {
					return kycList;
				}
			}
			return finalList;
		} else {
			return kycList;
		}
	}

	/**
	 *
	 * @param deductorPan
	 * @param deductorTan
	 * @param kycDetails
	 * @param turnOverFile
	 * @param iTRFile2
	 * @param iTRFile1
	 * @param fsFile
	 * @param panFile
	 * @param tanFiles
	 * @param panFilePath
	 * @param fsFilePath
	 * @param panDocumentFile
	 * @param deductorTan
	 * @param deductorPan
	 * @param type
	 * @param uploadFile
	 * @param year2File
	 * @param year1File
	 * @param panFile
	 * @return
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws ParseException
	 */
	public KYCDetailsFormDTO updateKycDetails(KYCDetailsFormDTO kycDetails, MultipartFile iTRFile1,
			MultipartFile iTRFile2, MultipartFile iTRFile3, String key, List<MultipartFile> tanFiles,
			MultipartFile panFile, MultipartFile fsFile)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, ParseException {
		String customerJSONString = redisUtilUserTenantInfo.getMapAsAll(RedisKeys.KYC.name()).get(key);
		try {
			KycRedisDTO redisDTO = new ObjectMapper().readValue(customerJSONString, KycRedisDTO.class);
			MultiTenantContext.setTenantId(redisDTO.getTenantId());
			List<KYCDetails> kycList = kycDetailsDAO.getKycDetails(redisDTO.getDeductorPan(), redisDTO.getDeductorTan(),
					Integer.valueOf(redisDTO.getKycId()), redisDTO.getType());
			if (!kycList.isEmpty()) {
				KYCDetails kycDetailsObj = kycList.get(0);
				if (!kycDetailsObj.getIsFormSubmitted()) {
					kycDetailsObj.setIsFormSubmitted(true);
					kycDetailsObj.setModifiedDate(new Timestamp(new Date().getTime()));
					kycDetailsObj.setType(kycDetailsObj.getType().toUpperCase());
					kycDetailsObj.setId(kycDetailsObj.getId());
					kycDetailsObj.setIsAuthorizedPerson(kycDetails.getIsRightPerson());
					if (kycDetailsObj.getIsAuthorizedPerson().equals(false)) {
						kycDetailsObj.setSignedEmailId(kycDetails.getSignedEmailId());
						kycDetailsObj.setIsKycSubmitted(false);
						kycDetailsObj.setSignedMobileNumber(kycDetails.getSignedMobileNumber());
						kycDetailsObj.setSignedNameforNo(kycDetails.getSignedNameforNo());
					} else {
						kycDetailsObj.setIsTanFiles(false);
						if (iTRFile1 != null) {
							String year1FileUri = blob.uploadExcelToBlob(iTRFile1);
							kycDetailsObj.setItrAttachmentYear1(year1FileUri);
						}
						if (iTRFile2 != null) {
							String year2FileUri = blob.uploadExcelToBlob(iTRFile2);
							kycDetailsObj.setItrAttachmentYear2(year2FileUri);
						}
						if (iTRFile3 != null) {
							String year3FileUri = blob.uploadExcelToBlob(iTRFile3);
							kycDetailsObj.setItrAttachmentYear3(year3FileUri);
						}
						if (panFile != null) {
							String panFileUrl = blob.uploadExcelToBlob(panFile);
							kycDetailsObj.setPanFilePath(panFileUrl);
						}
						if (fsFile != null) {
							String fsFileUrl = blob.uploadExcelToBlob(fsFile);
							kycDetailsObj.setFsFilePath(fsFileUrl);
						}
						for (MultipartFile tanFile : tanFiles) {
							String fileURL = blob.uploadExcelToBlob(tanFile);
							DeclarationTanFiles declarationTanFiles = new DeclarationTanFiles();
							declarationTanFiles.setKycId(kycDetailsObj.getId());
							declarationTanFiles.setFilePath(fileURL);
							declarationTanFiles.setDeductorPan(kycDetailsObj.getDeductorPan());
							declarationTanFiles.setDeductorTan(kycDetailsObj.getDeductorMasterTan());
							declarationTanFiles.setActive(true);
							declarationTanFiles.setAssesmentYear(CommonUtil.getAssessmentYear(null));
							declarationTanFiles.setCreatedDate(new Date());
							declarationTanFiles.setModifiedDate(new Date());
							declarationTanFiles.setCreatedBy(kycDetailsObj.getCreatedBy());
							declarationTanFiles.setModifiedBy(kycDetailsObj.getCreatedBy());
							declarationTanFiles.setFileName(tanFile.getOriginalFilename());
							declarationTanFilesDAO.save(declarationTanFiles);
							kycDetailsObj.setIsTanFiles(true);
						}
						kycDetailsObj.setIsPanExists(kycDetails.getIsPanApplicable());
						if (!StringUtils.isBlank(kycDetails.getPanNo())) {
							kycDetailsObj.setKycPan(kycDetails.getPanNo());
						}
						kycDetailsObj.setTanApplicable(kycDetails.getIsTanApplicable());
						if (!kycDetails.getTanNo().isEmpty()) {
							kycDetailsObj.setTanNumber(String.join(",", kycDetails.getTanNo()));
						}
						kycDetailsObj.setAcceptTermsAndConditions(kycDetails.getAcceptTnC());
						kycDetailsObj.setIsKycSubmitted(true);
						kycDetailsObj.setTurnoverExceed10cr(kycDetails.getTurnOverAmt());
						if ("YES".equalsIgnoreCase(kycDetails.getTurnOverAmt())) {
							kycDetailsObj.setTdsTcsApplicabilityIndicator("TDS");
						} else if ("NO".equalsIgnoreCase(kycDetails.getTurnOverAmt())
								|| "NA".equalsIgnoreCase(kycDetails.getTurnOverAmt())) {
							kycDetailsObj.setTdsTcsApplicabilityIndicator("TCS");
						}
						kycDetailsObj.setItrFinancialYear1(kycDetails.getItr1());
						kycDetailsObj.setItrFinancialYear2(kycDetails.getItr2());
						kycDetailsObj.setItrFinancialYear3(kycDetails.getItr3());
						kycDetailsObj.setAcknowledgementItrYear1(kycDetails.getAckNoITR1());
						kycDetailsObj.setAcknowledgementItrYear2(kycDetails.getAckNoITR2());
						kycDetailsObj.setAcknowledgementItrYear3(kycDetails.getAckNoITR3());
						kycDetailsObj.setAggregateTcsOrTdsGreaterThan50kForYear1(kycDetails.getAggregatedAmount1());
						kycDetailsObj.setAggregateTcsOrTdsGreaterThan50kForYear2(kycDetails.getAggregatedAmount2());
						kycDetailsObj.setAggregateTcsOrTdsGreaterThan50kForYear3(kycDetails.getAggregatedAmount3());
						kycDetailsObj.setSignedName(kycDetails.getSignedName());
						kycDetailsObj.setSignedDesignation(kycDetails.getSignedDesignation());
						kycDetailsObj.setKycRemarks(kycDetails.getRemarks());
						kycDetailsObj.setIndemnifyDeclare(kycDetails.getIndemnifyDeclare());
						kycDetailsObj.setYesSignedEmailId(kycDetails.getYesSignedEmailId());
					}
					kycDetailsDAO.update(kycDetailsObj);
					// delete redies key
					List<KYCRedisDetails> kycEmailList = kycEmailDetailsDAO
							.getKycEmailRediesKeys(kycDetailsObj.getId());
					for (KYCRedisDetails kycEmail : kycEmailList) {
						KycRedisDTO dto = new KycRedisDTO(kycEmail.getEmail(), kycDetailsObj.getId().toString(),
								kycDetailsObj.getDeductorPan(), kycDetailsObj.getDeductorMasterTan(),
								redisDTO.getTenantId(), kycDetailsObj.getCustomerName(), redisDTO.getDeductorName(),
								kycDetailsObj.getType());
						ObjectMapper objectMapper = new ObjectMapper();
						String customerJson = StringUtils.EMPTY;
						try {
							customerJson = objectMapper.writeValueAsString(dto);
						} catch (Exception e) {
							logger.error("Exception occured while converting KycRedisDTO to string for key {} : ",
									e.getMessage());
						}
						redisUtilUserTenantInfo.deleteMap(RedisKeys.KYC.name(), kycEmail.getRedisKey(), customerJson);
					}
					kycEmailDetailsDAO.batchUpdate(kycEmailList);
				} else {
					// send message that already submitted
					throw new CustomException("KYC Details already submitted");
				}
			}
		} catch (Exception e) {
			logger.error("Unable to convert JSON String to KycRedisDTO : {}", e.getMessage());
			throw new CustomException("KYC Details already submitted");
		}

		return kycDetails;
	}

	/**
	 *
	 * @param errorList
	 * @param tan
	 * @param tenantId
	 * @param deductorPan
	 * @param error
	 * @return
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws ParseException
	 */
	@Async
	public void kycTdsVsTcsApplicabilityExcel(String deductorPan, String tenantId, String tan, String userName,
			int year, String type)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String fileName = "CUSTOEMR_TDS_vs_TCS_Applicability_Report"
				+ CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		BatchUpload batchUpload = saveBatchUploadReport(tan, tenantId, year, null, 0L,
				UploadTypes.CUSTOMER_KYC_TDS_VS_TCS_APPLICABILITY.name(), "Processing", month, userName, null,
				fileName);
		List<KYCDetails> kycList = kycDetailsDAO.getKycActionList(deductorPan, tan, year, type);
		int count = kycList.size();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = resourceLoader.getResource("classpath:templates/" + "kyc_tds_vs_tcs_report.xlsx");
		InputStream input = resource.getInputStream();
		try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
			XSSFSheet sheet = wb.getSheetAt(0);
			sheet.protectSheet("password");
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);
			String msg = getErrorReportMsg(tenantId, deductorPan, "Customer KYC TDS vs TCS Applicability");
			DataValidation dataValidation = null;
			DataValidationConstraint constraint = null;
			DataValidationHelper validationHelper = null;

			Font fonts = wb.createFont();
			fonts.setBold(true);

			XSSFCellStyle style = wb.createCellStyle();
			style.setFont(fonts);
			style.setWrapText(true);
			style.setBorderLeft(BorderStyle.MEDIUM);
			style.setBorderTop(BorderStyle.MEDIUM);
			style.setBorderBottom(BorderStyle.MEDIUM);
			style.setBorderRight(BorderStyle.MEDIUM);
			style.setAlignment(HorizontalAlignment.LEFT);
			style.setVerticalAlignment(VerticalAlignment.CENTER);

			Font fonts2 = wb.createFont();
			fonts2.setBold(false);

			XSSFCellStyle style1 = wb.createCellStyle();
			style1.setFont(fonts);
			style1.setWrapText(true);
			style1.setFont(fonts2);
			style1.setLocked(true);

			XSSFCellStyle style2 = (XSSFCellStyle) wb.createCellStyle();
			style2.setFont(fonts);
			style2.setWrapText(true);
			style2.setFont(fonts2);
			style2.setLocked(false);

			XSSFRow row0 = sheet.createRow(0);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);

			int size = kycList.size();
			validationHelper = sheet.getDataValidationHelper();
			CellRangeAddressList addressList = new CellRangeAddressList(5, size + 5, 5, 5);
			constraint = validationHelper.createExplicitListConstraint(new String[] { "Accept", "Reject", "Hold" });
			dataValidation = validationHelper.createValidation(constraint, addressList);
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
			dataValidation.setShowPromptBox(true);
			sheet.addValidationData(dataValidation);

			int rowindex = 5;
			for (KYCDetails kyc : kycList) {
				XSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
				createSXSSFCell(style1, row1, 0, kyc.getCustomerName());
				createSXSSFCell(style1, row1, 1, kyc.getCustomerPan());
				createSXSSFCell(style1, row1, 2, kyc.getCustomerCode());
				createSXSSFCell(style1, row1, 3, kyc.getTurnoverExceed10cr());
				createSXSSFCell(style1, row1, 4, kyc.getTdsTcsApplicabilityIndicator());
				createSXSSFCell(style2, row1, 5, "");
				createSXSSFCell(style2, row1, 6, kyc.getTdsTcsClientFinalResponse());
				createSXSSFCell(style1, row1, 7, kyc.getId().toString());
			}
			sheet.setColumnHidden(7, true);
			wb.write(out);
			saveBatchUploadReport(tan, tenantId, year, out, Long.valueOf(count),
					UploadTypes.CUSTOMER_KYC_TDS_VS_TCS_APPLICABILITY.name(), "Processed", month, userName,
					batchUpload.getBatchUploadID(), null);
		} catch (Exception e) {
			logger.info("Exception occured while preparing error file {} ", e.getMessage());
		}
	}

	/**
	 *
	 * @param style
	 * @param row
	 * @param cellNumber
	 * @param value
	 */
	private void createSXSSFCell(XSSFCellStyle style, XSSFRow row, int cellNumber, String value) {
		row.createCell(cellNumber).setCellValue(StringUtils.isBlank(value) == true ? StringUtils.EMPTY : value);
		row.getCell(cellNumber).setCellStyle(style); // K
	}

	/**
	 *
	 * @param deductorMasterTan
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 */
	public String getErrorReportMsg(String tenantId, String deductorPan, String fileType) {
		MultiTenantContext.setTenantId(tenantId);
		List<DeductorMaster> getDeductor = deductorMasterDAO.findBasedOnDeductorPan(deductorPan);
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return fileType + " (Dated: " + date + ")\n Client Name: " + getDeductor.get(0).getName() + "\n";
	}

	/**
	 *
	 * @param deductorTan
	 * @param tenantId
	 * @param assessmentYear
	 * @param out
	 * @param noOfRows
	 * @param uploadType
	 * @param status
	 * @param month
	 * @param userName
	 * @param batchId
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws StorageException
	 * @throws ParseException
	 */
	public BatchUpload saveBatchUploadReport(String deductorTan, String tenantId, int assessmentYear,
			ByteArrayOutputStream out, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId, String fileName) throws FileNotFoundException, IOException, URISyntaxException,
			InvalidKeyException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String path = null;
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setSuccessFileUrl(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		batchUpload.setActive(true);
		if (StringUtils.isNotBlank(fileName)) {
			fileName = fileName + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
			batchUpload.setFileName(fileName);
		}
		List<BatchUpload> response = new ArrayList<>();
		if (batchId != null) {
			response = batchUploadDAO.findById(assessmentYear, deductorTan, uploadType, batchId);
		}
		if (!response.isEmpty()) {
			batchUpload = response.get(0);
			if (out != null) {
				File file = getConvertedExcelFile(out.toByteArray(), batchUpload.getFileName());
				path = blob.uploadExcelToBlobWithFile(file, tenantId);
				batchUpload.setStatus(status);
				batchUpload.setFilePath(path);
				batchUpload.setSuccessFileUrl(path);
				batchUpload.setRowsCount(noOfRows);
				batchUpload.setSuccessCount(noOfRows);
				batchUpload.setModifiedBy(userName);
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			} else {
				batchUpload.setModifiedBy(userName);
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchUpload.setRowsCount(0l);
			}
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	/**
	 *
	 * @param byteArray
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	protected File getConvertedExcelFile(byte[] byteArray, String fileName) throws IOException {
		File someFile = new File(fileName);
		try (FileOutputStream fos = new FileOutputStream(someFile)) {
			fos.write(byteArray);
			fos.flush();
			fos.close();
			return someFile;
		}
	}

	/**
	 *
	 * @param deductorPan
	 * @param tenantId
	 * @param deductorTan
	 * @param userName
	 * @throws Exception
	 */
	@Async
	public void kycPANValidationReport(String deductorPan, String tenantId, String deductorTan, String userName,
			int year, String type) throws Exception {
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		String uploadType = StringUtils.EMPTY;
		String kycType = StringUtils.EMPTY;
		if ("CUSTOMER_KYC_PAN_VALIDATION".equalsIgnoreCase(type)) {
			uploadType = UploadTypes.CUSTOMER_KYC_PAN_VALIDATION.name();
			kycType = "CUSTOMER";
		} else if ("VENDOR_KYC_PAN_VALIDATION".equalsIgnoreCase(type)) {
			uploadType = UploadTypes.VENDOR_KYC_PAN_VALIDATION.name();
			kycType = "VENDOR";
		} else {
			uploadType = UploadTypes.SHAREHOLDER_KYC_PAN_VALIDATION.name();
			kycType = "SHAREHOLDER";
		}

		String fileName = uploadType + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		fileName = fileName.replace("KYC_", "");
		BatchUpload batchUpload = saveBatchUploadReport(deductorTan, tenantId, year, null, 0L, uploadType, "Processing",
				month, userName, null, fileName);

		List<KYCDetails> kycList = kycDetailsDAO.getKycDetailsList(deductorPan, deductorTan, kycType);
		int count = kycList.size();
		logger.info("kyc pan count :{}", kycList.size());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Kyc Pan Validation");

		String[] headerNames1 = new String[] { "Customer code", "Customer Name", "PAN - KYC", "PAN - Input Template",
				"PAN - KYC Survey", "PAN for TRACES Validation", "TRACES Validation", "Name as per the Traces",
				"Match Score" };

		String[] headerNames2 = new String[] { "Vendor code", "Vendor Name", "PAN - KYC", "PAN - Input Template",
				"PAN - KYC Survey", "PAN for TRACES Validation", "TRACES Validation", "Name as per the Traces",
				"Match Score" };

		String[] headerNames3 = new String[] { "Sharehoder code", "Sharehoder Name", "PAN - KYC",
				"PAN - Input Template", "PAN - KYC Survey", "PAN for TRACES Validation", "TRACES Validation",
				"Name as per the Traces", "Match Score" };

		if ("CUSTOMER".equalsIgnoreCase(kycType)) {
			worksheet.getCells().importArray(headerNames1, 0, 0, false);
		} else if ("VENDOR".equalsIgnoreCase(kycType)) {
			worksheet.getCells().importArray(headerNames2, 0, 0, false);
		} else {
			worksheet.getCells().importArray(headerNames3, 0, 0, false);
		}

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (!kycList.isEmpty()) {
			int rowIndex = 1;
			for (KYCDetails kyc : kycList) {
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add(StringUtils.isBlank(kyc.getCustomerCode()) ? StringUtils.EMPTY : kyc.getCustomerCode());
				rowData.add(StringUtils.isBlank(kyc.getCustomerName()) ? StringUtils.EMPTY : kyc.getCustomerName());
				rowData.add(StringUtils.isEmpty(kyc.getKycPan()) ? "NO" : "YES");
				rowData.add(kyc.getCustomerPan());
				rowData.add(StringUtils.isBlank(kyc.getKycPan()) ? StringUtils.EMPTY : kyc.getKycPan());
				rowData.add(StringUtils.isEmpty(kyc.getKycPan()) ? kyc.getCustomerPan() : kyc.getKycPan());
				if (kyc.getIsPanVerifyStatus() == null) {
					rowData.add(StringUtils.EMPTY);
				} else {
					rowData.add(kyc.getIsPanVerifyStatus().equals(true) ? "Valid" : "Invalid");
				}
				rowData.add(
						StringUtils.isBlank(kyc.getNameAsPerTraces()) ? StringUtils.EMPTY : kyc.getNameAsPerTraces());
				rowData.add(StringUtils.isBlank(kyc.getMatchScore()) ? StringUtils.EMPTY : kyc.getMatchScore());
				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}

		// Style for A1 to G1 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(114, 159, 207));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		style1.setLocked(true);
		Range headerColorRange1 = worksheet.getCells().createRange("A1:I1");
		headerColorRange1.setStyle(style1);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:I1");
		workbook.save(out, SaveFormat.XLSX);

		saveBatchUploadReport(deductorTan, tenantId, year, out, Long.valueOf(count), uploadType, "Processed", month,
				userName, batchUpload.getBatchUploadID(), null);
	}

	/**
	 *
	 * @param deductorTan
	 * @param tenantId
	 * @param deductorPan
	 * @param batchId
	 * @param userName
	 * @param type
	 * @param path
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	@Async
	public BatchUpload asyncUpdateKycRemediationReport(String deductorTan, String tenantId, String deductorPan,
			Integer batchId, String userName, String type)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		BatchUpload batchUpload = new BatchUpload();
		List<BatchUpload> batchList = batchUploadDAO.getBatchListBasedOnTanAndGroupId(deductorTan, batchId);
		if (!batchList.isEmpty()) {
			batchUpload = batchList.get(0);
			batchUpload.setFailedCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setRowsCount(0L);
			batchUpload.setUploadType(type);
			batchUpload.setStatus("Processing");
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUploadDAO.update(batchUpload);
			batchUpload = updateKycRemediationReport(batchUpload, tenantId, batchList.get(0).getFilePath(), type);
		}
		return batchUpload;
	}

	/**
	 *
	 * @param batchUpload
	 * @param tenantId
	 * @param path
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	public BatchUpload updateKycRemediationReport(BatchUpload batchUpload, String tenantId, String path, String type)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		String otherFileurl = null;
		Biff8EncryptionKey.setCurrentUserPassword("password");
		Workbook workbook;
		try {
			logger.info("kyc file path : {}", path);
			File file = blobStorageService.getFileFromBlobUrl(tenantId, path);
			workbook = new Workbook(file.getAbsolutePath());
			Worksheet worksheet = workbook.getWorksheets().get(0);
			worksheet.getCells().deleteRows(0, 4);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.CSV);
			File xlsxInvoiceFile = new File("TestCsvFile");
			FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
			CsvReader csvReader = new CsvReader();
			csvReader.setContainsHeader(true);
			CsvContainer csv = csvReader.read(xlsxInvoiceFile, StandardCharsets.UTF_8);

			List<KYCDetails> kycDetailsList = new ArrayList<>();
			String errorFilepath = null;
			int processedCount = 0;
			int errorCount = 0;
			for (CsvRow row : csv.getRows()) {
				String clientFinalResponse = row.getField("Client Final Response");

				String outPutResponse = row.getField("Output-TDS/TCS Applicability-Indicative");
				// action token
				String userAction = row.getField("Action Taken");
				// id
				Integer kycId = StringUtils.isNotBlank(row.getField("Kyc Id"))
						? Integer.parseInt(row.getField("Kyc Id"))
						: null;
				if (StringUtils.isNotBlank(userAction)) {
					KYCDetails kyc = new KYCDetails();
					if (kycId != null) {
						kyc.setId(kycId);
						kyc.setTcsTdsApplicabilityUserAction(userAction);
						if ("Accept".equalsIgnoreCase(userAction)) {
							kyc.setTdsTcsClientFinalResponse(outPutResponse);
						} else if ("Reject".equalsIgnoreCase(userAction)) {
							if (StringUtils.isNotBlank(clientFinalResponse)) {
								kyc.setTdsTcsClientFinalResponse(clientFinalResponse);
							} else {
								kyc.setTdsTcsClientFinalResponse(
										"TDS".equalsIgnoreCase(outPutResponse) ? "TCS" : "TDS");
							}
						} else if ("Hold".equalsIgnoreCase(userAction)) {
							kyc.setTdsTcsClientFinalResponse("Hold");
						}
					}
					kycDetailsList.add(kyc);
					processedCount++;
				}
			}
			// batch update kyc details
			if (!kycDetailsList.isEmpty()) {
				kycDetailsDAO.batchUpdate(kycDetailsList);
			}
			if (batchUpload.getBatchUploadID() != null) {
				batchUpload.setFilePath(batchUpload.getFilePath());
				batchUpload.setProcessedCount(processedCount);
				batchUpload.setFailedCount(Long.valueOf(errorCount));
				batchUpload.setErrorFilePath(errorFilepath);
				batchUpload.setStatus("Processed");
				batchUpload.setOtherFileUrl(otherFileurl);
				batchUpload.setRowsCount((long) processedCount + errorCount);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchUploadDAO.update(batchUpload);
			}
		} catch (Exception e1) {
			logger.error("Exception occurred while updating kyc remediation report {}", e1.getMessage());
		}
		return batchUpload;
	}

	/**
	 *
	 * @param deductorPan
	 * @param deductorTan
	 * @param id
	 * @param type
	 * @return
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public KYCDetailsDTO getKycSubmited(String key) throws JsonMappingException, JsonProcessingException {
		KYCDetailsDTO kycDto = new KYCDetailsDTO();
		String customerJSONString = redisUtilUserTenantInfo.getMapAsAll(RedisKeys.KYC.name()).get(key);
		if (StringUtils.isNotBlank(customerJSONString)) {
			KycRedisDTO redisDTO = new ObjectMapper().readValue(customerJSONString, KycRedisDTO.class);
			MultiTenantContext.setTenantId(redisDTO.getTenantId());
			List<KYCDetails> kycList = kycDetailsDAO.getKycDetails(redisDTO.getDeductorPan(), redisDTO.getDeductorTan(),
					Integer.valueOf(redisDTO.getKycId()), redisDTO.getType());
			List<DeductorMaster> response = deductorMasterDAO.findByDeductorPan(redisDTO.getDeductorPan());
			List<KYCDeclarationEmailDetails> perferencsList = kycDeclarationEmailDetailsDAO
					.getAllPerferences(redisDTO.getDeductorTan());
			if (!kycList.isEmpty()) {
				kycDto.setName(kycList.get(0).getCustomerName());
				kycDto.setClientName(response.get(0).getName());
				kycDto.setType(redisDTO.getType());
				kycDto.setCustomerPan(kycList.get(0).getCustomerPan());
				kycDto.setCustomerEmail(redisDTO.getEmailId());
				if (!perferencsList.isEmpty()) {
					kycDto.setIndemnifyDeclare(perferencsList.get(0).getIndemnifyDeclare());
					kycDto.setIsEmailCc(perferencsList.get(0).getIsEmailCc());
				}
				if (kycList.get(0).getIsFormSubmitted().equals(true)) {
					kycDto.setIsKycSubmited(true);
				} else {
					kycDto.setIsKycSubmited(false);
				}
			}
			kycDto.setFileSize(fileSize);
		} else {
			throw new CustomException("Invalid key", HttpStatus.BAD_REQUEST);
		}
		return kycDto;
	}

	/**
	 *
	 * @param pan
	 * @param tenantId
	 * @param tan
	 * @param userName
	 * @param moduleType
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvalidKeyException
	 * @throws ParseException
	 */
	@Async
	public void surveyResponseReportExcel(String deductorPan, String tenantId, String tan, String userName, int year,
			String type) throws InvalidKeyException, FileNotFoundException, IOException, URISyntaxException,
			StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		String uploadType = StringUtils.EMPTY;
		String kycType = StringUtils.EMPTY;
		if ("CUSTOMER_SURVEY_RESPONSE_REPORT".equalsIgnoreCase(type)) {
			uploadType = UploadTypes.CUSTOMER_SURVEY_RESPONSE_REPORT.name();
			kycType = "CUSTOMER";
		} else if ("VENDOR_SURVEY_RESPONSE_REPORT".equalsIgnoreCase(type)) {
			uploadType = UploadTypes.VENDOR_SURVEY_RESPONSE_REPORT.name();
			kycType = "VENDOR";
		} else {
			uploadType = UploadTypes.SHAREHOLDER_SURVEY_RESPONSE_REPORT.name();
			kycType = "SHAREHOLDER";
		}
		String fileName = uploadType + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		BatchUpload batchUpload = saveBatchUploadReport(tan, tenantId, year, null, 0L, uploadType, "Processing", month,
				userName, null, fileName);
		// get kyc list
		List<KYCDetails> kycList = kycDetailsDAO.getKycSurvyReportList(deductorPan, tan, year, kycType);
		int count = kycList.size();
		Resource resource = null;
		String msg = StringUtils.EMPTY;
		if ("CUSTOMER".equalsIgnoreCase(kycType)) {
			resource = resourceLoader.getResource("classpath:templates/" + "customer_survey_response_report.xlsx");
			msg = getErrorReportMsg(tenantId, deductorPan, "Customer Survey Response Report");
		} else if ("VENDOR".equalsIgnoreCase(kycType)) {
			resource = resourceLoader.getResource("classpath:templates/" + "vendor_survey_response_report.xlsx");
			msg = getErrorReportMsg(tenantId, deductorPan, "Vendor Survey Response Report");
		} else {
			resource = resourceLoader.getResource("classpath:templates/" + "shareholder_survey_response_report.xlsx");
			msg = getErrorReportMsg(tenantId, deductorPan, "Shareholder Survey Response Report");
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		InputStream input = resource.getInputStream();
		try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
			XSSFSheet sheet = wb.getSheetAt(0);

			Font fonts = wb.createFont();
			fonts.setBold(true);

			XSSFCellStyle style = wb.createCellStyle();
			style.setFont(fonts);
			style.setWrapText(true);
			style.setBorderLeft(BorderStyle.MEDIUM);
			style.setBorderTop(BorderStyle.MEDIUM);
			style.setBorderBottom(BorderStyle.MEDIUM);
			style.setBorderRight(BorderStyle.MEDIUM);
			style.setAlignment(HorizontalAlignment.LEFT);
			style.setVerticalAlignment(VerticalAlignment.CENTER);

			Font fonts2 = wb.createFont();
			fonts2.setBold(false);

			XSSFCellStyle style1 = wb.createCellStyle();
			style1.setFont(fonts);
			style1.setWrapText(true);
			style1.setFont(fonts2);
			style1.setLocked(true);

			XSSFRow row0 = sheet.createRow(0);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);
			int serialNo = 0;
			int rowindex = 5;
			String patternTime = "hh:mm:ss";
			String patternDate = "yyyy-MM-dd";
			SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(patternTime);
			SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(patternDate);
			String basicEmailTime = StringUtils.EMPTY;
			String basicEmailDate = StringUtils.EMPTY;
			for (KYCDetails kyc : kycList) {
				if (kyc.getBasisEmailSent() != null) {
					basicEmailTime = simpleDateFormat1.format(kyc.getBasisEmailSent().getTime());
					basicEmailDate = simpleDateFormat2.format(kyc.getBasisEmailSent());
				}
				String modifiedTime = simpleDateFormat1.format(kyc.getModifiedDate().getTime());
				XSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
				createSXSSFCell(style1, row1, 0, String.valueOf(++serialNo));
				if (kyc.getBasisEmailSent() != null) {
					createSXSSFCell(style1, row1, 1, basicEmailDate);
					createSXSSFCell(style1, row1, 2, basicEmailTime);
				} else {
					createSXSSFCell(style1, row1, 1, StringUtils.EMPTY);
					createSXSSFCell(style1, row1, 2, StringUtils.EMPTY);
				}
				createSXSSFCell(style1, row1, 3, simpleDateFormat2.format(kyc.getModifiedDate()));
				createSXSSFCell(style1, row1, 4, modifiedTime);
				createSXSSFCell(style1, row1, 5, kyc.getCustomerName());
				createSXSSFCell(style1, row1, 6, kyc.getCustomerPan());
				createSXSSFCell(style1, row1, 7, kyc.getKycPan());
				createSXSSFCell(style1, row1, 8, kyc.getCustomerCode());
				createSXSSFCell(style1, row1, 9, kyc.getEmailId());
				if (kyc.getIsAuthorizedPerson().equals(true)) {
					createSXSSFCell(style1, row1, 10, kyc.getYesSignedEmailId());
				} else {
					createSXSSFCell(style1, row1, 10, kyc.getSignedEmailId());
				}
				createSXSSFCell(style1, row1, 11, kyc.getPhoneNumber());
				// Email response Status
				if (kyc.getIsFormSubmitted() != null) {
					createSXSSFCell(style1, row1, 12, kyc.getIsFormSubmitted().equals(true) ? "YES" : "NO");
				} else {
					createSXSSFCell(style1, row1, 12, StringUtils.EMPTY);
				}
				// Are you the right person
				if (kyc.getIsAuthorizedPerson() != null) {
					createSXSSFCell(style1, row1, 13, kyc.getIsAuthorizedPerson().equals(true) ? "YES" : "NO");
				} else {
					createSXSSFCell(style1, row1, 13, StringUtils.EMPTY);
				}
				// Do you have PAN
				if (kyc.getIsPanExists() != null) {
					createSXSSFCell(style1, row1, 14, kyc.getIsPanExists().equals(true) ? "YES" : "NO");
				} else {
					createSXSSFCell(style1, row1, 14, StringUtils.EMPTY);
				}
				if ("CUSTOMER".equalsIgnoreCase(kycType) || "SHAREHOLDER".equalsIgnoreCase(kycType)) {
					// Do you Hold TAN
					createSXSSFCell(style1, row1, 15, kyc.getTanApplicable());
					// Whether Turnover in previous financial year > 10 Crores
					createSXSSFCell(style1, row1, 16, kyc.getTurnoverExceed10cr());
					// Have you filed ITR for following financial years
					createSXSSFCell(style1, row1, 17, kyc.getItrFinancialYear1());
					createSXSSFCell(style1, row1, 18, kyc.getItrFinancialYear2());
					createSXSSFCell(style1, row1, 19, kyc.getItrFinancialYear3());
					// Does aggregate amount of TDS+TCS>50000 cumulatively in each of two previous
					// financial years
					createSXSSFCell(style1, row1, 20, kyc.getAggregateTcsOrTdsGreaterThan50kForYear1());
					createSXSSFCell(style1, row1, 21, kyc.getAggregateTcsOrTdsGreaterThan50kForYear2());
					createSXSSFCell(style1, row1, 22, kyc.getAggregateTcsOrTdsGreaterThan50kForYear3());
					createSXSSFCell(style1, row1, 23, kyc.getKycRemarks());
				} else {
					// Have you filed ITR for following financial years
					createSXSSFCell(style1, row1, 15, kyc.getItrFinancialYear1());
					createSXSSFCell(style1, row1, 16, kyc.getItrFinancialYear2());
					createSXSSFCell(style1, row1, 17, kyc.getItrFinancialYear3());
					// Does aggregate amount of TDS+TCS>50000 cumulatively in each of two previous
					// financial years
					createSXSSFCell(style1, row1, 18, kyc.getAggregateTcsOrTdsGreaterThan50kForYear1());
					createSXSSFCell(style1, row1, 19, kyc.getAggregateTcsOrTdsGreaterThan50kForYear2());
					createSXSSFCell(style1, row1, 20, kyc.getAggregateTcsOrTdsGreaterThan50kForYear3());
					createSXSSFCell(style1, row1, 21, kyc.getKycRemarks());
				}

			}
			wb.write(out);
			saveBatchUploadReport(tan, tenantId, year, out, Long.valueOf(count), uploadType, "Processed", month,
					userName, batchUpload.getBatchUploadID(), null);
		} catch (Exception e) {
			logger.info("Exception occured while preparing error file {} ", e.getMessage());
		}
	}

	/**
	 *
	 * @param pan
	 * @param tenantId
	 * @param tan
	 * @param userName
	 * @param moduleType
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvalidKeyException
	 * @throws ParseException
	 */
	@Async
	public void failedResponseReportExcel(String deductorPan, String tenantId, String tan, String userName, int year,
			String type) throws InvalidKeyException, FileNotFoundException, IOException, URISyntaxException,
			StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		String uploadType = StringUtils.EMPTY;
		String kycType = StringUtils.EMPTY;
		if ("CUSTOMER_FAILED_RESPONSE_REPORT".equalsIgnoreCase(type)) {
			uploadType = UploadTypes.CUSTOMER_FAILED_RESPONSE_REPORT.name();
			kycType = "CUSTOMER";
		} else if ("VENDOR_FAILED_RESPONSE_REPORT".equalsIgnoreCase(type)) {
			uploadType = UploadTypes.VENDOR_FAILED_RESPONSE_REPORT.name();
			kycType = "VENDOR";
		} else {
			uploadType = UploadTypes.SHAREHOLDER_FAILED_RESPONSE_REPORT.name();
			kycType = "SHAREHOLDER";
		}
		String fileName = uploadType + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		BatchUpload batchUpload = saveBatchUploadReport(tan, tenantId, year, null, 0L, uploadType, "Processing", month,
				userName, null, fileName);
		// get kyc list
		List<KYCDetails> kycList = kycDetailsDAO.getFailedResponseReport(deductorPan, tan, year, kycType);
		int count = kycList.size();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = null;
		String msg = StringUtils.EMPTY;
		if ("CUSTOMER".equalsIgnoreCase(kycType)) {
			resource = resourceLoader.getResource("classpath:templates/" + "customer_failed_response_report.xlsx");
			msg = getErrorReportMsg(tenantId, deductorPan, "Customer failed Response Report");
		} else if ("VENDOR".equalsIgnoreCase(kycType)) {
			resource = resourceLoader.getResource("classpath:templates/" + "vendor_failed_response_report.xlsx");
			msg = getErrorReportMsg(tenantId, deductorPan, "Vendor failed Response Report");
		} else {
			resource = resourceLoader.getResource("classpath:templates/" + "shareholder_failed_response_report.xlsx");
			msg = getErrorReportMsg(tenantId, deductorPan, "Shareholder failed Response Report");
		}

		InputStream input = resource.getInputStream();
		try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
			XSSFSheet sheet = wb.getSheetAt(0);

			Font fonts = wb.createFont();
			fonts.setBold(true);

			XSSFCellStyle style = wb.createCellStyle();
			style.setFont(fonts);
			style.setWrapText(true);
			style.setBorderLeft(BorderStyle.MEDIUM);
			style.setBorderTop(BorderStyle.MEDIUM);
			style.setBorderBottom(BorderStyle.MEDIUM);
			style.setBorderRight(BorderStyle.MEDIUM);
			style.setAlignment(HorizontalAlignment.LEFT);
			style.setVerticalAlignment(VerticalAlignment.CENTER);

			Font fonts2 = wb.createFont();
			fonts2.setBold(false);

			XSSFCellStyle style1 = wb.createCellStyle();
			style1.setFont(fonts);
			style1.setWrapText(true);
			style1.setFont(fonts2);
			style1.setLocked(true);

			XSSFRow row0 = sheet.createRow(0);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);
			int serialNo = 0;
			int rowindex = 5;
			String patternTime = "hh:mm:ss";
			String patternDate = "yyyy-MM-dd";
			SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(patternTime);
			SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(patternDate);
			String basicEmailTime = StringUtils.EMPTY;
			String basicEmailDate = StringUtils.EMPTY;
			for (KYCDetails kyc : kycList) {
				if (kyc.getBasisEmailSent() != null) {
					basicEmailTime = simpleDateFormat1.format(kyc.getBasisEmailSent());
					basicEmailDate = simpleDateFormat2.format(kyc.getBasisEmailSent());
				}
				String modifiedTime = simpleDateFormat1.format(kyc.getModifiedDate());
				XSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
				createSXSSFCell(style1, row1, 0, String.valueOf(++serialNo));
				if (kyc.getBasisEmailSent() != null) {
					createSXSSFCell(style1, row1, 1, basicEmailDate);
					createSXSSFCell(style1, row1, 2, basicEmailTime);
				} else {
					createSXSSFCell(style1, row1, 1, StringUtils.EMPTY);
					createSXSSFCell(style1, row1, 2, StringUtils.EMPTY);
				}
				createSXSSFCell(style1, row1, 3, kyc.getCustomerName());
				createSXSSFCell(style1, row1, 4, kyc.getCustomerPan());
				createSXSSFCell(style1, row1, 5, kyc.getCustomerCode());
				createSXSSFCell(style1, row1, 6, kyc.getEmailId());
				createSXSSFCell(style1, row1, 7, kyc.getPhoneNumber());
				// Email response Status
				createSXSSFCell(style1, row1, 8,
						kyc.getIsFormSubmitted().equals(false) ? "Email sent - Not responded" : "");
			}
			wb.write(out);
			saveBatchUploadReport(tan, tenantId, year, out, Long.valueOf(count), uploadType, "Processed", month,
					userName, batchUpload.getBatchUploadID(), null);
		} catch (Exception e) {
			logger.info("Exception occured while preparing error file {} ", e.getMessage());
		}
	}

	/**
	 *
	 * @param pan
	 * @param tenantId
	 * @param tan
	 * @param userName
	 * @param mailNotTrigger
	 * @param moduleType
	 * @param year2
	 * @throws ParseException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvalidKeyException
	 */
	@Async
	public void customerRecordReport(String deductorPan, String tenantId, String tan, String userName, int year,
			String uploadType, String reportType) throws InvalidKeyException, FileNotFoundException, IOException,
			URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		String kycType = StringUtils.EMPTY;
		if ("CUSTOMER_RECORD_REPORT".equalsIgnoreCase(uploadType)
				|| "CUSTOMER_MAIL_NOT_TRIGGERED_REPORT".equalsIgnoreCase(uploadType)
				|| "CUSTOMER_MAIL_TRIGGERED_REPORT".equalsIgnoreCase(uploadType)) {
			kycType = "CUSTOMER";
		} else if ("VENDOR_RECORD_REPORT".equalsIgnoreCase(uploadType)
				|| "VENDOR_MAIL_NOT_TRIGGERED_REPORT".equalsIgnoreCase(uploadType)
				|| "VENDOR_MAIL_TRIGGERED_REPORT".equalsIgnoreCase(uploadType)) {
			kycType = "VENDOR";
		} else if ("SHAREHOLDER_RECORD_REPORT".equalsIgnoreCase(uploadType)
				|| "SHAREHOLDER_MAIL_NOT_TRIGGERED_REPORT".equalsIgnoreCase(uploadType)
				|| "SHAREHOLDER_MAIL_TRIGGERED_REPORT".equalsIgnoreCase(uploadType)) {
			kycType = "SHAREHOLDER";
		}

		String fileName = uploadType + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		fileName = fileName.replace("KYC_", "");
		BatchUpload batchUpload = saveBatchUploadReport(tan, tenantId, year, null, 0L, uploadType, "Processing", month,
				userName, null, fileName);
		// get kyc list
		List<KYCDetails> kycList = new ArrayList<>();
		if ("MAIL_TRIGGERED".equals(reportType)) {
			kycList = kycDetailsDAO.getAllMailTriggeredReport(deductorPan, tan, year, kycType);
		} else if ("MAIL_NOT_TRIGGERED".equals(reportType)) {
			kycList = kycDetailsDAO.getAllMailNotTriggeredReport(deductorPan, tan, year, kycType);
		} else if ("TOTAL_REPORT".equals(reportType)) {
			kycList = kycDetailsDAO.getKycByTanAndPanAndYear(deductorPan, tan, year, kycType);
		}
		int count = kycList.size();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = null;
		String msg = StringUtils.EMPTY;
		if ("CUSTOMER".equalsIgnoreCase(kycType)) {
			resource = resourceLoader.getResource("classpath:templates/" + "customer_record_report.xlsx");
			msg = getErrorReportMsg(tenantId, deductorPan, "Customer Record Report");
		} else if ("VENDOR".equalsIgnoreCase(kycType)) {
			resource = resourceLoader.getResource("classpath:templates/" + "vendor_record_report.xlsx");
			msg = getErrorReportMsg(tenantId, deductorPan, "Vendor Record Report");
		} else {
			resource = resourceLoader.getResource("classpath:templates/" + "shareholder_record_report.xlsx");
			msg = getErrorReportMsg(tenantId, deductorPan, "Shareholder Record Report");
		}
		InputStream input = resource.getInputStream();
		try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
			XSSFSheet sheet = wb.getSheetAt(0);

			Font fonts = wb.createFont();
			fonts.setBold(true);

			XSSFCellStyle style = wb.createCellStyle();
			style.setFont(fonts);
			style.setWrapText(true);
			style.setBorderLeft(BorderStyle.MEDIUM);
			style.setBorderTop(BorderStyle.MEDIUM);
			style.setBorderBottom(BorderStyle.MEDIUM);
			style.setBorderRight(BorderStyle.MEDIUM);
			style.setAlignment(HorizontalAlignment.LEFT);
			style.setVerticalAlignment(VerticalAlignment.CENTER);

			Font fonts2 = wb.createFont();
			fonts2.setBold(false);

			XSSFCellStyle style1 = wb.createCellStyle();
			style1.setFont(fonts);
			style1.setWrapText(true);
			style1.setFont(fonts2);
			style1.setLocked(true);

			XSSFRow row0 = sheet.createRow(0);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);
			int rowindex = 5;
			for (KYCDetails kyc : kycList) {
				XSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
				createSXSSFCell(style1, row1, 0, kyc.getCustomerName());
				createSXSSFCell(style1, row1, 1, kyc.getCustomerPan());
				createSXSSFCell(style1, row1, 2, kyc.getCustomerCode());
				createSXSSFCell(style1, row1, 3, kyc.getEmailId());
				createSXSSFCell(style1, row1, 4, kyc.getPhoneNumber());
				createSXSSFCell(style1, row1, 5, kyc.getIsEmailTriggered().equals(true) ? "YES" : "NO");
			}
			wb.write(out);
			saveBatchUploadReport(tan, tenantId, year, out, Long.valueOf(count), uploadType, "Processed", month,
					userName, batchUpload.getBatchUploadID(), null);
		} catch (Exception e) {
			logger.info("Exception occured while preparing error file {} ", e.getMessage());
		}
	}

	/**
	 *
	 * @param tan
	 * @param year
	 * @param deductorPan
	 * @param tenantId
	 * @param type
	 * @param userName
	 * @param moduleType
	 * @return
	 * @throws Exception
	 */
	public String getReportsByType(String tan, int year, String deductorPan, String tenantId, String type,
			String userName) throws Exception {
		if ("CUSTOMER_RECORD_REPORT".equalsIgnoreCase(type) || "VENDOR_RECORD_REPORT".equalsIgnoreCase(type)
				|| "SHAREHOLDER_RECORD_REPORT".equalsIgnoreCase(type)) {
			String totalReport = "TOTAL_REPORT";
			customerRecordReport(deductorPan, tenantId, tan, userName, year, type, totalReport);
		} else if ("CUSTOMER_FAILED_RESPONSE_REPORT".equalsIgnoreCase(type)
				|| "VENDOR_FAILED_RESPONSE_REPORT".equalsIgnoreCase(type)
				|| "SHAREHOLDER_FAILED_RESPONSE_REPORT".equalsIgnoreCase(type)) {
			failedResponseReportExcel(deductorPan, tenantId, tan, userName, year, type);
		} else if ("CUSTOMER_SURVEY_RESPONSE_REPORT".equalsIgnoreCase(type)
				|| "VENDOR_SURVEY_RESPONSE_REPORT".equalsIgnoreCase(type)
				|| "SHAREHOLDER_SURVEY_RESPONSE_REPORT".equalsIgnoreCase(type)) {
			surveyResponseReportExcel(deductorPan, tenantId, tan, userName, year, type);
		} else if ("CUSTOMER_KYC_PAN_VALIDATION".equalsIgnoreCase(type)
				|| "VENDOR_KYC_PAN_VALIDATION".equalsIgnoreCase(type)
				|| "SHAREHOLDER_KYC_PAN_VALIDATION".equalsIgnoreCase(type)) {
			kycPANValidationReport(deductorPan, tenantId, tan, userName, year, type);
		} else if ("CUSTOMER_KYC_TDS_VS_TCS_APPLICABILITY_REPORT".equalsIgnoreCase(type)
				|| "VENDOR_KYC_TDS_VS_TCS_APPLICABILITY_REPORT".equalsIgnoreCase(type)
				|| "SHAREHOLDER_KYC_TDS_VS_TCS_APPLICABILITY_REPORT".equalsIgnoreCase(type)) {
			String tdsVsTcsReport = "TDS_VS_TCS_REPORT";
			controlTdsTcsReportExcel(deductorPan, tenantId, tan, userName, year, type, tdsVsTcsReport);
		} else if ("CUSTOMER_MAIL_NOT_TRIGGERED_REPORT".equalsIgnoreCase(type)
				|| "VENDOR_MAIL_NOT_TRIGGERED_REPORT".equalsIgnoreCase(type)
				|| "SHAREHOLDER_MAIL_NOT_TRIGGERED_REPORT".equalsIgnoreCase(type)) {
			String mailNotTriggered = "MAIL_NOT_TRIGGERED";
			customerRecordReport(deductorPan, tenantId, tan, userName, year, type, mailNotTriggered);
		} else if ("CUSTOMER_MAIL_TRIGGERED_REPORT".equalsIgnoreCase(type)
				|| "VENDOR_MAIL_TRIGGERED_REPORT".equalsIgnoreCase(type)
				|| "SHAREHOLDER_MAIL_TRIGGERED_REPORT".equalsIgnoreCase(type)) {
			String mailTriggered = "MAIL_TRIGGERED";
			customerRecordReport(deductorPan, tenantId, tan, userName, year, type, mailTriggered);
		} else if ("CUSTOMER_KYC_TDS_VS_TCS_HOLD_AND_NEW_REPORT".equalsIgnoreCase(type)
				|| "VENDOR_KYC_TDS_VS_TCS_HOLD_AND_NEW_REPORT".equalsIgnoreCase(type)
				|| "SHAREHOLDER_KYC_TDS_VS_TCS_HOLD_AND_NEW_REPORT".equalsIgnoreCase(type)) {
			String holdNewReport = "HOLD_AND_NEW_REPORT";
			controlTdsTcsReportExcel(deductorPan, tenantId, tan, userName, year, type, holdNewReport);
		}
		return "KYC report generated successfully";
	}

	/**
	 *
	 * @param deductorPan
	 * @param tan
	 * @param year
	 * @param type
	 * @return
	 */
	public KYCDetailsControlOutput getControlOutput(String deductorPan, String tan, int year, String type) {
		return kycDetailsDAO.getKYCControlOutput(deductorPan, tan, year, type).get(0);
	}

	/**
	 *
	 * @param pan
	 * @param tenantId
	 * @param tan
	 * @param userName
	 * @param year
	 * @param reportType
	 * @throws ParseException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvalidKeyException
	 */
	public void controlTdsTcsReportExcel(String deductorPan, String tenantId, String tan, String userName, int year,
			String type, String reportType) throws InvalidKeyException, FileNotFoundException, IOException,
			URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		String kycType = StringUtils.EMPTY;
		if ("CUSTOMER_KYC_TDS_VS_TCS_APPLICABILITY_REPORT".equalsIgnoreCase(type)
				|| "CUSTOMER_KYC_TDS_VS_TCS_HOLD_AND_NEW_REPORT".equalsIgnoreCase(type)) {
			kycType = "CUSTOMER";
		}
		String fileName = type + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		fileName = fileName.replace("KYC_", "");
		BatchUpload batchUpload = saveBatchUploadReport(tan, tenantId, year, null, 0L, type, "Processing", month,
				userName, null, fileName);
		List<KYCDetails> kycList = new ArrayList<>();
		if ("TDS_VS_TCS_REPORT".equals(reportType)) {
			kycList = kycDetailsDAO.getKycActionFinalReportList(deductorPan, tan, year, kycType);
		} else if ("HOLD_AND_NEW_REPORT".equals(reportType)) {
			kycList = kycDetailsDAO.getKycHoldAndNewControlReport(deductorPan, tan, year, kycType);
		}

		int count = kycList.size();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = null;
		String msg = StringUtils.EMPTY;
		if ("CUSTOMER".equalsIgnoreCase(kycType)) {
			resource = resourceLoader.getResource("classpath:templates/" + "kyc_tds_vs_tcs_report.xlsx");
			msg = getErrorReportMsg(tenantId, deductorPan, "Customer KYC TDS vs TCS Applicability Report");
		}
		InputStream input = resource.getInputStream();
		try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
			XSSFSheet sheet = wb.getSheetAt(0);
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);

			Font fonts = wb.createFont();
			fonts.setBold(true);

			XSSFCellStyle style = wb.createCellStyle();
			style.setFont(fonts);
			style.setWrapText(true);
			style.setBorderLeft(BorderStyle.MEDIUM);
			style.setBorderTop(BorderStyle.MEDIUM);
			style.setBorderBottom(BorderStyle.MEDIUM);
			style.setBorderRight(BorderStyle.MEDIUM);
			style.setAlignment(HorizontalAlignment.LEFT);
			style.setVerticalAlignment(VerticalAlignment.CENTER);

			Font fonts2 = wb.createFont();
			fonts2.setBold(false);

			XSSFCellStyle style1 = wb.createCellStyle();
			style1.setFont(fonts);
			style1.setWrapText(true);
			style1.setFont(fonts2);
			style1.setLocked(true);

			XSSFRow row0 = sheet.createRow(0);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);

			int rowindex = 5;
			for (KYCDetails kyc : kycList) {
				XSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
				createSXSSFCell(style1, row1, 0, kyc.getCustomerName());
				createSXSSFCell(style1, row1, 1, kyc.getCustomerPan());
				createSXSSFCell(style1, row1, 2, kyc.getCustomerCode());
				createSXSSFCell(style1, row1, 3, kyc.getTurnoverExceed10cr());
				createSXSSFCell(style1, row1, 4, kyc.getTdsTcsApplicabilityIndicator());
				createSXSSFCell(style1, row1, 5, kyc.getTcsTdsApplicabilityUserAction());
				createSXSSFCell(style1, row1, 6, kyc.getTdsTcsClientFinalResponse());
				createSXSSFCell(style1, row1, 7, kyc.getId().toString());
			}
			sheet.setColumnHidden(7, true);
			wb.write(out);
			saveBatchUploadReport(tan, tenantId, year, out, Long.valueOf(count), type, "Processed", month, userName,
					batchUpload.getBatchUploadID(), null);
		} catch (Exception e) {
			logger.info("Exception occured while preparing error file {} ", e.getMessage());
		}
	}

	public static String encrypt(String input) {

		String encryptedString = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update("mail@)(*&^%$#@!".getBytes());
			byte[] bytes = md.digest(input.getBytes());
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			encryptedString = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return encryptedString;
	}

	/**
	 * 
	 * @param deductorTan
	 * @param tenantId
	 * @param deductorPan
	 * @param batchId
	 * @param userName
	 * @param type
	 * @return
	 */
	public Integer getPanMismatchCount(String deductorTan, String deductorPan, String type) {
		return kycDetailsDAO.getPanMismatchCount(deductorPan, deductorTan, type);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tan
	 * @param kycId
	 * @return
	 */
	public List<DeclarationTanFilesDTO> getDeclarationTanFileDetails(String deductorPan, String tan, int kycId) {
		List<DeclarationTanFiles> tanFileList = declarationTanFilesDAO.getDeclarationTanFileDetails(deductorPan, tan,
				kycId);
		List<DeclarationTanFilesDTO> tanFiles = new ArrayList<>();
		for (DeclarationTanFiles tanFile : tanFileList) {
			DeclarationTanFilesDTO tanDto = new DeclarationTanFilesDTO();
			tanDto.setFilePath(tanFile.getFilePath());
			tanDto.setId(tanFile.getId());
			tanDto.setKycId(tanFile.getKycId());
			tanDto.setFileName(tanFile.getFileName());
			tanFiles.add(tanDto);
		}
		return tanFiles;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tan
	 * @param id
	 * @param tenantId
	 * @param type
	 * @return
	 * @throws ParseException
	 */
	@Async
	public void controlTotalMailTriggred(String deductorPan, String tan, String tenantId, String type, int year,
			String mailType) {
		MultiTenantContext.setTenantId(tenantId);
		List<KYCDetails> kycList = new ArrayList<>();
		if ("MAIL_TRIGGRED".equals(mailType)) {
			kycList = kycDetailsDAO.getAllMailNotTriggeredReport(deductorPan, tan, year, type);
		} else if ("NOT_RESPONSE_KYC".equals(mailType)) {
			kycList = kycDetailsDAO.getFailedResponseReport(deductorPan, tan, year, type);
		}
		List<DeductorMaster> response = deductorMasterDAO.findByDeductorPan(deductorPan);
		List<KYCDeclarationEmailDetails> perferenceList = kycDeclarationEmailDetailsDAO.getAllPerferences(tan);
		Boolean isEmailCc = false;
		if (!perferenceList.isEmpty()) {
			isEmailCc = perferenceList.get(0).getIsEmailCc();
		}
		List<KYCDetails> kycDetailsSentList = new ArrayList<>();
		Email email = new Email();
		Context ctx = new Context();
		if (!kycList.isEmpty()) {
			for (KYCDetails kycDetails : kycList) {
				// mail send logic
				mailSend(tan, tenantId, deductorPan, email, ctx, kycDetails, response.get(0).getName(),
						kycDetailsSentList, response.get(0).getEmail(), isEmailCc);
			}
			if (!kycDetailsSentList.isEmpty()) {
				// batch update basic sent date
				kycDetailsDAO.batchUpdateForRedisKeys(kycDetailsSentList);
			}
		}
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tan
	 * @param tenantId
	 * @param type
	 * @param year
	 * @param batchId
	 * @return
	 */
	@Async
	public void batchMailTriggred(String deductorPan, String tan, String tenantId, String type, int batchId) {
		MultiTenantContext.setTenantId(tenantId);
		List<KYCDetails> kycList = kycDetailsDAO.getKycListBasedOnBatchId(batchId, type.toUpperCase(), tan,
				deductorPan);
		List<DeductorMaster> response = deductorMasterDAO.findByDeductorPan(deductorPan);
		List<KYCDeclarationEmailDetails> perferenceList = kycDeclarationEmailDetailsDAO.getAllPerferences(tan);
		Email email = new Email();
		Context ctx = new Context();
		List<KYCDetails> kycDetailsSentList = new ArrayList<>();
		Boolean isEmailCc = false;
		if (!perferenceList.isEmpty()) {
			isEmailCc = perferenceList.get(0).getIsEmailCc();
		}
		for (KYCDetails kyc : kycList) {
			if (kyc.getIsEmailTriggered().equals(true)) {
				// mail send logic
				mailSend(tan, tenantId, deductorPan, email, ctx, kyc, response.get(0).getName(), kycDetailsSentList,
						response.get(0).getEmail(), isEmailCc);
			}
		}
		if (!kycDetailsSentList.isEmpty()) {
			// batch update basic sent date
			kycDetailsDAO.batchUpdateForRedisKeys(kycDetailsSentList);
		}
	}

	@Async
	public void generatetanLevelFile(String tenantId, String deductorTan, String deductorPan, String userName)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		BatchUpload batchUpload = saveBatchUploadReportFile(deductorTan, tenantId, CommonUtil.getAssessmentYear(null),
				null, 0L, UploadTypes.DECLARATION_ENTRY_LEVEL.name(), "Processing", CommonUtil.getAssessmentMonth(null),
				userName, null, deductorTan + "_" + deductorPan);

		try {
			List<String> kycTypes = new ArrayList<String>(Arrays.asList("CUSTOMER", "VENDOR", "SHAREHOLDER"));

			File parentDirectory = new File(deductorTan + "/" + deductorPan);
			if (!parentDirectory.exists()) {
				logger.info("Started creating folders..");
				try {
					parentDirectory.mkdirs();
					logger.info("Parent directory created {}", parentDirectory.getPath());
				} catch (Exception e) {
					logger.error("Failed to create Parent directory! {}", parentDirectory.getPath());
				}
			}
			for (String type : kycTypes) {
				logger.info("Started creating folders.. in type level");
				List<KYCDetails> kycList = kycDetailsDAO.getKycDetailsListForZip(deductorPan, deductorTan, type);

				File worldDirectory = new File(deductorTan + "/" + deductorPan + "/" + type);
				if (!worldDirectory.exists()) {
					try {
						worldDirectory.mkdirs();
						logger.info("Tan level directory created {}", worldDirectory.getPath());
					} catch (Exception e) {
						logger.error("Failed to create Tan level directory! {}", worldDirectory.getPath());
					}
				}

				for (KYCDetails kycDetails : kycList) {

					List<DeclarationTanFiles> tanFileList = declarationTanFilesDAO
							.getDeclarationTanFileDetails(deductorPan, deductorTan, kycDetails.getId());

					List<File> destFiles = new ArrayList<>();
					File fsFilePath = StringUtils.isEmpty(kycDetails.getFsFilePath()) ? null
							: blobStorageService.getFileFromBlobUrl(tenantId, kycDetails.getFsFilePath());
					if (fsFilePath != null) {
						destFiles.add(fsFilePath);
					}
					File panFilePath = StringUtils.isEmpty(kycDetails.getPanFilePath()) ? null
							: blobStorageService.getFileFromBlobUrl(tenantId, kycDetails.getPanFilePath());
					if (panFilePath != null) {
						destFiles.add(panFilePath);
					}
					File itr1FilePath = StringUtils.isEmpty(kycDetails.getItrAttachmentYear1()) ? null
							: blobStorageService.getFileFromBlobUrl(tenantId, kycDetails.getItrAttachmentYear1());
					if (itr1FilePath != null) {
						destFiles.add(itr1FilePath);
					}
					File itr2FilePath = StringUtils.isEmpty(kycDetails.getItrAttachmentYear2()) ? null
							: blobStorageService.getFileFromBlobUrl(tenantId, kycDetails.getItrAttachmentYear2());
					if (itr2FilePath != null) {
						destFiles.add(itr2FilePath);
					}
					File itr3FilePath = StringUtils.isEmpty(kycDetails.getItrAttachmentYear3()) ? null
							: blobStorageService.getFileFromBlobUrl(tenantId, kycDetails.getItrAttachmentYear3());
					if (itr3FilePath != null) {
						destFiles.add(itr3FilePath);
					}

					for (DeclarationTanFiles declarationTanFiles : tanFileList) {
						destFiles.add(
								blobStorageService.getFileFromBlobUrl(tenantId, declarationTanFiles.getFilePath()));
					}

					if (!destFiles.isEmpty()) {
						File subWorldDir = new File(
								deductorTan + "/" + deductorPan + "/" + type + "/" + kycDetails.getCustomerCode());
						if (!subWorldDir.exists()) {
							subWorldDir.mkdirs();
						}
					}
					for (File f : destFiles) {
						Path destPath = new File(deductorTan + "/" + deductorPan + "/" + type + "/"
								+ kycDetails.getCustomerCode() + "/" + f.getName()).toPath();
						Files.copy(f.toPath(), destPath);
					}
				}

				logger.info("Upto type level folder generation done...");
			}

			logger.info("All the folder generation done...  About to generate ZIP file");
			File f = new File(deductorTan + ".zip");
			FileOutputStream fos = new FileOutputStream(f);
			ZipOutputStream zos = new ZipOutputStream(fos);
			addDirToZipArchive(zos, parentDirectory, null);

			zos.flush();
			fos.flush();
			zos.close();
			fos.close();
			logger.info("ZIP generation done...  About to update the batch table ");
			saveBatchUploadReportFile(deductorTan, tenantId, CommonUtil.getAssessmentYear(null), f, 0L,
					UploadTypes.DECLARATION_ENTRY_LEVEL.name(), "Processed", CommonUtil.getAssessmentMonth(null),
					userName, batchUpload.getBatchUploadID(), null);

		} catch (Exception e) {
			logger.error("Failed to generate ZIP file {}", e);
			saveBatchUploadReportFile(deductorTan, tenantId, CommonUtil.getAssessmentYear(null), null, 0L,
					UploadTypes.DECLARATION_ENTRY_LEVEL.name(), "Failed", CommonUtil.getAssessmentMonth(null), userName,
					batchUpload.getBatchUploadID(), null);
		}

	}

	public void addDirToZipArchive(ZipOutputStream zos, File fileToZip, String parrentDirectoryName) throws Exception {
		if (fileToZip == null || !fileToZip.exists()) {
			return;
		}

		String zipEntryName = fileToZip.getName();
		if (parrentDirectoryName != null && !parrentDirectoryName.isEmpty()) {
			zipEntryName = parrentDirectoryName + "/" + fileToZip.getName();
		}

		if (fileToZip.isDirectory()) {
			logger.info("+" + zipEntryName);
			for (File file : fileToZip.listFiles()) {
				addDirToZipArchive(zos, file, zipEntryName);
			}
		} else {
			System.out.println("   " + zipEntryName);
			byte[] buffer = new byte[1024];
			FileInputStream fis = new FileInputStream(fileToZip);
			zos.putNextEntry(new ZipEntry(zipEntryName));
			int length;
			while ((length = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, length);
			}
			zos.closeEntry();
			fis.close();
		}
	}

	public BatchUpload saveBatchUploadReportFile(String deductorTan, String tenantId, int assessmentYear, File file,
			Long noOfRows, String uploadType, String status, int month, String userName, Integer batchId,
			String fileName) throws FileNotFoundException, IOException, URISyntaxException, InvalidKeyException,
			StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String path = null;
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setSuccessFileUrl(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		batchUpload.setActive(true);
		if (StringUtils.isNotBlank(fileName)) {
			batchUpload.setFileName(fileName);
		}
		List<BatchUpload> response = new ArrayList<>();
		if (batchId != null) {
			response = batchUploadDAO.findById(assessmentYear, deductorTan, uploadType, batchId);
		}
		if (!response.isEmpty()) {
			batchUpload = response.get(0);
			if (file != null) {
				path = blob.uploadExcelToBlobWithFile(file, tenantId);
				batchUpload.setStatus(status);
				batchUpload.setFilePath(path);
				batchUpload.setSuccessFileUrl(path);
				batchUpload.setRowsCount(noOfRows);
				batchUpload.setSuccessCount(noOfRows);
				batchUpload.setModifiedBy(userName);
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			} else {
				batchUpload.setModifiedBy(userName);
				batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchUpload.setRowsCount(0l);
			}
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param perferenesDTO
	 * @param logoFile
	 * @return
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	public KYCDeclarationEmailDetails createPreferences(String deductorPan, String deductorTan,
			PerferencsDTO perferenesDTO, String userName) {
		List<KYCDeclarationEmailDetails> perferencesList = kycDeclarationEmailDetailsDAO.getAllPerferences(deductorTan);
		KYCDeclarationEmailDetails perferences = new KYCDeclarationEmailDetails();
		if (!perferencesList.isEmpty()) {
			perferences = perferencesList.get(0);
			perferences.setModifiedBy(userName);
			perferences.setModifiedDate(new Timestamp(new Date().getTime()));
			perferences.setActive(false);
			kycDeclarationEmailDetailsDAO.update(perferences);
		}
		perferences.setActive(true);
		perferences.setCreatedBy(userName);
		perferences.setCreatedDate(new Timestamp(new Date().getTime()));
		perferences.setModifiedBy(userName);
		perferences.setModifiedDate(new Timestamp(new Date().getTime()));
		perferences.setPrimaryColor(perferenesDTO.getPrimaryColor());
		perferences.setSecondaryColor(perferenesDTO.getSecondaryColor());
		perferences.setCustomEmailContent(StringUtils.EMPTY);
		perferences.setSubject(StringUtils.EMPTY);
		perferences.setDeductorMasterTan(deductorTan);
		perferences.setDeductorPan(deductorPan);
		perferences.setIndemnifyDeclare(perferenesDTO.getIndemnifyDeclare());
		perferences.setIsEmailCc(perferenesDTO.getIsEmailCc());
		perferences.setLogo(perferenesDTO.getLogo());
		return kycDeclarationEmailDetailsDAO.save(perferences);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @return
	 */
	public List<KYCDeclarationEmailDetails> getAllPreferences(String deductorPan, String deductorTan) {
		return kycDeclarationEmailDetailsDAO.getAllPerferences(deductorTan);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param userName
	 * @return
	 */
	public String createApproved(String deductorPan, String deductorTan, String userName, String type) {
		List<KYCDeclarationEmailDetails> perferencesList = kycDeclarationEmailDetailsDAO.getAllPerferences(deductorTan);
		KYCDeclarationEmailDetails perferences = new KYCDeclarationEmailDetails();
		if (!perferencesList.isEmpty()) {
			perferences = perferencesList.get(0);
			perferences.setModifiedBy(userName);
			perferences.setModifiedDate(new Timestamp(new Date().getTime()));
			perferences.setIsApproved(true);
			perferences.setType(type);
			kycDeclarationEmailDetailsDAO.updateApproved(perferences);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param type
	 * @return
	 */
	public Boolean getApproved(String deductorPan, String deductorTan, String type) {
		List<KYCDeclarationEmailDetails> perferencesList = kycDeclarationEmailDetailsDAO.getAllPerferences(deductorTan,
				type);
		Boolean approvedCheck = null;
		if (!perferencesList.isEmpty()) {
			return perferencesList.get(0).getIsApproved();
		} else {
			return approvedCheck;
		}

	}

}
