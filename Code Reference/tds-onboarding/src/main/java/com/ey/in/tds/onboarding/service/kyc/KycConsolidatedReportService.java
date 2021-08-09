package com.ey.in.tds.onboarding.service.kyc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.CustomSectionRateDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeDeclarationRateType;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeDeclarationRateType;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderDeclarationRateType;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.jdbc.dao.CollecteeMasterDAO;
import com.ey.in.tds.jdbc.dao.DeducteeMasterResidentialDAO;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.jdbc.dao.ShareholderMasterResidentialDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

/**
 * 
 * @author vamsir
 *
 */
@Service
public class KycConsolidatedReportService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TCSBatchUploadDAO tcsbatchUploadDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	@Autowired
	private CollecteeMasterDAO collecteeMasterDAO;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private DeducteeMasterResidentialDAO deducteeMasterResidentialDAO;

	@Autowired
	private ShareholderMasterResidentialDAO shareholderMasterResidentialDAO;

	public void triggerConsolidatedReport(String pan, String tenantId, String tan, String userName,
			Integer assesssmentYear) throws Exception {
		asyncTriggerConsolidatedReport(pan, tenantId, tan, userName, assesssmentYear);

	}

	/**
	 * @param pan
	 * @param tenantId
	 * @param tan
	 * @param userName
	 * @param assesssmentYear
	 * @param type
	 * @throws Exception
	 */

	@Async
	private void asyncTriggerConsolidatedReport(String deductorPan, String tenantId, String deductorTan,
			String userName, Integer year) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		consolidatedReport(deductorPan, tenantId, deductorTan, userName, year);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tenantId
	 * @param deductorTan
	 * @param userName
	 * @param year
	 * @param batchUpload
	 * @return
	 * @throws Exception
	 */
	private TCSBatchUpload consolidatedReport(String deductorPan, String tenantId, String deductorTan, String userName,
			Integer year) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		List<CollecteeDeclarationRateType> listOfCollectee = collecteeMasterDAO
				.getAllCollecteeByDeclaration(deductorPan, year, month);

		String uploadType = UploadTypes.CUSTOMER_CONSOLIDATED_REPORT.name();

		String fileName = uploadType;
		TCSBatchUpload batchUpload = saveBatchUploadReport(deductorTan, tenantId, year, null, 0L, uploadType,
				"Processing", month, userName, null, fileName);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = resourceLoader.getResource("classpath:templates/" + "collectee_consolidated_report.xlsx");
		InputStream input = resource.getInputStream();
		try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
			XSSFSheet sheet = wb.getSheetAt(0);
			String msg = getReportHeader(tenantId, deductorPan);

			org.apache.poi.ss.usermodel.Font fonts = wb.createFont();
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
			style.setLocked(true);

			Font fonts2 = wb.createFont();
			fonts2.setBold(false);

			XSSFCellStyle style1 = wb.createCellStyle();
			style1.setFont(fonts);
			style1.setWrapText(true);
			style1.setFont(fonts2);
			style1.setLocked(true);

			XSSFCellStyle unlockedCellStyle = wb.createCellStyle();
			unlockedCellStyle.setFont(fonts);
			unlockedCellStyle.setWrapText(true);
			unlockedCellStyle.setFont(fonts2);
			unlockedCellStyle.setLocked(false);

			XSSFRow row0 = sheet.createRow(0);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);

			int rowindex = 7;
			ObjectMapper objectMapper = new ObjectMapper();
			// got total nature of income details
			// List<CustomSectionRateDTO> listRatesSections =
			// mastersClient.findSectionRates().getBody().getData();
			List<CustomSectionRateDTO> listRatesSections = new ArrayList<>();
			Map<String, BigDecimal> normalratesMap = new HashMap<>();
			Map<String, BigDecimal> noPanRatesMap = new HashMap<>();
			Map<String, BigDecimal> noIrtRatesMap = new HashMap<>();
			Map<String, BigDecimal> noPanNoItrRatesMap = new HashMap<>();
			Long totalCount = 0L;
			for (CustomSectionRateDTO customSectionRateDTO : listRatesSections) {

				String section = customSectionRateDTO.getSection();
				String nature = customSectionRateDTO.getNature();

				// Normal Rate
				if (customSectionRateDTO.getRate() != null) {
					normalratesMap.put(section + "-" + nature, BigDecimal.valueOf(customSectionRateDTO.getRate()));
				}
				// NO Pan Rate
				if (customSectionRateDTO.getNoPanRate() != null) {
					noPanRatesMap.put(section + "-" + nature, BigDecimal.valueOf(customSectionRateDTO.getNoPanRate()));
				}
				// No ITR Rate
				if (customSectionRateDTO.getNoItrRate() != null) {
					noIrtRatesMap.put(section + "-" + nature, BigDecimal.valueOf(customSectionRateDTO.getNoItrRate()));
				}
				// No Pan No ITR Rate
				if (customSectionRateDTO.getNoPanNoItrRate() != null) {
					noPanNoItrRatesMap.put(section + "-" + nature,
							BigDecimal.valueOf(customSectionRateDTO.getNoPanNoItrRate()));
				}

			}

			// collectee details with declaration details
			if (!listOfCollectee.isEmpty()) {
				for (CollecteeDeclarationRateType collectee : listOfCollectee) {
					String additionalsections = collectee.getAdditionalSections();
					String section = collectee.getTcsSection();
					String noi = collectee.getNatureOfIncome();
					// BigDecimal rate = normalratesMap.get(section + "-" + noi);
					String rateType = collectee.getRateType();
					BigDecimal rate = new BigDecimal(0.0);
					BigDecimal uiNormalRate = new BigDecimal(0.0);
					if (collectee.getTcsRate() != null) {
						rate = collectee.getTcsRate();
						uiNormalRate = collectee.getTcsRate();
					}

					// for direct section
					BigDecimal noPanDirectRate = new BigDecimal(0.0);
					BigDecimal noITRDirectRate = new BigDecimal(0.0);

					Map<String, BigDecimal> additionalSectionss = null;
					if (StringUtils.isNotBlank(additionalsections)) {
						additionalSectionss = objectMapper.readValue(collectee.getAdditionalSections(),
								new TypeReference<Map<String, BigDecimal>>() {
								});
					}
					// setting normal section as well in map
					if (collectee.getIsEligibleForMultipleSections() != null && additionalSectionss != null) {
						for (Map.Entry<String, BigDecimal> entry : additionalSectionss.entrySet()) {
							String addtionalSection = StringUtils.substringBefore(entry.getKey(), "-");
							String addtionalNoi = StringUtils.substringAfter(entry.getKey(), "-");
							String mapKey = addtionalSection + "-" + addtionalNoi;

							BigDecimal noPanRate = new BigDecimal(0.0);
							BigDecimal noITRRate = new BigDecimal(0.0);

							BigDecimal addtionalRate = entry.getValue();
							BigDecimal uiRate = entry.getValue();
							if (rateType.equalsIgnoreCase("NO PAN")) {
								if (addtionalSection.equalsIgnoreCase("206C(1H)")) {
									noPanRate = new BigDecimal(1.0);
								} else {
									noPanRate = new BigDecimal(5.0);
								}
								// noPanRatesMap.get(mapKey);
								addtionalRate = addtionalRate.max(noPanRate);
							} else if (rateType.equalsIgnoreCase("NO ITR")) {

								BigDecimal normalITRRate = new BigDecimal(5.0);
								noITRRate = addtionalRate.multiply(new BigDecimal(2.0));
								// noPanRatesMap.get(mapKey);
								addtionalRate = normalITRRate.max(noITRRate);
								// noIrtRatesMap.get(mapKey);
							} else if (rateType.equalsIgnoreCase("NO PAN AND ITR")) {
								if (addtionalSection.equalsIgnoreCase("206C(1H)")) {
									noPanRate = new BigDecimal(5.0);
								} else {
									noPanRate = new BigDecimal(10.0);
								}
								noPanRate = noPanRate.max(addtionalRate);

								BigDecimal normalITRRate = new BigDecimal(5.0);
								noITRRate = addtionalRate.multiply(new BigDecimal(2.0));
								BigDecimal itrRate = noITRRate.max(normalITRRate);

								addtionalRate = noPanRate.max(itrRate);// noPanNoItrRatesMap.get(mapKey);
							}
							++totalCount;
							rowindex = setConsolidatedReport(sheet, style1, rowindex, collectee, addtionalSection,
									addtionalRate, rateType, uiRate);
						}
					}

					// for direct section
					if (rateType.equalsIgnoreCase("NO PAN")) {
						if (section.equalsIgnoreCase("206C(1H)")) {
							noPanDirectRate = new BigDecimal(1.0);
						} else {
							noPanDirectRate = new BigDecimal(5.0);
						}
						// noPanRatesMap.get(mapKey);
						rate = rate.max(noPanDirectRate);
					} else if (rateType.equalsIgnoreCase("NO ITR")) {

						BigDecimal normalITRRate = new BigDecimal(5.0);
						noITRDirectRate = rate.multiply(new BigDecimal(2.0));
						// noPanRatesMap.get(mapKey);
						rate = normalITRRate.max(noITRDirectRate);
						// noIrtRatesMap.get(mapKey);
					} else if (rateType.equalsIgnoreCase("NO PAN AND ITR")) {
						if (section.equalsIgnoreCase("206C(1H)")) {
							noPanDirectRate = new BigDecimal(5.0);
						} else {
							noPanDirectRate = new BigDecimal(10.0);
						}
						noPanDirectRate = noPanDirectRate.max(rate);

						BigDecimal normalITRRate = new BigDecimal(5.0);
						noITRDirectRate = rate.multiply(new BigDecimal(2.0));
						BigDecimal itrRate = noITRDirectRate.max(normalITRRate);

						rate = noPanDirectRate.max(itrRate);// noPanNoItrRatesMap.get(mapKey);
					}
					++totalCount;
					rowindex = setConsolidatedReport(sheet, style1, rowindex, collectee, section, rate, rateType,
							uiNormalRate);
				}
			}
			wb.write(out);
			batchUpload = saveBatchUploadReport(deductorTan, tenantId, year, out, totalCount, uploadType, "Processed",
					month, userName, batchUpload.getId(), null);
		} catch (Exception e) {
			logger.info(
					"Exception occurred while preparing declaration collectee consolidated report" + e.getStackTrace());
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param sheet
	 * @param style1
	 * @param rowindex
	 * @param collectee
	 * @param section
	 * @param noi
	 * @param rate
	 * @param rateType
	 * @return
	 */
	private int setConsolidatedReport(XSSFSheet sheet, XSSFCellStyle style1, int rowindex,
			CollecteeDeclarationRateType collectee, String section, BigDecimal rate, String rateType,
			BigDecimal uiRate) {
		XSSFRow row1 = sheet.createRow(rowindex++);
		row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
		createSXSSFCell(style1, row1, 0, collectee.getNameOfTheCollectee());
		createSXSSFCell(style1, row1, 1, collectee.getCollecteePan());
		createSXSSFCell(style1, row1, 2, collectee.getCollecteeCode());
		createSXSSFCell(style1, row1, 3, "");
		createSXSSFCell(style1, row1, 4, section);
		createSXSSFCell(style1, row1, 5, uiRate != null ? uiRate.toString() : "");
		if ("TCS".equalsIgnoreCase(collectee.getTdsOrTcs())) {
			createSXSSFCell(style1, row1, 6, "NO");
		} else if ("TDS".equalsIgnoreCase(collectee.getTdsOrTcs())) {
			createSXSSFCell(style1, row1, 6, "YES");
		}
		createSXSSFCell(style1, row1, 7, collectee.getTdsOrTcs());
		createSXSSFCell(style1, row1, 8, collectee.getCollecteePan());
		createSXSSFCell(style1, row1, 9, collectee.getNameAsPerTraces());
		createSXSSFCell(style1, row1, 10,
				collectee.getPanAllotmentDate() != null ? collectee.getPanAllotmentDate().toString() : "");
		createSXSSFCell(style1, row1, 11, collectee.getPanAadhaarLinkStatus());
		createSXSSFCell(style1, row1, 12, collectee.getSpecifiedPerson());
		createSXSSFCell(style1, row1, 13,
				collectee.getDeclarationApplicableFrom() != null ? collectee.getDeclarationApplicableFrom().toString()
						: "");
		createSXSSFCell(style1, row1, 14, rateType);
		createSXSSFCell(style1, row1, 15, rate != null ? rate.toString() : "");
		createSXSSFCell(style1, row1, 16, "");

		// collectee gstin status
		createSXSSFCell(style1, row1, 17, collectee.getGstinNumber());
		createSXSSFCell(style1, row1, 18, collectee.getTaxPeriodByUser());
		createSXSSFCell(style1, row1, 19, collectee.getLegalNameOfBusiness());
		createSXSSFCell(style1, row1, 20, collectee.getGstnStatus());
		if (StringUtils.isNotBlank(collectee.getDateOfCancellation())) {
			String dateOfCancellation = collectee.getDateOfCancellation().replace("-", "/");
			createSXSSFCell(style1, row1, 21, dateOfCancellation);
		} else {
			createSXSSFCell(style1, row1, 21, "");
		}
		if (StringUtils.isNotBlank(collectee.getLastUpdatedDate())) {
			String lastUpdateDate = collectee.getLastUpdatedDate().replace("-", "/");
			createSXSSFCell(style1, row1, 22, lastUpdateDate);
		} else {
			createSXSSFCell(style1, row1, 22, "");
		}
		createSXSSFCell(style1, row1, 23, collectee.getTradeName());
		if (StringUtils.isNotBlank(collectee.getGstr1DateOfFiling())) {
			String gstr1Date = collectee.getGstr1DateOfFiling().replace("/", "-");
			createSXSSFCell(style1, row1, 24, gstr1Date);
		} else {
			createSXSSFCell(style1, row1, 24, "");
		}
		createSXSSFCell(style1, row1, 25, collectee.getGstr1ReturnFiled());
		createSXSSFCell(style1, row1, 26, collectee.getGstr1IsReturnValid());
		if (StringUtils.isNotBlank(collectee.getGstr3DateOfFiling())) {
			String gstr3Date = collectee.getGstr3DateOfFiling().replace("/", "-");
			createSXSSFCell(style1, row1, 27, gstr3Date);
		} else {
			createSXSSFCell(style1, row1, 27, "");
		}
		createSXSSFCell(style1, row1, 28, collectee.getGstr3ReturnFiled());
		createSXSSFCell(style1, row1, 29, collectee.getGstr3IsReturnValid());
		if (StringUtils.isNotBlank(collectee.getGstr9DateOfFiling())) {
			String gstr9Date = collectee.getGstr9DateOfFiling().replace("/", "-");
			createSXSSFCell(style1, row1, 30, gstr9Date);
		} else {
			createSXSSFCell(style1, row1, 31, "");
		}
		createSXSSFCell(style1, row1, 32, collectee.getGstr9ReturnFiled());
		createSXSSFCell(style1, row1, 33, collectee.getGstr9IsReturnValid());
		if (StringUtils.isNotBlank(collectee.getGstr6DateOfFiling())) {
			String gstr6Date = collectee.getGstr6DateOfFiling().replace("/", "-");
			createSXSSFCell(style1, row1, 34, gstr6Date);
		} else {
			createSXSSFCell(style1, row1, 35, "");
		}
		createSXSSFCell(style1, row1, 36, collectee.getGstr6ReturnFiled());
		createSXSSFCell(style1, row1, 37, collectee.getGstr6IsReturnValid());

		return rowindex;
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
	public TCSBatchUpload saveBatchUploadReport(String deductorTan, String tenantId, int assessmentYear,
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
			fileName = fileName + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
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
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 */
	private String getReportHeader(String tenantId, String deductorPan) {
		MultiTenantContext.setTenantId(tenantId);
		List<DeductorMaster> getDeductor = deductorMasterDAO.findBasedOnDeductorPan(deductorPan);
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return "Final Report (Dated: " + date + ")\n Client Name: " + getDeductor.get(0).getName() + "\n";
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

	@Async
	public void tdsAsyncTriggerConsolidatedReport(String deductorPan, String tenantId, String deductorTan,
			String userName, Integer year) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		tdsConsolidatedReport(deductorPan, tenantId, deductorTan, userName, year);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tenantId
	 * @param deductorTan
	 * @param userName
	 * @param year
	 * @return
	 * @throws Exception
	 */
	private BatchUpload tdsConsolidatedReport(String deductorPan, String tenantId, String deductorTan, String userName,
			Integer year) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		List<DeducteeDeclarationRateType> deducteeList = deducteeMasterResidentialDAO
				.getAllDeducteeByDeclaration(deductorPan, year, month);

		String uploadType = UploadTypes.VENDOR_CONSOLIDATED_REPORT.name();

		String fileName = uploadType;
		BatchUpload batchUpload = saveBatchUploadTdsReport(deductorTan, tenantId, year, null, 0L, uploadType,
				"Processing", month, userName, null, fileName);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = resourceLoader.getResource("classpath:templates/" + "deductee_consolidated_report.xlsx");
		InputStream input = resource.getInputStream();
		try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
			XSSFSheet sheet = wb.getSheetAt(0);
			String msg = getReportHeader(tenantId, deductorPan);

			org.apache.poi.ss.usermodel.Font fonts = wb.createFont();
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
			style.setLocked(true);

			Font fonts2 = wb.createFont();
			fonts2.setBold(false);

			XSSFCellStyle style1 = wb.createCellStyle();
			style1.setFont(fonts);
			style1.setWrapText(true);
			style1.setFont(fonts2);
			style1.setLocked(true);

			XSSFCellStyle unlockedCellStyle = wb.createCellStyle();
			unlockedCellStyle.setFont(fonts);
			unlockedCellStyle.setWrapText(true);
			unlockedCellStyle.setFont(fonts2);
			unlockedCellStyle.setLocked(false);

			XSSFRow row0 = sheet.createRow(0);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);

			int rowindex = 7;
			ObjectMapper objectMapper = new ObjectMapper();
			// got total nature of income details
			// List<CustomSectionRateDTO> listRatesSections =
			// mastersClient.findTdsSectionRates().getBody().getData();
			List<CustomSectionRateDTO> listRatesSections = new ArrayList<>();
			Map<String, BigDecimal> normalratesMap = new HashMap<>();
			Map<String, BigDecimal> noPanRatesMap = new HashMap<>();
			Map<String, BigDecimal> noIrtRatesMap = new HashMap<>();
			Map<String, BigDecimal> noPanNoItrRatesMap = new HashMap<>();
			Map<String, String> swappedMap = new HashMap<>();
			Long totalCount = 0L;
			for (CustomSectionRateDTO customSectionRateDTO : listRatesSections) {

				String section = customSectionRateDTO.getSection();
				String nature = customSectionRateDTO.getNature();
				String deduteeStatus = customSectionRateDTO.getDeducteeStatus();

				// Normal Rate
				if (customSectionRateDTO.getRate() != null) {
					normalratesMap.put(section + "-" + deduteeStatus + "-" + nature,
							BigDecimal.valueOf(customSectionRateDTO.getRate()));
				}
				// NO Pan Rate
				if (customSectionRateDTO.getNoPanRate() != null) {
					noPanRatesMap.put(section + "-" + deduteeStatus + "-" + nature,
							BigDecimal.valueOf(customSectionRateDTO.getNoPanRate()));
				}
				// No ITR Rate
				if (customSectionRateDTO.getNoItrRate() != null) {
					noIrtRatesMap.put(section + "-" + deduteeStatus + "-" + nature,
							BigDecimal.valueOf(customSectionRateDTO.getNoItrRate()));
				}
				// No Pan No ITR Rate
				if (customSectionRateDTO.getNoPanNoItrRate() != null) {
					noPanNoItrRatesMap.put(section + "-" + deduteeStatus + "-" + nature,
							BigDecimal.valueOf(customSectionRateDTO.getNoPanNoItrRate()));
				}

			}

			// deductee details with declaration details
			if (!deducteeList.isEmpty()) {
				for (DeducteeDeclarationRateType deductee : deducteeList) {
					String additionalsections = deductee.getAdditionalSections();
					String additionalsectionCode = deductee.getAdditionalSectionCode();
					String section = deductee.getSection();
					String nop = deductee.getNatureOfPayment();
					String sectionCode = deductee.getSectionCode();
					// BigDecimal rate = normalratesMap.get(section + "-" + nop);
					BigDecimal rate = new BigDecimal(0.0);
					BigDecimal uiNormalRate = new BigDecimal(0.0);
					if (deductee.getRate() != null) {
						rate = deductee.getRate();
						uiNormalRate = deductee.getRate();
					}
					String rateType = deductee.getRateType();

					Map<String, BigDecimal> additionalSectionss = null;
					if (StringUtils.isNotBlank(additionalsections)) {
						additionalSectionss = objectMapper.readValue(deductee.getAdditionalSections(),
								new TypeReference<Map<String, BigDecimal>>() {
								});
					}
					Map<String, String> additionalSectionsCodes = null;
					if (StringUtils.isNotBlank(additionalsectionCode)) {
						additionalSectionsCodes = objectMapper.readValue(deductee.getAdditionalSectionCode(),
								new TypeReference<Map<String, String>>() {
								});
						for (Map.Entry<String, String> entry : additionalSectionsCodes.entrySet()) {
							swappedMap.put(entry.getValue(), entry.getKey());
						}
					}
					// setting normal section as well in map
					if (deductee.getIsDeducteeHasAdditionalSections() != null && additionalSectionss != null) {
						for (Map.Entry<String, BigDecimal> entry : additionalSectionss.entrySet()) {
							String addtionalSection = StringUtils.substringBefore(entry.getKey(), "-");
							String addtionalNoi = StringUtils.substringAfter(entry.getKey(), "-");
							String mapKey = addtionalSection + "-" + deductee.getDeducteeStatus() + "-" + addtionalNoi;

							BigDecimal noPanRate = new BigDecimal(0.0);
							BigDecimal noITRRate = new BigDecimal(0.0);

							BigDecimal addtionalRate = entry.getValue();
							BigDecimal uiRate = entry.getValue();
							if (rateType.equalsIgnoreCase("NO PAN")) {
								if (addtionalSection.equalsIgnoreCase("194Q")
										| addtionalSection.equalsIgnoreCase("194O")) {
									noPanRate = new BigDecimal(5.0);
								} else {
									noPanRate = new BigDecimal(20.0);
								}
								// noPanRatesMap.get(mapKey);
								addtionalRate = addtionalRate.max(noPanRate);
							} else if (rateType.equalsIgnoreCase("NO ITR")) {

								BigDecimal normalITRRate = new BigDecimal(5.0);
								noITRRate = addtionalRate.multiply(new BigDecimal(2.0));
								// noPanRatesMap.get(mapKey);
								addtionalRate = normalITRRate.max(noITRRate);
								// noIrtRatesMap.get(mapKey);
							} else if (rateType.equalsIgnoreCase("NO PAN AND ITR")) {
								if (addtionalSection.equalsIgnoreCase("194Q")
										| addtionalSection.equalsIgnoreCase("194O")) {
									noPanRate = new BigDecimal(5.0);
								} else {
									noPanRate = new BigDecimal(20.0);
								}
								noPanRate = noPanRate.max(addtionalRate);

								BigDecimal normalITRRate = new BigDecimal(5.0);
								noITRRate = addtionalRate.multiply(new BigDecimal(2.0));
								BigDecimal itrRate = noITRRate.max(normalITRRate);

								addtionalRate = noPanRate.max(itrRate);// noPanNoItrRatesMap.get(mapKey);
							}
							++totalCount;
							rowindex = setTdsConsolidatedReport(sheet, style1, rowindex, deductee, addtionalSection,
									addtionalRate, rateType, swappedMap.get(addtionalSection), uiRate);
						}
					}

					String key = section + "-" + deductee.getDeducteeStatus() + "-" + nop;
					BigDecimal noPanRate = new BigDecimal(0.0);
					BigDecimal noITRRate = new BigDecimal(0.0);
					if (rateType.equalsIgnoreCase("NO PAN")) {
						if (section.equalsIgnoreCase("194Q") | section.equalsIgnoreCase("194O")) {
							noPanRate = new BigDecimal(5.0);
						} else {
							noPanRate = new BigDecimal(20.0);
						}
						// noPanRatesMap.get(mapKey);
						rate = rate.max(noPanRate);
					} else if (rateType.equalsIgnoreCase("NO ITR")) {

						BigDecimal normalITRRate = new BigDecimal(5.0);
						noITRRate = rate.multiply(new BigDecimal(2.0));
						// noPanRatesMap.get(mapKey);
						rate = normalITRRate.max(noITRRate);
						// noIrtRatesMap.get(mapKey);
					} else if (rateType.equalsIgnoreCase("NO PAN AND ITR")) {
						if (section.equalsIgnoreCase("194Q") | section.equalsIgnoreCase("194O")) {
							noPanRate = new BigDecimal(5.0);
						} else {
							noPanRate = new BigDecimal(20.0);
						}
						noPanRate = noPanRate.max(rate);

						BigDecimal normalITRRate = new BigDecimal(5.0);
						noITRRate = rate.multiply(new BigDecimal(2.0));
						BigDecimal itrRate = noITRRate.max(normalITRRate);

						rate = noPanRate.max(itrRate);// noPanNoItrRatesMap.get(mapKey);
					}
					++totalCount;
					rowindex = setTdsConsolidatedReport(sheet, style1, rowindex, deductee, section, rate, rateType,
							sectionCode, uiNormalRate);
				}
			}
			wb.write(out);
			saveBatchUploadTdsReport(deductorTan, tenantId, year, out, totalCount, uploadType, "Processed", month,
					userName, batchUpload.getBatchUploadID(), null);
		} catch (Exception e) {
			logger.info("Exception occurred while preparing declaration collectee consolidated report {} ",
					e.getMessage());
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param sheet
	 * @param style1
	 * @param rowindex
	 * @param deductee
	 * @param section
	 * @param nop
	 * @param rate
	 * @param rateType
	 * @return
	 */
	private int setTdsConsolidatedReport(XSSFSheet sheet, XSSFCellStyle style1, int rowindex,
			DeducteeDeclarationRateType deductee, String section, BigDecimal rate, String rateType, String sectionCode,
			BigDecimal uiRate) {
		// setting data to report
		XSSFRow row1 = sheet.createRow(rowindex++);
		row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
		createSXSSFCell(style1, row1, 0, deductee.getDeducteeName());
		createSXSSFCell(style1, row1, 1, deductee.getDeducteePAN());
		createSXSSFCell(style1, row1, 2, deductee.getDeducteeCode());
		createSXSSFCell(style1, row1, 3, sectionCode);
		createSXSSFCell(style1, row1, 4, section);
		createSXSSFCell(style1, row1, 5, uiRate != null ? uiRate.toString() : "");
		createSXSSFCell(style1, row1, 6, deductee.getDeducteePAN());
		createSXSSFCell(style1, row1, 7, deductee.getNameAsPerTraces());
		createSXSSFCell(style1, row1, 8,
				deductee.getPanAllotmentDate() != null ? deductee.getPanAllotmentDate().toString() : "");
		createSXSSFCell(style1, row1, 9, deductee.getPanAadhaarLinkStatus());
		createSXSSFCell(style1, row1, 10, deductee.getSpecifiedPerson());
		createSXSSFCell(style1, row1, 11,
				deductee.getDeclarationApplicableFrom() != null ? deductee.getDeclarationApplicableFrom().toString()
						: "");
		createSXSSFCell(style1, row1, 12, rateType);
		createSXSSFCell(style1, row1, 13, rate != null ? rate.toString() : "");
		createSXSSFCell(style1, row1, 14, "");
		// deductee gstin status
		createSXSSFCell(style1, row1, 15, deductee.getDeducteeGSTIN());
		createSXSSFCell(style1, row1, 16, deductee.getTaxPeriodByUser());
		createSXSSFCell(style1, row1, 17, deductee.getLegalNameOfBusiness());
		createSXSSFCell(style1, row1, 18, deductee.getGstnStatus());
		if (StringUtils.isNotBlank(deductee.getDateOfCancellation())) {
			String dateOfCancellation = deductee.getDateOfCancellation().replace("-", "/");
			createSXSSFCell(style1, row1, 19, dateOfCancellation);
		} else {
			createSXSSFCell(style1, row1, 19, "");
		}
		if (StringUtils.isNotBlank(deductee.getLastUpdatedDate())) {
			String lastUpdateDate = deductee.getLastUpdatedDate().replace("-", "/");
			createSXSSFCell(style1, row1, 20, lastUpdateDate);
		} else {
			createSXSSFCell(style1, row1, 20, "");
		}
		createSXSSFCell(style1, row1, 21, deductee.getTradeName());
		if (StringUtils.isNotBlank(deductee.getGstr1DateOfFiling())) {
			String gstr1Date = deductee.getGstr1DateOfFiling().replace("/", "-");
			createSXSSFCell(style1, row1, 22, gstr1Date);
		} else {
			createSXSSFCell(style1, row1, 22, "");
		}
		createSXSSFCell(style1, row1, 23, deductee.getGstr1ReturnFiled());
		createSXSSFCell(style1, row1, 24, deductee.getGstr1IsReturnValid());
		if (StringUtils.isNotBlank(deductee.getGstr3DateOfFiling())) {
			String gstr3Date = deductee.getGstr3DateOfFiling().replace("/", "-");
			createSXSSFCell(style1, row1, 25, gstr3Date);
		} else {
			createSXSSFCell(style1, row1, 25, "");
		}
		createSXSSFCell(style1, row1, 26, deductee.getGstr3ReturnFiled());
		createSXSSFCell(style1, row1, 27, deductee.getGstr3IsReturnValid());
		if (StringUtils.isNotBlank(deductee.getGstr9DateOfFiling())) {
			String gstr9Date = deductee.getGstr9DateOfFiling().replace("/", "-");
			createSXSSFCell(style1, row1, 28, gstr9Date);
		} else {
			createSXSSFCell(style1, row1, 28, "");
		}
		createSXSSFCell(style1, row1, 29, deductee.getGstr9ReturnFiled());
		createSXSSFCell(style1, row1, 30, deductee.getGstr9IsReturnValid());
		if (StringUtils.isNotBlank(deductee.getGstr6DateOfFiling())) {
			String gstr6Date = deductee.getGstr6DateOfFiling().replace("/", "-");
			createSXSSFCell(style1, row1, 31, gstr6Date);
		} else {
			createSXSSFCell(style1, row1, 31, "");
		}
		createSXSSFCell(style1, row1, 32, deductee.getGstr6ReturnFiled());
		createSXSSFCell(style1, row1, 33, deductee.getGstr6IsReturnValid());

		return rowindex;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tenantId
	 * @param deductorTan
	 * @param userName
	 * @param year
	 * @throws Exception
	 */
	@Async
	public void shareholderAsyncTriggerConsolidatedReport(String deductorPan, String tenantId, String deductorTan,
			String userName, Integer year) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		shareholderConsolidatedReport(deductorPan, tenantId, deductorTan, userName, year);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tenantId
	 * @param deductorTan
	 * @param userName
	 * @param year
	 * @return
	 * @throws Exception
	 */
	private BatchUpload shareholderConsolidatedReport(String deductorPan, String tenantId, String deductorTan,
			String userName, Integer year) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		List<ShareholderDeclarationRateType> shareholderList = shareholderMasterResidentialDAO
				.getAllShareholderByDeclaration(deductorPan);

		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		String uploadType = UploadTypes.SHAREHOLDER_CONSOLIDATED_REPORT.name();

		String fileName = uploadType + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		BatchUpload batchUpload = saveBatchUploadTdsReport(deductorTan, tenantId, year, null, 0L, uploadType,
				"Processing", month, userName, null, fileName);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Resource resource = resourceLoader.getResource("classpath:templates/" + "shareholder_consolidated_report.xlsx");
		InputStream input = resource.getInputStream();
		try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
			XSSFSheet sheet = wb.getSheetAt(0);
			String msg = getReportHeader(tenantId, deductorPan);

			org.apache.poi.ss.usermodel.Font fonts = wb.createFont();
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
			style.setLocked(true);

			Font fonts2 = wb.createFont();
			fonts2.setBold(false);

			XSSFCellStyle style1 = wb.createCellStyle();
			style1.setFont(fonts);
			style1.setWrapText(true);
			style1.setFont(fonts2);
			style1.setLocked(true);

			XSSFCellStyle unlockedCellStyle = wb.createCellStyle();
			unlockedCellStyle.setFont(fonts);
			unlockedCellStyle.setWrapText(true);
			unlockedCellStyle.setFont(fonts2);
			unlockedCellStyle.setLocked(false);

			XSSFRow row0 = sheet.createRow(0);
			row0.createCell(0).setCellValue(msg);
			row0.getCell(0).setCellStyle(style);

			int rowindex = 7;

			// deductee details with declaration details
			if (!shareholderList.isEmpty()) {
				for (ShareholderDeclarationRateType shareholder : shareholderList) {
					String rateType = shareholder.getRateType();

					XSSFRow row1 = sheet.createRow(rowindex++);
					row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
					createSXSSFCell(style1, row1, 0, shareholder.getShareholderName());
					createSXSSFCell(style1, row1, 1, shareholder.getShareholderPan());
					createSXSSFCell(style1, row1, 2, shareholder.getFolioNo());
					createSXSSFCell(style1, row1, 3, shareholder.getShareholderPan());
					createSXSSFCell(style1, row1, 4, shareholder.getNameAsPerTraces());
					createSXSSFCell(style1, row1, 5,
							shareholder.getPanAllotmentDate() != null ? shareholder.getPanAllotmentDate().toString()
									: "");
					createSXSSFCell(style1, row1, 6, shareholder.getPanAadhaarLinkStatus());
					createSXSSFCell(style1, row1, 7, shareholder.getSpecifiedPerson());
					createSXSSFCell(style1, row1, 8,
							shareholder.getDeclarationApplicableFrom() != null
									? shareholder.getDeclarationApplicableFrom().toString()
									: "");
					createSXSSFCell(style1, row1, 9, rateType);
					createSXSSFCell(style1, row1, 10, "");
				}
			}
			wb.write(out);
			saveBatchUploadTdsReport(deductorTan, tenantId, year, out,
					shareholderList != null ? ((long) shareholderList.size()) : 0L, uploadType, "Processed", month,
					userName, batchUpload.getBatchUploadID(), null);
		} catch (Exception e) {
			logger.info("Exception occurred while preparing declaration shareholder consolidated report {} ",
					e.getMessage());
		}
		return batchUpload;
	}

	public BatchUpload saveBatchUploadTdsReport(String deductorTan, String tenantId, int assessmentYear,
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

}
