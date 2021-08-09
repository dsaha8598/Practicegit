package com.ey.in.tds.onboarding.service.util.excel.ldc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ey.in.tds.common.model.ldc.LccErrorReportCsvDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccMaster;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;

/**
 * 
 * @author vamsir
 *
 */
public class LccExcel extends Excel<TCSLccMaster, LccErrorReportCsvDTO> {

	private static final String HEADER_LCC_CERTIFICATE_NUMBER = "LCC Certificate Number";
	private static final String HEADER_COLLECTOR_TAN = "Collector TAN";
	private static final String HEADER_COLLECTEE_NAME = "Collectee Name";
	private static final String HEADER_COLLECTEE_PAN = "Collectee PAN";
	private static final String HEADER_SECTION = "Section";
	private static final String HEADER_NATURE_OF_INCOME = "Nature of income";
	private static final String HEADER_AMOUNT = "Amount";
	private static final String HEADER_CERTIFICATE_RATE = "Certificate rate";
	private static final String HEADER_APPLICABLE_FROM = "Valid From";
	private static final String HEADER_APPLICABLE_TO = "Valid To";
	private static final String HEADER_CANCEL_DATE = "Cancel Date";
	private static final String HEADER_TRACES_VALIDATION_STATUS = "Traces validation status";
	private static final String HEADER_TRACES_VALIDATION_DATE = "Traces validation date";
	private static final String HEADER_LIMIT_UTILISED = "Limit utilised";
	private static final String HEADER_ACTION = "Action";

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	@Override
	public TCSLccMaster get(int index) {
		TCSLccMaster lcc = new TCSLccMaster();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(lcc, index, fieldMapping);
		}
		return lcc;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
			new FieldMapping(HEADER_LCC_CERTIFICATE_NUMBER, "certificateNumber", "certificateNumber",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_COLLECTOR_TAN, "collectorMasterTan", "collectorMasterTan",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_COLLECTEE_NAME, "collecteeName", "collecteeName", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_COLLECTEE_PAN, "lccMasterPan", "lccMasterPan", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_NATURE_OF_INCOME, "natureOfIncome", "natureOfIncome"),
			new FieldMapping(HEADER_SECTION, "sectionAsPerTraces", "sectionAsPerTraces", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_AMOUNT, "amount", "amount", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_CERTIFICATE_RATE, "rateAsPerTraces", "rateAsPerTraces", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_CANCEL_DATE, "applicableFrom", "cancelDate"),
			new FieldMapping(HEADER_APPLICABLE_FROM, "applicableFrom", "applicableFrom", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_APPLICABLE_TO, "applicableTo", "applicableTo", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_LIMIT_UTILISED, "utilizedAmount", "utilizedAmount"),
			new FieldMapping(HEADER_TRACES_VALIDATION_STATUS, "validationStatus", "validationStatus"),
			new FieldMapping(HEADER_TRACES_VALIDATION_DATE, "validationDate", "validationDate"),
			new FieldMapping(HEADER_ACTION, "action", "action")));

	public LccExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, TCSLccMaster.class, LccErrorReportCsvDTO.class);

	}

	// to validate the row data of LCCExcel sheet
	public Optional<LccErrorReportCsvDTO> validate(int rowIndex) {
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
			LccErrorReportCsvDTO lccMasterErrorReportCsvDTO = this.getErrorDTO(rowIndex);
			lccMasterErrorReportCsvDTO.setReason(errorMessages.toString());
			return Optional.of(lccMasterErrorReportCsvDTO);
		} else {
			return Optional.empty();
		}
	}
}
