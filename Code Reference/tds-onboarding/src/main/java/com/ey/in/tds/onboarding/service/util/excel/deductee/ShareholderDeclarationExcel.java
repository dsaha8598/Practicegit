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

import com.ey.in.tds.common.model.deductee.DeclarationErrorDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderDeclaration;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;

/**
 * 
 * @author vamsir
 *
 */
public class ShareholderDeclarationExcel extends Excel<ShareholderDeclaration, DeclarationErrorDTO> {

	private static final String HEADER_FOLIO_NUMBER = "Folio Number";
	private static final String HEADER_RATE_TYPE = "Rate Type";
	private static final String HEADER_TDS_OR_TCS = "Tds or Tcs Applicability";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
			new FieldMapping(HEADER_FOLIO_NUMBER, "folioNumber", "folioNumber", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_RATE_TYPE, "rateType", "rateType"),
			new FieldMapping(HEADER_TDS_OR_TCS, "tdsOrTcs", "tdsOrTcs", Excel.VALIDATION_MANDATORY)));

	public ShareholderDeclarationExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, ShareholderDeclaration.class, DeclarationErrorDTO.class);
	}

	@Override
	public ShareholderDeclaration get(int index) {
		ShareholderDeclaration declaration = new ShareholderDeclaration();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(declaration, index, fieldMapping);
		}
		return declaration;
	}

	public Optional<DeclarationErrorDTO> validate(int rowIndex) {
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
			DeclarationErrorDTO declarationErrorFileDTO = this.getErrorDTO(rowIndex);
			declarationErrorFileDTO.setReason(errorMessages.toString());
			return Optional.of(declarationErrorFileDTO);
		} else {
			return Optional.empty();
		}
	}

}
