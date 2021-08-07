package com.ey.in.tds.onboarding.service.util.excel.ldc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ey.in.tds.common.model.ldc.TracesLdcMasterErrorReportDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.NewLDCMasterTraces;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;

public class TracesLdcMasterExcel extends Excel<NewLDCMasterTraces, TracesLdcMasterErrorReportDTO> {

	private static final String HEADER_DEDUCTOR_TAN = "DeductorTan";
	private static final String HEADER_IS_DIVIDEND = "IsLDCIssuedForDividend";
	private static final String HEADER_IS_RESIDENT = "IsResident";
	private static final String HEADER_LDC_NUMBER = "LDCNumber";
	private static final String HEADER_FINANCIAL_YEAR = "FinancialYear";
	private static final String HEADER_DEDUCTEE_NAME = "DeducteeName";
	private static final String HEADER_DEDUCTEE_PAN = "DeducteePAN";
	private static final String HEADER_DATE_OF_ISSUE = "DateOfIssue";
	private static final String HEADER_APPLICABLE_FROM = "ValidFrom";
	private static final String HEADER_CANCEL_DATE = "CancelDate";
	private static final String HEADER_APPLICABLE_TO = "ValidTo";
	private static final String HEADER_SECTION = "TDSSection";
	private static final String HEADER_NATURE_OF_PAYMENT = "NatureOfPayment";
	private static final String HEADER_LDC_RATE = "TDSRate";
	private static final String HEADER_AMOUNT = "LDCLimit";
	private static final String HEADER_AMOUNT_UTILISED = "AmountUtilised";
	private static final String HEADER_LDC_SECTION_DETAILS = "LDCSectionDetails";
	private static final String HEADER_ASSESSING_OFFICER_DETAILS = "AssessingOfficerDetails";
	private static final String HEADER_APPROVED = "Approved";

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	// author dipak
	@Override
	public NewLDCMasterTraces get(int index) {
		NewLDCMasterTraces ldc = new NewLDCMasterTraces();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(ldc, index, fieldMapping);
		}
		return ldc;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
			new FieldMapping(HEADER_IS_DIVIDEND, "isLDCIssuedForDividend", "isLDCIssuedForDividend",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_IS_RESIDENT, "isResident", "isResident", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_LDC_NUMBER, "certificateNumber", "certificateNumber", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_FINANCIAL_YEAR, "financialYear", "financialYear", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_DEDUCTEE_NAME, "deducteeName", "deducteeName", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_DEDUCTEE_PAN, "pan", "pan", Excel.VALIDATION_PAN_MANDATORY),
			new FieldMapping(HEADER_NATURE_OF_PAYMENT, "natureOfPayment", "natureOfPayment",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_SECTION, "section", "section"),
			new FieldMapping(HEADER_AMOUNT, "amount", Excel.VALIDATION_MANDATORY_DOUBLE, "amount"),
			new FieldMapping(HEADER_LDC_RATE, "rate", Excel.VALIDATION_MANDATORY_DOUBLE, "rate"),
			new FieldMapping(HEADER_CANCEL_DATE, "applicableFrom", "cancelDate"),
			new FieldMapping(HEADER_DATE_OF_ISSUE, "applicableFrom", "dateOfIssue"),
			new FieldMapping(HEADER_APPLICABLE_FROM, "applicableFrom", "applicableFrom", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_APPLICABLE_TO, "applicableTo", "applicableTo", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_AMOUNT_UTILISED, "utilizedAmount", "utilizedAmount"),
			new FieldMapping(HEADER_LDC_SECTION_DETAILS, "ldcSection", "ldcSection"),
			new FieldMapping(HEADER_ASSESSING_OFFICER_DETAILS, "assessingOfficerDetails", "assessingOfficerDetails"),
			new FieldMapping(HEADER_DEDUCTOR_TAN, "deductorMasterTan", "deductorMasterTan", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_APPROVED, "isApproved", "isApproved")));

	public TracesLdcMasterExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, NewLDCMasterTraces.class, TracesLdcMasterErrorReportDTO.class);

	}

	// to validate the row data of LDCExcel sheet
	public Optional<TracesLdcMasterErrorReportDTO> validate(int rowIndex) {
		StringJoiner errorMessages = new StringJoiner("\n");

		// validation check
		for (FieldMapping fieldMapping : fieldMappings) {
			if (fieldMapping.getValidator() != null) {
				String validationMessage = fieldMapping.getValidator().apply(
						this.getHeaders().get(this.getHeaderIndex(fieldMapping.getExcelHeaderName().toLowerCase())),
						getRawCellValue(rowIndex, fieldMapping.getExcelHeaderName()));
				if (StringUtils.isNotEmpty(validationMessage)) {
					errorMessages.add(validationMessage);
				}
			}
			if (fieldMapping.getValidatorDouble() != null) {

				String validationMessage = getRawCellValue(rowIndex, fieldMapping.getExcelHeaderName());
				String headerNamee = this.getHeaders()
						.get(this.getHeaderIndex(fieldMapping.getExcelHeaderName().toLowerCase()));
				if (StringUtils.isBlank(validationMessage))
					errorMessages.add(headerNamee + " can not be empty");
			}
		}

		if (errorMessages.length() != 0) {
			TracesLdcMasterErrorReportDTO ldcMasterErrorReportCsvDTO = this.getErrorDTO(rowIndex);
			ldcMasterErrorReportCsvDTO.setReason(errorMessages.toString());
			return Optional.of(ldcMasterErrorReportCsvDTO);
		} else {
			return Optional.empty();
		}
	}

}
