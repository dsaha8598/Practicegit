package com.ey.in.tds.onboarding.tcs.service.chartofaccounts;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ey.in.tds.common.dto.chartofaccounts.ChartOfAccountsErrorDTO;
import com.ey.in.tds.common.onboarding.tcs.jdbc.dto.ChartOfAccountsTCS;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;
/**
 * 
 * @author dipak
 *
 */
public class ChartOfAccountTCSExcel extends Excel<ChartOfAccountsTCS,  ChartOfAccountsErrorDTO>{

	private static final String HEADER_ACCOUNT_CODE = "Account Code";
	private static final String HEADER_ACCOUNT_TYPE = "Account Type";
	private static final String HEADER_ACCOUNT_DESCRIPTION = "Account Description";
	private static final String HEADER_CLASSIFICATION = "Classification";

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	@Override
	public ChartOfAccountsTCS get(int index) {
		ChartOfAccountsTCS coa = new ChartOfAccountsTCS();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(coa, index, fieldMapping);
		}
		return coa;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
			new FieldMapping(HEADER_ACCOUNT_CODE, "accountCode", "accountCode",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_ACCOUNT_TYPE, "accountType", "accountType"),
			new FieldMapping(HEADER_ACCOUNT_DESCRIPTION, "accountDescription", "accountDescription"),
			new FieldMapping(HEADER_CLASSIFICATION, "classification", "classification")));

	public ChartOfAccountTCSExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, ChartOfAccountsTCS.class, ChartOfAccountsErrorDTO.class);

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
