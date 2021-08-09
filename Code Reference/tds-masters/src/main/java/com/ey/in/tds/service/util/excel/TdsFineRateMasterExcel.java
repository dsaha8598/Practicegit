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

import com.ey.in.tds.dto.FineRateMasterDTO;
import com.ey.in.tds.dto.FineRateMasterErrorReportDTO;
import com.ey.in.tds.service.sac.FieldMapping;

/**
 * 
 * @author vamsir
 *
 */
public class TdsFineRateMasterExcel extends MasterExcel<FineRateMasterDTO, FineRateMasterErrorReportDTO> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String HEADER_INTEREST_TYPE = "Interest type";
	private static final String HEADER_RATE = "Rate";
	private static final String HEADER_FINE_PER_DAY = "Fine per day";
	private static final String HEADER_TYPE_OF_INTEREST_CALCULATION = "Type of interest calculation";
	private static final String HEADER_APPLICABLE_FROM = "Applicable from";
	private static final String HEADER_APPLICABLE_TO = "Applicable to";

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static final List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(

			new FieldMapping(HEADER_INTEREST_TYPE, "interestType", "interestType", MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_RATE, "rate", "rate"),
			new FieldMapping(HEADER_FINE_PER_DAY, "finePerDay", "finePerDay"),
			new FieldMapping(HEADER_TYPE_OF_INTEREST_CALCULATION, "typeOfIntrestCalculation",
					"typeOfIntrestCalculation", MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_APPLICABLE_FROM, "applicableFrom", "applicableFrom",
					MasterExcel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_APPLICABLE_TO, "applicableTo", "applicableTo")));

	public TdsFineRateMasterExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, FineRateMasterDTO.class, FineRateMasterErrorReportDTO.class);
	}

	@Override
	public FineRateMasterDTO get(int index) {
		FineRateMasterDTO fineRateMaster = new FineRateMasterDTO();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(fineRateMaster, index, fieldMapping);
		}
		return fineRateMaster;
	}

	public Optional<FineRateMasterErrorReportDTO> validate(int rowIndex) {
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
			FineRateMasterErrorReportDTO fineRateMasterErrors = this.getErrorDTO(rowIndex);
			fineRateMasterErrors.setReason(errorMessages.toString());
			return Optional.of(fineRateMasterErrors);
		} else {
			return Optional.empty();
		}
	}

}
