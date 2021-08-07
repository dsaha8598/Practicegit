package com.ey.in.tds.onboarding.service.chartofaccounts;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ey.in.tds.common.dto.chartofaccounts.ChartOfAccountsErrorDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.ChartOfAccounts;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;

/**
 * 
 * @author
 *
 */
public class ChartOfAccountsExcel extends Excel<ChartOfAccounts, ChartOfAccountsErrorDTO> {

	private static final String HEADER_ACCOUNT_CODE = "GLAccountCode";
	private static final String HEADER_ACCOUNT_TYPE = "GLAccountType";
	private static final String HEADER_ACCOUNT_DESCRIPTION = "GLAccountName";
	private static final String HEADER_CLASSIFICATION = "GLAccountClassification";
	private static final String HEADER_TDS_SECTION = "TDSSection";
	private static final String HEADER_NATURE_OF_PAYMENT = "NatureofPayment";

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	@Override
	public ChartOfAccounts get(int index) {
		ChartOfAccounts coa = new ChartOfAccounts();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(coa, index, fieldMapping);
		}
		return coa;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
			new FieldMapping(HEADER_ACCOUNT_CODE, "accountCode", "accountCode", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_ACCOUNT_TYPE, "accountType", "accountType", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_ACCOUNT_DESCRIPTION, "accountDescription", "accountDescription",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_CLASSIFICATION, "classification", "classification"),
			new FieldMapping(HEADER_TDS_SECTION, "tdsSection", "tdsSection"),
			new FieldMapping(HEADER_NATURE_OF_PAYMENT, "natureOfPayment", "natureOfPayment")));

	public ChartOfAccountsExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, ChartOfAccounts.class, ChartOfAccountsErrorDTO.class);

	}

	// to validate the row data of Chart of Account sheet
	public Optional<ChartOfAccountsErrorDTO> validate(int rowIndex) {
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
			ChartOfAccountsErrorDTO ldcMasterErrorReportCsvDTO = this.getErrorDTO(rowIndex);
			ldcMasterErrorReportCsvDTO.setReason(errorMessages.toString());
			return Optional.of(ldcMasterErrorReportCsvDTO);
		} else {
			return Optional.empty();
		}
	}
}
