package com.ey.in.tds.service.util.excel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ey.in.tds.common.dto.ExemptionExcelDTO;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;
import com.ey.in.tds.dto.ExemptionErrorDTO;

public class ExemptionExcel extends Excel<ExemptionExcelDTO, ExemptionErrorDTO> {

	private static final String HEADER_DEDUCTOR_TYPE="Deductor Type";
	private static final String HEADER_SHAREHOLDER_CATAGORY = "Category of Shareholder";
	private static final String HEADER_RESIDENTIAL_STATUS = "Residential status";
	private static final String HEADER_SECTION = "Section";
	

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	// author dipak
	@Override
	public ExemptionExcelDTO get(int index) {
		ExemptionExcelDTO exemption = new ExemptionExcelDTO();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(exemption, index, fieldMapping);
		}
		return exemption;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
			new FieldMapping(HEADER_DEDUCTOR_TYPE, "deductorType", "deductorType",Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_SHAREHOLDER_CATAGORY, "shareHolderCatagory", "shareHolderCatagory",Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_RESIDENTIAL_STATUS, "residentialStatus", "residentialStatus"),
			new FieldMapping(HEADER_SECTION, "section", "section",
					Excel.VALIDATION_MANDATORY)));

	public ExemptionExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, ExemptionExcelDTO.class, ExemptionErrorDTO.class);

	}

	// to validate the row data of LDCExcel sheet
	public Optional<ExemptionErrorDTO> validate(int rowIndex) {
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
			ExemptionErrorDTO exemptionerror = this.getErrorDTO(rowIndex);
			exemptionerror.setReason(errorMessages.toString());
			return Optional.of(exemptionerror);
		} else {
			return Optional.empty();
		}
	}
}
