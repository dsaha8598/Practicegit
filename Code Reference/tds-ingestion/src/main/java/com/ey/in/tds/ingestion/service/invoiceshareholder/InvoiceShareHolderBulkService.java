package com.ey.in.tds.ingestion.service.invoiceshareholder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tcs.common.domain.dividend.ActSummary;
import com.ey.in.tcs.common.domain.dividend.InvoiceShareholderNonResident;
import com.ey.in.tcs.common.domain.dividend.LDCAOSummary;
import com.ey.in.tcs.common.domain.dividend.MiscRateSummary;
import com.ey.in.tcs.common.domain.dividend.NonResidentWithholdingDetails;
import com.ey.in.tcs.common.domain.dividend.TreatySummary;
import com.ey.in.tcs.common.domain.dividend.WithholdingRateType;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.dividend.InvoiceShareholderResident;
import com.ey.in.tds.common.domain.dividend.LDCSummary;
import com.ey.in.tds.common.domain.dividend.ResidentWithholdingDetails;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceShareholderNonResidentDAO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceShareholderResidentDAO;
import com.microsoft.azure.storage.StorageException;

/**
 * this class is for generating dividend summery report and liability report
 * asynchronusly
 * 
 * @author dipak
 *
 */
@Service
public class InvoiceShareHolderBulkService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private InvoiceShareholderNonResidentDAO invoiceShareholderNonResidentDAO;

	@Autowired
	private InvoiceShareholderResidentDAO invoiceShareholderResidentDAO;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	/**
	 * generating report asynchronusly and updating batch upload record
	 * 
	 * @param batchUpload
	 * @param residentType
	 * @param deductorPan
	 * @param year
	 * @param tenantId
	 * @throws Exception
	 */
	@Async
	public void AsyncDownloadDividendSummeryReport(BatchUpload batchUpload, String residentType, String deductorPan,
			Integer year, String tenantId) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		if (residentType.equalsIgnoreCase("resident")) {
			List<InvoiceShareholderResident> invoiceShareholderResidentList = invoiceShareholderResidentDAO
					.findAllByPanAndYear(deductorPan, year);

			prcocessResidentSummary(invoiceShareholderResidentList, deductorPan, batchUpload, tenantId);
		} else {
			List<InvoiceShareholderNonResident> invoiceShareholderNonResidentList = invoiceShareholderNonResidentDAO
					.findAllByPanAndYear(deductorPan, year);

			prcocessNonResidentSummary(invoiceShareholderNonResidentList, deductorPan, batchUpload, tenantId);
		}

	}

	private void updateBatchUpload(BatchUpload batchUpload, String tenantId, MultipartFile multipartFile,
			Long recordCount)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, ParseException {
		String filePath = blob.uploadExcelToBlob(multipartFile, tenantId);
		batchUpload.setSuccessFileUrl(filePath);
		batchUpload.setFilePath(filePath);
		batchUpload.setOtherFileUrl(filePath);
		batchUpload.setFileName(multipartFile.getOriginalFilename());
		batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
		batchUpload.setStatus("Processed");
		batchUpload.setRowsCount(recordCount);
		batchUploadDAO.update(batchUpload);
	}

	private void prcocessResidentSummary(List<InvoiceShareholderResident> invoiceShareholderResidentList,
			String deductorPan, BatchUpload batchUpload, String tenantId) throws Exception {

		Resource resource = resourceLoader.getResource("classpath:templates/" + "Resident_Withholding_Summary.xls");
		InputStream input = resource.getInputStream();

		try (SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook(input))) {
			SXSSFSheet worksheet = workbook.getSheetAt(0);
			AtomicInteger integer=new AtomicInteger(1);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			logger.info("Preparing Resident Summery Report {}");
			invoiceShareholderResidentList.stream().forEach(record->{
				SXSSFRow row = worksheet.createRow(integer.addAndGet(1));
				acceptResidentData(record, row, deductorPan,formatter);
			});
			logger.debug("File Writing End");

			File file = new File("Resident_Withholding_Summary" + System.currentTimeMillis() + ".xlsx");
			OutputStream out = new FileOutputStream(file);
			workbook.write(out);
			InputStream inputstream = new FileInputStream(file);

			MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "application/vnd.ms-excel",
					IOUtils.toByteArray(inputstream));
			inputstream.close();
			out.close();

			// update batch upload
			updateBatchUpload(batchUpload, tenantId, multipartFile, (long) invoiceShareholderResidentList.size());

		} catch (IOException ex) {
			logger.info("EXception occured while Prepoparing Resident Summery report "+ex);
			throw new RuntimeException(ex);
		}
	}

	private void acceptResidentData(InvoiceShareholderResident invoiceShareholderResident, SXSSFRow row,
			String deductorPan,SimpleDateFormat formatter) {

		row.createCell(0).setCellValue(checkInteger(invoiceShareholderResident.getId()));
		row.createCell(1)
				.setCellValue(StringUtils.isBlank(invoiceShareholderResident.getDeductorTan()) ? StringUtils.EMPTY
						: invoiceShareholderResident.getDeductorTan());
		row.createCell(2)
				.setCellValue(StringUtils.isBlank(invoiceShareholderResident.getDeductorPan()) ? StringUtils.EMPTY
						: invoiceShareholderResident.getDeductorPan());
		row.createCell(3).setCellValue(
				StringUtils.isBlank(invoiceShareholderResident.getDeductorDividendType()) ? StringUtils.EMPTY
						: invoiceShareholderResident.getDeductorDividendType());
		row.createCell(4)
				.setCellValue(StringUtils.isBlank(invoiceShareholderResident.getFolioNumber()) ? StringUtils.EMPTY
						: invoiceShareholderResident.getFolioNumber());
		row.createCell(5)
				.setCellValue(StringUtils.isBlank(invoiceShareholderResident.getUniqueIdentificationNumber())
						? StringUtils.EMPTY
						: invoiceShareholderResident.getUniqueIdentificationNumber());

		row.createCell(6)
				.setCellValue(StringUtils.isBlank(invoiceShareholderResident.getShareholderPan()) ? StringUtils.EMPTY
						: (invoiceShareholderResident.getShareholderPan().equalsIgnoreCase("none") ? ""
								: invoiceShareholderResident.getShareholderPan()));
		row.createCell(7)
				.setCellValue(StringUtils.isBlank(invoiceShareholderResident.getAadharNumber()) ? StringUtils.EMPTY
						: invoiceShareholderResident.getAadharNumber());
		row.createCell(8)
				.setCellValue(StringUtils.isBlank(invoiceShareholderResident.getShareholderName()) ? StringUtils.EMPTY
						: invoiceShareholderResident.getShareholderName());
		row.createCell(9).setCellValue(
				StringUtils.isBlank(invoiceShareholderResident.getShareholderCategory()) ? StringUtils.EMPTY
						: invoiceShareholderResident.getShareholderCategory());
		row.createCell(10)
				.setCellValue(StringUtils.isBlank(invoiceShareholderResident.getShareholderType()) ? StringUtils.EMPTY
						: invoiceShareholderResident.getShareholderType());
		row.createCell(11).setCellValue(checkBoolean(invoiceShareholderResident.getKeyShareholder()));
		row.createCell(12).setCellValue(StringUtils.isBlank(invoiceShareholderResident.getCountry()) ? StringUtils.EMPTY
				: invoiceShareholderResident.getCountry());
		row.createCell(13).setCellValue(StringUtils.isBlank(invoiceShareholderResident.getEmailId()) ? StringUtils.EMPTY
				: invoiceShareholderResident.getEmailId());
		row.createCell(14)
				.setCellValue(StringUtils.isBlank(invoiceShareholderResident.getDematAccountNo()) ? StringUtils.EMPTY
						: invoiceShareholderResident.getDematAccountNo());
		row.createCell(15)
				.setCellValue(StringUtils.isBlank(invoiceShareholderResident.getDividendWarrantNo()) ? StringUtils.EMPTY
						: invoiceShareholderResident.getDividendWarrantNo());
		row.createCell(16).setCellValue(checkBoolean(invoiceShareholderResident.getForm15ghApplicable()));
		row.createCell(17).setCellValue(checkBigdecimal(invoiceShareholderResident.getTotalSharesHeld()));
		row.createCell(18).setCellValue(checkBigdecimal(invoiceShareholderResident.getPercentageSharesHeld()));
		row.createCell(19).setCellValue(checkBigdecimal(invoiceShareholderResident.getPercentageOfDividendDeclared()));
		row.createCell(20).setCellValue(Objects.isNull(invoiceShareholderResident.getDateOfPosting()) ? "  "
				: formatter.format(invoiceShareholderResident.getDateOfPosting()));
		row.createCell(21).setCellValue(Objects.isNull(invoiceShareholderResident.getDateOfDistribution()) ? "  "
				: formatter.format(invoiceShareholderResident.getDateOfDistribution()));

		// Last Payout
		if (Objects.isNull(invoiceShareholderResident.getLastPayoutId())) {
			if (!Objects.isNull(invoiceShareholderResident.getTransactionCount())) {
				if (invoiceShareholderResident.getTransactionCount() != 0) {
					InvoiceShareholderResident lastTransaction = null;
					List<InvoiceShareholderResident> resident = invoiceShareholderResidentDAO
							.findByTransactionCountPanFolioNo(invoiceShareholderResident.getTransactionCount() - 1,
									invoiceShareholderResident.getFolioNumber(), deductorPan);
					if (!resident.isEmpty()) {
						lastTransaction = resident.get(0);
						row.createCell(22).setCellValue(checkBigdecimal(lastTransaction.getDividendAmountRs()));
						row.createCell(23).setCellValue(checkBigdecimal(lastTransaction.getTdsRate()));
						row.createCell(24).setCellValue(checkBigdecimal(lastTransaction.getTotalWithholding()));
						InvoiceShareholderResident finalLastTransaction = lastTransaction;
						CompletableFuture.runAsync(
								() -> this.updateLastPayoutDetails(finalLastTransaction, invoiceShareholderResident));
					}
				}
			}
		} else {
			row.createCell(22)
					.setCellValue(checkBigdecimal(invoiceShareholderResident.getDividendDeclaredLastPayout()));
			row.createCell(23).setCellValue(checkBigdecimal(invoiceShareholderResident.getTdsRateLastPayout()));
			row.createCell(24).setCellValue(checkBigdecimal(invoiceShareholderResident.getWitholdingLastPayout()));
		}

		if (!Objects.isNull(invoiceShareholderResident.getWithholdingDetails())) {
			ResidentWithholdingDetails withholdingDetails = invoiceShareholderResident.getWithholdingDetails();

			// LDC RATE
			if (!Objects.isNull(withholdingDetails.getLdcSummary())) {
				LDCSummary ldcSummary = withholdingDetails.getLdcSummary();
				row.createCell(25)
						.setCellValue(StringUtils.isBlank(ldcSummary.getCertificateNumber()) ? StringUtils.EMPTY
								: ldcSummary.getCertificateNumber());
				row.createCell(26).setCellValue(checkBigdecimal(ldcSummary.getUtilizedAmount()));
				row.createCell(27).setCellValue(checkBigdecimal(ldcSummary.getThresholdLimit()));
				row.createCell(28).setCellValue(checkBigdecimal(ldcSummary.getDividendAmount()));
				row.createCell(29).setCellValue(checkBigdecimal(ldcSummary.getRate()));
				row.createCell(30).setCellValue(checkBigdecimal(ldcSummary.getWithholding()));
			}

			// Rate as per Act
			if (!Objects.isNull(withholdingDetails.getActSummary())) {
				ActSummary actSummary = withholdingDetails.getActSummary();
				row.createCell(31).setCellValue(checkBigdecimal(actSummary.getThresholdLimit()));
				row.createCell(32).setCellValue(checkBigdecimal(actSummary.getActRate()));
				row.createCell(33).setCellValue(checkBigdecimal(actSummary.getDividendAmount()));
				row.createCell(34).setCellValue(checkBigdecimal(actSummary.getAppliedRate()));
				row.createCell(35).setCellValue(checkBigdecimal(actSummary.getWithholding()));
			}

			// No Pan Exemped
			if (!Objects.isNull(withholdingDetails.getMiscRateSummary())) {
				MiscRateSummary miscRateSummary = withholdingDetails.getMiscRateSummary();
				row.createCell(36).setCellValue(checkBigdecimal(miscRateSummary.getRate()));
				row.createCell(37).setCellValue(checkBigdecimal(miscRateSummary.getWithholding()));
			}
		}
		row.createCell(38).setCellValue(Objects.isNull(invoiceShareholderResident.getPayoutState()) ? StringUtils.EMPTY
				: invoiceShareholderResident.getPayoutState().description());
		
		/**look into the query to identify why  printing values from record type and email id to print 
		PAN VERIFICATION STATUS and Specified person status*/
		row.createCell(39).setCellValue(Objects.isNull(invoiceShareholderResident.getRecordType()) ? "PANNOTAVBL"
				: invoiceShareholderResident.getRecordType()); // PAN VERIFICATION STATUS
		row.createCell(40).setCellValue(Objects.isNull(invoiceShareholderResident.getEmailId()) ? StringUtils.EMPTY
				: invoiceShareholderResident.getEmailId()); //Specified person status
		
		row.createCell(41)
				.setCellValue(StringUtils.isBlank(invoiceShareholderResident.getTdsSection()) ? StringUtils.EMPTY
						: invoiceShareholderResident.getTdsSection());
		row.createCell(42).setCellValue(checkBigdecimal(invoiceShareholderResident.getDividendAmountRs()));
		row.createCell(43).setCellValue(StringUtils.isBlank(invoiceShareholderResident.getRemarks()) ? StringUtils.EMPTY
				: invoiceShareholderResident.getRemarks());
		row.createCell(44).setCellValue(checkBigdecimal(invoiceShareholderResident.getTdsRate()));
		row.createCell(45).setCellValue(checkBigdecimal(invoiceShareholderResident.getTotalWithholding()));
		row.createCell(46).setCellValue(checkBigdecimal(invoiceShareholderResident.getFinalDividendWithholding()));
		row.createCell(47)
				.setCellValue(checkBigdecimal(invoiceShareholderResident.getAccumulatedDividendInCurrentYear()));
		String rateDetermination = "";
		if (!Objects.isNull(invoiceShareholderResident.getWithholdingDetails())) {
			if (!Objects.isNull(invoiceShareholderResident.getWithholdingDetails().getProcessSteps())) {
				for (String s : invoiceShareholderResident.getWithholdingDetails().getProcessSteps()) {
					rateDetermination = rateDetermination.concat(" " + s);
					rateDetermination.concat("\n");
				}

			}
		}
		row.createCell(48).setCellValue(rateDetermination); // Rate Determination Basis
		row.createCell(50).setCellValue(checkBigdecimal(invoiceShareholderResident.getClientOverriddenRate()));
		row.createCell(51).setCellValue("");
	}

	private String checkInteger(int ldcId) {
		if (Objects.isNull(ldcId)) {
			return " ";
		} else {
			return ldcId+"";
		}
	}

	private double checkBigdecimal(BigDecimal value) {
		if (Objects.isNull(value)) {
			return 0;
		} else {
			value.setScale(2,RoundingMode.UP);
			return value.doubleValue();
		}
	}
	
	private String checkBigdecimalWithString(BigDecimal value) {
		if (Objects.isNull(value)) {
			return "0";
		} else {
			value=value.setScale(2);
			return value.toString().contains(".00")?value.toString().replace(".00", ""):value.toString();
		}
	}

	private String checkBoolean(Boolean value) {
		String str = "No";
		if (Objects.isNull(value)) {
			return str;
		} else {
			if (value == true) {
				return "Yes";
			}
			if (value == false) {
				return "No";
			}
		}
		return str;
	}

	private void prcocessNonResidentSummary(List<InvoiceShareholderNonResident> invoiceShareholderNonResidentList,
			String deductorPan, BatchUpload batchUpload, String tenantId) throws Exception {

		Resource resource = resourceLoader.getResource("classpath:templates/" + "Non_Resident_Withholding_Summary.xls");
		InputStream input = resource.getInputStream();

		try (SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook(input))) {
			SXSSFSheet worksheet = workbook.getSheetAt(0);
			AtomicInteger integer=new AtomicInteger(1);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			logger.info("Preparing the  non resident summery report {}");
			invoiceShareholderNonResidentList.stream().forEach(record->{
				SXSSFRow row = worksheet.createRow(integer.addAndGet(1));
				acceptNonResidentData(record, row, deductorPan,formatter);
			});
			logger.debug("File Writing End");

			File file = new File("Non_Resident_Withholding_Summary" + System.currentTimeMillis() + ".xlsx");
			OutputStream out = new FileOutputStream(file);
			workbook.write(out);
			InputStream inputstream = new FileInputStream(file);

			MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "application/vnd.ms-excel",
					IOUtils.toByteArray(inputstream));
			inputstream.close();
			out.close();

			// update batch upload
			updateBatchUpload(batchUpload, tenantId, multipartFile, (long) invoiceShareholderNonResidentList.size());
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void acceptNonResidentData(InvoiceShareholderNonResident invoiceShareholderNonResident, SXSSFRow row,
			String deductorPan,SimpleDateFormat formatter) {

		row.createCell(0).setCellValue(checkInteger(invoiceShareholderNonResident.getId()));
		row.createCell(1)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getDeductorPan()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getDeductorPan());
		row.createCell(2)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getDeductorTan()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getDeductorTan());
		row.createCell(3).setCellValue(
				StringUtils.isBlank(invoiceShareholderNonResident.getDeductorDividendType()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getDeductorDividendType());
		row.createCell(4)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getFolioNumber()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getFolioNumber());
		row.createCell(5)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getUniqueIdentificationNumber())
						? StringUtils.EMPTY
						: invoiceShareholderNonResident.getUniqueIdentificationNumber());

		row.createCell(6).setCellValue(
				StringUtils.isBlank(invoiceShareholderNonResident.getShareholderName()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getShareholderName());
		row.createCell(7).setCellValue(
				StringUtils.isBlank(invoiceShareholderNonResident.getShareholderCategory()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getShareholderCategory());
		row.createCell(8).setCellValue(
				StringUtils.isBlank(invoiceShareholderNonResident.getShareholderType()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getShareholderType());
		row.createCell(9).setCellValue(checkBoolean(invoiceShareholderNonResident.getKeyShareholder()));
		row.createCell(10)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getShareholderPan()) ? StringUtils.EMPTY
						: (invoiceShareholderNonResident.getShareholderPan().equalsIgnoreCase("none") ? ""
								: invoiceShareholderNonResident.getShareholderPan()));
		row.createCell(11)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getShareholderTin()) ? StringUtils.EMPTY
						: (invoiceShareholderNonResident.getShareholderTin().equalsIgnoreCase("none") ? ""
								: invoiceShareholderNonResident.getShareholderTin()));
		row.createCell(12)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getCountry()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getCountry());
		row.createCell(13)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getRemittanceMadeCountry())
						? StringUtils.EMPTY
						: invoiceShareholderNonResident.getRemittanceMadeCountry());
		row.createCell(14)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getPrincipalPlaceOfBusiness())
						? StringUtils.EMPTY
						: invoiceShareholderNonResident.getPrincipalPlaceOfBusiness());
		row.createCell(15)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getEmailId()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getEmailId());
		row.createCell(16)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getDematAccountNo()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getDematAccountNo());
		row.createCell(17).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getTotalSharesHeld()));
		row.createCell(18).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getPercentageSharesHeld()));
		row.createCell(19).setCellValue(Objects.isNull(invoiceShareholderNonResident.getShareHeldFromDate()) ? "  "
				: formatter.format(invoiceShareholderNonResident.getShareHeldFromDate()));
		row.createCell(20).setCellValue(Objects.isNull(invoiceShareholderNonResident.getShareHeldToDate()) ? "  "
				: formatter.format(invoiceShareholderNonResident.getShareHeldToDate()));
		row.createCell(21)
				.setCellValue(checkBigdecimal(invoiceShareholderNonResident.getPercentageOfDividendDeclared()));
		row.createCell(22).setCellValue(Objects.isNull(invoiceShareholderNonResident.getDateOfPosting()) ? "  "
				: formatter.format(invoiceShareholderNonResident.getDateOfPosting()));
		row.createCell(23).setCellValue(Objects.isNull(invoiceShareholderNonResident.getDateOfDistribution()) ? "  "
				: formatter.format(invoiceShareholderNonResident.getDateOfDistribution()));
		row.createCell(24).setCellValue(
				StringUtils.isBlank(invoiceShareholderNonResident.getDividendWarrantNo()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getDividendWarrantNo());
		row.createCell(25)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getDividendAmountForeignCurrency())
						? StringUtils.EMPTY
						: invoiceShareholderNonResident.getDividendAmountForeignCurrency());
		row.createCell(26).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getAggreateRemittanceAmountFY()));
		row.createCell(27).setCellValue(
				StringUtils.isBlank(invoiceShareholderNonResident.getActualRemmitanceAmountForeignCurrency())
						? StringUtils.EMPTY
						: invoiceShareholderNonResident.getActualRemmitanceAmountForeignCurrency());
		row.createCell(28).setCellValue(
				StringUtils.isBlank(invoiceShareholderNonResident.getForm15CACBApplicable()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getForm15CACBApplicable());
		row.createCell(29).setCellValue(checkBoolean(invoiceShareholderNonResident.getTaxPayableGrossedUp()));
		row.createCell(30).setCellValue(checkBoolean(invoiceShareholderNonResident.getIsTrcAvailable()));
		row.createCell(31).setCellValue(checkBoolean(invoiceShareholderNonResident.getIsTenfAvailable()));
		row.createCell(32).setCellValue(checkBoolean(invoiceShareholderNonResident.getIsPeAvailableInIndia()));
		row.createCell(33)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getIsNoPeDeclarationAvailable())
						? "No"
						: (invoiceShareholderNonResident.getIsNoPeDeclarationAvailable().equals("1")?"Yes":"No"));
		row.createCell(34).setCellValue(checkBoolean(invoiceShareholderNonResident.getIsPoemOfShareholderInIndia()));
		row.createCell(35).setCellValue(checkBoolean(invoiceShareholderNonResident.getIsNoPoemDeclarationAvailable()));
		row.createCell(36).setCellValue(
				StringUtils.isBlank(invoiceShareholderNonResident.getIsMliSlobSatisfactionDeclarationAvailable())
						? "No"
						: (invoiceShareholderNonResident.getIsMliSlobSatisfactionDeclarationAvailable().equals("1")?"Yes":"No"));
		row.createCell(37).setCellValue(checkBoolean(invoiceShareholderNonResident.getIsBeneficialOwnerOfIncome()));
		row.createCell(38).setCellValue(
				StringUtils.isBlank(invoiceShareholderNonResident.getIsBeneficialOwnershipDeclarationAvailable())
						? "No"
						: (invoiceShareholderNonResident.getIsBeneficialOwnershipDeclarationAvailable().equals("1")?"Yes":"No"));
		row.createCell(39)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getIsTransactionGAARCompliant())
						?  "No"
						: (invoiceShareholderNonResident.getIsTransactionGAARCompliant().equals("1")?"Yes":"No"));
		row.createCell(40).setCellValue(checkBoolean(invoiceShareholderNonResident.getTreatyBenefitsByIndemnity()));
		row.createCell(41).setCellValue(checkBoolean(invoiceShareholderNonResident.getIsKuwaitShareholderType()));
		row.createCell(42).setCellValue(checkBoolean(invoiceShareholderNonResident.getIsUKVehicleExemptTax()));
		row.createCell(43)
				.setCellValue(checkBigdecimal(invoiceShareholderNonResident.getIcelandDividendTaxationRate()));
		row.createCell(44).setCellValue(checkBoolean(invoiceShareholderNonResident.getIsIcelandRateLessThanTwenty()));

		// Last Payout
		if (Objects.isNull(invoiceShareholderNonResident.getLastPayoutId())) {
			if (!Objects.isNull(invoiceShareholderNonResident.getTransactionCount())) {
				if (invoiceShareholderNonResident.getTransactionCount() != 0) {
					InvoiceShareholderNonResident lastTransaction = null;
					List<InvoiceShareholderNonResident> nonResident = invoiceShareholderNonResidentDAO
							.findByTransactionCountPanFolioNo(invoiceShareholderNonResident.getTransactionCount() - 1,
									invoiceShareholderNonResident.getFolioNumber(), deductorPan);
					if (nonResident.isEmpty()) {
						lastTransaction = nonResident.get(0);
						row.createCell(45).setCellValue(checkBigdecimal(lastTransaction.getDividendAmountRs()));
						row.createCell(46).setCellValue(checkBigdecimal(lastTransaction.getTdsRate()));
						row.createCell(47).setCellValue(checkBigdecimal(lastTransaction.getTotalWithholding()));
						InvoiceShareholderNonResident finalLastTransaction = lastTransaction;
						CompletableFuture.runAsync(() -> this.updateLastPayoutDetailsNonRes(finalLastTransaction,
								invoiceShareholderNonResident));
					}
				}
			}
		} else {
			row.createCell(45)
					.setCellValue(checkBigdecimal(invoiceShareholderNonResident.getDividendDeclaredLastPayout()));
			row.createCell(46).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getTdsRateLastPayout()));
			row.createCell(47).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getWitholdingLastPayout()));
		}

		// Treaty Eligibility as per client input and Onboarding parameters
		row.createCell(48).setCellValue(checkBoolean(invoiceShareholderNonResident.getTreatyBenefitsInDocsAbsence()));
		row.createCell(49).setCellValue(checkBoolean(invoiceShareholderNonResident.getTreatyBenefitsByIndemnity()));
		row.createCell(50).setCellValue(checkBoolean(invoiceShareholderNonResident.getMfnClauseTreatyBenfit()));
		row.createCell(51)
				.setCellValue(checkBoolean(invoiceShareholderNonResident.getShareholderCategoryTreatyBenfit()));
		row.createCell(52)
				.setCellValue(checkBoolean(invoiceShareholderNonResident.getDeductorBusinessTrustOrMutualFund()));
		row.createCell(53).setCellValue(checkBoolean(invoiceShareholderNonResident.getSurchargeAndCessOnTDS()));

		if (!Objects.isNull(invoiceShareholderNonResident.getWithholdingDetails())) {
			NonResidentWithholdingDetails withholdingDetails = invoiceShareholderNonResident.getWithholdingDetails();

			// LDC RATE
			if (!Objects.isNull(withholdingDetails.getLdcSummary())) {
				LDCAOSummary ldcSummary = withholdingDetails.getLdcSummary();
				row.createCell(54)
						.setCellValue(StringUtils.isBlank(ldcSummary.getCertificateNumber()) ? StringUtils.EMPTY
								: ldcSummary.getCertificateNumber());
				row.createCell(55).setCellValue(checkBigdecimal(ldcSummary.getUtilizedAmount()));
				row.createCell(56).setCellValue(checkBigdecimal(ldcSummary.getThresholdLimit()));
				row.createCell(57).setCellValue(checkBigdecimal(ldcSummary.getDividendAmount()));
				row.createCell(58).setCellValue(checkBigdecimal(ldcSummary.getRate()));
				row.createCell(59).setCellValue(checkBigdecimal(ldcSummary.getWithholding()));
			}

			if (!Objects.isNull(withholdingDetails.getTreatySummary())
					&& !Objects.isNull(withholdingDetails.getActSummary())) {
				TreatySummary treatySummary = withholdingDetails.getTreatySummary();
				ActSummary actSummary = withholdingDetails.getActSummary();
				BigDecimal ldcWithholding = !Objects.isNull(withholdingDetails.getLdcSummary())
						? withholdingDetails.getLdcSummary().getWithholding()
						: new BigDecimal(0);
				BigDecimal actLdcWithholding = BigDecimal
						.valueOf(actSummary.getWithholding().add(ldcWithholding).doubleValue())
						.setScale(2, RoundingMode.UP);
				BigDecimal treatyWithholding = BigDecimal.valueOf(
						treatySummary.getWithholding() != null ? treatySummary.getWithholding().doubleValue() : 0)
						.add(ldcWithholding).setScale(2, RoundingMode.UP);
				if (actLdcWithholding.equals(invoiceShareholderNonResident.getTotalWithholding())) {
					row.createCell(60).setCellValue("");
					row.createCell(61).setCellValue("");
					row.createCell(62).setCellValue("");
					row.createCell(63).setCellValue("");
					row.createCell(64).setCellValue("");
					row.createCell(65).setCellValue("");
					row.createCell(66).setCellValue("");
					row.createCell(67).setCellValue("");
					row.createCell(68).setCellValue("");
					row.createCell(69).setCellValue(checkBigdecimal(actSummary.getThresholdLimit()));
					row.createCell(70).setCellValue(checkBigdecimal(actSummary.getActRate()));
					row.createCell(71).setCellValue(checkBigdecimal(actSummary.getDividendAmount()));
					row.createCell(72).setCellValue(checkBigdecimal(actSummary.getAppliedRate()));
					row.createCell(73).setCellValue(checkBigdecimal(actSummary.getWithholding()));
				}
				if (treatyWithholding.equals(invoiceShareholderNonResident.getTotalWithholding())) {
					row.createCell(60)
							.setCellValue(StringUtils.isBlank(treatySummary.getTaxTreatyClause()) ? StringUtils.EMPTY
									: treatySummary.getTaxTreatyClause());
					row.createCell(61).setCellValue(checkBoolean(treatySummary.getMfnClauseExists()));
					row.createCell(62).setCellValue(checkBoolean(treatySummary.getMliArticle8Applicable()));
					row.createCell(63).setCellValue(checkBoolean(treatySummary.getMliPptConditionSatisfied()));
					row.createCell(64).setCellValue(checkBoolean(treatySummary.getMliSlobConditionSatisfied()));
					row.createCell(65).setCellValue(checkBoolean(treatySummary.getMfnBenefitAvailed()));
					row.createCell(66).setCellValue(checkBigdecimal(treatySummary.getDividendAmount()));
					row.createCell(67).setCellValue(checkBigdecimal(treatySummary.getRate()));
					row.createCell(68).setCellValue(checkBigdecimal(treatySummary.getWithholding()));
					row.createCell(69).setCellValue("");
					row.createCell(70).setCellValue("");
					row.createCell(71).setCellValue("");
					row.createCell(72).setCellValue("");
					row.createCell(73).setCellValue("");
				}

			}

			if (!Objects.isNull(withholdingDetails.getTreatySummary())
					&& Objects.isNull(withholdingDetails.getActSummary())) {
				TreatySummary treatySummary = withholdingDetails.getTreatySummary();
				row.createCell(60)
						.setCellValue(StringUtils.isBlank(treatySummary.getTaxTreatyClause()) ? StringUtils.EMPTY
								: treatySummary.getTaxTreatyClause());
				row.createCell(61).setCellValue(checkBoolean(treatySummary.getMfnClauseExists()));
				row.createCell(62).setCellValue(checkBoolean(treatySummary.getMliArticle8Applicable()));
				row.createCell(63).setCellValue(checkBoolean(treatySummary.getMliPptConditionSatisfied()));
				row.createCell(64).setCellValue(checkBoolean(treatySummary.getMliSlobConditionSatisfied()));
				row.createCell(65).setCellValue(checkBoolean(treatySummary.getMfnBenefitAvailed()));
				row.createCell(66).setCellValue(checkBigdecimal(treatySummary.getDividendAmount()));
				row.createCell(67).setCellValue(checkBigdecimal(treatySummary.getRate()));
				row.createCell(68).setCellValue(checkBigdecimal(treatySummary.getWithholding()));
			}

			// Rate as per IT Act
			if (!Objects.isNull(withholdingDetails.getActSummary())
					&& Objects.isNull(withholdingDetails.getTreatySummary())) {
				ActSummary actSummary = withholdingDetails.getActSummary();
				row.createCell(69).setCellValue(checkBigdecimal(actSummary.getThresholdLimit()));
				row.createCell(70).setCellValue(checkBigdecimal(actSummary.getActRate()));
				row.createCell(71).setCellValue(checkBigdecimal(actSummary.getDividendAmount()));
				row.createCell(72).setCellValue(checkBigdecimal(actSummary.getAppliedRate()));
				row.createCell(73).setCellValue(checkBigdecimal(actSummary.getWithholding()));
			}
			// No Pan Exemped
			if (!Objects.isNull(withholdingDetails.getMiscRateSummary())) {
				MiscRateSummary miscRateSummary = withholdingDetails.getMiscRateSummary();
				row.createCell(74).setCellValue(checkBigdecimal(miscRateSummary.getRate()));
				row.createCell(75).setCellValue(checkBigdecimal(miscRateSummary.getWithholding()));
			}
		}
		// Surcharge
		row.createCell(76).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getSurchargeRate()));
		row.createCell(77).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getSurchargeWithholding()));

		// Cess
		row.createCell(78).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getCessRate()));
		row.createCell(79).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getCessWithholding()));

		row.createCell(80)
				.setCellValue(Objects.isNull(invoiceShareholderNonResident.getPayoutState()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getPayoutState().description());
		row.createCell(81)
				.setCellValue(StringUtils.isBlank(invoiceShareholderNonResident.getTdsSection()) ? StringUtils.EMPTY
						: invoiceShareholderNonResident.getTdsSection());
		row.createCell(82).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getDividendAmountRs()));
		row.createCell(83).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getTdsRate()));
		row.createCell(84).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getTotalWithholding()));
		row.createCell(85).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getFinalDividendWithholding()));
		row.createCell(86)
				.setCellValue(checkBigdecimal(invoiceShareholderNonResident.getAccumulatedDividendInCurrentYear()));

		String rateDetermination = "";
		if (!Objects.isNull(invoiceShareholderNonResident.getWithholdingDetails())) {
			if (!Objects.isNull(invoiceShareholderNonResident.getWithholdingDetails().getProcessSteps())) {
				for (String s : invoiceShareholderNonResident.getWithholdingDetails().getProcessSteps()) {
					rateDetermination = rateDetermination.concat(" " + s);
					rateDetermination.concat("\n");
				}
			}
		}
		row.createCell(87).setCellValue(rateDetermination); // Rate Determination basis (Description of how the rate is
															// derived)
		row.createCell(89).setCellValue(checkBigdecimal(invoiceShareholderNonResident.getClientOverriddenRate()));
		row.createCell(90).setCellValue(" ");
	}

	private void updateLastPayoutDetails(InvoiceShareholderResident finalLastTransaction,
			InvoiceShareholderResident invoiceShareholderResident) {
		invoiceShareholderResident.setWitholdingLastPayout(finalLastTransaction.getTotalWithholding());
		invoiceShareholderResident.setDividendDeclaredLastPayout(finalLastTransaction.getDividendAmountRs());
		invoiceShareholderResident.setTdsRateLastPayout(finalLastTransaction.getTdsRate());
		invoiceShareholderResident.setLastPayoutId(finalLastTransaction.getId());
		invoiceShareholderResidentDAO.updateInvoiceShareholderResident(invoiceShareholderResident);
	}

	private void updateLastPayoutDetailsNonRes(InvoiceShareholderNonResident finalLastTransaction,
			InvoiceShareholderNonResident invoiceShareholderNonResident) {
		invoiceShareholderNonResident.setWitholdingLastPayout(finalLastTransaction.getTotalWithholding());
		invoiceShareholderNonResident.setDividendDeclaredLastPayout(finalLastTransaction.getDividendAmountRs());
		invoiceShareholderNonResident.setTdsRateLastPayout(finalLastTransaction.getTdsRate());
		invoiceShareholderNonResident.setLastPayoutId(finalLastTransaction.getId());
		invoiceShareholderNonResidentDAO.updateInvoiceShareholderNonResident(invoiceShareholderNonResident);
	}

	@Async
	public void asyncDownloadDividendLiabilityReport(BatchUpload batchUpload, String deductorPan, Integer year,
			String tenantId, String level) throws Exception {
		MultiTenantContext.setTenantId(tenantId);

		List<InvoiceShareholderResident> invoiceShareholderResidentList = invoiceShareholderResidentDAO
				.findAllApprovedRecordsByPanAndYear(deductorPan, year);
		List<InvoiceShareholderNonResident> invoiceShareholderNonResidentList = invoiceShareholderNonResidentDAO
				.findAllApprovedRecordsByPanAndYear(deductorPan, year);
		if (!level.equals("FOLIO")) {
			Map<String, InvoiceShareholderResident> residentMap = new HashMap<>();
			Map<String, InvoiceShareholderNonResident> nonResidentMap = new HashMap<>();

			for (InvoiceShareholderResident res : invoiceShareholderResidentList) {
				String key = (res.getShareholderPan() == null ? "" : res.getShareholderPan())
						+ (res.getUniqueIdentificationNumber() == null ? "" : res.getUniqueIdentificationNumber())
						+ (res.getAadharNumber() == null ? "" : res.getAadharNumber());
				if(StringUtils.isBlank(key)) {
					key=res.getFolioNumber();
				}
				if (residentMap.containsKey(key.trim())) { 
					InvoiceShareholderResident dto = residentMap.get(key.trim());
					dto.setDividendDeclaredLastPayout((dto.getDividendDeclaredLastPayout() == null ? BigDecimal.ZERO
							: dto.getDividendDeclaredLastPayout())
									.add(res.getDividendDeclaredLastPayout() == null ? BigDecimal.ZERO
											: res.getDividendDeclaredLastPayout()));
					dto.setFinalDividendWithholding((dto.getFinalDividendWithholding() == null ? BigDecimal.ZERO
							: dto.getFinalDividendWithholding())
									.add(res.getFinalDividendWithholding() == null ? BigDecimal.ZERO
											: res.getFinalDividendWithholding()));
					dto.setDividendAmountRs((dto.getDividendAmountRs() == null ? BigDecimal.ZERO
							: dto.getDividendAmountRs()).add(
									res.getDividendAmountRs() == null ? BigDecimal.ZERO : res.getDividendAmountRs()));
					dto.setTotalWithholding((dto.getTotalWithholding() == null ? BigDecimal.ZERO
							: dto.getTotalWithholding()).add(
									res.getTotalWithholding() == null ? BigDecimal.ZERO : res.getTotalWithholding()));
					dto.setClientOverriddenWithholding(dto.getClientOverriddenWithholding() == null ? BigDecimal.ZERO
							: dto.getClientOverriddenWithholding());
					dto.setPreFinlAmount(dto.getPreFinlAmount() == null ? BigDecimal.ZERO
							: dto.getPreFinlAmount());
					residentMap.put(key.trim(), dto);
				} else {
					residentMap.put(key.trim(), res);
					// uniqueResident.add(res);
				}
			}

			for (InvoiceShareholderNonResident nr : invoiceShareholderNonResidentList) {
				String key = (nr.getShareholderPan() == null ? "" : nr.getShareholderPan())
						+ (nr.getUniqueIdentificationNumber() == null ? "" : nr.getUniqueIdentificationNumber())
						+ (nr.getShareholderTin() == null ? "" : nr.getShareholderTin());
				if(StringUtils.isBlank(key)) {
					key=nr.getFolioNumber();
				}
				if (nonResidentMap.containsKey(key.trim())) {
					InvoiceShareholderNonResident dto = nonResidentMap.get(key.trim());
					dto.setDividendDeclaredLastPayout((dto.getDividendDeclaredLastPayout() == null ? BigDecimal.ZERO
							: dto.getDividendDeclaredLastPayout())
									.add(nr.getDividendDeclaredLastPayout() == null ? BigDecimal.ZERO
											: nr.getDividendDeclaredLastPayout()));
					dto.setFinalDividendWithholdingDecimal((dto.getFinalDividendWithholding() == null ? BigDecimal.ZERO
							: dto.getFinalDividendWithholding())
									.add(nr.getFinalDividendWithholding() == null ? BigDecimal.ZERO
											: nr.getFinalDividendWithholding()));
					dto.setDividendAmountRs((dto.getDividendAmountRs() == null ? BigDecimal.ZERO
							: dto.getDividendAmountRs()).add(
									nr.getDividendAmountRs() == null ? BigDecimal.ZERO : nr.getDividendAmountRs()));
					dto.setTotalWithholding((dto.getTotalWithholding() == null ? BigDecimal.ZERO
							: dto.getTotalWithholding()).add(
									nr.getTotalWithholding() == null ? BigDecimal.ZERO : nr.getTotalWithholding()));
					dto.setClientOverriddenWithholding(dto.getClientOverriddenWithholding() == null ? BigDecimal.ZERO
							: dto.getClientOverriddenWithholding());
					dto.setCessWithholding((dto.getCessWithholding() == null ? BigDecimal.ZERO
							: dto.getCessWithholding())
									.add(nr.getCessWithholding() == null ? BigDecimal.ZERO : nr.getCessWithholding()));
					dto.setSurchargeWithholding((dto.getSurchargeWithholding() == null ? BigDecimal.ZERO
							: dto.getSurchargeWithholding()).add(nr.getSurchargeWithholding() == null ? BigDecimal.ZERO
									: nr.getSurchargeWithholding()));
					nonResidentMap.put(key.trim(), dto);
				} else {
					nonResidentMap.put(key.trim(), nr);
					// uniqueResident.add(nr);
				}
			}

			invoiceShareholderNonResidentList.clear();
			invoiceShareholderResidentList.clear();
			residentMap.entrySet().stream().forEach(n -> invoiceShareholderResidentList.add(n.getValue()));
			nonResidentMap.entrySet().stream().forEach(n -> invoiceShareholderNonResidentList.add(n.getValue()));
		}

		processLiabilityReport(invoiceShareholderResidentList, invoiceShareholderNonResidentList, deductorPan,
				batchUpload, tenantId, level);
	}

	private void processLiabilityReport(List<InvoiceShareholderResident> invoiceShareholderResidentList,
			List<InvoiceShareholderNonResident> invoiceShareholderNonResidentList, String deductorPan,
			BatchUpload batchUpload, String tenantId, String level)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		Resource resource = resourceLoader.getResource("classpath:templates/" + "Liability_Report.xlsx");
		InputStream input = resource.getInputStream();
		BigDecimal amountOfDividend = new BigDecimal(0);
		BigDecimal tdsAmount = new BigDecimal(0);
		BigDecimal totalSurcharge = new BigDecimal(0);
		BigDecimal totalCess = new BigDecimal(0);
		Set<String> uniqueFolioNoRes = new HashSet<>();
		Set<String> uniqueFolioNoNr = new HashSet<>();
		try (XSSFWorkbook workbook = new XSSFWorkbook(input)) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			AtomicInteger count = new AtomicInteger();
			for (int i = 0; i < invoiceShareholderResidentList.size(); i++) {
				amountOfDividend = amountOfDividend.add(invoiceShareholderResidentList.get(i).getDividendAmountRs());
				tdsAmount = tdsAmount.add(invoiceShareholderResidentList.get(i).getTotalWithholding());
				uniqueFolioNoRes.add(invoiceShareholderResidentList.get(i).getFolioNumber());
				int totalRows = worksheet.getPhysicalNumberOfRows();
				XSSFRow row = worksheet.createRow(totalRows);
				acceptLiabilityData(invoiceShareholderResidentList.get(i), null, row, count);
			}
			for (int i = 0; i < invoiceShareholderNonResidentList.size(); i++) {
				amountOfDividend = amountOfDividend.add(invoiceShareholderNonResidentList.get(i).getDividendAmountRs());
				tdsAmount = tdsAmount.add(invoiceShareholderNonResidentList.get(i).getTotalWithholding());
				totalSurcharge = totalSurcharge.add(invoiceShareholderNonResidentList.get(i).getSurchargeWithholding());
				totalCess = totalCess.add(invoiceShareholderNonResidentList.get(i).getCessWithholding());
				uniqueFolioNoNr.add(invoiceShareholderNonResidentList.get(i).getFolioNumber());
				int totalRows = worksheet.getPhysicalNumberOfRows();
				XSSFRow row = worksheet.createRow(totalRows);
				acceptLiabilityData(null, invoiceShareholderNonResidentList.get(i), row, count);
			}
			
			
			DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();
			XSSFCellStyle blueStyle=createStyle(defaultIndexedColorMap, workbook, 226, 226, 226);
			
			int totalShareholders = uniqueFolioNoRes.size() + uniqueFolioNoNr.size();
			int totalRows = worksheet.getPhysicalNumberOfRows();
			NumberFormat numberFormat = NumberFormat.getInstance();
			XSSFRow row = worksheet.createRow(totalRows+1);
			row.createCell(14).setCellValue(numberFormat.format(amountOfDividend.doubleValue()));
			row.createCell(21).setCellValue(numberFormat.format(tdsAmount.doubleValue()));
			row.createCell(23).setCellValue(numberFormat.format(totalSurcharge.doubleValue()));
			row.createCell(25).setCellValue(numberFormat.format(totalCess.doubleValue()));
			row.getCell(14).setCellStyle(blueStyle);
			row.getCell(14).setCellStyle(blueStyle);
			row.getCell(21).setCellStyle(blueStyle);
			row.getCell(23).setCellStyle(blueStyle);
			row.getCell(25).setCellStyle(blueStyle);
			BigDecimal totalTdsLiability = new BigDecimal(0);
			totalTdsLiability = totalTdsLiability.add(tdsAmount).add(totalSurcharge).add(totalCess);
			row.createCell(27).setCellValue(numberFormat.format(totalTdsLiability.doubleValue()));
			row.getCell(27).setCellStyle(blueStyle);

			logger.debug("File Writing End");

			worksheet.setDefaultRowHeightInPoints(2 * worksheet.getDefaultRowHeightInPoints());

			File file = new File("Liability_Report" + level + System.currentTimeMillis() + ".xlsx");
			OutputStream out = new FileOutputStream(file);
			workbook.write(out);
			InputStream inputstream = new FileInputStream(file);

			MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "application/vnd.ms-excel",
					IOUtils.toByteArray(inputstream));
			inputstream.close();
			out.close();

			// update batch upload
			updateBatchUpload(batchUpload, tenantId, multipartFile, (long) invoiceShareholderResidentList.size());

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void acceptLiabilityData(InvoiceShareholderResident resident, InvoiceShareholderNonResident nonResident,
			XSSFRow row, AtomicInteger counter) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		if (!Objects.isNull(resident)) {
			// resident record
			row.createCell(0).setCellValue(counter.addAndGet(1));
			row.createCell(1).setCellValue(
					StringUtils.isBlank(resident.getFolioNumber()) ? StringUtils.EMPTY : resident.getFolioNumber());
			row.createCell(2).setCellValue(
					Objects.isNull(resident.getDateOfPosting()) ? "  " : formatter.format(resident.getDateOfPosting()));
			row.createCell(3).setCellValue(StringUtils.isBlank(resident.getShareholderPan()) ? StringUtils.EMPTY
					: (resident.getShareholderPan().equals("None") ? "" : resident.getShareholderPan()));
			row.createCell(4)
					.setCellValue(StringUtils.isBlank(resident.getUniqueIdentificationNumber()) ? StringUtils.EMPTY
							: resident.getUniqueIdentificationNumber());
			row.createCell(5).setCellValue(
					StringUtils.isBlank(resident.getAadharNumber()) ? StringUtils.EMPTY : resident.getAadharNumber());
			row.createCell(6).setCellValue(StringUtils.isBlank(resident.getShareholderName()) ? StringUtils.EMPTY
					: resident.getShareholderName());
			row.createCell(7).setCellValue(StringUtils.isBlank(resident.getShareholderCategory()) ? StringUtils.EMPTY
					: resident.getShareholderCategory());
			row.createCell(8).setCellValue(StringUtils.isBlank(resident.getShareholderType()) ? StringUtils.EMPTY
					: resident.getShareholderType());
			row.createCell(9).setCellValue("Resident");
			row.createCell(10).setCellValue(StringUtils.isBlank(resident.getCountry()) ? StringUtils.EMPTY
					: resident.getCountry().toUpperCase());
			// lastPayout
			if (Objects.isNull(resident.getLastPayoutId())) {
				if (!Objects.isNull(resident.getTransactionCount())) {
					if (resident.getTransactionCount() != 0) {
						InvoiceShareholderResident lastTransaction = null;
						List<InvoiceShareholderResident> residentOptional = invoiceShareholderResidentDAO
								.findByTransactionCountPanFolioNo(resident.getTransactionCount() - 1,
										resident.getFolioNumber(), resident.getDeductorPan());
						if (!residentOptional.isEmpty()) {
							lastTransaction = residentOptional.get(0);
							row.createCell(11).setCellValue(checkBigdecimal(lastTransaction.getDividendAmountRs()));
							row.createCell(12).setCellValue(checkBigdecimalWithString(lastTransaction.getTdsRate()));
							InvoiceShareholderResident finalLastTransaction = lastTransaction;
							CompletableFuture
									.runAsync(() -> this.updateLastPayoutDetails(finalLastTransaction, resident));
						}
					}
				}
			} else {
				row.createCell(11).setCellValue(checkBigdecimal(resident.getDividendDeclaredLastPayout()));
				row.createCell(12).setCellValue(checkBigdecimal(resident.getTdsRateLastPayout()));
			}
			row.createCell(13).setCellValue(resident.getIsExempt()==false?"No":"Yes");
			row.createCell(14).setCellValue(checkBigdecimal(resident.getDividendAmountRs()));
			row.createCell(15).setCellValue("NA");
			row.createCell(17)
					.setCellValue(StringUtils.isBlank(resident.getTdsSection()) ? "" : resident.getTdsSection());
			if (!Objects.isNull(resident.getWithholdingDetails())) {
				ActSummary actSummary = resident.getWithholdingDetails().getActSummary();
				if (!Objects.isNull(actSummary)) {
					row.createCell(16).setCellValue(checkBigdecimalWithString(actSummary.getActRate()));
				}
				LDCSummary ldcSummary = resident.getWithholdingDetails().getLdcSummary();
				if (!Objects.isNull(ldcSummary)) {
					row.createCell(18).setCellValue(checkBoolean(resident.getWithholdingDetails().isLdcApplied()));
					row.createCell(19).setCellValue(checkBigdecimalWithString(ldcSummary.getRate()));
				} else {
					row.createCell(18).setCellValue("No");
					row.createCell(19).setCellValue("NA");
				}
			}
			row.createCell(20).setCellValue(StringUtils.isBlank(resident.getEmailId()) ? StringUtils.EMPTY
					: resident.getEmailId());//Specified person ///
			if(StringUtils.isNotBlank(resident.getRemarks()) && resident.getRemarks().equals("NORMAL RATE")) {
				row.createCell(21).setCellValue("No");//Higher rate applicable
			}else {
				row.createCell(21).setCellValue("Yes");//Higher rate applicable
			}
			
			row.createCell(22).setCellValue(checkBigdecimalWithString(resident.getTdsRate()));
			if (!Objects.isNull(resident.getPayoutState()))
				row.createCell(23).setCellValue(checkBigdecimal(resident.getTotalWithholding()));
			row.createCell(24).setCellValue("NA");
			row.createCell(25).setCellValue("NA");
			row.createCell(26).setCellValue("NA");
			row.createCell(27).setCellValue("NA");
			row.createCell(28).setCellValue(checkBigdecimalWithString(resident.getTdsRate()));
			row.createCell(29).setCellValue(checkBigdecimal(resident.getTotalWithholding()));
			if(resident.getClientOverriddenRate()!=null && resident.getClientOverriddenRate().compareTo(BigDecimal.ZERO)!=0) {
				row.createCell(28).setCellValue(checkBigdecimalWithString(resident.getClientOverriddenRate()));
			}
			if(resident.getClientOverriddenWithholding()!=null && resident.getClientOverriddenWithholding().compareTo(BigDecimal.ZERO)!=0) {
				row.createCell(29).setCellValue(checkBigdecimal(resident.getClientOverriddenWithholding()));
			}
			row.createCell(30).setCellValue("NA");
			row.createCell(31).setCellValue("NA");
			row.createCell(32).setCellValue("NA");
			row.createCell(33).setCellValue("NA");
			row.createCell(34).setCellValue("NA");
			row.createCell(35).setCellValue("NA");
			row.createCell(36).setCellValue("NA");
			row.createCell(37).setCellValue("NA");
		}
		if (!Objects.isNull(nonResident)) {
			// non res record
			row.createCell(0).setCellValue(counter.addAndGet(1));
			row.createCell(1).setCellValue(StringUtils.isBlank(nonResident.getFolioNumber()) ? StringUtils.EMPTY
					: nonResident.getFolioNumber());
			row.createCell(2).setCellValue(Objects.isNull(nonResident.getDateOfPosting()) ? "  "
					: formatter.format(nonResident.getDateOfPosting()));
			row.createCell(3).setCellValue(StringUtils.isBlank(nonResident.getShareholderPan()) ? StringUtils.EMPTY
					: nonResident.getShareholderPan());
			row.createCell(4).setCellValue(StringUtils.isBlank(nonResident.getUniqueIdentificationNumber()) ? StringUtils.EMPTY
					: nonResident.getUniqueIdentificationNumber());
			row.createCell(5).setCellValue("");
			row.createCell(6).setCellValue(StringUtils.isBlank(nonResident.getShareholderName()) ? StringUtils.EMPTY
					: nonResident.getShareholderName());
			row.createCell(7).setCellValue(StringUtils.isBlank(nonResident.getShareholderCategory()) ? StringUtils.EMPTY
					: nonResident.getShareholderCategory());
			row.createCell(8).setCellValue(StringUtils.isBlank(nonResident.getShareholderType()) ? StringUtils.EMPTY
					: nonResident.getShareholderType());
			row.createCell(9).setCellValue("Non-Resident");
			row.createCell(10).setCellValue(StringUtils.isBlank(nonResident.getCountry()) ? StringUtils.EMPTY
					: nonResident.getCountry().toUpperCase());
			// lastPayout
			if (Objects.isNull(nonResident.getLastPayoutId())) {
				if (!Objects.isNull(nonResident.getTransactionCount())) {
					if (nonResident.getTransactionCount() != 0) {
						InvoiceShareholderNonResident lastTransaction = null;
						List<InvoiceShareholderNonResident> nonResidentOptional = invoiceShareholderNonResidentDAO
								.findByTransactionCountPanFolioNo(nonResident.getTransactionCount() - 1,
										nonResident.getFolioNumber(), nonResident.getDeductorPan());
						if (nonResidentOptional.isEmpty()) {
							lastTransaction = nonResidentOptional.get(0);
							row.createCell(11).setCellValue(checkBigdecimal(lastTransaction.getDividendAmountRs()));
							row.createCell(12).setCellValue(checkBigdecimalWithString(lastTransaction.getTdsRate()));
							InvoiceShareholderNonResident finalLastTransaction = lastTransaction;
							CompletableFuture.runAsync(
									() -> this.updateLastPayoutDetailsNonRes(finalLastTransaction, nonResident));
						}
					}
				}
			} else {
				row.createCell(11).setCellValue(checkBigdecimal(nonResident.getDividendDeclaredLastPayout()));
				row.createCell(12).setCellValue(checkBigdecimalWithString(nonResident.getTdsRateLastPayout()));
			}

			row.createCell(13).setCellValue(nonResident.getIsExempt()==false?"No":"Yes");
			row.createCell(14).setCellValue(checkBigdecimal(nonResident.getDividendAmountRs()));
			if (!Objects.isNull(nonResident.getWithholdingDetails())) {
				TreatySummary treatySummary = nonResident.getWithholdingDetails().getTreatySummary();
				ActSummary actSummary = nonResident.getWithholdingDetails().getActSummary();
				if (!Objects.isNull(treatySummary) && !Objects.isNull(actSummary)) {
					if (treatySummary.getRateType().equals(WithholdingRateType.RATE_AS_PER_TREATY)) {
						row.createCell(15)
								.setCellValue(checkBoolean(nonResident.getWithholdingDetails().isTreatyApplied()));
						row.createCell(16).setCellValue(checkBigdecimalWithString(treatySummary.getRate()));
						row.createCell(17).setCellValue(
								StringUtils.isBlank(treatySummary.getTaxTreatyClause()) ? StringUtils.EMPTY
										: treatySummary.getTaxTreatyClause());
						row.createCell(22).setCellValue(checkBigdecimalWithString(treatySummary.getRate()));
					}
					if (treatySummary.getRateType().equals(WithholdingRateType.RATE_AS_PER_ACT)) {
						row.createCell(16).setCellValue(checkBigdecimalWithString(actSummary.getActRate()));
						row.createCell(17)
								.setCellValue(StringUtils.isBlank(nonResident.getTdsSection()) ? StringUtils.EMPTY
										: nonResident.getTdsSection());
						row.createCell(22).setCellValue(checkBigdecimalWithString(actSummary.getActRate()));
					}
				}
				if (!Objects.isNull(treatySummary) && Objects.isNull(actSummary)) {
					row.createCell(15)
							.setCellValue(checkBoolean(nonResident.getWithholdingDetails().isTreatyApplied()));
					row.createCell(16).setCellValue(checkBigdecimalWithString(treatySummary.getRate()));
					row.createCell(17)
							.setCellValue(StringUtils.isBlank(treatySummary.getTaxTreatyClause()) ? StringUtils.EMPTY
									: treatySummary.getTaxTreatyClause());
					row.createCell(22).setCellValue(checkBigdecimalWithString(treatySummary.getRate()));
				} else {
					row.createCell(15).setCellValue("No");
				}
				if (!Objects.isNull(actSummary) && Objects.isNull(treatySummary)) {
					row.createCell(16).setCellValue(checkBigdecimalWithString(actSummary.getActRate()));
					row.createCell(17).setCellValue(StringUtils.isBlank(nonResident.getTdsSection()) ? StringUtils.EMPTY
							: nonResident.getTdsSection());
					row.createCell(22).setCellValue(checkBigdecimalWithString(actSummary.getActRate()));
				}
				LDCAOSummary ldcaoSummary = nonResident.getWithholdingDetails().getLdcSummary();
				if (!Objects.isNull(ldcaoSummary)) {
					row.createCell(18).setCellValue("Yes");
					row.createCell(19).setCellValue(checkBigdecimalWithString(ldcaoSummary.getRate()));
				} else {
					row.createCell(18).setCellValue("No");
					row.createCell(19).setCellValue("NA");
				}
			}
			// row.createCell(20).setCellValue(checkBigdecimal(nonResident.getClientOverriddenRate()));
			
			row.createCell(20).setCellValue("");//Specified person
			row.createCell(21).setCellValue("");//Higher rate applicable
			BigDecimal totalWithHolding = nonResident.getTotalWithholding() == null ? BigDecimal.ZERO
					: nonResident.getTotalWithholding();
			BigDecimal cessWithholding = nonResident.getCessWithholding() == null ? BigDecimal.ZERO
					: nonResident.getCessWithholding();
			BigDecimal surchargeWithholding = nonResident.getSurchargeWithholding() == null ? BigDecimal.ZERO
					: nonResident.getSurchargeWithholding();
			row.createCell(23).setCellValue(
					checkBigdecimal(totalWithHolding.subtract(cessWithholding).subtract(surchargeWithholding)));
			row.createCell(24).setCellValue(checkBigdecimalWithString(nonResident.getSurchargeRate()));
			row.createCell(25).setCellValue(checkBigdecimal(nonResident.getSurchargeWithholding()));
			row.createCell(26).setCellValue(checkBigdecimalWithString(nonResident.getCessRate()));
			row.createCell(27).setCellValue(checkBigdecimal(nonResident.getCessWithholding()));
			row.createCell(28).setCellValue(checkBigdecimalWithString(nonResident.getTdsRate()));
			row.createCell(29).setCellValue(checkBigdecimal(nonResident.getFinalDividendWithholding()));
			if(nonResident.getClientOverriddenRate()!=null && nonResident.getClientOverriddenRate().compareTo(BigDecimal.ZERO)!=0) {
				row.createCell(28).setCellValue(checkBigdecimalWithString(nonResident.getClientOverriddenRate()));
			}
			if(nonResident.getClientOverriddenWithholding()!=null && nonResident.getClientOverriddenWithholding().compareTo(BigDecimal.ZERO)!=0) {
				row.createCell(29).setCellValue(checkBigdecimal(nonResident.getClientOverriddenWithholding()));
			}
			row.createCell(30).setCellValue(checkString(nonResident.getForm15CACBApplicable()));
			row.createCell(31).setCellValue(checkBoolean(nonResident.getTrcAvailable()));
			row.createCell(32).setCellValue(checkBoolean(nonResident.getTenfAvailable()));
			row.createCell(33).setCellValue(checkString(nonResident.getIsNoPeDeclarationAvailable()));
			row.createCell(34).setCellValue(checkBoolean(nonResident.getIsNoPoemDeclarationAvailable()));
			row.createCell(35).setCellValue(checkString(nonResident.getIsMliSlobSatisfactionDeclarationAvailable()));
			row.createCell(36).setCellValue(checkBoolean(nonResident.getBeneficialOwnerOfIncome()));
			row.createCell(37)
					.setCellValue(checkBoolean(nonResident.getIsCommercialIndemnityOrTreatyBenefitsWithoutDocuments()));
		}
	}

	private String checkString(String value) {
		if (Objects.isNull(value)) {
			return "No";
		}
		if (value.equalsIgnoreCase("true")) {
			return "Yes";
		}
		if (value.equalsIgnoreCase("false")) {
			return "No";
		}
		if (value.equalsIgnoreCase("1")) {
			return "Yes";
		}
		if (value.equalsIgnoreCase("0")) {
			return "No";
		}
		return value;
	}

	private XSSFCellStyle createStyle(DefaultIndexedColorMap defaultIndexedColorMap,XSSFWorkbook workbook,int r, int g,int b) {
		XSSFCellStyle style=(XSSFCellStyle) workbook.createCellStyle();
		XSSFFont font=workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		style.setWrapText(true);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillForegroundColor(new XSSFColor(new java.awt.Color(r, g, b), defaultIndexedColorMap));
		return style;
	}
}
