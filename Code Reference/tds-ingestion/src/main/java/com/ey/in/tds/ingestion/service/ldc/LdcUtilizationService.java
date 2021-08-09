package com.ey.in.tds.ingestion.service.ldc;

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
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.dividend.InvoiceShareholderResident;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.LDCUtilizationDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.LdcMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.model.provision.LDCMatrixDTO;
import com.ey.in.tds.common.model.provision.UtilizationFileCommonDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcUtilization;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcUtilizationDTO;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.jdbc.dao.AdvanceDAO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceLineItemDAO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceShareholderResidentDAO;
import com.ey.in.tds.ingestion.jdbc.dao.ProvisionDAO;
import com.ey.in.tds.ingestion.service.tdsmismatch.TdsMismatchService;
import com.microsoft.azure.storage.StorageException;

import bsh.ParseException;

@Service
public class LdcUtilizationService extends TdsMismatchService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LDCUtilizationDAO lDCUtilizationDAO;

	@Autowired
	private LdcMasterDAO ldcMasterDAO;

	@Autowired
	private OnboardingClient onboardingClient;

	@Autowired
	private InvoiceLineItemDAO invoiceLineItemDAO;

	@Autowired
	private AdvanceDAO advanceDAO;

	@Autowired
	private ProvisionDAO provisionDAO;

	@Autowired
	private InvoiceShareholderResidentDAO invoiceShareholderResidentDAO;

	@Value("${page_size}")
	protected int pageSize;

	// TODO NEED TO CHANGE FOR SQL
	/*
	 * public LdcUtilization create(LdcUtilization ldcUtilization) {
	 * ldcUtilization.getKey().setId(UUID.randomUUID()); return
	 * ldcUtilizationRepository.insert(ldcUtilization); }
	 * 
	 * public LdcUtilization get(LdcUtilization.Key key) { Optional<LdcUtilization>
	 * response = ldcUtilizationRepository.findById(key); if (response.isPresent())
	 * { return response.get(); } return null; }
	 * 
	 * public PagedData<LdcUtilization>
	 * getLdcUtilizationsByAssessmentYearMonthDeductorTan(int assessmentYear, int
	 * assessmentMonth, List<String> deductorTan, Pagination pagination) { return
	 * ldcUtilizationRepository.getLdcUtilizationsByAssessmentYearMonthDeductorTan(
	 * assessmentYear, assessmentMonth, deductorTan, pagination); }
	 * 
	 * public List<LdcUtilization> findByLdcMaster(String deductorTan, UUID
	 * ldcMasterid) { return ldcUtilizationRepository.findByLdcMaster(deductorTan,
	 * ldcMasterid); }
	 */

	/**
	 * calculating ldc matrix values
	 * 
	 * @param deductorTan
	 * @return
	 * @throws ParseException
	 */
	public Map<Integer, Object> getLDCMatrix(String deductorTan, int yearFromUI, String certificateNumber)
			throws ParseException {
		Map<Integer, Object> ldcMatrixValues = new HashMap<>();
		List<LdcMaster> ldcMasterOpeningOptional = onboardingClient
				.getValidLdcByCertificateNo(deductorTan, certificateNumber, MultiTenantContext.getTenantId()).getBody()
				.getData();
		logger.info("ldc Master data {}:", ldcMasterOpeningOptional);
		if (!ldcMasterOpeningOptional.isEmpty()) {
			LdcMaster ldcMaster = ldcMasterOpeningOptional.get(0);
			BigDecimal finalOpeningAmount = new BigDecimal(0);
			for (int month = 4; month <= 12; month++) {
				finalOpeningAmount = matrixCaluclationByMonth(deductorTan, ldcMatrixValues, yearFromUI,
						finalOpeningAmount, month, ldcMaster);
			}
			for (int month = 1; month <= 3; month++) {
				finalOpeningAmount = matrixCaluclationByMonth(deductorTan, ldcMatrixValues, yearFromUI,
						finalOpeningAmount, month, ldcMaster);
			}
		}
		// adding default vaues for no records
		if (ldcMatrixValues.isEmpty()) {
			for (int month = 1; month <= 12; month++) {
				LDCMatrixDTO ldcMatrixDTO = new LDCMatrixDTO();
				ldcMatrixDTO.setOpeningAmount(BigDecimal.ZERO);
				ldcMatrixDTO.setAdjustmentAmount(BigDecimal.ZERO);
				ldcMatrixDTO.setClosingAmount(BigDecimal.ZERO);
				ldcMatrixValues.put(month, ldcMatrixDTO);
			}
		}

		return ldcMatrixValues;
	}

	/**
	 * 
	 * @param deductorTan
	 * @param ldcMatrixValues
	 * @param yearFromUI
	 * @param previousMonthClosingAmount
	 * @param month
	 * @param ldcMaster
	 * @return
	 */
	private BigDecimal matrixCaluclationByMonth(String deductorTan, Map<Integer, Object> ldcMatrixValues,
			Integer yearFromUI, BigDecimal previousMonthClosingAmount, int month, LdcMaster ldcMaster) {

		BigDecimal ftmAmount = BigDecimal.valueOf(0);
		BigDecimal sumOfLdcAdjustmentAmount = BigDecimal.valueOf(0);
		BigDecimal closingAmount = BigDecimal.valueOf(0);
		if (ldcMaster != null) {
			Date processingMonth = CommonUtil.getMonthStartDate(yearFromUI, month - 1);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(processingMonth);
			if (ldcMaster.getApplicableFrom() != null && ldcMaster.getApplicableTo() != null) {
				// Months logic based on applicable from and to date
				Date monthStartDate = CommonUtil.getMonthStartDate(ldcMaster.getApplicableFrom());
				Date monthEndDate = CommonUtil.getMonthEndDate(ldcMaster.getApplicableTo());

				logger.info("{} -- {} -- {}", monthStartDate.toString(), processingMonth.toString(),
						monthEndDate.toString());

				if (processingMonth.getTime() >= monthStartDate.getTime()
						&& processingMonth.getTime() <= monthEndDate.getTime()) {
					if (previousMonthClosingAmount.equals(BigDecimal.valueOf(0))) {
						Double previousYearUltilization = lDCUtilizationDAO.getLdcAmountUtilizedUntill(deductorTan,
								yearFromUI, ldcMaster.getLdcMasterID());

						ftmAmount = ldcMaster.getAmount()
								.subtract(BigDecimal.valueOf(previousYearUltilization.doubleValue()));
					} else {
						ftmAmount = previousMonthClosingAmount;
					}
					sumOfLdcAdjustmentAmount = BigDecimal.valueOf(lDCUtilizationDAO
							.getTotalOpeningAmountForMonth(deductorTan, yearFromUI, month, ldcMaster.getLdcMasterID()));
					closingAmount = (ftmAmount).subtract(sumOfLdcAdjustmentAmount);
				}
			} else if (ldcMaster.getApplicableFrom() != null) {
				// Months logic based on applicable from and to date
				Date monthStartDate = CommonUtil.getMonthStartDate(ldcMaster.getApplicableFrom());
				if (processingMonth.getTime() >= monthStartDate.getTime()) {
					if (previousMonthClosingAmount.equals(BigDecimal.valueOf(0))) {
						BigDecimal previousYearUltilization = BigDecimal.valueOf(lDCUtilizationDAO
								.getLdcAmountUtilizedUntill(deductorTan, yearFromUI, ldcMaster.getLdcMasterID()));
						ftmAmount = ldcMaster.getAmount().subtract(previousYearUltilization);
					} else {
						ftmAmount = previousMonthClosingAmount;
					}
					sumOfLdcAdjustmentAmount = BigDecimal.valueOf(lDCUtilizationDAO
							.getTotalOpeningAmountForMonth(deductorTan, yearFromUI, month, ldcMaster.getLdcMasterID()));

					closingAmount = (ftmAmount).subtract(sumOfLdcAdjustmentAmount);
				}
			}
		}

		LDCMatrixDTO ldcMatrixDTO = new LDCMatrixDTO();
		ldcMatrixDTO.setOpeningAmount(ftmAmount);
		ldcMatrixDTO.setAdjustmentAmount(sumOfLdcAdjustmentAmount);
		ldcMatrixDTO.setClosingAmount(closingAmount);
		ldcMatrixValues.put(month, ldcMatrixDTO);
		return closingAmount;
	}

	/**
	 * 
	 * @param deductorTan
	 * @param assessmentYear
	 * @param month
	 * @param certificateNumber
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@Async
	public ByteArrayInputStream asyncLdcMatrixAdjustedFileDownload(String deductorTan, Integer assessmentYear,
			Integer month, String certificateNumber, String tenantId, String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return ldcMatrixAdjustedFileDownload(deductorTan, assessmentYear, month, certificateNumber, tenantId, userName);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param assessmentYear
	 * @param month
	 * @param certificateNumber
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public ByteArrayInputStream ldcMatrixAdjustedFileDownload(String deductorTan, int assessmentYear, int month,
			String certificateNumber, String tenantId, String userName) throws Exception {

		logger.info("Ldc matrix adjusted download request for Year : {} Month : {}", assessmentYear, month);
		String fileName = "Adjusted_Ldc_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, assessmentYear, null, 0L,
				UploadTypes.LDC_ADJUSTED_REPORT.name(), "Processing", month, userName, null, fileName);

		List<LdcMaster> ldcMasterOpeningOptional = ldcMasterDAO.getLdcBycertificateNoAndTan(deductorTan,
				certificateNumber);
		List<LdcUtilization> ldcAdjustments = new ArrayList<>();
		if (!ldcMasterOpeningOptional.isEmpty()) {
			ldcAdjustments = lDCUtilizationDAO.getLdcAdjustment(deductorTan, assessmentYear, month,
					ldcMasterOpeningOptional.get(0).getLdcMasterID());
		}
		String[] ldcMatrixheaderNames = null;
		if (!ldcMasterOpeningOptional.isEmpty() && ldcMasterOpeningOptional.get(0).getDividendProcessing() != null
				&& ldcMasterOpeningOptional.get(0).getDividendProcessing() == true) {
			ldcMatrixheaderNames = new String[] { "Deductor Master Tan", "LDC Master Pan", "Utilized Amount",
					"Source File Name", "Company Code", "Name Of The Company Code", "Deductor TAN", "Deductor GSTIN",
					"SareHolder PAN", "SareHolder TIN", "Name of the SareHolder", "SareHolder Address",
					"Vendor Invoice Number", "Miro Number", "Migo Number", "Document Type", "Document Date",
					"Posting Date of Document", "Line Item Number", "HSN/SAC", "Sac Description",
					"Service Description - Invoice", "Service Description - PO", "Service Description - GL Text",
					"Taxable value", "IGST Rate", "IGST Amount", "CGST Rate", "CGST Amount", "SGST Rate", "SGST Amount",
					"Cess Rate", "Cess Amount", "Creditable (Y/N)", "POS", "TDS Section", "TDS Rate", "TDS Amount",
					"PO number", "PO date", "Linked advance Number", "Grossing up Indicator",
					"Original Document Number", "Original Document Date", "User Defined Field 1",
					"User Defined Field 2", "User Defined Field 3" };
		} else {
			ldcMatrixheaderNames = new String[] { "Deductor Master Tan", "LDC Master Pan", "Utilized Amount",
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

		for (LdcUtilization ldcUtilization : ldcAdjustments) {
			UtilizationFileCommonDTO utilizationFileCommonDTO = new UtilizationFileCommonDTO();
			List<InvoiceLineItem> invoiceLineItem = null;
			List<ProvisionDTO> provison = null;
			List<AdvanceDTO> advance = null;
			List<InvoiceShareholderResident> invShareHolderRes = null;

			utilizationFileCommonDTO.setDeductorMasterTan(ldcUtilization.getDeductorMasterTan());
			utilizationFileCommonDTO.setMasterPan(ldcUtilization.getLdcMasterPan());
			utilizationFileCommonDTO.setUtilizedAmount(ldcUtilization.getUtilizedAmount());

			// checking for dividend LDC
			if (!ldcMasterOpeningOptional.isEmpty() && ldcMasterOpeningOptional.get(0).getDividendProcessing() != null
					&& ldcMasterOpeningOptional.get(0).getDividendProcessing() == true) {
				invShareHolderRes = invoiceShareholderResidentDAO.findByYearTanId(assessmentYear, deductorTan,
						ldcUtilization.getInvoiceShareHolderResidentId());

				if (invShareHolderRes != null && !invShareHolderRes.isEmpty()) {
					utilizationFileCommonDTO.setInvoiceShareholderResident(invShareHolderRes.get(0));
					utilizationFileCommonDTO.setType(UploadTypes.INVOICE_SHARE_HOLDER_RESIDENT.name());
				}
			} else {// non dividend LDC
				if (UploadTypes.INVOICE.name().equalsIgnoreCase(ldcUtilization.getConsumedFrom())) {
					invoiceLineItem = invoiceLineItemDAO.findByLineItemId(assessmentYear, deductorTan,
							ldcUtilization.getInvoiceLineItemId());
				} else if (UploadTypes.ADVANCE.name().equalsIgnoreCase(ldcUtilization.getConsumedFrom())) {
					advance = advanceDAO.findByYearTanDocumentPostingDateId(assessmentYear, deductorTan,
							ldcUtilization.getAdvanceId());
				} else if (UploadTypes.PROVISION.name().equalsIgnoreCase(ldcUtilization.getConsumedFrom())) {
					provison = provisionDAO.findByYearTanId(assessmentYear, deductorTan,
							ldcUtilization.getProvisionId());
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
		worksheet.setName("Ldc Matrix Adjustments");
		worksheet.freezePanes(0, 4, 0, 4);
		worksheet.autoFitColumns();
		worksheet.autoFitRows();

		worksheet.getCells().importArray(ldcMatrixheaderNames, 0, 0, false);

		setLdcMatrixHeaders(listutilizationFileCommonDTO, worksheet);

		// Style for A1 to B1 headers
		Cell a1 = worksheet.getCells().get("A1");
		Style style1 = a1.getStyle();
		style1.setForegroundColor(Color.fromArgb(189, 215, 238));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		Range headerColorRange = worksheet.getCells().createRange("A1:B1");
		headerColorRange.setStyle(style1);

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
		Range headerColorRange1 = worksheet.getCells().createRange("E1:AU1");
		headerColorRange1.setStyle(style5);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:AU1");

		workbook.save(out, SaveFormat.XLSX);
		saveMatrixReport(deductorTan, tenantId, assessmentYear, out, 1L, UploadTypes.LDC_ADJUSTED_REPORT.name(),
				"Processed", month, userName, batchUpload.getBatchUploadID(), null);
		return new ByteArrayInputStream(out.toByteArray());
	}

	public void setLdcMatrixHeaders(ArrayList<UtilizationFileCommonDTO> listutilizationFileCommonDTO,
			Worksheet worksheet) throws Exception {

		if (!listutilizationFileCommonDTO.isEmpty()) {
			int rowIndex = 1;
			BigDecimal adjustedSum = BigDecimal.valueOf(0);
			for (UtilizationFileCommonDTO utilizationFileCommonDTO : listutilizationFileCommonDTO) {
				List<Object> rowData = new ArrayList<>();
				setInvoiceDataForMatrixReports(utilizationFileCommonDTO, rowData);
				adjustedSum = adjustedSum.add(utilizationFileCommonDTO.getUtilizedAmount());
				worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
			}
			// Sum of Adjustement Amount and Utilization amount
			rowIndex = rowIndex + 2;
			List<Object> rowData = new ArrayList<>();
			rowData.add(StringUtils.EMPTY);
			rowData.add("Total");
			rowData.add(adjustedSum);
			worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
		}

	}

	/**
	 * 
	 * @param deductorTan
	 * @param assessmentYear
	 * @param month
	 * @param certificateNumber
	 * @param type
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@Async
	public ByteArrayInputStream asyncLdcMatrixClosingAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, String certificateNumber, String type, String tenantId, String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return ldcMatrixClosingAmountDownload(deductorTan, assessmentYear, month, certificateNumber, type, tenantId,
				userName);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param assessmentYear
	 * @param month
	 * @param certificateNumber
	 * @param type
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public ByteArrayInputStream ldcMatrixClosingAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, String certificateNumber, String type, String tenantId, String userName) throws Exception {
		String fileName = "";
		if (UploadTypes.LDC_OPENING_REPORT.name().equalsIgnoreCase(type)) {
			logger.info("Ldc matrix opening report download request for Year : {} Month : {}", assessmentYear, month);
			fileName = "Ldc_opening_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		} else {
			logger.info("Ldc matrix opening report download request for Year : {} Month : {}", assessmentYear, month);
			fileName = "Ldc_closing_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BatchUpload batchUpload = saveMatrixReport(deductorTan, tenantId, assessmentYear, null, 0L, type, "Processing",
				month, userName, null, fileName);
		Map<Integer, Object> ldcMatrixValues = getLDCMatrix(deductorTan, assessmentYear, certificateNumber);

		LDCMatrixDTO matrixDTO = (LDCMatrixDTO) ldcMatrixValues.get(month);

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("LDC Matrix");

		String[] headerNames = new String[] { "Deductor Tan", "Opening Amount", "Adjusted Amount", "Closing Amount" };

		worksheet.getCells().importArray(headerNames, 0, 0, false);
		worksheet.autoFitColumns();
		worksheet.autoFitRows();

		List<Object> rowData = new ArrayList<>();
		if (matrixDTO != null) {
			rowData.add(StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
			rowData.add(matrixDTO.getOpeningAmount() == null ? StringUtils.EMPTY : matrixDTO.getOpeningAmount());
			rowData.add(matrixDTO.getAdjustmentAmount() == null ? StringUtils.EMPTY : matrixDTO.getAdjustmentAmount());
			rowData.add(matrixDTO.getClosingAmount() == null ? StringUtils.EMPTY : matrixDTO.getClosingAmount());
		}
		worksheet.getCells().importArrayList((ArrayList<Object>) rowData, 1, 0, false);

		// Style for A1 to B1 headers
		Cell a1 = worksheet.getCells().get("A1");
		Style style1 = a1.getStyle();
		style1.setForegroundColor(Color.fromArgb(189, 215, 238));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		Range headerColorRange = worksheet.getCells().createRange("A1:B1");
		headerColorRange.setStyle(style1);

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

	public void setInvoiceDataForMatrixReports(UtilizationFileCommonDTO utilizationFileCommonDTO,
			List<Object> rowData) {
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getDeductorMasterTan()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getDeductorMasterTan());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getMasterPan()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getMasterPan());
		rowData.add(utilizationFileCommonDTO.getUtilizedAmount());
		if ("INVOICE".equalsIgnoreCase(utilizationFileCommonDTO.getType())) {
			setInvoiceLineItemDataForMatrixReports(utilizationFileCommonDTO, rowData);
		}
		if ("ADVANCE".equalsIgnoreCase(utilizationFileCommonDTO.getType())) {
			setAdvanceDataForMatrixReports(utilizationFileCommonDTO, rowData);
		}
		if ("PROVISION".equalsIgnoreCase(utilizationFileCommonDTO.getType())) {
			setProvisionDataForMatrixReports(utilizationFileCommonDTO, rowData);
		}
		if ("INVOICE_SHARE_HOLDER_RESIDENT".equalsIgnoreCase(utilizationFileCommonDTO.getType())
				&& utilizationFileCommonDTO.getInvoiceShareholderResident() != null) {
			setInvoiceShareHolderResidentDataForMatrixReports(utilizationFileCommonDTO, rowData);
		}
		if ("INVOICE_SHARE_HOLDER_NON_RESIDENT".equalsIgnoreCase(utilizationFileCommonDTO.getType())
				&& utilizationFileCommonDTO.getInvoiceShareholderNonResident() != null) {
			setInvoiceShareHolderNonResidentDataForMatrixReports(utilizationFileCommonDTO, rowData);
		}
	}

	// Invoice
	public void setInvoiceLineItemDataForMatrixReports(UtilizationFileCommonDTO utilizationFileCommonDTO,
			List<Object> rowData) {
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getSourceFileName()) ? StringUtils.EMPTY // Source
																														// File
																														// Name
				: utilizationFileCommonDTO.getInvoice().getSourceFileName());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getCompanyCode()) ? StringUtils.EMPTY // Company
																													// Code
				: utilizationFileCommonDTO.getInvoice().getCompanyCode());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getCompanyName()) ? StringUtils.EMPTY // Name
																													// Of
																													// The
																													// Company
																													// Code
				: utilizationFileCommonDTO.getInvoice().getCompanyName());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getDeductorMasterTan()) ? StringUtils.EMPTY // Deductor
																												// TAN
				: utilizationFileCommonDTO.getDeductorMasterTan());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getDeductorGstin()) ? StringUtils.EMPTY // Deductor
																														// GSTIN
				: utilizationFileCommonDTO.getInvoice().getDeductorGstin());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getPan()) ? StringUtils.EMPTY // Deductee
																											// PAN
				: utilizationFileCommonDTO.getInvoice().getPan());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getDeducteeTin()) ? StringUtils.EMPTY // Deductee
																													// TIN
				: utilizationFileCommonDTO.getInvoice().getDeducteeTin());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getDeducteeName()) ? StringUtils.EMPTY // Name
																														// of
																														// the
																														// Deductee
				: utilizationFileCommonDTO.getInvoice().getDeducteeName());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getDeducteeAddress()) ? StringUtils.EMPTY // Deductee
																														// Address
				: utilizationFileCommonDTO.getInvoice().getDeducteeAddress());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getVendorInvoiceNumber()) ? StringUtils.EMPTY // Vendor
																														// Invoice
																														// Number
						: utilizationFileCommonDTO.getInvoice().getVendorInvoiceNumber());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getMiroNumber()) ? StringUtils.EMPTY // Miro
																													// Number
				: utilizationFileCommonDTO.getInvoice().getMiroNumber());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getMigoNumber()) ? StringUtils.EMPTY // Migo
																													// Number
				: utilizationFileCommonDTO.getInvoice().getMigoNumber());

		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getDocumentType()) ? StringUtils.EMPTY // Document
																														// Type
				: utilizationFileCommonDTO.getInvoice().getDocumentType());

		rowData.add(utilizationFileCommonDTO.getInvoice().getDocumentDate() == null ? StringUtils.EMPTY // Document date
				: utilizationFileCommonDTO.getInvoice().getDocumentDate());

		rowData.add(( // Posting Date of Document
		utilizationFileCommonDTO.getInvoice().getDocumentPostingDate() == null) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getInvoice().getDocumentPostingDate());

		rowData.add(utilizationFileCommonDTO.getInvoice().getLineItemNumber() == null ? StringUtils.EMPTY // Line Item
																											// Number
				: utilizationFileCommonDTO.getInvoice().getLineItemNumber());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getHsnSacCode()) ? StringUtils.EMPTY // HSN/SAC
				: utilizationFileCommonDTO.getInvoice().getHsnSacCode());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getSacDecription()) ? StringUtils.EMPTY // Sac
																														// Description
				: utilizationFileCommonDTO.getInvoice().getSacDecription());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getServiceDescriptionInvoice())
				? StringUtils.EMPTY // Service Description - Invoice
				: utilizationFileCommonDTO.getInvoice().getServiceDescriptionInvoice());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getServiceDescriptionPo()) ? StringUtils.EMPTY // Service
																															// Description
																															// -
																															// PO
						: utilizationFileCommonDTO.getInvoice().getServiceDescriptionPo());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getServiceDescriptionGl()) ? StringUtils.EMPTY // Service
																															// Description
																															// -
																															// GL
																															// Text
						: utilizationFileCommonDTO.getInvoice().getServiceDescriptionGl());
		rowData.add(utilizationFileCommonDTO.getInvoice().getInvoiceAmount() == null ? StringUtils.EMPTY // Taxable
																											// value
				: utilizationFileCommonDTO.getInvoice().getInvoiceAmount());
		rowData.add(utilizationFileCommonDTO.getInvoice().getIgstRate() == null ? StringUtils.EMPTY // IGST Rate
				: utilizationFileCommonDTO.getInvoice().getIgstRate());
		rowData.add(utilizationFileCommonDTO.getInvoice().getIgstAmount() == null ? StringUtils.EMPTY // IGST Amount
				: utilizationFileCommonDTO.getInvoice().getIgstAmount());
		rowData.add(utilizationFileCommonDTO.getInvoice().getCgstRate() == null ? StringUtils.EMPTY // CGST Rate
				: utilizationFileCommonDTO.getInvoice().getCgstRate());
		rowData.add(utilizationFileCommonDTO.getInvoice().getCgstAmount() == null ? StringUtils.EMPTY // CGST Amount
				: utilizationFileCommonDTO.getInvoice().getCgstAmount());
		rowData.add(utilizationFileCommonDTO.getInvoice().getSgstRate() == null ? StringUtils.EMPTY // SGST Rate
				: utilizationFileCommonDTO.getInvoice().getSgstRate());
		rowData.add(utilizationFileCommonDTO.getInvoice().getSgstAmount() == null ? StringUtils.EMPTY // SGST Amount
				: utilizationFileCommonDTO.getInvoice().getSgstAmount());
		rowData.add(utilizationFileCommonDTO.getInvoice().getCessRate() == null ? StringUtils.EMPTY // Cess Rate
				: utilizationFileCommonDTO.getInvoice().getCessRate());
		rowData.add(utilizationFileCommonDTO.getInvoice().getCessAmount() == null ? StringUtils.EMPTY // Cess Amount
				: utilizationFileCommonDTO.getInvoice().getCessAmount());
		rowData.add(utilizationFileCommonDTO.getInvoice().getCreditable() == true ? 'Y' : 'N'); // Creditable (Y/N)
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getPos()) ? StringUtils.EMPTY // POS
				: utilizationFileCommonDTO.getInvoice().getPos());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getTdsSection()) ? StringUtils.EMPTY // TDS
																													// Section
				: utilizationFileCommonDTO.getInvoice().getTdsSection());
		rowData.add(utilizationFileCommonDTO.getInvoice().getTdsRate() == null ? StringUtils.EMPTY // TDS Rate
				: utilizationFileCommonDTO.getInvoice().getTdsRate());
		rowData.add(utilizationFileCommonDTO.getInvoice().getTdsAmount() == null ? StringUtils.EMPTY // TDS Amount
				: utilizationFileCommonDTO.getInvoice().getTdsAmount());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getPoNumber()) ? StringUtils.EMPTY // PO
																													// Number
				: utilizationFileCommonDTO.getInvoice().getPoNumber());
		rowData.add(utilizationFileCommonDTO.getInvoice().getPoDate() == null ? StringUtils.EMPTY // PO date
				: utilizationFileCommonDTO.getInvoice().getPoDate());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getLinkedAdvanceNumber()) // Linked
																										// advance
																										// Number
				? StringUtils.EMPTY
				: utilizationFileCommonDTO.getInvoice().getLinkedAdvanceNumber());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getGrossIndicator()) // Grossing up
																									// Indicator
				? StringUtils.EMPTY
				: utilizationFileCommonDTO.getInvoice().getGrossIndicator());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getOriginalDocumentNumber())
				? StringUtils.EMPTY // Original Document Number
				: utilizationFileCommonDTO.getInvoice().getOriginalDocumentNumber());
		rowData.add(utilizationFileCommonDTO.getInvoice().getOriginalDocumentDate() == null ? StringUtils.EMPTY // Original
																												// Document
																												// Date
				: utilizationFileCommonDTO.getInvoice().getOriginalDocumentDate());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getUserDefinedField1()) ? StringUtils.EMPTY // User
																														// Defined
																														// Field
																														// 1
						: utilizationFileCommonDTO.getInvoice().getUserDefinedField1());

		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getUserDefinedField2()) ? StringUtils.EMPTY // User
																														// Defined
																														// Field
																														// 2
						: utilizationFileCommonDTO.getInvoice().getUserDefinedField2());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getInvoice().getUserDefinedField3()) ? StringUtils.EMPTY // User
																														// Defined
																														// Field
																														// 3
						: utilizationFileCommonDTO.getInvoice().getUserDefinedField3());

	}

	public void setAdvanceDataForMatrixReports(UtilizationFileCommonDTO utilizationFileCommonDTO,
			List<Object> rowData) {
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getSourceFileName()) ? StringUtils.EMPTY // Source
																														// File
																														// Name
				: utilizationFileCommonDTO.getAdvance().getSourceFileName());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getCompanyCode()) ? StringUtils.EMPTY // Company
																													// Code
				: utilizationFileCommonDTO.getAdvance().getCompanyCode());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getNameOfTheCompanyCode()) ? StringUtils.EMPTY // Name
																															// Of
																															// The
																															// Company
																															// Code
						: utilizationFileCommonDTO.getAdvance().getNameOfTheCompanyCode());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getDeductorMasterTan()) ? StringUtils.EMPTY // Deductor
																												// TAN
				: utilizationFileCommonDTO.getDeductorMasterTan());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getDeductorGstin()) ? StringUtils.EMPTY // Deductor
																														// GSTIN
				: utilizationFileCommonDTO.getAdvance().getDeductorGstin());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getDeducteePan()) ? StringUtils.EMPTY // Deductee
																													// PAN
				: utilizationFileCommonDTO.getAdvance().getDeducteePan());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getDeducteeTin()) ? StringUtils.EMPTY // Deductee
																													// TIN
				: utilizationFileCommonDTO.getAdvance().getDeducteeTin());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getDeducteeName()) ? StringUtils.EMPTY // Name
																														// of
																														// the
																														// Deductee
				: utilizationFileCommonDTO.getAdvance().getDeducteeName());
		rowData.add(StringUtils.EMPTY); // Deductee Address
		rowData.add(StringUtils.EMPTY); // Vendor Invoice Number
		rowData.add(StringUtils.EMPTY); // Miro Number
		rowData.add(StringUtils.EMPTY); // Migo Number
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getDocumentType()) ? StringUtils.EMPTY // Document
																														// Type
				: utilizationFileCommonDTO.getAdvance().getDocumentType());
		rowData.add(utilizationFileCommonDTO.getAdvance().getDocumentDate() == null ? StringUtils.EMPTY // Document date
				: utilizationFileCommonDTO.getAdvance().getDocumentDate());
		rowData.add((utilizationFileCommonDTO.getAdvance().getPostingDateOfDocument() == null) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getAdvance().getPostingDateOfDocument());
		rowData.add(utilizationFileCommonDTO.getAdvance().getLineItemNumber() == null ? StringUtils.EMPTY // Line Item
																											// Number
				: utilizationFileCommonDTO.getAdvance().getLineItemNumber());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getHsnOrSac()) // HSN/SAC
				? StringUtils.EMPTY
				: utilizationFileCommonDTO.getAdvance().getHsnOrSac());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getSacDescription()) ? StringUtils.EMPTY // Sac
																														// Description
				: utilizationFileCommonDTO.getAdvance().getSacDescription());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getServiceDescriptionInvoice())
				? StringUtils.EMPTY // Service Description - Invoice
				: utilizationFileCommonDTO.getAdvance().getServiceDescriptionInvoice());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getServiceDescriptionPo()) ? StringUtils.EMPTY // Service
																															// Description
																															// -
																															// PO
						: utilizationFileCommonDTO.getAdvance().getServiceDescriptionPo());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getServiceDescriptionGl()) ? StringUtils.EMPTY // Service
																															// Description
																															// -
																															// GL
																															// Text
						: utilizationFileCommonDTO.getAdvance().getServiceDescriptionGl());
		rowData.add(utilizationFileCommonDTO.getAdvance().getAmount() == null ? StringUtils.EMPTY // Taxable value
				: utilizationFileCommonDTO.getAdvance().getAmount());
		rowData.add(StringUtils.EMPTY); // IGST Rate
		rowData.add(StringUtils.EMPTY); // IGST Amount
		rowData.add(StringUtils.EMPTY); // CGST Rate
		rowData.add(StringUtils.EMPTY); // CGST Amount
		rowData.add(StringUtils.EMPTY); // SGST Rate
		rowData.add(StringUtils.EMPTY); // SGST Amount
		rowData.add(utilizationFileCommonDTO.getAdvance().getCessRate() == null ? StringUtils.EMPTY // Cess Rate
				: utilizationFileCommonDTO.getAdvance().getCessRate());
		rowData.add(utilizationFileCommonDTO.getAdvance().getCessAmount() == null ? StringUtils.EMPTY // Cess Amount
				: utilizationFileCommonDTO.getAdvance().getCessAmount());
		rowData.add(StringUtils.EMPTY); // Creditable (Y/N)
		rowData.add(StringUtils.EMPTY); // POS
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getWithholdingSection()) ? StringUtils.EMPTY // TDS
																														// Section
						: utilizationFileCommonDTO.getAdvance().getWithholdingSection());
		rowData.add(utilizationFileCommonDTO.getAdvance().getWithholdingRate() == null ? StringUtils.EMPTY // TDS Rate
				: utilizationFileCommonDTO.getAdvance().getWithholdingRate());
		rowData.add(utilizationFileCommonDTO.getAdvance().getClientAmount() == null ? StringUtils.EMPTY // TDS Amount
				: utilizationFileCommonDTO.getAdvance().getClientAmount());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getPoNumber()) ? StringUtils.EMPTY // PO
																													// number
				: utilizationFileCommonDTO.getAdvance().getPoNumber());
		rowData.add(utilizationFileCommonDTO.getAdvance().getPoDate() == null ? StringUtils.EMPTY // PO date
				: utilizationFileCommonDTO.getAdvance().getPoDate());
		rowData.add(StringUtils.EMPTY); // Linked advance Number
		rowData.add(StringUtils.EMPTY); // Grossing up Indicator
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getDocumentNumber()) ? StringUtils.EMPTY // Original
																														// Document
																														// Number
				: utilizationFileCommonDTO.getAdvance().getDocumentNumber());
		rowData.add(utilizationFileCommonDTO.getAdvance().getDocumentDate() == null ? StringUtils.EMPTY // Original
																										// Document Date
				: utilizationFileCommonDTO.getAdvance().getDocumentDate());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getUserDefinedField1()) ? StringUtils.EMPTY // User
																														// Defined
																														// Field
																														// 1
						: utilizationFileCommonDTO.getAdvance().getUserDefinedField1());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getUserDefinedField2()) ? StringUtils.EMPTY // User
																														// Defined
																														// Field
																														// 2
						: utilizationFileCommonDTO.getAdvance().getUserDefinedField2());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getAdvance().getUserDefinedField3()) ? StringUtils.EMPTY // User
																														// Defined
																														// Field
																														// 3
						: utilizationFileCommonDTO.getAdvance().getUserDefinedField3());
	}

	// provision
	public void setProvisionDataForMatrixReports(UtilizationFileCommonDTO utilizationFileCommonDTO,
			List<Object> rowData) {
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getSourceFileName()) ? StringUtils.EMPTY // Source
																															// File
																															// Name
				: utilizationFileCommonDTO.getProvision().getSourceFileName());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getCompanyCode()) ? StringUtils.EMPTY // Company
																														// Code
				: utilizationFileCommonDTO.getProvision().getCompanyCode());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getNameOfTheCompanyCode())
				? StringUtils.EMPTY // Name Of The Company Code
				: utilizationFileCommonDTO.getProvision().getNameOfTheCompanyCode());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getDeductorMasterTan()) ? StringUtils.EMPTY // Deductor
																												// TAN
				: utilizationFileCommonDTO.getDeductorMasterTan());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getDeductorGstin()) ? StringUtils.EMPTY // Deductor
																														// GSTIN
				: utilizationFileCommonDTO.getProvision().getDeductorGstin());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getDeducteePan()) ? StringUtils.EMPTY // Deductee
																														// PAN
				: utilizationFileCommonDTO.getProvision().getDeducteePan());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getDeducteeTin()) ? StringUtils.EMPTY // Deductee
																														// TIN
				: utilizationFileCommonDTO.getProvision().getDeducteeTin());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getDeducteeName()) ? StringUtils.EMPTY // Name
																														// of
																														// the
																														// Deductee
				: utilizationFileCommonDTO.getProvision().getDeducteeName());
		rowData.add(StringUtils.EMPTY); // Deductee Address
		rowData.add(StringUtils.EMPTY); // Vendor Invoice Number
		rowData.add(StringUtils.EMPTY); // Miro Number
		rowData.add(StringUtils.EMPTY); // Migo Number
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getDocumentType()) ? StringUtils.EMPTY // Document
																														// Type
				: utilizationFileCommonDTO.getProvision().getDocumentType());
		rowData.add(utilizationFileCommonDTO.getProvision().getDocumentDate() == null ? StringUtils.EMPTY // Document
																											// date
				: utilizationFileCommonDTO.getProvision().getDocumentDate());
		rowData.add((utilizationFileCommonDTO.getProvision().getPostingDateOfDocument() == null) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getProvision().getPostingDateOfDocument());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getLineItemNumber()) ? StringUtils.EMPTY // Line
																															// Item
																															// Number
				: utilizationFileCommonDTO.getProvision().getLineItemNumber());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getHsnOrSac()) ? StringUtils.EMPTY // HSN/SAC
				: utilizationFileCommonDTO.getProvision().getHsnOrSac());
		rowData.add(StringUtils.EMPTY); // Sac Description
		rowData.add(StringUtils.EMPTY); // Service Description - Invoice
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getServiceDescriptionPo())
				? StringUtils.EMPTY // Service Description - PO
				: utilizationFileCommonDTO.getProvision().getServiceDescriptionPo());
		rowData.add(utilizationFileCommonDTO.getProvision().getServiceDescriptionGl() == null ? StringUtils.EMPTY // Service
																													// Description
																													// -
																													// GL
																													// Text
				: utilizationFileCommonDTO.getProvision().getServiceDescriptionGl());
		rowData.add(utilizationFileCommonDTO.getProvision().getProvisionalAmount() == null ? StringUtils.EMPTY // Taxable
																												// value
				: utilizationFileCommonDTO.getProvision().getProvisionalAmount());
		rowData.add(StringUtils.EMPTY); // IGST Rate
		rowData.add(StringUtils.EMPTY); // IGST Amount
		rowData.add(StringUtils.EMPTY); // CGST Rate
		rowData.add(StringUtils.EMPTY); // CGST Amount
		rowData.add(StringUtils.EMPTY); // SGST Rate
		rowData.add(StringUtils.EMPTY); // SGST Amount
		rowData.add(utilizationFileCommonDTO.getProvision().getCessRate() == null ? StringUtils.EMPTY // Cess Rate
				: utilizationFileCommonDTO.getProvision().getCessRate());
		rowData.add(utilizationFileCommonDTO.getProvision().getCessAmount() == null ? StringUtils.EMPTY // Cess Amount
				: utilizationFileCommonDTO.getProvision().getCessAmount());
		rowData.add(StringUtils.EMPTY); // Creditable (Y/N)
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getPos()) ? StringUtils.EMPTY // POS
				: utilizationFileCommonDTO.getProvision().getPos());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getClientSection()) ? StringUtils.EMPTY // TDS
																														// Section
				: utilizationFileCommonDTO.getProvision().getClientSection());
		rowData.add(utilizationFileCommonDTO.getProvision().getClientRate() == null ? StringUtils.EMPTY // TDS Rate
				: utilizationFileCommonDTO.getProvision().getClientRate());
		rowData.add(utilizationFileCommonDTO.getProvision().getClientAmount() == null ? StringUtils.EMPTY // TDS Amount
				: utilizationFileCommonDTO.getProvision().getClientAmount());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getPoNumber()) ? StringUtils.EMPTY // PO
																													// number
				: utilizationFileCommonDTO.getProvision().getPoNumber());
		rowData.add(utilizationFileCommonDTO.getProvision().getPoDate() == null ? StringUtils.EMPTY // PO date
				: utilizationFileCommonDTO.getProvision().getPoDate());
		rowData.add(StringUtils.EMPTY); // Linked advance Number
		rowData.add(StringUtils.EMPTY); // Grossing up Indicator
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getDocumentNumber()) ? StringUtils.EMPTY // Original
																															// Document
																															// Number
				: utilizationFileCommonDTO.getProvision().getDocumentNumber());
		rowData.add(utilizationFileCommonDTO.getProvision().getDocumentDate() == null ? StringUtils.EMPTY // Original
																											// Document
																											// Date
				: utilizationFileCommonDTO.getProvision().getDocumentDate());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getUserDefinedField1()) ? StringUtils.EMPTY // User
																														// Defined
																														// Field
																														// 1
						: utilizationFileCommonDTO.getProvision().getUserDefinedField1());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getUserDefinedField2()) ? StringUtils.EMPTY // User
																														// Defined
																														// Field
																														// 2
						: utilizationFileCommonDTO.getProvision().getUserDefinedField2());
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getProvision().getUserDefinedField3()) ? StringUtils.EMPTY // User
																														// Defined
																														// Field
																														// 3
						: utilizationFileCommonDTO.getProvision().getUserDefinedField3());
	}

	// InvoiceShareholderResident
	public void setInvoiceShareHolderResidentDataForMatrixReports(UtilizationFileCommonDTO utilizationFileCommonDTO,
			List<Object> rowData) {
		rowData.add(StringUtils.EMPTY);// Source File Name
		rowData.add(StringUtils.EMPTY); // Company Code
		rowData.add(StringUtils.EMPTY); // Name Of The Company Code
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getDeductorMasterTan()) ? StringUtils.EMPTY // Deductor
				// TAN
				: utilizationFileCommonDTO.getDeductorMasterTan());
		rowData.add(StringUtils.EMPTY); // Deductor GSTIN
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoiceShareholderResident().getShareholderPan())
				? StringUtils.EMPTY // Deductee
				// PAN
				: utilizationFileCommonDTO.getInvoiceShareholderResident().getShareholderPan());
		rowData.add(StringUtils.EMPTY); // Deductee TIN
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoiceShareholderResident().getShareholderName())
				? StringUtils.EMPTY // Name
				: utilizationFileCommonDTO.getInvoiceShareholderResident().getShareholderName());
		rowData.add(StringUtils.EMPTY); // Deductee Address
		rowData.add(StringUtils.EMPTY); // Vendor Invoice Number
		rowData.add(StringUtils.EMPTY); // Miro Number
		rowData.add(StringUtils.EMPTY); // Migo Number
		rowData.add(StringUtils.EMPTY);// Document Type
		rowData.add(StringUtils.EMPTY);// Document date
		rowData.add((utilizationFileCommonDTO.getInvoiceShareholderResident().getDateOfPosting() == null)
				? StringUtils.EMPTY
				: utilizationFileCommonDTO.getInvoiceShareholderResident().getDateOfPosting());
		rowData.add(StringUtils.EMPTY);// Line Item Number
		rowData.add(StringUtils.EMPTY); // HSN/SAC
		rowData.add(StringUtils.EMPTY); // Sac Description
		rowData.add(StringUtils.EMPTY); // Service Description - Invoice
		rowData.add(StringUtils.EMPTY); // Service Description - PO
		rowData.add(StringUtils.EMPTY); // Service Description -GL Text
		rowData.add(utilizationFileCommonDTO.getInvoiceShareholderResident().getDividendAmountRs() == null
				? StringUtils.EMPTY // Taxable value
				: utilizationFileCommonDTO.getInvoiceShareholderResident().getDividendAmountRs());
		rowData.add(StringUtils.EMPTY); // IGST Rate
		rowData.add(StringUtils.EMPTY); // IGST Amount
		rowData.add(StringUtils.EMPTY); // CGST Rate
		rowData.add(StringUtils.EMPTY); // CGST Amount
		rowData.add(StringUtils.EMPTY); // SGST Rate
		rowData.add(StringUtils.EMPTY); // SGST Amount
		rowData.add(StringUtils.EMPTY); // Cess Rate
		rowData.add(StringUtils.EMPTY);// Cess Amount
		rowData.add(StringUtils.EMPTY); // Creditable (Y/N)
		rowData.add(StringUtils.EMPTY);// POS
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoiceShareholderResident().getTdsSection())
				? StringUtils.EMPTY // TDS
				// Section
				: utilizationFileCommonDTO.getInvoiceShareholderResident().getTdsSection());
		rowData.add(utilizationFileCommonDTO.getInvoiceShareholderResident().getTdsRate() == null ? StringUtils.EMPTY // TDS
																														// Rate
				: utilizationFileCommonDTO.getInvoiceShareholderResident().getTdsRate());
		rowData.add(BigDecimal.ZERO);// TDS Amount
		rowData.add(StringUtils.EMPTY); // PO number
		rowData.add(StringUtils.EMPTY); // PO date
		rowData.add(StringUtils.EMPTY); // Linked advance Number
		rowData.add(StringUtils.EMPTY); // Grossing up Indicator
		rowData.add(StringUtils.EMPTY);// Original Document Number
		rowData.add(StringUtils.EMPTY);// Original Document Date
		rowData.add(StringUtils.EMPTY); // User defined field 1
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
	}

	// InvoiceShareholder Non Resident
	public void setInvoiceShareHolderNonResidentDataForMatrixReports(UtilizationFileCommonDTO utilizationFileCommonDTO,
			List<Object> rowData) {
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getDeductorMasterTan()) ? StringUtils.EMPTY // Deductor
																												// TAN
				: utilizationFileCommonDTO.getDeductorMasterTan());
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoiceShareholderNonResident().getShareholderPan())
				? StringUtils.EMPTY // Deductee PAN
				: utilizationFileCommonDTO.getInvoiceShareholderNonResident().getShareholderPan());
		rowData.add(StringUtils.EMPTY);
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getInvoiceShareholderNonResident().getShareholderName())
						? StringUtils.EMPTY // Name of the Deductee
						: utilizationFileCommonDTO.getInvoiceShareholderNonResident().getShareholderName());
		rowData.add(StringUtils.EMPTY); // Deductee Address
		rowData.add(StringUtils.EMPTY); // Vendor Invoice Number
		rowData.add(StringUtils.EMPTY); // Miro Number
		rowData.add(StringUtils.EMPTY); // Migo Number
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
		rowData.add((utilizationFileCommonDTO.getInvoiceShareholderNonResident().getDateOfPosting() == null)
				? StringUtils.EMPTY
				: utilizationFileCommonDTO.getInvoiceShareholderNonResident().getDateOfPosting());
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY); // Sac Description
		rowData.add(StringUtils.EMPTY); // Service Description - Invoice
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
		rowData.add(utilizationFileCommonDTO.getInvoiceShareholderNonResident().getDividendAmountRs() == null
				? StringUtils.EMPTY // Taxable value
				: utilizationFileCommonDTO.getInvoiceShareholderNonResident().getDividendAmountRs());
		rowData.add(StringUtils.EMPTY); // IGST Rate
		rowData.add(StringUtils.EMPTY); // IGST Amount
		rowData.add(StringUtils.EMPTY); // CGST Rate
		rowData.add(StringUtils.EMPTY); // CGST Amount
		rowData.add(StringUtils.EMPTY); // SGST Rate
		rowData.add(StringUtils.EMPTY); // SGST Amount
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY); // Creditable (Y/N)
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getInvoiceShareholderNonResident().getTdsSection())
				? StringUtils.EMPTY // TDS Section
				: utilizationFileCommonDTO.getInvoiceShareholderNonResident().getTdsSection());
		rowData.add(utilizationFileCommonDTO.getInvoiceShareholderNonResident().getTdsRate() == null ? StringUtils.EMPTY // TDS
																															// Rate
				: utilizationFileCommonDTO.getInvoiceShareholderNonResident().getTdsRate());
		rowData.add(BigDecimal.ZERO);
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY); // Linked advance Number
		rowData.add(StringUtils.EMPTY); // Grossing up Indicator
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
		rowData.add(StringUtils.EMPTY);
	}

	/**
	 * to generarte the file and save in batch upload
	 * 
	 * @param lccId
	 * @param tenantId
	 * @param tanNumber
	 * @param userName
	 * @param collectorPan
	 * @throws Exception
	 */
	@Async
	public void generateLdcUtilizationFileAsync(Integer ldcId, String tenantId, String tanNumber, String userName,
			String collectorPan) throws Exception {
		logger.info("Service Method executing , Record saving in Data Base {}");
		MultiTenantContext.setTenantId(tenantId);

		Integer year = CommonUtil.getAssessmentYear(null);
		BatchUpload batchUpload = new BatchUpload();
		String[] headers;
		Integer isDividend = null;
		List<LdcUtilizationDTO> list = null;
		Workbook workbook = null;
		Worksheet worksheet = null;
		ImportTableOptions tableOptions = null;

		batchUpload = saveLDCUtiliationReportInBatchUpload(null, "LDC_UTILIZATION_REPORT", "Processing", tenantId, 0L,
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
		style2.setTextWrapped(true);

		Range headerColorRange1 = worksheet.getCells().createRange("A5:S5");
		headerColorRange1.setStyle(style0);

		worksheet.setName("ldc_utiliation_report");

		String msg = getHeaderReportMsg(tanNumber, tenantId, collectorPan);
		worksheet.getCells().get("A1").setValue(msg);
		worksheet.getCells().merge(0, 0, 4, 5);
		headerColorRange1 = worksheet.getCells().createRange("A1:E1");
		headerColorRange1.setStyle(style2);

		isDividend = ldcMasterDAO.isDividendLDC(ldcId);
		list = lDCUtilizationDAO.getLdcUtilizationRecordsById(ldcId, isDividend);
		logger.info("DAO execution sucessfull and no of records is " + list.size() + " {}");

		if (isDividend == 1) {
			logger.info("LDC is having dividend {}");
			headers = new String[] { "Certificate No", "Assesment Year", "Assesment Month", "LDC Rate", "LDC Section",
					"LDC Master Total Amount", "LDC Applicable From", "LDC Applicable To", " Document Number",
					"Date Of Posting", "Line Item Number", "Client Overriden Rate", "Final TDS Section",
					"Final Total Withholding", "Applicable Total Taxable Amount", "Utilized Amount", "Remaining Amount",
					"Initial Utilized Amount", "Consumed From" };

		} else {
			logger.info("LDC is having not dividend {}");
			headers = new String[] { "Certificate No", "Assesment Year", "Assesment Month", "LDC Rate", "LDC Section",
					"LDC Master Total Amount", "LDC Applicable From", "LDC Applicable To", "Document Number",
					"Document Date", "Line Item Number", "Final TDS Rate", "Final TDS Section", "Final TDS Amount",
					"Applicable Total Taxable Amount", "Utilized Amount", "Remaining Amount", "Initial Utilized Amount",
					"Consumed From" };
		}
		worksheet.getCells().importArray(headers, 4, 0, false);

		int rowIndex = 5;
		for (LdcUtilizationDTO data : list) {

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
			rowData.add(data.getLdcRate() == null ? "0.0" : data.getLdcRate());
			// LDC Section","
			rowData.add(StringUtils.isEmpty(data.getLdcSection()) ? StringUtils.EMPTY : data.getLdcSection());
			// LDC Master Total Amount
			rowData.add(data.getLdcMasterTotalAmount() == null ? "0.0" : data.getLdcMasterTotalAmount().toString());
			// LDC Applicable From",
			rowData.add(
					data.getLdcApplicableFrom() == null ? StringUtils.EMPTY : data.getLdcApplicableFrom().toString());
			// "LCC Applicable To","
			rowData.add(data.getLdcApplicableTo() == null ? StringUtils.EMPTY : data.getLdcApplicableTo().toString());
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

		File generatedExcelFile = new File("LDC_Utilization_Report" + new Date().getTime() + ".xlsx");
		OutputStream out = new FileOutputStream(generatedExcelFile);
		workbook.save(out, SaveFormat.XLSX);

		saveLDCUtiliationReportInBatchUpload(batchUpload.getBatchUploadID(), "LDC_UTILIZATION_REPORT", "Processed",
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
			logger.info(" LDC Utilization report {} completed for : {}", uploadType, userName);
		} else {
			logger.info(" LDC Utilization report {} started for : {}", uploadType, userName);
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
		return "LDC Utilization Report (Dated: " + date + ")\n Client Name: " + deductorData.getDeductorName() + "\n";
	}

}
