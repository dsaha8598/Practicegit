package com.ey.in.tds.onboarding.service.deductee;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.aspose.cells.Cells;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tcs.common.domain.CollecteeGstinAndCollecteeDTO;
import com.ey.in.tcs.common.domain.CollecteeGstinStatus;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.dividend.ShareholderGstinStatus;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.AuthenticateDTO;
import com.ey.in.tds.common.dto.GSTINValidationAndFilingDTO;
import com.ey.in.tds.common.dto.GstinReturnFiliingDTO;
import com.ey.in.tds.common.dto.GstindetailsDTO;
import com.ey.in.tds.common.model.deductee.DeducteeGstinAndDeducteeResDTO;
import com.ey.in.tds.common.model.deductee.DeducteeGstinStatus;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.jdbc.dao.CollecteeGstinStatusDAO;
import com.ey.in.tds.jdbc.dao.CollecteeMasterDAO;
import com.ey.in.tds.jdbc.dao.DeducteeGstinStatusDAO;
import com.ey.in.tds.jdbc.dao.DeducteeMasterResidentialDAO;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.jdbc.dao.ShareholderGstinStatusDAO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

/**
 * 
 * @author vamsir
 *
 */
@Service
public class GstinStatusService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DeducteeMasterResidentialDAO deducteeMasterResidentialDAO;

	@Autowired
	private CollecteeMasterDAO collecteeMasterDAO;

	@Value("${tds.declaration.endpoint}")
	private String username;

	@Value("${tds.declaration.endpoint}")
	private String password;

	@Value("${tds.declaration.endpoint}")
	private String api_key;

	@Value("${gstin.url.authentication}")
	private String gstin_authentication_url;

	@Value("${gstin.url.validation}")
	private String gstin_validation_url;

	@Value("${gstin.url.return_filling}")
	private String gstin_return_filing_url;

	@Value("${gstin.api_key}")
	private String gstin_api_key;

	@Value("${gstin.username}")
	private String gstin_username;

	@Value("${gstin.password}")
	private String gstin_password;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private DeducteeGstinStatusDAO deducteeGstinStatusDAO;

	@Autowired
	private CollecteeGstinStatusDAO collecteeGstinStatusDAO;

	@Autowired
	private ShareholderGstinStatusDAO shareholderGstinStatusDAO;

	@Autowired
	private TCSBatchUploadDAO tcsbatchUploadDAO;

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param tan
	 * @param deductorPan
	 * @param tenantId
	 * @param userName
	 * @param month
	 * @param year
	 * @return
	 * @throws Exception
	 */
	public void getGstinStatusValidation(String deductorTan, String deductorPan, String userName, String year,
			Integer month, String tenantId) throws Exception {

		logger.info("Enterted into getGstinStatusValidation async method");
		ObjectMapper obj = new ObjectMapper();
		Integer finatialYear = Integer.valueOf(String.valueOf(year).substring(0, 4));
		Integer assessmentYear = finatialYear + 1;
		
		Integer taxPeriodYear = finatialYear;
		if (month.equals(1) || month.equals(2)
				|| month.equals(3)) {
			taxPeriodYear = assessmentYear;
		}

		String uploadType = UploadTypes.GSTIN_DEDUCTEE_REPORT.name();
		String fileName = uploadType + "_" + month + "_" + assessmentYear + ".xlsx";
		BatchUpload batchUpload = saveBatchUploadReport(deductorTan, tenantId, assessmentYear, null, 0L, uploadType,
				"Processing", month, userName, null, fileName);

		// get all deductee gstin numbers
		List<DeducteeMasterResidential> deducteeGstinList = deducteeMasterResidentialDAO
				.getAllDeducteeGstin(deductorTan, deductorPan);
		logger.info("Size of deductees for the GSTIN validation {}", deducteeGstinList.size());
		try {

			// calling authentication API
			ResponseEntity<AuthenticateDTO> result = callingAuthenticationApi();

			String challanAssessmentYear = String.valueOf(finatialYear) + "-"
					+ String.valueOf(assessmentYear).substring(2, 4);
			String challanNextAssessmentYear = String.valueOf(finatialYear + 1) + "-"
					+ String.valueOf(assessmentYear + 1).substring(2, 4);
			String retrunFillingStatusValue = "return_filing_status_" + challanAssessmentYear;

			String retrunNextYearFillingStatusValue = "return_filing_status_" + challanNextAssessmentYear;

			if (result.getBody().getAccesstoken() != null && !deducteeGstinList.isEmpty()) {
				logger.info("Got acess token and about to enter into for-loop for Deductee");
				for (DeducteeMasterResidential deductee : deducteeGstinList) {
					if (StringUtils.isNotBlank(deductee.getDeducteeGSTIN())) {
						try {
							DeducteeGstinStatus deducteeGstinStatus = new DeducteeGstinStatus();
							List<GstinReturnFiliingDTO> sacList = new ArrayList<>();
							// calling gstin validation URL
							GstindetailsDTO gstindetailsValidation = callingGstinValidApi(year, result,
									deductee.getDeducteeGSTIN());

							logger.info("GSTIN validation API response {}", gstindetailsValidation.toString());
							logger.info("GSTIN validation API response {}", gstindetailsValidation);

							logger.info("GSTIN status {}", gstindetailsValidation.getMessage() == null ? "valid "
									: gstindetailsValidation.getMessage());

							if (StringUtils.isBlank(gstindetailsValidation.getMessage())
									|| !gstindetailsValidation.getMessage().equals("Invalid GSTIN/UID")) {

								// calling return Validation API url
								ResponseEntity<GSTINValidationAndFilingDTO> filingStatusResult = callingReturnApi(year,
										result, deductee.getDeducteeGSTIN());
								Map gstindetails = filingStatusResult.getBody().getGstinDetails();
								// gstinValues
								String gstinJson = obj.writeValueAsString(gstindetails);
								GstindetailsDTO gstindetailsDTO = obj.readValue(gstinJson, GstindetailsDTO.class);

								sacList = extracteReturnListFromJson(obj, retrunFillingStatusValue, sacList,
										filingStatusResult);

								if (month.equals(1) || month.equals(2) || month.equals(3)) {
									ResponseEntity<GSTINValidationAndFilingDTO> filingStatusResultNextYear = callingReturnApi(
											challanNextAssessmentYear, result, deductee.getDeducteeGSTIN());
									sacList = extracteReturnListFromJson(obj, retrunNextYearFillingStatusValue, sacList,
											filingStatusResultNextYear);
								}
								if (sacList == null) {
									sacList = new ArrayList<>();
								}

								// gstin details
								deducteeGstinStatus.setLegalNameOfBusiness(gstindetailsDTO.getLgnm());
								deducteeGstinStatus.setDateOfCancellation(gstindetailsDTO.getCxdt());
								deducteeGstinStatus.setLastUpdatedDate(gstindetailsDTO.getRgdt());
								deducteeGstinStatus.setTradeName("");
								deducteeGstinStatus.setGstnStatus(gstindetailsDTO.getSts());
								deducteeGstinStatus.setDeducteeId(deductee.getDeducteeMasterId());
								deducteeGstinStatus.setTaxPeriodByUser(month.toString() + taxPeriodYear);
							} else {// check for invalid GSTIN
								deducteeGstinStatus.setDeducteeId(deductee.getDeducteeMasterId());
								deducteeGstinStatus.setGstnStatus("Invalid");
								deducteeGstinStatus.setTaxPeriodByUser(month.toString() + taxPeriodYear);
							}

							// Return filling details
							for (GstinReturnFiliingDTO gstinReturnFiliingDTO : sacList) {
								String returnPeriod = gstinReturnFiliingDTO.getRet_prd();
								Integer returnPeriodMonth = Integer.valueOf(returnPeriod.substring(0, 2));
								Integer returnPeriodYear = Integer.valueOf(returnPeriod.substring(2, 6));

								Integer comparisonYear = finatialYear;
								if (returnPeriodMonth.equals(1) || returnPeriodMonth.equals(2)
										|| returnPeriodMonth.equals(3)) {
									comparisonYear = assessmentYear;
								}
								logger.info("retrunFillingStatusValue {} and returnPeriod {}", retrunFillingStatusValue,
										returnPeriod);
								
								if ((month.equals(returnPeriodMonth)) && (returnPeriodYear.equals(comparisonYear))) {

									logger.info("satisfied month and year check for GSTR values",
											gstinReturnFiliingDTO.getRet_prd());
									// Tax period Year
									if (gstinReturnFiliingDTO.getRtntype().equalsIgnoreCase("GSTR1")) {
										deducteeGstinStatus.setGstr1DateOfFiling(gstinReturnFiliingDTO.getDof());
										deducteeGstinStatus.setGstr1IsReturnValid(gstinReturnFiliingDTO.getValid());
										deducteeGstinStatus.setGstr1ReturnFiled(gstinReturnFiliingDTO.getStatus());
									} else if (gstinReturnFiliingDTO.getRtntype().equalsIgnoreCase("GSTR3B")) {
										deducteeGstinStatus.setGstr3DateOfFiling(gstinReturnFiliingDTO.getDof());
										deducteeGstinStatus.setGstr3IsReturnValid(gstinReturnFiliingDTO.getValid());
										deducteeGstinStatus.setGstr3ReturnFiled(gstinReturnFiliingDTO.getStatus());
									} else if (gstinReturnFiliingDTO.getRtntype().equalsIgnoreCase("GSTR9")) {
										deducteeGstinStatus.setGstr9DateOfFiling(gstinReturnFiliingDTO.getDof());
										deducteeGstinStatus.setGstr9IsReturnValid(gstinReturnFiliingDTO.getValid());
										deducteeGstinStatus.setGstr9ReturnFiled(gstinReturnFiliingDTO.getStatus());
									} else if (gstinReturnFiliingDTO.getRtntype().equalsIgnoreCase("GSTR6")) {
										deducteeGstinStatus.setGstr6DateOfFiling(gstinReturnFiliingDTO.getDof());
										deducteeGstinStatus.setGstr6IsReturnValid(gstinReturnFiliingDTO.getValid());
										deducteeGstinStatus.setGstr6ReturnFiled(gstinReturnFiliingDTO.getStatus());
									}
								}

							}
							deducteeGstinStatus.setActive(true);
							deducteeGstinStatus.setCreatedBy(userName);
							deducteeGstinStatus.setCreatedDate(new Date());
							deducteeGstinStatus.setYear(assessmentYear);
							deducteeGstinStatus.setMonth(month);
							deducteeGstinStatus.setDeductorMasterTan(deductorTan);
							deducteeGstinStatus.setDeductorPan(deductorPan);

							// get all deductee gstin status based on deductee id.
							List<DeducteeGstinStatus> gstinList = deducteeGstinStatusDAO.getAllGstinBasedOnDeducteeId(
									deductorTan, deductorPan, month, assessmentYear, deductee.getDeducteeMasterId());
							if (!gstinList.isEmpty()) {
								deducteeGstinStatus.setId(gstinList.get(0).getId());
								deducteeGstinStatus.setModifiedBy(userName);
								deducteeGstinStatus.setModifiedDate(new Date());
								// InActive deductee status
								deducteeGstinStatusDAO.deducteeGstinInActive(deducteeGstinStatus);
							}
							// Saving deductee Gstn status
							deducteeGstinStatusDAO.save(deducteeGstinStatus);
							logger.info("Data inserted for deductee GSTIN");
						} catch (Exception e) {
							logger.error("Exception occurred while requesting gstin validation :{}", e);
						}
					}
				}
			}

			getGstinReportExcel(deductorTan, assessmentYear, deductorPan, tenantId, userName, batchUpload, month);
		} catch (Exception e) {
			logger.error("Exception occurred while requesting gstin authenticate api :{}", e);

			// Downloading exception in file
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			Cells cells = worksheet.getCells();
			worksheet.setName("Error Details");
			com.aspose.cells.Cell cell = cells.get("A1");
			cell.setValue("Error Message");
			// Adding a double value to the cell
			cell = cells.get("A2");
			cell.setValue(e.getMessage());
			// Write the output to the file
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.XLSX);
			File file = new File("Error_File", ".xlsx");
			FileUtils.writeByteArrayToFile(file, baout.toByteArray());

			saveBatchUploadReportWithFile(deductorTan, tenantId, assessmentYear, file, 1L, batchUpload.getUploadType(),
					"Processed", batchUpload.getAssessmentMonth(), userName, batchUpload.getBatchUploadID(),
					file.getName());
		}

	}

	private ResponseEntity<AuthenticateDTO> callingAuthenticationApi() throws URISyntaxException {
		RestTemplate restTemplate = new RestTemplate();
		URI uri = new URI(gstin_authentication_url);

		HttpHeaders headers = new HttpHeaders();
		headers.add("username", gstin_username);
		headers.add("password", gstin_password);
		headers.add("api_key", gstin_api_key);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("username", gstin_username);
		map.add("password", gstin_password);
		map.add("api_key", gstin_api_key);
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
		ResponseEntity<AuthenticateDTO> result = restTemplate.exchange(uri, HttpMethod.POST, entity,
				AuthenticateDTO.class);
		return result;
	}

	private GstindetailsDTO callingGstinValidApi(String year, ResponseEntity<AuthenticateDTO> result,
			String gstinNumber)
			throws URISyntaxException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException {

		RestTemplate restTemplate = getRestTemplate();
		URI gstinvalidationUri = new URI(gstin_validation_url);

		HttpHeaders headers1 = new HttpHeaders();
		headers1.add("accesstoken", result.getBody().getAccesstoken());
		headers1.add("api_key", gstin_api_key);
		headers1.add("Content-Type", "application/json");

		Map<String, Object> gstinMap = new HashMap<>();
		gstinMap.put("gstin", gstinNumber);
		gstinMap.put("fy", year);
		HttpEntity<Map<String, Object>> validationRequest = new HttpEntity<>(gstinMap, headers1);
		ResponseEntity<GstindetailsDTO> validationResult = restTemplate.exchange(gstinvalidationUri, HttpMethod.POST,
				validationRequest, GstindetailsDTO.class);

		GstindetailsDTO gstindetailsValidation = validationResult.getBody();

		logger.info("Deductee Response for the GSTIN Validation API {}", validationResult);
		return gstindetailsValidation;
	}

	private List<GstinReturnFiliingDTO> extracteReturnListFromJson(ObjectMapper obj, String retrunFillingStatusValue,
			List<GstinReturnFiliingDTO> sacList, ResponseEntity<GSTINValidationAndFilingDTO> filingStatusResult)
			throws JsonProcessingException, JsonMappingException {
		// current year values
		Map retunrdetails = filingStatusResult.getBody().getReturnDetails(retrunFillingStatusValue);
		// return type values
		String retunFillingJson = obj.writeValueAsString(retunrdetails.get("EFiledlist"));
		if (StringUtils.isNotBlank(retunFillingJson)) {
			sacList = obj.readValue(retunFillingJson, new TypeReference<List<GstinReturnFiliingDTO>>() {
			});
		}
		return sacList;
	}

	private ResponseEntity<GSTINValidationAndFilingDTO> callingReturnApi(String year,
			ResponseEntity<AuthenticateDTO> result, String gstinNumber)
			throws URISyntaxException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
		RestTemplate restTemplate = getRestTemplate();
		Map<String, Object> gstinReturnMap = new HashMap<>();
		gstinReturnMap.put("gstin", gstinNumber);
		gstinReturnMap.put("fy", year);
		// calling return filling API
		URI gstinFilingStatusUri = new URI(gstin_return_filing_url);
		HttpHeaders headers2 = new HttpHeaders();
		headers2.add("accesstoken", result.getBody().getAccesstoken());
		headers2.add("api_key", gstin_api_key);
		headers2.add("Content-Type", "application/json");

		HttpEntity<Map<String, Object>> gstinReturnFilingStatus = new HttpEntity<>(gstinReturnMap, headers2);

		ResponseEntity<GSTINValidationAndFilingDTO> filingStatusResult = restTemplate.exchange(gstinFilingStatusUri,
				HttpMethod.POST, gstinReturnFilingStatus, GSTINValidationAndFilingDTO.class);
		logger.info("Response from gstin return filing status API :{}", filingStatusResult.getBody());
		return filingStatusResult;
	}

	/**
	 * 
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
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

	@Async
	public void getAsyncgetGstinStatusValidation(String deductorTan, String deductorPan, String userName, String year,
			int month, String tenantId) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		getGstinStatusValidation(deductorTan, deductorPan, userName, year, month, tenantId);
	}

	/**
	 * 
	 * @param tan
	 * @param year
	 * @param deductorPan
	 * @param tenantId
	 * @param userName2
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	public void getGstinReportExcel(String tan, int year, String deductorPan, String tenantId, String userName,
			BatchUpload batchUpload, int month)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);

		logger.info("Enterted into deductee GSTIN validation file generation method");
		// get all deductee gstin
		List<DeducteeGstinAndDeducteeResDTO> gstinList = deducteeGstinStatusDAO.getAllDeducteeGstin(year, deductorPan,
				tan, month);
		List<DeductorMaster> getDeductor = deductorMasterDAO.findBasedOnDeductorPan(deductorPan);
		int count = gstinList.size();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = null;
		String msg = StringUtils.EMPTY;
		resource = resourceLoader.getResource("classpath:templates/" + "gstin_deductee_report.xlsx");
		msg = getErrorReportMsg(tenantId, getDeductor.get(0).getName(), "GSTIN DEDUCTEE REPORT");
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
			for (DeducteeGstinAndDeducteeResDTO gstin : gstinList) {
				XSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));

				createSXSSFCell(style1, row1, 0, getDeductor.get(0).getName());
				createSXSSFCell(style1, row1, 1, gstin.getDeductorPan());
				createSXSSFCell(style1, row1, 2, gstin.getDeducteeCode());
				createSXSSFCell(style1, row1, 3, gstin.getDeducteeName());
				createSXSSFCell(style1, row1, 4, gstin.getDeducteePAN());
				createSXSSFCell(style1, row1, 5, gstin.getDeducteeGSTIN());
				createSXSSFCell(style1, row1, 6, gstin.getTaxPeriodByUser());
				createSXSSFCell(style1, row1, 7, gstin.getLegalNameOfBusiness());
				createSXSSFCell(style1, row1, 8, gstin.getGstnStatus());
				if (StringUtils.isNotBlank(gstin.getDateOfCancellation())) {
					String dateOfCancellation = gstin.getDateOfCancellation().replace("-", "/");
					createSXSSFCell(style1, row1, 9, dateOfCancellation);
				} else {
					createSXSSFCell(style1, row1, 9, "");
				}
				if (StringUtils.isNotBlank(gstin.getLastUpdatedDate())) {
					String lastUpdateDate = gstin.getLastUpdatedDate().replace("-", "/");
					createSXSSFCell(style1, row1, 10, lastUpdateDate);
				} else {
					createSXSSFCell(style1, row1, 10, "");
				}
				createSXSSFCell(style1, row1, 11, gstin.getTradeName());
				if (StringUtils.isNotBlank(gstin.getGstr1DateOfFiling())) {
					String gstr1Date = gstin.getGstr1DateOfFiling().replace("/", "-");
					createSXSSFCell(style1, row1, 12, gstr1Date);
				} else {
					createSXSSFCell(style1, row1, 12, "");
				}
				createSXSSFCell(style1, row1, 13, gstin.getGstr1ReturnFiled());
				createSXSSFCell(style1, row1, 14, gstin.getGstr1IsReturnValid());
				if (StringUtils.isNotBlank(gstin.getGstr3DateOfFiling())) {
					String gstr3Date = gstin.getGstr3DateOfFiling().replace("/", "-");
					createSXSSFCell(style1, row1, 15, gstr3Date);
				} else {
					createSXSSFCell(style1, row1, 15, "");
				}
				createSXSSFCell(style1, row1, 16, gstin.getGstr3ReturnFiled());
				createSXSSFCell(style1, row1, 17, gstin.getGstr3IsReturnValid());
				if (StringUtils.isNotBlank(gstin.getGstr9DateOfFiling())) {
					String gstr9Date = gstin.getGstr9DateOfFiling().replace("/", "-");
					createSXSSFCell(style1, row1, 18, gstr9Date);
				} else {
					createSXSSFCell(style1, row1, 18, "");
				}
				createSXSSFCell(style1, row1, 19, gstin.getGstr9ReturnFiled());
				createSXSSFCell(style1, row1, 20, gstin.getGstr9IsReturnValid());
				if (StringUtils.isNotBlank(gstin.getGstr6DateOfFiling())) {
					String gstr6Date = gstin.getGstr6DateOfFiling().replace("/", "-");
					createSXSSFCell(style1, row1, 21, gstr6Date);
				} else {
					createSXSSFCell(style1, row1, 21, "");
				}
				createSXSSFCell(style1, row1, 22, gstin.getGstr6ReturnFiled());
				createSXSSFCell(style1, row1, 23, gstin.getGstr6IsReturnValid());

			}
			wb.write(out);
			saveBatchUploadReport(tan, tenantId, year, out, Long.valueOf(count), batchUpload.getUploadType(),
					"Processed", batchUpload.getAssessmentMonth(), userName, batchUpload.getBatchUploadID(), null);
		} catch (Exception e) {
			logger.info("Exception occured while preparing error file {} ", e);
		}
		logger.info("Deductee GSTIN validation file generated sucessfully");
	}

	/**
	 * @param style
	 * @param row
	 * @param cellNumber
	 * @param value
	 */
	private static void createSXSSFCell(XSSFCellStyle style, XSSFRow row, int cellNumber, String value) {
		Cell cell = row.createCell(cellNumber);
		cell.setCellValue(StringUtils.isBlank(value) == true ? StringUtils.EMPTY : value);
		cell.setCellStyle(style);
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
		if (StringUtils.isBlank(fileName)) {
			fileName = uploadType + "_" + month + "_" + assessmentYear + ".xlsx";
		}
		batchUpload.setFileName(fileName);
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

	public BatchUpload saveBatchUploadReportWithFile(String deductorTan, String tenantId, int assessmentYear, File file,
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
		if (StringUtils.isBlank(fileName)) {
			fileName = uploadType + "_" + month + "_" + assessmentYear + ".xlsx";
		}
		batchUpload.setFileName(fileName);
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
	 * @param deductorMasterTan
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 */
	public String getErrorReportMsg(String tenantId, String deductorName, String fileType) {
		MultiTenantContext.setTenantId(tenantId);
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return fileType + " (Dated: " + date + ")\n Client Name: " + deductorName + "\n";
	}

	/**
	 * 
	 * @param tan
	 * @param year
	 * @param deductorPan
	 * @param tenantId
	 * @param userName2
	 * @throws ParseException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InvalidKeyException
	 */
	public void getGstinCollecteeReportExcel(String tan, int year, String deductorPan, String tenantId, String userName,
			TCSBatchUpload batchUpload, int month) throws InvalidKeyException, FileNotFoundException, IOException,
			URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);

		logger.info("Enterted into collectee GSTIN validation file generation method");
		// get all collectee gstin
		List<CollecteeGstinAndCollecteeDTO> gstinList = collecteeGstinStatusDAO.getAllCollecteeGstin(year, deductorPan,
				tan, month);
		List<DeductorMaster> getDeductor = deductorMasterDAO.findBasedOnDeductorPan(deductorPan);
		int count = gstinList.size();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = null;
		String msg = StringUtils.EMPTY;
		resource = resourceLoader.getResource("classpath:templates/" + "gstin_collectee_report.xlsx");
		msg = getErrorReportMsg(tenantId, getDeductor.get(0).getName(), "GSTIN COLLECTEE REPORT");
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
			for (CollecteeGstinAndCollecteeDTO gstin : gstinList) {
				XSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));

				createSXSSFCell(style1, row1, 0, getDeductor.get(0).getName());
				createSXSSFCell(style1, row1, 1, gstin.getCollectorPan());
				createSXSSFCell(style1, row1, 2, gstin.getCollecteeCode());
				createSXSSFCell(style1, row1, 3, gstin.getNameOfTheCollectee());
				createSXSSFCell(style1, row1, 4, gstin.getCollecteePan());
				createSXSSFCell(style1, row1, 5, gstin.getGstinNumber());
				createSXSSFCell(style1, row1, 6, gstin.getTaxPeriodByUser());
				createSXSSFCell(style1, row1, 7, gstin.getLegalNameOfBusiness());
				createSXSSFCell(style1, row1, 8, gstin.getGstnStatus());
				if (StringUtils.isNotBlank(gstin.getDateOfCancellation())) {
					String dateOfCancellation = gstin.getDateOfCancellation().replace("-", "/");
					createSXSSFCell(style1, row1, 9, dateOfCancellation);
				} else {
					createSXSSFCell(style1, row1, 9, "");
				}
				if (StringUtils.isNotBlank(gstin.getLastUpdatedDate())) {
					String lastUpdateDate = gstin.getLastUpdatedDate().replace("-", "/");
					createSXSSFCell(style1, row1, 10, lastUpdateDate);
				} else {
					createSXSSFCell(style1, row1, 10, "");
				}
				createSXSSFCell(style1, row1, 11, gstin.getTradeName());
				if (StringUtils.isNotBlank(gstin.getGstr1DateOfFiling())) {
					String gstr1Date = gstin.getGstr1DateOfFiling().replace("/", "-");
					createSXSSFCell(style1, row1, 12, gstr1Date);
				} else {
					createSXSSFCell(style1, row1, 12, "");
				}
				createSXSSFCell(style1, row1, 13, gstin.getGstr1ReturnFiled());
				createSXSSFCell(style1, row1, 14, gstin.getGstr1IsReturnValid());
				if (StringUtils.isNotBlank(gstin.getGstr3DateOfFiling())) {
					String gstr3Date = gstin.getGstr3DateOfFiling().replace("/", "-");
					createSXSSFCell(style1, row1, 15, gstr3Date);
				} else {
					createSXSSFCell(style1, row1, 15, "");
				}
				createSXSSFCell(style1, row1, 16, gstin.getGstr3ReturnFiled());
				createSXSSFCell(style1, row1, 17, gstin.getGstr3IsReturnValid());
				if (StringUtils.isNotBlank(gstin.getGstr9DateOfFiling())) {
					String gstr9Date = gstin.getGstr9DateOfFiling().replace("/", "-");
					createSXSSFCell(style1, row1, 18, gstr9Date);
				} else {
					createSXSSFCell(style1, row1, 18, "");
				}
				createSXSSFCell(style1, row1, 19, gstin.getGstr9ReturnFiled());
				createSXSSFCell(style1, row1, 20, gstin.getGstr9IsReturnValid());
				if (StringUtils.isNotBlank(gstin.getGstr6DateOfFiling())) {
					String gstr6Date = gstin.getGstr6DateOfFiling().replace("/", "-");
					createSXSSFCell(style1, row1, 21, gstr6Date);
				} else {
					createSXSSFCell(style1, row1, 21, "");
				}
				createSXSSFCell(style1, row1, 22, gstin.getGstr6ReturnFiled());
				createSXSSFCell(style1, row1, 23, gstin.getGstr6IsReturnValid());

			}
			wb.write(out);
			saveTcsBatchUploadReport(tan, tenantId, year, out, Long.valueOf(count), batchUpload.getUploadType(),
					"Processed", batchUpload.getAssessmentMonth(), userName, batchUpload.getId(), null);
			logger.info("Generated collectee GSTIN validation report");
		} catch (Exception e) {
			logger.info("Exception occured while preparing error file {} ", e);
		}
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
	public TCSBatchUpload saveTcsBatchUploadReport(String deductorTan, String tenantId, int assessmentYear,
			ByteArrayOutputStream out, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId, String fileName) throws FileNotFoundException, IOException, URISyntaxException,
			InvalidKeyException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String path = null;
		TCSBatchUpload batchUpload = new TCSBatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setCollectorMasterTan(deductorTan);
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
			fileName = uploadType + "_" + month + "_" + assessmentYear + ".xlsx";
			batchUpload.setFileName(fileName);
		}
		List<TCSBatchUpload> response = new ArrayList<>();
		if (batchId != null) {
			response = tcsbatchUploadDAO.findById(assessmentYear, deductorTan, uploadType, batchId);
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
			batchUpload = tcsbatchUploadDAO.update(batchUpload);
		} else {
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload = tcsbatchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	public TCSBatchUpload saveTcsBatchUploadReportFile(String deductorTan, String tenantId, int assessmentYear,
			File file, Long noOfRows, String uploadType, String status, int month, String userName, Integer batchId,
			String fileName) throws FileNotFoundException, IOException, URISyntaxException, InvalidKeyException,
			StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		String path = null;
		TCSBatchUpload batchUpload = new TCSBatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setCollectorMasterTan(deductorTan);
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
			fileName = uploadType + "_" + month + "_" + assessmentYear + ".xlsx";
			batchUpload.setFileName(fileName);
		}
		List<TCSBatchUpload> response = new ArrayList<>();
		if (batchId != null) {
			response = tcsbatchUploadDAO.findById(assessmentYear, deductorTan, uploadType, batchId);
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
			batchUpload = tcsbatchUploadDAO.update(batchUpload);
		} else {
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload = tcsbatchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param deductorTan
	 * @param deductorPan
	 * @param userName
	 * @param year
	 * @param month
	 * @return
	 * @throws Exception
	 */
	public void getGstinCollecteeStatusValidation(String deductorTan, String deductorPan, String userName, String year,
			Integer month, String tenantId) throws Exception {

		logger.info("Enterted collectee into getGstinStatusValidation async method");
		ObjectMapper obj = new ObjectMapper();
		Integer finatialYear = Integer.valueOf(String.valueOf(year).substring(0, 4));
		Integer assessmentYear = finatialYear + 1;
		
		Integer taxPeriodYear = finatialYear;
		if (month.equals(1) || month.equals(2)
				|| month.equals(3)) {
			taxPeriodYear = assessmentYear;
		}

		String uploadType = UploadTypes.GSTIN_COLLECTEE_REPORT.name();
		String fileName = uploadType + "_" + month + "_" + assessmentYear + ".xlsx";
		TCSBatchUpload batchUpload = saveTcsBatchUploadReport(deductorTan, tenantId, assessmentYear, null, 0L,
				uploadType, "Processing", month, userName, null, fileName);
		// get all collectee gstin numbers
		List<CollecteeMaster> collecteeGstinList = collecteeMasterDAO.getAllCollecteeGstin(deductorPan, deductorTan);
		logger.info("Size of deductees for the GSTIN validation {}", collecteeGstinList.size());
		try {
			// calling authentication API
			ResponseEntity<AuthenticateDTO> result = callingAuthenticationApi();

			String challanAssessmentYear = String.valueOf(finatialYear) + "-"
					+ String.valueOf(assessmentYear).substring(2, 4);
			String challanNextAssessmentYear = String.valueOf(finatialYear + 1) + "-"
					+ String.valueOf(assessmentYear + 1).substring(2, 4);
			String retrunFillingStatusValue = "return_filing_status_" + challanAssessmentYear;

			String retrunNextYearFillingStatusValue = "return_filing_status_" + challanNextAssessmentYear;

			if (result.getBody().getAccesstoken() != null && !collecteeGstinList.isEmpty()) {
				logger.info("Got acess token and about to enter into for-loop for Collectee");
				for (CollecteeMaster collectee : collecteeGstinList) {
					if (StringUtils.isNotBlank(collectee.getGstinNumber())) {
						try {
							CollecteeGstinStatus colecteeGstinStatus = new CollecteeGstinStatus();
							List<GstinReturnFiliingDTO> sacList = new ArrayList<>();
							// calling gstin validation URL
							GstindetailsDTO gstindetailsValidation = callingGstinValidApi(year, result,
									collectee.getGstinNumber());

							logger.info("GSTIN validation API response {}", gstindetailsValidation.toString());
							logger.info("GSTIN validation API response {}", gstindetailsValidation);

							logger.info("GSTIN collectevalidation API response {}", gstindetailsValidation.getGstin());
							logger.info("Got status code of collectee {}", gstindetailsValidation.getMessage());

							logger.info("Got a valid GSTIN for collectee GSTIn API {}",
									gstindetailsValidation.getGstin());
							if (StringUtils.isBlank(gstindetailsValidation.getMessage())
									|| !gstindetailsValidation.getMessage().equals("Invalid GSTIN/UID")) {
								// calling return Validation API url
								ResponseEntity<GSTINValidationAndFilingDTO> filingStatusResult = callingReturnApi(year,
										result, collectee.getGstinNumber());
								Map gstindetails = filingStatusResult.getBody().getGstinDetails();
								// gstinValues
								String gstinJson = obj.writeValueAsString(gstindetails);
								GstindetailsDTO gstindetailsDTO = obj.readValue(gstinJson, GstindetailsDTO.class);

								sacList = extracteReturnListFromJson(obj, retrunFillingStatusValue, sacList,
										filingStatusResult);

								if (month.equals(1) || month.equals(2) || month.equals(3)) {
									ResponseEntity<GSTINValidationAndFilingDTO> filingStatusResultNextYear = callingReturnApi(
											challanNextAssessmentYear, result, collectee.getGstinNumber());
									sacList = extracteReturnListFromJson(obj, retrunNextYearFillingStatusValue, sacList,
											filingStatusResultNextYear);

								}

								if (sacList == null) {
									sacList = new ArrayList<>();
								}

								// gstin details
								colecteeGstinStatus.setLegalNameOfBusiness(gstindetailsDTO.getLgnm());
								colecteeGstinStatus.setDateOfCancellation(gstindetailsDTO.getCxdt());
								colecteeGstinStatus.setLastUpdatedDate(gstindetailsDTO.getRgdt());
								colecteeGstinStatus.setTradeName("");
								colecteeGstinStatus.setGstnStatus(gstindetailsDTO.getSts());
								colecteeGstinStatus.setCollecteeId(collectee.getId());
								colecteeGstinStatus.setTaxPeriodByUser(month.toString() + taxPeriodYear);
							} else {
								colecteeGstinStatus.setGstnStatus("Invalid");
								colecteeGstinStatus.setCollecteeId(collectee.getId());
								colecteeGstinStatus.setTaxPeriodByUser(month.toString() + taxPeriodYear);
							}
							// Return filling details
							for (GstinReturnFiliingDTO gstinReturnFiliingDTO : sacList) {
								String returnPeriod = gstinReturnFiliingDTO.getRet_prd();
								Integer returnPeriodMonth = Integer.valueOf(returnPeriod.substring(0, 2));
								Integer returnPeriodYear = Integer.valueOf(returnPeriod.substring(2, 6));

								Integer comparisonYear = finatialYear;
								if (returnPeriodMonth.equals(1) || returnPeriodMonth.equals(2)
										|| returnPeriodMonth.equals(3)) {
									comparisonYear = assessmentYear;
								}
								logger.info("retrunFillingStatusValue {} and returnPeriod {}", retrunFillingStatusValue,
										returnPeriod);
								if ((month.equals(returnPeriodMonth)) && (returnPeriodYear.equals(comparisonYear))) {

									logger.info("satisfied month and year check for GSTR values",
											gstinReturnFiliingDTO.getRet_prd());
									// Tax period Year
									if (gstinReturnFiliingDTO.getRtntype().equalsIgnoreCase("GSTR1")) {
										colecteeGstinStatus.setGstr1DateOfFiling(gstinReturnFiliingDTO.getDof());
										colecteeGstinStatus.setGstr1IsReturnValid(gstinReturnFiliingDTO.getValid());
										colecteeGstinStatus.setGstr1ReturnFiled(gstinReturnFiliingDTO.getStatus());
									} else if (gstinReturnFiliingDTO.getRtntype().equalsIgnoreCase("GSTR3B")) {
										colecteeGstinStatus.setGstr3DateOfFiling(gstinReturnFiliingDTO.getDof());
										colecteeGstinStatus.setGstr3IsReturnValid(gstinReturnFiliingDTO.getValid());
										colecteeGstinStatus.setGstr3ReturnFiled(gstinReturnFiliingDTO.getStatus());
									} else if (gstinReturnFiliingDTO.getRtntype().equalsIgnoreCase("GSTR9")) {
										colecteeGstinStatus.setGstr9DateOfFiling(gstinReturnFiliingDTO.getDof());
										colecteeGstinStatus.setGstr9IsReturnValid(gstinReturnFiliingDTO.getValid());
										colecteeGstinStatus.setGstr9ReturnFiled(gstinReturnFiliingDTO.getStatus());
									} else if (gstinReturnFiliingDTO.getRtntype().equalsIgnoreCase("GSTR6")) {
										colecteeGstinStatus.setGstr6DateOfFiling(gstinReturnFiliingDTO.getDof());
										colecteeGstinStatus.setGstr6IsReturnValid(gstinReturnFiliingDTO.getValid());
										colecteeGstinStatus.setGstr6ReturnFiled(gstinReturnFiliingDTO.getStatus());
									}
								}
							}
							colecteeGstinStatus.setActive(true);
							colecteeGstinStatus.setCreatedBy(userName);
							colecteeGstinStatus.setCreatedDate(new Date());
							colecteeGstinStatus.setYear(assessmentYear);
							colecteeGstinStatus.setMonth(month);
							colecteeGstinStatus.setDeductorMasterTan(deductorTan);
							colecteeGstinStatus.setDeductorPan(deductorPan);
							// get all collectee gstin status based on collectee id.
							List<CollecteeGstinStatus> gstinList = collecteeGstinStatusDAO
									.getAllGstinBasedOnCollecteeId(deductorTan, deductorPan, month, assessmentYear,
											collectee.getId());
							if (!gstinList.isEmpty()) {
								colecteeGstinStatus.setId(gstinList.get(0).getId());
								colecteeGstinStatus.setModifiedBy(userName);
								colecteeGstinStatus.setModifiedDate(new Date());
								// InActive collectee status
								collecteeGstinStatusDAO.collecteeGstinInActive(colecteeGstinStatus);
							}
							// Saving collectee Gstn status
							collecteeGstinStatusDAO.save(colecteeGstinStatus);
							logger.info("Data inserted for collectee GSTIN");
						} catch (Exception e) {
							logger.error("Exception occurred while requesting gstin validation :{}", e);
						}
					}
				}
			}

			getGstinCollecteeReportExcel(deductorTan, assessmentYear, deductorPan, tenantId, userName, batchUpload,
					month);

		} catch (Exception e) {
			logger.error("Exception occurred while requesting gstin authenticate api :{}", e);

			// Downloading exception in file
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			Cells cells = worksheet.getCells();
			worksheet.setName("Error Details");
			com.aspose.cells.Cell cell = cells.get("A1");
			cell.setValue("Error Message");
			// Adding a double value to the cell
			cell = cells.get("A2");
			cell.setValue(e.getMessage());
			// Write the output to the file
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.XLSX);
			File file = new File("Error_File", ".xlsx");
			FileUtils.writeByteArrayToFile(file, baout.toByteArray());

			saveTcsBatchUploadReportFile(deductorTan, tenantId, assessmentYear, file, 1L, batchUpload.getUploadType(),
					"Processed", batchUpload.getAssessmentMonth(), userName, batchUpload.getId(), null);
		}

	}

	/**
	 * 
	 * @param tan
	 * @param year
	 * @param deductorPan
	 * @param tenantId
	 * @param userName2
	 * @throws ParseException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InvalidKeyException
	 */
	@Async
	public void getAsyncGstinShareholderReportExcel(String tan, int year, String deductorPan, String tenantId,
			String userName)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		getGstinShareholderReportExcel(tan, year, deductorPan, tenantId, userName);
	}

	/**
	 * 
	 * @param tan
	 * @param year
	 * @param deductorPan
	 * @param tenantId
	 * @param userName
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	public void getGstinShareholderReportExcel(String tan, int year, String deductorPan, String tenantId,
			String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		String uploadType = UploadTypes.GSTIN_SHAREHOLDER_REPORT.name();
		String fileName = uploadType + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		BatchUpload batchUpload = saveBatchUploadReport(tan, tenantId, year, null, 0L, uploadType, "Processing", month,
				userName, null, fileName);
		// get all shareholder gstin
		List<ShareholderGstinStatus> gstinList = shareholderGstinStatusDAO.getAllShareholderGstin(year, deductorPan,
				tan);
		List<DeductorMaster> getDeductor = deductorMasterDAO.findBasedOnDeductorPan(deductorPan);
		int count = gstinList.size();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = null;
		String msg = StringUtils.EMPTY;
		resource = resourceLoader.getResource("classpath:templates/" + "gstin_shareholder_report.xlsx");
		msg = getErrorReportMsg(tenantId, getDeductor.get(0).getName(), "GSTIN DEDUCTEE REPORT");
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
			for (ShareholderGstinStatus gstin : gstinList) {
				XSSFRow row1 = sheet.createRow(rowindex++);
				row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));

				createSXSSFCell(style1, row1, 0, "");
				createSXSSFCell(style1, row1, 1, "");
				createSXSSFCell(style1, row1, 2, "");
				createSXSSFCell(style1, row1, 3, "");
				createSXSSFCell(style1, row1, 4, "");
				createSXSSFCell(style1, row1, 5, "");
				createSXSSFCell(style1, row1, 6, "");
				createSXSSFCell(style1, row1, 7, gstin.getLegalNameOfBusiness());
				createSXSSFCell(style1, row1, 8, gstin.getGstnStatus());
				if (StringUtils.isNotBlank(gstin.getDateOfCancellation())) {
					String dateOfCancellation = gstin.getDateOfCancellation().replace("/", "-");
					createSXSSFCell(style1, row1, 9, dateOfCancellation);
				} else {
					createSXSSFCell(style1, row1, 9, "");
				}
				if (StringUtils.isNotBlank(gstin.getLastUpdatedDate())) {
					String lastUpdateDate = gstin.getLastUpdatedDate().replace("/", "-");
					createSXSSFCell(style1, row1, 10, lastUpdateDate);
				} else {
					createSXSSFCell(style1, row1, 10, "");
				}
				createSXSSFCell(style1, row1, 11, gstin.getTradeName());
				if (StringUtils.isNotBlank(gstin.getGstr1DateOfFiling())) {
					String gstr1Date = gstin.getGstr1DateOfFiling().replace("/", "-");
					createSXSSFCell(style1, row1, 12, gstr1Date);
				} else {
					createSXSSFCell(style1, row1, 12, "");
				}
				createSXSSFCell(style1, row1, 13, gstin.getGstr1ReturnFiled());
				createSXSSFCell(style1, row1, 14, gstin.getGstr1IsReturnValid());
				if (StringUtils.isNotBlank(gstin.getGstr3DateOfFiling())) {
					String gstr3Date = gstin.getGstr3DateOfFiling().replace("/", "-");
					createSXSSFCell(style1, row1, 15, gstr3Date);
				} else {
					createSXSSFCell(style1, row1, 15, "");
				}
				createSXSSFCell(style1, row1, 16, gstin.getGstr3ReturnFiled());
				createSXSSFCell(style1, row1, 17, gstin.getGstr3IsReturnValid());
				if (StringUtils.isNotBlank(gstin.getGstr9DateOfFiling())) {
					String gstr9Date = gstin.getGstr9DateOfFiling().replace("/", "-");
					createSXSSFCell(style1, row1, 18, gstr9Date);
				} else {
					createSXSSFCell(style1, row1, 18, "");
				}
				createSXSSFCell(style1, row1, 19, gstin.getGstr9ReturnFiled());
				createSXSSFCell(style1, row1, 20, gstin.getGstr9IsReturnValid());
				if (StringUtils.isNotBlank(gstin.getGstr6DateOfFiling())) {
					String gstr6Date = gstin.getGstr6DateOfFiling().replace("/", "-");
					createSXSSFCell(style1, row1, 21, gstr6Date);
				} else {
					createSXSSFCell(style1, row1, 21, "");
				}
				createSXSSFCell(style1, row1, 22, gstin.getGstr6ReturnFiled());
				createSXSSFCell(style1, row1, 23, gstin.getGstr6IsReturnValid());

			}
			wb.write(out);
			saveBatchUploadReport(tan, tenantId, year, out, Long.valueOf(count), uploadType, "Processed", month,
					userName, batchUpload.getBatchUploadID(), null);
		} catch (Exception e) {
			logger.info("Exception occured while preparing error file {} ", e);
		}
	}

	@Async
	public void getAsyncGstinCollecteeStatusValidation(String deductorTan, String deductorPan, String userName,
			String year, int month, String tenantId) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		getGstinCollecteeStatusValidation(deductorTan, deductorPan, userName, year, month, tenantId);
	}

}
