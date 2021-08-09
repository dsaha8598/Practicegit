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

import com.ey.in.tds.dto.CessTypeMasterErrorRepotDTO;
import com.ey.in.tds.dto.CessTypeMasterExcelDTO;
import com.ey.in.tds.service.sac.FieldMapping;

/**
 * 
 * @author vamsir
 *
 */
public class CessTypeMasterExcel extends MasterExcel<CessTypeMasterExcelDTO, CessTypeMasterErrorRepotDTO> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String HEADER_CESS_TYPE = "Cess type";
	private static final String HEADER_APPLICABLE_FROM = "Applicable from";
	private static final String HEADER_APPLICABLE_TO = "Applicable to";

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static final List<FieldMapping> fieldMappings = Collections.unmodifiableList(
			Arrays.asList(new FieldMapping(HEADER_CESS_TYPE, "cessType", "cessType", MasterExcel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_APPLICABLE_FROM, "applicableFrom", "applicableFrom",
							MasterExcel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_APPLICABLE_TO, "applicableTo", "applicableTo")));

	public CessTypeMasterExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, CessTypeMasterExcelDTO.class, CessTypeMasterErrorRepotDTO.class);
	}

	@Override
	public CessTypeMasterExcelDTO get(int index) {
		CessTypeMasterExcelDTO cessTypeMaster = new CessTypeMasterExcelDTO();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(cessTypeMaster, index, fieldMapping);
		}
		return cessTypeMaster;
	}

	public Optional<CessTypeMasterErrorRepotDTO> validate(int rowIndex) {
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
			CessTypeMasterErrorRepotDTO cessTypeMasterErrorReport = this.getErrorDTO(rowIndex);
			cessTypeMasterErrorReport.setReason(errorMessages.toString());
			return Optional.of(cessTypeMasterErrorReport);
		} else {
			return Optional.empty();
		}
	}

}
