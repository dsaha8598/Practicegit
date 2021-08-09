package com.ey.in.tds.ingestion.service.ldc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.Color;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tcs.common.model.payment.TcsMatrixDTO;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.tcs.TCSInvoiceLineItem;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSLccMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSLccUtilizationDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsPaymentDTO;
import com.ey.in.tds.common.model.provision.TCSUtilizationFileCommonDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccUtilization;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.ingestion.service.tdsmismatch.TcsMismatchService;
import com.ey.in.tds.ingestion.tcs.dao.PaymentDAO;
import com.ey.in.tds.ingestion.tcs.dao.TCSInvoiceLineItemDAO;

import bsh.ParseException;

/**
 * 
 * @author vamsir
 *
 */
@Service
public class TCSLccUtilizationService extends TcsMismatchService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TCSLccUtilizationDAO tcsLccUtilizationDAO;

	@Autowired
	private TCSLccMasterDAO tcsLcccMasterDAO;

	@Autowired
	private TCSInvoiceLineItemDAO tcsInvoiceLineItemDAO;

	@Autowired
	private PaymentDAO paymentDAO;

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
	 * calculating lcc matrix values
	 * 
	 * @param deductorTan
	 * @return
	 * @throws ParseException
	 */
	public Map<Integer, Object> getLCCMatrix(String deductorTan, int yearFromUI, String certificateNumber)
			throws ParseException {
		String tenantId = MultiTenantContext.getTenantId();
		logger.info("tenant id :{}", tenantId);
		Map<Integer, Object> lccMatrixValues = new HashMap<>();
		List<TcsMatrixDTO> matrixValues = tcsLcccMasterDAO.getTcsLccMasterMatrix(deductorTan, certificateNumber,
				yearFromUI);
		for (TcsMatrixDTO matrixDTO : matrixValues) {
			lccMatrixValues.put(matrixDTO.getMonth(), matrixDTO);
		}
		return lccMatrixValues;
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
	public ByteArrayInputStream asyncLccMatrixAdjustedFileDownload(String deductorTan, Integer assessmentYear,
			Integer month, String certificateNumber, String tenantId, String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return lccMatrixAdjustedFileDownload(deductorTan, assessmentYear, month, certificateNumber, tenantId, userName);
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
	public ByteArrayInputStream lccMatrixAdjustedFileDownload(String deductorTan, int assessmentYear, int month,
			String certificateNumber, String tenantId, String userName) throws Exception {

		logger.info("Ldc matrix adjusted download request for Year : {} Month : {}", assessmentYear, month);
		String fileName = "Adjusted_Lcc_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TCSBatchUpload tcsBatchUpload = saveMatrixReport(deductorTan, tenantId, assessmentYear, null, 0L,
				UploadTypes.LCC_ADJUSTED_REPORT.name(), "Processing", month, userName, null, fileName);

		List<TCSLccMaster> lccMasterOpeningOptional = tcsLcccMasterDAO.getLccBycertificateNoAndTan(deductorTan,
				certificateNumber);
		List<TCSLccUtilization> lccAdjustments = new ArrayList<>();
		if (!lccMasterOpeningOptional.isEmpty()) {
			int pageNumer = 1;
			Pagination pagination = null;
			List<String> pageStates = null;
			BigInteger ldcAdjustmentsCount = tcsLccUtilizationDAO.getLccAdjustmentCount(deductorTan, assessmentYear,
					month, lccMasterOpeningOptional.get(0).getId());
			for (int index = 0; index < ldcAdjustmentsCount.intValue(); index += pageSize) {
				if (pageNumer++ == 1) {
					pagination = new Pagination(false, pageSize, null);
				} else {
					pagination = new Pagination(true, pageSize, pageStates);
				}
				List<TCSLccUtilization> ldcutilizations = tcsLccUtilizationDAO.getLccAdjustment(deductorTan,
						assessmentYear, month, lccMasterOpeningOptional.get(0).getId());
				lccAdjustments.addAll(ldcutilizations);
			}
		}

		ArrayList<TCSUtilizationFileCommonDTO> listutilizationFileCommonDTO = new ArrayList<>();

		for (TCSLccUtilization lccUtilization : lccAdjustments) {
			TCSUtilizationFileCommonDTO utilizationFileCommonDTO = new TCSUtilizationFileCommonDTO();
			List<TCSInvoiceLineItem> tcsInvoiceLine = null;
			List<TcsPaymentDTO> payment = null;
			if (UploadTypes.INVOICE.name().equalsIgnoreCase(lccUtilization.getConsumedFrom())) {
				tcsInvoiceLine = tcsInvoiceLineItemDAO.findByLineItemId(assessmentYear, deductorTan,
						lccUtilization.getInvoiceLineItemId());
			} else if (UploadTypes.PAYMENT.name().equalsIgnoreCase(lccUtilization.getConsumedFrom())) {
				payment = paymentDAO.findByYearTanId(assessmentYear, deductorTan,
						lccUtilization.getInvoiceLineItemId());
			}
			utilizationFileCommonDTO.setCollectorMasterTan(lccUtilization.getCollectorMasterTan());
			utilizationFileCommonDTO.setMasterPan(lccUtilization.getLccMasterPan());
			utilizationFileCommonDTO.setUtilizedAmount(lccUtilization.getLccMasterUtilizedAmount());

			if (tcsInvoiceLine != null && !tcsInvoiceLine.isEmpty()) {
				utilizationFileCommonDTO.setTcsInvoice(tcsInvoiceLine.get(0));
				utilizationFileCommonDTO.setType(UploadTypes.INVOICE.name());
			}
			if (payment != null && !payment.isEmpty()) {
				utilizationFileCommonDTO.setPayment(payment.get(0));
				utilizationFileCommonDTO.setType(UploadTypes.PAYMENT.name());
			}
			listutilizationFileCommonDTO.add(utilizationFileCommonDTO);
		}
		String[] lccMatrixheaderNames = new String[] { "Collector Master Tan", "LCC Master Pan", "Utilized Amount",
				"Source Identifier", "Source File Name", "Collector Code", "Collector PAN", "Collectee Code",
				"Collectee PAN", "Document Type", "Document Number", "Document Date", "Clearing Document Number",
				"Clearing Date", "Invoice Document Number", "Invoice Date", "Line Number", "Payment Desc",
				"GL Account Code", "GL Desc", "Amount", "TCS Section", "TCS Rate", "TCS Amount", "Surcharge Rate",
				"Surcharge Amount", "ITCESS Rate", "ITCESS Amount", "TDS Section", "TDS Rate", "TDS Amount",
				"User Defined Field1", "User Defined Field2", "User Defined Field3", "User Defined Field4",
				"User Defined Field5", "User Defined Field6" };

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Lcc Matrix Adjustments");
		worksheet.freezePanes(0, 4, 0, 4);
		worksheet.autoFitColumns();
		worksheet.autoFitRows();

		worksheet.getCells().importArray(lccMatrixheaderNames, 0, 0, false);

		setLccMatrixHeaders(listutilizationFileCommonDTO, worksheet);

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

		// Style for E1 to AK1 headers
		Style style5 = workbook.createStyle();
		style5.setForegroundColor(Color.fromArgb(91, 155, 213));
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(true);
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("E1:AK1");
		headerColorRange1.setStyle(style5);

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:AK1");

		workbook.save(out, SaveFormat.XLSX);
		saveMatrixReport(deductorTan, tenantId, assessmentYear, out, 1L, UploadTypes.LCC_ADJUSTED_REPORT.name(),
				"Processed", month, userName, tcsBatchUpload.getId(), null);
		return new ByteArrayInputStream(out.toByteArray());
	}

	public void setLccMatrixHeaders(ArrayList<TCSUtilizationFileCommonDTO> listutilizationFileCommonDTO,
			Worksheet worksheet) throws Exception {

		if (!listutilizationFileCommonDTO.isEmpty()) {
			int rowIndex = 1;
			BigDecimal adjustedSum = BigDecimal.valueOf(0);
			for (TCSUtilizationFileCommonDTO utilizationFileCommonDTO : listutilizationFileCommonDTO) {
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
	public ByteArrayInputStream asyncLccMatrixClosingAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, String certificateNumber, String type, String tenantId, String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		return lccMatrixClosingAmountDownload(deductorTan, assessmentYear, month, certificateNumber, type, tenantId,
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
	public ByteArrayInputStream lccMatrixClosingAmountDownload(String deductorTan, Integer assessmentYear,
			Integer month, String certificateNumber, String type, String tenantId, String userName) throws Exception {
		String fileName = "";
		if (UploadTypes.LCC_OPENING_REPORT.name().equalsIgnoreCase(type)) {
			logger.info("Lcc matrix opening report download request for Year : {} Month : {}", assessmentYear, month);
			fileName = "Lcc_opening_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		} else {
			logger.info("Lcc matrix opening report download request for Year : {} Month : {}", assessmentYear, month);
			fileName = "Lcc_closing_Report_" + new DateFormatSymbols().getMonths()[month - 1];
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TCSBatchUpload tcsBatchUpload = saveMatrixReport(deductorTan, tenantId, assessmentYear, null, 0L, type,
				"Processing", month, userName, null, fileName);

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("LCC Matrix");

		String[] headerNames = new String[] { "Collector Tan", "Opening Amount", "Adjusted Amount", "Closing Amount" };

		List<TcsMatrixDTO> matrixValues = tcsLcccMasterDAO.getTcsClosingAndOpeningMatrixReport(deductorTan,
				assessmentYear, month, certificateNumber);
		List<Object> rowData = new ArrayList<>();
		for (TcsMatrixDTO matrixDTO : matrixValues) {
			rowData.add(StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
			rowData.add(matrixDTO.getOpeningAmount() == null ? StringUtils.EMPTY : matrixDTO.getOpeningAmount());
			rowData.add(matrixDTO.getAdjustmentAmount() == null ? StringUtils.EMPTY : matrixDTO.getAdjustmentAmount());
			rowData.add(matrixDTO.getClosingAmount() == null ? StringUtils.EMPTY : matrixDTO.getClosingAmount());
		}
		worksheet.getCells().importArray(headerNames, 0, 0, false);
		worksheet.autoFitColumns();
		worksheet.autoFitRows();

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
				tcsBatchUpload.getId(), null);
		return new ByteArrayInputStream(out.toByteArray());
	}

	/**
	 * 
	 * @param utilizationFileCommonDTO
	 * @param rowData
	 */
	public void setInvoiceDataForMatrixReports(TCSUtilizationFileCommonDTO utilizationFileCommonDTO,
			List<Object> rowData) {
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getCollectorMasterTan()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getCollectorMasterTan());
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getMasterPan()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getMasterPan());
		rowData.add(utilizationFileCommonDTO.getUtilizedAmount());
		if ("INVOICE".equalsIgnoreCase(utilizationFileCommonDTO.getType())) {
			setInvoiceLineItemDataForMatrixReports(utilizationFileCommonDTO, rowData);
		}
		if ("PAYMENT".equalsIgnoreCase(utilizationFileCommonDTO.getType())) {
			setProvisionDataForMatrixReports(utilizationFileCommonDTO, rowData);
		}
	}

	// Invoice
	public void setInvoiceLineItemDataForMatrixReports(TCSUtilizationFileCommonDTO utilizationFileCommonDTO,
			List<Object> rowData) {
		// Source Identifier
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getSourceFileName()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getTcsInvoice().getSourceFileName());
		// Source File Name
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getSourceFileName()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getTcsInvoice().getSourceFileName());
		// Collector Code
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getCollectorCode()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getCollectorCode());
		// Collector PAN
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getCollectorPan()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getCollectorPan());
		// Collectee Code
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getCollecteeCode()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getCollecteeCode());
		// Collectee PAN
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getCollecteePan()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getCollecteePan());
		// Document Type
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getDocumentType()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getDocumentType());
		// Document Number
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getDocumentNumber()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getTcsInvoice().getDocumentNumber());
		// Document Date
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getDocumentDate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getDocumentDate());
		// Clearing Document Number
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getDocumentNumber()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getTcsInvoice().getDocumentNumber());
		// Clearing Date
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getDocumentDate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getDocumentDate());
		// Invoice Document Number
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getOriginalDocumentNumber())
				? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getOriginalDocumentNumber());
		// Invoice Date
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getOriginalDocumentDate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getOriginalDocumentDate());
		// Line Number
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getLineNumber() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getLineNumber());
		// Payment Desc
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getInvoiceDesc()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getInvoiceDesc());
		// GL Account Code
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getGlAccountCode()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getGlAccountCode());
		// GL Desc
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getGlDesc()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getGlDesc());
		// Amount
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getFinalTcsAmount() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getFinalTcsAmount());
		// TCS Section
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getTcsSection()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getTcsSection());
		// TCS Rate
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getTcsRate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getTcsRate());
		// TCS Amount
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getTcsAmount() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getTcsAmount());
		// Surcharge Rate
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getSurchargeRate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getSurchargeRate());
		// Surcharge Amount
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getSurchargeAmount() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getSurchargeAmount());
		// ITCESS Rate
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getItcessRate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getItcessRate());
		// ITCESS Amount
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getItcessAmount() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getItcessAmount());
		// TDS Section
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getActualTcsSection() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getActualTcsSection());
		// TDS Rate
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getActualTcsRate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getActualTcsRate());
		// TDS Amount
		rowData.add(utilizationFileCommonDTO.getTcsInvoice().getActualTcsAmount() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getTcsInvoice().getActualTcsAmount());
		// User Defined Field1
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getUserDefinedField1()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getTcsInvoice().getUserDefinedField1());
		// User Defined Field2
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getUserDefinedField2()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getTcsInvoice().getUserDefinedField2());
		// User Defined Field3
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getUserDefinedField3()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getTcsInvoice().getUserDefinedField3());
		// User Defined Field4
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getUserDefinedField4()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getTcsInvoice().getUserDefinedField4());
		// User Defined Field5
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getUserDefinedField5()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getTcsInvoice().getUserDefinedField5());
		// User Defined Field6
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getTcsInvoice().getUserDefinedField6()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getTcsInvoice().getUserDefinedField6());

	}

	// Payment
	public void setProvisionDataForMatrixReports(TCSUtilizationFileCommonDTO utilizationFileCommonDTO,
			List<Object> rowData) {
		// Source Identifier
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getSourceFileName()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getSourceFileName());
		// Source File Name
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getSourceFileName()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getSourceFileName());
		// Collector Code
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getCollectorCode()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getCollectorCode());
		// Collector PAN
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getCollectorPan()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getCollectorPan());
		// Collectee Code
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getCollecteeCode()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getCollecteeCode());
		// Collectee PAN
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getCollecteePan()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getCollecteePan());
		// Document Type
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getDocumentType()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getDocumentType());
		// Document Number
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getDocumentNumber()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getDocumentNumber());
		// Document Date
		rowData.add(utilizationFileCommonDTO.getPayment().getDocumentDate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getDocumentDate());
		// Clearing Document Number
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getDocumentNumber()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getDocumentNumber());
		// Clearing Date
		rowData.add(utilizationFileCommonDTO.getPayment().getDocumentDate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getDocumentDate());
		// Invoice Document Number
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getInvoiceDocumentNumber())
				? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getInvoiceDocumentNumber());
		// Invoice Date
		rowData.add(utilizationFileCommonDTO.getPayment().getInvoiceDate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getInvoiceDate());
		// Line Number
		rowData.add(utilizationFileCommonDTO.getPayment().getLineNumber() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getLineNumber());
		// Payment Desc
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getPaymentDesc()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getInvoiceDate());
		// GL Account Code
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getGlAccountCode()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getGlAccountCode());
		// GL Desc
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getGlDesc()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getGlDesc());
		// Amount
		rowData.add(utilizationFileCommonDTO.getPayment().getFinalTcsAmount() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getFinalTcsAmount());
		// TCS Section
		rowData.add(StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getTcsSection()) ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getTcsSection());
		// TCS Rate
		rowData.add(utilizationFileCommonDTO.getPayment().getTcsRate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getTcsRate());
		// TCS Amount
		rowData.add(utilizationFileCommonDTO.getPayment().getTcsAmount() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getTcsAmount());
		// Surcharge Rate
		rowData.add(utilizationFileCommonDTO.getPayment().getSurchargeRate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getSurchargeRate());
		// Surcharge Amount
		rowData.add(utilizationFileCommonDTO.getPayment().getSurchargeAmount() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getSurchargeAmount());
		// ITCESS Rate
		rowData.add(utilizationFileCommonDTO.getPayment().getItcessRate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getItcessRate());
		// ITCESS Amount
		rowData.add(utilizationFileCommonDTO.getPayment().getItcessAmount() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getItcessAmount());
		// TDS Section
		rowData.add(utilizationFileCommonDTO.getPayment().getActualTcsSection() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getActualTcsSection());
		// TDS Rate
		rowData.add(utilizationFileCommonDTO.getPayment().getActualTcsRate() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getActualTcsRate());
		// TDS Amount
		rowData.add(utilizationFileCommonDTO.getPayment().getActualTcsAmount() == null ? StringUtils.EMPTY
				: utilizationFileCommonDTO.getPayment().getActualTcsAmount());
		// User Defined Field1
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getUserDefinedField1()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getPayment().getUserDefinedField1());
		// User Defined Field2
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getUserDefinedField2()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getPayment().getUserDefinedField2());
		// User Defined Field3
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getUserDefinedField3()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getPayment().getUserDefinedField3());
		// User Defined Field4
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getUserDefinedField4()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getPayment().getUserDefinedField4());
		// User Defined Field5
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getUserDefinedField5()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getPayment().getUserDefinedField5());
		// User Defined Field6
		rowData.add(
				StringUtils.isBlank(utilizationFileCommonDTO.getPayment().getUserDefinedField6()) ? StringUtils.EMPTY
						: utilizationFileCommonDTO.getPayment().getUserDefinedField6());

	}

}
