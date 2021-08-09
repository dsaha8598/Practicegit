package com.ey.in.tds.ingestion.service.ao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.BorderType;
import com.aspose.cells.Cell;
import com.aspose.cells.CellBorderType;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tcs.common.domain.dividend.InvoiceShareholderNonResident;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.AoMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.AoUtilizationDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.dto.ao.AOMatrixDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.model.provision.UtilizationFileCommonDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.AOUtilizationFileDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.AoMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.AoUtilization;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.jdbc.dao.AdvanceDAO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceLineItemDAO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceShareholderNonResidentDAO;
import com.ey.in.tds.ingestion.jdbc.dao.ProvisionDAO;
import com.ey.in.tds.ingestion.service.ldc.LdcUtilizationService;
import com.ey.in.tds.ingestion.service.tdsmismatch.TdsMismatchService;
import com.microsoft.azure.storage.StorageException;

import bsh.ParseException;

@Service
public class AOUtilizationService extends TdsMismatchService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LdcUtilizationService ldcUtilizationService;

	@Autowired
	private AoUtilizationDAO aoUtilizationDAO;

	@Autowired
	private AoMasterDAO aoMasterDAO;

	@Autowired
	private InvoiceLineItemDAO invoiceLineItemDAO;

	@Autowired
	private AdvanceDAO advanceDAO;

	@Autowired
	private ProvisionDAO provisionDAO;

	@Autowired
	private InvoiceShareholderNonResidentDAO invoiceShareholderNonResidentDAO;

	@Autowired
	private OnboardingClient onboardingClient;

	@Value("${page_size}")
	protected int pageSize;

	/**
	 * calculating ao matrix values
	 * 
	 * @param deductorTan
	 * @return
	 * @throws ParseException
	 */
	public Map<Integer, Object> getAOMatrix(String deductorTan, int year, String certificateNumber) {
		Map<Integer, Object> aoMatrixValues = new HashMap<>();
		List<AoMaster> aoMasterOpeningOptional = aoMasterDAO.findByCertificateNumber(deductorTan, certificateNumber);

		if (!aoMasterOpeningOptional.isEmpty()) {
			AoMaster aoMaster = aoMasterOpeningOptional.get(0);

			BigDecimal finalOpeningAmount = new BigDecimal(0);

			for (int month = 4; month <= 12; month++) {

				finalOpeningAmount = matrixCaluclationByMonth(deductorTan, aoMatrixValues, year, finalOpeningAmount,
						month, aoMaster);
			}
			for (int month = 1; month <= 3; month++) {

				finalOpeningAmount = matrixCaluclationByMonth(deductorTan, aoMatrixValues, year, finalOpeningAmount,
						month, aoMaster);
			}
		}
		return aoMatrixValues;
	}

	private BigDecimal matrixCaluclationByMonth(String deductorTan, Map<Integer, Object> aoMatrixValues, int yearFromUI,
			BigDecimal previousMonthClosingAmount, int month, AoMaster aoMaster) {
		BigDecimal ftmAmount = BigDecimal.valueOf(0);
		BigDecimal sumOfAoAdjustmentAmount = BigDecimal.valueOf(0);
		BigDecimal closingAmount = BigDecimal.valueOf(0);
		if (aoMaster != null) {
			Date processingMonth = CommonUtil.getMonthStartDate(yearFromUI, month - 1);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(processingMonth);
			if (aoMaster.getApplicableFrom() != null && aoMaster.getApplicableTo() != null) {
				// Months logic based on applicable from and to date
				Date monthStartDate = CommonUtil.getMonthStartDate(aoMaster.getApplicableFrom());
				Date monthEndDate = CommonUtil.getMonthEndDate(aoMaster.getApplicableTo());

				logger.info("{} -- {} -- {}", monthStartDate.toString(), processingMonth.toString(),
						monthEndDate.toString());

				if (processingMonth.getTime() >= monthStartDate.getTime()
						&& processingMonth.getTime() <= monthEndDate.getTime()) {
					if (previousMonthClosingAmount.equals(BigDecimal.valueOf(0))) {
						Double previousYearUltilization = aoUtilizationDAO.getAoAmountUtilizedUntill(deductorTan,
								yearFromUI, aoMaster.getAoMasterId());

						ftmAmount = aoMaster.getAmount()
								.subtract(BigDecimal.valueOf(previousYearUltilization.doubleValue()));
					} else {
						ftmAmount = previousMonthClosingAmount;
					}
					sumOfAoAdjustmentAmount = BigDecimal.valueOf(aoUtilizationDAO
							.getTotalOpeningAmountForMonth(deductorTan, yearFromUI, month, aoMaster.getAoMasterId()));

					closingAmount = (ftmAmount).subtract(sumOfAoAdjustmentAmount);

				}

			} else if (aoMaster.getApplicableFrom() != null) {
				// Months logic based on applicable from and to date
				Date monthStartDate = CommonUtil.getMonthStartDate(aoMaster.getApplicableFrom());
				if (processingMonth.getTime() >= monthStartDate.getTime()) {
					if (previousMonthClosingAmount.equals(BigDecimal.valueOf(0))) {
						BigDecimal previousYearUltilization = BigDecimal.valueOf(aoUtilizationDAO
								.getAoAmountUtilizedUntill(deductorTan, yearFromUI, aoMaster.getAoMasterId()));
						ftmAmount = aoMaster.getAmount().subtract(previousYearUltilization);
					} else {
						ftmAmount = previousMonthClosingAmount;
					}
					sumOfAoAdjustmentAmount = BigDecimal.valueOf(aoUtilizationDAO
							.getTotalOpeningAmountForMonth(deductorTan, yearFromUI, month, aoMaster.getAoMasterId()));

					closingAmount = (ftmAmount).subtract(sumOfAoAdjustmentAmount);
				}
			}
		}

		AOMatrixDTO aoMatrixDTO = new AOMatrixDTO();
		aoMatrixDTO.setOpeningAmount(ftmAmount);
		aoMatrixDTO.setAdjustmentAmount(sumOfAoAdjustmentAmount);
		aoMatrixDTO.setClosingAmount(closingAmount);
		aoMatrixValues.put(month, aoMatrixDTO);
		return closingAmount;
	}

	@Async
	public ByteArrayInputStream asyncAoMatrixAdjustedFileDownload(String deductorTan, Integer assessmentYear,
			Integer month, String certificateNumber, String tenantId, String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return aoMatrixAdjustedFileDownload(deductorTan, assessmentYear, month, certificateNumber, tenantId, userName);
	}

	public ByteArrayInputStream aoMatrixAdjustedFileDownload(String deductorTan, int assessmentYear, int month,
			String certificateNumber, String tenantId, String userName) throws Exception {
		logger.info("Ao matrix adjusted download request for Year : {} Month : {}", assessmentYear, month);
		String fileName = "Adjusted_AO_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, assessmentYear, null, 0L,
				UploadTypes.AO_ADJUSTED_REPORT.name(), "Processing", month, userName, null, fileName);

		List<AoMaster> aoMasterOpeningOptional = aoMasterDAO.findByCertificateNumber(deductorTan, certificateNumber);
		List<AoUtilization> aoAdjustments = new ArrayList<>();
		String[] aoMatrixheaderNames = null;

		if (!aoMasterOpeningOptional.isEmpty()) {
			aoAdjustments = aoUtilizationDAO.getAoAdjustment(Arrays.asList(deductorTan), assessmentYear, month,
					aoMasterOpeningOptional.get(0).getAoMasterId());

		}
		if (!aoMasterOpeningOptional.isEmpty() && aoMasterOpeningOptional.get(0).getDividendProcessing() != null
				&& aoMasterOpeningOptional.get(0).getDividendProcessing() == true) {
			aoMatrixheaderNames = new String[] { "Deductor Master Tan", "AO Master Pan", "Utilized Amount",
					"Source File Name", "Company Code", "Name Of The Company Code", "Deductor TAN", "Deductor GSTIN",
					"ShareHolder PAN", "ShareHolder TIN", "Name of the ShareHolder", "ShareHolder Address",
					"Vendor Invoice Number", "Miro Number", "Migo Number", "Document Type", "Document Date",
					"Posting Date of Document", "Line Item Number", "HSN/SAC", "Sac Description",
					"Service Description - Invoice", "Service Description - PO", "Service Description - GL Text",
					"Taxable value", "IGST Rate", "IGST Amount", "CGST Rate", "CGST Amount", "SGST Rate", "SGST Amount",
					"Cess Rate", "Cess Amount", "Creditable (Y/N)", "POS", "TDS Section", "TDS Rate", "TDS Amount",
					"PO number", "PO date", "Linked advance Number", "Grossing up Indicator",
					"Original Document Number", "Original Document Date", "User Defined Field 1",
					"User Defined Field 2", "User Defined Field 3" };
		} else {
			aoMatrixheaderNames = new String[] { "Deductor Master Tan", "AO Master Pan", "Utilized Amount",
					"Source File Name", "Company Code", "Name Of The Company Code", "Deductor TAN", "Deductor GSTIN",
					"Deductee PAN", "Deductee TIN", "Name of the Deductee", "Deductee Address", "Vendor Invoice Number",
					"Miro Number", "Migo Number", "Document Type", "Document Date", "Posting Date of Document",
					"Line Item Number", "HSN/SAC", "Sac Description", "Service Description - Invoice",
					"Service Description - PO", "Service Description - GL Text", "Taxable value", "IGST Rate",
					"IGST Amount", "CGST Rate", "CGST Amount", "SGST Rate", "SGST Amount", "Cess Rate", "Cess Amount",
					"Creditable (Y/N)", "POS", "TDS Section", "TDS Rate", "TDS Amount", "PO number", "PO date",
					"Linked advance Number", "Grossing up Indicator", "Original Document Number",
					"Original Document Date", "User Defined Field 1", "User Defined Field 2", "User Defined Field 3" };
		}

		ArrayList<UtilizationFileCommonDTO> listutilizationFileCommonDTO = new ArrayList<>();

		for (AoUtilization aoUtilization : aoAdjustments) {

			UtilizationFileCommonDTO utilizationFileCommonDTO = new UtilizationFileCommonDTO();

			List<InvoiceLineItem> invoiceLineItem = null;
			List<ProvisionDTO> provison = null;
			List<AdvanceDTO> advance = null;
			List<InvoiceShareholderNonResident> invoiceShareholderNonResident = null;

			utilizationFileCommonDTO.setDeductorMasterTan(aoUtilization.getDeductorMasterTan());
			utilizationFileCommonDTO.setMasterPan(aoUtilization.getAoMasterPan());
			if (aoUtilization.getRemainingAmount() != null) {
				utilizationFileCommonDTO.setRemainingAmount(BigDecimal.valueOf(aoUtilization.getRemainingAmount()));
			} else {
				utilizationFileCommonDTO.setRemainingAmount(BigDecimal.valueOf(0));
			}
			utilizationFileCommonDTO.setUtilizedAmount(BigDecimal.valueOf(aoUtilization.getUtilizedAmount()));

			// for dividend AO
			if (!aoMasterOpeningOptional.isEmpty() && aoMasterOpeningOptional.get(0).getDividendProcessing() != null
					&& aoMasterOpeningOptional.get(0).getDividendProcessing() == true) {
				invoiceShareholderNonResident = invoiceShareholderNonResidentDAO.findByYearTanId(assessmentYear,
						deductorTan, aoUtilization.getInvoiceShareholderNonResidentId());
				if (invoiceShareholderNonResident != null && !invoiceShareholderNonResident.isEmpty()) {
					utilizationFileCommonDTO.setInvoiceShareholderNonResident(invoiceShareholderNonResident.get(0));
					utilizationFileCommonDTO.setType(UploadTypes.INVOICE_SHARE_HOLDER_NON_RESIDENT.name());
				}

			} else {// for non dividend AO
				if (UploadTypes.INVOICE.name().equalsIgnoreCase(aoUtilization.getConsumedFrom())) {
					invoiceLineItem = invoiceLineItemDAO.findByLineItemId(assessmentYear, deductorTan,
							aoUtilization.getInvoiceLineItemId());
				} else if (UploadTypes.ADVANCE.name().equalsIgnoreCase(aoUtilization.getConsumedFrom())) {
					advance = advanceDAO.findByYearTanDocumentPostingDateId(assessmentYear, deductorTan,
							aoUtilization.getAdvanceId());
				} else if (UploadTypes.PROVISION.name().equalsIgnoreCase(aoUtilization.getConsumedFrom())) {
					provison = provisionDAO.findByYearTanId(assessmentYear, deductorTan,
							aoUtilization.getProvisionId());
				}

				if (invoiceLineItem != null && !invoiceLineItem.isEmpty()) {
					utilizationFileCommonDTO.setInvoice(invoiceLineItem.get(0));
					utilizationFileCommonDTO.setType(UploadTypes.INVOICE.name());
				}
				if (advance != null && !advance.isEmpty()) {
					utilizationFileCommonDTO.setAdvance(advance.get(0));
					utilizationFileCommonDTO.setType(UploadTypes.ADVANCE.name());
				}
				if (provison != null && !provison.isEmpty()) {
					utilizationFileCommonDTO.setProvision(provison.get(0));
					utilizationFileCommonDTO.setType(UploadTypes.PROVISION.name());
				}
			}

			listutilizationFileCommonDTO.add(utilizationFileCommonDTO);
		}

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Ao Matrix Adjustments");
		worksheet.freezePanes(0, 4, 0, 4);
		worksheet.autoFitColumns();
		worksheet.autoFitRows();

		worksheet.getCells().importArray(aoMatrixheaderNames, 0, 0, false);

		ldcUtilizationService.setLdcMatrixHeaders(listutilizationFileCommonDTO, worksheet);

		Cell a1 = worksheet.getCells().get("A1");
		Style style1 = a1.getStyle();
		style1.setForegroundColor(Color.fromArgb(189, 215, 238));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		a1.setStyle(style1);

		Cell a2 = worksheet.getCells().get("B1");
		Style style2 = a2.getStyle();
		style2.setForegroundColor(Color.fromArgb(189, 215, 238));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		a2.setStyle(style2);

		Cell a3 = worksheet.getCells().get("C1");
		Style style3 = a3.getStyle();
		style3.setForegroundColor(Color.fromArgb(255, 255, 0));
		style3.setPattern(BackgroundType.SOLID);
		style3.getFont().setBold(true);
		a3.setStyle(style3);

		Cell a4 = worksheet.getCells().get("D1");
		Style style4 = a4.getStyle();
		style4.setForegroundColor(Color.fromArgb(255, 230, 153));
		style4.setPattern(BackgroundType.SOLID);
		style4.getFont().setBold(true);
		a4.setStyle(style4);

		worksheet.autoFitColumns();

		// Style for E1 to BC1 headers
		Style style5 = workbook.createStyle();
		style5.setForegroundColor(Color.fromArgb(91, 155, 213));
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(true);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("E1:AV1");
		headerColorRange1.setStyle(style5);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:AV1");

		workbook.save(out, SaveFormat.XLSX);
		saveMatrixReport(deductorTan, tenantId, assessmentYear, out, 1L, UploadTypes.AO_ADJUSTED_REPORT.name(),
				"Processed", month, userName, batchUpload.getBatchUploadID(), null);
		return new ByteArrayInputStream(out.toByteArray());
	}

	@Async
	public ByteArrayInputStream asyncAoMatrixClosingAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, String certificateNumber, String type, String tenantId, String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return aoMatrixClosingAmountDownload(deductorTan, assessmentYear, month, certificateNumber, type, tenantId,
				userName);
	}

	public ByteArrayInputStream aoMatrixClosingAmountDownload(String deductorTan, int assessmentYear, int month,
			String certificateNumber, String type, String tenantId, String userName) throws Exception {
		String fileName = "";
		if (UploadTypes.AO_OPENING_REPORT.name().equalsIgnoreCase(type)) {
			logger.info("AO matrix opening report download request for Year : {} Month : {}", assessmentYear, month);
			fileName = "AO_opening_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		} else {
			logger.info("Ao matrix opening report download request for Year : {} Month : {}", assessmentYear, month);
			fileName = "AO_closing_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, assessmentYear, null, 0L, type, "Processing",
				month, userName, null, fileName);

		Map<Integer, Object> aoMatrixValues = getAOMatrix(deductorTan, assessmentYear, certificateNumber);
		AOMatrixDTO matrixDTO = new AOMatrixDTO();
		if (aoMatrixValues.get(month) != null) {
			matrixDTO = (AOMatrixDTO) aoMatrixValues.get(month);
		}

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("AO Matrix");

		String[] headerNames = new String[] { "Deductor Tan", "Opening Amount", "Adjusted Amount", "Closing Amount" };

		worksheet.getCells().importArray(headerNames, 0, 0, false);
		worksheet.autoFitColumns();
		worksheet.autoFitRows();

		List<Object> rowData = new ArrayList<>();
		rowData.add(StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
		rowData.add(matrixDTO.getOpeningAmount() == null ? StringUtils.EMPTY : matrixDTO.getOpeningAmount());
		rowData.add(matrixDTO.getAdjustmentAmount() == null ? StringUtils.EMPTY : matrixDTO.getAdjustmentAmount());
		rowData.add(matrixDTO.getClosingAmount() == null ? StringUtils.EMPTY : matrixDTO.getClosingAmount());
		worksheet.getCells().importArrayList((ArrayList<Object>) rowData, 1, 0, false);

		Cell a1 = worksheet.getCells().get("A1");
		Style style1 = a1.getStyle();
		style1.setForegroundColor(Color.fromArgb(189, 215, 238));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		a1.setStyle(style1);

		Cell a2 = worksheet.getCells().get("B1");
		Style style2 = a2.getStyle();
		style2.setForegroundColor(Color.fromArgb(189, 215, 238));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		a2.setStyle(style2);

		Cell a3 = worksheet.getCells().get("C1");
		Style style3 = a3.getStyle();
		style3.setForegroundColor(Color.fromArgb(255, 255, 0));
		style3.setPattern(BackgroundType.SOLID);
		style3.getFont().setBold(true);
		a3.setStyle(style3);

		Cell a4 = worksheet.getCells().get("D1");
		Style style4 = a4.getStyle();
		style4.setForegroundColor(Color.fromArgb(255, 230, 153));
		style4.setPattern(BackgroundType.SOLID);
		style4.getFont().setBold(true);
		a4.setStyle(style4);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:D1");

		workbook.save(out, SaveFormat.XLSX);
		saveMatrixReport(deductorTan, tenantId, assessmentYear, out, 1L, type, "Processed", month, userName,
				batchUpload.getBatchUploadID(), null);
		return new ByteArrayInputStream(out.toByteArray());
	}

	@Async
	public void generateAOUtilizationFileAsync(Integer aoId, String tenantId, String tanNumber, String userName,
			String collectorPan) throws Exception {
		logger.info("Service Method executing , Record saving in Data Base {}");
		MultiTenantContext.setTenantId(tenantId);

		Integer year = CommonUtil.getAssessmentYear(null);
		BatchUpload batchUpload = new BatchUpload();
		String[] headers;
		Integer isDividend = null;
		List<AOUtilizationFileDTO> list = null;
		Workbook workbook = null;
		Worksheet worksheet = null;
		ImportTableOptions tableOptions = null;

		batchUpload = saveLDCUtiliationReportInBatchUpload(null, "AO_UTILIZATION_REPORT", "Processing", tenantId, 0L,
				userName, null, tanNumber, year);
		logger.info("BatchUpload record saved with status as a Processing {}");

		workbook = new Workbook();
		worksheet = workbook.getWorksheets().get(0);
		worksheet.setGridlinesVisible(false);
		worksheet.autoFitColumns();

		tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		Style style0 = workbook.createStyle();
		style0.setForegroundColor(Color.fromArgb(155, 187, 89));
		style0.setPattern(BackgroundType.SOLID);
		style0.getFont().setBold(true);
		style0.getFont().setSize(12);
		style0.getFont().setColor(Color.fromArgb(250, 251, 252));
		style0.setHorizontalAlignment(TextAlignmentType.CENTER);
		setBorderToCell(style0);
		style0.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getWhite());

		Style style1 = workbook.createStyle();
		style1.setPattern(BackgroundType.SOLID);
		style1.setHorizontalAlignment(TextAlignmentType.LEFT);
		style1.setForegroundColor(Color.fromArgb(234, 241, 221));
		setBorderToCell(style1);

		Style style2 = workbook.createStyle();
		style2.setPattern(BackgroundType.SOLID);
		style2.setHorizontalAlignment(TextAlignmentType.LEFT);
		style2.getFont().setBold(true);
		style2.getFont().setColor(Color.fromArgb(112, 153, 32));
		style2.setForegroundColor(Color.fromArgb(250, 251, 252));
		style2.getFont().setSize(13);
		// setBorderToCell(style2);

		Range headerColorRange1 = worksheet.getCells().createRange("A5:S5");
		headerColorRange1.setStyle(style0);

		worksheet.setName("ao_utiliation_report");

		String msg = getHeaderReportMsg(tanNumber, tenantId, collectorPan);
		worksheet.getCells().get("A1").setValue(msg);
		worksheet.getCells().merge(0, 0, 4, 5);
		headerColorRange1 = worksheet.getCells().createRange("A1:E1");
		headerColorRange1.setStyle(style2);

		isDividend = aoMasterDAO.isDividendAO(aoId);
		list = aoUtilizationDAO.getLdcUtilizationRecordsById(aoId, isDividend);
		logger.info("DAO execution sucessfull and no of records is " + list.size() + " {}");

		if (isDividend == 1) {
			logger.info("AO is having dividend {}");
			headers = new String[] { "Certificate No", "Assesment Year", "Assesment Month", "AO Rate", "AO Section",
					"AO Master Total Amount", "AO Applicable From", "AO Applicable To", " Document Number",
					"Date Of Posting", "Line Item Number", "Client Overriden Rate", "Final TDS Section",
					"Final Total Withholding", "Applicable Total Taxable Amount", "Utilized Amount", "Remaining Amount",
					"Initial Utilized Amount", "Consumed From" };

		} else {
			logger.info("AO is having not dividend {}");
			headers = new String[] { "Certificate No", "Assesment Year", "Assesment Month", "AO Rate", "AO Section",
					"AO Master Total Amount", "AO Applicable From", "AO Applicable To", "Document Number",
					"Document Date", "Line Item Number", "Final TDS Rate", "Final TDS Section", "Final TDS Amount",
					"Applicable Total Taxable Amount", "Utilized Amount", "Remaining Amount", "Initial Utilized Amount",
					"Consumed From" };
		}
		worksheet.getCells().importArray(headers, 4, 0, false);

		int rowIndex = 5;
		for (AOUtilizationFileDTO data : list) {

			// for sheet coloring
			style1.setForegroundColor(
					rowIndex % 2 == 0 ? Color.fromArgb(215, 228, 188) : Color.fromArgb(234, 241, 221));

			List<Object> rowData = new ArrayList<>();
			// Certificate No
			rowData.add(
					StringUtils.isEmpty(data.getCertificateNumber()) ? StringUtils.EMPTY : data.getCertificateNumber());
			// Assesment Year
			rowData.add(data.getAssesmentYear() == null ? StringUtils.EMPTY : data.getAssesmentYear().toString());
			// Assesment Month
			rowData.add(data.getChallanMonth() == null ? StringUtils.EMPTY : data.getChallanMonth().toString());
			// LDC Rate
			rowData.add(data.getAoRate() == null ? "0.0" : data.getAoRate());
			// LDC Section","
			rowData.add(StringUtils.isEmpty(data.getAoSection()) ? StringUtils.EMPTY : data.getAoSection());
			// LDC Master Total Amount
			rowData.add(data.getAoMasterTotalAmount() == null ? "0.0" : data.getAoMasterTotalAmount().toString());
			// LDC Applicable From",
			rowData.add(data.getAoApplicableFrom() == null ? StringUtils.EMPTY : data.getAoApplicableFrom().toString());
			// "LCC Applicable To","
			rowData.add(data.getAoApplicableTo() == null ? StringUtils.EMPTY : data.getAoApplicableTo().toString());
			// Document Number",""
			rowData.add(StringUtils.isEmpty(data.getDocumentNumber()) ? StringUtils.EMPTY : data.getDocumentNumber());
			// Document Date
			rowData.add(data.getDocumentDate() == null ? StringUtils.EMPTY : data.getDocumentDate().toString());
			// Line Item Number","",
			rowData.add(StringUtils.isEmpty(data.getLineItemNumber()) ? StringUtils.EMPTY : data.getLineItemNumber());
			// Final TDS Rate
			rowData.add(data.getFinalTdsRate() == null ? "0.0" : data.getFinalTdsRate().toString());
			// Final TDS Section
			rowData.add(
					StringUtils.isEmpty(data.getFinalTdscSection()) ? StringUtils.EMPTY : data.getFinalTdscSection());
			// ","Final TDS Amount
			rowData.add(data.getFinalTdsAmount() == null ? "0.0"
					: data.getFinalTdsAmount().setScale(2, BigDecimal.ROUND_HALF_DOWN).toString());
			// ","Applicable Total Taxable Amount"
			rowData.add(data.getApplicableTotalTaxableAmount() == null ? "0.0"
					: data.getApplicableTotalTaxableAmount().toString());
			// Utilized Amount",""
			rowData.add(data.getUtilizedAmount() == null ? "0.0" : data.getUtilizedAmount().toString());
			// Remaining Amount
			rowData.add(data.getRemainingAmount() == null ? StringUtils.EMPTY : data.getRemainingAmount().toString());
			// initial utilized amount
			rowData.add(data.getInitialUtilizedAmount() == null ? StringUtils.EMPTY
					: data.getInitialUtilizedAmount().toString());
			// consumed from
			rowData.add(data.getConsumedFrom() == null ? StringUtils.EMPTY : data.getConsumedFrom().toString());

			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
			worksheet.getCells().setStandardHeight(12);
			Range headerColorRange2 = worksheet.getCells().createRange("A" + rowIndex + ":" + "S" + rowIndex + "");
			headerColorRange2.setStyle(style1);
		}
		worksheet.autoFitColumns();
		worksheet.autoFitRows();

		File generatedExcelFile = new File("AO_Utilization_Report" + new Date().getTime() + ".xlsx");
		OutputStream out = new FileOutputStream(generatedExcelFile);
		workbook.save(out, SaveFormat.XLSX);

		saveLDCUtiliationReportInBatchUpload(batchUpload.getBatchUploadID(), "AO_UTILIZATION_REPORT", "Processed",
				tenantId, (long) list.size(), userName, generatedExcelFile, tanNumber, year);

	}

	/**
	 * method to save and update batch upload for utilization report
	 * 
	 * @param batchUploadId
	 * @param uploadType
	 * @param status
	 * @param tenantId
	 * @param rowCount
	 * @param userName
	 * @param file
	 * @param tan
	 * @param year
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 */
	public BatchUpload saveLDCUtiliationReportInBatchUpload(Integer batchUploadId, String uploadType, String status,
			String tenantId, Long rowCount, String userName, File file, String tan, Integer year)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		String fileName = "";
		String filePath = "";
		BatchUpload batchupload = null;
		if (file != null) {
			fileName = uploadType.toLowerCase() + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
			filePath = blob.uploadExcelToBlobWithFile(file, tenantId);
			logger.info(" AO Utilization report {} completed for : {}", uploadType, userName);
		} else {
			logger.info(" AO Utilization report {} started for : {}", uploadType, userName);
		}
		if (batchUploadId != null) {
			batchupload = batchUploadDAO.findByOnlyId(batchUploadId);
			batchupload.setProcessEndTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			batchupload.setModifiedBy(userName);
			batchupload.setModifiedDate(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			batchupload.setFileName(fileName);
			batchupload.setRowsCount(rowCount);
			batchupload.setStatus(status);
			batchupload.setFilePath(filePath);
			return batchUploadDAO.update(batchupload);
		} else {

			batchupload = new BatchUpload();
			batchupload.setDeductorMasterTan(tan);
			batchupload.setAssessmentYear(year);
			batchupload.setActive(true);
			batchupload.setStatus(status);
			batchupload.setCreatedBy(userName);
			batchupload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchupload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchupload.setUploadType(uploadType);
			return batchUploadDAO.save(batchupload);
		}
	}

	private Style setBorderToCell(Style style) {
		style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
		style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());
		return style;
	}

	public String getHeaderReportMsg(String collectorTan, String tenantId, String collectorPan) {
		DeductorMasterDTO deductorData = onboardingClient.getDeductorByPan(collectorPan, tenantId).getBody().getData();
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return "AO Utilization Report (Dated: " + date + ")\n Client Name: " + deductorData.getDeductorName() + "\n";
	}

}
