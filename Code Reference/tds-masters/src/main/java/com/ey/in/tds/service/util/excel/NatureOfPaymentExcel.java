package com.ey.in.tds.service.util.excel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ey.in.tds.dto.NOPExcelErrorDTO;
import com.ey.in.tds.dto.NatureOfPaymentMasterExcelDTO;
import com.ey.in.tds.service.sac.FieldMapping;

public class NatureOfPaymentExcel extends MasterExcel<NatureOfPaymentMasterExcelDTO, NOPExcelErrorDTO> {

	private static final String HEADER_NATURE_OF_PAYMENT = "Nature of payment";
	private static final String HEADER_SECTION = "Section";
	private static final String HEADER_SAC_CODE = "SAC Code";
	private static final String HEADER_DEDUCTEE_STATUS = "Deductee Status";
	private static final String HEADER_RESIDENTIAL_STATUS = "Residential Status (Y/N)";
	private static final String HEADER_IS_ANNUAL_TRANSACTION_LIMIT_APPLICABLE = "is annual Transaction limit applicable? (Y/N)";
	private static final String HEADER_ANNUAL_TRANSACTION_LIMIT = "annual transaction limit";
	private static final String HEADER_PER_IS_TRANSACTION_LIMIT_APPLICABLE = "Is per transaction limit applicable? (Y/N)";
	private static final String HEADER_PER_TRANSACTION_LIMIT_AMOUNT = "per tansaction limit amount";
	private static final String HEADER_RATE = "Rate";
	private static final String HEADER_APPLICABLE_FROM = "applicable from";
	private static final String HEADER_APPLICABLE_TO = "applicable to";
	private static final String HEADER_DISPLAY_VALUE = "Display Value";
	private static final String HEADER_CONSIDER_DATE_OF_PAYMENT_FOR_INTEREST = "Consider Date of payment for interest? (Y/N)";
	private static final String HEADER_NO_PAN_RATE = "No Pan Rate";
	private static final String HEADER_NO_ITR_RATE = "No Itr Rate";
	private static final String HEADER_NO_PAN_RATE_AND_NO_ITR_RATE = "No Pan Rate And No Itr Rate";

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	// author dipak
	@Override
	public NatureOfPaymentMasterExcelDTO get(int index) {
		NatureOfPaymentMasterExcelDTO nop = new NatureOfPaymentMasterExcelDTO();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(nop, index, fieldMapping);
		}
		return nop;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
			new FieldMapping(HEADER_NATURE_OF_PAYMENT, "natureOfPayment", "natureOfPayment",
					MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_SECTION, "section", "section", MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_SAC_CODE, "sacCode", "sacCode", MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_DEDUCTEE_STATUS, "deducteeStatus", "deducteeStatus",
					MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_RESIDENTIAL_STATUS, "residentialStatus", "residentialStatus",
					MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_IS_ANNUAL_TRANSACTION_LIMIT_APPLICABLE, "isAnnualTransactionLimitApplicable",
					"isAnnualTransactionLimitApplicable"),
			new FieldMapping(HEADER_ANNUAL_TRANSACTION_LIMIT, "annualTransactionLimit", "annualTransactionLimit"),
			new FieldMapping(HEADER_PER_IS_TRANSACTION_LIMIT_APPLICABLE, "isPerTransactionLimitApplicable",
					"isPerTransactionLimitApplicable"),
			new FieldMapping(HEADER_PER_TRANSACTION_LIMIT_AMOUNT, "perTansactionLimitAmount",
					"perTansactionLimitAmount"),
			new FieldMapping(HEADER_RATE, "rate", "rate", MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_APPLICABLE_FROM, "applicableFrom", "applicableFrom",
					MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_APPLICABLE_TO, "applicableTo", "applicableTo"),
			new FieldMapping(HEADER_DISPLAY_VALUE, "displayValue", "displayValue", MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_CONSIDER_DATE_OF_PAYMENT_FOR_INTEREST, "considerDateofPayment",
					"considerDateofPayment", MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_NO_PAN_RATE, "noPanRate", "noPanRate", MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_NO_ITR_RATE, "noItrRate", "noItrRate", MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_NO_PAN_RATE_AND_NO_ITR_RATE, "noPanRateAndNoItrRate", "noPanRateAndNoItrRate",
					MasterExcel.VALIDATION_MANDATORY)));

	public NatureOfPaymentExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, NatureOfPaymentMasterExcelDTO.class, NOPExcelErrorDTO.class);

	}

	// to validate the row data of LDCExcel sheet
	public Optional<NOPExcelErrorDTO> validate(int rowIndex) {
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
			NOPExcelErrorDTO nopError = this.getErrorDTO(rowIndex);
			nopError.setReason(errorMessages.toString());
			return Optional.of(nopError);
		} else {
			return Optional.empty();
		}
	}
}