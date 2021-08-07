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

import com.ey.in.tcs.common.domain.CollecteeThresholdUpdate;
import com.ey.in.tds.common.model.deductee.CollecteeThresholdUpdateErrorDTO;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;

/**
 * 
 * @author Amani
 *
 */
public class CollecteeThresholdExcel extends Excel<CollecteeThresholdUpdate, CollecteeThresholdUpdateErrorDTO> {

	private static final String HEADER_COLLECTEE_CODE = "Collectee Code";
	private static final String HEADER_COLLECTEE_PAN = "Collectee Pan";
	private static final String HEADER_THRESHOLD_AMOUNT = "Threshold Amount";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
			new FieldMapping(HEADER_COLLECTEE_CODE, "collecteeCode", "collecteeCode", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_COLLECTEE_PAN, "collecteePan", "collecteePan"), new FieldMapping(
					HEADER_THRESHOLD_AMOUNT, "thresholdAmount", "thresholdAmount", Excel.VALIDATION_MANDATORY)));

	public CollecteeThresholdExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, CollecteeThresholdUpdate.class, CollecteeThresholdUpdateErrorDTO.class);
	}

	@Override
	public CollecteeThresholdUpdate get(int index) {
		CollecteeThresholdUpdate declaration = new CollecteeThresholdUpdate();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(declaration, index, fieldMapping);
		}
		return declaration;
	}

	public Optional<CollecteeThresholdUpdateErrorDTO> validate(int rowIndex) {
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
			CollecteeThresholdUpdateErrorDTO declarationErrorFileDTO = this.getErrorDTO(rowIndex);
			declarationErrorFileDTO.setReason(errorMessages.toString());
			return Optional.of(declarationErrorFileDTO);
		} else {
			return Optional.empty();
		}
	}

}
