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

import com.ey.in.tcs.common.domain.DeducteeThresholdUpdate;
import com.ey.in.tds.common.model.deductee.DeducteeThresholdUpdateErrorDTO;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;

/**
 * 
 * @author vamsir
 *
 */
public class DeducteeThresholdExcel extends Excel<DeducteeThresholdUpdate, DeducteeThresholdUpdateErrorDTO> {
					

	private static final String HEADER_DEDUCTEE_CODE = "Deductee Code";
	private static final String HEADER_DEDUCTEE_NAME = "Deductee Name";
	private static final String HEADER_DEDUCTEE_PAN = "Deductee PAN";
	private static final String HEADER_THRESHOLD_AMOUNT = "Threshold Amount";
	private static final String HEADER_NATURE_OF_PAYMENT = "Nature of Payment";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static List<FieldMapping> fieldMappings = Collections
			.unmodifiableList(Arrays.asList(new FieldMapping(HEADER_DEDUCTEE_CODE, "deducteeCode", "deducteeCode"),
					new FieldMapping(HEADER_DEDUCTEE_NAME, "deducteeName", "deducteeName", Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_DEDUCTEE_PAN, "deducteePan", "deducteePan"),
					new FieldMapping(HEADER_THRESHOLD_AMOUNT, "thresholdAmount", "thresholdAmount",
							Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_NATURE_OF_PAYMENT, "natureOfPayment", "natureOfPayment",
							Excel.VALIDATION_MANDATORY)));

	public DeducteeThresholdExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, DeducteeThresholdUpdate.class, DeducteeThresholdUpdateErrorDTO.class);
	}

	@Override
	public DeducteeThresholdUpdate get(int index) {
		DeducteeThresholdUpdate declaration = new DeducteeThresholdUpdate();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(declaration, index, fieldMapping);
		}
		return declaration;
	}

	public Optional<DeducteeThresholdUpdateErrorDTO> validate(int rowIndex) {
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
			DeducteeThresholdUpdateErrorDTO declarationErrorFileDTO = this.getErrorDTO(rowIndex);
			declarationErrorFileDTO.setReason(errorMessages.toString());
			return Optional.of(declarationErrorFileDTO);
		} else {
			return Optional.empty();
		}
	}

}
