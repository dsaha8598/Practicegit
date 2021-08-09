package com.ey.in.tds.service.util.excel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;
import com.ey.in.tds.dto.DividendRateActErrorDTO;
import com.ey.in.tds.dto.DividendRateActExcelDTO;

public class DividendRateActExcel extends Excel<DividendRateActExcelDTO, DividendRateActErrorDTO> {

		private static final String HEADER_SERIAL_NO="S. No";
		private static final String HEADER_TYPE_OF_DEDUCTOR = "Type of deductor";
		private static final String HEADER_CATAGORY = "Category of shareholder (Recipient)";
		private static final String HEADER_RESIDENTIAL_STATUS = "Residential Status (Resident/ Non Resident)";
		private static final String HEADER_THRESHOLD_LIMIT = "Threshold Limit";
		private static final String HEADER_APPLICABLE_RATE = "Applicable rate (%)";
		private static final String HEADER_APPLICABLE_FROM = "Applicable From (yyyy-MM-dd)";
		private static final String HEADER_APPLICABLE_TO = "Applicable To (yyyy-MM-dd)";
		

		@Override
		public List<FieldMapping> getFieldMappings() {
			return fieldMappings;
		}

		// author dipak
		@Override
		public DividendRateActExcelDTO get(int index) {
			DividendRateActExcelDTO dividendRate = new DividendRateActExcelDTO();
			for (FieldMapping fieldMapping : fieldMappings) {
				populateValue(dividendRate, index, fieldMapping);
			}
			return dividendRate;
		}

		public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
				new FieldMapping(HEADER_SERIAL_NO, "dividendDeductorType", "dividendDeductorType"),
				new FieldMapping(HEADER_TYPE_OF_DEDUCTOR, "dividendDeductorType", "dividendDeductorType",Excel.VALIDATION_MANDATORY),
				new FieldMapping(HEADER_CATAGORY, "shareholderCategory", "shareholderCategory"),
				new FieldMapping(HEADER_RESIDENTIAL_STATUS, "residentialStatus", "residentialStatus", Excel.VALIDATION_MANDATORY),
				new FieldMapping(HEADER_THRESHOLD_LIMIT, "threshHoldLimit", "threshHoldLimit",
						Excel.VALIDATION_MANDATORY),
				new FieldMapping(HEADER_APPLICABLE_RATE, "applicaationRate",
						"applicaationRate",Excel.VALIDATION_MANDATORY),
				new FieldMapping(HEADER_APPLICABLE_FROM, "applicableFrom", "applicableFrom",Excel.VALIDATION_MANDATORY),
				new FieldMapping(HEADER_APPLICABLE_TO, "applicableTo",
						"applicableTo")));

		public DividendRateActExcel(XSSFWorkbook workbook) {
			super(workbook, fieldMappings, DividendRateActExcelDTO.class, DividendRateActErrorDTO.class);

		}

		// to validate the row data of LDCExcel sheet
		public Optional<DividendRateActErrorDTO> validate(int rowIndex) {
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
				DividendRateActErrorDTO nopError = this.getErrorDTO(rowIndex);
				nopError.setReason(errorMessages.toString());
				return Optional.of(nopError);
			} else {
				return Optional.empty();
			}
		}
	}
