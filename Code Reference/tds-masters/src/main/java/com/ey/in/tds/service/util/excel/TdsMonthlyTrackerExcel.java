package com.ey.in.tds.service.util.excel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ey.in.tds.dto.TdsMonthTrackereDTO;
import com.ey.in.tds.dto.TdsMonthTrackereErrorReportDTO;
import com.ey.in.tds.service.sac.FieldMapping;

/**
 * 
 * @author vamsir
 *
 */
public class TdsMonthlyTrackerExcel extends MasterExcel<TdsMonthTrackereDTO, TdsMonthTrackereErrorReportDTO> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String HEADER_MONTH = "Month";
	private static final String HEADER_YEAR = "Year";
	private static final String HEADER_DUE_DATE_FOR_FILING = "Due date for filing";
	private static final String HEADER_DUE_DATE_FOR_CHALLAN_PAYMENT = "Due date for Challan payment";
	private static final String HEADER_MONTH_CLOSURE_FOR_PROCESSING = "Month closure for processing";
	private static final String HEADER_APPLICABLE_FROM = "Applicable from";
	private static final String HEADER_APPLICABLE_TO = "Applicable to";

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static final List<FieldMapping> fieldMappings = Collections.unmodifiableList(
			Arrays.asList(new FieldMapping(HEADER_MONTH, "month", "month", MasterExcel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_YEAR, "year", "year", MasterExcel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_DUE_DATE_FOR_FILING, "dueDateForFiling", "dueDateForFiling",
							MasterExcel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_DUE_DATE_FOR_CHALLAN_PAYMENT, "dueDateForChallanPayment",
							"dueDateForChallanPayment", MasterExcel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_MONTH_CLOSURE_FOR_PROCESSING, "monthClosureForProcessing",
							"monthClosureForProcessing", MasterExcel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_APPLICABLE_FROM, "applicableFrom", "applicableFrom",
							MasterExcel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_APPLICABLE_TO, "applicableTo", "applicableTo")));

	public TdsMonthlyTrackerExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, TdsMonthTrackereDTO.class, TdsMonthTrackereErrorReportDTO.class);
	}

	@Override
	public TdsMonthTrackereDTO get(int index) {
		TdsMonthTrackereDTO monthTracker = new TdsMonthTrackereDTO();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(monthTracker, index, fieldMapping);
		}
		return monthTracker;
	}

	public Optional<TdsMonthTrackereErrorReportDTO> validate(int rowIndex) {
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
			TdsMonthTrackereErrorReportDTO monthTrackerErrorReport = this.getErrorDTO(rowIndex);
			monthTrackerErrorReport.setReason(errorMessages.toString());
			return Optional.of(monthTrackerErrorReport);
		} else {
			return Optional.empty();
		}
	}

}
