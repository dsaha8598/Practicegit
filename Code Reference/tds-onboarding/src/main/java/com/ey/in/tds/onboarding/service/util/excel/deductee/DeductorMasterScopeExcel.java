package com.ey.in.tds.onboarding.service.util.excel.deductee;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterScopeExcelDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterScopeExcelErrorDTO;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;
import com.ey.in.tds.onboarding.service.util.excel.DeductorExcel;

/**
 * 
 * @author scriptbees
 *
 */
public class DeductorMasterScopeExcel
		extends DeductorExcel<DeductorMasterScopeExcelDTO, DeductorMasterScopeExcelErrorDTO> {

	private static final String HEADER_DEDUCTOR_PAN = "Deductor PAN";
	private static final String HEADER_PAN_LDC_VALIDATION = "PAN/LDC Validation";
	private static final String HEADER_INVOICES_EXCEL_UPLOADS = "Invoices excel uploads";
	private static final String HEADER_RATE_PREDICATION = "Rate Prediction";
	private static final String HEADER_MONTHLY_COMPLIANCE = "Monthly Compliance";
	private static final String HEADER_QUARTERLY_RETURNS = "Quarterly Returns";
	private static final String HEADER_CLAUSE_34A = "Clause 34a";
	private static final String HEADER_TRACES_DEFAULTS = "TRACES Defaults, console and Justifications";
	private static final String HEADER_SAP = "SAP";
	private static final String HEADER_EXCEL = "EXCEL";
	private static final String HEADER_PDF = "PDF";
	private static final String HEADER_INVOICE_DESCRIPTION = "Invoice Description";
	private static final String HEADER_PO_DESCRIPTION = "PO Description";
	private static final String HEADER_GL_DESCRIPTION = "GL Description";
	private static final String HEADER_SAC = "SAC";
	private static final String HEADER_VENDOR_MASTER = "Vendor Master";
	private static final String HEADER_PROVISION_TRACKING_PERIODS = "Provision Tracking Periods";
	private static final String HEADER_PROVISION_PROCESSING = "Provision Processing";
	private static final String HEADER_ADVANCE_PROCESSING = "Advance Processing";
	private static final String HEADER_CREDIT_NOTE_PROCESSING = "Credit Note Processing";
	private static final String HEADER_CONSOLIDATED_CHALLAN_PROCESSING = "Consolidated Challan Processing";

	private static final String HEADER_DIVIDENDS = "Dividends(masters, transaction processing, liability computation)";
	private static final String HEADER_FILINGFORM_15CA_CB = "Filing form 15 CA/ CB";
	private static final String HEADER_FILINGFORM_15_GH = "Filing form 15 G/H";
	private static final String HEADER_ENABLE_ANNUAL_PERTRANSACTION_LIMIT = "Enable Annual / Per transaction limit for rate determination ?";
	private static final String HEADER_SELECT_TYPE_OF_INTEREST_CALCULATION = "Select type of interest calulation";
	private static final String HEADER_SELECT_ROUNDIG_OFF = "Select rounding off";
	private static final String HEADER_KEY_STRATEGIC_SHAREHOLDER_A = "Key/ Strategic Shareholders(A)";
	private static final String HEADER_NON_KEY_STRATEGIC_SHAREHOLDER_A = "Non-key/ Strategic shareholders (A)";
	private static final String HEADER_KEY_STRATEGIC_SHAREHOLDER_B = "Key/ Strategic Shareholders(B)";
	private static final String HEADER_NON_KEY_STRATEGIC_SHAREHOLDER_B = "Non-key/ Strategic shareholders(B)";
	private static final String HEADER_KEY_STRATEGIC_SHAREHOLDER_C = "Key/ Strategic Shareholders  (C)";
	private static final String HEADER_NON_KEY_STRATEGIC_SHAREHOLDER_C = "Non-key/ Strategic shareholders  (C)";
	private static final String HEADER_KEY_STRATEGIC_SHAREHOLDER_D = "Key/ Strategic Shareholders  (D)";
	private static final String HEADER_NON_KEY_STRATEGIC_SHAREHOLDER_D = "Non-key/ Strategic shareholders   (D)";
	private static final String HEADER_KEY_STRATEGIC_SHAREHOLDER_E = "Key/ Strategic Shareholders  (E)";
	private static final String HEADER_NON_KEY_STRATEGIC_SHAREHOLDER_E = "Non-key/ Strategic shareholders  (E)";
	private static final String HEADER_KEY_STRATEGIC_SHAREHOLDER_F = "Key/ Strategic Shareholders  (F)";
	private static final String HEADER_NON_KEY_STRATEGIC_SHAREHOLDER_F = "Non-key/ Strategic shareholders   (F)";
	private static final String HEADER_IS_DIVIDEND_DISTRIBUTION_TAX_PAID = "Is Dividend Distribution Tax paid on dividend declared before 31st March, 2020";
	private static final String HEADER_PREPARATION_OF_FORM_15CB = "Preparation of Form 15CB";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
			new FieldMapping(HEADER_DEDUCTOR_PAN, "deductorPan", "deductorPan", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PAN_LDC_VALIDATION, "panLdcValidation", "panLdcValidation"),
			new FieldMapping(HEADER_INVOICES_EXCEL_UPLOADS, "invoicesProvisionsAdvancesDetermination",
					"invoicesProvisionsAdvancesDetermination"),
			new FieldMapping(HEADER_RATE_PREDICATION, "ratePredication", "ratePredication"),
			new FieldMapping(HEADER_MONTHLY_COMPLIANCE, "monthlyCompliance", "monthlyCompliance"),
			new FieldMapping(HEADER_QUARTERLY_RETURNS, "quarterlyReturns", "quarterlyReturns"),
			new FieldMapping(HEADER_CLAUSE_34A, "clause34a", "clause34a"),
			new FieldMapping(HEADER_TRACES_DEFAULTS, "tracesDefaults", "tracesDefaults"),
			new FieldMapping(HEADER_SAP, "sap", "sap"), new FieldMapping(HEADER_EXCEL, "excel", "excel"),
			new FieldMapping(HEADER_PDF, "pdf", "pdf"),
			new FieldMapping(HEADER_INVOICE_DESCRIPTION, "invoiceDescription", "invoiceDescription"),
			new FieldMapping(HEADER_PO_DESCRIPTION, "poDescription", "poDescription"),
			new FieldMapping(HEADER_GL_DESCRIPTION, "glDescription", "glDescription"),
			new FieldMapping(HEADER_SAC, "sac", "sac"),
			new FieldMapping(HEADER_VENDOR_MASTER, "vendorMaster", "vendorMaster"),
			new FieldMapping(HEADER_PROVISION_TRACKING_PERIODS, "provisionTrackingPeriods", "provisionTrackingPeriods"),
			new FieldMapping(HEADER_PROVISION_PROCESSING, "provisionProcessing", "provisionProcessing"),
			new FieldMapping(HEADER_ADVANCE_PROCESSING, "advanceProcessing", "advanceProcessing"),
			new FieldMapping(HEADER_CREDIT_NOTE_PROCESSING, "creditNoteProcessing", "creditNoteProcessing"),
			new FieldMapping(HEADER_CONSOLIDATED_CHALLAN_PROCESSING, "consolidatedChallanProcessing",
					"consolidatedChallanProcessing"),

			new FieldMapping(HEADER_DIVIDENDS, "dividends", "dividends"),
			new FieldMapping(HEADER_FILINGFORM_15CA_CB, "filingform15CACB", "filingform15CACB"),
			new FieldMapping(HEADER_FILINGFORM_15_GH, "filingform15GH", "filingform15GH"),
			new FieldMapping(HEADER_ENABLE_ANNUAL_PERTRANSACTION_LIMIT, "enableAnnualTransactionPerLimit",
					"enableAnnualTransactionPerLimit"),
			new FieldMapping(HEADER_SELECT_TYPE_OF_INTEREST_CALCULATION, "selectTypeInterestCalculation",
					"selectTypeInterestCalculation"),
			new FieldMapping(HEADER_SELECT_ROUNDIG_OFF, "selectRoundingOff", "selectRoundingOff"),
			new FieldMapping(HEADER_KEY_STRATEGIC_SHAREHOLDER_A, "keyStrategicShareHolderA",
					"keyStrategicShareHolderA"),
			new FieldMapping(HEADER_NON_KEY_STRATEGIC_SHAREHOLDER_A, "nonKeyStrategicShareHolderA",
					"nonKeyStrategicShareHolderA"),
			new FieldMapping(HEADER_KEY_STRATEGIC_SHAREHOLDER_B, "keyStrategicShareHolderB",
					"keyStrategicShareHolderB"),
			new FieldMapping(HEADER_NON_KEY_STRATEGIC_SHAREHOLDER_B, "nonKeyStrategicShareHolderB",
					"nonKeyStrategicShareHolderB"),
			new FieldMapping(HEADER_KEY_STRATEGIC_SHAREHOLDER_C, "keyStrategicShareHolderC",
					"keyStrategicShareHolderC"),
			new FieldMapping(HEADER_NON_KEY_STRATEGIC_SHAREHOLDER_C, "nonKeyStrategicShareHolderC",
					"nonKeyStrategicShareHolderC"),
			new FieldMapping(HEADER_KEY_STRATEGIC_SHAREHOLDER_D, "keyStrategicShareHolderD",
					"keyStrategicShareHolderD"),
			new FieldMapping(HEADER_NON_KEY_STRATEGIC_SHAREHOLDER_D, "nonKeyStrategicShareHolderD",
					"nonKeyStrategicShareHolderD"),
			new FieldMapping(HEADER_KEY_STRATEGIC_SHAREHOLDER_E, "keyStrategicShareHolderE",
					"keyStrategicShareHolderE"),
			new FieldMapping(HEADER_NON_KEY_STRATEGIC_SHAREHOLDER_E, "nonKeyStrategicShareHolderE",
					"nonKeyStrategicShareHolderE"),
			new FieldMapping(HEADER_KEY_STRATEGIC_SHAREHOLDER_F, "keyStrategicShareHolderF",
					"keyStrategicShareHolderF"),
			new FieldMapping(HEADER_NON_KEY_STRATEGIC_SHAREHOLDER_F, "nonKeyStrategicShareHolderF",
					"nonKeyStrategicShareHolderF"),
			new FieldMapping(HEADER_IS_DIVIDEND_DISTRIBUTION_TAX_PAID, "isDividendDistriutionTaxPaid",
					"isDividendDistriutionTaxPaid"),
			new FieldMapping(HEADER_PREPARATION_OF_FORM_15CB, "preparationOfForm15CB", "preparationOfForm15CB")));

	public DeductorMasterScopeExcel(XSSFSheet workSheet) {
		super(workSheet, fieldMappings, DeductorMasterScopeExcelDTO.class, DeductorMasterScopeExcelErrorDTO.class);
	}

	@Override
	public DeductorMasterScopeExcelDTO get(int index) {
		DeductorMasterScopeExcelDTO deductee = new DeductorMasterScopeExcelDTO();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(deductee, index, fieldMapping);
		}
		return deductee;
	}

	public Optional<DeductorMasterScopeExcelErrorDTO> validate(int rowIndex) {
		StringJoiner errorMessages = new StringJoiner("\n");

		// validation check
		for (FieldMapping fieldMapping : fieldMappings) {
			logger.debug("Processing {}", fieldMapping.getExcelHeaderName());
			if (fieldMapping.getValidator() != null) {
				String validationMessage = fieldMapping.getValidator().apply(
						this.getHeaders().get(this.getHeaderIndex(fieldMapping.getExcelHeaderName().toLowerCase())),
						getRawCellValue(rowIndex, fieldMapping.getExcelHeaderName()));
				if (StringUtils.isNotEmpty(validationMessage)) {
					errorMessages.add(validationMessage);
				}
			}
			// ======================================================
			if (fieldMapping.getValidatorDouble() != null) {

				String validationMessage = getRawCellValue(rowIndex, fieldMapping.getExcelHeaderName());
				String headerNamee = this.getHeaders()
						.get(this.getHeaderIndex(fieldMapping.getExcelHeaderName().toLowerCase()));
				if (StringUtils.isBlank(validationMessage))
					errorMessages.add(headerNamee + " can not be empty");
			}
			// =======================================================
		}

		if (errorMessages.length() != 0) {
			DeductorMasterScopeExcelErrorDTO deducteeMasterScopeErrorReportDTO = this.getErrorDTO(rowIndex);
			deducteeMasterScopeErrorReportDTO.setReason(errorMessages.toString());
			return Optional.of(deducteeMasterScopeErrorReportDTO);
		} else {
			return Optional.empty();
		}
	}

}
