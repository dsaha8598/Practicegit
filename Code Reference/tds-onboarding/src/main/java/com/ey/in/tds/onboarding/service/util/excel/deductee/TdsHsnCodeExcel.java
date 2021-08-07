package com.ey.in.tds.onboarding.service.util.excel.deductee;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ey.in.tds.common.dto.TdsHsnCodeExcelDTO;
import com.ey.in.tds.common.dto.TdsHsnCodeErrorReportDTO;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;

public class TdsHsnCodeExcel extends Excel<TdsHsnCodeExcelDTO, TdsHsnCodeErrorReportDTO> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String HEADER_HSN_OR_SAC_CODE = "HSN/SAC Code";
	private static final String HEADER_DESCRIPTION = "Description";
	private static final String HEADER_TDS_SECTION = "TDS Section";
	private static final String HEADER_NATURE_OF_PAYMENT = "Nature of Payment";

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static final List<FieldMapping> fieldMappings = Collections.unmodifiableList(
			Arrays.asList(new FieldMapping(HEADER_HSN_OR_SAC_CODE, "hsnCode", "hsnCode", Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_DESCRIPTION, "desc", "desc", Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_TDS_SECTION, "tdsSection", "tdsSection"),
					new FieldMapping(HEADER_NATURE_OF_PAYMENT, "natureOfPayment", "natureOfPayment")));

	public TdsHsnCodeExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, TdsHsnCodeExcelDTO.class, TdsHsnCodeErrorReportDTO.class);
	}

	@Override
	public TdsHsnCodeExcelDTO get(int index) {
		TdsHsnCodeExcelDTO tdsHsnCode = new TdsHsnCodeExcelDTO();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(tdsHsnCode, index, fieldMapping);
		}
		return tdsHsnCode;
	}

	public Optional<TdsHsnCodeErrorReportDTO> validate(int rowIndex) {
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
			TdsHsnCodeErrorReportDTO tdsHsnErrorReport = this.getErrorDTO(rowIndex);
			tdsHsnErrorReport.setReason(errorMessages.toString());
			return Optional.of(tdsHsnErrorReport);
		} else {
			return Optional.empty();
		}
	}

}
